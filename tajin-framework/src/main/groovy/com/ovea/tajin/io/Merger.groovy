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
package com.ovea.tajin.io

import groovy.transform.ToString

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class Merger {

    final Collection<Element> elements
    final Closure<Boolean> callback

    Merger(Collection<Element> elements, Closure<Boolean> callback) {
        this.elements = elements
        this.callback = callback
    }

    boolean mergeTo(File out) {
        if (!out.parentFile.exists()) {
            out.parentFile.mkdirs()
        }
        return out.withOutputStream { mergeTo(out.name, it) } as boolean
    }

    boolean mergeTo(String name, OutputStream out) {
        return mergeTo(name, new OutputStreamWriter(out, 'UTF-8'))
    }

    boolean mergeTo(String name, Writer out) {
        StringBuilder sb = new StringBuilder()
        boolean complete = true
        elements.each { Element el ->
            try {
                String data = new String(el.resource.bytes, 'UTF-8')
                if (el.min && name.endsWith('.css')) {
                    data = Minifier.minifyCSS(data)
                } else if (el.min && name.endsWith('.js')) {
                    data = Minifier.minifyJS(el.location, data)
                }
                if (name.endsWith('.css') || name.endsWith('.js')) {
                    sb << "/* ${el.location} */\n" as String
                }
                sb << data
                sb << '\n'
                if (callback.call(el, null) == Boolean.FALSE) return
            } catch (e) {
                complete = false
                if (callback.call(el, e) == Boolean.FALSE) return
            }
        }
        out << sb.toString()
        out.flush()
        return complete
    }

    @ToString(includes = ['location'])
    static class Element {
        String location
        Resource resource
        boolean min
    }
}
