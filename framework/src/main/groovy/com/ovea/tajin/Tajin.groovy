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

import com.ovea.tajin.io.FileWatcher
import com.ovea.tajin.io.FileWatcher.Event
import com.ovea.tajin.io.Resource
import com.ovea.tajin.resources.TajinResourceManager

import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-01-10
 */
class Tajin {

    static final String DEFAULT_CONFIG_LOCATION = 'WEB-INF/tajin.json'

    private static final Logger LOGGER = Logger.getLogger(TajinConfig.name)

    private final FileWatcher watcher = new FileWatcher()
    private final TajinConfig config
    private final TajinResourceManager resourceManager

    private Tajin(TajinConfig config) {
        this.config = config
        this.resourceManager = new TajinResourceManager(config)
        try {
            this.config.reload()
        } catch (e) {
            throw new IllegalStateException('Error loading JSON configuration ' + config + ' : ' + e.message, e)
        }
    }

    void build() {
        resourceManager.buid()
    }

    void watch() {
        synchronized (this) {
            if (config.reloadable) {
                watcher.watch([config.file], { Event evt ->
                    // executed in watcher thread !
                    if (evt.type == 'ENTRY_MODIFY') {
                        try {
                            if (config.reload()) {
                                build()
                            }
                        } catch (e) {
                            LOGGER.log(Level.SEVERE, 'Error loading JSON configuration ' + config + ' : ' + e.message)
                        }
                    }
                })
            }
            watcher.watch(resourceManager.resources, { Event evt ->
                if (evt.type == 'ENTRY_MODIFY' || evt.type == 'ENTRY_CREATE') {
                    resourceManager.buid(evt.target)
                }
            })
        }
    }

    void unwatch() {
        synchronized (this) {
            if (config.reloadable) {
                watcher.unwatch([config.file])
            }
            watcher.unwatch(resourceManager.resources)
        }
    }

    static Tajin load(File webapp, Resource config, Map<String, ?> ctx = [:]) {
        return new Tajin(new TajinConfig(webapp, config, ctx))
    }

}
