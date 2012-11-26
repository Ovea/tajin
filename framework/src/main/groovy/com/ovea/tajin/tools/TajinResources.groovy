package com.ovea.tajin.tools

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.ovea.tajin.TajinConfig
import com.ovea.tajin.resources.TajinResourceManager

import java.util.concurrent.CountDownLatch

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 */
class TajinResources {

    static void main(String... args) {
        Options options = new Options()
        JCommander commander = new JCommander(options, args)
        commander.programName = TajinResources.name
        if (options.help) {
            commander.usage()
            System.exit(1)
        }
        TajinConfig config = TajinConfig.read(options.config)
        TajinResourceManager manager = new TajinResourceManager(config, options.basedir)
        if (options.watch) {
            manager.watch()
            Runtime.runtime.addShutdownHook(new Thread() {
                @Override
                void run() {
                    manager.unwatch()
                }
            })
            new CountDownLatch(1).await()
        } else {
            manager.buid()
        }
    }

    static class Options {

        @Parameter(names = ['-d', '--directory'], required = false, arity = 1, description = 'Base directory')
        File basedir = new File('.').canonicalFile

        @Parameter(names = ['-c', '--config'], required = true, arity = 1, description = 'Tajin JSON configuration file')
        File config = new File(basedir, 'src/main/resources/META-INF/tajin.json').canonicalFile

        @Parameter(names = ['-w', '--watch'], required = false, arity = 0, description = 'Watch for file change to regenerate resource files')
        boolean watch = false

        @Parameter(names = ['-h', '--help'], required = false, arity = 0, hidden = true, description = 'Show this help', help = true)
        boolean help
    }

}
