import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * @author waynewu
 */

public class ParsecGradleBuildTest extends Specification{

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File buildFile;
    private File propertyFile;


    def setup(){
        buildFile = temporaryFolder.newFile('build.gradle')
        propertyFile = temporaryFolder.newFile('gradle.properties')
    }


    def "loading the build file should have no error and return success"(){
        given:
            buildFile.append(getClass().getResourceAsStream("/parsec.gradle"))
            propertyFile.append(getClass().getResourceAsStream("/gradle.properties"))

        when:
            BuildResult buildResult = GradleRunner.create()
                .withProjectDir(temporaryFolder.getRoot())
                .withArguments('tasks')
                .build()

        then:
            buildResult.task(":tasks").getOutcome() == SUCCESS

    }

}
