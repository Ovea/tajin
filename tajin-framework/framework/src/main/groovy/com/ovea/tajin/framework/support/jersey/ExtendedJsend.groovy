package com.ovea.tajin.framework.support.jersey

import com.sun.jersey.api.model.AbstractMethod
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerRequestFilter
import com.sun.jersey.spi.container.ContainerResponse
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ResourceFilter
import com.sun.jersey.spi.container.ResourceFilterFactory

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.Provider

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-11
 */
class ExtendedJsend {

    static class FilterFactory implements ResourceFilterFactory {
        @Override
        List<ResourceFilter> create(AbstractMethod am) {
            return [new ResponseFilter(am)]
        }
    }

    static class ResponseFilter implements ResourceFilter, ContainerResponseFilter {

        final AbstractMethod am

        ResponseFilter() {
            this.am = null
        }

        ResponseFilter(AbstractMethod am) {
            this.am = am
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

    }

    @Provider
    @javax.inject.Singleton
    static class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Throwable> {
        @Override
        Response toResponse(Throwable e) {
            // WebApplicationException, no cause, status 403

            // WebApplicationException, cause = Exception, 400, message = Error creating JSON

            // WebApplicationException, cause = Exception, 500, message = Error writing JSON Type

            if (e instanceof WebApplicationException) {
                return ((WebApplicationException)e).response
            }

            return Response.status(500).build()
        }
    }

}

// 404 http://localhost:8081/api/profile/404
//   - resp filter
// 405 http://localhost:8081/api/security/login
//   - resp filter
// 403 http://localhost:8081/api/restaurants/10Gea1uJJNEQGV-l0dd-nw
//   - resp filter
//   - res filter
// 400 http://localhost:8081/api/security/login?callback=toto&method=POST
//   - resp filter
//   - res filter
// 401 http://localhost:8081/api/security/login?callback=toto&method=POST&email=toto@rr.com&password=password1
//   - resp filter
// 200 DEPRECATED http://localhost:8081/api/meta/deprecated
//   - res filter REQUIRED
//   - resp filter
// 500 http://localhost:8081/api/meta/throw1
//   - exception mapper REQUIRED
//   - resp filter
// 500 http://localhost:8081/api/meta/throw2
//   - exception mapper REQUIRED
//   - resp filter
// 204 no data
