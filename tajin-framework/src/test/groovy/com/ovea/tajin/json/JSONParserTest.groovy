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

import org.junit.Test

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
//@RunWith(ConcurrentJunitRunner)
class JSONParserTest {
    @Test
    void test_parsing() throws Exception {
        System.out.println("=======decode=======");

        String s = "[0,{\"1\":{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}}]";
        Object obj = JSON.parse(s);
        org.junit.Assert.assertTrue obj instanceof JSONArray
        JSONArray array = (JSONArray) obj;
        System.out.println("======the 2nd element of array======");
        System.out.println(array.get(1));
        System.out.println();
        org.junit.Assert.assertEquals("{\"1\":{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}}", array.get(1).toString());

        JSONObject obj2 = (JSONObject) array.get(1);
        System.out.println("======field \"1\"==========");
        System.out.println(obj2.get("1"));
        org.junit.Assert.assertEquals("{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}", obj2.get("1").toString());

        s = "{}";
        obj = JSON.parse(s);
        org.junit.Assert.assertTrue obj instanceof JSONObject
        org.junit.Assert.assertEquals("{}", obj.toString());

        s = "[5,]";
        obj = JSON.parse(s);
        org.junit.Assert.assertTrue obj instanceof JSONArray
        org.junit.Assert.assertEquals("[5]", obj.toString());

        s = "[5,,2]";
        obj = JSON.parse(s);
        org.junit.Assert.assertTrue obj instanceof JSONArray
        org.junit.Assert.assertEquals("[5,2]", obj.toString());

        s = "[\"hello\\bworld\\\"abc\\tdef\\\\ghi\\rjkl\\n123\"]";
        obj = JSON.parse(s);
        println obj
        org.junit.Assert.assertTrue obj instanceof JSONArray
        org.junit.Assert.assertEquals("hello\bworld\"abc\tdef\\ghi\rjkl\n123", ((JSONArray) obj).get(0).asString());

        s = "\"qqqq\"";
        obj = JSON.parse(s);
        println obj
        org.junit.Assert.assertTrue obj instanceof JSONValue
        org.junit.Assert.assertEquals("qqqq", obj.asString());

        s = "1234";
        obj = JSON.parse(s);
        println obj
        org.junit.Assert.assertTrue obj instanceof JSONValue
        org.junit.Assert.assertEquals(1234, obj.asInt());

        JSONObject object = JSON.parse(new File('src/test/resources/test.json').text).asObject();
        println object
    }
}
