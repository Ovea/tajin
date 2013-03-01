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
package com.ovea.tajin.resources

import com.ovea.tajin.TajinConfig
import com.ovea.tajin.io.FileWatcher
import groovy.json.JsonBuilder

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 */
class TajinResourceManager {

    final TajinConfig config

    private final Collection<ResourceBuilder> resourceBuilders

    TajinResourceManager(TajinConfig config) {
        this.config = config
        resourceBuilders = [
            new I18N(config),
            new Minifier(config),
            new Merger(config)
        ]
    }

    Collection<File> getResources() { resourceBuilders.collect { it.watchables }.flatten().unique().sort() }

    void buid() {
        int n = 1
        int c = 0
        config.log("Building Tajin resources: pass %s...", n++)
        def works = resourceBuilders*.build()
        def incompletes = works.findAll { it.incomplete }
        while (c != incompletes.size()) {
            // keep old size
            c = incompletes.size()
            // rebuild
            config.log("Building Tajin resources: pass %s...", n++)
            works = incompletes*.complete()
            // get incompletes
            incompletes = works.findAll { it.incomplete }
        }
        if (incompletes) {
            config.log("Incomplete Tajin build: %s", incompletes)
        }
        updateClientConfig()
    }

    void modified(FileWatcher.Event event) {
        config.log("%s %s", event.kind, event.target.name)
        boolean modified = false
        resourceBuilders.each { modified |= it.modified(event) }
        if (modified) {
            updateClientConfig()
        }
    }

    private void updateClientConfig() {
        if (config.hasClientConfig()) {
            config.clientFile.bytes = new JsonBuilder(config.clientConfig).toString().getBytes('UTF-8')
            config.log("Wrote client config %s", config.clientFile)
        } else {
            config.clientFile.delete()
            config.log("Removed client config %s", config.clientFile)
        }
    }
}
