package com.ovea.tajin.json.jsonpath;

import com.ovea.tajin.json.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.ovea.tajin.json.JSON.filter;
import static com.ovea.tajin.json.JSON.where;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: kallestenflo
 * Date: 3/5/12
 * Time: 4:24 PM
 */
public class JsonPathFilterTest {

    @Test
    public void arrays_of_maps_can_be_filtered() throws Exception {


        Map<String, Object> rootGrandChild_A = new HashMap<>();
        rootGrandChild_A.put("name", "rootGrandChild_A");

        Map<String, Object> rootGrandChild_B = new HashMap<>();
        rootGrandChild_B.put("name", "rootGrandChild_B");

        Map<String, Object> rootGrandChild_C = new HashMap<>();
        rootGrandChild_C.put("name", "rootGrandChild_C");


        Map<String, Object> rootChild_A = new HashMap<>();
        rootChild_A.put("name", "rootChild_A");
        rootChild_A.put("children", asList(rootGrandChild_A, rootGrandChild_B, rootGrandChild_C));

        Map<String, Object> rootChild_B = new HashMap<>();
        rootChild_B.put("name", "rootChild_B");
        rootChild_B.put("children", asList(rootGrandChild_A, rootGrandChild_B, rootGrandChild_C));

        Map<String, Object> rootChild_C = new HashMap<>();
        rootChild_C.put("name", "rootChild_C");
        rootChild_C.put("children", asList(rootGrandChild_A, rootGrandChild_B, rootGrandChild_C));

        Map<String, Object> root = new HashMap<>();
        root.put("children", asList(rootChild_A, rootChild_B, rootChild_C));



        Filter customFilter = new Filter.FilterAdapter() {
            @Override
            public boolean accept(JSONType map) {
                return map.asObject().get("name").asString().equals("rootGrandChild_A");
            }
        };

        Filter rootChildFilter = filter(where("name").regex(Pattern.compile("rootChild_[A|B]")));
        Filter rootGrandChildFilter = filter(where("name").regex(Pattern.compile("rootGrandChild_[A|B]")));

        JSONArray read = JSON.expr(new JSONObject(root), "children[?].children[?][?]", rootChildFilter, rootGrandChildFilter, customFilter).asArray();


        System.out.println(read.size());
    }


    @Test
    public void arrays_of_objects_can_be_filtered() throws Exception {
        Map<String, Object> doc = new HashMap<>();
        doc.put("items", asList(1, 2, 3));

        Filter customFilter = new Filter.FilterAdapter(){
            @Override
            public boolean accept(JSONType o) {
                return 1 == o.asInt();
            }
        };

        JSONArray res = JSON.expr(new JSONObject(doc), "$.items[?]", customFilter).asArray();

        assertEquals(1, res.get(0).asInt());
    }

}
