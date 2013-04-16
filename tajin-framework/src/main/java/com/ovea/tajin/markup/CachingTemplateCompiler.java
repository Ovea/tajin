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

import com.ovea.tajin.io.Resource;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class CachingTemplateCompiler implements TemplateCompiler {

    private final ConcurrentMap<Resource, Template> cache = new ConcurrentHashMap<Resource, Template>();
    private final TemplateCompiler templateCompiler;

    public CachingTemplateCompiler(TemplateCompiler templateCompiler) {
        this.templateCompiler = templateCompiler;
    }

    @Override
    public Template compile(Resource location) throws TemplateCompilerException {
        Template template = cache.get(location);
        if (template == null) {
            template = templateCompiler.compile(location);
            Template old = cache.putIfAbsent(location, template);
            if (old != null) {
                template = old;
            }
        }
        return template;
    }
}