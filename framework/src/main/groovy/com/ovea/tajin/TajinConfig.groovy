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

import com.ovea.tajin.io.Resource
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine

import java.nio.file.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 */
class TajinConfig {

    static final String DEFAULT_LOCATION = 'WEB-INF/tajin.json'

    private static final Logger LOGGER = Logger.getLogger(TajinConfig.name)

    private final List<Closure<?>> listeners = new CopyOnWriteArrayList<>()
    private final AtomicBoolean watching = new AtomicBoolean(false)
    private final AtomicReference<Object> cfg = new AtomicReference<>(new JsonSlurper().parseText("{}"))
    private Thread watcher
    private Map<String, ?> context

    final Resource config
    final File webapp
    final boolean reloadable

    TajinConfig(File webapp, Resource config, Map<String, ?> ctx = [:]) {
        if (!config.exist) {
            throw new IllegalArgumentException('Not a regular readable file: ' + config)
        }
        if (!webapp.exists()) {
            throw new IllegalArgumentException('Not a folder: ' + webapp)
        }
        this.config = config
        this.webapp = webapp
        this.reloadable = config.file
        this.context = ctx
        loadConfig()
    }

    private boolean loadConfig(silent = false) {
        def old = cfg.get().toString()
        def conf
        try {
            conf = new JsonSlurper().parseText(new SimpleTemplateEngine().createTemplate(config.text).make(context + System.getenv() + System.properties + [web: webapp.canonicalPath]) as String)
        } catch (e) {
            conf = null
            if (silent) {
                LOGGER.log(Level.SEVERE, 'Error in JSON configuration at ' + conf + ' : ' + e.message)
            } else {
                throw new IllegalArgumentException('Unable to read Tajin JSON configuration file ' + config + ' : ' + e.message)
            }
        }
        if (conf != null && conf.toString() != old) {
            cfg.set(conf)
            return true
        }
        return false
    }

    void onchange(Closure<?> c) {
        listeners << c
    }

    void watch() {
        if (reloadable && !watching.getAndSet(true)) {
            WatchService watchService = FileSystems.default.newWatchService()
            String filename = config.asFile.name
            Paths.get(config.asFile.parent).register(watchService, ENTRY_CREATE, ENTRY_MODIFY)
            TajinConfig tajinConfig = this
            Thread.start 'TajinConfig-Reloader', {
                while (watching.get() && !Thread.currentThread().interrupted) {
                    WatchKey key = watchService.take()
                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                        Path file = (Path) watchEvent.context()
                        if (filename == file.toFile().name) {
                            if (loadConfig(true)) {
                                listeners.each { it.call(tajinConfig) }
                            }
                        }
                    }
                    key.reset()
                }
            }
        }
    }

    void unwatch() {
        watching.getAndSet(false)
        watcher?.interrupt()
        watcher = null
    }

    @Override
    public String toString() {
        return "TajinConfig{webapp=" + webapp + ", config=" + config + '}'
    }
}
