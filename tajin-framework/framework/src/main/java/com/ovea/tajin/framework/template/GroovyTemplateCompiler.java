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

import com.ovea.tajin.framework.core.Resource;
import groovy.text.SimpleTemplateEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-29
 */
public class GroovyTemplateCompiler implements TemplateCompiler {
    private final String defaultCharset;

    public GroovyTemplateCompiler() {
        this("UTF-8");
    }

    public GroovyTemplateCompiler(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    @Override
    public ResolvedTemplate compile(Resource resource) throws TemplateCompilerException {
        final groovy.text.Template tmpl;
        try {
            tmpl = new SimpleTemplateEngine(Thread.currentThread().getContextClassLoader()).createTemplate(resource.getText(defaultCharset));
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return new ResolvedTemplate(resource) {
            @Override
            public String merge(Object context) {
                try {
                    StringWriter sw = new StringWriter();
                    tmpl.make((Map) context).writeTo(sw);
                    return sw.toString();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };
    }
}
