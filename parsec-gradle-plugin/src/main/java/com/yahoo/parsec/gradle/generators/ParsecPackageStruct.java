// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle.generators;

import org.gradle.api.Project;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ParsecPackageStruct.
 * to maintain package infos.
 */
public final class ParsecPackageStruct {

    /**
     * Packages.
     */
    private Map<String, String> packages = new HashMap<>();

    /**
     * Package's handlers.
     */
    private Map<String, List<String>> handlers = new HashMap<>();

    /**
     * Package's resources.
     */
    private Map<String, List<String>> resources = new HashMap<>();

    /**
     * Package's data objects.
     */
    private Map<String, List<Path>> dataobjects = new HashMap<>();

    /**
     * Packages intersect package name.
     */
    private String intersectPackageName;

    /**
     * Packages's project.
     */
    private Project project;

    /**
     * Packages's java source root.
     */
    private String javaSourceRoot;

    /**
     * Packages's generated root.
     */
    private Path generatedSourceRootPath;

    /**
     * Packages's generated namespace.
     */
    private String generatedNamespace;

    /**
     * the default constructor.
     * @param project project
     * @param javaSourceRoot java source root
     * @param generatedSourceRootPath generated source root
     * @param generatedNamespace generated namespace
     * @param packages pavkages
     * @param handlers handlers
     * @param resources resources
     * @param dataobjects dataobjects
     * @param intersectPackageName intersect package name
     */
    public ParsecPackageStruct(
            final Project project,
            final String javaSourceRoot,
            final Path generatedSourceRootPath,
            final String generatedNamespace,
            final Map<String, String> packages,
            final Map<String, List<String>> handlers,
            final Map<String, List<String>> resources,
            final Map<String, List<Path>> dataobjects,
            final String intersectPackageName
    ) {
        this.project = project;
        this.javaSourceRoot = javaSourceRoot;
        this.generatedSourceRootPath = generatedSourceRootPath;
        this.generatedNamespace = generatedNamespace;
        this.packages = packages;
        this.handlers = handlers;
        this.resources = resources;
        this.dataobjects = dataobjects;
        this.intersectPackageName = intersectPackageName;
    }

    /**
     * the packages getter.
     * @return packages
     */
    public Map<String, String> getPackages() { return packages; }

    /**
     * the handlers getter.
     * @return handlers
     */
    public Map<String, List<String>> getHandlers() { return handlers; }

    /**
     * the resources getter.
     * @return resources
     */
    public Map<String, List<String>> getResources() { return resources; }

    /**
     * the dataobjects getter.
     * @return dataobjects
     */
    public Map<String, List<Path>> getDataobjects() { return dataobjects; }

    /**
     * the intersectPackageName getter.
     * @return intersectPackageName
     */
    public String getIntersectPackageName() { return intersectPackageName; }

    /**
     * the project getter.
     * @return project
     */
    public Project getProject() { return project; }

    /**
     * the java source root getter.
     * @return source root
     */
    public String getJavaSourceRoot() { return javaSourceRoot; }

    /**
     * the generated root getter.
     * @return generate root
     */
    public Path getGeneratedSourceRootPath() { return generatedSourceRootPath; }

    /**
     * the generated namespace getter.
     * @return generated namespace
     */
    public String getGeneratedNamespace() { return generatedNamespace; }
}