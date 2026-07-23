# Part 12 — Framework Design | 40 Questions | Complete Architecture Answers

> CV Context: Kupeshanth — Qoria Lanka (Selenium+TestNG, Playwright, GitHub Actions), Cerexio (Angular, Postman, JIRA). Projects: Singer Page (Serenity+Cucumber+BDD), Python+Pytest+Selenium, Playwright+JS.

---

## Q1. Design a Selenium+TestNG framework from scratch — full folder structure + all key files with code.

**Answer:**

### Folder Structure

```
selenium-testng-framework/
├── src/
│   ├── main/java/
│   │   └── com/framework/
│   │       ├── base/
│   │       │   ├── BaseTest.java
│   │       │   └── DriverFactory.java
│   │       ├── config/
│   │       │   └── ConfigReader.java
│   │       ├── pages/
│   │       │   ├── LoginPage.java
│   │       │   └── DashboardPage.java
│   │       └── listeners/
│   │           └── TestListener.java
│   └── test/java/
│       └── com/tests/
│           ├── LoginTest.java
│           └── DashboardTest.java
├── src/test/resources/
│   ├── config.properties
│   └── testng.xml
├── screenshots/
└── pom.xml
```

### BaseTest.java

```java
package com.framework.base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;

public class BaseTest {

    // ThreadLocal ensures each thread gets its own WebDriver instance (parallel-safe)
    protected static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driver.get();
    }

    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        driver.set(DriverFactory.createDriver(browser));
        getDriver().manage().window().maximize();
        getDriver().get(ConfigReader.getProperty("base.url"));
    }

    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove(); // Prevent memory leaks in parallel execution
        }
    }
}
```

### DriverFactory.java

```java
package com.framework.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

public class DriverFactory {

    public static WebDriver createDriver(String browser) {
        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
                return new ChromeDriver(options);
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
    }
}
```

### ConfigReader.java

```java
package com.framework.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties = new Properties();

    static {
        try {
            FileInputStream file = new FileInputStream("src/test/resources/config.properties");
            properties.load(file);
        } catch (IOException e) {
            throw new RuntimeException("config.properties not found", e);
        }
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) throw new RuntimeException("Key not found: " + key);
        return value;
    }
}
```

### TestListener.java (screenshot on failure)

```java
package com.framework.listeners;

import com.framework.base.BaseTest;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            TakesScreenshot ts = (TakesScreenshot) BaseTest.getDriver();
            File src = ts.getScreenshotAs(OutputType.FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String dest = "screenshots/" + result.getName() + "_" + timestamp + ".png";
            Files.copy(src.toPath(), new File(dest).toPath());
            System.out.println("Screenshot saved: " + dest);
        } catch (Exception e) {
            System.out.println("Screenshot failed: " + e.getMessage());
        }
    }
}
```

### LoginPage.java (POM)

```java
package com.framework.pages;

import com.framework.base.BaseTest;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {

    @FindBy(id = "username") private WebElement usernameField;
    @FindBy(id = "password") private WebElement passwordField;
    @FindBy(id = "loginBtn")  private WebElement loginButton;

    public LoginPage() {
        PageFactory.initElements(BaseTest.getDriver(), this);
    }

    public DashboardPage login(String user, String pass) {
        usernameField.sendKeys(user);
        passwordField.sendKeys(pass);
        loginButton.click();
        return new DashboardPage();
    }
}
```

### testng.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="RegressionSuite" parallel="methods" thread-count="3">
    <listeners>
        <listener class-name="com.framework.listeners.TestListener"/>
    </listeners>
    <test name="Chrome Tests">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="com.tests.LoginTest"/>
            <class name="com.tests.DashboardTest"/>
        </classes>
    </test>
</suite>
```

---

## Q2. Design a REST API test framework with RestAssured — full structure.

**Answer:**

### Folder Structure

```
restassured-framework/
├── src/
│   ├── main/java/com/api/
│   │   ├── base/
│   │   │   └── BaseTest.java
│   │   ├── constants/
│   │   │   └── ApiConstants.java
│   │   └── utils/
│   │       └── ConfigReader.java
│   └── test/java/com/api/tests/
│       ├── GetUserTests.java
│       └── CreateUserTests.java
├── src/test/resources/
│   ├── schemas/
│   │   └── user-schema.json
│   ├── config.properties
│   └── testng.xml
└── pom.xml
```

### BaseTest.java

```java
package com.api.base;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

public class BaseTest {

    protected RequestSpecification requestSpec;

    @BeforeClass
    public void setup() {
        requestSpec = new RequestSpecBuilder()
            .setBaseUri("https://reqres.in")
            .setBasePath("/api")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build();
    }
}
```

### ApiConstants.java

```java
package com.api.constants;

public class ApiConstants {
    public static final String GET_USERS    = "/users";
    public static final String GET_USER     = "/users/{id}";
    public static final String CREATE_USER  = "/users";
    public static final String UPDATE_USER  = "/users/{id}";
    public static final String DELETE_USER  = "/users/{id}";
}
```

### GetUserTests.java

```java
package com.api.tests;

import com.api.base.BaseTest;
import com.api.constants.ApiConstants;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class GetUserTests extends BaseTest {

    @Test
    public void testGetAllUsers() {
        given()
            .spec(requestSpec)
            .queryParam("page", 2)
        .when()
            .get(ApiConstants.GET_USERS)
        .then()
            .statusCode(200)
            .body("page", equalTo(2))
            .body("data", not(empty()))
            .body("data[0].id", notNullValue());
    }

    @Test
    public void testGetSingleUser_ValidSchema() {
        given()
            .spec(requestSpec)
            .pathParam("id", 2)
        .when()
            .get(ApiConstants.GET_USER)
        .then()
            .statusCode(200)
            .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/user-schema.json"));
    }

    @Test
    public void testGetUser_NotFound() {
        given()
            .spec(requestSpec)
            .pathParam("id", 9999)
        .when()
            .get(ApiConstants.GET_USER)
        .then()
            .statusCode(404);
    }
}
```

### user-schema.json

```json
{
  "$schema": "http://json-schema.org/draft-07/schema",
  "type": "object",
  "required": ["data"],
  "properties": {
    "data": {
      "type": "object",
      "required": ["id", "email", "first_name", "last_name"],
      "properties": {
        "id":         { "type": "integer" },
        "email":      { "type": "string", "format": "email" },
        "first_name": { "type": "string" },
        "last_name":  { "type": "string" },
        "avatar":     { "type": "string" }
      }
    }
  }
}
```

---

## Q3. Design a Playwright framework — playwright.config.ts, POM BasePage+LoginPage, auth.setup.ts, fixtures.

**Answer:**

### playwright.config.ts

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 2 : undefined,
  reporter: [['html', { open: 'never' }], ['allure-playwright']],

  use: {
    baseURL: process.env.BASE_URL || 'https://staging.example.com',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

  projects: [
    { name: 'setup', testMatch: /auth\.setup\.ts/ },
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: '.auth/user.json',
      },
      dependencies: ['setup'],
    },
    {
      name: 'firefox',
      use: {
        ...devices['Desktop Firefox'],
        storageState: '.auth/user.json',
      },
      dependencies: ['setup'],
    },
  ],
});
```

### pages/BasePage.ts

```typescript
import { Page, Locator } from '@playwright/test';

export class BasePage {
  constructor(protected page: Page) {}

  async navigateTo(path: string) {
    await this.page.goto(path);
  }

  async getTitle(): Promise<string> {
    return this.page.title();
  }

  async waitForElement(locator: Locator) {
    await locator.waitFor({ state: 'visible', timeout: 10000 });
  }

  async takeScreenshot(name: string) {
    await this.page.screenshot({ path: `screenshots/${name}.png` });
  }
}
```

### pages/LoginPage.ts

```typescript
import { Page, expect } from '@playwright/test';
import { BasePage } from './BasePage';

export class LoginPage extends BasePage {
  private usernameInput = this.page.getByRole('textbox', { name: 'Username' });
  private passwordInput = this.page.getByRole('textbox', { name: 'Password' });
  private loginButton   = this.page.getByRole('button', { name: 'Login' });
  private errorMessage  = this.page.locator('.error-message');

  constructor(page: Page) {
    super(page);
  }

  async login(username: string, password: string) {
    await this.navigateTo('/login');
    await this.usernameInput.fill(username);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }

  async expectErrorMessage(text: string) {
    await expect(this.errorMessage).toContainText(text);
  }
}
```

### auth.setup.ts

```typescript
import { test as setup, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

const authFile = '.auth/user.json';

setup('authenticate', async ({ page }) => {
  const loginPage = new LoginPage(page);
  await loginPage.login(
    process.env.TEST_USER || 'admin@example.com',
    process.env.TEST_PASS || 'password123'
  );

  await expect(page).toHaveURL(/dashboard/);
  // Save authentication state — reused by all tests, saves login time
  await page.context().storageState({ path: authFile });
});
```

### fixtures/index.ts

```typescript
import { test as base } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';

type MyFixtures = {
  loginPage: LoginPage;
  dashboardPage: DashboardPage;
};

export const test = base.extend<MyFixtures>({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page));
  },
  dashboardPage: async ({ page }, use) => {
    await use(new DashboardPage(page));
  },
});

export { expect } from '@playwright/test';
```

---

## Q4. What is the Page Object Model? Why use it? Show before/after code.

**Answer:**

POM is a design pattern where each page/component of the application has a corresponding class that contains the locators and actions for that page.

**Why use it:**
- Separation of concerns: locators and test logic are in separate places
- Maintainability: if a locator changes, update only one class
- Reusability: the same page method can be called from multiple tests
- Readability: tests read like business requirements

**Before POM (everything in test):**

```java
@Test
public void testLogin() {
    driver.findElement(By.id("username")).sendKeys("admin");
    driver.findElement(By.id("password")).sendKeys("secret");
    driver.findElement(By.id("loginBtn")).click();
    Assert.assertTrue(driver.findElement(By.id("welcome")).isDisplayed());
}

@Test
public void testLoginFails() {
    driver.findElement(By.id("username")).sendKeys("admin");
    driver.findElement(By.id("password")).sendKeys("wrongpass");
    driver.findElement(By.id("loginBtn")).click();
    Assert.assertTrue(driver.findElement(By.id("errorMsg")).isDisplayed());
    // Locators duplicated in every test — nightmare to maintain
}
```

**After POM:**

```java
// LoginPage.java
public class LoginPage {
    private WebDriver driver;

    @FindBy(id = "username") private WebElement usernameField;
    @FindBy(id = "password") private WebElement passwordField;
    @FindBy(id = "loginBtn")  private WebElement loginButton;
    @FindBy(id = "errorMsg")  private WebElement errorMessage;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public DashboardPage login(String user, String pass) {
        usernameField.sendKeys(user);
        passwordField.sendKeys(pass);
        loginButton.click();
        return new DashboardPage(driver);
    }

    public String getErrorMessage() {
        return errorMessage.getText();
    }
}

// LoginTest.java — clean, readable, maintainable
@Test
public void testLogin() {
    LoginPage loginPage = new LoginPage(driver);
    DashboardPage dashboard = loginPage.login("admin", "secret");
    Assert.assertTrue(dashboard.isWelcomeDisplayed());
}
```

---

## Q5. What is Page Factory? @FindBy, PageFactory.initElements.

**Answer:**

Page Factory is a built-in Selenium support class for implementing POM. It uses `@FindBy` annotations to declare locators and `PageFactory.initElements()` to initialize them as proxies (lazy-loaded — element is located when first interacted with).

```java
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

public class SearchPage {

    // Standard @FindBy with strategy
    @FindBy(id = "searchInput")
    private WebElement searchBox;

    // Multiple locators — first one found wins
    @FindBy(how = How.CSS, using = ".search-btn")
    private WebElement searchButton;

    // FindAll — locates all elements matching ANY of the given criteria
    @FindBy(xpath = "//ul[@class='results']/li")
    private List<WebElement> resultsList;

    public SearchPage(WebDriver driver) {
        // This initialises all @FindBy fields as WebElement proxies
        PageFactory.initElements(driver, this);
    }

    public void search(String term) {
        searchBox.clear();
        searchBox.sendKeys(term);
        searchButton.click();
    }

    public int getResultCount() {
        return resultsList.size();
    }
}
```

**Key points:**
- Elements are lazy proxies — no NoSuchElementException at construction time
- The driver is stored internally by the proxy and looks up the element on each interaction
- Works well with StaleElementReferenceException since each interaction re-locates the element

---

## Q6. What is a Base Test class? What should go in it?

**Answer:**

A Base Test class is the parent class for all test classes. It contains setup and teardown logic that all tests need, so you don't repeat it in every test class.

```java
package com.framework.base;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.*;
import java.time.Duration;

public class BaseTest {

    // 1. ThreadLocal driver for parallel test safety
    protected static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() { return driver.get(); }

    // 2. Browser setup from testng.xml parameter
    @BeforeMethod
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        driver.set(DriverFactory.createDriver(browser));
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        getDriver().get(ConfigReader.getProperty("base.url"));
    }

    // 3. Driver teardown
    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
        }
    }
}
```

**What belongs in BaseTest:**
- Driver initialization and teardown
- Browser URL navigation
- Common waits/timeouts
- ThreadLocal for parallel safety

**What does NOT belong in BaseTest:**
- Test methods (those go in test classes)
- Page-specific interactions
- Assertions

---

## Q7. What is a Driver Factory? Show full implementation.

**Answer:**

A Driver Factory is a class responsible for creating WebDriver instances. It centralises browser creation logic so you can support multiple browsers without putting if/else in every test.

```java
package com.framework.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;

public class DriverFactory {

    private DriverFactory() {} // Prevent instantiation — utility class

    public static WebDriver createDriver(String browser) {
        boolean headless = Boolean.parseBoolean(
            System.getProperty("headless", ConfigReader.getProperty("headless"))
        );

        switch (browser.trim().toLowerCase()) {

            case "chrome": {
                WebDriverManager.chromedriver().setup();
                ChromeOptions opts = new ChromeOptions();
                if (headless) {
                    opts.addArguments("--headless=new", "--no-sandbox",
                                      "--disable-dev-shm-usage", "--window-size=1920,1080");
                }
                return new ChromeDriver(opts);
            }

            case "firefox": {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("--headless");
                return new FirefoxDriver(opts);
            }

            case "edge": {
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();
            }

            case "safari":
                return new SafariDriver(); // Safari manages its own driver

            default:
                throw new IllegalArgumentException(
                    "Unsupported browser: " + browser +
                    ". Supported: chrome, firefox, edge, safari"
                );
        }
    }
}
```

---

## Q8. What is a Config Reader? Read from properties file.

**Answer:**

```java
package com.framework.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();
    private static final String CONFIG_PATH = "src/test/resources/config.properties";

    static {
        try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config file: " + CONFIG_PATH, e);
        }
    }

    // System property takes precedence (allows CI override)
    public static String getProperty(String key) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) return sysProp;
        String value = properties.getProperty(key);
        if (value == null) throw new RuntimeException("Property not found: " + key);
        return value.trim();
    }

    public static int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }
}
```

**config.properties:**

```properties
base.url=https://staging.example.com
browser=chrome
headless=false
implicit.wait=10
explicit.wait=15
db.url=jdbc:postgresql://localhost:5432/testdb
api.base.url=https://api.staging.example.com
```

---

## Q9. How do you implement data-driven testing in Selenium+TestNG? (CSV + DataProvider)

**Answer:**

```java
// utils/CsvReader.java
package com.framework.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {

    public static Object[][] readCsv(String filePath) {
        List<Object[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                data.add(line.split(","));
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot read CSV: " + filePath, e);
        }
        return data.toArray(new Object[0][]);
    }
}
```

```
// test-data/login-data.csv
username,password,expected
admin@test.com,Admin123,Dashboard
user@test.com,User123,Dashboard
bad@test.com,wrong,Invalid credentials
```

```java
// LoginTest.java
public class LoginTest extends BaseTest {

    @DataProvider(name = "loginData")
    public Object[][] provideLoginData() {
        return CsvReader.readCsv("src/test/resources/test-data/login-data.csv");
    }

    @Test(dataProvider = "loginData")
    public void testLogin(String username, String password, String expected) {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.login(username, password);

        if (expected.equals("Dashboard")) {
            Assert.assertTrue(new DashboardPage(getDriver()).isWelcomeDisplayed(),
                "Expected dashboard for: " + username);
        } else {
            Assert.assertEquals(loginPage.getErrorMessage(), expected);
        }
    }
}
```

---

## Q10. How do you implement cross-browser testing in the framework?

**Answer:**

```xml
<!-- testng.xml with multiple browsers -->
<suite name="CrossBrowserSuite" parallel="tests" thread-count="3">
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
// BaseTest.java — reads browser parameter from testng.xml
@BeforeMethod
@Parameters("browser")
public void setUp(@Optional("chrome") String browser) {
    driver.set(DriverFactory.createDriver(browser));
}
```

For Playwright, cross-browser is configured in playwright.config.ts projects array (see Q3).

**CI override (GitHub Actions):**
```yaml
- name: Run Firefox Tests
  run: mvn test -Dbrowser=firefox -Dheadless=true
```

---

## Q11. How do you implement parallel testing in the framework?

**Answer:**

**TestNG parallel options:**

```xml
<!-- Parallel at method level -->
<suite name="ParallelSuite" parallel="methods" thread-count="4">
    <test name="AllTests">
        <classes>
            <class name="com.tests.LoginTest"/>
            <class name="com.tests.UserTest"/>
        </classes>
    </test>
</suite>
```

**Critical: ThreadLocal for driver isolation**

```java
// Without ThreadLocal, parallel tests share one driver — tests interfere
// With ThreadLocal, each thread gets its own driver instance

protected static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

@BeforeMethod
public void setUp() {
    driver.set(DriverFactory.createDriver("chrome"));
}

@AfterMethod
public void tearDown() {
    driver.get().quit();
    driver.remove(); // MUST remove to prevent thread pool memory leaks
}
```

**Playwright (parallel by default):**
```typescript
// playwright.config.ts
export default defineConfig({
  fullyParallel: true,  // All tests run in parallel
  workers: 4,           // 4 parallel workers
});
```

---

## Q12. How do you implement reporting (Allure + TestNG)?

**Answer:**

**pom.xml dependencies:**

```xml
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-testng</artifactId>
    <version>2.25.0</version>
</dependency>
```

**Test with Allure annotations:**

```java
import io.qameta.allure.*;

@Epic("Authentication")
@Feature("Login")
public class LoginTest extends BaseTest {

    @Test
    @Story("Successful login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify that valid credentials lead to dashboard")
    public void testValidLogin() {
        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboard = loginPage.login("admin", "secret");
        Allure.step("Verify dashboard is shown");
        Assert.assertTrue(dashboard.isWelcomeDisplayed());
    }
}
```

**Generate report:**

```bash
mvn test
allure serve target/allure-results
```

**GitHub Actions:**

```yaml
- name: Generate Allure Report
  uses: simple-elf/allure-report-action@master
  with:
    allure_results: target/allure-results
    allure_report: allure-report
- name: Deploy to GitHub Pages
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: allure-report
```

---

## Q13. How do you add screenshots on failure automatically?

**Answer:**

```java
// TestListener.java implements ITestListener
public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        Object testClass = result.getInstance();
        WebDriver driver = ((BaseTest) testClass).getDriver();

        if (driver instanceof TakesScreenshot) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES);

                // Attach to Allure report
                Allure.addAttachment("Failure Screenshot",
                    "image/png", new ByteArrayInputStream(screenshot), "png");

                // Save to file
                String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String path = "screenshots/" + result.getName() + "_" + timestamp + ".png";
                Files.write(Paths.get(path), screenshot);

            } catch (Exception e) {
                System.err.println("Screenshot failed: " + e.getMessage());
            }
        }
    }
}
```

Register in testng.xml:
```xml
<listeners>
    <listener class-name="com.framework.listeners.TestListener"/>
</listeners>
```

---

## Q14. How do you implement retry logic for flaky tests?

**Answer:**

```java
// RetryAnalyzer.java
package com.framework.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int MAX_RETRIES = 2;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            System.out.println("Retrying test: " + result.getName() +
                               " (attempt " + retryCount + ")");
            return true;
        }
        return false;
    }
}
```

```java
// RetryListener.java — auto-apply to all tests
public class RetryListener implements IAnnotationTransformer {
    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
```

```xml
<!-- testng.xml -->
<listeners>
    <listener class-name="com.framework.listeners.RetryListener"/>
</listeners>
```

For Playwright:
```typescript
// playwright.config.ts
retries: process.env.CI ? 2 : 0,
```

---

## Q15. How do you manage test environments (dev/staging/prod)?

**Answer:**

```properties
# config.dev.properties
base.url=https://dev.example.com
api.url=https://api.dev.example.com

# config.staging.properties
base.url=https://staging.example.com
api.url=https://api.staging.example.com
```

```java
public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        String env = System.getProperty("env", "staging");
        String path = "src/test/resources/config." + env + ".properties";
        try (FileInputStream fis = new FileInputStream(path)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config for env: " + env, e);
        }
    }
}
```

**Run against different environments:**

```bash
mvn test -Denv=staging
mvn test -Denv=dev
```

**GitHub Actions matrix:**

```yaml
strategy:
  matrix:
    environment: [dev, staging]
steps:
  - name: Run Tests
    run: mvn test -Denv=${{ matrix.environment }}
```

---

## Q16. How do you handle authentication in the framework (tokens, sessions)?

**Answer:**

**RestAssured — Bearer Token:**

```java
public class AuthHelper {

    private static String token;

    public static String getToken() {
        if (token == null) {
            token = given()
                .contentType("application/json")
                .body("{ \"email\": \"" + ConfigReader.getProperty("api.user") +
                      "\", \"password\": \"" + ConfigReader.getProperty("api.pass") + "\" }")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .extract().path("token");
        }
        return token; // Cached — only logs in once per test run
    }
}

// In test
given()
    .header("Authorization", "Bearer " + AuthHelper.getToken())
    .spec(requestSpec)
.when()
    .get("/protected-endpoint")
.then()
    .statusCode(200);
```

**Playwright — saved session:**
```typescript
// auth.setup.ts saves storageState once
// All tests reuse it without needing to log in again
```

**Selenium — cookie injection:**
```java
Cookie authCookie = new Cookie("sessionId", AuthHelper.getSessionCookie());
driver.manage().addCookie(authCookie);
driver.navigate().refresh();
```

---

## Q17. How do you implement logging in a test framework?

**Answer:**

```xml
<!-- log4j2.xml in src/test/resources -->
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <File name="LogFile" fileName="logs/test-execution.log" append="false">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} %-5level [%t] - %msg%n"/>
        </File>
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="LogFile"/>
        </Root>
    </Loggers>
</Configuration>
```

```java
// In any class
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginPage {
    private static final Logger log = LogManager.getLogger(LoginPage.class);

    public DashboardPage login(String user, String pass) {
        log.info("Logging in as: {}", user);
        usernameField.sendKeys(user);
        passwordField.sendKeys(pass);
        loginButton.click();
        log.info("Login action completed");
        return new DashboardPage(driver);
    }
}
```

---

## Q18. How do you implement smoke vs regression test grouping?

**Answer:**

```java
public class LoginTest extends BaseTest {

    @Test(groups = {"smoke", "regression"})
    public void testValidLogin() { /* critical path */ }

    @Test(groups = {"regression"})
    public void testLoginWithRememberMe() { /* less critical */ }

    @Test(groups = {"regression"})
    public void testLoginLockedAccount() { /* edge case */ }
}
```

```xml
<!-- smoke-testng.xml -->
<suite name="SmokeSuite">
    <test name="SmokeTests">
        <groups><run><include name="smoke"/></run></groups>
        <classes><class name="com.tests.LoginTest"/></classes>
    </test>
</suite>
```

**Maven command:**
```bash
mvn test -Dgroups=smoke          # Run only smoke
mvn test -Dgroups=regression     # Run full regression
```

**GitHub Actions:**
```yaml
- name: Smoke Tests
  run: mvn test -Dgroups=smoke -Denv=staging
```

---

## Q19. What are the SOLID principles? Which apply to test framework design?

**Answer:**

| Principle | Full Name | Applies in Framework |
|---|---|---|
| S | Single Responsibility | Each class does one thing: Page class handles UI, ConfigReader reads config |
| O | Open/Closed | DriverFactory can be extended for new browsers without changing existing code |
| L | Liskov Substitution | Any driver subclass should work where WebDriver is expected |
| I | Interface Segregation | ITestListener has many methods — implement only what you need |
| D | Dependency Inversion | Tests depend on WebDriver interface, not ChromeDriver directly |

**S — Single Responsibility:**
```java
// BAD: LoginPage that also logs and reads config
// GOOD: LoginPage only has UI interactions, ConfigReader only reads properties
```

**O — Open/Closed:**
```java
// DriverFactory: add SauceLabsDriver support without changing existing cases
case "saucelabs": return new RemoteWebDriver(new URL(SL_URL), caps);
```

**D — Dependency Inversion:**
```java
// Test accepts WebDriver (interface), not ChromeDriver (concrete)
public LoginPage(WebDriver driver) { ... }
```

---

## Q20. How do you prevent code duplication in your framework?

**Answer:**

- **Base classes:** BaseTest, BasePage, BaseApiTest hold common setup
- **Utility classes:** WaitUtils, FileUtils, RandomUtils for helpers
- **Builder pattern for test data:** UserBuilder, OrderBuilder instead of constructor overloads
- **Constants:** URL constants, message constants, timeout constants in dedicated classes
- **Fixtures / DataProviders:** reusable test data rather than inline values
- **Composition:** WaitHelper injected into pages rather than each page re-implementing waits

```java
// WaitUtils.java — used by all page classes
public class WaitUtils {
    public static WebElement waitForVisible(WebDriver driver, By locator, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
```

---

## Q21. How do you handle test data management?

**Answer:**

**Approach 1 — External files (CSV/JSON/Excel):**
```java
@DataProvider(name = "users")
public Object[][] provideUsers() {
    return CsvReader.readCsv("src/test/resources/data/users.csv");
}
```

**Approach 2 — Builder pattern:**
```java
User user = new User.Builder()
    .name("Test User")
    .email("test+" + System.currentTimeMillis() + "@example.com") // unique email
    .role("ADMIN")
    .build();
```

**Approach 3 — API-created test data:**
```java
@BeforeMethod
public void createTestUser() {
    testUser = ApiHelper.createUser("user@test.com", "password123");
}

@AfterMethod
public void deleteTestUser() {
    ApiHelper.deleteUser(testUser.getId());
}
```

**Approach 4 — Database seeding:**
```java
@BeforeClass
public void seedDatabase() {
    DbHelper.runScript("src/test/resources/sql/seed-users.sql");
}
```

---

## Q22. How do you integrate the framework with Jenkins/GitHub Actions?

**Answer:**

**GitHub Actions workflow:**

```yaml
name: Regression Tests

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  schedule:
    - cron: '0 2 * * *' # Nightly at 2am

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Maven
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Run Tests
        run: mvn test -Dheadless=true -Denv=staging
        env:
          TEST_USER: ${{ secrets.TEST_USER }}
          TEST_PASS: ${{ secrets.TEST_PASS }}

      - name: Upload Test Results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: target/surefire-reports/

      - name: Publish Allure Report
        uses: simple-elf/allure-report-action@master
        if: always()
        with:
          allure_results: target/allure-results
```

---

## Q23. What is the difference between framework and tool?

**Answer:**

| Aspect | Tool | Framework |
|---|---|---|
| Definition | Software that performs a specific task | Structure that guides how you build something |
| Control | You control the tool | Framework controls the flow (inversion of control) |
| Examples | Selenium, RestAssured, Playwright | TestNG, JUnit, Cucumber, Serenity |
| Reusability | Used across projects as-is | Custom-built on top of tools for your project |
| Extensibility | Fixed feature set | You extend and customise it |

**Analogy:** Selenium is like a hammer (a tool). Your POM + BaseTest + Listeners + Reports structure is the framework — it tells you where to put the hammer, how to swing it, and how to record results.

---

## Q24. How do you maintain a large test suite (1000+ tests)?

**Answer:**

1. **Groups/Tags:** Categorise tests (smoke/regression/feature-specific) to avoid running everything every time
2. **Parallel execution:** Run tests concurrently to keep execution time manageable
3. **Regular cleanup:** Archive or delete tests for removed features
4. **Dependency management:** No test should depend on another test's state
5. **Flaky test tracking:** Tag flaky tests with `@Ignore` or `flaky` group and fix them in sprints
6. **Modular structure:** Tests organised by feature/module so a team can own their section
7. **Execution time budgets:** Smoke < 10 min, regression < 60 min — if exceeded, optimise or parallelise

```java
// Track flaky tests with JIRA link
@Test(groups = {"flaky"}, description = "QA-1234 - Intermittent timing issue")
public void testCheckoutFlaky() { }
```

---

## Q25. What is test isolation? How do you ensure it?

**Answer:**

Test isolation means each test is completely independent — it sets up its own data, runs, and cleans up. A failing test should never cause another test to fail.

**How to ensure it:**

```java
public class UserTest extends BaseTest {

    private String createdUserId;

    @BeforeMethod
    public void createUser() {
        // Each test creates its own fresh user
        createdUserId = ApiHelper.createUser("test" + System.currentTimeMillis() + "@test.com");
    }

    @AfterMethod
    public void deleteUser() {
        // Each test cleans up after itself
        ApiHelper.deleteUser(createdUserId);
    }

    @Test
    public void testUpdateUser() {
        // Uses own user — not affected by any other test
        ApiHelper.updateUser(createdUserId, "New Name");
        Assert.assertEquals(ApiHelper.getUser(createdUserId).getName(), "New Name");
    }
}
```

**Anti-patterns to avoid:**
- `@BeforeClass` data shared between tests — if one modifies it, others break
- Tests that must run in order (`dependsOnMethods`)
- Hardcoded test data IDs that may not exist in the environment

---

## Q26. How do you version control your automation framework?

**Answer:**

- **Git branching:** `main` (stable), `develop` (integration), `feature/QA-123-login-tests`
- **.gitignore:** exclude screenshots, test results, IDE files, secrets

```gitignore
# .gitignore
screenshots/
target/
allure-results/
.env
*.log
.idea/
```

- **PR-based reviews:** Framework changes go through code review before merging
- **Semantic versioning for framework library:** if shared across projects, publish as Maven artifact
- **Changelogs:** Keep CHANGELOG.md updated with framework changes

```bash
# Good commit messages
git commit -m "feat: add retry mechanism for flaky tests"
git commit -m "fix: ThreadLocal not cleared causing memory leak"
git commit -m "chore: update Playwright to 1.45"
```

---

## Q27. What is the Builder pattern and how to use it for test data?

**Answer:**

Builder pattern creates objects step by step, making test data construction readable and flexible.

```java
public class User {
    private final String name;
    private final String email;
    private final String role;
    private final boolean active;

    private User(Builder builder) {
        this.name   = builder.name;
        this.email  = builder.email;
        this.role   = builder.role;
        this.active = builder.active;
    }

    // Getters...

    public static class Builder {
        private String name  = "Default User";
        private String email = "user@test.com";
        private String role  = "USER";
        private boolean active = true;

        public Builder name(String name)     { this.name = name; return this; }
        public Builder email(String email)   { this.email = email; return this; }
        public Builder role(String role)     { this.role = role; return this; }
        public Builder inactive()            { this.active = false; return this; }
        public User build()                  { return new User(this); }
    }
}

// In tests — highly readable
User adminUser = new User.Builder()
    .name("Admin Kupe")
    .email("admin." + System.currentTimeMillis() + "@test.com")
    .role("ADMIN")
    .build();

User inactiveUser = new User.Builder()
    .inactive()
    .build();
```

---

## Q28. What is the Singleton pattern in test frameworks?

**Answer:**

Singleton ensures a class has only one instance. In test frameworks, used for ConfigReader, Driver manager, or shared resources.

```java
public class ConfigReader {

    private static ConfigReader instance;
    private final Properties properties = new Properties();

    // Private constructor prevents instantiation from outside
    private ConfigReader() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config", e);
        }
    }

    // Thread-safe lazy initialisation
    public static synchronized ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}

// Usage
ConfigReader.getInstance().get("base.url");
```

**Note:** In parallel tests, prefer static initialisation block over Singleton to avoid synchronisation overhead.

---

## Q29. What is the Strategy pattern in test frameworks (e.g. wait strategies)?

**Answer:**

Strategy pattern defines a family of algorithms and makes them interchangeable. Useful for wait strategies, browser strategies, or data reading strategies.

```java
// Strategy interface
public interface WaitStrategy {
    WebElement wait(WebDriver driver, By locator);
}

// Concrete strategies
public class VisibilityWait implements WaitStrategy {
    @Override
    public WebElement wait(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}

public class ClickabilityWait implements WaitStrategy {
    @Override
    public WebElement wait(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.elementToBeClickable(locator));
    }
}

// Context class
public class ElementFinder {
    private WaitStrategy strategy;

    public ElementFinder(WaitStrategy strategy) {
        this.strategy = strategy;
    }

    public WebElement find(WebDriver driver, By locator) {
        return strategy.wait(driver, locator);
    }
}

// Usage in page
WebElement btn = new ElementFinder(new ClickabilityWait())
    .find(driver, By.id("submit"));
```

---

## Q30. How do you design a framework to support both API and UI testing?

**Answer:**

```
hybrid-framework/
├── src/main/java/com/framework/
│   ├── base/
│   │   ├── BaseUiTest.java      # Selenium setup
│   │   └── BaseApiTest.java     # RestAssured setup
│   ├── pages/                   # UI Page Objects
│   ├── api/
│   │   ├── endpoints/           # API endpoint helpers
│   │   └── models/              # Request/Response POJOs
│   ├── config/
│   │   └── ConfigReader.java
│   └── utils/
│       ├── ApiHelper.java       # Setup data via API for UI tests
│       └── DbHelper.java
└── src/test/java/com/tests/
    ├── ui/
    │   └── LoginUiTest.java
    ├── api/
    │   └── LoginApiTest.java
    └── e2e/
        └── CheckoutE2ETest.java  # Uses API + UI combined
```

```java
// E2E test — use API to create data, UI to verify
public class CheckoutE2ETest extends BaseUiTest {

    @Test
    public void testCheckoutFlow() {
        // API: create a product (fast, no UI needed)
        String productId = ApiHelper.createProduct("Laptop", 1200.00);

        // UI: log in and checkout the product
        LoginPage login = new LoginPage(getDriver());
        login.login("user@test.com", "pass");
        new ProductPage(getDriver())
            .openProduct(productId)
            .addToCart()
            .checkout();

        // API: verify the order was created
        Order order = ApiHelper.getLatestOrder("user@test.com");
        Assert.assertEquals(order.getProductId(), productId);

        // API cleanup
        ApiHelper.deleteProduct(productId);
    }
}
```

---

## Q31. How do you handle test execution ordering?

**Answer:**

By default tests should be **independent** (no ordering). But when ordering is needed:

```java
// TestNG — priority (lower = earlier)
@Test(priority = 1)
public void testCreateUser() { }

@Test(priority = 2)
public void testUpdateUser() { }

// TestNG — dependency (use sparingly — creates coupling)
@Test(dependsOnMethods = {"testCreateUser"})
public void testDeleteUser() { }
```

**Better approach — use API setup, not test ordering:**
```java
@BeforeMethod
public void setUp() {
    userId = ApiHelper.createUser(...); // independent setup
}

@AfterMethod
public void tearDown() {
    ApiHelper.deleteUser(userId);       // independent cleanup
}

@Test
public void testDeleteUser() {
    // This test is self-contained — no ordering dependency
}
```

---

## Q32. What is a test utility class? What goes in it?

**Answer:**

```java
public class TestUtils {

    // Generate unique email to avoid conflicts
    public static String uniqueEmail(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "@test.com";
    }

    // Random string of given length
    public static String randomString(int length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    // Wait for a condition with custom message
    public static void waitFor(BooleanSupplier condition, int timeoutSec, String message) {
        long end = System.currentTimeMillis() + (timeoutSec * 1000L);
        while (System.currentTimeMillis() < end) {
            if (condition.getAsBoolean()) return;
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        throw new RuntimeException("Timed out waiting for: " + message);
    }

    // Parse date from string
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // Read file content
    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}
```

---

## Q33. How do you handle sensitive data (passwords, tokens) in the framework?

**Answer:**

**Never hardcode secrets in code:**
```java
// BAD
String password = "Admin123!"; // in committed code

// GOOD — environment variable
String password = System.getenv("TEST_PASSWORD");
```

**Config with env override:**
```java
public static String getProperty(String key) {
    // Environment variable takes highest priority
    String envVal = System.getenv(key.toUpperCase().replace(".", "_"));
    if (envVal != null) return envVal;
    return properties.getProperty(key);
}
```

**GitHub Actions secrets:**
```yaml
env:
  TEST_USER: ${{ secrets.TEST_USER }}
  TEST_PASS: ${{ secrets.TEST_PASS }}
  API_TOKEN: ${{ secrets.API_TOKEN }}
```

**.gitignore:**
```gitignore
.env
config.local.properties
secrets/
```

**Playwright — .env file:**
```
# .env (gitignored)
TEST_USER=admin@example.com
TEST_PASS=secret123
```

---

## Q34. How do you ensure the framework is easy to onboard new team members?

**Answer:**

1. **README.md** with clear setup instructions (Java version, Maven version, how to run)
2. **Code comments** on non-obvious design decisions
3. **Consistent naming conventions** enforced via code review
4. **Working examples** — at least one complete test for each module
5. **Config-first design** — new members only change config, not code, to run tests
6. **Documented test data** — where it comes from, how to create it
7. **Pair testing sessions** — walk a new joiner through their first test
8. **CONTRIBUTING.md** — PR guidelines, commit message format

```markdown
# Quick Start
1. Clone the repo
2. Ensure Java 17 and Maven 3.9+ are installed
3. Copy config.properties.example to config.properties
4. Fill in the credentials (ask your team lead for test env values)
5. Run: mvn test -Dgroups=smoke
6. View results: allure serve target/allure-results
```

---

## Q35. How do you measure framework effectiveness/ROI?

**Answer:**

**Quantitative metrics:**
- Test execution time: manual (4h) vs automated (20 min) = 92% reduction
- Defect detection rate: bugs found by automation before production
- Coverage: % of test cases automated
- Flakiness rate: % tests that pass/fail inconsistently (target < 2%)
- Cost per test run: CI minutes vs manual tester hours

**Qualitative metrics:**
- Team confidence to release
- Onboarding time for new QAs
- Frequency of missed bugs reaching production

**Report to stakeholders:**
- Before: 3 QEs testing manually for 2 days per release
- After: Automated suite runs in 25 minutes in CI
- Saving: ~48 person-hours per release cycle

---

## Q36. What is the difference between test framework and test suite?

**Answer:**

| Aspect | Test Framework | Test Suite |
|---|---|---|
| What it is | The architecture and infrastructure for writing tests | A collection of test cases grouped together |
| Contains | BaseTest, DriverFactory, ConfigReader, Listeners, Utilities | The actual test classes and methods |
| Purpose | Enables writing tests efficiently and consistently | Executes a set of tests for a feature or module |
| Examples | Selenium+TestNG+POM structure | LoginTestSuite, RegressionSuite, SmokeTests |
| Changes | Changed rarely — architectural decisions | Changed frequently as features change |

The framework is the house; the test suite is the furniture inside it.

---

## Q37. How do you implement test hooks (before/after) at different levels?

**Answer:**

```java
// TestNG hook levels
public class OrderTest extends BaseTest {

    @BeforeSuite // Once before ALL tests in the suite
    public void beforeSuite() {
        System.out.println("Suite starting — global setup");
    }

    @BeforeClass // Once before all tests in THIS class
    public void beforeClass() {
        testData = ApiHelper.createTestOrder();
    }

    @BeforeMethod // Before each individual test method
    public void beforeEach() {
        getDriver().get(ConfigReader.getProperty("base.url") + "/orders");
    }

    @AfterMethod // After each test method
    public void afterEach() {
        // Screenshot on failure handled by TestListener
    }

    @AfterClass // After all tests in this class
    public void afterClass() {
        ApiHelper.deleteOrder(testData.getId());
    }

    @AfterSuite // Once after ALL tests complete
    public void afterSuite() {
        System.out.println("Suite complete — generating report");
    }
}
```

**Playwright:**
```typescript
test.beforeAll(async ({ browser }) => { /* once per file */ });
test.beforeEach(async ({ page }) => { /* before each test */ });
test.afterEach(async ({ page }) => { /* after each test */ });
test.afterAll(async () => { /* once per file */ });
```

---

## Q38. How do you handle timeouts consistently across the framework?

**Answer:**

```java
// TimeoutConstants.java
public class TimeoutConstants {
    public static final int IMPLICIT_WAIT     = 10;
    public static final int EXPLICIT_WAIT     = 15;
    public static final int PAGE_LOAD_TIMEOUT = 30;
    public static final int LONG_WAIT         = 60; // For slow operations
}
```

```java
// WaitUtils.java — single source of truth for waits
public class WaitUtils {

    public static WebElement waitForVisible(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(TimeoutConstants.EXPLICIT_WAIT))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitForClickable(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(TimeoutConstants.EXPLICIT_WAIT))
            .until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static void waitForPageLoad(WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(TimeoutConstants.PAGE_LOAD_TIMEOUT))
            .until(d -> ((JavascriptExecutor) d)
                .executeScript("return document.readyState").equals("complete"));
    }
}
```

**Rule:** Never use `Thread.sleep()` in tests. Always use explicit waits.

---

## Q39. What is a test harness?

**Answer:**

A test harness is the complete collection of software and test data required to run tests and produce test results. It includes:

- The test runner (TestNG, JUnit, pytest)
- Test framework infrastructure (BaseTest, DriverFactory, ConfigReader)
- Mock servers or stubs (WireMock, MockServer)
- Test data setup/teardown mechanisms
- Reporting mechanisms (Allure, Extent)
- CI pipeline configuration (GitHub Actions, Jenkins)

The term is often used interchangeably with "test framework" but technically refers to the broader execution environment.

```
Test Harness
├── Test Runner          (TestNG)
├── Framework code       (BaseTest, POM, utilities)
├── Test data            (CSV, JSON, API-created)
├── Environment config   (config.properties per env)
├── Stubs/Mocks          (WireMock for external services)
└── Reporting            (Allure + CI integration)
```

---

## Q40. Walk through how you would onboard a new QA engineer to your framework.

**Answer:**

**Day 1 — Overview:**
- Walk through the folder structure and explain what each layer does
- Show the class hierarchy: BaseTest → TestClass, BasePage → PageClass
- Run the smoke suite together so they see a green build
- Explain how config, DriverFactory, and TestNG are connected

**Day 2 — Write their first test:**
- Assign a simple test scenario (e.g., verify page title)
- They write the page class and test class with guidance
- Review their PR together — focus on naming, structure, and assertions

**Day 3 — Data and environment:**
- Show how to read test data from CSV/DataProvider
- Explain how to switch environments with `-Denv=staging`
- Show how GitHub Actions runs the tests

**Resources to share:**
- README.md with setup steps
- An example test that covers: setup, action, assertion, cleanup
- The CONTRIBUTING.md with PR guidelines
- Allure report access so they can see results visually

**First week goal:** New QA writes and merges their first automation test PR with minimal guidance.
