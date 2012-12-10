describe("Config Module", function () {

    it("loaded default config", function () {
        expect(tajin.options.config.url).toContain('custom-config.json');
    });

});

describe("Custom config", function () {

    it("loaded", function () {
        expect(tajin.options.module2.option2).toBe('value2');
    });

});
