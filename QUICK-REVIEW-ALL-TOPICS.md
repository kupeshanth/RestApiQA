# QA Interview — Quick Review Cheat Sheet
## All Topics | Short Questions + Short Answers | Read Before Interview

> Use this the night before. Every question, one-line answer.
> See something unfamiliar? Click the **→ Full Guide** link at the top of each section.

---

## INTERVIEW CHAPTER FILES (guides/interview/)

> Deep-dive chapters personalised for Kupeshanth — behavioral answers, live scenario practice, coding questions.

| Part | Topic | Link |
|------|-------|------|
| 1 | HR & Resume / Behavioral | [PART1-HR-RESUME.md](guides/interview/PART1-HR-RESUME.md) |
| 2 | Manual Testing | [PART2-MANUAL-TESTING.md](guides/interview/PART2-MANUAL-TESTING.md) |
| 3 | Core Java | [PART3-JAVA.md](guides/interview/PART3-JAVA.md) |
| 4 | Selenium WebDriver | [PART4-SELENIUM.md](guides/interview/PART4-SELENIUM.md) |
| 5 | TestNG | [PART5-TESTNG.md](guides/interview/PART5-TESTNG.md) |
| 6 | Playwright | [PART6-PLAYWRIGHT.md](guides/interview/PART6-PLAYWRIGHT.md) |
| 7 | API Testing (RestAssured + Postman) | [PART7-API-TESTING.md](guides/interview/PART7-API-TESTING.md) |
| 8 | Appium | [PART8-APPIUM.md](guides/interview/PART8-APPIUM.md) |
| 9 | SQL | [PART9-SQL.md](guides/interview/PART9-SQL.md) |
| 10 | CI/CD | [PART10-CICD.md](guides/interview/PART10-CICD.md) |
| 11 | Agile & JIRA | [PART11-AGILE-JIRA.md](guides/interview/PART11-AGILE-JIRA.md) |
| 12 | Framework Design | [PART12-FRAMEWORK-DESIGN.md](guides/interview/PART12-FRAMEWORK-DESIGN.md) |
| 13 | Coding Challenges | [PART13-CODING.md](guides/interview/PART13-CODING.md) |
| 14 | Live Scenarios | [PART14-SCENARIOS.md](guides/interview/PART14-SCENARIOS.md) |

---

## NAVIGATE TO FULL GUIDES

| # | Topic | Full Guide |
|---|-------|-----------|
| 1 | Selenium WebDriver | [→ Open](guides/01-SELENIUM-COMPLETE-GUIDE.md) |
| 2 | TestNG | [→ Open](guides/02-TESTNG-COMPLETE-GUIDE.md) |
| 3 | RestAssured | [→ Open](guides/03-RESTASSURED-COMPLETE-GUIDE.md) |
| 4 | Playwright API | [→ Open](guides/04-PLAYWRIGHT-API-GUIDE.md) |
| 5 | Playwright UI | [→ Open](guides/05-PLAYWRIGHT-UI-GUIDE.md) |
| 6 | Appium Mobile | [→ Open](guides/06-APPIUM-COMPLETE-GUIDE.md) |
| 7 | BDD / Cucumber / Gauge | [→ Open](guides/07-BDD-CUCUMBER-GAUGE-GUIDE.md) |
| 8 | CI/CD Jenkins + GitLab | [→ Open](guides/08-CICD-JENKINS-GITLAB-GUIDE.md) |
| 9 | ISTQB / QA Theory | [→ Open](guides/09-ISTQB-QA-CONCEPTS-GUIDE.md) |
| 10 | Interview Q&A Full | [→ Open](guides/10-INTERVIEW-QA-GUIDE.md) |
| 11 | Technical Interview Scenarios | [→ Open](guides/11-TECHNICAL-INTERVIEW-QA.md) |
| 12 | Your CV Stories & STAR Answers | [→ Open](guides/12-MY-EXPERIENCE-WITH-EXAMPLES.md) |
| 13 | Cypress | [→ Open](guides/13-CYPRESS-COMPLETE-GUIDE.md) |
| 14 | JMeter Performance | [→ Open](guides/14-JMETER-PERFORMANCE-TESTING-GUIDE.md) |
| 15 | SQL / Database Testing | [→ Open](guides/15-SQL-DATABASE-TESTING-GUIDE.md) |
| 16 | JIRA / Defect Management | [→ Open](guides/16-JIRA-DEFECT-MANAGEMENT-GUIDE.md) |
| 17 | Git for QA | [→ Open](guides/17-GIT-FOR-QA-GUIDE.md) |
| 18 | Parallel + Negative Testing | [→ Open](guides/18-PARALLEL-AND-NEGATIVE-TESTING-GUIDE.md) |
| 19 | Manual Testing | [→ Open](guides/19-MANUAL-TESTING-COMPLETE-GUIDE.md) |
| 20 | Accessibility (WCAG) | [→ Open](guides/20-ACCESSIBILITY-WCAG-TESTING-GUIDE.md) |
| 21 | Security Testing (OWASP) | [→ Open](guides/21-SECURITY-TESTING-OWASP-GUIDE.md) |
| 22 | ETL / Data Pipeline | [→ Open](guides/22-ETL-DATA-PIPELINE-TESTING-GUIDE.md) |
| 23 | Test Reporting (Allure) | [→ Open](guides/23-TEST-REPORTING-ALLURE-EXTENTREPORTS-GUIDE.md) |
| 24 | Google Cloud Spanner | [→ Open](guides/24-GOOGLE-CLOUD-SPANNER-TESTING-GUIDE.md) |
| 25 | Core Java + OOP + Design Patterns | [→ Open](guides/25-CORE-JAVA-AUTOMATION-GUIDE.md) |

---

## 1. SELENIUM
> Need more depth? [→ Full Selenium Guide](guides/01-SELENIUM-COMPLETE-GUIDE.md)

**Q: What is WebDriver architecture?**
A: Your test → Selenium Client → ChromeDriver (W3C HTTP protocol) → Chrome Browser.

**Q: Locator priority order?**
A: id → name → cssSelector → xpath. Use xpath only when nothing else works.

**Q: implicit vs explicit wait?**
A: Implicit = global, waits for element to appear. Explicit = specific condition on specific element. Never mix both.

**Q: What is ThreadLocal\<WebDriver\> and why?**
A: Gives each parallel thread its own driver. Without it, threads share one driver and crash each other.

**Q: StaleElementReferenceException cause and fix?**
A: DOM re-rendered after you found the element. Fix: re-find the element. Wrap in retry loop.

**Q: What is POM and why?**
A: Page Object Model — locators in page classes, logic in test classes. One place to update when UI changes.

**Q: ElementClickInterceptedException fix?**
A: Another element is on top. Scroll into view, wait for overlay to disappear, or use JS click as last resort.

**Q: difference between driver.close() and driver.quit()?**
A: close() = current tab only. quit() = all tabs + kills driver process. Always use quit() in teardown.

**Q: How to handle iframes?**
A: `driver.switchTo().frame("name")` → interact → `driver.switchTo().defaultContent()`.

**Q: What is JavascriptExecutor used for?**
A: Force-click hidden elements, scroll to element, set field values that Selenium can't reach normally.

---

## 2. TESTNG
> Need more depth? [→ Full TestNG Guide](guides/02-TESTNG-COMPLETE-GUIDE.md)

**Q: Annotation execution order?**
A: BeforeSuite → BeforeTest → BeforeClass → BeforeMethod → @Test → AfterMethod → AfterClass → AfterTest → AfterSuite.

**Q: @BeforeClass vs @BeforeMethod?**
A: @BeforeClass = once before first test in class (driver setup). @BeforeMethod = before every test (navigate to page).

**Q: What happens if @BeforeMethod fails?**
A: @Test is SKIPPED (not failed). AfterMethod still runs.

**Q: What is @DataProvider?**
A: Feeds multiple rows of data into one test method. Returns `Object[][]`. Each row = one test execution.

**Q: How to run only smoke tests?**
A: Tag with `@Test(groups="smoke")` and run `mvn test -Dgroups=smoke`.

**Q: What is ITestListener?**
A: Interface to react to test events. onTestFailure() is most used — take screenshot on failure.

**Q: What is IRetryAnalyzer?**
A: Retries a failed test automatically. Return true to retry, false to stop. Set max retry count.

**Q: parallel="tests" vs parallel="methods"?**
A: tests = each `<test>` tag runs in its own thread (safe). methods = each @Test method in its own thread (dangerous — tests must be 100% independent).

**Q: What does tlDriver.remove() do?**
A: Removes driver reference from ThreadLocal after quit(). Without it, memory leaks after many test runs.

**Q: How to skip a test conditionally?**
A: Throw `new SkipException("reason")` inside the test method.

---

## 3. RESTASSURED
> Need more depth? [→ Full RestAssured Guide](guides/03-RESTASSURED-COMPLETE-GUIDE.md)

**Q: What is the given/when/then pattern?**
A: given() = setup request. when() = send it. then() = assert response.

**Q: How to set base URL once for all tests?**
A: `RestAssured.baseURI = "https://api.example.com"` in @BeforeClass in BaseTest.

**Q: What is RequestSpecification?**
A: Reusable request config (headers, content type, auth). Defined once in BaseTest, used everywhere with `.spec(requestSpec)`.

**Q: How to extract a value from response?**
A: `.then().extract().path("id")` for one field. `.extract().response()` for the full response object.

**Q: How to assert an array has 100 items?**
A: `.body("size()", equalTo(100))`.

**Q: How to assert every item in array has userId=1?**
A: `.body("userId", everyItem(equalTo(1)))`.

**Q: What is JSON Schema validation?**
A: Validates entire response structure against a contract file. Catches renamed/removed fields automatically.

**Q: How to chain POST → GET → DELETE?**
A: Extract id from POST response → use it in GET/DELETE path param.

**Q: Soft assertions vs hard assertions?**
A: Hard: stops at first failure. Soft: collects all failures, reports at the end. Always end soft with `assertAll()`.

**Q: What does enableLoggingOfRequestAndResponseIfValidationFails() do?**
A: Prints full request + response only when a test fails. Essential for debugging.

---

## 4. PLAYWRIGHT API
> Need more depth? [→ Full Playwright API Guide](guides/04-PLAYWRIGHT-API-GUIDE.md)

**Q: How to send GET in Playwright?**
A: `const r = await request.get('/posts');`

**Q: How to assert status 200?**
A: `expect(r.status()).toBe(200);` or `expect(r.ok()).toBeTruthy();`

**Q: How to assert body field?**
A: `const body = await r.json(); expect(body.id).toBe(1);`

**Q: How to test with auth token?**
A: `await request.get('/protected', { headers: { 'Authorization': 'Bearer token' } });`

**Q: How to chain requests in Playwright API?**
A: Capture from first response: `const id = (await r.json()).id;` → use in next request.

**Q: How to run specific test?**
A: `npx playwright test --grep "test name"` or `npx playwright test tests/api/get.spec.ts`.

---

## 5. PLAYWRIGHT UI
> Need more depth? [→ Full Playwright UI Guide](guides/05-PLAYWRIGHT-UI-GUIDE.md)

**Q: Best locator strategy?**
A: `getByRole()` first, then `getByLabel()`, `getByText()`, `getByTestId()`. CSS/XPath last resort.

**Q: How does auto-waiting work?**
A: Before every action, Playwright waits for element to be attached, visible, stable, and enabled. No explicit waits needed for most cases.

**Q: NEVER use what for waiting?**
A: `page.waitForTimeout()` — it's a hardcoded sleep. Always wait for a real condition.

**Q: How to assert URL after navigation?**
A: `await expect(page).toHaveURL('/dashboard');`

**Q: How to handle a new tab?**
A: `const tab = await page.waitForEvent('popup'); await page.click('#link');`

**Q: How to save login session across all tests?**
A: `storageState` — login once in setup, save to file, all workers reuse it.

**Q: How to run all 3 browsers at once?**
A: Configure `projects` in playwright.config.ts with chromium, firefox, webkit → `npx playwright test`.

**Q: What is a trace and when do you use it?**
A: Recording of every action, screenshot, network call. Open with `npx playwright show-trace`. Use when CI test fails.

**Q: soft assertions in Playwright?**
A: `expect.soft(locator).toHaveText('...')` — collects failures without stopping the test.

---

## 6. APPIUM
> Need more depth? [→ Full Appium Guide](guides/06-APPIUM-COMPLETE-GUIDE.md)

**Q: What is Appium?**
A: Open-source framework for automating native, hybrid, and mobile web apps on Android (UIAutomator2) and iOS (XCUITest). Extends WebDriver.

**Q: What are Desired Capabilities?**
A: Key-value pairs telling Appium what device/app to automate. platformName, deviceName, appPackage, automationName are required for Android.

**Q: Native vs Hybrid vs Mobile Web?**
A: Native = built with SDK. Hybrid = native wrapper around WebView. Mobile Web = website in mobile browser.

**Q: How to switch context in hybrid app?**
A: `driver.context("WEBVIEW_com.example.app")` → interact → `driver.context("NATIVE_APP")`.

**Q: Best locator for cross-platform (Android + iOS)?**
A: `MobileBy.AccessibilityId()` — works on both.

**Q: How to swipe in Appium?**
A: W3C Actions API — press at top point, move to bottom point, release.

---

## 7. BDD / CUCUMBER / GAUGE
> Need more depth? [→ Full BDD Guide](guides/07-BDD-CUCUMBER-GAUGE-GUIDE.md)

**Q: What is BDD?**
A: Behaviour-Driven Development — tests written in plain English (Gherkin) readable by business + dev + QA. Aligns everyone before code is written.

**Q: Gherkin keywords?**
A: Feature, Scenario, Background, Given, When, Then, And, But, Scenario Outline, Examples.

**Q: Background vs BeforeEach?**
A: Background = Gherkin steps shown in the feature file, run before each scenario. BeforeEach = hidden Java hook.

**Q: Scenario vs Scenario Outline?**
A: Scenario = one fixed test. Scenario Outline = template + Examples table, runs once per row.

**Q: How to run only @smoke tagged scenarios?**
A: `mvn test -Dcucumber.filter.tags="@smoke"`

**Q: What does Serenity add to Cucumber?**
A: Rich living documentation reports, @Steps pattern for readable step libraries, automatic screenshots, WebDriver management.

**Q: Gauge vs Cucumber?**
A: Gauge uses Markdown specs instead of Gherkin. Steps start with `*`. Has "Concepts" (reusable step groups). Built-in reports.

**Q: How to share state between step definition classes?**
A: Use PicoContainer DI (add cucumber-picocontainer dep), inject shared context class via constructor.

---

## 8. CI/CD
> Need more depth? [→ Full CI/CD Guide](guides/08-CICD-JENKINS-GITLAB-GUIDE.md)

**Q: What is CI/CD?**
A: CI = every push triggers automatic build + test. CD = tested code auto-deployed to staging/prod.

**Q: Jenkinsfile structure?**
A: pipeline → stages → stage(name) → steps → sh 'command'. post { failure { emailext } }.

**Q: GitLab CI/CD config file?**
A: `.gitlab-ci.yml` — defines stages, jobs, image, script, artifacts, rules.

**Q: GitHub Actions trigger on PR?**
A: `on: [pull_request]` in workflow yml.

**Q: How to publish test report in Jenkins?**
A: Install Allure Jenkins plugin, add `allure([results: [[path: 'allure-results']]])` in post step.

**Q: Why tests pass locally but fail in CI?**
A: Headless mode, wrong baseURL, expired token, slower machine timeout, missing browser deps. Add `--headless`, `--no-sandbox`, increase timeout.

**Q: What is a branch protection rule?**
A: GitHub/GitLab setting: CI must pass + reviewer must approve before merge to main.

---

## 9. ISTQB / QA THEORY
> Need more depth? [→ Full ISTQB Guide](guides/09-ISTQB-QA-CONCEPTS-GUIDE.md)

**Q: Test levels?**
A: Unit → Integration → System → Acceptance (UAT). Each higher level = more cost to fix bugs found.

**Q: Verification vs Validation?**
A: Verification = "building it right" (reviews, walkthroughs). Validation = "building the right thing" (testing running system).

**Q: Equivalence Partitioning?**
A: Divide inputs into groups with same behaviour. Test ONE value from each group.

**Q: Boundary Value Analysis?**
A: Test at exact boundaries. Age 18-65 → test 17, 18, 65, 66. Bugs hide at boundaries.

**Q: Risk = ?**
A: Probability of failure × Impact if it fails. High risk = test first.

**Q: Bug lifecycle statuses?**
A: New → Assigned → Open → Fixed → Ready for Retest → Closed. Also: Rejected, Deferred, Cannot Reproduce, Duplicate.

**Q: Severity vs Priority?**
A: Severity = impact (set by QA). Priority = urgency (set by business). Can be different: low severity high priority = wrong logo before launch.

**Q: Shift-left testing?**
A: Involve QA earlier in SDLC — review requirements, test in dev environment, don't wait for release.

---

## 10. CYPRESS
> Need more depth? [→ Full Cypress Guide](guides/13-CYPRESS-COMPLETE-GUIDE.md)

**Q: Cypress vs Selenium main difference?**
A: Cypress runs INSIDE the browser (no WebDriver). Automatic waiting, no driver management, faster feedback.

**Q: How to intercept an API call in Cypress?**
A: `cy.intercept('GET', '/api/users', { fixture: 'users.json' }).as('getUsers');` then `cy.wait('@getUsers');`

**Q: How to make an API call in Cypress?**
A: `cy.request('POST', '/api/users', { name: 'Test' }).then(r => expect(r.status).to.eq(201));`

**Q: Best locator in Cypress?**
A: `cy.get('[data-cy="submit-btn"]')` — data-cy attribute is most stable.

**Q: Cypress limitations?**
A: No true multi-tab support, limited iframe support, only JavaScript/TypeScript, no Safari automation.

**Q: How to add custom command?**
A: Add to `cypress/support/commands.js`: `Cypress.Commands.add('login', (u, p) => { ... });` then use `cy.login()` in tests.

---

## 11. JMETER / PERFORMANCE
> Need more depth? [→ Full JMeter Guide](guides/14-JMETER-PERFORMANCE-TESTING-GUIDE.md)

**Q: Types of performance tests?**
A: Load (expected users), Stress (find breaking point), Spike (sudden surge), Endurance/Soak (sustained), Volume (large data), Scalability (scale up/down).

**Q: Key metrics?**
A: Response time (avg/P95/P99), Throughput (req/sec), Error rate (%), Concurrent users, Latency.

**Q: P95 vs Average?**
A: Average hides outliers. P95 = 95% of users get response within this time. P95 is what users actually experience.

**Q: JMeter Thread Group?**
A: Number of Users = concurrent users. Ramp-up = seconds to reach full load. Loop Count = test duration.

**Q: How to run JMeter without GUI (for CI)?**
A: `jmeter -n -t test.jmx -l results.jtl -e -o report/`

**Q: What is an acceptable error rate?**
A: Typically < 1%. Any error rate > 5% under expected load = performance problem.

---

## 12. SQL / DATABASE
> Need more depth? [→ Full SQL Guide](guides/15-SQL-DATABASE-TESTING-GUIDE.md)

**Q: How do you verify an API call wrote to the DB?**
A: `SELECT * FROM users WHERE email = 'test@test.com' AND created_at > NOW() - INTERVAL 1 MINUTE;`

**Q: How to find duplicate records?**
A: `SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;`

**Q: How to check no orphaned records?**
A: `SELECT o.id FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE u.id IS NULL;`

**Q: TRUNCATE vs DELETE?**
A: DELETE = row by row, logged, WHERE clause works, can rollback. TRUNCATE = drops all rows fast, no WHERE, harder to rollback.

**Q: What is ACID?**
A: Atomicity (all or nothing), Consistency (valid state before/after), Isolation (concurrent transactions don't interfere), Durability (committed data persists).

**Q: NULL trap?**
A: `WHERE field != 'value'` does NOT return rows where field IS NULL. Always check NULLs separately.

---

## 13. JIRA / DEFECT MANAGEMENT
> Need more depth? [→ Full JIRA Guide](guides/16-JIRA-DEFECT-MANAGEMENT-GUIDE.md)

**Q: Bug report summary formula?**
A: [Area] [What happened] [Condition]. Example: "Login: 500 error when submitting empty password on Safari mobile".

**Q: Severity levels?**
A: Critical (system crash/data loss), High (major feature broken), Medium (feature works with workaround), Low (cosmetic).

**Q: Developer says bug is "not reproducible" — what do you do?**
A: Share exact steps, OS, browser, environment. Record a video. Try to reproduce together. Never close without evidence.

**Q: What is a regression bug?**
A: A feature that worked before is now broken after a code change.

**Q: JQL to find all P1 open bugs in current sprint?**
A: `project = "APP" AND issuetype = Bug AND priority = Highest AND sprint in openSprints() AND status != Done`

**Q: Bug escape?**
A: A bug that reaches production without being caught in testing. Prevented by: better test coverage, automation, exploratory testing.

---

## 14. GIT
> Need more depth? [→ Full Git Guide](guides/17-GIT-FOR-QA-GUIDE.md)

**Q: git fetch vs git pull?**
A: fetch = downloads changes, doesn't merge. pull = fetch + merge.

**Q: How to find what changed between yesterday and today?**
A: `git log --oneline --since="1 day ago"` or `git diff HEAD~1 HEAD`

**Q: How to undo a commit already pushed?**
A: `git revert HEAD` — creates a new commit that undoes it. Safe. Never `git reset --hard` on pushed commits.

**Q: What is git stash?**
A: Saves uncommitted work temporarily. `git stash` → switch branch → `git stash pop` to restore.

**Q: .gitignore must-haves for QA?**
A: target/, node_modules/, .env, test-results/, playwright-report/, *.class, .DS_Store

**Q: How to check out a colleague's branch?**
A: `git fetch origin && git checkout origin/their-branch-name`

---

## 15. PARALLEL + NEGATIVE TESTING
> Need more depth? [→ Full Parallel & Negative Guide](guides/18-PARALLEL-AND-NEGATIVE-TESTING-GUIDE.md)

**Q: Why must WebDriver be in ThreadLocal for parallel tests?**
A: Otherwise all threads share one browser → actions collide → unpredictable failures.

**Q: testng.xml parallel="tests" thread-count="3" meaning?**
A: Up to 3 `<test>` blocks run simultaneously. Tests within one block still run serially.

**Q: Playwright parallel — do you need ThreadLocal?**
A: No. Each Playwright worker is an isolated process. Isolation is built-in.

**Q: A test fails in parallel but passes serially — first thing to check?**
A: Shared state — static fields, shared test data, order dependency, file name collisions.

**Q: 500 on bad input — always a bug?**
A: YES. Server must never crash on user input. Must return 400/422 with helpful message.

**Q: What is boundary value testing?**
A: Test at exact boundaries (min, min-1, min+1, max-1, max, max+1). Bugs hide at boundaries.

**Q: SQL injection test — expected result?**
A: 400 error or "invalid input" message. NEVER: data returned, auth bypassed, or 500.

---

## 16. MANUAL TESTING
> Need more depth? [→ Full Manual Testing Guide](guides/19-MANUAL-TESTING-COMPLETE-GUIDE.md)

**Q: Smoke vs Sanity vs Regression?**
A: Smoke = is the build stable enough to test? Sanity = did this specific fix work? Regression = did anything else break?

**Q: What is exploratory testing?**
A: Simultaneous learning, test design, and execution. Structured freedom — not random clicking. Use session charters.

**Q: Test case required fields?**
A: ID, Title, Module, Precondition, Steps to Reproduce, Expected Result, Actual Result, Status, Priority.

**Q: What is UAT?**
A: User Acceptance Testing — business validates the product meets their requirements. QA supports, doesn't lead.

**Q: How to test with no requirements?**
A: Use similar features as reference, talk to dev and PO, document your assumptions, use exploratory testing.

**Q: What to do when a test is blocked?**
A: Log "Blocked" status with reason, flag to team immediately, find a workaround or alternative path to test.

---

## 17. ACCESSIBILITY (WCAG)
> Need more depth? [→ Full Accessibility Guide](guides/20-ACCESSIBILITY-WCAG-TESTING-GUIDE.md)

**Q: What does POUR stand for?**
A: Perceivable, Operable, Understandable, Robust.

**Q: WCAG level for most companies?**
A: Level AA — required by most laws (ADA, EN 301 549, Section 508).

**Q: Color contrast ratio for normal text?**
A: 4.5:1 minimum (AA). 3:1 for large text (18pt+ or 14pt bold).

**Q: How to test keyboard accessibility?**
A: Tab through every interactive element. Every button/link/input must be reachable. Enter/Space must activate. Escape must close modals.

**Q: Best automated accessibility tool?**
A: axe-core. Integrates with Playwright: `new AxeBuilder({ page }).withTags(['wcag2aa']).analyze()`

**Q: What do automated tools miss?**
A: ~70% of issues. Context-dependent issues: meaningful alt text, logical reading order, sensible focus order, color used as only indicator.

---

## 18. SECURITY TESTING (OWASP)
> Need more depth? [→ Full Security Guide](guides/21-SECURITY-TESTING-OWASP-GUIDE.md)

**Q: What is OWASP Top 10?**
A: The 10 most critical web application security risks. Every QA should know them.

**Q: #1 OWASP risk (2021)?**
A: Broken Access Control — users accessing other users' data (IDOR), bypassing authorization.

**Q: How to test IDOR?**
A: GET /users/2 while logged in as user 1. Should get 403. If you see user 2's data → IDOR vulnerability.

**Q: XSS types?**
A: Reflected (payload in URL/input, immediate), Stored (payload saved to DB, shown to all users — worse).

**Q: How to verify XSS is prevented?**
A: Enter `<script>alert('xss')</script>` in inputs. Page should show it as text or reject it. Alert should NEVER fire.

**Q: What is rate limiting and how to test?**
A: Server limits requests per second. Send 100 requests rapidly → should get 429 Too Many Requests after threshold.

**Q: QA security testing vs penetration testing?**
A: QA = black-box, OWASP-based, input validation, auth checks. Pentest = specialist, deep exploit development, infrastructure.

---

## 19. ETL / DATA PIPELINE
> Need more depth? [→ Full ETL Guide](guides/22-ETL-DATA-PIPELINE-TESTING-GUIDE.md)

**Q: ETL stands for?**
A: Extract (pull from source), Transform (clean/reshape/enrich), Load (write to target).

**Q: Data completeness testing?**
A: `SELECT COUNT(*) FROM source` vs `SELECT COUNT(*) FROM target` — counts must match (or differ by known reason).

**Q: Data accuracy testing?**
A: JOIN source and target on primary key, compare all field values. Differences = transformation errors.

**Q: How to find data loaded incorrectly?**
A: `SELECT s.id, s.amount, t.amount FROM source s JOIN target t ON s.id = t.id WHERE s.amount != t.amount;`

**Q: What is data migration testing?**
A: Baseline before migration → run migration → verify counts, values, relationships intact. Test rollback too.

**Q: What are data quality dimensions?**
A: Completeness, Accuracy, Consistency, Timeliness, Validity, Uniqueness.

---

## 20. TEST REPORTING
> Need more depth? [→ Full Reporting Guide](guides/23-TEST-REPORTING-ALLURE-EXTENTREPORTS-GUIDE.md)

**Q: Allure key annotations?**
A: @Epic, @Feature, @Story (hierarchy), @Description, @Step (shown in report), @Severity, @Attachment (screenshots).

**Q: How to attach screenshot on failure in Allure?**
A: In ITestListener.onTestFailure(): `@Attachment(type="image/png") return driver.getScreenshotAs(OutputType.BYTES);`

**Q: How to add RestAssured logs to Allure?**
A: `RestAssured.filters(new AllureRestAssured());` — one line in BaseTest setUp().

**Q: Playwright built-in report command?**
A: `npx playwright show-report` — opens HTML report in browser.

**Q: What makes a good test report?**
A: Pass rate summary, categorised failures, screenshots on failure, drill-down to step level, history trend.

**Q: Allure generate command?**
A: `allure generate allure-results/ --clean -o allure-report/` then `allure open allure-report/`

---

## 21. GOOGLE CLOUD SPANNER
> Need more depth? [→ Full Spanner Guide](guides/24-GOOGLE-CLOUD-SPANNER-TESTING-GUIDE.md)

**Q: What is Google Cloud Spanner?**
A: Fully managed, globally distributed relational database by Google. ACID guarantees + horizontal scaling globally.

**Q: How does Spanner differ from MySQL?**
A: No auto-increment IDs (use UUIDs), no ENUM type, interleaved tables for parent-child locality, strong global consistency.

**Q: How to test Spanner without GCP account?**
A: Use Cloud Spanner Emulator: `gcloud emulators spanner start` → set `SPANNER_EMULATOR_HOST=localhost:9010`.

**Q: Strong read vs stale read?**
A: Strong read = guaranteed latest data. Stale read = may return slightly old data but faster. Tests should use strong reads.

**Q: How to verify data after API call in Spanner?**
A: API call → use Java Spanner client to query → `Statement.of("SELECT * FROM users WHERE id = @id")` → assert fields match.

**Q: Why use UUID instead of sequential IDs in Spanner?**
A: Sequential IDs cause "hot spots" — all writes go to same server. UUIDs distribute writes evenly.

---

## 22. CORE JAVA & OOP
> Need more depth? [→ Full Java Guide](guides/25-CORE-JAVA-AUTOMATION-GUIDE.md)

**Q: == vs .equals() for Strings?**
A: `==` compares references (memory address). `.equals()` compares content. ALWAYS use `.equals()` for Strings.

**Q: 4 pillars of OOP?**
A: Encapsulation (hide data), Inheritance (reuse parent), Polymorphism (same method different behaviour), Abstraction (hide complexity).

**Q: Interface vs Abstract class?**
A: Abstract class = partial implementation, one parent only. Interface = pure contract, implement many. Use abstract for shared code (BaseTest), interface for capability (WebDriver).

**Q: Overloading vs Overriding?**
A: Overloading = same class, same name, different params, compile-time. Overriding = child replaces parent method, runtime, needs `@Override`.

**Q: What is ThreadLocal?**
A: Each thread gets its own copy of a variable. Used in parallel Selenium to give each test its own WebDriver.

**Q: ArrayList vs LinkedList?**
A: ArrayList = fast random access, slow middle insert. LinkedList = slow random access, fast insert/delete. Use ArrayList for most cases.

**Q: What is autoboxing?**
A: Auto-convert primitive to wrapper: `int` → `Integer`. Happens when adding int to `List<Integer>`.

**Q: Checked vs Unchecked exceptions?**
A: Checked = compiler forces handling (IOException). Unchecked (RuntimeException) = optional (NullPointerException, NoSuchElementException).

**Q: What is Singleton pattern?**
A: One instance of a class shared everywhere. Private constructor + static getInstance(). Used for ConfigReader, DriverFactory.

**Q: What is Builder pattern?**
A: Construct complex objects step by step with readable chaining: `new User.Builder().name("x").email("y").build()`.

**Q: What is Factory pattern?**
A: Create objects without specifying exact class. `DriverFactory.createDriver("chrome")` hides which driver class is used.

**Q: What is `final` on a variable?**
A: Cannot be reassigned after assignment. `public static final String BASE_URL = "..."` — constant.

**Q: What does `static` mean on a method?**
A: Belongs to the class, not an instance. Call without creating object: `Math.sqrt()`, `Collections.sort()`.

**Q: What is a Stream in Java 8?**
A: Pipeline operations on collections. `list.stream().filter().map().collect()`. Replaces verbose for-loops.

**Q: Design patterns you use in automation?**
A: POM (Page Object), Singleton (ConfigReader), Factory (DriverFactory), Builder (test data), Strategy (wait strategies).

---

## MASTER COMMAND REFERENCE

```bash
# ── MAVEN / JAVA ──────────────────────────────────────────────────────────────
mvn test                              # run all tests
mvn test -Dtest=ClassName             # run one class
mvn test -Dtest=ClassName#methodName  # run one method
mvn test -Dgroups=smoke               # run by group
mvn clean test                        # clean + run
mvn dependency:resolve                # download deps
./apache-maven-3.9.6/bin/mvn test     # local Maven

# ── PLAYWRIGHT ────────────────────────────────────────────────────────────────
npx playwright test                   # all tests
npx playwright test tests/login.spec.ts  # one file
npx playwright test --grep @smoke     # by tag
npx playwright test --project=chromium  # one browser
npx playwright test --workers=1       # serial
npx playwright test --headed          # show browser
npx playwright test --debug           # step through
npx playwright show-report            # open HTML report
npx playwright test --update-snapshots  # update visual baselines

# ── CYPRESS ───────────────────────────────────────────────────────────────────
npx cypress open                      # interactive GUI
npx cypress run                       # headless CI mode
npx cypress run --spec tests/login.cy.ts  # one file
npx cypress run --browser firefox     # specific browser

# ── JMETER ────────────────────────────────────────────────────────────────────
jmeter -n -t plan.jmx -l results.jtl -e -o report/  # non-GUI run

# ── ALLURE ────────────────────────────────────────────────────────────────────
allure serve allure-results/          # serve live
allure generate allure-results/ -o allure-report/ --clean
allure open allure-report/

# ── GIT ───────────────────────────────────────────────────────────────────────
git status                            # what changed
git log --oneline -10                 # last 10 commits
git diff HEAD~1 HEAD                  # last commit changes
git stash && git stash pop            # save/restore work
git revert HEAD                       # undo last commit safely
git fetch origin && git checkout origin/branch  # get colleague's branch

# ── POWERSHELL FILTER ─────────────────────────────────────────────────────────
mvn test 2>&1 | Select-String "Tests run|BUILD|FAIL"
npx playwright test 2>&1 | Select-String "passed|failed"

# ── APPIUM ────────────────────────────────────────────────────────────────────
appium                                # start server
appium --port 4723                    # specific port

# ── ACCESSIBILITY ─────────────────────────────────────────────────────────────
npx lighthouse https://example.com --only-categories=accessibility
npx playwright test tests/accessibility/
```

---

## WHAT TO SAY FOR EACH TOOL YOU KNOW WELL vs PARTIALLY

| Tool | Say confidently | Say if gap |
|------|----------------|-----------|
| Selenium | "Hands-on at Qoria, built POM framework, parallel with ThreadLocal" | — |
| Playwright | "Hands-on at Qoria and personal projects, API + UI + GitHub Actions" | — |
| Postman | "Used at Cerexio for API testing, collections, test scripts" | — |
| RestAssured | "Built a framework this week — GET/POST/PUT/DELETE, chaining, schema" | "Actively building, solid Java foundation" |
| Cypress | "Used in personal projects with JavaScript" | "Less experience than Selenium/Playwright" |
| JMeter | "Familiar with load testing concepts, Thread Groups, performance metrics" | "Less hands-on — happy to dive deeper" |
| Appium | "Understand architecture, caps, locators — building hands-on experience" | Be honest, don't bluff |
| Gauge | "Similar to Cucumber — BDD in Markdown. Know the concepts, less hands-on" | Be honest |
| BDD/Cucumber | "Built Serenity+Cucumber framework in Singer Page project" | — |
| GCP/GitHub Actions | "Used GitHub Actions and GCP pipelines at Qoria for CI/CD" | — |

---

*Read this file the morning of your interview. Open full guides only for deep dives.*
*All guides: https://github.com/kupeshanth/RestApiQA/guides/*
