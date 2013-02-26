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

import com.ovea.tajin.json.JSON;
import com.ovea.tajin.json.JSONType;

/**
 * @author Kalle Stenflo
 */
public class ExpressionEvaluator {

    public static boolean eval(Object data, String comparator, String expected) {
        return eval(JSON.valueOf(data), comparator, expected);
    }

    public static boolean eval(JSONType data, String comparator, String expected) {
        Object actual = data.asValue();
        if (actual instanceof Long) {
            Long a = (Long) actual;
            Long e = Long.parseLong(expected.trim());
            switch (comparator) {
                case "==":
                    return a.longValue() == e.longValue();
                case "!=":
                case "<>":
                    return a.longValue() != e.longValue();
                case ">":
                    return a > e;
                case ">=":
                    return a >= e;
                case "<":
                    return a < e;
                case "<=":
                    return a <= e;
            }
        } else if (actual instanceof Integer) {
            Integer a = (Integer) actual;
            Integer e = Integer.parseInt(expected.trim());
            switch (comparator) {
                case "==":
                    return a.intValue() == e.intValue();
                case "!=":
                case "<>":
                    return a.intValue() != e.intValue();
                case ">":
                    return a > e;
                case ">=":
                    return a >= e;
                case "<":
                    return a < e;
                case "<=":
                    return a <= e;
            }
        } else if (actual instanceof Double) {
            Double a = (Double) actual;
            Double e = Double.parseDouble(expected.trim());
            switch (comparator) {
                case "==":
                    return a.doubleValue() == e.doubleValue();
                case "!=":
                case "<>":
                    return a.doubleValue() != e.doubleValue();
                case ">":
                    return a > e;
                case ">=":
                    return a >= e;
                case "<":
                    return a < e;
                case "<=":
                    return a <= e;
            }
        } else if (actual instanceof String) {
            String a = (String) actual;
            expected = expected.trim();
            if (expected.startsWith("'")) {
                expected = expected.substring(1);
            }
            if (expected.endsWith("'")) {
                expected = expected.substring(0, expected.length() - 1);
            }
            if ("==".equals(comparator)) {
                return a.equals(expected);
            } else if ("!=".equals(comparator) || "<>".equals(comparator)) {
                return !a.equals(expected);
            }
        }

        return false;
    }
}
