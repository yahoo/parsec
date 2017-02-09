buildscript {
  repositories {
    jcenter()
    maven {
      url 'http://dl.bintray.com/cjstehno/public'
    }
  }
  dependencies {
    classpath "com.yahoo.parsec:parsec-template-plugin:0.0.28-pre"
  }
}

allprojects {
    apply plugin: com.yahoo.parsec.template.ParsecTemplatePlugin
}
