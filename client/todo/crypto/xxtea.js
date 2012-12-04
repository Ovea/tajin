if (!('xxtea' in window.ovea.crypto)) {
    window.ovea.crypto.xxtea = (function() {

        var DELTA = 0x9e3779b9;

        return {

            /*
             * encrypt integer array using Corrected Block XXTEA (xxtea) algorithm
             *
             * @param {integer array} clear Array of 32-bits to be encrypted (multi-byte safe)
             * @param {integer array} key Array of 32-bits to be used for encryption (1st 16 chars)
             * @returns {integer array} encrypted data
             */
            encrypt: function(clear, key) {
                return ovea.crypto.xxtea.encryptInPlace(clear.slice(0), key);
            },

            /*
             * encrypt integer array using Corrected Block XXTEA (xxtea) algorithm
             *
             * @param {integer array} data Array of 32-bits to be encrypted (multi-byte safe) in-place
             * @param {integer array} key Array of 32-bits to be used for encryption (1st 16 chars)
             */
            encryptInPlace: function(data, key) {
                if (key.length != 4) {
                    throw new Error('XXTEA requires a 128-bits key');
                }
                if(typeof data === 'number') {
                    data = [data];
                }
                if(data.length < 2) {
                    return data;
                }
                // enforce 32-bits integers for data
                for (var i = 0; i < data.length; i++) {
                    data[i] &= 0xffffffff;
                }
                // enforce 32-bits integers for key
                for (var i = 0; i < key.length; i++) {
                    key[i] &= 0xffffffff;
                }
                var n = data.length,
                    p,
                    rounds = 6 + Math.floor(52 / data.length),
                    e,
                    y,
                    sum = 0,
                    z = data[n - 1];
                do {
                    sum += DELTA;
                    e = sum >>> 2 & 3;
                    for (p = 0; p < n - 1; p++) {
                        y = data[p + 1];
                        z = data[p] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (key[(p & 3) ^ e] ^ z);
                    }
                    y = data[0];
                    z = data[n - 1] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (key[(p & 3) ^ e] ^ z);
                } while (--rounds > 0);
                for (var i = 0; i < data.length; i++) {
                    data[i] &= 0xffffffff;
                }
                return data;
            },

            decrypt: function(data, key) {
                return ovea.crypto.xxtea.decryptInPlace(data.slice(0), key);
            },

            decryptInPlace: function(data, key) {
                if (key.length != 4) {
                    throw new Error('XXTEA requires a 128-bits key');
                }
                if(typeof data === 'number') {
                    data = [data];
                }
                if(data.length < 2) {
                    return data;
                }
                // enforce 32-bits integers for key
                for (var i = 0; i < key.length; i++) {
                    key[i] &= 0xffffffff;
                }
                var z,
                    p,
                    e,
                    y = data[0],
                    sum = (6 + Math.floor(52 / data.length)) * DELTA;
                do {
                    e = sum >>> 2 & 3;
                    for (p = data.length - 1; p > 0; p--) {
                        z = data[p - 1];
                        y = data[p] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (key[(p & 3) ^ e] ^ z);
                    }
                    z = data[data.length - 1];
                    y = data[0] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (key[(p & 3) ^ e] ^ z);
                } while ((sum -= DELTA) != 0);
                for (var i = 0; i < data.length; i++) {
                    data[i] &= 0xffffffff;
                }
                return data;
            }

        };

    })();
}
