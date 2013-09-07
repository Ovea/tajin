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

import groovy.transform.ToString

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
@ToString(includeNames = true, excludes = ['data'])
class ScheduledJob {

    static final int INFINITE_RETRY = -1

    /**
     * Job name, required
     */
    String name

    /**
     * Optional data for the job to be executed. Should be serializable.
     */
    Map data = [:]

    /**
     * Wanted job start date, default to current time
     */
    Date startDate = new Date()

    /**
     * Set wheter the job should be persisted for reliability. False by default.
     */
    boolean persisted

    /**
     * Max retry count for the job execution if fails. Default to INFINIT_RETRY.
     */
    int maxRetry = INFINITE_RETRY

    /**
     * Duration to wait in seconds before attempting a retry if the job fails
     */
    long retryDelaySecs = 300

}
