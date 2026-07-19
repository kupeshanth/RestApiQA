# REST API QA Practice — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Take a complete beginner from zero REST API knowledge to writing automated Java + RestAssured tests against JSONPlaceholder.

**Architecture:** Two phases — Phase 1 uses Postman (GUI) to learn HTTP concepts hands-on; Phase 2 builds a Maven/TestNG/RestAssured project that automates the same tests in Java code.

**Tech Stack:** Postman (manual), Java 11+, Maven, REST Assured 5.3.1, TestNG 7.8.0, Hamcrest, JSON Schema Validator

## Global Constraints

- Target API: `https://jsonplaceholder.typicode.com` — free, public, no auth required
- Java 11+ required (check with `java -version`)
- Maven 3.6+ required (check with `mvn -version`)
- All test source files go under `src/test/java/`
- Package structure: `base`, `tests`, `utils`
- All file paths below are relative to `c:\Users\Kupeshanth\Desktop\Rest APi Sample\`

---

## PHASE 1 — Manual Testing with Postman

---

### Task 1: Install Postman and Send Your First GET Request

**Concept:** A REST API is a way for two systems to talk over the internet using HTTP. When you visit a website, your browser sends a GET request. APIs work the same way — just with data (JSON) instead of web pages.

**Files:** None (Postman is a GUI app)

- [ ] **Step 1: Install Postman**
  - Go to https://www.postman.com/downloads/ and download the free desktop app
  - Install and open it. Create a free account or skip and "Go to app"

- [ ] **Step 2: Send your first GET request**
  - Click **New → HTTP Request**
  - Method: `GET` (already selected)
  - URL: `https://jsonplaceholder.typicode.com/posts`
  - Click **Send**
  - You should see a JSON array of 100 posts in the response body below

- [ ] **Step 3: Read the response**
  - Look at the bottom panel. Note:
    - **Status:** `200 OK` — means success
    - **Time:** how long the server took to respond (e.g. 400ms)
    - **Body:** 100 JSON objects, each with `id`, `userId`, `title`, `body`

- [ ] **Step 4: Get a single post using a path parameter**
  - Change the URL to: `https://jsonplaceholder.typicode.com/posts/1`
  - Click **Send**
  - You get back ONE post — the one with `id: 1`
  - **Understanding:** `/posts/1` — the `1` is a **path parameter** (identifies a specific resource)

- [ ] **Step 5: Understand status codes**
  - Change URL to: `https://jsonplaceholder.typicode.com/posts/9999`
  - Click **Send**
  - Status: `404 Not Found` — resource does not exist
  - Key codes to memorise:
    - `200 OK` — request succeeded
    - `201 Created` — new resource was created
    - `204 No Content` — success but nothing returned
    - `400 Bad Request` — your request had an error
    - `401 Unauthorized` — you need to log in
    - `403 Forbidden` — logged in but not allowed
    - `404 Not Found` — resource does not exist
    - `500 Internal Server Error` — server crashed

---

### Task 2: Query Parameters and Response Headers

**Concept:** Query parameters filter results. Headers are metadata sent with every request and response (content type, auth tokens, etc.).

- [ ] **Step 1: Use a query parameter to filter posts**
  - URL: `https://jsonplaceholder.typicode.com/posts?userId=1`
  - Click **Send**
  - You get 10 posts — only the ones belonging to user 1
  - The `?userId=1` part is a **query parameter** — it filters, not identifies

- [ ] **Step 2: Compare path param vs query param**
  - Path param: `/posts/1` → "give me post with ID 1" (specific resource)
  - Query param: `/posts?userId=1` → "give me all posts where userId equals 1" (filtered list)

- [ ] **Step 3: Inspect response headers**
  - After sending any request, click the **Headers** tab in the response panel
  - Find `Content-Type: application/json; charset=utf-8`
  - This tells you the server is returning JSON data
  - As a QA, you test headers too — not just the body

---

### Task 3: POST, PUT, PATCH, DELETE

**Concept:** GET reads data. POST creates. PUT fully replaces. PATCH partially updates. DELETE removes.

- [ ] **Step 1: POST — Create a new post**
  - Method: `POST`
  - URL: `https://jsonplaceholder.typicode.com/posts`
  - Click **Body → raw → JSON** (from the dropdown)
  - Paste this body:
    ```json
    {
      "title": "My First Test Post",
      "body": "This is the body content",
      "userId": 1
    }
    ```
  - Click **Send**
  - Status: `201 Created`
  - Response contains your data + a new `id: 101`
  - **Note:** JSONPlaceholder doesn't actually save data — it simulates it

- [ ] **Step 2: PUT — Full update (replace everything)**
  - Method: `PUT`
  - URL: `https://jsonplaceholder.typicode.com/posts/1`
  - Body (raw JSON):
    ```json
    {
      "id": 1,
      "title": "Completely Replaced Title",
      "body": "Completely replaced body",
      "userId": 1
    }
    ```
  - Click **Send** — Status: `200 OK`
  - PUT sends the FULL new version of the resource

- [ ] **Step 3: PATCH — Partial update (change only one field)**
  - Method: `PATCH`
  - URL: `https://jsonplaceholder.typicode.com/posts/1`
  - Body (raw JSON):
    ```json
    {
      "title": "Only Title Changed"
    }
    ```
  - Click **Send** — Status: `200 OK`
  - PATCH sends only the fields you want to change

- [ ] **Step 4: DELETE — Remove a resource**
  - Method: `DELETE`
  - URL: `https://jsonplaceholder.typicode.com/posts/1`
  - No body needed
  - Click **Send** — Status: `200 OK`, body: `{}`

---

### Task 4: Postman Collections, Environments, and Test Scripts

**Concept:** Collections organise your requests into folders. Environments store reusable variables (like the base URL). Test scripts let you write assertions that run automatically after each request.

- [ ] **Step 1: Create an Environment**
  - Click **Environments** (left sidebar) → **+**
  - Name: `JSONPlaceholder`
  - Add variable: `base_url` = `https://jsonplaceholder.typicode.com`
  - Save, then select this environment from the top-right dropdown

- [ ] **Step 2: Create a Collection**
  - Click **Collections** (left sidebar) → **+** → **Blank collection**
  - Name: `JSONPlaceholder API Tests`
  - Add folders: `GET Tests`, `POST Tests`, `PUT Tests`, `DELETE Tests`

- [ ] **Step 3: Add requests to the collection**
  - Drag or re-create your requests inside the folders
  - Replace the URL with: `{{base_url}}/posts` — Postman substitutes the variable

- [ ] **Step 4: Write your first test script on the GET all posts request**
  - Open the GET `/posts` request → click the **Tests** tab
  - Paste:
    ```javascript
    pm.test("Status code is 200", function () {
        pm.response.to.have.status(200);
    });

    pm.test("Response is an array", function () {
        var body = pm.response.json();
        pm.expect(body).to.be.an("array");
    });

    pm.test("Array has 100 posts", function () {
        pm.expect(pm.response.json().length).to.equal(100);
    });

    pm.test("Each post has required fields", function () {
        var first = pm.response.json()[0];
        pm.expect(first).to.have.property("id");
        pm.expect(first).to.have.property("title");
        pm.expect(first).to.have.property("body");
        pm.expect(first).to.have.property("userId");
    });

    pm.test("Response time is under 3 seconds", function () {
        pm.expect(pm.response.responseTime).to.be.below(3000);
    });
    ```
  - Click **Send** — scroll to the **Test Results** tab to see PASS/FAIL

- [ ] **Step 5: Write tests on the POST request**
  - Open the POST `/posts` request → Tests tab:
    ```javascript
    pm.test("Status code is 201 Created", function () {
        pm.response.to.have.status(201);
    });

    pm.test("Response has an id", function () {
        pm.expect(pm.response.json().id).to.exist;
    });

    pm.test("Title matches what we sent", function () {
        pm.expect(pm.response.json().title).to.equal("My First Test Post");
    });
    ```

- [ ] **Step 6: Run the whole collection**
  - Click the **...** menu on your collection → **Run collection**
  - Click **Run JSONPlaceholder API Tests**
  - See all tests run in sequence with PASS/FAIL results

**Phase 1 complete.** You can now manually test any REST API.

---

## PHASE 2 — Automation with Java + RestAssured + TestNG

---

### Task 5: Prerequisites Check and Maven Project Setup

**Concept:** Maven is a build tool for Java — it downloads your dependencies (like RestAssured) and runs your tests. `pom.xml` is its config file.

**Files:**
- Create: `pom.xml`
- Create: `src/test/java/base/` (folder)
- Create: `src/test/java/tests/` (folder)
- Create: `src/test/java/utils/` (folder)
- Create: `src/test/resources/` (folder)
- Create: `src/test/resources/schemas/` (folder)

- [ ] **Step 1: Verify Java and Maven are installed**
  ```bash
  java -version
  mvn -version
  ```
  Both must return a version. If not:
  - Java: https://adoptium.net/ (download JDK 11 or 17)
  - Maven: https://maven.apache.org/install.html

- [ ] **Step 2: Create the folder structure**

  In your terminal, navigate to `c:\Users\Kupeshanth\Desktop\Rest APi Sample\` and run:
  ```bash
  mkdir -p src/test/java/base
  mkdir -p src/test/java/tests
  mkdir -p src/test/java/utils
  mkdir -p src/test/resources/schemas
  ```

- [ ] **Step 3: Create `pom.xml`**

  Create file `pom.xml` at the project root with this content:
  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>

      <groupId>com.restapi.practice</groupId>
      <artifactId>rest-api-sample</artifactId>
      <version>1.0-SNAPSHOT</version>

      <properties>
          <maven.compiler.source>11</maven.compiler.source>
          <maven.compiler.target>11</maven.compiler.target>
          <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      </properties>

      <dependencies>
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

- [ ] **Step 4: Download dependencies**
  ```bash
  mvn dependency:resolve
  ```
  Expected: `BUILD SUCCESS` and dependencies downloaded to your local Maven cache.

---

### Task 6: ApiConstants and BaseTest

**Concept:** `ApiConstants` holds shared values (URLs) so you never repeat them. `BaseTest` runs setup code before any test class — like configuring RestAssured's base URL once.

**Files:**
- Create: `src/test/java/utils/ApiConstants.java`
- Create: `src/test/java/base/BaseTest.java`

**Interfaces:**
- Produces: `ApiConstants.BASE_URL`, `ApiConstants.POSTS_ENDPOINT` (used by all test classes)
- Produces: `BaseTest` (extended by all test classes — sets RestAssured base URI)

- [ ] **Step 1: Create `ApiConstants.java`**
  ```java
  package utils;

  public class ApiConstants {
      public static final String BASE_URL = "https://jsonplaceholder.typicode.com";
      public static final String POSTS_ENDPOINT = "/posts";
  }
  ```

- [ ] **Step 2: Create `BaseTest.java`**
  ```java
  package base;

  import io.restassured.RestAssured;
  import org.testng.annotations.BeforeClass;
  import utils.ApiConstants;

  public class BaseTest {

      @BeforeClass
      public void setUp() {
          RestAssured.baseURI = ApiConstants.BASE_URL;
          RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
      }
  }
  ```

  **What this does:**
  - `@BeforeClass` runs once before any test in the class
  - `baseURI` means every test can write `get("/posts")` instead of the full URL
  - `enableLoggingOfRequestAndResponseIfValidationFails()` prints request/response details only when a test fails — very useful for debugging

- [ ] **Step 3: Verify the project compiles**
  ```bash
  mvn compile test-compile
  ```
  Expected: `BUILD SUCCESS`

---

### Task 7: GET Tests

**Concept:** In RestAssured, the pattern is always `given() → when() → then()`. `given()` sets up the request, `when()` sends it, `then()` asserts the response. This is called BDD (Behaviour-Driven Development) style.

**Files:**
- Create: `src/test/java/tests/GetTests.java`

**Interfaces:**
- Consumes: `BaseTest` (extends it), `ApiConstants.POSTS_ENDPOINT`

- [ ] **Step 1: Create `GetTests.java`**
  ```java
  package tests;

  import base.BaseTest;
  import org.testng.annotations.Test;
  import utils.ApiConstants;

  import static io.restassured.RestAssured.*;
  import static org.hamcrest.Matchers.*;

  public class GetTests extends BaseTest {

      @Test(description = "GET all posts returns 200 with 100 items")
      public void getAllPosts() {
          given()
              .when()
                  .get(ApiConstants.POSTS_ENDPOINT)
              .then()
                  .statusCode(200)
                  .body("size()", equalTo(100))
                  .body("[0].id", equalTo(1))
                  .body("[0].title", not(emptyString()))
                  .body("[0].userId", notNullValue());
      }

      @Test(description = "GET single post by ID returns correct data")
      public void getPostById() {
          given()
              .pathParam("id", 1)
              .when()
                  .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
              .then()
                  .statusCode(200)
                  .body("id", equalTo(1))
                  .body("title", not(emptyString()))
                  .body("body", not(emptyString()))
                  .body("userId", equalTo(1));
      }

      @Test(description = "GET posts filtered by userId returns only that user's posts")
      public void getPostsByUserId() {
          given()
              .queryParam("userId", 1)
              .when()
                  .get(ApiConstants.POSTS_ENDPOINT)
              .then()
                  .statusCode(200)
                  .body("size()", equalTo(10))
                  .body("userId", everyItem(equalTo(1)));
      }

      @Test(description = "GET non-existent post returns 404")
      public void getNonExistentPost() {
          given()
              .pathParam("id", 99999)
              .when()
                  .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
              .then()
                  .statusCode(404);
      }

      @Test(description = "GET response has Content-Type application/json")
      public void getPostsHasJsonContentType() {
          given()
              .when()
                  .get(ApiConstants.POSTS_ENDPOINT)
              .then()
                  .statusCode(200)
                  .contentType("application/json");
      }

      @Test(description = "GET all posts responds in under 3 seconds")
      public void getPostsResponseTime() {
          given()
              .when()
                  .get(ApiConstants.POSTS_ENDPOINT)
              .then()
                  .statusCode(200)
                  .time(lessThan(3000L));
      }
  }
  ```

  **Reading guide:**
  - `body("size()", equalTo(100))` — asserts the JSON array has 100 elements
  - `body("[0].id", equalTo(1))` — asserts the first element's `id` field equals `1`
  - `body("userId", everyItem(equalTo(1)))` — asserts every element in the array has `userId == 1`
  - `time(lessThan(3000L))` — asserts response came back in under 3000 milliseconds

- [ ] **Step 2: Run only the GET tests**
  ```bash
  mvn test -Dtest=GetTests
  ```
  Expected output:
  ```
  Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
  BUILD SUCCESS
  ```

---

### Task 8: POST Tests

**Concept:** POST requests send a body (usually JSON). You must tell the server the body format using `contentType(ContentType.JSON)`. The server creates the resource and returns `201 Created`.

**Files:**
- Create: `src/test/java/tests/PostTests.java`

**Interfaces:**
- Consumes: `BaseTest`, `ApiConstants.POSTS_ENDPOINT`

- [ ] **Step 1: Create `PostTests.java`**
  ```java
  package tests;

  import base.BaseTest;
  import io.restassured.http.ContentType;
  import org.testng.annotations.Test;
  import utils.ApiConstants;

  import static io.restassured.RestAssured.*;
  import static org.hamcrest.Matchers.*;

  public class PostTests extends BaseTest {

      private static final String NEW_POST = """
              {
                "title": "QA Practice Post",
                "body": "This is a test post body",
                "userId": 1
              }
              """;

      @Test(description = "POST a new post returns 201 Created with an ID")
      public void createPost() {
          given()
              .contentType(ContentType.JSON)
              .body(NEW_POST)
              .when()
                  .post(ApiConstants.POSTS_ENDPOINT)
              .then()
                  .statusCode(201)
                  .body("id", notNullValue())
                  .body("title", equalTo("QA Practice Post"))
                  .body("body", equalTo("This is a test post body"))
                  .body("userId", equalTo(1));
      }

      @Test(description = "POST returns response with application/json content type")
      public void createPostReturnsJsonContentType() {
          given()
              .contentType(ContentType.JSON)
              .body(NEW_POST)
              .when()
                  .post(ApiConstants.POSTS_ENDPOINT)
              .then()
                  .statusCode(201)
                  .contentType(containsString("application/json"));
      }

      @Test(description = "POST with empty body returns 201 (JSONPlaceholder is lenient)")
      public void createPostWithEmptyBody() {
          given()
              .contentType(ContentType.JSON)
              .body("{}")
              .when()
                  .post(ApiConstants.POSTS_ENDPOINT)
              .then()
                  .statusCode(201);
      }
  }
  ```

  **Note on text blocks:** The `"""..."""` syntax is a Java 15+ text block. If you're on Java 11, replace with:
  ```java
  private static final String NEW_POST = "{ \"title\": \"QA Practice Post\", \"body\": \"This is a test post body\", \"userId\": 1 }";
  ```

- [ ] **Step 2: Run POST tests**
  ```bash
  mvn test -Dtest=PostTests
  ```
  Expected: `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`

---

### Task 9: PUT and PATCH Tests

**Concept:** PUT sends a full replacement body. PATCH sends only the fields you want to change. Both return `200 OK` on success.

**Files:**
- Create: `src/test/java/tests/PutTests.java`

**Interfaces:**
- Consumes: `BaseTest`, `ApiConstants.POSTS_ENDPOINT`

- [ ] **Step 1: Create `PutTests.java`**
  ```java
  package tests;

  import base.BaseTest;
  import io.restassured.http.ContentType;
  import org.testng.annotations.Test;
  import utils.ApiConstants;

  import static io.restassured.RestAssured.*;
  import static org.hamcrest.Matchers.*;

  public class PutTests extends BaseTest {

      @Test(description = "PUT replaces a post completely and returns 200")
      public void updatePostWithPut() {
          String fullReplacement = "{ \"id\": 1, \"title\": \"Replaced Title\", \"body\": \"Replaced body\", \"userId\": 1 }";

          given()
              .contentType(ContentType.JSON)
              .body(fullReplacement)
              .pathParam("id", 1)
              .when()
                  .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
              .then()
                  .statusCode(200)
                  .body("id", equalTo(1))
                  .body("title", equalTo("Replaced Title"))
                  .body("body", equalTo("Replaced body"));
      }

      @Test(description = "PATCH updates only the title and returns 200")
      public void updatePostTitleWithPatch() {
          String partialUpdate = "{ \"title\": \"Only Title Updated\" }";

          given()
              .contentType(ContentType.JSON)
              .body(partialUpdate)
              .pathParam("id", 1)
              .when()
                  .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
              .then()
                  .statusCode(200)
                  .body("id", equalTo(1))
                  .body("title", equalTo("Only Title Updated"));
      }
  }
  ```

- [ ] **Step 2: Run PUT tests**
  ```bash
  mvn test -Dtest=PutTests
  ```
  Expected: `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`

---

### Task 10: DELETE Tests

**Files:**
- Create: `src/test/java/tests/DeleteTests.java`

**Interfaces:**
- Consumes: `BaseTest`, `ApiConstants.POSTS_ENDPOINT`

- [ ] **Step 1: Create `DeleteTests.java`**
  ```java
  package tests;

  import base.BaseTest;
  import org.testng.annotations.Test;
  import utils.ApiConstants;

  import static io.restassured.RestAssured.*;
  import static org.hamcrest.Matchers.*;

  public class DeleteTests extends BaseTest {

      @Test(description = "DELETE a post by ID returns 200 OK")
      public void deletePost() {
          given()
              .pathParam("id", 1)
              .when()
                  .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
              .then()
                  .statusCode(200);
      }

      @Test(description = "DELETE response body is empty object")
      public void deletePostReturnsEmptyBody() {
          given()
              .pathParam("id", 1)
              .when()
                  .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
              .then()
                  .statusCode(200)
                  .body(equalTo("{}"));
      }
  }
  ```

- [ ] **Step 2: Run DELETE tests**
  ```bash
  mvn test -Dtest=DeleteTests
  ```
  Expected: `Tests run: 2, Failures: 0, Errors: 0, Skipped: 0`

---

### Task 11: JSON Schema Validation

**Concept:** A JSON Schema defines the expected structure of a response — field names, types, which are required. RestAssured can validate a response against a schema automatically. This catches API contract breaks (e.g., a field renamed or removed by developers).

**Files:**
- Create: `src/test/resources/schemas/post-schema.json`
- Modify: `src/test/java/tests/GetTests.java` — add one new test method

- [ ] **Step 1: Create `post-schema.json`**
  ```json
  {
    "$schema": "http://json-schema.org/draft-04/schema#",
    "type": "object",
    "properties": {
      "id":     { "type": "integer" },
      "title":  { "type": "string" },
      "body":   { "type": "string" },
      "userId": { "type": "integer" }
    },
    "required": ["id", "title", "body", "userId"]
  }
  ```

- [ ] **Step 2: Add a schema validation test to `GetTests.java`**

  Add this import at the top of `GetTests.java`:
  ```java
  import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
  ```

  Add this test method inside the `GetTests` class:
  ```java
  @Test(description = "GET single post response matches the post JSON schema")
  public void getPostMatchesSchema() {
      given()
          .pathParam("id", 1)
          .when()
              .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
          .then()
              .statusCode(200)
              .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
  }
  ```

- [ ] **Step 3: Run the schema test**
  ```bash
  mvn test -Dtest=GetTests#getPostMatchesSchema
  ```
  Expected: `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`

---

### Task 12: TestNG Suite Config and Full Test Run

**Concept:** `testng.xml` groups all test classes into a suite. Maven's Surefire plugin runs this suite and generates an HTML report you can open in your browser.

**Files:**
- Create: `src/test/resources/testng.xml`

- [ ] **Step 1: Create `testng.xml`**
  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
  <suite name="REST API Test Suite" verbose="1">

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

  </suite>
  ```

- [ ] **Step 2: Run the full suite**
  ```bash
  mvn test
  ```
  Expected output:
  ```
  [INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
  [INFO] BUILD SUCCESS
  ```

- [ ] **Step 3: Open the HTML report**
  - Navigate to: `target/surefire-reports/`
  - Open `index.html` in your browser (or any `.html` file there)
  - You will see each test class, each test method, and PASS/FAIL status

- [ ] **Step 4: Intentionally break a test to see failure output**
  - In `GetTests.java`, temporarily change `equalTo(100)` to `equalTo(50)` in `getAllPosts()`
  - Run `mvn test -Dtest=GetTests#getAllPosts`
  - See the failure message: `Expected: <50> but was: <100>`
  - This is what a real test failure looks like — RestAssured prints what it expected vs what it got
  - Revert the change back to `equalTo(100)` and run again to confirm it passes

---

## Final Project Structure

```
Rest APi Sample/
├── pom.xml
├── docs/
│   └── superpowers/
│       ├── specs/2026-07-16-rest-api-qa-practice-design.md
│       └── plans/2026-07-16-rest-api-qa-practice.md
└── src/
    └── test/
        ├── java/
        │   ├── base/
        │   │   └── BaseTest.java
        │   ├── tests/
        │   │   ├── GetTests.java
        │   │   ├── PostTests.java
        │   │   ├── PutTests.java
        │   │   └── DeleteTests.java
        │   └── utils/
        │       └── ApiConstants.java
        └── resources/
            ├── testng.xml
            └── schemas/
                └── post-schema.json
```

## What You've Learned

After completing this plan you can:
- Explain REST, HTTP methods, status codes, headers, path params, query params
- Test any API manually in Postman with organised collections and assertions
- Write Java + RestAssured automated tests for GET, POST, PUT, PATCH, DELETE
- Validate response status codes, body fields, headers, and JSON schemas
- Organise tests with TestNG and run the full suite via `mvn test`
- Read and interpret test failure messages
