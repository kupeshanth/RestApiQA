# Playwright API Testing — Full Interview Q&A | Node.js + TypeScript

---

## SECTION 1: FUNDAMENTALS

**Q1: What is Playwright API testing and how is it different from Playwright UI testing?**

**A:** Playwright API testing uses Playwright's `APIRequestContext` to send HTTP requests directly to a REST API — no browser is launched at all. UI testing uses the `page` fixture which opens a real browser (Chromium, Firefox, or WebKit) and drives the rendered web application.

| Aspect | API Testing | UI Testing |
|---|---|---|
| Browser | Not needed | Required |
| Speed | Very fast (no rendering) | Slower (renders DOM) |
| Fixture | `request` | `page` |
| What it tests | HTTP responses, status codes, JSON payloads | User interface, visual layout, user journeys |
| Auth | Headers, tokens, cookies via code | Real browser session |

Real-world use case: API tests verify that your backend contract is correct (correct status codes, correct payload shape) before UI tests rely on those endpoints. You run API tests first in your CI pipeline because they are faster and cheaper to execute.

Common mistake: using Playwright API testing when you actually want to test the UI behaviour. If your requirement is "the login form shows an error message on bad credentials," that is a UI test, not an API test.

```typescript
// API test — no browser, uses request fixture
import { test, expect } from '@playwright/test';

test('GET /users returns 200 with array', async ({ request }) => {
  const response = await request.get('/users');
  expect(response.status()).toBe(200);
  const body = await response.json();
  expect(Array.isArray(body)).toBe(true);
});
```

---

**Q2: Why would a team choose Playwright for API testing instead of RestAssured, Postman, or Axios?**

**A:** The main advantage is a single unified toolchain. If your team already uses Playwright for UI tests, adopting it for API tests means:

- The same TypeScript test runner, `expect()` assertion library, and HTML reporter for both API and UI suites
- Shared authentication state between API and UI: log in via API, save the token, reuse it in a UI test session
- Playwright's `beforeAll`, `afterAll`, and custom fixtures work identically for API and UI tests
- No Java dependency (RestAssured requires JVM), no separate Postman license
- Richer assertions than RestAssured and better TypeScript type support than Axios-based setups
- Built-in retries and parallel execution with the same `playwright.config.ts`

Disadvantage vs RestAssured: RestAssured has more mature support for XML response bodies, complex authentication flows (OAuth2 PKCE), and report integration with Java-based CI stacks.

```typescript
// Compare: Playwright vs raw Axios
// Playwright — assertion is directly on the response object
const response = await request.post('/auth/login', { data: { username: 'u', password: 'p' } });
expect(response.status()).toBe(200);
expect(await response.json()).toMatchObject({ access_token: expect.any(String) });

// Axios requires you to wire assertions separately
const { data, status } = await axios.post('/auth/login', { username: 'u', password: 'p' });
expect(status).toBe(200);
expect(data.access_token).toBeDefined();
```

---

**Q3: How do you set up a Playwright project for API-only testing — no browsers needed?**

**A:** During `npm init playwright@latest`, choose TypeScript, set `tests` as the test directory, and answer **No** to installing browsers. For API-only testing you do not need browser binaries. Then install dotenv for environment variable support.

```bash
# Step 1: Create project
mkdir playwright-api-tests
cd playwright-api-tests
npm init -y

# Step 2: Install Playwright (no browsers)
npm init playwright@latest
# Prompts:
#   TypeScript? Yes
#   Test directory? tests
#   Add GitHub Actions? No (configure manually)
#   Install browsers? No  <--- critical for API-only

# Step 3: Extra dependencies
npm install --save-dev dotenv @types/node
```

Recommended project layout:

```
playwright-api-tests/
├── tests/
│   ├── api/
│   │   ├── users.spec.ts
│   │   ├── auth.spec.ts
│   │   └── products.spec.ts
│   └── fixtures/
│       └── test-data.json
├── utils/
│   ├── constants.ts
│   └── api-helpers.ts
├── playwright.config.ts
├── .env                  ← never commit
├── .env.example          ← commit this as documentation
├── package.json
└── tsconfig.json
```

```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "commonjs",
    "lib": ["ES2020"],
    "strict": true,
    "esModuleInterop": true,
    "outDir": "dist",
    "rootDir": ".",
    "resolveJsonModule": true
  },
  "include": ["tests/**/*.ts", "utils/**/*.ts", "playwright.config.ts"]
}
```

---

**Q4: Walk through every important option in playwright.config.ts for API testing.**

**A:** Here is a fully documented config file for a pure API testing project:

```typescript
// playwright.config.ts
import { defineConfig } from '@playwright/test';
import * as dotenv from 'dotenv';

dotenv.config(); // must be called before defineConfig so env vars are available

export default defineConfig({
  // Where Playwright looks for test files
  testDir: './tests',

  // Run all test files in parallel (default: serial within a file, parallel across files)
  fullyParallel: true,

  // Prevent accidental test.only from blocking the CI pipeline
  forbidOnly: !!process.env.CI,

  // Retry failed tests: 2 times on CI, never locally
  retries: process.env.CI ? 2 : 0,

  // Number of parallel worker processes
  workers: process.env.CI ? 4 : undefined, // undefined = CPU count locally

  // Reporter output formats
  reporter: [
    ['list'],                                                     // terminal output
    ['html', { outputFolder: 'playwright-report', open: 'never' }], // HTML report
    ['json', { outputFile: 'test-results/results.json' }],        // machine-readable
  ],

  // Maximum time a single test can take (milliseconds)
  timeout: 30000,

  // Maximum time a single expect() assertion can take
  expect: {
    timeout: 10000,
  },

  // Settings shared across all tests
  use: {
    // All relative URL paths resolve against this
    baseURL: process.env.BASE_URL || 'https://jsonplaceholder.typicode.com',

    // These headers are added to EVERY request automatically
    extraHTTPHeaders: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'x-api-version': 'v1',
    },

    // Trust self-signed certs (useful for dev/staging)
    ignoreHTTPSErrors: true,
  },

  // Named environments as separate projects
  projects: [
    {
      name: 'staging',
      use: {
        baseURL: process.env.STAGING_URL || 'https://staging.api.example.com',
        extraHTTPHeaders: {
          'Accept': 'application/json',
          'x-environment': 'staging',
        },
      },
    },
    {
      name: 'production',
      use: {
        baseURL: process.env.PROD_URL || 'https://api.example.com',
      },
    },
  ],
});
```

Key distinctions:
- `timeout` — per test. If exceeded, the test is marked as timed out.
- `expect.timeout` — per assertion. Playwright retries the assertion until this expires.
- `extraHTTPHeaders` — merged with per-request headers. Per-request headers override config headers when there is a conflict.

---

**Q5: What is baseURL in Playwright API config and how does it affect request.get('/users')?**

**A:** `baseURL` is a prefix that Playwright prepends to any path that begins with `/`. This means you write portable test paths without hardcoding the server address in every test.

```typescript
// playwright.config.ts
use: {
  baseURL: 'https://jsonplaceholder.typicode.com',
}

// In your test
const response = await request.get('/users');
// Actual URL called: https://jsonplaceholder.typicode.com/users

const response2 = await request.get('/users/1');
// Actual URL called: https://jsonplaceholder.typicode.com/users/1
```

If you pass a full URL (starting with `https://`), baseURL is ignored for that call:

```typescript
// baseURL is ignored — uses the full URL as-is
const response = await request.get('https://other-server.com/api/data');
```

Switching environments without changing tests:

```bash
# Run tests against staging
BASE_URL=https://staging.api.example.com npx playwright test

# Run tests against production
BASE_URL=https://api.example.com npx playwright test
```

---

**Q6: What is extraHTTPHeaders and how do you use it for global authentication?**

**A:** `extraHTTPHeaders` is a map of header name to value that Playwright sends with every request made through the `request` fixture or any `APIRequestContext` derived from the config. It is the correct place to set an API key or Bearer token that all tests need.

```typescript
// playwright.config.ts — global Bearer token for all tests
use: {
  baseURL: 'https://api.example.com',
  extraHTTPHeaders: {
    'Authorization': `Bearer ${process.env.API_TOKEN}`,
    'x-api-key': process.env.API_KEY || '',
    'Accept': 'application/json',
    'Content-Type': 'application/json',
  },
},
```

You can still override headers per request:

```typescript
test('use a different token for this one test', async ({ request }) => {
  const response = await request.get('/admin/users', {
    headers: {
      // This overrides the Authorization from extraHTTPHeaders for this call only
      'Authorization': `Bearer ${process.env.ADMIN_TOKEN}`,
    },
  });
  expect(response.status()).toBe(200);
});
```

Common mistake: setting `extraHTTPHeaders` with a token that has an expiry. If the token expires during the test run, all tests will start failing with 401. Use a long-lived test token for CI, or refresh the token in a `beforeAll` fixture and inject it via `request.newContext()`.

---

**Q7: What is the `request` fixture in Playwright and how is it created?**

**A:** The `request` fixture is an `APIRequestContext` instance that Playwright automatically creates for each test. It inherits `baseURL` and `extraHTTPHeaders` from your config. You declare it as a parameter in your test function and Playwright injects it.

```typescript
import { test, expect, APIRequestContext, APIResponse } from '@playwright/test';

// `request` is automatically injected by Playwright
test('request fixture example', async ({ request }) => {
  // request is an APIRequestContext — you call .get(), .post(), .put(), etc.
  const response: APIResponse = await request.get('/users/1');

  // APIResponse exposes:
  console.log(response.status());         // 200
  console.log(response.ok());             // true (200-299)
  console.log(response.statusText());     // "OK"
  console.log(response.url());            // full URL that was called
  console.log(response.headers());        // all response headers as object
  const body = await response.json();     // parse body as JSON
  const text = await response.text();     // body as raw string
  const buffer = await response.body();   // body as Buffer
});
```

The `request` fixture is scoped to each test — a fresh context per test by default. This ensures test isolation (cookies, sessions do not leak between tests).

---

## SECTION 2: HTTP METHODS

**Q8: How do you make a GET request and assert the status code and response body?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('GET /users/1 — happy path assertions', async ({ request }) => {
  const response = await request.get('/users/1');

  // Assert status code exactly
  expect(response.status()).toBe(200);

  // Assert 200-299 range (success)
  expect(response.ok()).toBeTruthy();

  // Parse and assert body
  const user = await response.json();

  // Exact field value
  expect(user.id).toBe(1);

  // Type check
  expect(typeof user.name).toBe('string');
  expect(typeof user.email).toBe('string');

  // Contains substring (good for emails, URLs)
  expect(user.email).toContain('@');

  // Partial object match — the body can have extra fields
  expect(user).toMatchObject({
    id: 1,
    name: expect.any(String),
    email: expect.stringContaining('@'),
  });

  // Property existence
  expect(user).toHaveProperty('id');
  expect(user).toHaveProperty('address.city'); // nested path
});

test('GET /users — list response assertions', async ({ request }) => {
  const response = await request.get('/users');

  expect(response.status()).toBe(200);

  const users = await response.json();

  expect(Array.isArray(users)).toBe(true);
  expect(users.length).toBeGreaterThan(0);
  expect(users.length).toBeLessThanOrEqual(100);

  // Assert every item has required fields
  for (const user of users) {
    expect(user).toHaveProperty('id');
    expect(user).toHaveProperty('email');
    expect(typeof user.id).toBe('number');
  }
});

test('GET /users/99999 — 404 for non-existent resource', async ({ request }) => {
  const response = await request.get('/users/99999');
  expect(response.status()).toBe(404);
});
```

---

**Q9: How do you assert response headers in a Playwright API test?**

**A:** `response.headers()` returns a plain object of header name to value (all lowercase). Use `toContain`, `toMatch`, or `toBe` depending on the precision you need.

```typescript
import { test, expect } from '@playwright/test';

test('assert response headers', async ({ request }) => {
  const response = await request.get('/users');

  // Get all headers as an object
  const headers = response.headers();

  // Content-Type must include application/json
  expect(headers['content-type']).toContain('application/json');

  // Use regex for more flexible matching
  expect(headers['content-type']).toMatch(/application\/json/);

  // Check a header exists
  expect(headers).toHaveProperty('content-type');

  // Cache-Control header should be present
  expect(headers['cache-control']).toBeDefined();

  // Assert CORS header for public APIs
  expect(headers['access-control-allow-origin']).toBe('*');

  // Security headers
  expect(headers['x-content-type-options']).toBe('nosniff');

  // Assert a header is NOT present (e.g., no sensitive info leaked)
  expect(headers['x-internal-server-id']).toBeUndefined();
});
```

Real-world context: In a security-focused sprint, you might add assertions that responses include `Strict-Transport-Security`, `X-Frame-Options`, and `Content-Security-Policy` headers. These are non-functional requirements that API tests can verify automatically.

---

**Q10: How do you assert response time (performance SLA) in a Playwright API test?**

**A:** Playwright does not have a built-in timing assertion on the `APIResponse` object, so you measure it manually using `Date.now()` around the request call.

```typescript
import { test, expect } from '@playwright/test';

test('GET /users — response time under 2 seconds', async ({ request }) => {
  const startTime = Date.now();

  const response = await request.get('/users');

  const durationMs = Date.now() - startTime;

  // Assert the API responded within the SLA
  expect(response.status()).toBe(200);
  expect(durationMs).toBeLessThan(2000); // 2-second SLA
});

test('POST /data — slow endpoint has generous threshold', async ({ request }) => {
  const start = Date.now();

  const response = await request.post('/data/process', {
    data: { records: Array.from({ length: 1000 }, (_, i) => ({ id: i })) },
  });

  const elapsed = Date.now() - start;

  expect(response.ok()).toBeTruthy();
  expect(elapsed).toBeLessThan(10000); // 10-second SLA for bulk operations
  console.log(`Bulk process completed in ${elapsed}ms`);
});
```

Real-world context: Agree on SLA thresholds with the team and document them. Run these tests nightly against production to detect regressions before users notice. If you need p95/p99 latency testing, Playwright alone is insufficient — use k6 or Gatling for load testing. Playwright API performance tests are for basic regression checking only.

---

**Q11: How do you make a POST request with a JSON body and assert the 201 response?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('POST /posts — create resource returns 201', async ({ request }) => {
  const newPost = {
    title: 'My First Playwright Post',
    body: 'This content was submitted via Playwright API test.',
    userId: 1,
  };

  const response = await request.post('/posts', {
    data: newPost, // Playwright serialises this to JSON automatically
  });

  // Assert creation status
  expect(response.status()).toBe(201);

  const created = await response.json();

  // The server should echo back what was sent + assign an id
  expect(created.title).toBe(newPost.title);
  expect(created.body).toBe(newPost.body);
  expect(created.userId).toBe(newPost.userId);
  expect(created.id).toBeDefined();
  expect(typeof created.id).toBe('number');
});

test('POST /posts — 400 for missing required fields', async ({ request }) => {
  const response = await request.post('/posts', {
    data: {
      // Deliberately omit `title` which is required
      body: 'Some body without a title',
    },
  });

  // API may return 400 or 422 depending on framework
  expect([400, 422]).toContain(response.status());

  const error = await response.json();
  expect(error).toHaveProperty('error');
});

test('POST /users — 409 Conflict for duplicate email', async ({ request }) => {
  const existingUser = {
    email: 'existing@example.com',
    name: 'Already Exists',
  };

  const response = await request.post('/users', {
    data: existingUser,
  });

  expect(response.status()).toBe(409);

  const error = await response.json();
  expect(error.message).toMatch(/already exists|duplicate/i);
});
```

Common mistake: not asserting the exact 201 status. `response.ok()` returns true for both 200 and 201, so if you write `expect(response.ok()).toBeTruthy()` on a POST that should return 201, a 200 will also pass the test — and you will not detect if the API contract changed.

---

**Q12: What is the difference between PUT and PATCH, and how do you test each?**

**A:** PUT replaces the entire resource. The request body must include all required fields, even those that are not changing. PATCH applies a partial update — only the fields you send are modified; everything else remains as it was on the server.

```typescript
import { test, expect } from '@playwright/test';

// ── PUT — Full Replacement ────────────────────────────────────────────────────

test('PUT /posts/1 — full replacement with all fields', async ({ request }) => {
  const fullReplacement = {
    id: 1,
    title: 'Completely New Title',
    body: 'Completely new body content.',
    userId: 1,
    // Must include ALL fields — missing fields may be set to null
  };

  const response = await request.put('/posts/1', {
    data: fullReplacement,
  });

  expect(response.status()).toBe(200);

  const result = await response.json();
  expect(result.title).toBe('Completely New Title');
  expect(result.body).toBe('Completely new body content.');
  expect(result.userId).toBe(1);
});

test('PUT /posts/1 — 400 when required fields are missing', async ({ request }) => {
  // PUT with only one field — rest would be wiped or rejected
  const response = await request.put('/posts/1', {
    data: { title: 'Only title' }, // missing body and userId
  });

  // Well-designed APIs should reject incomplete PUT bodies
  expect([400, 422]).toContain(response.status());
});

// ── PATCH — Partial Update ────────────────────────────────────────────────────

test('PATCH /posts/1 — update only title, body unchanged', async ({ request }) => {
  const patch = { title: 'Only the Title Changed' };

  const response = await request.patch('/posts/1', {
    data: patch,
  });

  expect(response.status()).toBe(200);

  const result = await response.json();
  expect(result.title).toBe('Only the Title Changed');
  // Body field should still be present from the original resource
  expect(result.body).toBeDefined();
  expect(result.body).not.toBeNull();
});

test('PATCH /posts/99999 — 404 for non-existent resource', async ({ request }) => {
  const response = await request.patch('/posts/99999', {
    data: { title: 'Ghost patch' },
  });

  expect(response.status()).toBe(404);
});
```

---

**Q13: How do you test DELETE requests and handle the difference between 200 and 204 responses?**

**A:** DELETE can return 200 with a body (e.g., the deleted resource) or 204 No Content (no body). You must test for both possibilities unless your API spec guarantees one.

```typescript
import { test, expect } from '@playwright/test';

test('DELETE /posts/1 — accepts 200 or 204', async ({ request }) => {
  const response = await request.delete('/posts/1');

  // Accept either status — both indicate success
  expect([200, 204]).toContain(response.status());
});

test('DELETE /posts/1 — if 200, body contains deleted resource', async ({ request }) => {
  const response = await request.delete('/posts/1');

  if (response.status() === 200) {
    const deleted = await response.json();
    expect(deleted).toHaveProperty('id');
  }
  // If 204, body is empty — do not call response.json() or it throws
});

test('DELETE /posts/99999 — 404 for non-existent resource', async ({ request }) => {
  const response = await request.delete('/posts/99999');
  expect(response.status()).toBe(404);
});

test('DELETE /posts/1 — idempotent: second delete returns 404', async ({ request }) => {
  // First delete
  const first = await request.delete('/posts/1');
  expect([200, 204]).toContain(first.status());

  // Second delete of same resource
  const second = await request.delete('/posts/1');
  expect(second.status()).toBe(404);
});
```

Common mistake: calling `await response.json()` on a 204 response. A 204 response has no body and `json()` will throw a JSON parse error. Always check the status before parsing.

---

**Q14: What is response.ok() and when should you use it instead of asserting the exact status code?**

**A:** `response.ok()` returns `true` when the HTTP status code is in the 200–299 range. It is a convenience check for "the request succeeded" without specifying which exact success code.

Use `response.ok()` when:
- You do not care whether it was 200, 201, 202, etc. — just that it succeeded
- Writing a helper that validates general API health

Use the exact status code check when:
- You are asserting a POST should return 201 (not 200)
- You are asserting a DELETE returns 204 (no body)
- You are writing a contract test where the status code is part of the API specification

```typescript
import { test, expect } from '@playwright/test';

test('response.ok() — use for general success', async ({ request }) => {
  const response = await request.get('/users');

  // Passes for 200, 201, 202, 204, etc.
  expect(response.ok()).toBeTruthy();
});

test('exact status — use for contract testing', async ({ request }) => {
  const response = await request.post('/users', {
    data: { name: 'Alice', email: 'alice@example.com' },
  });

  // Strict contract: POST must return 201, not just "something 2xx"
  expect(response.status()).toBe(201);
});

test('incorrect use of ok() — misses contract violation', async ({ request }) => {
  const response = await request.post('/users', {
    data: { name: 'Bob', email: 'bob@example.com' },
  });

  // BAD: If the API changes to return 200 instead of 201,
  // this test still passes — contract regression is invisible
  expect(response.ok()).toBeTruthy(); // do NOT use for POST/PUT/DELETE assertions
});
```

---

## SECTION 3: ASSERTIONS

**Q15: What is toMatchObject and when is it better than toEqual or toBe?**

**A:**
- `toBe(value)` — strict reference/value equality using `===`. For primitives only.
- `toEqual(value)` — deep equality. Every single property in the actual object must match the expected object exactly. Extra properties in the actual object cause failure.
- `toMatchObject(subset)` — the actual object must contain at least all properties in the expected object. Extra properties in the actual object are ignored.

`toMatchObject` is ideal for API response bodies because the server typically adds fields you cannot predict at assertion time (like `id`, `createdAt`, `updatedAt`).

```typescript
import { test, expect } from '@playwright/test';

test('toMatchObject vs toEqual demonstration', async ({ request }) => {
  const response = await request.post('/posts', {
    data: { title: 'My Post', body: 'Content', userId: 1 },
  });
  const body = await response.json();

  // body is: { id: 101, title: 'My Post', body: 'Content', userId: 1 }

  // toEqual FAILS — id is not in expected but is in actual
  // expect(body).toEqual({ title: 'My Post', body: 'Content', userId: 1 }); // FAILS

  // toMatchObject PASSES — checks only the listed fields, ignores id
  expect(body).toMatchObject({
    title: 'My Post',
    body: 'Content',
    userId: 1,
  });

  // toMatchObject with matchers — very flexible
  expect(body).toMatchObject({
    id: expect.any(Number),                    // id is a number
    title: expect.any(String),                 // title is a string
    email: expect.stringContaining('@'),        // email has @
    createdAt: expect.stringMatching(/^\d{4}/), // starts with year
  });
});
```

---

**Q16: How do you write soft assertions in Playwright API tests and why would you use them?**

**A:** A regular assertion (`expect(x).toBe(y)`) stops the test immediately on failure. A soft assertion (`expect.soft(x).toBe(y)`) records the failure but continues executing the test. All soft assertion failures are reported together at the end of the test.

Use soft assertions when you want to capture all API response problems in a single test run rather than stopping at the first failure.

```typescript
import { test, expect } from '@playwright/test';

test('soft assertions — validate all user fields in one pass', async ({ request }) => {
  const response = await request.get('/users/1');
  const user = await response.json();

  // Hard assertion — if this fails, test stops here
  expect(response.status()).toBe(200);

  // Soft assertions — all are evaluated even if some fail
  expect.soft(user.id).toBe(1);
  expect.soft(user.name).toBeTruthy();
  expect.soft(user.email).toContain('@');
  expect.soft(user.phone).toBeDefined();
  expect.soft(user.website).toMatch(/^[a-z]/); // starts with lowercase letter
  expect.soft(user.address).toHaveProperty('city');
  expect.soft(user.company).toHaveProperty('name');

  // Test continues even if some soft assertions above failed
  console.log('All fields checked — see failure report for any issues');
});
```

Real-world use case: schema validation of a new API endpoint. You want to check every field of a large response in one test run to see the full picture of what is wrong, not just the first failure.

---

**Q17: How do you assert that an array response has the correct structure for every item?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('assert every item in a list response', async ({ request }) => {
  const response = await request.get('/users');
  expect(response.status()).toBe(200);

  const users = await response.json();

  // Assert array type and length
  expect(Array.isArray(users)).toBe(true);
  expect(users.length).toBeGreaterThan(0);

  // Assert every item matches a schema
  for (const user of users) {
    expect(user).toMatchObject({
      id: expect.any(Number),
      name: expect.any(String),
      email: expect.stringContaining('@'),
      username: expect.any(String),
    });
    expect(user.id).toBeGreaterThan(0);
    expect(user.email).toMatch(/^[^\s@]+@[^\s@]+\.[^\s@]+$/); // basic email regex
  }

  // Find a specific item in the array
  const alice = users.find((u: { email: string }) => u.email === 'Sincere@april.biz');
  expect(alice).toBeDefined();
  expect(alice.name).toBe('Leanne Graham');

  // Assert array length matches expected count
  expect(users).toHaveLength(10);
});
```

---

## SECTION 4: AUTHENTICATION

**Q18: How do you test API key authentication — both valid key and invalid key scenarios?**

**A:**

```typescript
// tests/api/auth-apikey.spec.ts
import { test, expect } from '@playwright/test';

test.describe('API Key Authentication', () => {

  test('valid API key in header — 200 OK', async ({ request }) => {
    const response = await request.get('/protected/data', {
      headers: {
        'x-api-key': process.env.API_KEY || 'valid-test-key-12345',
      },
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toHaveProperty('data');
  });

  test('API key in query parameter — 200 OK', async ({ request }) => {
    const response = await request.get('/protected/data', {
      params: {
        api_key: process.env.API_KEY || 'valid-test-key-12345',
      },
    });

    expect(response.status()).toBe(200);
  });

  test('invalid API key — 401 Unauthorized', async ({ request }) => {
    const response = await request.get('/protected/data', {
      headers: {
        'x-api-key': 'completely-invalid-key-xyz-999',
      },
    });

    expect(response.status()).toBe(401);

    const body = await response.json();
    expect(body).toHaveProperty('error');
    expect(body.error).toMatch(/unauthorized|invalid.*key/i);
  });

  test('missing API key — 401 Unauthorized', async ({ request }) => {
    // No x-api-key header at all
    const response = await request.get('/protected/data');

    expect(response.status()).toBe(401);
  });

  test('revoked API key — 401 or 403', async ({ request }) => {
    const response = await request.get('/protected/data', {
      headers: {
        'x-api-key': process.env.REVOKED_API_KEY || 'revoked-key-abc',
      },
    });

    // Some APIs return 401 (unauthenticated) some return 403 (authenticated but revoked)
    expect([401, 403]).toContain(response.status());
  });
});
```

---

**Q19: How do you implement Bearer token authentication and test invalid/expired tokens?**

**A:**

```typescript
// tests/api/auth-bearer.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Bearer Token Authentication', () => {

  test('valid Bearer token — 200 OK with user profile', async ({ request }) => {
    const token = process.env.BEARER_TOKEN || 'valid-jwt-token-here';

    const response = await request.get('/api/me', {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });

    expect(response.status()).toBe(200);

    const profile = await response.json();
    expect(profile).toHaveProperty('id');
    expect(profile).toHaveProperty('email');
    expect(profile.email).toContain('@');
  });

  test('expired Bearer token — 401 with appropriate error', async ({ request }) => {
    const expiredToken =
      'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9' +
      '.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZXhwIjoxfQ' +
      '.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c';

    const response = await request.get('/api/me', {
      headers: {
        'Authorization': `Bearer ${expiredToken}`,
      },
    });

    expect(response.status()).toBe(401);

    const body = await response.json();
    // Error message should mention token is expired
    expect(body.message || body.error).toMatch(/expired|invalid token/i);
  });

  test('malformed Authorization header — 401', async ({ request }) => {
    const response = await request.get('/api/me', {
      headers: {
        'Authorization': 'Token abc123', // wrong scheme — should be Bearer
      },
    });

    expect(response.status()).toBe(401);
  });

  test('missing Authorization header — 401', async ({ request }) => {
    const response = await request.get('/api/me');
    // No Authorization header
    expect(response.status()).toBe(401);
  });

  test('Bearer token with wrong scope — 403 Forbidden', async ({ request }) => {
    // Token is valid but does not have admin scope
    const regularUserToken = process.env.USER_TOKEN || 'regular-user-token';

    const response = await request.get('/api/admin/users', {
      headers: {
        'Authorization': `Bearer ${regularUserToken}`,
      },
    });

    // Authenticated but not authorized
    expect(response.status()).toBe(403);
  });
});
```

---

**Q20: How do you implement Basic authentication in Playwright API tests?**

**A:**

```typescript
// tests/api/auth-basic.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Basic Authentication', () => {

  // Encode credentials to Base64 (standard Basic Auth format)
  function encodeBasicAuth(username: string, password: string): string {
    return Buffer.from(`${username}:${password}`).toString('base64');
  }

  test('valid Basic auth credentials — 200 OK', async ({ request }) => {
    const credentials = encodeBasicAuth('admin', 'SecretPassword123');

    const response = await request.get('/api/admin/dashboard', {
      headers: {
        'Authorization': `Basic ${credentials}`,
      },
    });

    expect(response.status()).toBe(200);
  });

  test('wrong password — 401 Unauthorized', async ({ request }) => {
    const credentials = encodeBasicAuth('admin', 'wrongpassword');

    const response = await request.get('/api/admin/dashboard', {
      headers: {
        'Authorization': `Basic ${credentials}`,
      },
    });

    expect(response.status()).toBe(401);
  });

  test('non-existent user — 401 Unauthorized', async ({ request }) => {
    const credentials = encodeBasicAuth('ghost@example.com', 'anypassword');

    const response = await request.get('/api/admin/dashboard', {
      headers: {
        'Authorization': `Basic ${credentials}`,
      },
    });

    expect(response.status()).toBe(401);
  });

  test('Basic auth error message does not reveal which field was wrong', async ({ request }) => {
    const credentials = encodeBasicAuth('admin', 'wrongpassword');

    const response = await request.get('/api/admin/dashboard', {
      headers: {
        'Authorization': `Basic ${credentials}`,
      },
    });

    const body = await response.json();
    // Security: error should be generic, not say "password is incorrect"
    expect(body.error || body.message).not.toMatch(/password.*incorrect/i);
    expect(body.error || body.message).toMatch(/invalid credentials/i);
  });
});
```

---

**Q21: How do you implement an OAuth2 login flow — POST to login, capture the token, then use it in subsequent requests using beforeAll?**

**A:**

```typescript
// tests/api/auth-oauth2.spec.ts
import { test, expect, APIRequestContext } from '@playwright/test';

// Module-level variable to hold the token across tests
let authToken: string;
let refreshToken: string;

test.describe('OAuth2 Login Flow', () => {

  // beforeAll runs once before all tests in this describe block
  // Logs in, captures the token, makes it available to all tests
  test.beforeAll(async ({ request }) => {
    const loginResponse = await request.post('/auth/login', {
      data: {
        username: process.env.TEST_USERNAME || 'user@example.com',
        password: process.env.TEST_PASSWORD || 'SecurePass123!',
        grant_type: 'password',
      },
    });

    expect(loginResponse.status()).toBe(200);

    const body = await loginResponse.json();
    expect(body).toHaveProperty('access_token');
    expect(body).toHaveProperty('refresh_token');
    expect(body.token_type).toBe('Bearer');

    authToken = body.access_token;
    refreshToken = body.refresh_token;
  });

  test('GET /api/profile — uses token from beforeAll', async ({ request }) => {
    expect(authToken).toBeDefined();

    const response = await request.get('/api/profile', {
      headers: {
        'Authorization': `Bearer ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
    const profile = await response.json();
    expect(profile.email).toBe('user@example.com');
  });

  test('GET /api/orders — uses same token', async ({ request }) => {
    const response = await request.get('/api/orders', {
      headers: {
        'Authorization': `Bearer ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
  });

  test('POST /auth/refresh — obtain new access token', async ({ request }) => {
    const response = await request.post('/auth/refresh', {
      data: {
        refresh_token: refreshToken,
        grant_type: 'refresh_token',
      },
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body).toHaveProperty('access_token');
    expect(body.access_token).not.toBe(authToken); // must be a new token
  });

  test('POST /auth/login — wrong credentials — 401', async ({ request }) => {
    const response = await request.post('/auth/login', {
      data: {
        username: 'wrong@example.com',
        password: 'wrongpassword',
      },
    });

    expect(response.status()).toBe(401);

    const body = await response.json();
    // Generic error — do not reveal which field was wrong
    expect(body.error).toMatch(/invalid credentials/i);
  });
});
```

Better pattern — using a custom fixture for scoped login teardown:

```typescript
// tests/fixtures/auth-fixture.ts
import { test as base, expect } from '@playwright/test';

type AuthFixtures = {
  authToken: string;
};

export const test = base.extend<AuthFixtures>({
  authToken: async ({ request }, use) => {
    // Setup: login
    const res = await request.post('/auth/login', {
      data: {
        username: process.env.TEST_USERNAME!,
        password: process.env.TEST_PASSWORD!,
      },
    });
    expect(res.status()).toBe(200);
    const { access_token } = await res.json();

    // Yield token to the test
    await use(access_token);

    // Teardown: logout (runs after every test that uses this fixture)
    await request.post('/auth/logout', {
      headers: { 'Authorization': `Bearer ${access_token}` },
    });
  },
});

export { expect };

// Usage in a test file:
// import { test, expect } from '../fixtures/auth-fixture';
// test('protected endpoint', async ({ request, authToken }) => {
//   const response = await request.get('/api/profile', {
//     headers: { 'Authorization': `Bearer ${authToken}` },
//   });
//   expect(response.status()).toBe(200);
// });
```

---

**Q22: How do you test that missing authentication returns 401 and wrong permissions return 403?**

**A:** 401 Unauthorized means the client is not authenticated (no credentials or invalid credentials). 403 Forbidden means the client is authenticated but does not have permission to access the resource. Both must be tested explicitly.

```typescript
import { test, expect } from '@playwright/test';

test.describe('Auth error scenarios', () => {

  test('no Authorization header → 401', async ({ request }) => {
    const response = await request.get('/api/protected/resource');
    // No credentials provided
    expect(response.status()).toBe(401);

    const body = await response.json();
    expect(body).toHaveProperty('error');
    // WWW-Authenticate header should be present in 401 responses (RFC 7235)
    expect(response.headers()['www-authenticate']).toBeDefined();
  });

  test('invalid token → 401', async ({ request }) => {
    const response = await request.get('/api/protected/resource', {
      headers: {
        'Authorization': 'Bearer this-is-completely-invalid-garbage',
      },
    });

    expect(response.status()).toBe(401);
  });

  test('valid token, insufficient role → 403', async ({ request }) => {
    // Regular user token — valid auth but no admin permission
    const regularToken = process.env.REGULAR_USER_TOKEN || 'regular-user-jwt';

    const response = await request.delete('/api/admin/users/1', {
      headers: {
        'Authorization': `Bearer ${regularToken}`,
      },
    });

    // Authenticated (token is valid) but not authorized (lacks admin role)
    expect(response.status()).toBe(403);

    const body = await response.json();
    expect(body.message || body.error).toMatch(/forbidden|insufficient.*permission/i);
  });

  test('rate limiting after too many failed logins → 429', async ({ request }) => {
    // Simulate brute force
    for (let i = 0; i < 5; i++) {
      await request.post('/auth/login', {
        data: { username: 'victim@example.com', password: 'wrong' },
      });
    }

    const response = await request.post('/auth/login', {
      data: { username: 'victim@example.com', password: 'wrong' },
    });

    // Should be rate limited after 5 failed attempts
    expect([429, 423]).toContain(response.status());

    // Should include Retry-After header
    const retryAfter = response.headers()['retry-after'];
    if (retryAfter) {
      expect(Number(retryAfter)).toBeGreaterThan(0);
    }
  });
});
```

---

## SECTION 5: ADVANCED PATTERNS

**Q23: How do you chain API requests — POST to create a resource, GET to verify it, then DELETE to clean up?**

**A:**

```typescript
// tests/api/chained-crud.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Chained CRUD — POST → GET → PATCH → DELETE', () => {
  let createdPostId: number;

  test('Step 1 — POST: create a new resource', async ({ request }) => {
    const response = await request.post('/posts', {
      data: {
        title: 'Chained Test Post',
        body: 'Created by the chained CRUD test.',
        userId: 1,
      },
    });

    expect(response.status()).toBe(201);

    const body = await response.json();
    expect(body.title).toBe('Chained Test Post');
    expect(body.id).toBeDefined();

    createdPostId = body.id;
    console.log(`Created post with ID: ${createdPostId}`);
  });

  test('Step 2 — GET: verify the resource exists', async ({ request }) => {
    expect(createdPostId).toBeDefined(); // guard

    const response = await request.get(`/posts/${createdPostId}`);

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.id).toBe(createdPostId);
    expect(body.title).toBe('Chained Test Post');
  });

  test('Step 3 — PATCH: update the resource', async ({ request }) => {
    expect(createdPostId).toBeDefined();

    const response = await request.patch(`/posts/${createdPostId}`, {
      data: { title: 'Updated by PATCH in chain test' },
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body.title).toBe('Updated by PATCH in chain test');
  });

  test('Step 4 — DELETE: remove the resource', async ({ request }) => {
    expect(createdPostId).toBeDefined();

    const response = await request.delete(`/posts/${createdPostId}`);

    expect([200, 204]).toContain(response.status());
  });

  test('Step 5 — GET after DELETE: confirm 404', async ({ request }) => {
    expect(createdPostId).toBeDefined();

    const response = await request.get(`/posts/${createdPostId}`);

    expect(response.status()).toBe(404);
  });
});
```

Better approach — do it all in a single test to avoid ordering dependencies:

```typescript
test('full CRUD lifecycle in a single test', async ({ request }) => {
  // 1. Create
  const createRes = await request.post('/posts', {
    data: { title: 'CRUD Test', body: 'Content', userId: 1 },
  });
  expect(createRes.status()).toBe(201);
  const { id } = await createRes.json();
  expect(id).toBeDefined();

  // 2. Read
  const readRes = await request.get(`/posts/${id}`);
  expect(readRes.status()).toBe(200);
  expect((await readRes.json()).title).toBe('CRUD Test');

  // 3. Update
  const updateRes = await request.patch(`/posts/${id}`, {
    data: { title: 'Updated CRUD Test' },
  });
  expect(updateRes.status()).toBe(200);
  expect((await updateRes.json()).title).toBe('Updated CRUD Test');

  // 4. Delete
  const deleteRes = await request.delete(`/posts/${id}`);
  expect([200, 204]).toContain(deleteRes.status());

  // 5. Verify deleted
  const verifyRes = await request.get(`/posts/${id}`);
  expect(verifyRes.status()).toBe(404);
});
```

---

**Q24: How do you add query parameters to a GET request in Playwright?**

**A:** Use the `params` option in the request call. Playwright URL-encodes the values automatically.

```typescript
import { test, expect } from '@playwright/test';

test('GET with query parameters', async ({ request }) => {
  // Simple query params
  const response = await request.get('/users', {
    params: {
      page: 1,
      limit: 20,
      sort: 'email',
      order: 'asc',
    },
  });
  // Actual URL: /users?page=1&limit=20&sort=email&order=asc

  expect(response.status()).toBe(200);
  const body = await response.json();
  expect(Array.isArray(body.data)).toBe(true);
  expect(body.data.length).toBeLessThanOrEqual(20);
});

test('GET with filter params', async ({ request }) => {
  const response = await request.get('/products', {
    params: {
      category: 'electronics',
      minPrice: 100,
      maxPrice: 500,
      inStock: true,
      search: 'laptop 15 inch', // auto URL-encoded to 'laptop+15+inch' or %20
    },
  });

  expect(response.status()).toBe(200);
});

test('GET with empty/optional params — skip falsy', async ({ request }) => {
  const filters = {
    category: 'books',
    search: '', // empty — some APIs treat this differently
    page: 1,
  };

  const params = Object.fromEntries(
    Object.entries(filters).filter(([, v]) => v !== '' && v !== undefined)
  );

  const response = await request.get('/products', { params });
  expect(response.status()).toBe(200);
});
```

---

**Q25: What is request.newContext() and when do you need a separate request context?**

**A:** By default, the `request` fixture is a single shared context per test, inheriting from `playwright.config.ts`. `request.newContext()` creates a completely isolated HTTP client with its own cookies, headers, and base URL — independent of the global config.

Use `request.newContext()` when you need to:
- Simulate two different authenticated users in a single test (e.g., user A posts a comment, user B sees it)
- Use a completely different base URL or headers for part of a test
- Test isolation scenarios where cookie/session state must not carry over

```typescript
import { test, expect, request as playwrightRequest } from '@playwright/test';

test('two users interacting with the same resource', async ({ }) => {
  // Create two independent request contexts
  const contextUserA = await playwrightRequest.newContext({
    baseURL: 'https://api.example.com',
    extraHTTPHeaders: {
      'Authorization': `Bearer ${process.env.USER_A_TOKEN}`,
    },
  });

  const contextUserB = await playwrightRequest.newContext({
    baseURL: 'https://api.example.com',
    extraHTTPHeaders: {
      'Authorization': `Bearer ${process.env.USER_B_TOKEN}`,
    },
  });

  // User A creates a post
  const createRes = await contextUserA.post('/posts', {
    data: { title: 'User A post', body: 'Hello', userId: 1 },
  });
  expect(createRes.status()).toBe(201);
  const { id } = await createRes.json();

  // User B reads User A's post
  const readRes = await contextUserB.get(`/posts/${id}`);
  expect(readRes.status()).toBe(200);
  expect((await readRes.json()).title).toBe('User A post');

  // User B cannot delete User A's post (ownership check)
  const deleteRes = await contextUserB.delete(`/posts/${id}`);
  expect(deleteRes.status()).toBe(403);

  // Cleanup: User A deletes their own post
  await contextUserA.delete(`/posts/${id}`);

  // Always dispose contexts when done
  await contextUserA.dispose();
  await contextUserB.dispose();
});
```

---

## SECTION 6: DATA-DRIVEN TESTING

**Q26: How do you write data-driven API tests using loops and arrays?**

**A:**

```typescript
// tests/api/data-driven.spec.ts
import { test, expect } from '@playwright/test';

// ── Pattern 1: Loop over valid IDs ───────────────────────────────────────────

const validUserIds = [1, 2, 3, 4, 5];

for (const userId of validUserIds) {
  test(`GET /users/${userId} — exists and has correct structure`, async ({ request }) => {
    const response = await request.get(`/users/${userId}`);

    expect(response.status()).toBe(200);

    const user = await response.json();
    expect(user.id).toBe(userId);
    expect(user.email).toMatch(/^[^\s@]+@[^\s@]+\.[^\s@]+$/);
    expect(typeof user.name).toBe('string');
  });
}

// ── Pattern 2: Scenario matrix (multiple inputs and expected outputs) ─────────

type PostScenario = {
  description: string;
  payload: Record<string, unknown>;
  expectedStatus: number;
  expectedErrorField?: string;
};

const postScenarios: PostScenario[] = [
  {
    description: 'valid post — all fields present',
    payload: { title: 'Valid Title', body: 'Valid body', userId: 1 },
    expectedStatus: 201,
  },
  {
    description: 'missing title — validation error',
    payload: { body: 'No title here', userId: 1 },
    expectedStatus: 400,
    expectedErrorField: 'title',
  },
  {
    description: 'missing userId — validation error',
    payload: { title: 'Title', body: 'Body' },
    expectedStatus: 400,
    expectedErrorField: 'userId',
  },
  {
    description: 'title too long — validation error',
    payload: { title: 'A'.repeat(500), body: 'Valid body', userId: 1 },
    expectedStatus: 422,
  },
];

for (const scenario of postScenarios) {
  test(`POST /posts — ${scenario.description}`, async ({ request }) => {
    const response = await request.post('/posts', {
      data: scenario.payload,
    });

    expect(response.status()).toBe(scenario.expectedStatus);

    if (scenario.expectedErrorField) {
      const error = await response.json();
      // Error response should mention which field failed
      const errorText = JSON.stringify(error).toLowerCase();
      expect(errorText).toContain(scenario.expectedErrorField);
    }
  });
}

// ── Pattern 3: Load from external JSON file ───────────────────────────────────

import testData from '../fixtures/test-data.json';

for (const testCase of testData.users) {
  test(`GET user — ${testCase.name} (ID ${testCase.id})`, async ({ request }) => {
    const response = await request.get(`/users/${testCase.id}`);

    expect(response.status()).toBe(200);

    const user = await response.json();
    expect(user.name).toBe(testCase.name);
    expect(user.email).toBe(testCase.email);
  });
}
```

```json
// tests/fixtures/test-data.json
{
  "users": [
    { "id": 1, "name": "Leanne Graham", "email": "Sincere@april.biz" },
    { "id": 2, "name": "Ervin Howell",  "email": "Shanna@melissa.tv" },
    { "id": 3, "name": "Clementine Bauch", "email": "Nathan@yesenia.net" }
  ],
  "invalidIds": [0, -1, 9999999]
}
```

---

**Q27: How do you use environment variables in Playwright API tests?**

**A:**

```env
# .env — never commit this file
BASE_URL=https://jsonplaceholder.typicode.com
STAGING_URL=https://staging.api.example.com
API_KEY=your-api-key-here
BEARER_TOKEN=your-bearer-token-here
TEST_USERNAME=test.user@example.com
TEST_PASSWORD=SecurePass123!
CI=false
```

```typescript
// playwright.config.ts
import { defineConfig } from '@playwright/test';
import * as dotenv from 'dotenv';

dotenv.config(); // load .env before defineConfig reads process.env

export default defineConfig({
  use: {
    baseURL: process.env.BASE_URL || 'https://fallback.api.com',
    extraHTTPHeaders: {
      'Authorization': `Bearer ${process.env.BEARER_TOKEN || ''}`,
      'x-api-key': process.env.API_KEY || '',
    },
  },
});
```

```typescript
// Accessing env vars in a test file
import { test, expect } from '@playwright/test';

test('environment-driven test', async ({ request }) => {
  // Access env var directly in test
  const adminKey = process.env.ADMIN_API_KEY;
  if (!adminKey) {
    test.skip(); // skip gracefully if var not set
  }

  const response = await request.get('/admin/stats', {
    headers: { 'x-api-key': adminKey! },
  });

  expect(response.status()).toBe(200);
});
```

Switching environments from the command line:

```bash
# Use .env.staging file
dotenv -e .env.staging npx playwright test

# Inline override (Linux/macOS)
BASE_URL=https://staging.api.example.com npx playwright test

# Windows PowerShell
$env:BASE_URL="https://staging.api.example.com"; npx playwright test

# Run staging project from config
npx playwright test --project=staging
```

---

## SECTION 7: CENTRALIZING CONFIGURATION

**Q28: How do you centralize API endpoints, HTTP status codes, and test data in a constants file?**

**A:**

```typescript
// utils/constants.ts
export const BASE_URL = process.env.BASE_URL || 'https://jsonplaceholder.typicode.com';

// All API endpoint paths in one place — update here if the API changes
export const ENDPOINTS = {
  USERS: '/users',
  USER_BY_ID: (id: number | string) => `/users/${id}`,
  POSTS: '/posts',
  POST_BY_ID: (id: number | string) => `/posts/${id}`,
  TODOS: '/todos',
  COMMENTS: '/comments',
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    REGISTER: '/auth/register',
  },
  PRODUCTS: '/products',
  PRODUCT_BY_ID: (id: number | string) => `/products/${id}`,
} as const;

// Status codes as named constants for readability
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  ACCEPTED: 202,
  NO_CONTENT: 204,
  MOVED_PERMANENTLY: 301,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  METHOD_NOT_ALLOWED: 405,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_SERVER_ERROR: 500,
  BAD_GATEWAY: 502,
  SERVICE_UNAVAILABLE: 503,
} as const;

// Timeout thresholds (agreed SLAs with the team)
export const TIMEOUTS = {
  DEFAULT: 10000,       // 10s — standard endpoints
  SLOW_ENDPOINT: 30000, // 30s — report generation, bulk operations
  FAST_ENDPOINT: 2000,  // 2s — health check endpoints
} as const;

// Reusable test user data
export const TEST_USERS = {
  STANDARD: {
    username: process.env.TEST_USERNAME || 'test.user@example.com',
    password: process.env.TEST_PASSWORD || 'SecurePass123!',
  },
  ADMIN: {
    username: process.env.ADMIN_USERNAME || 'admin@example.com',
    password: process.env.ADMIN_PASSWORD || 'AdminPass456!',
  },
  INVALID: {
    username: 'nonexistent@nowhere.com',
    password: 'wrongpassword123',
  },
} as const;

// Usage in tests:
// import { ENDPOINTS, HTTP_STATUS, TEST_USERS } from '../../utils/constants';
// const response = await request.get(ENDPOINTS.USER_BY_ID(1));
// expect(response.status()).toBe(HTTP_STATUS.OK);
```

---

## SECTION 8: COMMANDS REFERENCE

**Q29: What are all the Playwright CLI commands you need for API testing?**

**A:**

```bash
# ── Running Tests ─────────────────────────────────────────────────────────────

# Run all tests
npx playwright test

# Run a specific file
npx playwright test tests/api/users.spec.ts

# Run tests matching a name pattern
npx playwright test --grep "GET user"

# Run tests with a tag (e.g., test names containing @smoke)
npx playwright test --grep "@smoke"

# Exclude tests matching a pattern
npx playwright test --grep-invert "@slow"

# Run against a specific project defined in playwright.config.ts
npx playwright test --project=staging

# ── Parallelism ───────────────────────────────────────────────────────────────

# Run with 4 parallel workers
npx playwright test --workers=4

# Run serially (1 worker — useful for debugging ordering issues)
npx playwright test --workers=1

# ── Debugging ─────────────────────────────────────────────────────────────────

# Debug mode — pauses at each action (opens browser for UI tests)
npx playwright test --debug

# Run a specific test by line number
npx playwright test tests/api/users.spec.ts:45

# List all discovered tests without running them
npx playwright test --list

# ── Reports ───────────────────────────────────────────────────────────────────

# Open the HTML report in the browser
npx playwright show-report

# Open report from a custom output folder
npx playwright show-report playwright-report

# ── Configuration ─────────────────────────────────────────────────────────────

# Override timeout for this run
npx playwright test --timeout=60000

# Use a specific config file
npx playwright test --config=playwright.staging.config.ts

# Retry failed tests 3 times
npx playwright test --retries=3

# ── Environment Switching ─────────────────────────────────────────────────────

# Linux/macOS — set env var inline
BASE_URL=https://staging.api.example.com npx playwright test

# Use dotenv-cli for .env files
npx dotenv -e .env.staging -- npx playwright test
```

---

## SECTION 9: TROUBLESHOOTING

**Q30: How do you troubleshoot an ECONNREFUSED error in Playwright API tests?**

**A:** This error means Playwright tried to connect to a server that is not listening on that address and port.

```
Error: connect ECONNREFUSED 127.0.0.1:3000
```

Causes and fixes:

```typescript
// 1. Check your baseURL is correct
// playwright.config.ts
use: {
  baseURL: 'http://localhost:3000', // is the server actually running on port 3000?
}

// 2. Start the server before tests in package.json
// "scripts": {
//   "test:api": "node server.js & npx playwright test && kill %1"
// }
```

```yaml
# 3. GitHub Actions — ensure server starts before tests
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Install dependencies
        run: npm ci

      - name: Start API server in background
        run: npm run start:server &

      - name: Wait for server to be ready
        run: npx wait-on http://localhost:3000/health --timeout 30000

      - name: Run Playwright API tests
        run: npx playwright test
```

```typescript
// 4. Add a global setup file to wait for the server
// playwright.config.ts
globalSetup: './global-setup.ts',

// global-setup.ts
import { chromium } from '@playwright/test';

async function globalSetup() {
  // Poll the health endpoint until the server is ready
  const { request } = await import('@playwright/test');
  const context = await (await request.newContext()).get('/health');
  // Or use a retry loop
}
export default globalSetup;
```

---

**Q31: How do you troubleshoot a 401 Unauthorized error that occurs in CI but not locally?**

**A:** The most common cause is that the authentication token or API key is set in your local `.env` file but is not configured as a CI secret.

```yaml
# GitHub Actions — add secrets in repository settings first
# Settings → Secrets and variables → Actions → New repository secret

# Then reference them in the workflow
jobs:
  test:
    runs-on: ubuntu-latest
    env:
      # Map GitHub secret to environment variable
      API_KEY: ${{ secrets.API_KEY }}
      BEARER_TOKEN: ${{ secrets.BEARER_TOKEN }}
      TEST_USERNAME: ${{ secrets.TEST_USERNAME }}
      TEST_PASSWORD: ${{ secrets.TEST_PASSWORD }}
      BASE_URL: ${{ secrets.STAGING_BASE_URL }}
    steps:
      - name: Run tests
        run: npx playwright test
```

Debugging steps:

```typescript
// Add a diagnostic test that only runs in CI to verify env is set
test('CI diagnostic — verify env vars are set', async ({ request }) => {
  if (!process.env.CI) test.skip();

  expect(process.env.API_KEY, 'API_KEY secret is not set in CI').toBeTruthy();
  expect(process.env.BEARER_TOKEN, 'BEARER_TOKEN secret is not set in CI').toBeTruthy();
  expect(process.env.BASE_URL, 'BASE_URL is not set in CI').toBeTruthy();
});
```

---

**Q32: How do you troubleshoot a JSON parse error in Playwright API tests?**

**A:**

```
SyntaxError: Unexpected token '<' at position 0
```

This means the API returned HTML (usually an error page, login redirect, or proxy page) instead of JSON.

```typescript
import { test, expect } from '@playwright/test';

// WRONG — calls json() without checking content type first
test('BAD PRACTICE — unsafe JSON parse', async ({ request }) => {
  const response = await request.get('/endpoint');
  const body = await response.json(); // throws if HTML was returned
});

// CORRECT — check content-type and status before parsing
test('GOOD PRACTICE — safe JSON parse', async ({ request }) => {
  const response = await request.get('/endpoint');

  // Check status first
  expect(response.status()).toBe(200);

  // Check content type before parsing
  const contentType = response.headers()['content-type'];
  expect(contentType).toContain('application/json');

  // Now safe to parse
  const body = await response.json();
  expect(body).toBeDefined();
});

// Debug what was actually returned
test('debug — print raw response on parse failure', async ({ request }) => {
  const response = await request.get('/endpoint');

  console.log('Status:', response.status());
  console.log('Content-Type:', response.headers()['content-type']);

  // Read as text to see what was actually returned
  const text = await response.text();
  console.log('Raw body:', text.substring(0, 500)); // first 500 chars

  // Now parse if it really is JSON
  if (response.headers()['content-type']?.includes('application/json')) {
    const body = JSON.parse(text);
    expect(body).toBeDefined();
  }
});
```

---

**Q33: How do you handle and increase timeouts for slow API endpoints?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

// Override timeout for a specific test
test('slow bulk export endpoint', async ({ request }) => {
  test.setTimeout(90000); // 90 seconds for this test only

  const response = await request.post('/reports/export', {
    data: {
      type: 'full',
      format: 'csv',
      dateRange: { from: '2023-01-01', to: '2023-12-31' },
    },
    timeout: 60000, // also increase per-request timeout (default: 30s)
  });

  expect(response.status()).toBe(200);
});

// Increase globally in playwright.config.ts for all tests
// timeout: 60000,

// Per-request timeout (inside the request call options)
test('per-request timeout', async ({ request }) => {
  const response = await request.get('/slow-endpoint', {
    timeout: 45000, // 45 seconds for this specific request
  });

  expect(response.ok()).toBeTruthy();
});
```

---

## SECTION 10: COMPARISON AND DECISION MAKING

**Q34: When should you choose Playwright API testing over RestAssured?**

**A:**

| Criterion | Choose Playwright | Choose RestAssured |
|---|---|---|
| Team language | JavaScript/TypeScript team | Java team |
| Existing framework | Already using Playwright for UI | Already using Java test framework |
| Browser-API combo | Need to share auth state between API and UI tests | API-only testing |
| Reporting | Want unified HTML report for API + UI | Need integration with Java CI (Surefire, Allure) |
| XML APIs | REST JSON APIs only | SOAP/XML-heavy APIs |
| Dependency | Prefer Node.js only | JVM is already in the stack |
| Learning curve | Lower for JS/TS developers | Higher for non-Java developers |

Real-world decision: if your team writes UI tests in Playwright/TypeScript, add API tests in Playwright too. The cognitive cost of switching toolchains (Java vs Node.js, JUnit vs Playwright runner) outweighs any advantage RestAssured offers for pure REST JSON testing.

```typescript
// Playwright API test — clean TypeScript, familiar assertions
test('GET /products — price is positive', async ({ request }) => {
  const response = await request.get('/products');
  expect(response.status()).toBe(200);
  const products = await response.json();
  products.forEach((p: { price: number }) => {
    expect(p.price).toBeGreaterThan(0);
  });
});
```

```java
// RestAssured equivalent — requires JVM, RestAssured library, Hamcrest matchers
given()
  .baseUri("https://api.example.com")
.when()
  .get("/products")
.then()
  .statusCode(200)
  .body("price", everyItem(greaterThan(0)));
```

---

**Q35: What is the complete list of APIRequestContext methods in Playwright?**

**A:**

```typescript
import { test, expect, APIRequestContext } from '@playwright/test';

test('APIRequestContext methods reference', async ({ request }) => {

  // ── HTTP Methods ──────────────────────────────────────────────────────────
  const getRes    = await request.get('/resource');
  const postRes   = await request.post('/resource', { data: {} });
  const putRes    = await request.put('/resource/1', { data: {} });
  const patchRes  = await request.patch('/resource/1', { data: {} });
  const deleteRes = await request.delete('/resource/1');
  const headRes   = await request.head('/resource');     // headers only, no body
  const fetchRes  = await request.fetch('/resource', {   // generic method
    method: 'OPTIONS',
  });

  // ── Request Options (available on all methods) ─────────────────────────────
  await request.get('/resource', {
    params: { page: 1, limit: 10 },              // query string parameters
    headers: { 'x-custom': 'value' },            // additional headers (merged with extraHTTPHeaders)
    timeout: 15000,                               // override timeout for this call
    failOnStatusCode: false,                      // do not throw on 4xx/5xx (default: false)
    ignoreHTTPSErrors: true,                      // trust invalid certs for this call
    maxRedirects: 5,                              // maximum redirect follows (default: 20)
  });

  await request.post('/resource', {
    data: { key: 'value' },                       // JSON body (auto-serialised)
    form: { field1: 'val1', field2: 'val2' },     // application/x-www-form-urlencoded
    multipart: {                                  // multipart/form-data
      file: { name: 'test.txt', mimeType: 'text/plain', buffer: Buffer.from('hello') },
      field: 'value',
    },
  });

  // ── APIResponse Methods ───────────────────────────────────────────────────
  const response = await request.get('/resource');

  response.status();        // number — HTTP status code
  response.statusText();    // string — "OK", "Not Found", etc.
  response.ok();            // boolean — true if 200–299
  response.url();           // string — final URL after redirects
  response.headers();       // Record<string, string> — all response headers
  response.headersArray();  // Array<{name, value}> — headers as array (for duplicate names)
  await response.json();    // parsed JSON body
  await response.text();    // body as string
  await response.body();    // body as Buffer

  // ── Context Management ────────────────────────────────────────────────────
  await request.storageState();          // get current cookies/storage
  await request.storageState({ path: 'state.json' }); // save to file
  await request.dispose();              // release context (for manually created contexts)
});
```

---

## SECTION 11: INTERVIEW Q&A

**Q36: An interviewer asks: explain the Playwright test lifecycle — beforeAll, beforeEach, afterEach, afterAll. How does it apply to API testing?**

**A:** Playwright follows a standard xUnit lifecycle:

- `test.beforeAll(async ({ request }) => {})` — runs once before all tests in a `describe` block. Use this to log in and capture a token.
- `test.beforeEach(async ({ request }) => {})` — runs before every individual test. Use this to create fresh test data or reset state.
- `test.afterEach(async ({ request }) => {})` — runs after every individual test. Use this to clean up resources created during the test.
- `test.afterAll(async ({ request }) => {})` — runs once after all tests in the block. Use this to log out or delete bulk test data.

```typescript
import { test, expect } from '@playwright/test';

let authToken: string;
let createdUserId: number;

test.describe('User management — lifecycle demo', () => {

  test.beforeAll(async ({ request }) => {
    // Login once for all tests
    const res = await request.post('/auth/login', {
      data: { username: 'admin@example.com', password: 'Admin123!' },
    });
    authToken = (await res.json()).access_token;
    console.log('beforeAll: logged in');
  });

  test.beforeEach(async ({ request }) => {
    // Create a fresh user before each test
    const res = await request.post('/users', {
      data: { name: 'Temp User', email: `temp${Date.now()}@test.com` },
      headers: { 'Authorization': `Bearer ${authToken}` },
    });
    createdUserId = (await res.json()).id;
    console.log(`beforeEach: created user ${createdUserId}`);
  });

  test('update user name', async ({ request }) => {
    const res = await request.patch(`/users/${createdUserId}`, {
      data: { name: 'Updated Name' },
      headers: { 'Authorization': `Bearer ${authToken}` },
    });
    expect(res.status()).toBe(200);
  });

  test('delete user', async ({ request }) => {
    const res = await request.delete(`/users/${createdUserId}`, {
      headers: { 'Authorization': `Bearer ${authToken}` },
    });
    expect([200, 204]).toContain(res.status());
    createdUserId = 0; // already deleted — skip afterEach cleanup
  });

  test.afterEach(async ({ request }) => {
    // Cleanup user created in beforeEach (if not already deleted in the test)
    if (createdUserId) {
      await request.delete(`/users/${createdUserId}`, {
        headers: { 'Authorization': `Bearer ${authToken}` },
      });
      console.log(`afterEach: deleted user ${createdUserId}`);
    }
  });

  test.afterAll(async ({ request }) => {
    // Logout
    await request.post('/auth/logout', {
      headers: { 'Authorization': `Bearer ${authToken}` },
    });
    console.log('afterAll: logged out');
  });
});
```

---

**Q37: What is the difference between `request.get()` in Playwright and the browser's fetch() API?**

**A:** `request.get()` in Playwright uses Playwright's `APIRequestContext`, which runs at the Node.js level — completely outside any browser sandbox. Key differences:

| Aspect | Playwright request.get() | Browser fetch() |
|---|---|---|
| Runs in | Node.js process | Browser rendering engine |
| CORS | Not subject to CORS | Subject to CORS restrictions |
| Browser cookies | No — separate context | Yes — uses browser's session |
| JavaScript sandbox | No | Yes — browser security applies |
| Use case | Server-side API testing | Frontend code calling an API |
| Access to DOM | No | Yes — can read/write page state |

Playwright `request` is essentially a headless HTTP client (similar to Axios or node-fetch) that is integrated with Playwright's assertion library and test runner.

---

**Q38: How do you test pagination in a REST API?**

**A:**

```typescript
import { test, expect } from '@playwright/test';

test('pagination — first page has correct structure', async ({ request }) => {
  const response = await request.get('/users', {
    params: { page: 1, limit: 5 },
  });

  expect(response.status()).toBe(200);

  const body = await response.json();

  // Pagination metadata
  expect(body).toHaveProperty('data');
  expect(body).toHaveProperty('total');
  expect(body).toHaveProperty('page');
  expect(body).toHaveProperty('limit');
  expect(body).toHaveProperty('totalPages');

  expect(body.data.length).toBe(5);
  expect(body.page).toBe(1);
  expect(body.limit).toBe(5);
  expect(body.total).toBeGreaterThan(5);
  expect(body.totalPages).toBe(Math.ceil(body.total / 5));
});

test('pagination — last page has remaining items', async ({ request }) => {
  // First, get total count
  const countRes = await request.get('/users', {
    params: { page: 1, limit: 5 },
  });
  const { total, totalPages, limit } = await countRes.json();
  const remainingItems = total % limit || limit;

  // Request the last page
  const lastPageRes = await request.get('/users', {
    params: { page: totalPages, limit },
  });

  expect(lastPageRes.status()).toBe(200);

  const lastPage = await lastPageRes.json();
  expect(lastPage.data.length).toBe(remainingItems);
  expect(lastPage.page).toBe(totalPages);
});

test('pagination — page beyond total returns empty', async ({ request }) => {
  const response = await request.get('/users', {
    params: { page: 9999, limit: 10 },
  });

  // Either 200 with empty array or 404 depending on API design
  if (response.status() === 200) {
    const body = await response.json();
    expect(body.data.length).toBe(0);
  } else {
    expect(response.status()).toBe(404);
  }
});
```

---

*End of File 04 — Playwright API Testing Complete Interview Q&A Guide*
