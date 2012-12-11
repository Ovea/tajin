describe("Timer Module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('timer');
    });

    it("can schedule a timer with no name (anonymous timer)", function () {
        this.fail('TODO');
    });

    it("can schedule a timer with a name", function () {
        this.fail('TODO');
    });

    it("can schedule a timer with a repeating interval", function () {
        this.fail('TODO');
    });

    it("exposes all current running timers (tajin.timer.timers)", function () {
        this.fail('TODO');
    });

    it("can stop a named timer (tajin.timer.stop())", function () {
        this.fail('TODO');
    });

    it("can override an existing timer by scheduling a new one with an existing name (previous timer is stopped)", function () {
        this.fail('TODO');
    });

});

describe("Timer instance", function () {

    it("has a toString() method exposing its name", function () {
        this.fail('TODO');
    });

    it("can be stopped", function () {
        this.fail('TODO');
    });

    it("exposes its ID as a property", function () {
        this.fail('TODO');
    });

    it("has an isActive() method", function () {
        this.fail('TODO');
    });

});
