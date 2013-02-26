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
package com.ovea.tajin;

import com.ovea.tajin.util.Token;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Carbou
 */
@RunWith(JUnit4.class)
public final class TokenTest {

    @Test
    public void test_genrate() throws Exception {
        Token token = Token.generate("invite", "toto@me.com");
        System.out.println(token);
        assertEquals(2, token.size());
        assertEquals("invite", token.part(0));
        assertEquals("toto@me.com", token.part(1));
    }

    @Test
    public void test_parse() throws Exception {
        Token token = Token.valueOf("JuxQ5X5RRf+ZZDO7l8mS77rRbSNMYvogbkQDksZPmYs=");
        assertEquals(2, token.size());
        assertEquals("invite", token.part(0));
        assertEquals("toto@me.com", token.part(1));
        assertEquals("JuxQ5X5RRf+ZZDO7l8mS77rRbSNMYvogbkQDksZPmYs=", token.value());
    }

    @Test
    public void test_parse_new_format() throws Exception {
        Token token = Token.valueOf("JuxQ5X5RRf-ZZDO7l8mS77rRbSNMYvogbkQDksZPmYs");
        assertEquals(2, token.size());
        assertEquals("invite", token.part(0));
        assertEquals("toto@me.com", token.part(1));
        assertEquals("JuxQ5X5RRf-ZZDO7l8mS77rRbSNMYvogbkQDksZPmYs", token.value());
    }

}
