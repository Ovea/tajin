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

    var uid = 1;

    w.tajin.install({
        name: 'timer',
        requires: 'core',
        exports: {
            timers: {},
            init: function (next, opts) {
                next();
            },
            stop: function (id) {
                if (w.tajin.timer.timers[id]) {
                    w.tajin.timer.timers[id].stop();
                }
            },
            schedule: function (id, delay, repeat, cb) {
                id = id || uid++;
                if (!delay) {
                    throw new Error('Missing delay');
                }
                if (!$.isFunction(cb)) {
                    throw new Error('Missing function');
                }
                w.tajin.timer.stop(id);
                var running = true, tid = repeat ?
                    w.setInterval(function () {
                        if (running) {
                            cb.apply(this, arguments);
                        }
                    }, delay) :
                    w.setTimeout(function () {
                        if (running) {
                            cb.apply(this, arguments);
                            w.tajin.timer.stop(id);
                        }
                    }, delay);
                w.tajin.timer.timers[id] = {
                    toString: function () {
                        return (repeat ? 'Interval ' : 'Timer ') + id + ' ' + delay + 'ms';
                    },
                    id: id,
                    stop: function () {
                        if (running) {
                            delete w.tajin.timer.timers[id];
                            running = false;
                            if (repeat) {
                                clearInterval(tid);
                            } else {
                                clearTimeout(tid);
                            }
                        }
                    }
                };
                return w.tajin.timer.timers[id];
            }
        }
    });

}(window, jQuery));
