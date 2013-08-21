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
