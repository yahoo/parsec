// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle.utils;

import com.yahoo.parsec.gradle.ParsecPluginExtension;
import org.gradle.api.Project;

import java.io.File;

/**
 * @author sho
 */
public class PathUtils {

    /**
     * RDL binary filename.
     */
    public static final String RDL_BINARY = "rdl";

    /**
     * Parsec RDL generators prefix.
     */
    public static final String RDL_GEN_PARSEC_PREFIX = "rdl-gen-parsec-";

    /**
     * Relative bin path.
     */
    private static final String RELATIVE_BIN_PATH = "bin";

    /**
     * Relative doc path.
     */
    private static final String RELATIVE_DOC_PATH = "generated-resources/parsec/doc";

    /**
     * Relative generated resources path.
     */
    private static final String RELATIVE_GENERATED_RESOURCES_PATH = "generated-resources/parsec";

    /**
     * Relative generated sources path.
     */
    private static final String RELATIVE_GENERATED_SOURCES_PATH = "generated-sources/java";

    /**
     * Relative Swagger-ui path.
     */
    private static final String RELATIVE_SWAGGER_UI_PATH = "generated-resources/parsec/swagger-ui";

    /**
     * Relative unit-test root path.
     */
    private static final String RELATIVE_TEST_SOURCES_ROOT_PATH = "src/test/java";

    /**
     * Gradle project instance.
     */
    private Project project;

    /**
     * Parsec plugin extension.
     */
    private ParsecPluginExtension pluginExtension;

    /**
     * Constructor.
     *
     * @param project project
     * @param pluginExtension plugin extension
     */
    public PathUtils(final Project project, final ParsecPluginExtension pluginExtension) {
        this.project = project;
        this.pluginExtension = pluginExtension;
    }

    /**
     * Get relative bin path.
     *
     * @return relative bin path
     */
    public static String getRelativeBinPath() {
        return RELATIVE_BIN_PATH;
    }

    /**
     * Get relative doc path.
     *
     * @return relative doc path
     */
    public static String getRelativeDocPath() {
        return RELATIVE_DOC_PATH;
    }

    /**
     * Get relative generated resources path.
     *
     * @return relative generated sources path
     */
    public static String getRelativeGeneratedResourcesPath() {
        return RELATIVE_GENERATED_RESOURCES_PATH;
    }

    /**
     * Get relative generated sources path.
     *
     * @return relative generated sources path
     */
    public static String getRelativeGeneratedSourcesPath() {
        return RELATIVE_GENERATED_SOURCES_PATH;
    }

    /**
     * Get relative Swagger UI path.
     *
     * @return relative Swagger UI path
     */
    public static String getRelativeSwaggerUIPath() {
        return RELATIVE_SWAGGER_UI_PATH;
    }

    /**
     * Get relative test sources root path.
     * @return relative test sources root path
     */
    public static String getRelativeTestSourcesRootPath() {
        return RELATIVE_TEST_SOURCES_ROOT_PATH;
    }

    /**
     * Get project path.
     *
     * @return project path
     */
    public String getProjectPath() {
        return project.getProjectDir().getPath();
    }

    /**
     * Get project build path.
     *
     * @return project build path
     */
    public String getProjectBuildPath() {
        return project.getBuildDir().getPath();
    }

    /**
     * Get bin path.
     *
     * @return bin path
     */
    public String getBinPath() {
        return getProjectBuildPath() + "/" + getRelativeBinPath();
    }

    /**
     * Get Rdl binary path.
     *
     * @return Rdl binary path
     */
    public String getRdlBinaryPath() {
        return getBinPath() + "/" + RDL_BINARY;
    }

    /**
     * Get doc path.
     *
     * @return doc path
     */
    public String getDocPath() {
        return getProjectBuildPath() + "/" + getRelativeDocPath();
    }

    /**
     * Get generated resources path.
     *
     * @return generated sources path
     */
    public String getGeneratedResourcesPath() {
        return getProjectBuildPath() + "/" + getRelativeGeneratedResourcesPath();
    }

    /**
     * Get generated sources path.
     *
     * @return generated sources path
     */
    public String getGeneratedSourcesPath() {
        return getProjectBuildPath() + "/" + getRelativeGeneratedSourcesPath();
    }

    /**
     * Get generated sources directory.
     *
     * @return generated sources directory
     */
    public File getGeneratedSourcesDir() {
        return new File(getGeneratedSourcesPath());
    }

    /**
     * Get Swagger-ui path.
     *
     * @return script path
     */
    public String getSwaggerUIPath() {
        return getProjectBuildPath() + "/" + getRelativeSwaggerUIPath();
    }

    /**
     * Get Java Test Root Path.
     *
     * @return script path
     */
    public String getJavaTestRootPath() {
        return getProjectPath() + "/" + getRelativeTestSourcesRootPath();
    }

    /**
     * Get source path.
     *
     * @return source path
     */
    public String getSourcePath() {
        String sourcePath = pluginExtension.getSourcePath();

        if (!sourcePath.startsWith("/")) {
            return getProjectPath() + "/" + sourcePath;
        }

        return sourcePath;
    }

    /**
     * Get source directory.
     *
     * @return source directory
     */
    public File getSourceDir() {
        String sourcePath = pluginExtension.getSourcePath();

        if (!sourcePath.startsWith("/")) {
            return new File(getProjectPath() + "/" + sourcePath);
        }

        return new File(sourcePath);
    }
}
