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
/*jslint white: true, browser: true, devel: false, indent: 4, plusplus: true, regexp: true */
/*global window, jQuery, console*/
(function (w, $) {
    "use strict";
    if (!w.JSON || !w.JSON.parse || !w.JSON.stringify) {
        if (w.console) {
            w.console.log('Tajin Store module requires JSON library to be present. It is not present, so Tajin will download it from https://github.com/douglascrockford/JSON-js/blob/master/json2.js');
        }
        return;
    }
    var StoreModule = function () {
        var self = this,
            impl,
            prefix = "__tajin__";
        this.name = 'store';
        this.exports = {
            get: function (k) {
                if (typeof k !== 'string') {
                    throw new Error('Key must be a string');
                }
                return impl.get(prefix + k);
            },
            clear: function () {
                impl.clear();
            },
            has: function (k) {
                var v = this.get(k);
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
        };
        this.init = function (next, opts, tajin) {
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
                            s.setItem(t, t);
                            s.getItem(t);
                            s.removeItem(t);
                            impl = {
                                get: function (k) {
                                    var v = s.getItem(k);
                                    return typeof v === 'string' ? JSON.parse(v) : v;
                                },
                                put: function (k, v) {
                                    s.setItem(k, JSON.stringify(v));
                                },
                                del: function (k) {
                                    s.removeItem(k);
                                },
                                clear: function () {
                                    s.clear();
                                }
                            };
                            self.exports.type = store;
                        }
                        return false;
                    }
                } catch (e) {
                }
                return true;
            });
            if (!impl) {
                (function () {
                    // append to html instead of body so we can do this from the head
                    var div = document.createElement("div"),
                        fix = function (k) {
                            // convert invalid characters to dashes
                            // http://www.w3.org/TR/REC-xml/#NT-Name
                            // simplified to assume the starting character is valid
                            // also removed colon as it is invalid in HTML attribute names
                            return k.replace(/[^\-._0-9A-Za-z\xb7\xc0-\xd6\xd8-\xf6\xf8-\u037d\u037f-\u1fff\u200c-\u200d\u203f\u2040\u2070-\u218f]/g, "-");
                        };
                    div.style.display = "none";
                    document.getElementsByTagName("head")[ 0 ].appendChild(div);
                    // we can't feature detect userData support
                    // so just try and see if it fails
                    // surprisingly, even just adding the behavior isn't enough for a failure
                    // so we need to load the data as well
                    try {
                        div.addBehavior("#default#userdata");
                        div.load(prefix);
                    } catch (e) {
                        div.parentNode.removeChild(div);
                        return;
                    }
                    div.load(prefix);
                    impl = {
                        get: function (k) {
                            var v = div.getAttribute(fix(k));
                            return typeof v === 'string' ? JSON.parse(v) : v;
                        },
                        put: function (k, v) {
                            div.setAttribute(fix(k), JSON.stringify(v));
                            div.save(prefix);
                        },
                        del: function (k) {
                            div.removeAttribute(fix(k));
                            div.save(prefix);
                        },
                        clear: function () {
                            var i, attrs = div.XMLDocument.documentElement.attributes;
                            for (i = 0; i < attrs.length; i++) {
                                div.removeAttribute(attrs[i].name);
                            }
                            div.save(prefix);
                        }
                    };
                    self.exports.type = 'userData';
                }());
            }
            if (!impl) {
                var memory = {};
                impl = {
                    get: function (k) {
                        var v = memory[k];
                        return typeof v === 'string' ? JSON.parse(v) : v;
                    },
                    put: function (k, v) {
                        memory[k] = JSON.stringify(v);
                    },
                    del: function (k) {
                        delete memory[k];
                    },
                    clear: function () {
                        memory = {};
                    }
                };
                self.exports.type = 'memory';
            }
            next();
        };
    };

    w.tajin.install(new StoreModule());

}(window, jQuery));
