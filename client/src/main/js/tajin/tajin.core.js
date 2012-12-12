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
    var ready = 0, resolved = true, listeners = [], modules = [
        {
            name: 'core'
        }
    ];
    w.tajin = {
        uninstall: function (name) {
            if (!name) {
                throw new Error('Module name is missing');
            }
            var i;
            for (i = 0; i < modules.length; i++) {
                if (modules[i].name === name) {
                    modules.splice(i, 1);
                    delete w.tajin[name];
                    break;
                }
            }
        },
        install: function (module) {
            if (ready === 1) {
                throw new Error('Initializing...');
            }
            if (!resolved) {
                throw new Error('Unable to init Tajin: previous modules have not been resolved correctly');
            }
            if (!module.name) {
                throw new Error('Module name is missing');
            }
            w.tajin.uninstall(module.name);
            if (module.requires) {
                var missing = $.isArray(module.requires) ? module.requires.slice(0) : module.requires.split(','), p, i;
                for (i = 0; i < modules.length; i++) {
                    p = $.inArray(modules[i].name, missing);
                    if (p >= 0) {
                        missing.splice(p, 1);
                    }
                }
                if (missing.length) {
                    resolved = false;
                    throw new Error("error loading module '" + module.name + "': missing module " + missing);
                }
            }
            modules.push(module);
            if (module.exports) {
                w.tajin[module.name] = module.exports;
            }
            resolved = true;
            if (ready === 2 && module.exports && $.isFunction(module.exports.init)) {
                w.tajin.options[module.name] = w.tajin.options[module.name] || {};
                module.exports.init.call(w.tajin, $.noop, w.tajin.options[module.name], module.exports);
            }
        },
        init: function (opts) {
            if (!resolved) {
                throw new Error('Unable to init Tajin: modules have not been resolved correctly');
            }
            if (ready === 0) {
                ready = 1;
                w.tajin.options = $.extend(true, {
                    debug: false,
                    onready: $.noop
                }, w.tajin_init || {}, opts || {});
                var n, i = -1,
                    inits = $.grep(modules, function (m) {
                        return m.exports && $.isFunction(m.exports.init);
                    }),
                    next = function () {
                        i++;
                        if (i < inits.length) {
                            n = inits[i].name;
                            w.tajin.options[n] = w.tajin.options[n] || {};
                            if (w.tajin.options.debug) {
                                console.log('[tajin.core] init', n, w.tajin.options[n]);
                            }
                            inits[i].exports.init.call(w.tajin, next, w.tajin.options[n], inits[i].exports);
                        } else if (i === inits.length) {
                            if (w.tajin.options.debug) {
                                console.log('[tajin.core] onready - init completed with options', w.tajin.options);
                            }
                            ready = 2;
                            if ($.isFunction(w.tajin.options.onready)) {
                                w.tajin.options.onready(w.tajin);
                            }
                            while (listeners.length) {
                                listeners.shift()(w.tajin);
                            }
                        }
                    };
                delete w.tajin_init;
                next();
            }
        },
        toString: function () {
            return "Tajin Framework, version ${project.version}, modules: " + w.tajin.modules();
        },
        modules: function () {
            return $.map(modules, function (e) {
                return e.name;
            });
        },
        ready: function (fn) {
            if ($.isFunction(fn)) {
                if (ready === 2) {
                    fn(w.tajin);
                } else {
                    listeners.push(fn);
                }
            }
        }
    };
}(window, jQuery));
