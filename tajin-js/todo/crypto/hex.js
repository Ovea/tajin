/*
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
if (!('hex' in window.ovea.crypto)) {
    window.ovea.crypto.hex = (function() {

        var ENCODING = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'];
        var DECODING = '0123456789abcdef';

        function toDigit(str, i) {
            var pos = DECODING.indexOf(str.charAt(i));
            if (pos == -1) throw new Error('Invalid character at position ' + i + ' in hexadecimal string ' + str);
            return pos;
        }

        return {

            encode: function(ints) {
                if(typeof ints === 'number') {
                    ints = [ints];
                }
                var l = ints.length;
                var out = new Array(l << 3);
                for (var i = 0, j = 0; i < l; i++) {
                    out[j++] = ENCODING[(0xF0000000 & ints[i]) >>> 28];
                    out[j++] = ENCODING[(0x0F000000 & ints[i]) >>> 24];
                    out[j++] = ENCODING[(0x00F00000 & ints[i]) >>> 20];
                    out[j++] = ENCODING[(0x000F0000 & ints[i]) >>> 16];
                    out[j++] = ENCODING[(0x0000F000 & ints[i]) >>> 12];
                    out[j++] = ENCODING[(0x00000F00 & ints[i]) >>> 8];
                    out[j++] = ENCODING[(0x000000F0 & ints[i]) >>> 4];
                    out[j++] = ENCODING[0x0000000F & ints[i]];
                }
                return out.join('');
            },

            decode: function(str) {
                if(typeof str !== 'string') {
                    throw new Error('Invalid HEX string: ' + str);
                }
                var len = str.length;
                if ((len & 0x3) != 0) {
                    throw new Error("Char array length must be a multiple of 4 to be put in a 32-bits array");
                }
                str = str.toLowerCase();
                var out = new Array(len >> 3);
                for (var i = 0, j = 0; j < len; i++) {
                    out[i] = toDigit(str, j) << 28;
                    j++;
                    out[i] = out[i] | toDigit(str, j) << 24;
                    j++;
                    out[i] = out[i] | toDigit(str, j) << 20;
                    j++;
                    out[i] = out[i] | toDigit(str, j) << 16;
                    j++;
                    out[i] = out[i] | toDigit(str, j) << 12;
                    j++;
                    out[i] = out[i] | toDigit(str, j) << 8;
                    j++;
                    out[i] = out[i] | toDigit(str, j) << 4;
                    j++;
                    out[i] = out[i] | toDigit(str, j);
                    j++;
                }
                return out;
            }

        };

    })();
}
