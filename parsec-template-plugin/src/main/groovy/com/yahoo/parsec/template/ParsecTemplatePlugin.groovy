// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.template

import com.yahoo.parsec.template.tasks.CreateParsecProjectTask
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * @author waynwu
 */

class ParsecTemplatePlugin implements Plugin<Project> {

    void apply(Project project) {

        project.extensions.create("parsecTemplate", ParsecTemplateExtension)
        project.task 'createParsecProject', type:CreateParsecProjectTask
    }
}

