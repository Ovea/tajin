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

import java.util.*;

public class DefaultI18NService extends I18NServiceSkeleton {

    public DefaultI18NService(String bundleName, ClassLoader classLoader) {
        super(bundleName, classLoader);
    }

    @Override
    I18NBundle newBundle(String bundleName, ClassLoader classLoader, Locale locale) {
        return new I18NBundleSkeleton(bundleName, classLoader, locale, getMissingKeyBehaviour()) {
            ResourceBundle bundle;

            @Override
            public List<String> keys() {
                List<String> keys = new LinkedList<String>();
                Enumeration<String> enu = getBundle().getKeys();
                while (enu.hasMoreElements())
                    keys.add(enu.nextElement());
                return keys;
            }

            @Override
            public boolean hasKey(String key) {
                return getBundle().containsKey(key);
            }

            @Override
            String getValue(String key) throws MissingMessageException {
                try {
                    return getBundle().getString(key);
                } catch (MissingResourceException e) {
                    return null;
                }
            }

            private ResourceBundle getBundle() {
                if (isDebug()) {
                    ResourceBundle.clearCache(loader());
                    return ResourceBundle.getBundle(bundleName(), locale(), loader());
                }
                if (this.bundle == null) {
                    this.bundle = ResourceBundle.getBundle(bundleName(), locale(), loader());
                }
                return this.bundle;
            }
        };
    }
}
