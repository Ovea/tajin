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

import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class I18NBundleSkeleton implements I18NBundle {

    private final ClassLoader loader;
    private final String bundleName;
    private final Locale locale;
    private final I18NServiceSkeleton.MissingKeyBehaviour missingKeyBehaviour;

    protected I18NBundleSkeleton(String bundleName, ClassLoader classLoader, Locale locale, I18NServiceSkeleton.MissingKeyBehaviour missingKeyBehaviour) {
        this.bundleName = bundleName;
        this.locale = locale;
        this.loader = new URLClassLoader(new URL[0], classLoader);
        this.missingKeyBehaviour = missingKeyBehaviour;
    }

    @Override
    public final String message(String key) throws MissingMessageException {
        return message(key, new Object[0]);
    }

    @Override
    public final String message(String key, Object... params) throws MissingMessageException {
        if (key == null)
            throw new IllegalArgumentException("Missing key !");
        String str = getValue(key);
        if (str == null || str.length() == 0) {
            switch (missingKeyBehaviour) {
                case RETURN_KEY: {
                    str = "[" + key + "]";
                    break;
                }
                case RETURN_NULL:
                    return null;
                case THROW_EXCEPTION:
                    throw new MissingMessageException(loader(), bundleName(), locale(), key);
            }
        }
        return params == null || params.length == 0 ? str : MessageFormat.format(str, params);
    }

    @Override
    public final String toString() {
        return bundleName + " (" + locale + ")";
    }

    abstract String getValue(String key);

    final String bundleName() {
        return bundleName;
    }

    final ClassLoader loader() {
        return loader;
    }

    final Locale locale() {
        return locale;
    }
}
