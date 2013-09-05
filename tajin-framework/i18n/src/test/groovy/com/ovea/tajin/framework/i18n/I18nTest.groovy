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

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-08-12
 */
@RunWith(JUnit4)
class I18nTest {

    @Test
    void test() throws Exception {
        I18NService service = new Default18NService()
        service.missingKeyBehaviour = MissingKeyBehaviour.RETURN_KEY

        [
            service.getBundleProvider('bundle.properties'),
            service.getBundleProvider('classpath:bundle.json'),
            service.getBundleProvider('classpath:bundle.js')
        ].eachWithIndex { provider, int i ->
            if (i == 0) assert provider instanceof PropertyI18NBundlerProvider
            if (i == 1) assert provider instanceof JsonI18NBundlerProvider
            if (i == 2) assert provider instanceof JsI18NBundlerProvider

            assert provider.getBundle(Locale.US).getValue('msg1', ['mat', 'dave']) == 'message1 from mat to dave'
            assert provider.getBundle(Locale.US).getValue('msg2', ['mat', 'dave']) == 'message2 from mat to dave'
            assert provider.getBundle(Locale.US).getValue('msg3', ['mat', 'dave']) == 'message3 from mat to dave'
            assert provider.getBundle(Locale.US).getValue('msg4', ['mat', 'dave']) == '[msg4]'

            assert provider.getBundle(Locale.FRANCE).getValue('msg1', ['mat', 'dave']) == 'message1 from mat to dave'
            assert provider.getBundle(Locale.FRANCE).getValue('msg2', ['mat', 'dave']) == 'message2 fr from mat to dave'
            assert provider.getBundle(Locale.FRANCE).getValue('msg4', ['mat', 'dave']) == '[msg4]'

            assert provider.getBundle(Locale.CANADA_FRENCH).getValue('msg1', ['mat', 'dave']) == 'message1 from mat to dave'
            assert provider.getBundle(Locale.CANADA_FRENCH).getValue('msg2', ['mat', 'dave']) == 'message2 fr from mat to dave'
            assert provider.getBundle(Locale.CANADA_FRENCH).getValue('msg4', ['mat', 'dave']) == '[msg4]'
        }
    }

    static class MyClass {

        @Bundle('classpath:bundle.js')
        I18NBundlerProvider i18n

        void aMethod() {
            i18n.getBundle(Locale.CANADA_FRENCH).getValue('mykey', ['10', 'seconds'])
        }
    }

}
