describe("Event module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
    });

    it("can add event with no name", function () {
        var evt = tajin.event.add();
        expect(evt.id).toBeDefined();
        expect(tajin.event.has(evt.id)).toBe(true);
    });

    it("can add event with a name", function () {
        var evt = tajin.event.add('my/event');
        expect(evt.id).toBe('my/event');
        expect(tajin.event.has('my/event')).toBe(true);
    });

    it("can add event with options", function () {
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

    it("cannot add event with a duplicate name", function () {
        expect(function () {
            tajin.event.add('my/event');
        }).toThrow(new Error("Duplicate event: my/event"));
    });

    it("can test event existance", function () {
        expect(tajin.event.has('my/event')).toBe(true);
    });

    it("can get an event object", function () {
        var e = tajin.event.get('my/event');
        expect(e).toBeDefined();
        expect(e.id).toBe('my/event');
    });

    it("can get an inexisting event and create it at once", function () {
        tajin.event.get('inexisting', {
            state: true
        });
        expect(tajin.event.has('inexisting')).toBe(true);
    });

    it("can reset a stateful event", function () {
        var e = tajin.event.add('my/stateful');
        spyOn(e, 'reset');
        tajin.event.reset('my/stateful');
        expect(e.reset).toHaveBeenCalled();
    });

    it("can destroy an event", function () {
        var e = tajin.event.add('my/destroyable');
        spyOn(e, 'destroy');
        tajin.event.destroy('my/destroyable');
        expect(e.destroy).toHaveBeenCalled();
    });

    it("can reset all event", function () {
        var e1 = tajin.event.add('my/e1'),
            e2 = tajin.event.add('my/e2');
        spyOn(e1, 'reset');
        spyOn(e2, 'reset');
        tajin.event.resetAll();
        expect(e1.reset).toHaveBeenCalled();
        expect(e2.reset).toHaveBeenCalled();
    });

    it("can destroy all event", function () {
        var d1 = tajin.event.add('my/d1'),
            d2 = tajin.event.add('my/d2');
        spyOn(d1, 'destroy');
        spyOn(d2, 'destroy');
        tajin.event.destroyAll();
        expect(d1.destroy).toHaveBeenCalled();
        expect(d2.destroy).toHaveBeenCalled();
    });

    it("provides Tajin ready event", function () {
        var obj = {
            f: function () {
            }
        };
        spyOn(obj, 'f');
        tajin.event.get('tajin/ready').listen(obj.f);
        expect(obj.f).toHaveBeenCalled();
    });

});

describe("Event objet", function () {

    it("exposes properties", function () {
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

    it("has time when fired", function () {
        var evt = tajin.event.add();
        expect(evt.time).toBeUndefined();
        evt.fire();
        expect(evt.time).toBeDefined();
    });

    it("has no data when fired if stateless", function () {
        var evt = tajin.event.add();
        expect(evt.data).toBeUndefined();
        evt.fire();
        expect(evt.data).toBeUndefined();
    });

    it("has data set when fired if stateful", function () {
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

    it("can be fired multiple times if stateless", function () {
        var evt = tajin.event.add();
        evt.fire();
        evt.fire();
    });

    it("can be fired only once if stateful", function () {
        var evt = tajin.event.add({
            state: true
        });
        evt.fire();
        expect(function () {
            evt.fire();
        }).toThrow(new Error("fire() cannot be called again on a stateful event. If needed, reset() must be called before or stateful flag must be removed."));

    });

    it("can be resetted to be fired again if stateful", function () {
        var evt = tajin.event.add({
            state: true
        });
        evt.fire('data1');
        expect(evt.data).toBe('data1');
        evt.reset();
        evt.fire('data2');
        expect(evt.data).toBe('data2');
    });

    it("cannot be fired with more than one parameters", function () {
        var evt = tajin.event.add();
        expect(function () {
            evt.fire(1, 2);
        }).toThrow(new Error("fire() only accept at most one argument"));

    });

    it("can be destroyed", function () {
        var evt = tajin.event.add();
        expect(tajin.event.has(evt.id)).toBe(true);
        evt.destroy();
        expect(tajin.event.has(evt.id)).toBe(false);
    });

    it("can be listened", function () {
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

    it("cannot be listened with same listener twice or more", function () {
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

    it("can remove listener", function () {
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

    it("can be listened once", function () {
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
