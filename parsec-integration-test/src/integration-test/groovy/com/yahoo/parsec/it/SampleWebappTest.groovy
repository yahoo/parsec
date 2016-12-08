package com.yahoo.parsec.it

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

class SampleWebappTest extends Specification {
    def resp
    def restClient
    def pathUri

    def setup() {
        def testRequestUrl = System.getProperty("parsec_test.testRequestUrl","http://localhost:8080")
        restClient = new RESTClient(testRequestUrl)
    }

    @Test
    def "uri /api/static/swagger-ui should get code 200"() {
        given:
        pathUri = "/api/static/swagger-ui/"

        when:
        resp = restClient.get(path : pathUri)

        then:
        resp.status == 200
    }

    @Test
    def "uri /api/static/doc/sample_swagger.json should return code 200 and the content should be as expected"() {
        given:
        pathUri = "/api/static/doc/sample_swagger.json"
        def testSourceDir = System.getProperty("parsec_test.testDir");
        println(testSourceDir)
        def expectedResp = new File(testSourceDir + "/build/generated-resources/parsec/doc/sample_swagger.json").text;

        when:
        resp = restClient.get(path : pathUri, contentType: 'text/json')

        then:
        resp.status == 200
        def actualResp = resp.data.text
        actualResp == expectedResp

    }

    @Test
    def "uri /api/sample/v1/users/1234 should get code 200 and the content should be as expected"() {
        given:
        pathUri = "/api/sample/v1/users/1234"

        when:
        resp = restClient.get(path : pathUri)


        then:
        resp.status == 200
        def expectedResp = [age:0, name:"dm4"]
        def actualResp = resp.data
        actualResp == expectedResp
    }

    @Test
    def "post to /api/sample/v1/users with age=0, name='test' should get code 200"() {
        given:
        pathUri = "/api/sample/v1/users"
        def body = [age: 0, name: "test", salary: "123456.00"]
        restClient.parser.'application/json' = restClient.parser.'text/json'

        when:
        resp = restClient.post(path : pathUri,
                contentType: "application/json",
                requestContentType: ContentType.JSON,
                body: body
        )

        then:
        resp.status == 200
        def expectedResp = "Hello test!\n";
        def actualResp = resp.data.text;

        actualResp == expectedResp
    }

    @Test
    def "post to /api/sample/v1/users with name < 3 chars or > 5 chars should get code 400"() {
        given:
        pathUri = "/api/sample/v1/users"
        def body = [age: 0, name: nameVar]

        when:
        restClient.post(path : pathUri,
                contentType: "application/json",
                requestContentType: ContentType.JSON,
                body: body
        )

        then:
        HttpResponseException exp = thrown()
        exp.statusCode == 400

        where:
        description              | nameVar
        "name less than 2 chars" | "bo"
        "name exceeds 5 chars"   | "a very long name"
    }

    @Test
    def "post to /api/sample/v1/users with occupation < 4 chars should get code 400"() {
        given:
        pathUri = "/api/sample/v1/users"
        def body = [age: 0, name: "jane", occupation: "foo"]

        when:
        restClient.post(path : pathUri,
                contentType: "application/json",
                requestContentType: ContentType.JSON,
                body: body
        )

        then:
        HttpResponseException exp = thrown()
        exp.statusCode == 400
    }

    @Test
    def "put to /api/sample/v1/users/001 with occupation less than 4 chars should get code 400"(){
        given:
        pathUri = "/api/sample/v1/users/001"
        def body = ["occupation": "foo"];

        when:
        restClient.put(path : pathUri,
                contentType: "application/json",
                requestContentType: ContentType.JSON,
                body: body
        )

        then:
        HttpResponseException exp = thrown()
        exp.statusCode == 400
    }

    @Test
    def "put to /api/sample/v1/users/001 without occupation should get code 400"(){
        given:
        pathUri = "/api/sample/v1/users/001"
        def body = ["name": "jane"];
        restClient.parser.'application/json' = restClient.parser.'text/json'

        when:
        resp = restClient.put(path : pathUri,
                contentType: "application/json",
                requestContentType: ContentType.JSON,
                body: body
        )

        then:
        HttpResponseException exp = thrown()
        exp.statusCode == 400

        //TODO: figure out what the actual error message is
//        def actualResp = IOUtils.toString(exp.getResponse().data)
//        def actualResp = IOUtils.toString((InputStream)exp.getResponse().getData())
//        def expectedResp =
//                ["error":
//                         [
//                                 "code":0,
//                                 "message":"constraint violation validate error",
//                                 "detail":[
//                                         [
//                                                 "type":"validationError",
//                                                 "message":"may not be null","messageTemplate":"{javax.validation.constraints.NotNull.message}",
//                                                 "path":"SampleResources.putUser.namedUser.occupation"
//                                         ]
//                                 ],
//                         ]
//                ]
//        new JsonSlurper().setType(RELAX).parseText(actualResp) == expectedResp
    }


    @Test
    def "put to /api/sample/v1/users/001 without name and with occupation more than 4 chars should get code 200"(){
        given:
        pathUri = "/api/sample/v1/users/001"
        def body = ["occupation": "coder"];
        restClient.parser.'application/json' = restClient.parser.'text/json'

        when:
        resp = restClient.put(path : pathUri,
                contentType: "application/json",
                requestContentType: ContentType.JSON,
                body: body
        )

        then:
        resp.status == 200
    }

    @Test
    def "post to /api/sample/v1/users with not null id should get code 400"() {
        given:
        pathUri = "/api/sample/v1/users"
        def body = [age: 0, name: "test", id: "12345"]
        restClient.parser.'application/json' = restClient.parser.'text/json'

        when:
        resp = restClient.post(path : pathUri,
                contentType: "application/json",
                requestContentType: ContentType.JSON,
                body: body
        )

        then:
        HttpResponseException exp = thrown()
        exp.statusCode == 400
    }

    @Test
    @Unroll
    def "put to /api/sample/v1/users with salary=#salaryValue exceed digit should get code 400"(){
        given:
        pathUri = "/api/sample/v1/users"
        def body = [age: 0, name: "test", salary: salaryValue]
        restClient.parser.'application/json' = restClient.parser.'text/json'

        when:
        resp = restClient.post(path : pathUri,
                contentType: "application/json",
                requestContentType: ContentType.JSON,
                body: body
        )

        then:
        HttpResponseException exp = thrown()
        exp.statusCode == 400

        where:
        salaryValue << [ "99999.123", "999999999.00" ]
    }

}