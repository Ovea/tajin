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
import org.codehaus.jettison.json.JSONObject
import org.junit.Test

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
//@RunWith(ConcurrentJunitRunner)
class JSONObjectTest {

    @Test
    void test_ctor() throws Exception {
        org.junit.Assert.assertEquals new JSONObject().toString(), JSON.object().toString()
        org.junit.Assert.assertEquals new JSONObject("{'both':2}").toString(), JSON.object("{'both':2}").toString()
        org.junit.Assert.assertEquals new JSONObject([a: 1, b: 2], ['a'] as String[]).toString(), JSON.object([a: 1, b: 2], ['a'] as String[]).toString()
        org.junit.Assert.assertEquals new JSONObject([a: 1, b: 2], ['a'] as String[]).toString(), JSON.object([a: 1, b: 2], ['a']).toString()
        org.junit.Assert.assertEquals new JSONObject([a: 1, b: 2], ['a'] as String[]).toString(), JSON.object([a: 1, b: 2], ['a', 'inexisting']).toString()
        org.junit.Assert.assertEquals new JSONObject([a: 1, b: 2]).toString(), JSON.object([a: 1, b: 2]).toString()
        org.junit.Assert.assertEquals new JSONObject([a: 1, b: 2]).toString(), JSON.object([a: 1, b: 2]).toString()

        org.junit.Assert.assertEquals new JSONObject("{'both':2}").toString(), JSON.object("{ 'both' : 2 }").toString()
        org.junit.Assert.assertEquals new JSONObject("{'both':2}").toString(), JSON.object(JSON.object(JSON.object("{'both':2}"))).toString()
        org.junit.Assert.assertEquals new JSONObject("{'both':2}").toString(), JSON.object(JSON.object("{'both':2}"), "both").toString()
        org.junit.Assert.assertEquals new JSONObject("{'both':2}").toString(), JSON.object(JSON.object("{'both':2}"), JSON.array().put("both")).toString()
        org.junit.Assert.assertEquals new JSONObject("{'both':2}").toString(), JSON.object(JSON.object("{'both':2}"), Arrays.asList("both")).toString()

        org.junit.Assert.assertEquals new JSONObject("{'both':2}").toString(), JSON.object("{'both'=2}").toString()
        org.junit.Assert.assertEquals new JSONObject("{'both':2}").toString(), JSON.object("{'both'=>2}").toString()
        org.junit.Assert.assertEquals new JSONObject("{'both':2,'a':1}").toString(), JSON.object("{'both':2 , 'a':1}").toString()
        org.junit.Assert.assertEquals new JSONObject("{'both':2,'a':1}").toString(), JSON.object("{'both':2 ; 'a':1}").toString()

        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("A JSONObject text must begin with '{' at character 0 of "))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("A JSONObject text must end with '}' at character 1 of {"))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1': 1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("Expected a ',' or '}' at character 7 of {'1': 1"))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1':")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("Missing value. at character 4 of {'1':"))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1'")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("Expected a ':' after a key at character 4 of {'1'"))
    }

    @Test
    void test_arrays() throws Exception {
        org.junit.Assert.assertEquals new JSONObject("{'a':[1,2,3]}").toString(), JSON.object().accumulate('a', 1).accumulate('a', 2).accumulate('a', 3).toString()
        org.junit.Assert.assertEquals new JSONObject("{'a':[1,2,3]}").toString(), JSON.object().accumulate('a', JSON.array().put(1)).accumulate('a', 2).accumulate('a', 3).toString()
        org.junit.Assert.assertEquals new JSONObject('{"a":[1,2,3]}').toString(), JSON.object().append('a', 1).append('a', 2).append('a', 3).toString()
        org.junit.Assert.assertEquals new JSONObject('{"a":[[1],2,3]}').toString(), JSON.object().append('a', JSON.array().put(1)).append('a', 2).append('a', 3).toString()
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object().put('a', JSON.object()).append('a', 2)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSONObject[a] is not a JSONArray."))
    }

    @Test
    void test_get() throws Exception {
        org.junit.Assert.assertEquals true, JSON.object("{'1':2}").has('1')

        org.junit.Assert.assertEquals new JSONObject("{'1':2}").get('1'), JSON.object("{'1':2}").get('1').asValue()
        org.junit.Assert.assertEquals new JSONObject("{'1':2}").getInt('1'), JSON.object("{'1':2}").getInt('1')
        org.junit.Assert.assertEquals new JSONObject("{'1':" + Long.MAX_VALUE + "}").getLong('1'), JSON.object("{'1':" + Long.MAX_VALUE + "}").getLong('1')
        org.junit.Assert.assertEquals new JSONObject("{'1':" + Double.MAX_VALUE + "}").getDouble('1'), JSON.object("{'1':" + Double.MAX_VALUE + "}").getDouble('1'), 0.0001
        org.junit.Assert.assertEquals new JSONObject("{'1':true}").getBoolean('1'), JSON.object("{'1':true}").getBoolean('1')
        org.junit.Assert.assertEquals new JSONObject("{'1':'true'}").getBoolean('1'), JSON.object("{'1':'true'}").getBoolean('1')
        org.junit.Assert.assertEquals new JSONObject("{'1':null}").isNull('1'), JSON.object("{'1':null}").isNull('1')
        org.junit.Assert.assertEquals new JSONObject("{'1':[]}").getJSONArray('1').toString(), JSON.object("{'1':[]}").getArray('1').toString()
        org.junit.Assert.assertEquals new JSONObject("{'1':{}}").getJSONObject('1').toString(), JSON.object("{'1':{}}").getObject('1').toString()
        org.junit.Assert.assertEquals "null", JSON.object().put('1', (Object) null).getString('1')
        org.junit.Assert.assertEquals new JSONObject().put('1', "q").getString('1'), JSON.object().put('1', "q").getString('1')
        org.junit.Assert.assertEquals new JSONObject().put('1', "q").get('1'), JSON.object().put('1', "q").get('1').asValue()
    }

    @Test
    void test_get_failings() throws Exception {
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{}").get("1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONObject["1"] not found.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1':1}").getArray("1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONObject["1"] is not a JSONArray.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1':1}").getObject("1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONObject["1"] is not a JSONObject.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1':true}").getInt("1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONObject["1"] is not a number.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1':true}").getLong("1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONObject["1"] is not a number.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1':true}").getDouble("1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONObject["1"] is not a number.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1':[]}").getDouble("1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONObject["1"] is not a number.'))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object("{'1':[]}").getBoolean("1")}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage('JSONObject["1"] is not a boolean.'))
    }

    @Test
    void test_opt() throws Exception {
        org.junit.Assert.assertEquals null, JSON.object("{}").opt('1')
        org.junit.Assert.assertEquals 1, JSON.object("{'1':1}").opt('1').asValue()
        org.junit.Assert.assertEquals true, JSON.object("{'1':true}").optBoolean('1')
        org.junit.Assert.assertEquals false, JSON.object("{}").optBoolean('1')
        org.junit.Assert.assertEquals true, JSON.object("{}").optBoolean('1', true)
        org.junit.Assert.assertEquals 1.0, JSON.object("{'1':1.0}").optDouble('1'), 0.001
        org.junit.Assert.assertEquals Double.NaN, JSON.object("{}").optDouble('1'), 0.001
        org.junit.Assert.assertEquals 1.0, JSON.object("{}").optDouble('1', 1.0), 0.001
        org.junit.Assert.assertEquals 1, JSON.object("{'1':1}").optInt('1')
        org.junit.Assert.assertEquals 0, JSON.object("{}").optInt('1')
        org.junit.Assert.assertEquals 3, JSON.object("{}").optInt('1', 3)
        org.junit.Assert.assertEquals Long.MAX_VALUE, JSON.object("{'1':9223372036854775807}").optLong('1')
        org.junit.Assert.assertEquals 0, JSON.object("{}").optLong('1')
        org.junit.Assert.assertEquals Long.MAX_VALUE, JSON.object("{}").optLong('1', Long.MAX_VALUE)
        org.junit.Assert.assertEquals "a", JSON.object("{'1':'a'}").optString('1')
        org.junit.Assert.assertEquals "", JSON.object("{}").optString('1')
        org.junit.Assert.assertEquals "a", JSON.object("{}").optString('1', "a")
        org.junit.Assert.assertEquals null, JSON.object("{}").optArray('1')
        org.junit.Assert.assertEquals "[]", JSON.object("{'1':[]}").optArray('1').toString()
        org.junit.Assert.assertEquals null, JSON.object("{}").optObject('1')
        org.junit.Assert.assertEquals "{}", JSON.object("{'1':{}}").optObject('1').toString()
    }

    @Test
    void test_put() throws Exception {
        org.junit.Assert.assertEquals(
            new JSONObject().put('2', new JSONArray(Arrays.asList(true, 1, 'a'))).put('1', (boolean) false).put('1', (boolean) true).put('1', Double.MAX_VALUE).put('1', 1).put('1', Long.MAX_VALUE).put('1', (Object) null).put('1', (Object) null).put('1', [a: 1, b: 'f']).put('1', BigInteger.TEN).put('1', Arrays.asList('1', '2')).toString(),
            JSON.object().put('2', true, 1, 'a').put('1', (boolean) false).put('1', (boolean) true).put('1', Double.MAX_VALUE).put('1', 1).put('1', Long.MAX_VALUE).put('1', (Object) null).putNull('1').put('1', [a: 1, b: 'f']).put('1', BigInteger.TEN).put('1', Arrays.asList('1', '2')).toString())
    }

    @Test
    void test_put_failures() throws Exception {
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object().put('1', Double.NaN)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSON does not allow non-finite numbers."))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object().put('1', Float.NaN)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSON does not allow non-finite numbers."))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object().put('1', Double.POSITIVE_INFINITY)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSON does not allow non-finite numbers."))
        org.junit.Assert.assertThat(com.mycila.junit.matchers.ExceptionMatchers.expression {JSON.object().put('1', Float.POSITIVE_INFINITY)}, com.mycila.junit.matchers.ExceptionMatchers.thrown(JSONException).withMessage("JSON does not allow non-finite numbers."))
    }

    @Test
    void test_transforms() throws Exception {
        org.junit.Assert.assertEquals '["one","two"]', JSON.object('{"one":1,"two":2}').keysArray().toString()
        org.junit.Assert.assertEquals '{"three":3}', JSON.object('{"one":1,"two":2,"three":3}').removeAll('one', 'two').toString()

        def c = 0
        JSON.object('{"one":1,"two":2}').entries().each {c++}
        org.junit.Assert.assertEquals 2, c

        org.junit.Assert.assertEquals 2, JSON.object('{"one":1,"two":2}').size()
        org.junit.Assert.assertEquals 0, JSON.object('{"one":1,"two":2}').values(JSON.array()).length()
        org.junit.Assert.assertEquals 0, JSON.object('{"one":1,"two":2}').values((com.ovea.tajin.json.JSONArray) null).length()

        c = 0
        JSON.object('{"one":1,"two":2}').values().each {c++}
        org.junit.Assert.assertEquals 2, c

        c = 0
        JSON.object('{"one":1,"two":2}').values("one").each {c++}
        org.junit.Assert.assertEquals 1, c

        c = 0
        JSON.object('{"one":1,"two":2}').values(JSON.array("['one', 'two']")).each {c++}
        org.junit.Assert.assertEquals 2, c

        c = 0
        JSON.object('{"one":1,"two":2}').values(JSON.array()).each {c++}
        org.junit.Assert.assertEquals 0, c

        c = 0
        JSON.object('{"one":1,"two":2}').values(Arrays.asList('one', 'two')).each {c++}
        org.junit.Assert.assertEquals 2, c

        c = 0
        JSON.object('{"one":1,"two":2}').values([]).each {c++}
        org.junit.Assert.assertEquals 0, c

        org.junit.Assert.assertEquals """{
    "one": 1,
    "two": 2
}""", JSON.object('{"one":1,"two":2}').toString(4, 0)
        org.junit.Assert.assertEquals """{
     "one": 1,
     "two": 2
 }""", JSON.object('{"one":1,"two":2}').toString(4, 1)
        org.junit.Assert.assertEquals "{}", JSON.object("{ }").toString(4, 0)
        org.junit.Assert.assertEquals '{"one": 1}', JSON.object('{"one" : 1}').toString(4, 0)
        org.junit.Assert.assertEquals '{"one": 1}', JSON.object('{ "one" : 1 }').toString(4, 0)

        JSON.object('{"one":1,"two":2,"obj":{},"arr":[]}').with {j ->
            new StringWriter().with {sw ->
                j.write(sw)
                org.junit.Assert.assertEquals '{"one":1,"two":2,"obj":{},"arr":[]}', sw.toString()
            }
        }

        org.junit.Assert.assertEquals '"one"', JSON.object('{"one":"one"}').get('one').toString()
        org.junit.Assert.assertEquals 'one', JSON.object('{"one":"one"}').get('one').asString()

        org.junit.Assert.assertEquals 'null', JSON.object('{"null":null}').get('null').toString()
        org.junit.Assert.assertEquals 'null', JSON.object('{"null":null}').get('null').asString()
    }

}
