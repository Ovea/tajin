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

import com.ovea.tajin.json.InvalidPathException;
import com.ovea.tajin.json.JSONArray;
import com.ovea.tajin.json.JSONObject;
import com.ovea.tajin.json.JSONType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kalle Stenflo
 */
public class ArrayEvalFilter extends PathTokenFilter {

    private static final Pattern PATTERN = Pattern.compile("(.*?)\\s?([=<>]+)\\s?(.*)");

    public ArrayEvalFilter(String condition) {
        super(condition);
    }

    @Override
    public JSONType filter(JSONType obj) {
        //[?(@.isbn == 10)]
        JSONArray src = obj.asArray();
        JSONArray result = new JSONArray();

        String trimmedCondition = condition;

        if(condition.contains("['")){
            trimmedCondition = trimmedCondition.replace("['", ".");
            trimmedCondition = trimmedCondition.replace("']", "");
        }

        trimmedCondition = trim(trimmedCondition, 5, 2);

        ConditionStatement conditionStatement = createConditionStatement(trimmedCondition);

        for (JSONType item : src) {
            if (isMatch(item, conditionStatement)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public boolean isArrayFilter() {
        return true;
    }

    private boolean isMatch(JSONType check, ConditionStatement conditionStatement) {
        if (!check.isObject()) {
            return false;
        }
        JSONObject obj = check.asObject();

        if (!obj.has(conditionStatement.getField())) {
            return false;
        }

        JSONType propertyValue = obj.get(conditionStatement.getField());

        return !propertyValue.isContainer() && ExpressionEvaluator.eval(propertyValue, conditionStatement.getOperator(), conditionStatement.getExpected());
    }


    private ConditionStatement createConditionStatement(String str) {
        Matcher matcher = PATTERN.matcher(str);
        if (matcher.matches()) {
            String property = matcher.group(1);
            String operator = matcher.group(2);
            String expected = matcher.group(3);

            return new ConditionStatement(property, operator, expected);
        } else {
            throw new InvalidPathException("Invalid match " + str);
        }
    }

    private class ConditionStatement {
        private final String field;
        private final String operator;
        private String expected;

        private ConditionStatement(String field, String operator, String expected) {
            this.field = field;
            this.operator = operator.trim();
            this.expected = expected;

            if(this.expected.startsWith("'")){
                this.expected = trim(this.expected, 1, 1);
            }
        }

        public String getField() {
            return field;
        }

        public String getOperator() {
            return operator;
        }

        public String getExpected() {
            return expected;
        }
    }
}
