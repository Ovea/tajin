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
package com.ovea.tajin.framework.support.shiro

import com.ovea.tajin.framework.util.PropertySettings
import org.apache.shiro.subject.Subject
import org.apache.shiro.web.filter.authc.AuthenticationFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@javax.inject.Singleton
class SecurityFilter extends AuthenticationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter)

    @Inject
    AccountRepository accountRepository

    @Inject
    PropertySettings settings

    @PostConstruct
    void init() {
        settings.getStrings('security.paths', []).collect { it.trim() }.each { String spec ->
            int bs = spec.indexOf('[')
            int be = spec.indexOf(']')
            String path = bs == -1 ? spec : spec.substring(0, bs)
            def required = []
            if (bs != -1 && be != -1) {
                required = spec.substring(bs + 1, be).split('\\+') as List
            }
            LOGGER.info("Requirements for path {} : {}", path, required)
            appliedPaths.put(path, required)
        }
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = getSubject(request, response)
        def allowed = mappedValue as List
        boolean allowAuth = 'auth' in allowed
        boolean allowRmb = 'rmb' in allowed
        if (allowAuth && allowRmb) {
            return subject.authenticated || subject.remembered
        }
        if (allowAuth && !allowRmb) {
            return subject.authenticated
        }
        return false
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse res = (HttpServletResponse) response
        res.status = HttpServletResponse.SC_FORBIDDEN
    }

}
