if (jaxspot.navigation == undefined) {
    (function ($) {

        var logger = new Logger('jaxspot.navigation'),
            storage = new Store({
                order:['html5', 'page']
            });

        jaxspot.navigation = new window.jqmcontrib.Navigation(jaxspot.bus.local);

        jaxspot.navigation.changePageAndCall = function (uri, func) {
            if ($.isFunction(func)) {
                jaxspot.bus.local.topic('/event/ui/view/pagebeforeshow').once(function (page) {
                    var p_uri = page.attr('data-url');
                    if (p_uri.charAt(0) != '/') {
                        p_uri += '/'
                    }
                    if (uri.charAt(0) != '/') {
                        uri += '/'
                    }
                    if (p_uri == uri + '.html') {
                        func.call(page);
                    }
                });
            }
            jaxspot.navigation.changePage(uri);
        };

        jaxspot.navigation.redirectAndPublish = function (location, topic, data) {
            logger.debug('Preparing redirection and publish', location, topic);
            jaxspot.navigation.redirect(options.ws.app, {
                topic:topic,
                data:data
            });
        };

        jaxspot.navigation.once = function (id, func) {
            var displayed = storage.get('displayed') || {};
            if ($.isEmptyObject(displayed) || !displayed['__' + id]) {
                logger.debug('First time, calling function for: ' + id);
                func();
                displayed['__' + id] = true;
                storage.set('displayed', displayed);
            } else {
                logger.debug('Skipping function call for: ' + id);
            }
        };

        jaxspot.bus.local.topic('/event/ui/view/pagebeforeshow').subscribe(function (page) {
            var displayed = storage.get('displayed') || {},
                name = page.attr('id');
            if ($.isEmptyObject(displayed)) {
                logger.debug('First time on website !');
                jaxspot.bus.local.topic('/event/ui/display-count/firstTime').publish(page);
            }
            displayed[name] = (displayed[name] || 0) + 1;
            storage.set('displayed', displayed);
            logger.debug('Display count on page', name, displayed[name]);
            jaxspot.bus.local.topic('/event/ui/display-count/' + name).publish(page, displayed[name]);
        });

    })(jQuery);
}
