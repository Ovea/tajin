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

import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_DELETE
import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_MODIFY
import static com.ovea.tajin.resources.Work.Status.COMPLETED
import static com.ovea.tajin.resources.Work.Status.INCOMPLETE

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class Merger implements ResourceBuilder {

    final TajinConfig config

    Merger(TajinConfig config) {
        this.config = config
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
    Collection<File> getWatchables() { (config.merge ?: [:]).values().flatten().unique().collect { new File(config.webapp, it) } }

    @Override
    boolean modified(FileWatcher.Event e) {
        if (e.kind in [ENTRY_MODIFY, ENTRY_DELETE]) {
            (config.merge ?: [:]).each { String m, Collection<String> files ->
                if (files.find { e.target == new File(config.webapp, it) }) {
                    merge(m)
                }
            }
        }
        // do not need a client-json regeneration
        return false
    }

    boolean merge(String m) {
        Collection<File> files = ((config.merge ?: [:])[m] ?: [:]).collect { new File(config.webapp, it as String) }
        if (files) {
            boolean complete = true
            File big = new File(config.webapp, m)
            Class<?> c = getClass()
            big.withWriter { w ->
                files.each { File f ->
                    if (f.exists()) {
                        f.withReader {
                            w << it
                            w << '\n'
                        }
                        config.log('[%s] + %s', c.simpleName, f.name)
                    } else {
                        config.log('[%s] File not found: %s', c.simpleName, f)
                        complete = false
                    }
                }
            }
            config.log('[%s] %s %s', getClass().simpleName, complete ? COMPLETED : INCOMPLETE, big.name)
            return complete
        }
        return false
    }
}
