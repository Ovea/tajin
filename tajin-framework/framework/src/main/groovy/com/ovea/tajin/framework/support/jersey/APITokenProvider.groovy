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
