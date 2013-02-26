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

final class PropertyPlaceholderResolver {

    static final String DEFAULT_PLACEHOLDER_PREFIX = '${'
    static final String DEFAULT_PLACEHOLDER_SUFFIX = '}'

    private final String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX
    private final String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX

    boolean ignoreSystemEnvironment = false
    boolean ignoreUnresolvablePlaceholders = false
    SystemPropertiesMode systemPropertiesMode = SystemPropertiesMode.FALLBACK

    Properties resolveAll(Collection<Properties> properties) {
        Properties p = new Properties()
        Properties all = new Properties()
        properties.each { all.putAll(it) }
        all.stringPropertyNames().each { String key ->
            String v = parseStringValue(all, all.getProperty(key), new HashSet<String>())
            String k = parseStringValue(all, key, new HashSet<String>())
            p.setProperty(k, v)
        }
        return p
    }

    private String parseStringValue(Properties properties, String strVal, Set<String> visitedPlaceholders) {
        StringBuilder buf = new StringBuilder(strVal)
        int startIndex = strVal.indexOf(this.placeholderPrefix)
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(buf, startIndex)
            if (endIndex != -1) {
                String placeholder = buf.substring(startIndex + this.placeholderPrefix.length(), endIndex)
                if (!visitedPlaceholders.add(placeholder))
                    throw new IllegalStateException("Circular placeholder reference '" + placeholder + "' in property definitions")
                placeholder = parseStringValue(properties, placeholder, visitedPlaceholders)
                String propVal = resolvePlaceholder(properties, placeholder, this.systemPropertiesMode)
                if (propVal != null) {
                    propVal = parseStringValue(properties, propVal, visitedPlaceholders)
                    buf.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal)
                    startIndex = buf.indexOf(this.placeholderPrefix, startIndex + propVal.length())
                } else if (this.ignoreUnresolvablePlaceholders)
                    startIndex = buf.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length())
                else
                    throw new IllegalStateException("Could not resolve placeholder '" + placeholder + "'")
                visitedPlaceholders.remove(placeholder)
            } else
                startIndex = -1
        }
        return buf.toString()
    }

    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + this.placeholderPrefix.length()
        int withinNestedPlaceholder = 0
        while (index < buf.length()) {
            if (substringMatch(buf, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--
                    index = index + this.placeholderSuffix.length()
                } else
                    return index
            } else if (substringMatch(buf, index, this.placeholderPrefix)) {
                withinNestedPlaceholder++
                index = index + this.placeholderPrefix.length()
            } else
                index++
        }
        return -1
    }

    String merge(String line, Properties properties) {
        return parseStringValue(properties, line, new HashSet<String>())
    }

    String resolve(String placeholder, Properties properties) {
        return resolvePlaceholder(properties, placeholder, systemPropertiesMode)
    }

    private String resolvePlaceholder(Properties properties, String placeholder, SystemPropertiesMode mode) {
        String propVal = null
        if (mode == SystemPropertiesMode.OVERRIDE)
            propVal = resolveSystemProperty(placeholder)
        if (propVal == null)
            propVal = properties.getProperty(placeholder)
        if (propVal == null && mode == SystemPropertiesMode.FALLBACK)
            propVal = resolveSystemProperty(placeholder)
        return propVal
    }

    private String resolveSystemProperty(String key) {
        try {
            String value = System.getProperty(key)
            if (value == null && !this.ignoreSystemEnvironment)
                value = System.getenv(key)
            return value
        }
        catch (RuntimeException ignored) {
            return null
        }
    }

    private static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        for (int j = 0; j < substring.length(); j++) {
            int i = index + j
            if (i >= str.length() || str.charAt(i) != substring.charAt(j))
                return false
        }
        return true;
    }

}
