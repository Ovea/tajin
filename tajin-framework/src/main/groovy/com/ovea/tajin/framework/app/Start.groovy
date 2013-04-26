/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.tajin.framework.app

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.ovea.tajin.framework.io.Resource
import com.ovea.tajin.framework.prop.PropertySettings
import com.ovea.tajin.framework.web.Container
import org.slf4j.LoggerFactory

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-26
 */
class Start {
    static void main(String... args) {
        // parse options
        Options options = new Options()
        JCommander commander = new JCommander()
        commander.programName = Start.name
        commander.addObject(options)
        try {
            commander.parse(args)
        } catch (ParameterException ignored) {
            commander.usage()
            System.exit(1)
        }
        if (options.help) {
            commander.usage()
            System.exit(1)
        }
        // load config
        PropertySettings settings = options.config ? new PropertySettings(Resource.file(options.config)) : new PropertySettings()
        // setup logging
        settings.getPath('logging.app.config', null)?.with { File config ->
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            try {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                // Call context.reset() to clear any previous configuration, e.g. default
                // configuration. For multi-step configuration, omit calling context.reset().
                context.reset();
                configurator.doConfigure(config);
            } catch (JoranException ignored) {
                // StatusPrinter will handle this
            }
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }
        // config and start container
        Container container = new Container(settings)
        container.start()
    }

    static class Options {

        @Parameter(names = ['-c', '--config'], required = false, arity = 1, description = 'Configuration property file')
        File config

        @Parameter(names = ['-h', '--help'], required = false, arity = 0, description = 'Show this help', help = true)
        Boolean help
    }

}
