describe("Core", function () {

    it("is installed", function () {
        expect(tajin).not.toBe(undefined);
    });

    it("have one module (core)", function () {
        expect(tajin.modules().length).toBe(1);
        expect(tajin.modules()).toContain('core');
    });

    it("cannot be initialized (tajin.init()) if there was module installation errors", function () {
        this.fail('TODO');
    });

    it("cannot be initialized twice or more", function () {
        this.fail('TODO');
    });

    it("optional callback method 'onready' called when initialization finished", function () {
        this.fail('TODO');
    });

});

describe("Module", function () {

    it("can add (install) a module", function () {
        tajin.install({name: 'module1'});

        expect(tajin.modules().length).toBe(2);
        expect(tajin.modules()).toContain('core');
        expect(tajin.modules()).toContain('module1');
    });

    it("can remove (uninstall) a module", function () {
        tajin.install({name: 'module1'});
        tajin.install({name: 'module2'});

        expect(tajin.modules().length).toBe(3);
        expect(tajin.modules()).toContain('core');
        expect(tajin.modules()).toContain('module1');
        expect(tajin.modules()).toContain('module2');

        tajin.uninstall('module1');

        expect(tajin.modules().length).toBe(2);
        expect(tajin.modules()).toContain('core');
        expect(tajin.modules()).toContain('module2');
    });

    it("dependencies must be installed first", function () {
        this.fail('TODO');
    });

    it("overrides module previously installed with same name", function () {
        this.fail('TODO');
    });

    it("is initialized when Tajin is initialized", function () {
        this.fail('TODO');
    });

    it("cannot be installed if there was previously installation errors", function () {
        this.fail('TODO');
    });

});
