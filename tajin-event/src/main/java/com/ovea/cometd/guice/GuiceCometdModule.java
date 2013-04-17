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

import com.google.inject.*;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.ovea.cometd.Dispatcher;
import com.ovea.json.JSONObject;
import org.cometd.bayeux.Session;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.java.annotation.ServerAnnotationProcessor;
import org.cometd.java.annotation.Service;
import org.cometd.server.BayeuxServerImpl;

/**
 * Guice module which provides BayeuxServer and BayeuxServerImpl instances and automatically
 * process CometD services annotated by {@link org.cometd.java.annotation.Service}. This module can be subclassed to
 * configure Bayeux server
 * <p>CometD service:</p>
 * <code><pre>
 * &#064;Service
 * &#064;Singleton
 * final class EchoService {
 *     &#064;Session
 *     ServerSession serverSession;
 * <p/>
 *     &#064;Listener(&quot;/echo&quot;)
 *     public void echo(ServerSession remote, ServerMessage.Mutable message) {
 *         String channel = message.getChannel();
 *         Object data = message.getData();
 *         remote.deliver(serverSession, channel, data, null);
 *     }
 * }
 * </pre></code>
 * <p>Guice module for services:</p>
 * <code><pre>
 * final class ServiceModule extends AbstractModule {
 *     protected void configure() {
 *         bind(EchoService.class);
 *     }
 * }
 * </pre></code>
 * <p>Guice module to configure CometD:</p>
 * <code><pre>
 * final class CometdModule extends GuiceCometdModule {
 *     &#064;Inject
 *     SecurityPolicy policy;
 * <p/>
 *     protected void configure(BayeuxServerImpl server) {
 *         server.setOption(BayeuxServerImpl.LOG_LEVEL, BayeuxServerImpl.DEBUG_LOG_LEVEL);
 *         server.addTransport(new Jetty8WebSocketTransport(server));
 *         server.setSecurityPolicy(policy);
 *     }
 * }
 * </pre></code>
 * <p>GuiceConfig (set in web.xml file):</p>
 * <code><pre>
 * public final class GuiceConfig extends GuiceServletContextListener {
 *     protected Injector getInjector() {
 *         return Guice.createInjector(Stage.PRODUCTION, new ServiceModule(), new WebModule(), new CometdModule());
 *     }
 * }
 * </pre></code>
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @since 1.0
 */
@Singleton
public class GuiceCometdModule extends AbstractModule implements Provider<BayeuxServerImpl> {

    @Override
    protected final void configure() {
        bind(BayeuxServerImpl.class).toProvider(this).in(Singleton.class);
        bind(BayeuxServer.class).to(BayeuxServerImpl.class).in(Singleton.class);
        if (discoverBindings()) {
            // automatically add services
            bindListener(new AbstractMatcher<TypeLiteral<?>>() {
                    public boolean matches(TypeLiteral<?> o) {
                        return o.getRawType().isAnnotationPresent(Service.class);
                    }
                }, new TypeListener() {
                public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                    final Provider<ServerAnnotationProcessor> processor = encounter.getProvider(ServerAnnotationProcessor.class);
                    encounter.register(new InjectionListener<I>() {
                        public void afterInjection(I injectee) {
                            processor.get().process(injectee);
                        }
                    });
                }
            }
            );
            // automatically add extensions
            bindListener(new AbstractMatcher<TypeLiteral<?>>() {
                    public boolean matches(TypeLiteral<?> o) {
                        return BayeuxServer.Extension.class.isAssignableFrom(o.getRawType());
                    }
                }, new TypeListener() {
                public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                    final Provider<BayeuxServer> server = encounter.getProvider(BayeuxServer.class);
                    encounter.register(new InjectionListener<I>() {
                        public void afterInjection(I injectee) {
                            server.get().addExtension(BayeuxServer.Extension.class.cast(injectee));
                        }
                    });
                }
            }
            );
            // automatically add session listeners
            bindListener(new AbstractMatcher<TypeLiteral<?>>() {
                    public boolean matches(TypeLiteral<?> o) {
                        return BayeuxServer.BayeuxServerListener.class.isAssignableFrom(o.getRawType());
                    }
                }, new TypeListener() {
                public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                    final Provider<BayeuxServer> server = encounter.getProvider(BayeuxServer.class);
                    encounter.register(new InjectionListener<I>() {
                        public void afterInjection(I injectee) {
                            server.get().addListener(BayeuxServer.BayeuxServerListener.class.cast(injectee));
                        }
                    });
                }
            }
            );
        }
        applicationBindings();
    }

    protected void applicationBindings() {
    }

    public final BayeuxServerImpl get() {
        BayeuxServerImpl server = new BayeuxServerImpl();
        configure(server);
        try {
            server.start();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return server;
    }

    protected boolean discoverBindings() {
        return true;
    }

    protected void configure(BayeuxServerImpl server) {
    }

    @Provides
    @Singleton
    ServerAnnotationProcessor annotationProcessor(BayeuxServer server) {
        return new ServerAnnotationProcessor(server);
    }

    @Provides
    @Singleton
    Dispatcher dispatcher(final BayeuxServer bayeuxServer) {
        return new Dispatcher() {
            @Override
            public void publish(String topic, JSONObject data) {
                publish(null, topic, data);
            }

            @Override
            public void publish(String from, String topic, JSONObject data) {
                Session session = bayeuxServer.getSession(from);
                bayeuxServer.createIfAbsent(topic);
                bayeuxServer.getChannel(topic).publish(session, data, null);
            }
        };
    }

}
