API Endpoint Definition
=======================

Parsec integrates RDL, Swagger, and gradle jetty plugin, allowing users
to test web applications on local development machines.

In order to make endpoints across local Jetty, Swagger JSON schema, and
Production Jetty consistent, you will need to adjust some settings in
project pom and RDL files. Doing so also ensures compatibility with both
Yinst based and Manhattan environments.

Understanding related settings about API endpoint
-------------------------------------------------

You **Don't** need to understand this section if you are deploying a web
application to Manhattan

-   There are four settings you need to care about if you would like to
    change the root path of API endpoint:
    -   **artifact id** (pom): "artifactId" in pom
        -   The project name, .i.e: "echo"
    -   **final name** (pom): "finalName" in build section of pom
        -   The name of war.
        -   The default value will be the same with {artifact id}
        -   We setting as "api" by default, you could modify as you like.
        -   This name will become the first path segment of API
            endpoints in runtime enviornment by default
        -   End point example: /{pom.finalName}/echo/v1/users
    -   **webapp root path** (pom): "parsec.webapp\_root\_path" in pom's
        property
        -   This is just like contextPath setting in Jetty, but only
            influences local Jetty and Swagger JSON schema
        -   The default value is "/api"
        -   End point example: {parsec.webapp\_root\_path}/echo/v1/users

