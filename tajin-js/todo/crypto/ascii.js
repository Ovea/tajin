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
if (!('ascii' in window.ovea.crypto)) {
    window.ovea.crypto.ascii = (function() {

        return {

            toInts: function(ascii) {
                if (typeof ascii !== 'string') {
                    throw new Error('string required');
                }
                if(ascii.length == 0) {
                    return [];
                }
                var len = ascii.length, ar = [], i = 0;
                do {
                    ar.push(((ascii.charCodeAt(i++) | 0) << 24) + ((ascii.charCodeAt(i++) | 0) << 16) + ((ascii.charCodeAt(i++) | 0) << 8) + (ascii.charCodeAt(i++) | 0));
                } while (i < len);
                return ar
            },

            fromInts: function(ints) {
                if (typeof ints == 'number') {
                    ints = [ints];
                } else {
                    ints = ints.slice(0);
                }
                var len = ints.length, str = '';
                for (var i = 0; i < len; i++) {
                    var c = ints[i] >>> 24 & 0xff;
                    if (c != 0) {
                        str += String.fromCharCode(c);
                        c = ints[i] >>> 16 & 0xff;
                        if (c != 0) {
                            str += String.fromCharCode(c);
                            c = ints[i] >>> 8 & 0xff;
                            if (c != 0) {
                                str += String.fromCharCode(c);
                                c = ints[i] & 0xff;
                                if (c != 0) {
                                    str += String.fromCharCode(c);
                                }
                            }
                        }
                    }
                }
                return str;
            }

        };

    })();
}
