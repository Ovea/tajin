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

import com.sun.jersey.api.model.AbstractMethod
import com.sun.jersey.spi.container.*
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.pam.UnsupportedTokenException
import org.apache.shiro.codec.Base64

import javax.servlet.http.HttpServletRequest
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class AuthenticatedFilterFactory implements ResourceFilterFactory {

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

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        Authenticated authenticated = am.getAnnotation(Authenticated)
        if (!authenticated) {
            authenticated = am.resource.getAnnotation(Authenticated)
        }
        return authenticated ? [new AuthenticatedFilter(authenticated)] : []
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
            Throwable t = null
            if (authzHeader != null && authzHeader.toUpperCase(Locale.ENGLISH).startsWith(HttpServletRequest.BASIC_AUTH)) {
                UsernamePasswordToken token = buildToken(authzHeader)
                if (token == null) {
                    t = new UnsupportedTokenException('Malformed Basic HTTP Token')
                } else {
                    try {
                        SecurityUtils.subject.login(token)
                    } catch (AuthenticationException e) {
                        t = e
                    }
                }
            }
            if (t || !(SecurityUtils.subject.authenticated || SecurityUtils.subject.remembered && authenticated.allowRemembered())) {
                Response r = Response.status(Response.Status.UNAUTHORIZED).header(AUTHENTICATE_HEADER, "${HttpServletRequest.BASIC_AUTH} realm=\"${request.baseUri}\"").build()
                if (t) throw new WebApplicationException(t, r)
                else throw new WebApplicationException(r)
            }
            return request
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
