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

import javax.naming.InitialContext
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-01-08
 */
class TajinResourceContextListener implements ServletContextListener {

    private static final String PARAM_CONFIG = TajinResourceContextListener.name + '.config'
    private static final String PARAM_CONTEXT = TajinResourceContextListener.name + '.context'
    private Tajin tajin

    @Override
    void contextInitialized(ServletContextEvent sce) {
        File webapp = new File(sce.servletContext.getRealPath('.'))
        String configLocation = System.getProperty(PARAM_CONFIG, sce.servletContext.getInitParameter(PARAM_CONFIG) ?: "file://${new File(webapp, Tajin.DEFAULT_CONFIG_LOCATION).absolutePath}")
        Map<String, ?> p = new HashMap<>()
        String ctxJndi = sce.servletContext.getInitParameter(PARAM_CONTEXT)
        if (ctxJndi) {
            p.putAll(new InitialContext().lookup(ctxJndi) as Map)
        }
        tajin = Tajin.load(webapp, Resource.resource(webapp, configLocation), p)
        tajin.build()
        tajin.watch()
    }

    @Override
    void contextDestroyed(ServletContextEvent sce) {
        tajin.unwatch()
    }

}
