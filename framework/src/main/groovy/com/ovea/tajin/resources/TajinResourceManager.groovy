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

    private static final String BUNDLE_FORMAT = '((_([a-z]{2}))|(_([a-z]{2}_[A-Z]{2})))?\\.json'

    final TajinConfig config

    TajinResourceManager(TajinConfig config) {
        this.config = config
    }

    Collection<File> getResources() {
        def watch = []
        i18nBundles.each { bundle, cfg ->
            watch << new File(config.webapp, cfg.location ?: '.').absoluteFile
        }
        return watch
    }

    void buid() {
        config.log("Building Tajin resources...")
        i18nBundles.each { String bundle, cfg ->
            cfg.variants = findVariants(bundle, cfg)
        }
        updateClientConfig()
    }

    void modified(FileWatcher.Event event) {
        config.log("Modified: %s", event)
        if (event.kind in [ENTRY_CREATE, ENTRY_DELETE]) {
            def e = i18nBundles.find { String bundle, cfg -> event.target.name =~ "${bundle}${BUNDLE_FORMAT}" && event.folder == new File(config.webapp, cfg.location ?: '.').absoluteFile }
            if (e) {
                def variants = findVariants(e.key, e.value)
                if (e.value.variants != variants) {
                    e.value.variants = variants
                    updateClientConfig()
                }
            }
        }
    }

    private def findVariants(String bundle, def cfg) {
        File dir = new File(config.webapp, cfg.location ?: '.').absoluteFile
        def variants = []
        dir.eachFile { File f ->
            def matcher = f.name =~ "${bundle}${BUNDLE_FORMAT}"
            if (matcher) {
                if (matcher[0][3]) {
                    variants << matcher[0][3]
                } else if (matcher[0][5]) {
                    variants << matcher[0][5]
                }
            }
        }
        config.log("Variants found for bundle %s: %s", bundle, variants)
        return variants
    }

    private def getI18nBundles() {
        return config.hasClientConfig() ? (config.clientConfig?.i18n?.bundles ?: [:]) : [:]
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
