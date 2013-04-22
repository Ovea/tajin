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
            tajin.event.on('dom/loaded').listen(obj.cb);
            expect(obj.cb).toHaveBeenCalled();
        });

    });

});
