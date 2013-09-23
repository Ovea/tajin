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

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-21
 */
class CachingAPIRepository implements APIRepository {

    final APIRepository delegate
    final LoadingCache<String, APIAccess> accesses
    final LoadingCache<String, APIAccount> accounts

    CachingAPIRepository(APIRepository delegate, long expiration, TimeUnit unit) {
        this.delegate = delegate
        this.accesses = CacheBuilder
            .newBuilder()
            .expireAfterWrite(expiration, unit)
            .build(new CacheLoader<String, APIAccess>() {
            @Override
            APIAccess load(String key) throws Exception {
                return delegate.getAPIAccessByToken(key)
            }
        })
        this.accounts = CacheBuilder
            .newBuilder()
            .expireAfterWrite(expiration, unit)
            .build(new CacheLoader<String, APIAccount>() {
            @Override
            APIAccount load(String key) throws Exception {
                return delegate.getAPIAccount(key)
            }
        })
    }

    @Override
    APIAccess getAPIAccessByToken(String token) {
        try {
            return accesses.get(token)
        } catch (ExecutionException e) {
            throw e.cause
        } catch (UncheckedExecutionException e) {
            throw e.cause
        }
    }

    @Override
    APIAccount getAPIAccount(String id) {
        try {
            return accounts.get(id)
        } catch (ExecutionException e) {
            throw e.cause
        } catch (UncheckedExecutionException e) {
            throw e.cause
        }
    }
}
