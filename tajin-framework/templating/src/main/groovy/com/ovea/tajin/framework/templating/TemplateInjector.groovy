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
package com.ovea.tajin.framework.templating

import javax.inject.Inject
import java.lang.reflect.Field

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-08-12
 */
class TemplateInjector {

    final TemplateResolver resolver

    @Inject
    TemplateInjector(TemplateResolver resolver) {
        this.resolver = resolver
    }

    void inject(Object instance) {
        List<Field> fields = new LinkedList<>()
        Class c = instance.class
        for (c; c != null && c != Object; c = c.superclass) {
            fields.addAll(c.getDeclaredFields().findAll { it.isAnnotationPresent(Template) })
        }
        inject(instance, fields)
    }

    void inject(Object instance, List<Field> fields) {
        for (Field field : fields) {
            if (!field.getType().isAssignableFrom(TemplateMerger.class))
                throw new IllegalStateException("Field " + field + " must be of type " + TemplateMerger.class.getName());
            if (!field.isAccessible())
                field.setAccessible(true);
            Template annotation = field.getAnnotation(Template.class);
            try {
                field.set(instance, new TemplateMerger(resolver, annotation.value()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

}
