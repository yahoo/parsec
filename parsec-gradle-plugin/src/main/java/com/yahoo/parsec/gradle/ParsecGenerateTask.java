// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle;

import com.yahoo.parsec.gradle.generators.ParsecFileGenerator;
import com.yahoo.parsec.gradle.generators.ParsecGeneratorUtil;
import com.yahoo.parsec.gradle.generators.ParsecPackageResolver;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.testng.Assert;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * @author sho
 */
public class ParsecGenerateTask extends AbstractParsecGradleTask {


    public ParsecGenerateTask(){
        super("parsec-generate", "Parsec: parse through the RDLs and generate necessary files");
    }

    /**
     * Parsec generate.
     *
     * @throws TaskExecutionException TaskExecutionException
     */
    @TaskAction
    public void executeTask() throws TaskExecutionException {

        try {
            // Create ${baseDir}/target/generated-sources/java
            fileUtils.checkAndCreateDirectory(pathUtils.getGeneratedSourcesDir());

            // Find RDL files to parse / generate
            ArrayList<String> files = new ArrayList<>(); //RDL files
            if ("*.rdl".equals(pluginExtension.getSourceFiles())) {
                addFiles(files, pathUtils.getSourceDir()); //sourceDir = "src/main/rdl"
            } else {
                for (String filename : pluginExtension.getSourceFiles().split(",")) {
                    File file = filename.startsWith("/") ?
                        new File(filename) : new File(pathUtils.getSourcePath(), filename);
                    if (file.exists()) {
                        files.add(file.getPath());
                    }
                }
            }

            getLogger().info("RDL files: " + files.size());
            getLogger().error("RDL file list: " + files);

            // Leave if no files found
            if (files.size() == 0) {
                getLogger().info("0 RDL files found");
                return;
            }

            if (pluginExtension.isGenerateSwagger() || pluginExtension.isGenerateJson()) {
                // Create ${buildDir}/generated-resources/parsec/doc
                fileUtils.checkAndCreateDirectory(pathUtils.getDocPath());
            }

            if (pluginExtension.isHandleUncaughtExceptions()) {
                // thrown error layout of parsec exception mapper depends on generated parsec error object
                pluginExtension.setGenerateParsecError(true);
            }

            for (String file : files) {
                getLogger().info("");
                getLogger().info("Parsing " + file);

                if (pluginExtension.isGenerateClient()) {
                    List<String> options = new ArrayList<>();
                    options.add("-o");
                    options.add(pathUtils.getGeneratedSourcesPath());
                    if (pluginExtension.isGenerateModelClassNamePcSuffix()) {
                        options.add("-xpc=true");
                    }
                    rdlGenerate(
                            pathUtils.getRdlBinaryPath(),
                            "parsec-java-client",
                            file,
                            options
                    );
                }

                if (pluginExtension.isGenerateSwagger()) {
                    List<String> options = new ArrayList<>();
                    options.add("-o");
                    options.add(pathUtils.getDocPath());
                    if (pluginExtension.isGenerateParsecError()) {
                        options.add("-xe=true");
                    }
                    if (pluginExtension != null && !pluginExtension.getSwaggerSchema().isEmpty()) {
                        options.add("-xc=" + pluginExtension.getSwaggerSchema());
                    }
                    if (pluginExtension.getFinalName() != null && !pluginExtension.getFinalName().isEmpty()) {
                        options.add("-xf=" + pluginExtension.getFinalName());
                    }
                    rdlGenerate(pathUtils.getRdlBinaryPath(), "parsec-swagger", file, options);
                }

                if (pluginExtension.isGenerateJson()) {
                    rdlGenerate(
                        pathUtils.getRdlBinaryPath(),
                        "json",
                        file,
                        Arrays.asList("-o", pathUtils.getDocPath())
                    );
                }

                if (pluginExtension.isGenerateModel()) {
                    List<String> options = new ArrayList<>();
                    options.add("-o");
                    options.add(pathUtils.getGeneratedSourcesPath());
                    if (pluginExtension.isGenerateModelClassNamePcSuffix()) {
                        options.add("-xpc=true");
                    }
                    if (pluginExtension.isAccessorNamingStyle()) {
                        options.add("-xnamingStyle=" + pluginExtension.getAccessorNamingStyle());
                    }
                    rdlGenerate(
                        pathUtils.getRdlBinaryPath(),
                        "parsec-java-model",
                        file,
                        options
                    );
                }

                if (pluginExtension.isGenerateServer()) {
                    List<String> options = new ArrayList<>();
                    options.add("-o");
                    options.add(pathUtils.getGeneratedSourcesPath());
                    if (pluginExtension.isGenerateHandlerImpl()) {
                        options.add("-xi=true");
                    } else {
                        options.add("-xi=false");
                    }
                    if (pluginExtension.isUseSmartMethodNames()) {
                        options.add("-xp=true");
                    } else {
                        options.add("-xp=false");
                    }
                    if (pluginExtension.isGenerateParsecError()) {
                        options.add("-xe=true");
                    } else {
                        options.add("-xe=false");
                    }
                    if (pluginExtension.isGenerateModelClassNamePcSuffix()) {
                        options.add("-xpc=true");
                    }
                    rdlGenerate(
                        pathUtils.getRdlBinaryPath(),
                        "parsec-java-server",
                        file,
                        options
                    );
                }
            }

            getLogger().info("");

            if (pluginExtension.isGenerateSwagger()) {
                Set<Path> swaggerJsons = fileUtils.findFiles(pathUtils.getDocPath(), "regex:^\\w+_swagger\\.json$");
                if (!swaggerJsons.isEmpty()) {
                    boolean needCopySwaggerJson = false;
                    if (pluginExtension.getAdditionSwaggerJsonPath() != null
                            && !pluginExtension.getAdditionSwaggerJsonPath().isEmpty()) {
                        needCopySwaggerJson = true;
                    }
                    if (needCopySwaggerJson) {
                        File f = new File(pluginExtension.getAdditionSwaggerJsonPath());
                        fileUtils.checkAndCreateDirectory(f.getAbsolutePath());
                    }
                    StringBuilder stringBuilder = new StringBuilder()
                        .append(getOverwriteWarningCommentBlock())
                        .append("var localSwaggerJsons = [")
                        .append(System.getProperty("line.separator"));
                    List<Path> sortedSwaggerJsons = new ArrayList<>(swaggerJsons);
                    Collections.sort(sortedSwaggerJsons, new Comparator<Path>() {
                        @Override
                        public int compare(Path o1, Path o2) {
                            return o1.getFileName().toString().compareTo(o2.getFileName().toString());
                        }
                    });
                    int i = 0;
                    for (Path swaggerJson : sortedSwaggerJsons) {
                        i++;

                        stringBuilder.append("\t\"")
                            .append(swaggerJson.getFileName())
                            .append("\"");

                        if (i < swaggerJsons.size()) {
                            stringBuilder.append(",");
                        }

                        stringBuilder.append(System.getProperty("line.separator"));
                        if (needCopySwaggerJson) {
                            try {
                                Files.copy(
                                    swaggerJson,
                                    new File(pluginExtension.getAdditionSwaggerJsonPath()
                                            + File.separator + swaggerJson.getFileName()).toPath(),
                                    StandardCopyOption.REPLACE_EXISTING
                                );
                            } catch (IOException e) {
                                getLogger().warn("Copy File Error: " + swaggerJson.toString()
                                        + " => " + pluginExtension.getAdditionSwaggerJsonPath());
                            }
                        }
                    }

                    stringBuilder.append("]");
                    fileUtils.writeResourceToFile(
                        new ByteArrayInputStream(stringBuilder.toString().getBytes()),
                        pathUtils.getDocPath() + "/_local-swagger-jsons.js",
                        true
                    );
                    if (needCopySwaggerJson) {
                        try {
                            Files.copy(
                                    new File(pathUtils.getDocPath() + "/_local-swagger-jsons.js").toPath(),
                                    new File(pluginExtension.getAdditionSwaggerJsonPath()
                                            + "/_local-swagger-jsons.js").toPath(),
                                    StandardCopyOption.REPLACE_EXISTING
                            );
                        } catch (IOException e) {
                            getLogger().warn("Copy File Error: _local-swagger-jsons.js => "
                                    + pluginExtension.getAdditionSwaggerJsonPath());
                        }
                    }
                }
            }

            if (pluginExtension.isGenerateServer()) {
                // handle handler, resources
                final ParsecGeneratorUtil generatorUtil = new ParsecGeneratorUtil(fileUtils);
                ParsecFileGenerator parsecFileGenerator = new ParsecFileGenerator(
                    pathUtils.getGeneratedSourcesDir().toPath(),
                    getProject(),
                    fileUtils,
                    new ParsecPackageResolver(generatorUtil, fileUtils),
                    generatorUtil
                );

                // not support handle uncaught exception under multiple namespaces until now
                if (parsecFileGenerator.hasMultipleNamespaces() && pluginExtension.isHandleUncaughtExceptions()) {
                    getLogger().error("handleUncaughtException not support under multiple namespaces");
                    pluginExtension.setHandleUncaughtExceptions(false);
                }

                parsecFileGenerator.generateParsecApplication(pluginExtension.isHandleUncaughtExceptions());
                parsecFileGenerator.generateParsecValidationGroups();
                parsecFileGenerator.generateFromTemplateToSourceRoot("DefaultResourceContext.java");
                parsecFileGenerator.generateFromTemplateToIntersectSourceRoot("DefaultApplication.java");
                parsecFileGenerator.generateFromTemplateToIntersectSourceRoot("DefaultWebListener.java");
                parsecFileGenerator.generateFromTemplateToIntersectGeneratedNamespace("ParsecWrapperServlet.java");
                parsecFileGenerator.generateFromTemplateToIntersectGeneratedNamespace("ParsecWebListener.java");

                if (pluginExtension.isHandleUncaughtExceptions()) {
                    parsecFileGenerator.generateFromTemplateToIntersectSourceRoot("DefaultExceptionMapper.java");
                    parsecFileGenerator.generateFromTemplateToIntersectGeneratedNamespace("ParsecExceptionMapper.java");
                }
            }

        } catch (IOException e) {
            throw new TaskExecutionException(this, e);
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
     * @throws TaskExecutionException TaskExecutionException
     */
    void rdlGenerate(
        final String executable,
        String type,
        String sourcePath,
        List<String> options
    ) throws TaskExecutionException {
        try {
            File f = new File(executable);
            List<String> command =  new ArrayList<>();
            command.addAll(Arrays.asList(executable, "generate", type, sourcePath));
            command.addAll(2, options);

            getLogger().info(" Generating " + type);
            getLogger().debug("execute command: {}", command);
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                .directory(getProject().getProjectDir())
                .inheritIO();

            // Add execute PATH
            Map<String, String> env = processBuilder.environment();
            env.put("PATH", pathUtils.getBinPath());
            Process process = processBuilder.start();

            int errorCode = process.waitFor();
            if (errorCode != 0) {
                throw new IOException("Error parsing RDL file " + sourcePath);
            }
        } catch (IOException | InterruptedException e) {
            throw new TaskExecutionException(this, e);
        }
    }

    /**
     * Get overwrite warning comment block.
     *
     * @return overwrite warning comment block
     */
    public static String getOverwriteWarningCommentBlock() {
        StringBuilder stringBuilder = new StringBuilder()
            .append("/**")
            .append(System.getProperty("line.separator"))
            .append(" * This file is generated by Parsec Gradle Plugin.")
            .append(System.getProperty("line.separator"))
            .append(" * Please DO NOT edit directly; changes could be overwritten.")
            .append(System.getProperty("line.separator"))
            .append(" */")
            .append(System.getProperty("line.separator"));

        return stringBuilder.toString();
    }
}
