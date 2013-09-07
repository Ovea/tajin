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

import com.google.common.eventbus.AsyncEventBus
import com.google.common.eventbus.EventBus
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.ovea.tajin.framework.core.Settings

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.inject.Inject
import java.util.concurrent.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-05
 */
@javax.inject.Singleton
class ConfiguredEventBus implements Dispatcher {

    private static final Logger LOGGER = Logger.getLogger(ConfiguredEventBus.name)

    private ExecutorService executorService
    private EventBus eventBus

    int minPoolSize = 0
    int maxPoolSize = 100

    @Inject
    void setSettings(Settings settings) {
        this.minPoolSize = settings.getInt('tajin.async.dispatcher.minPoolSize', minPoolSize)
        this.maxPoolSize = settings.getInt('tajin.async.dispatcher.maxPoolSize', maxPoolSize)
    }

    @PostConstruct
    void init() {
        executorService = new ThreadPoolExecutor(
            minPoolSize, maxPoolSize,
            1L, TimeUnit.MINUTES,
            new SynchronousQueue<Runnable>(),
            new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("${Dispatcher.simpleName}-thread-%d")
                .setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                void uncaughtException(Thread t, Throwable e) {
                    LOGGER.log(Level.SEVERE, "UncaughtException in ${Dispatcher.simpleName} thread '${t.name}': ${e.message}", e)
                }
            }).build(),
            new RejectedExecutionHandler() {
                @Override
                void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    // if the task cannot go within the pool, just run it in the current thread
                    r.run()
                }
            }
        )
        eventBus = new AsyncEventBus(executorService)
    }

    @PreDestroy
    void shutdown() {
        executorService.shutdown()
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS)
        } catch (e) {
            LOGGER.log(Level.SEVERE, 'Unable to terminate after 30 seconds', e)
        }
    }

    void register(Object o) {
        LOGGER.info('+subscriber ' + o.class.name)
        eventBus.register(o)
    }

    void unregister(Object o) {
        LOGGER.info('-subscriber ' + o.class.name)
        eventBus.unregister(o)
    }

    @Override
    void broadcast(Object event) { eventBus.post(event) }

}
