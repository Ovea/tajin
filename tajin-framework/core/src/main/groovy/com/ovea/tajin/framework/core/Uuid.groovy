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

import org.apache.commons.codec.binary.Base64

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-05-10
 */
class Uuid {

    static String getNewUUID() {
        UUID j = UUID.randomUUID()
        byte[] data = new byte[16]
        long msb = j.getMostSignificantBits()
        long lsb = j.getLeastSignificantBits()
        for (int i = 0; i < 8; i++) {
            data[i] = (byte) (msb & 0xff)
            msb >>>= 8
        }
        for (int i = 8; i < 16; i++) {
            data[i] = (byte) (lsb & 0xff)
            lsb >>>= 8
        }
        return Base64.encodeBase64URLSafeString(data)
    }

    static boolean isUUID(String uuidString) {
        if (uuidString == null)
            return false
        byte[] b = Base64.decodeBase64(uuidString);
        // the string is the B64 representation of 128bits: two long
        if (b.length != 16)
            return false
        return true
    }

    /*private static UUID fromBytes(byte[] data) {
        long msb = 0;
        long lsb = 0;
        for (int i = 7; i >= 0; i--)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 15; i >= 8; i--)
            lsb = (lsb << 8) | (data[i] & 0xff);
        return new UUID(msb, lsb);
    }*/

}
