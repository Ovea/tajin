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

    static boolean mergeElements(File out, Collection<Element> elements, Closure<Boolean> callback) {
        StringBuilder sb = new StringBuilder()
        boolean complete = true
        elements.each { Element el ->
            try {
                String data = new String(el.resource.bytes, 'UTF-8')
                if (el.min && out.name.endsWith('.css')) {
                    data = Minifier.minifyCSS(data)
                } else if (el.min && out.name.endsWith('.js')) {
                    data = Minifier.minifyJS(el.location, data)
                }
                if (out.name.endsWith('.css') || out.name.endsWith('.js')) {
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
        if (out.parentFile.exists()) {
            out.parentFile.mkdirs()
        }
        out.bytes = sb.toString().getBytes('UTF-8')
        return complete
    }

    static boolean mergeElements(File out, Collection<Element> elements) {
        return mergeElements(out, elements, { true })
    }

    static boolean mergeResources(File out, Collection<Resource> resources) {
        return mergeElements(out, resources.collect {
            new Element(
                resource: it,
                min: false
            )
        })
    }

    static boolean mergeFiles(File out, Collection<File> files) {
        return mergeResources(out, files.collect { Resource.file(it) })
    }

    @ToString(includes = ['location'])
    static class Element {
        String location
        Resource resource
        boolean min
    }
}
