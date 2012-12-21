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
        var self = this, current_page, the_tajin, will_change_timer, events, restoreState = function () {
            var opts = the_tajin.store.del('tajin.jqm.nav');
            if (opts) {
                self.restoring = true;
                events.restore.fire(opts);
                if (opts.to) {
                    self.exports.changePage(opts.to);
                }
                self.restoring = false;
            }
        };
        this.name = 'jqm';
        this.requires = 'event,timer,store';
        this.init = function (next, opts, tajin) {
            if (!$.mobile) {
                throw new Error('jQuery Mobile scripts not found ! Please add them.');
            }
            the_tajin = tajin;
            var tevent = tajin.event,
                fire = function (evt, event) {
                    var page = $(event.target), name = page.attr('id');
                    events[evt].fire(page);
                    if (name) {
                        tevent.get('jqm/' + evt + '/' + name).fire(page);
                    }
                };
            events = {
                beforeshow: tajin.event.add('jqm/beforeshow'),
                show: tajin.event.add('jqm/show'),
                beforehide: tajin.event.add('jqm/beforehide'),
                hide: tajin.event.add('jqm/hide'),
                init: tajin.event.add('jqm/init'),
                first: tajin.event.add('jqm/first'),
                count: tajin.event.add('jqm/count'),
                restore: tajin.event.add('jqm/restore')
            };
            $(document).on('pagebeforeshow',function (event) {
                current_page = $(event.target);
                if (will_change_timer && will_change_timer.isActive()) {
                    will_change_timer.stop();
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
            next();
        };
        this.exports = {
            page: function () {
                var p = current_page || $('body div[data-role=page]:visible');
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
                if ($.isFunction(callback)) {
                    events.beforeshow.once(function (page) {
                        var p_uri = page.attr('data-url');
                        if (p_uri.charAt(0) !== '/') {
                            p_uri += '/';
                        }
                        if (location.charAt(0) !== '/') {
                            location += '/';
                        }
                        if (p_uri === location + '.html') {
                            callback(page);
                        }
                    });
                }
                $.mobile.changePage(location + '.html', jqm_opts || {});
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
