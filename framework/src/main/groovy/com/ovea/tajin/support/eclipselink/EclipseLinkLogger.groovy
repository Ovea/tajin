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

import org.eclipse.persistence.logging.AbstractSessionLog
import org.eclipse.persistence.logging.SessionLog
import org.eclipse.persistence.logging.SessionLogEntry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class EclipseLinkLogger extends AbstractSessionLog {

    static final String ECLIPSELINK_NAMESPACE = "org.eclipse.persistence.logging"
    static final String DEFAULT_CATEGORY = "default"
    static final String DEFAULT_ECLIPSELINK_NAMESPACE = ECLIPSELINK_NAMESPACE + "." + DEFAULT_CATEGORY

    static enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, OFF
    }

    static final Map<Integer, LogLevel> mapLevels = [
        (SessionLog.ALL): LogLevel.TRACE,
        (SessionLog.FINEST): LogLevel.TRACE,
        (SessionLog.FINER): LogLevel.TRACE,
        (SessionLog.FINE): LogLevel.DEBUG,
        (SessionLog.CONFIG): LogLevel.INFO,
        (SessionLog.INFO): LogLevel.INFO,
        (SessionLog.WARNING): LogLevel.WARN,
        (SessionLog.SEVERE): LogLevel.ERROR,
    ]

    private Map<String, Logger> categoryLoggers = new HashMap<>()

    EclipseLinkLogger() {
        createCategoryLoggers()
    }

    @Override
    void log(SessionLogEntry entry) {
        if (!shouldLog(entry.level, entry.nameSpace)) {
            return
        }
        StringBuilder message = new StringBuilder()
        message.append(getSupplementDetailString(entry))
        message.append(formatMessage(entry))
        switch (getLogLevel(entry.level)) {
            case com.zimmer.pmp.logging.EclipseLinkLogger.LogLevel.TRACE:
                getLogger(entry.nameSpace).trace(message.toString())
                break
            case com.zimmer.pmp.logging.EclipseLinkLogger.LogLevel.DEBUG:
                getLogger(entry.nameSpace).debug(message.toString())
                break
            case INFO:
                getLogger(entry.nameSpace).info(message.toString())
                break
            case com.zimmer.pmp.logging.EclipseLinkLogger.LogLevel.WARN:
                getLogger(entry.nameSpace).warn(message.toString())
                break
            case com.zimmer.pmp.logging.EclipseLinkLogger.LogLevel.ERROR:
                getLogger(entry.nameSpace).error(message.toString())
                break
        }
    }

    @Override
    boolean shouldLog(int level, String category) {
        switch (getLogLevel(level)) {
            case com.zimmer.pmp.logging.EclipseLinkLogger.LogLevel.TRACE: return getLogger(category).traceEnabled
            case com.zimmer.pmp.logging.EclipseLinkLogger.LogLevel.DEBUG: return getLogger(category).debugEnabled
            case INFO: return getLogger(category).infoEnabled
            case com.zimmer.pmp.logging.EclipseLinkLogger.LogLevel.WARN: return getLogger(category).warnEnabled
            case com.zimmer.pmp.logging.EclipseLinkLogger.LogLevel.ERROR: return getLogger(category).errorEnabled
        }
        return false
    }

    @Override
    boolean shouldLog(int level) {
        return shouldLog(level, "default")
    }

    /**
     * Return true if SQL logging should log visible bind parameters. If the
     * shouldDisplayData is not set, return false.
     */
    @Override
    boolean shouldDisplayData() {
        return shouldDisplayData != null && shouldDisplayData.booleanValue()
    }

    /**
     * Initialize loggers eagerly
     */
    private void createCategoryLoggers() {
        SessionLog.loggerCatagories.each {addLogger(it, ECLIPSELINK_NAMESPACE + "." + it)}
        // Logger default para cuando no hay categoría.
        addLogger(DEFAULT_CATEGORY, DEFAULT_ECLIPSELINK_NAMESPACE)
    }

    /**
     * INTERNAL: Add Logger to the categoryLoggers.
     */
    private void addLogger(String loggerCategory, String loggerNameSpace) {
        categoryLoggers.put(loggerCategory, LoggerFactory.getLogger(loggerNameSpace))
    }

    /**
     * INTERNAL: Return the Logger for the given category
     */
    private Logger getLogger(String category) {
        return categoryLoggers.get(category != null && category.length() > 0 && this.categoryLoggers.containsKey(category) ? category : DEFAULT_CATEGORY)
    }

    /**
     * Return the corresponding Slf4j Level for a given EclipseLink level.
     */
    private LogLevel getLogLevel(Integer level) {
        return mapLevels.get(level) ?: LogLevel.OFF
    }

}