package com.ovea.tajin.io

import java.nio.file.FileSystems
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
    private final Map<WatchKey, Map<String, ?>> watched = new HashMap<>()
    private final ReadWriteLock lock = new ReentrantReadWriteLock()
    private final AtomicReference<Thread> watcher = new AtomicReference<>()

    void watch(Collection<File> files, Closure<?> listener) {
        lock.writeLock().lock()
        try {
            files.collect { it.canonicalFile }.unique().each {
                def desc = watched.get(it)
                if (desc) {
                    desc.listeners << listener
                } else {
                    watched.put(it.toPath().register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE), [
                        file: it,
                        listeners: [listener] as CopyOnWriteArrayList
                    ])
                }
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
                                key.pollEvents().each { desc.listeners*.call(it.kind().name(), it.context(), desc.file) }
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
        files = files.collect { it.canonicalFile }.unique()
        lock.writeLock().lock()
        try {
            watched.findAll { k, v -> v.file in files }.each { k, v -> k.cancel(); watched.remove(k) }
            Thread t = watcher.get()
            if (!watched && t && !t.interrupted) {
                t.interrupt()
                watcher.set(null)
            }
        } finally {
            lock.writeLock().unlock()
        }
    }
}
