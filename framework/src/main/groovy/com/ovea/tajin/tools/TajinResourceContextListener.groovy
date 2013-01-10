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

import com.ovea.tajin.Tajin
import com.ovea.tajin.io.Resource

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-01-08
 */
class TajinResourceContextListener implements ServletContextListener {

    private static final String PARAM_CONFIG = TajinResourceContextListener.name + '.config'
    private Tajin tajin

    @Override
    void contextInitialized(ServletContextEvent sce) {
        String configLocation = sce.servletContext.getInitParameter(PARAM_CONFIG)
        File webapp = new File(sce.servletContext.getRealPath('.'))
        Resource config = configLocation ? Resource.resource(webapp, configLocation) : Resource.file(new File(webapp, Tajin.DEFAULT_CONFIG_LOCATION))
        tajin = Tajin.load(webapp, config)
        tajin.build()
        tajin.watch()
    }

    @Override
    void contextDestroyed(ServletContextEvent sce) {
        tajin.unwatch()
    }

}
