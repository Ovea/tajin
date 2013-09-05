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

import com.google.common.cache.*
import com.google.common.util.concurrent.UncheckedExecutionException

import java.util.concurrent.TimeUnit

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class I18NBundlerProviderSkeleton implements I18NBundlerProvider {

    private final LoadingCache<Locale, I18NBundle> cache

    final String bundleName;
    MissingKeyBehaviour missingKeyBehaviour = MissingKeyBehaviour.THROW_EXCEPTION;

    protected I18NBundlerProviderSkeleton(String bundleName, int maximumSize, long expirationSeconds) {
        this.bundleName = bundleName.startsWith("/") ? bundleName.substring(1) : bundleName;
        CacheBuilder builder = CacheBuilder.newBuilder()
        if (maximumSize >= 0) builder.maximumSize(maximumSize)
        if (expirationSeconds >= 0) builder.expireAfterWrite(expirationSeconds, TimeUnit.SECONDS)
        this.cache = builder.removalListener(new RemovalListener() {
            @Override
            void onRemoval(RemovalNotification notification) {
                if (notification.value instanceof PropertyI18NBundle) {
                    ResourceBundle.clearCache(((PropertyI18NBundle) notification.value).loader)
                }
            }
        }).build(new CacheLoader<Locale, I18NBundle>() {
            @Override
            I18NBundle load(Locale key) throws Exception { newBundle(bundleName, key) }
        })
    }

    @Override
    public final I18NBundle getBundle(Locale locale) {
        try {
            cache.get(locale)
        } catch (UncheckedExecutionException e) {
            throw e.cause
        }
    }

    @Override
    public final String toString() { bundleName; }

    abstract I18NBundle newBundle(String bundleName, Locale locale);

}
