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
