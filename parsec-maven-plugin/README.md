# parsec_maven_plugin

This is a project to deploy a maven plugin into maven repository.

# Developer Guides

##Mojo Unit Testings

What described on the [official maven plugin testing guide](https://maven.apache.org/plugin-testing/maven-plugin-testing-harness/index.html) doesn't work.
It throws all kinds of runtime class not found exceptions and can't be resolved.

However it's not required to extend `AbstractMojoTestCase` to do Mojo Unit Testing.
What this base class does is to inject dependencies automatically, which can be accomplished with java reflection and a tiny bit more coding.

`parsec_maven_plugin/src/test/java/com/yahoo/ec/parsec/tools/mojo/MojoTestUtils.java` has an example on how to inject an arbitrary MavenProject object,
 and `parsec_maven_plugin/src/test/groovy/mojo/ParsecInitializeMojoTest.groovy` shows how to use this util to inject a stub object to the Mojo under testing.
