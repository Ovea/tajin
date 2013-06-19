package com.ovea.tajin.framework.util

import com.ovea.tajin.framework.jmx.annotation.JmxBean
import com.ovea.tajin.framework.jmx.annotation.JmxMethod

import javax.inject.Inject

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-19
 */
@javax.inject.Singleton
@JmxBean('com.ovea.tajin:type=PropertySettings,name=main')
class PropertySettingsMBean {

    @Inject
    PropertySettings settings

    @JmxMethod
    void set(String key, String value) { settings.properties.setProperty(key, value) }

    String get(String key) { settings.properties.getProperty(key) }

    @JmxMethod
    void unset(String key) { settings.properties.remove(key) }

    @JmxMethod
    Map<String,String> list() { new TreeMap<String, String>(settings.properties) }

}
