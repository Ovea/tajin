package com.ovea.tajin.json.jsonpath;

import com.ovea.tajin.json.JSON;
import com.ovea.tajin.json.JSONArray;
import com.ovea.tajin.json.JSONExpr;
import com.ovea.tajin.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: kallestenflo
 * Date: 2/2/11
 * Time: 3:07 PM
 */
public class JsonPathTest {

    public final static JSONArray ARRAY = JSON.parse("[{\"value\": 1},{\"value\": 2}, {\"value\": 3},{\"value\": 4}]").asArray();

    public final static JSONObject DOCUMENT = JSON.parse(
        "{ \"store\": {\n" +
            "    \"book\": [ \n" +
            "      { \"category\": \"reference\",\n" +
            "        \"author\": \"Nigel Rees\",\n" +
            "        \"title\": \"Sayings of the Century\",\n" +
            "        \"display-price\": 8.95\n" +
            "      },\n" +
            "      { \"category\": \"fiction\",\n" +
            "        \"author\": \"Evelyn Waugh\",\n" +
            "        \"title\": \"Sword of Honour\",\n" +
            "        \"display-price\": 12.99\n" +
            "      },\n" +
            "      { \"category\": \"fiction\",\n" +
            "        \"author\": \"Herman Melville\",\n" +
            "        \"title\": \"Moby Dick\",\n" +
            "        \"isbn\": \"0-553-21311-3\",\n" +
            "        \"display-price\": 8.99\n" +
            "      },\n" +
            "      { \"category\": \"fiction\",\n" +
            "        \"author\": \"J. R. R. Tolkien\",\n" +
            "        \"title\": \"The Lord of the Rings\",\n" +
            "        \"isbn\": \"0-395-19395-8\",\n" +
            "        \"display-price\": 22.99\n" +
            "      }\n" +
            "    ],\n" +
            "    \"bicycle\": {\n" +
            "      \"color\": \"red\",\n" +
            "      \"display-price\": 19.95,\n" +
            "      \"foo:bar\": \"fooBar\",\n" +
            "      \"dot.notation\": \"new\",\n" +
            "      \"dash-notation\": \"dashes\"\n" +
            "    }\n" +
            "  }\n" +
            "}").asObject();


    private final static JSONObject PRODUCT_JSON = JSON.parse("{\n" +
        "\t\"product\": [ {\n" +
        "\t    \"version\": \"A\", \n" +
        "\t    \"codename\": \"Seattle\", \n" +
        "\t    \"attr.with.dot\": \"A\"\n" +
        "\t},\n" +
        "\t{\n" +
        "\t    \"version\": \"4.0\", \n" +
        "\t    \"codename\": \"Montreal\", \n" +
        "\t    \"attr.with.dot\": \"B\"\n" +
        "\t}]\n" +
        "}").asObject();

    private final static JSONArray ARRAY_EXPAND = JSON.parse("[{\"parent\": \"ONE\", \"child\": {\"name\": \"NAME_ONE\"}}, [{\"parent\": \"TWO\", \"child\": {\"name\": \"NAME_TWO\"}}]]").asArray();


    @Test
    public void array_start_expands() throws Exception {
        //assertThat(JsonPath.read(ARRAY_EXPAND, "$[?(@.parent = 'ONE')].child.name"), hasItems("NAME_ONE"));
        assertThat(ARRAY_EXPAND.expr("$[?(@['parent'] == 'ONE')].child.name"), M.hasItems("NAME_ONE"));
    }

    @Test
    public void bracket_notation_can_be_used_in_path() throws Exception {

        //System.out.println(ScriptEngineJsonPath.eval(DOCUMENT, "$.['store'].['bicycle'].['dot.notation']"));
        System.out.println(ScriptEngineJsonPath.eval(DOCUMENT.toString(), "$.store.bicycle.['dot.notation']"));


        assertEquals("new", DOCUMENT.expr("$.['store'].bicycle.['dot.notation']").asString());
        assertEquals("new", DOCUMENT.expr("$['store']['bicycle']['dot.notation']").asString());
        assertEquals("new", DOCUMENT.expr("$.['store']['bicycle']['dot.notation']").asString());
        assertEquals("new", DOCUMENT.expr("$.['store'].['bicycle'].['dot.notation']").asString());

        System.out.println(ScriptEngineJsonPath.eval(DOCUMENT.toString(), "$.store.bicycle.['dash-notation']"));

        assertEquals("dashes", DOCUMENT.expr("$.['store'].bicycle.['dash-notation']").asString());
        assertEquals("dashes", DOCUMENT.expr("$['store']['bicycle']['dash-notation']").asString());
        assertEquals("dashes", DOCUMENT.expr("$.['store']['bicycle']['dash-notation']").asString());
        assertEquals("dashes", DOCUMENT.expr("$.['store'].['bicycle'].['dash-notation']").asString());
    }

    @Test
    public void filter_an_array() throws Exception {
        JSONArray matches = JSON.expr(ARRAY, "$.[?(@.value == 1)]").asArray();
        assertEquals(1, matches.size());
        System.out.println(matches);
    }

    @Test
    public void filter_an_array_on_index() throws Exception {
        Integer matches = JSON.expr(ARRAY, "$.[1].value").asInt();
        assertEquals(new Integer(2), matches);
        System.out.println(matches);
    }

    @Test
    public void read_path_with_colon() throws Exception {
        assertEquals(DOCUMENT.expr("$.store.bicycle.foo:bar").asString(), "fooBar");
        assertEquals(DOCUMENT.expr("$['store']['bicycle']['foo:bar']").asString(), "fooBar");
    }

    @Test
    public void read_document_from_root() throws Exception {
        JSONObject result = DOCUMENT.expr("$.store").asObject();
        assertEquals(2, result.values().size());
    }

    @Test
    public void read_store_book_1() throws Exception {
        JSONExpr expr = JSON.compile("$.store.book[1]");
        JSONObject map = expr.expr(DOCUMENT).asObject();
        assertEquals("Evelyn Waugh", map.get("author").asString());
    }

    @Test
    public void read_store_book_wildcard() throws Exception {
        JSONExpr expr = JSON.compile("$.store.book[*]");
        JSONArray list = expr.expr(DOCUMENT).asArray();
    }

    @Test
    public void read_store_book_author() throws Exception {
        assertThat(DOCUMENT.expr("$.store.book[0,1].author"), M.hasItems("Nigel Rees", "Evelyn Waugh"));
        assertThat(DOCUMENT.expr("$.store.book[*].author"), M.hasItems("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
        assertThat(DOCUMENT.expr("$.['store'].['book'][*].['author']"), M.hasItems("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
        assertThat(DOCUMENT.expr("$['store']['book'][*]['author']"), M.hasItems("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
        assertThat(DOCUMENT.expr("$['store'].book[*]['author']"), M.hasItems("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
    }

    @Test
    public void all_authors() throws Exception {
        assertThat(DOCUMENT.expr("$..author"), M.hasItems("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"));
    }


    @Test
    public void all_store_properties() throws Exception {
        JSONArray itemsInStore = DOCUMENT.expr("$.store.*").asArray();

        assertEquals(JSON.expr(itemsInStore, "$.[0].[0].author").asString(), "Nigel Rees");
        assertEquals(JSON.expr(itemsInStore, "$.[0][0].author").asString(), "Nigel Rees");
    }

    @Test
    public void all_prices_in_store() throws Exception {

        assertThat(DOCUMENT.expr("$.store..['display-price']"), M.hasItems(8.95D, 12.99D, 8.99D, 19.95D));

    }

    @Test
    public void access_array_by_index_from_tail() throws Exception {

        assertThat(DOCUMENT.expr("$..book[(@.length-1)].author"), M.equalTo("J. R. R. Tolkien"));
        assertThat(DOCUMENT.expr("$..book[-1:].author"), M.equalTo("J. R. R. Tolkien"));
    }

    @Test
    public void read_store_book_index_0_and_1() throws Exception {

        assertThat(DOCUMENT.expr("$.store.book[0,1].author"), M.hasItems("Nigel Rees", "Evelyn Waugh"));
        assertTrue(DOCUMENT.expr("$.store.book[0,1].author").asArray().size() == 2);
    }

    @Test
    public void read_store_book_pull_first_2() throws Exception {

        assertThat(DOCUMENT.expr("$.store.book[:2].author"), M.hasItems("Nigel Rees", "Evelyn Waugh"));
        assertTrue(DOCUMENT.expr("$.store.book[:2].author").asArray().size() == 2);
    }


    @Test
    public void read_store_book_filter_by_isbn() throws Exception {

        assertThat(DOCUMENT.expr("$.store.book[?(@.isbn)].isbn"), M.hasItems("0-553-21311-3", "0-395-19395-8"));
        assertTrue(DOCUMENT.expr("$.store.book[?(@.isbn)].isbn").asArray().size() == 2);
        assertTrue(DOCUMENT.expr("$.store.book[?(@['isbn'])].isbn").asArray().size() == 2);
    }

    @Test
    public void all_books_cheaper_than_10() throws Exception {


        assertThat(DOCUMENT.expr("$..book[?(@['display-price'] < 10)].title"), M.hasItems("Sayings of the Century", "Moby Dick"));
        assertThat(DOCUMENT.expr("$..book[?(@.display-price < 10)].title"), M.hasItems("Sayings of the Century", "Moby Dick"));

    }

    @Test
    public void all_books() throws Exception {
        //List<String> books = JsonPath.read(DOCUMENT, "$..book");
        Object books = DOCUMENT.expr("$..book");
        System.out.println("test");
    }

    @Test
    public void dot_in_predicate_works() throws Exception {

        assertThat(JSON.expr(PRODUCT_JSON, "$.product[?(@.version=='4.0')].codename"), M.hasItems("Montreal"));

    }

    @Test
    public void dots_in_predicate_works() throws Exception {

        assertThat(JSON.expr(PRODUCT_JSON, "$.product[?(@.attr.with.dot=='A')].codename"), M.hasItems("Seattle"));

    }

    @Test
    public void all_books_with_category_reference() throws Exception {

        assertThat(DOCUMENT.expr("$..book[?(@.category=='reference')].title"), M.hasItems("Sayings of the Century"));
        assertThat(DOCUMENT.expr("$.store.book[?(@.category=='reference')].title"), M.hasItems("Sayings of the Century"));

    }

    @Test
    public void all_members_of_all_documents() throws Exception {
        JSONArray all = DOCUMENT.expr("$..*").asArray();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void access_index_out_of_bounds_does_not_throw_exception() throws Exception {
        DOCUMENT.expr("$.store.book[100].author");
    }


}
