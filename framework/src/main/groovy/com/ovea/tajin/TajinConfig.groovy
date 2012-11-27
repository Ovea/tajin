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

    static TajinConfig read(File file, Map<String, ?> ctx = [:]) {
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
