if (!window.timer) {
    (function () {

        var intervals = {};
        var timeouts = {};

        window.timer = {

            list:function () {
                return {
                    intervals:intervals,
                    timeouts:timeouts
                }
            },

            timeout:function (id, delay, fun) {
                if (!(typeof fun === 'function')) {
                    throw new Error('Not a function: ' + fun);
                }
                timer.clearTimeout(id);
                timeouts[id] = {
                    func:fun.toString(),
                    uid:setTimeout(function () {
                        if (timeouts[id]) {
                            delete timeouts[id];
                            fun.apply(this, arguments);
                        }
                    }, delay)
                };
            },

            clearTimeout:function (id) {
                if (timeouts[id]) {
                    clearTimeout(timeouts[id].uid);
                    delete timeouts[id];
                }
            },

            interval:function (id, delay, fun) {
                if (!(typeof fun === 'function')) {
                    throw new Error('Not a function: ' + fun);
                }
                timer.clearInterval(id);
                intervals[id] = {
                    func:fun.toString(),
                    uid:setInterval(function () {
                        fun.apply(this, arguments);
                    }, delay)
                }
            },

            clearInterval:function (id) {
                if (intervals[id]) {
                    clearInterval(intervals[id].uid);
                    delete intervals[id];
                }
            }
        }

    })();
}
