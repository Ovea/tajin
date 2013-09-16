package com.ovea.tajin.framework.support.jersey

import com.sun.jersey.api.model.AbstractMethod
import com.sun.jersey.spi.container.*

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

    private static final Logger LOGGER = Logger.getLogger(ExceptionMapper.name);

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
                return [new ResponseFilter(true)]
            }
            return []
        }
    }

    static class ResponseFilter implements ResourceFilter, ContainerResponseFilter {

        final boolean deprecated

        ResponseFilter() { this(false) }

        ResponseFilter(boolean deprecated) { this.deprecated = deprecated }

        @Override
        ContainerRequestFilter getRequestFilter() { return null }

        @Override
        ContainerResponseFilter getResponseFilter() { return this }

        @Override
        ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            if (response.entity != null
                && !response.mediaType.toString().startsWith(MediaType.APPLICATION_JSON)
                || isWrapped(response.response)) {
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
            LOGGER.log(Level.SEVERE, "Internal Server Error (500): " + e.message, e)
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
                entity.error.message = e.class.name + ': ' + e.message ?: '<no description>'
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

}

/*

404 http://localhost:8081/api/profile/404
   - exception mapper
   - resp filter
405 http://localhost:8081/api/security/login
   - exception mapper
   - resp filter
403 http://localhost:8081/api/restaurants/10Gea1uJJNEQGV-l0dd-nw
   - exception mapper
   - filter factory
   - resp filter
400 http://localhost:8081/api/security/login?callback=toto&method=POST
   - filter factory
   - resp filter
401 http://localhost:8081/api/security/login?callback=toto&method=POST&email=toto@rr.com&password=password1
   - exception mapper
   - filter factory
   - resp filter
200 DEPRECATED http://localhost:8081/api/meta/deprecated
   - filter factory REQUIRED
   - resp filter
500 http://localhost:8081/api/meta/throw1
   - exception mapper REQUIRED
   - filter factory
   - resp filter
500 http://localhost:8081/api/meta/throw2
   - exception mapper REQUIRED
   - filter factory
   - resp filter

   http://localhost:8081/api/meta/throw3

204 no data

--
Responses & Errors
All responses will look roughly like this:

```
{
    "meta": {
        "status": 200
    },
    "error": {
        "type",
        "data": {...}
        "message": "a string"
    },
    "data": {...}
}
```

 - __data__: JSON response, if any
 - __meta__: Meta information about the request and response
   - __status__: HTTP sattus code. In JSONP, the HTTP status code will always be 200 and `status` will hold the real HTTP status code value
 - __error__: Error section if error:
   - __type__: error type (see below)
   - __data__: error details, optional. I.e., for Bad Requests, contains validation errors.
   - __message__: optional message describing the error. I.e. in case of Internal 500 error, this field could be set.

Here is a list of error `type` and its matching `status` code:

 - `401` __authc__: Authentication error.
 - `403` __authz__: Although authentication succeeded, the acting user is not allowed to see this information due to privacy restrictions.
 - `400` __request__: A required parameter was missing or a parameter was malformed. This is also used if the resource ID in the path is incorrect.
 - `404` __notfound__: The requested path does not exist.
 - `405` __method__: The method (GET, PUT, POST, DELETE) set for the request is not allowed for the requested path..
 - `200` __deprecated__: Something about this request is using deprecated functionality, or the response format may be about to change.
 - `500` __server__:     Server is currently experiencing issues. Check [status.guestful.com](http://status.guestful.com) for updates.
--

*/
