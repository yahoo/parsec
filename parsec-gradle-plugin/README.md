#Parsec Gradle Plugin
Parsec Gradle Plugin is the underlying core of Parsec which utilizes the RDL generator. Its main purpose is to generate all the
necessary files, based on the given RDL schema, in order to get a webservice started.

##Installation
In your build script, add in the buildscript dependencies needed in order
to apply the plugin:

```
buildscript{
    repositories{
        jcenter()
    }
    dependencies{
        classpath group: 'com.yahoo.parsec'  , name: 'parsec-gradle-plugin'   , version : '0.0.18-pre'
    }
}
```

then you can `apply plugin: 'com.yahoo.parsec.gradle-plugin'`

##Usage
If used in conjunction with the Parsec Template Plugin (ie. after running `$ gradle createParsecProject`), you can simply
put the RDL files inside "src/main/rdl". If not, you must specify the RDL sourcePath inside the configuration.

To generate the files after providing the rdl schema, use `$ gradle parsec-generate`

##Configuration
Parsec gradle plugin provides several settings that you can play around with.

```
parsec {
    //extension properties
}
```

| Property               | Type   | Default      | Description |
|:----------------------:|:------:|:------------:|:-----------:|
|sourcePath              |String  |"src/main/rdl"|The path where the RDL files are located |
|sourceFiles             |String  |"*.rdl"       |The rdl files to be parsed, separated by commas. If not indicated every RDL file will be parsed|
|swaggerRootPath         |String  |""            |The value will be put in frontend of the generated swagger schema's endpoint|
|generateModel           |boolean |true          |Generate model classes for the RDL types|
|generateServer          |boolean |true          |Generate JAX-RS (jersey) server classes|
|generateSwagger         |boolean |true          |Generate swagger resources|
|generateJson            |boolean |false         |Generate JSON resources|
|generateHandlerImpl     |boolean |true          |Generate handler's implementation|
|generateParsecError     |boolean |false         |Generate Parsec error objects|
|useSmartMethodNames     |boolean |true          |Generator will use resource params for generating resource and handler method names|
|handleUncaughtExceptions|boolean |false         |Generate exception mapper for handling uncaught exception|