describe("tajin.dom", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('event');
        expect(tajin.modules()).toContain('dom');
    });


    describe("at initialization", function () {

        it("when dom is loaded, fires event dom/loaded", function () {
            var obj = {
                cb: function () {
                }
            };
            spyOn(obj, 'cb');
            tajin.event.get('dom/loaded').listen(obj.cb);
            expect(obj.cb).toHaveBeenCalled();
        });

        it("register event dom/loaded", function () {
            expect(tajin.event.has('dom/loaded')).toBe(true);
        });

    });

});
