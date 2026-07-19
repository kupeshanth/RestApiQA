# BDD + Cucumber + Gauge — Complete Interview Q&A Guide | Gherkin + Serenity + Step Definitions

---

## SECTION 1: BDD FUNDAMENTALS

---

**Q1: What is BDD? What problem does it solve in a software team?**

**A:** BDD (Behaviour-Driven Development) is an Agile practice that bridges the communication gap between business stakeholders, developers, and QA engineers. The core problem it solves: requirements misunderstandings. When a BA writes a JIRA story, the developer interprets it one way, the QA interprets it another way, and the business meant something else entirely. Features get built that don't match business intent.

BDD solves this by requiring the team to express requirements as concrete, human-readable examples before any code is written. These examples use a structured language called Gherkin (Given/When/Then) that everyone on the team can read and agree on.

Without BDD vs with BDD:

| Without BDD | With BDD |
|---|---|
| Requirements in JIRA prose — ambiguous | Requirements as concrete Gherkin examples — unambiguous |
| Tests written after code, often incomplete | Scenarios agreed before development — drive implementation |
| Developer, QA, and BA each understand differently | One shared language (Gherkin) everyone reads |
| Documentation becomes stale | Feature files ARE the living documentation — always current |
| "Done" is subjective | "Done" = all Gherkin scenarios pass |

The **Three Amigos** meeting is central to BDD: before a story is developed, the Product Owner (or BA), Developer, and QA Engineer meet to:
1. Define what "done" looks like using concrete examples
2. Write Gherkin scenarios together
3. Agree on edge cases and negative scenarios
4. Challenge assumptions and discover ambiguities before any code is written

The resulting feature files become both the acceptance criteria and the automated test suite.

Common mistake: Treating BDD as "just a way to write tests". BDD is primarily a collaboration and communication practice. The automation is a by-product of the collaboration, not the goal.

---

**Q2: What is the difference between BDD and TDD? When do you use each?**

**A:**

| Aspect | TDD (Test-Driven Development) | BDD (Behaviour-Driven Development) |
|---|---|---|
| Level | Unit level | Feature/acceptance level |
| Written by | Developers | Business + QA + Developers together |
| Language | Java/Python/etc. code | Gherkin (human-readable) |
| Tests what | Individual functions/methods | System behaviour from user perspective |
| Tool | JUnit, TestNG, pytest, Mocha | Cucumber, SpecFlow, Gauge |
| When to use | Before writing a function/class | Before writing a user story/feature |
| Audience | Developers only | Entire team including business |

TDD example:
```java
// JUnit 5 — pure TDD unit test
@Test
void shouldReturnTrueWhenEmailIsValid() {
    EmailValidator validator = new EmailValidator();
    assertTrue(validator.isValid("user@example.com"));
    assertFalse(validator.isValid("notanemail"));
    assertFalse(validator.isValid("@nodomain.com"));
}
// This tests the EmailValidator class in isolation — no browser, no app
```

BDD example:
```gherkin
# Cucumber feature file — BDD acceptance test
Scenario: Login fails with invalid email format
  Given the login page is open
  When the user enters email "notanemail"
  And the user enters password "SecurePass123!"
  Then the email validation error "Please enter a valid email address" is displayed
  And the Sign In button is disabled
```

They complement each other: BDD defines what the system should do at the business level; TDD ensures each individual component is implemented correctly. A well-rounded project uses both.

---

**Q3: What is Gherkin? Walk me through every keyword with its purpose.**

**A:** Gherkin is the domain-specific language used to write BDD scenarios. It is structured, plain-English, and language-independent (Cucumber supports Gherkin in 70+ spoken languages). Gherkin files have the `.feature` extension.

Complete keyword reference:

| Keyword | Purpose | Analogy |
|---|---|---|
| `Feature` | Names the feature; provides narrative description | The JIRA story title |
| `Background` | Steps that run before every scenario in the file | A `@BeforeEach` scoped to the file |
| `Scenario` | A single concrete example of behaviour | One test case |
| `Scenario Outline` | Parameterized scenario template | Data-driven test case |
| `Examples` | Data table for Scenario Outline | The data rows |
| `Given` | Precondition / initial state | Arrange (AAA pattern) |
| `When` | Action or event being tested | Act |
| `Then` | Expected outcome / assertion | Assert |
| `And` | Continues the previous step type (readability) | Additional step of same type |
| `But` | Negative continuation (same as And, conventional) | Negative assertion |
| `@tag` | Groups and filters scenarios | JUnit @Category / @Tag |
| `#` | Comment — ignored by Cucumber | Code comment |
| `"""` (triple quotes) | Docstring — multi-line string argument | String block |
| `\|` (pipe) | Data table column delimiter | Table cell separator |

```gherkin
# Comment — ignored by Cucumber at runtime

@login @authentication                # Tags on the Feature apply to ALL scenarios
Feature: User Login                   # Feature keyword — one per file
  As a registered user                # Optional narrative: As a <role>
  I want to log in to the application # I want to <action>
  So that I can access my dashboard   # So that <benefit>

  Background:                         # Runs before EVERY scenario in this file
    Given the login page is open
    And no user is currently logged in

  @smoke @happy-path                  # Tags on Scenario apply only to this scenario
  Scenario: Successful login          # One test case
    When the user enters email "john@example.com"
    And the user enters password "Password123!"    # And = another When
    And the user clicks the Sign In button
    Then the dashboard is displayed               # Assert
    And the welcome message shows "Hello, John!"  # Another Then
    But the login form is no longer visible       # Negative Then — But = another Then

  @regression                         # Different tag on this scenario
  Scenario Outline: Login validation  # Parameterized — runs once per Examples row
    When the user enters email "<email>"
    Then the error "<error>" is displayed

    Examples:                         # Data for Scenario Outline
      | email          | error                           |
      | notanemail     | Please enter a valid email      |
      | @nodomain.com  | Please enter a valid email      |

  Scenario: Submit with long message  # Docstring example
    When the user submits the form with message:
      """
      Hello support team,
      I cannot log in.
      Please help.
      """
    Then a support ticket is created
```

Step type rules:
- `Given` = Arrange — set the precondition. Answer: "What is already true before the action?"
- `When` = Act — the thing being tested. Answer: "What does the user/system do?"
- `Then` = Assert — the observable outcome. Answer: "What should be true after the action?"
- `And` / `But` inherit the type of the step above them (no functional difference)

---

**Q4: What is a Feature file? Show a complete, realistic example.**

**A:** A Feature file is a plain-text `.feature` file containing Gherkin scenarios. It represents one feature of the system — typically corresponding to a user story. Feature files live in `src/test/resources/features/` in a Maven project.

```gherkin
# src/test/resources/features/checkout.feature

@checkout @e2e
Feature: Shopping Cart Checkout
  As a logged-in customer
  I want to complete a purchase
  So that I can receive my ordered items

  Background:
    Given the user "alice@example.com" is logged in with password "Secure123!"
    And the shopping cart is empty

  @smoke @happy-path
  Scenario: Successful checkout with credit card
    Given the product "Laptop Pro 15" at price "$1299.00" is in the cart
    When the user navigates to the checkout page
    And the user enters shipping address:
      | Field        | Value            |
      | Street       | 123 Main Street  |
      | City         | San Francisco    |
      | State        | CA               |
      | Zip Code     | 94102            |
    And the user enters credit card number "4111111111111111"
    And the user enters expiry "12/26" and CVV "123"
    And the user clicks "Place Order"
    Then the order confirmation page is displayed
    And the order number is generated
    And a confirmation email is sent to "alice@example.com"
    And the cart is now empty

  @regression @negative
  Scenario: Checkout fails when card is declined
    Given the product "Wireless Headphones" at price "$299.00" is in the cart
    When the user navigates to the checkout page
    And the user enters a declined credit card "4000000000000002"
    And the user clicks "Place Order"
    Then the error "Your card was declined. Please try a different payment method." is displayed
    And the order is not created
    And the cart still contains "Wireless Headphones"

  @regression
  Scenario Outline: Apply discount codes
    Given the product "T-Shirt" at price "$29.99" is in the cart
    When the user applies discount code "<code>"
    Then the discount "<discount_amount>" is applied
    And the final total is "<final_total>"

    Examples:
      | code        | discount_amount | final_total |
      | SAVE10      | $3.00           | $26.99      |
      | WELCOME20   | $6.00           | $23.99      |
      | INVALID999  | $0.00           | $29.99      |
      | FREESHIP    | $0.00           | $29.99      |

  @regression @negative
  Scenario: Cannot checkout with empty cart
    Given the shopping cart is empty
    When the user navigates to the checkout page
    Then the user is redirected to the shopping page
    And the message "Your cart is empty. Add items to continue." is displayed
```

---

**Q5: What is `Background` in Gherkin? When should you use it and when should you avoid it?**

**A:** `Background` is a block of steps that runs before every scenario in the feature file. It is used to avoid repeating common setup steps in every scenario.

```gherkin
Feature: Product Management

  Background:
    Given the admin user "admin@example.com" is logged in
    And the admin is on the product management page
    # These 2 steps run before EVERY scenario in this file

  Scenario: Add a new product
    When the admin clicks "Add New Product"
    And the admin fills in product name "Laptop Pro"
    And the admin sets price to "$999.99"
    And the admin clicks "Save Product"
    Then the product "Laptop Pro" appears in the product list

  Scenario: Delete a product
    Given the product "Old Product" exists
    When the admin clicks "Delete" next to "Old Product"
    And the admin confirms deletion
    Then "Old Product" is removed from the product list

  Scenario: Edit a product price
    Given the product "Laptop Pro" exists with price "$999.99"
    When the admin clicks "Edit" next to "Laptop Pro"
    And the admin changes the price to "$899.99"
    And the admin saves the changes
    Then "Laptop Pro" shows price "$899.99"
```

When to use Background:
- Common navigation (navigate to a page before every scenario)
- Common authentication (log in before every scenario)
- Common data setup that applies to ALL scenarios in the file

When NOT to use Background:
- If only some scenarios need the steps — use hooks with tags instead (`@Before("@needs-login")`)
- If the steps make scenarios harder to read in isolation — a reader should understand a scenario without needing to scroll up to Background
- Avoid more than 2-3 steps in Background — if it's getting complex, use a `@Before` hook in Java

Common mistake: Putting conflicting setup in Background. If one scenario needs the cart to be empty and another needs the cart to have items, Background cannot serve both — use `Given` steps per scenario for the cart state.

---

## SECTION 2: GHERKIN SYNTAX — ADVANCED

---

**Q6: What is the difference between `Scenario` and `Scenario Outline`? Show both with examples.**

**A:**

`Scenario` is a single concrete example with fixed values. It runs exactly once. `Scenario Outline` is a template with placeholders (written in angle brackets `<placeholder>`) combined with an `Examples` table that provides the data. It runs once per row in the Examples table — each row creates a completely independent scenario with its own Before/After hooks.

```gherkin
# Scenario: fixed values — runs ONCE
Scenario: Admin user logs in to admin panel
  Given the login page is open
  When the user logs in with email "admin@company.com" and password "Admin123!"
  Then the admin dashboard is displayed
  And the "User Management" menu is visible

# Scenario Outline: template with <placeholders> — runs ONCE PER EXAMPLES ROW
Scenario Outline: Different user roles navigate to their respective dashboards
  Given the login page is open
  When the user logs in with email "<email>" and password "<password>"
  Then the user is redirected to "<expected_page>"
  And the page title is "<page_title>"
  And the "<menu_item>" menu is <menu_visibility>

  Examples:
    | email                  | password    | expected_page | page_title       | menu_item       | menu_visibility |
    | admin@company.com      | Admin123!   | /admin        | Admin Dashboard  | User Management | visible         |
    | manager@company.com    | Mgr456!     | /manager      | Manager Portal   | Reports         | visible         |
    | employee@company.com   | Emp789!     | /dashboard    | My Dashboard     | User Management | hidden          |
    | readonly@company.com   | Read000!    | /dashboard    | My Dashboard     | User Management | hidden          |
```

What Cucumber generates from the Scenario Outline above:
- 4 independent scenarios
- Each has the substituted values in its title (e.g., "Different user roles — admin@company.com")
- Each runs its own `@Before` and `@After` hooks
- Each appears separately in the HTML report — pass or fail independently

Step definition — same method handles all rows because Cucumber does the substitution before calling the step:

```java
@When("the user logs in with email {string} and password {string}")
public void theUserLogsInWith(String email, String password) {
    loginPage.fillEmail(email);
    loginPage.fillPassword(password);
    loginPage.clickSignIn();
}

@Then("the user is redirected to {string}")
public void theUserIsRedirectedTo(String expectedPage) {
    String currentUrl = driver.getCurrentUrl();
    Assert.assertTrue(currentUrl.contains(expectedPage),
        "Expected URL to contain '" + expectedPage + "' but was: " + currentUrl);
}
```

---

**Q7: What are Examples tables in a Scenario Outline and how does Cucumber process them?**

**A:** The `Examples` table (also called `Scenarios` table in some Gherkin implementations) provides the data that fills the `<placeholder>` tokens in a Scenario Outline. The first row is the header — column names must match the placeholder names in angle brackets. Each subsequent row is one test run.

```gherkin
Scenario Outline: Registration form validates all required fields
  Given the registration page is open
  When the user submits the form with <field> set to <value>
  Then the validation error "<error_message>" is displayed for the "<field>" field

  Examples: Empty fields
    | field      | value | error_message               |
    | First Name |       | First name is required      |
    | Last Name  |       | Last name is required       |
    | Email      |       | Email is required           |
    | Password   |       | Password is required        |

  Examples: Invalid formats
    | field    | value           | error_message                          |
    | Email    | notanemail      | Please enter a valid email address     |
    | Email    | @nodomain       | Please enter a valid email address     |
    | Password | 123             | Password must be at least 8 characters |
    | Password | alllowercase1   | Password must contain an uppercase letter |
```

Multiple `Examples` blocks in one Scenario Outline: Cucumber combines all rows from all Examples blocks and runs them all. The different Examples blocks are just for organization — they can have different names (like "Empty fields" and "Invalid formats" above) which appear in the report.

How Cucumber processes them:
1. For each row in Examples, Cucumber replaces every `<placeholder>` in the scenario with the corresponding column value
2. The resulting scenario is run as an independent test
3. Placeholders in step text, in data table cells, and in docstrings are all replaced

```java
// Step definition — works for any row because values are passed as parameters
@When("the user submits the form with {word} set to {string}")
public void theUserSubmitsFormWithField(String field, String value) {
    registrationPage.fillField(field, value);
    registrationPage.clickSubmit();
}

@Then("the validation error {string} is displayed for the {string} field")
public void theValidationErrorIsDisplayed(String errorMessage, String field) {
    String actualError = registrationPage.getFieldError(field);
    Assert.assertEquals(actualError, errorMessage,
        "Validation error for field '" + field + "' does not match");
}
```

---

**Q8: What are Data Tables in Gherkin? How are they different from Examples tables?**

**A:** Data tables pass structured tabular data to a single step. They do not create multiple scenarios — they pass a table to one step method as a `DataTable` parameter.

| Aspect | Examples table (Scenario Outline) | Data table |
|---|---|---|
| Purpose | Multiple scenario runs with different inputs | Structured data for ONE step |
| Creates multiple scenarios? | Yes — one per row | No — one scenario total |
| Has Before/After hooks per row? | Yes | No |
| In step definition | Not accessed in Java — Cucumber substitutes | Accessed as `DataTable` parameter |

```gherkin
# Data table example — this scenario runs ONCE; the table is passed to one step
Scenario: Create multiple products in a batch upload
  Given the admin is on the product import page
  When the admin uploads the following products:
    | Name              | Category    | Price   | Stock | SKU       |
    | Laptop Pro 15     | Electronics | $999.99 | 50    | LAP-001   |
    | Wireless Mouse    | Accessories | $29.99  | 200   | MOU-001   |
    | USB-C Hub 7-port  | Accessories | $49.99  | 150   | USB-001   |
    | 4K Monitor 27"    | Electronics | $399.99 | 30    | MON-001   |
  Then all 4 products appear in the product catalogue
  And the total inventory value is "$62,797.00"

# Another data table example — key-value pairs for a form
Scenario: Register a new corporate account
  Given the registration page is open
  When the user fills in the registration form:
    | Field          | Value                   |
    | Company Name   | Acme Corporation        |
    | First Name     | John                    |
    | Last Name      | Doe                     |
    | Email          | john.doe@acme.com       |
    | Phone          | +1-555-123-4567         |
    | Country        | United States           |
    | Plan           | Enterprise              |
  And the user clicks "Create Account"
  Then the account is created successfully
  And a welcome email is sent to "john.doe@acme.com"
```

```java
import io.cucumber.datatable.DataTable;
import java.util.List;
import java.util.Map;

// Processing a data table as List<Map<String, String>> — each row is a Map
@When("the admin uploads the following products:")
public void theAdminUploadsProducts(DataTable dataTable) {
    // asMaps() interprets first row as header, rest as data rows
    List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);

    for (Map<String, String> product : products) {
        String name     = product.get("Name");
        String category = product.get("Category");
        String price    = product.get("Price");
        String stock    = product.get("Stock");
        String sku      = product.get("SKU");

        productImportPage.addProduct(name, category, price, Integer.parseInt(stock), sku);
    }
    productImportPage.clickUpload();
}

// Processing a key-value data table as List<List<String>> — raw rows
@When("the user fills in the registration form:")
public void theUserFillsInRegistrationForm(DataTable dataTable) {
    // asLists() returns raw rows including header row
    List<List<String>> rows = dataTable.asLists(String.class);
    // Skip header row (index 0), process data rows
    for (int i = 1; i < rows.size(); i++) {
        String field = rows.get(i).get(0);   // "Company Name"
        String value = rows.get(i).get(1);   // "Acme Corporation"
        registrationPage.fillField(field, value);
    }
}

// OR: for key-value tables, use asMaps() which handles header automatically
@When("the user fills in the registration form:")
public void theUserFillsInRegistrationFormAlt(DataTable dataTable) {
    Map<String, String> formData = dataTable.asMap(String.class, String.class);
    // Maps Field → Value
    registrationPage.fillCompanyName(formData.get("Company Name"));
    registrationPage.fillFirstName(formData.get("First Name"));
    registrationPage.fillEmail(formData.get("Email"));
}
```

---

**Q9: What is a tag in Cucumber? How do you use tags to organize and filter tests?**

**A:** Tags are annotations prefixed with `@` placed above `Feature`, `Scenario`, or `Scenario Outline` in a feature file. They group, filter, and add metadata to scenarios. Tags on a `Feature` apply to all scenarios in that file.

```gherkin
@authentication @core
Feature: User Authentication

  @smoke @happy-path @critical
  Scenario: Successful login
    ...

  @regression @negative @security
  Scenario: Account locked after 5 failed attempts
    ...

  @regression @validation
  Scenario Outline: Email format validation
    ...

  @wip
  Scenario: Login with biometric (not yet implemented)
    ...

  @skip
  Scenario: Legacy SSO (deprecated, do not run)
    ...
```

Standard tag conventions (your team defines these):

```
@smoke       — Fast, critical-path tests (aim for < 5 min total suite)
@regression  — Full regression suite (run on every PR merge)
@sanity      — Minimal post-deployment health check
@happy-path  — Positive/success scenarios only
@negative    — Error, failure, rejection scenarios
@validation  — Input validation scenarios
@security    — Security-related scenarios (XSS, auth bypass, etc.)
@e2e         — End-to-end full user journeys
@api         — API-level tests
@ui          — UI-level tests
@performance — Performance/load related
@critical    — Business-critical — must always pass
@wip         — Work in progress — do not run in CI
@skip        — Intentionally disabled (with a comment explaining why)
@flaky       — Known flaky tests (run in isolation or with retry logic)
@db-cleanup  — Requires database cleanup after test
@smoke and @android  — Multi-platform tagging
```

---

**Q10: What are all the tag expression operators? Show syntax for AND, OR, NOT combinations.**

**A:** Cucumber tag expressions use standard boolean operators. The syntax changed from Cucumber 4 (which used `~` for NOT and `,` for AND) to Cucumber 5+ (which uses standard `and`, `or`, `not` keywords).

```bash
# ── Single tag ────────────────────────────────────────────────────────────────
mvn test -Dcucumber.filter.tags="@smoke"
# Runs all scenarios tagged @smoke

# ── AND — both tags must be present on the scenario ──────────────────────────
mvn test -Dcucumber.filter.tags="@smoke and @login"
# Runs scenarios that have BOTH @smoke AND @login

# ── OR — either tag is enough ─────────────────────────────────────────────────
mvn test -Dcucumber.filter.tags="@smoke or @sanity"
# Runs scenarios that have @smoke OR @sanity (or both)

# ── NOT — exclude tag ─────────────────────────────────────────────────────────
mvn test -Dcucumber.filter.tags="not @wip"
# Runs all scenarios that are NOT tagged @wip

# ── Complex combinations ──────────────────────────────────────────────────────
mvn test -Dcucumber.filter.tags="@regression and not @slow"
# Run @regression but exclude any tagged @slow

mvn test -Dcucumber.filter.tags="@smoke or @critical"
# Run @smoke or @critical

mvn test -Dcucumber.filter.tags="(@smoke or @sanity) and not @skip"
# Run @smoke or @sanity, but exclude anything tagged @skip

mvn test -Dcucumber.filter.tags="@ui and @regression and not @flaky"
# Run UI regression tests but skip known-flaky ones

# ── In CucumberOptions (runner class) ─────────────────────────────────────────
@CucumberOptions(
    tags = "not @skip and not @wip"   // Always exclude wip and skip
)
// This is the default — override via -Dcucumber.filter.tags on command line
```

In the feature file and runner — tags can also be combined at the Feature level:

```gherkin
# This scenario inherits @authentication from Feature + has its own @smoke
@authentication @core
Feature: User Authentication

  @smoke    # This scenario is tagged: @authentication, @core, @smoke
  Scenario: Successful login
    ...

  @regression @negative  # This scenario is tagged: @authentication, @core, @regression, @negative
  Scenario: Invalid password
    ...
```

---

## SECTION 3: STEP DEFINITIONS

---

**Q11: How do you write step definitions in Java? Show a complete class with all step types.**

**A:** Step definitions are Java methods annotated with `@Given`, `@When`, `@Then`, `@And`, or `@But`. Cucumber matches the text in the annotation to the step text in the feature file. Cucumber creates a new instance of the step definition class for each scenario (unless using dependency injection).

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

// Cucumber creates a new instance of this class per scenario
public class LoginStepDefinitions {

    private final WebDriver driver;
    private final LoginPage loginPage;
    private DashboardPage dashboardPage;

    // Constructor injection — Cucumber/PicoContainer provides the WebDriver
    public LoginStepDefinitions(WebDriver driver) {
        this.driver = driver;
        this.loginPage = new LoginPage(driver);
    }

    // ── Given steps (preconditions) ───────────────────────────────────────────

    @Given("the login page is open")
    public void theLoginPageIsOpen() {
        loginPage.open();
    }

    @Given("no user is currently logged in")
    public void noUserIsCurrentlyLoggedIn() {
        driver.manage().deleteAllCookies();
        driver.navigate().refresh();
    }

    @Given("the user {string} is logged in with password {string}")
    public void theUserIsLoggedInWith(String email, String password) {
        loginPage.open();
        loginPage.loginWith(email, password);
        dashboardPage = new DashboardPage(driver);
        dashboardPage.verifyPageLoaded();
    }

    // ── When steps (actions) ─────────────────────────────────────────────────

    @When("the user enters email {string}")
    public void theUserEntersEmail(String email) {
        loginPage.fillEmail(email);
    }

    @When("the user enters password {string}")
    public void theUserEntersPassword(String password) {
        loginPage.fillPassword(password);
    }

    @When("the user clicks the {string} button")
    public void theUserClicksButton(String buttonName) {
        loginPage.clickButton(buttonName);
    }

    @When("the user attempts to login with wrong password {int} times")
    public void theUserAttemptsLoginWithWrongPassword(int times) {
        for (int i = 0; i < times; i++) {
            loginPage.open();
            loginPage.fillEmail("john@example.com");
            loginPage.fillPassword("wrongpassword" + i);
            loginPage.clickButton("Sign In");
        }
    }

    @When("the user clicks the {string} link")
    public void theUserClicksLink(String linkText) {
        loginPage.clickLink(linkText);
    }

    // ── Then steps (assertions) ──────────────────────────────────────────────

    @Then("the user is redirected to the dashboard")
    public void theUserIsRedirectedToDashboard() {
        dashboardPage = new DashboardPage(driver);
        dashboardPage.verifyPageLoaded();
    }

    @Then("the welcome message displays {string}")
    public void theWelcomeMessageDisplays(String expectedMessage) {
        Assert.assertEquals(dashboardPage.getWelcomeMessage(), expectedMessage,
            "Welcome message text does not match expected value");
    }

    @Then("the URL contains {string}")
    public void theURLContains(String fragment) {
        Assert.assertTrue(driver.getCurrentUrl().contains(fragment),
            "URL '" + driver.getCurrentUrl() + "' does not contain '" + fragment + "'");
    }

    @Then("an error message {string} is displayed")
    public void anErrorMessageIsDisplayed(String expectedError) {
        Assert.assertEquals(loginPage.getErrorMessage(), expectedError,
            "Error message text does not match");
    }

    @Then("the {string} button is disabled")
    public void theButtonIsDisabled(String buttonName) {
        Assert.assertFalse(loginPage.isButtonEnabled(buttonName),
            "Button '" + buttonName + "' should be disabled but is enabled");
    }

    @Then("the account is locked")
    public void theAccountIsLocked() {
        Assert.assertTrue(loginPage.isAccountLockMessageVisible(),
            "Account locked message should be visible");
    }

    // ── And / But work exactly like Given/When/Then ────────────────────────────
    // Cucumber treats And/But as the same type as the preceding step keyword

    @And("the password field is cleared")
    public void thePasswordFieldIsCleared() {
        Assert.assertEquals(loginPage.getPasswordFieldValue(), "",
            "Password field should be empty after failed login");
    }

    @And("the session is terminated")
    public void theSessionIsTerminated() {
        Assert.assertNull(driver.manage().getCookieNamed("session_token"),
            "Session cookie should not exist after logout");
    }
}
```

---

**Q12: What are Cucumber expression types? Explain `{string}`, `{int}`, `{float}`, `{word}`, and `{}`.**

**A:** Cucumber expressions are the pattern system used in step annotation strings to capture parameters from step text. They are simpler and more readable than raw regular expressions.

```java
// {string} — matches a double-quoted string; captures content without quotes
// Feature: the user enters email "john@example.com"
@When("the user enters email {string}")
public void enterEmail(String email) {
    // email = "john@example.com" (no quotes)
    loginPage.fillEmail(email);
}

// {int} — matches an unquoted integer
// Feature: the user waits 3 seconds
@When("the user waits {int} seconds")
public void wait(int seconds) throws InterruptedException {
    Thread.sleep(seconds * 1000L);
}

// Feature: the user attempts to login 5 times
@When("the user attempts to login {int} times")
public void attemptLogin(int times) {
    for (int i = 0; i < times; i++) { /* ... */ }
}

// {float} — matches a decimal number (also matches integers)
// Feature: the price should be 29.99
@Then("the price should be {float}")
public void verifyPrice(float price) {
    Assert.assertEquals(cartPage.getPrice(), price, 0.01f);
}

// {word} — matches a single word (no spaces, no quotes)
// Feature: the user selects role admin
@When("the user selects role {word}")
public void selectRole(String role) {
    // role = "admin" — no quotes in feature, no spaces allowed
    userPage.selectRole(role);
}

// {} (anonymous parameter) — matches anything except whitespace
// Feature: the status is Active
@Then("the status is {}")
public void verifyStatus(String status) {
    // Matches any single word without quotes
    Assert.assertEquals(userPage.getStatus(), status);
}

// {bigdecimal} — for precise monetary/decimal values
// Feature: the total is 1234.56
@Then("the total is {bigdecimal}")
public void verifyTotal(java.math.BigDecimal total) {
    Assert.assertEquals(checkoutPage.getTotal(), total);
}

// Regular expressions — more powerful but less readable
// Feature: the user waits 10 seconds or 10000 milliseconds
@When("the user waits (\\d+) (seconds|milliseconds)")
public void waitWithUnit(int amount, String unit) {
    long millis = unit.equals("seconds") ? amount * 1000L : amount;
    // ...
}

// Optional text with () — parentheses make part optional
// Feature: the cart shows 1 item    OR    the cart shows 2 items
@Then("the cart shows {int} item(s)")
public void verifyCartItemCount(int count) {
    Assert.assertEquals(cartPage.getItemCount(), count);
}
```

---

**Q13: How do you use a DataTable in a step definition? Show `asMaps()`, `asLists()`, and `asMap()`.**

**A:**

```java
import io.cucumber.datatable.DataTable;
import java.util.List;
import java.util.Map;

// ── Feature file step with data table ─────────────────────────────────────────
//
// When the admin creates the following users:
//   | Name       | Email               | Role    |
//   | Alice Wong | alice@example.com   | Manager |
//   | Bob Smith  | bob@example.com     | Admin   |
//   | Carol Lee  | carol@example.com   | Editor  |

// ── Method 1: asMaps() — each row as Map<header, value> (MOST COMMON) ─────────
@When("the admin creates the following users:")
public void theAdminCreatesUsers(DataTable dataTable) {
    List<Map<String, String>> users = dataTable.asMaps(String.class, String.class);
    // users = [
    //   {"Name": "Alice Wong", "Email": "alice@example.com", "Role": "Manager"},
    //   {"Name": "Bob Smith",  "Email": "bob@example.com",  "Role": "Admin"},
    //   {"Name": "Carol Lee",  "Email": "carol@example.com", "Role": "Editor"}
    // ]
    for (Map<String, String> user : users) {
        String name  = user.get("Name");
        String email = user.get("Email");
        String role  = user.get("Role");
        userManagementPage.createUser(name, email, role);
        System.out.printf("Created user: %s (%s) as %s%n", name, email, role);
    }
}

// ── Method 2: asLists() — raw rows as List<List<String>> (includes header) ────
@When("the admin creates the following users:")
public void theAdminCreatesUsersRaw(DataTable dataTable) {
    List<List<String>> rows = dataTable.asLists(String.class);
    // rows.get(0) = ["Name", "Email", "Role"]  ← header
    // rows.get(1) = ["Alice Wong", "alice@example.com", "Manager"]
    // rows.get(2) = ["Bob Smith", "bob@example.com", "Admin"]

    // Skip header row (index 0)
    for (int i = 1; i < rows.size(); i++) {
        List<String> row = rows.get(i);
        userManagementPage.createUser(row.get(0), row.get(1), row.get(2));
    }
}

// ── Method 3: asMap() — for key-value pair tables (2 columns only) ────────────
//
// Feature:
//   When the user fills in the contact form:
//     | Name    | John Doe            |
//     | Email   | john@example.com    |
//     | Subject | Account inquiry     |
//     | Message | I need help please  |

@When("the user fills in the contact form:")
public void theUserFillsContactForm(DataTable dataTable) {
    Map<String, String> formData = dataTable.asMap(String.class, String.class);
    // formData = {"Name": "John Doe", "Email": "john@example.com", ...}
    contactPage.fillName(formData.get("Name"));
    contactPage.fillEmail(formData.get("Email"));
    contactPage.fillSubject(formData.get("Subject"));
    contactPage.fillMessage(formData.get("Message"));
}

// ── Method 4: Convert to POJO list using DataTableType ────────────────────────
// Define a converter (in a configuration class):
@DataTableType
public User userEntry(Map<String, String> entry) {
    User user = new User();
    user.setName(entry.get("Name"));
    user.setEmail(entry.get("Email"));
    user.setRole(entry.get("Role"));
    return user;
}

// Now the step receives List<User> directly:
@When("the admin creates the following users:")
public void theAdminCreatesUsers(List<User> users) {
    for (User user : users) {
        userManagementPage.createUser(user.getName(), user.getEmail(), user.getRole());
    }
}
```

---

**Q14: What are Cucumber hooks? Explain `@Before`, `@After`, `@BeforeStep`, and `@AfterStep`.**

**A:** Hooks are Java methods annotated with Cucumber lifecycle annotations that run at specific points around scenarios and steps. Unlike TestNG's `@BeforeMethod`, they are Cucumber-aware and receive the `Scenario` object.

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
    // Runs before EVERY scenario in every feature file
    // 'order' controls execution order when multiple @Before hooks exist
    // Lower number = runs first

    @Before(order = 1)
    public void logScenarioStart(Scenario scenario) {
        System.out.println("═══════════════════════════════════");
        System.out.println("SCENARIO: " + scenario.getName());
        System.out.println("TAGS:     " + scenario.getSourceTagNames());
        System.out.println("═══════════════════════════════════");
    }

    @Before(order = 2)
    public void startBrowser(Scenario scenario) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,720",
                             "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        System.out.println("Browser started for: " + scenario.getName());
    }

    // ── @After ────────────────────────────────────────────────────────────────
    // Runs after EVERY scenario, even if the scenario fails
    // Higher order = runs first for @After (reverse of @Before)

    @After(order = 1)
    public void quitBrowser(Scenario scenario) {
        System.out.println("RESULT: " + (scenario.isFailed() ? "FAILED" : "PASSED"));
        if (driver != null) {
            driver.quit();
        }
    }

    @After(order = 2)
    public void takeScreenshotOnFailure(Scenario scenario) {
        // This runs BEFORE quitBrowser (higher order runs first for @After)
        if (scenario.isFailed()) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES);
                // Attach screenshot to Cucumber HTML report
                scenario.attach(screenshot, "image/png", "Failure Screenshot - " + scenario.getName());
                System.out.println("Screenshot attached for failed scenario");
            } catch (Exception e) {
                System.err.println("Screenshot failed: " + e.getMessage());
            }
        }
    }

    // ── @BeforeStep ───────────────────────────────────────────────────────────
    // Runs before EVERY step in EVERY scenario
    // Use sparingly — adds overhead to every step

    @BeforeStep
    public void beforeEachStep(Scenario scenario) {
        // Useful for: logging step execution, recording timing, resetting state
        // Avoid: heavy operations that slow down each step
    }

    // ── @AfterStep ────────────────────────────────────────────────────────────
    // Runs after EVERY step in EVERY scenario
    // Useful for: capturing screenshot after each step (for trace reports)

    @AfterStep
    public void afterEachStep(Scenario scenario) {
        // Screenshot after every step — creates a full execution trace
        // ONLY enable when debugging — dramatically increases test time
        if (scenario.isFailed()) {
            // Already failed — no need to capture on every subsequent step
            return;
        }
        // Optional: attach screenshot after each step for trace
        // byte[] step_screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        // scenario.attach(step_screenshot, "image/png", "Step Screenshot");
    }
}
```

---

**Q15: What are tagged hooks in Cucumber? Show examples with `@Before("@smoke")` syntax.**

**A:** Tagged hooks restrict a hook to run only for scenarios that have a specific tag. This is essential for setup/teardown that only applies to certain scenarios (authentication hooks, database cleanup, mobile setup, etc.).

```java
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

public class TaggedHooks {

    private WebDriver driver;

    // ── Runs ONLY before scenarios tagged @authenticated ──────────────────────
    @Before("@authenticated")
    public void setUpAuthenticatedSession(Scenario scenario) {
        System.out.println("Loading saved auth cookies for: " + scenario.getName());
        // Load previously saved authentication cookies
        // loginHelper.loadSavedSession(driver, "fixtures/auth-session.json");
    }

    // ── Runs ONLY before scenarios tagged @mobile ─────────────────────────────
    @Before("@mobile")
    public void setUpMobileViewport(Scenario scenario) {
        // Set viewport to mobile size for mobile-responsive tests
        driver.manage().window().setSize(
            new org.openqa.selenium.Dimension(375, 812)
        );
    }

    // ── Runs ONLY before scenarios tagged @db-setup ───────────────────────────
    @Before("@db-setup")
    public void populateTestDatabase(Scenario scenario) {
        System.out.println("Seeding test database for: " + scenario.getName());
        // dbSeeder.seedData("fixtures/test-data.sql");
    }

    // ── Tagged @After hooks ────────────────────────────────────────────────────

    @After("@db-cleanup")
    public void cleanDatabase(Scenario scenario) {
        System.out.println("Cleaning up DB after: " + scenario.getName());
        // dbCleaner.rollbackTestData();
    }

    @After("@api-cleanup")
    public void cleanApiTestData(Scenario scenario) {
        // Delete resources created via API during the test
        // apiClient.deleteCreatedResources();
    }

    // ── Multiple tags in a hook — AND expression ───────────────────────────────
    @Before("@regression and @authenticated")
    public void setUpRegressionAuthenticatedScenario(Scenario scenario) {
        // Runs only when scenario has BOTH @regression AND @authenticated
        System.out.println("Regression + authenticated setup for: " + scenario.getName());
    }

    // ── Negative tag expression ────────────────────────────────────────────────
    @Before("not @api")
    public void startBrowserForNonApiTests(Scenario scenario) {
        // Starts browser only for UI tests — skips API-only tests
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
    }
}
```

---

**Q16: How do you take a screenshot on failure inside a Cucumber hook?**

**A:**

```java
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotHook {

    // WebDriver injected via PicoContainer (shared with step definitions)
    private final WebDriver driver;

    public ScreenshotHook(WebDriver driver) {
        this.driver = driver;
    }

    @After
    public void captureScreenshotOnFailure(Scenario scenario) {
        if (scenario.isFailed()) {

            // Method 1: Attach screenshot directly to Cucumber HTML report
            try {
                byte[] screenshot = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES);
                // This embeds the image inside the Cucumber HTML report
                scenario.attach(screenshot, "image/png",
                    "Screenshot — " + scenario.getName());
            } catch (Exception e) {
                System.err.println("Could not attach screenshot: " + e.getMessage());
            }

            // Method 2: Save to disk with timestamped filename
            try {
                String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                // Clean scenario name for use as filename
                String safeName = scenario.getName()
                    .replaceAll("[^a-zA-Z0-9_-]", "_")
                    .substring(0, Math.min(scenario.getName().length(), 50));

                Path screenshotPath = Paths.get(
                    "target/screenshots/FAILED_" + safeName + "_" + timestamp + ".png"
                );
                Files.createDirectories(screenshotPath.getParent());

                byte[] screenshotBytes = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES);
                Files.write(screenshotPath, screenshotBytes);

                System.out.println("Screenshot saved: " + screenshotPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Could not save screenshot to disk: " + e.getMessage());
            }
        }
    }
}
```

---

## SECTION 4: CUCUMBER PROJECT SETUP

---

**Q17: What is the CucumberRunner class? Walk through every part of it.**

**A:** The CucumberRunner class is the entry point that tells Cucumber where to find feature files and step definitions, which reporters to use, and how to filter scenarios. It is annotated with `@CucumberOptions` and (for TestNG) extends `AbstractTestNGCucumberTests`.

```java
// src/test/java/com/example/runner/CucumberRunner.java
package com.example.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
    // ── features ──────────────────────────────────────────────────────────────
    // Path to .feature files (relative to project root)
    features = "src/test/resources/features",
    // Can also target a specific file:
    // features = "src/test/resources/features/login.feature"
    // Or a specific scenario by line number:
    // features = "src/test/resources/features/login.feature:25"

    // ── glue ──────────────────────────────────────────────────────────────────
    // Package paths containing step definitions AND hooks
    // Cucumber scans these packages for @Given/@When/@Then/@Before/@After methods
    glue = {
        "com.example.stepdefs",
        "com.example.hooks"
    },

    // ── tags ──────────────────────────────────────────────────────────────────
    // Default tag filter (overridden by -Dcucumber.filter.tags on command line)
    tags = "not @skip and not @wip",

    // ── plugin ────────────────────────────────────────────────────────────────
    // Reporters — can have multiple
    plugin = {
        "pretty",                                             // Colorized console output with step details
        "html:target/cucumber-reports/cucumber.html",         // Standard HTML report
        "json:target/cucumber-reports/cucumber.json",         // JSON (for CI/Masterthought/Allure)
        "junit:target/cucumber-reports/cucumber.xml",         // JUnit XML (for Jenkins test trends)
        "rerun:target/cucumber-reports/failed-scenarios.txt", // Saves failed scenario paths for rerun
        "timeline:target/cucumber-reports/timeline",          // Timeline report showing parallel execution
    },

    // ── monochrome ────────────────────────────────────────────────────────────
    // true = cleaner console output (no ANSI color codes)
    // Useful in CI environments where color codes appear as garbage
    monochrome = true,

    // ── publish ───────────────────────────────────────────────────────────────
    // true = publish results to Cucumber Reports cloud (reports.cucumber.io)
    publish = false,

    // ── dryRun ────────────────────────────────────────────────────────────────
    // true = checks step definitions are found WITHOUT running tests
    // Useful for verifying new scenarios are mapped before running the full suite
    dryRun = false
)
public class CucumberRunner extends AbstractTestNGCucumberTests {

    // Override to enable parallel execution at the scenario level
    // parallel = true runs multiple scenarios simultaneously using TestNG threads
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

```xml
<!-- testng.xml — configure thread count for parallel execution -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.0.dtd">
<suite name="BDD Test Suite" verbose="1" data-provider-thread-count="4">
  <test name="All BDD Tests">
    <classes>
      <class name="com.example.runner.CucumberRunner"/>
    </classes>
  </test>
</suite>
```

---

**Q18: List all CucumberOptions and explain what each one does.**

**A:**

```java
@CucumberOptions(
    // features — where to find .feature files
    // String or String[] — relative to project root
    features = {"src/test/resources/features"},

    // glue — packages containing step definitions and hooks
    // Must include ALL packages with @Given/@When/@Then/@Before/@After methods
    glue = {"com.example.stepdefs", "com.example.hooks"},

    // tags — tag expression to filter which scenarios to run
    // Overridden at runtime: -Dcucumber.filter.tags="@smoke"
    tags = "not @wip",

    // plugin — list of formatters/reporters
    plugin = {
        "pretty",                                   // Console — color, step details
        "html:target/reports/cucumber.html",         // HTML file
        "json:target/reports/cucumber.json",         // JSON (for downstream tools)
        "junit:target/reports/cucumber.xml",         // JUnit XML
        "rerun:target/reports/rerun.txt",            // Failed scenarios file
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm", // Allure (if dependency added)
        "usage:target/reports/usage.json",           // Step usage statistics
    },

    // monochrome — strip ANSI color codes from console output
    monochrome = true,   // Use in CI; false for local colored output

    // dryRun — check step bindings exist WITHOUT executing steps
    // true = only reports undefined/pending steps, does not run anything
    dryRun = false,

    // publish — publish to Cucumber Reports cloud service
    publish = false,

    // snippets — code style for undefined step suggestions
    // CAMEL_CASE (default) or UNDERSCORE
    snippets = io.cucumber.core.options.SnippetType.CAMELCASE,

    // name — run scenarios whose name matches the regex
    // Alternative to tags for filtering by name
    // name = {".*login.*", ".*checkout.*"},

    // objectFactory — dependency injection framework
    // Default: PicoContainer if cucumber-picocontainer is on classpath
    // objectFactory = io.cucumber.picocontainer.PicoFactory.class,
)
```

---

**Q19: How do you share state between multiple step definition classes?**

**A:** Cucumber creates step definition classes per scenario. When you split steps across multiple classes (LoginStepDefs, CartStepDefs, etc.), they run in the same scenario but are separate instances. Sharing state between them requires dependency injection.

The problem:

```java
// LoginStepDefs creates a user via API — sets the userId
// CartStepDefs needs that userId to add items to the cart
// They are different classes — how does CartStepDefs get the userId?
```

Solution using PicoContainer:

```java
// Step 1: Create a shared context class (plain Java class — no annotation needed)
// src/test/java/com/example/context/ScenarioContext.java
package com.example.context;

public class ScenarioContext {
    // Shared state between step definition classes in the same scenario
    public String createdUserId;
    public String authToken;
    public String createdOrderId;
    public String currentProductName;

    // Can also hold domain objects
    public User loggedInUser;
    public List<String> addedProductIds = new ArrayList<>();

    // Reset between scenarios — PicoContainer creates a new instance per scenario,
    // so this happens automatically. No manual reset needed.
}

// Step 2: Inject ScenarioContext via constructor in EACH step class
// src/test/java/com/example/stepdefs/UserStepDefs.java
public class UserStepDefs {
    private final ScenarioContext context;

    // PicoContainer sees the constructor parameter and injects the same
    // ScenarioContext instance used by ALL step classes in this scenario
    public UserStepDefs(ScenarioContext context) {
        this.context = context;
    }

    @When("a new user account is created")
    public void createUserAccount() {
        String userId = userApi.createUser("test@example.com", "Password123!");
        context.createdUserId = userId;  // Store for other step classes
        System.out.println("Created user: " + userId);
    }
}

// src/test/java/com/example/stepdefs/CartStepDefs.java
public class CartStepDefs {
    private final ScenarioContext context;

    public CartStepDefs(ScenarioContext context) {
        this.context = context;
    }

    @Then("the user has an empty cart")
    public void theUserHasEmptyCart() {
        // Uses the userId set by UserStepDefs — same ScenarioContext instance
        String userId = context.createdUserId;
        Cart cart = cartApi.getCart(userId);
        Assert.assertTrue(cart.isEmpty(), "Cart for user " + userId + " should be empty");
    }
}
```

PicoContainer dependency in pom.xml:

```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-picocontainer</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
</dependency>
```

---

**Q20: Explain PicoContainer dependency injection in Cucumber. How does it work internally?**

**A:** PicoContainer is a lightweight dependency injection container. When `cucumber-picocontainer` is on the classpath, Cucumber automatically uses it to manage step definition class lifecycles.

How it works:

```
1. For each new scenario, PicoContainer creates a NEW container
2. Cucumber scans step definition classes in the glue packages
3. For each step class, PicoContainer inspects its constructor
4. If the constructor has parameters, PicoContainer resolves them:
   - If the parameter type is another class (like ScenarioContext or WebDriver),
     PicoContainer creates ONE instance of it and injects IT into ALL step classes
     that declare it in their constructor
5. This guarantees: all step classes in ONE scenario share THE SAME instance
6. After the scenario ends, the container is discarded — a fresh one is made for the next scenario
```

```java
// WebDriver wrapper — shared via PicoContainer
// src/test/java/com/example/context/DriverContext.java
public class DriverContext {

    private WebDriver driver;

    public void initDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,720");
        this.driver = new ChromeDriver(options);
        this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}

// Hook class — also injected
public class BrowserHooks {
    private final DriverContext driverContext;

    public BrowserHooks(DriverContext driverContext) {
        this.driverContext = driverContext;
    }

    @Before
    public void startBrowser() {
        driverContext.initDriver();
    }

    @After
    public void stopBrowser(Scenario scenario) {
        if (scenario.isFailed()) {
            // Take screenshot using driverContext.getDriver()
        }
        driverContext.quitDriver();
    }
}

// Step class — same DriverContext instance injected
public class LoginStepDefs {
    private final DriverContext driverContext;
    private final ScenarioContext scenarioContext;

    public LoginStepDefs(DriverContext driverContext, ScenarioContext scenarioContext) {
        this.driverContext = driverContext;
        this.scenarioContext = scenarioContext;
    }

    @Given("the login page is open")
    public void openLoginPage() {
        // Same WebDriver instance started by BrowserHooks.startBrowser()
        WebDriver driver = driverContext.getDriver();
        driver.get("https://example.com/login");
    }
}
```

Anti-patterns to avoid:
- **Static fields** for shared state — causes thread-safety issues with parallel execution and data leakage between scenarios
- **Singleton pattern** for WebDriver — same problem as static fields
- **Passing objects via scenario.getId()** lookups — fragile and unnecessary when PicoContainer is available

---

## SECTION 5: SERENITY BDD

---

**Q21: What is Serenity BDD? What does it add over plain Cucumber?**

**A:** Serenity BDD (formerly Thucydides) is a library that sits on top of Cucumber (or JUnit) and adds living documentation reports, a step-library pattern for reusable actions, and requirements management.

| Feature | Plain Cucumber | Serenity BDD + Cucumber |
|---|---|---|
| Report style | HTML table of pass/fail | Rich narrative with screenshots, step descriptions |
| Screenshot capture | Only on failure (with hook) | Every step, automatically |
| Step logging | Manual `System.out.println` | First-class in report with method name |
| Requirements tracking | None | Links scenarios to features/epics; shows % covered |
| Living documentation | Requires extra plugin | Built-in |
| JIRA integration | No | Yes (links to JIRA issues) |
| Page object management | Manual | Serenity injects and manages @Pages |

Maven setup:

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
                    <goals><goal>aggregate</goal></goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

**Q22: What are `@Steps` and `@Step` in Serenity BDD? How do they work?**

**A:** `@Steps` is a Serenity field annotation that injects a Step Library — a class whose methods are automatically logged in the Serenity report with their descriptions and screenshots. `@Step` is a method annotation that defines the human-readable description of what that method does in the report.

How it works: When Serenity sees `@Steps LoginSteps loginSteps`, it creates a proxy of `LoginSteps`. Every call to a method on this proxy is intercepted: Serenity logs the method call (using the `@Step` description), takes a screenshot, and records timing. The real method is then executed. This means the report shows exactly what happened at each step without any additional logging code.

```java
// src/test/java/com/example/steps/LoginSteps.java
package com.example.steps;

import com.example.pages.LoginPage;
import com.example.pages.DashboardPage;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.UIInteractionSteps;

// Extends UIInteractionSteps so Serenity manages the driver automatically
public class LoginSteps extends UIInteractionSteps {

    // Serenity injects page objects — no manual new LoginPage(driver) needed
    LoginPage loginPage;
    DashboardPage dashboardPage;

    // @Step — the string is what appears in the Serenity HTML report
    // {0} is replaced by the first argument value in the report
    @Step("Open the login page at {0}")
    public void openLoginPage(String url) {
        loginPage.open();  // navigates to the page
    }

    @Step("{0} logs in with their credentials")
    public void loginWith(String email, String password) {
        loginPage.fillEmail(email);
        loginPage.fillPassword(password);
        loginPage.clickLoginButton();
    }

    @Step("Verify user is on the dashboard")
    public void verifyOnDashboard() {
        dashboardPage.verifyPageLoaded();
    }

    @Step("Verify welcome message shows {0}")
    public void verifyWelcomeMessage(String expectedMessage) {
        String actualMessage = dashboardPage.getWelcomeMessage();
        assert actualMessage.equals(expectedMessage) :
            "Expected '" + expectedMessage + "' but was '" + actualMessage + "'";
    }

    @Step("Click the {0} menu item")
    public void clickMenuItem(String menuItemName) {
        dashboardPage.clickMenuItem(menuItemName);
    }
}

// src/test/java/com/example/stepdefs/LoginStepDefinitions.java (Serenity version)
package com.example.stepdefs;

import com.example.steps.LoginSteps;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.annotations.Steps;

public class LoginStepDefinitions {

    // @Steps injects LoginSteps and wraps it in a Serenity proxy
    // The proxy intercepts every method call and logs it to the report
    @Steps
    LoginSteps loginSteps;

    @Given("the login page is open")
    public void theLoginPageIsOpen() {
        loginSteps.openLoginPage("https://example.com/login");
        // Serenity logs: "Open the login page at https://example.com/login" + screenshot
    }

    @When("the user logs in with email {string} and password {string}")
    public void theUserLogsIn(String email, String password) {
        loginSteps.loginWith(email, password);
        // Serenity logs: "john@example.com logs in with their credentials" + screenshot
    }

    @Then("the user is on the dashboard")
    public void theUserIsOnTheDashboard() {
        loginSteps.verifyOnDashboard();
        // Serenity logs: "Verify user is on the dashboard" + screenshot
    }

    @Then("the welcome message is {string}")
    public void theWelcomeMessageIs(String expectedMessage) {
        loginSteps.verifyWelcomeMessage(expectedMessage);
    }
}
```

---

**Q23: How do you run a Serenity BDD test? What command generates the Serenity report?**

**A:**

```java
// SerenityRunner.java — use CucumberWithSerenity instead of standard TestNG runner
package com.example.runner;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.example.stepdefs", "com.example.hooks"},
    plugin = {"pretty"},        // Serenity adds its own reporters automatically
    tags = "not @skip"
)
public class SerenityRunner {
    // No body — annotations drive everything
}
```

```bash
# Run tests and generate Serenity report in one command
mvn clean verify

# What 'verify' does:
# 1. clean — delete target/ directory
# 2. compile — compile test sources
# 3. test — run tests (generates raw Serenity data in target/site/serenity/)
# 4. post-integration-test — serenity:aggregate plugin generates HTML report

# Report location:
# target/site/serenity/index.html

# Run only @smoke tagged scenarios
mvn clean verify -Dcucumber.filter.tags="@smoke"

# Run on specific environment
mvn clean verify -Denvironment=staging

# Skip compilation, just regenerate report from existing data
mvn serenity:aggregate
```

Serenity report sections:
- **Requirements**: Shows all features and stories with pass/fail/pending counts, percentage covered
- **Test Results**: All scenarios with step-by-step narrative, screenshots, timing
- **Errors**: Grouped failure analysis
- **Tags**: Test results by tag
- **History**: Trend of pass/fail over time (if running in CI with history)

---

**Q24: How does the Serenity report differ from the standard Cucumber HTML report?**

**A:**

| Aspect | Cucumber HTML Report | Serenity Report |
|---|---|---|
| Location | `target/cucumber-reports/cucumber.html` | `target/site/serenity/index.html` |
| Generated by | Cucumber built-in HTML plugin | `serenity:aggregate` Maven goal |
| Content | Table of feature/scenario pass/fail | Rich narrative with screenshots at each step |
| Screenshots | Only if manually attached in @After | Automatic at every @Step |
| Requirements view | None | Feature → Story → Scenario hierarchy with coverage % |
| Step descriptions | Step text from feature file | Step text + @Step descriptions from Java |
| Charts | None | Pie charts, bar charts, trend lines |
| JIRA linking | No | Yes — links to JIRA story IDs |
| Living documentation | No | Yes — readable by business stakeholders |
| Filtering | None | By tag, by date, by requirement, by result |

When to use Serenity over vanilla Cucumber report:
- When business stakeholders need to view test results and understand what was tested
- When you need requirements coverage metrics
- When you need per-step screenshots without manually coding them
- When the project connects to JIRA for story management

When vanilla Cucumber HTML is sufficient:
- Developer-only audiences
- Quick feedback during development
- Projects without Serenity on the classpath

---

## SECTION 6: GAUGE FRAMEWORK

---

**Q25: What is Gauge? How does it differ from Cucumber at a high level?**

**A:** Gauge is an open-source test automation framework created by ThoughtWorks. It uses Markdown `.spec` files instead of Gherkin `.feature` files. Unlike Cucumber, Gauge does not enforce the Given/When/Then structure — steps are free-form English sentences written in Markdown.

```
Cucumber uses:                     Gauge uses:
  Feature: ...                       # Feature title (Markdown heading)
  Scenario: ...                      ## Scenario title
  Given the login page is open       * The login page is open
  When the user enters email "x"     * The user enters email "x"
  Then the dashboard is displayed    * The dashboard is displayed
```

Key Gauge concepts:
- `.spec` files: Markdown-format test specifications
- `.cpt` files: Concept files — reusable step sequences (like a named macro)
- `DataStore`: Built-in state sharing between steps (no PicoContainer needed)
- `@Step("text")`: The only step annotation — no @Given/@When/@Then
- Built-in parallel execution with `gauge run --parallel --n=4`

```bash
# Install Gauge
npm install -g @getgauge/cli
brew install gauge           # macOS

# Install Java plugin
gauge install java

# Initialize Java project
gauge init java

# Verify
gauge version
```

---

**Q26: Compare Gauge and Cucumber in detail. When would you choose one over the other?**

**A:**

| Aspect | Cucumber | Gauge |
|---|---|---|
| Spec format | Gherkin (.feature) | Markdown (.spec) |
| Keywords required | Given/When/Then strictly required | Free-form — no keywords |
| Step binding | `@Given`, `@When`, `@Then` | `@Step` only |
| Reusable step groups | Background (file-scoped) + Hooks | Concepts (.cpt) — project-wide |
| State sharing | PicoContainer / Spring | DataStore (built-in: scenario, spec, suite level) |
| Parallel execution | Via TestNG DataProvider or Surefire | Built-in: `--parallel --n=4` |
| Reporting | Cucumber HTML / Allure | Built-in HTML at `reports/html-report/` |
| Data-driven tests | Scenario Outline + Examples | `<table:file.csv>` or inline tables |
| IDE support | IntelliJ, Eclipse (Cucumber plugin) | IntelliJ, VS Code (Gauge plugin) |
| Ecosystem maturity | Larger, more integrations | Smaller but growing |
| Business readability | High (Gherkin is very structured) | High (Markdown is familiar) |

Choose Gauge when:
- The team dislikes the Given/When/Then rigidity
- Specs should read more like natural documentation
- Built-in parallel execution is needed without configuration
- Team is familiar with Markdown
- Built-in DataStore is preferred over PicoContainer DI

Choose Cucumber when:
- Stakeholders already know Gherkin
- You need Serenity BDD integration
- The project uses JUnit/TestNG runners with existing CI configuration
- Team already has Cucumber expertise
- Richer plugin ecosystem is required (Allure, Masterthought, etc.)

---

**Q27: Show a complete Gauge `.spec` file. How does it differ from a Gherkin `.feature` file?**

**A:**

```markdown
# User Authentication
Tags: authentication, core

This specification covers the login functionality for the MyApp application.
Regular Markdown text is allowed and is treated as documentation.

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
* An error message "Invalid credentials. Please try again." is displayed
* The user remains on the login page

## Login with Multiple User Types
Tags: regression, data-driven

* The login page is open
* The user logs in with email <email> and password <password>
* The user is redirected to <expected_page>

|email                 |password    |expected_page|
|----------------------|------------|-------------|
|admin@example.com     |Admin123!   |/admin       |
|manager@example.com   |Manager456! |/manager     |
|employee@example.com  |Emp789!     |/dashboard   |

## Login Validates Email Format
Tags: regression, validation

* The login page is open
* The user enters email "notanemail"
* The user clicks the "Sign In" button
* The email validation error "Please enter a valid email address" is displayed

## Account Locks After Repeated Failures
Tags: regression, security

* The login page is open
* The user attempts login with wrong password "5" times
* The account lock message is displayed
* The user cannot log in even with correct credentials
```

Key differences from Gherkin:
- The heading `# Feature Title` uses Markdown heading syntax — not the `Feature:` keyword
- `## Scenario Title` uses `##` for scenarios — not the `Scenario:` keyword
- Steps use `*` (Markdown list bullets) — not `Given`, `When`, `Then`
- Tags are on a line starting with `Tags:` not `@tag` directly above
- No `Background` keyword — use Concepts (`.cpt` files) instead
- Data-driven tables are inline Markdown tables — not `Examples:` tables

---

**Q28: How do you implement steps in Java for a Gauge spec file?**

**A:**

```java
// src/test/java/com/example/steps/LoginStepImpl.java
package com.example.steps;

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.datastore.ScenarioDataStore;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;

public class LoginStepImpl {

    private WebDriver driver;

    @BeforeScenario
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,720");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        // Store driver in DataStore for use by other step classes
        ScenarioDataStore.put("driver", driver);
    }

    @AfterScenario
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // @Step maps the Java method to step text in .spec files
    // The text must match EXACTLY (case-insensitive by default)
    @Step("The login page is open")
    public void openLoginPage() {
        driver.get("https://example.com/login");
    }

    // <email> in @Step matches <email> placeholder in .spec file
    // Gauge passes the value as a method parameter
    @Step("The user enters email <email>")
    public void enterEmail(String email) {
        driver.findElement(By.id("email")).sendKeys(email);
    }

    @Step("The user enters password <password>")
    public void enterPassword(String password) {
        driver.findElement(By.id("password")).sendKeys(password);
    }

    @Step("The user clicks the <buttonName> button")
    public void clickButton(String buttonName) {
        // Find button by text
        driver.findElement(
            By.xpath("//button[contains(text(),'" + buttonName + "')]")
        ).click();
    }

    @Step("The user is redirected to the dashboard")
    public void verifyDashboard() {
        Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
            "URL should contain /dashboard. Actual: " + driver.getCurrentUrl());
    }

    @Step("The welcome message displays <message>")
    public void verifyWelcomeMessage(String expectedMessage) {
        String actualMessage = driver.findElement(
            By.cssSelector("[data-testid='welcome-banner']")
        ).getText();
        Assert.assertEquals(actualMessage, expectedMessage,
            "Welcome message does not match");
    }

    @Step("An error message <message> is displayed")
    public void verifyErrorMessage(String expectedMessage) {
        String actualError = driver.findElement(
            By.cssSelector("[data-testid='error-message']")
        ).getText();
        Assert.assertTrue(actualError.contains(expectedMessage),
            "Expected error containing '" + expectedMessage + "' but got: '" + actualError + "'");
    }

    // Combined step — shortcut combining multiple actions
    @Step("The user logs in with email <email> and password <password>")
    public void loginWithCredentials(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickButton("Sign In");
    }

    @Step("The user attempts login with wrong password <times> times")
    public void attemptLoginMultipleTimes(String times) {
        int count = Integer.parseInt(times);
        for (int i = 0; i < count; i++) {
            driver.get("https://example.com/login");
            enterEmail("locked@example.com");
            enterPassword("wrongpass" + i);
            clickButton("Sign In");
        }
    }

    @Step("The account lock message is displayed")
    public void verifyAccountLocked() {
        String errorText = driver.findElement(
            By.cssSelector("[data-testid='account-locked']")
        ).getText();
        Assert.assertTrue(errorText.contains("locked") || errorText.contains("Account locked"),
            "Account lock message not found");
    }

    @Step("The email validation error <message> is displayed")
    public void verifyEmailValidationError(String expectedError) {
        String actualError = driver.findElement(
            By.cssSelector("[data-testid='email-error']")
        ).getText();
        Assert.assertEquals(actualError, expectedError);
    }

    @Step("The user remains on the login page")
    public void verifyOnLoginPage() {
        Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
            "Should be on login page but URL is: " + driver.getCurrentUrl());
    }
}
```

---

**Q29: What are Gauge Concepts (`.cpt` files)? How do you use them?**

**A:** Concepts are Gauge's answer to reusable step sequences. A concept is a named macro that expands to multiple lower-level steps when used in a spec. They are stored in `.cpt` files anywhere in the specs directory.

```markdown
<!-- specs/concepts/auth.cpt -->

# Log in as a valid user
* The login page is open
* The user enters email "john.doe@example.com"
* The user enters password "SecurePass123!"
* The user clicks the "Sign In" button
* The user is redirected to the dashboard

# Log in as admin user
* The login page is open
* The user enters email "admin@company.com"
* The user enters password "Admin123!"
* The user clicks the "Sign In" button
* The admin dashboard is displayed

# Log in as user with email <email> and password <password>
* The login page is open
* The user enters email <email>
* The user enters password <password>
* The user clicks the "Sign In" button
```

Using concepts in a spec:

```markdown
## Add Product to Cart (uses concept)
Tags: smoke

* Log in as a valid user        ← Expands to all 5 steps in the concept
* The user navigates to "Products"
* The user clicks "Add to Cart" for "Laptop Pro"
* The cart shows 1 item

## Admin Creates Product (uses parameterized concept)
Tags: regression

* Log in as user with email "admin@company.com" and password "Admin123!"
* The admin navigates to the product management page
* The admin creates product "New Laptop" priced at "$599.99"
```

Concepts vs Background:
- Background is scoped to one `.feature` file
- Concepts are available across ALL spec files in the project
- Concepts can call other concepts (nested concepts)
- Concepts can be parameterized (take arguments)
- Background steps run as separate steps in the report; concepts show as the concept name

---

## SECTION 7: MAVEN COMMANDS

---

**Q30: What are all the Maven commands for running Cucumber tests with different configurations?**

**A:**

```bash
# ── Basic run ──────────────────────────────────────────────────────────────────
mvn test                                      # Run all scenarios (using testng.xml or default runner)
mvn clean test                                # Clean build artifacts first, then run
mvn test -q                                   # Quiet mode — suppress most Maven output

# ── Tag filtering ──────────────────────────────────────────────────────────────
mvn test -Dcucumber.filter.tags="@smoke"
mvn test -Dcucumber.filter.tags="@regression and not @slow"
mvn test -Dcucumber.filter.tags="@smoke or @critical"
mvn test -Dcucumber.filter.tags="not @wip and not @skip"
mvn test -Dcucumber.filter.tags="(@smoke or @sanity) and not @flaky"

# ── Feature file filtering ─────────────────────────────────────────────────────
mvn test -Dcucumber.features="src/test/resources/features/login.feature"
mvn test -Dcucumber.features="src/test/resources/features/login.feature:25"  # Specific line
mvn test -Dcucumber.features="src/test/resources/features/checkout"          # Subdirectory

# ── Environment and configuration ─────────────────────────────────────────────
mvn test -Denv=staging -Dbase.url=https://staging.example.com
mvn test -Dbrowser=firefox
mvn test -Dheadless=true
mvn test -DbaseUrl=https://qa.example.com

# ── TestNG suite ───────────────────────────────────────────────────────────────
mvn test -DsuiteXmlFile=testng-smoke.xml
mvn test -DsuiteXmlFile=testng-regression.xml
mvn test -DsuiteXmlFile=src/test/resources/testng.xml

# ── Parallel execution ─────────────────────────────────────────────────────────
mvn test -Dthreads=4                          # Thread count (must also configure in testng.xml)
mvn test -Dsurefire.threadCount=4

# ── Dry run (check step bindings exist without running) ───────────────────────
mvn test -Dcucumber.execution.dry-run=true

# ── Rerun failed scenarios ─────────────────────────────────────────────────────
# First run generates target/cucumber-reports/failed-scenarios.txt
mvn test
# Rerun only the failed ones:
mvn test -Dcucumber.features="@target/cucumber-reports/failed-scenarios.txt"
# Or use surefire rerun:
mvn test -Dsurefire.rerunFailingTestsCount=2  # Retry each failing test up to 2 times

# ── Serenity-specific ──────────────────────────────────────────────────────────
mvn clean verify                              # Run tests AND generate Serenity report
mvn clean verify -Dcucumber.filter.tags="@smoke"
mvn serenity:aggregate                        # Regenerate report without re-running tests
mvn verify -DskipTests=true serenity:aggregate # Skip tests, just rebuild report

# ── Skip tests ────────────────────────────────────────────────────────────────
mvn install -DskipTests                       # Build without running tests
mvn install -Dmaven.test.skip=true            # Skip compilation AND execution of tests

# ── Specific test class ────────────────────────────────────────────────────────
mvn test -Dtest=CucumberRunner                # Run specific runner class
mvn test -Dtest=LoginTest                     # Run specific TestNG test class
mvn test -Dtest=LoginTest#testValidLogin      # Run specific test method
```

---

**Q31: What are all the Gauge commands for running specs?**

**A:**

```bash
# ── Basic run ──────────────────────────────────────────────────────────────────
gauge run specs/                              # Run all specs
gauge run specs/login.spec                    # Run specific spec file
gauge run specs/login.spec:25                 # Run scenario starting at line 25

# ── Tag filtering ──────────────────────────────────────────────────────────────
gauge run --tags="smoke" specs/
gauge run --tags="smoke,regression" specs/    # AND — both tags required
gauge run --tags="smoke || regression" specs/ # OR — either tag
gauge run --tags="!wip" specs/               # NOT — exclude @wip

# ── Parallel execution ──────────────────────────────────────────────────────────
gauge run --parallel specs/                   # Default parallel (auto thread count)
gauge run --parallel --n=4 specs/            # 4 parallel threads
gauge run --parallel --group=2 specs/        # Group specs into 2 groups and run parallel

# ── Environment ───────────────────────────────────────────────────────────────
gauge run --env=staging specs/               # Use staging environment config
gauge run --env=production specs/            # Use production config

# ── Reporting ─────────────────────────────────────────────────────────────────
gauge run specs/                             # Report at: reports/html-report/index.html
gauge run --html-report-dir=my-reports/ specs/ # Custom report directory

# ── Rerun failed ──────────────────────────────────────────────────────────────
gauge run specs/                             # Run all
gauge run --failed specs/                   # Rerun only scenarios that failed last run

# ── Dry run ───────────────────────────────────────────────────────────────────
gauge run --dry-run specs/                  # Validate steps are implemented, don't run

# ── List specs and steps ──────────────────────────────────────────────────────
gauge list --specs                           # List all spec files
gauge list --scenarios                       # List all scenarios
gauge list --tags                            # List all tags used in specs
gauge docs specs/                            # Generate documentation from specs

# ── Plugin management ─────────────────────────────────────────────────────────
gauge install java                           # Install Java plugin
gauge install html-report                    # Install HTML report plugin
gauge update java                            # Update Java plugin
gauge plugin list                            # List installed plugins
gauge version                                # Show Gauge and plugin versions
```

---

## SECTION 8: TROUBLESHOOTING

---

**Q32: How do you fix an "Undefined step" error in Cucumber?**

**A:**

```
Error: io.cucumber.junit.UndefinedStepException:
The step "the user enters email "john@example.com"" is undefined.

Implement the step with this snippet:
    @When("the user enters email {string}")
    public void theUserEntersEmail(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
```

Causes and fixes in order of frequency:

```java
// CAUSE 1: Wrong 'glue' path in CucumberOptions
// Your step definition is in com.example.steps but glue says com.example.stepdefs
@CucumberOptions(
    glue = {"com.example.stepdefs", "com.example.hooks"}  // Must match actual package
)
// Fix: update glue to include ALL packages with step definitions

// CAUSE 2: Step text does not exactly match the annotation
// Feature:    the user enters email "john@example.com"
// Annotation: @When("the user enters email{string}")   ← MISSING space before {string}
// Fix:
@When("the user enters email {string}")                  // Space before {string}

// CAUSE 3: Quoted vs unquoted mismatch
// {string} requires double quotes in the feature file
// Feature:    the user enters email john@example.com     ← no quotes
// Annotation: @When("the user enters email {string}")    ← expects quoted
// Fix: either add quotes in feature:
//   the user enters email "john@example.com"
// Or change annotation to use {word}:
@When("the user enters email {word}")

// CAUSE 4: Wrong import for @When
// Using io.cucumber.junit.Before instead of io.cucumber.java.en.When
import io.cucumber.java.en.When;    // Correct
import io.cucumber.java.en.Given;   // Correct
// NOT: import io.cucumber.junit.CucumberOptions (that's for the runner)

// CAUSE 5: Missing maven dependency
// cucumber-java artifact must be on test classpath
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.14.0</version>
    <scope>test</scope>
</dependency>

// Use Cucumber's console suggestion — it prints the exact snippet to copy-paste
```

---

**Q33: How do you fix an "Ambiguous step definition" error?**

**A:**

```
Error: io.cucumber.ambiguity.AmbiguousStepDefinitionsException:
"the user clicks the Login button" is ambiguous. It matches:
  @When("the user clicks the {string} button") in LoginStepDefs.java:45
  @When("the user clicks the Login button") in LoginStepDefs.java:52
```

Cause: Two step definition methods both match the same step text from the feature file.

```java
// Problem: both match "the user clicks the Login button"
@When("the user clicks the {string} button")    // Matches: "Login" captured as string
public void clickButton(String name) { ... }

@When("the user clicks the Login button")       // Exact text match — also matches
public void clickLoginButton() { ... }

// Fix 1: Remove the more specific one and use only the parameterized version
@When("the user clicks the {string} button")
public void clickButton(String name) {
    switch (name) {
        case "Login":
        case "Sign In":
            loginPage.clickLoginButton();
            break;
        case "Register":
            registrationPage.clickRegisterButton();
            break;
        default:
            driver.findElement(By.xpath("//button[text()='" + name + "']")).click();
    }
}

// Fix 2: Make patterns non-overlapping
@When("the user clicks the {string} button")    // For all OTHER buttons
public void clickButton(String name) { ... }

// Delete the exact-match method entirely — it is now covered by the parameterized one

// Fix 3: If both must exist, use regex anchors to prevent overlap
@When("^the user clicks the \"([^\"]+)\" button$")  // Requires explicit quotes in feature
@When("^the user clicks the Login button$")          // Only matches exact text with no quotes
```

---

**Q34: How do you fix Cucumber version conflicts?**

**A:**

```
Error: NoClassDefFoundError: io/cucumber/java/en/Given
OR
Error: ClassNotFoundException: io.cucumber.core.internal.gherkin.Parser
OR
Unexpected behaviour where steps are not found despite correct glue path
```

Root cause: Different Cucumber artifacts on the classpath are different versions (e.g., `cucumber-java` is 7.14 but `cucumber-testng` is 6.11).

```xml
<!-- Fix: Use the cucumber-bom (Bill of Materials) to lock all versions -->
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

<!-- Declare Cucumber dependencies WITHOUT version — bom manages them -->
<dependencies>
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-java</artifactId>
        <scope>test</scope>
        <!-- No <version> — bom provides it -->
    </dependency>
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-testng</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.cucumber</groupId>
        <artifactId>cucumber-picocontainer</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Diagnostic command:

```bash
# Check what version of each Cucumber artifact is actually being used
mvn dependency:tree | grep cucumber

# Output shows all Cucumber artifacts and their resolved versions
# Look for version mismatches
```

Common mistake: Adding both `cucumber-junit` and `cucumber-testng` dependencies. These are separate runner integrations — include only the one matching your test runner.

---

**Q35: Why are Background steps not running for some scenarios? How do you fix cross-file setup?**

**A:** `Background` is scoped to the feature file it is defined in. If you have 10 feature files and want common setup to run before scenarios in ALL of them, Background in one file does not help the others.

```gherkin
# login.feature — Background here
Feature: Login
  Background:
    Given the login page is open  ← Runs for every scenario in login.feature ONLY

# checkout.feature — NO Background
Feature: Checkout
  # The Background from login.feature does NOT run here
  Scenario: Checkout fails with empty cart
    # No automatic setup
```

Fix: Use `@Before` hooks in Java for cross-file setup:

```java
// Runs before EVERY scenario in EVERY feature file
@Before
public void openBrowser(Scenario scenario) {
    driver = new ChromeDriver();
}

// Runs before every scenario tagged @needs-login in any feature file
@Before("@needs-login")
public void loginBeforeScenario(Scenario scenario) {
    driver.get("https://example.com/login");
    driver.findElement(By.id("email")).sendKeys("user@example.com");
    driver.findElement(By.id("password")).sendKeys("Password123!");
    driver.findElement(By.cssSelector("button[type='submit']")).click();
    // Wait for dashboard
    new WebDriverWait(driver, Duration.ofSeconds(10)).until(
        ExpectedConditions.urlContains("/dashboard")
    );
}
```

Use Background for: setup that truly applies to all scenarios in one feature file.
Use `@Before` hooks for: cross-file setup or conditional setup based on tags.

---

## SECTION 9: INTERVIEW Q&A

---

**Q36: An interviewer asks: "What is BDD and how is it different from TDD? Which one replaces the other?"**

**A:** Neither replaces the other — they operate at different levels and complement each other.

TDD is a developer discipline: write a failing unit test, write the minimum code to make it pass, refactor. Tests are technical (Java/Python code), understood only by developers, and focus on individual functions and classes in isolation. The goal is clean, testable code design.

BDD is a team practice: express system behaviour as human-readable Gherkin scenarios before development begins. Scenarios are written collaboratively by business, developers, and QA in the Three Amigos session. They operate at the feature/acceptance level, are understood by the whole team, and focus on observable system behaviour from a user perspective.

A production-quality project uses both: BDD at the acceptance level (Cucumber feature files passing end-to-end tests), TDD at the unit level (JUnit tests covering each service method). The BDD scenarios define what the system should do; TDD ensures each component is implemented correctly.

---

**Q37: "Explain the difference between Scenario Outline with Examples and a data table in a Scenario step. Show when you would use each."**

**A:** `Scenario Outline` + `Examples` creates multiple independent scenarios — one per row. Each scenario gets its own Before/After hook execution, its own browser session, and appears as a separate test result in reports. Use it when you want to run the same user journey with different input combinations, each as an independent, separately-reported test case (e.g., testing login with admin, manager, and employee accounts).

A data table inside a regular `Scenario` passes structured data to a single step as a `DataTable` parameter. It does not create multiple scenarios — it's one scenario that processes the table. Use it when a single action works on a collection of data (e.g., creating 5 products in one batch operation, filling a multi-field form in one step).

The practical rule: if each row represents a complete, independent user story (start to finish), use Scenario Outline. If the table is just structured input to one action within a single story, use a data table.

---

**Q38: "Walk me through how state is shared between step definition classes in Cucumber."**

**A:** Cucumber creates a new instance of each step definition class for every scenario. When steps are split across multiple classes (LoginStepDefs, CartStepDefs, CheckoutStepDefs), they need to share objects like WebDriver, auth tokens, or IDs created in earlier steps.

The correct solution is PicoContainer dependency injection. You create a shared context class (a plain Java class with public fields) and inject it into all step definition classes via their constructors. PicoContainer sees the constructor parameter type, creates one instance of the context class, and injects the same instance into every step class in the same scenario. When the scenario ends, PicoContainer discards the container and creates fresh instances for the next scenario.

The anti-pattern is static fields or singletons — these leak state between scenarios and cause issues in parallel execution because static state is shared across threads.

---

**Q39: "What does Serenity BDD add over vanilla Cucumber and when is the additional complexity worth it?"**

**A:** Serenity adds: rich living documentation reports that show per-step screenshots and narrative descriptions readable by business stakeholders; the `@Steps` injection pattern that logs every action in the report without manual logging; requirements coverage metrics showing what percentage of features are covered by passing tests; and JIRA/Xray integration for requirement tracing.

The complexity is worth it when: the project has business stakeholders who need to review test results; when requirements traceability is a compliance or audit requirement; when screenshots at every step are needed for debugging (saves time diagnosing CI failures); or when the team uses Serenity's screenplay pattern for more maintainable test architecture.

It is not worth the complexity when: the team is small, the report audience is only developers, there is no requirement tracking need, or the Serenity compilation step adds unacceptable overhead to the CI pipeline.

---

**Q40: "What is Gauge and when would your team choose it over Cucumber?"**

**A:** Gauge is a test automation framework from ThoughtWorks that uses Markdown `.spec` files instead of Gherkin. Steps are free-form English sentences — no Given/When/Then keywords required. Step bindings use only `@Step("text")` annotation. Reusable step sequences are defined in Concept files (`.cpt`) which work project-wide. State sharing between steps uses a built-in DataStore rather than PicoContainer. Parallel execution is a first-class built-in feature.

Choose Gauge over Cucumber when: your team finds the Given/When/Then structure too rigid for their writing style; your tests should read more like natural documentation; you want built-in parallel execution without TestNG configuration; your team is already familiar with Markdown; or you want simpler state sharing without PicoContainer setup.

Choose Cucumber when: your stakeholders already understand Gherkin and are involved in writing scenarios; you need Serenity BDD's rich reporting and requirements coverage; your existing infrastructure uses JUnit/TestNG runners; or your team has established Cucumber expertise and tooling.

---

**Q41: "In a Cucumber hook, what is `scenario.isFailed()` and what does `scenario.attach()` do?"**

**A:**

```java
@After
public void afterScenario(Scenario scenario) {
    // scenario.isFailed() — returns true if any step in the scenario threw an exception
    // or failed an assertion. Returns false if all steps passed.
    if (scenario.isFailed()) {
        System.out.println("Scenario failed: " + scenario.getName());

        // scenario.attach(data, mediaType, name)
        // Embeds data inline in the Cucumber HTML report
        // media type tells the report how to render the attachment
        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        scenario.attach(screenshot, "image/png", "Screenshot on Failure");
        // The HTML report will show the screenshot inline when you expand the failing scenario

        // Can also attach text (e.g., API response body, page source)
        String pageSource = driver.getPageSource();
        scenario.attach(pageSource.getBytes(), "text/html", "Page Source on Failure");

        // Or attach plain text log
        scenario.attach("Error occurred on page: " + driver.getCurrentUrl(),
            "text/plain", "Current URL");
    }

    // scenario.getName() — the scenario title from the feature file
    // scenario.getSourceTagNames() — Set<String> of all tags on this scenario
    // scenario.getId() — unique identifier (useful for logging)
    // scenario.getUri() — path to the .feature file
    System.out.println("Tags: " + scenario.getSourceTagNames());
    System.out.println("URI:  " + scenario.getUri());
}
```

---

**Q42: "You have 500 Cucumber scenarios. How would you organize them to run efficiently in CI?"**

**A:** The strategy combines tagging, parallelism, and selective execution:

```
CI Pipeline structure:

Stage 1 — Smoke (< 5 minutes)
  - Run: mvn test -Dcucumber.filter.tags="@smoke"
  - ~20-30 critical path scenarios
  - Runs on every commit to any branch
  - Blocks merge if fails

Stage 2 — Regression (< 30 minutes)
  - Run: mvn test -Dcucumber.filter.tags="@regression and not @slow"
  - Parallel: data-provider-thread-count="6" in testng.xml
  - Runs on every PR merge to main branch
  - Blocks release if fails

Stage 3 — Full Suite (< 60 minutes)
  - Run: mvn test -Dcucumber.filter.tags="not @wip and not @skip"
  - Parallel: 8 threads
  - Runs nightly
  - Results reviewed next morning

Failed scenario rerun:
  - After Stage 2/3, rerun only failed scenarios automatically:
    mvn test -Dcucumber.features="@target/cucumber-reports/failed-scenarios.txt"
  - Or: -Dsurefire.rerunFailingTestsCount=2
```

PicoContainer + ThreadLocal for parallel safety:

```java
// DriverContext must be thread-safe for parallel execution
public class DriverContext {
    // Each thread gets its own WebDriver instance
    private static final ThreadLocal<WebDriver> driverLocal = new ThreadLocal<>();

    public WebDriver getDriver() { return driverLocal.get(); }

    @BeforeScenario  // or @Before in Cucumber
    public void initDriver() {
        WebDriver driver = new ChromeDriver();
        driverLocal.set(driver);
    }

    @AfterScenario  // or @After
    public void quitDriver() {
        if (driverLocal.get() != null) {
            driverLocal.get().quit();
            driverLocal.remove();
        }
    }
}
```

---

**Q43: "Show a complete pom.xml for a Cucumber + TestNG + PicoContainer + Selenium project."**

**A:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>bdd-selenium-tests</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <cucumber.version>7.14.0</cucumber.version>
        <selenium.version>4.15.0</selenium.version>
        <testng.version>7.8.0</testng.version>
        <webdrivermanager.version>5.6.3</webdrivermanager.version>
    </properties>

    <!-- Lock ALL Cucumber artifact versions with BOM -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.cucumber</groupId>
                <artifactId>cucumber-bom</artifactId>
                <version>${cucumber.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>

        <!-- Cucumber core — step definition annotations, Gherkin parser -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-java</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Cucumber + TestNG integration (runner base class) -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-testng</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- PicoContainer — dependency injection between step classes -->
        <dependency>
            <groupId>io.cucumber</groupId>
            <artifactId>cucumber-picocontainer</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Selenium WebDriver -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>${selenium.version}</version>
        </dependency>

        <!-- WebDriverManager — auto-download chromedriver, geckodriver -->
        <dependency>
            <groupId>io.github.bonigarcia</groupId>
            <artifactId>webdrivermanager</artifactId>
            <version>${webdrivermanager.version}</version>
        </dependency>

        <!-- TestNG -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Assertions (AssertJ — fluent assertions) -->
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.24.2</version>
            <scope>test</scope>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.9</version>
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
                    <systemPropertyVariables>
                        <!-- Allow tag filter override: mvn test -Dcucumber.filter.tags="@smoke" -->
                        <cucumber.filter.tags>${cucumber.filter.tags}</cucumber.filter.tags>
                        <browser>${browser}</browser>
                        <env>${env}</env>
                        <base.url>${base.url}</base.url>
                    </systemPropertyVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

---

*End of File 07 — BDD + Cucumber + Gauge Complete Q&A Guide | 43 Questions*
