package com.yahoo.parsec.gradle.generators

import com.yahoo.parsec.gradle.utils.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author waynewu
 */
class ParsecPackageResolverTest extends Specification {

    def generatedNameSpace, packagePath, packageName, javaSourceRoot
    Project project
    Path filePath, generatedSourceRootPath
    ParsecGeneratorUtil generatorUtil
    Map<String, String> packages
    Map<String, List<String>> handlers, resources
    Map<String, List<Path>> dataobjects
    FileUtils fileUtils

    def setup(){
        generatorUtil = Mock()
        fileUtils = Mock()
        packages = [:]
        handlers = [:]
        resources = [:]
        dataobjects = [:]
        packagePath = "com/example"
        packageName = packagePath.replace("/", ".")
        generatedSourceRootPath = Paths.get("./src/test/resources/generated-sources")
        generatedNameSpace = "parsec_generated"
        javaSourceRoot = "src/main"
        project = ProjectBuilder.builder().build()

    }

    def getPPR(){
        return Spy(ParsecPackageResolver, constructorArgs: [generatorUtil, fileUtils])
    }

    @Unroll
    def "resolveByFile() should set packages, handlers, resources or dataobjects if input fileName #fileName"(){
        given:
            def packageResolver = getPPR()
            filePath = getFilePath(generatedSourceRootPath, packagePath, generatedNameSpace, fileName)
            fileUtils.checkFileContains(filePath, _) >> dataobjectIsSet

        when:
            packageResolver.resolveByFile(
                    filePath, generatedNameSpace, generatedSourceRootPath, packages, handlers, resources, dataobjects)

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

    def "getFilePathsFromGeneratedSourceRoot() should return paths of given generated source root"(){
        expect:
            7 == getPPR().getFilePathsFromGeneratedSourceRoot(Paths.get("./src/test/resources/generated-sources")).size()

    }

    def "getFilePathsFromGeneratedSourceRoot() should throw exception if given path is invalid"(){
        when:
            getPPR().getFilePathsFromGeneratedSourceRoot(Paths.get("./inv@lid_path"))

        then:
            thrown(IOException)
    }

    def "resolve() with correct generated sources should return packageStruct"(){
        when:
            def packageStruct = getPPR().resolve(
                project, javaSourceRoot, generatedSourceRootPath, generatedNameSpace)

        then:
            fileUtils.checkFileContains(_ as Path, _) >> { Path filePath, dummy ->
                return filePath.endsWith("User.java")
            }
            def intersectPackageName = "java.com.example"
            generatorUtil.getIntersectPackageName(["java.com.example"]) >> "java.com.example"
            packageStruct.getGeneratedNamespace() == generatedNameSpace
            packageStruct.getJavaSourceRoot() == javaSourceRoot
            packageStruct.getGeneratedSourceRootPath() == generatedSourceRootPath
            packageStruct.getIntersectPackageName() == intersectPackageName
            packageStruct.getHandlers().containsKey(intersectPackageName)
            packageStruct.getResources().containsKey(intersectPackageName)
            packageStruct.getPackages().containsKey(intersectPackageName)
            packageStruct.getDataobjects().containsKey(intersectPackageName)
    }

    def "resolve() should throw excpetion if get file paths got exception"(){
        given:
            def packageResolver = getPPR()
            packageResolver.getFilePathsFromGeneratedSourceRoot(generatedSourceRootPath) >> {throw new IOException("mock exception")}

        when:
            packageResolver.resolve(
                        project, javaSourceRoot, generatedSourceRootPath, generatedNameSpace)

        then:
            thrown(IOException)
    }

    def getFilePath(Path generatedSourceRootPath, def packagePath, def generatedNameSpace, def fileName){
        return Paths.get(generatedSourceRootPath.toString() + "/" + packagePath + "/" + generatedNameSpace + "/" + fileName)
    }



}
