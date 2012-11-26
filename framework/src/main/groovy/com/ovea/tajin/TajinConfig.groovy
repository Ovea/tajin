package com.ovea.tajin

import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 */
class TajinConfig {

    final def cfg

    TajinConfig(json) {
        this.cfg = json
    }

    static TajinConfig read(File file, Map ctx = [:]) {
        if (!file.file || !file.canRead()) {
            throw new IllegalArgumentException('Not a regular readable file: ' + file)
        }
        try {
            return new TajinConfig(new JsonSlurper().parseText(new SimpleTemplateEngine().createTemplate(file).make(ctx + System.getenv() + System.properties) as String))
        } catch (e) {
            throw new IllegalArgumentException('Unable to read Tajin JSON configuration file ' + file + ' : ' + e.message)
        }
    }

}
