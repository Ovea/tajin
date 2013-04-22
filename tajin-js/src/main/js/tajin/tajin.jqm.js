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
        var self = this, restoring, current_page, the_tajin, will_change_timer, events, restoreState = function () {
            var opts = the_tajin.store.del('tajin.jqm.nav');
            if (opts) {
                restoring = true;
                events.restore.fire(opts);
                if (opts.data && opts.data.to) {
                    self.exports.changePage(opts.data.to);
                } else if (opts.data && opts.data.topic) {
                    the_tajin.events.get(opts.data.topic).fire(opts.data.data || {});
                }
                restoring = false;
            }
        };
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
                beforeshow: tajin.event.on('jqm/beforeshow'),
                show: tajin.event.on('jqm/show'),
                beforehide: tajin.event.on('jqm/beforehide'),
                hide: tajin.event.on('jqm/hide'),
                init: tajin.event.on('jqm/init'),
                first: tajin.event.on('jqm/first'),
                count: tajin.event.on('jqm/count'),
                restore: tajin.event.on('jqm/restore')
            };
            $(function () {
                restoreState();
            });
            $(document)
                .on("mobileinit",function () {
                    events.ready.fire(the_tajin.jqm);
                }).on('pagebeforeshow',function (event) {
                    current_page = $(event.target);
                    if (will_change_timer && will_change_timer.isActive()) {
                        will_change_timer.stop();
                    }
                    if (!restoring) {
                        restoreState();
                    }
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
            events.beforeshow.listen(function (page) {
                var displayed = the_tajin.store.get('tajin.jqm.page_count') || {},
                    name = page.attr('id');
                if ($.isEmptyObject(displayed)) {
                    events.first.fire(page);
                }
                displayed[name] = (displayed[name] || 0) + 1;
                the_tajin.store.put('tajin.jqm.page_count', displayed);
                events.count.fire({
                    page: page,
                    count: displayed[name]
                });
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
            },
            cancelChangePage: function () {
                if (will_change_timer && will_change_timer.isActive()) {
                    will_change_timer.stop();
                }
            },
            willChangePage: function (delay, location, callback, jqm_opts) {
                var self = this;
                this.cancelChangePage();
                will_change_timer = the_tajin.timer.schedule('tajin.jqm.willChangePage', delay, false, function () {
                    self.changePage(location, callback, jqm_opts);
                });
            },
            changePage: function (location, callback, jqm_opts) {
                var ext = the_tajin.options.jqm.extension || '.html';
                if (location.lastIndexOf(ext) === -1) {
                    location += ext;
                }
                if ($.isFunction(callback)) {
                    events.beforeshow.once(function (page) {
                        var p_uri = page.attr('data-url');
                        if (location.charAt(0) !== '/') {
                            location = '/' + location;
                        }
                        if (p_uri.length === location.length + p_uri.lastIndexOf(location)) {
                            callback(page);
                        }
                    });
                }
                if ($.mobile) {
                    $.mobile.changePage(location, jqm_opts || {});
                } else {
                    throw new Error('jQuery Mobile script not found !');
                }
            },
            redirect: function (location, data) {
                the_tajin.store.put('tajin.jqm.nav', {
                    from: this.pageName(),
                    data: data
                });
                window.location = location;
            },
            redirectThenFire: function (location, topic, data) {
                this.redirect(location, {
                    topic: topic,
                    data: data
                });
                window.location = location;
            }
        };
    };

    w.tajin.install(new JQMModule());

}(window, jQuery));
