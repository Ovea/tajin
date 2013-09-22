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
import com.sun.jersey.api.model.AbstractSubResourceMethod
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerRequestFilter
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ResourceFilter
import com.sun.jersey.spi.container.ResourceFilterFactory

import javax.inject.Inject
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class APIFilterFactory implements ResourceFilterFactory {

    @Inject
    PropertySettings settings

    @Inject
    APIRepository repository

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        if (settings.getBoolean('tajin.jersey.support.apitoken', false)) {
            return [new APIFilter((AbstractSubResourceMethod) am)]
        }
        return []
    }

    class APIFilter implements ResourceFilter, ContainerRequestFilter {

        final AbstractSubResourceMethod asrm

        APIFilter(AbstractSubResourceMethod asrm) {
            this.asrm = asrm
        }

        @Override
        public ContainerRequestFilter getRequestFilter() { return this }

        @Override
        public ContainerResponseFilter getResponseFilter() { return null }

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            APIAccess access = repository.getAPIAccessByToken(request.queryParameters.getFirst('token'))
            if (access == null) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity([token: 'missing']).type(MediaType.APPLICATION_JSON).build())
            }
            //TODO MATHIEU: validate token
            return request
        }

    }

}
