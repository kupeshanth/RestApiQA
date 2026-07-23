# Part 7 — API Testing | 80 Questions | RestAssured + Postman + Concepts

> CV context: Kupeshanth used Postman and JIRA at Cerexio, RestAssured at Qoria Lanka, and built API test suites with Python+Pytest and Playwright. These 80 questions cover everything from HTTP fundamentals to advanced RestAssured patterns.

---

## Q1. What is an API? What is a REST API?

**API (Application Programming Interface):** A contract that defines how software components communicate. It specifies what requests can be made, how to make them, and what responses to expect.

**REST API (Representational State Transfer):** An architectural style for building APIs over HTTP. A REST API exposes resources (Users, Orders, Products) via URLs, and clients manipulate those resources using HTTP methods.

```
Client                    REST API Server
  │                           │
  │── GET /api/users ─────────>│
  │<── 200 OK + JSON list ────│
  │                           │
  │── POST /api/users ────────>│  (create)
  │<── 201 Created + new user ─│
  │                           │
  │── PUT /api/users/5 ───────>│  (update)
  │<── 200 OK + updated user ──│
  │                           │
  │── DELETE /api/users/5 ────>│
  │<── 204 No Content ─────────│
```

---

## Q2. What makes an API RESTful? (6 constraints)

Roy Fielding defined 6 constraints. An API is truly RESTful only if it satisfies all 6:

1. **Client-Server** — Client and server are separate; client handles UI, server handles data. They evolve independently.

2. **Stateless** — Each request contains ALL information needed. Server stores NO session state. Auth token must be sent with every request.

3. **Cacheable** — Responses must declare themselves cacheable or non-cacheable. Reduces server load. (`Cache-Control`, `ETag` headers)

4. **Uniform Interface** — Consistent way to interact with resources:
   - Resource identification via URI (`/users/5`)
   - Manipulation via representations (JSON/XML)
   - Self-descriptive messages
   - HATEOAS (links to related actions in response)

5. **Layered System** — Client doesn't know if it's talking to the server directly or a proxy/load balancer/CDN. Each layer only knows about the next one.

6. **Code on Demand (optional)** — Server can return executable code (JavaScript). The only optional constraint.

---

## Q3. What is the difference between REST and SOAP?

| Feature | REST | SOAP |
|---|---|---|
| Protocol | HTTP only | HTTP, SMTP, TCP |
| Format | JSON, XML, plain text | XML only |
| Message structure | Flexible | Strict SOAP envelope |
| WSDL | No (uses OpenAPI/Swagger) | Yes (required) |
| Standards | None enforced | WS-Security, WS-AtomicTransaction |
| Performance | Faster (lighter) | Slower (verbose XML) |
| Caching | Supported | Not supported |
| Error handling | HTTP status codes | SOAP Fault |
| Use case | Public web APIs, mobile | Enterprise, banking, legacy |

**SOAP example (verbose):**
```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <GetUser>
      <UserId>5</UserId>
    </GetUser>
  </soap:Body>
</soap:Envelope>
```

**REST equivalent:**
```
GET /api/users/5
Accept: application/json
```

---

## Q4. What are all HTTP methods?

| Method | Purpose | Has Body | Idempotent | Safe |
|---|---|---|---|---|
| GET | Retrieve resource | No | Yes | Yes |
| POST | Create resource | Yes | No | No |
| PUT | Replace entire resource | Yes | Yes | No |
| PATCH | Partial update | Yes | No | No |
| DELETE | Delete resource | No | Yes | No |
| HEAD | GET without body (headers only) | No | Yes | Yes |
| OPTIONS | List allowed methods | No | Yes | Yes |

```java
// RestAssured examples
given().when().get("/api/users/1");
given().body(json).when().post("/api/users");
given().body(json).when().put("/api/users/1");
given().body(json).when().patch("/api/users/1");
given().when().delete("/api/users/1");
given().when().head("/api/users");
given().when().options("/api/users");
```

---

## Q5. What is idempotency? Which methods are idempotent?

**Idempotent:** Calling the same request multiple times produces the same result. The server state is the same after 1 call or 10 calls.

| Method | Idempotent | Why |
|---|---|---|
| GET | Yes | Just reads — no change |
| PUT | Yes | Replacing with same data = same result |
| DELETE | Yes | Delete once or 10 times = resource is gone |
| HEAD | Yes | Just reads headers |
| OPTIONS | Yes | Just reads options |
| POST | No | Creates a new resource each time |
| PATCH | No | May apply incremental changes |

```
// Idempotent — safe to retry
DELETE /api/orders/99
→ First call: 204 No Content (deleted)
→ Second call: 404 Not Found (already deleted)
→ State: same (resource doesn't exist)

// Not idempotent — dangerous to retry
POST /api/orders
→ First call: 201 (order #1 created)
→ Second call: 201 (order #2 created — duplicate!)
```

**Why it matters for QA:** Retry logic should only retry idempotent methods. POST retries can create duplicate records.

---

## Q6. What is the difference between PUT and PATCH?

**PUT:** Replaces the **entire** resource. Fields not included in the body are cleared/reset.

**PATCH:** Updates **only** the specified fields. Other fields are unchanged.

```java
// Current state: { "id": 1, "name": "Alice", "email": "alice@co.com", "role": "USER" }

// PUT — must send ALL fields
given()
  .body("{ \"name\": \"Alice Smith\", \"email\": \"alice@co.com\", \"role\": \"USER\" }")
  .when().put("/api/users/1");
// Result: { "id": 1, "name": "Alice Smith", "email": "alice@co.com", "role": "USER" }
// If you omit "role" → it becomes null/default

// PATCH — send only what changes
given()
  .body("{ \"name\": \"Alice Smith\" }")
  .when().patch("/api/users/1");
// Result: { "id": 1, "name": "Alice Smith", "email": "alice@co.com", "role": "USER" }
// Only name changed, others untouched
```

---

## Q7. What is the difference between path parameters and query parameters?

**Path parameters:** Part of the URL structure. Identify a specific resource.

**Query parameters:** After `?`. Filter, sort, paginate, or search a collection.

```
// Path parameter — identifies specific resource
GET /api/users/42          → user with ID 42
GET /api/orders/99/items   → items of order 99

// Query parameters — filter/modify the collection
GET /api/users?role=admin               → filter by role
GET /api/users?page=2&limit=20         → pagination
GET /api/products?sort=price&order=asc  → sorting
GET /api/orders?status=pending&from=2024-01-01  → multiple filters
```

```java
// RestAssured path parameters
given()
  .pathParam("userId", 42)
  .when().get("/api/users/{userId}");

// RestAssured query parameters
given()
  .queryParam("role", "admin")
  .queryParam("page", 2)
  .when().get("/api/users");
// Sends: GET /api/users?role=admin&page=2
```

---

## Q8. What are all HTTP status codes? (full list with meaning)

**1xx — Informational**
- `100 Continue` — keep sending the request body
- `101 Switching Protocols` — WebSocket upgrade

**2xx — Success**
- `200 OK` — standard success
- `201 Created` — resource created (POST)
- `202 Accepted` — async processing started
- `204 No Content` — success, no body (DELETE, PUT with no return)
- `206 Partial Content` — range request (streaming)

**3xx — Redirection**
- `301 Moved Permanently` — permanent redirect
- `302 Found` — temporary redirect
- `304 Not Modified` — use cached version

**4xx — Client Error**
- `400 Bad Request` — malformed request / validation failure
- `401 Unauthorized` — missing or invalid auth
- `403 Forbidden` — auth OK but no permission
- `404 Not Found` — resource doesn't exist
- `405 Method Not Allowed` — wrong HTTP method
- `408 Request Timeout`
- `409 Conflict` — duplicate resource / state conflict
- `410 Gone` — resource permanently deleted
- `415 Unsupported Media Type` — wrong Content-Type
- `422 Unprocessable Entity` — validation error (semantically wrong)
- `429 Too Many Requests` — rate limit exceeded

**5xx — Server Error**
- `500 Internal Server Error` — unhandled exception
- `502 Bad Gateway` — upstream server bad response
- `503 Service Unavailable` — server down / overloaded
- `504 Gateway Timeout` — upstream server timed out

---

## Q9. What is 200 vs 201 vs 204?

```java
// 200 OK — request succeeded, body returned
// Use: GET, PUT (with return), POST login
given().when().get("/api/users/1")
  .then().statusCode(200)
        .body("name", equalTo("Alice"));

// 201 Created — POST created a new resource
// Location header usually points to new resource
given().body(newUser).when().post("/api/users")
  .then().statusCode(201)
        .header("Location", containsString("/api/users/"))
        .body("id", notNullValue());

// 204 No Content — success but no response body
// Use: DELETE, PUT/PATCH when no data to return
given().when().delete("/api/users/5")
  .then().statusCode(204)
        .body(emptyOrNullString());
```

---

## Q10. What is 400 vs 401 vs 403 vs 404?

```java
// 400 Bad Request — client sent malformed data
given().body("{\"email\": \"not-valid-email\"}").when().post("/api/users")
  .then().statusCode(400)
        .body("errors[0].field", equalTo("email"))
        .body("errors[0].message", containsString("valid email"));

// 401 Unauthorized — no/bad credentials
given().when().get("/api/admin/users")  // no auth header
  .then().statusCode(401)
        .body("message", equalTo("Authentication required"));

// 403 Forbidden — authenticated but not authorized
given().header("Authorization", "Bearer USER_TOKEN")
       .when().get("/api/admin/users")  // user token, admin endpoint
  .then().statusCode(403)
        .body("message", equalTo("Insufficient permissions"));

// 404 Not Found — resource doesn't exist
given().when().get("/api/users/99999")
  .then().statusCode(404)
        .body("message", equalTo("User not found"));
```

---

## Q11. What is 409 vs 415 vs 422 vs 429?

```java
// 409 Conflict — duplicate or state conflict
// E.g., register with existing email
given().body("{\"email\": \"existing@test.com\"}").when().post("/api/register")
  .then().statusCode(409)
        .body("message", containsString("already exists"));

// 415 Unsupported Media Type — wrong Content-Type
given().header("Content-Type", "text/plain")  // should be application/json
       .body("some text").when().post("/api/users")
  .then().statusCode(415);

// 422 Unprocessable Entity — valid syntax, but semantic validation failed
// E.g., date in past, quantity negative, min > max
given().body("{\"startDate\":\"2020-01-01\",\"endDate\":\"2019-01-01\"}")
       .when().post("/api/bookings")
  .then().statusCode(422)
        .body("message", containsString("end date must be after start date"));

// 429 Too Many Requests — rate limit hit
// Headers tell you when to retry
given().when().get("/api/search?q=test")
  .then().statusCode(429)
        .header("Retry-After", notNullValue())
        .body("message", containsString("rate limit"));
```

---

## Q12. What is 500 vs 502 vs 503 vs 504?

```java
// 500 Internal Server Error — unhandled exception on server
// Test by sending data that triggers a bug
given().body("{\"userId\": null}").when().post("/api/orders")
  .then().statusCode(500);
// Note: 500 should NOT leak stack traces in production

// 502 Bad Gateway — proxy/gateway received invalid response from upstream
// Usually infrastructure issue, not directly testable

// 503 Service Unavailable — server overloaded or down for maintenance
// May include Retry-After header
given().when().get("/api/reports/generate")
  .then().statusCode(503)
        .header("Retry-After", notNullValue());

// 504 Gateway Timeout — upstream took too long
// Test by sending request that exceeds server timeout
```

---

## Q13. What are request headers? Common ones?

**Request headers** are key-value pairs sent with HTTP requests, providing metadata about the request.

```java
// Common request headers
given()
  .header("Content-Type", "application/json")       // format of request body
  .header("Accept", "application/json")             // format client wants back
  .header("Authorization", "Bearer eyJhbGc...")    // auth token
  .header("X-Request-ID", "uuid-123")              // correlation ID
  .header("X-API-Key", "abc123")                   // API key auth
  .header("User-Agent", "RestAssured/5.0")         // client identifier
  .header("Accept-Language", "en-US")              // preferred language
  .header("Cache-Control", "no-cache")             // caching directive
  .when().get("/api/users");
```

---

## Q14. What is Content-Type? What is Accept?

**Content-Type:** Tells the server what format the **request body** is in.

**Accept:** Tells the server what format the **client wants** in the response.

```java
// Client sends JSON, wants JSON back
given()
  .header("Content-Type", "application/json")
  .header("Accept", "application/json")
  .body("{\"name\": \"Alice\"}")
  .when().post("/api/users");

// Client sends form data
given()
  .header("Content-Type", "application/x-www-form-urlencoded")
  .formParam("name", "Alice")
  .when().post("/api/users");

// File upload
given()
  .header("Content-Type", "multipart/form-data")
  .multiPart("file", new File("data.csv"))
  .when().post("/api/upload");

// Client accepts XML
given()
  .header("Accept", "application/xml")
  .when().get("/api/users/1");

// Common MIME types:
// application/json    — JSON body
// application/xml     — XML body
// text/plain          — plain text
// multipart/form-data — file upload
// application/x-www-form-urlencoded — HTML form data
```

---

## Q15. What is Authorization header? Types of auth?

```java
// 1. Bearer Token (JWT / OAuth2)
given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9...")
       .when().get("/api/profile");

// 2. Basic Authentication (Base64 of username:password)
given().auth().basic("admin", "password")
       .when().get("/api/admin");
// Sends: Authorization: Basic YWRtaW46cGFzc3dvcmQ=

// 3. API Key (in header)
given().header("X-API-Key", "abc123def456")
       .when().get("/api/data");

// 4. API Key (in query param)
given().queryParam("api_key", "abc123def456")
       .when().get("/api/data");

// 5. Digest Auth
given().auth().digest("user", "pass")
       .when().get("/api/resource");

// 6. OAuth2 (token in header)
given().auth().oauth2("access-token-here")
       .when().get("/api/me");
```

---

## Q16. What is Bearer token authentication?

**Bearer token** is a security token where possession grants access. The token (usually a JWT) is sent in the `Authorization` header.

```java
// Step 1: Login to get token
String token =
  given()
    .body("{\"email\":\"user@test.com\",\"password\":\"pass123\"}")
    .contentType("application/json")
  .when()
    .post("/api/auth/login")
  .then()
    .statusCode(200)
    .extract().path("token");

// Step 2: Use token for protected endpoints
given()
  .header("Authorization", "Bearer " + token)
  .when()
  .get("/api/profile")
  .then()
  .statusCode(200)
  .body("email", equalTo("user@test.com"));

// JWT structure: header.payload.signature
// eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQHRlc3QuY29tIn0.SIGNATURE
// Decode payload (Base64): {"sub":"user@test.com","exp":1700000000}
```

---

## Q17. What is Basic authentication?

**Basic auth** sends `username:password` encoded as Base64 in the Authorization header.

```java
// RestAssured — Basic auth
given()
  .auth().basic("admin", "Admin123!")
  .when()
  .get("/api/admin/dashboard")
  .then()
  .statusCode(200);

// What it sends in header:
// Authorization: Basic YWRtaW46QWRtaW4xMjMh
// (Base64 of "admin:Admin123!")

// Manual construction
String credentials = Base64.getEncoder()
  .encodeToString("admin:Admin123!".getBytes());
given()
  .header("Authorization", "Basic " + credentials)
  .when().get("/api/admin");
```

**Security note:** Basic auth over HTTP is insecure — credentials are only Base64 encoded (not encrypted). Always use HTTPS.

---

## Q18. What is OAuth2? How do you test the login→token→use flow?

OAuth2 is an authorization framework. The client gets an **access token** from an authorization server and uses it to access protected resources.

**Common OAuth2 flows for API testing:**

```java
// Client Credentials Flow (machine-to-machine)
String accessToken =
  given()
    .formParam("grant_type", "client_credentials")
    .formParam("client_id",  "my-client-id")
    .formParam("client_secret", "my-client-secret")
    .formParam("scope", "read:users")
  .when()
    .post("https://auth.example.com/oauth/token")
  .then()
    .statusCode(200)
    .extract().path("access_token");

// Password Flow (user credentials)
String userToken =
  given()
    .formParam("grant_type", "password")
    .formParam("username",   "user@test.com")
    .formParam("password",   "pass123")
    .formParam("client_id",  "app-client")
  .when()
    .post("https://auth.example.com/oauth/token")
  .then()
    .statusCode(200)
    .body("token_type", equalTo("Bearer"))
    .body("expires_in", greaterThan(0))
    .extract().path("access_token");

// Use token
given()
  .auth().oauth2(userToken)
  .when()
  .get("/api/me")
  .then()
  .statusCode(200);
```

---

## Q19. What is API key authentication?

```java
// API key in header (most common)
given()
  .header("X-API-Key", System.getenv("API_KEY"))
  .when()
  .get("/api/weather?city=Colombo")
  .then()
  .statusCode(200);

// API key in query param
given()
  .queryParam("apikey", System.getenv("API_KEY"))
  .when()
  .get("/api/data")
  .then()
  .statusCode(200);

// Test missing API key → 401
given()
  .when()
  .get("/api/data")  // no key
  .then()
  .statusCode(401)
  .body("message", equalTo("API key required"));

// Test invalid API key → 403
given()
  .header("X-API-Key", "invalid-key-123")
  .when()
  .get("/api/data")
  .then()
  .statusCode(403)
  .body("message", equalTo("Invalid API key"));
```

---

## Q20. What is CORS? How does it affect API testing?

**CORS (Cross-Origin Resource Sharing):** Browser security policy preventing JavaScript from making requests to a different origin (domain/protocol/port) unless the server explicitly allows it.

**CORS only affects browser-based requests — NOT RestAssured/Postman/curl.**

```
Browser (origin: https://app.com) → API (origin: https://api.example.com)
→ Browser sends OPTIONS preflight first
→ API must respond with: Access-Control-Allow-Origin: https://app.com

RestAssured → API → No CORS check (not a browser)
Postman → API → No CORS check (not a browser)
```

**Testing CORS with RestAssured:**
```java
// Simulate CORS preflight
given()
  .header("Origin", "https://app.example.com")
  .header("Access-Control-Request-Method", "POST")
  .header("Access-Control-Request-Headers", "Content-Type, Authorization")
  .when()
  .options("/api/users")
  .then()
  .statusCode(200)
  .header("Access-Control-Allow-Origin", "https://app.example.com")
  .header("Access-Control-Allow-Methods", containsString("POST"))
  .header("Access-Control-Allow-Headers", containsString("Authorization"));
```

---

## Q21. What is JSON? What is XML?

**JSON (JavaScript Object Notation):** Lightweight, human-readable data format. Default for REST APIs.

```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@test.com",
  "roles": ["ADMIN", "USER"],
  "address": {
    "street": "123 Main St",
    "city": "Colombo"
  },
  "active": true,
  "score": 99.5
}
```

**XML (eXtensible Markup Language):** Verbose, tag-based. Used in SOAP and legacy systems.

```xml
<user>
  <id>1</id>
  <name>Alice</name>
  <email>alice@test.com</email>
  <roles>
    <role>ADMIN</role>
    <role>USER</role>
  </roles>
  <active>true</active>
</user>
```

| Feature | JSON | XML |
|---|---|---|
| Verbosity | Low | High |
| Readability | High | Medium |
| Data types | String, number, boolean, null, array, object | String only (all values are text) |
| Schema | JSON Schema | XSD |
| Query | JSONPath | XPath |

---

## Q22. What is JSONPath? Give examples.

JSONPath is a query language for extracting values from JSON, similar to XPath for XML.

```json
// Sample JSON
{
  "store": {
    "users": [
      { "id": 1, "name": "Alice", "role": "ADMIN", "active": true },
      { "id": 2, "name": "Bob",   "role": "USER",  "active": false },
      { "id": 3, "name": "Carol", "role": "USER",  "active": true }
    ],
    "total": 3
  }
}
```

```java
// JSONPath expressions in RestAssured
.body("store.total", equalTo(3))
.body("store.users[0].name", equalTo("Alice"))
.body("store.users[1].role", equalTo("USER"))
.body("store.users.size()", equalTo(3))
.body("store.users.name", hasItems("Alice", "Bob", "Carol"))
.body("store.users.findAll { it.active == true }.size()", equalTo(2))
.body("store.users.find { it.role == 'ADMIN' }.name", equalTo("Alice"))
.body("store.users*.id", hasItems(1, 2, 3))

// Extract a value
String firstName = response.jsonPath().getString("store.users[0].name");
int total        = response.jsonPath().getInt("store.total");
List<String> names = response.jsonPath().getList("store.users.name");
```

---

## Q23. What is the difference between synchronous and asynchronous APIs?

**Synchronous:** Client sends request and **waits** until response is ready. Most REST APIs are synchronous.

```
Client → POST /api/report/generate
        [waits 30 seconds]
Server → 200 OK + report data
```

**Asynchronous:** Client sends request, server immediately returns an acknowledgement. Client polls or receives a callback when processing is done.

```
Client → POST /api/report/generate
Server → 202 Accepted + { "jobId": "job-123" }

// Later — client polls
Client → GET /api/jobs/job-123
Server → 200 OK + { "status": "PROCESSING" }

// Keep polling
Client → GET /api/jobs/job-123
Server → 200 OK + { "status": "COMPLETED", "reportUrl": "..." }
```

```java
// Testing async API with RestAssured
// Step 1: Submit job
String jobId =
  given().body(reportRequest).when().post("/api/reports")
  .then().statusCode(202)
  .extract().path("jobId");

// Step 2: Poll until complete
String status = "";
int maxAttempts = 10;
for (int i = 0; i < maxAttempts; i++) {
  status = given().when().get("/api/jobs/" + jobId)
             .then().statusCode(200)
             .extract().path("status");
  if ("COMPLETED".equals(status) || "FAILED".equals(status)) break;
  Thread.sleep(2000); // poll every 2 seconds
}

assertEquals("COMPLETED", status);
```

---

## Q24. What is REST API contract testing?

**Contract testing** verifies that a **consumer** (client) and **provider** (server) agree on the API contract (request/response structure). Catches breaking changes before deployment.

**Types:**
- **Consumer-driven contract testing (Pact):** Consumer defines what it needs; provider verifies it satisfies those needs.
- **Provider-side contract testing:** Provider publishes schema (OpenAPI); consumers validate against it.

```java
// Basic contract test with RestAssured — schema validation
@Test
public void testUserContractSchema() {
  given()
    .when().get("/api/users/1")
    .then()
    .statusCode(200)
    .body(matchesJsonSchemaInClasspath("schemas/user-response.json"));
}
```

```json
// schemas/user-response.json
{
  "$schema": "http://json-schema.org/draft-07/schema",
  "type": "object",
  "required": ["id", "name", "email"],
  "properties": {
    "id":    { "type": "integer" },
    "name":  { "type": "string" },
    "email": { "type": "string", "format": "email" }
  }
}
```

---

## Q25. What is JSON Schema validation?

JSON Schema defines the **structure and types** of a JSON document. Validation confirms a response matches the expected schema.

```xml
<!-- pom.xml -->
<dependency>
  <groupId>io.rest-assured</groupId>
  <artifactId>json-schema-validator</artifactId>
  <version>5.3.2</version>
</dependency>
```

```json
// src/test/resources/schemas/create-user-response.json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "CreateUserResponse",
  "type": "object",
  "required": ["id", "name", "email", "createdAt"],
  "properties": {
    "id":        { "type": "integer", "minimum": 1 },
    "name":      { "type": "string",  "minLength": 2 },
    "email":     { "type": "string",  "format": "email" },
    "role":      { "type": "string",  "enum": ["ADMIN", "USER", "VIEWER"] },
    "active":    { "type": "boolean" },
    "createdAt": { "type": "string",  "format": "date-time" }
  },
  "additionalProperties": false
}
```

```java
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Test
public void testCreateUserResponseMatchesSchema() {
  given()
    .body("{\"name\": \"Alice\", \"email\": \"alice@test.com\"}")
    .contentType("application/json")
  .when()
    .post("/api/users")
  .then()
    .statusCode(201)
    .body(matchesJsonSchemaInClasspath("schemas/create-user-response.json"));
}
```

---

## Q26. What is Postman? Walk through its features.

**Postman** is a GUI-based API testing platform. Features:

1. **Request Builder:** Build GET/POST/PUT/DELETE requests with headers, body, params, auth.
2. **Collections:** Organize related requests into folders.
3. **Environments:** Switch between dev/staging/prod configurations using variables.
4. **Tests tab:** Write JavaScript assertions for each request.
5. **Pre-request Scripts:** Run JS before the request (set dynamic values, chain tokens).
6. **Collection Runner:** Run an entire collection in sequence.
7. **Newman:** CLI runner for Postman collections (used in CI).
8. **Mock Servers:** Create fake APIs for testing.
9. **API Documentation:** Auto-generate docs from collections.
10. **Monitors:** Schedule collection runs.
11. **Flows:** Visual API workflow builder.
12. **Workspaces:** Team collaboration.

---

## Q27. What are Postman Collections?

A **Collection** is a group of saved API requests organized into folders. Collections can be exported, shared, and run via Newman.

**Structure:**
```
My API Collection
├── Auth
│   ├── POST Login
│   └── POST Refresh Token
├── Users
│   ├── GET All Users
│   ├── GET User by ID
│   ├── POST Create User
│   ├── PUT Update User
│   └── DELETE User
└── Orders
    ├── GET Orders
    └── POST Create Order
```

**Collection-level scripts:** Pre-request scripts and tests that apply to every request in the collection.

**Export → JSON file → commit to git → run in CI with Newman.**

---

## Q28. What are Postman Environments and Variables?

**Environments** hold variable sets for different targets (dev, staging, prod).

**Variable types:**

| Type | Scope | Use case |
|---|---|---|
| Environment | Current environment | `baseUrl`, `apiKey` |
| Collection | All requests in collection | Shared auth token |
| Global | All collections | Rarely used |
| Local | Current request only | Temp computation |

```javascript
// Using variables in Postman
// URL field:
{{baseUrl}}/api/users/{{userId}}

// Header value:
Authorization: Bearer {{authToken}}

// Setting variable from response (Pre-request or Tests tab)
pm.environment.set("authToken", pm.response.json().token);
pm.environment.set("userId",    pm.response.json().id);

// Getting variable
const token = pm.environment.get("authToken");
```

---

## Q29. How do you chain requests in Postman?

Use the **Tests tab** to extract values from one response and store them for the next request.

```javascript
// Request 1: POST /auth/login
// Tests tab:
const json = pm.response.json();
pm.environment.set("authToken", json.token);
pm.environment.set("userId",    json.userId);

// Request 2: GET /users/{{userId}}
// Uses {{authToken}} in Authorization header and {{userId}} in URL
// Automatically uses values set by Request 1

// Request 3: DELETE /users/{{userId}}
// Same variables, different action
```

**Collection Runner:** Runs requests in order, passing variables between them — automated chain testing.

---

## Q30. How do you write test scripts in Postman? (JavaScript assertions)

```javascript
// Status code
pm.test("Status is 200", () => {
  pm.response.to.have.status(200);
});

// Response time
pm.test("Response under 2 seconds", () => {
  pm.expect(pm.response.responseTime).to.be.below(2000);
});

// Body field
pm.test("Name is Alice", () => {
  const json = pm.response.json();
  pm.expect(json.name).to.eql("Alice");
});

// Header
pm.test("Content-Type is JSON", () => {
  pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

// Array assertions
pm.test("Users list not empty", () => {
  const json = pm.response.json();
  pm.expect(json.data).to.be.an("array").that.is.not.empty;
  pm.expect(json.data).to.have.lengthOf.above(0);
});

// Nested field
pm.test("User has valid email format", () => {
  const json = pm.response.json();
  pm.expect(json.email).to.match(/^[\w.+-]+@[\w-]+\.[\w.]+$/);
});

// Store for chaining
pm.test("Store token", () => {
  const json = pm.response.json();
  pm.expect(json.token).to.be.a("string");
  pm.environment.set("authToken", json.token);
});
```

---

## Q31. How do you run a Postman Collection via Newman (CLI)?

```bash
# Install Newman
npm install -g newman

# Run collection (JSON export from Postman)
newman run MyAPI.postman_collection.json

# Run with environment
newman run MyAPI.postman_collection.json \
  -e staging.postman_environment.json

# Run with HTML reporter
npm install -g newman-reporter-htmlextra
newman run MyAPI.postman_collection.json \
  -e staging.postman_environment.json \
  -r htmlextra --reporter-htmlextra-export newman-report.html

# Run specific folder
newman run MyAPI.postman_collection.json \
  --folder "Auth Tests"

# Set iterations and delay
newman run MyAPI.postman_collection.json \
  -n 3 --delay-request 500

# GitHub Actions:
- name: Run API Tests
  run: newman run collections/MyAPI.json -e envs/staging.json -r cli,junit --reporter-junit-export results/newman.xml
```

---

## Q32. What is RestAssured? Why use it over raw HTTP client?

**RestAssured** is a Java library for testing REST APIs. It provides a fluent DSL that reads like English.

**Why over raw HTTP client (HttpURLConnection/Apache HttpClient):**

```java
// Raw HttpURLConnection — verbose, error-prone
URL url = new URL("https://api.example.com/users/1");
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
conn.setRequestProperty("Authorization", "Bearer " + token);
int status = conn.getResponseCode();
BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
String response = in.lines().collect(Collectors.joining());
conn.disconnect();
// Now parse JSON manually...

// RestAssured — readable, declarative
given()
  .header("Authorization", "Bearer " + token)
.when()
  .get("/api/users/1")
.then()
  .statusCode(200)
  .body("name", equalTo("Alice"));
```

**RestAssured advantages:**
- BDD-style given/when/then syntax
- Built-in JSON/XML parsing and assertions
- JSONPath/XPath support
- Auth helpers (basic, bearer, OAuth2)
- Request/response logging
- JSON Schema validation
- Integration with TestNG/JUnit

---

## Q33. What is the given/when/then pattern in RestAssured?

```java
import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

// given: setup the request (headers, body, params, auth)
// when:  execute the HTTP action (get, post, put, delete)
// then:  assert the response (status, body, headers)

given()
  .baseUri("https://reqres.in")
  .header("Content-Type", "application/json")
  .header("Accept", "application/json")
  .body("{ \"name\": \"Alice\", \"job\": \"QA\" }")
.when()
  .post("/api/users")
.then()
  .log().ifValidationFails()
  .statusCode(201)
  .body("name", equalTo("Alice"))
  .body("job",  equalTo("QA"))
  .body("id",   notNullValue())
  .time(lessThan(3000L));
```

---

## Q34. How do you set base URI globally?

```java
import io.restassured.RestAssured;

// Option 1: Set globally in @BeforeClass or @BeforeSuite
@BeforeClass
public static void setup() {
  RestAssured.baseURI  = "https://api.example.com";
  RestAssured.basePath = "/v1";
  RestAssured.port     = 443;

  // Now all requests use this base
  // given().when().get("/users") → GET https://api.example.com:443/v1/users
}

// Option 2: Via RequestSpecification (preferred for multi-env)
RequestSpecification requestSpec = new RequestSpecBuilder()
  .setBaseUri("https://api.example.com")
  .setBasePath("/v1")
  .setContentType(ContentType.JSON)
  .addHeader("Accept", "application/json")
  .build();

RestAssured.requestSpecification = requestSpec;
```

---

## Q35. What is RequestSpecification?

`RequestSpecification` is a reusable object holding common request settings — eliminates duplication.

```java
import io.restassured.specification.RequestSpecification;
import io.restassured.builder.RequestSpecBuilder;

public class ApiConfig {

  public static RequestSpecification getBaseSpec() {
    return new RequestSpecBuilder()
      .setBaseUri(System.getProperty("baseUrl", "https://api.example.com"))
      .setBasePath("/api/v1")
      .setContentType(ContentType.JSON)
      .addHeader("Accept", "application/json")
      .addHeader("X-Request-Source", "automation")
      .log(LogDetail.URI)
      .build();
  }

  public static RequestSpecification getAuthSpec(String token) {
    return new RequestSpecBuilder()
      .addRequestSpecification(getBaseSpec())
      .addHeader("Authorization", "Bearer " + token)
      .build();
  }
}

// Usage in tests
@Test
public void testGetUsers() {
  given()
    .spec(ApiConfig.getAuthSpec(authToken))
  .when()
    .get("/users")
  .then()
    .statusCode(200);
}
```

---

## Q36. What is ResponseSpecification?

`ResponseSpecification` is a reusable object holding common response assertions.

```java
import io.restassured.specification.ResponseSpecification;
import io.restassured.builder.ResponseSpecBuilder;

public class ApiConfig {

  public static ResponseSpecification getSuccessSpec() {
    return new ResponseSpecBuilder()
      .expectStatusCode(200)
      .expectContentType(ContentType.JSON)
      .expectHeader("X-Request-ID", notNullValue())
      .expectResponseTime(lessThan(3000L))
      .build();
  }

  public static ResponseSpecification getCreatedSpec() {
    return new ResponseSpecBuilder()
      .expectStatusCode(201)
      .expectContentType(ContentType.JSON)
      .expectHeader("Location", notNullValue())
      .build();
  }
}

// Usage
@Test
public void testGetUser() {
  given().spec(requestSpec)
  .when().get("/users/1")
  .then().spec(ApiConfig.getSuccessSpec())
         .body("id", equalTo(1));
}
```

---

## Q37. How do you set auth in requestSpec?

```java
// Bearer token in requestSpec
RequestSpecification authSpec = new RequestSpecBuilder()
  .setBaseUri("https://api.example.com")
  .addHeader("Authorization", "Bearer " + token)
  .build();

// Basic auth in requestSpec
RequestSpecification basicSpec = new RequestSpecBuilder()
  .setBaseUri("https://api.example.com")
  .setAuth(RestAssured.basic("admin", "password"))
  .build();

// Preemptive Basic auth (send without waiting for 401 challenge)
RequestSpecification preemptiveSpec = new RequestSpecBuilder()
  .setBaseUri("https://api.example.com")
  .setAuth(RestAssured.preemptive().basic("admin", "pass"))
  .build();

// OAuth2
RequestSpecification oauth2Spec = new RequestSpecBuilder()
  .setBaseUri("https://api.example.com")
  .setAuth(RestAssured.oauth2(accessToken))
  .build();
```

---

## Q38. How do you send a GET with path parameter?

```java
// Method 1: pathParam() — recommended
given()
  .pathParam("userId", 42)
  .when()
  .get("/api/users/{userId}")
  .then()
  .statusCode(200)
  .body("id", equalTo(42));

// Method 2: inline in URL
given()
  .when()
  .get("/api/users/42")
  .then()
  .statusCode(200);

// Multiple path params
given()
  .pathParam("orderId", 99)
  .pathParam("itemId",  5)
  .when()
  .get("/api/orders/{orderId}/items/{itemId}")
  .then()
  .statusCode(200);
```

---

## Q39. How do you send a GET with query parameter?

```java
// Single query param
given()
  .queryParam("page", 2)
  .when()
  .get("/api/users")
  .then()
  .statusCode(200)
  .body("page", equalTo(2));

// Multiple query params
given()
  .queryParam("role",   "admin")
  .queryParam("active", true)
  .queryParam("page",   1)
  .queryParam("limit",  20)
  .when()
  .get("/api/users")
  .then()
  .statusCode(200);
// Sends: GET /api/users?role=admin&active=true&page=1&limit=20

// Using Map
Map<String, Object> params = new HashMap<>();
params.put("sort",  "createdAt");
params.put("order", "desc");
given()
  .queryParams(params)
  .when()
  .get("/api/orders");
```

---

## Q40. How do you send a POST with JSON body (String)?

```java
// Method 1: String body
String requestBody = """
    {
        "name": "Alice Smith",
        "email": "alice@test.com",
        "role": "USER",
        "password": "SecurePass123!"
    }
    """;

given()
  .contentType("application/json")
  .body(requestBody)
.when()
  .post("/api/users")
.then()
  .statusCode(201)
  .body("id",    notNullValue())
  .body("name",  equalTo("Alice Smith"))
  .body("email", equalTo("alice@test.com"));

// Method 2: org.json
import org.json.JSONObject;

JSONObject body = new JSONObject();
body.put("name",  "Alice Smith");
body.put("email", "alice@test.com");
body.put("role",  "USER");

given()
  .contentType("application/json")
  .body(body.toString())
  .when().post("/api/users");
```

---

## Q41. How do you send a POST with Java Map?

```java
import java.util.HashMap;
import java.util.Map;

// Map is auto-serialized to JSON by RestAssured
Map<String, Object> requestBody = new HashMap<>();
requestBody.put("name",  "Bob Jones");
requestBody.put("email", "bob@test.com");
requestBody.put("age",   30);
requestBody.put("active", true);

given()
  .contentType(ContentType.JSON)
  .body(requestBody)
.when()
  .post("/api/users")
.then()
  .statusCode(201)
  .body("name", equalTo("Bob Jones"));

// Nested map
Map<String, Object> address = new HashMap<>();
address.put("street", "123 Main St");
address.put("city",   "Colombo");

Map<String, Object> user = new HashMap<>();
user.put("name",    "Alice");
user.put("address", address);

given()
  .contentType(ContentType.JSON)
  .body(user)
  .when().post("/api/users");
```

---

## Q42. How do you send a multipart file upload?

```java
// Single file
given()
  .multiPart("file", new File("src/test/resources/testdata/report.csv"))
  .when()
  .post("/api/upload")
  .then()
  .statusCode(200)
  .body("filename", equalTo("report.csv"))
  .body("size", greaterThan(0));

// File + additional fields
given()
  .multiPart("file",        new File("profile.jpg"), "image/jpeg")
  .multiPart("userId",      "123")
  .multiPart("description", "Profile photo")
  .when()
  .post("/api/users/123/avatar")
  .then()
  .statusCode(200)
  .body("url", notNullValue());

// Multiple files
given()
  .multiPart("files", new File("doc1.pdf"))
  .multiPart("files", new File("doc2.pdf"))
  .when()
  .post("/api/documents/batch")
  .then()
  .statusCode(201)
  .body("count", equalTo(2));
```

---

## Q43. How do you assert status code?

```java
// Exact match
.then().statusCode(200);
.then().statusCode(201);
.then().statusCode(204);

// Range assertion (using Hamcrest)
import static org.hamcrest.Matchers.*;
.then().statusCode(allOf(greaterThanOrEqualTo(200), lessThan(300)));

// Check status line
.then().statusLine("HTTP/1.1 200 OK");

// Extract and assert manually
int status = given().when().get("/api/users").getStatusCode();
assertEquals(200, status);

// Using response object
Response response = given().when().get("/api/users");
assertEquals(200, response.getStatusCode());
assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
```

---

## Q44. How do you assert body fields?

```java
import static org.hamcrest.Matchers.*;

given().when().get("/api/users/1")
.then()
  // Exact value
  .body("id",    equalTo(1))
  .body("name",  equalTo("Alice"))
  .body("email", equalTo("alice@test.com"))

  // Contains/starts with
  .body("name",  containsString("Ali"))
  .body("email", endsWith("@test.com"))

  // Boolean
  .body("active", equalTo(true))

  // Null check
  .body("deletedAt", nullValue())
  .body("id",        notNullValue())

  // Number comparisons
  .body("age",   greaterThan(18))
  .body("score", lessThanOrEqualTo(100.0f))

  // Type check
  .body("id",    instanceOf(Integer.class))
  .body("name",  instanceOf(String.class));
```

---

## Q45. How do you assert arrays in RestAssured?

```java
given().when().get("/api/users")
.then()
  .statusCode(200)

  // Array size
  .body("data",        hasSize(6))
  .body("data.size()", equalTo(6))

  // Array contains items
  .body("data.name",   hasItems("Alice", "Bob"))
  .body("data.id",     hasItems(1, 2, 3))

  // Every item satisfies condition
  .body("data.active", everyItem(equalTo(true)))
  .body("data.age",    everyItem(greaterThan(0)))

  // Array not empty
  .body("data",        not(empty()))

  // Specific index
  .body("data[0].name",  equalTo("Alice"))
  .body("data[0].role",  equalTo("ADMIN"))
  .body("data[-1].name", equalTo("Last User"));  // last element
```

---

## Q46. How do you assert nested JSON fields?

```java
// JSON: { "user": { "address": { "city": "Colombo", "zip": "00100" } } }
given().when().get("/api/users/1")
.then()
  .body("user.address.city", equalTo("Colombo"))
  .body("user.address.zip",  equalTo("00100"));

// Nested array: { "orders": [ { "items": [ { "sku": "ABC" } ] } ] }
given().when().get("/api/users/1/orders")
.then()
  .body("orders[0].items[0].sku", equalTo("ABC"))
  .body("orders[0].total",        greaterThan(0.0f));

// Dynamic depth with find
given().when().get("/api/products")
.then()
  .body("products.find { it.category == 'Electronics' }.name",
        equalTo("Laptop"));
```

---

## Q47. How do you assert every item in array?

```java
// Every item in array has expected structure
given().when().get("/api/users")
.then()
  .statusCode(200)
  // Every user has a non-null id
  .body("data.id",     everyItem(notNullValue()))
  // Every email contains @
  .body("data.email",  everyItem(containsString("@")))
  // Every age is positive
  .body("data.age",    everyItem(greaterThan(0)))
  // Every role is valid
  .body("data.role",   everyItem(isIn(Arrays.asList("ADMIN","USER","VIEWER"))));

// Extract list and loop (when Hamcrest matchers aren't enough)
List<Map<String, Object>> users =
  given().when().get("/api/users").jsonPath().getList("data");

for (Map<String, Object> user : users) {
  assertNotNull(user.get("id"));
  assertTrue(user.get("email").toString().contains("@"));
  assertTrue((Integer) user.get("age") > 0);
}
```

---

## Q48. How do you assert response headers?

```java
given().when().get("/api/users")
.then()
  // Exact match
  .header("Content-Type", "application/json; charset=utf-8")

  // Contains (Hamcrest)
  .header("Content-Type",  containsString("application/json"))

  // Not null
  .header("X-Request-ID",  notNullValue())

  // Specific value patterns
  .header("Cache-Control", containsString("no-cache"))
  .header("Location",      containsString("/api/users/"))

  // Multiple headers
  .headers("Content-Type",  containsString("json"),
           "X-API-Version", equalTo("2.0"));

// Extract and assert manually
String contentType = response.getHeader("Content-Type");
assertTrue(contentType.contains("application/json"));

// Assert header exists
assertTrue(response.getHeaders().hasHeaderWithName("X-Request-ID"));
```

---

## Q49. How do you assert response time?

```java
import static org.hamcrest.Matchers.*;

// Response within 2 seconds
given().when().get("/api/users")
.then()
  .statusCode(200)
  .time(lessThan(2000L));  // milliseconds

// Response within 500ms (for SLA testing)
given().when().get("/api/health")
.then()
  .time(lessThan(500L));

// Extract and log response time
Response response = given().when().get("/api/users");
long responseTime = response.getTime();
System.out.println("Response time: " + responseTime + "ms");
assertTrue(responseTime < 3000, "Response took too long: " + responseTime + "ms");

// Using TimeUnit
.time(lessThan(2L), TimeUnit.SECONDS);
```

---

## Q50. How do you extract a value from response?

```java
// Extract single value
String name    = given().when().get("/api/users/1")
                        .then().extract().path("name");

int id         = given().when().get("/api/users/1")
                        .then().extract().path("id");

String token   = given().when().post("/api/login")
                        .then().extract().path("data.token");

// Extract from array
String first   = given().when().get("/api/users")
                        .then().extract().path("data[0].name");

List<String> allNames = given().when().get("/api/users")
                               .then().extract().path("data.name");

// Extract header
String location = given().when().post("/api/users")
                         .then().statusCode(201)
                         .extract().header("Location");

// Extract the full response body as String
String json = given().when().get("/api/users/1")
                     .then().extract().body().asString();

// Extract and parse manually
JsonPath jsonPath = given().when().get("/api/users/1")
                           .then().extract().jsonPath();
String city = jsonPath.getString("address.city");
```

---

## Q51. How do you extract full response object?

```java
import io.restassured.response.Response;

// Store full response
Response response = given()
  .contentType("application/json")
  .body("{\"name\": \"Alice\"}")
.when()
  .post("/api/users");

// Status
int statusCode    = response.getStatusCode();
String statusLine = response.getStatusLine();

// Headers
String contentType = response.getHeader("Content-Type");
Headers headers    = response.getHeaders();

// Body
String bodyString  = response.getBody().asString();
int userId         = response.jsonPath().getInt("id");
String userName    = response.jsonPath().getString("name");

// Time
long responseTime  = response.getTime();

// Log everything
response.prettyPrint();

// Then assert
assertEquals(201, statusCode);
assertNotNull(userId);
```

---

## Q52. How do you do JSON Schema validation?

```java
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import java.io.File;

// From classpath (recommended — file in src/test/resources/)
given().when().get("/api/users/1")
.then()
  .statusCode(200)
  .body(matchesJsonSchemaInClasspath("schemas/user.json"));

// From File
given().when().get("/api/users")
.then()
  .body(matchesJsonSchema(new File("src/test/resources/schemas/users-list.json")));

// From String
String schema = """
  {
    "type": "object",
    "required": ["id", "name"],
    "properties": {
      "id":   { "type": "integer" },
      "name": { "type": "string" }
    }
  }
  """;
given().when().get("/api/users/1")
.then().body(matchesJsonSchema(schema));
```

---

## Q53. How do you use soft assertions with RestAssured?

```java
import org.testng.asserts.SoftAssert;

@Test
public void testUserProfileSoftAssert() {
  Response response = given()
    .header("Authorization", "Bearer " + token)
  .when()
    .get("/api/users/1");

  SoftAssert soft = new SoftAssert();

  // Status
  soft.assertEquals(response.getStatusCode(), 200, "Status code");

  // Body fields
  JsonPath json = response.jsonPath();
  soft.assertEquals(json.getString("name"),   "Alice",           "Name");
  soft.assertEquals(json.getString("email"),  "alice@test.com",  "Email");
  soft.assertEquals(json.getString("role"),   "ADMIN",           "Role");
  soft.assertTrue(json.getBoolean("active"),                     "Active flag");

  // Header
  soft.assertTrue(
    response.getHeader("Content-Type").contains("application/json"),
    "Content-Type header"
  );

  // Response time
  soft.assertTrue(response.getTime() < 3000,
    "Response time was: " + response.getTime() + "ms");

  // Assert all — throws if any failed, showing ALL failures
  soft.assertAll();
}
```

---

## Q54. How do you chain POST → GET → DELETE?

```java
@Test
public void testUserCRUD() {
  // Step 1: POST — Create user
  String requestBody = """
    { "name": "Test User", "email": "testuser@example.com", "role": "USER" }
    """;

  int userId =
    given()
      .spec(requestSpec)
      .body(requestBody)
    .when()
      .post("/users")
    .then()
      .statusCode(201)
      .body("name", equalTo("Test User"))
      .extract().path("id");

  System.out.println("Created user ID: " + userId);

  // Step 2: GET — Verify user exists
  given()
    .spec(requestSpec)
  .when()
    .get("/users/" + userId)
  .then()
    .statusCode(200)
    .body("id",   equalTo(userId))
    .body("name", equalTo("Test User"))
    .body("email", equalTo("testuser@example.com"));

  // Step 3: PUT — Update user
  given()
    .spec(requestSpec)
    .body("{ \"name\": \"Updated Name\", \"email\": \"testuser@example.com\" }")
  .when()
    .put("/users/" + userId)
  .then()
    .statusCode(200)
    .body("name", equalTo("Updated Name"));

  // Step 4: DELETE — Remove user
  given()
    .spec(requestSpec)
  .when()
    .delete("/users/" + userId)
  .then()
    .statusCode(204);

  // Step 5: GET — Verify deleted
  given()
    .spec(requestSpec)
  .when()
    .get("/users/" + userId)
  .then()
    .statusCode(404);
}
```

---

## Q55. How do you use @DataProvider with RestAssured?

```java
@DataProvider(name = "userRoles")
public Object[][] userRoleData() {
  return new Object[][] {
    { "admin@test.com",  "Admin123!", 200, "ADMIN"  },
    { "user@test.com",   "User456!",  200, "USER"   },
    { "viewer@test.com", "View789!",  200, "VIEWER" }
  };
}

@Test(dataProvider = "userRoles")
public void testRoleBasedAccess(String email, String password,
                                 int expectedStatus, String expectedRole) {
  // Step 1: Login and get token
  String token =
    given()
      .body(String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password))
      .contentType("application/json")
    .when()
      .post("/api/auth/login")
    .then()
      .statusCode(200)
      .extract().path("token");

  // Step 2: Access profile and verify role
  given()
    .header("Authorization", "Bearer " + token)
  .when()
    .get("/api/profile")
  .then()
    .statusCode(expectedStatus)
    .body("role", equalTo(expectedRole));
}
```

---

## Q56. What is enableLoggingOfRequestAndResponseIfValidationFails()?

Enables automatic request and response logging ONLY when the test fails — keeps passing test output clean.

```java
import static io.restassured.RestAssured.*;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

// Global setup — logs only on failure
@BeforeClass
public static void setup() {
  RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
}

// This test: if it passes → no logging output
// If it fails → full request + response printed to console
@Test
public void testGetUser() {
  given()
  .when()
    .get("/api/users/1")
  .then()
    .statusCode(200)
    .body("name", equalTo("Alice"));
}

// Manual equivalent per-request
given()
  .log().ifValidationFails()  // log request if fails
.when()
  .get("/api/users/1")
.then()
  .log().ifValidationFails()  // log response if fails
  .statusCode(200);
```

---

## Q57. What are all logging options in RestAssured?

```java
// Request logging
given().log().all()        // log everything
given().log().uri()        // just the URL
given().log().headers()    // just headers
given().log().body()       // just body
given().log().params()     // just parameters
given().log().cookies()    // just cookies
given().log().ifValidationFails()  // only on failure

// Response logging
.then().log().all()        // log everything
.then().log().body()       // just response body
.then().log().headers()    // just headers
.then().log().status()     // just status code
.then().log().cookies()    // just cookies
.then().log().ifError()    // only on 4xx/5xx
.then().log().ifValidationFails()  // only when assertion fails

// Global logging via filters
RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

// Log to file
PrintStream logStream = new PrintStream(new File("test-output/api.log"));
given().filter(new RequestLoggingFilter(logStream))
       .filter(new ResponseLoggingFilter(logStream));

// Allure integration (log to Allure report)
RestAssured.filters(new AllureRestAssured());
```

---

## Q58. How to test rate limiting (429)?

```java
@Test
public void testRateLimitEnforced() throws InterruptedException {
  String endpoint = "/api/search?q=test";

  // Fire requests until rate limit hit
  int rateLimitHit = -1;
  for (int i = 1; i <= 20; i++) {
    int status = given().when().get(endpoint).getStatusCode();
    if (status == 429) {
      rateLimitHit = i;
      break;
    }
  }

  assertTrue(rateLimitHit > 0, "Rate limit should be enforced");
  System.out.println("Rate limit hit after " + rateLimitHit + " requests");
}

@Test
public void testRateLimitResponse() {
  // Simulate being rate limited (if you can trigger it)
  Response response = given().when().get("/api/search?q=test");

  if (response.getStatusCode() == 429) {
    // Verify Retry-After header is present
    assertNotNull(response.getHeader("Retry-After"));
    int retryAfter = Integer.parseInt(response.getHeader("Retry-After"));
    assertTrue(retryAfter > 0, "Retry-After should be positive seconds");

    // Verify error message
    assertEquals("Rate limit exceeded", response.jsonPath().getString("message"));
  }
}
```

---

## Q59. How to test authentication failures (401, 403)?

```java
@Test
public void test401_NoToken() {
  given()
    .when().get("/api/profile")  // no Authorization header
    .then()
    .statusCode(401)
    .body("message", equalTo("Authentication required"))
    .body("code",    equalTo("UNAUTHORIZED"));
}

@Test
public void test401_InvalidToken() {
  given()
    .header("Authorization", "Bearer invalid-token-xyz")
    .when().get("/api/profile")
    .then()
    .statusCode(401)
    .body("message", containsString("Invalid token"));
}

@Test
public void test401_ExpiredToken() {
  String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjF9.EXPIRED";
  given()
    .header("Authorization", "Bearer " + expiredToken)
    .when().get("/api/profile")
    .then()
    .statusCode(401)
    .body("message", containsString("Token expired"));
}

@Test
public void test403_InsufficientPermission() {
  // Logged in as regular user — trying admin endpoint
  given()
    .header("Authorization", "Bearer " + userToken)
    .when().get("/api/admin/users")
    .then()
    .statusCode(403)
    .body("message", containsString("Forbidden"))
    .body("requiredRole", equalTo("ADMIN"));
}
```

---

## Q60. How to test validation errors (400, 422)?

```java
@Test
public void test400_MissingRequiredField() {
  // Missing "email" (required)
  given()
    .contentType("application/json")
    .body("{\"name\": \"Alice\"}")
    .when().post("/api/users")
    .then()
    .statusCode(400)
    .body("errors",          hasSize(greaterThan(0)))
    .body("errors[0].field", equalTo("email"))
    .body("errors[0].message", containsString("required"));
}

@Test
public void test400_InvalidEmailFormat() {
  given()
    .contentType("application/json")
    .body("{\"name\": \"Alice\", \"email\": \"not-an-email\"}")
    .when().post("/api/users")
    .then()
    .statusCode(400)
    .body("errors.find { it.field == 'email' }.message",
          containsString("valid email"));
}

@Test
public void test422_BusinessRuleViolation() {
  // End date before start date — valid JSON but invalid business logic
  given()
    .contentType("application/json")
    .body("{\"startDate\":\"2025-12-31\",\"endDate\":\"2025-01-01\"}")
    .when().post("/api/bookings")
    .then()
    .statusCode(422)
    .body("message", containsString("end date must be after start date"));
}

@Test
public void test400_EmptyBody() {
  given()
    .contentType("application/json")
    .body("{}")
    .when().post("/api/users")
    .then()
    .statusCode(400);
}
```

---

## Q61. How to test 404 for non-existent resources?

```java
@Test
public void test404_UserNotFound() {
  given()
    .when().get("/api/users/99999")
    .then()
    .statusCode(404)
    .body("message",    equalTo("User not found"))
    .body("resourceId", equalTo(99999));
}

@Test
public void test404_DeleteNonExistent() {
  given()
    .when().delete("/api/users/99999")
    .then()
    .statusCode(404);
}

@Test
public void test404_UpdateNonExistent() {
  given()
    .contentType("application/json")
    .body("{\"name\": \"Ghost\"}")
    .when().put("/api/users/99999")
    .then()
    .statusCode(404);
}
```

---

## Q62. How to do negative testing for APIs?

Negative testing verifies the API rejects bad input gracefully.

```java
public class NegativeTests {

  @DataProvider(name = "invalidUsers")
  public Object[][] invalidUserData() {
    return new Object[][] {
      // email,             name,    expectedStatus, expectedError
      { "not-email",       "Alice", 400, "email" },
      { "",                "Alice", 400, "email" },
      { "a@b.com",         "",      400, "name"  },
      { "a@b.com",         "A",     400, "name"  }, // too short
      { "a@b.com",         "Alice", 201, null    }, // valid - control case
    };
  }

  @Test(dataProvider = "invalidUsers")
  public void testCreateUserValidation(String email, String name,
                                       int expectedStatus, String expectedErrorField) {
    Response response =
      given()
        .contentType("application/json")
        .body(String.format("{\"email\":\"%s\",\"name\":\"%s\"}", email, name))
      .when()
        .post("/api/users");

    assertEquals(response.getStatusCode(), expectedStatus);

    if (expectedErrorField != null) {
      List<String> errorFields = response.jsonPath().getList("errors.field");
      assertTrue(errorFields.contains(expectedErrorField),
                 "Expected error on field: " + expectedErrorField);
    }
  }
}
```

---

## Q63. How to test SQL injection in API inputs?

```java
@Test
public void testSQLInjectionInSearchParam() {
  String[] sqlPayloads = {
    "' OR '1'='1",
    "'; DROP TABLE users; --",
    "1 UNION SELECT * FROM users",
    "admin'--",
    "\" OR \"\"=\""
  };

  for (String payload : sqlPayloads) {
    Response response =
      given()
        .queryParam("name", payload)
        .when().get("/api/users/search");

    // Should NOT return 500 (which would indicate SQL error leaked)
    assertNotEquals(500, response.getStatusCode(),
                    "SQL injection may have caused server error for: " + payload);

    // Should return 200 (empty result) or 400 (validation) — not 500
    assertTrue(
      response.getStatusCode() == 200 || response.getStatusCode() == 400,
      "Unexpected status " + response.getStatusCode() + " for payload: " + payload
    );

    // Response should not contain SQL error messages
    String body = response.getBody().asString().toLowerCase();
    assertFalse(body.contains("sql"), "SQL error leaked for: " + payload);
    assertFalse(body.contains("syntax error"), "SQL error leaked for: " + payload);
  }
}
```

---

## Q64. How to test XSS in API inputs?

```java
@Test
public void testXSSInUserName() {
  String[] xssPayloads = {
    "<script>alert('XSS')</script>",
    "<img src=x onerror=alert(1)>",
    "javascript:alert('XSS')",
    "<svg onload=alert(1)>",
    "';alert('XSS');//"
  };

  for (String payload : xssPayloads) {
    // Test: API should sanitize or reject XSS input
    Response createResp =
      given()
        .contentType("application/json")
        .body(String.format("{\"name\":\"%s\",\"email\":\"test@test.com\"}", payload))
      .when()
        .post("/api/users");

    // Either reject (400) or store safely (201 with sanitized output)
    if (createResp.getStatusCode() == 201) {
      int userId = createResp.jsonPath().getInt("id");

      // Verify stored value does NOT contain raw script tags
      String storedName = given().when().get("/api/users/" + userId)
                                 .jsonPath().getString("name");

      assertFalse(storedName.contains("<script>"),
                  "XSS payload was stored unescaped: " + storedName);

      // Cleanup
      given().when().delete("/api/users/" + userId);
    }
  }
}
```

---

## Q65. What is API performance testing? How to measure response time?

```java
// Response time assertion per test
@Test
public void testGetUsersPerformance() {
  given()
    .when().get("/api/users")
    .then()
    .statusCode(200)
    .time(lessThan(1000L)); // must respond within 1 second
}

// Measure across multiple calls
@Test
public void testAverageResponseTime() throws Exception {
  int iterations = 10;
  long totalTime = 0;

  for (int i = 0; i < iterations; i++) {
    Response response = given().when().get("/api/users");
    totalTime += response.getTime();
  }

  long average = totalTime / iterations;
  System.out.println("Average response time over " + iterations + " calls: " + average + "ms");
  assertTrue(average < 500, "Average response time too high: " + average + "ms");
}

// For load/performance testing — use JMeter or Gatling
// JMeter: set thread group with 100 users, 10 second ramp-up
// Gatling: write Scala simulations for HTTP scenarios
// RestAssured: measure individual request timing — not load testing
```

---

## Q66. What is contract testing vs functional API testing?

| Aspect | Contract Testing | Functional API Testing |
|---|---|---|
| What | Verifies API structure/schema | Verifies API behaviour/logic |
| Who | Between teams (consumer + provider) | Within QA team |
| Tools | Pact, JSON Schema | RestAssured, Postman |
| Focus | Response shape, data types, required fields | Business logic, status codes, data values |
| When | During integration / CI | During sprint / regression |
| Example | "Does user response always have an `id` field?" | "Does POST /users return 201 with the created user's data?" |

**Contract test example:**
```java
// Checks schema — not specific values
given().when().get("/api/users/1")
.then().body(matchesJsonSchemaInClasspath("schemas/user.json"));
```

**Functional test example:**
```java
// Checks specific business logic
given().body("{\"email\":\"existing@test.com\"}").when().post("/api/register")
.then().statusCode(409).body("error", equalTo("Email already registered"));
```

---

## Q67. What is the difference between Postman and RestAssured?

| Feature | Postman | RestAssured |
|---|---|---|
| Language | JavaScript (tests) | Java |
| Interface | GUI | Code |
| Learning curve | Low | Medium (Java knowledge needed) |
| CI integration | Via Newman | Via Maven/Gradle |
| Code reuse | Collections, scripts | Java classes, inheritance, utilities |
| Version control | JSON export | Native (Java files in git) |
| Parameterisation | Environments, CSV runner | @DataProvider, Java code |
| Assertions | pm.expect (Chai) | Hamcrest matchers |
| Auth flows | Built-in UI | auth().basic/oauth2/bearer |
| Schema validation | Built-in (limited) | JSON Schema Validator |
| Reports | Newman HTML, Allure | TestNG/Allure/ExtentReports |
| Exploratory testing | Excellent | Not applicable |
| Team collaboration | Workspaces | Git |

---

## Q68. What is a mock API? When do you use it?

A **mock API** is a simulated server that returns pre-configured responses without real business logic or database.

**When to use:**
1. Backend not ready yet — frontend/QA can test against mock.
2. External third-party APIs (payment gateways, SMS) — avoid real costs/side effects.
3. Simulating error conditions difficult to reproduce (500, timeouts, 429).
4. Isolating the system under test from dependencies.
5. Speed — mock responses are instant.

**Tools:**
- **WireMock** — Java, most powerful
- **Postman Mock Servers** — quick GUI setup
- **json-server** — Node.js, mock from JSON file
- **Playwright `page.route()`** — browser-level mocking

---

## Q69. What is WireMock?

WireMock is a Java-based HTTP mock server. Define stubs (what request to match → what response to return).

```xml
<!-- pom.xml -->
<dependency>
  <groupId>com.github.tomakehurst</groupId>
  <artifactId>wiremock-jre8</artifactId>
  <version>2.35.0</version>
  <scope>test</scope>
</dependency>
```

```java
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockTest {

  static WireMockServer wireMock;

  @BeforeClass
  public static void startServer() {
    wireMock = new WireMockServer(8089);
    wireMock.start();
    configureFor("localhost", 8089);
  }

  @Test
  public void testMockedEndpoint() {
    // Stub: GET /api/users/1 → 200 with JSON
    stubFor(get(urlEqualTo("/api/users/1"))
      .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"id\": 1, \"name\": \"Alice\"}")));

    // Test against the mock
    given()
      .baseUri("http://localhost:8089")
    .when()
      .get("/api/users/1")
    .then()
      .statusCode(200)
      .body("name", equalTo("Alice"));
  }

  @Test
  public void testMockedError() {
    stubFor(get(urlEqualTo("/api/users/99"))
      .willReturn(aResponse()
        .withStatus(404)
        .withBody("{\"error\": \"Not found\"}")));

    given().baseUri("http://localhost:8089")
           .when().get("/api/users/99")
           .then().statusCode(404);
  }

  @AfterClass
  public static void stopServer() { wireMock.stop(); }
}
```

---

## Q70. What is GraphQL? How is testing different from REST?

**GraphQL** is a query language for APIs where the client specifies exactly what data it needs. All requests go to a single endpoint (`POST /graphql`).

**Key differences:**

| Feature | REST | GraphQL |
|---|---|---|
| Endpoints | Multiple (`/users`, `/orders`) | Single (`/graphql`) |
| HTTP method | GET/POST/PUT/DELETE | POST only (mostly) |
| Over-fetching | Common | Never — client picks fields |
| Under-fetching | Requires multiple calls | One query for nested data |
| Status codes | 200, 201, 404, 400... | Always 200 (errors in body) |
| Versioning | `/v1`, `/v2` | Schema evolves, no versions |

**Testing GraphQL with RestAssured:**
```java
@Test
public void testGraphQLQuery() {
  String query = """
    {
      "query": "{ user(id: 1) { id name email role } }"
    }
    """;

  given()
    .contentType("application/json")
    .body(query)
  .when()
    .post("/graphql")
  .then()
    .statusCode(200)  // GraphQL always returns 200
    .body("data.user.id",   equalTo(1))
    .body("data.user.name", equalTo("Alice"))
    .body("errors",         nullValue());  // Check no GraphQL errors
}

@Test
public void testGraphQLMutation() {
  String mutation = """
    {
      "query": "mutation { createUser(name: \\"Bob\\", email: \\"bob@test.com\\") { id name } }"
    }
    """;

  given()
    .contentType("application/json")
    .body(mutation)
  .when()
    .post("/graphql")
  .then()
    .statusCode(200)
    .body("data.createUser.id",   notNullValue())
    .body("data.createUser.name", equalTo("Bob"))
    .body("errors",               nullValue());
}
```

---

## Q71. What is a WebSocket API?

**WebSocket** is a persistent, bidirectional communication channel between client and server over a single TCP connection.

```
REST:   Client → Request → Server → Response (then connection closes)
WS:     Client ←→ Server (connection stays open, both sides send anytime)
```

**Use cases:** Chat apps, live dashboards, real-time notifications, multiplayer games.

**Testing WebSocket in Java:**
```java
import javax.websocket.*;
import java.net.URI;
import java.util.concurrent.*;

@ClientEndpoint
public class WebSocketTest {
  private static CountDownLatch messageLatch = new CountDownLatch(1);
  private static String receivedMessage;

  @OnMessage
  public void onMessage(String message) {
    receivedMessage = message;
    messageLatch.countDown();
  }

  @Test
  public void testWebSocketMessage() throws Exception {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    Session session = container.connectToServer(
      WebSocketTest.class, URI.create("ws://localhost:8080/ws/notifications")
    );

    // Send message
    session.getBasicRemote().sendText("{\"action\": \"subscribe\", \"topic\": \"orders\"}");

    // Wait for response (max 5 seconds)
    messageLatch.await(5, TimeUnit.SECONDS);

    assertNotNull(receivedMessage);
    assertTrue(receivedMessage.contains("subscribed"));
    session.close();
  }
}
```

---

## Q72. What is Swagger/OpenAPI? How do you use it for testing?

**Swagger/OpenAPI** is a specification for describing REST APIs in a machine-readable format (YAML/JSON). Includes: endpoints, methods, request/response schemas, auth requirements.

**How to use for testing:**
1. Read the spec to understand what to test.
2. Generate test skeletons from spec.
3. Validate responses against spec schemas.
4. Ensure API matches spec (contract testing).

```java
// Validate response against OpenAPI spec using rest-assured + swagger-request-validator
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;

public class SwaggerValidationTest {

  static OpenApiValidationFilter validationFilter =
    new OpenApiValidationFilter("https://api.example.com/openapi.json");

  @Test
  public void testGetUserMatchesSpec() {
    given()
      .filter(validationFilter)  // validates request AND response against spec
    .when()
      .get("/api/users/1")
    .then()
      .statusCode(200);
    // Fails if response doesn't match the schema defined in OpenAPI spec
  }
}
```

**Manual use:** Read Swagger UI (`/swagger-ui.html`) to discover endpoints, expected params, and schema before writing tests.

---

## Q73. How do you test pagination in APIs?

```java
@Test
public void testPaginationFirstPage() {
  given()
    .queryParam("page",  1)
    .queryParam("limit", 10)
  .when()
    .get("/api/users")
  .then()
    .statusCode(200)
    .body("page",       equalTo(1))
    .body("limit",      equalTo(10))
    .body("data",       hasSize(10))
    .body("total",      greaterThan(10))
    .body("totalPages", greaterThan(1));
}

@Test
public void testPaginationLastPage() {
  // Get total first
  int total = given().queryParam("page", 1).queryParam("limit", 10)
                     .when().get("/api/users")
                     .jsonPath().getInt("total");
  int lastPage = (int) Math.ceil(total / 10.0);

  given()
    .queryParam("page",  lastPage)
    .queryParam("limit", 10)
  .when()
    .get("/api/users")
  .then()
    .statusCode(200)
    .body("page", equalTo(lastPage))
    .body("data", hasSize(lessThanOrEqualTo(10)));
}

@Test
public void testPaginationBeyondLastPage() {
  given()
    .queryParam("page",  9999)
    .queryParam("limit", 10)
  .when()
    .get("/api/users")
  .then()
    .statusCode(200)
    .body("data", hasSize(0));
  // OR: .statusCode(404); depends on API design
}

@Test
public void testPaginationNoDuplicates() {
  List<Integer> page1Ids = given().queryParam("page", 1).queryParam("limit", 5)
    .when().get("/api/users").jsonPath().getList("data.id");

  List<Integer> page2Ids = given().queryParam("page", 2).queryParam("limit", 5)
    .when().get("/api/users").jsonPath().getList("data.id");

  // No overlaps between pages
  page1Ids.retainAll(page2Ids);
  assertTrue(page1Ids.isEmpty(), "Duplicate IDs found across pages");
}
```

---

## Q74. How do you test file upload APIs?

```java
@Test
public void testFileUploadSuccess() {
  File testFile = new File("src/test/resources/testdata/sample.csv");
  assertTrue(testFile.exists(), "Test file must exist");

  given()
    .header("Authorization", "Bearer " + token)
    .multiPart("file", testFile, "text/csv")
  .when()
    .post("/api/files/upload")
  .then()
    .statusCode(200)
    .body("fileName",    equalTo("sample.csv"))
    .body("size",        equalTo((int) testFile.length()))
    .body("downloadUrl", notNullValue())
    .body("downloadUrl", startsWith("https://"));
}

@Test
public void testUploadWrongFileType() {
  File exeFile = new File("src/test/resources/testdata/malware.exe");
  given()
    .header("Authorization", "Bearer " + token)
    .multiPart("file", exeFile, "application/octet-stream")
  .when()
    .post("/api/files/upload")
  .then()
    .statusCode(415)
    .body("error", containsString("File type not allowed"));
}

@Test
public void testUploadFileTooLarge() {
  File largeFile = new File("src/test/resources/testdata/50mb-file.bin");
  given()
    .header("Authorization", "Bearer " + token)
    .multiPart("file", largeFile)
  .when()
    .post("/api/files/upload")
  .then()
    .statusCode(413)
    .body("error", containsString("File size exceeds"));
}
```

---

## Q75. How do you handle authentication tokens that expire during test run?

```java
public class TokenManager {

  private static String token;
  private static long tokenExpiry;

  public static synchronized String getToken() {
    // If token is null or expires within next 30 seconds — refresh
    if (token == null || System.currentTimeMillis() > (tokenExpiry - 30_000)) {
      refreshToken();
    }
    return token;
  }

  private static void refreshToken() {
    Response response =
      given()
        .body("{\"email\":\"test@co.com\",\"password\":\"pass123\"}")
        .contentType("application/json")
      .when()
        .post("/api/auth/login");

    token      = response.jsonPath().getString("token");
    int expiresIn = response.jsonPath().getInt("expiresIn"); // seconds
    tokenExpiry   = System.currentTimeMillis() + (expiresIn * 1000L);

    System.out.println("Token refreshed. Expires in: " + expiresIn + "s");
  }
}

// In tests — always use TokenManager.getToken()
@Test
public void testProtectedEndpoint() {
  given()
    .header("Authorization", "Bearer " + TokenManager.getToken())
  .when()
    .get("/api/profile")
  .then()
    .statusCode(200);
}
```

---

## Q76. What is the difference between API testing and UI testing?

| Aspect | API Testing | UI Testing |
|---|---|---|
| What is tested | Business logic, data layer | User interface, user flows |
| Speed | Fast (no browser) | Slow (browser rendering) |
| Stability | Very stable | More flaky (CSS changes) |
| Tools | RestAssured, Postman, Playwright `request` | Selenium, Playwright, Cypress |
| Access | Direct data access | Through user interface only |
| Assertions | JSON fields, status codes | Element visibility, text content |
| When to run | Every commit (fast) | Regression, release |
| Coverage | Contract, negative, security, performance | User journeys, accessibility |
| Without UI | Can test before UI exists | Requires UI |
| Defect detection | Early (lower cost to fix) | Late (higher cost) |

**Testing pyramid:**
```
         /  UI  \        (few, slow, expensive)
        / ------  \
       / Integration\    (some)
      / (API tests)  \
     / -------------- \
    /   Unit Tests     \  (many, fast, cheap)
   /--------------------\
```

---

## Q77. What is API versioning? How do you test it?

**API versioning** lets you evolve the API without breaking existing clients.

**Common versioning strategies:**

```
// 1. URL path versioning (most common)
GET /api/v1/users
GET /api/v2/users  (new schema)

// 2. Header versioning
GET /api/users
Accept-Version: 2.0

// 3. Query parameter versioning
GET /api/users?version=2

// 4. Content-Type versioning
Accept: application/vnd.myapi.v2+json
```

**Testing versioning:**
```java
@Test
public void testV1AndV2ReturnDifferentSchema() {
  // V1 response: { "name": "Alice" }
  // V2 response: { "firstName": "Alice", "lastName": "Smith" }

  // V1
  given().when().get("/api/v1/users/1")
  .then()
    .statusCode(200)
    .body("name",      notNullValue())
    .body("firstName", nullValue());  // V1 doesn't have firstName

  // V2
  given().when().get("/api/v2/users/1")
  .then()
    .statusCode(200)
    .body("firstName", notNullValue())
    .body("lastName",  notNullValue())
    .body("name",      nullValue());  // V2 doesn't have name
}

@Test
public void testV1StillWorks() {
  // Backward compatibility — old version still responds correctly
  given().when().get("/api/v1/users")
  .then().statusCode(200);
}
```

---

## Q78. Walk me through your API testing strategy for a new API.

**Step-by-step strategy (answer this verbally in interviews):**

**1. Understand the API:**
- Read the OpenAPI/Swagger spec or API documentation.
- Understand business requirements the API implements.
- Identify all endpoints, methods, auth mechanisms.

**2. Happy path testing:**
- Test each endpoint with valid data.
- Verify correct status codes (200, 201, 204).
- Verify response schema and data types.
- Verify CRUD operations work end-to-end.

**3. Negative testing:**
- Missing required fields (400).
- Invalid data formats (400/422).
- Boundary values (empty strings, max length, negative numbers).
- Non-existent resources (404).
- Wrong HTTP methods (405).

**4. Authentication & Authorization:**
- No token → 401.
- Invalid/expired token → 401.
- Valid token but wrong role → 403.
- Each endpoint requires correct role.

**5. Business logic validation:**
- Data persists correctly (POST → GET).
- Updates are reflected (PUT/PATCH → GET).
- Delete removes resource (DELETE → GET → 404).
- Calculations are correct (totals, discounts, dates).

**6. Error handling:**
- 5xx should not leak stack traces.
- Error messages are meaningful.
- Error codes are consistent.

**7. Performance:**
- Response time within SLA.
- Pagination works correctly.
- Large payload handling.

**8. Security:**
- SQL injection inputs handled.
- XSS inputs sanitized.
- Auth tokens expire correctly.

**9. Contract/schema validation:**
- JSON Schema matches for all responses.
- Breaking changes detected.

**10. Automate and integrate:**
- Tests in CI pipeline.
- Reports generated and reviewed.

---

## Q79. Postman vs RestAssured — when do you use each?

**Use Postman when:**
- Exploring a new API for the first time.
- Doing manual/exploratory testing.
- Developers want to test their endpoints quickly.
- Creating API documentation.
- Running quick smoke checks.
- When you need a GUI and don't want to write code.
- Sharing with non-technical team members.

**Use RestAssured when:**
- Building an automated regression suite in Java.
- Integrating API tests with existing TestNG/JUnit framework.
- Complex test scenarios requiring loops, data structures, or Java logic.
- Tests need to chain with UI tests (same Java project).
- Using @DataProvider for data-driven testing.
- Generating Allure/ExtentReports with rich detail.
- CI/CD integration in Maven projects.
- When tests need to be version-controlled as Java code.

**In practice (Kupeshanth's CV context):**
- Cerexio: Postman for manual API exploration and team-shared collections.
- Qoria Lanka: RestAssured for automated regression suites with TestNG.

---

## Q80. Walk through a complete API test for a CRUD operation.

```java
// Complete CRUD test — POST, GET, PUT, PATCH, DELETE
// Using reqres.in as example

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.builder.RequestSpecBuilder;
import org.testng.annotations.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class UserCRUDTest {

  private static RequestSpecification spec;
  private static int createdUserId;

  @BeforeClass
  public static void setup() {
    spec = new RequestSpecBuilder()
      .setBaseUri("https://reqres.in")
      .setBasePath("/api")
      .setContentType("application/json")
      .addHeader("Accept", "application/json")
      .build();

    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @Test(priority = 1, description = "Create a new user")
  public void testCreateUser() {
    String body = """
      {
        "name": "Kupeshanth",
        "job": "QA Engineer"
      }
      """;

    Response response =
      given().spec(spec).body(body)
      .when().post("/users")
      .then()
        .statusCode(201)
        .body("name", equalTo("Kupeshanth"))
        .body("job",  equalTo("QA Engineer"))
        .body("id",   notNullValue())
        .body("createdAt", notNullValue())
        .time(lessThan(3000L))
      .extract().response();

    createdUserId = Integer.parseInt(response.jsonPath().getString("id"));
    System.out.println("Created user ID: " + createdUserId);
  }

  @Test(priority = 2, description = "Get existing user", dependsOnMethods = {"testCreateUser"})
  public void testGetUser() {
    given().spec(spec)
    .when().get("/users/2")  // reqres.in has preset users
    .then()
      .statusCode(200)
      .body("data.id",         equalTo(2))
      .body("data.email",      notNullValue())
      .body("data.first_name", notNullValue())
      .body("data.last_name",  notNullValue())
      .body("data.avatar",     startsWith("https://"));
  }

  @Test(priority = 3, description = "Get list of users — paginated")
  public void testGetAllUsers() {
    given().spec(spec).queryParam("page", 1)
    .when().get("/users")
    .then()
      .statusCode(200)
      .body("page",       equalTo(1))
      .body("per_page",   greaterThan(0))
      .body("total",      greaterThan(0))
      .body("data",       not(empty()))
      .body("data.id",    everyItem(notNullValue()))
      .body("data.email", everyItem(containsString("@")));
  }

  @Test(priority = 4, description = "Full update with PUT")
  public void testUpdateUserPut() {
    String body = """
      {
        "name": "Kupeshanth Updated",
        "job": "Senior QA Engineer"
      }
      """;

    given().spec(spec).body(body)
    .when().put("/users/2")
    .then()
      .statusCode(200)
      .body("name",      equalTo("Kupeshanth Updated"))
      .body("job",       equalTo("Senior QA Engineer"))
      .body("updatedAt", notNullValue());
  }

  @Test(priority = 5, description = "Partial update with PATCH")
  public void testUpdateUserPatch() {
    String body = "{ \"job\": \"Lead QA\" }";

    given().spec(spec).body(body)
    .when().patch("/users/2")
    .then()
      .statusCode(200)
      .body("job",       equalTo("Lead QA"))
      .body("updatedAt", notNullValue());
  }

  @Test(priority = 6, description = "Delete user")
  public void testDeleteUser() {
    given().spec(spec)
    .when().delete("/users/2")
    .then()
      .statusCode(204)
      .body(emptyOrNullString());
  }

  @Test(priority = 7, description = "Get non-existent user — 404")
  public void testGetNonExistentUser() {
    given().spec(spec)
    .when().get("/users/99999")
    .then()
      .statusCode(404)
      .body(equalTo("{}"));
  }

  @Test(priority = 8, description = "Login and get token")
  public void testLogin() {
    String body = """
      {
        "email": "eve.holt@reqres.in",
        "password": "cityslicka"
      }
      """;

    given().spec(spec).body(body)
    .when().post("/login")
    .then()
      .statusCode(200)
      .body("token", notNullValue())
      .body("token", not(emptyString()));
  }

  @Test(priority = 9, description = "Login with missing password — 400")
  public void testLoginMissingPassword() {
    given().spec(spec)
      .body("{\"email\": \"eve.holt@reqres.in\"}")
    .when().post("/login")
    .then()
      .statusCode(400)
      .body("error", equalTo("Missing password"));
  }

  @Test(priority = 10, description = "Verify response time SLA")
  public void testResponseTimeSLA() {
    given().spec(spec)
    .when().get("/users?page=1")
    .then()
      .statusCode(200)
      .time(lessThan(3000L));
  }
}
```

---

*End of Part 7 — API Testing (80/80 questions)*
