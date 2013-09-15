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

import com.ovea.tajin.framework.util.Json
import com.sun.jersey.core.header.InBoundHeaders
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
class JSONP {

    private static final METHODS = [HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.OPTIONS, HttpMethod.POST, HttpMethod.PUT]

    static class RequestFilter implements ContainerRequestFilter {

        String callbackParam = 'callback'
        String methodParam = 'method'
        List<String> ignores = [callbackParam, methodParam, '_']

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            String cb = request.queryParameters.getFirst(callbackParam)
            if (cb) {
                String requestedMethod = request.queryParameters.getFirst(methodParam)?.toUpperCase()
                if (requestedMethod in METHODS && requestedMethod != request.method) {
                    request.method = requestedMethod
                }
                Map data = [:]
                request.queryParameters.each { k, v ->
                    if (!ignores.contains(k)) {
                        data.put(k, v.empty ? null : v[0])
                    }
                }
                request.setEntity(Map, Map, null, MediaType.APPLICATION_JSON_TYPE, null, data)
                InBoundHeaders headers = new InBoundHeaders()
                request.getRequestHeaders().each { k, v -> v.each { headers.add(k, it) } }
                headers.putSingle('Content-Type', MediaType.APPLICATION_JSON)
                request.headers = headers
            }
            return request
        }
    }

    static class ResponseFilter implements ContainerResponseFilter {

        String callbackParam = 'callback'
        String methodParam = 'method'
        List<String> ignores = [callbackParam, methodParam, '_']

        @Override
        ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            String cb = request.queryParameters.getFirst(callbackParam)
            if (cb) {
                response.response = Response
                    .status(Response.Status.OK)
                    .entity("${cb}(${Json.stringify(response.entity)});" as String)
                    .type('application/javascript; charset=UTF-8')
                    .build()
            }
            return response
        }
    }

}
