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
package com.ovea.tajin.framework.i18n

import com.ovea.tajin.framework.core.Resource

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-09-05
 */
class JsI18NBundlerProvider extends JsonI18NBundlerProvider {

    private final ScriptEngine scriptEngine

    JsI18NBundlerProvider(String bundleName, int maximumSize, long expirationSeconds) {
        super(bundleName, maximumSize, expirationSeconds)
        scriptEngine = new ScriptEngineManager().getEngineByExtension('js')
        scriptEngine.eval("function add_bundle(b) { return JSON.stringify(b); }")
    }

    @Override
    String loadAsjson(Resource resource) { scriptEngine.eval(resource.getText()) }

}
