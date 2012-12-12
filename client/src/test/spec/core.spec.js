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

        it("does nothing if called more than 1 time", function () {
            var c = 0;
            var m = {
                name: 'test',
                exports: {
                    init: function () {
                        c++;
                    }
                }
            };
            tajin.install(m);
            expect(c).toBe(1);
            tajin.init();
            expect(c).toBe(1);
        });

        it("calls each module exported init() method", function () {
            expect(tajin.options['test-module'].init_called).toBe(true);
        });

    });

    describe("tajin.ready()", function () {

        it("is called when initialization is finished, and keep state", function () {
            var obj = {f: function () {
            }};
            spyOn(obj, 'f');
            tajin.ready(obj.f);
            expect(obj.f).toHaveBeenCalled();
        });

        it("optional callback method 'onready' called when initialization finished", function () {
            expect(window.onready_called).toBe(true);
        });

    });

    describe("tajin.install()", function () {

        it("installs a module", function () {
            tajin.install({name: 'module1'});
            expect(tajin.modules()).toContain('core');
            expect(tajin.modules()).toContain('module1');
        });

        it("module init() method called at install time if tajin is initialized", function () {
            var c = 0;
            var m = {
                name: 'test',
                exports: {
                    init: function () {
                        c++;
                    }
                }
            };
            tajin.install(m);
            expect(c).toBe(1);
        });

        it("check dependencies at installation time", function () {
            var m = {
                name: 'testdep',
                requires: 'inexisting'
            };
            expect(function () {
                tajin.install(m);
            }).toThrow(new Error("Error loading module 'testdep': missing modules: inexisting"));
        });

        it("check for required module.name attribute", function () {
            var m = {};
            expect(function () {
                tajin.install(m);
            }).toThrow(new Error("Module name is missing"));
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
