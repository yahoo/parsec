package com.yahoo.parsec.template

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * @author waynewu
 */

class ParsecProjectPluginTest {

    @Test
    void 'apply'(){
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.yahoo.parsec.template-plugin'

        assert project.tasks.createParsecProject
    }

}
