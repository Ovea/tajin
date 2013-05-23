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

import org.apache.shiro.SecurityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.*
import javax.servlet.http.HttpServletRequest

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-05-23
 */
@javax.inject.Singleton
class RequestLog implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLog)
    boolean secured

    @Override
    void destroy() {

    }

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        secured = Boolean.parseBoolean(filterConfig.getInitParameter('secured') ?: 'false')
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        HttpServletRequest req = (HttpServletRequest) request
        long time = System.currentTimeMillis()
        try {
            chain.doFilter(request, response)
        } finally {
            time = System.currentTimeMillis() - time
            if (secured) {
                LOGGER.info("|${(time as String).padLeft(5)}|${req.method.padLeft(6)}|${req.requestURI}|auth=${SecurityUtils.subject.authenticated ? 1 : 0}|rmb=${SecurityUtils.subject.remembered ? 1 : 0}|${req.userPrincipal}")
            } else {
                LOGGER.info("|${(time as String).padLeft(5)}|${req.method.padLeft(6)}|${req.requestURI}")
            }
        }
    }
}
