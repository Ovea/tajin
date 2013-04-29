import com.ovea.tajin.framework.app.TajinApplication

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-28
 */
class StartWithConfig {
    public static void main(String[] args) {
        //TajinApplication.main('-c', 'classpath:com/ovea/tajin/framework/config.properties')
        //TajinApplication.main('-c', 'src/test/data/sample1.properties')
        //TajinApplication.main('-c', 'src/test/data/sample2.properties')
        //TajinApplication.main('-c', 'src/test/data/sample3.properties')
        TajinApplication.main('-c', 'src/test/data/sample4.properties')
    }
}
