// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.template.tasks

import org.gradle.api.DefaultTask
import templates.TemplatesPlugin

import java.nio.file.NotDirectoryException

/**
 * @author waynewu
 */

abstract class AbstractProjectTask extends DefaultTask {
    String name
    String description
    String group

    AbstractProjectTask(final String name, final String description){
        this.name = name
        this.description = description
        this.group = "Parsec"
    }

    /**
     * Get the group name either through command, project specification, or prompt
     * @return group name (groupId)
     */
    protected String groupName(){
        project.hasProperty('groupId') ? project.property('groupId').toString() : TemplatesPlugin.prompt("groupId: ")
    }

    /**
     * Get the project name either through command, project specification, or prompt
     * @return project name (artifactId)
     */
    protected String projectName(){
        project.hasProperty('artifactId') ? project.property('artifactId').toString() : TemplatesPlugin.prompt("artifactId: ")
    }

    /**
     * Get the project version through command, or project specification
     * @return project version
     */
    protected String projectVersion(){
         project.hasProperty('versionNum') ? project.property('versionNum').toString() : "1.0.0-SNAPSHOT"
    }

    /**
     * Generate the project path either with specified PARENT_DIR
     * or through the default directory
     * @param projectName
     * @return project path
     */
    protected String projectPath( final String projectName ) {
        String parentDir = project.hasProperty('parent_dir') ? project.property('parent_dir') : defaultDir()
        if (parentDir){
            return "$parentDir/$projectName"
        }else{
            throw new NotDirectoryException("Directory does not exist")
        }
    }

    /**
     * Get the default directory which is the current directory
     * @return directory
     */
    protected String defaultDir(){
        System.getProperty('init.dir', System.getProperty('user.dir'))
    }

    /**
     * Get the content (Non GString) of a specified file in the resource folder
     * @param path
     * @return content of the file as String
     */
    protected String getText(String path){
        try {
            this.getClass().getResourceAsStream(path).getText()
        } catch(NullPointerException e) {
            throw new FileNotFoundException("Template file not found: $path")
        }
    }
}
