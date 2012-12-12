describe("DOM Module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
        expect(tajin.modules()).toContain('dom');
    });

});

describe("Event dom/loaded", function () {

    it("is fired", function () {
        var obj = {
            cb: function () {
            }
        };
        spyOn(obj, 'cb');
        tajin.event.get('dom/loaded').listen(obj.cb);
        expect(obj.cb).toHaveBeenCalled();
    });

    it("is exported", function () {
        expect(tajin.event.has('dom/loaded')).toBe(true);
    });

});
