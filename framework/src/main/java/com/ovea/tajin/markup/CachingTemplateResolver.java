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
package com.ovea.tajin.markup;

import com.ovea.tajin.markup.util.Resource;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class CachingTemplateResolver implements TemplateResolver {

    private static final Object NULL = new Object();

    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<String, Object>();
    private final TemplateResolver resolver;

    public CachingTemplateResolver(TemplateResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Resource resolve(String path, Locale locale) throws TemplateResolverException {
        final String key = path + locale;
        Object obj = cache.get(key);
        if (obj == null) {
            try {
                obj = resolver.resolve(path, locale);
            } catch (TemplateResolverException e) {
                obj = NULL;
            }
            Object old = cache.putIfAbsent(key, obj);
            if (old != null) {
                obj = old;
            }
        }
        if (obj == NULL) {
            throw new TemplateResolverException("Cannot resolve template for path " + path);
        }
        return (Resource) obj;
    }
}
