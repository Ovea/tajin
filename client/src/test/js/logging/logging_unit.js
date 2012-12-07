describe("Logger", function () {

    it("should be installed", function () {
        expect(tajin.modules()).toContain("log");
    });

    it("it should be at 'none' level by default", function () {
        expect(tajin['log'].level()).toBe('none');
    });

    it("and nothing is logged", function() {

    });


//    existe default logger
//    at level none


    // Ca create my own logger
    // can set a level
    // can log



    // when using config its visible in  tajin.options.<module name>.level

});

