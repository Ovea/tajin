describe("Core", function () {

    it("is installed", function () {
        expect(tajin).not.toBe(undefined);
    });

    it("have one module (core)", function() {
        expect(tajin.modules().length).toBe(1);
        expect(tajin.modules()).toContain('core');
    });

});

describe("Module", function () {

    it("can add (install) a module", function() {
        tajin.install({name:'module1'});

        expect(tajin.modules().length).toBe(2);
        expect(tajin.modules()).toContain('core');
        expect(tajin.modules()).toContain('module1');
    });

    it("can remove (uninstall) a module", function() {
        tajin.install({name:'module1'});
        tajin.install({name:'module2'});

        expect(tajin.modules().length).toBe(3);
        expect(tajin.modules()).toContain('core');
        expect(tajin.modules()).toContain('module1');
        expect(tajin.modules()).toContain('module2');

        tajin.uninstall('module1');

        expect(tajin.modules().length).toBe(2);
        expect(tajin.modules()).toContain('core');
        expect(tajin.modules()).toContain('module2');
    });

    it("");

});



// if two module with the same name is installed the previous is uninstalled tajin.uninstall

// module depends must be reach

// when core initialized each function inti off all module is called

