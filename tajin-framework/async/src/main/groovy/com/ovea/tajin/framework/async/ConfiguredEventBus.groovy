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

import javax.annotation.PreDestroy
import java.util.concurrent.*
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-05
 */
class ConfiguredEventBus implements Dispatcher {

    private static final Logger LOGGER = Logger.getLogger(ConfiguredEventBus.name)

    private final ExecutorService executorService

    final EventBus eventBus

    ConfiguredEventBus(Executor executor) {
        // executor managed elsewhere
        this.executorService = null
        this.eventBus = new AsyncEventBus(executor)
    }

    ConfiguredEventBus(ExecutorService executorService) {
        // executor managed elsewhere
        this.executorService = null
        this.eventBus = new AsyncEventBus(executorService)
    }

    ConfiguredEventBus(int minPoolSize, int maxPoolSize) {
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

    @Override
    void broadcast(Object event) { eventBus.post(event) }

    @PreDestroy
    void shutdown() {
        if (executorService) {
            executorService.shutdown()
            executorService.awaitTermination(1, TimeUnit.MINUTES)
        }
    }

}
