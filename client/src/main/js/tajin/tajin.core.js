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
    var ready = 0, listeners = [], modules = [];
    w.Tajin = function () {
    };
    w.Tajin.prototype = {
        uninstall: function (name) {
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
            if (ready === 1) {
                throw new Error('Initializing...');
            }
            if (!module.name) {
                throw new Error('Module name is missing');
            }
            this.uninstall(module.name);
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
                module.exports = {}
            }
            if (!$.isFunction(module.init)) {
                module.init = function (next) {
                    next();
                };
            }
            if (ready === 2) {
                this.options[module.name] = this.options[module.name] || {};
                module.init($.noop, this.options[module.name], this);
            }
            modules.push(module);
        },
        init: function (opts) {
            if (ready === 0 || ready === 1) {
                if (ready === 0) {
                    this.options = $.extend(true, {
                        debug: false,
                        onready: $.noop
                    }, w.tajin_init || {}, opts || {});
                }
                ready = 1;
                var n, i = -1, self = this,
                    inits = $.grep(modules, function (m) {
                        return m.tajin_init !== 'success';
                    }),
                    next = function () {
                        i++;
                        if (i > 0 && i <= inits.length) {
                            // exports previously suceed module
                            self[n] = inits[i - 1].exports;
                            inits[i - 1].tajin_init = 'success';
                        }
                        if (i < inits.length) {
                            n = inits[i].name;
                            self.options[n] = self.options[n] || {};
                            if (self.options.debug) {
                                console.log('[tajin.core] init', n, self.options[n]);
                            }
                            try {
                                inits[i].init(next, self.options[n], self);
                            } catch (e) {
                                inits[i].tajin_init = e;
                                throw  e;
                            }
                        } else if (i === inits.length) {
                            if (self.options.debug) {
                                console.log('[tajin.core] onready - init completed with options', self.options);
                            }
                            ready = 2;
                            if ($.isFunction(self.options.onready)) {
                                self.options.onready(self);
                            }
                            while (listeners.length) {
                                listeners.shift()(self);
                            }
                        }
                    };
                next();
            }
        },
        toString: function () {
            return "Tajin Framework, version ${project.version}, modules: " + this.modules();
        },
        modules: function () {
            return $.map(modules, function (e) {
                return e.name;
            });
        },
        ready: function (fn) {
            if ($.isFunction(fn)) {
                if (ready === 2) {
                    fn(this);
                } else {
                    listeners.push(fn);
                }
            }
        }
    };
    w.tajin = new w.Tajin();
}(window, jQuery));
