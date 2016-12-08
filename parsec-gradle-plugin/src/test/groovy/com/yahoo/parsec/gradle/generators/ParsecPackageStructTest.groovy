package com.yahoo.parsec.gradle.generators

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import java.nio.file.Paths

/**
 * @author waynewu
 */
class ParsecPackageStructTest extends Specification {

    def "create a ParsecPackageStruct fills with all arguments should get the same data from the getters"() throws Exception {
        given:
        def project = ProjectBuilder.builder().build()
        def javaSourceRoot = "mock source root"
        def generatedSourceRootPath = Paths.get("/tmp/foo");
        def generatedNamespace = "mock generated namespace"
        def packages = ["foo": "bar"]
        def handlers = ["foo1": ["bar1", "bar2", "bar3"], "foo2": ["bar2", "bar3", "bar4"]]
        def resources = ["damn1": ["bar1", "bar5"], "damn2": ["bar1", "bar5"]]
        def dataobjects = ["pkg1": [Paths.get("/tmp/foo1"), Paths.get("/tmp/foo2")]]
        def intersectPackageName = "mock intersect package name"

        when:
        def struct = new ParsecPackageStruct(
                project, javaSourceRoot, generatedSourceRootPath, generatedNamespace,
                packages, handlers, resources, dataobjects, intersectPackageName
        )

        then:
        struct.getProject() == project
        struct.getJavaSourceRoot() == javaSourceRoot
        struct.getGeneratedSourceRootPath() == generatedSourceRootPath
        struct.getGeneratedNamespace() == generatedNamespace
        struct.getIntersectPackageName() == intersectPackageName
        struct.getPackages() == packages
        struct.getHandlers() == handlers
        struct.getResources() == resources
        struct.getDataobjects() == dataobjects
    }


}
