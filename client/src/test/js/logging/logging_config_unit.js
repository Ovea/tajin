describe("Default Logger configured to WARN on initialization ", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain("log");
    });

    it("is at 'warn' level by default", function () {
        expect(tajin.options.log.level).toBe("warn");
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