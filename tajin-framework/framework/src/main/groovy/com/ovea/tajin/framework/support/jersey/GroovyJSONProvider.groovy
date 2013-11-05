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
package com.ovea.tajin.framework.support.jersey;

import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import java.lang.annotation.Annotation
import java.lang.reflect.Type

@Produces("*/*")
@Consumes("*/*")
class GroovyJSONProvider extends AbstractMessageReaderWriterProvider<Object> {

    private static boolean isSupported(MediaType m) { m.type == 'application' && m.subtype.endsWith("json") }

    private static List<Class<?>> SUPPORTED_PARAM_TYPES = [Object, Map, List, Collection]
    private static List<Class<?>> SUPPORTED_RETURN_TYPES = [Map, List, JsonBuilder]

    @Override
    boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        isSupported(mediaType) && type in SUPPORTED_PARAM_TYPES
    }

    @Override
    final boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        isSupported(mediaType) && (SUPPORTED_RETURN_TYPES.find { it.isAssignableFrom(type) } || type.array)
    }

    @Override
    Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) {
        Object o
        String s = null
        try {
            s = readFromAsString(entityStream, mediaType)
            o = new JsonSlurper().parseText(s)
            if (type.isInstance(o)) return o
        } catch (ignored) {
            if (s == null) {
                throw new WebApplicationException(new Exception("Error reading JSON"), 500)
            } else {
                throw new WebApplicationException(new Exception("Error parsing JSON: ${s}"), 500)
            }
        }
        throw new WebApplicationException(new Exception("Error creating expected JSON type " + type.simpleName + ' from JSON type ' + o.class.simpleName), 500)
    }

    @Override
    public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(entityStream, getCharset(mediaType))
            if (t instanceof JsonBuilder) {
                ((JsonBuilder) t).writeTo(writer);
            } else if (t instanceof Map || t instanceof List || type.array) {
                new JsonBuilder(t).writeTo(writer);
            } else {
                // we hope the object is not complex
                new JsonBuilder(t).writeTo(writer);
            }
            writer.flush();
        } catch (EOFException ignored) {
            // do nothing: output stream closed by clients
        }
    }
}
