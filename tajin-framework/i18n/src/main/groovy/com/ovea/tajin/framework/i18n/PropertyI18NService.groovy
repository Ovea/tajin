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

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@CompileStatic
public class PropertyI18NService implements I18NService {

    private final ConcurrentMap<String, PropertyI18NBundlerProvider> providers = new ConcurrentHashMap<String, PropertyI18NBundlerProvider>();

    boolean cache = true;
    MissingKeyBehaviour missingKeyBehaviour = MissingKeyBehaviour.THROW_EXCEPTION;

    @Override
    public I18NBundlerProvider getBundleProvider(String bundleName) {
        bundleName = bundleName.startsWith("/") ? bundleName.substring(1) : bundleName;
        PropertyI18NBundlerProvider service = providers.get(bundleName);
        if (service == null) {
            providers.putIfAbsent(bundleName, new PropertyI18NBundlerProvider(bundleName));
            service = providers.get(bundleName);
            service.cache = this.cache
            service.missingKeyBehaviour = this.missingKeyBehaviour
        }
        return service;
    }
}
