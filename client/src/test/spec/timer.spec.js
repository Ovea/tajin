describe("tajin.timer", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('timer');
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

        describe("t.toString()", function () {

            it("describes object", function () {
                this.fail('TODO');
            });

        });

        describe("t.stop()", function () {

            it("stops this timer", function () {
                this.fail('TODO');
            });

        });

        describe("t.id", function () {

            it("returns timer ID", function () {
                this.fail('TODO');
            });

        });

        describe("t.isActive()", function () {

            it("returns if timer is till alive", function () {
                this.fail('TODO');
            });

        });

    });

});
