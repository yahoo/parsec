package com.yahoo.ec.parsec.tools.mojo.generators

import com.yahoo.parsec.tools.mojo.generators.ParsecGeneratorUtil
import com.yahoo.parsec.tools.mojo.utils.FileUtils
import org.apache.maven.plugin.MojoExecutionException
import spock.lang.Specification
import spock.lang.Unroll

/**
 * ParsecGeneratorUtil Unit Test
 */
class ParsecGeneratorUtilTest extends Specification {

    FileUtils mockFileUtils

    def setup() {
        mockFileUtils = Mock()
    }

    def getGeneratorUtil() {
        new ParsecGeneratorUtil(mockFileUtils)
    }

    @Unroll
    def "packageNameToPath(#givenPackageName) should return #expectPackagePath"() {
        expect:
        expectPackagePath == getGeneratorUtil().packageNameToPath(givenPackageName)

        where:
        givenPackageName || expectPackagePath
        "com.yahoo.example" || "com/yahoo/example"
        "example" || "example"
    }

    @Unroll
    def "getIntersectPackageName(#givenPackageNames) should return intersect package #expectIntersectPackage"() {
        expect:
        expectIntersectPackage == getGeneratorUtil().getIntersectPackageName(givenPackageNames)

        where:
        givenPackageNames || expectIntersectPackage
        ["com.yahoo.example", "com.yahoo"] || "com.yahoo"
        ["com.yahoo.example"] || "com.yahoo.example"
        ["com", "com"] || "com"
    }

    @Unroll
    def "getIntersectPackageName(#givenPackageNames) should throw exception if no intersect package found"() {
        when:
        getGeneratorUtil().getIntersectPackageName(givenPackageNames)

        then:
        def e = thrown(MojoExecutionException)
        e.getMessage() == "no intersect part found from packages"

        where:
        givenPackageNames | _
        ["com.yahoo.example", "yahoo.example"] | _
        ["com.yahoo.example", "co.yahoo.example"] | _
        ["com.yahoo.example", "yahoo.com.example"] | _
        ["com", "co"] | _
        ["com", "om"] | _
    }

    @Unroll
    def "generateFromTemplateTo(#templateName, #packageName, #outputDir, #replaceMaterials) should check and create directory"() {
        expect:
        getGeneratorUtil().generateFromTemplateTo(templateName, packageName, outputDir, replaceMaterials, true)
        mockFileUtils.checkAndCreateDirectory(outputDir)

        where:
        templateName | packageName | outputDir | replaceMaterials
        "ParsecApplication.java" | "com.yahoo" | "/tmp/" | ["register": "regist replacement"]
        "ParsecApplication.java" | "com.yahoo.example" | "/tmp/com/yahoo" | ["bind": "bind replacement"]
        "ParsecApplication.java" | "com.yahoo.example" | "/tmp/com" | [:]
        "ParsecApplication.java" | "com.yahoo.example" | "/tmp/yahoo" | ["nothing": "nothing replacement"]
    }

    def "generateFromTemplateTo() should throw null pointer exception if template not found"() {
        when:
        getGeneratorUtil().generateFromTemplateTo("NotFoundTemplate.java", "com.yahoo", "/tmp/", [:], true)

        then:
        thrown(NullPointerException)
    }

    def "generateFromTemplateTo() should throw mojo exception if check and create directory error"() {
        when:
        getGeneratorUtil().generateFromTemplateTo("ParsecApplication.java", "com.yahoo", "/tmp/", [:], true)

        then:
        thrown(MojoExecutionException)
        mockFileUtils.checkAndCreateDirectory("/tmp/") >> { throw new IOException("mock io expection") }
    }

    @Unroll
    def "generateFromTemplateTo(#templateName, #packageName, #outputDir) should check and create directory"() {
        expect:
        getGeneratorUtil().generateFromTemplateTo(templateName, packageName, outputDir, true)
        mockFileUtils.checkAndCreateDirectory(outputDir)

        where:
        templateName | packageName | outputDir
        "ParsecWebListener.java" | "com.yahoo" | "/tmp/"
        "DefaultApplication.java" | "com.yahoo.example" | "/tmp/com/yahoo"
        "ParsecApplication.java" | "com" | "/tmp/yahoo"
    }
}
