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

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class I18NServiceSkeleton implements I18NService {

    private final ConcurrentMap<Locale, I18NBundle> cache = new ConcurrentHashMap<Locale, I18NBundle>();
    private final String bundleName;

    private boolean debug;
    private MissingKeyBehaviour missingKeyBehaviour = MissingKeyBehaviour.THROW_EXCEPTION;

    protected I18NServiceSkeleton(String bundleName) {
        this.bundleName = bundleName.startsWith("/") ? bundleName.substring(1) : bundleName;
    }

    public MissingKeyBehaviour getMissingKeyBehaviour() {
        return missingKeyBehaviour;
    }

    public void setMissingKeyBehaviour(MissingKeyBehaviour missingKeyBehaviour) {
        this.missingKeyBehaviour = missingKeyBehaviour;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public final I18NBundle forLocale(Locale locale) {
        I18NBundle bundle = cache.get(locale);
        if (bundle == null) {
            cache.putIfAbsent(locale, newBundle(bundleName, locale));
            bundle = cache.get(locale);
        }
        return bundle;
    }

    @Override
    public final String toString() {
        return bundleName;
    }

    abstract I18NBundle newBundle(String bundleName, Locale locale);

}
