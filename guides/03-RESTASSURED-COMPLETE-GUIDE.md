# RestAssured — Complete Interview Q&A Guide
## Every Concept as a Real Interview Question

---

## SECTION 1: FOUNDATIONS

**Q1: What is RestAssured and why use it instead of HttpClient or OkHttp?**

**A:** RestAssured is a Java DSL (domain-specific language) for testing REST APIs. It wraps Java's HTTP machinery in a fluent, readable syntax designed specifically for test assertions.

Comparison:

| | Raw HttpURLConnection | OkHttp | RestAssured |
|---|---|---|---|
| Lines to make GET + assert | ~40 | ~20 | ~5 |
| Built-in assertions | None | None | Yes (Hamcrest) |
| JSON path extraction | Manual | Manual | Built-in GPath |
| Schema validation | Manual | Manual | Built-in |
| BDD syntax | No | No | Yes (given/when/then) |
| DataProvider integration | Manual | Manual | Native TestNG/JUnit |

```java
// Raw HttpURLConnection — 40+ lines for a simple GET + status check
URL url = new URL("https://api.example.com/users/1");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setRequestProperty("Accept", "application/json");
int statusCode = conn.getResponseCode();
if (statusCode != 200) throw new AssertionError("Expected 200 but got " + statusCode);
BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
StringBuilder sb = new StringBuilder();
String line;
while ((line = reader.readLine()) != null) sb.append(line);
reader.close();
// Now manually parse JSON...
ObjectMapper mapper = new ObjectMapper();
JsonNode node = mapper.readTree(sb.toString());
if (node.get("id").asInt() != 1) throw new AssertionError("Wrong id");

// RestAssured — same test in 6 lines
given()
    .when().get("https://api.example.com/users/1")
    .then()
    .statusCode(200)
    .body("id", equalTo(1));
```

RestAssured also integrates natively with TestNG and JUnit — your tests look like normal test methods with no boilerplate.

---

**Q2: Explain the given / when / then structure of RestAssured.**

**A:** RestAssured uses a BDD (Behaviour Driven Development) pattern:

```
given()  — set up the request (headers, body, params, auth, spec)
  when() — specify the HTTP method and endpoint
  .then()— assert the response (status, body, headers, time)
```

```java
given()                                         // REQUEST SETUP
    .contentType(ContentType.JSON)              // set Content-Type header
    .header("Authorization", "Bearer mytoken") // add auth header
    .queryParam("userId", 1)                    // add ?userId=1
    .body("{ \"title\": \"New Post\" }")        // set request body

.when()                                         // SEND REQUEST
    .post("/posts")                             // HTTP POST to /posts

.then()                                         // ASSERT RESPONSE
    .statusCode(201)                            // verify status
    .body("id", notNullValue())                 // verify body field
    .body("title", equalTo("New Post"))         // verify field value
    .contentType(ContentType.JSON)              // verify response header
    .time(lessThan(3000L));                     // verify response time
```

Each part returns a specification object that you chain. `given()` returns `RequestSpecification`. `when()` returns `Response`. `then()` returns `ValidatableResponse`.

You can skip `given()` if there is nothing to set up:

```java
// Shorthand — no given() needed for simple GET
when().get("/posts").then().statusCode(200);

// Or even simpler
get("/posts").then().statusCode(200);
```

---

**Q3: How do you set a base URI globally so you don't repeat it in every test?**

**A:** Set `RestAssured.baseURI` once (in `@BeforeClass` or `@BeforeSuite`) and all subsequent requests use it automatically.

```java
import io.restassured.RestAssured;

public class BaseTest {

    @BeforeClass
    public void setUp() {
        // Set once — all tests in suite append their paths to this
        RestAssured.baseURI  = "https://jsonplaceholder.typicode.com";
        RestAssured.basePath = "/v1";    // optional — prepends to every path
        RestAssured.port     = 443;      // optional — defaults to 80/443

        // Now tests only need relative paths:
        // given().when().get("/posts/1")  →  hits https://jsonplaceholder.typicode.com/v1/posts/1
    }

    @AfterClass
    public void resetDefaults() {
        // Optional — reset after test class so other suites aren't affected
        RestAssured.reset();
    }
}
```

```java
// Read from system property so environment can be changed at runtime
RestAssured.baseURI = System.getProperty("baseUrl", "https://staging.example.com");
// mvn test -DbaseUrl=https://production.example.com
```

---

**Q4: What is RequestSpecification and why should you use it?**

**A:** `RequestSpecification` is a reusable request configuration object. Instead of repeating headers, content type, and base settings on every `given()`, you build the spec once and apply it everywhere.

Without `RequestSpecification` — repetitive:
```java
// Every single test repeats this
given()
    .contentType(ContentType.JSON)
    .accept(ContentType.JSON)
    .header("Authorization", "Bearer mytoken123")
    .header("x-api-key", "abc123")
.when().get("/posts");

given()
    .contentType(ContentType.JSON)
    .accept(ContentType.JSON)
    .header("Authorization", "Bearer mytoken123")
    .header("x-api-key", "abc123")
.when().post("/posts").body("...");
```

With `RequestSpecification` — DRY:
```java
// Build once in BaseTest @BeforeClass
RequestSpecification requestSpec = new RequestSpecBuilder()
    .setContentType(ContentType.JSON)
    .setAccept(ContentType.JSON)
    .addHeader("Authorization", "Bearer " + getAuthToken())
    .addHeader("x-api-key", ApiConstants.API_KEY)
    .setBaseUri("https://api.example.com")
    .log(LogDetail.ALL)          // log all requests
    .build();

// Use in every test — one line instead of five
given().spec(requestSpec).when().get("/posts").then().statusCode(200);
given().spec(requestSpec).body(payload).when().post("/posts").then().statusCode(201);
```

You can also layer specs — start with a base spec and add test-specific items:

```java
// Base spec in BaseTest
protected RequestSpecification requestSpec;

// Override or extend for a specific test
RequestSpecification adminSpec = new RequestSpecBuilder()
    .addRequestSpecification(requestSpec)   // inherit base spec
    .addHeader("x-admin-token", ADMIN_TOKEN) // add admin-only header
    .build();
```

---

**Q5: What is ResponseSpecification and when should you use it?**

**A:** `ResponseSpecification` captures common assertions that every response should meet. Apply it with `.spec(responseSpec)` in the `.then()` chain.

```java
// Build once in BaseTest @BeforeClass
ResponseSpecification responseSpec = new ResponseSpecBuilder()
    .expectStatusCode(200)                         // every response must be 200
    .expectContentType(ContentType.JSON)           // every response must be JSON
    .expectResponseTime(lessThan(3000L))           // every response under 3 seconds
    .build();

// Apply in tests
given().spec(requestSpec)
    .when().get("/posts/1")
    .then().spec(responseSpec)          // checks 200 + JSON + time < 3s
    .body("id", equalTo(1));            // then add test-specific assertions

given().spec(requestSpec)
    .when().get("/users/1")
    .then().spec(responseSpec)          // same base checks reused
    .body("email", containsString("@"));
```

When responseSpec does not fit (e.g. for a POST that returns 201):

```java
// Override the status code expectation for POST
given().spec(requestSpec)
    .body(payload)
    .when().post("/posts")
    .then()
    .spec(responseSpec)
    .statusCode(201);   // this overrides the 200 in responseSpec
```

---

## SECTION 2: HTTP METHODS

**Q6: How do you make a GET request with a path parameter?**

**A:** Use `.pathParam()` in `given()` and `{paramName}` placeholder in the URL.

```java
// Single path parameter
given()
    .pathParam("id", 1)
.when()
    .get("/posts/{id}")     // {id} is replaced with 1
.then()
    .statusCode(200)
    .body("id", equalTo(1))
    .body("title", not(emptyString()));

// Multiple path parameters
given()
    .pathParam("userId", 1)
    .pathParam("postId", 5)
.when()
    .get("/users/{userId}/posts/{postId}")
.then()
    .statusCode(200);

// Inline — without named pathParam
given()
.when()
    .get("/posts/{id}", 1)     // pass value directly
.then()
    .statusCode(200);
```

Real-world context: REST APIs use path parameters for resource identifiers (`/users/123`, `/orders/456/items/789`). Path params are part of the URL structure, not appended as query strings.

---

**Q7: How do you make a GET request with query parameters?**

**A:** Use `.queryParam()` in `given()`. Query params are appended as `?key=value` after the URL.

```java
// Single query parameter — GET /posts?userId=1
given()
    .queryParam("userId", 1)
.when()
    .get("/posts")
.then()
    .statusCode(200)
    .body("size()", equalTo(10))
    .body("userId", everyItem(equalTo(1)));  // every result has userId=1

// Multiple query parameters — GET /posts?userId=1&_limit=5&_sort=id
given()
    .queryParam("userId", 1)
    .queryParam("_limit", 5)
    .queryParam("_sort", "id")
.when()
    .get("/posts")
.then()
    .statusCode(200)
    .body("size()", lessThanOrEqualTo(5));

// Using Map for query params
Map<String, Object> params = new HashMap<>();
params.put("userId", 1);
params.put("_limit", 5);

given()
    .queryParams(params)
.when()
    .get("/posts")
.then()
    .statusCode(200);

// Encoding — RestAssured URL-encodes values automatically
given()
    .queryParam("search", "hello world")  // becomes ?search=hello+world
.when()
    .get("/posts");
```

Difference from path params:
- Path param: `/posts/1` — part of the URL path
- Query param: `/posts?userId=1` — after the `?`, used for filtering/sorting/pagination

---

**Q8: How do you make a POST request with a String body?**

**A:**

```java
// POST with raw JSON string
String body = "{ \"title\": \"New Post\", \"body\": \"Content here\", \"userId\": 1 }";

given()
    .contentType(ContentType.JSON)
    .body(body)
.when()
    .post("/posts")
.then()
    .statusCode(201)
    .body("id", notNullValue())
    .body("title", equalTo("New Post"))
    .body("userId", equalTo(1));

// POST and capture the created resource ID
int newId = given()
    .contentType(ContentType.JSON)
    .body(body)
.when()
    .post("/posts")
.then()
    .statusCode(201)
    .extract().path("id");

System.out.println("Created post with ID: " + newId);

// Use the ID in subsequent request
given()
    .pathParam("id", newId)
.when()
    .get("/posts/{id}")
.then()
    .statusCode(anyOf(equalTo(200), equalTo(404)));
```

---

**Q9: How do you make a POST request with a Map or HashMap body?**

**A:** RestAssured serializes a `Map<String, Object>` to JSON automatically when `ContentType.JSON` is set.

```java
// Using HashMap
Map<String, Object> payload = new HashMap<>();
payload.put("title", "Test Post");
payload.put("body", "Body content here");
payload.put("userId", 1);

given()
    .contentType(ContentType.JSON)
    .body(payload)               // RestAssured converts to JSON
.when()
    .post("/posts")
.then()
    .statusCode(201)
    .body("title", equalTo("Test Post"));

// Using Map.of (Java 9+) — immutable, cleaner
Map<String, Object> payload = Map.of(
    "title", "Test Post",
    "body", "Body content",
    "userId", 1
);

given()
    .spec(requestSpec)
    .body(payload)
.when()
    .post("/posts")
.then()
    .statusCode(201);

// Nested object in map
Map<String, Object> address = Map.of("street", "123 Main St", "city", "London");
Map<String, Object> user = new HashMap<>();
user.put("name", "John Doe");
user.put("email", "john@example.com");
user.put("address", address);         // nested object

given()
    .spec(requestSpec)
    .body(user)
.when()
    .post("/users")
.then()
    .statusCode(201)
    .body("address.city", equalTo("London"));
```

Why use Map over String? Maps are type-safe and avoid JSON syntax errors. No risk of mismatched braces or missing quotes.

---

**Q10: How do you POST with a Java POJO using Jackson serialization?**

**A:** When `jackson-databind` is on the classpath and `ContentType.JSON` is set, RestAssured automatically serializes a POJO to JSON.

```xml
<!-- pom.xml — Jackson is usually pulled in transitively by RestAssured -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```

```java
// POJO
public class Post {
    private String title;
    private String body;
    private int userId;

    // Constructors
    public Post() {}
    public Post(String title, String body, int userId) {
        this.title = title;
        this.body = body;
        this.userId = userId;
    }

    // Getters and setters — Jackson requires these for serialization
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}

// Use POJO in test
@Test
public void createPostWithPojo() {
    Post post = new Post("Interview Post", "Detailed body", 1);

    given()
        .spec(requestSpec)           // has ContentType.JSON set
        .body(post)                  // Jackson serializes to {"title":"Interview Post","body":"Detailed body","userId":1}
    .when()
        .post("/posts")
    .then()
        .statusCode(201)
        .body("title", equalTo("Interview Post"));
}

// Deserialize response into POJO
@Test
public void getPostAsPojo() {
    Post post = given()
        .spec(requestSpec)
        .pathParam("id", 1)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(200)
        .extract().as(Post.class);   // Jackson deserializes response into Post

    Assert.assertEquals(post.getTitle(), "sunt aut facere repellat...");
    Assert.assertEquals(post.getUserId(), 1);
}
```

---

**Q11: What is the difference between PUT and PATCH and how do you write each?**

**A:**

| | PUT | PATCH |
|---|---|---|
| Intent | Full replacement of the resource | Partial update — only provided fields change |
| Body | Must include ALL fields | Only the fields you want to change |
| Idempotent | Yes | Yes |
| Status | 200 | 200 |

```java
// PUT — send COMPLETE resource (all fields)
// Missing fields may be set to null by the server
String fullBody = """
    {
        "id": 1,
        "title": "Completely Replaced Title",
        "body": "Completely replaced body content",
        "userId": 1
    }
    """;

given()
    .spec(requestSpec)
    .pathParam("id", 1)
    .body(fullBody)
.when()
    .put("/posts/{id}")
.then()
    .statusCode(200)
    .body("title", equalTo("Completely Replaced Title"))
    .body("id", equalTo(1));

// PATCH — send ONLY changed fields
// Other fields remain as they were
given()
    .spec(requestSpec)
    .pathParam("id", 1)
    .body("{ \"title\": \"Only Title Changed\" }")   // body and userId unchanged
.when()
    .patch("/posts/{id}")
.then()
    .statusCode(200)
    .body("id", equalTo(1))
    .body("title", equalTo("Only Title Changed"))
    .body("userId", notNullValue());    // userId still present — not deleted by PATCH
```

Real-world example: Updating a user profile. `PUT /users/1` requires sending name, email, phone, address all at once. `PATCH /users/1` with `{"email": "new@email.com"}` updates only the email.

---

**Q12: How do you make a DELETE request and verify the response?**

**A:**

```java
// DELETE and check status code
given()
    .pathParam("id", 1)
.when()
    .delete("/posts/{id}")
.then()
    .statusCode(anyOf(equalTo(200), equalTo(204)));
// 200 = deleted with body returned
// 204 = deleted, no body (No Content)

// DELETE expecting 204 — body should be empty
given()
    .pathParam("id", 1)
.when()
    .delete("/posts/{id}")
.then()
    .statusCode(204)
    .body(emptyString());    // no body for 204

// DELETE expecting 200 with empty JSON object
given()
    .pathParam("id", 1)
.when()
    .delete("/posts/{id}")
.then()
    .statusCode(200)
    .body(equalTo("{}"));

// Verify resource is actually gone after DELETE
given()
    .pathParam("id", 1)
.when()
    .delete("/posts/{id}")
.then()
    .statusCode(anyOf(equalTo(200), equalTo(204)));

// Follow-up GET should return 404
given()
    .pathParam("id", 1)
.when()
    .get("/posts/{id}")
.then()
    .statusCode(404);    // confirms the resource was deleted
```

---

## SECTION 3: ASSERTIONS AND HAMCREST MATCHERS

**Q13: What are all the Hamcrest matchers used in RestAssured and what does each do?**

**A:**

```java
import static org.hamcrest.Matchers.*;

// ── EQUALITY ─────────────────────────────────────────────────────────────────
.body("id",    equalTo(1))             // field equals exact value
.body("id",    not(equalTo(99)))       // field does not equal value
.body("id",    notNullValue())         // field is not null
.body("field", nullValue())            // field is null / missing

// ── STRING MATCHERS ───────────────────────────────────────────────────────────
.body("title", equalTo("Hello World"))
.body("title", not(emptyString()))              // not empty string
.body("title", containsString("Hello"))         // contains substring
.body("title", startsWith("He"))                // starts with
.body("title", endsWith("World"))               // ends with
.body("title", equalToIgnoringCase("hello world")) // case-insensitive
.body("email", matchesPattern(".*@.*\\..*"))    // regex match

// ── NUMERIC COMPARISONS ───────────────────────────────────────────────────────
.body("count", greaterThan(0))
.body("count", greaterThanOrEqualTo(1))
.body("count", lessThan(100))
.body("count", lessThanOrEqualTo(99))
.body("price", closeTo(9.99, 0.01))    // within tolerance (for doubles)

// ── BOOLEAN ───────────────────────────────────────────────────────────────────
.body("active", equalTo(true))
.body("deleted", equalTo(false))
.body("active", is(true))              // is() is just readability alias for equalTo()

// ── ARRAY / COLLECTION MATCHERS ───────────────────────────────────────────────
.body("size()",     equalTo(100))              // array has exactly 100 items
.body("size()",     greaterThan(0))            // array is not empty
.body("[0].id",     equalTo(1))               // first item's id = 1
.body("[0].title",  notNullValue())           // first item has a title
.body("userId",     everyItem(equalTo(1)))    // EVERY item in array has userId=1
.body("id",         hasItem(5))              // array contains 5 somewhere
.body("title",      hasItems("A", "B"))      // array contains BOTH "A" and "B"
.body("name",       hasSize(10))             // list has exactly 10 elements

// ── NESTED JSON ───────────────────────────────────────────────────────────────
.body("address.city",       equalTo("London"))
.body("address.zip",        notNullValue())
.body("roles[0]",           equalTo("admin"))
.body("data.users[0].name", notNullValue())

// ── STATUS CODE MATCHERS ──────────────────────────────────────────────────────
.statusCode(200)
.statusCode(not(500))
.statusCode(anyOf(equalTo(200), equalTo(201)))   // either 200 or 201
.statusCode(greaterThanOrEqualTo(200))

// ── HEADER MATCHERS ───────────────────────────────────────────────────────────
.header("Content-Type", containsString("application/json"))
.header("Cache-Control", notNullValue())
.header("x-rate-limit",  notNullValue())
.contentType(ContentType.JSON)         // shorthand for Content-Type header

// ── RESPONSE TIME ─────────────────────────────────────────────────────────────
.time(lessThan(3000L))    // milliseconds
.time(lessThan(3L), TimeUnit.SECONDS)
```

---

**Q14: How do you assert the size of an array in a response?**

**A:**

```java
// Response: [{"id":1,...}, {"id":2,...}, ... {"id":100,...}]  (100 items)

// Assert exact size
given()
.when()
    .get("/posts")
.then()
    .statusCode(200)
    .body("size()", equalTo(100));

// Assert at least one item
given()
.when()
    .get("/posts")
.then()
    .body("size()", greaterThan(0));

// Assert size within range
given()
    .queryParam("_limit", 5)
.when()
    .get("/posts")
.then()
    .body("size()", lessThanOrEqualTo(5));

// Assert that a nested array has the right size
// Response: {"user": {"roles": ["admin", "editor"]}}
given()
    .pathParam("id", 1)
.when()
    .get("/users/{id}")
.then()
    .body("roles.size()", equalTo(2));

// Extract size and assert in Java
int size = given()
    .when().get("/posts")
    .then().extract().path("size()");

Assert.assertTrue(size > 0, "Posts list should not be empty");
```

---

**Q15: How do you assert that every item in an array meets a condition?**

**A:** Use `everyItem()` matcher.

```java
// Response: [{"userId":1, "id":1,...}, {"userId":1, "id":2,...}, ...]
// Assert all items belong to userId=1

given()
    .queryParam("userId", 1)
.when()
    .get("/posts")
.then()
    .statusCode(200)
    .body("userId", everyItem(equalTo(1)))           // all userId fields = 1
    .body("id",     everyItem(notNullValue()))        // all ids not null
    .body("title",  everyItem(not(emptyString())));   // no empty titles

// Assert every item is a positive number
given()
.when()
    .get("/posts")
.then()
    .body("id", everyItem(greaterThan(0)));

// Assert every status in a list of orders is in valid values
given()
    .queryParam("customerId", 123)
.when()
    .get("/orders")
.then()
    .body("status", everyItem(
        anyOf(equalTo("pending"), equalTo("shipped"), equalTo("delivered"))
    ));
```

---

**Q16: How do you assert response headers?**

**A:**

```java
// Assert specific header value
given()
.when()
    .get("/posts/1")
.then()
    .statusCode(200)
    .header("Content-Type",  containsString("application/json"))
    .header("Cache-Control", notNullValue())
    .header("X-Powered-By",  not(emptyString()));

// Assert content type using ContentType enum (more readable)
.contentType(ContentType.JSON)
.contentType("application/json; charset=utf-8")  // exact string match

// Extract header value and assert in Java
Response response = given()
    .when().get("/posts/1")
    .then().statusCode(200).extract().response();

String contentType = response.header("Content-Type");
Assert.assertTrue(contentType.contains("application/json"),
    "Expected JSON but got: " + contentType);

// Assert header does NOT exist
given()
.when()
    .get("/posts/1")
.then()
    .header("X-Secret-Token", nullValue());   // header should not be present
```

---

**Q17: How do you assert response time in RestAssured?**

**A:**

```java
import java.util.concurrent.TimeUnit;

// Assert response time under 3000 milliseconds
given()
.when()
    .get("/posts")
.then()
    .time(lessThan(3000L));   // L suffix = long literal

// Assert under 3 seconds (more readable)
given()
.when()
    .get("/posts")
.then()
    .time(lessThan(3L), TimeUnit.SECONDS);

// Using ResponseSpecification for all tests
ResponseSpecification responseSpec = new ResponseSpecBuilder()
    .expectResponseTime(lessThan(3000L))
    .build();

// Extract time and log it
Response response = given()
    .when().get("/posts")
    .then().extract().response();

long responseTime = response.time();           // in milliseconds
System.out.println("Response time: " + responseTime + "ms");
Assert.assertTrue(responseTime < 3000, "Response took too long: " + responseTime + "ms");
```

---

## SECTION 4: EXTRACTION

**Q18: How do you extract a single value from a response?**

**A:**

```java
// Extract single field directly
int id = given()
    .when().get("/posts/1")
    .then().statusCode(200)
    .extract().path("id");

String title = given()
    .when().get("/posts/1")
    .then().extract().path("title");

// Extract nested field
String city = given()
    .when().get("/users/1")
    .then().extract().path("address.city");

// Extract array element
String firstTitle = given()
    .when().get("/posts")
    .then().extract().path("[0].title");

// Extract from array by index
int thirdId = given()
    .when().get("/posts")
    .then().extract().path("[2].id");

// Type-safe extraction
Integer count = given()
    .when().get("/posts")
    .then().extract().path("size()");

// Use extracted value in next request
int userId = given()
    .when().get("/posts/1")
    .then().extract().path("userId");

given()
    .pathParam("id", userId)
    .when().get("/users/{id}")
    .then().statusCode(200);
```

---

**Q19: What is the difference between extract().path() and extract().response()?**

**A:**

```java
// extract().path() — get ONE specific field
// Use when you need a single value
int id = given()
    .when().get("/posts/1")
    .then().statusCode(200)
    .extract().path("id");
// Concise — directly gives you the value you need

// extract().response() — get the FULL response object
// Use when you need multiple values from the same response
Response response = given()
    .when().get("/posts/1")
    .then().statusCode(200)
    .extract().response();

// Now access multiple things from the same response
int    id          = response.path("id");
String title       = response.path("title");
String body        = response.path("body");
int    userId      = response.path("userId");
int    statusCode  = response.statusCode();
String contentType = response.header("Content-Type");
String rawBody     = response.body().asString();
long   timeTaken   = response.time();

// Rule: if you only need ONE field → extract().path()
// Rule: if you need 2+ fields or status/headers/body → extract().response()
```

---

**Q20: How do you extract a list from a response?**

**A:**

```java
// Response is an array — extract all IDs
List<Integer> allIds = given()
    .when().get("/posts")
    .then().statusCode(200)
    .extract().path("id");   // GPath returns a List<Integer>

System.out.println("Total posts: " + allIds.size());
Assert.assertTrue(allIds.contains(1));

// Extract all titles
List<String> allTitles = given()
    .when().get("/posts")
    .then().extract().path("title");

// Extract nested field from all items
List<Integer> allUserIds = given()
    .when().get("/posts")
    .then().extract().path("userId");

// Extract items from nested array
// Response: {"users": [{"id":1,"name":"Alice"}, {"id":2,"name":"Bob"}]}
List<String> names = given()
    .when().get("/team")
    .then().extract().path("users.name");   // extracts name from each user object

// Filter with GPath
// Extract IDs only where userId = 1
List<Integer> filteredIds = given()
    .when().get("/posts")
    .then().extract()
    .path("findAll { it.userId == 1 }.id");   // GPath Groovy expression
```

---

## SECTION 5: AUTHENTICATION

**Q21: How do you add an API key as a header in RestAssured?**

**A:**

```java
// API key in custom header
given()
    .header("x-api-key", "abc123secretkey")
.when()
    .get("/protected-resource")
.then()
    .statusCode(200);

// API key as query parameter (some APIs use this)
given()
    .queryParam("api_key", "abc123secretkey")
.when()
    .get("/protected-resource")
.then()
    .statusCode(200);

// Best practice — add to requestSpec so all tests use it
requestSpec = new RequestSpecBuilder()
    .setContentType(ContentType.JSON)
    .addHeader("x-api-key", System.getProperty("apiKey", ApiConstants.API_KEY))
    .build();
```

---

**Q22: How do you use Bearer token authentication in RestAssured?**

**A:**

```java
// Hardcoded token (for quick tests)
given()
    .header("Authorization", "Bearer eyJhbGciOiJSUzI1NiJ9...")
.when()
    .get("/protected")
.then()
    .statusCode(200);

// Dynamic token — login first, capture token, use it
String token = given()
    .contentType(ContentType.JSON)
    .body(Map.of("username", "admin", "password", "Admin@123"))
.when()
    .post("/auth/login")
.then()
    .statusCode(200)
    .body("token", notNullValue())
    .extract().path("token");

// Now use the token
given()
    .header("Authorization", "Bearer " + token)
.when()
    .get("/protected-resource")
.then()
    .statusCode(200);

// Best practice — store token in requestSpec in @BeforeClass
@BeforeClass
public void setUp() {
    RestAssured.baseURI = ApiConstants.BASE_URL;

    String token = given()
        .contentType(ContentType.JSON)
        .body(Map.of("username", "admin", "password", "Admin@123"))
        .when().post("/auth/login")
        .then().statusCode(200)
        .extract().path("token");

    requestSpec = new RequestSpecBuilder()
        .setContentType(ContentType.JSON)
        .addHeader("Authorization", "Bearer " + token)
        .build();
    // Now every test using requestSpec is automatically authenticated
}
```

---

**Q23: How do you implement Basic Authentication in RestAssured?**

**A:**

```java
// Basic auth — RestAssured handles Base64 encoding automatically
given()
    .auth().basic("username", "password")
.when()
    .get("/admin/users")
.then()
    .statusCode(200);

// Preemptive basic auth — sends credentials without waiting for server challenge
// Use this when the server expects credentials upfront (most REST APIs)
given()
    .auth().preemptive().basic("username", "password")
.when()
    .get("/admin/users")
.then()
    .statusCode(200);

// Manual — add Authorization header with Base64 encoded credentials
String credentials = Base64.getEncoder()
    .encodeToString("username:password".getBytes());

given()
    .header("Authorization", "Basic " + credentials)
.when()
    .get("/admin/users")
.then()
    .statusCode(200);
```

---

**Q24: How do you implement an OAuth2 login flow in RestAssured tests?**

**A:**

```java
// Full OAuth2 flow: login → get token → use token → verify protected resource
public class OAuth2FlowTest extends BaseTest {

    private static String authToken;

    @BeforeClass
    public void obtainToken() {
        // Step 1: POST credentials to auth endpoint
        authToken = given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "username", "testuser",
                "password", "TestPass@123",
                "grant_type", "password"
            ))
        .when()
            .post("/oauth/token")
        .then()
            .statusCode(200)
            .body("access_token", notNullValue())
            .body("token_type", equalTo("Bearer"))
            .extract().path("access_token");

        System.out.println("Token obtained: " + authToken.substring(0, 20) + "...");
    }

    @Test
    public void accessProtectedEndpoint() {
        // Step 2: Use token to call protected endpoint
        given()
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/api/me")
        .then()
            .statusCode(200)
            .body("username", equalTo("testuser"));
    }

    @Test
    public void verifyTokenExpiry() {
        // Step 3: Verify expired/invalid token returns 401
        given()
            .header("Authorization", "Bearer invalidtoken123")
        .when()
            .get("/api/me")
        .then()
            .statusCode(401)
            .body("error", equalTo("Unauthorized"));
    }
}
```

---

## SECTION 6: LOGGING

**Q25: What does enableLoggingOfRequestAndResponseIfValidationFails() do?**

**A:** This is the "smart logging" mode. RestAssured captures all request and response details in memory but only prints them to the console if an assertion fails. When tests pass, no noise. When tests fail, full debug info is right there.

```java
// Set globally in BaseTest — recommended for all projects
RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

// When a test passes — nothing printed
// When a test fails — console shows:
// Request method: GET
// Request URI: https://api.example.com/posts/1
// Request headers: Accept=application/json, Content-Type=application/json
//
// HTTP/1.1 404 Not Found
// Content-Type: application/json
// {"error": "Not Found"}
// 
// java.lang.AssertionError: expected status code 200 but was 404
```

---

**Q26: What are the different log() options in RestAssured?**

**A:**

```java
// Log EVERYTHING about the request
given()
    .log().all()
    .when().get("/posts");

// Log only specific parts of request
given()
    .log().headers()    // only headers
    .log().body()       // only request body
    .log().params()     // only parameters
    .log().method()     // only HTTP method
    .log().uri()        // only URL

// Log response
given()
    .when().get("/posts")
    .then()
    .log().all()        // log full response
    .log().body()       // only response body
    .log().headers()    // only response headers
    .log().status()     // only status code
    .log().ifError()    // only if status code >= 400
    .log().ifValidationFails()  // same as enableLoggingOfRequestAndResponseIfValidationFails()
    .statusCode(200);

// log().ifError() is useful in CI — only prints response when something goes wrong
given()
    .spec(requestSpec)
.when()
    .get("/users/1")
.then()
    .log().ifError()    // silent on pass, detailed on 4xx/5xx
    .statusCode(200);
```

---

## SECTION 7: JSON SCHEMA VALIDATION

**Q27: What is JSON Schema validation and how do you set it up in RestAssured?**

**A:** JSON Schema validation checks that the API response structure matches a predefined contract. It validates field names, types, required fields, and format — independent of specific values.

```xml
<!-- pom.xml — add schema validator -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>5.3.1</version>
    <scope>test</scope>
</dependency>
```

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

```json
// src/test/resources/schemas/users-list-schema.json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id":    { "type": "integer" },
      "name":  { "type": "string"  },
      "email": { "type": "string", "format": "email" }
    },
    "required": ["id", "name", "email"]
  }
}
```

```java
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Test
public void validatePostSchema() {
    given()
        .pathParam("id", 1)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
    // Fails if: field is wrong type, required field is missing,
    // extra field violates additionalProperties: false
}

@Test
public void validateUsersListSchema() {
    given()
    .when()
        .get("/users")
    .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("schemas/users-list-schema.json"));
}
```

Why use schema validation: Value assertions like `.body("id", equalTo(1))` only check one test case. Schema validation protects against breaking API contract changes — if the API team renames a field or changes a type, schema validation catches it immediately across all test runs.

---

## SECTION 8: ADVANCED PATTERNS

**Q28: How do you chain a full CRUD workflow (POST → GET → PUT → DELETE) in one test?**

**A:**

```java
@Test
public void fullCrudWorkflow() {

    // STEP 1: CREATE
    Map<String, Object> createPayload = Map.of(
        "title", "CRUD Test Post",
        "body", "Created in CRUD test",
        "userId", 1
    );

    int createdId = given()
        .spec(requestSpec)
        .body(createPayload)
    .when()
        .post("/posts")
    .then()
        .statusCode(201)
        .body("title", equalTo("CRUD Test Post"))
        .extract().path("id");

    System.out.println("Created with ID: " + createdId);

    // STEP 2: READ — verify created resource exists
    given()
        .spec(requestSpec)
        .pathParam("id", createdId)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(200), equalTo(404)));
    // JSONPlaceholder doesn't persist — real API would return 200

    // STEP 3: UPDATE — full replacement
    Map<String, Object> updatePayload = Map.of(
        "id", createdId,
        "title", "Updated Title",
        "body", "Updated body content",
        "userId", 1
    );

    given()
        .spec(requestSpec)
        .pathParam("id", createdId)
        .body(updatePayload)
    .when()
        .put("/posts/{id}")
    .then()
        .statusCode(200)
        .body("title", equalTo("Updated Title"));

    // STEP 4: PARTIAL UPDATE
    given()
        .spec(requestSpec)
        .pathParam("id", createdId)
        .body(Map.of("title", "Patched Title"))
    .when()
        .patch("/posts/{id}")
    .then()
        .statusCode(200)
        .body("title", equalTo("Patched Title"));

    // STEP 5: DELETE
    given()
        .spec(requestSpec)
        .pathParam("id", createdId)
    .when()
        .delete("/posts/{id}")
    .then()
        .statusCode(anyOf(equalTo(200), equalTo(204)));

    System.out.println("CRUD workflow completed successfully");
}
```

---

**Q29: How do you use soft assertions with RestAssured?**

**A:**

```java
import org.testng.asserts.SoftAssert;

@Test
public void softAssertMultipleFields() {
    // Extract full response — don't want to make multiple API calls
    Response response = given()
        .spec(requestSpec)
        .pathParam("id", 1)
    .when()
        .get("/posts/{id}")
    .then()
        .statusCode(200)
        .extract().response();

    SoftAssert soft = new SoftAssert();

    // Assert multiple fields — all checked even if one fails
    soft.assertEquals((int) response.path("id"), 1,
        "ID should be 1");
    soft.assertNotNull(response.path("title"),
        "title should not be null");
    soft.assertNotNull(response.path("body"),
        "body should not be null");
    soft.assertEquals((int) response.path("userId"), 1,
        "userId should be 1");
    soft.assertTrue(response.path("title").toString().length() > 0,
        "title should not be empty");
    soft.assertEquals(response.statusCode(), 200,
        "status should be 200");

    soft.assertAll();   // Reports ALL failures — NEVER skip this line
}
```

---

**Q30: How do you use @DataProvider with RestAssured for data-driven API tests?**

**A:**

```java
public class DataDrivenApiTest extends BaseTest {

    // Single-value DataProvider
    @DataProvider(name = "validPostIds")
    public Object[][] validPostIds() {
        return new Object[][] {
            { 1 }, { 5 }, { 10 }, { 50 }, { 100 }
        };
    }

    @Test(dataProvider = "validPostIds")
    public void getPostByIdTest(int postId) {
        given()
            .spec(requestSpec)
            .pathParam("id", postId)
        .when()
            .get("/posts/{id}")
        .then()
            .statusCode(200)
            .body("id", equalTo(postId))
            .body("title", notNullValue());
    }

    // Multi-column DataProvider for POST
    @DataProvider(name = "createPostData")
    public Object[][] createPostData() {
        return new Object[][] {
            // title,          body,            userId, expectedStatus
            { "Valid Post",   "Valid Body",     1,      201 },
            { "Another Post", "Another body",   2,      201 },
        };
    }

    @Test(dataProvider = "createPostData")
    public void createPostTest(String title, String body, int userId, int expectedStatus) {
        given()
            .spec(requestSpec)
            .body(Map.of("title", title, "body", body, "userId", userId))
        .when()
            .post("/posts")
        .then()
            .statusCode(expectedStatus)
            .body("title", equalTo(title));
    }

    // Parallel DataProvider for speed
    @DataProvider(name = "parallelGetIds", parallel = true)
    public Object[][] parallelGetIds() {
        return new Object[][] {
            { 1 }, { 2 }, { 3 }, { 4 }, { 5 },
            { 6 }, { 7 }, { 8 }, { 9 }, { 10 }
        };
    }

    @Test(dataProvider = "parallelGetIds")
    public void parallelGetTest(int id) {
        // 10 API calls run concurrently — fast!
        given()
            .spec(requestSpec)
            .pathParam("id", id)
        .when()
            .get("/posts/{id}")
        .then()
            .statusCode(200);
    }
}
```

---

**Q31: What is a complete BaseTest implementation for RestAssured with TestNG?**

**A:**

```java
package base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import listeners.TestListener;
import utils.ApiConstants;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

@Listeners({ TestListener.class })
public class BaseTest {

    protected RequestSpecification  requestSpec;
    protected ResponseSpecification responseSpec;

    @BeforeClass
    public void setUp() {
        // Base URI from system property or default
        RestAssured.baseURI = System.getProperty("baseUrl", ApiConstants.BASE_URL);

        // Auto-log on failure — no noise on pass
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Obtain auth token if required
        String token = obtainAuthToken();

        // Build reusable request specification
        RequestSpecBuilder reqBuilder = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON);

        if (token != null) {
            reqBuilder.addHeader("Authorization", "Bearer " + token);
        }

        requestSpec = reqBuilder.build();

        // Build reusable response specification
        responseSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(5000L))
            .build();
    }

    private String obtainAuthToken() {
        // Return null if the API doesn't require auth (like JSONPlaceholder)
        // For authenticated APIs:
        /*
        return given()
            .contentType(ContentType.JSON)
            .body(Map.of("username", ApiConstants.USERNAME,
                         "password", ApiConstants.PASSWORD))
            .when().post("/auth/login")
            .then().statusCode(200)
            .extract().path("token");
        */
        return null;
    }
}
```

---

## SECTION 9: STATUS CODES

**Q32: What HTTP status codes should you assert in API tests and when does each occur?**

**A:**

```java
// 200 OK — successful GET, PUT, PATCH
given().when().get("/posts/1").then().statusCode(200);
given().spec(requestSpec).body(payload).pathParam("id",1).when().put("/posts/{id}").then().statusCode(200);

// 201 Created — successful POST that created a resource
given().spec(requestSpec).body(payload).when().post("/posts").then().statusCode(201);

// 204 No Content — successful DELETE or PUT with no response body
given().pathParam("id",1).when().delete("/posts/{id}").then().statusCode(204);

// 400 Bad Request — invalid input (missing required field, wrong type)
given().spec(requestSpec)
    .body("{ \"title\": null }")   // null title where string expected
    .when().post("/posts")
    .then().statusCode(400)
    .body("error", notNullValue());

// 401 Unauthorized — no credentials or invalid credentials
given().when().get("/protected").then().statusCode(401);

// 403 Forbidden — valid credentials but insufficient permissions
given()
    .header("Authorization", "Bearer " + readonlyUserToken)
    .when().delete("/admin/users/1")
    .then().statusCode(403);

// 404 Not Found — resource does not exist
given().pathParam("id", 999999).when().get("/posts/{id}").then().statusCode(404);

// 409 Conflict — duplicate resource (e.g., creating user with existing email)
given().spec(requestSpec)
    .body(Map.of("email", "existing@example.com"))
    .when().post("/users")
    .then().statusCode(409)
    .body("message", containsString("already exists"));

// 422 Unprocessable Entity — validation failure (structurally valid JSON, wrong business logic)
given().spec(requestSpec)
    .body(Map.of("age", -1))     // age cannot be negative
    .when().post("/users")
    .then().statusCode(422);

// 429 Too Many Requests — rate limited
given().when().get("/posts").then().statusCode(429);

// 500 Internal Server Error — server crashed
// Negative test — pass invalid data and check server handles gracefully (not 500)
given().spec(requestSpec)
    .body("not-json-at-all")
    .when().post("/posts")
    .then().statusCode(not(equalTo(500)));

// Assert any of multiple acceptable codes
.statusCode(anyOf(equalTo(200), equalTo(204)));
```

---

## SECTION 10: ERRORS AND TROUBLESHOOTING

**Q33: What are the most common RestAssured errors and how do you fix each?**

**A:**

**Error 1: Connection refused**
```
java.net.ConnectException: Connection refused: connect
```
```java
// Cause: Wrong baseURI, server not running, port wrong
// Fix: Verify the URL
RestAssured.baseURI = "https://jsonplaceholder.typicode.com";  // NOT http://
// Verify the API is running — hit it in browser or Postman first
```

**Error 2: JSON path not found / NullPointerException on extract**
```
java.lang.IllegalArgumentException: Cannot extract path from a response with Content-Type text/html
```
```java
// Cause: Response is not JSON (maybe returned HTML error page)
// Fix: Log the response body to see what actually came back
given()
    .when().get("/posts/1")
    .then()
    .log().body()   // see what the server actually returned
    .statusCode(200)
    .extract().path("id");
```

**Error 3: ClassCastException on extract**
```
java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.Long
```
```java
// Cause: JSON integers parse as Integer but you stored in long
// Fix: Use Integer, not int for path extraction
Integer id = given().when().get("/posts/1").then().extract().path("id");
// Or cast explicitly
int id = (Integer) given().when().get("/posts/1").then().extract().path("id");
```

**Error 4: 401 on every request**
```
// Cause: Auth header not set or token expired
// Fix: Log request headers to verify token is being sent
given()
    .log().headers()   // prints all headers — verify Authorization is present
    .spec(requestSpec)
    .when().get("/protected");
```

**Error 5: Schema validation failed**
```
io.restassured.module.jsv.JsonSchemaValidatorException: ...
```
```java
// Cause 1: Schema file not found
// Fix: File must be exactly in src/test/resources/schemas/post-schema.json
// and the path in code is "schemas/post-schema.json" (relative to resources)

// Cause 2: Response field is wrong type
// Fix: Open the schema file and check field types match the API response
// Common: API returns "id": "1" (string) but schema says "type": "integer"
```

**Error 6: Body assertion fails but you're not sure why**
```java
// Always print the response body when debugging
given()
    .when().get("/posts/1")
    .then()
    .log().body()           // prints response body
    .statusCode(200)
    .body("title", equalTo("expected title"));
// You'll see what the actual title is and can fix the assertion
```

---

**Q34: What is the difference between extract().path() with GPath vs JsonPath?**

**A:**

```java
// RestAssured uses GPath (Groovy's path language) for body assertions and extraction
// GPath is dot-notation based, similar to JsonPath

// GPath examples:
.body("id", equalTo(1))                   // top-level field
.body("address.city", equalTo("London"))  // nested field
.body("[0].id", equalTo(1))              // first array item
.body("users[0].roles[1]", equalTo("editor"))  // nested array

// Extract a list using GPath
List<Integer> ids = given().when().get("/posts").then().extract().path("id");

// GPath filter — find all items where userId == 1
List<Integer> filtered = given().when().get("/posts").then()
    .extract().path("findAll { it.userId == 1 }.id");

// Equivalent using explicit JsonPath
import io.restassured.path.json.JsonPath;

Response response = given().when().get("/posts/1").then().extract().response();
JsonPath jp = response.jsonPath();

int id = jp.getInt("id");
String title = jp.getString("title");
List<Integer> ids2 = jp.getList("id", Integer.class);
// JsonPath is useful when you need to do more complex extraction
// after getting the response object
```

---

**Q35: How do you handle an API that returns an array at the top level?**

**A:**

```java
// Response is a JSON array: [{"id":1,...}, {"id":2,...}, ...]

// Assert on the array directly
given()
.when()
    .get("/posts")
.then()
    .statusCode(200)
    .body("size()", equalTo(100))       // array has 100 items
    .body("[0].id", equalTo(1))         // first item id = 1
    .body("id", hasItem(5))             // array contains an item with id=5
    .body("userId", everyItem(notNullValue()));  // all items have userId

// Extract the whole array into a List
List<Map<String, Object>> posts = given()
    .when().get("/posts")
    .then().statusCode(200)
    .extract().path(".");   // "." = root, returns the entire response as list

System.out.println("First post title: " + posts.get(0).get("title"));

// Extract into typed list using JsonPath
Response response = given().when().get("/posts").then().extract().response();
List<Integer> allIds = response.jsonPath().getList("id", Integer.class);
Assert.assertEquals(allIds.size(), 100);
Assert.assertTrue(allIds.contains(1));

// Extract as array of POJOs
Post[] posts = given()
    .when().get("/posts")
    .then().extract().as(Post[].class);
```

---

**Q36: How do you implement the complete requestSpec pattern in BaseTest?**

**A:**

```java
package base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.BeforeClass;
import utils.ApiConstants;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.lessThan;
import java.util.Map;

public class BaseTest {

    protected RequestSpecification  requestSpec;
    protected ResponseSpecification responseSpec;

    @BeforeClass
    public void setUp() {
        // 1. Set base URI from system property or config
        RestAssured.baseURI = System.getProperty("baseUrl", ApiConstants.BASE_URL);

        // 2. Enable smart logging — logs only on failure
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // 3. Build request spec
        requestSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .log(LogDetail.URI)          // always log the request URL
            .build();

        // 4. Build response spec
        responseSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(5000L))
            .build();
    }
}

// Authenticated BaseTest — when auth is required
public class AuthenticatedBaseTest extends BaseTest {

    @Override
    @BeforeClass
    public void setUp() {
        super.setUp();

        // Login and get token
        String token = given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                "username", System.getProperty("username", ApiConstants.USERNAME),
                "password", System.getProperty("password", ApiConstants.PASSWORD)
            ))
            .when().post(ApiConstants.LOGIN_ENDPOINT)
            .then().statusCode(200)
            .extract().path("token");

        // Rebuild requestSpec with token
        requestSpec = new RequestSpecBuilder()
            .addRequestSpecification(requestSpec)  // keep base settings
            .addHeader("Authorization", "Bearer " + token)
            .build();
    }
}
```

---

## SECTION 11: STRATEGY AND INTERVIEW QUESTIONS

**Q37: What is your strategy for API testing — what do you verify in every test?**

**A:** A complete API test verifies these layers:

**1. Status Code** — always first
```java
.statusCode(200)  // correct code for the operation
```

**2. Response Body — field values**
```java
.body("id", equalTo(requestedId))
.body("title", notNullValue())
```

**3. Response Body — data types**
```java
// Implicit via schema validation
.body(matchesJsonSchemaInClasspath("schemas/post-schema.json"))
```

**4. Response Headers**
```java
.contentType(ContentType.JSON)
.header("Content-Type", containsString("application/json"))
```

**5. Response Time**
```java
.time(lessThan(3000L))
```

**6. Negative scenarios** — what happens with bad input:
- Missing required field → 400
- Wrong data type → 400 or 422
- Non-existent resource → 404
- Unauthenticated → 401
- Insufficient permission → 403

**7. Boundary conditions**:
- Maximum field length
- Minimum values (zero, negative numbers)
- Empty strings vs null

---

**Q38: How do you test that an error response has the correct error message and structure?**

**A:**

```java
// Test: missing required field → 400 with error message
@Test
public void missingTitleReturns400() {
    given()
        .spec(requestSpec)
        .body("{ \"body\": \"Content without title\" }")  // no title field
    .when()
        .post("/posts")
    .then()
        .statusCode(400)
        .body("error",   notNullValue())
        .body("message", containsString("title"))
        .body("code",    equalTo("VALIDATION_ERROR"));
}

// Test: unauthorized request → 401 with correct error
@Test
public void noTokenReturns401() {
    given()
        .contentType(ContentType.JSON)
        // NO Authorization header
    .when()
        .get("/protected-resource")
    .then()
        .statusCode(401)
        .body("error",   equalTo("Unauthorized"))
        .body("message", containsString("token"));
}

// Test: duplicate resource → 409
@Test
public void duplicateUserReturns409() {
    Map<String, Object> existingUser = Map.of(
        "email", "admin@example.com"  // email that already exists
    );

    given()
        .spec(requestSpec)
        .body(existingUser)
    .when()
        .post("/users")
    .then()
        .statusCode(409)
        .body("message", containsString("already exists"));
}
```

---

**Q39: How do you structure your RestAssured project for maintainability?**

**A:**

```
src/
├── test/
│   ├── java/
│   │   ├── base/
│   │   │   └── BaseTest.java              ← requestSpec, @BeforeClass
│   │   ├── tests/
│   │   │   ├── UserApiTest.java           ← test classes by resource
│   │   │   ├── PostApiTest.java
│   │   │   └── AuthApiTest.java
│   │   ├── models/
│   │   │   ├── Post.java                  ← POJOs for serialization
│   │   │   └── User.java
│   │   ├── utils/
│   │   │   ├── ApiConstants.java          ← BASE_URL, endpoint paths
│   │   │   └── TestDataProviders.java     ← @DataProvider methods
│   │   └── listeners/
│   │       ├── TestListener.java          ← ITestListener
│   │       └── RetryAnalyzer.java
│   └── resources/
│       ├── testng.xml
│       ├── schemas/
│       │   ├── post-schema.json
│       │   └── user-schema.json
│       └── data/
│           └── test-data.csv
```

```java
// ApiConstants.java — one place for all URLs
public class ApiConstants {
    public static final String BASE_URL        = "https://jsonplaceholder.typicode.com";
    public static final String POSTS_ENDPOINT  = "/posts";
    public static final String USERS_ENDPOINT  = "/users";
    public static final String LOGIN_ENDPOINT  = "/auth/login";
    public static final String USERNAME        = "admin";
    public static final String PASSWORD        = "Admin@123";
}
```

Design principles:
- Never hardcode the base URL in a test — use `ApiConstants` or system property
- Never repeat auth setup — do it once in `BaseTest`
- Never duplicate assertions — use `ResponseSpecification` for shared ones
- Use `@DataProvider` for any test that runs with multiple inputs
- Schema validate every response — protects against contract changes

---

**Q40: What are the most common interview questions asked about RestAssured API testing?**

**A:**

**"How do you handle authentication in your framework?"**
```
Answer: In BaseTest @BeforeClass, I call the login endpoint, capture the token,
and add it to a RequestSpecBuilder. All tests use requestSpec — single place
to change auth if token format changes.
```

**"How do you test negative scenarios?"**
```
Answer: Every endpoint gets at least: missing required field (400),
invalid data type (400/422), non-existent resource (404),
unauthenticated (401), unauthorized role (403).
I use @DataProvider to run happy path + all negative paths from one test method.
```

**"What is the difference between path param and query param?"**
```
Path param:  /posts/{id}     → identifies the specific resource
Query param: /posts?userId=1 → filters, sorts, or paginates results
```

**"How do you make your tests run faster?"**
```
1. parallel="methods" thread-count="10" in testng.xml
2. parallel=true on @DataProvider for data-driven tests
3. One API call per test — don't chain requests unless testing a workflow
4. Use ResponseSpecification to avoid re-asserting basics
5. Don't use Thread.sleep — RestAssured is synchronous
```

**"How do you ensure your tests are independent?"**
```
1. Each test creates its own test data (or uses existing read-only data)
2. Tests don't depend on execution order
3. Cleanup in @AfterMethod or at start of test
4. No shared mutable state between tests
5. Use @DataProvider instead of dependsOnMethods
```

**"What do you do when a test is flaky?"**
```
1. First determine if the API is flaky or the test is flaky
2. Add retry with IRetryAnalyzer (max 2 retries)
3. Add better logging to understand failure pattern
4. Check for race conditions if running in parallel
5. Check for test data conflicts between parallel tests
```
