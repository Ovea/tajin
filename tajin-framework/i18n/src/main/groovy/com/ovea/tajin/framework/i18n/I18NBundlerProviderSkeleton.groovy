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

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class I18NBundlerProviderSkeleton implements I18NBundlerProvider {

    private final ConcurrentMap<Locale, I18NBundle> bundles = new ConcurrentHashMap<Locale, I18NBundle>();
    private final String bundleName;

    boolean cache = true;
    MissingKeyBehaviour missingKeyBehaviour = MissingKeyBehaviour.THROW_EXCEPTION;

    protected I18NBundlerProviderSkeleton(String bundleName) {
        this.bundleName = bundleName.startsWith("/") ? bundleName.substring(1) : bundleName;
    }

    @Override
    public final I18NBundle getBundle(Locale locale) {
        I18NBundle bundle = bundles.get(locale);
        if (bundle == null) {
            bundles.putIfAbsent(locale, newBundle(bundleName, locale));
            bundle = bundles.get(locale);
        }
        return bundle;
    }

    @Override
    public final String toString() { bundleName; }

    abstract I18NBundle newBundle(String bundleName, Locale locale);

}
