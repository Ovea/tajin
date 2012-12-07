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
/*global window*/
(function (w) {
    "use strict";
    w.tajin.install({
        name: 'util',
        requires: 'core',
        exports: {
            path: function (loc) {
                loc = loc || '';
                if (loc.indexOf('http') !== -1) {
                    return loc;
                }
                if (loc.charAt(0) === '/') {
                    return document.location.origin + loc;
                }
                var p = document.location.origin + document.location.pathname;
                return p.substring(0, p.lastIndexOf('/') + 1) + loc;
            }
        }
    });
}(window));
