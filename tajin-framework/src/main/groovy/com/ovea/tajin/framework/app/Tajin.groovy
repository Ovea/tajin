package com.ovea.tajin.framework.app

import com.ovea.tajin.framework.io.Resource
import com.ovea.tajin.framework.util.PropertySettings

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Tajin {

    static final String VERSION = {
        try {
            return new PropertySettings(Resource.classpath(Tajin.classLoader, '/META-INF/maven/com.ovea.tajin/tajin-framework/pom.properties')).getString("version")
        } catch (ignored) {
            return "unknown"
        }
    }.call()

}
