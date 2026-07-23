# Playwright UI Testing — Full Interview Q&A | TypeScript + POM + Cross-Browser

---

## SECTION 1: FUNDAMENTALS

**Q1: What is Playwright for UI testing and how is it different from Selenium?**

**A:** Playwright is a Microsoft-maintained browser automation framework that drives real browsers (Chromium, Firefox, WebKit) through the browser's internal DevTools Protocol rather than a WebDriver HTTP interface. Selenium uses the W3C WebDriver standard — a separate HTTP server process between your test code and the browser.

| Aspect | Playwright | Selenium |
|---|---|---|
| Protocol | CDP / browser-native | W3C WebDriver HTTP |
| Auto-waiting | Built-in (waits for actionability) | Manual — you write explicit waits |
| Parallel execution | Built-in with workers in config | Needs Selenium Grid or TestNG/JUnit setup |
| Browser support | Chromium, Firefox, WebKit (Safari engine) | Chrome, Firefox, Safari, Edge, IE |
| Setup | One `npm init playwright@latest` | Driver binaries + browser version matching |
| Language | JavaScript, TypeScript, Python, Java, C# | Java, Python, C#, Ruby, JavaScript |
| Network interception | Built-in (`page.route()`) | Requires BrowserMob proxy or similar |
| Screenshots/Videos | Built-in on failure | Requires extra libraries |
| iframe support | `frameLocator()` — clean API | `driver.switchTo().frame()` — imperative |
| Headless browsers | Native headless mode | Requires extra configuration |

Real-world context: Teams migrating from Selenium to Playwright report 2–5x faster test execution because Playwright's auto-waiting eliminates most `Thread.sleep()` / `ImplicitWait` calls. The test suite is also more stable because Playwright waits for real actionability rather than arbitrary timers.

Common mistake: thinking Playwright replaces Selenium in every context. Selenium is still preferred when you need IE 11 support, or your team is heavily invested in Java with existing Selenium infrastructure.

---

**Q2: What is auto-waiting in Playwright and what actionability checks does it perform before an action?**

**A:** Auto-waiting is Playwright's built-in mechanism that checks whether an element is ready to receive an interaction before executing any action. You never need to write `waitForElement()` before `click()`. Playwright performs these checks automatically within the configured `actionTimeout`.

Before every action (click, fill, check, etc.), Playwright verifies that the element is:

1. **Attached to the DOM** — the element exists in the document
2. **Visible** — not hidden by `display: none`, `visibility: hidden`, or zero-size
3. **Stable** — not animating (CSS transition/animation has settled)
4. **Enabled** — not disabled via `disabled` attribute or `pointer-events: none`
5. **Editable** — for `fill()`, the input is not read-only
6. **Receives pointer events** — no other element is covering it (e.g., a modal overlay)

```typescript
import { test, expect } from '@playwright/test';

test('auto-waiting demonstration', async ({ page }) => {
  await page.goto('/dashboard');

  // Playwright waits for the button to:
  // 1. Appear in DOM
  // 2. Become visible (not hidden by loading spinner)
  // 3. Finish any CSS animation
  // 4. Not be disabled
  // 5. Not be covered by another element
  // All of this happens AUTOMATICALLY before click()
  await page.getByRole('button', { name: 'Load Data' }).click();

  // Similarly, toBeVisible() keeps retrying until the element is visible
  // or the expect timeout expires (default 10 seconds)
  await expect(page.getByTestId('data-table')).toBeVisible();
});
```

If any check fails within `actionTimeout` (default 15 seconds), Playwright throws a `TimeoutError` with a clear description of which check failed.

---

**Q3: What is headless mode in Playwright and when would you disable it?**

**A:** In headless mode (`headless: true`), the browser runs without a visible window — no GUI is rendered. This is the default for CI pipelines and the fastest way to run tests. In headed mode (`headless: false`), the browser window is visible and you can watch interactions in real time.

```typescript
// playwright.config.ts
use: {
  headless: true,      // CI — no visible window, fastest
  // headless: false,  // local debugging — see what Playwright is doing
}

// Or override from CLI
// npx playwright test --headed
```

When to use headed mode:
- Debugging a failing test locally — watch what happens step by step
- Developing new tests — visually verify locators are targeting the right elements
- Investigating visual discrepancies between screenshots

When headless MUST be true:
- All CI/CD pipelines (GitHub Actions, Jenkins, CircleCI) — no display server is available
- Performance testing — headless is 20–30% faster than headed

Common mistake: running headed tests in CI. Most CI runners have no display. This will crash with `DISPLAY is not set` on Linux runners. Use headless in CI and headed only locally.

---

## SECTION 2: CONFIGURATION

**Q4: Walk through every important option in playwright.config.ts for UI testing.**

**A:**

```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test';
import * as dotenv from 'dotenv';

dotenv.config();

export default defineConfig({
  // ── Test Discovery ──────────────────────────────────────────────────────────
  testDir: './tests',                          // root directory to scan for test files
  testMatch: '**/*.spec.ts',                   // file pattern to include

  // ── Execution Model ──────────────────────────────────────────────────────────
  // Run all test files in parallel (each file gets its own worker process)
  fullyParallel: true,

  // ── CI Safety Net ────────────────────────────────────────────────────────────
  // Prevent test.only() from running in CI — forces all tests to run
  forbidOnly: !!process.env.CI,

  // ── Reliability ──────────────────────────────────────────────────────────────
  // Retry failed tests N times before marking as failed
  retries: process.env.CI ? 2 : 0,

  // ── Parallelism ──────────────────────────────────────────────────────────────
  // Number of worker processes (undefined = CPU count on local machine)
  workers: process.env.CI ? 2 : undefined,

  // ── Reporting ────────────────────────────────────────────────────────────────
  reporter: [
    ['list'],                                                     // terminal: one line per test
    ['html', { outputFolder: 'playwright-report', open: 'never' }], // HTML report
    ['json', { outputFile: 'test-results/results.json' }],        // machine-readable
    ['junit', { outputFile: 'test-results/junit.xml' }],          // CI systems (Jenkins, etc.)
  ],

  // ── Global Timeouts ────────────────────────────────────────────────────────
  timeout: 60000,                              // max time a single test can run (60s)
  expect: {
    timeout: 10000,                            // max time an assertion retries before failing (10s)
  },

  // ── Shared Browser/Context Settings ────────────────────────────────────────
  use: {
    baseURL: process.env.BASE_URL || 'https://example.com',

    // ── Browser Window ────────────────────────────────────────────────────────
    headless: true,                            // no visible window in CI
    viewport: { width: 1280, height: 720 },   // browser window size
    locale: 'en-US',                           // browser locale for date/time formatting
    timezoneId: 'America/New_York',            // browser timezone
    geolocation: { latitude: 40.7128, longitude: -74.0060 }, // GPS coordinates

    // ── Timeouts ──────────────────────────────────────────────────────────────
    actionTimeout: 15000,                      // per-action auto-wait timeout
    navigationTimeout: 30000,                  // timeout for page.goto() and waitForURL()

    // ── Failure Evidence ─────────────────────────────────────────────────────
    screenshot: 'only-on-failure',             // 'on' | 'off' | 'only-on-failure'
    video: 'on-first-retry',                   // 'on' | 'off' | 'on-first-retry' | 'retain-on-failure'
    trace: 'on-first-retry',                   // 'on' | 'off' | 'on-first-retry' | 'retain-on-failure'

    // ── Network ──────────────────────────────────────────────────────────────
    ignoreHTTPSErrors: true,                   // trust self-signed certs
    extraHTTPHeaders: {                        // headers added to every request the browser makes
      'x-test-env': 'playwright',
    },
  },

  // ── Cross-Browser Projects ────────────────────────────────────────────────
  projects: [
    {
      name: 'setup',
      testMatch: '**/auth.setup.ts',           // runs before all other projects
    },
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'auth/storageState.json', // reuse login session
      },
      dependencies: ['setup'],                 // run setup project first
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    {
      name: 'webkit',
      use: { ...devices['Desktop Safari'] },
    },
  ],
});
```

---

**Q5: What is the difference between timeout, actionTimeout, and navigationTimeout?**

**A:**
- `timeout` — the maximum total time a single `test()` block can run. If exceeded, the test is aborted and marked as timed out.
- `actionTimeout` — the maximum time Playwright waits for auto-waiting checks to pass on a single action (e.g., `click()`, `fill()`, `hover()`). Default is 15 seconds.
- `navigationTimeout` — the maximum time Playwright waits for page navigation to complete (`page.goto()`, `page.waitForURL()`, `page.waitForNavigation()`). Default is 30 seconds.
- `expect.timeout` — the maximum time a `expect()` assertion keeps retrying before failing. Default is 5 seconds.

```typescript
// playwright.config.ts
use: {
  actionTimeout: 15000,     // each action waits up to 15s for element readiness
  navigationTimeout: 30000, // page.goto() waits up to 30s for navigation
},
timeout: 60000,             // the whole test must finish within 60s
expect: { timeout: 10000 }, // each await expect() retries for up to 10s

// Override per test
test('test with custom timeouts', async ({ page }) => {
  test.setTimeout(120000); // override global timeout for this test only

  await page.goto('/slow-page', { timeout: 60000 }); // override navigation timeout

  await page.getByRole('button').click({ timeout: 5000 }); // override action timeout

  await expect(page.getByText('Done')).toBeVisible({ timeout: 30000 }); // override expect timeout
});
```

---

**Q6: What are retries and workers in playwright.config.ts, and how do they affect test stability?**

**A:**
- `retries: N` — if a test fails, Playwright reruns it up to N times. Only if it fails on every retry is it marked as failed in the report. On retry, Playwright captures more failure evidence (screenshots, trace) to help diagnose flakiness.
- `workers: N` — the number of parallel worker processes. Each worker runs an independent browser context. More workers = faster suite execution but higher memory usage.

```typescript
// playwright.config.ts
retries: process.env.CI ? 2 : 0,
// Local: 0 retries — fail fast, investigate immediately
// CI: 2 retries — tolerate occasional infrastructure flakiness

workers: process.env.CI ? 2 : undefined,
// Local: undefined = number of logical CPU cores
// CI: 2 workers — limited by CI runner RAM/CPU
```

```typescript
// Per-test retry override
test('critical payment test — retry 3 times', async ({ page }) => {
  test.info().annotations.push({ type: 'retry-reason', description: 'Payment API is occasionally slow' });
  // Configure retry in playwright.config.ts or at describe level
}, { retries: 3 });
```

Real-world context: `retries: 2` in CI is the industry standard for handling infrastructure flakiness (network blips, slow CI runners). However, if you find yourself relying on retries to pass your suite consistently, that is a sign of flaky tests that need to be fixed — not just masked with retries.

---

**Q7: What is fullyParallel and what is the difference from the default parallel mode?**

**A:** By default, Playwright runs test files in parallel (each file = one worker) but runs tests within a single file in series, top to bottom. `fullyParallel: true` tells Playwright to run individual tests within a file in parallel as well.

```typescript
// Without fullyParallel (default)
// File A → Worker 1: test1 → test2 → test3 (series)
// File B → Worker 2: test1 → test2 → test3 (series)

// With fullyParallel: true
// All tests from all files are distributed across all workers
// test A1, A2, A3, B1, B2, B3 → assigned to workers independently

export default defineConfig({
  fullyParallel: true, // maximum parallelism
});
```

Caution: `fullyParallel: true` can break tests that rely on shared mutable state. If test A creates a user and test B reads that user, running them in parallel can cause a race condition. Fix: make each test fully independent (create its own data in beforeEach, clean up in afterEach).

```typescript
// Tests that are safe for fullyParallel: true
// Each test creates and cleans up its own data
test('create user A', async ({ request }) => { /* ... */ });
test('create user B', async ({ request }) => { /* ... */ });
// These can run in parallel safely — no shared state
```

---

## SECTION 3: LOCATORS

**Q8: What is getByRole and which ARIA roles can you use? What does the name option do?**

**A:** `getByRole()` is the most semantic and resilient locator strategy. It queries elements by their ARIA role — the same role that screen readers use. It is the recommended approach because it tests the application the way assistive technology users experience it.

```typescript
import { test, expect } from '@playwright/test';

test('getByRole — all common roles', async ({ page }) => {
  await page.goto('/demo');

  // ── Interactive elements ──────────────────────────────────────────────────
  const submitBtn   = page.getByRole('button', { name: 'Submit' });
  const cancelBtn   = page.getByRole('button', { name: /cancel/i }); // case-insensitive regex
  const loginLink   = page.getByRole('link', { name: 'Login' });
  const emailInput  = page.getByRole('textbox', { name: 'Email address' });
  const ageSpinner  = page.getByRole('spinbutton', { name: 'Age' });
  const countryList = page.getByRole('combobox', { name: 'Country' });
  const agreeBox    = page.getByRole('checkbox', { name: 'I agree to terms' });
  const genderRadio = page.getByRole('radio', { name: 'Female' });
  const slider      = page.getByRole('slider', { name: 'Volume' });
  const searchBar   = page.getByRole('searchbox');

  // ── Structure elements ────────────────────────────────────────────────────
  const heading     = page.getByRole('heading', { name: 'Dashboard', level: 1 });
  const h2          = page.getByRole('heading', { level: 2 }); // any h2
  const navEl       = page.getByRole('navigation');
  const mainEl      = page.getByRole('main');
  const bannerEl    = page.getByRole('banner');            // typically <header>
  const contentInfo = page.getByRole('contentinfo');       // typically <footer>
  const dialog      = page.getByRole('dialog');            // modal
  const alertBox    = page.getByRole('alert');             // error/success message
  const statusEl    = page.getByRole('status');            // live region

  // ── List elements ─────────────────────────────────────────────────────────
  const list        = page.getByRole('list');
  const listItem    = page.getByRole('listitem');

  // ── Table elements ────────────────────────────────────────────────────────
  const table       = page.getByRole('table');
  const row         = page.getByRole('row');
  const colHeader   = page.getByRole('columnheader', { name: 'Email' });
  const cell        = page.getByRole('cell', { name: 'alice@example.com' });

  // ── Menu ──────────────────────────────────────────────────────────────────
  const menu        = page.getByRole('menu');
  const menuItem    = page.getByRole('menuitem', { name: 'Settings' });
  const menuBar     = page.getByRole('menubar');

  // ── Tab panel ────────────────────────────────────────────────────────────
  const tab         = page.getByRole('tab', { name: 'Reviews' });
  const tabPanel    = page.getByRole('tabpanel');

  // ── Image ─────────────────────────────────────────────────────────────────
  const image       = page.getByRole('img', { name: 'Company logo' }); // via alt text
});
```

The `name` option matches the **accessible name** of the element — this is usually the visible text label, `aria-label`, `aria-labelledby`, or `title` attribute. It also accepts a regex for partial or case-insensitive matching.

---

**Q9: What is getByLabel, getByText, getByPlaceholder, getByAltText, getByTitle, and getByTestId — when do you use each?**

**A:**

```typescript
import { test } from '@playwright/test';

test('semantic locator strategies', async ({ page }) => {
  await page.goto('/form-demo');

  // getByLabel — finds an input that is associated with a <label> element
  // Works with htmlFor, aria-label, aria-labelledby
  const emailField = page.getByLabel('Email address');
  const passField  = page.getByLabel('Password');
  await emailField.fill('user@example.com');
  await passField.fill('SecurePass!');

  // getByText — finds any element whose visible text content matches
  // Use for buttons without semantic role, paragraphs, spans
  const welcomeMsg  = page.getByText('Welcome back!');
  const exactMatch  = page.getByText('Sign In', { exact: true }); // full string must match
  const partialCI   = page.getByText('sign in', { exact: false }); // case-insensitive partial

  // getByPlaceholder — finds inputs by their placeholder attribute
  // Good when a label is not visually present
  const searchInput  = page.getByPlaceholder('Search products...');
  const cityInput    = page.getByPlaceholder('Enter city name');
  await searchInput.fill('laptop');

  // getByAltText — finds <img> elements by their alt attribute
  // Preferred for images that convey meaning
  const logo        = page.getByAltText('Company Logo');
  const productImg  = page.getByAltText('Red running shoes');
  await logo.click(); // click the logo to go to home page

  // getByTitle — finds elements with a matching title attribute
  // Usually tooltips, icon buttons, or frame titles
  const closeBtn   = page.getByTitle('Close dialog');
  const helpIcon   = page.getByTitle('Help — click for more information');
  await closeBtn.click();

  // getByTestId — finds elements with data-testid attribute
  // Most stable locator for automation — not tied to visible text or style
  // Requires developers to add data-testid="..." to elements
  const loginForm  = page.getByTestId('login-form');
  const errorMsg   = page.getByTestId('error-message');
  const userAvatar = page.getByTestId('user-avatar');
});
```

Priority order (most to least resilient to UI changes):
1. `getByRole` — tests accessibility and is most semantically meaningful
2. `getByLabel` / `getByText` / `getByPlaceholder` — tied to visible text, good for users
3. `getByTestId` — requires dev cooperation but most stable to styling changes
4. `getByAltText` / `getByTitle` — for images and tooltips
5. CSS / XPath — last resort, fragile

---

**Q10: How do you use CSS selectors and XPath in Playwright, and when should you avoid them?**

**A:**

```typescript
import { test } from '@playwright/test';

test('CSS and XPath locators', async ({ page }) => {
  await page.goto('/demo');

  // ── CSS Selectors (page.locator()) ────────────────────────────────────────

  // By ID
  const byId = page.locator('#submit-button');

  // By class name
  const byClass = page.locator('.btn-primary');

  // By attribute
  const byAttr = page.locator('[data-user-id="123"]');
  const byType = page.locator('input[type="email"]');

  // Complex CSS combinators
  const childOf   = page.locator('.card > .card-header');    // direct child
  const descendant = page.locator('.nav a');                  // any descendant
  const sibling   = page.locator('.label + input');           // adjacent sibling
  const nth       = page.locator('li:nth-child(3)');         // 3rd list item
  const hasText   = page.locator('button:has-text("Save")'); // button containing text
  const has       = page.locator('div:has(> .required)');    // div with direct .required child

  // ── XPath Selectors ───────────────────────────────────────────────────────

  // Basic XPath
  const xpathId    = page.locator('//input[@id="username"]');
  const xpathClass = page.locator('//div[contains(@class,"error")]');
  const xpathText  = page.locator('//button[text()="Submit"]');
  const xpathContains = page.locator('//button[contains(text(),"Subm")]');

  // XPath axes — traverse DOM structure
  const parent     = page.locator('//span[@class="error"]/..');         // parent element
  const following  = page.locator('//label[@for="email"]/following-sibling::input'); // sibling
  const ancestor   = page.locator('//input[@id="email"]/ancestor::form'); // ancestor

  // XPath with position
  const firstRow   = page.locator('(//table//tr)[1]');
  const lastRow    = page.locator('(//table//tr)[last()]');
});
```

When to avoid XPath and CSS:
- Prefer semantic locators (`getByRole`, `getByLabel`, `getByTestId`) — they describe intent, not markup structure
- XPath based on position (`[1]`, `[last()]`) breaks when the page adds or reorders elements
- Class-based CSS locators break when developers rename CSS classes
- Use XPath only when no semantic alternative exists (e.g., finding a parent element from a child — CSS cannot traverse upward)

---

**Q11: How do you chain locators to scope them within a parent element?**

**A:** Chaining a locator call on another locator restricts the search to the subtree of the parent element. This is essential when the same text or role appears multiple times on a page and you need to target a specific instance.

```typescript
import { test, expect } from '@playwright/test';

test('scoped locators — chaining', async ({ page }) => {
  await page.goto('/products');

  // Without scoping — may match multiple buttons on the page
  // await page.getByRole('button', { name: 'Add to Cart' }).click(); // ambiguous if multiple products

  // With scoping — only the Add to Cart button inside this specific product card
  const laptopCard = page.getByTestId('product-card').filter({ hasText: 'MacBook Pro' });
  await laptopCard.getByRole('button', { name: 'Add to Cart' }).click();

  // Scoping inside a table row
  const tableRow = page.getByRole('row', { name: /John Doe/ });
  const editBtn  = tableRow.getByRole('button', { name: 'Edit' });
  const deleteBtn = tableRow.getByRole('button', { name: 'Delete' });
  await editBtn.click();

  // Scoping inside a form
  const loginForm = page.locator('form#login-form');
  await loginForm.getByLabel('Email').fill('user@example.com');
  await loginForm.getByLabel('Password').fill('pass123');
  await loginForm.getByRole('button', { name: 'Sign In' }).click();

  // Scoping inside a dialog
  const confirmDialog = page.getByRole('dialog');
  await expect(confirmDialog.getByRole('heading')).toHaveText('Confirm Deletion');
  await confirmDialog.getByRole('button', { name: 'Confirm' }).click();
});
```

---

**Q12: How do you use filter(), nth(), first(), and last() on locators?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('locator filtering and indexing', async ({ page }) => {
  await page.goto('/products');

  // ── filter() — narrow a locator set ──────────────────────────────────────

  // Among all product cards, find ones with "Sale" badge
  const saleItems = page.getByTestId('product-card').filter({
    has: page.locator('.sale-badge'),
  });

  // Among all buttons, find the ones containing "Remove"
  const removeButtons = page.getByRole('button').filter({ hasText: 'Remove' });

  // NOT filter — find cards WITHOUT a sale badge
  const fullPriceItems = page.getByTestId('product-card').filter({
    hasNot: page.locator('.sale-badge'),
  });

  // ── first(), last(), nth() — select by index ──────────────────────────────

  const allRows     = page.getByRole('row');
  const firstRow    = allRows.first();        // equivalent to nth(0)
  const lastRow     = allRows.last();         // last matching element
  const thirdRow    = allRows.nth(2);         // 0-indexed — nth(2) = 3rd row

  // Click the first Add to Cart button on the page
  await page.getByRole('button', { name: 'Add to Cart' }).first().click();

  // Assert the last notification in a list
  const notifications = page.getByRole('listitem').filter({ hasText: /notification/i });
  await expect(notifications.last()).toContainText('Your order has shipped');

  // Assert count of matching elements
  await expect(saleItems).toHaveCount(3);
  await expect(page.getByRole('checkbox')).toHaveCount(5);
});
```

---

## SECTION 4: ACTIONS

**Q13: What is the difference between fill() and type() for text input?**

**A:** `fill()` sets the value of an input field directly and triggers `input` and `change` events. It replaces all existing content in one step — fast. `type()` simulates keystroke-by-keystroke input, triggering `keydown`, `keypress`, `keyup`, and `input` events for each character. Use `type()` when the application uses key event handlers that respond to individual keystrokes (e.g., an autocomplete that fires on each keypress).

```typescript
import { test, expect } from '@playwright/test';

test('fill vs type demonstration', async ({ page }) => {
  await page.goto('/search');

  // fill() — fast, replaces existing value, triggers input event
  await page.getByLabel('Search').fill('playwright testing');
  // Entire value set at once — good for most form inputs

  // type() — simulates individual keystrokes (deprecated in newer Playwright — use pressSequentially)
  await page.getByLabel('Search').pressSequentially('playwright', { delay: 50 });
  // delay: 50ms between keystrokes — good for autocomplete dropdowns

  // clear() — removes existing content
  await page.getByLabel('Name').clear();

  // fill('') — also clears the field
  await page.getByLabel('Name').fill('');

  // Press a single key
  await page.getByLabel('Search').press('Enter');

  // Press keyboard shortcuts
  await page.keyboard.press('Control+A'); // select all
  await page.keyboard.press('Control+C'); // copy
  await page.keyboard.press('Escape');     // close modal/dropdown

  // Type a sequence using the keyboard object
  await page.keyboard.type('Hello World');
});
```

---

**Q14: How do you perform single click, double click, right click, and click with modifier keys?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('all click variants', async ({ page }) => {
  await page.goto('/demo');

  // ── Single click ──────────────────────────────────────────────────────────
  await page.getByRole('button', { name: 'Submit' }).click();

  // ── Double click — trigger edit mode on a table cell ─────────────────────
  await page.getByRole('cell', { name: 'Click to edit' }).dblclick();

  // ── Right click — open context menu ───────────────────────────────────────
  await page.getByRole('listitem').first().click({ button: 'right' });
  await expect(page.getByRole('menu')).toBeVisible();
  await page.getByRole('menuitem', { name: 'Delete' }).click();

  // ── Click with modifier — multi-select with Ctrl ───────────────────────────
  await page.getByRole('option', { name: 'Option 1' }).click();
  await page.getByRole('option', { name: 'Option 3' }).click({ modifiers: ['Control'] });

  // ── Click with Shift — range select ──────────────────────────────────────
  await page.getByRole('option', { name: 'Option 1' }).click();
  await page.getByRole('option', { name: 'Option 5' }).click({ modifiers: ['Shift'] });

  // ── Click at coordinates within element ───────────────────────────────────
  await page.getByRole('canvas', { name: 'Drawing canvas' }).click({
    position: { x: 100, y: 200 }, // relative to the element's top-left corner
  });

  // ── Force click — bypass actionability checks (use sparingly) ─────────────
  // Only when you know the element is intentionally covered or hidden
  // but you need to interact with it anyway (e.g., behind a semi-transparent overlay)
  await page.locator('.behind-overlay').click({ force: true });
});
```

---

**Q15: How do you hover, scroll, and drag and drop in Playwright?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('hover, scroll, drag and drop', async ({ page }) => {
  await page.goto('/demo');

  // ── Hover — reveal dropdown menu ─────────────────────────────────────────
  await page.getByRole('link', { name: 'Products' }).hover();
  // Dropdown appears after hover
  await page.getByRole('menuitem', { name: 'Electronics' }).click();

  // ── Scroll element into view ──────────────────────────────────────────────
  const footerLink = page.getByRole('link', { name: 'Privacy Policy' });
  await footerLink.scrollIntoViewIfNeeded();
  await footerLink.click();

  // ── Scroll the page using mouse wheel ────────────────────────────────────
  await page.mouse.wheel(0, 300);   // scroll down 300 pixels
  await page.mouse.wheel(0, -300);  // scroll up 300 pixels

  // ── Scroll inside a scrollable container ─────────────────────────────────
  await page.locator('.results-container').evaluate((el: HTMLElement) => {
    el.scrollTop = el.scrollHeight; // scroll to bottom of container
  });

  // ── Drag and drop — high level ────────────────────────────────────────────
  const dragItem = page.getByTestId('draggable-card');
  const dropZone = page.getByTestId('drop-target');
  await dragItem.dragTo(dropZone);

  // ── Drag and drop — fine-grained mouse control ────────────────────────────
  const source = page.getByTestId('drag-source');
  const target = page.getByTestId('drag-target');

  const sourceBox = await source.boundingBox();
  const targetBox = await target.boundingBox();

  if (sourceBox && targetBox) {
    await page.mouse.move(
      sourceBox.x + sourceBox.width / 2,
      sourceBox.y + sourceBox.height / 2
    );
    await page.mouse.down();
    await page.mouse.move(
      targetBox.x + targetBox.width / 2,
      targetBox.y + targetBox.height / 2,
      { steps: 10 } // smooth drag in 10 steps (triggers drag events)
    );
    await page.mouse.up();
  }

  await expect(target).toContainText('Item dropped successfully');
});
```

---

**Q16: How do you select dropdown options, check checkboxes, and interact with radio buttons?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('form controls — select, checkbox, radio', async ({ page }) => {
  await page.goto('/form-demo');

  // ── Native <select> dropdown ──────────────────────────────────────────────

  // By visible text label
  await page.getByRole('combobox', { name: 'Country' }).selectOption('United States');

  // By value attribute
  await page.locator('select#country').selectOption({ value: 'US' });

  // By index (0-based)
  await page.locator('select#size').selectOption({ index: 2 });

  // Multiple selection (for multi-select inputs)
  await page.locator('select[multiple]#tags').selectOption(['javascript', 'typescript', 'playwright']);

  // Assert selected value
  await expect(page.getByRole('combobox', { name: 'Country' })).toHaveValue('US');

  // ── Checkboxes ────────────────────────────────────────────────────────────

  // Check (idempotent — does nothing if already checked)
  await page.getByRole('checkbox', { name: 'Remember me' }).check();

  // Uncheck (idempotent — does nothing if already unchecked)
  await page.getByRole('checkbox', { name: 'Subscribe to newsletter' }).uncheck();

  // Set to specific state regardless of current state
  await page.getByRole('checkbox', { name: 'Enable notifications' }).setChecked(true);
  await page.getByRole('checkbox', { name: 'Marketing emails' }).setChecked(false);

  // Assert checkbox state
  await expect(page.getByRole('checkbox', { name: 'Remember me' })).toBeChecked();
  await expect(page.getByRole('checkbox', { name: 'Marketing emails' })).not.toBeChecked();

  // ── Radio buttons ─────────────────────────────────────────────────────────

  // Check a radio button (automatically unchecks others in the group)
  await page.getByRole('radio', { name: 'Female' }).check();
  await page.getByRole('radio', { name: 'Credit Card' }).check();

  // Assert radio button state
  await expect(page.getByRole('radio', { name: 'Female' })).toBeChecked();
  await expect(page.getByRole('radio', { name: 'Male' })).not.toBeChecked();
});
```

---

## SECTION 5: ASSERTIONS

**Q17: What are all the Playwright UI assertions and when do you use each?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('complete UI assertions reference', async ({ page }) => {
  await page.goto('https://example.com/login');

  const heading   = page.getByRole('heading', { level: 1 });
  const emailInput = page.getByLabel('Email');
  const loginBtn  = page.getByRole('button', { name: 'Login' });
  const checkbox  = page.getByRole('checkbox', { name: 'Remember me' });
  const allLinks  = page.getByRole('link');
  const errorDiv  = page.getByTestId('error-message');

  // ── URL and Title ──────────────────────────────────────────────────────────
  await expect(page).toHaveURL('https://example.com/login');
  await expect(page).toHaveURL(/\/login$/);          // regex
  await expect(page).toHaveTitle('Login — MyApp');
  await expect(page).toHaveTitle(/Login/);           // partial match via regex

  // ── Visibility ────────────────────────────────────────────────────────────
  await expect(heading).toBeVisible();
  await expect(errorDiv).not.toBeVisible();          // should be hidden initially
  await expect(page.locator('.loading-spinner')).toBeHidden(); // same as not.toBeVisible

  // ── Text Content ──────────────────────────────────────────────────────────
  await expect(heading).toHaveText('Sign In to Your Account'); // exact full text
  await expect(heading).toHaveText(/Sign In/);                 // regex on full text
  await expect(heading).toContainText('Sign In');              // substring match
  await expect(errorDiv).not.toContainText('Error');

  // ── Input Values ──────────────────────────────────────────────────────────
  await emailInput.fill('user@example.com');
  await expect(emailInput).toHaveValue('user@example.com');
  await expect(emailInput).toHaveValue(/user@/);      // regex

  // ── Element State ─────────────────────────────────────────────────────────
  await expect(loginBtn).toBeEnabled();
  await expect(page.getByRole('button', { name: 'Save Draft' })).toBeDisabled();
  await expect(checkbox).not.toBeChecked();           // unchecked by default
  await checkbox.check();
  await expect(checkbox).toBeChecked();
  await expect(emailInput).toBeFocused();             // element has keyboard focus

  // ── Count of Matching Elements ────────────────────────────────────────────
  await expect(allLinks).toHaveCount(5);
  await expect(page.getByRole('row')).toHaveCount(11); // 10 data rows + 1 header

  // ── CSS Properties ────────────────────────────────────────────────────────
  await expect(loginBtn).toHaveCSS('background-color', 'rgb(59, 130, 246)');
  await expect(heading).toHaveCSS('font-size', '24px');
  await expect(errorDiv).toHaveCSS('color', 'rgb(220, 38, 38)'); // red

  // ── HTML Attributes ───────────────────────────────────────────────────────
  await expect(emailInput).toHaveAttribute('type', 'email');
  await expect(emailInput).toHaveAttribute('placeholder', 'Enter your email');
  await expect(loginBtn).not.toHaveAttribute('disabled');
  await expect(page.getByRole('img', { name: 'logo' })).toHaveAttribute('src', /logo\.png/);

  // ── CSS Class ─────────────────────────────────────────────────────────────
  await expect(loginBtn).toHaveClass(/btn-primary/);
  await expect(errorDiv).toHaveClass('alert alert-danger');

  // ── Attachment to DOM ─────────────────────────────────────────────────────
  await expect(page.locator('[data-testid="optional-widget"]')).toBeAttached();
  await expect(page.locator('[data-testid="removed-element"]')).not.toBeAttached();

  // ── Soft Assertions — collect all failures before stopping ────────────────
  await expect.soft(page).toHaveTitle('Login — MyApp');
  await expect.soft(heading).toBeVisible();
  await expect.soft(loginBtn).toBeEnabled();
  // Test continues even if soft assertions fail
  // All failures are reported together at the end of the test
});
```

---

**Q18: What are soft assertions in Playwright UI tests and when should you use them?**

**A:** A regular assertion (`await expect(locator).toBeVisible()`) stops the test immediately on failure. A soft assertion (`await expect.soft(locator).toBeVisible()`) records the failure but continues executing. All soft failures are summarised at the end of the test.

Use soft assertions for:
- Form validation tests where you want to check every field error at once
- Visual completeness checks where you want to see all missing elements
- Smoke tests where you want a full health report of the page on first run

```typescript
import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

test('homepage completeness — soft assertions for full report', async ({ page }) => {
  await page.goto('/home');

  // Hard assertion — if logo is missing it's a critical failure, stop test
  await expect(page.getByAltText('Company Logo')).toBeVisible();

  // Soft assertions — check everything else, report all issues together
  await expect.soft(page.getByRole('navigation')).toBeVisible();
  await expect.soft(page.getByRole('heading', { name: 'Welcome' })).toBeVisible();
  await expect.soft(page.getByRole('button', { name: 'Get Started' })).toBeEnabled();
  await expect.soft(page.getByRole('link', { name: 'Sign In' })).toBeVisible();
  await expect.soft(page.getByRole('link', { name: 'Sign Up' })).toBeVisible();
  await expect.soft(page.getByTestId('hero-image')).toBeVisible();
  await expect.soft(page.getByRole('contentinfo')).toBeVisible(); // footer

  // Test.info() gives access to soft assertion errors for custom reporting
  const errors = test.info().errors;
  if (errors.length > 0) {
    console.log(`${errors.length} soft assertion(s) failed on the home page`);
  }
});
```

---

## SECTION 6: PAGE OBJECT MODEL

**Q19: What is the Page Object Model pattern in Playwright? Why is it used?**

**A:** The Page Object Model (POM) is a design pattern where each UI page or component is represented as a TypeScript class. The class contains:
- Locators as private readonly properties
- Actions as public methods (composite operations like `loginWith()`)
- Assertions as public methods (`verifyPageLoaded()`)

Benefits:
- **Maintainability**: When a button's label or a form field's ID changes, you update it in one class — not in every test that uses it.
- **Readability**: `loginPage.loginWith('user@example.com', 'pass')` reads as business intent, not implementation detail.
- **Reusability**: Multiple test files share the same page objects without code duplication.
- **Separation of concerns**: Tests express what should happen; page objects handle how to interact with the UI.

---

**Q20: Write a complete BasePage, LoginPage, and DashboardPage with full TypeScript code.**

**A:**

```typescript
// pages/BasePage.ts
import { Page, Locator, expect } from '@playwright/test';

export abstract class BasePage {
  protected readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  async navigate(path: string): Promise<void> {
    await this.page.goto(path);
    await this.page.waitForLoadState('domcontentloaded');
  }

  async getURL(): Promise<string> {
    return this.page.url();
  }

  async getTitle(): Promise<string> {
    return this.page.title();
  }

  async takeScreenshot(name: string): Promise<void> {
    await this.page.screenshot({
      path: `test-results/screenshots/${name}-${Date.now()}.png`,
      fullPage: false,
    });
  }

  // Subclasses must implement this to verify the correct page is loaded
  abstract verifyPageLoaded(): Promise<void>;
}
```

```typescript
// pages/LoginPage.ts
import { Page, expect } from '@playwright/test';
import { BasePage } from './BasePage';
import { DashboardPage } from './DashboardPage';

export class LoginPage extends BasePage {
  // Locators defined once — update here if the UI changes
  private readonly emailInput      = this.page.getByLabel('Email');
  private readonly passwordInput   = this.page.getByLabel('Password');
  private readonly loginButton     = this.page.getByRole('button', { name: 'Sign In' });
  private readonly errorMessage    = this.page.getByTestId('login-error');
  private readonly rememberMe      = this.page.getByRole('checkbox', { name: 'Remember me' });
  private readonly forgotPassword  = this.page.getByRole('link', { name: 'Forgot Password?' });
  private readonly pageHeading     = this.page.getByRole('heading', { name: 'Sign In', level: 1 });
  private readonly registerLink    = this.page.getByRole('link', { name: 'Create account' });

  constructor(page: Page) {
    super(page);
  }

  async verifyPageLoaded(): Promise<void> {
    await expect(this.pageHeading).toBeVisible();
    await expect(this.emailInput).toBeVisible();
    await expect(this.passwordInput).toBeVisible();
    await expect(this.loginButton).toBeEnabled();
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
    await this.rememberMe.check();
  }

  async clickSignIn(): Promise<void> {
    await this.loginButton.click();
  }

  // Composite action — fills form and submits, returns next page object
  async loginWith(email: string, password: string): Promise<DashboardPage> {
    await this.fillEmail(email);
    await this.fillPassword(password);
    await this.clickSignIn();
    await this.page.waitForURL('**/dashboard');
    return new DashboardPage(this.page);
  }

  async getErrorMessage(): Promise<string> {
    await expect(this.errorMessage).toBeVisible();
    return (await this.errorMessage.textContent()) ?? '';
  }

  async isSignInButtonEnabled(): Promise<boolean> {
    return this.loginButton.isEnabled();
  }

  async clickForgotPassword(): Promise<void> {
    await this.forgotPassword.click();
    await this.page.waitForURL('**/forgot-password');
  }

  async clickCreateAccount(): Promise<void> {
    await this.registerLink.click();
    await this.page.waitForURL('**/register');
  }
}
```

```typescript
// pages/DashboardPage.ts
import { Page, Locator, expect } from '@playwright/test';
import { BasePage } from './BasePage';

export class DashboardPage extends BasePage {
  private readonly pageHeading       = this.page.getByRole('heading', { level: 1 });
  private readonly welcomeMessage    = this.page.getByTestId('welcome-message');
  private readonly userDisplayName   = this.page.getByTestId('user-name');
  private readonly logoutButton      = this.page.getByRole('button', { name: 'Logout' });
  private readonly navigationMenu    = this.page.getByRole('navigation');
  private readonly notificationBadge = this.page.getByTestId('notification-count');
  private readonly searchBar         = this.page.getByPlaceholder('Search...');

  constructor(page: Page) {
    super(page);
  }

  async verifyPageLoaded(): Promise<void> {
    await expect(this.page).toHaveURL(/\/dashboard/);
    await expect(this.pageHeading).toBeVisible();
    await expect(this.welcomeMessage).toBeVisible();
    await expect(this.logoutButton).toBeVisible();
  }

  async getWelcomeText(): Promise<string> {
    return (await this.welcomeMessage.textContent()) ?? '';
  }

  async getDisplayedUserName(): Promise<string> {
    return (await this.userDisplayName.textContent()) ?? '';
  }

  async getNotificationCount(): Promise<number> {
    const text = await this.notificationBadge.textContent();
    return parseInt(text ?? '0', 10);
  }

  async navigateTo(section: string): Promise<void> {
    await this.navigationMenu.getByRole('link', { name: section }).click();
    await this.page.waitForLoadState('networkidle');
  }

  async search(query: string): Promise<void> {
    await this.searchBar.fill(query);
    await this.searchBar.press('Enter');
    await this.page.waitForURL(/.*search.*/);
  }

  async logout(): Promise<void> {
    await this.logoutButton.click();
    await this.page.waitForURL('**/login');
  }
}
```

```typescript
// tests/login.spec.ts — using the POM
import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

test.describe('Login Page', () => {
  let loginPage: LoginPage;

  test.beforeEach(async ({ page }) => {
    loginPage = new LoginPage(page);
    await loginPage.open();
  });

  test('valid credentials — redirects to dashboard', async ({ page }) => {
    const dashboard = await loginPage.loginWith('user@example.com', 'Password123!');
    await dashboard.verifyPageLoaded();
    expect(await dashboard.getWelcomeText()).toContain('Welcome');
  });

  test('invalid credentials — shows error message', async () => {
    await loginPage.fillEmail('wrong@example.com');
    await loginPage.fillPassword('wrongpass');
    await loginPage.clickSignIn();

    const error = await loginPage.getErrorMessage();
    expect(error).toContain('Invalid credentials');
  });

  test('empty email — Sign In button is disabled', async () => {
    await loginPage.fillPassword('Password123!');
    expect(await loginPage.isSignInButtonEnabled()).toBe(false);
  });

  test('remember me checkbox is unchecked by default', async () => {
    await expect(loginPage['rememberMe']).not.toBeChecked(); // direct locator access if needed
  });
});
```

---

## SECTION 7: DYNAMIC ELEMENTS AND WAITS

**Q21: How do you wait for elements to appear or change after an async action?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('waiting for dynamic elements', async ({ page }) => {
  await page.goto('/dashboard');

  // ── waitForSelector — wait for element to reach a specific DOM state ──────
  // States: 'attached' | 'detached' | 'visible' | 'hidden'
  const notification = await page.waitForSelector('[data-testid="toast-notification"]', {
    state: 'visible',
    timeout: 10000,
  });
  await expect(page.getByTestId('toast-notification')).toHaveText('Operation successful');

  // ── waitForURL — wait for navigation to complete ──────────────────────────
  await page.getByRole('button', { name: 'Go to Settings' }).click();
  await page.waitForURL('**/settings');
  await page.waitForURL(/\/settings$/);          // regex version

  // ── waitForLoadState — wait for network or DOM to settle ──────────────────
  await page.goto('/reports');
  await page.waitForLoadState('networkidle');    // no pending requests for 500ms
  await page.waitForLoadState('domcontentloaded'); // DOM is parsed
  await page.waitForLoadState('load');           // all resources (images, fonts) loaded

  // ── waitForResponse — wait for a specific API call before continuing ───────
  const [apiResponse] = await Promise.all([
    // Start waiting for the response BEFORE the action that triggers it
    page.waitForResponse(
      (res) => res.url().includes('/api/users') && res.status() === 200
    ),
    page.getByRole('button', { name: 'Load Users' }).click(),
  ]);

  const data = await apiResponse.json();
  expect(data.length).toBeGreaterThan(0);
  await expect(page.getByRole('table')).toBeVisible();

  // ── waitForFunction — wait for any JavaScript condition ───────────────────
  await page.waitForFunction(() => {
    const bar = document.querySelector('[data-testid="progress-bar"]') as HTMLElement | null;
    return bar && bar.style.width === '100%';
  }, { timeout: 20000 });

  // ── Locator.waitFor — simplest approach for element-level waits ───────────
  const statusBadge = page.getByTestId('status-badge');
  await statusBadge.waitFor({ state: 'visible', timeout: 15000 });
  await expect(statusBadge).toHaveText('Active');
});
```

---

**Q22: Why should you NEVER use waitForTimeout, and what should you use instead?**

**A:** `waitForTimeout(ms)` is an unconditional sleep. It makes your test wait the full duration regardless of whether the element is ready in 100ms or needs 5 seconds. This leads to two problems:

1. **Tests are slow** — a `waitForTimeout(3000)` always wastes at least 3 seconds even when the element appears in 200ms.
2. **Tests are still flaky** — if the element sometimes takes 4 seconds, the 3-second sleep is too short and the test fails.

```typescript
// BAD — never do this in production tests
test('BAD EXAMPLE — using waitForTimeout', async ({ page }) => {
  await page.goto('/dashboard');
  await page.getByRole('button', { name: 'Generate Report' }).click();
  await page.waitForTimeout(5000); // blind sleep — slow AND fragile
  await expect(page.getByTestId('report-ready')).toBeVisible();
});

// GOOD — wait for the specific condition you need
test('GOOD EXAMPLE — wait for what you actually need', async ({ page }) => {
  await page.goto('/dashboard');
  await page.getByRole('button', { name: 'Generate Report' }).click();

  // Option 1: Wait for the element to appear (retries until visible or timeout)
  await expect(page.getByTestId('report-ready')).toBeVisible({ timeout: 30000 });

  // Option 2: Wait for a specific API call that indicates readiness
  // (combine with Promise.all if the call happens on button click)
  await page.waitForResponse((res) => res.url().includes('/api/reports') && res.status() === 200);

  // Option 3: Wait for text content to change
  await expect(page.getByTestId('status')).toHaveText('Report Ready');
});
```

The only legitimate uses of `waitForTimeout`:
- During local debugging to pause and inspect the page state (remove before committing)
- When testing a specific animation duration (e.g., asserting an animation takes roughly 2 seconds)
- Integration with external systems that have documented eventual consistency delays

---

**Q23: What is waitForResponse and how do you use it to synchronise UI tests with API calls?**

**A:** `page.waitForResponse()` creates a promise that resolves when the browser makes a network response matching your predicate. It is used with `Promise.all` to ensure you start listening before the action that triggers the request.

```typescript
import { test, expect } from '@playwright/test';

test('wait for API response before asserting UI', async ({ page }) => {
  await page.goto('/orders');

  // Scenario 1: Click loads data via API — wait for the API call to complete
  const [ordersResponse] = await Promise.all([
    // Register listener BEFORE the click
    page.waitForResponse((res) =>
      res.url().includes('/api/orders') &&
      res.request().method() === 'GET' &&
      res.status() === 200
    ),
    // Now click
    page.getByRole('button', { name: 'Load Orders' }).click(),
  ]);

  // Inspect the API response
  const orders = await ordersResponse.json();
  expect(orders.length).toBeGreaterThan(0);

  // Now assert the UI reflects the data
  await expect(page.getByRole('table')).toBeVisible();
  await expect(page.getByRole('row')).toHaveCount(orders.length + 1); // +1 for header

  // Scenario 2: Form submit triggers POST — wait for success response
  await page.getByRole('button', { name: 'Create Order' }).click();

  const [createResponse] = await Promise.all([
    page.waitForResponse((res) =>
      res.url().includes('/api/orders') &&
      res.request().method() === 'POST' &&
      res.status() === 201
    ),
    page.getByRole('button', { name: 'Submit' }).click(),
  ]);

  const newOrder = await createResponse.json();
  await expect(page.getByText(`Order #${newOrder.id}`)).toBeVisible();
});
```

---

## SECTION 8: FILE OPERATIONS

**Q24: How do you upload a file (single and multiple) in Playwright?**

**A:**

```typescript
import { test, expect } from '@playwright/test';
import path from 'path';

test.describe('File Upload', () => {

  test('upload a single file via input element', async ({ page }) => {
    await page.goto('/upload');

    const filePath = path.join(__dirname, '../fixtures/test-document.pdf');

    // Method 1: via the file input element directly
    await page.locator('input[type="file"]').setInputFiles(filePath);

    // Method 2: via the button that opens the file chooser
    // For buttons that trigger a file chooser dialog
    const [fileChooser] = await Promise.all([
      page.waitForEvent('filechooser'),
      page.getByRole('button', { name: 'Choose File' }).click(),
    ]);
    await fileChooser.setFiles(filePath);

    // Assert file name appears in the UI
    await expect(page.getByTestId('selected-file-name')).toHaveText('test-document.pdf');

    // Submit the upload form
    await page.getByRole('button', { name: 'Upload' }).click();
    await expect(page.getByTestId('upload-success')).toBeVisible();
  });

  test('upload multiple files', async ({ page }) => {
    await page.goto('/upload');

    const file1 = path.join(__dirname, '../fixtures/image1.png');
    const file2 = path.join(__dirname, '../fixtures/image2.png');
    const file3 = path.join(__dirname, '../fixtures/image3.png');

    await page.locator('input[type="file"]').setInputFiles([file1, file2, file3]);

    await expect(page.getByTestId('file-count')).toHaveText('3 files selected');
  });

  test('clear file selection', async ({ page }) => {
    await page.goto('/upload');

    const filePath = path.join(__dirname, '../fixtures/test-document.pdf');
    const fileInput = page.locator('input[type="file"]');

    await fileInput.setInputFiles(filePath);
    await expect(page.getByTestId('selected-file-name')).toHaveText('test-document.pdf');

    // Clear selection by passing an empty array
    await fileInput.setInputFiles([]);
    await expect(page.getByTestId('selected-file-name')).toHaveText('No file chosen');
  });
});
```

---

**Q25: How do you handle file downloads in Playwright and verify the file content?**

**A:**

```typescript
import { test, expect } from '@playwright/test';
import path from 'path';
import fs from 'fs';

test.describe('File Download', () => {

  test('download CSV and verify filename', async ({ page }) => {
    await page.goto('/reports');

    // Listen for the download event BEFORE clicking the download button
    const downloadPromise = page.waitForEvent('download');
    await page.getByRole('button', { name: 'Export CSV' }).click();
    const download = await downloadPromise;

    // Verify the suggested filename
    expect(download.suggestedFilename()).toMatch(/report.*\.csv/);

    // Save the file to disk
    const savePath = path.join('test-results/downloads', download.suggestedFilename());
    await download.saveAs(savePath);

    // Verify the file exists
    expect(fs.existsSync(savePath)).toBe(true);
  });

  test('download PDF and verify file content', async ({ page }) => {
    await page.goto('/reports');

    const downloadPromise = page.waitForEvent('download');
    await page.getByRole('link', { name: 'Download Full Report' }).click();
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toContain('.pdf');

    // Get the file path from the download object
    const filePath = await download.path();
    expect(filePath).toBeTruthy();

    // Read and verify content (works best for text-based formats)
    const content = fs.readFileSync(filePath!, 'utf-8');
    // PDFs have a %PDF header
    expect(content.startsWith('%PDF')).toBe(true);
  });

  test('download via anchor tag with download attribute', async ({ page }) => {
    await page.goto('/documents');

    const downloadPromise = page.waitForEvent('download');
    // Click a standard anchor tag with href pointing to a file
    await page.getByRole('link', { name: 'Download Terms & Conditions' }).click();
    const download = await downloadPromise;

    // Verify no download error
    expect(await download.failure()).toBeNull();
    expect(download.suggestedFilename()).toContain('terms');
  });
});
```

---

## SECTION 9: IFRAMES AND DIALOGS

**Q26: How do you interact with elements inside an iframe in Playwright?**

**A:** Playwright uses `frameLocator()` to create a scoped locator that resolves elements inside a specific iframe. All standard locator methods work inside a frame locator exactly as they do on the main page.

```typescript
import { test, expect } from '@playwright/test';

test.describe('iFrame Handling', () => {

  test('interact with a payment iframe', async ({ page }) => {
    await page.goto('/checkout');

    // Get the frame by its title attribute — most accessible way
    const paymentFrame = page.frameLocator('iframe[title="Secure Payment Form"]');

    // Interact with elements inside the iframe as if they were on the main page
    await paymentFrame.getByLabel('Card Number').fill('4111 1111 1111 1111');
    await paymentFrame.getByLabel('Expiry').fill('12/28');
    await paymentFrame.getByLabel('CVC').fill('123');
    await paymentFrame.getByRole('button', { name: 'Pay Now' }).click();

    // Assert result inside the iframe
    await expect(paymentFrame.getByText('Payment Successful')).toBeVisible();
  });

  test('iframe by src URL pattern', async ({ page }) => {
    await page.goto('/embed-demo');

    const widgetFrame = page.frameLocator('iframe[src*="widget.example.com"]');
    await widgetFrame.getByRole('textbox', { name: 'Email' }).fill('user@example.com');
    await widgetFrame.getByRole('button', { name: 'Subscribe' }).click();

    await expect(widgetFrame.getByText('Thank you for subscribing!')).toBeVisible();
  });

  test('nested iframes', async ({ page }) => {
    await page.goto('/nested-frames');

    // Chain frameLocator for nested iframes
    const outerFrame = page.frameLocator('#outer-iframe');
    const innerFrame = outerFrame.frameLocator('#inner-iframe');

    await innerFrame.getByRole('button', { name: 'Inner Button' }).click();
    await expect(innerFrame.getByTestId('result')).toHaveText('Inner button clicked');
  });

  test('assert element count inside iframe', async ({ page }) => {
    await page.goto('/survey');

    const surveyFrame = page.frameLocator('iframe#survey-frame');
    const questions = surveyFrame.getByRole('group');
    await expect(questions).toHaveCount(5);
  });
});
```

---

**Q27: How do you handle JavaScript alerts, confirm dialogs, and prompt dialogs?**

**A:** JavaScript native dialogs (`alert`, `confirm`, `prompt`) are intercepted by registering a listener on the `dialog` event of the page. The listener must be registered BEFORE the action that triggers the dialog, using `page.on('dialog', ...)`.

```typescript
import { test, expect } from '@playwright/test';

test.describe('JavaScript Dialogs', () => {

  test('accept a JavaScript alert', async ({ page }) => {
    await page.goto('/dialogs-demo');

    // Register handler BEFORE the action that triggers the dialog
    page.once('dialog', async (dialog) => {
      expect(dialog.type()).toBe('alert');
      expect(dialog.message()).toBe('This is an important alert!');
      await dialog.accept(); // dismiss the alert (only option for alerts)
    });

    await page.getByRole('button', { name: 'Show Alert' }).click();
    // Test continues after dialog is handled
  });

  test('accept a confirm dialog — user clicks OK', async ({ page }) => {
    await page.goto('/dialogs-demo');

    page.once('dialog', async (dialog) => {
      expect(dialog.type()).toBe('confirm');
      expect(dialog.message()).toContain('Are you sure you want to delete?');
      await dialog.accept(); // simulates clicking "OK"
    });

    await page.getByRole('button', { name: 'Delete Item' }).click();
    await expect(page.getByText('Item deleted successfully')).toBeVisible();
  });

  test('dismiss a confirm dialog — user clicks Cancel', async ({ page }) => {
    await page.goto('/dialogs-demo');

    page.once('dialog', async (dialog) => {
      await dialog.dismiss(); // simulates clicking "Cancel"
    });

    await page.getByRole('button', { name: 'Delete Item' }).click();
    await expect(page.getByText('Deletion cancelled')).toBeVisible();
  });

  test('respond to a prompt dialog', async ({ page }) => {
    await page.goto('/dialogs-demo');

    page.once('dialog', async (dialog) => {
      expect(dialog.type()).toBe('prompt');
      expect(dialog.defaultValue()).toBe(''); // default pre-filled value
      await dialog.accept('My custom name');  // fill and click OK
    });

    await page.getByRole('button', { name: 'Ask for Name' }).click();
    await expect(page.getByText('Hello, My custom name!')).toBeVisible();
  });

  test('dismiss a prompt — cancel without entering text', async ({ page }) => {
    await page.goto('/dialogs-demo');

    page.once('dialog', async (dialog) => {
      await dialog.dismiss(); // click Cancel — prompt.value returns null
    });

    await page.getByRole('button', { name: 'Ask for Name' }).click();
    await expect(page.getByText('No name provided')).toBeVisible();
  });

  test('handle custom HTML modal (not a JS dialog)', async ({ page }) => {
    await page.goto('/modal-demo');

    // Custom modals are NOT JS dialogs — interact as normal elements
    await page.getByRole('button', { name: 'Open Confirmation Modal' }).click();

    const modal = page.getByRole('dialog'); // HTML dialog element or role="dialog"
    await expect(modal).toBeVisible();
    await expect(modal.getByRole('heading')).toHaveText('Confirm Action');
    await expect(modal.getByText('Are you sure?')).toBeVisible();

    await modal.getByRole('button', { name: 'Yes, confirm' }).click();
    await expect(modal).not.toBeVisible();
    await expect(page.getByTestId('confirmation-result')).toHaveText('Action confirmed');
  });
});
```

---

## SECTION 10: MULTIPLE TABS AND WINDOWS

**Q28: How do you test links that open in a new browser tab?**

**A:** When a link has `target="_blank"` or JavaScript opens a new window, Playwright detects it as a new page on the browser context. Listen for the `page` event on the context using `Promise.all` to avoid a race condition.

```typescript
import { test, expect } from '@playwright/test';

test.describe('Multiple Tabs', () => {

  test('click _blank link and interact with new tab', async ({ browser }) => {
    const context = await browser.newContext();
    const page = await context.newPage();

    await page.goto('https://example.com');

    // Use Promise.all to guarantee the listener is registered before the click
    const [newPage] = await Promise.all([
      context.waitForEvent('page'),   // resolves when new tab opens
      page.getByRole('link', { name: 'Open in New Tab' }).click(),
    ]);

    // Wait for new tab to load
    await newPage.waitForLoadState('domcontentloaded');

    // Assert the new tab content
    await expect(newPage).toHaveTitle('Expected New Page Title');
    await expect(newPage.getByRole('heading', { level: 1 })).toHaveText('Welcome');

    // Interact in the new tab
    await newPage.getByRole('button', { name: 'Accept Cookies' }).click();

    // Switch back to original tab
    await page.bringToFront();
    await expect(page).toHaveURL('https://example.com');

    await context.close();
  });

  test('handle popup window opened by JavaScript', async ({ context, page }) => {
    await page.goto('/popup-demo');

    const [popup] = await Promise.all([
      context.waitForEvent('page'),
      page.getByRole('button', { name: 'Open Popup' }).click(),
    ]);

    await expect(popup.getByRole('heading')).toHaveText('Popup Window');
    await popup.getByRole('button', { name: 'Close' }).click();
    await popup.waitForEvent('close');

    // Back to main page — original tab is still available
    await expect(page.getByTestId('popup-closed-indicator')).toBeVisible();
  });

  test('manage multiple tabs simultaneously', async ({ browser }) => {
    const context = await browser.newContext();

    const tab1 = await context.newPage();
    const tab2 = await context.newPage();

    await tab1.goto('https://example.com/page-a');
    await tab2.goto('https://example.com/page-b');

    await tab1.bringToFront();
    await expect(tab1).toHaveTitle('Page A');
    await tab1.getByRole('button', { name: 'Submit' }).click();

    await tab2.bringToFront();
    // Verify that the action in tab1 is reflected in tab2 (e.g., shared cart)
    await expect(tab2.getByTestId('cart-count')).toHaveText('1');

    await context.close();
  });
});
```

---

## SECTION 11: SCREENSHOTS AND VISUAL TESTING

**Q29: How do you take screenshots and perform visual comparison testing in Playwright?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test.describe('Screenshots and Visual Testing', () => {

  test('full page screenshot on demand', async ({ page }) => {
    await page.goto('/dashboard');
    await page.waitForLoadState('networkidle');

    await page.screenshot({
      path: 'test-results/screenshots/dashboard-full.png',
      fullPage: true, // captures the entire scrollable page
    });
  });

  test('screenshot of a specific element', async ({ page }) => {
    await page.goto('/products');

    const productCard = page.getByTestId('product-card').first();
    await productCard.screenshot({
      path: 'test-results/screenshots/product-card.png',
    });
  });

  test('visual snapshot — compare against baseline', async ({ page }) => {
    await page.goto('/home');
    await page.waitForLoadState('networkidle');

    // First run: creates a baseline snapshot file in __snapshots__ folder
    // Subsequent runs: compares pixel-by-pixel against the baseline
    await expect(page).toHaveScreenshot('home-page.png', {
      maxDiffPixels: 100,   // allow up to 100 pixels difference (for anti-aliasing)
      threshold: 0.2,       // 0.0–1.0: colour difference tolerance per pixel
      fullPage: false,      // only visible viewport (default)
    });
  });

  test('element-level visual snapshot', async ({ page }) => {
    await page.goto('/analytics');

    const salesChart = page.getByTestId('sales-chart');
    await salesChart.waitFor({ state: 'visible' });

    await expect(salesChart).toHaveScreenshot('sales-chart.png', {
      maxDiffPixels: 50,
    });
  });

  test('mask dynamic content before comparison', async ({ page }) => {
    await page.goto('/profile');

    // Mask elements that change every run (timestamps, random avatars, ads)
    // so they do not cause false visual failures
    await expect(page).toHaveScreenshot('profile-page.png', {
      mask: [
        page.getByTestId('last-login-timestamp'), // changes every login
        page.getByTestId('user-avatar'),           // could change if user updates it
        page.locator('.ad-banner'),                // external ads vary
      ],
      maskColor: '#ff00ff', // replace masked areas with magenta in the snapshot
    });
  });
});
```

```bash
# Update baseline snapshots when the UI changes intentionally
npx playwright test --update-snapshots

# Update snapshots for a specific test file only
npx playwright test tests/visual.spec.ts --update-snapshots
```

Real-world context: Visual testing catches unintended CSS regressions — a developer changes the primary button colour globally and breaks 50 other pages. With `toHaveScreenshot`, every affected page snapshot fails in CI, catching the regression before it reaches production.

Common mistake: not masking dynamic content. Timestamps, live counters, and randomised order of items cause constant snapshot failures unrelated to actual visual regressions. Always identify dynamic content and mask it.

---

## SECTION 12: AUTHENTICATION STATE

**Q30: What is storageState in Playwright and how does it speed up test suites?**

**A:** `storageState` captures the browser context's cookies and `localStorage` at a specific moment and saves them to a JSON file. When subsequent tests load that file as `storageState`, they start already authenticated — without repeating the login flow in every test.

This can reduce test suite time by 20–50% if many tests require authentication, because a typical login flow (navigate to login page, fill email, fill password, submit, wait for redirect) takes 2–5 seconds per test.

---

**Q31: How do you set up auth.setup.ts and configure storageState dependencies in playwright.config.ts?**

**A:**

```typescript
// tests/auth.setup.ts — runs once per test run, saves auth state
import { test as setup, expect } from '@playwright/test';
import path from 'path';

// Path where the auth state will be saved
const AUTH_STATE_FILE = path.join(__dirname, '../auth/storageState.json');

setup('authenticate and save session state', async ({ page }) => {
  await page.goto('/login');

  await page.getByLabel('Email').fill(process.env.TEST_EMAIL || 'admin@example.com');
  await page.getByLabel('Password').fill(process.env.TEST_PASSWORD || 'Password123!');
  await page.getByRole('button', { name: 'Sign In' }).click();

  // Wait until fully authenticated
  await page.waitForURL('**/dashboard');
  await expect(page.getByTestId('user-menu')).toBeVisible();

  // Save cookies and localStorage to file
  await page.context().storageState({ path: AUTH_STATE_FILE });
  console.log(`Auth state saved to: ${AUTH_STATE_FILE}`);
});
```

```typescript
// playwright.config.ts — wiring setup into the test pipeline
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  projects: [
    // Setup project — runs before any authenticated project
    {
      name: 'setup',
      testMatch: '**/auth.setup.ts',
    },

    // Tests that need authentication
    {
      name: 'authenticated-chrome',
      use: {
        ...devices['Desktop Chrome'],
        storageState: 'auth/storageState.json', // load saved auth state
      },
      dependencies: ['setup'], // ensures setup runs first and completes successfully
    },

    // Unauthenticated tests (login page tests, public pages)
    {
      name: 'unauthenticated',
      use: { ...devices['Desktop Chrome'] },
      // No storageState — starts with empty browser context
      testMatch: '**/public/**/*.spec.ts',
    },
  ],
});
```

```typescript
// tests/dashboard.spec.ts — no login needed, starts authenticated
import { test, expect } from '@playwright/test';

test('dashboard shows user data — no login needed', async ({ page }) => {
  // Browser context already has session cookies from storageState
  await page.goto('/dashboard');

  // No need to call loginPage.loginWith() — already authenticated
  await expect(page.getByTestId('welcome-message')).toBeVisible();
  await expect(page.getByTestId('user-menu')).toBeVisible();
});
```

---

**Q32: How do you handle multiple user roles — admin and regular user — with separate auth states?**

**A:**

```typescript
// tests/auth-admin.setup.ts
import { test as setup } from '@playwright/test';
import path from 'path';

const ADMIN_STATE = path.join(__dirname, '../auth/admin.json');

setup('authenticate as admin', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Email').fill(process.env.ADMIN_EMAIL!);
  await page.getByLabel('Password').fill(process.env.ADMIN_PASSWORD!);
  await page.getByRole('button', { name: 'Sign In' }).click();
  await page.waitForURL('**/admin/dashboard');
  await page.context().storageState({ path: ADMIN_STATE });
});
```

```typescript
// playwright.config.ts — multiple auth projects
projects: [
  { name: 'setup-user',  testMatch: '**/auth-user.setup.ts' },
  { name: 'setup-admin', testMatch: '**/auth-admin.setup.ts' },

  {
    name: 'user-tests',
    use: { storageState: 'auth/user.json' },
    dependencies: ['setup-user'],
    testMatch: '**/user/**/*.spec.ts',
  },
  {
    name: 'admin-tests',
    use: { storageState: 'auth/admin.json' },
    dependencies: ['setup-admin'],
    testMatch: '**/admin/**/*.spec.ts',
  },
],
```

---

## SECTION 13: CROSS-BROWSER TESTING

**Q33: How do you configure cross-browser and cross-device testing in playwright.config.ts?**

**A:**

```typescript
// playwright.config.ts
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  projects: [
    // ── Desktop Browsers ──────────────────────────────────────────────────
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
      use: { ...devices['Desktop Safari'] }, // uses WebKit engine
    },
    {
      name: 'Edge',
      use: { ...devices['Desktop Edge'], channel: 'msedge' },
    },

    // ── Mobile Devices (emulated viewport and user-agent) ─────────────────
    {
      name: 'Pixel 7 Chrome',
      use: { ...devices['Pixel 7'] },
    },
    {
      name: 'iPhone 14',
      use: { ...devices['iPhone 14'] },
    },
    {
      name: 'iPad Air',
      use: { ...devices['iPad Air'] },
    },
    {
      name: 'Galaxy S23',
      use: { ...devices['Galaxy S23'] },
    },

    // ── Custom viewport ───────────────────────────────────────────────────
    {
      name: 'widescreen',
      use: {
        browserName: 'chromium',
        viewport: { width: 1920, height: 1080 },
      },
    },
  ],
});
```

```bash
# Run on a specific browser
npx playwright test --project=Chrome
npx playwright test --project=Firefox
npx playwright test --project=Safari

# Run on all mobile projects
npx playwright test --project="Pixel 7 Chrome" --project="iPhone 14"

# Run across all projects (all browsers) — full cross-browser run
npx playwright test
```

Real-world context: not all teams run cross-browser on every PR. A common strategy is:
- PRs: run on Chrome only (fast feedback)
- Nightly: run all browsers (full coverage)
- Release: run all browsers + mobile devices (comprehensive)

---

## SECTION 14: DATA-DRIVEN TESTING

**Q34: How do you write data-driven UI tests that cover multiple input scenarios?**

**A:**

```typescript
// tests/login-data-driven.spec.ts
import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';

type LoginScenario = {
  description: string;
  email: string;
  password: string;
  outcome: 'success' | 'error';
  expectedUrl?: string;
  expectedError?: string;
};

const loginScenarios: LoginScenario[] = [
  {
    description: 'valid admin credentials',
    email: 'admin@example.com',
    password: 'Admin123!',
    outcome: 'success',
    expectedUrl: '/dashboard',
  },
  {
    description: 'wrong password',
    email: 'admin@example.com',
    password: 'wrongpassword',
    outcome: 'error',
    expectedError: 'Invalid credentials',
  },
  {
    description: 'non-existent email',
    email: 'nobody@nowhere.com',
    password: 'Password123!',
    outcome: 'error',
    expectedError: 'Invalid credentials',
  },
  {
    description: 'empty email field',
    email: '',
    password: 'Password123!',
    outcome: 'error',
    expectedError: 'Email is required',
  },
  {
    description: 'invalid email format',
    email: 'not-an-email',
    password: 'Password123!',
    outcome: 'error',
    expectedError: 'Enter a valid email address',
  },
  {
    description: 'SQL injection attempt — safely rejected',
    email: "admin@example.com' OR '1'='1",
    password: 'Password123!',
    outcome: 'error',
    expectedError: 'Invalid credentials',
  },
];

for (const scenario of loginScenarios) {
  test(`Login: ${scenario.description}`, async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.open();

    await loginPage.fillEmail(scenario.email);
    await loginPage.fillPassword(scenario.password);
    await loginPage.clickSignIn();

    if (scenario.outcome === 'success') {
      await page.waitForURL(`**${scenario.expectedUrl}`);
      await expect(page).toHaveURL(new RegExp(scenario.expectedUrl!));
    } else {
      const error = await loginPage.getErrorMessage();
      expect(error).toContain(scenario.expectedError!);
      // Should remain on login page
      await expect(page).toHaveURL(/\/login/);
    }
  });
}
```

---

## SECTION 15: CLI COMMANDS REFERENCE

**Q35: What are all the Playwright CLI commands for UI testing?**

**A:**

```bash
# ── Running Tests ─────────────────────────────────────────────────────────────

# Run all tests across all configured projects
npx playwright test

# Run a specific test file
npx playwright test tests/login.spec.ts

# Run by test name pattern (grep)
npx playwright test --grep "valid credentials"

# Run by tag (tests with @smoke in their name)
npx playwright test --grep "@smoke"

# Exclude tests matching a pattern
npx playwright test --grep-invert "@slow"

# Run a test at a specific line number
npx playwright test tests/login.spec.ts:45

# ── Browser Selection ─────────────────────────────────────────────────────────

# Run on a single browser
npx playwright test --project=Chrome

# Run on multiple browsers
npx playwright test --project=Chrome --project=Firefox

# ── Display Mode ──────────────────────────────────────────────────────────────

# Headed mode — visible browser window
npx playwright test --headed

# Debug mode — pauses at each action, shows Inspector
npx playwright test --debug

# Playwright Inspector (step-by-step debugging with call stack)
PWDEBUG=1 npx playwright test tests/login.spec.ts

# ── Parallelism ───────────────────────────────────────────────────────────────

# Run with N workers
npx playwright test --workers=4

# Run serially — useful for debugging ordering issues
npx playwright test --workers=1

# ── Reporting ─────────────────────────────────────────────────────────────────

# Open HTML report in browser
npx playwright show-report

# Open Trace Viewer for a saved trace
npx playwright show-trace test-results/trace.zip

# ── Snapshots ────────────────────────────────────────────────────────────────

# Update visual comparison baseline snapshots
npx playwright test --update-snapshots

# ── Discovery ────────────────────────────────────────────────────────────────

# List all discovered tests without running them
npx playwright test --list

# List all available device emulations
npx playwright devices

# ── Code Generation ───────────────────────────────────────────────────────────

# Record browser interactions and generate Playwright test code
npx playwright codegen https://example.com

# Record with specific browser
npx playwright codegen --browser=firefox https://example.com

# ── Configuration ─────────────────────────────────────────────────────────────

# Use a specific config file
npx playwright test --config=playwright.staging.config.ts

# Override global timeout
npx playwright test --timeout=120000

# Set number of retries
npx playwright test --retries=3

# ── Install Browsers ─────────────────────────────────────────────────────────

# Install all browsers
npx playwright install

# Install specific browser
npx playwright install chromium

# Install browsers with OS dependencies
npx playwright install --with-deps
```

---

## SECTION 16: TROUBLESHOOTING

**Q36: How do you fix a strict mode violation error?**

**A:**

```
Error: strict mode violation: getByRole('button') resolved to 3 elements
```

Playwright's strict mode means a locator must resolve to exactly one element. If it matches multiple elements, Playwright refuses to act and throws this error to prevent you from accidentally clicking the wrong element.

```typescript
// BAD — ambiguous: which button?
await page.getByRole('button').click(); // ERROR if page has multiple buttons

// FIX 1: Be more specific with the name option
await page.getByRole('button', { name: 'Submit' }).click();

// FIX 2: Use first() if you know you want the first match
await page.getByRole('button').first().click();

// FIX 3: Scope inside a parent container
const loginForm = page.locator('form#login');
await loginForm.getByRole('button').click(); // only buttons inside this form

// FIX 4: Use filter to narrow down
await page.getByRole('button').filter({ hasText: 'Save' }).click();

// Diagnostic: count how many elements match your locator
const count = await page.getByRole('button').count();
console.log(`Found ${count} buttons — need to be more specific`);
```

---

**Q37: How do you troubleshoot an element not found / timeout error?**

**A:**

```
TimeoutError: Locator.click: Timeout 15000ms exceeded
waiting for getByTestId('submit-button')
```

Causes and fixes:

```typescript
import { test, expect } from '@playwright/test';

// Diagnostic checklist:
test('debug element not found', async ({ page }) => {
  await page.goto('/form');

  // 1. Check if selector is correct — use codegen to generate it
  // npx playwright codegen https://example.com/form

  // 2. Check if element is inside an iframe
  const possiblyInFrame = page.frameLocator('iframe#payment').getByTestId('submit-button');
  // vs:
  const mainPage = page.getByTestId('submit-button'); // these are different

  // 3. Check if element appears after an async action
  await page.getByRole('button', { name: 'Load Form' }).click();
  // Wait for the element to appear — do NOT hardcode sleep
  await page.getByTestId('submit-button').waitFor({ state: 'visible', timeout: 10000 });
  await page.getByTestId('submit-button').click();

  // 4. Check if element is covered by a cookie banner
  const cookieBanner = page.getByRole('dialog', { name: /cookie/i });
  if (await cookieBanner.isVisible()) {
    await cookieBanner.getByRole('button', { name: 'Accept' }).click();
  }

  // 5. Use page.pause() to inspect the live page during debugging
  // await page.pause(); // pauses test, opens Playwright Inspector

  // 6. Take a screenshot to see what the page actually looks like at failure point
  await page.screenshot({ path: 'debug-screenshot.png' });
});
```

---

**Q38: How do you troubleshoot stale element / element detached errors?**

**A:**

```
Error: element is not attached to the DOM
```

This error occurs when you stored a reference to an element handle (`page.$()`) and the framework (React, Vue, Angular) re-rendered the DOM, destroying and recreating that element. The handle now points to a detached element.

```typescript
// BAD — OLD API: elementHandle is stale after re-render
const button = await page.$('[data-testid="save-btn"]'); // returns ElementHandle
// React re-renders...
await button?.click(); // ERROR: element no longer in DOM

// GOOD — Locators re-query the DOM fresh on each call
await page.getByTestId('save-btn').click(); // always fetches the current element

// Why this matters in practice:
test('locators vs elementHandle', async ({ page }) => {
  await page.goto('/dynamic-form');

  // Locator — lazy, re-queries DOM on each action — SAFE
  const nameInput = page.getByLabel('Name'); // no DOM query yet
  await nameInput.fill('Alice');             // queries DOM now
  // React re-renders the input...
  await nameInput.clear();                   // queries DOM again — gets fresh element

  // ElementHandle — eagerly captures DOM reference — RISKY
  const handle = await page.$('input[name="email"]'); // captured NOW
  // React re-renders...
  await handle?.fill('user@example.com'); // MAY fail if element was replaced
});
```

Rule: always use `page.locator()`, `page.getByRole()`, `page.getByLabel()`, etc. Never use `page.$()` or `page.$$()` in new test code — they return `ElementHandle` which is the deprecated, stale-prone API.

---

**Q39: How do you debug failing visual comparison tests?**

**A:**

```
Error: Screenshot comparison failed: 1234 pixels are different (threshold: 100)
```

```bash
# Step 1: Open the HTML report — it shows a visual diff with before/after/diff images
npx playwright show-report

# Step 2: If the change is intentional (new feature, redesign), update the baseline
npx playwright test --update-snapshots

# Step 3: For a specific test only
npx playwright test tests/visual.spec.ts --update-snapshots

# Step 4: Increase tolerance if CI/local rendering slightly differs (fonts, anti-aliasing)
# In the test:
await expect(page).toHaveScreenshot('page.png', {
  maxDiffPixels: 500,   # allow 500 pixel difference
  threshold: 0.3,        # allow 30% colour difference per pixel
});
```

Common causes of spurious visual failures:
- Dynamic content (timestamps, live prices) — fix by masking with `.mask`
- Fonts rendering slightly differently on CI vs local — fix with `threshold: 0.2`
- Animations not fully settled — fix with `await page.waitForLoadState('networkidle')` before snapshot
- Different system DPI settings — fix with `scale: 'css'` option

---

**Q40: How do you troubleshoot CI-specific test failures that pass locally?**

**A:**

```yaml
# Most common CI issues and fixes:

# 1. Install browsers in CI
# playwright.config.ts browsers are not automatically installed in CI
- name: Install Playwright browsers
  run: npx playwright install --with-deps chromium

# 2. Add CI-specific config
# playwright.config.ts
retries: process.env.CI ? 2 : 0,        # retry flaky tests in CI
workers: process.env.CI ? 2 : undefined, # limit workers to avoid OOM

# 3. Headless mode issues — add Chrome args for CI
use: {
  launchOptions: {
    args: ['--disable-gpu', '--no-sandbox', '--disable-dev-shm-usage'],
  },
},

# 4. Animations causing flakiness
use: {
  animations: 'disabled', # disable CSS animations in CI
},

# 5. Missing environment variables in CI
env:
  BASE_URL: ${{ secrets.BASE_URL }}
  TEST_EMAIL: ${{ secrets.TEST_EMAIL }}
  TEST_PASSWORD: ${{ secrets.TEST_PASSWORD }}
```

---

## SECTION 17: INTERVIEW Q&A

**Q41: An interviewer asks: how does Playwright compare to Selenium for a team starting a new project from scratch?**

**A:** For a team starting fresh in 2024+, Playwright is generally the better choice for browser automation. Key reasons:

**Playwright advantages:**
- Auto-waiting eliminates 80% of the flaky test root causes that plague Selenium suites
- A single `npm init playwright@latest` installs everything — no driver version management
- Built-in HTML reports, screenshots on failure, video recording, and trace viewer
- Network interception for mocking API responses (`page.route()`)
- `storageState` for auth reuse without repeating login in every test
- TypeScript support out of the box

**Selenium advantages still:**
- Wider browser support (IE 11, legacy browsers)
- Larger ecosystem of tutorials, Stack Overflow answers, and third-party integrations
- Java/C# teams may prefer Selenium's Java bindings if the rest of their stack is Java

For a JavaScript/TypeScript team building a new product on modern browsers, Playwright is the recommended choice.

---

**Q42: An interviewer asks: explain Playwright traces. What are they, when are they generated, and how do you use them?**

**A:** A Playwright trace is a comprehensive recording of everything that happened during a test: every action, before-and-after DOM snapshots, network requests and responses, console logs, and screenshots. It is saved as a `.zip` file.

```typescript
// playwright.config.ts
use: {
  trace: 'on-first-retry', // generate trace only when a test is retried (CI best practice)
  // trace: 'on',           // generate trace for every test (large disk usage)
  // trace: 'off',          // disable traces
  // trace: 'retain-on-failure', // keep trace only for tests that finally fail
},
```

```bash
# View a trace file in the Playwright Trace Viewer (web-based)
npx playwright show-trace test-results/test-name/trace.zip
```

In the Trace Viewer you can:
- Step through each action chronologically
- See the DOM snapshot before and after each action
- See which network requests were made at each point
- See console errors and warnings
- Replay the test visually without re-running it

This makes trace files the most powerful debugging tool in Playwright — especially for CI failures that you cannot reproduce locally.

---

**Q43: An interviewer asks: what is the difference between toHaveText and toContainText?**

**A:**
- `toHaveText('Exact Text')` — the element's entire text content (after trimming whitespace) must match the expected string exactly. If the element says `"Welcome back, John!"` and you assert `toHaveText('Welcome')`, it fails.
- `toContainText('Welcome')` — the element's text content must include the expected string as a substring. This passes for `"Welcome back, John!"`.
- Both support regex: `toHaveText(/^\d+ items$/)` matches `"5 items"`.

```typescript
const heading = page.getByRole('heading', { level: 1 });
// heading text: "Dashboard — Welcome back"

await expect(heading).toHaveText('Dashboard — Welcome back'); // PASS — exact match
await expect(heading).toHaveText('Dashboard');                // FAIL — not the full text
await expect(heading).toContainText('Dashboard');             // PASS — substring match
await expect(heading).toHaveText(/Dashboard/);                // PASS — regex match
```

Use `toHaveText` for critical assertions where the exact wording matters (error messages, legal text, prices). Use `toContainText` for flexible assertions where you care about a keyword but not surrounding dynamic text.

---

**Q44: An interviewer asks: what is the recommended locator priority order and why?**

**A:** Playwright's own documentation recommends this priority (most preferred → least preferred):

1. `getByRole()` — tests accessibility semantics; if this locator works, your app is accessible
2. `getByLabel()` — ties to form labels; good for form testing
3. `getByPlaceholder()` — for inputs with placeholder text
4. `getByText()` — for content-driven selection
5. `getByAltText()` — for images
6. `getByTitle()` — for tooltips
7. `getByTestId()` — requires developer cooperation (adding `data-testid` attributes) but is immune to text and style changes
8. CSS selectors — fragile to class name and markup structure changes
9. XPath — most fragile; breaks on any DOM restructuring

```typescript
// Least preferred — fragile to class rename or markup restructure
page.locator('.btn.btn-primary.submit-btn')
page.locator('//div[@class="container"]/button[1]')

// Most preferred — survives UI redesigns
page.getByRole('button', { name: 'Submit' })
page.getByLabel('Email address')
page.getByTestId('submit-form-button')
```

The principle: write tests the way a human describes the element ("the Submit button", "the Email field"), not the way CSS describes it (`.btn-primary`).

---

**Q45: An interviewer asks: how do you handle a test that intermittently fails with "Timeout exceeded" on CI but always passes locally?**

**A:** This is the classic flaky test problem. Systematic approach:

1. **Enable traces in CI** (`trace: 'on-first-retry'`) and download the trace zip from the CI artefacts. Open it with `npx playwright show-trace` — it shows the exact point of failure.

2. **Common root causes:**
   - Element exists but is covered by another element (cookie banner, chat widget) — add dismissal logic in `beforeEach`
   - Animation has not finished — use `await expect(element).toBeVisible()` with a longer timeout instead of `waitForTimeout`
   - API call is slower in CI — use `waitForResponse` instead of a hardcoded delay
   - CI runner is slower than local machine — increase timeout: `test.setTimeout(90000)`
   - Different viewport on CI — set explicit viewport in config

3. **Quick fixes:**
   ```typescript
   // Increase timeout for this specific test
   test.setTimeout(90000);

   // Wait for the specific condition, not a timer
   await expect(page.getByTestId('result')).toBeVisible({ timeout: 30000 });

   // Add retries for this specific describe block
   test.describe.configure({ retries: 2 });
   ```

4. **Preventive measures:**
   - Add `animations: 'disabled'` to CI config
   - Add `--disable-gpu` and `--no-sandbox` Chrome launch args
   - Ensure tests are fully independent (no shared state between tests)

---

---

**Q: How do you intercept and mock API responses in Playwright UI tests?**

**A:** `page.route()` intercepts network requests during a UI test. This lets you test UI behaviour when the API is slow, unavailable, or returns specific data you control.

```typescript
// MOCK a response — replace real API data with test data
await page.route('/api/users', async route => {
    await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify([
            { id: 1, name: 'Test User', email: 'test@example.com' }
        ])
    });
});
await page.goto('/users');
// UI shows "Test User" — guaranteed, no matter what the real API returns

// SIMULATE server error — test how UI handles 500
await page.route('/api/products', async route => {
    await route.fulfill({ status: 500, body: 'Internal Server Error' });
});
await page.goto('/products');
await expect(page.getByText('Something went wrong')).toBeVisible();
// Verify the error state displays correctly

// SIMULATE slow response — test loading spinner appears
await page.route('/api/slow-data', async route => {
    await new Promise(resolve => setTimeout(resolve, 3000));  // 3 second delay
    await route.fulfill({ status: 200, body: JSON.stringify({ data: [] }) });
});
await page.goto('/dashboard');
await expect(page.getByTestId('loading-spinner')).toBeVisible(); // spinner shows during delay

// BLOCK analytics trackers — speed up tests
await page.route('**/google-analytics.com/**', route => route.abort());
await page.route('**/hotjar.com/**', route => route.abort());

// MODIFY a real response — change one field and let rest through
await page.route('/api/users/1', async route => {
    const response = await route.fetch();          // get real response
    const body = await response.json();
    body.role = 'admin';                           // change one field
    await route.fulfill({ response, body: JSON.stringify(body) });
});

// SPY on a request — don't change it, just capture it
const requestPromise = page.waitForRequest('/api/checkout');
await page.click('#buyNowBtn');
const request = await requestPromise;
const requestBody = request.postDataJSON();
expect(requestBody.items).toHaveLength(3);
expect(requestBody.total).toBeGreaterThan(0);
```

**Why this matters:** Testing UI error states, loading states, and edge cases would require the backend to deliberately fail — page.route() removes that dependency. Your UI tests are independent of the API's actual data.

---

## SECTION — TEST REPORTING FOR PLAYWRIGHT

---

### Three Reporting Options

```
Option A: Playwright Built-in HTML Report  → zero setup, best for Playwright
Option B: List / Dot Reporter             → CI terminal output
Option C: Allure for Playwright           → setup needed, most professional
```

---

### OPTION A — Playwright Built-in HTML Report (Zero Setup — Best Option)

**This is Playwright's own report — richer than anything you'll need for most projects.**

#### Setup in playwright.config.ts

```typescript
export default defineConfig({
  reporter: [
    // HTML report — opens in browser
    ['html', {
      outputFolder: 'playwright-report',  // where to save
      open: 'on-failure',                 // auto-open if tests fail
      // open: 'never'   ← for CI (never auto-open)
      // open: 'always'  ← always open after every run
    }],

    // Also show results in terminal while running
    ['list'],
  ],

  use: {
    // These get attached to the HTML report automatically:
    screenshot: 'only-on-failure',  // screenshot saved when test fails
    video:      'retain-on-failure', // video saved when test fails
    trace:      'on-first-retry',    // trace saved when test retries
  },
});
```

#### Run Tests and Open Report

```powershell
# Run tests — generates playwright-report/ folder
npx playwright test

# Open the HTML report in browser
npx playwright show-report

# Open a specific report folder
npx playwright show-report playwright-report/

# Run and auto-open report when done
npx playwright test --reporter=html
```

#### What the Playwright HTML Report Shows

```
Summary bar:
  ✓ 14 passed   ✗ 1 failed   ↻ 0 flaky   ○ 0 skipped

Filter by:
  Status: passed / failed / skipped / flaky
  Project: chromium / firefox / webkit
  Duration: slow tests first

Each test row:
  ✓ GET all users returns 200 with data      chromium   512ms
  ✗ POST create user — response body check  chromium   891ms  ← CLICK TO EXPAND

Expanded failure view:
  Error:
    expect(received).toBe(expected)
    Expected: 201
    Received: 400

  Call log:
    - navigating to https://reqres.in/api/users
    - apiRequestContext.post /users
    - expect.toBe with timeout 5000ms
    - received value 400

  Screenshot: [attached image of page at moment of failure]
  Video:      [click to replay the test run]
  Trace:      [click to open full step-by-step trace viewer]
```

---

### OPTION B — Terminal Reporters for CI/CD

```typescript
// playwright.config.ts
reporter: [
  ['list'],      // shows each test as it runs — good for local development
  ['dot'],       // minimal dots — good for CI (less output)
  ['line'],      // one line per test — compact
  ['json', { outputFile: 'results.json' }],       // machine-readable
  ['junit', { outputFile: 'results/junit.xml' }], // for Jenkins/GitHub Actions
];
```

```powershell
# Run with specific reporter
npx playwright test --reporter=list
npx playwright test --reporter=dot
npx playwright test --reporter=html
npx playwright test --reporter=junit

# Multiple reporters at once (defined in config is better)
npx playwright test --reporter=html,list
```

**List reporter output:**
```
Running 14 tests using 4 workers

  ✓  tests/api/users.spec.ts:8 › GET all users returns 200     (512ms)
  ✓  tests/api/users.spec.ts:21 › GET user by ID returns 200   (198ms)
  ✗  tests/api/users.spec.ts:34 › POST create user              (891ms)
  ✓  tests/api/users.spec.ts:52 › DELETE user returns 204       (203ms)

  1 failed
  1) tests/api/users.spec.ts:34 › POST create user
     Error: expect(received).toBe(expected)
     Expected: 201, Received: 400
```

---

### OPTION C — Allure Report for Playwright (TypeScript)

#### STEP 1 — Install Allure for Playwright

```powershell
npm install --save-dev allure-playwright
npm install --save-dev @playwright/test
```

#### STEP 2 — Configure in playwright.config.ts

```typescript
import { defineConfig } from '@playwright/test';

export default defineConfig({
  reporter: [
    ['allure-playwright', {
      detail: true,         // include step details
      outputFolder: 'allure-results',  // where to save raw data
      suiteTitle: false,
    }],
    ['list'],  // keep terminal output too
  ],
});
```

#### STEP 3 — Add Allure Decorators to Tests

```typescript
import { test, expect } from '@playwright/test';
import { allure } from 'allure-playwright';

test.describe('User API Tests', () => {

  test('GET all users returns 200 with data', async ({ request }) => {
    // Add metadata to Allure report
    allure.epic('User Management');
    allure.feature('GET Users');
    allure.story('List all users');
    allure.severity('critical');
    allure.description('Verify GET /users returns 200 with non-empty user list');
    allure.link('https://jira.company.com/PROJ-101', 'JIRA Ticket');

    // Steps — each shows as a step in the report
    await allure.step('Send GET request to /users', async () => {
      const response = await request.get('/users');
      expect(response.status()).toBe(200);

      await allure.step('Verify response body', async () => {
        const body = await response.json();
        expect(body.data.length).toBeGreaterThan(0);

        // Attach response body to report for debugging
        allure.attachment('Response Body', JSON.stringify(body, null, 2), 'application/json');
      });
    });
  });

  test('POST create user returns 201 @smoke', async ({ request }) => {
    allure.severity('normal');
    allure.tag('smoke');
    allure.tag('regression');

    const response = await request.post('/users', {
      data: { name: 'QA Engineer', job: 'Tester' }
    });
    expect(response.status()).toBe(201);
  });
});
```

#### STEP 4 — Run Tests

```powershell
npx playwright test
# allure-results/ folder is created with raw JSON files
```

#### STEP 5 — Install Allure CLI and Open Report

```powershell
# Install Allure CLI (one time)
choco install allure           # Windows Admin PowerShell
# OR
npm install -g allure-commandline   # via npm (no admin needed)

# Verify:
allure --version

# Open live report in browser
allure serve allure-results/

# Generate static HTML
allure generate allure-results/ --clean -o allure-report/
allure open allure-report/
```

---

### Screenshots and Videos — Automatic Attachment

```typescript
// playwright.config.ts — these attach automatically to the report on failure
use: {
  screenshot: 'only-on-failure',   // saved as PNG, attached to HTML report
  video:      'retain-on-failure', // saved as WebM, attached to HTML report
  trace:      'on-first-retry',    // saved as ZIP, viewable in trace viewer
},

// What appears in HTML report for failed test:
// [Screenshot] → click to view
// [Video]      → click to play back
// [Trace]      → click to open step-by-step trace viewer
```

**Trace viewer** (most powerful Playwright debug tool):
```powershell
# Open trace file manually
npx playwright show-trace test-results/test-name/trace.zip

# OR — it's linked directly in the HTML report
# Click the test → click "Trace" button → opens in browser
# Shows: every action, before/after screenshot, network requests, console
```

---

### Playwright Report — Complete Command Reference

```powershell
# ── RUN + REPORT ─────────────────────────────────────────────────────────────
npx playwright test                           # run all tests, generate report
npx playwright test --reporter=html           # explicitly use HTML reporter
npx playwright show-report                    # open the last generated report

# ── SPECIFIC REPORTER ─────────────────────────────────────────────────────────
npx playwright test --reporter=list           # show each test as it runs
npx playwright test --reporter=dot            # minimal dots for CI
npx playwright test --reporter=junit          # XML for Jenkins/GitHub Actions

# ── ALLURE ────────────────────────────────────────────────────────────────────
npx playwright test                           # generates allure-results/ (if configured)
allure serve allure-results/                  # live report in browser
allure generate allure-results/ -o allure-report/ --clean
allure open allure-report/

# ── TRACE VIEWER ──────────────────────────────────────────────────────────────
npx playwright show-trace test-results/*/trace.zip   # open trace file

# ── UPDATE VISUAL BASELINES ───────────────────────────────────────────────────
npx playwright test --update-snapshots        # update screenshot baselines
```

---

### GitHub Actions — Upload Report as Artifact

```yaml
# .github/workflows/playwright.yml
- name: Run Playwright tests
  run: npx playwright test

- name: Upload HTML report
  uses: actions/upload-artifact@v3
  if: always()           # upload even if tests fail
  with:
    name: playwright-report
    path: playwright-report/
    retention-days: 30

# After workflow runs:
# GitHub Actions → your workflow run → Artifacts → download playwright-report.zip
# Extract → open index.html in browser
```

---

### Q: What reporting do you use in Playwright?

```
For Playwright I use two layers:

1. Playwright's built-in HTML reporter for local development and team sharing.
   It attaches screenshots on failure automatically and lets me replay the
   test as a video — this is the fastest way to understand what went wrong.

2. For CI/CD I add --reporter=junit alongside HTML to generate JUnit XML.
   GitHub Actions/Jenkins reads the XML and shows pass/fail counts inline
   in the pipeline dashboard without downloading any files.

3. For professional stakeholder reporting I use Allure — it adds severity
   levels, story mapping, and trend graphs that show pass rate over sprints.

The trace viewer is the most powerful debugging tool — when a test fails in
CI at 3am, the trace file attached to the HTML report shows me every action,
every network request, and before/after screenshots for each step. I can
debug a CI failure in minutes without re-running the test.
```

*End of File 05 — Playwright UI Testing Complete Interview Q&A Guide | Reporting Added*
