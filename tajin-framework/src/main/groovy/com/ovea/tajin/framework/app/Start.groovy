package com.ovea.tajin.framework.app

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.ovea.tajin.framework.io.Resource
import com.ovea.tajin.framework.prop.PropertySettings
import com.ovea.tajin.framework.web.Container

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2013-04-26
 */
class Start {
    static void main(String... args) {
        Options options = new Options()
        JCommander commander = new JCommander(options, args)
        commander.programName = Start.name
        if (options.help) {
            commander.usage()
            System.exit(1)
        }

        PropertySettings settings = new PropertySettings(Resource.file(options.config))

        Container container = new Container()
        container.start()

        // org.eclipse.jetty.servlet.SessionIdPathParameterName => none
        // cookie config => name
    }

    static class Options {

        @Parameter(names = ['-c', '--config'], required = true, arity = 1, description = 'Configuration property file')
        File config

        @Parameter(names = ['-h', '--help'], required = false, arity = 0, hidden = true, description = 'Show this help', help = true)
        boolean help
    }

}
