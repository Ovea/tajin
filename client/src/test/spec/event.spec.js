describe("tajin.event", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
    });

    describe("at initialization", function () {

        it("register event tajin/ready", function () {
            var obj = {
                f: function () {
                }
            };
            spyOn(obj, 'f');
            tajin.event.get('tajin/ready').listen(obj.f);
            expect(obj.f).toHaveBeenCalled();
        });

    });

    describe("Event creation", function () {

        describe("tajin.event.add()", function () {

            it("adds event with no name", function () {
                var evt = tajin.event.add();
                expect(evt.id).toBeDefined();
                expect(tajin.event.has(evt.id)).toBe(true);
            });

            it("adds event with a name", function () {
                var evt = tajin.event.add('my/event');
                expect(evt.id).toBe('my/event');
                expect(tajin.event.has('my/event')).toBe(true);
            });

            it("adds event with options", function () {
                var evt = tajin.event.add({
                    id: 'toto',
                    state: true,
                    remote: true,
                    context: this
                });
                expect(evt.id).toBe('toto');
                expect(evt.remote).toBe(true);
                expect(evt.stateful).toBe(true);
                expect(evt.context).toBe(this);
                expect(tajin.event.has('toto')).toBe(true);
            });

            it("fails with a duplicate name", function () {
                expect(function () {
                    tajin.event.add('my/event');
                }).toThrow(new Error("Duplicate event: my/event"));
            });

        });

        describe("tajin.event.addAll()", function () {

            it("adds multiple events with optional options at once", function () {
                var all = tajin.event.addAll('my/evt1', {
                    id: 'my/evt2',
                    state: false
                }, 'my/evt3', {
                    state: true,
                    remote: true,
                    context: this
                });
                expect(tajin.event.get('my/evt1')).toBe(all[0]);
                expect(tajin.event.get('my/evt2')).toBe(all[1]);
                expect(tajin.event.get('my/evt3')).toBe(all[2]);
                expect(tajin.event.get('my/evt1').remote).toBe(true);
                expect(tajin.event.get('my/evt2').remote).toBe(true);
                expect(tajin.event.get('my/evt3').remote).toBe(true);
                expect(tajin.event.get('my/evt1').stateful).toBe(true);
                expect(tajin.event.get('my/evt2').stateful).toBe(false);
                expect(tajin.event.get('my/evt3').stateful).toBe(true);
            });

        });

    });

    describe("Event accessors", function () {

        describe("tajin.event.has()", function () {

            it("can test event existance", function () {
                expect(tajin.event.has('my/event')).toBe(true);
            });

        });

        describe("tajin.event.get()", function () {

            it("gets an event object", function () {
                var e = tajin.event.get('my/event');
                expect(e).toBeDefined();
                expect(e.id).toBe('my/event');
            });

            it("gets an inexisting event and create it at once", function () {
                tajin.event.get('inexisting', {
                    state: true
                });
                expect(tajin.event.has('inexisting')).toBe(true);
            });

        });

        describe("tajin.event.getAll()", function () {

            it("gets many event object at once", function () {
                var all = tajin.event.getAll('my/evt1', 'my/evt2', 'my/evt3');
                expect(all[0].stateful).toBe(true);
                expect(all[1].stateful).toBe(false);
                expect(all[2].stateful).toBe(true);
                expect(all[2].listen).toBeDefined();
                expect(all[2].fire).toBeDefined();
            });

        });

    });

    describe("Stateful event reset", function () {

        describe("tajin.event.reset()", function () {

            it("can reset a stateful event", function () {
                var e = tajin.event.add('my/stateful');
                spyOn(e, 'reset');
                tajin.event.reset('my/stateful');
                expect(e.reset).toHaveBeenCalled();
            });

        });

        describe("tajin.event.resetAll()", function () {

            it("can reset all event", function () {
                var e1 = tajin.event.add('my/e1'),
                    e2 = tajin.event.add('my/e2');
                spyOn(e1, 'reset');
                spyOn(e2, 'reset');
                tajin.event.resetAll();
                expect(e1.reset).toHaveBeenCalled();
                expect(e2.reset).toHaveBeenCalled();
            });

        });

    });

    describe("Event deletion", function () {

        describe("tajin.event.destroy()", function () {

            it("can destroy an event", function () {
                var e = tajin.event.add('my/destroyable');
                spyOn(e, 'destroy');
                tajin.event.destroy('my/destroyable');
                expect(e.destroy).toHaveBeenCalled();
            });

        });

    });

    describe("Event instance e", function () {

        it("exposes id, remote, stateful, context", function () {
            var evt = tajin.event.add({
                id: 'tata',
                state: true,
                remote: true,
                context: this
            });
            expect(evt.id).toBe('tata');
            expect(evt.remote).toBe(true);
            expect(evt.stateful).toBe(true);
            expect(evt.context).toBe(this);
            expect(tajin.event.has('tata')).toBe(true);
        });
        describe("e.time", function () {

            it("set when fired", function () {
                var evt = tajin.event.add();
                expect(evt.time).toBeUndefined();
                evt.fire();
                expect(evt.time).toBeDefined();
            });

        });

        describe("e.data", function () {

            it("unset when fired if stateless", function () {
                var evt = tajin.event.add();
                expect(evt.data).toBeUndefined();
                evt.fire();
                expect(evt.data).toBeUndefined();
            });

            it("set when fired if stateful", function () {
                var evt = tajin.event.add({
                    state: true
                });
                expect(evt.data).toBeUndefined();
                evt.fire();
                expect(evt.data).toBeNull();
                evt = tajin.event.add({
                    state: true
                });
                evt.fire('data');
                expect(evt.data).toBe('data');
            });

        });

        describe("e.fire()", function () {

            it("can be called multiple times if stateless", function () {
                var evt = tajin.event.add();
                evt.fire();
                evt.fire();
            });

            it("can be called only once if stateful", function () {
                var evt = tajin.event.add({
                    state: true
                });
                evt.fire();
                expect(function () {
                    evt.fire();
                }).toThrow(new Error("fire() cannot be called again on a stateful event. If needed, reset() must be called before or stateful flag must be removed."));

            });

            it("cannot be called with more than one parameters", function () {
                var evt = tajin.event.add();
                expect(function () {
                    evt.fire(1, 2);
                }).toThrow(new Error("fire() only accept at most one argument"));

            });

        });

        describe("e.reset()", function () {

            it("can be called to reset a stateful event and be able to call fire() again", function () {
                var evt = tajin.event.add({
                    state: true
                });
                evt.fire('data1');
                expect(evt.data).toBe('data1');
                evt.reset();
                evt.fire('data2');
                expect(evt.data).toBe('data2');
            });

        });

        describe("e.destroy()", function () {

            it("destroy the event", function () {
                var evt = tajin.event.add();
                expect(tajin.event.has(evt.id)).toBe(true);
                evt.destroy();
                expect(tajin.event.has(evt.id)).toBe(false);
            });

        });

        describe("e.listen()", function () {

            it("adds a listener", function () {
                var evt = tajin.event.add();
                var obj = {
                    l: function () {
                    }
                };
                spyOn(obj, 'l');
                evt.listen(obj.l);
                evt.fire();
                expect(obj.l).toHaveBeenCalled();
            });

            it("checks for multiple registration errors", function () {
                var evt = tajin.event.add(), c = 0;
                var obj = {
                    l: function () {
                        c++;
                    }
                };
                evt.listen(obj.l);
                evt.listen(obj.l);
                evt.listen(obj.l);
                evt.fire();
                expect(c).toBe(1);
            });

        });

        describe("e.once()", function () {

            it("register a listener to be triggered only once then removed", function () {
                var evt = tajin.event.add(), c = 0;
                var obj = {
                    f1: function () {
                        c++;
                    }
                };
                evt.once(obj.f1);
                evt.fire();
                expect(c).toBe(1);
                evt.fire();
                expect(c).toBe(1);
            });

        });

        describe("e.remove()", function () {

            it("removes a listener", function () {
                var evt = tajin.event.add(), c = 0;
                var obj = {
                    f1: function () {
                        c++;
                    }
                };
                evt.listen(obj.f1);
                evt.fire();
                expect(c).toBe(1);
                evt.remove(obj.f1);
                evt.fire();
                expect(c).toBe(1);
            });

        });

    });

});
