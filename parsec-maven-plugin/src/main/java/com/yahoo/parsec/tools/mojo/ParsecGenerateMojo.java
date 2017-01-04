package com.yahoo.parsec.tools.mojo;

import com.yahoo.parsec.tools.mojo.generators.ParsecFileGenerator;
import com.yahoo.parsec.tools.mojo.generators.ParsecGeneratorUtil;
import com.yahoo.parsec.tools.mojo.generators.ParsecPackageResolver;
import com.yahoo.parsec.tools.mojo.utils.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * @author sho
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE , requiresProject = true )
public class ParsecGenerateMojo extends AbstractParsecMojo {
    /**
     * The directory where the RDL files ({@code *.rdl}) are located.
     */
    @Parameter( defaultValue="${basedir}/src/main/rdl" )
    private File sourceDirectory;

    /**
     * The RDL file(s) to parse, comma separated. If not present, all .rdl files in the source directory
     * are processed. The filenames here can also be absolute, and override the source directory.
     */
    @Parameter( defaultValue="*.rdl" )
    private String sourceFiles;

    /**
     * If set to true (the default), the generated code will include model classes for the RDL types.
     */
    @Parameter( defaultValue="true" )
    protected boolean model;

    /**
     * If set to true (the default), the generated code will include JAX-RS (jersey) server classes,
     * using the XxxHandler interface (with a context), rather than the older 'jersey' option
     */
    @Parameter( defaultValue="true" )
    protected boolean server;

    /**
     * If set to true, the generated code will include swagger resources
     * Note: this generates older 1.x swagger with many limitations. Not recommended.
     */
    @Parameter( defaultValue="true" )
    protected boolean swagger;

    /**
     * If set to true, the generated code will include json resources
     */
    @Parameter( defaultValue="false" )
    protected boolean json;

    /**
     * If not empty, the value will be put in frontend of the generated swagger schema's endpoint
     */
    @Parameter( defaultValue="" )
    protected String swaggerRootPath;

    /**
     * If set to true, the generated code will include handler's implementation
     */
    @Parameter( defaultValue="false" )
    protected boolean handlerImpl;

    /**
     * If set to true, the generators will use resource path for generating resource and handler method names
     */
    @Parameter( defaultValue="false" )
    protected boolean generateMethodNamesUsingPath;

    /**
     * If set to true, the generators will generate exception mapper for handling uncaught exception
     */
    @Parameter( defaultValue="false" )
    protected boolean handleUncaughtException;

    /**
     * If set to true, the generators will generate parsec error objects.
     */
    @Parameter( defaultValue="false" )
    protected boolean generateParsecError;

    /**
     * If not empty, the value will overwrite swagger endpoint scheme
     */
    @Parameter( defaultValue="" )
    protected String swaggerScheme;

    /**
     * The fileUtil.
     */
    private FileUtils fileUtils;

    /**
     * The default constructor.
     */
    public ParsecGenerateMojo() {
        this.fileUtils = new FileUtils(getLog());
    }

    /**
     * Constructor with fileUtil argument for unit testing injection.
     *
     * @param fileUtils fileUtil object
     */
    ParsecGenerateMojo(FileUtils fileUtils) {
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
        File generatedSourceRoot = new File(getGeneratedSourcesPath());

        // Create ${baseDir}/target/generated-sources/java
        fileUtils.checkAndCreateDirectory(generatedSourceRoot);

        // Add ${baseDir}/target/generated-sources/java to compile source root
        project.addCompileSourceRoot(generatedSourceRoot.getPath());

        // Find RDL files to parse / generate
        ArrayList<String> files = new ArrayList<>();
        if ("*.rdl".equals(sourceFiles)) {
            addFiles(files, sourceDirectory);
        } else {
            for (String filename : sourceFiles.split(",")) {
                File file = filename.startsWith("/") ? new File(filename) : new File(sourceDirectory, filename);
                if (file.exists()) {
                    files.add(file.getPath());
                }
            }
        }

        // Leave if no files found
        if (files.size() == 0) {
            getLog().info("0 RDL files found");
            return;
        }

        if (swagger || json) {
            // Create ${baseDir}/target/generated-resources/parsec/doc
            fileUtils.checkAndCreateDirectory(getDocPath());
        }

        if (handleUncaughtException) {
            // thrown error layout of parsec exception mapper depends on generated parsec error object
            generateParsecError = true;
        }

        for (String file : files) {
            getLog().info("");
            getLog().info("Parsing " + file);

            if (swagger) {
                List<String> options = new ArrayList<>();
                options.add("-o");
                options.add(getRelativeDocPath());
                if (!swaggerRootPath.isEmpty()) {
                    options.add("-r");
                    options.add(swaggerRootPath);
                }
                if (generateParsecError) {
                    options.add("-g");
                }
                if (swaggerScheme != null && !swaggerScheme.isEmpty()) {
                    options.add("-s");
                    options.add(swaggerScheme);
                }
                rdlGenerate("swagger", file, options);
            }

            if (json) {
                rdlGenerate("json", file, Arrays.asList("-o", getRelativeDocPath()));
            }

            if (model) {
                rdlGenerate("java-model", file, Arrays.asList("-o", getRelativeGeneratedSourcesDoc()));
            }

            if (server) {
                List<String> options = new ArrayList<>();
                options.add("-o");
                options.add(getRelativeGeneratedSourcesDoc());
                if (handlerImpl) {
                    options.add("-i");
                }
                if (generateMethodNamesUsingPath) {
                    options.add("-p");
                }
                if (generateParsecError) {
                    options.add("-g");
                }
                rdlGenerate("java-server", file, options);
            }
        }

        getLog().info("");

        if (swagger) {
            Set<Path> swaggerJsons = (fileUtils.findFiles(getDocPath(), "regex:^\\w+_swagger\\.json$"));
            if (!swaggerJsons.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder()
                    .append(getOverwriteWarningCommentBlock())
                    .append("var localSwaggerJsons = [")
                    .append(System.getProperty("line.separator"));

                int i = 0;
                for (Path swaggerJson : swaggerJsons) {
                    i++;

                    stringBuilder.append("\t\"")
                        .append(swaggerJson.getFileName())
                        .append("\"");

                    if (i < swaggerJsons.size()) {
                        stringBuilder.append(",");
                    }

                    stringBuilder.append(System.getProperty("line.separator"));
                }

                stringBuilder.append("]");
                fileUtils.writeResourceToFile(
                    new ByteArrayInputStream(stringBuilder.toString().getBytes()),
                    getDocPath() + "/_local-swagger-jsons.js",
                    true
                );
            }
        }

        if (server) {
            // handle handler, resources
            final FileUtils fileUtils = new FileUtils(getLog());
            final ParsecGeneratorUtil generatorUtil = new ParsecGeneratorUtil(fileUtils);
            ParsecFileGenerator parsecFileGenerator = new ParsecFileGenerator(
                generatedSourceRoot.toPath(),
                project,
                fileUtils,
                new ParsecPackageResolver(generatorUtil, fileUtils),
                generatorUtil
            );

            // not support handle uncaught exception under multiple namespaces until now
            if (parsecFileGenerator.hasMultipleNamespaces() && handleUncaughtException) {
                getLog().error("handleUncaughtException not support under multiple namespaces");
                handleUncaughtException = false;
            }

            parsecFileGenerator.generateParsecApplication(handleUncaughtException);
            parsecFileGenerator.generateParsecValidationGroups();
            parsecFileGenerator.generateFromTemplateToSourceRoot("DefaultResourceContext.java");
            parsecFileGenerator.generateFromTemplateToIntersectSourceRoot("DefaultApplication.java");
            parsecFileGenerator.generateFromTemplateToIntersectSourceRoot("DefaultWebListener.java");
            parsecFileGenerator.generateFromTemplateToIntersectGeneratedNamespace("ParsecWrapperServlet.java");
            parsecFileGenerator.generateFromTemplateToIntersectGeneratedNamespace("ParsecWebListener.java");

            if (handleUncaughtException) {
                parsecFileGenerator.generateFromTemplateToIntersectSourceRoot("DefaultExceptionMapper.java");
                parsecFileGenerator.generateFromTemplateToIntersectGeneratedNamespace("ParsecExceptionMapper.java");
            }
        }
    }

    /**
     * Add file.
     *
     * @param files files
     * @param sourcePath source path
     */
    void addFiles(ArrayList<String> files, File sourcePath) {
        if (sourcePath != null && sourcePath.exists()) {
            for (String filename : sourcePath.list()) {
                File file = new File(sourcePath, filename);
                if (file.isDirectory()) {
                    addFiles(files, file);
                } else if (filename.endsWith(".rdl")) {
                    files.add(file.getPath());
                }
            }
        }
    }

    /**
     * RDL generate.
     *
     * @param type type
     * @param sourcePath source path
     * @param options options
     * @throws MojoExecutionException MojoExecutionException
     */
    void rdlGenerate(String type, String sourcePath, List<String> options) throws MojoExecutionException {
        try {
            List<String> command =  new ArrayList<>();
            command.addAll(Arrays.asList("target/bin/parsec_rdl", "generate", type, sourcePath));
            command.addAll(2, options);

            getLog().info("    Generating " + type);
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                .directory(project.getBasedir())
                .inheritIO();

            Process process = processBuilder.start();
            int errorCode = process.waitFor();
            if (errorCode != 0) {
                throw new MojoExecutionException("Error parsing RDL file " + sourcePath);
            }
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

}
