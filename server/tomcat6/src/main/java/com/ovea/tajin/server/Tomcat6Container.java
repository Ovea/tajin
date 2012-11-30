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
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Catalina;
import org.apache.catalina.startup.Embedded;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static com.ovea.tajin.server.util.ClassloaderUtils.tryEnhanceContextClassloaderWithClasspath;
import static com.ovea.tajin.server.util.ResourceUtils.contextResource;

public final class Tomcat6Container extends ContainerSkeleton<Embedded> {

    public static final String PROPERTY_TOMCAT_ENV = "tomcat.env";
    public static final String PROPERTY_TOMCAT_CONF = "tomcat.conf";

    private Thread serverThread;
    private CountDownLatch stop;

    public Tomcat6Container(ContainerConfiguration properties) {
        super(properties);
    }

    protected Embedded buildServer() {
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

        Embedded server = new Embedded();
        server.setCatalinaBase(homeDir.getAbsolutePath());
        server.setCatalinaHome(homeDir.getAbsolutePath());

        // Create server if it is configured through a server.xml file
        if (settings().has(PROPERTY_TOMCAT_CONF)) {
            File conf = new File(settings().get(PROPERTY_TOMCAT_CONF));
            if (!conf.exists() || !conf.canRead())
                throw new IllegalArgumentException("Cannot access configuration file " + conf);
            Catalina catalina = new Catalina();
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
        // Create webapp loader with extra classpath of there are some
        WebappLoader loader = new WebappLoader();
        loader.setReloadable(true);
        if (settings().hasWebappClassPath()) {
            for (URL url : settings().webappClassPath()) {
                loader.addRepository(url.toString());
            }
        }

        // Create context
        Context context = server.createContext(contextPath(), webappRoot().getAbsolutePath());
        context.setLoader(loader);
        context.setReloadable(true);

        Host localHost = server.createHost("localHost", webappsDir.getAbsolutePath());
        localHost.addChild(context);

        // create engine
        Engine engine = server.createEngine();
        engine.setName("localEngine");
        engine.addChild(localHost);
        engine.setDefaultHost(localHost.getName());
        server.addEngine(engine);

        // create http connector
        Connector connector = server.createConnector((InetAddress) null, port(), "org.apache.coyote.http11.Http11NioProtocol");
        server.addConnector(connector);

        server.setAwait(true);

        // Set optionnal context.xml file
        if (settings().has(PROPERTY_TOMCAT_ENV)) {
            File ctx = new File(settings().get(PROPERTY_TOMCAT_ENV));
            if (!ctx.exists() || !ctx.canRead())
                throw new IllegalArgumentException("Cannot access configuration file " + ctx);
            context.setConfigFile(ctx.getAbsolutePath());
        } else {
            File ctx = new File(webappRoot(), "META-INF/context.xml");
            context.setConfigFile(ctx.getAbsolutePath());
        }

        return server;
    }

    protected void start(final Embedded server) {
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

    protected void stop(final Embedded server) {
        try {
            server.stop();
        } catch (LifecycleException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (stop != null) stop.countDown();
            stop = null;
            serverThread = null;
        }
    }

    @Override
    protected boolean isRunning(Embedded embedded) {
        return serverThread != null;
    }

}
