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
package com.ovea.tajin.support.jersey

import com.ovea.tajin.json.JSONException
import com.ovea.tajin.json.JSONType
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider

import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import java.lang.annotation.Annotation
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Type

/**
 * @author japod
 */
public abstract class JSONTypeProvider extends AbstractMessageReaderWriterProvider<JSONType> {

    @Produces("application/json")
    @Consumes("application/json")
    public static final class App extends JSONTypeProvider {
        @Override
        protected boolean isSupported(MediaType m) {
            return true;
        }
    }

    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends JSONTypeProvider {
        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+json");
        }
    }

    @Override
    public final boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return JSONType.class.isAssignableFrom(type) && isSupported(mediaType);
    }

    @Override
    public final boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return JSONType.class.isAssignableFrom(type) && isSupported(mediaType);
    }

    protected abstract boolean isSupported(MediaType m);

    @Override
    public JSONType readFrom(
        Class<JSONType> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders,
        InputStream entityStream) throws IOException {
        try {
            return type.getConstructor(String.class).newInstance(readFromAsString(entityStream, mediaType));
        } catch (InvocationTargetException e) {
            throw new WebApplicationException(new Exception("Error parsing JSON Type: " + type.getName(), e.getTargetException()), 400);
        } catch (Exception e) {
            throw new WebApplicationException(new Exception("Error creating JSON Type: " + type.getName(), e), 400);
        }
    }

    @Override
    public void writeTo(
        JSONType t,
        Class<?> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders,
        OutputStream entityStream) throws IOException {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(entityStream, getCharset(mediaType));
            t.write(writer);
            if (t.isArray())
                writer.write("\n");
            writer.flush();
        } catch (JSONException je) {
            throw new WebApplicationException(new Exception("Error writing JSON Type: " + type.getName(), je), 500);
        }
    }
}
