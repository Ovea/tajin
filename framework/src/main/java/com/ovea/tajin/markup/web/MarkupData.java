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
package com.ovea.tajin.markup.web;

import com.ovea.tajin.markup.util.Gzip;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class MarkupData {

    final byte[] data;
    final byte[] gzip;
    final String tag;
    final long when;
    final RuntimeException error;

    MarkupData(byte[] data) {
        long time = System.currentTimeMillis();
        this.when = time - (time % 1000);
        this.data = data;
        this.gzip = Gzip.compress(data);
        this.error = null;
        this.tag = "\"" + DigestUtils.md5Hex(data) + "\"";
    }

    MarkupData(RuntimeException error) {
        long time = System.currentTimeMillis();
        this.when = time - (time % 1000);
        this.error = error;
        this.data = null;
        this.gzip = null;
        this.tag = null;
    }

    boolean hasError() {
        return error != null;
    }

    RuntimeException error() {
        return error;
    }

    void reThrow() throws RuntimeException {
        throw error;
    }
}
