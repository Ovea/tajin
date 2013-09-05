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
package com.ovea.tajin.framework.i18n

import com.ovea.tajin.framework.core.Resource
import groovy.json.JsonSlurper

import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class JsonI18NBundlerProvider extends I18NBundlerProviderSkeleton {

    private static final Logger LOGGER = Logger.getLogger(JsonI18NBundlerProvider.class.getName());

    public JsonI18NBundlerProvider(String bundleName) {
        super(bundleName);
    }

    @Override
    I18NBundle newBundle(String bundleName, Locale locale) {
        return new I18NBundleSkeleton(bundleName, locale, getMissingKeyBehaviour()) {
            volatile Properties _bundle;

            @Override
            public List<String> getKeys() { getBundle().stringPropertyNames() as List }

            @Override
            public boolean contains(String key) { getBundle().containsKey(key); }

            @Override
            String doGetValue(String key) { getBundle().getProperty(key); }

            private Properties getBundle() {
                if (_bundle != null) return _bundle
                if (JsonI18NBundlerProvider.this.cache) {
                    _bundle = load();
                    return _bundle;
                } else {
                    return load();
                }
            }

            private Properties load() {
                Properties all = new Properties();
                load(bundleName, all, "");
                String str = locale.toString();
                if (str.length() >= 2)
                    load(bundleName, all, str.substring(0, 2));
                if (str.length() >= 5)
                    load(bundleName, all, str.substring(0, 5));
                return all;
            }


        };

    }

    String loadAsjson(Resource resource) { resource.getText() }

    private void load(String bundleName, Properties target, String lc) {
        Resource resource = null;
        try {
            String b = bundleName;
            int pos = b.lastIndexOf('.');
            if (pos == -1) {
                throw new IllegalArgumentException("Illegal bundle name: extension needed");
            }
            resource = Resource.parse(b.substring(0, pos) + (lc.length() > 0 ? "_" + lc : lc) + b.substring(pos));
            String content = loadAsjson(resource);
            if (content.length() > 0) {
                //noinspection unchecked
                parse(target, (Map<String, Object>) new JsonSlurper().parseText(content), "");
            }
        } catch (RuntimeException ignored) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Cannot find resource " + resource);
        }
    }

    private static void parse(Properties target, Map<String, Object> object, String completeKey) {
        for (String key : object.keySet()) {
            Object o = object.get(key);
            String ck = completeKey.length() > 0 ? completeKey + "." + key : key;
            if (o instanceof Map) {
                //noinspection unchecked
                parse(target, (Map<String, Object>) o, ck);
            } else if (o instanceof Collection) {
                target.put(ck, o.toString());
            } else if (o != null) {
                target.put(ck, String.valueOf(o));
            }
            // else o == null => ignore property
        }
    }

}
