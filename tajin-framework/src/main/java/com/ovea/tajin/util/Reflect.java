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
package com.ovea.tajin.util;
/**
 * Copyright (C) 2010 Mycila <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.base.Predicate;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class Reflect {

    public static Class<?> getTargetClass(Class<?> proxy) {
        if (proxy.getName().contains("$$")) {
            do {
                proxy = proxy.getSuperclass();
            } while (proxy.getName().contains("$$"));
            return proxy;
        }
        return proxy;
    }

    public static Class<?> getTargetClass(Object instance) {
        return getTargetClass(instance.getClass());
    }

    public static <T> Predicate<T> predicate(final Matcher<T> matcher) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return matcher.matches(input);
            }
        };
    }

    public static <T> Matcher<T> matcher(final Predicate<T> predicate) {
        return new AbstractMatcher<T>() {
            @Override
            public boolean matches(T t) {
                return predicate.apply(t);
            }
        };
    }

    public static Predicate<Method> withParameterTypes(final Class<?>... classes) {
        return new Predicate<Method>() {
            @Override
            public boolean apply(Method m) {
                Class<?>[] thisParams = m.getParameterTypes();
                if (thisParams.length != classes.length)
                    return false;
                int c = 0;
                for (Class<?> thisParam : thisParams)
                    if (thisParam != classes[c++])
                        return false;
                return true;
            }
        };
    }

    public static <T extends Member> Predicate<T> named(final String methodName) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T member) {
                return member.getName().equals(methodName);
            }
        };
    }

    public static <T extends AnnotatedElement> Predicate<T> annotatedBy(final Class<? extends Annotation> annotationType) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T element) {
                return element.isAnnotationPresent(annotationType);
            }
        };
    }

    public static Iterable<Field> findFields(Class<?> type, Predicate<? super Field> predicate) {
        List<Field> fields = new LinkedList<Field>();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields())
                if (predicate.apply(field))
                    fields.add(field);
            type = type.getSuperclass();
        }
        return fields;
    }

    private Reflect() {
    }

    private static final Predicate<Method> METHOD_FILTER = new Predicate<Method>() {
        @Override
        public boolean apply(Method member) {
            return !(member.isSynthetic() || member.isBridge());
        }
    };

    /**
     * Returns true if a overrides b. Assumes signatures of a and b are the same and a's declaring
     * class is a subclass of b's declaring class.
     */
    private static boolean overrides(Method a, Method b) {
        // See JLS section 8.4.8.1
        int modifiers = b.getModifiers();
        if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
            return true;
        }
        if (Modifier.isPrivate(modifiers)) {
            return false;
        }
        // b must be package-private
        return a.getDeclaringClass().getPackage().equals(b.getDeclaringClass().getPackage());
    }

}