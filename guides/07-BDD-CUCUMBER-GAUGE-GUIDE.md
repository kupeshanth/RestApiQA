# BDD + Cucumber + Gauge — Complete Guide | Gherkin + Serenity + Step Definitions

---

## Table of Contents

1. [What is BDD and Why It Matters](#1-what-is-bdd-and-why-it-matters)
2. [Gherkin Syntax — Complete Reference](#2-gherkin-syntax--complete-reference)
3. [Complete .feature File Examples](#3-complete-feature-file-examples)
4. [Step Definitions in Java](#4-step-definitions-in-java)
5. [Hooks — @Before and @After](#5-hooks--before-and-after)
6. [Cucumber Project Setup](#6-cucumber-project-setup)
7. [Tags — Running by Tag](#7-tags--running-by-tag)
8. [Serenity BDD](#8-serenity-bdd)
9. [Gauge Framework](#9-gauge-framework)
10. [Page Objects with BDD](#10-page-objects-with-bdd)
11. [Reporting](#11-reporting)
12. [All Maven Commands for BDD](#12-all-maven-commands-for-bdd)
13. [Troubleshooting](#13-troubleshooting)
14. [Interview Q&A](#14-interview-qa)

---

## 1. What is BDD and Why It Matters

**BDD (Behaviour-Driven Development)** is an Agile development practice that bridges the communication gap between:
- Business stakeholders (Product Owners, Business Analysts)
- Developers
- QA Engineers

**Core idea:** Express system behaviour as human-readable scenarios written before the code is built. These scenarios become both documentation and executable tests.

**Why BDD matters:**

| Without BDD | With BDD |
|---|---|
| Requirements misunderstood by devs | Requirements expressed as concrete examples everyone agrees on |
| Tests written after code, often incomplete | Tests defined before development — drive implementation |
| QA speaks a different language than business | One shared language: Gherkin |
| Living documentation does not exist | Feature files ARE the living documentation |
| Regression value unclear | Every feature file maps to business capability |

**The Three Amigos meeting:**
Before a story is developed, the Product Owner, Developer, and QA Engineer meet to:
1. Define exactly what "done" looks like using examples
2. Write the Gherkin scenarios together
3. Agree on edge cases

The resulting feature files become the acceptance criteria.

**BDD is not just test automation** — it is a collaborative process. Cucumber is the tool that allows those agreed-upon scenarios to be executed as automated tests.

---

## 2. Gherkin Syntax — Complete Reference

Gherkin is the domain-specific language used to write BDD scenarios. It is structured, readable, and language-independent.

### Keywords

| Keyword | Purpose |
|---|---|
| `Feature` | Name and description of the feature being tested |
| `Background` | Steps that run before every scenario in the Feature |
| `Scenario` | A single concrete example of behaviour |
| `Scenario Outline` | A template with variables, run once per row in Examples |
| `Examples` | Data table for Scenario Outline |
| `Given` | Precondition / initial context |
| `When` | The action / event |
| `Then` | Expected outcome / assertion |
| `And` | Continuation of the previous keyword (same meaning) |
| `But` | Negative continuation (same meaning as And) |
| `@tag` | Tag for filtering and grouping scenarios |
| `#` | Comment (line starts with #) |
| `"""` | Multiline string (docstring) |
| `\|` | Data table delimiter |

### Step Keyword Rules

- `Given` = Arrange (set up preconditions)
- `When` = Act (perform the action being tested)
- `Then` = Assert (verify the expected result)
- `And` / `But` = inherit the type of the step above them (no functional difference; used for readability)

```gherkin
Given the user is on the login page          # First Given
And the login form is empty                  # Another Given (And inherits Given)
When the user enters valid credentials       # First When
And the user clicks the Login button         # Another When (And inherits When)
Then the dashboard page is displayed         # First Then
And the welcome message shows "Hello, John!" # Another Then (And inherits Then)
But the login form is no longer visible      # Negative Then (But inherits Then)
```

### Background

`Background` runs before every scenario in the feature file. Use it for common setup steps to avoid repetition.

```gherkin
Feature: Shopping Cart

  Background:
    Given the user "john@example.com" is logged in
    And the shopping cart is empty

  Scenario: Add a product to cart
    When the user adds "Laptop" to the cart
    Then the cart shows 1 item
    And the cart total is "$999.99"

  Scenario: Remove a product from cart
    Given the product "Laptop" is in the cart
    When the user removes "Laptop" from the cart
    Then the cart is empty
```

### Scenario Outline with Examples

Runs the same scenario multiple times with different data values.

```gherkin
Scenario Outline: Login with multiple user types
  Given the user is on the login page
  When the user logs in with email "<email>" and password "<password>"
  Then the user should be redirected to "<expected_page>"
  And the page title should be "<page_title>"

  Examples:
    | email                  | password     | expected_page | page_title       |
    | admin@example.com      | Admin123!    | /admin        | Admin Dashboard  |
    | manager@example.com    | Manager456!  | /manager      | Manager Portal   |
    | employee@example.com   | Emp789!      | /dashboard    | My Dashboard     |
    | readonly@example.com   | Read000!     | /dashboard    | My Dashboard     |
```

### Data Tables

Data tables pass structured data to a single step. Unlike `Examples`, data tables do not create multiple scenario runs.

```gherkin
Scenario: Register a new user with all required fields
  Given the user is on the registration page
  When the user fills in the registration form with:
    | Field         | Value                |
    | First Name    | John                 |
    | Last Name     | Doe                  |
    | Email         | john.doe@example.com |
    | Phone         | +1-555-123-4567      |
    | Date of Birth | 1990-05-15           |
    | Country       | United States        |
  And the user submits the registration form
  Then a confirmation email is sent to "john.doe@example.com"

Scenario: Create multiple products at once
  Given the admin is on the product management page
  When the admin creates the following products:
    | Name       | Price   | Category    | Stock |
    | Laptop Pro | $999.99 | Electronics | 50    |
    | Wireless Mouse | $29.99 | Accessories | 200  |
    | USB Hub    | $49.99  | Accessories | 150   |
  Then all 3 products appear in the product list
```

### Tags

Tags group, filter, and annotate scenarios.

```gherkin
@smoke @login @critical
Feature: User Authentication

  @smoke @happy-path
  Scenario: Successful login with valid credentials
    ...

  @regression @negative
  Scenario: Login fails with wrong password
    ...

  @wip @skip
  Scenario: Login with SSO (not yet implemented)
    ...
```

### Docstrings (Multiline strings)

```gherkin
Scenario: Submit a contact form with a long message
  Given the user is on the contact page
  When the user submits the form with the message:
    """
    Hello Support Team,

    I am writing to report an issue with my account.
    My account number is 12345 and I cannot log in.

    Please help me resolve this at your earliest convenience.

    Thank you,
    John Doe
    """
  Then the support ticket is created with the full message
```

---

## 3. Complete .feature File Examples

### login.feature

```gherkin
@login @authentication
Feature: User Login
  As a registered user
  I want to log in to the application
  So that I can access my personalised dashboard

  Background:
    Given the login page is open
    And no user is currently logged in

  @smoke @happy-path
  Scenario: Successful login with valid credentials
    When the user enters email "john.doe@example.com"
    And the user enters password "SecurePass123!"
    And the user clicks the "Sign In" button
    Then the user is redirected to the dashboard
    And the welcome message displays "Welcome back, John!"
    And the URL contains "/dashboard"

  @regression @negative
  Scenario: Login fails with incorrect password
    When the user enters email "john.doe@example.com"
    And the user enters password "wrongpassword"
    And the user clicks the "Sign In" button
    Then an error message "Invalid credentials. Please try again." is displayed
    And the user remains on the login page
    And the password field is cleared

  @regression @negative
  Scenario: Login fails with non-existent email
    When the user enters email "nobody@nowhere.com"
    And the user enters password "AnyPassword123!"
    And the user clicks the "Sign In" button
    Then an error message "Invalid credentials. Please try again." is displayed
    # Same error as wrong password — do not reveal which field is wrong (security)

  @regression @validation
  Scenario: Login button is disabled when email is empty
    When the user enters password "SecurePass123!"
    And the email field is empty
    Then the "Sign In" button is disabled

  @regression @validation
  Scenario Outline: Login validates email format
    When the user enters email "<invalid_email>"
    And the user enters password "SecurePass123!"
    Then the email validation error "<error_message>" is displayed

    Examples:
      | invalid_email     | error_message                    |
      | notanemail        | Please enter a valid email address |
      | @nodomain.com     | Please enter a valid email address |
      | missing@          | Please enter a valid email address |
      | spaces in@email.com | Please enter a valid email address |

  @regression @security
  Scenario: Account is locked after 5 failed attempts
    When the user attempts to login with wrong password 5 times
    Then the account is locked
    And the error message "Your account has been locked. Please contact support." is displayed
    And a lockout email is sent to "john.doe@example.com"

  @regression
  Scenario: Forgot password link navigates to reset page
    When the user clicks the "Forgot Password?" link
    Then the user is redirected to the password reset page
    And the URL contains "/forgot-password"

  @regression
  Scenario: Successful logout
    Given the user is logged in as "john.doe@example.com"
    When the user clicks the "Logout" button
    Then the user is redirected to the login page
    And the session is terminated
    And navigating to "/dashboard" redirects back to the login page
```

---

## 4. Step Definitions in Java

### LoginStepDefinitions.java

```java
// src/test/java/com/example/stepdefs/LoginStepDefinitions.java
package com.example.stepdefs;

import com.example.pages.LoginPage;
import com.example.pages.DashboardPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

// Cucumber creates a new instance of this class for each scenario
public class LoginStepDefinitions {

    // Injected via dependency injection (PicoContainer)
    private final WebDriver driver;
    private final LoginPage loginPage;
    private DashboardPage dashboardPage;

    // Constructor injection — PicoContainer or Cucumber Spring provides the driver
    public LoginStepDefinitions(WebDriver driver) {
        this.driver = driver;
        this.loginPage = new LoginPage(driver);
    }

    // ── Given Steps ──────────────────────────────────────────────────────────

    @Given("the login page is open")
    public void theLoginPageIsOpen() {
        loginPage.open();
    }

    @Given("no user is currently logged in")
    public void noUserIsCurrentlyLoggedIn() {
        // Clear cookies/session
        driver.manage().deleteAllCookies();
    }

    @Given("the user is logged in as {string}")
    public void theUserIsLoggedInAs(String email) {
        loginPage.open();
        loginPage.loginWith(email, "SecurePass123!");
        dashboardPage = new DashboardPage(driver);
        dashboardPage.verifyPageLoaded();
    }

    // ── When Steps ───────────────────────────────────────────────────────────

    @When("the user enters email {string}")
    public void theUserEntersEmail(String email) {
        loginPage.fillEmail(email);
    }

    @When("the user enters password {string}")
    public void theUserEntersPassword(String password) {
        loginPage.fillPassword(password);
    }

    @When("the user clicks the {string} button")
    public void theUserClicksTheButton(String buttonName) {
        loginPage.clickButton(buttonName);
    }

    @When("the email field is empty")
    public void theEmailFieldIsEmpty() {
        loginPage.clearEmail();
    }

    @When("the user attempts to login with wrong password {int} times")
    public void theUserAttemptsToLoginWithWrongPasswordTimes(int times) {
        for (int i = 0; i < times; i++) {
            loginPage.open();
            loginPage.fillEmail("john.doe@example.com");
            loginPage.fillPassword("wrongpassword" + i);
            loginPage.clickButton("Sign In");
        }
    }

    @When("the user clicks the {string} link")
    public void theUserClicksTheLink(String linkText) {
        loginPage.clickLink(linkText);
    }

    // ── Then Steps ───────────────────────────────────────────────────────────

    @Then("the user is redirected to the dashboard")
    public void theUserIsRedirectedToTheDashboard() {
        dashboardPage = new DashboardPage(driver);
        dashboardPage.verifyPageLoaded();
    }

    @Then("the welcome message displays {string}")
    public void theWelcomeMessageDisplays(String expectedMessage) {
        String actualMessage = dashboardPage.getWelcomeMessage();
        Assert.assertEquals(actualMessage, expectedMessage,
            "Welcome message does not match expected value");
    }

    @Then("the URL contains {string}")
    public void theURLContains(String urlFragment) {
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains(urlFragment),
            "URL '" + currentUrl + "' does not contain '" + urlFragment + "'");
    }

    @Then("an error message {string} is displayed")
    public void anErrorMessageIsDisplayed(String expectedError) {
        String actualError = loginPage.getErrorMessage();
        Assert.assertEquals(actualError, expectedError,
            "Error message does not match");
    }

    @Then("the user remains on the login page")
    public void theUserRemainsOnTheLoginPage() {
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/login"),
            "User should remain on login page but is at: " + currentUrl);
    }

    @Then("the password field is cleared")
    public void thePasswordFieldIsCleared() {
        String passwordValue = loginPage.getPasswordFieldValue();
        Assert.assertEquals(passwordValue, "",
            "Password field should be cleared after failed login");
    }

    @Then("the {string} button is disabled")
    public void theButtonIsDisabled(String buttonName) {
        Assert.assertFalse(loginPage.isButtonEnabled(buttonName),
            "Button '" + buttonName + "' should be disabled");
    }

    @Then("the email validation error {string} is displayed")
    public void theEmailValidationErrorIsDisplayed(String expectedError) {
        String actualError = loginPage.getEmailValidationError();
        Assert.assertEquals(actualError, expectedError);
    }

    @Then("the account is locked")
    public void theAccountIsLocked() {
        Assert.assertTrue(loginPage.isAccountLockMessageVisible(),
            "Account locked message should be visible");
    }

    @Then("a lockout email is sent to {string}")
    public void aLockoutEmailIsSentTo(String email) {
        // In real test: check email server (MailHog, Mailinator, etc.)
        // Simplified: assert an API call was made
        Assert.assertTrue(loginPage.wasLockoutEmailSent(email),
            "Lockout email was not sent to " + email);
    }

    @Then("the user is redirected to the password reset page")
    public void theUserIsRedirectedToPasswordResetPage() {
        Assert.assertTrue(driver.getCurrentUrl().contains("/forgot-password"));
    }

    @Then("the session is terminated")
    public void theSessionIsTerminated() {
        // Verify session cookie is removed
        Assert.assertNull(driver.manage().getCookieNamed("session_token"),
            "Session token cookie should be removed after logout");
    }

    @Then("navigating to {string} redirects back to the login page")
    public void navigatingToRedirectsBackToLoginPage(String path) {
        driver.navigate().to(driver.getCurrentUrl().replaceAll("/[^/]*$", path));
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
            "Should redirect to login page but is at: " + driver.getCurrentUrl());
    }
}
```

### ProductStepDefinitions.java (with Data Table)

```java
// src/test/java/com/example/stepdefs/ProductStepDefinitions.java
package com.example.stepdefs;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

public class ProductStepDefinitions {

    @When("the admin creates the following products:")
    public void theAdminCreatesTheFollowingProducts(DataTable dataTable) {
        // DataTable as list of maps (each row is a Map<columnName, value>)
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> product : products) {
            String name = product.get("Name");
            String price = product.get("Price");
            String category = product.get("Category");
            String stock = product.get("Stock");

            // Call product creation service/page object
            System.out.printf("Creating product: %s at %s in %s with stock %s%n",
                name, price, category, stock);
            // productPage.createProduct(name, price, category, Integer.parseInt(stock));
        }
    }

    @When("the user fills in the registration form with:")
    public void theUserFillsInTheRegistrationFormWith(DataTable dataTable) {
        // DataTable as list of lists (raw rows including header)
        List<List<String>> rows = dataTable.asLists(String.class);

        for (List<String> row : rows) {
            String field = row.get(0);   // "First Name"
            String value = row.get(1);   // "John"
            System.out.printf("Filling field '%s' with value '%s'%n", field, value);
            // registrationPage.fillField(field, value);
        }
    }

    @Then("all {int} products appear in the product list")
    public void allProductsAppearInTheProductList(int expectedCount) {
        // int productCount = productPage.getProductCount();
        int productCount = expectedCount; // placeholder
        Assert.assertEquals(productCount, expectedCount,
            "Product count in list does not match expected count");
    }
}
```

---

## 5. Hooks — @Before and @After

Hooks run setup and teardown code at specific points in the Cucumber lifecycle.

```java
// src/test/java/com/example/hooks/TestHooks.java
package com.example.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class TestHooks {

    private WebDriver driver;

    // ── @Before ───────────────────────────────────────────────────────────────

    // Runs before EVERY scenario
    @Before
    public void setUp(Scenario scenario) {
        System.out.println("Starting scenario: " + scenario.getName());

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1280,720");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    // Runs before scenarios tagged with @authenticated
    @Before("@authenticated")
    public void setUpAuthenticated(Scenario scenario) {
        System.out.println("Setting up authenticated session for: " + scenario.getName());
        // Load saved auth state / cookies
        // cookieManager.loadSessionCookies(driver, "auth/session.json");
    }

    // Order parameter: lower number runs first
    @Before(order = 1)
    public void logScenarioStart(Scenario scenario) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("TEST: " + scenario.getName());
        System.out.println("TAGS: " + scenario.getSourceTagNames());
        System.out.println("═══════════════════════════════════════");
    }

    // ── @After ────────────────────────────────────────────────────────────────

    // Runs after EVERY scenario
    @After
    public void tearDown(Scenario scenario) {
        // Take screenshot on failure and embed in report
        if (scenario.isFailed()) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                scenario.attach(screenshot, "image/png", "Failure Screenshot");
                System.out.println("Screenshot taken for failed scenario: " + scenario.getName());
            } catch (Exception e) {
                System.out.println("Failed to take screenshot: " + e.getMessage());
            }
        }

        // Log result
        System.out.println("RESULT: " + (scenario.isFailed() ? "FAILED" : "PASSED"));

        // Always quit the driver
        if (driver != null) {
            driver.quit();
        }
    }

    // Runs after scenarios tagged with @db-cleanup
    @After("@db-cleanup")
    public void cleanUpDatabase(Scenario scenario) {
        System.out.println("Cleaning up test data in database for: " + scenario.getName());
        // dbCleaner.cleanTestData();
    }

    // ── @BeforeStep / @AfterStep ──────────────────────────────────────────────

    @BeforeStep
    public void beforeEachStep(Scenario scenario) {
        // Runs before every step in every scenario
        // Useful for logging, but adds overhead — use sparingly
    }

    @AfterStep
    public void afterEachStep(Scenario scenario) {
        // Take screenshot after every step (for trace reports)
        // Only enable when debugging
    }
}
```

---

## 6. Cucumber Project Setup

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>bdd-cucumber-tests</artifactId>
  <version>1.0.0</version>

  <properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <cucumber.version>7.14.0</cucumber.version>
    <selenium.version>4.15.0</selenium.version>
    <testng.version>7.8.0</testng.version>
  </properties>

  <dependencies>

    <!-- Cucumber Core -->
    <dependency>
      <groupId>io.cucumber</groupId>
      <artifactId>cucumber-java</artifactId>
      <version>${cucumber.version}</version>
      <scope>test</scope>
    </dependency>

    <!-- Cucumber + TestNG integration -->
    <dependency>
      <groupId>io.cucumber</groupId>
      <artifactId>cucumber-testng</artifactId>
      <version>${cucumber.version}</version>
      <scope>test</scope>
    </dependency>

    <!-- PicoContainer for dependency injection in step definitions -->
    <dependency>
      <groupId>io.cucumber</groupId>
      <artifactId>cucumber-picocontainer</artifactId>
      <version>${cucumber.version}</version>
      <scope>test</scope>
    </dependency>

    <!-- Selenium WebDriver -->
    <dependency>
      <groupId>org.seleniumhq.selenium</groupId>
      <artifactId>selenium-java</artifactId>
      <version>${selenium.version}</version>
    </dependency>

    <!-- WebDriverManager — auto-downloads browser drivers -->
    <dependency>
      <groupId>io.github.bonigarcia</groupId>
      <artifactId>webdrivermanager</artifactId>
      <version>5.6.3</version>
    </dependency>

    <!-- TestNG -->
    <dependency>
      <groupId>org.testng</groupId>
      <artifactId>testng</artifactId>
      <version>${testng.version}</version>
      <scope>test</scope>
    </dependency>

    <!-- Cucumber Reporting (HTML reports) -->
    <dependency>
      <groupId>net.masterthought</groupId>
      <artifactId>cucumber-reporting</artifactId>
      <version>5.7.7</version>
    </dependency>

  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.1</version>
        <configuration>
          <suiteXmlFiles>
            <suiteXmlFile>testng.xml</suiteXmlFile>
          </suiteXmlFiles>
          <!-- Pass system properties to tests -->
          <systemPropertyVariables>
            <cucumber.filter.tags>${cucumber.filter.tags}</cucumber.filter.tags>
            <browser>${browser}</browser>
            <env>${env}</env>
          </systemPropertyVariables>
        </configuration>
      </plugin>
    </plugins>
  </build>

</project>
```

### CucumberRunner.java

```java
// src/test/java/com/example/runner/CucumberRunner.java
package com.example.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    // Location of .feature files
    features = "src/test/resources/features",

    // Location of step definition classes
    glue = {
        "com.example.stepdefs",
        "com.example.hooks"
    },

    // Reporters
    plugin = {
        "pretty",                                         // Console output — coloured
        "html:target/cucumber-reports/cucumber.html",    // HTML report
        "json:target/cucumber-reports/cucumber.json",    // JSON (for CI integrations)
        "junit:target/cucumber-reports/cucumber.xml",    // JUnit XML (for Jenkins)
        "rerun:target/failed-scenarios.txt",             // Save failed scenarios for rerun
    },

    // Show step arguments in reports
    monochrome = true,

    // Publish to Cucumber Reports cloud (optional)
    publish = false,

    // Tags to run (overridden via Maven -Dcucumber.filter.tags)
    tags = "not @skip and not @wip"
)
public class CucumberRunner extends AbstractTestNGCucumberTests {

    // Override to enable parallel scenario execution
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

### testng.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="BDD Cucumber Suite" verbose="1" parallel="tests" thread-count="3">

  <test name="Smoke Tests">
    <classes>
      <class name="com.example.runner.CucumberRunner"/>
    </classes>
  </test>

</suite>
```

### Project Structure

```
src/
├── test/
│   ├── java/
│   │   └── com/example/
│   │       ├── runner/
│   │       │   └── CucumberRunner.java
│   │       ├── stepdefs/
│   │       │   ├── LoginStepDefinitions.java
│   │       │   ├── ProductStepDefinitions.java
│   │       │   └── CommonStepDefinitions.java
│   │       ├── hooks/
│   │       │   └── TestHooks.java
│   │       ├── pages/
│   │       │   ├── BasePage.java
│   │       │   ├── LoginPage.java
│   │       │   └── DashboardPage.java
│   │       └── utils/
│   │           ├── DriverFactory.java
│   │           └── ConfigReader.java
│   └── resources/
│       └── features/
│           ├── login.feature
│           ├── products.feature
│           └── checkout.feature
pom.xml
testng.xml
```

---

## 7. Tags — Running by Tag

```bash
# Run only @smoke scenarios
mvn test -Dcucumber.filter.tags="@smoke"

# Run @smoke AND @login scenarios (both tags must be present)
mvn test -Dcucumber.filter.tags="@smoke and @login"

# Run @smoke OR @regression scenarios
mvn test -Dcucumber.filter.tags="@smoke or @regression"

# Exclude @wip scenarios
mvn test -Dcucumber.filter.tags="not @wip"

# Complex: run @regression but not @slow
mvn test -Dcucumber.filter.tags="@regression and not @slow"

# Run @smoke or @critical, but not @skip
mvn test -Dcucumber.filter.tags="(@smoke or @critical) and not @skip"
```

**Tagging conventions:**
```gherkin
@smoke          # Fast, critical path tests (< 5 minutes total)
@regression     # Full regression suite
@sanity         # Minimal sanity after deployment
@critical       # Business-critical scenarios
@happy-path     # Positive scenarios only
@negative       # Error/failure scenarios
@validation     # Input validation scenarios
@security       # Security-related scenarios
@performance    # Performance scenarios
@wip            # Work in progress — do not run in CI
@skip           # Intentionally skipped (document reason in scenario comment)
@flaky          # Known flaky — run in isolation or with retry
@api            # API-level tests
@ui             # UI-level tests
@db-cleanup     # Requires database cleanup after
```

---

## 8. Serenity BDD

Serenity BDD (formerly Thucydides) is a library that extends Cucumber (and JUnit) with:
- Rich **living documentation** reports (not just pass/fail — shows intent)
- **@Steps** pattern for composable, loggable action layers
- Automatic **screenshot capture** at each step
- Integration with JIRA and requirement management tools
- Test narrative in plain English in reports

### What Serenity Adds Over Vanilla Cucumber

| Feature | Cucumber | Serenity + Cucumber |
|---|---|---|
| Reports | HTML table of scenarios | Rich narrative with screenshots, timing, requirements coverage |
| Screenshot | Only on failure (with hook) | Every step, automatically |
| Step logging | System.out | First-class in report with step descriptions |
| Requirements tracking | None | Links scenarios to features/epics |
| Living documentation | Requires extra plugin | Built-in |

### pom.xml with Serenity

```xml
<properties>
  <serenity.version>4.1.4</serenity.version>
</properties>

<dependencies>
  <dependency>
    <groupId>net.serenity-bdd</groupId>
    <artifactId>serenity-core</artifactId>
    <version>${serenity.version}</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>net.serenity-bdd</groupId>
    <artifactId>serenity-cucumber</artifactId>
    <version>${serenity.version}</version>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>net.serenity-bdd</groupId>
    <artifactId>serenity-junit5</artifactId>
    <version>${serenity.version}</version>
    <scope>test</scope>
  </dependency>
</dependencies>

<build>
  <plugins>
    <plugin>
      <groupId>net.serenity-bdd.maven.plugins</groupId>
      <artifactId>serenity-maven-plugin</artifactId>
      <version>${serenity.version}</version>
      <executions>
        <execution>
          <id>serenity-reports</id>
          <phase>post-integration-test</phase>
          <goals>
            <goal>aggregate</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### @Steps in Serenity

`@Steps` is the Serenity pattern for composable action layers. Unlike raw Page Objects, `@Steps` classes are logged in reports with their step names.

```java
// src/test/java/com/example/steps/LoginSteps.java
package com.example.steps;

import com.example.pages.LoginPage;
import com.example.pages.DashboardPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.UIInteractionSteps;

// Extends UIInteractionSteps to get Serenity's driver management
public class LoginSteps extends UIInteractionSteps {

    LoginPage loginPage;        // Serenity injects page objects automatically
    DashboardPage dashboardPage;

    // @Step annotation: the method name appears in Serenity reports
    // {0} is replaced by the first parameter in the report
    @Step("Open the login page")
    public void openLoginPage() {
        loginPage.open();
    }

    @Step("{0} logs in with password {1}")
    public void loginWith(String email, String password) {
        loginPage.fillEmail(email);
        loginPage.fillPassword(password);
        loginPage.clickLoginButton();
    }

    @Step("Verify user is on the dashboard")
    public void verifyOnDashboard() {
        dashboardPage.verifyPageLoaded();
    }

    @Step("Verify welcome message is {0}")
    public void verifyWelcomeMessage(String expectedMessage) {
        String actualMessage = dashboardPage.getWelcomeMessage();
        assert actualMessage.equals(expectedMessage) :
            "Expected: " + expectedMessage + " but got: " + actualMessage;
    }
}
```

```java
// src/test/java/com/example/stepdefs/LoginStepDefinitions.java (Serenity version)
package com.example.stepdefs;

import com.example.steps.LoginSteps;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.annotations.Steps;

public class LoginStepDefinitions {

    // @Steps tells Serenity to inject and proxy this field
    // The proxy intercepts calls and logs them in reports
    @Steps
    LoginSteps loginSteps;

    @Given("the login page is open")
    public void theLoginPageIsOpen() {
        loginSteps.openLoginPage();
    }

    @When("the user logs in with email {string} and password {string}")
    public void theUserLogsIn(String email, String password) {
        loginSteps.loginWith(email, password);
    }

    @Then("the user is on the dashboard")
    public void theUserIsOnTheDashboard() {
        loginSteps.verifyOnDashboard();
    }

    @Then("the welcome message is {string}")
    public void theWelcomeMessageIs(String expectedMessage) {
        loginSteps.verifyWelcomeMessage(expectedMessage);
    }
}
```

### SerenityRunner.java

```java
// src/test/java/com/example/runner/SerenityRunner.java
package com.example.runner;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.example.stepdefs", "com.example.hooks"},
    plugin = {"pretty"},
    tags = "not @skip"
)
public class SerenityRunner {
    // No body needed — annotations drive everything
}
```

**Generate Serenity report:**
```bash
mvn clean verify -Dcucumber.filter.tags="@smoke"
# Report generated at: target/site/serenity/index.html
```

---

## 9. Gauge Framework

### What is Gauge?

Gauge is an open-source test automation framework by ThoughtWorks that uses **Markdown** for test specifications instead of Gherkin. Key differences from Cucumber:

| Aspect | Cucumber | Gauge |
|---|---|---|
| Spec format | Gherkin (.feature) | Markdown (.spec) |
| Keywords | Given/When/Then | Free-form step text |
| Concept reuse | Background + Hooks | Concepts (.cpt files) |
| Step binding | Annotations (@Given etc.) | @Step annotation only |
| Reports | Cucumber HTML / Allure | Built-in HTML report |
| Data storage | Scenario context (manual) | DataStore (built-in) |
| Step autocompletion | IDE plugin | Built-in with gauge CLI |

### Installing Gauge

```bash
# Install Gauge CLI
npm install -g @getgauge/cli  # via npm
# or
brew install gauge             # macOS

# Install Java plugin
gauge install java

# Initialize a new Java project
gauge init java

# Verify
gauge version
```

### .spec File (Markdown format)

```markdown
# Login Feature
Tags: login, authentication

This spec covers user login scenarios for the MyApp application.

## Successful Login with Valid Credentials
Tags: smoke, happy-path

* The login page is open
* The user enters email "john.doe@example.com"
* The user enters password "SecurePass123!"
* The user clicks the "Sign In" button
* The user is redirected to the dashboard
* The welcome message displays "Welcome back, John!"

## Login Fails with Incorrect Password
Tags: regression, negative

* The login page is open
* The user enters email "john.doe@example.com"
* The user enters password "wrongpassword"
* The user clicks the "Sign In" button
* An error message "Invalid credentials" is displayed

## Login with Multiple Users <table:users.csv>
Tags: data-driven

* The login page is open
* The user logs in with email <email> and password <password>
* The user is redirected to <redirect_page>
```

### users.csv (data table for Gauge)

```csv
email,password,redirect_page
admin@example.com,Admin123!,/admin
manager@example.com,Manager456!,/manager
employee@example.com,Emp789!,/dashboard
```

### Step Implementation in Java (Gauge)

```java
// src/test/java/com/example/steps/LoginStepImpl.java
package com.example.steps;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.AfterScenario;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;

public class LoginStepImpl {

    private WebDriver driver;
    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    @BeforeScenario
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        loginPage = new LoginPage(driver);
    }

    @AfterScenario
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // @Step maps to text in .spec files — no Given/When/Then keywords
    @Step("The login page is open")
    public void openLoginPage() {
        loginPage.open();
    }

    @Step("The user enters email <email>")
    public void enterEmail(String email) {
        loginPage.fillEmail(email);
    }

    @Step("The user enters password <password>")
    public void enterPassword(String password) {
        loginPage.fillPassword(password);
    }

    @Step("The user clicks the <buttonName> button")
    public void clickButton(String buttonName) {
        loginPage.clickButton(buttonName);
    }

    @Step("The user is redirected to the dashboard")
    public void verifyDashboard() {
        dashboardPage = new DashboardPage(driver);
        dashboardPage.verifyPageLoaded();
    }

    @Step("The welcome message displays <message>")
    public void verifyWelcomeMessage(String expectedMessage) {
        String actual = dashboardPage.getWelcomeMessage();
        Assert.assertEquals(actual, expectedMessage);
    }

    @Step("An error message <message> is displayed")
    public void verifyErrorMessage(String expectedMessage) {
        String actual = loginPage.getErrorMessage();
        Assert.assertTrue(actual.contains(expectedMessage),
            "Expected error containing '" + expectedMessage + "' but got: " + actual);
    }

    // Combined step — shortcut
    @Step("The user logs in with email <email> and password <password>")
    public void loginWithCredentials(String email, String password) {
        loginPage.fillEmail(email);
        loginPage.fillPassword(password);
        loginPage.clickButton("Sign In");
    }
}
```

### Concepts in Gauge (.cpt files)

Concepts are reusable sequences of steps — similar to Background but reusable across specs.

```markdown
<!-- specs/concepts/login.cpt -->
# Log in as a valid user
* The login page is open
* The user enters email "john.doe@example.com"
* The user enters password "SecurePass123!"
* The user clicks the "Sign In" button
* The user is redirected to the dashboard
```

**Using the concept in a spec:**
```markdown
## Add item to cart
* Log in as a valid user        ← Expands to all 5 steps in the concept
* Navigate to the Products page
* Add "Laptop Pro" to the cart
* The cart shows 1 item
```

### Running Gauge Tests

```bash
# Run all specs
gauge run specs/

# Run a specific spec file
gauge run specs/login.spec

# Run by tag
gauge run --tags="smoke" specs/

# Run by multiple tags (AND)
gauge run --tags="smoke,regression" specs/

# Run in parallel (4 threads)
gauge run --parallel --n=4 specs/

# Run with environment
gauge run --env=staging specs/

# Generate HTML report
gauge run specs/ --html-report-dir=reports/

# Run with repeat (rerun failed only)
gauge run --failed specs/

# List all specs
gauge list --specs
```

---

## 10. Page Objects with BDD

Page Objects in BDD projects work identically to standalone Selenium/Playwright projects. The key is that step definitions call page object methods, keeping business language in feature files and UI mechanics in page objects.

```java
// src/test/java/com/example/pages/LoginPage.java
package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String BASE_URL = System.getProperty("base.url", "https://demo.example.com");

    // Locators
    private final By EMAIL_INPUT = By.id("email");
    private final By PASSWORD_INPUT = By.id("password");
    private final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private final By ERROR_MESSAGE = By.cssSelector("[data-testid='error-message']");
    private final By EMAIL_ERROR = By.cssSelector("[data-testid='email-error']");
    private final By PASSWORD_INPUT_FIELD = By.id("password");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void open() {
        driver.navigate().to(BASE_URL + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
    }

    public void fillEmail(String email) {
        WebElement emailField = wait.until(
            ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        emailField.clear();
        emailField.sendKeys(email);
    }

    public void fillPassword(String password) {
        driver.findElement(PASSWORD_INPUT).clear();
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
    }

    public void clearEmail() {
        driver.findElement(EMAIL_INPUT).clear();
    }

    public void clickButton(String buttonName) {
        if (buttonName.equals("Sign In")) {
            driver.findElement(LOGIN_BUTTON).click();
        } else {
            driver.findElement(By.xpath("//button[contains(text(),'" + buttonName + "')]")).click();
        }
    }

    public void clickLink(String linkText) {
        driver.findElement(By.linkText(linkText)).click();
    }

    public void clickLoginButton() {
        driver.findElement(LOGIN_BUTTON).click();
    }

    public void loginWith(String email, String password) {
        fillEmail(email);
        fillPassword(password);
        clickLoginButton();
    }

    public String getErrorMessage() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(ERROR_MESSAGE)).getText();
    }

    public String getEmailValidationError() {
        return driver.findElement(EMAIL_ERROR).getText();
    }

    public String getPasswordFieldValue() {
        return driver.findElement(PASSWORD_INPUT_FIELD).getAttribute("value");
    }

    public boolean isButtonEnabled(String buttonName) {
        return driver.findElement(LOGIN_BUTTON).isEnabled();
    }

    public boolean isAccountLockMessageVisible() {
        try {
            return driver.findElement(By.cssSelector("[data-testid='account-locked']")).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean wasLockoutEmailSent(String email) {
        // In real tests: query email server API
        return true; // Simplified
    }
}
```

---

## 11. Reporting

### Cucumber HTML Report

Generated automatically by the runner with `html:target/cucumber-reports/cucumber.html` in the plugin list.

```bash
# Run tests and open report
mvn test
open target/cucumber-reports/cucumber.html  # macOS
start target/cucumber-reports/cucumber.html # Windows
```

**Report contains:**
- Feature list with pass/fail/skip counts
- Each scenario with step-by-step pass/fail
- Duration per scenario
- Failure messages and stack traces
- Screenshots (if attached in @After hook)

### Masterthought Cucumber Reports (Rich HTML)

```java
// Generate rich report as part of Maven build (post-test)
// Add to pom.xml plugins:
<plugin>
  <groupId>net.masterthought</groupId>
  <artifactId>maven-cucumber-reporting</artifactId>
  <version>5.7.7</version>
  <executions>
    <execution>
      <id>execution</id>
      <phase>verify</phase>
      <goals>
        <goal>generate</goal>
      </goals>
      <configuration>
        <projectName>MyProject BDD Tests</projectName>
        <outputDirectory>target/cucumber-html-reports</outputDirectory>
        <inputDirectory>target/cucumber-reports</inputDirectory>
        <jsonFiles>
          <param>**/*.json</param>
        </jsonFiles>
        <buildNumber>1</buildNumber>
        <parallelTesting>false</parallelTesting>
        <skipEmptyJSONFiles>true</skipEmptyJSONFiles>
        <mergeFeaturesById>false</mergeFeaturesById>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### Serenity Report

```bash
# Run tests and generate Serenity report in one command
mvn clean verify

# Report at:
# target/site/serenity/index.html

# Includes:
# - Requirements coverage (% of features covered by passing tests)
# - Scenario narrative with screenshots at each step
# - Test timing and trends
# - Tag-based filtering
```

### Allure Report (Popular in CI)

```xml
<!-- Add to pom.xml -->
<dependency>
  <groupId>io.qameta.allure</groupId>
  <artifactId>allure-cucumber7-jvm</artifactId>
  <version>2.24.0</version>
</dependency>

<!-- And in CucumberOptions plugin: -->
<!-- "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" -->
```

```bash
# Run tests (generates allure-results/)
mvn test

# Generate and open report
allure serve target/allure-results
```

---

## 12. All Maven Commands for BDD

```bash
# Run all scenarios
mvn test

# Run with tag filter
mvn test -Dcucumber.filter.tags="@smoke"

# Run excluding tags
mvn test -Dcucumber.filter.tags="not @wip"

# Run AND condition (both tags)
mvn test -Dcucumber.filter.tags="@smoke and @login"

# Run OR condition
mvn test -Dcucumber.filter.tags="@smoke or @sanity"

# Run with browser override
mvn test -Dbrowser=firefox

# Run with environment override
mvn test -Denv=staging -Dbase.url=https://staging.example.com

# Run a specific feature file
mvn test -Dcucumber.features="src/test/resources/features/login.feature"

# Run in parallel (threads defined in testng.xml)
mvn test -Dthreads=4

# Skip tests (compile but don't run)
mvn test -DskipTests

# Clean and run
mvn clean test

# Run and generate full Serenity report
mvn clean verify

# Run failed scenarios only (needs rerun plugin config)
mvn test -Dsurefire.rerunFailingTestsCount=2

# Run with dry-run (check step definitions are found, don't execute)
mvn test -Dcucumber.filter.tags="@smoke" -Dcucumber.execution.dry-run=true

# Generate Serenity aggregate report
mvn serenity:aggregate

# Show Gauge test results
gauge run specs/ && open reports/html-report/index.html
```

---

## 13. Troubleshooting

### Problem 1: Step Definition Not Found

**Error:**
```
io.cucumber.junit.UndefinedStepException:
The step "the user enters email "john@example.com"" is undefined.
```

**Causes and fixes:**

1. **Wrong `glue` path in CucumberOptions** — Verify the package in `glue` matches the actual package of your step definition class.

2. **Step text mismatch** — The feature file step must exactly match the regex/string in `@Given`, `@When`, or `@Then`. Check for trailing spaces, punctuation differences.

3. **Parameter capture mismatch** — For `{string}`, the feature must have quoted text. For `{int}`, the feature must have a plain integer.

```java
// Feature:    the user enters email "john@example.com"
// Annotation: @When("the user enters email {string}")   ← matches
// Annotation: @When("the user enters email (.+)")       ← also matches (regex)

// WRONG — annotation uses {string} but feature has no quotes
// Feature:    the user enters email john@example.com
// Annotation: @When("the user enters email {string}")   ← NO MATCH
```

4. **Cucumber will print the suggested step definition** — copy it from the console output.

---

### Problem 2: Ambiguous Step Definitions

**Error:**
```
io.cucumber.ambiguity.AmbiguousStepDefinitionsException:
"the user clicks the Login button" matches more than one step definition
```

**Cause:** Two step definition methods match the same step text.

**Fix:** Make one pattern more specific, or merge them into one method with conditional logic:

```java
// Ambiguous — both match "the user clicks the Login button"
@When("the user clicks the {string} button")
public void clickButton(String name) { ... }

@When("the user clicks the Login button")
public void clickLoginButton() { ... }

// Fix: keep only the parameterized version and remove the specific one
@When("the user clicks the {string} button")
public void clickButton(String name) { ... }
```

---

### Problem 3: Cucumber Version Conflicts

**Error:**
```
NoClassDefFoundError: io/cucumber/java/en/Given
```

**Cause:** Mixed Cucumber versions across different artifacts.

**Fix:** Use the `cucumber-bom` (Bill of Materials) to enforce consistent versions:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.cucumber</groupId>
      <artifactId>cucumber-bom</artifactId>
      <version>7.14.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<!-- Then declare cucumber dependencies WITHOUT version — bom manages it -->
<dependencies>
  <dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <scope>test</scope>
  </dependency>
  <dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-testng</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

---

### Problem 4: Background Steps Not Running

**Cause:** The `Background` is scoped to one `Feature` file. If you have multiple Feature files, the Background only applies to the Feature it is defined in.

**Fix:** Use a `@Before` hook (applied globally or by tag) for cross-feature setup:

```java
@Before("@needs-login")
public void loginBeforeScenario() {
    loginPage.loginWith("admin@example.com", "Password123!");
}
```

---

### Problem 5: Data Table Not Injecting Correctly

**Error:**
```
io.cucumber.java.InvalidMethodSignatureException:
A method annotated with @When expects a DataTable or List, but received a String
```

**Fix:** The step method must accept a `DataTable` parameter when the feature has a data table:

```java
// Feature step has a table — method must accept DataTable
@When("the user fills in:")
public void fillForm(DataTable table) {    // ← DataTable, not String
    List<Map<String, String>> rows = table.asMaps();
    ...
}
```

---

## 14. Interview Q&A

**Q1. What is BDD and how is it different from TDD?**

**A:** TDD (Test-Driven Development) is a developer practice where unit tests are written before the code they test. Tests are technical, written in the programming language, and understood only by developers. BDD (Behaviour-Driven Development) extends TDD by shifting focus from technical units to system behaviour expressed in business language. BDD scenarios are written in Gherkin (Given/When/Then) before development and are understood by the whole team — business analysts, developers, and QA. BDD is about collaboration and shared understanding, not just test-first coding. Practically: TDD uses `assertEquals()` in Java; BDD uses a `login.feature` file that the business can read and validate. Both involve writing tests before code, but BDD operates at the feature/acceptance level.

---

**Q2. Explain the purpose of each Gherkin keyword: Feature, Background, Scenario, Scenario Outline, Given, When, Then, And, But.**

**A:**
- `Feature`: Names the feature and provides a narrative description of who benefits and why. Groups related scenarios.
- `Background`: Steps that run before every scenario in the file. Used for common setup (opening a page, seeding data) to avoid duplication.
- `Scenario`: A single, concrete example of the feature's behaviour. Represents one test case.
- `Scenario Outline`: A parameterized scenario template. Combined with `Examples`, it runs once per row — enabling data-driven testing within the feature file.
- `Given`: Sets the precondition or initial state. Corresponds to Arrange in AAA pattern.
- `When`: Describes the action or event being tested. Corresponds to Act.
- `Then`: Describes the expected outcome. Corresponds to Assert.
- `And`: Used after any of the above to add another step of the same type — improves readability without semantic change.
- `But`: Same as `And` but conventionally used for negative assertions, e.g., `But the error message is not displayed`.

---

**Q3. How do step definitions capture parameters from Gherkin steps?**

**A:** Cucumber uses expression types in step annotation strings to capture values from the step text. The two systems are:

1. **Cucumber Expressions** (recommended): `{string}` captures a double-quoted string, `{int}` captures an integer, `{float}` captures a float, `{word}` captures a single word (no spaces), `{bigdecimal}` for large numbers.

```java
// Feature: the user enters email "john@example.com"
@When("the user enters email {string}")
public void enterEmail(String email) { ... }

// Feature: the user waits 3 seconds
@When("the user waits {int} seconds")
public void waitSeconds(int seconds) { ... }
```

2. **Regular Expressions**: Capture groups `(.+)`, `(\d+)`, `(true|false)` etc.:
```java
@When("the user enters email (.+)")
public void enterEmail(String email) { ... }
```

Data Tables are passed as a `DataTable` parameter for steps that have a table in the feature. Docstrings are passed as a `String` parameter.

---

**Q4. What does Serenity BDD add over vanilla Cucumber, and what is the `@Steps` annotation?**

**A:** Serenity adds rich living documentation reports that show not just pass/fail but the full narrative of what was done — with screenshots at every step, timing, and requirements coverage (% of user stories covered by passing tests). It also integrates with JIRA for requirement tracing. The `@Steps` annotation is Serenity's mechanism for injecting "Step Libraries" — composable, reusable action layers similar to Page Objects but enhanced. When you annotate a field with `@Steps`, Serenity creates a proxy of that class. Every method call on the proxy is intercepted, logged with its method name as a step title in the report, and a screenshot is taken. This means the report shows exactly what actions were performed in each step without additional logging code. The `@Step("description {0}")` annotation on the method allows customizing the step title with parameter values in reports.

---

**Q5. How is Gauge different from Cucumber, and when would you choose Gauge over Cucumber?**

**A:** Gauge uses Markdown `.spec` files instead of Gherkin `.feature` files. There are no `Given/When/Then` keywords — steps are plain English sentences in Markdown. Step implementations use `@Step("step text")` without needing to specify Given/When/Then. Gauge has built-in concepts (`.cpt` files) for reusable step groups, DataStore for sharing data between steps, and native parallel execution. Choose Gauge when: the team dislikes the rigidity of Given/When/Then structure; specs need to be written in a more free-form narrative; the team is already familiar with Markdown; or you want built-in parallel execution without configuration. Choose Cucumber when: stakeholders are familiar with Gherkin; the project requires Serenity BDD integration; you need the mature ecosystem of Cucumber plugins and integrations; or team expertise is already in Cucumber.

---

**Q6. What is the purpose of the `@Before` and `@After` hooks, and how do tagged hooks work?**

**A:** `@Before` runs before each scenario and is used for setup — starting a browser, clearing state, loading test data, setting up authentication. `@After` runs after each scenario and is used for teardown — quitting the browser, cleaning up database records, taking screenshots on failure. Tagged hooks restrict execution to scenarios with specific tags:

```java
@Before("@authenticated")       // Only runs before @authenticated scenarios
@Before("@mobile")              // Only before @mobile scenarios
@After("@db-cleanup")           // Only after @db-cleanup scenarios
```

The `Scenario` parameter in hook methods gives access to scenario name, tags, and status:
```java
@After
public void takeScreenshotOnFailure(Scenario scenario) {
    if (scenario.isFailed()) {
        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        scenario.attach(screenshot, "image/png", "Failure Screenshot");
    }
}
```

The `order` attribute controls execution order when multiple hooks apply: `@Before(order = 1)` runs before `@Before(order = 2)`.

---

**Q7. What is a Scenario Outline and how is it different from a regular Scenario with a data table?**

**A:** A `Scenario Outline` with `Examples` creates multiple independent test scenarios — one per row in the Examples table. Each run is a completely separate scenario with its own Before/After hooks, its own browser session, and its own report entry. A data table inside a regular `Scenario` is passed to a single step method as a `DataTable` parameter. It does not create multiple scenarios — it passes structured data to that one step. Use `Scenario Outline` when you want to run the same user journey with different inputs (e.g., different user types logging in) and each run should be independent and separately reportable. Use a data table when a single step needs to process a list of data (e.g., creating 5 products in one test, filling a multi-field form in one test).

---

**Q8. How do you handle sharing state between step definitions in Cucumber?**

**A:** Step definitions are split across multiple classes for maintainability, but scenarios often need shared state (e.g., a created resource ID from a POST step used in a GET step). There are three patterns:

1. **PicoContainer dependency injection** (recommended — included with `cucumber-picocontainer`): Define a shared context class with the state you need to pass. PicoContainer injects the same instance into all step definition classes within a scenario:

```java
// Shared state class
public class ScenarioContext {
    public String createdUserId;
    public String authToken;
}

// Step definition 1
public class UserStepDefs {
    private final ScenarioContext context;
    public UserStepDefs(ScenarioContext context) { this.context = context; }

    @When("a new user is created")
    public void createUser() {
        // ...
        context.createdUserId = response.body().jsonPath().get("id");
    }
}

// Step definition 2
public class OrderStepDefs {
    private final ScenarioContext context;
    public OrderStepDefs(ScenarioContext context) { this.context = context; }

    @Then("the user has no orders")
    public void verifyNoOrders() {
        // Uses the ID set by step definition 1
        String userId = context.createdUserId;
        // ...
    }
}
```

2. **Spring dependency injection** (for Spring projects): Use `@ScenarioScope` beans shared across step classes.

3. **Static/singleton** (anti-pattern — avoid): Thread safety issues with parallel execution; data leaks between scenarios.

---

*End of File 07 — BDD + Cucumber + Gauge Complete Guide*
