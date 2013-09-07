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
package com.ovea.tajin.framework.async

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.mycila.jmx.JmxSelfNaming
import com.mycila.jmx.annotation.JmxBean
import com.mycila.jmx.annotation.JmxProperty
import com.ovea.tajin.framework.core.Settings

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.management.MalformedObjectNameException
import javax.management.ObjectName
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
@JmxBean
@javax.inject.Singleton
class DefaultJobScheduler implements JobScheduler, JmxSelfNaming {

    private static final Logger LOGGER = Logger.getLogger(DefaultJobScheduler.simpleName)
    private static final AtomicLong INSTANCES = new AtomicLong(-1)

    private final ConcurrentHashMap<TriggeredScheduledJob, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>()

    private ScheduledExecutorService executorService

    @Inject JobRepository repository = new EmptyJobRepository()
    @Inject JobListener listener = new EmptyJobListener()

    int poolSize = Runtime.runtime.availableProcessors() * 2
    boolean enabled = true

    DefaultJobScheduler() {
        INSTANCES.incrementAndGet()
    }

    @Inject
    void setSettings(Settings settings) {
        this.poolSize = settings.getInt('tajin.async.scheduler.poolSize', poolSize)
        this.enabled = settings.getBoolean('tajin.async.scheduler.enabled', enabled)
    }

    @PostConstruct
    void init() {
        if (!enabled) return
        this.executorService = new ScheduledThreadPoolExecutor(
            poolSize,
            new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("${DefaultJobScheduler.simpleName}-thread-%d")
                .setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                void uncaughtException(Thread t, Throwable e) {
                    LOGGER.log(Level.SEVERE, "UncaughtException in ${Dispatcher.simpleName} thread '${t.name}': ${e.message}", e)
                }
            }).build()
        )
        repository?.listPendingJobs()?.findAll { it.currentRetry <= it.source.maxRetry }?.each {
            doSchedule(new PersistentJobRunner(it))
        }
    }

    @PreDestroy
    void shutdown() {
        while (scheduledJobs) {
            scheduledJobs.keySet().each { scheduledJobs.remove(it)?.cancel(false) }
        }
        executorService.shutdown()
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS)
        } catch (e) {
            LOGGER.log(Level.SEVERE, 'Unable to terminate after 30 seconds', e)
        }
    }

    @Override
    ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.ovea.tajin:type=${getClass().simpleName},name=${INSTANCES.get()}")
    }

    @Override
    void cancel(List<String> ids) {
        if (ids) {
            def entries = scheduledJobs.find { k, v -> k.id in ids }
            entries.each {
                it.value.cancel(false)
                scheduledJobs.remove(it.key)
            }
            repository.delete(entries.collect { it.key })
        }
    }

    @Override
    void schedule(ScheduledJob e) {
        if (!e.name) throw new IllegalArgumentException('Missing jobName')
        if (!e.startDate) throw new IllegalArgumentException('Missing time')
        TriggeredScheduledJob job = new TriggeredScheduledJob(
            source: e,
            nextTry: e.startDate
        )
        if (job.source.persisted) {
            repository.insert(job)
            doSchedule new PersistentJobRunner(job)
        } else {
            doSchedule new JobRunner(job)
        }
    }

    private void doSchedule(JobRunner jobRunner) {
        if (!executorService.shutdown && !executorService.terminated) {
            long diff = Math.max(0, jobRunner.job.nextTry.time - System.currentTimeMillis())
            LOGGER.fine("Scheduling: ${jobRunner.job} in ${diff / 1000}s")
            ScheduledFuture<?> future = executorService.schedule(jobRunner, diff, TimeUnit.MILLISECONDS)
            scheduledJobs.put(jobRunner.job, future)
        } else {
            throw new IllegalStateException('Job Scheduler is closing or closed and cannot accept new job. Job ' + jobRunner.job + ' will be executed at next startup.')
        }
    }

    private class JobRunner implements Runnable {
        final TriggeredScheduledJob job

        JobRunner(TriggeredScheduledJob job) {
            this.job = job
        }

        @Override
        final void run() {
            try {
                nRunning.incrementAndGet()
                listener.onJobTriggered(job)
                scheduledJobs.remove(job)
                job.completionDate = new Date()
                onComplete()
            } catch (err) {
                scheduledJobs.remove(job)
                nFailed.incrementAndGet()
                job.currentRetry++
                onFailure()
                if (retryable) {
                    job.nextTry = new Date(System.currentTimeMillis() + job.source.retryDelaySecs * 1000)
                    doSchedule this
                }
                LOGGER.log(Level.SEVERE, "Job ${job.source.name} with ID ${job.id} has been rescheduled at ${job.nextTry} due to a failure: ${err.message}. Job detail: ${job}", err)
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
        PersistentJobRunner(TriggeredScheduledJob job) {
            super(job)
        }

        @Override
        void onComplete() { repository.update(job) }

        @Override
        void onFailure() { repository.update(job) }

        @Override
        boolean isRetryable() { job.source.maxRetry == ScheduledJob.INFINITE_RETRY || job.currentRetry < job.source.maxRetry }
    }

    // stats

    private final AtomicLong nRunning = new AtomicLong()
    private final AtomicLong nRan = new AtomicLong()
    private final AtomicLong nFailed = new AtomicLong()

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
        scheduledJobs.collect { k, v -> "${k.id} ${k.source.name} in ${Math.max(0, k.source.startDate.time - now) / 1000}s" as String }
    }

}
