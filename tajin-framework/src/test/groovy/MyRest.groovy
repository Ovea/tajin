import com.ovea.tajin.framework.i18n.I18N
import com.ovea.tajin.framework.i18n.I18NService
import com.ovea.tajin.framework.template.I18NTemplate
import com.ovea.tajin.framework.template.Tmpl

import javax.annotation.security.PermitAll
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

    @Tmpl('classpath:tmpl.txt')
    I18NTemplate template

    @I18N('classpath:a.json')
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
            msg2: i18n.forLocale(Locale.CANADA_FRENCH).message('mykey.mysubkey')
        ]
    }

}
