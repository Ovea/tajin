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
package com.ovea.tajin.framework.support.guice;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.internal.BytecodeGen;
import com.google.inject.internal.cglib.reflect.$FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutionException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Proxy {

    private Proxy() {
    }

    private static final LoadingCache<Method, MethodInvoker> INVOKER_CACHE = CacheBuilder.newBuilder().weakKeys().weakValues().build(new CacheLoader<Method, MethodInvoker>() {
        @Override
        public MethodInvoker load(final Method method) throws Exception {
            int modifiers = method.getModifiers();
            if (!Modifier.isPrivate(modifiers) && !Modifier.isProtected(modifiers)) {
                try {
                    final $FastMethod fastMethod = BytecodeGen.newFastClass(method.getDeclaringClass(), BytecodeGen.Visibility.forMember(method)).getMethod(method);
                    return new MethodInvoker() {
                        public Object invoke(Object target, Object... parameters) throws IllegalAccessException, InvocationTargetException {
                            return fastMethod.invoke(target, parameters);
                        }
                    };
                } catch (net.sf.cglib.core.CodeGenerationException e) {/* fall-through */}
            }
            if (!Modifier.isPublic(modifiers) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                method.setAccessible(true);
            }
            return new MethodInvoker() {
                public Object invoke(Object target, Object... parameters) throws IllegalAccessException, InvocationTargetException {
                    return method.invoke(target, parameters);
                }
            };
        }
    });

    public static MethodInvoker invoker(final Method method) {
        try {
            return INVOKER_CACHE.get(method);
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
