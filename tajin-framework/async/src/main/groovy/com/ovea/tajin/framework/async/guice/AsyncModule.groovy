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
import com.google.inject.*
import com.google.inject.matcher.Matchers
import com.google.inject.spi.InjectionListener
import com.google.inject.spi.TypeEncounter
import com.google.inject.spi.TypeListener
import com.ovea.tajin.framework.async.ConfiguredEventBus
import com.ovea.tajin.framework.async.Dispatcher
import com.ovea.tajin.framework.core.Settings

import javax.inject.Provider

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-05
 */
class AsyncModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(Settings)
        bind(Dispatcher).to(ConfiguredEventBus)
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
}
