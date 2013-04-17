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
package com.ovea.crypto;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ByteUtils {

    public static int[] toInts(byte... data) {
        int n = data.length;
        if (n == 0) return new int[0];
        if ((n & 0x3) != 0) {
            throw new IllegalArgumentException("Byte array length must be a multiple of 4 to be put in a 32-bits array");
        }
        int[] result = new int[n >>> 2];
        for (int i = 0, r = 0; i < n; i++, r = i >>> 2) {
            result[r] = result[r] << 8 | (0xFF & data[i]);
        }
        return result;
    }

    public static byte[] toBytes(IntBuffer data) {
        int base = data.position();
        int n = data.limit() - base;
        if (n == 0) return new byte[0];
        int l = n << 2;
        byte[] result = new byte[l];
        for (int i = 0, r = base; i < l; i++, r = base + (i >>> 2)) {
            result[i] = (byte) (data.get(r) >>> (3 - (i & 3) << 3));
        }
        data.position(base);
        return result;
    }

    public static byte[] toBytes(int... data) {
        return toBytes(IntBuffer.wrap(data));
    }

    public static ByteBuffer toByteBuffer(int[] ints) {
        return toByteBuffer(IntBuffer.wrap(ints));
    }

    public static ByteBuffer toByteBuffer(IntBuffer buffer) {
        int pos = buffer.position();
        ByteBuffer bf = ByteBuffer.allocate((buffer.limit() - buffer.position()) << 2);
        while (buffer.hasRemaining()) {
            bf.putInt(buffer.get());
        }
        buffer.position(pos);
        bf.flip();
        return bf;
    }
}
