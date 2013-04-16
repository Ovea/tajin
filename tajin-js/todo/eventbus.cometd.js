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
if (window.EventBus.cometd == undefined) {
    (function($) {

        window.EventBus.cometd = function(opts) {

            // config

            var options = $.extend({
                url: document.location,
                logLevel: Log.level == Log.DEBUG ? 'debug' : (Log.level == Log.INFO ? 'info' : 'warn'),
                name: 'EventBus.CometD',
                maxConnections: 2,
                backoffIncrement: 1000,
                maxBackoff: 60000,
                reverseIncomingExtensions: true,
                maxNetworkDelay: 10000,
                requestHeaders: {},
                appendMessageTypeToURL: true,
                autoBatch: false,
                advice: {
                    timeout: 60000,
                    interval: 0,
                    reconnect: 'retry'
                },
                websocketEnabled: true,
                ackEnabled: true,
                jmsEnabled: true,
                onConnect: function() {
                },
                onDisconnect: function() {
                },
                onSubscribe: function() {
                },
                onUnsubscribe: function() {
                },
                onPublish: function() {
                },
                onError: function() {
                }
            }, opts || {});

            // private variables

            var subscriptions = {};
            var _connected = false;
            var _disconnecting = false;
            var _batch = false;
            var queue = [];
            var eventbus = undefined;
            var logger = new Logger(options.name);
            var cometd = new $.Cometd(options.name);
            var MessageType = {
                SUBSCRIBE: 1,
                UNSUBSCRIBE: 2,
                PUBLISH: 3,
                ACK: 4
            };

            // private functions

            function raw_send(msg) {
                switch (msg.type) {
                    case MessageType.SUBSCRIBE:
                    {
                        if (!subscriptions[topic]) {
                            var topic = msg.topic;
                            subscriptions[topic] = cometd.subscribe(topic, function(m) {
                                logger.debug('Publishing incoming message to ' + topic);
                                eventbus.topic(topic).fire($.parseJSON(m.data), m);
                            });
                        }
                        break;
                    }
                    case MessageType.UNSUBSCRIBE:
                    {
                        if (subscriptions[msg.topic]) {
                            cometd.unsubscribe(subscriptions[msg.topic]);
                            delete subscriptions[msg.topic];
                        }
                        break;
                    }
                    case MessageType.PUBLISH:
                    {
                        cometd.publish(msg.topic, msg.data);
                        break;
                    }
                }
            }

            function send(msg) {
                if (!_batch && _connected) {
                    logger.debug('Sending message type=' + msg.type);
                    raw_send(msg)
                } else {
                    enqueue(msg);
                }
            }

            function enqueue(msg) {
                // check for duplicates and enqueue message for future send
                if (msg.type == MessageType.SUBSCRIBE || msg.type == MessageType.UNSUBSCRIBE)
                    for (var i = 0; i < queue.length; i++) {
                        if (queue[i].type == msg.type && queue[i].topic == msg.topic) {
                            return;
                        }
                    }
                logger.debug('Queueing message type=' + msg.type);
                queue.push(msg);
            }

            function dequeue() {
                if (queue.length && _connected) {
                    logger.debug('Dequeuing messages...');
                    cometd.batch(function() {
                        while (queue.length && _connected) {
                            raw_send(queue.shift());
                        }
                    });
                }
            }

            // offline messages

            function disconnected() {
                var tt = eventbus.topics();
                if (tt.length) {
                    logger.debug('Queueing re-subscription requests...');
                    var old = queue;
                    queue = [];
                    for (var i = 0; i < tt.length; i++) {
                        enqueue({
                            type: MessageType.SUBSCRIBE,
                            topic: tt[i].name
                        });
                    }
                    while (old.length) {
                        enqueue(old.shift());
                    }
                }
                options.onDisconnect.call(eventbus);
            }

            // cometd config

            cometd.configure(options);
            cometd.websocketEnabled = options.websocketEnabled;
            cometd.ackEnabled = options.ackEnabled;
            cometd.jmsEnabled = options.jmsEnabled;

            cometd.addListener('/meta/disconnect', function(message) {
                if (message.successful) {
                    _connected = false;
                    logger.info('Disconnected !');
                    disconnected();
                }
            });

            cometd.addListener('/meta/connect', function(message) {
                if (_disconnecting || cometd.isDisconnected()) {
                    _connected = false;
                } else {
                    var wasConnected = _connected;
                    _connected = message.successful;
                    if (!wasConnected && _connected) {
                        logger.debug('Connected !');
                        dequeue();
                        options.onConnect.call(eventbus);
                    } else if (wasConnected && !_connected) {
                        logger.info('Connection failed !');
                        disconnected();
                    }
                }
            });

            cometd.addListener('/meta/publish', function(message) {
                if (message.successful) {
                    logger.debug('published - ' + message.channel);
                    options.onPublish.call(eventbus, message.channel);
                } else if(!message.error) {
                    enqueue({
                        type: MessageType.PUBLISH,
                        topic: message.channel,
                        data: message.request.data
                    });
                } else {
                    logger.error('Publish to ' + message.channel + ' error: ' + message.error);
                    options.onError.call(eventbus, message.channel, message.error);
                }
            });

            cometd.addListener('/meta/subscribe', function(message) {
                if (message.successful) {
                    logger.debug('subscribed - ' + message.subscription);
                    options.onSubscribe.call(eventbus, message.subscription);
                } else {
                    enqueue({
                        type: MessageType.SUBSCRIBE,
                        topic: message.subscription
                    });
                }
            });

            cometd.addListener('/meta/unsubscribe', function(message) {
                if (message.successful) {
                    logger.debug('unsubscribed - ' + message.subscription);
                    options.onUnsubscribe.call(eventbus, message.subscription);
                } else {
                    enqueue({
                        type: MessageType.UNSUBSCRIBE,
                        topic: message.subscription
                    });
                }
            });

            // eventbus bridge

            eventbus = new EventBus({
                name: options.name,
                onPublish: function(data) {
                    logger.debug('onPublish - sending to ' + this.name);
                    data = $.toJSON(data);
                    send({
                        type: MessageType.PUBLISH,
                        topic: this.name,
                        data: data
                    });
                },
                onSubscribe: function(callback) {
                    logger.debug('onSubscribe - to ' + this.name);
                    send({
                        type: MessageType.SUBSCRIBE,
                        topic: this.name
                    });
                },
                onTopicEmpty: function() {
                    logger.debug('onTopicEmpty - unsubscribing from ' + this.name);
                    send({
                        type: MessageType.UNSUBSCRIBE,
                        topic: this.name
                    });
                },
                onBatchStart: function() {
                    _batch = true;
                    logger.debug('Start batch');
                },
                onBatchEnd: function() {
                    _batch = false;
                    logger.debug('End batch');
                    dequeue();
                }
            });

            eventbus.start = function() {
                if (!_connected) {
                    logger.debug('Starting...');
                    _disconnecting = false;
                    cometd.handshake();
                }
            };

            eventbus.stop = function() {
                if (_connected) {
                    logger.debug('Stopping...');
                    _disconnecting = true;
                    cometd.disconnect();
                }
            };

            eventbus.isConnected = function() {
                return _connected;
            };

            eventbus.cometd = cometd;

            return eventbus;
        };

    })(jQuery);
}
