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
package com.ovea.tajin.framework.i18nL10n

public class PropertyI18NBundlerProvider extends I18NBundlerProviderSkeleton {

    PropertyI18NBundlerProvider(String bundleName, int maximumSize, long expirationSeconds) {
        super(bundleName, maximumSize, expirationSeconds)
    }

    @Override
    I18NBundle newBundle(String bundleName, Locale locale) {
        URLClassLoader loader = new URLClassLoader(new URL[0], Thread.currentThread().contextClassLoader)
        return new PropertyI18NBundle(bundleName, locale, missingKeyBehaviour, load(locale, loader), loader)
    }

    private ResourceBundle load(Locale locale, ClassLoader loader) {
        String b = bundleName;
        if (b.endsWith(".properties")) {
            b = b.substring(0, b.length() - 11);
        }
        return ResourceBundle.getBundle(b, locale, loader);
    }

}
