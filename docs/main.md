Main Components
===============

RDL and Parsec RDL Generator
----------------------------

-   RDL
    -   Is a description language for specifying an HTTP-based web
        service.
    -   Ardielle(RDL) official site: [https://ardielle.github.io](https://ardielle.github.io)
-   Parsec RDL Generator
    -   Generates data objects, Jersey endpoints, interfaces, and stubs.
    -   Generates Swagger JSON for Swagger UI
    -   Generates Markdown and JSON for documentations

[Parsec Template Plugin](https://github.com/yahoo/parsec/tree/master/parsec-template-plugin)
----------------------

Parsec Template Plugin is a gradle plugin extended from townsfolk's [gradle templates plugin](https://github.com/townsfolk/gradle-templates).
It provides similar functionality as Maven's archetype, which generates files and directories based on a template. In parsec templates plugin, a custom template is provided which is tailored
for Parsec. See the default output for a sample of what the folder structures will look like.


Please refer to [Parsec Template Plugin Doc](https://github.com/yahoo/parsec/tree/master/parsec-template-plugin) for details

[Parsec Base Build](https://github.com/yahoo/parsec/tree/master/parsec-base-build)
----------------------------

The base build provides a parent [parsec.gradle](https://github.com/yahoo/parsec/parsec-base-build/src/main/resources/parsec.gradle)
file for any Parsec project to inherit from. The build file includes
dependencies, build plugins, and necessary configurations that are used within a Parsec project. These external settings
are designed to enforce a standardized build process in which enhances the quality of the Parsec client's application.
It also includes the Parsec Gradle Plugin which is responsible for parsing the RDLs and generating files.

### Usage
The base build is essentially a Script Plugin and must be inherited using:
```
apply from: 'https://raw.githubusercontent.com/yahoo/parsec/master/parsec-base-build/src/main/resources/parsec.gradle'
```

The parsec.gradle file includes all necessary dependencies for you to get started with a Parsec project.

### Properties
To override properties (eg. version numbers), create identically-named properties within your project. There are multiple
ways of doing this, the easiest and recommended way is to add in the properties in your project's `gradle.properties` file. Once you have
define the properties, it will take precedence over the properties defined by the base build. Optionally, you could also define
the properties in your `buildscript { } ` block. 

You can view the defined properties [here.](https://github.com/yahoo/parsec/blob/master/parsec-base-build/src/main/resources/gradle.properties)



[Parsec Gradle Plugin](https://github.com/yahoo/parsec/tree/master/parsec-gradle-plugin)
----------------------

Parsec Gradle Plugin is the underlying core of Parsec which utilizes the RDL generator. 
Its main purpose is to generate all the necessary files, based on the given RDL schema, in order to get a webservice started.

###Installation
In your build script, add in the buildscript dependencies needed in order
to apply the plugin:

```
buildscript{
    repositories{
        jcenter()
    }
    dependencies{
        classpath group: 'com.yahoo.parsec'  , name: 'parsec-gradle-plugin'   , version : '0.0.14-pre'
    }
}
```

then you can `apply plugin: 'com.yahoo.parsec.gradle-plugin'`

###Usage
If used in conjunction with the Parsec Template Plugin (ie. after running `$ gradle createParsecProject`), you can simply
put the RDL files inside "src/main/rdl". If not, you must specify the RDL sourcePath inside the configuration.

To generate the files after providing the rdl schema, use `$ gradle parsec-generate`

Please refer to [Parsec Gradle Plugin Doc](https://github.com/yahoo/parsec/tree/master/parsec-gradle-plugin) for details.
