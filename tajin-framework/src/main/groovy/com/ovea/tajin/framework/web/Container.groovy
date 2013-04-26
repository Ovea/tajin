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
package com.ovea.tajin.framework.web

import com.google.inject.servlet.GuiceFilter
import com.ovea.tajin.framework.prop.PropertySettings
import com.ovea.tajin.framework.support.guice.GuiceListener
import org.eclipse.jetty.jmx.MBeanContainer
import org.eclipse.jetty.server.AsyncNCSARequestLog
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.RequestLogHandler
import org.eclipse.jetty.servlet.ServletContextHandler

import javax.servlet.DispatcherType
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener
import java.lang.management.ManagementFactory

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-26
 */
class Container {

    private final Server server

    Container(PropertySettings settings) {
        HandlerCollection handlerCollection = new HandlerCollection()
        ServletContextHandler context = new ServletContextHandler(handlerCollection, settings.getString('server.context', '/'))
        handlerCollection.addHandler(context)
        settings.getPath('logging.request.folder', null)?.with { File dir ->
            RequestLogHandler requestLogHandler = new RequestLogHandler(
                _requestLog: new AsyncNCSARequestLog(
                    filename: "${dir.absolutePath}/request.yyyy_mm_dd.log",
                    filenameDateFormat: 'yyyy_MM_dd',
                    retainDays: settings.getInt('logging.request.retainDays', 30),
                    extended: settings.getBoolean('logging.request.extended', false),
                    logCookies: settings.getBoolean('logging.request.cookies', false),
                    append: true,
                    logTimeZone: 'GMT',
                    ignorePaths: settings.getStrings('logging.request.ignores', ''),
                )
            )
            handlerCollection.addHandler(requestLogHandler)
        }
        server = new Server(settings.getInt('server.port', 8080))
        server.stopAtShutdown = true
        server.setHandler(handlerCollection)
        server.addBean(new MBeanContainer(ManagementFactory.platformMBeanServer))
        context.setInitParameter('org.eclipse.jetty.servlet.SessionIdPathParameterName', 'none')
        context.setInitParameter('org.eclipse.jetty.servlet.SessionCookie', settings.getString('session.cookie.name', 'id'))
        InternalWebModule module = new InternalWebModule(settings)
        context.addFilter(GuiceFilter, '/*', EnumSet.allOf(DispatcherType))
        context.addEventListener(new GuiceListener([module]))
        context.addEventListener(module)
        context.addEventListener(new HttpSessionListener() {
            @Override
            void sessionCreated(HttpSessionEvent se) {
                se.session.setMaxInactiveInterval(settings.getInt('session.timeout', 5))
            }

            @Override
            void sessionDestroyed(HttpSessionEvent se) {

            }
        })
    }

    void start() {
        server.start()
    }

    void stop() {
        server.stop()
    }
}
