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
package com.ovea.tajin.framework.support.shiro

import com.google.common.cache.CacheBuilder
import com.ovea.tajin.framework.util.PropertySettings
import org.apache.shiro.cache.AbstractCacheManager
import org.apache.shiro.cache.Cache
import org.apache.shiro.cache.CacheException

import javax.annotation.PostConstruct
import javax.inject.Inject
import java.util.concurrent.TimeUnit

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-05-08
 */
@javax.inject.Singleton
class MemoryCacheManager extends AbstractCacheManager {

    @Inject
    PropertySettings settings

    long expiration = 300

    @PostConstruct
    void init() {
        expiration = settings.getLong('security.cache.expiration', 300)
    }

    @Override
    protected Cache createCache(String name) throws CacheException {
        com.google.common.cache.Cache c = CacheBuilder.newBuilder().softValues().expireAfterWrite(expiration, TimeUnit.SECONDS).build()
        return new Cache() {
            @Override
            Object get(Object key) throws CacheException {
                return c.getIfPresent(key)
            }

            @Override
            Object put(Object key, Object value) throws CacheException {
                return c.put(key, value)
            }

            @Override
            Object remove(Object key) throws CacheException {
                return c.invalidate(key)
            }

            @Override
            void clear() throws CacheException {
                c.invalidateAll()
            }

            @Override
            int size() {
                return c.size()
            }

            @Override
            Set keys() {
                return c.asMap().keySet()
            }

            @Override
            Collection values() {
                return c.asMap().values()
            }
        }
    }

}
