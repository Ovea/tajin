package com.ovea.tajin.console

import groovy.json.JsonBuilder
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
class FileProjectRepository implements ProjectRepository {

    private File root;
    final String filename = 'tajin.json';

    FileProjectRepository(File root) {
        this.root = root;
    }

    List<Project> list() {
        List<Project> projects = new ArrayList<>();

        NameFileFilter fileFilter = new NameFileFilter(filename);
        for (File project : FileUtils.listFiles(root, fileFilter, TrueFileFilter.INSTANCE)) {
            projects.add(new Project(project))
        }
        return projects;
    }

    Project load(String name) {
        return list().find { it.name == name }
    }

    void create(String name) {
        new File(root.absolutePath + File.separator + name).mkdir();
        File file = new File(root.absolutePath + File.separator + name + File.separator + filename);

        def project = [
                name: name,
                version: '1.0.0-SNAPSHOT'
        ]

        def builder = new JsonBuilder(project)

        file.withWriter('UTF-8') { it << builder.toPrettyString() }
    }
}
