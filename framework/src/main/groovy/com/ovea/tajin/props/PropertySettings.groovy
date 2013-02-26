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
package com.ovea.tajin.props

import com.ovea.tajin.io.Resource

class PropertySettings {

    private final PropertyPlaceholderResolver resolver = new PropertyPlaceholderResolver()
    private final Properties properties

    public PropertySettings() {
        this(new Properties())
    }

    public PropertySettings(Properties properties) {
        this.properties = properties
        this.resolver.systemPropertiesMode = SystemPropertiesMode.OVERRIDE
    }

    public PropertySettings(Map<String, ?> map) {
        this.properties = new Properties()
        this.resolver.systemPropertiesMode = SystemPropertiesMode.OVERRIDE
        properties.putAll(map)
    }

    public PropertySettings(Resource resource) {
        if (!resource.exist)
            throw new IllegalArgumentException("Inexisting resource: " + resource)
        this.properties = new Properties()
        this.resolver.systemPropertiesMode = SystemPropertiesMode.OVERRIDE
        resource.withInput { InputStream is ->
            try {
                properties.load(is)
            } catch (IOException e) {
                throw new IllegalArgumentException("Unreadable resource: " + resource)
            }
        }
    }

    public File getPath(String key) {
        return new File(getRequired(key))
    }

    public String getString(String key) {
        return getRequired(key)
    }

    public String getString(String key, String defaultValue) {
        String v = resolve(key)
        return v == null ? defaultValue : v
    }

    public List<String> getStrings(String key) {
        return getRequired(key).split(",|;")
    }

    public List<String> getStrings(String key, String... defaultValues) {
        String v = resolve(key)
        return v == null ? defaultValues : v.split(",|;")
    }

    public Resource getResource(String key) {
        return Resource.resource(getRequired(key))
    }

    private String getRequired(String key) throws MissingPropertySettingException {
        String v = resolve(key)
        if (v == null)
            throw new MissingPropertySettingException(key)
        return v
    }

    public Properties getProperties() {
        return properties
    }

    @Override
    public String toString() {
        return properties.toString()
    }

    private String resolve(String key) {
        return resolver.resolve(key, properties)
    }

    public long getLong(String key) {
        return Long.parseLong(getRequired(key))
    }

    public long getLong(String key, long defaultValue) {
        String v = resolve(key)
        return v == null ? defaultValue : Long.parseLong(v)
    }

    public int getInt(String key) {
        return Integer.parseInt(getRequired(key))
    }

    public int getInt(String key, int defaultValue) {
        String v = resolve(key)
        return v == null ? defaultValue : Integer.parseInt(v)
    }

    public boolean getBoolean(String key) {
        return Boolean.valueOf(getRequired(key))
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String v = resolve(key)
        return v == null ? defaultValue : Boolean.valueOf(v)
    }

    public <E extends Enum<E>> E getEnum(Class<E> type, String key) {
        return Enum.valueOf(type, getRequired(key))
    }

    public <E extends Enum<E>> E getEnum(Class<E> type, String key, E defaultValue) {
        String v = resolve(key)
        return v == null ? defaultValue : Enum.valueOf(type, v)
    }

    public boolean has(String key) {
        return resolve(key) != null
    }
}
