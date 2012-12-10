describe("Config Module", function () {

    it("loaded default config", function () {
        expect(tajin.options.config.url).toContain('inexisting.json');
    });

});

describe("Custom config", function () {

    it("loaded", function () {
        expect(tajin.options).toBeDefined();
    });

});
