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

import com.ovea.tajin.framework.async.guava.AsyncEventBus
import com.ovea.tajin.framework.async.guava.EventBus
import com.ovea.tajin.framework.core.Settings

import javax.annotation.PostConstruct
import javax.inject.Inject
import java.util.concurrent.Executor
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-05
 */
@javax.inject.Singleton
class ConfiguredEventBus implements Dispatcher {

    private static final Logger LOGGER = Logger.getLogger(Dispatcher.name)

    @Inject @AsyncExecutor Executor executor
    @Inject Settings settings

    private EventBus eventBus

    @PostConstruct
    void init() {
        if (settings.getBoolean('tajin.async.dispatcher.enabled', true)) {
            eventBus = new AsyncEventBus(executor)
        }
    }

    void register(Object o) {
        LOGGER.info('+subscriber ' + o.class.name)
        eventBus.register(o)
    }

    void unregister(Object o) {
        LOGGER.info('-subscriber ' + o.class.name)
        eventBus.unregister(o)
    }

    @Override
    void broadcast(Object event) { eventBus.post(event) }

}
