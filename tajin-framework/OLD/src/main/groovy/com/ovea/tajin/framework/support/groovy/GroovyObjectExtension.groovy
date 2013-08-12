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
package com.ovea.tajin.framework.support.groovy
/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-05-31
 */
class GroovyObjectExtension {

    static <T> T deepClone(T t) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ObjectOutputStream oos = new ObjectOutputStream(bos)
        oos.writeObject(t)
        oos.flush()
        ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray())
        ObjectInputStream ois = new ObjectInputStream(bin)
        return ois.readObject() as T
    }

    public static Map merge(Map to, Map from) {
        Map r = new LinkedHashMap(to)
        from.each { k, v ->
            if (r[k] == null) {
                r[k] = v
            } else if (r[k] instanceof Map && v instanceof Map) {
                r[k] = merge(r[k] as Map, v as Map)
            }
        }
        return r
    }

}
