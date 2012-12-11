describe("DOM Module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
        expect(tajin.modules()).toContain('dom');
    });

    it("exports DOM Loaded event", function () {
        expect(tajin.event.has('dom/loaded')).toBe(true);
    });

});

describe("DOM Loaded Event", function () {

    it("is fired", function () {
        var obj = {
            cb: function () {
            }
        };
        spyOn(obj, 'cb');
        tajin.event.get('dom/loaded').listen(obj.cb);
        expect(obj.cb).toHaveBeenCalled();
    });

});
