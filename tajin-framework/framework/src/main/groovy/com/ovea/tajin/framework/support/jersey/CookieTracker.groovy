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
package com.ovea.tajin.framework.support.jersey

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Cookie
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.ext.ReaderInterceptor
import javax.ws.rs.ext.ReaderInterceptorContext
import javax.ws.rs.ext.WriterInterceptor
import javax.ws.rs.ext.WriterInterceptorContext

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-30
 */
//TODO MATHIEU - JERSEY 2 REVIEW & TEST
class CookieTracker implements ReaderInterceptor, WriterInterceptor {
    final Map<String, Cookie> cookies = [:]

    @Override
    Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        cookies = getRequestCookies(context)
        return context.proceed()
    }

    @Override
    void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        cookies.each { k, v -> context.headers.add(HttpHeaders.COOKIE, v) }
        context.proceed()
    }

}
