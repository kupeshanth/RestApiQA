# Selenium WebDriver — Full Interview Q&A
## Every Concept as a Real Interview Question | Complete Answers + Code

---

## SECTION 1 — ARCHITECTURE & SETUP

---

**Q1: What is Selenium WebDriver and how does it work internally?**

**A:** Selenium WebDriver is an open-source test automation framework for web browsers. It provides a programming interface to write test code in Java, Python, C#, etc., and drive browsers programmatically.

**How it works internally:**
```
Your Java Test Code
      ↓
Selenium Client Library (selenium-java.jar)
      ↓  [W3C WebDriver Protocol — HTTP requests with JSON body]
ChromeDriver / GeckoDriver / SafariDriver  (browser-specific server)
      ↓  [Browser-native DevTools Protocol or native API calls]
Chrome / Firefox / Safari
```

Every Selenium command you call (`.click()`, `.sendKeys()`, `.findElement()`) becomes an **HTTP request** sent to the browser driver server. The driver translates it into browser-native instructions and sends the result back.

Example: `driver.findElement(By.id("username"))` sends:
```
POST http://localhost:9515/session/{sessionId}/element
Body: {"using": "id", "value": "username"}
```

**Why this matters for interviews:** Understanding the HTTP layer explains why Selenium is slower than Playwright (which uses CDP directly in-process), and why network issues can affect test stability.

---

**Q2: What is the difference between Selenium WebDriver, Selenium RC, and Selenium IDE?**

**A:**
| | Selenium IDE | Selenium RC | Selenium WebDriver |
|--|-------------|------------|-------------------|
| Type | Browser plugin | Server + JS injection | Direct browser API |
| Languages | Record/replay | Java, Python, C# | Java, Python, C#, Ruby, JS |
| Architecture | Record in browser | RC server injects JS into page | W3C protocol to native driver |
| Speed | Fast to create | Slow (JS injection overhead) | Fast |
| Status | Still used for quick tests | Deprecated | Industry standard |
| Limitation | No complex logic | Same-origin policy issues | None significant |

**Current relevance:** Selenium IDE for quick record-and-check. Selenium WebDriver (Selenium 4) is the only production-grade option. Nobody uses RC anymore.

---

**Q3: What is WebDriverManager and why is it needed?**

**A:** WebDriverManager automatically downloads and configures the browser driver binary that matches your installed browser version.

**Without WebDriverManager — manual pain:**
```java
// 1. Go to ChromeDriver website
// 2. Check your Chrome version (e.g. 119.0.6045.105)
// 3. Download matching chromedriver.exe
// 4. Place it somewhere and hardcode the path
System.setProperty("webdriver.chrome.driver", "C:/drivers/chromedriver.exe");
WebDriver driver = new ChromeDriver();
// Chrome auto-updates → version mismatch → all tests fail → repeat process
```

**With WebDriverManager — one line:**
```java
WebDriverManager.chromedriver().setup();   // downloads correct version automatically
WebDriver driver = new ChromeDriver();
// Chrome updates → WebDriverManager handles it automatically
```

**pom.xml dependency:**
```xml
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>5.6.3</version>
    <scope>test</scope>
</dependency>
```

---

**Q4: How do you set up a complete BaseTest class with proper browser initialisation?**

**A:**
```java
package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.*;

public class BaseTest {

    // ThreadLocal gives each parallel thread its OWN driver — no conflicts
    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public WebDriver getDriver() {
        return tlDriver.get();
    }

    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        WebDriver driver;

        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ffOpts = new FirefoxOptions();
                ffOpts.addArguments("--headless");
                driver = new FirefoxDriver(ffOpts);
                break;

            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--headless");          // no UI — required for CI
                opts.addArguments("--no-sandbox");        // required in Docker/Linux CI
                opts.addArguments("--disable-gpu");       // required in some environments
                opts.addArguments("--disable-dev-shm-usage"); // prevents memory issues in CI
                opts.addArguments("--window-size=1920,1080");
                opts.addArguments("--disable-extensions");
                driver = new ChromeDriver(opts);
                break;
        }

        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        tlDriver.set(driver);   // store in THIS thread's slot
    }

    @AfterMethod(alwaysRun = true)  // alwaysRun ensures cleanup even after test failure
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();     // closes browser + kills driver process
            tlDriver.remove();      // CRITICAL: prevent memory leak in thread pools
        }
    }
}
```

**Why `alwaysRun = true` on @AfterMethod?**
Without it, if a test throws an exception, TestNG may skip the @AfterMethod, leaving browsers open. `alwaysRun = true` ensures cleanup always happens.

---

## SECTION 2 — LOCATORS

---

**Q5: What are all the locator strategies in Selenium? Which do you prefer and why?**

**A:** Selenium provides 8 locator strategies. Priority order (best to worst):

```java
// 1. By.id — fastest, most stable
driver.findElement(By.id("username"));
// Use when: element has a unique, stable id attribute

// 2. By.name — for form elements
driver.findElement(By.name("email"));
// Use when: element has a name attribute (common on inputs)

// 3. By.cssSelector — fast, flexible, widely supported
driver.findElement(By.cssSelector("#loginForm input[type='email']"));
driver.findElement(By.cssSelector(".btn.btn-primary"));
driver.findElement(By.cssSelector("[data-testid='submit-btn']"));
driver.findElement(By.cssSelector("div.container > button:first-child"));
// Use when: no id/name, but element has stable class or attribute

// 4. By.xpath — most powerful, slowest
driver.findElement(By.xpath("//button[@type='submit']"));
driver.findElement(By.xpath("//button[contains(text(),'Login')]"));
driver.findElement(By.xpath("//label[text()='Email']/following-sibling::input"));
// Use when: no other strategy works, especially for text-based location

// 5. By.linkText — for hyperlinks, exact match
driver.findElement(By.linkText("Forgot Password?"));

// 6. By.partialLinkText — for hyperlinks, partial match
driver.findElement(By.partialLinkText("Forgot"));

// 7. By.className — risky when multiple classes
driver.findElement(By.className("submit-btn"));  // only one class allowed!

// 8. By.tagName — returns all elements of that type
driver.findElements(By.tagName("tr"));   // all table rows
```

**Recommendation:** `id > cssSelector > xpath`. Ask devs to add `data-testid` attributes for stable automation locators.

---

**Q6: What is the difference between CSS Selector and XPath? When do you use each?**

**A:**

| | CSS Selector | XPath |
|--|------------|-------|
| Speed | Faster | Slower |
| Direction | Forward only | Forward AND backward |
| Text matching | Cannot match element text | Can match text: `//button[text()='OK']` |
| Complexity | Simpler syntax | More complex but more powerful |
| Browser support | All browsers natively | All browsers |
| Axes | No | Yes (parent, sibling, ancestor) |

```java
// CSS Selector examples:
By.cssSelector("#id")                          // by id
By.cssSelector(".class")                       // by class
By.cssSelector("input[type='text']")           // by attribute
By.cssSelector("div.form > input.field")       // parent > direct child
By.cssSelector("table tr:nth-child(3)")        // 3rd row
By.cssSelector("[data-testid='submit']")       // data attribute

// XPath — use when CSS can't do it:
By.xpath("//button[contains(text(),'Submit')]")           // text content
By.xpath("//label[text()='Email']/following-sibling::input") // sibling
By.xpath("//div[@class='error']/parent::form")            // parent
By.xpath("(//button[@class='btn'])[2]")                   // 2nd matching element
By.xpath("//input[not(@disabled)]")                       // negation
```

**Rule:** Use CSS when the element has stable attributes. Switch to XPath when you need text matching, traversing to siblings/parents, or complex conditions.

---

**Q7: How do you write a robust XPath that won't break when the page changes slightly?**

**A:**
```java
// FRAGILE XPath — breaks if anything in the hierarchy changes
By.xpath("/html/body/div[2]/form/div[1]/input")   // absolute path — never use

// FRAGILE — depends on exact class string
By.xpath("//input[@class='form-control input-lg']")  // breaks if class order changes

// ROBUST — use contains() for partial attribute matches
By.xpath("//input[contains(@class,'form-control')]")

// ROBUST — use text content
By.xpath("//button[contains(text(),'Sign')]")

// ROBUST — use data attributes (most stable)
By.xpath("//input[@data-testid='username-input']")

// ROBUST — combine multiple attributes
By.xpath("//input[@type='email' and @name='email']")

// ROBUST — XPath axes for relationships
By.xpath("//label[text()='Email Address']/following-sibling::input")
By.xpath("//div[contains(@class,'error')]//span[@role='alert']")

// Finding by index (use as last resort)
By.xpath("(//button[@type='submit'])[1]")   // first submit button

// Avoiding brittle patterns:
// ❌ //div[3]/input   — positional, breaks on UI change
// ❌ //*[@id='col-1'] — auto-generated IDs change
// ✅ //input[@placeholder='Search products']
// ✅ //button[@data-testid='checkout-btn']
```

---

**Q8: What is the difference between `findElement()` and `findElements()`?**

**A:**
```java
// findElement() — finds FIRST matching element
// Throws NoSuchElementException if nothing found (test fails immediately)
WebElement btn = driver.findElement(By.cssSelector(".submit-btn"));
btn.click();

// findElements() — finds ALL matching elements
// Returns empty List<WebElement> if nothing found (no exception!)
List<WebElement> rows = driver.findElements(By.tagName("tr"));
System.out.println("Row count: " + rows.size());

// KEY DIFFERENCE: findElements() never throws NoSuchElementException
// Use it to CHECK if an element exists without try-catch:
boolean elementExists = !driver.findElements(By.id("optional-banner")).isEmpty();
if (elementExists) {
    driver.findElement(By.id("optional-banner")).click();
}

// Use findElements() for:
// - Counting elements (table rows, product cards)
// - Iterating over all matching elements
// - Safe existence checks without exception handling

// Getting specific element from list
List<WebElement> links = driver.findElements(By.tagName("a"));
WebElement thirdLink = links.get(2);   // 0-indexed

// Filtering elements from list
List<WebElement> enabledButtons = driver.findElements(By.tagName("button"))
    .stream()
    .filter(WebElement::isEnabled)
    .collect(Collectors.toList());
```

---

## SECTION 3 — ACTIONS & INTERACTIONS

---

**Q9: What is the difference between `element.click()` and `Actions.click()` and JS click?**

**A:**
```java
// 1. element.click() — standard WebDriver click
// WebDriver moves focus to element, then clicks
// Fails if: element not visible, element disabled, another element on top
element.click();

// 2. Actions.click() — simulates real mouse movement to element then click
// More realistic mouse simulation
Actions actions = new Actions(driver);
actions.moveToElement(element).click().perform();
// Use when: hover-triggered menus, elements that need mouse-over state

// 3. JavaScript click — bypasses browser event handling
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].click();", element);
// Use when: element is behind another element (intercepted)
//           element is outside viewport
//           normal click is intercepted by overlay

// Real-world scenario:
// Cookie banner covers the Submit button → ElementClickInterceptedException
// Fix: close the cookie banner first, OR use JS click as last resort
try {
    submitBtn.click();
} catch (ElementClickInterceptedException e) {
    // First try to close the overlay
    driver.findElement(By.id("cookieBanner")).findElement(By.tagName("button")).click();
    submitBtn.click();
    // If that doesn't work:
    // js.executeScript("arguments[0].click();", submitBtn);
}
```

---

**Q10: How do you handle a dropdown that is NOT an HTML `<select>` element?**

**A:**
```java
// HTML <select> — use Select class
Select dropdown = new Select(driver.findElement(By.id("country")));
dropdown.selectByVisibleText("United Kingdom");
dropdown.selectByValue("UK");
dropdown.selectByIndex(3);

// Custom dropdown (div/ul based) — manual approach
// Step 1: Click the dropdown trigger to open it
driver.findElement(By.cssSelector(".dropdown-toggle")).click();

// Step 2: Wait for options to appear
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dropdown-menu")));

// Step 3: Find and click the option by text
driver.findElement(By.xpath("//ul[@class='dropdown-menu']//a[text()='United Kingdom']")).click();

// Step 4: Verify selection
String selected = driver.findElement(By.cssSelector(".dropdown-toggle")).getText();
Assert.assertEquals(selected, "United Kingdom");

// Generic reusable method for custom dropdowns:
public void selectFromCustomDropdown(By trigger, By optionLocator, String optionText) {
    driver.findElement(trigger).click();
    wait.until(ExpectedConditions.visibilityOfElementLocated(optionLocator));
    List<WebElement> options = driver.findElements(optionLocator);
    options.stream()
        .filter(opt -> opt.getText().trim().equals(optionText))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Option not found: " + optionText))
        .click();
}
```

---

**Q11: How do you perform keyboard actions in Selenium?**

**A:**
```java
import org.openqa.selenium.Keys;

// Single key presses on element
element.sendKeys(Keys.ENTER);
element.sendKeys(Keys.TAB);        // move to next field
element.sendKeys(Keys.BACK_SPACE);
element.sendKeys(Keys.DELETE);
element.sendKeys(Keys.ESCAPE);
element.sendKeys(Keys.ARROW_DOWN);
element.sendKeys(Keys.ARROW_UP);
element.sendKeys(Keys.HOME);       // beginning of field
element.sendKeys(Keys.END);        // end of field

// Keyboard shortcuts (chord)
element.sendKeys(Keys.chord(Keys.CONTROL, "a"));   // Ctrl+A (select all)
element.sendKeys(Keys.chord(Keys.CONTROL, "c"));   // Ctrl+C (copy)
element.sendKeys(Keys.chord(Keys.CONTROL, "v"));   // Ctrl+V (paste)
element.sendKeys(Keys.chord(Keys.CONTROL, "z"));   // Ctrl+Z (undo)
element.sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB)); // Shift+Tab (reverse tab)

// Using Actions class for complex key combinations
Actions actions = new Actions(driver);
actions.keyDown(Keys.SHIFT)
    .click(firstElement)
    .click(lastElement)           // Shift+click for multi-select
    .keyUp(Keys.SHIFT)
    .perform();

// Type text character by character (triggers each keydown/keypress/keyup event)
actions.sendKeys(element, "Hello World").perform();

// Real-world: submit form with Enter key
searchField.sendKeys("selenium webdriver");
searchField.sendKeys(Keys.ENTER);   // triggers search without clicking the button
```

---

**Q12: How do you scroll to an element and scroll to the bottom of the page?**

**A:**
```java
JavascriptExecutor js = (JavascriptExecutor) driver;

// Scroll to a specific element
WebElement footer = driver.findElement(By.id("footer"));
js.executeScript("arguments[0].scrollIntoView(true);", footer);
// true = align to top of viewport, false = align to bottom

// Scroll into view with smooth behavior
js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", footer);

// Scroll down by pixel amount
js.executeScript("window.scrollBy(0, 500)");   // scroll down 500px

// Scroll to the bottom of the page
js.executeScript("window.scrollTo(0, document.body.scrollHeight)");

// Scroll to the top of the page
js.executeScript("window.scrollTo(0, 0)");

// Scroll to specific coordinates
js.executeScript("window.scrollTo(0, 1000)");  // scroll to y=1000px

// Using Actions class for scroll (Selenium 4+)
WebElement element = driver.findElement(By.id("target"));
new Actions(driver).scrollToElement(element).perform();
new Actions(driver).scrollByAmount(0, 500).perform();  // scroll by 500px down

// Practical: lazy-loaded content (infinite scroll)
public void scrollToLoadAllItems() {
    long lastHeight = (long) js.executeScript("return document.body.scrollHeight");
    while (true) {
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        Thread.sleep(1000);   // wait for new items to load
        long newHeight = (long) js.executeScript("return document.body.scrollHeight");
        if (newHeight == lastHeight) break;   // no new content loaded
        lastHeight = newHeight;
    }
}
```

---

## SECTION 4 — WAITS

---

**Q13: What is the difference between implicit wait, explicit wait, and fluent wait? Which do you use?**

**A:**

```java
// ── IMPLICIT WAIT ─────────────────────────────────────────────────────────────
// WHAT: Globally tells WebDriver to wait up to N seconds for ANY element to appear.
// WHERE: Set once in BaseTest setUp() — applies to every findElement call.
// PROBLEM: Makes NEGATIVE tests slow — checking element is NOT there waits full timeout.
// PROBLEM: Combining with explicit wait causes unpredictable total wait times.

driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
// Every driver.findElement() will retry for up to 10 seconds before throwing NoSuchElementException

// ── EXPLICIT WAIT ─────────────────────────────────────────────────────────────
// WHAT: Waits for a SPECIFIC CONDITION on a SPECIFIC element.
// WHERE: Wherever you need to wait for dynamic content.
// BETTER: More precise, doesn't affect other elements.

WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

// All available ExpectedConditions:
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));   // visible
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loader"))); // gone
wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));         // clickable
wait.until(ExpectedConditions.presenceOfElementLocated(By.id("form")));       // in DOM (even hidden)
wait.until(ExpectedConditions.textToBePresentInElement(el, "Success"));       // has text
wait.until(ExpectedConditions.textToBePresentInElementValue(el, "admin"));    // value has text
wait.until(ExpectedConditions.attributeContains(By.id("st"), "class", "active")); // attribute
wait.until(ExpectedConditions.urlContains("/dashboard"));                     // URL
wait.until(ExpectedConditions.titleIs("Dashboard | MyApp"));                  // page title
wait.until(ExpectedConditions.alertIsPresent());                              // alert popup
wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("myFrame"));    // iframe
wait.until(ExpectedConditions.numberOfElementsToBe(By.tagName("tr"), 10));    // count
wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.tagName("li"), 0)); // more than
wait.until(ExpectedConditions.stalenessOf(oldElement));                       // element gone

// Custom condition using lambda:
wait.until(d -> d.findElement(By.id("status")).getText().equals("Loaded"));

// ── FLUENT WAIT ───────────────────────────────────────────────────────────────
// WHAT: Like explicit wait BUT with configurable polling interval and exception ignoring.
// USE: For elements that appear/disappear unpredictably (e.g. after a file upload).

Wait<WebDriver> fluentWait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(30))         // max total wait
    .pollingEvery(Duration.ofSeconds(2))          // check every 2 seconds
    .ignoring(NoSuchElementException.class)       // don't fail on this exception
    .ignoring(StaleElementReferenceException.class)
    .withMessage("Element did not appear within 30 seconds");

WebElement result = fluentWait.until(d -> {
    WebElement el = d.findElement(By.id("result"));
    return el.isDisplayed() ? el : null;   // return null = keep waiting
});
```

**My recommendation:** Use **explicit wait** as the primary strategy. Set a small implicit wait (5-10s) as a safety net. Use fluent wait for unpredictable async elements like file processing or external API responses.

---

**Q14: Why should you NEVER mix implicit and explicit waits?**

**A:**
```
Mixing them causes unpredictable and doubled wait times.

Example: implicit wait = 10s, explicit wait = 15s
When element is not found:
- Explicit wait calls findElement every 500ms
- Each findElement call triggers implicit wait (10s!)
- Total wait = 15s × 10s = up to 150 seconds instead of 15s

The Selenium documentation explicitly warns: "Do not mix implicit and explicit waits"

Safe patterns:
- Use ONLY implicit wait (simple but imprecise)
- Use ONLY explicit wait (preferred — precise)
- Set implicit to 0 if using explicit: driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0))
```

---

**Q15: Why should you never use `Thread.sleep()` in automation?**

**A:**
```
Thread.sleep() is a hardcoded pause. It has two problems:

1. TOO SLOW: If the element appears in 200ms but you sleep 3000ms,
   you waste 2800ms per test. 100 tests × 2.8s = 4.7 minutes wasted.

2. STILL FLAKY: If the element takes 3100ms but you sleep 3000ms,
   the test still fails. A longer sleep means more wasted time, not more reliability.

The correct approach: wait for the ACTUAL CONDITION.

BAD:
Thread.sleep(3000);
WebElement result = driver.findElement(By.id("result"));

GOOD:
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));

Exception: Thread.sleep() is acceptable ONLY for debugging — never in production tests.
```

---

## SECTION 5 — PAGE OBJECT MODEL

---

**Q16: What is the Page Object Model? Why is it used in automation?**

**A:**
```java
// Page Object Model (POM) — DESIGN PATTERN
// Each web page = one Java class
// Locators and actions in the page class, assertions in test classes

// ── WITHOUT POM — locators scattered everywhere ───────────────────────────────
@Test
public void loginTest() {
    driver.findElement(By.id("username")).sendKeys("admin");
    driver.findElement(By.id("password")).sendKeys("Admin@123");
    driver.findElement(By.id("loginBtn")).click();
    Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"));
}
// Problem: if id="username" changes to id="user_email":
// → search every test file → update every test → error-prone

// ── WITH POM — single source of truth ────────────────────────────────────────
// LoginPage.java (page class)
public class LoginPage {
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(id = "username")         private WebElement usernameField;
    @FindBy(id = "password")         private WebElement passwordField;
    @FindBy(id = "loginBtn")         private WebElement loginButton;
    @FindBy(css = ".error-msg")      private WebElement errorMessage;
    @FindBy(linkText = "Forgot?")    private WebElement forgotPasswordLink;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);  // initialises @FindBy fields
    }

    public DashboardPage login(String username, String password) {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
        return new DashboardPage(driver);   // returns next page object
    }

    public String getErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(errorMessage));
        return errorMessage.getText();
    }

    public boolean isLoaded() {
        return loginButton.isDisplayed() && driver.getCurrentUrl().contains("/login");
    }
}

// LoginTest.java (test class — reads like plain English)
public class LoginTest extends BaseTest {

    @Test
    public void validLogin_redirectsToDashboard() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.navigate();
        DashboardPage dashboard = loginPage.login("admin", "Admin@123");
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard should be loaded after login");
    }

    @Test
    public void wrongPassword_showsError() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.navigate();
        loginPage.login("admin", "wrongpassword");
        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid credentials");
    }
}

// Benefits:
// 1. Maintainability: change id="username" → update ONLY LoginPage.java
// 2. Readability: loginPage.login() tells the story, not driver.findElement(By.id(...)
// 3. Reusability: login() called by 50 tests, written once
// 4. Separation: page structure vs test assertions are separate
```

---

**Q17: What is `PageFactory` and what does `@FindBy` do?**

**A:**
```java
// PageFactory — lazy initialisation of @FindBy annotated fields
// Without PageFactory: WebElement fields are null until explicitly found
// With PageFactory: fields are automatically located when first accessed

// @FindBy — tells PageFactory which locator to use for that field
@FindBy(id = "username")                        // By.id("username")
@FindBy(name = "email")                         // By.name("email")
@FindBy(css = ".submit-btn")                    // By.cssSelector(".submit-btn")
@FindBy(xpath = "//button[@type='submit']")     // By.xpath(...)
@FindBy(linkText = "Forgot Password")           // By.linkText(...)
@FindBy(tagName = "h1")                         // By.tagName("h1")

// Multiple locators — tries each in order, uses first that finds element
@FindBys({
    @FindBy(id = "submitBtn"),
    @FindBy(css = ".submit-btn"),
    @FindBy(xpath = "//button[@type='submit']")
})
private WebElement submitButton;

// @FindAll — finds all elements matching ANY of the locators
@FindAll({
    @FindBy(css = ".error"),
    @FindBy(css = ".alert-danger")
})
private List<WebElement> errorMessages;

// IMPORTANT: PageFactory uses lazy proxies
// The element is NOT found when PageFactory.initElements() runs
// It is found when you FIRST ACCESS the field (call a method on it)
// This means: StaleElementReferenceException is LESS common with PageFactory
// because each access triggers a fresh findElement internally

// Initialise in constructor:
public LoginPage(WebDriver driver) {
    this.driver = driver;
    PageFactory.initElements(driver, this);  // sets up lazy proxies for all @FindBy fields
}
```

---

## SECTION 6 — SPECIAL ELEMENTS

---

**Q18: How do you handle iframes in Selenium?**

**A:**
```java
// An iframe (inline frame) is a page within a page.
// To interact with elements INSIDE an iframe, you must first switch to it.

// Method 1: Switch by name or id attribute
driver.switchTo().frame("iframeName");
driver.switchTo().frame("iframeId");

// Method 2: Switch by index (0 = first iframe on page)
driver.switchTo().frame(0);

// Method 3: Switch by WebElement (most reliable)
WebElement iframeElement = driver.findElement(By.cssSelector("iframe.content-frame"));
driver.switchTo().frame(iframeElement);

// Now you can interact with elements INSIDE the iframe
driver.findElement(By.id("insideIframe")).sendKeys("text");
driver.findElement(By.cssSelector("button.submit")).click();

// Return to the main document
driver.switchTo().defaultContent();   // back to the main page (top level)
driver.switchTo().parentFrame();      // back up ONE level (if nested iframes)

// Real-world example: CKEditor (rich text editor) is always in an iframe
WebElement editorIframe = driver.findElement(By.cssSelector("iframe.cke_wysiwyg_frame"));
driver.switchTo().frame(editorIframe);
driver.findElement(By.tagName("body")).sendKeys("This is my article text");
driver.switchTo().defaultContent();
driver.findElement(By.id("saveBtn")).click();

// Common mistake: finding element in main page when it's inside iframe
// Result: NoSuchElementException even though element clearly exists on screen
// Fix: always check if the element is inside an iframe using browser DevTools
```

---

**Q19: How do you handle JavaScript alerts, confirms, and prompts?**

**A:**
```java
// Three types of browser dialogs:
// alert()   — "OK" only
// confirm() — "OK" and "Cancel"
// prompt()  — text input + "OK" and "Cancel"

// Always wait for alert before switching
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.alertIsPresent());

// Switch to the alert
Alert alert = driver.switchTo().alert();

// Read the message
String message = alert.getText();
System.out.println("Alert says: " + message);

// Handle alert (OK only)
alert.accept();   // clicks OK

// Handle confirm (OK or Cancel)
alert.accept();   // clicks OK
alert.dismiss();  // clicks Cancel

// Handle prompt (type + OK or Cancel)
alert.sendKeys("My typed response");
alert.accept();   // submit with typed text

// Full example — delete confirmation
driver.findElement(By.id("deleteBtn")).click();          // triggers confirm dialog
wait.until(ExpectedConditions.alertIsPresent());
Alert confirmDialog = driver.switchTo().alert();
Assert.assertEquals(confirmDialog.getText(), "Are you sure you want to delete?");
confirmDialog.accept();   // confirm deletion

// What happens if no alert is present and you call switchTo().alert()?
// Throws: NoAlertPresentException
// Prevention: always use wait.until(alertIsPresent()) before switchTo().alert()
```

---

**Q20: How do you handle multiple browser windows or tabs?**

**A:**
```java
// Get the current (main) window handle before opening new window
String mainWindowHandle = driver.getWindowHandle();
System.out.println("Main window: " + mainWindowHandle);

// Click something that opens a new window/tab
driver.findElement(By.linkText("Open in new tab")).click();

// Wait for new window to open
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.numberOfWindowsToBe(2));

// Get all window handles
Set<String> allHandles = driver.getWindowHandles();

// Switch to the new window
for (String handle : allHandles) {
    if (!handle.equals(mainWindowHandle)) {
        driver.switchTo().window(handle);
        break;
    }
}

// Now you are in the new window — interact with it
System.out.println("New window URL: " + driver.getCurrentUrl());
Assert.assertTrue(driver.getTitle().contains("New Page"));

// Close the new window and go back to main
driver.close();   // close CURRENT window (the new one)
driver.switchTo().window(mainWindowHandle);   // switch back to main window

// Verify you are back on main window
Assert.assertEquals(driver.getTitle(), "Main Page Title");

// Opening a new tab programmatically (Selenium 4)
driver.switchTo().newWindow(WindowType.TAB);    // opens blank tab
driver.get("https://newpage.com");

// Opening a new window programmatically (Selenium 4)
driver.switchTo().newWindow(WindowType.WINDOW);
```

---

**Q21: How do you upload a file in Selenium?**

**A:**
```java
// Standard file upload (input type="file")
// WebDriver can directly sendKeys() the file path to the input element
WebElement uploadInput = driver.findElement(By.id("fileInput"));
// or: By.cssSelector("input[type='file']")

// Provide the ABSOLUTE path to the file
uploadInput.sendKeys("C:\\Users\\Kupeshanth\\Downloads\\test-file.pdf");   // Windows
uploadInput.sendKeys("/home/user/Downloads/test-file.pdf");                  // Linux/Mac

// If the input is hidden (display:none), make it visible first
((JavascriptExecutor) driver).executeScript(
    "arguments[0].style.display = 'block';", uploadInput);
uploadInput.sendKeys("/path/to/file.pdf");

// Verify upload was successful
String uploadedFileName = driver.findElement(By.css(".uploaded-filename")).getText();
Assert.assertTrue(uploadedFileName.contains("test-file.pdf"));

// For custom file upload buttons (not input type=file):
// Robot class — simulates actual keyboard/mouse at OS level
driver.findElement(By.id("customUploadBtn")).click();   // opens OS file dialog
Robot robot = new Robot();
StringSelection fileSelection = new StringSelection("/path/to/file.pdf");
Toolkit.getDefaultToolkit().getSystemClipboard().setContents(fileSelection, null);
robot.keyPress(KeyEvent.VK_CONTROL);
robot.keyPress(KeyEvent.VK_V);      // paste path
robot.keyRelease(KeyEvent.VK_V);
robot.keyRelease(KeyEvent.VK_CONTROL);
robot.keyPress(KeyEvent.VK_ENTER);  // confirm
robot.keyRelease(KeyEvent.VK_ENTER);
```

---

**Q22: How do you handle checkboxes and radio buttons?**

**A:**
```java
// Checkbox
WebElement agreeCheckbox = driver.findElement(By.id("agreeTerms"));

// Check (tick) — only if not already checked
if (!agreeCheckbox.isSelected()) {
    agreeCheckbox.click();
}

// Uncheck — only if already checked
if (agreeCheckbox.isSelected()) {
    agreeCheckbox.click();
}

// Verify state
Assert.assertTrue(agreeCheckbox.isSelected(), "Checkbox should be checked");

// Radio button — group of options, one selected at a time
List<WebElement> genderRadios = driver.findElements(By.name("gender"));
for (WebElement radio : genderRadios) {
    if (radio.getAttribute("value").equals("male")) {
        radio.click();
        break;
    }
}

// Using XPath to select specific radio by value
driver.findElement(By.xpath("//input[@type='radio' and @value='female']")).click();

// Verify which radio is selected
List<WebElement> radios = driver.findElements(By.name("gender"));
String selectedValue = radios.stream()
    .filter(WebElement::isSelected)
    .map(r -> r.getAttribute("value"))
    .findFirst()
    .orElse("none");
System.out.println("Selected gender: " + selectedValue);
```

---

## SECTION 7 — WAITS & EXCEPTIONS

---

**Q23: What is `StaleElementReferenceException` and how do you handle it?**

**A:**
```java
// Cause: The element you found is no longer attached to the DOM.
// This happens when:
// 1. Page refreshes after you found the element
// 2. DOM is re-rendered by JavaScript (e.g. AJAX table reload)
// 3. Element is removed and recreated with same locator
// 4. Navigation to a new page

// BAD — element found, then DOM refreshes, then click fails
WebElement submitBtn = driver.findElement(By.id("submitBtn"));
driver.findElement(By.id("reloadData")).click();   // DOM refreshes!
submitBtn.click();   // StaleElementReferenceException!

// FIX 1: Re-find element after any DOM change
driver.findElement(By.id("reloadData")).click();
Thread.sleep(500);  // wait for DOM update
driver.findElement(By.id("submitBtn")).click();  // find again

// FIX 2: Retry loop (most robust)
public void clickWithRetry(By locator, int maxRetries) {
    for (int attempt = 0; attempt < maxRetries; attempt++) {
        try {
            driver.findElement(locator).click();
            return;   // success — exit loop
        } catch (StaleElementReferenceException e) {
            if (attempt == maxRetries - 1) throw e;  // last attempt — rethrow
            System.out.println("Stale element, retrying... attempt " + (attempt + 1));
        }
    }
}

// FIX 3: Use explicit wait to handle staleness
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.refreshed(
    ExpectedConditions.elementToBeClickable(By.id("submitBtn"))
));
driver.findElement(By.id("submitBtn")).click();

// FIX 4: PageFactory handles staleness automatically (lazy proxies re-find each time)
// If using @FindBy with PageFactory, StaleElementReferenceException is less common
// because each method call re-finds the element

// NOTE: StaleElementReferenceException vs NoSuchElementException:
// Stale = element WAS found, now gone from DOM
// NoSuchElement = element was NEVER found
```

---

**Q24: What is `ElementClickInterceptedException` and how do you fix it?**

**A:**
```java
// Cause: The element you want to click is covered by another element
// Common causes:
// - Cookie consent banner floating over page
// - Loading spinner/overlay covering the element
// - Sticky header covering element when scrolling
// - Tooltip appearing over element

// FIX 1: Close the covering element first
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
try {
    WebElement cookieBanner = wait.until(
        ExpectedConditions.elementToBeClickable(By.id("cookieAcceptBtn")));
    cookieBanner.click();   // close cookie banner
} catch (TimeoutException e) {
    // No cookie banner — that's fine, continue
}
driver.findElement(By.id("mainSubmitBtn")).click();

// FIX 2: Wait for overlay to disappear
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingSpinner")));
driver.findElement(By.id("submitBtn")).click();

// FIX 3: Scroll element into centre of viewport
JavascriptExecutor js = (JavascriptExecutor) driver;
WebElement el = driver.findElement(By.id("submitBtn"));
js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);
el.click();

// FIX 4: JavaScript click (bypasses the interception entirely)
WebElement el = driver.findElement(By.id("submitBtn"));
((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);

// FIX 5: Move to element using Actions
new Actions(driver).moveToElement(el).click().perform();

// Best practice: try fixes in order. JS click is last resort — it bypasses
// browser event handlers and may not trigger all events the app needs.
```

---

**Q25: What is `NoSuchElementException` vs `ElementNotInteractableException`?**

**A:**
```java
// NoSuchElementException:
// - Element does NOT exist in the DOM at all
// - Causes: wrong locator, element not yet loaded, wrong page

// Fix:
// 1. Check your locator is correct (inspect the page)
// 2. Add a wait: wait.until(presenceOfElementLocated(locator))
// 3. Check you're on the right page: System.out.println(driver.getCurrentUrl());

// ElementNotInteractableException:
// - Element IS in the DOM but cannot be interacted with
// - Causes: element is hidden (display:none, visibility:hidden), disabled, off-screen

// Check element state:
WebElement el = driver.findElement(By.id("submitBtn"));
System.out.println("Displayed: " + el.isDisplayed());  // false if hidden
System.out.println("Enabled:   " + el.isEnabled());    // false if disabled
System.out.println("Size:      " + el.getSize());       // (0,0) if invisible

// Fix:
// 1. Wait for element to become visible/enabled
wait.until(ExpectedConditions.elementToBeClickable(By.id("submitBtn")));

// 2. Scroll to element
js.executeScript("arguments[0].scrollIntoView(true);", el);

// 3. Use JavaScript for hidden input fields
js.executeScript("arguments[0].value = 'my value';", el);

// Summary table:
// Exception                      | Element in DOM | Element visible
// NoSuchElementException         |      NO        |      -
// ElementNotInteractableException|      YES       |      NO / disabled
// StaleElementReferenceException |  WAS, now NO   |      -
// ElementClickInterceptedException|    YES        |     YES (but covered)
```

---

## SECTION 8 — PARALLEL EXECUTION

---

**Q26: How do you run Selenium tests in parallel across different browsers?**

**A:**
```xml
<!-- testng.xml — parallel="tests" runs each <test> block in its own thread -->
<suite name="Cross Browser Suite" parallel="tests" thread-count="3">

    <test name="Chrome Tests">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="tests.LoginTest"/>
            <class name="tests.CheckoutTest"/>
        </classes>
    </test>

    <test name="Firefox Tests">
        <parameter name="browser" value="firefox"/>
        <classes>
            <class name="tests.LoginTest"/>
            <class name="tests.CheckoutTest"/>
        </classes>
    </test>

    <test name="Edge Tests">
        <parameter name="browser" value="edge"/>
        <classes>
            <class name="tests.LoginTest"/>
        </classes>
    </test>

</suite>
```

```java
// BaseTest — MUST use ThreadLocal. Without it, threads share one browser → chaos.
public class BaseTest {
    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public WebDriver getDriver() { return tlDriver.get(); }

    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        WebDriver driver = DriverFactory.createDriver(browser);
        tlDriver.set(driver);   // store in THIS thread's slot
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            tlDriver.remove();   // MUST remove to prevent memory leak
        }
    }
}

// DriverFactory.java — creates driver based on browser parameter
public class DriverFactory {
    public static WebDriver createDriver(String browser) {
        switch (browser.toLowerCase()) {
            case "firefox": 
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();
            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                opts.addArguments("--headless", "--no-sandbox", "--disable-gpu");
                return new ChromeDriver(opts);
        }
    }
}
```

---

**Q27: What happens if you don't use ThreadLocal and run tests in parallel?**

**A:**
```java
// WITHOUT ThreadLocal — shared driver (WRONG)
public class BaseTest {
    protected WebDriver driver;   // ONE instance, shared across ALL threads

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();   // Thread 1 sets driver
                                        // Thread 2 OVERWRITES driver
    }
    // Thread 1's actions and Thread 2's actions happen on Thread 2's browser
    // RESULT: random failures, actions on wrong pages, race conditions
}

// WITH ThreadLocal — each thread has its own (CORRECT)
public class BaseTest {
    private static ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public WebDriver getDriver() { return tlDriver.get(); }   // THIS thread's driver

    @BeforeMethod
    public void setUp() {
        tlDriver.set(new ChromeDriver());   // Thread 1 gets its own Chrome
                                             // Thread 2 gets its own Chrome
    }
    // Thread 1 acts on its browser, Thread 2 acts on its browser — no conflict
}

// Memory leak without remove():
@AfterMethod
public void tearDown() {
    getDriver().quit();
    tlDriver.remove();   // CRITICAL: without this, reference stays in ThreadLocal
                          // In thread pools (Maven Surefire), threads are reused
                          // → old WebDriver references accumulate → OutOfMemoryError
}
```

---

## SECTION 9 — SCREENSHOTS & ADVANCED

---

**Q28: How do you automatically take a screenshot when a test fails?**

**A:**
```java
// ITestListener approach — cleanest in TestNG

public class ScreenshotListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        // Get the driver from the test instance
        Object testInstance = result.getInstance();
        WebDriver driver = ((BaseTest) testInstance).getDriver();

        try {
            // Capture screenshot as file
            TakesScreenshot ts = (TakesScreenshot) driver;
            File srcFile = ts.getScreenshotAs(OutputType.FILE);

            // Build unique filename: FAIL_LoginTest_validLogin_1701234567890.png
            String fileName = "FAIL_"
                + result.getTestClass().getRealClass().getSimpleName() + "_"
                + result.getName() + "_"
                + System.currentTimeMillis() + ".png";

            // Save to screenshots folder
            File destFile = new File("screenshots/" + fileName);
            destFile.getParentFile().mkdirs();   // create folder if not exists
            FileUtils.copyFile(srcFile, destFile);

            System.out.println("Screenshot saved: " + destFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Could not take screenshot: " + e.getMessage());
        }
    }
}

// Register listener in testng.xml:
// <listeners>
//     <listener class-name="listeners.ScreenshotListener"/>
// </listeners>

// For Allure reports — attach screenshot directly to report
public class AllureScreenshotListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = ((BaseTest) result.getInstance()).getDriver();
        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Allure.getLifecycle().addAttachment("Screenshot", "image/png", "png", screenshot);
    }
}
```

---

**Q29: How do you get text, attributes, and CSS values from elements?**

**A:**
```java
WebElement element = driver.findElement(By.id("myElement"));

// Get visible text content
String text = element.getText();
// Returns: visible text only (like what user sees), strips HTML tags

// Get attribute value
String href    = element.getAttribute("href");
String value   = element.getAttribute("value");    // input field value
String classes = element.getAttribute("class");    // all classes as string
String style   = element.getAttribute("style");
String dataId  = element.getAttribute("data-id"); // custom data attributes
String disabled = element.getAttribute("disabled"); // null if not disabled, "true" if disabled

// Get CSS property value
String color      = element.getCssValue("color");         // "rgba(0, 0, 0, 1)"
String fontSize   = element.getCssValue("font-size");     // "16px"
String display    = element.getCssValue("display");       // "none" if hidden
String bgColor    = element.getCssValue("background-color");
String fontWeight = element.getCssValue("font-weight");   // "700" for bold

// Get element dimensions and position
int width  = element.getSize().getWidth();
int height = element.getSize().getHeight();
int x = element.getLocation().getX();   // x coordinate from left
int y = element.getLocation().getY();   // y coordinate from top

// Check element state
boolean displayed = element.isDisplayed();   // visible on screen
boolean enabled   = element.isEnabled();     // can interact
boolean selected  = element.isSelected();    // checkbox/radio selected

// Get inner HTML via JavaScript
String innerHTML = (String) js.executeScript("return arguments[0].innerHTML;", element);
String innerText = (String) js.executeScript("return arguments[0].innerText;", element);

// Get text including hidden elements (getText() only returns visible text)
String allText = (String) js.executeScript("return arguments[0].textContent;", element);
```

---

**Q30: What is the difference between `driver.close()` and `driver.quit()`?**

**A:**
```java
// driver.close()
// - Closes ONLY the current browser window/tab
// - If multiple windows open, switches to the next one after closing
// - Does NOT end the WebDriver session
// - Does NOT kill the ChromeDriver/GeckoDriver process

// driver.quit()
// - Closes ALL browser windows and tabs
// - Ends the WebDriver session (session ID becomes invalid)
// - Kills the ChromeDriver/GeckoDriver process
// - Releases all resources

// ALWAYS use driver.quit() in @AfterMethod
@AfterMethod(alwaysRun = true)
public void tearDown() {
    if (getDriver() != null) {
        getDriver().quit();   // NOT close()!
        tlDriver.remove();
    }
}

// When to use close():
// Only when you open a new tab and want to close it, then go back to main
String mainHandle = driver.getWindowHandle();
driver.switchTo().newWindow(WindowType.TAB);
driver.get("https://another-page.com");
// ... interact with new tab ...
driver.close();   // close just this tab
driver.switchTo().window(mainHandle);   // back to main tab

// Common mistake: calling close() in @AfterMethod
// If your test opens 3 tabs, close() only closes one
// The other 2 remain open → browser process stays in memory → test suite slows down
```

---

**Q31: What is `JavascriptExecutor` and when do you use it?**

**A:**
```java
// JavascriptExecutor — runs JavaScript code within the browser context
JavascriptExecutor js = (JavascriptExecutor) driver;

// 1. Click elements that normal click can't handle
js.executeScript("arguments[0].click();", element);
// Use when: element intercepted, hidden, outside viewport, React/Angular events

// 2. Set field value directly (bypasses all event handlers)
js.executeScript("arguments[0].value = 'test@email.com';", emailField);
// Use when: sendKeys doesn't work (date pickers, readonly fields set via JS)
// WARNING: this doesn't trigger keydown/keyup events — might not work for all apps

// 3. Scroll operations
js.executeScript("window.scrollTo(0, document.body.scrollHeight)");  // bottom
js.executeScript("window.scrollTo(0, 0)");                            // top
js.executeScript("arguments[0].scrollIntoView({block: 'center'});", el);  // to element
js.executeScript("window.scrollBy(0, 300)");                           // down 300px

// 4. Get element text (including hidden)
String text = (String) js.executeScript("return arguments[0].textContent;", el);

// 5. Remove element attribute
js.executeScript("arguments[0].removeAttribute('readonly');", readonlyInput);
readonlyInput.clear();
readonlyInput.sendKeys("new value");

// 6. Get/set localStorage and sessionStorage
js.executeScript("localStorage.setItem('token', 'test-token-123')");
String token = (String) js.executeScript("return localStorage.getItem('token')");

// 7. Check if page is fully loaded
Boolean loaded = (Boolean) js.executeScript("return document.readyState === 'complete'");

// 8. Highlight element (for debugging)
js.executeScript("arguments[0].style.border='3px solid red'", el);

// 9. Take full page screenshot (with certain tools)
// Standard Selenium takes viewport only; JS helps capture more

// When NOT to use JavascriptExecutor:
// - Don't use as first option — use only when WebDriver methods fail
// - JS click bypasses browser events — app might not receive all events correctly
// - Makes tests harder to debug when things go wrong
```

---

**Q32: How do you verify that a page has fully loaded before interacting with it?**

**A:**
```java
// Method 1: Wait for document.readyState == "complete"
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
wait.until(d -> ((JavascriptExecutor) d)
    .executeScript("return document.readyState").equals("complete"));

// Method 2: Wait for specific element that appears only when page is ready
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pageLoadedIndicator")));

// Method 3: Wait for AJAX calls to finish (if page uses jQuery)
wait.until(d -> (Boolean)((JavascriptExecutor) d)
    .executeScript("return jQuery.active == 0"));

// Method 4: Wait for URL to change after navigation
driver.findElement(By.id("dashboardLink")).click();
wait.until(ExpectedConditions.urlContains("/dashboard"));

// Method 5: Wait for page title to change
wait.until(ExpectedConditions.titleContains("Dashboard"));

// Method 6: Custom method combining all checks
public void waitForPageLoad(WebDriverWait wait) {
    // Wait for ready state
    wait.until(d -> ((JavascriptExecutor) d)
        .executeScript("return document.readyState").equals("complete"));

    // Wait for no active AJAX (if jQuery is used)
    try {
        wait.until(d -> (Boolean)((JavascriptExecutor) d)
            .executeScript("return jQuery.active == 0"));
    } catch (Exception e) {
        // jQuery not used on this page — skip this check
    }

    // Wait for no loading spinner
    try {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.css(".loading-spinner")));
    } catch (Exception e) {
        // No spinner on this page
    }
}
```

---

## SECTION 10 — REAL-WORLD SCENARIOS

---

**Q33: How do you test a table — verify data, count rows, find a specific row?**

**A:**
```java
// Assuming table structure:
// <table id="resultsTable">
//   <thead><tr><th>Name</th><th>Status</th><th>Date</th></tr></thead>
//   <tbody>
//     <tr><td>John</td><td>Active</td><td>2024-01-15</td></tr>
//     ...
//   </tbody>
// </table>

// 1. Count data rows (excluding header)
List<WebElement> rows = driver.findElements(By.cssSelector("#resultsTable tbody tr"));
int rowCount = rows.size();
System.out.println("Total rows: " + rowCount);
Assert.assertEquals(rowCount, 5, "Table should have 5 rows");

// 2. Get all values from a specific column
List<WebElement> nameCells = driver.findElements(By.cssSelector("#resultsTable tbody tr td:nth-child(1)"));
List<String> names = nameCells.stream().map(WebElement::getText).collect(Collectors.toList());
System.out.println("Names: " + names);
Assert.assertTrue(names.contains("John"), "John should be in the table");

// 3. Find a specific row by content
List<WebElement> allRows = driver.findElements(By.cssSelector("#resultsTable tbody tr"));
for (WebElement row : allRows) {
    String name = row.findElement(By.cssSelector("td:nth-child(1)")).getText();
    if (name.equals("John")) {
        String status = row.findElement(By.cssSelector("td:nth-child(2)")).getText();
        Assert.assertEquals(status, "Active", "John's status should be Active");
        break;
    }
}

// 4. Using XPath to find row by cell value (more concise)
WebElement johnRow = driver.findElement(By.xpath("//table[@id='resultsTable']//tr[td[text()='John']]"));
String johnStatus = johnRow.findElement(By.xpath("td[2]")).getText();

// 5. Click a button in a specific row (e.g. "Edit" for John)
WebElement editBtn = driver.findElement(
    By.xpath("//table//tr[td[text()='John']]//button[text()='Edit']"));
editBtn.click();

// 6. Verify table is sorted alphabetically by first column
List<WebElement> nameElements = driver.findElements(By.cssSelector("tbody tr td:first-child"));
List<String> displayedNames = nameElements.stream().map(WebElement::getText).collect(Collectors.toList());
List<String> sortedNames = new ArrayList<>(displayedNames);
Collections.sort(sortedNames);
Assert.assertEquals(displayedNames, sortedNames, "Table should be sorted alphabetically");
```

---

**Q34: How do you handle a scenario where an element sometimes appears and sometimes doesn't (conditional element)?**

**A:**
```java
// Scenario: cookie banner appears on first visit, not on return visits
// Scenario: promotional popup appears randomly for some users

// Method 1: findElements() — returns empty list if not found (no exception)
List<WebElement> cookieBanners = driver.findElements(By.id("cookieBanner"));
if (!cookieBanners.isEmpty()) {
    cookieBanners.get(0).findElement(By.id("acceptBtn")).click();
    System.out.println("Cookie banner dismissed");
} else {
    System.out.println("No cookie banner present");
}

// Method 2: try-catch with short wait
public boolean clickIfPresent(By locator, int waitSeconds) {
    try {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
        WebElement el = shortWait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
        return true;   // element found and clicked
    } catch (TimeoutException e) {
        return false;  // element not present — that's OK
    }
}

// Usage:
boolean bannerDismissed = clickIfPresent(By.id("cookieBanner"), 3);

// Method 3: Fluent wait with null return for conditional check
Wait<WebDriver> wait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(5))
    .pollingEvery(Duration.ofMillis(500))
    .ignoring(NoSuchElementException.class);

WebElement banner = wait.until(d -> {
    List<WebElement> els = d.findElements(By.id("cookieBanner"));
    return els.isEmpty() ? null : els.get(0);  // null = keep waiting, element = stop
});

// In @BeforeMethod: dismiss optional overlays before test starts
@BeforeMethod
public void dismissOptionalOverlays() {
    clickIfPresent(By.id("cookieBanner"), 2);
    clickIfPresent(By.css(".promotional-popup .close"), 2);
}
```

---

**Q35: How do you handle hover-triggered dropdown menus?**

**A:**
```java
// Scenario: navbar has a hover-triggered dropdown
// <nav>
//   <a class="nav-link" id="productsMenu">Products</a>
//   <ul class="dropdown-menu" id="productsDropdown" style="display:none">
//     <li><a href="/electronics">Electronics</a></li>
//     <li><a href="/clothing">Clothing</a></li>
//   </ul>
// </nav>

Actions actions = new Actions(driver);

// Step 1: Move mouse to the trigger (hover over it)
WebElement productsMenu = driver.findElement(By.id("productsMenu"));
actions.moveToElement(productsMenu).perform();

// Step 2: Wait for dropdown to appear
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("productsDropdown")));

// Step 3: Click the option in the dropdown
driver.findElement(By.linkText("Electronics")).click();

// Step 4: Verify navigation
Assert.assertTrue(driver.getCurrentUrl().contains("/electronics"));

// Alternative: chain the hover and click in one Actions sequence
actions
    .moveToElement(productsMenu)
    .pause(Duration.ofMillis(500))    // small pause for dropdown to animate open
    .moveToElement(driver.findElement(By.linkText("Clothing")))
    .click()
    .perform();

// If JavaScript triggers the hover state (CSS :hover doesn't work in headless):
js.executeScript("arguments[0].dispatchEvent(new MouseEvent('mouseover'));", productsMenu);
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("productsDropdown")));
```

---

**Q36: What are all the common Selenium exceptions and how do you fix each?**

**A:**

```java
// 1. NoSuchElementException
// Cause: element not in DOM, wrong locator, element not yet loaded
// Fix:
wait.until(ExpectedConditions.presenceOfElementLocated(By.id("myElement")));
// Also check: are you on the correct page? System.out.println(driver.getCurrentUrl());

// 2. StaleElementReferenceException
// Cause: DOM changed after you found the element
// Fix: re-find element after any DOM-changing action
public void clickWithRetry(By locator) {
    for (int i = 0; i < 3; i++) {
        try { driver.findElement(locator).click(); return; }
        catch (StaleElementReferenceException e) { if (i==2) throw e; }
    }
}

// 3. ElementNotInteractableException
// Cause: element in DOM but hidden/disabled/off-screen
// Fix: wait for clickability, scroll into view
wait.until(ExpectedConditions.elementToBeClickable(By.id("btn")));

// 4. ElementClickInterceptedException
// Cause: another element is on top (overlay, banner, tooltip)
// Fix: dismiss overlay first, scroll, or JS click
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("overlay")));
driver.findElement(By.id("btn")).click();

// 5. TimeoutException
// Cause: ExpectedCondition not met within timeout period
// Fix: increase timeout, check if condition is ever true
// Also: add logging to see what state the page is actually in

// 6. NoAlertPresentException
// Cause: switchTo().alert() called when no alert exists
// Fix: always wait first
wait.until(ExpectedConditions.alertIsPresent());
driver.switchTo().alert().accept();

// 7. WebDriverException: chrome not reachable
// Cause: browser crashed, was closed externally, or driver killed
// Fix: ensure proper tearDown, check system resources

// 8. SessionNotCreatedException
// Cause: ChromeDriver version doesn't match Chrome version
// Fix: use WebDriverManager.chromedriver().setup();

// 9. MoveTargetOutOfBoundsException
// Cause: Actions trying to move to coordinates outside viewport
// Fix: scroll element into viewport first
js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
new Actions(driver).moveToElement(element).perform();

// 10. InvalidSelectorException
// Cause: CSS selector or XPath syntax is wrong
// Fix: test your selector in browser DevTools console first
// CSS: $$('#myId .myClass') in Chrome console
// XPath: $x('//button[@id="myId"]') in Chrome console
```

---

**Q37: How do you get the current URL, page title, and page source?**

**A:**
```java
// Current URL
String currentUrl = driver.getCurrentUrl();
System.out.println("Current URL: " + currentUrl);
Assert.assertTrue(currentUrl.contains("/dashboard"), "Should be on dashboard");
Assert.assertEquals(currentUrl, "https://app.com/dashboard");

// Page title
String title = driver.getTitle();
System.out.println("Page title: " + title);
Assert.assertEquals(title, "Dashboard | MyApp");
Assert.assertTrue(title.startsWith("Dashboard"));

// Page source (full HTML)
String pageSource = driver.getPageSource();
Assert.assertTrue(pageSource.contains("Welcome, Admin"));
// Note: pageSource contains HTML — text in page source may differ from getText()

// Useful for debugging:
if (!driver.getCurrentUrl().contains("/login")) {
    System.out.println("Wrong page! URL: " + driver.getCurrentUrl());
    System.out.println("Title: " + driver.getTitle());
}
```

---

**Q38: How do you manage cookies in Selenium?**

**A:**
```java
// Get all cookies
Set<Cookie> allCookies = driver.manage().getCookies();
for (Cookie cookie : allCookies) {
    System.out.println(cookie.getName() + " = " + cookie.getValue());
}

// Get a specific cookie
Cookie sessionCookie = driver.manage().getCookieNamed("session_id");
String sessionValue = sessionCookie.getValue();

// Add a cookie (to set auth state without logging in via UI)
Cookie authCookie = new Cookie.Builder("auth_token", "test-token-abc123")
    .domain("app.com")
    .path("/")
    .isHttpOnly(true)
    .isSecure(true)
    .build();
driver.manage().addCookie(authCookie);
driver.navigate().refresh();   // reload page to apply cookie

// Delete specific cookie
driver.manage().deleteCookieNamed("session_id");

// Delete all cookies
driver.manage().deleteAllCookies();

// Real-world use: skip login for faster tests
// 1. Log in manually once via UI
// 2. Capture the auth cookie
// 3. In subsequent tests: add cookie + navigate directly to protected page
@BeforeClass
public void loginOnce() {
    driver.get("https://app.com/login");
    loginPage.login("admin", "Admin@123");
    authCookie = driver.manage().getCookieNamed("auth_token");  // save cookie
}

@BeforeMethod
public void goToPageWithAuth() {
    driver.get("https://app.com");           // navigate to domain
    driver.manage().addCookie(authCookie);   // inject auth cookie
    driver.get("https://app.com/dashboard"); // go directly to protected page
    // No login needed! Faster tests.
}
```

---

---

**Q: What is Selenium Grid and why do you use it?**

**A:** Selenium Grid distributes test execution across multiple machines simultaneously. A Hub receives test commands and delegates them to registered Nodes (machines with browsers).

```
Architecture:
  Your Test Code → Hub (central server) → Node 1 (Chrome on Windows)
                                        → Node 2 (Firefox on Mac)
                                        → Node 3 (Safari on Mac)
```

```java
// Run tests against Selenium Grid instead of local browser
// Only ONE change needed — swap ChromeDriver for RemoteWebDriver

// WITHOUT Grid (local):
WebDriver driver = new ChromeDriver();

// WITH Grid (remote):
ChromeOptions options = new ChromeOptions();
URL gridUrl = new URL("http://grid-hub-ip:4444");
WebDriver driver = new RemoteWebDriver(gridUrl, options);
// Everything else in your tests stays identical

// For Selenium Grid 4 (simpler — no Hub/Node, just Grid):
URL gridUrl = new URL("http://localhost:4444");
WebDriver driver = new RemoteWebDriver(gridUrl, new ChromeOptions());
```

Start Grid locally:
```bash
# Download selenium-server.jar then:
java -jar selenium-server.jar standalone   # Grid 4 — one command
# Tests connect to: http://localhost:4444
```

Cloud alternatives (no setup needed): BrowserStack, Sauce Labs, LambdaTest — same RemoteWebDriver code, just different URL + credentials.

---

**Q: What is Shadow DOM and how do you interact with it in Selenium?**

**A:** Shadow DOM is a web standard that lets components encapsulate their internal DOM. Regular `driver.findElement()` cannot penetrate Shadow DOM — elements inside it are invisible to standard locators.

```java
// Check if element is in Shadow DOM: inspect it in browser DevTools
// If you see #shadow-root in the element tree → Shadow DOM

// Method 1: JavaScript to pierce the shadow boundary
WebElement shadowHost = driver.findElement(By.cssSelector("my-component"));
// Execute JS to get the shadow root, then find element inside it
WebElement shadowRoot = (WebElement) ((JavascriptExecutor) driver)
    .executeScript("return arguments[0].shadowRoot", shadowHost);
WebElement button = shadowRoot.findElement(By.cssSelector("button.submit"));
button.click();

// Method 2: Selenium 4 native Shadow DOM support (Java 4.x+)
WebElement shadowHost = driver.findElement(By.cssSelector("my-component"));
SearchContext shadowRoot = shadowHost.getShadowRoot();
WebElement button = shadowRoot.findElement(By.cssSelector("button.submit"));
button.click();

// Multi-level Shadow DOM (shadow inside shadow)
SearchContext outerShadow = driver.findElement(By.id("outer")).getShadowRoot();
SearchContext innerShadow = outerShadow.findElement(By.id("inner")).getShadowRoot();
innerShadow.findElement(By.id("target")).click();
```

---

**Q: What new features did Selenium 4 introduce?**

**A:** Key Selenium 4 improvements over Selenium 3:

```java
// 1. Relative Locators — find elements by position relative to other elements
WebElement passwordField = driver.findElement(
    RelativeLocator.with(By.tagName("input")).below(By.id("username"))
);
// Other relative locators: above(), leftOf(), rightOf(), near()

// 2. Native Shadow DOM support (without JavaScript)
SearchContext shadow = driver.findElement(By.id("host")).getShadowRoot();

// 3. Chrome DevTools Protocol (CDP) — directly control browser
((HasDevTools) driver).getDevTools().createSession();
// Enables: network throttling, console listener, geolocation mock, etc.

// 4. Selenium Manager — auto-downloads drivers (like WebDriverManager built-in)
// Just instantiate ChromeDriver() — no WebDriverManager.setup() needed
WebDriver driver = new ChromeDriver();   // Selenium 4 handles driver automatically

// 5. New window/tab API
driver.switchTo().newWindow(WindowType.TAB);      // open new tab
driver.switchTo().newWindow(WindowType.WINDOW);   // open new window

// 6. Improved Screenshots — element-level screenshot
WebElement element = driver.findElement(By.id("chart"));
File shot = element.getScreenshotAs(OutputType.FILE);  // screenshot of just this element
```

---

## SECTION — TEST REPORTING FOR SELENIUM + TESTNG

---

### Three Reporting Options

```
Option A: Maven Surefire Report   → zero setup, auto-generated, good enough
Option B: TestNG Built-in Report  → zero setup, slightly nicer layout
Option C: Allure Report           → setup needed, most professional, industry standard
```

---

### OPTION A — Maven Surefire Report (Zero Setup)

**How it works:** Every `mvn test` run automatically generates HTML and XML reports.

**Location:**
```
target/
└── surefire-reports/
    ├── index.html             ← OPEN THIS in browser
    ├── TEST-TestSuite.xml     ← JUnit XML (for Jenkins/GitHub Actions)
    └── TestSuite.txt          ← plain text log
```

**How to open:**
```powershell
# Windows PowerShell
start target/surefire-reports/index.html

# From IntelliJ — no terminal needed:
# Left panel → target → surefire-reports → index.html
# Right-click → Open In → Browser → Chrome
```

**What it shows:**
```
Test Suite:  REST API Test Suite
Tests:       26   Failures: 0   Errors: 0   Skipped: 0
Time:        18.4s

LoginTest:
  ✓ validLogin_redirectsToDashboard    1.2s
  ✓ wrongPassword_showsError           0.8s

CheckoutTest:
  ✓ addToCart_itemAppearsInCart        2.1s
  ✗ checkout_withExpiredCard           0.3s  ← FAIL (click here for error)
```

---

### OPTION B — TestNG Built-in Report (Zero Setup)

**Location:**
```
test-output/
├── index.html              ← TestNG dashboard
├── emailable-report.html   ← simple one-page report for email/sharing
└── testng-results.xml
```

**How to open:**
```powershell
start test-output/index.html
start test-output/emailable-report.html   # simpler, best for sharing
```

**What it shows:**
```
Suite: Selenium Test Suite
Total: 26   Passed: 25   Failed: 1   Skipped: 0

Groups:
  smoke:      10 tests   All passed ✓
  regression: 16 tests   1 failed ✗

LoginTest (8 tests):
  validLogin_redirectsToDashboard     PASS   1.2s
  wrongPassword_showsError            PASS   0.8s
  emptyUsername_showsValidation       PASS   0.4s
```

---

### OPTION C — Allure Report (Professional — Most Used in Enterprise)

Allure shows: step-by-step execution, screenshots on failure, request/response logs, trend history, severity levels.

#### STEP 1 — Add to pom.xml

```xml
<!-- Inside <dependencies> -->
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-testng</artifactId>
    <version>2.24.0</version>
    <scope>test</scope>
</dependency>

<!-- Inside <build><plugins> -->
<plugin>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-maven</artifactId>
    <version>2.12.0</version>
    <configuration>
        <reportVersion>2.24.0</reportVersion>
    </configuration>
</plugin>
```

Refresh Maven after adding: IntelliJ → Maven panel → ↻

#### STEP 2 — Add Allure Annotations to Tests

```java
import io.qameta.allure.*;
import io.qameta.allure.testng.AllureTestNg;

@Epic("Login Feature")                  // top-level grouping
@Feature("Authentication")             // sub-group
public class LoginTest extends BaseTest {

    @Story("Valid Login")
    @Severity(SeverityLevel.CRITICAL)   // BLOCKER, CRITICAL, NORMAL, MINOR, TRIVIAL
    @Description("Verify valid credentials redirect to dashboard")
    @Test
    public void validLogin_redirectsToDashboard() {
        Allure.step("Navigate to login page", () -> {
            driver.get("https://app.com/login");
        });

        Allure.step("Enter valid credentials", () -> {
            driver.findElement(By.id("username")).sendKeys("admin");
            driver.findElement(By.id("password")).sendKeys("Admin@123");
        });

        Allure.step("Click login button", () -> {
            driver.findElement(By.id("loginBtn")).click();
        });

        Allure.step("Verify dashboard is shown", () -> {
            Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        });
    }

    @Story("Invalid Login")
    @Severity(SeverityLevel.NORMAL)
    @Link(name = "JIRA Ticket", url = "https://jira.company.com/LOGIN-123")
    @Test
    public void wrongPassword_showsError() {
        // test code
    }
}
```

#### STEP 3 — Auto Screenshot on Failure (Attaches to Allure Report)

```java
// Add to your TestListener class:
public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        Object testInstance = result.getInstance();
        WebDriver driver = ((BaseTest) testInstance).getDriver();

        // Attach screenshot to Allure report
        byte[] screenshot = ((TakesScreenshot) driver)
            .getScreenshotAs(OutputType.BYTES);

        Allure.getLifecycle().addAttachment(
            "Screenshot on Failure",   // name shown in report
            "image/png",               // type
            "png",                     // extension
            screenshot
        );

        // Also attach page source for debugging
        Allure.getLifecycle().addAttachment(
            "Page Source",
            "text/html",
            "html",
            driver.getPageSource().getBytes()
        );
    }
}
```

Register the listener in testng.xml:
```xml
<suite name="Suite">
    <listeners>
        <listener class-name="listeners.TestListener"/>
    </listeners>
    ...
</suite>
```

#### STEP 4 — Run Tests (Generates allure-results/)

```powershell
mvn test
# OR
./apache-maven-3.9.6/bin/mvn test

# After run:
# allure-results/   ← raw JSON data (created automatically)
# target/surefire-reports/  ← Surefire report (also created)
```

#### STEP 5 — Install Allure CLI

```powershell
# Option A: Chocolatey (run PowerShell as Admin)
choco install allure

# Option B: Scoop
scoop install allure

# Option C: Manual download
# https://github.com/allure-framework/allure2/releases
# Download allure-X.X.X.zip → extract to C:\tools\allure
# Add C:\tools\allure\bin to PATH

# Verify:
allure --version   # should show version number
```

#### STEP 6 — Generate and Open Allure Report

```powershell
# Open live server (best — opens browser automatically)
allure serve allure-results/

# Generate static files then open
allure generate allure-results/ --clean -o allure-report/
allure open allure-report/

# Via Maven plugin (no Allure CLI needed)
./apache-maven-3.9.6/bin/mvn allure:serve
```

---

### What Allure Report Shows

```
Dashboard:
  ○ 25 passed   ● 1 failed   ○ 0 skipped
  Pie chart + trend graph

Suites tab:
  Login Feature
    Authentication
      ✓ Valid Login → CRITICAL → 1.8s
        Steps:
          → Navigate to login page      ✓
          → Enter valid credentials     ✓
          → Click login button          ✓
          → Verify dashboard is shown   ✓

      ✗ Wrong Password → NORMAL → 0.3s
        Steps:
          → Navigate to login page      ✓
          → Enter wrong credentials     ✓
          → Click login button          ✓
          → Verify error message        ✗  ← FAILED HERE
        Screenshot: [attached image]
        Error: Expected "Invalid credentials" but was "Something went wrong"

Graphs tab:
  - Test duration distribution
  - Pass/fail trend across last 10 runs
```

---

### All Reporting Commands — Quick Reference

```powershell
# ── SUREFIRE (auto-generated) ─────────────────────────────────────────────────
mvn test
start target/surefire-reports/index.html

# ── TESTNG (auto-generated) ───────────────────────────────────────────────────
mvn test
start test-output/index.html
start test-output/emailable-report.html

# ── ALLURE ────────────────────────────────────────────────────────────────────
mvn test                                                    # generates allure-results/
allure serve allure-results/                                # live server (best)
allure generate allure-results/ --clean -o allure-report/  # static HTML
allure open allure-report/                                  # open static HTML
mvn allure:serve                                            # via Maven plugin

# ── OPEN IN IntelliJ (no terminal needed) ─────────────────────────────────────
# target → surefire-reports → index.html → right-click → Open In → Browser
# test-output → index.html → right-click → Open In → Browser
```

---

### Q: What reporting do you use in your automation framework?

```
In my current projects I use two levels of reporting:

1. Maven Surefire for quick CI checks — it generates HTML and JUnit XML automatically.
   Jenkins/GitHub Actions reads the XML to show pass/fail in the pipeline dashboard.

2. Allure for detailed debugging and stakeholder reporting.
   It captures step-by-step execution, screenshots on failure, and request/response
   logs (when AllureRestAssured filter is added to RestAssured setup).
   The trend graph shows if pass rate is improving over sprints.

For my Selenium framework specifically, I implemented a TestNG ITestListener
that attaches a screenshot to the Allure report automatically when any test fails.
This means when a developer asks "why did this test fail?", I can show them
the exact screenshot from the moment of failure without them running the test again.
```

*Last updated: Senior QA Interview Preparation | Reporting: Surefire + TestNG + Allure*
*GitHub: https://github.com/kupeshanth/RestApiQA/guides/01-SELENIUM-COMPLETE-GUIDE.md*
