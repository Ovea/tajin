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
package com.ovea.tajin.framework.templating.guice

import com.google.inject.AbstractModule
import com.google.inject.MembersInjector
import com.google.inject.TypeLiteral
import com.google.inject.matcher.Matchers
import com.google.inject.spi.TypeEncounter
import com.google.inject.spi.TypeListener
import com.ovea.tajin.framework.templating.*

import javax.inject.Provider
import java.lang.reflect.Field

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-09
 */
class TemplatingModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TemplateCompiler).to(GroovyTemplateCompiler)
        bind(TemplateResolver).to(ResourceTemplateResolver)
        binder().bindListener(Matchers.any(), new TypeListener() {
            @Override
            def <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                List<Field> fields = new LinkedList<>()
                Class c = type.getRawType()
                for (c; c != null && c != Object; c = c.superclass) {
                    fields.addAll(c.getDeclaredFields().findAll { it.isAnnotationPresent(Template) })
                }
                if (fields) {
                    Provider<TemplateInjector> injector = encounter.getProvider(TemplateInjector)
                    encounter.register(new MembersInjector<Object>() {
                        @Override
                        public void injectMembers(Object instance) {
                            injector.get().inject(instance, fields)
                        }
                    });
                }
            }
        });
    }
}
