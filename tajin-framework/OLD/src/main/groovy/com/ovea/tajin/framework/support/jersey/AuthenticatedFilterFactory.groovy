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
package com.ovea.tajin.framework.support.jersey

import com.ovea.tajin.framework.util.PropertySettings
import com.sun.jersey.api.model.AbstractMethod
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerRequestFilter
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ResourceFilter
import com.sun.jersey.spi.container.ResourceFilterFactory
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.pam.UnsupportedTokenException
import org.apache.shiro.codec.Base64

import javax.inject.Inject
import javax.inject.Provider
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class AuthenticatedFilterFactory implements ResourceFilterFactory {

    /**
     * HTTP Authorization header, equal to <code>Authorization</code>
     */
    static final String AUTHORIZATION_HEADER = "Authorization"

    /**
     * HTTP Authentication header, equal to <code>WWW-Authenticate</code>
     */
    static final String AUTHENTICATE_HEADER = "WWW-Authenticate"

    @Context
    HttpServletRequest httpServletRequest

    @Inject
    PropertySettings settings

    @Inject
    Provider<APIToken> apiToken

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        if (settings.getBoolean('security.enabled', false)) {
            Authenticated authenticated = am.getAnnotation(Authenticated)
            if (!authenticated) {
                authenticated = am.resource.getAnnotation(Authenticated)
            }
            return authenticated ? [new AuthenticatedFilter(authenticated)] : []
        }
        return []
    }

    class AuthenticatedFilter implements ResourceFilter, ContainerRequestFilter {

        final Authenticated authenticated

        AuthenticatedFilter(Authenticated authenticated) {
            this.authenticated = authenticated
        }

        @Override
        public ContainerRequestFilter getRequestFilter() { return this }

        @Override
        public ContainerResponseFilter getResponseFilter() { return null }

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            String authzHeader = request.getHeaderValue(AUTHORIZATION_HEADER)
            if (authzHeader != null && authzHeader.toUpperCase(Locale.ENGLISH).startsWith(HttpServletRequest.BASIC_AUTH)) {
                UsernamePasswordToken token = buildToken(authzHeader)
                if (token == null) {
                    throw buildException(request, new UnsupportedTokenException('Malformed Basic HTTP Token'))
                } else {
                    try {
                        SecurityUtils.subject.login(token)
                    } catch (AuthenticationException e) {
                        throw buildException(request, e)
                    }
                }
            }
            if(SecurityUtils.subject.remembered && !authenticated.allowRemembered()) {
                SecurityUtils.subject.logout()
                throw buildException(request)
            }
            if (SecurityUtils.subject.authenticated || SecurityUtils.subject.remembered) {
                APIToken token = apiToken.get()
                if (token && !SecurityUtils.subject.hasRole('api:account:' + token.account)) {
                    SecurityUtils.subject.logout()
                    throw buildException(request, new UnsupportedTokenException('Invalid Token'))
                }
            } else {
                throw buildException(request)
            }
            return request
        }

        private WebApplicationException buildException(ContainerRequest request, Throwable t = null) {
            Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED)
            if (authenticated.askAuthenticate()) {
                builder.header(AUTHENTICATE_HEADER, "${HttpServletRequest.BASIC_AUTH} realm=\"${request.baseUri}\"")
            }
            if (t) return new WebApplicationException(t, builder.build())
            return new WebApplicationException(builder.build())
        }

        private UsernamePasswordToken buildToken(String authzHeader) {
            String[] parts = authzHeader.split(' ')
            if (parts.length < 2) return null
            String decoded = Base64.decodeToString(parts[1])
            parts = decoded.split(':', 2)
            if (parts.length < 2) return null
            return new UsernamePasswordToken(parts[0], parts[1], false, httpServletRequest.remoteHost)
        }

    }

}
