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
package com.ovea.tajin.console.command

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import com.ovea.tajin.console.ContainerConfiguration
import com.ovea.tajin.console.Executor
import com.ovea.tajin.console.FileProjectRepository
import com.ovea.tajin.console.Project
import com.ovea.tajin.console.ProjectRepository
import org.eclipse.jetty.server.Server

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
@Parameters(separators = "=", commandDescription = "Start project")
class StartProjectCommand implements Executor {

    private ProjectRepository repository

    @Parameter(required = true, description = "name")
    List<String> names;

    @Parameter(names = "--root", description = "root")
    String root = '.'

    public StartProjectCommand() {
        this.repository = new FileProjectRepository(new File(root))
    }

    void execute() {
        Project project = repository().load(names[0]);

        // Start web server
        ContainerConfiguration.create()
                .webappRoot(project.root)
                .buildContainer()
                .start()


        // Watch project file modification

        // Parse config

        // Start jetty server



    }

    void projectRepository(ProjectRepository repository) {
        this.repository = repository
    }

    ProjectRepository repository() {
        if (repository != null)
            return repository
        return new FileProjectRepository(new File(root))
    }
}
