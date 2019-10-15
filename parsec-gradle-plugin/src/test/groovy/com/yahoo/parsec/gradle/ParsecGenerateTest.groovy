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
/**
 * @author waynewu
 */

class ParsecGenerateTest extends Specification {

    @Rule
    TemporaryFolder testFolder = new TemporaryFolder()

    Project project
    Task task
    ParsecPluginExtension pluginExtension
    File projectDir
    PathUtils pathUtils

    def setup(){
        projectDir =  testFolder.newFolder("dummy_project")
        project = new ProjectBuilder().withProjectDir(projectDir).build()
        pluginExtension = project.extensions.create("settings", ParsecPluginExtension)
        task = project.task('generateTask', type: ParsecGenerateTask)
        pathUtils = Spy(PathUtils, constructorArgs: [project, pluginExtension])
    }

    @Unroll
    def "parsec generated files should be in the same path for single namespace"() throws Exception{
        given:
            def generatedSourcePath = "build/generated-sources/java"
            def packagePath = "com/example"
            def rdlPath = getResourceFilePath("/rdl/sample") //rdl files can't be found

            FileUtils fileUtils = getMockFileUtils()
            passFileUtils(this.task, fileUtils)
            setTaskProperties(rdlPath)

            def tempDir = projectDir.absolutePath

            prepareParseRDL(fileUtils, tempDir)
            def files = projectDir.listFiles()
            pluginExtension.handleUncaughtExceptions = handleUncaughtExceptions
            pluginExtension.generateParsecError = generateParsecError
            pluginExtension.additionSwaggerJsonPath = tempDir + "/build/swagger-json"

        when:
            task.executeTask()

        then:
            assertFilesExists(
                    ["DefaultApplication.java", "DefaultResourceContext.java", "DefaultWebListener.java", "SampleHandlerImpl.java"] as String[],
                    tempDir + "/src/main/java/" + packagePath
            )

            assertFilesExists(
                    ["_local-swagger-jsons.js", "sample_swagger.json"] as String[],
                    tempDir + "/build/generated-resources/parsec/doc"
            )
            assertFilesExists(
                    ["_local-swagger-jsons.js", "sample_swagger.json"] as String[],
                    tempDir + "/build/swagger-json"
            )
            assertFilesExists(
                    ["ParsecApplication.java", "ParsecWebListener.java", "ParsecWrapperServlet.java", "ParsecValidationGroups.java"] as String[],
                    tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated"
            )

            // validate exception mapper
            assertFilesExists(
                    ["ParsecExceptionMapper.java"] as String[],
                    tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated",
                    handleUncaughtExceptions
            )
            assertFilesExists(
                    ["DefaultExceptionMapper.java"] as String[],
                    tempDir + "/src/main/java/" + packagePath,
                    handleUncaughtExceptions
            )

            // validate parsec error
            assertFilesExists(
                    ["ParsecResourceError.java", "ParsecErrorBody.java", "ParsecErrorDetail.java"] as String[],
                    tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated",
                    parsecErrorGenerated
            )

        where:
            handleUncaughtExceptions | generateParsecError | parsecErrorGenerated
            false                    | false               | false
            true                     | false               | true
            false                    | true                | true
            true                     | true                | true
    }


    def "parsec generated files should be in the intersect path for multiple namespace"() throws Exception {
        given:
            def generatedSourcePath = "build/generated-sources/java"
            def packagePath = "com/example"
            def rdlPath = getResourceFilePath("/rdl/sample2")
            def fileUtils = getMockFileUtils()
            passFileUtils(this.task, fileUtils)
            setTaskProperties(rdlPath)

            def tempDir = projectDir.absolutePath

            prepareParseRDL(fileUtils, tempDir)

            pluginExtension.handleUncaughtExceptions = handleUncaughtExceptions

        when:
            task.executeTask()

        then:
            assertFilesExists(
                    ["ParsecApplication.java", "ParsecWebListener.java", "ParsecWrapperServlet.java"] as String[],
                    tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated"
            )
            assertFilesExists(
                    ["DefaultApplication.java", "DefaultWebListener.java"] as String[],
                    tempDir + "/src/main/java/" + packagePath
            )
            assertFilesExists(
                    ["User.java", "Sample2aResources.java", "ParsecValidationGroups.java"] as String[],
                    tempDir + "/" + generatedSourcePath + "/" + packagePath + "/sample2a/parsec_generated"
            )
            assertFilesExists(
                    ["User.java", "Sample2bResources.java", "ParsecValidationGroups.java"] as String[],
                    tempDir + "/" + generatedSourcePath + "/" + packagePath + "/sample2b/parsec_generated"
            )
            assertFilesExists(
                    ["Sample2aHandlerImpl.java", "DefaultResourceContext.java"] as String[],
                    tempDir + "/src/main/java/" + packagePath + "/sample2a"
            )
            assertFilesExists(
                    ["Sample2bHandlerImpl.java", "DefaultResourceContext.java"] as String[],
                    tempDir + "/src/main/java/" + packagePath + "/sample2b"
            )
            assertFilesExists(
                    ["_local-swagger-jsons.js", "sample2a_swagger.json", "sample2b_swagger.json"] as String[],
                    tempDir + "/build/generated-resources/parsec/doc"
            )

            // validate exception mapper, always not generated
            assertFilesExists(
                    ["ParsecExceptionMapper.java"] as String[],
                    tempDir + "/" + generatedSourcePath + "/" + packagePath + "/parsec_generated",
                    false
            )
            assertFilesExists(
                    ["DefaultExceptionMapper.java"] as String[],
                    tempDir + "/src/main/java/" + packagePath,
                    false
            )

        where:
            handleUncaughtExceptions << [false, true]

    }

    def "parsec generated files no intersect path error"() throws Exception {
        given:
            def rdlPath = getResourceFilePath("/rdl/sample3")
            def fileUtils = getMockFileUtils()
            passFileUtils(this.task, fileUtils)
            setTaskProperties(rdlPath)

        when:
            task.executeTask()

        then:
            thrown(TaskExecutionException)
            //TODO: Catch the exact error message
    }

    def "should ignore if no rdl file found"() throws Exception {
        given:
            def rdlPath = getResourceFilePath("/rdl/no_files")

            def fileUtils = getMockFileUtils()
            passFileUtils(this.task, fileUtils)
            setTaskProperties(rdlPath)

        when:
            task.executeTask()

        then:
            1 == 1
    }

    def "should ignore if turn off all rdl generator options"() throws Exception {
        given:
            def rdlPath = getResourceFilePath("/rdl/sample")
            def fileUtils = getMockFileUtils()
            passFileUtils(this.task, fileUtils)
            setTaskProperties(rdlPath)

            pluginExtension.generateServer = false
            pluginExtension.generateSwagger = false
            pluginExtension.generateModel = false
            pluginExtension.generateHandlerImpl = false
            pluginExtension.generateJson = false
            pluginExtension.handleUncaughtExceptions = false
            pluginExtension.generateClient = false

        when:
            task.executeTask()

        then:
            1 == 1

    }

    def "test getter methods"() throws Exception {
        /* Use this to test any getter methods of interest */

        given:
            def rdlPath = getResourceFilePath("/rdl/sample")
            def fileUtils = getMockFileUtils()
            passFileUtils(this.task, fileUtils)

        when:
            setTaskProperties(rdlPath)

        then:
            pathUtils.getRelativeBinPath() == "bin"
            pathUtils.getSwaggerUIPath().endsWith("build/generated-resources/parsec/swagger-ui")
            pathUtils.getJavaTestRootPath().endsWith("dummy_project/src/test/java")

    }

    /**
     * Generate a spy FileUtils with task.logger
     * @return fileUtils
     */
    private FileUtils getMockFileUtils(){
        Logger logger = task.getLogger()
        FileUtils fileUtils = Spy(FileUtils, constructorArgs: [logger])

        return fileUtils
    }

    /**
     * Pass Mock FileUtils to the task
     * Due to the passing method can vary
     * We put it in this function so that only one change
     * has to be made here if the passing method changes
     * @param mockFileUtils
     */
    private void passFileUtils(Task task, FileUtils mockFileUtils){
        task.setFileUtils(mockFileUtils)
    }

    /**
     * Set the plugin extension properties
     * @param rdlPath
     * @param generatedSourcePath
     */
    private void setTaskProperties(def rdlPath){
        pluginExtension.sourcePath = rdlPath
        pluginExtension.sourceFiles = "*.rdl"
        pluginExtension.generateServer = true
        pluginExtension.generateSwagger = true
        pluginExtension.generateModel = true
        pluginExtension.generateHandlerImpl = true
        pluginExtension.generateJson = true
        pluginExtension.swaggerRootPath = "test_path"
        pluginExtension.handleUncaughtExceptions = false
        pluginExtension.generateParsecError = false
    }

    /**
     * Run parsec-init to generate the parsec_rdl bin for generating the rdl
     * @param fileUtils: same fileUtils as parsec-generate
     * @param tempDir: testing folder
     */
    private void prepareParseRDL(FileUtils fileUtils, def tempDir){
        Task init_task = project.task("initTask", type: ParsecInitTask)
        passFileUtils(init_task, fileUtils)
        init_task.executeTask() //generate the parsec_rdl bin

        new File(tempDir + "/build/bin/parsec_rdl").setExecutable(true)
    }

    /**
     * Get the string file path of the resource folder with the rdls
     * Will return a random non-existing file path if no resource
     * exist in the path given
     * @param path
     * @return filepath, fake or not
     */
    private String getResourceFilePath(String path){
        if(!path.startsWith("/")){
            path = "/" + path
        }
        def resource = this.getClass().getResource(path)
        return (resource) ? resource.getFile().toString() : "src/test/resources/rdl/no_files"
    }

    private void assertFilesExists(String[] files, def targetDir){
        assertFilesExists(files, targetDir, true)
    }

    private void assertFilesExists(String[] files, def targetDir, boolean assertion){
        for (file in files) {
            assert assertion == new File(targetDir + "/" + file).exists()
        }
    }
}
