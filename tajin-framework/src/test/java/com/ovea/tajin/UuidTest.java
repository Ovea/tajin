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

import com.mycila.junit.concurrent.ConcurrentJunitRunner;
import com.ovea.tajin.util.Uuid;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(ConcurrentJunitRunner.class)
public final class UuidTest {

    @Test
    public void test_equality_is_consistent() throws Exception {
        Uuid uuid = Uuid.generate();
        String s = uuid.toString();
        assertEquals(uuid, Uuid.from(s));
    }

    @Test
    public void can_migrate_from_old_B64_to_new_B64() throws Exception {
        String[] olds = {
                "+0uwPiL4Py6E5dpJnAbFnA==",
                "bkOgOea9dtpIkxnZNXyqrA==",
                "cEUD2qLeHSBJCPwSZqq+mg==",
                "dkhwLvhG64/q+H9uvoleiA==",
                "e0x3tnrg+VJejxGdud03ug==",
                "eEdhA5TjiGzib8mFwTerrQ==",
                "FE0riumqnvgqxQL6/vhHpw==",
                "G0wupvEVFVTmfkzhCJhDgQ==",
                "H0HZIeUzsa+MR4L9VPVWpQ==",
                "IETOkS8uVu2pzaPYWdnGhw==",
                "iEX8YYKdcykqnzDJon4HlQ==",
                "Jk9vdxVxuvnTFyzOiQq8nQ==",
                "kkIb7kbQs3Dg5PPpi08Otg==",
                "lEH9oGr11Y4kdtFEiWBVmg==",
                "LExHxS+iJbvcY6E2mZmNsg==",
                "m0je9VkJeSgGCURpEEAHpg==",
                "mEAta6yCZr7RKRjOTtGqgQ==",
                "MkSRrk06fw4si+w0MCn7ng==",
                "oEZchiA2xFE9VnIxDKg+qg==",
                "qUM9GrytmwpZPSYu/KwGoA==",
                "Rkw9E+P2rDJIZpi1cMO1lA==",
                "TEcXiU4CahybAD3ZwYwluw==",
                "UUxDYstG7dtNsxsP08TZpQ==",
                "w0RxhS14xdrzYpBpOgt5tg==",
                "YEkmJU5izQIP2plhZ6l7gw==",
                "ykCcJPOr9S32NtFRd+Iqjw==",
                "zE3TGJbG25imAfopRvoIvg=="};
        for (String old : olds) {
            String migrated = migrate(old);
            System.out.println(old);
            System.out.println(migrated);
            assertEquals(Uuid.from(old), Uuid.from(migrated));
        }
    }

    private static String migrate(String old) {
        return old.replace('+', '-').replace('/', '_').replace("=", "");
    }
}