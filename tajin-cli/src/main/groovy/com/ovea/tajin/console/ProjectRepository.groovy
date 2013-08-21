package com.ovea.tajin.console

/**
 * @author David Avenante (d.avenante@gmail.com)
 */
public interface ProjectRepository {

    List<Project> list()

    Project load(String name)

    void create(String name)

}