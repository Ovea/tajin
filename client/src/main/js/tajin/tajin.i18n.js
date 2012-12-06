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
/*global jQuery, window, console*/
(function (w, $) {
    "use strict";

    var cache = {},
        options = {
            debug:false,
            bundles:{},
            attributes:['href'],
            onlocalize:function (bundle, locale, elem, key, value) {
                if (value === undefined) {
                    value = '[' + key + ']';
                }
                if (options.debug) {
                    console.log('[tajin.i18n] onlocalize', bundle, locale, key, value);
                }
                elem.html(value);
            }
        },
        Bundle = function (name, locale, b, l) {
            this.name = name;
            this.locale = locale;
            this.bundle = b;
            this.resolved = l;
        };

    Bundle.prototype = {
        toString:function () {
            return 'Bundle ' + this.name + ' for locale ' + this.locale + " (resolved as '" + (this.resolved || 'default') + "')";
        },
        localize:function (expr) {
            if (options.debug) {
                console.log('[tajin.i18n] localize', this.name, this.locale);
            }
            var i, attr, self = this, e = (e instanceof jQuery) ? expr : $(expr);
            e.find('[rel*="localize"]').each(function () {
                var elem = $(this), key = elem.attr("rel").match(/localize\[(.*?)\]/)[1];
                options.onlocalize(self.name, self.locale, elem, key, self.value(key));
            });
            for (i = 0; i < options.attributes.length; i++) {
                attr = options.attributes[i];
                e.find('[' + attr + '*="localize"]').each(function () {
                    var elem = $(this), key = elem.attr(attr).match(/localize\[(.*?)\]/)[1];
                    elem.attr(attr, self.value(key));
                });
            }
        },
        value:function (key) {
            var value = this.bundle;
            var keys = key.split(/\./);
            while (keys.length && $.isPlainObject(value)) {
                value = value[keys.shift()];
            }
            return value;
        }
    };

    function fix_locale(locale) {
        locale = (locale || navigator.language || navigator.userLanguage).replace(/-/, '_').toLowerCase();
        return locale.length > 3 ? locale.substring(0, 3) + locale.substring(3).toUpperCase() : locale;
    }

    function extensions(bundle, locale) {
        var v = options.bundles[bundle].variants, tries = [], l;
        // load base
        if ($.inArray('', v) >= 0) {
            tries.push('');
        }
        // load per language (en, fr, es, ...)
        if (locale.length >= 2) {
            l = locale.substring(0, 2);
            if ($.inArray(l, v) >= 0) {
                tries.push(l);
            }
        }
        // load per region (CA, US, ...)
        if (locale.length >= 5) {
            l = locale.substring(0, 5);
            if ($.inArray(l, v) >= 0) {
                tries.push(l);
            }
        }
        if (options.debug) {
            console.log('[tajin.i18n] extensions', bundle, locale, tries);
        }
        return tries;
    }

    function load_bundle(bundle, locale, cb, tries, index) {
        var b, path, l;
        if (arguments.length <= 3) {
            if (cache[bundle] && cache[bundle][locale]) {
                if (options.debug) {
                    console.log('[tajin.i18n] load_bundle from cache', bundle, locale, cache[bundle][locale]);
                }
                cb(bundle, locale, true, cache[bundle][locale], locale);
            } else {
                load_bundle(bundle, locale, cb || $.noop, extensions(bundle, locale), 0);
            }
        } else if (index >= tries.length) {
            b = cache[bundle][locale];
            l = locale;
            if (!b) {
                l = tries[tries.length - 1];
                b = cache[bundle][l];
            }
            if (options.debug) {
                console.log('[tajin.i18n] load_bundle completed', bundle, locale, b, l);
            }
            cb(bundle, locale, false, b, l);
        } else {
            if (!cache[bundle]) {
                cache[bundle] = {};
            }
            if (cache[bundle][tries[index]]) {
                if (options.debug) {
                    console.log('[tajin.i18n] load_bundle found in cache', bundle, tries[index], cache);
                }
                load_bundle(bundle, locale, cb, tries, index + 1);
            } else {
                path = w.tajin.util.path(options.bundles[bundle].location + '/' + bundle);
                if (tries[index].length > 0) {
                    path += '_' + tries[index];
                }
                path += '.json';
                $.ajax({
                        url:path,
                        dataType:'json',
                        cache:false,
                        success:function (data) {
                            var i;
                            cache[bundle][tries[index]] = {};
                            for (i = 0; i < index; i++) {
                                $.extend(true, cache[bundle][tries[index]], cache[bundle][tries[i]]);
                            }
                            $.extend(true, cache[bundle][tries[index]], data);
                            if (options.debug) {
                                console.log('[tajin.i18n] load_bundle put in cache', bundle, tries[index], cache);
                            }
                            load_bundle(bundle, locale, cb, tries, index);
                        },
                        error:function () {
                            load_bundle(bundle, locale, cb, tries, index + 1);
                        }
                    }
                );
            }
        }
    }

    w.tajin.install({
        name:'i18n',
        requires:'core,util',
        exports:{
            init:function (next, opts) {
                $.extend(options, opts);
                if (!$.isFunction(options.onlocalize)) {
                    options.onlocalize = $.noop;
                }
                var b, v = 0, bnds = [], pre = function () {
                    if (b >= bnds.length) {
                        next();
                    } else {
                        var variants = options.bundles[bnds[b]].preload || [];
                        if (v >= variants.length) {
                            v = 0;
                            b++;
                            pre();
                        } else {
                            if (options.debug) {
                                console.log('[tajin.i18n] preloading', bnds[b], variants[v]);
                            }
                            load_bundle(bnds[b], variants[v], function () {
                                v++;
                                pre();
                            });
                        }
                    }
                };
                for (b in options.bundles) {
                    if (options.bundles.hasOwnProperty(b)) {
                        bnds.push(b);
                    }
                }
                b = 0;
                pre();
            },
            load:function (name, locale, cb) {
                if (!options.bundles[name]) {
                    throw new Error('Inexisting bundle: ' + name);
                }
                locale = fix_locale(locale);
                load_bundle(name, locale, function (name, locale, incache, b, l) {
                    if ($.isFunction(cb)) {
                        cb(new Bundle(name, locale, b, l));
                    }
                });
            }
        }
    });

}(window, jQuery));
