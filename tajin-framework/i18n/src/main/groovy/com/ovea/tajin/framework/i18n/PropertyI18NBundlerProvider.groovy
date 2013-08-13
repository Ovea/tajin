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
package com.ovea.tajin.framework.i18n

import groovy.transform.CompileStatic

@CompileStatic
public class PropertyI18NBundlerProvider extends I18NBundlerProviderSkeleton {

    public PropertyI18NBundlerProvider(String bundleName) {
        super(bundleName);
    }

    @Override
    I18NBundle newBundle(String bundleName, Locale locale) {
        return new I18NBundleSkeleton(bundleName, locale, missingKeyBehaviour) {
            volatile ResourceBundle _bundle;

            @Override
            public List<String> getKeys() { getBundle().keys.toList() }

            @Override
            public boolean contains(String key) { getBundle().containsKey(key); }

            @Override
            String doGetValue(String key) throws MissingKeyException {
                try {
                    return getBundle().getString(key);
                } catch (MissingResourceException ignored) {
                    return null;
                }
            }

            private ResourceBundle getBundle() {
                if (_bundle != null) return _bundle
                String b = bundleName;
                if (b.endsWith(".properties")) {
                    b = b.substring(0, b.length() - 11);
                }
                if (cache) {
                    _bundle = ResourceBundle.getBundle(b, locale, loader);
                    return _bundle;
                } else {
                    ResourceBundle.clearCache(loader);
                    return ResourceBundle.getBundle(b, locale, loader);

                }
            }
        };
    }
}
