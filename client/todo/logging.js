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
if (window.Log == undefined) {
    (function() {

        var _log = {}, methods = ["log","debug","info","warn","error"];
        for (var i = 0; i < methods.length; i++) {
            _log[methods[i]] = (function(m) {
                if ('console' in window && m in window.console) {
                    var delegate = console[m];
                    if (typeof delegate === 'function') {
                        delegate = function(args) {
                            console[m].apply(console, args);
                        };
                        delegate.toString = function() {
                            return m;
                        };
                    }
                    return delegate;
                } else if (_log.log) {
                    return _log.log;
                } else {
                    var f = function() {
                    };
                    f.toString = function() {
                        return m + ' (noop)';
                    };
                    return f;
                }
            })(methods[i]);
        }

        window.Log = {
            // constants
            DEBUG: 1,
            INFO: 2,
            WARN: 3,
            ERROR: 4,
            NONE: 5,

            // default level for static logging
            level: window['LOG_LEVEL'] || 5,

            // static logging
            is: function(level) {
                return level >= Log.level;
            },
            isDebug: function() {
                return this.is(Log.DEBUG)
            },
            isInfo: function() {
                return this.is(Log.INFO)
            },
            isWarn: function() {
                return this.is(Log.WARN)
            },
            isError: function() {
                return this.is(Log.ERROR)
            },
            debug: function() {
                Log.log(Log.DEBUG, Log.level, 'main', arguments);
            },
            info: function() {
                Log.log(Log.INFO, Log.level, 'main', arguments);
            },
            warn: function() {
                Log.log(Log.WARN, Log.level, 'main', arguments);
            },
            error: function() {
                Log.log(Log.ERROR, Log.level, 'main', arguments);
            },

            // internal
            log: function(levelWanted, levelNow, name, argum) {
                if (levelWanted >= levelNow) {
                    var args = [' [' + name + '] '];
                    if(argum.length) {
                        args[0] += argum[0];
                        for (var i = 1; i < argum.length; i++) {
                            args.push(argum[i]);
                        }
                    }
                    switch (levelWanted) {
                        case Log.DEBUG:
                            args[0] = 'DEBUG' + args[0];
                            _log.debug(args);
                            break;
                        case Log.INFO:
                            args[0] = 'INFO' + args[0];
                            _log.info(args);
                            break;
                        case Log.WARN:
                            args[0] = 'WARN' + args[0];
                            _log.warn(args);
                            break;
                        case Log.ERROR:
                            args[0] = 'ERROR' + args[0];
                            _log.error(args);
                            break;
                    }
                }
            }

        };

        window.Logger = function(name, level) {
            this.name = name;
            this.level = level || Log.level;
        };

        window.Logger.prototype = {
            is: function(level) {
                return level >= this.level;
            },
            isDebug: function() {
                return this.is(Log.DEBUG)
            },
            isInfo: function() {
                return this.is(Log.INFO)
            },
            isWarn: function() {
                return this.is(Log.WARN)
            },
            isError: function() {
                return this.is(Log.ERROR)
            },
            log: function(wanted, args) {
                Log.log(wanted, this.level, this.name, args)
            },
            debug: function() {
                this.log(Log.DEBUG, arguments);
            },
            info: function(msg) {
                this.log(Log.INFO, arguments);
            },
            warn: function(msg) {
                this.log(Log.WARN, arguments);
            },
            error: function(msg) {
                this.log(Log.ERROR, arguments);
            }
        }

    })();
}
