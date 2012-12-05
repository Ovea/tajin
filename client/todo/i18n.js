(function ($) {

    var logger = new Logger('zimmer.i18n'),
        bundle = new I18N({
            url:options.ws.root + '/i18n',
            bundle:'pmp',
            cache:true,
            versionning:options.version,
            variants:['fr'],
            attributes:['href'],
            onKeyFound:function (elem, key, value) {
                var type = elem.get(0).tagName.toUpperCase();
                if (type === 'INPUT') {
                    if (elem.attr('placeholder') && value)
                        elem.attr('placeholder', value);
                    else
                        elem.val(value);
                }
                else
                    elem.html(value);
            },
            onReady:function () {
                logger.debug('I18N bundle ready !');
                bus.topic('i18n/pmp/loaded').publish();
            }
        }),
        codeBundle = new I18N({
            url:options.ws.root + '/i18n',
            bundle:'code',
            cache:true,
            versionning:options.version,
            variants:['fr'],
            onKeyFound:function (elem, key, value) {
            },
            onReady:function () {
                logger.debug('I18N codes ready !');
                bus.topic('i18n/code/loaded').publish();
            }
        });

    window.i18n = {
        message:function (key, obj) {
            var msg = bundle.value(key, zimmer.user.me.locale || navigator.language) || '';
            return obj ? $.nano(msg, obj) : msg
        },
        codes:function (key) {
            return codeBundle.value(key, zimmer.user.me.locale || navigator.language) || []
        },
        code:function (key, code) {
            return $.grep(i18n.codes(key), function (e) {
                return e.code === code;
            })[0] || {};
        },
        innercodes:function (key1, code1, key2) {
            var inner = (i18n.code(key1, code1) || {})[key2] || [];
            if (!inner.length || $.isPlainObject(inner[0])) {
                return inner;
            }
            var filtered = [];
            $.each(i18n.codes(key2), function (i, e) {
                if (e.code === '' || $.inArray(e.code, inner) >= 0) {
                    filtered.push(e);
                }
            });
            return filtered;
        },
        innercode:function (key1, code1, key2, code2) {
            return $.grep(i18n.innercodes(key1, code1, key2), function (e) {
                return e.code === code2;
            })[0] || {};
        },
        localize:function (element) {
            bundle.localize(element, zimmer.user.me.locale || navigator.language);
        }
    };

    bus.topics('user/loaded', 'user/updated').subscribe(function (user) {
        bundle.localize($(document), user.locale);
        codeBundle.localize($(''), user.locale);
    });

})(jQuery);
