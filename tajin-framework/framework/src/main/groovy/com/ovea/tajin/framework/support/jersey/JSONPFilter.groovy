package com.ovea.tajin.framework.support.jersey

import com.ovea.tajin.framework.util.Json
import com.sun.jersey.api.model.AbstractResourceMethod
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerRequestFilter
import com.sun.jersey.spi.container.ContainerResponse
import com.sun.jersey.spi.container.ContainerResponseFilter

import javax.ws.rs.HttpMethod
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-11
 */
class JSONPFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final METHODS = [HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.POST, HttpMethod.PUT]

    final AbstractResourceMethod am
    final JSONP annotation
    final String method
    final boolean consumesJSON
    final boolean producesJSON

    /*protected JSONPFilter(AbstractResourceMethod arm) {
        this.am = arm
        this.method = am.httpMethod.toUpperCase()
        this.consumesJSON = arm.supportedInputTypes.empty || arm.supportedInputTypes.find { it.toString().startsWith('application/json') }
        this.producesJSON = arm.supportedOutputTypes.empty || arm.supportedOutputTypes.find { it.toString().startsWith('application/json') }
        JSONP annotation = am.getAnnotation(JSONP)
        if (!annotation) {
            annotation = am.resource.getAnnotation(JSONP)
        }
        this.annotation = annotation
    }*/

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        if(!(annotation && consumesJSON && producesJSON)) return request
        String cb = request.queryParameters.getFirst(annotation.callbackParam())
        if (cb) {
            String requestedMethod = request.queryParameters.getFirst(annotation.methodParam())?.toUpperCase()
            if (requestedMethod in METHODS && requestedMethod != method) {
                request.method = requestedMethod
            }
            if (!am.supportedInputTypes.empty) {
                Map data = [:]
                request.queryParameters.each { k, v ->
                    if (!annotation.ignores().contains(k)) {
                        data.put(k, v.empty ? null : v[0])
                    }
                }
                request.setEntity(Map, Map, null, MediaType.APPLICATION_JSON_TYPE, null, data)
            }
        }
        return request
    }

    @Override
    ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        if(!(annotation && consumesJSON && producesJSON)) return response
        String cb = request.queryParameters.getFirst(annotation.callbackParam())
        if (cb) {
            response.response = Response
                .status(Response.Status.OK)
                .entity("${cb}(${Json.stringify(response.entity)});" as String)
                .type('application/javascript; charset=UTF-8')
                .build()
        }
        return response
    }

    @Override
    String toString() { "JSONP(${am.method.declaringClass.simpleName}#${am.method.name})" }
}
