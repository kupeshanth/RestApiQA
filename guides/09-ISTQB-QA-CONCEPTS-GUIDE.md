# ISTQB & QA Fundamentals — Complete Guide | Testing Theory + Techniques + Defect Management

---

## Table of Contents

1. [SDLC vs STLC — All Phases with QA Activities](#1-sdlc-vs-stlc--all-phases-with-qa-activities)
2. [Test Levels](#2-test-levels)
3. [Test Types](#3-test-types)
4. [Verification vs Validation](#4-verification-vs-validation)
5. [Black-Box Test Design Techniques](#5-black-box-test-design-techniques)
6. [White-Box Test Design Techniques](#6-white-box-test-design-techniques)
7. [Test Design Process](#7-test-design-process)
8. [Defect Lifecycle](#8-defect-lifecycle)
9. [Defect Severity vs Priority](#9-defect-severity-vs-priority)
10. [Risk-Based Testing](#10-risk-based-testing)
11. [Test Planning](#11-test-planning)
12. [Exploratory vs Scripted Testing](#12-exploratory-vs-scripted-testing)
13. [Regression Testing Strategy](#13-regression-testing-strategy)
14. [Performance Testing Concepts](#14-performance-testing-concepts)
15. [Non-Functional Testing Types](#15-non-functional-testing-types)
16. [Test Metrics](#16-test-metrics)
17. [Agile Testing — Shift-Left, Three Amigos, BDD](#17-agile-testing--shift-left-three-amigos-bdd)
18. [Interview Q&A (10 Questions)](#18-interview-qa-10-questions)

---

## 1. SDLC vs STLC — All Phases with QA Activities

### SDLC — Software Development Life Cycle

The SDLC is the overall process for building software. QA is involved at every phase, not just at the end.

| Phase | Description | QA Activities |
|---|---|---|
| **Requirements** | Business needs gathered, documented | Review requirements for ambiguity, testability, completeness; raise questions early |
| **System Design** | Architecture, database schema, API contracts designed | Review design docs; identify testability concerns; plan test approach |
| **Implementation** | Developers write code | Write test cases; prepare test data; set up test environments |
| **Testing** | Verify the built software | Execute tests (unit, integration, system, UAT); report defects; track fixes |
| **Deployment** | Release to production | Smoke testing in production; verify deployment success; monitor |
| **Maintenance** | Bug fixes, enhancements | Regression testing after fixes; test new features; maintain test suite |

**Key QA principle**: Defect cost increases the later it is found. A requirements defect costs 1x to fix; a production defect costs 100x. QA involvement from day one saves money and time.

### STLC — Software Testing Life Cycle

The STLC is QA's own process — the sequence of testing activities.

| Phase | Description | Entry Criteria | Exit Criteria |
|---|---|---|---|
| **Requirement Analysis** | QA studies requirements, identifies what to test | Requirements available, stable | Test scope documented, ambiguities resolved |
| **Test Planning** | Define strategy, scope, resources, schedule | Requirements reviewed | Test plan approved |
| **Test Case Design** | Write test cases, prepare test data | Test plan approved | Test cases written and reviewed |
| **Test Environment Setup** | Configure servers, browsers, databases | Test cases ready | Environment verified with smoke test |
| **Test Execution** | Run tests, log defects | Environment ready, build delivered | All planned tests executed |
| **Test Closure** | Evaluate completion, lessons learned | Execution complete, exit criteria met | Test closure report signed off |

---

## 2. Test Levels

### Unit Testing

- **What**: Testing individual components (methods, functions, classes) in isolation
- **Who writes**: Developers
- **Tools**: JUnit, NUnit, Pytest, Jest, Mocha
- **Example**: Testing that `calculateTax(100, 0.15)` returns `15.0`
- **Scope**: No DB, no HTTP calls — mocked/stubbed dependencies

### Integration Testing

- **What**: Testing that two or more components work together correctly
- **Who writes**: Developers + QA
- **Tools**: TestNG, JUnit, Pytest, Postman/RestAssured for API integration
- **Examples**:
  - Testing that the service layer correctly calls the repository layer and returns a mapped DTO
  - Testing that the checkout service correctly calls the payment API
- **Scope**: Real DB or test DB; services talk to each other

### System Testing

- **What**: Testing the complete, integrated system against requirements
- **Who writes**: QA
- **Tools**: Selenium, Playwright, RestAssured, manual testing
- **Examples**:
  - End-to-end user registration flow through UI
  - API test covering full CRUD lifecycle
- **Scope**: Full stack — UI, API, DB, external services (or stubs)

### Acceptance Testing

- **What**: Verifying the system meets business requirements and is ready for delivery
- **Who does it**: Business stakeholders + QA (or customers)
- **Types**:
  - **UAT** (User Acceptance Testing): Business users test real scenarios
  - **Alpha testing**: Internal testing by a selected user group before release
  - **Beta testing**: External users test in production-like environment
- **Scope**: Full production-like environment; test from user perspective

### Test Level Pyramid

```
        ┌──────────────┐
        │   UI Tests   │  ← Few, slow, expensive
        ├──────────────┤
        │  API Tests   │  ← Moderate number, fast
        ├──────────────┤
        │ Unit Tests   │  ← Many, very fast, cheap
        └──────────────┘
```

---

## 3. Test Types

### Functional Testing

Tests that the system does what it should do — business logic and features work correctly.

- Login, registration, checkout, search, CRUD operations
- Positive tests: valid inputs produce expected outputs
- Negative tests: invalid inputs are handled gracefully

### Non-Functional Testing

Tests how the system behaves, not what it does.

- Performance, security, usability, reliability, compatibility
- Usually separate from functional testing; requires specialist tools

### Structural Testing (White-Box)

Based on the internal structure of the code. Ensures all code paths are exercised.

- Statement coverage, branch coverage, path coverage
- Done primarily by developers

### Change-Related Testing

Done after code changes to verify nothing is broken.

- **Regression testing**: Re-run existing tests after changes
- **Confirmation testing (Re-testing)**: Re-test specifically the defect that was fixed

---

## 4. Verification vs Validation

| | Verification | Validation |
|---|---|---|
| **Question asked** | "Are we building the product right?" | "Are we building the right product?" |
| **Focus** | Process, standards, documents | The actual product/system |
| **When** | Throughout development | Usually at the end of a phase |
| **Techniques** | Reviews, walkthroughs, inspections | Testing, demonstrations, prototypes |
| **Example** | Reviewing a test plan against standards | Running tests on the live application |
| **Who** | QA + team | QA + stakeholders |

**Memory trick**: Verification = checking the spec/documents. Validation = checking the actual software.

---

## 5. Black-Box Test Design Techniques

Black-box techniques derive tests from requirements and specifications, with no knowledge of internal code.

### 5.1 Equivalence Partitioning (EP)

**Principle**: Divide input data into groups (partitions) where every value in the partition is expected to behave the same way. Test one representative value from each partition.

**Why**: Avoids testing every possible value (infinite) and instead tests each class of behaviour.

**Example: Age field — must be between 18 and 65 (inclusive)**

| Partition | Range | Representative Value | Type |
|---|---|---|---|
| Invalid — too young | Below 18 | 10 | Invalid |
| Valid | 18 to 65 | 30 | Valid |
| Invalid — too old | Above 65 | 80 | Invalid |

Minimum 3 test cases instead of testing every possible age.

**Another example: HTTP status codes**

| Partition | Values | Test Value |
|---|---|---|
| Success | 200–299 | 200 |
| Redirection | 300–399 | 301 |
| Client error | 400–499 | 404 |
| Server error | 500–599 | 500 |

### 5.2 Boundary Value Analysis (BVA)

**Principle**: Defects cluster at the edges of partitions. Test the minimum, maximum, and values just inside/outside the boundaries.

**Extension of EP**: Apply BVA at the boundaries of each partition.

**Example: Age field — must be between 18 and 65 (inclusive)**

| Boundary | Value | Expected Result |
|---|---|---|
| Just below minimum | 17 | Invalid |
| At minimum | 18 | Valid |
| Just above minimum | 19 | Valid |
| Just below maximum | 64 | Valid |
| At maximum | 65 | Valid |
| Just above maximum | 66 | Invalid |

6 boundary test cases capture most edge-case defects.

**2-value vs 3-value BVA:**
- 2-value: test the boundary and just outside it (min/max + invalid on each side)
- 3-value: test just below, at, and just above each boundary

**Example: Discount calculation — 10% off if order >= £100**

| Value | Expected |
|---|---|
| £99 | No discount |
| £100 | 10% discount |
| £101 | 10% discount |

### 5.3 Decision Table Testing

**Use when**: There are combinations of conditions that lead to different actions.

**Principle**: Create a table listing all condition combinations and the expected action for each.

**Example: Loan application**

Conditions:
- Credit score: Good or Poor
- Income: Above threshold or Below threshold
- Collateral: Yes or No

| | Rule 1 | Rule 2 | Rule 3 | Rule 4 | Rule 5 | Rule 6 | Rule 7 | Rule 8 |
|---|---|---|---|---|---|---|---|---|
| Credit score | Good | Good | Good | Good | Poor | Poor | Poor | Poor |
| Income | Above | Above | Below | Below | Above | Above | Below | Below |
| Collateral | Yes | No | Yes | No | Yes | No | Yes | No |
| **Action** | Approve | Approve | Approve | Reject | Approve | Reject | Reject | Reject |

Each column is one test case. This ensures all logical combinations are covered.

**When to use**: Login logic, pricing rules, permission systems, discount calculators.

### 5.4 State Transition Testing

**Use when**: The system can be in different states, and input events cause transitions between states.

**Principle**: Model the system as states, events (inputs), and transitions. Derive tests to cover all states and transitions.

**Example: Order status**

```
[Created] --pay--> [Paid] --ship--> [Shipped] --deliver--> [Delivered]
    |                                    |
    +--cancel--> [Cancelled]    --return--> [Returned]
```

**State table:**

| Current State | Event | Next State | Action |
|---|---|---|---|
| Created | Pay | Paid | Confirm payment |
| Created | Cancel | Cancelled | Send cancellation email |
| Paid | Ship | Shipped | Send tracking info |
| Paid | Cancel | Cancelled | Refund payment |
| Shipped | Deliver | Delivered | Send delivery confirmation |
| Shipped | Return | Returned | Process return |

**Tests to write:**
1. Valid path: Created → Paid → Shipped → Delivered
2. Cancellation from Created
3. Cancellation from Paid (tests refund logic)
4. Return from Shipped
5. Invalid transition: trying to Ship an order that is still Created (should be blocked)

### 5.5 Use Case Testing

**Principle**: Derive tests directly from use cases (scenarios of how users interact with the system).

**Use case: User logs in**
- Basic flow: User enters valid email and password → logged in → redirected to dashboard
- Alternative flow 1: User enters wrong password → error message shown
- Alternative flow 2: User enters unregistered email → error message
- Alternative flow 3: User clicks "Forgot password" → password reset email sent
- Exception flow: System is down → user sees a friendly error page

Each flow becomes one or more test cases.

---

## 6. White-Box Test Design Techniques

White-box techniques are based on the internal code structure.

### Statement Coverage

**Goal**: Every executable statement in the code is executed at least once.

```java
// Code under test
public String classify(int n) {
    String result = "";                    // Line 1
    if (n > 0) {                           // Line 2
        result = "positive";               // Line 3
    }
    if (n < 0) {                           // Line 4
        result = "negative";               // Line 5
    }
    return result;                         // Line 6
}
```

To achieve 100% statement coverage: need `n > 0` (hits line 3) and `n < 0` (hits line 5).
Two tests: n=5, n=-5. But n=0 returns "" and that is not exposed by statement coverage.

### Branch Coverage (Decision Coverage)

**Goal**: Every branch (true/false outcome) of every decision point is executed.

For the example above:
- Test 1: n=5 → `n > 0` is TRUE (line 3 executes), `n < 0` is FALSE (line 5 skipped)
- Test 2: n=-5 → `n > 0` is FALSE (line 3 skipped), `n < 0` is TRUE (line 5 executes)
- Test 3: n=0 → both conditions FALSE

Branch coverage is stronger than statement coverage and is the minimum recommended for production code.

### Path Coverage

**Goal**: Every unique path through the code is executed.

Number of paths grows exponentially with the number of conditions — impractical for complex code. Used in safety-critical systems.

**Cyclomatic Complexity (McCabe)**: Measures the number of linearly independent paths.
- Formula: CC = E - N + 2P (edges minus nodes plus 2 × connected components)
- Simple interpretation: CC = number of decision points + 1
- CC > 10 means the code is too complex — refactor it
- CC also tells you the minimum number of tests for branch coverage

---

## 7. Test Design Process

The ISTQB-defined progression from requirements to executable tests:

```
Requirements / User Stories
         ↓
   Test Conditions
(what needs to be tested)
         ↓
    Test Cases
(specific input/expected output)
         ↓
  Test Procedures
(step-by-step execution instructions)
         ↓
   Test Scripts
(automated code implementing the procedure)
```

### Test Conditions

A testable aspect of a component or system identified from requirements.

- Requirement: "The login page must validate the email format"
- Test conditions:
  - Valid email format
  - Missing @ symbol
  - Missing domain
  - Missing username part
  - Multiple @ symbols
  - Spaces in email

### Test Cases

A test case specifies:
1. **Test Case ID** — unique identifier
2. **Title/Description** — what is being tested
3. **Preconditions** — what must be true before execution
4. **Test Steps** — numbered actions to perform
5. **Test Data** — inputs
6. **Expected Result** — what should happen
7. **Actual Result** — filled in during execution
8. **Pass/Fail** — outcome

### Example Test Case

| Field | Value |
|---|---|
| **ID** | TC_LOGIN_003 |
| **Title** | Login with invalid email format |
| **Preconditions** | Application is running; login page is displayed |
| **Test Steps** | 1. Enter `notanemail` in email field; 2. Enter `Password123` in password field; 3. Click Login |
| **Test Data** | Email: `notanemail`, Password: `Password123` |
| **Expected Result** | Error message "Please enter a valid email address" is displayed; user is not logged in |
| **Priority** | Medium |
| **Type** | Negative, Functional |

---

## 8. Defect Lifecycle

### All Defect States

```
Developer fixes it ──────────────────────────────────────┐
                                                          ↓
[New] → [Assigned] → [Open] → [Fixed] → [Retest] → [Closed]
  │                    │                    │
  │                    │                    └→ [Reopened] → [Open]
  │                    └→ [Deferred]
  └→ [Rejected]
```

### State Descriptions

| State | When It Applies | Who Changes It |
|---|---|---|
| **New** | QA logs the defect; not yet reviewed | QA |
| **Assigned** | Tech lead / developer has been assigned ownership | Lead / PM |
| **Open** | Developer has accepted and is working on the fix | Developer |
| **Fixed** | Developer has fixed and deployed the fix to the test environment | Developer |
| **Retest** | QA is verifying the fix | QA |
| **Closed** | QA has verified the fix; defect no longer reproducible | QA |
| **Reopened** | QA retested and the defect is still present | QA |
| **Deferred** | Acknowledged but will not be fixed in this release; moved to backlog | PM / Lead |
| **Rejected** | Developer or lead determines it is not a defect (works as designed, cannot reproduce, duplicate) | Developer / Lead |
| **Duplicate** | Same defect already logged; this instance is closed | QA / Lead |

### Important Rules

- Only QA should move a defect to **Closed** — never the developer
- Only QA should move a defect to **Reopened** — after failed retest
- **Deferred** is not the same as **Closed** — the bug still exists
- **Rejected** must include a reason; QA can escalate if they disagree

### What to Include in a Good Defect Report

1. **Title**: Short, specific. Not "Login doesn't work" — use "Login fails with valid credentials when email contains uppercase letters"
2. **Environment**: Browser, OS, version, URL
3. **Steps to Reproduce**: Numbered, minimal, reproducible
4. **Expected Result**: What should happen
5. **Actual Result**: What actually happened
6. **Severity**: How bad is the impact?
7. **Priority**: How urgently should it be fixed?
8. **Screenshots / Videos / Logs**: Evidence
9. **Test Data**: Exact data used

---

## 9. Defect Severity vs Priority

### Definitions

- **Severity**: The technical impact of the defect on the system's functionality. "How bad is it?"
- **Priority**: The business urgency of fixing the defect. "How soon must it be fixed?"

These are independent and set by different people:
- Severity is set by QA (technical assessment)
- Priority is set by PM/Business (business decision)

### Severity Levels

| Level | Description | Example |
|---|---|---|
| **Critical (S1)** | System crashes, data loss, security breach, core function completely broken | Payment processing fails; user data deleted; app crashes on launch |
| **Major (S2)** | Major feature broken but workaround exists | Export to PDF fails; search returns no results |
| **Minor (S3)** | Feature works but with cosmetic/functional issues | Dropdown not sorted alphabetically; tooltip text wrong |
| **Trivial (S4)** | Cosmetic defect with no functional impact | Misaligned button by 2px; wrong font colour |

### Priority Levels

| Level | Description | Example |
|---|---|---|
| **Critical (P1)** | Must fix immediately, blocks release | Homepage shows 500 error |
| **High (P2)** | Must fix in current sprint/release | Core workflow broken |
| **Medium (P3)** | Should fix in near-term release | Non-critical feature broken |
| **Low (P4)** | Fix when time permits | Minor cosmetic issues |

### The 4 × 4 Matrix — All Combinations Explained

| Severity | Priority | Example | Why |
|---|---|---|---|
| High Severity + High Priority | S1/P1 | Payment gateway is down | Broken core feature + high business impact |
| High Severity + Low Priority | S1/P4 | Crash on a rarely-used legacy report | System crashes but almost nobody uses that feature |
| Low Severity + High Priority | S4/P1 | Company name spelled wrong on the homepage | Cosmetic but visible to all users + brand risk |
| Low Severity + Low Priority | S4/P4 | Misaligned icon in the admin panel | Nobody sees it, no functional impact |

### Common Interview Trap

"The login button is missing from the homepage."

- Severity: Critical (core function completely broken — nobody can log in)
- Priority: Critical (must fix immediately, blocks all users)

"The 'About Us' page has a spelling mistake."

- Severity: Trivial (no functional impact)
- Priority: Medium or High (if the company is sensitive about brand reputation)

---

## 10. Risk-Based Testing

### Definition

Risk-based testing prioritises testing effort based on the risk level of different parts of the system. You cannot test everything exhaustively — you test the most important things first.

### Risk Formula

```
Risk = Probability of Failure × Impact of Failure
     = Likelihood × Consequence
```

### Risk Assessment Process

**Step 1: Identify risks**
List all features and ask: "What could go wrong here?"

**Step 2: Assess probability (likelihood)**

| Level | Probability | Criteria |
|---|---|---|
| High | > 50% | New code, complex logic, lots of dependencies, inexperienced developer |
| Medium | 20–50% | Modified code, moderate complexity |
| Low | < 20% | Mature, stable, rarely changed code |

**Step 3: Assess impact (consequence)**

| Level | Impact | Criteria |
|---|---|---|
| High | Business critical | Revenue loss, legal risk, data loss, security breach |
| Medium | Significant | Core feature degraded, many users affected |
| Low | Minor | Cosmetic issue, few users, workaround available |

**Step 4: Plot risk matrix**

```
         Impact
          Low │ Med │ High
         ─────┼─────┼─────
    High │ Med │ High│ CRIT  ← Test these first and most thoroughly
         ─────┼─────┼─────
P   Med  │ Low │ Med │ High
r        ─────┼─────┼─────
o   Low  │ Low │ Low │ Med   ← Test these last, or not at all if time runs out
b
```

**Step 5: Allocate test effort**
- Critical risk areas: thorough testing, multiple techniques, automation
- Medium risk: standard testing
- Low risk: light testing or skip

### Practical Example

For an e-commerce application:

| Feature | Probability | Impact | Risk Level | Testing Approach |
|---|---|---|---|---|
| Payment processing | High (new code) | High (revenue) | Critical | Full regression, edge cases, error paths |
| Product search | Medium | High (user experience) | High | Functional + performance tests |
| Admin reporting | Low (stable) | Medium | Medium | Basic functional tests |
| Footer links | Low | Low | Low | Quick smoke check |

---

## 11. Test Planning

### What Goes in a Test Plan (IEEE 829 / ISTQB)

A test plan is the master document that defines the entire testing effort. It answers: What, How, Who, When, Where.

| Section | Content |
|---|---|
| **1. Introduction / Purpose** | Why this document exists; project context |
| **2. Test Scope** | What is in scope and what is explicitly out of scope |
| **3. Test Objectives** | Goals of testing (find defects, gain confidence, provide info) |
| **4. Test Strategy** | Test levels, types, techniques to be used |
| **5. Entry and Exit Criteria** | Conditions to start and stop testing |
| **6. Test Items** | Features/components under test |
| **7. Features Not Tested** | Explicitly excluded items and reasons |
| **8. Test Environment** | OS, browsers, devices, test data approach |
| **9. Test Schedule** | Timeline, milestones, dependencies |
| **10. Resources** | Team members, roles, responsibilities |
| **11. Risks and Contingencies** | What could go wrong; mitigation plan |
| **12. Deliverables** | Test cases, reports, defect logs to be produced |
| **13. Approval** | Sign-off section |

### Entry and Exit Criteria Examples

**Entry Criteria (when to start testing):**
- Build has been deployed to test environment
- Smoke test passes (basic navigation works)
- Test cases have been reviewed and approved
- Test data is available

**Exit Criteria (when to stop testing):**
- 95% of test cases executed
- No open Critical or High severity defects
- Defect density below agreed threshold
- Test coverage meets agreed percentage

---

## 12. Exploratory vs Scripted Testing

| | Scripted Testing | Exploratory Testing |
|---|---|---|
| **Definition** | Test cases written in advance; executed step by step | Learning, designing, and executing tests simultaneously |
| **Documentation** | Detailed test cases and procedures | Session notes, charters, time-boxed sessions |
| **When to use** | Regression testing, compliance, UAT sign-off | New features, complex scenarios, finding unexpected bugs |
| **Advantages** | Repeatable, measurable, coverage tracking | Fast, finds edge cases, tests human intuition |
| **Disadvantages** | Misses unexpected defects, can become robotic | Hard to measure, depends on tester skill |
| **Suitable for** | Automation | Skilled QA engineers |

### Session-Based Exploratory Testing

- **Charter**: A mission statement — "Explore the checkout flow with focus on edge cases in coupon code application"
- **Time-box**: 60–90 minutes per session
- **Notes**: Log what you tested, what you found, questions raised
- **Debrief**: Report to lead; convert important findings to formal test cases

### When to Use Each

- Use **scripted** for regression packs, release sign-off, compliance testing, onboarding new testers
- Use **exploratory** for new features, risk-based investigation, usability assessment, when requirements are incomplete

---

## 13. Regression Testing Strategy

### What is Regression Testing?

Re-running existing tests after code changes to ensure that previously working functionality still works. Changes introduce unintended side effects (regressions).

### When to Regression Test

- After every bug fix
- After every new feature added
- After every code refactor
- Before every release

### Regression Strategies

**Full Regression**: Run every test in the suite.
- Pro: Maximum coverage
- Con: Slow and expensive; impractical daily

**Selective Regression**: Run tests related to changed code.
- Identify which modules changed
- Run tests that cover those modules + their dependencies
- Requires good traceability (test ↔ requirement ↔ code)

**Risk-Based Regression**: Prioritise tests by risk score.
- Always run Critical and High risk tests
- Run Medium tests when time allows
- Skip Low risk tests until release

**Automated Regression**: The suite runs automatically in CI on every commit.
- Smoke tests on every push (5–10 minutes)
- Full regression nightly or on release branches (30–60 minutes)

### Regression Test Suite Maintenance

- Remove obsolete tests (features removed)
- Update tests when requirements change
- Review flaky tests monthly — fix or remove them
- Track test execution time — split if > 30 minutes

---

## 14. Performance Testing Concepts

### Types of Performance Tests

| Type | Purpose | What You Measure |
|---|---|---|
| **Load Testing** | Verify the system handles expected user load | Response time, throughput, error rate at normal load |
| **Stress Testing** | Find the breaking point of the system | Maximum load before failure; how it fails |
| **Spike Testing** | Sudden large increase in users | System response to sudden traffic burst |
| **Endurance (Soak) Testing** | System under sustained load over time | Memory leaks, resource degradation, performance drift |
| **Volume Testing** | Large volumes of data | DB performance with millions of records |
| **Scalability Testing** | How well the system scales | Performance at 2x, 5x, 10x load |

### Key Performance Metrics

| Metric | Definition | Acceptable Value (example) |
|---|---|---|
| **Response Time** | Time from request sent to response received | < 2 seconds for 95th percentile |
| **Throughput** | Requests processed per second | 1000 RPS for target load |
| **Error Rate** | Percentage of requests that fail | < 0.1% |
| **Concurrent Users** | Number of simultaneous users | 500 concurrent users |
| **CPU Usage** | Server CPU during load | < 80% |
| **Memory Usage** | RAM during load | Stable (not growing = no leak) |
| **Latency** | Network delay | < 100ms |
| **Apdex Score** | User satisfaction score (0–1) | > 0.9 |

### Performance Testing Tools

| Tool | Use |
|---|---|
| Apache JMeter | Load testing; simulates hundreds of concurrent users |
| k6 | Modern scripting-based load testing; great for CI |
| Gatling | Scala-based; high performance; code-as-config |
| Locust | Python-based; easy scripting |
| Playwright/Selenium | Not suitable for load testing |

### Load Testing Terminology

- **Virtual User (VU)**: A simulated user performing actions
- **Ramp-up**: Gradually increasing users (prevents sudden overload)
- **Steady State**: Period at full load being observed
- **Ramp-down**: Gradually decreasing users
- **Think Time**: Simulated pause between user actions

---

## 15. Non-Functional Testing Types

| Type | What It Tests | Tools/Methods |
|---|---|---|
| **Performance** | Speed, scalability, stability under load | JMeter, k6, Gatling |
| **Security** | Vulnerabilities, authentication, data protection | OWASP ZAP, Burp Suite, manual pen testing |
| **Usability** | User experience, ease of use, accessibility | User testing sessions, heuristic evaluation |
| **Accessibility** | WCAG compliance, screen reader support | Axe, Wave, manual with NVDA |
| **Compatibility** | Works on different browsers/OS/devices | BrowserStack, Selenium grid |
| **Reliability** | Uptime, fault tolerance, recovery | Chaos engineering, endurance tests |
| **Maintainability** | Code quality, documentation | SonarQube, code review |
| **Portability** | Works across environments | Environment testing |
| **Localisation** | Correct language, date/number formats | Manual + automation |
| **Compliance** | Meets legal/regulatory standards | Audit against standards (GDPR, PCI-DSS) |

---

## 16. Test Metrics

Metrics enable measurement of testing quality, progress, and effectiveness.

### Defect Metrics

**Defect Density**
```
Defect Density = Number of Defects / Size of Component
              = 15 defects / 1000 lines of code
              = 0.015 defects per LOC
```
Lower is better. Used to compare component quality.

**Defect Removal Efficiency (DRE)**
```
DRE = (Defects found before release / Total defects) × 100
    = (90 / 100) × 100 = 90%
```
Measures how effective testing was at finding defects before users do.

**Defect Leakage Rate**
```
Defect Leakage = (Defects found in production / Total defects found) × 100
               = (10 / 100) × 100 = 10%
```
Lower is better.

**Mean Time to Fix (MTTF)**
```
MTTF = Total fix time for all defects / Number of defects fixed
     = 300 hours / 30 defects = 10 hours/defect
```

### Test Coverage Metrics

**Test Case Pass Rate**
```
Pass Rate = (Tests Passed / Tests Executed) × 100
          = (180 / 200) × 100 = 90%
```

**Test Execution Progress**
```
Execution % = (Tests Executed / Total Tests Planned) × 100
```

**Requirements Coverage**
```
Coverage = (Requirements with test cases / Total requirements) × 100
```

### Other Important Metrics

| Metric | Formula | Purpose |
|---|---|---|
| **Blocked tests** | Tests blocked / total tests | Shows environment stability |
| **Automation coverage** | Automated test cases / total test cases | Shows automation maturity |
| **Test efficiency** | Defects found / hours of testing | Shows testing effectiveness |
| **Defect age** | Days from raised to closed | Shows resolution speed |

### Reporting Metrics to Stakeholders

Always provide trend data — a single number is meaningless without context.

"90% pass rate this week, up from 85% last week, trending toward release exit criteria of 95%."

---

## 17. Agile Testing — Shift-Left, Three Amigos, BDD

### Shift-Left Testing

**Definition**: Moving testing activities earlier in the development lifecycle — to the left on the timeline.

**Traditional approach**: Developers build everything, throw it to QA at the end.

**Shift-left approach**: QA is involved from sprint planning. Test cases are written before code. QA reviews requirements before a single line is coded.

**Benefits**:
- Defects found in requirements cost almost nothing to fix
- Developers know what is being tested before they code
- No surprises at the end of the sprint
- Faster feedback cycles

**How QA shifts left**:
- Attend backlog refinement to clarify requirements
- Write test conditions when user stories are written
- Define acceptance criteria with the team
- Review API contracts before implementation
- Pair with developers on tricky logic

### Three Amigos

A technique where three perspectives review each user story before development begins:

- **Product Owner (Business)**: "This is what I want"
- **Developer (Technical)**: "This is how I will build it"
- **QA (Testing)**: "This is how I will test it — and here are the edge cases you haven't thought of"

**Goal**: Shared understanding. All three agree on requirements, scope, and acceptance criteria before a story enters a sprint.

**What QA brings to Three Amigos**:
- Edge cases and boundary values
- Negative scenarios
- Questions about error handling
- Questions about permissions and roles
- Non-functional requirements (performance, security)

### BDD — Behaviour-Driven Development

**Definition**: A collaborative approach where tests are written in plain language (Gherkin) that all stakeholders (business, developers, QA) can read and understand.

**Gherkin syntax** (the language for BDD):

```gherkin
Feature: User Login

  Scenario: Successful login with valid credentials
    Given the user is on the login page
    And the user has a registered account with email "user@example.com"
    When the user enters email "user@example.com" and password "Password123"
    And the user clicks the Login button
    Then the user should be redirected to the dashboard
    And the user should see "Welcome, User" in the header

  Scenario: Login fails with incorrect password
    Given the user is on the login page
    When the user enters email "user@example.com" and password "WrongPass"
    And the user clicks the Login button
    Then the user should remain on the login page
    And an error message "Invalid credentials" should be displayed

  Scenario Outline: Login validation for various invalid emails
    Given the user is on the login page
    When the user enters email "<email>" and password "Password123"
    And the user clicks the Login button
    Then the user should see error "<error_message>"

    Examples:
      | email         | error_message                     |
      | notanemail    | Please enter a valid email address |
      |               | Email is required                 |
      | @nodomain.com | Please enter a valid email address |
```

**Tools**: Cucumber (Java/JS/Ruby), SpecFlow (.NET), Behave (Python)

**BDD vs TDD**

| | TDD | BDD |
|---|---|---|
| **Focus** | Implementation correctness | Behaviour / business value |
| **Language** | Code (test frameworks) | Gherkin (plain language) |
| **Who writes** | Developers | QA + BA + Developers together |
| **Level** | Unit tests | Acceptance tests |
| **Cycle** | Red → Green → Refactor | Scenarios defined → Automated → Code written |

**Agile testing quadrants** (Brian Marick model):

```
                   Business-facing
                         ↑
Q2 (Automated + Manual)  |  Q3 (Manual)
 - Acceptance tests      |  - Exploratory testing
 - BDD scenarios         |  - Usability testing
                         |  - User acceptance testing
Supporting the team ←────┼────→ Critiquing the product
                         |
Q1 (Automated)           |  Q4 (Tools)
 - Unit tests            |  - Performance testing
 - Integration tests     |  - Security testing
                         |  - Compatibility testing
                         ↓
                   Technology-facing
```

---

## 18. Interview Q&A (10 Questions)

**Q1: What is the difference between verification and validation? Give an example.**

Verification asks "are we building the product right?" and checks documents, processes, and plans against standards — without running the software. Validation asks "are we building the right product?" and involves actually testing the software.

Example: Reviewing a requirements specification for completeness and clarity is verification. Running the built application against those requirements is validation.

**Q2: Explain equivalence partitioning and boundary value analysis. How do they complement each other?**

Equivalence partitioning divides inputs into groups where every value in the group is expected to behave identically. You test one value per group, reducing the number of tests. BVA extends EP by focusing on the edges of each partition, since defects cluster at boundaries. EP tells you *which groups* to test; BVA tells you *which values within those groups* to prioritise. Together they give maximum fault detection with minimum test cases.

**Q3: What is the difference between severity and priority? Give an example where they differ.**

Severity is the technical impact on the system; priority is the business urgency to fix. They are independent.

Example 1 (high severity, low priority): A crash occurs in a legacy report used by one internal user monthly. The system crashes, which is high severity, but priority is low because almost nobody is affected.

Example 2 (low severity, high priority): The company CEO's name is spelled wrong on the homepage. No functional impact (low severity) but must be fixed immediately for brand reasons (high priority).

**Q4: Walk me through the defect lifecycle. When does a defect get "Reopened"?**

A defect follows: New → Assigned → Open → Fixed → Retest. After retest, QA either Closes it (fix verified) or Reopens it (bug still present). Reopened means the developer's fix did not resolve the problem; QA adds evidence (screenshot, new steps) and the defect goes back through the cycle. Only QA can Close or Reopen a defect — never the developer.

**Q5: What is risk-based testing and how do you prioritise your test effort?**

Risk-based testing prioritises areas of the system with the highest risk of failure combined with the highest impact if they fail. Risk = Probability × Impact. I identify risky areas (new code, complex logic, recently changed modules, business-critical features), score them on probability and impact, and allocate most test effort to high-risk areas. If time runs out, low-risk areas are skipped, not high-risk ones.

**Q6: What is the difference between regression testing and confirmation testing?**

Confirmation testing (re-testing) specifically verifies that a particular defect has been fixed — you re-execute exactly the steps that originally found the defect to confirm the fix works. Regression testing is broader: after any change, you re-run the existing test suite to verify that nothing previously working has been broken by the change. Every fix requires both confirmation testing (did this specific bug get fixed?) and regression testing (did fixing it break anything else?).

**Q7: What are the main ISTQB black-box techniques and when do you use each?**

Equivalence Partitioning: when the input space is large and can be grouped into classes. Boundary Value Analysis: always applied at the edges of EP partitions. Decision Table: when the system has multiple condition combinations leading to different outcomes (permission systems, pricing rules). State Transition: when the system has distinct states and events (order lifecycle, booking systems). Use Case Testing: when you want to test end-to-end user journeys.

**Q8: What is exploratory testing and when is it better than scripted testing?**

Exploratory testing means simultaneously designing and executing tests, guided by the tester's knowledge and intuition, usually within a time-boxed session. It is better than scripted testing when: testing new features with incomplete requirements; investigating areas where automated tests pass but something "feels wrong"; performing risk-based investigation; testing usability and user experience. Scripted testing is better for regression, compliance, and when repeatability and coverage measurement are required.

**Q9: What performance testing types do you know and what distinguishes load from stress testing?**

Load testing applies expected production load to verify the system meets performance SLAs — it answers "does the system perform adequately under normal conditions?". Stress testing pushes the system beyond its design limits to find the breaking point — it answers "how does the system fail and at what threshold?". Other types: spike testing (sudden traffic bursts), endurance/soak testing (sustained load over hours to find memory leaks), volume testing (large data sets), scalability testing (how well it scales horizontally).

**Q10: What is shift-left testing and why does it matter?**

Shift-left means involving QA earlier in the development process — during requirements and design, not just after code is written. It matters because defects are exponentially cheaper to fix the earlier they are found. A requirements defect caught by QA in sprint planning takes minutes to correct; the same defect found in production may require a hotfix, data migration, customer communication, and loss of reputation. QA contributes most value not by finding bugs in finished software but by preventing them from being built in the first place.

---

*Guide complete — covers all ISTQB Foundation Level topics: SDLC/STLC, test levels, test types, design techniques, defect management, risk, metrics, and agile testing.*
