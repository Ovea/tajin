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

import java.io.Writer;
import java.lang.reflect.Array;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class JSONType {

    JSONType() {
    }

    JSONType(String string) throws JSONException {
    }

    JSONType(JSONTokener tokener) {
    }

    @Override
    public abstract String toString();

    public final JSONType expr(String expression) {
        return JSONExpr.expr(this, expression);
    }

    public final JSONType expr(String expression, Filter... filters) {
        return JSONExpr.expr(this, expression, filters);
    }

    /**
     * Make a prettyprinted JSON text of this JSON type.
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of
     *                     indentation.
     * @return a printable, displayable, transmittable
     *         representation of the object, beginning
     *         with <code>[</code>&nbsp;<small>(left bracket)</small> and ending
     *         with <code>]</code>&nbsp;<small>(right bracket)</small>.
     * @throws JSONException
     */
    public final String toString(int indentFactor) throws JSONException {
        return toString(indentFactor, 0);
    }

    public abstract String toString(int indentFactor, int indent) throws JSONException;

    public abstract Writer write(Writer writer) throws JSONException;


    public boolean isArray() {
        return JSONArray.class.isInstance(this);
    }

    public boolean isNull() {
        return JSONNull.class.isInstance(this);
    }

    public boolean isObject() {
        return JSONObject.class.isInstance(this);
    }

    public boolean isValue() {
        return JSONValue.class.isInstance(this);
    }

    public final boolean isContainer() {
        return isArray() || isObject();
    }

    public JSONArray asArray() throws ConversionException {
        return this instanceof JSONArray ? (JSONArray) this : new JSONArray().put(this);
    }

    public JSONObject asObject() throws ConversionException {
        if (this instanceof JSONObject) {
            return (JSONObject) this;
        }
        throw new ConversionException("Not a JSON Object: " + this);
    }

    public Object asValue() throws ConversionException {
        if (this.isValue()) {
            return ((JSONValue) this).value();
        }
        throw new ConversionException("Not a JSON Value: " + this);
    }

    public boolean asBoolean() throws ConversionException {
        Object o = val();
        if (o.equals(Boolean.FALSE) ||
            (o instanceof String && ((String) o).equalsIgnoreCase("false"))) {
            return false;
        } else if (o.equals(Boolean.TRUE) ||
            (o instanceof String && ((String) o).equalsIgnoreCase("true"))) {
            return true;
        }
        return false;
    }

    public double asDouble() throws ConversionException {
        Object o = val();
        try {
            return o instanceof Number ? ((Number) o).doubleValue() : Double.valueOf((String) o);
        } catch (Exception e) {
            throw new ConversionException("Not a JSON number: " + o);
        }
    }

    public int asInt() throws ConversionException {
        Object o = val();
        return o instanceof Number ? ((Number) o).intValue() : (int) asDouble();
    }

    public long asLong() throws ConversionException {
        Object o = val();
        return o instanceof Number ? ((Number) o).longValue() : (long) asDouble();
    }

    public String asString() throws ConversionException {
        return isValue() ? ((JSONValue) this).toString(false) : toString();
    }

    private Object val() throws ConversionException {
        if (this.isValue()) {
            return ((JSONValue) this).value();
        }
        throw new ConversionException("Not a JSON Value: " + getClass().getName());
    }

    static JSONType valueOf(Object o) {
        if (o instanceof JSONType) {
            return (JSONType) o;
        }
        if (o == null) {
            return JSON.NULL;
        }
        if (o instanceof Iterable) {
            return new JSONArray((Iterable<?>) o);
        }
        if (o.getClass().isArray()) {
            JSONArray array = new JSONArray();
            int len = Array.getLength(o);
            for (int i = 0; i < len; i++) {
                array.put(Array.get(o, i));
            }
            return array;
        }
        return new JSONValue(o);
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();
}
