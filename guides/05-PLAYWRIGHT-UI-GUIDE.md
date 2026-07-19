# Playwright UI Testing — Complete Guide | TypeScript + POM + Cross-Browser

---

## Table of Contents

1. [What is Playwright UI Testing?](#1-what-is-playwright-ui-testing)
2. [Project Setup](#2-project-setup)
3. [playwright.config.ts for UI Testing](#3-playwrightconfigts-for-ui-testing)
4. [All Locators](#4-all-locators)
5. [All Actions](#5-all-actions)
6. [All Assertions](#6-all-assertions)
7. [Page Object Model (POM)](#7-page-object-model-pom)
8. [Handling Dynamic Elements and Waits](#8-handling-dynamic-elements-and-waits)
9. [File Upload and Download](#9-file-upload-and-download)
10. [iFrames](#10-iframes)
11. [Alerts and Dialogs](#11-alerts-and-dialogs)
12. [Multiple Tabs and Windows](#12-multiple-tabs-and-windows)
13. [Screenshots and Visual Comparison](#13-screenshots-and-visual-comparison)
14. [Auth State with storageState](#14-auth-state-with-storagestate)
15. [Cross-Browser Testing](#15-cross-browser-testing)
16. [Data-Driven UI Tests](#16-data-driven-ui-tests)
17. [Running Tests — All Commands](#17-running-tests--all-commands)
18. [Troubleshooting](#18-troubleshooting)
19. [Interview Q&A](#19-interview-qa)

---

## 1. What is Playwright UI Testing?

Playwright UI testing automates user interactions in real browsers (Chromium, Firefox, WebKit/Safari). Unlike Selenium, Playwright:

- Has built-in **auto-waiting** — it waits for elements to be visible and stable before acting
- Has a built-in **test runner** with parallel execution out of the box
- Provides **network interception**, **multi-tab**, **iframe**, and **file upload/download** support natively
- Generates **HTML reports, traces, screenshots, and videos** on failure
- Supports **storageState** to reuse login sessions across tests

---

## 2. Project Setup

```bash
# Create project folder
mkdir playwright-ui-tests && cd playwright-ui-tests
npm init -y

# Install Playwright and browsers
npm init playwright@latest
# Choose: TypeScript, tests/ directory, add GitHub Actions = Yes, install browsers = Yes

# Install additional dependencies
npm install --save-dev dotenv @types/node
```

**Project structure:**

```
playwright-ui-tests/
├── tests/
│   ├── login.spec.ts
│   ├── dashboard.spec.ts
│   └── products.spec.ts
├── pages/
│   ├── BasePage.ts
│   ├── LoginPage.ts
│   └── DashboardPage.ts
├── fixtures/
│   ├── custom-fixtures.ts
│   └── test-data.json
├── utils/
│   └── helpers.ts
├── auth/
│   └── storageState.json    ← Generated; not committed
├── playwright.config.ts
├── .env
└── tsconfig.json
```

---

## 3. playwright.config.ts for UI Testing

```typescript
import { defineConfig, devices } from '@playwright/test';
import * as dotenv from 'dotenv';

dotenv.config();

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 2 : undefined,

  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['json', { outputFile: 'test-results/results.json' }],
  ],

  use: {
    // Base URL — all page.goto('/login') calls resolve against this
    baseURL: process.env.BASE_URL || 'https://demo.playwright.dev/todomvc',

    // Run without visible browser window
    headless: true,

    // Viewport size for all tests
    viewport: { width: 1280, height: 720 },

    // Take screenshot on test failure
    screenshot: 'only-on-failure',

    // Record video on first retry (useful for CI debugging)
    video: 'on-first-retry',

    // Capture trace on first retry (use Playwright Trace Viewer)
    trace: 'on-first-retry',

    // Highlight actions with a colored border (useful for debugging)
    actionTimeout: 15000,

    // Navigation timeout
    navigationTimeout: 30000,

    // Ignore HTTPS errors
    ignoreHTTPSErrors: true,

    // Locale and timezone
    locale: 'en-US',
    timezoneId: 'America/New_York',
  },

  // Global timeout per test
  timeout: 60000,

  // Global expect timeout
  expect: {
    timeout: 10000,
  },

  // Cross-browser projects
  projects: [
    // Desktop browsers
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },

    // Mobile viewports
    {
      name: 'mobile-chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'mobile-safari',
      use: { ...devices['iPhone 12'] },
    },

    // Setup project — runs first, saves auth state
    {
      name: 'setup',
      testMatch: '**/auth.setup.ts',
    },

    // Tests that need authentication — depend on setup
    {
      name: 'authenticated-chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'auth/storageState.json',
      },
      dependencies: ['setup'],
    },
  ],
});
```

---

## 4. All Locators

```typescript
import { test, expect, Page } from '@playwright/test';

test('all locator types demonstration', async ({ page }) => {
  await page.goto('/demo');

  // ── Semantic Locators (preferred — resilient to style changes) ────────────

  // By ARIA role and accessible name
  const submitButton = page.getByRole('button', { name: 'Submit' });
  const loginHeading = page.getByRole('heading', { name: 'Login', level: 1 });
  const emailInput = page.getByRole('textbox', { name: 'Email' });
  const agreeCheckbox = page.getByRole('checkbox', { name: 'I agree to terms' });
  const countryDropdown = page.getByRole('combobox', { name: 'Country' });
  const navLink = page.getByRole('link', { name: 'Dashboard' });
  const table = page.getByRole('table');
  const listItem = page.getByRole('listitem');
  const menuItem = page.getByRole('menuitem', { name: 'Settings' });
  const alertBox = page.getByRole('alert');

  // By label text (form inputs with <label> elements)
  const passwordField = page.getByLabel('Password');
  const firstNameField = page.getByLabel('First Name');

  // By visible text content
  const welcomeMsg = page.getByText('Welcome back!');
  const exactText = page.getByText('Sign In', { exact: true }); // exact match
  const caseInsensitive = page.getByText('SUBMIT', { exact: false });

  // By test ID attribute (data-testid="..." — stable, not tied to style)
  const loginForm = page.getByTestId('login-form');
  const errorMessage = page.getByTestId('error-message');

  // By placeholder text
  const searchField = page.getByPlaceholder('Search products...');
  const emailPlaceholder = page.getByPlaceholder('Enter your email');

  // By alt text (images)
  const logo = page.getByAltText('Company Logo');

  // By title attribute
  const closeBtn = page.getByTitle('Close dialog');

  // ── CSS Selectors ─────────────────────────────────────────────────────────

  const cssById = page.locator('#username');
  const cssByClass = page.locator('.btn-primary');
  const cssByAttribute = page.locator('[data-id="user-123"]');
  const cssComplex = page.locator('form.login-form input[type="password"]');
  const cssChildOf = page.locator('.card > .card-title');
  const cssNthChild = page.locator('li:nth-child(3)');
  const cssContains = page.locator('button:has-text("Delete")');
  const cssHas = page.locator('div:has(> .required-badge)');

  // ── XPath Selectors (use sparingly — fragile to markup changes) ───────────

  const xpathById = page.locator('//input[@id="username"]');
  const xpathByText = page.locator('//button[text()="Submit"]');
  const xpathContainsText = page.locator('//button[contains(text(),"Submit")]');
  const xpathParent = page.locator('//span[@class="error"]/..');
  const xpathSibling = page.locator('//label[@for="email"]/following-sibling::input');

  // ── Chaining Locators (scope within parent) ───────────────────────────────

  const form = page.locator('form#login');
  // All locators below are scoped INSIDE the form
  const usernameInForm = form.getByLabel('Username');
  const passwordInForm = form.getByLabel('Password');
  const submitInForm = form.getByRole('button', { name: 'Sign In' });

  // Inside a table row
  const tableRow = page.getByRole('row', { name: 'John Doe' });
  const editInRow = tableRow.getByRole('button', { name: 'Edit' });
  const deleteInRow = tableRow.getByRole('button', { name: 'Delete' });

  // ── Selecting by index (nth, first, last) ────────────────────────────────

  const allRows = page.getByRole('row');
  const firstRow = allRows.first();
  const lastRow = allRows.last();
  const thirdRow = allRows.nth(2); // 0-indexed

  const allCheckboxes = page.getByRole('checkbox');
  const firstCheckbox = allCheckboxes.first();

  // ── Filtering locators ────────────────────────────────────────────────────

  // Among all buttons, find the one that has text "Save"
  const saveBtn = page.getByRole('button').filter({ hasText: 'Save' });

  // Among all list items, find ones containing an active badge
  const activeItems = page.getByRole('listitem').filter({
    has: page.locator('.active-badge'),
  });

  // ── Combining locators with or() ──────────────────────────────────────────

  const saveOrSubmit = page.getByRole('button', { name: 'Save' })
    .or(page.getByRole('button', { name: 'Submit' }));
});
```

---

## 5. All Actions

```typescript
import { test, expect } from '@playwright/test';

test('all actions demonstration', async ({ page }) => {
  await page.goto('https://demo.playwright.dev/todomvc');

  // ── Text Input ────────────────────────────────────────────────────────────

  // Fill replaces existing content
  await page.getByPlaceholder('What needs to be done?').fill('Buy groceries');

  // Type simulates keystrokes one by one (triggers input events)
  await page.getByLabel('Search').type('playwright', { delay: 50 });

  // Clear a field
  await page.getByLabel('Name').clear();

  // Press a key (Enter, Tab, Backspace, ArrowDown, etc.)
  await page.getByLabel('Search').press('Enter');
  await page.getByLabel('Email').press('Tab');
  await page.keyboard.press('Control+A'); // Select all

  // Type with keyboard shortcut
  await page.keyboard.type('Hello World');

  // ── Clicks ────────────────────────────────────────────────────────────────

  // Single click
  await page.getByRole('button', { name: 'Submit' }).click();

  // Click with modifier key
  await page.getByRole('link', { name: 'Item 3' }).click({ modifiers: ['Control'] });

  // Click at specific coordinates within element
  await page.getByRole('canvas').click({ position: { x: 100, y: 200 } });

  // Double click
  await page.getByRole('cell', { name: 'Edit me' }).dblclick();

  // Right click (context menu)
  await page.getByRole('listitem').first().click({ button: 'right' });

  // Force click (bypasses actionability checks — use sparingly)
  await page.locator('.hidden-button').click({ force: true });

  // Click by position (last resort)
  await page.locator('#map').click({ position: { x: 250, y: 300 } });

  // ── Hover ─────────────────────────────────────────────────────────────────

  await page.getByRole('link', { name: 'Products' }).hover();
  // Dropdown should appear; now click an option
  await page.getByRole('menuitem', { name: 'Electronics' }).click();

  // ── Scroll ────────────────────────────────────────────────────────────────

  // Scroll element into view
  await page.getByTestId('load-more-button').scrollIntoViewIfNeeded();

  // Scroll page by pixels
  await page.mouse.wheel(0, 500); // scroll down 500px

  // Scroll inside a scrollable container
  await page.locator('.scroll-container').evaluate((el) => {
    el.scrollTop = 300;
  });

  // ── Drag and Drop ─────────────────────────────────────────────────────────

  const source = page.getByTestId('draggable-item');
  const target = page.getByTestId('drop-zone');
  await source.dragTo(target);

  // Drag with fine-grained control
  await page.mouse.move(100, 200);
  await page.mouse.down();
  await page.mouse.move(300, 400, { steps: 10 }); // smooth drag
  await page.mouse.up();

  // ── Select Dropdown (select element) ─────────────────────────────────────

  // By visible label
  await page.getByRole('combobox', { name: 'Country' }).selectOption('United States');

  // By value attribute
  await page.locator('select#country').selectOption({ value: 'US' });

  // By index
  await page.locator('select#country').selectOption({ index: 2 });

  // Multiple select
  await page.locator('select#tags').selectOption(['tag1', 'tag2', 'tag3']);

  // ── Checkbox and Radio ────────────────────────────────────────────────────

  // Check a checkbox
  await page.getByRole('checkbox', { name: 'Remember me' }).check();

  // Uncheck
  await page.getByRole('checkbox', { name: 'Remember me' }).uncheck();

  // Set to a specific state
  await page.getByRole('checkbox', { name: 'Subscribe' }).setChecked(true);
  await page.getByRole('checkbox', { name: 'Subscribe' }).setChecked(false);

  // Radio button
  await page.getByRole('radio', { name: 'Female' }).check();
});
```

---

## 6. All Assertions

```typescript
import { test, expect } from '@playwright/test';

test('all UI assertions demonstration', async ({ page }) => {
  await page.goto('https://example.com/login');

  const heading = page.getByRole('heading', { level: 1 });
  const input = page.getByLabel('Email');
  const button = page.getByRole('button', { name: 'Login' });
  const checkbox = page.getByRole('checkbox', { name: 'Remember me' });
  const links = page.getByRole('link');
  const errorDiv = page.getByTestId('error-msg');

  // ── URL and Title ──────────────────────────────────────────────────────────

  await expect(page).toHaveURL('https://example.com/login');
  await expect(page).toHaveURL(/\/login$/);              // regex
  await expect(page).toHaveTitle('Login — MyApp');
  await expect(page).toHaveTitle(/Login/);               // regex

  // ── Visibility ────────────────────────────────────────────────────────────

  await expect(heading).toBeVisible();
  await expect(errorDiv).not.toBeVisible();              // should be hidden
  await expect(page.locator('.spinner')).toBeHidden();   // toBeHidden === not visible

  // ── Text Content ──────────────────────────────────────────────────────────

  await expect(heading).toHaveText('Sign In to Your Account');
  await expect(heading).toHaveText(/Sign In/);           // regex
  await expect(heading).toContainText('Sign In');        // partial match
  await expect(errorDiv).not.toContainText('Password');

  // ── Input Values ──────────────────────────────────────────────────────────

  await input.fill('user@example.com');
  await expect(input).toHaveValue('user@example.com');
  await expect(input).toHaveValue(/user@/);              // regex

  // ── Element State ─────────────────────────────────────────────────────────

  await expect(button).toBeEnabled();
  await expect(page.getByRole('button', { name: 'Save Draft' })).toBeDisabled();
  await expect(checkbox).toBeChecked();
  await expect(page.getByRole('checkbox', { name: 'Notify' })).not.toBeChecked();

  // ── Count ─────────────────────────────────────────────────────────────────

  await expect(links).toHaveCount(5);
  await expect(page.getByRole('row')).toHaveCount(10);

  // ── CSS Properties ────────────────────────────────────────────────────────

  await expect(button).toHaveCSS('background-color', 'rgb(0, 128, 0)');
  await expect(heading).toHaveCSS('font-size', '24px');
  await expect(errorDiv).toHaveCSS('color', 'rgb(255, 0, 0)');

  // ── Attributes ────────────────────────────────────────────────────────────

  await expect(input).toHaveAttribute('type', 'email');
  await expect(input).toHaveAttribute('placeholder', 'Enter your email');
  await expect(button).not.toHaveAttribute('disabled');
  await expect(page.getByRole('img', { name: 'logo' })).toHaveAttribute('src', /logo\.png/);

  // ── Class ─────────────────────────────────────────────────────────────────

  await expect(button).toHaveClass(/btn-primary/);
  await expect(errorDiv).toHaveClass('error error-visible');

  // ── Focused element ───────────────────────────────────────────────────────

  await expect(input).toBeFocused();

  // ── Soft Assertions (do not stop test on failure) ─────────────────────────

  // Soft assertions are collected and reported together at end of test
  await expect.soft(page).toHaveTitle('Login — MyApp');
  await expect.soft(heading).toBeVisible();
  await expect.soft(button).toBeEnabled();
  // Test continues even if a soft assertion fails
  // All failures reported in a single summary

  // ── Negations ─────────────────────────────────────────────────────────────

  await expect(page.locator('.success-banner')).not.toBeVisible();
  await expect(heading).not.toHaveText('Dashboard');
  await expect(button).not.toBeDisabled();
});
```

---

## 7. Page Object Model (POM)

### BasePage.ts

```typescript
// pages/BasePage.ts
import { Page, Locator, expect } from '@playwright/test';

export abstract class BasePage {
  protected readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  // Navigate to a URL (relative or absolute)
  async navigate(path: string): Promise<void> {
    await this.page.goto(path);
  }

  // Wait for the page to finish loading
  async waitForPageLoad(): Promise<void> {
    await this.page.waitForLoadState('networkidle');
  }

  // Get the current page URL
  async getURL(): Promise<string> {
    return this.page.url();
  }

  // Get page title
  async getTitle(): Promise<string> {
    return this.page.title();
  }

  // Click and wait for navigation in one step
  async clickAndWaitForNavigation(locator: Locator): Promise<void> {
    await Promise.all([
      this.page.waitForNavigation(),
      locator.click(),
    ]);
  }

  // Take a screenshot (named by caller)
  async takeScreenshot(name: string): Promise<void> {
    await this.page.screenshot({ path: `screenshots/${name}.png` });
  }

  // Abstract method that subclasses must implement — verifies correct page loaded
  abstract verifyPageLoaded(): Promise<void>;
}
```

### LoginPage.ts

```typescript
// pages/LoginPage.ts
import { Page, expect } from '@playwright/test';
import { BasePage } from './BasePage';
import { DashboardPage } from './DashboardPage';

export class LoginPage extends BasePage {
  // Locators defined as readonly properties
  private readonly emailInput = this.page.getByLabel('Email');
  private readonly passwordInput = this.page.getByLabel('Password');
  private readonly loginButton = this.page.getByRole('button', { name: 'Sign In' });
  private readonly errorMessage = this.page.getByTestId('login-error');
  private readonly rememberMeCheckbox = this.page.getByRole('checkbox', { name: 'Remember me' });
  private readonly forgotPasswordLink = this.page.getByRole('link', { name: 'Forgot Password?' });
  private readonly pageHeading = this.page.getByRole('heading', { name: 'Sign In', level: 1 });

  constructor(page: Page) {
    super(page);
  }

  async verifyPageLoaded(): Promise<void> {
    await expect(this.pageHeading).toBeVisible();
    await expect(this.page).toHaveTitle(/Login|Sign In/);
  }

  async open(): Promise<void> {
    await this.navigate('/login');
    await this.verifyPageLoaded();
  }

  async fillEmail(email: string): Promise<void> {
    await this.emailInput.fill(email);
  }

  async fillPassword(password: string): Promise<void> {
    await this.passwordInput.fill(password);
  }

  async checkRememberMe(): Promise<void> {
    await this.rememberMeCheckbox.check();
  }

  async clickLogin(): Promise<void> {
    await this.loginButton.click();
  }

  // Composite action: fill and submit login form
  async loginWith(email: string, password: string): Promise<DashboardPage> {
    await this.fillEmail(email);
    await this.fillPassword(password);
    await this.clickLogin();

    // Wait for navigation away from login page
    await this.page.waitForURL('**/dashboard');

    return new DashboardPage(this.page);
  }

  // Returns error message text after failed login
  async getErrorMessage(): Promise<string> {
    await expect(this.errorMessage).toBeVisible();
    return (await this.errorMessage.textContent()) ?? '';
  }

  async isLoginButtonEnabled(): Promise<boolean> {
    return await this.loginButton.isEnabled();
  }

  async clickForgotPassword(): Promise<void> {
    await this.forgotPasswordLink.click();
    await this.page.waitForURL('**/forgot-password');
  }
}
```

### DashboardPage.ts

```typescript
// pages/DashboardPage.ts
import { Page, expect, Locator } from '@playwright/test';
import { BasePage } from './BasePage';

export class DashboardPage extends BasePage {
  private readonly welcomeMessage = this.page.getByTestId('welcome-message');
  private readonly userNameDisplay = this.page.getByTestId('user-display-name');
  private readonly logoutButton = this.page.getByRole('button', { name: 'Logout' });
  private readonly navigationMenu = this.page.getByRole('navigation');
  private readonly pageHeading = this.page.getByRole('heading', { level: 1 });
  private readonly notificationBadge = this.page.getByTestId('notification-count');
  private readonly searchBar = this.page.getByPlaceholder('Search...');

  constructor(page: Page) {
    super(page);
  }

  async verifyPageLoaded(): Promise<void> {
    await expect(this.page).toHaveURL(/\/dashboard/);
    await expect(this.pageHeading).toBeVisible();
    await expect(this.welcomeMessage).toBeVisible();
  }

  async getWelcomeText(): Promise<string> {
    return (await this.welcomeMessage.textContent()) ?? '';
  }

  async getDisplayedUsername(): Promise<string> {
    return (await this.userNameDisplay.textContent()) ?? '';
  }

  async logout(): Promise<void> {
    await this.logoutButton.click();
    await this.page.waitForURL('**/login');
  }

  async navigateTo(section: string): Promise<void> {
    await this.navigationMenu.getByRole('link', { name: section }).click();
  }

  async getNotificationCount(): Promise<number> {
    const text = await this.notificationBadge.textContent();
    return parseInt(text ?? '0', 10);
  }

  async search(query: string): Promise<void> {
    await this.searchBar.fill(query);
    await this.searchBar.press('Enter');
    await this.page.waitForURL(/.*search.*/);
  }

  // Get all visible navigation links
  async getNavigationLinks(): Promise<string[]> {
    const links = this.navigationMenu.getByRole('link');
    const count = await links.count();
    const labels: string[] = [];
    for (let i = 0; i < count; i++) {
      labels.push((await links.nth(i).textContent()) ?? '');
    }
    return labels;
  }
}
```

### Test Using POM

```typescript
// tests/login.spec.ts
import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';

test.describe('Login Page — POM tests', () => {

  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    await loginPage.open();
  });

  test('successful login — redirects to dashboard', async ({ page }) => {
    const dashboard = await loginPage.loginWith('user@example.com', 'Password123!');

    await dashboard.verifyPageLoaded();
    expect(await dashboard.getWelcomeText()).toContain('Welcome');
    expect(await dashboard.getDisplayedUsername()).toBe('John Doe');
  });

  test('invalid credentials — shows error message', async () => {
    await loginPage.fillEmail('wrong@example.com');
    await loginPage.fillPassword('wrongpassword');
    await loginPage.clickLogin();

    const error = await loginPage.getErrorMessage();
    expect(error).toContain('Invalid credentials');
  });

  test('empty email — login button disabled', async () => {
    await loginPage.fillPassword('Password123!');
    // Email is empty — button should be disabled
    expect(await loginPage.isLoginButtonEnabled()).toBeFalsy();
  });

  test('logout flow', async ({ page }) => {
    const dashboard = await loginPage.loginWith('user@example.com', 'Password123!');
    await dashboard.logout();

    await expect(page).toHaveURL(/\/login/);
  });
});
```

---

## 8. Handling Dynamic Elements and Waits

### Playwright's Auto-Wait

Playwright automatically waits for elements to be:
- Attached to the DOM
- Visible (not hidden or zero-size)
- Stable (not animating)
- Enabled (not disabled)
- Receiving events (not covered by another element)

This means you rarely need explicit waits. **Never use `waitForTimeout` (arbitrary sleep) in production tests.**

```typescript
import { test, expect } from '@playwright/test';

test('handling dynamic elements', async ({ page }) => {
  await page.goto('/dashboard');

  // ── waitForSelector — wait for element to appear ──────────────────────────
  // Wait until the element is visible before interacting
  const notification = await page.waitForSelector('[data-testid="notification"]', {
    state: 'visible',   // 'visible' | 'attached' | 'detached' | 'hidden'
    timeout: 10000,
  });

  // ── waitForURL — wait for navigation ──────────────────────────────────────
  await page.getByRole('button', { name: 'Go to Dashboard' }).click();
  await page.waitForURL('**/dashboard');

  // ── waitForURL with regex ────────────────────────────────────────────────
  await page.waitForURL(/\/dashboard$/);

  // ── waitForResponse — wait for a specific API call ─────────────────────
  const [response] = await Promise.all([
    page.waitForResponse(resp => resp.url().includes('/api/users') && resp.status() === 200),
    page.getByRole('button', { name: 'Load Users' }).click(),
  ]);
  const data = await response.json();
  expect(data.length).toBeGreaterThan(0);

  // ── waitForLoadState ──────────────────────────────────────────────────────
  await page.goto('/slow-page');
  await page.waitForLoadState('networkidle');    // No pending network requests for 500ms
  await page.waitForLoadState('domcontentloaded'); // DOM is ready
  await page.waitForLoadState('load');           // All resources loaded

  // ── waitForFunction — custom condition ────────────────────────────────────
  await page.waitForFunction(() => {
    const el = document.querySelector('[data-testid="progress-bar"]') as HTMLElement | null;
    return el && el.style.width === '100%';
  }, { timeout: 15000 });

  // ── Polling with waitFor on a locator ─────────────────────────────────────
  const statusBadge = page.getByTestId('status-badge');
  await statusBadge.waitFor({ state: 'visible', timeout: 20000 });
  await expect(statusBadge).toHaveText('Active');

  // ── Handling elements that appear after some action ──────────────────────
  await page.getByRole('button', { name: 'Generate Report' }).click();
  // Wait for the "Download Ready" toast to appear
  await expect(page.getByText('Download Ready')).toBeVisible({ timeout: 30000 });

  // ── NEVER do this — arbitrary sleep is flaky ──────────────────────────────
  // await page.waitForTimeout(3000); // BAD — avoid in production tests

  // ── Exception: waitForTimeout is acceptable for debugging only ────────────
  // await page.waitForTimeout(1000); // Only during local debugging, remove before commit
});
```

---

## 9. File Upload and Download

```typescript
import { test, expect } from '@playwright/test';
import path from 'path';

test.describe('File Upload and Download', () => {

  test('upload a single file', async ({ page }) => {
    await page.goto('/upload');

    const filePath = path.join(__dirname, '../fixtures/test-document.pdf');

    // Method 1: Set input files directly (most reliable)
    await page.getByRole('button', { name: 'Choose File' }).setInputFiles(filePath);

    // Or by locating the input element
    await page.locator('input[type="file"]').setInputFiles(filePath);

    // Verify file name appears in UI
    await expect(page.getByTestId('uploaded-file-name')).toHaveText('test-document.pdf');

    // Submit the upload
    await page.getByRole('button', { name: 'Upload' }).click();
    await expect(page.getByTestId('upload-success')).toBeVisible();
  });

  test('upload multiple files', async ({ page }) => {
    await page.goto('/upload');

    const file1 = path.join(__dirname, '../fixtures/image1.png');
    const file2 = path.join(__dirname, '../fixtures/image2.png');

    await page.locator('input[type="file"]').setInputFiles([file1, file2]);

    await expect(page.getByTestId('file-count')).toHaveText('2 files selected');
  });

  test('upload via drag and drop', async ({ page }) => {
    await page.goto('/upload');

    const filePath = path.join(__dirname, '../fixtures/test-document.pdf');
    const dropZone = page.getByTestId('drop-zone');

    // Use Playwright's built-in drag-and-drop for files
    await dropZone.setInputFiles(filePath);
  });

  test('download a file', async ({ page }) => {
    await page.goto('/reports');

    // Method 1: Wait for the download event
    const downloadPromise = page.waitForEvent('download');
    await page.getByRole('button', { name: 'Export CSV' }).click();
    const download = await downloadPromise;

    // Verify download filename
    expect(download.suggestedFilename()).toMatch(/report.*\.csv/);

    // Save to disk
    const savePath = path.join(__dirname, '../downloads', download.suggestedFilename());
    await download.saveAs(savePath);
  });

  test('download and verify file content', async ({ page }) => {
    await page.goto('/reports');

    const downloadPromise = page.waitForEvent('download');
    await page.getByRole('link', { name: 'Download Report' }).click();
    const download = await downloadPromise;

    // Get file path after saving
    const filePath = await download.path();
    expect(filePath).toBeTruthy();

    // Read file content for verification
    const fs = await import('fs');
    const content = fs.readFileSync(filePath!, 'utf-8');
    expect(content).toContain('Report Title');
  });
});
```

---

## 10. iFrames

```typescript
import { test, expect } from '@playwright/test';

test.describe('iFrame Handling', () => {

  test('interact with elements inside an iframe', async ({ page }) => {
    await page.goto('/page-with-iframe');

    // Get the frame by locator (frameLocator is the recommended approach)
    const frame = page.frameLocator('iframe[title="Payment Form"]');

    // Now interact with elements inside the iframe as if they were on the page
    await frame.getByLabel('Card Number').fill('4111111111111111');
    await frame.getByLabel('Expiry Date').fill('12/26');
    await frame.getByLabel('CVV').fill('123');
    await frame.getByRole('button', { name: 'Pay Now' }).click();

    // Assert something inside the iframe
    await expect(frame.getByText('Payment Successful')).toBeVisible();
  });

  test('interact with nested iframe', async ({ page }) => {
    await page.goto('/nested-iframes');

    // Chain frameLocator for nested iframes
    const outerFrame = page.frameLocator('#outer-frame');
    const innerFrame = outerFrame.frameLocator('#inner-frame');

    await innerFrame.getByRole('button', { name: 'Click Me' }).click();
    await expect(innerFrame.getByText('Button clicked!')).toBeVisible();
  });

  test('locate iframe by src attribute', async ({ page }) => {
    await page.goto('/embedded-widget');

    const widgetFrame = page.frameLocator('iframe[src*="widget.example.com"]');
    await widgetFrame.getByRole('textbox').fill('Test input');
  });

  test('switch to frame by name or id (legacy approach)', async ({ page }) => {
    await page.goto('/page-with-iframe');

    // Using page.frame() — returns a Frame object
    const frame = page.frame({ name: 'myframe' }); // by name attribute
    // or
    const frame2 = page.frame({ url: /iframe-content/ }); // by URL pattern

    if (frame) {
      await frame.getByRole('button', { name: 'Submit' }).click();
    }
  });
});
```

---

## 11. Alerts and Dialogs

```typescript
import { test, expect } from '@playwright/test';

test.describe('Alerts and Dialogs', () => {

  test('accept a JavaScript alert', async ({ page }) => {
    await page.goto('/alerts-demo');

    // Listen for dialog BEFORE triggering it
    page.on('dialog', async (dialog) => {
      expect(dialog.type()).toBe('alert');
      expect(dialog.message()).toBe('This is an alert!');
      await dialog.accept();
    });

    await page.getByRole('button', { name: 'Trigger Alert' }).click();
  });

  test('accept a JavaScript confirm dialog', async ({ page }) => {
    await page.goto('/alerts-demo');

    page.on('dialog', async (dialog) => {
      expect(dialog.type()).toBe('confirm');
      expect(dialog.message()).toContain('Are you sure');
      await dialog.accept(); // Click OK
    });

    await page.getByRole('button', { name: 'Delete Item' }).click();
    await expect(page.getByText('Item deleted')).toBeVisible();
  });

  test('dismiss a JavaScript confirm dialog', async ({ page }) => {
    await page.goto('/alerts-demo');

    page.on('dialog', async (dialog) => {
      await dialog.dismiss(); // Click Cancel
    });

    await page.getByRole('button', { name: 'Delete Item' }).click();
    await expect(page.getByText('Deletion cancelled')).toBeVisible();
  });

  test('handle prompt dialog', async ({ page }) => {
    await page.goto('/alerts-demo');

    page.on('dialog', async (dialog) => {
      expect(dialog.type()).toBe('prompt');
      await dialog.accept('My entered text'); // Fill the prompt and click OK
    });

    await page.getByRole('button', { name: 'Open Prompt' }).click();
    await expect(page.getByText('You entered: My entered text')).toBeVisible();
  });

  test('handle modal dialog (custom HTML dialog)', async ({ page }) => {
    await page.goto('/modal-demo');

    await page.getByRole('button', { name: 'Open Modal' }).click();

    // Custom modal — not a JS alert — interact with it as normal elements
    const modal = page.getByRole('dialog');
    await expect(modal).toBeVisible();
    await expect(modal.getByRole('heading')).toHaveText('Confirm Action');

    await modal.getByRole('button', { name: 'Confirm' }).click();
    await expect(modal).not.toBeVisible();
  });
});
```

---

## 12. Multiple Tabs and Windows

```typescript
import { test, expect } from '@playwright/test';

test.describe('Multiple Tabs and Windows', () => {

  test('interact with a new tab opened by clicking a link', async ({ browser }) => {
    const context = await browser.newContext();
    const page = await context.newPage();

    await page.goto('https://example.com');

    // Wait for new tab to open when clicking an _blank link
    const [newPage] = await Promise.all([
      context.waitForEvent('page'),
      page.getByRole('link', { name: 'Open in New Tab' }).click(),
    ]);

    // Wait for the new page to load
    await newPage.waitForLoadState('domcontentloaded');

    // Interact with the new tab
    await expect(newPage).toHaveTitle('New Page Title');
    await newPage.getByRole('button', { name: 'Accept' }).click();

    // Go back to the original tab
    await page.bringToFront();
    await expect(page).toHaveURL('https://example.com');

    await context.close();
  });

  test('open a new tab programmatically', async ({ browser }) => {
    const context = await browser.newContext();
    const page1 = await context.newPage();
    const page2 = await context.newPage();

    await page1.goto('https://example.com/page1');
    await page2.goto('https://example.com/page2');

    // Switch between tabs
    await page1.bringToFront();
    await expect(page1).toHaveTitle('Page 1');

    await page2.bringToFront();
    await expect(page2).toHaveTitle('Page 2');

    await context.close();
  });

  test('close a popup window', async ({ context, page }) => {
    await page.goto('https://example.com');

    const [popup] = await Promise.all([
      context.waitForEvent('page'),
      page.getByRole('button', { name: 'Open Popup' }).click(),
    ]);

    await expect(popup.getByRole('heading')).toHaveText('Popup Title');
    await popup.close();

    // Back to main page
    await expect(page).toHaveURL('https://example.com');
  });
});
```

---

## 13. Screenshots and Visual Comparison

```typescript
import { test, expect } from '@playwright/test';

test.describe('Screenshots and Visual Testing', () => {

  test('take a full page screenshot', async ({ page }) => {
    await page.goto('/dashboard');
    await page.screenshot({
      path: 'screenshots/dashboard-full.png',
      fullPage: true,
    });
  });

  test('take a screenshot of a specific element', async ({ page }) => {
    await page.goto('/products');
    const card = page.getByTestId('product-card').first();
    await card.screenshot({ path: 'screenshots/product-card.png' });
  });

  test('visual comparison — full page snapshot', async ({ page }) => {
    await page.goto('/home');
    await page.waitForLoadState('networkidle');

    // On first run: creates baseline snapshot
    // On subsequent runs: compares against baseline and fails if different
    await expect(page).toHaveScreenshot('home-page.png', {
      maxDiffPixels: 100,     // allow up to 100 pixels difference
      threshold: 0.2,         // 0 to 1 — color difference tolerance
    });
  });

  test('visual comparison — element snapshot', async ({ page }) => {
    await page.goto('/dashboard');

    const chart = page.getByTestId('sales-chart');
    await expect(chart).toHaveScreenshot('sales-chart.png', {
      maxDiffPixels: 50,
    });
  });

  test('mask dynamic content before snapshot', async ({ page }) => {
    await page.goto('/profile');

    // Mask dynamic elements (timestamps, avatars) to prevent false failures
    await expect(page).toHaveScreenshot('profile.png', {
      mask: [
        page.getByTestId('last-login-time'),  // timestamp — changes every run
        page.getByTestId('user-avatar'),       // might change
      ],
      maskColor: '#ff00ff', // replace masked areas with magenta
    });
  });
});
```

**Updating snapshots when UI changes intentionally:**
```bash
npx playwright test --update-snapshots
```

---

## 14. Auth State with storageState

### auth.setup.ts — Save auth state once

```typescript
// tests/auth.setup.ts
import { test as setup, expect } from '@playwright/test';
import path from 'path';

const AUTH_FILE = path.join(__dirname, '../auth/storageState.json');

setup('authenticate and save session', async ({ page }) => {
  await page.goto('/login');

  await page.getByLabel('Email').fill(process.env.TEST_EMAIL || 'admin@example.com');
  await page.getByLabel('Password').fill(process.env.TEST_PASSWORD || 'Password123!');
  await page.getByRole('button', { name: 'Sign In' }).click();

  // Wait until fully logged in
  await page.waitForURL('**/dashboard');
  await expect(page.getByTestId('user-menu')).toBeVisible();

  // Save cookies + localStorage to file
  await page.context().storageState({ path: AUTH_FILE });
});
```

### Using saved auth state in tests

```typescript
// playwright.config.ts — project that uses the saved auth
{
  name: 'authenticated',
  use: {
    ...devices['Desktop Chrome'],
    storageState: 'auth/storageState.json',
  },
  dependencies: ['setup'],  // Runs setup first
},

// tests/dashboard.spec.ts — no login needed, starts authenticated
import { test, expect } from '@playwright/test';

test('dashboard loads with user data', async ({ page }) => {
  await page.goto('/dashboard');
  // Already logged in — no need to repeat login steps
  await expect(page.getByTestId('welcome-message')).toBeVisible();
});
```

---

## 15. Cross-Browser Testing

```typescript
// playwright.config.ts — cross-browser projects
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  projects: [
    {
      name: 'Chrome',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'Firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'Safari',
      use: { ...devices['Desktop Safari'] },
    },
    {
      name: 'Edge',
      use: { ...devices['Desktop Edge'], channel: 'msedge' },
    },
    {
      name: 'Pixel 5 Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'iPhone 13',
      use: { ...devices['iPhone 13'] },
    },
    {
      name: 'iPad Pro',
      use: { ...devices['iPad Pro 11'] },
    },
  ],
});
```

**Running on specific browsers:**
```bash
npx playwright test --project=Chrome
npx playwright test --project=Firefox
npx playwright test --project=Safari
npx playwright test --project="Pixel 5 Chrome"
```

---

## 16. Data-Driven UI Tests

```typescript
// tests/login-data-driven.spec.ts
import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

type LoginScenario = {
  description: string;
  email: string;
  password: string;
  expectedOutcome: 'success' | 'error';
  expectedError?: string;
  expectedURL?: string;
};

const loginScenarios: LoginScenario[] = [
  {
    description: 'valid credentials',
    email: 'admin@example.com',
    password: 'Password123!',
    expectedOutcome: 'success',
    expectedURL: '/dashboard',
  },
  {
    description: 'wrong password',
    email: 'admin@example.com',
    password: 'wrongpassword',
    expectedOutcome: 'error',
    expectedError: 'Invalid credentials',
  },
  {
    description: 'non-existent email',
    email: 'nobody@nowhere.com',
    password: 'Password123!',
    expectedOutcome: 'error',
    expectedError: 'Invalid credentials',
  },
  {
    description: 'empty email',
    email: '',
    password: 'Password123!',
    expectedOutcome: 'error',
    expectedError: 'Email is required',
  },
  {
    description: 'malformed email',
    email: 'not-an-email',
    password: 'Password123!',
    expectedOutcome: 'error',
    expectedError: 'Enter a valid email address',
  },
];

for (const scenario of loginScenarios) {
  test(`Login — ${scenario.description}`, async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.open();

    await loginPage.fillEmail(scenario.email);
    await loginPage.fillPassword(scenario.password);
    await loginPage.clickLogin();

    if (scenario.expectedOutcome === 'success') {
      await page.waitForURL(`**${scenario.expectedURL}`);
      await expect(page).toHaveURL(new RegExp(scenario.expectedURL!));
    } else {
      const error = await loginPage.getErrorMessage();
      expect(error).toContain(scenario.expectedError!);
    }
  });
}
```

---

## 17. Running Tests — All Commands

```bash
# Run all tests (all browsers defined in projects)
npx playwright test

# Run all tests in a specific file
npx playwright test tests/login.spec.ts

# Run by test name pattern
npx playwright test --grep "Login — valid credentials"

# Run by tag (tests with @smoke in their name)
npx playwright test --grep "@smoke"

# Run excluding a pattern
npx playwright test --grep-invert "@slow"

# Run only on Chromium
npx playwright test --project=Chrome

# Run on multiple browsers
npx playwright test --project=Chrome --project=Firefox

# Run headed (visible browser window)
npx playwright test --headed

# Run in debug mode (pauses at each step)
npx playwright test --debug

# Run a single test by line number
npx playwright test tests/login.spec.ts:45

# Run with 4 parallel workers
npx playwright test --workers=4

# Run serially (1 worker)
npx playwright test --workers=1

# Open Playwright Inspector (interactive debugging)
PWDEBUG=1 npx playwright test tests/login.spec.ts

# Open Trace Viewer after test run
npx playwright show-trace test-results/trace.zip

# Open HTML report
npx playwright show-report

# Update visual comparison snapshots
npx playwright test --update-snapshots

# List all tests without running them
npx playwright test --list

# Run with a specific timeout
npx playwright test --timeout=120000

# Generate code by recording browser actions
npx playwright codegen https://example.com

# Show all available devices
npx playwright devices
```

---

## 18. Troubleshooting

### Problem 1: Strict Mode Violation

**Error:**
```
Error: strict mode violation: getByRole('button') resolved to 3 elements
```

**Cause:** Your locator matches multiple elements when Playwright expects exactly one.

**Fix:**
```typescript
// Too broad — matches all buttons on the page
await page.getByRole('button').click(); // ERROR if multiple buttons

// Fix: be more specific
await page.getByRole('button', { name: 'Submit' }).click();

// Or use .first() if you know you want the first match
await page.getByRole('button').first().click();

// Or scope within a parent container
await page.locator('form#login').getByRole('button').click();
```

---

### Problem 2: Element Not Found / Timeout

**Error:**
```
TimeoutError: Locator.click: Timeout 30000ms exceeded
waiting for getByTestId('submit-btn')
```

**Causes and fixes:**
1. Wrong locator — use `npx playwright codegen` to find the correct selector
2. Element is inside an iframe — use `frameLocator()`
3. Element is not yet rendered — Playwright auto-waits, but the timeout may be too short: `test.setTimeout(60000)`
4. Element is covered by a cookie banner — dismiss it first: `await page.getByRole('button', { name: 'Accept Cookies' }).click()`

---

### Problem 3: Stale Element / Element Detached

**Error:**
```
Error: element is not attached to the DOM
```

**Cause:** The element was re-rendered by the framework (React, Vue) between locating it and acting on it.

**Fix:** Playwright locators are lazy — they re-query the DOM on each action. This error only happens if you use `elementHandle` (the old API). Always use `page.locator()` which re-queries automatically.

```typescript
// OLD — stale element risk
const el = await page.$('[data-testid="my-btn"]');
await el?.click(); // might be stale

// NEW — locators re-query automatically
await page.getByTestId('my-btn').click(); // always fresh
```

---

### Problem 4: Tests Timeout in CI

**Causes and fixes:**
1. **Headless mode — different rendering:** Some CSS animations are slower in CI. Add `animations: 'disabled'` to `use` in `playwright.config.ts`.
2. **No retries:** Add `retries: 2` for CI in config.
3. **Slow VM:** Increase timeout: `timeout: 90000`.
4. **No hardware acceleration:** Add `--disable-gpu` Chrome arg:
   ```typescript
   use: {
     launchOptions: {
       args: ['--disable-gpu', '--no-sandbox', '--disable-dev-shm-usage'],
     },
   }
   ```
5. **Video/trace take space:** In CI, set `video: 'on-first-retry'` not `'on'` for every run.

---

## 19. Interview Q&A

**Q1. What is Playwright's auto-wait mechanism and why is `waitForTimeout` considered an anti-pattern?**

**A:** Playwright automatically waits for actionability before executing any action. Before a `click()`, it checks that the element is attached to the DOM, visible, enabled, stable (not animating), and not covered by another element. These checks happen automatically within the configured `actionTimeout` (default 15 seconds). `waitForTimeout` is an anti-pattern because it introduces a fixed, arbitrary delay — the test waits the full duration even if the element is ready in 100ms, making the suite slow; or it is too short for a slow server, making the suite flaky. Use `waitForSelector`, `waitForResponse`, `waitForURL`, or `waitForLoadState` instead — these wait for a specific condition rather than a fixed time.

---

**Q2. What is the Page Object Model (POM) and what benefits does it provide in Playwright tests?**

**A:** POM is a design pattern where each application page/screen is represented as a class. The class encapsulates all locators and actions for that page. Tests call methods on the page object rather than repeating locators. Benefits:

- **Maintainability:** When a locator changes (e.g., label renamed), update in one place (the page class), not in every test.
- **Readability:** `loginPage.loginWith('user@example.com', 'pass')` is more readable than inline `fill()` + `click()` calls.
- **Reusability:** Multiple test files share the same page objects.
- **Single Responsibility:** Tests focus on business logic; page objects handle UI mechanics.

---

**Q3. How does `storageState` enable efficient test authentication in Playwright?**

**A:** `storageState` captures the browser's cookies and `localStorage` at a point in time and saves them to a JSON file. In `playwright.config.ts`, a `setup` project runs a real login once and saves the auth state. All subsequent test projects declare this file as `storageState`, so they start each test already authenticated without repeating the login flow. This can reduce test suite time by 30–50% if many tests need auth. The auth state is refreshed by re-running the setup project, which is triggered when the file is outdated or missing.

---

**Q4. Explain the difference between `toHaveText` and `toContainText`.**

**A:**
- `toHaveText('Exact Text')` — the element's full text content must exactly match the expected string (after trimming whitespace). If the element contains `'Welcome back, John!'` and you assert `toHaveText('Welcome')`, it fails.
- `toContainText('Welcome')` — the element's text content must include the expected string as a substring. `toContainText('Welcome')` passes for `'Welcome back, John!'`.

Both support regex: `toHaveText(/^\d+ items$/)` matches `'5 items'`. Use `toHaveText` when you want the complete text to be exactly right, and `toContainText` for partial matches or when the element contains dynamic suffixes you do not care about.

---

**Q5. How would you handle a test that needs to verify what happens in a new browser tab opened by a link with `target="_blank"`?**

**A:** Listen for the `page` event on the browser context before clicking the link, using `Promise.all` to avoid a race condition:

```typescript
const [newPage] = await Promise.all([
  context.waitForEvent('page'),  // resolves when new tab opens
  page.getByRole('link', { name: 'Open in New Tab' }).click(),
]);

await newPage.waitForLoadState('domcontentloaded');
await expect(newPage).toHaveTitle('Expected Title');
await expect(newPage.getByRole('heading')).toHaveText('Expected Heading');
```

The `Promise.all` ensures the event listener is registered before the click fires, preventing the race where the new tab opens before Playwright starts listening for it.

---

*End of File 05 — Playwright UI Testing Complete Guide*
