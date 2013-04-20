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
    var cb_uid = 1,
        Topic = function (opts) {
            if ($.type(opts) === 'string') {
                opts = {
                    id: opts
                };
            }
            if (!$.isPlainObject(opts)) {
                opts = {};
            }
            if ($.type(opts.id) !== 'string') {
                throw new Error('Missing Topic ID');
            }
            this.listeners = [];
            this.context = opts.context;
            this.id = opts.id;
            this.stateful = opts.state || opts.stateful || false;
            this.remote = opts.remote || false;
            this.data = undefined;
            this.time = undefined;
            this.toString = function () {
                return 'Topic(id=' + this.id + ', stateful=' + this.stateful + ', remote=' + this.remote + ', time=' + this.time + ')';
            };
        },
        forward = function (array, fn1, fn2, fnN) {
            // forward a call to a list to each element of the list
            var f;
            for (f = 1; f < arguments.length; f++) {
                (function (fn) {
                    array[fn] = function () {
                        var i;
                        for (i = 0; i < array.length; i++) {
                            array[i][fn].apply(array[i], arguments);
                        }
                    }
                }(arguments[f]));
            }
        },
        sync = function (topics, fn) {
            return function (cb) {
                var i, args = [], triggered = {};
                for (i = 0; i < topics.length; i++) {
                    (function (i) {
                        topics[i][fn](function (arg) {
                            args[i] = arg;
                            triggered[i] = true;
                            var j;
                            for (j = 0; j < topics.length; j++) {
                                if (!triggered[j]) {
                                    return;
                                }
                            }
                            j = topics.syncReset;
                            topics.syncReset = function () {
                                args = [];
                                triggered = {};
                            };
                            try {
                                cb.apply(topics, args);
                            } finally {
                                topics.syncReset = j;
                            }
                        });
                    }(i));
                }
            };
        },
        Topics = function (topics) {
            forward(topics, 'fire', 'listen', 'once', 'remove', 'reset');
            topics.sync = sync(topics, 'listen');
            topics.syncOnce = sync(topics, 'once');
            topics.toString = function () {
                var s = 'Topics(', i;
                for (i = 0; i < topics.length; i++) {
                    s += topics[i].id + (i === topics.length - 1 ? '' : ',');
                }
                return s + ')';
            };
            return topics;
        },
        EventModule = function () {
            var em_topics = {};
            // module name
            this.name = 'event';
            // module config
            this.onconfigure = function (tajin, opts) {
                //TODO MATHIEU - add remote features here with cometd if opts.remote
            };
            // module exports
            this.exports = {
                // tajin.event.on(...)
                on: function (topic1, topic2, topicN, options) {
                    // parses arguments: can be a list of topics, an array of topics, and optional args at the end
                    if (arguments.length === 0) {
                        throw new Error('Missing topic names');
                    }
                    var i, j,
                        max = arguments.length - 1,
                        g_opts = arguments[max],
                        topics = [],
                        ids;
                    if (!$.isPlainObject(g_opts)) {
                        g_opts = {};
                        max = arguments.length;
                    }
                    for (i = 0; i < max; i++) {
                        ids = arguments[i];
                        if (!$.isArray(ids)) {
                            ids = [ids];
                        }
                        for (j = 0; j < ids.length; j++) {
                            if (!em_topics[ids[j]]) {
                                // if topic id does not exist, create it
                                em_topics[ids[j]] = new Topic($.extend({}, g_opts, {
                                    id: ids[j]
                                }));
                            }
                            topics.push(em_topics[ids[j]]);
                        }
                    }
                    // return enhanced list of topics
                    return Topics(topics);
                }
            };
        };
    Topic.prototype = {
        fire: function (data) {
            if (this.stateful && this.data !== undefined) {
                throw new Error('fire() cannot be called again on a stateful topic. If needed, reset() must be called before or stateful flag must be removed.');
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
            for (i = 0; i < this.listeners.length; i++) {
                this.listeners[i].call(this, data);
            }
        },
        listen: function (cb) {
            var added = false;
            if ($.isFunction(cb)) {
                if (!cb.tajin_cb_uid) {
                    cb.tajin_cb_uid = cb_uid++;
                    this.listeners.push(cb);
                    added = true;
                } else if (!$.grep(this.listeners,function (l) {
                    return l.tajin_cb_uid === cb.tajin_cb_uid;
                }).length) {
                    this.listeners.push(cb);
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
                for (i = 0; i < this.listeners.length; i++) {
                    if (this.listeners[i].tajin_cb_uid === cb.tajin_cb_uid) {
                        this.listeners.splice(i, 1);
                        return true;
                    }
                }
            }
            return false;
        },
        reset: function () {
            this.data = undefined;
            this.time = undefined;
        }
    };
    w.tajin.install(new EventModule());
}(window, jQuery));
