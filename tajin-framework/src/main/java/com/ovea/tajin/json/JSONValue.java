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
package com.ovea.tajin.json;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class JSONValue extends JSONType {

    private static final Set<Class<?>> longs = new HashSet<Class<?>>() {{
        add(Long.class);
        add(Integer.class);
        add(Short.class);
        add(BigInteger.class);
        add(Byte.class);
    }};

    private static final Set<Class<?>> doubles = new HashSet<Class<?>>() {{
        add(Float.class);
        add(Double.class);
        add(BigDecimal.class);
    }};

    private final Object value;

    JSONValue(Object value) {
        Utils.testValidity(value);
        this.value = value;
    }

    public Object value() {
        return value;
    }

    @SuppressWarnings({"CloneDoesntCallSuperClone"})
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return this;
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (o1 == null || getClass() != o1.getClass()) return false;
        com.ovea.tajin.json.JSONValue that = (com.ovea.tajin.json.JSONValue) o1;
        if (value instanceof Number && that.value instanceof Number) {
            if (longs.contains(value.getClass()) && longs.contains(that.value.getClass())) {
                return ((Number) value).longValue() == ((Number) that.value).longValue();
            }
            if (doubles.contains(value.getClass()) && doubles.contains(that.value.getClass())) {
                return ((Number) value).doubleValue() == ((Number) that.value).doubleValue();
            }
        }
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value instanceof Number ? ((Number) value).intValue() : value.hashCode();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public String toString(int indentFactor, int indent) throws JSONException {
        return toString(true);
    }

    @Override
    public Writer write(Writer writer) throws JSONException {
        try {
            //noinspection NullableProblems
            writer.write(Utils.quote(value.toString()));
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    String toString(boolean quote) {
        if (value instanceof JSONString) {
            String s = ((JSONString) value).asString();
            if (s == null) {
                throw new JSONException("Bad value from asString: null");
            }
            return s;
        } else if (value instanceof Number) {
            return numberToString((Number) value);
        } else if (value instanceof Boolean) {
            return value.toString();
        } else {
            return quote ? Utils.quote(value.toString()) : value.toString();
        }
    }

    private static String numberToString(Number n) throws JSONException {
        if (n == null) {
            throw new JSONException("Null pointer");
        }
        // Shave off trailing zeros and decimal point, if possible.
        String s = n.toString();
        if (s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }
}
