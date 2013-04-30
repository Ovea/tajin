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

import com.google.inject.Binder
import com.google.inject.servlet.ServletModule

public interface WebBinder extends Binder {

    void configure(Closure<?> c)

    /**
     * @param urlPattern Any Servlet-style pattern. examples: /*, /html/*, *.html, etc.
     * @since 2.0
     */
    ServletModule.FilterKeyBindingBuilder filter(String urlPattern, String... morePatterns)

    /**
     * @param regex Any Java-style regular expression.
     * @since 2.0
     */
    ServletModule.FilterKeyBindingBuilder filterRegex(String regex, String... regexes)

    /**
     * @param urlPattern Any Servlet-style pattern. examples: /*, /html/*, *.html, etc.
     * @since 2.0
     */
    ServletModule.ServletKeyBindingBuilder serve(String urlPattern, String... morePatterns)

    /**
     * @param regex Any Java-style regular expression.
     * @since 2.0
     */
    ServletModule.ServletKeyBindingBuilder serveRegex(String regex, String... regexes)
}
