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
package com.ovea.tajin.framework.app

import com.ovea.tajin.framework.core.Resource
import com.ovea.tajin.framework.util.PropertySettings

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Tajin {

    static final String VERSION = {
        try {
            return new PropertySettings(Resource.classpath(Tajin.classLoader, '/META-INF/maven/com.ovea.tajin.framework/tajin-framework/pom.properties')).getString("version")
        } catch (ignored) {
            return "unknown"
        }
    }.call()

}
