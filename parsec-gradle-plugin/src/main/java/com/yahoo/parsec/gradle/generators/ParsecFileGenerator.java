// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle.generators;

import com.yahoo.parsec.gradle.utils.FileUtils;
import org.gradle.api.Project;

import java.io.IOException;
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

    private Project project;

    /**
     * Constructor without generators parameters.
     *
     * @param generatedSourceRootPath generated source root path
     * @param project maven project
     * @param fileUtils file utils
     * @param packageResolver package resolver
     * @param generatorUtil generators util
     * @throws  IOException IOException
     */
    public ParsecFileGenerator(
        final Path generatedSourceRootPath,
        final Project project,
        final FileUtils fileUtils,
        final ParsecPackageResolver packageResolver,
        final ParsecGeneratorUtil generatorUtil
    ) throws IOException {
        init(generatedSourceRootPath, project, packageResolver, generatorUtil);
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
     * @throws IOException IOException
     */
    public ParsecFileGenerator(
            final Path generatedSourceRootPath,
            final Project project,
            final FileUtils fileUtils,
            final ParsecPackageResolver packageResolver,
            final ParsecGeneratorUtil generatorUtil,
            final ParsecApplicationGenerator applicationGenerator,
            final ParsecValidationGroupGenerator validationGroupsGenerator
    ) throws IOException {
        init(generatedSourceRootPath, project, packageResolver, generatorUtil);
        this.applicationGenerator = applicationGenerator;
        this.validationGroupsGenerator = validationGroupsGenerator;
    }

    /**
     * Init Parameters.
     *
     * @param generatedSourceRootPath generated source root path
     * @param project maven project
     * @param packageResolver package resolver
     * @param generatorUtil generators util
     * @throws IOException IOException
     */
    private void init(
            final Path generatedSourceRootPath,
            final Project project,
            final ParsecPackageResolver packageResolver,
            final ParsecGeneratorUtil generatorUtil
    ) throws IOException {
        this.project = project;
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
     * @throws IOException IOException
     */
    public void generateFromTemplateToSourceRoot(
            final String templateName) throws IOException {
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
     * @throws IOException IOException
     */
    public void generateFromTemplateToIntersectSourceRoot(
            final String templateName)throws IOException {
        String packageName = packageStruct.getIntersectPackageName();
        String outputDir = ParsecGeneratorUtil.getPathFromSourceRoot(
                packageStruct, ParsecGeneratorUtil.packageNameToPath(packageName));
        generatorUtil.generateFromTemplateTo(templateName, packageName, outputDir, false);
    }

    /**
     * Generate from template to generated namespace.
     *
     * @param templateName template name
     * @throws IOException MojoExecutionException
     */
    public void generateFromTemplateToIntersectGeneratedNamespace(
            final String templateName) throws IOException {
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
     * @throws IOException IOException
     */
    public void generateParsecApplication(boolean handleUncaughtException) throws IOException {
        applicationGenerator.generateParsecApplication(handleUncaughtException);
    }

    /**
     * Generate ParsecValidationGroup.java
     *
     * @throws IOException IOException
     */
    public void generateParsecValidationGroups() throws IOException {
        validationGroupsGenerator.generateParsecValidationGroups();
    }

}
