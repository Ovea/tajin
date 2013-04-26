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
package com.ovea.tajin.framework.prop;

import com.ovea.tajin.framework.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public final class PropertySettings {

    private final PropertyPlaceholderResolver resolver = new PropertyPlaceholderResolver();
    private final Properties properties;

    public PropertySettings() {
        this(new Properties());
    }

    public PropertySettings(Properties properties) {
        this.properties = properties;
        this.resolver.setSystemPropertiesMode(SystemPropertiesMode.OVERRIDE);
    }

    public PropertySettings(Resource resource) {
        if (!resource.isExist())
            throw new IllegalArgumentException("Inexisting resource: " + resource);
        InputStream is = resource.getInput();
        this.properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unreadable resource: " + resource);
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
        this.resolver.setSystemPropertiesMode(SystemPropertiesMode.OVERRIDE);
    }

    public File getPath(String key) {
        return new File(getRequired(key));
    }

    public File getPath(String key, File def) {
        String v = resolve(key);
        return v == null ? def : new File(v);
    }

    public String getString(String key) {
        return getRequired(key);
    }

    public String getString(String key, String def) {
        String v = resolve(key);
        return v == null ? def : v;
    }

    public List<String> getStrings(String key) {
        return Arrays.asList(getRequired(key).split(",|;"));
    }

    public List<String> getStrings(String key, String... def) {
        String v = resolve(key);
        return Arrays.asList((v == null ? def : v.split(",|;")));
    }

    public Resource getResource(String key) {
        return Resource.resource(getRequired(key));
    }

    private String getRequired(String key) throws MissingPropertySettingException {
        String v = resolve(key);
        if (v == null)
            throw new MissingPropertySettingException(key);
        return v;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return properties.toString();
    }

    private String resolve(String key) {
        return resolver.resolve(key, properties);
    }

    public long getLong(String key) {
        return Long.parseLong(getRequired(key));
    }

    public long getLong(String key, long def) {
        String v = resolve(key);
        return v == null ? def : Long.parseLong(v);
    }

    public int getInt(String key) {
        return Integer.parseInt(getRequired(key));
    }

    public int getInt(String key, int def) {
        String v = resolve(key);
        return v == null ? def : Integer.parseInt(v);
    }

    public boolean getBoolean(String key) {
        return Boolean.valueOf(getRequired(key));
    }

    public boolean getBoolean(String key, boolean def) {
        String v = resolve(key);
        return v == null ? def : Boolean.valueOf(v);
    }

    public <E extends Enum<E>> E getEnum(Class<E> type, String key) {
        return Enum.valueOf(type, getRequired(key));
    }

    public <E extends Enum<E>> E getEnum(Class<E> type, String key, E def) {
        String v = resolve(key);
        return v == null ? def : Enum.valueOf(type, v);
    }

    public boolean has(String key) {
        return resolve(key) != null;
    }
}
