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

import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAccount

/**
 * Load account or returns null
 */
public interface AccountRepository {

    String ANONYMOUS_PRINCIPAL = 'anonymous@anonymous.me'

    /**
     * Build account information with principal id, password hash and locking status from a user entered data
     */
    SimpleAccount getAccount(AuthenticationToken token)

    /**
     * Retreive an account from database thanks to a principal ID. The user has been authenticated and the account with full roles can be created
     */
    SimpleAccount getAccount(String principalId)

}
