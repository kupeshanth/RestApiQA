# TestNG — Complete Guide
## Annotations + DataProvider + Groups + Listeners + Parallel + Reporting

---

## TABLE OF CONTENTS
1. [What is TestNG](#1-what-is-testng)
2. [Setup](#2-setup)
3. [Annotations — Full Reference](#3-annotations--full-reference)
4. [testng.xml — Full Reference](#4-testngxml--full-reference)
5. [Assertions](#5-assertions)
6. [DataProvider](#6-dataprovider)
7. [Groups](#7-groups)
8. [Parameters](#8-parameters)
9. [Listeners](#9-listeners)
10. [Parallel Execution](#10-parallel-execution)
11. [Retry Analyzer](#11-retry-analyzer)
12. [Reporting](#12-reporting)
13. [Interview Q&A](#13-interview-qa)

---

## 1. What is TestNG

TestNG (Test Next Generation) is a Java testing framework. It:
- Runs `@Test` methods and reports pass/fail
- Organises tests with `@BeforeClass`, `@AfterMethod` etc.
- Runs tests in parallel across threads/browsers
- Feeds test data via `@DataProvider`
- Groups tests for selective runs (smoke, regression)
- Generates HTML/XML reports

---

## 2. Setup

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.8.0</version>
    <scope>test</scope>
</dependency>

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

---

## 3. Annotations — Full Reference

```java
// ── EXECUTION ORDER ───────────────────────────────────────────────────────────

@BeforeSuite                  // runs once before all tests in the suite
public void globalSetup() { }

@BeforeTest                   // runs once before each <test> tag in testng.xml
public void testSetup() { }

@BeforeClass                  // runs once before first @Test in this class
public void classSetup() { }

@BeforeMethod                 // runs before EVERY @Test method
public void methodSetup() { }

@Test                         // the actual test
public void myTest() { }

@AfterMethod                  // runs after EVERY @Test method
public void methodTeardown() { }

@AfterClass                   // runs once after last @Test in this class
public void classTeardown() { }

@AfterTest                    // runs once after each <test> tag
public void testTeardown() { }

@AfterSuite                   // runs once after all tests
public void globalTeardown() { }

// ── @Test ATTRIBUTES ─────────────────────────────────────────────────────────

@Test(description = "Verify login with valid credentials")
@Test(groups = { "smoke", "regression" })
@Test(enabled = false)                              // skip this test
@Test(priority = 1)                                 // run order (lower = first)
@Test(timeOut = 5000)                               // fail if takes > 5 seconds
@Test(invocationCount = 3)                          // run this test 3 times
@Test(expectedExceptions = ArithmeticException.class)  // expect this exception
@Test(dataProvider = "loginData")                   // feed data from DataProvider
@Test(dependsOnMethods = { "loginTest" })           // run only if loginTest passed
@Test(alwaysRun = true)                             // run even if dependency failed
```

---

## 4. testng.xml — Full Reference

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">

<suite name="Full Test Suite" verbose="1" parallel="tests" thread-count="3">

    <!-- Listeners apply to entire suite -->
    <listeners>
        <listener class-name="listeners.TestListener"/>
        <listener class-name="listeners.RetryListener"/>
    </listeners>

    <!-- Smoke test group — run first -->
    <test name="Smoke Tests">
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

    <!-- Regression — runs all tests except slow -->
    <test name="Regression">
        <groups>
            <run>
                <include name="regression"/>
                <exclude name="slow"/>
            </run>
        </groups>
        <classes>
            <class name="tests.LoginTest"/>
            <class name="tests.CheckoutTest"/>
        </classes>
    </test>

    <!-- Browser parameter example -->
    <test name="Chrome Tests">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="tests.LoginTest"/>
        </classes>
    </test>

    <!-- Run only specific methods -->
    <test name="Specific Methods">
        <classes>
            <class name="tests.LoginTest">
                <methods>
                    <include name="validLogin"/>
                    <include name="invalidPassword"/>
                </methods>
            </class>
        </classes>
    </test>

</suite>
```

---

## 5. Assertions

```java
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

// ── HARD ASSERTIONS (stop on first failure) ───────────────────────────────────
Assert.assertEquals(actual, expected);
Assert.assertEquals(actual, expected, "failure message");
Assert.assertNotEquals(actual, expected);
Assert.assertTrue(condition);
Assert.assertTrue(condition, "message if fails");
Assert.assertFalse(condition);
Assert.assertNull(object);
Assert.assertNotNull(object);
Assert.assertSame(obj1, obj2);          // same object reference
Assert.assertNotSame(obj1, obj2);

// ── SOFT ASSERTIONS (collect all failures, report at end) ─────────────────────
SoftAssert soft = new SoftAssert();
soft.assertEquals(response.getStatusCode(), 200);
soft.assertNotNull(response.getBody());
soft.assertTrue(pageTitle.contains("Dashboard"));
soft.assertEquals(userName, "Admin");
soft.assertAll();   // ← MANDATORY. Reports ALL failures. Never forget this.
```

---

## 6. DataProvider

```java
// ── SINGLE VALUE DATA PROVIDER ────────────────────────────────────────────────
@DataProvider(name = "validUserIds")
public Object[][] validIds() {
    return new Object[][] {
        { 1 }, { 2 }, { 5 }, { 100 }
    };
}

@Test(dataProvider = "validUserIds")
public void getUser(int userId) {
    // runs 4 times — once per row
    Assert.assertTrue(userId > 0);
}

// ── MULTI-COLUMN DATA PROVIDER ────────────────────────────────────────────────
@DataProvider(name = "loginScenarios")
public Object[][] loginScenarios() {
    return new Object[][] {
        // username,   password,      expectedResult,  expectedUrl
        { "admin",   "Admin@123",   "Dashboard",     "/dashboard" },
        { "user1",   "User@123",    "Home",          "/home"      },
        { "admin",   "wrong",       "Error",         "/login"     },
        { "",        "Admin@123",   "Error",         "/login"     },
    };
}

@Test(dataProvider = "loginScenarios")
public void loginTest(String user, String pass, String expected, String url) {
    loginPage.login(user, pass);
    Assert.assertTrue(driver.getPageSource().contains(expected));
}

// ── EXTERNAL DATA PROVIDER (from CSV or Excel) ────────────────────────────────
@DataProvider(name = "csvData")
public Object[][] csvData() throws Exception {
    List<Object[]> rows = new ArrayList<>();
    BufferedReader br = new BufferedReader(new FileReader("src/test/resources/data.csv"));
    String line;
    while ((line = br.readLine()) != null) {
        String[] values = line.split(",");
        rows.add(new Object[]{ values[0], values[1], values[2] });
    }
    return rows.toArray(new Object[0][]);
}

// ── PARALLEL DATA PROVIDER ────────────────────────────────────────────────────
@DataProvider(name = "parallelData", parallel = true)
public Object[][] parallelData() {
    return new Object[][] {{ 1 }, { 2 }, { 3 }, { 4 }};
}
```

---

## 7. Groups

```java
// Tag tests with groups
@Test(groups = { "smoke" })
public void criticalLoginTest() { }

@Test(groups = { "smoke", "regression" })
public void loginWithValidCredentials() { }

@Test(groups = { "regression" })
public void loginWithInvalidPassword() { }

@Test(groups = { "regression", "slow" })
public void complexCheckoutFlow() { }
```

```bash
# Run by group via Maven
mvn test -Dgroups=smoke
mvn test -Dgroups="smoke,regression"
mvn test -DexcludedGroups=slow
```

---

## 8. Parameters

```java
// Read parameter value from testng.xml
@BeforeMethod
@Parameters({ "browser", "baseUrl" })
public void setUp(String browser, @Optional("https://default.com") String baseUrl) {
    // browser comes from testng.xml <parameter>
    // @Optional sets a default if not provided
    initDriver(browser);
    driver.get(baseUrl);
}
```

```xml
<!-- testng.xml -->
<test name="Chrome Production">
    <parameter name="browser" value="chrome"/>
    <parameter name="baseUrl" value="https://production.example.com"/>
    <classes><class name="tests.LoginTest"/></classes>
</test>
```

---

## 9. Listeners

```java
// ── ITestListener — react to test events ──────────────────────────────────────
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("STARTING: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("PASSED: " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("FAILED: " + result.getName());
        // Take screenshot on failure
        WebDriver driver = ((BaseTest) result.getInstance()).getDriver();
        File shot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(shot, new File("screenshots/FAIL_" + result.getName() + ".png"));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("SKIPPED: " + result.getName());
    }
}

// ── ISuiteListener — suite-level events ───────────────────────────────────────
public class SuiteListener implements ISuiteListener {
    @Override
    public void onStart(ISuite suite) {
        System.out.println("Suite starting: " + suite.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        System.out.println("Suite finished: " + suite.getName());
    }
}
```

---

## 10. Parallel Execution

```xml
<!-- testng.xml parallel options -->
<suite parallel="methods"  thread-count="4">  <!-- each @Test on separate thread -->
<suite parallel="classes"  thread-count="3">  <!-- each class on separate thread -->
<suite parallel="tests"    thread-count="2">  <!-- each <test> tag on separate thread -->
<suite parallel="instances" thread-count="2"> <!-- each class instance on separate thread -->
```

```java
// BaseTest — MUST use ThreadLocal for parallel safety
public class BaseTest {
    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public WebDriver getDriver() { return tlDriver.get(); }

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        tlDriver.set(new ChromeDriver());
    }

    @AfterMethod
    public void tearDown() {
        getDriver().quit();
        tlDriver.remove();   // prevent memory leak
    }
}
```

---

## 11. Retry Analyzer

```java
// RetryAnalyzer.java — retry failed tests automatically
public class RetryAnalyzer implements IRetryAnalyzer {
    private int count = 0;
    private static final int MAX = 2;   // retry up to 2 times

    @Override
    public boolean retry(ITestResult result) {
        if (count < MAX) {
            count++;
            System.out.println("Retrying: " + result.getName() + " (attempt " + count + ")");
            return true;
        }
        return false;
    }
}

// Apply to specific test
@Test(retryAnalyzer = RetryAnalyzer.class)
public void flakyNetworkTest() { }

// Apply to ALL tests via Listener
public class RetryListener implements IAnnotationTransformer {
    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
// Register RetryListener in testng.xml listeners section
```

---

## 12. Reporting

```bash
# Default report locations after mvn test:
target/surefire-reports/index.html        # Surefire HTML report
target/surefire-reports/TEST-*.xml        # JUnit XML (for CI parsing)

# Open the HTML report:
# Windows: start target/surefire-reports/index.html
# Mac:     open target/surefire-reports/index.html
```

---

## 13. Interview Q&A

**Q: What is the difference between `@BeforeClass` and `@BeforeMethod`?**
```
@BeforeClass → runs ONCE before the first @Test in the class (driver init)
@BeforeMethod → runs before EVERY @Test (navigate to page, reset state)
Use @BeforeClass for expensive setup (browser launch).
Use @BeforeMethod for per-test setup (go to login page).
```

**Q: What happens if a `@BeforeMethod` fails?**
```
The @Test method is SKIPPED (not failed). TestNG marks it as skipped.
The @AfterMethod still runs (to clean up).
```

**Q: What is `dependsOnMethods` and when is it dangerous?**
```java
@Test(dependsOnMethods = {"loginTest"})
public void dashboardTest() { }
// dashboardTest only runs if loginTest passed.
// DANGER: tests become dependent on each other.
// Best practice: each test should be independent.
// Only use when the tests genuinely represent a sequence.
```

**Q: How do you skip a test conditionally?**
```java
@Test
public void conditionalTest() {
    if (System.getenv("SKIP_THIS") != null) {
        throw new SkipException("Skipping because env var is set");
    }
    // ... test logic
}
```
