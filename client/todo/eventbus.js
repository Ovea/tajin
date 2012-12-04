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
if (window.EventBus == undefined) {
    (function () {

        Topic = function (bus, topicName) {
            this.bus = bus;
            this.name = topicName;
            this.subscribers = [];
        };

        function fire() {
            for (var i = 0; i < this.subscribers.length; i++) {
                this.subscribers[i].apply(this, arguments);
            }
        }

        Topic.prototype = {
            once:function (callback) {
                var topic = this, cb;
                cb = function () {
                    try {
                        return callback.apply(topic, arguments);
                    } finally {
                        topic.unsubscribe(cb);
                    }
                };
                topic.subscribe(cb);
            },
            subscribe:function (callback) {
                var logger = this.bus.logger;
                if (!this.isRegistered(callback)) {
                    logger.debug('Adding subscription to ' + this.name);
                    this.subscribers.push(callback);
                    this.bus.options.onSubscribe.call(this, callback);
                }
            },
            unsubscribe:function (callback) {
                if (callback) {
                    for (var i = 0; i < this.subscribers.length;) {
                        if (this.subscribers[i] == callback) {
                            this.bus.logger.debug('Removing subscription to ' + this.name);
                            this.subscribers.splice(i, 1);
                            this.bus.options.onUnsubscribe.call(this, callback);
                            if (!this.subscribers.length) {
                                this.bus.options.onTopicEmpty.call(this);
                            }
                        } else {
                            i++;
                        }
                    }
                } else {
                    this.subscribers = [];
                    this.bus.options.onTopicEmpty.call(this);
                }
            },
            isRegistered:function (callback) {
                for (var i = 0; i < this.subscribers.length; i++) {
                    if (this.subscribers[i] == callback) {
                        return true;
                    }
                }
                return false;
            },
            publish:function () {
                this.bus.logger.debug('Publishing to ' + this.name);
                this.bus.options.onPublish.apply(this, arguments)
            }

        };

        window.EventBus = function (opts) {

            var _bus = this,
                _running = false,
                _options = $.extend({
                    name:"no-name",
                    logLevel:Log.level
                }, opts || {}),
                //TODO
                _topics = {},
                _batches = 0,
                _logger = new Logger(_options.name, _options.logLevel);

            // topics wrapper

            function Topics(topics) {

            }

            // callbacks

            this.onStart = function () {
            };
            this.onStop = function () {
            };
            this.onPublish = function () {
                //TODO
                fire.apply(_bus, arguments);
            };
            this.onSubscribe = function () {
            };
            this.onUnsubscribe = function () {
            };
            this.onTopicEmpty = function () {
            };
            this.onBatchEnd = function () {
            };
            this.onBatchStart = function () {
            };

            // bus functions

            this.topics = function (n) {
                var names = [];
                if ($.isArray(n)) {
                    names = n;
                } else {
                    for (var i = 0; i < arguments.length; i++) {
                        names.push(arguments[i]);
                    }
                }
                return new Topics(names);
            };
            this.topic = function (name) {
                return this.topics(name);
            };
            this.isRunning = function () {
                return _running;
            };
            this.start = function () {
                _running = !$.isFunction(this.onStart) || this.onStart() !== false;
            };
            this.stop = function () {
                _running = $.isFunction(this.onStop) && this.onStop() === false;
            };

        };


        EventBus.prototype = {
            topic:function (name) {
                if (this._topics[name] == undefined)
                    this._topics[name] = new EventBus.Topic(this, name);
                return this._topics[name];
            },
            topics:function () {
                if (arguments.length == 0) {
                    var result = [];
                    for (var topic in this._topics) {
                        result.push(this._topics[topic])
                    }
                    return result;
                } else {
                    var bus = this;
                    var selection = [];
                    for (var i = 0; i < arguments.length; i++) {
                        selection.push(this.topic(arguments[i]))
                    }
                    return {
                        subscribe:function (callback) {
                            try {
                                bus.startBatch();
                                for (var i = 0; i < selection.length; i++) {
                                    selection[i].subscribe(callback);
                                }
                                return this;
                            } finally {
                                bus.endBatch();
                            }
                        },
                        unsubscribe:function (callback) {
                            try {
                                bus.startBatch();
                                for (var i = 0; i < selection.length; i++) {
                                    selection[i].unsubscribe(callback);
                                }
                                return this;
                            } finally {
                                bus.endBatch();
                            }
                        },
                        publish:function (data) {
                            try {
                                bus.startBatch();
                                for (var i = 0; i < selection.length; i++) {
                                    selection[i].publish(data);
                                }
                                return this;
                            } finally {
                                bus.endBatch();
                            }
                        }
                    };
                }
            },
            batch:function (func) {
                try {
                    this.startBatch();
                    func.call(this);
                } finally {
                    this.endBatch();
                }
            },
            startBatch:function () {
                this.batches++;
                if (this.batches <= 1) {
                    this.batches = 1;
                    this.options.onBatchStart.call(this);
                }
            },
            endBatch:function () {
                this.batches--;
                if (this.batches <= 0) {
                    this.batches = 0;
                    this.options.onBatchEnd.call(this);
                }
            }
        };

    })();
}
