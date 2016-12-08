// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.gradle.api.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * @author sho
 */
public class FileUtils {

    private Logger logger;

    public FileUtils(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Check and create directory.
     *
     * @param dirPath directory path
     * @throws IOException IOException
     */
    public void checkAndCreateDirectory(String dirPath) throws IOException {
        checkAndCreateDirectory(new File(dirPath));
    }

    /**
     * Check and create directory.
     *
     * @param dir directory file
     * @throws IOException IOException
     */
    public void checkAndCreateDirectory(File dir) throws IOException {
        String dirPath = dir.getPath();

        if (!dir.exists()) {
            logger.info("Creating directory " + dirPath);
            dir.mkdirs();
        }

        if (!dir.isDirectory()) {
            throw new IOException("Cannot create directory " + dirPath);
        }
    }

    /**
     * Write resource to file.
     *
     * @param resourcePath file path in resources
     * @param outputFile   output file
     * @param overwrite    overwrite flag
     */
    public void writeResourceToFile(String resourcePath, String outputFile, boolean overwrite) {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            writeResourceToFile(inputStream, outputFile, overwrite);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Write resource to file.
     *
     * @param inputStream input stream
     * @param outputFile  output file
     * @param overwrite   overwrite flag
     * @throws IOException IOException
     */
    public void writeResourceToFile(final InputStream inputStream, String outputFile, boolean overwrite) throws IOException {
        File file = new File(outputFile);
        String outputFileDigest = "";

        if (file.exists()) {
            if (!overwrite) {
                logger.info("Skipping pre-existing " + outputFile);
                return;
            } else {
                try (InputStream outputFileStream = new FileInputStream(file)) {
                    outputFileDigest = DigestUtils.md5Hex(outputFileStream);
                } catch (IOException e) {
                    throw e;
                }
            }
        }

        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            if (DigestUtils.md5Hex(bytes).equals(outputFileDigest)) {
                logger.info("Skipping unmodified " + outputFile);
                return;
            } else {
                logger.info("Creating file " + outputFile);
                Files.copy(new ByteArrayInputStream(bytes), Paths.get(outputFile), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Write resource as executable.
     *
     * @param resourcePath resource path
     * @param outputFile   output file
     * @throws IOException IOException
     */
    public void writeResourceAsExecutable(String resourcePath, String outputFile) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            writeResourceAsExecutable(inputStream, outputFile);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Write resource as executable.
     *
     * @param inputStream input stream
     * @param outputFile  output file
     * @throws IOException IOException
     */
    public void writeResourceAsExecutable(InputStream inputStream, String outputFile) throws IOException{
        writeResourceToFile(inputStream, outputFile, true);
        File file = new File(outputFile);
        file.setExecutable(true);
    }

    /**
     * List dir file paths.
     *
     * @param directory directory
     * @return List of file paths
     * @throws IOException IOException
     */
    public List<Path> listDirFilePaths(String directory) throws IOException {
        CodeSource src = getClass().getProtectionDomain().getCodeSource();
        List<Path> paths = new ArrayList<>();

        try {
            if (src != null) {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                ZipEntry zipEntry;

                while ((zipEntry = zip.getNextEntry()) != null) {
                    String entryName = zipEntry.getName();
                    if (entryName.startsWith(directory + "/")) {
                        paths.add(Paths.get("/" + entryName));
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        }

        return paths;
    }

    /**
     * Get file from resource.
     *
     * @param resourcePath resource path
     * @return file
     * @throws IOException IOException
     */
    public File getFileFromResource(String resourcePath) throws IOException {
        File file;

        try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IOException("Get resource failed: " + resourcePath);
            }

            file = File.createTempFile("temp", ".tmp");
            OutputStream out = new FileOutputStream(file);
            int read;
            byte[] bytes = new byte[1024];

            while ((read = input.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            file.deleteOnExit();
        }

        return file;
    }

    /**
     * Un-TarZip a tgz file.
     *
     * @param resourcePath resource path
     * @param outputPath   output path
     * @param overwrite    overwrite flag
     * @throws IOException IOException
     */
    public void unTarZip(String resourcePath, String outputPath, boolean overwrite) throws IOException {
        try (
            InputStream inputStream = getClass().getResourceAsStream(resourcePath);
            GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(inputStream);
            TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream);
        ) {
            TarArchiveEntry tarArchiveEntry;
            logger.info("Extracting tgz file to " + outputPath);

            while ((tarArchiveEntry = tarArchiveInputStream.getNextTarEntry()) != null) {
                final File outputFile = new File(outputPath, tarArchiveEntry.getName());

                if (!overwrite && outputFile.exists()) {
                    continue;
                }

                if (tarArchiveEntry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    Files.copy(tarArchiveInputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Find files in path.
     *
     * @param path    find path
     * @param pattern find patttern
     * @return a set of path
     * @throws IOException IOException
     */
    public Set<Path> findFiles(String path, String pattern) throws IOException {
        try {
            Set<Path> paths = new HashSet<>();
            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);

            Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) {
                    if (pathMatcher.matches(filePath.getFileName())) {
                        paths.add(filePath);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            return paths;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * check file contains.
     *
     * @param filePath input file
     * @param stringPattern string pattern
     * @return true if contains
     * @throws IOException io exception
     */
    public boolean checkFileContains(final Path filePath, final String stringPattern) throws IOException {
        return getFileContent(filePath).contains(stringPattern);
    }

    /**
     * get file content.
     * @param filePath file path
     * @return file content
     * @throws IOException io exception
     */
    public String getFileContent(final Path filePath) throws IOException {
        return new String(Files.readAllBytes(filePath));
    }

    /**
     * find match patterns in file path.
     *
     * @param filePath file path
     * @param regexPattern regex pattern
     * @return set of matched patterns
     * @throws IOException io exception
     */
    public Set<String> findPatternsInFile(final Path filePath, final String regexPattern) throws IOException {
        Set<String> matchResults = new HashSet<>();
        try {
            String fileContent = getFileContent(filePath);
            Matcher matches = Pattern.compile(regexPattern).matcher(fileContent);
            while(matches.find()) {
                matchResults.add(matches.group(1));
            }
        } catch (IOException e) {
            throw e;
        }

        return matchResults;
    }
}
