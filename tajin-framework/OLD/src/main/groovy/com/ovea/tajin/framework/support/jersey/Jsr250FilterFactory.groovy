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
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory
import com.sun.jersey.api.model.AbstractMethod
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerRequestFilter
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ResourceFilter
import org.apache.shiro.authz.UnauthorizedException

import javax.annotation.security.DenyAll
import javax.annotation.security.PermitAll
import javax.annotation.security.RolesAllowed
import javax.inject.Inject
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class Jsr250FilterFactory extends RolesAllowedResourceFilterFactory {

    @Context
    SecurityContext sc

    @Inject
    PropertySettings settings

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        if (settings.getBoolean('security.enabled', false)) {

            // DenyAll on the method take precedence over RolesAllowed and PermitAll
            if (am.isAnnotationPresent(DenyAll))
                return [new Jsr250Filter()]

            // RolesAllowed on the method takes precedence over PermitAll
            if (am.isAnnotationPresent(RolesAllowed))
                return [new Jsr250Filter(am.getAnnotation(RolesAllowed).value())]

            // PermitAll takes precedence over RolesAllowed on the class
            if (am.isAnnotationPresent(PermitAll))
                return []

            // DenyAll on the class takes precedence over other below
            if (am.getResource().isAnnotationPresent(DenyAll))
                return [new Jsr250Filter()]

            // RolesAllowed on the class takes precedence over PermitAll
            if (am.getResource().isAnnotationPresent(RolesAllowed))
                return [new Jsr250Filter(am.getResource().getAnnotation(RolesAllowed).value())]

            // Then check if we must permit
            if (am.getResource().isAnnotationPresent(PermitAll))
                return []

            // allow by default if no annotation is present
            return []

        }
        return []
    }

    class Jsr250Filter implements ResourceFilter, ContainerRequestFilter {

        private final boolean denyAll
        private final String[] rolesAllowed

        protected Jsr250Filter() {
            this.denyAll = true
            this.rolesAllowed = new String[0]
        }

        protected Jsr250Filter(String[] rolesAllowed) {
            this.denyAll = false
            this.rolesAllowed = (rolesAllowed != null) ? rolesAllowed : []
        }

        // ResourceFilter
        @Override
        public ContainerRequestFilter getRequestFilter() {
            return this
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return null
        }

        // ContainerRequestFilter
        @Override
        public ContainerRequest filter(ContainerRequest request) {
            // anonymous ? just pass
            if (request.userPrincipal == null) {
                return request
            }
            // authenticated ? then check roles
            if (!denyAll) {
                for (String role : rolesAllowed) {
                    if (sc.isUserInRole(role))
                        return request
                }
            }
            throw new WebApplicationException(new UnauthorizedException('Invalid role'), Response.Status.FORBIDDEN)
        }
    }

}
