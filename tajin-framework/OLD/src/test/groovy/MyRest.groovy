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
import com.ovea.tajin.framework.i18n.Bundle
import com.ovea.tajin.framework.i18n.I18NService
import com.ovea.tajin.framework.template.I18NTemplate
import com.ovea.tajin.framework.template.Template

import javax.annotation.security.PermitAll
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@Path("/user")
@Singleton
class MyRest {

    @Inject
    Service service

    @Inject
    Provider<Locale> locale

    @Template('classpath:tmpl.txt')
    I18NTemplate template

    @Bundle('classpath:a.json')
    I18NService i18n

    @GET
    @Path("/me")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    def getMe() {
        [
            id: 1,
            email: 'toto@tajin.com',
            msg: template.merge(Locale.FRANCE, [obj: [value: 'me']]),
            msg2: i18n.getBundle(Locale.CANADA_FRENCH).message('mykey.mysubkey')
        ]
    }

}
