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
package com.ovea.tajin.console

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
class ContainerConfiguration {

    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_CONTEXT_PATH = '/';
    public static final String DEFAULT_WEBAPP_ROOT = '/webapp';

    final Properties properties = new Properties();

    private ContainerConfiguration() {
        port(DEFAULT_PORT);
        context(DEFAULT_CONTEXT_PATH);
        webappRoot(DEFAULT_WEBAPP_ROOT);
    }

    public static ContainerConfiguration create() {
        return new ContainerConfiguration();
    }

    public Container buildContainer() {
        return new Container(properties);
    }

    public ContainerConfiguration port(int port) {
        if (port < 0 || port > 65535) throw new IllegalArgumentException('Invalid port: ' + port);
        properties.put('port', port);
        return this;
    }

    public ContainerConfiguration context(String contextPath) {
        notNull(contextPath, 'Webapp context path');
        properties.put('context', contextPath.startsWith('/') ? contextPath : '/' + contextPath);
        return this;
    }

    public ContainerConfiguration webappRoot(String webappRoot) {
        notNull(webappRoot, 'Webapp root');
        properties.put('webappRoot', new File(webappRoot));
        return this;
    }

    private static void notNull(Object o, String message) {
        if (o == null) throw new IllegalArgumentException(message + ' cannot be null !');
    }


}
