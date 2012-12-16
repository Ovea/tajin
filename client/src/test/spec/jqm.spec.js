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

    it("register event jqm/first wich is triggered when a JQM page is shown the first time", function () {
        expect(tajin.event.has('jqm/first')).toBe(true);
    });

    it("register event jqm/count which is triggered each time a page is shown and fired {page: page, count: pageHits}", function () {
        expect(tajin.event.has('jqm/count')).toBe(true);
    });

    describe("tajin.jqm.page()", function () {

        it("returns current JQM page", function () {
            this.fail('TODO');
        });

        it("returns null if not found", function () {
            this.fail('TODO');
        });

    });

    describe("tajin.jqm.pageName()", function () {

        it("returns current JQM page name", function () {
            this.fail('TODO');
        });

        it("returns null if no current page or if page ID is missing", function () {
            this.fail('TODO');
        });

    });

    describe("tajin.jqm.changePage()", function () {

        it("changes current JQM page", function () {
            this.fail('TODO'); // tajin.jqm.changePage(page)
        });

        it("can run a callback after page changed", function () {
            this.fail('TODO'); // tajin.jqm.changePage(page, callback)
        });

        it("can be provided by optional JQM options for page transition", function () {
            this.fail('TODO'); // tajin.jqm.changePage(page, callback, jqmOpts)
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
