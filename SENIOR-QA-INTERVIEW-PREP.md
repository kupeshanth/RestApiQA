# Senior QA Engineer — Complete Interview Preparation Guide
## Technical Q&A + Behavioral + Live Assessment + Troubleshooting Scenarios

> **How to use this file:** Read each question. Cover the answer. Try to answer it yourself first.
> Then read the model answer. If you can explain it in your own words, you know it.

---

## TABLE OF CONTENTS

1. [Selenium WebDriver — Deep Dive Q&A](#1-selenium-webdriver--deep-dive-qa)
2. [REST API Testing — Q&A](#2-rest-api-testing--qa)
3. [RestAssured — Q&A](#3-restassured--qa)
4. [TestNG — Q&A](#4-testng--qa)
5. [Appium — Mobile Testing Q&A](#5-appium--mobile-testing-qa)
6. [BDD & Gauge Framework — Q&A](#6-bdd--gauge-framework--qa)
7. [CI/CD — Jenkins & GitLab Q&A](#7-cicd--jenkins--gitlab-qa)
8. [ISTQB Concepts — Q&A](#8-istqb-concepts--qa)
9. [Agile & Scrum — Q&A](#9-agile--scrum--qa)
10. [Framework Design — Walk-Through Questions](#10-framework-design--walk-through-questions)
11. [Behavioral Questions — STAR Answers](#11-behavioral-questions--star-answers)
12. [Live Coding / Assessment Scenarios](#12-live-coding--assessment-scenarios)
13. [Test Case Writing — Given a Feature, Write Tests](#13-test-case-writing--given-a-feature-write-tests)
14. [Tricky Scenario Questions — If/Else Situations](#14-tricky-scenario-questions--ifelse-situations)
15. [What to Say When You Don't Know](#15-what-to-say-when-you-dont-know)

---

## 1. Selenium WebDriver — Deep Dive Q&A

---

**Q: What is the WebDriver architecture? How does Selenium communicate with the browser?**

A: Selenium WebDriver uses a client-server architecture.
- Your Java test code (client) sends JSON Wire Protocol / W3C WebDriver commands
- ChromeDriver / GeckoDriver (server) receives those commands
- The driver translates them into browser-native calls
- The browser executes the action and sends the result back

```
Java Test → WebDriver API → ChromeDriver → Chrome Browser
                    ↑ W3C Protocol (HTTP JSON)
```

---

**Q: What is the difference between `findElement` and `findElements`?**

A:
- `findElement()` returns the first matching element. Throws `NoSuchElementException` if not found
- `findElements()` returns a `List<WebElement>`. Returns empty list (not exception) if not found

```java
WebElement btn   = driver.findElement(By.id("submit"));    // single, throws if missing
List<WebElement> rows = driver.findElements(By.tagName("tr")); // list, empty if missing
```

Use `findElements().size() > 0` to check if an element exists without catching exceptions.

---

**Q: Explain all locator strategies and when to use each.**

A:

| Locator | Best When | Example |
|---------|-----------|---------|
| `By.id` | Element has unique id | `By.id("username")` — fastest |
| `By.name` | Form elements with name attribute | `By.name("email")` |
| `By.className` | Single class, not shared | `By.className("btn-primary")` |
| `By.tagName` | Counting/listing elements | `By.tagName("tr")` for table rows |
| `By.linkText` | Exact hyperlink text | `By.linkText("Sign In")` |
| `By.partialLinkText` | Partial hyperlink text | `By.partialLinkText("Sign")` |
| `By.cssSelector` | Fast, flexible, widely supported | `By.cssSelector(".form input[type='email']")` |
| `By.xpath` | Complex dynamic elements, no ID | `By.xpath("//button[contains(text(),'Submit')]")` — slowest |

**Priority order:** id → name → cssSelector → xpath

---

**Q: What are implicit wait, explicit wait, and fluent wait? When do you use each?**

A:

```java
// ── IMPLICIT WAIT ──────────────────────────────────────────────────────────
// Applies globally to ALL findElement calls. Waits up to X seconds for element to appear.
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// PROBLEM: It slows down negative tests (tests checking element is NOT there wait the full timeout)
// Use: only as a baseline, set low (5-10 seconds)

// ── EXPLICIT WAIT ──────────────────────────────────────────────────────────
// Waits for a specific condition on a specific element. More precise.
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));

// Common ExpectedConditions:
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("msg")));
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loader")));
wait.until(ExpectedConditions.textToBePresentInElement(element, "Success"));
wait.until(ExpectedConditions.urlContains("/dashboard"));
wait.until(ExpectedConditions.numberOfElementsToBe(By.tagName("tr"), 5));

// Use: for any dynamic element that appears/disappears after action

// ── FLUENT WAIT ────────────────────────────────────────────────────────────
// Like explicit wait but with polling interval and exception ignoring.
Wait<WebDriver> fluentWait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(30))
    .pollingEvery(Duration.ofSeconds(2))              // check every 2 seconds
    .ignoring(NoSuchElementException.class);          // ignore this exception while waiting

// Use: for elements that take unpredictable time, like loading animations

// ── NEVER MIX implicit + explicit ──────────────────────────────────────────
// They interact unpredictably. Pick one approach per project.
```

---

**Q: What is Page Object Model (POM) and why do you use it?**

A: POM separates the page structure (locators, actions) from the test logic. Each web page has its own class.

```java
// Without POM (bad — locators scattered across tests):
driver.findElement(By.id("username")).sendKeys("admin");
driver.findElement(By.id("password")).sendKeys("secret");
driver.findElement(By.id("loginBtn")).click();

// ── With POM ───────────────────────────────────────────────────────────────
// LoginPage.java (page class — describes the page)
public class LoginPage {
    private WebDriver driver;

    @FindBy(id = "username") private WebElement usernameField;
    @FindBy(id = "password") private WebElement passwordField;
    @FindBy(id = "loginBtn")  private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public DashboardPage login(String username, String password) {
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
        return new DashboardPage(driver);
    }
}

// LoginTest.java (test class — describes the test scenario)
@Test
public void validLogin() {
    LoginPage loginPage = new LoginPage(driver);
    DashboardPage dashboard = loginPage.login("admin", "secret");
    Assert.assertTrue(dashboard.isLoaded());
}
```

**Benefits:**
- Locator changes in ONE place (not 50 test files)
- Tests are readable — `loginPage.login()` not `findElement(By.id("..."))`
- Reusable across multiple tests

---

**Q: How do you handle dynamic elements with no stable ID?**

A:
```java
// 1. CSS selector by attribute that IS stable
driver.findElement(By.cssSelector("[data-testid='submit-button']"));

// 2. XPath with contains() for partial text
driver.findElement(By.xpath("//button[contains(text(),'Submit')]"));

// 3. XPath traversal from stable parent
driver.findElement(By.xpath("//div[@class='order-row']/button[@type='submit']"));

// 4. XPath with index (last resort — fragile)
driver.findElement(By.xpath("(//button[@class='btn'])[2]"));    // second button

// 5. Wait for the element to have a stable state
wait.until(ExpectedConditions.attributeContains(By.id("status"), "class", "loaded"));
```

---

**Q: How do you handle iframes?**

A:
```java
// Switch to iframe first — then interact with elements inside it
driver.switchTo().frame("iframeName");            // by name attribute
driver.switchTo().frame(0);                       // by index
driver.switchTo().frame(driver.findElement(By.id("myIframe")));  // by element

// Do your actions inside iframe
driver.findElement(By.id("insideIframe")).click();

// Switch BACK to main document when done
driver.switchTo().defaultContent();               // go back to main page
driver.switchTo().parentFrame();                  // go up one level
```

---

**Q: How do you handle alerts/popups?**

A:
```java
// Wait for alert
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.alertIsPresent());

// Switch to alert
Alert alert = driver.switchTo().alert();

// Options:
alert.accept();                // click OK
alert.dismiss();               // click Cancel
alert.getText();               // read alert message
alert.sendKeys("input text"); // type in prompt alert
```

---

**Q: What is StaleElementReferenceException and how do you handle it?**

A: Thrown when the element you found is no longer attached to the DOM — the page refreshed or element was re-rendered since you found it.

```java
// Solution 1: Re-find the element after each action
WebElement element = driver.findElement(By.id("row"));
element.click();    // DOM refreshes
element = driver.findElement(By.id("row"));   // find again
element.getText();

// Solution 2: Wrap in retry logic
public WebElement findWithRetry(By locator) {
    for (int attempt = 0; attempt < 3; attempt++) {
        try {
            return driver.findElement(locator);
        } catch (StaleElementReferenceException e) {
            if (attempt == 2) throw e;
        }
    }
    return null;
}
```

---

**Q: How do you run tests in parallel with TestNG?**

A:
```xml
<!-- testng.xml — parallel at test level -->
<suite name="Parallel Suite" parallel="tests" thread-count="3">
    <test name="Chrome Tests">
        <parameter name="browser" value="chrome"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
    <test name="Firefox Tests">
        <parameter name="browser" value="firefox"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
</suite>
```

```java
// In BaseTest — use ThreadLocal to avoid driver conflicts between threads
private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

public WebDriver getDriver() { return driver.get(); }

@BeforeMethod
@Parameters("browser")
public void setUp(String browser) {
    if (browser.equals("chrome")) {
        driver.set(new ChromeDriver());
    } else {
        driver.set(new FirefoxDriver());
    }
}

@AfterMethod
public void tearDown() {
    getDriver().quit();
    driver.remove();
}
```

---

## 2. REST API Testing — Q&A

---

**Q: What is a REST API? What makes an API "RESTful"?**

A: REST (Representational State Transfer) is an architectural style for APIs. An API is RESTful when it follows these 6 constraints:
1. **Client-Server** — UI and backend are separate, communicate over HTTP
2. **Stateless** — each request contains all info needed; server stores no session
3. **Cacheable** — responses say whether they can be cached
4. **Uniform Interface** — standard HTTP methods (GET/POST/PUT/DELETE), resources identified by URLs
5. **Layered System** — client doesn't know if it's talking to the real server or a proxy
6. **Code on Demand** (optional) — server can send executable code to client

---

**Q: Explain all HTTP methods and when each is used.**

A:

| Method | Purpose | Idempotent? | Safe? |
|--------|---------|------------|-------|
| GET | Read data | Yes | Yes |
| POST | Create new resource | No | No |
| PUT | Replace resource completely | Yes | No |
| PATCH | Partially update resource | No | No |
| DELETE | Remove resource | Yes | No |
| HEAD | Like GET but no body (check existence) | Yes | Yes |
| OPTIONS | What methods does this endpoint support? | Yes | Yes |

**Idempotent** = calling it multiple times gives same result  
**Safe** = does not change server state

---

**Q: Walk me through all HTTP status codes you test for.**

A:

```
2xx — Success
  200 OK           → GET, PUT, PATCH succeeded
  201 Created      → POST created a new resource
  204 No Content   → DELETE succeeded, no body returned

4xx — Client errors (YOUR fault as the caller)
  400 Bad Request       → wrong request body/params
  401 Unauthorized      → not authenticated
  403 Forbidden         → authenticated but not permitted
  404 Not Found         → resource doesn't exist
  405 Method Not Allowed → e.g. DELETE not supported here
  409 Conflict          → duplicate resource
  415 Unsupported Media Type → wrong Content-Type
  422 Unprocessable Entity  → validation failed
  429 Too Many Requests     → rate limit hit

5xx — Server errors (THEIR fault)
  500 Internal Server Error → server crashed
  502 Bad Gateway           → upstream server problem
  503 Service Unavailable   → overloaded/down
  504 Gateway Timeout       → upstream timed out
```

---

**Q: What is the difference between authentication and authorization?**

A:
- **Authentication** = "Who are you?" — verifying identity (login, token check)
- **Authorization** = "What can you do?" — checking permissions after authentication

```
401 Unauthorized → failed authentication (not logged in / wrong token)
403 Forbidden    → passed authentication BUT not authorized for this action
```

Example: You're logged in as a regular user (authenticated). You try to access the admin panel. You get **403** — you're known, but not permitted.

---

**Q: What is the difference between PUT and PATCH?**

A:
- **PUT** replaces the entire resource. You must send ALL fields. Missing fields get wiped.
- **PATCH** updates only the fields you send. Other fields stay unchanged.

```json
// Current resource: { "id": 1, "title": "Old", "body": "Content", "userId": 1 }

// PUT — must send everything, or "body" and "userId" will be gone:
PUT /posts/1  { "id": 1, "title": "New Title", "body": "Content", "userId": 1 }

// PATCH — send only what changes:
PATCH /posts/1  { "title": "New Title" }
// Result: { "id": 1, "title": "New Title", "body": "Content", "userId": 1 }
```

---

**Q: What is the difference between query parameters and path parameters?**

A:
```
Path parameter  → identifies a specific resource
  /posts/1        → the post with id=1
  /users/42/orders → orders belonging to user 42

Query parameter → filters, sorts, paginates a collection
  /posts?userId=1          → all posts by user 1
  /posts?_limit=10&_page=2 → page 2 with 10 items per page
  /users?sort=name&order=asc
```

---

**Q: How do you test API authentication in your test suite?**

A:
```
1. Happy path: valid token → 200
2. Missing token: no auth header → 401
3. Wrong token: invalid token → 401
4. Expired token: fetch fresh token in BeforeClass
5. Insufficient permission: valid token, wrong role → 403
6. Token in wrong format: "token" instead of "Bearer token" → 401
```

---

**Q: What is JSON Schema validation and why is it important?**

A: JSON Schema defines the expected structure of a response — field names, data types, required fields. Instead of asserting field by field, you validate the entire contract in one assertion.

```json
{
  "type": "object",
  "properties": {
    "id":    { "type": "integer" },
    "email": { "type": "string" },
    "name":  { "type": "string" }
  },
  "required": ["id", "email", "name"]
}
```

**Why important:** If a developer renames `userId` to `user_id`, or changes a field type from integer to string, your schema test catches it immediately — even if you hadn't written an explicit assertion for that field. It's a contract test.

---

## 3. RestAssured — Q&A

---

**Q: Walk me through a complete RestAssured test from scratch.**

A:
```java
import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class UserTest {

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = "https://api.example.com";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void createUser_returns201() {
        String requestBody = "{ \"name\": \"John\", \"email\": \"john@test.com\" }";

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body(requestBody)
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("John"))
            .body("email", equalTo("john@test.com"))
            .header("Content-Type", containsString("application/json"))
            .time(lessThan(3000L));
    }
}
```

---

**Q: What is RequestSpecification and why do you use it?**

A: A reusable configuration applied to multiple requests. Defined once in BaseTest, used everywhere.

```java
// Define once in BaseTest:
protected RequestSpecification requestSpec;

requestSpec = new RequestSpecBuilder()
    .setBaseUri("https://api.example.com")
    .setContentType(ContentType.JSON)
    .addHeader("Authorization", "Bearer " + token)
    .addHeader("x-api-version", "2")
    .build();

// Use in every test:
given()
    .spec(requestSpec)
    .body(data)
.when()
    .post("/users");

// Real-world value: when auth token rotates, update in ONE place
```

---

**Q: How do you extract values from a response to use in the next request?**

A:
```java
// Extract a single field
int newId = given()
    .spec(requestSpec)
    .body(requestBody)
.when()
    .post("/users")
.then()
    .statusCode(201)
    .extract().path("id");   // ← extracts integer "id" field

// Extract full response
Response response = given()
    .when().get("/users/1")
    .then().statusCode(200)
    .extract().response();

String email = response.path("email");
int userId   = response.path("id");

// Use extracted id in next request
given()
    .spec(requestSpec)
.when()
    .delete("/users/" + newId)
.then()
    .statusCode(204);
```

---

**Q: What is the difference between soft and hard assertions? When do you use soft assertions?**

A:
- **Hard assertion (default):** test stops immediately on first failure. You see one failure.
- **Soft assertion:** all assertions run, failures collected, reported all at end.

```java
// Hard — stops at first failure (normal for most tests)
.statusCode(200)
.body("id", equalTo(1))        // if this fails → test stops here
.body("name", equalTo("John")) // this never runs

// Soft — all run, all failures reported (use when validating multiple fields in one response)
SoftAssert soft = new SoftAssert();
soft.assertEquals(response.path("id").toString(), "1");
soft.assertEquals(response.path("name"), "John");
soft.assertNotNull(response.path("email"));
soft.assertAll();   // ← reports ALL failures. Never forget this line.
```

**When to use soft:** Reporting defects — you want one test run to reveal ALL wrong fields, not one per run.

---

## 4. TestNG — Q&A

---

**Q: Explain all TestNG annotations in order of execution.**

A:
```java
@BeforeSuite   → runs once before entire test suite
@BeforeTest    → runs once before each <test> tag in testng.xml
@BeforeClass   → runs once before first test method in this class
@BeforeMethod  → runs before EACH @Test method

@Test          → the actual test

@AfterMethod   → runs after EACH @Test method
@AfterClass    → runs once after last test method in this class
@AfterTest     → runs once after each <test> tag
@AfterSuite    → runs once after entire suite
```

---

**Q: What is a DataProvider? Give a real example.**

A:
```java
// DataProvider — supplies test data to a @Test method
@DataProvider(name = "loginData")
public Object[][] loginScenarios() {
    return new Object[][] {
        // username,   password,    expectedResult
        { "admin",    "correct",   "Dashboard"   },  // valid login
        { "admin",    "wrong",     "Error"       },  // wrong password
        { "",         "correct",   "Error"       },  // empty username
        { "admin",    "",          "Error"       },  // empty password
    };
}

// Test uses the DataProvider — runs 4 times, once per row
@Test(dataProvider = "loginData")
public void loginTest(String username, String password, String expectedResult) {
    loginPage.enterUsername(username);
    loginPage.enterPassword(password);
    loginPage.clickLogin();
    Assert.assertTrue(driver.getPageSource().contains(expectedResult));
}
```

---

**Q: How do you group tests and run only specific groups?**

A:
```java
// Tag tests with groups
@Test(groups = {"smoke", "regression"})
public void loginTest() { ... }

@Test(groups = {"regression"})
public void complexScenario() { ... }
```

```xml
<!-- testng.xml — run only smoke tests -->
<suite name="Suite">
    <test name="Smoke">
        <groups>
            <run><include name="smoke"/></run>
        </groups>
        <classes><class name="tests.LoginTest"/></classes>
    </test>
</suite>
```

```bash
# Maven command
mvn test -Dgroups=smoke
```

---

**Q: What is a TestNG Listener? Name two important ones.**

A: Listeners intercept test events (start, pass, fail, skip) so you can add custom behaviour.

```java
// ITestListener — react to test pass/fail
public class TestListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) {
        // Take screenshot on failure
        TakesScreenshot ts = (TakesScreenshot) driver;
        File screenshot = ts.getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(screenshot, new File("screenshots/" + result.getName() + ".png"));
    }
}

// IRetryAnalyzer — retry flaky tests
public class RetryAnalyzer implements IRetryAnalyzer {
    int count = 0;
    int maxRetry = 2;

    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && count < maxRetry) {
            count++;
            return true;   // retry
        }
        return false;
    }
}

// Register listener in testng.xml:
<suite name="Suite">
    <listeners>
        <listener class-name="utils.TestListener"/>
    </listeners>
</suite>
```

---

## 5. Appium — Mobile Testing Q&A

---

**Q: What is Appium and how does it work?**

A: Appium is an open-source framework for automating native, hybrid, and mobile web apps on iOS and Android. It extends WebDriver protocol with mobile-specific commands.

```
Your Test → Appium Client (Java/Python) → Appium Server → Device/Emulator
                                                  ↓
                                         UIAutomator2 (Android)
                                         XCUITest (iOS)
```

Appium Server acts as a middleware — translates WebDriver commands into platform-native automation.

---

**Q: What are Desired Capabilities? Give examples.**

A: Key-value pairs that tell Appium what to automate and how.

```java
DesiredCapabilities caps = new DesiredCapabilities();

// Required for Android:
caps.setCapability("platformName", "Android");
caps.setCapability("deviceName", "Pixel_4_Emulator");
caps.setCapability("appPackage", "com.example.app");
caps.setCapability("appActivity", ".MainActivity");
caps.setCapability("automationName", "UIAutomator2");

// Required for iOS:
caps.setCapability("platformName", "iOS");
caps.setCapability("deviceName", "iPhone 14");
caps.setCapability("bundleId", "com.example.app");
caps.setCapability("automationName", "XCUITest");
caps.setCapability("udid", "device-udid-here");

// Optional but useful:
caps.setCapability("noReset", true);        // don't reset app state between runs
caps.setCapability("fullReset", false);     // don't reinstall app
caps.setCapability("newCommandTimeout", 60); // wait 60s for commands
```

---

**Q: What is the difference between native, hybrid, and mobile web apps?**

A:

| Type | Description | Automation |
|------|-------------|------------|
| Native | Built with platform SDK (Swift/Kotlin) | UIAutomator2 / XCUITest |
| Hybrid | Native wrapper around web content (WebView) | Switch context between NATIVE_APP and WEBVIEW |
| Mobile Web | Website in mobile browser (Chrome/Safari) | Standard Selenium WebDriver |

```java
// Handling Hybrid app — switching contexts
Set<String> contexts = driver.getContextHandles();
// contexts: [NATIVE_APP, WEBVIEW_com.example.app]

driver.context("NATIVE_APP");      // interact with native elements
driver.context("WEBVIEW_com.example.app"); // interact with web elements
```

---

**Q: How do you locate elements in Appium?**

A:
```java
// Android locators
driver.findElement(By.id("com.example:id/username"));             // resource-id
driver.findElement(By.xpath("//android.widget.TextView[@text='Login']"));
driver.findElement(MobileBy.AccessibilityId("login_button"));     // content-desc
driver.findElement(MobileBy.AndroidUIAutomator(
    "new UiSelector().text(\"Submit\")"));

// iOS locators
driver.findElement(By.xpath("//XCUIElementTypeButton[@name='Login']"));
driver.findElement(MobileBy.AccessibilityId("login_button"));
driver.findElement(MobileBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND name == 'Login'"));

// Best: use AccessibilityId — works on both Android (content-desc) and iOS (accessibility identifier)
```

---

**Q: How do you perform gestures in Appium?**

A:
```java
// Tap
new TouchAction(driver)
    .tap(PointOption.point(500, 300))
    .perform();

// Swipe (scroll)
new TouchAction(driver)
    .press(PointOption.point(500, 800))
    .waitAction(WaitOptions.waitOptions(Duration.ofMillis(800)))
    .moveTo(PointOption.point(500, 200))
    .release().perform();

// Long press
new TouchAction(driver)
    .longPress(LongPressOptions.longPressOptions()
        .withElement(ElementOption.element(element))
        .withDuration(Duration.ofSeconds(2)))
    .release().perform();

// Scroll to element (Android)
driver.findElement(MobileBy.AndroidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(\"Target\"))"));
```

---

## 6. BDD & Gauge Framework — Q&A

---

**Q: What is BDD and what problem does it solve?**

A: Behaviour-Driven Development bridges communication between business (non-technical) and technical teams. Tests are written in plain English that everyone understands — so business stakeholders can read and verify test scenarios.

```
Without BDD: Dev builds feature, QA tests it, finds it doesn't match what business wanted.
With BDD:    Business writes scenarios in Gherkin first. Dev implements to make those scenarios pass.
```

---

**Q: What is Gherkin syntax? Give an example.**

A:
```gherkin
Feature: User Login

  Background:
    Given the user is on the login page

  Scenario: Successful login with valid credentials
    When the user enters username "admin" and password "secret"
    And the user clicks the Login button
    Then the user should be redirected to the Dashboard
    And the page title should be "Dashboard"

  Scenario Outline: Invalid login attempts
    When the user enters username "<username>" and password "<password>"
    And clicks Login
    Then an error message "<errorMsg>" should appear

    Examples:
      | username | password | errorMsg            |
      | admin    | wrong    | Invalid credentials |
      |          | secret   | Username required   |
      | admin    |          | Password required   |
```

Keywords:
- **Feature:** — what feature is being tested
- **Scenario:** — a specific test scenario
- **Background:** — steps that run before each scenario
- **Given** — precondition / initial state
- **When** — the action being performed
- **Then** — the expected outcome
- **And/But** — continuation of previous step type

---

**Q: What is the Gauge framework? How is it different from Cucumber?**

A:

| Feature | Gauge | Cucumber |
|---------|-------|---------|
| Language | Markdown (.spec files) | Gherkin (.feature files) |
| Step definitions | Java/Python/JS/Ruby | Java/Python/JS/Ruby |
| Reports | Built-in HTML reports | Needs Cucumber reporting plugin |
| IDE support | IntelliJ, VS Code plugins | IntelliJ, Eclipse plugins |
| Data tables | Native Markdown tables | Cucumber data tables |

```markdown
<!-- Gauge .spec file -->
# Login Feature

## Successful login
* User navigates to login page
* User enters username "admin" and password "secret"
* User should see the dashboard
```

```java
// Step implementation
public class LoginSteps {
    @Step("User enters username <username> and password <password>")
    public void enterCredentials(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }
}
```

---

## 7. CI/CD — Jenkins & GitLab Q&A

---

**Q: What is CI/CD and why does QA care about it?**

A:
- **CI (Continuous Integration):** code changes trigger automatic build + test on every push
- **CD (Continuous Delivery):** tested code automatically deployed to staging/production

QA cares because:
1. Your automated tests run on every code change — bugs caught before production
2. You see test reports in Jenkins/GitLab without running locally
3. If your tests are flaky, they block deployments — so test quality matters more

---

**Q: Describe a Jenkins pipeline for running your tests.**

A:
```groovy
// Jenkinsfile (declarative pipeline)
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/your-org/qa-tests.git'
            }
        }

        stage('Install Dependencies') {
            steps {
                sh 'mvn dependency:resolve'
            }
        }

        stage('Run Smoke Tests') {
            steps {
                sh 'mvn test -Dgroups=smoke'
            }
        }

        stage('Run Full Regression') {
            when { branch 'main' }    // only on main branch
            steps {
                sh 'mvn test'
            }
        }

        stage('Publish Reports') {
            steps {
                publishHTML(target: [
                    allowMissing: false,
                    reportDir: 'target/surefire-reports',
                    reportFiles: 'index.html',
                    reportName: 'Test Report'
                ])
            }
        }
    }

    post {
        failure {
            emailext to: 'qa-team@company.com',
                subject: "Test FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Check Jenkins: ${env.BUILD_URL}"
        }
    }
}
```

---

**Q: What is a GitLab CI/CD pipeline? How is it different from Jenkins?**

A:

| | Jenkins | GitLab CI/CD |
|--|---------|-------------|
| Config | Jenkinsfile (Groovy) | `.gitlab-ci.yml` (YAML) |
| Hosting | Self-hosted server | Built into GitLab |
| Setup | Need to install/maintain Jenkins | No extra setup |
| Trigger | Webhook or schedule | Push, MR, schedule |

```yaml
# .gitlab-ci.yml
stages:
  - test

smoke-tests:
  stage: test
  image: maven:3.9-openjdk-11
  script:
    - mvn test -Dgroups=smoke
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
  only:
    - merge_requests

regression-tests:
  stage: test
  script:
    - mvn clean test
  only:
    - main
```

---

## 8. ISTQB Concepts — Q&A

---

**Q: What is equivalence partitioning? Give an example.**

A: Divide input data into partitions where the system behaves the same way. Test one value from each partition — don't test all values, just one representative.

```
Example: Age field accepts 18-65

Partitions:
  < 18         → invalid (test with: 17, 0, -1)
  18-65        → valid   (test with: 18, 40, 65)
  > 65         → invalid (test with: 66, 100)

You only test one value from each partition:
  Invalid below: 17
  Valid: 40
  Invalid above: 66
```

---

**Q: What is boundary value analysis?**

A: Test at the boundaries of each partition — where bugs most commonly hide.

```
Age field: 18-65

Boundaries to test:
  17  → just below min (invalid)
  18  → min boundary (valid)
  19  → just above min (valid)
  64  → just below max (valid)
  65  → max boundary (valid)
  66  → just above max (invalid)
```

---

**Q: What is the difference between verification and validation?**

A:
- **Verification** = "Are we building the product RIGHT?" — checking work products against specifications (reviews, walkthroughs, inspections) — no code running
- **Validation** = "Are we building the RIGHT product?" — does the software meet the user's actual needs? (testing the running system)

---

**Q: What are the test levels?**

A:

| Level | What is tested | Who tests |
|-------|---------------|-----------|
| Unit | Single function/class | Developer |
| Integration | Components working together | Developer / QA |
| System | Complete system end-to-end | QA |
| Acceptance (UAT) | Meets business requirements | Business / QA |

---

**Q: What is risk-based testing?**

A: Prioritise testing based on likelihood and impact of failure.

```
Risk = Probability of failure × Impact if it fails

High risk (test first):
  - Payment processing (high impact + complex = likely bugs)
  - Login / Authentication (used by everyone)

Low risk (test last or not at all):
  - About page (static content, low impact)
  - Rarely-used admin feature (low probability of use)
```

In practice: work with the team to identify high-risk areas, write more tests there, focus less on low-risk.

---

**Q: What is the defect lifecycle?**

A:
```
New → Assigned → Open (being fixed) → Fixed → Ready for Retest
  → Retest Pass → Closed
  → Retest Fail → Reopened → Open → Fixed → Retest...

OR:
New → Rejected (not a bug / by design / duplicate)
New → Deferred (won't fix now, future release)
```

---

## 9. Agile & Scrum — Q&A

---

**Q: What is QA's role in each Scrum ceremony?**

A:

| Ceremony | QA's Role |
|----------|-----------|
| Sprint Planning | Review user stories, ask clarifying questions, identify acceptance criteria gaps, estimate testing effort |
| Daily Standup | Report what testing you did yesterday, what today, any blockers |
| Sprint Review (Demo) | Verify features are demo-ready, run quick smoke tests before demo |
| Sprint Retrospective | Raise quality issues, suggest process improvements |
| Backlog Grooming | Review upcoming stories, identify testability issues early, suggest AC changes |

---

**Q: How do you decide when a feature is "done" from QA's perspective?**

A: A feature is done when:
1. All acceptance criteria are met and verified
2. All test cases (happy path + negative + edge cases) pass
3. No P1/P2 defects remain open
4. Regression tests pass — existing features not broken
5. Performance is acceptable (response time within agreed SLA)
6. The story has been reviewed in the demo

---

**Q: How do you handle testing when developers finish late in the sprint?**

A:
```
Honest answer approach:
1. Risk-based: test the highest-risk functionality first when time is short
2. Communicate early: if I see a story won't be testable in time, I raise it immediately — not on the last day
3. Smoke test first: basic happy path passes before going deeper
4. Defer to next sprint: document what was NOT tested, create tech debt ticket
5. Regression automation: reduces time needed for regression so more time for new feature testing
```

---

## 10. Framework Design — Walk-Through Questions

---

**Q: Design a Selenium + TestNG automation framework from scratch.**

A: Walk through this structure in the interview:

```
framework/
├── pom.xml                          ← Maven, dependencies (Selenium, TestNG, WebDriverManager)
├── src/
│   ├── main/java/
│   │   ├── base/
│   │   │   └── BaseTest.java        ← driver init/teardown, ThreadLocal for parallel
│   │   ├── pages/
│   │   │   ├── LoginPage.java       ← POM: locators + actions
│   │   │   └── DashboardPage.java
│   │   ├── utils/
│   │   │   ├── ConfigReader.java    ← read config.properties (URLs, credentials)
│   │   │   ├── ScreenshotUtil.java  ← take screenshot on failure
│   │   │   └── WaitUtil.java        ← reusable wait helpers
│   │   └── listeners/
│   │       └── TestListener.java    ← screenshot on failure, logging
│   └── test/java/
│       └── tests/
│           └── LoginTest.java       ← @Test methods
├── src/test/resources/
│   ├── config.properties            ← base_url, browser, credentials
│   └── testng.xml
```

**Key decisions to explain:**
1. **ThreadLocal WebDriver** — parallel execution without driver conflicts
2. **WebDriverManager** — no manual ChromeDriver version management
3. **config.properties** — environment URLs without changing code
4. **ITestListener** — automatic screenshot on any test failure
5. **Page Object Model** — single place to update locators

---

**Q: How would you design an API test framework?**

A:
```
api-framework/
├── pom.xml                          ← RestAssured, TestNG, Jackson
├── src/test/java/
│   ├── base/BaseTest.java           ← baseURI, requestSpec, token fetch
│   ├── tests/
│   │   ├── GetTests.java
│   │   ├── PostTests.java
│   │   └── ChainedTests.java
│   └── utils/
│       ├── ApiConstants.java        ← base URL, endpoints
│       └── TokenManager.java        ← login once, cache token
├── src/test/resources/
│   ├── testng.xml
│   └── schemas/response-schema.json
```

---

## 11. Behavioral Questions — STAR Answers

**(STAR = Situation, Task, Action, Result)**

---

**Q: Tell me about a time you found a critical bug in production.**

A (template — fill with your experience):
```
Situation: During sprint X at [Company], a payment feature was about to be released.
Task: I was responsible for regression testing the checkout flow.
Action: While testing edge cases (large order amounts), I discovered the total calculation
        was rounding incorrectly for orders over $999. I documented it with steps to
        reproduce, actual vs expected values, and raised it as P1.
Result: The bug was fixed before release, preventing financial discrepancies for customers.
        I also automated this scenario so it would be caught in regression in future.
```

---

**Q: Tell me about a time you improved a testing process.**

A (template):
```
Situation: Our regression testing took 3 days manually before each release.
Task: As QA lead, I was asked to reduce this time.
Action: I identified the top 50 most critical scenarios, automated them with Selenium + TestNG,
        integrated into Jenkins to run on every PR.
Result: Regression time reduced from 3 days to 2 hours automated run.
        We caught 3 regressions in the first month that would have reached production.
```

---

**Q: Tell me about a disagreement with a developer over a bug.**

A (template):
```
Situation: A developer marked my bug as "by design" — the UI wasn't showing a loading
           indicator during an API call.
Task: I believed this was a UX defect that would confuse users.
Action: I referenced the UX spec which explicitly showed a loader, added the spec link
        to the ticket, and asked the Product Owner to clarify. I didn't escalate emotionally
        — I presented evidence.
Result: PO confirmed it was a defect. Developer fixed it. I learned to always reference
        documented requirements when raising disputed bugs.
```

---

**Q: How do you handle testing when requirements are unclear?**

A:
```
1. First, I ask the Product Owner / BA for clarification — don't assume
2. If unavailable, I document my assumption in the test case ("Assumed: X based on similar feature Y")
3. I test both the assumed behaviour AND alternative interpretations if time allows
4. I raise a question in the story comments so the team documents the decision
5. I never skip testing due to unclear requirements — that's when bugs hide
```

---

## 12. Live Coding / Assessment Scenarios

---

**Scenario: "Here's an API. Write tests for it in 30 minutes."**

```
Step 1 (2 minutes): Read the docs. Note: base URL, endpoints, auth, required fields.
Step 2 (2 minutes): Set up baseURI in BaseTest, add auth to requestSpec.
Step 3 (25 minutes): Write tests in this order:
  - GET all → 200, non-empty
  - GET by ID → 200, correct fields
  - GET invalid ID → 404
  - POST valid → 201, ID generated
  - POST missing required field → 400
  - DELETE → 200/204
Step 4 (1 minute): Run tests, show BUILD SUCCESS.
```

---

**Scenario: "Write test cases for the Login feature."**

```
Functional Test Cases:
TC001 - Valid username + valid password → redirect to Dashboard
TC002 - Valid username + wrong password → error "Invalid credentials"
TC003 - Wrong username + valid password → error "Invalid credentials"
TC004 - Empty username + valid password → error "Username is required"
TC005 - Valid username + empty password → error "Password is required"
TC006 - Both empty → error messages for both fields
TC007 - Username with spaces → trimmed or rejected
TC008 - Password case sensitivity → "Secret" ≠ "secret"
TC009 - Remember me checkbox → session persists after browser close
TC010 - Forgot password link → navigates to password reset page
TC011 - Login after 5 wrong attempts → account locked (if applicable)
TC012 - Login with SQL injection → no error, system handles safely
TC013 - Login from mobile browser → responsive layout, works correctly
TC014 - Login page response time < 2 seconds
TC015 - Login page loaded over HTTPS (padlock in browser)
```

---

**Scenario: "A test is passing locally but failing in CI. Debug it."**

```
Step 1: Check if it's environment-specific
  → Is the base URL the same? (CI might point to staging, local to dev)
  → Is the auth token the same? (CI might use expired/wrong token)

Step 2: Check timing issues
  → CI machines are often slower → increase timeouts
  → Is there an async operation not properly waited for?

Step 3: Check test isolation
  → Does this test depend on data from a previous test?
  → In CI tests might run in different order

Step 4: Check headless vs headed
  → For UI tests: headless Chrome behaves differently
  → Use: ChromeOptions.addArguments("--headless", "--no-sandbox", "--disable-gpu")

Step 5: Read CI logs carefully
  → The actual error message in CI logs shows you what's different
```

---

## 13. Test Case Writing — Given a Feature, Write Tests

---

**Feature: Shopping Cart**

```
Functional:
TC001 - Add item to empty cart → item appears in cart, quantity = 1
TC002 - Add same item twice → quantity = 2 (not two separate lines)
TC003 - Add different items → both appear as separate lines
TC004 - Remove item from cart → item removed, total recalculated
TC005 - Update quantity → total price updates correctly
TC006 - Apply valid coupon → discount applied, total reduced
TC007 - Apply invalid coupon → error "Invalid coupon code"
TC008 - Apply expired coupon → error "Coupon has expired"
TC009 - Proceed to checkout with empty cart → error or checkout button disabled
TC010 - Cart persists after page refresh (session storage)
TC011 - Cart persists after logout and login (if user account)

Boundary / Edge:
TC012 - Add maximum quantity allowed (e.g. 99) → success
TC013 - Add more than max quantity → error "Maximum quantity is 99"
TC014 - Cart with 0 quantity → item removed automatically
TC015 - Very long product name → truncated properly in cart display

Non-functional:
TC016 - Cart page loads in < 2 seconds
TC017 - Cart works on mobile (responsive)
TC018 - Cart updates without full page reload (AJAX)
```

---

## 14. Tricky Scenario Questions — If/Else Situations

---

**"The API returns 200 but the data is wrong. Is this a pass or fail?"**

```
FAIL. Status 200 only means HTTP communication succeeded.
It does NOT mean the business logic is correct.
You must ALWAYS assert the response body too, not just the status code.

Example:
  Status 200 → response.statusCode(200) passes ✓
  Body: { "total": 0 } when it should be 99.99 → body assertion fails ✗
```

---

**"A test passes 9 out of 10 times. What do you do?"**

```
1. Don't mark it as stable — a flaky test is a failing test
2. Investigate root cause:
   - Timing issue? → add explicit wait
   - Test data dependency? → make test data setup deterministic
   - Race condition? → add synchronisation
   - Network flakiness? → add retry logic
3. Tag it @flaky and exclude from blocking CI gates until fixed
4. Never ignore flaky tests — they erode trust in the whole test suite
```

---

**"A developer says your bug is not reproducible. What do you do?"**

```
1. Provide exact steps to reproduce — written step by step, no assumptions
2. Provide: OS, browser version, test data used, environment
3. Record a video or take screenshots with timestamps
4. Check if it's environment-specific (only happens in staging, not dev)
5. Try to reproduce on the developer's machine together
6. If still can't reproduce → leave the bug open with all evidence, don't close it
   → it may be intermittent, not gone
```

---

**"You don't have enough time to test everything before release. What do you do?"**

```
1. Risk-based prioritisation:
   - Test payment/auth first (highest impact if broken)
   - Skip testing rarely-used admin reports (low risk)

2. Communicate immediately to stakeholders:
   - "We can release with X tested. Y and Z are untested — these are the risks."
   - Never silently skip testing and hope nothing breaks

3. Regression automation:
   - Automate the most critical paths so they always run
   - Manual time goes to new/exploratory testing

4. Document what was NOT tested as known risk in release notes
```

---

**"How do you test something that has no requirements document?"**

```
1. Talk to the developer — they know what they built
2. Talk to the Product Owner — they know what they wanted
3. Look at similar features in the application as reference
4. Use the UI/UX design mockups as implicit requirements
5. Document your assumptions in test cases
6. Exploratory testing — structured exploration noting what you find
7. Raise a story to get the feature properly documented after release
```

---

## 15. What to Say When You Don't Know

### If asked about something you haven't used:

```
Appium (if you haven't used it):
"I haven't had hands-on production experience with Appium yet, but I understand the
architecture — it extends WebDriver for mobile, uses UIAutomator2 for Android and
XCUITest for iOS. I've reviewed desired capabilities and the core mobile locator strategies.
I'm actively building experience with it and confident I can get productive quickly
given my Java and WebDriver background."

Gauge framework (if you haven't used it):
"I've worked with BDD concepts and Cucumber. Gauge is similar — specs written in Markdown,
step implementations in Java. The core BDD principles are the same — readable scenarios,
Given/When/Then flow. I'd need a sprint to get fully comfortable with Gauge's specific
tooling, but the mindset is familiar."
```

### The honest senior formula:
```
1. Say what you DO know (related knowledge)
2. Acknowledge the gap honestly
3. Show you've done some research
4. Express confidence you can learn fast
```

**Never bluff.** Senior interviewers will ask one follow-up question that exposes bluffing immediately.

---

## QUICK REFERENCE — Commands

```bash
# Maven / Java
mvn test                              # run all tests
mvn test -Dtest=ClassName             # run one class
mvn test -Dgroups=smoke               # run by group
mvn clean test                        # clean then run
mvn dependency:tree                   # show dependency tree

# Playwright / Node
npx playwright test                   # run all tests
npx playwright test tests/api/        # run folder
npx playwright test --grep @smoke     # run by tag
npx playwright show-report            # view HTML report

# Git
git status                            # see what changed
git add .                             # stage all changes
git commit -m "message"               # commit
git push origin main                  # push to GitHub
git pull origin main                  # pull latest

# PowerShell output filtering
mvn test 2>&1 | Select-String "Tests run|BUILD|FAIL"
```

---

---

## 16. Your CV — Personalized Talking Points & STAR Answers

> These are built directly from your actual CV (Kupeshanth Kupenthiran).
> Learn these cold — they are YOUR stories, told well.

---

### Your Experience Summary (say this when asked "Tell me about yourself")

```
"I'm a Trainee Quality Engineer currently at Qoria Lanka, working on the EdTech
Insight project. I write Selenium and TestNG automation in Java, Playwright UI tests
in JavaScript, and I've been working on MCP and Agent integrations as part of
AI-augmented testing workflows. Our tests run through GitHub Actions pipelines with
GCP-triggered executions for deployment validation.

Before Qoria, I worked at Cerexio as an Intern Software Engineer and QA Engineer
for six months — manual and automated testing of Angular PrimeNG enterprise applications,
API testing with Postman, defect tracking in JIRA, and Agile collaboration with
development teams.

I have a B.Sc. Honours in Information Technology and Management from the University
of Moratuwa. My final year project is an AI-powered Diabetic Retinopathy Detection
System — Python, Machine Learning, NLP, and React — which shaped how I think about
non-binary quality metrics beyond simple pass/fail.

I'm looking for a role where I can deepen my automation craft and contribute to
quality from the beginning of the feature lifecycle, not just at the end."
```

---

### Framing the Experience Gap Honestly

The JD asks for 3+ years. You have ~1.5 years combined. Here's how to address it if asked:

```
"My total professional QA experience is about 1.5 years — an internship at Cerexio
(March to September 2024) and my current role at Qoria Lanka (September 2025 to present).
That said, the quality of the exposure has been dense: I moved from manual testing to
building automation frameworks with Selenium, TestNG, and Playwright, integrating them
into GitHub Actions CI/CD pipelines, and working on MCP and Agent integrations that
most QA engineers at any experience level haven't had yet.

I'm aware this role asks for 3+ years and I want to be transparent about where I am.
What I can offer is strong fundamentals, hands-on automation experience on real shipped
products, and the ability to learn fast. I've built frameworks rather than just used
them — which means I understand the architecture, not just the scripts.

I'd rather be honest and let my work speak than overstate my years."
```

---

### STAR Stories From Your Actual Experience

---

**Q: Tell me about an automation framework you built.**

```
Situation: In my Singer Page Testing project, there was no automation — all testing
was manual. Regression took significant time before any release.

Task: Build a maintainable automation framework that non-technical stakeholders
could understand — reports readable without reading code.

Action: I chose Serenity BDD with Cucumber and Java. Feature files in Gherkin
(readable by anyone), step definitions mapping Gherkin to Selenium WebDriver actions,
page objects for each key page of the Singer website. Serenity auto-generates HTML
reports with screenshots at each step — product owners can read these without
opening code. I documented the setup so the suite ran with a single Maven command.

A challenge: Singer's navigation menu was JavaScript-rendered. My initial CSS
selectors broke in different load states. I switched to XPath targeting text content
and added explicit waits — made the tests stable.

Result: Core user journeys automated. Non-technical stakeholders could verify
coverage from Serenity reports. My first complete framework build — it gave me
confidence in making architecture decisions independently.
```

---

**Q: Tell me about your API testing experience.**

```
Situation: At Cerexio (March–September 2024), I tested Angular PrimeNG enterprise
applications with REST API backends. When I joined, there was no structured API
testing — developers tested endpoints manually and bugs were often found late.

Task: As the QA intern, build API test coverage for core endpoints — authentication,
CRUD operations, and reporting.

Action: I used Postman — organized collections per API module, JavaScript test
scripts validating status codes, response body fields, and data types. Set up
Postman environments for development and staging so the same collection ran
against both with one variable swap. Standard practice: negative test cases for
every endpoint — missing fields, invalid data, unauthorized requests.

Result: We caught API bugs before they reached the UI layer, reducing frontend
debugging time. The Postman collections became the team's shared API documentation.
I handed over a runnable, documented API test suite when I moved on.

[If asked about RestAssured]:
"I've been building Java RestAssured automation to complement my Postman experience —
structured test classes with RequestSpecification, DataProviders, and JSON Schema
validation. Happy to discuss the architecture."
```

---

**Q: Tell me about your BDD / Cucumber experience.**

```
Situation: Singer Page Testing project — building automated tests for the Singer
website with no existing automation framework.

Task: Create tests that non-technical stakeholders could read and verify — not just
test scripts readable only by developers.

Action: Implemented Serenity BDD with Cucumber. Wrote feature files in Gherkin
(Given/When/Then) that anyone on the team could read. Implemented step definitions
in Java using Selenium WebDriver. Used Serenity's living documentation reporting —
after each run, HTML reports show which scenarios passed and what was covered,
against the feature language, not the code language.

Result: Readable test suite that covered core user journeys. Serenity reports
made test results accessible to non-developers. I now understand the full
three-layer BDD structure — feature files, step definitions, page objects — not
just the theory.
```

---

**Q: Tell me about your CI/CD experience.**

```
Situation: At Qoria Lanka, the Playwright test suite was being run manually before
releases — not integrated into the delivery pipeline. Feedback was slow: PR merged,
then QA found issues after the fact.

Task: Integrate the test suite into GitHub Actions so tests ran automatically on
every pull request. Also coordinate with GCP-triggered pipelines for deployment
validation.

Action: Worked on GitHub Actions workflow YAML — install Node.js and dependencies,
install Playwright browsers, run tests headless against a staging deployment, upload
test artifacts on failure. For GCP-triggered executions: tests run automatically
when a new build is deployed to the cloud environment, validating the deployment
before it progresses further. Critical test failures block PR merges.

Result: PRs now trigger automated test runs. Developers see results on GitHub
before code review completes. We caught regressions in the first month that
would previously have reached staging. Feedback loop went from end-of-sprint
to same-day.
```

---

**Q: Tell me about your MCP / Agent integration experience.**

```
Situation: At Qoria Lanka, the team began exploring AI-augmented testing workflows
as AI tooling matured. Traditional automation handles repetitive regression, but
there's a gap in intelligent test generation and defect pattern analysis.

Task: Contribute to integrating MCP and Agent tools into our testing workflow.

Action: I worked on connecting Claude-based agents via MCP (Model Context Protocol)
to our testing pipeline. This involved configuring agent integrations that could
interpret test results, assist with test case generation from acceptance criteria,
and surface patterns in defect reports. Working with MCP requires understanding
how to structure agent inputs and outputs, handle context windows, and integrate
AI responses into workflow decisions — which is different from scripted automation.

Result: The team gained a working AI-augmented layer on top of traditional
automation. I gained experience in a frontier area of QA tooling that most
engineers — at any experience level — haven't worked with yet. It's also shaped
how I think about where automation is heading over the next few years.

[Why mention it]: This differentiates you. Most QA candidates can talk about
Selenium and Playwright. Very few can speak credibly about MCP integrations.
```

---

**Q: Tell me about a time you worked in an Agile team.**

```
Situation: Both at Cerexio and Qoria, teams worked in Agile two-week sprints.
I came from university where testing was a phase after development — in my
first sprint at Cerexio I quickly realized QA was expected from the very start.

Task: Learn how to contribute meaningfully to each ceremony and shift from
"testing at the end" to "quality throughout the sprint."

Action: In sprint planning, I reviewed user stories for testability — "what
happens when the user enters invalid data?" or "how do we define done here?"
These questions either clarified ambiguous requirements or flagged gaps before
development started. In backlog grooming at Cerexio, I contributed to writing
acceptance criteria alongside the product owner. In retrospectives at Qoria, I
raised observations about where bugs were being found late.

Result: At Qoria, I became the person who spots acceptance criteria gaps in
sprint planning. The team found this valuable — it reduced "this needs
clarification" comments during actual testing and reduced rework.
```

---

**Q: Tell me about a significant bug you found.**

```
Situation: During regression at Qoria for an EdTech Insight sprint release, I
was testing a report export feature. School administrators use this to export
student activity reports as CSV files for a selected date range.

Task: Run regression tests before release. The feature had passed developer
testing on the happy path.

Action: During exploratory testing I tested a date range crossing a month
boundary — e.g. January 28 to February 4. Happy path (within a single month)
worked correctly. But crossing a month boundary silently dropped the second
month's data from the CSV. No error message. The file downloaded with a 200
status code. A school administrator would never know the data was incomplete.

I documented with exact reproduction steps, the specific dates, a screenshot
comparing in-app data versus CSV content, and expected versus actual record
counts. Flagged as high severity — silent data integrity failure.

Result: Developer found an off-by-one error in the SQL BETWEEN clause.
Fixed before release. Product owner thanked me specifically — data accuracy
is critical for schools trusting the platform. I added cross-month-boundary
tests to the regression suite permanently.
```

---

**Q: Tell me how the Diabetic Retinopathy project relates to your QA thinking.**

```
Situation: My university final year project (B.Sc. IT & Management, University
of Moratuwa) — an AI-powered Diabetic Retinopathy Detection System. Includes
an AI chatbot, Python ML model for retinal image classification, NLP for natural
language interactions, and a React.js frontend.

What it taught me about quality:
- Testing ML models is fundamentally different from testing deterministic software.
  Accuracy, precision, recall, and F1-score are quality metrics — a test can
  technically "pass" while the model is dangerously wrong for certain inputs.
- Testing an NLP chatbot means thinking about input variation, edge case prompts,
  and output relevance — not just status codes and response bodies.
- False positives vs false negatives in ML directly maps to risk thinking in
  software testing: what's the cost of a missed defect versus a false alarm?

How to frame it:
"This project showed me that 'quality' means different things for different
systems. In traditional software, a passing test means the system behaved
as specified. In ML, a passing test means the model output was within acceptable
probability ranges — which is a more nuanced and interesting quality problem.
I bring that thinking to how I assess risk in any software system I test."
```

---

### Your Projects — How to Talk About Each

**Singer Page Testing Application (Serenity + Cucumber + BDD + Java + Selenium WebDriver)**
```
"I built a full BDD test suite from scratch using Serenity and Cucumber — feature
files in Gherkin, step definitions in Java with Selenium WebDriver. This gave me
hands-on BDD experience, not just theory. Serenity's living documentation reports
mean stakeholders can see what was tested without reading code. The hardest part
was handling Singer's JS-rendered navigation menu — I had to switch from CSS
selectors to XPath targeting text content and add explicit waits to get stable tests."
```

**Automation Project Using Python and Pytest (Selenium + Pytest + Python)**
```
"I built an automated test framework in Python using Pytest and Selenium. This
expanded me beyond Java — I can work in both Python and Java test stacks.
Pytest's fixture model is similar to TestNG's Before/After setup, and parametrize
is the equivalent of DataProvider. Learning fixture scope (session vs function vs
class) was the key technical lesson that made tests properly isolated."
```

**UI Automation Project Using Playwright (Playwright + JavaScript + Node.js)**
```
"I built Playwright automation in JavaScript — Page Object pattern, async/await
handling, Playwright's built-in expect API for assertions, and HTML reports for
debugging failures. This personal project is what prepared me to use Playwright
professionally at Qoria. Understanding Playwright's browser context model — how
each test gets an isolated context — matters for state management between tests."
```

**Qoria EdTech Insight — Current Role (Selenium + TestNG + Playwright + GitHub Actions + GCP + MCP/Agents)**
```
"My current professional work. I write Selenium+TestNG automation in Java and
Playwright tests in JavaScript. The tests run in GitHub Actions on every PR and
through GCP-triggered pipelines for deployment validation. I've also been working
on MCP and Agent integrations — connecting Claude-based AI agents to testing
workflows for test generation and defect pattern analysis. This is frontier work
in QA tooling — something I'm genuinely excited about."
```

**Cerexio — Internship (Angular PrimeNG + Postman + JIRA + Agile)**
```
"Six months of professional QA on an enterprise Angular PrimeNG application.
I did manual and automated testing, API testing with Postman across all endpoints,
defect reporting in JIRA, and participated fully in Agile ceremonies. The Angular
PrimeNG UI had complex custom dropdowns and data tables — testing these required
understanding component state, not just static HTML. This internship is where I
built my API testing discipline and defect reporting rigour."
```

**Diabetic Retinopathy Detection System (AI/ML — Python + NLP + React.js + ML)**
```
"My final year project is an AI chatbot system for diabetic retinopathy. Python
ML model for retinal image classification, NLP for the chatbot interaction layer,
React.js frontend. What this demonstrates from a QA perspective: I understand
how ML systems fail differently from deterministic software. That shapes how I
approach risk in any system I test — especially AI-driven features."
```

---

### Skills You Should Highlight — With Correct Evidence

| Skill | Your Actual Evidence | Confidence to Claim |
|-------|---------------------|---------------------|
| Selenium + Java | Qoria (professional), Singer project (personal) | High |
| TestNG | Qoria daily use — groups, @Before/@After, parallel | High |
| Playwright + JavaScript | Qoria (professional), personal Playwright project | High |
| Python | Python+Pytest project, Diabetic Retinopathy ML/NLP | High |
| API testing (Postman) | Cerexio — 6 months on enterprise app | High |
| BDD / Cucumber | Singer — built Serenity+Cucumber framework from scratch | High |
| JIRA | Cerexio — daily defect tracking and sprint management | High |
| GitHub Actions | Qoria — configured PR pipelines in YAML | Medium |
| GCP Test Pipelines | Qoria — deployment-triggered test executions | Medium |
| MCP / Agent Integration | Qoria — Claude-based agent integrations (active work) | Medium |
| Serenity BDD | Singer project — full framework build and reports | Medium |
| Pytest | Python+Pytest project — fixtures, parametrize, conftest | Medium |
| Angular (testing it) | Cerexio — tested Angular PrimeNG app | Medium |
| React.js | Diabetic Retinopathy project frontend | Medium |
| RestAssured | Learning/study level — fluent API understood | Honest — say "actively building" |
| JMeter / Performance | Listed in CV — clarify depth if asked | Honest |
| Cypress | Listed in CV — understand architecture | Honest |
| Jenkins | CI/CD concepts, not hands-on in production | Honest |
| Appium | Conceptual knowledge only | Honest — study concepts, don't bluff |
| Gauge | Read docs, not used in a project | Honest — relate to Cucumber |

**Note on Jenkins:** At Qoria you use GitHub Actions, not Jenkins. Don't claim Jenkins as professional experience — you haven't used it in production.

---

### Questions YOU Should Ask in the Interview

```
1. "How does QA fit into the development cycle here — are testers involved from
   sprint planning or brought in once code is complete?"

2. "What does the automation coverage look like today — what's the biggest gap
   the team wants to close in the next 6 months?"

3. "I've been working on MCP and Agent integrations at Qoria — is AI-augmented
   testing something this team is exploring, or is the focus on traditional
   automation for now?"

4. "What would a successful first 3 months look like in this role?"

5. "What is the biggest quality challenge the team is facing right now —
   not in the future, but today?"
```

These questions show you think like a senior even if your years don't yet. Question 3 in particular opens a conversation where your actual current work at Qoria is directly relevant.

---

*Preparation Guide v2.0 — Senior QA Engineer Interview | Personalized for Kupeshanth Kupenthiran*
*Roles: Trainee QE @ Qoria Lanka (Sep 2025–Present) | Intern SE & QA @ Cerexio (Mar–Sep 2024)*
*Tools: Selenium + TestNG + Playwright + Cucumber/Serenity + GitHub Actions + GCP + MCP/Agents + Postman + JIRA*
