// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle;

import com.yahoo.parsec.gradle.utils.PathUtils;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

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

            // Extract rdl parser to ${buildDir}/bin
            extractRdlBinary(rdlBinSuffix);

            // Extract generator
            extractParsecRdlGenerator(rdlBinSuffix,
                    Arrays.asList("java-model", "java-server", "java-client", "swagger"));

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
            String test = pathUtils.getBinPath();
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

    void extractRdlBinary(final String rdlBinSuffix) throws IOException {
        if (rdlBinSuffix.equals("darwin")) {
            File file = fileUtils.getFileFromResource("/rdl-bin/rdl.zip");
            ZipFile zipFile = new ZipFile(file);
            InputStream inputStream = zipFile.getInputStream(zipFile.getEntry(PathUtils.RDL_BINARY));
            fileUtils.writeResourceAsExecutable(inputStream, pathUtils.getRdlBinaryPath());
        } else {
            // the tgz file for linux
            fileUtils.unTarZip("/rdl-bin/rdl.tgz", pathUtils.getBinPath(), true);
            InputStream rdlStream = new FileInputStream(pathUtils.getRdlBinaryPath());
            if (rdlStream != null) {
                fileUtils.writeResourceAsExecutable(rdlStream, pathUtils.getRdlBinaryPath());
            }
        }
    }

    /**
     * Extract Parsec Rdl Generator.
     *
     * @param rdlBinSuffix rdl bin suffix
     * @param generators the generator list
     * @throws IOException IOException
     */
    void extractParsecRdlGenerator(final String rdlBinSuffix, final List<String> generators) throws IOException {
        final File file = fileUtils.getFileFromResource("/rdl-gen/rdl-gen.zip");

        for (String g: generators) {
            String generatorBinary = PathUtils.RDL_GEN_PARSEC_PREFIX + g;
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
}
