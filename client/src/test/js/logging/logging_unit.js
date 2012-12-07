describe("Default Logger", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain("log");
    });

    it("is at 'none' level by default", function () {
        expect(tajin.log.level).toBe("none");
    });

    it("and nothing can be logged", function () {
        spyOn(console, 'log');

        tajin.log.debug('a message', 'with param', 1);
        tajin.log.info('a message', 'with param', 1);
        tajin.log.warn('a message', 'with param', 1);
        tajin.log.error('a message', 'with param', 1);

        expect(console.log).not.toHaveBeenCalled();
    });

    // when using config its visible in  tajin.options.<module name>.level

});

describe("Personal logger", function () {
    var debug_logger = tajin.log.logger('Debug Logger', 'debug');

    it("can create debug level logger", function () {
        expect(debug_logger.level).toBe('debug');
    });

    it("all log messages are processed", function () {
        spyOn(console, 'log');

        debug_logger.debug('a message', 'with param', 1);

        expect(console.log).toHaveBeenCalled();
//        expect(console.log).toHaveBeenCalledWith('INFO [Debug Logger] a message with param 1');


//        debug_logger.info('a message', 'with param', 1);
//        debug_logger.warn('a message', 'with param', 1);
//        debug_logger.error('a message', 'with param', 1);

    });


});



