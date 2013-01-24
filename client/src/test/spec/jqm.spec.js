describe("tajin.jqm", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
        expect(tajin.modules()).toContain('jqm');
        expect(tajin.modules()).toContain('timer');
    });

    it("register event jqm/init", function () {
        expect(tajin.event.has('jqm/init')).toBe(true);
    });

    it("register event jqm/beforeshow", function () {
        expect(tajin.event.has('jqm/beforeshow')).toBe(true);
    });

    it("register event jqm/show", function () {
        expect(tajin.event.has('jqm/show')).toBe(true);
    });

    it("register event jqm/beforehide", function () {
        expect(tajin.event.has('jqm/beforehide')).toBe(true);
    });

    it("register event jqm/hide", function () {
        expect(tajin.event.has('jqm/hide')).toBe(true);
    });

    it("register event jqm/first which is triggered when a JQM page is shown the first time", function () {
        expect(tajin.event.has('jqm/first')).toBe(true);
    });

    it("register event jqm/count which is triggered each time a page is shown and fired {page: page, count: pageHits}", function () {
        expect(tajin.event.has('jqm/count')).toBe(true);
    });

    describe("tajin.jqm.page()", function () {

        it("returns current JQM page", function () {
            var current_page = tajin.jqm.page();
            expect(current_page.selector).toBe('');
            expect(current_page.attr('id')).toBeUndefined();
        });

    });

    describe("tajin.jqm.pageName()", function () {

        it("returns null if no current page or if page ID is missing", function () {
            expect(tajin.jqm.pageName()).toBeNull()
        });

        it("returns current JQM page name", function () {
            $('body div[data-role=page]:visible a').click();
            waitsFor(function () {
                return tajin.jqm.pageName() === 'page_1';
            }, '', 1000);
        });

    });

    describe("tajin.jqm.changePage()", function () {

        it("changes current JQM page", function () {
            expect(tajin.jqm.pageName()).toBe('page_1');
            tajin.jqm.changePage('page_2');
            waitsFor(function () {
                return tajin.jqm.pageName() === 'page_2';
            }, '', 1000);
        });

        it("can run a callback after page changed", function () {
            expect(tajin.jqm.pageName()).toBe('page_2');
            var called;
            tajin.jqm.changePage('page_1', function () {
                called = true;
            });
            waitsFor(function () {
                return called;
            }, '', 1000);
        });

        it("can be provided by optional JQM options for page transition", function () {
            expect(tajin.jqm.pageName()).toBe('page_1');
            $('#page_2').append('<span id="my-content">some content to be removed</span>');
            expect($('#page_2').find('#my-content').length).toBe(1);
            var loaded;
            tajin.event.get('jqm/show/page_2').once(function (page) {
                expect(tajin.jqm.pageName()).toBe('page_2');
                expect($('#page_2').find('#my-content').length).toBe(0);
                loaded = true;
            });
            tajin.jqm.changePage(window.location.href, null, {
                allowSamePageTransition: true,
                transition: 'none',
                showLoadMsg: false,
                reloadPage: true
            });
            waitsFor(function () {
                return loaded;
            }, 'lod page 2', 10000);

        });

    });

    describe("tajin.jqm.willChangePage()", function () {

        it("schedule a future JQM page change", function () {
            this.fail('TODO'); // tajin.jqm.willChangePage(delayMs, page)
        });

        it("can run a callback after page changed", function () {
            this.fail('TODO'); // tajin.jqm.willChangePage(delayMs, page, callback)
        });

        it("can be provided by optional JQM options for page transition", function () {
            this.fail('TODO'); // tajin.jqm.willChangePage(delayMs, page, callback, jqmOpts)
        });

    });

    describe("tajin.jqm.cancelChangePage()", function () {

        it("cancels a scheduled page change", function () {
            this.fail('TODO'); // tajin.jqm.cancelChangePage()
        });

    });

    describe("tajin.jqm.redirect()", function () {

        it("redirects to a page", function () {
            this.fail('TODO'); // tajin.jqm.redirect()
        });

    });

    describe("tajin.jqm.redirectAndFire()", function () {

        it("redirects to a page then fires an event with specified data", function () {
            this.fail('TODO'); // tajin.jqm.redirectAndFire()
        });

    });

    describe("tajin.event.get('jqm/init')", function () {

        it("Event triggered at page initialization", function () {
            this.fail('TODO'); // tajin.jqm.redirect()
        });

    });

    describe("RESET')", function () {

        it("removes hash in address bar", function () {
            window.location.hash = '';
            $.mobile.changePage('suite.html');
        });

    });

});

/*
 // TODO david: complete for other events....
 tajin.ready(function () {
 tajin.event.get('jqm/init').listen(function (page) {
 console.log('init', page.attr('id'));
 });
 tajin.event.get('jqm/init/page_1').listen(function (page) {
 console.log('init -- page 1');
 });
 tajin.event.get('jqm/beforeshow').listen(function (page) {
 console.log('beforeshow', page.attr('id'));
 });
 tajin.event.get('jqm/show').listen(function (page) {
 console.log('show', page.attr('id'));
 });
 tajin.event.get('jqm/show/page_2').listen(function (page) {
 console.log('show -- page_2');
 });
 tajin.event.get('jqm/beforehide').listen(function (page) {
 console.log('beforehide', page.attr('id'));
 });
 tajin.event.get('jqm/hide').listen(function (page) {
 console.log('hide', page.attr('id'));
 });
 });*/

//describe("e.listen()", function () {
//
//    it("adds a listener", function () {
//        var evt = tajin.event.add();
//        var obj = {
//            l: function () {
//            }
//        };
//        spyOn(obj, 'l');
//        evt.listen(obj.l);
//        evt.fire();
//        expect(obj.l).toHaveBeenCalled();
//    });
//
//    it("checks for multiple registration errors", function () {
//        var evt = tajin.event.add(), c = 0;
//        var obj = {
//            l: function () {
//                c++;
//            }
//        };
//        evt.listen(obj.l);
//        evt.listen(obj.l);
//        evt.listen(obj.l);
//        evt.fire();
//        expect(c).toBe(1);
//    });
//
//});
