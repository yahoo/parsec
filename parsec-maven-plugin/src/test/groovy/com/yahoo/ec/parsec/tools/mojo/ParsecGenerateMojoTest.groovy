package com.yahoo.ec.parsec.tools.mojo

import com.yahoo.parsec.tools.mojo.AbstractParsecMojo
import com.yahoo.parsec.tools.mojo.ParsecGenerateMojo
import com.yahoo.parsec.tools.mojo.ParsecInitializeMojo
import com.yahoo.parsec.tools.mojo.utils.FileUtils
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path

/**
 * ParsecGenerateMojo Unit Test
 */
class ParsecGenerateMojoTest extends Specification {
    @Rule
    TemporaryFolder testFolder

    @Unroll
    def "parsec generated files should be in the same path for single namespace"() throws Exception {
        given:
        def generatedSourcePath = "target/generated-sources/java"
        def packagePath = "com/example"
        def rdlPath = "src/test/resources/rdl/sample"

        def fileUtils = getMockFileUtils()
        def mojo =  getMockMojo(fileUtils, rdlPath, generatedSourcePath)
        def projDir = createDummyProjDirAndSetupStubs("dummyProj", mojo)
        def tempDir = projDir.absolutePath

        prepareParseRDL(fileUtils, tempDir)
        copyRDLsTo((String[])["sample.rdl"], rdlPath, tempDir)

        mojo.handleUncaughtException = handleUncaughtException
        mojo.generateParsecError = generateParsecError

        when:
        mojo.execute()

        then:
        assertFilesExists(
                (String[])["ParsecApplication.java", "ParsecWebListener.java", "ParsecWrapperServlet.java", "ParsecValidationGroups.java"],
                tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated"
        )
        assertFilesExists(
                (String[]) ["DefaultApplication.java", "DefaultResourceContext.java", "DefaultWebListener.java", "SampleHandlerImpl.java"],
                tempDir + "/src/main/java/" + packagePath
        )
        assertFilesExists(
                (String[]) ["_local-swagger-jsons.js", "sample_swagger.json"],
                tempDir + "/target/generated-resources/parsec/doc"
        )

        // validate exception mapper
        assertFilesExists(
                (String[])["ParsecExceptionMapper.java"],
                tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated",
                handleUncaughtException
        )
        assertFilesExists(
                (String[]) ["DefaultExceptionMapper.java"],
                tempDir + "/src/main/java/" + packagePath,
                handleUncaughtException
        )

        // validate parsec error
        assertFilesExists(
                (String[])["ParsecResourceError.java", "ParsecErrorBody.java", "ParsecErrorDetail.java"],
                tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated",
                parsecErrorGenerated
        )

        where:
        handleUncaughtException | generateParsecError | parsecErrorGenerated
        false | false | false
        true | false | true
        false | true | true
        true | true | true
    }

    def "parsec generated files should be in the intersect path for multiple namespace"() throws Exception {
        given:
        def generatedSourcePath = "target/generated-sources/java"
        def packagePath = "com/example"
        def rdlPath = "src/test/resources/rdl/sample2"

        def fileUtils = getMockFileUtils()
        def mojo =  getMockMojo(fileUtils, rdlPath, generatedSourcePath)
        def projDir = createDummyProjDirAndSetupStubs("dummyProj", mojo)
        def tempDir = projDir.absolutePath

        prepareParseRDL(fileUtils, tempDir)
        copyRDLsTo((String[])["sample2a.rdl", "sample2b.rdl"], rdlPath, tempDir)

        mojo.handleUncaughtException = handleUncaughtException

        when:
        mojo.execute()

        then:
        assertFilesExists(
                (String[])["ParsecApplication.java", "ParsecWebListener.java", "ParsecWrapperServlet.java"],
                tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated"
        )
        assertFilesExists(
                (String[])["DefaultApplication.java", "DefaultWebListener.java"],
                tempDir + "/src/main/java/" + packagePath
        )
        assertFilesExists(
                (String[])["User.java", "Sample2aResources.java", "ParsecValidationGroups.java"],
                tempDir + "/" + generatedSourcePath + "/" + packagePath + "/sample2a/parsec_generated"
        )
        assertFilesExists(
                (String[])["User.java", "Sample2bResources.java", "ParsecValidationGroups.java"],
                tempDir + "/" + generatedSourcePath + "/" + packagePath + "/sample2b/parsec_generated"
        )
        assertFilesExists(
                (String[])["Sample2aHandlerImpl.java", "DefaultResourceContext.java"],
                tempDir + "/src/main/java/" + packagePath + "/sample2a"
        )
        assertFilesExists(
                (String[])["Sample2bHandlerImpl.java", "DefaultResourceContext.java"],
                tempDir + "/src/main/java/" + packagePath + "/sample2b"
        )
        assertFilesExists(
                (String[])["_local-swagger-jsons.js", "sample2a_swagger.json", "sample2b_swagger.json"],
                tempDir + "/target/generated-resources/parsec/doc"
        )

        // validate exception mapper, always not generated
        assertFilesExists(
                (String[])["ParsecExceptionMapper.java"],
                tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated",
                false
        )
        assertFilesExists(
                (String[])["DefaultExceptionMapper.java"],
                tempDir + "/src/main/java/" + packagePath,
                false
        )
        where:
        handleUncaughtException << [false , true]
    }

    def "parsec generated files no intersect path error"() throws Exception {
        given:
        def generatedSourcePath = "target/generated-sources/java"
        def packagePath = "com/example"
        def rdlPath = "src/test/resources/rdl/sample3"

        def fileUtils = getMockFileUtils()
        def mojo =  getMockMojo(fileUtils, rdlPath, generatedSourcePath)
        mojo.sourceFiles = "sample3a.rdl,sample3b.rdl"

        def projDir = createDummyProjDirAndSetupStubs("dummyProj", mojo)
        def tempDir = projDir.absolutePath

        prepareParseRDL(fileUtils, tempDir)
        copyRDLsTo((String[])["sample3a.rdl", "sample3b.rdl"], rdlPath, tempDir)

        when:
        mojo.execute()

        then:
        def e = thrown(MojoExecutionException)
        e.getMessage() == "no intersect part found from packages"
    }

    def "should ignore if no rdl file found"() throws Exception {
        given:
        def generatedSourcePath = "target/generated-sources/java"
        def packagePath = "com/example"
        def rdlPath = "src/test/resources/rdl/no_files"

        def fileUtils = getMockFileUtils()
        def mojo =  getMockMojo(fileUtils, rdlPath, generatedSourcePath)
        def projDir = createDummyProjDirAndSetupStubs("dummyProj", mojo)
        def tempDir = projDir.absolutePath

        when:
        mojo.execute()

        then:
        // nothing to test
        1 == 1
    }

    def "should ignore if turn off all rdl generator options"() throws Exception {
        given:
        def generatedSourcePath = "target/generated-sources/java"
        def packagePath = "com/example"
        def rdlPath = "src/test/resources/rdl/sample"

        def fileUtils = getMockFileUtils()
        def mojo =  getMockMojo(fileUtils, rdlPath, generatedSourcePath)
        def projDir = createDummyProjDirAndSetupStubs("dummyProj", mojo)
        def tempDir = projDir.absolutePath

        prepareParseRDL(fileUtils, tempDir)
        copyRDLsTo((String[])["sample.rdl"], rdlPath, tempDir)

        mojo.server = false
        mojo.swagger = false
        mojo.model = false
        mojo.handlerImpl = false
        mojo.json = false
        mojo.handleUncaughtException = false

        when:
        mojo.execute()

        then:
        // nothing to test
        1 == 1
    }

    def "test getter methods"() throws Exception {
        given:
        def generatedSourcePath = "target/generated-sources/java"
        def packagePath = "com/example"
        def rdlPath = "src/test/resources/rdl/sample"
        def fileUtils = getMockFileUtils()

        when:
        def mojo = getMockMojo(fileUtils, rdlPath, generatedSourcePath)
        createDummyProjDirAndSetupStubs("dummyProj", mojo)

        then:
        // test unused getter methods
        mojo.getRelativeBinPath() == "target/bin"
        mojo.getRelativeScriptPath() == "parsec-bin"
        mojo.getRelativeSwaggerUIPath() == "target/generated-resources/parsec/swagger-ui"
        mojo.getRelativeJavaTestRootPath() == "src/test/java"
        mojo.getJavaTestRootPath().endsWith("dummyProj/src/test/java") == true
    }

    private ParsecGenerateMojo getMockMojo(
            def fileUtils, def rdlPath, def generatedSourcePath) {
        def mojo = new ParsecGenerateMojo(fileUtils)
        mojo.sourceDirectory = new File(rdlPath)
        mojo.sourceFiles = "*.rdl"
        mojo.server = true
        mojo.swagger = true
        mojo.model = true
        mojo.handlerImpl = true
        mojo.json = true
        mojo.swaggerRootPath = "test_path"
        mojo.relativeGeneratedSourcesPath = generatedSourcePath
        mojo.handleUncaughtException = false
        mojo.generateParsecError = false
        mojo.swaggerScheme = ""

        return mojo
    }

    private FileUtils getMockFileUtils() {
        Log mockLog = Mock()
        FileUtils fileUtils = Spy(FileUtils, constructorArgs: [mockLog])
        //Set<Path> swaggerJsons = new HashSet<String>(Arrays.asList("sample_swagger.json"));
        //fileUtils.findFiles(_) >> swaggerJsons

        return fileUtils
    }

    private void assertFilesExists(String[] files, def targetDir) {
        assertFilesExists(files, targetDir, true)
    }

    private void assertFilesExists(String[] files, def targetDir, def assertion) {
        for (file in files) {
            assert assertion == new File(targetDir + "/" + file).exists()
        }
    }

    private def prepareParseRDL(def fileUtils, def tempDir) {
        // untar parsec_rdl by ParsecInitializeMojo
        fileUtils.getFileFromResource("/rdl_bin/parsec_rdl.zip") >> new File("target/classes/rdl_bin/parsec_rdl.zip")
        def initMojo = new ParsecInitializeMojo(fileUtils)
        createDummyProjDirAndSetupStubs("dummyInitProj", initMojo)
        initMojo.execute()

        // copy parsec_rdl binary to temp dir
        org.apache.commons.io.FileUtils.copyFile(
                new File(initMojo.getBinPath() + "/" + initMojo.PARSEC_RDL_BINARY),
                new File(tempDir + "/target/bin/parsec_rdl")
        )
        new File(tempDir + "/target/bin/parsec_rdl").setExecutable(true)
    }

    private def copyRDLsTo(String[] rdls, def rdlDir, def targetDir) {
        // copy rdl to temp dir
        def path = rdlDir
        for (file in rdls) {
            def targetPath = path + "/" + file
            org.apache.commons.io.FileUtils.copyFile(
                new File(targetPath),
                new File(targetDir + "/" + targetPath)
            )
        }
    }

    private def createDummyProjDirAndSetupStubs(def projDirName, AbstractParsecMojo mojo) throws Exception {
        def projDir = testFolder.newFolder(projDirName)
        MavenProject stubProject = Mock()
        stubProject.getBasedir() >> projDir
        MojoTestUtils.injectStubMavenProj(mojo, stubProject)
        return projDir;
    }
}
