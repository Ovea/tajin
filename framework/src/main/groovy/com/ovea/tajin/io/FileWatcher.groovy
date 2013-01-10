package com.ovea.tajin.io

import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.WatchKey
import java.nio.file.WatchService
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
                            def desc
                            try {
                                desc = watched.get(key)
                            } finally {
                                lock.readLock().unlock()
                            }
                            if (key.valid && desc) {
                                key.pollEvents().each {
                                    def f = ((Path) it.context()).toFile()
                                    desc.listeners[f.name]*.call(it.kind().name(), f, desc.file)
                                    desc.listeners['*']*.call(it.kind().name(), f, desc.file)
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
}
