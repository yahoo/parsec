package com.yahoo.ec.parsec.tools.mojo.generators

import com.yahoo.parsec.tools.mojo.generators.ParsecGeneratorUtil
import com.yahoo.parsec.tools.mojo.generators.ParsecPackageResolver
import com.yahoo.parsec.tools.mojo.utils.FileUtils
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.project.MavenProject
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

/**
 * ParsecPackageResolver Unit Test
 */
class ParsecPackageResolverTest extends Specification {

    def generatedNamespace, packagePath, packageName, javaSourceRoot
    MavenProject project
    Path filePath, generatedSourceRootPath
    ParsecGeneratorUtil generatorUtil
    Map<String, String> packages
    Map<String, List<String>> handlers, resources
    Map<String, List<Path>> dataobjects
    FileUtils fileUtils

    def setup() {
        generatorUtil = Mock()
        fileUtils = Mock()
        packages = [:]
        handlers = [:]
        resources = [:]
        dataobjects = [:]
        packagePath = "com/example"
        packageName = packagePath.replace("/", ".")
        generatedSourceRootPath = Paths.get("./src/test/resources/generated-sources")
        generatedNamespace = "parsec_generated"
        javaSourceRoot = "src/main"
        project = new MavenProject()

    }

    def getPackageResolver() {
        return Spy(ParsecPackageResolver, constructorArgs: [generatorUtil, fileUtils])
    }

    @Unroll
    def "resolveByFile() should set packages, handlers, resources or dataobjects if input fileName #fileName"() {
        given:
        def packageResolver = getPackageResolver()
        filePath = getFilePath(generatedSourceRootPath, packagePath, generatedNamespace, fileName)
        fileUtils.checkFileContains(filePath, _) >> dataobjectIsSet

        when:
        packageResolver.resolveByFile(
                filePath, generatedNamespace, generatedSourceRootPath, packages, handlers, resources, dataobjects)

        then:
        PackageIsSet == packages.containsKey(packageName)
        handlerIsSet == handlers.containsKey(packageName)
        resourceIsSet == resources.containsKey(packageName)
        dataobjectIsSet == dataobjects.containsKey(packageName)

        where:
        fileName                 | PackageIsSet | handlerIsSet | resourceIsSet | dataobjectIsSet
        "SampleHandler.java"     | false        | true         | false         | false
        "SampleResources.java"   | true         | false        | true          | false
        "User.java"              | false        | false        | false         | true
        "SampleApplication.java" | false        | false        | false         | false
    }

    def "getFilePathsFromGeneratedSourceRoot() should return paths of given generated source root"() {
        expect:
        7 == packageResolver.getFilePathsFromGeneratedSourceRoot(Paths.get("./src/test/resources/generated-sources")).size()
    }

    def "getFilePathsFromGeneratedSourceRoot() should throw exception if given path is invalid"() {
        when:
        packageResolver.getFilePathsFromGeneratedSourceRoot(Paths.get("./invalid_path"))

        then:
        thrown(IOException)
    }

    def "resolve() with correct generated sources should return package struct"() {
        when:
        def packageStruct = getPackageResolver().resolve(
                project, javaSourceRoot, generatedSourceRootPath, generatedNamespace)

        then:
        fileUtils.checkFileContains(_ as Path, _) >> { Path filePath, dummy ->
            return filePath.endsWith("User.java")
        }
        def intersectPackageName = "java.com.example"
        generatorUtil.getIntersectPackageName(["java.com.example"]) >> "java.com.example"
        packageStruct.getGeneratedNamespace() == generatedNamespace
        packageStruct.getJavaSourceRoot() == javaSourceRoot
        packageStruct.getGeneratedSourceRootPath() == generatedSourceRootPath
        packageStruct.getIntersectPackageName() == intersectPackageName
        packageStruct.getHandlers().containsKey(intersectPackageName) == true
        packageStruct.getPackages().containsKey(intersectPackageName) == true
        packageStruct.getDataobjects().containsKey(intersectPackageName) == true
        packageStruct.getResources().containsKey(intersectPackageName) == true
    }

    def "resolve() should throw mojo exception if get file paths got exception"() {
        given:
        def packageResolver = getPackageResolver()
        packageResolver.getFilePathsFromGeneratedSourceRoot(generatedSourceRootPath) >> { throw new IOException("mock expection")}

        when:
        packageResolver.resolve(
                project, javaSourceRoot, generatedSourceRootPath, generatedNamespace)

        then:
        thrown(MojoExecutionException)
    }

    def getFilePath(Path generatedSourceRootPath, def packagePath, def generatedNamespace, def fileName) {
        return Paths.get(generatedSourceRootPath.toString() + "/" + packagePath + "/" + generatedNamespace + "/" + fileName)
    }
}
