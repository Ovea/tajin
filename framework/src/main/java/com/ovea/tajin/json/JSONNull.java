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
import java.io.Writer;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class JSONNull extends JSONType {

    static JSONNull INSTANCE = new JSONNull();

    private JSONNull() {
    }

    @SuppressWarnings({"CloneDoesntCallSuperClone"})
    @Override
    protected final Object clone() {
        return this;
    }

    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
    @Override
    public boolean equals(Object object) {
        return object == null || object == this;
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public String toString(int indentFactor, int indent) throws JSONException {
        return toString();
    }

    @Override
    public Writer write(Writer writer) throws JSONException {
        try {
            //noinspection NullableProblems
            writer.write(Utils.quote(null));
            return writer;
        } catch (IOException e) {
            throw new JSONException(e);
        }
    }
}
