package com.yahoo.parsec.gradle.utils

import org.gradle.api.logging.Logger
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.NoSuchFileException
import java.nio.file.Paths

/**
 *@author waynewu
 */
class FileUtilsTest extends Specification{

    Logger mockLog
    def file, fileWithValidationGroup, fileWithoutValidationGroup, invalidFile

    def setup(){
        mockLog = Mock()
        def generatedTestResource = "./src/test/resources/generated-sources/java/com/example/parsec_generated"
        file = generatedTestResource + "/User.java"
        fileWithValidationGroup = generatedTestResource + "/User.java"
        fileWithoutValidationGroup = generatedTestResource + "/SampleResources.java"
        invalidFile = generatedTestResource + "/invalidFile.java"
    }

    def getUtil(){
        return new FileUtils(mockLog)
    }

    def "getFileContent() should return file string content"(){
        expect:
            2400 == getUtil().getFileContent(getFilePathFromFilename(file)).size()
    }

    def "getFileContent() with not found file should throw exception"(){
        when:
            getUtil().getFileContent(getFilePathFromFilename(invalidFile))

        then:
            thrown(NoSuchFileException)
    }

    @Unroll
    def "checkFileContains() should return #expectValue if given a string #givenString"(){
        expect:
            expectValue == getUtil().checkFileContains(getFilePathFromFilename(file), givenString)

        where:
            expectValue | givenString
            true        | "implements java.io.Serializable"
            true        | "ParsecValidationGroups.Insert.class"
            false       | "not found pattern"
    }


    def getFilePathFromFilename(def filename){
        Paths.get(filename)
    }

}
