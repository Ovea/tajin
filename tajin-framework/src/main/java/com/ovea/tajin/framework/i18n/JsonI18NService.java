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
package com.ovea.tajin.framework.i18n;

import com.ovea.tajin.framework.io.Resource;
import com.ovea.tajin.framework.template.TemplateResolverException;
import groovy.json.JsonSlurper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class JsonI18NService extends I18NServiceSkeleton {

    private static final Logger LOGGER = Logger.getLogger(JsonI18NService.class.getName());

    public JsonI18NService(String bundleName) {
        super(bundleName);
    }

    @Override
    I18NBundle newBundle(String bundleName, Locale locale) {
        return new I18NBundleSkeleton(bundleName, locale, getMissingKeyBehaviour()) {
            volatile Properties mappings;

            @Override
            public List<String> keys() {
                return new LinkedList<>(getMappings().stringPropertyNames());
            }

            @Override
            public boolean hasKey(String key) {
                return getMappings().containsKey(key);
            }

            @Override
            String getValue(String key) throws MissingMessageException {
                return getMappings().getProperty(key);
            }

            private Properties getMappings() {
                if (this.mappings == null) {
                    if (isDebug()) {
                        return load();
                    }
                    this.mappings = load();
                }
                return this.mappings;
            }

            private Properties load() {
                Properties all = new Properties();
                load(all, "");
                String str = locale().toString();
                if (str.length() >= 2)
                    load(all, str.substring(0, 2));
                if (str.length() >= 5)
                    load(all, str.substring(0, 5));
                return all;
            }

            private void load(Properties target, String locale) {
                Resource resource = null;
                try {
                    String b = bundleName();
                    int pos = b.lastIndexOf('.');
                    if (pos == -1) {
                        throw new TemplateResolverException("Illegal bundle name: extension needed");
                    }
                    resource = Resource.resource(b.substring(0, pos) + (locale.length() > 0 ? "_" + locale : locale) + b.substring(pos));
                    String content = resource.getText();
                    if (content.length() > 0) {
                        //noinspection unchecked
                        parse(target, (Map<String, Object>) new JsonSlurper().parseText(content), "");
                    }
                } catch (RuntimeException e) {
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("Cannot find resource " + resource);
                }
            }

        };

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
