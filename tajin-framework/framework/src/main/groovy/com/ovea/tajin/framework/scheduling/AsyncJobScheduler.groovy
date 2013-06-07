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
import com.ovea.tajin.framework.util.PropertySettings
import org.eclipse.jetty.util.annotation.ManagedObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
@javax.inject.Singleton
@ManagedObject("Job Scheduler")
//TODO JMX support
class AsyncJobScheduler implements JobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncJobScheduler)
    private static final long MINS_5 = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)

    @Inject
    LoadingCache<String, JobExecutor> executors

    @Inject
    JobRepository repository

    @Inject
    PropertySettings settings

    JobScheduler.OnError onError = JobScheduler.OnError.LOG
    ScheduledExecutorService service
    int maxRetry = -1

    @PostConstruct
    void init() {
        maxRetry = settings.getInt('scheduling.maxRetry', -1)
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
        jobs.each { doSchedule(it) }
    }

    @PreDestroy
    void close() {
        service.shutdownNow()
        try {
            service.awaitTermination(10, TimeUnit.SECONDS)
        } catch (ignored) {
        }
    }

    @Override
    void schedule(String jobName, Map<String, ?> data) { schedule(jobName, new Date(), data) }

    @Override
    void schedule(String jobName, Date time, Map<String, ?> data) {
        if (!jobName) throw new IllegalArgumentException('Missing jobName')
        if (!time) throw new IllegalArgumentException('Missing time')
        // will fail if jobName not found
        executors.get(jobName)
        // create and process job
        Job job = new Job(
            name: jobName,
            start: time,
            data: data ?: [:]
        )
        // save the job then after save, schedule it
        save job, { doSchedule(job) }
    }

    private void doSchedule(Job job) {
        if (!service.shutdown && !service.terminated) {
            long diff = Math.max(0, System.currentTimeMillis() - job.start.time)
            LOGGER.trace("Scheduling: ${job} in ${diff}ms")
            service.schedule(new JobRunner(job), diff, TimeUnit.MILLISECONDS)
        } else {
            throw new IllegalStateException('Job Scheduler is closing or closed and cannot accept new job. Job ' + job + ' will be executed at next startup.')
        }
    }

    private save(Job job, Closure<?> then = Closure.IDENTITY) {
        //TODO ASYNC
        job.updatedDate = new Date()
        repository.save(job)
        then()
    }

    private class JobRunner implements Runnable {
        final Job job

        JobRunner(Job job) {
            this.job = job
        }

        @Override
        void run() {
            try {
                executors.get(job.name).execute(job.data)
                job.end = new Date()
            } catch (e) {
                job.start = new Date(System.currentTimeMillis() + MINS_5)
                job.retry++
                save job, { onError.onError(job, e) }
            }
        }
    }
}
