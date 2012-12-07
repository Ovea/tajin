describe("Default Logger", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain("log");
    });

    it("is at 'none' level by default", function () {
        expect(tajin.options.log.level).toBe("none");
    });

    it("and nothing can be logged", function () {
        spyOn(console, 'log');

        tajin.log.debug('a message', 'with param', 1);
        tajin.log.info('a message', 'with param', 1);
        tajin.log.warn('a message', 'with param', 1);
        tajin.log.error('a message', 'with param', 1);

        expect(console.log).not.toHaveBeenCalled();
    });

});

describe("DEBUG Logger", function () {
    var debug_logger = tajin.log.logger('Debug Logger', 'debug');

    it("can create debug level logger", function () {
        expect(debug_logger.level).toBe('debug');
    });

    it("all log messages are processed", function () {
        spyOn(console, 'debug');
        spyOn(console, 'info');
        spyOn(console, 'warn');
        spyOn(console, 'error');

        debug_logger.debug('a message', 'with param', 1);
        debug_logger.info('a message', 'with param', 1);
        debug_logger.warn('a message', 'with param', 1);
        debug_logger.error('a message', 'with param', 1);

        expect(console.debug).toHaveBeenCalledWith('DEBUG [Debug Logger] a message', 'with param', 1);
        expect(console.info).toHaveBeenCalledWith('INFO [Debug Logger] a message', 'with param', 1);
        expect(console.warn).toHaveBeenCalledWith('WARN [Debug Logger] a message', 'with param', 1);
        expect(console.error).toHaveBeenCalledWith('ERROR [Debug Logger] a message', 'with param', 1);
    });
});

describe("INFO Logger", function () {
    var info_logger = tajin.log.logger('Info Logger', 'info');

    it("can create info level logger", function () {
        expect(info_logger.level).toBe('info');
    });

    it("only 'info', 'warn' and 'error'  log messages are processed", function () {
        spyOn(console, 'debug');
        spyOn(console, 'info');
        spyOn(console, 'warn');
        spyOn(console, 'error');

        info_logger.debug('a message', 'with param', 1);
        info_logger.info('a message', 'with param', 1);
        info_logger.warn('a message', 'with param', 1);
        info_logger.error('a message', 'with param', 1);

        expect(console.debug).not.toHaveBeenCalled();
        expect(console.info).toHaveBeenCalledWith('INFO [Info Logger] a message', 'with param', 1);
        expect(console.warn).toHaveBeenCalledWith('WARN [Info Logger] a message', 'with param', 1);
        expect(console.error).toHaveBeenCalledWith('ERROR [Info Logger] a message', 'with param', 1);
    });
});

describe("WARN Logger", function () {
    var warn_logger = tajin.log.logger('Warn Logger', 'warn');

    it("can create warn level logger", function () {
        expect(warn_logger.level).toBe('warn');
    });

    it("only 'warn' and 'error'  log messages are processed", function () {
        spyOn(console, 'debug');
        spyOn(console, 'info');
        spyOn(console, 'warn');
        spyOn(console, 'error');

        warn_logger.debug('a message', 'with param', 1);
        warn_logger.info('a message', 'with param', 1);
        warn_logger.warn('a message', 'with param', 1);
        warn_logger.error('a message', 'with param', 1);

        expect(console.debug).not.toHaveBeenCalled();
        expect(console.info).not.toHaveBeenCalled();
        expect(console.warn).toHaveBeenCalledWith('WARN [Warn Logger] a message', 'with param', 1);
        expect(console.error).toHaveBeenCalledWith('ERROR [Warn Logger] a message', 'with param', 1);
    });
});

describe("ERROR Logger", function () {
    var error_logger = tajin.log.logger('Error Logger', 'error');

    it("can create error level logger", function () {
        expect(error_logger.level).toBe('error');
    });

    it("only 'error'  log messages are processed", function () {
        spyOn(console, 'debug');
        spyOn(console, 'info');
        spyOn(console, 'warn');
        spyOn(console, 'error');

        error_logger.debug('a message', 'with param', 1);
        error_logger.info('a message', 'with param', 1);
        error_logger.warn('a message', 'with param', 1);
        error_logger.error('a message', 'with param', 1);

        expect(console.debug).not.toHaveBeenCalled();
        expect(console.info).not.toHaveBeenCalled();
        expect(console.warn).not.toHaveBeenCalled();
        expect(console.error).toHaveBeenCalledWith('ERROR [Error Logger] a message', 'with param', 1);
    });
});



