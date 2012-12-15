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
    var LogModule = function () {
        var root_level = 'none',
            loggers = {
                all: {
                    level: 10
                },
                log: {
                    level: 20
                },
                debug: {
                    level: 20
                },
                info: {
                    level: 30
                },
                warn: {
                    level: 40
                },
                error: {
                    level: 50
                },
                none: {
                    level: 60
                }
            },
            Logger = function (name, level) {
                this.name = name;
                this.level = (level || root_level).toLowerCase();
                this.level = loggers[this.level] ? this.level : root_level;
            };

        $.each(['log', 'debug', 'info', 'warn', 'error'], function (i, m) {
            loggers[m].f = window.console && window.console[m] ?
                (typeof console[m] === 'function' ? function (args) {
                    console[m].apply(console, args);
                } : console[m]) : (loggers.log.f || $.noop);
        });

        function doLog(name, level, argum, loggerLevel) {
            if (loggers[level].level >= loggers[loggerLevel || root_level].level) {
                var i, args = [' [' + name + '] '];
                if (argum.length) {
                    args[0] += argum[0];
                    for (i = 1; i < argum.length; i++) {
                        args.push(argum[i]);
                    }
                }
                args[0] = level.toUpperCase() + args[0];
                loggers[level].f(args);
            }
        }

        Logger.prototype = {
            debug: function () {
                doLog(this.name, 'debug', arguments, this.level);
            },
            info: function (msg) {
                doLog(this.name, 'info', arguments, this.level);
            },
            warn: function (msg) {
                doLog(this.name, 'warn', arguments, this.level);
            },
            error: function (msg) {
                doLog(this.name, 'error', arguments, this.level);
            }
        };

        this.name = 'log';
        this.init = function (next, opts, tajin) {
            var l = (opts.level || root_level).toLowerCase();
            root_level = loggers[l] ? l : root_level;
            opts.level = root_level;
            next();
        };
        this.exports = {
            debug: function () {
                doLog('tajin.log', 'debug', arguments);
            },
            info: function () {
                doLog('tajin.log', 'info', arguments);
            },
            warn: function () {
                doLog('tajin.log', 'warn', arguments);
            },
            error: function () {
                doLog('tajin.log', 'error', arguments);
            },
            logger: function (name, level) {
                if (!name) {
                    throw new Error('Missing logger name');
                }
                return new Logger(name, level);
            }
        };
    };

    w.tajin.install(new LogModule());

}(window, jQuery));
