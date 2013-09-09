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

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.util.concurrent.UncheckedExecutionException
import com.ovea.tajin.framework.core.Settings

import javax.inject.Inject
import java.util.concurrent.ExecutionException

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-08-12
 */
@javax.inject.Singleton
class Default18NService implements I18NService {

    int maximumSize = 100
    long expirationSeconds = 3600
    MissingKeyBehaviour missingKeyBehaviour = MissingKeyBehaviour.THROW_EXCEPTION;

    private final LoadingCache<String, I18NBundlerProvider> providers = CacheBuilder.newBuilder().build(new CacheLoader<String, I18NBundlerProvider>() {
        @Override
        I18NBundlerProvider load(String bundleName) throws Exception {
            I18NBundlerProviderSkeleton service
            if (bundleName.endsWith('.json'))
                service = new JsonI18NBundlerProvider(bundleName, maximumSize, expirationSeconds)
            else if (bundleName.endsWith('.properties'))
                service = new PropertyI18NBundlerProvider(bundleName, maximumSize, expirationSeconds)
            else if (bundleName.endsWith('.js'))
                service = new JsI18NBundlerProvider(bundleName, maximumSize, expirationSeconds)
            else
                throw new IllegalArgumentException(bundleName)
            service.missingKeyBehaviour = missingKeyBehaviour
            return service
        }
    })

    @Inject
    void setSettings(Settings settings) {
        expirationSeconds = settings.getLong('tajin.i18n.cache.expirationSeconds', expirationSeconds)
        maximumSize = settings.getLong('tajin.i18n.cache.maximumSize', maximumSize)
        missingKeyBehaviour = settings.getEnum(MissingKeyBehaviour, 'tajin.i18n.miss', missingKeyBehaviour)
    }

    @Override
    public I18NBundlerProvider getBundleProvider(String bundleName) {
        bundleName = bundleName.startsWith("/") ? bundleName.substring(1) : bundleName;
        try {
            return providers.get(bundleName)
        } catch (UncheckedExecutionException e) {
            throw e.cause
        } catch (ExecutionException e) {
            throw e.cause
        }
    }

}
