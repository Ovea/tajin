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
package com.ovea.tajin.markup.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class CachingFixture {
    private final MarkupOptions markupOptions;

    CachingFixture(MarkupOptions markupOptions) {
        this.markupOptions = markupOptions;
    }

    boolean isModified(HttpServletRequest request, HttpServletResponse response, MarkupData markupData) {
        if (!markupOptions.dynamic && !markupOptions.debug && markupOptions.clientCaching > 0) {
            // ETag validation
            String etag = request.getHeader("If-None-Match");
            if (markupData.tag.equals(etag)) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return false;
            }
            // If-Modified validation
            long clientLastModified = request.getDateHeader("If-Modified-Since");
            if (clientLastModified != -1 && clientLastModified >= markupData.when) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return false;
            }
            // Build cache headers...
            response.setHeader("ETag", markupData.tag);
            response.setDateHeader("Last-Modified", markupData.when);
            response.setHeader("Cache-Control", "public, max-age=" + markupOptions.clientCaching + ", must-revalidate");
        } else {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
        }
        return true;
    }

}
