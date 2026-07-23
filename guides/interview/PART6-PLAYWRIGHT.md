# Part 6 — Playwright | 70 Questions | Full Answers + TypeScript Code

> CV context: Kupeshanth used Playwright with JavaScript at Qoria Lanka (GitHub Actions, GCP), wrote Playwright API tests, and worked on Playwright UI automation. These 70 questions cover everything from basics to advanced patterns.

---

## Q1. What is Playwright? Why choose it over Selenium?

**Playwright** is a Node.js-based end-to-end testing framework by Microsoft that supports Chromium, Firefox, and WebKit with a single API.

**Key differences from Selenium:**

| Feature | Playwright | Selenium |
|---|---|---|
| Protocol | CDP / WebSocket (direct) | WebDriver (HTTP) |
| Auto-waiting | Built-in (6 checks) | Manual waits required |
| Network mocking | Built-in `page.route()` | Needs proxy (BrowserMob) |
| Multi-browser | Chromium, Firefox, WebKit | Chrome, Firefox, Edge, Safari |
| Speed | Faster (direct protocol) | Slower (HTTP round-trips) |
| Parallel | Built-in workers | Needs TestNG/Selenium Grid |
| Trace viewer | Built-in | Not available |
| API testing | Built-in (`request` fixture) | Separate (RestAssured) |
| Visual testing | Built-in screenshots/diff | Needs Applitools/Percy |
| Shadow DOM | Supported | Difficult |
| iframes | `frameLocator()` | Complex switching |
| Language | JS/TS, Python, Java, C# | All mainstream languages |

**Why Playwright wins for modern web:**
- No flakiness from timing: auto-wait handles it.
- Network interception at test level — no proxy needed.
- Trace viewer = full debugging timeline.
- Single command to run cross-browser.

---

## Q2. What protocol does Playwright use? (CDP vs WebDriver)

**Playwright uses a direct binary protocol over WebSocket** — not the WebDriver protocol.

- **Chromium**: Uses **Chrome DevTools Protocol (CDP)** directly.
- **Firefox**: Playwright uses a custom protocol extension (Firefox Remote Protocol).
- **WebKit**: Uses a custom WebKit-specific protocol.

**WebDriver (Selenium approach):**
```
Test → HTTP → WebDriver Server → HTTP → Browser
(slow round-trip for every command)
```

**Playwright approach:**
```
Test → WebSocket → Browser binary directly
(persistent connection, much faster)
```

**Consequences:**
- Playwright commands execute faster (no HTTP overhead per command).
- Playwright can intercept network at browser level (CDP).
- Playwright can manipulate browser internals (localStorage, service workers).
- Playwright trace files record everything via CDP.

---

## Q3. What is auto-waiting? What 6 checks does it perform?

**Auto-waiting** means Playwright automatically waits for an element to be in the right state before performing an action. You don't need explicit `waitForElement` calls.

**The 6 checks Playwright performs before acting:**

1. **Attached** — element exists in the DOM.
2. **Visible** — element is not hidden (`display:none`, `visibility:hidden`, zero opacity).
3. **Stable** — element is not animating (CSS transitions complete).
4. **Receives events** — element is not obscured by another element covering it.
5. **Enabled** — element is not disabled (for inputs/buttons).
6. **Editable** — element is not read-only (for `fill()` actions).

```typescript
import { test, expect } from '@playwright/test';

test('auto-waiting in action', async ({ page }) => {
  await page.goto('https://example.com');

  // Playwright waits for button to be: attached + visible + stable + enabled
  // No explicit wait needed
  await page.getByRole('button', { name: 'Submit' }).click();

  // Playwright waits for input to be: attached + visible + stable + enabled + editable
  await page.getByLabel('Email').fill('user@test.com');

  // Playwright waits for text to be visible
  await expect(page.getByText('Success')).toBeVisible();
});
```

**Default timeout:** 30 seconds per action (configurable in `playwright.config.ts`).

---

## Q4. What is the page, context, browser hierarchy?

```
Browser (1)
  └── BrowserContext (1 or many — isolated sessions)
        └── Page (1 or many — browser tabs)
```

**Browser:** The browser process (Chromium, Firefox, WebKit).

**BrowserContext:** An isolated incognito-like session with its own:
- Cookies
- localStorage
- SessionStorage
- Auth state
- Permissions

**Page:** A single browser tab/window inside a context.

```typescript
import { chromium } from '@playwright/test';

async function demo() {
  const browser = await chromium.launch({ headless: false });

  // Context 1: Admin user
  const adminContext = await browser.newContext({
    storageState: 'playwright/.auth/admin.json'
  });

  // Context 2: Regular user
  const userContext = await browser.newContext({
    storageState: 'playwright/.auth/user.json'
  });

  const adminPage = await adminContext.newPage();
  const userPage  = await userContext.newPage();

  // Both run in same browser, isolated sessions
  await adminPage.goto('https://app.com/admin');
  await userPage.goto('https://app.com/dashboard');

  await browser.close();
}
```

**In Playwright tests:** The `browser`, `context`, and `page` fixtures are automatically provided. The default context is fresh for each test (isolated by default).

---

## Q5. What is the request fixture for API testing?

The `request` fixture is Playwright's **built-in API client** — no Axios or Fetch needed. It sends HTTP requests and validates responses directly in Playwright tests.

```typescript
import { test, expect } from '@playwright/test';

test('GET /users returns 200', async ({ request }) => {
  const response = await request.get('https://reqres.in/api/users?page=2');

  expect(response.status()).toBe(200);

  const body = await response.json();
  expect(body.page).toBe(2);
  expect(body.data).toHaveLength(6);
});

test('POST /users creates a user', async ({ request }) => {
  const response = await request.post('https://reqres.in/api/users', {
    data: {
      name: 'Kupeshanth',
      job: 'QA Engineer'
    }
  });

  expect(response.status()).toBe(201);

  const body = await response.json();
  expect(body.name).toBe('Kupeshanth');
  expect(body.id).toBeTruthy();
});

test('with auth header', async ({ request }) => {
  const response = await request.get('https://api.example.com/profile', {
    headers: {
      'Authorization': `Bearer ${process.env.AUTH_TOKEN}`,
      'Content-Type': 'application/json'
    }
  });

  expect(response.ok()).toBeTruthy(); // status 200-299
});
```

**Base URL in playwright.config.ts:**
```typescript
export default defineConfig({
  use: {
    baseURL: 'https://reqres.in',
  }
});

// Then in test:
const response = await request.get('/api/users'); // resolves against baseURL
```

---

## Q6. What are all locator strategies? Which is best and why?

```typescript
// 1. getByRole — BEST (uses ARIA semantics)
page.getByRole('button', { name: 'Submit' })
page.getByRole('textbox', { name: 'Email' })
page.getByRole('link', { name: 'Home' })
page.getByRole('checkbox', { name: 'Remember me' })

// 2. getByLabel — great for form inputs
page.getByLabel('Email address')
page.getByLabel('Password')

// 3. getByPlaceholder
page.getByPlaceholder('Enter your email')

// 4. getByText
page.getByText('Welcome back')
page.getByText('Error', { exact: true })

// 5. getByAltText — for images
page.getByAltText('Company logo')

// 6. getByTitle — for tooltip/title attributes
page.getByTitle('Delete this item')

// 7. getByTestId — custom data attributes
page.getByTestId('login-button')  // finds [data-testid="login-button"]

// 8. locator (CSS) — fallback
page.locator('#submit-btn')
page.locator('.btn-primary')
page.locator('button[type="submit"]')

// 9. locator (XPath) — last resort
page.locator('//button[@type="submit"]')
```

**Priority order (best → worst):**
1. `getByRole` — mirrors accessibility tree, resilient to style changes.
2. `getByLabel` — tied to form semantics.
3. `getByPlaceholder` / `getByText` — human-readable.
4. `getByTestId` — requires dev cooperation to add `data-testid`.
5. CSS selectors — breaks if class names change.
6. XPath — brittle, verbose.

**Why `getByRole` is best:** It finds elements the way screen readers do — by semantic meaning, not fragile implementation details. A button is a button whether it uses `<button>`, `<input type="button">`, or `role="button"`.

---

## Q7. What is getByRole and what ARIA roles exist?

`getByRole` queries the **accessibility tree** using ARIA roles, mirroring how assistive technologies see the page.

```typescript
// Common ARIA roles with examples
page.getByRole('button', { name: 'Log In' })
page.getByRole('link', { name: 'Go to Dashboard' })
page.getByRole('textbox', { name: 'Search' })
page.getByRole('checkbox', { name: 'Agree to terms' })
page.getByRole('radio', { name: 'Male' })
page.getByRole('combobox', { name: 'Country' })       // select dropdown
page.getByRole('listbox')
page.getByRole('option', { name: 'Australia' })
page.getByRole('heading', { name: 'Dashboard', level: 1 })
page.getByRole('img', { name: 'User avatar' })
page.getByRole('navigation')
page.getByRole('main')
page.getByRole('dialog', { name: 'Confirm Delete' })
page.getByRole('alert')
page.getByRole('tab', { name: 'Settings' })
page.getByRole('tabpanel')
page.getByRole('menuitem', { name: 'Profile' })
page.getByRole('cell', { name: 'John Doe' })          // table cell
page.getByRole('row', { name: /John/ })
page.getByRole('columnheader', { name: 'Name' })

// Options:
page.getByRole('button', {
  name: /submit/i,       // case-insensitive regex
  exact: true,           // exact string match
  expanded: true,        // aria-expanded
  checked: true,         // aria-checked
  pressed: false,        // aria-pressed
  level: 2,             // heading level
  disabled: false        // aria-disabled
})
```

---

## Q8. What is the difference between strict mode and non-strict?

**Strict mode (default):** If a locator matches **multiple elements**, Playwright throws an error. Forces you to write specific locators.

**Non-strict:** Use `.first()`, `.last()`, `.nth()`, or `.all()` to handle multiple matches.

```typescript
// STRICT MODE (default) — throws if multiple elements match
await page.getByRole('button', { name: 'Delete' }).click();
// Error if there are 3 Delete buttons on the page

// NON-STRICT — explicitly handle multiple
await page.getByRole('button', { name: 'Delete' }).first().click();
await page.getByRole('button', { name: 'Delete' }).last().click();
await page.getByRole('button', { name: 'Delete' }).nth(1).click(); // 0-indexed

// Get all matches
const deleteButtons = page.getByRole('button', { name: 'Delete' });
const count = await deleteButtons.count();
console.log(`Found ${count} delete buttons`);

// Iterate all
for (const btn of await deleteButtons.all()) {
  await expect(btn).toBeVisible();
}
```

**Why strict mode is good:** Forces precise, unambiguous selectors. Non-strict selectors that accidentally match multiple elements cause flaky tests.

---

## Q9. What is toHaveText vs toContainText?

```typescript
const element = page.getByRole('heading', { level: 1 });

// toHaveText — EXACT match (default) or regex
await expect(element).toHaveText('Welcome to Dashboard');
await expect(element).toHaveText(/Welcome/i); // regex — partial match OK

// toContainText — SUBSTRING match (case-sensitive by default)
await expect(element).toContainText('Dashboard');
await expect(element).toContainText('welcome', { ignoreCase: true });

// Example HTML: <h1>Welcome to Dashboard</h1>
// toHaveText('Dashboard')       → FAILS (not exact)
// toHaveText('Welcome to Dashboard') → PASSES
// toContainText('Dashboard')   → PASSES (substring)
// toContainText('dashboard')   → FAILS (case-sensitive)
// toContainText('dashboard', { ignoreCase: true }) → PASSES

// Multiple text values (for lists)
const items = page.getByRole('listitem');
await expect(items).toHaveText(['Item 1', 'Item 2', 'Item 3']); // exact, in order
await expect(items).toContainText(['Item 1', 'Item 3']);          // subset OK
```

---

## Q10. What is expect.soft() and when to use it?

`expect.soft()` is a **soft assertion** — when it fails, the test continues running. All soft assertion failures are reported at the end.

```typescript
import { test, expect } from '@playwright/test';

test('validate user profile page', async ({ page }) => {
  await page.goto('/profile');

  // Soft assertions — ALL run even if one fails
  await expect.soft(page.getByTestId('username')).toHaveText('john_doe');
  await expect.soft(page.getByTestId('email')).toHaveText('john@example.com');
  await expect.soft(page.getByTestId('role')).toHaveText('Admin');
  await expect.soft(page.getByTestId('status')).toHaveText('Active');

  // Regular assertion — stops if fails
  await expect(page.getByRole('heading', { name: 'Profile' })).toBeVisible();

  // At end of test, if any soft assertion failed, test is FAILED
  // But you saw ALL the failures at once
});
```

**When to use:**
- Validating multiple fields in a form/API response.
- Page content checks where you want to see all errors at once.

**When NOT to use:**
- Preconditions: if page didn't load, soft-asserting its content is pointless.
- Critical path: if login fails, soft-assert nothing after it.

---

## Q11. List all web-first assertions with examples.

Web-first assertions **automatically retry** until the condition is true or timeout expires.

```typescript
// Element state
await expect(locator).toBeVisible();
await expect(locator).toBeHidden();
await expect(locator).toBeEnabled();
await expect(locator).toBeDisabled();
await expect(locator).toBeChecked();
await expect(locator).not.toBeChecked();
await expect(locator).toBeEditable();
await expect(locator).toBeFocused();
await expect(locator).toBeEmpty();          // input has no value

// Text content
await expect(locator).toHaveText('exact text');
await expect(locator).toHaveText(/regex/i);
await expect(locator).toContainText('partial');
await expect(locator).toHaveValue('input value');  // for inputs
await expect(locator).toHaveValues(['opt1', 'opt2']); // for multi-select

// Attributes and classes
await expect(locator).toHaveAttribute('href', '/home');
await expect(locator).toHaveClass('active');
await expect(locator).toHaveClass(/btn-/);
await expect(locator).toHaveId('submit-btn');

// Count
await expect(locator).toHaveCount(5);

// CSS properties
await expect(locator).toHaveCSS('color', 'rgb(255, 0, 0)');

// Page-level
await expect(page).toHaveTitle('Dashboard - MyApp');
await expect(page).toHaveTitle(/Dashboard/);
await expect(page).toHaveURL('https://example.com/dashboard');
await expect(page).toHaveURL(/\/dashboard/);

// Screenshot
await expect(page).toHaveScreenshot('dashboard.png');
await expect(locator).toHaveScreenshot('button.png');

// With custom timeout
await expect(locator).toBeVisible({ timeout: 10_000 });
```

---

## Q12. What is page.route() for API mocking? Full code example.

`page.route()` intercepts network requests and returns custom responses — enabling API mocking without a real backend.

```typescript
import { test, expect } from '@playwright/test';

test('mock API response', async ({ page }) => {
  // Intercept GET /api/users and return mock data
  await page.route('**/api/users', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        users: [
          { id: 1, name: 'Alice', role: 'Admin' },
          { id: 2, name: 'Bob',   role: 'User' }
        ]
      })
    });
  });

  await page.goto('/users');

  // Verify UI renders mock data
  await expect(page.getByText('Alice')).toBeVisible();
  await expect(page.getByText('Bob')).toBeVisible();
});

test('mock 500 error', async ({ page }) => {
  await page.route('**/api/products', async (route) => {
    await route.fulfill({
      status: 500,
      body: JSON.stringify({ error: 'Internal Server Error' })
    });
  });

  await page.goto('/products');
  await expect(page.getByText('Something went wrong')).toBeVisible();
});

test('modify real response', async ({ page }) => {
  await page.route('**/api/user/profile', async (route) => {
    // Get actual response first
    const response = await route.fetch();
    const json = await response.json();

    // Modify one field
    json.role = 'SUPERADMIN';

    await route.fulfill({
      response,
      json  // override body with modified json
    });
  });

  await page.goto('/profile');
  await expect(page.getByTestId('role')).toHaveText('SUPERADMIN');
});
```

---

## Q13. How do you spy on a network request?

```typescript
import { test, expect } from '@playwright/test';

test('spy on API call triggered by button click', async ({ page }) => {
  await page.goto('/dashboard');

  // Set up listener BEFORE the action that triggers the request
  const responsePromise = page.waitForResponse('**/api/analytics');

  // Trigger the action
  await page.getByRole('button', { name: 'Load Analytics' }).click();

  // Wait for and inspect the response
  const response = await responsePromise;

  expect(response.status()).toBe(200);
  const body = await response.json();
  expect(body).toHaveProperty('totalUsers');
  console.log('Analytics data:', body);
});

test('spy on request body', async ({ page }) => {
  await page.goto('/login');

  const requestPromise = page.waitForRequest('**/api/auth/login');

  await page.getByLabel('Email').fill('user@test.com');
  await page.getByLabel('Password').fill('pass123');
  await page.getByRole('button', { name: 'Login' }).click();

  const request = await requestPromise;
  const body = request.postDataJSON();
  expect(body.email).toBe('user@test.com');
});
```

---

## Q14. How do you simulate a 500 error from the backend?

```typescript
import { test, expect } from '@playwright/test';

test('UI handles 500 gracefully', async ({ page }) => {
  // Intercept the API call and return 500
  await page.route('**/api/orders', async (route) => {
    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({
        error: 'Internal Server Error',
        message: 'Database connection failed'
      })
    });
  });

  await page.goto('/orders');

  // Assert error state is shown to user
  await expect(page.getByRole('alert')).toBeVisible();
  await expect(page.getByRole('alert')).toContainText('Something went wrong');

  // Assert no data is shown
  await expect(page.getByRole('table')).not.toBeVisible();
});

test('UI shows retry on 503', async ({ page }) => {
  await page.route('**/api/inventory', async (route) => {
    await route.fulfill({
      status: 503,
      headers: { 'Retry-After': '30' },
      body: 'Service Unavailable'
    });
  });

  await page.goto('/inventory');
  await expect(page.getByText('Service temporarily unavailable')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Retry' })).toBeVisible();
});
```

---

## Q15. How do you block analytics trackers?

```typescript
import { test, expect } from '@playwright/test';

test('block third-party trackers', async ({ page }) => {
  // Abort requests to tracking domains
  await page.route('**googletag**', route => route.abort());
  await page.route('**analytics.google.com**', route => route.abort());
  await page.route('**facebook.com/tr**', route => route.abort());
  await page.route('**hotjar.com**', route => route.abort());
  await page.route('**mixpanel.com**', route => route.abort());

  await page.goto('https://example.com');

  // Test runs faster without tracker calls
  await expect(page).toHaveTitle(/Example/);
});

// Better: set globally in playwright.config.ts
// playwright.config.ts
export default defineConfig({
  use: {
    // Block all analytics globally
    extraHTTPHeaders: {},
  },
  // Use setup file to block globally:
});

// Or in a fixture:
test.beforeEach(async ({ page }) => {
  await page.route(/google-analytics|googletagmanager|hotjar|segment/, route => {
    route.abort();
  });
});
```

---

## Q16. What is page.waitForResponse()? When do you use it?

`page.waitForResponse()` waits for a specific network response — useful when an action triggers an async API call and you need to confirm it completed before proceeding.

```typescript
import { test, expect } from '@playwright/test';

test('wait for API after button click', async ({ page }) => {
  await page.goto('/products');

  // Must set up BEFORE the action that triggers the request
  const searchResponse = page.waitForResponse(
    response =>
      response.url().includes('/api/search') && response.status() === 200
  );

  await page.getByPlaceholder('Search products').fill('laptop');
  await page.getByRole('button', { name: 'Search' }).click();

  // Wait for actual API response (not just UI update)
  const response = await searchResponse;
  const data = await response.json();
  expect(data.results.length).toBeGreaterThan(0);

  // Now UI should reflect the results
  await expect(page.getByRole('list')).toBeVisible();
});

test('verify correct payload was sent', async ({ page }) => {
  await page.goto('/checkout');

  const orderResponse = page.waitForResponse('**/api/orders');

  await page.getByRole('button', { name: 'Place Order' }).click();

  const resp = await orderResponse;
  expect(resp.status()).toBe(201);
});
```

---

## Q17. What is page.waitForSelector()? All state options.

`page.waitForSelector()` waits for a CSS/XPath selector to reach a specific state.

```typescript
// State options:
// 'attached'   — element exists in DOM (default)
// 'visible'    — exists AND is visible
// 'detached'   — element is removed from DOM
// 'hidden'     — not visible (hidden or not in DOM)

// Wait for element to appear
await page.waitForSelector('#loading-spinner', { state: 'visible' });

// Wait for spinner to DISAPPEAR (loading complete)
await page.waitForSelector('#loading-spinner', { state: 'hidden' });
// OR
await page.waitForSelector('#loading-spinner', { state: 'detached' });

// Wait for element to be in DOM (may still be invisible)
await page.waitForSelector('.data-table', { state: 'attached' });

// With timeout
await page.waitForSelector('.result-item', {
  state: 'visible',
  timeout: 15_000  // 15 seconds
});
```

**Prefer locator assertions over waitForSelector:**
```typescript
// Old style (waitForSelector)
await page.waitForSelector('.result', { state: 'visible' });
const text = await page.textContent('.result');

// Modern style (preferred)
const result = page.locator('.result');
await expect(result).toBeVisible();
const text = await result.textContent();
```

---

## Q18. What is page.waitForURL()?

Waits for the page URL to match a string, glob, or regex. Used after navigation actions.

```typescript
// After clicking login button
await page.getByRole('button', { name: 'Login' }).click();

// Wait for redirect to dashboard
await page.waitForURL('**/dashboard');
// OR exact URL
await page.waitForURL('https://app.example.com/dashboard');
// OR regex
await page.waitForURL(/\/dashboard$/);

// With timeout
await page.waitForURL(/dashboard/, { timeout: 10_000 });

// In practice — after form submit
test('login redirects to dashboard', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Email').fill('admin@test.com');
  await page.getByLabel('Password').fill('Admin123!');
  await page.getByRole('button', { name: 'Sign In' }).click();

  await page.waitForURL('**/dashboard');  // waits for redirect
  await expect(page).toHaveURL(/dashboard/);
});
```

---

## Q19. What is page.waitForFunction()?

Executes a JavaScript function in the browser context and waits until it returns a truthy value.

```typescript
// Wait for a custom JavaScript condition
await page.waitForFunction(() => {
  // Executed in browser context
  return document.querySelectorAll('.product-card').length > 5;
});

// Wait for global variable to be set by app
await page.waitForFunction(() => window.__appReady === true);

// Wait for specific text in document
await page.waitForFunction(
  (text) => document.body.innerText.includes(text),
  'Order confirmed'   // argument passed to function
);

// With timeout
await page.waitForFunction(
  () => window.cartCount > 0,
  null,
  { timeout: 5000 }
);

// Real example: wait for animations to complete
await page.waitForFunction(() => {
  const animations = document.getAnimations();
  return animations.every(a => a.playState !== 'running');
});
```

---

## Q20. What is page.waitForLoadState()? All options.

Waits for the page to reach a specific load state after navigation.

```typescript
// Load states:
// 'load'            — 'load' event fired (default)
// 'domcontentloaded'— DOM parsed, before images/iframes
// 'networkidle'     — no network requests for 500ms

await page.goto('https://example.com');
await page.waitForLoadState('load');               // default
await page.waitForLoadState('domcontentloaded');   // faster
await page.waitForLoadState('networkidle');        // wait for all async calls

// After click that causes navigation
await page.getByRole('link', { name: 'Products' }).click();
await page.waitForLoadState('networkidle');

// Practical use — SPA with dynamic content
test('SPA navigation', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('link', { name: 'Dashboard' }).click();

  // SPA: DOM doesn't reload, but API calls happen
  await page.waitForLoadState('networkidle');  // wait for all API calls

  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
});
```

**Caution:** `networkidle` can be unreliable with apps that have constant polling (websockets, heartbeats). Prefer specific `waitForResponse()` or element assertions.

---

## Q21. Why should you never use waitForTimeout()?

`page.waitForTimeout(ms)` is a **static sleep** — it pauses for a fixed time regardless of whether the UI is ready. This is the primary cause of flaky tests.

```typescript
// BAD — fragile and slow
await page.getByRole('button', { name: 'Load Data' }).click();
await page.waitForTimeout(3000); // Hopes 3 seconds is enough
await expect(page.getByRole('table')).toBeVisible();

// GOOD — waits exactly as long as needed
await page.getByRole('button', { name: 'Load Data' }).click();
await expect(page.getByRole('table')).toBeVisible(); // auto-waits

// GOOD — wait for specific API response
const dataResponse = page.waitForResponse('**/api/data');
await page.getByRole('button', { name: 'Load Data' }).click();
await dataResponse;
await expect(page.getByRole('table')).toBeVisible();
```

**Problems with waitForTimeout:**
1. Slow: always waits the full time even when ready in 0.1 seconds.
2. Flaky: on slow CI machines the timeout may not be enough.
3. Masks root causes: if you need a sleep, something else is wrong.

**Legitimate use:** Debugging only. Never in committed test code.

---

## Q22. What is storageState? How do you implement login once?

`storageState` saves/loads browser storage (cookies, localStorage, sessionStorage) to avoid logging in before every test.

**Step 1: auth.setup.ts — login once and save state**

```typescript
// tests/auth.setup.ts
import { test as setup, expect } from '@playwright/test';
import path from 'path';

const authFile = path.join(__dirname, '../playwright/.auth/user.json');

setup('authenticate', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Email').fill('user@test.com');
  await page.getByLabel('Password').fill('User123!');
  await page.getByRole('button', { name: 'Sign In' }).click();

  // Wait until login is complete
  await page.waitForURL('**/dashboard');
  await expect(page.getByRole('navigation')).toBeVisible();

  // Save storage state to file
  await page.context().storageState({ path: authFile });
});
```

**Step 2: playwright.config.ts — wire up setup and use state**

```typescript
import { defineConfig, devices } from '@playwright/test';
import path from 'path';

const authFile = path.join(__dirname, 'playwright/.auth/user.json');

export default defineConfig({
  testDir: './tests',

  projects: [
    // Run auth setup first
    {
      name: 'setup',
      testMatch: '**/auth.setup.ts',
    },

    // All tests use saved auth state
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        storageState: authFile,   // pre-authenticated!
      },
      dependencies: ['setup'],   // setup must run first
    },
  ],
});
```

**Step 3: tests use authenticated context automatically**
```typescript
test('access protected page', async ({ page }) => {
  // Already logged in — storageState is loaded
  await page.goto('/dashboard');
  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
  // No login code needed here
});
```

---

## Q23. What is auth.setup.ts? How does it connect to playwright.config.ts?

`auth.setup.ts` is a **setup project** that runs authentication logic once and saves the browser state. All other test projects depend on it.

**Connection flow:**
```
playwright.config.ts
    └── projects[0]: { name: 'setup', testMatch: '**/auth.setup.ts' }
    └── projects[1]: { name: 'chromium', dependencies: ['setup'],
                       storageState: 'playwright/.auth/user.json' }

auth.setup.ts runs → saves cookies/storage to user.json
chromium tests run → load user.json → already authenticated
```

**Multiple user roles:**
```typescript
// admin.setup.ts
const adminAuthFile = 'playwright/.auth/admin.json';
setup('admin auth', async ({ page }) => {
  await loginAs(page, 'admin@test.com', 'Admin123!');
  await page.context().storageState({ path: adminAuthFile });
});

// playwright.config.ts
projects: [
  { name: 'admin-setup', testMatch: '**/admin.setup.ts' },
  { name: 'user-setup',  testMatch: '**/user.setup.ts'  },
  {
    name: 'admin-tests',
    use: { storageState: 'playwright/.auth/admin.json' },
    dependencies: ['admin-setup'],
  },
  {
    name: 'user-tests',
    use: { storageState: 'playwright/.auth/user.json' },
    dependencies: ['user-setup'],
  },
]
```

**Add to .gitignore:**
```
playwright/.auth/
```

---

## Q24. What are projects in playwright.config.ts? Cross-browser setup.

`projects` lets you run the same tests with different configurations (browsers, devices, viewports, auth states).

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  reporter: [['html'], ['allure-playwright']],

  use: {
    baseURL: 'https://staging.example.com',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },

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

    // Mobile
    {
      name: 'Mobile Chrome',
      use: { ...devices['Pixel 5'] },
    },
    {
      name: 'Mobile Safari',
      use: { ...devices['iPhone 13'] },
    },

    // Tablet
    {
      name: 'iPad',
      use: { ...devices['iPad Pro 11'] },
    },

    // Specific viewport
    {
      name: 'large-desktop',
      use: {
        ...devices['Desktop Chrome'],
        viewport: { width: 2560, height: 1440 },
      },
    },

    // API tests only (no browser)
    {
      name: 'api',
      testMatch: '**/*.api.spec.ts',
      use: { baseURL: 'https://api.example.com' },
    },
  ],
});
```

---

## Q25. What is fullyParallel?

`fullyParallel: true` makes every individual **test** run in parallel across workers. Without it, tests within a file run sequentially (only files are parallelised).

```typescript
// playwright.config.ts
export default defineConfig({
  fullyParallel: true,   // ALL tests across ALL files run in parallel
  workers: 4,            // using 4 worker processes
});

// Without fullyParallel (default):
// File A: test1 → test2 → test3 (sequential within file)
// File B: test1 → test2 → test3 (sequential within file)
// Files A and B run in parallel, but within each file: sequential

// With fullyParallel:
// Every test in every file can run in its own worker simultaneously
```

**Use `test.describe.configure({ mode: 'serial' })` to protect specific tests:**
```typescript
test.describe.configure({ mode: 'serial' }); // this describe block runs sequentially

test.describe('Order flow', () => {
  test('create order', async ({ page }) => { /* ... */ });
  test('verify order', async ({ page }) => { /* ... */ });
  test('cancel order', async ({ page }) => { /* ... */ });
});
```

---

## Q26. What are workers?

`workers` is the number of **parallel worker processes** Playwright uses to run tests.

```typescript
export default defineConfig({
  workers: process.env.CI ? 2 : 4, // fewer workers on CI to avoid resource issues
  // OR as a percentage of CPU cores:
  workers: '50%',  // half of available CPU cores
});
```

**How workers work:**
- Each worker is a separate Node.js process with its own browser instance.
- Tests are distributed across workers by Playwright.
- Worker processes share no state — fully isolated.

**Recommended settings:**

| Environment | Workers |
|---|---|
| Local dev | 4 (or `'50%'`) |
| CI (GitHub Actions 2-core) | 2 |
| CI (8-core) | 4–6 |

```bash
# Override from command line
npx playwright test --workers=1  # debug: sequential
npx playwright test --workers=4
```

---

## Q27. What is trace? When is it captured?

**Trace** is a recording of a test run — includes actions, screenshots, network requests, console logs, and DOM snapshots. Opened in Playwright Trace Viewer for debugging.

```typescript
// playwright.config.ts — trace capture modes
export default defineConfig({
  use: {
    trace: 'off',              // never
    trace: 'on',               // always (large files)
    trace: 'on-first-retry',   // only when test retries (RECOMMENDED)
    trace: 'retain-on-failure',// keep for failed tests only
    trace: 'on-all-retries',   // on every retry
  },
});
```

**Trace captures:**
- Every action (click, fill, navigate)
- Before/after screenshot for each action
- Network requests and responses
- Console messages
- DOM state at each step
- Call stack

**Capture programmatically:**
```typescript
test('debug this test', async ({ page, context }) => {
  await context.tracing.start({ screenshots: true, snapshots: true });

  await page.goto('/app');
  await page.getByRole('button', { name: 'Submit' }).click();

  await context.tracing.stop({ path: 'traces/debug-trace.zip' });
});
```

---

## Q28. How do you open a trace file?

```bash
# Open from command line
npx playwright show-trace trace.zip
npx playwright show-trace test-results/my-test/trace.zip

# After a test run — Playwright auto-links in HTML report
npx playwright test
npx playwright show-report   # HTML report has trace links
```

**What you see in Trace Viewer:**
- Timeline of all actions.
- Before/after screenshots for each step.
- Full network log.
- Console log.
- DOM explorer at any point.

**Generate trace and open:**
```bash
npx playwright test --trace on
npx playwright show-report   # click any failed test → Traces tab
```

---

## Q29. What is the Playwright UI mode?

UI mode is an interactive test runner with a visual interface for writing, running, and debugging tests.

```bash
npx playwright test --ui
```

**Features of UI mode:**
- Run individual tests by clicking.
- Watch mode: re-runs tests on file save.
- Time-travel debugger: step through each action.
- Action timeline with screenshots.
- Network requests panel.
- DOM explorer (Pick Locator tool).
- Console output.

**Keyboard shortcuts in UI mode:**
- `F5` — run selected test
- Click action in timeline → see DOM state at that moment
- "Pick Locator" button → click element in browser → get locator code

---

## Q30. What is playwright codegen?

`codegen` records your browser interactions and generates Playwright test code automatically.

```bash
# Basic — opens browser + recorder
npx playwright codegen https://example.com

# Save to file
npx playwright codegen --output=tests/generated.spec.ts https://example.com

# Specific browser
npx playwright codegen --browser=firefox https://example.com

# Mobile emulation
npx playwright codegen --device="iPhone 13" https://example.com

# With auth
npx playwright codegen --save-storage=auth.json https://example.com
```

**What it generates:**
```typescript
// Generated by codegen
import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('https://example.com/login');
  await page.getByLabel('Email').click();
  await page.getByLabel('Email').fill('user@test.com');
  await page.getByLabel('Password').fill('pass123');
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page).toHaveURL(/.*dashboard/);
});
```

**Use codegen as a starting point**, then refactor for maintainability (extract POM, add assertions).

---

## Q31. How do you handle iframes with frameLocator()?

```typescript
import { test, expect } from '@playwright/test';

test('interact with iframe', async ({ page }) => {
  await page.goto('https://example.com/with-iframe');

  // Get the frame by selector
  const frame = page.frameLocator('iframe[title="Payment Form"]');
  // OR by name
  const frame2 = page.frameLocator('iframe[name="checkout"]');

  // Now interact with elements INSIDE the iframe
  await frame.getByLabel('Card Number').fill('4111111111111111');
  await frame.getByLabel('Expiry').fill('12/28');
  await frame.getByLabel('CVV').fill('123');
  await frame.getByRole('button', { name: 'Pay Now' }).click();

  // Nested iframes
  const nestedFrame = page
    .frameLocator('#outer-frame')
    .frameLocator('#inner-frame');
  await nestedFrame.getByText('Nested content').click();
});
```

**Old way (page.frame) — for comparison:**
```typescript
// Old API — less recommended
const frame = page.frame({ name: 'checkout' });
await frame?.fill('#card-number', '4111111111111111');
```

---

## Q32. How do you handle alerts and dialogs?

```typescript
import { test, expect } from '@playwright/test';

test('handle alert', async ({ page }) => {
  // Set up handler BEFORE the action that triggers the dialog
  page.on('dialog', async dialog => {
    console.log('Dialog type:', dialog.type());      // alert, confirm, prompt
    console.log('Dialog message:', dialog.message());
    await dialog.accept(); // click OK
  });

  await page.goto('/app');
  await page.getByRole('button', { name: 'Delete' }).click();
  // Dialog is auto-handled
});

test('handle confirm — cancel', async ({ page }) => {
  page.on('dialog', async dialog => {
    await dialog.dismiss(); // click Cancel
  });

  await page.getByRole('button', { name: 'Clear Cart' }).click();
  // Verify item still in cart (user cancelled)
  await expect(page.getByRole('listitem')).toBeVisible();
});

test('handle prompt', async ({ page }) => {
  page.on('dialog', async dialog => {
    await dialog.accept('My custom input'); // type value in prompt
  });

  await page.getByRole('button', { name: 'Rename' }).click();
  await expect(page.getByText('My custom input')).toBeVisible();
});
```

---

## Q33. How do you handle new tabs/popup windows?

```typescript
import { test, expect } from '@playwright/test';

test('handle new tab opened by link', async ({ page, context }) => {
  await page.goto('https://example.com');

  // Wait for the new page (tab) to open
  const pagePromise = context.waitForEvent('page');

  // Click link that opens in new tab
  await page.getByRole('link', { name: 'Open in new tab' }).click();

  const newPage = await pagePromise;
  await newPage.waitForLoadState();

  // Interact with new tab
  await expect(newPage).toHaveURL(/new-tab-url/);
  await expect(newPage.getByRole('heading')).toBeVisible();

  // Close new tab, go back to original
  await newPage.close();
  await expect(page).toHaveURL(/original/);
});

test('handle popup window', async ({ page }) => {
  const popupPromise = page.waitForEvent('popup');

  await page.getByRole('button', { name: 'Open Popup' }).click();

  const popup = await popupPromise;
  await popup.waitForLoadState();
  await expect(popup.getByRole('heading', { name: 'Popup Title' })).toBeVisible();
  await popup.getByRole('button', { name: 'Close' }).click();
});
```

---

## Q34. How do you upload files in Playwright?

```typescript
import { test, expect } from '@playwright/test';
import path from 'path';

test('upload single file', async ({ page }) => {
  await page.goto('/upload');

  // Method 1: setInputFiles on the file input
  await page.locator('input[type="file"]').setInputFiles('tests/fixtures/test.pdf');

  await page.getByRole('button', { name: 'Upload' }).click();
  await expect(page.getByText('Upload successful')).toBeVisible();
});

test('upload multiple files', async ({ page }) => {
  await page.goto('/upload-multiple');

  await page.locator('input[type="file"]').setInputFiles([
    'tests/fixtures/image1.png',
    'tests/fixtures/image2.png',
    'tests/fixtures/document.pdf',
  ]);

  await expect(page.getByText('3 files selected')).toBeVisible();
});

test('upload via file chooser dialog', async ({ page }) => {
  const fileChooserPromise = page.waitForEvent('filechooser');

  await page.getByRole('button', { name: 'Choose File' }).click();

  const fileChooser = await fileChooserPromise;
  await fileChooser.setFiles(path.join(__dirname, 'fixtures/test.csv'));

  await expect(page.getByText('test.csv')).toBeVisible();
});
```

---

## Q35. How do you download files?

```typescript
import { test, expect } from '@playwright/test';
import path from 'path';
import fs from 'fs';

test('download file', async ({ page }) => {
  await page.goto('/reports');

  // Set up download listener BEFORE the action
  const downloadPromise = page.waitForEvent('download');

  await page.getByRole('button', { name: 'Export CSV' }).click();

  const download = await downloadPromise;

  // Assert filename
  expect(download.suggestedFilename()).toBe('report.csv');

  // Save to disk
  const filePath = path.join('downloads', download.suggestedFilename());
  await download.saveAs(filePath);

  // Verify file exists
  expect(fs.existsSync(filePath)).toBe(true);

  // Read and verify content
  const content = fs.readFileSync(filePath, 'utf-8');
  expect(content).toContain('Name,Email,Date');
});
```

---

## Q36. How do you take screenshots in Playwright?

```typescript
import { test, expect } from '@playwright/test';

test('screenshots', async ({ page }) => {
  await page.goto('https://example.com');

  // Full page screenshot
  await page.screenshot({
    path: 'screenshots/full-page.png',
    fullPage: true
  });

  // Viewport screenshot (default)
  await page.screenshot({ path: 'screenshots/viewport.png' });

  // Element screenshot
  const header = page.getByRole('banner');
  await header.screenshot({ path: 'screenshots/header.png' });

  // With clip (specific area)
  await page.screenshot({
    path: 'screenshots/clipped.png',
    clip: { x: 0, y: 0, width: 800, height: 400 }
  });

  // JPEG with quality
  await page.screenshot({
    path: 'screenshots/page.jpg',
    type: 'jpeg',
    quality: 80
  });
});

// Automatic screenshots on failure (playwright.config.ts)
export default defineConfig({
  use: {
    screenshot: 'only-on-failure', // or 'on', 'off'
  }
});
```

---

## Q37. What is visual testing with toHaveScreenshot()?

`toHaveScreenshot()` compares the current screenshot against a **stored baseline**. First run creates the baseline; subsequent runs compare against it.

```typescript
import { test, expect } from '@playwright/test';

test('homepage visual regression', async ({ page }) => {
  await page.goto('https://example.com');

  // First run: creates homepage.png baseline
  // Subsequent runs: compare against baseline
  await expect(page).toHaveScreenshot('homepage.png');
});

test('button visual test', async ({ page }) => {
  await page.goto('/buttons');

  const submitButton = page.getByRole('button', { name: 'Submit' });
  await expect(submitButton).toHaveScreenshot('submit-button.png');
});

test('full page visual', async ({ page }) => {
  await page.goto('/dashboard');

  await expect(page).toHaveScreenshot('dashboard-full.png', {
    fullPage: true,
    maxDiffPixels: 100,      // allow up to 100 pixel differences
    threshold: 0.2           // 20% color difference per pixel allowed
  });
});
```

**Baselines stored in:** `tests/__screenshots__/` (committed to git)

---

## Q38. What is maxDiffPixels and threshold?

Controls how strict visual comparisons are:

```typescript
await expect(page).toHaveScreenshot('page.png', {
  // maxDiffPixels: absolute number of pixels that can differ
  maxDiffPixels: 50,     // up to 50 different pixels allowed

  // maxDiffPixelRatio: percentage of total pixels (0-1)
  maxDiffPixelRatio: 0.01, // up to 1% of pixels can differ

  // threshold: per-pixel color difference tolerance (0-1)
  // 0 = identical color required
  // 1 = any color is the same
  threshold: 0.2,        // 20% color difference allowed per pixel
});

// Set globally in playwright.config.ts
export default defineConfig({
  expect: {
    toHaveScreenshot: {
      maxDiffPixels: 100,
      threshold: 0.2,
    },
  },
});
```

**Guidelines:**
- `threshold: 0` — pixel perfect (usually too strict for anti-aliasing).
- `threshold: 0.1–0.2` — good for rendering differences between OS/GPU.
- `maxDiffPixels: 50–200` — for small dynamic areas like timestamps.

---

## Q39. How do you mask dynamic content in visual tests?

Use `mask` to hide dynamic content (timestamps, ads, avatars) before taking visual snapshots.

```typescript
import { test, expect } from '@playwright/test';

test('visual test with masked dynamic content', async ({ page }) => {
  await page.goto('/dashboard');

  await expect(page).toHaveScreenshot('dashboard.png', {
    mask: [
      page.locator('.timestamp'),          // hide all timestamps
      page.locator('.user-avatar'),         // hide user profile pictures
      page.locator('[data-testid="ads"]'),  // hide ad banners
      page.locator('.chart-canvas'),        // hide animated charts
    ],
    maskColor: '#ff00ff',  // masked areas shown as pink (default: cyan)
  });
});

test('mask with element screenshot', async ({ page }) => {
  const card = page.locator('.product-card').first();

  await expect(card).toHaveScreenshot('product-card.png', {
    mask: [page.locator('.price-badge')],  // prices change frequently
  });
});
```

---

## Q40. How do you update visual baselines?

When UI intentionally changes and baselines need updating:

```bash
# Update ALL baselines
npx playwright test --update-snapshots

# Update specific test baseline
npx playwright test tests/visual.spec.ts --update-snapshots

# In CI — flag to update
npx playwright test --update-snapshots=missing  # only create missing ones
```

**Workflow:**
1. Developer makes UI change.
2. Visual tests fail (new vs old baseline).
3. Review the diff in HTML report.
4. If intentional change: run `--update-snapshots`.
5. Commit the new baseline files.
6. PR review includes reviewing baseline image changes.

**playwright.config.ts — control snapshot directory:**
```typescript
export default defineConfig({
  snapshotDir: './visual-baselines',  // custom directory
  snapshotPathTemplate: '{testDir}/__screenshots__/{projectName}/{testFilePath}/{arg}{ext}',
});
```

---

## Q41. What is the HTML reporter? How to configure?

The HTML reporter generates a rich interactive report showing test results, screenshots, videos, traces, and errors.

```typescript
// playwright.config.ts
export default defineConfig({
  reporter: [
    ['html', {
      outputFolder: 'playwright-report',  // output directory
      open: 'never',                      // 'always', 'never', 'on-failure'
    }]
  ],
});

// Multiple reporters
reporter: [
  ['html'],          // interactive HTML report
  ['list'],          // console output
  ['junit', { outputFile: 'results/junit.xml' }], // for CI
  ['allure-playwright'],                 // Allure
],
```

```bash
# Open report
npx playwright show-report
npx playwright show-report playwright-report  # custom folder

# Generate and open
npx playwright test
npx playwright show-report
```

---

## Q42. What is the JUnit reporter used for?

The JUnit reporter generates XML files compatible with CI systems (GitHub Actions, Jenkins, GitLab CI) that parse JUnit format.

```typescript
// playwright.config.ts
reporter: [
  ['junit', {
    outputFile: 'results/junit-results.xml',
    suiteName: 'Playwright E2E Tests',
  }]
],
```

**GitHub Actions integration:**
```yaml
- name: Run Playwright Tests
  run: npx playwright test

- name: Publish Test Results
  uses: EnricoMi/publish-unit-test-result-action@v2
  if: always()
  with:
    files: results/junit-results.xml
```

**Jenkins integration:**
```groovy
junit 'results/junit-results.xml'
```

---

## Q43. What is the Allure reporter for Playwright?

```bash
npm install -D allure-playwright
```

```typescript
// playwright.config.ts
reporter: [
  ['allure-playwright', {
    detail: true,
    outputFolder: 'allure-results',
    suiteTitle: false,
  }]
],
```

**Test annotations for rich Allure reports:**
```typescript
import { test, expect } from '@playwright/test';
import { allure } from 'allure-playwright';

test('login test with Allure', async ({ page }) => {
  allure.epic('Authentication');
  allure.feature('Login');
  allure.story('Valid credentials');
  allure.severity('critical');
  allure.owner('Kupeshanth');
  allure.tag('smoke');
  allure.description('Verify login with valid credentials redirects to dashboard');

  await allure.step('Navigate to login page', async () => {
    await page.goto('/login');
  });

  await allure.step('Fill credentials', async () => {
    await page.getByLabel('Email').fill('user@test.com');
    await page.getByLabel('Password').fill('pass123');
  });

  await allure.step('Submit and verify', async () => {
    await page.getByRole('button', { name: 'Login' }).click();
    await expect(page).toHaveURL(/dashboard/);
  });
});
```

```bash
npx playwright test
allure serve allure-results
```

---

## Q44. How do you set up GitHub Actions for Playwright?

```yaml
# .github/workflows/playwright.yml
name: Playwright Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    timeout-minutes: 60

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install --with-deps

      - name: Run Playwright tests
        run: npx playwright test
        env:
          BASE_URL: ${{ secrets.STAGING_URL }}
          AUTH_TOKEN: ${{ secrets.API_TOKEN }}
          CI: true

      - name: Upload HTML report
        uses: actions/upload-artifact@v4
        if: always()   # upload even on failure
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: test-results/
          retention-days: 7
```

---

## Q45. How do you upload artifacts in GitHub Actions?

```yaml
- name: Upload Playwright Report
  uses: actions/upload-artifact@v4
  if: always()
  with:
    name: playwright-report-${{ github.run_id }}
    path: |
      playwright-report/
      allure-results/
    retention-days: 30

- name: Upload Screenshots on Failure
  uses: actions/upload-artifact@v4
  if: failure()
  with:
    name: failure-screenshots
    path: test-results/*/screenshots/

- name: Upload Traces on Failure
  uses: actions/upload-artifact@v4
  if: failure()
  with:
    name: traces
    path: test-results/*/trace.zip
```

**Publish Allure report to GitHub Pages:**
```yaml
- name: Generate Allure Report
  if: always()
  run: |
    npm install -g allure-commandline
    allure generate allure-results --clean -o allure-report

- name: Deploy to GitHub Pages
  if: always()
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: allure-report
```

---

## Q46. What is page.evaluate()?

`page.evaluate()` executes JavaScript in the **browser context** and returns the result to the Node.js test.

```typescript
import { test, expect } from '@playwright/test';

test('evaluate examples', async ({ page }) => {
  await page.goto('https://example.com');

  // Get document title
  const title = await page.evaluate(() => document.title);
  console.log(title);

  // Get element property
  const buttonText = await page.evaluate(() => {
    const btn = document.querySelector('#submit-btn');
    return btn ? btn.textContent?.trim() : null;
  });

  // Pass argument to browser
  const elementCount = await page.evaluate((selector) => {
    return document.querySelectorAll(selector).length;
  }, '.product-card');

  // Manipulate DOM
  await page.evaluate(() => {
    document.body.style.backgroundColor = 'red';
  });

  // Read localStorage
  const token = await page.evaluate(() => localStorage.getItem('auth_token'));
  console.log('Token:', token);

  // Scroll to bottom
  await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
});
```

---

## Q47. What is page.exposeFunction()?

Exposes a Node.js function to the browser context — lets browser-side JavaScript call your test code.

```typescript
import { test } from '@playwright/test';

test('expose function', async ({ page }) => {
  // Expose a Node.js function to the browser
  await page.exposeFunction('log', (message: string) => {
    console.log('[Browser Log]:', message);
  });

  // Browser-side JS can now call window.log()
  await page.evaluate(() => {
    (window as any).log('Hello from browser!');
  });

  // More useful: expose a hash function to the browser
  const crypto = require('crypto');
  await page.exposeFunction('sha1', (text: string) => {
    return crypto.createHash('sha1').update(text).digest('hex');
  });

  const hash = await page.evaluate(async () => {
    return await (window as any).sha1('hello world');
  });

  console.log('Hash:', hash);
});
```

---

## Q48. How do you set localStorage using Playwright?

```typescript
import { test, expect } from '@playwright/test';

test('set localStorage before page load', async ({ page }) => {
  // Method 1: Use addInitScript — runs before any page script
  await page.addInitScript(() => {
    localStorage.setItem('auth_token', 'fake-jwt-token-123');
    localStorage.setItem('user_role', 'ADMIN');
    localStorage.setItem('theme', 'dark');
  });

  await page.goto('/dashboard');
  // Page loads with localStorage already set

  await expect(page.getByText('Admin Panel')).toBeVisible();
});

test('set localStorage after navigation', async ({ page }) => {
  await page.goto('/app');

  // Method 2: via evaluate
  await page.evaluate(() => {
    localStorage.setItem('feature_flags', JSON.stringify({
      newUI: true,
      betaFeature: false
    }));
  });

  await page.reload(); // reload to apply
  await expect(page.getByTestId('new-ui')).toBeVisible();
});

test('read localStorage', async ({ page }) => {
  await page.goto('/login');
  await performLogin(page);

  const token = await page.evaluate(() => localStorage.getItem('auth_token'));
  expect(token).toBeTruthy();
  expect(token).toMatch(/^eyJ/); // JWT starts with eyJ
});
```

---

## Q49. What are fixtures? How to create custom fixtures?

Fixtures are **dependency-injected objects** provided to tests. Built-in: `page`, `browser`, `context`, `request`. Custom fixtures extend these.

```typescript
// fixtures.ts — define custom fixtures
import { test as base, expect } from '@playwright/test';

// Types for custom fixtures
type MyFixtures = {
  loggedInPage: import('@playwright/test').Page;
  apiClient: { get: (url: string) => Promise<any> };
  testData: { userId: string; email: string };
};

export const test = base.extend<MyFixtures>({

  // Custom fixture: authenticated page
  loggedInPage: async ({ page }, use) => {
    // SETUP: login
    await page.goto('/login');
    await page.getByLabel('Email').fill('admin@test.com');
    await page.getByLabel('Password').fill('Admin123!');
    await page.getByRole('button', { name: 'Login' }).click();
    await page.waitForURL('**/dashboard');

    // PROVIDE to test
    await use(page);

    // TEARDOWN (after test)
    await page.getByRole('button', { name: 'Logout' }).click();
  },

  // Custom fixture: API client
  apiClient: async ({ request }, use) => {
    const client = {
      get: async (url: string) => {
        const resp = await request.get(url, {
          headers: { Authorization: `Bearer ${process.env.API_TOKEN}` }
        });
        return resp.json();
      }
    };
    await use(client);
  },

  // Fixture with static test data
  testData: async ({}, use) => {
    await use({
      userId: 'user-123',
      email: 'test@example.com'
    });
  },
});

export { expect };
```

**Using custom fixtures:**
```typescript
// my-test.spec.ts
import { test, expect } from './fixtures';

test('access protected page', async ({ loggedInPage }) => {
  // Already logged in!
  await expect(loggedInPage.getByRole('heading', { name: 'Dashboard' })).toBeVisible();
});

test('use API client', async ({ apiClient }) => {
  const users = await apiClient.get('/api/users');
  expect(users.length).toBeGreaterThan(0);
});
```

---

## Q50. What is the difference between beforeEach and beforeAll?

```typescript
import { test, expect } from '@playwright/test';

// beforeAll runs ONCE before all tests in the file
// (but NOT before each test)
test.beforeAll(async ({ request }) => {
  // Create test data once for all tests
  const response = await request.post('/api/users', {
    data: { name: 'Test User', email: 'test@example.com' }
  });
  // Store created user ID for tests to use
  process.env.TEST_USER_ID = (await response.json()).id;
});

// beforeEach runs BEFORE EVERY SINGLE test
test.beforeEach(async ({ page }) => {
  // Navigate to fresh page before each test
  await page.goto('/');
  await page.context().clearCookies();
});

test('test 1', async ({ page }) => {
  // beforeAll ran once, beforeEach ran
});

test('test 2', async ({ page }) => {
  // beforeAll did NOT run again, beforeEach ran again
});

test.afterAll(async ({ request }) => {
  // Cleanup test data once after all tests
  await request.delete(`/api/users/${process.env.TEST_USER_ID}`);
});
```

**Key difference:**
- `beforeAll`: once per test file/describe block — for expensive setup.
- `beforeEach`: before every test — for resetting state.

---

## Q51. How to pass environment variables to Playwright tests?

```typescript
// playwright.config.ts — define in env property
import { defineConfig } from '@playwright/test';
import dotenv from 'dotenv';

// Load .env file
dotenv.config({ path: '.env.staging' });

export default defineConfig({
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
  },
});
```

```bash
# .env.staging
BASE_URL=https://staging.example.com
API_TOKEN=secret-token-abc
USERNAME=testuser@example.com
PASSWORD=TestPass123!
```

**In tests:**
```typescript
test('use env vars', async ({ page, request }) => {
  // Access environment variables
  const baseUrl = process.env.BASE_URL;
  const token   = process.env.API_TOKEN;

  const response = await request.get('/api/users', {
    headers: { Authorization: `Bearer ${token}` }
  });
  expect(response.ok()).toBeTruthy();
});
```

**From CLI:**
```bash
BASE_URL=https://prod.example.com npx playwright test
# or
npx playwright test --project=chromium -- --BASE_URL=https://staging.example.com
```

**GitHub Actions:**
```yaml
- name: Run tests
  run: npx playwright test
  env:
    BASE_URL: ${{ secrets.STAGING_URL }}
    API_TOKEN: ${{ secrets.API_TOKEN }}
```

---

## Q52. How do you test with multiple users simultaneously?

```typescript
import { test, expect, Browser } from '@playwright/test';

test('admin and user interact simultaneously', async ({ browser }) => {
  // Create two isolated contexts
  const adminContext = await browser.newContext({
    storageState: 'playwright/.auth/admin.json'
  });
  const userContext = await browser.newContext({
    storageState: 'playwright/.auth/user.json'
  });

  const adminPage = await adminContext.newPage();
  const userPage  = await userContext.newPage();

  // Admin creates a resource
  await adminPage.goto('/admin/products/new');
  await adminPage.getByLabel('Name').fill('New Product');
  await adminPage.getByRole('button', { name: 'Publish' }).click();
  await expect(adminPage.getByText('Product published')).toBeVisible();

  // User sees the product immediately
  await userPage.goto('/products');
  await userPage.reload();
  await expect(userPage.getByText('New Product')).toBeVisible();

  await adminContext.close();
  await userContext.close();
});
```

---

## Q53. What is test.step()?

`test.step()` groups actions into named steps that appear in reports and traces.

```typescript
import { test, expect } from '@playwright/test';

test('complete checkout flow', async ({ page }) => {
  await test.step('Add item to cart', async () => {
    await page.goto('/products/laptop');
    await page.getByRole('button', { name: 'Add to Cart' }).click();
    await expect(page.getByText('Added to cart')).toBeVisible();
  });

  await test.step('Proceed to checkout', async () => {
    await page.getByRole('link', { name: 'Cart' }).click();
    await page.getByRole('button', { name: 'Checkout' }).click();
    await expect(page).toHaveURL(/\/checkout/);
  });

  await test.step('Fill shipping details', async () => {
    await page.getByLabel('Name').fill('John Doe');
    await page.getByLabel('Address').fill('123 Main St');
    await page.getByRole('button', { name: 'Continue' }).click();
  });

  await test.step('Place order', async () => {
    await page.getByRole('button', { name: 'Place Order' }).click();
    await expect(page.getByText('Order confirmed')).toBeVisible();
  });
});
```

**In reports:** Each step shows as a collapsible section with timing. Failed step highlighted with screenshot.

---

## Q54. What is test.skip()?

```typescript
// Skip unconditionally
test.skip('this test is not ready', async ({ page }) => {
  // Never runs
});

// Skip conditionally
test('skip on mobile', async ({ page, isMobile }) => {
  test.skip(isMobile, 'This feature is desktop-only');
  // Runs only on desktop
  await page.goto('/desktop-feature');
});

// Skip based on environment
test('skip in CI', async ({ page }) => {
  test.skip(!!process.env.CI, 'Skipped in CI — needs local setup');
  // Only runs locally
});

// Skip entire describe block
test.describe.skip('Feature in development', () => {
  test('test 1', async ({}) => {});
  test('test 2', async ({}) => {});
});
```

---

## Q55. What is test.fail()?

Marks a test as **expected to fail**. If the test passes, it is actually marked as FAILED (unexpected pass). If it fails, it is marked as PASSED.

```typescript
// Mark test as expected to fail (known bug)
test.fail('login button broken - BUG-123', async ({ page }) => {
  await page.goto('/login');
  await page.getByRole('button', { name: 'Login' }).click();
  // This currently throws — expected
  await expect(page).toHaveURL(/dashboard/);
});

// Conditional fail expectation
test('this might fail', async ({ page, browserName }) => {
  test.fail(browserName === 'firefox', 'Firefox has a known issue BUG-456');
  // If running Firefox → fail expected
  // If running Chrome → normal test
});
```

**Use case:** Track known bugs in test suite without removing them. When the bug is fixed, the test unexpectedly PASSES — alerting you to update `test.fail()` to normal.

---

## Q56. What is test.fixme()?

Similar to `test.skip()` but semantically means "this test is broken and needs fixing." Shown differently in reports.

```typescript
test.fixme('broken after last deployment', async ({ page }) => {
  // Won't run — marked as fixme in report
});

// Conditional fixme
test('payment flow', async ({ page }) => {
  test.fixme(true, 'Payment service is down — JIRA-789');
  // Skip with fixme annotation
});
```

**Difference:**
- `skip` — intentional, not applicable.
- `fixme` — needs to be fixed, tracked as technical debt.

---

## Q57. How do you tag tests with @smoke?

```typescript
// Add tags using test title with @ prefix (Playwright 1.42+)
test('@smoke @regression login test', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('Email').fill('user@test.com');
  await page.getByRole('button', { name: 'Login' }).click();
});

// Or using test.info().annotations (legacy)
test('login test', async ({ page }) => {
  test.info().annotations.push({ type: 'tag', description: 'smoke' });
});

// In describe blocks
test.describe('@smoke Authentication', () => {
  test('login', async ({ page }) => { /* ... */ });
  test('logout', async ({ page }) => { /* ... */ });
});
```

---

## Q58. How do you run only @smoke tests?

```bash
# Run tests with @smoke tag (Playwright 1.42+)
npx playwright test --grep @smoke
npx playwright test --grep "@smoke"

# Run smoke AND sanity
npx playwright test --grep "@smoke|@sanity"

# Exclude wip
npx playwright test --grep-invert @wip

# Combine: smoke but not wip
npx playwright test --grep @smoke --grep-invert @wip

# In package.json scripts
# "test:smoke": "playwright test --grep @smoke"
# "test:regression": "playwright test --grep @regression"
```

**playwright.config.ts — filter globally:**
```typescript
export default defineConfig({
  grep: /@smoke/,         // only run smoke tests
  grepInvert: /@wip/,    // exclude wip tests
});
```

---

## Q59. What is test.describe.configure({ mode: 'parallel' })?

Makes tests within a `describe` block run in **parallel**, even if `fullyParallel` is false globally.

```typescript
test.describe('Independent product tests', () => {
  // Override: run these tests in parallel
  test.describe.configure({ mode: 'parallel' });

  test('test product list', async ({ page }) => { /* ... */ });
  test('test product search', async ({ page }) => { /* ... */ });
  test('test product filter', async ({ page }) => { /* ... */ });
  // All 3 run simultaneously
});

test.describe('Sequential flow tests', () => {
  // Leave at default (or set serial)
  test('step 1', async ({ page }) => { /* ... */ });
  test('step 2', async ({ page }) => { /* ... */ });
  // Runs sequentially
});
```

---

## Q60. What is test.describe.configure({ mode: 'serial' })?

Forces tests in a `describe` block to run **sequentially** (one after another), sharing state if needed.

```typescript
test.describe('Order lifecycle', () => {
  test.describe.configure({ mode: 'serial' });

  let orderId: string;

  test('create order', async ({ request }) => {
    const resp = await request.post('/api/orders', {
      data: { product: 'laptop', qty: 1 }
    });
    const body = await resp.json();
    orderId = body.id;  // shared state
    expect(resp.status()).toBe(201);
  });

  test('verify order', async ({ request }) => {
    // orderId set by previous test
    const resp = await request.get(`/api/orders/${orderId}`);
    expect(resp.status()).toBe(200);
  });

  test('cancel order', async ({ request }) => {
    const resp = await request.delete(`/api/orders/${orderId}`);
    expect(resp.status()).toBe(204);
  });
});
```

**Risk:** If test 1 fails, tests 2 and 3 are skipped. Use only for true sequential flows.

---

## Q61. How do you retry failed tests?

```typescript
// playwright.config.ts — global retry
export default defineConfig({
  retries: process.env.CI ? 2 : 0, // retry twice in CI, never locally
});

// Per-test retry
test('flaky network test', async ({ page }) => {
  test.info().annotations.push({ type: 'issue', description: 'FLAKY-123' });
  // Uses global retry count
});

// Retry for specific test
test.describe('Flaky tests', () => {
  test.describe.configure({ retries: 3 });

  test('sometimes fails', async ({ page }) => {
    // Retries up to 3 times
  });
});
```

```bash
# Command line override
npx playwright test --retries=3
```

**Check retry number in test:**
```typescript
test('adaptive test', async ({ page }) => {
  if (test.info().retry > 0) {
    console.log(`Retry #${test.info().retry}`);
    // Clear state on retry
    await page.context().clearCookies();
  }
  // test body
});
```

---

## Q62. How do you set timeout per test vs global?

```typescript
// playwright.config.ts — global timeouts
export default defineConfig({
  timeout: 30_000,       // per-test timeout (default: 30s)
  globalTimeout: 600_000, // entire test suite timeout (10 min)
  expect: {
    timeout: 5_000,      // per-assertion timeout (default: 5s)
  },
});

// Per-test timeout
test('slow page test', async ({ page }) => {
  test.setTimeout(60_000); // override for this test only

  await page.goto('/slow-report');
  await expect(page.getByRole('table')).toBeVisible({ timeout: 45_000 });
});

// Per-describe timeout
test.describe('Slow integration tests', () => {
  test.describe.configure({ timeout: 120_000 });

  test('big data load', async ({ page }) => {
    // 120s timeout
  });
});

// Per-assertion timeout
await expect(locator).toBeVisible({ timeout: 15_000 });
await expect(locator).toHaveText('Result', { timeout: 10_000 });
```

---

## Q63. What is the difference between page.fill() and page.type()?

```typescript
// page.fill() — sets the value directly (fast)
// - Clears existing value first
// - Triggers input/change events
// - Does NOT trigger individual keydown/keypress/keyup events
await page.getByLabel('Email').fill('user@example.com');

// page.type() — simulates real keystrokes (slow)
// - Types character by character
// - Triggers all keyboard events (keydown, keypress, keyup, input)
// - Useful when app listens to individual key events
await page.getByLabel('Email').type('user@example.com');

// locator.pressSequentially() — modern equivalent of type()
await page.getByLabel('Email').pressSequentially('user@example.com', { delay: 50 });

// When to use type vs fill:
// - Autocomplete inputs that fire on each keystroke → type/pressSequentially
// - Regular inputs → fill (much faster)
// - OTP/PIN inputs that validate per character → pressSequentially

test('autocomplete search', async ({ page }) => {
  await page.goto('/search');

  // type triggers autocomplete dropdown
  await page.getByRole('searchbox').pressSequentially('lapt', { delay: 100 });

  // Wait for dropdown
  await expect(page.getByRole('option', { name: 'laptop' })).toBeVisible();
  await page.getByRole('option', { name: 'laptop' }).click();
});
```

---

## Q64. How do you hover in Playwright?

```typescript
test('hover interactions', async ({ page }) => {
  await page.goto('/menu');

  // Hover over menu item to reveal submenu
  await page.getByRole('menuitem', { name: 'Products' }).hover();

  // Wait for submenu to appear (auto-wait handles this)
  await expect(page.getByRole('menu', { name: 'Products submenu' })).toBeVisible();

  // Click submenu item
  await page.getByRole('menuitem', { name: 'Laptops' }).click();

  // Hover with modifiers
  await page.getByText('Element').hover({ modifiers: ['Shift'] });

  // Hover at specific position
  await page.getByTestId('canvas').hover({ position: { x: 100, y: 200 } });

  // Tooltip test
  await page.getByRole('button', { name: 'Info' }).hover();
  await expect(page.getByRole('tooltip')).toBeVisible();
  await expect(page.getByRole('tooltip')).toHaveText('Click to see details');
});
```

---

## Q65. How do you drag and drop?

```typescript
test('drag and drop', async ({ page }) => {
  await page.goto('/kanban');

  // Method 1: dragTo (recommended)
  const taskCard = page.getByText('Task: Write tests');
  const doneColumn = page.getByTestId('column-done');

  await taskCard.dragTo(doneColumn);
  await expect(doneColumn.getByText('Task: Write tests')).toBeVisible();

  // Method 2: manual drag with mouse events (for complex DnD)
  const source = page.locator('[data-id="item-1"]');
  const target = page.locator('[data-id="drop-zone"]');

  const sourceBox = await source.boundingBox();
  const targetBox = await target.boundingBox();

  await page.mouse.move(
    sourceBox!.x + sourceBox!.width / 2,
    sourceBox!.y + sourceBox!.height / 2
  );
  await page.mouse.down();
  await page.mouse.move(
    targetBox!.x + targetBox!.width / 2,
    targetBox!.y + targetBox!.height / 2,
    { steps: 20 } // smooth drag in 20 steps
  );
  await page.mouse.up();
});
```

---

## Q66. How do you select from a dropdown?

```typescript
test('dropdown selection', async ({ page }) => {
  await page.goto('/form');

  // HTML <select> element
  await page.selectOption('select#country', 'AU');              // by value
  await page.selectOption('select#country', { label: 'Australia' }); // by text
  await page.selectOption('select#country', { index: 3 });      // by index

  // Multiple select
  await page.selectOption('select#skills', ['js', 'ts', 'python']);

  // Using locator
  await page.getByLabel('Country').selectOption('Australia');

  // Custom dropdown (not <select>) — click to open, then click option
  await page.getByRole('combobox', { name: 'Country' }).click();
  await page.getByRole('option', { name: 'Australia' }).click();

  // Material UI / Ant Design dropdowns
  await page.locator('.country-select').click();
  await page.getByText('Australia').click();  // in dropdown list
});
```

---

## Q67. How do you check/uncheck checkboxes?

```typescript
test('checkbox interactions', async ({ page }) => {
  await page.goto('/settings');

  // Check a checkbox
  await page.getByLabel('Email notifications').check();
  await expect(page.getByLabel('Email notifications')).toBeChecked();

  // Uncheck
  await page.getByLabel('Email notifications').uncheck();
  await expect(page.getByLabel('Email notifications')).not.toBeChecked();

  // Toggle (click) — use when state is unknown
  await page.getByLabel('Dark mode').click();

  // Set specific state (check if unchecked, no-op if already checked)
  const checkbox = page.getByLabel('Terms and conditions');
  if (!await checkbox.isChecked()) {
    await checkbox.check();
  }

  // Using locator
  await page.locator('input[type="checkbox"][name="rememberMe"]').check();

  // Verify state
  await expect(page.getByLabel('Terms and conditions')).toBeChecked();
});
```

---

## Q68. What is the Playwright VS Code extension?

The **Playwright Test for VS Code** extension (by Microsoft) integrates Playwright into VS Code.

**Features:**
- Run tests with green play buttons next to each test in the editor.
- Debug tests with breakpoints — pauses browser at the breakpoint.
- **Pick Locator** — click element in browser, get locator code.
- **Record new test** — codegen from within VS Code.
- Test Explorer panel — tree view of all tests, run/debug individual tests.
- Inline test results — pass/fail shown in editor gutter.

**Install:**
1. Open VS Code Extensions.
2. Search "Playwright Test for VS Code".
3. Install the Microsoft extension.
4. Open Command Palette → "Test: Focus on Test Explorer".

**Usage:**
- Click the green triangle next to a `test(...)` call to run it.
- Right-click → "Debug Test" to step through with breakpoints.
- Click "Record" in Test Explorer to open codegen.
- Click "Pick Locator" to inspect elements interactively.

---

## Q69. Walk through a complete Playwright API test from scratch.

```typescript
// tests/api/users.api.spec.ts
import { test, expect } from '@playwright/test';

const BASE_URL = 'https://reqres.in/api';

test.describe('Users API — CRUD Operations', () => {

  let createdUserId: number;

  // POST — Create user
  test('POST /users creates a user', async ({ request }) => {
    const payload = {
      name: 'Kupeshanth',
      job: 'QA Engineer'
    };

    const response = await request.post(`${BASE_URL}/users`, {
      data: payload,
      headers: { 'Content-Type': 'application/json' }
    });

    // Status assertion
    expect(response.status()).toBe(201);

    const body = await response.json();

    // Body assertions
    expect(body.name).toBe('Kupeshanth');
    expect(body.job).toBe('QA Engineer');
    expect(body.id).toBeTruthy();
    expect(body.createdAt).toBeTruthy();

    // Store for later tests
    createdUserId = Number(body.id);
    console.log('Created user ID:', createdUserId);
  });

  // GET — List users
  test('GET /users returns paginated list', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/users`, {
      params: { page: 2 }
    });

    expect(response.status()).toBe(200);
    expect(response.headers()['content-type']).toContain('application/json');

    const body = await response.json();

    expect(body.page).toBe(2);
    expect(body.per_page).toBeDefined();
    expect(body.data).toBeInstanceOf(Array);
    expect(body.data.length).toBeGreaterThan(0);

    // Validate structure of first item
    const firstUser = body.data[0];
    expect(firstUser).toHaveProperty('id');
    expect(firstUser).toHaveProperty('email');
    expect(firstUser).toHaveProperty('first_name');
    expect(firstUser.email).toMatch(/^[\w.+-]+@[\w-]+\.[\w.]+$/);
  });

  // GET — Single user
  test('GET /users/2 returns specific user', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/users/2`);

    expect(response.status()).toBe(200);

    const { data } = await response.json();
    expect(data.id).toBe(2);
    expect(data.first_name).toBeTruthy();
    expect(data.email).toContain('@');
  });

  // GET — 404
  test('GET /users/999 returns 404', async ({ request }) => {
    const response = await request.get(`${BASE_URL}/users/999`);
    expect(response.status()).toBe(404);
  });

  // PUT — Update user
  test('PUT /users/2 updates user', async ({ request }) => {
    const response = await request.put(`${BASE_URL}/users/2`, {
      data: { name: 'Updated Name', job: 'Senior QA' }
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.name).toBe('Updated Name');
    expect(body.job).toBe('Senior QA');
    expect(body.updatedAt).toBeTruthy();
  });

  // DELETE — Delete user
  test('DELETE /users/2 returns 204', async ({ request }) => {
    const response = await request.delete(`${BASE_URL}/users/2`);
    expect(response.status()).toBe(204);
    expect(await response.text()).toBe('');
  });

  // Auth — POST /login
  test('POST /login returns token for valid credentials', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: {
        email: 'eve.holt@reqres.in',
        password: 'cityslicka'
      }
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.token).toBeTruthy();
    expect(typeof body.token).toBe('string');
  });

  // Auth — 400 for missing password
  test('POST /login returns 400 for missing password', async ({ request }) => {
    const response = await request.post(`${BASE_URL}/login`, {
      data: { email: 'user@test.com' }
      // password missing
    });

    expect(response.status()).toBe(400);
    const body = await response.json();
    expect(body.error).toBe('Missing password');
  });

  // Response time
  test('GET /users responds within 3 seconds', async ({ request }) => {
    const start = Date.now();
    const response = await request.get(`${BASE_URL}/users`);
    const duration = Date.now() - start;

    expect(response.status()).toBe(200);
    expect(duration).toBeLessThan(3000);
    console.log(`Response time: ${duration}ms`);
  });
});
```

---

## Q70. Walk through a complete Playwright UI test from scratch with POM.

```typescript
// pages/LoginPage.ts — Page Object Model
import { Page, Locator, expect } from '@playwright/test';

export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly loginButton: Locator;
  readonly errorMessage: Locator;
  readonly forgotPasswordLink: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput       = page.getByLabel('Email');
    this.passwordInput    = page.getByLabel('Password');
    this.loginButton      = page.getByRole('button', { name: 'Sign In' });
    this.errorMessage     = page.getByRole('alert');
    this.forgotPasswordLink = page.getByRole('link', { name: 'Forgot password?' });
  }

  async navigate() {
    await this.page.goto('/login');
    await expect(this.page).toHaveURL(/\/login/);
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }

  async getErrorMessage() {
    await expect(this.errorMessage).toBeVisible();
    return this.errorMessage.textContent();
  }
}

// pages/DashboardPage.ts
export class DashboardPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly logoutButton: Locator;
  readonly userMenu: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading      = page.getByRole('heading', { name: 'Dashboard' });
    this.logoutButton = page.getByRole('button', { name: 'Logout' });
    this.userMenu     = page.getByTestId('user-menu');
  }

  async isLoaded() {
    await expect(this.page).toHaveURL(/\/dashboard/);
    await expect(this.heading).toBeVisible();
  }

  async logout() {
    await this.userMenu.click();
    await this.logoutButton.click();
    await expect(this.page).toHaveURL(/\/login/);
  }
}

// tests/login.spec.ts — Tests using POM
import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { DashboardPage } from '../pages/DashboardPage';

test.describe('Login Feature', () => {

  let loginPage: LoginPage;
  let dashboardPage: DashboardPage;

  test.beforeEach(async ({ page }) => {
    loginPage    = new LoginPage(page);
    dashboardPage = new DashboardPage(page);
    await loginPage.navigate();
  });

  test('@smoke valid login redirects to dashboard', async ({ page }) => {
    await test.step('Login with valid credentials', async () => {
      await loginPage.login('admin@example.com', 'Admin123!');
    });

    await test.step('Verify dashboard loaded', async () => {
      await dashboardPage.isLoaded();
      await expect(page.getByText('Welcome, Admin')).toBeVisible();
    });
  });

  test('invalid password shows error', async () => {
    await loginPage.login('admin@example.com', 'wrongpassword');

    const error = await loginPage.getErrorMessage();
    expect(error).toContain('Invalid credentials');
  });

  test('empty email shows validation', async ({ page }) => {
    await loginPage.loginButton.click();

    await expect(page.getByText('Email is required')).toBeVisible();
  });

  test('invalid email format shows error', async ({ page }) => {
    await loginPage.emailInput.fill('not-an-email');
    await loginPage.loginButton.click();

    await expect(page.getByText('Enter a valid email')).toBeVisible();
  });

  test('logout returns to login page', async () => {
    // Login first
    await loginPage.login('admin@example.com', 'Admin123!');
    await dashboardPage.isLoaded();

    // Logout
    await dashboardPage.logout();

    // Verify back at login
    expect(await loginPage.page.title()).toContain('Login');
  });
});
```

**playwright.config.ts for this project:**
```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 2 : 4,
  reporter: [
    ['html'],
    ['junit', { outputFile: 'results/junit.xml' }],
  ],
  use: {
    baseURL: process.env.BASE_URL || 'https://staging.example.com',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox',  use: { ...devices['Desktop Firefox'] } },
  ],
});
```

---

*End of Part 6 — Playwright (70/70 questions)*
