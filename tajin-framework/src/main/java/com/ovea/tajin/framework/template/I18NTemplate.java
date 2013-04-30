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

import java.util.Locale;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-30
 */
public class I18NTemplate {

    private final TemplateResolver resolver;
    private final String path;

    public I18NTemplate(TemplateResolver resolver, String path) {
        this.resolver = resolver;
        this.path = path;
    }

    public String merge(Locale locale, Object context) {
        return resolver.resolve(path, locale).merge(context);
    }
}
