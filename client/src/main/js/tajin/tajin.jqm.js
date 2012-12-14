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
    var current_page;
    w.tajin.install({
        name: 'jqm',
        requires: 'event,timer,store',
        init: function (next, opts, tajin) {
            if (!$.mobile) {
                throw new Error('jQuery Mobile scripts not found ! Please add them.');
            }
            var tevent = tajin.event,
                events = {
                    beforeshow: tajin.event.add('jqm/beforeshow'),
                    show: tajin.event.add('jqm/show'),
                    beforehide: tajin.event.add('jqm/beforehide'),
                    hide: tajin.event.add('jqm/hide'),
                    init: tajin.event.add('jqm/init')
                },
                fire = function (evt, event) {
                    var page = $(event.target), name = page.attr('id');
                    events[evt].fire(page);
                    if (name) {
                        tevent.get('jqm/' + evt + '/' + name).fire(page);
                    }
                };
            $(document).on('pagebeforeshow',function (event) {
                current_page = $(event.target);
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
        },
        exports: {
            page: function () {
                var p = current_page || $('body div[data-role=page]:visible');
                return p.length ? p : null;
            },
            pageName: function () {
                var a, p = this.page();
                if (p) {
                    a = p.attr('id');
                    if (a) {
                        return a;
                    }
                }
                return null;
            }
        }
    });

}(window, jQuery));
