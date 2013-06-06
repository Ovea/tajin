package com.ovea.tajin.framework.support.guice

import com.google.inject.TypeLiteral
import com.google.inject.matcher.AbstractMatcher
import com.google.inject.matcher.Matcher

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
class ClassToTypeLiteralMatcherAdapter extends AbstractMatcher<TypeLiteral<?>> {
    private final Matcher<Class> classMatcher

    ClassToTypeLiteralMatcherAdapter(Matcher<Class> classMatcher) {
        this.classMatcher = classMatcher;
    }

    @Override
    boolean matches(TypeLiteral<?> typeLiteral) { classMatcher.matches(typeLiteral.getRawType()) }

    static Matcher<TypeLiteral<?>> adapt(Matcher<Class> classMatcher) { new ClassToTypeLiteralMatcherAdapter(classMatcher) }
}
