## Getting Started

### Requirements

+ **Java 1.8**: check your jdk version using `$ java -version` or [download Java JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
+ **Gradle (recommended version: >2.4)**: check your gradle version using `$ gradle --v` or run

 `$ sudo brew install gradle` to install the latest gradle

After having Java 1.8 and Gradle installed with the proper version, you must do a one time setup to allow you to create a
Parsec project without a build.gradle script.

`$ vim ~/.gradle/init.gradle `, then enter the following code:

```
gradle.beforeProject { prj ->
   prj.apply from: 'https://raw.githubusercontent.com/yahoo/parsec/master/parsec-template-plugin/installation/apply.groovy'
}

```

### Create a New Project

To create a new Parsec project, run:

`$ gradle createParsecProject -PgroupId='your.group.name' -PartifactId='your_project_name'`

**groupId** refers to the namespace of your package, while the **artifactId** is your project name.
If you do not specify the groupId or artifactId, you will be prompted to do so.

### Create Schema

You need one or more RDL schema files to define your API specifications; they should be placed under src/main/rdl/ and be named as *.rdl.
RDL is a machine-readable description of a schema that describes data types, as well as resources using those types.

You can start with this sample RDL file, save it as src/main/rdl/sample.rdl:

```
namespace your.group.name;
name sample;
version 1;

type User struct {
    string name (x_not_null="groups=insert", x_size="min=3,max=5,groups=update|insert");
    string occupation (x_not_null="groups=update", x_size="min=4,groups=update|insert");
    int32 age;
    string id (x_null="groups=insert");
}

resource User GET "/users/{id}" {
    int32 id;

    expected OK;
    exceptions {
        ResourceError INTERNAL_SERVER_ERROR;
        ResourceError BAD_REQUEST;
        ResourceError UNAUTHORIZED;
        ResourceError FORBIDDEN;
    }
}

resource string POST "/users" {
    User user (x_must_validate="insert");

    expected OK;
    exceptions {
        ResourceError INTERNAL_SERVER_ERROR;
        ResourceError BAD_REQUEST;
        ResourceError UNAUTHORIZED;
        ResourceError FORBIDDEN;
    }
}

resource string PUT "/users/{id}" {
    int32 id ;

    User user (x_must_validate="update");

    expected OK;
    exceptions {
        ResourceError INTERNAL_SERVER_ERROR;
        ResourceError BAD_REQUEST;
        ResourceError UNAUTHORIZED;
        ResourceError FORBIDDEN;
    }

}
```

### Generate Code

After you have added your rdl files in the folder, you can use the command below to generate files:

`$ gradle parsec-generate`

Here is an example of the generated folder structure, based on the sample.rdl above:

```
$ tree build/generated-sources/
build/generated-sources/
└── java
    └── your
        └──group
            └──name
                └──parsec_generated
                    ├── ParsecApplication.java
                    ├── ParsecErrorBody.java
                    ├── ParsecErrorDetail.java
                    ├── ParsecExceptionMapper.java
                    ├── ParsecResourceError.java
                    ├── ParsecValidationGroups.java
                    ├── ParsecWebListener.java
                    ├── ParsecWrapperServlet.java
                    ├── ResourceContext.java
                    ├── ResourceError.java
                    ├── ResourceException.java
                    ├── SampleHandler.java
                    ├── SampleResources.java
                    ├── SampleServer.java
                    └── User.java

4 directories, 15 files

$ tree src/main/java/
src/main/java/
└── your
    └── group
        └──name
            ├── DefaultApplication.java
            ├── DefaultExceptionMapper.java
            ├── DefaultResourceContext.java
            ├── DefaultWebListener.java
            ├── SampleClient.java
            ├── SampleClientImpl.java
            └── SampleHandlerImpl.java

2 directories, 7 files
```

Parsec generated Java server, model code, and Java client for the server. The generated code would include:

+ Java models / data objects, for example: User.java
+ Jersey resource endpoints, for example: SampleResource.java
+ Handler interfaces, for example: SampleHandler.java - Handler implementations follow a naming convention: <API name>HandlerImpl.java
+ Java client for your web service, for example: SampleClient.java, SampleClientImpl.java

Java files are generated under ${baseDir}/build/generated-sources/java and the generated Java code are in sub-package <user defined namespace>.parsec_generated

### Implement Handlers

Now you can start to implement your API by editing *HandlerImpl.java files. Here is an example of SampleHandlerImpl.java:

```
package your.group.name;

import your.group.name.parsec_generated.User;
import your.group.name.parsec_generated.SampleHandler;
import your.group.name.parsec_generated.ResourceContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SampleHandlerImpl is interface implementation that implement SampleHandler interface.
 */
public class SampleHandlerImpl implements SampleHandler {

    @Override
    public User getUsersById(ResourceContext context, Integer id) {
        User user = new User();
        user.setName("user");
        return user;
    }

    @Override
    public String postUsers(ResourceContext context, User user) {
        return "Post to user " + user.getName() + "!\n";
    }

    @Override
    public String putUsersById(ResourceContext context, Integer id, User user) {
        return "put to user " + user.getName() + " by id " + id + "!\n";
    }

    @Override
    public ResourceContext newResourceContext(HttpServletRequest request, HttpServletResponse response) {
        return new DefaultResourceContext(request, response);
    }
}
```

### Start your Server

Now you can start your server with:

`$ gradle jettyRun`

Check out your [Swagger dashboard](http://localhost:8080/api/static/swagger-ui/)

Now you can run test with:

```
$ curl http://localhost:8080/api/sample/v1/users/1

{"age":0,"name":"user"}

$ curl -H 'Content-Type: application/json' -d '{"name":"user","age":10}' http://localhost:8080/api/sample/v1/users

Welcome to Parsec, user!
```

