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

if (window.ovea === undefined) {
    window.ovea = {};
}
if (window.ovea.Merger === undefined) {
    (function($) {

        function nano(template, data) {
            return template.replace(/\{([\w\.]*)\}/g, function (str, key) {
                var keys = key.split("."), value = data[keys.shift()];
                $.each(keys, function () {
                    value = value === undefined ? undefined : value[this];
                });
                return (value === null || value === undefined) ? "" : value;
            });
        }

        window.ovea.Merger = function() {
            var self = this;
            this.attrs = {
                show: function(val) {
                    if (!val) {
                        this.remove();
                    }
                },
                func: function(v, d) {
                    (self.attrs[v] || $.noop).call(this, d, v);
                }
            }
        };

        window.ovea.Merger.prototype = {

            toString: function() {
                return 'Merger';
            },

            attr: function(attr, func) {
                if (typeof func !== 'function') {
                    throw new Error('Not a function', func);
                }
                if (attr === 'func') {
                    throw new Error('Protected attribute: ' + attr);
                }
                this.attrs[attr] = func;
            },

            merge: function(template, data) {
                var m = this,
                    html = $($.nano(template, data)),
                    attr;
                for (attr in this.attrs) {
                    $('[' + attr + ']', html).each(function() {
                        var el = $(this),
                            cond = el.attr(attr),
                            v;
                        el.removeAttr(attr);
                        if (cond) {
                            v = cond;
                            with (data) {
                                try {
                                    v = eval(cond);
                                } catch(e) {
                                }
                            }
                            m.attrs[attr].call(el, v, data, attr, cond);
                        }
                    });
                }
                if (this.onsuccess) {
                    this.onsuccess.call(undefined, html);
                }
                return html;
            }
        }

    })(jQuery);
}
