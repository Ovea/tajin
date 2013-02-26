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

import java.util.Locale;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
abstract class TemplateResolverSkeleton implements TemplateResolver {
    @Override
    public final Resource resolve(String path, Locale locale) throws TemplateResolverException {
        if (path == null) {
            throw new TemplateResolverException("Empty path");
        }
        StringBuilder templateName = new StringBuilder(path);
        if (templateName.charAt(0) == '/')
            templateName.deleteCharAt(0);
        int pos = templateName.lastIndexOf(".");
        if (pos == -1) {
            throw new TemplateResolverException("Illegal path: extension needed");
        }
        String[] locales = new String[]{"_" + locale, "_" + locale.getLanguage(), ""};
        for (int i = 0, prev = 2; i < locales.length; i++, prev = (2 + i) % 3) {
            templateName.replace(pos, pos + locales[prev].length(), locales[i]);
            Resource tmpl = tryPath(templateName.toString());
            if (tmpl != null) {
                return tmpl;
            }
        }
        throw new TemplateResolverException("Cannot resolve template for path " + path);
    }

    protected abstract Resource tryPath(String path);
}
