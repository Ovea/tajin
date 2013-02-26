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
package com.ovea.tajin.crypto;

/**
 * @author David Avenante
 */
public class PasswordGenerator {

    public static final char[] HEX_CHARS = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public static final char[] SECURE_CHARS = {'=', '!', '#', '%', '&', '/', '(', ')'};

    protected static java.util.Random r = new java.util.Random();

    /**
     * Generates an eight characters long password consisting of hexadecimal characters.
     *
     * @return the generated password
     */
    public static String generate() {
        return generate(HEX_CHARS, 8);
    }

    /**
     * Generates a password consisting of hexadecimal characters.
     *
     * @param length of the password
     * @return the generated password
     */
    public static String generate(final int length) {
        return generate(HEX_CHARS, length);
    }

    /**
     * Generates a password according to the given parameters.
     *
     * @param characters that make up the password
     * @param length     of the password
     * @return the generated password
     */
    public static String generate(final char[] characters, final int length) {
        StringBuffer sb = new StringBuffer();

        sb.append(SECURE_CHARS[r.nextInt(SECURE_CHARS.length)]);
        for (int i = 0; i < length - 2; i++) {
            sb.append(characters[r.nextInt(characters.length)]);
        }
        sb.append(SECURE_CHARS[r.nextInt(SECURE_CHARS.length)]);

        return sb.toString();
    }
}
