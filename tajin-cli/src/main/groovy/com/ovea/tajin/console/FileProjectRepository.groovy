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
