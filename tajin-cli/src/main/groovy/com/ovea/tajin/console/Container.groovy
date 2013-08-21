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

import org.apache.catalina.ssi.SSIServlet
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.servlet.ServletHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.servlet.ServletMapping

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
        connector.setPort(port())
        jetty.addConnector(connector)

        HandlerList handlers = new HandlerList()

        ResourceHandler resource_handler = new ResourceHandler()
        resource_handler.directoriesListed = true
        resource_handler.welcomeFiles = ['index.html']
        resource_handler.resourceBase = webappRoot().absolutePath

        handlers.addHandler(resource_handler)

        ServletHandler servletHandler = new ServletHandler()
        ServletHolder holder = new ServletHolder()
        holder.name = SSIServlet.class.name
        holder.className = SSIServlet.class.getName()
        holder.setInitParameter('inputEncoding', 'UTF-8')
        holder.setInitParameter('outputEncoding', 'UTF-8')
        holder.setInitParameter('buffered', 'false')
        holder.setInitParameter('expires', '0')

        servletHandler.setServlets([holder] as ServletHolder[])

        ServletMapping mapping = new ServletMapping();
        mapping.pathSpec = '/*.html';
        mapping.servletName = SSIServlet.class.name

        servletHandler.servletMappings = [mapping]

        handlers.addHandler(servletHandler)

        jetty.setHandler(handlers);
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
