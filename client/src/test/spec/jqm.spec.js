describe("tajin.jqm", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
        expect(tajin.modules()).toContain('jqm');
        expect(tajin.modules()).toContain('timer');
    });

    it("register event jqm/init", function () {
        this.fail('TODO');
    });

    it("register event jqm/beforeshow", function () {
        this.fail('TODO');
    });

    it("register event jqm/show", function () {
        this.fail('TODO');
    });

    it("register event jqm/beforehide", function () {
        this.fail('TODO');
    });

    it("register event jqm/hide", function () {
        this.fail('TODO');
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
