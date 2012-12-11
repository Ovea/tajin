describe("jQuery Mobile Module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
        expect(tajin.modules()).toContain('jqm');
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
