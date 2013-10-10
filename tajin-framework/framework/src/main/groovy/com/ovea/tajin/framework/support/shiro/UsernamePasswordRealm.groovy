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

import com.ovea.tajin.framework.util.PropertySettings
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection

import javax.inject.Inject
import java.security.Principal

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class UsernamePasswordRealm extends AuthorizingRealm {

    @Inject
    AccountRepository accountRepository

    @Inject
    UsernamePasswordRealm(PropertySettings settings) {
        authenticationCachingEnabled = true
        authorizationCachingEnabled = true
        authenticationTokenClass = UsernamePasswordToken
        credentialsMatcher = new HashedCredentialsMatcher(
            storedCredentialsHexEncoded: true,
            hashAlgorithmName: Sha512Hash.ALGORITHM_NAME,
            hashIterations: settings.getInt('security.hashIterations', 3)
        )
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        return accountRepository.getAuthenticationInfo(token)
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return accountRepository.getAuthorizationInfo(getAvailablePrincipal(principals))
    }

    @Override
    protected Object getAuthenticationCacheKey(PrincipalCollection principals) {
        return ((Principal) getAvailablePrincipal(principals)).name
    }

    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        return ((Principal) getAvailablePrincipal(principals)).name
    }

    @Override
    protected Object getAuthenticationCacheKey(AuthenticationToken token) {
        AuthenticationInfo info = accountRepository.getAuthenticationInfo(token)
        return getAuthenticationCacheKey(info.principals)
    }

}
