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
package com.ovea.tajin.framework.template.web;

import com.ovea.tajin.framework.io.Resource;
import com.ovea.tajin.framework.template.Template;
import com.ovea.tajin.framework.template.TemplateCompiler;
import com.ovea.tajin.framework.template.TemplateResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class DynamicMarkupDataBuilder implements MarkupDataBuilder {

    private final TemplateCompiler compiler;
    private final TemplateResolver resolver;
    private final LocaleProvider localeProvider;
    private final ContextProvider contextProvider;
    private final MarkupOptions markupOptions;

    DynamicMarkupDataBuilder(TemplateCompiler compiler, ContextProvider contextProvider, LocaleProvider localeProvider, MarkupOptions markupOptions, TemplateResolver resolver) {
        this.compiler = compiler;
        this.contextProvider = contextProvider;
        this.localeProvider = localeProvider;
        this.markupOptions = markupOptions;
        this.resolver = resolver;
    }

    @Override
    public MarkupData build(HttpServletRequest request, HttpServletResponse response, String path) {
        try {
            Resource resource = resolver.resolve(path, localeProvider.get(request));
            Template template = compiler.compile(resource);
            String markup = template.merge(contextProvider.build(request, response));
            return new MarkupData(markup.getBytes(markupOptions.defaultCharset));
        } catch (RuntimeException e) {
            return new MarkupData(e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
