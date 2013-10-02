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
import com.sun.jersey.api.model.AbstractResourceMethod
import com.sun.jersey.api.model.AbstractSubResourceMethod
import com.sun.jersey.api.uri.UriTemplateParser
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerRequestFilter
import com.sun.jersey.spi.container.ContainerResponse
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ResourceFilter
import com.sun.jersey.spi.container.ResourceFilterFactory
import org.apache.shiro.authz.UnauthorizedException

import javax.inject.Inject
import javax.inject.Provider
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class APITokenFilterFactory implements ResourceFilterFactory {

    private static final Logger LOGGER = Logger.getLogger(APITokenFilterFactory.name);

    @Inject
    PropertySettings settings

    @Inject
    Provider<APIToken> apiToken

    @Context
    HttpServletRequest rawRequest

    ConcurrentMap<String, AtomicLong> rateLimitingRemaining = new ConcurrentHashMap<>()
    ConcurrentMap<String, Date> rateLimitingResetDate = new ConcurrentHashMap<>()

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        if (settings.getBoolean('tajin.jersey.support.apitoken', true)) {
            return [new APIFilter((AbstractResourceMethod) am)]
        }
        return []
    }

    class APIFilter implements ResourceFilter, ContainerRequestFilter, ContainerResponseFilter {

        final AbstractResourceMethod arm
        final String path

        APIFilter(AbstractResourceMethod arm) {
            this.arm = arm
            String p = arm.resource.path.value
            if (!p.startsWith('/')) p = '/' + p
            if (p.endsWith('/')) p = p.substring(0, p.length() - 1)
            if (this.arm instanceof AbstractSubResourceMethod) {
                AbstractSubResourceMethod asrm = (AbstractSubResourceMethod) arm
                String s = asrm.path.value
                if (s.endsWith('/')) s = s.substring(0, s.length() - 1)
                if (s.startsWith('/')) p = p + s
                else p = p + '/' + s
            }
            this.path = "${arm.httpMethod} ${new UriTemplateParser(p).normalizedTemplate.replaceAll('\\{\\w+\\}', '?')}"
        }

        @Override
        public ContainerRequestFilter getRequestFilter() { return this }

        @Override
        public ContainerResponseFilter getResponseFilter() { return this }

        @Override
        ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            APIToken access = apiToken.get()
            if (access && access.rateLimited) {
                response.httpHeaders.add('X-RateLimit-Limit', access.rateLimitingLimit as String)
                response.httpHeaders.add('X-RateLimit-Remaining', rateLimitingRemaining.get(access.value).get() as String)
                response.httpHeaders.add('X-RateLimit-Reset', rateLimitingResetDate.get(access.value).time as String)
            }
            return response
        }

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            APIToken access = apiToken.get()
            if (!access) {
                throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity(
                    [
                        [
                            key: APIToken.TOKEN_PARAM,
                            type: 'invalid'
                        ]
                    ])
                    .type(MediaType.APPLICATION_JSON)
                    .build())
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("${path} - ${access}")
            }

            if (access.ipRestricted) {
                String ip = rawRequest.remoteAddr
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("${path} - ${access} | IP Restrictions: Request IP = ${ip}")
                }
                if (!(ip in access.ipRestrictions)) {
                    throw new WebApplicationException(new UnauthorizedException('IP refused'), Response.Status.FORBIDDEN)
                }
            }

            if (access.apiRestricted) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("${path} - ${access} | API Restrictions")
                }
                def found = access.apiRestrictions.find { r ->
                    if (r.startsWith('/')
                        && r.endsWith('/')
                        && path.matches(r.substring(1, r.length() - 1)) || r == path) {
                        return true
                    }
                    return false
                }
                if (!found) {
                    throw new WebApplicationException(new UnauthorizedException('API Resource access refused'), Response.Status.FORBIDDEN)
                }
            }

            if (access.isHeaderRestricted()) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    String hdump = request.requestHeaders.collect { k, v -> "${k}: ${v.join(', ')}" }.join('\n')
                    LOGGER.finest("${path} - ${access} | Header Restrictions = ${access.headerRestrictions}, Request Headers:\n${hdump}")
                }
                boolean foundInvalidValue = false
                def found = access.headerRestrictions.find { header, expectedValue ->
                    String v = request.getHeaderValue(header)
                    if (v == null) return false
                    if (expectedValue.startsWith('/')
                        && expectedValue.endsWith('/')
                        && v.matches(expectedValue.substring(1, expectedValue.length() - 1)) || v == expectedValue) {
                        return true
                    }
                    if (v != null) foundInvalidValue = true
                    return false
                }
                if (!found) {
                    throw new WebApplicationException(new UnauthorizedException(foundInvalidValue ? 'Header invalid' : 'Required Header missing'), Response.Status.FORBIDDEN)
                }
            }

            if (access.rateLimited) {
                //TODO MATHIEU: implement properly rate limiting by using external persistence storage for clustering
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("${path} - ${access} | Rate Limited")
                }
                Date now = new Date()
                Date previousResetDate = rateLimitingResetDate.get(access.value)
                Date resetDate = previousResetDate
                if (resetDate == null) {
                    resetDate = new Date(now.time - 1)
                }
                if (resetDate <= now) {
                    if (access.rateLimitingPeriod == APIToken.RATE_LIMITING_PERIOD_DAILY) {
                        resetDate = new Date(now.time + 86400000)
                    } else if (access.rateLimitingPeriod == APIToken.RATE_LIMITING_PERIOD_HOURLY) {
                        resetDate = new Date(now.time + 3600000)
                    } else if (access.rateLimitingPeriod == APIToken.RATE_LIMITING_PERIOD_MONTHLY) {
                        resetDate = new Date(now.time + 2629800000) // 1 month = 365.25/12*24*60*60*1000
                    }
                    boolean put = previousResetDate == null ? rateLimitingResetDate.putIfAbsent(access.value, resetDate) == null : rateLimitingResetDate.replace(access.value, previousResetDate, resetDate)
                    if (put) {
                        AtomicLong prev = rateLimitingRemaining.get(access.value)
                        if (prev == null) {
                            rateLimitingRemaining.putIfAbsent(access.value, new AtomicLong(access.rateLimitingLimit))
                        } else {
                            rateLimitingRemaining.replace(
                                access.value,
                                prev,
                                new AtomicLong(access.rateLimitingLimit))
                        }
                    }
                }
                long remaining = rateLimitingRemaining.get(access.value).decrementAndGet()
                if (remaining < 0) {
                    rateLimitingRemaining.get(access.value).compareAndSet(remaining, 0)
                    throw new WebApplicationException(new UnauthorizedException('Rate limit reached'), Response.Status.FORBIDDEN)
                }
            }

            return request
        }

    }

}
