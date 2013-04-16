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

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class Yytoken {
    public static final int TYPE_VALUE = 0;//JSON primitive value: string,number,boolean,null
    public static final int TYPE_LEFT_BRACE = 1;
    public static final int TYPE_RIGHT_BRACE = 2;
    public static final int TYPE_LEFT_SQUARE = 3;
    public static final int TYPE_RIGHT_SQUARE = 4;
    public static final int TYPE_COMMA = 5;
    public static final int TYPE_COLON = 6;
    public static final int TYPE_EOF = -1;//end of file

    public int type = 0;
    public Object value = null;

    public Yytoken(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        return String.valueOf(type + "=>|" + value + "|");
    }
}
