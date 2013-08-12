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

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-06-06
 */
interface JobScheduler {

    /**
     * Schedules a persistent job in the future. The job being scheduled is returned after being saved
     */
    Job schedule(String jobName, Date time, Map<String, ?> data)

    /**
     * Submit a non persistent job in the future which will not be retried if failed. Uses the executor thread pool.
     */
    Job submit(String jobName, Date time, Map<String, ?> data)

    /**
     * Schedules a persistent job for execution as soon as possible. The job being scheduled is returned after being saved
     */
    Job schedule(String jobName, Map<String, ?> data)

    /**
     * Submit a non persistent job in the future which will not be retried if failed. Uses the executor thread pool. Task will be executed as soon as possible
     */
    Job submit(String jobName, Map<String, ?> data)

    /**
     * Cancels jobs by ids
     */
    void cancel(Collection<String> jobIds)

    interface OnError {

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
                org.slf4j.LoggerFactory.getLogger(JobScheduler.OnError).error("Error executing ${job} : ${t.message}", t)
            }
        }

        static final OnError IGNORE = new OnError() {
            @Override
            void onError(Job job, Throwable t) throws Throwable {
            }
        }
    }

}
