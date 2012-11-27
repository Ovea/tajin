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

import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 */
class TajinResourceManager {

    final File webapp
    final TajinConfig config

    private final AtomicBoolean watching = new AtomicBoolean(false)

    TajinResourceManager(TajinConfig config, File webapp = new File('.')) {
        this.config = config
        this.webapp = webapp
        if (!webapp.exists() || !webapp.directory) {
            throw new IllegalArgumentException('Illegal Webapp folder: ' + webapp)
        }
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
