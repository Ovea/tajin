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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class JSONObject extends JSONType implements Iterable<String> {

    private final LinkedHashMap<String, JSONType> myHashMap = new LinkedHashMap<>();

    public JSONObject() {
    }

    /**
     * Construct a JSONObject from a subset of another JSONObject.
     * An array of strings is used to identify the keys that should be copied.
     * Missing keys are ignored.
     *
     * @param jo A JSONObject.
     * @param sa An array of strings.
     * @throws com.ovea.tajin.json.JSONException If a value is a non-finite number.
     */
    public JSONObject(JSONObject jo, String... sa) throws JSONException {
        for (String aSa : sa) {
            putOpt(aSa, jo.opt(aSa));
        }
    }

    public JSONObject(JSONObject jo) throws JSONException {
        for (String aSa : jo) {
            putOpt(aSa, jo.opt(aSa));
        }
    }

    public JSONObject(JSONObject jo, Iterable<String> sa) throws JSONException {
        for (String aSa : sa) {
            putOpt(aSa, jo.opt(aSa));
        }
    }

    public JSONObject(JSONObject jo, JSONArray sa) throws JSONException {
        for (JSONType aSa : sa) {
            putOpt(aSa.asString(), jo.opt(aSa.asString()));
        }
    }

    /**
     * Construct a JSONObject from a Map.
     *
     * @param map A map object that can be used to initialize the contents of
     *            the JSONObject.
     */
    public JSONObject(Map<String, ?> map) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }


    /**
     * Construct a JSONObject from an Object, using reflection to find the
     * public members. The resulting JSONObject's keys will be the strings
     * from the names array, and the values will be the field values associated
     * with those keys in the object. If a key is not found or not visible,
     * then it will not be copied into the new JSONObject.
     *
     * @param object An object that has fields that should be used to make a
     *               JSONObject.
     * @param names  An array of strings, the names of the fields to be used
     *               from the object.
     */
    public JSONObject(Object object, Iterable<String> names) {
        Class c = object.getClass();
        for (String name : names) {
            try {
                this.put(name, c.getField(name).get(object));
            } catch (Exception e) {
                try {
                    //noinspection unchecked
                    this.put(name, c.getMethod(name).invoke(object));
                } catch (Exception ee) {
                    try {
                        //noinspection unchecked
                        this.put(name, c.getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1)).invoke(object));
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    /**
     * Construct a JSONObject from a string.
     * This is the most commonly used JSONObject constructor.
     *
     * @param string A string beginning
     *               with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *               with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws com.ovea.tajin.json.JSONException If there is a syntax error in the source string.
     */
    public JSONObject(String string) throws JSONException {
        this(new JSONTokener(string));
    }

    JSONObject(JSONTokener x) throws JSONException {
        this();
        char c;
        String key;

        if (x.nextClean() != '{') {
            throw x.syntaxError("A JSONObject text must begin with '{'");
        }
        for (; ; ) {
            c = x.nextClean();
            switch (c) {
                case 0:
                    throw x.syntaxError("A JSONObject text must end with '}'");
                case '}':
                    return;
                default:
                    x.back();
                    key = x.nextValue().toString();
            }

            /*
             * The key is followed by ':'. We will also tolerate '=' or '=>'.
             */

            c = x.nextClean();
            if (c == '=') {
                if (x.next() != '>') {
                    x.back();
                }
            } else if (c != ':') {
                throw x.syntaxError("Expected a ':' after a key");
            }
            put(key, x.nextValue());

            /*
             * Pairs are separated by ','. We will also tolerate ';'.
             */

            switch (x.nextClean()) {
                case ';':
                case ',':
                    if (x.nextClean() == '}') {
                        return;
                    }
                    x.back();
                    break;
                case '}':
                    return;
                default:
                    throw x.syntaxError("Expected a ',' or '}'");
            }
        }
    }

    /**
     * Accumulate values under a key. It is similar to the put method except
     * that if there is already an object stored under the key then a
     * JSONArray is stored under the key to hold all of the accumulated values.
     * If there is already a JSONArray, then the new value is appended to it.
     * In contrast, the put method replaces the previous value.
     *
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException If the value is an invalid number
     *                                     or if the key is null.
     */
    public JSONObject accumulate(String key, Object value) throws JSONException {
        JSONType o = opt(key);
        if (o == null) {
            put(key, value);
        } else if (o instanceof JSONArray) {
            ((JSONArray) o).put(value);
        } else {
            put(key, new JSONArray().put(o).put(value));
        }
        return this;
    }


    /**
     * Append values to the array under a key. If the key does not exist in the
     * JSONObject, then the key is put in the JSONObject with its value being a
     * JSONArray containing the value parameter. If the key was already
     * associated with a JSONArray, then the value parameter is appended to it.
     *
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException If the key is null or if the current value
     *                                     associated with the key is not a JSONArray.
     */
    public JSONObject append(String key, Object value) throws JSONException {
        JSONType o = opt(key);
        if (o == null) {
            put(key, new JSONArray().put(value));
        } else if (!(o instanceof JSONArray)) {
            throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
        } else {
            ((JSONArray) o).put(value);
        }
        return this;
    }

    /**
     * Get the value object associated with a key.
     *
     * @param key A key string.
     * @return The object associated with the key.
     * @throws com.ovea.tajin.json.JSONException if the key is not found.
     */
    public JSONType get(String key) throws JSONException {
        JSONType o = opt(key);
        if (o == null) {
            throw new JSONException("JSONObject[" + Utils.quote(key) + "] not found.");
        }
        return o;
    }

    /**
     * Get the boolean value associated with a key.
     *
     * @param key A key string.
     * @return The truth.
     * @throws com.ovea.tajin.json.JSONException if the value is not a Boolean or the String "true" or "false".
     */
    public boolean getBoolean(String key) throws JSONException {
        try {
            return get(key).asBoolean();
        } catch (ConversionException e) {
            throw new JSONException("JSONObject[" + Utils.quote(key) + "] is not a boolean.");
        }
    }

    /**
     * Get the double value associated with a key.
     *
     * @param key A key string.
     * @return The numeric value.
     * @throws com.ovea.tajin.json.JSONException if the key is not found or
     *                                     if the value is not a Number object and cannot be converted to a number.
     */
    public double getDouble(String key) throws JSONException {
        try {
            return get(key).asDouble();
        } catch (ConversionException e) {
            throw new JSONException("JSONObject[" + Utils.quote(key) + "] is not a number.");
        }
    }

    /**
     * Get the int value associated with a key. If the number value is too
     * large for an int, it will be clipped.
     *
     * @param key A key string.
     * @return The integer value.
     * @throws com.ovea.tajin.json.JSONException if the key is not found or if the value cannot
     *                                     be converted to an integer.
     */
    public int getInt(String key) throws JSONException {
        try {
            return get(key).asInt();
        } catch (ConversionException e) {
            throw new JSONException("JSONObject[" + Utils.quote(key) + "] is not a number.");
        }
    }

    public JSONArray getArray(String key) throws JSONException {
        JSONType o = get(key);
        if (o.isArray()) {
            return o.asArray();
        }
        throw new JSONException("JSONObject[" + Utils.quote(key) + "] is not a JSONArray.");
    }

    public JSONObject getObject(String key) throws JSONException {
        JSONType o = get(key);
        if (o.isObject()) {
            return o.asObject();
        }
        throw new JSONException("JSONObject[" + Utils.quote(key) + "] is not a JSONObject.");
    }

    public long getLong(String key) throws JSONException {
        try {
            return get(key).asLong();
        } catch (ConversionException e) {
            throw new JSONException("JSONObject[" + Utils.quote(key) + "] is not a number.");
        }
    }

    public String getString(String key) throws JSONException {
        return get(key).asString();
    }

    public boolean has(String key) {
        return this.myHashMap.containsKey(key);
    }

    public boolean isNull(String key) {
        return JSON.NULL.equals(opt(key));
    }

    @Override
    public Iterator<String> iterator() {
        return keys().iterator();
    }

    public Iterable<String> keys() {
        return this.myHashMap.keySet();
    }

    /**
     * Produce a JSONArray containing the names of the elements of this
     * JSONObject.
     *
     * @return A JSONArray containing the key strings, or empty array if the JSONObject
     *         is empty.
     */
    public JSONArray keysArray() {
        return new JSONArray(keys());
    }

    public Iterable<Map.Entry<String, JSONType>> entries() {
        return this.myHashMap.entrySet();
    }

    public int size() {
        return this.myHashMap.size();
    }

    public int length() {
        return size();
    }

    public boolean isEmpty() {
        return this.myHashMap.isEmpty();
    }

    public JSONArray values() {
        return new JSONArray(this.myHashMap.values());
    }

    /**
     * Produce a JSONArray containing the values of the members of this
     * JSONObject.
     *
     * @param names A JSONArray containing a list of key strings. This
     *              determines the sequence of the values in the result.
     * @return A JSONArray of values.
     * @throws com.ovea.tajin.json.JSONException If any of the values are non-finite numbers.
     */
    public JSONArray values(JSONArray names) throws JSONException {
        JSONArray ja = new JSONArray();
        if (names == null || names.length() == 0) {
            return ja;
        }
        for (JSONType name : names) {
            ja.put(this.opt(name.toString()));
        }
        return ja;
    }

    public JSONArray values(String... keys) throws JSONException {
        return values(new JSONArray(Arrays.asList(keys)));
    }

    public JSONArray values(Iterable<String> keys) throws JSONException {
        return values(new JSONArray(keys));
    }

    /**
     * Get an optional value associated with a key.
     *
     * @param key A key string.
     * @return An object which is the value, or null if there is no value.
     */
    public JSONType opt(String key) {
        return key == null ? null : this.myHashMap.get(key);
    }

    /**
     * Get an optional boolean associated with a key.
     * It returns false if there is no such key, or if the value is not
     * Boolean.TRUE or the String "true".
     *
     * @param key A key string.
     * @return The truth.
     */
    public boolean optBoolean(String key) {
        return optBoolean(key, false);
    }

    /**
     * Get an optional boolean associated with a key.
     * It returns the defaultValue if there is no such key, or if it is not
     * a Boolean or the String "true" or "false" (case insensitive).
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return The truth.
     */
    public boolean optBoolean(String key, boolean defaultValue) {
        try {
            return getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional double associated with a key,
     * or NaN if there is no such key or if its value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key A string which is the key.
     * @return An object which is the value.
     */
    public double optDouble(String key) {
        return optDouble(key, Double.NaN);
    }


    /**
     * Get an optional double associated with a key, or the
     * defaultValue if there is no such key or if its value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return An object which is the value.
     */
    public double optDouble(String key, double defaultValue) {
        try {
            return opt(key).asDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional int value associated with a key,
     * or zero if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key A key string.
     * @return An object which is the value.
     */
    public int optInt(String key) {
        return optInt(key, 0);
    }


    /**
     * Get an optional int value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return An object which is the value.
     */
    public int optInt(String key, int defaultValue) {
        try {
            return getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     * Get an optional JSONArray associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * JSONArray.
     *
     * @param key A key string.
     * @return A JSONArray which is the value.
     */
    public JSONArray optArray(String key) {
        JSONType o = opt(key);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    /**
     * Get an optional JSONObject associated with a key.
     * It returns null if there is no such key, or if its value is not a
     * JSONObject.
     *
     * @param key A key string.
     * @return A JSONObject which is the value.
     */
    public JSONObject optObject(String key) {
        JSONType o = opt(key);
        return o instanceof JSONObject ? (JSONObject) o : null;
    }

    /**
     * Get an optional long value associated with a key,
     * or zero if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key A key string.
     * @return An object which is the value.
     */
    public long optLong(String key) {
        return optLong(key, 0);
    }


    /**
     * Get an optional long value associated with a key,
     * or the default if there is no such key or if the value is not a number.
     * If the value is a string, an attempt will be made to evaluate it as
     * a number.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return An object which is the value.
     */
    public long optLong(String key, long defaultValue) {
        try {
            return getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional string associated with a key.
     * It returns an empty string if there is no such key. If the value is not
     * a string and is not null, then it is coverted to a string.
     *
     * @param key A key string.
     * @return A string which is the value.
     */
    public String optString(String key) {
        return optString(key, "");
    }


    /**
     * Get an optional string associated with a key.
     * It returns the defaultValue if there is no such key.
     *
     * @param key          A key string.
     * @param defaultValue The default.
     * @return A string which is the value.
     */
    public String optString(String key, String defaultValue) {
        JSONType o = opt(key);
        return o != null ? o.asString() : defaultValue;
    }

    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONArray which is produced from a Collection.
     *
     * @param key   A key string.
     * @param value A Collection value.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException
     */
    public JSONObject put(String key, Iterable<?> value) throws JSONException {
        put(key, JSON.valueOf(value));
        return this;
    }

    public JSONObject putNull(String key) throws JSONException {
        return put(key, JSON.NULL);
    }

    public JSONObject put(String key, Object... values) throws JSONException {
        put(key, JSON.valueOf(values));
        return this;
    }

    /**
     * Put a key/boolean pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A boolean which is the value.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException If the key is null.
     */
    public JSONObject put(String key, boolean value) throws JSONException {
        put(key, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a key/double pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A double which is the value.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException If the key is null or if the number is invalid.
     */
    public JSONObject put(String key, double value) throws JSONException {
        put(key, new Double(value));
        return this;
    }


    /**
     * Put a key/int pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value An int which is the value.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException If the key is null.
     */
    public JSONObject put(String key, int value) throws JSONException {
        put(key, new Integer(value));
        return this;
    }


    /**
     * Put a key/long pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A long which is the value.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException If the key is null.
     */
    public JSONObject put(String key, long value) throws JSONException {
        put(key, new Long(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONObject which is produced from a Map.
     *
     * @param key   A key string.
     * @param value A Map value.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException
     */
    public JSONObject put(String key, Map<String, ?> value) throws JSONException {
        put(key, new JSONObject(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject. If the value is null,
     * then the key will be removed from the JSONObject if it is present.
     *
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *              types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *              or the JSON.NULL object.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException If the value is non-finite number
     *                                     or if the key is null.
     */
    public JSONObject put(String key, Object value) throws JSONException {
        this.myHashMap.put(key, JSON.valueOf(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, but only if the
     * key and the value are both non-null.
     *
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *              types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *              or the JSON.NULL object.
     * @return this.
     * @throws com.ovea.tajin.json.JSONException If the value is a non-finite number.
     */
    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            put(key, value);
        }
        return this;
    }

    /**
     * Remove a name and its value, if present.
     *
     * @param key The name to be removed.
     * @return The value that was associated with the name,
     *         or null if there was no value.
     */
    public JSONType remove(String key) {
        return this.myHashMap.remove(key);
    }

    public JSONObject removeAll(String... keys) {
        for (String key : keys) {
            remove(key);
        }
        return this;
    }

    public JSONObject merge(JSONObject overrides) {
        myHashMap.putAll(overrides.myHashMap);
        return this;
    }

    /**
     * Make a JSON text of this JSONObject. For compactness, no whitespace
     * is added. If this would not result in a syntactically correct JSON text,
     * then '' will be returned instead.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return a printable, displayable, portable, transmittable
     *         representation of the object, beginning
     *         with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *         with <code>}</code>&nbsp;<small>(right brace)</small>.
     */
    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder("{");
            for (String key : keys()) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                sb.append(Utils.quote(key));
                sb.append(':');
                sb.append(this.myHashMap.get(key));
            }
            sb.append('}');
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Make a prettyprinted JSON text of this JSONObject.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @param indentFactor The number of spaces to add to each level of
     *                     indentation.
     * @param indent       The indentation of the top level.
     * @return a printable, displayable, transmittable
     *         representation of the object, beginning
     *         with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *         with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws com.ovea.tajin.json.JSONException If the object contains an invalid number.
     */
    @Override
    public String toString(int indentFactor, int indent) throws JSONException {
        int i;
        int n = size();
        if (n == 0) {
            return "{}";
        }
        Iterator<String> keys = keys().iterator();
        StringBuilder sb = new StringBuilder("{");
        int newindent = indent + indentFactor;
        String o;
        if (n == 1) {
            o = keys.next();
            sb.append(Utils.quote(o));
            sb.append(": ");
            sb.append(this.myHashMap.get(o).toString(indentFactor, indent));
        } else {
            while (keys.hasNext()) {
                o = keys.next();
                if (sb.length() > 1) {
                    sb.append(",\n");
                } else {
                    sb.append('\n');
                }
                for (i = 0; i < newindent; i += 1) {
                    sb.append(' ');
                }
                sb.append(Utils.quote(o));
                sb.append(": ");
                sb.append(this.myHashMap.get(o).toString(indentFactor, newindent));
            }
            if (sb.length() > 1) {
                sb.append('\n');
                for (i = 0; i < indent; i += 1) {
                    sb.append(' ');
                }
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Write the contents of the JSONObject as JSON text to a writer.
     * For compactness, no whitespace is added.
     * <p/>
     * Warning: This method assumes that the data structure is acyclical.
     *
     * @return The writer.
     * @throws com.ovea.tajin.json.JSONException
     */
    @Override
    public Writer write(Writer writer) throws JSONException {
        try {
            boolean b = false;
            Iterator<String> keys = keys().iterator();
            writer.write('{');
            while (keys.hasNext()) {
                if (b) {
                    writer.write(',');
                }
                String k = keys.next();
                writer.write(Utils.quote(k));
                writer.write(':');
                JSONType v = this.myHashMap.get(k);
                if (v instanceof JSONObject || v instanceof JSONArray) {
                    v.write(writer);
                } else {
                    writer.write(v.toString());
                }
                b = true;
            }
            writer.write('}');
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JSONObject object = (JSONObject) o;
        return myHashMap.equals(object.myHashMap);
    }

    @Override
    public int hashCode() {
        return myHashMap.hashCode();
    }

    public Map<String, JSONType> toMap() {
        return new LinkedHashMap<>(myHashMap);
    }
}