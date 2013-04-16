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

public enum Server {

    JETTY8("com.ovea.tajin.server.Jetty8Container"),
    JETTY9("com.ovea.tajin.server.Jetty9Container"),
    TOMCAT6("com.ovea.tajin.server.Tomcat6Container"),
    TOMCAT7("com.ovea.tajin.server.Tomcat7Container");

    private final String serverClass;

    private Server(String serverClass) {
        this.serverClass = serverClass;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    /* keep package visibility */
    String serverClass() {
        return serverClass;
    }
}
