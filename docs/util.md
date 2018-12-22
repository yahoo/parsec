Supporting Libraries and Utilities
==================================

Input Validation
----------------

### Purpose

> -   Define standard method for input validation in Parsec Java web
>     applications
> -   Support web applications’ requirement to validate user input
>     depending on their business logic

### Design and implementation

> -   Adopt [bean validation in
>     Jersey](https://jersey.java.net/documentation/latest/bean-validation.html),
>     which is Jersey framework’s native feature
> -   Jersey bean validation depends directly on Hibernate Validator, it
>     supports validating all forms of input from Jersey framework. Such
>     as @PathParam, @QueryParam, @FormParam, and request body json
>     object by declaring hibernate validation annotations (such as
>     @Size, @Null, @NotNull, …) ([section builtin constraints
>     doc](http://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-builtin-constraints))
> -   parsec\_validation Java package
>     -   requires jersey-bean-validation dependency
>     -   parsec\_validation added to dependency by default if project
>         inherits [Parsec Base
>         Build](main.md#parsec-base-build-parent-pom)
>     -   when parsec\_validation is added to dependency (in build.gradle),
>         validation feature will be enabled automatically without
>         requiring any additional function calls
>     -   Implemented ParsecValidationExceptionMapper to catch
>         ConstraintViolationException exception (which is thrown by
>         Hibernate Validator)
> -   User can also customize their validation exception {code},
>     {message} by defining server properties in DefaultWebListener.java
>     -   ParsecValidationExceptionMapper.PROP\_VALIDATION\_DEFAULT\_ERROR\_CODE
>     -   ParsecValidationExceptionMapper.PROP\_VALIDATION\_DEFAULT\_ERROR\_MSG
> -   Implemented ParsecValidationAutoDiscoverable to register higher
>     priority for ParsecConstraintViolationExceptionMapper (because the
>     default priority of Jersey's ConstraintViolationExceptionMapper
>     for ConstraintViolationException is higher than others)
> -   Integrate hibernate validation annotation with Parsec RDL
>     Generator (see [RDL + Parsec RDL
>     Generator](main.md#rdl-and-parsec-rdl-generator))
>     -   define hibernate validation annotation in .rdl then the
>         annotations will be generated
> -   [Validation
>     github](https://github.com/yahoo/parsec-libraries/tree/master/parsec-validation)

default build.gradle snippet:

```
dependencies {
    //...
    compile group: 'com.yahoo.parsec', name: 'parsec-validation', version: '0.0.15'
    //...
}
```

error layout example:

```
{
    "error":{
        "detail":[
            {
                "message":"may not be null",
                "messageTemplate":"{javax.validation.constraints.NotNull.message}",
                "path":"SampleResources.postUser.user.id",
                "invalidValue":null
            },
            {
                "message":"'invalid_name' max 5",
                "messageTemplate":"'${validatedValue}' max {max}",
                "path":"SampleResources.postUser.user.name",
                "invalidValue":"invalid_name"
            }
        ],
        "code":40001,
        "message":"constraint violation validate error unittest"
    }
}
```

### Supported Validation constraints

Constraints                       |supported RDL data type             |Use
:---------------------------------|:-----------------------------------|:-------------------------------------
x\_min="x"                        | Int8, Int16, Int32, Int64, Byte    | value should be greater or equals x
x\_max="x"                        | Int8, Int16, Int32, Int64, Byte    | value should be less or equals x
x\_size="min=x,max=y"             | String, Array, Map                 | value should be between x and y (inclusive)
x\_pattern="regexp="x""           | String                             | value should match the regex defined by x
x\_must\_validate                 | Struct                             | Performs validation recursively on the associated object
x\_name="x"                       | String                             | use x instead if the originl rdl name to get input value
x\_not\_null                      | any type                           | value should be not null
x\_not\_blank                     | String                             | value should be not null and size is greater than zero
x\_not\_empty                     | Array, Map                         | the size of the value must be greater than 0 and is not null
x\_country\_code                  | String                             | ISO 639 country code, in lower case
x\_currency                       | String                             | ISO 4217 currency
x\_language\_tag                  | String                             | BCP 47 language tag
x\_null                           | any type                           | value should be null
x\_digits="integer=x,fraction=y"  | float32, float64                   | value should match x in integer part, and also match y in fraction part

### Using Validation Groups

Validation groups allows you to control the set of constraints to enable
per object for an endpoint. Please note that only data object validation
supports this feature. The syntax to define constraint validation groups
follows this syntax: *&lt;constraint&gt;="groups=&lt;groups&gt; \[,
&lt;other settings&gt;\]"*. Where:

-   &lt;constraint&gt; is one of the constraint in the previous section
-   &lt;groups&gt; is a **|** seperated list of groups that will enable
    this constraint.
-   &lt;other settings&gt; are other settings supported by the contraint

For example, this size constraint will only be enabled if group is
create or update:

    String someField (x_size="min=3, max=5, groups=create|update");

To control which validation group to enable for an object in a
particular endpoint, use *x\_must\_validate=&lt;group name&gt;* syntax.
Please be advised that only one validation group may be defined per
object at a time. For example:

    Object someObject (x_must_validate="update");

**Please note that the defined validation group must exist (i.e. used in
an object) otherwise you may receive Java compile time errors.**

Please see [Example Adding Validation in
RDL](util.md#example-adding-validation-in-rdl) section for example
usage.

For more details regarding validation groups, please refer to external
resource [Grouping
constraints](http://docs.jboss.org/hibernate/validator/5.2/reference/en-US/html/ch05.html).

### Customizing Validation Error Code and Message

Customize validation error {code}, {message} in DefaultWebListener.java:

    @WebListener
    public class DefaultWebListener implements ServletContextListener {
        @Override
        public void contextInitialized(ServletContextEvent sce) {
            ServletContext context = sce.getServletContext();

            // add api application servlet with customized validation error code, message
            final DefaultApplication app = new DefaultApplication();
            app.property( ParsecValidationExceptionMapper.PROP_VALIDATION_DEFAULT_ERROR_CODE, 40001);
            app.property( ParsecValidationExceptionMapper.PROP_VALIDATION_DEFAULT_ERROR_MSG, "test validation error message");
            ...
         }
    }

### Example Adding Validation in RDL

-   Adding validation to your name field in User struct at
    src/main/rdl/sample.rdl
    -   Here we set the limitation to User.name
    -   The length of User.name should be &gt;= 3 && &lt;= 5

```
...

type User struct {
    string name (x_size="min=3,max=5");
    int32 age;
}

...
```

-   Adding validation to POST data

```
...

resource string POST "/user" {
    User user (x_must_validate);

...
```

-   Then we re-generate java code and run the web server

```bash
$ gradle parsec-generate
$ gradle jettyRun
```

-   Check if the validation works

```bash
$ curl -H 'Content-Type: application/json' -d '{"name":"test","age":10}' http://localhost:8080/sample/v1/user

Hello test!

$ curl -H 'Content-Type: application/json' -d '{"name":"test_user","age":10}' http://localhost:8080/sample/v1/user

{
  "error": {
    "code": 0,
    "detail": [
      {
        "invalidValue": "test_user",
        "message": "size must be between 3 and 5",
        "messageTemplate": "{javax.validation.constraints.Size.message}",
        "path": "SampleResources.postUser.arg0.name"
      }
    ],
    "message": "constraint violation validate error"
  }
}
```

-   Adding a size constraint for create and update

```
...

type User struct {
    string name (x_size="min=3,max=5,groups=create|update");
    int32 age;
}

...
```

-   Using validation groups

```
...

type User struct {
    string name (x_not_null="groups=insert",x_size="min=3,max=5,groups=insert|update");
    string occupation (x_not_null="groups=update", x_size="min=4,groups=update|insert");
    int32 age;
}


resource string POST "/users" {
    User user (x_must_validate="insert");

    ...
}

resource string PUT "/users/{id}" {
    int32 id ;

    User user (x_must_validate="update");

    ....
}

...
```

With the example above, a "post" request to /users would be checked against the following rules:

>    -   the name field is required, the length of its value should be
>        from 3 to 5 chars inclusive.
>    -   the occupation field is NOT required, but when it is presented
>        the length of its value should be longer than 4 chars
>    -   the age field is not required and wouldn't be checked

And a "put" request to users/{id} would be checked against the following rules:

>    -   the name field is NOT required, but when it is presented the
>        length of its value should be from 3 to 5 chars inclusive.
>    -   the occupation field is required, the length of its value should
>        be longer than 4 chars
>    -   the age field is not required and wouldn't be checked

Parsec Config
-------------

### Purpose

-   Provide a library to define configuration by environment (such as
    alpha, beta, production)
-   Introduce a best practice for Java web application configuration
    definition
-   Supply a config library that is compatible with Manhattan’s
    environment

### Implementation Steps

Let's get started, you can easily understand how to use this library by
performing the following steps:

**Step 1: Construct your configurations**

> -   define shared and environment specific settings
> -   place your configuration files under *&lt;Project
>     Directory&gt;/src/main/resources/,* so Parsec config library can
>     find them
> -   configuration examples as below:

*dev.example.conf* (environment specific setting):

```
include "common_default.conf"

simpleKey = simpleValue
number = 123
duration = 10m
boolean = true

db {
    driver = com.mysql.jdbc.Driver
    username = testuser
    keyname = aabb
}

booleanList: [ true, false, true ]
numberList: [ 1, 2, 3, 4, 5, 6 ]
stringList: [ "abc", "456", "xyz" ]
durationList: [ 10m, 300s ]

configList: [
    {
        key1 = val1
    }
    {
        key2 = val2
    }
]
```

*common\_default.conf* (shared setting):

```
simpleKey = defaultValue
common {
   errorMsg = this is a error
}
```

**Step 2: Include parsec\_config dependency in build.gradle**

Add *parse\_config* dependency in *build.gradle*:

```
compile group: 'com.yahoo.parsec', name: 'parsec-config', version: '0.0.15'
```

**Step 3: Adopt parsec\_config to your code**:

```
public class ExampleConfig {
  static final ParsecConfig CONFIG = ParsecConfigFactory.load();

  public String getDbUserName() {
    return CONFIG.getString("db.username");
  }

  public String getErrorMessage() {
    return CONFIG.getString("common.errorMsg");
  }
}
```

**Step 4: Setup environment settings for different environments**:

We use *parsec.conf.env.context* system property key to identify the
environment. In other words, specifying
*-Dparsec.conf.env.context=tp2.conf* in JVM command line argument would
cause the library to read tp2.conf in resources.

To configure for Gradle unit testing, set *parsec.conf.env.context*
property to *dev.example.conf* in your gradle.properties. Example as below:

```
systemProp.parsec.conf.env.context=dev.example.conf
```

Or you could specify *System.setProperty(ParsecConfigFactory.ENV\_KEY,
"dev.example.conf")* in your test code

### Reference

> -   [parsec\_config
>     GitHub](https://github.com/yahoo/parsec-libraries/tree/master/parsec-config)
> -   [typesafe lib git doc](https://github.com/typesafehub/config)

Parsec Client
-------------

### Purpose

Provide a async HTTP client that can support the following 
requirements:

> -   Per request retry by response HTTP status code
> -   Cookie and header back posting
> -   Short duration session/response cache (for GET method only)
> -   Profiling logs (connection time, and etc)
> -   Splunk compatible log

### Basic Usage Example

Code example for basic usage example:

    // Initializing a client
    ParsecAsyncHttpClient client = new ParsecAsyncHttpClient.Builder().build();

    // Initialize a GET request
    ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
        .setUrl("http://api.yahoo.com:4080")
        .addQueryParam("mid", "12345")  // Adding a query parameter
        .addHeader("X-ESI", "1")        // Adding a header
        .build();

    // Executing a request
    Future<Response> future = client.execute(request);

    // Initialize a POST request
    ParsecAsyncHttpRequest postRequest = new ParsecAsyncHttpRequest.Builder()
        .setMethod("POST")
        .setUrl("http://api.yahoo.com:4080")
        .addFormParam("title", "My title")    // Adding form parameters
        .build();

    // Executing a blocking request
    Response response = client.execute(postRequest).get();

### Asynchronous and Blocking Requests

Both ParsecAsyncHttpClient.execute and ParsecAsyncHttpClient.criticalExecute
returns CompletableFuture and is therefore asynchronous / non-blocking by nature. 
If you need to make blocking calls, please use CompletableFuture.get method.

### Retrying Requests by Response HTTP Status Code

Requests can be retried based on the response's HTTP status code. To add
or remove a retry HTTP status code, please use
ParsecAsyncHttpRequest.Builder.addRetryStatusCode and
ParsecAsyncHttpRequest.Builder.removeRetryStatusCode method. 
The maximum number of **total** retries for all status code can
be controlled using ParsecAsyncHttpRequest.Builder.setMaxRetries
method.

For example, the following code will create a request object that
retries response status code 404 and 500 for a total of 2 times:

    ParsecAsyncHttpRequest request = new ParsecAsyncHttpRequest.Builder()
        .setUrl("http://tw.yahoo.com")
        .addRetryStatusCode(404)
        .addRetryStatusCode(500)
        .setMaxRetries(2)
        .build();

### In Memory Short Duration Response Cache

By default the client enables an in memory short duration loading cache
for *GET* requests. This means for all identical *GET* requests that
occur in a 2 seconds window, only the first request will be executed
while all remaining requests will be responded from cache. If fresh copy
of the data is required (for example, during a get update get scenario),
please use ParsecAsyncHttpClient.criticalExecute method or
ParsecAsyncHttpRequest.Builder.setCriticalGet method.

### Log Requests and Responses

Requests and responses to and from a ParsecAsyncHttpClient can be logged with `RequestResponeLoggingFilter`. Follow the 3 simple steps to enable this:


	RequestFilter loggingFilter = new RequestResponeLoggingFilter(new NingJsonFormatter());  //  (1)

	ParsecAsyncHttpClient parsecHttpClient = new ParsecAsyncHttpClient.Builder()
                .setAcceptAnyCertificate(true)
                .addRequestFilter(loggingFilter) 
                .build();                                                                   //   (2)
                
                

1. Create a `RequestResponeLoggingFilter`, as shown above (1).
	1. The only mandatory parameter for the constructor is an instance of `NingRequestResponseFormatter`, which dedicates how to present the Request and Response data in the log.
	2. By default, the filter only logs `post`, `put` and `delete` requests and response. This can be changed with a different instance of `BiPredicate<Request, ResponseOrThrowable>` 
	3. By	default, the logger name for request/response logging is `parsec.clients.reqresp_log`. This can be changed by passing a different name in the constructor. 
2. Add the filter to the ParsecAsyncHttpClient builder method, as shown above (2).
3. Configure `logback.xml` to enable the trace level of "parsec.clients.reqresp_log" logger.  Note that the log name is configurable from the `RequestResponeLoggingFilter` constructor.

```
<configuration scan="false">
    <!-- omit the other settings -->
    <logger name="parsec.clients.reqresp_log" level="trace" />
</configuration>
``` 	
 
 
 See also
 
 - [RequestResponeLoggingFilter.java](https://github.com/yahoo/parsec-libraries/blob/master/parsec-clients/src/main/java/com/yahoo/parsec/filters/RequestResponeLoggingFilter.java)
 - [RequestResponeLoggingFilterTest.java](https://github.com/yahoo/parsec-libraries/blob/master/parsec-clients/src/test/java/com/yahoo/parsec/filters/RequestResponeLoggingFilterTest.java)
 - [NingJsonFormatter.java](https://github.com/yahoo/parsec-libraries/blob/master/parsec-clients/src/main/java/com/yahoo/parsec/filters/NingJsonFormatter.java)



Web Utilities
---------------

### RequestResponseLoggingFilter

Add `RequestResponseLoggingFilter` to your servlet filter chain to log reqeusts and responses received and sent by the service.

If you use a `ServletContextListener`, add the following code to the `contextInitialized` method: 


```
@Override
public void contextInitialized(ServletContextEvent sce) {
    //..omit unrelated code

    ServletContext servletContext = sce.getServletContext();
    FilterRegistration.Dynamic loggingFilter = servletContext.addFilter("RequestResponseLoggingFilter",
            RequestResponseLoggingFilter.class);

    loggingFilter.setInitParameter("formatter-classname", "com.yahoo.parsec.web.JsonFormatter");
    loggingFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/endpointUri/*");
}
```    

