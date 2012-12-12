describe("Config Module", function () {

    it("loads custom config", function () {
        expect(tajin.options.config.url).toContain('custom-config.json');
    });

    it("exposes module settings", function () {
        expect(tajin.options.module2.option2).toBe('value2');
    });

});
