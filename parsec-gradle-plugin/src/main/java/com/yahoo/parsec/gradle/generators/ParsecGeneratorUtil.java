// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle.generators;

import com.yahoo.parsec.gradle.utils.FileUtils;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parsec Generator Utilities.
 */
public class ParsecGeneratorUtil {

    /**
     * file utils.
     */
    private FileUtils fileUtils;

    /**
     * default constructor.
     * @param fileUtils fileUtils instance
     */
    public ParsecGeneratorUtil(final FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    /**
     * Get Intersect PackageName, leave as non-static will make other package mock this method easier.
     *
     * @throws IOException IOException
     * @param packageNames package names
     * @return intersect package name
     */
    public String getIntersectPackageName(ArrayList<String> packageNames) throws IOException {
        if (packageNames == null || packageNames.size() == 0) {
            throw new IOException("packageNames is null or empty");
        }

        // choose first package name as baseline of package names
        int packageCount = packageNames.size();
        String firstPackageName = packageNames.get(0);
        String[] parts = firstPackageName.split("\\.");

        // if firstPackageName = "com.example"
        // the accumlateParts will be composed of [ "com", "com.example" ]
        List<String> accumulateParts = new ArrayList<>();
        String currPart = "";
        for (String part : parts) {
            currPart += currPart.isEmpty() ? part : "." + part;
            accumulateParts.add(currPart);
        }

        // try to find out the intersect package name by reverse order of accumulate parts
        // the order of match pattern would be something like "com.example", "com"
        for (int i = accumulateParts.size() - 1; i >= 0 ; i--) {
            String part = accumulateParts.get(i);
            int matchCount = 0;
            for (String packageName : packageNames) {
                // match com.example or com.
                String pattern = "^" + part + "(\\..+|$)";
                if (packageName.matches(pattern)) {
                    // make sure all package names have at least one intersect part
                    matchCount++;
                    if (matchCount == packageCount) {
                        return part;
                    }
                }
            }
        }

        throw new IOException("no intersect part found from packages");
    }

    /**
     * Generate from template to output dir.
     *
     * @param templateName template name
     * @param packageName package name in template to be replaced
     * @param outputDir output dir
     * @param overwrite overwrite
     * @throws IOException IOException
     */
    public void generateFromTemplateTo(
            final String templateName, final String packageName, final String outputDir, final boolean overwrite
    ) throws IOException {
        generateFromTemplateTo(templateName, packageName, outputDir, new HashMap<>(), overwrite);
    }

    /**
     * Generate from template to output dir with replaceMaterials.
     *
     * @param templateName template name
     * @param packageName package name in template to be replaced
     * @param outputDir output dir
     * @param replaceMaterials replace materials
     * @param overwrite overwrite
     * @throws IOException IOException
     */
    public void generateFromTemplateTo(
            final String templateName, final String packageName,
            final String outputDir, final Map<String, String> replaceMaterials, final boolean overwrite
    ) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream("/templates/" + templateName)) {
            String template = new String(IOUtils.toByteArray(inputStream));
            String output = template.replace("{packageName}", packageName);
            for (Map.Entry<String, String> replaceEntry : replaceMaterials.entrySet()) {
                output = output.replace(replaceEntry.getKey(), replaceEntry.getValue());
            }

            fileUtils.checkAndCreateDirectory(outputDir);
            fileUtils.writeResourceToFile(
                    new ByteArrayInputStream(output.getBytes()),
                    outputDir + "/" + templateName,
                    overwrite
            );
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * package name to file path.
     * @param packageName package name
     * @return filePath
     */
    public static String packageNameToPath(final String packageName) {
        return packageName.replace(".", "/");
    }

    /**
     * get path from source root.
     * @param packageStruct package struct object
     * @param packageName package name
     * @return path
     */
    public static String getPathFromSourceRoot(
            final ParsecPackageStruct packageStruct, final String packageName) {
        return packageStruct.getProject().getProjectDir().getPath() + packageStruct.getJavaSourceRoot() + packageName;
    }

    /**
     * get path from generated dir.
     * @param packageStruct package struct object
     * @param packageName package name
     * @return path
     */
    public static String getPathFromGeneratedRoot(
            final ParsecPackageStruct packageStruct, final String packageName) {
        return packageStruct.getGeneratedSourceRootPath()
                + "/" + packageName + "/" + packageStruct.getGeneratedNamespace();
    }
}
