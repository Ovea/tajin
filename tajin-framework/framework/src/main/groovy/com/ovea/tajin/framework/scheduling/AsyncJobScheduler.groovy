/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.tajin.framework.scheduling

import com.google.common.cache.LoadingCache
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.ovea.tajin.framework.jmx.Access
import com.ovea.tajin.framework.jmx.JmxSelfNaming
import com.ovea.tajin.framework.jmx.annotation.JmxBean
import com.ovea.tajin.framework.jmx.annotation.JmxField
import com.ovea.tajin.framework.jmx.annotation.JmxProperty
import com.ovea.tajin.framework.util.PropertySettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.management.MalformedObjectNameException
import javax.management.ObjectName
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
@javax.inject.Singleton
@JmxBean
class AsyncJobScheduler implements JobScheduler, JmxSelfNaming {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncJobScheduler)
    private static final AtomicLong INSTANCES = new AtomicLong(-1)

    @Inject
    LoadingCache<String, JobExecutor> executors

    @Inject
    JobRepository repository

    @Inject
    PropertySettings settings

    JobScheduler.OnError onError = JobScheduler.OnError.LOG

    @JmxField(access = Access.RW)
    int maxRetry = -1

    @JmxField(access = Access.RW)
    int retryDelay = 300

    private ScheduledExecutorService service
    private final ConcurrentHashMap<Job, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>()
    private final AtomicLong nRunning = new AtomicLong()
    private final AtomicLong nRan = new AtomicLong()
    private final AtomicLong nFailed = new AtomicLong()

    AsyncJobScheduler() {
        INSTANCES.incrementAndGet()
    }

    @JmxProperty
    long getScheduledCount() { scheduledJobs.size() }

    @JmxProperty
    long getRunningCount() { nRunning.get() }

    @JmxProperty
    long getTotalExecutionCount() { nRan.get() }

    @JmxProperty
    long getFailedCount() { nFailed.get() }

    @JmxProperty
    Collection<String> getScheduledJobs() {
        long now = System.currentTimeMillis()
        scheduledJobs.collect { k, v -> "${k.id} ${k.name} in ${Math.max(0, k.start.time - now) / 1000}s" as String }
    }

    @Override
    ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.ovea.tajin:type=${getClass().simpleName},name=${INSTANCES.get()}")
    }

    @PostConstruct
    void init() {
        maxRetry = settings.getInt('scheduling.maxRetry', -1)
        retryDelay = settings.getInt('scheduling.retryDelay', 300)
        service = new ScheduledThreadPoolExecutor(
            settings.getInt('scheduling.pool.size', Runtime.runtime.availableProcessors() * 2),
            new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("${AsyncJobScheduler.simpleName}-thread-%d")
                .setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                void uncaughtException(Thread t, Throwable e) {
                    LOGGER.error("UncaughtException in Job Scheduler: ${e.message}", e)
                }
            }).build()
        )
        List<Job> jobs = repository.listPendingJobs()
        // filter jobs that can be retried
        if (maxRetry > -1) {
            jobs = jobs.findAll { it.retry <= maxRetry }
        }
        jobs.each { doSchedule(new PersistentJobRunner(it)) }
    }

    @PreDestroy
    void close() {
        while (scheduledJobs) {
            scheduledJobs.keySet().each { scheduledJobs.remove(it)?.cancel(false) }
        }
        service.shutdown()
        try {
            service.awaitTermination(10, TimeUnit.SECONDS)
        } catch (ignored) {
        }
    }

    @Override
    void cancel(Collection<String> jobIds) {
        if (jobIds) {
            def entries = scheduledJobs.find { k, v -> k.id in jobIds }
            def now = new Date()
            entries.each {
                it.value.cancel(false)
                it.key.updatedDate = now
                scheduledJobs.remove(it.key)
            }
            repository.delete(entries.collect { it.key })
        }
    }

    @Override
    Job submit(String jobName, Map<String, ?> data) { submit(jobName, new Date(), data) }

    @Override
    Job submit(String jobName, Date time, Map<String, ?> data) {
        if (!jobName) throw new IllegalArgumentException('Missing jobName')
        if (!time) throw new IllegalArgumentException('Missing time')
        // will fail if jobName not found
        executors.get(jobName)
        // create and process job
        Job job = new Job(
            name: jobName,
            start: time,
            data: data ? data.deepClone() : [:]
        )
        doSchedule(new JobRunner(job))
        return job
    }

    @Override
    Job schedule(String jobName, Map<String, ?> data) { schedule(jobName, new Date(), data) }

    @Override
    Job schedule(String jobName, Date time, Map<String, ?> data) {
        if (!jobName) throw new IllegalArgumentException('Missing jobName')
        if (!time) throw new IllegalArgumentException('Missing time')
        // will fail if jobName not found
        executors.get(jobName)
        // create and process job
        Job job = new Job(
            name: jobName,
            start: time,
            data: data ? data.deepClone() : [:]
        )
        // save the job then after save, schedule it
        save job
        doSchedule new PersistentJobRunner(job)
        return job
    }

    private void doSchedule(JobRunner jobRunner) {
        if (!service.shutdown && !service.terminated) {
            long diff = Math.max(0, jobRunner.job.start.time - System.currentTimeMillis())
            LOGGER.trace("Scheduling: ${jobRunner.job} in ${diff / 1000}s")
            ScheduledFuture<?> future = service.schedule(jobRunner, diff, TimeUnit.MILLISECONDS)
            scheduledJobs.put(jobRunner.job, future)
        } else {
            throw new IllegalStateException('Job Scheduler is closing or closed and cannot accept new job. Job ' + jobRunner.job + ' will be executed at next startup.')
        }
    }

    private void save(Job job) {
        job.updatedDate = new Date()
        repository.save(job)
    }

    private class JobRunner implements Runnable {
        final Job job

        JobRunner(Job job) {
            this.job = job
        }

        @Override
        final void run() {
            try {
                nRunning.incrementAndGet()
                executors.get(job.name).execute(job.data)
                scheduledJobs.remove(job)
                job.end = new Date()
                onComplete()
            } catch (e) {
                scheduledJobs.remove(job)
                nFailed.incrementAndGet()
                job.retry++
                onFailure()
                if (retryable) {
                    job.start = new Date(System.currentTimeMillis() + retryDelay * 1000)
                    doSchedule this
                }
                onError.onError(job, e)
            } finally {
                nRunning.decrementAndGet()
                nRan.incrementAndGet()
            }
        }

        void onComplete() {}

        void onFailure() {}

        boolean isRetryable() { false }
    }

    private class PersistentJobRunner extends JobRunner {
        PersistentJobRunner(Job job) {
            super(job)
        }

        @Override
        void onComplete() { save job }

        @Override
        void onFailure() { save job }

        @Override
        boolean isRetryable() { maxRetry == -1 || job.retry < maxRetry }
    }
}
