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

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class Minifier implements ResourceBuilder {

    final TajinConfig config

    Minifier(TajinConfig config) {
        this.config = config
    }

    @Override
    void build() {
        watchables.each { minify(it) }
    }

    @Override
    Collection<File> getWatchables() { config.minify.collect { String path -> new File(config.webapp, path).absoluteFile } }

    @Override
    boolean modified(FileWatcher.Event e) {
        minify(e.target)
        // do not need a client-json regeneration
        return false
    }

    private void minify(File src) {
        config.log('Minify: %s', src)
        //TODO: what to do for inisting resources like big.css which are created after ?
    }
}
