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

    var configure = function (t, m) {
        var n = m.name;
        t.options[n] = t.options[n] || {};
        if (t.options.debug) {
            console.log('[tajin.core] configure', n, t.options[n]);
        }
        try {
            if ($.isFunction(m.onconfigure)) {
                m.onconfigure(t, t.options[n]);
            }
            m.tajin_init = 'success';
        } catch (e) {
            m.tajin_init = e;
            if ($.isFunction(t.options.onerror)) {
                t.options.onerror(t, e);
            }
            throw e;
        }
    };

    w.Tajin = function () {
        var self = this, status = 'new', modules = [];
        this.options = {};
        $.extend(self, {
            status: function () {
                return status;
            },
            uninstall: function (name) {
                if (name && typeof name !== 'string') {
                    name = name.name;
                }
                if (!name) {
                    throw new Error('Module name is missing');
                }
                var i;
                for (i = 0; i < modules.length; i++) {
                    if (modules[i].name === name) {
                        modules.splice(i, 1);
                        delete this[name];
                        break;
                    }
                }
            },
            install: function (module) {
                if (status === 'initializing') {
                    throw new Error('Initializing...');
                }
                if (!module.name) {
                    throw new Error('Module name is missing');
                }
                self.uninstall(module.name);
                if (module.requires) {
                    var missing = $.isArray(module.requires) ? module.requires.slice(0) : module.requires.split(','), p, i;
                    for (i = 0; i < modules.length; i++) {
                        p = $.inArray(modules[i].name, missing);
                        if (p >= 0) {
                            missing.splice(p, 1);
                        }
                    }
                    if (missing.length) {
                        throw new Error("Error loading module '" + module.name + "': missing modules: " + missing);
                    }
                }
                if (!module.exports) {
                    module.exports = {};
                }
                self.options[module.name] = self.options[module.name] || {};
                self[module.name] = module.exports;
                if ($.isFunction(module.oninstall)) {
                    module.oninstall(self);
                }
                if (status === 'ready') {
                    configure(self, module);
                }
                modules.push(module);
            },
            configure: function (opts) {
                if (status === 'new' || status === 'initializing') {
                    if (status === 'new') {
                        //noinspection JSUnusedAssignment
                        status = 'initializing';
                        self.options = $.extend(true, {
                            debug: false,
                            onready: $.noop
                        }, self.options || {}, w.tajin_init || {}, opts || {});
                    }
                    var i, inits = $.grep(modules, function (m) {
                        return m.tajin_init !== 'success' && $.isFunction(m.onconfigure);
                    });
                    for (i = 0; i < inits.length; i++) {
                        configure(self, inits[i]);
                    }
                    if (self.options.debug) {
                        console.log('[tajin.core] configure - completed with options', self.options);
                    }
                    status = 'ready';
                    if ($.isFunction(self.options.onconfigure)) {
                        self.options.onconfigure(self);
                    }
                }
            },
            toString: function () {
                return "Tajin Framework, version ${project.version}, modules: " + self.modules();
            },
            modules: function () {
                return $.map(modules, function (e) {
                    return e.name;
                });
            }
        });
    };
    w.tajin = new w.Tajin();
}(window, jQuery));
