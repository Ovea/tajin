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
package com.ovea.tajin.util

import com.ovea.tajin.crypto.AesCipherService
import com.ovea.tajin.crypto.OperationMode
import org.apache.commons.codec.binary.Base64

/**
 * @author Mathieu Carbou
 */
public final class Token implements Serializable {

    private static final long serialVersionUID = 1584911056653084608L;
    private static final byte[] ENCRYPTION_KEY = Base64.decodeBase64("nkBAiQ9lslobqHMjHhkHRQ==");
    private static final AesCipherService CIPHER_SERVICE = new AesCipherService();

    static {
        CIPHER_SERVICE.setGenerateInitializationVectors(false);
        CIPHER_SERVICE.setMode(OperationMode.ECB);
    }

    private final String token;
    private final String[] parts;

    private Token(String token, String... parts) {
        this.parts = parts;
        this.token = token;
    }

    public String value() {
        return token;
    }

    public String part(int i) {
        return parts[i];
    }

    public int size() {
        return parts.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this.is(o)) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token1 = (Token) o;
        return token.equals(token1.token);
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    @Override
    public String toString() {
        return value();
    }

    public static Token generate(String... parts) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeInt(parts.length);
            for (String part : parts)
                daos.writeUTF(part);
            byte[] serialized = baos.toByteArray();
            String token = Base64.encodeBase64URLSafeString(CIPHER_SERVICE.encrypt(serialized, ENCRYPTION_KEY));
            return new Token(token, parts);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Token valueOf(String token) {
        try {
            byte[] serialized = CIPHER_SERVICE.decrypt(Base64.decodeBase64(token), ENCRYPTION_KEY);
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(serialized));
            int size = dis.readInt();
            String[] parts = new String[size];
            for (int i = 0; i < size; i++)
                parts[i] = dis.readUTF();
            return new Token(token, parts);
        } catch (Exception e) {
            throw new TokenException(token, e);
        }
    }
}
