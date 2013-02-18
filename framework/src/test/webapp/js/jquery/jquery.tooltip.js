(function ($) {
    $.fn.tooltip = function (opts) {
        opts = $.extend({
            type:'success',
            msg:''
        }, opts || {});
        if(opts.fade === undefined) {
            opts.fade = opts.type === 'success';
        }
        var panel = $(this);
        var element = $(this).find('.ztooltip');
        element.find('span:first').text(opts.msg);
        element.removeClass('error').removeClass('success').addClass(opts.type);

        var t = panel.data('t_fade');
        if (t != undefined) {
            clearTimeout(t);
        }
        panel.stop().hide().fadeIn(500, function () {
            if (opts.fade) {
                var v = setTimeout(function () {
                    if(panel.data('t_fade') === v) {
                        panel.fadeOut(1000, 'linear');
                    }
                }, 3000);
                panel.data('t_fade', v);
            }
        });
        return $(this);
    };
})(jQuery);
