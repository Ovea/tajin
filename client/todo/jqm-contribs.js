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
require("Logger", "Store", "jQuery.mobile");

if (window.jqmcontrib == undefined) {

    (function ($) {

        var logger = new Logger('jqm-contribs'),
            target,
            timer,
            storage = new Store({
                order:['html5', 'cookie', 'page']
            });

        function cancelTimer() {
            if (timer) {
                clearTimeout(timer);
                timer = null;
            }
        }

        window.jqmcontrib = {

            bridge:function (bus) {
                // Page load events
                $(document).bind('pagebeforeload', function (event, data) {
                    logger.debug('pagebeforeload');
                    bus.topic('/event/ui/view/pagebeforeload').publish(event, data);
                });

                $(document).bind('pageload', function (event, data) {
                    var page = $(data.page);
                    var name = page.attr('id');
                    logger.debug('pageload => ' + name);
                    bus.topic('/event/ui/view/pageload').publish(page, data);
                    bus.topic('/event/ui/view/pageload/' + name).publish(page, event, data);
                });

                // Page change events
                $(document).bind('pagebeforechange', function (event, data) {
                    var page;
                    var name;
                    if (typeof(data.toPage) == 'object') {
                        page = $(data.toPage);
                        name = page.attr('id');
                        logger.debug('pagebeforeenter => ' + name);
                        bus.topic('/event/ui/view/pagebeforeenter').publish(page, data);
                        bus.topic('/event/ui/view/pagebeforeenter/' + name).publish(page, event, data);
                    } else {
                        page = $(data.options.fromPage);
                        name = page.attr('id');
                        logger.debug('pagebeforeleave => ' + name);
                        bus.topic('/event/ui/view/pagebeforeleave').publish(page, data);
                        bus.topic('/event/ui/view/pagebeforeleave/' + name).publish(page, event, data);
                    }
                });

                $(document).bind('pagechange', function (event, data) {
                    var page = $(data.toPage);
                    var name = page.attr('id');
                    logger.debug('pagechange => ' + name);
                    bus.topic('/event/ui/view/pagechange').publish(page, data);
                    bus.topic('/event/ui/view/pagechange/' + name).publish(event, data);
                });

                // Page transition events
                $(document).bind('pagebeforeshow', function (event, data) {
                    cancelTimer();
                    var page = $(event.target);
                    var name = page.attr('id');
                    logger.debug('pagebeforeshow => ' + name);
                    bus.topic('/event/ui/view/pagebeforeshow').publish(page, data);
                    bus.topic('/event/ui/view/pagebeforeshow/' + name).publish(page, event, data);
                });

                $(document).bind('pagebeforehide', function (event, data) {
                    var page = $(event.target);
                    var name = page.attr('id');
                    logger.debug('pagebeforehide => ' + name);
                    bus.topic('/event/ui/view/pagebeforehide').publish(page, data);
                    bus.topic('/event/ui/view/pagebeforehide/' + name).publish(page, event, data);
                });

                $(document).bind('pageshow', function (event, data) {
                    var page = $(event.target);
                    var name = page.attr('id');
                    logger.debug('pageshow => ' + name);
                    bus.topic('/event/ui/view/pageshow').publish(page, data);
                    bus.topic('/event/ui/view/pageshow/' + name).publish(page, event, data);
                });

                $(document).bind('pagehide', function (event, data) {
                    var page = $(event.target);
                    var name = page.attr('id');
                    logger.debug('pagehide => ' + name);
                    bus.topic('/event/ui/view/pagehide').publish(page, data);
                    bus.topic('/event/ui/view/pagehide/' + name).publish(page, event, data);
                });

                // Page initialization events
                $(document).bind('pagebeforecreate', function (event) {
                    var page = $(event.target);
                    var name = page.attr('id');
                    logger.debug('pagebeforecreate => ' + name);
                    bus.topic('/event/ui/view/pagebeforecreate').publish(page);
                    bus.topic('/event/ui/view/pagebeforecreate/' + name).publish(page, event);
                });

                $(document).bind('pagecreate', function (event) {
                    var page = $(event.target);
                    var name = page.attr('id');
                    logger.debug('pagecreate => ' + name);
                    bus.topic('/event/ui/view/pagecreate').publish(page);
                    bus.topic('/event/ui/view/pagecreate/' + name).publish(page, event);
                });

                $(document).bind('pageinit', function (event) {
                    var page = $(event.target);
                    var name = page.attr('id');
                    logger.debug('pageinit => ' + name);
                    bus.topic('/event/ui/view/pageinit').publish(page);
                    bus.topic('/event/ui/view/pageinit/' + name).publish(page, event);
                });

                $(function () {
                    bus.topic('/event/dom/loaded').publish();
                });
            }
        };

        // Navigation Object
        window.jqmcontrib.Navigation = function (bus) {
            var self = this;
            this.restoring = false;

            function restoreState() {
                var opts = storage.del('ovea-restore');
                if (opts) {
                    logger.debug('Restore options', opts);
                    self.restoring = true;
                    bus.topic('/event/navigation/restoring').publish(opts);
                    if (opts.to) {
                        self.changePage(opts.to);
                    }
                    self.restoring = false;
                    logger.debug('Restore finished');
                }
            }

            bus.topics('/event/ui/view/pagebeforeshow').subscribe(function (page) {
                target = page;
                if (!self.restoring) {
                    restoreState();
                }
            });

            bus.topic('/event/dom/loaded').subscribe(function () {
                restoreState();
            });

        };

        window.jqmcontrib.Navigation.prototype = {

            cancelChangePage:function () {
                cancelTimer();
            },

            willChangePage:function (delay, location, opts, jqm_opts) {
                var self = this;
                cancelTimer();
                timer = setTimeout(function () {
                    timer = null;
                    self.changePage(location, opts, jqm_opts);
                }, delay);
            },

            changePage:function (location, opts, jqm_opts) {
                logger.debug('Changing page to', location);
                if (opts) {
                    logger.debug('Setting options', opts);
                    if (typeof opts === 'object') {
                        opts.from = this.pageName()
                    }
                    storage.set('ovea-restore', opts);
                }
                $.mobile.changePage(location + '.html', jqm_opts || {});
            },

            pageName:function () {
                var id;
                if (target) {
                    id = target.attr('id');
                }
                if (!id) {
                    id = $('body div[data-role=page]:visible').attr('id');
                }
                if (!id) {
                    throw new Error('Current page name not found');
                }
                return id;
            },

            page:function () {
                var p = target || $('body div[data-role=page]:visible');
                if (p.length) {
                    return p;
                }
                throw new Error('Current page not found');
            },

            redirect:function (location, opts) {
                logger.debug('Redirecting to: ' + location);
                if (opts) {
                    logger.debug('Setting options', opts);
                    if (typeof opts === 'object') {
                        opts.from = this.pageName()
                    }
                    storage.set('ovea-restore', opts);
                }
                window.location = location;
            }
        }
    })(jQuery);

}


