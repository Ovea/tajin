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

import groovy.transform.Immutable
import groovy.transform.ToString
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.HostAuthenticationToken
import org.apache.shiro.authc.RememberMeAuthenticationToken
import org.apache.shiro.web.subject.WebSubject

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@Immutable
@ToString
class PassthroughToken implements HostAuthenticationToken, RememberMeAuthenticationToken {

    String principal
    boolean rememberMe = false
    String host = ((WebSubject) SecurityUtils.subject).servletRequest.remoteHost

    @Override
    Object getCredentials() {
        return null
    }

}
