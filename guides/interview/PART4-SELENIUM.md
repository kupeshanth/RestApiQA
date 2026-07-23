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

---

## Q51. How do you handle tables?

```java
// Table structure: <table><thead><tr><th></th></tr></thead><tbody><tr><td></td></tr></tbody></table>

// Get all rows in tbody
List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));
System.out.println("Row count: " + rows.size());

// Get cell value — row 2, column 3
String cellValue = driver.findElement(
    By.cssSelector("table tbody tr:nth-child(2) td:nth-child(3)")).getText();

// Iterate all rows and columns
List<WebElement> allRows = driver.findElements(By.cssSelector("table tbody tr"));
for (int i = 0; i < allRows.size(); i++) {
    List<WebElement> cells = allRows.get(i).findElements(By.tagName("td"));
    for (int j = 0; j < cells.size(); j++) {
        System.out.printf("Row %d, Col %d: %s%n", i+1, j+1, cells.get(j).getText());
    }
}

// Find row by cell content — click Edit in the row containing "John"
List<WebElement> tableRows = driver.findElements(By.cssSelector("table tbody tr"));
for (WebElement row : tableRows) {
    if (row.getText().contains("John")) {
        row.findElement(By.linkText("Edit")).click();
        break;
    }
}

// XPath — find Edit button in same row as "John"
driver.findElement(By.xpath("//table//tr[td[text()='John']]//a[text()='Edit']")).click();

// Get all header column names
List<WebElement> headers = driver.findElements(By.cssSelector("table thead th"));
headers.forEach(h -> System.out.println(h.getText()));
```

---

## Q52. How do you verify sorting in a table?

```java
// Click sort header
driver.findElement(By.cssSelector("th.name-col")).click();

// Get column values after sort
List<String> afterSort = driver.findElements(By.cssSelector("table tbody tr td:nth-child(1)"))
    .stream().map(WebElement::getText).collect(Collectors.toList());

// Verify ascending sort
List<String> expected = new ArrayList<>(afterSort);
Collections.sort(expected);
Assert.assertEquals(afterSort, expected, "Table not sorted ascending");

// Verify descending sort
driver.findElement(By.cssSelector("th.name-col")).click();
List<String> descValues = driver.findElements(By.cssSelector("table tbody tr td:nth-child(1)"))
    .stream().map(WebElement::getText).collect(Collectors.toList());
List<String> expectedDesc = new ArrayList<>(descValues);
Collections.sort(expectedDesc, Collections.reverseOrder());
Assert.assertEquals(descValues, expectedDesc, "Table not sorted descending");

// Numeric sort
List<Double> prices = driver.findElements(By.cssSelector("td.price")).stream()
    .map(el -> Double.parseDouble(el.getText().replace("$", "")))
    .collect(Collectors.toList());
List<Double> sortedPrices = new ArrayList<>(prices);
Collections.sort(sortedPrices);
Assert.assertEquals(prices, sortedPrices, "Prices not sorted ascending");
```

---

## Q53. How do you handle shadow DOM in Selenium 4?

```java
// Shadow DOM elements are encapsulated — regular findElement cannot reach them

// Selenium 4 — getShadowRoot()
WebElement host = driver.findElement(By.cssSelector("my-custom-element"));
SearchContext shadowRoot = host.getShadowRoot();

// Find inside shadow root
WebElement shadowInput = shadowRoot.findElement(By.cssSelector("input.inner-field"));
shadowInput.sendKeys("text inside shadow DOM");

// Nested shadow DOM
SearchContext outer = driver.findElement(By.cssSelector("outer-el")).getShadowRoot();
SearchContext inner = outer.findElement(By.cssSelector("inner-el")).getShadowRoot();
inner.findElement(By.cssSelector(".target")).click();

// Pre-Selenium 4 — JavaScript
JavascriptExecutor js = (JavascriptExecutor) driver;
WebElement shadowEl = (WebElement) js.executeScript(
    "return arguments[0].shadowRoot.querySelector('input')", host);
shadowEl.sendKeys("value");
```

---

## Q54. What are Selenium 4 new features?

```java
// 1. Relative Locators
import static org.openqa.selenium.support.locators.RelativeLocator.with;

WebElement pwdField = driver.findElement(
    with(By.tagName("input")).below(By.id("usernameLabel")));
WebElement btn = driver.findElement(
    with(By.tagName("button")).toRightOf(By.id("cancelBtn")));

// 2. Chrome DevTools Protocol (CDP)
ChromeDriver chrome = (ChromeDriver) driver;
DevTools devTools = chrome.getDevTools();
devTools.createSession();
devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
devTools.send(Network.setBlockedURLs(Arrays.asList("*.ads.com/*")));

// 3. New Window API
driver.switchTo().newWindow(WindowType.TAB);
driver.switchTo().newWindow(WindowType.WINDOW);

// 4. Element-level Screenshot
File img = driver.findElement(By.id("chart")).getScreenshotAs(OutputType.FILE);

// 5. getRect() — combined location + size
Rectangle rect = element.getRect();
System.out.println("X=" + rect.getX() + " W=" + rect.getWidth());

// 6. W3C protocol fully adopted — no JSON Wire Protocol
// 7. Selenium Grid 4 — Docker/Kubernetes, observability UI
// 8. getShadowRoot() for shadow DOM
```

---

## Q55. What is Selenium Grid?

Selenium Grid runs tests across multiple browsers, OSes, and machines in parallel.

```
Hub (central server)
  ├── Node 1: Windows + Chrome
  ├── Node 2: Windows + Firefox
  └── Node 3: Linux + Chrome headless
```

```java
// Connect to Grid via RemoteWebDriver
ChromeOptions opts = new ChromeOptions();
opts.setPlatformName("LINUX");
WebDriver driver = new RemoteWebDriver(new URL("http://grid-host:4444"), opts);

// All Selenium commands work the same — Grid is transparent to test code
driver.get("https://example.com");
driver.findElement(By.id("username")).sendKeys("admin");
```

---

## Q56. How do you set up Selenium Grid (Grid 4)?

```bash
# Download selenium-server-4.x.jar

# Standalone mode (hub + node on one machine)
java -jar selenium-server-4.x.jar standalone

# Hub-Node mode
java -jar selenium-server-4.x.jar hub --port 4444
java -jar selenium-server-4.x.jar node --hub http://hub:4444 --port 5555

# Grid Console: http://localhost:4444/ui
```

```java
// Test connecting to Grid
ChromeOptions opts = new ChromeOptions();
WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444"), opts);
driver.get("https://example.com");
driver.quit();
```

---

## Q57. How do you run tests on BrowserStack?

```java
MutableCapabilities caps = new MutableCapabilities();
caps.setCapability("browserName", "Chrome");

HashMap<String, Object> bsOptions = new HashMap<>();
bsOptions.put("os", "Windows");
bsOptions.put("osVersion", "11");
bsOptions.put("projectName", "Qoria Automation");
bsOptions.put("buildName", "CI Build #42");
bsOptions.put("userName", System.getenv("BROWSERSTACK_USER"));
bsOptions.put("accessKey", System.getenv("BROWSERSTACK_KEY"));
caps.setCapability("bstack:options", bsOptions);

WebDriver driver = new RemoteWebDriver(
    new URL("https://hub-cloud.browserstack.com/wd/hub"), caps);

// Mark pass/fail
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript(
    "browserstack_executor: {\"action\":\"setSessionStatus\"," +
    "\"arguments\":{\"status\":\"passed\",\"reason\":\"All assertions passed\"}}");
```

---

## Q58. What is RemoteWebDriver?

RemoteWebDriver connects to a browser session running on a different machine (Grid, cloud service, Docker container).

```java
import org.openqa.selenium.remote.RemoteWebDriver;

ChromeOptions options = new ChromeOptions();
WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444"), options);

// Same API as local WebDriver
driver.get("https://example.com");
driver.findElement(By.id("username")).sendKeys("admin");

// For TakesScreenshot on remote driver — augment it
WebDriver augmented = new Augmenter().augment(driver);
File screenshot = ((TakesScreenshot) augmented).getScreenshotAs(OutputType.FILE);
```

---

## Q59. How do you run tests in headless mode?

```java
// Chrome headless
ChromeOptions opts = new ChromeOptions();
opts.addArguments("--headless=new");          // Chrome 112+ / Selenium 4
opts.addArguments("--no-sandbox");
opts.addArguments("--disable-dev-shm-usage");
opts.addArguments("--window-size=1920,1080");
WebDriver driver = new ChromeDriver(opts);

// Firefox headless
FirefoxOptions ffOpts = new FirefoxOptions();
ffOpts.addArguments("-headless");
WebDriver driver = new FirefoxDriver(ffOpts);

// From system property
boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
if (headless) opts.addArguments("--headless=new");

// Run: mvn test -Dheadless=true
```

---

## Q60. How do you run cross-browser tests?

```xml
<!-- testng-crossbrowser.xml -->
<suite name="Cross Browser" parallel="tests" thread-count="3">
  <test name="Chrome">
    <parameter name="browser" value="chrome"/>
    <classes><class name="com.tests.LoginTest"/></classes>
  </test>
  <test name="Firefox">
    <parameter name="browser" value="firefox"/>
    <classes><class name="com.tests.LoginTest"/></classes>
  </test>
  <test name="Edge">
    <parameter name="browser" value="edge"/>
    <classes><class name="com.tests.LoginTest"/></classes>
  </test>
</suite>
```

```java
@Parameters({"browser"})
@BeforeMethod
public void setUp(@Optional("chrome") String browser) {
    DriverManager.set(DriverFactory.createDriver(browser));
}
// Run: mvn test -DsuiteXmlFiles=testng-crossbrowser.xml
```

---

## Q61. What is ThreadLocal\<WebDriver\>?

ThreadLocal gives each thread its own isolated WebDriver. Without it, parallel tests share and overwrite each other's driver.

```java
public class DriverManager {
    private static final ThreadLocal<WebDriver> tl = new ThreadLocal<>();

    public static void setDriver(WebDriver d) { tl.set(d); }
    public static WebDriver getDriver()       { return tl.get(); }
    public static void removeDriver() {
        if (tl.get() != null) { tl.get().quit(); tl.remove(); }
    }
}

// Thread-1's @BeforeMethod → setDriver(Chrome-1)
// Thread-2's @BeforeMethod → setDriver(Firefox-2)
// Thread-1 calls getDriver() → gets Chrome-1 (not Firefox-2)
// Thread-2 calls getDriver() → gets Firefox-2 (not Chrome-1)
```

---

## Q62. What happens without ThreadLocal in parallel tests?

```java
// WRONG — static shared field
public static WebDriver driver;  // shared by all threads

// Thread-1: driver = new ChromeDriver()   → driver points to Chrome-1
// Thread-2: driver = new ChromeDriver()   → driver NOW points to Chrome-2 (overwrites!)
// Thread-1: driver.findElement(...)       → interacts with Chrome-2 by accident
// → Wrong page, wrong session, test results corrupt each other
// → Flaky failures, NPEs, incorrect assertions

// CORRECT: ThreadLocal — each thread reads/writes its own slot
private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
```

---

## Q63. What is the difference between parallel="tests", "classes", "methods"?

```xml
<!-- parallel="tests" — each <test> block is one thread; methods inside are sequential -->
<suite parallel="tests" thread-count="3">
  <test name="T1">...</test>  <!-- thread 1 -->
  <test name="T2">...</test>  <!-- thread 2 -->
</suite>

<!-- parallel="classes" — each class is one thread -->
<suite parallel="classes" thread-count="3">
  <test name="All">
    <classes>
      <class name="LoginTest"/>     <!-- thread 1 -->
      <class name="SearchTest"/>    <!-- thread 2 -->
    </classes>
  </test>
</suite>

<!-- parallel="methods" — every @Test method is its own thread (most parallel) -->
<suite parallel="methods" thread-count="5">
  <test name="All"><classes>...</classes></test>
</suite>
```

**Rule:** Always pair any parallel mode with `ThreadLocal<WebDriver>`.

---

## Q64. How do you take screenshots on failure in TestNG?

```java
public class FailureListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) return;
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            String path = "screenshots/" + result.getName() + "_"
                + new SimpleDateFormat("HHmmss").format(new Date()) + ".png";
            FileUtils.copyFile(src, new File(path));
        } catch (IOException e) { e.printStackTrace(); }
    }
}

// Register in testng.xml
// <listeners><listener class-name="com.listeners.FailureListener"/></listeners>

// Or in @AfterMethod
@AfterMethod
public void tearDown(ITestResult result) {
    if (result.getStatus() == ITestResult.FAILURE) {
        File src = ((TakesScreenshot) DriverManager.getDriver())
            .getScreenshotAs(OutputType.FILE);
        try { FileUtils.copyFile(src, new File("screenshots/" + result.getName() + ".png")); }
        catch (IOException e) { e.printStackTrace(); }
    }
    DriverManager.removeDriver();
}
```

---

## Q65. How do you handle dynamic wait for AJAX elements?

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

// Wait for spinner to disappear, then element to appear
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("spinner")));
WebElement data = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("resultsTable")));

// FluentWait — ignore transient exceptions
FluentWait<WebDriver> fluent = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(20))
    .pollingEvery(Duration.ofMillis(250))
    .ignoring(NoSuchElementException.class)
    .ignoring(StaleElementReferenceException.class);

WebElement el = fluent.until(d -> {
    WebElement e = d.findElement(By.id("result"));
    return (e.isDisplayed() && !e.getText().isEmpty()) ? e : null;
});

// Wait for row count to change
int before = driver.findElements(By.cssSelector("tr.data")).size();
driver.findElement(By.id("loadMoreBtn")).click();
wait.until(d -> d.findElements(By.cssSelector("tr.data")).size() > before);
```

---

## Q66. How do you wait for jQuery AJAX to complete?

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
JavascriptExecutor js = (JavascriptExecutor) driver;

wait.until(d -> {
    try {
        boolean jqDone  = (Boolean) js.executeScript("return jQuery.active == 0");
        boolean docDone = js.executeScript("return document.readyState").equals("complete");
        return jqDone && docDone;
    } catch (Exception e) {
        return true;  // jQuery not present — skip
    }
});
```

---

## Q67. How do you handle file download?

```java
// Configure Chrome download directory
String downloadPath = System.getProperty("user.dir") + "/downloads";
new File(downloadPath).mkdirs();

HashMap<String, Object> prefs = new HashMap<>();
prefs.put("download.default_directory", downloadPath);
prefs.put("download.prompt_for_download", false);
prefs.put("safebrowsing.enabled", false);

ChromeOptions opts = new ChromeOptions();
opts.setExperimentalOption("prefs", prefs);
WebDriver driver = new ChromeDriver(opts);

// Trigger download
driver.findElement(By.id("downloadBtn")).click();

// Poll until file appears
public boolean waitForFile(String fileName, int timeoutSec) throws InterruptedException {
    File dir = new File(downloadPath);
    long end = System.currentTimeMillis() + timeoutSec * 1000L;
    while (System.currentTimeMillis() < end) {
        if (Arrays.stream(dir.listFiles()).anyMatch(f -> f.getName().equals(fileName))) return true;
        Thread.sleep(500);
    }
    return false;
}
Assert.assertTrue(waitForFile("report.pdf", 15), "File not downloaded");
```

---

## Q68. How do you handle cookies?

```java
// Get all cookies
driver.manage().getCookies().forEach(c -> System.out.println(c.getName() + "=" + c.getValue()));

// Get specific cookie
Cookie session = driver.manage().getCookieNamed("sessionId");

// Add cookie (inject auth — skip login)
driver.get("https://example.com");  // must be on domain first
Cookie auth = new Cookie.Builder("authToken", "abc123")
    .domain("example.com").path("/").isHttpOnly(true).build();
driver.manage().addCookie(auth);
driver.navigate().refresh();

// Delete
driver.manage().deleteCookieNamed("trackingCookie");
driver.manage().deleteAllCookies();
```

---

## Q69. How do you set localStorage via Selenium?

```java
JavascriptExecutor js = (JavascriptExecutor) driver;

js.executeScript("localStorage.setItem('authToken','abc123')");
String token = (String) js.executeScript("return localStorage.getItem('authToken')");
js.executeScript("localStorage.removeItem('authToken')");
js.executeScript("localStorage.clear()");

// sessionStorage
js.executeScript("sessionStorage.setItem('flag','true')");

// Navigate to app with pre-set token (bypass login)
driver.get("https://example.com");
js.executeScript("localStorage.setItem('token','" + authToken + "')");
driver.navigate().to("https://example.com/dashboard");
```

---

## Q70. How do you handle frames that load dynamically?

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

// Wait for iframe to be available and switch in one step
wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("dynamicFrame")));
driver.findElement(By.id("submitBtn")).click();
driver.switchTo().defaultContent();

// If frame has no id — wait for element, then switch by element reference
wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe.report")));
WebElement frame = driver.findElement(By.cssSelector("iframe.report"));
driver.switchTo().frame(frame);
```

---

## Q71. What is soft assertion vs hard assertion in Selenium context?

```java
// Hard assertion — stops test immediately on first failure
Assert.assertEquals(driver.getTitle(), "Dashboard");  // fails here if wrong
Assert.assertTrue(element.isDisplayed());             // never reached if above fails

// Soft assertion — collects ALL failures, reports together at assertAll()
SoftAssert soft = new SoftAssert();
soft.assertEquals(driver.getTitle(), "Dashboard", "Title wrong");
soft.assertTrue(driver.findElement(By.id("welcome")).isDisplayed(), "Welcome hidden");
soft.assertEquals(driver.findElement(By.id("user")).getText(), "Admin", "User wrong");
soft.assertAll();  // throws AssertionError listing ALL failures

// Use soft assertions when verifying multiple independent fields on the same page
```

---

## Q72. How do you verify a page has fully loaded?

```java
// document.readyState = "complete"
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
wait.until(d -> ((JavascriptExecutor) d)
    .executeScript("return document.readyState").equals("complete"));

// Combined: readyState + jQuery + key element
public void waitForPageLoad(By keyElement) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    wait.until(d -> {
        JavascriptExecutor js = (JavascriptExecutor) d;
        boolean ready = js.executeScript("return document.readyState").equals("complete");
        boolean jq = true;
        try { jq = (Boolean) js.executeScript("return jQuery.active==0"); }
        catch (Exception ignored) {}
        return ready && jq;
    });
    wait.until(ExpectedConditions.visibilityOfElementLocated(keyElement));
}
```

---

## Q73. How do you handle broken images?

```java
JavascriptExecutor js = (JavascriptExecutor) driver;
List<WebElement> images = driver.findElements(By.tagName("img"));
List<String> broken = new ArrayList<>();

for (WebElement img : images) {
    boolean isBroken = (Boolean) js.executeScript(
        "return arguments[0].naturalWidth == 0", img);
    if (isBroken) broken.add(img.getAttribute("src"));
}

Assert.assertTrue(broken.isEmpty(), "Broken images: " + broken);
```

---

## Q74. How do you find all links and verify none are broken?

```java
List<WebElement> anchors = driver.findElements(By.tagName("a"));
List<String> brokenLinks = new ArrayList<>();

for (WebElement a : anchors) {
    String href = a.getAttribute("href");
    if (href == null || href.startsWith("#") || href.startsWith("javascript")) continue;
    try {
        HttpURLConnection conn = (HttpURLConnection) new URL(href).openConnection();
        conn.setRequestMethod("HEAD");
        conn.setConnectTimeout(3000);
        conn.connect();
        if (conn.getResponseCode() >= 400) brokenLinks.add(href + " [" + conn.getResponseCode() + "]");
        conn.disconnect();
    } catch (Exception e) {
        brokenLinks.add(href + " [Error: " + e.getMessage() + "]");
    }
}
Assert.assertTrue(brokenLinks.isEmpty(), "Broken links: " + brokenLinks);
```

---

## Q75. How do you handle calendars/date pickers?

```java
// Strategy 1: sendKeys directly into date field
WebElement dateInput = driver.findElement(By.id("departureDate"));
dateInput.clear();
dateInput.sendKeys("25/07/2026");
dateInput.sendKeys(Keys.TAB);

// Strategy 2: JavaScript value set (bypasses date picker UI)
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].value='2026-07-25'", dateInput);
js.executeScript("arguments[0].dispatchEvent(new Event('change'))", dateInput);

// Strategy 3: Navigate the calendar widget
driver.findElement(By.id("calendarTrigger")).click();
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".calendar")));

// Navigate months until correct month
while (!driver.findElement(By.cssSelector(".calendar-month")).getText().contains("July 2026")) {
    driver.findElement(By.cssSelector(".next-month")).click();
}
driver.findElement(By.xpath("//td[@data-date='25']")).click();
```

---

## Q76. How do you handle autocomplete fields?

```java
WebElement searchInput = driver.findElement(By.id("citySearch"));
searchInput.sendKeys("Col");

WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".suggestions")));

// Click specific suggestion
driver.findElement(By.xpath("//ul[@class='suggestions']//li[text()='Colombo']")).click();

// Verify selection
Assert.assertEquals(driver.findElement(By.id("citySearch")).getAttribute("value"), "Colombo");
```

---

## Q77. What is the Robot class? When to use it?

```java
import java.awt.Robot;
import java.awt.event.KeyEvent;

// Use ONLY for native OS dialogs that Selenium cannot handle
Robot robot = new Robot();

// Press Enter
robot.keyPress(KeyEvent.VK_ENTER);
robot.keyRelease(KeyEvent.VK_ENTER);

// Type a string character by character
for (char c : "test@email.com".toCharArray()) {
    robot.keyPress(KeyEvent.getExtendedKeyCodeForChar(c));
    robot.keyRelease(KeyEvent.getExtendedKeyCodeForChar(c));
}

// Drawbacks: OS-dependent, doesn't work headless, brittle screen coords
// Prefer sendKeys for file uploads; use Robot only for true native OS dialogs
```

---

## Q78. What is a flaky test? How do you handle it?

A flaky test passes and fails intermittently without code changes.

**Causes:** timing issues, shared test data, environment lag, StaleElementReferenceException, animation not finished.

```java
// Fix 1: Replace Thread.sleep with explicit waits
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));

// Fix 2: RetryAnalyzer
public class RetryAnalyzer implements IRetryAnalyzer {
    private int count = 0;
    private static final int MAX = 2;
    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && count < MAX) { count++; return true; }
        return false;
    }
}
@Test(retryAnalyzer = RetryAnalyzer.class)
public void flakyTest() { ... }

// Fix 3: FluentWait ignoring StaleElementReferenceException
new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(15))
    .pollingEvery(Duration.ofMillis(300))
    .ignoring(StaleElementReferenceException.class)
    .until(d -> d.findElement(By.id("el")).isDisplayed());

// Fix 4: Isolate test data — each test creates its own data
// Fix 5: Investigate root cause — never rely on retry alone
```

---

## Q79. How do you handle SSL certificate errors in Selenium?

```java
// Chrome
ChromeOptions opts = new ChromeOptions();
opts.setAcceptInsecureCerts(true);
WebDriver driver = new ChromeDriver(opts);

// Firefox
FirefoxOptions ffOpts = new FirefoxOptions();
ffOpts.setAcceptInsecureCerts(true);
WebDriver driver = new FirefoxDriver(ffOpts);

// Edge
EdgeOptions edgeOpts = new EdgeOptions();
edgeOpts.setAcceptInsecureCerts(true);
WebDriver driver = new EdgeDriver(edgeOpts);

// RemoteWebDriver
ChromeOptions remoteOpts = new ChromeOptions();
remoteOpts.setAcceptInsecureCerts(true);
WebDriver driver = new RemoteWebDriver(new URL("http://grid:4444"), remoteOpts);
```

---

## Q80. How do you capture browser console logs?

```java
// Chrome — enable log preferences
ChromeOptions opts = new ChromeOptions();
LoggingPreferences prefs = new LoggingPreferences();
prefs.enable(LogType.BROWSER, Level.ALL);
opts.setCapability("goog:loggingPrefs", prefs);
WebDriver driver = new ChromeDriver(opts);

// Retrieve logs
LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
List<LogEntry> errors = logs.getAll().stream()
    .filter(e -> e.getLevel().equals(Level.SEVERE))
    .collect(Collectors.toList());
Assert.assertTrue(errors.isEmpty(), "JS errors on page: " + errors);

// Selenium 4 CDP approach
DevTools devTools = ((ChromeDriver) driver).getDevTools();
devTools.createSession();
devTools.send(Log.enable());
devTools.addListener(Log.entryAdded(), entry ->
    System.out.println("CDP: " + entry.getText()));
```

---

## Q81. How do you intercept network requests (Selenium 4 CDP)?

```java
ChromeDriver chrome = (ChromeDriver) driver;
DevTools devTools = chrome.getDevTools();
devTools.createSession();
devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));

// Capture API responses
List<String> apiCalls = new ArrayList<>();
devTools.addListener(Network.responseReceived(), resp -> {
    if (resp.getResponse().getUrl().contains("/api/")) {
        apiCalls.add(resp.getResponse().getUrl() + " → " + resp.getResponse().getStatus());
    }
});

// Block analytics
devTools.send(Network.setBlockedURLs(Arrays.asList("*.analytics.com/*")));

// Throttle network — simulate 3G
devTools.send(Network.emulateNetworkConditions(
    false, 100, 500_000, 100_000, Optional.empty()));

driver.get("https://example.com/dashboard");
System.out.println("API calls made: " + apiCalls);
```

---

## Q82. What is the difference between getText() and getAttribute("value")?

```java
// getText() — returns visible text content (innerText) of the element
// Works on: <p>, <div>, <span>, <button>, <td>, <h1>, etc.
String btnLabel = driver.findElement(By.id("submit")).getText();  // "Submit"

// getAttribute("value") — returns the VALUE property of form controls
// Works on: <input>, <textarea>, <select>
String inputVal = driver.findElement(By.id("email")).getAttribute("value");  // typed text

// KEY RULE:
// getText() on <input> → returns "" (inputs have no text content)
// getAttribute("value") on <div> → returns null

// For hidden element text use getAttribute("textContent") or "innerText"
String hiddenText = element.getAttribute("textContent");
```

---

## Q83. How do you verify element is visible vs present vs enabled?

```java
// isDisplayed() — in DOM AND visually rendered (not display:none, not opacity:0)
boolean visible = element.isDisplayed();

// isEnabled() — not disabled attribute
boolean enabled = element.isEnabled();  // false if <button disabled>

// isSelected() — for checkbox, radio, <option>
boolean checked = driver.findElement(By.id("agree")).isSelected();

// Presence — in DOM (may be hidden)
boolean present = !driver.findElements(By.id("msg")).isEmpty();

// Assertions
Assert.assertTrue(submitBtn.isDisplayed(), "Button not visible");
Assert.assertTrue(submitBtn.isEnabled(), "Button is disabled");
Assert.assertFalse(checkbox.isSelected(), "Checkbox should be unchecked");
```

---

## Q84. How do you handle elements that appear after a specific wait?

```java
// Trigger action, then wait for result element
driver.findElement(By.id("loadBtn")).click();
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
WebElement results = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("resultsTable")));

// Wait for text to appear
wait.until(ExpectedConditions.textToBePresentInElementLocated(
    By.id("status"), "Completed"));

// Wait for count to increase after click
int before = driver.findElements(By.cssSelector(".card")).size();
driver.findElement(By.id("loadMore")).click();
wait.until(d -> d.findElements(By.cssSelector(".card")).size() > before);
```

---

## Q85. What is Page Factory vs manual @FindBy initialization?

```java
// PageFactory — @FindBy with lazy proxy lookup
public class LoginPage {
    @FindBy(id = "username") private WebElement usernameField;

    public LoginPage(WebDriver driver) {
        PageFactory.initElements(driver, this);
        // Field is found when first accessed, not here
        // Risk: StaleElementReferenceException on dynamic SPA pages
    }
}

// Manual By locators — findElement called on every interaction (always fresh)
public class LoginPage {
    private final By username = By.id("username");
    private final WebDriver driver;

    public LoginPage(WebDriver driver) { this.driver = driver; }

    public void enterUsername(String user) {
        driver.findElement(username).sendKeys(user);  // fresh lookup, never stale
    }
}

// Recommendation:
// PageFactory — fine for stable, simple HTML pages
// Manual By — preferred for SPAs, React/Angular apps, AJAX-heavy pages
```

---

## Q86. How do you implement a retry mechanism for flaky tests?

```java
// RetryAnalyzer
public class RetryAnalyzer implements IRetryAnalyzer {
    private int count = 0;
    private static final int MAX = 2;

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && count < MAX) { count++; return true; }
        return false;
    }
}

// Apply globally via IAnnotationTransformer
public class RetryTransformer implements IAnnotationTransformer {
    @Override
    public void transform(ITestAnnotation ann, Class tc, Constructor c, Method m) {
        ann.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
// testng.xml: <listener class-name="com.utils.RetryTransformer"/>

// Custom retry for single interactions
public void clickWithRetry(By locator, int retries) {
    for (int i = 0; i < retries; i++) {
        try { driver.findElement(locator).click(); return; }
        catch (StaleElementReferenceException | ElementClickInterceptedException e) {
            if (i == retries - 1) throw e;
        }
    }
}
```

---

## Q87. What is the ExtentReport integration with Selenium?

```java
// ExtentManager.java (Singleton)
public class ExtentManager {
    private static ExtentReports extent;
    public static ExtentReports getInstance() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("target/ExtentReport.html");
            spark.config().setDocumentTitle("Test Report");
            extent = new ExtentReports();
            extent.attachReporter(spark);
        }
        return extent;
    }
}

// BaseTest — create test node + attach screenshot on failure
public class BaseTest {
    protected ExtentTest test;

    @BeforeMethod
    public void setUp(Method method) {
        test = ExtentManager.getInstance().createTest(method.getName());
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            test.fail(result.getThrowable());
            String b64 = ((TakesScreenshot) DriverManager.getDriver())
                .getScreenshotAs(OutputType.BASE64);
            test.addScreenCaptureFromBase64String(b64, "Failure Screenshot");
        } else {
            test.pass("Test Passed");
        }
        DriverManager.removeDriver();
        ExtentManager.getInstance().flush();
    }
}
```

---

## Q88. What is Allure integration with Selenium (screenshot on failure)?

```java
// pom.xml: allure-testng dependency
// testng.xml: <listener class-name="io.qameta.allure.testng.AllureTestNg"/>

// Listener — attach screenshot on failure
public class AllureListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) { attachScreenshot(); }

    @Attachment(value = "Failure Screenshot", type = "image/png")
    private byte[] attachScreenshot() {
        return ((TakesScreenshot) DriverManager.getDriver())
            .getScreenshotAs(OutputType.BYTES);
    }
}

// Test with Allure annotations
@Test
@Description("Valid login shows dashboard")
@Severity(SeverityLevel.CRITICAL)
@Feature("Auth")
public void loginTest() {
    Allure.step("Open login page", () -> driver.get(baseUrl + "/login"));
    Allure.step("Submit credentials", () -> {
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("Admin@123");
        driver.findElement(By.id("loginBtn")).click();
    });
    Allure.step("Verify dashboard", () ->
        Assert.assertTrue(driver.getTitle().contains("Dashboard")));
}
// allure serve target/allure-results
```

---

## Q89. How do you design a framework from scratch?

```
automation-framework/
├── pom.xml
├── testng.xml
└── src/test/
    ├── java/
    │   ├── base/
    │   │   ├── BasePage.java       (abstract, waitHelpers)
    │   │   └── BaseTest.java       (setUp/tearDown)
    │   ├── pages/
    │   │   ├── LoginPage.java
    │   │   └── DashboardPage.java
    │   ├── tests/
    │   │   └── LoginTest.java
    │   ├── utils/
    │   │   ├── DriverManager.java  (ThreadLocal<WebDriver>)
    │   │   ├── DriverFactory.java  (Chrome/Firefox/Edge)
    │   │   ├── ConfigReader.java   (Singleton properties)
    │   │   └── ExcelReader.java
    │   └── listeners/
    │       ├── FailureListener.java
    │       └── RetryAnalyzer.java
    └── resources/
        ├── config.properties
        └── testdata/
```

**Key decisions:**
- `ThreadLocal<WebDriver>` for parallel safety
- Abstract `BasePage` with shared helpers (`click`, `type`, `getText`)
- `ConfigReader` Singleton for one-time property load
- `DriverFactory` for browser-agnostic creation
- ITestListener for screenshots on failure
- TestNG RetryAnalyzer for flaky test resilience

---

## Q90. What is the BasePage abstract class pattern?

```java
public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected JavascriptExecutor js;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js     = (JavascriptExecutor) driver;
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }
    protected void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear(); el.sendKeys(text);
    }
    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }
    protected boolean isElementPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }
    protected void scrollTo(By locator) {
        js.executeScript("arguments[0].scrollIntoView({block:'center'});",
            driver.findElement(locator));
    }
    protected void jsClick(By locator) {
        js.executeScript("arguments[0].click();", driver.findElement(locator));
    }

    public abstract boolean isLoaded();
}
```

---

## Q91. How do you read test data from Excel in Selenium?

```java
// Apache POI dependency: poi-ooxml
public class ExcelReader {
    public static Object[][] getData(String file, String sheet) throws IOException {
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file))) {
            Sheet s = wb.getSheet(sheet);
            int rows = s.getPhysicalNumberOfRows();
            int cols = s.getRow(0).getPhysicalNumberOfCells();
            Object[][] data = new Object[rows - 1][cols];
            for (int i = 1; i < rows; i++) {
                Row row = s.getRow(i);
                for (int j = 0; j < cols; j++) {
                    Cell cell = row.getCell(j);
                    data[i-1][j] = cell == null ? "" : switch (cell.getCellType()) {
                        case STRING  -> cell.getStringCellValue();
                        case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
                        case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                        default -> "";
                    };
                }
            }
            return data;
        }
    }
}

@DataProvider(name = "loginData")
public Object[][] loginData() throws IOException {
    return ExcelReader.getData("src/test/resources/LoginData.xlsx", "Login");
}

@Test(dataProvider = "loginData")
public void loginTest(String user, String pass, String expected) {
    new LoginPage(DriverManager.getDriver()).login(user, pass);
    Assert.assertEquals(getToastMessage(), expected);
}
```

---

## Q92. How do you implement a test data factory pattern?

```java
public class TestDataFactory {

    public static TestUser validAdmin() {
        return new TestUser.Builder("admin@qoria.com", "Admin@123")
            .role("admin").active(true).build();
    }

    public static TestUser wrongPasswordUser() {
        return new TestUser.Builder("admin@qoria.com", "wrongpass").build();
    }

    public static TestUser lockedUser() {
        return new TestUser.Builder("locked@qoria.com", "Locked@123")
            .locked(true).build();
    }

    public static TestUser randomUser() {
        String ts = String.valueOf(System.currentTimeMillis());
        return new TestUser.Builder("user" + ts + "@test.com", "Test@12345").build();
    }
}

// Clean test methods
@Test
public void validLoginTest() {
    TestUser u = TestDataFactory.validAdmin();
    new LoginPage(DriverManager.getDriver()).login(u.getEmail(), u.getPassword());
    Assert.assertTrue(new DashboardPage(DriverManager.getDriver()).isLoaded());
}
```

---

## Q93. What is the difference between @BeforeMethod and @BeforeClass in Selenium context?

```java
// @BeforeClass — runs ONCE before all tests in the class
// Use for: shared session (login once, run all tests in class)
public class SearchTest extends BaseTest {
    @BeforeClass
    public void loginOnce() {
        new LoginPage(DriverManager.getDriver()).login("admin", "Admin@123");
    }
    @Test public void searchByName() { /* already logged in */ }
    @Test public void searchByDate() { /* already logged in */ }
}

// @BeforeMethod — runs before EVERY @Test method
// Use for: state reset, fresh navigation, clean test isolation
public class LoginTest extends BaseTest {
    @BeforeMethod
    public void goToLoginPage() {
        driver.get(baseUrl + "/login");
        driver.manage().deleteAllCookies();
    }
    @Test public void validLoginTest()   { /* starts at /login */ }
    @Test public void invalidLoginTest() { /* starts at /login */ }
}

// Execution order:
// @BeforeSuite → @BeforeTest → @BeforeClass → @BeforeMethod → @Test
//             → @AfterMethod → @AfterClass  → @AfterTest  → @AfterSuite
```

---

## Q94. How do you handle a page that uses Angular/React (dynamic loading)?

```java
// SPAs rebuild DOM after data loads — use manual By locators (avoid PageFactory)

// Wait for Angular stability
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
wait.until(d -> (Boolean) ((JavascriptExecutor) d)
    .executeScript("return window.getAllAngularTestabilities().every(t=>t.isStable())"));

// Wait for loading indicator to disappear
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loading")));

// FluentWait ignoring stale references (common in SPAs)
new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(15))
    .pollingEvery(Duration.ofMillis(300))
    .ignoring(StaleElementReferenceException.class)
    .ignoring(NoSuchElementException.class)
    .until(d -> d.findElement(By.id("content")).isDisplayed());
```

---

## Q95. What is the difference between isDisplayed(), isEnabled(), isSelected()?

```java
// isDisplayed() — rendered and visible on screen (not hidden by CSS)
element.isDisplayed();  // false if display:none or visibility:hidden

// isEnabled() — can be interacted with (no disabled attribute)
button.isEnabled();     // false if <button disabled>

// isSelected() — for checkboxes, radio buttons, <option> elements
checkbox.isSelected();  // true if checked

// Practical use
Assert.assertTrue(submitBtn.isDisplayed(), "Button not visible");
Assert.assertTrue(submitBtn.isEnabled(),   "Button is disabled");
Assert.assertFalse(agreeBox.isSelected(),  "Checkbox should start unchecked");
```

---

## Q96. How do you verify tooltip text?

```java
// 1. title attribute
String tooltip = driver.findElement(By.id("infoIcon")).getAttribute("title");
Assert.assertEquals(tooltip, "Click for more information");

// 2. aria-label
String label = element.getAttribute("aria-label");

// 3. Hover to trigger tooltip element, then read it
Actions actions = new Actions(driver);
actions.moveToElement(driver.findElement(By.id("infoIcon"))).perform();

WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
String tooltipText = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".tooltip")))
    .getText();
Assert.assertEquals(tooltipText, "Required field");
```

---

## Q97. How do you test responsive design with Selenium?

```java
int[][] viewports = {{375, 812}, {768, 1024}, {1366, 768}, {1920, 1080}};
String[] labels   = {"Mobile", "Tablet", "Laptop", "Desktop"};

for (int i = 0; i < viewports.length; i++) {
    driver.manage().window().setSize(new Dimension(viewports[i][0], viewports[i][1]));
    driver.navigate().refresh();

    boolean hamburger = driver.findElement(By.id("hamburger")).isDisplayed();
    boolean desktopNav = driver.findElement(By.id("desktopNav")).isDisplayed();

    if (viewports[i][0] < 768) {
        Assert.assertTrue(hamburger,   labels[i] + ": hamburger should show");
        Assert.assertFalse(desktopNav, labels[i] + ": desktop nav should hide");
    } else {
        Assert.assertFalse(hamburger,  labels[i] + ": hamburger should hide");
        Assert.assertTrue(desktopNav,  labels[i] + ": desktop nav should show");
    }
}
```

---

## Q98. How do you set browser window size?

```java
driver.manage().window().maximize();                             // maximize
driver.manage().window().fullscreen();                          // fullscreen
driver.manage().window().setSize(new Dimension(1920, 1080));   // exact size
driver.manage().window().setPosition(new Point(0, 0));         // position

Dimension current = driver.manage().window().getSize();
System.out.println(current.getWidth() + "x" + current.getHeight());

driver.manage().window().minimize();  // Selenium 4

// Headless — set via ChromeOptions (no screen)
opts.addArguments("--headless=new", "--window-size=1920,1080");
```

---

## Q99. How do you handle browser notifications (allow/block)?

```java
// Chrome — block notifications
HashMap<String, Object> prefs = new HashMap<>();
prefs.put("profile.default_content_setting_values.notifications", 2);  // 2=block, 1=allow
ChromeOptions opts = new ChromeOptions();
opts.setExperimentalOption("prefs", prefs);
WebDriver driver = new ChromeDriver(opts);

// Firefox — block notifications
FirefoxOptions ffOpts = new FirefoxOptions();
ffOpts.addPreference("permissions.default.desktop-notification", 2);

// Selenium 4 CDP — grant permission for specific origin
DevTools devTools = ((ChromeDriver) driver).getDevTools();
devTools.createSession();
devTools.send(Browser.grantPermissions(
    Arrays.asList(PermissionType.NOTIFICATIONS),
    Optional.of("https://example.com")));
```

---

## Q100. Walk me through designing and running a Selenium test from scratch (end-to-end).

**Scenario:** Verify a valid user can log in and reach the dashboard.

### 1 — Project setup (pom.xml)
```xml
<dependencies>
  <dependency>selenium-java 4.18.1</dependency>
  <dependency>testng 7.9.0 scope=test</dependency>
  <dependency>webdrivermanager 5.7.0</dependency>
</dependencies>
<build>
  <plugins>
    <plugin>maven-surefire-plugin: suiteXmlFiles=testng.xml</plugin>
  </plugins>
</build>
```

### 2 — config.properties (src/test/resources)
```
base.url=https://myapp.staging.com
browser=chrome
explicit.wait=15
```

### 3 — DriverFactory + DriverManager
```java
public class DriverFactory {
    public static WebDriver createDriver(String browser) {
        if ("chrome".equals(browser)) {
            WebDriverManager.chromedriver().setup();
            return new ChromeDriver(new ChromeOptions().addArguments("--start-maximized"));
        }
        throw new IllegalArgumentException(browser);
    }
}

public class DriverManager {
    private static final ThreadLocal<WebDriver> tl = new ThreadLocal<>();
    public static void set(WebDriver d)  { tl.set(d); }
    public static WebDriver get()        { return tl.get(); }
    public static void remove()          { tl.get().quit(); tl.remove(); }
}
```

### 4 — BasePage
```java
public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    public BasePage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(15));
    }
    protected void click(By l)            { wait.until(elementToBeClickable(l)).click(); }
    protected void type(By l, String t)   { wait.until(visibilityOf(l)).clear(); wait.until(visibilityOf(l)).sendKeys(t); }
    protected String getText(By l)        { return wait.until(visibilityOf(l)).getText(); }
    public abstract boolean isLoaded();

    private WebElement visibilityOf(By l) { return wait.until(ExpectedConditions.visibilityOfElementLocated(l)); }
    private WebElement elementToBeClickable(By l) { return wait.until(ExpectedConditions.elementToBeClickable(l)); }
}
```

### 5 — Page Objects
```java
public class LoginPage extends BasePage {
    private By username = By.id("username");
    private By password = By.id("password");
    private By loginBtn = By.cssSelector("button[type='submit']");

    public LoginPage(WebDriver d) { super(d); }

    public DashboardPage login(String user, String pass) {
        driver.get(ConfigReader.get("base.url") + "/login");
        type(username, user); type(password, pass); click(loginBtn);
        return new DashboardPage(driver);
    }
    @Override public boolean isLoaded() { return !driver.findElements(username).isEmpty(); }
}

public class DashboardPage extends BasePage {
    private By welcome = By.id("welcomeMsg");
    public DashboardPage(WebDriver d) { super(d); }
    public String getWelcomeText() { return getText(welcome); }
    @Override public boolean isLoaded() { return !driver.findElements(welcome).isEmpty(); }
}
```

### 6 — BaseTest
```java
public class BaseTest {
    @BeforeMethod
    public void setUp() {
        DriverManager.set(DriverFactory.createDriver(ConfigReader.get("browser", "chrome")));
    }
    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            // take screenshot...
        }
        DriverManager.remove();
    }
}
```

### 7 — Test
```java
public class LoginTest extends BaseTest {
    @Test(description = "Valid login should show dashboard")
    public void validLoginShowsDashboard() {
        DashboardPage dash = new LoginPage(DriverManager.get())
            .login("admin@qoria.com", "Admin@123");
        Assert.assertTrue(dash.isLoaded(), "Dashboard did not load");
        Assert.assertTrue(dash.getWelcomeText().contains("Welcome"), "Welcome text missing");
    }
}
```

### 8 — testng.xml
```xml
<suite name="Regression" parallel="methods" thread-count="4">
  <listeners><listener class-name="com.listeners.FailureListener"/></listeners>
  <test name="Login">
    <classes><class name="com.tests.LoginTest"/></classes>
  </test>
</suite>
```

### 9 — Run
```bash
mvn clean test -Dbrowser=chrome -Dsurefire.suiteXmlFiles=testng.xml
# Report: target/ExtentReport.html
# Allure: allure serve target/allure-results
```

---

*End of Part 4 — Selenium WebDriver | 100 Questions*
