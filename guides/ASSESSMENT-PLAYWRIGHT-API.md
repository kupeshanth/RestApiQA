# Playwright API Testing — Technical Assessment Guide
## Complete Setup → Write → Run → Push | Do This Like a Pro

---

## BEFORE YOU START — Read This First (2 minutes)

When they give you the task, do this BEFORE touching the keyboard:

```
1. Read the full task — understand what endpoints to test
2. Note the BASE URL they give you (highlight it — you'll change it in one place only)
3. Note what they want: GET/POST/PUT/DELETE? Auth? Schema?
4. Open your browser → paste the API URL → confirm it's reachable
5. Check: do they want TypeScript or JavaScript? (default: TypeScript)
6. Now start the clock
```

---

## STEP 1 — Prerequisites Check (1 minute)

Open terminal and verify:

```bash
node --version    # Need v18+. If missing: https://nodejs.org → LTS → install
npm --version     # Comes with Node. Should show 9+
```

If Node is missing:
```bash
# Windows — download installer from https://nodejs.org and install
# Then close and reopen terminal
node --version    # should now show v18 or higher
```

---

## STEP 2 — Create Project (3 minutes)

```bash
# Create a new folder with the company/task name
mkdir playwright-api-assessment
cd playwright-api-assessment

# Initialize Playwright
npm init playwright@latest
```

When prompted, answer:
```
Do you want to use TypeScript or JavaScript?   → TypeScript     (press Enter)
Where to put your end-to-end tests?            → tests          (press Enter)
Add a GitHub Actions workflow?                 → n              (press n, Enter)
Install Playwright browsers?                   → n              (press n — API only, no browsers)
```

Then:
```bash
npm install                      # install dependencies
```

✅ You should now have this structure:
```
playwright-api-assessment/
├── package.json
├── playwright.config.ts
└── tests/
    └── example.spec.ts   ← delete this, you'll create your own
```

---

## STEP 3 — Create Folder Structure (1 minute)

```bash
# Create the folders
mkdir tests/api
mkdir utils

# Delete the example file they generate
del tests\example.spec.ts          # Windows CMD
rm tests/example.spec.ts           # Mac/Linux/Git Bash
```

Final structure you want:
```
playwright-api-assessment/
├── package.json
├── playwright.config.ts      ← EDIT THIS (Step 4)
├── utils/
│   └── constants.ts          ← CREATE THIS (Step 5)
└── tests/
    └── api/
        └── posts.spec.ts     ← CREATE THIS (Step 6)
```

---

## STEP 4 — Configure playwright.config.ts

Open `playwright.config.ts` and **REPLACE EVERYTHING** with:

```typescript
import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,      // run in order for easier debugging
  retries: 0,
  reporter: [['html'], ['list']],

  use: {
    // ⚠️ CHANGE THIS to the API URL they give you
    baseURL: 'https://reqres.in',

    // Default headers sent with every request
    extraHTTPHeaders: {
      'Content-Type': 'application/json',
      'Accept':       'application/json',

      // ⚠️ If the API requires auth, add it here:
      // 'Authorization': 'Bearer YOUR_TOKEN_HERE',
      // 'x-api-key': 'YOUR_API_KEY_HERE',
    },

    // How long to wait for a response before failing
    actionTimeout: 15000,   // 15 seconds
  },
});
```

**Variable reference:**

| What to change | Where | Example |
|----------------|-------|---------|
| `baseURL` | Line 10 | `'https://api.company.com'` |
| `Authorization` | Line 17 | `'Bearer token123'` |
| `x-api-key` | Line 18 | `'key-abc-123'` |

---

## STEP 5 — Create constants.ts

Create file `utils/constants.ts`:

```typescript
// ⚠️ Change BASE_URL if different from playwright.config.ts baseURL
export const BASE_URL = 'https://reqres.in';

// ⚠️ Add/change endpoints to match what they give you
export const ENDPOINTS = {
  users:    '/api/users',
  posts:    '/api/posts',
  login:    '/api/login',
  register: '/api/register',
};

// ⚠️ Change these to match the API's actual test credentials (if given)
export const TEST_USERS = {
  valid: {
    email:    'eve.holt@reqres.in',
    password: 'cityslicka',
  },
  invalid: {
    email:    'nobody@reqres.in',
    password: 'wrongpass',
  },
};

// ⚠️ Adjust these to match the API's actual response structure
export const EXPECTED = {
  usersPerPage: 6,
  totalUsers:   12,
};
```

---

## STEP 6 — Write Your Tests

Create file `tests/api/posts.spec.ts`:

```typescript
import { test, expect } from '@playwright/test';
import { ENDPOINTS, TEST_USERS } from '../../utils/constants';

// ============================================================
// SECTION 1: GET REQUESTS
// ============================================================

test.describe('GET Requests', () => {

  // ── HAPPY PATH: Get all resources ────────────────────────
  test('GET all users returns 200 with data array', async ({ request }) => {
    const response = await request.get(ENDPOINTS.users);

    // ⚠️ Change 200 if API uses different success code
    expect(response.status()).toBe(200);

    const body = await response.json();

    // ⚠️ Change 'data' to whatever the array field is called in the response
    // e.g. body.users, body.items, body.results
    expect(body.data).toBeDefined();
    expect(body.data.length).toBeGreaterThan(0);

    // ⚠️ Change field names to match actual API response
    expect(body.data[0]).toHaveProperty('id');
    expect(body.data[0]).toHaveProperty('email');
    expect(body.data[0]).toHaveProperty('first_name');

    console.log(`Total users found: ${body.data.length}`);
  });

  // ── HAPPY PATH: Get single resource ──────────────────────
  test('GET user by ID returns 200 with correct user data', async ({ request }) => {
    const userId = 2;   // ⚠️ Change to a valid ID in the system

    const response = await request.get(`${ENDPOINTS.users}/${userId}`);
    expect(response.status()).toBe(200);

    const body = await response.json();

    // ⚠️ Change 'data' and field names to match response
    expect(body.data.id).toBe(userId);
    expect(body.data.email).toMatch(/.+@.+\..+/);  // any valid email format
    expect(body.data.first_name).toBeTruthy();

    console.log(`User: ${body.data.first_name} ${body.data.last_name}`);
  });

  // ── NEGATIVE: Resource not found ─────────────────────────
  test('GET non-existent user returns 404', async ({ request }) => {
    const response = await request.get(`${ENDPOINTS.users}/99999`);

    // ⚠️ Some APIs return 200 with empty body instead of 404
    // Check the API docs to know what to expect
    expect(response.status()).toBe(404);
  });

  // ── FILTER: Query parameters ──────────────────────────────
  test('GET users with page param returns paginated results', async ({ request }) => {
    const response = await request.get(ENDPOINTS.users, {
      params: { page: 2 }   // ⚠️ Change param name if different (e.g. page_number, offset)
    });

    expect(response.status()).toBe(200);
    const body = await response.json();

    // ⚠️ Change field names to match pagination structure
    expect(body.page).toBe(2);
    expect(body.data.length).toBeGreaterThan(0);
  });

});

// ============================================================
// SECTION 2: POST REQUESTS
// ============================================================

test.describe('POST Requests', () => {

  // ── HAPPY PATH: Create resource ──────────────────────────
  test('POST creates user and returns 201 with id', async ({ request }) => {
    const newUser = {
      // ⚠️ Change fields to match what the API expects
      name: 'QA Automation Engineer',
      job:  'Test Automation',
    };

    const response = await request.post(ENDPOINTS.users, {
      data: newUser
    });

    // ⚠️ Change 201 if API uses 200 for creation
    expect(response.status()).toBe(201);

    const body = await response.json();

    // ⚠️ Change field assertions to match response
    expect(body.id).toBeTruthy();
    expect(body.name).toBe(newUser.name);
    expect(body.job).toBe(newUser.job);
    expect(body.createdAt).toBeTruthy();

    console.log(`Created resource ID: ${body.id}`);
  });

  // ── NEGATIVE: Missing required field ─────────────────────
  test('POST with empty body returns 400 or 201 depending on API', async ({ request }) => {
    const response = await request.post(ENDPOINTS.users, {
      data: {}   // empty body
    });

    // ⚠️ JSONPlaceholder/ReqRes accept empty bodies (return 201)
    // Real APIs return 400 — check the API docs
    // Use anyOf to accept either:
    expect([200, 201, 400, 422]).toContain(response.status());
    console.log(`Empty body response status: ${response.status()}`);
  });

});

// ============================================================
// SECTION 3: PUT / PATCH REQUESTS
// ============================================================

test.describe('PUT and PATCH Requests', () => {

  // ── PUT: Full replacement ─────────────────────────────────
  test('PUT updates user and returns 200 with updatedAt', async ({ request }) => {
    const updatedUser = {
      // ⚠️ Must include ALL fields for PUT (full replacement)
      name: 'Updated Name',
      job:  'Senior QA',
    };

    const response = await request.put(`${ENDPOINTS.users}/2`, {   // ⚠️ Change ID
      data: updatedUser
    });

    expect(response.status()).toBe(200);
    const body = await response.json();

    expect(body.name).toBe(updatedUser.name);
    expect(body.job).toBe(updatedUser.job);
    expect(body.updatedAt).toBeTruthy();   // ⚠️ Remove if API doesn't return this
  });

  // ── PATCH: Partial update ─────────────────────────────────
  test('PATCH updates only specified field and returns 200', async ({ request }) => {
    const response = await request.patch(`${ENDPOINTS.users}/2`, {   // ⚠️ Change ID
      data: { name: 'Patched Name Only' }   // only ONE field changed
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.name).toBe('Patched Name Only');
  });

});

// ============================================================
// SECTION 4: DELETE REQUESTS
// ============================================================

test.describe('DELETE Requests', () => {

  test('DELETE returns 204 with no body', async ({ request }) => {
    const response = await request.delete(`${ENDPOINTS.users}/2`);   // ⚠️ Change ID

    // ⚠️ ReqRes returns 204. JSONPlaceholder returns 200.
    // Check the API docs for the correct code.
    expect([200, 204]).toContain(response.status());

    const text = await response.text();
    expect(text).toBe('');   // ⚠️ JSONPlaceholder returns '{}' not empty — change if needed
  });

});

// ============================================================
// SECTION 5: AUTHENTICATION (if required)
// ============================================================

test.describe('Authentication', () => {

  // ── Login and capture token ───────────────────────────────
  test('POST login with valid credentials returns 200 with token', async ({ request }) => {
    const response = await request.post(ENDPOINTS.login, {
      data: {
        email:    TEST_USERS.valid.email,     // ⚠️ Change field name if different
        password: TEST_USERS.valid.password,  // ⚠️ Change field name if different
      }
    });

    expect(response.status()).toBe(200);
    const body = await response.json();

    // ⚠️ Change 'token' to whatever the API calls the auth token
    // e.g. body.accessToken, body.access_token, body.jwt
    expect(body.token).toBeTruthy();
    console.log(`Login token: ${body.token}`);
  });

  // ── Wrong credentials ─────────────────────────────────────
  test('POST login with wrong password returns 400 with error', async ({ request }) => {
    const response = await request.post(ENDPOINTS.login, {
      data: {
        email:    TEST_USERS.invalid.email,
        password: TEST_USERS.invalid.password,
      }
    });

    // ⚠️ Change to 401 if API uses that for wrong credentials
    expect([400, 401]).toContain(response.status());
    const body = await response.json();
    expect(body.error).toBeTruthy();
  });

  // ── Use token in protected request ────────────────────────
  test('GET protected resource with valid token returns 200', async ({ request }) => {
    // Step 1: Login to get token
    const loginResponse = await request.post(ENDPOINTS.login, {
      data: {
        email:    TEST_USERS.valid.email,
        password: TEST_USERS.valid.password,
      }
    });
    const loginBody = await loginResponse.json();
    const token = loginBody.token;   // ⚠️ Change field name if needed

    // Step 2: Use token in protected request
    const response = await request.get(ENDPOINTS.users, {
      headers: {
        // ⚠️ Change header name and prefix to match API (Bearer/Token/ApiKey)
        'Authorization': `Bearer ${token}`,
      }
    });

    expect(response.status()).toBe(200);
  });

  // ── No token → 401 ───────────────────────────────────────
  test('GET protected resource without token returns 401', async ({ request }) => {
    // Create a new context with NO auth headers
    const noAuthContext = await request.newContext();
    const response = await noAuthContext.get(ENDPOINTS.users);

    // ⚠️ Only add this test if the API actually requires auth
    // ReqRes doesn't require auth — skip this if public API
    expect([200, 401]).toContain(response.status());
  });

});

// ============================================================
// SECTION 6: RESPONSE VALIDATION
// ============================================================

test.describe('Response Validation', () => {

  // ── Headers check ─────────────────────────────────────────
  test('Response has correct Content-Type header', async ({ request }) => {
    const response = await request.get(ENDPOINTS.users);
    expect(response.status()).toBe(200);

    // ⚠️ Change if API uses different content type
    expect(response.headers()['content-type']).toContain('application/json');
  });

  // ── Response time check ───────────────────────────────────
  test('API responds within 3 seconds', async ({ request }) => {
    const start = Date.now();
    await request.get(ENDPOINTS.users);
    const duration = Date.now() - start;

    // ⚠️ Change 3000 to the SLA they specify (if any)
    expect(duration).toBeLessThan(3000);
    console.log(`Response time: ${duration}ms`);
  });

  // ── Object structure validation ───────────────────────────
  test('User object has all required fields', async ({ request }) => {
    const response = await request.get(`${ENDPOINTS.users}/1`);
    const body = await response.json();

    // ⚠️ toMatchObject checks PARTIAL match — add all fields you expect
    expect(body.data).toMatchObject({
      id:         expect.any(Number),
      email:      expect.any(String),
      first_name: expect.any(String),
      last_name:  expect.any(String),
    });
  });

});
```

---

## STEP 7 — Run Your Tests

```bash
# Run all tests
npx playwright test

# Run and show browser report after
npx playwright test --reporter=html
npx playwright show-report

# Run specific file only
npx playwright test tests/api/posts.spec.ts

# Run with console output visible
npx playwright test --reporter=list
```

**Expected output:**
```
Running 14 tests using 1 worker

  ✓ GET all users returns 200 with data array (512ms)
  ✓ GET user by ID returns 200 with correct user data (198ms)
  ✓ GET non-existent user returns 404 (201ms)
  ...
  14 passed (8s)
```

---

## STEP 8 — Create README.md

Create file `README.md` in root folder:

```markdown
# API Test Assessment — Playwright

## Setup
```
node --version   # Need v18+
npm install
```

## Run Tests
```
npx playwright test
```

## View Report
```
npx playwright show-report
```

## Tech Stack
- Playwright (TypeScript)
- Node.js v18+

## Test Coverage
- GET all resources (200, array, fields)
- GET by ID (200, correct data)
- GET not found (404)
- POST create (201, id returned)
- PUT update (200, updatedAt)
- DELETE (204)
- Auth: login, token usage, invalid credentials
- Response time validation
- Content-Type header validation
```

---

## STEP 9 — Push to GitHub

```bash
git init
git add .
git commit -m "Add Playwright API test assessment"

# Create repo on GitHub first, then:
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git
git branch -M main
git push -u origin main
```

---

## WHERE THINGS GO WRONG — Fixes

### Error: `baseURL is not set`
```
Fix: Check playwright.config.ts — baseURL must be set under use: {}
```

### Error: `Cannot find module '../../utils/constants'`
```
Fix: Check the file path matches your folder structure
     utils/constants.ts must exist
     import path: '../../utils/constants' (two levels up from tests/api/)
```

### Error: `expect(received).toBe(200) — received 404`
```
Fix: The endpoint path is wrong. Open the URL in browser to check.
     Double check: ENDPOINTS.users = '/api/users' — is /api/ correct?
```

### Error: `expect(body.data[0]).toHaveProperty('email')` fails
```
Fix: The response field names are different. Add this line to see the actual response:
     console.log(JSON.stringify(body, null, 2));
     Then match your assertions to the real field names.
```

### Error: `SyntaxError: Unexpected token` in JSON
```
Fix: The server returned HTML (error page) not JSON.
     Add: console.log(await response.text());
     to see what actually came back.
```

### Tests pass locally, fail in CI
```
Fix: Add to playwright.config.ts:
     timeout: 30000,          // increase timeout for CI
     retries: process.env.CI ? 1 : 0,   // retry once in CI
```

---

## VARIABLE REFERENCE — What to Change Per Assessment

| Variable | File | What to put |
|----------|------|------------|
| `baseURL` | playwright.config.ts | The API domain they give you |
| `ENDPOINTS.users` | utils/constants.ts | The actual endpoint path |
| `ENDPOINTS.login` | utils/constants.ts | Login endpoint if auth required |
| `TEST_USERS.valid.email` | utils/constants.ts | Test credentials they give you |
| `body.data` | tests | The array/object field in response |
| `body.data[0].id` | tests | Actual field names from API |
| `body.token` | tests | Actual token field name from API |
| `201` in POST test | tests | Actual creation status code |
| `204` in DELETE test | tests | Actual delete status code |
| `'Bearer'` prefix | tests | Bearer / Token / ApiKey / Basic |

---

## WHAT INTERVIEWERS CHECK

```
✅ Tests actually run — no compile errors
✅ baseURL in one place only (config), not hardcoded in every test
✅ Positive + negative tests both present
✅ Assertions on body, not just status code
✅ Console logs show useful info (created IDs, tokens)
✅ README tells them how to run in one command
✅ Clean code — no dead code, no copy-paste
✅ Pushed to GitHub before the time is up
```

---

---

## MORE REAL-TIME FAILURES — Advanced Scenarios

---

### FAILURE 8: `SSL Certificate Error`
```
Error: request to https://staging-api.com failed, reason: certificate has expired
Error: unable to verify the first certificate
```
**Cause:** Staging/test environments often use self-signed SSL certificates.

**Fix:**
```typescript
// In playwright.config.ts — ignore SSL errors for test environments
export default defineConfig({
  use: {
    baseURL: 'https://staging-api.com',
    ignoreHTTPSErrors: true,   // ← add this
  },
});

// Or per-request:
const response = await request.get('/endpoint', {
  ignoreHTTPSErrors: true
});
```

---

### FAILURE 9: `TypeError: Cannot read properties of undefined (reading 'id')`
```
TypeError: Cannot read properties of undefined (reading 'id')
  at tests/api/posts.spec.ts:45
```
**Cause:** The response JSON structure is different from what you assumed. `body.id` doesn't exist.

**Debug:**
```typescript
const response = await request.post(ENDPOINTS.users, { data: newUser });
const body = await response.json();

// Print EVERYTHING first
console.log('Status:', response.status());
console.log('Full body:', JSON.stringify(body, null, 2));

// Now match your code to the actual structure:
// If response is: { "data": { "id": 101 } }
// Then: body.data.id  — NOT body.id
```

---

### FAILURE 10: `expect(response.status()).toBe(200) — received 302`
```
expect(received).toBe(expected)
Expected: 200
Received: 302
```
**Cause:** The API redirected you (302 Redirect). Playwright doesn't follow redirects by default for API tests in some cases.

**Fix:**
```typescript
// Option 1: follow redirects manually
const response = await request.get('/endpoint', {
  maxRedirects: 5   // follow up to 5 redirects
});

// Option 2: assert the redirect itself
expect(response.status()).toBe(302);
expect(response.headers()['location']).toContain('/new-url');

// Option 3: use the redirect URL directly
```

---

### FAILURE 11: `Promise — async/await missing`
```
TypeError: response.json is not a function
// or
Pending Promise: [object Promise]
```
**Cause:** Forgot `await` before an async call.

**Fix:**
```typescript
// WRONG — missing await
test('test', async ({ request }) => {
  const response = request.get('/posts');      // ← missing await
  const body = response.json();               // ← missing await
  expect(body.id).toBe(1);
});

// CORRECT
test('test', async ({ request }) => {
  const response = await request.get('/posts');    // ← await
  const body = await response.json();              // ← await
  expect(body.id).toBe(1);
});
```
**Rule:** Every line that calls a Playwright method needs `await`.

---

### FAILURE 12: `Could not resolve host`
```
Error: getaddrinfo ENOTFOUND api.company.com
Error: connect ECONNREFUSED 127.0.0.1:3000
```
**Cause:** Wrong URL, typo in domain, server not running, or VPN required.

**Debug:**
```typescript
// Step 1: print what URL you're actually calling
console.log('baseURL:', process.env.BASE_URL || 'https://reqres.in');

// Step 2: open the URL in browser — does it load?
// Step 3: check for typos: 'https' not 'http', no trailing slash

// Step 4: test with curl
// curl https://reqres.in/api/users
// if curl fails → it's a network/VPN issue
// if curl works → it's your code

// Step 5: check playwright.config.ts baseURL
// Must NOT have trailing slash: 'https://reqres.in' ✅
// WRONG: 'https://reqres.in/'  ❌
```

---

### FAILURE 13: `Rate limiting — 429 during tests`
```
expect(received).toBe(expected)
Expected: 200
Received: 429
```
**Cause:** Too many requests sent too fast to a public API.

**Fix:**
```typescript
// Option 1: Add delay between tests
test.beforeEach(async () => {
  await new Promise(resolve => setTimeout(resolve, 500));  // 500ms pause
});

// Option 2: assert 429 is handled correctly
test('rate limit is enforced', async ({ request }) => {
  let responses = [];
  for (let i = 0; i < 20; i++) {
    const r = await request.get(ENDPOINTS.users);
    responses.push(r.status());
  }
  expect(responses).toContain(429);
});

// Option 3: use anyOf to not fail on rate limits
expect([200, 429]).toContain(response.status());
```

---

### FAILURE 14: `Token issues — 401 after first few tests`
```
// First 3 tests: 200 OK
// Tests 4+: 401 Unauthorized
```
**Cause:** Token expired mid-run, or token was only fetched once and is short-lived.

**Fix:**
```typescript
// WRONG: token fetched once in describe — might expire
test.describe('authenticated', () => {
  let token: string;

  test.beforeAll(async ({ request }) => {
    const r = await request.post('/auth/login', {
      data: { email: 'user@test.com', password: 'pass' }
    });
    token = (await r.json()).token;
    // ← if tests take 10 minutes and token lives 5 min → later tests fail
  });
});

// CORRECT: fetch fresh token before EACH test (safer)
test.beforeEach(async ({ request }) => {
  const r = await request.post('/auth/login', {
    data: { email: 'user@test.com', password: 'pass' }
  });
  const token = (await r.json()).token;
  // Use token in this specific test
});

// OR: add token to config so it applies globally
// playwright.config.ts:
extraHTTPHeaders: {
  'Authorization': `Bearer ${process.env.API_TOKEN}`,
}
// Set env var: API_TOKEN=mytoken npx playwright test
```

---

### FAILURE 15: `Assertion on array fails — wrong order`
```
expect(received).toEqual(expected)
Expected: [1, 2, 3]
Received: [3, 1, 2]
```
**Cause:** API returned array in different order.

**Fix:**
```typescript
// WRONG — exact order assertion
expect(body.ids).toEqual([1, 2, 3]);

// CORRECT — sort before comparing
expect(body.ids.sort()).toEqual([1, 2, 3].sort());

// ALSO CORRECT — check contains, not exact match
expect(body.ids).toContain(1);
expect(body.ids.length).toBe(3);

// Check each item matches a structure
body.data.forEach((user: any) => {
  expect(user).toHaveProperty('id');
  expect(user).toHaveProperty('email');
});
```

---

### FAILURE 16: `Test timeout exceeded`
```
Test timeout of 30000ms exceeded.
  at tests/api/users.spec.ts:23
```
**Cause:** The API took too long, or `await` is missing and test never resolved.

**Fix:**
```typescript
// Fix 1: increase timeout for slow APIs
test('slow API test', async ({ request }) => {
  test.setTimeout(60000);   // 60 seconds for this test
  const response = await request.get('/slow-endpoint');
  expect(response.status()).toBe(200);
});

// Fix 2: increase globally in playwright.config.ts
use: {
  actionTimeout: 30000,   // 30 seconds per action
}
timeout: 60000,           // 60 seconds per test

// Fix 3: check for missing await — test hangs waiting
const response = await request.get('/endpoint');  // ← await required
```

---

### FAILURE 17: `TypeScript compilation error`
```
error TS2339: Property 'users' does not exist on type '{...}'
error TS7006: Parameter 'request' implicitly has an 'any' type
```
**Cause:** TypeScript type errors — usually wrong import or wrong property access.

**Fix:**
```typescript
// Error: Property 'users' does not exist
// WRONG
import { ENDPOINTS } from '../utils/constants';
ENDPOINTS.users;   // ← if you named it 'USERS' not 'users'

// CORRECT: match exact name from constants.ts
export const ENDPOINTS = {
  users: '/api/users',   // lowercase 'users'
};

// Error: Parameter implicitly has 'any' type
// Add type annotation to any parameters
body.data.forEach((user: any) => {   // ← add ': any'
  expect(user.id).toBeDefined();
});

// Run TypeScript check without running tests:
npx tsc --noEmit   // shows all type errors
```

---

### FAILURE 18: `Cannot find module '../../utils/constants'`
```
Error: Cannot find module '../../utils/constants'
Require stack: - /tests/api/posts.spec.ts
```
**Cause:** Import path is wrong. The `../..` count doesn't match the actual folder depth.

**Fix:**
```typescript
// Your file is at: tests/api/posts.spec.ts
// constants.ts is at: utils/constants.ts

// Counting up from tests/api/:
// tests/api/ → tests/ (one ..)
// tests/ → root (two ..)
// root/utils/constants

// Correct import:
import { ENDPOINTS } from '../../utils/constants';
//                        ↑↑ two levels up from tests/api/

// If tests were at: tests/posts.spec.ts (one level)
import { ENDPOINTS } from '../utils/constants';
//                        ↑ one level up

// Quick check: look at your folder structure and count slashes
```

---

### FAILURE 19: `Tests pass locally, fail in GitHub Actions CI`
```
# Local: all pass
# GitHub Actions: Error: connect ECONNREFUSED
```
**Common causes:**
```
1. Missing Node.js setup in CI
2. npm install not run
3. Environment variables not set in CI
4. Playwright browsers not installed in CI
```

**Fix — GitHub Actions workflow:**
```yaml
# .github/workflows/playwright.yml
name: API Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - uses: actions/setup-node@v3
        with:
          node-version: 18       # ← required

      - run: npm ci               # ← install exact versions

      - run: npx playwright install   # ← even for API tests

      - run: npx playwright test
        env:
          # ⚠️ Set these in GitHub repo → Settings → Secrets
          API_TOKEN: ${{ secrets.API_TOKEN }}
          BASE_URL: ${{ vars.BASE_URL }}

      - uses: actions/upload-artifact@v3
        if: always()             # ← upload even if tests fail
        with:
          name: playwright-report
          path: playwright-report/
```

---

### FAILURE 20: `expect is not defined` or wrong expect import
```
ReferenceError: expect is not defined
```
**Cause:** Wrong import at the top of the test file.

**Fix:**
```typescript
// WRONG — missing expect in import
import { test } from '@playwright/test';

// CORRECT
import { test, expect } from '@playwright/test';
//             ↑ must include expect

// Also check: if using custom expect, import from correct place
import { expect } from '@playwright/test';   // Playwright's expect
// NOT: import { expect } from 'expect';     // wrong library
```

---

## QUICK DEBUGGING CHECKLIST

When any test fails, check these in order:

```typescript
// 1. Print status + full body
const response = await request.get('/endpoint');
console.log('Status:', response.status());
console.log('Headers:', response.headers());
const body = await response.text();   // text() works even if not JSON
console.log('Body:', body);

// 2. Check if response is JSON at all
if (response.headers()['content-type']?.includes('application/json')) {
  const json = await response.json();
  console.log(JSON.stringify(json, null, 2));
} else {
  console.log('NOT JSON:', await response.text());
}

// 3. Run with --debug to step through
npx playwright test --debug

// 4. Run only the failing test
npx playwright test --grep "name of failing test"

// 5. Check TypeScript errors
npx tsc --noEmit

// 6. If passes locally, fails in CI: check env vars
console.log('baseURL:', process.env.BASE_URL);
console.log('CI:', process.env.CI);
```

---

## FAILURE REFERENCE TABLE

| Error message | Most likely cause | First thing to check |
|---------------|------------------|---------------------|
| `ECONNREFUSED` | Wrong URL or server down | Open URL in browser |
| `401 Unauthorized` | Missing/wrong auth token | Add Authorization header |
| `403 Forbidden` | Wrong role/permission | Check API docs for required role |
| `404 Not Found` | Wrong endpoint path | Check ENDPOINTS constant |
| `415 Unsupported Media Type` | Missing Content-Type header | Add extraHTTPHeaders in config |
| `429 Too Many Requests` | Rate limited | Add delay between tests |
| `SSL error` | Self-signed certificate | Add `ignoreHTTPSErrors: true` |
| `Cannot find module` | Wrong import path | Count `../` levels |
| `TypeError: undefined` | Missing await | Add `await` before Playwright call |
| `Timeout exceeded` | Slow API or missing await | Increase timeout or fix await |
| `TypeScript error` | Wrong type/property | Run `npx tsc --noEmit` |
| `302 Redirect` | API redirecting | Follow redirect or assert 302 |
| `JSON parse error` | HTML returned instead of JSON | Print `response.text()` first |

---

*Assessment Guide — Playwright API Testing | TypeScript | 20 Real Failures + Fixes*
