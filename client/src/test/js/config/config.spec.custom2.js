describe("Config Module", function () {

    it("loads inexisting config and does not prevent Tajin initalization", function () {
        expect(tajin.options.config.url).toContain('inexisting.json');
    });

});
