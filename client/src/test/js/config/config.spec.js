describe("Config Module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('config');
    });

    it("loaded default config", function () {
        expect(tajin.options.config.url).toContain('tajin-client.json');
    });

});

describe("Config", function () {

    it("loaded", function () {
        expect(tajin.options.module1.option1).toBe('value1');
    });

    it("overriden by settings defined in-page", function () {
        expect(tajin.options.log.level).toBe('all');
    });

});
