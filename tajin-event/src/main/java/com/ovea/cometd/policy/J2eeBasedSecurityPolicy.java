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
package com.ovea.cometd.policy;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Default {@link org.cometd.bayeux.server.SecurityPolicy} which determines if a user can handshake
 * by checking if the request has a {@link java.security.Principal}.
 * <p/>
 * I.e. useful in any J2ee or custom-based authentication
 * <p/>
 * You can also set a list of user principal names to exclude (like 'anonymous' or other sorts)
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @since 1.0
 */
public class J2eeBasedSecurityPolicy extends AuthSecurityPolicy {
    private final Set<String> excludes;

    /**
     * @param excludes Optional list of principal names to match as being non authenticated. I.e. 'anonymous', 'guest', ...
     */
    public J2eeBasedSecurityPolicy(String... excludes) {
        this.excludes = new HashSet<String>(Arrays.asList(excludes));
    }

    @Override
    protected boolean isAuthenticated(BayeuxServer server, ServerSession session, ServerMessage message) {
        return server.getContext().getUserPrincipal() != null && !excludes.contains(server.getContext().getUserPrincipal().getName());
    }
}
