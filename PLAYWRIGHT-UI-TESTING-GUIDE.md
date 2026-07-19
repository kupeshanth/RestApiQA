# Playwright UI Testing — Complete Guide
## From Setup to Advanced | TypeScript + Page Object Model + CI/CD

---

## TABLE OF CONTENTS

1. [Prerequisites & Installation](#1-prerequisites--installation)
2. [Project Setup](#2-project-setup)
3. [Core Concepts](#3-core-concepts)
4. [Locators — Every Strategy](#4-locators--every-strategy)
5. [Actions — Every Interaction](#5-actions--every-interaction)
6. [Assertions — Every Pattern](#6-assertions--every-pattern)
7. [Page Object Model](#7-page-object-model)
8. [Handling Dynamic Elements](#8-handling-dynamic-elements)
9. [Waits & Timing](#9-waits--timing)
10. [File Uploads & Downloads](#10-file-uploads--downloads)
11. [iFrames, Popups & Multiple Tabs](#11-iframes-popups--multiple-tabs)
12. [Screenshots & Visual Testing](#12-screenshots--visual-testing)
13. [Data-Driven UI Testing](#13-data-driven-ui-testing)
14. [Authentication Flows in UI Tests](#14-authentication-flows-in-ui-tests)
15. [Cross-Browser Testing](#15-cross-browser-testing)
16. [All Commands](#16-all-commands)
17. [Troubleshooting — Every Common Error](#17-troubleshooting--every-common-error)
18. [Interview Q&A — Playwright UI Specific](#18-interview-qa--playwright-ui-specific)

---

## 1. Prerequisites & Installation

### Check Node.js
```bash
node --version    # need v18+
npm --version     # comes with Node
```

### Install Playwright in a new project
```bash
npm init playwright@latest
```
Prompts:
```
TypeScript or JavaScript?  → TypeScript
Where to put tests?        → tests
Add GitHub Actions?        → y  (yes for CI)
Install Playwright browsers? → y  (yes — installs Chromium, Firefox, WebKit)
```

### Install Playwright in an existing project
```bash
npm install --save-dev @playwright/test
npx playwright install                   # downloads browsers
npx playwright install --with-deps       # also installs OS-level browser deps
```

### Verify installation
```bash
npx playwright --version
npx playwright test --list               # list all discovered tests
```

---

## 2. Project Setup

### Folder Structure
```
your-project/
├── package.json
├── playwright.config.ts          ← main config
├── tsconfig.json
├── tests/
│   ├── ui/
│   │   ├── login.spec.ts         ← login tests
│   │   ├── dashboard.spec.ts     ← dashboard tests
│   │   └── checkout.spec.ts      ← checkout tests
│   └── api/
│       └── users.spec.ts         ← API tests (separate)
├── pages/                        ← Page Object Model classes
│   ├── BasePage.ts
│   ├── LoginPage.ts
│   └── DashboardPage.ts
├── utils/
│   ├── constants.ts              ← URLs, test data
│   └── helpers.ts                ← reusable functions
└── test-results/                 ← auto-generated reports
```

### playwright.config.ts — Complete UI config
```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,       // retry twice in CI, never locally
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['html'],                             // open with: npx playwright show-report
    ['list'],                             // console output during run
  ],

  use: {
    baseURL: 'http://localhost:3000',     // change to your app URL
    trace: 'on-first-retry',             // record trace on retry (for debugging)
    screenshot: 'only-on-failure',       // screenshot when test fails
    video: 'retain-on-failure',          // record video on failure
    headless: true,                      // headless in CI, false for local debug
    viewport: { width: 1280, height: 720 },
    actionTimeout: 10000,                // 10s for each action
    navigationTimeout: 30000,            // 30s for page navigations
  },

  projects: [
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
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },    // mobile viewport
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 12'] },
    },
  ],
});
```

### constants.ts
```typescript
export const URLS = {
  base:      'http://localhost:3000',
  login:     '/login',
  dashboard: '/dashboard',
  profile:   '/profile',
};

export const USERS = {
  admin:    { username: 'admin',    password: 'Admin@123' },
  standard: { username: 'user1',   password: 'User@123'  },
  invalid:  { username: 'nobody',  password: 'wrong'     },
};

export const TIMEOUTS = {
  short:  5000,
  medium: 15000,
  long:   30000,
};
```

---

## 3. Core Concepts

### The Structure of Every Test
```typescript
import { test, expect } from '@playwright/test';

test.describe('Feature being tested', () => {

  // Runs before each test in this describe block
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('test name describing what it verifies', async ({ page }) => {
    // 1. ARRANGE: navigate/set up
    await page.goto('/login');

    // 2. ACT: interact with UI
    await page.fill('#username', 'admin');
    await page.fill('#password', 'secret');
    await page.click('#loginBtn');

    // 3. ASSERT: verify result
    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('h1')).toHaveText('Welcome');
  });

});
```

### The `page` Object — What It Gives You
```typescript
// Navigation
await page.goto('/login');
await page.goBack();
await page.goForward();
await page.reload();

// Info
page.url()                         // current URL
await page.title()                 // page title
await page.content()               // full HTML

// Wait for page to be fully loaded
await page.waitForLoadState('networkidle');
await page.waitForLoadState('domcontentloaded');
```

---

## 4. Locators — Every Strategy

### Role-Based Locators (BEST — most reliable)
```typescript
// By role — closest to how users perceive the UI
page.getByRole('button', { name: 'Submit' })
page.getByRole('link', { name: 'Sign In' })
page.getByRole('textbox', { name: 'Email' })
page.getByRole('checkbox', { name: 'Remember me' })
page.getByRole('combobox', { name: 'Country' })
page.getByRole('heading', { name: 'Dashboard' })
page.getByRole('alert')
page.getByRole('dialog')

// Role locators work with ARIA and semantic HTML
// <button>Submit</button>         → getByRole('button', { name: 'Submit' })
// <a href="/login">Sign In</a>    → getByRole('link', { name: 'Sign In' })
// <input type="email">            → getByRole('textbox')
```

### Text-Based Locators
```typescript
page.getByText('Welcome back')                    // exact text match
page.getByText('Welcome', { exact: false })       // contains text
page.getByLabel('Email address')                  // input by its <label>
page.getByPlaceholder('Enter your email')         // input by placeholder
page.getByAltText('Company Logo')                 // image by alt text
page.getByTitle('Close dialog')                   // element by title attribute
```

### Test ID Locator (most stable in codebases that support it)
```typescript
page.getByTestId('submit-button')                 // data-testid="submit-button"

// Ask devs to add data-testid attributes to important elements
// <button data-testid="submit-button">Submit</button>
```

### CSS and XPath (fallback)
```typescript
page.locator('#username')                         // id
page.locator('.btn-primary')                      // class
page.locator('input[type="email"]')               // attribute
page.locator('.form .submit-btn')                 // nested

page.locator('xpath=//button[contains(text(), "Submit")]')
page.locator('xpath=//div[@class="row"]//input')
```

### Chaining Locators (finding inside a container)
```typescript
// Find a specific row in a table, then find a button within that row
const row = page.locator('tr').filter({ hasText: 'John Doe' });
await row.getByRole('button', { name: 'Delete' }).click();

// Find within a parent element
const form = page.locator('#login-form');
await form.getByLabel('Username').fill('admin');
await form.getByLabel('Password').fill('secret');
```

### Selecting from a list (nth item)
```typescript
page.locator('li').first()                        // first item
page.locator('li').last()                         // last item
page.locator('li').nth(2)                         // third item (0-indexed)
```

---

## 5. Actions — Every Interaction

### Typing & Filling
```typescript
await page.fill('#username', 'admin');            // clear then type (fastest)
await page.type('#username', 'admin');            // types char by char (triggers keypress events)
await page.locator('#username').clear();          // clear field
await page.locator('#username').fill('');         // clear by filling empty string
await page.keyboard.type('Hello World');          // type at current focus
await page.keyboard.press('Enter');               // press a key
await page.keyboard.press('Control+A');           // select all
await page.keyboard.press('Backspace');
```

### Clicking
```typescript
await page.click('#submitBtn');                   // left click
await page.dblclick('#item');                     // double click
await page.click('#menu', { button: 'right' });   // right click
await page.click('#checkbox');                    // click checkbox

// Click with modifiers
await page.click('#link', { modifiers: ['Control'] });  // Ctrl+Click (open in new tab)
await page.click('#link', { modifiers: ['Shift'] });    // Shift+Click

// Click at specific position within element
await page.click('#canvas', { position: { x: 100, y: 50 } });

// Force click even if overlapped
await page.click('#hidden-btn', { force: true });
```

### Dropdowns & Selects
```typescript
// HTML <select> element
await page.selectOption('#country', 'UK');                      // by value
await page.selectOption('#country', { label: 'United Kingdom'}); // by visible text
await page.selectOption('#country', { index: 2 });              // by index
await page.selectOption('#multi', ['option1', 'option2']);       // multi-select

// Custom dropdown (not <select>)
await page.click('#dropdown-trigger');             // open dropdown
await page.click('text=United Kingdom');          // select option
```

### Checkboxes & Radio Buttons
```typescript
await page.check('#agreeCheckbox');               // check (tick)
await page.uncheck('#agreeCheckbox');             // uncheck
await page.locator('#agreeCheckbox').setChecked(true);  // set state
await expect(page.locator('#checkbox')).toBeChecked();  // assert checked
await expect(page.locator('#checkbox')).not.toBeChecked(); // assert unchecked
```

### Hover
```typescript
await page.hover('#menuItem');                    // hover over element
await page.locator('#tooltip-trigger').hover();
```

### Scrolling
```typescript
await page.mouse.wheel(0, 500);                   // scroll down 500px
await page.locator('#element').scrollIntoViewIfNeeded();  // scroll element into view
await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight)); // scroll to bottom
```

### Drag and Drop
```typescript
await page.dragAndDrop('#source', '#target');

// Or with locators
await page.locator('#source').dragTo(page.locator('#target'));
```

---

## 6. Assertions — Every Pattern

### URL Assertions
```typescript
await expect(page).toHaveURL('/dashboard');
await expect(page).toHaveURL('https://example.com/dashboard');
await expect(page).toHaveURL(/dashboard/);           // regex match
await expect(page).not.toHaveURL('/login');
```

### Page Title
```typescript
await expect(page).toHaveTitle('Dashboard | MyApp');
await expect(page).toHaveTitle(/Dashboard/);         // partial match
```

### Element Visibility
```typescript
await expect(page.locator('#successMsg')).toBeVisible();
await expect(page.locator('#loader')).not.toBeVisible();
await expect(page.locator('#errorMsg')).toBeHidden();  // same as not.toBeVisible
```

### Text Content
```typescript
await expect(page.locator('h1')).toHaveText('Welcome Back');
await expect(page.locator('h1')).toContainText('Welcome');  // partial
await expect(page.locator('#error')).toHaveText(/invalid/i); // regex
await expect(page.locator('#count')).toHaveText('5 items');
```

### Input Values
```typescript
await expect(page.locator('#email')).toHaveValue('user@example.com');
await expect(page.locator('#email')).not.toHaveValue('');
```

### Element State
```typescript
await expect(page.locator('#submitBtn')).toBeEnabled();
await expect(page.locator('#submitBtn')).toBeDisabled();
await expect(page.locator('#checkbox')).toBeChecked();
await expect(page.locator('#checkbox')).not.toBeChecked();
await expect(page.locator('.selected')).toHaveClass('active');
await expect(page.locator('#input')).toBeFocused();
```

### CSS & Attributes
```typescript
await expect(page.locator('#error')).toHaveCSS('color', 'rgb(255, 0, 0)');
await expect(page.locator('#link')).toHaveAttribute('href', '/home');
await expect(page.locator('img')).toHaveAttribute('alt', 'Company Logo');
```

### Counting Elements
```typescript
await expect(page.locator('li')).toHaveCount(5);           // exactly 5
const items = page.locator('.product-card');
await expect(items).toHaveCount(10);
const count = await items.count();
expect(count).toBeGreaterThan(0);
```

### Soft Assertions (all assertions run even if one fails)
```typescript
import { expect as softExpect } from '@playwright/test';

// Collect failures, don't stop on first
await expect.soft(page.locator('#title')).toHaveText('Dashboard');
await expect.soft(page.locator('#user')).toHaveText('Admin');
await expect.soft(page.locator('#count')).toHaveText('42');
// All three assertions run. All failures reported at end.
```

---

## 7. Page Object Model

### BasePage.ts — Shared across all pages
```typescript
import { Page, Locator } from '@playwright/test';

export class BasePage {
  readonly page: Page;
  readonly header: Locator;
  readonly navMenu: Locator;

  constructor(page: Page) {
    this.page = page;
    this.header  = page.locator('header');
    this.navMenu = page.locator('nav');
  }

  async navigate(path: string) {
    await this.page.goto(path);
    await this.page.waitForLoadState('networkidle');
  }

  async getTitle(): Promise<string> {
    return this.page.title();
  }

  async takeScreenshot(name: string) {
    await this.page.screenshot({ path: `screenshots/${name}.png` });
  }
}
```

### LoginPage.ts — Page-specific locators and actions
```typescript
import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from './BasePage';

export class LoginPage extends BasePage {
  // Locators — defined once here
  readonly usernameField:  Locator;
  readonly passwordField:  Locator;
  readonly loginButton:    Locator;
  readonly errorMessage:   Locator;
  readonly rememberMe:     Locator;
  readonly forgotPassword: Locator;

  constructor(page: Page) {
    super(page);
    this.usernameField  = page.getByLabel('Username');
    this.passwordField  = page.getByLabel('Password');
    this.loginButton    = page.getByRole('button', { name: 'Login' });
    this.errorMessage   = page.locator('[data-testid="error-msg"]');
    this.rememberMe     = page.getByLabel('Remember me');
    this.forgotPassword = page.getByRole('link', { name: 'Forgot password?' });
  }

  // Actions — methods that describe user behaviour
  async goto() {
    await this.navigate('/login');
  }

  async login(username: string, password: string) {
    await this.usernameField.fill(username);
    await this.passwordField.fill(password);
    await this.loginButton.click();
  }

  async getErrorMessage(): Promise<string> {
    await expect(this.errorMessage).toBeVisible();
    return this.errorMessage.textContent() ?? '';
  }

  async isLoaded(): Promise<boolean> {
    return this.loginButton.isVisible();
  }
}
```

### DashboardPage.ts
```typescript
import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from './BasePage';

export class DashboardPage extends BasePage {
  readonly welcomeMessage: Locator;
  readonly logoutButton:   Locator;
  readonly profileLink:    Locator;

  constructor(page: Page) {
    super(page);
    this.welcomeMessage = page.getByRole('heading', { level: 1 });
    this.logoutButton   = page.getByRole('button', { name: 'Logout' });
    this.profileLink    = page.getByRole('link', { name: 'Profile' });
  }

  async isLoaded(): Promise<boolean> {
    await expect(this.page).toHaveURL('/dashboard');
    return this.welcomeMessage.isVisible();
  }

  async logout() {
    await this.logoutButton.click();
  }
}
```

### login.spec.ts — Tests using Page Objects
```typescript
import { test, expect } from '@playwright/test';
import { LoginPage }     from '../../pages/LoginPage';
import { DashboardPage } from '../../pages/DashboardPage';
import { USERS }         from '../../utils/constants';

test.describe('Login Feature', () => {

  let loginPage: LoginPage;
  let dashboard: DashboardPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    dashboard = new DashboardPage(page);
    await loginPage.goto();
  });

  // ── HAPPY PATH ────────────────────────────────────────────────────────────
  test('TC001 - valid credentials → redirect to dashboard @smoke', async ({ page }) => {
    await loginPage.login(USERS.admin.username, USERS.admin.password);
    expect(await dashboard.isLoaded()).toBe(true);
    await expect(page).toHaveURL('/dashboard');
  });

  // ── NEGATIVE TESTS ────────────────────────────────────────────────────────
  test('TC002 - wrong password → error message shown', async () => {
    await loginPage.login('admin', 'wrongpassword');
    const error = await loginPage.getErrorMessage();
    expect(error).toContain('Invalid credentials');
    await expect(loginPage.page).toHaveURL('/login');  // stayed on login page
  });

  test('TC003 - empty username → validation error', async () => {
    await loginPage.login('', USERS.admin.password);
    const error = await loginPage.getErrorMessage();
    expect(error).toContain('Username is required');
  });

  test('TC004 - empty password → validation error', async () => {
    await loginPage.login(USERS.admin.username, '');
    const error = await loginPage.getErrorMessage();
    expect(error).toContain('Password is required');
  });

  test('TC005 - both empty → validation errors', async ({ page }) => {
    await loginPage.loginButton.click();   // click without filling anything
    await expect(page.getByText('Username is required')).toBeVisible();
    await expect(page.getByText('Password is required')).toBeVisible();
  });

  // ── EDGE CASES ────────────────────────────────────────────────────────────
  test('TC006 - SQL injection in username → handled safely, no 500', async () => {
    await loginPage.login("' OR 1=1 --", 'anything');
    const error = await loginPage.getErrorMessage();
    expect(error).toBeDefined();    // got an error, didn't crash
    await expect(loginPage.page).not.toHaveURL('/dashboard');
  });

  test('TC007 - XSS in username → handled safely', async () => {
    await loginPage.login("<script>alert('xss')</script>", 'test');
    // Should show error, not execute script
    await expect(loginPage.page).not.toHaveURL('/dashboard');
  });

  test('TC008 - username with leading/trailing spaces → trimmed or rejected', async () => {
    await loginPage.login('  admin  ', USERS.admin.password);
    // Either trims and logs in, OR shows "invalid" — both are acceptable behaviours
    // Document which the API actually does
    const url = loginPage.page.url();
    expect(['/dashboard', '/login'].some(u => url.includes(u))).toBe(true);
  });

});
```

---

## 8. Handling Dynamic Elements

### Wait for element to appear
```typescript
// Playwright auto-waits — these are built in:
await page.click('#btn');         // waits for button to be clickable
await page.fill('#input', 'val'); // waits for input to be editable
await expect(el).toBeVisible();   // waits for element to be visible

// Explicit wait when you need more control
await page.waitForSelector('#dynamicElement', { state: 'visible' });
await page.waitForSelector('#loader', { state: 'hidden' });      // wait for loader to disappear
await page.waitForSelector('.results', { timeout: 10000 });
```

### Wait for navigation
```typescript
// After a click that causes page navigation
await Promise.all([
  page.waitForNavigation(),       // wait for navigation
  page.click('#submitBtn'),       // trigger the navigation
]);

// Or (newer Playwright way):
await page.click('#submitBtn');
await page.waitForURL('/dashboard');    // wait for specific URL
await page.waitForURL(/dashboard/);    // regex match
```

### Wait for network requests
```typescript
// Wait for an API call to complete before asserting UI
await page.waitForResponse('**/api/users');
await page.waitForResponse(r => r.url().includes('/api/users') && r.status() === 200);

// Wait for all network activity to stop
await page.waitForLoadState('networkidle');
```

### Dynamic text — assert with contains instead of exact
```typescript
// Bad: fails if timestamp or count changes
await expect(page.locator('#message')).toHaveText('3 items added at 14:32');

// Good: check just the stable part
await expect(page.locator('#message')).toContainText('items added');
await expect(page.locator('#message')).toHaveText(/\d+ items added/);  // regex
```

---

## 9. Waits & Timing

### Auto-Waiting (Playwright's superpower)
Playwright automatically waits for elements before acting. You do NOT need `Thread.sleep()` or manual `waitFor` for most actions:
```typescript
await page.click('#btn');         // auto-waits for button to exist, be visible, enabled
await page.fill('#input', 'val'); // auto-waits for input to be editable
await expect(el).toBeVisible();   // auto-waits up to timeout
```

### When to use explicit waits
```typescript
// Use when auto-wait isn't enough:

// 1. Waiting for a specific state
await page.waitForSelector('#status', { state: 'visible' });    // appears
await page.waitForSelector('#loader', { state: 'hidden' });     // disappears
await page.waitForSelector('#item', { state: 'attached' });     // in DOM (even if hidden)
await page.waitForSelector('#item', { state: 'detached' });     // removed from DOM

// 2. Waiting for URL change
await page.waitForURL('/success', { timeout: 10000 });

// 3. Waiting for specific text
await page.locator('#status').waitFor({ state: 'visible' });

// 4. Waiting for a function to return true
await page.waitForFunction(() => document.title === 'Dashboard');
```

### NEVER use `page.waitForTimeout()` in production tests
```typescript
// BAD — arbitrary sleep makes tests slow and still flaky
await page.waitForTimeout(3000);   // just hoping 3 seconds is enough

// GOOD — wait for the actual condition
await page.waitForSelector('#content', { state: 'visible' });
```

### Configure timeouts in config
```typescript
// playwright.config.ts
use: {
  actionTimeout:     10000,   // 10s for each action (click, fill)
  navigationTimeout: 30000,   // 30s for page navigations
}

// Per test override
test('slow test', async ({ page }) => {
  test.setTimeout(60000);     // this test gets 60 seconds total
  ...
});

// Per expect override
await expect(page.locator('#slow')).toBeVisible({ timeout: 20000 });
```

---

## 10. File Uploads & Downloads

### File Upload
```typescript
// Upload via file input element
await page.setInputFiles('#fileInput', 'path/to/file.pdf');

// Upload multiple files
await page.setInputFiles('#fileInput', ['file1.pdf', 'file2.png']);

// Upload via dialog (triggered by button click)
const fileChooserPromise = page.waitForEvent('filechooser');
await page.click('#uploadButton');
const fileChooser = await fileChooserPromise;
await fileChooser.setFiles('path/to/file.pdf');
```

### File Download
```typescript
// Wait for download to start when clicking a download link
const downloadPromise = page.waitForEvent('download');
await page.click('#downloadBtn');
const download = await downloadPromise;

// Save the file
await download.saveAs('path/to/save/file.pdf');

// Get filename
const filename = download.suggestedFilename();
console.log('Downloaded:', filename);

// Assert the file downloaded
expect(filename).toContain('.pdf');
```

---

## 11. iFrames, Popups & Multiple Tabs

### iFrames
```typescript
// Access iframe content
const iframeElement = page.frameLocator('#myIframe');

// Now use iframeElement to find elements inside the iframe
await iframeElement.getByRole('button', { name: 'Submit' }).click();
await iframeElement.locator('#insideIframe').fill('value');
await expect(iframeElement.locator('#result')).toHaveText('Success');

// Nested iframe
const nestedFrame = page.frameLocator('#outer').frameLocator('#inner');
```

### Browser Alerts / Dialogs
```typescript
// Handle alert automatically BEFORE clicking what triggers it
page.on('dialog', async dialog => {
  console.log('Dialog message:', dialog.message());
  await dialog.accept();    // click OK
  // await dialog.dismiss(); // click Cancel
  // await dialog.accept('typed value'); // type in prompt
});

await page.click('#showAlertBtn');

// For confirm dialogs
page.on('dialog', async dialog => {
  if (dialog.message().includes('Are you sure?')) {
    await dialog.accept();
  } else {
    await dialog.dismiss();
  }
});
```

### New Tab / Popup Window
```typescript
// Open a new tab
const newTabPromise = page.waitForEvent('popup');
await page.click('#openNewTabLink');
const newTab = await newTabPromise;

// Wait for new tab to load
await newTab.waitForLoadState('networkidle');

// Interact with new tab
await expect(newTab).toHaveURL(/expected-url/);
const title = await newTab.title();

// Close new tab and go back to main
await newTab.close();
```

---

## 12. Screenshots & Visual Testing

### Take Screenshots
```typescript
// Full page screenshot
await page.screenshot({ path: 'screenshots/full-page.png', fullPage: true });

// Specific element screenshot
await page.locator('#dashboard-widget').screenshot({ path: 'widget.png' });

// Screenshot in test on failure (auto-configured in playwright.config.ts)
// screenshot: 'only-on-failure'
```

### Visual Comparison (Snapshot Testing)
```typescript
// First run: creates baseline screenshots
// Subsequent runs: compares against baseline, fails if different
await expect(page).toHaveScreenshot('homepage.png');
await expect(page.locator('#header')).toHaveScreenshot('header.png');

// With tolerance for slight pixel differences
await expect(page).toHaveScreenshot('page.png', { maxDiffPixels: 100 });
await expect(page).toHaveScreenshot('page.png', { threshold: 0.2 });  // 20% difference allowed

// Update baselines when UI legitimately changes:
npx playwright test --update-snapshots
```

---

## 13. Data-Driven UI Testing

### Loop over test data
```typescript
const loginScenarios = [
  { username: 'admin',    password: 'Admin@123', expectedURL: '/dashboard',    desc: 'admin login' },
  { username: 'user1',   password: 'User@123',  expectedURL: '/user-dashboard', desc: 'user login' },
];

for (const scenario of loginScenarios) {
  test(`login: ${scenario.desc}`, async ({ page }) => {
    await page.goto('/login');
    await page.fill('#username', scenario.username);
    await page.fill('#password', scenario.password);
    await page.click('#loginBtn');
    await expect(page).toHaveURL(scenario.expectedURL);
  });
}
```

### Negative test data
```typescript
const invalidLogins = [
  { username: '',        password: 'secret',  error: 'Username is required'   },
  { username: 'admin',  password: '',         error: 'Password is required'   },
  { username: 'nobody', password: 'wrong',    error: 'Invalid credentials'    },
  { username: "' OR 1", password: 'hack',     error: 'Invalid credentials'    },
];

for (const { username, password, error } of invalidLogins) {
  test(`invalid login: "${username}" / "${password}" → "${error}"`, async ({ page }) => {
    await page.goto('/login');
    await page.fill('#username', username);
    await page.fill('#password', password);
    await page.click('#loginBtn');
    await expect(page.locator('.error-message')).toContainText(error);
  });
}
```

---

## 14. Authentication Flows in UI Tests

### Save auth state — login once, reuse across tests
```typescript
// auth.setup.ts — login once, save session
import { test as setup } from '@playwright/test';

const AUTH_FILE = 'playwright/.auth/user.json';

setup('authenticate', async ({ page }) => {
  await page.goto('/login');
  await page.fill('#username', 'admin');
  await page.fill('#password', 'Admin@123');
  await page.click('#loginBtn');
  await page.waitForURL('/dashboard');

  // Save session storage/cookies to file
  await page.context().storageState({ path: AUTH_FILE });
});
```

```typescript
// playwright.config.ts — use saved auth in tests
projects: [
  {
    name: 'setup',
    testMatch: /auth\.setup\.ts/,
  },
  {
    name: 'authenticated tests',
    use: {
      storageState: 'playwright/.auth/user.json',  // reuse session
    },
    dependencies: ['setup'],
  },
],
```

```typescript
// Now tests start already logged in — no need to login in every test
test('dashboard shows user name', async ({ page }) => {
  await page.goto('/dashboard');  // already authenticated!
  await expect(page.locator('#userName')).toHaveText('Admin');
});
```

---

## 15. Cross-Browser Testing

### Run on all browsers
```bash
npx playwright test                              # all browsers (from playwright.config.ts projects)
npx playwright test --project=chromium          # only Chrome
npx playwright test --project=firefox           # only Firefox
npx playwright test --project=webkit            # only Safari
npx playwright test --project="Mobile Chrome"   # mobile viewport
```

### Browser-specific handling
```typescript
test('cross-browser test', async ({ page, browserName }) => {
  await page.goto('/');

  if (browserName === 'webkit') {
    // Safari-specific handling
    await page.waitForTimeout(500);  // Safari sometimes needs extra time
  }

  if (browserName === 'firefox') {
    // Firefox-specific assertion
    await expect(page.locator('.firefox-only')).toBeHidden();
  }

  await expect(page.locator('h1')).toBeVisible();
});
```

---

## 16. All Commands

```bash
# ── INSTALLATION ──────────────────────────────────────────────────────────────
npm init playwright@latest                       # fresh setup
npm install --save-dev @playwright/test          # add to existing project
npx playwright install                           # install browsers
npx playwright install chromium                  # install only Chrome
npx playwright install --with-deps               # install with OS dependencies

# ── RUNNING TESTS ─────────────────────────────────────────────────────────────
npx playwright test                              # run all tests
npx playwright test tests/ui/login.spec.ts       # run one file
npx playwright test tests/ui/                    # run all in folder
npx playwright test --project=chromium           # specific browser
npx playwright test --project=firefox            # Firefox only
npx playwright test --grep "login"               # run by name match
npx playwright test --grep @smoke                # run by tag
npx playwright test -g "TC001"                   # run by test ID

# ── DEBUG OPTIONS ─────────────────────────────────────────────────────────────
npx playwright test --headed                     # show browser window (not headless)
npx playwright test --debug                      # step through line by line
npx playwright test --ui                         # interactive UI mode (best for debugging)
PWDEBUG=1 npx playwright test                    # same as --debug

# ── CONFIGURATION OPTIONS ─────────────────────────────────────────────────────
npx playwright test --timeout=60000              # 60 second timeout
npx playwright test --retries=2                  # retry failed tests twice
npx playwright test --workers=4                  # 4 parallel workers
npx playwright test --workers=1                  # serial (no parallel)

# ── REPORTS ───────────────────────────────────────────────────────────────────
npx playwright show-report                       # open HTML report in browser
npx playwright test --reporter=list              # console list
npx playwright test --reporter=dot               # minimal dots
npx playwright test --reporter=json              # JSON output
npx playwright test --reporter=junit             # JUnit XML (for CI)

# ── VISUAL TESTING ────────────────────────────────────────────────────────────
npx playwright test --update-snapshots           # update baseline screenshots

# ── CODEGEN — auto-generate test code by recording actions ────────────────────
npx playwright codegen https://example.com       # opens browser + generates test code
npx playwright codegen --target=python           # generate in Python
npx playwright codegen --save-storage=auth.json  # save auth state

# ── TRACE VIEWER ──────────────────────────────────────────────────────────────
npx playwright show-trace test-results/trace.zip # view recorded trace

# ── WINDOWS POWERSHELL filtering ─────────────────────────────────────────────
npx playwright test 2>&1 | Select-String "passed|failed|error|skipped"
```

---

## 17. Troubleshooting — Every Common Error

---

### ERROR: Element not found / Timeout
```
TimeoutError: Waiting for locator('#submitBtn') to be visible for 30000ms
```
**Cause:** Locator is wrong, element not on page, or page not loaded
**Fix:**
```typescript
// 1. Use --debug to pause and inspect:
npx playwright test --debug

// 2. Use Playwright Inspector to find correct locator:
npx playwright codegen https://yourapp.com

// 3. Log what's on the page:
console.log(await page.content());

// 4. Take screenshot to see what page looks like when test fails:
await page.screenshot({ path: 'debug.png' });

// 5. Check if element is inside an iframe
const frame = page.frameLocator('#iframe-id');
await frame.locator('#element').click();
```

---

### ERROR: Test is flaky — passes sometimes, fails other times
**Causes and fixes:**
```
1. Missing wait → add waitForSelector or waitForLoadState
2. Race condition → await page.waitForResponse('**/api/data') before asserting UI
3. Animation → add: { animationTimeout: 0 } or wait for animation to finish
4. Order dependency → tests should be fully independent, no shared state
5. Hardcoded sleep → replace page.waitForTimeout with actual condition wait
```

---

### ERROR: `strict mode violation — found 2 elements`
```
Error: strict mode violation: locator('button') resolved to 2 elements
```
**Cause:** Your locator matches multiple elements
**Fix:**
```typescript
// Make locator more specific
page.getByRole('button', { name: 'Submit' })          // only "Submit" button
page.locator('.form button[type="submit"]')            // inside .form only
page.locator('button').first()                         // first match
page.locator('button').nth(1)                          // second match (0-indexed)
```

---

### ERROR: `page.click: Target closed`
```
Error: page.click: Target closed
```
**Cause:** Page navigated or closed during action — often a popup/tab issue
**Fix:**
```typescript
// For new tab clicks, capture the new tab first:
const newTabPromise = page.waitForEvent('popup');
await page.click('#openTab');
const newTab = await newTabPromise;
// Now interact with newTab, not page
```

---

### ERROR: `cannot read properties of null`
```
TypeError: Cannot read properties of null (reading 'textContent')
```
**Cause:** Element not found — locator returns null
**Fix:**
```typescript
// Assert element is visible first
await expect(page.locator('#element')).toBeVisible();
const text = await page.locator('#element').textContent();
```

---

### ERROR: Tests pass locally but fail in CI
```
Common causes:
1. Headless vs headed: add --headless flag or set headless: true in config
2. Viewport: CI machines have different screen sizes → set viewport in config
3. Fonts/CSS: headless rendering may look different → check screenshots
4. Slower CI: increase timeouts in playwright.config.ts
5. Missing browser dependencies: run npx playwright install --with-deps
```

```yaml
# GitHub Actions — correct setup
- name: Install dependencies
  run: npm ci
- name: Install Playwright browsers
  run: npx playwright install --with-deps   # ← --with-deps is important in CI
- name: Run tests
  run: npx playwright test
- name: Upload report
  uses: actions/upload-artifact@v3
  if: failure()
  with:
    name: playwright-report
    path: playwright-report/
```

---

### ERROR: Visual comparison fails — screenshots differ slightly
```
Error: Screenshot comparison failed. 20 pixels are different.
```
**Cause:** Font rendering differences between OS/browser versions
**Fix:**
```typescript
// Allow pixel tolerance
await expect(page).toHaveScreenshot('page.png', {
  maxDiffPixels: 50,      // allow up to 50 different pixels
  threshold: 0.1,         // allow 10% colour difference per pixel
});

// Or update baseline if change is intentional:
npx playwright test --update-snapshots
```

---

## 18. Interview Q&A — Playwright UI Specific

---

**Q: Why Playwright over Selenium?**

```
Playwright advantages over Selenium:
1. Auto-waiting: Playwright waits for elements automatically — no manual explicit waits needed
2. Multiple browsers: Chromium, Firefox, WebKit (Safari) with one API
3. Built-in parallel: tests run in parallel by default
4. Built-in tracing: records full trace of every action for debugging failures
5. Network interception: mock/stub API calls within UI tests
6. Faster: runs tests faster than Selenium due to CDP protocol (Chrome DevTools Protocol)
7. Modern locators: getByRole, getByLabel, getByTestId are more stable than CSS/XPath
8. No driver management: no need to download ChromeDriver manually

Selenium advantages over Playwright:
1. More mature, more community resources
2. Supports more languages (Java, Python, C#, Ruby)
3. Better for enterprise legacy projects already on Selenium
4. Wider browser support (Internet Explorer if needed)
```

---

**Q: What is Playwright's auto-waiting and how does it work?**

```
Before performing any action (click, fill, etc.), Playwright automatically waits for:
1. Element to be attached to DOM
2. Element to be visible (not hidden)
3. Element to be stable (not animating)
4. Element to be enabled (not disabled)
5. Element to receive events (not covered by another element)

This eliminates most "element not interactable" failures from Selenium.
The default timeout is 30 seconds (configurable in playwright.config.ts).
```

---

**Q: How do you handle authentication across multiple tests without logging in each time?**

```
Use storageState to save login session:
1. Create auth.setup.ts that logs in once and saves cookies/localStorage
2. Configure playwright.config.ts to use that saved state for all test projects
3. Tests start already authenticated — no login step needed

This is critical for test speed — logging in before every test wastes time.
```

---

**Q: What is a Playwright trace and when do you use it?**

```
A trace is a complete recording of a test run — every action, screenshot,
network request, and console log captured in a zip file.

When to use:
- Set trace: 'on-first-retry' in config → recorded when a test fails on retry
- Open with: npx playwright show-trace test-results/trace.zip
- Shows timeline of exactly what happened, what the page looked like, what network calls were made

In CI: after a failure, download the trace artifact and open it locally.
It's like having a video + DevTools recording of the exact failure.
```

---

**Q: How do you run Playwright tests in CI?**

```yaml
# .github/workflows/playwright.yml
name: Playwright Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with: { node-version: 18 }
      - run: npm ci
      - run: npx playwright install --with-deps
      - run: npx playwright test
      - uses: actions/upload-artifact@v3
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 7
```

---

*Guide version 1.0 — Playwright UI Testing | TypeScript + POM + Cross-Browser + CI/CD*
