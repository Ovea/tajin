package com.ovea.tajin.resources

import com.ovea.tajin.TajinConfig
import com.ovea.tajin.io.FileWatcher

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-18
 */
class I18N {

    private static final String BUNDLE_FORMAT = '((_([a-z]{2}))|(_([a-z]{2}_[A-Z]{2})))?\\.json'

    final TajinConfig config

    I18N(TajinConfig config) {
        this.config = config
    }

    void build() {
        bundles.each { String bundle, cfg ->
            cfg.variants = findVariants(bundle, cfg)
        }
    }

    Collection<File> getWatchables() {
        return bundles.collect { bundle, cfg -> new File(config.webapp, cfg.location ?: '.').absoluteFile }
    }

    boolean modified(FileWatcher.Event event) {
        def e = bundles.find { String bundle, cfg -> event.target.name =~ "${bundle}${BUNDLE_FORMAT}" && event.folder == new File(config.webapp, cfg.location ?: '.').absoluteFile }
        if (e) {
            def variants = findVariants(e.key, e.value)
            if (e.value.variants != variants) {
                e.value.variants = variants
                return true
            }
        }
        return false
    }

    private def findVariants(String bundle, def cfg) {
        File dir = new File(config.webapp, cfg.location ?: '.').absoluteFile
        def variants = []
        dir.eachFile { File f ->
            def matcher = f.name =~ "${bundle}${BUNDLE_FORMAT}"
            if (matcher) {
                if (matcher[0][3]) {
                    variants << matcher[0][3]
                } else if (matcher[0][5]) {
                    variants << matcher[0][5]
                }
            }
        }
        config.log("Variants found for bundle %s: %s", bundle, variants)
        return variants
    }

    private def getBundles() {
        return config.hasClientConfig() ? (config.clientConfig?.i18n?.bundles ?: [:]) : [:]
    }

}
