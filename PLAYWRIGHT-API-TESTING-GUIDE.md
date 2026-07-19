# Playwright API Testing — Complete Setup & Reference Guide
## From Zero to Interview Ready | Node.js + Playwright + TypeScript

---

## TABLE OF CONTENTS

1. [Prerequisites & Installation](#1-prerequisites--installation)
2. [Project Setup](#2-project-setup)
3. [Core Concepts](#3-core-concepts)
4. [All HTTP Methods — Every Scenario](#4-all-http-methods--every-scenario)
5. [All Assertions — Every Pattern](#5-all-assertions--every-pattern)
6. [Authentication — Every Type](#6-authentication--every-type)
7. [Advanced Techniques](#7-advanced-techniques)
8. [Working With Test Cases Given to You](#8-working-with-test-cases-given-to-you)
9. [All Commands](#9-all-commands)
10. [Troubleshooting — Every Common Error](#10-troubleshooting--every-common-error)
11. [Interview Checklist](#11-interview-checklist)

---

## 1. Prerequisites & Installation

### Check Node.js is installed
```bash
node --version
```
**Expected:** `v18.x.x` or higher
**If missing:** Download from https://nodejs.org → choose LTS version → install

### Check npm is installed
```bash
npm --version
```
**Expected:** `9.x.x` or higher (comes with Node.js automatically)

### Install Playwright
```bash
npm init playwright@latest
```
When prompted:
```
Do you want to use TypeScript or JavaScript?  → TypeScript
Where to put your end-to-end tests?          → tests
Add a GitHub Actions workflow?               → n (No for now)
Install Playwright browsers?                 → n (No — we only need API, no browser)
```

### Install Playwright in an existing project
```bash
npm install --save-dev @playwright/test
```

---

## 2. Project Setup

### Folder Structure
```
your-project/
├── package.json               ← Node project config
├── playwright.config.ts       ← Playwright config (base URL, timeouts, etc.)
├── tsconfig.json              ← TypeScript config
└── tests/
    ├── api/
    │   ├── get.spec.ts        ← GET tests
    │   ├── post.spec.ts       ← POST tests
    │   ├── put.spec.ts        ← PUT + PATCH tests
    │   ├── delete.spec.ts     ← DELETE tests
    │   ├── auth.spec.ts       ← Authentication tests
    │   └── chained.spec.ts    ← End-to-end chained tests
    └── utils/
        ├── constants.ts       ← Base URL, endpoints, tokens
        └── helpers.ts         ← Reusable functions
```

### Create folders
```bash
mkdir -p tests/api tests/utils
```

### playwright.config.ts — Complete config
```typescript
import { defineConfig } from '@playwright/test';

export default defineConfig({
  // Where your tests live
  testDir: './tests',

  // Run tests in parallel
  fullyParallel: true,

  // Stop after first failure (change to false to run all)
  forbidOnly: !!process.env.CI,

  // Retry failed tests once in CI
  retries: process.env.CI ? 1 : 0,

  // How many tests run at the same time
  workers: process.env.CI ? 1 : undefined,

  // HTML report — open with: npx playwright show-report
  reporter: 'html',

  use: {
    // Base URL — all tests use this + their endpoint path
    baseURL: 'https://jsonplaceholder.typicode.com',

    // Print extra info on failure
    trace: 'on-first-retry',

    // Request timeout (10 seconds)
    actionTimeout: 10000,

    // Extra headers sent with every request
    extraHTTPHeaders: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      // Add auth header here if needed:
      // 'Authorization': `Bearer ${process.env.API_TOKEN}`,
    },
  },
});
```

### constants.ts — All shared values
```typescript
export const BASE_URL = 'https://jsonplaceholder.typicode.com';

export const ENDPOINTS = {
  posts:    '/posts',
  users:    '/users',
  comments: '/comments',
  todos:    '/todos',
};

export const AUTH = {
  apiKey:   process.env.API_KEY    || 'your-api-key',
  token:    process.env.API_TOKEN  || 'your-token',
  username: process.env.USERNAME   || 'your-username',
  password: process.env.PASSWORD   || 'your-password',
};

export const TIMEOUTS = {
  response: 3000,   // 3 seconds max response time
};
```

### helpers.ts — Reusable functions
```typescript
import { APIRequestContext } from '@playwright/test';

// Create a new post and return its ID
export async function createPost(request: APIRequestContext, title: string) {
  const response = await request.post('/posts', {
    data: { title, body: 'Test body', userId: 1 }
  });
  const body = await response.json();
  return body.id;
}

// Delete a post by ID
export async function deletePost(request: APIRequestContext, id: number) {
  return await request.delete(`/posts/${id}`);
}
```

### package.json scripts section
```json
{
  "scripts": {
    "test":          "npx playwright test",
    "test:api":      "npx playwright test tests/api/",
    "test:headed":   "npx playwright test --headed",
    "test:debug":    "npx playwright test --debug",
    "test:report":   "npx playwright show-report",
    "test:smoke":    "npx playwright test --grep @smoke"
  }
}
```

---

## 3. Core Concepts

### How Playwright API Tests Work
```typescript
import { test, expect } from '@playwright/test';

test('test name here', async ({ request }) => {
  // request = Playwright's built-in HTTP client
  // Already has baseURL and headers from playwright.config.ts

  const response = await request.get('/posts');      // send request
  expect(response.status()).toBe(200);               // assert status
  const body = await response.json();               // parse body
  expect(body).toHaveLength(100);                   // assert body
});
```

### The 3 Parts of Every Test
```typescript
// 1. SEND the request
const response = await request.get('/posts/1');

// 2. ASSERT the status
expect(response.status()).toBe(200);

// 3. ASSERT the body
const body = await response.json();
expect(body.id).toBe(1);
expect(body.title).not.toBe('');
```

### test.describe — Group Related Tests
```typescript
test.describe('Posts API', () => {

  test('GET all posts', async ({ request }) => { ... });
  test('GET post by ID', async ({ request }) => { ... });
  test('POST create post', async ({ request }) => { ... });

});
```

### beforeEach / afterEach — Setup and Cleanup
```typescript
test.describe('Posts CRUD', () => {
  let createdId: number;

  // Runs before each test
  test.beforeEach(async ({ request }) => {
    const response = await request.post('/posts', {
      data: { title: 'Setup Post', body: 'Body', userId: 1 }
    });
    const body = await response.json();
    createdId = body.id;
  });

  // Runs after each test — clean up test data
  test.afterEach(async ({ request }) => {
    await request.delete(`/posts/${createdId}`);
  });

  test('verify post exists', async ({ request }) => {
    const response = await request.get(`/posts/${createdId}`);
    expect(response.status()).toBe(200);
  });
});
```

---

## 4. All HTTP Methods — Every Scenario

### GET — All Scenarios

```typescript
import { test, expect } from '@playwright/test';

test.describe('GET Requests', () => {

  // ── SCENARIO 1: GET all resources ────────────────────────────────────────
  test('GET all posts returns 200 with 100 items', async ({ request }) => {
    const response = await request.get('/posts');

    expect(response.status()).toBe(200);
    expect(response.ok()).toBeTruthy();             // ok() = status 200-299

    const body = await response.json();
    expect(body).toHaveLength(100);
    expect(body[0].id).toBeDefined();
    expect(body[0].title).not.toBe('');
  });

  // ── SCENARIO 2: GET single resource by ID ────────────────────────────────
  test('GET post by ID returns correct data', async ({ request }) => {
    const response = await request.get('/posts/1');

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.id).toBe(1);
    expect(body.title).toBeDefined();
    expect(body.userId).toBe(1);
  });

  // ── SCENARIO 3: GET with query parameters ────────────────────────────────
  test('GET posts filtered by userId', async ({ request }) => {
    const response = await request.get('/posts', {
      params: { userId: 1 }                        // → /posts?userId=1
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body).toHaveLength(10);
    body.forEach((post: any) => {
      expect(post.userId).toBe(1);                 // every post must belong to user 1
    });
  });

  // ── SCENARIO 4: GET non-existent resource → 404 ──────────────────────────
  test('GET non-existent post returns 404', async ({ request }) => {
    const response = await request.get('/posts/99999');
    expect(response.status()).toBe(404);
  });

  // ── SCENARIO 5: GET and verify response headers ───────────────────────────
  test('GET response has correct Content-Type header', async ({ request }) => {
    const response = await request.get('/posts/1');
    expect(response.status()).toBe(200);
    expect(response.headers()['content-type']).toContain('application/json');
  });

  // ── SCENARIO 6: GET response time is acceptable ───────────────────────────
  test('GET responds within 3 seconds', async ({ request }) => {
    const start = Date.now();
    const response = await request.get('/posts');
    const duration = Date.now() - start;

    expect(response.status()).toBe(200);
    expect(duration).toBeLessThan(3000);
    console.log(`Response time: ${duration}ms`);
  });

  // ── SCENARIO 7: GET and validate specific fields ──────────────────────────
  test('GET post has all required fields', async ({ request }) => {
    const response = await request.get('/posts/1');
    const body = await response.json();

    // Assert all required fields exist
    expect(body).toHaveProperty('id');
    expect(body).toHaveProperty('title');
    expect(body).toHaveProperty('body');
    expect(body).toHaveProperty('userId');

    // Assert field types
    expect(typeof body.id).toBe('number');
    expect(typeof body.title).toBe('string');
    expect(typeof body.userId).toBe('number');
  });

  // ── SCENARIO 8: GET with invalid ID format ────────────────────────────────
  test('GET with non-numeric ID returns 404 or 400', async ({ request }) => {
    const response = await request.get('/posts/abc');
    expect([400, 404]).toContain(response.status());
  });

});
```

---

### POST — All Scenarios

```typescript
test.describe('POST Requests', () => {

  // ── SCENARIO 1: POST valid data → 201 Created ────────────────────────────
  test('POST creates resource and returns 201', async ({ request }) => {
    const response = await request.post('/posts', {
      data: {
        title: 'QA Practice Post',
        body: 'This is the test body',
        userId: 1
      }
    });

    expect(response.status()).toBe(201);
    const body = await response.json();
    expect(body.id).toBeDefined();                  // server assigned an ID
    expect(body.title).toBe('QA Practice Post');
    expect(body.userId).toBe(1);
  });

  // ── SCENARIO 2: POST and capture new ID ──────────────────────────────────
  test('POST returns server-generated ID', async ({ request }) => {
    const response = await request.post('/posts', {
      data: { title: 'New Post', body: 'Content', userId: 1 }
    });

    const body = await response.json();
    const newId = body.id;
    console.log(`New resource created with ID: ${newId}`);
    expect(newId).toBeGreaterThan(0);
  });

  // ── SCENARIO 3: POST missing required field → 400 ────────────────────────
  test('POST without title returns 400 or 201 (depends on API)', async ({ request }) => {
    const response = await request.post('/posts', {
      data: { body: 'No title here', userId: 1 }    // missing title
    });
    expect([400, 201]).toContain(response.status());  // real APIs return 400
  });

  // ── SCENARIO 4: POST empty body ───────────────────────────────────────────
  test('POST with empty body returns 400 or 201', async ({ request }) => {
    const response = await request.post('/posts', {
      data: {}
    });
    expect([400, 201]).toContain(response.status());
  });

  // ── SCENARIO 5: POST verifies response Content-Type ──────────────────────
  test('POST response Content-Type is JSON', async ({ request }) => {
    const response = await request.post('/posts', {
      data: { title: 'Test', body: 'Content', userId: 1 }
    });
    expect(response.status()).toBe(201);
    expect(response.headers()['content-type']).toContain('application/json');
  });

  // ── SCENARIO 6: POST duplicate data → 409 Conflict ───────────────────────
  test('POST duplicate email returns 409 (real APIs)', async ({ request }) => {
    const response = await request.post('/users', {
      data: { email: 'duplicate@example.com', name: 'Test User' }
    });
    // JSONPlaceholder doesn't enforce uniqueness — real APIs return 409
    expect([201, 409]).toContain(response.status());
  });

});
```

---

### PUT and PATCH — All Scenarios

```typescript
test.describe('PUT and PATCH Requests', () => {

  // ── SCENARIO 1: PUT full update → 200 ────────────────────────────────────
  test('PUT replaces full resource and returns 200', async ({ request }) => {
    const response = await request.put('/posts/1', {
      data: {
        id: 1,
        title: 'Replaced Title',
        body: 'Replaced body content',
        userId: 1
      }
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.title).toBe('Replaced Title');
    expect(body.body).toBe('Replaced body content');
  });

  // ── SCENARIO 2: PATCH partial update → 200 ───────────────────────────────
  test('PATCH updates only specified fields', async ({ request }) => {
    const response = await request.patch('/posts/1', {
      data: { title: 'Only Title Updated' }
    });

    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.title).toBe('Only Title Updated');
    expect(body.id).toBe(1);                        // id unchanged
  });

  // ── SCENARIO 3: PUT non-existent resource → 404 ──────────────────────────
  test('PUT non-existent resource returns 404 or 500', async ({ request }) => {
    const response = await request.put('/posts/99999', {
      data: { id: 99999, title: 'Test', body: 'Test', userId: 1 }
    });
    expect([200, 404, 500]).toContain(response.status());
  });

  // ── SCENARIO 4: PATCH with invalid data → 400 ────────────────────────────
  test('PATCH with wrong data type returns 400 or 200', async ({ request }) => {
    const response = await request.patch('/posts/1', {
      data: { userId: 'not-a-number' }              // wrong type
    });
    expect([200, 400]).toContain(response.status());
  });

});
```

---

### DELETE — All Scenarios

```typescript
test.describe('DELETE Requests', () => {

  // ── SCENARIO 1: DELETE existing resource → 200 or 204 ────────────────────
  test('DELETE returns 200 or 204', async ({ request }) => {
    const response = await request.delete('/posts/1');
    expect([200, 204]).toContain(response.status());
  });

  // ── SCENARIO 2: DELETE and verify empty response body ─────────────────────
  test('DELETE response body is empty', async ({ request }) => {
    const response = await request.delete('/posts/1');
    expect([200, 204]).toContain(response.status());
    const text = await response.text();
    expect(text).toBe('{}');                        // JSONPlaceholder returns {}
  });

  // ── SCENARIO 3: DELETE non-existent resource → 404 ───────────────────────
  test('DELETE non-existent resource returns 404', async ({ request }) => {
    const response = await request.delete('/posts/99999');
    expect([200, 404]).toContain(response.status());
  });

  // ── SCENARIO 4: DELETE then verify gone (chained) ─────────────────────────
  test('DELETE then GET returns 404 (real APIs)', async ({ request }) => {
    await request.delete('/posts/1');

    // On real APIs — resource should be gone
    const verify = await request.get('/posts/1');
    expect([200, 404]).toContain(verify.status());  // 404 on real APIs
  });

});
```

---

## 5. All Assertions — Every Pattern

```typescript
// ── STATUS CODE ───────────────────────────────────────────────────────────────
expect(response.status()).toBe(200);
expect(response.status()).not.toBe(500);
expect([200, 201]).toContain(response.status());    // accept either
expect(response.ok()).toBeTruthy();                 // true if status 200-299
expect(response.ok()).toBeFalsy();                  // true if status NOT 200-299

// ── BODY — PARSE FIRST ────────────────────────────────────────────────────────
const body = await response.json();                 // parse JSON body
const text = await response.text();                 // get raw string body

// ── BODY — FIELD VALUES ───────────────────────────────────────────────────────
expect(body.id).toBe(1);                            // exact match
expect(body.title).toBe('Hello World');
expect(body.active).toBe(true);
expect(body.id).not.toBe(0);                        // must not equal
expect(body.title).toBeDefined();                   // field exists
expect(body.title).not.toBeUndefined();
expect(body.deletedAt).toBeNull();                  // field is null
expect(body.id).toBeGreaterThan(0);                 // number comparison
expect(body.id).toBeGreaterThanOrEqualTo(1);
expect(body.count).toBeLessThan(100);
expect(body.title).toContain('Hello');              // string contains
expect(body.email).toMatch(/.*@.*\..*/);            // regex match
expect(body.title).not.toBe('');                    // not empty string

// ── BODY — ARRAYS ─────────────────────────────────────────────────────────────
expect(body).toHaveLength(100);                     // array has 100 items
expect(body.length).toBeGreaterThan(0);             // array not empty
expect(body).toContain('value');                    // array contains value
expect(body[0].id).toBeDefined();                   // first item has id
body.forEach((item: any) => {
  expect(item.userId).toBe(1);                      // every item matches
});

// ── BODY — HAS PROPERTY ───────────────────────────────────────────────────────
expect(body).toHaveProperty('id');                  // field exists
expect(body).toHaveProperty('id', 1);               // field exists with value
expect(body).toHaveProperty('user.address.city');   // nested field exists

// ── BODY — STRUCTURE (OBJECT MATCH) ──────────────────────────────────────────
expect(body).toMatchObject({                        // body contains these fields
  id: 1,
  title: expect.any(String),                        // any string value
  userId: expect.any(Number),
});

// ── HEADERS ───────────────────────────────────────────────────────────────────
expect(response.headers()['content-type']).toContain('application/json');
expect(response.headers()['cache-control']).toBeDefined();
expect(response.headers()['x-custom-header']).toBe('expected-value');

// ── RESPONSE TIME ─────────────────────────────────────────────────────────────
const start = Date.now();
const response = await request.get('/posts');
const duration = Date.now() - start;
expect(duration).toBeLessThan(3000);               // under 3 seconds
```

---

## 6. Authentication — Every Type

### Type 1: API Key in Header
```typescript
test('API key in header', async ({ request }) => {
  const response = await request.get('/protected', {
    headers: {
      'x-api-key': 'your-api-key-here'
    }
  });
  expect(response.status()).toBe(200);
});
```

### Type 2: Bearer Token
```typescript
test('Bearer token auth', async ({ request }) => {
  const response = await request.get('/protected', {
    headers: {
      'Authorization': 'Bearer your-token-here'
    }
  });
  expect(response.status()).toBe(200);
});
```

### Type 3: Basic Auth
```typescript
test('Basic auth', async ({ request }) => {
  // Playwright handles Basic Auth encoding automatically
  const context = await request.newContext({
    httpCredentials: {
      username: 'admin',
      password: 'secret'
    }
  });
  const response = await context.get('/protected');
  expect(response.status()).toBe(200);
});
```

### Type 4: Login → Token → Use Token (OAuth2 flow)
```typescript
test.describe('Authenticated API', () => {
  let authToken: string;

  // Login once before all tests in this describe block
  test.beforeAll(async ({ request }) => {
    const loginResponse = await request.post('/auth/login', {
      data: {
        username: 'admin',
        password: 'secret'
      }
    });
    expect(loginResponse.status()).toBe(200);
    const loginBody = await loginResponse.json();
    authToken = loginBody.token;                    // ← capture token
    console.log('Login successful, token received');
  });

  test('access protected resource with token', async ({ request }) => {
    const response = await request.get('/protected', {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
    expect(response.status()).toBe(200);
  });

  test('access another protected resource', async ({ request }) => {
    const response = await request.post('/protected/resource', {
      headers: { 'Authorization': `Bearer ${authToken}` },
      data: { name: 'Test' }
    });
    expect(response.status()).toBe(201);
  });
});
```

### Type 5: Add Auth to All Tests via playwright.config.ts
```typescript
// playwright.config.ts — add token to every request globally
use: {
  baseURL: 'https://api.example.com',
  extraHTTPHeaders: {
    'Authorization': `Bearer ${process.env.API_TOKEN}`,
    'x-api-key': process.env.API_KEY || '',
  },
},
```

### Testing Auth Failures
```typescript
test('no token returns 401', async ({ request }) => {
  // Create a new context with NO auth headers
  const noAuthContext = await request.newContext();
  const response = await noAuthContext.get('/protected');
  expect(response.status()).toBe(401);
});

test('wrong token returns 401 or 403', async ({ request }) => {
  const response = await request.get('/protected', {
    headers: { 'Authorization': 'Bearer invalid-token-xyz' }
  });
  expect([401, 403]).toContain(response.status());
});
```

---

## 7. Advanced Techniques

### Chained Test — Full CRUD Workflow
```typescript
test('full CRUD workflow: create → read → update → delete', async ({ request }) => {

  // ── STEP 1: CREATE ───────────────────────────────────────────────────────
  const createResponse = await request.post('/posts', {
    data: { title: 'E2E Test', body: 'Content', userId: 1 }
  });
  expect(createResponse.status()).toBe(201);
  const created = await createResponse.json();
  const newId = created.id;
  console.log(`Created post ID: ${newId}`);

  // ── STEP 2: READ ─────────────────────────────────────────────────────────
  const readResponse = await request.get(`/posts/${newId}`);
  expect([200, 404]).toContain(readResponse.status()); // 404 on JSONPlaceholder (doesn't persist)
  console.log(`Read post ID: ${newId}`);

  // ── STEP 3: UPDATE ───────────────────────────────────────────────────────
  const updateResponse = await request.patch(`/posts/${newId}`, {
    data: { title: 'Updated Title' }
  });
  expect([200, 404]).toContain(updateResponse.status());
  console.log(`Updated post ID: ${newId}`);

  // ── STEP 4: DELETE ───────────────────────────────────────────────────────
  const deleteResponse = await request.delete(`/posts/${newId}`);
  expect([200, 204, 404]).toContain(deleteResponse.status());
  console.log(`Deleted post ID: ${newId}`);

  // ── STEP 5: VERIFY GONE ──────────────────────────────────────────────────
  const verifyResponse = await request.get(`/posts/${newId}`);
  expect([200, 404]).toContain(verifyResponse.status()); // should be 404 on real APIs
});
```

### Data-Driven Testing with test.each
```typescript
// Run same test with multiple inputs
const validPostIds = [1, 2, 5, 10, 100];

for (const id of validPostIds) {
  test(`GET post with valid ID ${id} returns 200`, async ({ request }) => {
    const response = await request.get(`/posts/${id}`);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.id).toBe(id);
  });
}

// Negative data-driven
const invalidIds = [0, -1, 99999, 'abc'];

for (const id of invalidIds) {
  test(`GET post with invalid ID "${id}" returns 4xx`, async ({ request }) => {
    const response = await request.get(`/posts/${id}`);
    expect(response.status()).toBeGreaterThanOrEqualTo(400);
    expect(response.status()).toBeLessThan(500);
  });
}
```

### Tagging Tests for Selective Runs
```typescript
// Tag with @smoke, @regression, @auth etc.
test('GET all posts @smoke', async ({ request }) => { ... });
test('POST create post @smoke', async ({ request }) => { ... });
test('complex scenario @regression', async ({ request }) => { ... });

// Run only smoke tests:
// npx playwright test --grep @smoke
```

### Environment Variables for Sensitive Data
```typescript
// .env file (never commit to GitHub):
// API_TOKEN=your-secret-token
// BASE_URL=https://staging.api.example.com

// In playwright.config.ts:
import dotenv from 'dotenv';
dotenv.config();

use: {
  baseURL: process.env.BASE_URL || 'https://jsonplaceholder.typicode.com',
  extraHTTPHeaders: {
    'Authorization': `Bearer ${process.env.API_TOKEN || ''}`,
  },
}

// Install dotenv: npm install --save-dev dotenv
```

---

## 8. Working With Test Cases Given to You

### Step 1 — When They Give You an API, Do This First
```
1. Read the API docs / Swagger completely
2. Note: base URL, all endpoints, required fields, auth type
3. Ask: "What should I prioritise — happy path, negative, or both?"
4. Open playwright.config.ts → update baseURL
5. Update constants.ts → add endpoints and auth details
```

### Step 2 — If They Give You a Swagger/API Doc

Look for these things:
```
✅ Base URL           → put in playwright.config.ts baseURL
✅ Endpoints          → put in constants.ts ENDPOINTS object
✅ Auth required?     → put token in extraHTTPHeaders or .env
✅ Required fields    → use these in your POST/PUT test data
✅ Response codes     → use these in your expect().toBe() assertions
✅ Response schema    → use toMatchObject() to verify structure
```

### Step 3 — If They Give You a Real API with Auth

```typescript
// 1. Update playwright.config.ts
use: {
  baseURL: 'https://their-api.example.com',
  extraHTTPHeaders: {
    'Authorization': 'Bearer TOKEN_THEY_GAVE_YOU',
    'x-api-key': 'KEY_THEY_GAVE_YOU',
  },
},

// 2. Run a quick smoke test first to verify access
test('smoke: API is reachable and auth works', async ({ request }) => {
  const response = await request.get('/health');    // or any simple endpoint
  expect(response.ok()).toBeTruthy();
});
```

### Step 4 — If They Give You a Test Case Document

Common format:
```
Test Case ID: TC001
Title: Verify GET /users returns all users
Steps:
  1. Send GET request to /users
  2. Verify status code is 200
  3. Verify response contains array of users
  4. Verify each user has id, name, email fields
Expected Result: 200 OK with array of user objects
```

How to convert to Playwright:
```typescript
// TC001: Verify GET /users returns all users
test('TC001 - GET /users returns 200 with all users', async ({ request }) => {
  // Step 1: Send GET request to /users
  const response = await request.get('/users');

  // Step 2: Verify status code is 200
  expect(response.status()).toBe(200);

  // Step 3: Verify response contains array of users
  const body = await response.json();
  expect(Array.isArray(body)).toBeTruthy();
  expect(body.length).toBeGreaterThan(0);

  // Step 4: Verify each user has id, name, email
  body.forEach((user: any) => {
    expect(user).toHaveProperty('id');
    expect(user).toHaveProperty('name');
    expect(user).toHaveProperty('email');
  });
});
```

### Step 5 — Template for Any New Test Case

Copy this template and fill in the blanks:
```typescript
test('[TEST CASE ID] - [TEST CASE TITLE]', async ({ request }) => {
  // ARRANGE: set up data if needed
  const requestData = {
    // ... fill in request body from test case
  };

  // ACT: send the request
  const response = await request.[METHOD]('[ENDPOINT]', {
    data: requestData,                              // for POST/PUT/PATCH
    params: { key: 'value' },                      // for query params
    headers: { 'Custom-Header': 'value' },         // for extra headers
  });

  // ASSERT: verify everything from expected result
  expect(response.status()).toBe([EXPECTED_STATUS]);
  const body = await response.json();
  expect(body.[FIELD]).toBe([EXPECTED_VALUE]);
  // add more assertions from the test case expected result
});
```

---

## 9. All Commands

```bash
# ── SETUP ─────────────────────────────────────────────────────────────────────
npm init playwright@latest           # fresh setup
npm install --save-dev @playwright/test   # add to existing project
npm install                          # install all dependencies from package.json

# ── RUNNING TESTS ─────────────────────────────────────────────────────────────
npx playwright test                              # run ALL tests
npx playwright test tests/api/get.spec.ts       # run ONE file
npx playwright test tests/api/                  # run ALL files in folder
npx playwright test --grep "GET all posts"      # run by test name
npx playwright test --grep @smoke               # run by tag
npx playwright test -g "TC001"                  # run by test case ID

# ── RUNNING WITH OPTIONS ──────────────────────────────────────────────────────
npx playwright test --headed                    # open browser (for UI tests)
npx playwright test --debug                     # step through test one line at a time
npx playwright test --timeout=60000             # 60 second timeout
npx playwright test --retries=2                 # retry failed tests twice
npx playwright test --workers=1                 # run tests one at a time (no parallel)

# ── REPORTS ───────────────────────────────────────────────────────────────────
npx playwright show-report                      # open HTML report in browser
npx playwright test --reporter=list             # simple list output
npx playwright test --reporter=dot              # minimal dot output
npx playwright test --reporter=json             # JSON output for CI parsing

# ── SPECIFIC TEST SELECTION ───────────────────────────────────────────────────
npx playwright test --grep "POST"               # run all tests with "POST" in name
npx playwright test --grep-invert "slow"        # exclude tests with "slow" in name

# ── ENVIRONMENT VARIABLES ─────────────────────────────────────────────────────
API_TOKEN=mytoken npx playwright test           # pass token via env var
BASE_URL=https://staging.api.com npx playwright test   # override base URL

# ── CHECKING SETUP ────────────────────────────────────────────────────────────
npx playwright --version                        # check Playwright version
node --version                                  # check Node.js version
npm --version                                   # check npm version

# ── ON WINDOWS (PowerShell) ───────────────────────────────────────────────────
npx playwright test | Select-String "passed|failed|error"
```

---

## 10. Troubleshooting — Every Common Error

---

### ERROR: `node` not recognized
```
'node' is not recognized as an internal or external command
```
**Fix:** Install Node.js from https://nodejs.org → LTS version → restart terminal

---

### ERROR: `Cannot find module '@playwright/test'`
```
Error: Cannot find module '@playwright/test'
```
**Fix:**
```bash
npm install --save-dev @playwright/test
# or
npm install
```

---

### ERROR: `connect ECONNREFUSED` or `net::ERR_CONNECTION_REFUSED`
```
Error: connect ECONNREFUSED 127.0.0.1:3000
```
**Cause:** API server is not running or wrong baseURL
**Fix checklist:**
1. Check `baseURL` in `playwright.config.ts`
2. Open the URL in browser — does it load?
3. Check if you need VPN to reach the API

---

### ERROR: `401 Unauthorized` when you expect 200
```
expect(received).toBe(expected)
Expected: 200
Received: 401
```
**Fix checklist:**
1. Check auth header is set in `playwright.config.ts`
2. Check token has not expired
3. Check token format: `Bearer TOKEN` (with space, capital B)
4. Try the request in Postman with the same token

---

### ERROR: `Timeout 30000ms exceeded`
```
Test timeout of 30000ms exceeded
```
**Cause:** API is too slow or not responding
**Fix:**
```typescript
// In playwright.config.ts:
timeout: 60000,       // increase test timeout to 60 seconds
actionTimeout: 15000, // increase request timeout to 15 seconds

// Or on specific test:
test('slow test', async ({ request }) => {
  test.setTimeout(60000);
  ...
});
```

---

### ERROR: `response.json()` fails — not valid JSON
```
SyntaxError: Unexpected token < in JSON at position 0
```
**Cause:** API returned HTML (error page) instead of JSON
**Fix:**
```typescript
const text = await response.text();         // read as text first
console.log('Raw response:', text);         // see what actually came back
// then fix the URL or auth issue causing the HTML error page
```

---

### ERROR: `Cannot read properties of undefined`
```
TypeError: Cannot read properties of undefined (reading 'id')
```
**Cause:** `body.id` doesn't exist — field name wrong or API response different
**Fix:**
```typescript
const body = await response.json();
console.log('Full body:', JSON.stringify(body, null, 2));  // print entire response
// then fix your field path
```

---

### ERROR: Tests pass locally but fail in CI
**Common causes:**

| Cause | Fix |
|-------|-----|
| Token expired | Use short-lived tokens, fetch fresh token in `beforeAll` |
| Wrong base URL | Use environment variable `process.env.BASE_URL` |
| Network timeout | Increase `timeout` in `playwright.config.ts` |
| Tests depend on each other | Make each test independent — don't share state between tests |
| Missing `.env` file in CI | Add secrets in GitHub Actions / CI settings |

---

### ERROR: `toHaveLength` fails unexpectedly
```
Expected length: 100
Received: 0
```
**Cause:** Response returned empty array — check filter params or auth
**Fix:**
```typescript
const body = await response.json();
console.log(`Total items received: ${body.length}`);
console.log('First item:', body[0]);       // inspect what came back
```

---

## 11. Interview Checklist

### Before You Write a Single Line — Ask These
```
✅ What is the base URL of the API?
✅ Is authentication required? What type? (API key / Bearer token / Basic auth)
✅ Is there Swagger/API documentation?
✅ What format is the request body? (JSON / form-data)
✅ What are the required vs optional fields in POST/PUT?
✅ What status codes does this API return for errors?
✅ Should I cover only happy path, or negative tests too?
```

### Minimum Test Coverage for Any API
```
✅ GET all resources         → 200, non-empty array
✅ GET by valid ID           → 200, correct fields
✅ GET by invalid ID         → 404
✅ POST valid data           → 201, ID generated
✅ POST missing required     → 400
✅ PUT / PATCH update        → 200, data changed
✅ DELETE                    → 200 or 204
✅ Auth failure              → 401 (if API has auth)
✅ Response time             → under 3 seconds
✅ Content-Type header       → application/json
```

### What to Say Out Loud in the Interview
```
"Before I write tests I will:
1. Read the API docs to understand endpoints and required fields
2. Update playwright.config.ts with the base URL and auth
3. Start with GET tests (read-only, safest)
4. Move to POST/PUT/DELETE (mutation tests)
5. Add negative tests for each endpoint
6. Tag critical tests as @smoke for quick validation"
```

### Test Naming Convention
```typescript
// Format: [Method] [endpoint] [scenario] → [expected result]
test('GET /posts - valid ID - returns 200 with post data', ...)
test('GET /posts - invalid ID - returns 404', ...)
test('POST /posts - valid data - returns 201 with generated ID', ...)
test('POST /posts - missing title - returns 400', ...)
test('DELETE /posts - valid ID - returns 204', ...)

// Or use test case IDs from documents:
test('TC001 - GET all users returns 200', ...)
test('TC002 - GET user by ID returns correct data', ...)
```

### Quick Commands Reference
```bash
npx playwright test                          # run all tests
npx playwright test tests/api/get.spec.ts   # run one file
npx playwright test --grep "TC001"          # run by name/ID
npx playwright test --grep @smoke           # run smoke tests only
npx playwright show-report                  # view HTML report
```

---

*Guide version 1.0 — Playwright API Testing | Node.js + TypeScript*
