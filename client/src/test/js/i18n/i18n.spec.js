describe("I18N module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('i18n');
    });

    it("is configured", function () {
        this.fail('TODO');
        // can define additional attributes for localization
    });

    it("can preload specified bundles and locales", function () {
        this.fail('TODO');
        // see config "preload": ["fr_CA", "en", "fr", "en_US"]
    });

    it("can be set with a custom onlocalize callback", function () {
        this.fail('TODO');
    });

    it("can load bundle using navigator locale", function () {
        this.fail('TODO');
        // tajin.i18n.load('app', '', function (bundle) {...});
    });

    it("can load bundle using specified locale", function () {
        this.fail('TODO');
        // tajin.i18n.load('app', 'fr_CA', function (bundle) {...});
    });

    it("can load bundles and merge them", function () {
        this.fail('TODO');
        // tajin.i18n.load('app', 'fr_CA', function (bundle) {...});
    });

    it("can degrade to less specific bundle", function () {
        this.fail('TODO');
        // tajin.i18n.load('app', 'fr_FR', function (bundle) { => fallback to fr
    });

    it("fire event i18n/bundle/loaded when a bundle is loaded", function () {
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

describe("I18N bundle", function () {

    it("provides some properties", function () {
        this.fail('TODO');
        // name of bundle, locale asked, locale resolved
    });

    it("returns translation for key", function () {
        this.fail('TODO');
        // bundle.value('existing')
    });

    it("returns undefined when no key found", function () {
        this.fail('TODO');
        // bundle.value('inexisting')
    });

    it("can localize HTML elements using rel attribute", function () {
        this.fail('TODO');
    });

    it("can localize specific HTML attributes (href, src, ...)", function () {
        this.fail('TODO');
    });

    it("can localize unattached DOM fragment", function () {
        this.fail('TODO');
        // var html = $(template({ title: "My New Post", body: "This is my first post!", url: res_fr_CA.url('pub.jpg') })); bundle.localize(html);
    });

});

describe("Resources bundle", function () {

    it("provides some properties", function () {
        this.fail('TODO');
        // locale asked
    });

    it("can get real URL of an i18n-ized resource", function () {
        this.fail('TODO');
        // var res_fr_CA = tajin.i18n.resources('fr-CA'); res_fr_CA.url('contents/pub.html')
    });

    it("can load i18n-ized image", function () {
        this.fail('TODO');
        // res_fr_CA.image('pub.jpg', function (url, error) {
    });

    it("can load i18n-ized image", function () {
        this.fail('TODO');
        // res_fr_CA.image('pub.jpg', function (url, error) {
    });

    it("can load i18n-ized image and listen to event i18n/image/loaded", function () {
        this.fail('TODO');
        // res_fr_CA.image('pub.jpg')
        // tajin.event.get('i18n/image/loaded').listen(obj.f);
    });

    it("can load i18n-ized html template", function () {
        this.fail('TODO');
        // res_fr_CA.html('pub.html', function (url) {
    });

    it("can load i18n-ized html template and listen to event i18n/html/loaded", function () {
        this.fail('TODO');
        // res_fr_CA.html('pub.html')
        // tajin.event.get('i18n/html/loaded').listen(obj.f);
    });

});

describe("Handlebars integration", function () {

    it("can load, process and localize i18n-ized template", function () {
        this.fail('TODO');
        // full sample
    });

});
