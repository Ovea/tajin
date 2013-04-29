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
package com.ovea.tajin.framework.support.jetty

import com.google.inject.Module
import com.google.inject.servlet.GuiceFilter
import com.ovea.tajin.framework.app.Application
import com.ovea.tajin.framework.prop.PropertySettings
import com.ovea.tajin.framework.support.guice.GuiceListener
import org.eclipse.jetty.jmx.MBeanContainer
import org.eclipse.jetty.server.*
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.handler.RequestLogHandler
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.util.ssl.SslContextFactory

import javax.servlet.DispatcherType
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener
import java.lang.management.ManagementFactory

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-26
 */
class Container {

    private final Server server

    Container(PropertySettings settings, Collection<Application> applications, Module module) {
        HandlerCollection handlers = new HandlerCollection()

        // create a programmatic context for defined context path
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS)
        handlers.addHandler(context)
        context.contextPath = settings.getString('server.context', '/')
        context.classLoader = Thread.currentThread().contextClassLoader
        context.addFilter(GuiceFilter, '/*', EnumSet.allOf(DispatcherType))
        context.addServlet(DefaultServlet, '/*')
        context.setInitParameter('org.eclipse.jetty.servlet.SessionIdPathParameterName', 'none')
        context.setInitParameter('org.eclipse.jetty.servlet.SessionCookie', settings.getString('session.cookie.name', 'id'))
        context.addEventListener(new GuiceListener([module]))
        context.addEventListener(new ServletContextListener() {
            @Override
            void contextDestroyed(ServletContextEvent sce) {
                applications*.onstop()
            }

            @Override
            void contextInitialized(ServletContextEvent sce) {
                applications*.onStart()
            }
        })
        context.addEventListener(new HttpSessionListener() {
            @Override
            void sessionCreated(HttpSessionEvent se) {
                se.session.setMaxInactiveInterval(settings.getInt('session.timeout', 5))
            }

            @Override
            void sessionDestroyed(HttpSessionEvent se) {

            }
        })
        // attach a NCSA logger if desired
        settings.getPath('logging.request.folder', null)?.with { File dir ->
            dir.mkdirs()
            handlers.addHandler(new RequestLogHandler(
                requestLog: new AsyncNCSARequestLog(
                    filename: "${dir.absolutePath}/request.yyyy_mm_dd.log",
                    filenameDateFormat: 'yyyy-MM-dd',
                    retainDays: settings.getInt('logging.request.retainDays', 30),
                    extended: settings.getBoolean('logging.request.extended', false),
                    logCookies: settings.getBoolean('logging.request.cookies', false),
                    append: true,
                    logTimeZone: 'GMT',
                    ignorePaths: settings.getStrings('logging.request.ignores', '').collect { it.trim() },
                )
            ))
        }

        // create jetty server
        server = new Server()
        server.stopAtShutdown = true
        server.handler = handlers
        server.addBean(new MBeanContainer(ManagementFactory.platformMBeanServer))

        // configure HTTP connector
        HttpConfiguration httpConf = new HttpConfiguration(
            secureScheme: 'https',
            securePort: settings.getInt('server.https.port', 0),
            outputBufferSize: 32 * 1024,
            requestHeaderSize: 8 * 1024,
            responseHeaderSize: 8 * 1024,
            headerCacheSize: 512,
            sendServerVersion: false,
            sendDateHeader: false
        )
        HttpConnectionFactory http = new HttpConnectionFactory(httpConf)
        ServerConnector httpConnect = new ServerConnector(server, http)
        httpConnect.port = settings.getInt('server.http.port', 8080)
        httpConnect.idleTimeout = 30000
        server.addConnector(httpConnect)

        // configure HTTPS connectors
        if (settings.has('server.https.port')) {
            // ssl config
            SslContextFactory sslContextFactory = new SslContextFactory(
                keyStorePath: settings.getString('server.https.keystore.path'),
                keyStorePassword: settings.getString('server.https.keystore.password'),
                trustStorePath: settings.getString('server.https.keystore.path'),
                trustStorePassword: settings.getString('server.https.keystore.password'),
                endpointIdentificationAlgorithm: '',
                excludeCipherSuites: [
                    'SSL_RSA_WITH_DES_CBC_SHA',
                    'SSL_DHE_RSA_WITH_DES_CBC_SHA',
                    'SSL_DHE_DSS_WITH_DES_CBC_SHA',
                    'SSL_RSA_EXPORT_WITH_RC4_40_MD5',
                    'SSL_RSA_EXPORT_WITH_DES40_CBC_SHA',
                    'SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA',
                    'SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA'
                ]
            )
            // connector config
            HttpConfiguration httpsConf = new HttpConfiguration(httpConf)
            httpsConf.addCustomizer(new SecureRequestCustomizer())
            // SSL Factory
            SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, http.protocol)
            // HTTPS Connector
            ServerConnector httpsConnect = new ServerConnector(server, ssl, new HttpConnectionFactory(httpsConf))
            httpsConnect.port = settings.getInt('server.https.port')
            httpsConnect.idleTimeout = 30000
            server.addConnector(httpsConnect)
        }
    }

    void start() {
        server.start()
        server.join()
    }

    void stop() {
        server.stop()
    }
}
