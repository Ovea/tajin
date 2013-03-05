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

import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_CREATE
import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_DELETE

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-18
 */
class I18NResourceBuilder implements ResourceBuilder {

    private static final String BUNDLE_FORMAT = '((_([a-z]{2}))|(_([a-z]{2}_[A-Z]{2})))?\\.json'

    private final TajinConfig config
    private final Collection<File> folders = new HashSet<>()
    private final ReadWriteLock lock = new ReentrantReadWriteLock()

    I18NResourceBuilder(TajinConfig config) {
        this.config = config
        Class<?> c = getClass()
        config.onConfig {
            config.log("[%s] Tajin configuration changed", c.simpleName)
            lock.writeLock().lock()
            try {
                folders.clear()
                folders.addAll(bundles.collect { bundle, cfg -> new File(config.webapp, cfg.location ?: '.') }.findAll { it && it.exists() }.collect {it.absoluteFile})
            } finally {
                lock.writeLock().unlock()
            }
        }
    }

    @Override
    synchronized Collection<File> getWatchables() {
        lock.readLock().lock()
        try {
            return new HashSet<File>(folders)
        } finally {
            lock.readLock().unlock()
        }
    }

    @Override
    Work build() {
        bundles.each { String bundle, cfg ->
            if (!cfg.variants) {
                cfg.variants = findVariants(bundle, cfg)
            }
        }
        return Work.COMPLETED
    }

    @Override
    Work complete(Work work) {
        return Work.COMPLETED
    }

    @Override
    boolean modified(FileWatcher.Event event) {
        boolean mustHandle = false
        lock.readLock().lock()
        try {
            mustHandle = event.kind in [ENTRY_DELETE, ENTRY_CREATE] && event.target.parentFile in folders
        } finally {
            lock.readLock().unlock()
        }
        if (mustHandle) {
            def e = bundles.find { String bundle, cfg -> event.target.name =~ "${bundle}${BUNDLE_FORMAT}" }
            if (e) {
                def variants = findVariants(e.key, e.value)
                if (e.value.variants != variants) {
                    e.value.variants = variants
                    return true
                }
            }
        }
        return false
    }

    private def findVariants(String bundle, def cfg) {
        def variants = []
        File dir = new File(config.webapp, cfg.location ?: '.')
        if (dir.exists()) {
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
            config.log("[I18N] Variants found for bundle %s: %s", bundle, variants)
        } else {
            config.log("[I18N] Not a local bundle: %s", bundle)
        }
        return variants
    }

    private def getBundles() { config.hasClientConfig() ? (config.clientConfig?.i18n?.bundles ?: [:]) : [:] }

}
