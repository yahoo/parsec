// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle;

import com.yahoo.parsec.gradle.utils.PathUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;

/**
 * @author sho
 */
public class ParsecGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        ParsecPluginExtension pluginExtension = project.getExtensions().create("parsec", ParsecPluginExtension.class);
        PathUtils pathUtils = new PathUtils(project, pluginExtension);
        TaskContainer tasks = project.getTasks();

        // Create tasks (when applied as a plugin)
        ParsecInitTask initTask = tasks.create("parsec-init", ParsecInitTask.class);
        ParsecGenerateTask generateTask = tasks.create("parsec-generate", ParsecGenerateTask.class);

        // Make generate trigger init.
        generateTask.dependsOn(initTask);

        project.getPlugins().withType(JavaPlugin.class, plugin -> {
            SourceSet sourceSet = ((SourceSetContainer) project.getProperties().get("sourceSets")).getByName("main");

            // Add ${buildDir}/generated-sources/java to sources
            sourceSet.getJava().srcDir(pathUtils.getGeneratedSourcesPath());

            // Add ${buildDir}/generated-resources/parsec to resources
            sourceSet.getResources().srcDir(pathUtils.getGeneratedResourcesPath());

            // Make compileJava trigger generate
            tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).dependsOn(generateTask);
        });
    }
}
