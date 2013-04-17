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

/**
 * Converts hexadecimal Strings.
 *
 * @author Apache Software Foundation
 * @version $Id: Hex.java 1080701 2011-03-11 17:52:27Z ggregory $
 * @since 1.1
 */
public class HEX {

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Used to build output as Hex
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Converts an array of characters representing hexadecimal values into an array of bytes of those same values. The
     * returned array will be half the length of the passed array, as it takes two characters to represent any given
     * byte. An exception is thrown if the passed char array has an odd number of elements.
     *
     * @param data An array of characters containing hexadecimal digits
     * @return A byte array containing binary data decoded from the supplied char array.
     * @throws IllegalArgumentException Thrown if an odd number or illegal of characters is supplied
     */
    public static ByteBuffer decodeHex(char... data) {
        return decodeHex(CharBuffer.wrap(data));
    }

    public static ByteBuffer decodeHex(CharSequence data) {
        int len = data.length();
        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }
        ByteBuffer out = ByteBuffer.allocate(len >> 1);
        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data.charAt(j), j) << 4;
            j++;
            f = f | toDigit(data.charAt(j), j);
            j++;
            out.put(i, (byte) (f & 0xFF));
        }
        out.clear();
        return out;
    }

    public static IntBuffer decodeHexAsInts(char... data) {
        return decodeHexAsInts(CharBuffer.wrap(data));
    }

    public static IntBuffer decodeHexAsInts(CharSequence data) {
        int len = data.length();
        if ((len & 0x3) != 0) {
            throw new IllegalArgumentException("Char array length must be a multiple of 4 to be put in a 32-bits array");
        }
        IntBuffer out = IntBuffer.allocate(len >> 3);
        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int val = toDigit(data.charAt(j), j) << 28;
            j++;
            val |= toDigit(data.charAt(j), j) << 24;
            j++;
            val |= toDigit(data.charAt(j), j) << 20;
            j++;
            val |= toDigit(data.charAt(j), j) << 16;
            j++;
            val |= toDigit(data.charAt(j), j) << 12;
            j++;
            val |= toDigit(data.charAt(j), j) << 8;
            j++;
            val |= toDigit(data.charAt(j), j) << 4;
            j++;
            val |= toDigit(data.charAt(j), j);
            j++;
            out.put(i, val);
        }
        out.clear();
        return out;
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data a byte[] to convert to Hex characters
     * @return A char[] containing hexadecimal characters
     */
    public static CharBuffer encodeHex(byte... data) {
        return encodeHex(data, true);
    }

    public static CharBuffer encodeHex(ByteBuffer data) {
        return encodeHex(data, true);
    }

    public static CharBuffer encodeHex(int... data) {
        return encodeHex(data, true);
    }

    public static CharBuffer encodeHex(IntBuffer data) {
        return encodeHex(data, true);
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data        a byte[] to convert to Hex characters
     * @param toLowerCase <code>true</code> converts to lowercase, <code>false</code> to uppercase
     * @return A char[] containing hexadecimal characters
     * @since 1.4
     */
    public static CharBuffer encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    public static CharBuffer encodeHex(ByteBuffer data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    public static CharBuffer encodeHex(IntBuffer data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    public static CharBuffer encodeHex(int[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * Converts an array of bytes into an array of characters representing the hexadecimal values of each byte in order.
     * The returned array will be double the length of the passed array, as it takes two characters to represent any
     * given byte.
     *
     * @param data     a byte[] to convert to Hex characters
     * @param toDigits the output alphabet
     * @return A char[] containing hexadecimal characters
     * @since 1.4
     */
    private static CharBuffer encodeHex(byte[] data, char[] toDigits) {
        return encodeHex(ByteBuffer.wrap(data), toDigits);
    }

    private static CharBuffer encodeHex(int[] data, char[] toDigits) {
        return encodeHex(IntBuffer.wrap(data), toDigits);
    }

    private static CharBuffer encodeHex(IntBuffer data, char[] toDigits) {
        return encodeHex(ByteUtils.toByteBuffer(data), toDigits);
    }

    private static CharBuffer encodeHex(ByteBuffer data, char[] toDigits) {
        int l = data.limit();
        int pos = data.position();
        CharBuffer out = CharBuffer.allocate(l << 1);
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out.append(toDigits[(0xF0 & data.get(i)) >>> 4]);
            j++;
            out.append(toDigits[0x0F & data.get(i)]);
            j++;
        }
        out.clear();
        data.position(pos);
        return out;
    }

    /**
     * Converts an array of bytes into a String representing the hexadecimal values of each byte in order. The returned
     * String will be double the length of the passed array, as it takes two characters to represent any given byte.
     *
     * @param data a byte[] to convert to Hex characters
     * @return A String containing hexadecimal characters
     * @since 1.4
     */
    public static String encodeHexString(byte... data) {
        return encodeHex(data).toString();
    }

    public static String encodeHexString(ByteBuffer data) {
        return encodeHex(data).toString();
    }

    public static String encodeHexString(IntBuffer data) {
        return encodeHex(data).toString();
    }

    public static String encodeHexString(int... data) {
        return encodeHex(data).toString();
    }

    /**
     * Converts a hexadecimal character to an integer.
     *
     * @param ch    A character to convert to an integer digit
     * @param index The index of the character in the source
     * @return An integer
     * @throws IllegalArgumentException Thrown if ch is an illegal hex character
     */
    public static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }

}
