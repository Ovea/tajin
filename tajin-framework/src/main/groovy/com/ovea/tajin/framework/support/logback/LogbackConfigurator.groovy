package com.ovea.tajin.framework.support.logback

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.ovea.tajin.framework.io.Resource
import org.slf4j.LoggerFactory

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-28
 */
class LogbackConfigurator {
    static void configure(Resource resource) {
        println "Configuring logging with ${resource}"
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            if (resource.file) {
                configurator.doConfigure(resource.asFile);
            } else {
                configurator.doConfigure(resource.asUrl);
            }
        } catch (JoranException ignored) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }
}
