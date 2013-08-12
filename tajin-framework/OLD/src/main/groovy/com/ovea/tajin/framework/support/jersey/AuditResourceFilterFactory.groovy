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

import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-05-08
 */
class AuditResourceFilterFactory implements ResourceFilterFactory {

    private static final Logger LOGGER = Logger.getLogger(AuditResourceFilterFactory.name);

    private class ResourceMethodFilter implements ResourceFilter, ContainerRequestFilter {

        private final AbstractMethod arm;

        public ResourceMethodFilter(AbstractMethod arm) {
            this.arm = arm;
        }

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            LOGGER.fine("${request.method} ${request.path} => ${arm.method.declaringClass.simpleName}.${arm.method.name}")
            return request;
        }

        @Override
        public ContainerRequestFilter getRequestFilter() {
            return this;
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return null;
        }
    }

    public List<ResourceFilter> create(AbstractMethod am) {
        return Collections.<ResourceFilter> singletonList(new ResourceMethodFilter(am));
    }
}
