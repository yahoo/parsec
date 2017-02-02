Testing
=======

Unit Testing
------------

### Spock

As the default unit test framework, Spock has a number of features:

> -   Enables Behavior-Driven Development
> -   Uses Groovy which makes it easier to develop test cases
> -   Tests are descriptive, making them easier to understand and manage
> -   Has powerful features such as mocking methods by regex match

Ideally, each piece of Spock code should be fairly self-explanatory. For
instance, a test method that checks for non-null variable may look like
this:

```java
import spock.lang.Specification

class MyClassTest extends Specification {
    def "ensure that foo is not null"() {
        given: def myClass = new MyClass()
        when: def foo = myClass.getFoo()
        then: foo != null
    }
}
```

Another example that mocks with sample data in Spock:

```java
class SampleHandlerImplTest extends Specification {    
    def "test post user"() {
        given: 
        def impl = new SampleHandlerImpl()
        def context = Mock(SampleResourceContext)
        context.request() >> Mock(HttpServletRequest)

        expected:
        impl.postUser(context, new User().setName(name)) == result

        where:
        name   || result
        "jake" || "\"Hello jake!\"\n"
    }
}
```

Besides being highly descriptive, Spock is also packed with powerful
features such as regex mocking. For example, to match all setter methods
of a given object:

```java
class MyClassTest extends Specification {
    def "ensure that foo is not null"() {
        given: def myClass = new MyClass()
        when: def foo = myClass.getFoo()

        then:
        foo != null
        // check if call to any setter of myClass with any argument once
        1 * myClass./set.*/(_) 
    }
}
```

To learn more about other Spock features, refer to the official [Spock
documentation](http://spockframework.github.io/spock/docs/1.0/index.html).

**Note: By default Spock test files should be placed under
src/test/groovy**

Getting started with Spock should be straightforward if you are already
familiar with Java or Groovy. If you are unfamiliar with Java, learning
Groovy would probably be easier than learning Java. Here is some
documentation to get you started:

> -   [Spock
>     Primer](http://spockframework.github.io/spock/docs/1.0/spock_primer.html)
> -   [Groovy documentation](http://groovy-lang.org/documentation.html)

Or if you prefer to learn by example or prefer actual code for
referencing:

> -   [Official Spock
>     example](https://github.com/spockframework/spock-example/tree/master/src/test/groovy)

#### Alternative test frameworks

Alternative recommend unit test framewords are JUnit and TestNG.

### JUnit

Spock depends on JUnit. Place your test files under **src/test/java** to
run tests with JUnit.

### TestNG

Spock and JUnit conflicts with TestNG. To use TestNG you will need to
remove **spock-core** from your build.gradle and add TestNG dependencies.

```
dependencies {
    compile group: 'org.spockframework', name: 'spock-core', version: '1.1-groovy-2.4-rc-3'
}
```

[TestNG on
mvnrepository.com](http://mvnrepository.com/artifact/org.testng/testng)

Smoke and Functional Testing
----------------------------

Cucumber-JVM + Groovy is the recommended framework for integration tests
of Parsec services for reasons that include:

> -   **Behaviour Driven Design (BDD)** reduces the gap between product
>     requirements and software development and is aligned with agile
>     development.
> -   **Cucumber-JVM** is a tool that implements a BDD workflow.
>     Cucumber-JVM is a pure Java implementation of Cucumber.
> -   **Groovy** is a JVM-based language and it’s fully compatible with
>     pure Java libraries.
> -   **Spock** is also implemented using Groovy

Before you start, make sure the following **Cucumber dependencies are in
your build.gradle**:

```
dependencies {
    compile group: 'info.cukes', name: 'cucumber-junit', version: '1.2.5'
    compile group: 'info.cukes', name: 'cucumber-groovy', version: '1.2.5'
    compile group: 'info.cukes', name: 'cucumber-java', version: '1.2.5'
}
```

The following steps will help you get started:

> -   write scenarios in feature files (\*.feature)
> -   implement RunCukesIT.java
> -   implement step definitions (\*.groovy)
> -   integrate with CD

If you're not familiar with BDD or Cucumber, here are some resources to
get you started:

> -   [Behavior Driven Development
>     wiki](https://en.wikipedia.org/wiki/Behavior-driven_development)
> -   [Cucumber wiki](https://en.wikipedia.org/wiki/Cucumber_(software))

