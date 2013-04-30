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

import com.sun.jersey.api.client.ClientHandlerException
import com.sun.jersey.api.client.ClientRequest
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.filter.ClientFilter

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-30
 */
class CookieTracking extends ClientFilter {
    final Map<String, String> cookies = [:]

    @Override
    ClientResponse handle(ClientRequest request) throws ClientHandlerException {
        if (cookies) {
            request.headers.add("Cookie", cookies.collect { k, v -> "${k}=${v}" }.join("; "))
        }
        ClientResponse response = next.handle(request)
        response.cookies?.each {
            cookies.put(it.name, it.value)
        }
        return response
    }

}
