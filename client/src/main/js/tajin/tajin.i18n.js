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

    var cache = {}, options = {
        bundles:{}
    };

    function fix_locale(locale) {
        locale = (locale || '').replace(/-/, '_').toLowerCase();
        return locale.length > 3 ? locale.substring(0, 3) + locale.substring(3).toUpperCase() : locale;
    }

    function extensions(bundle, locale) {
        var v = options.bundles[bundle].variants, tries = [], l;
        // load base
        if ($.inArray('', v) >= 0) {
            tries.push('')
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
                tries.push(v);
            }
        }
        if (options.debug) {
            console.log('[tajin.i18n] extensions', bundle, locale, tries);
        }
        return tries;
    }

    function load_bundle(bundle, locale, cb, tries, index) {
        if (arguments.length <= 3) {
            if (cache[bundle] && cache[bundle][locale]) {
                if (options.debug) {
                    console.log('[tajin.i18n] load_bundle from cache', bundle, locale, cache[bundle][locale]);
                }
                cb(bundle, locale, true, options);
            } else {
                load_bundle(bundle, locale, cb || $.noop, extensions(bundle, locale), 0);
            }
        } else if (inex >= tries.length) {
            if (options.debug) {
                console.log('[tajin.i18n] load_bundle completed', bundle, locale, cache[bundle][locale]);
            }
            cb(bundle, locale, false, options);
        } else {
            var path = w.tajin.util.path(options.bundles[bundle].location + '/' + bundle);
            if (tries[index].length > 0) {
                path += '_' + tries[index];
            }
            path += '.json';
            $.ajax({
                    url:path,
                    dataType:'json',
                    cache:options.bundles[bundle].cache,
                    success:function (data) {
                        if (!cache[bundle]) {
                            cache[bundle] = {};
                            cache[bundle][locale] = {};
                        } else if (!cache[bundle][locale]) {
                            cache[bundle][locale] = {};
                        }
                        $.extend(true, cache[bundle][locale], data);
                        load_bundle(bundle, locale, cb, tries, index + 1);
                    },
                    error:function () {
                        load_bundle(bundle, locale, cb, tries, tries.length);
                    }
                }
            );
        }
    }

    w.tajin.install({
        name:'i18n',
        requires:'core,util',
        exports:{
            init:function (next, opts) {
                $.extend(options, opts);
            },
            bundle:function (name) {

            }
        }
    });

}(window, jQuery));
