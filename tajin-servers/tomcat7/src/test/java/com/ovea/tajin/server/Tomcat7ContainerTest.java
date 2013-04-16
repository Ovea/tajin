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

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ServerSocketFactory;
import java.util.Random;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

public final class Tomcat7ContainerTest {

    int PORT;

    @Before
    public void getPort() throws Exception {
        PORT = 1024 + new Random().nextInt(60000);
    }

    @Test
    @Ignore
    public void test_restart() throws Exception {
        Container container = ContainerConfiguration.create()
            .port(PORT)
            .webappRoot("../core/src/test/webapp")
            .context("superApp")
            .buildContainer(Server.TOMCAT7);
        assertFalse(container.isRunning());
        assertTrue(isPortFree(PORT));
        container.start();
        assertFalse(isPortFree(PORT));
        assertTrue(container.isRunning());
        container.stop();
        assertTrue(isPortFree(PORT));
        assertFalse(container.isRunning());
        container.start();
        assertFalse(isPortFree(PORT));
        assertTrue(container.isRunning());
    }

    @Test
    public void test_html() throws Exception {
        assertTrue(isPortFree(PORT));
        Container container = ContainerConfiguration.create()
            .port(PORT)
            .webappRoot("../core/src/test/webapp")
            .context("superApp")
            .buildContainer(Server.TOMCAT7);
        assertFalse(container.isRunning());
        assertTrue(isPortFree(PORT));
        container.start();
        assertFalse(isPortFree(PORT));
        assertTrue(container.isRunning());
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse("http://127.0.0.1:" + PORT + "/superApp/index.xhtml");
        assertEquals(resp.getTitle(), "HTML file");
        assertThat(resp.getText(), containsString("HTML file"));
        container.stop();
        assertTrue(isPortFree(PORT));
        assertFalse(container.isRunning());
    }

    @Test
    public void test_jsp() throws Exception {
        assertTrue(isPortFree(PORT));
        Container container = ContainerConfiguration.create()
            .port(PORT)
            .webappRoot("../core/src/test/webapp")
            .context("superApp")
            .buildContainer(Server.TOMCAT7);
        assertFalse(container.isRunning());
        assertTrue(isPortFree(PORT));
        container.start();
        assertFalse(isPortFree(PORT));
        assertTrue(container.isRunning());
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse("http://127.0.0.1:" + PORT + "/superApp/index.jsp");
        assertEquals(resp.getTitle(), "JSP Test");
        assertThat(resp.getText(), containsString("Languages"));
        container.stop();
        assertTrue(isPortFree(PORT));
        assertFalse(container.isRunning());
    }

    @Test
    public void test_jndi() throws Exception {
        assertTrue(isPortFree(PORT));
        Container container = ContainerConfiguration.create()
            .port(PORT)
            .webappRoot("../core/src/test/webapp")
            .context("superApp")
            .buildContainer(Server.TOMCAT7);
        assertTrue(isPortFree(PORT));
        container.start();
        assertFalse(isPortFree(PORT));
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse("http://127.0.0.1:" + PORT + "/superApp/jndi.jsp");
        assertEquals(resp.getTitle(), "JNDI Test");
        assertThat(resp.getText(), containsString("OK"));
        container.stop();
        assertTrue(isPortFree(PORT));
    }

    private boolean isPortFree(int port) {
        try {
            ServerSocketFactory.getDefault().createServerSocket(port).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}