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
package com.ovea.tajin.framework.json

import com.ovea.tajin.framework.support.jersey.JsonSupport
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-05-14
 */
class Json {
    static String stringify(o, boolean pretty = false) {
        if (o instanceof JsonBuilder) return pretty ? ((JsonBuilder) o).toPrettyString() : ((JsonBuilder) o).toString()
        if (o instanceof JsonSupport) return stringify(new JsonBuilder(((JsonSupport) o).json), pretty)
        return stringify(new JsonBuilder(o), pretty)
    }

    static def parse(String s) { new JsonSlurper().parseText(s) }
}