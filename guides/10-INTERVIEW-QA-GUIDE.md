# Senior QA Engineer Interview — Complete Q&A Guide | Personalized for Kupeshanth Kupenthiran

---

## Table of Contents

1. [Section 1 — Behavioral Questions (STAR Format)](#section-1--behavioral-questions-star-format)
2. [Section 2 — Technical Q&A (10 Questions)](#section-2--technical-qa-10-questions)
3. [Section 3 — Framework Design](#section-3--framework-design)
4. [Section 4 — Live Scenarios](#section-4--live-scenarios)
5. [Section 5 — Questions to Ask the Interviewer](#section-5--questions-to-ask-the-interviewer)
6. [Section 6 — What to Say When You Don't Know](#section-6--what-to-say-when-you-dont-know)

---

## Section 1 — Behavioral Questions (STAR Format)

> STAR = **S**ituation, **T**ask, **A**ction, **R**esult. Every story must be concrete, first-person, and end with a measurable or observable outcome.

---

### Q1: Tell me about yourself

"I am a QA Engineer with around 18 months of commercial experience, currently a QA Trainee at Qoria Lanka, where I work on a B2B SaaS product used by schools across multiple countries. Before that I completed an internship at Cerexio, a data analytics company, where I worked on API testing for an IoT platform.

My core strengths are UI test automation — I built a Serenity BDD + Cucumber framework for Qoria's Singer Page project using Java and Selenium — and API testing, which I practised heavily with Postman at Cerexio and I am now building skills in RestAssured. I have also set up GitHub Actions CI pipelines at Qoria and have experience working inside an Agile Scrum team, attending sprint planning, refinement, and daily stand-ups.

I am seeking a Senior QA Engineer role where I can apply and deepen my automation skills, take ownership of the test strategy for a product, and mentor others. My long-term goal is to lead QA at a product company that takes quality seriously."

---

### Q2: Describe an automation framework you built from scratch

**Situation**: Qoria's Singer Page was a newly developed web module. There was no existing automation. The team was manually testing 40+ UI scenarios every sprint, which was taking 2 days and was prone to human error.

**Task**: I was asked to design and implement an end-to-end UI automation framework that could be run in CI and reduce regression time.

**Action**:
- I chose Serenity BDD with Cucumber (Java) because it produces living documentation and the Gherkin scenarios are readable by non-technical stakeholders
- I set up a Maven project with the standard Serenity + Cucumber + Selenium WebDriver stack
- I implemented the Page Object Model pattern — each page was a separate class with WebElements and interaction methods, keeping test logic out of step definitions
- I wrote Cucumber feature files covering the full regression scope — 40+ scenarios across login, form submission, data validation, and navigation
- I configured the framework to run headless in GitHub Actions with Chromium
- I integrated Serenity's HTML reporting so the pipeline published a rich visual report after each run

**Result**: Regression time dropped from 2 manual days to approximately 25 minutes of automated execution. The framework runs automatically on every push to the develop branch. Stakeholders can review the Serenity report without asking QA for status. The framework has been adopted as the standard for future modules.

---

### Q3: Tell me about your API testing experience

**Situation**: During my internship at Cerexio I was assigned to test the REST APIs for an IoT data ingestion platform. There were around 30 endpoints across 5 services, and no existing API test suite.

**Task**: Build a test collection that covered all endpoints, automated where possible, and documented the expected behaviour for future QA reference.

**Action**:
- I used Postman to create a structured collection mirroring the API architecture — one folder per service
- For each endpoint I tested: valid input with 200 response, missing required fields, invalid data types, boundary values, unauthorised access (no token and expired token), and not-found scenarios
- I wrote Postman test scripts (JavaScript) to assert status codes, response body schema, and specific field values
- I set up environment variables so the collection could switch between dev and staging without changing URLs
- I ran the collection with Newman (Postman CLI) so it could be run in a script
- I documented findings in JIRA tickets with full reproduction steps, expected vs actual responses, and screenshots from Postman

**Result**: I documented and tested all 30 endpoints within 3 weeks. I found 12 defects, 4 of which were rated High severity — including one endpoint that returned 200 on a missing required field instead of 400. The Newman-based run was later integrated into the build pipeline. Since then I have been learning RestAssured to bring the same approach into a Java codebase.

---

### Q4: Tell me about your CI/CD experience

**Situation**: At Qoria, the Singer Page automation framework I built was running only locally. Developers would have to ask me to run it — which defeated the purpose of automation.

**Task**: Integrate the framework into Qoria's GitHub Actions CI pipeline so tests run automatically on every push.

**Action**:
- I created a `.github/workflows/regression.yml` file in the repository
- I configured the workflow to trigger on push to `develop` and on pull requests to `main`
- I used the `actions/setup-java` action to set up JDK 17, and configured Maven dependency caching to speed up subsequent runs
- I added headless Chrome flags to the configuration because the CI agents don't have a display
- I set up the workflow to upload the Serenity HTML report as an artifact on every run (including failures)
- Qoria uses GCP for its infrastructure; the test environment URL was configured as a GitHub Actions secret, not hardcoded
- I added a notification step that posts to the team Slack channel if the workflow fails

**Result**: Tests now run automatically on every pull request. The pipeline catches regressions before they reach the develop branch. The team spends less time manually running tests and the Serenity report is available to all stakeholders from the GitHub Actions tab.

---

### Q5: Tell me about working in Agile

**Situation**: Both Qoria and Cerexio run 2-week Agile Sprints with daily stand-ups, sprint planning, and retrospectives.

**Task** / **Context**: My role as QA in Agile is to be involved from the beginning of the sprint — not just at the end when code is ready to test.

**Action at Qoria**:
- I attend sprint refinement to review user stories before they are committed to a sprint. I raise questions about acceptance criteria that are ambiguous, missing edge cases, or untestable.
- I participate in Three Amigos sessions with the developer and BA before implementation starts. I challenge stories where we have not agreed on error handling or validation behaviour.
- Once development begins I write test cases in Zephyr/JIRA so I am not writing them under pressure at the end of the sprint.
- I sign off stories early in the testing window so defects can be fixed within the same sprint.
- In retrospectives I raise issues like "we received the build 2 days before sprint end — QA had no time to test properly" and propose process changes like feature flags or earlier merges.

**Result**: Over 3 sprints I tracked our defect escape rate (defects found in production vs test). By increasing my involvement in refinement the escape rate dropped from around 15% to 5% because I was catching issues in requirements before code was written.

---

### Q6: How do you handle testing when requirements are unclear?

**Situation**: At Qoria, a new filtering feature was added to the Singer Page. The story said "users can filter singers by genre" but had no detail on multi-select behaviour, case sensitivity, empty results, or maximum selections.

**Task**: I needed to test this without waiting for requirements to be clarified over days of email chains.

**Action**:
- I listed all my assumptions and questions in a comment on the JIRA ticket: "Does multi-select use AND or OR logic? What happens with 0 results? Is search case-sensitive?"
- I requested a quick 15-minute clarification call with the BA and developer — got answers in one session
- I used the behaviour of a similar existing filter (Country filter) as a reference for anything not answered
- I wrote test cases based on the answers and explicitly noted which ones were based on my interpretation
- I flagged any test case that had an unclear expected result as "Pending Clarification" in JIRA and shared the list with the BA before I started executing

**Result**: The feature was tested completely in one sprint without defects escaping. Two test cases I had marked "pending clarification" were updated when the BA confirmed the expected behaviour was different from my assumption — those would have been bugs or wrong test verdicts without that process.

---

### Q7: Tell me about a time you found a critical bug

**Situation**: During regression testing of Qoria's Student Management module, I was running the data export feature as part of my standard test suite.

**Task**: My job was to verify data export worked correctly with the new user permission roles introduced in that sprint.

**Action**:
- While testing the "School Admin" role (limited permissions), I discovered that the export function was returning data for ALL schools in the system — not just the admin's own school
- I verified this was not a test data issue by creating two fresh accounts in two different school organisations and confirming both were visible in the export
- I documented the defect immediately: severity Critical, Priority Critical. I included the exact steps, two Postman API traces showing the raw response, a screenshot of the downloaded CSV, and tagged the developer

**Result**: The defect was a missing filter clause on the backend query — applying to schools the current user did not belong to. Had this reached production, any school admin could have downloaded student data from other organisations — a serious data protection breach (GDPR risk). The sprint was extended by 2 days to fix and re-test. The bug was caught 6 days before the scheduled release.

---

### Q8: Tell me about a time you improved a process

**Situation**: At Qoria, the test execution was done manually via a shared Excel sheet that multiple QA team members updated. It was slow, caused merge conflicts, and did not integrate with our JIRA workflow.

**Task**: I identified this as a bottleneck and proposed migrating to Zephyr Scale (JIRA plugin) for test case management.

**Action**:
- I built a business case: I tracked how much time per sprint was lost to Excel conflicts and reported it in a retrospective
- I demoed Zephyr Scale in one sprint for my module only — showing how test cases linked to JIRA stories, execution was tracked per cycle, and reports were generated automatically
- I wrote a migration guide for converting our Excel test cases to Zephyr format
- I ran a knowledge-sharing session for the other QA team members

**Result**: The team adopted Zephyr Scale for all modules in the following quarter. Sprint report generation time dropped from 2 hours (compiling Excel data) to 10 minutes (Zephyr auto-report). Our JIRA traceability (story → test case → defect) became complete, which also helped in client-facing audit reviews.

---

### Q9: How did you handle a disagreement with a developer over a bug?

**Situation**: I raised a defect on Qoria's Singer Page: the "Save" button remained enabled even when a required field was empty, allowing form submission with missing data. The developer marked it "Rejected — works as designed" without explanation.

**Task**: I believed this was a genuine defect that would cause bad data in the database and a poor user experience.

**Action**:
- Rather than escalating immediately, I responded on the ticket calmly: "I'd like to understand the design decision here — can you share where this is documented or what the intended behaviour is?"
- The developer replied that the backend would reject the empty submission with a 400 error. I acknowledged that, but pointed out that the UX requirement in the story said "form fields should be validated in real-time and the Save button should only be active when all required fields are filled" — I pasted the exact acceptance criteria
- I also checked two similar forms in the same product that both disabled the Save button until required fields were filled, for consistency
- I brought it to the next sprint review with both the developer and BA present, presenting the acceptance criteria and the UX inconsistency

**Result**: The BA confirmed the story's intent matched my interpretation. The developer acknowledged they had missed the AC. The fix was made that sprint. I followed up by suggesting that AC be reviewed together at sprint refinement so these misunderstandings are caught earlier — which became part of our Three Amigos process.

---

### Q10: How do you frame having 1.5 years of experience when a role asks for 3+?

"I understand the job spec asks for 3 or more years and I want to be transparent about where I am in my career. I have 18 months of commercial experience — but those 18 months have been very focused: I built an automation framework from scratch using Serenity BDD and Cucumber, integrated it into CI with GitHub Actions, handled real API testing at Cerexio, and worked inside a shipping Agile team at Qoria.

I have deliberately invested in building depth — I have studied ISTQB concepts thoroughly, I am comfortable with Selenium, Playwright, Postman, RestAssured, TestNG, and Maven, and I understand CI/CD pipelines. What I may lack is breadth — I haven't yet managed a full QA team, led a performance testing engagement, or worked across 10 different products.

I am applying because I believe the role's core requirements match my current skills. I am a fast learner and have demonstrated that by going from intern to building production-used automation within months. If you are looking for someone who will hit the ground running on automation and API testing and grow rapidly into the full scope of the role, I believe I can do that."

---

## Section 2 — Technical Q&A (10 Questions)

---

### Q1: What is the WebDriver architecture?

WebDriver follows a client-server architecture. Your test code (Java, Python, JS) acts as the **client**. It sends HTTP commands in the W3C WebDriver Protocol format to a **WebDriver server** (ChromeDriver, GeckoDriver, EdgeDriver). The WebDriver server translates those commands into browser-native instructions and controls the actual browser.

```
Test Code (Java)
      ↓  HTTP (W3C WebDriver protocol)
WebDriver Server (ChromeDriver)
      ↓  Native commands
Chrome Browser
      ↓  DOM / JavaScript
Web Page
```

**Selenium 4** moved to the W3C standard fully, removing the need for browser-specific JSONWireProtocol hacks. ChromeDriver must be the same major version as the installed Chrome. **WebDriverManager** (or Selenium Manager built into Selenium 4) handles this automatically.

**Key Selenium Manager note (Selenium 4.6+)**: You no longer need to manually download ChromeDriver. Selenium Manager detects your Chrome version and downloads the matching driver automatically.

```java
// Selenium 4 — Selenium Manager handles the driver automatically
WebDriver driver = new ChromeDriver();

// Explicit WebDriverManager (fallback or older versions)
WebDriverManager.chromedriver().setup();
WebDriver driver = new ChromeDriver();
```

---

### Q2: What is the Page Object Model and why does it matter?

POM is a design pattern where each UI page (or component) in your application is represented by a separate Java class. The class contains:
1. The web element locators (annotated fields)
2. Methods representing actions a user can take on that page

**Why it matters**:
- **Maintainability**: When the UI changes, you update the locator in one place, not in 50 test methods
- **Readability**: Tests read like user actions: `loginPage.enterEmail("user@test.com").clickLogin()`
- **Reusability**: The same page object method can be used by many test classes
- **Separation of concerns**: Tests contain only assertions; page objects contain only interactions

```java
// Page Object
public class LoginPage {
    private WebDriver driver;

    @FindBy(id = "email")
    private WebElement emailField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    @FindBy(css = ".error-message")
    private WebElement errorMessage;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public DashboardPage loginAs(String email, String password) {
        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
        return new DashboardPage(driver);
    }

    public String getErrorMessage() {
        return errorMessage.getText();
    }
}

// Test class — clean, no locators, no driver calls
@Test
public void validLoginRedirectsToDashboard() {
    LoginPage loginPage = new LoginPage(driver);
    DashboardPage dashboard = loginPage.loginAs("user@test.com", "Password123");
    assertTrue(dashboard.isWelcomeMessageVisible());
}
```

---

### Q3: What is the difference between explicit and implicit waits? Which should you use?

**Implicit Wait**: Tells WebDriver to poll the DOM for a specified time when trying to find an element. Set once and applies globally for the lifetime of the driver.

```java
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
```

Problem: It applies to every `findElement` call, making tests slower than necessary. It also interacts unpredictably with `ExpectedConditions`, sometimes causing longer waits than expected.

**Explicit Wait**: Waits for a specific condition to be true before continuing. Set per element, per condition.

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

// Wait until visible
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));

// Wait until clickable
wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.submit")));

// Wait until text is present
wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("status"), "Submitted"));

// Custom condition
wait.until(driver -> driver.findElement(By.id("count")).getText().equals("10"));
```

**Fluent Wait**: Explicit wait with configurable polling interval and ignored exceptions.

```java
Wait<WebDriver> wait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(30))
    .pollingEvery(Duration.ofMillis(500))
    .ignoring(NoSuchElementException.class);

wait.until(d -> d.findElement(By.id("status")).getText().contains("Done"));
```

**Rule**: Never use `Thread.sleep()`. Prefer explicit waits. Use implicit wait only as a safety net set to a low value (3–5 seconds), never as the primary wait strategy.

---

### Q4: What is BDD and how is it different from TDD?

**TDD (Test-Driven Development)**:
- Developers write a failing unit test first
- Then write just enough code to make it pass
- Then refactor
- Cycle: Red → Green → Refactor
- Focus: Code correctness at the unit level
- Language: Code (JUnit, NUnit, etc.)
- Audience: Developers only

**BDD (Behaviour-Driven Development)**:
- Team (BA + Dev + QA) write scenarios in Gherkin (plain English) before code is written
- Scenarios describe business behaviour from the user's perspective
- Scenarios become automated acceptance tests
- Focus: Business value and user behaviour
- Language: Gherkin (Given/When/Then)
- Audience: Everyone — business, dev, QA

```gherkin
# BDD scenario — readable by anyone
Scenario: User cannot submit a form with a missing required field
  Given the user is on the registration form
  When the user leaves the "Email" field empty
  And the user clicks "Register"
  Then the "Email" field should show "Email is required"
  And the form should not be submitted
```

BDD is not just about test automation — it is primarily about communication. The Gherkin scenarios become the shared language between all stakeholders and eliminate ambiguity in requirements.

**In your experience**: The Singer Page BDD project at Qoria used Serenity + Cucumber. Feature files were reviewed by the BA. The step definitions called Page Object methods. Running the suite produced a Serenity Living Documentation report that the whole team could read.

---

### Q5: What is your REST API testing strategy?

A complete API test strategy covers:

**1. Contract / Schema Testing**
- Verify every field in the response exists and has the right type
- Use JSON Schema validation in RestAssured or Postman's schema validation
- Prevents consumer-side breaks when the API team changes a field name

**2. Functional Testing (Happy Path)**
- Every endpoint returns 2xx for valid requests
- Response body contains expected data
- CRUD lifecycle: POST creates → GET returns it → PUT updates → GET verifies → DELETE removes → GET returns 404

**3. Negative / Error Testing**
- 400 for missing required fields
- 400 for invalid data types (send a string where number expected)
- 401 for missing/expired token
- 403 for valid token but insufficient permissions
- 404 for non-existent resource
- 405 for wrong HTTP method (PUT on a GET-only endpoint)
- 422 for semantically invalid data (dates in the past when future is required)

**4. Boundary Value Testing**
- String fields at max length and max+1
- Numeric fields at boundaries
- Empty arrays, empty strings, null values

**5. Authentication and Authorization**
- Test with no token → 401
- Test with expired token → 401
- Test with valid token but wrong role → 403
- Test that User A cannot access User B's data

**6. Performance**
- Response time under load
- Establish baseline response times for critical endpoints
- API should respond under 2 seconds at normal load

**7. Integration**
- End-to-end flows that exercise multiple endpoints in sequence
- Example: Create order → Process payment → Check order status → Cancel → Verify refund

---

### Q6: How do you approach testing a new API with no documentation?

**Step 1: Gather available information**
- Ask the developer for a Swagger/OpenAPI spec — even a rough one
- Check the codebase for route definitions or controller annotations
- Look at the frontend code to see what API calls it makes (Browser DevTools → Network tab)
- Review existing Postman collections or any wiki pages

**Step 2: Explore with a REST client**
- Start with the base URL; try `/api/docs`, `/swagger-ui`, `/openapi.json` — many APIs expose self-documentation
- Make a GET to the root path and see what the error response structure looks like — it reveals a lot

**Step 3: Map the endpoints**
- Group by resource: `/users`, `/orders`, `/products`
- Note which HTTP methods work on each
- Log every request and response in Postman

**Step 4: Infer the contract**
- Send valid requests and document what comes back
- Note required vs optional fields
- Try sending extra fields — does the API ignore them or reject them?
- Try sending wrong types — what error format does it return?

**Step 5: Build a test collection**
- Start with happy paths
- Add negative tests based on what the API actually validates
- Add schema validation so future responses are compared against the established contract

**Step 6: Confirm with developer**
- List your assumptions and validate them
- Ask about any hidden business rules (e.g. "orders can only be deleted within 24 hours")

---

### Q7: What is the difference between a 401 and a 403?

| | 401 Unauthorized | 403 Forbidden |
|---|---|---|
| **Meaning** | Not authenticated — the server does not know who you are | Not authorised — the server knows who you are but you don't have permission |
| **When returned** | Missing token, expired token, invalid token | Valid token but insufficient role/scope |
| **Example** | Calling an API with no `Authorization` header | A regular user trying to access an admin-only endpoint |
| **What to do** | Re-authenticate (log in again, refresh token) | Request elevated permissions; you are correctly blocked |
| **Fix** | Send valid credentials | Nothing — the authenticated user simply doesn't have access |

**Memory trick**: 401 = "I don't know who you are." 403 = "I know exactly who you are and the answer is no."

**In API tests you write**:
- 401 test: Remove the `Authorization` header entirely → expect 401
- 401 test: Send an expired JWT → expect 401
- 403 test: Use a regular-user token on an admin endpoint → expect 403
- 403 test: User A token to access User B's resource → expect 403

---

### Q8: What is JSON Schema validation?

JSON Schema is a specification for describing the structure of JSON data. You define what fields should exist, their types, whether they are required, and any format constraints. Then you validate your API response against that schema automatically.

**Why it matters**: Without schema validation, you test specific values but miss structural changes. If the API team renames `userId` to `user_id`, your value assertions might still pass (if you don't check that field) but the schema validation catches it immediately.

**In RestAssured**:

```java
// Schema file: src/test/resources/schemas/user-schema.json
/*
{
  "$schema": "http://json-schema.org/draft-07/schema",
  "type": "object",
  "required": ["id", "email", "firstName", "lastName", "role"],
  "properties": {
    "id":        { "type": "integer" },
    "email":     { "type": "string", "format": "email" },
    "firstName": { "type": "string", "minLength": 1 },
    "lastName":  { "type": "string", "minLength": 1 },
    "role":      { "type": "string", "enum": ["ADMIN", "USER", "VIEWER"] },
    "active":    { "type": "boolean" }
  },
  "additionalProperties": false
}
*/

// In your test:
given()
    .header("Authorization", "Bearer " + token)
.when()
    .get("/api/users/1")
.then()
    .statusCode(200)
    .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"));
```

**In Postman (Test Scripts)**:

```javascript
const schema = {
    type: "object",
    required: ["id", "email", "role"],
    properties: {
        id:    { type: "number" },
        email: { type: "string" },
        role:  { type: "string", enum: ["ADMIN", "USER"] }
    }
};

const Ajv = require('ajv');
const ajv = new Ajv();
const validate = ajv.compile(schema);
const body = pm.response.json();

pm.test("Response matches schema", () => {
    pm.expect(validate(body)).to.be.true;
});
```

---

### Q9: How do you handle flaky tests?

A flaky test is one that sometimes passes and sometimes fails without any code change. Flaky tests are dangerous — they erode trust in the test suite and get ignored, defeating the purpose of automation.

**Step 1: Identify and tag flaky tests**
- Add a `@Flaky` tag or mark them in your test management tool
- Track the flakiness rate (% of runs where result differs)
- Never let a flaky test block a CI pipeline without investigation

**Step 2: Diagnose the root cause**

| Cause | Solution |
|---|---|
| Timing issue — element not ready | Replace sleep with explicit wait; use `waitForSelector` (Playwright) or `ExpectedConditions` |
| Test data shared between tests | Give each test its own data; clean up after |
| Race condition — async operations | Wait for the network call to complete before asserting |
| Environment-dependent | Ensure test environment is stable; don't use shared staging data |
| Element position changes | Use more stable locators (ID, data-testid) instead of CSS position-based |
| JavaScript not loaded | Wait for `networkidle` or specific element visibility |
| Random data collision | Use unique data (timestamp-based IDs) |

**Step 3: Fix or quarantine**
- Fix immediately if root cause is clear
- If fix is complex, quarantine the test (move to a separate suite, run nightly instead of on every push)
- Never delete a flaky test until you understand what it was testing

**Step 4: Enable retries (short-term)**
```typescript
// playwright.config.ts
retries: process.env.CI ? 2 : 0,
```

Retries are a band-aid, not a fix. Use them only while you diagnose.

**Step 5: Review monthly**
- Track flaky test count as a metric
- Target: zero flaky tests in the smoke suite; < 2% flakiness in regression

---

### Q10: How do you decide what to automate vs keep manual?

Not everything should be automated. The cost of writing, maintaining, and running an automated test must be less than the cost of manual testing over time.

**Automate when**:
- The test will be run repeatedly (regression, smoke on every commit)
- The test is stable — requirements won't change frequently
- The test is deterministic — same input always produces same output
- The test is time-consuming manually but fast when automated
- API-level or data-level checks — not dependent on visual rendering
- Cross-browser compatibility checks run across multiple browsers

**Keep manual when**:
- Exploratory testing — finding unexpected bugs
- Usability and UX testing — how it feels to use
- Tests for features that change frequently (automating a moving target is expensive)
- Visual regression that requires human judgment
- One-off investigations
- UAT — business stakeholders need to validate themselves
- Accessibility testing that requires screen reader interaction

**The ROI formula**:

```
Automation ROI = (Manual testing cost × Number of runs) - Automation build cost - Maintenance cost

If ROI > 0 → automate
If ROI ≤ 0 → manual is cheaper
```

**Practical rule of thumb**: If a test will run more than 5 times, consider automation. If it changes every sprint, keep it manual until it stabilises.

---

## Section 3 — Framework Design

### Design a Selenium + TestNG Framework from Scratch

**Folder Structure**:

```
my-selenium-framework/
├── src/
│   ├── main/java/
│   │   └── (empty — test code goes in test/)
│   └── test/
│       ├── java/
│       │   ├── base/
│       │   │   └── BaseTest.java          ← WebDriver setup/teardown, common utilities
│       │   ├── pages/
│       │   │   ├── LoginPage.java         ← Page Objects
│       │   │   ├── DashboardPage.java
│       │   │   └── RegistrationPage.java
│       │   ├── tests/
│       │   │   ├── LoginTests.java        ← TestNG test classes
│       │   │   ├── DashboardTests.java
│       │   │   └── RegistrationTests.java
│       │   ├── utils/
│       │   │   ├── ConfigReader.java      ← Read config.properties
│       │   │   ├── WaitUtils.java         ← Reusable wait helpers
│       │   │   ├── ScreenshotUtils.java   ← Capture screenshots
│       │   │   └── TestDataUtils.java     ← Generate/load test data
│       │   └── listeners/
│       │       └── TestListener.java      ← ITestListener for reporting hooks
│       └── resources/
│           ├── config.properties          ← Base URL, credentials, timeouts
│           ├── testng-smoke.xml           ← Smoke suite
│           ├── testng-regression.xml      ← Regression suite
│           └── testdata/
│               └── users.json            ← Test data files
├── pom.xml
└── .github/workflows/test.yml
```

**Key Files with Code**:

```java
// base/BaseTest.java
public class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(@Optional("chrome") String browser) {
        if (browser.equalsIgnoreCase("firefox")) {
            driver = new FirefoxDriver();
        } else {
            ChromeOptions options = new ChromeOptions();
            if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
                options.addArguments("--headless=new", "--no-sandbox",
                                     "--disable-dev-shm-usage", "--window-size=1920,1080");
            }
            driver = new ChromeDriver(options);
        }
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        wait = new WebDriverWait(driver, Duration.ofSeconds(
                Integer.parseInt(ConfigReader.get("wait.timeout", "20"))
        ));
        driver.get(ConfigReader.get("base.url"));
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            ScreenshotUtils.capture(driver, result.getName());
        }
        if (driver != null) {
            driver.quit();
        }
    }
}
```

```java
// utils/ConfigReader.java
public class ConfigReader {
    private static Properties props;

    static {
        props = new Properties();
        String env = System.getProperty("env", "staging");
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config-" + env + ".properties")) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config for env: " + env, e);
        }
    }

    public static String get(String key) {
        return System.getProperty(key, props.getProperty(key));
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return (value != null) ? value : defaultValue;
    }
}
```

```java
// listeners/TestListener.java
public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("[FAILED] " + result.getName());
        // Could also send Slack notification here
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("[PASSED] " + result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("[SKIPPED] " + result.getName() + " — " + result.getThrowable());
    }
}
```

```xml
<!-- testng-smoke.xml -->
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Smoke Suite" parallel="methods" thread-count="4" verbose="1">
    <listeners>
        <listener class-name="listeners.TestListener"/>
    </listeners>
    <test name="Smoke Tests" preserve-order="true">
        <parameter name="browser" value="chrome"/>
        <groups>
            <run>
                <include name="smoke"/>
            </run>
        </groups>
        <classes>
            <class name="tests.LoginTests"/>
            <class name="tests.DashboardTests"/>
        </classes>
    </test>
</suite>
```

---

### Design a REST API Test Framework

**Folder Structure**:

```
api-test-framework/
├── src/
│   └── test/
│       ├── java/
│       │   ├── base/
│       │   │   └── BaseApiTest.java       ← RestAssured base config
│       │   ├── clients/
│       │   │   ├── UserClient.java        ← API client per resource
│       │   │   └── OrderClient.java
│       │   ├── models/
│       │   │   ├── User.java              ← Request/response POJOs
│       │   │   ├── CreateUserRequest.java
│       │   │   └── ApiError.java
│       │   ├── tests/
│       │   │   ├── UserApiTests.java
│       │   │   └── OrderApiTests.java
│       │   └── utils/
│       │       ├── AuthUtils.java         ← Token retrieval
│       │       ├── TestDataFactory.java   ← Build test payloads
│       │       └── SchemaValidator.java
│       └── resources/
│           ├── config.properties
│           └── schemas/
│               └── user-schema.json
├── pom.xml
└── .github/workflows/api-tests.yml
```

**Key Files with Code**:

```java
// base/BaseApiTest.java
public class BaseApiTest {
    protected static String authToken;

    @BeforeSuite
    public static void globalSetup() {
        // Configure RestAssured base settings
        RestAssured.baseURI  = ConfigReader.get("base.url");
        RestAssured.basePath = "/api/v1";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Set default request spec
        RestAssured.requestSpecification = new RequestSpecBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build();

        authToken = AuthUtils.getToken(
            ConfigReader.get("api.username"),
            ConfigReader.get("api.password")
        );
    }

    protected RequestSpecification authenticatedRequest() {
        return given()
            .header("Authorization", "Bearer " + authToken);
    }
}
```

```java
// clients/UserClient.java
public class UserClient extends BaseApiTest {

    public Response createUser(CreateUserRequest request) {
        return authenticatedRequest()
            .body(request)
        .when()
            .post("/users");
    }

    public Response getUserById(int userId) {
        return authenticatedRequest()
        .when()
            .get("/users/" + userId);
    }

    public Response updateUser(int userId, UpdateUserRequest request) {
        return authenticatedRequest()
            .body(request)
        .when()
            .put("/users/" + userId);
    }

    public Response deleteUser(int userId) {
        return authenticatedRequest()
        .when()
            .delete("/users/" + userId);
    }

    public Response getUserWithoutToken() {
        return given()
            .header("Content-Type", "application/json")
        .when()
            .get("/users/1");
    }
}
```

```java
// tests/UserApiTests.java
public class UserApiTests extends BaseApiTest {
    private UserClient userClient = new UserClient();
    private int createdUserId;

    @Test(description = "Create a user with valid data returns 201")
    public void createUserReturns201() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();

        Response response = userClient.createUser(request);

        response.then()
            .statusCode(201)
            .body("email", equalTo(request.getEmail()))
            .body("id", notNullValue())
            .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"));

        createdUserId = response.jsonPath().getInt("id");
    }

    @Test(dependsOnMethods = "createUserReturns201",
          description = "Get created user by ID returns 200")
    public void getCreatedUserReturns200() {
        userClient.getUserById(createdUserId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdUserId));
    }

    @Test(description = "Create user without token returns 401")
    public void createUserWithoutTokenReturns401() {
        userClient.getUserWithoutToken()
            .then()
            .statusCode(401);
    }

    @Test(description = "Create user with missing email returns 400")
    public void createUserMissingEmailReturns400() {
        CreateUserRequest request = TestDataFactory.validCreateUserRequest();
        request.setEmail(null);  // Remove required field

        userClient.createUser(request)
            .then()
            .statusCode(400)
            .body("error", containsString("email"));
    }
}
```

---

## Section 4 — Live Scenarios

### Scenario 1: "Here's an API, test it in 30 minutes"

**Context**: You are given an API (e.g., `POST /api/v1/users`, `GET /api/v1/users/{id}`, etc.) with 30 minutes to test it.

**Step-by-step approach**:

**Minutes 0–5: Understand the API**
- Open Postman; set up the environment with base URL
- Perform a discovery GET: what does the base path return?
- Check for Swagger docs at `/swagger-ui` or `/api-docs`
- Note all available endpoints, methods, and any documented constraints

**Minutes 5–10: Happy path testing**
- POST with fully valid data → verify 201 and response body
- GET the created resource by ID → verify 200 and data matches what was sent
- Note exact response structure — field names and types

**Minutes 10–18: Negative testing**
- Remove each required field one at a time → expect 400 with descriptive error
- Send wrong data types (number field → send string) → expect 400
- Send empty string for required field → expect 400
- Send a non-existent ID on GET → expect 404
- Remove the auth token → expect 401
- If you have a lower-privilege token → test for 403

**Minutes 18–22: Boundary and edge cases**
- String field at max length (if documented) and max+1
- Numeric field at boundary (0, negative, very large number)
- Special characters in string fields (apostrophe, `<script>`, emoji)
- Empty request body `{}` → expect 400
- `null` value for required fields → expect 400

**Minutes 22–27: Document findings**
- Any unexpected status codes (200 where 400 expected = defect)
- Any missing fields in response
- Any inconsistent error format
- Any response time outliers

**Minutes 27–30: Report**
- Summary: X endpoints tested, Y defects found
- List defects with: endpoint, method, input, expected, actual
- Recommend priority for fixes

---

### Scenario 2: "Write test cases for Login" — Complete List of 15 Test Cases

| # | Title | Input | Expected Result | Type |
|---|---|---|---|---|
| TC01 | Successful login with valid credentials | email: `user@test.com`, password: `ValidPass123` | Redirected to dashboard; welcome message shown | Positive |
| TC02 | Login fails with wrong password | email: `user@test.com`, password: `WrongPass` | Error: "Invalid credentials"; stay on login page | Negative |
| TC03 | Login fails with unregistered email | email: `nobody@test.com`, password: `ValidPass123` | Error: "Invalid credentials" (not revealing whether email exists) | Negative/Security |
| TC04 | Login fails with empty email field | email: (empty), password: `ValidPass123` | Error: "Email is required" or equivalent | Negative |
| TC05 | Login fails with empty password field | email: `user@test.com`, password: (empty) | Error: "Password is required" or equivalent | Negative |
| TC06 | Login fails with both fields empty | email: (empty), password: (empty) | Both fields flagged; cannot submit | Negative |
| TC07 | Login fails with invalid email format | email: `notanemail`, password: `ValidPass123` | Error: "Please enter a valid email address" | Negative |
| TC08 | Login button is disabled until both fields filled | Fill email only, then password only | Button remains disabled until both are non-empty | UI/Functional |
| TC09 | Password field masks input | Any password typed | Characters displayed as dots/asterisks | UI/Security |
| TC10 | "Show password" toggle reveals password | Click eye icon | Password becomes readable text | Functional |
| TC11 | "Remember me" checkbox persists session | Tick "Remember me", log in, close browser, reopen | User is still logged in | Functional |
| TC12 | Account locked after multiple failed attempts | Attempt login 5 times with wrong password | Account locked; message shown; login blocked | Security |
| TC13 | "Forgot password" link navigates to reset page | Click "Forgot password" | Reset password form displayed | Functional |
| TC14 | Redirect to originally requested page after login | Try to visit `/dashboard` unauthenticated, log in | Redirected to `/dashboard` after login | Functional |
| TC15 | SQL injection attempt in email field | email: `' OR 1=1 --`, password: anything | Login fails; no SQL error exposed; no unintended access | Security |

---

### Scenario 3: "Test is passing locally, failing in CI" — Debugging Steps

**Step 1: Read the CI log carefully**
- What is the exact error message?
- What stage does the pipeline fail at?
- Is it a compilation error, a test execution error, or an artifact error?

**Step 2: Common causes and checks**

```
Error: SessionNotCreatedException / ChromeDriver version mismatch
→ Fix: Use WebDriverManager or Selenium Manager (auto-manages driver version)
→ Or: Pin Chrome version in Dockerfile/CI runner

Error: no such file or directory (config file, driver, report)
→ Cause: Hardcoded absolute path from your local machine
→ Fix: Use System.getProperty("user.dir") + "/relative/path"
→ Or: Load from classpath: getClass().getClassLoader().getResourceAsStream("config.properties")

Error: TimeoutException — element not found
→ Cause: CI machine is slower; element takes longer to appear
→ Fix: Increase explicit wait timeout: -Dwait.timeout=60
→ Fix: Replace Thread.sleep() with proper explicit wait

Error: DevToolsActivePort file doesn't exist (Chrome crash)
→ Cause: Missing --no-sandbox flag in Docker container
→ Fix: Add --no-sandbox --disable-dev-shm-usage to ChromeOptions

Error: Connection refused / Cannot connect to base.url
→ Cause: APP_ENV or BASE_URL environment variable not set in CI
→ Fix: Add to GitHub Actions env: section or Jenkins environment {} block
→ Debug: Print env variables: sh 'env | sort | grep BASE'

Error: Test passes but CI marks build failed
→ Cause: Surefire/TestNG not generating XML reports at expected path
→ Fix: Verify <reportsDirectory> in pom.xml; verify junit path in CI config

Error: Out of memory / GC overhead limit exceeded
→ Fix: export MAVEN_OPTS="-Xmx1g"
→ Fix: Reduce parallel thread count in testng.xml
```

**Step 3: Reproduce the CI environment locally**

```bash
# Test with headless flag (as CI would run)
mvn test -Dheadless=true -Denv=staging

# Or spin up a Docker container matching CI
docker run --rm -it \
  -e APP_ENV=staging \
  -e BASE_URL=https://staging.myapp.com \
  mcr.microsoft.com/playwright:v1.44.0-jammy \
  npx playwright test
```

**Step 4: Add verbose logging temporarily**

```groovy
// Jenkinsfile — print environment
sh 'env | sort'
sh 'java -version'
sh 'mvn --version'
sh 'google-chrome --version || chromium-browser --version || true'
```

**Step 5: Check CI agent state**
- Is the test environment accessible from the CI network?
- Are secrets/environment variables injected correctly?
- Is there a firewall blocking the base URL?

```bash
# In CI pipeline step — verify connectivity
sh 'curl -I ${BASE_URL}/health'
```

---

## Section 5 — Questions to Ask the Interviewer

These demonstrate strategic thinking and genuine interest. Ask 2–3 per interview.

**Q1: "How does QA involvement fit into your sprint cycle? Are QA engineers included in refinement and planning, or does testing begin after development finishes?"**

Why: Shows you understand shift-left; lets you assess how mature their QA culture is.

**Q2: "What does the current test automation stack look like, and where do you see the biggest gap in coverage right now?"**

Why: Shows you are thinking about contribution from day one; you get to position your skills against their specific gap.

**Q3: "How do you handle the balance between moving fast as a team and maintaining quality? When a release deadline is at risk, what typically gets cut — time or scope?"**

Why: Reveals the real quality culture, not the aspirational one. Their answer tells you a lot about what working there would feel like.

**Q4: "What does a typical onboarding look like for a QA Engineer here — what would I be expected to own in the first 30, 60, and 90 days?"**

Why: Shows initiative and planning. Their answer reveals expectations and whether there is a real onboarding structure.

**Q5: "What are the qualities of the best QA Engineers you have worked with here?"**

Why: Directly tells you what they value most. Adjust your closing pitch to those traits. Also opens a genuine conversation.

---

## Section 6 — What to Say When You Don't Know

### The Honest Formula

Saying "I don't know" is not failure. Saying "I don't know and I don't care" is. Use this structure:

**Template**:
"I haven't worked with [X] directly, so I can't give you a hands-on answer. What I know from studying it is [Y]. I would approach learning it by [Z]. In my current work, I've solved a similar problem with [A]."

This demonstrates: intellectual honesty, background knowledge, learning mindset, and transferable skills.

---

### Example 1: Appium (Mobile Testing)

**If asked**: "Have you done mobile testing with Appium?"

"I haven't used Appium in a commercial project — my automation experience has been on web UI and APIs. I understand Appium conceptually: it uses the WebDriver protocol over a server that translates commands into native mobile gestures for iOS and Android, and it wraps the mobile platform's accessibility layer. The selectors are different — XCUITest for iOS, UIAutomator2 for Android — and setting up the environment is more involved than browser automation.

If this role requires Appium, I would set up a local environment with Android Studio and Appium Desktop within my first week and work through a tutorial project. My Selenium + WebDriver foundation transfers directly to Appium's API, so the learning curve would be primarily around device management and locator strategies."

---

### Example 2: Gauge (Test Framework)

**If asked**: "Do you have experience with the Gauge test framework?"

"I haven't used Gauge. I know it is a free, open-source acceptance testing framework developed by ThoughtWorks, similar in purpose to Cucumber in that tests are written in markdown-style plain language, but the execution model and plugin structure are different. My BDD experience has been with Cucumber + Serenity.

I would approach Gauge by reading its official documentation and noting how the concept of 'steps' and 'specs' maps to what I already know from Cucumber feature files and step definitions. The underlying test logic would be familiar — it is the framework-specific syntax I would need to learn. I'd be comfortable being productive within a couple of weeks."

---

### Example 3: RestAssured (If Gaps are Probed)

**If asked**: "How much RestAssured experience do you have in real projects?"

"At Cerexio my API testing was done in Postman and Newman, so I built strong API testing instincts there — request-response cycles, authentication, schema validation, negative testing. I am currently building my RestAssured skills in my own project, working through the Singer Page API layer. I can write RestAssured tests — `given().header().body().when().post().then().statusCode()` — and I understand the BDD-style fluent interface.

What I don't yet have is production experience debugging RestAssured in a large codebase with complex request specifications and custom response parsers. I'd be honest that my Postman skills are more polished right now, but RestAssured builds on all the same concepts and I am actively closing that gap."

---

### General Rules

- Never pretend to know something you don't — experienced interviewers will probe deeper and the bluff will fail
- "I haven't used it" followed by genuine knowledge is far better than a vague guess
- Always bridge to what you DO know: "I haven't used X but I've used Y which solves the same problem"
- Ask a clarifying question: "Is this a tool you use heavily day-to-day, or more occasionally?" — it tells you how big the gap really is
- Show curiosity: "That's interesting — I'd like to learn more about how you use it here. What's your stack built around it?"

---

*Guide complete — all six sections personalized for Kupeshanth Kupenthiran. Covers behavioral STAR stories, technical depth, framework design, live scenarios, smart interviewer questions, and handling knowledge gaps with integrity.*
