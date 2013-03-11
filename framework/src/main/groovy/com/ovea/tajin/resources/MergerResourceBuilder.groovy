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
import com.ovea.tajin.io.*

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
    private final AtomicBoolean defaultWatch = new AtomicBoolean(false)
    private final AtomicBoolean defaultFailOnMissing = new AtomicBoolean(true)
    private final AtomicBoolean defaultMin = new AtomicBoolean(false)
    private ResourceResolver resolver = ResourceResolver.DEFAULT

    MergerResourceBuilder(TajinConfig config) {
        this.config = config
        config.onConfig {
            config.log("[Merge] Tajin configuration changed")
            lock.writeLock().lock()
            try {
                def cfg = config.merge ?: [:]
                defaultFailOnMissing.set(Boolean.isInstance(cfg.failOnMissing) ? cfg.failOnMissing : defaultFailOnMissing.get())
                defaultWatch.set(Boolean.isInstance(cfg.watch) ? cfg.watch : defaultWatch.get())
                defaultMin.set(Boolean.isInstance(cfg.min) ? cfg.min : defaultMin.get())
                // caching ?
                if (String.isInstance(cfg.cache)) {
                    resolver = new CacheResourceResolver(new File(cfg.cache as String))
                }
                else if (Boolean.isInstance(cfg.cache) && cfg.cache) {
                    resolver = new CacheResourceResolver(new File(System.properties['user.home'] as String, '.tajin/cache'))
                } else {
                    resolver = ResourceResolver.DEFAULT
                }
                // watched files
                files.clear()
                cfg.each { String m, resources ->
                    if (Collection.isInstance(resources)) {
                        merges << m
                        resources.findAll { String.isInstance(it) && defaultWatch.get() || Map.isInstance(it) && it.f && (Boolean.isInstance(it.watch) ? it.watch : defaultWatch.get()) }.collect {
                            String f = String.isInstance(it) ? it : it.f
                            String min = String.isInstance(it) || !String.isInstance(it.min) ? f : it.min
                            return [Resource.resource(config.webapp, f), Resource.resource(config.webapp, min)].findAll { Resource r -> r.file }
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
        def existing = all.findAll { String.isInstance(it) || it.f }
        if (defaultFailOnMissing.get() && all.size() != existing.size()) {
            throw new IllegalStateException("Missing resources to merge: ${all.findAll { !String.isInstance(it) && !it.f }}")
        }
        def cb = { Merger.Element el, Throwable e ->
            if (e) {
                if (defaultFailOnMissing.get()) {
                    throw new IllegalStateException("File is missing for merge: ${el.location}")
                } else {
                    config.log('[Merge] WARNING %s: %s', el.location, e.message)
                }
            } else {
                config.log('[Merge]   + %s (min=%s)', el.location, el.min)
            }
            return true
        }
        // create DEV version
        config.log('[Merge] %s', m)
        File out = new File(config.webapp, m)
        Merger merger = new Merger(existing.collect {
            String f = String.isInstance(it) ? it : it.f
            return new Merger.Element(
                location: f,
                resource: resolver.resolve(Resource.resource(config.webapp, f)),
                min: false
            )
        }, cb)
        boolean complete = merger.mergeTo(out)
        // create PROD version
        config.log('[Merge] %s', Minifier.getFilename(m))
        File min = Minifier.getFilename(out)
        merger = new Merger(existing.collect {
            if (String.isInstance(it)) {
                return new Merger.Element(
                    location: it,
                    resource: resolver.resolve(Resource.resource(config.webapp, it as String)),
                    min: defaultMin.get()
                )
            } else {
                String loc = String.isInstance(it.min) ? it.min : it.f
                return new Merger.Element(
                    location: loc,
                    resource: resolver.resolve(Resource.resource(config.webapp, loc)),
                    min: it.min == null ? defaultMin.get() : Boolean.isInstance(it.min) ? it.min : false
                )
            }
        }, cb)
        return complete & merger.mergeTo(min)
    }

}
