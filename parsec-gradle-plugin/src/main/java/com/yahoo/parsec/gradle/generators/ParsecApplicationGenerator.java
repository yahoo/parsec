// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ParsecApplication Generator.
 */
public class ParsecApplicationGenerator {

    /**
     * the package struct.
     */
    private ParsecPackageStruct packageStruct;

    /**
     * the generator util.
     */
    private ParsecGeneratorUtil generatorUtil;

    /**
     * default constructor.
     *
     * @param packageStruct package struct.
     * @param generatorUtil generators util
     */
    public ParsecApplicationGenerator(
            final ParsecPackageStruct packageStruct, final ParsecGeneratorUtil generatorUtil) {
        this.packageStruct = packageStruct;
        this.generatorUtil = generatorUtil;
    }

    /**
     * Parsec generate materials for ParsecApplication.
     *
     * @param handleUncaughtException handle uncaught exception
     *
     * @return replace materials
     * @throws IOException IOException
     */
    Map<String, String> getReplaceMaterials(boolean handleUncaughtException) throws IOException {
        final String generatedNamespace = packageStruct.getGeneratedNamespace();
        final Map<String, List<String>> handlers = packageStruct.getHandlers();
        final Map<String, String> packages = packageStruct.getPackages();
        final Map<String, List<String>> resources = packageStruct.getResources();

        StringBuilder bindStringBuilder = new StringBuilder();
        StringBuilder registerStringBuilder = new StringBuilder();
        StringBuilder importStringBuilder = new StringBuilder();

        for (Map.Entry<String, String> packageEntry : packages.entrySet()) {
            List<String> imports = new ArrayList<>();
            String packageName = packageEntry.getKey();
            bindStringBuilder.append(
                    generateBindAndImpl(imports, handlers, packageName, generatedNamespace));
            registerStringBuilder.append(
                    generateRegister(imports, resources, packageName, generatedNamespace, handleUncaughtException));
            importStringBuilder.append(
                    generateImport(imports));
        }

        final Map<String, String> replaceMaterials = new HashMap<>();
        replaceMaterials.put("{imports}", importStringBuilder.toString());
        replaceMaterials.put("{binding}", bindStringBuilder.toString());
        replaceMaterials.put("{register}", registerStringBuilder.toString());

        return replaceMaterials;
    }

    /**
     * generate bind and impl.
     * @param collectImports imports to be filled
     * @param handlers handlers
     * @param packageName package name
     * @param generatedNamespace generated namespace
     * @return bind and impl appended string
     */
    String generateBindAndImpl(
            final List<String> collectImports,
            final Map<String, List<String>> handlers,
            final String packageName,
            final String generatedNamespace) {
        StringBuilder bindStringBuilder = new StringBuilder();
        if (handlers.containsKey(packageName)) {
            for (String handler : handlers.get(packageName)) {
                collectImports.add(packageName + "." + generatedNamespace + "." + handler);
                collectImports.add(packageName + "." + handler + "Impl");
                bindStringBuilder
                        .append("                bind(")
                        .append(handler)
                        .append("Impl.class).to(")
                        .append(handler)
                        .append(".class);")
                        .append(System.getProperty("line.separator"));
            }
        }

        return bindStringBuilder.toString();
    }

    /**
     * generate register.
     * @param collectImports imports to be filled
     * @param resources resources
     * @param packageName package name
     * @param generatedNamespace generated namespace
     * @return register appended string
     */
    String generateRegister(
            final List<String> collectImports,
            final Map<String, List<String>> resources,
            final String packageName,
            final String generatedNamespace,
            final boolean handleUncaughtException) {
        StringBuilder registerStringBuilder = new StringBuilder();
        if (resources.containsKey(packageName)) {
            for (String resource : resources.get(packageName)) {
                collectImports.add(packageName + "." + generatedNamespace + "." + resource);
                registerStringBuilder
                        .append("        register(")
                        .append(resource)
                        .append(".class);")
                        .append(System.getProperty("line.separator"));
            }
        }

        if (handleUncaughtException) {
            String exceptionMapper = "DefaultExceptionMapper";
            collectImports.add(packageName + "." + exceptionMapper);
            registerStringBuilder
                    .append("        register(")
                    .append(exceptionMapper)
                    .append(".class);")
                    .append(System.getProperty("line.separator"));
        }

        return registerStringBuilder.toString();
    }

    /**
     * generate import.
     * @param imports imports
     * @return imports appended string
     */
    String generateImport(final List<String> imports) {
        StringBuilder importStringBuilder = new StringBuilder();
        if (!imports.isEmpty()) {
            for (String importClass : imports) {
                importStringBuilder
                        .append("import ")
                        .append(importClass)
                        .append(";")
                        .append(System.getProperty("line.separator"));
            }
        }
        return importStringBuilder.toString();
    }

    /**
     * Generate ParsecApplication.java
     *
     * @param handleUncaughtException handle uncaught exception
     *
     * @throws IOException IOException
     */
    public void generateParsecApplication(boolean handleUncaughtException) throws IOException {
        Map<String, String> replaceMaterials = getReplaceMaterials(handleUncaughtException);
        String packageName = packageStruct.getIntersectPackageName();
        String outputDir = ParsecGeneratorUtil.getPathFromGeneratedRoot(
                packageStruct, ParsecGeneratorUtil.packageNameToPath(packageName));
        generatorUtil.generateFromTemplateTo(
                "ParsecApplication.java", packageName, outputDir, replaceMaterials, true);
    }
}
