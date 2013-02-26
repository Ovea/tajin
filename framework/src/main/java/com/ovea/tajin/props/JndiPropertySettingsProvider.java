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
package com.ovea.tajin.props;

import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public final class JndiPropertySettingsProvider implements Provider<PropertySettings> {

    @Inject
    Context context;

    private final String[] jndis;

    public JndiPropertySettingsProvider(String... jndis) {
        this.jndis = jndis;
        try {
            this.context = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public PropertySettings get() {
        try {
            Properties[] properties = new Properties[jndis.length];
            for (int i = 0; i < properties.length; i++)
                properties[i] = Properties.class.cast(context.lookup(jndis[i]));
            return new PropertySettings(new PropertyPlaceholderResolver().resolveAll(properties));
        } catch (NamingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
