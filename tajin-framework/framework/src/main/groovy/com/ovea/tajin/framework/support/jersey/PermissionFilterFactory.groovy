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

import com.sun.jersey.api.core.HttpContext
import com.sun.jersey.api.model.AbstractMethod
import com.sun.jersey.api.model.AbstractResourceMethod
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerRequestFilter
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ResourceFilter
import com.sun.jersey.spi.container.ResourceFilterFactory
import org.apache.shiro.SecurityUtils

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.Response

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class PermissionFilterFactory implements ResourceFilterFactory {

    private final HttpContext context;

    public PermissionFilterFactory(@Context HttpContext hc) {
        this.context = hc;
    }

    private class Filter implements ResourceFilter, ContainerRequestFilter {

        private final List<String> permissions
        private final Collection<String> vars

        protected Filter(String[] permissions, Collection<String> vars) {
            this.permissions = (permissions != null) ? permissions as List : []
            this.vars = vars
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
            Map<String, String> ctx = vars.collectEntries { [(it): context.getUriInfo().getPathParameters().getFirst(it)] }
            permissions.each { String perm ->
                ctx.each { String k, String v -> perm = perm.replace('{' + k + '}', v) }
                if (!SecurityUtils.subject.isPermitted(perm)) {
                    throw new WebApplicationException(new IllegalStateException('Invalid permissions'), Response.Status.FORBIDDEN)
                }
            }
            return request
        }
    }

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        if (am.isAnnotationPresent(Permissions)) {
            Collection<String> permissions = am.getAnnotation(Permissions).value() as List
            Collection<String> vars = new TreeSet<>()
            for (String p : permissions) {
                int s = p.indexOf('{')
                while (s != -1) {
                    int e = p.indexOf('}', s + 1)
                    vars << p.substring(s + 1, e)
                    s = p.indexOf('{', e + 1)
                }
            }
            if (((AbstractResourceMethod) am).parameters.collect { it.sourceName }.containsAll(vars)) {
                return Collections.<ResourceFilter> singletonList(new Filter(am.getAnnotation(Permissions).value(), vars))
            }
            throw new IllegalArgumentException('Bad permissions: ' + permissions + ' for method ' + am)

        }
        return null
    }
}