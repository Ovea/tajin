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

import org.apache.shiro.web.servlet.SimpleCookie

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class HttpCookie extends SimpleCookie implements Cookie {

    private String oldDomain;

    public HttpCookie(Cookie cookie) {
        super(cookie);
        setOldDomain(cookie.getOldDomain());
    }

    public HttpCookie(String name) {
        super(name);
    }

    public HttpCookie() {

    }

    private HttpCookie(Cookie cookie, String domain) {
        super(cookie);
        setDomain(domain);
        setOldDomain(domain);
    }

    @Override
    public void setOldDomain(String oldDomain) {
        this.oldDomain = oldDomain;
    }

    @Override
    public String getOldDomain() {
        return oldDomain;
    }

    @Override
    public void removeFrom(HttpServletRequest request, HttpServletResponse response) {
        super.removeFrom(request, response);
        if (domainChanged() && hasDuplicateCookie(request)) {
            new HttpCookie(this, oldDomain).removeFrom(request, response);
        }
    }

    @Override
    public String readValue(HttpServletRequest request) {
        return readValue(request, null);
    }

    @Override
    public Cookie withValue(String value) {
        HttpCookie cookie = new HttpCookie(this);
        cookie.setValue(value);
        return cookie;
    }

    @Override
    public String readValue(HttpServletRequest request, HttpServletResponse ignored) {
        return hasDuplicateCookie(request) ? null : super.readValue(request, ignored);
    }

    private boolean domainChanged() {
        if (getDomain() != null) return !getDomain().equals(oldDomain);
        return oldDomain != null && !oldDomain.equals(getDomain());
    }

    private boolean hasDuplicateCookie(HttpServletRequest request) {
        String name = getName();
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            int c = 0;
            for (int i = 0; i < cookies.length && c < 2; i++) {
                if (name.equals(cookies[i].getName())) {
                    c++;
                }
            }
            // if two same cookies are found
            return c == 2;
        }
        return false;
    }
}
