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

    var origin = document.location.origin || (document.location.protocol + '//' + document.location.host),
        pathname = document.location.pathname || '/',
        uri = origin + pathname,
        filename = pathname.substring(pathname.lastIndexOf('/') + 1) || '',
        fdup = function (el) {
            var ids = {}, total = 0, deleted = 0, id;
            $(el).find('[id]').each(function () {
                ids[this.id] = ids[this.id] ? ids[this.id] + 1 : 1;
            });
            for (id in ids) {
                if (ids.hasOwnProperty(id)) {
                    total++;
                    if (ids[id] === 1) {
                        deleted++;
                        delete ids[id];
                    }
                }
            }
            return total !== deleted ? ids : undefined;
        };

    w.tajin.install({
        name: 'util',
        onconfigure: function (next, opts, tajin) {
            if (opts.check_ids !== false) {
                $(function () {
                    if (opts.debug) {
                        console.log('[tajin.util] checking for duplicate ids');
                    }
                    var dups = fdup(document);
                    if (dups) {
                        throw new Error('Duplicate IDS found: ' + JSON.stringify(dups));
                    }
                });
            }
            next();
        },
        exports: {
            path: function (loc) {
                loc = loc || '';
                if (loc.indexOf('http') !== -1) {
                    return loc;
                }
                if (loc.charAt(0) === '/') {
                    return origin + loc;
                }
                return uri.substring(0, uri.lastIndexOf('/') + 1) + loc;
            },
            filename: function () {
                return filename;
            },
            find_duplicate_ids: fdup
        }
    });
}(window, jQuery));
