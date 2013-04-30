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
package com.ovea.tajin.framework.template;

import com.google.common.collect.Iterables;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.ovea.tajin.framework.util.Reflect;

import javax.inject.Provider;
import java.lang.reflect.Field;

public final class TmplHandler implements TypeListener {

    @Override
    public <I> void hear(final TypeLiteral<I> type, TypeEncounter<I> encounter) {
        final Iterable<Field> fields = Reflect.findFields(type.getRawType(), Reflect.annotatedBy(Template.class));
        if (!Iterables.isEmpty(fields)) {
            final Provider<TemplateResolver> resolver = encounter.getProvider(TemplateResolver.class);
            encounter.register(new MembersInjector<I>() {
                @Override
                public void injectMembers(I instance) {
                    for (Field field : fields) {
                        Template annotation = field.getAnnotation(Template.class);
                        I18NTemplate tmpl = new I18NTemplate(resolver.get(), annotation.value());
                        if (!field.getType().isAssignableFrom(I18NTemplate.class))
                            throw new IllegalStateException("Field " + field + " must be of type " + I18NTemplate.class.getName());
                        if (!field.isAccessible())
                            field.setAccessible(true);
                        try {
                            field.set(instance, tmpl);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                    }
                }
            });
        }
    }

}
