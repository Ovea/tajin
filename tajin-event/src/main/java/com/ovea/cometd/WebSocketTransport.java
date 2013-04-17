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
package com.ovea.cometd;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletScopes;
import org.cometd.server.BayeuxServerImpl;
import org.eclipse.jetty.websocket.WebSocket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class WebSocketTransport extends org.cometd.websocket.server.WebSocketTransport {

    private static final Field localContext;

    private String _protocol;
    private String[] roles = new String[0];

    static {
        try {
            localContext = GuiceFilter.class.getDeclaredField("localContext");
            localContext.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static boolean isHttpRequest() {
        try {
            return ((ThreadLocal) localContext.get(null)).get() != null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public WebSocketTransport(BayeuxServerImpl bayeux) {
        super(bayeux);
    }

    @Override
    protected ScheduledExecutorService newScheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setNameFormat("WebSocketTransport-Scheduler-%1s")
            .build());
    }

    @Override
    protected Executor newExecutor() {
        int size = getOption(THREAD_POOL_MAX_SIZE, 64);
        return Executors.newFixedThreadPool(size, new ThreadFactoryBuilder()
            .setNameFormat("WebSocketTransport-Thread-%1s")
            .build());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void init() {
        super.init();
        _protocol = getOption(PROTOCOL_OPTION, _protocol);
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        final WebSocket.OnTextMessage webSocket = (WebSocket.OnTextMessage) super.doWebSocketConnect(request, protocol);
        if (webSocket == null) {
            return null;
        }
        final HttpServletRequest adapter = new FrozenHttpServletRequest(request, getRoles());
        return new WebSocket.OnTextMessage() {
            @Override
            public void onMessage(final String data) {
                if (isHttpRequest()) {
                    webSocket.onMessage(data);
                } else {
                    try {
                        final Map<Key<?>, Object> guice_ctx = new HashMap<Key<?>, Object>();
                        guice_ctx.put(Key.get(HttpServletRequest.class), adapter);
                        HttpSession session = adapter.getSession(false);
                        if (session != null) {
                            guice_ctx.put(Key.get(HttpSession.class), session);
                        }
                        ServletScopes.scopeRequest(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                webSocket.onMessage(data);
                                return null;
                            }
                        }, guice_ctx).call();
                    } catch (Exception e) {
                        throw (RuntimeException) e;
                    }
                }
            }

            @Override
            public void onOpen(Connection connection) {
                webSocket.onOpen(connection);
            }

            @Override
            public void onClose(int closeCode, String message) {
                webSocket.onClose(closeCode, message);
            }
        };
    }

    public String[] getRoles() {
        return roles;
    }

    public WebSocketTransport withRoles(String... roles) {
        this.roles = roles;
        return this;
    }
}
