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
