# Pre-Run Checklist + All Commands
## Everything to Check Before Running | Every Command You Need | RestAssured + Playwright

---

## SECTION 1 — RESTASSURED PROJECT (Java + Maven + TestNG)

---

### PRE-RUN CHECKLIST — Do This Before Touching Any Command

```
Step 1: Check Java
Step 2: Check Maven (system or local)
Step 3: Verify all project files exist
Step 4: Check internet connection to API
Step 5: Verify resources folder is on classpath (for schema tests)
```

---

### Step 1 — Check Java

```powershell
java -version
```

**Expected output:**
```
openjdk version "11.0.x" 2024-xx-xx
OpenJDK Runtime Environment Temurin-11...
```

**If MISSING → install:**
```
1. Go to: https://adoptium.net/
2. Click: Latest LTS Release → JDK 17 or JDK 11
3. Download Windows .msi installer
4. Run installer → click Next all the way → Finish
5. Close and reopen all terminal windows
6. Run: java -version   ← should now show version
```

**If wrong version (need 11+):**
```
Current Java 8 or below will NOT compile the test code.
Install JDK 11 or 17 from adoptium.net
Multiple Java versions can coexist — the newest one takes priority
```

---

### Step 2 — Check Maven

```powershell
# Option A: System Maven (if installed globally)
mvn -version

# Option B: Local Maven downloaded in project folder
./apache-maven-3.9.6/bin/mvn -version
```

**Expected output:**
```
Apache Maven 3.9.x
Maven home: C:\...\apache-maven-3.9.6
Java version: 11.0.x
```

**If system Maven MISSING — download locally (safe, no global install):**
```powershell
# Run inside your project folder:
curl -L -o maven.zip "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
unzip maven.zip -d .
# Now use: ./apache-maven-3.9.6/bin/mvn  instead of  mvn
```

**JAVA_HOME must be set (Maven needs it):**
```powershell
# Check if set:
echo $env:JAVA_HOME    # PowerShell
echo %JAVA_HOME%       # CMD

# If empty — set it temporarily:
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-11.x.x"
# Or find your Java path: where java
```

---

### Step 3 — Verify Project Files Exist

Run these in your project folder:

```powershell
# Check all required files are present:
ls src/test/java/base/           # → BaseTest.java
ls src/test/java/tests/          # → GetTests.java, PostTests.java, PutTests.java, DeleteTests.java
ls src/test/java/utils/          # → ApiConstants.java
ls src/test/resources/           # → testng.xml, schemas/
ls src/test/resources/schemas/   # → post-schema.json
ls pom.xml                       # → pom.xml in root

# If any file is MISSING:
# Create the file from the INTELLIJ-RESTASSURED-SETUP-GUIDE.md
```

**Full expected project structure:**
```
project-root/
├── pom.xml                              ✅ must exist
├── apache-maven-3.9.6/                  ✅ if using local Maven
└── src/
    └── test/
        ├── java/
        │   ├── base/BaseTest.java        ✅ must exist
        │   ├── tests/
        │   │   ├── GetTests.java         ✅ must exist
        │   │   ├── PostTests.java        ✅ must exist
        │   │   ├── PutTests.java         ✅ must exist
        │   │   └── DeleteTests.java      ✅ must exist
        │   └── utils/ApiConstants.java   ✅ must exist
        └── resources/
            ├── testng.xml                ✅ must exist
            └── schemas/
                └── post-schema.json      ✅ must exist (for schema tests)
```

---

### Step 4 — Check Internet / API Access

```powershell
# Test the API is reachable:
curl https://jsonplaceholder.typicode.com/posts/1

# Expected output:
# {
#   "userId": 1,
#   "id": 1,
#   "title": "sunt aut facere...",
#   "body": "quia et suscipit..."
# }

# If curl fails:
# - Check internet connection
# - Try opening the URL in browser
# - Check if VPN is blocking public URLs
```

---

### Step 5 — Verify Schema Classpath (for schema validation tests)

This is needed for `matchesJsonSchemaInClasspath()` to work:

```powershell
# After running tests once, check schema was copied to target/:
ls target/test-classes/schemas/

# Expected: post-schema.json
# If MISSING: the resources folder isn't on the classpath
```

**Fix if schema not found:**
```xml
<!-- Add to pom.xml inside <build> tag: -->
<testResources>
    <testResource>
        <directory>src/test/resources</directory>
    </testResource>
</testResources>
```

---

## ALL RESTASSURED RUN COMMANDS

```powershell
# ════════════════════════════════════════════
# USING LOCAL MAVEN (./apache-maven-3.9.6/bin/mvn)
# ════════════════════════════════════════════

# Run ALL tests (uses testng.xml)
./apache-maven-3.9.6/bin/mvn test

# Clean old compiled files then run all
./apache-maven-3.9.6/bin/mvn clean test

# ── ONE CLASS ─────────────────────────────────────────────────────────────────
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests
./apache-maven-3.9.6/bin/mvn test -Dtest=PostTests
./apache-maven-3.9.6/bin/mvn test -Dtest=PutTests
./apache-maven-3.9.6/bin/mvn test -Dtest=DeleteTests
./apache-maven-3.9.6/bin/mvn test -Dtest=ChainedTests
./apache-maven-3.9.6/bin/mvn test -Dtest=DataDrivenTests

# ── ONE SPECIFIC METHOD ───────────────────────────────────────────────────────
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests#getAllPosts
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests#getPostById
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests#getPostsByUserId
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests#getNonExistentPost
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests#getPostMatchesSchema
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests#validatePostFieldsWithSoftAssert
./apache-maven-3.9.6/bin/mvn test -Dtest=PostTests#createPost
./apache-maven-3.9.6/bin/mvn test -Dtest=PutTests#updatePostWithPut
./apache-maven-3.9.6/bin/mvn test -Dtest=PutTests#updatePostTitleWithPatch
./apache-maven-3.9.6/bin/mvn test -Dtest=DeleteTests#deletePost

# ── MULTIPLE CLASSES AT ONCE ──────────────────────────────────────────────────
./apache-maven-3.9.6/bin/mvn test -Dtest="GetTests,PostTests"

# ── RUN BY GROUP (if @Test(groups="smoke") is tagged) ─────────────────────────
./apache-maven-3.9.6/bin/mvn test -Dgroups=smoke
./apache-maven-3.9.6/bin/mvn test -Dgroups=regression

# ── DOWNLOAD DEPENDENCIES ONLY (no test run) ──────────────────────────────────
./apache-maven-3.9.6/bin/mvn dependency:resolve

# ── CHECK COMPILATION ERRORS ONLY ────────────────────────────────────────────
./apache-maven-3.9.6/bin/mvn test-compile

# ── SHOW DEPENDENCY TREE (check for conflicts) ────────────────────────────────
./apache-maven-3.9.6/bin/mvn dependency:tree

# ── FILTER OUTPUT (PowerShell) ────────────────────────────────────────────────
./apache-maven-3.9.6/bin/mvn test 2>&1 | Select-String "Tests run|BUILD|FAIL|ERROR"

# ════════════════════════════════════════════
# IF MAVEN IS INSTALLED GLOBALLY: replace
# ./apache-maven-3.9.6/bin/mvn  with  mvn
# ════════════════════════════════════════════
mvn test
mvn test -Dtest=GetTests
mvn clean test
```

---

### Expected Outputs

```
# Full suite (all tests):
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

# GetTests only (6 tests):
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

# PostTests only (3 tests):
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

# BUILD FAILURE example:
Tests run: 6, Failures: 1, Errors: 0, Skipped: 0
[ERROR] GetTests.getAllPosts -- 1 expectation failed.
Expected status code <200> but was <404>
```

---

### View HTML Report

```powershell
# After any test run, open the report:
start target/surefire-reports/index.html

# Or navigate to it manually:
# project-root/target/surefire-reports/index.html
# Right-click → Open with → Chrome/Firefox
```

---

### First-Time Setup — Run in This Order

```powershell
# Navigate to project folder first:
cd "c:\Users\Kupeshanth\Desktop\Rest APi Sample"

# 1. Download all dependencies (first time: 2-5 minutes)
./apache-maven-3.9.6/bin/mvn dependency:resolve

# 2. Check compilation (no test run — finds syntax errors)
./apache-maven-3.9.6/bin/mvn test-compile

# 3. Run all tests
./apache-maven-3.9.6/bin/mvn test

# If Step 2 fails → check error → fix Java file → repeat
# If Step 3 fails → read [ERROR] lines → fix assertion/URL → repeat
```

---

## SECTION 2 — PLAYWRIGHT PROJECT (Node.js + TypeScript)

---

### PRE-RUN CHECKLIST

---

### Step 1 — Check Node.js

```powershell
node --version    # Need v18+
npm --version     # Comes with Node
```

**If MISSING:**
```
1. Go to: https://nodejs.org/
2. Click: LTS (Long Term Support)
3. Download Windows installer → install → restart terminal
4. Run: node --version  ← should show v18+
```

---

### Step 2 — Check Project Dependencies

```powershell
# Inside your Playwright project folder:
ls node_modules/    # Should exist with many folders inside

# If missing (never installed):
npm install         # downloads everything from package.json
npm ci              # cleaner install (uses package-lock.json exactly)
```

---

### Step 3 — Check Playwright Browsers

```powershell
npx playwright --version    # shows installed Playwright version
```

**If browsers not installed:**
```powershell
npx playwright install                   # install all browsers (Chromium, Firefox, WebKit)
npx playwright install chromium          # install only Chrome (smallest download)
npx playwright install --with-deps       # install browsers + OS-level dependencies (for CI)
```

---

### Step 4 — Check playwright.config.ts

```powershell
# Verify the config file exists:
ls playwright.config.ts

# Verify baseURL is correct inside it:
# Look for: baseURL: 'https://jsonplaceholder.typicode.com'
# or your API URL
```

---

### Step 5 — TypeScript Check (Optional)

```powershell
# Find TypeScript errors before running tests:
npx tsc --noEmit

# Expected: no output = no errors
# If errors appear: fix them before running tests
```

---

## ALL PLAYWRIGHT RUN COMMANDS

```powershell
# ── RUN ALL TESTS ─────────────────────────────────────────────────────────────
npx playwright test

# ── RUN ALL TESTS IN A FOLDER ─────────────────────────────────────────────────
npx playwright test tests/api/
npx playwright test tests/ui/

# ── RUN ONE FILE ──────────────────────────────────────────────────────────────
npx playwright test tests/api/posts.spec.ts
npx playwright test tests/ui/login.spec.ts

# ── RUN BY TEST NAME (grep) ───────────────────────────────────────────────────
npx playwright test --grep "GET all users"
npx playwright test --grep "POST creates"
npx playwright test --grep "returns 404"

# ── RUN BY TAG ────────────────────────────────────────────────────────────────
npx playwright test --grep @smoke
npx playwright test --grep @regression
npx playwright test --grep "@smoke|@sanity"        # smoke OR sanity
npx playwright test --grep-invert @slow            # exclude @slow tests

# ── RUN BY BROWSER ────────────────────────────────────────────────────────────
npx playwright test --project=chromium             # Chrome only
npx playwright test --project=firefox              # Firefox only
npx playwright test --project=webkit               # Safari only
npx playwright test --project="Mobile Chrome"      # mobile viewport

# ── RUN WITH OPTIONS ──────────────────────────────────────────────────────────
npx playwright test --headed                       # show browser window
npx playwright test --debug                        # step through test line by line
npx playwright test --ui                           # interactive UI mode (best for debugging)
npx playwright test --workers=1                    # serial (no parallel) — easier to debug
npx playwright test --workers=4                    # 4 tests at once
npx playwright test --retries=2                    # retry failed tests twice
npx playwright test --timeout=60000                # 60 second timeout per test

# ── REPORTING ─────────────────────────────────────────────────────────────────
npx playwright show-report                         # open last HTML report in browser
npx playwright test --reporter=list                # simple list in terminal
npx playwright test --reporter=dot                 # minimal dots
npx playwright test --reporter=html                # generate HTML (open with show-report)
npx playwright test --reporter=junit               # JUnit XML (for CI)

# ── VISUAL TESTING ────────────────────────────────────────────────────────────
npx playwright test --update-snapshots             # update screenshot baselines

# ── TYPESCRIPT ONLY ───────────────────────────────────────────────────────────
npx tsc --noEmit                                   # check for type errors without running

# ── ENVIRONMENT VARIABLES ─────────────────────────────────────────────────────
BASE_URL=https://staging.api.com npx playwright test
API_TOKEN=mytoken123 npx playwright test

# ── FILTER OUTPUT (PowerShell) ────────────────────────────────────────────────
npx playwright test 2>&1 | Select-String "passed|failed|error|skipped"
```

---

### Expected Outputs

```
# All tests passing:
Running 14 tests using 4 workers

  ✓ GET all users returns 200 (512ms)
  ✓ GET user by ID (198ms)
  ✓ POST creates user (341ms)
  ...
  14 passed (8.2s)

# With failures:
  ✓ GET all users returns 200 (512ms)
  ✗ GET user by ID (198ms)
    Error: expect(received).toBe(expected)
    Expected: 200
    Received: 404
  12 passed, 1 failed, 1 skipped
```

---

## SECTION 3 — QUICK DEBUG COMMANDS

### RestAssured — Debug a Failing Test

```powershell
# 1. Run just the failing test with full output
./apache-maven-3.9.6/bin/mvn test -Dtest=GetTests#getPostById

# 2. Read the error — look for these lines:
# [ERROR] GetTests.getPostById -- 1 expectation failed.
# Expected status code <200> but was <404>
# JSON path title doesn't match. Expected: Hello  Actual: World

# 3. Add logging to the test temporarily:
# given().log().all().when().get(...)...then().log().all()

# 4. Print response body in the test:
# System.out.println(response.asPrettyString());
```

### Playwright — Debug a Failing Test

```powershell
# 1. Run just the failing test
npx playwright test --grep "failing test name"

# 2. Run in debug mode (pauses at each step)
npx playwright test --debug --grep "failing test name"

# 3. Add to test file temporarily:
# console.log('Status:', response.status());
# console.log('Body:', JSON.stringify(await response.json(), null, 2));

# 4. Open the trace file (if trace is configured)
npx playwright show-trace test-results/test-name/trace.zip

# 5. Check TypeScript errors first
npx tsc --noEmit
```

---

## SECTION 4 — INSTALL EVERYTHING FROM SCRATCH

If you're on a completely fresh machine:

### For RestAssured Project

```powershell
# 1. Install Java 11+
# → https://adoptium.net/ → LTS → Windows installer → install

# 2. Navigate to project folder
cd "c:\Users\Kupeshanth\Desktop\Rest APi Sample"

# 3. Download Maven locally (no install needed)
curl -L -o maven.zip "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
unzip maven.zip -d .

# 4. Download all test dependencies
./apache-maven-3.9.6/bin/mvn dependency:resolve

# 5. Run all tests
./apache-maven-3.9.6/bin/mvn test

# Total time: ~10 minutes first run (downloading)
# Subsequent runs: ~15 seconds
```

### For Playwright Project

```powershell
# 1. Install Node.js 18+
# → https://nodejs.org/ → LTS → install

# 2. Navigate to project folder
cd "c:\your-playwright-project"

# 3. Install dependencies
npm install

# 4. Install browsers
npx playwright install chromium

# 5. Run tests
npx playwright test

# Total time: ~5 minutes first run
# Subsequent runs: ~5-10 seconds
```

---

## MASTER COMMAND REFERENCE TABLE

| Action | RestAssured | Playwright |
|--------|------------|-----------|
| Run all tests | `./apache-maven-3.9.6/bin/mvn test` | `npx playwright test` |
| Run one class/file | `mvn test -Dtest=GetTests` | `npx playwright test tests/api/get.spec.ts` |
| Run one method | `mvn test -Dtest=GetTests#getAllPosts` | `npx playwright test --grep "test name"` |
| Run by tag/group | `mvn test -Dgroups=smoke` | `npx playwright test --grep @smoke` |
| Clean + run | `mvn clean test` | `npx playwright test` (always clean) |
| Check compilation | `mvn test-compile` | `npx tsc --noEmit` |
| Download deps | `mvn dependency:resolve` | `npm install` |
| View report | Open `target/surefire-reports/index.html` | `npx playwright show-report` |
| Debug mode | Add `.log().all()` to given/then | `npx playwright test --debug` |
| Serial (no parallel) | Default (testng.xml parallel="none") | `npx playwright test --workers=1` |
| Filter output | `2>&1 \| Select-String "Tests run"` | `2>&1 \| Select-String "passed"` |

---

*Pre-Run Checklist + Commands | RestAssured + Playwright | Java + Node.js*
