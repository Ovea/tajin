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
package com.ovea.tajin.io

import java.nio.file.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

import static java.nio.file.StandardWatchEventKinds.*

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-01-10
 */
class FileWatcher {

    private final WatchService watchService = FileSystems.default.newWatchService()
    private final Map<WatchKey, Bucket> watched = new HashMap<>()
    private final ReadWriteLock lock = new ReentrantReadWriteLock()
    private final AtomicReference<Thread> watcher = new AtomicReference<>()

    void watch(Collection<File> files, Closure<?> listener) {
        lock.writeLock().lock()
        try {
            files.collect { it.canonicalFile }.unique().each {
                def folder = it.directory ? it : it.parentFile.canonicalFile
                def filename = it.directory ? '*' : it.name
                Bucket bucket = watched.find { k, v -> v.folder == folder }?.value
                if (!bucket) {
                    bucket = new Bucket(folder)
                    watched.put(folder.toPath().register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE), bucket)
                }
                if (!bucket.listeners[filename]) {
                    bucket.listeners[filename] = new CopyOnWriteArrayList<>()
                }
                bucket.listeners[filename] << listener
            }
            Thread t = watcher.get()
            if (watched && (!t || t.interrupted)) {
                watcher.set(Thread.start(FileWatcher.name, {
                    while (!Thread.currentThread().interrupted) {
                        try {
                            WatchKey key = watchService.take()
                            lock.readLock().lock()
                            Bucket desc
                            try {
                                desc = watched.get(key)
                            } finally {
                                lock.readLock().unlock()
                            }
                            if (key.valid && desc) {
                                key.pollEvents().each { WatchEvent<Path> evt ->
                                    def f = evt.context().toFile()
                                    ((desc.listeners[f.name] ?: []) + (desc.listeners['*'] ?: []))*.call(new Event(evt.kind().name(), f, desc.folder))
                                }
                            }
                            key.reset()
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt()
                            watcher.set(null)
                        }
                    }
                }))
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    void unwatch(Collection<File> files) {
        lock.writeLock().lock()
        try {
            files.collect { it.canonicalFile }.unique().each {
                def folder = it.directory ? it : it.parentFile.canonicalFile
                Map.Entry<WatchKey, Bucket> entry = watched.find { k, v -> v.folder == folder }
                if (entry) {
                    if (it.directory) {
                        entry.value.listeners.remove('*')
                    } else {
                        entry.value.listeners.remove(it.name)
                    }
                    if (!entry.value.listeners) {
                        entry.key.cancel()
                        watched.remove(entry.key)
                    }
                }
            }
            Thread t = watcher.get()
            if (!watched && t && !t.interrupted) {
                t.interrupt()
                watcher.set(null)
            }
        } finally {
            lock.writeLock().unlock()
        }
    }

    @Override
    String toString() {
        return "FileWatcher: " + watched.values()
    }

    private static class Bucket {
        final File folder
        final Map<String, Collection<Closure<?>>> listeners = new HashMap<>()

        Bucket(File folder) {
            this.folder = folder
        }

        @Override
        String toString() {
            return "${folder}:${listeners}"
        }
    }

    static class Event {

        static final String ENTRY_CREATE = StandardWatchEventKinds.ENTRY_CREATE.name()
        static final String ENTRY_MODIFY = StandardWatchEventKinds.ENTRY_MODIFY.name()
        static final String ENTRY_DELETE = StandardWatchEventKinds.ENTRY_DELETE.name()

        final File folder
        final String type
        final File target

        Event(String type, File target, File folder) {
            this.type = type
            this.target = target
            this.folder = folder
        }

        @Override
        public String toString() {
            return "Event{" +
                "type='" + type + '\'' +
                ", target=" + target +
                ", folder=" + folder +
                '}';
        }
    }
}
