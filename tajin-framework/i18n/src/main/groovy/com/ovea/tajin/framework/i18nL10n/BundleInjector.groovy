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
package com.ovea.tajin.framework.i18nL10n

import java.lang.reflect.Field

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-08-12
 */
class BundleInjector {

    final I18NService i18NService

    BundleInjector(I18NService i18NService) {
        this.i18NService = i18NService
    }

    void inject(Object instance) {
        List<Field> fields = new LinkedList<>()
        Class c = instance.class
        for (c; c != null && c != Object; c = c.superclass) {
            fields.addAll(c.getDeclaredFields().findAll { it.isAnnotationPresent(Bundle) })
        }
        inject(instance, fields)
    }

    void inject(Object instance, List<Field> fields) {
        for (Field field : fields) {
            Bundle annotation = field.getAnnotation(Bundle.class);
            I18NBundlerProvider service = i18NService.getBundleProvider(annotation.value());
            if (!field.getType().isAssignableFrom(I18NBundlerProvider.class))
                throw new IllegalStateException("Field " + field + " must be of type " + I18NBundlerProvider.class.getName());
            if (!field.isAccessible())
                field.setAccessible(true);
            try {
                field.set(instance, service);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

}
