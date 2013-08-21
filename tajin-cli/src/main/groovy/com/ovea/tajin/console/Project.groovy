package com.ovea.tajin.console

import groovy.json.JsonSlurper

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
class Project {

    private File config;
    def properties;


    public Project(File config) {
        this.config = config
        load()
    }

    public String getName() {
        return properties.name
    }

    public String getVersion() {
        return properties.version
    }

    public String getRoot() {
        return config.parentFile.absolutePath + '/webapp'
    }

    private load() {
        properties = new JsonSlurper().parseText(config.text)
        if (getName() == null || getVersion() == null) {
            throw new IllegalStateException('Invalid tajin project file format (name and version are mandatory): ' + config.absolutePath)
        }
    }

//    @Override
//    String toString() {
//        def data = [
//                name: properties.name,
//                version: properties.version
//        ]
//
//        return new JsonBuilder(data).toPrettyString()
//    }

}
