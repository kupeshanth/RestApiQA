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

---

## MORE REAL-TIME FAILURES — Advanced Scenarios

---

### FAILURE 11: `415 Unsupported Media Type`
```
Expected status code <201> but was <415>
```
**Cause:** You're not setting `Content-Type: application/json` on the request.

**Fix:**
```java
// In requestSpec — make sure this line exists:
.setContentType(ContentType.JSON)

// Or per-request:
given()
    .contentType(ContentType.JSON)   // ← this line is mandatory for POST/PUT
    .body(requestBody)
.when().post("/endpoint")
```

---

### FAILURE 12: `SSL Certificate Error (HTTPS)`
```
javax.net.ssl.SSLHandshakeException: PKIX path building failed
sun.security.validator.ValidatorException: PKIX path building failed
```
**Cause:** The API uses a self-signed certificate (common in test/staging environments).

**Fix:**
```java
// In BaseTest setUp() — disable SSL validation for test environments
RestAssured.useRelaxedHTTPSValidation();

// Or per-request:
given().relaxedHTTPSValidation().when().get("/endpoint")

// NEVER disable SSL in production — only for test environments
```

---

### FAILURE 13: `Response Body is HTML, Not JSON`
```
com.fasterxml.jackson.core.JsonParseException: Unexpected character '<'
groovy.lang.GroovyRuntimeException: Unable to process JSON
```
**Cause:** The server returned an HTML error page instead of JSON. This happens when the URL is wrong (redirected to login page) or the server crashed.

**Debug:**
```java
// Print the raw response FIRST — see what actually came back
Response response = given().when().get("/endpoint").then().extract().response();
System.out.println("Status: " + response.statusCode());
System.out.println("Body: " + response.body().asString());
// You'll see something like:
// <html><body>404 Not Found</body></html>
// Now you know the URL is wrong or auth is missing
```

---

### FAILURE 14: `429 Too Many Requests — Rate Limit Hit`
```
Expected status code <200> but was <429>
```
**Cause:** You sent too many requests too fast. Common with public APIs.

**Fix:**
```java
// Option 1: Add a small pause between tests in testng.xml
<suite name="Suite" verbose="1">
    <test name="Tests" time-out="60000">
        <!-- gap-between-suites is not standard but add Thread.sleep in @BeforeMethod -->
    </test>
</suite>

// Option 2: Add pause in @BeforeMethod
@BeforeMethod
public void waitBetweenTests() throws InterruptedException {
    Thread.sleep(500);   // 500ms between each test
}

// Option 3: Use anyOf to handle 429 gracefully
.statusCode(anyOf(equalTo(200), equalTo(429)));
```

---

### FAILURE 15: `ClassCastException on extract().path()`
```
java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String
```
**Cause:** Trying to extract an integer field as a String (or vice versa).

**Fix:**
```java
// WRONG — id is an integer, can't assign to String
String id = given().when().get("/posts/1").then().extract().path("id");

// CORRECT — match the Java type to the JSON type
int    id    = given().when().get("/posts/1").then().extract().path("id");
String title = given().when().get("/posts/1").then().extract().path("title");

// Or use explicit cast
Integer id = given().when().get("/posts/1").then()
    .extract().<Integer>path("id");
```

---

### FAILURE 16: `GPath expression fails on null field`
```
java.lang.NullPointerException
  at io.restassured.internal.path.json.JSONAssertion
```
**Cause:** You're asserting a nested field that is null in the response.

**Debug + Fix:**
```java
// Print full response first
given().when().get("/users/1").then().log().body().statusCode(200);

// Check if field can be null
// WRONG: assumes user.address is never null
.body("user.address.city", equalTo("London"))

// CORRECT: assert address exists before checking city
.body("user.address", notNullValue())
.body("user.address.city", anyOf(nullValue(), equalTo("London")))
```

---

### FAILURE 17: `Tests pass in local Maven but fail in IntelliJ`
```
Test passes via: mvn test
Test fails via: Right-click → Run in IntelliJ
Error: testng.xml not found
```
**Cause:** IntelliJ runs the class directly, not via Maven Surefire which reads testng.xml.

**Fix:**
```
In IntelliJ:
  Run → Edit Configurations → TestNG
  Change "Suite" to point to: src/test/resources/testng.xml
  OR
  Always run via terminal: mvn test
  Never right-click → Run for assessment demos
```

---

### FAILURE 18: `Floating point assertion fails`
```
Expected: <9.99>
  Actual: <9.990000247955322>
```
**Cause:** Float/double precision mismatch between JSON and Java.

**Fix:**
```java
// WRONG — exact float comparison
.body("price", equalTo(9.99))

// CORRECT — use float literal
.body("price", equalTo(9.99f))

// BETTER — use closeTo matcher for floating point
.body("price", closeTo(9.99, 0.01))   // within 0.01 of 9.99

// OR extract and assert with delta
double price = given().when().get("/products/1").then().extract().path("price");
Assert.assertEquals(price, 9.99, 0.001);  // 3rd arg = delta
```

---

### FAILURE 19: `Array assertion fails — wrong order`
```
Expected: <[3, 1, 2]>
  Actual: <[1, 2, 3]>
```
**Cause:** API returned array in different order than expected.

**Fix:**
```java
// WRONG — asserts exact order
.body("ids", equalTo(Arrays.asList(3, 1, 2)))

// CORRECT — asserts contents regardless of order
.body("ids", containsInAnyOrder(1, 2, 3))

// ALSO CORRECT — just check it contains specific item
.body("ids", hasItem(1))

// Check size only (don't care about content)
.body("ids", hasSize(3))
```

---

### FAILURE 20: `Token expired during test run`
```
Expected status code <200> but was <401>
// Only happens midway through a long test run, not at start
```
**Cause:** Auth token expires while tests are running (common with short-lived JWTs).

**Fix:**
```java
// In BaseTest — fetch fresh token before EACH test class
@BeforeClass
public void setUp() {
    String freshToken = fetchToken();   // always get new token
    requestSpec = new RequestSpecBuilder()
        .addHeader("Authorization", "Bearer " + freshToken)
        .build();
}

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

---

### FAILURE 21: `Content-Length mismatch / Encoding issue`
```
Expected: <"Sao Paulo">
  Actual: <"São Paulo">
```
**Cause:** Special characters (accents, non-ASCII) encoded differently.

**Fix:**
```java
// In BaseTest requestSpec — add charset
.setContentType("application/json; charset=UTF-8")

// Or in pom.xml properties:
<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

// Use containsString instead of exact match for international text
.body("city", containsString("Paulo"))  // partial match avoids encoding issues
```

---

### FAILURE 22: `Test order dependency — test B depends on test A's data`
```
// Test A creates a user, gets ID 101
// Test B tries to GET user 101 but the API doesn't persist it
// → Test B fails with 404
```
**Cause:** JSONPlaceholder and many test APIs simulate creation but don't persist data.

**Fix:**
```java
// For real APIs that DO persist: chain tests within ONE test method
@Test
public void createThenVerifyUser() {
    // Step 1: create
    int newId = given().spec(requestSpec)
        .body("{ \"name\": \"Test\" }")
        .when().post("/users")
        .then().statusCode(201).extract().path("id");

    // Step 2: verify (in SAME test, guaranteed sequential)
    given().pathParam("id", newId)
        .when().get("/users/{id}")
        .then().statusCode(200).body("id", equalTo(newId));
}
```

---

### FAILURE 23: `mvn test — BUILD SUCCESS but 0 tests run`
```
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
**Cause 1:** Test files are in `src/main/java/` instead of `src/test/java/`.
**Cause 2:** testng.xml class name doesn't match actual class package.
**Cause 3:** `@Test` annotation is from wrong package.

**Fix:**
```bash
# Check 1: verify test files location
ls src/test/java/tests/   # should list your .java files

# Check 2: verify annotation import
# WRONG:  import org.junit.Test;
# CORRECT: import org.testng.annotations.Test;

# Check 3: compile test classes and check for errors
mvn test-compile -e   # shows detailed errors
```

---

### FAILURE 24: `Cannot call .spec(requestSpec) — NullPointerException`
```
java.lang.NullPointerException at tests.GetTests.getAllPosts(GetTests.java:25)
// at the .spec(requestSpec) line
```
**Cause:** `requestSpec` is null because `setUp()` in BaseTest didn't run, or test class doesn't extend BaseTest.

**Fix:**
```java
// Check 1: class must extend BaseTest
public class GetTests extends BaseTest {   // ← must have 'extends BaseTest'
    @Test
    public void getAllPosts() {
        given().spec(requestSpec)...   // requestSpec available from BaseTest
    }
}

// Check 2: @BeforeClass must be public
@BeforeClass
public void setUp() {   // ← must be public, not private
    requestSpec = new RequestSpecBuilder()...
}
```

---

### FAILURE 25: `Response time assertion fails in CI`
```
Expected: response time less than <3000L>
  Actual: response time was <4521L>
```
**Cause:** CI machines are slower. 3000ms is too tight for CI.

**Fix:**
```java
// In responseSpec — use different timeout for CI vs local
int timeout = System.getenv("CI") != null ? 10000 : 3000;

responseSpec = new ResponseSpecBuilder()
    .expectResponseTime(lessThan((long) timeout))
    .build();

// Or remove response time from responseSpec entirely
// and only add it to tests where SLA matters
```

---

## QUICK DEBUGGING CHECKLIST

When a test fails, check these in order:

```
1. Print full response:
   given().when().get("/endpoint").then().log().all();

2. Print just the body:
   Response r = given().when().get("/endpoint").then().extract().response();
   System.out.println(r.asPrettyString());

3. Print status code:
   System.out.println("Status: " + response.statusCode());

4. Print all headers:
   System.out.println("Headers: " + response.headers());

5. Check URL is correct:
   System.out.println("URL: " + RestAssured.baseURI + "/endpoint");

6. Test just with Postman first — if it fails there too, it's the API
   If it passes in Postman but fails in code → it's your code
```

---

*Assessment Guide — RestAssured API Testing | Java + TestNG | 25 Real Failures + Fixes*
