package com.ovea.tajin.framework.support.jersey

import com.sun.jersey.api.model.AbstractMethod
import com.sun.jersey.api.model.AbstractResourceMethod
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerRequestFilter
import com.sun.jersey.spi.container.ContainerResponse
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ResourceFilter
import com.sun.jersey.spi.container.ResourceFilterFactory

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-11
 */
class ExtendedJsendResponseFilterFactory implements ResourceFilterFactory {

    @Override
    List<ResourceFilter> create(AbstractMethod am) {
        return [new ExtendedJsendResponseFilter()]
    }

    static class ExtendedJsendResponseFilter implements ResourceFilter, ContainerResponseFilter {

        final AbstractResourceMethod am
        final String method
        final boolean producesJSON

        protected ExtendedJsendResponseFilterFactory(AbstractResourceMethod arm) {
            this.am = arm
            this.method = am.httpMethod.toUpperCase()
            this.producesJSON = arm.supportedOutputTypes.empty || arm.supportedOutputTypes.find { it.toString().startsWith('application/json') }
        }

        @Override
        public ContainerRequestFilter getRequestFilter() {
            return null
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return null
        }

        @Override
        ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            return response
        }

        @Override
        String toString() { "Meta(${am.method.declaringClass.simpleName}#${am.method.name})" }

    }
}
