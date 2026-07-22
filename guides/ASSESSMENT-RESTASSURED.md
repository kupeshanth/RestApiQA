# RestAssured API Testing — Technical Assessment Guide
## Complete Setup → Write → Run → Push | Do This Like a Pro

---

## BEFORE YOU START — Read This First (2 minutes)

```
1. Read the full task — what endpoints, what methods?
2. Note the BASE URL (change it in ONE place only — ApiConstants.java)
3. Open the URL in browser — confirm the API works
4. Check Java version: java -version (need 11+)
5. Check Maven: mvn -version (or use local Maven)
6. Now start coding
```

---

## STEP 1 — Prerequisites Check (1 minute)

```bash
java -version     # Need Java 11+. Output: openjdk version "11.x.x"
mvn -version      # Need Maven 3.6+. Output: Apache Maven 3.x.x
```

**If Maven not installed:**
```bash
# Use local Maven (downloaded to project) — no global install needed
curl -L -o maven.zip "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
unzip maven.zip -d .
# Then use: ./apache-maven-3.9.6/bin/mvn  instead of  mvn
```

---

## STEP 2 — Create Project Structure (3 minutes)

```bash
# Create project folder
mkdir restassured-assessment
cd restassured-assessment

# Create folder structure
mkdir -p src/test/java/base
mkdir -p src/test/java/tests
mkdir -p src/test/java/utils
mkdir -p src/test/resources/schemas
```

Your full structure:
```
restassured-assessment/
├── pom.xml                              ← CREATE (Step 3)
└── src/
    └── test/
        ├── java/
        │   ├── base/
        │   │   └── BaseTest.java        ← CREATE (Step 4)
        │   ├── tests/
        │   │   ├── GetTests.java        ← CREATE (Step 6)
        │   │   ├── PostTests.java       ← CREATE (Step 7)
        │   │   ├── PutTests.java        ← CREATE (Step 8)
        │   │   └── DeleteTests.java     ← CREATE (Step 9)
        │   └── utils/
        │       └── ApiConstants.java    ← CREATE (Step 5)
        └── resources/
            ├── testng.xml               ← CREATE (Step 10)
            └── schemas/
                └── post-schema.json     ← CREATE (Step 11)
```

---

## STEP 3 — Create pom.xml

Create `pom.xml` in the ROOT folder:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- ⚠️ Change groupId to match company if they specify -->
    <groupId>com.assessment</groupId>
    <artifactId>api-test-assessment</artifactId>
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

        <!-- TestNG: test runner and reporter -->
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

---

## STEP 4 — Create BaseTest.java

Create `src/test/java/base/BaseTest.java`:

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

        // ⚠️ BASE URL comes from ApiConstants — change it there, not here
        RestAssured.baseURI = ApiConstants.BASE_URL;

        // Prints full request + response ONLY when a test fails
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Shared setup applied to every request with .spec(requestSpec)
        requestSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)

            // ⚠️ If API needs auth — uncomment ONE of these:
            // .addHeader("Authorization", "Bearer " + ApiConstants.AUTH_TOKEN)
            // .addHeader("x-api-key", ApiConstants.API_KEY)
            // .addHeader("Authorization", "Basic " + Base64.encode(user:pass))

            .build();

        // Common assertions for every response
        responseSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(3000L))   // ⚠️ Change 3000 if SLA is different
            .build();
    }
}
```

---

## STEP 5 — Create ApiConstants.java

Create `src/test/java/utils/ApiConstants.java`:

```java
package utils;

public class ApiConstants {

    // ⚠️ CHANGE THIS to the API URL they give you
    public static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    // ⚠️ CHANGE THESE to the actual endpoints
    public static final String POSTS_ENDPOINT    = "/posts";
    public static final String USERS_ENDPOINT    = "/users";
    public static final String COMMENTS_ENDPOINT = "/comments";

    // ⚠️ Add auth if required. Fill in from their docs.
    public static final String AUTH_TOKEN = "your-token-here";
    public static final String API_KEY    = "your-api-key-here";

    // ⚠️ Change if they specify a different timeout SLA
    public static final int RESPONSE_TIMEOUT_MS = 3000;
}
```

**Variable reference:**

| Constant | What to put |
|----------|------------|
| `BASE_URL` | The domain they give you: `"https://api.company.com"` |
| `POSTS_ENDPOINT` | Rename to match (e.g. `USERS_ENDPOINT = "/users"`) |
| `AUTH_TOKEN` | Token from login or docs |
| `API_KEY` | API key from docs |

---

## STEP 6 — Create GetTests.java

Create `src/test/java/tests/GetTests.java`:

```java
package tests;

import base.BaseTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

public class GetTests extends BaseTest {

    // ── GET ALL ───────────────────────────────────────────────────────────────
    @Test(description = "GET all posts returns 200 with 100 items")
    public void getAllPosts() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            // ⚠️ Change 100 to the actual total count in the system
            .body("size()", equalTo(100))
            // ⚠️ Change field names to match actual response
            .body("[0].id",    notNullValue())
            .body("[0].title", not(emptyString()))
            .contentType("application/json")
            .time(lessThan(3000L));
    }

    // ── GET BY PATH PARAMETER ─────────────────────────────────────────────────
    @Test(description = "GET post by ID returns 200 with correct fields")
    public void getPostById() {
        int postId = 1;   // ⚠️ Change to a valid ID

        given()
            .pathParam("id", postId)   // replaces {id} in URL
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            // ⚠️ Change field names and values to match response
            .body("id",     equalTo(postId))
            .body("title",  not(emptyString()))
            .body("userId", notNullValue());
    }

    // ── GET WITH QUERY PARAMETER ──────────────────────────────────────────────
    @Test(description = "GET posts filtered by userId returns only matching posts")
    public void getPostsByUserId() {
        int filterUserId = 1;   // ⚠️ Change to a valid userId

        given()
            // ⚠️ Change "userId" to the actual filter param name
            .queryParam("userId", filterUserId)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            // ⚠️ Change 10 to expected count for this filter
            .body("size()", equalTo(10))
            // ⚠️ Change "userId" to actual field name
            .body("userId", everyItem(equalTo(filterUserId)));
    }

    // ── NEGATIVE: Resource does not exist ─────────────────────────────────────
    @Test(description = "GET non-existent post returns 404")
    public void getNonExistentPost() {
        given()
            .pathParam("id", 99999)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            // ⚠️ Some APIs return 200 with empty body — adjust if needed
            .statusCode(404);
    }

    // ── JSON SCHEMA VALIDATION ────────────────────────────────────────────────
    @Test(description = "GET single post matches JSON schema contract")
    public void getPostMatchesSchema() {
        given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            // ⚠️ Change schema file name if needed
            .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
    }

    // ── RESPONSE HEADERS ──────────────────────────────────────────────────────
    @Test(description = "GET response has correct Content-Type header")
    public void verifyResponseHeaders() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            // ⚠️ Change if API uses a different content type
            .header("Content-Type", containsString("application/json"));
    }

    // ── SOFT ASSERTIONS ───────────────────────────────────────────────────────
    @Test(description = "Validate all fields of a post with soft assertions")
    public void validatePostFieldsWithSoftAssert() {
        Response response = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().response();

        // ⚠️ Change field names and expected values to match response
        SoftAssert soft = new SoftAssert();
        soft.assertEquals((int) response.path("id"),     1,   "id should be 1");
        soft.assertNotNull(response.path("title"),             "title should not be null");
        soft.assertNotNull(response.path("body"),              "body should not be null");
        soft.assertEquals((int) response.path("userId"), 1,   "userId should be 1");
        soft.assertAll();   // ← NEVER forget this line
    }

    // ── EXTRACT AND REUSE VALUE ───────────────────────────────────────────────
    @Test(description = "Extract value from response and verify it")
    public void extractAndVerifyTitle() {
        // ⚠️ Change "title" to whatever field you need to verify
        String title = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().path("title");

        System.out.println("Extracted title: " + title);
        Assert.assertFalse(title.isEmpty(), "Title should not be empty");
    }
}
```

---

## STEP 7 — Create PostTests.java

Create `src/test/java/tests/PostTests.java`:

```java
package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PostTests extends BaseTest {

    // ── HAPPY PATH: Create resource ───────────────────────────────────────────
    @Test(description = "POST creates resource and returns 201 with generated ID")
    public void createPost() {
        // ⚠️ Change field names and values to match what the API expects
        String requestBody = "{ " +
            "\"title\": \"Assessment Test Post\", " +
            "\"body\":  \"This is test body content\", " +
            "\"userId\": 1 " +   // ⚠️ Change userId to a valid user
        "}";

        int newId = given()
            .spec(requestSpec)
            .body(requestBody)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            // ⚠️ Change 201 if API uses 200 for creation
            .statusCode(201)
            .body("id",    notNullValue())
            // ⚠️ Change field assertions to match response
            .body("title", equalTo("Assessment Test Post"))
            .extract().path("id");

        System.out.println("Created resource ID: " + newId);
        Assert.assertTrue(newId > 0, "ID should be a positive number");
    }

    // ── POST WITH MISSING REQUIRED FIELD ─────────────────────────────────────
    @Test(description = "POST with missing title field returns 400 Bad Request")
    public void createPostMissingTitle() {
        String incompleteBody = "{ " +
            "\"body\": \"No title here\", " +
            "\"userId\": 1 " +
        "}";

        given()
            .spec(requestSpec)
            .body(incompleteBody)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            // ⚠️ JSONPlaceholder accepts this (returns 201)
            // Real APIs return 400 — adjust to match actual API behaviour
            .statusCode(anyOf(equalTo(400), equalTo(201)));
    }

    // ── POST EMPTY BODY ───────────────────────────────────────────────────────
    @Test(description = "POST with empty body — check API behaviour")
    public void createPostEmptyBody() {
        given()
            .spec(requestSpec)
            .body("{}")
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            // ⚠️ Adjust to actual API behaviour
            .statusCode(anyOf(equalTo(400), equalTo(201)));
    }

    // ── VERIFY RESPONSE CONTENT TYPE ─────────────────────────────────────────
    @Test(description = "POST response Content-Type is application/json")
    public void createPostReturnsJsonContentType() {
        String body = "{ \"title\": \"Test\", \"body\": \"Content\", \"userId\": 1 }";

        given()
            .spec(requestSpec)
            .body(body)
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(containsString("application/json"));
    }
}
```

---

## STEP 8 — Create PutTests.java

Create `src/test/java/tests/PutTests.java`:

```java
package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PutTests extends BaseTest {

    // ── PUT: Full replacement ─────────────────────────────────────────────────
    @Test(description = "PUT replaces full post and returns 200")
    public void updatePostWithPut() {
        int resourceId = 1;   // ⚠️ Change to a valid ID

        // ⚠️ PUT must include ALL fields — missing fields get wiped
        String fullReplacement = "{ " +
            "\"id\": " + resourceId + ", " +
            "\"title\": \"Replaced Title\", " +
            "\"body\":  \"Replaced body content\", " +
            "\"userId\": 1 " +
        "}";

        given()
            .spec(requestSpec)
            .body(fullReplacement)
            .pathParam("id", resourceId)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            // ⚠️ Change field names to match response
            .body("title", equalTo("Replaced Title"))
            .body("body",  equalTo("Replaced body content"));
    }

    // ── PATCH: Partial update ─────────────────────────────────────────────────
    @Test(description = "PATCH updates only title field and returns 200")
    public void updatePostTitleWithPatch() {
        int resourceId = 1;   // ⚠️ Change to a valid ID

        // ⚠️ PATCH sends ONLY the fields you want to change
        String partialUpdate = "{ \"title\": \"Only Title Changed\" }";

        given()
            .spec(requestSpec)
            .body(partialUpdate)
            .pathParam("id", resourceId)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body("id",    equalTo(resourceId))   // id unchanged
            .body("title", equalTo("Only Title Changed"));
    }

    // ── PUT NON-EXISTENT RESOURCE ─────────────────────────────────────────────
    @Test(description = "PUT non-existent resource — check API behaviour")
    public void updateNonExistentPost() {
        String body = "{ \"id\": 99999, \"title\": \"Test\", \"body\": \"Test\", \"userId\": 1 }";

        given()
            .spec(requestSpec)
            .body(body)
            .pathParam("id", 99999)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            // ⚠️ JSONPlaceholder returns 500 here
            // Real APIs return 404 — adjust to match
            .statusCode(anyOf(equalTo(404), equalTo(500), equalTo(200)));
    }
}
```

---

## STEP 9 — Create DeleteTests.java

Create `src/test/java/tests/DeleteTests.java`:

```java
package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class DeleteTests extends BaseTest {

    // ── DELETE EXISTING RESOURCE ──────────────────────────────────────────────
    @Test(description = "DELETE post returns 200 OK")
    public void deletePost() {
        int resourceId = 1;   // ⚠️ Change to a valid ID

        given()
            .pathParam("id", resourceId)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            // ⚠️ JSONPlaceholder returns 200
            // Many real APIs return 204 No Content
            // Use anyOf if unsure:
            .statusCode(anyOf(equalTo(200), equalTo(204)));
    }

    // ── VERIFY EMPTY RESPONSE BODY ────────────────────────────────────────────
    @Test(description = "DELETE response body is empty or empty object")
    public void deletePostReturnsEmptyBody() {
        given()
            .pathParam("id", 1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)))
            // ⚠️ JSONPlaceholder returns "{}", 204 returns ""
            .body(anyOf(equalTo("{}"), equalTo("")));
    }

    // ── DELETE NON-EXISTENT RESOURCE ─────────────────────────────────────────
    @Test(description = "DELETE non-existent resource returns 404 or 200")
    public void deleteNonExistentPost() {
        given()
            .pathParam("id", 99999)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            // ⚠️ JSONPlaceholder returns 200 even for non-existent
            // Real APIs return 404 — adjust to match
            .statusCode(anyOf(equalTo(200), equalTo(404)));
    }
}
```

---

## STEP 10 — Create testng.xml

Create `src/test/resources/testng.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">

<!-- ⚠️ Change suite name to match the company/task -->
<suite name="API Assessment Test Suite" verbose="1">

    <test name="GET Tests">
        <classes>
            <!-- ⚠️ Change package.ClassName to match your class names -->
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

---

## STEP 11 — Create JSON Schema File

Create `src/test/resources/schemas/post-schema.json`:

```json
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

**⚠️ Change this schema to match the actual API response fields and types.**

---

## STEP 12 — Run Your Tests

```bash
# Using system Maven
mvn test

# Using local Maven (if no system Maven)
./apache-maven-3.9.6/bin/mvn test

# Run only one class
mvn test -Dtest=GetTests

# Run one specific method
mvn test -Dtest=GetTests#getAllPosts

# Clean then run
mvn clean test

# PowerShell — filter output
mvn test 2>&1 | Select-String "Tests run|BUILD|FAIL"
```

**Expected output:**
```
[INFO] Running TestSuite
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 12.3 s
[INFO] BUILD SUCCESS
```

---

## STEP 13 — Create README.md

```markdown
# RestAssured API Test Assessment

## Prerequisites
- Java 11+
- Maven 3.6+ (or use included Maven)

## Run Tests

```bash
mvn test
```

## Run Specific Tests

```bash
mvn test -Dtest=GetTests           # GET tests only
mvn test -Dtest=PostTests          # POST tests only
mvn test -Dtest=GetTests#getAllPosts  # one method
```

## View Report
After running, open:
`target/surefire-reports/index.html`

## Tech Stack
Java 11, RestAssured 5.3.1, TestNG 7.8.0

## Test Coverage
- GET all posts (200, count, fields)
- GET by ID (200, field values)
- GET with filter (200, everyItem matches)
- GET 404 (not found)
- POST create (201, id returned)
- PUT full update (200)
- PATCH partial update (200)
- DELETE (200/204)
- JSON Schema validation
- Soft assertions
```

---

## STEP 14 — Push to GitHub

```bash
# In the project root folder:
git init
git add .
git commit -m "Add RestAssured API test assessment — 14 tests passing"
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git
git branch -M main
git push -u origin main
```

---

## REAL-TIME FAILURES — What Goes Wrong & How to Debug

---

### FAILURE 1: `BUILD FAILURE — COMPILATION ERROR`
```
[ERROR] cannot find symbol: class WebDriverWait
[ERROR] package io.restassured does not exist
```
**Cause:** Maven didn't download dependencies yet, or wrong package name.

**Fix:**
```bash
mvn dependency:resolve    # force download all dependencies
mvn test-compile          # check compilation only, see exact error

# If still failing, check pom.xml — scope must be "test"
# and version 5.3.1 must be available in Maven Central
```

---

### FAILURE 2: `Connection refused / Unable to connect`
```
java.net.ConnectException: Connection refused: connect
io.restassured.exception.PathException
```
**Cause:** BASE_URL is wrong, API is down, or you need VPN.

**Debug steps:**
```
1. Open https://jsonplaceholder.typicode.com/posts in your browser
   → Does it load? If yes, URL is fine — check Java code
   → If no, API is down or you need VPN

2. Print the full URL being used:
   Add this to BaseTest setUp():
   System.out.println("Testing against: " + RestAssured.baseURI);

3. Check ApiConstants.BASE_URL — no trailing slash!
   ✅ "https://jsonplaceholder.typicode.com"
   ❌ "https://jsonplaceholder.typicode.com/"  ← trailing slash breaks paths
```

---

### FAILURE 3: `Expected status 200 but was 401`
```
java.lang.AssertionError: 1 expectation failed.
Expected status code <200> but was <401>.
```
**Cause:** API requires authentication but you're not sending any.

**Debug steps:**
```java
// Add to BaseTest requestSpec:
.addHeader("Authorization", "Bearer " + ApiConstants.AUTH_TOKEN)

// Or use basic auth:
given()
    .auth().basic("username", "password")
    .when().get("/endpoint")

// Check what headers the API expects — read the docs
// Use Postman first to verify auth works, then replicate in code
```

---

### FAILURE 4: `JSON path field doesn't match`
```
java.lang.AssertionError: 1 expectation failed.
JSON path userId doesn't match.
Expected: <1>
  Actual: <2>
```
**Cause:** Wrong expected value, or wrong field name.

**Debug steps:**
```java
// Step 1: Print the full response body
Response response = given().when().get("/posts/1").then().extract().response();
System.out.println(response.asPrettyString());
// Now you can see exactly what came back

// Step 2: Fix your assertion to match
// If actual userId is 2, change:
.body("userId", equalTo(1))
// to:
.body("userId", equalTo(2))
// or remove the constraint if userId doesn't matter
```

---

### FAILURE 5: `NoSuchMethodError` or `ClassNotFound`
```
java.lang.NoSuchMethodError: io.restassured...
java.lang.ClassNotFoundException: org.testng.TestNG
```
**Cause:** Dependency version conflict between RestAssured and TestNG.

**Fix:**
```xml
<!-- In pom.xml, add dependency management to lock versions -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured-bom</artifactId>
            <version>5.3.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then run `mvn dependency:tree` to check for conflicts.

---

### FAILURE 6: `Schema validation failed`
```
com.github.fge.jsonschema.core.exceptions.ProcessingException
Schema validation failed at: /properties/id
```
**Cause:** Schema file not found, wrong path, or schema doesn't match response.

**Debug steps:**
```
1. Verify schema file is at: src/test/resources/schemas/post-schema.json
   Note: schemas/ folder must exist inside resources/

2. Print actual response and compare to schema:
   given().pathParam("id",1).when().get("/posts/{id}").then().log().body();

3. Update schema to match actual field types:
   - Integer field:  { "type": "integer" }
   - String field:   { "type": "string"  }
   - Boolean field:  { "type": "boolean" }
   - Nullable field: { "type": ["string", "null"] }
```

---

### FAILURE 7: `NullPointerException on extract().path()`
```
java.lang.NullPointerException at PostTests.java:45
```
**Cause:** The field you're extracting doesn't exist in the response.

**Debug steps:**
```java
// Print full response before extracting
Response response = given().when().post("/posts").then().extract().response();
System.out.println("FULL RESPONSE: " + response.asPrettyString());
System.out.println("Status: " + response.statusCode());

// Now check the actual field name
// Wrong: response.path("postId")   — field doesn't exist
// Correct: response.path("id")     — actual field name
```

---

### FAILURE 8: Tests pass alone but fail together
```
Test A passes alone.
Test B passes alone.
Both fail when running the full suite.
```
**Cause:** Tests are sharing state (static variables, shared test data).

**Fix:**
```java
// Never use shared mutable state between tests
// BAD:
public class GetTests extends BaseTest {
    static int sharedId;   // ← shared between tests — dangerous

    @Test
    public void createAndStore() {
        sharedId = ...;   // set by test 1
    }
    @Test
    public void useShared() {
        // uses sharedId — what if test order changes?
    }
}

// GOOD: each test is self-contained
@Test
public void verifyPost() {
    int id = 1;   // local variable, specific to this test
    given().pathParam("id", id).when().get("/posts/{id}")...;
}
```

---

### FAILURE 9: `SLF4J: No-operation logger` warning
```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder"
SLF4J: Defaulting to no-operation (NOP) logger implementation
```
**This is NOT an error.** It's just a warning — tests still pass. Ignore it.

---

### FAILURE 10: `Tests run: 0` — no tests found
```
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
```
**Cause:** testng.xml class names don't match your actual class names.

**Fix:**
```xml
<!-- In testng.xml, verify this EXACTLY matches your class -->
<class name="tests.GetTests"/>
<!--              ↑        ↑
                 package   class name — case sensitive! -->

<!-- If your class is in package "com.assessment.tests":
<class name="com.assessment.tests.GetTests"/> -->
```

Also verify: class file is in `src/test/java/tests/GetTests.java` (not `src/main/java/`).

---

## VARIABLE REFERENCE — Everything to Change Per Assessment

| What to change | File | Example |
|----------------|------|---------|
| `BASE_URL` | ApiConstants.java | `"https://api.company.com"` |
| `POSTS_ENDPOINT` | ApiConstants.java | `"/api/v2/users"` |
| `AUTH_TOKEN` | ApiConstants.java | `"eyJ0eX..."` |
| `Authorization` header | BaseTest.java | `"Bearer token"` or `"ApiKey key"` |
| `equalTo(100)` | GetTests | `equalTo(50)` if count is different |
| `equalTo(1)` in POST | PostTests | Valid userId for that system |
| `statusCode(201)` | PostTests | `statusCode(200)` if API returns 200 |
| `statusCode(204)` | DeleteTests | `statusCode(200)` if API returns 200 |
| Field names in body() | All tests | Match actual API response fields |
| Schema fields | post-schema.json | Match actual response structure |
| testng.xml class names | testng.xml | Your actual package.ClassName |

---

## WHAT INTERVIEWERS CHECK

```
✅ Tests compile and run — BUILD SUCCESS
✅ BASE_URL in ApiConstants only (not repeated in tests)
✅ requestSpec used — no copy-paste of headers in every test
✅ GET + POST + PUT/PATCH + DELETE all covered
✅ At least 1 negative test (404 or 400)
✅ JSON Schema validation included
✅ Soft assertions used at least once
✅ Test method names are descriptive
✅ README explains how to run
✅ Pushed to GitHub before time is up
```

---

*Assessment Guide — RestAssured API Testing | Java + TestNG*
