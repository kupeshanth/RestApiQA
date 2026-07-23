# Part 4 — Selenium WebDriver | 100 Questions | Full Answers + Code

> CV context: Kupeshanth Kupenthiran — Trainee QA at Qoria Lanka, Intern at Cerexio.
> Java, Selenium, TestNG. Singer Page BDD project (Serenity+Cucumber).

---

## Q1. What is Selenium? What are its components?

Selenium is an open-source suite of tools for automating web browsers.

**Components:**
| Component | Purpose |
|-----------|---------|
| Selenium WebDriver | Programmatically controls browsers via browser drivers |
| Selenium IDE | Record-and-playback browser extension |
| Selenium Grid | Run tests on multiple browsers/machines in parallel |

```java
// Selenium WebDriver — the component you use daily
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

WebDriver driver = new ChromeDriver();
driver.get("https://example.com");
System.out.println(driver.getTitle());
driver.quit();
```

**Key facts:**
- Selenium supports Java, Python, C#, JavaScript, Ruby, Kotlin.
- WebDriver communicates with browsers via the W3C WebDriver protocol.
- No built-in reporting — use Allure, ExtentReports, or TestNG reports.

---

## Q2. What is WebDriver architecture?

```
Test Code (Java)
      |
      v
WebDriver API (selenium-java.jar)
      |
      v  HTTP (W3C WebDriver Protocol / JSON Wire Protocol)
      |
      v
Browser Driver (chromedriver.exe / geckodriver / msedgedriver)
      |
      v
Browser (Chrome / Firefox / Edge)
```

- Test code sends commands to the **WebDriver API**.
- The API translates them to HTTP requests conforming to the **W3C WebDriver Protocol**.
- The **browser driver** receives requests and drives the actual browser.
- The browser driver returns responses (element details, page source, etc.) back up the chain.

```java
// WebDriverManager handles downloading the correct browser driver version
WebDriverManager.chromedriver().setup();
WebDriver driver = new ChromeDriver();
// Behind the scenes: driver.get("url") → HTTP POST /session/{id}/url → chromedriver → Chrome
```

---

## Q3. What is the difference between Selenium RC, IDE, and WebDriver?

| Feature | Selenium RC (v1) | Selenium IDE | Selenium WebDriver |
|---------|-----------------|--------------|-------------------|
| Technology | JavaScript injection, proxy server | Browser extension | Direct browser control via native API |
| Language | Java, Python, etc. | Selenese (own language) | Java, Python, C#, JS, etc. |
| Speed | Slow | N/A (record/playback) | Fast |
| Parallel | Limited | No | Yes (Grid) |
| Status | Deprecated | Active (export to code) | Active — current standard |

---

## Q4. What is WebDriverManager and why use it?

Without WebDriverManager you must manually download chromedriver.exe and keep it in sync with your Chrome version. WebDriverManager automates this.

```java
// WITHOUT WebDriverManager — manual, breaks when Chrome updates
System.setProperty("webdriver.chrome.driver", "C:/drivers/chromedriver.exe");
WebDriver driver = new ChromeDriver();

// WITH WebDriverManager — automatic driver management
import io.github.bonigarcia.wdm.WebDriverManager;

WebDriverManager.chromedriver().setup();  // downloads correct version automatically
WebDriver driver = new ChromeDriver();

// Other browsers
WebDriverManager.firefoxdriver().setup();
WebDriverManager.edgedriver().setup();

// Pin a specific version
WebDriverManager.chromedriver().driverVersion("114.0.5735.90").setup();

// pom.xml dependency
// <dependency>
//     <groupId>io.github.bonigarcia</groupId>
//     <artifactId>webdrivermanager</artifactId>
//     <version>5.7.0</version>
// </dependency>
```

---

## Q5. How do you launch Chrome, Firefox, Edge browsers?

```java
// Chrome
WebDriverManager.chromedriver().setup();
ChromeOptions chromeOptions = new ChromeOptions();
chromeOptions.addArguments("--start-maximized");
chromeOptions.addArguments("--no-sandbox");
chromeOptions.addArguments("--disable-dev-shm-usage");
WebDriver driver = new ChromeDriver(chromeOptions);

// Firefox
WebDriverManager.firefoxdriver().setup();
FirefoxOptions ffOptions = new FirefoxOptions();
ffOptions.addArguments("--width=1920", "--height=1080");
WebDriver driver = new FirefoxDriver(ffOptions);

// Edge
WebDriverManager.edgedriver().setup();
EdgeOptions edgeOptions = new EdgeOptions();
WebDriver driver = new EdgeDriver(edgeOptions);

// DriverFactory pattern — from config/system property
public static WebDriver createDriver() {
    String browser = System.getProperty("browser", "chrome").toLowerCase();
    switch (browser) {
        case "chrome":
            WebDriverManager.chromedriver().setup();
            return new ChromeDriver();
        case "firefox":
            WebDriverManager.firefoxdriver().setup();
            return new FirefoxDriver();
        case "edge":
            WebDriverManager.edgedriver().setup();
            return new EdgeDriver();
        default:
            throw new IllegalArgumentException("Unknown browser: " + browser);
    }
}
```

---

## Q6. What are all locator strategies? Priority order?

```java
// All 8 locator strategies (Selenium 4 adds relative locators separately)

// 1. ID — fastest, most reliable (use first if available)
driver.findElement(By.id("username"));

// 2. Name
driver.findElement(By.name("email"));

// 3. CSS Selector — fast, flexible, widely used
driver.findElement(By.cssSelector("#loginBtn"));
driver.findElement(By.cssSelector(".error-message"));
driver.findElement(By.cssSelector("input[type='submit']"));
driver.findElement(By.cssSelector("[data-testid='submit-btn']"));

// 4. XPath — most powerful, handles complex DOM traversal
driver.findElement(By.xpath("//button[@id='loginBtn']"));
driver.findElement(By.xpath("//label[text()='Username']/following-sibling::input"));

// 5. Class Name — only single class (no compound)
driver.findElement(By.className("error-msg"));

// 6. Tag Name — returns multiple elements usually
driver.findElements(By.tagName("input"));

// 7. Link Text — exact match on <a> text
driver.findElement(By.linkText("Forgot Password?"));

// 8. Partial Link Text — partial match on <a> text
driver.findElement(By.partialLinkText("Forgot"));

// Priority order (best → worst):
// ID > Name > CSS Selector > XPath > Class Name > Tag Name > Link Text
```

**Rule of thumb:** Prefer `data-testid` or `data-qa` attributes via CSS selector. They are stable, fast, and semantically clear.

---

## Q7. What is the difference between findElement and findElements?

```java
// findElement — returns ONE WebElement, throws NoSuchElementException if not found
WebElement username = driver.findElement(By.id("username"));

// findElements — returns List<WebElement>, returns EMPTY LIST if not found (no exception)
List<WebElement> errors = driver.findElements(By.className("error"));
System.out.println("Error count: " + errors.size());  // 0 if none

// Common pattern — check if element exists
boolean isPresent = !driver.findElements(By.id("closeModal")).isEmpty();

// Safe click
List<WebElement> modals = driver.findElements(By.id("closeModal"));
if (!modals.isEmpty()) {
    modals.get(0).click();
}

// findElements for counting
int checkboxCount = driver.findElements(By.cssSelector("input[type='checkbox']")).size();

// findElements with stream
driver.findElements(By.tagName("a"))
    .stream()
    .map(WebElement::getText)
    .filter(text -> !text.isEmpty())
    .forEach(System.out::println);
```

---

## Q8. How do you write a robust XPath?

```java
// 1. By attribute
//input[@id='username']
//button[@type='submit']
//div[@data-testid='login-form']

// 2. By text content
//button[text()='Login']
//h1[contains(text(),'Dashboard')]
//span[normalize-space()='Submit']

// 3. By partial attribute
//input[contains(@class,'error')]
//div[starts-with(@id,'row-')]
//a[ends-with(@href,'/login')]  // XPath 2.0+ only

// 4. Parent-child
//div[@class='form-group']/input
//table/tbody/tr[1]/td[2]

// 5. Following/preceding sibling
//label[text()='Username']/following-sibling::input
//input[@id='email']/preceding-sibling::label

// 6. Ancestor/descendant
//td[contains(text(),'John')]/ancestor::tr
//div[@class='container']//button[@type='submit']

// 7. Multiple conditions — and / or
//input[@type='text' and @required]
//button[@id='submit' or @id='login']

// 8. Index (use sparingly — fragile)
(//input[@type='text'])[2]   // second text input

// Java
WebElement el = driver.findElement(By.xpath("//label[text()='Email']/following-sibling::input"));
```

---

## Q9. What is the difference between XPath and CSS Selector? When to use each?

| Feature | XPath | CSS Selector |
|---------|-------|-------------|
| Speed | Slightly slower | Faster |
| Direction | Can go UP (parent/ancestor) | Only downward in DOM |
| Text matching | Yes (`text()`, `contains(text())`) | No |
| Browser support | All | All |
| Readability | Verbose | Concise |
| Standard | XML standard | CSS standard |

```java
// CSS Selector — prefer for most cases
By.cssSelector("#loginBtn")                    // by ID
By.cssSelector(".error-message")              // by class
By.cssSelector("input[type='password']")      // by attribute
By.cssSelector("div.form-group > input")      // direct child
By.cssSelector("div.form-group input")        // any descendant
By.cssSelector("[data-testid='submit']")      // data attribute (best practice)
By.cssSelector("input:nth-child(2)")          // pseudo-selector

// XPath — use when CSS can't do it
// Text content match
By.xpath("//button[text()='Submit']")
// Traverse upward (parent/ancestor)
By.xpath("//td[text()='Active']/ancestor::tr/td[1]")
// Following sibling
By.xpath("//label[text()='Email']/following-sibling::input")
```

---

## Q10. What is absolute vs relative XPath?

```java
// Absolute XPath — starts from root, every node listed, FRAGILE
/html/body/div[1]/form/div[2]/input[1]
// One DOM change and it breaks

// Relative XPath — starts from //, searches entire DOM, ROBUST
//input[@id='username']
//div[@class='login-form']//input[@type='password']

// Rule: NEVER use absolute XPath in production automation code
// Always use relative XPath starting with //

WebElement username = driver.findElement(By.xpath("//input[@id='username']"));
```

---

## Q11. How do you handle dynamic elements with no stable ID?

```java
// Dynamic ID — changes on every page load
// <input id="user_1234567_field"> — BAD locator

// Strategies:

// 1. Use data-testid or stable attribute
driver.findElement(By.cssSelector("[data-testid='username-input']"));

// 2. Use text content
driver.findElement(By.xpath("//button[contains(text(),'Add to Cart')]"));

// 3. Use parent relationship
driver.findElement(By.xpath("//label[text()='Email']/following-sibling::input"));

// 4. Use contains() on partial stable part of ID
driver.findElement(By.xpath("//input[contains(@id,'username')]"));

// 5. Combine multiple stable attributes
driver.findElement(By.cssSelector("form.login-form input[type='email']"));

// 6. Use name attribute if stable
driver.findElement(By.name("email"));

// 7. Index as LAST resort
driver.findElement(By.cssSelector("form input:nth-of-type(1)"));
```

---

## Q12. What is implicit wait?

Implicit wait tells WebDriver to wait a maximum amount of time when trying to find an element before throwing NoSuchElementException.

```java
// Set once — applies to ALL findElement calls globally
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// After this:
// driver.findElement(By.id("result"))
// → polls the DOM every 500ms for up to 10 seconds
// → returns element as soon as found
// → throws NoSuchElementException after 10s if still not found

// Common setup in BaseTest
@BeforeMethod
public void setUp() {
    WebDriverManager.chromedriver().setup();
    driver = new ChromeDriver();
    driver.manage().window().maximize();
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));  // global wait
}

// Remove implicit wait
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
```

**Limitation:** Implicit wait applies to ALL elements globally. You cannot specify different waits for different elements easily.

---

## Q13. What is explicit wait?

Explicit wait waits for a specific condition on a specific element for a specified duration.

```java
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

// Wait for element to be visible
WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));

// Wait for element to be clickable
WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
btn.click();

// Wait for text
wait.until(ExpectedConditions.textToBe(By.id("status"), "Success"));

// Wait for URL to contain
wait.until(ExpectedConditions.urlContains("/dashboard"));

// Wait for title
wait.until(ExpectedConditions.titleContains("Dashboard"));

// Custom condition using lambda
wait.until(driver -> driver.findElement(By.id("count")).getText().equals("10"));

// Custom timeout for a specific element (overrides global)
WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("heavyReport")));
```

---

## Q14. What is fluent wait?

Fluent wait is the most configurable wait. You can set: max duration, polling interval, exceptions to ignore, and a custom message.

```java
import org.openqa.selenium.support.ui.FluentWait;
import java.time.Duration;

FluentWait<WebDriver> wait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(30))        // max wait time
    .pollingEvery(Duration.ofMillis(500))       // check every 500ms
    .ignoring(NoSuchElementException.class)     // don't fail on these
    .ignoring(StaleElementReferenceException.class)
    .withMessage("Element not found after 30 seconds");

WebElement el = wait.until(driver -> driver.findElement(By.id("dynamicResult")));

// WebDriverWait is actually a subclass of FluentWait with sensible defaults
// WebDriverWait polls every 500ms and ignores NoSuchElementException by default
```

---

## Q15. Why should you NEVER use Thread.sleep()?

```java
// Thread.sleep(3000) — WRONG for these reasons:

// 1. Hardcoded — waits 3s even if element appears in 200ms (wastes time)
// 2. Brittle — fails on slow CI servers where element needs 4s
// 3. Cascades — 10 steps × 2s = 20 extra seconds per test
// 4. Masks real issues — slow app should fail fast, not silently pass

// The only LEGITIMATE use of Thread.sleep:
// - Waiting for a file to be written to disk (non-DOM)
// - Waiting for an animation that has no DOM hook
// - Quick debugging (NEVER commit to main)

// ALWAYS replace with explicit wait
// WRONG
Thread.sleep(3000);
driver.findElement(By.id("result")).getText();

// CORRECT
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
String text = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("result"))
).getText();
```

---

## Q16. Why should you not mix implicit and explicit waits?

```java
// Mixing causes UNPREDICTABLE wait times

// Scenario: implicit = 10s, explicit = 5s
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

// When wait.until(visibilityOf...) is called:
// → WebDriverWait internally calls findElement repeatedly
// → Each findElement call ALSO applies the 10s implicit wait
// → Total wait = up to 10s (implicit) × number of polls = much more than 5s
// → Explicit timeout is effectively ignored

// Rule: Use ONE wait strategy in your framework
// Option A: Implicit wait ONLY (simple projects)
// Option B: Explicit wait ONLY (recommended for production frameworks)
//           Set implicitWait to 0 explicitly if using explicit waits

driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // disable implicit
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // use explicit only
```

---

## Q17. What are all ExpectedConditions? (Key ones with code)

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Visibility
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("msg")));
wait.until(ExpectedConditions.visibilityOf(element));
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loader")));
wait.until(ExpectedConditions.invisibilityOf(element));

// Clickability / Presence
wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
wait.until(ExpectedConditions.presenceOfElementLocated(By.id("hidden")));  // in DOM, not visible
wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("tr")));

// Text
wait.until(ExpectedConditions.textToBe(By.id("status"), "Done"));
wait.until(ExpectedConditions.textToBePresentInElement(element, "partial text"));
wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("x"), "text"));
wait.until(ExpectedConditions.textToBePresentInElementValue(By.id("input"), "value"));

// URL / Title
wait.until(ExpectedConditions.urlContains("/dashboard"));
wait.until(ExpectedConditions.urlToBe("https://example.com/home"));
wait.until(ExpectedConditions.titleContains("Dashboard"));
wait.until(ExpectedConditions.titleIs("My App - Dashboard"));

// Alert
wait.until(ExpectedConditions.alertIsPresent());

// Selection
wait.until(ExpectedConditions.elementToBeSelected(By.id("checkbox")));
wait.until(ExpectedConditions.elementSelectionStateToBe(By.id("cb"), true));

// Frame
wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("myFrame")));

// Number of elements
wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.tagName("tr"), 5));
wait.until(ExpectedConditions.numberOfElementsToBe(By.className("row"), 10));

// Attribute
wait.until(ExpectedConditions.attributeContains(By.id("btn"), "class", "active"));
wait.until(ExpectedConditions.attributeToBe(By.id("input"), "disabled", "true"));

// Staleness
wait.until(ExpectedConditions.stalenessOf(oldElement));

// Custom lambda
wait.until(d -> d.findElement(By.id("count")).getText().equals("100"));
```

---

## Q18. What is Page Object Model?

POM is a design pattern where each web page (or component) is represented by a Java class. The class contains locators and interaction methods. Tests use page objects rather than interacting with the driver directly.

```
src/
  test/
    java/
      pages/
        BasePage.java
        LoginPage.java
        DashboardPage.java
        CheckoutPage.java
      tests/
        LoginTest.java
        CheckoutTest.java
      utils/
        DriverManager.java
        ConfigReader.java
```

```java
// LoginPage.java
public class LoginPage extends BasePage {
    private By usernameField = By.id("username");
    private By passwordField = By.id("password");
    private By loginButton   = By.cssSelector("button[type='submit']");
    private By errorMessage  = By.className("alert-error");

    public LoginPage(WebDriver driver) { super(driver); }

    public LoginPage enterUsername(String user) {
        waitForVisibility(usernameField).sendKeys(user);
        return this;
    }

    public LoginPage enterPassword(String pass) {
        waitForVisibility(passwordField).sendKeys(pass);
        return this;
    }

    public DashboardPage clickLogin() {
        click(loginButton);
        return new DashboardPage(driver);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }
}

// LoginTest.java
public class LoginTest extends BaseTest {
    @Test
    public void validLogin() {
        new LoginPage(driver)
            .enterUsername("admin")
            .enterPassword("Admin@123")
            .clickLogin()
            .verifyWelcomeMessage("Welcome, Admin");
    }
}
```

**Benefits:** Maintainability (one place to fix locators), reusability, readability.

---

## Q19. What is PageFactory and @FindBy?

PageFactory initialises `@FindBy` annotated WebElement fields using lazy initialisation — the element is looked up only when first accessed.

```java
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {
    private WebDriver driver;

    @FindBy(id = "username")
    private WebElement usernameField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    @FindBy(className = "error-msg")
    private WebElement errorMsg;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);  // initialises all @FindBy fields
    }

    public void login(String user, String pass) {
        usernameField.sendKeys(user);
        passwordField.sendKeys(pass);
        loginButton.click();
    }
}
```

**Downside of PageFactory:** `@FindBy` elements can become stale in dynamic SPAs. If the DOM is rebuilt, a `StaleElementReferenceException` is thrown. Manual `By` locators with `findElement` inside methods are more robust for such cases.

---

## Q20. What is @FindBys and @FindAll?

```java
// @FindBys — AND logic — element must match ALL criteria (chained search)
// Finds element that is BOTH inside .panel AND has type='submit'
@FindBys({
    @FindBy(className = "panel"),
    @FindBy(css = "button[type='submit']")
})
private WebElement submitInsidePanel;

// @FindAll — OR logic — elements matching ANY of the criteria
// Finds elements matching .error OR .warning
@FindAll({
    @FindBy(className = "error"),
    @FindBy(className = "warning")
})
private List<WebElement> allAlerts;

// Note:
// @FindBys — returns first element matching the chained (parent→child) path
// @FindAll — returns all elements matching any of the selectors (union)
```

---

## Q21. What is the Actions class? What can it do?

The Actions class provides a way to perform complex user gestures — mouse movements, keyboard actions, drag and drop.

```java
import org.openqa.selenium.interactions.Actions;

Actions actions = new Actions(driver);

// Mouse hover
WebElement menu = driver.findElement(By.id("navMenu"));
actions.moveToElement(menu).perform();

// Click at specific offset within an element
actions.moveToElement(element, 10, 20).click().perform();

// Double click
actions.doubleClick(element).perform();

// Right click (context menu)
actions.contextClick(element).perform();

// Click and hold
actions.clickAndHold(element).perform();

// Drag and drop
WebElement source = driver.findElement(By.id("source"));
WebElement target = driver.findElement(By.id("target"));
actions.dragAndDrop(source, target).perform();
// Alternative
actions.clickAndHold(source).moveToElement(target).release().perform();

// Keyboard
actions.sendKeys(Keys.CONTROL, "a").perform();  // Ctrl+A (select all)
actions.sendKeys(Keys.CONTROL, "c").perform();  // Ctrl+C (copy)
actions.sendKeys(element, "text").perform();

// Chain multiple actions
actions.moveToElement(menu)
       .pause(Duration.ofMillis(500))
       .click(submenuItem)
       .perform();
```

---

## Q22. How do you perform double click, right click, hover?

```java
Actions actions = new Actions(driver);

// Double click
WebElement item = driver.findElement(By.id("editableItem"));
actions.doubleClick(item).perform();

// Right click / context menu
WebElement cell = driver.findElement(By.cssSelector("td.data-cell"));
actions.contextClick(cell).perform();
// Then click menu item that appears
driver.findElement(By.xpath("//li[text()='Edit']")).click();

// Hover (mouse-over)
WebElement navItem = driver.findElement(By.id("productsMenu"));
actions.moveToElement(navItem).perform();
// Wait for dropdown to appear
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
WebElement dropdown = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("productsDropdown"))
);
dropdown.findElement(By.linkText("Laptops")).click();
```

---

## Q23. How do you drag and drop?

```java
Actions actions = new Actions(driver);

WebElement source = driver.findElement(By.id("draggable"));
WebElement target = driver.findElement(By.id("droppable"));

// Method 1 — dragAndDrop
actions.dragAndDrop(source, target).perform();

// Method 2 — clickAndHold + moveToElement + release
actions.clickAndHold(source)
       .moveToElement(target)
       .release(target)
       .perform();

// Method 3 — moveByOffset (when target coordinates known)
actions.clickAndHold(source)
       .moveByOffset(200, 0)   // move 200px right
       .release()
       .perform();

// JavaScript fallback (when Actions doesn't work due to HTML5 drag events)
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript(
    "function simulateDragDrop(sourceNode, targetNode) {" +
    "  var EVENT_TYPES = { ... }" +  // use a proper simulate library
    "}", source, target);
```

---

## Q24. How do you handle keyboard shortcuts?

```java
import org.openqa.selenium.Keys;

WebElement input = driver.findElement(By.id("searchBox"));

// sendKeys with Keys
input.sendKeys(Keys.ENTER);
input.sendKeys(Keys.TAB);
input.sendKeys(Keys.ESCAPE);
input.sendKeys(Keys.BACK_SPACE);
input.sendKeys(Keys.DELETE);

// Key combinations
input.sendKeys(Keys.chord(Keys.CONTROL, "a"));  // Ctrl+A
input.sendKeys(Keys.chord(Keys.CONTROL, "c"));  // Ctrl+C
input.sendKeys(Keys.chord(Keys.CONTROL, "v"));  // Ctrl+V
input.sendKeys(Keys.chord(Keys.CONTROL, "z"));  // Ctrl+Z
input.sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB)); // Shift+Tab

// Using Actions for keyboard shortcuts
Actions actions = new Actions(driver);
actions.keyDown(Keys.CONTROL)
       .sendKeys("a")
       .keyUp(Keys.CONTROL)
       .perform();

// Function keys
actions.sendKeys(Keys.F5).perform();   // refresh
actions.sendKeys(Keys.F12).perform();  // dev tools

// Clear field — Ctrl+A then Delete
actions.click(input)
       .keyDown(Keys.CONTROL)
       .sendKeys("a")
       .keyUp(Keys.CONTROL)
       .sendKeys(Keys.DELETE)
       .perform();
```

---

## Q25. What is JavascriptExecutor? When do you use it?

JavascriptExecutor allows executing JavaScript code in the context of the browser.

```java
JavascriptExecutor js = (JavascriptExecutor) driver;

// Use when:
// 1. Element not clickable via normal click (overlapping element, not visible)
js.executeScript("arguments[0].click();", element);

// 2. Scrolling
js.executeScript("window.scrollTo(0, document.body.scrollHeight)");  // bottom
js.executeScript("window.scrollTo(0, 0)");                           // top
js.executeScript("arguments[0].scrollIntoView(true);", element);     // to element

// 3. Setting input values (hidden inputs, read-only fields)
js.executeScript("arguments[0].value='test@email.com';", emailInput);

// 4. Getting values
String text = (String) js.executeScript("return arguments[0].textContent;", element);
Long count  = (Long)   js.executeScript("return document.querySelectorAll('tr').length;");

// 5. Highlighting element for debugging
js.executeScript("arguments[0].style.border='3px solid red';", element);

// 6. Removing attribute (e.g., readonly)
js.executeScript("arguments[0].removeAttribute('readonly');", dateInput);

// 7. Wait for jQuery AJAX
Boolean ready = (Boolean) js.executeScript("return jQuery.active == 0");

// 8. Modifying localStorage
js.executeScript("localStorage.setItem('token','abc123')");
String token = (String) js.executeScript("return localStorage.getItem('token')");
```

---

## Q26. How do you scroll to an element?

```java
WebElement target = driver.findElement(By.id("footer-section"));

// Method 1 — JavaScript scrollIntoView
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].scrollIntoView(true);", target);

// Method 2 — scrollIntoView with smooth behavior
js.executeScript("arguments[0].scrollIntoView({behavior:'smooth', block:'center'});", target);

// Method 3 — Actions moveToElement (also scrolls into view)
new Actions(driver).moveToElement(target).perform();

// Method 4 — scroll by pixels
js.executeScript("window.scrollBy(0, 500)");  // scroll 500px down

// Wait after scroll (element may need a moment to become interactable)
js.executeScript("arguments[0].scrollIntoView(true);", target);
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.elementToBeClickable(target));
target.click();
```

---

## Q27. How do you scroll to the bottom of the page?

```java
JavascriptExecutor js = (JavascriptExecutor) driver;

// Scroll to bottom
js.executeScript("window.scrollTo(0, document.body.scrollHeight)");

// Scroll to top
js.executeScript("window.scrollTo(0, 0)");

// Scroll within a scrollable div
WebElement scrollableDiv = driver.findElement(By.id("scrollPanel"));
js.executeScript("arguments[0].scrollTop = arguments[0].scrollHeight", scrollableDiv);

// Infinite scroll — load all items
long lastHeight = (Long) js.executeScript("return document.body.scrollHeight");
while (true) {
    js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
    Thread.sleep(1500);  // wait for new content to load
    long newHeight = (Long) js.executeScript("return document.body.scrollHeight");
    if (newHeight == lastHeight) break;  // no more content
    lastHeight = newHeight;
}
```

---

## Q28. How do you handle iframes?

```java
// You MUST switch to the iframe before interacting with its content

// Switch by index
driver.switchTo().frame(0);  // first iframe on page

// Switch by name or ID
driver.switchTo().frame("loginFrame");
driver.switchTo().frame("iframeId");

// Switch by WebElement reference
WebElement iframe = driver.findElement(By.cssSelector("iframe.content-frame"));
driver.switchTo().frame(iframe);

// Now interact with content inside the iframe
driver.findElement(By.id("username")).sendKeys("admin");

// Switch BACK to main document
driver.switchTo().defaultContent();

// Switch to parent frame (one level up)
driver.switchTo().parentFrame();

// Wait for iframe to be available and switch
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("myFrame")));
driver.findElement(By.id("submitBtn")).click();
driver.switchTo().defaultContent();
```

---

## Q29. How do you handle nested iframes?

```java
// Outer iframe → Inner iframe → interact → back to default

// Switch to outer iframe
driver.switchTo().frame("outerFrame");

// Switch to inner iframe (nested inside outer)
driver.switchTo().frame("innerFrame");

// Interact with element inside inner iframe
driver.findElement(By.id("targetField")).sendKeys("value");

// Go back one level — to outer frame
driver.switchTo().parentFrame();

// Go back to main document
driver.switchTo().defaultContent();

// Best practice — always return to defaultContent after iframe interaction
try {
    driver.switchTo().frame("outerFrame");
    driver.switchTo().frame("innerFrame");
    driver.findElement(By.id("field")).sendKeys("data");
} finally {
    driver.switchTo().defaultContent();  // guaranteed cleanup
}
```

---

## Q30. How do you handle alerts, confirms, prompts?

```java
// Wait for alert to appear
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
Alert alert = wait.until(ExpectedConditions.alertIsPresent());

// Simple alert — only OK button
// driver.findElement(By.id("trigger")).click();
String alertText = alert.getText();  // get alert message
alert.accept();                      // click OK

// Confirm dialog — OK or Cancel
Alert confirm = wait.until(ExpectedConditions.alertIsPresent());
System.out.println(confirm.getText());
confirm.accept();   // click OK
// confirm.dismiss();  // click Cancel

// Prompt dialog — input field + OK/Cancel
Alert prompt = wait.until(ExpectedConditions.alertIsPresent());
prompt.sendKeys("My input text");    // type into prompt
prompt.accept();                     // submit
// prompt.dismiss();                 // cancel (ignores input)

// Switch to alert (without waiting — use wait in production)
Alert alert2 = driver.switchTo().alert();
alert2.accept();
```

---

## Q31. How do you handle multiple windows and tabs?

```java
// Get the current window handle
String mainWindow = driver.getWindowHandle();

// Get all open window handles
Set<String> allWindows = driver.getWindowHandles();

// Switch to new window/tab
for (String handle : allWindows) {
    if (!handle.equals(mainWindow)) {
        driver.switchTo().window(handle);
        break;
    }
}

// Now on new window — interact
System.out.println("New window title: " + driver.getTitle());
driver.findElement(By.id("closeBtn")).click();

// Switch back to main window
driver.switchTo().window(mainWindow);

// Helper method — switch to latest opened window
public void switchToLatestWindow() {
    String latestHandle = new ArrayList<>(driver.getWindowHandles())
        .get(driver.getWindowHandles().size() - 1);
    driver.switchTo().window(latestHandle);
}

// Helper method — switch by title
public void switchToWindowByTitle(String title) {
    for (String handle : driver.getWindowHandles()) {
        driver.switchTo().window(handle);
        if (driver.getTitle().equals(title)) return;
    }
    throw new RuntimeException("Window with title '" + title + "' not found");
}
```

---

## Q32. How do you open a new tab in Selenium 4?

```java
// Selenium 4 — newWindow API
// Open new tab and switch to it automatically
driver.switchTo().newWindow(WindowType.TAB);
driver.get("https://google.com");

// Open new browser window
driver.switchTo().newWindow(WindowType.WINDOW);
driver.get("https://example.com");

// Switch back to original tab
String originalTab = driver.getWindowHandles().iterator().next();
driver.switchTo().window(originalTab);

// Pre-Selenium 4 — JavaScript approach
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("window.open('https://google.com','_blank')");
// Then switch using getWindowHandles()
```

---

## Q33. How do you handle dropdown (HTML select)?

```java
import org.openqa.selenium.support.ui.Select;

WebElement dropdownEl = driver.findElement(By.id("country"));
Select dropdown = new Select(dropdownEl);

// Select by visible text
dropdown.selectByVisibleText("Sri Lanka");

// Select by value attribute
dropdown.selectByValue("LK");

// Select by index (0-based)
dropdown.selectByIndex(2);

// Get selected option
WebElement selected = dropdown.getFirstSelectedOption();
System.out.println("Selected: " + selected.getText());

// Get all options
List<WebElement> options = dropdown.getOptions();
options.forEach(opt -> System.out.println(opt.getText()));

// Check if multiple selection is supported
boolean isMultiple = dropdown.isMultiple();

// Multi-select
if (isMultiple) {
    dropdown.selectByVisibleText("Option A");
    dropdown.selectByVisibleText("Option B");
    dropdown.deselectByVisibleText("Option A");
    dropdown.deselectAll();
}
```

---

## Q34. How do you handle custom dropdowns (not `<select>`)?

```java
// Custom dropdown — typically: <div class="dropdown"> with <ul><li> options
// Cannot use Select class — use regular WebElement interactions

// Step 1: Click the dropdown trigger to open it
WebElement dropdownTrigger = driver.findElement(By.cssSelector(".dropdown-trigger"));
dropdownTrigger.click();

// Step 2: Wait for options to appear
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".dropdown-menu")));

// Step 3: Click the desired option
driver.findElement(By.xpath("//ul[@class='dropdown-menu']//li[text()='Sri Lanka']")).click();

// Reusable method
public void selectFromCustomDropdown(By trigger, By optionLocator, String optionText) {
    driver.findElement(trigger).click();
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    By option = By.xpath(String.format(
        "//%s[normalize-space(text())='%s']",
        "li", optionText));
    wait.until(ExpectedConditions.visibilityOfElementLocated(option)).click();
}
```

---

## Q35. How do you handle checkboxes and radio buttons?

```java
// Checkbox
WebElement checkbox = driver.findElement(By.id("rememberMe"));

// Check it (only if not already checked)
if (!checkbox.isSelected()) {
    checkbox.click();
}

// Uncheck it
if (checkbox.isSelected()) {
    checkbox.click();
}

// Verify state
Assert.assertTrue(checkbox.isSelected(), "Checkbox should be checked");
Assert.assertFalse(checkbox.isSelected(), "Checkbox should be unchecked");

// Radio button
WebElement radioMonthly = driver.findElement(By.id("monthly"));
radioMonthly.click();
Assert.assertTrue(radioMonthly.isSelected());

// Select radio by value
List<WebElement> radios = driver.findElements(By.name("subscription"));
for (WebElement radio : radios) {
    if (radio.getAttribute("value").equals("annual")) {
        radio.click();
        break;
    }
}

// Select all checkboxes on page
driver.findElements(By.cssSelector("input[type='checkbox']"))
    .stream()
    .filter(cb -> !cb.isSelected())
    .forEach(WebElement::click);
```

---

## Q36. How do you upload a file in Selenium?

```java
// Standard file upload — <input type="file">
WebElement fileInput = driver.findElement(By.cssSelector("input[type='file']"));
// sendKeys with absolute file path — no need to click, just send the path
fileInput.sendKeys("C:\\Users\\Kupeshanth\\Desktop\\test-file.pdf");

// Wait for upload confirmation
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.textToBePresentInElementLocated(
    By.id("uploadStatus"), "Upload successful"));

// Hidden file input — make it visible first via JavaScript
WebElement hiddenInput = driver.findElement(By.id("hiddenFileUpload"));
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].style.display='block';", hiddenInput);
hiddenInput.sendKeys("/path/to/file.txt");

// Multiple files
fileInput.sendKeys("C:\\file1.pdf" + "\n" + "C:\\file2.pdf");

// Using Robot class for native OS dialogs (not recommended, brittle)
// Robot robot = new Robot();
// Prefer sendKeys approach above
```

---

## Q37. How do you take a screenshot?

```java
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.apache.commons.io.FileUtils;

// Full page screenshot
TakesScreenshot ts = (TakesScreenshot) driver;
File src  = ts.getScreenshotAs(OutputType.FILE);
File dest = new File("screenshots/test-" + System.currentTimeMillis() + ".png");
FileUtils.copyFile(src, dest);

// Screenshot as Base64 (for embedding in reports)
String base64 = ts.getScreenshotAs(OutputType.BASE64);

// Screenshot as byte array
byte[] bytes = ts.getScreenshotAs(OutputType.BYTES);

// TestNG — screenshot on failure using ITestListener
public class ScreenshotListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = DriverManager.getDriver();
        if (driver instanceof TakesScreenshot) {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                FileUtils.copyFile(src, new File(
                    "screenshots/" + result.getName() + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
```

---

## Q38. How do you take a screenshot of a specific element (Selenium 4)?

```java
// Selenium 4 — element-level screenshot
WebElement card = driver.findElement(By.cssSelector(".product-card"));
File elementScreenshot = card.getScreenshotAs(OutputType.FILE);
FileUtils.copyFile(elementScreenshot, new File("screenshots/product-card.png"));

// Base64 for Allure or ExtentReports
String base64 = card.getScreenshotAs(OutputType.BASE64);

// Pre-Selenium 4 workaround — crop from full page screenshot
TakesScreenshot ts = (TakesScreenshot) driver;
BufferedImage fullPage = ImageIO.read(ts.getScreenshotAs(OutputType.FILE));
Rectangle rect = card.getRect();
BufferedImage cropped = fullPage.getSubimage(
    rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
ImageIO.write(cropped, "png", new File("screenshots/element.png"));
```

---

## Q39. What is StaleElementReferenceException? Cause and fix?

StaleElementReferenceException is thrown when a WebElement reference is no longer attached to the DOM (the page was refreshed, reloaded, or dynamically updated).

```java
// Cause 1: Page refresh after finding element
WebElement btn = driver.findElement(By.id("submit"));
driver.navigate().refresh();    // DOM rebuilt
btn.click();                    // STALE — btn refers to old DOM

// Cause 2: AJAX update replaces the element
WebElement row = driver.findElement(By.cssSelector("tr.first-row"));
driver.findElement(By.id("filter")).click();  // AJAX updates table
row.getText();  // STALE — row is in old DOM

// Fix 1: Re-find element after action
driver.navigate().refresh();
driver.findElement(By.id("submit")).click();  // fresh lookup

// Fix 2: Retry wrapper
public WebElement findWithRetry(By locator, int retries) {
    for (int i = 0; i < retries; i++) {
        try {
            return driver.findElement(locator);
        } catch (StaleElementReferenceException e) {
            if (i == retries - 1) throw e;
        }
    }
    return null;
}

// Fix 3: FluentWait ignoring StaleElementReferenceException
FluentWait<WebDriver> wait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(10))
    .pollingEvery(Duration.ofMillis(500))
    .ignoring(StaleElementReferenceException.class);
wait.until(d -> d.findElement(By.id("result")).getText().equals("Done"));
```

---

## Q40. What is NoSuchElementException? Cause and fix?

```java
// Cause 1: Element not yet loaded (timing issue)
driver.get("https://example.com");
driver.findElement(By.id("dynamicContent")).getText();  // element loads after JS

// Cause 2: Wrong locator
driver.findElement(By.id("usernameField"));  // actual id is "username"

// Cause 3: Inside iframe — not switched to iframe
driver.findElement(By.id("fieldInsideIframe"));  // forgot to switch

// Cause 4: Element in wrong context (wrong frame, window)

// Fix 1: Add explicit wait
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("dynamicContent")));

// Fix 2: Verify locator in browser DevTools before using
// F12 → Console → document.querySelector('#elementId')

// Fix 3: Check if in iframe
driver.switchTo().frame("myFrame");
driver.findElement(By.id("fieldInsideIframe"));

// Fix 4: Safe existence check
boolean exists = !driver.findElements(By.id("elementId")).isEmpty();
```

---

## Q41. What is ElementNotInteractableException? Cause and fix?

```java
// Element is in DOM and visible but cannot be interacted with
// Causes:
// - Element is hidden (display:none; visibility:hidden; opacity:0)
// - Element is disabled
// - Element is covered by another element (overlay/modal)
// - Element not yet fully rendered

// Fix 1: Wait for element to become interactable
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.elementToBeClickable(By.id("submit"))).click();

// Fix 2: Scroll element into view
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].scrollIntoView(true);", element);
element.click();

// Fix 3: JavaScript click (bypasses visibility check)
js.executeScript("arguments[0].click();", element);

// Fix 4: Close overlay first
driver.findElement(By.id("cookieBanner")).click();  // dismiss overlay
driver.findElement(By.id("submit")).click();

// Fix 5: Wait for element to be enabled
wait.until(d -> d.findElement(By.id("submit")).isEnabled());
```

---

## Q42. What is ElementClickInterceptedException? Cause and fix?

```java
// Click was intercepted by another element (overlay, sticky header, cookie banner, modal)

// Fix 1: Close/dismiss the intercepting element
driver.findElement(By.id("cookieAccept")).click();
driver.findElement(By.id("targetBtn")).click();

// Fix 2: Scroll to bring element out from under sticky header
JavascriptExecutor js = (JavascriptExecutor) driver;
WebElement target = driver.findElement(By.id("targetBtn"));
js.executeScript("arguments[0].scrollIntoView({block:'center'});", target);
target.click();

// Fix 3: JavaScript click (bypasses the interception check)
js.executeScript("arguments[0].click();", target);

// Fix 4: Wait for intercepting element to disappear
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loadingOverlay")));
target.click();

// Fix 5: Actions class — moves to element then clicks
new Actions(driver).moveToElement(target).click().perform();
```

---

## Q43. What is SessionNotCreatedException?

Thrown when WebDriver cannot create a new browser session. Most common cause: **browser version and driver version mismatch**.

```java
// Common error message:
// SessionNotCreatedException: session not created: This version of ChromeDriver only
// supports Chrome version 114. Current browser version is 120.

// Fix 1: Use WebDriverManager (auto-matches versions)
WebDriverManager.chromedriver().setup();  // always correct version
WebDriver driver = new ChromeDriver();

// Fix 2: Specify browser version explicitly
WebDriverManager.chromedriver().browserVersion("120").setup();

// Fix 3: Update chromedriver manually to match Chrome version

// Other causes:
// - Chrome is not installed
// - Chrome binary is at non-standard path
// - Corporate firewall blocks WebDriverManager download
ChromeOptions options = new ChromeOptions();
options.setBinary("/usr/bin/google-chrome");  // specify binary path
WebDriver driver = new ChromeDriver(options);
```

---

## Q44. What is the difference between driver.close() and driver.quit()?

```java
// driver.close() — closes ONLY the current active browser window/tab
// If multiple windows/tabs are open, others remain open
// The WebDriver session remains active
driver.close();

// driver.quit() — closes ALL windows/tabs AND ends the WebDriver session
// Releases the port and kills the browser driver process
driver.quit();

// RULE: Always use driver.quit() in @AfterMethod
// Using driver.close() causes resource leaks (dangling chromedriver processes)

@AfterMethod
public void tearDown() {
    if (driver != null) {
        driver.quit();  // NOT close()
    }
}

// When to use driver.close():
// - You opened a popup/new tab and want to close only that tab
// - Then switch back to main window
driver.switchTo().window(popupHandle);
driver.close();  // close popup
driver.switchTo().window(mainHandle);  // back to main
```

---

## Q45. What is the difference between driver.get() and navigate().to()?

```java
// driver.get(url) — loads the URL and waits for page to fully load (document.readyState = 'complete')
driver.get("https://example.com");

// driver.navigate().to(url) — same as get(), also loads URL
driver.navigate().to("https://example.com");

// navigate() provides additional navigation methods
driver.navigate().back();     // browser back button
driver.navigate().forward();  // browser forward button
driver.navigate().refresh();  // F5 refresh

// Key difference: functionally identical for URL loading
// navigate().to() is part of the NavigationManager which also gives back/forward/refresh

// In practice — use either for navigation, use navigate().back() for history
driver.get("https://example.com/page1");
driver.navigate().to("https://example.com/page2");
driver.navigate().back();    // returns to page1
driver.navigate().forward(); // returns to page2
```

---

## Q46. How do you verify text on a page?

```java
// Method 1 — getText() with assertion
String actualTitle = driver.findElement(By.tagName("h1")).getText();
Assert.assertEquals(actualTitle, "Welcome to Dashboard", "Title mismatch");

// Method 2 — page source contains
Assert.assertTrue(driver.getPageSource().contains("Login successful"),
    "Success message not found");

// Method 3 — explicit wait for text
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.textToBe(By.id("status"), "Payment Confirmed"));

// Method 4 — soft assertion (TestNG) — collects all failures
SoftAssert softAssert = new SoftAssert();
softAssert.assertEquals(driver.getTitle(), "Dashboard", "Title wrong");
softAssert.assertTrue(driver.findElement(By.id("welcomeMsg")).isDisplayed());
softAssert.assertAll();  // reports all failures at once

// Method 5 — contains partial text
String actualText = driver.findElement(By.id("notification")).getText();
Assert.assertTrue(actualText.contains("successfully"), "Notification missing 'successfully'");
```

---

## Q47. How do you get text from an element?

```java
// getText() — returns visible text content
WebElement heading = driver.findElement(By.tagName("h1"));
String text = heading.getText();  // "Welcome to Dashboard"

// getText() returns empty string if element is hidden
// Use getAttribute("textContent") or getAttribute("innerText") for hidden elements

// getAttribute("textContent") — includes hidden text
String allText = element.getAttribute("textContent");

// getAttribute("innerText") — visible text only (same as getText() but JS-side)
String visibleText = element.getAttribute("innerText");

// Get text via JavaScript (useful for hidden or shadow DOM elements)
JavascriptExecutor js = (JavascriptExecutor) driver;
String jsText = (String) js.executeScript("return arguments[0].textContent;", element);

// getText() strips leading/trailing whitespace automatically
// If getText() returns empty, check if element is displayed
System.out.println(element.isDisplayed());  // must be true for getText() to work
```

---

## Q48. How do you get an attribute value?

```java
WebElement input = driver.findElement(By.id("email"));

// getAttribute — returns attribute value as String, null if not present
String placeholder = input.getAttribute("placeholder");  // "Enter email"
String type        = input.getAttribute("type");         // "email"
String value       = input.getAttribute("value");        // current input value
String disabled    = input.getAttribute("disabled");     // "true" or null
String href        = driver.findElement(By.id("link")).getAttribute("href");
String src         = driver.findElement(By.tagName("img")).getAttribute("src");
String dataTestId  = element.getAttribute("data-testid");

// getDomProperty (Selenium 4) — returns DOM property (not HTML attribute)
String checked = checkbox.getDomProperty("checked");  // "true" or "false"

// getDomAttribute (Selenium 4) — returns HTML attribute
String attrVal = element.getDomAttribute("class");

// Difference: getAttribute("checked") on a checkbox returns "true" if checked
// but getDomProperty("checked") returns "true"/"false" based on current state
```

---

## Q49. How do you get CSS value?

```java
// getCssValue — returns computed CSS value for a property
WebElement button = driver.findElement(By.id("submitBtn"));

String color       = button.getCssValue("color");           // "rgba(255, 255, 255, 1)"
String bgColor     = button.getCssValue("background-color");// "rgba(0, 128, 0, 1)"
String fontSize    = button.getCssValue("font-size");        // "16px"
String fontWeight  = button.getCssValue("font-weight");      // "700"
String display     = button.getCssValue("display");          // "flex"
String visibility  = button.getCssValue("visibility");       // "visible"
String borderColor = button.getCssValue("border-color");     // "rgba(0,0,0,1)"

// Verify a button is green (success state)
String bg = button.getCssValue("background-color");
Assert.assertEquals(bg, "rgba(40, 167, 69, 1)", "Button should be green");

// Convert rgba to hex for easier assertion
public static String rgbaToHex(String rgba) {
    String[] parts = rgba.replace("rgba(","").replace(")","").split(",");
    return String.format("#%02x%02x%02x",
        Integer.parseInt(parts[0].trim()),
        Integer.parseInt(parts[1].trim()),
        Integer.parseInt(parts[2].trim()));
}
```

---

## Q50. How do you count elements on a page?

```java
// findElements returns a list — use .size()
int checkboxCount = driver.findElements(By.cssSelector("input[type='checkbox']")).size();
System.out.println("Checkboxes: " + checkboxCount);

int rowCount = driver.findElements(By.cssSelector("table tbody tr")).size();
System.out.println("Table rows: " + rowCount);

// Assert count
Assert.assertEquals(rowCount, 10, "Expected 10 rows in the table");
Assert.assertTrue(rowCount > 0, "Table should have at least one row");

// Count via JavaScript
JavascriptExecutor js = (JavascriptExecutor) driver;
Long count = (Long) js.executeScript(
    "return document.querySelectorAll('input[type=checkbox]').length");

// ExpectedConditions-based count assertion
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector("tr.data-row"), 5));

// Stream count
long activeCount = driver.findElements(By.cssSelector(".user-row"))
    .stream()
    .filter(el -> el.getText().contains("Active"))
    .count();
```
