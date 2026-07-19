# Parallel Test Execution + Negative Testing — Complete Interview Q&A Guide

> Senior QA Interview Reference — Every concept answered as a real interview question. Parallel execution with Selenium/TestNG and Playwright, ThreadLocal deep-dive, negative testing patterns, boundary analysis, security inputs, and 10 dedicated interview Q&As.

---

## SECTION 1 — Parallel Test Execution Fundamentals

---

**Q1: What is parallel test execution and why does it matter in CI/CD?**

**A:** Parallel test execution means running multiple tests simultaneously across separate threads, processes, or machines rather than one at a time. It is one of the most impactful improvements a QA team can make to its automation pipeline.

**The maths of why it matters:**

```
Serial execution:
  100 tests × 30 seconds each = 50 minutes feedback time

Parallel execution with 5 workers:
  100 tests ÷ 5 workers × 30 seconds = ~10 minutes feedback time
  (5x faster — practical speedup is slightly less due to setup overhead)

In CI/CD context:
  50-minute feedback loop  → developer has moved on, context is cold
  10-minute feedback loop  → developer can fix and re-push in the same session
```

**Why it matters for QA specifically:**
1. **Faster feedback** — developers get test results before they context-switch to the next task
2. **Cross-browser coverage** — Chrome and Firefox tests run simultaneously, not serially
3. **Scalable suite growth** — as your test suite grows, parallelism keeps CI times acceptable
4. **Earlier bug detection** — tests that run every PR need to be fast enough to be actionable

**Trade-off:** Parallelism requires tests to be independent. Shared state, shared data, and order-dependent tests all break under parallelism. Designing for parallel execution forces better test architecture.

---

**Q2: What is ThreadLocal? Why is it essential for parallel Selenium tests?**

**A:** `ThreadLocal<T>` is a Java class that provides thread-local variable storage — each thread that accesses a `ThreadLocal` variable gets its own independent copy of that variable. Other threads cannot see or touch it.

In parallel Selenium testing, every thread running tests needs its own `WebDriver` instance. If all threads share a single `WebDriver`, they fight over the same browser window — one thread navigates to `/login` while another is trying to click a button on `/checkout`. The result is unpredictable failures.

`ThreadLocal<WebDriver>` solves this by giving each thread its own `WebDriver` that no other thread can access.

```java
public class BaseTest {
    // ThreadLocal holds one WebDriver per thread
    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    // Static getter — always returns THIS thread's driver
    public static WebDriver getDriver() {
        return tlDriver.get();
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
            case "edge":
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                break;
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--headless", "--no-sandbox", "--disable-gpu");
                driver = new ChromeDriver(opts);
        }
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        tlDriver.set(driver);   // store in THIS thread's slot
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            tlDriver.remove();   // CRITICAL — explained in Q5
        }
    }
}
```

---

**Q3: What happens if you do NOT use ThreadLocal in parallel tests? Show the problem with code.**

**A:** Without `ThreadLocal`, all parallel threads share one `WebDriver` instance. The result is race conditions — both threads are operating on the same browser simultaneously, producing completely unpredictable behaviour.

```java
// ── THE BROKEN VERSION — shared driver ──────────────────────────────────────

public class BrokenBaseTest {
    protected WebDriver driver;   // ← one shared instance for ALL threads

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        // Thread 1: sets driver = ChromeDriver instance A
        // Thread 2: IMMEDIATELY OVERWRITES driver = ChromeDriver instance B
        // Thread 1 is now using instance B — the window Thread 2 opened
    }
}

public class LoginTest extends BrokenBaseTest {
    @Test
    public void login_validCredentials() {
        driver.get("https://app.com/login");   // Thread 1
        // Thread 2 simultaneously: driver.get("https://app.com/checkout")
        // Both are calling methods on the same driver object
        // Result: driver is now on /checkout (Thread 2 won the race)
        // Thread 1's login call operates on the checkout page → test fails
        driver.findElement(By.id("username")).sendKeys("admin");
        // Error: no element with id="username" on the checkout page
    }
}
```

**What you actually observe (symptoms):**
- `NoSuchElementException` on elements that clearly exist on the page
- `StaleElementReferenceException` because another thread reloaded the page
- Test A passes, Test B fails — then swap on the next run (non-deterministic)
- Tests that pass when run alone fail when run in parallel
- `InvalidSessionIdException` — the session was closed by another thread's tearDown

**The fix:**
```java
// ── THE CORRECT VERSION — ThreadLocal driver ─────────────────────────────────

public class BaseTest {
    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return tlDriver.get();  // Thread 1 gets its own driver, Thread 2 gets its own
    }

    @BeforeMethod
    public void setUp() {
        ChromeDriver driver = new ChromeDriver();
        tlDriver.set(driver);   // stored in THIS thread's slot — invisible to other threads
    }
}
```

Thread 1 calls `tlDriver.set(driverA)` → Thread 1 always gets `driverA` back from `tlDriver.get()`.
Thread 2 calls `tlDriver.set(driverB)` → Thread 2 always gets `driverB` back from `tlDriver.get()`.
Neither thread can see the other's driver.

---

**Q4: How does ThreadLocal work internally?**

**A:** Internally, each `Thread` object in Java has a field called `threadLocals` which is a map-like structure (`ThreadLocalMap`). When you call `tlDriver.set(value)`, Java stores the value in the current thread's `threadLocals` map, keyed by the `ThreadLocal` instance.

```
Thread 1's threadLocals map:
  { tlDriver → ChromeDriver instance A }

Thread 2's threadLocals map:
  { tlDriver → ChromeDriver instance B }

Thread 3's threadLocals map:
  { tlDriver → FirefoxDriver instance C }
```

When Thread 1 calls `tlDriver.get()`, Java:
1. Gets the current thread (`Thread.currentThread()`)
2. Looks up the `threadLocals` map on that thread
3. Returns the value stored for the `tlDriver` key

Thread 1's map has `ChromeDriver A`. Thread 2's map has `ChromeDriver B`. They are completely separate.

**The thread-pool problem (why `remove()` is critical):**
In thread pools (used by TestNG), threads are reused across tests. If Thread 1 runs Test A, stores a `WebDriver`, finishes Test A, and is recycled for Test C — the `ThreadLocal` still holds the old `WebDriver` reference (from Test A) unless you explicitly call `tlDriver.remove()`. This is both a memory leak and a correctness bug.

---

**Q5: What is `tlDriver.remove()` and why is it CRITICAL to call it?**

**A:** `tlDriver.remove()` deletes the `WebDriver` reference stored for the current thread from the `ThreadLocal` map. Without it, two serious problems occur.

**Problem 1 — Memory leak:**

```java
// WRONG — quit but no remove
@AfterMethod
public void tearDown() {
    getDriver().quit();
    // driver browser process is closed — BUT the Java WebDriver object reference
    // is still stored in the ThreadLocal map on the thread
    // Thread pool recycles this thread → ThreadLocal still holds a reference
    // to a dead WebDriver object
    // After 100+ tests: 100 dead WebDriver references in memory → OutOfMemoryError
}

// CORRECT — quit AND remove
@AfterMethod(alwaysRun = true)
public void tearDown() {
    if (getDriver() != null) {
        getDriver().quit();    // close the browser process
        tlDriver.remove();     // remove the reference from THIS thread's ThreadLocal map
    }
    // Thread is now clean — no reference to the old driver remains
    // When thread is recycled, getDriver() returns null (correct, no stale driver)
}
```

**Problem 2 — Stale driver reuse:**

```java
// Without remove():
// Thread 1 runs Test A: setUp() → sets WebDriver A
// Thread 1 runs Test A: tearDown() → driver.quit() (browser closed)
// Thread 1 is recycled for Test B: setUp() fails partway through → tlDriver.set() never called
// Thread 1 runs Test B tests: getDriver() returns WebDriver A (dead session!)
// Result: InvalidSessionIdException on every action
```

The `alwaysRun = true` attribute on `@AfterMethod` ensures `tearDown()` runs even when `setUp()` or the test itself throws an exception — another critical detail. Without `alwaysRun = true`, a failed setUp skips tearDown and the thread remains dirty.

---

**Q6: What are the four parallel modes in TestNG? Explain each with when to use it.**

**A:** TestNG's `parallel` attribute in `testng.xml` has four values:

**Mode 1: `parallel="tests"` — each `<test>` block runs in its own thread**

```xml
<suite name="Suite" parallel="tests" thread-count="3">
    <test name="Login Tests - Chrome">
        <parameter name="browser" value="chrome"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
    <test name="Checkout Tests - Firefox">
        <parameter name="browser" value="firefox"/>
        <classes><class name="tests.CheckoutTest"/></classes>
    </test>
    <test name="Profile Tests - Chrome">
        <parameter name="browser" value="chrome"/>
        <classes><class name="tests.ProfileTest"/></classes>
    </test>
</suite>
```

When to use: Most common starting point. Safe — tests within a `<test>` block run serially. Use for cross-browser runs or feature-group separation.

---

**Mode 2: `parallel="classes"` — each test class runs in its own thread**

```xml
<suite name="Suite" parallel="classes" thread-count="4">
    <test name="All Tests">
        <classes>
            <class name="tests.LoginTest"/>      <!-- thread 1 -->
            <class name="tests.CheckoutTest"/>   <!-- thread 2 -->
            <class name="tests.ProfileTest"/>    <!-- thread 3 -->
            <class name="tests.SearchTest"/>     <!-- thread 4 -->
        </classes>
    </test>
</suite>
```

When to use: When you have clean class-level isolation (each class has its own `@BeforeClass` setup), and you want tests within a class to still run serially. Good middle ground.

---

**Mode 3: `parallel="methods"` — each `@Test` method runs in its own thread**

```xml
<suite name="Suite" parallel="methods" thread-count="5">
    <test name="All Tests">
        <classes><class name="tests.LoginTest"/></classes>
    </test>
</suite>
```

When to use: Maximum parallelism, maximum speed. Requires every `@Test` method to be 100% independent — no shared state whatsoever. Any `@BeforeClass` shared setup creates a race condition. Start here only after you have proven your suite is fully independent.

---

**Mode 4: `parallel="instances"` — each class instance runs in its own thread**

```xml
<suite name="Suite" parallel="instances" thread-count="2">
    <test name="All Tests">
        <classes><class name="tests.LoginTest"/></classes>
    </test>
</suite>
```

When to use: Rare. Used when you have multiple instances of the same class (via `@Factory`) and want each instance on its own thread. Useful for data-driven tests where each data row creates a class instance.

---

**Q7: What does `thread-count` mean in testng.xml? How do you choose the right value?**

**A:** `thread-count` sets the maximum number of tests that run simultaneously. It is not "how many tests in total" — it is the concurrency limit.

```
thread-count="3" with parallel="tests" and 5 <test> blocks:

Time 0s:  Test1 starts (thread 1), Test2 starts (thread 2), Test3 starts (thread 3)
          [thread pool is now full]
Time 30s: Test1 finishes → Test4 starts (thread 1 recycled)
Time 45s: Test2 finishes → Test5 starts (thread 2 recycled)
Time 60s: Test3, Test4, Test5 all finish
Wall clock: ~60s total vs ~150s serial (2.5x speedup)
```

**Choosing the right thread-count:**

```
Rule of thumb: thread-count = number of CPU cores - 1
  (leave one core for the OS and CI agent overhead)

For 4 cores: thread-count="3"
For 8 cores: thread-count="7"

In CI (e.g. GitHub Actions 2-core runner):
  thread-count="2"   (practical limit for the environment)

For Selenium Grid or cloud (BrowserStack, Sauce Labs):
  thread-count = your concurrent session limit in the plan
  (5 sessions? thread-count="5")
```

Setting `thread-count` too high causes CPU saturation — tests fight for resources, become slower, and produce more false failures. Too low wastes available capacity.

---

**Q8: How do you run the same test on Chrome AND Firefox simultaneously in TestNG?**

**A:** Pass the browser as a `<parameter>` in testng.xml, with each `<test>` block specifying a different browser value. Your `BaseTest` reads the parameter and initialises the correct WebDriver.

```xml
<!-- testng.xml -->
<suite name="Cross-Browser Suite" parallel="tests" thread-count="2">

    <test name="Login - Chrome">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="tests.LoginTest"/>
            <class name="tests.CheckoutTest"/>
        </classes>
    </test>

    <test name="Login - Firefox">
        <parameter name="browser" value="firefox"/>
        <classes>
            <class name="tests.LoginTest"/>
            <class name="tests.CheckoutTest"/>
        </classes>
    </test>

</suite>
```

```java
// BaseTest.java
@BeforeMethod
@Parameters("browser")
public void setUp(@Optional("chrome") String browser) {
    WebDriver driver;
    switch (browser.toLowerCase()) {
        case "firefox":
            WebDriverManager.firefoxdriver().setup();
            driver = new FirefoxDriver();
            break;
        case "edge":
            WebDriverManager.edgedriver().setup();
            driver = new EdgeDriver();
            break;
        default:
            WebDriverManager.chromedriver().setup();
            ChromeOptions opts = new ChromeOptions();
            opts.addArguments("--headless=new", "--no-sandbox");
            driver = new ChromeDriver(opts);
    }
    driver.manage().window().maximize();
    tlDriver.set(driver);
}
```

Both `<test>` blocks run simultaneously (thread-count="2"). LoginTest runs on Chrome in Thread 1 and on Firefox in Thread 2 at the same moment. If they both pass, you have cross-browser confidence in one parallel run.

---

**Q9: How does Playwright parallelism work? Why do you NOT need ThreadLocal?**

**A:** Playwright's parallelism model is fundamentally different from Selenium/TestNG.

**Selenium model:** Single process, multiple threads. All threads share the same JVM memory space. Without `ThreadLocal`, threads share `WebDriver` instances — race conditions occur.

**Playwright model:** Multiple isolated processes (workers). Each worker is a completely separate Node.js process with its own memory space. Processes cannot access each other's variables. There is no shared memory problem to solve.

```
Selenium parallel:
  JVM Process
    ├── Thread 1 → LoginTest
    ├── Thread 2 → CheckoutTest  ← same memory space → need ThreadLocal
    └── Thread 3 → ProfileTest

Playwright parallel:
  Worker Process 1 → login.spec.ts     ← completely isolated memory
  Worker Process 2 → checkout.spec.ts  ← completely isolated memory
  Worker Process 3 → profile.spec.ts   ← completely isolated memory
```

Playwright injects a fresh `page`, `browser`, and `context` fixture into each test automatically. These are scoped to the test — there is nothing to manage manually.

```typescript
// No setup needed — Playwright handles isolation automatically
test('login test', async ({ page }) => {
    // This `page` is unique to this test and this worker
    // Another test running simultaneously has a completely different `page`
    await page.goto('/login');
    await page.fill('#username', 'admin');
    // ...
});
```

---

**Q10: What is `fullyParallel` in Playwright? How is it different from the default?**

**A:** By default, Playwright runs different test FILES in parallel (each file on a separate worker), but tests within a single file run serially — one after another on the same worker.

`fullyParallel: true` makes Playwright run every individual test in parallel regardless of which file it belongs to.

```typescript
// playwright.config.ts
export default defineConfig({
    // DEFAULT behaviour: files in parallel, tests within a file: serial
    workers: 4,

    // FULLY PARALLEL: every test runs in parallel, even within the same file
    fullyParallel: true,
    workers: 4,
});
```

```typescript
// You can also control this per describe block:
test.describe('Login tests', () => {
    test.describe.configure({ mode: 'parallel' });  // this block runs in parallel

    test('valid login', async ({ page }) => { ... });
    test('invalid password', async ({ page }) => { ... });  // runs at same time as above
});

// Or force serial for a block that shares state:
test.describe('Order flow', () => {
    test.describe.configure({ mode: 'serial' });  // step 1 must complete before step 2

    test('add to cart', async ({ page }) => { ... });
    test('proceed to checkout', async ({ page }) => { ... });
});
```

**When to use `fullyParallel: true`:** When every test is independently written and does not rely on any shared state within a file. Gives maximum speed. When tests within a file share setup state or have ordering dependencies, keep the default or use `mode: 'serial'` for those describe blocks.

---

**Q11: How do you share authentication state across Playwright workers without re-logging in for every test?**

**A:** Playwright has a built-in mechanism called "storage state" — you log in once in a setup project, save the browser storage (cookies + localStorage), and all workers reuse it.

```typescript
// auth.setup.ts — runs ONCE before all other tests
import { test as setup } from '@playwright/test';
import path from 'path';

const AUTH_FILE = path.join(__dirname, '../playwright/.auth/user.json');

setup('authenticate as standard user', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#username', process.env.TEST_USERNAME!);
    await page.fill('#password', process.env.TEST_PASSWORD!);
    await page.click('#loginBtn');
    await page.waitForURL('/dashboard');

    // Save the authenticated session state (cookies + localStorage)
    await page.context().storageState({ path: AUTH_FILE });
});
```

```typescript
// playwright.config.ts — wire up the dependency
export default defineConfig({
    fullyParallel: true,
    workers: 4,
    projects: [
        // Setup project — runs first, once, not in parallel
        {
            name: 'setup',
            testMatch: /auth\.setup\.ts/,
        },
        // All other tests — depend on setup, reuse saved auth
        {
            name: 'tests',
            use: {
                storageState: 'playwright/.auth/user.json',
            },
            dependencies: ['setup'],   // waits for setup to complete
        },
        // Admin tests with different auth
        {
            name: 'admin-tests',
            use: {
                storageState: 'playwright/.auth/admin.json',
            },
            dependencies: ['setup-admin'],
        },
    ],
});
```

```
# Add auth file to .gitignore — contains session cookies
playwright/.auth/
```

Without this pattern, every test would log in fresh — adding a full login sequence (2-3 seconds) to every test. With 100 tests, that is 5 minutes of pure login overhead eliminated.

---

**Q12: What is the most common cause of test interference in parallel runs?**

**A:** Shared test data is the single most common source of parallel test interference. Specifically: two tests using the same user account, the same email address, the same product ID, or the same record — and one test's action breaking the state that the other test expects.

**Classic example:**

```
Test A: create user "test@example.com", assert created successfully
Test B: create user "test@example.com", assert created successfully

Running serially: Test A passes, Test B gets 409 Conflict → failure caught
Running in parallel: Test A and Test B send the request simultaneously
  → Race condition: one gets 201, one gets 409 → flaky, non-deterministic
```

**Fix — unique data per test:**

```java
// Selenium / Java
String email = "user_" + System.currentTimeMillis() + "_" 
             + Thread.currentThread().getId() + "@test.com";
// Thread ID ensures uniqueness even if two tests start at the same millisecond
```

```typescript
// Playwright / TypeScript
const email = `user_${Date.now()}_${Math.random().toString(36).slice(2)}@test.com`;
```

**Other common causes of interference:**
- Tests sharing a user account and one test logs out
- Tests sharing a cart/order and one test clears it
- File name collisions for screenshots or downloaded files
- Port conflicts when tests start local mock servers
- Database state — one test deletes a record another test is reading
- Global application state — one test changes a setting that affects all sessions

---

**Q13: How do you create unique test data for parallel test runs?**

**A:** Three strategies in order of preference:

**Strategy 1 — Timestamp + Thread ID (simplest, good for most cases):**

```java
// Java — email unique per test run and per thread
String email = String.format("user_%d_%d@test.com",
    System.currentTimeMillis(),
    Thread.currentThread().getId());

// Java — username
String username = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
```

```typescript
// TypeScript — Playwright
const email = `user_${Date.now()}@test.com`;
const username = `user_${crypto.randomUUID().slice(0, 8)}`;

// Or use a test-info fixture for test-specific names
test('create user', async ({ page }, testInfo) => {
    const email = `user_${testInfo.testId}@test.com`;
    // testInfo.testId is unique per test
});
```

**Strategy 2 — API setup + cleanup (isolated data per test):**

```java
@BeforeMethod
public void createTestUser() {
    // Create a fresh user via API for this test only
    String uniqueEmail = "user_" + UUID.randomUUID() + "@test.com";
    userId = userApiHelper.createUser(uniqueEmail, "TestPass@123");
}

@AfterMethod(alwaysRun = true)
public void deleteTestUser() {
    userApiHelper.deleteUser(userId);  // clean up after test
}
```

**Strategy 3 — Test data partitioning (for large suites):**

```
Worker 1 operates on user IDs 1–1000
Worker 2 operates on user IDs 1001–2000
Worker 3 operates on user IDs 2001–3000
```

Requires infrastructure support but gives clean isolation and no UUID-generated names.

---

**Q14: A test passes when run serially but fails in parallel. What do you investigate?**

**A:** Systematic debugging process — five things to check in order:

```
Step 1: Confirm it is a parallelism problem
  npx playwright test --workers=1    (or mvn test -Dparallel=none)
  If it passes serially → confirmed parallel issue

Step 2: Minimum reproduction
  npx playwright test --workers=2
  Run only the two tests you suspect are interfering
  Identify which pair of tests causes the failure

Step 3: Check for these root causes (in order of likelihood):

  a) Shared test data
     → Are both tests using the same email/user/product?
     → Fix: unique data per test (timestamp, UUID)

  b) Test order dependency
     → Does the failing test assume another test ran first?
     → Fix: each test must be fully self-contained with its own setup

  c) Shared application state
     → Does one test log out, change settings, or clear cart that affects others?
     → Fix: use beforeEach to reset state, or use separate user accounts

  d) Static shared variables in test code
     → Are there static fields (not ThreadLocal) being written by multiple threads?
     → Fix: make them ThreadLocal or instance variables

  e) File name collision
     → Are screenshots or downloaded files using the same name?
     → Fix: include thread ID and timestamp in file names

  f) Port conflict
     → Does each test start a mock server on the same port?
     → Fix: use random ports or a shared singleton mock server

Step 4: Add diagnostic logging
  System.out.println("[Thread " + Thread.currentThread().getId() + "] email = " + email);
  Watch the logs when two tests run simultaneously

Step 5: Verify fix
  Run with workers=5 multiple times — parallel failures are often non-deterministic
  A fix must make it pass consistently across multiple runs, not just once
```

---

**Q15: How do you prevent screenshot file name collisions in parallel tests?**

**A:** If multiple parallel tests fail at the same moment and all write to `screenshots/failure.png`, they overwrite each other. Only the last screenshot survives. You lose evidence for all other failures.

```java
// WRONG — same filename for all failures
@AfterMethod
public void takeScreenshotOnFailure(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE) {
        TakesScreenshot ts = (TakesScreenshot) getDriver();
        File shot = ts.getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(shot, new File("screenshots/failure.png"));  // COLLISION!
    }
}

// CORRECT — unique filename per test, per thread, per timestamp
@AfterMethod(alwaysRun = true)
public void takeScreenshotOnFailure(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE) {
        TakesScreenshot ts = (TakesScreenshot) getDriver();
        File shot = ts.getScreenshotAs(OutputType.FILE);

        String filename = String.format("screenshots/FAIL_%s_%d_%d.png",
            result.getName(),                      // test method name
            Thread.currentThread().getId(),        // thread ID
            System.currentTimeMillis());           // timestamp (ms)

        FileUtils.copyFile(shot, new File(filename));
        System.out.println("Screenshot: " + filename);
    }
}
```

```typescript
// Playwright handles this automatically
// playwright.config.ts
use: {
    screenshot: 'only-on-failure',
    // Playwright names screenshots: test-name-{hash}.png — guaranteed unique
}
```

Playwright's built-in screenshot-on-failure generates unique filenames automatically per test worker. For Selenium, you must implement the uniqueness yourself — always include the thread ID and timestamp.

---

## SECTION 2 — Negative Testing

---

**Q16: What is negative testing? Why is it as important as positive testing?**

**A:** Negative testing intentionally provides invalid, unexpected, or out-of-bounds inputs to verify the system handles them correctly — that it fails gracefully, rejects the input cleanly, and does not crash, expose data, or silently corrupt state.

**Positive test (happy path):** enter valid email and password → user logs in → redirected to dashboard.

**Negative tests (everything else that could happen):**
- Empty email → "Email is required" error message shown
- Empty password → "Password is required" error message shown
- Wrong password → "Invalid credentials" — not "password is wrong" (security)
- Email without @ → "Enter a valid email" format error
- SQL injection in username → rejected or treated as plain text, no server crash, no login bypass
- XSS script tag in name field → not executed in browser
- Correct email, 10,000-character password → rejected cleanly, no 500 error

**Why negative testing is equally important:**

```
Happy path tests catch obvious bugs — the features that work for the average user.
Negative tests catch the bugs that reach production:
  - The attacker who enters SQL injection in the login field
  - The user who double-clicks submit and places two orders
  - The API call with a missing field that crashes the server
  - The import that silently truncates data at 255 characters

Senior QA insight: happy path coverage alone = you only tested what you hoped for.
                  Negative coverage = you tested what users and attackers will actually do.
```

---

**Q17: What is boundary value analysis? Give step-by-step test cases for an age field accepting 18–65.**

**A:** Boundary Value Analysis (BVA) is a test design technique based on the observation that bugs concentrate at the boundaries of valid ranges — the edges where logic transitions between "accept" and "reject." BVA systematically tests at and around every boundary.

**For an age field accepting 18–65:**

The boundaries are: 18 (lower), 65 (upper).
BVA says: test at the boundary, one below, and one above each boundary.

```
Boundary points to test:
  17  → just below lower boundary → INVALID → expect 400 / error message
  18  → lower boundary            → VALID   → expect 200 / success
  19  → just above lower boundary → VALID   → expect 200 / success
  64  → just below upper boundary → VALID   → expect 200 / success
  65  → upper boundary            → VALID   → expect 200 / success
  66  → just above upper boundary → INVALID → expect 400 / error message

Extra cases always worth adding:
  0   → zero          → INVALID
  -1  → negative      → INVALID
  -99 → large negative → INVALID (sometimes causes int overflow bugs)
```

```java
// Java + Rest Assured + TestNG
@DataProvider(name = "ageBoundaries")
public Object[][] ageBoundaries() {
    return new Object[][] {
        // { age, expectedStatus, description }
        { 17,  400, "just below minimum — invalid" },
        { 18,  201, "minimum boundary — valid"     },
        { 19,  201, "just above minimum — valid"   },
        { 64,  201, "just below maximum — valid"   },
        { 65,  201, "maximum boundary — valid"     },
        { 66,  400, "just above maximum — invalid" },
        { 0,   400, "zero — invalid"               },
        { -1,  400, "negative — invalid"           },
    };
}

@Test(dataProvider = "ageBoundaries")
public void ageField_boundaryValues(int age, int expectedStatus, String description) {
    given()
        .spec(requestSpec)
        .body("{ \"age\": " + age + ", \"name\": \"Test\" }")
    .when()
        .post("/users")
    .then()
        .statusCode(expectedStatus);
}
```

---

**Q18: How do you test empty required fields step by step?**

**A:**

**What to test:** what happens when a user submits a form with one or more required fields left empty?

**Expected behaviour:** validation error shown, form NOT submitted, user stays on the same page, data in other fields is preserved.

**Step-by-step:**

```
1. Navigate to the form
2. Leave the target required field empty
3. Fill all other required fields with valid data
4. Click Submit
5. Assert: validation error message is visible
6. Assert: error message text is correct and helpful (e.g. "Email is required" — not "Error")
7. Assert: user did NOT navigate away (still on the same URL or still on same page)
8. Assert: other fields the user filled are NOT cleared (data preservation)
9. Repeat steps 2–8 for each required field individually
10. Repeat with ALL required fields empty simultaneously
```

```java
@Test
public void login_emptyUsername_showsValidationError() {
    loginPage.navigate();
    loginPage.enterPassword("Admin@123");    // fill password, leave username empty
    loginPage.clickSubmit();

    // Assert error is shown
    Assert.assertTrue(loginPage.getUsernameError().isDisplayed(),
        "Username error message should be visible when username is empty");

    // Assert message is specific
    Assert.assertEquals(loginPage.getUsernameError().getText(),
        "Username is required",
        "Error message text should match spec");

    // Assert user stays on login page
    Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
        "User should remain on login page after failed submission");

    // Assert password field was not cleared
    Assert.assertEquals(loginPage.getPasswordField().getAttribute("value"), "Admin@123",
        "Password field should be preserved after validation error");
}
```

```typescript
// Playwright
test('empty email shows specific validation error', async ({ page }) => {
    await page.goto('/register');
    await page.fill('#name', 'John Smith');   // fill other fields
    await page.fill('#password', 'Pass@123');
    // leave #email empty
    await page.click('#submitBtn');

    await expect(page.locator('#email-error')).toBeVisible();
    await expect(page.locator('#email-error')).toHaveText('Email is required');
    await expect(page).toHaveURL(/\/register/);
    await expect(page.locator('#name')).toHaveValue('John Smith');  // preserved
});
```

---

**Q19: How do you test maximum length exceeded step by step?**

**A:**

**What to test:** what happens when input exceeds the maximum allowed length for a field?

**Expected behaviour:** either field stops accepting characters at the max (HTML `maxlength` attribute), or an error is shown on submit. Silent truncation (accepts 10,000 chars, saves only 255) is a bug.

**Step-by-step:**

```
1. Find the maximum length from the spec, or inspect the HTML maxlength attribute
2. Prepare test strings:
   - Exactly max length (max chars)     → should PASS
   - Max length + 1 character          → should be rejected or prevented
   - Very large string (10,000+ chars) → must NOT crash the server
3. Enter the max-length string → submit → assert: accepted
4. Enter max+1 string → submit → assert: rejected OR field prevented entry
5. After submission for max+1, query the API directly:
   - Verify the actual saved value equals what was entered (no silent truncation)
6. Enter 10,000 characters → submit → assert: no 500 error
```

```java
@Test
public void username_atMaxLength_accepted() {
    String maxUsername = "a".repeat(50);   // spec says max 50 chars
    registrationPage.navigate();
    registrationPage.enterUsername(maxUsername);
    registrationPage.fillRequiredFields();
    registrationPage.clickSubmit();

    // Verify the field accepted the full max value
    Assert.assertEquals(
        registrationPage.getUsernameField().getAttribute("value"),
        maxUsername,
        "Max length username should be accepted in full — no truncation");
}

@Test
public void username_exceedsMaxLength_rejected() {
    String tooLong = "a".repeat(51);
    registrationPage.navigate();
    registrationPage.enterUsername(tooLong);

    // The field should either truncate (show maxlength attribute enforcement)
    // or show an error
    String actualValue = registrationPage.getUsernameField().getAttribute("value");
    Assert.assertTrue(actualValue.length() <= 50,
        "Username exceeding max length should be rejected, not silently truncated beyond 50");

    // Also verify: no 500 error on submit with overlong input
    registrationPage.fillRequiredFields();
    registrationPage.clickSubmit();
    Assert.assertFalse(
        driver.getPageSource().contains("Internal Server Error"),
        "Server must not crash on overlong input");
}

@Test
public void username_veryLargeInput_doesNotCrashServer() {
    String veryLong = "a".repeat(10_000);
    given().spec(requestSpec)
        .body("{ \"username\": \"" + veryLong + "\" }")
    .when()
        .post("/users")
    .then()
        .statusCode(anyOf(equalTo(400), equalTo(422)))  // rejected cleanly
        .statusCode(not(equalTo(500)));                 // NOT a server crash
}
```

---

**Q20: How do you test an invalid email format (without @) step by step?**

**A:**

```
1. Navigate to the form with an email field
2. Enter each invalid format — one test per format:
   - "notanemail"     (no @ symbol)
   - "@.com"          (@ at start, no local part)
   - "a@b"            (domain with no TLD)
   - "a @b.com"       (space inside)
   - "a@b@c.com"      (double @)
   - ""               (empty — covered in empty field test)
3. Trigger validation (tab away OR click submit)
4. Assert: specific format error shown ("Please enter a valid email address")
5. Assert: NOT a generic error ("Something went wrong", 500)
6. Assert: user stays on the page
7. Enter a valid email → assert error disappears (error state is cleared)
```

```java
@DataProvider(name = "invalidEmailFormats")
public Object[][] invalidEmailFormats() {
    return new Object[][] {
        { "notanemail",  "Please enter a valid email address" },
        { "@.com",       "Please enter a valid email address" },
        { "a@b",         "Please enter a valid email address" },
        { "a @b.com",    "Please enter a valid email address" },
        { "a@b@c.com",   "Please enter a valid email address" },
    };
}

@Test(dataProvider = "invalidEmailFormats")
public void emailField_invalidFormat_showsSpecificError(String email, String expectedMsg) {
    registrationPage.navigate();
    registrationPage.enterEmail(email);
    registrationPage.clickAway();   // trigger blur/onBlur validation

    Assert.assertTrue(registrationPage.getEmailError().isDisplayed(),
        "Email error should be visible for: " + email);
    Assert.assertEquals(registrationPage.getEmailError().getText(), expectedMsg,
        "Error message should match for: " + email);
}
```

---

**Q21: How do you test SQL injection step by step? What is the expected result?**

**A:**

**What to test:** entering SQL injection strings in input fields (login, search, name fields) must not bypass authentication, crash the server, or return unintended data.

**Expected result:** the server either rejects the input with a 400/validation error, or treats it as plain text. Never: authentication bypass, 500 crash, or database error exposed to user.

**Step-by-step:**

```
1. Identify all text input fields (login, search, name, comment, filter)
2. Prepare SQL injection payloads:
   - ' OR '1'='1          (classic auth bypass)
   - ' OR 1=1 --          (comment-based bypass)
   - '; DROP TABLE users; -- (destructive)
   - ' UNION SELECT * FROM users -- (data extraction)
   - admin'--             (single comment injection)
3. For each payload in each field:
   a. Enter the SQL injection string
   b. Submit the form or trigger the action
   c. Assert: NOT logged in (URL does not contain /dashboard)
   d. Assert: NOT a 500 error ("Internal Server Error" not in page source)
   e. Assert: NOT a raw SQL error ("SQL syntax error", "ORA-", "mysql" not in page source)
   f. Assert: treated as plain text OR rejected with validation error
4. For API: send injection in body JSON, assert 400 or input treated as string
```

```java
@DataProvider(name = "sqlInjections")
public Object[][] sqlInjections() {
    return new Object[][] {
        { "' OR '1'='1"           },
        { "' OR 1=1 --"           },
        { "'; DROP TABLE users;--" },
        { "' UNION SELECT * FROM users --" },
        { "admin'--"              },
    };
}

@Test(dataProvider = "sqlInjections")
public void sqlInjection_doesNotBypassAuth(String injection) {
    loginPage.navigate();
    loginPage.enterUsername(injection);
    loginPage.enterPassword("anything");
    loginPage.clickSubmit();

    // Must NOT bypass authentication
    Assert.assertFalse(driver.getCurrentUrl().contains("/dashboard"),
        "SQL injection must not bypass login for: " + injection);

    // Must NOT expose SQL errors
    String pageSource = driver.getPageSource();
    Assert.assertFalse(pageSource.contains("SQL syntax error"),
        "SQL error must not be exposed to user");
    Assert.assertFalse(pageSource.contains("Internal Server Error"),
        "Server error must not be exposed");
    Assert.assertFalse(pageSource.contains("ORA-"),
        "Oracle error must not be exposed");
}
```

---

**Q22: How do you test XSS step by step? How do you verify a script did not execute?**

**A:**

**What to test:** entering `<script>` tags or other XSS payloads in user-input fields that are later displayed back to users. The script must NOT execute in any user's browser.

**Step-by-step:**

```
1. Identify fields where input is displayed back (profile name, comment, bio, search term)
2. Enter XSS payloads:
   - <script>alert('xss')</script>
   - <img src=x onerror="alert('xss')">
   - <svg onload="alert('xss')">
   - javascript:alert('xss')
3. Submit and save the input
4. Navigate to the page where the value is displayed
5. Set up a dialog listener BEFORE the navigation (alert = XSS executed)
6. Wait 1-2 seconds after navigation (time for any script to fire)
7. Assert: NO alert dialog appeared (script did not execute)
8. Assert: the raw text is displayed safely (as escaped HTML) or not displayed at all
9. Inspect the HTML source: the < > characters should be escaped as &lt; &gt;
```

```typescript
test('XSS payload in display name does not execute', async ({ page }) => {
    // Set up dialog listener BEFORE navigating to display page
    let xssExecuted = false;
    page.on('dialog', async dialog => {
        xssExecuted = true;
        await dialog.dismiss();   // close any dialog that appears
    });

    // Submit XSS payload
    await page.goto('/profile/edit');
    await page.fill('#displayName', "<script>alert('xss')</script>");
    await page.click('#saveBtn');
    await page.waitForResponse('/api/profile');

    // Navigate to where the name is displayed
    await page.goto('/profile/view');
    await page.waitForTimeout(2000);   // give script time to fire if not escaped

    // Assert: script did not execute
    expect(xssExecuted).toBe(false);

    // Assert: text is displayed safely (as escaped text, not as HTML)
    const displayedText = await page.locator('#displayName').textContent();
    expect(displayedText).toContain('<script>');   // shown as text, not executed as HTML

    // Inspect source: & should be escaped
    const pageContent = await page.content();
    expect(pageContent).toContain('&lt;script&gt;');  // properly escaped
});
```

```java
// Selenium + JavaScript Alert detection
@Test(dataProvider = "xssPayloads")
public void xss_doesNotExecuteScript(String payload) {
    profilePage.navigate();
    profilePage.enterDisplayName(payload);
    profilePage.clickSave();

    profilePage.navigateToView();

    try {
        Alert alert = new WebDriverWait(getDriver(), Duration.ofSeconds(2))
            .until(ExpectedConditions.alertIsPresent());
        alert.dismiss();
        Assert.fail("XSS script executed — alert appeared for payload: " + payload);
    } catch (TimeoutException e) {
        // PASS — no alert appeared — script did not execute
    }
}
```

---

**Q23: How do you test double-submit (clicking Submit twice quickly)? What is the expected result?**

**A:**

**What to test:** user accidentally double-clicks the submit button, or clicks once and impatiently clicks again before the response arrives.

**Expected result:** exactly ONE record is created / ONE action performed. The second click should be ignored — either because the button is disabled after the first click, or because the backend is idempotent, or because there is a duplicate check.

**Real-world stakes:** on a payment form, double-submit can charge the card twice. On an order form, it can create two identical orders. This is a high-severity bug category.

**Step-by-step:**

```
1. Fill the form completely
2. Click submit — immediately click again without waiting for response
   (or use double-click if appropriate to the scenario)
3. Wait for complete response
4. Assert: only ONE record was created — check via:
   a. The count displayed on screen (e.g. "Orders: 1")
   b. A GET API call to count records with your test data
   c. A direct database query (if test environment allows)
5. Assert: button was disabled/changed state after first click (UI protection)
6. For payment: assert ONE charge in payment processor (use sandbox)
```

```typescript
test('double submit creates only one order', async ({ page }) => {
    await page.goto('/checkout');
    await fillOrderDetails(page);

    // Double-click submit (both clicks happen before the first response arrives)
    await page.dblclick('#submitOrder');
    // OR: two clicks in rapid succession
    // await page.click('#submitOrder');
    // await page.click('#submitOrder');  // second click

    await page.waitForURL('/order-confirmation');

    // Assert: only ONE order created — check via API
    const response = await page.request.get('/api/orders?email=test@test.com');
    const orders = await response.json();
    expect(orders.length).toBe(1);
    expect(orders[0].status).toBe('confirmed');
});
```

```java
@Test
public void checkout_doubleClick_createsOnlyOneOrder() {
    checkoutPage.fillOrderDetails();

    // Double click — fire two click events in rapid succession
    Actions actions = new Actions(getDriver());
    actions.doubleClick(checkoutPage.getSubmitButton()).perform();
    // Or:
    checkoutPage.getSubmitButton().click();
    checkoutPage.getSubmitButton().click();  // second click before response

    // Wait for confirmation
    checkoutPage.waitForConfirmation();

    // Verify via API — only 1 order should exist for this test user
    int orderCount = orderApiHelper.getOrderCountForUser(testUser.getId());
    Assert.assertEquals(orderCount, 1, "Only one order should be created on double submit");
}
```

---

## SECTION 3 — API Negative Testing

---

**Q24: How do you test an API with a missing required field? What are the exact assertions?**

**A:**

**What to test:** sending a POST request with a required field omitted.

**Expected:** 400 Bad Request (or 422 Unprocessable Entity), with a response body that specifically identifies which field is missing.

**Step-by-step:**

```
1. Identify all required fields in the request body (from API spec / OpenAPI schema)
2. For each required field:
   a. Send a request with ONLY that field omitted (all others present and valid)
   b. Assert status: 400 or 422 (NEVER 200 or 201, NEVER 500)
   c. Assert body: error message mentions the specific missing field name
   d. Assert body: no stack trace, no SQL error, no internal class names
3. Send a request with ALL required fields omitted
4. Send an entirely empty body {}
5. Send a null body
```

```java
@Test
public void createUser_missingEmail_returns400WithFieldIdentified() {
    String bodyWithoutEmail = "{ \"name\": \"John\", \"password\": \"Pass@123\" }";

    given()
        .spec(requestSpec)
        .body(bodyWithoutEmail)
    .when()
        .post("/api/users")
    .then()
        .statusCode(400)
        .body("error", containsString("email"))            // field name in error
        .body("error", not(containsString("stack")))       // no stack trace
        .body("error", not(containsString("Exception")))   // no exception class
        .body("error", not(containsString("null")));       // no null pointer exposed
}

@DataProvider(name = "missingRequiredFields")
public Object[][] missingRequiredFields() {
    return new Object[][] {
        { "{ \"name\": \"John\", \"password\": \"Pass@123\" }",       "email"    },
        { "{ \"email\": \"j@t.com\", \"password\": \"Pass@123\" }",   "name"     },
        { "{ \"name\": \"John\", \"email\": \"j@t.com\" }",           "password" },
        { "{}",                                                         "email"    },
    };
}

@Test(dataProvider = "missingRequiredFields")
public void createUser_missingField_returns400(String body, String missingField) {
    given().spec(requestSpec).body(body)
    .when().post("/api/users")
    .then()
        .statusCode(anyOf(equalTo(400), equalTo(422)))
        .body("error", containsString(missingField));
}
```

---

**Q25: How do you test an API with the wrong data type for a field?**

**A:**

**What to test:** sending a string where an integer is expected, a boolean where a string is expected, an array where an object is expected.

**Expected:** 400 (never 500). Error message is descriptive — not an internal stack trace.

```java
@DataProvider(name = "wrongTypes")
public Object[][] wrongTypes() {
    return new Object[][] {
        { "{ \"name\": \"Widget\", \"price\": \"expensive\", \"stock\": 10 }", "price" },
        { "{ \"name\": 123, \"price\": 9.99, \"stock\": 10 }",                 "name"  },
        { "{ \"name\": \"Widget\", \"price\": 9.99, \"stock\": \"many\" }",    "stock" },
        { "{ \"name\": \"Widget\", \"price\": null, \"stock\": 10 }",          "price" },
    };
}

@Test(dataProvider = "wrongTypes")
public void createProduct_wrongDataType_returns400(String body, String invalidField) {
    given()
        .spec(requestSpec)
        .body(body)
    .when()
        .post("/api/products")
    .then()
        .statusCode(400)
        .body("error", not(containsString("NullPointerException")))
        .body("error", not(containsString("ClassCastException")))
        .body("error", not(containsString("Internal Server")))
        .body("error", containsString(invalidField));  // field identified in error
}
```

*Why "NEVER 500" is a hard rule:* A 500 means an exception reached the HTTP layer unhandled. Sending invalid types to crash a server is the first step in many attack patterns. The server MUST validate input types before they reach business logic or the database.

---

**Q26: How do you test all authentication failure scenarios for an API?**

**A:** Authentication failures have four distinct cases — test each separately because they can have different root causes even when they share the same 401 response code.

```java
// ── Case 1: No token at all → 401 Unauthorized ────────────────────────────
@Test
public void protectedEndpoint_noToken_returns401() {
    given()
        // No Authorization header
    .when()
        .get("/api/orders")
    .then()
        .statusCode(401)
        .body("error", containsString("unauthorized"));
}

// ── Case 2: Wrong token format → 401 ─────────────────────────────────────
@Test
public void protectedEndpoint_malformedToken_returns401() {
    given()
        .header("Authorization", "NotBearer xyz.abc.def")
    .when()
        .get("/api/orders")
    .then()
        .statusCode(anyOf(equalTo(401), equalTo(400)));
}

// ── Case 3: Valid format but wrong/fake token → 401 ───────────────────────
@Test
public void protectedEndpoint_fakeToken_returns401() {
    given()
        .header("Authorization", "Bearer fake.token.here")
    .when()
        .get("/api/orders")
    .then()
        .statusCode(401);
}

// ── Case 4: Valid token but wrong role → 403 Forbidden ────────────────────
@Test
public void adminEndpoint_viewerToken_returns403() {
    String viewerToken = authHelper.getTokenForRole("viewer");
    given()
        .header("Authorization", "Bearer " + viewerToken)
        .pathParam("id", 1)
    .when()
        .delete("/api/admin/users/{id}")
    .then()
        .statusCode(403)
        .body("error", containsString("forbidden"));
}

// ── Case 5: Expired token → 401 ───────────────────────────────────────────
@Test
public void protectedEndpoint_expiredToken_returns401() {
    String expiredToken = authHelper.generateExpiredToken();
    given()
        .header("Authorization", "Bearer " + expiredToken)
    .when()
        .get("/api/orders")
    .then()
        .statusCode(401)
        .body("error", anyOf(containsString("expired"), containsString("invalid")));
}
```

*Key distinction:* 401 means "I don't know who you are" (authentication failure). 403 means "I know who you are but you are not allowed" (authorization failure). Tests for both must exist separately.

---

**Q27: How do you test 404 for non-existent resources?**

**A:**

```java
@DataProvider(name = "nonExistentIds")
public Object[][] nonExistentIds() {
    return new Object[][] {
        { 0 },               // zero — typically no resource has ID 0
        { -1 },              // negative ID
        { 99999999 },        // very large ID unlikely to exist
        { Integer.MAX_VALUE }, // maximum integer value
    };
}

@Test(dataProvider = "nonExistentIds")
public void getUser_nonExistentId_returns404(int id) {
    given()
        .spec(authedRequestSpec)
        .pathParam("id", id)
    .when()
        .get("/api/users/{id}")
    .then()
        .statusCode(404)
        .body("error", containsString("not found"))
        .body("error", not(containsString("NullPointerException")));  // no crash
}

@Test
public void getUser_nonExistentEmail_returns404() {
    given()
        .spec(authedRequestSpec)
    .when()
        .get("/api/users?email=doesnotexist_" + System.currentTimeMillis() + "@test.com")
    .then()
        .statusCode(404);
}

@Test
public void getDeletedResource_returns404() {
    // Create, then delete, then try to get it
    int userId = userHelper.createUser();
    userHelper.deleteUser(userId);

    given()
        .spec(authedRequestSpec)
        .pathParam("id", userId)
    .when()
        .get("/api/users/{id}")
    .then()
        .statusCode(404);  // deleted resource must return 404, not 200 with null body
}
```

---

**Q28: How do you test 409 Conflict for duplicate resources?**

**A:** A 409 Conflict should be returned when creating a resource that violates a uniqueness constraint (same email, same username, same order number).

```java
@Test
public void createUser_duplicateEmail_returns409() {
    String uniqueEmail = "test_" + System.currentTimeMillis() + "@test.com";
    String requestBody = "{ \"email\": \"" + uniqueEmail + "\", \"name\": \"Test\" }";

    // First creation → 201 Created
    given()
        .spec(requestSpec)
        .body(requestBody)
    .when()
        .post("/api/users")
    .then()
        .statusCode(201);

    // Second creation with identical email → 409 Conflict
    given()
        .spec(requestSpec)
        .body(requestBody)
    .when()
        .post("/api/users")
    .then()
        .statusCode(409)
        .body("error", containsString("already exists"))
        .body("error", not(containsString("500")));
}

@Test
public void bulkCreate_duplicateWithinBatch_returns409() {
    // Some APIs must detect duplicates within a batch request
    String body = "{ \"users\": ["
        + "{ \"email\": \"dup@test.com\", \"name\": \"User 1\" },"
        + "{ \"email\": \"dup@test.com\", \"name\": \"User 2\" }"   // duplicate
        + "]}";

    given()
        .spec(requestSpec)
        .body(body)
    .when()
        .post("/api/users/bulk")
    .then()
        .statusCode(anyOf(equalTo(409), equalTo(400)));  // depends on spec
}
```

---

**Q29: What is the decision tree for handling any negative scenario you discover?**

**A:**

```
STEP 1 — WHAT is the input type?
  → Empty / null
  → Wrong data type (string where int expected)
  → Out of range / boundary violation
  → Invalid format (email without @, phone with letters)
  → Malicious (SQL injection, XSS, path traversal)
  → Duplicate (same email, same username)
  → Missing required field
  → Conflicting state (booking same slot twice)

STEP 2 — WHAT does the spec say should happen?
  → Check acceptance criteria
  → Check API contract / OpenAPI schema
  → Not documented? → ask the developer/PO and document the answer as a test
  → Assume sensible default: bad input should be rejected, never silently accepted

STEP 3 — WHAT does the system actually do?
  → Run the test
  → Observe: status code, response body, page behaviour, database state

STEP 4 — IS THE ACTUAL RESULT ACCEPTABLE?

  PASS scenarios:
  → 400/422 with a helpful error message identifying the invalid field
  → Input rejected before reaching the database
  → Error message is user-friendly (not a stack trace)

  BUG scenarios:
  → 500 Server Error on bad input           → always a bug (server crashed)
  → 200 with silent failure (empty result)  → always a bug (no feedback to user)
  → 200 with incorrect/dangerous result     → always a bug
  → Stack trace or SQL error in response    → always a bug (information disclosure)
  → Authentication bypass via injection     → critical security bug
  → XSS executing in browser               → critical security bug

STEP 5 — SEVERITY CLASSIFICATION:
  → SQL injection bypass / XSS execution   → Critical
  → 500 on bad input                       → High (potential security risk)
  → Silent acceptance of invalid data      → High (data integrity)
  → Missing validation                     → Medium
  → Wrong error message / poor UX          → Low

STEP 6 — AUTOMATE IT
  → Write the test before raising the bug
  → The test documents the expected behaviour
  → The test is a regression check — it must run on every build
  → Negative tests are permanent — never delete them after the bug is fixed
```

---

**Q30: Why is a 500 error on bad user input always a bug?**

**A:** A 500 Internal Server Error means the server threw an unhandled exception. It crashed. This should never happen as a result of user input, no matter how malformed or malicious that input is.

**Why it is always a bug:**

```
1. Security risk:
   If a tester can cause a 500 with bad input, an attacker can too.
   Repeated 500s can be used to:
   - Cause denial of service (repeated bad requests crash the server repeatedly)
   - Probe the system: the specific 500 message may reveal internal details
     (stack traces, class names, SQL queries, file paths)

2. Defensive programming violation:
   Input validation must happen at the API boundary.
   If bad input reaches the database or business logic layers,
   the validation layer was missing or insufficient.

3. The contract violation:
   The HTTP spec defines what status codes mean.
   500 means "the server itself has a problem".
   400/422 means "the request itself has a problem".
   Bad user input is a 400 problem — never a 500 problem.

4. User experience:
   A 500 gives the user zero actionable information.
   "Internal Server Error" tells them nothing about what they did wrong.
   A 400 with "email is required" tells them exactly how to fix it.
```

**What should happen instead:**

```
Bad input arrives at API →
  Input validation layer catches it →
    Returns 400 with: { "error": "email field is required" } →
      Bad input never reaches database or business logic
```

Every time you find a 500 on bad input, the root cause is: the validation layer is absent or incomplete. Always report it as High or Critical severity — it is a coding defect, not just a UX issue.

---

**Q31: How do you prioritise negative tests when time is limited?**

**A:** Prioritise by the cost of the bug if it reaches production:

```
PRIORITY 1 — Security inputs (always test these, no exceptions):
  - SQL injection in all text fields and API parameters
  - XSS in all fields that are displayed back to users
  - Authentication bypass: missing token, expired token, wrong role
  - Unauthorised access to other users' data (IDOR — pass another user's ID)
  Why: these have the highest impact if missed. One finding justifies the entire test effort.

PRIORITY 2 — Required field validation:
  - Empty required fields (both UI and API)
  - Missing required fields in API body
  Why: data integrity failures. Invalid records reach the database silently.

PRIORITY 3 — Boundary values for numeric and length fields:
  - Age/price/quantity: at boundaries and just outside
  - String length: at max, max+1, very large
  Why: off-by-one bugs are extremely common and often escape unit tests.

PRIORITY 4 — Duplicate and conflict scenarios:
  - Creating two resources with the same unique key (email, username)
  - Race conditions: two users claiming the last item simultaneously
  Why: broken without proper database constraints or optimistic locking.

PRIORITY 5 — Format validation:
  - Email format, phone format, date format
  - Currency and number formatting (comma vs period)
  Why: lower risk if server-side validation exists, but user-facing impact is high.

SKIP when time is critically limited:
  - Deeply unusual combinations with no realistic user or attacker path
  - Validation that is purely frontend (JS) with confirmed server-side backup
  - Fields with no security, data, or business logic implications
```

---

## SECTION 4 — 10 Interview Q&A

---

**Q32: What is ThreadLocal and why is it needed for parallel Selenium tests?**

**A:** `ThreadLocal<T>` provides each thread with its own isolated copy of a variable. In parallel Selenium testing, each thread needs its own `WebDriver`. Without `ThreadLocal`, all threads share one `WebDriver` instance — they fight over the same browser window, causing unpredictable failures. `ThreadLocal<WebDriver>` gives Thread 1 its own Chrome and Thread 2 its own separate Chrome. They cannot interfere with each other.

---

**Q33: What does `tlDriver.remove()` do and why is calling it critical?**

**A:** `remove()` deletes the `WebDriver` reference from the current thread's `ThreadLocal` storage. Without it, when a thread finishes a test and is recycled by the thread pool, the dead `WebDriver` reference remains in memory. Over many test runs, accumulating dead references causes `OutOfMemoryError`. Additionally, if `setUp()` fails partway through and does not set a new driver, `getDriver()` returns the dead session from the previous test — causing `InvalidSessionIdException`. Always call `tlDriver.remove()` in an `@AfterMethod(alwaysRun = true)` after `driver.quit()`.

---

**Q34: Why doesn't Playwright need ThreadLocal?**

**A:** Playwright runs each worker as an isolated Node.js process. Processes do not share memory — there is no shared-state problem. Selenium runs tests as threads within the same JVM process, where all threads share memory. The `page` fixture Playwright injects into each test is already process-isolated — no configuration needed.

---

**Q35: What are the four `parallel` modes in TestNG?**

**A:** `parallel="tests"` — each `<test>` block runs in its own thread (safest, most common). `parallel="classes"` — each test class runs in its own thread. `parallel="methods"` — each `@Test` method runs in its own thread (fastest but requires 100% independent tests). `parallel="instances"` — each class instance runs in its own thread (used with `@Factory` for data-driven parallel execution).

---

**Q36: A test suite passes serially but has failures in parallel. What is your debugging approach?**

**A:** Confirm it is a parallelism issue (run `--workers=1`, confirm pass). Reduce to minimum reproduction (`--workers=2`, identify which test pair conflicts). Then check: shared test data (same email or ID), order dependency (test B assumes test A ran), shared static variables (race conditions), screenshot name collisions, or port conflicts for mock servers. Add thread ID and timestamp logging to shared resources. Fix the root cause — never suppress with `synchronized` unless absolutely necessary.

---

**Q37: What is negative testing and why is it as important as positive testing?**

**A:** Negative testing intentionally sends invalid, unexpected, or malicious inputs to verify the system handles them gracefully — not crashing, not silently accepting bad data, not exposing errors to attackers. Positive tests verify happy paths work. Negative tests verify the edges that real users hit and that attackers exploit. The bugs that reach production are almost always on the edges, not the happy path. A QA engineer who only writes positive tests leaves the most dangerous defects untested.

---

**Q38: What is boundary value analysis? Apply it to an age field accepting 18–65.**

**A:** BVA tests at and around each boundary: 17 (invalid — just below minimum), 18 (valid — lower boundary), 19 (valid — just above), 64 (valid — just below upper), 65 (valid — upper boundary), 66 (invalid — just above). Also test 0, -1. The logic is: bugs concentrate at boundaries where code transitions from one behaviour to another. BVA maximises defect detection with a small number of targeted test cases.

---

**Q39: Why is a 500 error on bad user input always a bug?**

**A:** A 500 means the server crashed on input that should have been validated and rejected at the API boundary with a 400. If a tester can cause a 500, an attacker can too — and can use it to probe internal system structure, cause denial of service, or extract information from error messages. The contract is: bad input = 400 (client problem), server failure = 500 (server problem). Bad input should never cause a server failure.

---

**Q40: How do you verify that XSS did not execute in a Playwright test?**

**A:** Register a `page.on('dialog', ...)` listener before navigating to the page that displays the user input. If the XSS payload fires an `alert()`, the listener catches it and sets a flag. After navigating and waiting 2 seconds, assert the flag is `false`. Also inspect the page source to verify `<` and `>` are escaped as `&lt;` and `&gt;` — confirming the output is sanitised, not just that the current payload did not fire.

---

**Q41: How do you prioritise negative tests under time pressure?**

**A:** Security inputs first — SQL injection, XSS, authentication bypass — because these have the highest impact if missed and the greatest regulatory/reputational cost. Then required field validation (data integrity). Then boundary values for numeric and length fields (common off-by-one bugs). Then duplicate/conflict detection. Format validation last. Skip hypothetical edge cases with no realistic user or attacker path. One critical security finding always justifies the negative testing investment.

---

*End of Guide — Parallel Test Execution + Negative Testing*
