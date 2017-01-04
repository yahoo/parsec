package com.yahoo.ec.parsec.tools.mojo.generators

import com.yahoo.parsec.tools.mojo.generators.ParsecGeneratorUtil
import com.yahoo.parsec.tools.mojo.generators.ParsecPackageResolver
import com.yahoo.parsec.tools.mojo.generators.ParsecPackageStruct
import com.yahoo.parsec.tools.mojo.generators.ParsecValidationGroupGenerator
import com.yahoo.parsec.tools.mojo.utils.FileUtils
import org.apache.maven.project.MavenProject
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

/**
 * ParsecValidationGroupsGenerator Unit Test
 */
class ParsecValidationGroupsGeneratorTest extends Specification {

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

    def getGenerator() {
        return new ParsecValidationGroupGenerator(packageStruct, generatorUtil, fileUtils)
    }

    def "generateParsecValidationGroups() should generate from template with expected arguments"() {
        given:
        fileUtils.findPatternsInFile(_ as Path, "ParsecValidationGroups\\.(.+?)\\.class") >> { Path path, def dummy ->
            if (path.getFileName().endsWith("User.java")) {
                ["Create", "Update"] as Set
            } else {
                [:] as Set
            }
        }

        when:
        getGenerator().generateParsecValidationGroups()

        then:
        1 * generatorUtil.generateFromTemplateTo(
                "ParsecValidationGroups.java", "java.com.example",
                "./src/test/resources/generated-sources/java/com/example/parsec_generated",
                _ as Map<String, String>, true) >> { dummy1, dummy2, dummy3, materials, overwrite ->
            assert materials.get("{validationGroups}").indexOf("public interface Create { }") > 0
            assert materials.get("{validationGroups}").indexOf("public interface Update { }") > 0
        }
    }
}

