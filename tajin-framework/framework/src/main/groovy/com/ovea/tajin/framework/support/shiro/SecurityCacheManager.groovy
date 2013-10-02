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
import com.mycila.jmx.annotation.JmxBean
import com.mycila.jmx.annotation.JmxMethod
import com.mycila.jmx.annotation.JmxProperty
import com.ovea.tajin.framework.util.PropertySettings
import org.apache.shiro.cache.AbstractCacheManager
import org.apache.shiro.cache.Cache
import org.apache.shiro.cache.CacheException
import org.apache.shiro.util.Destroyable

import javax.inject.Inject
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-05-08
 */
@javax.inject.Singleton
@JmxBean('com.ovea.tajin:type=SecurityCacheManager,name=main')
class SecurityCacheManager extends AbstractCacheManager {

    @Inject PropertySettings settings

    final Set<String> names = new ConcurrentSkipListSet<>()

    @JmxMethod
    void clearShiroCaches() { destroy() }

    @JmxProperty
    Collection<String> getCacheNames() { names }

    @JmxProperty
    Map<String, Collection<String>> getCachedKeys() { names.collectEntries { [(it): getCache(it).keys().collect { it as String }] } }

    @Override
    protected Cache createCache(String name) throws CacheException { new GuavaCache(name) }

    class GuavaCache implements Cache, Destroyable {

        final String name
        final com.google.common.cache.Cache c = CacheBuilder.newBuilder()
            .softValues()
            .expireAfterWrite(settings.getLong('security.cache.expiration', 300), TimeUnit.SECONDS)
            .build()

        GuavaCache(String name) {
            this.name = name
            names.add(name)
        }

        @Override
        void destroy() throws Exception {
            c.invalidateAll()
            names.remove(name)
        }

        @Override
        Object get(Object key) throws CacheException { c.getIfPresent(key) }

        @Override
        Object put(Object key, Object value) throws CacheException { c.put(key, value) }

        @Override
        Object remove(Object key) throws CacheException { c.invalidate(key) }

        @Override
        void clear() throws CacheException { c.invalidateAll() }

        @Override
        int size() { c.size() }

        @Override
        Set keys() { c.asMap().keySet().collect() }

        @Override
        Collection values() { c.asMap().values() }

        @Override
        String toString() { "Cache(${name})" }
    }

}
