import com.ovea.tajin.framework.app.TajinApplication

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-28
 */
class StartWithConfig {
    public static void main(String[] args) {
        //TajinApplication.main('-c', 'classpath:com/ovea/tajin/framework/config.properties')
        //TajinApplication.main('-c', 'src/test/data/sample-logback.properties')
        //TajinApplication.main('-c', 'src/test/data/sample-ncsa.properties')
        //TajinApplication.main('-c', 'src/test/data/sample-security.properties')
        //TajinApplication.main('-c', 'src/test/data/sample-ssl.properties')
        //TajinApplication.main('-c', 'src/test/data/sample-tmpl.properties')
        TajinApplication.main('-c', 'src/test/data/sample-classpath.properties')
    }
}
