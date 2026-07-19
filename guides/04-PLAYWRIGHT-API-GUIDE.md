# Playwright API Testing — Complete Guide | Node.js + TypeScript

---

## Table of Contents

1. [What is Playwright API Testing?](#1-what-is-playwright-api-testing)
2. [Project Setup](#2-project-setup)
3. [playwright.config.ts for API Testing](#3-playwrightconfigts-for-api-testing)
4. [constants.ts — Centralizing Configuration](#4-constantsts--centralizing-configuration)
5. [HTTP Methods — All Scenarios](#5-http-methods--all-scenarios)
6. [All Assertions](#6-all-assertions)
7. [Authentication Types](#7-authentication-types)
8. [Chained Tests (POST → GET → DELETE)](#8-chained-tests-post--get--delete)
9. [Data-Driven Testing with Loops](#9-data-driven-testing-with-loops)
10. [Environment Variables](#10-environment-variables)
11. [Running Tests — All Commands](#11-running-tests--all-commands)
12. [Troubleshooting](#12-troubleshooting)
13. [Interview Q&A](#13-interview-qa)

---

## 1. What is Playwright API Testing?

Playwright is not just a UI automation tool. Its `APIRequestContext` enables direct HTTP calls to REST APIs without a browser — making it ideal for API contract testing, integration testing, and data setup/teardown in UI tests.

**Why use Playwright for API testing instead of Postman/Rest Assured?**

- Same toolchain as UI tests (TypeScript, test runner, assertions)
- Share authentication state between API and UI tests
- Built-in retry logic, parallel execution, and HTML reports
- No Java dependency — pure Node.js
- `expect()` assertions are richer and more readable than Rest Assured

---

## 2. Project Setup

### Step 1: Initialize a new Node.js project

```bash
mkdir playwright-api-tests
cd playwright-api-tests
npm init -y
```

### Step 2: Install Playwright

```bash
npm init playwright@latest
```

During the wizard:
- Choose **TypeScript**
- Choose `tests` as the test directory
- Say **No** to adding GitHub Actions (configure manually later)
- Say **No** to installing browsers (not needed for pure API testing)

### Step 3: Project structure after setup

```
playwright-api-tests/
├── tests/
│   ├── api/
│   │   ├── users.api.spec.ts
│   │   ├── auth.api.spec.ts
│   │   └── products.api.spec.ts
│   └── fixtures/
│       └── test-data.json
├── utils/
│   ├── constants.ts
│   └── api-helpers.ts
├── playwright.config.ts
├── package.json
└── tsconfig.json
```

### Step 4: Install additional dependencies

```bash
npm install --save-dev @types/node dotenv
```

### Step 5: tsconfig.json

```json
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

## 3. playwright.config.ts for API Testing

```typescript
import { defineConfig } from '@playwright/test';
import * as dotenv from 'dotenv';

dotenv.config();

export default defineConfig({
  // Directory where test files live
  testDir: './tests',

  // Run all tests in parallel
  fullyParallel: true,

  // Fail the build on CI if you accidentally left test.only
  forbidOnly: !!process.env.CI,

  // Retry on CI only
  retries: process.env.CI ? 2 : 0,

  // Number of parallel workers
  workers: process.env.CI ? 4 : undefined,

  // Reporter configuration
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['json', { outputFile: 'test-results/results.json' }],
  ],

  // Global test timeout (milliseconds)
  timeout: 30000,

  // Global expect timeout
  expect: {
    timeout: 10000,
  },

  // Shared settings for ALL projects/tests
  use: {
    // Base URL for all API requests
    baseURL: process.env.BASE_URL || 'https://jsonplaceholder.typicode.com',

    // Extra HTTP headers sent with every request
    extraHTTPHeaders: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
    },

    // Ignore HTTPS/SSL errors (useful for dev/staging environments)
    ignoreHTTPSErrors: true,
  },

  // Define separate projects for different environments or API versions
  projects: [
    {
      name: 'api-staging',
      use: {
        baseURL: process.env.STAGING_URL || 'https://staging.api.example.com',
        extraHTTPHeaders: {
          'Accept': 'application/json',
          'x-api-version': 'v2',
        },
      },
      testMatch: '**/tests/api/**/*.spec.ts',
    },
    {
      name: 'api-production',
      use: {
        baseURL: process.env.PROD_URL || 'https://api.example.com',
        extraHTTPHeaders: {
          'Accept': 'application/json',
        },
      },
      testMatch: '**/tests/api/**/*.spec.ts',
    },
  ],
});
```

---

## 4. constants.ts — Centralizing Configuration

```typescript
// utils/constants.ts

export const BASE_URL = process.env.BASE_URL || 'https://jsonplaceholder.typicode.com';

export const ENDPOINTS = {
  USERS: '/users',
  USER_BY_ID: (id: number) => `/users/${id}`,
  POSTS: '/posts',
  POST_BY_ID: (id: number) => `/posts/${id}`,
  TODOS: '/todos',
  COMMENTS: '/comments',
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
  },
  PRODUCTS: '/products',
} as const;

export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  UNPROCESSABLE_ENTITY: 422,
  TOO_MANY_REQUESTS: 429,
  INTERNAL_SERVER_ERROR: 500,
} as const;

export const TIMEOUTS = {
  DEFAULT: 10000,
  SLOW_ENDPOINT: 30000,
  FAST_ENDPOINT: 5000,
} as const;

export const TEST_USERS = {
  VALID: {
    username: 'john.doe@example.com',
    password: 'SecurePass123!',
  },
  INVALID: {
    username: 'wrong@example.com',
    password: 'wrongpassword',
  },
  ADMIN: {
    username: 'admin@example.com',
    password: 'AdminPass456!',
  },
} as const;

export const API_KEYS = {
  VALID: process.env.API_KEY || 'test-api-key-12345',
  INVALID: 'invalid-key-xyz',
} as const;
```

---

## 5. HTTP Methods — All Scenarios

### Full test file: `tests/api/users.api.spec.ts`

```typescript
import { test, expect, APIResponse } from '@playwright/test';
import { ENDPOINTS, HTTP_STATUS, TIMEOUTS } from '../../utils/constants';

// ─────────────────────────────────────────────
// GET Requests
// ─────────────────────────────────────────────

test.describe('GET /users', () => {

  test('GET all users — happy path', async ({ request }) => {
    const startTime = Date.now();

    const response: APIResponse = await request.get(ENDPOINTS.USERS);
    const elapsed = Date.now() - startTime;

    // Status assertion
    expect(response.status()).toBe(HTTP_STATUS.OK);

    // Response time assertion
    expect(elapsed).toBeLessThan(TIMEOUTS.DEFAULT);

    // Body assertions
    const users = await response.json();
    expect(Array.isArray(users)).toBeTruthy();
    expect(users.length).toBeGreaterThan(0);

    // Validate first user structure
    const firstUser = users[0];
    expect(firstUser).toHaveProperty('id');
    expect(firstUser).toHaveProperty('name');
    expect(firstUser).toHaveProperty('email');
    expect(firstUser).toHaveProperty('username');
    expect(typeof firstUser.id).toBe('number');
    expect(typeof firstUser.email).toBe('string');
  });

  test('GET user by ID — valid ID returns user', async ({ request }) => {
    const userId = 1;
    const response = await request.get(ENDPOINTS.USER_BY_ID(userId));

    expect(response.status()).toBe(HTTP_STATUS.OK);
    expect(response.headers()['content-type']).toContain('application/json');

    const user = await response.json();
    expect(user.id).toBe(userId);
    expect(user).toMatchObject({
      id: 1,
      name: expect.any(String),
      email: expect.stringContaining('@'),
    });
  });

  test('GET user by ID — 404 for non-existent user', async ({ request }) => {
    const response = await request.get(ENDPOINTS.USER_BY_ID(99999));

    expect(response.status()).toBe(HTTP_STATUS.NOT_FOUND);
  });

  test('GET users — response time under threshold', async ({ request }) => {
    const start = Date.now();
    const response = await request.get(ENDPOINTS.USERS);
    const duration = Date.now() - start;

    expect(response.ok()).toBeTruthy();
    expect(duration).toBeLessThan(3000); // Must respond within 3 seconds
  });

  test('GET users — validates Content-Type header', async ({ request }) => {
    const response = await request.get(ENDPOINTS.USERS);

    expect(response.headers()['content-type']).toContain('application/json');
  });
});

// ─────────────────────────────────────────────
// POST Requests
// ─────────────────────────────────────────────

test.describe('POST /posts', () => {

  test('POST — create resource — happy path', async ({ request }) => {
    const newPost = {
      title: 'My Test Post',
      body: 'This is the body content of the post.',
      userId: 1,
    };

    const response = await request.post(ENDPOINTS.POSTS, {
      data: newPost,
    });

    expect(response.status()).toBe(HTTP_STATUS.CREATED);

    const created = await response.json();
    expect(created.title).toBe(newPost.title);
    expect(created.body).toBe(newPost.body);
    expect(created.userId).toBe(newPost.userId);
    expect(created.id).toBeDefined();
    expect(typeof created.id).toBe('number');
  });

  test('POST — 400 Bad Request — missing required fields', async ({ request }) => {
    const incompletePost = {
      // Missing "title" which is required
      body: 'Body without a title',
    };

    const response = await request.post(ENDPOINTS.POSTS, {
      data: incompletePost,
    });

    // Depending on the API this may be 400 or 422
    expect([HTTP_STATUS.BAD_REQUEST, HTTP_STATUS.UNPROCESSABLE_ENTITY])
      .toContain(response.status());
  });

  test('POST — 400 Bad Request — invalid data types', async ({ request }) => {
    const invalidPost = {
      title: 12345,       // Should be string
      userId: 'notAnId', // Should be number
    };

    const response = await request.post(ENDPOINTS.POSTS, {
      data: invalidPost,
    });

    expect([HTTP_STATUS.BAD_REQUEST, HTTP_STATUS.UNPROCESSABLE_ENTITY])
      .toContain(response.status());
  });

  test('POST — 401 Unauthorized — no auth token', async ({ request }) => {
    // Simulate a POST to a protected endpoint without credentials
    const response = await request.post('/protected/posts', {
      data: { title: 'Test' },
      headers: {
        // Deliberately omit Authorization header
      },
    });

    expect(response.status()).toBe(HTTP_STATUS.UNAUTHORIZED);
  });
});

// ─────────────────────────────────────────────
// PUT Requests (full replacement)
// ─────────────────────────────────────────────

test.describe('PUT /posts/:id', () => {

  test('PUT — full update — happy path', async ({ request }) => {
    const updatedPost = {
      id: 1,
      title: 'Updated Title',
      body: 'Updated body content.',
      userId: 1,
    };

    const response = await request.put(ENDPOINTS.POST_BY_ID(1), {
      data: updatedPost,
    });

    expect(response.status()).toBe(HTTP_STATUS.OK);

    const result = await response.json();
    expect(result.title).toBe(updatedPost.title);
    expect(result.body).toBe(updatedPost.body);
  });

  test('PUT — 404 for non-existent resource', async ({ request }) => {
    const response = await request.put(ENDPOINTS.POST_BY_ID(99999), {
      data: { title: 'Update ghost', body: 'x', userId: 1 },
    });

    expect(response.status()).toBe(HTTP_STATUS.NOT_FOUND);
  });

  test('PUT — 400 for missing required fields', async ({ request }) => {
    // PUT requires the full resource — missing body field
    const response = await request.put(ENDPOINTS.POST_BY_ID(1), {
      data: { title: 'Only title, missing body and userId' },
    });

    expect([HTTP_STATUS.BAD_REQUEST, HTTP_STATUS.UNPROCESSABLE_ENTITY])
      .toContain(response.status());
  });
});

// ─────────────────────────────────────────────
// PATCH Requests (partial update)
// ─────────────────────────────────────────────

test.describe('PATCH /posts/:id', () => {

  test('PATCH — partial update — happy path', async ({ request }) => {
    const patch = { title: 'Patched Title Only' };

    const response = await request.patch(ENDPOINTS.POST_BY_ID(1), {
      data: patch,
    });

    expect(response.status()).toBe(HTTP_STATUS.OK);

    const result = await response.json();
    expect(result.title).toBe(patch.title);
    // Body should still be present (partial update, not full replacement)
    expect(result.body).toBeDefined();
  });

  test('PATCH — 404 for non-existent resource', async ({ request }) => {
    const response = await request.patch(ENDPOINTS.POST_BY_ID(99999), {
      data: { title: 'Ghost patch' },
    });

    expect(response.status()).toBe(HTTP_STATUS.NOT_FOUND);
  });
});

// ─────────────────────────────────────────────
// DELETE Requests
// ─────────────────────────────────────────────

test.describe('DELETE /posts/:id', () => {

  test('DELETE — happy path — 200 or 204', async ({ request }) => {
    const response = await request.delete(ENDPOINTS.POST_BY_ID(1));

    // Some APIs return 200 with body, others return 204 No Content
    expect([HTTP_STATUS.OK, HTTP_STATUS.NO_CONTENT]).toContain(response.status());
  });

  test('DELETE — 404 for non-existent resource', async ({ request }) => {
    const response = await request.delete(ENDPOINTS.POST_BY_ID(99999));

    expect(response.status()).toBe(HTTP_STATUS.NOT_FOUND);
  });

  test('DELETE — 401 Unauthorized — no auth', async ({ request }) => {
    const response = await request.delete('/protected/posts/1');

    expect(response.status()).toBe(HTTP_STATUS.UNAUTHORIZED);
  });
});
```

---

## 6. All Assertions

```typescript
import { test, expect } from '@playwright/test';

test('comprehensive assertion examples', async ({ request }) => {
  const response = await request.get('/users/1');

  // ── Status Assertions ────────────────────────────────────
  expect(response.status()).toBe(200);
  expect(response.ok()).toBeTruthy();                    // status 200–299
  expect(response.status()).not.toBe(404);

  // ── Body — primitive fields ──────────────────────────────
  const body = await response.json();
  expect(body.id).toBe(1);
  expect(body.name).toBe('Leanne Graham');
  expect(body.email).toContain('@');
  expect(typeof body.id).toBe('number');
  expect(typeof body.name).toBe('string');

  // ── Body — object shape (partial match) ─────────────────
  expect(body).toMatchObject({
    id: 1,
    name: expect.any(String),
    email: expect.stringContaining('@'),
    address: {
      city: expect.any(String),
    },
  });

  // ── Body — property existence ────────────────────────────
  expect(body).toHaveProperty('id');
  expect(body).toHaveProperty('address.city'); // nested path
  expect(body).toHaveProperty('id', 1);        // property + value

  // ── Arrays ──────────────────────────────────────────────
  const listResponse = await request.get('/users');
  const users = await listResponse.json();

  expect(Array.isArray(users)).toBeTruthy();
  expect(users).toHaveLength(10);
  expect(users.length).toBeGreaterThan(0);
  expect(users.length).toBeLessThanOrEqual(100);

  // Every item has an id
  users.forEach((user: { id: unknown; email: string }) => {
    expect(user).toHaveProperty('id');
    expect(user.email).toContain('@');
  });

  // Find a specific user in the array
  const foundUser = users.find((u: { id: number }) => u.id === 1);
  expect(foundUser).toBeDefined();

  // ── Headers ──────────────────────────────────────────────
  expect(response.headers()['content-type']).toContain('application/json');
  expect(response.headers()['content-type']).toMatch(/application\/json/);
  expect(response.headers()).toHaveProperty('content-type');

  // ── Soft Assertions — do not stop on failure ─────────────
  // All soft assertions are checked at the end of the test
  expect.soft(response.status()).toBe(200);
  expect.soft(body.id).toBe(1);
  expect.soft(body.name).toBeTruthy();

  // ── Negation ─────────────────────────────────────────────
  expect(body.name).not.toBeNull();
  expect(body.name).not.toBeUndefined();
  expect(response.status()).not.toBe(500);
  expect(users).not.toHaveLength(0);
});
```

---

## 7. Authentication Types

### 7.1 API Key Authentication

```typescript
// tests/api/auth-apikey.spec.ts
import { test, expect } from '@playwright/test';

test.describe('API Key Authentication', () => {

  test('API key in header — happy path', async ({ request }) => {
    const response = await request.get('/protected/data', {
      headers: {
        'x-api-key': process.env.API_KEY || 'your-valid-api-key',
      },
    });

    expect(response.status()).toBe(200);
  });

  test('API key in query param', async ({ request }) => {
    const response = await request.get('/protected/data', {
      params: {
        api_key: process.env.API_KEY || 'your-valid-api-key',
      },
    });

    expect(response.status()).toBe(200);
  });

  test('Invalid API key — 401 Unauthorized', async ({ request }) => {
    const response = await request.get('/protected/data', {
      headers: {
        'x-api-key': 'completely-invalid-key-xyz',
      },
    });

    expect(response.status()).toBe(401);

    const body = await response.json();
    expect(body).toHaveProperty('error');
    expect(body.error).toMatch(/unauthorized|invalid key/i);
  });

  test('Missing API key — 401 Unauthorized', async ({ request }) => {
    const response = await request.get('/protected/data');
    // No x-api-key header

    expect(response.status()).toBe(401);
  });
});
```

### 7.2 Bearer Token Authentication

```typescript
// tests/api/auth-bearer.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Bearer Token Authentication', () => {

  test('valid Bearer token — 200 OK', async ({ request }) => {
    const token = process.env.BEARER_TOKEN || 'valid-jwt-token-here';

    const response = await request.get('/api/profile', {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });

    expect(response.status()).toBe(200);
    const profile = await response.json();
    expect(profile).toHaveProperty('id');
    expect(profile).toHaveProperty('email');
  });

  test('expired Bearer token — 401', async ({ request }) => {
    const expiredToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired';

    const response = await request.get('/api/profile', {
      headers: {
        'Authorization': `Bearer ${expiredToken}`,
      },
    });

    expect(response.status()).toBe(401);
    const body = await response.json();
    expect(body.message).toMatch(/expired|invalid token/i);
  });

  test('no Authorization header — 401', async ({ request }) => {
    const response = await request.get('/api/profile');

    expect(response.status()).toBe(401);
  });

  test('malformed Authorization header — 401', async ({ request }) => {
    const response = await request.get('/api/profile', {
      headers: {
        'Authorization': 'NotBearer abc123',
      },
    });

    expect(response.status()).toBe(401);
  });
});
```

### 7.3 Basic Authentication

```typescript
// tests/api/auth-basic.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Basic Authentication', () => {

  // Helper: encode credentials as Base64
  function encodeBasicAuth(username: string, password: string): string {
    return Buffer.from(`${username}:${password}`).toString('base64');
  }

  test('valid Basic auth — 200 OK', async ({ request }) => {
    const credentials = encodeBasicAuth('admin', 'secret123');

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
});
```

### 7.4 OAuth2 — Login Flow to Token Extraction

```typescript
// tests/api/auth-oauth2.spec.ts
import { test, expect } from '@playwright/test';

let authToken: string;

test.describe('OAuth2 Login → Token → Protected Call', () => {

  // Step 1: Login and extract token
  test('POST /auth/login — obtain access token', async ({ request }) => {
    const response = await request.post('/auth/login', {
      data: {
        username: process.env.TEST_USERNAME || 'john.doe@example.com',
        password: process.env.TEST_PASSWORD || 'SecurePass123!',
      },
    });

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body).toHaveProperty('access_token');
    expect(body).toHaveProperty('token_type', 'Bearer');
    expect(body).toHaveProperty('expires_in');
    expect(typeof body.access_token).toBe('string');
    expect(body.access_token.length).toBeGreaterThan(10);

    // Save token for subsequent tests
    authToken = body.access_token;
  });

  // Step 2: Use token to access protected resource
  test('GET /api/me — use token from login', async ({ request }) => {
    // Note: In real usage, use fixtures to share state properly
    // This test depends on the token being set in the previous test
    expect(authToken).toBeDefined();

    const response = await request.get('/api/me', {
      headers: {
        'Authorization': `Bearer ${authToken}`,
      },
    });

    expect(response.status()).toBe(200);
    const profile = await response.json();
    expect(profile.email).toBe('john.doe@example.com');
  });

  // Better pattern: login within each test or use beforeAll fixture
  test('full OAuth2 flow in a single test', async ({ request }) => {
    // Step 1: Login
    const loginResponse = await request.post('/auth/login', {
      data: {
        username: 'john.doe@example.com',
        password: 'SecurePass123!',
      },
    });
    expect(loginResponse.status()).toBe(200);

    const { access_token } = await loginResponse.json();
    expect(access_token).toBeTruthy();

    // Step 2: Use token
    const profileResponse = await request.get('/api/me', {
      headers: { 'Authorization': `Bearer ${access_token}` },
    });
    expect(profileResponse.status()).toBe(200);

    // Step 3: Refresh token
    const refreshResponse = await request.post('/auth/refresh', {
      headers: { 'Authorization': `Bearer ${access_token}` },
    });
    expect(refreshResponse.status()).toBe(200);

    const { access_token: newToken } = await refreshResponse.json();
    expect(newToken).toBeTruthy();
    expect(newToken).not.toBe(access_token); // Should be a new token
  });

  // Testing auth failure scenarios
  test('POST /auth/login — wrong credentials — 401', async ({ request }) => {
    const response = await request.post('/auth/login', {
      data: {
        username: 'wrong@example.com',
        password: 'wrongpassword',
      },
    });

    expect(response.status()).toBe(401);

    const body = await response.json();
    expect(body).toHaveProperty('error');
    // Ensure the error message does NOT reveal which field was wrong (security)
    expect(body.error).not.toMatch(/password is incorrect/i);
    expect(body.error).toMatch(/invalid credentials/i);
  });

  test('POST /auth/login — account locked after brute force', async ({ request }) => {
    // Simulate multiple failed attempts
    for (let i = 0; i < 5; i++) {
      await request.post('/auth/login', {
        data: { username: 'user@example.com', password: 'wrongpassword' },
      });
    }

    // 6th attempt — should be rate limited or account locked
    const response = await request.post('/auth/login', {
      data: { username: 'user@example.com', password: 'wrongpassword' },
    });

    expect([HTTP_STATUS.TOO_MANY_REQUESTS, 423]).toContain(response.status());
  });
});

// Import missing constant
import { HTTP_STATUS } from '../../utils/constants';
```

### 7.5 Using `extraHTTPHeaders` for Global Auth

```typescript
// playwright.config.ts snippet — global auth for all requests
use: {
  baseURL: 'https://api.example.com',
  extraHTTPHeaders: {
    'Authorization': `Bearer ${process.env.GLOBAL_API_TOKEN}`,
    'x-api-key': process.env.API_KEY,
  },
},
```

---

## 8. Chained Tests (POST → GET → DELETE)

```typescript
// tests/api/chained-crud.spec.ts
import { test, expect } from '@playwright/test';
import { ENDPOINTS, HTTP_STATUS } from '../../utils/constants';

test.describe('Chained CRUD — POST → GET → PATCH → DELETE', () => {

  // Shared state across tests in this describe block
  let createdPostId: number;

  test('Step 1 — POST: create a new post', async ({ request }) => {
    const response = await request.post(ENDPOINTS.POSTS, {
      data: {
        title: 'Chained Test Post',
        body: 'Created for chained CRUD test.',
        userId: 1,
      },
    });

    expect(response.status()).toBe(HTTP_STATUS.CREATED);

    const body = await response.json();
    expect(body.title).toBe('Chained Test Post');
    expect(body.id).toBeDefined();

    createdPostId = body.id;
  });

  test('Step 2 — GET: retrieve the created post', async ({ request }) => {
    expect(createdPostId).toBeDefined();

    const response = await request.get(ENDPOINTS.POST_BY_ID(createdPostId));

    expect(response.status()).toBe(HTTP_STATUS.OK);

    const body = await response.json();
    expect(body.id).toBe(createdPostId);
    expect(body.title).toBe('Chained Test Post');
  });

  test('Step 3 — PATCH: update the created post', async ({ request }) => {
    expect(createdPostId).toBeDefined();

    const response = await request.patch(ENDPOINTS.POST_BY_ID(createdPostId), {
      data: { title: 'Updated Chained Post Title' },
    });

    expect(response.status()).toBe(HTTP_STATUS.OK);

    const body = await response.json();
    expect(body.title).toBe('Updated Chained Post Title');
  });

  test('Step 4 — DELETE: remove the created post', async ({ request }) => {
    expect(createdPostId).toBeDefined();

    const response = await request.delete(ENDPOINTS.POST_BY_ID(createdPostId));

    expect([HTTP_STATUS.OK, HTTP_STATUS.NO_CONTENT]).toContain(response.status());
  });

  test('Step 5 — GET after DELETE: confirm 404', async ({ request }) => {
    expect(createdPostId).toBeDefined();

    const response = await request.get(ENDPOINTS.POST_BY_ID(createdPostId));

    expect(response.status()).toBe(HTTP_STATUS.NOT_FOUND);
  });
});
```

### Using Fixtures for Proper State Sharing (Recommended)

```typescript
// tests/fixtures/api-fixtures.ts
import { test as base, expect } from '@playwright/test';
import { ENDPOINTS } from '../../utils/constants';

// Define a custom fixture type
type ApiFixtures = {
  createdPostId: number;
};

// Extend base test with custom fixture
export const test = base.extend<ApiFixtures>({
  // This fixture creates a post and yields its ID, then deletes it after the test
  createdPostId: async ({ request }, use) => {
    // SETUP: Create the post
    const createResponse = await request.post(ENDPOINTS.POSTS, {
      data: {
        title: 'Fixture Test Post',
        body: 'Managed by fixture',
        userId: 1,
      },
    });
    expect(createResponse.status()).toBe(201);
    const { id } = await createResponse.json();

    // YIELD the id to the test
    await use(id);

    // TEARDOWN: Delete the post after the test
    await request.delete(ENDPOINTS.POST_BY_ID(id));
  },
});

export { expect };

// Usage in tests:
// import { test, expect } from '../fixtures/api-fixtures';
//
// test('uses fixture', async ({ request, createdPostId }) => {
//   const response = await request.get(ENDPOINTS.POST_BY_ID(createdPostId));
//   expect(response.status()).toBe(200);
// });
```

---

## 9. Data-Driven Testing with Loops

```typescript
// tests/api/data-driven.spec.ts
import { test, expect } from '@playwright/test';
import { ENDPOINTS, HTTP_STATUS } from '../../utils/constants';

// ── Data-driven with forEach ─────────────────────────────────────────────────

const validUserIds = [1, 2, 3, 4, 5];

for (const userId of validUserIds) {
  test(`GET user with ID ${userId} — returns 200`, async ({ request }) => {
    const response = await request.get(ENDPOINTS.USER_BY_ID(userId));

    expect(response.status()).toBe(HTTP_STATUS.OK);

    const user = await response.json();
    expect(user.id).toBe(userId);
    expect(user.email).toContain('@');
  });
}

// ── Data-driven with test matrix ─────────────────────────────────────────────

type PostPayload = {
  scenario: string;
  payload: Record<string, unknown>;
  expectedStatus: number;
};

const postScenarios: PostPayload[] = [
  {
    scenario: 'valid post',
    payload: { title: 'Valid Title', body: 'Valid body', userId: 1 },
    expectedStatus: 201,
  },
  {
    scenario: 'empty title',
    payload: { title: '', body: 'Some body', userId: 1 },
    expectedStatus: 400,
  },
  {
    scenario: 'missing userId',
    payload: { title: 'Title', body: 'Body' },
    expectedStatus: 400,
  },
];

for (const { scenario, payload, expectedStatus } of postScenarios) {
  test(`POST /posts — ${scenario}`, async ({ request }) => {
    const response = await request.post(ENDPOINTS.POSTS, {
      data: payload,
    });

    expect(response.status()).toBe(expectedStatus);
  });
}

// ── Data from external JSON file ─────────────────────────────────────────────

import testData from '../fixtures/test-data.json';

for (const testCase of testData.users) {
  test(`GET user ${testCase.name} — ID ${testCase.id}`, async ({ request }) => {
    const response = await request.get(ENDPOINTS.USER_BY_ID(testCase.id));

    expect(response.status()).toBe(HTTP_STATUS.OK);
    const user = await response.json();
    expect(user.name).toBe(testCase.name);
  });
}
```

### tests/fixtures/test-data.json

```json
{
  "users": [
    { "id": 1, "name": "Leanne Graham", "email": "sincere@april.biz" },
    { "id": 2, "name": "Ervin Howell",  "email": "shanna@melissa.tv" },
    { "id": 3, "name": "Clementine Bauch", "email": "nathan@yesenia.net" }
  ],
  "invalidIds": [0, -1, 9999999, "abc", null]
}
```

---

## 10. Environment Variables

### .env (never commit to source control)

```env
BASE_URL=https://jsonplaceholder.typicode.com
STAGING_URL=https://staging.api.example.com
PROD_URL=https://api.example.com
API_KEY=your-api-key-here
BEARER_TOKEN=your-bearer-token-here
TEST_USERNAME=john.doe@example.com
TEST_PASSWORD=SecurePass123!
CI=false
```

### Loading in playwright.config.ts

```typescript
import * as dotenv from 'dotenv';
dotenv.config(); // Load .env before defineConfig

export default defineConfig({
  use: {
    baseURL: process.env.BASE_URL!,
  },
});
```

### Switching environments via CLI

```bash
# Use a specific .env file
dotenv -e .env.staging npx playwright test

# Or set inline
BASE_URL=https://staging.api.example.com npx playwright test

# On Windows PowerShell
$env:BASE_URL="https://staging.api.example.com"; npx playwright test
```

---

## 11. Running Tests — All Commands

```bash
# Run all tests
npx playwright test

# Run with visible output (verbose)
npx playwright test --reporter=list

# Run a specific file
npx playwright test tests/api/users.api.spec.ts

# Run by test name grep pattern
npx playwright test --grep "GET user by ID"

# Run by tag (use test.describe or @tag in test name)
npx playwright test --grep "@smoke"

# Run tests NOT matching a pattern
npx playwright test --grep-invert "@slow"

# Run against a specific project from playwright.config.ts
npx playwright test --project=api-staging

# Run in parallel with N workers
npx playwright test --workers=4

# Run in serial (one at a time)
npx playwright test --workers=1

# Show HTML report after tests
npx playwright show-report

# Show report from a specific output folder
npx playwright show-report playwright-report

# Debug mode (pauses at each action)
npx playwright test --debug

# Run with headed browser (not relevant for API tests but useful for mixed suites)
npx playwright test --headed

# Update snapshots
npx playwright test --update-snapshots

# List all tests without running them
npx playwright test --list

# Run with timeout override
npx playwright test --timeout=60000

# Run specific test by line number
npx playwright test tests/api/users.api.spec.ts:45
```

---

## 12. Troubleshooting

### Problem 1: Connection Refused (ECONNREFUSED)

**Error:**
```
Error: connect ECONNREFUSED 127.0.0.1:3000
```

**Causes and fixes:**
- The API server is not running — start it: `npm run start:server`
- Wrong `baseURL` in config — check `playwright.config.ts`
- Wrong port — verify the API listens on the configured port
- Firewall blocking the connection in CI — ensure the service is started in the CI pipeline before tests run

```yaml
# GitHub Actions example
jobs:
  test:
    steps:
      - name: Start API server
        run: npm run start:server &
      - name: Wait for server
        run: npx wait-on http://localhost:3000/health
      - name: Run tests
        run: npx playwright test
```

---

### Problem 2: 401 Unauthorized in CI but not locally

**Cause:** Token or API key is not set as a CI secret.

**Fix:**
1. Add secrets to GitHub Actions: Settings → Secrets → New repository secret
2. Reference in workflow:
```yaml
env:
  API_KEY: ${{ secrets.API_KEY }}
  BEARER_TOKEN: ${{ secrets.BEARER_TOKEN }}
```

---

### Problem 3: Test Timeout

**Error:**
```
Test timeout of 30000ms exceeded
```

**Fixes:**
```typescript
// Increase timeout for a specific test
test('slow endpoint test', async ({ request }) => {
  test.setTimeout(60000); // 60 seconds
  const response = await request.get('/slow-endpoint');
  // ...
});

// Increase globally in config
timeout: 60000,
```

---

### Problem 4: JSON Parse Error

**Error:**
```
SyntaxError: Unexpected token < in JSON at position 0
```

**Cause:** API returned HTML (usually an error page) instead of JSON.

**Fix:**
```typescript
test('safe JSON parsing', async ({ request }) => {
  const response = await request.get('/endpoint');

  // Check content type BEFORE parsing
  const contentType = response.headers()['content-type'];
  expect(contentType).toContain('application/json');

  // Only then parse
  const body = await response.json();
});
```

---

### Problem 5: Tests Fail in CI Only

**Common causes:**
1. **Race conditions** — add retries in `playwright.config.ts`: `retries: 2`
2. **Environment variables not set** — check CI secrets
3. **Timing** — API server not ready when tests start — add health check wait
4. **Different Node.js version** — pin version in CI: `node-version: '18'`
5. **Test ordering** — ensure tests are independent; avoid shared mutable state

---

## 13. Interview Q&A

**Q1. What is the difference between `request.get()` in Playwright and a browser fetch?**

**A:** `request.get()` in Playwright uses Playwright's `APIRequestContext`, which runs at the Node.js level — completely outside any browser sandbox. It does not inherit browser cookies, does not trigger browser events, and has no JavaScript execution context. It is purely an HTTP client like Axios or node-fetch, but integrated with Playwright's assertion library and test runner. `fetch()` in a browser runs inside the browser's rendering engine, is subject to CORS, and inherits the browser's session state.

---

**Q2. How do you share an authentication token between multiple API tests without repeating the login call?**

**A:** Use a `beforeAll` hook with a module-level variable, or better, use Playwright Fixtures. With fixtures you define a `authToken` fixture that logs in once per worker, yields the token to each test, and logs out in teardown. This avoids repeating the login in every test and handles cleanup automatically. Example:

```typescript
const test = base.extend<{ authToken: string }>({
  authToken: async ({ request }, use) => {
    const res = await request.post('/auth/login', { data: { username: 'u', password: 'p' } });
    const { access_token } = await res.json();
    await use(access_token);
  },
});
```

---

**Q3. What is `response.ok()` and how is it different from checking `response.status() === 200`?**

**A:** `response.ok()` returns `true` for any HTTP status code in the range 200–299. It is a convenience method for checking general success. `response.status() === 200` is a strict equality check for exactly 200. For a POST that returns 201 Created, `response.ok()` is `true` but `response.status() === 200` is `false`. Always use the specific status check when the exact code matters (e.g., asserting 201 for resource creation).

---

**Q4. How do you test response time (performance) in Playwright API tests?**

**A:** Playwright does not have a built-in response time assertion, but you can measure it manually:

```typescript
const start = Date.now();
const response = await request.get('/endpoint');
const duration = Date.now() - start;
expect(duration).toBeLessThan(2000); // Must respond within 2 seconds
```

For more granular measurement, use Playwright's `request.timing()` if available, or integrate with a performance tool. The key is asserting `duration` against an SLA threshold agreed with the team.

---

**Q5. What is `toMatchObject` and when should you use it instead of `toBe` or `toEqual`?**

**A:**
- `toBe(value)` — strict equality (uses `===`). For primitives.
- `toEqual(value)` — deep equality. Every property must match exactly, including all nested fields.
- `toMatchObject(subset)` — the actual object must contain at least all the properties in the expected object, but can have extra properties. This is ideal for API responses where the server adds auto-generated fields (`id`, `createdAt`) that you do not know at assertion time but want to assert the fields you do know.

```typescript
// toEqual would FAIL because createdAt is not in expected
// toMatchObject passes — it only checks the listed fields
expect(body).toMatchObject({
  title: 'My Post',
  userId: 1,
});
```

---

*End of File 04 — Playwright API Testing Complete Guide*
