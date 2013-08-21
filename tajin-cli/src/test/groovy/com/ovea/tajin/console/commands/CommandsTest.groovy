package com.ovea.tajin.console.commands

import com.beust.jcommander.JCommander
import com.ovea.tajin.console.ProjectRepository
import com.ovea.tajin.console.command.CreateProjectCommand
import com.ovea.tajin.console.command.HelpCommand
import com.ovea.tajin.console.command.ListProjectCommand
import com.ovea.tajin.console.command.StartProjectCommand
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import static org.mockito.Mockito.*

/**
 * @author David Avenante (d.avenante@gmail.com)
 */

@RunWith(JUnit4.class)
class CommandsTest {

    @Test
    void test_list_project_command() {
        ListProjectCommand command = new ListProjectCommand();

        command.root = 'target/test/data'

        // Set mock
        ProjectRepository repository = mock(ProjectRepository.class)
        command.projectRepository(repository);

        command.execute();

        verify(repository, times(1)).list()
    }

    @Test
    void test_start_project_command() {
        StartProjectCommand command = new StartProjectCommand()

        command.root = 'target/test/data'
        command.names = new ArrayList<>()
        command.names.add('project-1')

        // Set mock
        ProjectRepository repository = mock(ProjectRepository.class)
        command.projectRepository(repository);

        verify(repository, times(0)).load('project-1');
        command.execute();
        verify(repository, times(1)).load('project-1');
    }

    @Test
    void tes_can_create_a_new_project() {
        CreateProjectCommand command = new CreateProjectCommand();

        command.names = new ArrayList<>()
        command.names.add('project-X')
        command.path = 'target/test/data'

        // Set mock
        ProjectRepository repository = mock(ProjectRepository.class)
        command.projectRepository(repository);

        verify(repository, times(0)).create('project-X');
        command.execute();
        verify(repository, times(1)).create('project-X');
    }

    @Test
    void test_help_command() {
        // Set mock
        JCommander commander = mock(JCommander.class)
        HelpCommand command = new HelpCommand(commander)

        verify(commander, times(0)).usage()
        command.execute()
        verify(commander, times(1)).usage()
    }

}
