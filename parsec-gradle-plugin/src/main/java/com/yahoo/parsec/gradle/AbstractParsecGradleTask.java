// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.gradle;

import com.yahoo.parsec.gradle.utils.FileUtils;
import com.yahoo.parsec.gradle.utils.PathUtils;
import org.gradle.api.DefaultTask;

/**
 * @author waynewu
 */
abstract public class AbstractParsecGradleTask extends DefaultTask{

    ParsecPluginExtension pluginExtension;
    FileUtils fileUtils;
    PathUtils pathUtils;

    String name;
    String description;
    String group;

    AbstractParsecGradleTask(final String name, final String description){
        super();
        this.name = name;
        this.description = description;
        this.group = "parsec";
        pluginExtension = getProject().getExtensions().findByType(ParsecPluginExtension.class);
        if(pluginExtension == null){
            getLogger().info("Project did not create an extension");
            pluginExtension = new ParsecPluginExtension();
        }
        fileUtils = new FileUtils(getLogger());
        pathUtils = new PathUtils(getProject(), pluginExtension);
    }

    void setPluginExtension(ParsecPluginExtension pluginExtension){ this.pluginExtension = pluginExtension; }

    void setFileUtils(FileUtils fileUtils){this.fileUtils = fileUtils; }

    void setPathUtils(PathUtils pathUtils){this.pathUtils = pathUtils; }

}
