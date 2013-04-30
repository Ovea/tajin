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
package com.ovea.tajin.framework.i18n;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class DefaultI18NService extends I18NServiceSkeleton {

    public DefaultI18NService(String bundleName) {
        super(bundleName);
    }

    @Override
    I18NBundle newBundle(String bundleName, Locale locale) {
        return new I18NBundleSkeleton(bundleName, locale, getMissingKeyBehaviour()) {
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
                String b = bundleName();
                if (b.endsWith(".properties")) {
                    b = b.substring(0, b.length() - 11);
                }
                if (isDebug()) {
                    ResourceBundle.clearCache(loader);
                    return ResourceBundle.getBundle(b, locale(), loader);
                }
                if (this.bundle == null) {
                    this.bundle = ResourceBundle.getBundle(b, locale(), loader);
                }
                return this.bundle;
            }
        };
    }
}
