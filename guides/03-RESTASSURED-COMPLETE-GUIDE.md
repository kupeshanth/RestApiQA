# RestAssured — Complete Guide
## Java API Testing | Every Method, Every Assertion, Every Pattern

---

## TABLE OF CONTENTS
1. [What is RestAssured](#1-what-is-restassured)
2. [Setup](#2-setup)
3. [BaseTest Setup](#3-basetest-setup)
4. [GET Requests — All Scenarios](#4-get-requests--all-scenarios)
5. [POST Requests — All Scenarios](#5-post-requests--all-scenarios)
6. [PUT & PATCH — All Scenarios](#6-put--patch--all-scenarios)
7. [DELETE — All Scenarios](#7-delete--all-scenarios)
8. [Assertions — Every Pattern](#8-assertions--every-pattern)
9. [Authentication — Every Type](#9-authentication--every-type)
10. [Request Building](#10-request-building)
11. [Response Extraction](#11-response-extraction)
12. [JSON Schema Validation](#12-json-schema-validation)
13. [Advanced Patterns](#13-advanced-patterns)
14. [All Status Codes](#14-all-status-codes)
15. [Troubleshooting](#15-troubleshooting)
16. [Interview Q&A](#16-interview-qa)

---

## 1. What is RestAssured

RestAssured is a Java library for testing REST APIs. It uses a fluent BDD-style syntax:

```
given()   → set up request
  when()  → send request
  .then() → assert response
```

Without it: 50+ lines of `HttpURLConnection` boilerplate.
With it: 5 lines.

---

## 2. Setup

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.3.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>5.3.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.8.0</version>
    <scope>test</scope>
</dependency>
```

### Always add these imports
```java
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
```

---

## 3. BaseTest Setup

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

    protected RequestSpecification  requestSpec;
    protected ResponseSpecification responseSpec;

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = ApiConstants.BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            // .addHeader("Authorization", "Bearer " + getToken())
            // .addHeader("x-api-key", ApiConstants.API_KEY)
            .build();

        responseSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(3000L))
            .build();
    }
}
```

```java
// ApiConstants.java
package utils;
public class ApiConstants {
    public static final String BASE_URL        = "https://jsonplaceholder.typicode.com";
    public static final String POSTS_ENDPOINT  = "/posts";
    public static final String USERS_ENDPOINT  = "/users";
}
```

---

## 4. GET Requests — All Scenarios

```java
// ── GET all resources ─────────────────────────────────────────────────────────
given()
.when()
    .get("/posts")
.then()
    .statusCode(200)
    .body("size()", equalTo(100))
    .body("[0].id", notNullValue())
    .contentType(ContentType.JSON)
    .time(lessThan(3000L));

// ── GET by path parameter ─────────────────────────────────────────────────────
given()
    .pathParam("id", 1)
.when()
    .get("/posts/{id}")
.then()
    .statusCode(200)
    .body("id", equalTo(1))
    .body("title", not(emptyString()));

// ── GET with query parameter ──────────────────────────────────────────────────
given()
    .queryParam("userId", 1)
.when()
    .get("/posts")
.then()
    .statusCode(200)
    .body("size()", equalTo(10))
    .body("userId", everyItem(equalTo(1)));

// ── GET with multiple query parameters ───────────────────────────────────────
given()
    .queryParam("userId", 1)
    .queryParam("_limit", 5)
.when()
    .get("/posts")
.then()
    .statusCode(200)
    .body("size()", lessThanOrEqualTo(5));

// ── GET non-existent → 404 ────────────────────────────────────────────────────
given()
    .pathParam("id", 99999)
.when()
    .get("/posts/{id}")
.then()
    .statusCode(404);

// ── GET and verify headers ────────────────────────────────────────────────────
given()
.when()
    .get("/posts/1")
.then()
    .statusCode(200)
    .header("Content-Type", containsString("application/json"))
    .header("Cache-Control", notNullValue());
```

---

## 5. POST Requests — All Scenarios

```java
// ── POST valid data → 201 ─────────────────────────────────────────────────────
String body = "{ \"title\": \"New Post\", \"body\": \"Content\", \"userId\": 1 }";

given()
    .spec(requestSpec)
    .body(body)
.when()
    .post("/posts")
.then()
    .statusCode(201)
    .body("id", notNullValue())
    .body("title", equalTo("New Post"));

// ── POST and capture ID ───────────────────────────────────────────────────────
int newId = given()
    .spec(requestSpec)
    .body(body)
.when()
    .post("/posts")
.then()
    .statusCode(201)
    .extract().path("id");

System.out.println("Created ID: " + newId);

// ── POST with Java Map (cleaner than String) ──────────────────────────────────
Map<String, Object> payload = new HashMap<>();
payload.put("title", "Test Post");
payload.put("body", "Body content");
payload.put("userId", 1);

given()
    .spec(requestSpec)
    .body(payload)
.when()
    .post("/posts")
.then()
    .statusCode(201);

// ── POST missing required field → 400 (real APIs) ────────────────────────────
given()
    .spec(requestSpec)
    .body("{ \"body\": \"No title\" }")
.when()
    .post("/posts")
.then()
    .statusCode(anyOf(equalTo(400), equalTo(201)));
```

---

## 6. PUT & PATCH — All Scenarios

```java
// ── PUT — full replacement ────────────────────────────────────────────────────
String fullBody = "{ \"id\": 1, \"title\": \"Replaced\", \"body\": \"New body\", \"userId\": 1 }";

given()
    .spec(requestSpec)
    .body(fullBody)
    .pathParam("id", 1)
.when()
    .put("/posts/{id}")
.then()
    .statusCode(200)
    .body("title", equalTo("Replaced"));

// ── PATCH — partial update ────────────────────────────────────────────────────
given()
    .spec(requestSpec)
    .body("{ \"title\": \"Only Title Changed\" }")
    .pathParam("id", 1)
.when()
    .patch("/posts/{id}")
.then()
    .statusCode(200)
    .body("id", equalTo(1))
    .body("title", equalTo("Only Title Changed"));
```

---

## 7. DELETE — All Scenarios

```java
// ── DELETE → 200 or 204 ───────────────────────────────────────────────────────
given()
    .pathParam("id", 1)
.when()
    .delete("/posts/{id}")
.then()
    .statusCode(anyOf(equalTo(200), equalTo(204)));

// ── DELETE and verify empty body ──────────────────────────────────────────────
given()
    .pathParam("id", 1)
.when()
    .delete("/posts/{id}")
.then()
    .statusCode(200)
    .body(equalTo("{}"));
```

---

## 8. Assertions — Every Pattern

```java
// Status
.statusCode(200)
.statusCode(not(500))
.statusCode(anyOf(equalTo(200), equalTo(201)))

// Body — field values
.body("id", equalTo(1))
.body("title", equalTo("Hello"))
.body("title", not(emptyString()))
.body("title", containsString("Hello"))
.body("title", startsWith("He"))
.body("email", matchesPattern(".*@.*\\..*"))
.body("id", notNullValue())
.body("id", nullValue())
.body("count", greaterThan(0))
.body("count", greaterThanOrEqualTo(1))
.body("count", lessThan(100))
.body("count", lessThanOrEqualTo(99))
.body("active", equalTo(true))

// Body — arrays
.body("size()", equalTo(100))
.body("size()", greaterThan(0))
.body("[0].id", equalTo(1))
.body("userId", everyItem(equalTo(1)))
.body("id", hasItem(5))
.body("title", hasItems("A", "B"))

// Body — nested JSON
.body("user.address.city", equalTo("London"))
.body("user.roles[0]", equalTo("admin"))

// Headers
.contentType("application/json")
.contentType(ContentType.JSON)
.header("Content-Type", containsString("application/json"))
.header("x-custom", equalTo("value"))

// Response time
.time(lessThan(3000L))

// Schema
.body(matchesJsonSchemaInClasspath("schemas/post-schema.json"))
```

---

## 9. Authentication — Every Type

```java
// API Key in header
given()
    .header("x-api-key", "your-key")
.when()
    .get("/protected");

// Bearer token
given()
    .header("Authorization", "Bearer your-token")
.when()
    .get("/protected");

// Basic auth
given()
    .auth().basic("username", "password")
.when()
    .get("/protected");

// OAuth2 — login, capture token, use it
String token = given()
    .contentType(ContentType.JSON)
    .body("{ \"username\": \"admin\", \"password\": \"secret\" }")
.when()
    .post("/auth/login")
.then()
    .statusCode(200)
    .extract().path("token");

given()
    .header("Authorization", "Bearer " + token)
.when()
    .get("/protected-resource")
.then()
    .statusCode(200);

// In requestSpec (applies to all tests)
requestSpec = new RequestSpecBuilder()
    .setContentType(ContentType.JSON)
    .addHeader("Authorization", "Bearer " + token)
    .build();
```

---

## 10. Request Building

```java
// Path params
given().pathParam("id", 1).pathParam("type", "posts")
    .when().get("/{type}/{id}");

// Query params
given().queryParam("userId", 1).queryParam("_limit", 10)
    .when().get("/posts");

// Multiple headers
given()
    .header("Authorization", "Bearer token")
    .header("Accept-Language", "en-US")
    .header("x-correlation-id", "test-001");

// Logging — always print request
given().log().all().when().get("/posts");

// Logging — only on failure
RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

// Response logging
.then().log().all()
.then().log().ifError()
.then().log().status()
.then().log().body()
```

---

## 11. Response Extraction

```java
// Extract single value
int id         = given().when().get("/posts/1").then().extract().path("id");
String title   = given().when().get("/posts/1").then().extract().path("title");

// Extract nested value
String city = given().when().get("/users/1").then()
    .extract().path("address.city");

// Extract full response object
Response response = given().when().get("/posts/1")
    .then().statusCode(200).extract().response();

int    id2    = response.path("id");
String title2 = response.path("title");
int    status = response.statusCode();
String header = response.header("Content-Type");
String raw    = response.body().asString();

// Extract list
List<Integer> ids = given().when().get("/posts")
    .then().extract().path("id");
```

---

## 12. JSON Schema Validation

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

```java
// Use in test
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

given()
    .pathParam("id", 1)
.when()
    .get("/posts/{id}")
.then()
    .statusCode(200)
    .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
```

---

## 13. Advanced Patterns

### Chained CRUD test
```java
@Test
public void fullCrudWorkflow() {
    // CREATE
    int id = given().spec(requestSpec)
        .body("{ \"title\": \"Test\", \"body\": \"Body\", \"userId\": 1 }")
        .when().post("/posts")
        .then().statusCode(201).extract().path("id");

    // READ
    given().pathParam("id", id)
        .when().get("/posts/{id}")
        .then().statusCode(anyOf(equalTo(200), equalTo(404)));

    // UPDATE
    given().spec(requestSpec)
        .body("{ \"title\": \"Updated\" }").pathParam("id", id)
        .when().patch("/posts/{id}")
        .then().statusCode(anyOf(equalTo(200), equalTo(404)));

    // DELETE
    given().pathParam("id", id)
        .when().delete("/posts/{id}")
        .then().statusCode(anyOf(equalTo(200), equalTo(204)));
}
```

### Soft assertions on response
```java
@Test
public void softAssertResponse() {
    Response r = given().pathParam("id", 1)
        .when().get("/posts/{id}")
        .then().statusCode(200).extract().response();

    SoftAssert soft = new SoftAssert();
    soft.assertEquals((int) r.path("id"), 1);
    soft.assertNotNull(r.path("title"));
    soft.assertNotNull(r.path("body"));
    soft.assertAll();   // NEVER skip this
}
```

---

## 14. All Status Codes

```
200 OK            → GET/PUT/PATCH success
201 Created       → POST created resource
204 No Content    → DELETE success, no body
400 Bad Request   → Wrong input
401 Unauthorized  → Not authenticated
403 Forbidden     → Authenticated but not allowed
404 Not Found     → Resource doesn't exist
409 Conflict      → Duplicate resource
422 Unprocessable → Validation failed
429 Too Many Req  → Rate limit
500 Server Error  → Server crashed
```

---

## 15. Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `Connection refused` | Wrong URL or server down | Check BASE_URL, verify API is running |
| `401 Unauthorized` | Missing/wrong token | Add auth header to requestSpec |
| `JSON path not found` | Wrong field name | Print response body with `.log().body()` |
| `Schema validation failed` | Schema file path wrong | File must be in `src/test/resources/schemas/` |
| `ClassCastException` on extract | Wrong type | Use `Integer` not `int`: `response.<Integer>path("id")` |

---

## 16. Interview Q&A

**Q: What is the difference between `extract().path()` and getting the full response?**
```java
// extract().path() — get one specific field directly
int id = ...then().extract().path("id");

// extract().response() — get the whole response to use multiple fields
Response r = ...then().extract().response();
int id     = r.path("id");
String title = r.path("title");
// Use response() when you need multiple values
```

**Q: When do you use `ResponseSpecification`?**
```
When multiple tests share the same basic response assertions (e.g. all
must be JSON and under 3 seconds), define a responseSpec once in BaseTest
and apply with .spec(responseSpec). Keeps tests DRY.
```

**Q: How do you test that an error response has the right error message?**
```java
given().spec(requestSpec)
    .body("{ \"body\": \"no title\" }")
.when()
    .post("/posts")
.then()
    .statusCode(400)
    .body("error", equalTo("Title is required"))
    .body("code", equalTo("VALIDATION_ERROR"));
```
