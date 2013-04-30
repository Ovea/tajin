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
package com.ovea.tajin.framework.security

import org.apache.shiro.codec.Base64
import org.apache.shiro.crypto.AesCipherService
import org.apache.shiro.crypto.OperationMode

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-29
 */
@javax.inject.Singleton
class TokenBuilder {

    // 128-bit key
    private final byte[] key
    private final AesCipherService aesCipherService

    TokenBuilder(byte[] key) {
        if (key.length != 16) {
            throw new IllegalArgumentException('Bad key size: ' + key.length + '. Expected: 128-bits')
        }
        this.key = key
        this.aesCipherService = new AesCipherService()
        this.aesCipherService.generateInitializationVectors = false
        this.aesCipherService.mode = OperationMode.ECB
    }

    Token decode(String token) {
        try {
            byte[] serialized = aesCipherService.decrypt(Base64.decode(token), key).bytes;
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

    Token encode(String... data) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeInt(data.length);
            for (String part : data)
                daos.writeUTF(part);
            byte[] serialized = baos.toByteArray();
            String token = Base64.encodeToString(aesCipherService.encrypt(serialized, key).bytes);
            return new Token(token, data);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
