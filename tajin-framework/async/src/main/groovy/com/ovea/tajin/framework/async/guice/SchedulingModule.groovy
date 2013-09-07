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
package com.ovea.tajin.framework.async.guice

import com.google.inject.AbstractModule
import com.ovea.tajin.framework.async.DefaultJobScheduler
import com.ovea.tajin.framework.async.JobListener
import com.ovea.tajin.framework.async.JobRepository
import com.ovea.tajin.framework.async.JobScheduler
import com.ovea.tajin.framework.core.Settings

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-05
 */
class SchedulingModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(Settings)
        requireBinding(JobRepository)
        requireBinding(JobListener)
        bind(JobScheduler).to(DefaultJobScheduler)
    }
}
