# Part 5 — TestNG | 40 Questions | Full Answers + Code

> CV context: Kupeshanth used TestNG at Qoria Lanka with Selenium, in Singer Page project with Serenity+Cucumber, and in RestAssured test suites. These 40 questions cover everything an interviewer will ask.

---

## Q1. What is TestNG and why use it over JUnit?

**TestNG** (Test Next Generation) is a testing framework for Java inspired by JUnit and NUnit, but designed to cover a wider range of testing categories: unit, functional, end-to-end, and integration.

**Why TestNG over JUnit 4:**

| Feature | TestNG | JUnit 4 |
|---|---|---|
| Parallel execution | Built-in (testng.xml) | Needs plugins |
| Data-driven | @DataProvider (native) | @Parameterized (limited) |
| Grouping | @Test(groups="smoke") | @Category (verbose) |
| Dependencies | dependsOnMethods/Groups | None |
| Listeners | Rich ITestListener | RunListener (basic) |
| Soft assertions | SoftAssert built-in | AssertJ needed |
| Reports | Built-in HTML + emailable | Basic |
| Configuration scope | @BeforeSuite/Test/Class/Method | @BeforeClass/Before |

**Key annotations TestNG has that JUnit 4 lacks:**
- `@BeforeSuite` / `@AfterSuite`
- `@BeforeTest` / `@AfterTest`
- `@DataProvider` with parallel support
- `@Factory`
- `@Listeners`

```java
// TestNG test class - full structure
import org.testng.annotations.*;
import org.testng.Assert;

public class LoginTest {

    @BeforeSuite
    public void suiteSetup() {
        System.out.println("Once before entire suite");
    }

    @BeforeClass
    public void classSetup() {
        System.out.println("Once before all methods in this class");
    }

    @BeforeMethod
    public void methodSetup() {
        System.out.println("Before each test method");
    }

    @Test(groups = "smoke", priority = 1, description = "Login with valid credentials")
    public void testValidLogin() {
        Assert.assertTrue(true, "Login should succeed");
    }

    @AfterMethod
    public void methodTeardown() {
        System.out.println("After each test method");
    }

    @AfterClass
    public void classTeardown() {
        System.out.println("Once after all methods in this class");
    }

    @AfterSuite
    public void suiteTeardown() {
        System.out.println("Once after entire suite");
    }
}
```

---

## Q2. What is the complete annotation execution order? (with diagram)

```
@BeforeSuite
    @BeforeTest
        @BeforeClass
            @BeforeMethod  ──┐
            @Test          ──┤ (repeats for each @Test)
            @AfterMethod   ──┘
        @AfterClass
    @AfterTest
@AfterSuite
```

**Full example proving the order:**

```java
public class OrderDemoTest {

    @BeforeSuite
    public void bs() { System.out.println("1. @BeforeSuite"); }

    @BeforeTest
    public void bt() { System.out.println("2. @BeforeTest"); }

    @BeforeClass
    public void bc() { System.out.println("3. @BeforeClass"); }

    @BeforeMethod
    public void bm() { System.out.println("4. @BeforeMethod"); }

    @Test
    public void test1() { System.out.println("5. @Test - test1"); }

    @Test
    public void test2() { System.out.println("5. @Test - test2"); }

    @AfterMethod
    public void am() { System.out.println("6. @AfterMethod"); }

    @AfterClass
    public void ac() { System.out.println("7. @AfterClass"); }

    @AfterTest
    public void at() { System.out.println("8. @AfterTest"); }

    @AfterSuite
    public void as() { System.out.println("9. @AfterSuite"); }
}
```

**Output:**
```
1. @BeforeSuite
2. @BeforeTest
3. @BeforeClass
4. @BeforeMethod
5. @Test - test1
6. @AfterMethod
4. @BeforeMethod
5. @Test - test2
6. @AfterMethod
7. @AfterClass
8. @AfterTest
9. @AfterSuite
```

**Rule:** `@BeforeSuite` and `@AfterSuite` run once per suite (testng.xml). `@BeforeTest`/`@AfterTest` run once per `<test>` tag in testng.xml. `@BeforeClass`/`@AfterClass` run once per class. `@BeforeMethod`/`@AfterMethod` wrap every single `@Test`.

---

## Q3. @BeforeClass vs @BeforeMethod — when to use each?

**@BeforeClass** — runs once before the first test method in the class.
Use for: expensive one-time setup (browser launch, DB connection, reading config file).

**@BeforeMethod** — runs before every single test method.
Use for: resetting state (navigate to home page, clear cookies, reset test data).

```java
public class EcommerceTest {

    WebDriver driver;

    // Browser launches ONCE for the whole class
    @BeforeClass
    public void launchBrowser() {
        driver = new ChromeDriver();
        driver.get("https://shop.example.com");
        driver.manage().window().maximize();
    }

    // Each test starts fresh at home page
    @BeforeMethod
    public void navigateHome() {
        driver.get("https://shop.example.com");
        driver.manage().deleteAllCookies();
    }

    @Test
    public void testAddToCart() {
        // test logic
    }

    @Test
    public void testCheckout() {
        // test logic
    }

    // Browser closes ONCE after all tests
    @AfterClass
    public void closeBrowser() {
        if (driver != null) driver.quit();
    }
}
```

**Interview tip:** If you launch the browser in `@BeforeMethod`, it opens/closes for EVERY test — expensive. Use `@BeforeClass` for the browser, `@BeforeMethod` for navigation + cleanup.

---

## Q4. What happens if @BeforeMethod fails?

When `@BeforeMethod` throws an exception or an assertion fails:
1. The **corresponding `@Test` is marked as SKIPPED** (not FAILED).
2. The `@AfterMethod` **still runs** if `alwaysRun = true` is set.
3. Other `@Test` methods have their own `@BeforeMethod` calls — they are unaffected.

```java
@BeforeMethod
public void setUp() {
    // If this throws, the @Test below is SKIPPED
    driver.get("https://example.com");
    // If element not found here, test is skipped
}

@Test
public void testLogin() {
    // This is SKIPPED if @BeforeMethod above failed
}

// This STILL RUNS even if @BeforeMethod failed
@AfterMethod(alwaysRun = true)
public void tearDown() {
    if (driver != null) driver.quit();
}
```

**Why alwaysRun = true matters:** Without it, if `@BeforeMethod` fails, the `@AfterMethod` is ALSO skipped — your driver is never quit, causing resource leaks. Always set `alwaysRun = true` on cleanup methods.

---

## Q5. What are all @Test attributes?

```java
@Test(
    groups           = {"smoke", "regression"},   // group membership
    priority         = 1,                          // lower = runs first (default 0)
    enabled          = true,                       // false = skip this test
    timeOut          = 5000,                       // milliseconds; fails if exceeded
    invocationCount  = 3,                          // run this test 3 times
    threadPoolSize   = 2,                          // threads for invocationCount
    dependsOnMethods = {"testLogin"},              // must pass before this runs
    dependsOnGroups  = {"smoke"},                  // group must pass first
    alwaysRun        = false,                      // run even if dependency failed
    expectedExceptions = {IllegalArgumentException.class}, // test passes if this is thrown
    description      = "Verify login with valid credentials",
    successPercentage = 80                         // pass if 80% of invocations succeed
)
public void testDashboard() {
    // test body
}
```

**Full examples:**

```java
// Test that must complete within 3 seconds
@Test(timeOut = 3000)
public void testResponseTime() {
    long start = System.currentTimeMillis();
    apiCall();
    // Will fail with TimeoutException if > 3000ms
}

// Test that runs 5 times (load/stress scenario)
@Test(invocationCount = 5, threadPoolSize = 5)
public void testConcurrentLogin() {
    // Runs 5 times in 5 threads simultaneously
}

// Test that expects an exception
@Test(expectedExceptions = {NumberFormatException.class})
public void testInvalidInput() {
    Integer.parseInt("not-a-number"); // throws NFE — test PASSES
}

// Disabled test
@Test(enabled = false)
public void testWIP() {
    // This test is skipped — won't even appear as failed
}
```

---

## Q6. What is testng.xml structure? Walk through every element.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">

<!-- suite: top-level container. name is shown in reports. -->
<suite name="RegressionSuite"
       verbose="2"
       parallel="tests"
       thread-count="4"
       time-out="30000">

    <!-- listeners: attach globally to all tests in suite -->
    <listeners>
        <listener class-name="listeners.ExtentReportListener"/>
        <listener class-name="listeners.ScreenshotListener"/>
    </listeners>

    <!-- parameters: key-value pairs passed to @Parameters -->
    <parameter name="browser" value="chrome"/>
    <parameter name="env"     value="staging"/>

    <!-- test: logical grouping (maps to @BeforeTest/@AfterTest scope) -->
    <test name="LoginTests" parallel="methods" thread-count="2">

        <!-- groups: which groups to run or exclude -->
        <groups>
            <run>
                <include name="smoke"/>
                <include name="regression"/>
                <exclude name="wip"/>
            </run>
        </groups>

        <!-- classes to include in this <test> -->
        <classes>
            <class name="tests.LoginTest">
                <!-- optionally include only specific methods -->
                <methods>
                    <include name="testValidLogin"/>
                    <exclude name="testOldLogin"/>
                </methods>
            </class>
            <class name="tests.DashboardTest"/>
        </classes>
    </test>

    <!-- second <test> block -->
    <test name="CheckoutTests">
        <classes>
            <class name="tests.CartTest"/>
            <class name="tests.CheckoutTest"/>
        </classes>
    </test>

</suite>
```

**Element reference:**

| Element | Purpose |
|---|---|
| `<suite>` | Root element; name shown in reports |
| `<test>` | Logical group; has its own Before/AfterTest scope |
| `<classes>` | Container for test classes |
| `<class>` | Individual test class |
| `<methods>` | Include/exclude specific methods |
| `<groups>` | Specify which groups to run/exclude |
| `<listeners>` | Register listeners globally |
| `<parameter>` | Pass values to @Parameters |

---

## Q7. What is parallel="tests" vs "classes" vs "methods" vs "instances"?

**parallel** attribute on `<suite>` or `<test>` controls what runs concurrently.

```xml
<!-- parallel="tests": each <test> tag runs in its own thread -->
<suite name="Suite" parallel="tests" thread-count="2">
    <test name="ChromeTests">...</test>  <!-- Thread 1 -->
    <test name="FirefoxTests">...</test> <!-- Thread 2 -->
</suite>

<!-- parallel="classes": each <class> runs in its own thread -->
<suite name="Suite" parallel="classes" thread-count="3">
    <test name="AllTests">
        <classes>
            <class name="LoginTest"/>     <!-- Thread 1 -->
            <class name="SearchTest"/>    <!-- Thread 2 -->
            <class name="CartTest"/>      <!-- Thread 3 -->
        </classes>
    </test>
</suite>

<!-- parallel="methods": each @Test method runs in its own thread -->
<suite name="Suite" parallel="methods" thread-count="5">
    <test name="AllTests">
        <classes>
            <class name="LoginTest"/>  <!-- All methods run in parallel -->
        </classes>
    </test>
</suite>

<!-- parallel="instances": each instance of a class runs in its own thread -->
<!-- Used with @Factory -->
<suite name="Suite" parallel="instances" thread-count="3">
    ...
</suite>
```

**When to use what:**

| Mode | Use case |
|---|---|
| `tests` | Cross-browser: one `<test>` per browser |
| `classes` | Independent test classes with no shared state |
| `methods` | Fast independent test methods |
| `instances` | @Factory pattern — each test instance has its own data |

**Thread safety rule:** When running parallel, each thread must have its own WebDriver. Use `ThreadLocal<WebDriver>`:

```java
public class DriverManager {
    private static ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    public static void setDriver(WebDriver driver) {
        driverThread.set(driver);
    }

    public static WebDriver getDriver() {
        return driverThread.get();
    }

    public static void quitDriver() {
        driverThread.get().quit();
        driverThread.remove();
    }
}
```

---

## Q8. What is thread-count?

`thread-count` specifies the **maximum number of threads** TestNG uses for parallel execution.

```xml
<suite name="Suite" parallel="methods" thread-count="4">
    <!-- At most 4 test methods run simultaneously -->
</suite>
```

**Rules:**
- If you have 10 methods and `thread-count="4"`, 4 run at a time; as one finishes the next queues.
- `thread-count` has no effect if `parallel` is not set.
- Setting it too high causes resource contention; too low defeats the purpose.
- For cross-browser with 3 browsers: `parallel="tests"` + `thread-count="3"`.

```xml
<!-- Typical CI cross-browser setup -->
<suite name="CrossBrowserSuite" parallel="tests" thread-count="3" verbose="1">
    <test name="Chrome">
        <parameter name="browser" value="chrome"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
    <test name="Firefox">
        <parameter name="browser" value="firefox"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
    <test name="Edge">
        <parameter name="browser" value="edge"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
</suite>
```

---

## Q9. What is @DataProvider? Full example with multiple columns.

`@DataProvider` feeds multiple sets of data to a single `@Test` method. The test runs once per row of data.

```java
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;

public class LoginDataDrivenTest {

    // DataProvider returns Object[][] — each row is one test run
    @DataProvider(name = "loginData")
    public Object[][] provideLoginData() {
        return new Object[][] {
            // username,       password,    expectedResult
            { "admin@test.com", "Admin123!", true  },
            { "user@test.com",  "User456!",  true  },
            { "invalid@test.com","wrong",    false },
            { "",               "pass",      false },
            { "user@test.com",  "",          false }
        };
    }

    // dataProvider = name of the @DataProvider method
    @Test(dataProvider = "loginData", description = "Login with multiple credentials")
    public void testLogin(String username, String password, boolean shouldSucceed) {
        System.out.printf("Testing: %s / %s → expected: %s%n",
                          username, password, shouldSucceed);

        // Simulate login
        boolean result = performLogin(username, password);
        Assert.assertEquals(result, shouldSucceed,
            "Login result mismatch for user: " + username);
    }

    private boolean performLogin(String u, String p) {
        return u.contains("@") && p.length() >= 6;
    }
}
```

**With custom objects:**

```java
public class User {
    public String email, password, role;
    public User(String email, String password, String role) {
        this.email = email; this.password = password; this.role = role;
    }
}

@DataProvider(name = "users")
public Object[][] provideUsers() {
    return new Object[][] {
        { new User("admin@co.com", "pass", "ADMIN") },
        { new User("viewer@co.com","pass", "VIEWER") }
    };
}

@Test(dataProvider = "users")
public void testRoleAccess(User user) {
    System.out.println("Testing role: " + user.role);
}
```

---

## Q10. What is a cross-class DataProvider?

A DataProvider defined in a **separate utility class**, referenced by `dataProviderClass`:

```java
// TestDataProvider.java — reusable data class
public class TestDataProvider {

    @DataProvider(name = "searchTerms")
    public static Object[][] searchData() {
        return new Object[][] {
            { "laptop",   "Electronics" },
            { "keyboard", "Accessories" },
            { "monitor",  "Electronics" }
        };
    }

    @DataProvider(name = "credentials")
    public static Object[][] loginData() {
        return new Object[][] {
            { "admin",   "admin123" },
            { "manager", "mgr456"  }
        };
    }
}

// SearchTest.java — uses the cross-class provider
public class SearchTest {

    @Test(
        dataProvider      = "searchTerms",
        dataProviderClass = TestDataProvider.class
    )
    public void testSearch(String term, String category) {
        System.out.println("Searching: " + term + " in " + category);
    }
}
```

**Benefit:** One DataProvider class supplies data to multiple test classes without duplication.

---

## Q11. What is parallel DataProvider?

By default, DataProvider feeds rows **sequentially**. Setting `parallel = true` feeds rows **simultaneously** in multiple threads:

```java
@DataProvider(name = "parallelUsers", parallel = true)
public Object[][] parallelData() {
    return new Object[][] {
        { "user1@test.com", "pass1" },
        { "user2@test.com", "pass2" },
        { "user3@test.com", "pass3" },
        { "user4@test.com", "pass4" }
    };
}

@Test(dataProvider = "parallelUsers")
public void testParallelLogin(String email, String pass) {
    // Runs in parallel — each row in its own thread
    System.out.println(Thread.currentThread().getName() + " testing: " + email);
}
```

Thread count for parallel DataProvider is controlled in testng.xml:
```xml
<suite name="Suite" data-provider-thread-count="4">
```

**Warning:** The test method must be thread-safe. Use `ThreadLocal<WebDriver>` if using Selenium.

---

## Q12. How to read CSV data in @DataProvider?

```java
import org.testng.annotations.DataProvider;
import java.io.*;
import java.util.*;

public class CSVDataProvider {

    @DataProvider(name = "csvLogin")
    public Object[][] readCSV() throws IOException {
        List<Object[]> data = new ArrayList<>();

        // CSV file: src/test/resources/testdata/login.csv
        String filePath = "src/test/resources/testdata/login.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; } // skip header row
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    data.add(new Object[]{
                        parts[0].trim(),  // username
                        parts[1].trim(),  // password
                        Boolean.parseBoolean(parts[2].trim()) // expected
                    });
                }
            }
        }

        return data.toArray(new Object[0][]);
    }
}
```

**CSV file (login.csv):**
```
username,password,expected
admin@test.com,Admin123!,true
invalid@test.com,wrong,false
,empty,false
```

**Using it:**
```java
@Test(dataProvider = "csvLogin", dataProviderClass = CSVDataProvider.class)
public void testLoginCSV(String user, String pass, boolean expected) {
    boolean result = login(user, pass);
    Assert.assertEquals(result, expected);
}
```

---

## Q13. What is @Parameters and @Optional?

`@Parameters` injects values from testng.xml `<parameter>` tags at runtime.

```xml
<!-- testng.xml -->
<suite name="Suite">
    <parameter name="browser" value="chrome"/>
    <parameter name="env"     value="staging"/>
    <test name="Tests">
        <parameter name="username" value="admin@test.com"/>
        <classes>
            <class name="tests.SetupTest"/>
        </classes>
    </test>
</suite>
```

```java
public class SetupTest {

    WebDriver driver;

    @Parameters({"browser", "env"})
    @BeforeSuite
    public void suitSetup(String browser, String env) {
        System.out.println("Browser: " + browser + ", Env: " + env);
        // Initialize driver based on browser param
    }

    // @Optional provides a default if parameter is missing from testng.xml
    @Parameters("username")
    @BeforeClass
    public void classSetup(@Optional("default@test.com") String username) {
        System.out.println("Username: " + username);
    }

    @Test
    public void testSomething() {
        // test body
    }
}
```

**Key points:**
- `@Parameters` throws `TestNGException` if the parameter is not in testng.xml (unless `@Optional` is used).
- `@Optional("default")` provides a fallback value.
- Parameters are inherited from `<suite>` to `<test>` to `<class>` to method level.
- Can also pass via Maven: `mvn test -Dbrowser=firefox` — but only works if testng.xml reads system properties.

---

## Q14. What are groups? How to include/exclude in testng.xml?

**Groups** tag tests by category so you can run specific subsets.

```java
public class ProductTest {

    @Test(groups = {"smoke", "regression"})
    public void testProductList() { /* ... */ }

    @Test(groups = {"regression"})
    public void testProductFilter() { /* ... */ }

    @Test(groups = {"smoke"})
    public void testProductSearch() { /* ... */ }

    @Test(groups = {"wip"})
    public void testNewFeature() { /* Not ready yet */ }
}
```

**Include/Exclude in testng.xml:**

```xml
<!-- Run only smoke tests -->
<test name="SmokeRun">
    <groups>
        <run>
            <include name="smoke"/>
        </run>
    </groups>
    <classes>
        <class name="tests.ProductTest"/>
    </classes>
</test>

<!-- Run regression but exclude wip -->
<test name="RegressionRun">
    <groups>
        <run>
            <include name="regression"/>
            <exclude name="wip"/>
        </run>
    </groups>
    <classes>
        <class name="tests.ProductTest"/>
    </classes>
</test>
```

**Define groups with dependencies:**

```xml
<groups>
    <define name="all">
        <include name="smoke"/>
        <include name="regression"/>
    </define>
    <run>
        <include name="all"/>
        <exclude name="wip"/>
    </run>
</groups>
```

**From Maven command line:**
```bash
mvn test -Dgroups="smoke"
mvn test -Dgroups="smoke,regression"
mvn test -DexcludedGroups="wip"
```

---

## Q15. What is dependsOnMethods? When is it dangerous?

`dependsOnMethods` makes a test run only if its specified dependency passed.

```java
public class OrderFlowTest {

    @Test(priority = 1)
    public void testLogin() {
        System.out.println("Login step");
        // Assert login success
    }

    @Test(priority = 2, dependsOnMethods = {"testLogin"})
    public void testAddToCart() {
        // Only runs if testLogin PASSED
        System.out.println("Add to cart");
    }

    @Test(priority = 3, dependsOnMethods = {"testAddToCart"})
    public void testCheckout() {
        // Only runs if testAddToCart PASSED
        System.out.println("Checkout");
    }
}
```

**When it is dangerous:**

1. **Chain effect:** If `testLogin` fails, `testAddToCart` is SKIPPED, and `testCheckout` is SKIPPED. One failure skips the whole chain.
2. **Test isolation broken:** Tests are no longer independent — violates the unit testing principle.
3. **Hard to debug:** A failure in step 1 masks whether steps 2 and 3 work correctly.
4. **Parallel issues:** Dependencies force sequential execution, reducing parallel benefit.

**Better alternative:** Use `@BeforeMethod` to set up state rather than chaining tests:

```java
@BeforeMethod
public void loginAndSetup() {
    // Always log in before each test — no dependency chains
    login("admin", "pass");
}

@Test
public void testAddToCart() { /* Independently tests cart */ }

@Test
public void testCheckout() { /* Independently tests checkout */ }
```

**Legitimate use:** UI smoke test flows where steps literally cannot run without prior steps (e.g., "create order" → "verify order ID" → "cancel order" using same order ID).

---

## Q16. What is dependsOnGroups?

Like `dependsOnMethods` but at group level — the test only runs if all tests in the specified group passed.

```java
public class PaymentTest {

    @Test(groups = "auth")
    public void testValidToken() { /* ... */ }

    @Test(groups = "auth")
    public void testTokenExpiry() { /* ... */ }

    // Only runs if ALL tests in "auth" group passed
    @Test(dependsOnGroups = {"auth"})
    public void testProcessPayment() {
        System.out.println("All auth tests passed, now testing payment");
    }
}
```

```xml
<!-- testng.xml: group dependencies -->
<test name="FullFlow">
    <groups>
        <run>
            <include name="auth"/>
            <include name="payment"/>
        </run>
    </groups>
    <classes>
        <class name="tests.PaymentTest"/>
    </classes>
</test>
```

---

## Q17. What is @Factory annotation?

`@Factory` creates **multiple instances** of a test class at runtime, each with different data. Useful when you want to run the same tests with different configurations.

```java
// Test class that takes constructor parameters
public class BrowserTest {

    private String browser;
    private WebDriver driver;

    // Constructor — receives the browser type
    public BrowserTest(String browser) {
        this.browser = browser;
    }

    @BeforeClass
    public void setup() {
        System.out.println("Launching: " + browser);
        // driver = BrowserFactory.create(browser);
    }

    @Test
    public void testHomePage() {
        System.out.println(browser + ": testing home page");
    }

    @Test
    public void testLogin() {
        System.out.println(browser + ": testing login");
    }

    @AfterClass
    public void teardown() {
        System.out.println("Closing: " + browser);
    }
}

// Factory class — creates instances
public class BrowserTestFactory {

    @Factory
    public Object[] createTests() {
        return new Object[] {
            new BrowserTest("chrome"),
            new BrowserTest("firefox"),
            new BrowserTest("edge")
        };
    }
}
```

**testng.xml — only reference the factory class:**
```xml
<test name="CrossBrowserTests" parallel="instances" thread-count="3">
    <classes>
        <class name="factories.BrowserTestFactory"/>
    </classes>
</test>
```

**Result:** TestNG creates 3 instances of `BrowserTest` and runs all `@Test` methods in each — 6 tests total (2 tests × 3 browsers), all in parallel.

---

## Q18. What is alwaysRun = true? Why is it important in @AfterMethod?

`alwaysRun = true` on a configuration method (`@AfterMethod`, `@AfterClass`, etc.) means it **runs even if a previous configuration method or test failed/was skipped**.

```java
@BeforeMethod
public void setUp() {
    driver = new ChromeDriver();    // might fail if driver not found
    driver.get("https://example.com");
}

// WITHOUT alwaysRun: if @BeforeMethod fails, @AfterMethod is SKIPPED
// The ChromeDriver process keeps running — memory leak!

@AfterMethod(alwaysRun = true)   // ← CRITICAL
public void tearDown() {
    // This ALWAYS runs, even if @BeforeMethod or @Test failed
    if (driver != null) {
        driver.quit();           // Always clean up
    }
}
```

**Why critical for QA automation:**
1. **Resource cleanup:** Browser/driver processes are cleaned up even on failure.
2. **Screenshot capture:** Take screenshot on failure in `@AfterMethod`.
3. **Logging:** Always log test completion status.

```java
@AfterMethod(alwaysRun = true)
public void afterEachTest(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE) {
        // Capture screenshot on failure
        TakesScreenshot ts = (TakesScreenshot) driver;
        File src = ts.getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(src, new File("screenshots/" + result.getName() + ".png"));
    }
    driver.quit();
}
```

---

## Q19. What is ITestListener? Implement screenshot on failure.

`ITestListener` is an interface with callbacks for every test lifecycle event. Implement it to add custom behavior (screenshots, reporting, logging) without modifying test classes.

```java
import org.testng.*;
import org.openqa.selenium.*;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;

public class ScreenshotListener implements ITestListener {

    // Called when any test FAILS
    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("FAILED: " + result.getName());

        // Get the test class instance to access the driver
        Object testInstance = result.getInstance();

        // Assumes your test class has a 'driver' field
        try {
            WebDriver driver = (WebDriver) testInstance.getClass()
                                    .getDeclaredField("driver")
                                    .get(testInstance);
            takeScreenshot(driver, result.getName());
        } catch (Exception e) {
            System.out.println("Could not take screenshot: " + e.getMessage());
        }
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("STARTING: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("PASSED: " + result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("SKIPPED: " + result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("Test context started: " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.printf("Finished: %d passed, %d failed, %d skipped%n",
            context.getPassedTests().size(),
            context.getFailedTests().size(),
            context.getSkippedTests().size());
    }

    private void takeScreenshot(WebDriver driver, String testName) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File src = ts.getScreenshotAs(OutputType.FILE);
        String dest = "test-output/screenshots/" + testName + "_" +
                      System.currentTimeMillis() + ".png";
        try {
            FileUtils.copyFile(src, new File(dest));
            System.out.println("Screenshot: " + dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

**Register in testng.xml:**
```xml
<listeners>
    <listener class-name="listeners.ScreenshotListener"/>
</listeners>
```

**Or in test class:**
```java
@Listeners(ScreenshotListener.class)
public class LoginTest { ... }
```

---

## Q20. What is ISuiteListener?

`ISuiteListener` fires at suite start and finish — useful for global setup/teardown (e.g., start test reporting server, send Slack notification).

```java
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteReportListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        System.out.println("Suite STARTED: " + suite.getName());
        // Initialize Extent Report
        // Send "Tests starting" Slack notification
        // Connect to DB
    }

    @Override
    public void onFinish(ISuite suite) {
        System.out.println("Suite FINISHED: " + suite.getName());

        // Print summary
        suite.getResults().forEach((testName, result) -> {
            int passed  = result.getTestContext().getPassedTests().size();
            int failed  = result.getTestContext().getFailedTests().size();
            int skipped = result.getTestContext().getSkippedTests().size();
            System.out.printf("[%s] Passed: %d, Failed: %d, Skipped: %d%n",
                              testName, passed, failed, skipped);
        });

        // Flush Extent Report
        // Send test result email/Slack notification
    }
}
```

Register in testng.xml:
```xml
<listeners>
    <listener class-name="listeners.SuiteReportListener"/>
</listeners>
```

---

## Q21. What is IAnnotationTransformer?

`IAnnotationTransformer` lets you **modify `@Test` annotations at runtime** without changing source code. Useful for: dynamically adding retry analyzers, enabling/disabling tests based on environment, changing timeouts in CI.

```java
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {

        // Attach RetryAnalyzer to ALL tests globally
        annotation.setRetryAnalyzer(RetryAnalyzer.class);

        // Dynamically disable slow tests in CI
        if (System.getProperty("ci") != null) {
            if (annotation.getTimeOut() == 0) {
                annotation.setTimeOut(10000); // 10s timeout in CI
            }
        }
    }
}
```

Register in testng.xml:
```xml
<listeners>
    <listener class-name="listeners.RetryTransformer"/>
</listeners>
```

**Key point:** `IAnnotationTransformer` is called before tests run, so it can change behavior globally without touching test code.

---

## Q22. What is IRetryAnalyzer? Full implementation.

`IRetryAnalyzer` automatically re-runs failed tests a specified number of times before marking them as FAILED.

```java
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount   = 0;
    private static final int MAX_RETRY = 2; // retry up to 2 times (3 total runs)

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            System.out.printf("Retrying [%s] — attempt %d of %d%n",
                              result.getName(), retryCount, MAX_RETRY);
            return true;  // true = retry
        }
        return false;     // false = mark as FAILED
    }
}
```

**Apply to specific test:**
```java
@Test(retryAnalyzer = RetryAnalyzer.class)
public void testFlakyAPI() {
    // Runs up to 3 times total before failing
}
```

**Execution flow:**
```
Attempt 1 → FAILS → retry(result) returns true
Attempt 2 → FAILS → retry(result) returns true
Attempt 3 → FAILS → retry(result) returns false → MARKED FAILED
```

```
Attempt 1 → FAILS → retry(result) returns true
Attempt 2 → PASSES → MARKED PASSED (previous failures recorded as SKIPPED)
```

---

## Q23. How to apply retry to ALL tests globally?

Use `IAnnotationTransformer` to inject `RetryAnalyzer` into every `@Test` without modifying test classes:

```java
public class GlobalRetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {
        // Only add retry if not already set
        if (annotation.getRetryAnalyzerClass() == null) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}
```

**testng.xml:**
```xml
<listeners>
    <listener class-name="listeners.GlobalRetryTransformer"/>
</listeners>
```

**Result:** Every `@Test` in the suite automatically retries up to 2 times on failure — no code changes needed.

---

## Q24. What is SkipException?

Throwing `SkipException` from within a test marks it as **SKIPPED** instead of FAILED. Useful for conditional skipping based on runtime conditions.

```java
import org.testng.SkipException;
import org.testng.annotations.Test;

public class ConditionalTest {

    @Test
    public void testPaymentGateway() {
        String env = System.getProperty("env");

        // Skip this test if running in development environment
        if ("dev".equals(env)) {
            throw new SkipException("Payment gateway not available in DEV — skipping");
        }

        // Actual test logic
        System.out.println("Testing payment in: " + env);
    }

    @Test
    public void testFeatureFlag() {
        boolean featureEnabled = isFeatureEnabled("NEW_DASHBOARD");

        if (!featureEnabled) {
            throw new SkipException("NEW_DASHBOARD feature flag is OFF — test skipped");
        }

        // Test the new dashboard
    }

    private boolean isFeatureEnabled(String flag) {
        return Boolean.parseBoolean(System.getProperty(flag, "false"));
    }
}
```

**SkipException vs test.enabled=false:**
- `enabled = false`: Always skipped, never even attempted.
- `SkipException`: Decision made at runtime — can check config, flags, environment.

---

## Q25. What is soft assertion in TestNG?

**Hard assertion** (default `Assert`): When one assertion fails, the test **stops immediately**. Remaining assertions are not executed.

**Soft assertion** (`SoftAssert`): All assertions are collected. The test continues even if one fails. At the end, `assertAll()` throws if any failed.

```java
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

public class UserProfileTest {

    @Test
    public void testUserProfile() {
        SoftAssert soft = new SoftAssert();

        // All these run even if one fails
        soft.assertEquals(getUsername(), "john_doe",  "Username mismatch");
        soft.assertEquals(getEmail(),    "john@co.com","Email mismatch");
        soft.assertTrue(isActive(),                    "User should be active");
        soft.assertEquals(getRole(),     "ADMIN",      "Role mismatch");

        // Must call assertAll() — throws if any soft assertion failed
        soft.assertAll();
    }

    // Simulate API calls
    private String getUsername() { return "john_doe"; }
    private String getEmail()    { return "wrong@co.com"; } // This fails
    private boolean isActive()   { return true; }
    private String getRole()     { return "ADMIN"; }
}
```

**Output:** Test runs all 4 assertions, then fails at `assertAll()` listing all failures.

**When to use soft assertions:**
- Validating multiple fields on a form/API response — you want to see ALL failures at once.
- Not for preconditions — if the page isn't loaded, soft-asserting its content makes no sense.

---

## Q26. What is the difference between SKIPPED and FAILED in TestNG?

| State | Cause | Counted as failure? |
|---|---|---|
| PASSED | Test completed, all assertions passed | No |
| FAILED | Test completed, an assertion/exception failed | Yes |
| SKIPPED | Test did not run | No (but noted) |

**SKIPPED causes:**
1. `dependsOnMethods` dependency failed.
2. `@BeforeMethod` threw an exception.
3. `SkipException` thrown explicitly.
4. Test marked `enabled = false`.

```java
@Test
public void testA() {
    Assert.fail("Deliberately failing"); // FAILED
}

// testB is SKIPPED because testA FAILED
@Test(dependsOnMethods = {"testA"})
public void testB() {
    System.out.println("This will be SKIPPED");
}
```

**Why SKIPPED matters in CI:** A CI pipeline may treat SKIPPED differently than FAILED. Too many SKIPPED tests can indicate poor dependency design. Failed tests break the build; skipped tests may just be flagged.

---

## Q27. What is successPercentage attribute?

Allows a test to PASS even if some invocations fail, as long as the success rate meets the threshold.

```java
// Test runs 10 times (invocationCount=10)
// Passes if at least 80% (8 out of 10) succeed
@Test(invocationCount = 10, successPercentage = 80)
public void testAPIReliability() {
    // Simulate occasional failure (random)
    if (Math.random() < 0.15) { // 15% chance of failure
        Assert.fail("Simulated intermittent failure");
    }
    System.out.println("API call succeeded");
}
```

**Use case:** Load testing where you accept a small failure rate (e.g., "99% of API calls must succeed").

---

## Q28. How to run only smoke tests from Maven?

**Option 1: Groups via Maven Surefire:**
```bash
mvn test -Dgroups="smoke"
mvn test -Dgroups="smoke,sanity"
mvn test -DexcludedGroups="wip,slow"
```

**Option 2: Specify testng.xml via Maven:**
```bash
mvn test -DsuiteXmlFile=testng-smoke.xml
```

**Option 3: Configure in pom.xml:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
    <configuration>
        <groups>smoke</groups>
        <!-- Or specify file: -->
        <suiteXmlFiles>
            <suiteXmlFile>src/test/resources/testng-smoke.xml</suiteXmlFile>
        </suiteXmlFiles>
    </configuration>
</plugin>
```

**testng-smoke.xml:**
```xml
<suite name="SmokeSuite">
    <test name="SmokeTests">
        <groups>
            <run>
                <include name="smoke"/>
            </run>
        </groups>
        <classes>
            <class name="tests.LoginTest"/>
            <class name="tests.HomeTest"/>
        </classes>
    </test>
</suite>
```

---

## Q29. What is the Surefire plugin? How to configure it?

Maven Surefire Plugin runs Java tests during the `test` phase of the Maven build lifecycle.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version>
            <configuration>

                <!-- Point to testng.xml -->
                <suiteXmlFiles>
                    <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
                </suiteXmlFiles>

                <!-- Run specific groups -->
                <groups>smoke,regression</groups>
                <excludedGroups>wip</excludedGroups>

                <!-- System properties passed to tests -->
                <systemPropertyVariables>
                    <browser>chrome</browser>
                    <env>staging</env>
                </systemPropertyVariables>

                <!-- Parallel execution settings -->
                <parallel>methods</parallel>
                <threadCount>4</threadCount>

                <!-- Retry failed tests (with surefire rerunFailingTestsCount) -->
                <rerunFailingTestsCount>2</rerunFailingTestsCount>

                <!-- Skip tests (useful in some Maven profiles) -->
                <!-- <skip>true</skip> -->

                <!-- Include/exclude test classes -->
                <includes>
                    <include>**/*Test.java</include>
                </includes>
                <excludes>
                    <exclude>**/*ManualTest.java</exclude>
                </excludes>

            </configuration>
        </plugin>
    </plugins>
</build>
```

**Maven lifecycle:**
```
mvn compile → mvn test-compile → mvn test (Surefire runs here) → mvn package
```

---

## Q30. Where are TestNG reports generated?

TestNG generates reports in `test-output/` by default:

```
test-output/
├── index.html          ← Main HTML report (click to open)
├── emailable-report.html ← Single file for email
├── testng-results.xml  ← Machine-readable XML
├── junitreports/       ← JUnit XML format (for CI)
│   └── TEST-*.xml
└── old/                ← Previous run results
```

**Customise output directory in testng.xml:**
```xml
<suite name="Suite" output-dir="custom-reports/">
```

**Via Maven Surefire:** Reports go to `target/surefire-reports/` in JUnit XML format.

---

## Q31. What is the emailable report?

`emailable-report.html` is a **single self-contained HTML file** that contains the complete test results — passed, failed, skipped counts, method names, and exceptions. It can be emailed directly (no external CSS/JS dependencies).

**Location:** `test-output/emailable-report.html`

**In GitHub Actions — email/attach:**
```yaml
- name: Upload TestNG Report
  uses: actions/upload-artifact@v4
  with:
    name: testng-report
    path: test-output/emailable-report.html
```

**Custom emailable report with Extent Reports:**
```java
// pom.xml dependency
// <dependency>
//     <groupId>com.aventstack</groupId>
//     <artifactId>extentreports</artifactId>
//     <version>5.1.1</version>
// </dependency>

public class ExtentReportListener implements ITestListener, ISuiteListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ISuite suite) {
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/extent-report.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);
    }

    @Override
    public void onTestStart(ITestResult result) {
        test.set(extent.createTest(result.getName()));
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
    public void onFinish(ISuite suite) {
        extent.flush();
    }
}
```

---

## Q32. How to integrate Allure with TestNG?

**pom.xml dependencies:**
```xml
<dependencies>
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-testng</artifactId>
        <version>2.25.0</version>
    </dependency>
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-rest-assured</artifactId>
        <version>2.25.0</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version>
            <configuration>
                <argLine>
                    -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/1.9.21/aspectjweaver-1.9.21.jar"
                </argLine>
                <suiteXmlFiles>
                    <suiteXmlFile>testng.xml</suiteXmlFile>
                </suiteXmlFiles>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Test class with Allure annotations:**
```java
import io.qameta.allure.*;
import io.restassured.RestAssured;
import io.qameta.allure.restassured.AllureRestAssured;

@Epic("User Management")
@Feature("Login")
public class LoginAllureTest {

    @BeforeClass
    public void setup() {
        // Attach AllureRestAssured filter — logs all requests/responses to Allure
        RestAssured.filters(new AllureRestAssured());
    }

    @Test
    @Story("Login with valid credentials")
    @Description("Verify that a valid user can login and receives a 200 status")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("Kupeshanth")
    public void testValidLogin() {
        // Test steps shown in Allure report
        step1_navigateToLogin();
        step2_enterCredentials();
        step3_verifySuccess();
    }

    @Step("Navigate to login page")
    private void step1_navigateToLogin() {
        System.out.println("Step 1: Navigate");
    }

    @Step("Enter credentials: {username}")
    private void step2_enterCredentials() {
        System.out.println("Step 2: Enter creds");
    }

    @Step("Verify login success")
    private void step3_verifySuccess() {
        System.out.println("Step 3: Verify");
    }
}
```

**Generate and open Allure report:**
```bash
mvn clean test
allure serve target/allure-results
# OR
allure generate target/allure-results --clean -o target/allure-report
allure open target/allure-report
```

---

## Q33. How to run a specific test method from command line?

```bash
# Via Maven Surefire — specify class#method
mvn test -Dtest="LoginTest#testValidLogin"

# Multiple methods
mvn test -Dtest="LoginTest#testValidLogin+testInvalidLogin"

# All methods in a class
mvn test -Dtest="LoginTest"

# Wildcard
mvn test -Dtest="Login*"

# Via testng.xml method include
# (create a specific xml or use -Dmethod via system property)
```

**Using TestNG directly with a custom testng.xml:**
```xml
<suite name="SingleTest">
    <test name="SpecificMethod">
        <classes>
            <class name="tests.LoginTest">
                <methods>
                    <include name="testValidLogin"/>
                </methods>
            </class>
        </classes>
    </test>
</suite>
```
```bash
mvn test -DsuiteXmlFile=single-test.xml
```

---

## Q34. What is the difference between @BeforeTest and @BeforeClass?

| Annotation | Runs | Scope |
|---|---|---|
| `@BeforeTest` | Before the first `@Test` in a `<test>` tag | `<test>` tag in testng.xml |
| `@BeforeClass` | Before the first `@Test` in a class | The class |

```xml
<suite name="Suite">
    <test name="LoginSection">         <!-- @BeforeTest runs once here -->
        <classes>
            <class name="LoginTest"/>  <!-- @BeforeClass runs once per class -->
            <class name="LogoutTest"/> <!-- @BeforeClass runs once per class -->
        </classes>
    </test>
</suite>
```

```java
public class LoginTest {

    @BeforeTest
    public void testSectionSetup() {
        // Runs ONCE before LoginTest AND LogoutTest
        // Good for: initializing shared resources for the entire <test> block
        System.out.println("BeforeTest: common setup for LoginSection");
    }

    @BeforeClass
    public void classSetup() {
        // Runs once before LoginTest methods only
        System.out.println("BeforeClass: LoginTest specific setup");
    }

    @Test
    public void testLogin() { }
}
```

**Practical use:** `@BeforeTest` initializes a shared WebDriver pool for all classes in a `<test>` tag. `@BeforeClass` initializes class-specific resources.

---

## Q35. How does TestNG handle test dependencies when a dependency fails?

When a dependency fails, dependent tests are **SKIPPED** (not run) by default.

```java
@Test
public void testCreateUser() {
    Assert.fail("DB connection failed"); // FAILED
}

@Test(dependsOnMethods = {"testCreateUser"})
public void testVerifyUser() {
    // SKIPPED — because testCreateUser FAILED
}

@Test(dependsOnMethods = {"testCreateUser"})
public void testDeleteUser() {
    // ALSO SKIPPED
}
```

**Force run with alwaysRun = true:**
```java
// alwaysRun = true makes this run even if dependency failed
@Test(dependsOnMethods = {"testCreateUser"}, alwaysRun = true)
public void testCleanup() {
    // Runs even if testCreateUser FAILED — useful for cleanup
    System.out.println("Cleaning up regardless");
}
```

**Summary:**
- Dependency PASSED → dependent test RUNS
- Dependency FAILED → dependent test SKIPPED (unless `alwaysRun = true`)
- Dependency SKIPPED → dependent test also SKIPPED

---

## Q36. What is verbose attribute in testng.xml?

`verbose` controls how much output TestNG prints to the console during execution.

```xml
<suite name="Suite" verbose="2">
```

| Value | Output level |
|---|---|
| 0 | Silent — no output |
| 1 | Minimal (suite/test level only) |
| 2 | Default — test names, pass/fail/skip counts |
| 5 | Detailed — method invocations, parameters |
| 10 | Maximum — full debug output |

```xml
<!-- Development: verbose for debugging -->
<suite name="Suite" verbose="5">

<!-- CI: quiet output -->
<suite name="Suite" verbose="1">
```

---

## Q37. How to pass parameters from Maven command line to testng?

**Three-step flow: Maven CLI → System Property → testng.xml OR @Parameters**

```bash
# Step 1: Pass from command line
mvn test -Dbrowser=firefox -Denv=staging
```

```xml
<!-- Step 2: testng.xml reads system property (optional) -->
<suite name="Suite">
    <parameter name="browser" value="${browser}"/>
    <parameter name="env"     value="${env}"/>
    ...
</suite>
```

```java
// Step 3a: Via @Parameters
@Parameters("browser")
@BeforeClass
public void setup(String browser) {
    System.out.println("Browser: " + browser);
}

// Step 3b: OR read directly from System.getProperty
@BeforeClass
public void setup() {
    String browser = System.getProperty("browser", "chrome"); // default: chrome
    String env     = System.getProperty("env", "dev");
}
```

**Maven Surefire systemPropertyVariables:**
```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <browser>${browser}</browser>
            <env>${env}</env>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

---

## Q38. What is a TestNG listener and how to register it?

A **listener** is a class that implements a TestNG interface and receives callbacks during test execution. It lets you add cross-cutting behavior without modifying test code.

**Main listener interfaces:**

| Interface | Purpose |
|---|---|
| `ITestListener` | Test start, pass, fail, skip |
| `ISuiteListener` | Suite start and finish |
| `IAnnotationTransformer` | Modify annotations at runtime |
| `IRetryAnalyzer` | Retry failed tests |
| `IReporter` | Generate custom reports |
| `IInvokedMethodListener` | Before/after each invoked method |

**Three ways to register:**

```xml
<!-- 1. testng.xml — applies to entire suite -->
<listeners>
    <listener class-name="listeners.ScreenshotListener"/>
    <listener class-name="listeners.ExtentReportListener"/>
</listeners>
```

```java
// 2. @Listeners annotation on class — applies to this class only
@Listeners({ScreenshotListener.class, ExtentReportListener.class})
public class LoginTest { ... }
```

```java
// 3. ServiceLoader — auto-discovery (add META-INF/services file)
// File: src/main/resources/META-INF/services/org.testng.ITestListener
// Content: listeners.ScreenshotListener
```

**Best practice:** Register in testng.xml so all tests benefit without modifying classes.

---

## Q39. How to generate HTML report with TestNG + Surefire?

**TestNG built-in HTML report:**
Run any TestNG test → `test-output/index.html` is auto-generated.

**Surefire HTML report with maven-surefire-report-plugin:**
```xml
<reporting>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-report-plugin</artifactId>
            <version>3.1.2</version>
        </plugin>
    </plugins>
</reporting>
```

```bash
mvn surefire-report:report   # generates target/site/surefire-report.html
mvn site                      # full site with reports
```

**Allure report (recommended):**
```bash
mvn clean test
allure serve target/allure-results    # opens in browser immediately
```

**GitHub Actions — upload HTML report:**
```yaml
- name: Run Tests
  run: mvn test

- name: Upload Report
  if: always()   # upload even if tests fail
  uses: actions/upload-artifact@v4
  with:
    name: testng-html-report
    path: |
      test-output/
      target/surefire-reports/
    retention-days: 7
```

---

## Q40. Design a complete testng.xml for cross-browser parallel testing.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">

<!--
    Cross-browser parallel suite:
    - 3 browsers run simultaneously (parallel="tests", thread-count="3")
    - Each browser runs test methods in parallel (parallel="methods", thread-count="2")
    - Listeners handle screenshots, retry, and reporting
-->
<suite name="CrossBrowserRegressionSuite"
       parallel="tests"
       thread-count="3"
       verbose="2"
       time-out="60000">

    <!-- Global listeners -->
    <listeners>
        <listener class-name="listeners.ScreenshotListener"/>
        <listener class-name="listeners.ExtentReportListener"/>
        <listener class-name="listeners.GlobalRetryTransformer"/>
    </listeners>

    <!-- Global parameters -->
    <parameter name="env"     value="staging"/>
    <parameter name="baseUrl" value="https://staging.example.com"/>

    <!-- ==================== CHROME ==================== -->
    <test name="Chrome_Tests" parallel="methods" thread-count="2">
        <parameter name="browser"  value="chrome"/>
        <parameter name="headless" value="false"/>

        <groups>
            <run>
                <include name="smoke"/>
                <include name="regression"/>
                <exclude name="wip"/>
            </run>
        </groups>

        <classes>
            <class name="tests.auth.LoginTest"/>
            <class name="tests.auth.LogoutTest"/>
            <class name="tests.product.SearchTest"/>
            <class name="tests.product.FilterTest"/>
            <class name="tests.cart.CartTest"/>
            <class name="tests.checkout.CheckoutTest"/>
        </classes>
    </test>

    <!-- ==================== FIREFOX ==================== -->
    <test name="Firefox_Tests" parallel="methods" thread-count="2">
        <parameter name="browser"  value="firefox"/>
        <parameter name="headless" value="false"/>

        <groups>
            <run>
                <include name="smoke"/>
                <include name="regression"/>
                <exclude name="wip"/>
            </run>
        </groups>

        <classes>
            <class name="tests.auth.LoginTest"/>
            <class name="tests.auth.LogoutTest"/>
            <class name="tests.product.SearchTest"/>
            <class name="tests.product.FilterTest"/>
            <class name="tests.cart.CartTest"/>
            <class name="tests.checkout.CheckoutTest"/>
        </classes>
    </test>

    <!-- ==================== EDGE ==================== -->
    <test name="Edge_Tests" parallel="methods" thread-count="2">
        <parameter name="browser"  value="edge"/>
        <parameter name="headless" value="true"/>

        <groups>
            <run>
                <!-- Edge: smoke only for speed -->
                <include name="smoke"/>
                <exclude name="wip"/>
            </run>
        </groups>

        <classes>
            <class name="tests.auth.LoginTest"/>
            <class name="tests.product.SearchTest"/>
        </classes>
    </test>

</suite>
```

**Supporting base class using @Parameters + ThreadLocal:**
```java
public class BaseTest {

    protected static ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();

    @Parameters({"browser", "headless", "baseUrl"})
    @BeforeMethod(alwaysRun = true)
    public void setup(String browser,
                      @Optional("false") String headless,
                      @Optional("https://example.com") String baseUrl) {

        WebDriver driver;
        boolean isHeadless = Boolean.parseBoolean(headless);

        switch (browser.toLowerCase()) {
            case "firefox":
                FirefoxOptions ffOpts = new FirefoxOptions();
                if (isHeadless) ffOpts.addArguments("--headless");
                driver = new FirefoxDriver(ffOpts);
                break;
            case "edge":
                EdgeOptions edgeOpts = new EdgeOptions();
                if (isHeadless) edgeOpts.addArguments("--headless");
                driver = new EdgeDriver(edgeOpts);
                break;
            default: // chrome
                ChromeOptions chromeOpts = new ChromeOptions();
                if (isHeadless) chromeOpts.addArguments("--headless=new");
                driver = new ChromeDriver(chromeOpts);
        }

        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get(baseUrl);
        driverThread.set(driver);
    }

    public static WebDriver getDriver() {
        return driverThread.get();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
        WebDriver driver = driverThread.get();
        if (driver != null) {
            driver.quit();
            driverThread.remove();
        }
    }
}
```

**GitHub Actions CI:**
```yaml
name: Cross-Browser Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - name: Run Cross-Browser Suite
        run: mvn test -DsuiteXmlFile=testng-crossbrowser.xml
      - name: Upload Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: test-output/
```

---

*End of Part 5 — TestNG (40/40 questions)*
