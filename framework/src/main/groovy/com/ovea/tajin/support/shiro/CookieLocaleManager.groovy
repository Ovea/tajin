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
package com.ovea.tajin.support.shiro

import com.ovea.tajin.util.LocaleUtil

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class CookieLocaleManager implements Provider<Locale> {

    @Inject
    Provider<HttpServletRequest> request;

    @Inject
    Provider<HttpServletResponse> response;

    @Inject
    @Named("locale")
    Cookie cookie;

    public void set(Locale locale) {
        cookie.withValue(locale.toString()).saveTo(request.get(), response.get());
    }

    @Override
    public Locale get() {
        // first try to get value from cookie
        String val = cookie.readValue(request.get());
        if (val != null) {
            return LocaleUtil.valueOf(val);
        }
        // when using cors, check the request parameters if a cookie is not present
        val = request.get().getParameter("locale");
        if (val != null) {
            return LocaleUtil.valueOf(val);
        }
        // otherwise: no cookie exists client-side => first time, check request header and set cookie
        Locale l = request.get().getHeader("Accept-Language") == null ? Locale.US : request.get().getLocale();
        set(l);
        return l;
    }
}
