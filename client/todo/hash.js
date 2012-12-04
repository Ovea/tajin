(function () {

    var trigger = true;

    $(window).bind('hashchange', function (e) {
        if (trigger) {
            $(window).trigger('hash.changed');
        } else {
            trigger = true;
        }
    });

    window.hash = {

        get:function () {
            var h = window.location.hash,
                s = h.indexOf('#'),
                e = h.indexOf('#', s + 1);
            return h.substring(s + 1, e == -1 ? h.length : e);
        },

        set:function (hash, args) {
            var h = hash.charAt(0) === '#' ? hash : '#' + hash;
            if (args) {
                if ($.isArray(args)) {
                    for (var i = 0; i < args.length; i++) {
                        h += '#' + args[i];
                    }
                } else {
                    for (var i = 1; i < arguments.length; i++) {
                        h += '#' + arguments[i];
                    }
                }
            }
            if(window.location.hash != h) {
                trigger = false;
                window.location.hash = h;
            }
            return h;
        },

        arg:function (i) {
            return this.args()[i];
        },

        args:function () {
            var p = (window.location.hash || '').split('#');
            p.shift();
            p.shift();
            return p;
        }
    }

})();
