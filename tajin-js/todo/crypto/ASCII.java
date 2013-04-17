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
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ASCII {

    public static IntBuffer toIntBuffer(CharSequence sequence) {
        ByteBuffer buffer = Charset.defaultCharset().encode(CharBuffer.wrap(sequence));
        if((buffer.capacity() & 3) != 0) {
            buffer = ByteBuffer.allocate((buffer.capacity() & 0xfffffffc) + 4).put(buffer);
            buffer.flip();
        }
        buffer.position(buffer.limit()).limit(buffer.capacity());
        while (buffer.hasRemaining()) {
            buffer.put((byte) 0);
        }
        buffer.position(0);
        return buffer.asIntBuffer();
    }

    public static String fromInts(int... ints) {
        return fromIntBuffer(IntBuffer.wrap(ints));
    }

    public static int[] toInts(CharSequence sequence) {
        IntBuffer buffer = toIntBuffer(sequence);
        int[] res = new int[buffer.capacity()];
        buffer.get(res);
        return res;
    }

    public static String fromIntBuffer(IntBuffer buffer) {
        int p = buffer.position();
        ByteBuffer bb = ByteBuffer.allocate((buffer.limit() - p) << 2);
        while (buffer.hasRemaining()) {
            int i = buffer.get();
            if (!buffer.hasRemaining()) {
                // last int, check for 0x0 paddings
                byte b = (byte) (i >>> 24 & 0xff);
                if (b != 0) {
                    bb.put(b);
                    b = (byte) (i >>> 16 & 0xff);
                    if (b != 0) {
                        bb.put(b);
                        b = (byte) (i >>> 8 & 0xff);
                        if (b != 0) {
                            bb.put(b);
                            b = (byte) (i & 0xff);
                            if (b != 0) {
                                bb.put(b);
                            }
                        }
                    }
                }
            } else {
                bb.putInt(i);
            }
        }
        bb.flip();
        CharBuffer chars = Charset.defaultCharset().decode(bb);
        buffer.position(p);
        return chars.toString();
    }

}
