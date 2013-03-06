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
import com.ovea.tajin.io.Merger
import com.ovea.tajin.io.Minifier
import com.ovea.tajin.io.Resource

import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_DELETE
import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_MODIFY

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class MergerResourceBuilder implements ResourceBuilder {

    private final TajinConfig config
    private final Map<File, Collection<String>> files = new HashMap<>()

    MergerResourceBuilder(TajinConfig config) {
        this.config = config
        config.onConfig {
            config.log("[Merge] Tajin configuration changed")
            synchronized (files) {
                files.clear()
                (config.merge ?: [:]).each { String m, resources ->
                    resources.findAll { it.file }.collect { new File(config.webapp, it.file as String) }.findAll { !it.directory }.unique().each {
                        if (files[it]) {
                            files[it] << m
                        } else {
                            files[it] = new HashSet<String>([m])
                        }
                    }
                }
            }
        }
    }

    @Override
    Work build() {
        return complete(Work.incomplete(this, (config.merge ?: [:]).keySet()))
    }

    @Override
    Work complete(Work work) {
        Collection<String> incompletes = work.data
        def missing = []
        incompletes.each { if (!merge(it)) missing << it }
        return missing ? Work.incomplete(this, missing) : Work.COMPLETED
    }

    @Override
    Collection<File> getWatchables() { files.keySet() }

    @Override
    boolean modified(FileWatcher.Event e) {
        Collection<File> w = getWatchables()
        if (e.kind in [ENTRY_MODIFY, ENTRY_DELETE] && e.target in w) {
            files[e.target]?.each { merge(it) }
        }
        // do not need a client-json regeneration
        return false
    }

    boolean merge(String m) {
        Collection<Merger.Element> els = ((config.merge ?: [:])[m] ?: []).findAll { it.file || it.url }.collect {
            new Merger.Element(
                location: it.file ?: it.url,
                resource: it.file ? Resource.file(new File(config.webapp, it.file as String)) : Resource.url(new URL(it.url as String)),
                min: it.min ?: false
            )
        }
        def cb = { Merger.Element el, Throwable e ->
            if (e) {
                if (config?.merge?.failOnMissing) {
                    throw new IllegalStateException("File is missing for minification: ${it}")
                } else {
                    config.log('[Merge] ERROR %s: %s', el.location, e.message)
                }
            } else {
                config.log('[Merge] + %s', el.location)
            }
            return true
        }
        config.log('[Merge] %s', Minifier.getFilename(m))
        def out = new File(config.webapp, m)
        def min = Minifier.getFilename(out)
        def complete = Merger.mergeElements(min, els, cb)
        config.log('[Merge] %s', m)
        return complete & Merger.mergeElements(out, els.collect { it.min = false; it }, cb)
    }

}
