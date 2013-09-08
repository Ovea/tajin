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
import com.mycila.jmx.annotation.JmxBean
import com.mycila.jmx.annotation.JmxMethod
import com.mycila.jmx.annotation.JmxProperty
import com.ovea.tajin.framework.core.Settings

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
@JmxBean('com.ovea.tajin:type=JobScheduler,name=main')
@javax.inject.Singleton
class DefaultJobScheduler implements JobScheduler {

    private static final Logger LOGGER = Logger.getLogger(JobScheduler.simpleName)

    private final ConcurrentMap<String, Bucket> scheduledJobs = new ConcurrentHashMap<>()

    private ScheduledExecutorService executorService

    @Inject JobRepository repository = new EmptyJobRepository()
    @Inject JobListener listener = new EmptyJobListener()
    @Inject Settings settings
    @Inject
    @AsyncExecutor Executor fallbackExecutor

    @PostConstruct
    void init() {
        boolean enabled = settings.getBoolean('tajin.async.scheduler.enabled', true)
        if (enabled) {
            int poolSize = settings.getInt('tajin.async.scheduler.poolSize', 2 * Runtime.runtime.availableProcessors())
            if (poolSize <= 0) throw new IllegalArgumentException("Invalid pool size: " + poolSize + ". 'tajin.async.scheduler.poolSize' must be greater than 0.")
            this.executorService = new ScheduledThreadPoolExecutor(
                poolSize,
                new ThreadFactoryBuilder()
                    .setDaemon(false)
                    .setNameFormat("${JobScheduler.simpleName}-thread-%d")
                    .setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    void uncaughtException(Thread t, Throwable e) {
                        LOGGER.log(Level.SEVERE, "UncaughtException in ${Dispatcher.simpleName} thread '${t.name}': ${e.message}", e)
                    }
                }).build()
            ).with {
                it.removeOnCancelPolicy = true
                return it
            }
            List<TriggeredScheduledJob> deletions = []
            repository?.listPendingJobs()?.each {
                if (it.retryable) {
                    doSchedule(new PersistentJobRunner(it))
                } else {
                    deletions << it
                }
            }
            if (deletions) {
                repository.delete(deletions)
            }
        }
    }

    @PreDestroy
    void shutdown() {
        while (scheduledJobs) {
            cancel(scheduledJobs.keySet())
        }
        executorService.shutdown()
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS)
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, 'Unable to terminate after 30 seconds', e)
        }
    }

    @Override
    void cancel(Collection<String> ids) {
        if (ids) {
            List<TriggeredScheduledJob> jobs = []
            ids.each { String id ->
                Bucket b = scheduledJobs.remove(id)
                if (b) {
                    jobs << b.job
                    b.future.cancel(false)
                }
            }
            repository.delete(jobs)
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
        long diff = Math.max(0, jobRunner.job.nextTry.time - System.currentTimeMillis())
        LOGGER.fine("Scheduling: ${jobRunner.job} in ${diff / 1000}s")
        if (diff == 0 && fallbackExecutor) {
            FutureTask<?> future = new FutureTask(jobRunner, null) {
                @Override
                protected void done() {
                    try {
                        get()
                    } catch (ExecutionException e) {
                        throw e.cause
                    }
                }
            }
            scheduledJobs.put(jobRunner.job.id, new Bucket(
                job: jobRunner.job,
                future: future
            ))
            fallbackExecutor.execute(future)
        } else {
            Future<?> future = executorService.schedule(jobRunner, diff, TimeUnit.MILLISECONDS)
            if (!future.done) {
                scheduledJobs.put(jobRunner.job.id, new Bucket(
                    job: jobRunner.job,
                    future: future
                ))
            }
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
                scheduledJobs.remove(job.id)
                job.completionDate = new Date()
                onComplete()
            } catch (Throwable err) {
                scheduledJobs.remove(job.id)
                nFailed.incrementAndGet()
                job.currentRetry++
                job.lastTry = new Date()
                job.nextTry = new Date(System.currentTimeMillis() + job.source.retryDelaySecs * 1000)
                onFailure()
                listener.onJobFailure(job, err)
                if (job.retryable) {
                    doSchedule this
                } else {
                    onAbandon()
                }
                throw err
            } finally {
                nRunning.decrementAndGet()
                nRan.incrementAndGet()
            }
        }

        void onComplete() {}

        void onFailure() {}

        void onAbandon() {}

    }

    private class PersistentJobRunner extends JobRunner {
        PersistentJobRunner(TriggeredScheduledJob job) {
            super(job)
        }

        @Override
        void onComplete() { repository.update(job) }

        @Override
        void onFailure() {
            try {
                repository.update(job)
            } catch (Throwable err) {
                LOGGER.log(Level.SEVERE, err.message, err)
                throw err
            }
        }

        @Override
        void onAbandon() {
            try {
                repository.delete([job])
            } catch (Throwable err) {
                LOGGER.log(Level.SEVERE, err.message, err)
                throw err
            }
        }

    }

    static final class Bucket {
        TriggeredScheduledJob job
        Future<?> future
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
    Collection<String> getScheduledJobInfos() {
        long now = System.currentTimeMillis()
        return scheduledJobs.collect { k, v -> "${k} ${v.job.source.name} at ${v.job.nextTry}" as String }
    }

    @JmxProperty
    Collection<String> getScheduledJobIds() { scheduledJobs.collect { k, v -> k } }

    @JmxMethod
    void cancel(String jobId) { cancel([jobId]) }

}
