buildscript{
    repositories{
        maven{
            url "https://plugins.gradle.org/m2/"
        }
        maven{
            url 'http://dl.bintray.com/cjstehno/public'
        }
    }
    dependencies{
        classpath group: 'gradle.plugin.com.yahoo.parsec',
                name: 'parsec-template-plugin',
                version: '1.0.13-pre'
    }
}

allprojects {
    apply plugin: com.yahoo.parsec.template.ParsecTemplatePlugin
}