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
import com.ovea.tajin.markup.mvel.TemplateMergingCallback;

public class Template {

    private final Resource resource;
    private final String defaultCharset;

    public Template(Resource resource) {
        this(resource, "UTF-8");
    }

    public Template(Resource resource, String defaultCharset) {
        this.resource = resource;
        this.defaultCharset = defaultCharset;
    }

    public final String merge(Object context) {
        return merge(context, TemplateMergingCallback.ADAPTER);
    }

    public String merge(Object context, TemplateMergingCallback callback) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getDefaultCharset() {
        return defaultCharset;
    }

    public final Resource getResource() {
        return resource;
    }

    public final String getTemplate() {
        return getTemplate(getDefaultCharset());
    }

    public final String getTemplate(String charset) {
        return resource.getText(charset);
    }

    @Override
    public final String toString() {
        return getTemplate();
    }
}
