import com.ovea.tajin.framework.app.Application
import com.ovea.tajin.framework.support.guice.WebBinder
import com.ovea.tajin.framework.support.shiro.AccountRepository
import com.ovea.tajin.framework.support.shiro.UsernamePasswordRealm
import com.ovea.tajin.framework.util.PropertySettings
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.util.SimpleByteSource

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-28
 */
class Sample implements Application {

    @Override
    void onInit(WebBinder binder, PropertySettings settings) {
        binder.configure {
            bind(MyRest)
            bind(AccountRepository).toInstance(new AccountRepository() {
                @Override
                SimpleAccount getAuthorizationInfo(String principalId) {
                    return new SimpleAccount(
                        principalId,
                        new Sha512Hash('password', principalId, 1).toHex(),
                        new SimpleByteSource(principalId as String),
                        UsernamePasswordRealm.simpleName)
                }

                @Override
                SimpleAccount getAuthenticationInfo(AuthenticationToken token) {
                    return getAuthorizationInfo(token.principal as String)
                }

            })
        }
    }

    @Override
    void onStart() {

    }

    @Override
    void onstop() {

    }
}
