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
        newLocaleCookie().withValue(locale.toString().replace('_', '-')).saveTo(request.get(), response.get());
    }

    @Override
    public Locale get() {
        // first try to get value from cookie
        Locale found = LocaleUtil.valueOf(newLocaleCookie().readValue(request.get()))
        if (!found) {
            // when using cors, check the request parameters if a cookie is not present
            found = LocaleUtil.valueOf(request.get().getParameter(settings.getString('locale.cookie.name', DEFAULT_NAME)));
            if (!found && request.get().getHeader("Accept-Language")) {
                // otherwise: no cookie exists client-side => first time, check request header and set cookie
                // must check if Accept-Language header is there otherwise the default system locale is returned
                found = request.get().getLocale();
            }
            if (!found) {
                found = LocaleUtil.valueOf(settings.getString('locale.default', 'en_US'), Locale.US)
            }
            // set locale cookie at the end
            set(found)
        }
        return found
    }

    Cookie newLocaleCookie() {
        return new HttpCookie(
            name: settings.getString('locale.cookie.name', DEFAULT_NAME),
            maxAge: settings.getInt('locale.cookie.days', 365) * DAY_SEC,
            httpOnly: false
        )
    }
}
