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

    @GET
    @Path("/me")
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    def getMe() {
        [id: 1, email: 'toto@tajin.com']
    }

}
