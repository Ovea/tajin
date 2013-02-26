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

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class JSONParser {
    public static final int S_INIT = 0;
    public static final int S_IN_FINISHED_VALUE = 1;//string,number,boolean,null,object,array
    public static final int S_IN_OBJECT = 2;
    public static final int S_IN_ARRAY = 3;
    public static final int S_PASSED_PAIR_KEY = 4;
    public static final int S_IN_ERROR = -1;

    private LinkedList<Integer> statusStack = new LinkedList<>();
    private LinkedList<JSONType> valueStack = new LinkedList<>();
    private Yylex lexer = new Yylex((Reader) null);
    private Yytoken token = null;
    private int status = S_INIT;

    private int peekStatus(LinkedList statusStack) {
        if (statusStack.size() == 0)
            return -1;
        return (Integer) statusStack.getFirst();
    }

    private void reset(Reader in) {
        statusStack.clear();
        valueStack.clear();
        lexer.yyreset(in);
        token = null;
        status = S_INIT;
    }

    public JSONType parse(Reader in) {
        reset(in);

        do {
            try {
                token = lexer.yylex();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            if (token == null)
                token = new Yytoken(Yytoken.TYPE_EOF, null);
            switch (status) {
                case S_INIT:
                    switch (token.type) {
                        case Yytoken.TYPE_VALUE:
                            status = S_IN_FINISHED_VALUE;
                            statusStack.addFirst(status);
                            valueStack.addFirst(JSONValue.valueOf(token.value));
                            break;
                        case Yytoken.TYPE_LEFT_BRACE:
                            status = S_IN_OBJECT;
                            statusStack.addFirst(status);
                            valueStack.addFirst(new JSONObject());
                            break;
                        case Yytoken.TYPE_LEFT_SQUARE:
                            status = S_IN_ARRAY;
                            statusStack.addFirst(status);
                            valueStack.addFirst(new JSONArray());
                            break;
                        default:
                            status = S_IN_ERROR;
                    }//inner switch
                    break;

                case S_IN_FINISHED_VALUE:
                    if (token.type == Yytoken.TYPE_EOF)
                        return valueStack.removeFirst();
                    else
                        return null;

                case S_IN_OBJECT:
                    switch (token.type) {
                        case Yytoken.TYPE_COMMA:
                            break;
                        case Yytoken.TYPE_VALUE:
                            if (token.value instanceof String) {
                                String key = (String) token.value;
                                valueStack.addFirst(JSONValue.valueOf(key));
                                status = S_PASSED_PAIR_KEY;
                                statusStack.addFirst(status);
                            } else {
                                status = S_IN_ERROR;
                            }
                            break;
                        case Yytoken.TYPE_RIGHT_BRACE:
                            if (valueStack.size() > 1) {
                                statusStack.removeFirst();
                                valueStack.removeFirst();
                                status = peekStatus(statusStack);
                            } else {
                                status = S_IN_FINISHED_VALUE;
                            }
                            break;
                        default:
                            status = S_IN_ERROR;
                            break;
                    }//inner switch
                    break;

                case S_PASSED_PAIR_KEY:
                    switch (token.type) {
                        case Yytoken.TYPE_COLON:
                            break;
                        case Yytoken.TYPE_VALUE:
                            statusStack.removeFirst();
                            String key = valueStack.removeFirst().asString();
                            JSONObject parent = (JSONObject) valueStack.getFirst();
                            parent.put(key, token.value);
                            status = peekStatus(statusStack);
                            break;
                        case Yytoken.TYPE_LEFT_SQUARE:
                            statusStack.removeFirst();
                            key = valueStack.removeFirst().asString();
                            parent = (JSONObject) valueStack.getFirst();
                            JSONArray newArray = new JSONArray();
                            parent.put(key, newArray);
                            status = S_IN_ARRAY;
                            statusStack.addFirst(status);
                            valueStack.addFirst(newArray);
                            break;
                        case Yytoken.TYPE_LEFT_BRACE:
                            statusStack.removeFirst();
                            key = valueStack.removeFirst().asString();
                            parent = (JSONObject) valueStack.getFirst();
                            JSONObject newObject = new JSONObject();
                            parent.put(key, newObject);
                            status = S_IN_OBJECT;
                            statusStack.addFirst(status);
                            valueStack.addFirst(newObject);
                            break;
                        default:
                            status = S_IN_ERROR;
                    }
                    break;

                case S_IN_ARRAY:
                    switch (token.type) {
                        case Yytoken.TYPE_COMMA:
                            break;
                        case Yytoken.TYPE_VALUE:
                            JSONArray val = (JSONArray) valueStack.getFirst();
                            val.put(token.value);
                            break;
                        case Yytoken.TYPE_RIGHT_SQUARE:
                            if (valueStack.size() > 1) {
                                statusStack.removeFirst();
                                valueStack.removeFirst();
                                status = peekStatus(statusStack);
                            } else {
                                status = S_IN_FINISHED_VALUE;
                            }
                            break;
                        case Yytoken.TYPE_LEFT_BRACE:
                            val = (JSONArray) valueStack.getFirst();
                            JSONObject newObject = new JSONObject();
                            val.put(newObject);
                            status = S_IN_OBJECT;
                            statusStack.addFirst(status);
                            valueStack.addFirst(newObject);
                            break;
                        case Yytoken.TYPE_LEFT_SQUARE:
                            val = (JSONArray) valueStack.getFirst();
                            JSONArray newArray = new JSONArray();
                            val.put(newArray);
                            status = S_IN_ARRAY;
                            statusStack.addFirst(status);
                            valueStack.addFirst(newArray);
                            break;
                        default:
                            status = S_IN_ERROR;
                    }//inner switch
                    break;
                case S_IN_ERROR:
                    return null;
            }//switch
            if (status == S_IN_ERROR)
                return null;
        } while (token.type != Yytoken.TYPE_EOF);
        return null;
    }
}
