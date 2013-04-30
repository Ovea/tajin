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
import org.apache.tools.ant.DirectoryScanner

import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_DELETE
import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_MODIFY

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class TJSResourceBuilder implements ResourceBuilder {

    private final TajinConfig config
    private final Collection<File> libs = new TreeSet<>()
    private final ReadWriteLock lock = new ReentrantReadWriteLock()

    TJSResourceBuilder(TajinConfig config) {
        this.config = config
        config.onConfig {
            config.log("[TJS] Tajin configuration changed")
            lock.writeLock().lock()
            try {
                DirectoryScanner scanner = new DirectoryScanner(
                    basedir: config.webapp,
                    includes: '**/*.t.js'
                )
                scanner.scan()
                libs.clear()
                libs.addAll(scanner.includedFiles.collect { new File(config.webapp, it) })
            }
            finally {
                lock.writeLock().unlock()
            }
        }
    }

    @Override
    Work build() {
        return complete(Work.incomplete(this, watchables))
    }

    @Override
    Work complete(Work work) {
        Collection<String> incompletes = work.data
        def missing = []
        incompletes.each { if (!merge(it)) missing << it }
        return missing ? Work.incomplete(this, missing) : Work.COMPLETED
    }

    @Override
    Collection<File> getWatchables() {
        lock.readLock().lock()
        try {
            return new HashSet<File>(libs)
        } finally {
            lock.readLock().unlock()
        }
    }

    @Override
    boolean modified(FileWatcher.Event e) {
        if (e.kind in [ENTRY_MODIFY, ENTRY_DELETE] && e.target in watchables) {
            build(e.target)
        }
        // do not need a client-json regeneration
        return false
    }

    boolean build(File lib) {
        if (!lib.exists()) {

        }
    }

}
