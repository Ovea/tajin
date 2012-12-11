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
    if ($.mobile) {
        w.tajin.install({
            name: 'jqm',
            requires: 'core,event,timer',
            exports: {
                init: function (next, opts) {
                    var tevent = this.event,
                        events = {
                            beforeshow: this.event.add('jqm/beforeshow'),
                            show: this.event.add('jqm/show'),
                            beforehide: this.event.add('jqm/beforehide'),
                            hide: this.event.add('jqm/hide'),
                            init: this.event.add('jqm/init')
                        },
                        fire = function (evt, event) {
                            var page = $(event.target), name = page.attr('id');
                            events[evt].fire(page);
                            if (name) {
                                tevent.get('jqm/' + evt + '/' + name).fire(page);
                            }
                        };
                    $(document).on('pagebeforeshow',function (event) {
                        fire('beforeshow', event);
                    }).on('pagebeforehide',function (event) {
                            fire('beforehide', event);
                        }).on('pageshow',function (event) {
                            fire('show', event);
                        }).on('pagehide',function (event) {
                            fire('hide', event);
                        }).on('pageinit', function (event) {
                            fire('init', event);
                        });
                    next();
                }
            }
        });

    }
}(window, jQuery));
