package com.ovea.tajin.framework.support.jersey

import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-11
 */
@Provider
@javax.inject.Singleton
class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    Response toResponse(Throwable e) {
        // WebApplicationException, no cause, status 403

        // WebApplicationException, cause = Exception, 400, message = Error creating JSON

        // WebApplicationException, cause = Exception, 500, message = Error writing JSON Type


    }
}
