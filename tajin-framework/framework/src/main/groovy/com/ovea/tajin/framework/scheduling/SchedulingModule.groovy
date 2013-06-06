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

import javax.inject.Named

import static com.ovea.tajin.framework.support.guice.ClassToTypeLiteralMatcherAdapter.adapt

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

        bindListener(adapt(Matchers.subclassesOf(JobExecutor)), new TypeListener() {
            @Override
            <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                Class<? extends JobExecutor> executorClass = type.rawType
                String name = executorClass.isAnnotationPresent(Named) ? executorClass.getAnnotation(Named).value() : executorClass.simpleName
                executors.put(name, encounter.getProvider(executorClass))
            }
        })

    }

}
