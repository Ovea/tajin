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
package com.ovea.tajin.framework.support.guice

import com.google.inject.TypeLiteral
import com.google.inject.matcher.AbstractMatcher
import com.google.inject.matcher.Matcher

import java.lang.reflect.AnnotatedElement

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
class ClassToTypeLiteralMatcherAdapter extends AbstractMatcher<TypeLiteral<?>> {
    private final Matcher<TypeLiteral<?>> m

    ClassToTypeLiteralMatcherAdapter(final Matcher<? extends AnnotatedElement> classMatcher) {
        this.m = new AbstractMatcher<TypeLiteral<?>>() {
            @Override
            boolean matches(TypeLiteral<?> t) {
                return classMatcher.matches(t.rawType)
            }
        }
    }

    @Override
    boolean matches(TypeLiteral<?> typeLiteral) { m.matches(typeLiteral) }

    static Matcher<TypeLiteral<?>> adapt(Matcher<? extends AnnotatedElement> classMatcher) { new ClassToTypeLiteralMatcherAdapter(classMatcher) }
}
