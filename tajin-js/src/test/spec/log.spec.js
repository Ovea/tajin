describe("tajin.log", function () {

    it("is installed", function () {
        expect(tajin.modules()).toContain('log');
    });

    describe("at initialization", function () {

        it("is configured at 'all' at init", function () {
            expect(tajin.options.log.level).toBe('all');
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

        it("can create debug level logger", function () {
            var debug_logger = tajin.log.logger('Debug Logger', 'debug');
            expect(debug_logger.level).toBe('debug');
        });

        it("all log messages are processed", function () {
            var debug_logger = tajin.log.logger('Debug Logger', 'debug');

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

        it("can create info level logger", function () {
            var info_logger = tajin.log.logger('Info Logger', 'info');
            expect(info_logger.level).toBe('info');
        });

        it("only 'info', 'warn' and 'error'  log messages are processed", function () {
            var info_logger = tajin.log.logger('Info Logger', 'info');

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

        it("can create warn level logger", function () {
            var warn_logger = tajin.log.logger('Warn Logger', 'warn');
            expect(warn_logger.level).toBe('warn');
        });

        it("only 'warn' and 'error'  log messages are processed", function () {
            var warn_logger = tajin.log.logger('Warn Logger', 'warn');

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

        it("can create error level logger", function () {
            var error_logger = tajin.log.logger('Error Logger', 'error');
            expect(error_logger.level).toBe('error');
        });

        it("only 'error'  log messages are processed", function () {
            var error_logger = tajin.log.logger('Error Logger', 'error');

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

});
