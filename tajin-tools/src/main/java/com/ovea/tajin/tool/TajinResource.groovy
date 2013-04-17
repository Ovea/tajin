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
package com.ovea.tajin.tool

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.ovea.tajin.Tajin
import com.ovea.tajin.io.Resource

import java.util.concurrent.CountDownLatch

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 * @date 2012-11-26
 */
class TajinResource {

    static void main(String... args) {
        Options options = new Options()
        JCommander commander = new JCommander(options, args)
        commander.programName = TajinResource.name
        if (options.help) {
            commander.usage()
            System.exit(1)
        }
        Tajin tajin = Tajin.load(options.webapp, Resource.file(options.config))
        tajin.build()
        if (options.watch) {
            tajin.watch()
            Runtime.runtime.addShutdownHook(new Thread() {
                @Override
                void run() {
                    tajin.unwatch()
                }
            })
            try {
                new CountDownLatch(1).await()
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt()
            }
        }
    }

    static class Options {

        @Parameter(names = ['-d', '--directory'], required = false, arity = 1, description = 'Base webapp directory. Default to src/main/webapp')
        File webapp = new File('src/main/webapp')

        @Parameter(names = ['-c', '--config'], required = true, arity = 1, description = 'Tajin JSON configuration file. Default to src/main/webapp/WEB-INF/tajin.json')
        File config = new File('src/main/webapp', Tajin.DEFAULT_CONFIG_LOCATION)

        @Parameter(names = ['-w', '--watch'], required = false, arity = 0, description = 'Watch for file change to regenerate resource files')
        boolean watch = false

        @Parameter(names = ['-h', '--help'], required = false, arity = 0, hidden = true, description = 'Show this help', help = true)
        boolean help
    }

}
