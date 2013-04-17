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
package com.ovea.cometd.guice;

import com.google.inject.Injector;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.java.annotation.AnnotationCometdServlet;
import org.cometd.java.annotation.ServerAnnotationProcessor;
import org.cometd.server.BayeuxServerImpl;
import org.eclipse.jetty.util.Loader;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;

/**
 * CometD Servlet to use with Guice Servlet extention.
 *
 * <code><pre>
 * final class WebModule extends ServletModule {
 *     protected void configureServlets() {
 *         bind(CrossOriginFilter.class).in(Singleton.class);
 *         filter(&quot;/*&quot;).through(CrossOriginFilter.class);
 *         serve(&quot;/cometd/*&quot;).with(GuiceCometdServlet.class);
 *     }
 * }
 * </pre></code>
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @since 1.0
 */
@Singleton
public final class GuiceCometdServlet extends AnnotationCometdServlet {

    private final Injector injector;

    @Inject
    public GuiceCometdServlet(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void init() throws ServletException {
        getServletContext().setAttribute(BayeuxServer.ATTRIBUTE, newBayeuxServer());
        super.init();
    }

    @Override
    protected BayeuxServerImpl newBayeuxServer() {
        return injector.getInstance(BayeuxServerImpl.class);
    }

    @Override
    protected ServerAnnotationProcessor newServerAnnotationProcessor(BayeuxServer bayeuxServer) {
        return injector.getInstance(ServerAnnotationProcessor.class);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected Object newService(String serviceClassName) throws Exception {
        return injector.getInstance(Loader.loadClass(getClass(), serviceClassName));
    }
}
