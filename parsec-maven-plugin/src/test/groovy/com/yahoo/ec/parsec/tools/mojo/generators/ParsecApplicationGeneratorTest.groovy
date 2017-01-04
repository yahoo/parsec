package com.yahoo.ec.parsec.tools.mojo.generators

import com.yahoo.parsec.tools.mojo.generators.ParsecApplicationGenerator
import com.yahoo.parsec.tools.mojo.generators.ParsecGeneratorUtil
import com.yahoo.parsec.tools.mojo.generators.ParsecPackageResolver
import com.yahoo.parsec.tools.mojo.generators.ParsecPackageStruct
import com.yahoo.parsec.tools.mojo.utils.FileUtils
import org.apache.maven.project.MavenProject
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * ParsecApplicationGenerator Unit Test
 */
class ParsecApplicationGeneratorTest extends Specification {

    ParsecGeneratorUtil generatorUtil
    ParsecPackageStruct packageStruct
    FileUtils fileUtils

    def setup() {
        generatorUtil = Mock()
        fileUtils = Spy(FileUtils)

        // TODO: this's just a convenient way to build package struct object, should build it directly
        generatorUtil.getIntersectPackageName(_) >> "java.com.example"
        packageStruct = new ParsecPackageResolver(generatorUtil, fileUtils).resolve(
                new MavenProject(), "src/main",
                Paths.get("./src/test/resources/generated-sources"), "parsec_generated")
    }

    def getApplicationGenerator() {
        return new ParsecApplicationGenerator(packageStruct, generatorUtil)
    }

    @Unroll
    def "generateParsecApplication(#handleUncaughtException) should generate from template with expected arguments"() {
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

            // validate exception mapper registered or imported or not
            assert handleUncaughtException == materials.get("{imports}").contains("DefaultExceptionMapper")
            assert handleUncaughtException == materials.get("{register}").contains("DefaultExceptionMapper")
        }

        where:
        handleUncaughtException << [ false, true ]
    }
}
