# Technical Interview — Real-World Q&A | All Topics | Senior QA Engineer

> Every answer here is grounded in how things actually work on real projects — not textbook theory. Read each answer as if you are explaining it to a hiring manager who has seen engineers fail at this exact problem.

---

## TABLE OF CONTENTS

- [Section 1 — Selenium (10 Q&A)](#section-1--selenium)
- [Section 2 — API Testing (10 Q&A)](#section-2--api-testing)
- [Section 3 — TestNG (5 Q&A)](#section-3--testng)
- [Section 4 — Playwright (5 Q&A)](#section-4--playwright)
- [Section 5 — CI/CD & Quality (5 Q&A)](#section-5--cicd--quality)
- [Section 6 — Real-World Scenarios (5 Q&A)](#section-6--real-world-scenarios)

---

## SECTION 1 — Selenium

---

**Q1. Walk me through what happens when `driver.findElement()` can't find an element.**

When Selenium cannot locate an element using the locator you provided, it throws a `NoSuchElementException`. This happens immediately — Selenium does not wait by default. Here's the full failure chain on a real project:

1. `driver.findElement(By.id("submit-btn"))` is called
2. Selenium queries the DOM at that exact instant
3. If the element is absent, the exception fires and the test fails with a stack trace pointing to that line
4. If you have an implicit wait set (e.g. `driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))`), Selenium polls the DOM repeatedly for up to 10 seconds before giving up

**What goes wrong on real projects:**
- Dev changes the element ID from `submit-btn` to `submitButton` — your test breaks with `NoSuchElementException`
- Element exists in DOM but is inside an `<iframe>` — Selenium can't see it without switching frame context first
- Element is rendered by JavaScript after page load — your locator runs before the element exists

**Fix strategy:**
```java
// Step 1: Use explicit wait instead of implicit
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement button = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submit-btn")));

// Step 2: If inside an iframe
driver.switchTo().frame("iframeId");
WebElement button = driver.findElement(By.id("submit-btn"));
driver.switchTo().defaultContent(); // Always switch back

// Step 3: Check element actually exists in DOM
List<WebElement> elements = driver.findElements(By.id("submit-btn"));
if (!elements.isEmpty()) {
    elements.get(0).click();
}
```

**What to watch for:** Never mix implicit and explicit waits — they interact unpredictably and can cause your explicit wait to effectively double or behave inconsistently across browsers.

---

**Q2. Your test clicks a button but nothing happens — how do you debug it?**

This is one of the most frustrating Selenium problems because the test doesn't fail with an exception — it just does nothing. On real projects, this happens for several reasons.

**Step-by-step debug process:**

**Step 1 — Confirm the click actually landed**
```java
// Take a screenshot immediately after click
((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
```
If the page looks the same, the click did nothing. If the page changed but the test assertion is wrong, that's a different problem.

**Step 2 — Check if the element is truly visible and interactable**
```java
WebElement btn = driver.findElement(By.id("submit-btn"));
System.out.println("Displayed: " + btn.isDisplayed());
System.out.println("Enabled: " + btn.isEnabled());
System.out.println("Size: " + btn.getSize());
```
A button with size 0x0 or `display:none` will accept a `click()` call without throwing but nothing happens.

**Step 3 — Check if something is covering the element**
An overlay, modal, or cookie banner sitting on top of your button intercepts the click. Selenium clicks the element at its center coordinates — if another element is there, that element receives the click.

```java
// Use JavascriptExecutor to force the click directly on the element
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].click();", btn);
```

**Step 4 — Check browser console for JavaScript errors**
```java
LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
for (LogEntry entry : logs) {
    System.out.println(entry.getMessage());
}
```
A JS error triggered by a previous action can prevent the button from working.

**Step 5 — Add explicit wait before the click**
```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.elementToBeClickable(By.id("submit-btn"))).click();
```

**What could go wrong and you'd miss it:** If you use `isDisplayed()` to check visibility, note that it returns `true` even if the element is scrolled off screen — it just checks CSS display property. Use `elementToBeClickable` in explicit wait instead.

---

**Q3. A test passes 9/10 times — what do you do?**

A flaky test is one of the most damaging things in a test suite. When a test is intermittently failing, the team starts ignoring failures — and that's when real bugs slip through. Here's the real-world approach:

**Immediate action:**
1. Do NOT re-run until you have a theory. Blind re-runs mask the problem.
2. Run the test 20 times in isolation using a loop to reproduce the failure consistently.
3. Add timestamps and logs around every key step to see where it breaks.

**The five most common causes of flaky Selenium tests:**

| Cause | Symptom | Fix |
|---|---|---|
| Timing — no wait or wrong wait | Fails on slow CI machines, passes locally | Replace Thread.sleep with explicit waits |
| Test order dependency | Fails when run after a specific other test | Make tests independent, clear state in @BeforeMethod |
| Shared state (static data) | Fails when run in parallel | Use test-specific data, avoid shared test accounts |
| Stale element reference | StaleElementReferenceException on specific runs | Re-fetch element after page interaction |
| Network latency | Slower in CI, times out occasionally | Increase timeout, mock slow external calls |

**Practical debugging process:**
```java
// Wrap the flaky step in retry logic temporarily to confirm it's timing
for (int i = 0; i < 3; i++) {
    try {
        driver.findElement(By.id("dynamic-element")).click();
        break;
    } catch (StaleElementReferenceException e) {
        System.out.println("Attempt " + (i+1) + " failed: " + e.getMessage());
    }
}
```

**What I'd tell the team:** Quarantine the flaky test immediately — tag it with `@Ignore` or move it to a separate suite. A failing test that sometimes passes is worse than no test because it erodes trust in the entire suite.

---

**Q4. How do you handle a dropdown that is NOT a `<select>` element?**

Modern web apps almost never use native `<select>` dropdowns. They use custom components — div-based lists, Material UI dropdowns, PrimeNG overlays, Angular select components. These don't work with Selenium's `Select` class at all.

**The real-world approach:**

**Step 1 — Inspect the dropdown's actual HTML structure**
Open DevTools, right-click the dropdown, Inspect. Look for patterns like:
- A `<div class="dropdown-trigger">` that opens a list
- A `<ul>` or `<div>` container with list items that appears only when the trigger is clicked

**Step 2 — Interact with it as regular elements**
```java
// Click the trigger to open the dropdown
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement dropdownTrigger = wait.until(
    ExpectedConditions.elementToBeClickable(By.cssSelector(".p-dropdown-trigger"))
);
dropdownTrigger.click();

// Wait for the list panel to appear
WebElement dropdownPanel = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".p-dropdown-panel"))
);

// Find and click the desired option by its text
List<WebElement> options = dropdownPanel.findElements(By.cssSelector(".p-dropdown-item"));
for (WebElement option : options) {
    if (option.getText().equals("Option I Want")) {
        option.click();
        break;
    }
}
```

**Step 3 — For searchable dropdowns**
```java
// Some dropdowns have a search input inside
WebElement searchInput = dropdownPanel.findElement(By.cssSelector("input.p-dropdown-filter"));
searchInput.sendKeys("search term");
wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".p-dropdown-item"))).click();
```

**Common mistake on real projects:** Clicking the option before the panel animation finishes. The element exists in DOM but isn't clickable yet. Always use `elementToBeClickable`, not just `visibilityOfElementLocated`.

---

**Q5. WebDriver vs WebElement — what's the difference?**

This question tests whether you actually understand the Selenium object model.

**WebDriver** is the browser controller — it's your connection to the browser instance. You use it to:
- Navigate to URLs (`driver.get("https://example.com")`)
- Manage the browser window, tabs, cookies
- Set timeouts
- Find elements on the current page

**WebElement** represents a single HTML element on the page — a button, input, link, div. You use it to:
- Click (`element.click()`)
- Type text (`element.sendKeys("value")`)
- Read attributes (`element.getAttribute("class")`)
- Check state (`element.isDisplayed()`, `element.isEnabled()`)

**The real-world relationship:**
```java
// WebDriver finds elements, returns WebElement references
WebDriver driver = new ChromeDriver();            // This is WebDriver
driver.get("https://example.com");

WebElement searchBox = driver.findElement(By.name("q"));  // Returns WebElement
searchBox.sendKeys("Selenium");                   // Acting on WebElement
searchBox.submit();                               // Still WebElement

WebElement firstResult = driver.findElement(By.cssSelector("h3"));
System.out.println(firstResult.getText());        // Read from WebElement
```

**Key difference that matters in interviews:** WebDriver is the session-level object. WebElement is a snapshot reference — it becomes stale if the DOM is modified. This is why `StaleElementReferenceException` happens. WebDriver itself doesn't go stale; WebElement references do.

---

**Q6. Your PageFactory locators work but are slow — why and how to fix?**

This is a real production problem that many teams hit when they have large Page Object classes.

**Why it's slow:**

`PageFactory.initElements()` with `@FindBy` annotations uses lazy initialization — each `@FindBy` field is a proxy. The first time you access the field, Selenium calls `findElement()` live. The problem is when you call `PageFactory.initElements()` in the constructor, the proxy setup itself is cheap — BUT every field access triggers a fresh DOM lookup at the moment of access.

**The real issue:** If your page object has 20 `@FindBy` fields and you access all 20 in a test, that's 20 separate `findElement()` calls to the browser over WebDriver's wire protocol. Each call is a round-trip.

**Fix 1 — Only access locators you actually need**
Don't access every field in every test. Structure page objects so each method only touches the elements it needs.

**Fix 2 — Use explicit By locators and cache strategically**
```java
// Instead of PageFactory with @FindBy (proxied, every access = DOM call)
// Use By locators and find only when needed
private final By submitButtonLocator = By.id("submit-btn");

public void clickSubmit() {
    driver.findElement(submitButtonLocator).click(); // One call, only when needed
}
```

**Fix 3 — Use explicit waits on the specific element, not the whole page**
PageFactory doesn't wait — if the element isn't there, it fails. This causes both slowness (implicit wait polling) and unreliability.

```java
// Fast: find element only when action is called
public void submitForm() {
    new WebDriverWait(driver, Duration.ofSeconds(5))
        .until(ExpectedConditions.elementToBeClickable(submitButtonLocator))
        .click();
}
```

**Real-world outcome:** On one project I worked on, switching from PageFactory to pure `By` locators in page objects reduced average test execution time by ~20% because we eliminated unnecessary element lookups during page object initialization.

---

**Q7. How do you test a file download in Selenium?**

Selenium cannot interact with the browser's native download dialog. You need to bypass it entirely. Here's the approach used on real projects:

**Approach 1 — Configure the browser to download without dialog (most common)**
```java
// Chrome: configure download directory and disable dialog
Map<String, Object> prefs = new HashMap<>();
prefs.put("download.default_directory", "/tmp/downloads");
prefs.put("download.prompt_for_download", false);
prefs.put("download.directory_upgrade", true);
prefs.put("safebrowsing.enabled", true);

ChromeOptions options = new ChromeOptions();
options.setExperimentalOption("prefs", prefs);
WebDriver driver = new ChromeDriver(options);
```

**Then verify the download:**
```java
public boolean waitForFileDownload(String fileName, int timeoutSeconds) throws InterruptedException {
    File downloadDir = new File("/tmp/downloads");
    long end = System.currentTimeMillis() + (timeoutSeconds * 1000L);
    
    while (System.currentTimeMillis() < end) {
        File[] files = downloadDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(fileName) && !file.getName().endsWith(".crdownload")) {
                    return true; // .crdownload means still downloading
                }
            }
        }
        Thread.sleep(500);
    }
    return false;
}
```

**Approach 2 — Test the download URL directly (preferred for API-delivered files)**
```java
// Get the download link href
String downloadUrl = driver.findElement(By.id("download-link")).getAttribute("href");

// Use Java's HTTP client to download and verify
URL url = new URL(downloadUrl);
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
// Copy session cookies from WebDriver to the connection
// Verify response code 200 and Content-Type header
```

**What to verify beyond "file exists":**
- File size is not zero
- File extension matches expected (PDF, CSV, XLSX)
- For CSV/text files: read and verify content
- For PDFs: use Apache PDFBox to extract and assert text content
- Filename matches what was expected

---

**Q8. A test fails with `StaleElementReferenceException` — what caused it and how do you fix it?**

`StaleElementReferenceException` means your WebElement reference is pointing to a DOM node that no longer exists — it was removed and possibly re-created by a JavaScript framework re-render.

**What actually happens:**
1. You call `findElement()` — Selenium returns a reference (essentially a pointer) to a specific DOM node
2. JavaScript runs (React re-renders, Angular updates bindings, AJAX response triggers DOM change)
3. The original DOM node is destroyed and recreated — even if it looks identical on screen
4. Your old WebElement reference is now a dangling pointer — the next access throws `StaleElementReferenceException`

**Common real-world triggers:**
- Clicking something that triggers a page partial reload
- Sorting a table — the rows get destroyed and re-created
- Framework re-rendering (React, Angular, Vue) on state change
- AJAX updating a section of the page

**Fix 1 — Re-fetch the element each time you need it**
```java
// BAD: store and reuse reference
WebElement row = driver.findElement(By.cssSelector("tr:first-child"));
row.click(); // triggers re-render
row.getText(); // StaleElementReferenceException — row was re-created

// GOOD: re-fetch after any DOM modification
driver.findElement(By.cssSelector("tr:first-child")).click(); // triggers re-render
String text = driver.findElement(By.cssSelector("tr:first-child")).getText(); // fresh reference
```

**Fix 2 — Retry wrapper for inherently unstable elements**
```java
public WebElement findWithRetry(By locator, int maxAttempts) {
    for (int i = 0; i < maxAttempts; i++) {
        try {
            return driver.findElement(locator);
        } catch (StaleElementReferenceException e) {
            if (i == maxAttempts - 1) throw e;
        }
    }
    return null;
}
```

**Fix 3 — Use explicit waits with staleness check**
```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement element = driver.findElement(By.id("dynamic-table"));
// After triggering refresh
wait.until(ExpectedConditions.stalenessOf(element)); // Wait for old element to go stale
WebElement freshElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dynamic-table")));
```

---

**Q9. How do you run the same test on Chrome AND Firefox at the same time?**

Running tests in parallel across browsers requires TestNG's parallel execution and a factory or parameter-driven WebDriver setup.

**Setup in testng.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="CrossBrowserSuite" parallel="tests" thread-count="2">
    <test name="ChromeTest">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="com.tests.LoginTest"/>
        </classes>
    </test>
    <test name="FirefoxTest">
        <parameter name="browser" value="firefox"/>
        <classes>
            <class name="com.tests.LoginTest"/>
        </classes>
    </test>
</suite>
```

**Base test class with browser factory:**
```java
public class BaseTest {
    protected ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    @BeforeMethod
    @Parameters("browser")
    public void setup(String browser) {
        WebDriver webDriver;
        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                webDriver = new ChromeDriver();
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                webDriver = new FirefoxDriver();
                break;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
        driver.set(webDriver);
        driver.get().manage().window().maximize();
    }

    protected WebDriver getDriver() {
        return driver.get();
    }

    @AfterMethod
    public void tearDown() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}
```

**Critical detail:** Use `ThreadLocal<WebDriver>` — this gives each thread its own driver instance. Without it, parallel threads share one driver and you get race conditions where one test navigates away while another test is mid-interaction.

**For real cross-browser parallel execution at scale:** Use Selenium Grid. The hub distributes test execution to nodes that have different browsers. For cloud scale, use BrowserStack or Sauce Labs which provide real device and browser combinations.

---

**Q10. When would you use `JavascriptExecutor` instead of normal `click()`?**

`JavascriptExecutor` bypasses Selenium's normal interaction model and executes JS directly in the browser. It should be used sparingly and for specific reasons.

**Legitimate use cases on real projects:**

**Case 1 — Element is covered by another element (overlay, header)**
```java
// An element is technically visible but another element is on top of it
// Normal click() throws: Element click intercepted
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].click();", element);
```

**Case 2 — Scrolling to element before interaction**
```java
// Scroll element into view
js.executeScript("arguments[0].scrollIntoView(true);", element);
// Then use normal click
element.click();
```

**Case 3 — Setting values that normal sendKeys() won't set**
```java
// Some React/Angular inputs don't respond properly to sendKeys
js.executeScript("arguments[0].value='" + value + "';", inputElement);
// Then trigger the change event so the framework picks it up
js.executeScript("arguments[0].dispatchEvent(new Event('input'));", inputElement);
js.executeScript("arguments[0].dispatchEvent(new Event('change'));", inputElement);
```

**Case 4 — Reading computed properties not in attributes**
```java
// Get actual CSS computed value
String color = (String) js.executeScript(
    "return window.getComputedStyle(arguments[0]).getPropertyValue('background-color');",
    element
);
```

**When NOT to use it:** Do not use JavascriptExecutor as a default fallback for elements that are hard to interact with. If a button is hard to click, it might be covered by something — a real user would have the same problem. Using JS click hides real UX issues from your tests. Use it only when you have confirmed the element is accessible to real users.

---

## SECTION 2 — API Testing

---

**Q1. The API returns 200 but the data is wrong — is this a pass or fail and why?**

This is a **fail**, unambiguously. The HTTP status code is only one dimension of an API response. Status 200 means the server processed the request without a protocol error — it says nothing about business logic correctness.

**Real-world example:**
A search API returns `200 OK` with an empty array `[]` when it should have returned 5 results. The server didn't crash, but the feature is broken. A test that only checks `status == 200` would mark this as green.

**What you must validate in every API test:**
```javascript
// Postman test example showing all layers
pm.test("Status code is 200", () => {
    pm.response.to.have.status(200);
});

pm.test("Response time is acceptable", () => {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});

pm.test("Content-Type is JSON", () => {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

pm.test("Response body has correct structure", () => {
    const body = pm.response.json();
    pm.expect(body).to.have.property("data");
    pm.expect(body.data).to.be.an("array");
    pm.expect(body.data.length).to.be.greaterThan(0);
});

pm.test("First item has required fields", () => {
    const item = pm.response.json().data[0];
    pm.expect(item).to.have.property("id").that.is.a("number");
    pm.expect(item).to.have.property("name").that.is.a("string");
    pm.expect(item.name).to.not.be.empty;
});
```

**The answer in an interview:** "200 with wrong data is a functional failure. The status code validates the transport layer; the body validates the business logic. Both must pass for the test to pass. In fact, this scenario — a 200 with incorrect payload — is harder to catch because your monitoring and alerting likely only checks status codes, so this class of bug can go undetected in production for longer."

---

**Q2. How do you test an API when there's no documentation?**

No documentation is common on legacy systems, acquired products, or internal tools. Here's the real-world approach:

**Step 1 — Intercept real traffic**
Open the application in a browser with DevTools Network tab open. Perform every user action and capture the API calls. Export as HAR file or copy requests from DevTools.

```
In DevTools > Network:
- Filter by XHR/Fetch
- Right-click request > Copy as cURL
- Import into Postman: Import > Raw Text > paste cURL
```

**Step 2 — Use Postman's documentation generator**
Once you have real requests in Postman, use the collection runner to capture responses. These become your baseline "known good" responses.

**Step 3 — Explore the API systematically**
- Change one field at a time and observe behavior
- Send null, empty string, boundary values for each field
- Try HTTP methods the API might not expect (DELETE instead of GET)
- Look at error messages — they often reveal field names, types, validation rules

**Step 4 — Read source code if accessible**
```bash
# Search for route definitions in codebase
grep -r "@GetMapping\|@PostMapping\|@RequestMapping" src/
grep -r "router\." backend/routes/
```

**Step 5 — Talk to the developer**
This is the most efficient step and engineers sometimes forget it's valid. A 15-minute call with the developer who wrote the API gives you the business rules, edge cases, and intended behavior faster than any tool.

**Step 6 — Generate documentation from what you learn**
Use Postman to create a collection with example requests and responses, then generate documentation from it. This helps the whole team.

---

**Q3. Describe your complete strategy for testing a new CRUD API.**

CRUD means Create, Read, Update, Delete. Each operation has distinct test concerns.

**CREATE (POST /resource)**
- Happy path: valid body returns 201 Created with created resource in response body
- ID should be system-generated, not from request
- Verify resource actually persists — follow with GET to confirm
- Required field missing → 400 Bad Request with meaningful error message
- Duplicate (if unique constraint exists) → 409 Conflict
- Invalid data types → 400
- Extremely large payload → check for limit (413 or graceful rejection)
- SQL injection / script in string fields → should be sanitized, not executed
- Empty string vs null → both should be handled explicitly

**READ (GET /resource and GET /resource/:id)**
- Valid ID → 200 with correct data
- Non-existent ID → 404 Not Found (not 500)
- Invalid format ID (letters when numeric expected) → 400
- Verify all fields are present and correct type
- Test pagination if applicable (page, limit, total count in response)
- Test filtering and sorting parameters

**UPDATE (PUT/PATCH /resource/:id)**
- PUT: full replacement — omitting optional fields should clear them
- PATCH: partial update — omitting fields leaves them unchanged
- Update non-existent resource → 404
- Update with invalid data → 400
- Verify the change persists — follow with GET
- Check updated_at timestamp changes
- Test optimistic locking if implemented (version/ETag)

**DELETE (DELETE /resource/:id)**
- Valid ID → 200 or 204 (no body)
- Non-existent ID → 404
- Verify resource is gone — follow with GET, expect 404
- Check cascading deletes if there are related resources
- Idempotency: deleting the same ID twice — second should return 404, not 500

**Cross-cutting concerns:**
- Authentication: all CRUD operations without auth token → 401
- Authorization: user A cannot modify user B's resource → 403
- Rate limiting: does the API enforce limits?
- Response time baseline for each operation

---

**Q4. What is idempotency and why does it matter for PUT vs POST?**

**Idempotency** means calling the same operation multiple times produces the same result as calling it once. The side effects are identical whether you called it 1 time or 100 times.

**PUT is idempotent:**
`PUT /users/123` with body `{name: "Alice"}` — no matter how many times you send this, user 123's name is Alice. The state after the 1st call and the 10th call is identical.

**POST is NOT idempotent:**
`POST /users` with body `{name: "Alice"}` — each call creates a NEW user. After 10 calls, you have 10 users named Alice with 10 different IDs.

**Why this matters in testing:**

```
Test scenario: Network timeout on a PUT request
- Client sends PUT /users/123
- Server processes it, updates the record
- Response is lost in transit (timeout)
- Client retries the PUT
- Result: user 123 still has the correct data — idempotent behavior, safe to retry

Test scenario: Network timeout on a POST request
- Client sends POST /orders
- Server creates order #999
- Response is lost in transit (timeout)
- Client retries the POST
- Result: server creates ANOTHER order #1000 — duplicate order, money charged twice
```

**What to test for idempotency:**
```javascript
// Test PUT idempotency
const body = { name: "Test Product", price: 99.99 };

// First call
pm.sendRequest({ url: 'PUT /products/1', body: body });
// Second call  
pm.sendRequest({ url: 'PUT /products/1', body: body });
// Verify only one record exists with that data, not two
pm.sendRequest({ url: 'GET /products/1' }, (err, res) => {
    pm.expect(res.json().price).to.equal(99.99); // Same result
});
```

**Real-world consequence of getting this wrong:** E-commerce systems where POST order endpoint is not idempotent + poor client retry logic = duplicate charges. This is a critical test case on payment systems.

---

**Q5. How do you test an API that requires OAuth2 authentication?**

OAuth2 is the standard for modern APIs. You can't just pass a username and password — you need an access token. Here's the process on real projects:

**Step 1 — Get the token**

**Client Credentials flow (service-to-service, no user):**
```javascript
// Postman Pre-request Script
pm.sendRequest({
    url: 'https://auth.example.com/oauth/token',
    method: 'POST',
    header: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: {
        mode: 'urlencoded',
        urlencoded: [
            { key: 'grant_type', value: 'client_credentials' },
            { key: 'client_id', value: pm.environment.get('CLIENT_ID') },
            { key: 'client_secret', value: pm.environment.get('CLIENT_SECRET') },
            { key: 'scope', value: 'read write' }
        ]
    }
}, (err, res) => {
    pm.environment.set('ACCESS_TOKEN', res.json().access_token);
    pm.environment.set('TOKEN_EXPIRY', Date.now() + (res.json().expires_in * 1000));
});
```

**Step 2 — Use the token in subsequent requests**
```
Authorization: Bearer {{ACCESS_TOKEN}}
```

**Step 3 — Handle token expiry intelligently**
```javascript
// In Pre-request Script of every request
const expiry = pm.environment.get('TOKEN_EXPIRY');
if (!expiry || Date.now() > expiry - 60000) { // Refresh 60s before expiry
    // Re-request token (same code as above)
}
```

**Step 4 — Test the auth failure cases**
- No token → 401 Unauthorized
- Expired token → 401 with message like "Token expired"
- Invalid token (tampered) → 401
- Token with insufficient scope → 403 Forbidden
- Token for user without permission to this resource → 403

**In RestAssured (Java):**
```java
String token = given()
    .contentType("application/x-www-form-urlencoded")
    .formParam("grant_type", "client_credentials")
    .formParam("client_id", clientId)
    .formParam("client_secret", clientSecret)
    .post("https://auth.example.com/oauth/token")
    .then().extract().path("access_token");

given()
    .header("Authorization", "Bearer " + token)
    .get("https://api.example.com/users")
    .then()
    .statusCode(200);
```

**Security: never hardcode credentials.** Use environment variables or a secrets manager. For CI, use vault or pipeline secret injection.

---

**Q6. The response time is 2.9 seconds — is this acceptable? How do you decide?**

There is no universal answer. The acceptability of 2.9 seconds depends on context, and saying "it depends" without explaining what it depends on is a non-answer.

**The framework for deciding:**

**Step 1 — What is the SLA/SLO?**
Is there a defined performance target? Check requirements documentation, Non-Functional Requirements (NFRs), or SLAs with customers. If the SLA says responses must be under 3 seconds, then 2.9 seconds technically passes — but it's dangerously close to the boundary.

**Step 2 — What is the user operation?**
- Background sync, data export, report generation: 2.9 seconds may be acceptable
- Login button click: 2.9 seconds is too slow — users perceive 300ms as "instant", 1 second as "limit of flow"
- Search autocomplete: anything over 300ms is too slow

**Step 3 — What is the load condition?**
2.9 seconds at 1 concurrent user vs 2.9 seconds at 500 concurrent users are completely different stories. Run JMeter or k6 to measure under realistic load.

**Step 4 — Is it consistent?**
Run the request 50 times. Plot the percentiles:
- P50 (median): typical user experience
- P95: 95% of users experience this or better
- P99: worst-case realistic experience

A P95 of 2.9s means 5% of real users wait longer than that.

**Step 5 — What is the trend?**
Was it 1.5 seconds last sprint? If response time doubled, that's a regression even if it's "still under 3 seconds."

**My answer in context:** "2.9 seconds in isolation tells me little. I'd compare it against the defined SLA, run it under realistic load, measure the percentile distribution, and compare it to baseline measurements from previous releases. If there's no SLA, I'd raise it as a performance risk for the team to define one."

---

**Q7. How do you verify that an API didn't break backward compatibility after a change?**

Backward compatibility means existing consumers of the API continue to work after your change. Breaking it causes downstream systems and integrations to fail — often silently.

**What counts as a breaking change:**
- Removing a field from the response
- Renaming a field
- Changing a field's data type (string to integer)
- Changing a required field from optional to required in the request
- Removing a supported HTTP method
- Changing response status codes

**What is NOT breaking:**
- Adding a new optional field to the response
- Adding a new optional query parameter
- Adding a new endpoint
- Adding new optional request fields

**Testing strategy:**

**Approach 1 — Contract testing with Pact**
Producer and consumer agree on a contract. Before deployment, the producer verifies the API still satisfies all consumer contracts.

**Approach 2 — Snapshot testing in RestAssured/Postman**
```javascript
// Postman: save a known-good response as a snapshot
const snapshot = {
    "id": "schema-check",
    "required_fields": ["id", "name", "email", "created_at"],
    "types": {
        "id": "number",
        "name": "string",
        "email": "string"
    }
};

// Validate current response against snapshot
const body = pm.response.json();
snapshot.required_fields.forEach(field => {
    pm.test(`Field '${field}' exists`, () => {
        pm.expect(body).to.have.property(field);
    });
    pm.test(`Field '${field}' is ${snapshot.types[field]}`, () => {
        pm.expect(typeof body[field]).to.equal(snapshot.types[field]);
    });
});
```

**Approach 3 — Version your API**
Run v1 and v2 side by side. Run the same test suite against both versions and compare output.

**Approach 4 — API linting tools**
Tools like `openapi-diff` or `breaking-change-detector` compare two OpenAPI/Swagger specs and flag breaking changes automatically in CI.

**What I do on real projects:** Maintain a baseline Postman collection that tests the contract (field names, types, response structure) — not just the data values. Run this baseline collection in CI on every PR that touches the API layer. Any structural change fails the contract tests and requires explicit review.

---

**Q8. What is the difference between functional and contract testing for APIs?**

These are complementary but distinct types of testing that answer different questions.

**Functional testing** asks: Does this API do what it's supposed to do?
- Tests business logic and data correctness
- Tests all scenarios: happy path, error cases, boundary values
- Run by the QA team, often via Postman, RestAssured, pytest
- Tightly coupled to the current implementation

**Contract testing** asks: Does this API still honour the agreement it made with its consumers?
- Tests only the interface — fields, types, response codes
- Does NOT test business logic or data values
- Run at integration boundaries — between services, between teams
- Decoupled from implementation detail

**Concrete example:**

*Functional test:*
```
POST /login with valid credentials
→ Verify status 200
→ Verify response has token field
→ Verify token is a valid JWT (3 parts, base64 encoded)
→ Verify token expires in 1 hour
→ Verify user data in token matches the logged-in user
```

*Contract test (Pact):*
```
Consumer (mobile app) expects:
→ POST /login returns 200
→ Response has field "token" of type string
→ Response has field "user" with "id" (number) and "email" (string)

That's it. No business logic. Just: "please keep giving me these fields."
```

**Why both matter:**
- Functional tests catch business logic bugs
- Contract tests catch integration breaks between teams when one team changes their API without coordinating

**When to use which:** Functional testing on every API endpoint. Contract testing at every integration boundary — especially microservice-to-microservice and backend-to-mobile-app boundaries.

---

**Q9. How do you handle testing APIs that call third-party services?**

Third-party dependencies (payment gateways, SMS providers, email services, external data feeds) create three problems in testing:
1. You can't control them — they may be slow, down, or return unexpected data
2. You don't want to trigger real charges, emails, or side effects in tests
3. Your test results become dependent on external service availability

**Strategy 1 — Mock/Stub the third-party service**

At the code level (unit/integration):
```java
// Mock payment gateway in your test
@Mock
private PaymentGateway paymentGateway;

@Test
public void testSuccessfulPayment() {
    when(paymentGateway.charge(any())).thenReturn(new PaymentResult(true, "txn-123"));
    // Test your logic without hitting real gateway
}
```

At the API level — use WireMock:
```java
// WireMock stubs the third-party endpoint
stubFor(post(urlEqualTo("/payment/charge"))
    .willReturn(aResponse()
        .withStatus(200)
        .withBody("{\"success\": true, \"transactionId\": \"txn-123\"}")));
```

**Strategy 2 — Use sandbox environments**
Most reputable third-party services provide a sandbox. Stripe, PayPal, Twilio, SendGrid all have test environments with specific test credentials and card numbers that simulate various responses.

**Strategy 3 — Test the integration boundary, not through it**
Write tests that verify your service sends the correct request to the third party (right fields, right format). Separately, write tests that verify your service handles all possible third-party responses correctly (success, failure, timeout, malformed response).

**Strategy 4 — Contract tests with the third-party provider**
Some providers support consumer-driven contract testing — you submit a contract and their sandbox validates it.

**What I say in interviews:** "I separate tests into two groups: tests that verify my service's logic with the third party mocked out, and integration tests that run against the sandbox environment during the integration test phase of CI. I never run tests against production third-party endpoints."

---

**Q10. Postman vs RestAssured — when do you use each?**

Both are API testing tools, but they serve different contexts and different audiences.

**Postman — when to use:**
- Exploratory testing and API discovery during development
- Manual verification by QA engineers without coding background
- Creating shareable API documentation and examples for the team
- Quick sanity checks and ad-hoc testing
- Building test collections that non-developers can run and maintain
- Client-facing API demos and sandbox environments

**RestAssured — when to use:**
- Automated regression testing integrated into a Java/JVM CI pipeline
- Complex test data setup using Java code (loops, conditionals, data generation)
- Integration testing within the same codebase (share models, constants, utilities)
- Tests that need to be version-controlled alongside application code
- High-volume data-driven tests from CSV, Excel, or database
- When you need assertions more complex than Postman scripting can handle cleanly

**Concrete comparison:**

```javascript
// Postman (JavaScript in test scripts)
pm.test("User age is valid", () => {
    pm.expect(pm.response.json().age).to.be.above(0).and.below(150);
});
```

```java
// RestAssured (Java)
given()
    .header("Authorization", "Bearer " + getToken())
    .pathParam("userId", 123)
    .when()
    .get("/users/{userId}")
    .then()
    .statusCode(200)
    .body("age", greaterThan(0))
    .body("age", lessThan(150))
    .body("name", not(emptyString()));
```

**My real-world approach:** I use Postman for exploratory testing, documentation, and sharing with the team during development. I then graduate those tests to RestAssured for the automated regression suite that runs in CI. Some tests stay in Postman if they need to be run by non-developers (DevOps, business analysts, demo purposes).

---

## SECTION 3 — TestNG

---

**Q1. What happens to a test that depends on a failed test?**

In TestNG, when you declare a dependency with `@Test(dependsOnMethods = "loginTest")`, and `loginTest` fails, the dependent test is **skipped** — not failed. TestNG marks it with `SKIP` status.

```java
@Test
public void loginTest() {
    // This fails
    Assert.assertEquals(driver.getTitle(), "Dashboard"); // actual: "Error"
}

@Test(dependsOnMethods = "loginTest")
public void createOrderTest() {
    // This will be SKIPPED, not run, because loginTest failed
}
```

**Why this matters on real projects:**

If you have a deep dependency chain:
```
loginTest → navigateToProductsTest → addToCartTest → checkoutTest → confirmOrderTest
```

One failure at the top cascades to SKIP every downstream test. Your report shows 1 FAIL + 4 SKIP. This can mislead stakeholders into thinking "only 1 test failed" when actually 5 features are broken.

**The real-world debate:**

Hard dependencies (`dependsOnMethods`) are generally considered bad practice for this reason. **Preferred alternative:** Make tests truly independent. Each test handles its own setup (login, navigate) in `@BeforeMethod`. The cost is slightly slower test runs; the benefit is accurate failure reporting.

**When dependencies are valid:**
- Truly sequential user flows (wizard steps where step 2 is impossible without step 1)
- Tests that create data for subsequent tests (though test data setup should be in `@BeforeClass` instead)

---

**Q2. How do you run 100 tests in 10 minutes?**

Assuming a test takes ~5-8 seconds each, 100 tests sequentially would take ~10-15 minutes. You need parallelism.

**Step 1 — Enable parallel execution in testng.xml**
```xml
<suite name="RegressionSuite" parallel="methods" thread-count="10">
    <test name="AllTests">
        <classes>
            <class name="com.tests.LoginTests"/>
            <class name="com.tests.CheckoutTests"/>
            <class name="com.tests.SearchTests"/>
        </classes>
    </test>
</suite>
```

**Step 2 — Thread-safe WebDriver with ThreadLocal**
```java
public class BaseTest {
    private static ThreadLocal<WebDriver> driverThread = new ThreadLocal<>();
    
    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
        driverThread.set(new ChromeDriver(options));
    }
    
    protected WebDriver getDriver() {
        return driverThread.get();
    }
    
    @AfterMethod
    public void tearDown() {
        if (driverThread.get() != null) {
            driverThread.get().quit();
            driverThread.remove();
        }
    }
}
```

**Step 3 — Ensure tests are truly independent**
- Each test creates its own test data via API calls in `@BeforeMethod`
- No shared state between tests
- Each test cleans up after itself

**Step 4 — Use headless browser**
Headless Chrome is 20-30% faster than headed because there's no rendering overhead.

**Step 5 — Distribute across machines with Selenium Grid**
```
10 nodes × 5 parallel tests per node = 50 concurrent tests
100 tests ÷ 50 concurrent = 2 cycles of ~5 minutes each = ~10 minutes total
```

**What could go wrong:** Tests that share a test user account fail when run in parallel because two tests simultaneously modify the same user state. Solution: each test uses a unique user created by the test setup.

---

**Q3. A `@BeforeMethod` is failing — how does it affect the test?**

When `@BeforeMethod` fails (throws an exception), TestNG marks the `@Test` method that was about to run as **FAILED** — not SKIP. This is different from `dependsOnMethods` behavior.

```java
@BeforeMethod
public void openBrowser() {
    driver = new ChromeDriver(); // Suppose ChromeDriver binary is missing
    // Exception thrown here
}

@Test
public void loginTest() {
    // This test is marked FAILED even though it never ran
    // The failure points to BeforeMethod, not to loginTest
}
```

**Why this creates a misleading report:**

The test failure message says `loginTest FAILED` but the actual cause is in `openBrowser()`. The stack trace should reveal this, but if your reporting dashboard only shows test names without stack traces, the root cause is hidden.

**How to handle `@BeforeMethod` failures gracefully:**
```java
@BeforeMethod
public void setup(Method testMethod) {
    try {
        driver = createDriver();
        driver.get(BASE_URL);
    } catch (Exception e) {
        System.err.println("Setup failed for test: " + testMethod.getName());
        throw e; // Re-throw so TestNG marks it as failure properly
    }
}
```

**Best practice:** `@BeforeMethod` should be rock solid — minimal logic, just browser setup and navigation. Complex setup logic (creating test users, seeding data) should use the API directly with RestAssured or HTTP client, wrapped in a try-catch with proper error messaging.

---

**Q4. How do you ensure tests are independent of each other?**

Test independence means any test can run in any order, in isolation, or in parallel without affecting other tests.

**Rule 1 — Each test owns its data**
```java
@BeforeMethod
public void createTestUser() {
    // Create a fresh user via API for THIS test only
    testUser = apiClient.createUser("user_" + System.currentTimeMillis() + "@test.com", "password");
}

@AfterMethod
public void deleteTestUser() {
    apiClient.deleteUser(testUser.getId()); // Clean up
}
```

**Rule 2 — Never rely on database state left by another test**
If test A creates a product and test B reads that product, test B will fail when run alone. Instead, test B should create the product in its own `@BeforeMethod`.

**Rule 3 — Reset browser state between tests**
```java
@BeforeMethod
public void clearState() {
    driver.manage().deleteAllCookies();
    driver.manage().getCookies(); // Verify cleared
    // Clear local storage
    ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
    ((JavascriptExecutor) driver).executeScript("window.sessionStorage.clear();");
}
```

**Rule 4 — Avoid shared static variables**
```java
// BAD — shared state between parallel tests
static String authToken;

// GOOD — ThreadLocal for parallel safety
ThreadLocal<String> authToken = new ThreadLocal<>();
```

**Rule 5 — Use unique identifiers for test data**
```java
String uniqueEmail = "test+" + UUID.randomUUID() + "@example.com";
```

**How to verify independence:** Run your full suite in random order multiple times. TestNG supports this with `preserve-order="false"` in testng.xml. If any test fails in one order but passes in another, you have a dependency.

---

**Q5. How do you retry only network-related failures, not assertion failures?**

This is a nuanced question. Blindly retrying all failures hides real bugs — you want to retry transient failures (network timeouts, service momentarily unavailable) but not assertion failures (actual bugs).

**Implement a smart IRetryAnalyzer:**
```java
public class SmartRetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int MAX_RETRY = 2;
    
    // Exceptions that indicate transient infrastructure issues, not bugs
    private static final Set<Class<? extends Exception>> RETRYABLE_EXCEPTIONS = Set.of(
        NoSuchElementException.class,
        TimeoutException.class,
        WebDriverException.class,    // Connection refused, browser crash
        SocketTimeoutException.class
    );
    
    // Exceptions that indicate real failures — never retry
    private static final Set<Class<? extends Exception>> NON_RETRYABLE_EXCEPTIONS = Set.of(
        AssertionError.class,        // Assert.assertEquals failed — real bug
        NullPointerException.class   // Code bug in test
    );
    
    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && retryCount < MAX_RETRY) {
            Throwable cause = result.getThrowable();
            
            // Never retry assertion failures
            if (cause instanceof AssertionError) {
                System.out.println("Assertion failure — not retrying: " + result.getName());
                return false;
            }
            
            // Only retry known transient exceptions
            if (isRetryableException(cause)) {
                retryCount++;
                System.out.println("Transient failure — retry " + retryCount + " for: " + result.getName());
                return true;
            }
        }
        return false;
    }
    
    private boolean isRetryableException(Throwable t) {
        if (t == null) return false;
        return RETRYABLE_EXCEPTIONS.stream()
            .anyMatch(e -> e.isInstance(t) || e.isInstance(t.getCause()));
    }
}
```

**Apply to tests:**
```java
@Test(retryAnalyzer = SmartRetryAnalyzer.class)
public void testCheckoutFlow() { ... }
```

**Report correctly:** Log every retry attempt. Ensure your report shows "passed on retry 2" so you can track flakiness over time and fix root causes.

---

## SECTION 4 — Playwright

---

**Q1. Playwright says element is visible but click fails — why?**

Playwright's actionability checks verify that an element is: attached to DOM, visible, stable (not animating), not obscured, and enabled. If `click()` still fails after these checks pass, it's usually one of these causes:

**Cause 1 — Element is in a scrollable container, not the viewport**
Playwright auto-scrolls to elements before clicking, but if the container has `overflow: hidden` or the element is in a nested scroll region, the auto-scroll may not work correctly.
```javascript
// Force scroll and then click
await element.scrollIntoViewIfNeeded();
await element.click();
```

**Cause 2 — Animation/transition not complete**
The element appears visible but is still animating into position. Playwright's stability check waits for position to stop changing, but some animations are complex.
```javascript
// Wait for the animation CSS class to be removed
await page.waitForSelector('.modal-opening', { state: 'detached' });
await page.click('#confirm-button');
```

**Cause 3 — Element is obscured by a transparent/invisible overlay**
A `z-index` overlay (cookie banner, loading spinner, invisible div) intercepts the click. Playwright detects this and throws `Element is not clickable at point (x, y), another element is receiving the click`.
```javascript
// Check what's on top at those coordinates
await page.evaluate((selector) => {
    const el = document.querySelector(selector);
    const rect = el.getBoundingClientRect();
    const topEl = document.elementFromPoint(rect.x + rect.width/2, rect.y + rect.height/2);
    return topEl.tagName + ' ' + topEl.className;
}, '#target-button');
```

**Cause 4 — Shadow DOM**
Elements inside Web Components (Shadow DOM) require different handling.
```javascript
// Pierce through Shadow DOM
await page.locator('my-component >> button.submit').click();
```

**Cause 5 — iframe context**
```javascript
const frame = page.frameLocator('#payment-frame');
await frame.locator('#card-number').fill('4111111111111111');
```

---

**Q2. How does Playwright handle authentication across 50 tests without logging in each time?**

Logging in for every test is the most common performance mistake in Playwright suites. Each login takes 2-5 seconds — across 50 tests that's 100-250 seconds wasted. Playwright's storage state solves this.

**Step 1 — Create a global auth setup**
```javascript
// global-setup.js
const { chromium } = require('@playwright/test');

module.exports = async (config) => {
    const browser = await chromium.launch();
    const page = await browser.newPage();
    
    // Login once
    await page.goto('https://app.example.com/login');
    await page.fill('#email', process.env.TEST_EMAIL);
    await page.fill('#password', process.env.TEST_PASSWORD);
    await page.click('#login-btn');
    await page.waitForURL('**/dashboard');
    
    // Save authentication state (cookies + localStorage)
    await page.context().storageState({ path: 'auth-state.json' });
    await browser.close();
};
```

**Step 2 — Configure playwright.config.js**
```javascript
module.exports = {
    globalSetup: './global-setup.js',
    use: {
        storageState: 'auth-state.json', // All tests start pre-authenticated
    },
    projects: [
        {
            name: 'authenticated-tests',
            use: { storageState: 'auth-state.json' }
        },
        {
            name: 'unauthenticated-tests',  // Login page, error page tests
            use: { storageState: undefined }
        }
    ]
};
```

**Step 3 — Tests start authenticated**
```javascript
test('access dashboard', async ({ page }) => {
    // No login needed — storage state is pre-loaded
    await page.goto('/dashboard');
    await expect(page.locator('h1')).toContainText('Welcome');
});
```

**For multiple user roles:**
```javascript
// auth.setup.js
test('authenticate as admin', async ({ page }) => {
    await page.goto('/login');
    await page.fill('#email', process.env.ADMIN_EMAIL);
    await page.fill('#password', process.env.ADMIN_PASSWORD);
    await page.click('#login-btn');
    await page.context().storageState({ path: 'admin-auth.json' });
});

test('authenticate as regular user', async ({ page }) => {
    // ...
    await page.context().storageState({ path: 'user-auth.json' });
});
```

---

**Q3. Your Playwright test passes locally but fails in GitHub Actions — step-by-step debugging.**

This is one of the most common problems in real CI/CD setups. The local-CI gap has well-known causes.

**Step 1 — Check the CI failure output**
GitHub Actions gives you the error message, screenshot, and video if configured. Start there.
```yaml
# playwright.config.js — ensure artifacts are captured
use:
  screenshot: 'only-on-failure'
  video: 'retain-on-failure'
  trace: 'on-first-retry'
```

**Step 2 — Check the most common causes:**

| Cause | How to detect | Fix |
|---|---|---|
| Different screen resolution | CI uses 1280x720, local is 1920x1080 | `viewport: { width: 1280, height: 720 }` in config |
| Missing environment variables | Test references undefined env var | Add to GitHub Secrets + workflow yaml |
| Slower CI machine | Timeouts that pass locally | Increase `timeout` in playwright.config.js |
| Different timezone | Date-sensitive tests | Set `TZ=UTC` in workflow |
| Browser not installed | "Executable doesn't exist" error | Add `npx playwright install --with-deps` to workflow |
| Test isolation failure | Test passes alone, fails in CI parallel run | Check shared state, ThreadLocal data |

**Step 3 — Reproduce locally with CI conditions**
```bash
# Run in headless mode (like CI)
npx playwright test --headed=false

# Run with CI environment variables
TEST_ENV=ci npx playwright test

# Run with same parallelism as CI
npx playwright test --workers=2
```

**Step 4 — Enable trace for detailed debugging**
```javascript
// playwright.config.js
use: {
    trace: 'on', // Always capture trace in CI
}
```
Download the trace artifact from GitHub Actions and open with `npx playwright show-trace trace.zip`. This shows every network request, DOM snapshot, and action in a timeline — invaluable for debugging timing issues.

**Step 5 — Check GitHub Actions workflow setup**
```yaml
- name: Install Playwright
  run: npx playwright install --with-deps chromium

- name: Run Tests
  run: npx playwright test
  env:
    CI: true
    BASE_URL: ${{ secrets.STAGING_URL }}
    TEST_PASSWORD: ${{ secrets.TEST_PASSWORD }}

- name: Upload Artifacts
  if: failure()
  uses: actions/upload-artifact@v3
  with:
    name: test-results
    path: test-results/
```

---

**Q4. When would you use Playwright over Selenium for a new project?**

This is a practical architectural decision. Both are capable; the context determines the right choice.

**Choose Playwright when:**
- The app is a modern SPA (React, Vue, Angular) — Playwright was built for this
- You want built-in network interception (mock APIs in tests without WireMock)
- Your team uses JavaScript/TypeScript — Playwright's API is natural in JS
- You need reliable parallel execution — Playwright's architecture isolates browser contexts natively
- You want automatic waiting — Playwright's auto-wait eliminates most flakiness
- You need cross-browser in a single runner (Chromium, Firefox, Safari/WebKit)
- You need mobile viewport simulation
- CI integration is a priority — Playwright's GitHub Actions integration is first-class

```javascript
// Playwright network interception — built-in, no extra setup
await page.route('**/api/products', route => {
    route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([{ id: 1, name: 'Test Product' }])
    });
});
```

**Choose Selenium when:**
- The team already has a large Selenium + Java suite in production
- You need real cross-browser with Internet Explorer or older browser versions
- You need integration with specific Java testing tools (TestNG groups, JUnit rules)
- Company policy mandates Java
- You need Selenium Grid for distributed execution at large scale

**My honest answer on a greenfield project in 2024:** I'd choose Playwright. Auto-wait, built-in tracing, first-class TypeScript support, network interception, and the Playwright Test runner make it faster to build reliable tests. The only reason I'd default to Selenium is an existing Java codebase or team Java expertise.

---

**Q5. How do you test a feature that opens in a new browser tab?**

New tab handling is a common source of confusion because the new tab is a separate browser context with no automatic reference in your test.

**Approach 1 — Wait for the new page event (Playwright native)**
```javascript
test('link opens in new tab', async ({ page, context }) => {
    await page.goto('https://example.com');
    
    // Wait for new page to open when clicking the link
    const [newPage] = await Promise.all([
        context.waitForEvent('page'),           // Listen for new tab
        page.click('a[target="_blank"]')        // Action that opens it
    ]);
    
    await newPage.waitForLoadState('networkidle');
    
    // Assert on the new page
    await expect(newPage).toHaveURL(/expected-url/);
    await expect(newPage.locator('h1')).toContainText('Expected Title');
    
    await newPage.close(); // Clean up
});
```

**Approach 2 — Intercept the navigation (prevent new tab, test in same page)**
Sometimes opening a new tab is inconvenient and you just want to verify the URL:
```javascript
// Change target="_blank" to open in same window for testing
await page.evaluate(() => {
    document.querySelectorAll('a[target="_blank"]').forEach(a => {
        a.removeAttribute('target');
    });
});

await page.click('a[href*="report"]');
await expect(page).toHaveURL(/report/);
```

**Approach 3 — Test the download behavior if the new tab triggers a file**
```javascript
const [download] = await Promise.all([
    page.waitForEvent('download'),
    page.click('#download-report-link')
]);

const path = await download.path();
expect(path).toBeTruthy();
const filename = download.suggestedFilename();
expect(filename).toMatch(/report.*\.pdf/);
```

**Common mistake:** Using `page.goto()` in a new tab and missing that the existing page still has the original context. Always keep references to both pages if you need to interact with the original after the new tab opens.

---

## SECTION 5 — CI/CD & Quality

---

**Q1. A developer pushes code that breaks 5 tests — what do you do?**

This is a process and communication question as much as a technical one. The answer shows how you work in a team.

**Immediate actions (within minutes):**

1. **Confirm it's not your tests that are wrong.** Before escalating, verify: Did the dev intentionally change behavior that your tests were checking? Run the failing tests in isolation to confirm they fail.

2. **Identify the failing tests and the commit that broke them.** Use git bisect or check CI history.
   ```bash
   git log --oneline -10  # Find the breaking commit
   git diff HEAD~1 -- src/  # See what changed
   ```

3. **Categorize the failures:**
   - Test asserting old behavior that is intentionally changed? → Update tests, not a bug
   - Test asserting correct behavior that is now broken? → Bug in the code, block the PR

**Communication:**
- Slack/Teams message to the developer: "Hey, your PR #123 broke these 5 tests: [list]. Can you take a look?" — not confrontational, just informational
- If it's a bug: "The checkout calculation returns incorrect totals when discount is applied. Tests: [links]"
- If it's intentional: "I see you changed the login redirect behavior — I'll update the test to match the new spec. Can you confirm this is the intended behavior?"

**Never silently skip or mark as expected failure.** If you bypass the failure, you've removed the safety net.

**If the team is under pressure to merge anyway:** Raise it explicitly. "Merging this breaks 5 regression tests that cover [feature X]. I can't sign off on this without either fixing the code or explicitly accepting the risk in writing."

---

**Q2. How do you decide which tests go in the smoke suite vs regression suite?**

This is a test selection strategy question — it's about signal-to-noise ratio and execution speed.

**Smoke suite — "Is the system alive?"**
- Runs in under 5 minutes
- Tests the most critical, highest-traffic user journeys
- Runs on EVERY build, EVERY deployment to every environment
- Failure = block the deployment

Criteria for inclusion:
- Login / authentication
- Core navigation
- Primary value transaction (place order, create record, submit form)
- API is responding
- Data is loading

```
Smoke suite examples (e-commerce):
✓ User can log in
✓ Products page loads with results
✓ Can add item to cart
✓ Checkout flow completes
✓ Order confirmation email is sent
```

**Regression suite — "Is everything still working?"**
- Runs in under 60 minutes (target), nightly or on release branches
- Tests all features, edge cases, error scenarios
- Failure = block the release, investigate before deployment
- Includes all smoke tests plus:
  - Error handling flows
  - Edge cases and boundary values
  - Integration with third-party services
  - Permission and role-based access
  - Data validation

**Decision framework:**
```
Ask: If this test fails, would you stop a production deployment?
Yes + answer in minutes → Smoke
Yes + answer in an hour → Regression
No (nice to have) → Consider if it should exist at all
```

**Anti-pattern:** A smoke suite that takes 45 minutes provides no value. If everything is "critical", nothing is.

---

**Q3. Your automation suite takes 2 hours — how do you speed it up?**

A 2-hour suite is too slow to give useful feedback. The goal is under 30 minutes for regression, under 5 minutes for smoke.

**Step 1 — Measure before you optimize**
Profile where time is actually spent. Don't guess.
```bash
# TestNG report shows per-test times
# Look for tests taking >30 seconds — they're the priority
grep -E "duration-ms" testng-results.xml | sort -t'"' -k4 -nr | head -20
```

**Step 2 — Parallelize (biggest gain)**
- If currently sequential, going to 10 parallel threads can cut time by 80%
- Use Selenium Grid or Playwright workers
- Requires tests to be independent (see TestNG Q4)

**Step 3 — Eliminate sleep() calls**
```java
// Each Thread.sleep(3000) in a 200-test suite = 600 seconds = 10 minutes
// Replace with explicit waits
Thread.sleep(3000); // BAD — eliminate all of these
wait.until(ExpectedConditions.visibilityOf(element)); // GOOD
```

**Step 4 — Use API calls for test setup instead of UI**
```java
// BAD: Log in via UI, navigate to user management, create test user via form
// 15 seconds of UI interaction for setup

// GOOD: Create test user via API in @BeforeMethod
// 300ms for REST call
testUser = apiClient.post("/users", newUser);
```

**Step 5 — Run headless**
Headless Chrome/Firefox is 20-30% faster than headed execution.

**Step 6 — Trim dead tests**
Run coverage analysis. Tests that never fail and test trivial things may not be worth the execution time. Delete or de-prioritize them.

**Step 7 — Split into tiers and run selectively**
Not all tests need to run on every commit. Run smoke on every push, full regression nightly or on release branches.

**Realistic impact:**
- Parallelization (1→10 threads): 2 hours → 15-20 minutes
- Eliminating sleeps: -15-20%
- API setup instead of UI setup: -10-15%
- Headless: -10%

---

**Q4. How do you measure whether your test automation is actually valuable?**

"We have 500 tests" is not a measure of value. The metrics that actually matter:

**Metric 1 — Defect Detection Rate**
How many bugs did automation catch before production?
```
Bugs caught by automation / Total bugs found × 100%
Target: automation should catch >60% of bugs before production
```

**Metric 2 — False Positive Rate (% of failures that are test bugs, not product bugs)**
```
Test failures due to flaky tests / Total test failures × 100%
Target: <5%
If 40% of failures are false positives, the team stops trusting the suite
```

**Metric 3 — Time Saved vs Manual Testing**
```
Manual regression time (per sprint) - Automation execution time = Time saved
If manual regression = 40 hours, automation runs in 1 hour → 39 hours saved per sprint
```

**Metric 4 — Feedback Speed**
How long from code push to knowing if tests pass?
```
Target: <30 minutes for "should I continue reviewing this PR?"
2-hour suites provide delayed feedback — PRs sit waiting
```

**Metric 5 — Coverage of New Features**
```
Features with automated tests / Total features released × 100%
Are you keeping pace with development?
```

**Metric 6 — Escaped Defects**
How many bugs reached production that automation should have caught?
This is the most painful metric — and the most important.

**How I track this in practice:** I maintain a monthly report showing: tests run, pass rate, false positive rate, bugs caught, bugs escaped. The trend matters more than a single data point. If the false positive rate is rising, trust is eroding and the suite needs maintenance.

---

**Q5. A test passes in CI but fails on the demo environment — how do you investigate?**

The test result disagrees between two environments. This means environment differences are likely the cause, not a bug in the code or the test.

**Step 1 — Check environment differences**

| Area | CI Environment | Demo Environment |
|---|---|---|
| App version | Latest build | Possibly older, pinned version |
| Database | Seeded test data | Real-ish data, may differ |
| Config | Test config values | Demo config — may point to different services |
| Third-party integrations | Mocked/stubbed | Real sandbox |
| Browser version | Latest in Docker image | Demo machine's browser |
| Network | Internal, fast | May go through different proxies |

**Step 2 — Run the specific test against demo to capture failure**
```bash
# Run only the failing test against demo URL
BASE_URL=https://demo.example.com npx playwright test tests/checkout.spec.js --headed
```
Capture screenshots and network traffic. What specifically fails — assertion, element not found, timeout?

**Step 3 — Compare the relevant data**

If the test asserts a product exists and it passes in CI (with seeded data) but fails on demo (where the product doesn't exist), the test data is the issue — not the app.

**Step 4 — Check application logs on demo**
```
Error in demo logs during test execution?
→ API returning 500 where CI returns 200?
→ Database migration not run on demo?
→ Feature flag disabled on demo?
```

**Step 5 — Common resolutions:**

- **Test data mismatch:** Tests should create their own data via API, not rely on pre-existing data
- **Config mismatch:** Test should parameterize values from environment rather than hardcoding
- **Feature flag:** Test should check feature flag state and skip if feature is disabled
- **Version mismatch:** Document which version the test was validated against

**What I report to the team:** "Test X fails on demo but passes in CI. Root cause: the test relies on product ID 42 existing in the database. This product exists in CI's seed data but not in demo. Fix: update the test to create the product via API in setup and delete it in teardown."

---

## SECTION 6 — Real-World Scenarios

---

**Q1. You join a project with no tests and a 2-week deadline — what do you do first?**

Two weeks with no tests and a deadline is a triage situation. You cannot build comprehensive coverage in 2 weeks — so you must be strategic.

**Day 1 — Understand the risk landscape:**
- Talk to developers: "What areas of the code do you trust least? What keeps you up at night?"
- Talk to the product owner: "What user journeys, if broken, would cause the biggest business problem?"
- Review recent production issues or support tickets — these reveal the historically fragile areas

**Day 2 — Map the critical user journeys:**
Identify the 5-7 flows that are absolutely essential for the business. Example for e-commerce:
1. User registration and login
2. Product search and discovery
3. Add to cart
4. Checkout and payment
5. Order status tracking

**Day 3-5 — Manual smoke testing of critical paths:**
Before writing any automation, manually validate these paths work. Document what you find. This gives you: (a) knowledge of the app, (b) any bugs blocking automation, (c) a test plan.

**Day 5-14 — Automation priority order:**
1. API tests first — faster to write, more stable, catch logic bugs
2. Smoke tests for the critical UI journeys
3. Any integration points with payment, auth, or external services

**What I explicitly deprioritize in this scenario:**
- Edge case testing (important but not 2-week emergency material)
- Performance testing
- Perfect page object model architecture

**What I tell the team:**
"We'll have baseline smoke coverage for the 5 critical journeys by the end of 2 weeks. This gives us confidence in the happy paths. We're accepting risk on edge cases and error paths — I'd recommend we document this as technical debt and address it after the release."

---

**Q2. The team wants to skip testing to meet a release deadline — what do you say?**

This is a judgment and communication challenge. The wrong answers are: (1) blindly agree because it's management's call, or (2) refuse and create conflict.

**The right approach — quantify the risk:**

"I understand the deadline pressure. Before we decide, let me share what we'd be skipping:

- We haven't tested the payment integration with the new tax logic
- The user import feature has had 3 bugs in the last sprint — we haven't regression-tested it with the new data format
- We have no coverage of the edge case where a user's session expires mid-checkout

If any of these fail in production, here's the likely impact: [quantify — lost transactions, user data issues, support ticket volume]

My recommendation: we release with these risks documented and a rollback plan ready. Alternatively, we delay 2 days to cover the payment integration specifically, since that's the highest risk."

**Key principles:**
- Don't say "no, we can't release" — you don't make the release decision
- Do say "here are the specific risks of releasing untested" — with specifics, not vague concerns
- Always provide options, not just problems
- Get the decision in writing — email or Slack — so the risk acceptance is documented

**Never agree silently.** If you say nothing and the release causes a production incident, you share responsibility for not raising the risk.

---

**Q3. You find a P1 bug on the day of release — walk through your decision process.**

A P1 bug on release day is a moment that tests both your technical judgment and professional maturity.

**Step 1 — Verify it's actually a bug and actually P1**
Reproduce it clearly. Confirm the severity:
- P1: System unusable, data loss, security breach, payment failure
- Are there workarounds? P1 usually means no acceptable workaround

**Step 2 — Determine scope**
- How many users are affected?
- Is it in a core flow or an edge case?
- Is the feature new (not previously in production) or a regression?

**Step 3 — Notify immediately**
"I've found a bug in the payment confirmation step that causes duplicate orders when a user's network times out. I can reproduce it 100% with these steps. This is a P1 because it causes duplicate charges."

Do not sit on this. Do not try to find a workaround yourself and only mention it if you can't solve it. Escalate within 10 minutes of confirming it.

**Step 4 — Provide the information the team needs to decide:**
- Severity and reproducibility
- Affected user percentage (all users? specific config?)
- Is there a workaround even if imperfect?
- Can it be patched with a small code change or does it require architectural work?
- What's the cost of delay vs. the cost of releasing with this bug?

**Step 5 — Support the decision, don't make it unilaterally**
The release decision belongs to the product owner, delivery manager, and engineering lead — not QA. Your role is to give accurate information, clearly and quickly. Their role is to weigh it against business context you may not have.

**Step 6 — If they release anyway:**
Document your findings, when you reported them, and the decision taken. Not to "protect yourself" politically, but because this is data the team needs to learn from in the retrospective.

---

**Q4. How do you test a feature that isn't done yet?**

Testing incomplete features requires coordination and creativity. The key is not waiting until "done" to get involved.

**Approach 1 — Test in layers as each layer completes**
```
Backend API done but UI not:
→ Test the API completely with Postman/RestAssured now
→ Write the API test automation now
→ When UI arrives, the backend is already verified

UI done but backend is mock:
→ Test UI behavior with mocked responses
→ Verify loading states, error states, empty states
→ When real backend integrates, run the full E2E test
```

**Approach 2 — Feature flags and partial deployments**
If the feature is behind a flag, you can test it in staging while users don't see it in production.

**Approach 3 — Test spec review before coding**
QA should review acceptance criteria and write test scenarios BEFORE development starts. This:
- Catches ambiguous requirements early
- Gives developers a definition of "done" they can code toward
- Speeds up testing because you know exactly what to test when the feature arrives

**Approach 4 — Pair with developers during development**
Sit with (or screen-share with) the developer as they build the feature. You can:
- Point out missing error handling as they code it
- Validate behavior immediately without waiting for a formal handoff
- Catch "oh, that requirement is ambiguous" before it's already built the wrong way

**Approach 5 — Exploratory testing on partial builds**
Even incomplete features have testable parts. Test what's there. Document what's missing. File placeholder test cases for incomplete areas.

---

**Q5. How do you handle a developer who says "that's not a bug, it's by design"?**

This disagreement happens on every project. The goal is to resolve it with facts and collaboration, not to win an argument.

**Step 1 — Approach with curiosity, not confrontation**
"Help me understand the intended behavior here — I want to make sure I'm testing against the right spec."

This is not capitulation. It's gathering information. Sometimes the developer is right — the requirement was changed after the test was written, or you misread the spec.

**Step 2 — Go to the source of truth**
- Is there a user story, acceptance criteria, or specification?
- What does the AC say exactly?
- If the AC is ambiguous, that's the real problem — not the developer vs. QA disagreement

```
Bug report: User can submit the form with a negative price — system accepts it.
Dev response: "That's by design — price validation is the frontend's job."
QA response: "The AC says 'price must be a positive number.' Let me pull up the story."
```

**Step 3 — Involve the product owner if spec is clear but dev disagrees**
Bring the spec to a quick 3-way conversation: dev, QA, product owner. 5 minutes resolves what could be a 2-day email thread.

**Step 4 — If the feature genuinely has no spec:**
"Since there's no specification on this behavior, can we get a quick decision from the PO on whether negative prices should be rejected? I'll update my test cases either way."

**Step 5 — Accept that you might be wrong**
QA is not always right. If the developer explains the design decision and it makes sense, update your test and move on. Document the agreed behavior in the ticket.

**What to never do:** Silently close the bug. Silently mark it as "won't fix." If the behavior is genuinely risky to users even if "by design," escalate it as a UX or product risk — not as a code bug.

---

*End of Technical Interview Q&A Guide. Every scenario here has been validated against real project situations. The best preparation is to practice saying these answers out loud — technical accuracy matters, but delivery and confidence matter equally in an interview.*
