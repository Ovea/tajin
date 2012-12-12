describe("tajin", function () {

    it("is installed", function () {
        expect(tajin).not.toBe(undefined);
    });

    describe("tajin.modules()", function () {

        it("exposes available modules with tajin.modules()", function () {
            expect(tajin.modules().length).toBeGreaterThan(1);
            expect(tajin.modules()).toContain('core');
        });

    });

    describe("tajin.init()", function () {

        it("fails if there was module installation errors", function () {
            this.fail('TODO');
        });

        it("does nothing if called more than 1 time", function () {
            this.fail('TODO');
        });

        it("calls each module exported init() method", function () {
            this.fail('TODO');
        });

    });

    describe("tajin.ready()", function () {

        it("is called when initialization is finished, and keep state", function () {
            this.fail('TODO');
            // tajin.ready(function() {});
        });

        it("optional callback method 'onready' called when initialization finished", function () {
            this.fail('TODO');
        });

    });

    describe("tajin.install()", function () {

        it("installs a module", function () {
            tajin.install({name: 'module1'});
            expect(tajin.modules()).toContain('core');
            expect(tajin.modules()).toContain('module1');
        });

        it("check dependencies", function () {
            this.fail('TODO');
        });

        it("check for required module.name attribute", function () {
            this.fail('TODO');
        });

        it("overrides module previously installed with same name", function () {
            this.fail('TODO');
        });

        it("fails if there was previously installation error", function () {
            this.fail('TODO');
        });

    });

    describe("tajin.uninstall()", function () {

        it("uninstalls a module", function () {
            tajin.install({name: 'module1'});
            tajin.install({name: 'module2'});

            expect(tajin.modules()).toContain('core');
            expect(tajin.modules()).toContain('module1');
            expect(tajin.modules()).toContain('module2');

            tajin.uninstall('module1');

            expect(tajin.modules()).toContain('core');
            expect(tajin.modules()).toContain('module2');
        });

        it("check for required name parameter", function () {
            this.fail('TODO');
        });

    });
});
