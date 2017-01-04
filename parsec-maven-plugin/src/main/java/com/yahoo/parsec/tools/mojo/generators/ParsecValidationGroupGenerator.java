package com.yahoo.parsec.tools.mojo.generators;

import com.yahoo.parsec.tools.mojo.utils.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.nio.file.Path;
import java.util.*;

/**
 * ParsecValidationGroup Generator.
 */
public class ParsecValidationGroupGenerator {

    /**
     * the package struct.
     */
    private ParsecPackageStruct packageStruct;

    /**
     * the generator util.
     */
    private ParsecGeneratorUtil generatorUtil;

    /**
     * the file utils.
     */
    private FileUtils fileUtils;

    /**
     * default constructor.
     *
     * @param packageStruct package struct.
     * @param generatorUtil generators util
     * @param fileUtils file util
     */
    public ParsecValidationGroupGenerator(
            final ParsecPackageStruct packageStruct, final ParsecGeneratorUtil generatorUtil,
            final FileUtils fileUtils) {
        this.packageStruct = packageStruct;
        this.generatorUtil = generatorUtil;
        this.fileUtils = fileUtils;
    }

    /**
     * get replace materials for ParsecValidationGroups.
     *
     * replaceMaterial by pkg example:
     *  [ {package name1}: [ {replace key1}: {replace value1}, {replace key2}: {replace value2} ] ]
     *
     * @return replace materials
     * @throws MojoExecutionException
     */
    Map<String, Map<String, String>> getReplaceMaterialsByPkg() throws MojoExecutionException {
        final String regexPattern = "ParsecValidationGroups\\.(.+?)\\.class";
        final Map<String, List<Path>> dataobjects = packageStruct.getDataobjects();
        final Map<String, Map<String, String>> replaceMaterialsByPkg = new HashMap<>();

        for (Map.Entry<String, List<Path>> entry : dataobjects.entrySet()) {
            String packageName = entry.getKey();
            Set<String> validationGroups = new HashSet<>();
            for (Path dataobject : entry.getValue()) {
                validationGroups.addAll(fileUtils.findPatternsInFile(
                        dataobject, regexPattern));
            }
            if (validationGroups.size() > 0) {
                StringBuilder validationGroupStringBuilder = new StringBuilder();
                for (String group : validationGroups) {
                    validationGroupStringBuilder
                            .append("    public interface ")
                            .append(group)
                            .append(" { }")
                            .append(System.getProperty("line.separator"));
                }
                Map<String, String> replaceMaterials = new HashMap<>();
                replaceMaterials.put("{validationGroups}", validationGroupStringBuilder.toString());
                replaceMaterialsByPkg.put(packageName, replaceMaterials);
            }
        }

        return replaceMaterialsByPkg;
    }

    /**
     * generate validation groups.
     * @throws MojoExecutionException
     */
    public void generateParsecValidationGroups() throws MojoExecutionException {
        Map<String, Map<String, String>> replaceMaterialsByPkg = getReplaceMaterialsByPkg();
        final Map<String, String> packages = packageStruct.getPackages();
        for (Map.Entry<String, String> packageEntry : packages.entrySet()) {
            String packageName = packageEntry.getKey();
            if (replaceMaterialsByPkg.containsKey(packageName)) {
                String outputDir = ParsecGeneratorUtil.getPathFromGeneratedRoot(
                        packageStruct, packageEntry.getValue());
                generatorUtil.generateFromTemplateTo(
                        "ParsecValidationGroups.java", packageName, outputDir,
                        replaceMaterialsByPkg.get(packageName), true);
            }
        }
    }
}
