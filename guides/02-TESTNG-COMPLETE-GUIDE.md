# TestNG — Complete Interview Q&A Guide
## Every Concept as a Real Interview Question

---

## SECTION 1: FOUNDATIONS

**Q1: What is TestNG and why would you choose it over JUnit?**

**A:** TestNG (Test Next Generation) is a Java testing framework inspired by JUnit but designed with additional features for enterprise-scale test automation. It was created by Cedric Beust.

Key reasons to choose TestNG over JUnit 4:
- TestNG has built-in support for parallel execution (`parallel="methods"`, `"classes"`, `"tests"`). JUnit 4 requires third-party runners.
- TestNG has `@DataProvider` for data-driven tests. JUnit 4 uses `@Parameterized` which is more cumbersome.
- TestNG has `testng.xml` for flexible suite configuration — include/exclude groups, set thread counts, pass parameters, all without touching code.
- TestNG has `dependsOnMethods` and `dependsOnGroups` for expressing test dependencies.
- TestNG generates detailed HTML and XML reports out of the box.
- TestNG has `IRetryAnalyzer` for automatically retrying flaky tests.
- TestNG has richer listener interfaces (`ITestListener`, `ISuiteListener`, `IAnnotationTransformer`).

```xml
<!-- pom.xml — add TestNG dependency -->
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.8.0</version>
    <scope>test</scope>
</dependency>

<!-- Maven Surefire Plugin to run TestNG suites -->
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
```

Real-world context: In API automation with RestAssured + TestNG, `testng.xml` lets you run smoke tests against production and regression tests against staging from a single command, passing environment URLs as parameters.

---

**Q2: What is the complete execution order of TestNG annotations?**

**A:** This is the most commonly asked TestNG interview question. The order is:

```
@BeforeSuite
  @BeforeTest
    @BeforeClass
      @BeforeMethod
        @Test
      @AfterMethod
    @AfterClass
  @AfterTest
@AfterSuite
```

If a suite has two test classes (ClassA and ClassB) with two `@Test` methods each, the full order is:

```
@BeforeSuite        (once — entire suite)
  @BeforeTest       (once per <test> tag in testng.xml)
    @BeforeClass    (ClassA — once before first @Test in ClassA)
      @BeforeMethod (before test1 in ClassA)
        @Test       (ClassA.test1)
      @AfterMethod
      @BeforeMethod (before test2 in ClassA)
        @Test       (ClassA.test2)
      @AfterMethod
    @AfterClass     (ClassA — once after last @Test)
    @BeforeClass    (ClassB — once)
      @BeforeMethod
        @Test       (ClassB.test1)
      @AfterMethod
      @BeforeMethod
        @Test       (ClassB.test2)
      @AfterMethod
    @AfterClass     (ClassB)
  @AfterTest
@AfterSuite
```

```java
// Complete annotation reference with execution order
public class ExecutionOrderDemo {

    @BeforeSuite
    public void beforeSuite() {
        System.out.println("1. @BeforeSuite — runs once before entire suite");
        // Use for: global config, starting test data servers
    }

    @BeforeTest
    public void beforeTest() {
        System.out.println("2. @BeforeTest — runs before each <test> tag");
        // Use for: environment setup per test group
    }

    @BeforeClass
    public void beforeClass() {
        System.out.println("3. @BeforeClass — runs once before first @Test in class");
        // Use for: expensive setup like launching browser, DB connection
    }

    @BeforeMethod
    public void beforeMethod() {
        System.out.println("4. @BeforeMethod — runs before EVERY @Test");
        // Use for: navigate to page, reset state, clear cookies
    }

    @Test
    public void myTest() {
        System.out.println("5. @Test — the actual test");
    }

    @AfterMethod
    public void afterMethod() {
        System.out.println("6. @AfterMethod — runs after EVERY @Test");
        // Use for: clear session, reset test data
    }

    @AfterClass
    public void afterClass() {
        System.out.println("7. @AfterClass — runs once after last @Test in class");
        // Use for: close browser, close DB connection
    }

    @AfterTest
    public void afterTest() {
        System.out.println("8. @AfterTest — runs after each <test> tag");
    }

    @AfterSuite
    public void afterSuite() {
        System.out.println("9. @AfterSuite — runs once after entire suite");
        // Use for: send test report email, cleanup global resources
    }
}
```

Common mistake: Candidates say `@BeforeTest` runs before every `@Test` method. Wrong — it runs before each `<test>` tag in testng.xml, not before each test method.

---

**Q3: What is the difference between @BeforeClass and @BeforeMethod — when do you use each?**

**A:**

| | @BeforeClass | @BeforeMethod |
|---|---|---|
| Runs | Once before the first @Test in the class | Before every single @Test method |
| Use for | Expensive one-time setup | Per-test reset/navigation |
| Example | Launch browser, open DB connection | Navigate to login page, clear form |

```java
public class LoginTest extends BaseTest {

    // @BeforeClass: launching ChromeDriver is expensive (~2 seconds)
    // You only want to do this once per class, not before every test
    @BeforeClass
    public void launchBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        System.out.println("Browser launched once for this class");
    }

    // @BeforeMethod: each test needs a fresh login page
    // Navigate back to starting point before each test
    @BeforeMethod
    public void goToLoginPage() {
        driver.get("https://example.com/login");
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("password")).clear();
        System.out.println("Navigated to login page before test");
    }

    @Test
    public void validLoginTest() {
        // page is clean — ready to type
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("Admin@123");
        driver.findElement(By.id("loginBtn")).click();
        Assert.assertTrue(driver.getTitle().contains("Dashboard"));
    }

    @Test
    public void invalidPasswordTest() {
        // @BeforeMethod ran again — fresh login page
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("wrong");
        driver.findElement(By.id("loginBtn")).click();
        Assert.assertTrue(driver.findElement(By.id("error")).isDisplayed());
    }

    @AfterClass
    public void closeBrowser() {
        if (driver != null) driver.quit();
    }
}
```

Real-world rule: `@BeforeClass` for what costs time to create. `@BeforeMethod` for what must be fresh for each test.

---

**Q4: What happens if @BeforeMethod fails? Does the @Test run?**

**A:** If `@BeforeMethod` throws an exception or assertion fails, the `@Test` method is **skipped** — not failed. TestNG marks the test as `SKIPPED`.

Critically, `@AfterMethod` still runs after a `@BeforeMethod` failure (for cleanup purposes).

```java
public class BeforeMethodFailDemo {

    @BeforeMethod
    public void setUp() {
        // Simulate a setup failure (e.g., DB connection refused)
        boolean serverUp = checkServer();
        if (!serverUp) {
            throw new RuntimeException("Server not available — test will be SKIPPED");
        }
        // If this throws, the @Test below is skipped
    }

    @Test
    public void myTest() {
        // This is NEVER reached if @BeforeMethod threw
        System.out.println("This will not print");
    }

    @AfterMethod
    public void tearDown() {
        // This STILL runs even if @BeforeMethod failed
        System.out.println("Cleanup always happens");
    }

    // Result in report:
    // myTest — SKIPPED (not FAILED)
    // Reason: configuration failure in beforeMethod
}
```

Interview tip: The distinction between SKIP and FAIL matters. A SKIP means setup didn't run. A FAIL means the test ran and the assertion failed. In CI dashboards these show differently.

---

**Q5: What are all the @Test attributes and what does each one do?**

**A:**

```java
// description — human-readable name in report
@Test(description = "Verify user can log in with valid credentials")
public void validLogin() { }

// groups — tag this test for selective execution
@Test(groups = { "smoke", "regression" })
public void criticalLoginTest() { }

// enabled — set to false to skip without deleting the test
@Test(enabled = false)
public void brokenTestTemporarilyDisabled() { }
// Use enabled=false when a bug is raised and you don't want to delete the test

// priority — controls execution order within a class (lower number = runs first)
// Default priority is 0. Negative values are allowed.
@Test(priority = 1)
public void firstTest() { }

@Test(priority = 2)
public void secondTest() { }

@Test(priority = -1)
public void runsBeforeEverything() { }

// timeOut — fail the test if it takes longer than N milliseconds
@Test(timeOut = 5000)   // fail if > 5 seconds
public void performanceTest() {
    Response r = given().when().get("/slow-endpoint").then().extract().response();
    // If this takes 6 seconds, TestNG throws ThreadTimeoutException → FAIL
}

// invocationCount — run the same test N times (useful for stability testing)
@Test(invocationCount = 5)
public void runFiveTimes() {
    // Runs 5 times — each run appears separately in report
    given().when().get("/posts/1").then().statusCode(200);
}

// invocationCount + threadPoolSize — run N times with T threads in parallel
@Test(invocationCount = 10, threadPoolSize = 3)
public void loadTest() {
    // 10 runs, 3 at a time — simulates concurrent users
    given().when().get("/posts").then().statusCode(200);
}

// expectedExceptions — test passes only if this exception is thrown
@Test(expectedExceptions = ArithmeticException.class)
public void divisionByZeroTest() {
    int result = 10 / 0;   // must throw ArithmeticException
}

// expectedExceptionsMessageRegExp — also verify the exception message
@Test(
    expectedExceptions = IllegalArgumentException.class,
    expectedExceptionsMessageRegExp = ".*cannot be null.*"
)
public void nullInputTest() {
    processInput(null);
}

// dataProvider — feed multiple data rows to this test
@Test(dataProvider = "loginData")
public void loginTest(String user, String pass, int expectedCode) { }

// dependsOnMethods — only run if specified method passed
@Test(dependsOnMethods = { "loginTest" })
public void dashboardTest() { }

// alwaysRun — run even if a dependency failed or was skipped
@Test(dependsOnMethods = { "loginTest" }, alwaysRun = true)
public void logoutTest() {
    // Always try to logout even if loginTest failed — important for cleanup
}

// singleThreaded — run all methods in this class on same thread (even in parallel mode)
@Test(singleThreaded = true)
public void threadSensitiveTest() { }
```

---

**Q6: What is dependsOnMethods and when is it dangerous?**

**A:** `dependsOnMethods` makes a test run only if the specified method passed. If the dependency failed or was skipped, the dependent test is marked SKIP automatically.

```java
public class LoginFlowTest {

    @Test(priority = 1)
    public void loginTest() {
        given().spec(requestSpec)
            .body("{ \"username\": \"admin\", \"password\": \"Admin@123\" }")
            .when().post("/auth/login")
            .then().statusCode(200);
    }

    // Only runs if loginTest PASSED
    @Test(priority = 2, dependsOnMethods = { "loginTest" })
    public void accessDashboard() {
        given().spec(requestSpec)
            .when().get("/dashboard")
            .then().statusCode(200);
    }

    // Only runs if accessDashboard PASSED
    @Test(priority = 3, dependsOnMethods = { "accessDashboard" })
    public void logoutTest() {
        given().spec(requestSpec)
            .when().post("/auth/logout")
            .then().statusCode(200);
    }
}
```

Why it is dangerous:
1. Tests become tightly coupled — one failure cascades to skip many tests
2. Masks real failures — you see SKIP instead of knowing what actually broke
3. Hard to run tests in isolation
4. Anti-pattern for unit and API tests where each test should stand alone

When it is acceptable: End-to-end UI flows that truly represent a sequence (create account → verify email → login). Even then, consider whether setup fixtures are a better approach.

---

**Q7: What does alwaysRun=true do and when would you use it?**

**A:** `alwaysRun = true` on a `@Test` or configuration method (`@BeforeMethod`, `@AfterMethod`) ensures it runs regardless of whether its dependencies passed or failed.

```java
public class ResourceCleanupTest {

    private String createdResourceId;

    @Test
    public void createResource() {
        createdResourceId = given().spec(requestSpec)
            .body("{ \"name\": \"test-resource\" }")
            .when().post("/resources")
            .then().statusCode(201)
            .extract().path("id");
    }

    @Test(dependsOnMethods = { "createResource" })
    public void updateResource() {
        // This might fail — but cleanup must still happen
        given().spec(requestSpec)
            .body("{ \"name\": \"updated\" }")
            .pathParam("id", createdResourceId)
            .when().put("/resources/{id}")
            .then().statusCode(200);
    }

    // alwaysRun=true — delete the resource even if updateResource failed
    // Without this, if updateResource fails, deleteResource is SKIPPED
    // and the test data is left in the system
    @Test(dependsOnMethods = { "createResource", "updateResource" }, alwaysRun = true)
    public void deleteResource() {
        given().pathParam("id", createdResourceId)
            .when().delete("/resources/{id}")
            .then().statusCode(anyOf(equalTo(200), equalTo(204)));
    }
}
```

Common use on `@AfterMethod`:
```java
// @AfterMethod with alwaysRun=true — runs even if @BeforeMethod failed
@AfterMethod(alwaysRun = true)
public void tearDown() {
    if (driver != null) {
        driver.quit();   // Always close browser, no matter what
    }
}
```

---

## SECTION 2: testng.xml

**Q8: What is testng.xml and what is its purpose?**

**A:** `testng.xml` is the suite configuration file for TestNG. It controls which tests run, in what order, with what parameters, in how many threads, and which listeners are active — all without modifying Java code.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">

<suite name="API Regression Suite" verbose="2" parallel="tests" thread-count="3">

    <!-- Listeners apply to all tests in suite -->
    <listeners>
        <listener class-name="listeners.TestListener"/>
        <listener class-name="listeners.RetryListener"/>
    </listeners>

    <!-- First <test> tag — smoke tests only -->
    <test name="Smoke Tests">
        <groups>
            <run>
                <include name="smoke"/>
            </run>
        </groups>
        <classes>
            <class name="tests.UserApiTest"/>
            <class name="tests.AuthApiTest"/>
        </classes>
    </test>

    <!-- Second <test> tag — regression, exclude slow tests -->
    <test name="Regression Tests">
        <groups>
            <run>
                <include name="regression"/>
                <exclude name="slow"/>
            </run>
        </groups>
        <classes>
            <class name="tests.PostApiTest"/>
            <class name="tests.CommentApiTest"/>
            <class name="tests.UserApiTest"/>
        </classes>
    </test>

    <!-- Parameterized test — pass browser/environment -->
    <test name="Chrome Production">
        <parameter name="browser" value="chrome"/>
        <parameter name="baseUrl" value="https://production.example.com"/>
        <classes>
            <class name="tests.LoginTest"/>
        </classes>
    </test>

    <!-- Run only specific methods -->
    <test name="Critical Path Only">
        <classes>
            <class name="tests.LoginTest">
                <methods>
                    <include name="validLogin"/>
                    <include name="tokenExpiry"/>
                </methods>
            </class>
        </classes>
    </test>

</suite>
```

---

**Q9: What does the verbose attribute in testng.xml do?**

**A:** `verbose` controls how much output TestNG prints to the console. Scale is 0–10.

```xml
<suite name="MySuite" verbose="0">  <!-- silent — no console output -->
<suite name="MySuite" verbose="1">  <!-- default — test names and pass/fail -->
<suite name="MySuite" verbose="2">  <!-- show method names and parameters -->
<suite name="MySuite" verbose="10"> <!-- maximum — full debug output -->
```

Use `verbose="2"` during development for debugging. Use `verbose="1"` in CI to keep logs readable.

---

**Q10: What are the four parallel modes in testng.xml and how does each work?**

**A:** The `parallel` attribute on `<suite>` controls what unit runs concurrently.

```xml
<!-- parallel="methods" — each @Test method runs on its own thread -->
<!-- Best for: API tests where tests are independent -->
<suite parallel="methods" thread-count="4">

<!-- parallel="classes" — each test class runs on its own thread -->
<!-- All methods in ClassA run on Thread-1, all in ClassB on Thread-2 -->
<suite parallel="classes" thread-count="3">

<!-- parallel="tests" — each <test> tag in testng.xml runs on its own thread -->
<!-- Smoke Tests run on Thread-1, Regression on Thread-2 simultaneously -->
<suite parallel="tests" thread-count="2">

<!-- parallel="instances" — each instance of a class runs on its own thread -->
<!-- Useful when you create multiple instances of the same class -->
<suite parallel="instances" thread-count="2">
```

Concrete example: If you have 20 API tests and `parallel="methods" thread-count="5"`, TestNG runs 5 tests simultaneously, completing in roughly 1/5 the time.

```java
// BaseTest — MANDATORY ThreadLocal pattern for parallel safety
// Without ThreadLocal, all parallel tests share the same driver → race conditions
public class BaseTest {

    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public WebDriver getDriver() {
        return tlDriver.get();
    }

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        tlDriver.set(new ChromeDriver());   // Each thread gets its own driver
        getDriver().manage().window().maximize();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            tlDriver.remove();   // CRITICAL — prevents memory leak
        }
    }
}
```

---

**Q11: What does thread-count mean in testng.xml?**

**A:** `thread-count` is the maximum number of threads TestNG creates in its thread pool for parallel execution. It works together with the `parallel` attribute.

```xml
<suite parallel="methods" thread-count="5">
```

This means: at most 5 test methods run simultaneously. If you have 20 tests, they run in batches of 5.

Rules:
- Setting `thread-count` without `parallel` has no effect
- Setting `thread-count` higher than your CPU cores doesn't always help — for CPU-heavy tasks it can slow things down. For I/O-bound tasks (API calls, browser waits), higher thread count helps
- For API testing, `thread-count="10"` or higher is reasonable since tests spend most time waiting for HTTP responses

```xml
<!-- Reasonable config for API test parallelism -->
<suite name="API Suite" parallel="methods" thread-count="10" verbose="1">
    <test name="All API Tests">
        <classes>
            <class name="tests.UserApiTest"/>
            <class name="tests.PostApiTest"/>
            <class name="tests.AuthApiTest"/>
        </classes>
    </test>
</suite>
```

---

**Q12: How do you pass parameters from testng.xml to your Java test and what is @Optional?**

**A:** Use `<parameter>` in testng.xml and `@Parameters` annotation in Java.

```xml
<!-- testng.xml -->
<test name="Firefox Staging">
    <parameter name="browser" value="firefox"/>
    <parameter name="baseUrl" value="https://staging.example.com"/>
    <classes>
        <class name="tests.LoginTest"/>
    </classes>
</test>
```

```java
// In Java — read parameters
@BeforeMethod
@Parameters({ "browser", "baseUrl" })
public void setUp(String browser,
                  @Optional("https://default.example.com") String baseUrl) {
    // browser is REQUIRED — testng.xml must provide it
    // @Optional("https://default.example.com") — uses default if not in testng.xml

    System.out.println("Browser: " + browser);
    System.out.println("URL: " + baseUrl);

    switch (browser.toLowerCase()) {
        case "chrome":
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            break;
        case "firefox":
            WebDriverManager.firefoxdriver().setup();
            driver = new FirefoxDriver();
            break;
        default:
            throw new IllegalArgumentException("Unsupported browser: " + browser);
    }
    driver.get(baseUrl);
}
```

Key point: If a `@Parameters` field has no `@Optional` and the parameter is missing from testng.xml, TestNG throws `TestNGException: Parameter 'browser' is required by Test but has not been marked @Optional or defined in testng.xml`.

---

## SECTION 3: ASSERTIONS

**Q13: What is the difference between hard assertions and soft assertions in TestNG?**

**A:** Hard assertions (`Assert`) stop test execution immediately on first failure. Soft assertions (`SoftAssert`) collect all failures and report them all at the end.

```java
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

public class AssertionDemo {

    // ── HARD ASSERTIONS — stop on first failure ───────────────────────────────
    @Test
    public void hardAssertDemo() {
        Response response = given().when().get("/users/1").then().extract().response();

        Assert.assertEquals(response.statusCode(), 200);  // if this fails → stops here
        Assert.assertNotNull(response.path("name"));      // never reached if above fails
        Assert.assertTrue(response.path("email").toString().contains("@")); // also skipped
        // You only ever see the FIRST failure with hard assertions
    }

    // ── ALL HARD ASSERTION METHODS ────────────────────────────────────────────
    @Test
    public void allHardAsserts() {
        Assert.assertEquals("actual", "actual");
        Assert.assertEquals("actual", "expected", "Custom failure message");
        Assert.assertNotEquals("value1", "value2");
        Assert.assertTrue(5 > 3);
        Assert.assertTrue(5 > 3, "5 should be greater than 3");
        Assert.assertFalse(3 > 5);
        Assert.assertNull(null);
        Assert.assertNotNull("notNull");
        Assert.assertSame(obj1, obj1);       // same reference
        Assert.assertNotSame(obj1, obj2);    // different references
    }

    // ── SOFT ASSERTIONS — collect all failures ────────────────────────────────
    @Test
    public void softAssertDemo() {
        Response response = given().when().get("/users/1").then().extract().response();

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(response.statusCode(), 200, "Status code mismatch");
        soft.assertNotNull(response.path("name"), "name should not be null");
        soft.assertNotNull(response.path("email"), "email should not be null");
        soft.assertTrue(
            response.path("email").toString().contains("@"),
            "email should contain @"
        );
        soft.assertEquals(response.path("id"), 1, "id should be 1");

        soft.assertAll();   // MANDATORY — triggers report of ALL failures
        // If you forget assertAll(), soft assertion failures are silently ignored!
    }
}
```

When to use soft assertions:
- Validating multiple fields of an API response — you want to see ALL fields that are wrong, not just the first
- Form validation pages — see all error messages at once
- Do NOT use soft assertions for critical preconditions (status code check before body check)

Common mistake: Forgetting `soft.assertAll()`. The test passes even when fields are wrong.

---

**Q14: What happens if you forget to call soft.assertAll()?**

**A:** The test passes even when soft assertion failures occurred. TestNG has no knowledge of the failures because `SoftAssert` stores them internally and only throws when `assertAll()` is called.

```java
@Test
public void buggyTest() {
    SoftAssert soft = new SoftAssert();
    soft.assertEquals("actual", "expected");   // stores failure internally
    soft.assertTrue(false);                     // stores another failure
    // NO soft.assertAll() — test reports as PASSED
    // This is a serious bug in your test — missed failures
}

@Test
public void correctTest() {
    SoftAssert soft = new SoftAssert();
    soft.assertEquals("actual", "expected");
    soft.assertTrue(false);
    soft.assertAll();   // throws AssertionError listing BOTH failures
    // Test reports as FAILED with details of every failure
}
```

---

## SECTION 4: DATA PROVIDER

**Q15: What is @DataProvider in TestNG and how does it work?**

**A:** `@DataProvider` is a method that returns `Object[][]`. Each inner `Object[]` is one test run with its arguments. TestNG calls the `@Test` method once per row.

```java
public class DataProviderDemo {

    // ── SINGLE PARAMETER ─────────────────────────────────────────────────────
    @DataProvider(name = "userIds")
    public Object[][] userIds() {
        return new Object[][] {
            { 1 },
            { 2 },
            { 5 },
            { 100 }
        };
    }

    @Test(dataProvider = "userIds")
    public void getUserById(int userId) {
        // Runs 4 times with userId = 1, 2, 5, 100
        given()
            .pathParam("id", userId)
            .when().get("/users/{id}")
            .then().statusCode(200)
            .body("id", equalTo(userId));
    }

    // ── MULTIPLE PARAMETERS ───────────────────────────────────────────────────
    @DataProvider(name = "loginScenarios")
    public Object[][] loginScenarios() {
        return new Object[][] {
            // username,   password,      expectedCode, expectedField
            { "admin",   "Admin@123",   200,           "token"  },
            { "user1",   "User@123",    200,           "token"  },
            { "admin",   "wrongpass",   401,           "error"  },
            { "",        "Admin@123",   400,           "error"  },
            { "admin",   "",            400,           "error"  },
        };
    }

    @Test(dataProvider = "loginScenarios")
    public void loginTest(String username, String password,
                          int expectedCode, String expectedField) {
        // Runs 5 times — once per row
        given()
            .spec(requestSpec)
            .body(String.format(
                "{ \"username\": \"%s\", \"password\": \"%s\" }",
                username, password
            ))
            .when().post("/auth/login")
            .then()
            .statusCode(expectedCode)
            .body(expectedField, notNullValue());
    }
}
```

---

**Q16: How do you use a DataProvider from a different class?**

**A:** Specify `dataProviderClass` in the `@Test` annotation.

```java
// TestDataProviders.java — separate class holding all data providers
public class TestDataProviders {

    @DataProvider(name = "validUsers")
    public static Object[][] validUsers() {
        return new Object[][] {
            { "admin", "Admin@123" },
            { "manager", "Manager@456" }
        };
    }

    @DataProvider(name = "postPayloads")
    public static Object[][] postPayloads() {
        return new Object[][] {
            { "First Post", "First Body", 1 },
            { "Second Post", "Second Body", 2 }
        };
    }
}

// PostApiTest.java — uses DataProvider from different class
public class PostApiTest {

    @Test(dataProvider = "postPayloads", dataProviderClass = TestDataProviders.class)
    public void createPost(String title, String body, int userId) {
        given()
            .spec(requestSpec)
            .body(Map.of("title", title, "body", body, "userId", userId))
            .when().post("/posts")
            .then().statusCode(201);
    }
}
```

---

**Q17: How do you create a DataProvider that reads from an external CSV file?**

**A:**

```java
@DataProvider(name = "csvLoginData")
public Object[][] csvLoginData() throws Exception {
    List<Object[]> rows = new ArrayList<>();
    String filePath = "src/test/resources/data/login-data.csv";

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        boolean firstLine = true;
        while ((line = br.readLine()) != null) {
            if (firstLine) {
                firstLine = false; // skip header row
                continue;
            }
            String[] cols = line.split(",");
            // CSV format: username,password,expectedStatusCode
            rows.add(new Object[] {
                cols[0].trim(),          // username
                cols[1].trim(),          // password
                Integer.parseInt(cols[2].trim())  // expected code
            });
        }
    }
    return rows.toArray(new Object[0][]);
}

// CSV file: src/test/resources/data/login-data.csv
// username,password,expectedCode
// admin,Admin@123,200
// user1,User@123,200
// hacker,wrongpass,401
// ,Admin@123,400

@Test(dataProvider = "csvLoginData")
public void loginFromCsv(String username, String password, int expectedCode) {
    given()
        .spec(requestSpec)
        .body(Map.of("username", username, "password", password))
        .when().post("/auth/login")
        .then().statusCode(expectedCode);
}
```

---

**Q18: What is a parallel DataProvider and how does it work?**

**A:** Setting `parallel = true` on `@DataProvider` makes TestNG run the multiple data rows concurrently rather than sequentially.

```java
// Without parallel — runs row 1, then row 2, then row 3, then row 4
@DataProvider(name = "sequential")
public Object[][] sequential() {
    return new Object[][] {{ 1 }, { 2 }, { 3 }, { 4 }};
}

// With parallel — runs all rows concurrently (up to thread-count limit)
@DataProvider(name = "parallelIds", parallel = true)
public Object[][] parallelIds() {
    return new Object[][] {
        { 1 }, { 2 }, { 3 }, { 4 }, { 5 }, { 6 }, { 7 }, { 8 }
    };
}

@Test(dataProvider = "parallelIds")
public void fetchUserInParallel(int userId) {
    // 8 API calls run concurrently — much faster for I/O bound tests
    given()
        .pathParam("id", userId)
        .when().get("/users/{id}")
        .then().statusCode(200)
        .body("id", equalTo(userId));
}
```

The thread count for parallel DataProvider is controlled by `data-provider-thread-count` attribute on the `<suite>` tag in testng.xml:

```xml
<suite name="Suite" data-provider-thread-count="5">
```

---

## SECTION 5: GROUPS

**Q19: How do TestNG groups work and how do you run tests by group?**

**A:** Groups let you tag tests with labels and selectively include or exclude them from runs.

```java
public class ApiTestSuite {

    @Test(groups = { "smoke" })
    public void healthCheckTest() {
        // Runs in smoke — fast, critical path only
        given().when().get("/health").then().statusCode(200);
    }

    @Test(groups = { "smoke", "regression" })
    public void createUserTest() {
        // Runs in both smoke and regression
    }

    @Test(groups = { "regression" })
    public void updateUserTest() {
        // Regression only
    }

    @Test(groups = { "regression", "slow" })
    public void complexReportTest() {
        // Regression but tagged slow — can be excluded
    }

    @Test(groups = { "negative" })
    public void unauthorizedAccessTest() {
        // Negative test group
    }
}
```

```xml
<!-- testng.xml — include/exclude groups -->
<test name="Smoke Only">
    <groups>
        <run>
            <include name="smoke"/>
        </run>
    </groups>
    <classes>
        <class name="tests.ApiTestSuite"/>
    </classes>
</test>

<test name="Regression Without Slow Tests">
    <groups>
        <run>
            <include name="regression"/>
            <exclude name="slow"/>
        </run>
    </groups>
    <classes>
        <class name="tests.ApiTestSuite"/>
    </classes>
</test>
```

```bash
# Run by group from Maven command line
mvn test -Dgroups=smoke
mvn test -Dgroups="smoke,regression"
mvn test -DexcludedGroups=slow
mvn test -Dgroups=regression -DexcludedGroups=slow
```

---

**Q20: What is dependsOnGroups and how does it differ from dependsOnMethods?**

**A:** `dependsOnGroups` makes a test depend on all tests in a named group passing, rather than a specific method.

```java
public class E2EOrderTest {

    @Test(groups = { "auth" })
    public void loginTest() {
        // Must pass before any "order" group test runs
    }

    @Test(groups = { "auth" })
    public void tokenValidationTest() {
        // Also in auth group
    }

    // Depends on ALL tests in "auth" group passing
    @Test(groups = { "order" }, dependsOnGroups = { "auth" })
    public void createOrderTest() {
        // Only runs if EVERY test in "auth" group passed
    }

    @Test(groups = { "order" }, dependsOnGroups = { "auth" })
    public void viewOrderTest() {
        // Also depends on entire auth group
    }

    @Test(dependsOnGroups = { "auth", "order" })
    public void checkoutTest() {
        // Depends on both auth group and order group
    }
}
```

`dependsOnMethods` → depends on a specific named method.
`dependsOnGroups` → depends on all tests in a named group — more flexible when the group may grow.

---

## SECTION 6: LISTENERS

**Q21: What is ITestListener in TestNG and what methods does it have?**

**A:** `ITestListener` is an interface that lets you hook into test lifecycle events. Implement it to add custom behaviour at each stage.

```java
package listeners;

import org.testng.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class TestListener implements ITestListener {

    // Called when a @Test method is about to start
    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("[START] " + result.getName());
        // result.getName()        — method name
        // result.getMethod()      — full ITestNGMethod object
        // result.getInstance()    — the test class instance
        // result.getParameters()  — parameters passed to DataProvider
    }

    // Called when a @Test method passes
    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("[PASS]  " + result.getName() +
            " (" + (result.getEndMillis() - result.getStartMillis()) + "ms)");
    }

    // Called when a @Test method fails (assertion error or exception)
    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("[FAIL]  " + result.getName());
        System.out.println("Reason: " + result.getThrowable().getMessage());

        // Take screenshot on failure — critical for UI tests
        Object instance = result.getInstance();
        if (instance instanceof BaseTest) {
            WebDriver driver = ((BaseTest) instance).getDriver();
            if (driver != null) {
                try {
                    File screenshot = ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.FILE);
                    String path = "screenshots/FAIL_" + result.getName()
                        + "_" + System.currentTimeMillis() + ".png";
                    FileUtils.copyFile(screenshot, new File(path));
                    System.out.println("Screenshot saved: " + path);
                } catch (IOException e) {
                    System.err.println("Could not save screenshot: " + e.getMessage());
                }
            }
        }
    }

    // Called when a @Test method is skipped
    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("[SKIP]  " + result.getName());
        Throwable cause = result.getThrowable();
        if (cause != null) {
            System.out.println("Skip reason: " + cause.getMessage());
        }
    }

    // Called when a @Test method fails but is within success percentage
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Rarely used — for @Test(successPercentage=80, invocationCount=10)
    }

    // Called before the test class methods start running
    @Override
    public void onStart(ITestContext context) {
        System.out.println("Test context starting: " + context.getName());
        System.out.println("Total tests included: " + context.getAllTestMethods().length);
    }

    // Called after all test class methods in the context have run
    @Override
    public void onFinish(ITestContext context) {
        System.out.println("Passed:  " + context.getPassedTests().size());
        System.out.println("Failed:  " + context.getFailedTests().size());
        System.out.println("Skipped: " + context.getSkippedTests().size());
    }
}
```

---

**Q22: How do you register a Listener — what is the difference between @Listeners annotation and testng.xml?**

**A:** Two ways to register listeners:

```java
// 1. @Listeners annotation on the test class
@Listeners({ TestListener.class, SuiteListener.class })
public class LoginTest {
    @Test
    public void myTest() { }
}

// 2. On a base class — all subclasses inherit the listener
@Listeners({ TestListener.class })
public class BaseTest {
    // All classes that extend BaseTest get the listener
}
```

```xml
<!-- 3. In testng.xml — applies to entire suite -->
<suite name="Suite">
    <listeners>
        <listener class-name="listeners.TestListener"/>
        <listener class-name="listeners.RetryListener"/>
    </listeners>
    ...
</suite>
```

Difference:
- `@Listeners` is per-class (or inherited via BaseTest). Best when you want the listener only for specific classes.
- testng.xml listeners apply to the entire suite. Best for global listeners like screenshot on failure or retry.
- You can use both — TestNG merges them, no duplication.

---

**Q23: What is ISuiteListener and what is it used for?**

**A:** `ISuiteListener` hooks into suite-level events — before any test runs and after all tests complete.

```java
package listeners;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import java.util.Map;

public class SuiteListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        System.out.println("=== Suite Starting: " + suite.getName() + " ===");
        System.out.println("Output directory: " + suite.getOutputDirectory());
        // Common use: start a test data server, set global environment variable,
        // initialise a DB connection pool, start recording
    }

    @Override
    public void onFinish(ISuite suite) {
        System.out.println("=== Suite Finished: " + suite.getName() + " ===");

        // Print summary across all test groups
        Map<String, ISuiteResult> results = suite.getResults();
        for (Map.Entry<String, ISuiteResult> entry : results.entrySet()) {
            ITestContext ctx = entry.getValue().getTestContext();
            System.out.println("  Test: " + entry.getKey());
            System.out.println("    Passed:  " + ctx.getPassedTests().size());
            System.out.println("    Failed:  " + ctx.getFailedTests().size());
            System.out.println("    Skipped: " + ctx.getSkippedTests().size());
        }
        // Common use: send summary email, upload report to dashboard
    }
}
```

---

## SECTION 7: RETRY AND TRANSFORMERS

**Q24: What is IRetryAnalyzer and how do you implement retry for flaky tests?**

**A:** `IRetryAnalyzer` is an interface with a single `retry()` method. When a test fails, TestNG calls `retry()`. If it returns `true`, the test runs again. If `false`, it is marked as failed.

```java
package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRY = 2;  // retry up to 2 times (3 total attempts)

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            System.out.println(
                "Retrying test: " + result.getName() +
                " | Attempt " + retryCount + " of " + MAX_RETRY
            );
            return true;   // retry the test
        }
        return false;  // give up — mark as FAILED
    }
}
```

```java
// Apply to a specific test
@Test(retryAnalyzer = RetryAnalyzer.class)
public void flakyNetworkTest() {
    // If this fails, RetryAnalyzer retries it up to 2 more times
    given().when().get("/unstable-endpoint").then().statusCode(200);
}
```

Important: Each test method gets its own `RetryAnalyzer` instance, so `retryCount` resets per test. If it were static, a retry on test A would count toward test B's limit.

---

**Q25: How do you apply IRetryAnalyzer to ALL tests without adding retryAnalyzer to every @Test annotation?**

**A:** Use `IAnnotationTransformer`. This listener intercepts annotation processing and can modify any annotation programmatically.

```java
package listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryListener implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {
        // Called for every @Test annotation before TestNG processes it
        // Set retryAnalyzer globally — no need to add it to each @Test
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
```

```xml
<!-- testng.xml — register RetryListener -->
<suite name="Suite">
    <listeners>
        <listener class-name="listeners.RetryListener"/>
        <listener class-name="listeners.TestListener"/>
    </listeners>
    ...
</suite>
```

Now every `@Test` method in the entire suite automatically retries on failure. You can add conditions in `transform()` to skip retry for certain classes or groups:

```java
@Override
public void transform(ITestAnnotation annotation, Class testClass,
                      Constructor testConstructor, Method testMethod) {
    // Don't retry performance tests — they should fail fast
    if (testMethod != null &&
        !testMethod.getDeclaringClass().getName().contains("Performance")) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
```

---

## SECTION 8: SKIPPING TESTS

**Q26: How do you skip a test conditionally at runtime using SkipException?**

**A:** Throw `org.testng.SkipException` from inside a test or `@BeforeMethod`. TestNG catches it and marks the test as SKIPPED, not FAILED.

```java
import org.testng.SkipException;

public class ConditionalSkipTest {

    @Test
    public void skipOnEnvironment() {
        String env = System.getenv("TEST_ENV");
        if ("production".equals(env)) {
            throw new SkipException("Skipping destructive test on production environment");
        }
        // ... test logic that deletes data — safe to run only on staging
        given().pathParam("id", 999).when().delete("/test-data/{id}").then().statusCode(200);
    }

    @Test
    public void skipIfFeatureFlagOff() {
        boolean featureEnabled = getFeatureFlag("NEW_CHECKOUT");
        if (!featureEnabled) {
            throw new SkipException("NEW_CHECKOUT feature flag is disabled — skipping test");
        }
        // ... test the new checkout flow
    }

    @Test
    public void skipIfApiDown() {
        int status = given().when().get("/health").then().extract().statusCode();
        if (status != 200) {
            throw new SkipException("API is not healthy (status " + status + ") — skipping");
        }
        // ... proceed with test
    }
}
```

The key difference:
- `throw new SkipException(...)` → test marked SKIPPED
- `Assert.fail(...)` → test marked FAILED
- Unhandled exception → test marked FAILED

Use `SkipException` when the test is not applicable for the current environment or state, not when the system under test has a bug.

---

## SECTION 9: REPORTING

**Q27: What reports does TestNG generate and where are they?**

**A:** TestNG generates two types of reports automatically:

```
target/surefire-reports/
├── index.html             ← HTML report — open in browser
├── TEST-TestSuiteName.xml ← JUnit XML — CI systems parse this
├── emailable-report.html  ← Simple email-ready report
└── testng-results.xml     ← TestNG native XML format
```

```bash
# Generate reports by running tests
mvn test

# View HTML report (Windows)
start target/surefire-reports/index.html

# View HTML report (Mac)
open target/surefire-reports/index.html

# View TestNG native HTML report (different from surefire)
# Located at: test-output/index.html after running TestNG directly
```

The HTML report shows:
- Total tests run / passed / failed / skipped
- Each test method with pass/fail status
- Execution time per test
- Stack traces for failures
- Groups used

---

**Q28: What is the Maven Surefire Plugin and what role does it play?**

**A:** Maven Surefire Plugin is what actually runs your tests when you type `mvn test`. Without it, Maven doesn't know how to execute TestNG or JUnit tests.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.1.2</version>
      <configuration>
        <!-- Tell Surefire which testng.xml to use -->
        <suiteXmlFiles>
          <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
        </suiteXmlFiles>

        <!-- Pass system properties to tests -->
        <systemPropertyVariables>
          <env>staging</env>
          <baseUrl>https://staging.example.com</baseUrl>
        </systemPropertyVariables>

        <!-- Run tests in parallel at Maven level (different from TestNG parallel) -->
        <parallel>classes</parallel>
        <threadCount>4</threadCount>

        <!-- Fail build if no tests ran -->
        <failIfNoTests>true</failIfNoTests>
      </configuration>
    </plugin>
  </plugins>
</build>
```

```bash
# Common Maven test commands
mvn test                              # run all tests
mvn test -Dgroups=smoke               # run smoke group
mvn test -DsuiteXmlFile=regression.xml # use different testng.xml
mvn test -Dtest=LoginTest             # run specific class
mvn test -Dtest=LoginTest#validLogin  # run specific method
mvn test -DskipTests=true             # skip tests (still compiles)
mvn test-compile                      # compile without running
```

---

**Q29: How do you run a specific test method from the command line?**

**A:**

```bash
# Run a specific test class
mvn test -Dtest=LoginTest

# Run a specific method in a class
mvn test -Dtest=LoginTest#validLogin

# Run multiple methods
mvn test -Dtest=LoginTest#validLogin+invalidPassword

# Run all methods matching a pattern
mvn test -Dtest=LoginTest#valid*

# Run specific class with a specific testng.xml
mvn test -DsuiteXmlFile=src/test/resources/smoke.xml -Dtest=LoginTest
```

Alternatively, target a specific method via testng.xml:

```xml
<test name="Single Method">
    <classes>
        <class name="tests.LoginTest">
            <methods>
                <include name="validLogin"/>
            </methods>
        </class>
    </classes>
</test>
```

---

## SECTION 10: COMMON MISTAKES AND ADVANCED QUESTIONS

**Q30: What are the most common TestNG mistakes made by automation engineers?**

**A:**

**Mistake 1: Forgetting soft.assertAll()**
```java
// WRONG — failures silently ignored
SoftAssert soft = new SoftAssert();
soft.assertEquals(status, 200);
soft.assertNotNull(body);
// Missing soft.assertAll() — test passes even if assertions fail

// CORRECT
soft.assertAll();  // Always the last line
```

**Mistake 2: Static fields in parallel tests**
```java
// WRONG — all parallel threads share the same driver
public class BaseTest {
    private static WebDriver driver;  // STATIC = shared = race condition
}

// CORRECT — each thread gets its own driver
public class BaseTest {
    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();
    public WebDriver getDriver() { return tlDriver.get(); }
}
```

**Mistake 3: Not removing ThreadLocal in @AfterMethod**
```java
// CAUSES MEMORY LEAK in long test runs
@AfterMethod
public void tearDown() {
    getDriver().quit();
    // Missing: tlDriver.remove()
}

// CORRECT
@AfterMethod(alwaysRun = true)
public void tearDown() {
    if (getDriver() != null) {
        getDriver().quit();
        tlDriver.remove();  // prevents memory leak
    }
}
```

**Mistake 4: Depending on test execution order without priority**
```java
// WRONG — execution order is not guaranteed without priority
@Test
public void createUser() { ... }

@Test
public void deleteUser() { ... }  // might run before createUser

// CORRECT
@Test(priority = 1)
public void createUser() { ... }

@Test(priority = 2)
public void deleteUser() { ... }
```

**Mistake 5: Using @BeforeClass for per-test setup**
```java
// WRONG — navigates to page once, but next test needs fresh page
@BeforeClass
public void goToLoginPage() {
    driver.get("https://example.com/login");
}

// CORRECT — navigates fresh before every test
@BeforeMethod
public void goToLoginPage() {
    driver.get("https://example.com/login");
}
```

---

**Q31: What is the difference between @BeforeSuite and @BeforeTest?**

**A:**

```xml
<!-- testng.xml with two <test> tags -->
<suite name="Suite">
    <test name="Smoke">...</test>
    <test name="Regression">...</test>
</suite>
```

```
@BeforeSuite → runs ONCE before "Smoke" starts
@BeforeTest  → runs ONCE before "Smoke" starts, then ONCE before "Regression" starts

So with 2 <test> tags:
- @BeforeSuite  runs 1 time total
- @BeforeTest   runs 2 times total (once per <test> tag)
```

Use `@BeforeSuite` for truly global setup (e.g., start a Docker container, initialise test database).
Use `@BeforeTest` for setup that is specific to each `<test>` block (e.g., authenticate for one environment but not another).

---

**Q32: Can TestNG run the same test class in parallel with different parameters?**

**A:** Yes, using `parallel="instances"` and multiple `<test>` tags with different parameters.

```xml
<suite name="Cross-Browser" parallel="tests" thread-count="3">

    <test name="Chrome Tests">
        <parameter name="browser" value="chrome"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>

    <test name="Firefox Tests">
        <parameter name="browser" value="firefox"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>

    <test name="Edge Tests">
        <parameter name="browser" value="edge"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>

</suite>
```

With `parallel="tests" thread-count="3"`, all three browser tests run simultaneously. `LoginTest` runs three times concurrently — once per browser — on three separate threads.

---

**Q33: How does TestNG handle test dependencies when a dependency is skipped vs failed?**

**A:** Both result in the dependent test being SKIPPED. TestNG treats a skipped dependency the same as a failed dependency.

```java
@Test
public void setupTest() {
    throw new SkipException("Deliberately skipped");
    // OR: Assert.fail("Deliberately failed");
}

@Test(dependsOnMethods = { "setupTest" })
public void dependentTest() {
    // This is SKIPPED regardless of whether setupTest was SKIPPED or FAILED
    // You only see the dependent test run if ALL dependencies PASSED
}

// To override this behaviour:
@Test(dependsOnMethods = { "setupTest" }, alwaysRun = true)
public void alwaysRunsTest() {
    // This runs even if setupTest failed or was skipped
}
```

---

**Q34: How do you implement a complete BaseTest class for API testing with TestNG?**

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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import listeners.TestListener;

import static org.hamcrest.Matchers.lessThan;

@Listeners({ TestListener.class })
public class BaseTest {

    protected RequestSpecification  requestSpec;
    protected ResponseSpecification responseSpec;

    @BeforeClass
    public void setUpSpec() {
        RestAssured.baseURI = System.getProperty("baseUrl", "https://jsonplaceholder.typicode.com");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .build();

        responseSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(5000L))
            .build();
    }

    @BeforeMethod
    public void logTestStart(java.lang.reflect.Method method) {
        System.out.println("\n--- Test Starting: " + method.getName() + " ---");
    }

    @AfterMethod
    public void logTestEnd(java.lang.reflect.Method method) {
        System.out.println("--- Test Ended: " + method.getName() + " ---\n");
    }
}
```

---

**Q35: What is the @Factory annotation in TestNG?**

**A:** `@Factory` creates multiple instances of a test class dynamically at runtime. Each instance is configured differently (e.g., different browsers, different user roles).

```java
public class LoginTest {
    private final String browser;

    public LoginTest(String browser) {
        this.browser = browser;
    }

    @Test
    public void loginTest() {
        System.out.println("Running on: " + browser);
        // Use this.browser to initialise the right driver
    }
}

// Factory class — creates instances
public class TestFactory {
    @Factory
    public Object[] createTests() {
        return new Object[] {
            new LoginTest("chrome"),
            new LoginTest("firefox"),
            new LoginTest("edge")
        };
    }
}
```

Difference between `@Factory` and DataProvider:
- DataProvider → one class instance, method called multiple times with different data
- @Factory → multiple class instances, each instance runs all methods independently

---

**Q36: How do you generate an Extent Report or Allure Report with TestNG?**

**A:**

**Extent Report** (via ITestListener):
```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.aventstack</groupId>
    <artifactId>extentreports</artifactId>
    <version>5.1.1</version>
</dependency>
```

```java
public class ExtentReportListener implements ITestListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        ExtentSparkReporter spark = new ExtentSparkReporter("reports/extent-report.html");
        spark.config().setDocumentTitle("API Test Report");
        spark.config().setReportName("Test Execution Report");
        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extent.createTest(result.getName());
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass("Test passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().fail(result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip(result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();  // write report to disk — MUST call this
    }
}
```

**Allure Report**:
```xml
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-testng</artifactId>
    <version>2.24.0</version>
</dependency>
```

```bash
# Generate and open Allure report
mvn test
allure serve target/allure-results
```

---

**Q37: What is the successPercentage attribute on @Test?**

**A:** Used with `invocationCount` — specifies what percentage of invocations must pass for the test to be considered passing.

```java
// Run 10 times — pass if at least 80% (8 out of 10) succeed
@Test(invocationCount = 10, successPercentage = 80)
public void stabilityTest() {
    // Tests a flaky operation — acceptable if it passes 8/10 times
    given().when().get("/sometimes-slow-endpoint").then().statusCode(200);
}

// Run 5 times — ALL must pass (default successPercentage = 100)
@Test(invocationCount = 5)
public void mustAlwaysPassTest() {
    given().when().get("/critical-endpoint").then().statusCode(200);
}
```

---

**Q38: How do you read a System Property in a TestNG test and pass it from Maven?**

**A:**

```java
// In your test or BaseTest
@BeforeClass
public void setUp() {
    String env = System.getProperty("env", "staging");       // default: staging
    String baseUrl = System.getProperty("baseUrl", "https://staging.example.com");

    System.out.println("Running against: " + env + " at " + baseUrl);
    RestAssured.baseURI = baseUrl;
}
```

```bash
# Pass from command line
mvn test -Denv=production -DbaseUrl=https://api.example.com

# Pass from pom.xml Surefire config
<configuration>
    <systemPropertyVariables>
        <env>staging</env>
        <baseUrl>https://staging.example.com</baseUrl>
    </systemPropertyVariables>
</configuration>
```

This pattern is used to run the same test suite against different environments without changing code.

---

**Q39: What is the difference between TestNG SKIPPED and TestNG FAILED?**

**A:**

| | FAILED | SKIPPED |
|---|---|---|
| Cause | Assertion failed or unexpected exception | `@BeforeMethod` failed, dependency failed, `SkipException` thrown, `enabled=false` |
| Shows in report as | Red FAIL | Yellow SKIP |
| Counts against pass rate | Yes | Depends on CI config |
| Retry applies | Yes (IRetryAnalyzer) | No |
| @AfterMethod runs | Yes | Yes |

```java
// Results in FAILED
@Test
public void failedTest() {
    Assert.assertEquals(200, 404);  // assertion failure
}

// Results in FAILED
@Test
public void exceptionTest() {
    throw new RuntimeException("Unexpected error");
}

// Results in SKIPPED
@Test
public void skippedTest() {
    throw new SkipException("Not applicable for this environment");
}

// Results in SKIPPED (enabled=false)
@Test(enabled = false)
public void disabledTest() { }
```

---

**Q40: How would you design a TestNG framework for API testing from scratch?**

**A:** Complete project structure:

```
src/
├── test/
│   ├── java/
│   │   ├── base/
│   │   │   └── BaseTest.java          ← requestSpec, responseSpec, BeforeClass
│   │   ├── tests/
│   │   │   ├── UserApiTest.java
│   │   │   ├── PostApiTest.java
│   │   │   └── AuthApiTest.java
│   │   ├── listeners/
│   │   │   ├── TestListener.java      ← ITestListener — logging
│   │   │   ├── RetryAnalyzer.java     ← IRetryAnalyzer
│   │   │   └── RetryListener.java     ← IAnnotationTransformer — apply retry globally
│   │   ├── utils/
│   │   │   ├── ApiConstants.java      ← URLs, endpoints
│   │   │   └── TestDataProviders.java ← All @DataProvider methods
│   │   └── models/
│   │       ├── Post.java              ← POJO for serialization
│   │       └── User.java
│   └── resources/
│       ├── testng.xml
│       ├── testng-smoke.xml
│       ├── testng-regression.xml
│       ├── schemas/
│       │   ├── post-schema.json
│       │   └── user-schema.json
│       └── data/
│           └── login-data.csv
```

```java
// Complete BaseTest
@Listeners({ listeners.TestListener.class })
public class BaseTest {

    protected RequestSpecification requestSpec;

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = System.getProperty("baseUrl", ApiConstants.BASE_URL);
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .build();
    }
}

// Complete test class
public class PostApiTest extends BaseTest {

    @Test(groups = { "smoke", "regression" },
          description = "Verify creating a post returns 201 with correct fields")
    public void createPost() {
        Map<String, Object> body = Map.of(
            "title", "Test Post",
            "body", "Test body",
            "userId", 1
        );

        given().spec(requestSpec)
            .body(body)
            .when().post("/posts")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("title", equalTo("Test Post"))
            .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"));
    }

    @Test(groups = { "regression" },
          dataProvider = "postIds",
          dataProviderClass = TestDataProviders.class)
    public void getPostById(int postId) {
        given().spec(requestSpec)
            .pathParam("id", postId)
            .when().get("/posts/{id}")
            .then()
            .statusCode(200)
            .body("id", equalTo(postId));
    }
}
```
