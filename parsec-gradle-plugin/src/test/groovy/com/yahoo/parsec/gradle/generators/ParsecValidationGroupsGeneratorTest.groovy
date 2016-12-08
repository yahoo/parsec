package com.yahoo.parsec.gradle.generators

import com.yahoo.parsec.gradle.utils.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author waynewu
 */
class ParsecValidationGroupsGeneratorTest extends Specification{

    ParsecPackageStruct packageStruct
    ParsecGeneratorUtil generatorUtil
    FileUtils fileUtils

    def setup(){
        generatorUtil = Mock()
        fileUtils = Spy(FileUtils)

        generatorUtil.getIntersectPackageName(_) >> "java.com.example"
        packageStruct = new ParsecPackageResolver(generatorUtil, fileUtils).resolve(
                ProjectBuilder.builder().build(),
                "src/main",
                Paths.get("./src/test/resources/generated-sources"),
                "parsec_generated")
    }

    def getGenerator(){
        return new ParsecValidationGroupGenerator(packageStruct, generatorUtil, fileUtils)
    }

    def "generateParsecValidationGroups() should generate from template with expected arguments"() {
        given:
            fileUtils.findPatternsInFile(_ as Path, "ParsecValidationGroups\\.(.+?)\\.class") >> { Path path, def dummy ->
                path.getFileName().endsWith("User.java") ? ["Create", "Update"] as Set : [:] as Set }

        when:
            getGenerator().generateParsecValidationGroups()

        then:
        1 * generatorUtil.generateFromTemplateTo("ParsecValidationGroups.java", "java.com.example",
                "./src/test/resources/generated-sources/java/com/example/parsec_generated",
                _ as Map<String, String>, true) >> { dummy1, dummy2, dummy3, materials, overwrite ->
            assert materials.get("{validationGroups}").indexOf("public interface Create { }") > 0
            assert materials.get("{validationGroups}").indexOf("public interface Update { }") > 0
        }

    }


}
