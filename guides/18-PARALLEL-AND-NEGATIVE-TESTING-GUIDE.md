# Parallel Execution + Negative Testing — Complete Guide
## Selenium + Playwright | Step-by-Step Handling + Full Q&A

---

## TABLE OF CONTENTS
1. [Parallel Execution — Selenium + TestNG](#1-parallel-execution--selenium--testng)
2. [Parallel Execution — Playwright](#2-parallel-execution--playwright)
3. [Parallel Execution — Common Problems & Fixes](#3-parallel-execution--common-problems--fixes)
4. [Parallel Execution — Q&A](#4-parallel-execution--qa)
5. [Negative Testing — What It Is](#5-negative-testing--what-it-is)
6. [Negative Scenarios — UI Testing Step by Step](#6-negative-scenarios--ui-testing-step-by-step)
7. [Negative Scenarios — API Testing Step by Step](#7-negative-scenarios--api-testing-step-by-step)
8. [Negative Testing — Edge Cases & Boundary Values](#8-negative-testing--edge-cases--boundary-values)
9. [How to Handle Each Negative Scenario (Decision Tree)](#9-how-to-handle-each-negative-scenario-decision-tree)
10. [Negative Testing — Q&A](#10-negative-testing--qa)

---

## 1. Parallel Execution — Selenium + TestNG

### Why Parallel Execution?
Running 100 tests serially takes 100 × 30s = 50 minutes.
Running 100 tests with 5 parallel threads = ~10 minutes.
Parallel = faster feedback, faster CI pipelines.

### The Core Rule: ThreadLocal\<WebDriver\>

```java
// ── WRONG — shared driver, will CRASH in parallel ─────────────────────────
public class BaseTest {
    protected WebDriver driver;          // ← ONE shared instance = thread conflicts

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();     // Thread 1 sets driver
                                         // Thread 2 OVERWRITES it → both tests use same driver
    }
}

// ── CORRECT — ThreadLocal gives each thread its OWN driver ───────────────
public class BaseTest {
    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return tlDriver.get();           // returns THIS thread's driver only
    }

    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                break;
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--headless", "--no-sandbox", "--disable-gpu");
                driver = new ChromeDriver(opts);
        }
        driver.manage().window().maximize();
        tlDriver.set(driver);            // set THIS thread's driver
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            tlDriver.remove();           // CRITICAL: prevent memory leak
        }
    }
}
```

### testng.xml — All Parallel Modes

```xml
<!-- MODE 1: parallel="tests" — each <test> block runs on its own thread -->
<!-- Most common. Different browsers or feature groups run simultaneously. -->
<suite name="Suite" parallel="tests" thread-count="3">
    <test name="Login Tests - Chrome">
        <parameter name="browser" value="chrome"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
    <test name="Login Tests - Firefox">
        <parameter name="browser" value="firefox"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
    <test name="Checkout Tests - Chrome">
        <parameter name="browser" value="chrome"/>
        <classes><class name="tests.CheckoutTest"/></classes>
    </test>
</suite>

<!-- MODE 2: parallel="classes" — each test CLASS runs on its own thread -->
<suite name="Suite" parallel="classes" thread-count="4">
    <test name="All Tests">
        <classes>
            <class name="tests.LoginTest"/>       <!-- thread 1 -->
            <class name="tests.CheckoutTest"/>    <!-- thread 2 -->
            <class name="tests.ProfileTest"/>     <!-- thread 3 -->
            <class name="tests.SearchTest"/>      <!-- thread 4 -->
        </classes>
    </test>
</suite>

<!-- MODE 3: parallel="methods" — each @Test method runs on its own thread -->
<!-- DANGEROUS unless tests are fully independent with no shared state -->
<suite name="Suite" parallel="methods" thread-count="5">
    <test name="All Tests">
        <classes><class name="tests.LoginTest"/></classes>
    </test>
</suite>

<!-- MODE 4: parallel="instances" — each class instance runs on its own thread -->
<suite name="Suite" parallel="instances" thread-count="2">
    <test name="All Tests">
        <classes><class name="tests.LoginTest"/></classes>
    </test>
</suite>
```

### How thread-count Works

```
thread-count="3" means UP TO 3 tests run at the same moment.

Example with parallel="tests" and 5 <test> blocks, thread-count="3":
  Time 0: Test1 starts, Test2 starts, Test3 starts
  Time 1: Test1 finishes → Test4 starts
  Time 2: Test2 finishes → Test5 starts
  Time 3: Test3, Test4, Test5 finish

Wall clock: ~2x longer than 1/3 of serial time (not exactly 3x due to setup overhead)
```

### Maven command for parallel
```bash
mvn test                               # uses testng.xml parallel settings
mvn test -DthreadCount=4               # override thread count
mvn test -Dparallel=methods            # override parallel mode
```

---

## 2. Parallel Execution — Playwright

### How Playwright Parallelism Works

Playwright runs tests in parallel by default — no ThreadLocal needed. Each test worker gets its own browser context automatically.

```typescript
// playwright.config.ts — parallel settings
export default defineConfig({
  fullyParallel: true,        // all tests in all files run in parallel
  workers: 4,                 // 4 parallel workers (browsers)
  // workers: '50%',          // use 50% of CPU cores
  // workers: process.env.CI ? 1 : undefined,  // serial in CI, parallel locally
});
```

### File-level vs Test-level Parallelism

```typescript
// Default: different TEST FILES run in parallel
// tests/login.spec.ts  → worker 1
// tests/checkout.spec.ts → worker 2
// tests/profile.spec.ts  → worker 3

// Within a file: tests run SERIALLY by default
// To run tests within ONE file in parallel:
test.describe.configure({ mode: 'parallel' });

test.describe('Login tests', () => {
  test.describe.configure({ mode: 'parallel' });   // this describe block runs in parallel

  test('valid login', async ({ page }) => { ... });
  test('invalid login', async ({ page }) => { ... }); // runs at same time as above
});

// To force SERIAL execution (when tests share state):
test.describe.configure({ mode: 'serial' });
```

### Shared State Across Workers (Storage State)

```typescript
// auth.setup.ts — login ONCE, share session across all workers
import { test as setup } from '@playwright/test';
const AUTH_FILE = 'playwright/.auth/user.json';

setup('authenticate', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#username', 'admin');
    await page.fill('#password', 'secret');
    await page.click('#loginBtn');
    await page.waitForURL('/dashboard');
    await page.context().storageState({ path: AUTH_FILE });
});

// playwright.config.ts — use saved auth in all workers
projects: [
    { name: 'setup', testMatch: /auth\.setup\.ts/ },
    {
        name: 'tests',
        use: { storageState: 'playwright/.auth/user.json' },
        dependencies: ['setup'],   // setup runs first, once, then all workers reuse
    },
]
```

### Running Parallel Playwright Tests

```bash
npx playwright test                        # parallel (default)
npx playwright test --workers=4            # 4 workers
npx playwright test --workers=1            # serial — no parallel
npx playwright test --fully-parallel       # even tests within a file run parallel

# Cross-browser parallel (matrix)
# In playwright.config.ts projects: chromium, firefox, webkit
# Each project runs all tests in parallel with its browser
npx playwright test --project=chromium     # only chromium, parallel
npx playwright test                        # all 3 browsers simultaneously
```

---

## 3. Parallel Execution — Common Problems & Fixes

### Problem 1: Tests interfering with each other's data

```
Symptom: Test A creates user "john@test.com", Test B also creates "john@test.com"
         → Conflict / unique constraint error
Fix:     Use unique test data per test — append timestamp or UUID:

String email = "test_" + System.currentTimeMillis() + "@test.com";
// or in Playwright:
const email = `test_${Date.now()}@test.com`;
```

### Problem 2: Tests depend on each other's order

```
Symptom: LoginTest passes when run alone but fails when run in parallel
         because CheckoutTest logs out the shared user session

Fix:     Each test must be FULLY INDEPENDENT.
         - Never rely on another test's setup or teardown
         - Each test creates its own data, navigates to its own state
         - Use @BeforeMethod not @BeforeClass for data that can't be shared
```

### Problem 3: Selenium — StaleElementReferenceException in parallel

```java
// Symptom: element found in one thread, DOM refreshes due to another thread's action
// Fix:     Re-find the element. Use explicit waits. Wrap in retry.

public WebElement findWithRetry(By locator) {
    for (int i = 0; i < 3; i++) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (StaleElementReferenceException e) {
            if (i == 2) throw e;
        }
    }
    return null;
}
```

### Problem 4: Playwright — test.beforeAll shared state corrupted

```typescript
// WRONG: beforeAll runs once per worker but state is shared
test.describe('suite', () => {
    let userId: number;

    test.beforeAll(async ({ request }) => {
        const r = await request.post('/users', { data: { name: 'Test' } });
        userId = (await r.json()).id;   // parallel tests all share this userId
    });                                  // → race conditions
});

// CORRECT: use beforeEach so each test gets its own data
test.beforeEach(async ({ request }) => {
    const r = await request.post('/users', {
        data: { name: `Test_${Date.now()}` }  // unique per test
    });
    userId = (await r.json()).id;
});
```

### Problem 5: Screenshots overwriting each other

```java
// WRONG: same filename → parallel tests overwrite each other's screenshots
File shot = ts.getScreenshotAs(OutputType.FILE);
FileUtils.copyFile(shot, new File("screenshots/failure.png"));  // race condition!

// CORRECT: unique filename per thread
String filename = "screenshots/FAIL_"
    + result.getName() + "_"
    + Thread.currentThread().getId() + "_"
    + System.currentTimeMillis() + ".png";
FileUtils.copyFile(shot, new File(filename));
```

### Problem 6: Port conflicts in parallel API tests

```
Symptom: Two tests try to start a mock server on the same port
Fix:     Use random ports or use a single shared mock server started in @BeforeSuite
```

---

## 4. Parallel Execution — Q&A

**Q: What is the problem with a non-ThreadLocal WebDriver in parallel tests?**
```
If WebDriver is a plain instance variable (not ThreadLocal), all parallel threads
share the SAME driver object. Thread 1 navigates to /login, Thread 2 navigates
to /checkout — both on the same browser window. Actions from Thread 1 and Thread 2
collide, causing unpredictable failures and race conditions.

ThreadLocal<WebDriver> gives each thread its own independent driver instance.
Thread 1 has its own Chrome, Thread 2 has its own separate Chrome. Zero conflict.
```

**Q: What does `tlDriver.remove()` do and why is it critical?**
```java
@AfterMethod
public void tearDown() {
    getDriver().quit();
    tlDriver.remove();    // ← removes THIS thread's driver reference from ThreadLocal
}

// Without remove(): the ThreadLocal holds onto the WebDriver reference even after
// quit() closes the browser. Over many test runs (thread pool reuse), this builds
// up in memory → OutOfMemoryError after ~200 tests.
// Always call remove() after quit().
```

**Q: In Playwright, why don't you need ThreadLocal?**
```
Playwright's architecture is fundamentally different from Selenium.
Each Playwright worker is a completely isolated Node.js process — not a thread.
Processes don't share memory. So there's no shared-state problem to solve.
The `page` fixture Playwright injects into each test is already isolated per test.
```

**Q: How do you run the same test against Chrome AND Firefox at the same time in Selenium?**
```xml
<!-- testng.xml -->
<suite parallel="tests" thread-count="2">
    <test name="Chrome">
        <parameter name="browser" value="chrome"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
    <test name="Firefox">
        <parameter name="browser" value="firefox"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
</suite>
```
```java
// BaseTest reads the parameter
@BeforeMethod
@Parameters("browser")
public void setUp(String browser) {
    WebDriver d = browser.equals("firefox") ? new FirefoxDriver() : new ChromeDriver();
    tlDriver.set(d);
}
```

**Q: How do you run parallel tests against multiple browsers in Playwright?**
```typescript
// playwright.config.ts
projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox',  use: { ...devices['Desktop Firefox'] } },
    { name: 'webkit',   use: { ...devices['Desktop Safari'] } },
]

// npx playwright test → runs all tests on all 3 browsers simultaneously
// npx playwright test --project=chromium → only Chrome
```

**Q: How do you prevent two parallel tests from creating conflicting test data?**
```
Three strategies:

1. Unique per-test data (most common):
   String email = "user_" + System.currentTimeMillis() + "@test.com";

2. Test data partitioning:
   Test worker 1 uses userId 1-100, worker 2 uses userId 101-200

3. Isolated test database per worker:
   Each worker connects to a separate schema/database
   (advanced — requires infrastructure support)
```

**Q: A test suite that ran fine serially now fails in parallel. How do you debug it?**
```
Step 1: Run with --workers=1 (serial) to confirm it passes → confirms parallel issue
Step 2: Run with --workers=2 to find the minimum reproduction
Step 3: Look for:
  - Shared state (static fields, shared objects)
  - Test data conflicts (duplicate emails, IDs)
  - Order dependency (test B assumes test A ran first)
  - Screenshot/file name collisions
  - Same port used by multiple tests
Step 4: Add timestamps/thread IDs to all shared resources
Step 5: Use synchronized blocks only if absolutely necessary
```

**Q: What is the difference between `parallel="tests"` and `parallel="methods"` in TestNG?**
```
parallel="tests":   each <test> tag in testng.xml runs in its own thread.
                    Tests within a <test> run serially. SAFE — good starting point.

parallel="methods": each @Test method gets its own thread.
                    Fastest but DANGEROUS unless every method is 100% independent.
                    Any shared state (@BeforeClass setup, static fields) will break.

Start with parallel="tests". Only use parallel="methods" when you've confirmed
every single test method is fully independent.
```

---

## 5. Negative Testing — What It Is

**Negative testing** verifies that the system handles invalid, unexpected, or out-of-bounds inputs correctly — that it fails gracefully rather than crashing, exposing data, or silently passing bad data through.

```
Positive test: enter valid credentials → user logs in (happy path)
Negative test: enter empty password → error message shown, no login
               enter SQL injection → handled safely, no crash
               enter 10,000 character username → rejected or truncated cleanly

Senior QA insight: bugs hide at the boundaries and the edges.
Happy path tests catch obvious bugs.
Negative tests catch the bugs that reach production.
```

---

## 6. Negative Scenarios — UI Testing Step by Step

### Scenario: Empty Required Fields

```
What to test: What happens when user submits a form with required fields empty?
Expected: Validation error shown. Form NOT submitted. User stays on the page.

Step-by-step approach:
1. Navigate to the form
2. Leave required field(s) empty
3. Click Submit
4. Assert: error message is visible
5. Assert: error message text is correct and helpful
6. Assert: user did NOT navigate away (still on same URL)
7. Assert: form is NOT reset (data user typed in other fields is preserved)
```

```java
// Selenium
@Test
public void emptyUsername_showsValidationError() {
    loginPage.navigate();
    loginPage.enterPassword("Admin@123");     // fill password, leave username empty
    loginPage.clickSubmit();

    Assert.assertTrue(loginPage.getUsernameError().isDisplayed(),
        "Username error should be visible");
    Assert.assertEquals(loginPage.getUsernameError().getText(),
        "Username is required",
        "Error message text should match");
    Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
        "User should stay on login page");
}
```

```typescript
// Playwright
test('empty username shows validation error', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#password', 'Admin@123');   // fill password only
    await page.click('#loginBtn');

    await expect(page.locator('#username-error')).toBeVisible();
    await expect(page.locator('#username-error')).toHaveText('Username is required');
    await expect(page).toHaveURL(/\/login/);     // still on login page
});
```

---

### Scenario: Wrong / Invalid Input Format

```
What to test: Field accepts text but receives wrong format (e.g. email without @)
Expected: Specific format validation error. Not a generic "something went wrong".

Step-by-step:
1. Navigate to form
2. Enter invalid format value (e.g. "notanemail" in email field)
3. Trigger validation (click away or click submit)
4. Assert: specific error shown (e.g. "Please enter a valid email address")
5. Assert: generic error NOT shown (e.g. "Error 500")
6. Assert: user stays on page
7. Repeat for boundary formats: "@.com", "a@b", "a @b.com" (space in email)
```

```java
@Test(dataProvider = "invalidEmails")
public void invalidEmailFormat_showsValidationError(String email, String expectedError) {
    registrationPage.navigate();
    registrationPage.enterEmail(email);
    registrationPage.clickAway();   // trigger blur validation

    Assert.assertTrue(registrationPage.getEmailError().isDisplayed());
    Assert.assertEquals(registrationPage.getEmailError().getText(), expectedError);
}

@DataProvider(name = "invalidEmails")
public Object[][] invalidEmails() {
    return new Object[][] {
        { "notanemail",   "Please enter a valid email address" },
        { "@.com",        "Please enter a valid email address" },
        { "a @b.com",     "Please enter a valid email address" },
        { "a@b",          "Please enter a valid email address" },
        { "",             "Email is required"                  },
    };
}
```

---

### Scenario: Exceeding Maximum Length

```
What to test: Field has a max character limit. What happens if exceeded?
Expected: Either field stops accepting input at max, OR error shown, NOT data truncated silently.

Step-by-step:
1. Find the max length (from spec, or inspect maxlength attribute)
2. Enter exactly max length → should PASS
3. Enter max + 1 character → should show error OR be prevented
4. Enter very large string (10,000 chars) → should not crash the server

Key assertion: verify the ACTUAL saved value matches what user entered up to max.
Silent truncation (accepts 10,000 chars but saves only 255) is a bug.
```

```java
@Test
public void usernameAtMaxLength_accepted() {
    String maxUsername = "a".repeat(50);   // assuming 50 char limit
    registrationPage.enterUsername(maxUsername);
    Assert.assertEquals(registrationPage.getUsernameField().getAttribute("value"),
        maxUsername, "Max length username should be accepted fully");
}

@Test
public void usernameExceedingMaxLength_rejected() {
    String tooLong = "a".repeat(51);
    registrationPage.enterUsername(tooLong);
    // Either truncated to 50 chars, OR error shown
    String actual = registrationPage.getUsernameField().getAttribute("value");
    Assert.assertTrue(actual.length() <= 50,
        "Username should not exceed max length of 50");
}
```

---

### Scenario: Security — SQL Injection in Input Fields

```
What to test: Entering SQL injection in text fields should NOT:
  - Crash the server (500 error)
  - Return unintended data
  - Execute SQL commands

Expected: Either rejected with validation error, OR treated as plain text.
NEVER: 500 error, exposed data, authentication bypass.

Step-by-step:
1. Enter SQL injection strings in login, search, name fields
2. Assert: no 500 error
3. Assert: no unexpected redirect (e.g. auto-login)
4. Assert: system handles it as plain text input
```

```java
@Test(dataProvider = "sqlInjectionStrings")
public void sqlInjection_handledSafely(String injection) {
    loginPage.navigate();
    loginPage.enterUsername(injection);
    loginPage.enterPassword("anything");
    loginPage.clickSubmit();

    // Must NOT get logged in
    Assert.assertFalse(driver.getCurrentUrl().contains("/dashboard"),
        "SQL injection must not bypass authentication");

    // Must NOT see a 500 error
    Assert.assertFalse(driver.getPageSource().contains("SQL syntax error"),
        "SQL error must not be exposed to user");
    Assert.assertFalse(driver.getPageSource().contains("Internal Server Error"),
        "Server error must not be exposed");
}

@DataProvider(name = "sqlInjectionStrings")
public Object[][] sqlInjectionStrings() {
    return new Object[][] {
        { "' OR '1'='1" },
        { "' OR 1=1 --" },
        { "'; DROP TABLE users; --" },
        { "' UNION SELECT * FROM users --" },
        { "admin'--" },
    };
}
```

---

### Scenario: XSS (Cross-Site Scripting) in Input Fields

```
What to test: Entering script tags should NOT execute JavaScript in the browser.
Expected: Script is either escaped/sanitised or rejected.

Step-by-step:
1. Enter XSS payload in name, comment, search fields
2. Submit the form
3. Navigate to where the value is displayed
4. Assert: script did NOT execute (no alert box appeared)
5. Assert: the text is displayed as plain text (escaped), OR not shown at all
```

```typescript
test('XSS payload is not executed', async ({ page }) => {
    await page.goto('/profile/edit');
    await page.fill('#displayName', "<script>alert('xss')</script>");
    await page.click('#saveBtn');

    // Set up listener — if an alert fires, the test will catch it
    let alertFired = false;
    page.on('dialog', async dialog => {
        alertFired = true;
        await dialog.dismiss();
    });

    await page.goto('/profile');       // navigate to where name is displayed
    await page.waitForTimeout(1000);   // give any script time to execute

    expect(alertFired).toBe(false);    // script must NOT have executed
    // Optionally: check the text is displayed escaped
    await expect(page.locator('#displayName'))
        .toContainText('<script>');    // shown as text, not executed
});
```

---

### Scenario: Concurrent / Double Submit

```
What to test: User clicks Submit button twice quickly (or double-clicks).
Expected: Form submitted ONCE. Not twice.
Real bug: Order placed twice, payment charged twice.

Step-by-step:
1. Fill and submit a form
2. Immediately click Submit again (or double-click)
3. Assert: only ONE record was created
4. Assert: button disabled/loading after first click (UI protection)
```

```java
@Test
public void doubleSubmit_createsOnlyOneRecord() {
    orderPage.navigate();
    orderPage.fillOrderDetails();
    orderPage.clickSubmitButton();
    orderPage.clickSubmitButton();   // second click immediately

    // Verify only one order was created
    int orderCount = orderPage.getOrderCount();
    Assert.assertEquals(orderCount, 1, "Only one order should be created on double submit");
}
```

---

## 7. Negative Scenarios — API Testing Step by Step

### Scenario: Missing Required Fields → 400

```
What to test: POST with required field missing
Expected: 400 Bad Request, meaningful error message identifying WHICH field

Step-by-step:
1. Send POST with one required field missing
2. Assert: status 400 (not 201, not 500)
3. Assert: error message names the missing field
4. Repeat for each required field individually
5. Repeat with ALL required fields missing
```

```java
@Test
public void createUser_missingEmail_returns400() {
    String bodyWithoutEmail = "{ \"name\": \"John\", \"password\": \"secret\" }";

    given()
        .spec(requestSpec)
        .body(bodyWithoutEmail)
    .when()
        .post("/users")
    .then()
        .statusCode(400)
        .body("error", containsString("email"))     // error names the missing field
        .body("error", not(containsString("password")));  // other fields not mentioned
}

@DataProvider(name = "missingFields")
public Object[][] missingFields() {
    return new Object[][] {
        { "{ \"name\": \"John\" }",                           "email" },
        { "{ \"email\": \"j@test.com\" }",                   "name"  },
        { "{}",                                               "email" },
    };
}

@Test(dataProvider = "missingFields")
public void createUser_missingField_returns400(String body, String missingField) {
    given().spec(requestSpec).body(body)
    .when().post("/users")
    .then()
        .statusCode(400)
        .body("error", containsString(missingField));
}
```

---

### Scenario: Invalid Data Type → 400

```
What to test: Send wrong data type for a field (string where integer expected)
Expected: 400 Bad Request, not 500 (server must not crash on bad input)

Step-by-step:
1. Identify fields with type constraints (e.g. age: integer, price: decimal)
2. Send wrong type (e.g. age: "twenty" instead of 20)
3. Assert: 400 (never 500)
4. Assert: error message is informative, not a stack trace
```

```java
@Test
public void createProduct_invalidPriceType_returns400() {
    String invalidBody = "{ \"name\": \"Widget\", \"price\": \"expensive\" }";

    given()
        .spec(requestSpec)
        .body(invalidBody)
    .when()
        .post("/products")
    .then()
        .statusCode(400)
        .body("error", not(containsString("NullPointerException"))) // no stack trace exposed
        .body("error", not(containsString("Exception")));
}
```

---

### Scenario: Authentication Failures

```
What to test: All the ways auth can fail
Expected: 401 for missing/invalid token. 403 for insufficient permission.

Step-by-step — test each case separately:
```

```java
// 1. No token at all → 401
@Test
public void noToken_returns401() {
    given()   // no auth header
    .when().get("/protected-resource")
    .then().statusCode(401);
}

// 2. Wrong token format → 401
@Test
public void malformedToken_returns401() {
    given()
        .header("Authorization", "NotBearer xyz")  // wrong format
    .when().get("/protected-resource")
    .then().statusCode(anyOf(equalTo(401), equalTo(400)));
}

// 3. Valid token, wrong role → 403
@Test
public void validTokenWrongRole_returns403() {
    String viewerToken = getTokenForRole("viewer");  // viewer can't DELETE
    given()
        .header("Authorization", "Bearer " + viewerToken)
        .pathParam("id", 1)
    .when().delete("/admin/users/{id}")
    .then().statusCode(403);
}

// 4. Expired token → 401
@Test
public void expiredToken_returns401() {
    String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MDB9.expired";
    given()
        .header("Authorization", "Bearer " + expiredToken)
    .when().get("/protected-resource")
    .then().statusCode(401);
}
```

---

### Scenario: Non-Existent Resource → 404

```java
@Test(dataProvider = "nonExistentIds")
public void getByNonExistentId_returns404(int id) {
    given().pathParam("id", id)
    .when().get("/posts/{id}")
    .then().statusCode(404);
}

@DataProvider(name = "nonExistentIds")
public Object[][] nonExistentIds() {
    return new Object[][] {
        { 0 },          // zero — no resource has id=0
        { -1 },         // negative
        { 99999 },      // very large
        { Integer.MAX_VALUE },  // max int
    };
}
```

---

### Scenario: Duplicate Resource → 409

```java
@Test
public void createDuplicateUser_returns409() {
    String body = "{ \"email\": \"existing@test.com\", \"name\": \"Test\" }";

    // First creation → 201
    given().spec(requestSpec).body(body)
    .when().post("/users")
    .then().statusCode(201);

    // Second creation with same email → 409 Conflict
    given().spec(requestSpec).body(body)
    .when().post("/users")
    .then().statusCode(409)
    .body("error", containsString("already exists"));
}
```

---

## 8. Negative Testing — Edge Cases & Boundary Values

```java
// ── BOUNDARY VALUES ───────────────────────────────────────────────────────────
// Rule: test at min, min-1, min+1, max-1, max, max+1

// Example: age field accepts 18-65
@DataProvider(name = "ageBoundaries")
public Object[][] ageBoundaries() {
    return new Object[][] {
        // age,  expectedStatus,   description
        { 17,    400,  "just below minimum" },
        { 18,    201,  "minimum boundary"   },
        { 19,    201,  "just above minimum" },
        { 64,    201,  "just below maximum" },
        { 65,    201,  "maximum boundary"   },
        { 66,    400,  "just above maximum" },
        { 0,     400,  "zero"               },
        { -1,    400,  "negative"           },
    };
}

// ── SPECIAL CHARACTERS ────────────────────────────────────────────────────────
@DataProvider(name = "specialCharInputs")
public Object[][] specialCharInputs() {
    return new Object[][] {
        { "O'Brien",        "apostrophe in name"   },
        { "José",           "accented characters"  },
        { "用户名",          "Chinese characters"   },
        { "name@test.com",  "@ symbol in name"     },
        { "test<>test",     "angle brackets"       },
        { "   spaces   ",   "leading/trailing spaces" },
        { "\t\n\r",         "whitespace only"      },
    };
}

// ── NULL AND EMPTY ─────────────────────────────────────────────────────────────
// Test null (not sent), empty string (""), whitespace only ("   ")
// These are DIFFERENT edge cases — test each separately
@Test
public void nameField_whitespaceOnly_treatedAsEmpty() {
    given().spec(requestSpec)
        .body("{ \"name\": \"   \", \"email\": \"test@test.com\" }")
    .when().post("/users")
    .then()
        .statusCode(400)    // whitespace-only name should be rejected like empty
        .body("error", containsString("name"));
}
```

---

## 9. How to Handle Each Negative Scenario (Decision Tree)

```
When you find a negative test scenario, ask these questions in order:

1. WHAT is the input?
   → Identify: empty, wrong type, out of range, invalid format, malicious

2. WHAT should happen?
   → Check the spec/acceptance criteria
   → If not documented: ask the developer/PO
   → Document your assumption in the test

3. WHAT does the system actually do?
   → Run the test
   → Observe: error message, status code, redirect, data state

4. IS THE ACTUAL RESULT ACCEPTABLE?
   → 400/422 with helpful error message → PASS
   → 500 Server Error → BUG — server crashed on bad input
   → 200 with wrong/empty data silently → BUG — silent failure
   → Redirect to login instead of error → depends on spec

5. IF IT'S A BUG — what severity?
   → 500 on bad input = High (attacker can crash your server)
   → XSS executing = Critical (security vulnerability)
   → Wrong error message = Low (UX issue)
   → Missing validation = Medium (data integrity risk)

6. WRITE THE TEST SO IT STAYS FIXED
   → Automate it. It must run on every build.
   → Negative tests are regression tests for security and data integrity.
```

---

## 10. Negative Testing — Q&A

**Q: What is the difference between negative testing and error handling testing?**
```
Negative testing: YOU send bad input intentionally to verify the system rejects it properly.
Error handling testing: you verify the system's RESPONSE when something goes wrong
(e.g. database is down, third-party service times out) — these are conditions OUTSIDE
the input, not caused by bad user input.

Both are essential. Negative tests the input boundary. Error handling tests the system resilience.
```

**Q: Why is 500 on bad input always a bug?**
```
A 500 means the server crashed. A well-designed server should NEVER crash on user input,
no matter how bad. It should:
  - Validate input early
  - Return 400/422 with a meaningful error
  - Never let bad input reach the database or business logic

If a tester can cause a 500 with bad input, an attacker can too.
500 on bad input = untested input validation = potential security vulnerability.
```

**Q: How do you decide WHICH negative tests to write when you have limited time?**
```
Prioritise by risk:
1. Security inputs first: SQL injection, XSS, authentication bypass
   → Highest impact if missed
2. Required field validation
   → Data integrity at stake
3. Boundary values for numeric/length fields
   → Common source of off-by-one bugs
4. Duplicate/conflict scenarios (email, username)
   → Often broken by naive implementations
5. Format validation (email, phone, date)
   → Lower risk but high user-facing impact

Skip: deeply edge cases that have no realistic user or attacker path.
```

**Q: A form submits successfully with an empty required field. What do you do?**
```
1. Raise a bug immediately — severity Medium or High depending on field
2. Check if validation is only on frontend (JS disabled removes it) → mention in bug
3. Check if backend also validates → test the API directly without frontend
4. If only frontend validation exists → add "missing server-side validation" to bug
5. Check what gets saved in the database — null? empty string? — include in bug report
6. Write an automated test so it can't regress
```

**Q: How do you test for race conditions (two users doing the same thing at the same time)?**
```
Race conditions are hard to test with standard tools. Approaches:

1. Parallel API calls:
   Send the same POST request from 5 threads simultaneously using JMeter or custom Java
   Assert only ONE resource was created (check response + DB count)

2. Playwright parallel workers:
   Two tests both try to claim the same limited resource simultaneously
   Assert system handles it gracefully (one succeeds, one gets 409/error)

3. Database transaction check:
   After race, query DB and verify count = 1, no duplicates, no null states

Real-world: "Flash sale — 1000 users click Buy at the same moment on last item"
   → Only 1 purchase should succeed. Others get "out of stock".
   → Without proper locking: 10 people buy the 1 remaining item.
```

**Q: How is boundary value testing different from equivalence partitioning?**
```
Equivalence Partitioning: divide inputs into groups (partitions) where behaviour
is the same. Test ONE value from each group.
  → Age 18-65 valid → test ONE valid age (e.g. 30)
  → Age <18 invalid → test ONE invalid (e.g. 10)

Boundary Value Analysis: TEST AT THE EXACT BOUNDARIES of those partitions.
  → Test 17 (just invalid), 18 (just valid), 65 (just valid), 66 (just invalid)

Use BOTH together: EP to identify the partitions, BVA to test their edges.
Bugs hide at boundaries — BVA without EP misses whole categories of input.
```
