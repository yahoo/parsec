// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle;

import com.yahoo.parsec.gradle.utils.PathUtils;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author sho
 */
public class ParsecInitTask extends AbstractParsecGradleTask {

    public ParsecInitTask(){
        super("parsec-init", "Parsec: initialize the process for parsec-generate");
    }

    /**
     * Execute.
     *
     * @throws TaskExecutionException TaskExecutionException
     */
    @TaskAction
    public void executeTask() throws TaskExecutionException {

        try {
            // Create ${buildDir}/bin
            fileUtils.checkAndCreateDirectory(pathUtils.getBinPath());
            String rdlBinSuffix = System.getProperty("os.name").equals("Mac OS X") ? "darwin" : "linux";

            // Extract rdl to ${buildDir}/bin
            File file = fileUtils.getFileFromResource("/rdl-bin/rdl-bin.zip");
            try (
                ZipFile zipFile = new ZipFile(file);
                InputStream inputStream = zipFile.getInputStream(
                    zipFile.getEntry(PathUtils.RDL_BINARY + "-" + rdlBinSuffix)
                )
            ) {
                fileUtils.writeResourceAsExecutable(inputStream, pathUtils.getRdlBinaryPath());
            }

            // Extract Parsec rdl generators to ${buildDir}/bin
            extractParsecRdlGenerator(rdlBinSuffix, "java-model");
            extractParsecRdlGenerator(rdlBinSuffix, "java-server");
            extractParsecRdlGenerator(rdlBinSuffix, "swagger");

            // Create ${baseDir}/parsec-bin
            fileUtils.checkAndCreateDirectory(pathUtils.getBinPath());

            // Copy all scripts under resource/scripts to ${baseDir}/parsec-bin
            for (Path scriptPath : fileUtils.listDirFilePaths("scripts")) {
                String scriptPathString = scriptPath.toString();
                if (scriptPathString.endsWith(".sh") || scriptPathString.endsWith(".rb")) {
                    fileUtils.writeResourceAsExecutable(
                        scriptPath.toString(),
                        pathUtils.getBinPath() + "/" + scriptPath.getFileName()
                    );
                }
            }

            if (pluginExtension.isGenerateSwagger()) {
                String swaggerUIPath = pathUtils.getSwaggerUIPath();

                // Create ${buildDir}/generated-resources/swagger-ui
                fileUtils.checkAndCreateDirectory(swaggerUIPath);

                // Extract swagger-ui archive if ${buildDir}/generated-resources/swagger-ui is empty
                if (new File(swaggerUIPath).list().length <= 0) {
                    fileUtils.unTarZip("/swagger-ui/swagger-ui.tgz", swaggerUIPath, true);
                }
            }
        } catch (IOException e) {
            throw new TaskExecutionException(this, e);
        }
    }

    /**
     * Extract Parsec Rdl Generator.
     *
     * @param rdlBinSuffix rdl bin suffix
     * @param generator generator
     * @throws IOException IOException
     */
    void extractParsecRdlGenerator(final String rdlBinSuffix, final String generator) throws IOException {
        final File file = fileUtils.getFileFromResource("/rdl-gen/rdl-gen.zip");
        final String generatorBinary = PathUtils.RDL_GEN_PARSEC_PREFIX + generator;

        try (
            ZipFile zipFile = new ZipFile(file);
            InputStream inputStream = zipFile.getInputStream(
                zipFile.getEntry(generatorBinary + "-" + rdlBinSuffix)
            )
        ) {
            fileUtils.writeResourceAsExecutable(inputStream, pathUtils.getBinPath() + "/" + generatorBinary);
        }
    }
}
