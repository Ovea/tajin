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
package com.ovea.tajin.framework.scheduling

import com.ovea.tajin.framework.scheduling.JobScheduler.OnError
import org.slf4j.LoggerFactory

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
interface JobScheduler {

    /**
     * Schedules a job in the future
     */
    void schedule(String jobName, Date time, Map<String, ?> data)

    /**
     * Schedules a job for execution as soon as possible
     */
    void schedule(String jobName, Map<String, ?> data)

    static interface OnError {

        void onError(Job job, Throwable t) throws Throwable

        static final OnError RETHROW = new OnError() {
            @Override
            void onError(Job job, Throwable t) throws Throwable {
                throw t
            }
        }

        static final OnError LOG = new OnError() {
            @Override
            void onError(Job job, Throwable t) throws Throwable {
                LoggerFactory.getLogger(OnError).error("Error executing ${job} : ${t.message}", t)
            }
        }

        static final OnError IGNORE = new OnError() {
            @Override
            void onError(Job job, Throwable t) throws Throwable {
            }
        }
    }

}
