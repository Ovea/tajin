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
import org.junit.Ignore;
import org.junit.Test;

import javax.net.ServerSocketFactory;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TomcatContainerTest {

    @Test
    @Ignore
    public void test_restart() throws Exception {
        Container container = ContainerConfiguration.create()
                .port(9999)
                .webappRoot("../core/src/test/webapp")
                .context("superApp")
                .buildContainer(Server.TOMCAT7);
        assertFalse(container.isRunning());
        assertTrue(isPortFree(9999));
        container.start();
        assertFalse(isPortFree(9999));
        assertTrue(container.isRunning());
        container.stop();
        assertTrue(isPortFree(9999));
        assertFalse(container.isRunning());
        container.start();
        assertFalse(isPortFree(9999));
        assertTrue(container.isRunning());
    }

    @Test
    public void test_html() throws Exception {
        assertTrue(isPortFree(9999));
        Container container = ContainerConfiguration.create()
                .port(9999)
                .webappRoot("../core/src/test/webapp")
                .context("superApp")
                .buildContainer(Server.TOMCAT7);
        assertFalse(container.isRunning());
        assertTrue(isPortFree(9999));
        container.start();
        assertFalse(isPortFree(9999));
        assertTrue(container.isRunning());
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse("http://127.0.0.1:9999/superApp/index.xhtml");
        assertEquals(resp.getTitle(), "HTML file");
        assertThat(resp.getText(), containsString("HTML file"));
        container.stop();
        assertTrue(isPortFree(9999));
        assertFalse(container.isRunning());
    }

    @Test
    public void test_jsp() throws Exception {
        assertTrue(isPortFree(9989));
        Container container = ContainerConfiguration.create()
                .port(9989)
                .webappRoot("../core/src/test/webapp")
                .context("superApp")
                .buildContainer(Server.TOMCAT7);
        assertFalse(container.isRunning());
        assertTrue(isPortFree(9989));
        container.start();
        assertFalse(isPortFree(9989));
        assertTrue(container.isRunning());
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse("http://127.0.0.1:9989/superApp/index.jsp");
        assertEquals(resp.getTitle(), "JSP Test");
        assertThat(resp.getText(), containsString("Languages"));
        container.stop();
        assertTrue(isPortFree(9989));
        assertFalse(container.isRunning());
    }

    @Test
    public void test_jndi() throws Exception {
        assertTrue(isPortFree(3999));
        Container container = ContainerConfiguration.create()
                .port(3999)
                .webappRoot("../core/src/test/webapp")
                .context("superApp")
                .buildContainer(Server.TOMCAT7);
        assertTrue(isPortFree(3999));
        container.start();
        assertFalse(isPortFree(3999));
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse("http://127.0.0.1:3999/superApp/jndi.jsp");
        assertEquals(resp.getTitle(), "JNDI Test");
        assertThat(resp.getText(), containsString("OK"));
        container.stop();
        assertTrue(isPortFree(3999));
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