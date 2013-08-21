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
package com.ovea.tajin.console

import org.apache.catalina.ssi.SSIFilter
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

import javax.servlet.DispatcherType

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
public class Container {

    final Properties properties
    final Server jetty

    Container(Properties properties) {
        this.properties = properties
        jetty = new Server()

        ServerConnector connector = new ServerConnector(jetty, new HttpConnectionFactory())
        connector.port = port()
        jetty.addConnector(connector)

        HandlerCollection handlers = new HandlerCollection()

        // create a programmatic context for defined context path
        ServletContextHandler context = new ServletContextHandler(
            contextPath: '',
            resourceBase: webappRoot().absolutePath,
            welcomeFiles: ['index.html', 'home.html'] as String[]
        )

        handlers.addHandler(context)

        new FilterHolder(SSIFilter).with {
            it.setInitParameter('isVirtualWebappRelative', 'true')
            it.setInitParameter('contentType', 'text/html')
            it.setInitParameter('expires', '0')
            context.addFilter(it, "/*", EnumSet.allOf(DispatcherType))
        }

        new ServletHolder(DefaultServlet).with {
            it.setInitParameter('dirAllowed', 'true')
            context.addServlet(it, '/*')
        }

        jetty.handler = handlers

    }

    void start() {
        jetty.start();
    };

    void stop() {
        jetty.stop();
    };

    int port() {
        return getProperty('port') as int
    };

    boolean isRunning() {
        return jetty.isRunning()
    };

    String contextPath() {
        return getProperty('context')
    };

    File webappRoot() {
        return getProperty('webappRoot') as File
    };

    boolean hasProperty(String property) {
        return !properties.getProperty(property)
    };

    String getProperty(String property) {
        return properties.get(property)
    };

}
