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
/*global $, jQuery, window, console*/
(function (w) {
    "use strict";
    var ready, modules = [
        {
            name:'core'
        }
    ];
    w.tajin = {
        uninstall:function (name) {
            var i;
            for (i = 0; i < modules.length; i++) {
                if (modules[i].name === name) {
                    modules.splice(i, 1);
                    delete w.tajin[name];
                }
            }
        },
        install:function (module) {
            w.tajin.uninstall(module.name);
            if (module.requires) {
                var missing = $.isArray(module.requires) ? module.requires.slice(0) : [module.requires], p, i;
                for (i = 0; i < modules.length; i++) {
                    p = $.inArray(modules[i].name, missing);
                    if (p >= 0) {
                        missing.splice(p, 1);
                    }
                }
                if (missing.length) {
                    throw new Error("Required modules " + missing + " are missing for module '" + module.name + "'");
                }
            }
            modules.push(module);
            if (module.exports) {
                w.tajin[module.name] = module.exports;
            }
        },
        init:function (opts) {
            if (!ready) {
                ready = true;
                w.tajin.options = $.extend({
                    debug:false,
                    onready:$.noop
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
                                console.log('[tajin.core] init module with options', n, w.tajin.options[n]);
                            }
                            inits[i].exports.init.call(w.tajin, next, w.tajin.options[n], inits[i].exports);
                        } else if (i === inits.length && $.isFunction(w.tajin.options.onready)) {
                            if (w.tajin.options.debug) {
                                console.log('[tajin.core] onready - init completed with options', w.tajin.options);
                            }
                            w.tajin.options.onready(w.tajin);
                        }
                    };
                delete w.tajin_init;
                next();
            }

        },
        toString:function () {
            return "Tajin Framework, version ${project.version}, modules: " + $.map(modules, function (e) {
                return e.name;
            });
        }
    };
}(window));
