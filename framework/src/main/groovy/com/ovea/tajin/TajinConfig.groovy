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
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine

import java.util.concurrent.atomic.AtomicReference

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 */
class TajinConfig {

    private final AtomicReference<Object> cfg = new AtomicReference<>(new JsonSlurper().parseText("{}"))
    private final AtomicReference<String> cfgStr = new AtomicReference<>("{}")
    private Map<String, ?> context
    private final Resource config

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
        try {
            reload()
        } catch (e) {
            throw new IllegalArgumentException("Error in configuration ${config} for webapp ${webapp.absolutePath} : ${e.message}", e)
        }
    }

    File getFile() { config.asFile }

    @Override
    String toString() { "TajinConfig{webapp=" + webapp + ", config=" + config + '}' }

    boolean modified() { readCfg().modified }

    boolean reload() {
        def c = readCfg()
        if (c.modified && cfgStr.compareAndSet(c.oldCfgStr, c.newCfgStr)) {
            cfg.set(c.newCfg)
            log('Loaded Tajin configuration: %s', config)
            return true
        }
        return false
    }

    private def readCfg() {
        def newCfg = new JsonSlurper().parseText(new SimpleTemplateEngine().createTemplate(config.text).make(context + System.getenv() + System.properties + [web: webapp.canonicalPath]) as String)
        def newCfgStr = new JsonBuilder(newCfg).toString()
        def oldCfgStr = cfgStr.get()
        return [
            newCfg: newCfg,
            newCfgStr: newCfgStr,
            oldCfgStr: oldCfgStr,
            modified: newCfgStr != oldCfgStr
        ]
    }

    def getClientConfig() { cfg.get().client.modules ?: [:] }

    File getClientFile() { new File(webapp, cfg.get().client.output as String ?: 'tajin-client.json') }

    boolean hasClientConfig() { cfg.get()?.client }

    boolean getDebug() { cfg.get().debug ?: false }

    boolean getTrace() { cfg.get().trace ?: false }

    void log(String msg, Object... args) {
        if (debug) {
            println "[tajin] ${String.format(msg, args)}"
        }
    }

    void trace(String msg, Object... args) {
        if (trace) {
            println "[tajin] ${String.format(msg, args)}"
        }
    }

    def propertyMissing(String name) { cfg.get()[name] ?: [:] }
}
