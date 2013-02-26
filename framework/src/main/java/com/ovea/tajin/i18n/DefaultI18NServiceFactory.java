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
package com.ovea.tajin.i18n;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultI18NServiceFactory implements I18NServiceFactory {

    private final ConcurrentMap<String, DefaultI18NService> cache = new ConcurrentHashMap<String, DefaultI18NService>();
    private boolean debug;

    @Override
    public I18NService forBundle(String bundleName) {
        return forBundle(bundleName, Thread.currentThread().getContextClassLoader());
    }

    @Override
    public I18NService forBundle(String bundleName, ClassLoader classLoader) {
        bundleName = bundleName.startsWith("/") ? bundleName.substring(1) : bundleName;
        DefaultI18NService service = cache.get(bundleName);
        if (service == null) {
            cache.putIfAbsent(bundleName, new DefaultI18NService(bundleName, classLoader));
            service = cache.get(bundleName);
            service.setDebug(isDebug());
        }
        return service;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
