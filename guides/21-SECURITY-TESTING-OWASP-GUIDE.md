# Security Testing — Complete Guide | OWASP Top 10 + How QA Tests Each

> Senior QA Interview Preparation — Security Testing from a QA Perspective

---

## Table of Contents

1. [Why QA Does Security Testing](#section-1)
2. [OWASP Top 10 (2021)](#section-2)
3. [SQL Injection Testing](#section-3)
4. [XSS Testing](#section-4)
5. [Authentication and Session Testing](#section-5)
6. [API Security Testing](#section-6)
7. [Security Testing Tools for QA](#section-7)
8. [How to Raise Security Bugs](#section-8)
9. [Interview Q&A](#section-9)

---

## Section 1 — Why QA Does Security Testing {#section-1}

### Security is Not Just the Security Team's Job

Security vulnerabilities discovered after production release are exponentially more expensive to fix than those found during testing. The average cost of a data breach in 2023 was $4.45 million (IBM). QA is the last structured gate before release — if security testing is not done in QA, vulnerabilities reach production.

Dedicated security teams (penetration testers, security architects) work at a different level of depth. But QA can and should perform a first pass of security testing during every test cycle. This is sometimes called "security smoke testing" or "application security QA."

### QA's Role in Security Testing

| QA Does | Penetration Testers Do |
|---------|----------------------|
| Black-box testing — testing the application as an external user | Deep technical exploitation |
| OWASP awareness — knowing what to look for | Full network and infrastructure testing |
| Input validation testing — injection payloads in form fields | Custom exploit development |
| Authentication and session testing | Privilege escalation chains |
| API parameter manipulation | Binary and memory analysis |
| Checking HTTP headers and HTTPS | Social engineering |
| Raising security defects with proper handling | Full written penetration test report |
| Verifying fixes (regression testing of security bugs) | Threat modelling |

### Difference Between QA Security Testing and Penetration Testing

**QA Security Testing:**
- Performed by QA engineers during the test cycle
- Uses standard tools (Burp Suite Community, OWASP ZAP, browser DevTools)
- Covers OWASP Top 10 basics — input validation, authentication, authorisation
- Takes hours to days
- Part of the normal release process

**Penetration Testing:**
- Performed by specialist security engineers or third-party firms
- Uses advanced tooling and manual exploitation techniques
- Covers full attack surface: application, network, infrastructure, social engineering
- Takes days to weeks
- Typically done once or twice a year, or after major changes

Both are necessary. QA catches the common, high-frequency vulnerabilities. Pentest catches the deeper, more sophisticated ones.

---

## Section 2 — OWASP Top 10 (2021) {#section-2}

The OWASP Top 10 is the globally recognised standard for the most critical web application security risks. It is updated every few years based on data from thousands of real-world applications.

---

### 1. Broken Access Control

**What it is:** Users can access resources or perform actions they should not be permitted to. The most common finding in web applications.

**Real example:** User A is logged in and viewing their profile at `/users/1234`. They change the URL to `/users/1235` and see User B's private profile, medical records, or payment details.

**How QA tests it:**
1. Log in as User A (ID: 1234). Note the user ID in the URL or API response.
2. Manually change the ID to another user's ID (1235, 1, admin).
3. Attempt to access admin-only pages while logged in as a standard user.
4. Attempt to access another user's order, file, or account without being logged in.
5. In API testing: GET `/api/orders/5001` as User A when Order 5001 belongs to User B — should return 403, not the order.

**Expected:** 403 Forbidden or a redirect to the user's own data.
**Failure:** Other user's data is returned.

---

### 2. Cryptographic Failures

**What it is:** Sensitive data is not protected properly. Includes: data in plaintext, weak encryption, HTTP instead of HTTPS, sensitive data in logs or URLs.

**Real example:** Passwords stored as MD5 hashes (cracked in seconds), API keys in source code, payment data transmitted over HTTP.

**How QA tests it:**
1. Check that all pages serving sensitive data (login, checkout, user profile) use HTTPS.
2. Check browser DevTools (Network tab) — are passwords or tokens appearing in GET request query strings?
3. Check API responses — are passwords, full card numbers, or unmasked tokens returned in API responses?
4. Check if the application allows HTTP (non-HTTPS) access and redirects, or allows it entirely.
5. Check SSL/TLS certificate validity using SSL Labs (ssllabs.com/ssltest/).
6. In error responses or logs — are stack traces exposing internal details like file paths or database queries?

**Expected:** All sensitive data encrypted in transit (HTTPS), passwords never returned in API responses, tokens short-lived.
**Failure:** Any sensitive data visible in plaintext.

---

### 3. Injection (SQL, XSS, Command)

**What it is:** Untrusted data is sent to an interpreter as part of a command or query. Attacker can manipulate the query to access, modify, or delete data.

**Real example:** Entering `'; DROP TABLE users; --` into a search field and having the database execute it. Entering `<script>alert(1)</script>` in a comment field that later appears to other users.

**How QA tests it:**
See dedicated sections below for SQL Injection (Section 3) and XSS (Section 4).

Key principle: **test every input field with injection payloads**. This includes: login fields, search boxes, URL parameters, HTTP headers (User-Agent, Referer, Cookie values), API request bodies.

---

### 4. Insecure Design

**What it is:** Security weaknesses that are built into the design of the application — not implementation bugs, but missing controls at the design level.

**Real examples:**
- No rate limiting on login — allows brute force attacks
- No CAPTCHA on account creation — allows bot registrations
- Workflow that allows users to skip steps in a payment flow
- Password reset sends the new password in plain text email rather than a reset link

**How QA tests it:**
1. **Rate limiting**: send repeated login requests rapidly — does the system throttle or lock?
2. **Account lockout**: attempt to brute force login — does the account lock after N failures?
3. **Business logic bypass**: can a user reach the order confirmation page without completing payment?
4. **Workflow sequence**: access Step 3 of a multi-step form directly without completing Steps 1 and 2.
5. **Registration abuse**: create 50 accounts in rapid succession — is there any protection?

---

### 5. Security Misconfiguration

**What it is:** Incorrectly configured security settings that expose the application or infrastructure.

**Real examples:**
- Debug mode enabled in production (exposes stack traces, configuration)
- Default admin credentials not changed (admin/admin)
- Directory listing enabled (http://example.com/uploads/ shows all uploaded files)
- Verbose error messages revealing database structure
- Missing security HTTP headers

**How QA tests it:**
1. Try accessing `/debug`, `/admin`, `/console`, `/.env`, `/config.json` — should return 404 or 403, not content.
2. Check HTTP response headers using browser DevTools or curl:
   - `X-Content-Type-Options: nosniff` (should be present)
   - `X-Frame-Options: DENY` or `SAMEORIGIN` (should be present)
   - `Content-Security-Policy` (should be present)
   - `Strict-Transport-Security` (should be present)
3. Trigger an error (submit invalid data) — does the error message reveal a database name, file path, or stack trace?
4. Try default credentials: admin/admin, admin/password, admin/123456.

```bash
# Quick check of HTTP security headers
curl -I https://example.com

# Expected headers to see:
# X-Content-Type-Options: nosniff
# X-Frame-Options: DENY
# Strict-Transport-Security: max-age=31536000; includeSubDomains
# Content-Security-Policy: default-src 'self'
```

---

### 6. Vulnerable and Outdated Components

**What it is:** Using libraries, frameworks, or other software components with known security vulnerabilities (CVEs — Common Vulnerabilities and Exposures).

**Real examples:**
- Log4Shell (CVE-2021-44228) — a vulnerability in the Log4j library used by millions of Java applications
- Apache Struts vulnerability that led to the Equifax breach (143 million records)

**How QA tests it:**
QA's role here is more about **awareness and triggering** rather than direct testing:
1. Include a check in the test cycle: are dependencies up to date?
2. Run `npm audit` (Node.js), `mvn dependency:check` (Maven), or `safety check` (Python) as part of CI
3. Check tools like Snyk or OWASP Dependency-Check for known CVEs in the project
4. Flag to the team if the dependency check has high-severity findings — do not release with critical CVE unresolved

```bash
# Check for known vulnerabilities in Node.js project
npm audit

# Fix automatically fixable vulnerabilities
npm audit fix

# Maven — check for dependency vulnerabilities
mvn org.owasp:dependency-check-maven:check
```

---

### 7. Identification and Authentication Failures

**What it is:** Weaknesses in login, session management, and credential handling that allow attackers to compromise accounts.

**Real examples:**
- Weak password allowed ("123456", "password")
- No account lockout after failed attempts (brute force possible)
- Session token visible in URL (logged in server access logs)
- Session not invalidated after logout (re-using a captured token still works)
- Default credentials not required to change

**How QA tests it:**
See dedicated Section 5 (Authentication and Session Testing) for full detail with code examples.

---

### 8. Software and Data Integrity Failures

**What it is:** Assumptions about software updates, critical data, and CI/CD pipelines without verifying integrity.

**Real examples:**
- Application auto-updates from an untrusted source
- Insecure deserialization: attacker sends crafted serialised object and achieves remote code execution
- CI/CD pipeline pulls from untrusted public repository

**How QA tests it:**
1. **Insecure deserialization**: send malformed JSON or serialised data in API requests — does the server crash (500) or handle gracefully (400)?
2. Check that API endpoints accepting complex data structures validate and sanitise all fields.
3. Verify that the application only accepts updates from trusted, signed sources.

---

### 9. Security Logging and Monitoring Failures

**What it is:** Insufficient logging and monitoring allows attackers to persist undetected, escalate privileges, and exfiltrate data.

**Real examples:**
- Failed login attempts are not logged
- Successful login from a new IP address triggers no alert
- User changes their own role to admin — not logged
- No alerting on 1000 failed API requests from one IP

**How QA tests it:**
1. Attempt a failed login — verify a log entry is created (check with developer or in a log aggregation tool like Kibana).
2. Attempt to access a page you are not authorised for — is the 403 logged?
3. Verify that log entries do not contain sensitive data (passwords, full card numbers, SSN).
4. Check that logs cannot be accessed by a regular user (log files should not be publicly accessible).

---

### 10. Server-Side Request Forgery (SSRF)

**What it is:** The server is tricked into making HTTP requests to an attacker-controlled URL. Can expose internal services, cloud metadata, and bypass firewalls.

**Real example:** An application has a feature where the user enters a URL to import a profile picture. The attacker enters `http://169.254.169.254/latest/meta-data/` (AWS metadata URL) and the server returns cloud credentials.

**How QA tests it:**
1. Identify any features that take a URL as input (profile picture import, webhook URL, site preview).
2. Enter a URL pointing to an internal resource: `http://localhost:8080/admin`, `http://127.0.0.1/`.
3. Enter a URL pointing to AWS metadata: `http://169.254.169.254/latest/meta-data/`.
4. Enter your own server URL (use webhook.site) — does the server make a request to it?

**Expected:** Input URL is validated against an allowlist. Internal URLs and cloud metadata endpoints are blocked.
**Failure:** Server returns internal content or makes requests to attacker-specified URLs.

---

## Section 3 — SQL Injection Testing {#section-3}

### What SQL Injection Is

SQL Injection (SQLi) occurs when user-supplied input is included in a database query without proper sanitisation. The attacker can:
- Bypass authentication
- Read all data from the database
- Modify or delete data
- In some configurations, execute operating system commands

### Test Payloads — Login Forms

Enter these in both the username/email field and the password field:

| Payload | What It Tests |
|---------|---------------|
| `'` | Single quote — does it cause a database error? |
| `''` | Escaped single quote |
| `' OR '1'='1` | Classic auth bypass |
| `' OR '1'='1' --` | Auth bypass with comment |
| `' OR 1=1 --` | Auth bypass — numeric comparison |
| `admin'--` | Comment out password check |
| `' OR 'x'='x` | Another auth bypass variant |
| `1' OR '1' = '1` | Slightly different format |

**Expected result:** Login fails with a generic error message ("Invalid email or password"). No 500 error. No database error message in the response.

**Failure indicators:**
- HTTP 500 Internal Server Error
- Error message containing "SQL syntax", "ORA-", "MySQL", "MSSQL", "sqlite_"
- Login succeeds without valid credentials
- Application returns data from other users

### Test Payloads — Search Fields

| Payload | What It Tests |
|---------|---------------|
| `test'` | Basic SQL probe |
| `test' AND '1'='1` | Boolean-based detection |
| `test' AND '1'='2` | Boolean-based detection (should return different result) |
| `test'; SELECT * FROM users; --` | Stacked queries (if supported) |
| `test' UNION SELECT NULL--` | Union-based injection probe |
| `' OR 1=1 ORDER BY 1--` | Order by probe |

**Expected result:** Search returns no results or returns only results matching the search term. No database errors.

### Test Payloads — API Parameters

Test URL parameters and request body fields:

```http
GET /api/products?id=1'
GET /api/products?id=1 OR 1=1
GET /api/users?search=test' UNION SELECT username, password FROM users--

POST /api/login
Content-Type: application/json
{"email": "admin'--", "password": "anything"}
```

### RestAssured SQL Injection Test Example

```java
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SqlInjectionTest {

    @Test
    public void testLoginWithSqlInjectionPayloadReturnsUnauthorized() {
        String[] sqlInjectionPayloads = {
            "' OR '1'='1",
            "' OR 1=1 --",
            "admin'--",
            "' OR 'x'='x",
            "'; DROP TABLE users; --"
        };

        for (String payload : sqlInjectionPayloads) {
            given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + payload + "\", \"password\": \"anything\"}")
            .when()
                .post("/api/auth/login")
            .then()
                // Must NOT return 200 OK (that would mean successful login)
                .statusCode(not(200))
                // Must NOT return 500 (that would indicate SQL error)
                .statusCode(not(500))
                // Should return 400 or 401
                .statusCode(anyOf(is(400), is(401)))
                // Response body must NOT contain SQL error indicators
                .body(not(containsString("SQL syntax")))
                .body(not(containsString("ORA-")))
                .body(not(containsString("mysql_fetch")))
                .body(not(containsString("syntax error")));
        }
    }

    @Test
    public void testSearchEndpointWithSqlInjectionPayload() {
        given()
            .queryParam("q", "test' OR 1=1 --")
        .when()
            .get("/api/products/search")
        .then()
            .statusCode(anyOf(is(200), is(400)))
            // If 200, body should be an empty or filtered result set
            // Should NOT return all products (which would indicate injection success)
            .body("size()", lessThan(100)) // sanity check — should not return all records
            .body(not(containsString("SQL syntax")));
    }
}
```

---

## Section 4 — XSS Testing {#section-4}

### What XSS Is

Cross-Site Scripting (XSS) allows attackers to inject client-side scripts (JavaScript) into pages viewed by other users. Types:

**Reflected XSS:** The malicious script is in the request (URL or form input) and reflected immediately in the response. Requires the victim to click a crafted link.

**Stored XSS (Persistent):** The malicious script is saved to the database (in a comment, profile field, etc.) and executed every time another user views the page. More dangerous because it does not require a crafted link.

**DOM-Based XSS:** The vulnerability is in client-side JavaScript code that processes untrusted data from sources like `document.location` or `innerHTML`.

### XSS Test Payloads

| Payload | Purpose |
|---------|---------|
| `<script>alert(1)</script>` | Basic XSS probe |
| `<script>alert(document.cookie)</script>` | Cookie theft probe |
| `"><script>alert(1)</script>` | Breaking out of attribute |
| `'><script>alert(1)</script>` | Breaking out of single-quoted attribute |
| `<img src=x onerror=alert(1)>` | Event handler payload (bypasses some filters) |
| `<svg onload=alert(1)>` | SVG-based XSS |
| `javascript:alert(1)` | In href attributes |
| `<iframe src="javascript:alert(1)">` | Iframe-based |
| `<body onload=alert(1)>` | Body event handler |
| `<input autofocus onfocus=alert(1)>` | Autofocus payload |

### Where to Test

Test these payloads in:
- Login form fields (email, username)
- Search bars
- Comment fields
- Profile fields (name, bio, address)
- URL parameters (`?search=`, `?name=`, `?redirect=`)
- Contact/support forms
- File upload filenames
- HTTP headers (User-Agent, Referer) if reflected in error pages

### Expected vs Failure Results

**Expected (secure application):**
- Input is HTML-encoded in the response: `<script>` becomes `&lt;script&gt;`
- Script is displayed as literal text, never executed
- Input is rejected with a validation error (400 Bad Request)
- No alert box appears

**Failure (XSS vulnerability present):**
- An alert box appears (`alert(1)` executes)
- The `<script>` tag is present in the rendered HTML (unencoded)
- `document.cookie` content is displayed in an alert
- A network request is made to an attacker-controlled server

### Playwright XSS Detection Test

```typescript
import { test, expect } from '@playwright/test';

test('search field does not execute XSS payload', async ({ page }) => {
  // Track any dialog (alert) boxes that appear
  let alertFired = false;
  page.on('dialog', async dialog => {
    alertFired = true;
    await dialog.dismiss(); // dismiss to prevent test from hanging
  });

  await page.goto('/search');

  // Enter XSS payload in search field
  await page.fill('input[name="q"]', '<script>alert(1)</script>');
  await page.press('input[name="q"]', 'Enter');

  // Wait for results to load
  await page.waitForLoadState('networkidle');

  // Assert that no alert was fired
  expect(alertFired).toBe(false);

  // Assert that the payload is encoded in the page source (not executed)
  const pageContent = await page.content();
  expect(pageContent).not.toContain('<script>alert(1)</script>');
  expect(pageContent).toContain('&lt;script&gt;'); // properly encoded
});

test('comment field does not store and execute XSS payload (stored XSS)', async ({ page }) => {
  let alertFired = false;
  page.on('dialog', async dialog => {
    alertFired = true;
    await dialog.dismiss();
  });

  // Log in as a test user
  await page.goto('/login');
  await page.fill('#email', 'attacker@example.com');
  await page.fill('#password', 'TestPassword123!');
  await page.click('button[type="submit"]');

  // Navigate to a page with a comment field
  await page.goto('/course/123/discussion');

  // Submit XSS payload as a comment
  await page.fill('textarea[name="comment"]', '<img src=x onerror=alert(document.cookie)>');
  await page.click('button[type="submit"]');

  // Now view the page as a different user (log out and log in as another)
  await page.goto('/logout');
  await page.goto('/login');
  await page.fill('#email', 'victim@example.com');
  await page.fill('#password', 'TestPassword123!');
  await page.click('button[type="submit"]');

  await page.goto('/course/123/discussion');
  await page.waitForLoadState('networkidle');

  // No alert should have fired
  expect(alertFired).toBe(false);
});
```

---

## Section 5 — Authentication and Session Testing {#section-5}

### 1. Test Default Credentials

Every application must not ship with default or easily guessable admin credentials.

**Credentials to try:**
- `admin` / `admin`
- `admin` / `password`
- `admin` / `123456`
- `admin` / `admin123`
- `administrator` / `administrator`
- `root` / `root`
- Application name as password: `myapp` / `myapp`

```java
@Test
public void testDefaultCredentialsAreRejected() {
    String[][] defaultCredentials = {
        {"admin", "admin"},
        {"admin", "password"},
        {"admin", "123456"},
        {"root", "root"},
        {"administrator", "administrator"}
    };

    for (String[] creds : defaultCredentials) {
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\": \"" + creds[0] + "\", \"password\": \"" + creds[1] + "\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(401)
            .body("message", not(containsString("Welcome")));
    }
}
```

### 2. Test Account Lockout

After a configurable number of failed attempts (typically 5–10), the account should be locked or rate-limited.

```java
@Test
public void testAccountLocksAfterRepeatedFailedLogins() {
    String email = "lockout-test@example.com";
    
    // Attempt login 10 times with wrong password
    for (int i = 0; i < 10; i++) {
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\": \"" + email + "\", \"password\": \"WrongPassword" + i + "\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(anyOf(is(401), is(429))); // 401 Unauthorized or 429 Too Many Requests
    }

    // After lockout, even the correct password should be rejected
    given()
        .contentType(ContentType.JSON)
        .body("{\"email\": \"" + email + "\", \"password\": \"CorrectPassword123!\"}")
    .when()
        .post("/api/auth/login")
    .then()
        .statusCode(anyOf(is(401), is(423), is(429))); // 423 Locked, 429 Rate Limited
}
```

### 3. Test Password Complexity Enforcement

```java
@Test
public void testWeakPasswordsAreRejected() {
    String[] weakPasswords = {"123456", "password", "abc", "12345678", "qwerty"};

    for (String weakPassword : weakPasswords) {
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\": \"newuser@example.com\", \"password\": \"" + weakPassword + "\"}")
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400) // Bad Request — weak password
            .body("errors", hasItem(containsString("password")));
    }
}
```

### 4. Test Session Timeout

After a period of inactivity, the session should expire and the user should be redirected to login.

```java
@Test
public void testSessionExpiresAfterTimeout() throws InterruptedException {
    // Log in and capture the session token
    String token = given()
        .contentType(ContentType.JSON)
        .body("{\"email\": \"user@example.com\", \"password\": \"Password123!\"}")
    .when()
        .post("/api/auth/login")
    .then()
        .statusCode(200)
        .extract().path("token");

    // Wait for session to expire (use a short timeout in test environment)
    // In a real test, this would be configured to a shorter timeout
    Thread.sleep(30 * 60 * 1000); // 30 minutes — adjust to test environment timeout

    // Attempt to use the expired session
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .get("/api/user/profile")
    .then()
        .statusCode(401) // Session expired — Unauthorized
        .body("message", containsString("expired"));
}
```

### 5. Test Session Fixation — Token Changes After Login

The session token must be regenerated after a successful login. If the pre-login session token remains valid after login, an attacker who obtained it can take over the session.

```java
@Test
public void testSessionTokenChangesAfterLogin() {
    // Get a session token before login (as an anonymous user)
    String preLoginToken = given()
    .when()
        .get("/api/session")
    .then()
        .statusCode(200)
        .extract().path("sessionToken");

    // Log in
    String postLoginToken = given()
        .contentType(ContentType.JSON)
        .body("{\"email\": \"user@example.com\", \"password\": \"Password123!\"}")
    .when()
        .post("/api/auth/login")
    .then()
        .statusCode(200)
        .extract().path("token");

    // Tokens must be different
    assertThat(preLoginToken, not(equalTo(postLoginToken)));
}
```

### 6. Test That Logout Invalidates the Token

After logout, the session token must no longer be accepted by the server.

```java
@Test
public void testLogoutInvalidatesSessionToken() {
    // Log in and get a token
    String token = given()
        .contentType(ContentType.JSON)
        .body("{\"email\": \"user@example.com\", \"password\": \"Password123!\"}")
    .when()
        .post("/api/auth/login")
    .then()
        .statusCode(200)
        .extract().path("token");

    // Verify the token works
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .get("/api/user/profile")
    .then()
        .statusCode(200);

    // Log out
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .post("/api/auth/logout")
    .then()
        .statusCode(200);

    // Verify the token no longer works after logout
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .get("/api/user/profile")
    .then()
        .statusCode(401); // Must be Unauthorized — not 200
}
```

---

## Section 6 — API Security Testing {#section-6}

### 1. IDOR — Insecure Direct Object Reference

IDOR occurs when a user can access another user's resources by guessing or incrementing an object identifier.

**Test:**
1. Log in as User A. Note your user ID (e.g. 1001).
2. Send a request to GET `/api/users/1002` (another user's ID).
3. Expected: 403 Forbidden. Failure: User 1002's data returned.

```java
@Test
public void testUserCannotAccessAnotherUsersProfile() {
    // Log in as User A
    String tokenUserA = loginAndGetToken("userA@example.com", "Password123!");

    // Attempt to access User B's profile (ID: 9999)
    given()
        .header("Authorization", "Bearer " + tokenUserA)
    .when()
        .get("/api/users/9999")
    .then()
        .statusCode(403) // Forbidden
        .body(not(containsString("userB@example.com")))
        .body(not(containsString("John Doe")));
}

@Test
public void testUserCannotAccessAnotherUsersOrders() {
    String tokenUserA = loginAndGetToken("userA@example.com", "Password123!");

    // User B's order ID
    given()
        .header("Authorization", "Bearer " + tokenUserA)
    .when()
        .get("/api/orders/ORDER-99999")
    .then()
        .statusCode(403);
}
```

### 2. Mass Assignment

Mass assignment occurs when an API blindly accepts all fields in a request body, including fields the user should not be allowed to set (like `role`, `isAdmin`, `balance`).

**Test:** Send extra privileged fields in the request body and verify they are ignored.

```java
@Test
public void testMassAssignmentPreventsRoleEscalation() {
    String token = loginAndGetToken("regularuser@example.com", "Password123!");

    // Attempt to set role=admin in the update request
    given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body("{\"name\": \"My New Name\", \"role\": \"admin\", \"isAdmin\": true, \"balance\": 999999}")
    .when()
        .put("/api/user/profile")
    .then()
        .statusCode(anyOf(is(200), is(400))); // Either OK (with ignored fields) or 400

    // Verify the user is still a regular user
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .get("/api/user/profile")
    .then()
        .statusCode(200)
        .body("role", not(equalTo("admin")))
        .body("isAdmin", equalTo(false))
        .body("balance", not(equalTo(999999)));
}
```

### 3. Rate Limiting

API endpoints must limit the number of requests per time window to prevent brute force, scraping, and DoS attacks.

```java
@Test
public void testLoginEndpointHasRateLimiting() {
    int requestCount = 0;
    boolean rateLimitHit = false;

    // Send 50 rapid requests
    for (int i = 0; i < 50; i++) {
        int statusCode = given()
            .contentType(ContentType.JSON)
            .body("{\"email\": \"test@example.com\", \"password\": \"WrongPassword\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .extract().statusCode();

        requestCount++;

        if (statusCode == 429) { // 429 Too Many Requests
            rateLimitHit = true;
            break;
        }
    }

    assertTrue(rateLimitHit, "Rate limiting not triggered after " + requestCount + " rapid requests");
}
```

### 4. HTTP Verb Tampering

Verify that endpoints only accept the HTTP methods they are designed for. A DELETE request on a read-only resource should be rejected.

```java
@Test
public void testReadOnlyEndpointRejectsDeleteRequest() {
    String token = loginAndGetToken("user@example.com", "Password123!");

    // This endpoint should only support GET
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .delete("/api/public/announcements/1")
    .then()
        .statusCode(anyOf(is(403), is(405))); // 403 Forbidden or 405 Method Not Allowed
}

@Test
public void testPOSTEndpointRejectsGETRequest() {
    // POST-only endpoint should reject GET
    given()
        .contentType(ContentType.JSON)
    .when()
        .get("/api/auth/login")
    .then()
        .statusCode(405); // Method Not Allowed
}
```

---

## Section 7 — Security Testing Tools for QA {#section-7}

### OWASP ZAP (Zed Attack Proxy) — Free

OWASP ZAP is a free, open-source web application security scanner maintained by OWASP. It is the most accessible security tool for QA engineers.

**How to use ZAP for a basic scan:**
1. Download from zaproxy.org
2. Launch ZAP
3. Select "Automated Scan" from the Quick Start tab
4. Enter your application URL
5. Click "Attack"
6. ZAP runs an active scan (sends attack payloads) and a passive scan (analyses traffic)
7. Review the Alerts tab — each finding includes the URL, request, response, evidence, and OWASP reference

**ZAP as a proxy (manual testing):**
1. Configure your browser to use ZAP as a proxy (usually `localhost:8080`)
2. Browse the application — ZAP records all requests
3. In the Sites tree, right-click any request and select "Active Scan"
4. ZAP attacks that specific endpoint

**Note:** Only use ZAP against applications you have permission to test.

---

### Burp Suite Community Edition — Free (Limited)

Burp Suite is the industry standard for web security testing. The Community Edition (free) has the core proxy and repeater features.

**Key features for QA:**

**Proxy:** Intercepts HTTP/HTTPS requests between the browser and server. You can view and modify any request before it is sent.

**Repeater:** Replay and modify captured requests. Change parameter values, inject payloads, modify headers.

**How to use:**
1. Download from portswigger.net/burp
2. Launch Burp Suite
3. Configure browser to use proxy: `127.0.0.1:8080`
4. Install Burp's CA certificate in the browser (to intercept HTTPS)
5. Browse the application — requests appear in the Proxy > HTTP History tab
6. Right-click any request and "Send to Repeater"
7. In Repeater, modify the request and send it — analyse the response

**Example use case:** Test IDOR by capturing a GET `/api/orders/1001` request and changing the ID to `1002` in Repeater.

---

### SSL Labs — Free Online Tool

SSL Labs (ssllabs.com/ssltest) tests the HTTPS configuration of a public-facing server.

**What it checks:**
- Certificate validity and expiry
- Cipher suites (weak ciphers flagged)
- Protocol support (should support TLS 1.2+, not SSL 3.0 or TLS 1.0)
- HSTS configuration
- Certificate chain
- Overall grade (A+ to F)

**How to use:**
1. Go to ssllabs.com/ssltest
2. Enter the domain
3. Wait 60–90 seconds for the scan
4. Review the grade and any findings

A grade of A or A+ is expected for production applications.

---

### Security Headers Checker — Free Online Tool

securityheaders.com checks whether the required HTTP security headers are present in the server's response.

**Headers it checks:**
- `Content-Security-Policy`
- `X-Frame-Options`
- `X-Content-Type-Options`
- `Referrer-Policy`
- `Permissions-Policy`
- `Strict-Transport-Security`

**How to use:**
1. Go to securityheaders.com
2. Enter the URL
3. Click Scan
4. Review the grade (A+ to F) and individual header results

Missing headers are flagged as warnings or failures with explanations of the risk.

---

## Section 8 — How to Raise Security Bugs {#section-8}

### Security Bugs Require Special Treatment

Security vulnerabilities are not raised the same way as functional bugs. A security bug in a public Jira board could be exploited by anyone who sees the board — including external users, contractors, and competitors.

**Rules for security bug handling:**
1. **Never put exploit details in a public backlog** — use a private, restricted security project in Jira
2. **Confidential by default** — security bugs are restricted to the development team and security stakeholders
3. **Do not create a public test environment repro** — do not leave payloads in a shared test environment
4. **Notify immediately for critical issues** — do not wait for the next sprint review. Notify the team lead and security point of contact directly.
5. **Do not self-remediate** — QA raises and verifies, not fixes

### What to Include in a Security Bug Report

```
SECURITY DEFECT REPORT
=======================
Title: [Vulnerability Type] in [Feature/Endpoint] allows [Impact]
Example: SQL Injection in Login Form allows authentication bypass

Classification: CONFIDENTIAL — SECURITY
Severity: Critical / High / Medium / Low
CVSS Score: [If known — score/10 e.g. 9.1/10 Critical]
OWASP Category: [e.g. A03:2021 – Injection]

ENVIRONMENT:
URL: https://test.example.com/login
Build version: 2.4.1
Environment: Test

STEPS TO REPRODUCE:
1. Navigate to https://test.example.com/login
2. Enter the following in the Email field: ' OR '1'='1
3. Enter any value in the Password field: anything
4. Click the Log In button

PAYLOAD USED:
Email: ' OR '1'='1
Password: anything

ACTUAL RESULT:
HTTP 200 OK — user is logged in as the first user in the database (admin account)
Response body: {"userId": 1, "role": "admin", "email": "admin@example.com"}

EXPECTED RESULT:
HTTP 401 Unauthorized — login should fail. Input should be sanitised.
No database error should be exposed.

EVIDENCE:
[Screenshot/video of the exploit — stored in restricted attachment]
[HTTP request/response capture from Burp Suite]

RISK ASSESSMENT:
An attacker could bypass authentication entirely and gain admin access to the application
without valid credentials. This would allow access to all user data.

REMEDIATION SUGGESTION:
Use parameterised queries / prepared statements. Input should be bound as a parameter,
not concatenated into the SQL string.

Example fix (Java):
  PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
  ps.setString(1, email); // safe — parameterised
```

### CVSS Severity Basics

CVSS (Common Vulnerability Scoring System) scores vulnerabilities from 0 to 10:

| Score | Severity |
|-------|----------|
| 0.0 | None |
| 0.1–3.9 | Low |
| 4.0–6.9 | Medium |
| 7.0–8.9 | High |
| 9.0–10.0 | Critical |

For a QA bug report, you do not need to calculate the full CVSS score. Indicate: **Critical** (authentication bypass, SQL injection, RCE), **High** (stored XSS, IDOR exposing sensitive data), **Medium** (reflected XSS, missing rate limiting), **Low** (security header missing, verbose error message).

---

## Section 9 — Interview Q&A {#section-9}

### Q1: What are the OWASP Top 10 and can you name them?

**Answer:**

The OWASP Top 10 is a standard awareness document for the most critical web application security risks. The 2021 edition is:

1. **A01 — Broken Access Control**: Users can access resources or perform actions beyond their permissions
2. **A02 — Cryptographic Failures**: Sensitive data exposed due to weak or missing encryption
3. **A03 — Injection**: SQL, XSS, command injection through unsanitised input
4. **A04 — Insecure Design**: Missing security controls in the design (rate limiting, CAPTCHA, business logic)
5. **A05 — Security Misconfiguration**: Default credentials, debug mode on, missing headers
6. **A06 — Vulnerable and Outdated Components**: Libraries with known CVEs
7. **A07 — Identification and Authentication Failures**: Weak passwords allowed, no lockout, session issues
8. **A08 — Software and Data Integrity Failures**: Insecure deserialization, unverified updates
9. **A09 — Security Logging and Monitoring Failures**: Failed logins not logged, no alerting
10. **A10 — Server-Side Request Forgery**: Server makes requests to attacker-controlled URLs

---

### Q2: How would you test for SQL injection?

**Answer:**

I test SQL injection systematically across all user inputs:

1. **Identify all input points**: form fields (login, search, registration), URL parameters, API request bodies, HTTP headers
2. **Start with the basic probe**: enter a single quote `'` and observe the response — a 500 error with a database error message is a strong indicator
3. **Test authentication bypass payloads** on login: `' OR '1'='1`, `' OR 1=1 --`
4. **Test search fields**: `test' AND 1=1 --` vs `test' AND 1=2 --` (boolean-based: different results indicate injection)
5. **In API testing with RestAssured**: send injection payloads in JSON body fields and assert that the response is 400 or 401 — never 500 — and that no database error messages appear in the response
6. **Expected result**: a secure application returns a generic error message. A vulnerable one returns 500, a DB error, or successfully bypasses authentication.

---

### Q3: What are the two types of XSS and how do you test for each?

**Answer:**

**Reflected XSS**: The payload is in the request (URL or form input) and is reflected in the immediate response. It requires the victim to click a crafted link.

To test: Enter `<script>alert(1)</script>` in search fields and URL parameters. If an alert appears, the input is being reflected without encoding.

**Stored XSS (Persistent)**: The payload is saved to the database and executed every time any user views the page containing it. More dangerous — no crafted link required.

To test: Enter `<script>alert(document.cookie)</script>` in comment fields, profile bios, forum posts. Log out, log in as a different user, view the page. If an alert fires, the stored XSS is confirmed.

In both cases, the expected result is that the payload is HTML-encoded in the output (`<script>` becomes `&lt;script&gt;`) and displayed as literal text — never executed.

I use Playwright to detect XSS programmatically: listen for the `dialog` event and assert it never fires.

---

### Q4: What is IDOR and how do you test for it?

**Answer:**

IDOR stands for Insecure Direct Object Reference. It occurs when an application uses user-controllable values (like an object ID in a URL) to access database records without verifying that the user is authorised to access that specific record.

**Example**: User A is logged in. Their profile URL is `/api/users/1001`. They change `1001` to `1002` and receive User B's profile data.

**How I test it:**
1. Log in as User A, note all object IDs visible in URLs and API responses (user ID, order ID, document ID)
2. Log in as User B in a different browser/session
3. Using User A's token, attempt to access User B's resources by their ID
4. Expected: 403 Forbidden. Failure: User B's data is returned to User A.

I also test:
- Incrementing/decrementing IDs to find other users' records
- Accessing admin resources with a standard user token
- Accessing another user's orders, messages, or files

---

### Q5: How does QA test authentication security?

**Answer:**

I cover authentication security across several dimensions:

1. **Password complexity**: Register with weak passwords ("123456", "password") — should be rejected with 400
2. **Account lockout**: Submit 10+ incorrect passwords — account should lock or rate-limit (expect 429 or 401 with lockout message)
3. **Default credentials**: Try admin/admin, admin/password — must be rejected
4. **Session fixation**: Capture the session token before login, log in, verify the token changed
5. **Logout**: Log in, capture token, log out, use the old token — must return 401 (not 200)
6. **Session timeout**: Verify that inactive sessions expire after the configured timeout
7. **Token in URL**: Verify that authentication tokens are never passed as URL query parameters (they appear in server logs)
8. **Brute force protection**: Rapid-fire login attempts should trigger 429 responses

All of these are tested with RestAssured for API-level verification, and manually for the UI.

---

### Q6: How do you test rate limiting?

**Answer:**

I test rate limiting by sending requests in rapid succession and verifying that the server returns 429 Too Many Requests after a threshold is crossed.

In code (RestAssured):

```java
boolean rateLimitHit = false;
for (int i = 0; i < 50; i++) {
    int status = given()
        .body("{\"email\": \"test@test.com\", \"password\": \"wrong\"}")
    .when().post("/api/auth/login")
    .then().extract().statusCode();
    
    if (status == 429) { rateLimitHit = true; break; }
}
assertTrue(rateLimitHit, "Rate limiting not triggered");
```

I also verify:
- The `Retry-After` header is present in the 429 response (tells the client when to retry)
- The rate limit applies per IP, not just per account (to prevent distributed brute force)
- After the rate limit window resets, the endpoint accepts requests again

---

### Q7: What is the difference between QA security testing and penetration testing?

**Answer:**

**QA security testing** is performed by QA engineers as part of the normal test cycle. It covers the application layer using standard tools (Burp Suite, OWASP ZAP). It focuses on OWASP Top 10 basics: input validation, authentication, authorisation, and API security. It takes hours to a few days and happens with every release.

**Penetration testing** is performed by specialist security engineers or third-party firms. It is a deep technical assessment covering the full attack surface: application, network, infrastructure, and sometimes social engineering. Pentesters write custom exploits, escalate privileges through chains of vulnerabilities, and produce a formal written report. It typically takes 1–3 weeks and happens once or twice per year, or after major architecture changes.

Both are necessary. QA catches the common, high-frequency vulnerabilities early and cheaply. Pentest finds the sophisticated, chained vulnerabilities that require deeper expertise.

---

### Q8: How do you raise a security bug?

**Answer:**

Security bugs require special handling — they should not go into a public Jira backlog.

1. **Do not create a public ticket**: use a restricted security project or a private Jira board accessible only to the development team and security stakeholders
2. **Notify immediately for critical issues**: tell the team lead directly — do not wait for a meeting
3. **Document the full exploit**: include the exact payload, exact steps to reproduce, the HTTP request and response
4. **State the impact clearly**: what data could be accessed or modified? What user population is at risk?
5. **Include OWASP category**: reference the relevant OWASP Top 10 item
6. **Suggest remediation**: parameterised queries for SQL injection, output encoding for XSS, authorisation checks for IDOR
7. **Keep evidence restricted**: screenshots and Burp captures attached with restricted visibility

I never share a security bug in a public Slack channel, public Jira comment, or pull request description.

---

### Q9: What security testing tools do you use?

**Answer:**

The tools I use as a QA engineer for security testing:

1. **Burp Suite Community Edition (free)**: HTTP proxy for intercepting and modifying requests. I use the Repeater to replay requests with modified parameters for IDOR, mass assignment, and injection testing.

2. **OWASP ZAP (free)**: Automated scanner for web application vulnerabilities. I run a basic active scan against test environments before release.

3. **RestAssured**: For automated API security tests in our Java test suite — injection payloads, authentication tests, IDOR tests, rate limiting tests.

4. **Playwright**: For XSS detection (listening for dialog events) and session testing in the browser.

5. **SSL Labs (ssllabs.com/ssltest)**: Checking HTTPS certificate configuration and cipher suite strength.

6. **securityheaders.com**: Checking HTTP security header presence and configuration.

7. **npm audit / mvn dependency check**: Identifying known CVEs in project dependencies.

---

### Q10: How would you test the security of a login page if you had 30 minutes?

**Answer:**

In 30 minutes I would cover the highest-risk scenarios:

**Minutes 1–5: Basic SQL injection**
- Try `' OR '1'='1` in both email and password fields
- Confirm the response is 401 with a generic message, no database error, no 500

**Minutes 5–10: XSS**
- Enter `<script>alert(1)</script>` in the email field — verify it is encoded in the response
- Check the forgot password flow for XSS in the email field

**Minutes 10–15: Authentication bypass**
- Try common default credentials (admin/admin, admin/password)
- Submit empty credentials, whitespace-only credentials
- Check if the error message distinguishes between "user not found" and "wrong password" (it should not — enumeration risk)

**Minutes 15–20: Account lockout and rate limiting**
- Submit 10 incorrect passwords — does the account lock or rate limit?
- Check the response code changes to 429 after threshold

**Minutes 20–25: Session security**
- Log in and copy the session token
- Log out
- Use the old token in a new request — must return 401

**Minutes 25–30: Transport security and headers**
- Confirm HTTPS is enforced — try HTTP and verify redirect
- Check response headers: is `Secure` flag on the session cookie? Is `HttpOnly` flag set?

This covers A01, A03, A05, and A07 from the OWASP Top 10 — the most likely and highest-impact findings on a login page.

---

*End of Security Testing OWASP Complete Guide*
