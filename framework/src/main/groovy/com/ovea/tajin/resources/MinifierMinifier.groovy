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
import com.ovea.tajin.io.Minifier
import org.apache.tools.ant.DirectoryScanner

import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_DELETE
import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_MODIFY

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class MinifierMinifier implements ResourceBuilder {

    final TajinConfig config
    Collection<File> watchables = []

    MinifierMinifier(TajinConfig config) {
        this.config = config
        Class<?> c = getClass()
        config.onConfig {
            config.log("[%s] Tajin configuration changed", c.simpleName)
            Collection<String> w = new TreeSet((config.minify?.includes ?: []).findAll { String path -> !path.contains('*') })
            DirectoryScanner scanner = new DirectoryScanner(
                basedir: config.webapp,
                includes: (config.minify?.includes ?: []).findAll { String path -> path.contains('*') },
                excludes: config.minify?.excludes
            )
            scanner.scan()
            w.addAll(scanner.includedFiles.collect {
                // skip XXX-min.ext and XXX.min.ext
                int pos = it.lastIndexOf('.')
                if (pos != -1) {
                    String part = it.substring(0, pos)
                    if (!part.endsWith('.min') && !part.endsWith('-min')) {
                        return it
                    }
                }
                return ''
            })
            // get non nulls and tranform to files
            watchables = w.findAll { it }.collect { new File(config.webapp, it) }
        }
    }

    @Override
    Work build() {
        return complete(Work.incomplete(this, watchables))
    }

    @Override
    Work complete(Work work) {
        Collection<File> incompletes = work.data
        def missing = []
        incompletes.each { if (!Minifier.minify(it)) missing << it }
        return missing ? Work.incomplete(this, missing) : Work.COMPLETED
    }

    @Override
    boolean modified(FileWatcher.Event e) {
        if (e.kind in [ENTRY_DELETE, ENTRY_MODIFY] && e.target in watchables) {
            if (e.kind == ENTRY_DELETE) {
                File min = Minifier.getFilename(e.target)
                if (min) {
                    min.delete()
                    config.log('[%s] Removed: %s', getClass().simpleName, min.name)
                }
            } else {
                Minifier.minify(e.target)
            }
        }
        // do not need a client-json regeneration
        return false
    }

}
