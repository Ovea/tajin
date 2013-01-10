package com.ovea.tajin

import com.ovea.tajin.io.FileWatcher
import com.ovea.tajin.io.Resource
import com.ovea.tajin.resources.TajinResourceManager

import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-01-10
 */
class Tajin {

    static final String DEFAULT_CONFIG_LOCATION = 'WEB-INF/tajin.json'

    private static final Logger LOGGER = Logger.getLogger(TajinConfig.name)

    private final FileWatcher watcher = new FileWatcher()
    private final TajinConfig config
    private final TajinResourceManager resourceManager

    Tajin(TajinConfig config) {
        this.config = config
        this.resourceManager = new TajinResourceManager(config)
        try {
            this.config.reload()
        } catch (e) {
            throw new IllegalStateException('Error loading JSON configuration ' + config + ' : ' + e.message, e)
        }
    }

    void build() {
        resourceManager.buid()
    }

    void watch() {
        synchronized (this) {
            if (config.reloadable) {
                watcher.watch([config.file], { String type ->
                    // executed in watcher thread !
                    if (type == 'ENTRY_MODIFY') {
                        try {
                            if (config.reload()) {

                            }
                        } catch (e) {
                            LOGGER.log(Level.SEVERE, 'Error loading JSON configuration ' + config + ' : ' + e.message)
                        }
                    }
                })
            }
            watcher.watch(resourceManager.resources, { String type, File res ->
                if (type == 'ENTRY_MODIFY' || type == 'ENTRY_CREATE') {
                    resourceManager.buid(res)
                }
            })
            println watcher
        }
    }

    void unwatch() {
        synchronized (this) {
            if (config.reloadable) {
                watcher.unwatch([config.file])
            }
            watcher.unwatch(resourceManager.resources)
        }
    }

    static Tajin load(File webapp, Resource config, Map<String, ?> ctx = [:]) {
        return new Tajin(new TajinConfig(webapp, config, ctx))
    }

}
