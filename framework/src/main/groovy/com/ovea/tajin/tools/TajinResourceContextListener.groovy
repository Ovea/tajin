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
package com.ovea.tajin.tools

import com.ovea.tajin.TajinConfig
import com.ovea.tajin.io.Resource
import com.ovea.tajin.resources.TajinResourceManager

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-01-08
 */
class TajinResourceContextListener implements ServletContextListener {

    private static final String PARAM_CONFIG = TajinResourceContextListener.name + '.config'
    private TajinResourceManager resourceManager

    @Override
    void contextInitialized(ServletContextEvent sce) {
        String configLocation = sce.servletContext.getInitParameter(PARAM_CONFIG)
        File webapp = new File(sce.servletContext.getRealPath('.'))
        Resource config = configLocation ? Resource.resource(webapp, configLocation) : Resource.file(new File(webapp, TajinConfig.DEFAULT_LOCATION))
        TajinConfig tajinConfig = new TajinConfig(webapp, config)
        resourceManager = new TajinResourceManager(tajinConfig)
        resourceManager.buid()
        resourceManager.watch()
    }

    @Override
    void contextDestroyed(ServletContextEvent sce) {
        resourceManager.unwatch()
    }

}
