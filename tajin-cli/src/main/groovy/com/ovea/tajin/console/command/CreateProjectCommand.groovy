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
