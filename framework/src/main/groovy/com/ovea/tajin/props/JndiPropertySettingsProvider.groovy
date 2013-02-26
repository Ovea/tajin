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

import javax.inject.Inject
import javax.inject.Provider
import javax.naming.Context
import javax.naming.InitialContext
import javax.naming.NamingException

class JndiPropertySettingsProvider implements Provider<PropertySettings> {

    @Inject
    Context context

    private final Collection<String> jndis

    JndiPropertySettingsProvider(Collection<String> jndis) {
        this.jndis = jndis
        try {
            this.context = new InitialContext()
        } catch (NamingException e) {
            throw new RuntimeException(e.getMessage(), e)
        }
    }

    @Override
    PropertySettings get() {
        try {
            return new PropertySettings(new PropertyPlaceholderResolver().resolveAll(jndis.collect { Properties.class.cast(context.lookup(it)) }))
        } catch (NamingException e) {
            throw new RuntimeException(e.getMessage(), e)
        }
    }
}
