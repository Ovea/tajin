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
package com.ovea.tajin.framework.core

import com.mycila.jmx.annotation.JmxBean
import com.mycila.jmx.annotation.JmxMethod
import com.mycila.jmx.annotation.JmxProperty

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-19
 */
@JmxBean('com.ovea.tajin:type=Settings,name=main')
class SettingsMBean {

    final Settings settings

    SettingsMBean(Settings settings) {
        this.settings = settings
    }

    @JmxMethod
    void set(String key, String value) { settings.properties.setProperty(key, value) }

    String get(String key) { settings.properties.getProperty(key) }

    @JmxMethod
    void unset(String key) { settings.properties.remove(key) }

    @JmxProperty
    Map<String, String> getProperties() { new TreeMap<String, String>(settings.properties) }

}
