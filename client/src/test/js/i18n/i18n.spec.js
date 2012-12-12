describe("I18N module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('i18n');
    });

    it("is configured", function () {
        this.fail('TODO');
        // can define additional attributes for localization
    });

    it("has preloaded specified bundles and locales", function () {
        this.fail('TODO');
        // see config "preload": ["fr_CA", "en", "fr", "en_US"]
    });

    it("can be configured with a custom 'onlocalize' callback", function () {
        this.fail('TODO');
    });

});

describe("tajin.i18n.load()", function () {

    it("loads bundle using navigator locale if none specified", function () {
        this.fail('TODO');
        // tajin.i18n.load('app', '', function (bundle) {...});
    });

    it("loads bundle using specified locale", function () {
        this.fail('TODO');
        // tajin.i18n.load('app', 'fr_CA', function (bundle) {...});
    });

    it("it load bundles and merge them", function () {
        this.fail('TODO');
        // tajin.i18n.load('app', 'fr_CA', function (bundle) {...});
    });

    it("degrades to less specific bundle if specified one is missing", function () {
        this.fail('TODO');
        // tajin.i18n.load('app', 'fr_FR', function (bundle) { => fallback to fr
    });


    it("calls provided callback when complete", function () {
        this.fail('TODO');
    });

    it("fires event i18n/bundle/loaded when complete", function () {
        var obj = {
            f: function () {
            }
        };
        spyOn(obj, 'f');
        tajin.event.get('i18n/bundle/loaded').listen(obj.f);
        tajin.i18n.load('app', 'fr_FR');
        expect(obj.f).toHaveBeenCalled();
    });

});

describe("I18N bundle object", function () {

    it("provides properties b.name, b.locale, b.resolved", function () {
        this.fail('TODO');
        // name of bundle, locale asked, locale resolved
    });

});

describe("bundle.value()", function () {

    it("returns translation for key", function () {
        this.fail('TODO');
        // bundle.value('existing')
    });

    it("returns undefined when no key found", function () {
        this.fail('TODO');
        // bundle.value('inexisting')
    });

});

describe("bundle.localize()", function () {

    it("localizes HTML elements using rel attribute", function () {
        this.fail('TODO');
    });

    it("localizes specific HTML attributes (href, src, ...)", function () {
        this.fail('TODO');
    });

    it("localizes unattached DOM fragment", function () {
        this.fail('TODO');
        // var html = $(template({ title: "My New Post", body: "This is my first post!", url: res_fr_CA.url('pub.jpg') })); bundle.localize(html);
    });

    it("localizes to another locale same DOM fragment", function () {
        this.fail('TODO');
    });

});

describe("tajin.i18n.resources()", function () {

    it("provides some properties", function () {
        this.fail('TODO');
        // locale asked
    });

});

describe("tajin.i18n.resources().url()", function () {

    it("gets real URL of an i18n-ized resource", function () {
        this.fail('TODO');
        // var res_fr_CA = tajin.i18n.resources('fr-CA'); res_fr_CA.url('contents/pub.html')
    });

});

describe("tajin.i18n.resources().image()", function () {

    it("loads i18n-ized image", function () {
        this.fail('TODO');
        // res_fr_CA.image('pub.jpg', function (url, error) {
    });

    it("fires event i18n/image/loaded when load completes", function () {
        this.fail('TODO');
        // res_fr_CA.image('pub.jpg')
        // tajin.event.get('i18n/image/loaded').listen(obj.f);
    });

    it("call optional callback when load completes", function () {
        this.fail('TODO');
    });

});

describe("tajin.i18n.resources().html()", function () {

    it("loads i18n-ized html template", function () {
        this.fail('TODO');
        // res_fr_CA.html('pub.html', function (url) {
    });

    it("fires event i18n/html/loaded when load completes", function () {
        this.fail('TODO');
        // res_fr_CA.html('pub.html')
        // tajin.event.get('i18n/html/loaded').listen(obj.f);
    });

    it("call optional callback when load completes", function () {
        this.fail('TODO');
    });

});

describe("Handlebars integration", function () {

    it("can load, process and localize i18n-ized template", function () {
        this.fail('TODO');
        // see full sample in other html pages
    });

});
