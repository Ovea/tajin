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
    var JQMModule = function () {
        var current_page, the_tajin, events
        this.name = 'jqm';
        this.requires = 'event,timer,store';
        this.oninstall = function (tajin) {
            the_tajin = tajin;
            var tevent = tajin.event,
                fire = function (evt, event) {
                    var page = $(event.target), name = page.attr('id');
                    page.name = name;
                    events[evt].fire(page);
                    if (name) {
                        tevent.on('jqm/' + evt + '/' + name).fire(page);
                    }
                };

            events = {
                ready: tajin.event.on({
                    id: 'jqm/ready',
                    state: true
                }),
                // Page load events
                beforeload: tajin.event.on('jqm/beforeload'),
                load: tajin.event.on('jqm/load'),
                loadfailed: tajin.event.on('jqm/loadfailed'),

                // Page change events
                beforechange: tajin.event.on('jqm/beforechange'),
                change: tajin.event.on('jqm/change'),
                changefailed: tajin.event.on('jqm/changefailed'),

                // Page transition events
                beforeshow: tajin.event.on('jqm/beforeshow'),
                beforehide: tajin.event.on('jqm/beforehide'),
                show: tajin.event.on('jqm/show'),
                hide: tajin.event.on('jqm/hide'),

                // Page initialization events
                beforecreate: tajin.event.on('jqm/beforecreate'),
                create: tajin.event.on('jqm/create'),
                init: tajin.event.on('jqm/init'),

                // Page remove events
                remove: tajin.event.on('jqm/remove'),

                // Layout events
                updatelayout: tajin.event.on('jqm/updatelayout')
            };
            $(document)
                .on('mobileinit',function () {
                    events.ready.fire(the_tajin.jqm);
                }).on('pagebeforeload', function(event, data) {
                    fire('beforeload', data);
                }).on('pageload', function(event, data) {
                    fire('load', data);
                }).on('pageloadfailed', function(event, data) {
                    fire('loadfailed', data);
                }).on('pagebeforechange', function(data) {
                    fire('beforechange', data);
                }).on('pagechange', function(data) {
                    fire('change', data);
                }).on('pagechangefailed', function(data) {
                    fire('changefailed', data);
                })



                .on('pagebeforeshow',function (event) {
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
        };
        this.exports = {
            page: function () {
                var p = current_page || $('body div[data-role=page]:visible:first');
                if (p.length) {
                    return p;
                }
                throw new Error('Illegal state: no JQM page is currently visible');
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
        };
    };

    w.tajin.install(new JQMModule());

}(window, jQuery));
