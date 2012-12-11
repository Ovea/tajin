describe("jQuery Mobile Module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
        expect(tajin.modules()).toContain('jqm');
        expect(tajin.modules()).toContain('timer');
    });

    it("exports all JQM events", function () {
        this.fail('TODO');
    });

});

describe("JQM Page Init Event", function () {

    it("can be listened", function () {
        this.fail('TODO');
    });

});

describe("JQM Page Show Event", function () {

    it("can be listened", function () {
        this.fail('TODO');
    });

});

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
});