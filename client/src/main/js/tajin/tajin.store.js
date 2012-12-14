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
/*jslint white: true, browser: true, devel: false, indent: 4, plusplus: true */
/*global window, jQuery, console*/
(function (w, $) {
    "use strict";
    if (!w.JSON || !w.JSON.parse || !w.JSON.stringify) {
        if (w.console) {
            w.console.log('Tajin Store module requires JSON library to be present. It is not present, so Tajin will download it from https://github.com/douglascrockford/JSON-js/blob/master/json2.js');
        }
        return;
    }
    var impl,
        prefix = "__tajin__",
        adapt_html5 = function (storage) {
            return {
                get: function (k) {
                    var v = storage.getItem(k);
                    return typeof v === 'string' ? JSON.parse(v) : v;
                },
                put: function (k, v) {
                    storage.setItem(k, JSON.stringify(v));
                },
                del: function (k) {
                    storage.removeItem(k);
                }
            };
        };
    w.tajin.install({
        name: 'store',
        init: function (next, opts, tajin) {
            // localStorage + sessionStorage: IE 8+, Firefox 3.5+, Safari 4+, Chrome 4+, Opera 10.5+, iPhone 2+, Android 2+
            // globalStorage: Firefox 2+ (See: https://developer.mozilla.org/en/dom/storage#globalStorage)
            $(['localStorage', 'sessionStorage', 'globalStorage']).each(function (i, store) {
                // try/catch for file protocol in Firefox
                try {
                    var s = w[store], t = '__tajin__test';
                    if (s) {
                        if (store === 'globalStorage') {
                            s = s[window.location.hostname];
                        }
                        //globalStorage
                        if ($.isFunction(s.getItem) && $.isFunction(s.setItem) && $.isFunction(s.removeItem)) {
                            w[store].setItem(t, t);
                            w[store].getItem(t);
                            w[store].removeItem(t);
                            impl = adapt_html5(s);
                        }
                        return false;
                    }
                } catch (e) {
                }
                return true;
            });

            // sessionStorage
            // IE 8+, Firefox 3.5+, Safari 4+, Chrome 4+, Opera 10.5+, iPhone 2+, Android 2+
            this.type = '...';
            next();
        },
        exports: {
            get: function (k) {
                if (typeof k !== 'string') {
                    throw new Error('Key must be a string');
                }
                return impl.get(prefix + k);
            },
            has: function (k) {
                var v = this.get(prefix + k);
                return v !== null && v !== undefined;
            },
            del: function (k) {
                var v = this.get(k);
                impl.del(prefix + k);
                return v;
            },
            put: function (k, v) {
                if (v === undefined || v === null) {
                    return this.del(k);
                } else {
                    var old = this.get(k);
                    impl.put(prefix + k, v);
                    return old;
                }
            }
        }
    });
}(window, jQuery));
