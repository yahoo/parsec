package com.yahoo.parsec.tools.mojo;

import com.yahoo.parsec.tools.mojo.utils.FileUtils;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;


/**
 * @author sho
 */
@Mojo(name = "init", defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.COMPILE , requiresProject = true )

public class ParsecInitializeMojo extends AbstractParsecMojo {

    /**
     * Parsec RDL binary filename.
     */
    private static final String PARSEC_RDL_BINARY = "parsec_rdl";

    /**
     * The FileUtil.
     */
    private FileUtils fileUtils;

    /**
     * The default constructor.
     */
    public ParsecInitializeMojo() {
        this.fileUtils = new FileUtils(getLog());
    }

    /**
     * Constructor with fileUtil argument for unit testing injection.
     *
     * @param fileUtils fileUtil object
     */
    ParsecInitializeMojo(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    /**
     * Execute.
     *
     * @throws MojoExecutionException MojoExecutionException
     * @throws MojoFailureException MojoFailureException
     */
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Create ${baseDir}/target/bin
        fileUtils.checkAndCreateDirectory(getBinPath());

        // Extract parsec_rdl to ${baseDir}/target/bin
        String rdlBinSuffix = (System.getProperty("os.name").equals("Mac OS X")) ? "darwin" : "linux";
        File file = fileUtils.getFileFromResource("/rdl_bin/parsec_rdl.zip");

        try (
            ZipFile zipFile = new ZipFile(file);
            InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(PARSEC_RDL_BINARY + "-" + rdlBinSuffix));
        ){
            fileUtils.writeResourceAsExecutable(inputStream, getBinPath() + "/" + PARSEC_RDL_BINARY);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // Create ${baseDir}/parsec-bin
        fileUtils.checkAndCreateDirectory(getScriptPath());

        // Copy all scripts under resource/scripts to ${baseDir}/parsec-bin
        for (Path scriptPath : fileUtils.listDirFilePaths("scripts")) {
            String scriptPathString = scriptPath.toString();
            if (scriptPathString.endsWith(".sh") || scriptPathString.endsWith(".rb")) {
                fileUtils.writeResourceAsExecutable(scriptPath.toString(), getScriptPath() + "/" + scriptPath.getFileName());
            }
        }

        // Create ${baseDir}/target/generated-resources/swagger-ui
        fileUtils.checkAndCreateDirectory(getSwaggerUIPath());

        // Create ${baseDir}/src/main/rdl
        fileUtils.checkAndCreateDirectory(getRdlPath());

        // Extract swagger-ui archive if ${baseDir}/target/generated-resources/swagger-ui is empty
        if (new File(getSwaggerUIPath()).list().length <= 0) {
            fileUtils.unTarZip("/swagger-ui/swagger-ui.tgz", getSwaggerUIPath(), true);
        }

        // Copy screwdriver.yaml to base dir if it doesn't exist
        fileUtils.writeResourceToFile("/screwdriver.yaml", getProject().getBasedir() + "/screwdriver.yaml", false);

        // Copy ParsecResourceError.yaml to rdl dir if it doesn't exist
        fileUtils.writeResourceToFile(
                "/rdl/ParsecResourceError.rdli",
                getRdlPath() + "/ParsecResourceError.rdli",
                false
        );
    }
}
