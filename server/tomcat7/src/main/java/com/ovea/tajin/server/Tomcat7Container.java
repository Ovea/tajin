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
package com.ovea.tajin.server;

import com.ovea.tajin.server.util.FileUtils;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static com.ovea.tajin.server.util.ClassloaderUtils.tryEnhanceContextClassloaderWithClasspath;
import static com.ovea.tajin.server.util.ResourceUtils.contextResource;

public final class Tomcat7Container extends ContainerSkeleton<Tomcat> {

    public static final String PROPERTY_TOMCAT_ENV = "tomcat.env";
    public static final String PROPERTY_TOMCAT_CONF = "tomcat.conf";

    private Thread serverThread;
    private CountDownLatch stop;

    public Tomcat7Container(ContainerConfiguration properties) {
        super(properties);
    }

    protected Tomcat buildServer() {
        // Create tomcat structure
        File homeDir = FileUtils.createTemporaryFolder("tomcat-");
        File confDir = new File(homeDir, "conf");
        File webappsDir = new File(homeDir, "webapps");
        new File(homeDir, "logs").mkdir();
        confDir.mkdir();
        webappsDir.mkdir();
        try {
            FileUtils.copyURLToFile(contextResource("com/ovea/tajin/server/web.xml"), new File(confDir, "web.xml"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        // Server classpath
        if (settings().hasServerClassPath()) {
            tryEnhanceContextClassloaderWithClasspath(settings().serverClassPath());
        }

        // Create server if it is configured through a server.xml file
        if (settings().has(PROPERTY_TOMCAT_CONF)) {
            Tomcat server = new Tomcat();
            Catalina catalina = new Catalina();
            server.setBaseDir(homeDir.getAbsolutePath());
            File conf = new File(settings().get(PROPERTY_TOMCAT_CONF));
            if (!conf.exists() || !conf.canRead())
                throw new IllegalArgumentException("Cannot access configuration file " + conf);

            catalina.setConfigFile(conf.getAbsolutePath());
            catalina.load();

            // In tomcat config file, you can set the server port.
            // If it is set, we must get it.
            // But as Tomcat supports several connectors, we can only reset the port of the first connector we find, hopping it will be the one we want...
            for (Service service : catalina.getServer().findServices()) {
                try {
                    settings().port(service.findConnectors()[0].getPort());
                    break;
                } catch (Exception e) {
                }
            }
            return server;
        }

        // Otherwise create an embedded server
        Tomcat server = new Tomcat();
        server.setBaseDir(homeDir.getAbsolutePath());
        server.setHostname("localhost");
        server.getServer().setAddress("localhost");
        server.setPort(port());
        server.getHost().setAppBase(webappsDir.getAbsolutePath());
        server.enableNaming();

        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(port());

        server.setConnector(connector);
        server.getService().addConnector(connector);

        // Create webapp loader with extra classpath of there are some
        WebappLoader loader = new WebappLoader();
        if (settings().hasWebappClassPath()) {
            for (URL url : settings().webappClassPath()) {
                loader.addRepository(url.toString());
            }
        }

        // Create context
        //Context context = server.addContext(contextPath(), webappRoot().getAbsolutePath());
        Context context = null;
        try {
            context = server.addWebapp(contextPath(), webappRoot().getAbsolutePath());
        } catch (ServletException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        context.setLoader(loader);
        context.setReloadable(true);

        // Set optionnal context.xml file
        if (settings().has(PROPERTY_TOMCAT_ENV)) {
            File ctx = new File(settings().get(PROPERTY_TOMCAT_ENV));
            if (!ctx.exists() || !ctx.canRead())
                throw new IllegalArgumentException("Cannot access configuration file " + ctx);
            try {
                context.setConfigFile(new File(ctx.getAbsolutePath()).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            File ctx = new File(webappRoot(), "META-INF/context.xml");
            try {
                context.setConfigFile(new File(ctx.getAbsolutePath()).toURI().toURL());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return server;
    }

    protected void start(final Tomcat server) {
        final CountDownLatch startupComplete = new CountDownLatch(1);
        stop = new CountDownLatch(1);
        serverThread = new Thread("Tomcat Server") {
            @Override
            public void run() {
                try {
                    server.start();
                    startupComplete.countDown();
                    stop.await();
                } catch (LifecycleException e) {
                    throw new RuntimeException(e.getMessage(), e);
                } catch (InterruptedException ignored) {
                } finally {
                    serverThread = null;
                    stop = null;
                }
            }
        };
        serverThread.start();
        try {
            startupComplete.await();
        } catch (InterruptedException ignored) {
        }
    }

    protected void stop(final Tomcat server) {
        try {
            server.stop();
            server.getConnector().destroy();
        } catch (LifecycleException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (stop != null) stop.countDown();
            stop = null;
            serverThread = null;
        }
    }

    @Override
    protected boolean isRunning(Tomcat embedded) {
        return serverThread != null;
    }

}
