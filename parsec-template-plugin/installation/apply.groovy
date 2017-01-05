buildscript{
    repositories{
//        maven{
//            url "https://plugins.gradle.org/m2/"
//        }
        jcenter()
        maven{
            url 'http://dl.bintray.com/cjstehno/public'
        }
    }
    dependencies{
        classpath group: 'com.yahoo.parsec', name: 'parsec-template-plugin', version: '0.0.0.4-pre'
    }
}

allprojects {
    apply plugin: com.yahoo.parsec.template.ParsecTemplatePlugin
}