describe("tajin", function () {

    it("is installed", function () {
        expect(tajin).not.toBe(undefined);
    });

    describe("tajin.modules()", function () {

        it("exposes available modules with tajin.modules()", function () {
            expect(tajin.modules().length).toBeGreaterThan(1);
        });

    });

    describe("tajin.configure()", function () {

        it("does nothing if called more than 1 time when succeed", function () {
            var c = 0;
            var m = {
                name: 'test',
                onconfigure: function () {
                    c++;
                }
            };
            tajin.install(m);
            expect(c).toBe(1);
            tajin.configure();
            expect(c).toBe(1);
        });

        it("calls each module exported onconfigure() method", function () {
            expect(tajin.options['test-module'].init_called).toBe(true);
        });

        it("restart initialization at failure point when a module initialization fails", function () {
            var steps = [],
                m1 = {
                    name: 'mod1',
                    onconfigure: function () {
                        steps.push(1);
                    }
                }, m2 = {
                    name: 'mod2',
                    onconfigure: function () {
                        steps.push(2);
                    }
                }, failing = {
                    name: 'failing',
                    onconfigure: function () {
                        steps.push(3);
                        throw new Error('exception');
                    }
                },
                t = new Tajin(),
                call_err = false,
                call_succ = false,
                obj = {
                    err: function (t, e) {
                        call_err = true;
                        expect(e.message).toBe('exception');
                        expect(steps.length).toBe(2);
                        expect(steps[0]).toBe(1);
                        expect(steps[1]).toBe(3);
                    },
                    succ: function (t) {
                        call_succ = true;
                        expect(steps.length).toBe(3);
                        expect(steps[2]).toBe(2);
                    }
                };
            t.install(m1);
            t.install(failing);
            t.install(m2);
            expect(steps.length).toBe(0);
            try {
                t.configure({
                    onerror: obj.err,
                    onready: obj.succ
                });
                // since no module is asynchrnous, the error is thrown in this part of code
                this.fail();
            } catch (e) {
                expect(e.message).toBe('exception');
            }
            waitsFor(function () {
                return call_err;
            }, 600, 'waiting for error');
            t.uninstall(failing);
            t.configure({
                onerror: obj.err,
                onready: obj.succ
            });
            waitsFor(function () {
                return call_succ;
            }, 600, 'waiting for success');
            expect(steps.length).toBe(3);
        });

        it("exposes module exports, options and tajin to module's init functions", function () {
            var passed = 0;
            var m = {
                name: 'module-2',
                onconfigure: function (tajin, opts) {
                    expect(typeof opts).toBe('object');
                    expect(opts.myopt).toBe('myvalue');
                    expect(window.tajin).toBe(tajin);
                    passed = 1;
                },
                exports: {
                    dummy: function () {
                    }
                }
            };
            tajin.install(m);
            expect(passed).toBe(1);
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
            expect(tajin.modules()).toContain('module1');
        });

        it("module onconfigure() method called at install time if tajin is initialized", function () {
            var c = 0;
            var m = {
                name: 'test',
                onconfigure: function () {
                    c++;
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
            var c1 = 0, c2 = 0;
            var m1 = {
                    name: 'test-m1',
                    onconfigure: function () {
                        c1++;
                    }
                },
                m2 = {
                    name: 'test-m1',
                    onconfigure: function () {
                        c2++;
                    }
                };
            tajin.install(m1);
            expect(c1).toBe(1);
            expect(c2).toBe(0);
            tajin.install(m2);
            expect(c1).toBe(1);
            expect(c2).toBe(1);
        });

    });

    describe("tajin.uninstall()", function () {

        it("uninstalls a module", function () {
            tajin.install({name: 'module1'});
            tajin.install({name: 'module2'});

            expect(tajin.modules()).toContain('module1');
            expect(tajin.modules()).toContain('module2');

            tajin.uninstall('module1');

            expect(tajin.modules()).toContain('module2');
        });

        it("check for required name parameter", function () {
            expect(function () {
                tajin.uninstall();
            }).toThrow(new Error("Module name is missing"));
        });

    });
});
