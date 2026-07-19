# REST API QA Practice Path — Design Spec

**Date:** 2026-07-16
**Learner level:** Complete beginner
**Goal:** Full hands-on REST API QA practice from zero, following real enterprise QA workflows

---

## Overview

Two-phase learning path that mirrors how QA engineers grow in enterprise companies:

- **Phase 1:** Manual testing with Postman — build visual understanding of REST concepts
- **Phase 2:** Automation with Java + RestAssured + TestNG — automate what you tested manually

**Target API:** [JSONPlaceholder](https://jsonplaceholder.typicode.com/)
- Free, public, no authentication needed to start
- Supports GET, POST, PUT, PATCH, DELETE
- Returns realistic JSON (posts, users, comments, todos)

---

## Phase 1 — Manual Testing with Postman

### Prerequisites
- Download and install Postman (free): https://www.postman.com/downloads/

### Steps

**Step 1 — HTTP Fundamentals**
- What is a REST API? Client-server communication over HTTP
- HTTP Methods: GET (read), POST (create), PUT (full update), PATCH (partial update), DELETE (remove)
- HTTP Status codes:
  - 2xx = Success (200 OK, 201 Created, 204 No Content)
  - 4xx = Client error (400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found)
  - 5xx = Server error (500 Internal Server Error)
- Request anatomy: URL, Method, Headers, Query Params, Body
- Response anatomy: Status code, Headers, Body (JSON/XML)

**Step 2 — First GET Request in Postman**
- Send `GET https://jsonplaceholder.typicode.com/posts`
- Inspect response: status code, JSON array of 100 posts
- Send `GET https://jsonplaceholder.typicode.com/posts/1` — single resource by ID
- Understand path parameters (`/posts/{id}`)

**Step 3 — Query Parameters**
- `GET https://jsonplaceholder.typicode.com/posts?userId=1` — filter by userId
- `GET https://jsonplaceholder.typicode.com/comments?postId=1`
- Understand how query params filter vs path params identify resources

**Step 4 — POST Request (Create)**
- `POST https://jsonplaceholder.typicode.com/posts`
- Body (JSON): `{ "title": "Test Post", "body": "Hello World", "userId": 1 }`
- Expect: 201 Created, response contains new resource with `id`

**Step 5 — PUT and PATCH (Update)**
- `PUT https://jsonplaceholder.typicode.com/posts/1` — full replacement
- `PATCH https://jsonplaceholder.typicode.com/posts/1` — partial update (just title)
- Expect: 200 OK

**Step 6 — DELETE**
- `DELETE https://jsonplaceholder.typicode.com/posts/1`
- Expect: 200 OK, empty response body `{}`

**Step 7 — Postman Collections and Environments**
- Create a Collection: "JSONPlaceholder Tests"
- Create folders: GET Tests, POST Tests, PUT Tests, DELETE Tests
- Create an Environment: set variable `base_url = https://jsonplaceholder.typicode.com`
- Use `{{base_url}}` in all requests

**Step 8 — Postman Test Scripts (JavaScript assertions)**
- Write tests in the "Tests" tab for each request:
  - Assert status code: `pm.test("Status is 200", () => pm.response.to.have.status(200))`
  - Assert response field: `pm.test("Has title", () => pm.expect(pm.response.json().title).to.not.be.empty)`
  - Assert response time: `pm.test("Response < 2s", () => pm.expect(pm.response.responseTime).to.be.below(2000))`

---

## Phase 2 — Automation with Java + RestAssured + TestNG

### Prerequisites
- Java JDK 11+ installed
- Maven installed
- IDE: IntelliJ IDEA Community (free) or Eclipse

### Project Structure

```
Rest APi Sample/
├── pom.xml
└── src/
    └── test/
        ├── java/
        │   ├── base/
        │   │   └── BaseTest.java       ← RestAssured base URI setup
        │   ├── tests/
        │   │   ├── GetTests.java       ← GET request tests
        │   │   ├── PostTests.java      ← POST request tests
        │   │   ├── PutTests.java       ← PUT/PATCH tests
        │   │   └── DeleteTests.java    ← DELETE tests
        │   └── utils/
        │       └── ApiConstants.java   ← base URL, endpoints as constants
        └── resources/
            └── testng.xml              ← TestNG suite config
```

### Maven Dependencies (pom.xml)
- `rest-assured` — HTTP client for API testing
- `testng` — test framework (annotations, assertions, reporting)
- `json-schema-validator` — validate response structure against a schema
- `hamcrest` — fluent assertion matchers

### Steps

**Step 1 — Project Setup**
- Create Maven project, add all dependencies to pom.xml
- Configure `ApiConstants.java` with `BASE_URL = "https://jsonplaceholder.typicode.com"`
- Configure `BaseTest.java` with `@BeforeClass` that sets `RestAssured.baseURI`

**Step 2 — First GET Test**
```java
given()
  .when()
    .get("/posts/1")
  .then()
    .statusCode(200)
    .body("id", equalTo(1))
    .body("title", not(emptyString()));
```

**Step 3 — GET All Posts + List Assertions**
- Assert response is an array
- Assert array size equals 100
- Assert first element has required fields (id, title, body, userId)

**Step 4 — POST Test (Create Resource)**
```java
given()
  .contentType(ContentType.JSON)
  .body("{ \"title\": \"Test\", \"body\": \"Hello\", \"userId\": 1 }")
  .when()
    .post("/posts")
  .then()
    .statusCode(201)
    .body("id", notNullValue());
```

**Step 5 — PUT Test (Full Update)**
- Build a full replacement body
- Assert 200 OK and updated fields in response

**Step 6 — DELETE Test**
- Call `delete("/posts/1")`
- Assert 200 OK
- Assert response body is empty `{}`

**Step 7 — Path Parameters and Query Parameters**
- Use `.pathParam("id", 1)` and `"/posts/{id}"`
- Use `.queryParam("userId", 1)` to filter results

**Step 8 — Headers**
- Assert `Content-Type: application/json` in response header
- Pass custom request headers with `.header("Authorization", "Bearer token")`

**Step 9 — JSON Schema Validation**
- Create `post-schema.json` defining expected fields and types
- Use `matchesJsonSchemaInClasspath("post-schema.json")` assertion
- Catches contract violations automatically

**Step 10 — TestNG Suite + HTML Report**
- Configure `testng.xml` to group all test classes
- Run with `mvn test`
- View Surefire HTML report in `target/surefire-reports/`

---

## Success Criteria

By the end of this path, you can:
- Explain what REST, HTTP methods, and status codes mean
- Manually test any REST API using Postman with assertions
- Write automated RestAssured tests covering GET/POST/PUT/DELETE
- Validate response status, headers, body fields, and JSON schema
- Organize tests in a Maven project and run them via TestNG
- Read an HTML test report

---

## Out of Scope (for now)
- CI/CD integration (Jenkins/GitHub Actions) — next step after this path
- Database validation alongside API tests
- Performance/load testing (JMeter)
- Mock servers
