(function () {
    var jasmineEnv = window.jasmineEnv = jasmine.getEnv(),
        htmlReporter = new jasmine.HtmlReporter();

    jasmineEnv.updateInterval = 1000;
    jasmineEnv.addReporter(htmlReporter);
    jasmineEnv.specFilter = function (spec) {
     return htmlReporter.specFilter(spec);
     };

    window.tajin_init = {
        debug: true,
        onready: function () {
            var f = document.location.pathname || '/';
            f = f.substring(f.lastIndexOf('/') + 1) || '';
            $(document).append('<script type="text/javascript" src="' + f.substring(0, f.length - 5) + '.js"/>');
            $(function () {
                jasmineEnv.execute();
            });
        },
        log: {
            level: 'all'
        }
    };

})();
