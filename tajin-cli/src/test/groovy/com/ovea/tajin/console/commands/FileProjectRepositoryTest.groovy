package com.ovea.tajin.console.commands

import com.ovea.tajin.console.FileProjectRepository
import com.ovea.tajin.console.Project
import com.ovea.tajin.console.ProjectRepository
import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.CoreMatchers.*


/**
 * @author David Avenante (d.avenante@gmail.com)
 */
@RunWith(JUnit4.class)
class FileProjectRepositoryTest {

    @Before
    void set_up() {
        FileUtils.deleteDirectory(new File('target/test/data'))
        FileUtils.copyDirectory(new File('src/test/data'), new File('target/test/data'))
    }

    @After
    void tear_down() {
        FileUtils.deleteDirectory(new File('target/test/data'))
    }

    @Test
    void can_list_project() {
        ProjectRepository repository = new FileProjectRepository(new File('target/test/data'));
        List<Project> projects = repository.list();

        assertThat(projects.size(), is(5))

        assertThat(projects.collect { it.name }.containsAll([
                'project-1',
                'project-2',
                'project-3',
                'project-4',
                'project-5'
        ]), is(true))

        new File('target/test/data/tajin.json').withWriter{ it << '{"name": "project-6", "version": "6.0.0-SNAPSHOT"}'}

        projects = repository.list();

        assertThat(projects.size(), is(6))

        assertThat(projects.collect { it.name }.containsAll([
                'project-1',
                'project-2',
                'project-3',
                'project-4',
                'project-5',
                'project-6'
        ]), is(true))
    }

    @Test
    void can_load_project() {
        ProjectRepository repository = new FileProjectRepository(new File('target/test/data'));

        Project project = repository.load('project-1');

        assert project.name == 'project-1'
        assert project.version == '1.0.0-SNAPSHOT'
    }

    @Test
    void can_create_project() {
        ProjectRepository repository = new FileProjectRepository(new File('target/test/data'));

        assert !new File('target/test/data/project-X').exists()
        assert repository.load('project-X') == null;

        repository.create('project-X')

        assert new File('target/test/data/project-X').exists()
        assert repository.load('project-X');
    }

}
