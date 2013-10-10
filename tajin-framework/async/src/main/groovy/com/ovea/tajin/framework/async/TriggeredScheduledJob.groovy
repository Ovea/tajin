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
package com.ovea.tajin.framework.async

import com.ovea.tajin.framework.core.Uuid
import groovy.transform.ToString

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
@ToString(includeNames = true)
class TriggeredScheduledJob {

    /**
     * ID, auto-generated if not set
     */
    String id = Uuid.newUUID

    ScheduledJob source

    /**
     * Current retry count
     */
    int currentRetry

    /**
     * Date of last retry
     */
    Date lastTry

    Date nextTry

    Date completionDate

    boolean isRetryable() { lastTry == null || completionDate == null && (source.maxRetry == ScheduledJob.INFINITE_RETRY || currentRetry < source.maxRetry) }

    void preventReschedule() {
        completionDate = new Date()
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false
        TriggeredScheduledJob that = (TriggeredScheduledJob) o
        if (id != that.id) return false
        return true
    }

    @Override
    int hashCode() { id.hashCode() }
}
