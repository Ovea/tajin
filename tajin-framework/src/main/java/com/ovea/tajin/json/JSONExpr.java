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
package com.ovea.tajin.json;


import com.ovea.tajin.json.internal.IOUtils;
import com.ovea.tajin.json.internal.PathToken;
import com.ovea.tajin.json.internal.PathTokenizer;
import com.ovea.tajin.json.internal.filter.PathTokenFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.ovea.tajin.util.Assert.isTrue;
import static com.ovea.tajin.util.Assert.notEmpty;
import static com.ovea.tajin.util.Assert.notNull;
import static java.util.Arrays.asList;

/**
 * <p/>
 * JsonPath is to JSON what XPATH is to XML, a simple way to extract parts of a given document. JsonPath is
 * available in many programming languages such as Javascript, Python and PHP.
 * <p/>
 * JsonPath allows you to compile a json path string to use it many times or to compile and apply in one
 * single on demand operation.
 * <p/>
 * Given the Json document:
 * <p/>
 * <code>
 * String json =
 * "{
 * "store":
 * {
 * "book":
 * [
 * {
 * "category": "reference",
 * "author": "Nigel Rees",
 * "title": "Sayings of the Century",
 * "price": 8.95
 * },
 * {
 * "category": "fiction",
 * "author": "Evelyn Waugh",
 * "title": "Sword of Honour",
 * "price": 12.99
 * }
 * ],
 * "bicycle":
 * {
 * "color": "red",
 * "price": 19.95
 * }
 * }
 * }";
 * </code>
 * <p/>
 * A JsonPath can be compiled and used as shown:
 * <p/>
 * <code>
 * JsonPath path = JsonPath.compile("$.store.book[1]");
 * <br/>
 * List&lt;Object&gt; books = path.read(json);
 * </code>
 * </p>
 * Or:
 * <p/>
 * <code>
 * List&lt;Object&gt; authors = JsonPath.read(json, "$.store.book[*].author")
 * </code>
 * <p/>
 * If the json path returns a single value (is definite):
 * </p>
 * <code>
 * String author = JsonPath.read(json, "$.store.book[1].author")
 * </code>
 *
 * @author Kalle Stenflo
 */
public final class JSONExpr {

    private static Pattern DEFINITE_PATH_PATTERN = Pattern.compile(".*(\\.\\.|\\*|\\[[\\\\/]|\\?|,|:\\s?\\]|\\[\\s?:|>|\\(|<|=|\\+).*");

    private PathTokenizer tokenizer;
    private LinkedList<Filter> filters;

    private JSONExpr(String jsonPath, Filter[] filters) {
        if (jsonPath == null ||
            jsonPath.trim().length() == 0 ||
            jsonPath.matches("[^\\?\\+\\=\\-\\*\\/\\!]\\(")) {

            throw new InvalidPathException("Invalid path");
        }

        int filterCountInPath = countMatches(jsonPath, "[?]");
        isTrue(filterCountInPath == filters.length, "Filters in path ([?]) does not match provided filters.");

        this.tokenizer = new PathTokenizer(jsonPath);
        this.filters = new LinkedList<>();
        this.filters.addAll(asList(filters));

    }

    PathTokenizer getTokenizer() {
        return this.tokenizer;
    }


    public JSONExpr copy() {
        return new JSONExpr(tokenizer.getPath(), filters.toArray(new Filter[filters.size()]));
    }


    /**
     * Returns the string representation of this JsonPath
     *
     * @return path as String
     */
    public String getPath() {
        return this.tokenizer.getPath();
    }

    /**
     * Checks if a path points to a single item or if it potentially returns multiple items
     * <p/>
     * a path is considered <strong>not</strong> definite if it contains a scan fragment ".."
     * or an array position fragment that is not based on a single index
     * <p/>
     * <p/>
     * definite path examples are:
     * <p/>
     * $store.book
     * $store.book[1].title
     * <p/>
     * not definite path examples are:
     * <p/>
     * $..book
     * $.store.book[1,2]
     * $.store.book[?(@.category = 'fiction')]
     *
     * @return true if path is definite (points to single item)
     */
    public boolean isPathDefinite() {
        String preparedPath = getPath().replaceAll("\"[^\"\\\\\\n\r]*\"", "");

        return !DEFINITE_PATH_PATTERN.matcher(preparedPath).matches();
    }

    /**
     * Applies this JsonPath to the provided json document.
     * Note that the document must either a {@link List} or a {@link Map}
     *
     * @param jsonObject a container Object ({@link List} or {@link Map})
     * @return list of objects matched by the given path
     */
    @SuppressWarnings({"unchecked"})
    public JSONType expr(JSONType jsonObject) {
        notNull(jsonObject, "json can not be null");

        if (!jsonObject.isContainer()) {
            throw new IllegalArgumentException("Invalid container object");
        }
        LinkedList<Filter> contextFilters = new LinkedList<>(filters);

        JSONType result = jsonObject;

        boolean inArrayContext = false;

        for (PathToken pathToken : tokenizer) {
            PathTokenFilter filter = pathToken.getFilter();
            result = filter.filter(result, contextFilters, inArrayContext);
            if (!inArrayContext) {
                inArrayContext = filter.isArrayFilter();
            }
        }
        return result;
    }

    /**
     * Applies this JsonPath to the provided json string
     *
     * @param json a json string
     * @return list of objects matched by the given path
     */
    @SuppressWarnings({"unchecked"})
    public JSONType expr(String json) {
        notEmpty(json, "json can not be null or empty");
        return expr(JSON.parse(json));
    }

    /**
     * Applies this JsonPath to the provided json file
     *
     * @param jsonFile file to read from
     * @return list of objects matched by the given path
     * @throws IOException
     */
    @SuppressWarnings({"unchecked"})
    public JSONType expr(File jsonFile) {
        notNull(jsonFile, "json file can not be null");
        isTrue(jsonFile.exists(), "json file does not exist");

        FileReader fis = null;
        try {
            fis = new FileReader(jsonFile);
            return expr(JSON.parse(fis));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    @SuppressWarnings({"unchecked"})
    public JSONType expr(Reader reader) {
        notNull(reader, "json input stream can not be null");
        try {
            return expr(JSON.parse(reader));
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    // --------------------------------------------------------
    //
    // Static factory methods
    //
    // --------------------------------------------------------

    /**
     * Compiles a JsonPath
     *
     * @param jsonPath to compile
     * @param filters  filters to be applied to the filter place holders  [?] in the path
     * @return compiled JsonPath
     */
    static JSONExpr compile(String jsonPath, Filter... filters) {
        notEmpty(jsonPath, "json can not be null or empty");

        return new JSONExpr(jsonPath, filters);
    }


    // --------------------------------------------------------
    //
    // Static utility functions
    //
    // --------------------------------------------------------

    /**
     * Creates a new JsonPath and applies it to the provided Json string
     *
     * @param json     a json string
     * @param jsonPath the json path
     * @param filters  filters to be applied to the filter place holders  [?] in the path
     * @return list of objects matched by the given path
     */
    @SuppressWarnings({"unchecked"})
    static JSONType expr(String json, String jsonPath, Filter... filters) {
        notEmpty(json, "json can not be null or empty");
        notEmpty(jsonPath, "jsonPath can not be null or empty");

        return compile(jsonPath, filters).expr(json);
    }

    /**
     * Creates a new JsonPath and applies it to the provided Json object
     *
     * @param json     a json object
     * @param jsonPath the json path
     * @param filters  filters to be applied to the filter place holders  [?] in the path
     * @return list of objects matched by the given path
     */
    @SuppressWarnings({"unchecked"})
    static JSONType expr(JSONType json, String jsonPath, Filter... filters) {
        notNull(json, "json can not be null");
        notNull(jsonPath, "jsonPath can not be null");

        return compile(jsonPath, filters).expr(json);
    }

    /**
     * Creates a new JsonPath and applies it to the provided Json object
     *
     * @param jsonFile json file
     * @param jsonPath the json path
     * @param filters  filters to be applied to the filter place holders  [?] in the path
     * @return list of objects matched by the given path
     */
    @SuppressWarnings({"unchecked"})
    static JSONType expr(File jsonFile, String jsonPath, Filter... filters) {
        notNull(jsonFile, "json file can not be null");
        notEmpty(jsonPath, "jsonPath can not be null or empty");
        return compile(jsonPath, filters).expr(jsonFile);
    }

    /**
     * Creates a new JsonPath and applies it to the provided Json object
     *
     * @param jsonInputStream json input stream
     * @param jsonPath        the json path
     * @param filters         filters to be applied to the filter place holders  [?] in the path
     * @return list of objects matched by the given path
     */
    @SuppressWarnings({"unchecked"})
    static JSONType expr(Reader jsonInputStream, String jsonPath, Filter... filters) {
        notNull(jsonInputStream, "json input stream can not be null");
        notEmpty(jsonPath, "jsonPath can not be null or empty");
        return compile(jsonPath, filters).expr(jsonInputStream);
    }

    /**
     * <p>Counts how many times the substring appears in the larger String.</p>
     * <p/>
     * <p>A <code>null</code> or empty ("") String input returns <code>0</code>.</p>
     * <p/>
     * <pre>
     * StringUtils.countMatches(null, *)       = 0
     * StringUtils.countMatches("", *)         = 0
     * StringUtils.countMatches("abba", null)  = 0
     * StringUtils.countMatches("abba", "")    = 0
     * StringUtils.countMatches("abba", "a")   = 2
     * StringUtils.countMatches("abba", "ab")  = 1
     * StringUtils.countMatches("abba", "xxx") = 0
     * </pre>
     *
     * @param str the String to check, may be null
     * @param sub the substring to count, may be null
     * @return the number of occurrences, 0 if either String is <code>null</code>
     */
    public static int countMatches(String str, String sub) {
        if (isEmpty(str) || isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
