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
package com.ovea.tajin.io
/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-02-20
 */
class Merger {

    static boolean merge(File out, Collection<File> files) {
        StringBuilder sb = new StringBuilder()
        boolean complete = true
        files.each { File f ->
            if (f.exists()) {
                if (f.name.endsWith('.css') || f.name.endsWith('.js')) {
                    sb.append("/* ${f.name} */\n" as String)
                }
                sb.append(new String(f.bytes, 'UTF-8')).append('\n')
            } else {
                complete = false
            }
        }
        out.bytes = sb.toString().getBytes('UTF-8')
        return complete
    }

}
