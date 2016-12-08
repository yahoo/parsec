package com.yahoo.parsec.template.tasks

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import java.nio.file.NotDirectoryException

/**
 * @author waynewu
 */

class AbstractProjectTaskTest extends Specification {

    Project projectMock
    Task task

    def setup(){
        projectMock = ProjectBuilder.builder().build()
        task = projectMock.task('newTask', type: CreateSampleProjectTask)

    }

    def "get group name through properties"(){
        when:
            projectMock.ext['groupId'] = "test.group.name"
        then:
            task.groupName() == "test.group.name"
            notThrown(NullPointerException)
    }

    def "get project name through properties"(){
        when:
            projectMock.ext['artifactId'] = "test_project_name"
        then:
            task.projectName() == "test_project_name"
            notThrown(NullPointerException)
    }

    def "get version number through properties"(){
        when:
            projectMock.ext['versionNum'] = "1.0"
        then:
            projectMock.hasProperty('version')
            task.projectVersion() == "1.0"
            notThrown(NullPointerException)
    }

    def "get version number by default"(){
        expect:
            task.projectVersion() == "1.0.0-SNAPSHOT"
    }

    def "get project directory through properties"(){
        when:
            projectMock.ext['parent_dir'] = "/user/tmp"
        then:
            task.projectPath('test') == "/user/tmp/test"
            notThrown(NotDirectoryException)
    }

    def "get text from template in resources"(){
        when:
            String content = task.getText("/templates/test.txt")
        then:
            content == "this is for testing"
            notThrown(FileNotFoundException)
    }

    def "get text with incorrect path"(){
        when:
            task.getText("r@nd0m/test.txt")
        then:
            thrown(FileNotFoundException)
    }


}
