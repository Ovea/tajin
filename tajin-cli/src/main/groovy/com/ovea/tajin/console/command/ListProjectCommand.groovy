package com.ovea.tajin.console.command

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import com.ovea.tajin.console.Executor
import com.ovea.tajin.console.FileProjectRepository
import com.ovea.tajin.console.Json
import com.ovea.tajin.console.ProjectRepository
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
@Parameters(separators = "=", commandDescription = "List all projects")
class ListProjectCommand implements Executor {

    private ProjectRepository repository

    @Parameter(names = "--root", description = "root")
    String root = '.'

    void execute() {
        repository().list().each { it ->
            println '-------------------------------'
            println it.name
            println it.version
        }
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
