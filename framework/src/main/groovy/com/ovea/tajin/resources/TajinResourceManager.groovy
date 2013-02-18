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

import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_CREATE
import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_DELETE

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 */
class TajinResourceManager {

    final TajinConfig config

    private final I18N i18n

    TajinResourceManager(TajinConfig config) {
        this.config = config
        this.i18n = new I18N(config)
    }

    Collection<File> getResources() {
        return i18n.watchables
    }

    void buid() {
        config.log("Building Tajin resources...")
        i18n.build()
        updateClientConfig()
    }

    void modified(FileWatcher.Event event) {
        config.log("Modified: %s", event)
        if (event.kind in [ENTRY_CREATE, ENTRY_DELETE] && i18n.modified(event)) {
            updateClientConfig()
        }
    }

    private void updateClientConfig() {
        if (config.hasClientConfig()) {
            config.clientFile.text = new JsonBuilder(config.clientConfig)
            config.log("Wrote client config %s", config.clientFile)
        } else {
            config.clientFile.delete()
            config.log("Removed client config %s", config.clientFile)
        }
    }
}
