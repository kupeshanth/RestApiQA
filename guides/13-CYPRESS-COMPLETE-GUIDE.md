# Cypress — Complete Guide | JavaScript + E2E + API Testing

> Senior QA Engineer Interview Reference — Cypress Architecture, Commands, Patterns, and Best Practices

---

## Table of Contents

1. [What is Cypress and How It Differs from Selenium](#1-what-is-cypress-and-how-it-differs-from-selenium)
2. [Setup and Project Structure](#2-setup-and-project-structure)
3. [cypress.config.js — Full Configuration](#3-cypressconfigjs--full-configuration)
4. [Core Concepts](#4-core-concepts)
5. [Locators and Selectors](#5-locators-and-selectors)
6. [Actions](#6-actions)
7. [Assertions](#7-assertions)
8. [Waiting Strategies](#8-waiting-strategies)
9. [Network Interception with cy.intercept()](#9-network-interception-with-cyintercept)
10. [API Testing with cy.request()](#10-api-testing-with-cyrequest)
11. [Fixtures — External Test Data](#11-fixtures--external-test-data)
12. [Custom Commands](#12-custom-commands)
13. [Page Object Model in Cypress](#13-page-object-model-in-cypress)
14. [Hooks and Test Organisation](#14-hooks-and-test-organisation)
15. [Environment Variables](#15-environment-variables)
16. [Cross-Browser Testing](#16-cross-browser-testing)
17. [CLI Commands Reference](#17-cli-commands-reference)
18. [Cypress vs Playwright vs Selenium — Comparison Table](#18-cypress-vs-playwright-vs-selenium--comparison-table)
19. [Troubleshooting Common Issues](#19-troubleshooting-common-issues)
20. [Senior Interview Q&A — 10 Questions with Full Answers](#20-senior-interview-qa--10-questions-with-full-answers)

---

## 1. What is Cypress and How It Differs from Selenium

### What is Cypress?

Cypress is a modern, JavaScript-first end-to-end testing framework designed specifically for web applications. It runs directly inside the browser — not through a WebDriver bridge — which gives it unique advantages in speed, reliability, and developer experience.

Released in 2017, Cypress is built on top of Mocha (test runner), Chai (assertions), Sinon (spies/stubs), and jQuery-style DOM querying. It is entirely JavaScript/TypeScript and does not support other languages natively.

---

### Architecture Comparison: Cypress vs Selenium

#### Selenium Architecture

```
Your Test Code
     |
WebDriver Protocol (HTTP JSON Wire Protocol)
     |
Browser Driver (ChromeDriver / GeckoDriver / EdgeDriver)
     |
Browser (Chrome / Firefox / Edge)
```

- Selenium operates **outside** the browser, communicating through HTTP requests via the WebDriver protocol.
- This adds latency and can introduce flakiness, because the test code and browser are in separate processes.
- Selenium supports many languages: Java, Python, C#, Ruby, JavaScript.

#### Cypress Architecture

```
Your Test Code
     |
Node.js Process (Cypress backend — file system, network, config)
     |
Browser (Chrome / Firefox / Edge) — Cypress runs INSIDE the browser context
```

- Cypress test code runs **inside** the browser in the same run loop as the application.
- There is no WebDriver layer — Cypress controls the browser natively using browser automation APIs.
- Cypress has a separate Node.js process that handles file system access, task plugins, and communicating with the browser proxy.

---

### Key Differences at a Glance

| Feature | Cypress | Selenium |
|---|---|---|
| Architecture | Runs inside browser | Runs outside via WebDriver |
| Language | JavaScript / TypeScript only | Java, Python, C#, Ruby, JS |
| Automatic Waiting | Built-in (retries until timeout) | Manual waits required |
| Network Stubbing | Native cy.intercept() | Requires external proxy tools |
| Real-time Reload | Yes — Test Runner updates live | No |
| Parallel Execution | Paid Cypress Cloud (or manual) | Free with grid/hub |
| Multi-tab Support | Limited (experimental) | Full support |
| iFrame Support | Limited | Full |
| Cross-origin | Limited (same superdomain by default) | Full |
| Speed | Fast for single-origin apps | Moderate |
| Time Travel Debugging | Yes — snapshot at each command | No |
| Browser Support | Chrome, Firefox, Edge, Electron | All major browsers + IE |

---

### What "Runs Inside the Browser" Means

When Cypress runs your tests, it injects itself into the browser. This means:

1. **Direct DOM access** — No HTTP round-trips to query elements.
2. **Automatic waiting** — Cypress retries commands until the element appears or the timeout is reached. You almost never need `sleep()`.
3. **Network interception** — Cypress sits at the network layer of the browser, allowing native request stubbing.
4. **Time-travel snapshots** — Every command captures a DOM snapshot, visible in the Cypress Test Runner.
5. **Native browser events** — Click, type, and other actions use real browser events, not simulated ones.

---

## 2. Setup and Project Structure

### Installation

```bash
# Using npm (save as devDependency)
npm install cypress --save-dev

# Using yarn
yarn add cypress --dev

# Open Cypress for the first time (creates folder structure)
npx cypress open
```

After running `npx cypress open`, Cypress automatically creates the following folder structure:

---

### Default Folder Structure

```
project-root/
├── cypress/
│   ├── e2e/                    # Test files (.cy.js or .cy.ts)
│   │   ├── login.cy.js
│   │   ├── checkout.cy.js
│   │   └── api/
│   │       └── users.cy.js
│   ├── fixtures/               # Static test data (JSON files)
│   │   ├── user.json
│   │   └── products.json
│   ├── support/                # Support files loaded before every test
│   │   ├── commands.js         # Custom commands go here
│   │   ├── e2e.js              # Imported before every e2e spec
│   │   └── pages/              # Page Object classes (if using POM)
│   │       ├── LoginPage.js
│   │       └── DashboardPage.js
│   └── screenshots/            # Auto-generated on failure
│   └── videos/                 # Auto-generated during cy run
├── cypress.config.js           # Cypress configuration file
├── package.json
└── node_modules/
```

---

### File Roles

| File/Folder | Purpose |
|---|---|
| `cypress/e2e/` | All test specs live here |
| `cypress/fixtures/` | JSON data files (test inputs, mocked responses) |
| `cypress/support/commands.js` | Custom Cypress commands (e.g., cy.login()) |
| `cypress/support/e2e.js` | Runs before every test file — import plugins, set globals |
| `cypress/pages/` | Page Object Model classes (not auto-created, you add it) |
| `cypress.config.js` | Main configuration — baseUrl, timeouts, env vars |

---

## 3. cypress.config.js — Full Configuration

```javascript
// cypress.config.js
const { defineConfig } = require('cypress');

module.exports = defineConfig({
  // ─── E2E Configuration ──────────────────────────────────────────────────
  e2e: {
    // Base URL — cy.visit('/login') will navigate to http://localhost:3000/login
    baseUrl: 'http://localhost:3000',

    // Where Cypress looks for spec files
    specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',

    // Support file loaded before every test
    supportFile: 'cypress/support/e2e.js',

    // Where fixtures are stored
    fixturesFolder: 'cypress/fixtures',

    // Setup and teardown node events (plugins)
    setupNodeEvents(on, config) {
      // Example: code coverage plugin
      // require('@cypress/code-coverage/task')(on, config);

      // Task example — run custom Node.js code
      on('task', {
        log(message) {
          console.log(message);
          return null;
        },
        queryDatabase({ query }) {
          // connect to DB and run query
          return null;
        }
      });

      return config;
    },
  },

  // ─── Viewport ───────────────────────────────────────────────────────────
  viewportWidth: 1280,
  viewportHeight: 720,

  // ─── Timeouts (milliseconds) ─────────────────────────────────────────────
  defaultCommandTimeout: 10000,   // How long to retry .get(), .find(), etc.
  requestTimeout: 15000,          // cy.request() and cy.intercept() wait time
  responseTimeout: 30000,         // Wait for server response
  pageLoadTimeout: 60000,         // cy.visit() page load timeout
  execTimeout: 60000,             // cy.exec() command timeout

  // ─── Test Behaviour ──────────────────────────────────────────────────────
  retries: {
    runMode: 2,    // Retry failing tests this many times in CI (npx cypress run)
    openMode: 0,   // No retries in interactive mode (npx cypress open)
  },

  // Stop after N failing tests (useful in CI)
  // bail: 1,

  // ─── Screenshots and Video ───────────────────────────────────────────────
  screenshotOnRunFailure: true,
  video: true,
  videosFolder: 'cypress/videos',
  screenshotsFolder: 'cypress/screenshots',
  trashAssetsBeforeRuns: true,   // Clear old screenshots/videos before each run

  // ─── Environment Variables ───────────────────────────────────────────────
  env: {
    apiUrl: 'https://api.myapp.com',
    adminEmail: 'admin@example.com',
    adminPassword: 'Admin@123',
    featureFlag_newCheckout: true,
  },

  // ─── Reporter ────────────────────────────────────────────────────────────
  reporter: 'mochawesome',
  reporterOptions: {
    reportDir: 'cypress/reports',
    overwrite: false,
    html: true,
    json: true,
  },

  // ─── Experimental Features ───────────────────────────────────────────────
  experimentalStudio: false,        // Record and generate tests in browser
  experimentalWebKitSupport: false, // Safari support (experimental)
});
```

---

## 4. Core Concepts

### cy.visit()

Navigates to a URL. If `baseUrl` is set in config, relative paths work.

```javascript
cy.visit('/')                          // visits baseUrl
cy.visit('/login')                     // visits baseUrl + /login
cy.visit('https://example.com')        // absolute URL
cy.visit('/login', {
  method: 'POST',
  body: { user: 'admin' }
})
```

---

### cy.get()

Queries the DOM. Returns a chainable "subject" (jQuery-like wrapper).

```javascript
cy.get('#username')            // by ID
cy.get('.btn-primary')         // by class
cy.get('[data-cy="submit"]')   // by data attribute (recommended)
cy.get('input[type="email"]')  // by attribute + tag
cy.get('form > input')         // CSS selector
```

---

### cy.contains()

Finds an element by its visible text content.

```javascript
cy.contains('Login')                    // finds first element with "Login" text
cy.contains('button', 'Submit')         // finds a button with "Submit" text
cy.contains('.nav-item', 'Dashboard')   // scoped to selector
```

---

### cy.find()

Queries **within** the current subject (scoped search).

```javascript
cy.get('.user-card').find('button')       // find button inside .user-card
cy.get('form').find('[data-cy="email"]')  // find email input inside form
```

---

### The Subject / Chaining Concept

Every Cypress command returns a "subject" (the DOM element, value, or result). Commands are chained on that subject:

```javascript
cy.get('[data-cy="email"]')   // subject = input element
  .type('user@example.com')   // still subject = input element
  .should('have.value', 'user@example.com') // assert on subject

cy.get('ul')                  // subject = ul
  .find('li')                 // subject = all li elements
  .first()                    // subject = first li
  .should('contain', 'Item 1')
```

Cypress commands are **asynchronous** and **queued**. You cannot use `const el = cy.get(...)` like a synchronous variable — use `.then()` if you need to extract values:

```javascript
cy.get('[data-cy="price"]').invoke('text').then((text) => {
  const price = parseFloat(text.replace('$', ''));
  expect(price).to.be.greaterThan(0);
});
```

---

## 5. Locators and Selectors

### Selector Priority (Best to Worst)

| Priority | Selector Type | Example | Why |
|---|---|---|---|
| 1 (Best) | data-cy attribute | `[data-cy="login-btn"]` | Test-specific, never changes with styling |
| 2 | data-testid | `[data-testid="submit"]` | Also test-specific |
| 3 | ARIA role | `cy.get('[role="button"]')` | Accessibility-aligned |
| 4 | ID | `#username` | Stable but sometimes dynamic |
| 5 | Name attribute | `[name="email"]` | Stable for form inputs |
| 6 | CSS class | `.btn-primary` | Fragile — changes with styling |
| 7 (Worst) | XPath (via plugin) | — | Verbose, brittle |

---

### Selector Examples

```javascript
// data-cy attribute (recommended approach)
cy.get('[data-cy="login-button"]')
cy.get('[data-cy="user-email"]')
cy.get('[data-cy="product-card"]').first()

// By ID
cy.get('#username')
cy.get('#login-form')

// By class (avoid for test-critical elements)
cy.get('.btn.btn-primary')

// By text (use for links and buttons where text is stable)
cy.contains('Sign In')
cy.contains('button', 'Add to Cart')

// By attribute
cy.get('input[type="password"]')
cy.get('a[href="/dashboard"]')

// Combining selectors
cy.get('table tbody tr').eq(0).find('td').eq(2)  // 3rd cell of first row
```

---

### Adding data-cy Attributes in HTML

```html
<!-- Add to your application code -->
<button data-cy="submit-btn">Submit</button>
<input data-cy="email-input" type="email" />
<div data-cy="user-card">...</div>
```

This keeps selectors resilient to CSS/HTML refactors.

---

## 6. Actions

### Typing and Clearing

```javascript
cy.get('[data-cy="email"]').type('user@example.com')
cy.get('[data-cy="email"]').type('user@example.com', { delay: 50 }) // type slowly
cy.get('[data-cy="email"]').clear()                  // clear existing value
cy.get('[data-cy="email"]').clear().type('new@email.com')

// Special key sequences
cy.get('[data-cy="search"]').type('hello{enter}')    // press Enter
cy.get('[data-cy="input"]').type('{selectAll}{del}') // select all and delete
cy.get('[data-cy="input"]').type('{ctrl}a{del}')     // ctrl+A then delete
```

### Clicking

```javascript
cy.get('[data-cy="submit"]').click()
cy.get('[data-cy="submit"]').click({ force: true })   // force click even if covered
cy.get('[data-cy="btn"]').dblclick()                  // double click
cy.get('[data-cy="btn"]').rightclick()                // right click (context menu)

// Click by position
cy.get('[data-cy="canvas"]').click('topLeft')
cy.get('[data-cy="canvas"]').click('center')
cy.get('[data-cy="canvas"]').click(150, 75)           // click at x=150, y=75
```

### Checkboxes and Radio Buttons

```javascript
cy.get('[data-cy="terms"]').check()          // check a checkbox
cy.get('[data-cy="terms"]').uncheck()        // uncheck
cy.get('[value="male"]').check()             // check radio by value
cy.get('[data-cy="options"]').check(['a', 'b']) // check multiple
cy.get('[data-cy="terms"]').should('be.checked')
```

### Dropdowns / Select

```javascript
cy.get('[data-cy="country"]').select('United States')  // select by visible text
cy.get('[data-cy="country"]').select('US')             // select by value
cy.get('[data-cy="country"]').select(2)                // select by index
cy.get('[data-cy="size"]').should('have.value', 'large')
```

### Scrolling

```javascript
cy.scrollTo('bottom')                       // scroll page to bottom
cy.scrollTo('top')                          // scroll page to top
cy.scrollTo(0, 500)                         // scroll to x=0, y=500

cy.get('[data-cy="lazy-image"]').scrollIntoView()   // scroll element into viewport
cy.get('[data-cy="list"]').scrollTo('bottom')       // scroll within element
```

### Triggering Events

```javascript
cy.get('[data-cy="slider"]').trigger('mousedown')
cy.get('[data-cy="slider"]').trigger('mousemove', { clientX: 200 })
cy.get('[data-cy="hover-target"]').trigger('mouseover')
cy.get('[data-cy="input"]').trigger('change')
cy.get('[data-cy="input"]').trigger('keydown', { key: 'Escape' })
```

---

## 7. Assertions

Cypress uses Chai assertions. They are attached via `.should()` or `expect()`.

### .should() — Chainable Assertions

```javascript
// Visibility
cy.get('[data-cy="modal"]').should('be.visible')
cy.get('[data-cy="modal"]').should('not.be.visible')
cy.get('[data-cy="spinner"]').should('not.exist')     // element not in DOM
cy.get('[data-cy="error"]').should('exist')

// Text content
cy.get('[data-cy="title"]').should('have.text', 'Welcome to the App')
cy.get('[data-cy="message"]').should('contain', 'success')   // partial match
cy.get('[data-cy="title"]').should('not.contain', 'Error')

// Form values
cy.get('[data-cy="email"]').should('have.value', 'user@example.com')
cy.get('[data-cy="submit"]').should('be.disabled')
cy.get('[data-cy="submit"]').should('not.be.disabled')
cy.get('[data-cy="terms"]').should('be.checked')

// CSS and attributes
cy.get('[data-cy="btn"]').should('have.class', 'active')
cy.get('[data-cy="link"]').should('have.attr', 'href', '/dashboard')
cy.get('[data-cy="img"]').should('have.attr', 'src').and('include', 'logo')
cy.get('[data-cy="input"]').should('have.css', 'color', 'rgb(255, 0, 0)')

// Collection length
cy.get('[data-cy="product-card"]').should('have.length', 5)
cy.get('[data-cy="product-card"]').should('have.length.greaterThan', 0)
cy.get('[data-cy="product-card"]').should('have.length.lessThan', 10)

// Multiple assertions (chained)
cy.get('[data-cy="username"]')
  .should('be.visible')
  .and('have.value', 'admin')
  .and('not.be.disabled')

// URL assertions
cy.url().should('include', '/dashboard')
cy.url().should('eq', 'http://localhost:3000/login')

// Title
cy.title().should('eq', 'My Application')
```

### expect() — BDD-style Assertions

Used inside `.then()` blocks when you have extracted a value:

```javascript
cy.get('[data-cy="price"]').invoke('text').then((text) => {
  expect(text).to.equal('$29.99');
  expect(parseFloat(text.replace('$', ''))).to.be.greaterThan(0);
});

cy.request('/api/users').then((response) => {
  expect(response.status).to.equal(200);
  expect(response.body).to.have.property('users');
  expect(response.body.users).to.have.length.greaterThan(0);
});
```

### assert Syntax (TDD style)

```javascript
assert.equal(actual, expected, 'message');
assert.isTrue(value);
assert.include(array, item);
```

---

## 8. Waiting Strategies

### Automatic Waiting (The Cypress Difference)

Cypress automatically retries most commands until they succeed or the `defaultCommandTimeout` is reached. You almost never need `cy.wait(1000)`.

```javascript
// Cypress retries cy.get() every 50ms until element appears or timeout
cy.get('[data-cy="success-toast"]').should('be.visible')

// Cypress retries until text appears
cy.get('[data-cy="result"]').should('contain', 'Order Confirmed')
```

**Do NOT use hard waits** (`cy.wait(2000)`) — they make tests slow and fragile.

---

### cy.wait('@alias') — Wait for Network Request

This is the correct way to wait for an API call to complete:

```javascript
// 1. Intercept and alias the request
cy.intercept('POST', '/api/login').as('loginRequest')

// 2. Trigger the action
cy.get('[data-cy="submit"]').click()

// 3. Wait for the request to complete
cy.wait('@loginRequest')

// 4. Now assert on the UI (API call is done)
cy.url().should('include', '/dashboard')

// You can also assert on the request/response:
cy.wait('@loginRequest').then(({ request, response }) => {
  expect(request.body).to.have.property('email');
  expect(response.statusCode).to.equal(200);
});
```

---

### cy.intercept() with Timing

```javascript
cy.intercept('GET', '/api/slow-endpoint').as('slowRequest')
cy.visit('/page-with-data')
cy.wait('@slowRequest', { timeout: 30000 })  // override timeout for this wait
cy.get('[data-cy="data-table"]').should('be.visible')
```

---

### cy.waitUntil() — Plugin

Requires `cypress-wait-until` package:

```bash
npm install cypress-wait-until --save-dev
```

```javascript
// Import in support/e2e.js
import 'cypress-wait-until';

// Usage
cy.waitUntil(() => cy.window().then(win => win.someGlobalState === 'ready'));
cy.waitUntil(() => Cypress.$('[data-cy="element"]').length > 0);
```

---

## 9. Network Interception with cy.intercept()

### Spying on Requests (Observe without Stubbing)

```javascript
cy.intercept('GET', '/api/users').as('getUsers')
cy.visit('/users')
cy.wait('@getUsers').its('response.statusCode').should('eq', 200)
```

### Stubbing Responses (Mock API)

```javascript
// Return a fixed response — no real API call is made
cy.intercept('GET', '/api/users', {
  statusCode: 200,
  body: [
    { id: 1, name: 'Alice', email: 'alice@example.com' },
    { id: 2, name: 'Bob', email: 'bob@example.com' },
  ]
}).as('mockUsers')

cy.visit('/users')
cy.wait('@mockUsers')
cy.get('[data-cy="user-row"]').should('have.length', 2)
```

### Using Fixture Files as Response

```javascript
cy.intercept('GET', '/api/products', { fixture: 'products.json' }).as('getProducts')
```

### Stubbing with Dynamic Responses

```javascript
cy.intercept('POST', '/api/orders', (req) => {
  req.reply({
    statusCode: 201,
    body: { orderId: 'ORD-999', status: 'confirmed' }
  });
}).as('createOrder')
```

### Simulating Errors / Network Failures

```javascript
// 500 error
cy.intercept('GET', '/api/data', { statusCode: 500, body: 'Server Error' })

// Network timeout / failure
cy.intercept('GET', '/api/data', { forceNetworkError: true })

// Slow response
cy.intercept('GET', '/api/data', (req) => {
  req.reply((res) => {
    res.delay(3000);  // delay response by 3 seconds
    res.send({ status: 200, body: {} });
  });
})
```

### Intercepting with URL Pattern / RegEx

```javascript
cy.intercept('GET', '/api/users/*').as('getUser')        // wildcard
cy.intercept('GET', /\/api\/users\/\d+/).as('getUser')   // regex
cy.intercept({ method: 'POST', url: '/api/**' }).as('anyPost')
```

### Modifying Real Responses (Spy + Transform)

```javascript
cy.intercept('GET', '/api/users', (req) => {
  req.continue((res) => {
    // Modify the real response before it reaches the app
    res.body.push({ id: 99, name: 'Injected User' });
  });
})
```

---

## 10. API Testing with cy.request()

Cypress can test REST APIs directly, without browser interaction.

### GET Request

```javascript
cy.request('GET', '/api/users').then((response) => {
  expect(response.status).to.equal(200);
  expect(response.body).to.be.an('array');
  expect(response.body).to.have.length.greaterThan(0);
  expect(response.body[0]).to.have.property('email');
});

// Shorthand
cy.request('/api/users').its('status').should('eq', 200)
```

### POST Request

```javascript
cy.request({
  method: 'POST',
  url: '/api/users',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + Cypress.env('authToken')
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
```

### PUT Request

```javascript
cy.request({
  method: 'PUT',
  url: '/api/users/1',
  body: { name: 'Updated Name' }
}).then((response) => {
  expect(response.status).to.equal(200);
  expect(response.body.name).to.equal('Updated Name');
});
```

### DELETE Request

```javascript
cy.request({
  method: 'DELETE',
  url: '/api/users/1'
}).then((response) => {
  expect(response.status).to.equal(204);
});
```

### Full API Test Spec Example

```javascript
// cypress/e2e/api/users.cy.js
describe('Users API', () => {
  let userId;

  it('POST /api/users — creates a new user', () => {
    cy.request({
      method: 'POST',
      url: `${Cypress.env('apiUrl')}/users`,
      body: { name: 'QA User', email: 'qa@test.com' }
    }).then((res) => {
      expect(res.status).to.eq(201);
      userId = res.body.id;
    });
  });

  it('GET /api/users/:id — returns the created user', () => {
    cy.request(`${Cypress.env('apiUrl')}/users/${userId}`).then((res) => {
      expect(res.status).to.eq(200);
      expect(res.body.email).to.eq('qa@test.com');
    });
  });

  it('DELETE /api/users/:id — deletes the user', () => {
    cy.request({ method: 'DELETE', url: `${Cypress.env('apiUrl')}/users/${userId}` })
      .its('status').should('eq', 204);
  });
});
```

---

## 11. Fixtures — External Test Data

Fixtures are static JSON files stored in `cypress/fixtures/`. They keep test data out of test code.

### Creating a Fixture File

```json
// cypress/fixtures/user.json
{
  "validUser": {
    "email": "admin@example.com",
    "password": "Admin@123",
    "role": "admin"
  },
  "invalidUser": {
    "email": "wrong@example.com",
    "password": "wrongpass"
  }
}
```

### Loading Fixtures in Tests

```javascript
// Method 1: cy.fixture() — load and use in test
cy.fixture('user').then((userData) => {
  cy.get('[data-cy="email"]').type(userData.validUser.email);
  cy.get('[data-cy="password"]').type(userData.validUser.password);
});

// Method 2: beforeEach with this (using function() not arrow)
describe('Login Tests', function () {
  beforeEach(function () {
    cy.fixture('user').as('userData');
  });

  it('logs in with valid credentials', function () {
    cy.get('[data-cy="email"]').type(this.userData.validUser.email);
    cy.get('[data-cy="password"]').type(this.userData.validUser.password);
    cy.get('[data-cy="submit"]').click();
  });
});

// Method 3: As API stub response
cy.intercept('GET', '/api/users', { fixture: 'users.json' });
```

---

## 12. Custom Commands

Add reusable commands to `cypress/support/commands.js`. They become available as `cy.commandName()`.

### Login Command Example

```javascript
// cypress/support/commands.js

// UI Login Command
Cypress.Commands.add('login', (email, password) => {
  cy.get('[data-cy="email"]').clear().type(email);
  cy.get('[data-cy="password"]').clear().type(password);
  cy.get('[data-cy="submit"]').click();
  cy.url().should('include', '/dashboard');
});

// API Login Command (faster — bypasses UI)
Cypress.Commands.add('loginViaApi', (email, password) => {
  cy.request({
    method: 'POST',
    url: `${Cypress.env('apiUrl')}/auth/login`,
    body: { email, password }
  }).then((response) => {
    window.localStorage.setItem('authToken', response.body.token);
  });
  cy.visit('/dashboard');
});

// Set token in localStorage (avoid login UI on every test)
Cypress.Commands.add('setAuthToken', (token) => {
  window.localStorage.setItem('authToken', token);
});

// Overwrite existing command
Cypress.Commands.overwrite('visit', (originalFn, url, options) => {
  const combinedOptions = {
    ...options,
    onBeforeLoad(win) {
      win.localStorage.setItem('authToken', Cypress.env('authToken'));
    }
  };
  return originalFn(url, combinedOptions);
});
```

### Using Custom Commands

```javascript
// In any spec file
cy.login('admin@example.com', 'Admin@123');
cy.loginViaApi(Cypress.env('adminEmail'), Cypress.env('adminPassword'));
```

### Type Safety for Custom Commands (TypeScript)

```typescript
// cypress/support/index.d.ts
declare namespace Cypress {
  interface Chainable {
    login(email: string, password: string): Chainable<void>;
    loginViaApi(email: string, password: string): Chainable<void>;
  }
}
```

---

## 13. Page Object Model in Cypress

### Class-Based Approach

```javascript
// cypress/support/pages/LoginPage.js
class LoginPage {
  // Selectors
  get emailInput()    { return cy.get('[data-cy="email"]'); }
  get passwordInput() { return cy.get('[data-cy="password"]'); }
  get submitButton()  { return cy.get('[data-cy="submit"]'); }
  get errorMessage()  { return cy.get('[data-cy="error-message"]'); }

  // Actions
  navigate() {
    cy.visit('/login');
  }

  fillEmail(email) {
    this.emailInput.clear().type(email);
  }

  fillPassword(password) {
    this.passwordInput.clear().type(password);
  }

  submit() {
    this.submitButton.click();
  }

  login(email, password) {
    this.navigate();
    this.fillEmail(email);
    this.fillPassword(password);
    this.submit();
  }

  // Assertions
  assertErrorMessage(message) {
    this.errorMessage.should('be.visible').and('contain', message);
  }
}

export default new LoginPage();
```

```javascript
// cypress/e2e/login.cy.js
import LoginPage from '../support/pages/LoginPage';

describe('Login', () => {
  it('logs in with valid credentials', () => {
    LoginPage.login('admin@example.com', 'Admin@123');
    cy.url().should('include', '/dashboard');
  });

  it('shows error for invalid credentials', () => {
    LoginPage.navigate();
    LoginPage.fillEmail('wrong@example.com');
    LoginPage.fillPassword('wrongpass');
    LoginPage.submit();
    LoginPage.assertErrorMessage('Invalid credentials');
  });
});
```

### Module-Based Approach (Function style)

```javascript
// cypress/support/pages/dashboard.js
export const DashboardPage = {
  elements: {
    userMenu:      () => cy.get('[data-cy="user-menu"]'),
    logoutButton:  () => cy.get('[data-cy="logout"]'),
    pageTitle:     () => cy.get('[data-cy="page-title"]'),
  },

  navigate() {
    cy.visit('/dashboard');
  },

  logout() {
    this.elements.userMenu().click();
    this.elements.logoutButton().click();
  },

  assertTitle(title) {
    this.elements.pageTitle().should('have.text', title);
  }
};
```

---

## 14. Hooks and Test Organisation

```javascript
describe('Shopping Cart', () => {
  // Runs once before all tests in this describe block
  before(() => {
    cy.log('Setting up test suite');
    cy.task('seedDatabase');
  });

  // Runs once after all tests in this describe block
  after(() => {
    cy.task('cleanDatabase');
  });

  // Runs before each individual test
  beforeEach(() => {
    cy.loginViaApi(Cypress.env('adminEmail'), Cypress.env('adminPassword'));
    cy.visit('/cart');
  });

  // Runs after each individual test
  afterEach(() => {
    cy.clearLocalStorage();
    cy.clearCookies();
  });

  it('adds a product to cart', () => {
    cy.get('[data-cy="add-to-cart"]').first().click();
    cy.get('[data-cy="cart-count"]').should('have.text', '1');
  });

  it('removes a product from cart', () => {
    cy.get('[data-cy="remove-item"]').first().click();
    cy.get('[data-cy="cart-empty-message"]').should('be.visible');
  });

  // Nested describe
  describe('Checkout', () => {
    it('proceeds to checkout', () => {
      cy.get('[data-cy="checkout-btn"]').click();
      cy.url().should('include', '/checkout');
    });
  });
});
```

### Skip and Only

```javascript
it.skip('skipped test', () => { ... });         // skip this test
it.only('only run this test', () => { ... });   // only run this test
describe.skip('skip entire suite', () => { }); 
describe.only('focus suite', () => { });
```

---

## 15. Environment Variables

### Defining Environment Variables

```javascript
// cypress.config.js
env: {
  apiUrl: 'https://api.example.com',
  adminEmail: 'admin@example.com',
  authToken: 'Bearer abc123'
}
```

```json
// cypress.env.json (not committed to git — add to .gitignore)
{
  "adminPassword": "super-secret",
  "authToken": "Bearer xyz456"
}
```

### Accessing Environment Variables

```javascript
Cypress.env('apiUrl')          // 'https://api.example.com'
Cypress.env('adminEmail')      // 'admin@example.com'
```

### Passing via CLI

```bash
npx cypress run --env apiUrl=https://staging.api.com,adminEmail=test@test.com
```

### Environment-Specific Config

```bash
# Run against staging
npx cypress run --env environment=staging

# In config, read dynamically
setupNodeEvents(on, config) {
  const env = config.env.environment || 'dev';
  config.baseUrl = env === 'staging' ? 'https://staging.myapp.com' : 'http://localhost:3000';
  return config;
}
```

---

## 16. Cross-Browser Testing

```bash
# Chrome (default)
npx cypress run --browser chrome

# Firefox
npx cypress run --browser firefox

# Microsoft Edge
npx cypress run --browser edge

# Electron (default headless)
npx cypress run

# Chromium
npx cypress run --browser chromium
```

### Running Specific Spec in Specific Browser

```bash
npx cypress run --browser firefox --spec "cypress/e2e/login.cy.js"
```

---

## 17. CLI Commands Reference

### Open Cypress Test Runner (Interactive)

```bash
npx cypress open                                    # opens interactive UI
npx cypress open --browser chrome                   # open with Chrome
npx cypress open --project ./my-project             # specify project path
```

### Run Cypress Headlessly (CI mode)

```bash
npx cypress run                                           # run all specs, headless
npx cypress run --headed                                  # run with browser visible
npx cypress run --browser chrome                          # specify browser
npx cypress run --spec "cypress/e2e/login.cy.js"          # run one spec
npx cypress run --spec "cypress/e2e/api/*.cy.js"          # run all API specs
npx cypress run --spec "cypress/e2e/login.cy.js,cypress/e2e/signup.cy.js"  # multiple
npx cypress run --env apiUrl=https://staging.api.com      # pass env vars
npx cypress run --record --key YOUR_RECORD_KEY            # record to Cypress Cloud
npx cypress run --parallel --record --key KEY             # parallel execution
npx cypress run --reporter mochawesome                    # custom reporter
npx cypress run --config baseUrl=https://staging.com      # override config
npx cypress run --config-file cypress.staging.config.js   # different config file
```

### Other Commands

```bash
npx cypress info           # print browser and system info
npx cypress verify         # verify Cypress is installed correctly
npx cypress cache list     # list cached Cypress versions
npx cypress cache clear    # clear Cypress cache
```

---

## 18. Cypress vs Playwright vs Selenium — Comparison Table

| Feature | Cypress | Playwright | Selenium |
|---|---|---|---|
| **Language** | JavaScript / TypeScript | JavaScript, TypeScript, Python, Java, C# | Java, Python, C#, Ruby, JS |
| **Architecture** | Runs inside browser | Separate process via CDP | WebDriver protocol |
| **Auto-waiting** | Yes — built in | Yes — built in | No — manual waits |
| **Browser Support** | Chrome, Firefox, Edge, Electron | Chrome, Firefox, Safari, Edge | All major + IE |
| **Multi-tab** | Limited (experimental) | Full support | Full support |
| **Multi-origin** | Limited (same superdomain) | Full support | Full support |
| **iFrame Support** | Limited | Full support | Full support |
| **Mobile / Native** | No | Mobile web via device emulation | Limited |
| **Network Intercept** | cy.intercept() — native | route() — native | Needs BrowserMob Proxy |
| **Parallel Tests** | Paid Cypress Cloud or CI config | Free — built-in workers | Selenium Grid (free) |
| **Speed** | Fast for single-origin | Fast | Slower |
| **Setup Complexity** | Low | Low | Moderate |
| **Debugging** | Time-travel + live reloads | Trace viewer | Browser DevTools |
| **CI Integration** | Easy | Easy | Moderate |
| **Community** | Large | Growing fast | Very large (mature) |
| **Cost** | Free (paid Cloud optional) | Free | Free |
| **Best For** | JS teams, single-origin web apps | Multi-browser, multi-origin, complex apps | Enterprise Java teams |

### When to Choose Which

- **Cypress**: JavaScript project, single-domain app, fast feedback loop needed, team prefers simple setup.
- **Playwright**: Need cross-browser (including Safari), multi-tab, multi-origin, or need to test in Python/Java alongside JS.
- **Selenium**: Large enterprise, legacy system, non-JS team, or need Internet Explorer support.

---

## 19. Troubleshooting Common Issues

### Element Detached from DOM

**Symptom**: `CypressError: cy.click() failed because this element is detached from the DOM`

**Cause**: React/Angular re-rendered the component between `.get()` and `.click()`.

**Fix**: Re-query the element rather than storing the reference:
```javascript
// WRONG — element reference goes stale after re-render
const btn = cy.get('[data-cy="submit"]');
cy.get('[data-cy="input"]').type('something');
btn.click(); // stale

// CORRECT — re-query each time
cy.get('[data-cy="input"]').type('something');
cy.get('[data-cy="submit"]').click(); // fresh query
```

---

### Timeout Errors

**Symptom**: `Timed out retrying after 10000ms: Expected to find element...`

**Fixes**:
```javascript
// Increase timeout for this assertion
cy.get('[data-cy="element"]', { timeout: 20000 }).should('be.visible')

// Wait for the network call that populates the element
cy.intercept('GET', '/api/data').as('getData')
cy.wait('@getData')
cy.get('[data-cy="element"]').should('be.visible')

// Increase global timeout in config
defaultCommandTimeout: 15000
```

---

### Cross-Origin / Same-Origin Errors

**Symptom**: `CypressError: Cypress detected a cross-origin URL redirect`

**Fixes**:
```javascript
// Enable experimental cross-origin support in cypress.config.js
experimentalModifyObstructiveThirdPartyCode: true

// Or use cy.origin() for cross-origin navigation
cy.origin('https://otherdomain.com', () => {
  cy.get('[data-cy="element"]').click();
});
```

---

### iFrame Limitations

Cypress has limited native iframe support. Use `cypress-iframe` plugin:

```bash
npm install -D cypress-iframe
```

```javascript
import 'cypress-iframe';

cy.frameLoaded('[data-cy="my-iframe"]');
cy.iframe('[data-cy="my-iframe"]').find('[data-cy="submit"]').click();
```

---

### Flaky Tests

**Common causes**:
- Timing issues (hard `cy.wait(ms)` instead of waiting for events)
- Animations not complete
- Race conditions with API calls

**Fixes**:
- Replace `cy.wait(2000)` with `cy.wait('@apiAlias')`
- Add `.should('be.visible')` before interacting with elements
- Use `{ force: true }` carefully for elements behind animations
- Enable test retries:
  ```javascript
  retries: { runMode: 2, openMode: 0 }
  ```

---

### cy.type() Not Working on Numeric Input

```javascript
// Use force or type as string
cy.get('input[type="number"]').type('123', { force: true });
cy.get('input[type="number"]').invoke('val', 123).trigger('input');
```

---

## 20. Senior Interview Q&A — 10 Questions with Full Answers

---

**Q1: How does Cypress differ architecturally from Selenium, and what are the practical advantages?**

**A**: Selenium operates outside the browser using the WebDriver protocol — it sends HTTP commands to a browser driver (ChromeDriver, GeckoDriver) which then controls the browser. This introduces network latency and can cause timing issues.

Cypress runs inside the browser in the same execution context as the application. It injects itself into the browser and has direct access to the DOM, `window`, `document`, and network requests. This enables:
- **Automatic waiting**: Cypress retries commands until the element exists and passes assertions, eliminating manual `Thread.sleep()` calls.
- **Native network interception**: `cy.intercept()` intercepts requests at the browser level without needing external proxies.
- **Time-travel debugging**: Every command captures a DOM snapshot, visible in the Cypress Test Runner.
- **Real-time reloads**: The test runner reloads automatically when you save a spec file.

The trade-off is that Cypress only supports JavaScript/TypeScript and has limitations with cross-origin and multi-tab scenarios.

---

**Q2: What is automatic waiting in Cypress, and how does it work internally?**

**A**: Cypress has a built-in retry mechanism — every command that interacts with the DOM retries at ~50ms intervals until the element meets the expected condition or the `defaultCommandTimeout` (default: 4 seconds, configurable) is reached.

For example, `cy.get('[data-cy="button"]').should('be.visible')` will:
1. Query the DOM for the element every 50ms.
2. Check whether it is visible.
3. Pass as soon as the condition is true.
4. Fail with a timeout error if it never becomes true within the timeout.

This eliminates the need for `cy.wait(ms)` in most cases. The only time you should use `cy.wait('@alias')` is when waiting for a specific network request to complete — not for time-based waiting.

---

**Q3: What is cy.intercept() and how do you use it to stub and spy on API calls?**

**A**: `cy.intercept()` allows Cypress to listen to, spy on, and optionally stub HTTP requests made by the application.

**Spy (observe without mocking)**:
```javascript
cy.intercept('GET', '/api/users').as('getUsers')
cy.visit('/users-page')
cy.wait('@getUsers').its('response.statusCode').should('eq', 200)
```

**Stub (mock response)**:
```javascript
cy.intercept('GET', '/api/users', { fixture: 'users.json' }).as('mockUsers')
```

**Simulate errors**:
```javascript
cy.intercept('GET', '/api/data', { statusCode: 500 })
cy.intercept('GET', '/api/data', { forceNetworkError: true })
```

This is critical for:
- Testing error states without a real API returning errors.
- Running E2E tests without a backend (isolated frontend testing).
- Verifying that the app correctly handles specific API responses.
- Waiting for real API calls to complete before asserting on UI.

---

**Q4: What are the limitations of Cypress you should know before choosing it?**

**A**:
1. **JavaScript/TypeScript only** — No Java, Python, or C# support.
2. **Same-origin limitation** — Cross-origin navigation (e.g., OAuth redirect to another domain) is limited; `cy.origin()` helps but is experimental in some versions.
3. **Multi-tab testing** — Cypress cannot control multiple browser tabs natively.
4. **iFrame limitations** — Native iframe support is limited; requires plugins.
5. **No native mobile app testing** — Cypress tests web apps only; for React Native or native mobile, you need Detox, Appium, or XCTest.
6. **Parallel execution** — Free only with manual CI configuration; built-in parallelisation requires Cypress Cloud (paid).
7. **No real Safari support** — WebKit support is experimental.
8. **File downloads** — Handling downloads requires extra configuration.

---

**Q5: How do you handle authentication in Cypress tests without logging in through the UI on every test?**

**A**: Logging in through the UI on every test is slow and tests the login flow unnecessarily. Better approaches:

**1. API Login (set token in localStorage)**:
```javascript
Cypress.Commands.add('loginViaApi', () => {
  cy.request('POST', '/api/auth/login', {
    email: Cypress.env('email'),
    password: Cypress.env('password')
  }).then(({ body }) => {
    window.localStorage.setItem('authToken', body.token);
  });
});
```

**2. Programmatic session (Cypress `cy.session()`)**:
```javascript
// Caches session cookies/localStorage across tests
cy.session('admin', () => {
  cy.loginViaApi();
}, {
  validate() {
    cy.request('/api/profile').its('status').should('eq', 200);
  }
});
```

`cy.session()` caches and restores session state between tests, greatly reducing test duration.

---

**Q6: What is the Page Object Model in Cypress and when would you use it?**

**A**: Page Object Model (POM) is a design pattern that encapsulates the selectors and actions of a page in a separate class or module, so test specs focus on behaviour rather than implementation details.

Benefits:
- **Maintainability**: If a selector changes, you update one place (the page class), not every test.
- **Readability**: Tests read like user stories (`LoginPage.login(...)`).
- **Reusability**: Same page actions used across multiple specs.

In Cypress, POM is typically implemented as a JavaScript class with getter properties for selectors and methods for user actions. Unlike Selenium, you do not store element references (they go stale in Cypress too) — instead, selectors are defined as methods that call `cy.get()` fresh each time.

---

**Q7: How does cy.request() differ from cy.intercept()?**

**A**:
- `cy.request()` sends an **actual HTTP request from Cypress** (like a direct API call). Use it for testing REST APIs directly, setting up test data, or bypassing the UI for prerequisites.
- `cy.intercept()` **intercepts requests made by the application** in the browser. Use it to spy on what the app calls, stub responses, or wait for a specific API call to complete.

```javascript
// cy.request() — Cypress calls the API directly
cy.request('POST', '/api/users', { name: 'Test' }).then(res => {
  expect(res.status).to.eq(201);
});

// cy.intercept() — App makes the call, Cypress observes/stubs it
cy.intercept('GET', '/api/users', { fixture: 'users.json' });
cy.visit('/users');  // the app calls /api/users; Cypress intercepts it
```

---

**Q8: How do you run Cypress tests in CI (e.g., GitHub Actions or Jenkins)?**

**A**: Run Cypress in headless (non-GUI) mode using `npx cypress run`. A typical GitHub Actions config:

```yaml
- name: Run Cypress Tests
  run: npx cypress run --browser chrome --spec "cypress/e2e/**/*.cy.js"
  env:
    CYPRESS_BASE_URL: https://staging.myapp.com
    CYPRESS_adminEmail: ${{ secrets.ADMIN_EMAIL }}
```

Key considerations:
- Use `npx cypress run` (not `open`) — no GUI needed in CI.
- Pass secrets via environment variables (prefix with `CYPRESS_` for automatic pickup).
- Use test retries (`retries: { runMode: 2 }`) to handle transient failures.
- Store artifacts: screenshots and videos from failed runs.
- Use Cypress Cloud (`--record`) for parallel execution and advanced analytics.

---

**Q9: How do you debug a failing Cypress test?**

**A**:
1. **Time-travel debugging** — In Cypress Test Runner, click any command in the left panel to see the DOM snapshot at that moment.
2. **`cy.pause()`** — Pauses execution so you can inspect the page manually.
3. **`cy.debug()`** — Opens browser DevTools at that point in the chain.
4. **`.then(console.log)`** — Log subject value to console.
5. **`cy.log('message')`** — Print to Cypress command log.
6. **Screenshots and videos** — Automatically generated on failure during `cypress run`.
7. **Browser DevTools** — Press F12 in Cypress Test Runner to open DevTools.
8. **Check network tab** — Use `cy.intercept` to log what the app is actually sending/receiving.
9. **`--headed` flag** — Run with browser visible during debugging: `npx cypress run --headed --no-exit`.

---

**Q10: What is cy.session() and why is it useful?**

**A**: `cy.session()` (introduced in Cypress 9+) caches and restores browser session state (cookies, localStorage, sessionStorage) between tests. Without it, every `beforeEach` login call performs the full login flow, which is slow.

```javascript
beforeEach(() => {
  cy.session('userSession', () => {
    cy.visit('/login');
    cy.get('[data-cy="email"]').type('admin@example.com');
    cy.get('[data-cy="password"]').type('Admin@123');
    cy.get('[data-cy="submit"]').click();
    cy.url().should('include', '/dashboard');
  }, {
    validate() {
      // Optional: verify session is still valid
      cy.getCookie('authCookie').should('exist');
    }
  });
});
```

On first run, it executes the login function and saves the session. On subsequent tests, it restores the saved session directly, skipping the login flow entirely. This can reduce suite runtime by 30–50% for auth-heavy test suites.

---

*End of Cypress Complete Guide*
