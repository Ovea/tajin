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
if (!('base64' in window.ovea.crypto)) {
    window.ovea.crypto.base64 = (function() {

        var enc = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'.split('');
        var dec = {};
        for (var i = 0; i < enc.length; i++) {
            dec[enc[i]] = i;
        }

        return {

            encode: function(ints) {
                if (typeof ints === 'number') ints = [ints];
                var b64 = '', bytes = ints.length << 2, n = 0, b = 0, m = 0, p = (ints.length << 1) % 3;
                if (p != 0) {
                    ints.push(0);
                    bytes += p;
                }
                for (; b < bytes; b++,m = b % 3) {
                    n += (ints[b >> 2] >>> ((3 - (b & 3)) << 3) & 0xff) << (2 - m << 3);
                    if (m == 2) {
                        b64 += enc[n >>> 18 & 63] + enc[n >>> 12 & 63] + enc[n >>> 6 & 63] + enc[n & 63];
                        n = 0;
                    }
                }
                return p === 0 ? b64 : b64.substring(0, b64.length - p) + (p === 1 ? '=' : '==');
            },

            decode: function(str) {
                if(typeof str !== 'string') {
                    throw new Error('Invalid BASE64 string: ' + str);
                }
                if(str.length == 0) return [];
                if (str.length < 8 || ((str.length & 0x3) != 0)) throw new Error('Invalid Base64 string: ' + str);
                var p = str.charAt(str.length - 1) != '=' ? '' : str.charAt(str.length - 2) == '=' ? 'AA' : 'A',
                    bytes = (str.length >>> 2) * 3 - p.length,
                    ints = new Array((bytes + 3) >>> 2), c, b, s, n;
                for (b = 0; b < ints.length; b++) ints[b] = 0;
                if (p != '') str = str.substring(0, str.length - p.length) + p;
                for (b = 0,c = 0; c < str.length; c += 4) {
                    n = (dec[str.charAt(c)] << 18) + (dec[str.charAt(c + 1)] << 12) + (dec[str.charAt(c + 2)] << 6) + dec[str.charAt(c + 3)];
                    for (s = 16; s >= 0 && b < bytes; s -= 8,b++) ints[b >> 2] |= (n >>> s & 0xff) << ((3 - (b & 3)) << 3);
                }
                return ints;
            }

        };

    })();
}
