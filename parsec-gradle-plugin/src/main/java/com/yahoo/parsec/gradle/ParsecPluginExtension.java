// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle;

/**
 * @author sho
 */
public class ParsecPluginExtension {

    /**
     * The path where RDL files ({@code *.rdl}) are located.
     */
    private String sourcePath = "src/main/rdl";

    /**
     * The RDL file(s) to parse, comma separated. If not present, all .rdl files in the source path
     * are processed.  The filenames here can also be absolute, and override the source path.
     */
    private String sourceFiles = "*.rdl";

    /**
     * If not empty, the value will be put in frontend of the generated swagger schema's endpoint.
     */
    private String swaggerRootPath = "";

    /**
     * If not empty, the value will overwrite swagger endpoint scheme.
     */
    private String swaggerSchema = "";

    /**
     * If set to true (the default), the generated code will include model classes for the RDL types.
     */
    private boolean generateModel = true;

    /**
     * If set to true (the default), the generated code will include JAX-RS (jersey) server classes,
     * using the XxxHandler interface (with a context).
     */
    private boolean generateServer = true;

    /**
     * If set to true, the generated code will include swagger resources
     */
    private boolean generateSwagger = true;

    /**
     * If set to true, the generated code will include json resources
     */
    private boolean generateJson = false;

    /**
     * If set to true, the generated code will include handlers' implementation
     */
    private boolean generateHandlerImpl = true;

    /**
     * If set to true, the generated code will include client implementation
     */
    private boolean generateClient = false;

    /**
     * If set to true, the generators will generate parsec error objects.
     */
    private boolean generateParsecError = true;

    /**
     * If set to true, the generators will generate classname with _Pc suffix.
     */
    private boolean generateModelClassNamePcSuffix = false;

    /**
     * If set to true, the generators will use resource params for generating resource and handler method names
     */
    private boolean useSmartMethodNames = false;

    /**
     * If set to true, the generators will generate exception mapper for handling uncaught exception
     */
    private boolean handleUncaughtExceptions = false;

    /**
     * If not empty, the value will be appended to the start of swagger basePath
     */
    private String finalName = "/api";

    /**
     * If set the addition path, the generators will copy swagger Json files to the path
     */
    private String additionSwaggerJsonPath;

    /**
     * If not empty, the generator will use naming style specify by user
     */
    private String accessorNamingStyle = "";

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(String sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public String getSwaggerRootPath() {
        return swaggerRootPath;
    }

    public void setSwaggerRootPath(String swaggerRootPath) {
        this.swaggerRootPath = swaggerRootPath;
    }

    public String getSwaggerSchema() {
        return swaggerSchema;
    }

    public void setSwaggerSchema(String schema) {
        this.swaggerSchema = schema;
    }

    public boolean isGenerateModel() {
        return generateModel;
    }

    public void setGenerateModel(boolean generateModel) {
        this.generateModel = generateModel;
    }

    public boolean isGenerateServer() {
        return generateServer;
    }

    public void setGenerateServer(boolean generateServer) {
        this.generateServer = generateServer;
    }

    public boolean isGenerateSwagger() {
        return generateSwagger;
    }

    public void setGenerateSwagger(boolean generateSwagger) {
        this.generateSwagger = generateSwagger;
    }

    public boolean isGenerateJson() {
        return generateJson;
    }

    public void setGenerateJson(boolean generateJson) {
        this.generateJson = generateJson;
    }

    public boolean isGenerateHandlerImpl() {
        return generateHandlerImpl;
    }

    public void setGenerateHandlerImpl(boolean generateHandlerImpl) {
        this.generateHandlerImpl = generateHandlerImpl;
    }

    public boolean isGenerateClient() {
        return generateClient;
    }

    public void setGenerateClient(boolean generateClient) {
        this.generateClient = generateClient;
    }

    public boolean isGenerateParsecError() {
        return generateParsecError;
    }

    public void setGenerateParsecError(boolean generateParsecError) {
        this.generateParsecError = generateParsecError;
    }

    public boolean isGenerateModelClassNamePcSuffix() {
        return generateModelClassNamePcSuffix;
    }

    public void setGenerateModelClassNamePcSuffix(boolean generateModelClassNamePcSuffix) {
        this.generateModelClassNamePcSuffix = generateModelClassNamePcSuffix;
    }

    public boolean isUseSmartMethodNames() {
        return useSmartMethodNames;
    }

    public void setUseSmartMethodNames(boolean useSmartMethodNames) {
        this.useSmartMethodNames = useSmartMethodNames;
    }

    public boolean isHandleUncaughtExceptions() {
        return handleUncaughtExceptions;
    }

    public void setHandleUncaughtExceptions(boolean handleUncaughtExceptions) {
        this.handleUncaughtExceptions = handleUncaughtExceptions;
    }

    public String getFinalName() {
        return finalName;
    }

    public void setFinalName(String finalName) {
        this.finalName = finalName;
    }

    public String getAdditionSwaggerJsonPath() {
        return additionSwaggerJsonPath;
    }

    public void setAdditionSwaggerJsonPath(String additionSwaggerJsonPath) {
        this.additionSwaggerJsonPath = additionSwaggerJsonPath;
    }

    public String getAccessorNamingStyle() {
        return accessorNamingStyle;
    }

    public void setAccessorNamingStyle(String accessorNamingStyle) {
        this.accessorNamingStyle = accessorNamingStyle;
    }

    public boolean isAccessorNamingStyle() {
        return accessorNamingStyle != null && !accessorNamingStyle.isEmpty();
    }
}
