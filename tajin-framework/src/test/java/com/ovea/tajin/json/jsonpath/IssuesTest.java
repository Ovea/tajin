package com.ovea.tajin.json.jsonpath;

import com.ovea.tajin.json.JSON;
import com.ovea.tajin.json.JSONArray;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: kallestenflo
 * Date: 2/29/12
 * Time: 8:42 AM
 */
public class IssuesTest {
    @Test
    public void issue_7() throws Exception {

        String json = "{ \"foo\" : [\n" +
                "  { \"id\": 1 },  \n" +
                "  { \"id\": 2 },  \n" +
                "  { \"id\": 3 }\n" +
                "  ] }";


        assertNull(JSON.expr(json, "$.foo.id"));
    }
    
    @Test
    public void issue_11() throws Exception {
        String json = "{ \"foo\" : [] }";
        JSONArray result = JSON.expr(json, "$.foo[?(@.rel= 'item')][0].uri").asArray();

        System.out.println(JSON.compile("$.foo[?(@.rel= 'item')][0].uri").isPathDefinite());
        System.out.println(JSON.compile("$.foo[?(@.rel= 'item')][0]").isPathDefinite());
        System.out.println(JSON.compile("$.foo[?(@.rel= 'item')]").isPathDefinite());

        assertTrue(result.isEmpty());
    }

}
