package com.yahoo.parsec.gradle.generators

import com.yahoo.parsec.gradle.utils.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths
/**
 * @author waynewu
 */
class ParsecApplicationGeneratorTest extends Specification {

    ParsecGeneratorUtil generatorUtil
    ParsecPackageStruct packageStruct
    FileUtils fileUtils

    def setup(){
        generatorUtil = Mock()
        fileUtils = Spy(FileUtils)

        generatorUtil.getIntersectPackageName(_) >> "java.com.example" //stubbing the return to "java.com.example"

        packageStruct = new ParsecPackageResolver(generatorUtil, fileUtils).resolve(
                ProjectBuilder.builder().build(),
                "src/main",
                Paths.get("./src/test/resources/generated-sources"),
                "parsec_generated"
        )
    }

    def getApplicationGenerator(){
        return new ParsecApplicationGenerator(packageStruct, generatorUtil)
    }

    @Unroll
    def "generateParsecApplication(#handleUncaughtException) should generate from template with expected arguments"(){
        when:
        getApplicationGenerator().generateParsecApplication(handleUncaughtException)

        then:
        1 * generatorUtil.generateFromTemplateTo(
                "ParsecApplication.java", "java.com.example",
                "./src/test/resources/generated-sources/java/com/example/parsec_generated",
                _ as Map<String, String>, true) >> { dummy1, dummy2, dummy3, materials, overwrite ->
            assert materials.get("{binding}").size() > 0
            assert materials.get("{imports}").size() > 0
            assert materials.get("{register}").size() > 0

            assert handleUncaughtException == materials.get("{imports}").contains("DefaultExceptionMapper")
            assert handleUncaughtException == materials.get("{register}").contains("DefaultExceptionMapper")
        }

        where:
            handleUncaughtException << [ false, true ]

    }

}
