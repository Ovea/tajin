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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class StaticMarkupDataBuilder implements MarkupDataBuilder {

    private final ConcurrentMap<String, MarkupData> cache = new ConcurrentHashMap<>();
    private final MarkupDataBuilder markupDataBuilder;
    private final LocaleProvider localeProvider;

    StaticMarkupDataBuilder(MarkupDataBuilder markupDataBuilder, LocaleProvider localeProvider) {
        this.markupDataBuilder = markupDataBuilder;
        this.localeProvider = localeProvider;
    }

    @Override
    public MarkupData build(HttpServletRequest request, HttpServletResponse response, String path) {
        String key = path + localeProvider.get(request);
        MarkupData markupData = cache.get(key);
        if (markupData == null) {
            markupData = markupDataBuilder.build(request, response, path);
            MarkupData old = cache.putIfAbsent(key, markupData);
            if (old != null) {
                markupData = old;
            }
        }
        return markupData;
    }

}
