package com.ovea.tajin.framework.support.jersey

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-11
 */
@Target([ElementType.TYPE, ElementType.METHOD])
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONP {
    String callbackParam() default 'callback'

    String methodParam() default 'method'

    String[] ignores() default ['callback', 'method', '_']
}
