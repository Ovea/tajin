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

import org.eclipse.persistence.internal.helper.ClassConstants
import org.eclipse.persistence.mappings.DatabaseMapping
import org.eclipse.persistence.mappings.converters.Converter
import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping
import org.eclipse.persistence.sessions.Session
import org.joda.time.LocalDate

import java.sql.Date

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class LocalDateConverter implements Converter {

    public Object convertDataValueToObjectValue(Object dataValue, Session arg1) {
        if (dataValue == null) {
            return null;
        }
        if (dataValue instanceof Date)
            return new LocalDate(dataValue)
        else
            throw new IllegalStateException("Converstion exception, value is not of type java.util.Date")

    }

    public Object convertObjectValueToDataValue(Object objectValue, Session arg1) {
        if (objectValue == null) {
            return null
        }
        if (objectValue instanceof LocalDate) {
            return new Date(((LocalDate) objectValue).toDateTimeAtStartOfDay().millis)
        } else
            throw new IllegalStateException("Converstion exception, value is not of type org.joda.time.LocalDate")

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
                directMapping.setFieldClassification(ClassConstants.SQLDATE)
            }
        }
    }
}
