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
        config.log('Tajin.build()')
        resourceManager.buid()
    }

    void watch() {
        synchronized (this) {
            config.log('Tajin.watch()')
            if (config.reloadable) {
                config.log('Watching: %s', config.file)
                watcher.watch([config.file], { Event evt ->
                    if (evt.kind == Event.Kind.ENTRY_MODIFY) {
                        try {
                            if (config.modified()) {
                                unwatch()
                                config.reload()
                                build()
                                watch()
                            }
                        } catch (e) {
                            LOGGER.log(Level.SEVERE, 'Error loading JSON configuration ' + config + ' : ' + e.message, e)
                        }
                    }
                })
            }
            def res = resourceManager.resources
            if (res) {
                res.each { config.log('Watching: %s', it) }
            }
            watcher.watch(res, { Event evt ->
                try {
                    resourceManager.modified(evt)
                } catch (e) {
                    LOGGER.log(Level.SEVERE, 'Error processing modified file ' + evt.target + ' : ' + e.message, e)
                }
            })
        }
    }

    void unwatch() {
        synchronized (this) {
            config.log('Tajin.unwatch()')
            if (config.reloadable) {
                watcher.unwatch([config.file])
            }
            watcher.unwatch(resourceManager.resources)
        }
    }

    static Tajin load(File webapp, Resource config, Map<String, ?> ctx = [:]) { new Tajin(new TajinConfig(webapp, config, ctx)) }

}
