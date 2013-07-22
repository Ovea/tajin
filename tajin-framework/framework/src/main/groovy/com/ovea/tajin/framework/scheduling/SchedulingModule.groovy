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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.inject.AbstractModule
import com.google.inject.Provider
import com.google.inject.TypeLiteral
import com.google.inject.matcher.Matchers
import com.google.inject.spi.TypeEncounter
import com.google.inject.spi.TypeListener
import com.mycila.guice.ext.injection.ClassToTypeLiteralMatcherAdapter

import javax.inject.Named


/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
class SchedulingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JobScheduler).to(AsyncJobScheduler)

        Map<String, Provider<? extends JobExecutor>> executors = new HashMap<>()

        bind(new TypeLiteral<LoadingCache<String, JobExecutor>>() {}).toProvider(new Provider<LoadingCache<String, JobExecutor>>() {
            @Override
            LoadingCache<String, JobExecutor> get() {
                return CacheBuilder.newBuilder().build(new CacheLoader<String, JobExecutor>() {
                    @Override
                    JobExecutor load(String name) throws Exception {
                        Provider<? extends JobExecutor> p = executors.get(name)
                        if (!p) throw new IllegalArgumentException("Job Executor '${name}' not found.")
                        return p.get()
                    }
                })
            }
        }).in(javax.inject.Singleton)

        bindListener(ClassToTypeLiteralMatcherAdapter.adapt(Matchers.subclassesOf(JobExecutor)), new TypeListener() {
            @Override
            <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                Class<? extends JobExecutor> executorClass = type.rawType
                String name = executorClass.isAnnotationPresent(Named) ? executorClass.getAnnotation(Named).value() : executorClass.simpleName
                executors.put(name, encounter.getProvider(executorClass))
            }
        })

    }

}
