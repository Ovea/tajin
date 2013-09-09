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

import com.google.common.base.Function
import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.google.common.collect.Multimap
import com.google.common.collect.TreeMultimap

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-08-12
 */
final class Settings {

    private final PropertyPlaceholderResolver resolver = new PropertyPlaceholderResolver();
    private final Properties properties;

    public Settings() {
        this(new Properties());
    }

    public Settings(Map map) {
        this()
        map?.each { k, v -> properties.setProperty(k as String, v as String) }
    }

    public Settings(Properties properties) {
        this.properties = properties;
        this.resolver.setSystemPropertiesMode(SystemPropertiesMode.OVERRIDE);
    }

    public Settings(Resource resource) {
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

    public Settings plus(Map<String, String> m) {
        properties.putAll(m);
        return this;
    }

    public File getFile(String key) { new File(getRequired(key)); }

    public File getFile(String key, File fallback) {
        String v = resolve(key);
        return v == null ? fallback : new File(v);
    }

    public String getString(String key) { getRequired(key); }

    public String getString(String key, String fallback) {
        String v = resolve(key);
        return v == null ? fallback : v;
    }

    public List<String> getStrings(String key) { split(getRequired(key)); }

    public List<String> getStrings(String key, String... fallback) {
        String v = resolve(key);
        return v == null ? Arrays.asList(fallback) : split(v);
    }

    public List<String> getStrings(String key, List<String> fallback) {
        String v = resolve(key);
        return v == null ? fallback : split(v);
    }

    private List<String> split(String s) {
        return Lists.newArrayList(Iterables.transform(Lists.newArrayList(s.split(",|;")), new Function<String, String>() {
            @Override
            public String apply(String input) {
                return input.trim();
            }
        }));
    }

    public Resource getResource(String key) { Resource.parse(getRequired(key)); }

    public Resource getResource(String key, String fallback) {
        String v = resolve(key);
        return Resource.parse(v == null ? fallback : v);
    }

    private String getRequired(String key) throws MissingPropertySettingException {
        String v = resolve(key);
        if (v == null)
            throw new MissingPropertySettingException(key);
        return v;
    }

    public Properties getProperties() { properties; }

    @Override
    public String toString() { properties.toString(); }

    public long getLong(String key) { Long.parseLong(getRequired(key)); }

    public long getLong(String key, long fallback) {
        String v = resolve(key);
        return v == null ? fallback : Long.parseLong(v);
    }

    public int getInt(String key) { Integer.parseInt(getRequired(key)); }

    public int getInt(String key, int fallback) {
        String v = resolve(key);
        return v == null ? fallback : Integer.parseInt(v);
    }

    public boolean getBoolean(String key) { Boolean.valueOf(getRequired(key)); }

    public boolean getBoolean(String key, boolean fallback) {
        String v = resolve(key);
        return v == null ? fallback : Boolean.valueOf(v);
    }

    public <E extends Enum<E>> E getEnum(Class<? extends Enum> type, String key) { (E) Enum.valueOf(type, getRequired(key)) }

    public <E extends Enum<E>> E getEnum(Class<? extends Enum> type, String key, E fallback) {
        String v = resolve(key);
        return v == null ? fallback : (E) Enum.valueOf(type, v);
    }

    public boolean has(String key) { resolve(key) != null; }

    public List<Map<String, String>> getList(String prefix) {
        Multimap<Integer, String> map = TreeMultimap.create();
        prefix = prefix + '.';
        for (String s : properties.stringPropertyNames()) {
            if (s.startsWith(prefix)) {
                int end = s.indexOf('.', prefix.length());
                map.put(Integer.parseInt(s.substring(prefix.length(), end)), s.substring(end + 1));
            }
        }
        List<Map<String, String>> list = new ArrayList<Map<String, String>>(map.size());
        for (Integer i : new TreeSet<Integer, String>(map.keySet())) {
            Map<String, String> o = new HashMap<>();
            for (String prop : map.get(i)) {
                String v = resolve(prefix + i + '.' + prop);
                o.put(prop, v == null ? null : v.trim());
            }
            list.add(o);
        }
        return list;
    }

    private String resolve(String key) { resolver.resolve(key, properties); }

}
