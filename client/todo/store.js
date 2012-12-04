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
if (window.Store == undefined) {
    (function ($) {

        var order = [];
        var stores = {};
        var instances = 0;

        /* Storage API */

        window.Store = function (opts) {
            var self = this;
            this.options = $.extend({
            }, opts || {});
            if (this.options.order) {
                this.order = this.options.order;
            } else {
                this.order = order;
            }
            this.stores = stores;
            for (var i = 0; i < order.length; i++) {
                if (stores[order[i]].check()) {
                    this.impl = stores[order[i]];
                    break;
                }
            }
            if (!this.impl) {
                throw new Error('No appropriate storage found amongst: ' + order);
            }
        };

        window.Store.prototype = {

            set:function (key, obj) {
                if (obj == undefined || obj == null) {
                    return this.del(key)
                } else {
                    var old = this.get(key);
                    this.impl.set(key, obj, this.options);
                    return old;
                }
            },

            get:function (key) {
                return this.impl.get(key, this.options);
            },

            del:function (key) {
                var old = this.get(key);
                this.impl.del(key, this.options);
                return old;
            }

        };

        /* HTML5 Storage */

        order.push('html5');
        stores.html5 = {

            name:'html5',

            check:function () {
                if ('localStorage' in window) {
                    try {
                        localStorage.setItem('__ovea_feature_test__', 'on');
                        if (localStorage.getItem('__ovea_feature_test__') === 'on') {
                            localStorage.removeItem('__ovea_feature_test__');
                            return true;
                        }
                    } catch (e) {
                    }
                }
                return false;
            },

            set:function (key, obj, opts) {
                localStorage.setItem(key, $.toJSON(obj));
            },

            del:function (key, opts) {
                localStorage.removeItem(key);
            },

            get:function (key, opts) {
                var v = localStorage.getItem(key);
                return v == null ? null : $.parseJSON(v);
            }

        };

        /* Cookie Storage */

        order.push('cookie');
        stores.cookie = {

            name:'cookie',

            check:function () {
                return "cookie" in document;
            },

            set:function (key, obj, opts) {
                var e = '', dt = new Date();
                if (obj == null) {
                    dt.setDate(dt.getDate() - 1);
                    e = ";expires=" + dt.toGMTString();
                }
                if (opts != undefined && 'days' in opts) {
                    dt.setDate(dt.getDate() + opts['days']);
                    e = ";expires=" + dt.toGMTString();
                }
                if (key.indexOf('=') != -1 || key.indexOf(';') != -1) {
                    key = "_" + encodeURIComponent(key);
                }
                var val = '';
                if (obj != null) {
                    val = $.toJSON(obj);
                    if (val.indexOf('=') != -1 || val.indexOf(';') != -1) {
                        val = "_" + encodeURIComponent(val);
                    }
                }
                document.cookie = key + "=" + val + ";path=/" + e;
            },

            del:function (key, opts) {
                this.set(key, null, opts);
            },

            get:function (key, opts) {
                if (key.indexOf('=') != -1 || key.indexOf(';') != -1) {
                    key = "_" + encodeURIComponent(key);
                }
                var result = new RegExp('(?:^|; )' + key + '=([^;]*)').exec(document.cookie);
                return result ? $.parseJSON(result[1].indexOf('_') == 0 ? decodeURIComponent(result[1].substring(1)) : result[1]) : null;
            }

        };

        /* In-Page Storage */

        order.push('page');
        stores.page = (function () {

            var data = {};

            return {
                name:'page',

                check:function () {
                    return true;
                },

                set:function (key, obj, opts) {
                    data[key] = obj;
                },

                del:function (key, opts) {
                    delete data[key];
                },

                get:function (key, opts) {
                    var v = data[key];
                    return v == undefined ? null : v;
                }
            };
        })();


    })(jQuery);
}
