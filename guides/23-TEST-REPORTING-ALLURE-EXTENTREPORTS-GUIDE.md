# Test Reporting — Complete Guide | Allure + ExtentReports + Playwright HTML

> Senior QA Interview Preparation — Test Reporting Mastery

---

## SECTION 1 — Why Test Reporting Matters

### The Core Problem

Automated tests run hundreds of assertions across many files. The raw output — stack traces, console logs, JUnit XML — is meaningful to the developer who wrote the tests, but completely opaque to everyone else who needs to make decisions based on it.

**Stakeholders who read reports (not code):**
- Engineering Manager — is the release safe to ship?
- Product Owner — which features are passing, which are blocked?
- Dev Lead — which tests are failing, is it my code or the test environment?
- Release Manager — is the build green? Can we promote to staging?

### What a Good Report Answers

| Question | Bad Report (console output) | Good Report (Allure/HTML) |
|----------|----------------------------|--------------------------|
| What passed? | Scroll through 10,000 lines | Green bar with count at a glance |
| What failed? | Find the red text in the noise | Grouped failure list with error message |
| Why did it fail? | Stack trace only | Screenshot + API request/response + step log |
| How long did it take? | No timing | Duration per test and per suite |
| Is it getting better? | No context | Trend graph over last 10 builds |
| Is this a new failure? | No way to know | Flaky test flag vs new failure flag |

### The Reporting Hierarchy

```
Executive Summary   ← Pass rate, total count, duration, trend
    ↓
Test Suite Level    ← Which feature/epic is failing?
    ↓
Individual Test     ← Which specific test case failed?
    ↓
Step Level          ← At which step did it fail?
    ↓
Evidence            ← Screenshot + logs + API request at point of failure
```

Good reporting means stakeholders at every level can answer their question without needing to understand automation code.

---

## SECTION 2 — Allure Report — Complete Setup for Java + TestNG

### Maven Dependencies

Add to `pom.xml`:

```xml
<properties>
    <allure.version>2.24.0</allure.version>
    <aspectj.version>1.9.20.1</aspectj.version>
</properties>

<dependencies>
    <!-- Allure TestNG adapter -->
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-testng</artifactId>
        <version>${allure.version}</version>
        <scope>test</scope>
    </dependency>

    <!-- Allure RestAssured filter (auto-logs requests/responses) -->
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-rest-assured</artifactId>
        <version>${allure.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version>
            <configuration>
                <!-- Required for Allure to capture step data via AspectJ -->
                <argLine>
                    -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar"
                </argLine>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Allure Annotations — Full Reference

#### Hierarchy Annotations (organise tests in the report)

```java
@Epic("Order Management")       // Highest level — maps to a business epic
@Feature("Order Creation")      // Mid level — maps to a feature
@Story("Create order via API")  // Lowest level — maps to a user story or test case
```

These create a tree in the Allure report:
```
Epic: Order Management
  └─ Feature: Order Creation
       └─ Story: Create order via API
            └─ [individual test methods]
```

#### @Description — describe what the test does

```java
@Description("Verify that POST /api/orders returns 201 and persists the order in the database")
```

Shown in the report alongside the test name. Write it like a test case description.

#### @Step — mark methods as named steps

```java
@Step("Send POST request to create order with payload {orderRequest}")
public Response createOrder(OrderRequest orderRequest) {
    return RestAssured.given()
        .contentType(ContentType.JSON)
        .body(orderRequest)
        .post("/api/orders");
}

@Step("Verify response status code is {expectedStatus}")
public void verifyStatusCode(Response response, int expectedStatus) {
    assertEquals(response.getStatusCode(), expectedStatus,
        "Expected status " + expectedStatus + " but got " + response.getStatusCode());
}
```

In the Allure report, each `@Step` method appears as a numbered step with pass/fail status and the actual parameter value substituted into the name (e.g. `Send POST request to create order with payload OrderRequest{customerId=42, amount=99.99}`).

#### @Attachment — attach files to the report

```java
// Attach a screenshot (bytes)
@Attachment(value = "Screenshot on failure", type = "image/png", fileExtension = "png")
public byte[] captureScreenshot(WebDriver driver) {
    return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
}

// Attach a text log
@Attachment(value = "Application log", type = "text/plain", fileExtension = "txt")
public String attachLog(String logContent) {
    return logContent;
}

// Attach raw JSON (e.g. API response body)
@Attachment(value = "API Response Body", type = "application/json", fileExtension = "json")
public String attachResponseBody(String responseBody) {
    return responseBody;
}
```

#### @Severity — classify test importance

```java
@Severity(SeverityLevel.BLOCKER)   // Blocks the release if failing
@Severity(SeverityLevel.CRITICAL)  // Core business functionality
@Severity(SeverityLevel.NORMAL)    // Standard importance (default)
@Severity(SeverityLevel.MINOR)     // Minor cosmetic or edge case
@Severity(SeverityLevel.TRIVIAL)   // Low-value, can be deferred
```

Allure dashboard shows breakdown by severity — helps prioritise which failures to fix first.

#### @Link, @Issue, @TmsLink — link to external systems

```java
@Link(name = "API Docs", url = "https://docs.example.com/orders-api")
@Issue("OMS-1234")             // Links to JIRA issue (configure JIRA URL in allure.properties)
@TmsLink("XRAY-567")           // Links to Xray/Zephyr test case
```

### Complete Example Test Class with All Annotations

```java
package com.example.tests;

import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

@Epic("Order Management")
@Feature("Order Creation")
public class CreateOrderTest {

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = "https://api.example.com";
        // One line: auto-logs all requests & responses to Allure
        RestAssured.filters(new AllureRestAssured());
    }

    @Test
    @Story("Create order with valid payload returns 201")
    @Description("Send a well-formed POST /api/orders request and verify the API creates the order, returns HTTP 201, and the response body contains the generated order ID.")
    @Severity(SeverityLevel.BLOCKER)
    @Issue("OMS-100")
    @TmsLink("XRAY-200")
    public void createOrder_withValidPayload_returns201() {
        // Step 1: Build payload
        String payload = buildOrderPayload("CUST-42", 99.99, "STANDARD");

        // Step 2: Send request
        Response response = sendCreateOrderRequest(payload);

        // Step 3: Verify status
        verifyStatusCode(response, 201);

        // Step 4: Verify response body
        String orderId = verifyResponseContainsOrderId(response);

        // Step 5: Verify order persisted in DB
        verifyOrderInDatabase(orderId);
    }

    @Test
    @Story("Create order with missing customer ID returns 400")
    @Description("Send POST /api/orders with no customerId field and verify the API rejects it with HTTP 400 and a meaningful error message.")
    @Severity(SeverityLevel.CRITICAL)
    public void createOrder_withMissingCustomerId_returns400() {
        String payload = "{ \"amount\": 99.99, \"type\": \"STANDARD\" }"; // no customerId

        Response response = sendCreateOrderRequest(payload);

        verifyStatusCode(response, 400);
        verifyErrorMessage(response, "customerId is required");
    }

    // ── Step methods ──────────────────────────────────────────────

    @Step("Build order payload: customerId={customerId}, amount={amount}, type={type}")
    private String buildOrderPayload(String customerId, double amount, String type) {
        return String.format(
            "{ \"customerId\": \"%s\", \"amount\": %.2f, \"type\": \"%s\" }",
            customerId, amount, type
        );
    }

    @Step("Send POST /api/orders")
    private Response sendCreateOrderRequest(String payload) {
        return RestAssured.given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + getTestToken())
            .body(payload)
            .post("/api/orders");
    }

    @Step("Verify HTTP status code is {expectedStatus}")
    private void verifyStatusCode(Response response, int expectedStatus) {
        assertEquals(response.getStatusCode(), expectedStatus,
            "Status code mismatch. Response body: " + response.getBody().asString());
    }

    @Step("Verify response body contains orderId")
    private String verifyResponseContainsOrderId(Response response) {
        String orderId = response.jsonPath().getString("orderId");
        assertNotNull(orderId, "orderId should not be null in response");
        assertTrue(orderId.startsWith("ORD-"), "orderId should start with ORD-");
        return orderId;
    }

    @Step("Verify order {orderId} exists in database")
    private void verifyOrderInDatabase(String orderId) {
        // DB verification logic here (JDBC or service call)
        Allure.addAttachment("DB verification", "Order " + orderId + " found in database");
    }

    @Step("Verify error message contains: {expectedMessage}")
    private void verifyErrorMessage(Response response, String expectedMessage) {
        String actualMessage = response.jsonPath().getString("message");
        assertTrue(actualMessage.contains(expectedMessage),
            "Expected error message '" + expectedMessage + "' but got: " + actualMessage);
    }

    private String getTestToken() {
        return System.getenv("TEST_AUTH_TOKEN");
    }
}
```

### Adding RestAssured Logs to Allure (One Line)

```java
// In your BaseTest @BeforeClass or @BeforeSuite:
RestAssured.filters(new AllureRestAssured());
```

This single line makes every RestAssured request and response automatically appear in the Allure report as an attachment — the full request URL, headers, body, response status, response headers, and response body. No manual attachment code needed.

### Auto-Screenshot on Failure — TestNG Listener

```java
package com.example.listeners;

import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ScreenshotListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        // Get WebDriver from the test instance (assumes your test class has a getDriver() method)
        Object testInstance = result.getInstance();
        if (testInstance instanceof BaseTest) {
            WebDriver driver = ((BaseTest) testInstance).getDriver();
            if (driver != null) {
                takeScreenshot(driver);
            }
        }
    }

    @Attachment(value = "Screenshot on failure", type = "image/png", fileExtension = "png")
    public byte[] takeScreenshot(WebDriver driver) {
        // This byte[] is automatically attached to the Allure report
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }
}
```

Register the listener in `testng.xml`:
```xml
<suite name="Test Suite">
    <listeners>
        <listener class-name="com.example.listeners.ScreenshotListener"/>
    </listeners>
    <test name="API Tests">
        <classes>
            <class name="com.example.tests.CreateOrderTest"/>
        </classes>
    </test>
</suite>
```

### Generating and Serving the Allure Report

```bash
# Step 1: Run the tests (generates allure-results/ directory with JSON data files)
mvn test

# Step 2a: Serve an interactive report in your browser (best for local development)
allure serve allure-results/
# Opens browser automatically at http://localhost:PORT/

# Step 2b: Generate a static HTML report (best for CI artifacts or sharing)
allure generate allure-results/ --clean -o allure-report/
# Creates allure-report/index.html

# Step 3: Open the generated static report
allure open allure-report/
```

### Allure in Jenkins

1. Install the **Allure Jenkins Plugin** from Jenkins plugin manager
2. In your Jenkins pipeline (`Jenkinsfile`):

```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
    }
    post {
        always {
            // Generate Allure report from results
            allure([
                includeProperties: false,
                jdk: '',
                results: [[path: 'allure-results']]
            ])
        }
    }
}
```

3. After the build, the Allure Report appears as a **sidebar link** in Jenkins ("Allure Report")
4. Allure Jenkins plugin automatically tracks **history** — you can see pass rate trends over time

---

## SECTION 3 — ExtentReports — Complete Setup

### Maven Dependency

```xml
<dependency>
    <groupId>com.aventstack</groupId>
    <artifactId>extentreports</artifactId>
    <version>5.1.1</version>
    <scope>test</scope>
</dependency>
```

### ExtentReports with TestNG Listener — Complete Working Code

#### ExtentManager.java — singleton for thread-safe usage
```java
package com.example.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentManager {

    private static ExtentReports extent;

    public static ExtentReports getInstance() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("Test Execution Report");
            spark.config().setReportName("API Automation Suite");
            spark.config().setTimeStampFormat("dd-MMM-yyyy HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Environment", System.getProperty("env", "staging"));
            extent.setSystemInfo("Author", "QA Team");
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        }
        return extent;
    }
}
```

#### ExtentTestListener.java — complete TestNG listener
```java
package com.example.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.example.reporting.ExtentManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Base64;

public class ExtentTestListener implements ITestListener {

    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static final ExtentReports extent = ExtentManager.getInstance();

    @Override
    public void onTestStart(ITestResult result) {
        // Create a new ExtentTest entry for this test method
        ExtentTest test = extent.createTest(
            result.getMethod().getMethodName(),
            result.getMethod().getDescription()
        );
        extentTest.set(test);
        test.log(Status.INFO, "Test started: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        extentTest.get().log(Status.PASS, "Test PASSED");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = extentTest.get();
        test.log(Status.FAIL, "Test FAILED");
        test.log(Status.FAIL, result.getThrowable()); // log the exception

        // Attach screenshot if WebDriver is available
        Object instance = result.getInstance();
        if (instance instanceof BaseTest) {
            WebDriver driver = ((BaseTest) instance).getDriver();
            if (driver != null) {
                String base64Screenshot = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BASE64);
                test.fail("Screenshot at point of failure:",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        extentTest.get().log(Status.SKIP, "Test SKIPPED");
        if (result.getThrowable() != null) {
            extentTest.get().log(Status.SKIP, result.getThrowable());
        }
    }

    // Log an individual step from your test code:
    // ExtentTestListener.logStep(Status.PASS, "Verified response status 201");
    public static void logStep(Status status, String message) {
        if (extentTest.get() != null) {
            extentTest.get().log(status, message);
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        // Flush writes the report to disk — MUST call this
        extent.flush();
    }
}
```

#### Using logStep from your test
```java
@Test
public void createOrder_returnsCorrectStatus() {
    ExtentTestListener.logStep(Status.INFO, "Sending POST request to /api/orders");
    Response response = given().body(payload).post("/api/orders");

    ExtentTestListener.logStep(Status.INFO, "Verifying response status code");
    assertEquals(response.getStatusCode(), 201);
    ExtentTestListener.logStep(Status.PASS, "Status code verified: 201");

    String orderId = response.jsonPath().getString("orderId");
    assertNotNull(orderId);
    ExtentTestListener.logStep(Status.PASS, "orderId present in response: " + orderId);
}
```

#### Report Output
- HTML report generated at: `test-output/ExtentReport.html`
- Open in any browser — no server needed (self-contained HTML with embedded charts)
- Contains: test list with status icons, timeline view, pie chart of pass/fail/skip, detailed step log per test

---

## SECTION 4 — Playwright HTML Report

### Built-In — No Dependencies Required

Playwright's HTML reporter is included with `@playwright/test`. No extra packages to install.

### playwright.config.ts — reporter configuration

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30000,
  retries: 1,

  reporter: [
    // Primary reporter: rich HTML with trace, screenshots, video
    ['html', {
      outputFolder: 'playwright-report',   // where the report is generated
      open: 'on-failure'                   // 'always' | 'on-failure' | 'never'
    }],

    // Secondary: line-by-line output in terminal while tests run
    ['list'],

    // For CI: JUnit XML so tools like Jenkins can parse results
    ['junit', { outputFile: 'results/junit.xml' }],
  ],

  use: {
    screenshot: 'only-on-failure',  // capture screenshot on failure
    video: 'retain-on-failure',     // record video; keep only on failure
    trace: 'on-first-retry',        // full trace on first retry
  },
});
```

### What the Playwright HTML Report Shows

| Section | Detail |
|---------|--------|
| Test list | Each test with PASSED / FAILED / SKIPPED status |
| Duration | Exact millisecond timing per test |
| Error message | The assertion error with expected vs received values |
| Stack trace | Line number in test file where failure occurred |
| Screenshot | Full-page screenshot captured at point of failure |
| Video | Video recording of the full test execution (replay button) |
| Network requests | Every fetch/XHR request made during the test (URL, status, timing) |
| Trace viewer | Step-by-step DOM snapshots — see the page at every action |

### Commands

```bash
# Run tests and generate the report
npx playwright test

# Open the report in your browser (after tests have run)
npx playwright show-report

# Explicitly specify HTML reporter (if not in config)
npx playwright test --reporter=html

# Output JUnit XML for CI pipelines
npx playwright test --reporter=junit

# Run specific test file and open report immediately
npx playwright test tests/orders.spec.ts --reporter=html
npx playwright show-report
```

### Trace Viewer (Powerful for Debugging)

The trace viewer lets you step through every action in the test like a time machine:

```typescript
// playwright.config.ts — capture trace on every run
use: {
  trace: 'on',  // options: 'on' | 'off' | 'retain-on-failure' | 'on-first-retry'
}
```

```bash
# Open a specific trace file
npx playwright show-trace path/to/trace.zip
```

The trace shows:
- Timeline bar with every action
- DOM snapshot before and after each action
- Console messages at that moment
- Network requests at that moment

---

## SECTION 5 — Allure Report for Playwright (TypeScript)

### Installation

```bash
npm install --save-dev allure-playwright allure-js-commons
```

### playwright.config.ts

```typescript
import { defineConfig } from '@playwright/test';

export default defineConfig({
  reporter: [
    ['allure-playwright', {
      detail: true,           // include step details
      outputFolder: 'allure-results',
      suiteTitle: false,
    }],
    ['list'],
  ],

  use: {
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    trace: 'on-first-retry',
  },
});
```

### Using Allure Annotations in Playwright Tests

```typescript
import { test, expect } from '@playwright/test';
import { allure } from 'allure-playwright';

test.describe('Order API', () => {

    test('create order returns 201', async ({ request }) => {
        allure.epic('Order Management');
        allure.feature('Order Creation');
        allure.story('POST /api/orders with valid payload');
        allure.severity('critical');
        allure.description('Verify POST /api/orders returns 201 and a valid orderId');

        await allure.step('Send POST request to /api/orders', async () => {
            const response = await request.post('/api/orders', {
                data: { customerId: 'CUST-42', amount: 99.99, type: 'STANDARD' }
            });

            await allure.step('Verify status 201', async () => {
                expect(response.status()).toBe(201);
            });

            await allure.step('Verify orderId in response', async () => {
                const body = await response.json();
                expect(body.orderId).toBeTruthy();
                expect(body.orderId).toMatch(/^ORD-/);
            });
        });
    });
});
```

### Generate and Serve the Allure Report

```bash
# Run tests (generates allure-results/ directory)
npx playwright test

# Serve interactive report (opens in browser)
npx allure serve allure-results/

# Generate static HTML report
npx allure generate allure-results/ -o allure-report/ --clean

# Open the generated static report
npx allure open allure-report/
```

---

## SECTION 6 — What Makes a Good Test Report

### Executive Summary
The first thing visible in the report should be:
- Total tests: 350
- Passed: 312 (89.1%)
- Failed: 24 (6.9%)
- Skipped: 14 (4.0%)
- Total duration: 8m 32s
- Trend arrow: ↑ (up from 85% last run)

A manager should be able to assess the health of the release in 5 seconds.

### Categorised Failures
Group failures into:
- **New failures** — passed on the previous run, failing now (regression, highest priority)
- **Known failures** — have been failing for multiple runs (backlogged, being investigated)
- **Flaky tests** — pass and fail intermittently (environmental or race condition, medium priority)
- **Infrastructure failures** — test environment was down, not a product bug

### Attachments
Every failure should have:
- Screenshot of the UI or API response at point of failure
- Full API request and response (URL, headers, body, status)
- Application log excerpt
- Test data used (the specific inputs that caused the failure)

### Drill-Down Structure
```
Suite level → Feature level → Test level → Step level → Evidence
```
A stakeholder should never have to read code to understand what failed.

### History / Trends
- Pass rate over the last 10 builds (is it improving or degrading?)
- Which tests are consistently flaky (appearing red and green alternately)?
- Duration trend (are tests getting slower sprint over sprint?)

Allure and Jenkins Allure Plugin provide this automatically if you keep history between builds.

### Tags and Labels
Use labels (Allure severity, feature, story; Playwright tags) so the report can be filtered:
- "Show me only BLOCKER failures"
- "Show me only failures in the Checkout feature"
- "Show me all tests tagged @smoke"

---

## SECTION 7 — All Commands Reference

### Allure (Java + Maven)
```bash
# Run tests and generate allure-results/ (JSON data files)
mvn test

# Serve interactive report in browser (auto-opens)
allure serve allure-results/

# Generate static HTML report (for CI artifacts)
allure generate allure-results/ -o allure-report/ --clean

# Open previously generated static report
allure open allure-report/

# Install Allure CLI (if not installed)
npm install -g allure-commandline
# or on Mac:
brew install allure
```

### Playwright
```bash
# Run all tests, generate playwright-report/
npx playwright test

# Open the HTML report in browser
npx playwright show-report

# Run with explicit HTML reporter
npx playwright test --reporter=html

# JUnit XML output (for Jenkins/CI to parse)
npx playwright test --reporter=junit

# Multiple reporters simultaneously
npx playwright test --reporter=html,junit

# Open a trace file for debugging
npx playwright show-trace test-results/test-name/trace.zip

# Run specific test file only
npx playwright test tests/orders.spec.ts
```

### GitHub Actions — Publish Playwright Report as Artifact
```yaml
# .github/workflows/e2e.yml
name: E2E Tests

on: [push, pull_request]

jobs:
  e2e:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node
        uses: actions/setup-node@v4
        with:
          node-version: 20

      - name: Install dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install --with-deps

      - name: Run Playwright tests
        run: npx playwright test

      - name: Upload Playwright report
        if: always()   # upload even if tests failed
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30
```

After the workflow runs, the HTML report is downloadable from the GitHub Actions run page under "Artifacts."

---

## SECTION 8 — Interview Q&A

### Q1: Why do test reports matter in a QA process?
**A:** Automated test results are only valuable if the right people can act on them. A developer can read a stack trace, but a Product Owner, Engineering Manager, or Release Manager cannot. Good reports translate pass/fail data into business-readable information: which features are healthy, which are failing, whether the release is safe to ship. Reports also serve as a historical record — you can demonstrate over time that quality is improving (or flag that it is degrading) with trend graphs. Without good reporting, your automation runs but nobody outside QA can make informed decisions from the results.

### Q2: What are the key Allure annotations and what does each one do?
**A:**
- `@Epic` / `@Feature` / `@Story` — organise tests into a three-level hierarchy that maps to your business structure (matches Jira epics, features, stories)
- `@Description` — human-readable description of what the test verifies, shown in the report
- `@Step` — marks a method as a named step; each call appears in the report as a step with pass/fail status and the actual parameter values substituted in
- `@Attachment` — attaches binary or text content (screenshots, logs, JSON) to the test result
- `@Severity` — classifies the test as BLOCKER, CRITICAL, NORMAL, MINOR, or TRIVIAL; Allure dashboard can filter by severity so you prioritise failures
- `@Issue` / `@TmsLink` — hyperlinks from the test result to the corresponding JIRA issue or Xray/Zephyr test case

### Q3: How do you attach a screenshot on failure to the Allure report?
**A:** I implement a TestNG `ITestListener` and override `onTestFailure`. In that method, I get the WebDriver from the test instance, call `driver.getScreenshotAs(OutputType.BYTES)`, and return the byte array from a method annotated with `@Attachment(type = "image/png")`. Allure embeds the screenshot in the report automatically. The key points are: the method must be annotated with `@Attachment`, the return type must be `byte[]` for images, and the listener must be registered in `testng.xml`. For RestAssured API tests, I add `RestAssured.filters(new AllureRestAssured())` in my `@BeforeClass` — this automatically logs every request and response body without any additional code.

### Q4: What information should a test report contain?
**A:** A good test report should contain: (1) an executive summary — total tests, pass/fail/skip counts, pass rate, total duration; (2) a failure list categorised by feature area; (3) for each failed test — the specific assertion that failed, the error message, a screenshot or API response at point of failure, and the step where it failed; (4) test metadata — which environment it ran on, the build number, who ran it; (5) trend/history — comparison with previous runs to identify regressions; (6) links to related items — JIRA issue, test case in Xray/Zephyr.

### Q5: How do you show test trends in reporting?
**A:** In Allure, trend data is maintained by keeping the `allure-results` directory from previous runs. When you run `allure generate`, Allure reads the history from a `history/` folder inside `allure-results` and generates trend charts automatically. In Jenkins, the Allure Jenkins Plugin maintains this history between builds and shows a trend graph in the sidebar. In Playwright, the HTML report does not natively show trends, but you can use tools like Playwright's own test results API or integrate with external dashboards. For long-term trending, many teams push test results to a metrics platform (Grafana + InfluxDB) or use Allure's hosted service (TestOps).

### Q6: What is the difference between Allure, ExtentReports, and Playwright HTML reports?
**A:** All three generate HTML test reports, but they differ in integration and capability. **Allure** is the most feature-rich — it has hierarchical organisation (epic/feature/story), severity levels, external system links (JIRA, Xray), automatic request/response logging for RestAssured, built-in trend history, and a rich interactive dashboard. It requires a separate CLI tool to generate. Best for Java/TestNG suites. **ExtentReports** is a lightweight Java library, no external CLI needed — you generate the report in `@AfterSuite` with a method call. Simpler setup but fewer features — no built-in trends, no external links. Good for teams that want a quick rich HTML report without infrastructure overhead. **Playwright HTML** is built into Playwright — zero extra setup. It is unique in showing video replay, trace viewer (DOM snapshots at every step), and network requests. It is the best debugging tool of the three but is specific to Playwright and does not support Java/TestNG.

### Q7: How do you integrate test reports in CI (Jenkins or GitHub Actions)?
**A:** In **Jenkins**: install the Allure Jenkins Plugin, add a post-build step `allure([results: [[path: 'allure-results']]])`, and the report appears as a sidebar link on each build with automatic history tracking. For Playwright, publish `playwright-report/` as a build artifact. In **GitHub Actions**: add an `upload-artifact` step with `if: always()` (so it runs even when tests fail), pointing at the `playwright-report/` or `allure-report/` folder. The report is then downloadable from the Actions run page. For Allure in GitHub Actions, you can use the `simple-elf/allure-report-action` community action which commits the report to GitHub Pages.

### Q8: What is a flaky test and how do you flag it in reports?
**A:** A flaky test is a test that produces inconsistent results — it passes sometimes and fails sometimes without any code change. Causes include: timing issues (not waiting long enough for async operations), test data collisions (two parallel tests modifying the same record), environment instability, or genuinely intermittent application bugs. In Allure, if you run tests with retries, Allure marks a test as "flaky" if it failed at least once but passed on a retry — these appear with a special flaky badge in the report. In Playwright, set `retries: 2` in `playwright.config.ts`; tests that pass after a retry are flagged as "flaky" in the HTML report and the JUnit XML. The key principle is: flaky tests should be visible and tracked, not silently retried into green — they represent risk.

---

*Guide covers: why reporting matters, Allure annotations with complete code, ExtentReports setup with TestNG listener, Playwright HTML report, Playwright + Allure setup, all CLI commands, and interview Q&A.*
