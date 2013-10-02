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
package com.ovea.tajin.framework.support.jersey

import com.ovea.tajin.framework.util.PropertySettings

import javax.inject.Inject
import javax.inject.Provider
import javax.servlet.http.HttpServletRequest

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-10-02
 */
class APITokenProvider implements Provider<APIToken> {

    @Inject Provider<HttpServletRequest> request
    @Inject APIRepository repository
    @Inject PropertySettings settings

    String tokenParam = APIToken.TOKEN_PARAM
    String tokenHeader = APIToken.TOKEN_HEADER

    private String getTokenValue() {
        if (!settings.getBoolean('tajin.jersey.support.apitoken', true)) return null
        String token = request.get().getParameter(tokenParam)
        if (!token) {
            token = request.get().getHeader(tokenHeader)
        }
        return token
    }

    @Override
    APIToken get() { tokenValue?.with { repository.getAPIToken(it) } }

}
