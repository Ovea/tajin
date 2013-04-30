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

import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerResponse
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ContainerResponseWriter

import javax.ws.rs.core.HttpHeaders
import java.lang.annotation.Annotation
import java.util.zip.GZIPOutputStream

public class GzipEncoder implements ContainerResponseFilter {

    private static final class Adapter implements ContainerResponseWriter {
        private final ContainerResponseWriter crw;

        private GZIPOutputStream gos;

        Adapter(ContainerResponseWriter crw) {
            this.crw = crw;
        }

        public OutputStream writeStatusAndHeaders(long contentLength, ContainerResponse response) throws IOException {
            gos = new GZIPOutputStream(crw.writeStatusAndHeaders(-1, response));
            return gos;
        }

        public void finish() throws IOException {
            gos.finish();
            crw.finish();
        }
    }

    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        if (response.getEntity() != null &&
                request.getRequestHeaders().containsKey(HttpHeaders.ACCEPT_ENCODING) &&
                !response.getHttpHeaders().containsKey(HttpHeaders.CONTENT_ENCODING)) {
            Annotation[] annotations = response.getAnnotations();
            if (annotations != null) {
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType() == NoGzip.class)
                        return response;
                }
            }
            if (request.getRequestHeaders().getFirst(HttpHeaders.ACCEPT_ENCODING).contains("gzip")) {
                response.getHttpHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");
                response.setContainerResponseWriter(
                        new Adapter(response.getContainerResponseWriter()));
            }
        }
        return response;
    }
}