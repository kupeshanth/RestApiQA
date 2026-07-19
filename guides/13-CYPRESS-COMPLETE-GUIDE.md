# Cypress — Complete Q&A Interview Guide

> Senior QA Engineer Interview Reference — Every major Cypress concept as a real interview question with full answers, code, and real-world context.

---

## SECTION: Architecture and Fundamentals

---

**Q1: What is Cypress and what makes it different from traditional web testing tools?**

**A:**
Cypress is a modern, JavaScript-first end-to-end (E2E) testing framework built specifically for web applications. Released in 2017, it runs test code directly inside the browser rather than communicating with it over HTTP, which is the core architectural difference from Selenium.

Built-in technology stack:
- **Mocha** — test runner (`describe`, `it`, `beforeEach`, hooks)
- **Chai** — assertion library (`should`, `expect`, `assert`)
- **Sinon** — spies, stubs, and mocks
- **jQuery-style querying** — familiar selector syntax

Key capabilities:
- Automatic waiting — no manual sleeps or explicit waits needed
- Native network interception — spy on, stub, or modify HTTP requests
- Time-travel debugging — click any past command to see the DOM at that moment
- Real-time test reloads — test runner updates as you save spec files
- Built-in screenshots and video recording

Real-world context: Cypress is the dominant choice for React, Vue, and Angular teams. Its interactive runner reduces time-to-debug from minutes to seconds compared to Selenium.

Common mistake: Assuming Cypress is "just a faster Selenium". The architecture is fundamentally different — Cypress is not a WebDriver-based tool at all.

---

**Q2: How is Cypress architecturally different from Selenium?**

**A:**
The core difference is WHERE the test code executes relative to the browser.

Selenium architecture:
```
Your Test Code (Java / Python / C# / JS)
        |
   HTTP JSON Wire Protocol / W3C WebDriver
        |
   Browser Driver (ChromeDriver / GeckoDriver / msedgedriver)
        |
   Browser (Chrome / Firefox / Edge)
```

Cypress architecture:
```
Node.js Process (Cypress backend)
   - Reads config, fixtures, files
   - Runs cy.task() plugins
   - Acts as network proxy for interception
        |
   Browser (Chrome / Firefox / Edge / Electron)
        |
   [Cypress code injected here — same JS event loop as the app]
   [Your test code + Application code run side by side]
```

What "runs inside the browser" delivers in practice:

| Capability | Selenium | Cypress |
|---|---|---|
| DOM access | Via HTTP round-trips | Direct JavaScript — no HTTP |
| Automatic waiting | Not built-in — you write waits | Built-in — retries every ~50ms |
| Network interception | Needs BrowserMob Proxy or similar | Native via browser's network layer |
| Time-travel debugging | No | Yes — DOM snapshot at each command |
| Real-time reloads | No | Yes — saves trigger re-run |

Real-world context: A login test in Selenium with proper explicit waits takes 5–10 seconds. The same test in Cypress typically takes 1–3 seconds because there are no HTTP round-trips per command.

Common mistake: Storing element references like `const btn = cy.get(...)` and expecting to reuse them later (like Selenium's `WebElement`). Cypress commands are asynchronous and queued — they do not return DOM elements directly.

---

**Q3: Why does Cypress not use WebDriver? What protocol does it use instead?**

**A:**
Cypress deliberately chose not to use WebDriver (the protocol Selenium, Playwright, and WebDriverIO use) because WebDriver operates over HTTP JSON Wire Protocol, which means:

1. Every command (find element, click, type) requires an HTTP request-response cycle
2. Test code and browser live in separate processes — synchronisation is the developer's problem
3. The latency per command adds up — 100 commands × 20ms HTTP overhead = 2 extra seconds minimum
4. Error messages arrive after the fact, making debugging harder

What Cypress uses instead:

Cypress injects its test runner as a script that executes directly in the browser alongside the application. For low-level browser control (launching, navigating), Cypress uses the **Chrome DevTools Protocol (CDP)** in Chrome/Edge and a custom automation bridge for Firefox.

The key insight: Cypress test code and app code share the same JavaScript execution environment (same `window`, same `document`, same event loop). This is what enables:

- `cy.get()` retrying via `document.querySelector()` in a tight loop — no HTTP
- `cy.intercept()` hooking into the browser's `fetch` and `XMLHttpRequest` natively
- `cy.window()` accessing the actual app's global `window` object
- `cy.clock()` and `cy.tick()` controlling the app's real `setTimeout` and `Date`

```javascript
// This works in Cypress because test code is IN the browser:
cy.window().then((win) => {
  // win IS the actual application window object
  expect(win.localStorage.getItem('authToken')).to.not.be.null;
});
```

Common mistake: Thinking Cypress uses CDP in the same way Playwright does. Playwright wraps CDP as its primary automation protocol. Cypress's primary advantage is the script injection approach — CDP is used for browser management, not every test command.

---

**Q4: What is automatic waiting in Cypress and how is it different from Selenium explicit waits?**

**A:**
Automatic waiting is Cypress's built-in retry mechanism. Every command that interacts with the DOM retries its action at approximately 50ms intervals until one of two things happens:
1. The condition is satisfied (success)
2. The `defaultCommandTimeout` is reached (failure with descriptive error)

How it works internally:
```
cy.get('[data-cy="submit"]').should('be.visible')

Cypress internally does:
  t=0ms    → querySelector('[data-cy="submit"]') → null → retry
  t=50ms   → querySelector('[data-cy="submit"]') → found, not visible → retry
  t=100ms  → querySelector('[data-cy="submit"]') → found, visible → PASS
```

Selenium comparison — you must write this manually:
```java
// Selenium — explicit wait (Java)
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement button = wait.until(
    ExpectedConditions.elementToBeClickable(By.cssSelector("[data-cy='submit']"))
);
button.click();
```

Cypress equivalent:
```javascript
// Cypress — automatic waiting built in, no explicit wait code needed
cy.get('[data-cy="submit"]').click(); // retries get() + waits for clickable state
```

The automatic waiting covers:
- **Element existence** — retries `cy.get()` until element is in DOM
- **Element visibility** — retries until `display != none` and `visibility != hidden`
- **Assertions** — `.should('have.text', 'Ready')` retries until the text matches
- **DOM mutations** — if React re-renders and removes the element, Cypress retries

When to use `cy.wait()`:
```javascript
// DO THIS — wait for a specific API call
cy.intercept('POST', '/api/login').as('loginRequest')
cy.get('[data-cy="submit"]').click()
cy.wait('@loginRequest') // waits for the actual network request — correct

// NEVER DO THIS — hard wait is fragile and slow
cy.wait(2000) // wrong — arbitrary sleep
```

Real-world context: Selenium teams spend significant time adding, tuning, and debugging explicit waits. Cypress teams almost never write wait code because the framework handles it. This difference alone can halve the time to write stable E2E tests.

Common mistake: Using `cy.wait(ms)` for timing issues. Almost always the correct fix is either intercepting the API call with `cy.intercept().as()` and using `cy.wait('@alias')`, or simply letting Cypress's built-in retry handle it with a `.should()` assertion.

---

## SECTION: Setup and Configuration

---

**Q5: How do you install and set up Cypress from scratch?**

**A:**

```bash
# Step 1: Initialize a node project (if not already)
npm init -y

# Step 2: Install Cypress as a dev dependency
npm install cypress --save-dev

# Step 3: Open Cypress for the first time
# This creates the folder structure and cypress.config.js
npx cypress open
```

Cypress creates the project structure automatically when first opened. After installation, add these npm scripts to `package.json`:

```json
{
  "scripts": {
    "cy:open": "cypress open",
    "cy:run": "cypress run",
    "cy:run:chrome": "cypress run --browser chrome",
    "cy:run:staging": "cypress run --env environment=staging",
    "cy:run:headed": "cypress run --headed"
  }
}
```

First test to verify setup:
```javascript
// cypress/e2e/smoke.cy.js
describe('Smoke Test', () => {
  it('loads the homepage', () => {
    cy.visit('/');
    cy.title().should('not.be.empty');
  });
});
```

Run it:
```bash
npx cypress run --spec "cypress/e2e/smoke.cy.js"
```

Real-world context: In a CI pipeline (GitHub Actions, Jenkins), you install Cypress as part of your test stage and run `npx cypress run` — no GUI needed. The `--save-dev` flag ensures Cypress is not shipped to production.

Common mistake: Running `npx cypress open` in CI pipelines. The GUI cannot render in headless CI environments. Use `npx cypress run` for CI.

---

**Q6: What is the Cypress folder structure and what does each part do?**

**A:**

```
project-root/
├── cypress/
│   ├── e2e/                        # All test spec files
│   │   ├── login.cy.js
│   │   ├── checkout.cy.js
│   │   └── api/
│   │       └── users.cy.js         # API tests (cy.request)
│   │
│   ├── fixtures/                   # Static JSON test data files
│   │   ├── user.json               # User credentials, expected responses
│   │   └── products.json           # Product catalogue for stubs
│   │
│   ├── support/
│   │   ├── commands.js             # Custom commands: cy.login(), cy.setToken()
│   │   ├── e2e.js                  # Loaded before every spec — import commands, plugins
│   │   └── pages/                  # Page Object Model classes (you create this folder)
│   │       ├── LoginPage.js
│   │       └── DashboardPage.js
│   │
│   ├── screenshots/                # Auto-captured on test failure (cypress run)
│   └── videos/                     # Auto-recorded during cypress run
│
├── cypress.config.js               # Main configuration file
├── package.json
└── node_modules/
```

File roles in detail:

| File / Folder | Purpose |
|---|---|
| `cypress/e2e/` | All `.cy.js` or `.cy.ts` test specs |
| `cypress/fixtures/` | JSON data — test inputs, mock API responses |
| `cypress/support/commands.js` | Custom commands added to `cy.*` namespace |
| `cypress/support/e2e.js` | Global setup — imports commands, plugins, global hooks |
| `cypress/support/pages/` | Page Object classes (not auto-created, you add it) |
| `cypress.config.js` | baseUrl, timeouts, retries, env vars, reporters |

Real-world context: The `support/e2e.js` file is loaded before every single spec. This is where you import custom commands, set up global `beforeEach` hooks (like resetting state), or configure third-party plugins.

Common mistake: Putting actual test assertions in `support/e2e.js`. That file is for setup only. Tests belong in `e2e/`.

---

**Q7: What is cypress.config.js and what are all the important configuration options?**

**A:**

```javascript
// cypress.config.js — complete annotated example
const { defineConfig } = require('cypress');

module.exports = defineConfig({

  // ─── E2E Testing Block ─────────────────────────────────────────────────────
  e2e: {
    // Base URL — cy.visit('/login') navigates to http://localhost:3000/login
    baseUrl: 'http://localhost:3000',

    // Glob pattern for spec discovery
    specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',

    // Support file auto-imported before every spec
    supportFile: 'cypress/support/e2e.js',

    // Fixtures folder location
    fixturesFolder: 'cypress/fixtures',

    // Node.js plugin/task setup
    setupNodeEvents(on, config) {
      // Register custom tasks (run Node.js code from tests)
      on('task', {
        log(message) {
          console.log(message);
          return null; // tasks must return something or null
        },
        seedDatabase({ tableName }) {
          // run DB seed logic here
          return null;
        }
      });

      // Dynamic config based on environment
      const env = config.env.environment || 'local';
      if (env === 'staging') {
        config.baseUrl = 'https://staging.myapp.com';
      }

      return config; // always return config
    },
  },

  // ─── Viewport ──────────────────────────────────────────────────────────────
  viewportWidth: 1280,
  viewportHeight: 720,

  // ─── Timeouts (all in milliseconds) ────────────────────────────────────────
  defaultCommandTimeout: 10000,  // cy.get(), cy.find() — how long to retry
  requestTimeout: 15000,         // cy.intercept() — how long to wait for request
  responseTimeout: 30000,        // how long to wait for server response
  pageLoadTimeout: 60000,        // cy.visit() — how long to wait for page load
  execTimeout: 60000,            // cy.exec() shell command timeout

  // ─── Test Retries ──────────────────────────────────────────────────────────
  retries: {
    runMode: 2,   // retry failing tests 2 times in CI (cypress run)
    openMode: 0,  // no retries in interactive mode (cypress open)
  },

  // ─── Screenshots and Video ─────────────────────────────────────────────────
  screenshotOnRunFailure: true,
  video: true,
  videosFolder: 'cypress/videos',
  screenshotsFolder: 'cypress/screenshots',
  trashAssetsBeforeRuns: true,   // clear old screenshots/videos before each run

  // ─── Environment Variables ─────────────────────────────────────────────────
  env: {
    apiUrl: 'https://api.myapp.com',
    adminEmail: 'admin@example.com',
    adminPassword: 'Admin@123',
    featureFlag_newCheckout: true,
  },

  // ─── Reporter ──────────────────────────────────────────────────────────────
  reporter: 'mochawesome',
  reporterOptions: {
    reportDir: 'cypress/reports',
    overwrite: false,
    html: true,
    json: true,
  },

  // ─── Experimental ──────────────────────────────────────────────────────────
  experimentalModifyObstructiveThirdPartyCode: true, // helps with cross-origin issues
  experimentalWebKitSupport: false, // Safari/WebKit — still experimental
});
```

Key configuration decisions:

- `defaultCommandTimeout` — increase to 15000 on slow CI environments to reduce flakiness
- `retries.runMode: 2` — in CI, retry twice before marking a test failed (absorbs transient network issues)
- `video: false` — disable in CI if not needed; videos consume disk and slow down runs
- `env` — never put real secrets here; use `cypress.env.json` (gitignored) or CI environment variables

Real-world context: In most real projects, the `setupNodeEvents` function is where you wire up the code coverage plugin, read environment-specific config files, or set up database seed/teardown tasks.

Common mistake: Forgetting to `return config` from `setupNodeEvents`. Without it, environment overrides you made to `config` are silently discarded.

---

## SECTION: Navigation and Querying

---

**Q8: How does cy.visit() work and what options does it support?**

**A:**
`cy.visit()` navigates the browser to a URL. When `baseUrl` is set in config, relative paths resolve against it.

```javascript
// Relative path — uses baseUrl from cypress.config.js
cy.visit('/')                     // → http://localhost:3000/
cy.visit('/login')                // → http://localhost:3000/login
cy.visit('/products?category=tv') // → http://localhost:3000/products?category=tv

// Absolute URL — ignores baseUrl
cy.visit('https://example.com')

// With options
cy.visit('/dashboard', {
  timeout: 30000,                // override pageLoadTimeout for this visit
  failOnStatusCode: false,       // don't fail if page returns 4xx/5xx
  onBeforeLoad(win) {
    // runs before page JS executes — good for stubbing window globals
    win.localStorage.setItem('authToken', 'Bearer test123');
  },
  onLoad(win) {
    // runs after page has fully loaded
    console.log('Page loaded:', win.location.href);
  }
});

// POST to a URL (uncommon but supported)
cy.visit('/submit', {
  method: 'POST',
  body: { userId: 1 }
});
```

Assertions after visit:
```javascript
cy.visit('/login');
cy.url().should('include', '/login');
cy.title().should('eq', 'Login — MyApp');
```

Real-world context: The `onBeforeLoad` option is critical when bypassing UI login — you set the auth token in localStorage before the app JavaScript even runs, so the app initialises in an authenticated state.

Common mistake: Visiting absolute URLs when `baseUrl` is set, then being surprised when cross-origin errors fire. Always prefer relative paths with `baseUrl`.

---

**Q9: How does cy.get() work? Walk through all selector strategies with examples.**

**A:**
`cy.get()` queries the DOM using a CSS selector and returns a chainable "subject" — a wrapped jQuery-like object representing matched elements.

```javascript
// By data-cy attribute (recommended — see Q15)
cy.get('[data-cy="submit-btn"]')
cy.get('[data-cy="user-card"]').first()
cy.get('[data-cy="product-card"]').eq(2) // third card (0-indexed)

// By ID
cy.get('#username')
cy.get('#login-form')

// By class
cy.get('.btn-primary')
cy.get('.nav-item.active')

// By tag
cy.get('button')
cy.get('input')

// By attribute
cy.get('input[type="email"]')
cy.get('input[type="password"]')
cy.get('a[href="/dashboard"]')
cy.get('[placeholder="Search..."]')

// Compound / CSS selectors
cy.get('form > input')                    // direct child
cy.get('table tbody tr')                  // all table rows in tbody
cy.get('table tbody tr').eq(0).find('td').eq(2) // 3rd cell of first row

// With options
cy.get('[data-cy="element"]', { timeout: 20000 })  // override timeout for this query
cy.get('[data-cy="element"]', { log: false })       // suppress from command log

// Chaining — the subject flows through the chain
cy.get('[data-cy="email"]')           // subject = input element
  .clear()                            // subject = input element
  .type('user@example.com')          // subject = input element
  .should('have.value', 'user@example.com') // assertion on input value
```

Traversal commands that follow `cy.get()`:

```javascript
cy.get('[data-cy="list"]').children()         // direct children
cy.get('[data-cy="item"]').parent()           // parent element
cy.get('[data-cy="item"]').siblings()         // sibling elements
cy.get('[data-cy="item"]').next()             // next sibling
cy.get('[data-cy="item"]').prev()             // previous sibling
cy.get('[data-cy="item"]').closest('.card')   // nearest ancestor matching selector
cy.get('[data-cy="item"]').first()            // first matched element
cy.get('[data-cy="item"]').last()             // last matched element
cy.get('[data-cy="items"]').filter(':visible') // only visible elements
cy.get('[data-cy="items"]').not('.disabled')  // exclude matching
```

Common mistake: Expecting `cy.get()` to return a DOM element synchronously like `document.querySelector()`. It returns a Cypress chainable object, not the element. Use `.then()` to extract values:

```javascript
// WRONG — undefined, not a DOM element
const el = cy.get('[data-cy="price"]');
console.log(el.text()); // does not work

// CORRECT — extract via .then()
cy.get('[data-cy="price"]').invoke('text').then((text) => {
  const price = parseFloat(text.replace('$', ''));
  expect(price).to.be.greaterThan(0);
});
```

---

**Q10: What is cy.contains() and how does it differ from cy.get()?**

**A:**
`cy.contains()` finds an element by its visible text content. `cy.get()` finds elements by CSS selector. They serve different purposes.

```javascript
// cy.contains() — find by text
cy.contains('Login')                     // finds first element containing "Login" text
cy.contains('Sign In')                   // partial text match
cy.contains('button', 'Submit')          // restricts to <button> elements with "Submit"
cy.contains('.nav-item', 'Dashboard')    // restricts to .nav-item elements
cy.contains(/^\d+ items$/)              // regex match — e.g., "5 items", "12 items"

// cy.get() — find by CSS selector
cy.get('[data-cy="submit-btn"]')
cy.get('.nav-item')
```

Key difference — chaining:

```javascript
// cy.contains() is great for links and buttons where text is the identifier
cy.contains('a', 'View Order Details').click();
cy.contains('button', 'Add to Cart').first().click();

// cy.get() is better when you have a stable test attribute
cy.get('[data-cy="add-to-cart"]').first().click();
```

Scoped contains — use within a parent:
```javascript
cy.get('[data-cy="product-card"]').first().contains('Add to Cart').click();
// Clicks "Add to Cart" inside the first product card only
```

Real-world context: `cy.contains()` is useful when working with dynamically generated content where the text is stable but you cannot add `data-cy` attributes (e.g., third-party components). For your own application, prefer `data-cy` attributes with `cy.get()`.

Common mistake: Using `cy.contains('Submit')` when there are multiple elements with "Submit" on the page. Scope it to a specific container:
```javascript
cy.get('[data-cy="checkout-form"]').contains('Submit').click();
```

---

**Q11: What is cy.find() and when do you use it instead of cy.get()?**

**A:**
`cy.find()` searches within the current subject — it is a scoped search. `cy.get()` always searches from the root of the document.

```javascript
// cy.find() — search within a parent element (scoped)
cy.get('.user-card')
  .find('button')            // finds buttons INSIDE .user-card only

cy.get('[data-cy="product-list"]')
  .find('[data-cy="product-card"]')
  .should('have.length', 6)

cy.get('form[name="billing"]')
  .find('input[type="email"]')
  .type('billing@example.com')

// cy.get() — always searches the whole document
cy.get('[data-cy="submit"]')  // finds ANY submit button on page
```

When to use each:

| Use `cy.get()` | Use `cy.find()` |
|---|---|
| You know the unique selector for the element | You want to find elements INSIDE a specific parent |
| Starting a fresh query | Chaining after a parent was already found |
| Selecting from the document root | Scoping your search to avoid ambiguous matches |

```javascript
// Real scenario: testing a data table
cy.get('table tbody tr').each(($row, index) => {
  cy.wrap($row).find('td').eq(0).should('not.be.empty'); // first cell not empty
  cy.wrap($row).find('[data-cy="edit-btn"]').should('be.visible');
});
```

Common mistake: Using `cy.get()` inside a `.then()` block and expecting it to search within that element. You must use `cy.find()` or `cy.wrap($el).find()` for scoped searches.

---

**Q12: What is cy.within() and when do you use it?**

**A:**
`cy.within()` temporarily scopes all subsequent Cypress commands to execute within a specific DOM element. Every `cy.get()` inside the `.within()` block is equivalent to calling `.find()` on that parent.

```javascript
// Without cy.within() — repetitive parent selector
cy.get('[data-cy="checkout-form"]').find('[data-cy="name"]').type('John');
cy.get('[data-cy="checkout-form"]').find('[data-cy="email"]').type('john@example.com');
cy.get('[data-cy="checkout-form"]').find('[data-cy="submit"]').click();

// With cy.within() — cleaner
cy.get('[data-cy="checkout-form"]').within(() => {
  cy.get('[data-cy="name"]').type('John');         // scoped to form
  cy.get('[data-cy="email"]').type('john@example.com');
  cy.get('[data-cy="submit"]').click();
});

// Practical: testing a specific table row
cy.get('table tbody tr').eq(3).within(() => {
  cy.get('td').eq(0).should('have.text', 'Order #1234');
  cy.get('[data-cy="status-badge"]').should('have.text', 'Delivered');
  cy.get('[data-cy="action-btn"]').click();
});

// Practical: modal dialog
cy.get('[data-cy="confirm-modal"]').within(() => {
  cy.get('[data-cy="modal-title"]').should('have.text', 'Confirm Delete');
  cy.get('[data-cy="confirm-btn"]').click();
});
```

Real-world context: `cy.within()` is essential when your page has multiple similar sections (multiple forms, multiple cards, multiple table rows). Without it, `cy.get('[data-cy="submit"]')` might match multiple elements across the page.

Common mistake: Trying to use `cy.intercept()` inside a `.within()` block. Network interception is not DOM-scoped — it should be set up before the action, not inside `.within()`.

---

**Q13: What is the best locator strategy in Cypress and why is data-cy recommended?**

**A:**
Cypress's official recommendation is to use `data-cy` (or `data-testid`) attributes because they are decoupled from both CSS styling and application logic.

Selector priority (best to worst):

| Priority | Strategy | Example | Why |
|---|---|---|---|
| 1 (Best) | `data-cy` attribute | `[data-cy="login-btn"]` | Test-only, never changes with refactors |
| 2 | `data-testid` | `[data-testid="submit"]` | Also test-only, same benefit |
| 3 | ARIA role | `[role="button"]` | Accessibility-aligned, stable |
| 4 | ID | `#username` | Stable but can be dynamic |
| 5 | Name attribute | `[name="email"]` | Good for form inputs |
| 6 | CSS class | `.btn-primary` | Fragile — styling changes break tests |
| 7 (Worst) | Text content | `cy.contains('Click Here')` | Breaks on copy changes |
| 8 (Worst) | XPath | — | Verbose, brittle, not native |

Adding `data-cy` to your HTML:
```html
<!-- Application code -->
<button data-cy="submit-btn" class="btn btn-primary">Submit</button>
<input data-cy="email-input" type="email" class="form-control" />
<div data-cy="user-card" class="card shadow-md">...</div>
<table data-cy="orders-table">...</table>
```

Using them in tests:
```javascript
cy.get('[data-cy="submit-btn"]').click();
cy.get('[data-cy="email-input"]').type('user@example.com');
cy.get('[data-cy="user-card"]').should('have.length', 3);
cy.get('[data-cy="orders-table"]').find('tr').should('have.length.greaterThan', 0);
```

Why this matters in practice:
```javascript
// Scenario: designer changes button class from "btn-primary" to "btn-cta"
// Bad selector — test BREAKS
cy.get('.btn-primary').click();

// Good selector — test SURVIVES the styling change
cy.get('[data-cy="submit-btn"]').click();
```

Stripping data-cy from production builds (optional):

If you do not want `data-cy` attributes in your production bundle, use a Babel plugin:
```bash
npm install --save-dev babel-plugin-react-remove-properties
```
```json
// babel.config.json — only in production
{ "plugins": [["react-remove-properties", { "properties": ["data-cy"] }]] }
```

Common mistake: Using CSS classes for test selectors because "they look cleaner". A designer can rename a class at any time and break hundreds of tests.

---

## SECTION: Actions

---

**Q14: How does cy.type() work? What special keys can you send?**

**A:**
`cy.type()` simulates keyboard input into a focused element. It triggers native browser keyboard events (`keydown`, `keypress`, `keyup`, `input`, `change`).

```javascript
// Basic typing
cy.get('[data-cy="email"]').type('user@example.com');
cy.get('[data-cy="password"]').type('SecretPass@123');

// Clear before typing (best practice for inputs that may have pre-filled values)
cy.get('[data-cy="email"]').clear().type('newuser@example.com');

// Typing with options
cy.get('[data-cy="search"]').type('cypress testing', { delay: 100 }); // type 100ms between chars
cy.get('[data-cy="input"]').type('hello', { force: true });  // force even if covered

// Special key sequences (use curly-brace notation)
cy.get('[data-cy="search"]').type('query{enter}');      // type then press Enter
cy.get('[data-cy="input"]').type('{selectAll}');         // Ctrl+A / Cmd+A
cy.get('[data-cy="input"]').type('{selectAll}{del}');    // select all and delete
cy.get('[data-cy="input"]').type('{ctrl}a');             // Ctrl+A
cy.get('[data-cy="input"]').type('{ctrl}c');             // Ctrl+C
cy.get('[data-cy="input"]').type('{esc}');               // Escape key
cy.get('[data-cy="input"]').type('{tab}');               // Tab
cy.get('[data-cy="input"]').type('{backspace}');         // Backspace
cy.get('[data-cy="input"]').type('{del}');               // Delete key
cy.get('[data-cy="input"]').type('{uparrow}');           // Up arrow
cy.get('[data-cy="input"]').type('{downarrow}');         // Down arrow
cy.get('[data-cy="input"]').type('{leftarrow}');         // Left arrow
cy.get('[data-cy="input"]').type('{rightarrow}');        // Right arrow
cy.get('[data-cy="input"]').type('{home}');              // Home key
cy.get('[data-cy="input"]').type('{end}');               // End key
cy.get('[data-cy="input"]').type('{pageup}');            // Page Up
cy.get('[data-cy="input"]').type('{pagedown}');          // Page Down

// Modifier keys held during type
cy.get('[data-cy="input"]').type('{shift}hello');        // HELLO (uppercase)
cy.get('[data-cy="input"]').type('{ctrl}{shift}i');      // Ctrl+Shift+I
```

Typing in number inputs:
```javascript
// number inputs can be tricky
cy.get('input[type="number"]').type('42');
cy.get('input[type="number"]').invoke('val', 42).trigger('input'); // alternative
```

Common mistake: Calling `.type()` on an element without first clearing it when it has a placeholder or previous value. Always `.clear().type()` for inputs that persist values.

---

**Q15: How does cy.click() work and what options does it have?**

**A:**
`cy.click()` simulates a mouse click on an element. It fires `mousedown`, `focus`, `mouseup`, and `click` events.

```javascript
// Basic click
cy.get('[data-cy="submit"]').click();
cy.get('[data-cy="nav-link"]').click();

// Click options
cy.get('[data-cy="submit"]').click({ force: true });       // ignore actionability checks
cy.get('[data-cy="submit"]').click({ timeout: 20000 });    // wait longer for element
cy.get('[data-cy="submit"]').click({ multiple: true });    // click all matched elements

// Click position (within the element)
cy.get('[data-cy="canvas"]').click('topLeft');    // top-left corner
cy.get('[data-cy="canvas"]').click('top');
cy.get('[data-cy="canvas"]').click('topRight');
cy.get('[data-cy="canvas"]').click('left');
cy.get('[data-cy="canvas"]').click('center');     // default
cy.get('[data-cy="canvas"]').click('right');
cy.get('[data-cy="canvas"]').click('bottomLeft');
cy.get('[data-cy="canvas"]').click('bottom');
cy.get('[data-cy="canvas"]').click('bottomRight');
cy.get('[data-cy="canvas"]').click(150, 75);      // click at coordinates x=150, y=75

// Double click
cy.get('[data-cy="file"]').dblclick();

// Right click (opens context menu)
cy.get('[data-cy="item"]').rightclick();

// Click with modifier key
cy.get('[data-cy="checkbox"]').click({ shiftKey: true });  // shift+click
cy.get('[data-cy="link"]').click({ ctrlKey: true });       // ctrl+click (open in new tab sim)
cy.get('[data-cy="link"]').click({ metaKey: true });       // cmd+click on Mac
```

Actionability checks: Before clicking, Cypress verifies:
1. Element is not disabled
2. Element is not hidden (display, visibility, opacity)
3. Element is not covered by another element (pointer-events)
4. Element is within the viewport

```javascript
// If element is behind an animation or overlay, use force with caution
cy.get('[data-cy="btn"]').click({ force: true });
// WARNING: force bypasses ALL actionability checks — use only when intentional
```

Real-world context: `{ force: true }` is commonly needed for custom dropdowns or elements behind animation overlays. Document why you are using it in a comment so future maintainers understand the intent.

---

**Q16: How do you handle checkboxes, radio buttons, and dropdowns in Cypress?**

**A:**

Checkboxes:
```javascript
// Check a checkbox
cy.get('[data-cy="terms-checkbox"]').check();
cy.get('[data-cy="terms-checkbox"]').should('be.checked');

// Uncheck a checkbox
cy.get('[data-cy="newsletter"]').uncheck();
cy.get('[data-cy="newsletter"]').should('not.be.checked');

// Check/uncheck multiple checkboxes by value
cy.get('[data-cy="options"]').check(['option-a', 'option-b']);

// Check by value attribute
cy.get('input[type="checkbox"]').check('terms');
```

Radio buttons:
```javascript
// Check a radio by value
cy.get('input[type="radio"]').check('male');
cy.get('[name="plan"]').check('premium');
cy.get('[name="plan"]').should('be.checked');
```

Dropdowns (native `<select>`):
```javascript
// Select by visible text
cy.get('[data-cy="country"]').select('United States');

// Select by option value attribute
cy.get('[data-cy="country"]').select('US');

// Select by option index (0-based)
cy.get('[data-cy="country"]').select(2);

// Select multiple options (multi-select)
cy.get('[data-cy="tags"]').select(['JavaScript', 'TypeScript']);

// Verify selected value
cy.get('[data-cy="country"]').should('have.value', 'US');

// Verify option text
cy.get('[data-cy="plan"]').find('option:selected').should('have.text', 'Premium');
```

Custom dropdowns (not `<select>` — React Select, Material UI, etc.):
```javascript
// Custom dropdown requires clicking to open, then selecting from list
cy.get('[data-cy="custom-dropdown"]').click();               // open dropdown
cy.get('[data-cy="dropdown-option"]').contains('Europe').click(); // pick option
cy.get('[data-cy="custom-dropdown"]').should('contain', 'Europe'); // verify
```

Real-world context: Many modern applications use custom dropdown components rather than native `<select>`. These require a two-step interaction: open the dropdown, then click the desired option. Native `<select>` elements are handled by `cy.select()` in a single step.

---

**Q17: How do cy.scrollTo() and cy.scrollIntoView() work?**

**A:**
These handle scrolling — essential for lazy-loaded content, sticky headers, and long pages.

```javascript
// Scroll the entire page
cy.scrollTo('top');                      // scroll to very top
cy.scrollTo('bottom');                   // scroll to very bottom
cy.scrollTo('center');                   // center of page
cy.scrollTo(0, 500);                     // x=0, y=500 pixels
cy.scrollTo('50%', '70%');              // percentage of page

// Scroll with options
cy.scrollTo('bottom', { duration: 500 });             // animate over 500ms
cy.scrollTo('bottom', { easing: 'swing' });           // easing function
cy.scrollTo(0, 1000, { ensureScrollable: false });    // don't fail if not scrollable

// Scroll a specific element (not the window)
cy.get('[data-cy="long-list"]').scrollTo('bottom');    // scroll within element
cy.get('[data-cy="scrollable-div"]').scrollTo(0, 300);

// Scroll element into the visible viewport
cy.get('[data-cy="lazy-image"]').scrollIntoView();
cy.get('[data-cy="lazy-image"]').scrollIntoView().should('be.visible');

// scrollIntoView with options
cy.get('[data-cy="footer-btn"]').scrollIntoView({ duration: 300 });
cy.get('[data-cy="element"]').scrollIntoView({ block: 'center' }); // align center
cy.get('[data-cy="element"]').scrollIntoView({ offset: { top: -100, left: 0 } });
```

Real-world context: Lazy loading is common in modern apps. Images, infinite scroll lists, and off-screen components only load when scrolled into view. `scrollIntoView()` triggers the load, then you can assert the content appeared.

```javascript
// Testing infinite scroll
cy.get('[data-cy="post-list"]').scrollTo('bottom');
cy.get('[data-cy="loading-spinner"]').should('not.exist');
cy.get('[data-cy="post-item"]').should('have.length.greaterThan', 10);
```

---

**Q18: What is cy.trigger() and when do you need it?**

**A:**
`cy.trigger()` fires any DOM event on an element. You use it when native Cypress action commands (`.click()`, `.type()`) cannot replicate the interaction — typically for custom JavaScript event handlers, canvas elements, drag interactions, or hover menus.

```javascript
// Hover — trigger mouseover on an element with a CSS hover state
cy.get('[data-cy="hover-card"]').trigger('mouseover');
cy.get('[data-cy="hover-menu"]').should('be.visible');

// Mouse events for drag-and-drop simulation
cy.get('[data-cy="slider"]').trigger('mousedown', { button: 0 });
cy.get('[data-cy="slider"]').trigger('mousemove', { clientX: 350 });
cy.get('[data-cy="slider"]').trigger('mouseup');

// Keyboard events
cy.get('[data-cy="input"]').trigger('keydown', { key: 'Escape', keyCode: 27 });
cy.get('[data-cy="input"]').trigger('keydown', { key: 'Enter', keyCode: 13 });

// Touch events (mobile simulation)
cy.get('[data-cy="swipe-area"]').trigger('touchstart', { touches: [{ clientX: 100 }] });
cy.get('[data-cy="swipe-area"]').trigger('touchend');

// Form events
cy.get('[data-cy="file-input"]').trigger('change');
cy.get('[data-cy="input"]').trigger('input');     // fires when value changes
cy.get('[data-cy="input"]').trigger('blur');      // fires when element loses focus
cy.get('[data-cy="input"]').trigger('focus');     // fires when element gains focus

// Wheel event (scroll within element via scroll wheel)
cy.get('[data-cy="chart"]').trigger('wheel', { deltaY: 100, deltaMode: 0 });
```

Real-world context: Drag-and-drop testing often requires `trigger()` for `mousedown` + `mousemove` + `mouseup` sequences. For complex drag-and-drop, the `@4tw/cypress-drag-drop` plugin provides `cy.drag()` which handles this more reliably.

Common mistake: Using `trigger('click')` instead of `.click()`. The `.click()` command is always preferred because it includes Cypress's actionability checks. `trigger()` bypasses them.

---

## SECTION: Assertions

---

**Q19: What are the main assertion strategies in Cypress and how do they work?**

**A:**
Cypress has three assertion styles, all based on the Chai library:

1. **`.should()` — chainable, attached to a Cypress command** (most common)
2. **`expect()` — BDD style, used inside `.then()` blocks**
3. **`assert` — TDD style (rare in Cypress)**

The key difference: `.should()` is retried automatically. `expect()` is evaluated once.

```javascript
// .should() — retried until passes or timeout
cy.get('[data-cy="status"]').should('have.text', 'Active');
// Cypress retries this every 50ms until the text is "Active" or timeout

// expect() — evaluated once when .then() executes
cy.request('/api/user/1').then((response) => {
  expect(response.status).to.equal(200);         // evaluated once
  expect(response.body.name).to.equal('Alice');
});
```

---

**Q20: What are all the key .should() assertions with examples?**

**A:**

```javascript
// ─── Visibility ────────────────────────────────────────────────────────────
cy.get('[data-cy="modal"]').should('be.visible');
cy.get('[data-cy="modal"]').should('not.be.visible');
cy.get('[data-cy="spinner"]').should('not.exist');       // not in DOM at all
cy.get('[data-cy="error-msg"]').should('exist');         // in DOM (may not be visible)
cy.get('[data-cy="dropdown"]').should('be.hidden');      // in DOM but hidden

// ─── Text Content ───────────────────────────────────────────────────────────
cy.get('[data-cy="page-title"]').should('have.text', 'Welcome to MyApp');  // exact
cy.get('[data-cy="message"]').should('contain', 'success');                 // partial
cy.get('[data-cy="error"]').should('include.text', 'required');             // alias
cy.get('[data-cy="title"]').should('not.contain', 'Error');

// ─── Form Inputs ────────────────────────────────────────────────────────────
cy.get('[data-cy="email"]').should('have.value', 'user@example.com');
cy.get('[data-cy="submit"]').should('be.disabled');
cy.get('[data-cy="submit"]').should('not.be.disabled');
cy.get('[data-cy="submit"]').should('be.enabled');
cy.get('[data-cy="terms"]').should('be.checked');
cy.get('[data-cy="terms"]').should('not.be.checked');
cy.get('[data-cy="input"]').should('have.focus');
cy.get('[data-cy="readonly-input"]').should('have.attr', 'readonly');

// ─── HTML Attributes ────────────────────────────────────────────────────────
cy.get('[data-cy="link"]').should('have.attr', 'href', '/dashboard');
cy.get('[data-cy="img"]').should('have.attr', 'src').and('include', 'logo');
cy.get('[data-cy="btn"]').should('have.attr', 'data-cy', 'submit-btn');
cy.get('[data-cy="btn"]').should('not.have.attr', 'disabled');

// ─── CSS Classes ────────────────────────────────────────────────────────────
cy.get('[data-cy="tab"]').should('have.class', 'active');
cy.get('[data-cy="tab"]').should('not.have.class', 'disabled');

// ─── CSS Properties ─────────────────────────────────────────────────────────
cy.get('[data-cy="error-text"]').should('have.css', 'color', 'rgb(220, 53, 69)');
cy.get('[data-cy="box"]').should('have.css', 'display', 'flex');

// ─── Collections ────────────────────────────────────────────────────────────
cy.get('[data-cy="product-card"]').should('have.length', 6);
cy.get('[data-cy="product-card"]').should('have.length.greaterThan', 0);
cy.get('[data-cy="product-card"]').should('have.length.at.least', 3);
cy.get('[data-cy="product-card"]').should('have.length.lessThan', 20);

// ─── URL and Browser State ──────────────────────────────────────────────────
cy.url().should('include', '/dashboard');
cy.url().should('eq', 'http://localhost:3000/login');
cy.url().should('match', /\/orders\/\d+/);        // regex on URL
cy.title().should('eq', 'My Application — Dashboard');
cy.location('pathname').should('eq', '/dashboard');
cy.location('search').should('include', 'page=2');
```

---

**Q21: How does .and() work for chaining multiple assertions?**

**A:**
`.and()` is an alias for `.should()` that reads more naturally when chaining multiple assertions on the same subject. Both are identical in behavior.

```javascript
// Multiple assertions on same element — use .and() for readability
cy.get('[data-cy="username-input"]')
  .should('be.visible')
  .and('not.be.disabled')
  .and('have.value', 'admin')
  .and('have.attr', 'placeholder', 'Enter username');

cy.get('[data-cy="submit-btn"]')
  .should('be.visible')
  .and('not.be.disabled')
  .and('have.class', 'btn-primary');

cy.get('[data-cy="profile-image"]')
  .should('be.visible')
  .and('have.attr', 'src')
  .and('include', 'avatar');       // chain on the attribute value itself

// All three are equivalent:
cy.get('el').should('be.visible').should('have.text', 'Hello');
cy.get('el').should('be.visible').and('have.text', 'Hello');
cy.get('el').should(($el) => {
  expect($el).to.be.visible;
  expect($el).to.have.text('Hello');
});
```

Real-world context: When multiple assertions fail, `.and()` chaining means Cypress reports the first failing assertion clearly. This is cleaner than multiple separate `.should()` calls.

---

**Q22: What is the difference between expect() and .should() in Cypress?**

**A:**

| Aspect | `.should()` | `expect()` |
|---|---|---|
| Retry behavior | Yes — retries automatically | No — evaluated once |
| Usage | Chained on Cypress commands | Inside `.then()` callbacks |
| Subject | The current Cypress subject (DOM element, response) | Any JavaScript value |
| Failure timing | Fails after timeout if condition never met | Fails immediately if assertion fails |

```javascript
// .should() — retried automatically, correct for DOM assertions
cy.get('[data-cy="total-price"]').should('have.text', '$99.00');
// Cypress retries every 50ms until "total-price" shows $99.00

// expect() — evaluated once, correct for extracted values
cy.request('/api/cart').then((response) => {
  expect(response.status).to.equal(200);
  expect(response.body.items).to.have.length(3);
  expect(response.body.total).to.be.closeTo(99.00, 0.01);
});

// expect() inside should callback — gives you full Chai API
cy.get('[data-cy="price-list"]').should(($list) => {
  const prices = $list.find('.price').toArray().map(el => parseFloat(el.innerText));
  expect(prices).to.have.length.greaterThan(0);
  prices.forEach(price => expect(price).to.be.greaterThan(0));
});
```

When to use each:
- **`.should()`** — asserting on DOM elements, API responses via `cy.intercept()`, anything that may take time to appear
- **`expect()`** — asserting inside `.then()` after extracting a value, complex multi-step assertions, JavaScript value comparisons

Common mistake: Using `expect()` outside a `.then()` block and expecting it to retry. `expect()` does not retry — only `.should()` does.

---

## SECTION: Network Interception

---

**Q23: What is cy.intercept() and how do you spy on API calls?**

**A:**
`cy.intercept()` intercepts HTTP requests made by the application. It can observe (spy), modify, stub, or block requests. It must be called before the action that triggers the request.

Spying — observe without changing behavior:
```javascript
// Spy on GET /api/users — intercept but let real request through
cy.intercept('GET', '/api/users').as('getUsers');

cy.visit('/users-page');                              // page makes GET /api/users

cy.wait('@getUsers');                                 // wait for request to complete
cy.wait('@getUsers').its('response.statusCode').should('eq', 200);

// Assert on request details
cy.wait('@getUsers').then(({ request, response }) => {
  expect(request.headers).to.have.property('authorization');
  expect(response.statusCode).to.equal(200);
  expect(response.body).to.be.an('array');
  expect(response.body).to.have.length.greaterThan(0);
});

// Wildcard and regex patterns
cy.intercept('GET', '/api/users/*').as('getUser');        // wildcard
cy.intercept('GET', /\/api\/users\/\d+/).as('getUser');   // regex
cy.intercept({ method: 'POST', url: '/api/**' }).as('anyPost');

// Match on headers
cy.intercept({
  method: 'GET',
  url: '/api/secure',
  headers: { 'x-api-version': '2' }
}).as('secureRequest');
```

Real-world context: Spying is the correct pattern for verifying your app sends the right request. For example, confirming that clicking "Save" sends a PUT request to the correct endpoint with the correct body.

Common mistake: Setting up `cy.intercept()` AFTER the action that triggers the request. The intercept must be registered before the network request fires.

---

**Q24: How do you stub API responses with cy.intercept()?**

**A:**
Stubbing returns a fixed response instead of the real API response. No actual network request reaches the server.

```javascript
// Basic stub — return fixed body
cy.intercept('GET', '/api/products', {
  statusCode: 200,
  body: [
    { id: 1, name: 'Laptop', price: 999 },
    { id: 2, name: 'Mouse', price: 29 },
  ]
}).as('mockProducts');

cy.visit('/products');
cy.wait('@mockProducts');
cy.get('[data-cy="product-card"]').should('have.length', 2);

// Stub from fixture file
cy.intercept('GET', '/api/products', { fixture: 'products.json' }).as('getProducts');

// Stub with dynamic response using callback
cy.intercept('POST', '/api/orders', (req) => {
  req.reply({
    statusCode: 201,
    body: {
      orderId: 'ORD-' + Date.now(),
      status: 'confirmed',
      email: req.body.email     // echo back request data
    }
  });
}).as('createOrder');

// Simulate error responses
cy.intercept('GET', '/api/data', { statusCode: 500, body: 'Internal Server Error' });
cy.intercept('GET', '/api/data', { statusCode: 401, body: { error: 'Unauthorized' } });
cy.intercept('GET', '/api/data', { statusCode: 404, body: { error: 'Not Found' } });

// Simulate network failure (no response at all)
cy.intercept('GET', '/api/data', { forceNetworkError: true });

// Simulate slow response
cy.intercept('GET', '/api/slow', (req) => {
  req.reply((res) => {
    res.delay(3000);     // delay 3 seconds before responding
    res.send({ data: 'finally' });
  });
});
```

Real-world context: Stubbing is critical for testing error states. You cannot easily make a real API return a 500 error on demand, but with `cy.intercept()` you can test exactly how your UI handles service failures.

---

**Q25: How does cy.wait('@alias') work and when should you use it?**

**A:**
`cy.wait('@alias')` pauses test execution until the intercepted request that matches the alias completes (both request sent and response received). This is the correct way to wait for API calls.

```javascript
// Full workflow: intercept → trigger → wait → assert
cy.intercept('POST', '/api/login').as('loginRequest');

cy.get('[data-cy="email"]').type('admin@example.com');
cy.get('[data-cy="password"]').type('Admin@123');
cy.get('[data-cy="submit"]').click();

cy.wait('@loginRequest');                    // waits for POST /api/login to complete
cy.url().should('include', '/dashboard');   // now it's safe to assert

// Assert on the actual request and response
cy.wait('@loginRequest').then(({ request, response }) => {
  expect(request.body.email).to.equal('admin@example.com');
  expect(response.statusCode).to.equal(200);
  expect(response.body).to.have.property('token');
});

// Wait with custom timeout
cy.intercept('GET', '/api/report').as('heavyReport');
cy.get('[data-cy="generate-btn"]').click();
cy.wait('@heavyReport', { timeout: 60000 }); // report can take up to 60s

// Wait for multiple requests (sequential)
cy.intercept('GET', '/api/users').as('getUsers');
cy.intercept('GET', '/api/roles').as('getRoles');
cy.visit('/admin');
cy.wait(['@getUsers', '@getRoles']); // wait for both to complete

// Wait for the same alias multiple times (e.g., pagination)
cy.intercept('GET', '/api/items*').as('getItems');
cy.get('[data-cy="next-page"]').click();
cy.wait('@getItems');                        // first intercept
cy.get('[data-cy="next-page"]').click();
cy.wait('@getItems');                        // second intercept
```

Real-world context: Without `cy.wait('@alias')`, tests assert on UI state before the API response arrives. This is a leading cause of flaky tests. The pattern intercept → action → wait → assert is the most reliable sequence.

Common mistake: Using `cy.wait(2000)` to "give the API time to respond". This is fragile. If the API responds in 3 seconds, the test fails. If the API responds in 0.1 seconds, you wasted 1.9 seconds. `cy.wait('@alias')` is always correct.

---

**Q26: How does cy.request() differ from cy.intercept() and when do you use it?**

**A:**

| Aspect | `cy.request()` | `cy.intercept()` |
|---|---|---|
| What it does | Cypress itself makes an HTTP request | Intercepts requests made by the APPLICATION |
| Use case | API testing, test setup, bypassing UI | Spy/stub app's network calls |
| Response available | Immediately via `.then()` | After `cy.wait('@alias')` |
| Triggers app code | No — Cypress calls directly | No — but observes/stubs what app calls |

```javascript
// cy.request() — Cypress MAKES the API call directly
cy.request({
  method: 'POST',
  url: '/api/users',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${Cypress.env('authToken')}`
  },
  body: {
    name: 'Test User',
    email: 'testuser@example.com',
    role: 'viewer'
  }
}).then((response) => {
  expect(response.status).to.equal(201);
  expect(response.body).to.have.property('id');
  expect(response.body.email).to.equal('testuser@example.com');
});

// cy.intercept() — observes/stubs what the APP calls
cy.intercept('GET', '/api/users', { fixture: 'users.json' });
cy.visit('/users');  // app makes GET /api/users; Cypress intercepts it
```

Complete cy.request() examples:
```javascript
// GET request — short form
cy.request('/api/health').its('status').should('eq', 200);

// GET with query params
cy.request('GET', '/api/users?page=1&limit=10').then((res) => {
  expect(res.status).to.eq(200);
  expect(res.body.data).to.have.length(10);
});

// PUT request
cy.request({
  method: 'PUT',
  url: '/api/users/42',
  body: { name: 'Updated Name' }
}).then((res) => {
  expect(res.status).to.eq(200);
  expect(res.body.name).to.eq('Updated Name');
});

// DELETE request
cy.request({ method: 'DELETE', url: '/api/users/42' })
  .its('status').should('eq', 204);

// Failing requests — don't throw on non-2xx
cy.request({ method: 'GET', url: '/api/missing', failOnStatusCode: false })
  .its('status').should('eq', 404);
```

Real-world context: `cy.request()` is perfect for test setup and teardown — creating test users via API before a test, then deleting them after. This is faster than doing it through the UI.

---

## SECTION: Test Data and Organization

---

**Q27: What are fixtures in Cypress and how do you use them?**

**A:**
Fixtures are static JSON (or other format) files stored in `cypress/fixtures/`. They externalize test data from test code, making tests easier to read and data easier to update.

Creating fixture files:
```json
// cypress/fixtures/user.json
{
  "validUser": {
    "email": "admin@example.com",
    "password": "Admin@123",
    "name": "Admin User",
    "role": "admin"
  },
  "guestUser": {
    "email": "guest@example.com",
    "password": "Guest@123",
    "role": "viewer"
  },
  "invalidCredentials": {
    "email": "notreal@example.com",
    "password": "wrongpass"
  }
}
```

```json
// cypress/fixtures/products.json
[
  { "id": 1, "name": "Laptop", "price": 999.99, "category": "electronics" },
  { "id": 2, "name": "Desk Chair", "price": 349.99, "category": "furniture" },
  { "id": 3, "name": "Monitor", "price": 599.99, "category": "electronics" }
]
```

Using fixtures in tests:
```javascript
// Method 1: cy.fixture() with .then()
cy.fixture('user').then((userData) => {
  cy.get('[data-cy="email"]').type(userData.validUser.email);
  cy.get('[data-cy="password"]').type(userData.validUser.password);
  cy.get('[data-cy="submit"]').click();
});

// Method 2: as an alias — use in beforeEach
describe('Login Tests', function () {
  beforeEach(function () {
    cy.fixture('user').as('userData');           // MUST use function() not arrow function
  });

  it('logs in successfully', function () {
    cy.get('[data-cy="email"]').type(this.userData.validUser.email);   // this.userData
    cy.get('[data-cy="password"]').type(this.userData.validUser.password);
    cy.get('[data-cy="submit"]').click();
  });

  it('rejects invalid credentials', function () {
    cy.get('[data-cy="email"]').type(this.userData.invalidCredentials.email);
    cy.get('[data-cy="password"]').type(this.userData.invalidCredentials.password);
    cy.get('[data-cy="submit"]').click();
    cy.get('[data-cy="error-msg"]').should('be.visible');
  });
});

// Method 3: as API stub response
cy.intercept('GET', '/api/products', { fixture: 'products.json' }).as('getProducts');
cy.visit('/products');
cy.wait('@getProducts');
```

Real-world context: Fixture files allow non-technical team members to update test data without touching test code. They also make it easy to run the same test with different data sets.

Common mistake: Using arrow functions `() => {}` when accessing fixture data via `this`. Arrow functions do not bind `this` from Mocha's context. Always use `function() {}` when using `this.fixtureName`.

---

**Q28: How do you create and use custom commands in Cypress?**

**A:**
Custom commands extend the `cy.*` namespace with your own reusable commands. They go in `cypress/support/commands.js`.

```javascript
// cypress/support/commands.js

// ─── UI Login Command ────────────────────────────────────────────────────────
Cypress.Commands.add('login', (email, password) => {
  cy.get('[data-cy="email"]').clear().type(email);
  cy.get('[data-cy="password"]').clear().type(password);
  cy.get('[data-cy="submit"]').click();
  cy.url().should('include', '/dashboard');
});

// ─── API Login (faster — bypasses UI) ────────────────────────────────────────
Cypress.Commands.add('loginViaApi', (email, password) => {
  cy.request({
    method: 'POST',
    url: `${Cypress.env('apiUrl')}/auth/login`,
    body: { email, password }
  }).then(({ body }) => {
    window.localStorage.setItem('authToken', body.token);
    window.localStorage.setItem('userId', body.userId);
  });
  cy.visit('/dashboard');
});

// ─── Set Auth Token directly ─────────────────────────────────────────────────
Cypress.Commands.add('setAuthToken', (token) => {
  window.localStorage.setItem('authToken', token);
});

// ─── Create a User via API (test setup) ─────────────────────────────────────
Cypress.Commands.add('createUser', (userData = {}) => {
  const defaults = {
    name: 'Test User',
    email: `test-${Date.now()}@example.com`,
    role: 'viewer'
  };
  return cy.request({
    method: 'POST',
    url: `${Cypress.env('apiUrl')}/users`,
    headers: { 'Authorization': `Bearer ${Cypress.env('adminToken')}` },
    body: { ...defaults, ...userData }
  }).then(({ body }) => body); // returns the created user
});

// ─── Select from custom dropdown ─────────────────────────────────────────────
Cypress.Commands.add('selectFromDropdown', (dropdownSelector, option) => {
  cy.get(dropdownSelector).click();
  cy.get('[data-cy="dropdown-option"]').contains(option).click();
  cy.get(dropdownSelector).should('contain', option);
});

// ─── Overwrite existing command ───────────────────────────────────────────────
Cypress.Commands.overwrite('visit', (originalFn, url, options) => {
  const combinedOptions = {
    ...options,
    onBeforeLoad(win) {
      // Inject auth token on every page load
      const token = Cypress.env('authToken');
      if (token) win.localStorage.setItem('authToken', token);
      if (options?.onBeforeLoad) options.onBeforeLoad(win);
    }
  };
  return originalFn(url, combinedOptions);
});
```

Using custom commands:
```javascript
// In any spec file — available as cy.commandName()
cy.login('admin@example.com', 'Admin@123');
cy.loginViaApi(Cypress.env('adminEmail'), Cypress.env('adminPassword'));
cy.selectFromDropdown('[data-cy="country-dropdown"]', 'Australia');

// Create user, use ID in test
cy.createUser({ name: 'Jane Doe', role: 'admin' }).then((user) => {
  cy.visit(`/users/${user.id}`);
  cy.get('[data-cy="user-name"]').should('have.text', 'Jane Doe');
});
```

TypeScript type declarations:
```typescript
// cypress/support/index.d.ts
declare namespace Cypress {
  interface Chainable {
    login(email: string, password: string): Chainable<void>;
    loginViaApi(email: string, password: string): Chainable<void>;
    setAuthToken(token: string): Chainable<void>;
    createUser(userData?: Partial<User>): Chainable<User>;
    selectFromDropdown(selector: string, option: string): Chainable<void>;
  }
}
```

Real-world context: Every project should have `cy.login()` or `cy.loginViaApi()` as a custom command. Without it, every spec that requires authentication duplicates 5–10 lines of login code. A change to the login form breaks every test rather than one command.

---

**Q29: How do you implement the Page Object Model (POM) in Cypress?**

**A:**
POM in Cypress separates element selectors and user interactions into page classes. Tests focus on behavior, not implementation details.

```javascript
// cypress/support/pages/LoginPage.js
class LoginPage {
  // Selectors as getters — fresh cy.get() call each time (prevents stale references)
  get emailInput()    { return cy.get('[data-cy="email-input"]'); }
  get passwordInput() { return cy.get('[data-cy="password-input"]'); }
  get submitButton()  { return cy.get('[data-cy="submit-btn"]'); }
  get errorMessage()  { return cy.get('[data-cy="error-message"]'); }
  get forgotPasswordLink() { return cy.contains('a', 'Forgot Password?'); }

  // Actions
  navigate() {
    cy.visit('/login');
    return this;  // allows chaining
  }

  fillEmail(email) {
    this.emailInput.clear().type(email);
    return this;
  }

  fillPassword(password) {
    this.passwordInput.clear().type(password);
    return this;
  }

  submit() {
    this.submitButton.click();
    return this;
  }

  login(email, password) {
    this.navigate();
    this.fillEmail(email);
    this.fillPassword(password);
    this.submit();
    return this;
  }

  // Assertions
  assertErrorMessage(message) {
    this.errorMessage.should('be.visible').and('contain', message);
  }

  assertRedirectedToDashboard() {
    cy.url().should('include', '/dashboard');
  }
}

export default new LoginPage();
```

```javascript
// cypress/e2e/login.cy.js
import LoginPage from '../support/pages/LoginPage';
import DashboardPage from '../support/pages/DashboardPage';

describe('Login', () => {
  it('logs in with valid credentials', () => {
    LoginPage.login('admin@example.com', 'Admin@123');
    LoginPage.assertRedirectedToDashboard();
    DashboardPage.assertWelcomeMessage('Admin User');
  });

  it('shows error for wrong password', () => {
    LoginPage.navigate();
    LoginPage.fillEmail('admin@example.com');
    LoginPage.fillPassword('wrongpass');
    LoginPage.submit();
    LoginPage.assertErrorMessage('Invalid credentials');
  });

  it('shows error for empty email', () => {
    LoginPage.navigate();
    LoginPage.submit();
    LoginPage.assertErrorMessage('Email is required');
  });
});
```

Real-world context: When the login button's `data-cy` attribute changes from `submit-btn` to `login-btn`, you update one line in `LoginPage.js` instead of every spec file that logs in.

Common mistake: Storing element references in POM constructors like `this.emailInput = cy.get(...)`. Cypress elements go stale after DOM mutations. Always define selectors as getter methods that call `cy.get()` fresh each time.

---

**Q30: How do beforeEach, afterEach, before, and after hooks work in Cypress?**

**A:**

```javascript
describe('Shopping Cart Tests', () => {

  // Runs ONCE before ALL tests in this describe block
  before(() => {
    cy.log('=== Seeding test database ===');
    cy.task('seedProducts', { count: 10 });
    cy.task('seedUser', { email: 'test@example.com' });
  });

  // Runs ONCE after ALL tests in this describe block
  after(() => {
    cy.log('=== Cleaning up test data ===');
    cy.task('cleanDatabase');
  });

  // Runs before EACH individual test (it block)
  beforeEach(() => {
    // Login fresh before each test (or use cy.session for caching)
    cy.loginViaApi(Cypress.env('testEmail'), Cypress.env('testPassword'));
    cy.visit('/shop');

    // Reset any state
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // Runs after EACH individual test
  afterEach(() => {
    // Clean up created records
    // Screenshot on failure is handled automatically by Cypress
    cy.log('Test finished');
  });

  it('adds product to cart', () => {
    cy.get('[data-cy="add-to-cart"]').first().click();
    cy.get('[data-cy="cart-count"]').should('have.text', '1');
  });

  it('removes product from cart', () => {
    cy.get('[data-cy="add-to-cart"]').first().click();
    cy.get('[data-cy="remove-item"]').click();
    cy.get('[data-cy="empty-cart-message"]').should('be.visible');
  });

  // Nested describe — hooks also nest
  describe('Checkout flow', () => {
    beforeEach(() => {
      // This runs IN ADDITION to the parent beforeEach
      cy.get('[data-cy="add-to-cart"]').first().click(); // add item before checkout tests
    });

    it('proceeds to checkout with item in cart', () => {
      cy.get('[data-cy="checkout-btn"]').click();
      cy.url().should('include', '/checkout');
    });
  });
});
```

Hook execution order for nested describes:
```
before (outer) → beforeEach (outer) → beforeEach (inner) → it → afterEach (inner) → afterEach (outer) → after (outer)
```

Skip and only:
```javascript
it.skip('skipped — known bug JIRA-123', () => { });
it.only('focus only this test during development', () => { });
describe.skip('entire suite disabled', () => { });
describe.only('focus this suite', () => { });
```

Real-world context: `before()` is best for expensive one-time setup like database seeding. `beforeEach()` is for state reset between tests — ensuring each test starts from a known clean state. Tests should never depend on state left by a previous test.

Common mistake: Using `before()` for login. If the session expires or is cleared between tests, subsequent tests fail. Use `beforeEach()` with `cy.session()` for login.

---

**Q31: How do describe() and context() work in Cypress?**

**A:**
Both `describe()` and `context()` are identical in behavior — they are both aliases for Mocha's `describe()`. They exist to allow more natural English when organizing tests.

```javascript
// describe() — technical grouping
describe('User Authentication', () => {
  describe('Login', () => {
    it('logs in with valid email and password', () => { });
    it('rejects invalid credentials', () => { });
    it('rejects an empty form submission', () => { });
  });

  describe('Password Reset', () => {
    it('sends reset email for a valid address', () => { });
    it('shows error for unknown email address', () => { });
  });
});

// context() — behavioral grouping (reads like English)
describe('Shopping Cart', () => {
  context('when the cart is empty', () => {
    it('shows an empty state message', () => { });
    it('disables the checkout button', () => { });
  });

  context('when the cart has items', () => {
    beforeEach(() => {
      cy.get('[data-cy="add-to-cart"]').first().click();
    });

    it('shows the item count badge', () => { });
    it('calculates the total correctly', () => { });
    it('allows removing items', () => { });
  });

  context('when the user is not logged in', () => {
    it('redirects to login when clicking checkout', () => { });
  });
});
```

Real-world context: Using `context()` for BDD-style "when/given" scenarios makes tests readable as a living specification. The test output in the Cypress runner shows the full describe/context/it hierarchy, making failures instantly understandable.

---

## SECTION: Advanced Topics

---

**Q32: How do you manage environment variables in Cypress?**

**A:**
Cypress supports multiple sources for environment variables, with a clear override priority:

```
Priority (highest to lowest):
1. --env flag on CLI
2. CYPRESS_ prefixed OS environment variables
3. cypress.env.json file
4. env section in cypress.config.js
```

Defining in config:
```javascript
// cypress.config.js
env: {
  apiUrl: 'https://api.example.com',
  adminEmail: 'admin@example.com',
  featureFlag_newUI: true,
}
```

Separate secret file (not committed to git):
```json
// cypress.env.json — add to .gitignore
{
  "adminPassword": "SecretPass@123",
  "authToken": "Bearer abc123xyz",
  "dbConnectionString": "postgresql://user:pass@localhost/testdb"
}
```

Accessing in tests:
```javascript
Cypress.env('apiUrl')          // 'https://api.example.com'
Cypress.env('adminEmail')      // 'admin@example.com'
Cypress.env('featureFlag_newUI')  // true

// Conditional test based on feature flag
if (Cypress.env('featureFlag_newUI')) {
  cy.get('[data-cy="new-dashboard"]').should('be.visible');
} else {
  cy.get('[data-cy="old-dashboard"]').should('be.visible');
}
```

Passing via CLI:
```bash
# Single env var
npx cypress run --env apiUrl=https://staging.api.com

# Multiple env vars
npx cypress run --env apiUrl=https://staging.api.com,adminEmail=test@test.com

# OS environment variables (prefix with CYPRESS_)
export CYPRESS_adminPassword=SecretPass123
npx cypress run    # Cypress auto-reads CYPRESS_adminPassword as adminPassword
```

Dynamic environment config (e.g., staging vs production):
```javascript
// cypress.config.js
setupNodeEvents(on, config) {
  const env = config.env.environment || 'local';

  const envConfigs = {
    local:   { baseUrl: 'http://localhost:3000', apiUrl: 'http://localhost:8080' },
    staging: { baseUrl: 'https://staging.myapp.com', apiUrl: 'https://api.staging.myapp.com' },
    prod:    { baseUrl: 'https://myapp.com', apiUrl: 'https://api.myapp.com' },
  };

  config.baseUrl = envConfigs[env].baseUrl;
  config.env.apiUrl = envConfigs[env].apiUrl;
  return config;
}
```
```bash
npx cypress run --env environment=staging
```

Real-world context: Never hardcode URLs or credentials in test files. Use `Cypress.env()` everywhere, set real values in `cypress.env.json` locally (gitignored), and inject via CI secrets in pipelines.

---

**Q33: What is cy.session() and why is it important?**

**A:**
`cy.session()` (Cypress 9+) caches browser session state (cookies, localStorage, sessionStorage) across tests. On subsequent tests, it restores the saved session instead of re-running the login flow, dramatically reducing test suite runtime.

Without cy.session() — login runs before EVERY test:
```javascript
// Slow — login runs 50 times for a 50-test suite
beforeEach(() => {
  cy.visit('/login');
  cy.get('[data-cy="email"]').type('admin@example.com');
  cy.get('[data-cy="password"]').type('Admin@123');
  cy.get('[data-cy="submit"]').click();
  cy.url().should('include', '/dashboard');
});
```

With cy.session() — login runs once, session is restored for all other tests:
```javascript
// Fast — login runs once, session cached and reused
beforeEach(() => {
  cy.session(
    'adminSession',   // session cache key — unique identifier
    () => {
      // This "setup" function only runs when cache is empty
      cy.visit('/login');
      cy.get('[data-cy="email"]').type('admin@example.com');
      cy.get('[data-cy="password"]').type('Admin@123');
      cy.get('[data-cy="submit"]').click();
      cy.url().should('include', '/dashboard');
    },
    {
      // Optional: validate session is still valid before restoring
      validate() {
        cy.request({ url: '/api/profile', failOnStatusCode: false })
          .its('status').should('eq', 200);
      },
      // Which storage to cache and restore
      cacheAcrossSpecs: true,  // persist session across spec files
    }
  );
  cy.visit('/dashboard');
});
```

Multiple sessions (e.g., admin and guest):
```javascript
// Custom command for session management
Cypress.Commands.add('loginAs', (role) => {
  const users = {
    admin: { email: 'admin@example.com', password: 'Admin@123' },
    guest: { email: 'guest@example.com', password: 'Guest@123' },
  };

  const user = users[role];

  cy.session(role, () => {
    cy.loginViaApi(user.email, user.password);
  });
});

// In tests
cy.loginAs('admin');
cy.loginAs('guest');
```

Real-world context: A 50-test suite where each test logs in via UI (3 seconds each) takes 150 seconds just for logins. With `cy.session()`, login runs once (3 seconds total for the entire suite). This is a 50× improvement on login overhead.

Common mistake: Using `cy.session()` without the `validate` option when the app rotates session tokens. Without validation, Cypress restores an expired session and tests fail with 401 errors.

---

**Q34: How do you run cross-browser tests in Cypress?**

**A:**

```bash
# Chrome (most common)
npx cypress run --browser chrome

# Firefox
npx cypress run --browser firefox

# Microsoft Edge
npx cypress run --browser edge

# Chromium (open-source Chrome)
npx cypress run --browser chromium

# Electron (default headless — bundled with Cypress)
npx cypress run

# Headed (browser visible) — useful for debugging
npx cypress run --browser chrome --headed

# Specific spec in specific browser
npx cypress run --browser firefox --spec "cypress/e2e/login.cy.js"

# Run in interactive mode with specific browser
npx cypress open --browser firefox
```

Detecting browser in tests (for browser-specific behavior):
```javascript
it('handles browser-specific behavior', () => {
  if (Cypress.isBrowser('firefox')) {
    // Firefox-specific assertion
    cy.get('[data-cy="element"]').should('have.css', 'scrollbar-width', 'thin');
  } else {
    cy.get('[data-cy="element"]').should('be.visible');
  }
});

// Skip a test in a specific browser
it('uses clipboard API', { browser: '!firefox' }, () => {
  // This test won't run in Firefox
  cy.window().its('navigator.clipboard').should('exist');
});
```

Browser support limitations:
- Safari/WebKit: experimental via `experimentalWebKitSupport: true` — not production-ready
- Internet Explorer: not supported at all
- Chrome, Firefox, Edge: fully supported

Real-world context: Most organizations run primary tests in Chrome (fastest, most stable) and run the full suite in Firefox and Edge in a nightly job. Safari is the most common gap — Playwright is often chosen specifically for Safari support.

---

**Q35: What are all the important Cypress CLI commands?**

**A:**

```bash
# ─── Open (Interactive) ────────────────────────────────────────────────────
npx cypress open                                      # open interactive Test Runner
npx cypress open --browser chrome                     # open with Chrome
npx cypress open --browser firefox
npx cypress open --project ./my-project              # specify project directory
npx cypress open --config-file cypress.staging.config.js  # alternate config file

# ─── Run (Headless / CI) ───────────────────────────────────────────────────
npx cypress run                                       # run all specs, headless Electron
npx cypress run --browser chrome                      # run in Chrome
npx cypress run --browser firefox
npx cypress run --headed                              # run with browser visible

# Spec selection
npx cypress run --spec "cypress/e2e/login.cy.js"                    # single spec
npx cypress run --spec "cypress/e2e/api/*.cy.js"                    # all API specs
npx cypress run --spec "cypress/e2e/login.cy.js,cypress/e2e/signup.cy.js"  # multiple

# Environment variables
npx cypress run --env apiUrl=https://staging.api.com,adminEmail=test@test.com
npx cypress run --env environment=staging

# Config overrides
npx cypress run --config baseUrl=https://staging.com
npx cypress run --config baseUrl=https://staging.com,defaultCommandTimeout=15000
npx cypress run --config-file cypress.staging.config.js

# Reporting
npx cypress run --reporter mochawesome
npx cypress run --reporter junit --reporter-options mochaFile=results/junit.xml

# Cypress Cloud (paid)
npx cypress run --record --key YOUR_RECORD_KEY                       # record to cloud
npx cypress run --parallel --record --key YOUR_RECORD_KEY            # parallel
npx cypress run --tag "regression,smoke"                             # tag runs

# ─── Utility ───────────────────────────────────────────────────────────────
npx cypress info                      # print detected browsers and system info
npx cypress verify                    # verify Cypress binary is installed correctly
npx cypress version                   # print Cypress version
npx cypress cache list                # list all cached Cypress versions
npx cypress cache clear               # clear Cypress binary cache
npx cypress cache path                # show cache folder location

# ─── Running via package.json scripts ────────────────────────────────────
npm run cy:run                         # your defined scripts
npm run cy:open
```

---

## SECTION: Troubleshooting and Limitations

---

**Q36: What are Cypress's key limitations you must know?**

**A:**

| Limitation | Details | Workaround |
|---|---|---|
| JavaScript/TypeScript only | No Java, Python, C#, Ruby support | Use Playwright or Selenium for other languages |
| Same-origin restriction | Cross-origin navigation limited | Use `cy.origin()` (Cypress 9.6+); may still have edge cases |
| Multi-tab testing | Cannot control multiple browser tabs natively | Redesign test; test individual pages separately |
| iFrame support | Limited native support | Use `cypress-iframe` plugin |
| Native mobile apps | No | Use Appium or Detox |
| Parallel execution | Requires Cypress Cloud (paid) or manual CI matrix config | Use `--parallel` with CI matrix (free alternative) |
| Real Safari/WebKit | Experimental only | Use Playwright for Safari testing |
| File downloads | Needs custom configuration | Use `cy.readFile()` after triggering download |
| Shadow DOM | Limited support | Use `cy.get().shadow()` or plugins |
| Video streaming / WebSockets | Cannot test WebSocket messages directly | Spy via `cy.window()` and hook into `WebSocket` |

Specific limitation scenarios:
```javascript
// CANNOT natively — test a Stripe payment iframe
// MUST use — cypress-iframe plugin
import 'cypress-iframe';
cy.frameLoaded('[data-cy="stripe-iframe"]');
cy.iframe('[data-cy="stripe-iframe"]').find('[data-testid="cardNumber"]').type('4111111111111111');

// CANNOT natively — navigate to a different domain
cy.visit('https://myapp.com');
cy.get('[data-cy="login-with-google"]').click();
// Next navigation goes to accounts.google.com — Cypress blocks this

// CAN — use cy.origin() for cross-origin (Cypress 9.6+)
cy.origin('https://accounts.google.com', () => {
  cy.get('input[type="email"]').type('test@gmail.com');
});
```

---

**Q37: How do you fix the "element detached from DOM" error?**

**A:**
This error occurs when Cypress finds an element, then the DOM re-renders (React/Angular/Vue component update) before the next action fires — the element reference is now stale.

```
CypressError: cy.click() failed because this element is detached from the DOM.
```

Cause:
```javascript
// WRONG — element found, then DOM re-renders, reference is stale
cy.get('[data-cy="input"]').type('search term');
// React re-renders the input after typing
cy.get('[data-cy="submit"]').click();  // might be stale if submit is inside re-rendered component

// More obviously wrong:
const btn = cy.get('[data-cy="submit"]');  // not a DOM element — this is wrong anyway
cy.get('[data-cy="input"]').type('hello');
btn.click();  // stale reference
```

Fixes:
```javascript
// Fix 1: Re-query the element immediately before acting
cy.get('[data-cy="input"]').type('search term');
cy.get('[data-cy="submit"]').click();   // fresh query — no stale reference

// Fix 2: Ensure element stability before interaction
cy.get('[data-cy="submit"]').should('be.visible').click();
// The .should() assertion retries until element is in expected state

// Fix 3: Use .should() callback for complex assertions on potentially-re-rendering elements
cy.get('[data-cy="counter"]').should(($el) => {
  expect($el.text()).to.equal('5');
});
```

Real-world context: This most commonly happens in React applications that re-render synchronously on input events. The solution is always to re-query rather than cache element references.

---

**Q38: How do you handle cross-origin errors and iframe limitations?**

**A:**

Cross-origin errors:
```
CypressError: Cypress detected a cross-origin URL redirect.
```

Solution — cy.origin() (Cypress 9.6+):
```javascript
// Enable in cypress.config.js
experimentalModifyObstructiveThirdPartyCode: true

// In test
cy.visit('https://myapp.com/login');
cy.get('[data-cy="sso-login"]').click();  // redirects to auth.provider.com

cy.origin('https://auth.provider.com', () => {
  cy.get('#username').type('user@myapp.com');
  cy.get('#password').type('password123');
  cy.get('#submit').click();
});

// Back on myapp.com
cy.url().should('include', 'myapp.com/dashboard');
```

iFrame limitations:
```bash
npm install --save-dev cypress-iframe
```

```javascript
// cypress/support/e2e.js
import 'cypress-iframe';

// In test
cy.frameLoaded('[data-cy="payment-iframe"]');           // wait for iframe to load
cy.iframe('[data-cy="payment-iframe"]')
  .find('[data-testid="card-number"]')
  .type('4111111111111111');
cy.iframe('[data-cy="payment-iframe"]')
  .find('[data-testid="expiry"]')
  .type('12/28');
```

---

**Q39: How do you deal with flaky tests in Cypress?**

**A:**
Flaky tests pass sometimes and fail other times without code changes. Common causes and fixes:

1. Hard waits — replace with event-based waits:
```javascript
// WRONG — arbitrary sleep
cy.wait(3000);
cy.get('[data-cy="data-table"]').should('be.visible');

// CORRECT — wait for the API call that populates the table
cy.intercept('GET', '/api/data').as('loadData');
cy.visit('/data-page');
cy.wait('@loadData');
cy.get('[data-cy="data-table"]').should('be.visible');
```

2. Animation not complete — wait for animation class:
```javascript
cy.get('[data-cy="modal"]').should('have.class', 'fade-in-complete').click();
```

3. Race condition with re-render:
```javascript
// Add .should() before interacting to ensure element stability
cy.get('[data-cy="submit"]').should('be.enabled').click();
```

4. Inconsistent test data — use isolated, predictable data:
```javascript
// Create fresh test data before each test
beforeEach(() => {
  cy.task('seedFreshUser').then((user) => {
    cy.wrap(user).as('testUser');
  });
});
```

5. Configure retries for CI resilience:
```javascript
// cypress.config.js
retries: {
  runMode: 2,   // retry failing test 2 times in CI
  openMode: 0,
}

// Or per-test:
it('flaky test', { retries: 3 }, () => { });
```

6. Identify flaky tests using Cypress Cloud retry analysis or:
```bash
npx cypress run --spec "cypress/e2e/flaky.cy.js" --env repeatCount=10
# Run the same spec 10 times to expose intermittent failures
```

---

## SECTION: Comparison and Interview Questions

---

**Q40: How does Cypress compare to Playwright and Selenium? When do you choose each?**

**A:**

| Feature | Cypress | Playwright | Selenium |
|---|---|---|---|
| **Architecture** | Runs inside browser | CDP-based, separate process | WebDriver HTTP protocol |
| **Language** | JavaScript / TypeScript only | JS, TS, Python, Java, C# | Java, Python, C#, Ruby, JS |
| **Auto-waiting** | Yes — built-in retry | Yes — built-in retry | No — manual waits required |
| **Browser support** | Chrome, Firefox, Edge, Electron | Chrome, Firefox, Safari, Edge | All major + IE |
| **Multi-tab** | Limited (experimental) | Full support | Full support |
| **Cross-origin** | Limited (`cy.origin()`) | Full support | Full support |
| **iFrame** | Plugin required | Native full support | Full support |
| **Mobile testing** | No | Device emulation (web) | Limited |
| **Network intercept** | `cy.intercept()` — native | `route()` — native | BrowserMob Proxy needed |
| **Parallel execution** | Paid Cloud or CI matrix | Built-in workers — free | Selenium Grid — free |
| **Speed** | Fast for single-origin | Fast | Slower (HTTP overhead) |
| **Debugging** | Time-travel + live reload | Trace viewer | Browser DevTools only |
| **Setup complexity** | Low | Low | Moderate |
| **Community** | Large | Growing fast | Very large (mature) |
| **Safari support** | Experimental only | Full — first-class | Limited |

When to choose Cypress:
- JavaScript/TypeScript team
- React/Vue/Angular single-page application
- Want fast feedback loop and great DX
- Single-domain application (no OAuth redirects to third-party domains)

When to choose Playwright:
- Need cross-browser including Safari
- Multi-tab, multi-origin workflows
- Team codes in Python or Java alongside TypeScript
- Need free parallel execution
- Complex scenarios requiring full CDP access

When to choose Selenium:
- Large enterprise with existing Java/Python/C# test infrastructure
- Internet Explorer testing requirement
- Non-JavaScript team
- Need the broadest possible browser and OS matrix

---

**Q41: How do you run Cypress in CI (GitHub Actions, Jenkins)?**

**A:**

GitHub Actions:
```yaml
# .github/workflows/e2e.yml
name: E2E Tests

on: [push, pull_request]

jobs:
  cypress-run:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        browser: [chrome, firefox]    # run in both browsers

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Start application
        run: npm start &                # start app in background
        env:
          PORT: 3000

      - name: Wait for app to be ready
        run: npx wait-on http://localhost:3000

      - name: Run Cypress tests
        run: npx cypress run --browser ${{ matrix.browser }}
        env:
          CYPRESS_adminEmail: ${{ secrets.ADMIN_EMAIL }}
          CYPRESS_adminPassword: ${{ secrets.ADMIN_PASSWORD }}
          CYPRESS_apiUrl: https://staging.api.myapp.com

      - name: Upload screenshots on failure
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: cypress-screenshots-${{ matrix.browser }}
          path: cypress/screenshots

      - name: Upload videos
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: cypress-videos-${{ matrix.browser }}
          path: cypress/videos
```

Jenkins Declarative Pipeline:
```groovy
pipeline {
  agent any

  stages {
    stage('Install') {
      steps {
        sh 'npm ci'
      }
    }

    stage('E2E Tests') {
      steps {
        sh '''
          npx cypress run \
            --browser chrome \
            --spec "cypress/e2e/**/*.cy.js" \
            --env environment=staging
        '''
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'cypress/videos/**/*.mp4', allowEmptyArchive: true
      archiveArtifacts artifacts: 'cypress/screenshots/**/*.png', allowEmptyArchive: true
    }
    failure {
      echo 'E2E tests failed — check artifacts for screenshots and videos'
    }
  }
}
```

Key CI considerations:
- Use `npx cypress run` (not `open`) — no GUI in CI
- Prefix secrets with `CYPRESS_` for automatic Cypress pickup
- Enable retries (`retries: { runMode: 2 }`) to absorb transient failures
- Always upload screenshots and videos as artifacts from failed runs
- Use `--record --key` with Cypress Cloud for parallel execution and test analytics

---

**Q42: How do you debug a failing Cypress test?**

**A:**

In interactive mode (npx cypress open):
```javascript
// Time-travel debugging — click any command in the left panel
// to see the DOM snapshot at that exact moment

// Pause execution
cy.get('[data-cy="element"]').pause();      // test pauses — you can inspect manually

// Debug — opens DevTools at this point
cy.get('[data-cy="element"]').debug();

// Log to command log
cy.log('Current state: waiting for modal');

// Log to browser console
cy.get('[data-cy="price"]').invoke('text').then(console.log);

// Stop after specific command (use .only to isolate)
it.only('debug this test', () => {
  cy.visit('/');
  cy.get('[data-cy="element"]').click();
});
```

In CI / headless mode:
```javascript
// Screenshots auto-captured on failure
// Videos auto-recorded — check cypress/videos/

// Add cy.log() breadcrumbs for debugging run logs
cy.log('Step 1: Visiting login page');
cy.visit('/login');
cy.log('Step 2: Filling credentials');
cy.get('[data-cy="email"]').type('user@test.com');
```

From the command line:
```bash
# Run headed (browser visible) during local debugging
npx cypress run --headed --no-exit --spec "cypress/e2e/failing.cy.js"

# Enable verbose logging
DEBUG=cypress:* npx cypress run 2>&1 | head -200
```

Debugging network issues:
```javascript
cy.intercept('*', (req) => {
  console.log('REQUEST:', req.method, req.url);
}).as('allRequests');
```

---

**Q43: What is the difference between cy.request() for API testing and using a dedicated API testing tool like Postman or Rest Assured?**

**A:**
`cy.request()` is suitable for API testing within an E2E test suite but has limitations compared to dedicated API testing tools.

```javascript
// cy.request() — API test within Cypress
describe('Users API', () => {
  let userId;

  it('POST /api/users — creates a user', () => {
    cy.request({
      method: 'POST',
      url: `${Cypress.env('apiUrl')}/users`,
      body: { name: 'QA User', email: 'qa@test.com', role: 'viewer' }
    }).then((res) => {
      expect(res.status).to.eq(201);
      expect(res.body).to.have.property('id');
      userId = res.body.id;
    });
  });

  it('GET /api/users/:id — retrieves created user', () => {
    cy.request(`${Cypress.env('apiUrl')}/users/${userId}`)
      .then((res) => {
        expect(res.status).to.eq(200);
        expect(res.body.email).to.eq('qa@test.com');
      });
  });

  it('DELETE /api/users/:id', () => {
    cy.request({ method: 'DELETE', url: `${Cypress.env('apiUrl')}/users/${userId}` })
      .its('status').should('eq', 204);
  });
});
```

Use cy.request() when:
- Setting up test data before E2E tests
- Verifying backend state after UI actions
- Quick API smoke tests alongside E2E tests

Use dedicated tools (Postman, Rest Assured, SuperTest) when:
- Comprehensive API contract testing
- Testing request/response schema validation (JSON Schema)
- Authentication flows (OAuth 2.0, JWT token refresh)
- Performance testing of APIs
- API tests need to run independently of browser

---

**Q44: What are 10 common mistakes Cypress developers make?**

**A:**

1. **Using `cy.wait(ms)` instead of `cy.wait('@alias')`** — arbitrary sleeps cause flakiness and slow tests

2. **Using arrow functions with fixture aliases** — breaks `this` context:
```javascript
// WRONG
beforeEach(() => { cy.fixture('user').as('user'); });
it('test', () => { cy.get('[data-cy="email"]').type(this.user.email); }); // this.user is undefined

// CORRECT
beforeEach(function() { cy.fixture('user').as('user'); });
it('test', function() { cy.get('[data-cy="email"]').type(this.user.email); }); // works
```

3. **Storing cy.get() results as variables** — Cypress commands are async:
```javascript
const btn = cy.get('[data-cy="submit"]'); // wrong — not a DOM element
```

4. **Not returning config from setupNodeEvents** — env overrides silently discarded

5. **Using cy.intercept() after the action that triggers the request** — intercept must be set up first

6. **Logging in through the UI on every test** — use `cy.session()` or `loginViaApi()`

7. **Enabling View Results Tree listener in load tests** — (JMeter mistake, not Cypress — but) leaving `cy.log()` with huge data in production test runs wastes memory

8. **Not scoping selectors** — `cy.get('button')` on a page with 20 buttons is ambiguous

9. **Using CSS classes as selectors** — breaks when styles are refactored

10. **Testing implementation details** — testing internal state instead of user-visible behavior. Test what the user sees, not how the code works.

---

**Q45: How does cy.clock() and cy.tick() work for testing time-dependent behavior?**

**A:**
`cy.clock()` replaces the browser's native time functions (`Date`, `setTimeout`, `setInterval`, `clearTimeout`, `clearInterval`) with Cypress-controlled stubs. `cy.tick()` advances the fake clock by a specified number of milliseconds.

```javascript
// Test a session timeout warning without actually waiting 30 minutes
it('shows session timeout warning after 25 minutes of inactivity', () => {
  cy.clock();                                     // freeze time at current moment

  cy.loginViaApi('admin@example.com', 'Admin@123');
  cy.visit('/dashboard');

  cy.tick(25 * 60 * 1000);                       // advance clock 25 minutes instantly

  cy.get('[data-cy="session-warning"]').should('be.visible');
  cy.get('[data-cy="session-warning"]').should('contain', '5 minutes remaining');
});

// Test a countdown timer
it('counts down from 60 seconds', () => {
  cy.clock();
  cy.visit('/quiz');

  cy.get('[data-cy="timer"]').should('have.text', '60');

  cy.tick(10000);   // advance 10 seconds
  cy.get('[data-cy="timer"]').should('have.text', '50');

  cy.tick(50000);   // advance remaining 50 seconds
  cy.get('[data-cy="timer"]').should('have.text', '0');
  cy.get('[data-cy="quiz-expired"]').should('be.visible');
});

// Control Date.now() for reproducible tests
it('shows "New" badge on products added in the last 7 days', () => {
  const fixedDate = new Date('2025-01-15').getTime();
  cy.clock(fixedDate);                            // set clock to specific date

  cy.visit('/products');
  cy.get('[data-cy="new-badge"]').should('be.visible');  // predictable
});

// Restore the clock after test
afterEach(() => {
  cy.clock().then((clock) => clock.restore());
});
```

Real-world context: Without `cy.clock()`, testing time-dependent features (token expiration, countdown timers, "X days ago" labels) would require waiting for real time to pass. `cy.tick()` makes these tests run in milliseconds.

---

**Q46: Full Interview Q&A — Cypress Senior Engineer Questions**

---

**Q46: An interviewer asks: "Explain how Cypress's automatic waiting prevents flaky tests." Give a complete answer.**

**A:**
Cypress implements an internal retry loop for every DOM-interacting command. When you write `cy.get('[data-cy="result"]').should('have.text', 'Order Confirmed')`, Cypress does not execute this once and pass or fail immediately. Instead it runs an internal loop:

1. Query `document.querySelector('[data-cy="result"]')` → element found
2. Check `element.textContent === 'Order Confirmed'` → false → retry
3. Wait ~50ms → retry step 1
4. Repeat until the condition is true OR `defaultCommandTimeout` (default 10s) is exceeded

This means the test automatically absorbs:
- API response delays (React re-renders the component when data arrives)
- Animation delays (element becomes visible after a CSS transition)
- Component mounting delays (element not in DOM yet)

Without this, you would write:
```java
// Selenium — explicit wait needed
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.textToBePresentInElement(
    driver.findElement(By.cssSelector("[data-cy='result']")), "Order Confirmed"));
```

In Cypress, the assertion itself IS the wait:
```javascript
cy.get('[data-cy="result"]').should('have.text', 'Order Confirmed');
// No additional code — Cypress retries until text appears or timeout
```

The remaining source of flakiness (network calls) is addressed by `cy.intercept().as()` + `cy.wait('@alias')` — you wait for the specific network event, not for arbitrary time.

---

**Q47: How do you handle a situation where a test must work against both a real API and a mocked API (e.g., in CI vs. local development)?**

**A:**
Use a Cypress environment variable to switch between modes:

```javascript
// cypress.config.js
env: {
  mockApi: false,  // default: real API
}

// cypress/support/commands.js
Cypress.Commands.add('setupApiMode', () => {
  if (Cypress.env('mockApi')) {
    // Stub all API calls with fixtures
    cy.intercept('GET', '/api/users', { fixture: 'users.json' });
    cy.intercept('GET', '/api/products', { fixture: 'products.json' });
    cy.intercept('POST', '/api/orders', { fixture: 'create-order-response.json' });
  }
  // If mockApi is false — real API is used, no interception
});

// cypress/e2e/orders.cy.js
beforeEach(() => {
  cy.setupApiMode();
  cy.loginViaApi(Cypress.env('email'), Cypress.env('password'));
  cy.visit('/orders');
});
```

Run with real API:
```bash
npx cypress run                          # mockApi=false by default
```

Run with mocked API (faster, offline, deterministic):
```bash
npx cypress run --env mockApi=true
```

Real-world context: This pattern is common in teams where E2E tests run against a real staging environment in nightly CI, but developers run them locally with mocked APIs for instant feedback without needing the backend running.

---

**Q48: How do you test a file upload in Cypress?**

**A:**

```javascript
it('uploads a profile picture', () => {
  cy.visit('/profile/edit');

  // Method 1: selectFile with fixture
  cy.get('[data-cy="file-upload-input"]').selectFile('cypress/fixtures/profile-pic.jpg');

  // Method 2: selectFile with drag-and-drop action
  cy.get('[data-cy="drop-zone"]').selectFile('cypress/fixtures/document.pdf', {
    action: 'drag-drop'
  });

  // Method 3: selectFile with multiple files
  cy.get('[data-cy="multi-upload"]').selectFile([
    'cypress/fixtures/image1.jpg',
    'cypress/fixtures/image2.png'
  ]);

  // Verify upload succeeded
  cy.get('[data-cy="upload-success"]').should('be.visible');
  cy.get('[data-cy="file-name"]').should('contain', 'profile-pic.jpg');
});
```

Note: `cy.selectFile()` was introduced in Cypress 9.3 and replaces the older workaround using `cy.get('input').attachFile()` from the `cypress-file-upload` plugin.

---

**Q49: How do you test a drag-and-drop interaction?**

**A:**

```javascript
// Method 1: Using trigger() for HTML5 drag events
it('drags card to another column', () => {
  cy.get('[data-cy="card-1"]').trigger('dragstart');
  cy.get('[data-cy="done-column"]').trigger('drop');
  cy.get('[data-cy="done-column"]').find('[data-cy="card-1"]').should('exist');
});

// Method 2: Using cypress-drag-drop plugin (recommended for complex DnD)
// npm install --save-dev @4tw/cypress-drag-drop
import '@4tw/cypress-drag-drop';

cy.get('[data-cy="card-1"]').drag('[data-cy="done-column"]');

// Method 3: Using realMouse from cypress-real-events plugin
// npm install --save-dev cypress-real-events
import 'cypress-real-events';

cy.get('[data-cy="card"]').realMouseDown();
cy.get('[data-cy="target"]').realMouseMove(0, 0, { position: 'center' });
cy.get('[data-cy="target"]').realMouseUp();
```

---

**Q50: How do you test localStorage and cookies in Cypress?**

**A:**

```javascript
// Reading localStorage
cy.window().then((win) => {
  const token = win.localStorage.getItem('authToken');
  expect(token).to.not.be.null;
  expect(token).to.include('Bearer');
});

// Setting localStorage (in onBeforeLoad or via cy.window())
cy.window().then((win) => {
  win.localStorage.setItem('theme', 'dark');
});

// Clearing localStorage
cy.clearLocalStorage();
cy.clearLocalStorage('authToken');  // clear specific key

// Cookies
cy.getCookie('sessionId').should('exist');
cy.getCookie('sessionId').its('value').should('include', 'sess-');

cy.setCookie('testMode', 'true');

cy.clearCookies();              // clear all cookies
cy.clearCookie('sessionId');    // clear specific cookie

// Preserve cookies across tests (avoid clearing specific ones)
Cypress.Cookies.preserveOnce('sessionId', 'csrfToken');

// Assert on multiple cookies
cy.getCookies().then((cookies) => {
  expect(cookies).to.have.length.greaterThan(0);
  const sessionCookie = cookies.find(c => c.name === 'sessionId');
  expect(sessionCookie).to.exist;
  expect(sessionCookie.httpOnly).to.be.true;
  expect(sessionCookie.secure).to.be.true;
});
```

---

*End of Cypress Complete Q&A Interview Guide*
