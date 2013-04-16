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
package com.ovea.tajin.support.eclipselink

import com.ovea.tajin.util.LocaleUtil
import org.eclipse.persistence.internal.helper.ClassConstants
import org.eclipse.persistence.mappings.DatabaseMapping
import org.eclipse.persistence.mappings.converters.Converter
import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping
import org.eclipse.persistence.sessions.Session

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class LocaleConverter implements Converter {

    @Override
    Object convertDataValueToObjectValue(Object dataValue, Session session) {
        return dataValue ? LocaleUtil.valueOf(dataValue as String, Locale.US) : null
    }

    @Override
    Object convertObjectValueToDataValue(Object objectValue, Session session) {
        return objectValue as String
    }

    @Override
    boolean isMutable() {
        return false
    }

    @Override
    void initialize(DatabaseMapping mapping, Session session) {
        if (mapping.directToFieldMapping) {
            AbstractDirectMapping directMapping = (AbstractDirectMapping) mapping
            if (directMapping.getFieldClassification() == null) {
                directMapping.setFieldClassification(ClassConstants.STRING)
            }
        }
    }
}
