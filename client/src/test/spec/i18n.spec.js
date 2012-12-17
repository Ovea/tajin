describe("tajin.i18n", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('i18n');
    });

    describe("at initialization", function () {

        it("is configured", function () {
            expect(tajin.options.i18n.debug).toBe(true);
            expect(tajin.options.i18n.bundles.app).toBeDefined();
            expect(tajin.options.i18n.bundles.app2).toBeDefined();
            expect(tajin.options.i18n.bundles.app3).toBeDefined();

            expect(tajin.options.i18n.attributes.length).toBe(3);
            expect(tajin.options.i18n.attributes).toContain('href');
            expect(tajin.options.i18n.attributes).toContain('src');
            expect(tajin.options.i18n.attributes).toContain('custom-attr');

            expect(tajin.options.i18n.bundles.app.location).toBe('spec/i18n/bundles');

            expect(tajin.options.i18n.bundles.app.variants.length).toBe(3);
            expect(tajin.options.i18n.bundles.app.variants).toContain('fr');
            expect(tajin.options.i18n.bundles.app.variants).toContain('fr_CA');
            expect(tajin.options.i18n.bundles.app.variants).toContain('en_US');

            expect(tajin.options.i18n.resources.length).toBe(2);
            expect(tajin.options.i18n.resources[0].path).toBe('spec/i18n/contents/pub.html');
            expect(tajin.options.i18n.resources[0].variants.length).toBe(2);
            expect(tajin.options.i18n.resources[0].variants).toContain('fr');
            expect(tajin.options.i18n.resources[0].variants).toContain('ko');

            expect(tajin.options.i18n.resources[1].path).toBe('spec/i18n/images/pub.jpg');
            expect(tajin.options.i18n.resources[1].variants.length).toBe(2);
            expect(tajin.options.i18n.resources[1].variants).toContain('fr');
            expect(tajin.options.i18n.resources[1].variants).toContain('ko');
        });

        it("has preloaded specified bundles and locales", function () {
            expect(tajin.options.i18n.bundles.app.preload).toContain('fr_CA');
            expect(tajin.options.i18n.bundles.app.preload).toContain('en');
            expect(tajin.options.i18n.bundles.app.preload).toContain('fr');
            expect(tajin.options.i18n.bundles.app.preload).toContain('en_US');
        });

        it("can be configured with a custom 'onlocalize' callback", function () {
            var called = false, old = tajin.options.i18n.onlocalize;
            tajin.options.i18n.onlocalize = function (bundle, locale, elem, key, value) {
                called = true;
            };
            tajin.i18n.load('app3', 'fr_CA', function (bundle) {
                bundle.localize(document);
                tajin.options.i18n.onlocalize = old;
            });
            waitsFor(function () {
                return called;
            }, 'too long', 1000);
        });

    });

    describe("tajin.i18n.load()", function () {

        it("loads bundle using navigator locale if none specified", function () {
            tajin.i18n.load('app', '', function (bundle) {
                expect(bundle.name).toBe('app');
                expect(bundle.locale).toBe(browser_locale());
                expect(bundle.resolved).toBe(browser_locale());
            });
        });

        it("loads bundle using specified locale", function () {
            tajin.i18n.load('app', 'fr_CA', function (bundle) {
                expect(bundle.name).toBe('app');
                expect(bundle.locale).toBe('fr_CA');
                expect(bundle.resolved).toBe('fr_CA');
            });
        });

        it("it load bundles and merge them from fr_ca => fr => en", function () {
            tajin.i18n.load('app', 'fr_CA', function (bundle) {
                expect(bundle.value('msg1')).toBe('english 1');
                expect(bundle.value('msg2')).toBe('french 2');
                expect(bundle.value('msg3')).toBe('french CA 3');
                expect(bundle.value('inner.msg')).toBe('french');
                expect(bundle.value('link')).toBe('http://goto/fr.ca');

            });
        });

        it("degrades to less specific bundle if specified one is missing", function () {
            tajin.i18n.load('app', 'fr_FR', function (bundle) {
                expect(bundle.name).toBe('app');
                expect(bundle.locale).toBe('fr_FR');
                expect(bundle.resolved).toBe('fr');
            });
        });


        it("calls provided callback when complete", function () {
            var obj = {
                f: function (bundle) {
                }
            };

            spyOn(obj, 'f');
            tajin.i18n.load('app', 'fr_CA', obj.f);
            expect(obj.f).toHaveBeenCalled();
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
            tajin.i18n.load('app', 'fr', function (bundle) {
                expect(bundle.name).toBe('app');
                expect(bundle.locale).toBe('fr');
                expect(bundle.resolved).toBe('fr');
            });
        });

        describe("bundle.value()", function () {

            it("returns translation for key", function () {
                tajin.i18n.load('app3', 'fr_CA', function (bundle) {
                    expect(bundle.value('msg1')).toBe('bundle 3 fr');
                });
            });

            it("returns undefined when no key found", function () {
                tajin.i18n.load('app3', 'fr_CA', function (bundle) {
                    expect(bundle.value('inexisting')).toBe(undefined);
                });
            });

        });

        describe("bundle.localize()", function () {

            it("localizes HTML elements using rel attribute", function () {
                var element = $('#i18n').find('span:first');

                expect(element.text()).toBe('');

                tajin.i18n.load('app', 'fr_CA', function (bundle) {
                    bundle.localize(element);
                    expect(element.text()).toBe('french CA 3');
                });
            });

            it("localizes specific HTML attributes (href, src, ...)", function () {
                var element = $('#i18n').find('a:first');
                expect(element.attr('href')).toBe('localize[link]');
                tajin.i18n.load('app', 'fr_CA', function (bundle) {
                    bundle.localize(element);
                    expect(element.attr('href')).toBe('http://goto/fr.ca');
                });
            });

            it("localizes unattached DOM fragment", function () {
                var res_fr_CA = tajin.i18n.resources('fr-CA');
                var called = false;
                res_fr_CA.html('spec/i18n/contents/template.html', function () {
                    var template = Handlebars.compile(this);
                    var html = $(template());

                    expect(html.find('span').text()).toBe('');
                    expect(html.find('a').attr('href')).toBe('localize[link]');

                    bundle.localize(html);
                    expect(html.find('span').text()).toBe('french CA 3');
                    expect(html.find('a').attr('href')).toBe('http://goto/fr.ca');

                    called = true;
                });
            });

            it("localizes to another locale same DOM fragment", function () {
                var element = $('#i18n').find('span:first');

                tajin.i18n.load('app', 'fr_CA', function (bundle) {
                    bundle.localize(element);
                    expect(element.text()).toBe('french CA 3');
                });

                tajin.i18n.load('app', 'en', function (bundle) {
                    bundle.localize(element);
                    expect(element.text()).toBe('english 3');
                });

            });

        });

    });

    describe("Object tajin.i18n.resources(locale) r", function () {

        describe("r.locale", function () {

            it("provides requested locale", function () {
                this.fail('TODO');
                // locale asked
            });

        });

        describe("r.url()", function () {

            it("gets real URL of an i18n-ized resource", function () {
                this.fail('TODO');
                // var res_fr_CA = tajin.i18n.resources('fr-CA'); res_fr_CA.url('contents/pub.html')
            });

        });

        describe("r.image()", function () {

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

        describe("r.html()", function () {

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

    });

});

describe("Handlebars integration", function () {

    it("can load, process and localize i18n-ized template", function () {
        tajin.i18n.load('app', 'fr_CA', function (bundle) {
            var res_fr_CA = tajin.i18n.resources('fr-CA');
            var called = false;
            res_fr_CA.html('spec/i18n/contents/template.html', function () {
                var template = Handlebars.compile(this);
                var html = $(template({
                    title: "My New Post",
                    body: "This is my first post!",
                    url: res_fr_CA.url('pub.jpg')
                }));

                // 1 - the template is localized
                expect(html.find('.title').text()).toBe('My New Post');
                expect(html.find('.body').text()).toBe('This is my first post!');
                expect(html.find('span').text()).toBe('');
                expect(html.find('a').attr('href')).toBe('localize[link]');
                expect(html.find('img').attr('src')).toMatch('pub_fr.jpg');

                // 2 - it can be processed
                bundle.localize(html);
                expect(html.find('.title').text()).toBe('My New Post');
                expect(html.find('.body').text()).toBe('This is my first post!');
                expect(html.find('span').text()).toBe('french CA 3');
                expect(html.find('a').attr('href')).toBe('http://goto/fr.ca');
                expect(html.find('img').attr('src')).toMatch('pub_fr.jpg');

                called = true;
            });

            waitsFor(function () {
                return called;
            }, 'too long', 10000);
        });
    });
});

function browser_locale() {
    var locale = (navigator.language || navigator.userLanguage).replace(/-/, '_').toLowerCase();
    return locale.length > 3 ? locale.substring(0, 3) + locale.substring(3).toUpperCase() : locale;
}
