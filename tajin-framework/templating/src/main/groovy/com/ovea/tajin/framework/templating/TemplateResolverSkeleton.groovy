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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.util.concurrent.UncheckedExecutionException
import com.ovea.tajin.framework.core.Resource
import com.ovea.tajin.framework.core.Settings

import javax.annotation.PostConstruct
import javax.inject.Inject
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
abstract class TemplateResolverSkeleton implements TemplateResolver {

    private static CompiledTemplate NOT_FOUND = new CompiledTemplate(null) {
        @Override
        String merge(Object context) {
            return null
        }
    }

    private LoadingCache<Tuple, CompiledTemplate> templates

    int maximumSize = -1
    long expirationSeconds = -1

    @Inject
    TemplateCompiler compiler

    @Inject
    void setSettings(Settings settings) {
        maximumSize = settings.getInt('tajin.templating.maximumSize', maximumSize)
        expirationSeconds = settings.getLong('tajin.templating.expirationSeconds', expirationSeconds)
    }

    @PostConstruct
    void init() {
        CacheBuilder builder = CacheBuilder.newBuilder()
        if (maximumSize >= 0) builder.maximumSize(maximumSize)
        if (expirationSeconds >= 0) builder.expireAfterWrite(expirationSeconds, TimeUnit.SECONDS)
        this.templates = builder.build(new CacheLoader<Tuple, CompiledTemplate>() {
            @Override
            CompiledTemplate load(Tuple keys) throws Exception {
                String path = keys.get(0)
                Locale locale = keys.get(1) as Locale
                StringBuilder templateName = new StringBuilder(path);
                if (templateName.charAt(0) == '/')
                    templateName.deleteCharAt(0);
                int pos = templateName.lastIndexOf(".");
                if (pos == -1) {
                    throw new TemplateResolverException("Illegal path: extension needed");
                }
                List<String> locales = ["_" + locale, "_" + locale.getLanguage(), ""]
                int prev = 2, i = 0
                while (i < locales.size()) {
                    templateName.replace(pos, pos + locales[prev].length(), locales[i]);
                    Resource tmpl = tryPath(templateName.toString());
                    if (tmpl != null) {
                        return TemplateResolverSkeleton.this.getCompiler().compile(tmpl);
                    }
                    i++
                    prev = (2 + i) % 3
                }
                return NOT_FOUND
            }
        })
    }

    @Override
    public final CompiledTemplate resolve(String path, Locale locale) throws TemplateResolverException {
        if (path == null) {
            throw new TemplateResolverException("Empty path");
        }
        CompiledTemplate template
        try {
            template = templates.get(new Tuple([path, locale] as Object[]))
        } catch (ExecutionException e) {
            throw e.cause
        } catch (UncheckedExecutionException e) {
            throw e.cause
        }
        if (template == NOT_FOUND) {
            throw new TemplateResolverException("Template not found: " + path);
        }
        return template
    }

    protected abstract Resource tryPath(String path);
}
