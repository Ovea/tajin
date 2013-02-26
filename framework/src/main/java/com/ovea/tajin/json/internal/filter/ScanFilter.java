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


import com.ovea.tajin.json.JSONArray;
import com.ovea.tajin.json.JSONType;

/**
 * @author Kalle Stenflo
 */
public class ScanFilter extends PathTokenFilter {

    public ScanFilter(String condition) {
        super(condition);
    }

    @Override
    public JSONType filter(JSONType obj) {
        JSONArray result = new JSONArray();
        scan(obj, result);

        return result;
    }

    @Override
    public boolean isArrayFilter() {
        return true;
    }

    private void scan(JSONType container, JSONArray result) {
        if (container.isObject()) {
            result.add(container);
            for (JSONType value : container.asObject().values()) {
                if (value.isContainer()) {
                    scan(value, result);
                }
            }
        } else if (container.isArray()) {
            for (JSONType value : container.asArray()) {
                if (value.isContainer()) {
                    scan(value, result);
                }
            }
        }
    }
}
