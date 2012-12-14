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
        exports: {
            timers: {},
            stop: function (id) {
                if (this.timers[id]) {
                    this.timers[id].stop();
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
                this.stop(id);
                var self = this, running = true, tid = repeat ?
                    w.setInterval(function () {
                        if (running) {
                            cb.apply(this, arguments);
                        }
                    }, delay) :
                    w.setTimeout(function () {
                        if (running) {
                            cb.apply(this, arguments);
                            self.stop(id);
                        }
                    }, delay);
                this.timers[id] = {
                    toString: function () {
                        return (repeat ? 'Interval ' : 'Timer ') + id + ' ' + delay + 'ms';
                    },
                    id: id,
                    isActive: function () {
                        return running;
                    },
                    stop: function () {
                        if (running) {
                            delete self.timers[id];
                            running = false;
                            if (repeat) {
                                clearInterval(tid);
                            } else {
                                clearTimeout(tid);
                            }
                        }
                    }
                };
                return this.timers[id];
            }
        }
    });

}(window, jQuery));
