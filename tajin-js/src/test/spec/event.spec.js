describe("tajin.event", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
    });

    describe("Event creation", function () {

        describe("tajin.event.on()", function () {

            it("adds event with a name", function () {
                var evt = tajin.event.on('my/event');
                expect(evt.id).toBe('my/event');
            });

            it("adds event with options", function () {
                var evt = tajin.event.on({
                    id: 'toto',
                    state: true,
                    remote: true,
                    context: this
                });
                expect(evt.id).toBe('toto');
                expect(evt.remote).toBe(true);
                expect(evt.stateful).toBe(true);
                expect(evt.context).toBe(this);
            });

            it("adds multiple events with optional options at once", function () {
                var all = tajin.event.on('my/evt1', {
                    id: 'my/evt2',
                    state: false
                }, 'my/evt3', {
                    state: true,
                    remote: true,
                    context: this
                });
                expect(tajin.event.on('my/evt1')).toBe(all[0]);
                expect(tajin.event.on('my/evt2')).toBe(all[1]);
                expect(tajin.event.on('my/evt3')).toBe(all[2]);
                expect(tajin.event.on('my/evt1').remote).toBe(true);
                expect(tajin.event.on('my/evt2').remote).toBe(true);
                expect(tajin.event.on('my/evt3').remote).toBe(true);
                expect(tajin.event.on('my/evt1').stateful).toBe(true);
                expect(tajin.event.on('my/evt2').stateful).toBe(false);
                expect(tajin.event.on('my/evt3').stateful).toBe(true);
            });

            it("gets an event object", function () {
                var e = tajin.event.on('my/event');
                expect(e).toBeDefined();
                expect(e.id).toBe('my/event');
            });

            it("gets an inexisting event and create it at once", function () {
                tajin.event.on('inexisting', {
                    state: true
                });
            });

            it("gets many event object at once", function () {
                var all = tajin.event.on('my/evt1', 'my/evt2', 'my/evt3');
                expect(all[0].stateful).toBe(true);
                expect(all[1].stateful).toBe(false);
                expect(all[2].stateful).toBe(true);
                expect(all[2].listen).toBeDefined();
                expect(all[2].fire).toBeDefined();
            });

            describe("EventList", function () {

                it("it has listen, once, fire, toString, remove, reset, sync methods with targets all events in the list", function () {
                    var all = tajin.event.on('my/sync/a1', 'my/sync/a2', 'my/sync/a3');
                    expect(all.listen).toBeDefined();
                    expect(all.once).toBeDefined();
                    expect(all.fire).toBeDefined();
                    expect(all.toString).toBeDefined();
                    expect(all.remove).toBeDefined();
                    expect(all.reset).toBeDefined();
                    expect(all.sync).toBeDefined();
                    expect(all.syncOnce).toBeDefined();
                });

                describe("eventList.sync(cb)", function () {

                    it("calls cb when all events in the list are triggered", function () {
                        var all = tajin.event.on('my/sync/a1', 'my/sync/a2'),
                            obj = {
                                f: function (e1, e2) {
                                }
                            };
                        spyOn(obj, 'f');
                        all.sync(obj.f);
                        all[0].fire();
                        all[1].fire();
                        expect(obj.f).toHaveBeenCalled();
                    });

                    it("can sync stateful topics", function () {
                        var t1 = tajin.event.on('my/sync/g1');
                        var t2 = tajin.event.on({
                            id: 'my/sync/g2',
                            state: true
                        });
                        var d1, d2;
                        t2.fire('data2');
                        tajin.event.on('my/sync/g1', 'my/sync/g2').sync(function (_d1, _d2) {
                            d1 = _d1;
                            d2 = _d2;
                        });
                        expect(d1).toBeUndefined();
                        expect(d2).toBeUndefined();
                        t1.fire('data1');
                        expect(d1).toBe('data1');
                        expect(d2).toBe('data2');
                    });

                    it("calls cb again if at lest one of all events in the list is triggered another time", function () {
                        var c = 0;
                        var all = tajin.event.on('my/sync/b1', 'my/sync/b2'),
                            obj = {
                                f: function (e1, e2) {
                                    c++;
                                }
                            };
                        all.sync(obj.f);
                        expect(c).toBe(0);
                        all[0].fire();
                        expect(c).toBe(0);
                        all[1].fire();
                        expect(c).toBe(1);
                        all[1].fire();
                        expect(c).toBe(2);
                    });

                    it("explode parameters to match each event parameter", function () {
                        var all = tajin.event.on('my/sync/c1', 'my/sync/c2', 'my/sync/c3'),
                            obj = {
                                f: function (e1, e2, e3) {
                                }
                            };
                        spyOn(obj, 'f');
                        all.sync(obj.f);
                        all[0].fire('data1');
                        all[1].fire('data2');
                        all[2].fire('data3');
                        expect(obj.f).toHaveBeenCalledWith('data1', 'data2', 'data3');
                        all[1].fire('data22');
                        expect(obj.f).toHaveBeenCalledWith('data1', 'data22', 'data3');
                    });

                });

                describe("eventList.syncOnce(cb)", function () {

                    it("calls cb when all events in the list are triggered", function () {
                        var all = tajin.event.on('my/sync/d1', 'my/sync/d2'),
                            obj = {
                                f: function (e1, e2) {
                                }
                            };
                        spyOn(obj, 'f');
                        all.syncOnce(obj.f);
                        all[0].fire();
                        all[1].fire();
                        expect(obj.f).toHaveBeenCalled()
                    });

                    it("cb is caled at most one time event if one of all events in the list is triggered another time", function () {
                        var c = 0;
                        var all = tajin.event.on('my/sync/e1', 'my/sync/e2'),
                            obj = {
                                f: function (e1, e2) {
                                    c++;
                                }
                            };
                        all.syncOnce(obj.f);
                        expect(c).toBe(0);
                        all[0].fire();
                        expect(c).toBe(0);
                        all[1].fire();
                        expect(c).toBe(1);
                        all[1].fire();
                        expect(c).toBe(1);
                    });

                    it("explode parameters to match each event parameter", function () {
                        var all = tajin.event.on('my/sync/f1', 'my/sync/f2', 'my/sync/f3'),
                            obj = {
                                f: function (e1, e2, e3) {
                                }
                            };
                        spyOn(obj, 'f');
                        all.syncOnce(obj.f);
                        all[0].fire('data1');
                        all[1].fire('data2');
                        all[2].fire('data3');
                        expect(obj.f).toHaveBeenCalledWith('data1', 'data2', 'data3');
                    });

                });

            });

        });

    });

    describe("Stateful event reset", function () {

        describe("group.reset()", function () {

            it("can reset several events at once", function () {
                var e1 = tajin.event.on('my/e1'),
                    e2 = tajin.event.on('my/e2');
                spyOn(e1, 'reset');
                spyOn(e2, 'reset');
                tajin.event.on('my/e1', 'my/e2').reset();
                expect(e1.reset).toHaveBeenCalled();
                expect(e2.reset).toHaveBeenCalled();
            });

        });

    });

    describe("Event instance e", function () {

        it("exposes id, remote, stateful, context", function () {
            var evt = tajin.event.on({
                id: 'tata',
                state: true,
                remote: true,
                context: this
            });
            expect(evt.id).toBe('tata');
            expect(evt.remote).toBe(true);
            expect(evt.stateful).toBe(true);
            expect(evt.context).toBe(this);
        });

        describe("e.time", function () {

            it("set when fired", function () {
                var evt = tajin.event.on('/evt/time');
                expect(evt.time).toBeUndefined();
                evt.fire();
                expect(evt.time).toBeDefined();
            });

        });

        describe("e.data", function () {

            it("unset when fired if stateless", function () {
                var evt = tajin.event.on('/evt/data');
                expect(evt.data).toBeUndefined();
                evt.fire();
                expect(evt.data).toBeUndefined();
            });

            it("set when fired if stateful", function () {
                var evt = tajin.event.on({
                    id: 'evt/data/1',
                    state: true
                });
                expect(evt.data).toBeUndefined();
                evt.fire();
                expect(evt.data).toBeNull();
                evt = tajin.event.on({
                    id: 'evt/data/2',
                    state: true
                });
                evt.fire('data');
                expect(evt.data).toBe('data');
            });

        });

        describe("e.fire()", function () {

            it("can be called multiple times if stateless", function () {
                var evt = tajin.event.on('evt/111');
                evt.fire();
                evt.fire();
            });

            it("can be called only once if stateful", function () {
                var evt = tajin.event.on({
                    id: 'evt/222',
                    state: true
                });
                evt.fire();
                expect(function () {
                    evt.fire();
                }).toThrow(new Error("fire() cannot be called again on a stateful topic. If needed, reset() must be called before or stateful flag must be removed."));
            });

            it("cannot be called with more than one parameters", function () {
                var evt = tajin.event.on('evet/333');
                expect(function () {
                    evt.fire(1, 2);
                }).toThrow(new Error("fire() only accept at most one argument"));

            });

        });

        describe("e.reset()", function () {

            it("can be called to reset a stateful event and be able to call fire() again", function () {
                var evt = tajin.event.on({
                    id: 'qq/1111',
                    state: true
                });
                evt.fire('data1');
                expect(evt.data).toBe('data1');
                evt.reset();
                evt.fire('data2');
                expect(evt.data).toBe('data2');
            });

        });

        describe("e.listen()", function () {

            it("adds a listener", function () {
                var evt = tajin.event.on('evt/444');
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
                var evt = tajin.event.on('evt/555'), c = 0;
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
                var evt = tajin.event.on('evt/666'), c = 0;
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
                var evt = tajin.event.on('evt/777'), c = 0;
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
