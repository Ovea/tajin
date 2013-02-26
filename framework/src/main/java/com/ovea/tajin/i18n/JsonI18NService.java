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
package com.ovea.tajin.i18n;

import com.ovea.tajin.io.Resource;
import com.ovea.tajin.json.JSONException;
import com.ovea.tajin.json.JSONObject;
import com.ovea.tajin.json.JSONType;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class JsonI18NService extends I18NServiceSkeleton {

    private static final Logger LOGGER = Logger.getLogger(JsonI18NService.class.getName());

    public JsonI18NService(String bundleName, ClassLoader classLoader) {
        super(bundleName, classLoader);
    }

    @Override
    I18NBundle newBundle(String bundleName, ClassLoader classLoader, Locale locale) {
        return new I18NBundleSkeleton(bundleName, classLoader, locale, getMissingKeyBehaviour()) {
            volatile Properties mappings;

            @Override
            public List<String> keys() {
                return new LinkedList<String>(getMappings().stringPropertyNames());
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
                    resource = Resource.classpath(loader(), bundleName() + (locale.length() > 0 ? "_" + locale : locale) + ".json");
                    String content = resource.getText();
                    if (content.length() > 0) {
                        parse(target, new JSONObject(content), "");
                    }
                } catch (RuntimeException e) {
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("Cannot find resource " + resource);
                }
            }

        };

    }

    private static void parse(Properties target, JSONObject object, String completeKey) throws JSONException {
        for (String key : object) {
            JSONType o = object.get(key);
            String ck = completeKey.length() > 0 ? completeKey + "." + key : key;
            if (o.isObject()) {
                parse(target, (JSONObject) o, ck);
            } else if (o.isValue()) {
                target.put(ck, String.valueOf(o.asValue()));
            } else if (o.isArray()) {
                target.put(ck, o.toString());
            } else {
                // o.isNull() == true => ignore property
            }
        }
    }

}
