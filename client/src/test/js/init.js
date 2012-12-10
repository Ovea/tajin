(function () {
    var jasmineEnv = jasmine.getEnv();
    jasmineEnv.updateInterval = 1000;
    var htmlReporter = new jasmine.HtmlReporter();
    jasmineEnv.addReporter(htmlReporter);
    jasmineEnv.specFilter = function (spec) {
        return htmlReporter.specFilter(spec);
    };
    var currentWindowOnload = window.onload;
    window.onload = function () {
        if (currentWindowOnload) {
            currentWindowOnload();
        }
        execJasmine();
    };

    function execJasmine() {
        jasmineEnv.execute();
    }

})();
window.tajin_init = {
    debug: true,
    onready: function () {
        var f = document.location.pathname || '/';
        f = f.substring(f.lastIndexOf('/') + 1) || '';
        $(document).append('<script type="text/javascript" src="' + f.substring(0, f.length - 5) + '.js"/>');
    }
};
