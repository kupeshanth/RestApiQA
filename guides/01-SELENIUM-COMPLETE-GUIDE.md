# Selenium WebDriver — Complete Guide
## Java + Selenium + WebDriverManager | Architecture to Advanced

---

## TABLE OF CONTENTS
1. [Architecture](#1-architecture)
2. [Setup & Dependencies](#2-setup--dependencies)
3. [Browser Setup](#3-browser-setup)
4. [Locators — Every Strategy](#4-locators--every-strategy)
5. [Actions — Every Interaction](#5-actions--every-interaction)
6. [Waits — Implicit, Explicit, Fluent](#6-waits--implicit-explicit-fluent)
7. [Page Object Model](#7-page-object-model)
8. [Handling Special Elements](#8-handling-special-elements)
9. [Screenshots & Reporting](#9-screenshots--reporting)
10. [Parallel Execution](#10-parallel-execution)
11. [Common Exceptions & Fixes](#11-common-exceptions--fixes)
12. [Interview Q&A](#12-interview-qa)

---

## 1. Architecture

```
Your Java Test Code
      ↓
WebDriver API (Selenium Client Library)
      ↓  [W3C WebDriver Protocol — HTTP/JSON]
ChromeDriver / GeckoDriver / SafariDriver
      ↓  [Browser-native calls]
Chrome / Firefox / Safari
```

- **Client** = your Java test + Selenium JAR
- **Driver** = browser-specific server (ChromeDriver, GeckoDriver)
- **Protocol** = W3C WebDriver (JSON over HTTP)
- Each command (`click`, `sendKeys`) is an HTTP request to the driver

---

## 2. Setup & Dependencies

### pom.xml
```xml
<!-- Selenium WebDriver -->
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.15.0</version>
</dependency>

<!-- WebDriverManager: auto-downloads correct ChromeDriver version -->
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>5.6.3</version>
    <scope>test</scope>
</dependency>

<!-- TestNG -->
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.8.0</version>
    <scope>test</scope>
</dependency>
```

---

## 3. Browser Setup

### BaseTest.java — ThreadLocal for parallel safety
```java
package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.*;

public class BaseTest {

    // ThreadLocal: each parallel thread gets its OWN driver instance
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public WebDriver getDriver() {
        return driver.get();
    }

    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        WebDriver webDriver;

        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                webDriver = new FirefoxDriver();
                break;
            case "chrome":
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless");           // headless for CI
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-gpu");
                options.addArguments("--window-size=1920,1080");
                webDriver = new ChromeDriver(options);
                break;
        }

        webDriver.manage().window().maximize();
        webDriver.manage().deleteAllCookies();
        driver.set(webDriver);
    }

    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();              // prevent memory leak
        }
    }
}
```

---

## 4. Locators — Every Strategy

```java
// ── PRIORITY ORDER (use from top down) ──────────────────────────────────────

// 1. ID — fastest, most reliable
driver.findElement(By.id("username"));

// 2. Name
driver.findElement(By.name("email"));

// 3. CSS Selector — fast and flexible
driver.findElement(By.cssSelector("#loginForm input[type='email']"));
driver.findElement(By.cssSelector(".btn.btn-primary"));
driver.findElement(By.cssSelector("[data-testid='submit']"));
driver.findElement(By.cssSelector("div.container > button:first-child"));

// 4. XPath — most powerful but slowest
driver.findElement(By.xpath("//button[@type='submit']"));
driver.findElement(By.xpath("//button[contains(text(),'Login')]"));
driver.findElement(By.xpath("//label[text()='Username']/following-sibling::input"));
driver.findElement(By.xpath("(//button[@class='btn'])[2]"));   // second button

// 5. Link Text / Partial Link Text
driver.findElement(By.linkText("Sign In"));
driver.findElement(By.partialLinkText("Sign"));

// 6. Class Name — only single class
driver.findElement(By.className("submit-btn"));

// 7. Tag Name — rarely useful alone
driver.findElements(By.tagName("tr"));            // get all rows

// ── CSS SELECTOR CHEAT SHEET ─────────────────────────────────────────────────
// #id              → By id
// .class           → By class
// tag              → By tag name
// [attr]           → Has attribute
// [attr='value']   → Attribute equals value
// parent > child   → Direct child
// ancestor child   → Any descendant
// :first-child     → First child
// :last-child      → Last child
// :nth-child(2)    → Second child

// ── XPATH CHEAT SHEET ────────────────────────────────────────────────────────
// //tag                        → any tag anywhere
// //tag[@attr='value']         → tag with attribute
// //tag[text()='exact']        → tag with exact text
// //tag[contains(text(),'x')]  → tag containing text
// //tag[contains(@class,'x')]  → class contains
// //parent/child               → direct child
// //parent//descendant         → any descendant
// //tag[1]                     → first element (1-indexed in XPath)
// following-sibling::tag       → next sibling
// preceding-sibling::tag       → previous sibling
// parent::tag                  → parent element
```

---

## 5. Actions — Every Interaction

```java
WebElement el = driver.findElement(By.id("target"));

// ── BASIC INTERACTIONS ───────────────────────────────────────────────────────
el.click();
el.sendKeys("Hello World");
el.clear();
el.submit();                              // submits a form
el.getText();                             // get visible text
el.getAttribute("href");                  // get attribute value
el.getCssValue("color");                  // get CSS property
el.isDisplayed();                         // is visible?
el.isEnabled();                           // is interactable?
el.isSelected();                          // is checkbox/radio selected?

// ── DROPDOWNS ────────────────────────────────────────────────────────────────
Select dropdown = new Select(driver.findElement(By.id("country")));
dropdown.selectByValue("UK");             // by value attribute
dropdown.selectByVisibleText("United Kingdom");
dropdown.selectByIndex(2);               // 0-indexed
dropdown.getFirstSelectedOption().getText();
dropdown.getAllSelectedOptions();         // multi-select

// ── KEYBOARD ACTIONS ─────────────────────────────────────────────────────────
el.sendKeys(Keys.ENTER);
el.sendKeys(Keys.TAB);
el.sendKeys(Keys.BACK_SPACE);
el.sendKeys(Keys.chord(Keys.CONTROL, "a"));   // Ctrl+A (select all)
el.sendKeys(Keys.chord(Keys.CONTROL, "c"));   // Ctrl+C (copy)

// ── ACTIONS CLASS — advanced mouse/keyboard ──────────────────────────────────
Actions actions = new Actions(driver);
actions.moveToElement(el).click().perform();          // hover then click
actions.doubleClick(el).perform();
actions.contextClick(el).perform();                   // right click
actions.dragAndDrop(source, target).perform();
actions.clickAndHold(el).moveByOffset(100,0).release().perform();  // drag
actions.sendKeys(el, "text").perform();

// ── JAVASCRIPT EXECUTOR ──────────────────────────────────────────────────────
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].click();", el);        // force click even if hidden
js.executeScript("arguments[0].scrollIntoView(true);", el);  // scroll to element
js.executeScript("window.scrollTo(0, document.body.scrollHeight)");  // scroll to bottom
String text = (String) js.executeScript("return arguments[0].textContent;", el);
js.executeScript("arguments[0].value='new value';", el);  // set value directly

// ── NAVIGATE ─────────────────────────────────────────────────────────────────
driver.navigate().to("https://example.com");
driver.navigate().back();
driver.navigate().forward();
driver.navigate().refresh();
driver.get("https://example.com");       // same as navigate().to()
```

---

## 6. Waits — Implicit, Explicit, Fluent

```java
// ── IMPLICIT WAIT ─────────────────────────────────────────────────────────────
// Global — applies to all findElement calls. Waits up to N seconds for element to appear.
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
// Set ONCE in BaseTest setUp().
// NEVER combine with explicit wait — unpredictable behaviour.

// ── EXPLICIT WAIT ─────────────────────────────────────────────────────────────
// Specific — waits for one condition on one element.
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

// Visibility
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loader")));

// Clickability
WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));

// Text / attribute
wait.until(ExpectedConditions.textToBePresentInElement(el, "Success"));
wait.until(ExpectedConditions.attributeContains(By.id("status"), "class", "active"));

// URL / title
wait.until(ExpectedConditions.urlContains("/dashboard"));
wait.until(ExpectedConditions.titleIs("Dashboard"));

// Count
wait.until(ExpectedConditions.numberOfElementsToBe(By.tagName("tr"), 5));

// Alert
wait.until(ExpectedConditions.alertIsPresent());

// ── FLUENT WAIT ───────────────────────────────────────────────────────────────
// Like explicit wait + polling interval + ignore specific exceptions.
Wait<WebDriver> fluentWait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(30))
    .pollingEvery(Duration.ofSeconds(2))
    .ignoring(NoSuchElementException.class)
    .ignoring(StaleElementReferenceException.class);

WebElement result = fluentWait.until(d -> d.findElement(By.id("result")));
// Use for: elements that appear unpredictably (e.g. after an async operation)

// ── NEVER USE ─────────────────────────────────────────────────────────────────
Thread.sleep(3000);   // hard sleep — makes tests slow AND still flaky
```

---

## 7. Page Object Model

```java
// ── LoginPage.java ───────────────────────────────────────────────────────────
package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.*;
import org.openqa.selenium.support.ui.*;

public class LoginPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators defined ONCE here. If UI changes, update only this file.
    @FindBy(id = "username")          private WebElement usernameField;
    @FindBy(id = "password")          private WebElement passwordField;
    @FindBy(id = "loginBtn")          private WebElement loginButton;
    @FindBy(css = ".error-message")   private WebElement errorMessage;
    @FindBy(linkText = "Forgot?")     private WebElement forgotPasswordLink;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);  // initialise @FindBy fields
    }

    public void navigate() {
        driver.get("/login");
    }

    public DashboardPage login(String username, String password) {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.clear();
        passwordField.sendKeys(password);
        loginButton.click();
        return new DashboardPage(driver);    // return next page
    }

    public String getErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(errorMessage));
        return errorMessage.getText();
    }

    public boolean isLoaded() {
        return loginButton.isDisplayed();
    }
}

// ── LoginTest.java ────────────────────────────────────────────────────────────
public class LoginTest extends BaseTest {

    @Test
    public void validLogin_redirectsToDashboard() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.navigate();
        DashboardPage dashboard = loginPage.login("admin", "Admin@123");
        Assert.assertTrue(dashboard.isLoaded());
    }

    @Test
    public void wrongPassword_showsError() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.navigate();
        loginPage.login("admin", "wrong");
        Assert.assertEquals(loginPage.getErrorMessage(), "Invalid credentials");
    }
}
```

---

## 8. Handling Special Elements

```java
// ── IFRAMES ───────────────────────────────────────────────────────────────────
driver.switchTo().frame("iframeName");              // by name
driver.switchTo().frame(0);                         // by index
driver.switchTo().frame(driver.findElement(By.id("iframeId")));  // by element
// ... interact with elements inside iframe ...
driver.switchTo().defaultContent();                 // back to main page
driver.switchTo().parentFrame();                    // up one frame level

// ── ALERTS ───────────────────────────────────────────────────────────────────
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
wait.until(ExpectedConditions.alertIsPresent());
Alert alert = driver.switchTo().alert();
System.out.println(alert.getText());    // read message
alert.accept();                         // OK
alert.dismiss();                        // Cancel
alert.sendKeys("typed value");          // prompt dialog

// ── MULTIPLE WINDOWS / TABS ───────────────────────────────────────────────────
String mainWindow = driver.getWindowHandle();
Set<String> allWindows = driver.getWindowHandles();

for (String window : allWindows) {
    if (!window.equals(mainWindow)) {
        driver.switchTo().window(window);    // switch to new window
        break;
    }
}
// ... interact with new window ...
driver.close();                              // close current window
driver.switchTo().window(mainWindow);        // go back to main

// ── FILE UPLOAD ───────────────────────────────────────────────────────────────
WebElement uploadInput = driver.findElement(By.id("fileInput"));
uploadInput.sendKeys("C:\\path\\to\\file.pdf");   // Windows path
uploadInput.sendKeys("/home/user/file.pdf");       // Linux/Mac path

// ── CHECKBOXES ────────────────────────────────────────────────────────────────
WebElement checkbox = driver.findElement(By.id("agree"));
if (!checkbox.isSelected()) {
    checkbox.click();   // check it
}

// ── SCROLL ────────────────────────────────────────────────────────────────────
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("window.scrollBy(0, 500)");                  // scroll down 500px
js.executeScript("arguments[0].scrollIntoView(true);", el);  // scroll to element
js.executeScript("window.scrollTo(0, 0)");                    // scroll to top

// ── HOVER ────────────────────────────────────────────────────────────────────
new Actions(driver).moveToElement(menuItem).perform();
// then find and click the dropdown item that appeared
driver.findElement(By.linkText("Sub Menu Item")).click();
```

---

## 9. Screenshots & Reporting

```java
// ── TAKE SCREENSHOT ───────────────────────────────────────────────────────────
public static void takeScreenshot(WebDriver driver, String name) throws IOException {
    TakesScreenshot ts = (TakesScreenshot) driver;
    File src = ts.getScreenshotAs(OutputType.FILE);
    FileUtils.copyFile(src, new File("screenshots/" + name + ".png"));
}

// ── TESTNG LISTENER — auto screenshot on failure ──────────────────────────────
public class TestListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) {
        Object instance = result.getInstance();
        WebDriver driver = ((BaseTest) instance).getDriver();
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File src = ts.getScreenshotAs(OutputType.FILE);
            String path = "screenshots/FAIL_" + result.getName() + "_"
                          + System.currentTimeMillis() + ".png";
            FileUtils.copyFile(src, new File(path));
            System.out.println("Screenshot saved: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Register in testng.xml:
// <listeners><listener class-name="listeners.TestListener"/></listeners>
```

---

## 10. Parallel Execution

```xml
<!-- testng.xml — run tests in parallel across browsers -->
<suite name="Parallel Suite" parallel="tests" thread-count="2">

    <test name="Chrome">
        <parameter name="browser" value="chrome"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>

    <test name="Firefox">
        <parameter name="browser" value="firefox"/>
        <classes><class name="tests.LoginTest"/></classes>
    </test>

</suite>
```

**Key rule:** Use `ThreadLocal<WebDriver>` in BaseTest — each thread gets its own driver. Never use a single shared `driver` field in parallel tests.

---

## 11. Common Exceptions & Fixes

| Exception | Cause | Fix |
|-----------|-------|-----|
| `NoSuchElementException` | Element not in DOM | Check locator, add wait |
| `StaleElementReferenceException` | DOM re-rendered after you found element | Re-find element after action |
| `ElementNotInteractableException` | Element exists but not clickable | Scroll into view, wait for clickability |
| `TimeoutException` | Wait expired before condition met | Increase timeout, fix locator |
| `ElementClickInterceptedException` | Another element is on top | Scroll, wait for overlay to disappear, JS click |
| `WebDriverException: disconnected` | Browser crashed or closed | Check for unexpected popups/alerts |
| `SessionNotCreatedException` | Driver version mismatch | Use WebDriverManager to auto-manage |

```java
// Fix StaleElementReferenceException with retry:
public void clickWithRetry(By locator) {
    for (int i = 0; i < 3; i++) {
        try {
            driver.findElement(locator).click();
            return;
        } catch (StaleElementReferenceException e) {
            if (i == 2) throw e;
        }
    }
}

// Fix ElementClickInterceptedException with JS:
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].click();", driver.findElement(locator));
```

---

## 12. Interview Q&A

**Q: What is the difference between `driver.close()` and `driver.quit()`?**
```
close() → closes only the current browser window/tab
quit()  → closes ALL windows + ends the WebDriver session + kills the driver process
Always use quit() in @AfterMethod to clean up completely.
```

**Q: Why use WebDriverManager instead of manual ChromeDriver?**
```
Without WebDriverManager: download ChromeDriver manually, match version to Chrome,
update manually every Chrome update.
With WebDriverManager: one line — WebDriverManager.chromedriver().setup() —
automatically downloads the correct version for the installed Chrome.
```

**Q: What is the difference between `get()` and `navigate().to()`?**
```
driver.get(url)             → loads URL, waits for page load event
driver.navigate().to(url)   → same behaviour
driver.navigate().back()    → browser back (only navigate() has this)
driver.navigate().refresh() → reload page
get() is shorthand, navigate() has extra methods.
```

**Q: How do you handle a page that loads content via AJAX (after initial load)?**
```java
// Wait for the AJAX-loaded element to appear
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ajaxResult")));

// Or wait for jQuery AJAX to complete (if page uses jQuery):
wait.until(d -> (Boolean)((JavascriptExecutor)d)
    .executeScript("return jQuery.active == 0"));
```
