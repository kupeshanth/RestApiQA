# REST API Testing — Complete Reference Guide
## Java + RestAssured + TestNG | From Zero to Interview Ready

---

## TABLE OF CONTENTS

1. [Prerequisites & Installation](#1-prerequisites--installation)
2. [Project Setup](#2-project-setup)
3. [Core Concepts](#3-core-concepts)
4. [All HTTP Methods — Every Possible Scenario](#4-all-http-methods--every-possible-scenario)
5. [All Status Codes & When to Expect Them](#5-all-status-codes--when-to-expect-them)
6. [Authentication — Every Type](#6-authentication--every-type)
7. [Request Building — Every Option](#7-request-building--every-option)
8. [Assertions — Every Pattern](#8-assertions--every-pattern)
9. [Advanced Techniques](#9-advanced-techniques)
10. [Data-Driven Testing](#10-data-driven-testing)
11. [Negative & Edge Case Testing](#11-negative--edge-case-testing)
12. [Error Handling in Tests](#12-error-handling-in-tests)
13. [All Maven Commands](#13-all-maven-commands)
14. [Troubleshooting — Every Common Error](#14-troubleshooting--every-common-error)
15. [Interview Checklist](#15-interview-checklist)

---

## 1. Prerequisites & Installation

### Check if Java is installed
```bash
java -version
```
**Expected:** `openjdk version "11.x.x"` or higher  
**If missing:** Download JDK 11+ from https://adoptium.net/

### Check if Maven is installed
```bash
mvn -version
```
**Expected:** `Apache Maven 3.x.x`  
**If missing — Option A (download locally into project):**
```bash
curl -L -o maven.zip "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
# On Windows with unzip available:
unzip maven.zip -d .
# Then run tests with:
./apache-maven-3.9.6/bin/mvn test
```
**If missing — Option B (global install via Chocolatey on Windows, run PowerShell as Admin):**
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
choco install maven -y
# Close and reopen terminal, then:
mvn -version
```

### Check JAVA_HOME is set (required for Maven)
```bash
echo $JAVA_HOME          # Mac/Linux
echo %JAVA_HOME%         # Windows CMD
echo $env:JAVA_HOME      # Windows PowerShell
```
**If empty, set it:**
```powershell
# Windows PowerShell (temporary, current session only):
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-11.x.x"
```

---

## 2. Project Setup

### Folder Structure (create this every time)
```
your-project/
├── pom.xml
└── src/
    └── test/
        ├── java/
        │   ├── base/
        │   │   └── BaseTest.java
        │   ├── tests/
        │   │   ├── GetTests.java
        │   │   ├── PostTests.java
        │   │   ├── PutTests.java
        │   │   ├── DeleteTests.java
        │   │   ├── AuthTests.java
        │   │   ├── ChainedTests.java
        │   │   └── DataDrivenTests.java
        │   └── utils/
        │       └── ApiConstants.java
        └── resources/
            ├── testng.xml
            └── schemas/
                └── response-schema.json
```

### Create folders (Windows PowerShell)
```powershell
mkdir -Force src\test\java\base
mkdir -Force src\test\java\tests
mkdir -Force src\test\java\utils
mkdir -Force src\test\resources\schemas
```

### pom.xml — Complete with all dependencies
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.restapi.practice</groupId>
    <artifactId>rest-api-tests</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- RestAssured: sends HTTP requests and asserts responses -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>5.3.1</version>
            <scope>test</scope>
        </dependency>

        <!-- JSON Schema Validator -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>json-schema-validator</artifactId>
            <version>5.3.1</version>
            <scope>test</scope>
        </dependency>

        <!-- TestNG: test runner and reporting -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.8.0</version>
            <scope>test</scope>
        </dependency>

        <!-- Jackson: for serializing Java objects to JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
                    </suiteXmlFiles>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### ApiConstants.java — All shared values
```java
package utils;

public class ApiConstants {
    // Base URL — change this to match the API you are testing
    public static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    // Endpoints
    public static final String POSTS_ENDPOINT    = "/posts";
    public static final String USERS_ENDPOINT    = "/users";
    public static final String COMMENTS_ENDPOINT = "/comments";

    // Auth (populate these when the API requires authentication)
    public static final String API_KEY     = "your-api-key-here";
    public static final String AUTH_TOKEN  = "your-bearer-token-here";
    public static final String USERNAME    = "your-username";
    public static final String PASSWORD    = "your-password";

    // Timeouts
    public static final int RESPONSE_TIMEOUT_MS = 3000;  // 3 seconds max
}
```

### BaseTest.java — Complete setup with all options
```java
package base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;
import utils.ApiConstants;

import static org.hamcrest.Matchers.lessThan;

public class BaseTest {

    // Shared request spec — apply to every request with .spec(requestSpec)
    protected RequestSpecification requestSpec;

    // Shared response spec — apply to every response with .spec(responseSpec)
    protected ResponseSpecification responseSpec;

    @BeforeClass
    public void setUp() {
        // Set base URL — all tests use this + their endpoint path
        RestAssured.baseURI = ApiConstants.BASE_URL;

        // Print full request + response only when a test FAILS
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Build reusable request spec
        requestSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            // Uncomment whichever auth the API needs:
            // .addHeader("Authorization", "Bearer " + ApiConstants.AUTH_TOKEN)
            // .addHeader("x-api-key", ApiConstants.API_KEY)
            // .addHeader("x-api-version", "2")
            .build();

        // Build reusable response spec (common assertions for all responses)
        responseSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan((long) ApiConstants.RESPONSE_TIMEOUT_MS))
            .build();
    }
}
```

### testng.xml — Full suite config
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">

<suite name="REST API Test Suite" verbose="1" parallel="false">

    <test name="Smoke Tests" groups="smoke">
        <classes>
            <class name="tests.GetTests"/>
        </classes>
    </test>

    <test name="GET Tests">
        <classes>
            <class name="tests.GetTests"/>
        </classes>
    </test>

    <test name="POST Tests">
        <classes>
            <class name="tests.PostTests"/>
        </classes>
    </test>

    <test name="PUT and PATCH Tests">
        <classes>
            <class name="tests.PutTests"/>
        </classes>
    </test>

    <test name="DELETE Tests">
        <classes>
            <class name="tests.DeleteTests"/>
        </classes>
    </test>

    <test name="Auth Tests">
        <classes>
            <class name="tests.AuthTests"/>
        </classes>
    </test>

    <test name="Chained E2E Tests">
        <classes>
            <class name="tests.ChainedTests"/>
        </classes>
    </test>

    <test name="Data Driven Tests">
        <classes>
            <class name="tests.DataDrivenTests"/>
        </classes>
    </test>

</suite>
```

---

## 3. Core Concepts

### The Given / When / Then Pattern
```
given()   →   set up request (headers, body, params, auth)
  when()  →   send the HTTP request (GET, POST, PUT, PATCH, DELETE)
  .then() →   assert the response (status, body, headers, time)
```

### Imports — Always Add These
```java
import static io.restassured.RestAssured.*;           // given(), when(), then()
import static org.hamcrest.Matchers.*;                // equalTo(), notNullValue(), etc.
import io.restassured.http.ContentType;               // ContentType.JSON
import io.restassured.response.Response;              // extract full response
import org.testng.annotations.Test;                   // @Test annotation
import org.testng.annotations.DataProvider;           // @DataProvider
import org.testng.asserts.SoftAssert;                 // soft assertions
import static io.restassured.module.jsv.JsonSchemaValidator.*; // schema validation
```

---

## 4. All HTTP Methods — Every Possible Scenario

### GET — All Scenarios

```java
// ── SCENARIO 1: GET all resources (happy path) ──────────────────────────────
@Test
public void getAllResources_happyPath() {
    given()
    .when()
        .get("/posts")
    .then()
        .statusCode(200)
        .body("size()", greaterThan(0))        // list is not empty
        .body("size()", equalTo(100))          // exact count
        .body("[0].id", notNullValue())        // first item has id
        .body("[0].title", not(emptyString())) // first item has title
        .contentType(ContentType.JSON)
        .time(lessThan(3000L));               // under 3 seconds
}

// ── SCENARIO 2: GET single resource by ID (happy path) ──────────────────────
@Test
public void getResourceById_happyPath() {
    given()
        .pathParam("id", 1)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(200)
        .body("id", equalTo(1))
        .body("title", not(emptyString()))
        .body("userId", notNullValue());
}

// ── SCENARIO 3: GET with query parameters ───────────────────────────────────
@Test
public void getWithQueryParams_filtersCorrectly() {
    given()
        .queryParam("userId", 1)     // → /posts?userId=1
    .when()
        .get("/posts")
    .then()
        .statusCode(200)
        .body("size()", equalTo(10))
        .body("userId", everyItem(equalTo(1)));   // every item matches filter
}

// ── SCENARIO 4: GET with multiple query parameters ───────────────────────────
@Test
public void getWithMultipleQueryParams() {
    given()
        .queryParam("userId", 1)
        .queryParam("_limit", 5)      // → /posts?userId=1&_limit=5
    .when()
        .get("/posts")
    .then()
        .statusCode(200)
        .body("size()", lessThanOrEqualTo(5));
}

// ── SCENARIO 5: GET non-existent resource → 404 ─────────────────────────────
@Test
public void getById_notFound_returns404() {
    given()
        .pathParam("id", 99999)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(404);
}

// ── SCENARIO 6: GET with invalid ID format ──────────────────────────────────
// Some APIs return 400 Bad Request for non-numeric IDs
@Test
public void getById_invalidFormat() {
    given()
    .when()
        .get("/posts/abc")            // non-numeric ID
    .then()
        .statusCode(anyOf(equalTo(400), equalTo(404)));  // accept either
}

// ── SCENARIO 7: GET and verify response headers ──────────────────────────────
@Test
public void get_verifyResponseHeaders() {
    given()
    .when()
        .get("/posts/1")
    .then()
        .statusCode(200)
        .header("Content-Type", containsString("application/json"))
        .header("Cache-Control", notNullValue());
}

// ── SCENARIO 8: GET and validate JSON schema ─────────────────────────────────
@Test
public void get_validateJsonSchema() {
    given()
        .pathParam("id", 1)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
}

// ── SCENARIO 9: GET response time check ─────────────────────────────────────
@Test
public void get_responseTimeUnderThreshold() {
    given()
    .when()
        .get("/posts")
    .then()
        .statusCode(200)
        .time(lessThan(3000L));   // milliseconds
}

// ── SCENARIO 10: GET and extract value for reuse ────────────────────────────
@Test
public void get_extractValueForReuse() {
    String title = given()
        .pathParam("id", 1)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(200)
        .extract().path("title");

    System.out.println("Title extracted: " + title);
    Assert.assertFalse(title.isEmpty(), "Title should not be empty");
}
```

---

### POST — All Scenarios

```java
// ── SCENARIO 1: POST valid data → 201 Created ────────────────────────────────
@Test
public void post_validData_returns201() {
    String body = "{ \"title\": \"Test\", \"body\": \"Content\", \"userId\": 1 }";

    given()
        .spec(requestSpec)
        .body(body)
    .when()
        .post("/posts")
    .then()
        .statusCode(201)
        .body("id", notNullValue())          // server assigned an ID
        .body("title", equalTo("Test"))
        .body("userId", equalTo(1));
}

// ── SCENARIO 2: POST and capture new resource ID ─────────────────────────────
@Test
public void post_captureNewId() {
    String body = "{ \"title\": \"New Post\", \"body\": \"Body\", \"userId\": 1 }";

    int newId = given()
        .spec(requestSpec)
        .body(body)
    .when()
        .post("/posts")
    .then()
        .statusCode(201)
        .extract().path("id");

    Assert.assertTrue(newId > 0, "ID should be positive");
    System.out.println("New resource created with ID: " + newId);
}

// ── SCENARIO 3: POST with missing required fields → 400 Bad Request ──────────
// Real APIs validate required fields. JSONPlaceholder doesn't, but real ones do.
@Test
public void post_missingRequiredField_returns400() {
    String incompleteBody = "{ \"body\": \"No title here\" }";  // missing title

    given()
        .spec(requestSpec)
        .body(incompleteBody)
    .when()
        .post("/posts")
    .then()
        .statusCode(anyOf(equalTo(400), equalTo(201)));  // adjust to your API
}

// ── SCENARIO 4: POST with empty body ─────────────────────────────────────────
@Test
public void post_emptyBody() {
    given()
        .spec(requestSpec)
        .body("{}")
    .when()
        .post("/posts")
    .then()
        .statusCode(anyOf(equalTo(400), equalTo(201)));
}

// ── SCENARIO 5: POST with duplicate data → 409 Conflict ─────────────────────
// For APIs that don't allow duplicates (e.g. duplicate email)
@Test
public void post_duplicateData_returns409() {
    String body = "{ \"email\": \"already@exists.com\", \"name\": \"Test\" }";

    given()
        .spec(requestSpec)
        .body(body)
    .when()
        .post("/users")
    .then()
        .statusCode(anyOf(equalTo(409), equalTo(422)));  // Conflict or Unprocessable
}

// ── SCENARIO 6: POST without Content-Type header ─────────────────────────────
// Some APIs reject requests without Content-Type
@Test
public void post_withoutContentType() {
    given()
        .body("{ \"title\": \"Test\" }")   // no .contentType() set
    .when()
        .post("/posts")
    .then()
        .statusCode(anyOf(equalTo(201), equalTo(400), equalTo(415)));  // 415 = Unsupported Media Type
}

// ── SCENARIO 7: POST and verify response Content-Type ────────────────────────
@Test
public void post_responseIsJson() {
    given()
        .spec(requestSpec)
        .body("{ \"title\": \"Test\", \"body\": \"Content\", \"userId\": 1 }")
    .when()
        .post("/posts")
    .then()
        .statusCode(201)
        .contentType(containsString("application/json"));
}
```

---

### PUT — All Scenarios

```java
// ── SCENARIO 1: PUT full update → 200 OK ────────────────────────────────────
// PUT replaces the ENTIRE resource — you must send ALL fields
@Test
public void put_fullUpdate_returns200() {
    String fullBody = "{ \"id\": 1, \"title\": \"New Title\", \"body\": \"New Body\", \"userId\": 1 }";

    given()
        .spec(requestSpec)
        .body(fullBody)
        .pathParam("id", 1)
    .when()
        .put("/posts/{id}")
    .then()
        .statusCode(200)
        .body("title", equalTo("New Title"))
        .body("body", equalTo("New Body"));
}

// ── SCENARIO 2: PUT on non-existent resource → 404 ──────────────────────────
@Test
public void put_nonExistentResource_returns404() {
    String body = "{ \"id\": 99999, \"title\": \"Test\", \"body\": \"Test\", \"userId\": 1 }";

    given()
        .spec(requestSpec)
        .body(body)
        .pathParam("id", 99999)
    .when()
        .put("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(200), equalTo(404)));   // JSONPlaceholder returns 500, real APIs return 404
}

// ── SCENARIO 3: PATCH partial update → 200 OK ───────────────────────────────
// PATCH updates ONLY the fields you send
@Test
public void patch_partialUpdate_returns200() {
    String partialBody = "{ \"title\": \"Only Title Changed\" }";

    given()
        .spec(requestSpec)
        .body(partialBody)
        .pathParam("id", 1)
    .when()
        .patch("/posts/{id}")
    .then()
        .statusCode(200)
        .body("id", equalTo(1))
        .body("title", equalTo("Only Title Changed"));
}

// ── SCENARIO 4: PUT with invalid data → 400 Bad Request ─────────────────────
@Test
public void put_invalidData_returns400() {
    String invalidBody = "{ \"userId\": \"not-a-number\" }";  // wrong type

    given()
        .spec(requestSpec)
        .body(invalidBody)
        .pathParam("id", 1)
    .when()
        .put("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(400), equalTo(200)));  // depends on API strictness
}
```

---

### DELETE — All Scenarios

```java
// ── SCENARIO 1: DELETE existing resource → 200 or 204 ───────────────────────
@Test
public void delete_existingResource() {
    given()
        .pathParam("id", 1)
    .when()
        .delete("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(200), equalTo(204)));
        // 200 = OK with body,  204 = No Content (no body)
}

// ── SCENARIO 2: DELETE non-existent resource → 404 ──────────────────────────
@Test
public void delete_nonExistentResource_returns404() {
    given()
        .pathParam("id", 99999)
    .when()
        .delete("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(404), equalTo(200)));  // JSONPlaceholder returns 200
}

// ── SCENARIO 3: DELETE and verify resource is gone (chained) ─────────────────
@Test
public void delete_thenVerifyGone() {
    // Step 1: Delete
    given()
        .pathParam("id", 1)
    .when()
        .delete("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(200), equalTo(204)));

    // Step 2: Try to GET the deleted resource — should be 404
    // Note: JSONPlaceholder doesn't actually delete, so this is for real APIs
    given()
        .pathParam("id", 1)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(200), equalTo(404)));  // 404 on real APIs
}

// ── SCENARIO 4: DELETE without auth → 401 Unauthorized ──────────────────────
// For protected APIs — without token, deletion should be rejected
@Test
public void delete_withoutAuth_returns401() {
    given()
        // intentionally NOT adding auth header
        .pathParam("id", 1)
    .when()
        .delete("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(401), equalTo(403), equalTo(200)));
}
```

---

## 5. All Status Codes & When to Expect Them

```java
// ── 2xx SUCCESS ──────────────────────────────────────────────────────────────
.statusCode(200)   // OK — GET, PUT, PATCH succeeded
.statusCode(201)   // Created — POST created a new resource
.statusCode(202)   // Accepted — request accepted, processing async (background jobs)
.statusCode(204)   // No Content — DELETE succeeded, no body returned

// ── 3xx REDIRECT ─────────────────────────────────────────────────────────────
.statusCode(301)   // Moved Permanently — URL has changed forever
.statusCode(302)   // Found — temporary redirect
// RestAssured follows redirects automatically by default

// ── 4xx CLIENT ERRORS ────────────────────────────────────────────────────────
.statusCode(400)   // Bad Request — your request body/params are wrong
.statusCode(401)   // Unauthorized — missing or invalid authentication
.statusCode(403)   // Forbidden — authenticated but not allowed
.statusCode(404)   // Not Found — resource doesn't exist
.statusCode(405)   // Method Not Allowed — e.g. DELETE not supported on this endpoint
.statusCode(409)   // Conflict — duplicate resource (e.g. email already exists)
.statusCode(415)   // Unsupported Media Type — wrong Content-Type header
.statusCode(422)   // Unprocessable Entity — validation failed (e.g. invalid email format)
.statusCode(429)   // Too Many Requests — rate limit exceeded

// ── 5xx SERVER ERRORS ────────────────────────────────────────────────────────
.statusCode(500)   // Internal Server Error — server crashed
.statusCode(502)   // Bad Gateway — upstream server problem
.statusCode(503)   // Service Unavailable — server overloaded or down
.statusCode(504)   // Gateway Timeout — upstream server timed out

// ── ACCEPT MULTIPLE STATUS CODES ─────────────────────────────────────────────
.statusCode(anyOf(equalTo(200), equalTo(204)))     // DELETE can return either
.statusCode(anyOf(equalTo(400), equalTo(422)))     // validation errors vary by API
```

---

## 6. Authentication — Every Type

### Type 1: No Authentication (public APIs)
```java
// Nothing special needed — just send the request
given()
.when()
    .get("/posts")
.then()
    .statusCode(200);
```

### Type 2: API Key in Header
```java
given()
    .header("x-api-key", "your-api-key-here")
.when()
    .get("/posts")
.then()
    .statusCode(200);
```

### Type 3: API Key in Query Parameter
```java
given()
    .queryParam("api_key", "your-api-key-here")    // → /posts?api_key=xxx
.when()
    .get("/posts")
.then()
    .statusCode(200);
```

### Type 4: Bearer Token
```java
given()
    .header("Authorization", "Bearer your-token-here")
.when()
    .get("/posts")
.then()
    .statusCode(200);
```

### Type 5: Basic Auth (username + password)
```java
given()
    .auth().basic("username", "password")
.when()
    .get("/posts")
.then()
    .statusCode(200);
```

### Type 6: OAuth2 — Login to get token, then use it
```java
// Step 1: Login and capture token
String token = given()
    .contentType(ContentType.JSON)
    .body("{ \"username\": \"admin\", \"password\": \"secret\" }")
.when()
    .post("/auth/login")
.then()
    .statusCode(200)
    .extract().path("token");   // ← capture token from login response

System.out.println("Token received: " + token);

// Step 2: Use token in all subsequent requests
given()
    .header("Authorization", "Bearer " + token)
.when()
    .get("/protected-resource")
.then()
    .statusCode(200);
```

### Type 7: Add auth to BaseTest so all tests use it automatically
```java
// In BaseTest.java setUp():
requestSpec = new RequestSpecBuilder()
    .setContentType(ContentType.JSON)
    .addHeader("Authorization", "Bearer " + fetchToken())  // call login once
    .build();

private String fetchToken() {
    return given()
        .contentType(ContentType.JSON)
        .body("{ \"username\": \"admin\", \"password\": \"secret\" }")
    .when()
        .post("/auth/login")
    .then()
        .statusCode(200)
        .extract().path("token");
}
```

### Testing that auth FAILS correctly
```java
// Test: missing token → 401
@Test
public void noToken_returns401() {
    given()
        // no Authorization header
    .when()
        .get("/protected-resource")
    .then()
        .statusCode(401);
}

// Test: wrong token → 401 or 403
@Test
public void wrongToken_returns401or403() {
    given()
        .header("Authorization", "Bearer invalid-token-xyz")
    .when()
        .get("/protected-resource")
    .then()
        .statusCode(anyOf(equalTo(401), equalTo(403)));
}

// Test: expired token → 401
@Test
public void expiredToken_returns401() {
    String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired";
    given()
        .header("Authorization", "Bearer " + expiredToken)
    .when()
        .get("/protected-resource")
    .then()
        .statusCode(401);
}
```

---

## 7. Request Building — Every Option

```java
// ── PATH PARAMETERS ──────────────────────────────────────────────────────────
given()
    .pathParam("id", 1)              // replaces {id} in URL
    .pathParam("type", "posts")      // replaces {type} in URL
.when()
    .get("/{type}/{id}");            // → /posts/1

// ── QUERY PARAMETERS ─────────────────────────────────────────────────────────
given()
    .queryParam("userId", 1)         // → ?userId=1
    .queryParam("_limit", 10)        // → ?userId=1&_limit=10
    .queryParam("_page", 2)          // → ?userId=1&_limit=10&_page=2
.when()
    .get("/posts");

// ── HEADERS ──────────────────────────────────────────────────────────────────
given()
    .header("Authorization", "Bearer token")
    .header("x-api-key", "key123")
    .header("Accept-Language", "en-US")
    .header("x-correlation-id", "test-run-001")    // for tracing in logs
.when()
    .get("/posts");

// ── REQUEST BODY — String ─────────────────────────────────────────────────────
given()
    .contentType(ContentType.JSON)
    .body("{ \"title\": \"Test\", \"body\": \"Content\", \"userId\": 1 }")
.when()
    .post("/posts");

// ── REQUEST BODY — Java Map ───────────────────────────────────────────────────
// Cleaner than building strings — no escaping needed
Map<String, Object> body = new HashMap<>();
body.put("title", "Test Post");
body.put("body", "Content here");
body.put("userId", 1);

given()
    .contentType(ContentType.JSON)
    .body(body)
.when()
    .post("/posts");

// ── REQUEST LOGGING ───────────────────────────────────────────────────────────
given()
    .log().all()             // log entire request
    .log().headers()         // log only request headers
    .log().body()            // log only request body
.when()
    .get("/posts");

// ── RESPONSE LOGGING ─────────────────────────────────────────────────────────
.then()
    .log().all()             // log entire response
    .log().ifError()         // log only on error
    .log().ifValidationFails() // log only if assertion fails
    .log().status()          // log only status code
    .log().headers()         // log only response headers
    .log().body()            // log only response body
```

---

## 8. Assertions — Every Pattern

```java
// ── STATUS CODE ───────────────────────────────────────────────────────────────
.statusCode(200)
.statusCode(not(500))                            // must NOT be 500
.statusCode(anyOf(equalTo(200), equalTo(201)))   // accept either

// ── BODY — SINGLE FIELD ───────────────────────────────────────────────────────
.body("id", equalTo(1))                          // exact integer match
.body("title", equalTo("Hello"))                 // exact string match
.body("active", equalTo(true))                   // boolean match
.body("price", equalTo(9.99f))                   // float match
.body("id", notNullValue())                      // field exists and not null
.body("id", nullValue())                         // field is null
.body("title", not(emptyString()))               // string not empty
.body("title", containsString("Hello"))          // string contains substring
.body("email", matchesPattern(".*@.*\\..*"))     // regex match
.body("count", greaterThan(0))                   // number comparison
.body("count", greaterThanOrEqualTo(1))
.body("count", lessThan(100))
.body("count", lessThanOrEqualTo(100))
.body("count", between(1, 100))                  // inclusive range

// ── BODY — ARRAYS ─────────────────────────────────────────────────────────────
.body("size()", equalTo(100))                    // array has exactly 100 items
.body("size()", greaterThan(0))                  // array is not empty
.body("", hasSize(100))                          // alternative syntax
.body("[0].id", equalTo(1))                      // first item's id field
.body("[0].title", not(emptyString()))           // first item's title
.body("id", hasItem(5))                          // array contains value 5
.body("userId", everyItem(equalTo(1)))           // every item's userId equals 1
.body("title", hasItems("Title A", "Title B"))   // array contains these items

// ── BODY — NESTED JSON ────────────────────────────────────────────────────────
// For response like: { "user": { "address": { "city": "London" } } }
.body("user.address.city", equalTo("London"))    // dot notation for nesting
.body("user.roles[0]", equalTo("admin"))         // nested array item

// ── HEADERS ───────────────────────────────────────────────────────────────────
.contentType("application/json")                 // Content-Type header
.contentType(ContentType.JSON)                   // same, using enum
.header("Content-Type", containsString("application/json"))
.header("Cache-Control", notNullValue())
.header("X-Custom-Header", equalTo("expected-value"))

// ── RESPONSE TIME ─────────────────────────────────────────────────────────────
.time(lessThan(3000L))                           // under 3 seconds (milliseconds)
.time(lessThan(1000L))                           // under 1 second
.time(greaterThan(100L))                         // took at least 100ms (sanity check)

// ── SCHEMA VALIDATION ─────────────────────────────────────────────────────────
.body(matchesJsonSchemaInClasspath("schemas/post-schema.json"))

// ── USING RESPONSE SPEC (predefined assertions) ───────────────────────────────
.spec(responseSpec)                              // applies responseSpec from BaseTest
.statusCode(200)                                 // add specific assertions after spec
```

---

## 9. Advanced Techniques

### Extract Full Response Object
```java
Response response = given()
    .pathParam("id", 1)
.when()
    .get("/posts/{id}")
.then()
    .statusCode(200)
    .extract().response();

int    id     = response.path("id");
String title  = response.path("title");
int    status = response.statusCode();
String header = response.header("Content-Type");
String body   = response.body().asString();
```

### Chained Requests — POST → GET → DELETE
```java
@Test
public void fullCrudWorkflow() {
    // 1. CREATE
    int newId = given()
        .spec(requestSpec)
        .body("{ \"title\": \"New\", \"body\": \"Content\", \"userId\": 1 }")
    .when()
        .post("/posts")
    .then()
        .statusCode(201)
        .extract().path("id");

    // 2. READ
    given()
        .pathParam("id", newId)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(200), equalTo(404)));

    // 3. UPDATE
    given()
        .spec(requestSpec)
        .body("{ \"title\": \"Updated\" }")
        .pathParam("id", newId)
    .when()
        .patch("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(200), equalTo(404)));

    // 4. DELETE
    given()
        .pathParam("id", newId)
    .when()
        .delete("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(200), equalTo(204), equalTo(404)));
}
```

### Soft Assertions — See All Failures at Once
```java
@Test
public void validateAllFieldsWithSoftAssert() {
    Response response = given()
        .pathParam("id", 1)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(200)
        .extract().response();

    SoftAssert soft = new SoftAssert();
    soft.assertEquals(response.statusCode(), 200,          "Status should be 200");
    soft.assertNotNull(response.path("id"),                "id should not be null");
    soft.assertFalse(((String)response.path("title")).isEmpty(), "title should not be empty");
    soft.assertEquals((int)response.path("userId"), 1,    "userId should be 1");
    soft.assertAll();   // NEVER forget this — or all failures are swallowed silently
}
```

### JSON Schema File
```json
// src/test/resources/schemas/post-schema.json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "id":     { "type": "integer" },
    "title":  { "type": "string"  },
    "body":   { "type": "string"  },
    "userId": { "type": "integer" }
  },
  "required": ["id", "title", "body", "userId"]
}
```

---

## 10. Data-Driven Testing

### DataProvider — Single Value
```java
@DataProvider(name = "validIds")
public Object[][] validIds() {
    return new Object[][] {
        { 1 }, { 2 }, { 5 }, { 10 }, { 100 }
    };
}

@Test(dataProvider = "validIds")
public void getById_validIds(int id) {
    given()
        .pathParam("id", id)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(200)
        .body("id", equalTo(id));
}
```

### DataProvider — Multiple Values Per Row
```java
@DataProvider(name = "postVariants")
public Object[][] postVariants() {
    return new Object[][] {
        // title,          body,          userId, expectedStatus
        { "Post A",      "Body A",         1,       201 },
        { "Post B",      "Body B",         2,       201 },
        { "Post C",      "Body C",         3,       201 },
    };
}

@Test(dataProvider = "postVariants")
public void createPost_variants(String title, String body, int userId, int expectedStatus) {
    String requestBody = String.format(
        "{ \"title\": \"%s\", \"body\": \"%s\", \"userId\": %d }",
        title, body, userId
    );

    given()
        .spec(requestSpec)
        .body(requestBody)
    .when()
        .post("/posts")
    .then()
        .statusCode(expectedStatus);
}
```

### DataProvider — Negative Cases
```java
@DataProvider(name = "invalidIds")
public Object[][] invalidIds() {
    return new Object[][] {
        { 0     },    // zero
        { -1    },    // negative
        { 99999 },    // does not exist
    };
}

@Test(dataProvider = "invalidIds")
public void getById_invalidIds_returns404(int id) {
    given()
        .pathParam("id", id)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(400), equalTo(404)));
}
```

---

## 11. Negative & Edge Case Testing

```java
// ── BOUNDARY VALUES ───────────────────────────────────────────────────────────
// Test the minimum and maximum acceptable values

// Minimum valid ID
given().pathParam("id", 1).when().get("/posts/{id}").then().statusCode(200);

// Maximum valid ID  
given().pathParam("id", 100).when().get("/posts/{id}").then().statusCode(200);

// Just outside boundary
given().pathParam("id", 0).when().get("/posts/{id}").then().statusCode(anyOf(equalTo(400), equalTo(404)));
given().pathParam("id", 101).when().get("/posts/{id}").then().statusCode(anyOf(equalTo(200), equalTo(404)));

// ── EMPTY AND NULL VALUES ─────────────────────────────────────────────────────
// Empty string body
given().spec(requestSpec).body("").when().post("/posts")
    .then().statusCode(anyOf(equalTo(400), equalTo(201)));

// Null fields in JSON
given().spec(requestSpec)
    .body("{ \"title\": null, \"body\": \"Content\", \"userId\": 1 }")
    .when().post("/posts")
    .then().statusCode(anyOf(equalTo(400), equalTo(201)));

// ── SPECIAL CHARACTERS ────────────────────────────────────────────────────────
// SQL injection attempt — should be handled safely, not crash the server
given().spec(requestSpec)
    .body("{ \"title\": \"'; DROP TABLE posts; --\", \"body\": \"test\", \"userId\": 1 }")
    .when().post("/posts")
    .then().statusCode(anyOf(equalTo(201), equalTo(400)));  // must NOT return 500

// XSS attempt — server should sanitize or reject
given().spec(requestSpec)
    .body("{ \"title\": \"<script>alert('xss')</script>\", \"body\": \"test\", \"userId\": 1 }")
    .when().post("/posts")
    .then().statusCode(anyOf(equalTo(201), equalTo(400)));  // must NOT return 500

// ── LARGE PAYLOAD ─────────────────────────────────────────────────────────────
// Test how API handles very large input
String largeTitle = "A".repeat(10000);  // 10,000 character title
given().spec(requestSpec)
    .body("{ \"title\": \"" + largeTitle + "\", \"body\": \"test\", \"userId\": 1 }")
    .when().post("/posts")
    .then().statusCode(anyOf(equalTo(201), equalTo(400), equalTo(413)));  // 413 = Payload Too Large

// ── WRONG DATA TYPES ──────────────────────────────────────────────────────────
// userId as string instead of integer
given().spec(requestSpec)
    .body("{ \"title\": \"Test\", \"body\": \"test\", \"userId\": \"one\" }")
    .when().post("/posts")
    .then().statusCode(anyOf(equalTo(400), equalTo(201)));
```

---

## 12. Error Handling in Tests

### Handle Connection Errors
```java
@Test
public void handleConnectionError() {
    try {
        given()
        .when()
            .get("/posts")
        .then()
            .statusCode(200);
    } catch (Exception e) {
        System.err.println("Connection failed: " + e.getMessage());
        Assert.fail("API is not reachable: " + e.getMessage());
    }
}
```

### Expected Exception Testing
```java
// TestNG can verify that a specific exception IS thrown
@Test(expectedExceptions = AssertionError.class)
public void assertionShouldFail() {
    given()
    .when()
        .get("/posts/1")
    .then()
        .statusCode(999);  // intentionally wrong — expects AssertionError
}
```

### Retry on Flaky Tests (unstable network / rate limits)
```java
// In testng.xml, add retryAnalyzer:
// <test retry-analyzer="utils.RetryAnalyzer">

// RetryAnalyzer.java
package utils;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int MAX_RETRIES = 2;  // retry up to 2 times

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            System.out.println("Retrying test: " + result.getName() + " (attempt " + retryCount + ")");
            return true;
        }
        return false;
    }
}

// Use on specific tests:
@Test(retryAnalyzer = RetryAnalyzer.class)
public void flakyTest() { ... }
```

### Disable SSL Certificate Validation (self-signed certs in test environments)
```java
// In BaseTest.java setUp():
RestAssured.useRelaxedHTTPSValidation();

// Or per request:
given()
    .relaxedHTTPSValidation()
.when()
    .get("https://self-signed-cert-api.com/posts");
```

### Set Request Timeout
```java
// Fail if request takes longer than 5 seconds
given()
    .config(RestAssured.config()
        .httpClient(HttpClientConfig.httpClientConfig()
            .setParam("http.connection.timeout", 5000)
            .setParam("http.socket.timeout", 5000)))
.when()
    .get("/posts");
```

---

## 13. All Maven Commands

```bash
# ── RUNNING TESTS ─────────────────────────────────────────────────────────────

# Run all tests (uses testng.xml)
mvn test

# Run only one test CLASS
mvn test -Dtest=GetTests

# Run only one test METHOD
mvn test -Dtest=GetTests#getAllPosts

# Run multiple classes
mvn test -Dtest=GetTests,PostTests

# Run tests by group (must tag with @Test(groups="smoke") first)
mvn test -Dgroups=smoke

# Clean old results then run all tests
mvn clean test

# Run tests and skip compilation (faster, use only if no code changed)
mvn test -Dsurefire.failIfNoSpecifiedTests=false

# ── COMPILATION ───────────────────────────────────────────────────────────────

# Compile only (check for syntax errors without running tests)
mvn test-compile

# Clean all compiled files
mvn clean

# ── DEPENDENCIES ──────────────────────────────────────────────────────────────

# Download all dependencies defined in pom.xml
mvn dependency:resolve

# Show dependency tree (find version conflicts)
mvn dependency:tree

# ── REPORTS ───────────────────────────────────────────────────────────────────

# After mvn test, HTML report is at:
# target/surefire-reports/index.html
# Open in browser to see full pass/fail breakdown

# ── POWERSHELL SPECIFIC (Windows) ────────────────────────────────────────────

# Filter output to just show results (PowerShell)
./apache-maven-3.9.6/bin/mvn test 2>&1 | Select-String "Tests run|FAIL|ERROR|SUCCESS"

# If Maven installed globally:
mvn test 2>&1 | Select-String "Tests run|BUILD"

# ── RUNNING WITH LOCAL MAVEN (downloaded to project folder) ──────────────────
./apache-maven-3.9.6/bin/mvn test
./apache-maven-3.9.6/bin/mvn clean test
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests
```

---

## 14. Troubleshooting — Every Common Error

---

### ERROR: `mvn` not recognized
```
'mvn' is not recognized as the name of a cmdlet
```
**Cause:** Maven not installed or not on PATH  
**Fix:**
```powershell
# Option A: Use local Maven (downloaded to project folder)
./apache-maven-3.9.6/bin/mvn test

# Option B: Install globally
choco install maven -y
# Then close and reopen PowerShell
mvn -version
```

---

### ERROR: `java` not recognized
```
'java' is not recognized as an internal or external command
```
**Cause:** Java not installed  
**Fix:** Download from https://adoptium.net/ — choose JDK 11 LTS

---

### ERROR: Compilation failure
```
[ERROR] COMPILATION ERROR
[ERROR] cannot find symbol
```
**Cause:** Wrong import, typo in class/method name, or missing dependency  
**Fix checklist:**
1. Check all imports at the top of the file are correct
2. Ensure pom.xml has all required dependencies
3. Run `mvn dependency:resolve` to re-download libraries
4. Check for typos in class names — Java is case-sensitive

---

### ERROR: `Connection refused` or `UnknownHostException`
```
java.net.ConnectException: Connection refused
java.net.UnknownHostException: jsonplaceholder.typicode.com
```
**Cause:** No internet connection, wrong URL, or API server is down  
**Fix checklist:**
1. Check internet connection
2. Open the URL in a browser — does it load?
3. Verify `BASE_URL` in `ApiConstants.java` is correct (no trailing slash)
4. Check if you need VPN to reach the API

---

### ERROR: Status code mismatch
```
1 expectation failed.
Expected status code <200> but was <401>
```
**Cause:** Missing or invalid authentication  
**Fix checklist:**
1. Check if the endpoint requires a token
2. Verify the token is correct and not expired
3. Add auth to `requestSpec` in `BaseTest`:
```java
.addHeader("Authorization", "Bearer " + token)
```

---

### ERROR: `JSON path X doesn't match`
```
JSON path title doesn't match.
Expected: Hello
  Actual: World
```
**Cause:** API returned different data than expected  
**Fix checklist:**
1. Open the URL in Postman/browser — see what the actual response is
2. Update your assertion to match the actual value
3. Or check if the API has a bug — compare with API documentation

---

### ERROR: `No tests were found`
```
[WARNING] No tests were found
```
**Cause:** Test class/method not found, testng.xml misconfigured  
**Fix checklist:**
1. Verify the class name in `testng.xml` matches exactly (case-sensitive)
2. Ensure `@Test` annotation is on the method
3. Ensure test files are in `src/test/java/` (NOT `src/main/java/`)
4. Run `mvn test-compile` to check for compilation errors

---

### ERROR: `NullPointerException` in test
```
java.lang.NullPointerException at tests.GetTests.java:25
```
**Cause:** Trying to use a value that is null — often from `extract().path()` returning null  
**Fix:**
```java
// Problem: path might return null if field doesn't exist
String title = response.path("title");
System.out.println(title.length());  // NullPointerException if title is null

// Fix: null check before using
String title = response.path("title");
Assert.assertNotNull(title, "title field missing from response");
System.out.println(title.length());
```

---

### ERROR: SSL Certificate error
```
javax.net.ssl.SSLHandshakeException: PKIX path building failed
```
**Cause:** API uses a self-signed certificate (common in test environments)  
**Fix:**
```java
// In BaseTest.java setUp():
RestAssured.useRelaxedHTTPSValidation();
```

---

### ERROR: `ClassNotFoundException` for TestNG
```
java.lang.ClassNotFoundException: org.testng.TestNG
```
**Cause:** TestNG dependency missing or `scope` is wrong  
**Fix:** Check pom.xml has this exactly:
```xml
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.8.0</version>
    <scope>test</scope>
</dependency>
```

---

### ERROR: `Schema validation failure`
```
com.github.fge.jsonschema.core.exceptions.ProcessingException
```
**Cause:** Schema file not found or schema doesn't match response  
**Fix checklist:**
1. Verify schema file is at `src/test/resources/schemas/your-schema.json`
2. Open the schema file — check JSON is valid (no syntax errors)
3. Run the test with `.log().body()` to see the actual response
4. Update schema to match actual response structure

---

### ERROR: `SoftAssert — test passes but assertions were wrong`
```
Test passed but you expected it to fail
```
**Cause:** Forgot `softAssert.assertAll()` at the end  
**Fix:**
```java
SoftAssert soft = new SoftAssert();
soft.assertEquals(actual, expected);
soft.assertAll();    // ← THIS LINE IS MANDATORY. Never forget it.
```

---

### ERROR: Tests pass locally but fail in CI/CD
**Common causes and fixes:**

| Cause | Fix |
|-------|-----|
| Hardcoded token expired | Fetch token dynamically in `@BeforeClass` |
| Hardcoded environment URL | Use `-Denv=staging` with a config file |
| Different time zones affect date assertions | Use relative time checks, not absolute |
| Network timeout in CI | Increase timeout in BaseTest |
| Test order dependency | Each test must be independent, no shared state |

---

## 15. Interview Checklist

### Before You Start Coding — Ask These Questions
```
✅ Is there API documentation or Swagger?
✅ Does it require authentication? What type?
✅ What environment do I test against?
✅ What are the required vs optional fields in POST?
✅ What status codes does this API use for errors?
✅ Are there any rate limits I should be aware of?
```

### Your Test Coverage Should Always Include
```
✅ Happy path — valid input → success response
✅ 404 negative — invalid ID → not found
✅ 400 negative — missing required field → bad request
✅ 401 negative — no auth → unauthorized (if API has auth)
✅ Schema validation — response structure is correct
✅ Response time — API responds within acceptable limit
✅ Headers — Content-Type is correct
✅ At least one data-driven test — multiple inputs, one test method
```

### What to Say Before Writing Tests
```
"Before I write tests I want to cover:
1. Happy path — valid inputs, expected success
2. Negative — invalid inputs, expected errors
3. Boundary — edge of valid range
4. Schema — response contract
5. Performance — response time

Let me start with the GET endpoints first,
then move to mutation tests (POST/PUT/DELETE)."
```

### Naming Convention for Tests
```java
// Format: methodName_scenario_expectedResult
public void getPost_validId_returns200()       { }
public void getPost_invalidId_returns404()     { }
public void createPost_validData_returns201()  { }
public void createPost_missingTitle_returns400() { }
public void deletePost_validId_returns204()    { }
```

### Quick Reference — Most Used Commands
```powershell
# Run all tests
./apache-maven-3.9.6/bin/mvn test

# Run one class
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests

# Run one method
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests#getAllPosts

# Clean then run
./apache-maven-3.9.6/bin/mvn clean test

# Filter output (PowerShell)
./apache-maven-3.9.6/bin/mvn test 2>&1 | Select-String "Tests run|BUILD"

# View HTML report (open in browser after running tests)
# Path: target/surefire-reports/index.html
```

---

*Guide version 1.0 — REST API QA Practice | Java + RestAssured + TestNG*
