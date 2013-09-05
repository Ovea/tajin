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
import com.ovea.tajin.console.Executor
import com.ovea.tajin.console.FileProjectRepository
import com.ovea.tajin.console.ProjectRepository

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
@Parameters(separators = "=", commandDescription = "Create a project")
class CreateProjectCommand implements Executor {

    private ProjectRepository repository

    @Parameter(required = true, description = "name")
    List<String> names;

    @Parameter(names = "--path", description = "path")
    String path = '.'

    void execute() {
        repository().create(names[0]);
    }

    void projectRepository(ProjectRepository repository) {
        this.repository = repository
    }

    ProjectRepository repository() {
        if (repository != null)
            return repository
        return new FileProjectRepository(new File(path))
    }
}
