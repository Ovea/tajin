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
package com.ovea.tajin.json.internal.filter;

import com.ovea.tajin.json.*;

import java.util.LinkedList;

/**
 * @author Kalle Stenflo
 */
public class FieldFilter extends PathTokenFilter {

    public FieldFilter(String condition) {
        super(condition);
    }

    @Override
    public JSONType filter(JSONType obj, LinkedList<Filter> filters, boolean inArrayContext) {
        if (obj.isArray()) {
            if (!inArrayContext) {
                return null;
            } else {
                JSONArray result = new JSONArray();
                for (JSONType current : obj.asArray()) {
                    if (current.isObject()) {
                        JSONObject map = current.asObject();
                        if (map.has(condition)) {
                            JSONType o = map.get(condition);
                            if (o.isArray()) {
                                result.addAll(o.asArray());
                            } else {
                                result.add(map.get(condition));
                            }
                        }
                    }
                }
                return result;
            }
        } else {
            JSONObject map = obj.asObject();
            if (!map.has(condition)) {
                throw new InvalidPathException("invalid path");
            } else {
                return map.get(condition);
            }
        }
    }


    public JSONType filter(JSONType obj) {
        if (obj.isArray()) {
            return obj;
        } else {
            return obj.asObject().get(condition);
        }
    }

    @Override
    public boolean isArrayFilter() {
        return false;
    }


}
