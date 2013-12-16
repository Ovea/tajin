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
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerRequestFilter
import com.sun.jersey.spi.container.ContainerResponse
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ResourceFilter
import com.sun.jersey.spi.container.ResourceFilterFactory
import groovy.transform.Immutable

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.Family
import javax.ws.rs.ext.Provider
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-11
 */
class ExtendedJsend {

    private static final Logger LOGGER = Logger.getLogger('UncaughtException');
    private static final ThreadLocal<RequestInfo> requestInfo = new ThreadLocal<>()

    static final Map<Integer, String> ERROR_TYPES = [
        (400): 'request',
        (401): 'authc',
        (403): 'authz',
        (404): 'notfound',
        (405): 'method',
        (500): 'server'
    ]

    static class FilterFactory implements ResourceFilterFactory {
        @Override
        List<ResourceFilter> create(AbstractMethod am) {
            if (am.isAnnotationPresent(Deprecated) || am.resource.isAnnotationPresent(Deprecated)) {
                return [new Filter(true)]
            }
            return []
        }
    }

    static class Filter implements ResourceFilter, ContainerResponseFilter, ContainerRequestFilter {

        final boolean deprecated

        Filter() { this(false) }

        Filter(boolean deprecated) { this.deprecated = deprecated }

        @Override
        ContainerRequestFilter getRequestFilter() { return this }

        @Override
        ContainerResponseFilter getResponseFilter() { return this }

        @Override
        ContainerRequest filter(ContainerRequest request) {
            if (request.mediaType == null || request.mediaType.toString().startsWith(MediaType.APPLICATION_JSON)) {
                byte[] data = request.entityInputStream.bytes
                request.entityInputStream = new ByteArrayInputStream(data)
                requestInfo.set(new RequestInfo(
                    method: request.method,
                    uri: request.requestUri as String,
                    headers: request.getRequestHeaders().collectEntries { k, v -> [k, v.join(', ')] },
                    body: new String(data, 'UTF-8'),
                    user: request.userPrincipal?.name ?: 'public'
                ))
            }
            return request
        }

        @Override
        ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            if (response.entity != null
                && !response.mediaType.toString().startsWith(MediaType.APPLICATION_JSON)
                || isWrapped(response.response)) {
                requestInfo.remove()
                return response
            }
            response.response = wrapResponse(response.response, null, deprecated)
            return response
        }
    }

    @Provider
    @javax.inject.Singleton
    static class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Throwable> {
        @Override
        Response toResponse(Throwable e) {
            if (e instanceof WebApplicationException) {
                Response response = ((WebApplicationException) e).response
                String ct = response.metadata.getFirst(HttpHeaders.CONTENT_TYPE)
                return ct == null || ct.startsWith(MediaType.APPLICATION_JSON) ? wrapResponse(response, e.cause) : response
            }
            // uncaught exception
            RequestInfo infos = requestInfo.get()
            if (infos) {
                LOGGER.log(Level.SEVERE, e.message, new RuntimeException(e.message + """
${infos.method} ${infos.uri}
  - User: ${infos.user}
  - ${infos.headers.collect { k, v -> "${k}: ${v}" }.join('\n  - ')}
${infos.body ?: ''}""", e))
                requestInfo.remove()
            } else {
                LOGGER.log(Level.SEVERE, e.message, e)
            }
            return wrapResponse(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build(), e)
        }
    }

    static Family getFamily(int status) {
        switch (status.intdiv(100)) {
            case 1: return Family.INFORMATIONAL
            case 2: return Family.SUCCESSFUL
            case 3: return Family.REDIRECTION
            case 4: return Family.CLIENT_ERROR
            case 5: return Family.SERVER_ERROR
            default: return Family.OTHER
        }
    }

    static boolean isWrapped(Response response) { response.entity instanceof Map && response.entity.meta?.status != null }

    static Response wrapResponse(Response response, Throwable e = null, boolean deprecated = false) {
        Map entity = [
            meta: [
                status: response.status
            ]
        ]
        boolean error = getFamily(response.status) in [Family.CLIENT_ERROR, Family.SERVER_ERROR]
        Object data = response.entity
        if (error || e != null || deprecated) {
            entity.error = [
                type: ERROR_TYPES.get(response.status) ?: (deprecated ? 'deprecated' : 'other')
            ]
            if (e != null) {
                entity.error.message = e.class.simpleName + ': ' + e.message ?: '<no description>'
            }
            if (data != null && (error || e != null)) {
                entity.error.data = data
            }
        } else if (!error && e == null && (response.status == Response.Status.OK.statusCode || data != null)) {
            entity.data = data
        }
        return Response.fromResponse(response)
            .status(response.status == Response.Status.NO_CONTENT.statusCode ? Response.Status.OK.statusCode : response.status)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(entity)
            .build()
    }

    @Immutable
    static class RequestInfo {
        String method
        String uri
        Map headers
        String body
        String user
    }
}
