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
if (window.I18N == undefined) {
    (function ($) {

        var inst = 0;

        window.I18N = function (options) {
            this.options = $.extend({
                bundle:undefined,
                url:undefined,
                versionning:undefined,
                variants:[],
                defaultLocale:navigator.language ? navigator.language : navigator.userLanguage,
                onKeyNotFound:function (key, locale) {
                    return '[' + key + ']';
                },
                onKeyFound:function (elem, key, value) {
                    elem.html(value);
                },
                onReady:$.noop(),
                onLocalized:$.noop(),
                cache:false,
                attributes:['href']
            }, options);
            if (this.options.bundle == undefined) {
                throw new Error('[I18N] "bundle" option is required (bundle name)')
            }
            if (this.options.url == undefined) {
                throw new Error('[I18N] "url" option is required (where to get localization files)')
            }
            this._logger = new Logger('I18N-' + (inst++) + '-' + this.options.bundle);
            this._uid = 0;
            this._bundles = {};
            this.options.defaultLocale = this._normalize(this.options.defaultLocale);
            for (var i = 0; i < this.options.variants.length; i++) {
                this.options.variants[i] = this._normalize(this.options.variants[i]);
            }
        };

        window.I18N.prototype = {
            _loadBundleAndRun:function (locale, tries, index, func) {
                var self = this;
                if (tries.length <= index) {
                    self._logger.debug('_loadBundleAndRun: calling callback');
                    func();
                } else {
                    var path = this.options.url + '/' + this.options.bundle;
                    if (tries[index].length > 0)
                        path += '_' + tries[index];
                    path += '.json';
                    if (this.options.versionning !== undefined) {
                        path += '?v=' + this.options.versionning;
                    }
                    self._logger.debug('_loadBundleAndRun: getting ', path);
                    var jqxhr = $.ajax({
                        url:path,
                        dataType:'json',
                        cache:this.options.cache
                    }).success(function (data) {
                            self._logger.debug('_loadBundleAndRun: merging to ', locale);
                            $.extend(true, self._bundles[locale], data);
                            self._loadBundleAndRun(locale, tries, index + 1, func);
                        }).error(function () {
                            self._logger.debug('_loadBundleAndRun: inexisting bundle for ', locale);
                            // tries.length so that we skip other potentially bad requests
                            self._loadBundleAndRun(locale, tries, tries.length, func);
                        });
                }
            },

            _isSupported:function (locale) {
                if (this.options.variants.length === 0) {
                    return true;
                }
                return $.inArray(locale, this.options.variants) != -1;
            },

            _normalize:function (locale) {
                if (locale) {
                    locale = locale.replace(/-/, '_').toLowerCase();
                    if (locale.length > 3) {
                        locale = locale.substring(0, 3) + locale.substring(3).toUpperCase();
                    }
                    return locale;
                } else {
                    return this.options.defaultLocale;
                }
            },

            withBundle:function (func, locale) {
                locale = this._normalize(locale);
                func.__i18n_id = this._uid++;
                var self = this;
                if (this._bundles[locale] && this._bundles[locale].__loaded) {
                    self._logger.debug('withBundle: applying locale ', locale, func.__i18n_id);
                    func.call(null, locale, this._bundles[locale]);
                } else if (this._bundles[locale]) {
                    // bundle is loading
                    self._logger.debug('withBundle: delaying localization ', locale, func.__i18n_id);
                    this._bundles[locale].__delay.queue('i18n', function (next) {
                        self._logger.debug('withBundle: applying locale ', locale, func.__i18n_id);
                        func.call(null, locale, self._bundles[locale]);
                        next();
                    });
                } else {
                    self._logger.debug('withBundle: loading new bundle ', locale, func.__i18n_id);
                    // bundle inexisting, load it !
                    this._bundles[locale] = {
                        __delay:$({})
                    };
                    this._bundles[locale].__delay.queue('i18n', function (next) {
                        self._logger.debug('withBundle: applying locale ', locale, func.__i18n_id);
                        func.call(null, locale, self._bundles[locale]);
                        next();
                    });
                    // first load base (root bundle)
                    var tries = [''];
                    // load per language (fr, en, ...)
                    if (locale.length >= 2) {
                        var v = locale.substring(0, 2);
                        if (this._isSupported(v)) {
                            tries.push(v);
                        }
                    }
                    // load per region (CA, US, ...)
                    if (locale.length >= 5) {
                        var v = locale.substring(0, 5);
                        if (this._isSupported(v)) {
                            tries.push(v);
                        }
                    }
                    // aggregate possibilities
                    if (tries.length) {
                        self._logger.debug('withBundle - loading bundles ', locale, tries);
                        this._loadBundleAndRun(locale, tries, 0, function () {
                            self._logger.debug('withBundle - bundle ready - dequeuing calls', locale);
                            self._bundles[locale].__loaded = true;
                            var q = self._bundles[locale].__delay;
                            delete self._bundles[locale].__delay;
                            q.dequeue('i18n');
                            if ($.isFunction(self.options.onReady)) {
                                self.options.onReady(self);
                            }
                        });
                    }
                }
            },

            localize:function (expr, locale) {
                this._logger.debug('Localizing to locale ' + locale);
                var self = this;
                this.withBundle(function (loc) {
                    var e = typeof expr == 'string' ? $(expr) : expr;
                    e.find('[rel*="localize"]').each(function () {
                        var elem = $(this);
                        var key = elem.attr("rel").match(/localize\[(.*?)\]/)[1];
                        var value = self.value(key, loc);
                        self.options.onKeyFound(elem, key, value);
                    });
                    for (var i = 0; i < self.options.attributes.length; i++) {
                        e.find('[' + self.options.attributes[i] + '*="localize"]').each(function () {
                            var elem = $(this);
                            var key = elem.attr(self.options.attributes[i]).match(/localize\[(.*?)\]/)[1];
                            var value = self.value(key, loc);
                            elem.attr(self.options.attributes[i], value);
                        });
                    }
                    if ($.isFunction(self.options.onLocalized)) {
                        self.options.onLocalized();
                    }
                }, locale);
            },

            value:function (key, locale) {
                locale = this._normalize(locale);
                if (this._bundles[locale]) {
                    var value = this._bundles[locale];
                    var keys = key.split(/\./);
                    while (keys.length && $.isPlainObject(value)) {
                        value = value[keys.shift()];
                    }
                    if (value != undefined) {
                        return value;
                    } else {
                        this._logger.debug('value ' + key + ' not found in bundle ' + locale);
                        return this.options.onKeyNotFound(key, locale);
                    }
                } else {
                    this._logger.debug('Bundle ' + locale + ' not found');
                    return this.options.onKeyNotFound(key, locale);
                }
            }

        };

    })(jQuery);
}
