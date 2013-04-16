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
    var I18NModule = function () {
        var m = this,
            tajin,
            events,
            cache = {},
            rescache = {},
            Bundle = function (name, locale, b, l) {
                this.name = name;
                this.locale = locale;
                this.bundle = b;
                this.resolved = l;
            },
            Resources = function (locale) {
                this.locale = locale;
            };

        function fix_locale(locale) {
            locale = (locale || navigator.language || navigator.userLanguage || '').replace(/-/, '_').toLowerCase();
            return locale.length > 3 ? locale.substring(0, 3) + locale.substring(3).toUpperCase() : locale;
        }

        function extensions(locale, variants) {
            var tries = [], l;
            // load base
            tries.push('');
            // load per language (en, fr, es, ...)
            if (locale.length >= 2) {
                l = locale.substring(0, 2);
                if ($.inArray(l, variants) >= 0) {
                    tries.push(l);
                }
            }
            // load per region (CA, US, ...)
            if (locale.length >= 5) {
                l = locale.substring(0, 5);
                if ($.inArray(l, variants) >= 0) {
                    tries.push(l);
                }
            }
            return tries;
        }

        Resources.prototype = {
            toString: function () {
                return 'Resources for locale ' + this.locale;
            },
            url: function (res) {
                var i, ext, found = $.grep(tajin.options.i18n.resources, function (e) {
                    var p = e.path.indexOf(res);
                    return p !== -1 && e.path.length === p + res.length;
                });
                if (found.length) {
                    ext = extensions(this.locale, found[0].variants || []);
                    if (ext.length) {
                        if (ext[ext.length - 1]) {
                            i = found[0].path.lastIndexOf('.');
                            res = found[0].path.substring(0, i) + '_' + ext[ext.length - 1] + found[0].path.substring(i);
                        } else {
                            res = found[0].path;
                        }
                    }
                }
                return tajin.util.path(res);
            },
            image: function (res, cb) {
                var u = this.url(res), img = $("<img/>");
                cb = cb || $.noop;
                img.load(function () {
                    if (!this.complete || typeof this.naturalWidth === "undefined" || this.naturalWidth === 0) {
                        cb(img, u, true);
                    } else {
                        events.image.fire(img);
                        cb(img, u, false);
                    }
                }).attr('src', u);
            },
            html: function (res, cb) {
                var u = this.url(res);
                cb = cb || $.noop;
                if (rescache[u]) {
                    cb(rescache[u], u, false);
                    events.html.fire(rescache[u]);
                }
                $.ajax({
                    dataType: 'html',
                    cache: false,
                    url: u,
                    success: function (html) {
                        rescache[u] = html;
                        events.html.fire(rescache[u]);
                        cb(rescache[u], u, false);
                    },
                    error: function () {
                        cb('', u, true);
                    }
                });
            }
        };

        Bundle.prototype = {
            toString: function () {
                return 'Bundle ' + this.name + ' for locale ' + this.locale + " (resolved as '" + (this.resolved || 'default') + "')";
            },
            localize: function (expr) {
                if (tajin.options.i18n.debug) {
                    console.log('[tajin.i18n] localize', this.name, this.locale);
                }
                var self = this,
                    e = (expr instanceof jQuery) ? expr : $(expr),
                    attrs = $.isArray(tajin.options.i18n.attributes) ? tajin.options.i18n.attributes : ['href', 'src'],
                    cb = tajin.options.i18n.onlocalize || $.noop,
                    dolocEl = function () {
                        var elem = $(this), key = elem.attr("rel").match(/localize\[([\.\w]+)\]/)[1], v = self.value(key);
                        if (tajin.options.i18n.debug) {
                            console.log('[tajin.i18n] localize', self.name, self.locale, key, v, elem);
                        }
                        cb(self.name, self.locale, elem, key, v);
                    },
                    dolocAttr = function (attr) {
                        var elem = $(this), key = elem.attr(attr).match(/localize\[([\.\w]+)\]/)[1], v = self.value(key);
                        if (tajin.options.i18n.debug) {
                            console.log('[tajin.i18n] localize', self.name, self.locale, key, v, elem);
                        }
                        elem.attr(attr, v);
                    };
                if (e.attr('rel') && e.attr('rel').match(/^localize/)) {
                    dolocEl.call(e);
                }
                e.find('[rel*="localize"]').each(function () {
                    dolocEl.call(this);
                });
                $.each(attrs, function (i, attr) {
                    if (e.attr(attr) && e.attr(attr).match(/^localize/)) {
                        dolocAttr.call(e, attr);
                    }
                    e.find('[' + attr + '^="localize"]').each(function () {
                        dolocAttr.call(this, attr);
                    });
                });
            },
            value: function (key) {
                var value = this.bundle, keys = key.split(/\./);
                while (keys.length && $.isPlainObject(value)) {
                    value = value[keys.shift()];
                }
                return value;
            }
        };

        function load_bundle(bundle, locale, cb, tries, index) {
            var b, path, l, loc;
            if (arguments.length <= 3) {
                if (cache[bundle] && cache[bundle][locale]) {
                    if (tajin.options.i18n.debug) {
                        console.log('[tajin.i18n] load_bundle from cache', bundle, locale, cache[bundle][locale]);
                    }
                    cb(bundle, locale, true, cache[bundle][locale], locale);
                } else {
                    load_bundle(bundle, locale, cb || $.noop, extensions(locale, tajin.options.i18n.bundles[bundle].variants || []), 0);
                }
            } else if (index >= tries.length) {
                b = cache[bundle][locale];
                l = locale;
                if (!b) {
                    l = tries[tries.length - 1];
                    b = cache[bundle][l];
                }
                if (tajin.options.i18n.debug) {
                    console.log('[tajin.i18n] load_bundle completed', bundle, locale, b, l);
                }
                cb(bundle, locale, false, b, l);
            } else {
                if (!cache[bundle]) {
                    cache[bundle] = {};
                }
                if (cache[bundle][tries[index]]) {
                    if (tajin.options.i18n.debug) {
                        console.log('[tajin.i18n] load_bundle found in cache', bundle, tries[index], cache);
                    }
                    load_bundle(bundle, locale, cb, tries, index + 1);
                } else {
                    loc = tajin.options.i18n.bundles[bundle].location;
                    path = tajin.util.path((loc ? (loc + '/') : '') + bundle);
                    if (tries[index].length > 0) {
                        path += '_' + tries[index];
                    }
                    path += '.json';
                    $.ajax({
                            url: path,
                            dataType: 'json',
                            cache: false,
                            success: function (data) {
                                var i;
                                cache[bundle][tries[index]] = {};
                                for (i = 0; i < index; i++) {
                                    $.extend(true, cache[bundle][tries[index]], cache[bundle][tries[i]]);
                                }
                                $.extend(true, cache[bundle][tries[index]], data);
                                if (tajin.options.i18n.debug) {
                                    console.log('[tajin.i18n] load_bundle put in cache', bundle, tries[index], cache);
                                }
                                load_bundle(bundle, locale, cb, tries, index);
                            },
                            error: function () {
                                load_bundle(bundle, locale, cb, tries, index + 1);
                            }
                        }
                    );
                }
            }
        }

        m.name = 'i18n';
        m.requires = 'util,event';
        m.oninstall = function (t) {
            tajin = t;
            events = {
                bundle: tajin.event.add('i18n/bundle/loaded'),
                html: tajin.event.add('i18n/html/loaded'),
                image: tajin.event.add('i18n/image/loaded')
            };
        };
        m.onconfigure = function (tajin, opts) {
            var b, v = 0, bnds = [], pre = function () {
                if (b < bnds.length) {
                    var variants = opts.bundles[bnds[b]].preload || [];
                    if (v >= variants.length) {
                        v = 0;
                        b++;
                        pre();
                    } else {
                        if (opts.debug) {
                            console.log('[tajin.i18n] preloading', bnds[b], variants[v]);
                        }
                        load_bundle(bnds[b], variants[v], function () {
                            v++;
                            pre();
                        });
                    }
                }
            };
            for (b in opts.bundles) {
                if (opts.bundles.hasOwnProperty(b)) {
                    bnds.push(b);
                }
            }
            b = 0;
            pre();
        };
        m.exports = {
            load: function (name, locale, cb) {
                if (!tajin.options.i18n.bundles[name]) {
                    throw new Error('Inexisting bundle: ' + name);
                }
                locale = fix_locale(locale);
                load_bundle(name, locale, function (name, locale, incache, b, l) {
                    var bnd = new Bundle(name, locale, b, l);
                    if ($.isFunction(cb)) {
                        cb(bnd);
                    }
                    tajin.event.get('i18n/bundle/loaded/' + name).fire(bnd);
                    events.bundle.fire(bnd);
                });
            },
            resources: function (locale) {
                return new Resources(fix_locale(locale));
            }
        };
    };
    w.tajin.install(new I18NModule());
}(window, jQuery));
