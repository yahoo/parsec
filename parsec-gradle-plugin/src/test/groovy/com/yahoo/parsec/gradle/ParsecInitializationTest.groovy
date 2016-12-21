package com.yahoo.parsec.gradle

import com.yahoo.parsec.gradle.utils.FileUtils
import com.yahoo.parsec.gradle.utils.PathUtils
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path
/**
 * @author waynewu
 */
class ParsecInitializationTest extends Specification {

    @Rule
    TemporaryFolder testFolder

    Project project
    Task task
    ParsecPluginExtension pluginExtension
    PathUtils pathUtils
    File projectDir

    def setup(){
        projectDir =  testFolder.newFolder("dummy_project")
        project = new ProjectBuilder().withProjectDir(projectDir).build()
        pluginExtension = project.extensions.create("settings", ParsecPluginExtension)
        task = project.task('targetTask', type: ParsecInitTask)
        pathUtils = new PathUtils(project, pluginExtension)
    }


    def "Task should create all required directories and files by default"() throws Exception{
        when:
            task.executeTask()

        then:
            def parsecBin = new File(pathUtils.getBinPath() + '/rdl')
            parsecBin.exists()
            parsecBin.isFile()

            def swaggerUIDir = new File(pathUtils.getSwaggerUIPath())
            swaggerUIDir.exists()
            swaggerUIDir.list().length > 1

    }

    @Unroll
    def "should not modify #file if it exists"() throws Exception{
        given:
            def existingFile = testFolder.newFile(projectDir.getName() + file)
            def expectedModifiedTS = existingFile.lastModified()

        when:
            task.executeTask()

        then:
            def actualModifiedTS = new File(projectDir, file).lastModified()
            actualModifiedTS == expectedModifiedTS

        where:
            file << ["/test.txt"]
    }


    def "should throw exception while writing rdl bin got io exception"() throws Exception {
        given:
            Logger mockLog = Mock()
            FileUtils fileUtils = Spy(FileUtils, constructorArgs: [mockLog])
            task.setFileUtils(fileUtils)
            fileUtils.writeResourceAsExecutable(_, _) >> {throw new IOException("mock IO Exception")}

        when:
            task.executeTask()

        then:
            def e = thrown(TaskExecutionException)
            e.getCause().getMessage() == "mock IO Exception"
    }


    def "should copy scripts to basedir if the file extension is .sh or .rb"() throws Exception{
        given:
            Logger mockLog = Mock()
            FileUtils fileUtils = Spy(FileUtils, constructorArgs: [mockLog])
            task.setFileUtils(fileUtils)
            fileUtils.writeResourceAsExecutable(_, _) >> {} //do nothing
            Path mockPath1 = Mock()
            mockPath1.toString() >> "test.rb"
            Path mockPath2 = Mock()
            mockPath2.toString() >> "test.sh"
            Path mockPath3 = Mock()
            mockPath3.toString() >> "test.pl" //should not be copied
            List<Path> listPath = [mockPath1, mockPath2, mockPath3]
            fileUtils.listDirFilePaths(_) >> listPath

        when:
            task.executeTask()

        then:
            1 * fileUtils.writeResourceAsExecutable("test.rb", _)
            1 * fileUtils.writeResourceAsExecutable("test.sh", _)
            0 * fileUtils.writeResourceAsExecutable("test.pl", _)

    }

    def "should unzip parsec_bin"() throws Exception {
        when:
            task.executeTask()

        then:
            new File(pathUtils.getBinPath() + "/" + pathUtils.RDL_BINARY).exists()
    }

}
