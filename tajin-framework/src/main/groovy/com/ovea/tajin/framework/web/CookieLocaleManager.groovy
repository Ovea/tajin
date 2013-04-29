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
package com.ovea.tajin.framework.web

import com.google.inject.servlet.RequestScoped
import com.ovea.tajin.framework.prop.PropertySettings
import com.ovea.tajin.framework.util.LocaleUtil

import javax.inject.Inject
import javax.inject.Provider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@RequestScoped
final class CookieLocaleManager implements Provider<Locale> {

    private static final int DAY_SEC = 60 * 60 * 24
    private static final String DEFAULT_NAME = 'lc'

    @Inject
    Provider<HttpServletRequest> request;

    @Inject
    Provider<HttpServletResponse> response;

    @Inject
    PropertySettings settings

    public void set(Locale locale) {
        newLocaleCookie().withValue(locale.toString()).saveTo(request.get(), response.get());
    }

    @Override
    public Locale get() {
        Locale found = null
        // first try to get value from cookie
        String val = newLocaleCookie().readValue(request.get());
        if (val) {
            found = LocaleUtil.valueOf(val)
            if (!found) {
                // when using cors, check the request parameters if a cookie is not present
                val = request.get().getParameter(settings.getString('locale.cookie.name', DEFAULT_NAME));
                if (val) {
                    found = LocaleUtil.valueOf(val);
                    if (!found) {
                        // otherwise: no cookie exists client-side => first time, check request header and set cookie
                        Locale l = request.get().getHeader("Accept-Language") == null ? Locale.US : request.get().getLocale();
                    }
                }
            }
        }
        return found?:Locale.US;
    }

    Cookie newLocaleCookie() {
        return new HttpCookie(
            name: settings.getString('locale.cookie.name', DEFAULT_NAME),
            maxAge: settings.getInt('locale.cookie.days', 365) * DAY_SEC,
            httpOnly: false
        )
    }
}