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

import com.ovea.tajin.json.Filter;
import com.ovea.tajin.json.JSONArray;
import com.ovea.tajin.json.JSONType;

import java.util.LinkedList;

/**
 * @author Kalle Stenflo
 */
public class ArrayQueryFilter extends PathTokenFilter {

    ArrayQueryFilter(String condition) {
        super(condition);
    }

    @Override
    public JSONArray filter(JSONType obj, LinkedList<Filter> filters, boolean inArrayContext) {
        Filter filter = filters.poll();
        return filter.doFilter(obj.asArray());
    }

    @Override
    public JSONArray filter(JSONType obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isArrayFilter() {
        return true;
    }
}
