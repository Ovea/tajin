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

import java.text.MessageFormat

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class I18NBundleSkeleton implements I18NBundle {

    final String bundleName;
    final Locale locale;
    final MissingKeyBehaviour missingKeyBehaviour;

    protected I18NBundleSkeleton(String bundleName, Locale locale, MissingKeyBehaviour missingKeyBehaviour) {
        this.bundleName = bundleName;
        this.locale = locale;
        this.missingKeyBehaviour = missingKeyBehaviour;
    }

    @Override
    final String getValue(String key) throws MissingKeyException { getValue(key, []); }

    @Override
    final String getValue(String key, List params) throws MissingKeyException {
        if (key == null)
            throw new IllegalArgumentException("Missing key !");
        String str = doGetValue(key);
        if (str == null || str.length() == 0) {
            switch (missingKeyBehaviour) {
                case MissingKeyBehaviour.RETURN_KEY:
                    str = "[" + key + "]";
                    break;
                case MissingKeyBehaviour.RETURN_NULL:
                    return null;
                case MissingKeyBehaviour.THROW_EXCEPTION:
                    throw new MissingKeyException(bundleName, locale, key);
            }
        }
        return params == null || params.empty ? str : MessageFormat.format(str, params as Object[]);
    }

    @Override
    final String toString() { bundleName + " (" + locale + ")"; }

    abstract String doGetValue(String key);

}
