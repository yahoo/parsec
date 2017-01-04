package com.yahoo.ec.parsec.tools.mojo

import com.yahoo.parsec.tools.mojo.ParsecInitializeMojo
import com.yahoo.parsec.tools.mojo.utils.FileUtils
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path

/**
 * ParsecInitializeMojo Unit Test
 */
class ParsecInitializeMojoTest extends Specification {
    @Rule
    TemporaryFolder testFolder

    def "should create all required Dirs And Files By Default"() throws Exception {
        given:
        def mojo = new ParsecInitializeMojo()
        def projDir = createDummyProjDirAndSetupStubs("dummyProj", mojo)

        when:
        mojo.execute()

        //TODO
        //this is a big then block, should consider to refactor ParsecInitializeMojo implementation
        then:
        def parsecBin = new File (mojo.getBinPath()+"/parsec_rdl")
        parsecBin.exists()
        parsecBin.isFile()

        def scriptPath = new File(mojo.getScriptPath())
        scriptPath.exists();
        //don't assert if the dir is not empty
        //because during unit testing it does't copy the file over
        //the Mojo implementation assumes this plugin is running in an installed (as a jar) environment

        //screwdriver.yaml should exits
        new File(projDir,"screwdriver.yaml").exists()
        new File(projDir,"src/main/rdl/ParsecResourceError.rdli").exists()

        //swagger-ui dir should exist
        def swaggerUIDir = new File(mojo.getSwaggerUIPath())
        swaggerUIDir.exists()
        swaggerUIDir.list().length > 1
    }

    @Unroll
    def "should not modify #file if exits"() throws Exception {
        given:
        def mojo = new ParsecInitializeMojo()
        def projDir = createDummyProjDirAndSetupStubs("dummyProj", mojo)

        //a pre-existing file
        def existingFile = testFolder.newFile(projDir.getName()+ file);
        def expectedModifiedTS = existingFile.lastModified();

        when:
        mojo.execute()

        then:
        def actualModifiedTS = new File(projDir, file).lastModified();
        actualModifiedTS == expectedModifiedTS;

        where:
        file << ["/screwdriver.yaml"]
    }

    private def createDummyProjDirAndSetupStubs(def projDirName, ParsecInitializeMojo mojo) throws Exception {
        def projDir = testFolder.newFolder(projDirName)
        MavenProject stubProject = Mock()
        stubProject.getBasedir() >> projDir
        MojoTestUtils.injectStubMavenProj(mojo, stubProject)
        return projDir;
    }

    def "should throw exception while writing rdl bin got io exception"() throws Exception {
        given:
        Log mockLog = Mock()
        FileUtils fileUtils = Spy(FileUtils, constructorArgs: [mockLog])
        fileUtils.writeResourceAsExecutable(_, _) >> { throw new IOException("mock io exception") }
        def mojo = new ParsecInitializeMojo(fileUtils)
        createDummyProjDirAndSetupStubs("dummyProj", mojo)

        when:
        mojo.execute()

        then:
        def e = thrown(MojoExecutionException)
        e.message == "mock io exception"
    }

    def "should copy scripts to basedir if the file extension is .sh or .rb"() throws Exception {
        given:
        Log mockLog = Mock()
        FileUtils fileUtils = Spy(FileUtils, constructorArgs: [mockLog])
        fileUtils.writeResourceAsExecutable(_, _) >> {}
        Path mockPath1 = Mock()
        mockPath1.toString() >> "test.rb"
        Path mockPath2 = Mock()
        mockPath2.toString() >> "test.sh"
        Path mockPath3 = Mock()
        mockPath3.toString() >> "test.pl"
        List<Path> listPath = [mockPath1, mockPath2, mockPath3]
        fileUtils.listDirFilePaths(_) >> listPath

        def mojo = new ParsecInitializeMojo(fileUtils)
        createDummyProjDirAndSetupStubs("dummyProj", mojo)

        when:
        mojo.execute()

        then:
        1 * fileUtils.writeResourceAsExecutable("test.rb", _)
        1 * fileUtils.writeResourceAsExecutable("test.sh", _)
        0 * fileUtils.writeResourceAsExecutable("test.pl", _)
    }

    def "should unzip parsec_bin"() throws Exception {
        given:
        Log mockLog = Mock()
        FileUtils fileUtils = Spy(FileUtils, constructorArgs: [mockLog])
        fileUtils.getFileFromResource("/rdl_bin/parsec_rdl.zip") >> new File("target/classes/rdl_bin/parsec_rdl.zip")

        def mojo = new ParsecInitializeMojo(fileUtils)
        createDummyProjDirAndSetupStubs("dummyProj", mojo)

        when:
        mojo.execute()

        then:
        new File(mojo.getBinPath() + "/" + mojo.PARSEC_RDL_BINARY).exists() == true
    }
}
