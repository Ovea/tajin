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
package com.ovea.tajin.framework.core

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-08-12
 */
@RunWith(JUnit4)
class SettingsTest {

    @Test
    void test() throws Exception {
        Settings settings = new Settings([
            'yes': 'YES',
        ])
        assert settings.getEnum(Bool, 'yes') == Bool.YES
        assert settings.getEnum(Bool, 'blah', Bool.NO) == Bool.NO
    }

    static enum Bool {
        YES, NO
    }
}