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
class ExtendedJsendFilterFactory implements ResourceFilterFactory {

    @Override
    List<ResourceFilter> create(AbstractMethod am) {
        return [new ExtendedJsendResponseFilter()]
    }

    static class ExtendedJsendResponseFilter implements ResourceFilter, ContainerResponseFilter {

        final AbstractResourceMethod am

        protected ExtendedJsendResponseFilterFactory(AbstractResourceMethod arm) {
            this.am = arm
        }

        @Override
        public ContainerRequestFilter getRequestFilter() {
            return null
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return this
        }

        @Override
        ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            println 'HELLO'
            return response
        }

        @Override
        String toString() { "${getClass().simpleName}(${am.method.declaringClass.simpleName}#${am.method.name})" }

    }
}
