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
package com.ovea.tajin.framework.async.guice

import com.google.common.eventbus.Subscribe
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import com.google.inject.matcher.Matchers
import com.google.inject.spi.InjectionListener
import com.google.inject.spi.TypeEncounter
import com.google.inject.spi.TypeListener
import com.ovea.tajin.framework.async.AsyncExecutor
import com.ovea.tajin.framework.async.ConfiguredEventBus
import com.ovea.tajin.framework.async.DefaultJobScheduler
import com.ovea.tajin.framework.async.Dispatcher
import com.ovea.tajin.framework.async.JobListener
import com.ovea.tajin.framework.async.JobRepository
import com.ovea.tajin.framework.async.JobScheduler
import com.ovea.tajin.framework.core.Settings

import javax.annotation.PreDestroy
import javax.inject.Inject
import javax.inject.Provider
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-05
 */
class AsyncModule extends AbstractModule {

    private static final Logger LOGGER = Logger.getLogger(Dispatcher.name)

    @Override
    protected void configure() {
        requireBinding(Settings)
        requireBinding(JobRepository)
        requireBinding(JobListener)

        bind(Dispatcher).to(ConfiguredEventBus)
        bind(DefaultJobScheduler)
        bind(JobScheduler).to(DefaultJobScheduler)
        bind(Executor).annotatedWith(AsyncExecutor).toProvider(ExecutorProvider)

        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                if (type.rawType.getMethods().find { it.isAnnotationPresent(Subscribe) }) {
                    Provider<Injector> i = encounter.getProvider(Injector)
                    Provider<ConfiguredEventBus> e = encounter.getProvider(ConfiguredEventBus)
                    Provider<Settings> s = encounter.getProvider(Settings)
                    encounter.register(new InjectionListener<Object>() {
                        @Override
                        void afterInjection(Object injectee) {
                            if (s.get().getBoolean('tajin.async.dispatcher.enabled', true) && !Scopes.isSingleton(i.get().getBinding(Key.get(type)))) {
                                throw new IllegalStateException("Cannot register object " + injectee.class + " containing @Subscribe methods to EventBus because it is not registered as a singleton")
                            }
                            e.get().register(injectee)
                        }
                    })
                }
            }
        })
    }

    @javax.inject.Singleton
    static class ExecutorProvider implements Provider<Executor> {

        ExecutorService executorService

        @Inject
        Settings settings

        @Override
        Executor get() {
            boolean enabled = settings.getBoolean('tajin.async.dispatcher.enabled', true)
            int min = settings.getInt('tajin.async.dispatcher.minPoolSize', 0)
            int max = settings.getInt('tajin.async.dispatcher.maxPoolSize', 10 * Runtime.runtime.availableProcessors())
            if (enabled && max - min > 0) {
                return executorService = new ThreadPoolExecutor(
                    min,
                    max,
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
            } else {
                return new Executor() {
                    @Override
                    void execute(Runnable command) {
                        command.run()
                    }
                }
            }
        }

        @PreDestroy
        void shutdown() {
            if (executorService) {
                executorService.shutdown()
                try {
                    executorService.awaitTermination(30, TimeUnit.SECONDS)
                } catch (Throwable e) {
                    LOGGER.log(Level.SEVERE, 'Unable to terminate after 30 seconds', e)
                }
            }
        }
    }

}
