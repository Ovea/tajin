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
class AsyncJobScheduler implements JobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncJobScheduler)

    @Inject
    LoadingCache<String, JobExecutor> executors

    @Inject
    JobRepository repository

    @Inject
    PropertySettings settings

    ScheduledExecutorService service

    @PostConstruct
    void init() {
        service = new ScheduledThreadPoolExecutor(
            settings.getInt('scheduling.pool.size', Runtime.runtime.availableProcessors() * 2),
            new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("${AsyncJobScheduler.simpleName}-thread-%d")
                .setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                void uncaughtException(Thread t, Throwable e) {
                    LOGGER.error("Job execution error: ${e.message}", e)
                }
            }).build()
        )
        repository.listPendingJobs().each { doSchedule(it) }
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
    void schedule(String jobName, Date time, Map<String, ?> data) {
        if (!jobName) throw new IllegalArgumentException('Missing jobName')
        if (!time) throw new IllegalArgumentException('Missing time')
        Job job = new Job(
            name: jobName,
            start: time,
            data: data ?: [:]
        )
        repository.save(job)
        doSchedule(job)
    }

    private void doSchedule(Job job) {
        if (!service.shutdown) {
            service.schedule(new JobRunner(job), 0, TimeUnit.MILLISECONDS)
        }
    }

    private class JobRunner implements Runnable {
        final Job job

        JobRunner(Job job) {
            this.job = job
        }

        @Override
        void run() {
            executors.get(job.name).execute(job.data)
        }
    }
}
