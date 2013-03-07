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

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_DELETE
import static com.ovea.tajin.io.FileWatcher.Event.Kind.ENTRY_MODIFY

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class MergerResourceBuilder implements ResourceBuilder {

    private final TajinConfig config
    private final Map<File, Collection<String>> files = new HashMap<>()
    private final Collection<String> merges = []
    private final ReadWriteLock lock = new ReentrantReadWriteLock()
    private final AtomicBoolean defaultWatch = new AtomicBoolean(true)
    private final AtomicBoolean defaultFailOnMissing = new AtomicBoolean(true)

    MergerResourceBuilder(TajinConfig config) {
        this.config = config
        config.onConfig {
            config.log("[Merge] Tajin configuration changed")
            lock.writeLock().lock()
            try {
                def cfg = config.merge ?: [:]
                defaultFailOnMissing.set(cfg.failOnMissing ?: false)
                defaultWatch.set(cfg.watch ?: false)
                files.clear()
                cfg.each { String m, resources ->
                    if (Collection.isInstance(resources)) {
                        merges << m
                        resources.findAll { it.f && (Boolean.isInstance(it.watch) ? it.watch : defaultWatch.get()) }.collect {
                            Resource dev = Resource.resource(config.webapp, it.f as String)
                            Resource prod = Resource.resource(config.webapp, (String.isInstance(it.min) ? it.min : it.f) as String)
                            return [dev, prod].findAll { Resource r -> r.file }
                        }.flatten().unique().each { Resource r ->
                            File f = r.asFile
                            if (files[f]) {
                                files[f] << m
                            } else {
                                files[f] = new HashSet<String>([m])
                            }
                        }
                    }
                }
            }
            finally {
                lock.writeLock().unlock()
            }
        }
    }

    @Override
    Work build() {
        Collection<String> m = []
        lock.readLock().lock()
        try {
            m.addAll(merges)
        } finally {
            lock.readLock().unlock()
        }
        return complete(Work.incomplete(this, m))
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
            return new HashSet<File>(files.keySet())
        } finally {
            lock.readLock().unlock()
        }
    }

    @Override
    boolean modified(FileWatcher.Event e) {
        if (e.kind in [ENTRY_MODIFY, ENTRY_DELETE] && e.target in watchables) {
            files[e.target]?.each { merge(it) }
        }
        // do not need a client-json regeneration
        return false
    }

    boolean merge(String m) {
        def all = (config.merge ?: [:])[m] ?: []
        def existing = all.findAll { it.f }
        if (defaultFailOnMissing.get() && all.size() != existing.size()) {
            throw new IllegalStateException("Missing resources to merge: ${all.findAll { !it.f }}")
        }
        def cb = { Merger.Element el, Throwable e ->
            if (e) {
                if (defaultFailOnMissing.get()) {
                    throw new IllegalStateException("File is missing for merge: ${el.location}")
                } else {
                    config.log('[Merge] ERROR %s: %s', el.location, e.message)
                }
            } else {
                config.log('[Merge] + %s', el.location)
            }
            return true
        }
        // create DEV version
        config.log('[Merge] %s', m)
        def out = new File(config.webapp, m)
        def complete = Merger.mergeElements(out, existing.collect {
            return new Merger.Element(
                location: it.f,
                resource: Resource.resource(config.webapp, it.f as String),
                min: false
            )
        }, cb)
        // create PROD version
        config.log('[Merge] %s', Minifier.getFilename(m))
        def min = Minifier.getFilename(out)
        return complete & Merger.mergeElements(min, existing.collect {
            String loc = String.isInstance(it.min) ? it.min : it.f
            return new Merger.Element(
                location: loc,
                resource: Resource.resource(config.webapp, loc),
                min: Boolean.isInstance(it.min) ? it.min : false
            )
        }, cb)
    }

}
