package com.ovea.tajin.resources

import com.ovea.tajin.TajinConfig

import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 */
class TajinResourceManager {

    final File basedir
    final TajinConfig config

    private final AtomicBoolean watching = new AtomicBoolean(false)

    TajinResourceManager(TajinConfig config, File basedir = new File('.')) {
        this.config = config
        this.basedir = basedir
    }

    void watch() {
        synchronized (this) {

        }
    }

    void unwatch() {
        synchronized (this) {

        }
    }

    boolean isWatching() {
        return watching.get()
    }

    void buid() {

    }
}
