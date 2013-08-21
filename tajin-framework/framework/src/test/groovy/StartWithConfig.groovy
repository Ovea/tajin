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
        TajinApplication.main('-c', 'src/test/data/sample-security.properties')
        //TajinApplication.main('-c', 'src/test/data/sample-ssl.properties')
        //TajinApplication.main('-c', 'src/test/data/sample-tmpl.properties')
        //TajinApplication.main('-c', 'src/test/data/sample-classpath.properties')
    }
}
