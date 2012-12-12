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

                // Page transition events
                $(document).bind('pagebeforeshow', function (event, data) {
                    cancelTimer();
                    var page = $(event.target);
                    var name = page.attr('id');
                    logger.debug('pagebeforeshow => ' + name);
                    bus.topic('/event/ui/view/pagebeforeshow').publish(page, data);
                    bus.topic('/event/ui/view/pagebeforeshow/' + name).publish(page, event, data);
                });
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


