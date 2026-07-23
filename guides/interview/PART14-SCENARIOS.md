# Part 14 — Scenario-Based Questions | 50 Real-World Scenarios | Complete Answers

> CV Context: Kupeshanth — Qoria Lanka (Selenium+TestNG, Playwright, GitHub Actions), Cerexio (Angular, Postman, JIRA). Projects: Singer Page (Serenity+Cucumber+BDD), Python+Pytest+Selenium, Playwright+JS.

> Note: These questions test QA maturity — your thinking process matters as much as the answer. Structure every answer clearly: identify the problem, investigate, act, prevent recurrence.

---

## S1. Login feature isn't working. Walk through your debugging approach.

**Answer:**

**Step 1 — Reproduce and characterise the issue:**
- Is it completely broken or intermittent?
- Which environments? (dev, staging, prod)
- Which browsers/devices? (browser-specific or universal)
- Which user roles or credential types? (all users or specific accounts)
- What is the exact error? (UI error message, HTTP status code, console error)

**Step 2 — Check the obvious first:**
- Is the test environment up? (hit the base URL manually)
- Are the credentials correct? (try logging in manually in the browser)
- Check browser console for JS errors (F12 → Console)
- Check the Network tab for the login request (is it sending? what status code returns?)

**Step 3 — Investigate the stack:**
- UI: Is the login form rendering correctly? Any element locator changes?
- API: Does the `/auth/login` endpoint respond correctly in Postman/curl?
- Backend: Are there any error logs? (check server logs, Splunk, CloudWatch)
- Database: Is the user account active? Is the password hash correct?

**Step 4 — Narrow down:**
- Hardcode credentials to eliminate config issues
- Test with a known-good account
- Compare the failing request with a working one (headers, body format, content-type)

**Step 5 — Communicate:**
- Log the bug in JIRA with: steps, environment, expected vs actual, screenshots, HTTP request/response
- Tag the developer responsible for auth

---

## S2. Your automation test passes locally but fails in CI. How do you debug?

**Answer:**

**Common causes and checks:**

1. **Headless mode differences:**
   - CI runs headless; some elements behave differently (scroll, hover, fixed headers)
   - Fix: run locally in headless mode to reproduce: `-Dheadless=true`

2. **Timing/flakiness:**
   - CI servers are slower — elements take longer to appear
   - Fix: replace any hardcoded `Thread.sleep()` with explicit waits; increase timeout for CI

3. **Environment differences:**
   - Different Java/Node version in CI
   - Missing environment variables (credentials, API keys)
   - Check: `java -version` locally vs CI; verify all `env:` vars are set in the workflow

4. **Test data state:**
   - CI environment may have stale or missing test data
   - Fix: ensure `@BeforeMethod` creates fresh test data via API

5. **Screenshot and logs from CI:**
   ```yaml
   - name: Upload screenshots on failure
     uses: actions/upload-artifact@v4
     if: failure()
     with:
       name: screenshots
       path: screenshots/
   ```

6. **Run CI pipeline locally:**
   - Use `act` (GitHub Actions local runner) or Docker to replicate CI environment

**Systematic approach:**
- Read the full CI log — the actual failure message is usually clear
- Compare passing local log with failing CI log line by line
- Make CI verbose: add `--verbose` or logging to the test run

---

## S3. A test was passing for 3 months and suddenly fails today. What do you do?

**Answer:**

**First thought — something changed. What changed today?**

1. **Check recent deployments:**
   - Was there a code deployment today? Ask the dev team or check the release notes
   - Was there a DB schema change? A new API version?

2. **Check git history:**
   - Did any test or page object change? `git log --since="yesterday"`

3. **Check the environment:**
   - Is the test environment stable? (try opening the page manually)
   - Did the URL, port, or domain change?
   - Did test data get wiped or modified?

4. **Investigate the failure:**
   - Read the exact error message — it tells you almost everything
   - Check screenshots/video attached to the CI run
   - Is it a locator issue (element not found)? A functional issue (wrong result)? A connection issue (timeout)?

5. **Run it in isolation:**
   - Run just the failing test to rule out cross-test pollution

6. **Document and raise:**
   - If it's an application bug: raise in JIRA with steps to reproduce
   - If it's a test fragility: fix the test and add a note in the PR about what was brittle

---

## S4. Developer says your bug is not reproducible. What do you do?

**Answer:**

**Don't argue — provide evidence:**

1. **Prepare a detailed reproduction guide:**
   - Environment: exact URL, browser, OS, browser version
   - Preconditions: user role, existing data state, test data values
   - Steps: numbered, explicit (click X, enter Y, wait for Z)
   - Expected vs Actual: with screenshots, video recording, or a GIF
   - API trace: if relevant, capture the network request/response from DevTools

2. **Replicate with the developer present:**
   - Screen share and reproduce it live
   - Let the developer control and see it themselves

3. **Check environment parity:**
   - Is the developer testing against the same environment? (not their local build)
   - Is there a data condition required? (specific user state, timezone, flags)

4. **Check logs:**
   - Server logs, browser console, Sentry/Datadog error tracking — show log evidence

5. **Mark in JIRA and keep open:**
   - Don't close the bug — add "Under investigation" status with all evidence attached
   - Assign it for a follow-up if it reappears

6. **Remain professional:**
   - "Not reproducible" can mean environment-specific — work together to find out why, not to prove who is right

---

## S5. You have 3 days to test a feature with no requirements. What do you do?

**Answer:**

**Day 1 — Understand:**
- Meet with the BA/PO/developer to understand what the feature is supposed to do (even informally — a 30-min call is enough)
- Review any related tickets, Confluence pages, designs, or Figma mockups
- Check similar existing features for patterns — if there's a "Create Product" form, the new "Create Category" form will behave similarly
- Identify risks: what could go wrong? What integrates with this feature?

**Write working assumptions:**
- Document your understanding and share it: "Based on my analysis, I believe the feature should: do X, Y, Z..."
- Get sign-off or corrections from the team

**Day 2 — Test:**
- Happy path (golden flow): the normal, expected usage
- Negative cases: invalid inputs, empty fields, unauthorised access
- Boundary cases: max/min character limits, zero values, special characters
- Integration: does it work with other parts of the system?
- Cross-browser and responsive (if UI)

**Day 3 — Document and report:**
- Write up test results with found bugs in JIRA
- Write basic test cases in Zephyr or Confluence for future reference
- Flag any untested risk areas: "I was unable to test X due to no test data/environment access — this is a risk"

---

## S6. A production bug was found that should have been caught in testing. How do you handle it?

**Answer:**

**Immediate response:**
- Acknowledge it. No defensiveness. The team wins and loses together.
- Help triage the severity (P1/P2) and priority for the fix

**Root cause analysis (after the fire is out):**
- Why was it missed? Common causes:
  - Untested edge case — test coverage gap
  - The test existed but wasn't run (wrong scope/suite)
  - The test was marked as flaky and skipped
  - The feature was tested but the bug was introduced by a later change

**Preventive actions:**
- Add the specific test case that would have caught this bug
- Review the test coverage for that feature area
- If it was a regression: review what changed between the last passing release and the broken one
- Add it to the regression suite so it never misses again

**Communication:**
- Write a short incident report or blameless post-mortem: what happened, root cause, what we're changing
- Share learnings with the team — not to blame, but to improve

---

## S7. You need to automate 500 test cases in 2 weeks. How do you prioritise?

**Answer:**

500 tests in 2 weeks is not realistic for one person. First, set expectations clearly, then prioritise smartly.

**Prioritisation framework:**

1. **Risk-based (highest impact first):**
   - Smoke tests: core workflows the product cannot function without (login, checkout, key APIs)
   - Regression hotspots: areas that break frequently based on historical bugs
   - Business-critical: features that directly generate revenue or are used by the most users

2. **Effort vs. value:**
   - High value + low effort = automate first
   - High value + high effort = estimate and plan
   - Low value + high effort = skip for now, document the gap

3. **Categorise the 500:**
   - Group into smoke (20-30), critical regression (100-150), full regression (remainder)
   - Automate smoke + critical regression first (cover ~80% of risk with ~30% of tests)

4. **Communicate:**
   - "In 2 weeks I can automate 80-100 high-value tests. To cover all 500, I need either more time or additional engineers."
   - Document which tests are manually covered in the interim

---

## S8. The team wants to skip testing to meet a release deadline. What do you do?

**Answer:**

**Don't just say no — quantify the risk:**

1. **Ask what would be skipped:**
   - Skipping manual exploratory testing on a low-risk UI tweak? That's a considered risk.
   - Skipping regression on the payment flow? That's a P1 risk to the business.

2. **Present the risk clearly:**
   - "If we skip regression, we risk not catching regressions in these high-risk areas: X, Y, Z. Based on our history, these types of changes have caused production issues before."

3. **Propose a compromise:**
   - Run smoke tests only (10 min) — at minimum, core flows work
   - Manual testing of the highest-risk area only
   - Deploy to a canary (10% of traffic) and monitor logs before full release

4. **Document the decision:**
   - If the team decides to release with reduced testing, document in JIRA: "Released without full regression — known risk. Next sprint: run full regression."
   - This protects you and records the decision.

5. **Your professional responsibility:**
   - As QA, your job is to make the risk visible, not to block delivery. Once the risk is clear and documented, the decision is the PM/lead's to make.

---

## S9. API returns 500 error. How do you investigate?

**Answer:**

A 500 Internal Server Error means something on the server side broke. The API is not handling the request successfully.

**Step 1 — Capture everything:**
- The full request: method, URL, headers, body
- The full response: status code, response body (often contains an error message), response headers

**Step 2 — Check the request:**
- Is the request body valid JSON?
- Are all required fields present? Correct data types?
- Is the auth token valid and not expired?
- Compare with a working request — what's different?

**Step 3 — Check server logs:**
- Ask the developer for server logs at the time of the failure (application logs, not access logs)
- Look for stack traces — these tell you exactly what line of code threw the exception

**Step 4 — Isolate the trigger:**
- Does the same request work with different data?
- Is it specific to a certain user role, certain payload size, or certain environment?
- Is it consistently 500 or intermittent?

**Step 5 — Document and raise:**
- JIRA bug: include the cURL command to reproduce, the response body, timestamp for server log lookup, and which environment

```bash
# Always share the exact cURL for reproducibility
curl -X POST https://api.example.com/users \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test", "email": "test@test.com"}'
```

---

## S10. Selenium cannot find an element even though it's on the page. What do you check?

**Answer:**

**1. Timing issue (most common):**
- The element exists in the DOM but Selenium looks before the page fully loads
- Fix: replace `findElement` with an explicit wait
```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("myElement")));
```

**2. Wrong locator:**
- Inspect the element in DevTools and verify the locator is unique and stable
- Test the XPath/CSS in the browser console: `$x("//button[@id='submit']")` or `$$(".my-class")`

**3. iFrame:**
- If the element is inside an `<iframe>`, you must switch to it first
```java
driver.switchTo().frame("myIframe");
// Now interact with the element
driver.switchTo().defaultContent(); // switch back
```

**4. Shadow DOM:**
- Modern web components use Shadow DOM — standard `findElement` cannot pierce it
- Use JavaScript executor: `driver.executeScript("return document.querySelector('...').shadowRoot.querySelector('...')")`

**5. Element is not visible:**
- Check if the element is hidden (`display:none`, `visibility:hidden`, off-screen)
- `visibilityOfElementLocated` waits for visible; `presenceOfElementLocated` waits for just DOM presence

**6. Dynamic ID:**
- If the ID changes on each page load (e.g., `id="element_12345_67890"`), the locator breaks
- Use a stable attribute: name, data-testid, CSS class, or a relative XPath

**7. Stale element:**
- Page refreshed or DOM changed after you found the element — it's stale
- Re-find the element before interacting with it

---

## S11. Tests run in parallel and randomly fail. How do you debug?

**Answer:**

**Root cause — shared state:**
The most common cause of parallel test failures is shared mutable state. One thread interferes with another.

**Check list:**

1. **Static variables:**
   - Static `WebDriver driver` is shared across threads
   - Fix: use `ThreadLocal<WebDriver>` so each thread has its own driver

2. **Shared test data:**
   - Two tests trying to create a user with the same email address
   - Fix: use unique data per test: `"user_" + System.currentTimeMillis() + "@test.com"`

3. **Database state:**
   - Test A creates a record, Test B modifies it, Test C reads it — race condition
   - Fix: each test creates and cleans up its own data

4. **File writes:**
   - Two tests writing to the same log file or screenshot with the same name
   - Fix: include thread ID or test name in file names

5. **Browser session sharing:**
   - If using Selenium Grid, ensure sessions are allocated per test not per class

6. **Debugging approach:**
   - Run the tests that fail in parallel, but in sequential mode — if they pass, it confirms the parallel issue
   - Add logging with thread IDs to trace what each thread is doing
   - Look at the exact failure — which line? Which data? Which thread?

---

## S12. You joined a project with no automation. Where do you start?

**Answer:**

**Week 1 — Understand:**
- Meet with developers and PMs: what does the application do? What are the most critical flows?
- Review existing test documentation (even if just manual test cases in Excel or Confluence)
- Run the application manually — understand it as a user
- Look at the bug history in JIRA: what has broken before? These are your highest-risk areas

**Week 2 — Assess and plan:**
- Identify the tech stack (what language do developers use? what CI is in place?)
- Propose a framework that fits the team (Selenium+TestNG, Playwright, RestAssured)
- Define the automation strategy: start with API tests (faster, more stable), then critical UI flows

**Week 3+ — Build the foundation:**
- Set up the project skeleton: folder structure, BaseTest, ConfigReader, CI pipeline
- Write smoke tests first — the 5-10 most critical tests that prove the system is working
- Get the CI pipeline running with these smoke tests as a foundation

**Key principles:**
- Don't try to automate everything at once
- Get the first test running and in CI as fast as possible — this proves the framework works
- Involve the team: walk them through what you've built so they can contribute

---

## S13. Your test suite takes 4 hours to run. How do you reduce it?

**Answer:**

**1. Run tests in parallel:**
- The biggest win — 4 hours / 4 threads = ~1 hour
- TestNG: `parallel="methods" thread-count="4"` in testng.xml
- Playwright: `fullyParallel: true, workers: 4`

**2. Remove redundant tests:**
- Audit for duplicated tests — tests that verify the same thing in different ways
- Archive tests for features that no longer exist

**3. Replace UI tests with API tests where possible:**
- An API test takes ~200ms; a UI test takes 5-10 seconds
- Test business logic at the API level; test the UI for presentation only

**4. Group and run smartly:**
- Only run smoke (10 min) on every PR
- Run full regression nightly or before a release
- Developers don't need to wait for all 500 tests on every push

**5. Optimise slow tests:**
- Find the 20% of tests that take 80% of the time (`@Test(timeOut)` reports)
- Replace explicit `Thread.sleep()` calls with smart waits
- Reuse browser sessions where tests are independent and share setup

**6. Parallelize in CI:**
- Use a matrix strategy to split tests across multiple CI runners

```yaml
strategy:
  matrix:
    shard: [1, 2, 3, 4]
steps:
  - run: mvn test -Dgroups=shard${{ matrix.shard }}
```

---

## S14. A developer changed code without telling QA. Tests broke. What do you do?

**Answer:**

**Immediate action:**
- Identify which tests broke and why — often a locator or API contract change
- Fix the affected tests so the CI pipeline is green again (especially if blocking other PRs)

**Root cause and process fix:**
- This is a process gap, not just a technical one
- Raise it constructively in the next retrospective: "When code changes affect testable contracts (locators, API schemas, data formats), QA needs to know. How can we improve communication here?"

**Proposed process improvements:**
- PR descriptions should include "QA impact" section: "This PR changes X — QA needs to update tests for Y"
- Require test updates in the same PR as the code change
- Component-level tests (in the dev's codebase) catch contract breaks before QA automation is affected
- Set up Slack notifications for deployments to staging

**Stay collaborative:**
- Don't make the developer feel attacked — the goal is process improvement, not blame
- Offer to pair with developers on understanding what breaks automation when they change code

---

## S15. You find a P1 bug on release day. What do you do?

**Answer:**

**Immediately:**
1. Verify the bug is real — reproduce it one more time on the target release environment
2. Assess the impact: does it affect all users or a subset? Is there a workaround?
3. Escalate immediately — message the team lead, PM, and relevant developer directly (don't just log it in JIRA)

**Severity assessment:**
- Is the core user journey broken? (login, checkout, create) — this is a showstopper
- Is it a cosmetic or edge case that affects a small % of users? — may be acceptable to release with a known issue

**Escalation meeting (rapid):**
- Can the bug be fixed in time? (developer gives an estimate)
- If yes: fix, test the fix, release with delay
- If no: can we release with reduced scope (feature flag to hide the broken feature)? Or rollback the specific change?
- Document the decision

**Your role:**
- Provide the clearest possible bug report: exact steps, impact assessment, affected users
- Be ready to test the hotfix immediately if a fix is provided
- Stay calm and solution-focused

---

## S16. How do you test a feature that's not UI-testable (background job, cron)?

**Answer:**

**Strategy: test the inputs and outputs, not the mechanism.**

1. **Trigger the job manually (if possible):**
   - Most background jobs have a manual trigger endpoint for testing: `POST /admin/jobs/run-nightly-report`
   - Use this in testing rather than waiting for the cron schedule

2. **Test the outcome via API or database:**
   - Before: check the initial state of data
   - Trigger the job
   - After: verify the data changed as expected

```java
// Example: test a job that sends daily digest emails
@Test
public void testNightlyDigestJob() {
    // Arrange: create test data that should trigger the email
    ApiHelper.createUnreadNotifications(testUser.getId(), 5);

    // Act: trigger the job via admin endpoint
    given().spec(adminSpec).post("/admin/jobs/send-digest").then().statusCode(200);

    // Assert: check the email was queued (via mock email service)
    List<Email> emails = EmailHelper.getEmailsFor(testUser.getEmail());
    Assert.assertEquals(emails.size(), 1);
    Assert.assertTrue(emails.get(0).getSubject().contains("Your daily digest"));
}
```

3. **Mock time for time-dependent jobs:**
   - If the cron runs at midnight: test environments should allow setting the system clock
   - Or expose a parameter: `POST /admin/jobs/run?date=2026-01-01`

4. **Log assertions:**
   - Check job logs for success/failure messages

5. **Database assertions:**
   - Before/after snapshot of the table the job modifies

---

## S17. How do you test an API that calls another external service?

**Answer:**

**The problem:** You can't control the external service in tests. It may be rate-limited, cost money, or be unavailable in test environments.

**Solution: Mock the external service.**

**Option 1 — WireMock (Java):**
```java
// Stub the external service response
stubFor(post(urlEqualTo("/external/payment"))
    .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{ \"status\": \"APPROVED\", \"transactionId\": \"TX123\" }")));

// Your API calls the stubbed external service
given().body(paymentRequest).post("/api/checkout")
    .then().statusCode(200).body("status", equalTo("SUCCESS"));
```

**Option 2 — Backend configuration:**
- Request the team to configure a mock service URL for the test environment (e.g., mock Stripe/PayPal)
- Many payment providers offer sandbox environments specifically for testing

**Option 3 — Contract testing (Pact):**
- Define the contract (expected request/response) between your service and the external one
- Run contract tests without calling the real external service

**What to test with mocks:**
- Happy path: external service returns success
- Failure path: external service returns 500, 401, or times out
- Slow response: external service takes 30 seconds (test your timeout handling)

---

## S18. The database is unavailable. How do you continue testing?

**Answer:**

**Immediate assessment:**
- Is it temporary? (maintenance window, restart) — wait and monitor
- Is it a blocking environment issue? — escalate to the infrastructure team

**Continue testing without the database:**

1. **API tests with mocked data:**
   - Use WireMock to mock the API layer — you don't need the real DB if the API is stubbed

2. **Front-end/UI testing with mock API:**
   - Tools like MSW (Mock Service Worker) for Playwright or Cypress intercepts can replace real API calls

3. **Shift to non-DB-dependent tests:**
   - Reorder test execution: run unit tests, API contract tests, static analysis, accessibility tests

4. **Test data preparation:**
   - Use the downtime productively: write test data scripts, write new test cases, review and refactor existing tests

5. **Use a local/embedded database:**
   - For integration testing: H2 (in-memory) can replace PostgreSQL/MySQL in a pinch

6. **Document the gap:**
   - Flag to the team that DB-dependent tests were not run in this cycle — this is a risk that needs to be addressed before release

---

## S19. How do you test a login feature that uses CAPTCHA?

**Answer:**

**CAPTCHA is designed to block automation — you work around it at the environment level:**

**Option 1 — Disable CAPTCHA in test environment (best approach):**
- Request the development team to add a config flag: `CAPTCHA_ENABLED=false` for non-production environments
- This is standard practice — CAPTCHA is a production-only safeguard

**Option 2 — CAPTCHA bypass via config/header:**
- Some CAPTCHA implementations accept a special header or test key in non-prod:
  - Google reCAPTCHA: use the test key `6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI` which always passes

**Option 3 — API-based login (bypass UI):**
- Test authentication via the API endpoint directly — CAPTCHAs are typically only on the UI layer
- Your automation gets a token from the API and uses it for subsequent tests

**What to test manually:**
- CAPTCHA rendering (appears when expected)
- CAPTCHA validation (wrong answer fails login)
- CAPTCHA accessibility (alternative text, audio option)

**What NOT to do:**
- Never use third-party CAPTCHA-solving services in automation — it's a policy and ethics violation

---

## S20. Automation passes but users report the feature is broken. What happened?

**Answer:**

This is one of the most important QA lessons: **green tests don't mean the feature works — they mean the tests pass.**

**Possible reasons:**

1. **Tests are testing the wrong thing:**
   - Tests check that a button exists but not that clicking it actually does something meaningful
   - Fix: review assertions — are they validating the business outcome, not just UI presence?

2. **Tests use test data that doesn't match production:**
   - Automation uses `admin@test.com` — works fine. Real users use OAuth with company email — broken.
   - Fix: test with realistic data types and user profiles

3. **Tests cover the happy path, users hit edge cases:**
   - Automation covers the 10-step golden flow. Users have incomplete profiles, old browsers, slow networks.
   - Fix: exploratory testing in addition to automation

4. **Production-specific configuration:**
   - Certain feature flags, CDN, WAF rules, or payment gateway configs only exist in production
   - Fix: ensure test environment mirrors production as closely as possible

5. **Race conditions or timing:**
   - Automation has generous waits. Real users on slow connections don't.

6. **Test environment vs production data:**
   - The feature works on a fresh DB but breaks on production's 5-year-old data

**Action:** Review the failed reports, identify the gap, add tests that cover the failing scenario, and do exploratory testing on production-like data.

---

## S21. How do you test a payment gateway?

**Answer:**

**Never test with real payment credentials. Always use sandbox/test environments.**

**Test categories:**

1. **Successful payment:**
   - Use the gateway's test card numbers (e.g., Stripe: `4242 4242 4242 4242`, any future date, any CVV)
   - Verify: confirmation page appears, order status updates to "PAID", confirmation email sent, order record in DB

2. **Declined card scenarios:**
   - Insufficient funds: `4000 0000 0000 9995` (Stripe)
   - Card declined: `4000 0000 0000 0002`
   - Verify: appropriate error message to user, order NOT created in DB

3. **3D Secure / authentication:**
   - Test cards that trigger 3DS challenge
   - Verify the authentication popup appears and the flow completes correctly

4. **Edge cases:**
   - Expired card date
   - Invalid CVV
   - International cards (different currencies)
   - Payment timeout (gateway takes too long)

5. **Refund:**
   - Process a refund and verify funds are returned and order status updates

6. **What to check via API:**
   - Correct amount was charged (not $0, not double-charged)
   - Idempotency: submitting the form twice doesn't charge twice

7. **Security checks:**
   - Card data is not stored in your database or logs
   - Payment page uses HTTPS
   - No card numbers visible in URL or server logs

---

## S22. How do you test mobile push notifications?

**Answer:**

**Push notifications are hard to automate — a hybrid approach works best.**

**Manual testing:**
- Device setup: use a real device (or emulator) with the app installed and notifications permitted
- Trigger scenarios: user receives a message, an order ships, a promotion is available
- Verify: notification appears with correct title, body, icon; tapping the notification opens the correct screen in the app

**API/backend testing (most testable layer):**
- Test the API that triggers the notification: `POST /notifications` returns 200 with notification ID
- Verify the notification payload is correctly formed (title, body, data, target device token)
- Test targeting: correct users receive the notification (not all users, not wrong users)

**Automated testing approaches:**
- Firebase Test Lab: can run app automation including notification flows on real devices in the cloud
- Appium + custom plugin: can intercept notifications on Android/iOS emulators
- Mock notification service: stub FCM/APNs in tests and verify correct payloads were sent

**Negative cases:**
- Notification when app is in foreground vs background vs killed
- User has notifications disabled — verify no error in the backend
- Invalid device token — verify graceful handling (token cleanup in DB)

---

## S23. How do you test a file upload feature end-to-end?

**Answer:**

**Functional testing:**

```java
// Selenium — file upload via sendKeys to input[type=file]
WebElement uploadInput = driver.findElement(By.cssSelector("input[type='file']"));
uploadInput.sendKeys("/absolute/path/to/test-file.pdf");
driver.findElement(By.id("uploadBtn")).click();
Assert.assertTrue(driver.findElement(By.id("success-msg")).isDisplayed());
```

**What to test:**

1. **Valid file upload:**
   - Supported format (PDF, PNG, DOCX as required)
   - Within size limit (e.g., file under 10MB)
   - Verify: file appears in the UI, can be downloaded, metadata saved correctly

2. **Invalid file types:**
   - Upload an `.exe` or unsupported format
   - Verify: clear error message, file NOT saved

3. **File size limit:**
   - Upload a file just under the limit (should succeed)
   - Upload a file just over the limit (should fail with "File too large")

4. **Edge cases:**
   - Empty file (0 bytes)
   - File with special characters in the name: `my file (2) — final.pdf`
   - Very long filename
   - Duplicate upload: uploading the same file again

5. **API testing:**
   ```java
   given()
     .multiPart("file", new File("src/test/resources/test.pdf"))
     .post("/api/upload")
   .then()
     .statusCode(201)
     .body("fileUrl", notNullValue());
   ```

6. **Security:**
   - Upload a file disguised as an image but containing malicious content
   - Verify the server validates actual file content, not just extension

---

## S24. How do you test an autocomplete/search feature?

**Answer:**

**Functional tests:**

1. **Basic search:**
   - Type 2+ characters and verify suggestions appear
   - Verify the suggestions are relevant to the input

2. **Minimum character trigger:**
   - Type 1 character — do suggestions appear? (usually requires 2-3)
   - Type 0 characters (clear input) — do suggestions disappear?

3. **Keyboard navigation:**
   - Arrow Down/Up to navigate suggestions
   - Enter to select a suggestion
   - Escape to close the dropdown

4. **No results state:**
   - Type something with no match: "zzzxxx"
   - Verify "No results found" message (not a blank dropdown)

5. **Debounce behaviour:**
   - Type rapidly — verify only one API call fires (not one per keypress)
   - Check the Network tab in DevTools

6. **API testing:**
   ```java
   given().queryParam("q", "lap").get("/api/search")
   .then().statusCode(200).body("results", not(empty()))
   .body("results[0].name", containsString("lap"));
   ```

7. **Edge cases:**
   - Special characters: `<script>`, `%`, `&`
   - Very long input (200+ characters)
   - Unicode/emoji input
   - Case insensitivity: "LAPTOP" should return the same as "laptop"

8. **Performance:**
   - Response should appear within 300ms of typing (feels instant to the user)

---

## S25. How do you test a pagination feature?

**Answer:**

1. **First page:**
   - Correct number of items per page (e.g., 10)
   - "Previous" button disabled or hidden
   - "Next" button enabled

2. **Navigate forward:**
   - Click "Next" — verify page 2 loads with the correct 10 items (items 11-20)
   - Page indicator shows "Page 2 of X"

3. **Navigate backward:**
   - From page 3, click "Previous" — verify page 2 loads correctly

4. **Last page:**
   - "Next" button disabled or hidden
   - Correct number of remaining items (may be less than page size)

5. **Direct page input (if available):**
   - Enter a valid page number — navigates correctly
   - Enter 0 or a negative number — validation error
   - Enter a page beyond the total — appropriate message

6. **Total count:**
   - Verify the total item count displayed matches the actual total in the DB/API

7. **API testing:**
   ```java
   given().queryParam("page", 1).queryParam("size", 10).get("/api/products")
   .then().statusCode(200)
   .body("page", equalTo(1))
   .body("content.size()", equalTo(10))
   .body("totalElements", greaterThan(0));
   ```

8. **Edge cases:**
   - Only one page of data — no pagination controls shown (or both Next/Previous disabled)
   - Zero results — verify empty state message

---

## S26. How do you test a date picker?

**Answer:**

1. **Open/close:**
   - Click the date field — picker opens
   - Click outside or press Escape — picker closes

2. **Select a valid date:**
   - Select today's date — populates the field correctly in the expected format (DD/MM/YYYY, etc.)
   - Select a past date — works
   - Select a future date — works (unless the field restricts to past dates)

3. **Restriction testing:**
   - If field is "date of birth": future dates should be disabled/blocked
   - If field is "appointment date": past dates should be blocked
   - Min/max date constraints

4. **Keyboard navigation:**
   - Tab into the date field, enter date manually using keyboard
   - Arrow keys to navigate between months

5. **Date range picker:**
   - Start date cannot be after end date
   - Selecting a new start date clears/resets end date if invalid

6. **Automation approach:**
   ```java
   // Safer to clear and type directly than to click through the calendar
   WebElement dateField = driver.findElement(By.id("dateInput"));
   dateField.clear();
   dateField.sendKeys("25/12/2026");
   ```

7. **Edge cases:**
   - Leap year: 29 Feb 2024 (valid), 29 Feb 2025 (invalid)
   - Timezone boundary: if you're in UTC+5:30, what "today" means
   - Month end dates: 31st January, 30th November

---

## S27. How do you test a rich text editor?

**Answer:**

Rich text editors (like Quill, TinyMCE, CKEditor) are complex — they use contenteditable or iFrames.

**Functional tests:**

1. **Text formatting:**
   - Type text, select it, apply Bold/Italic/Underline — verify formatting in the rendered output
   - Apply Heading 1, Heading 2 — verify the HTML structure

2. **Lists:**
   - Create a bullet list, ordered list — verify correct HTML is generated
   - Tab to indent list items, Shift+Tab to outdent

3. **Links:**
   - Insert a hyperlink — verify URL is correct and opens in the right target (new tab/same tab)

4. **Images:**
   - Insert an image via URL or upload — verify it renders
   - Resize the image within the editor

5. **Content persistence:**
   - Type content, save, reload the page — verify the content is preserved with formatting intact

6. **Automation approach:**
   - Editors in iFrames: `driver.switchTo().frame("tinymce_editor_iframe")`
   - Editors with contenteditable: use JavaScript to set content
   ```java
   JavascriptExecutor js = (JavascriptExecutor) driver;
   js.executeScript("document.querySelector('.ql-editor').innerHTML = '<p>Test content</p>'");
   ```

7. **Edge cases:**
   - Paste plain text vs paste rich text from Word — formatting behaviour
   - Pasting HTML directly
   - Very long content (10,000 characters)
   - Special characters and unicode

---

## S28. How do you test a drag-and-drop feature?

**Answer:**

Drag-and-drop is one of the hardest things to automate reliably.

**Manual testing:**
- Drag item from source to valid target — verify item moves/copies correctly
- Drag to invalid area — item snaps back to original position
- Drag and drop ordering in a list — verify order is persisted

**Selenium approach (Actions class):**
```java
WebElement source = driver.findElement(By.id("dragItem"));
WebElement target = driver.findElement(By.id("dropZone"));

Actions actions = new Actions(driver);
actions.dragAndDrop(source, target).perform();
// Or more reliable:
actions.clickAndHold(source).moveToElement(target).release().perform();
```

**JavaScript approach (for stubborn elements):**
```java
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript(
    "function simulateDragDrop(srcEl, dstEl) { " +
    "  var dt = new DataTransfer(); " +
    "  srcEl.dispatchEvent(new DragEvent('dragstart', {dataTransfer: dt, bubbles: true})); " +
    "  dstEl.dispatchEvent(new DragEvent('drop', {dataTransfer: dt, bubbles: true})); " +
    "  srcEl.dispatchEvent(new DragEvent('dragend', {dataTransfer: dt, bubbles: true})); " +
    "} " +
    "simulateDragDrop(arguments[0], arguments[1]);",
    source, target);
```

**Playwright (more reliable):**
```typescript
await page.locator('#dragItem').dragTo(page.locator('#dropZone'));
```

---

## S29. How do you test a feature behind a feature flag?

**Answer:**

Feature flags allow features to be turned on/off without a deployment. Testing requires testing both states.

**Testing approach:**

1. **Feature flag ON (feature enabled):**
   - Verify the feature is visible and functional
   - All acceptance criteria met

2. **Feature flag OFF (feature disabled):**
   - Verify the feature is not visible (no broken UI, no console errors)
   - Old behaviour still works correctly
   - Verify there are no residual UI elements (e.g., a button that's visible but leads nowhere)

3. **Flag targeting:**
   - If the flag is enabled for a specific user group (e.g., beta users): test with beta user account (flag ON) and regular user account (flag OFF)
   - Verify no data leakage between user groups

4. **Implementation:**
   ```java
   // Config-driven — set the flag value per test
   @BeforeMethod
   public void enableFeature() {
       ApiHelper.setFeatureFlag("NEW_CHECKOUT", true);
   }

   @AfterMethod
   public void disableFeature() {
       ApiHelper.setFeatureFlag("NEW_CHECKOUT", false); // cleanup
   }
   ```

5. **Edge case — flag flip mid-session:**
   - User is mid-flow when the flag is turned off — what happens?

---

## S30. How do you test performance of an API that must handle 1000 concurrent users?

**Answer:**

Performance testing requires a dedicated tool — not RestAssured or Selenium.

**Tool options:**
- **k6** (recommended — JavaScript-based, free, CI-friendly)
- **Apache JMeter** (GUI-based, widely used in enterprise)
- **Gatling** (Scala-based, good for developer teams)
- **Locust** (Python-based)

**k6 example:**
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 1000,          // 1000 virtual users
  duration: '60s',    // for 60 seconds
  thresholds: {
    http_req_duration: ['p(95)<500'],   // 95% of requests must complete in < 500ms
    http_req_failed: ['rate<0.01'],     // error rate must be < 1%
  },
};

export default function () {
  const res = http.get('https://api.example.com/products');
  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(1);
}
```

**What to measure:**
- Response time: p50, p95, p99 (99th percentile is most telling)
- Throughput: requests per second
- Error rate: % of requests failing under load
- Resource usage: CPU, memory, DB connections on the server

**Test scenarios:**
- Ramp-up: gradually increase from 0 to 1000 users
- Steady state: 1000 users for 60 seconds
- Spike: sudden jump to 1000 users instantly
- Soak: 200 users for 4 hours (memory leaks show here)

---

## S31. How do you test data migration after a major database schema change?

**Answer:**

**Pre-migration:**
1. Take a snapshot of counts and key values from the current database
2. Document the expected transformation rules: "column X becomes columns A and B"

**Test the migration script in a staging environment:**
1. Restore production data (anonymised) to staging
2. Run the migration script
3. Verify:
   - Row counts match (no data lost): `SELECT COUNT(*) FROM users` before and after
   - Key data values preserved: spot-check 10-20 critical records manually
   - Foreign key relationships intact: no orphaned records
   - Nullable constraints honoured: no nulls where values are required
   - Indexes created: query performance is acceptable

**Automated data migration tests:**
```java
@Test
public void verifyUserCountAfterMigration() {
    int before = DbHelper.query("SELECT COUNT(*) FROM users_old");
    MigrationRunner.run("migrate-users.sql");
    int after = DbHelper.query("SELECT COUNT(*) FROM users_new");
    Assert.assertEquals(after, before, "User count should not change after migration");
}
```

**Application testing post-migration:**
- All existing user flows still work with the new schema
- New fields have correct default values
- Application does not throw DB errors when reading/writing

**Rollback plan:**
- Test the rollback script — can you go back to the pre-migration state if something goes wrong?

---

## S32. How do you handle a test environment that is constantly unstable?

**Answer:**

**Short term — work around instability:**
- Add retry logic to handle transient failures
- Skip DB-dependent tests if the DB is flapping — run API contract and unit tests instead
- Increase timeouts for slow environments
- Tag unreliable environment tests with `@Ignore("Environment unstable - QA-1234")` temporarily

**Medium term — reduce dependency on the shared environment:**
- Mock external dependencies (WireMock) so tests don't rely on the whole stack
- Use containerised environments (Docker Compose) for isolated, repeatable test runs
- Create a stable "test" environment snapshot that can be reset to a known state

**Long term — infrastructure fix:**
- Raise the instability as a formal blocker: document frequency of failures and impact on team velocity
- Work with DevOps/infrastructure to improve reliability (health checks, auto-restart, better monitoring)
- Advocate for environment-as-code (Terraform, Helm charts) so environments can be spun up reliably on demand

**Track the problem:**
- Log every environment incident with timestamp and duration
- Present data to management: "Our test environment was unavailable for 4 hours this week, blocking X hours of QA work"

---

## S33. You find a security vulnerability during regular testing. What do you do?

**Answer:**

**Immediately:**
1. Do NOT log it in the public JIRA board — security vulnerabilities need confidential handling
2. Note down the exact reproduction steps privately
3. Assess the severity: can it expose user data? allow unauthorised access? execute code remotely?

**Report through proper channels:**
- Notify your team lead or security champion directly (direct message, not a group chat)
- Your company may have a specific security incident process — follow it
- Provide: exact reproduction steps, affected endpoint or component, potential impact, screenshot/proof

**Do NOT:**
- Exploit the vulnerability beyond confirming it exists
- Share details outside the necessary people
- Post in public channels, Slack groups, or social media

**If there's no security process:**
- Escalate to your manager with the vulnerability details
- Suggest creating a high-severity/restricted JIRA ticket visible only to relevant engineers
- Advocate for a responsible disclosure process if one doesn't exist

**Common vulnerabilities QA finds:**
- SQL injection (input fields accepting `' OR 1=1`)
- Insecure direct object references (changing an ID in the URL to access another user's data)
- Exposed API endpoints without authentication
- Sensitive data in URL parameters or logs

---

## S34. How do you test an Angular single-page application?

**Answer:**

Angular SPAs present specific challenges for automation:

**Challenge 1 — Dynamic content:**
Angular updates the DOM asynchronously. Selenium may try to interact before Angular finishes rendering.

**Solution: use explicit waits consistently:**
```java
// Wait for Angular's HTTP calls to finish
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
wait.until(d -> ((JavascriptExecutor) d)
    .executeScript("return window.getAllAngularTestabilities && " +
                   "window.getAllAngularTestabilities().every(t => t.isStable())"));
```

**Challenge 2 — Client-side routing:**
- URL changes happen without full page loads
- Use `wait.until(ExpectedConditions.urlContains("/dashboard"))` instead of waiting for new page load

**Challenge 3 — Material Design components:**
- Angular Material dropdowns, date pickers, and dialogs have specific interaction patterns
- Use the visible text or `mat-option` selectors rather than native select elements

**Playwright approach (better for SPAs):**
```typescript
// Playwright auto-waits for network idle after navigation
await page.goto('/dashboard');
await page.waitForLoadState('networkidle');
await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
```

**What to specifically test in SPAs:**
- Route navigation: going back/forward in browser history
- Deep linking: opening a bookmarked URL directly
- State on refresh: data persists or is correctly re-fetched

---

## S35. How do you test an API that returns different responses based on user role?

**Answer:**

Test each role explicitly, verifying both access and data boundaries.

**Roles to test (typical):**
- Admin, Manager, User, Guest/Unauthenticated

**For each role, verify:**

1. **Correct data access:**
   - Admin: sees all records
   - User: sees only their own records
   - Guest: gets 401 Unauthorized

2. **Correct HTTP status:**
   ```java
   // Regular user cannot access admin endpoint
   given().header("Authorization", "Bearer " + userToken)
       .get("/api/admin/users")
   .then()
       .statusCode(403); // Forbidden
   ```

3. **Field-level access (data leakage):**
   - Admin response includes `salary`, `internalNotes`
   - User response must NOT include these fields even if requested
   ```java
   given().header("Authorization", "Bearer " + userToken)
       .get("/api/profile/1")
   .then()
       .statusCode(200)
       .body("salary", nullValue()) // field should not be returned for non-admin
       .body("name", notNullValue());
   ```

4. **CRUD operations:**
   - User can POST their own data, cannot DELETE others'
   - Manager can approve but not delete
   - Admin can do everything

5. **Token generation:**
   ```java
   String adminToken = AuthHelper.getToken("admin@test.com", "pass");
   String userToken  = AuthHelper.getToken("user@test.com", "pass");
   ```

---

## S36. Your manager asks for test coverage metrics. What do you measure?

**Answer:**

Coverage means different things at different levels. Don't just say "line coverage."

**Useful metrics to report:**

1. **Requirement coverage:**
   - Of the X user stories in this sprint, Y% have at least one test case
   - Track in Zephyr Scale or a coverage matrix

2. **Test case execution rate:**
   - X of Y planned test cases were executed this cycle
   - X passed, Y failed, Z blocked/not run

3. **Automation coverage:**
   - Of the X manual test cases, Y% have automated equivalents
   - Automation pass rate: Z% of automated tests are consistently passing

4. **Defect metrics:**
   - Bugs found in testing vs bugs found in production (test effectiveness)
   - Defect density by feature area
   - Open vs closed defect counts

5. **Code coverage (if accessible):**
   - Line/branch coverage from unit tests (developers usually own this, but QA can track it)
   - SonarQube dashboard

**What NOT to treat as a quality signal:**
- 100% test automation doesn't mean the product is good if the tests assert the wrong things
- High pass rate on a stale test suite is misleading

---

## S37. How do you convince a developer to fix a "minor" bug?

**Answer:**

**Lead with user impact, not QA process:**

"This bug causes [specific user action] to fail for [user group]. Even if the workaround is [X], users who don't know the workaround will think the product is broken."

**Provide context:**
- Show the user journey where the bug appears
- Quantify: "This affects every user who does X, which is our most common workflow"
- Compare: "This is similar to bug QA-456, which became a P1 production issue in March"

**Categorise correctly:**
- Is it really minor? Re-assess severity honestly: cosmetic bugs (truly minor), functional gaps (not minor)
- If it affects accessibility, security, or data integrity — it's never truly minor

**Offer to help:**
- Provide a clear, minimal reproduction case — reduces the developer's effort to fix it
- Offer to test the fix immediately so it doesn't stay open long

**Escalate respectfully if needed:**
- If the bug is a genuine risk and the developer still deprioritises it: involve your QA lead or PM
- "I want to make sure we're aligned on the risk of releasing with this bug — can we loop in [PM] to decide?"

---

## S38. How do you test backward compatibility of an API?

**Answer:**

Backward compatibility means: existing clients (apps, integrations) still work when the API changes.

**What breaks backward compatibility:**
- Removing a field from the response
- Changing a field's data type (string → integer)
- Changing a required field to a different name
- Removing an endpoint
- Changing an HTTP method (GET → POST)
- Making an optional field required

**Testing approach:**

1. **Contract testing (Pact):**
   - Consumer (client app) defines what it needs from the API
   - Provider (API) verifies its responses still satisfy the consumer contract
   - Run Pact verification as part of CI — fails immediately if a contract is broken

2. **Version-specific test suites:**
   - Maintain tests for the current API version AND the previous version
   - Run tests against both versions in CI
   ```java
   // Test that v1 responses still match old schema
   given().get("/api/v1/users/1").then()
       .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/user-v1-schema.json"));
   ```

3. **Changelog verification:**
   - Review every API change against a "breaking change checklist"
   - Non-breaking: adding a new optional field, adding a new endpoint, deprecating (not removing) an endpoint

4. **Consumer team communication:**
   - Notify downstream teams of upcoming changes
   - Provide a migration guide and a deprecation period

---

## S39. You discover that 40% of your automation suite is flaky. What do you do?

**Answer:**

40% flakiness is a crisis — the suite has lost its value. Act systematically:

**Step 1 — Quantify and categorise:**
- Tag all flaky tests: `@Test(groups = "flaky")`
- Analyse failures: is it timing? test data? environment? assertion issues?
- Categorise: network flakiness, timing, shared state, environment instability, genuine app bug

**Step 2 — Stop the bleed:**
- Exclude flaky tests from the PR gate (they're causing more pain than value)
- Run them in a separate nightly job as "informational only"
- Document each flaky test with its reason and JIRA ticket

**Step 3 — Fix by category:**
- **Timing issues:** replace all `Thread.sleep()` with proper waits, increase timeout for CI
- **Shared state:** ensure each test sets up and tears down its own data
- **Locators:** replace fragile locators (XPath with positions) with stable ones (data-testid)
- **Environment:** add retry for known environment issues; fix the environment

**Step 4 — Prevent recurrence:**
- Set a "no new flaky tests" standard — if a test fails intermittently in PR review, it doesn't merge until stable
- Track flakiness rate as a metric and report it
- Regular flakiness review sessions (monthly sprint goal: reduce flakiness by X%)

---

## S40. How do you test a feature that involves third-party OAuth login (Google/GitHub)?

**Answer:**

**The challenge:** You cannot automate Google/GitHub OAuth UI flows — they actively block bots, and their UI can change anytime.

**Strategy 1 — Bypass OAuth in tests (best):**
- Request developers to add a backdoor endpoint for test environments: `POST /auth/test-token` that returns a valid session for a test user without OAuth
- This is completely safe — test environment only, never in production

**Strategy 2 — API-level token injection:**
- If your system issues its own JWT after OAuth, mock the OAuth step and directly inject a valid JWT:
```java
// Exchange a known test user's refresh token for an access token
String token = OAuthHelper.getTestUserToken("test-user@gmail.com");
// Use that token for all subsequent test requests
```

**Strategy 3 — Real OAuth for manual/exploratory tests:**
- Maintain test Google/GitHub accounts (e.g., `qa-test-01@gmail.com`) for manual exploration
- Dedicate these accounts to testing only — never use real personal accounts

**What to actually test:**
- Successful OAuth login creates a user session
- First-time OAuth user account is provisioned correctly
- OAuth login for an existing user links to the correct account
- OAuth logout clears the session
- Revoked OAuth token is handled gracefully (user must re-authenticate)

---

## S41. How do you test a microservices architecture where 5 services interact?

**Answer:**

**Testing at multiple levels:**

1. **Unit tests (within each service):**
   - Each service team owns unit tests for their logic
   - QA audits coverage and quality

2. **Contract tests (between services):**
   - Use Pact or Spring Cloud Contract
   - Consumer service defines the expected API contract
   - Provider service proves it meets the contract
   - This catches integration breaks without needing all services running

3. **API-level integration tests (2-3 services together):**
   - Test the interaction between specific pairs of services
   - Use Docker Compose to spin up just the services involved
   ```yaml
   services:
     user-service: ...
     order-service: ...
     # stub payment-service with WireMock
   ```

4. **End-to-end tests (all services):**
   - Only for critical business flows (checkout, user registration)
   - Run against the full staging environment
   - Accept these are slower and more fragile — keep the count small

5. **Observability testing:**
   - Verify distributed tracing works (Jaeger, Zipkin): a request can be traced through all 5 services
   - Verify correct error propagation: if service C fails, service A returns a meaningful error to the user

6. **Chaos testing:**
   - Kill one service — verify the others degrade gracefully (circuit breaker pattern)

---

## S42. The test data you need doesn't exist in the environment. What do you do?

**Answer:**

**Option 1 — Create it via API (fastest and cleanest):**
```java
@BeforeMethod
public void createTestData() {
    product = ApiHelper.createProduct("Test Laptop", 999.99);
    user    = ApiHelper.createUser("test" + System.currentTimeMillis() + "@test.com");
}

@AfterMethod
public void cleanUp() {
    ApiHelper.deleteProduct(product.getId());
    ApiHelper.deleteUser(user.getId());
}
```

**Option 2 — Create it via the UI setup:**
- If there's no API, use Selenium to set up data through the application's admin screens
- This is slower but valid

**Option 3 — Database seeding:**
```sql
-- seed-data.sql
INSERT INTO products (name, price, stock) VALUES ('Test Product', 50.00, 100);
```
```java
@BeforeClass
public void seedDatabase() {
    DbHelper.executeScript("src/test/resources/sql/seed-data.sql");
}
```

**Option 4 — Request data setup from the dev/DevOps team:**
- If the data requires special permissions or complex setup, raise a task for the team
- Clearly describe what data is needed and why

**Option 5 — Mock the data:**
- Use WireMock to return mock responses that include the data you need
- Good for testing the UI layer without needing real backend data

---

## S43. How do you test an app that shows different content based on location/timezone?

**Answer:**

**Browser/Playwright timezone override:**
```typescript
// playwright.config.ts — set timezone per test context
const context = await browser.newContext({
  timezoneId: 'America/New_York',
  locale: 'en-US',
  geolocation: { latitude: 40.7128, longitude: -74.0060 }, // New York
  permissions: ['geolocation'],
});
```

**Selenium — set timezone via browser options:**
```java
ChromeOptions options = new ChromeOptions();
// Use Selenium Grid with timezone set at the node level
// Or set timezone via JavaScript injection
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("Date.prototype.getTimezoneOffset = function() { return -330; }"); // IST
```

**API level — pass timezone in headers:**
```java
given().header("X-Timezone", "America/New_York")
       .get("/api/events")
.then()
       .body("events[0].time", containsString("EST"));
```

**What to test:**
- Dates displayed in user's local timezone (not server timezone)
- Date comparisons (yesterday/today/tomorrow) correct per timezone
- Location-restricted content shown only for correct region (if geolocation-based)
- Currency, date format, and number format change correctly per locale

**Edge cases:**
- Daylight Saving Time transitions (clocks go forward/back)
- UTC midnight boundary (event created at 23:00 UTC may be "next day" in UTC+5:30)
- Leap years affecting date calculations

---

## S44. How do you test a video streaming feature?

**Answer:**

Video streaming is largely manual and performance-focused, with limited automation.

**Functional tests (manual):**
1. Video loads and plays within acceptable time (< 3 seconds to start)
2. Correct video is served for the user's account and subscription level
3. Playback controls work: play, pause, seek, volume, full screen
4. Quality selection works (360p, 720p, 1080p)
5. Subtitles/captions appear and are synchronised
6. Video completes and triggers end-state (related videos, replay button)
7. Unauthorized users cannot access premium content

**Performance testing:**
- Measure time to first frame (TTFF): should be < 3 seconds
- Test adaptive bitrate: degrade network speed and verify quality auto-adjusts
- Test concurrent streams: does the system handle 1000 simultaneous viewers?

**API testing:**
- `GET /api/video/stream-url` returns a valid signed URL for authorised user
- Signed URL expires after expected duration
- Unauthorised user receives 403

**Network conditions:**
- Throttle to 3G and verify adaptive bitrate kicks in
- Simulate network dropout and verify graceful recovery

**Browser automation limitation:**
- Playwright/Selenium cannot validate actual video playback quality — only the player controls and UI state
- Use performance monitoring tools (Datadog, New Relic) for streaming metrics in production

---

## S45. How do you approach testing when the ACs (acceptance criteria) are ambiguous?

**Answer:**

Ambiguous ACs are a risk — you test the wrong thing and the feature fails review.

**Before testing starts:**

1. **Identify the ambiguity explicitly:**
   - "The AC says 'user should see their orders' — does this mean all orders ever, or just the last 30 days? Sorted by? With what columns?"

2. **Raise a question in the ticket:**
   - Comment in JIRA, tagging the BA and developer: "Clarifying question for QA: [specific question]. Without this, I'll assume [assumption] — please confirm or correct."

3. **Three Amigos session (best practice):**
   - Before dev starts: BA, developer, and QA review the story together
   - QA's job is to ask "what about this edge case?" — this clarifies ACs before coding starts

4. **Document your assumptions:**
   - If you cannot get clarification in time: write your assumptions in the ticket and test against them
   - "I tested on the assumption that orders are sorted by date descending. If this is incorrect, please update the AC."

5. **During testing — if still ambiguous:**
   - Test the most conservative interpretation (most restrictive) AND the most permissive one
   - Report both: "When sorted ascending, the result is X. When sorted descending, the result is Y. Which is expected?"

---

## S46. Regression suite breaks after every release. How do you fix this long-term?

**Answer:**

Frequent regression breakage means the tests are brittle. The root causes are usually:

**Root cause analysis:**
1. Locators tied to text that changes (button labels, error messages)
2. Tests that depend on specific data that gets modified between releases
3. No `data-testid` attributes — developers rename CSS classes, breaking tests
4. Tests rely on test ordering or shared state

**Long-term fixes:**

1. **Stable selectors agreement with developers:**
   - Request `data-testid` attributes for all key interactive elements
   - These are test-only attributes — developers don't rename them during styling changes

2. **Data independence:**
   - Every test creates its own data, cleans it up after
   - No test relies on pre-existing data that can be modified

3. **Test-code review process:**
   - Require a QA review when developers make changes that affect UI structure or API contracts
   - PR checklist: "Does this change affect existing automated tests?"

4. **Regular maintenance sprints:**
   - Dedicate 10-20% of QA sprint capacity to automation maintenance
   - Don't wait for the suite to break — proactively review after each release

5. **Flakiness tracking:**
   - If a test breaks more than twice, it gets investigated — not just re-run

---

## S47. How do you test an email notification feature?

**Answer:**

**The challenge:** Real email delivery is slow, hard to intercept, and unreliable in test environments.

**Solution — use a test email service:**

1. **Mailosaur or Mailtrap (best for automation):**
   - Captures all emails sent to a test domain in an inbox you can query via API
   ```java
   MailosaurClient mailosaur = new MailosaurClient("API_KEY");
   Message email = mailosaur.messages.get(serverId,
       new SearchCriteria().withSentTo("user@" + serverId + ".mailosaur.net"));
   Assert.assertEquals(email.subject(), "Order confirmed");
   Assert.assertTrue(email.text().body().contains("Order #12345"));
   ```

2. **Greenmail (embedded SMTP for Java tests):**
   - Spin up a local SMTP server for tests, no external service needed

**What to test:**
- Email is sent to the correct recipient
- Subject line is correct
- Body contains required information (order number, username, link)
- Links in the email work (click-through and verify destination)
- Email is NOT sent when it shouldn't be (e.g., no email for draft orders)
- Unsubscribe link works

**Negative cases:**
- Invalid email address: system handles gracefully, logs error, does not crash
- Email template rendering failure: system logs the error, doesn't silently swallow it

---

## S48. How do you test a real-time chat feature?

**Answer:**

Real-time chat uses WebSockets or Server-Sent Events — different from standard HTTP APIs.

**Manual testing:**
1. Open two browser windows (two users) and verify messages appear in real-time
2. Test delivery acknowledgement (sent → delivered → read receipts)
3. Typing indicator: appears for the other user while you type
4. Message history: reloading the page shows all previous messages
5. File/image sharing within chat
6. Notification when a new message arrives while the chat window is not focused

**Automation with Playwright (WebSocket support):**
```typescript
// Playwright can intercept WebSocket messages
const wsMessages: string[] = [];
page.on('websocket', ws => {
    ws.on('framereceived', frame => wsMessages.push(frame.payload.toString()));
});

// Send a message as user1
await page1.getByRole('textbox').fill('Hello from user1');
await page1.keyboard.press('Enter');

// Verify user2 received it
await expect(page2.locator('.message').last()).toContainText('Hello from user1');
```

**Load testing:**
- Simulate 500 concurrent WebSocket connections using k6 or Artillery
- Verify messages still deliver within SLA (< 1 second)

**Edge cases:**
- Long messages (10,000 characters)
- Messages with XSS payload: `<script>alert('xss')</script>` — must be escaped
- Network reconnection: disconnect and reconnect — messages buffered during disconnect arrive
- Concurrent sends: both users send at the same time — no messages lost

---

## S49. How do you handover your automation framework when you leave a project?

**Answer:**

A good handover means the framework keeps delivering value after you leave.

**Documentation (write as you go, not on exit day):**
- README.md: setup instructions, how to run locally, how to run in CI, how to add a new test
- Framework architecture explanation: what each layer does, why you chose it
- Known issues and workarounds: "the datepicker needs a 2-second wait due to animation — see Q26"
- Test data guide: how to create test data, what data already exists, what gets cleaned up

**Knowledge transfer sessions:**
- Live walkthrough with the incoming QA: run the framework, show how to add a new page object and test
- Record a walkthrough video for reference
- Pair on writing 2-3 new tests together so the new QA has hands-on experience

**Code quality:**
- Ensure all tests are passing at handover time — don't leave a broken suite
- Fix or explicitly document all known flaky tests
- Review code for clarity: rename cryptic variables, add comments on non-obvious logic

**CI/CD access:**
- Ensure the new owner has all necessary access: repo, CI credentials, test environment logins
- Transfer ownership of any external accounts (Mailosaur, BrowserStack)

**Checklist:**
- [ ] README is up-to-date and accurate
- [ ] CI pipeline is green
- [ ] All env variables documented and accessible to the team
- [ ] No personal credentials stored in the repo
- [ ] New QA has run the suite successfully at least once

---

## S50. You have only 2 hours before a release. What do you test?

**Answer:**

With 2 hours, you cannot test everything. You test what matters most.

**Framework: risk-based prioritisation**

**First 30 minutes — smoke test (critical path):**
- Can users log in? (authentication is the gateway to everything)
- Can users complete the core business flow? (for e-commerce: search → add to cart → checkout → confirmation)
- For the Singer project: can users browse products, add to wishlist, checkout?

**Next 45 minutes — areas changed in this release:**
- Focus ONLY on what was changed/added in this release
- Don't retest areas that haven't changed — those are lower risk
- Verify the specific user stories in the release notes are working

**Next 30 minutes — high-risk areas:**
- Payment/checkout if not already covered (money is always P1)
- Authentication and permissions (security-sensitive)
- Any area that has had bugs in recent releases (historically unstable)

**Final 15 minutes — sanity check:**
- Check the browser console for JS errors on the main pages
- Quick scan of the UI on mobile viewport
- Verify no obvious data display issues on the home/main page

**Communicate:**
- Log what was tested and what was NOT tested
- "2-hour time-boxed smoke and release-delta testing completed. Full regression was not run — known risk. Recommend monitoring post-release for [X areas]."

**After release — monitor:**
- Watch error tracking (Sentry, Datadog) for the first 30 minutes post-release
- Be available for rapid response if a bug is reported
