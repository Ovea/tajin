package com.ovea.tajin.console

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
class Json {
    static String stringify(o, boolean pretty = false) {
        if (o instanceof JsonBuilder) return pretty ? ((JsonBuilder) o).toPrettyString() : ((JsonBuilder) o).toString()
        return stringify(new JsonBuilder(o), pretty)
    }

    static def parse(String s) { new JsonSlurper().parseText(s) }
}
