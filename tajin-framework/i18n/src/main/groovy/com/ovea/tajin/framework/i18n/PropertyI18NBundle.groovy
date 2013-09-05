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

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-05
 */
class PropertyI18NBundle extends I18NBundleSkeleton {
    final ResourceBundle bundle
    final URLClassLoader loader

    PropertyI18NBundle(String bundleName, Locale locale, MissingKeyBehaviour missingKeyBehaviour, ResourceBundle bundle, URLClassLoader loader) {
        super(bundleName, locale, missingKeyBehaviour)
        this.bundle = bundle
        this.loader = loader
    }

    @Override
    List<String> getKeys() { bundle.keys.toList() }

    @Override
    boolean contains(String key) { bundle.containsKey(key); }

    @Override
    String doGetValue(String key) throws MissingKeyException {
        try {
            return getBundle().getString(key);
        } catch (MissingResourceException ignored) {
            return null;
        }
    }

}
