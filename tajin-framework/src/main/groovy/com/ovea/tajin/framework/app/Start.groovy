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

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.ParameterException
import com.google.common.collect.Lists
import com.ovea.tajin.framework.io.Resource
import com.ovea.tajin.framework.prop.PropertySettings
import com.ovea.tajin.framework.support.logback.LogbackConfigurator
import com.ovea.tajin.framework.support.jetty.Container

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
        Resource loggingConfig = settings.getResource('logging.app.config', 'classpath:tajin-logback.xml')
        LogbackConfigurator.configure(loggingConfig)
        // load applications
        Collection<Application> apps = Lists.newLinkedList(ServiceLoader.load(Application))
        println "Starting applications: ${apps.collect {it.class.simpleName}.join(', ')}"
        // config and start container
        Container container = new Container(settings, apps, new InternalWebModule(settings, apps))
        container.start()
    }

    static class Options {

        @Parameter(names = ['-c', '--config'], required = false, arity = 1, description = 'Configuration property file')
        File config

        @Parameter(names = ['-h', '--help'], required = false, arity = 0, description = 'Show this help', help = true)
        Boolean help
    }

}
