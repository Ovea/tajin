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

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class JSON {

    public static JSONType NULL = JSONNull.INSTANCE;

    private JSON() {
    }

    public static JSONObject object() {
        return new JSONObject();
    }

    public static JSONObject object(String json) {
        return new JSONObject(json);
    }

    public static JSONObject object(Map<String, ?> map) {
        return new JSONObject(map);
    }

    public static JSONObject object(Object o, String... properties) {
        return new JSONObject(o, Arrays.asList(properties));
    }

    public static JSONObject object(Object o, Iterable<String> properties) {
        return new JSONObject(o, properties);
    }

    public static JSONObject object(JSONObject obj) {
        return new JSONObject(obj);
    }

    public static JSONObject object(JSONObject obj, String... properties) {
        return new JSONObject(obj, properties);
    }

    public static JSONObject object(JSONObject obj, Iterable<String> properties) {
        return new JSONObject(obj, properties);
    }

    public static JSONObject object(JSONObject obj, JSONArray properties) {
        return new JSONObject(obj, properties);
    }

    public static JSONArray array() {
        return new JSONArray();
    }

    public static JSONArray array(String json) {
        return new JSONArray(json);
    }

    public static JSONArray array(Iterable<?> iterable) {
        return new JSONArray(iterable);
    }

    public static JSONArray array(Object... values) {
        return new JSONArray(values);
    }

    public static JSONArray array(JSONArray array) {
        return new JSONArray(array);
    }

    public static JSONType parse(String data) {
        return parse(new StringReader(data));
    }

    public static JSONType parse(Reader in) {
        try {
            return new JSONParser().parse(in);
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static JSONType valueOf(Object o) {
        return JSONType.valueOf(o);
    }

    public static JSONExpr compile(String jsonPath, Filter... filters) {
        return JSONExpr.compile(jsonPath, filters);
    }

    public static JSONType expr(JSONType json, String expression, Filter... filters) {
        return JSONExpr.expr(json, expression, filters);
    }

    public static JSONType expr(String json, String expression, Filter... filters) {
        return JSONExpr.expr(json, expression, filters);
    }

    public static JSONType expr(File jsonFile, String expression, Filter... filters) {
        return JSONExpr.expr(jsonFile, expression, filters);
    }

    public static JSONType expr(Reader in, String expression, Filter... filters) {
        return JSONExpr.expr(in, expression, filters);
    }

    public static Criteria where(String key) {
        return Criteria.where(key);
    }

    public static Filter filter(Criteria criteria) {
        return Filter.filter(criteria);
    }
}
