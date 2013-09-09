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

import com.ovea.tajin.framework.core.Resource
import com.ovea.tajin.framework.core.Settings
import groovy.text.SimpleTemplateEngine

import javax.inject.Inject

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-29
 */
@javax.inject.Singleton
public class GroovyTemplateCompiler implements TemplateCompiler {

    String charset = 'UTF-8';
    ClassLoader classLoader = Thread.currentThread().contextClassLoader

    @Inject
    void setSettings(Settings settings) {
        charset = settings.getString('tajin.templating.charset', charset)
    }

    @Override
    final CompiledTemplate compile(Resource resource) throws TemplateCompilerException {
        def compiler = getCompiler(resource)
        return new CompiledTemplate(resource) {
            @Override
            public String merge(Object context) {
                try {
                    StringWriter sw = new StringWriter();
                    compiler.make((Map) context).writeTo(sw);
                    return sw.toString();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };
    }

    groovy.text.Template getCompiler(Resource r) {
        try {
            return new SimpleTemplateEngine(classLoader).createTemplate(r.getText(charset));
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
