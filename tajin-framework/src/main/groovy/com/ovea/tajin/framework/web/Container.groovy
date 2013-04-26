package com.ovea.tajin.framework.web

import com.ovea.tajin.framework.prop.PropertySettings
import org.eclipse.jetty.jmx.MBeanContainer
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.servlet.ServletContextHandler

import java.lang.management.ManagementFactory

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-26
 */
class Container {

    final Server server

    Container(PropertySettings settings) {
        HandlerCollection handlerCollection = new HandlerCollection()
        handlerCollection.addHandler(new ServletContextHandler(handlerCollection, settings.getString('server.context', '/')))
        server = new Server(settings.getInt('server.port', 8080))
        server.stopAtShutdown = true
        server.setHandler(handlerCollection)
        server.addBean(new MBeanContainer(ManagementFactory.platformMBeanServer))
    }

    void start() {
        server.start()
    }

    void stop() {
        server.stop()
    }
}
