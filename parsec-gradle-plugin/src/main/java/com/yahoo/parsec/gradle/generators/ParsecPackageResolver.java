// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle.generators;

import com.yahoo.parsec.gradle.utils.FileUtils;
import org.gradle.api.Project;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ParsecPackageResolver.
 * to resolve package infos into package struct object.
 */
public class ParsecPackageResolver {

    /**
     * the generatorUtil.
     */
    private ParsecGeneratorUtil generatorUtil;

    /**
     * the fileUtils.
     */
    private FileUtils fileUtils;

    /**
     * default constructor.
     * @param generatorUtil generators util
     * @param fileUtils file utils
     */
    public ParsecPackageResolver(ParsecGeneratorUtil generatorUtil, final FileUtils fileUtils) {
        this.generatorUtil = generatorUtil;
        this.fileUtils = fileUtils;
    }

    /**
     * Resolve packages, handlers, resources and dataobjects.
     *
     * @param project gradle project
     * @param javaSourceRoot java source root
     * @param generatedSourceRootPath generated source root path
     * @param generatedNamespace generated namespace
     * @throws IOException IOException
     *
     * @return PackagePackageStruct
     */
    public ParsecPackageStruct resolve(
        final Project project,
        final String javaSourceRoot,
        final Path generatedSourceRootPath,
        final String generatedNamespace) throws IOException {
        Map<String, String> packages = new HashMap<>();
        Map<String, List<String>> handlers = new HashMap<>();
        Map<String, List<String>> resources = new HashMap<>();
        Map<String, List<Path>> dataObjects = new HashMap<>();
        try {
            List<Path> filePaths = getFilePathsFromGeneratedSourceRoot(generatedSourceRootPath);
            for (Path filePath : filePaths) {
                resolveByFile(
                        filePath, generatedNamespace, generatedSourceRootPath,
                        packages, handlers, resources, dataObjects
                );
            }
        } catch (IOException e) {
            throw e;
        }

        return new ParsecPackageStruct(
                project, javaSourceRoot, generatedSourceRootPath, generatedNamespace,
                packages, handlers, resources, dataObjects,
                generatorUtil.getIntersectPackageName(new ArrayList<>(packages.keySet()))
        );
    }

    /**
     * Get filePaths from generated source root.
     *
     * @param generatedSourceRootPath generated source root path
     * @throws IOException IOException
     *
     * @return paths
     */
    List<Path> getFilePathsFromGeneratedSourceRoot(
            final Path generatedSourceRootPath) throws IOException {
        final List<Path> paths = new ArrayList<>();
        Files.walkFileTree(generatedSourceRootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                paths.add(filePath);
                return FileVisitResult.CONTINUE;
            }
        });

        return paths;
    }

    /**
     * resolve by single file, extract as a function for testing.
     *
     * @param filePath file path
     * @param generatedNamespace generated namespace
     * @param generatedSourceRootPath generated source root path
     * @param packages packages
     * @param handlers handlers
     * @param resources resources
     * @param dataObjects data objects
     * @throws IOException
     */
    void resolveByFile(
            final Path filePath,
            final String generatedNamespace,
            final Path generatedSourceRootPath,
            final Map<String, String> packages,
            final Map<String, List<String>> handlers,
            final Map<String, List<String>> resources,
            final Map<String, List<Path>> dataObjects
    ) throws IOException {
        String pathString = filePath.toString();
        if (pathString.contains("/" + generatedNamespace + "/")) {
            String filename = filePath.getFileName().toString();
            String packagePath = pathString
                    .replace(generatedSourceRootPath + "/", "")
                    .replace("/" + generatedNamespace, "")
                    .replace("/" + filename, "");
            String packageName = packagePath.replace("/", ".");

            filename = filename.replace(".java", "");
            if (isHandler(filename)) {
                addValue(handlers, packageName, filename);
            } else if (isResource(filename)) {
                addValue(resources, packageName, filename);
                // only keep the last package name of same path
                packages.putIfAbsent(packageName, packagePath);
            } else if (isDataObject(filePath)) {
                addValue(dataObjects, packageName, filePath);
            }
        }
    }

    /**
     * Add value.
     *
     * @param map map
     * @param key key
     * @param value value
     */
    private static <T> void addValue(final Map<String, List<T>> map, String key, T value) {
        List<T> list;
        if (map.containsKey(key)) {
            list = map.get(key);
        } else {
            list = new ArrayList<>();
        }
        list.add(value);
        map.put(key, list);
    }

    /**
     * check if the file is generated data object.
     *
     * @param filePath file path
     * @return boolean
     * @throws IOException
     */
    boolean isDataObject(final Path filePath) throws IOException {
        // the match pattern is not very accurate if we generate other classes with Serializable in the future,
        //  but there's no risk if the matched object is not a real data object
        final String pattern = " implements java.io.Serializable";
        return fileUtils.checkFileContains(filePath, pattern);
    }

    /**
     * check if the file is generated handler object.
     *
     * @param filename file name
     * @return boolean
     */
    boolean isHandler(final String filename) {
        return filename.endsWith("Handler");
    }

    /**
     * check if the file is generated resources object.
     *
     * @param filename file name
     * @return boolean
     */
    boolean isResource(final String filename) {
        return filename.endsWith("Resources");
    }
}
