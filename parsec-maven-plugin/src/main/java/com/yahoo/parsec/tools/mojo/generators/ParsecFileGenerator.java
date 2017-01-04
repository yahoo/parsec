package com.yahoo.parsec.tools.mojo.generators;

import com.yahoo.parsec.tools.mojo.utils.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author sho
 */
public class ParsecFileGenerator {
    /**
     * Parsec generated class.
     */
    private static final String PARSEC_GENERATED_NAMESPACE = "parsec_generated";

    /**
     * Java source root.
     */
    private static final String JAVA_SOURCE_ROOT = "/src/main/java/";

    /**
     * Generated source root path.
     */
    private Path generatedSourceRootPath;

    /**
     * Project.
     */
    private MavenProject project;

    /**
     * Utils.
     */
    private FileUtils fileUtils;

    /**
     * the package resolver.
     */
    private ParsecPackageResolver packageResolver;

    /**
     * the package struct.
     */
    private ParsecPackageStruct packageStruct;

    /**
     * the generators utils.
     */
    private ParsecGeneratorUtil generatorUtil;

    /**
     * the parsec application generators.
     */
    private ParsecApplicationGenerator applicationGenerator;

    /**
     * the parsc validation groups generators.
     */
    private ParsecValidationGroupGenerator validationGroupsGenerator;

    /**
     * Constructor without generators parameters.
     *
     * @param generatedSourceRootPath generated source root path
     * @param project maven project
     * @param fileUtils file utils
     * @param packageResolver package resolver
     * @param generatorUtil generators util
     * @throws MojoExecutionException MojoExecutionException
     */
    public ParsecFileGenerator(
        final Path generatedSourceRootPath,
        final MavenProject project,
        final FileUtils fileUtils,
        final ParsecPackageResolver packageResolver,
        final ParsecGeneratorUtil generatorUtil
    ) throws MojoExecutionException {
        init(generatedSourceRootPath, project, fileUtils, packageResolver, generatorUtil);
        this.applicationGenerator = new ParsecApplicationGenerator(packageStruct, generatorUtil);
        this.validationGroupsGenerator = new ParsecValidationGroupGenerator(packageStruct, generatorUtil, fileUtils);
    }

    /**
     * Constructor with generators parameters for testing.
     *
     * @param generatedSourceRootPath generated source root path
     * @param project maven project
     * @param fileUtils file utils
     * @param packageResolver package resolver
     * @param generatorUtil generators util
     * @param applicationGenerator application generator
     * @param validationGroupsGenerator validation group generator
     * @throws MojoExecutionException MojoExecutionException
     */
    public ParsecFileGenerator(
            final Path generatedSourceRootPath,
            final MavenProject project,
            final FileUtils fileUtils,
            final ParsecPackageResolver packageResolver,
            final ParsecGeneratorUtil generatorUtil,
            final ParsecApplicationGenerator applicationGenerator,
            final ParsecValidationGroupGenerator validationGroupsGenerator
    ) throws MojoExecutionException {
        init(generatedSourceRootPath, project, fileUtils, packageResolver, generatorUtil);
        this.applicationGenerator = applicationGenerator;
        this.validationGroupsGenerator = validationGroupsGenerator;
    }

    /**
     * Init Parameters.
     *
     * @param generatedSourceRootPath generated source root path
     * @param project maven project
     * @param fileUtils file utils
     * @param packageResolver package resolver
     * @param generatorUtil generators util
     * @throws MojoExecutionException MojoExecutionException
     */
    private void init(
            final Path generatedSourceRootPath,
            final MavenProject project,
            final FileUtils fileUtils,
            final ParsecPackageResolver packageResolver,
            final ParsecGeneratorUtil generatorUtil
    ) throws MojoExecutionException {
        this.generatedSourceRootPath = generatedSourceRootPath;
        this.project = project;
        this.fileUtils = fileUtils;
        this.packageResolver = packageResolver;
        this.generatorUtil = generatorUtil;
        this.packageStruct = packageResolver.resolve(
                project, JAVA_SOURCE_ROOT, generatedSourceRootPath, PARSEC_GENERATED_NAMESPACE);
    }

    /**
     * has multiple namespace.
     * @return boolean
     */
    public boolean hasMultipleNamespaces() {
        return this.packageStruct.getPackages().size() > 1;
    }

    /**
     * Generate from template to source root.
     *
     * @param templateName template name
     * @throws MojoExecutionException MojoExecutionException
     */
    public void generateFromTemplateToSourceRoot(
            final String templateName) throws MojoExecutionException {
        final Map<String, String> packages = packageStruct.getPackages();
        for (Map.Entry<String, String> packageEntry : packages.entrySet()) {
            String outputDir = ParsecGeneratorUtil.getPathFromSourceRoot(
                    packageStruct, packageEntry.getValue());
            String packageName = packageEntry.getKey();
            generatorUtil.generateFromTemplateTo(templateName, packageName, outputDir, false);
        }
    }

    /**
     * Generate from template to source root.
     *
     * @param templateName template name
     * @throws MojoExecutionException MojoExecutionException
     */
    public void generateFromTemplateToIntersectSourceRoot(
            final String templateName)throws MojoExecutionException {
        String packageName = packageStruct.getIntersectPackageName();
        String outputDir = ParsecGeneratorUtil.getPathFromSourceRoot(
                packageStruct, ParsecGeneratorUtil.packageNameToPath(packageName));
        generatorUtil.generateFromTemplateTo(templateName, packageName, outputDir, false);
    }

    /**
     * Generate from template to generated namespace.
     *
     * @param templateName template name
     * @throws MojoExecutionException MojoExecutionException
     */
    public void generateFromTemplateToIntersectGeneratedNamespace(
            final String templateName) throws MojoExecutionException {
        String packageName = packageStruct.getIntersectPackageName();
        String outputDir = ParsecGeneratorUtil.getPathFromGeneratedRoot(
                packageStruct, ParsecGeneratorUtil.packageNameToPath(packageName));
        generatorUtil.generateFromTemplateTo(templateName, packageName, outputDir, true);
    }

    /**
     * Generate ParsecApplication.java
     *
     * @param handleUncaughtException handle uncaught exception
     *
     * @throws MojoExecutionException MojoExecutionException
     */
    public void generateParsecApplication(boolean handleUncaughtException) throws MojoExecutionException {
        applicationGenerator.generateParsecApplication(handleUncaughtException);
    }

    /**
     * Generate ParsecValidationGroup.java
     *
     * @throws MojoExecutionException MojoExecutionException
     */
    public void generateParsecValidationGroups() throws MojoExecutionException {
        validationGroupsGenerator.generateParsecValidationGroups();
    }

}
