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
