// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.template

/**
 * @author waynewu
 */

class ParsecTemplateExtension {


    /**
     * The Parsec Base Build Version.
     * Default: (latest version)
     */
    String baseBuildVersion = "1.0.13-pre"

    private String BASE_BUILD_URL = "https://raw.githubusercontent.com/yahoo/parsec/master/parsec-base-build/src/main/resources/parsec.gradle"

    /**
     * Extra elements to generate
     * Default: {}
     */
    Closure extraTemplate = {}

    /**
     * File that the generated build.gradle should inherit (extend) from
     * default will be the parsec-base-build
     * Default: parsec.gradle
     */
    String applyFromPath = BASE_BUILD_URL

    /**
     * Create a sample RDL file that allows you to run parsec-generate without anything else
     * Used mostly for testing though, it can be used as a reference
     * Default: false
     */
    boolean createSampleRDL = false

    /**
     * Create .travis.yml file for Continuous Integration with Travis.
     * Default: true
     */
    boolean createTravisCI = true

    /**
     * Create the wrapper files for the gradle project
     * Default: true
     */
    boolean createWrapper = true


    public boolean usesBaseBuild(){
        applyFromPath == BASE_BUILD_URL
    }

    public String getBaseBuildUrl(){
        return BASE_BUILD_URL
    }

}
