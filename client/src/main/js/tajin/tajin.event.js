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

    var e_uid = 1,
        e_cb_uid = 1,
        events = {};

    w.tajin.install({
        name: 'event',
        requires: 'core',
        exports: {
            init: function (next, opts) {
                var tajin = this;
                //TODO MATHIEU - add remote features here with cometd if opts.remote
                next();
            },
            get: function (id) {
                if (!events[id]) {
                    throw new Error('Event ' + id + ' does not exist');
                }
                return events[id];
            },
            has: function (id) {
                return !!events[id];
            },
            add: function (opts) {
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
                if (w.tajin.event.has(opts.id)) {
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
                        if ($.isFunction(cb)) {
                            if (!cb.tajin_cb_uid) {
                                cb.tajin_cb_uid = e_cb_uid++;
                                listeners.push(cb);
                            } else if (!$.grep(listeners,function (l) {
                                return l.tajin_cb_uid === cb.tajin_cb_uid;
                            }).length) {
                                listeners.push(cb);
                            }
                            if (this.stateful && this.data !== undefined) {
                                cb.call(this, this.data);
                            }
                        }
                    },
                    remove: function (cb) {
                        if (cb.tajin_cb_uid) {
                            var i;
                            for (i = 0; i < listeners.length; i++) {
                                if (listeners[i].tajin_cb_uid === cb.tajin_cb_uid) {
                                    listeners.splice(i, 1);
                                    break;
                                }
                            }
                        }
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
            reset: function (id) {
                w.tajin.event.get(id).reset();
            },
            resetAll: function () {
                var e;
                for (e in events) {
                    if (events.hasOwnProperty(e)) {
                        events[e].reset();
                    }
                }
            },
            destroy: function (id) {
                w.tajin.event.get(id).destroy();
            },
            destroyAll: function () {
                var e;
                for (e in events) {
                    if (events.hasOwnProperty(e)) {
                        events[e].destroy();
                    }
                }
            }
        }
    });

}(window, jQuery));
