describe("Config Module", function () {

    it("loaded inexisting config does not prevent Tajin initalization", function () {
        expect(tajin.options.config.url).toContain('inexisting.json');
    });

});
