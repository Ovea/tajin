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
package com.ovea.tajin.json

import org.codehaus.jettison.json.JSONArray
import org.junit.Test

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
//@RunWith(ConcurentJunitRunner)
class JSONArrayTest {

    @Test
    void test_ctor() throws Exception {
        org.junit.Assert.assertEquals new JSONArray().toString(), JSON.array().toString()
        org.junit.Assert.assertEquals new JSONArray("[1,2]").toString(), JSON.array("[1,2]").toString()
        org.junit.Assert.assertEquals new JSONArray((Collection) null).toString(), JSON.array((Iterable) null).toString()
        org.junit.Assert.assertEquals new JSONArray(Arrays.asList(1, 2, 3)).toString(), JSON.array(1, 2, 3).toString()
        org.junit.Assert.assertEquals new JSONArray(Arrays.asList(1, 2, 3)).toString(), JSON.array(Arrays.asList(1, 2, 3)).toString()

        org.junit.Assert.assertEquals new JSONArray("[1]").toString(), JSON.array("[1,]").toString()
        org.junit.Assert.assertEquals new JSONArray("[ 1 , ]").toString(), JSON.array("[ 1 , ]").toString()
        org.junit.Assert.assertEquals new JSONArray("[1]").toString(), JSON.array("[1;]").toString()
        org.junit.Assert.assertEquals new JSONArray("[1]").toString(), JSON.array("[ 1 ; ]").toString()
        org.junit.Assert.assertEquals new JSONArray("[null, 1]").toString(), JSON.array("[,1]").toString()
        org.junit.Assert.assertEquals new JSONArray("[0,1,2,3]").toString(), JSON.array("[0;1,2;3]").toString()
        org.junit.Assert.assertEquals new JSONArray("[0,1,2,3]").toString(), JSON.array(JSON.array("[0;1,2;3]")).toString()

        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("A JSONArray text must start with '[' at character 0 of "))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("Expected a ',' or ']' at character 2 of [1"))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[ ")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("Missing value. at character 1 of [ "))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("Expected a ',' or ']' at character 0 of ["))
    }

    @Test
    void test_get() throws Exception {
        org.junit.Assert.assertEquals new JSONArray("[1,2]").get(0), JSON.array("[1,2]").get(0).asValue()
        org.junit.Assert.assertEquals new JSONArray("[1,2]").getInt(0), JSON.array("[1,2]").getInt(0)
        org.junit.Assert.assertEquals new JSONArray("[" + Long.MAX_VALUE + "]").getLong(0), JSON.array("[" + Long.MAX_VALUE + "]").getLong(0)
        org.junit.Assert.assertEquals new JSONArray("[" + Double.MAX_VALUE + "]").getDouble(0), JSON.array("[" + Double.MAX_VALUE + "]").getDouble(0), 0.0001
        org.junit.Assert.assertEquals new JSONArray("[true]").getBoolean(0), JSON.array("[true]").getBoolean(0)
        org.junit.Assert.assertEquals new JSONArray("['true']").getBoolean(0), JSON.array("['true']").getBoolean(0)
        org.junit.Assert.assertEquals new JSONArray("[null]").isNull(0), JSON.array("[null]").isNull(0)
        org.junit.Assert.assertEquals new JSONArray("[[]]").getJSONArray(0).toString(), JSON.array("[[]]").getArray(0).toString()
        org.junit.Assert.assertEquals new JSONArray("[{}]").getJSONObject(0).toString(), JSON.array("[{}]").getObject(0).toString()
        org.junit.Assert.assertEquals "null", JSON.array().put((Object) null).getString(0)
        org.junit.Assert.assertEquals new JSONArray().put("q").getString(0), JSON.array().put("q").getString(0)
        org.junit.Assert.assertEquals new JSONArray().put("q").get(0), JSON.array().put("q").get(0).asValue()
    }

    @Test
    void test_get_failings() throws Exception {
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[]").get(0)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONArray[0] not found.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[1]").getArray(0)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONArray[0] is not a JSONArray.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[1]").getObject(0)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONArray[0] is not a JSONObject.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[true]").getInt(0)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONArray[0] is not a number.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[true]").getLong(0)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONArray[0] is not a number.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[true]").getDouble(0)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONArray[0] is not a number.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[[]]").getDouble(0)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONArray[0] is not a number.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array("[[]]").getBoolean(0)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONArray[0] is not a boolean.'))
    }

    @Test
    void test_opt() throws Exception {
        org.junit.Assert.assertEquals null, JSON.array("[]").opt(-1)
        org.junit.Assert.assertEquals null, JSON.array("[]").opt(1)
        org.junit.Assert.assertEquals 1, JSON.array("[1]").opt(0).asValue()
        org.junit.Assert.assertEquals true, JSON.array("[true]").optBoolean(0)
        org.junit.Assert.assertEquals false, JSON.array("[]").optBoolean(0)
        org.junit.Assert.assertEquals true, JSON.array("[]").optBoolean(0, true)
        org.junit.Assert.assertEquals 1.0, JSON.array("[1.0]").optDouble(0), 0.001
        org.junit.Assert.assertEquals Double.NaN, JSON.array("[]").optDouble(0), 0.001
        org.junit.Assert.assertEquals 1.0, JSON.array("[]").optDouble(0, 1.0), 0.001
        org.junit.Assert.assertEquals 1, JSON.array("[1]").optInt(0)
        org.junit.Assert.assertEquals 0, JSON.array("[]").optInt(0)
        org.junit.Assert.assertEquals 3, JSON.array("[]").optInt(0, 3)
        org.junit.Assert.assertEquals Long.MAX_VALUE, JSON.array("[9223372036854775807]").optLong(0)
        org.junit.Assert.assertEquals 0, JSON.array("[]").optLong(0)
        org.junit.Assert.assertEquals Long.MAX_VALUE, JSON.array("[]").optLong(0, Long.MAX_VALUE)
        org.junit.Assert.assertEquals "a", JSON.array("['a']").optString(0)
        org.junit.Assert.assertEquals "", JSON.array("[]").optString(0)
        org.junit.Assert.assertEquals "a", JSON.array("[]").optString(0, "a")
        org.junit.Assert.assertEquals null, JSON.array("[]").optArray(0)
        org.junit.Assert.assertEquals "[]", JSON.array("[[]]").optArray(0).toString()
        org.junit.Assert.assertEquals null, JSON.array("[]").optObject(0)
        org.junit.Assert.assertEquals "{}", JSON.array("[{}]").optObject(0).toString()
    }

    @Test
    void test_put() throws Exception {
        org.junit.Assert.assertEquals(
            new JSONArray().put((boolean) false).put((boolean) true).put(Double.MAX_VALUE).put(1).put(Long.MAX_VALUE).put((Object) null).put((Object) null).put([a: 1, b: 'f']).put(BigInteger.TEN).put(Arrays.asList('1', '2')).toString(),
            JSON.array().put((boolean) false).put((boolean) true).put(Double.MAX_VALUE).put(1).put(Long.MAX_VALUE).put((Object) null).putNull().put([a: 1, b: 'f']).put(BigInteger.TEN).put(Arrays.asList('1', '2')).toString())

        org.junit.Assert.assertEquals(
            new JSONArray().put(0, (boolean) false).put(0, (boolean) true).put(0, Double.MAX_VALUE).put(0, Float.MAX_VALUE).put(0, 1).put(0, Long.MAX_VALUE).put(0, (Object) null).put(0, (Object) null).put(0, [a: 1, b: 'f']).put(0, BigInteger.TEN).put(0, Arrays.asList('1', '2')).toString(),
            JSON.array().put(0, (boolean) false).put(0, (boolean) true).put(0, Double.MAX_VALUE).put(0, Float.MAX_VALUE).put(0, 1).put(0, Long.MAX_VALUE).put(0, (Object) null).putNull(0).put(0, [a: 1, b: 'f']).put(0, BigInteger.TEN).put(0, Arrays.asList('1', '2')).toString())

        org.junit.Assert.assertEquals(new JSONArray().put(3, "a").toString(), JSON.array().put(3, "a").toString())
    }

    @Test
    void test_put_failures() throws Exception {
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array().putNull(-1)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSONArray[-1] not found."))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array().put(Double.NaN)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSON does not allow non-finite numbers."))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array().put(Float.NaN)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSON does not allow non-finite numbers."))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array().put(Double.POSITIVE_INFINITY)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSON does not allow non-finite numbers."))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.array().put(Float.POSITIVE_INFINITY)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSON does not allow non-finite numbers."))
    }

    @Test
    void test_transforms() throws Exception {
        org.junit.Assert.assertEquals '[1,3]', JSON.array("[1,2,3]").removeAll(1).toString()
        org.junit.Assert.assertEquals '{}', JSON.array("[]").toObject("1", "2").toString()
        org.junit.Assert.assertEquals '{}', JSON.array("[1, 2]").toObject((com.ovea.tajin.json.JSONArray) null).toString()
        org.junit.Assert.assertEquals '{}', JSON.array("[1, 2]").toObject().toString()
        org.junit.Assert.assertEquals '{"one":1,"two":2}', JSON.array("[1, 2]").toObject("one", "two").toString()
        org.junit.Assert.assertEquals '{"one":1,"two":2}', JSON.array("[1, 2]").toObject(JSON.array("one", "two")).toString()
        org.junit.Assert.assertEquals '{"one":1,"two":2}', JSON.array("[1, 2]").toObject(Arrays.asList("one", "two")).toString()
        org.junit.Assert.assertEquals """[
    1,
    2
]""", JSON.array("[1, 2]").toString(4, 0)
        org.junit.Assert.assertEquals """[
     1,
     2
 ]""", JSON.array("[1, 2]").toString(4, 1)
        org.junit.Assert.assertEquals "[]", JSON.array("[  ]").toString(4, 0)
        org.junit.Assert.assertEquals "[1]", JSON.array("[ 1 ]").toString(4, 0)
        org.junit.Assert.assertEquals '["ss"]', JSON.array("[ 'ss' ]").toString(4, 0)

        JSON.array('["ss", [1, 2], {}]').with {j ->
            new StringWriter().with {sw ->
                j.write(sw)
                org.junit.Assert.assertEquals '["ss",[1,2],{}]', sw.toString()
            }
        }

        int c = 0;
        for (JSONType type: JSON.array("[1, 2]")) {
            c += type.asInt()
        }
        org.junit.Assert.assertEquals 3, c
    }

}
