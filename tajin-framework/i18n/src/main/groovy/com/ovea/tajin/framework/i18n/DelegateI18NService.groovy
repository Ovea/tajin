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
 * @date 2013-08-12
 */
class DelegateI18NService implements I18NService {

    JsonI18NService jsonI18NService = new JsonI18NService()
    PropertyI18NService propertyI18NService = new PropertyI18NService()

    void setCache(boolean cache) {
        jsonI18NService.cache = cache
        propertyI18NService.cache = cache
    }

    void setMissingKeyBehaviour(MissingKeyBehaviour missingKeyBehaviour) {
        jsonI18NService.missingKeyBehaviour = missingKeyBehaviour
        propertyI18NService.missingKeyBehaviour = missingKeyBehaviour
    }

    @Override
    I18NBundlerProvider getBundleProvider(String bundleName) {
        if (bundleName.endsWith('.json'))
            return jsonI18NService.getBundleProvider(bundleName)
        if (bundleName.endsWith('.properties'))
            propertyI18NService.getBundleProvider(bundleName)
        throw new IllegalArgumentException(bundleName)
    }
}
