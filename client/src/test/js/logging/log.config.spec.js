describe("Default Logger", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain("log");
        expect(tajin.modules()).toContain("config");
    });

    it("is configured to ALL on initialization", function () {
        expect(tajin.options.log.level).toBe("all");
    });

    it("only 'warn' and 'error'  log messages are processed", function () {
        spyOn(console, 'debug');
        spyOn(console, 'info');
        spyOn(console, 'warn');
        spyOn(console, 'error');

        tajin.log.debug('a message', 'with param', 1);
        tajin.log.info('a message', 'with param', 1);
        tajin.log.warn('a message', 'with param', 1);
        tajin.log.error('a message', 'with param', 1);

        expect(console.debug).not.toHaveBeenCalled();
        expect(console.info).not.toHaveBeenCalled();
        expect(console.warn).toHaveBeenCalledWith('WARN [tajin.log] a message', 'with param', 1);
        expect(console.error).toHaveBeenCalledWith('ERROR [tajin.log] a message', 'with param', 1);
    });

});
