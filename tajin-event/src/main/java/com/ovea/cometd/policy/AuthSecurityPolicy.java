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

import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.DefaultSecurityPolicy;

/**
 * Skeleton to faciliate implementation of {@link org.cometd.bayeux.server.SecurityPolicy} based authentication.
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @since 1.0
 */
public abstract class AuthSecurityPolicy extends DefaultSecurityPolicy {

    @Override
    public boolean canHandshake(BayeuxServer server, ServerSession session, ServerMessage message) {
        return session.isLocalSession() || isAuthenticated(server, session, message);
    }

    @Override
    public boolean canCreate(BayeuxServer server, ServerSession session, String channelId, ServerMessage message) {
        return session != null && session.isLocalSession() || !ChannelId.isMeta(channelId) && isAuthenticated(server, session, message);
    }

    @Override
    public boolean canPublish(BayeuxServer server, ServerSession session, ServerChannel channel, ServerMessage message) {
        return session != null && session.isHandshook() && !channel.isMeta() && isAuthenticated(server, session, message);
    }

    @Override
    public boolean canSubscribe(BayeuxServer server, ServerSession session, ServerChannel channel, ServerMessage message) {
        return session != null && session.isLocalSession() || !channel.isMeta() && isAuthenticated(server, session, message);
    }

    protected abstract boolean isAuthenticated(BayeuxServer server, ServerSession session, ServerMessage message);

}
