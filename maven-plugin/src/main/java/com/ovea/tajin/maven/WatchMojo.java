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
package com.ovea.tajin.maven;

import com.ovea.tajin.TajinConfig;
import com.ovea.tajin.resources.TajinResourceManager;

import java.util.concurrent.CountDownLatch;

/**
 * Watch for resoruce change to rebuild resources
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-27
 * @goal watch
 * @threadSafe
 */
public final class WatchMojo extends TajinMavenPlugin {
    @Override
    void execute(TajinConfig config) {
        final TajinResourceManager manager = new TajinResourceManager(config);
        manager.buid();
        manager.watch();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                manager.unwatch();
            }
        });
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
