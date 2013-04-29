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

import com.ovea.tajin.framework.support.guice.HttpContext
import org.apache.shiro.web.filter.mgt.FilterChainResolver
import org.apache.shiro.web.mgt.WebSecurityManager
import org.apache.shiro.web.servlet.AbstractShiroFilter

import javax.inject.Inject
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-28
 */
@javax.inject.Singleton
class GuiceShiroFilter extends AbstractShiroFilter {

    @Inject
    WebSecurityManager securityManager;

    @Override
    void init() throws Exception {
        super.init();
        setFilterChainResolver(new FilterChainResolver() {
            @Override
            public FilterChain getChain(ServletRequest request, ServletResponse response, FilterChain originalChain) {
                return originalChain;
            }
        });
    }

    @Override
    WebSecurityManager createDefaultSecurityManager() {
        return securityManager;
    }

    @Override
    void executeChain(final ServletRequest request, final ServletResponse response, final FilterChain origChain) throws IOException, ServletException {
        try {
            HttpContext.start((HttpServletRequest) request, (HttpServletResponse) response);
            super.executeChain(request, response, origChain);
        } finally {
            HttpContext.end();
        }
    }

}
