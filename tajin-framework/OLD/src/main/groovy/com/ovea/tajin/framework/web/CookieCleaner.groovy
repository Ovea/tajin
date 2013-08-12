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

import com.google.common.collect.Multimap
import com.google.common.collect.TreeMultimap
import com.ovea.tajin.framework.util.PropertySettings
import org.apache.shiro.web.servlet.SimpleCookie

import javax.inject.Inject
import javax.servlet.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-07-31
 */
@javax.inject.Singleton
class CookieCleaner implements Filter {

    @Inject
    PropertySettings settings

    Multimap<String, String> cookies = TreeMultimap.create()

    @Override
    void destroy() {

    }

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        List<Map<String, String>> toDelete = settings.getList('cookies.delete')
        toDelete.each {
            cookies.put(it.name as String, it.domain ?: '')
        }
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request
        req?.cookies?.each { Cookie c ->
            cookies.get(c.name)?.each { String domain ->
                SimpleCookie delete = new SimpleCookie()
                delete.name = c.name
                if (domain) delete.domain = domain
                delete.removeFrom(req, (HttpServletResponse) response)
            }
        }
        chain.doFilter(request, response)
    }

}
