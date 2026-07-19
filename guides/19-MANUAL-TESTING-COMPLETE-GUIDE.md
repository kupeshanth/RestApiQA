# Manual Testing — Complete Guide | Techniques + Test Cases + UAT + Exploratory

> Senior QA Interview Preparation — Manual Testing Deep Dive

---

## Table of Contents

1. [Manual vs Automation — When to Use Each](#section-1)
2. [Types of Manual Testing](#section-2)
3. [Smoke vs Sanity vs Regression](#section-3)
4. [Writing Test Cases](#section-4)
5. [Test Case Prioritisation](#section-5)
6. [Exploratory Testing](#section-6)
7. [UAT — User Acceptance Testing](#section-7)
8. [Test Execution and Reporting](#section-8)
9. [Cross-Browser and Device Testing](#section-9)
10. [Interview Q&A](#section-10)

---

## Section 1 — Manual vs Automation: When to Use Each {#section-1}

### Manual Testing is NOT Less Valuable

A common misconception is that automation is always superior to manual testing. That is wrong. Manual testing represents irreplaceable human judgement: the ability to question assumptions, explore unexpected paths, evaluate usability, and understand business context. No automation tool can ask "does this feel right to a real user?"

### Decision Framework: What to Automate vs Keep Manual

| Factor | Automate | Keep Manual |
|--------|----------|-------------|
| Test stability | Tests unlikely to change | Tests that change with every sprint |
| Repetition | Run hundreds of times | Run once or twice |
| Speed required | Fast feedback loops (CI/CD) | Not time-critical |
| Data volume | Data-driven tests, many combinations | Single scenario with human judgement |
| Business logic | Precise, rule-based logic | Requires interpretation |
| Exploratory | Not suitable | Ideal for manual |
| UI complexity | Simple, stable UI | Complex UX, layouts, animations |
| New feature | After it stabilises | During development and discovery |
| Cost-benefit | ROI positive after N runs | ROI never breaks even |
| Requirements clarity | Requirements clear and stable | Requirements vague or changing |

### Rule of Thumb

- **Stable, repetitive, data-heavy** → Automate (regression suites, smoke packs, API checks)
- **New features, usability, edge discovery** → Manual (exploratory, UAT, first-pass testing)

### Why Exploratory and UAT Cannot Be Automated

**Exploratory testing** is simultaneous learning, design, and execution. The tester learns about the system as they test, and uses that knowledge to change what they test next. An automation script cannot adapt in real time. If it discovers something unexpected, it cannot follow the thread.

**UAT (User Acceptance Testing)** validates that the software meets business requirements from a human perspective. Business users, product owners, or real end users perform this. Automation can check function but not meaning. A checkout flow might be technically correct but confusing to actual customers — only a human can catch that.

---

## Section 2 — Types of Manual Testing {#section-2}

### 1. Smoke Testing

**Description:** A shallow, wide test pass to verify that the build is stable enough to test further. Covers only the most critical paths — no deep testing.

**When Used:** Immediately after a new build is received from development. Before any other testing begins.

**Goal:** Go/no-go decision. Is this build testable?

**Example:** After a new release is deployed to the test environment:
- Can the application launch?
- Can a user log in?
- Does the home page load?
- Can a user navigate to the main features?

If any of these fail, the build is returned to development. No further testing is done.

---

### 2. Sanity Testing

**Description:** A focused, narrow test on a specific fix or new functionality. Verifies that the bug fix has actually resolved the issue and has not broken adjacent functionality.

**When Used:** After a bug fix is deployed. Before deeper regression testing.

**Goal:** Confirm this specific thing works. Is this fix correct?

**Example:** Bug reported: "Login fails when username has uppercase letters."
Fix delivered. Sanity test:
- Log in with "Admin@example.com" — does it work?
- Log in with "ADMIN@EXAMPLE.COM" — does it work?
- Log in with mixed case passwords — does it work?

Only the affected area is tested. This is not regression.

---

### 3. Regression Testing

**Description:** A comprehensive test pass to verify that existing functionality has not been broken by new changes. Covers the entire application or a risk-assessed subset.

**When Used:** After any code change, bug fix, or new feature is added. Before every release.

**Goal:** Confirm nothing that was working before is now broken.

**Example:** After a new "remember me" feature is added to login:
- Test all login scenarios (success, failure, locked account, wrong password)
- Test all areas that share session management code
- Test areas that have historically been fragile

---

### 4. UAT (User Acceptance Testing)

**Description:** Business users or product owners validate that the software meets agreed requirements before it goes live. This is the final gate before production.

**When Used:** After QA has signed off. Before production release.

**Goal:** Business acceptance. Does this meet what we agreed to build?

**Example:** Business stakeholders test the new student enrolment flow:
- Can a school admin create a new student account using real school data?
- Does the system accept the date format used by their country?
- Are the email notifications in the correct language and tone?

---

### 5. Exploratory Testing

**Description:** Simultaneous learning, test design, and test execution. The tester uses their domain knowledge and curiosity to investigate the system without a fixed test script.

**When Used:** New features, unclear requirements, complex user flows, after major refactors.

**Goal:** Discover what is not known. Find defects that scripted tests would miss.

**Example:** New PDF export feature — explore what happens with:
- Very long document names
- Special characters in filenames
- Empty documents
- Documents with embedded images
- Concurrent exports by multiple users

---

### 6. Ad-hoc Testing

**Description:** Random, unstructured testing without a plan or documentation. Relies on tester intuition.

**When Used:** When time is very limited, or when testing a brand new feature for the very first time to get a feel for it.

**Goal:** Quick discovery. Not a substitute for planned testing.

**Example:** Developer hands you a new feature and asks "can you have a quick look?" You click around, enter random data, try to break it. No charter, no notes — pure intuition.

**Note:** Ad-hoc has value but is not a testing strategy. Use exploratory testing (with structure) for serious coverage.

---

### 7. UI/UX Testing

**Description:** Verifying the visual design, usability, layout, and user experience. Not functional correctness — can a user complete their goal comfortably?

**When Used:** New UI designs, redesigns, mobile responsiveness, design system changes.

**Goal:** Usability. Is this easy to use? Does it match the design specification?

**Example:**
- Does the button label clearly communicate the action?
- Is the error message shown next to the relevant field?
- Does the form flow feel natural (logical tab order)?
- Are click targets large enough on mobile?

---

### 8. Cross-Browser Testing

**Description:** Verifying the application behaves consistently across different browsers.

**When Used:** Before every release, and when CSS or JavaScript is changed.

**Goal:** No browser-specific defects reach users.

**Browsers to cover:** Chrome, Firefox, Safari, Edge (and mobile: Chrome on Android, Safari on iOS)

**Example:** A date picker works in Chrome but shows no calendar in Safari because of a missing CSS prefix. Cross-browser testing catches this.

---

### 9. Compatibility Testing

**Description:** Verifying the application works correctly across different operating systems, devices, and screen resolutions.

**When Used:** When targeting multiple platforms (Windows, Mac, Android, iOS) or screen sizes.

**Goal:** Works for all users regardless of their device.

**Example:** Testing a web app on:
- Windows 11 / Chrome
- macOS Ventura / Safari
- Android 14 / Chrome mobile
- iPhone 15 / Safari mobile

---

### 10. Localisation Testing

**Description:** Verifying the application works correctly for different languages, regions, date formats, number formats, and currencies.

**When Used:** When releasing to international markets or supporting multiple languages.

**Goal:** Native-feeling experience for each locale.

**Example:**
- Date: 01/12/2024 — is this January 12 (US) or December 1 (UK)?
- Currency: Does 1,000.00 display correctly vs 1.000,00 (European format)?
- RTL languages (Arabic, Hebrew): does the layout mirror correctly?
- String truncation: German words are much longer — does the UI still fit?

---

## Section 3 — Smoke vs Sanity vs Regression (Always Asked) {#section-3}

### Comparison Table

| Aspect | Smoke Testing | Sanity Testing | Regression Testing |
|--------|---------------|----------------|--------------------|
| **Purpose** | Is the build stable enough to test? | Has this specific fix worked? | Has anything else broken? |
| **Scope** | Wide, shallow — whole application | Narrow, deep — specific area only | Wide, deep — whole application |
| **When done** | After every new build | After a bug fix or small change | After any code change, before release |
| **Who does it** | QA Team | QA Team | QA Team (often automated) |
| **Pass criteria** | Core flows work, app is stable | The fixed issue is resolved | All previously passing tests still pass |
| **Fail criteria** | Any core flow is broken | The fix did not work | Any previously passing test now fails |
| **Test depth** | Superficial — just enough | Targeted — just the fix area | Comprehensive — full suite |
| **Duration** | Short (minutes to an hour) | Very short (minutes) | Long (hours to days) |
| **Test cases** | Subset of critical tests | 5–10 focused tests | Full regression suite |

### Real Examples Using Login Feature

**Smoke Test (new build received):**
1. Open the application — does it load?
2. Navigate to the login page — does it display?
3. Enter valid credentials and click login — does it succeed?
4. Does the dashboard appear?

If any of these fail → build rejected.

**Sanity Test (bug fix: "login fails for users with + in their email address"):**
1. Log in with email: `user+test@example.com` — should succeed
2. Log in with email: `user+work+home@example.com` — should succeed
3. Log in with standard email: `user@example.com` — should still work
4. Confirm session is created correctly after login

If fix works → move to regression. If not → return to developer.

**Regression Test (new feature added: biometric login option):**
1. Run full login test suite: valid credentials, invalid credentials, locked account, expired password, forgot password
2. Run session management tests: timeout, concurrent sessions
3. Run SSO/OAuth tests if applicable
4. Run all tests that interact with the authentication service

---

## Section 4 — Writing Test Cases {#section-4}

### Test Case Fields

| Field | Description |
|-------|-------------|
| **Test Case ID** | Unique identifier (e.g. TC-LOGIN-001) |
| **Title** | One-line summary of what is being tested |
| **Module** | Feature area (e.g. Login, Cart, Checkout) |
| **Preconditions** | State that must exist before execution |
| **Test Steps** | Numbered, specific, single-action steps |
| **Test Data** | Input data required (usernames, passwords, values) |
| **Expected Result** | Precise, verifiable outcome |
| **Actual Result** | Filled in during execution |
| **Status** | Pass / Fail / Blocked / Not Executed |
| **Priority** | P1 / P2 / P3 / P4 |
| **Severity** | Critical / High / Medium / Low |
| **Author** | Who wrote it |
| **Last Updated** | Date |

### Title Formula

```
[Feature] [Action] [Condition] [Expected Outcome]
```

Examples:
- "Login form submits successfully with valid email and password"
- "Login page displays error message when password is incorrect"
- "Forgot Password link sends reset email to registered address"

Avoid: "Test login", "Verify user", "Check form" — these are vague and untestable.

### Steps Format Rules

1. One action per step — never combine "click and verify"
2. Specific enough that anyone (even a non-tester) can follow
3. Use imperative voice: "Click", "Enter", "Navigate", "Select"
4. Include exact data values when relevant
5. Order matters — dependencies must be in sequence

Bad step: "Login and verify dashboard loads"
Good steps:
1. Enter "testuser@example.com" in the Email field
2. Enter "Password123!" in the Password field
3. Click the "Log In" button
4. Verify the page title is "Dashboard"
5. Verify the user's name appears in the top-right navigation

### Expected Result Rules

- Precise and verifiable — not "it works" or "it succeeds"
- State exactly what should be visible, returned, or stored
- Include HTTP status codes for API tests
- Include exact error messages if known

Bad: "User is logged in successfully"
Good: "User is redirected to /dashboard, the page displays 'Welcome, Jane Smith', and the navigation shows a 'Log Out' link"

---

### Example Test Case 1: Login — Successful Authentication

| Field | Value |
|-------|-------|
| **ID** | TC-LOGIN-001 |
| **Title** | Login form authenticates user with valid email and password |
| **Module** | Authentication / Login |
| **Priority** | P1 |
| **Severity** | Critical |
| **Preconditions** | 1. User account exists: email=`jane.smith@example.com`, password=`SecurePass123!`  2. User is not currently logged in  3. Application is accessible at `https://app.example.com/login` |
| **Test Data** | Email: `jane.smith@example.com` / Password: `SecurePass123!` |

**Steps:**
1. Open browser and navigate to `https://app.example.com/login`
2. Verify the Login page displays with Email field, Password field, and Log In button
3. Click the Email field and enter `jane.smith@example.com`
4. Click the Password field and enter `SecurePass123!`
5. Click the "Log In" button
6. Observe the result

**Expected Result:**
- User is redirected to `https://app.example.com/dashboard`
- The page heading reads "Dashboard"
- The navigation bar displays "Jane Smith" in the top-right corner
- A "Log Out" link is visible in the navigation
- No error messages are displayed
- HTTP response is 200 OK

**Actual Result:** *(Filled during execution)*
**Status:** *(Pass / Fail)*

---

### Example Test Case 2: Login — Incorrect Password

| Field | Value |
|-------|-------|
| **ID** | TC-LOGIN-002 |
| **Title** | Login form displays error message when incorrect password is entered |
| **Module** | Authentication / Login |
| **Priority** | P1 |
| **Severity** | High |
| **Preconditions** | 1. User account exists: email=`jane.smith@example.com`  2. User is on the Login page |
| **Test Data** | Email: `jane.smith@example.com` / Password: `WrongPassword99!` |

**Steps:**
1. Navigate to `https://app.example.com/login`
2. Enter `jane.smith@example.com` in the Email field
3. Enter `WrongPassword99!` in the Password field
4. Click the "Log In" button
5. Observe the result

**Expected Result:**
- User remains on the Login page (`/login`)
- An error message is displayed: "Invalid email or password. Please try again."
- The Password field is cleared
- The Email field retains the entered email address
- No account lockout occurs (this is the first failed attempt)
- HTTP response is 401 Unauthorized

**Actual Result:** *(Filled during execution)*
**Status:** *(Pass / Fail)*

---

### Example Test Case 3: Forgot Password — Reset Email Sent

| Field | Value |
|-------|-------|
| **ID** | TC-LOGIN-003 |
| **Title** | Forgot Password link sends password reset email to registered address |
| **Module** | Authentication / Forgot Password |
| **Priority** | P2 |
| **Severity** | High |
| **Preconditions** | 1. User account exists with email `jane.smith@example.com`  2. Access to email inbox for `jane.smith@example.com`  3. User is on the Login page |
| **Test Data** | Email: `jane.smith@example.com` |

**Steps:**
1. Navigate to `https://app.example.com/login`
2. Click the "Forgot Password?" link
3. Verify the Forgot Password page loads with an email input field
4. Enter `jane.smith@example.com` in the Email field
5. Click the "Send Reset Link" button
6. Observe the on-screen result
7. Open the email inbox for `jane.smith@example.com`
8. Locate the password reset email
9. Click the reset link in the email

**Expected Result:**
- On-screen: A confirmation message displays "A password reset link has been sent to jane.smith@example.com"
- Email received: Subject line includes "Reset your password"
- Email received: Within 5 minutes of request
- Reset link in email is functional and navigates to the password reset form
- Reset link expires after 24 hours (verify by using an old link)

**Actual Result:** *(Filled during execution)*
**Status:** *(Pass / Fail)*

---

### Common Test Case Mistakes

| Mistake | Why It's Wrong | Fix |
|---------|---------------|-----|
| Vague steps: "Test login" | Cannot be reproduced by anyone else | Write exact, numbered actions |
| Combined actions: "Click login and check dashboard" | One step hides two verifications | Split into separate step and expected result |
| Missing preconditions | Test starts in wrong state | Always state what must be true before step 1 |
| Vague expected results: "It works" | Cannot determine pass/fail | State exactly what is visible, returned, or stored |
| No test data | Tester makes up data, inconsistent results | Specify exact inputs |
| Skipping negative scenarios | Only testing happy path | Always add at least one negative test per feature |
| No ID | Cannot reference in defect reports | Always assign a unique ID |

---

## Section 5 — Test Case Prioritisation {#section-5}

### Priority Levels

| Priority | Label | Meaning | Examples |
|----------|-------|---------|---------|
| **P1** | Critical | Must test first. Release blocker if it fails. Core business function. | Login, payment, data save, core feature |
| **P2** | High | Important. Should be tested before release. Significant user impact if broken. | Profile update, search, notifications |
| **P3** | Medium | Should be tested but not a release blocker. Workarounds exist. | UI formatting, non-critical filters, sorting |
| **P4** | Low | Test if time permits. Minor inconvenience. | Cosmetic issues, edge case messages, tooltips |

### Risk-Based Prioritisation

When time is limited, use risk = **Probability of Failure × Business Impact**

```
Risk Score = Probability (1-5) × Impact (1-5)

Score 20-25: Test immediately (P1)
Score 10-19: Test before release (P2)
Score 5-9:  Test if time allows (P3)
Score 1-4:  Test in next cycle (P4)
```

**Example assessment for a checkout feature change:**

| Area | Prob | Impact | Risk | Priority |
|------|------|--------|------|----------|
| Payment processing | 4 | 5 | 20 | P1 |
| Order confirmation email | 3 | 4 | 12 | P2 |
| Order history display | 2 | 3 | 6 | P3 |
| Wishlist button | 1 | 2 | 2 | P4 |

### What to Test First When Time is Limited

1. Core user journey (the #1 thing users do in the application)
2. New or changed functionality (highest risk of regression)
3. Areas with a history of bugs
4. High-traffic pages / features
5. Integration points (where two systems connect)
6. Payment and security flows
7. Data write operations (anything that creates, updates, or deletes)

---

## Section 6 — Exploratory Testing {#section-6}

### What Exploratory Testing Is

Exploratory testing is **structured freedom**. It is NOT random clicking. It is an approach where the tester simultaneously learns about the system, designs tests based on what they discover, and executes those tests — all at the same time.

The key properties:
- Tester is free to follow interesting paths
- Tester adapts based on what they find
- Tester uses domain knowledge, heuristics, and curiosity
- Sessions are time-boxed and have a goal (charter)
- Notes are taken throughout

### Session-Based Exploratory Testing (SBET)

The industry standard structure for exploratory testing. Each session has:

1. **Charter** — What is the goal of this session?
2. **Time box** — How long? (typically 45–90 minutes)
3. **Notes** — What did you do, what did you find?
4. **Debrief** — What did you discover? What do you still not know?

### Charter Format

```
Explore [area of the application]
Using [technique or approach]
To discover [risks, issues, or information]
```

**Example charters:**
- "Explore the login form using boundary value inputs to discover input validation gaps"
- "Explore the checkout flow as a new user with an empty cart to discover navigation edge cases"
- "Explore the admin dashboard using keyboard-only navigation to discover accessibility issues"
- "Explore the file upload feature using oversized, wrong-type, and empty files to discover error handling"

### Note-Taking During Exploration

Keep a running log during the session. Document:
- What you tested (so it can be repeated or handed to another tester)
- What you found (defects, anomalies, questions)
- Questions raised (gaps in your understanding)
- Ideas for further testing

Use the format: **What I did → What I observed → What I think**

### When to Use Exploratory Testing

- New features before scripted tests are written
- Unclear or missing requirements
- After a major refactor (check for unexpected breakage)
- When scripted tests pass but something "feels off"
- Verifying a complex user journey end-to-end
- Learning a new feature from a QA perspective

---

### Complete Exploratory Session Example: Login Feature

**Charter:** "Explore the login form using boundary conditions, special characters, and timing to discover authentication vulnerabilities and edge cases"

**Tester:** Jane QA
**Date:** 2024-01-15
**Time box:** 60 minutes
**Environment:** Test environment v2.4.1

---

**Session Notes:**

**[0:00–0:10] Setup and initial observation**
- Navigated to /login. Page loads. Fields: Email, Password, "Log In" button, "Forgot Password" link, "Sign Up" link.
- No CAPTCHA present. This is notable — might be a security concern.
- Observed: Password field shows character count as typing. This could leak password length.

**[0:10–0:20] Input boundary testing**
- Entered 1 character email: `a` → Submitted → Error: "Please enter a valid email" (expected, good)
- Entered 254-character email (max RFC standard): Accepted, submitted, returned 401 (expected)
- Entered 255-character email: Accepted — *ANOMALY: should this be rejected?*
- Entered email with no TLD: `user@example` → Accepted by form — *POTENTIAL DEFECT: client-side validation should reject this*
- Entered email with double @: `user@@example.com` → Error "Invalid email" (good)

**[0:20–0:35] Special characters**
- Email with apostrophe: `o'brien@example.com` → *DEFECT: application returns 500 error. SQL injection risk? Raised as DEFECT-4421.*
- Email with plus sign: `user+tag@example.com` → Login succeeded. Good.
- Email with spaces: `user @example.com` → Form submits with trimmed value. Email becomes `user@example.com`. *QUESTION: Is this intended? Could bypass email uniqueness.*
- Password with special chars `P@$$w0rd!`: Worked correctly.
- Password with only spaces: `     ` → Accepted by form — *POTENTIAL DEFECT: whitespace-only passwords should be rejected*

**[0:35–0:50] Timing and concurrency**
- Tried submitting while page was still loading: Double-submission occurred. Two requests sent. *DEFECT: Should disable button after first click. Raised as DEFECT-4422.*
- Tried submitting with empty fields: "Email is required" shown. Good.
- Waited 30 minutes on page, then submitted: Login succeeded. *QUESTION: Is there a form token expiry?*

**[0:50–0:60] Wrap-up and debrief**

**Defects raised:**
- DEFECT-4421: 500 error when email contains apostrophe (Critical — possible SQL injection)
- DEFECT-4422: Double-submission on login button rapid click (Medium — duplicate requests)

**Anomalies to investigate further:**
- 255+ character email accepted (possible no max length validation)
- Whitespace-only password accepted
- Email trimming behaviour — intended?

**Questions raised:**
- Is there a form CSRF token? (Security team to confirm)
- What is the account lockout threshold? (Need to test without risking locking test accounts)
- Is password length displayed in the UI intentional?

**Ideas for next session:**
- Test account lockout: how many failures trigger it?
- Test session persistence: does session survive browser restart?
- Test concurrent logins: can same account log in from two browsers?

---

## Section 7 — UAT (User Acceptance Testing) {#section-7}

### What UAT Is and Who Does It

UAT is the **final validation gate** before software goes to production. It answers the question: "Does this software meet what the business agreed to build?"

**Who performs UAT:**
- Business users / stakeholders (the people who requested the feature)
- Product Owners
- End users (in a beta / pilot programme)
- Customer representatives

UAT is NOT performed by the QA team. QA supports UAT but does not execute it.

### QA's Role in UAT

| QA Responsibility | Details |
|-------------------|---------|
| Prepare UAT test cases | Write acceptance test scripts business users can follow |
| Set up the environment | Ensure the UAT environment is stable and has correct data |
| Brief UAT participants | Explain how to use the test management tool, how to log issues |
| Support during UAT | Answer questions, investigate anomalies, clarify behaviour |
| Log defects | Raise any issues found during UAT in the defect tracker |
| Track progress | Monitor which scenarios have been tested, which are outstanding |
| Facilitate sign-off | Prepare the sign-off documentation |

### UAT Entry Criteria

Before UAT begins, all of the following must be true:

- QA has completed its own testing and signed off
- All P1 and P2 defects are closed
- UAT environment is stable (smoke test passed)
- Test data is in place (realistic, representative data)
- UAT test scripts are written and reviewed
- UAT participants are identified and available
- Environment access has been granted to participants

### UAT Exit Criteria

UAT is complete when all of the following are true:

- All planned UAT scenarios have been executed
- All P1 defects found during UAT are resolved and re-tested
- Business stakeholders formally sign off
- Sign-off document is completed and stored
- Outstanding known issues are documented and accepted by the business (or deferred to next release)

### UAT Sign-Off Process

1. QA prepares a UAT Summary Report (scenarios tested, pass/fail, defects found)
2. Product Owner reviews the report
3. Stakeholders formally confirm acceptance (email, signature, or tool-based sign-off)
4. QA files the sign-off in the project management tool
5. Release is approved to proceed to production

### Common UAT Issues

| Issue | Description | How QA Prevents It |
|-------|-------------|---------------------|
| Missing test data | Business users cannot test without realistic data | QA sets up seed data before UAT starts |
| Wrong environment | UAT is done on a different version than what will be released | QA confirms environment build version matches |
| Scope creep | Users test features that are not in scope for this release | QA defines UAT scope clearly in advance |
| Vague defects | "It doesn't work" reports without steps to reproduce | QA provides defect log template and supports users |
| No-shows | UAT participants are unavailable | QA confirms availability during sprint planning |
| Environment instability | UAT environment crashes mid-session | QA runs smoke test before each UAT session |

---

## Section 8 — Test Execution and Reporting {#section-8}

### Executing Test Cases

During execution, mark each test case with one of four statuses:

| Status | Meaning |
|--------|---------|
| **Pass** | Test executed completely. Actual result matches expected result. |
| **Fail** | Test executed completely. Actual result does NOT match expected result. |
| **Blocked** | Test cannot be executed due to an external dependency (environment down, another defect blocks it). |
| **Not Executed** | Test was not run in this cycle (time constraints, out of scope for this build). |

### What to Do When a Test Fails

1. **Reproduce the failure** — run the test again to confirm it is consistent
2. **Document exact steps** — record every step you took, including exact data used
3. **Capture evidence** — screenshot, video recording, API response, log extract
4. **Raise a defect** — in the defect tracker (Jira, Azure DevOps, etc.)
5. **Link the defect to the test case** — in your test management tool (Zephyr, TestRail)
6. **Notify the team** — flag on the team channel, especially for P1/P2 issues
7. **Mark the test case as "Fail"**
8. **Continue testing** — do not stop the cycle for a single failure

### What to Do When a Test is Blocked

1. **Document the blocker** — what is preventing the test from running?
2. **Mark the test case as "Blocked"** — with a clear reason
3. **Flag the blocker to the team** — who owns resolving it?
4. **Try to find a workaround** — can you test in a different environment? Use a different account?
5. **Move to other tests** — do not let one blocker stop all testing
6. **Revisit when resolved** — unblock and re-execute

### Test Cycle Completion Report

At the end of a test cycle, produce a completion report:

```
TEST CYCLE COMPLETION REPORT
==============================
Project:          Example Application v2.4.0
Environment:      Test Environment
Cycle Duration:   2024-01-10 to 2024-01-15
Tester(s):        Jane QA, Bob QA

TEST EXECUTION SUMMARY
----------------------
Total Test Cases:      147
Passed:                128  (87.1%)
Failed:                 11  (7.5%)
Blocked:                 5  (3.4%)
Not Executed:            3  (2.0%)

Pass Rate (of executed): 92.1% (128/139)

DEFECTS SUMMARY
---------------
Total Defects Raised:   18
Critical (P1):           2  [OPEN: 1, CLOSED: 1]
High (P2):               7  [OPEN: 3, CLOSED: 4]
Medium (P3):             6  [OPEN: 4, CLOSED: 2]
Low (P4):                3  [OPEN: 3, CLOSED: 0]

RISK AREAS
----------
- Payment processing: 2 defects raised. 1 P1 still open.
- Email notifications: 3 tests blocked due to mail server config issue.

GO / NO-GO RECOMMENDATION
--------------------------
RECOMMENDATION: NO-GO

Reason: 1 Critical defect (DEFECT-4421 — SQL injection on login) remains
open. This is a release blocker. Recommend fixing and re-testing before
proceeding to release.

Conditions for GO:
- DEFECT-4421 resolved and re-tested (pass)
- Mail server configuration resolved — 3 blocked tests executed and pass
- P2 defects: 3 open to be reviewed by Product Owner (risk-accept or fix)
```

### Go/No-Go Recommendation

QA's go/no-go decision is based on:
- Are all P1 defects resolved? (If not → No-Go)
- Are P2 defects at an acceptable level? (Risk assessed with Product Owner)
- Is the pass rate above the agreed threshold? (e.g. 90% is a common bar)
- Are blocked tests resolved? (Or risk-accepted)
- Is there any known data loss, security, or compliance risk? (Always No-Go until resolved)

---

## Section 9 — Cross-Browser and Device Testing {#section-9}

### Browser Differences: What Breaks Where

| Issue | Chrome | Firefox | Safari | Edge |
|-------|--------|---------|--------|------|
| CSS Grid | Full support | Full support | Older versions have gaps | Full support |
| CSS custom properties | Full | Full | Full (Safari 9.1+) | Full |
| WebP image format | Full | Full | Safari 14+ | Full |
| `fetch()` with CORS | Full | Full | Some CORS quirks | Full |
| Date input (`<input type="date">`) | Custom UI | Custom UI | No custom UI — shows text field | Custom UI |
| `<dialog>` element | Full | Full | Safari 15.4+ | Full |
| Scroll behaviour | Consistent | Minor differences | Momentum scrolling | Consistent |
| Font rendering | Slightly different | Slightly different | Anti-aliasing different | Like Chrome |
| `position: sticky` | Full | Full | Older Safari bugs | Full |
| CSS `aspect-ratio` | Full | Full | Safari 15+ | Full |

**Key insight:** Safari on iOS is always the most different. Apple requires all browsers on iOS to use Safari's WebKit engine, so Chrome on iPhone = Safari underneath.

### Cross-Browser Testing Services

| Tool | Type | What It Does |
|------|------|-------------|
| **BrowserStack** | Cloud, paid | Real browsers on real devices. Automated + manual. Screenshots + live testing. |
| **Sauce Labs** | Cloud, paid | Similar to BrowserStack. Strong CI integration. |
| **LambdaTest** | Cloud, freemium | Cross-browser cloud. Good for teams on budget. |
| **Browser DevTools** | Free, local | Chrome DevTools device emulation for responsive testing. Not real devices. |
| **Firefox Responsive Mode** | Free, local | Simulate different screen sizes in Firefox. Not real devices. |

**Important:** Cloud device testing (BrowserStack, Sauce Labs) uses real physical devices. Browser DevTools simulation is approximate — always verify on real devices before release.

### Responsive Testing: Breakpoints

| Device | Width | Test Focus |
|--------|-------|-----------|
| Mobile portrait | 320–480px | All content visible, buttons tappable, no horizontal scroll |
| Mobile landscape | 568–667px | Forms still usable, no layout collapse |
| Tablet portrait | 768px | Two-column layouts, navigation mode change |
| Tablet landscape | 1024px | Desktop-like layout begins |
| Desktop | 1200px+ | Full layout, hover states, multi-column |
| Wide desktop | 1440px+ | Content max-width, no excessive whitespace |

### What to Check in Cross-Browser and Responsive Testing

**Layout checks:**
- No horizontal scrollbar on mobile
- No overlapping elements
- Text is not truncated or overflowing containers
- Buttons and links are not obscured by other elements
- Images are not stretched or distorted

**Typography checks:**
- Font sizes are legible on mobile (minimum 16px for body text)
- Line spacing allows comfortable reading
- No text is rendering in wrong font (fallback fonts)

**Interactive checks:**
- Buttons have minimum 44px tap target on mobile (Apple guideline) / 48px (Google guideline)
- Dropdowns open in correct direction (not hidden off-screen)
- Modals are accessible on mobile (not larger than viewport)
- Touch gestures work (swipe, pinch-to-zoom if applicable)

**Form checks:**
- Keyboard appears on mobile when input is focused
- Date inputs use native mobile date picker
- Select dropdowns use native mobile picker
- Auto-fill works correctly on mobile browsers

---

## Section 10 — Interview Q&A {#section-10}

### Q1: What is the difference between smoke testing and sanity testing?

**Answer:**

Smoke testing is a broad, shallow pass across the entire application to verify that the build is stable enough to test. It covers the most critical end-to-end paths — login, navigation, core functionality. The goal is a go/no-go decision: is this build worth investing testing effort in?

Sanity testing is narrow and deep. It is done after a specific bug fix to verify that the fix has worked and has not broken anything adjacent. It does not cover the whole application — only the affected area.

In summary: **Smoke = is the whole build stable? Sanity = has this specific fix worked?**

---

### Q2: When would you choose manual testing over automation?

**Answer:**

I choose manual when:

1. The feature is brand new and the UI/behaviour is still changing — automating against a moving target wastes effort
2. Exploratory testing is needed — I need human curiosity and adaptability to discover unknown issues
3. UAT is happening — business users validate, not automation
4. Usability testing — can a user complete their goal comfortably? This requires human judgement
5. One-off tests that will never be repeated — the ROI of automation does not exist
6. Accessibility with assistive technologies — screen readers need a human to evaluate whether the experience makes sense
7. Visual/UI checks — layouts, animations, "does this look right?" cannot be reliably automated

Automation has a cost: writing, maintaining, and debugging tests. If a test will only run twice, write it manually.

---

### Q3: How do you write a good test case?

**Answer:**

A good test case has:
- A **clear, specific title** using the formula: [Feature] [Action] [Condition] [Expected Outcome]
- **Concrete preconditions** — the state the system must be in before you start
- **Single-action numbered steps** — each step does exactly one thing, specific enough for anyone to reproduce
- **Exact test data** — not "enter a valid email" but "enter jane@example.com"
- **Precise expected results** — not "it works" but "user is redirected to /dashboard, username Jane Smith appears in the header"

The test case should be reproducible by someone who has never seen the feature. If it depends on the tester's interpretation, it is not a good test case.

---

### Q4: What is exploratory testing and how do you structure it?

**Answer:**

Exploratory testing is simultaneous learning, test design, and execution. Unlike scripted testing where steps are written in advance, exploratory testing adapts in real time based on what the tester discovers.

I structure it using **Session-Based Exploratory Testing (SBET)**:

1. Write a **charter**: "Explore [area] using [technique] to discover [risks]" — this gives the session direction without over-constraining it
2. Set a **time box**: 60 minutes is typical
3. Take **notes** throughout: what I did, what I observed, defects raised, questions that arose
4. **Debrief**: summarise what I found, what I still don't know, ideas for follow-up sessions

This gives exploratory testing the rigour of documentation without losing the freedom to follow interesting discoveries.

---

### Q5: How do you decide when test coverage is enough?

**Answer:**

Coverage is sufficient when:
- All planned test cases in the test plan have been executed (or formally deferred)
- All P1 and P2 defects are closed
- The pass rate meets the agreed threshold (e.g. 90%+ of executed tests pass)
- All high-risk areas have been covered (based on risk assessment)
- UAT has passed and the business has signed off
- Exit criteria defined in the test plan are met

Coverage is never truly 100% — the goal is sufficient coverage relative to the risk. I use risk-based prioritisation to ensure the most important areas get the most thorough testing.

---

### Q6: What is UAT and what is QA's role in it?

**Answer:**

UAT is the final validation before production. Business users or product owners verify that the software meets the agreed requirements from a business perspective. It answers "does this do what we said it would do?"

QA's role is to **support** UAT, not execute it:
- Write UAT test scripts that business users can follow
- Set up the UAT environment and ensure it is stable
- Provide test data that reflects real-world scenarios
- Brief participants on how to log issues
- Log and triage any defects raised during UAT
- Track progress and facilitate sign-off

QA does not approve or reject the release during UAT — that decision belongs to the business. QA provides information and support.

---

### Q7: How do you handle a blocked test?

**Answer:**

1. **Document the blocker clearly** — what is the specific reason this test cannot run? (Environment down, dependent feature broken, missing test data, access not granted)
2. **Mark the test case as Blocked** with the reason noted
3. **Flag the blocker immediately** — raise it in the team channel, tag the owner of the blocker
4. **Try workarounds** — can I test in a different environment? Use a different account? Use a mocked dependency?
5. **Move on** — continue testing other cases. One blocked test must not stop the cycle
6. **Track it** — add it to the daily standup as a blocker
7. **Re-execute when unblocked** — ensure the blocked test gets picked up as soon as the dependency is resolved

A blocked test is not a failed test — it is a risk and a dependency that must be resolved before the cycle is complete.

---

### Q8: What is a test cycle and what goes in a completion report?

**Answer:**

A test cycle is a defined period of testing against a specific build or scope. It has a start date, an end date, a list of test cases to be executed, and an environment.

A completion report includes:
- **Execution summary**: total tests, passed, failed, blocked, not executed, pass rate
- **Defect summary**: total defects by severity, open vs closed
- **Risk areas**: what areas still have open issues
- **Go/no-go recommendation**: based on pass rate, open P1/P2 defects, blocked tests
- **Conditions for release**: what must be resolved before production (if No-Go)

The report gives stakeholders an objective view of software quality so they can make an informed release decision.

---

### Q9: How do you test a feature with no requirements?

**Answer:**

This happens regularly. My approach:

1. **Get as much context as possible** — talk to the developer (what did they build?), the product owner (what problem does this solve?), and look at any designs, wireframes, or Jira tickets
2. **Infer requirements** — what behaviour would a reasonable user expect? What would be a defect regardless of specification?
3. **Use exploration** — run a session-based exploratory test with the charter "explore [feature] to understand its behaviour and discover risks"
4. **Apply heuristics**: does it handle empty states, boundaries, special characters, concurrent users, and error conditions?
5. **Document what you find** — your test notes become implicit requirements documentation
6. **Flag the risk** — raise with the team that testing without requirements increases risk. Push for acceptance criteria to be written, even retrospectively

---

### Q10: How do you test a mobile app manually?

**Answer:**

Mobile testing has unique considerations beyond desktop:

**Installation and onboarding:**
- Fresh install, first-time setup, permissions prompts (camera, location, notifications)
- Update from a previous version — data migrates correctly

**Device-specific:**
- Test on real devices (not just emulators)
- Test both portrait and landscape orientations
- Test with different font sizes (accessibility settings)
- Test on low-end and high-end devices (performance difference)

**Network conditions:**
- Test on WiFi, 4G, 3G, and offline — what happens with each?
- Switch from WiFi to mobile data mid-session
- Poor connection: do API calls timeout gracefully?

**Interruptions:**
- Receive a phone call during a form fill — does the data survive?
- Push notification while using the app
- App sent to background and resumed
- Battery low warning

**Platform-specific:**
- iOS: back gesture (swipe from left edge), Apple keyboard, iOS system dark mode
- Android: back button, split-screen mode, default back navigation

**Touch interactions:**
- Tap targets minimum 44–48px
- Scroll smoothness
- Pinch-to-zoom (if applicable)
- Long-press context menus

---

*End of Manual Testing Complete Guide*
