import com.ovea.tajin.framework.app.TajinApplication

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-28
 */
class StartWithConfig {
    public static void main(String[] args) {
        TajinApplication.main('-c', 'test/data/sample.properties')
    }
}
