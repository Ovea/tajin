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

import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.authc.credential.HashedCredentialsMatcher
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class UsernamePasswordRealm extends AuthorizingRealm {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsernamePasswordRealm)

    @Inject
    AccountRepository accountRepository

    UsernamePasswordRealm() {
        authenticationTokenClass = UsernamePasswordToken
        credentialsMatcher = new HashedCredentialsMatcher(
            storedCredentialsHexEncoded: true,
            hashAlgorithmName: Sha512Hash.ALGORITHM_NAME
        )
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        LOGGER.trace("doGetAuthenticationInfo {}", token)
        return accountRepository.getAuthenticationInfo(token)
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        LOGGER.trace("doGetAuthorizationInfo {}", principals)
        return accountRepository.getAuthorizationInfo(getAvailablePrincipal(principals))
    }

}
