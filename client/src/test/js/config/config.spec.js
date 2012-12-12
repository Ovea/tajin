describe("Config Module", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('config');
    });

    it("loads default config by default", function () {
        expect(tajin.options.config.url).toContain('tajin-client.json');
    });

    it("exposes all module options", function () {
        expect(tajin.options.module1.option1).toBe('value1');
    });

    it("overrides settings by those defined in-page", function () {
        expect(tajin.options.log.level).toBe('all');
    });

});
