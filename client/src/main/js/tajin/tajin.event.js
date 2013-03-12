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
/*global window, jQuery, console*/
(function (w, $) {
    "use strict";
    var EventModule = function () {
        var e_uid = 1,
            e_cb_uid = 1,
            events = {},
            t_add = function (opts) {
                if ($.type(opts) === 'string') {
                    opts = {
                        id: opts
                    };
                }
                if (!$.isPlainObject(opts)) {
                    opts = {};
                }
                if ($.type(opts.id) !== 'string') {
                    opts.id = 'anon-' + e_uid++;
                }
                if (events[opts.id]) {
                    throw new Error('Duplicate event: ' + opts.id);
                }
                var listeners = [];
                events[opts.id] = {
                    context: opts.context,
                    id: opts.id,
                    stateful: opts.state || opts.stateful || false,
                    remote: opts.remote || false,
                    data: undefined,
                    time: undefined,
                    toString: function () {
                        return 'Event(id=' + this.id + ', stateful=' + this.stateful + ', remote=' + this.remote + ', time=' + this.time + ')';
                    },
                    fire: function (data) {
                        if (this.stateful && this.data !== undefined) {
                            throw new Error('fire() cannot be called again on a stateful event. If needed, reset() must be called before or stateful flag must be removed.');
                        }
                        if (arguments.length > 1) {
                            throw new Error('fire() only accept at most one argument');
                        }
                        if (data === undefined) {
                            data = null;
                        }
                        if (this.stateful) {
                            this.data = data;
                        }
                        var i;
                        this.time = (new Date()).getTime();
                        for (i = 0; i < listeners.length; i++) {
                            listeners[i].call(this, data);
                        }
                    },
                    listen: function (cb) {
                        var added = false;
                        if ($.isFunction(cb)) {
                            if (!cb.tajin_cb_uid) {
                                cb.tajin_cb_uid = e_cb_uid++;
                                listeners.push(cb);
                                added = true;
                            } else if (!$.grep(listeners,function (l) {
                                return l.tajin_cb_uid === cb.tajin_cb_uid;
                            }).length) {
                                listeners.push(cb);
                                added = true;
                            }
                            if (added && this.stateful && this.data !== undefined) {
                                cb.call(this, this.data);
                            }
                        }
                        return added;
                    },
                    once: function (cb) {
                        if ($.isFunction(cb)) {
                            var self = this, f = function (data) {
                                self.remove(f);
                                cb.call(this, data);
                            };
                            self.listen(f);
                        }
                    },
                    remove: function (cb) {
                        if (cb.tajin_cb_uid) {
                            var i;
                            for (i = 0; i < listeners.length; i++) {
                                if (listeners[i].tajin_cb_uid === cb.tajin_cb_uid) {
                                    listeners.splice(i, 1);
                                    return true;
                                }
                            }
                        }
                        return false;
                    },
                    destroy: function () {
                        this.reset();
                        listeners = [];
                        delete events[this.id];
                    },
                    reset: function () {
                        this.data = undefined;
                        this.time = undefined;
                    }
                };
                return events[opts.id];
            },
            toEventList = function (events) {
                var syncf = function (call, cb) {
                    var m = events.length, i, args = [], triggered = {}, f = function (idx) {
                        events[i][call](function (arg) {
                            args[idx] = arg;
                            triggered[idx] = true;
                            var j;
                            for (j = 0; j < m; j++) {
                                if (!triggered[j]) {
                                    return;
                                }
                            }
                            if(!$.isFunction(events.syncReset)) {
                                events.syncReset = function() {
                                    args = [];
                                    triggered = {};
                                }
                            }
                            cb.apply(events, args);
                        });
                    };
                    for (i = 0; i < m; i++) {
                        f(i);
                    }
                };
                $.extend(events, {
                    toString: function () {
                        var s = 'EventList(', i;
                        for (i = 0; i < events.length; i++) {
                            s += events[i].id + (i === events.length - 1 ? '' : ',');
                        }
                        return s + ')';
                    },
                    fire: function (data) {
                        var i;
                        for (i = 0; i < events.length; i++) {
                            events[i].fire(data);
                        }
                    },
                    listen: function (cb) {
                        var i;
                        for (i = 0; i < events.length; i++) {
                            events[i].listen(cb);
                        }
                    },
                    once: function (cb) {
                        var i;
                        for (i = 0; i < events.length; i++) {
                            events[i].once(cb);
                        }
                    },
                    remove: function (cb) {
                        var i;
                        for (i = 0; i < events.length; i++) {
                            events[i].remove(cb);
                        }
                    },
                    destroy: function () {
                        var i;
                        for (i = 0; i < events.length; i++) {
                            events[i].destroy();
                        }
                    },
                    reset: function () {
                        var i;
                        for (i = 0; i < events.length; i++) {
                            events[i].reset();
                        }
                    },
                    sync: function (cb) {
                        syncf('listen', cb);
                    },
                    syncOnce: function (cb) {
                        syncf('once', cb);
                    }
                });
                return events;
            };
        this.name = 'event';
        this.oninstall = function (tajin) {
            var ready = t_add({
                id: 'tajin/ready',
                state: true
            });
            tajin.ready(function () {
                ready.fire();
            });
        };
        this.onconfigure = function (next, opts, tajin) {
            //TODO MATHIEU - add remote features here with cometd if opts.remote
            next();
        };
        this.exports = {
            get: function (id, opts) {
                if (!events[id]) {
                    this.add($.extend({}, opts, {
                        id: id
                    }));
                }
                return events[id];
            },
            getAll: function () {
                var i, m, events = [], g_opts = arguments[arguments.length - 1];
                if (!$.isPlainObject(g_opts)) {
                    g_opts = undefined;
                }
                for (i = 0, m = g_opts ? arguments.length - 1 : arguments.length; i < m; i++) {
                    events.push(this.get(arguments[i], g_opts));
                }
                return toEventList(events);
            },
            has: function (id) {
                return !!events[id];
            },
            addAll: function () {
                if (arguments.length <= 1) {
                    return t_add.apply(this, arguments);
                }
                var i, m, o, events = [], g_opts = arguments[arguments.length - 1];
                if (!$.isPlainObject(g_opts)) {
                    g_opts = undefined;
                }
                for (i = 0, m = g_opts ? arguments.length - 1 : arguments.length; i < m; i++) {
                    o = arguments[i];
                    if (typeof o === 'string') {
                        o = {
                            id: o
                        };
                    }
                    events.push(t_add($.extend({}, g_opts || {}, o)));
                }
                return toEventList(events);
            },
            add: t_add,
            reset: function (id) {
                this.get(id).reset();
            },
            destroy: function (id) {
                this.get(id).destroy();
            }
        };
    };
    w.tajin.install(new EventModule());
}(window, jQuery));
