describe("Timer Module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('timer');
    });

});

describe("tajin.timer.timers", function () {

    it("exposes all current running timers", function () {
        this.fail('TODO');
    });

});

describe("tajin.timer.stop()", function () {

    it("stops a named timer (tajin.timer.stop())", function () {
        this.fail('TODO');
    });

});

describe("tajin.timer.schedule()", function () {

    it("can schedule a timer with no name (anonymous timer)", function () {
        this.fail('TODO');
    });

    it("can schedule a timer with a name", function () {
        this.fail('TODO');
    });

    it("can schedule a timer with a repeating interval", function () {
        this.fail('TODO');
    });

    it("can override an existing timer by scheduling a new one with an existing name (previous timer is stopped)", function () {
        this.fail('TODO');
    });

});

describe("Timer instance t", function () {

    it("has a t.toString() method exposing its name", function () {
        this.fail('TODO');
    });

    it("can be stopped with t.stop()", function () {
        this.fail('TODO');
    });

    it("exposes its ID as t.id", function () {
        this.fail('TODO');
    });

    it("has a t.isActive() method", function () {
        this.fail('TODO');
    });

});
