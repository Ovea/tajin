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

import com.ovea.tajin.framework.prop.PropertySettings
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.SimpleAccount
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
        settings.getStrings('security.filter', '/secured/*').each { String path ->
            LOGGER.info('Protecting path {}', path)
            appliedPaths.put(path, [])
        }
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = getSubject(request, response)
        if (subject.authenticated) {
            return true
        }
        if (subject.remembered && AccountRepository.ANONYMOUS_PRINCIPAL != subject.principal) {
            SimpleAccount account = accountRepository.getAccount(subject.principal as String)
            if (account == null || account.locked) {
                // if the remembered user does not exist
                SecurityUtils.subject.logout()
                LOGGER.warn('Remembered user {} cannot be found !', subject.principal)
                return false
            }
            return true
        }
        return false
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse res = (HttpServletResponse) response
        res.status = HttpServletResponse.SC_FORBIDDEN
    }

}
