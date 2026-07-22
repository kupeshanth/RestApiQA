# IntelliJ IDEA + RestAssured — Complete Setup Guide
## From Zero to Running API Tests | Every File, Every Step, Every Location

---

## WHAT YOU WILL HAVE AT THE END

```
your-api-project/
├── pom.xml                                  ← Maven config + all dependencies
└── src/
    └── test/
        ├── java/
        │   ├── base/
        │   │   └── BaseTest.java            ← shared setup (runs before every test)
        │   ├── tests/
        │   │   ├── GetTests.java            ← all GET request tests
        │   │   ├── PostTests.java           ← all POST request tests
        │   │   ├── PutTests.java            ← all PUT/PATCH tests
        │   │   └── DeleteTests.java         ← all DELETE tests
        │   └── utils/
        │       └── ApiConstants.java        ← base URL, endpoints, auth tokens
        └── resources/
            ├── testng.xml                   ← runs all test classes as a suite
            └── schemas/
                └── response-schema.json     ← JSON schema for contract validation
```

**Goal:** Running `mvn test` → all tests pass → `BUILD SUCCESS`

---

## PART 1 — INSTALL THE TOOLS

---

### Step 1.1 — Install Java JDK 11+

**Check if already installed:**
```bash
java -version
```
Expected output:
```
openjdk version "11.0.x" or higher
```

**If not installed:**
1. Go to: **https://adoptium.net/**
2. Click **"Latest LTS Release"** (choose JDK 17 or 11)
3. Download Windows installer (`.msi`)
4. Run installer → click Next → Next → Finish
5. **Close and reopen** any terminal windows
6. Run `java -version` again — should now show version

---

### Step 1.2 — Install IntelliJ IDEA Community Edition

1. Go to: **https://www.jetbrains.com/idea/download/**
2. Scroll down to **Community Edition** (FREE — do not pay for Ultimate)
3. Click **Download**
4. Run the `.exe` installer
5. During install, tick these options:
   ```
   ✅ Add "Open Folder as Project"
   ✅ Add launchers dir to the PATH
   ✅ .java file association
   ```
6. Click Install → Finish → Reboot if asked

---

### Step 1.3 — Install Maven (optional — IntelliJ has built-in Maven)

IntelliJ includes a bundled Maven. You only need to install Maven separately if you want to run `mvn test` from the terminal.

**Check if Maven is installed:**
```bash
mvn -version
```

**If not installed and you want terminal access:**
```bash
# Open PowerShell as Administrator and run:
Set-ExecutionPolicy Bypass -Scope Process -Force
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
choco install maven -y
# Close and reopen terminal
mvn -version   # should show Apache Maven 3.x.x
```

**If Chocolatey doesn't work — download Maven portable:**
```bash
# In PowerShell inside your project folder:
curl -L -o maven.zip "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
unzip maven.zip -d .
# Then use: ./apache-maven-3.9.6/bin/mvn test
```

---

## PART 2 — CREATE THE PROJECT IN INTELLIJ

---

### Step 2.1 — Create a New Maven Project

1. Open IntelliJ IDEA
2. Click **"New Project"** (on the welcome screen)
3. In the left panel, select **"Maven Archetype"**
4. Fill in:
   ```
   Name:       api-test-project         ← your project name
   Location:   C:\Users\YourName\projects\api-test-project
   JDK:        11 or 17                 ← click dropdown, select installed JDK
   Archetype:  maven-archetype-quickstart
   ```
5. Click **Create**

IntelliJ creates the project and opens `pom.xml` automatically.

---

### Step 2.2 — OR Open an Existing Project

If you already have a project folder (like the one we built):

1. Open IntelliJ IDEA
2. Click **"Open"**
3. Navigate to your project folder (the one with `pom.xml`)
4. Click **OK**
5. If asked "Trust this project?" → click **"Trust Project"**
6. IntelliJ reads `pom.xml` and downloads all dependencies automatically
7. Wait for the loading bar at the bottom to complete (1-3 minutes first time)

---

### Step 2.3 — Create the Folder Structure

IntelliJ may create a basic structure. You need this exact structure:

**In IntelliJ:**
1. Right-click `src/test` → **New → Directory**
2. Type `java/base` → press Enter
3. Repeat for:
   - `java/tests`
   - `java/utils`
   - `resources/schemas`

**OR in terminal inside IntelliJ (View → Tool Windows → Terminal):**
```bash
mkdir -p src/test/java/base
mkdir -p src/test/java/tests
mkdir -p src/test/java/utils
mkdir -p src/test/resources/schemas
```

**Mark folders correctly in IntelliJ:**
- Right-click `src/test/java` → **Mark Directory as → Test Sources Root** (turns blue)
- Right-click `src/test/resources` → **Mark Directory as → Test Resources Root** (turns orange)

---

## PART 3 — CREATE ALL FILES

---

### File 1: `pom.xml` — The Heart of the Project

**Location:** Root folder of project (same level as `src/`)

**Full content — paste and replace everything:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- ===== PROJECT IDENTITY ===== -->
    <!-- Change groupId to your company/package name -->
    <groupId>com.apitest</groupId>
    <artifactId>api-test-project</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <!-- ===== JAVA VERSION ===== -->
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- ===== DEPENDENCIES (libraries your project uses) ===== -->
    <dependencies>

        <!-- RestAssured: the main library for API testing -->
        <!-- Lets you write: given().when().get("/posts").then().statusCode(200) -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>5.3.1</version>
            <scope>test</scope>
        </dependency>

        <!-- JSON Schema Validator: validates response structure against a schema file -->
        <!-- Lets you write: .body(matchesJsonSchemaInClasspath("schemas/post-schema.json")) -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>json-schema-validator</artifactId>
            <version>5.3.1</version>
            <scope>test</scope>
        </dependency>

        <!-- TestNG: the test runner — runs your @Test methods and generates reports -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.8.0</version>
            <scope>test</scope>
        </dependency>

        <!-- Jackson: converts Java objects to/from JSON automatically -->
        <!-- Useful when you want to use Java objects as request bodies -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
            <scope>test</scope>
        </dependency>

    </dependencies>

    <!-- ===== BUILD CONFIGURATION ===== -->
    <build>
        <plugins>
            <!-- Surefire: runs your TestNG tests when you do 'mvn test' -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <!-- Points to your testng.xml suite file -->
                    <suiteXmlFiles>
                        <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
                    </suiteXmlFiles>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

**After saving pom.xml:**
- IntelliJ shows a small popup at the top right: **"Maven build scripts found"** → click **"Load Maven Project"**
- OR click the Maven icon on the right sidebar → click the refresh (↻) button
- Watch the bottom bar — it downloads RestAssured, TestNG, etc. (first time: 2-5 minutes)

---

### File 2: `ApiConstants.java` — All URLs and Config in One Place

**Location:** `src/test/java/utils/ApiConstants.java`

**How to create in IntelliJ:**
1. Right-click the `utils` folder
2. New → Java Class
3. Name: `ApiConstants`
4. Click OK

**Full content:**

```java
package utils;

/**
 * Central place for all API configuration.
 * Change BASE_URL here and all tests automatically use the new URL.
 * Never hardcode URLs or tokens directly in test files.
 */
public class ApiConstants {

    // ═══════════════════════════════════════════════════════════
    // BASE URL — change this to switch between environments
    // ═══════════════════════════════════════════════════════════
    public static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    // For staging:    "https://staging.yourapi.com"
    // For production: "https://api.yourapi.com"
    // For localhost:  "http://localhost:8080"

    // ═══════════════════════════════════════════════════════════
    // API ENDPOINTS
    // Add new endpoints here as you test more APIs
    // ═══════════════════════════════════════════════════════════
    public static final String POSTS_ENDPOINT    = "/posts";
    public static final String USERS_ENDPOINT    = "/users";
    public static final String COMMENTS_ENDPOINT = "/comments";
    public static final String TODOS_ENDPOINT    = "/todos";

    // ═══════════════════════════════════════════════════════════
    // AUTHENTICATION
    // Fill these in when the API requires auth
    // ═══════════════════════════════════════════════════════════
    public static final String AUTH_TOKEN  = "";   // "Bearer eyJ0eXAi..."
    public static final String API_KEY     = "";   // "your-api-key-here"
    public static final String USERNAME    = "";   // for Basic Auth
    public static final String PASSWORD    = "";   // for Basic Auth

    // ═══════════════════════════════════════════════════════════
    // TIMEOUTS
    // ═══════════════════════════════════════════════════════════
    public static final int RESPONSE_TIMEOUT_MS = 3000;  // fail if response > 3 seconds
}
```

---

### File 3: `BaseTest.java` — Shared Setup for All Tests

**Location:** `src/test/java/base/BaseTest.java`

**How to create:** Right-click `base` folder → New → Java Class → `BaseTest`

**Full content:**

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

/**
 * BaseTest — parent class for all test classes.
 *
 * Every test class EXTENDS this class:
 *   public class GetTests extends BaseTest { ... }
 *
 * This means every test class automatically gets:
 *   - RestAssured.baseURI already set
 *   - requestSpec with Content-Type and auth headers
 *   - responseSpec with content type and timeout checks
 */
public class BaseTest {

    // These are available in all test classes via inheritance
    protected RequestSpecification  requestSpec;
    protected ResponseSpecification responseSpec;

    @BeforeClass  // Runs ONCE before the first test in each test class
    public void setUp() {

        // Set the base URL once — tests only need to write "/posts" not the full URL
        RestAssured.baseURI = ApiConstants.BASE_URL;

        // Prints the full request + response ONLY when a test FAILS
        // Extremely useful for debugging — you see exactly what went wrong
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Build a reusable request setup
        // Apply in tests with: given().spec(requestSpec)...
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)   // all requests send JSON
            .setAccept(ContentType.JSON);        // all requests expect JSON back

        // ── ADD AUTH IF NEEDED ────────────────────────────────────────────────
        // Uncomment whichever auth type the API uses:

        // Bearer Token:
        // if (!ApiConstants.AUTH_TOKEN.isEmpty()) {
        //     requestSpecBuilder.addHeader("Authorization", "Bearer " + ApiConstants.AUTH_TOKEN);
        // }

        // API Key in header:
        // if (!ApiConstants.API_KEY.isEmpty()) {
        //     requestSpecBuilder.addHeader("x-api-key", ApiConstants.API_KEY);
        // }

        // API Key as query parameter:
        // requestSpecBuilder.addQueryParam("api_key", ApiConstants.API_KEY);

        requestSpec = requestSpecBuilder.build();

        // Build reusable response assertions
        // Apply in tests with: .then().spec(responseSpec)...
        responseSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan((long) ApiConstants.RESPONSE_TIMEOUT_MS))
            .build();
    }
}
```

---

### File 4: `GetTests.java` — All GET Request Tests

**Location:** `src/test/java/tests/GetTests.java`

**How to create:** Right-click `tests` folder → New → Java Class → `GetTests`

**Full content:**

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

/**
 * All GET request tests.
 * Covers: get all, get by ID, filter by query param, 404, headers, schema, response time.
 */
public class GetTests extends BaseTest {

    // ─────────────────────────────────────────────────────────────
    // TEST 1: GET all resources
    // Sends:   GET https://jsonplaceholder.typicode.com/posts
    // Expects: 200, array of 100 items, correct fields
    // ─────────────────────────────────────────────────────────────
    @Test(description = "GET all posts returns 200 with 100 items and correct fields")
    public void getAllPosts() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .body("size()",        equalTo(100))      // array has exactly 100 items
            .body("[0].id",        notNullValue())     // first item has id
            .body("[0].title",     not(emptyString())) // first item has non-empty title
            .body("[0].userId",    notNullValue())     // first item has userId
            .contentType("application/json")           // response is JSON
            .time(lessThan(3000L));                    // responded in under 3 seconds
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 2: GET single resource by path parameter
    // Sends:   GET /posts/1
    // Expects: 200, post with id=1, all required fields
    // ─────────────────────────────────────────────────────────────
    @Test(description = "GET post by ID returns 200 with correct data")
    public void getPostById() {
        int postId = 1;   // the ID to request

        given()
            .pathParam("id", postId)          // {id} in URL gets replaced with postId
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")   // sends: GET /posts/1
        .then()
            .statusCode(200)
            .body("id",     equalTo(postId))       // id matches what we requested
            .body("title",  not(emptyString()))    // title is not empty
            .body("body",   not(emptyString()))    // body text is not empty
            .body("userId", notNullValue());       // userId exists
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 3: GET with query parameter (filtering)
    // Sends:   GET /posts?userId=1
    // Expects: 200, only posts belonging to user 1
    // ─────────────────────────────────────────────────────────────
    @Test(description = "GET posts filtered by userId returns only that user's posts")
    public void getPostsByUserId() {
        int filterUserId = 1;

        given()
            .queryParam("userId", filterUserId)     // adds ?userId=1 to the URL
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)       // GET /posts?userId=1
        .then()
            .statusCode(200)
            .body("size()",  equalTo(10))           // user 1 has exactly 10 posts
            .body("userId",  everyItem(equalTo(filterUserId))); // every post has userId=1
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 4: GET resource that does not exist → 404
    // Sends:   GET /posts/99999
    // Expects: 404 Not Found
    // ─────────────────────────────────────────────────────────────
    @Test(description = "GET non-existent post returns 404 Not Found")
    public void getNonExistentPost() {
        given()
            .pathParam("id", 99999)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(404);
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 5: GET and validate JSON Schema (contract test)
    // Sends:   GET /posts/1
    // Expects: 200, response matches schema in post-schema.json
    // ─────────────────────────────────────────────────────────────
    @Test(description = "GET single post matches JSON schema contract")
    public void getPostMatchesSchema() {
        given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
        // If any field is renamed or type changes → this test catches it
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 6: GET and verify response headers
    // ─────────────────────────────────────────────────────────────
    @Test(description = "GET response has correct Content-Type header")
    public void getResponseHasJsonContentType() {
        given()
        .when()
            .get(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"));
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 7: GET and extract value for use in assertions
    // ─────────────────────────────────────────────────────────────
    @Test(description = "GET extracts title and verifies it is not empty")
    public void getAndExtractTitle() {
        String title = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().path("title");   // extract the "title" field value

        System.out.println("Extracted title: " + title);
        Assert.assertFalse(title.isEmpty(), "Title should not be empty");
        Assert.assertTrue(title.length() > 3, "Title should be longer than 3 characters");
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 8: GET and use soft assertions (all fields validated together)
    // ─────────────────────────────────────────────────────────────
    @Test(description = "Validate all fields of a post using soft assertions")
    public void validateAllFieldsSoftly() {
        Response response = given()
            .pathParam("id", 1)
        .when()
            .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().response();

        // Soft assertions — all run even if one fails
        SoftAssert soft = new SoftAssert();
        soft.assertEquals((int) response.path("id"),     1,   "id should be 1");
        soft.assertNotNull(response.path("title"),             "title should not be null");
        soft.assertNotNull(response.path("body"),              "body should not be null");
        soft.assertEquals((int) response.path("userId"), 1,   "userId should be 1");
        soft.assertAll();   // MANDATORY — reports all failures, never skip this
    }
}
```

---

### File 5: `PostTests.java` — All POST Request Tests

**Location:** `src/test/java/tests/PostTests.java`

```java
package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PostTests extends BaseTest {

    // ─────────────────────────────────────────────────────────────
    // TEST 1: POST valid data → 201 Created
    // ─────────────────────────────────────────────────────────────
    @Test(description = "POST creates a new post and returns 201 with generated ID")
    public void createPost() {
        // The JSON body to send — matches what the API expects
        String requestBody = "{ " +
            "\"title\":  \"My Test Post\", " +
            "\"body\":   \"This is the test body content\", " +
            "\"userId\": 1 " +
        "}";

        int newId = given()
            .spec(requestSpec)      // applies Content-Type: application/json
            .body(requestBody)      // the JSON body
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)                              // 201 = Created
            .body("id",    notNullValue())                // server assigned an id
            .body("title", equalTo("My Test Post"))      // title matches what we sent
            .body("userId", equalTo(1))                  // userId matches
            .extract().path("id");                        // capture the new ID

        System.out.println("Created resource ID: " + newId);
        Assert.assertTrue(newId > 0, "New ID should be a positive number");
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 2: POST — verify response Content-Type
    // ─────────────────────────────────────────────────────────────
    @Test(description = "POST response is application/json")
    public void createPostReturnsJson() {
        given()
            .spec(requestSpec)
            .body("{ \"title\": \"Test\", \"body\": \"Body\", \"userId\": 1 }")
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(201)
            .contentType(containsString("application/json"));
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 3: POST with empty body — negative test
    // Real APIs return 400, JSONPlaceholder returns 201 (lenient)
    // ─────────────────────────────────────────────────────────────
    @Test(description = "POST with empty body — verify API handles it")
    public void createPostEmptyBody() {
        given()
            .spec(requestSpec)
            .body("{}")
        .when()
            .post(ApiConstants.POSTS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(400), equalTo(201)));
        // 400 = correct behaviour for real APIs
        // 201 = JSONPlaceholder is lenient and accepts empty bodies
    }
}
```

---

### File 6: `PutTests.java` — PUT and PATCH Tests

**Location:** `src/test/java/tests/PutTests.java`

```java
package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PutTests extends BaseTest {

    // ─────────────────────────────────────────────────────────────
    // TEST 1: PUT — full replacement
    // PUT sends ALL fields. Missing fields may be wiped.
    // ─────────────────────────────────────────────────────────────
    @Test(description = "PUT replaces entire post and returns 200")
    public void updatePostWithPut() {
        String fullBody = "{ " +
            "\"id\":    1, " +
            "\"title\": \"Fully Replaced Title\", " +
            "\"body\":  \"Fully replaced body content\", " +
            "\"userId\": 1 " +
        "}";

        given()
            .spec(requestSpec)
            .body(fullBody)
            .pathParam("id", 1)
        .when()
            .put(ApiConstants.POSTS_ENDPOINT + "/{id}")   // PUT /posts/1
        .then()
            .statusCode(200)
            .body("title", equalTo("Fully Replaced Title"))
            .body("body",  equalTo("Fully replaced body content"))
            .body("id",    equalTo(1));
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 2: PATCH — partial update
    // PATCH sends ONLY the fields you want to change.
    // ─────────────────────────────────────────────────────────────
    @Test(description = "PATCH updates only the title field and returns 200")
    public void updateTitleWithPatch() {
        // Only sending title — other fields stay unchanged
        String partialBody = "{ \"title\": \"Only Title Was Changed\" }";

        given()
            .spec(requestSpec)
            .body(partialBody)
            .pathParam("id", 1)
        .when()
            .patch(ApiConstants.POSTS_ENDPOINT + "/{id}")  // PATCH /posts/1
        .then()
            .statusCode(200)
            .body("id",    equalTo(1))                         // id unchanged
            .body("title", equalTo("Only Title Was Changed")); // title changed
    }
}
```

---

### File 7: `DeleteTests.java` — DELETE Tests

**Location:** `src/test/java/tests/DeleteTests.java`

```java
package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import utils.ApiConstants;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class DeleteTests extends BaseTest {

    // ─────────────────────────────────────────────────────────────
    // TEST 1: DELETE existing resource → 200
    // ─────────────────────────────────────────────────────────────
    @Test(description = "DELETE post returns 200 OK")
    public void deletePost() {
        given()
            .pathParam("id", 1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")  // DELETE /posts/1
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));
            // 200 = OK with body (JSONPlaceholder uses this)
            // 204 = No Content (many real APIs use this)
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 2: DELETE and verify response body is empty
    // ─────────────────────────────────────────────────────────────
    @Test(description = "DELETE response body is empty object")
    public void deletePostReturnsEmptyBody() {
        given()
            .pathParam("id", 1)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .body(equalTo("{}"));   // JSONPlaceholder returns empty JSON object
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 3: DELETE non-existent resource
    // ─────────────────────────────────────────────────────────────
    @Test(description = "DELETE non-existent resource — check API handles gracefully")
    public void deleteNonExistentPost() {
        given()
            .pathParam("id", 99999)
        .when()
            .delete(ApiConstants.POSTS_ENDPOINT + "/{id}")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)));
            // 404 = correct for most real APIs
            // 200 = JSONPlaceholder returns 200 even for non-existent IDs
    }
}
```

---

### File 8: `testng.xml` — Runs All Tests as a Suite

**Location:** `src/test/resources/testng.xml`

**How to create:** Right-click `resources` folder → New → File → `testng.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">

<!--
  TestNG Suite Configuration
  This file tells TestNG which test classes to run and in what order.
  Run via IntelliJ: right-click this file → Run
  Run via terminal: mvn test
-->
<suite name="API Test Suite" verbose="1">

    <!-- verbose="1" shows test names in output -->
    <!-- verbose="2" shows more detail -->

    <test name="GET Tests">
        <classes>
            <class name="tests.GetTests"/>
            <!--         ↑ package.ClassName — must match exactly -->
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

### File 9: `post-schema.json` — JSON Schema for Contract Validation

**Location:** `src/test/resources/schemas/post-schema.json`

**How to create:** Right-click `schemas` folder → New → File → `post-schema.json`

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "title": "Post Response Schema",
  "description": "Validates the structure of a single post response",
  "type": "object",
  "properties": {
    "id": {
      "type": "integer",
      "description": "Unique identifier — must be a whole number"
    },
    "title": {
      "type": "string",
      "description": "Post title — must be a string"
    },
    "body": {
      "type": "string",
      "description": "Post body content — must be a string"
    },
    "userId": {
      "type": "integer",
      "description": "ID of the user who created the post — must be a whole number"
    }
  },
  "required": ["id", "title", "body", "userId"]
}
```

**What this does:**
If the API ever renames `userId` to `user_id`, or changes `id` from integer to string, the schema test immediately fails and alerts you to a breaking API change.

---

## PART 4 — RUN YOUR TESTS

---

### Method A: Run from IntelliJ (easiest during development)

```
Right-click any test METHOD → Run 'methodName'      ← runs ONE test
Right-click a test CLASS   → Run 'ClassName'        ← runs all tests in that class
Right-click testng.xml     → Run 'testng.xml'       ← runs the FULL suite
```

**Keyboard shortcut:**
- Place cursor inside a test method → `Ctrl + Shift + F10` → runs that test

---

### Method B: Run from Terminal

Open terminal inside IntelliJ: **View → Tool Windows → Terminal**

```bash
# Run all tests (uses testng.xml)
mvn test

# Run only one test class
mvn test -Dtest=GetTests

# Run only one specific method
mvn test -Dtest=GetTests#getAllPosts

# Clean old results then run all
mvn clean test

# Run and filter output (PowerShell)
mvn test 2>&1 | Select-String "Tests run|BUILD|FAIL"
```

---

### Expected output when all pass:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running TestSuite
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 12.4 s
[INFO]
[INFO] Results:
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### View HTML Report after running:

```
target/surefire-reports/index.html
```

Open in browser: right-click the file in IntelliJ → **Open In → Browser**

---

## PART 5 — INTELLIJ TIPS FOR API TESTING

---

### Useful IntelliJ Features

**Auto-import missing classes:**
```
When you see red underlines on RestAssured, equalTo, etc.:
Press Alt + Enter → "Import class"
OR press Ctrl + Shift + O → "Optimize imports" (imports everything missing)
```

**Code completion:**
```
Type: .body("id", → IntelliJ suggests: equalTo, notNullValue, containsString, etc.
Type: given(). → IntelliJ shows all RestAssured methods
```

**Run configuration — save your test run:**
```
Run → Edit Configurations → + → TestNG
Name: "Full Suite"
Suite: browse to src/test/resources/testng.xml
Save → now you can run the full suite from the top toolbar
```

**Quick fixes:**
```
Red underline on class name → Alt + Enter → "Create class"
Red underline on method     → Alt + Enter → "Create method"
Yellow warning              → Alt + Enter → see options
```

**Format code:**
```
Ctrl + Alt + L  → format entire file (fixes indentation)
```

**Find in all files:**
```
Ctrl + Shift + F  → search across all project files
```

---

## PART 6 — COMMON SETUP PROBLEMS

---

### Problem: Red underlines everywhere — imports not resolved

**Cause:** Maven dependencies not downloaded yet.

**Fix:**
```
1. Click the Maven tab on the right sidebar
2. Click the refresh/reload icon (↻)
3. Wait for "Downloading..." to complete at the bottom
4. The red underlines disappear
```

---

### Problem: `java.lang.NoClassDefFoundError: io/restassured/...`

**Cause:** RestAssured JAR not in classpath.

**Fix:**
```
1. Open pom.xml
2. Check the RestAssured dependency is there with scope "test"
3. Click the Maven refresh icon
4. Run mvn dependency:resolve in terminal
```

---

### Problem: `Error: Could not find or load main class`

**Cause:** Running as Java application instead of TestNG test.

**Fix:**
```
Right-click the test file → Run 'GetTests' (TestNG icon — green play button with T)
NOT: Run 'GetTests.main()' (that's a Java app, not a TestNG test)
```

---

### Problem: `testng.xml: tests.GetTests — Cannot load class`

**Cause:** Class name in testng.xml doesn't match actual package or class name.

**Fix:**
```xml
<!-- In testng.xml — must be EXACTLY: package.ClassName -->
<class name="tests.GetTests"/>

<!-- If your package is com.company.tests: -->
<class name="com.company.tests.GetTests"/>

<!-- Check: open GetTests.java — first line must be: -->
package tests;   ← this is the package name to use in testng.xml
```

---

### Problem: `SLF4J: No-operation (NOP) logger` warning

```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
```

**This is NOT an error.** Tests still run and pass. Ignore this message safely.

---

### Problem: Maven not found — `mvn` command not recognized

**Fix — use IntelliJ's built-in Maven:**
```
1. View → Tool Windows → Maven
2. Expand your project → Lifecycle → double-click "test"
3. This runs mvn test using IntelliJ's built-in Maven — no install needed
```

---

## FINAL CHECK — Everything Should Be Here

```
✅ pom.xml                          — in root folder
✅ src/test/java/utils/ApiConstants.java    — BASE_URL and endpoints
✅ src/test/java/base/BaseTest.java         — requestSpec setup
✅ src/test/java/tests/GetTests.java        — 8 GET tests
✅ src/test/java/tests/PostTests.java       — 3 POST tests
✅ src/test/java/tests/PutTests.java        — 2 PUT/PATCH tests
✅ src/test/java/tests/DeleteTests.java     — 3 DELETE tests
✅ src/test/resources/testng.xml            — suite runner
✅ src/test/resources/schemas/post-schema.json  — contract schema

Total: 9 files, 16 tests
Run: mvn test
Expected: Tests run: 16, Failures: 0, BUILD SUCCESS
```

---

## KEYBOARD SHORTCUTS CHEAT SHEET

| Action | Shortcut |
|--------|---------|
| Run current test | `Ctrl + Shift + F10` |
| Rerun last test | `Ctrl + F5` |
| Fix error / import | `Alt + Enter` |
| Code completion | `Ctrl + Space` |
| Format file | `Ctrl + Alt + L` |
| Go to definition | `Ctrl + Click` |
| Find in files | `Ctrl + Shift + F` |
| Comment/uncomment | `Ctrl + /` |
| Duplicate line | `Ctrl + D` |
| Delete line | `Ctrl + Y` |
| Rename (refactor) | `Shift + F6` |
| Open terminal | `Alt + F12` |

---

---

## PART 7 — TROUBLESHOOTING + DEBUG STEPS FOR EVERY REAL FAILURE

---

### HOW TO DEBUG ANY FAILING TEST — START HERE

When a test fails, do these steps in order before anything else:

```java
// STEP 1: Add full logging to see exactly what was sent and received
given()
    .log().all()          // print the full REQUEST
.when()
    .get(ApiConstants.POSTS_ENDPOINT)
.then()
    .log().all()          // print the full RESPONSE
    .statusCode(200);

// STEP 2: Print just the response body
Response response = given().when().get("/posts/1").then().extract().response();
System.out.println("Status:  " + response.statusCode());
System.out.println("Headers: " + response.headers());
System.out.println("Body:    " + response.asPrettyString());

// STEP 3: Use RestAssured's built-in failure logging (already in BaseTest)
// This prints request + response automatically ONLY when test fails:
RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
// No extra code needed in tests — this is set in BaseTest.setUp()
```

---

### FAILURE 1: Red Underlines — Class Not Found (Won't Compile)

```
Cannot resolve symbol 'given'
Cannot resolve symbol 'equalTo'
Cannot resolve symbol 'ContentType'
```

**What happened:** Maven dependencies aren't downloaded or imported yet.

**Debug steps:**
```
Step 1: Click Maven panel (right side) → click ↻ Reload button
        Wait for download to finish (progress bar at bottom)

Step 2: If still red → Alt + Enter on the red word → "Import class"
        Select: import static io.restassured.RestAssured.given;

Step 3: If still failing → check pom.xml:
        - restassured dependency must have <scope>test</scope>
        - version must be 5.3.1
        
Step 4: Terminal → mvn dependency:resolve
        Look for errors in output — a missing dependency will say "FAILED"

Step 5: File → Invalidate Caches → Restart IntelliJ
        Sometimes IntelliJ's cache is stale — this fixes it
```

---

### FAILURE 2: `Expected status code <200> but was <404>`

```
java.lang.AssertionError: 1 expectation failed.
Expected status code <200> but was <404>
```

**What happened:** Wrong URL, wrong endpoint path, or resource doesn't exist.

**Debug steps:**
```java
// Step 1: Print the EXACT URL RestAssured is calling
System.out.println("Full URL: " + RestAssured.baseURI + ApiConstants.POSTS_ENDPOINT);

// Step 2: Add request logging to see what was sent
given().log().all().when().get(ApiConstants.POSTS_ENDPOINT).then().statusCode(200);

// Step 3: Open that URL in your browser
// https://jsonplaceholder.typicode.com/posts  ← does it load?

// Step 4: Check ApiConstants.BASE_URL — no trailing slash!
// ✅ CORRECT: "https://jsonplaceholder.typicode.com"
// ❌ WRONG:   "https://jsonplaceholder.typicode.com/"   ← extra slash

// Step 5: Check the endpoint constant
// ✅ CORRECT: "/posts"
// ❌ WRONG:   "posts"   ← missing leading slash
```

---

### FAILURE 3: `Expected status code <200> but was <401>`

```
java.lang.AssertionError: 1 expectation failed.
Expected status code <200> but was <401>
```

**What happened:** API requires authentication but you're not sending a token.

**Debug steps:**
```java
// Step 1: Print response body — it will say what auth it needs
Response r = given().when().get("/protected").then().extract().response();
System.out.println(r.asPrettyString());
// Output might say: "error": "Unauthorized", "message": "Bearer token required"

// Step 2: Add auth to BaseTest requestSpec
requestSpec = new RequestSpecBuilder()
    .setContentType(ContentType.JSON)
    .addHeader("Authorization", "Bearer " + ApiConstants.AUTH_TOKEN)  // ← add this
    .build();

// Step 3: If you need to GET the token first (OAuth2 login flow)
String token = given()
    .contentType(ContentType.JSON)
    .body("{ \"username\": \"admin\", \"password\": \"secret\" }")
.when()
    .post("/auth/login")
.then()
    .statusCode(200)
    .extract().path("token");   // ← capture token from login response

// Then use it:
given()
    .header("Authorization", "Bearer " + token)
.when()
    .get("/protected")
.then()
    .statusCode(200);
```

---

### FAILURE 4: `JSON path X doesn't match`

```
java.lang.AssertionError: 1 expectation failed.
JSON path title doesn't match.
Expected: My Test Post
  Actual: My Test Post Updated
```

**What happened:** The actual response value doesn't match your expected value.

**Debug steps:**
```java
// Step 1: Print the full response to see the ACTUAL structure
given()
    .pathParam("id", 1)
.when()
    .get(ApiConstants.POSTS_ENDPOINT + "/{id}")
.then()
    .log().body()          // prints just the body
    .statusCode(200);

// Step 2: Common causes:
// - Field name is different: "user_id" not "userId"
// - Value is different: 2 not 1
// - Field is nested: "user.id" not "id"
// - Field is in an array: "[0].id" not "id"

// Step 3: Extract and print the actual value
String actualTitle = given().when().get("/posts/1")
    .then().extract().path("title");
System.out.println("ACTUAL title: " + actualTitle);
// Now fix your assertion to match the actual value
```

---

### FAILURE 5: `Groovy.lang.MissingPropertyException` — Wrong Field Name

```
groovy.lang.MissingPropertyException: No such property: userId for class: ...
```

**What happened:** The field name you used in `.body()` doesn't exist in the response.

**Debug steps:**
```java
// Step 1: Print the full response body to see REAL field names
given().when().get("/posts/1").then().log().body();

// Output:
// {
//   "id": 1,
//   "title": "sunt aut facere...",
//   "body": "quia et suscipit...",
//   "userId": 1           ← this is the real field name
// }

// Step 2: Match your assertion to the EXACT field name (case-sensitive)
// ❌ .body("user_id", equalTo(1))    ← wrong, underscore
// ✅ .body("userId",  equalTo(1))    ← correct, camelCase

// Step 3: For nested fields use dot notation
// Response: { "user": { "address": { "city": "London" } } }
.body("user.address.city", equalTo("London"))

// Step 4: For array items use index notation
// Response: [ {"id":1}, {"id":2}, {"id":3} ]
.body("[0].id", equalTo(1))    // first item
.body("[2].id", equalTo(3))    // third item
```

---

### FAILURE 6: `NullPointerException at extract().path()`

```
java.lang.NullPointerException
  at tests.PostTests.createPost(PostTests.java:35)
```

**What happened:** The field you're trying to extract doesn't exist in the response.

**Debug steps:**
```java
// Step 1: Print full response BEFORE trying to extract
Response response = given()
    .spec(requestSpec)
    .body(requestBody)
.when()
    .post(ApiConstants.POSTS_ENDPOINT)
.then()
    .extract().response();

System.out.println("Status: " + response.statusCode());
System.out.println("Body: " + response.asPrettyString());
// Now you can see the real field name to extract

// Step 2: The issue — wrong field name
// Response: { "id": 101, "title": "..." }
int id = given().when().post("/posts").then()
    .extract().path("postId");   // ❌ field is "id" not "postId"

// Fix:
int id = given().when().post("/posts").then()
    .extract().path("id");       // ✅ correct field name

// Step 3: Handle null safely
Integer id = given().when().post("/posts").then()
    .extract().<Integer>path("id");   // explicit generic type
Assert.assertNotNull(id, "id should not be null");
```

---

### FAILURE 7: `Schema validation failed`

```
com.github.fge.jsonschema.core.exceptions.ProcessingException:
  fatal: /properties/id: required key [id] not found
```

**What happened:** Response structure doesn't match `post-schema.json`, or schema file not found.

**Debug steps:**
```
Step 1: Verify schema file exists at the RIGHT location:
        src/test/resources/schemas/post-schema.json
        ← NOT src/main/resources/
        ← NOT src/test/java/
        The file MUST be inside src/test/resources/

Step 2: Print the actual response to compare with schema
        given().pathParam("id",1).when().get("/posts/{id}").then().log().body();

Step 3: Check the schema JSON is valid:
        Open post-schema.json → look for syntax errors
        Paste it into: https://jsonlint.com to validate

Step 4: Update schema to match actual response fields
        If response has "user_id" but schema says "userId" → schema fails
        Fix: change "userId" → "user_id" in schema

Step 5: Make a field optional (not required)
        Remove it from the "required" array in schema
        "required": ["id", "title", "body"]   ← removed "userId"
```

---

### FAILURE 8: `Tests run: 0` — No Tests Found

```
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**What happened:** TestNG couldn't find or load your test classes.

**Debug steps:**
```
Step 1: Check testng.xml class names match EXACTLY
        Open GetTests.java → first line says: package tests;
        In testng.xml: <class name="tests.GetTests"/>
                                     ↑ match!

Step 2: Check test files are in src/test/java/ NOT src/main/java/
        In IntelliJ: src/test/java should be BLUE (marked as Test Sources Root)
        If it's grey: right-click → Mark Directory As → Test Sources Root

Step 3: Check @Test annotation is from the right package
        CORRECT: import org.testng.annotations.Test;
        WRONG:   import org.junit.Test;
        If you see import junit — delete it and re-import TestNG

Step 4: Run mvn test-compile to check for compilation errors
        If classes don't compile, TestNG can't find them
```

---

### FAILURE 9: `Connection refused` — API Not Reachable

```
java.net.ConnectException: Connection refused: connect
io.restassured.exception.PathException: Connection refused
```

**What happened:** The API server is not running or the URL is wrong.

**Debug steps:**
```
Step 1: Open the URL in your browser
        https://jsonplaceholder.typicode.com/posts
        Does it load? YES → URL is fine, problem is in your code
                     NO  → URL wrong, or you need VPN, or server is down

Step 2: Check for typos in BASE_URL
        ✅ "https://jsonplaceholder.typicode.com"
        ❌ "https://jsonplaeholder.typicode.com"   ← typo
        ❌ "http://jsonplaceholder.typicode.com"   ← http vs https

Step 3: If testing localhost API — is the server running?
        Start the API server first, then run tests

Step 4: Check for VPN requirement
        Some company APIs only work on VPN → connect VPN and retry
```

---

### FAILURE 10: `ClassCastException` on extract().path()

```
java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String
```

**What happened:** Extracting an integer field into a String variable (or vice versa).

**Debug steps:**
```java
// The response has: { "id": 1, "title": "..." }

// WRONG — id is Integer in JSON, can't auto-cast to String
String id = given().when().get("/posts/1").then().extract().path("id");

// CORRECT — match Java type to JSON type
int    id    = given().when().get("/posts/1").then().extract().path("id");
String title = given().when().get("/posts/1").then().extract().path("title");

// For nullable fields — use Object or Integer (capital I):
Integer maybeNull = given().when().get("/posts/1").then()
    .extract().<Integer>path("id");

// JSON type → Java type:
// JSON integer → int or Integer
// JSON string  → String
// JSON boolean → boolean or Boolean
// JSON float   → float or Float
// JSON array   → List<Object>
// JSON object  → Map<String, Object>
```

---

### FAILURE 11: `SoftAssert — test passes but failures were swallowed`

```
// Test says PASS but you know some assertions failed
```

**What happened:** `softAssert.assertAll()` is missing at the end.

**Debug steps:**
```java
// BROKEN — missing assertAll() → all failures silently ignored
@Test
public void validateFields() {
    SoftAssert soft = new SoftAssert();
    soft.assertEquals(response.path("id"), 1);
    soft.assertEquals(response.path("title"), "Wrong Title");   // this fails
    // ← NO assertAll() here → failure never reported → test shows PASS
}

// FIXED — always end with assertAll()
@Test
public void validateFields() {
    SoftAssert soft = new SoftAssert();
    soft.assertEquals((int) response.path("id"), 1, "id check");
    soft.assertEquals(response.path("title"), "Wrong Title", "title check");
    soft.assertAll();   // ← THIS IS MANDATORY. Every single time.
}
```

---

### FAILURE 12: `@BeforeClass not running` — requestSpec is null

```
java.lang.NullPointerException: Cannot invoke method spec() — requestSpec is null
```

**What happened:** Test class doesn't extend BaseTest, so `setUp()` never ran.

**Debug steps:**
```java
// WRONG — missing extends BaseTest
public class GetTests {   // ← no extends!
    @Test
    public void getAllPosts() {
        given().spec(requestSpec)...   // requestSpec is null → NullPointerException
    }
}

// CORRECT
public class GetTests extends BaseTest {   // ← must extend BaseTest
    @Test
    public void getAllPosts() {
        given().spec(requestSpec)...   // requestSpec is set by BaseTest.setUp()
    }
}

// Also check: @BeforeClass in BaseTest must be public
@BeforeClass
public void setUp() {   // ← must be PUBLIC, not private or protected
    requestSpec = ...;
}
```

---

### FAILURE 13: Response time assertion fails in CI

```
Expected: response time less than <3000L>
  Actual: response time was <5241L>
```

**What happened:** CI/CD machines are slower than your laptop. 3 seconds is too tight.

**Debug steps:**
```java
// Fix in BaseTest.setUp() — increase timeout for CI
int timeout = System.getenv("CI") != null ? 10000 : 3000;

responseSpec = new ResponseSpecBuilder()
    .expectResponseTime(lessThan((long) timeout))
    .build();

// Or remove response time from responseSpec — only add where SLA really matters
// In specific tests:
.then()
    .statusCode(200)
    .time(lessThan(5000L));   // 5 seconds — more lenient for CI
```

---

### FAILURE 14: `IllegalStateException` — RequestSpecification already built

```
java.lang.IllegalStateException: Request specification has already been built
```

**What happened:** Trying to modify `requestSpec` after calling `.build()`.

**Debug steps:**
```java
// WRONG — can't modify after build()
requestSpec = new RequestSpecBuilder()
    .setContentType(ContentType.JSON)
    .build();
requestSpec.header("X-Extra", "value");   // ❌ can't add after build

// CORRECT — add everything before build()
requestSpec = new RequestSpecBuilder()
    .setContentType(ContentType.JSON)
    .addHeader("X-Extra", "value")         // ✅ add before build
    .build();

// OR — create a new spec in the specific test
RequestSpecification testSpec = new RequestSpecBuilder()
    .addRequestSpecification(requestSpec)   // inherit base
    .addHeader("X-Extra", "value")          // add specific header
    .build();

given().spec(testSpec).when().get(...)
```

---

### FAILURE 15: `415 Unsupported Media Type`

```
Expected status code <201> but was <415>
```

**What happened:** Not setting `Content-Type: application/json` when sending a body.

**Debug steps:**
```java
// WRONG — no content type set → server rejects body
given()
    .body(requestBody)   // no contentType → 415 error
.when()
    .post("/posts");

// CORRECT — always set content type for POST/PUT/PATCH
given()
    .contentType(ContentType.JSON)   // ← this is REQUIRED when sending body
    .body(requestBody)
.when()
    .post("/posts");

// BETTER — use requestSpec which already has contentType set
given()
    .spec(requestSpec)   // requestSpec has contentType(JSON) already
    .body(requestBody)
.when()
    .post("/posts");
```

---

### HOW TO USE IntelliJ DEBUGGER FOR RESTASSURED

The IntelliJ debugger lets you pause test execution and inspect values live:

```
Step 1: Click in the left margin next to a line of code → red dot appears (breakpoint)
        Click next to: int newId = given()...

Step 2: Right-click the test method → Debug (not Run)

Step 3: Test pauses at your breakpoint. In the Debug panel at bottom:
        - "Variables" tab: see all variables and their values
        - "Watches" tab: type any expression to evaluate it

Step 4: Press F8 to go to next line
        Press F7 to step INTO a method
        Press F9 to continue to next breakpoint

Step 5: Hover your mouse over any variable → tooltip shows its value
        Hover over 'newId' → see what value was extracted from response
```

**Most useful breakpoints to set:**
```
1. After extract().path("id") — see what ID was actually extracted
2. After SoftAssert soft = new SoftAssert() — inspect response fields
3. Before .statusCode() assertion — see what status actually came back
```

---

### QUICK REFERENCE — Error to Cause to Fix

| Error message | Cause | Fix |
|---------------|-------|-----|
| `Cannot resolve symbol` | Missing import/dependency | Alt+Enter → import, refresh Maven |
| `Expected 200, got 404` | Wrong URL or endpoint | Check URL in browser, fix ApiConstants |
| `Expected 200, got 401` | Missing auth | Add Authorization header to requestSpec |
| `JSON path doesn't match` | Wrong field name or value | Print response, fix field name |
| `NullPointerException` | Missing await or wrong field | Print response, check field name |
| `SchemaValidation failed` | Schema file wrong or not found | Check file location, update schema |
| `Tests run: 0` | testng.xml class name wrong | Match exact package.ClassName |
| `Connection refused` | API down or wrong URL | Open URL in browser |
| `ClassCastException` | Wrong Java type for field | Use int for JSON integer, String for string |
| `SoftAssert fails silently` | Missing assertAll() | Always add soft.assertAll() at end |
| `requestSpec is null` | Not extending BaseTest | Add extends BaseTest to class |
| `415 Unsupported Media Type` | Missing Content-Type | Add .contentType(ContentType.JSON) |
| `Response time exceeded` | CI machine too slow | Increase timeout for CI environment |

---

*Setup Guide — IntelliJ IDEA + RestAssured + TestNG | Java API Testing | Full Troubleshooting*
