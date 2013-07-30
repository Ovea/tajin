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
package com.ovea.tajin.framework.support.shiro

import org.apache.commons.codec.binary.Base64
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.SubjectContext
import org.apache.shiro.web.mgt.CookieRememberMeManager
import org.apache.shiro.web.servlet.Cookie
import org.apache.shiro.web.servlet.ShiroHttpServletRequest
import org.apache.shiro.web.subject.WebSubjectContext
import org.apache.shiro.web.util.WebUtils

import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class VersionedRememberMeManager extends CookieRememberMeManager {

    private static final Logger LOGGER = Logger.getLogger(VersionedRememberMeManager.class.getName());

    private int version = 0;

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    protected PrincipalCollection convertBytesToPrincipals(byte[] bytes, SubjectContext subjectContext) {
        PrincipalCollection principalCollection = super.convertBytesToPrincipals(bytes, subjectContext);
        int version = -1;
        try {
            version = (Integer) principalCollection.fromRealm("_v_").iterator().next();
        } catch (Exception ignored) {
        }
        // if version missmatch, the cookie must be regenerated
        if (version != this.version) {
            LOGGER.fine("Forcing cookie regeneration: member=" + principalCollection.getPrimaryPrincipal() + ", cookie-version=" + version + ", current-version=" + this.version);
            return null;
        } else {
            return principalCollection;
        }
    }

    @Override
    protected byte[] convertPrincipalsToBytes(PrincipalCollection principals) {
        ((SimplePrincipalCollection) principals).add(this.version, "_v_");
        return super.convertPrincipalsToBytes(principals);
    }

    @Override
    protected byte[] getRememberedSerializedIdentity(SubjectContext subjectContext) {
        if (!WebUtils.isHttp(subjectContext)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                String msg = "SubjectContext argument is not an HTTP-aware instance.  This is required to obtain a " +
                    "servlet request and response in order to retrieve the rememberMe cookie. Returning " +
                    "immediately and ignoring rememberMe operation.";
                LOGGER.fine(msg);
            }
            return null;
        }

        WebSubjectContext wsc = (WebSubjectContext) subjectContext;
        if (isIdentityRemoved(wsc)) {
            return null;
        }

        HttpServletRequest request = WebUtils.getHttpRequest(wsc);

        // Check in parameter for CORS support in IE first
        String base64 = request.getParameter(this.getCookie().getName());
        if (base64 == null || base64.isEmpty()) {
            return super.getRememberedSerializedIdentity(subjectContext);
        }

        if (Cookie.DELETED_COOKIE_VALUE.equals(base64)) return null;

        base64 = ensurePadding(base64);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Acquired Base64 encoded identity [" + base64 + "]");
        }
        byte[] decoded = Base64.decodeBase64(base64);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Base64 decoded byte array length: " + (decoded != null ? decoded.length : 0) + " bytes.");
        }
        return decoded;
    }

    private boolean isIdentityRemoved(WebSubjectContext subjectContext) {
        ServletRequest request = subjectContext.resolveServletRequest();
        if (request != null) {
            Boolean removed = (Boolean) request.getAttribute(ShiroHttpServletRequest.IDENTITY_REMOVED_KEY);
            return removed != null && removed;
        }
        return false;
    }

    private String ensurePadding(String base64) {
        int length = base64.length();
        if (length % 4 != 0) {
            StringBuilder sb = new StringBuilder(base64);
            for (int i = 0; i < length % 4; ++i) {
                sb.append('=');
            }
            base64 = sb.toString();
        }
        return base64;
    }
}