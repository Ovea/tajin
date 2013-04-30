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
package com.ovea.tajin.framework.util

import org.apache.shiro.codec.Base64

public final class Uuid implements Serializable, Comparable<Uuid> {

    private static final long serialVersionUID = 1;

    private final UUID internal;

    private Uuid(UUID uuid) {
        this.internal = uuid;
    }

    public static Uuid valueOf(String uuidString) {
        if (uuidString == null)
            throw new IllegalArgumentException("Illegal UUID string: " + uuidString);
        byte[] b = Base64.decode(uuidString);
        // the string is the B64 representation of 128bits: two long
        if (b.length != 16)
            throw new IllegalArgumentException("Invalid UUID string: " + uuidString);
        return new Uuid(fromBytes(b));
    }

    public static Uuid generate() {
        return new Uuid(UUID.randomUUID());
    }

    @Override
    public int compareTo(Uuid o) {
        return internal.compareTo(o.internal);
    }

    @Override
    public boolean equals(Object o) {
        if (this.is(o)) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uuid uuid = (Uuid) o;
        return internal.equals(uuid.internal);
    }

    @Override
    public int hashCode() {
        return internal.hashCode();
    }

    @Override
    public String toString() {
        return Base64.encodeToString(toBytes());
    }

    /**
     * 128-bits UUID in a byte[16] array
     */
    public byte[] toBytes() {
        byte[] data = new byte[16];
        long msb = internal.getMostSignificantBits();
        long lsb = internal.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            data[i] = (byte) (msb & 0xff);
            msb >>>= 8;
        }
        for (int i = 8; i < 16; i++) {
            data[i] = (byte) (lsb & 0xff);
            lsb >>>= 8;
        }
        return data;
    }

    private static UUID fromBytes(byte[] data) {
        long msb = 0;
        long lsb = 0;
        for (int i = 7; i >= 0; i--)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 15; i >= 8; i--)
            lsb = (lsb << 8) | (data[i] & 0xff);
        return new UUID(msb, lsb);
    }

    public UUID getInternal() {
        return internal;
    }

}
