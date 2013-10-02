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
package com.ovea.tajin.framework.support.jersey

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.google.common.util.concurrent.UncheckedExecutionException
import com.mycila.jmx.annotation.JmxBean
import com.mycila.jmx.annotation.JmxMethod
import com.mycila.jmx.annotation.JmxProperty

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-21
 */
@javax.inject.Singleton
@JmxBean('com.ovea.tajin:type=CachingAPIRepository,name=main')
class CachingAPIRepository implements APIRepository {

    final APIRepository delegate
    final LoadingCache<String, APIToken> tokens

    CachingAPIRepository(APIRepository delegate, long expiration, TimeUnit unit) {
        this.delegate = delegate
        this.tokens = CacheBuilder
            .newBuilder()
            .expireAfterWrite(expiration, unit)
            .build(new CacheLoader<String, APIToken>() {
            @Override
            APIToken load(String key) throws Exception {
                APIToken token = delegate.getAPIToken(key)
                if (token) return token
                throw new NullToken()
            }
        })
    }

    @JmxMethod
    void clearTokenCache() { tokens.invalidateAll() }

    @JmxProperty
    Collection getCachedTokens() { tokens.asMap().keySet().collect() }

    @Override
    APIToken getAPIToken(String token) {
        try {
            return tokens.get(token)
        } catch (ExecutionException e) {
            if (e.cause instanceof NullToken) return null
            throw e.cause
        } catch (UncheckedExecutionException e) {
            if (e.cause instanceof NullToken) return null
            throw e.cause
        }
    }

    private static class NullToken extends Exception {}

}
