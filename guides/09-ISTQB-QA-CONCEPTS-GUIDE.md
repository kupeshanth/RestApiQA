# ISTQB & QA Fundamentals — Complete Interview Q&A Guide

---

## SECTION 1: SDLC AND STLC

---

**Q1: What is the SDLC? What are QA's activities at each phase?**

**A:**

The Software Development Life Cycle (SDLC) is the structured process for planning, creating, testing, and delivering a software application. QA is not a phase that happens at the end — QA has activities at every single phase.

| SDLC Phase | What Happens | QA Activities |
|---|---|---|
| **Requirements** | Business analysts gather and document what the software must do | Review requirements for ambiguity, completeness, and testability; raise questions; identify risks |
| **System Design** | Architects design the system: database schema, APIs, UI wireframes | Review design documents; identify testability concerns (can we observe the state?); plan the test approach |
| **Implementation (Coding)** | Developers write code | Write test cases; prepare test data; set up test environments; write automation code in parallel |
| **Testing** | Verify and validate the built software | Execute tests (unit, integration, system, UAT); log defects; track fixes; retest; regression test |
| **Deployment** | Release to production | Smoke test in production after deployment; verify core flows work; monitor for anomalies |
| **Maintenance** | Fix bugs, add features | Regression test after every fix; test new features; maintain and update the automated test suite |

**Key principle:** Defects found later are exponentially more expensive to fix. A requirements defect caught by QA during the requirements phase takes minutes to resolve. The same defect found in production may require a hotfix, data migration, customer communication, and reputational damage.

---

**Q2: What is the STLC? What are its phases and what makes each phase start and end?**

**A:**

The Software Testing Life Cycle (STLC) is QA's own process — the sequence of activities the testing team performs. It runs in parallel with (not after) the SDLC.

| Phase | What Happens | Entry Criteria | Exit Criteria |
|---|---|---|---|
| **Requirement Analysis** | QA studies requirements; identifies what to test; flags ambiguities | Requirements document available and stable | Test scope documented; all ambiguities resolved and answered |
| **Test Planning** | Define strategy, scope, schedule, resources, tools, environment needs | Requirements reviewed by QA | Test plan written and approved by stakeholders |
| **Test Case Design** | Write test cases; create test data; design automation scripts | Test plan approved | Test cases written, reviewed, and signed off |
| **Test Environment Setup** | Configure servers, databases, browsers, devices, test accounts | Test cases ready | Environment set up and verified with an initial smoke test |
| **Test Execution** | Execute test cases; log defects; retest fixes; do regression | Build deployed to test environment; environment verified | All planned tests executed; exit criteria met (pass rate, defect count) |
| **Test Closure** | Evaluate quality; document lessons learned; archive test artefacts | All testing complete; exit criteria met | Test closure report signed off by stakeholders |

**STLC vs SDLC — the key distinction:**
- SDLC covers the entire project from idea to deployment
- STLC is QA's process nested within the SDLC — it has its own phases, entry/exit criteria, and deliverables

---

**Q3: What is the difference between SDLC and STLC?**

**A:**

| Aspect | SDLC | STLC |
|---|---|---|
| **Scope** | Entire software development process | Testing activities only |
| **Who owns it** | Project manager / development team | QA / test manager |
| **Goal** | Build working software | Verify and validate the built software |
| **Phases** | Requirements, Design, Code, Test, Deploy, Maintain | Requirement Analysis, Planning, Design, Environment Setup, Execution, Closure |
| **Start** | When a project is initiated | When requirements are first available |
| **End** | When software is retired | When testing is complete and signed off |
| **Relationship** | STLC is a subset of SDLC | STLC runs within the Testing phase of SDLC (and earlier, with shift-left) |

**Interview answer tip:** SDLC is the parent process; STLC is QA's process within it. The STLC does not wait for the SDLC's "Testing" phase to begin — with shift-left testing, QA starts the STLC (requirement analysis, test planning) as soon as requirements are available.

---

## SECTION 2: TEST LEVELS AND THE PYRAMID

---

**Q4: What are the test levels? Explain each with a concrete example.**

**A:**

Test levels are distinct groups of testing activities, each focusing on a different part of the system and typically done at different times by different people.

**Level 1: Unit Testing**

Tests individual components (methods, functions, classes) in complete isolation. Dependencies are replaced with mocks or stubs.

- Who writes: Developers
- Tools: JUnit, NUnit, Jest, Pytest, Mocha
- Speed: Very fast (milliseconds per test)
- Example: Testing that a `calculateTax(amount, rate)` method returns the correct value for various inputs without calling a database or API

```java
@Test
void calculateTax_withPositiveAmountAndRate_returnsCorrectTax() {
    TaxCalculator calc = new TaxCalculator();
    double tax = calc.calculateTax(100.00, 0.15);
    assertEquals(15.00, tax, 0.001, "Tax should be 15% of 100");
}
```

**Level 2: Integration Testing**

Tests that two or more components work together correctly. Real dependencies (databases, services) may be used or partially mocked.

- Who writes: Developers + QA
- Tools: TestNG, RestAssured, Postman, Spring Test
- Example: Testing that the `OrderService.placeOrder()` method correctly calls the `InventoryRepository` to reduce stock and then calls the `PaymentService` to charge the customer — all three components working together

**Level 3: System Testing**

Tests the complete, integrated system against the specified requirements. This is the primary QA level.

- Who writes: QA
- Tools: Selenium, Playwright, RestAssured, Cypress
- Example: Testing the full user registration flow through the UI — fill in name, email, password, submit, verify confirmation email, click link, verify account is active — end-to-end through the real stack

**Level 4: Acceptance Testing**

Verifying the system meets business requirements and is ready for delivery. Done from the user perspective.

- Who does it: Business stakeholders + QA
- Types: UAT (User Acceptance Testing), Alpha testing, Beta testing
- Example: A business user testing the new expense approval workflow in a UAT environment against a real business scenario, confirming it matches the agreed process

---

**Q5: What is the test pyramid? Why is it important?**

**A:**

The test pyramid (by Mike Cohn) is a model for the ideal distribution of test types in a test suite. It represents the recommended ratio of tests at each level.

```
            /\
           /  \
          / UI \            <- Fewest tests (slow, brittle, expensive)
         /------\
        /  API   \          <- More tests (fast, stable, good coverage)
       /----------\
      /  Unit Tests \       <- Most tests (very fast, very cheap, very stable)
     /--------------\
```

**Why the pyramid shape matters:**

| Layer | Count | Speed | Cost | Stability |
|---|---|---|---|---|
| Unit | Many (60–70% of tests) | Milliseconds | Very cheap | Very stable |
| API/Integration | Moderate (20–30%) | Seconds | Moderate | Stable |
| UI/E2E | Few (5–10%) | Minutes | Expensive | Brittle |

**The anti-pattern — "ice cream cone":** Heavy reliance on UI tests with few unit tests. This produces slow, flaky pipelines. Signs you have an ice cream cone: suite takes 2+ hours, tests fail for no apparent reason, every change breaks multiple tests.

**Practical guidance for QA:**
- Resist the temptation to test everything through the UI
- Push coverage down to the API layer wherever the UI test is really just verifying business logic
- UI tests should verify the user journey and visual integration, not every business rule

---

**Q6: What is the difference between functional and non-functional testing?**

**A:**

**Functional testing** tests what the system does — whether the features and business logic work correctly according to requirements.

| Examples | What is being tested |
|---|---|
| User can log in with correct credentials | Login feature works |
| Cart total updates when item is added | Calculation logic is correct |
| Error message shown for invalid email | Input validation works |
| API returns 200 with correct JSON for GET /users | API contract is met |

**Non-functional testing** tests how the system behaves, not what it does. It tests quality characteristics.

| Type | What is being tested | Tools |
|---|---|---|
| Performance | Speed, throughput, response time under load | JMeter, k6, Gatling |
| Security | Vulnerabilities, authentication, data protection | OWASP ZAP, Burp Suite |
| Usability | Ease of use, accessibility, user experience | User testing, WCAG audit |
| Reliability | Uptime, fault tolerance, recovery | Chaos engineering |
| Compatibility | Works across browsers, OS, devices | BrowserStack, Selenium Grid |
| Scalability | How well it handles growing load | Load testing at 2x, 5x scale |
| Localisation | Correct language, date formats, currency | Manual + automation |

**Key interview point:** Functional testing verifies "it works." Non-functional testing verifies "it works well." Both are essential for a quality product.

---

## SECTION 3: TESTING TYPES

---

**Q7: What is regression testing? When do you do it and what strategies exist?**

**A:**

Regression testing is the practice of re-running existing tests after code changes to ensure that previously working functionality has not been broken. Every change — a bug fix, a new feature, a refactor — carries the risk of introducing unintended side effects (regressions).

**When to run regression tests:**
- After every bug fix (did fixing bug A break something else?)
- After every new feature is added
- After refactoring or code restructuring
- Before every release
- Automatically on every commit (in a CI pipeline)

**Regression testing strategies:**

**Full regression:** Run every test in the entire suite.
- Pro: Maximum coverage
- Con: Slow; impractical for daily runs on large suites

**Selective regression:** Run only tests related to changed code modules and their dependencies.
- Pro: Faster; proportional to change scope
- Con: Requires good traceability between tests, requirements, and code

**Risk-based regression:** Prioritise tests by risk — always run high-risk tests, run medium-risk when time allows, skip low-risk until release.
- Pro: Smart allocation of limited time
- Con: Requires risk analysis before execution

**Automated regression (in CI/CD):**
- Smoke tests run on every commit (5–10 minutes)
- Full regression runs nightly or on release branches (30–60 minutes)

**Regression suite maintenance rules:**
- Remove tests for deleted features
- Update tests when requirements change
- Fix or quarantine flaky tests monthly
- Split the suite if it exceeds 30 minutes — parallelise it

---

**Q8: What is the difference between smoke testing and sanity testing? (Most frequently asked question)**

**A:**

This is asked in almost every QA interview. The two terms are often confused or used interchangeably, but they are distinct in purpose, scope, and timing.

**Smoke Testing:**

Smoke testing is a broad, shallow test of the most critical system functions. The goal is to answer: "Is the build stable enough to test further?" It does not test features in depth — it just checks that the application is up and running and the core paths work.

- Also called: Build Verification Test (BVT)
- Scope: Wide but shallow — touches many features briefly
- Who runs it: QA (often automated in CI)
- When: Immediately after a new build is deployed
- Duration: 10–20 minutes
- If smoke fails: The build is rejected immediately; no further testing proceeds

**Smoke test examples:**
- Application launches without error
- Login page loads
- User can log in with valid credentials
- Dashboard is visible after login
- API health endpoint returns 200
- Database connection is established

**Sanity Testing:**

Sanity testing is a narrow, deep verification of a specific area that has just been changed or fixed. The goal is to answer: "Does this specific fix/change work correctly?" It is done after receiving a build with targeted changes.

- Also called: Narrow regression
- Scope: Narrow but deep — focuses on the changed area only
- Who runs it: QA
- When: After receiving a new build with specific bug fixes or changes
- Duration: Varies (30 minutes to a few hours for the relevant module)
- If sanity fails: This specific area needs rework; broader regression may or may not proceed

**Sanity test examples:**
- Login with uppercase email was fixed → verify login works with uppercase, lowercase, mixed case
- Cart total calculation was fixed → verify all discount/tax/coupon combinations in the cart
- API pagination was fixed → verify page 1, page 2, last page, empty page

**Side-by-side comparison:**

| Aspect | Smoke Testing | Sanity Testing |
|---|---|---|
| **Purpose** | Is the build stable? | Does this specific fix work? |
| **Scope** | Wide, shallow | Narrow, deep |
| **Coverage** | Many features briefly | One area thoroughly |
| **When** | After every new build | After targeted fixes |
| **Documentation** | Sometimes scripted, often automated | Usually undocumented (informal) |
| **If it fails** | Build rejected; no further testing | Only the changed area needs rework |
| **Analogy** | Checking if a car starts | Checking if the repaired brake works |

**Memory trick:** Smoke = "Does the smoke detector beep?" (quick whole-system check). Sanity = "Are we sure this one thing makes sense?" (targeted deep check on the change).

---

**Q9: What is exploratory testing? How do you structure a session?**

**A:**

Exploratory testing means simultaneously designing and executing tests while learning about the system. Unlike scripted testing, there are no pre-written test cases to follow step by step. The tester uses skill, intuition, and domain knowledge to investigate areas that might have problems.

**Definition (ISTQB):** "A type of experience-based testing in which the tester simultaneously designs and executes tests and uses information gained while testing to design new and better tests."

**When exploratory testing is most valuable:**
- New features with incomplete or evolving requirements
- When you suspect something is wrong but automated tests pass
- Usability and user experience assessment
- Risk-based investigation of complex workflows
- When a bug has been fixed and you want to probe the surrounding area

**Session-Based Exploratory Testing (SBET) — the structured approach:**

```
1. MISSION / CHARTER
   Define what you are exploring and why.
   Example: "Explore the checkout flow focusing on discount code application,
   particularly interactions between multiple codes and partial discounts"

2. TIME-BOX
   Typically 60–90 minutes per session.
   Set a timer; do not extend the session.

3. EXECUTE
   - Start with the most likely risk areas
   - Keep notes of what you tested, what you found, and questions raised
   - Take screenshots of anything unusual
   - Log defects immediately

4. SESSION NOTES TEMPLATE
   Charter: [Your mission]
   Duration: 90 minutes
   Tester: [Name]
   Build: [Version]
   Areas Tested: [Summary]
   Bugs Found: [List with ticket numbers]
   Issues/Questions: [Unresolved questions]
   Observations: [Interesting behaviour worth monitoring]

5. DEBRIEF
   Brief the test lead/team on findings
   Convert important discoveries to formal test cases
   Flag risks for re-testing in the next session
```

**Exploratory vs Scripted testing:**

| | Scripted Testing | Exploratory Testing |
|---|---|---|
| **Test cases** | Written in advance | Designed during execution |
| **Documentation** | Detailed procedures | Session notes and charters |
| **Coverage tracking** | Easy (test case execution %) | Hard to measure |
| **Finding unexpected bugs** | Limited — only covers what was anticipated | Strong — tester follows their instincts |
| **Best for** | Regression, compliance, onboarding | New features, complex areas, risk investigation |

---

## SECTION 4: TEST DESIGN TECHNIQUES

---

**Q10: What is the difference between verification and validation? Give examples.**

**A:**

These two terms are foundational ISTQB concepts that are frequently confused.

**Verification** asks: "Are we building the product right?"
It checks whether the product conforms to its specification, standards, and design documents. Verification is done without actually running the software.

**Validation** asks: "Are we building the right product?"
It checks whether the product meets the actual needs of the user. Validation involves running and using the actual software.

| Aspect | Verification | Validation |
|---|---|---|
| Question | "Are we building it right?" | "Are we building the right thing?" |
| Focus | Process, documents, specifications | The actual running system |
| Timing | Throughout development | Usually at the end of a phase |
| Techniques | Reviews, walkthroughs, inspections, static analysis | Testing, demonstrations, user trials |
| Example 1 | Reviewing the test plan against IEEE 829 standards | Running the application and executing tests |
| Example 2 | Checking the API specification for completeness | Calling the API and verifying responses |
| Example 3 | Reviewing requirements for ambiguity | Demonstrating the system to the client for approval |
| Who | QA + developers + business analysts | QA + stakeholders + users |

**Memory trick:** Verification = checking the Spec (both start with different letters but think V=Verify against specification). Validation = checking the Value to the user.

---

**Q11: What is equivalence partitioning? Give a real example with all partitions identified.**

**A:**

Equivalence partitioning (EP) is a black-box test design technique that divides possible input data into groups (partitions) where every value in the group is expected to behave identically. You test one representative value from each partition, dramatically reducing the number of test cases while maintaining coverage.

**Why:** There is no value in testing every possible input if inputs in the same range produce the same behaviour. If `age=30` and `age=45` both produce "valid", testing both is redundant.

**Rule:** One valid partition, one or more invalid partitions. Test exactly one value from each.

**Example 1: Username field — must be 5–15 characters**

| Partition | Range | Representative Value | Expected Result |
|---|---|---|---|
| Invalid — too short | 0–4 characters | 3 characters ("abc") | Error: "Username must be 5–15 characters" |
| Valid | 5–15 characters | 10 characters ("johnsmith1") | Accepted |
| Invalid — too long | 16+ characters | 20 characters ("johnnydoesmith2024") | Error: "Username must be 5–15 characters" |

Minimum 3 test cases instead of testing every possible length.

**Example 2: Age field for a loan application — only ages 18–65 qualify**

| Partition | Range | Representative Value | Type |
|---|---|---|---|
| Invalid — underage | Below 18 | 15 | Invalid |
| Valid | 18–65 | 35 | Valid |
| Invalid — over retirement age | Above 65 | 70 | Invalid |

**Example 3: HTTP status code handling**

| Partition | Status Codes | Test Value | Expected Handling |
|---|---|---|---|
| Success | 200–299 | 200 | Process response body |
| Redirection | 300–399 | 301 | Follow redirect |
| Client error | 400–499 | 404 | Show "Not Found" message |
| Server error | 500–599 | 500 | Show "Service unavailable" |

**Key interview point:** EP reduces test count while ensuring each class of behaviour is covered. It does not guarantee edge-case coverage — that is why it is always combined with boundary value analysis.

---

**Q12: What is boundary value analysis? Give an exact example with all boundary values listed.**

**A:**

Boundary Value Analysis (BVA) is a black-box technique that focuses on the edges (boundaries) of equivalence partitions. Defects are statistically most likely to occur at the boundary between partitions — an off-by-one error in a condition like `if (age >= 18)` is a classic example.

**BVA extends EP by testing at and around each boundary:**

**2-value BVA:** Test the boundary itself and one value just outside (invalid side). More common in practice.

**3-value BVA (ISTQB Foundation):** Test one value just below the boundary, the boundary itself, and one value just above. More thorough.

**Full example: Discount applies if order total is £50–£200 (inclusive)**

Boundaries: Lower boundary at £50. Upper boundary at £200.

| Test Value | BVA Point | Expected Result |
|---|---|---|
| £49 | Just below lower boundary | No discount |
| £50 | At lower boundary (minimum valid) | Discount applies |
| £51 | Just above lower boundary | Discount applies |
| £100 | Midpoint (EP representative value) | Discount applies |
| £199 | Just below upper boundary | Discount applies |
| £200 | At upper boundary (maximum valid) | Discount applies |
| £201 | Just above upper boundary | No discount |

**Why test £51 and £199?** Because a developer might accidentally write `> 50` instead of `>= 50`, or `< 200` instead of `<= 200`. The values just inside the boundary catch these off-by-one errors.

**Example: Password field — must be 8–20 characters**

| Value | BVA Point | Expected |
|---|---|---|
| 7 chars | Below minimum | Error |
| 8 chars | At minimum | Accepted |
| 9 chars | Just above minimum | Accepted |
| 14 chars | Midpoint (EP value) | Accepted |
| 19 chars | Just below maximum | Accepted |
| 20 chars | At maximum | Accepted |
| 21 chars | Just above maximum | Error |

7 test cases cover all important boundary behaviour.

**EP + BVA together:** Use EP first to identify the partitions. Then apply BVA at the boundary of each partition. They complement each other perfectly.

---

**Q13: What is decision table testing? Show a complete real-world example.**

**A:**

Decision table testing is a black-box technique for systematically testing combinations of conditions. It is most effective when the system has multiple input conditions that each independently affect the output, and different combinations lead to different actions.

**When to use:** Pricing rules, discount logic, permission systems, login workflows, loan approval systems.

**Construction steps:**
1. Identify all conditions (inputs)
2. Identify all actions (outputs/outcomes)
3. List all possible combinations of conditions
4. For each combination, determine the action
5. Each column in the table becomes one test case

**Example: Online loan approval**

Conditions:
- Credit score: Good or Poor
- Annual income: Above £30k or Below £30k
- Has collateral: Yes or No

With 3 binary conditions: 2³ = 8 possible combinations.

| | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 |
|---|---|---|---|---|---|---|---|---|
| Credit score | Good | Good | Good | Good | Poor | Poor | Poor | Poor |
| Income ≥ £30k | Yes | Yes | No | No | Yes | Yes | No | No |
| Has collateral | Yes | No | Yes | No | Yes | No | Yes | No |
| **Action** | **Approve** | **Approve** | **Approve** | **Reject** | **Approve** | **Review** | **Review** | **Reject** |

Each column is one test case. This gives you 8 tests that cover every possible combination.

**Collapsed decision table (merge equivalent rules):**

If rules T1, T2, T3 all result in Approve and the third condition (collateral) does not matter, you can collapse them:

| | T1-3 (collapsed) | T4 | T5 | T6 | T7 | T8 |
|---|---|---|---|---|---|---|
| Credit score | Good | Good | Poor | Poor | Poor | Poor |
| Income ≥ £30k | - (any) | No | Yes | Yes | No | No |
| Has collateral | - (any) | No | Yes | No | Yes | No |
| **Action** | **Approve** | **Reject** | **Approve** | **Review** | **Review** | **Reject** |

**Real QA application:**

```
Feature: Shopping cart discount
Conditions:
  - Is a Premium member?   (Yes/No)
  - Order total > £100?    (Yes/No)
  - Has valid coupon code? (Yes/No)

| | T1 | T2 | T3 | T4 | T5 | T6 | T7 | T8 |
|-|----|----|----|----|----|----|----|----|
| Premium member | Y | Y | Y | Y | N | N | N | N |
| Order > £100   | Y | Y | N | N | Y | Y | N | N |
| Valid coupon   | Y | N | Y | N | Y | N | Y | N |
| Discount       | 25% | 20% | 15% | 10% | 15% | 10% | 5% | 0% |
```

Each column is one automated test case with that exact combination of inputs.

---

**Q14: What is state transition testing? Give an example with a state diagram.**

**A:**

State transition testing is a black-box technique used when the system can exist in different states, and events (inputs/actions) cause transitions between states. It is used to verify that the system responds correctly to valid transitions and correctly rejects invalid ones.

**When to use:** Order lifecycle, booking systems, user account status, workflow approval processes, ATM state machine.

**Example: Online order lifecycle**

States: Created, Paid, Shipped, Delivered, Cancelled, Returned

Events: Pay, Ship, Deliver, Cancel, Return

```
[Created] ---pay---> [Paid] ---ship---> [Shipped] ---deliver---> [Delivered]
    |                  |                    |
    |---cancel--> [Cancelled]  ---cancel--> [Cancelled]
                                   |---return---> [Returned]
```

**State transition table:**

| Current State | Event | Next State | Action Taken |
|---|---|---|---|
| Created | Pay | Paid | Send payment confirmation email |
| Created | Cancel | Cancelled | Send cancellation; release inventory |
| Created | Ship | ERROR | Invalid — cannot ship unpaid order |
| Paid | Ship | Shipped | Send tracking number |
| Paid | Cancel | Cancelled | Issue refund; release inventory |
| Paid | Pay | ERROR | Invalid — already paid |
| Shipped | Deliver | Delivered | Send delivery confirmation |
| Shipped | Return | Returned | Initiate return process |
| Delivered | Return | Returned | Open return case |
| Cancelled | Pay | ERROR | Cannot pay cancelled order |
| Returned | — | — | Terminal state |

**Tests to derive:**
1. Happy path: Created → Paid → Shipped → Delivered
2. Cancellation from Created state
3. Cancellation from Paid state (verify refund is issued)
4. Return from Shipped state
5. Invalid transition: attempt to ship a Created (unpaid) order — verify rejection
6. Invalid transition: attempt to pay a Cancelled order — verify rejection

**Coverage levels (ISTQB):**
- All states covered (visit every state at least once)
- All transitions covered (exercise every arrow at least once)
- All transition pairs covered (every sequence of two transitions)

---

**Q15: What is use case testing?**

**A:**

Use case testing derives test cases from use cases — descriptions of how a user interacts with the system to achieve a goal. Each use case has a basic flow (happy path), alternative flows (valid variations), and exception flows (errors and edge cases).

**Use case: User checks out a shopping cart**

Basic flow (happy path):
1. User views cart with items
2. User enters shipping address
3. User selects delivery option
4. User enters payment details
5. User clicks "Place Order"
6. System confirms order and sends confirmation email

Alternative flows:
- ALT1: User applies a valid discount code before payment
- ALT2: User changes the shipping address on the payment page
- ALT3: User removes an item from the cart during checkout

Exception flows:
- EXC1: Payment is declined → show error, allow retry with different card
- EXC2: Item goes out of stock during checkout → notify user, offer alternatives
- EXC3: Session times out → redirect to login, preserve cart
- EXC4: Shipping address is in a non-supported region → show unavailability message

**Derived test cases:**
- TC01: Complete checkout with valid card (basic flow)
- TC02: Apply valid coupon → verify discount applied → complete checkout (ALT1)
- TC03: Enter declined card → verify error message → enter valid card → complete checkout (EXC1)
- TC04: Add item, remove it during checkout, verify cart updates → complete checkout (ALT3)
- TC05: Complete checkout, verify confirmation email is received (basic flow + post-condition)

**Use case testing is particularly effective because it tests the system from the user's perspective rather than testing individual functions in isolation.**

---

**Q16: What is statement coverage, branch coverage, and path coverage? How do they differ?**

**A:**

These are white-box testing coverage criteria that measure how much of the source code is exercised by your tests.

**Statement Coverage (Line Coverage)**

Goal: Every executable statement is executed at least once.

```java
public String classify(int n) {
    String result = "zero";              // Line 1 — always runs
    if (n > 0) {                         // Line 2 — decision
        result = "positive";             // Line 3 — only if n > 0
    }
    if (n < 0) {                         // Line 4 — decision
        result = "negative";             // Line 5 — only if n < 0
    }
    return result;                       // Line 6 — always runs
}
```

To achieve 100% statement coverage:
- Test with n=5 → executes lines 1, 2, 3, 4, 6 (misses line 5)
- Test with n=-3 → executes lines 1, 2, 4, 5, 6 (misses line 3)

Two tests: n=5 and n=-3 achieve 100% statement coverage but miss n=0.

**Branch Coverage (Decision Coverage)**

Goal: Every branch (true AND false) of every decision point is executed at least once.

For the example above, branch coverage requires:
- `n > 0` is TRUE (line 3 executes) AND FALSE (line 3 skipped)
- `n < 0` is TRUE (line 5 executes) AND FALSE (line 5 skipped)

Three tests needed:
- n=5 → `n>0` TRUE, `n<0` FALSE
- n=-3 → `n>0` FALSE, `n<0` TRUE
- n=0 → both FALSE (would have been missed by statement coverage)

**Branch coverage is stronger than statement coverage.** 100% branch coverage implies 100% statement coverage, but not vice versa.

**Path Coverage**

Goal: Every unique execution path through the code is executed.

The number of paths grows exponentially with conditions. For the example: 4 paths (TT, TF, FT, FF). In complex code this becomes impractical.

Used in: safety-critical systems (aviation, medical devices).

**Cyclomatic Complexity (McCabe)**

Measures the number of linearly independent paths through the code:
```
CC = Number of decision points + 1

For the example above:
  Decision points: 2 (two if statements)
  CC = 2 + 1 = 3

CC also = minimum number of tests for branch coverage
```

| CC Value | Risk Level | Action |
|---|---|---|
| 1–5 | Simple, low risk | Normal testing |
| 6–10 | Moderate | Consider refactoring |
| > 10 | High complexity, high risk | Must refactor; thoroughly test |

---

**Q17: How do you measure code coverage in a Java project? Give the tooling and a real example.**

**A:**

Code coverage is measured by running your test suite with a coverage agent that tracks which lines, branches, and methods are executed.

**Tool: JaCoCo (Java Code Coverage)**

JaCoCo is the standard coverage tool for Java/Maven projects.

**pom.xml configuration:**

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <!-- Attach JaCoCo agent before tests run -->
                <execution>
                    <id>prepare-agent</id>
                    <goals><goal>prepare-agent</goal></goals>
                </execution>
                <!-- Generate report after tests finish -->
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals><goal>report</goal></goals>
                </execution>
                <!-- Enforce minimum coverage threshold (build fails if not met) -->
                <execution>
                    <id>check</id>
                    <goals><goal>check</goal></goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>BUNDLE</element>
                                <limits>
                                    <limit>
                                        <counter>INSTRUCTION</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.80</minimum>  <!-- 80% instruction coverage required -->
                                    </limit>
                                    <limit>
                                        <counter>BRANCH</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.70</minimum>  <!-- 70% branch coverage required -->
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

**Run and view coverage:**

```bash
# Run tests and generate coverage report
mvn test

# Report generated at: target/site/jacoco/index.html
# Open in browser to see coverage by class, method, line, and branch
```

**Coverage report output example:**

```
Class: OrderService
  Instructions: 85% (170/200 covered)
  Branches:     75% (18/24 covered)
  Methods:      100% (12/12 covered)
  Lines:        90% (54/60 covered)

Uncovered branches in method: calculateShipping()
  Line 45: if (isInternational) -- FALSE branch not tested
  Line 67: if (weight > 50) -- TRUE branch not tested
```

**In CI/CD pipeline:**

```groovy
// Jenkinsfile — enforce coverage threshold
stage('Code Coverage') {
    steps {
        sh 'mvn jacoco:check'   // Fails build if coverage is below threshold
    }
    post {
        always {
            publishHTML([
                reportDir: 'target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report'
            ])
        }
    }
}
```

---

## SECTION 5: DEFECT MANAGEMENT

---

**Q18: What is the defect lifecycle? Walk through all states and who is responsible for each.**

**A:**

The defect lifecycle (also called bug lifecycle) tracks a defect from discovery to resolution. Different organisations use slightly different states, but the core flow is consistent.

**Full defect lifecycle:**

```
          [New] ──────────────────────────────────────┐
            |                                         |
            v                                         v
       [Assigned]                               [Rejected]
            |                                    (not a bug /
            v                                    cannot reproduce /
          [Open]                                 works as designed)
            |
       ┌────┴────┐
       v         v
  [Deferred]  [Fixed]
  (known, not   |
  fixing now)   v
              [Retest]
                |
          ┌─────┴─────┐
          v           v
       [Closed]   [Reopened] ──> [Open]
```

**State descriptions:**

| State | Description | Responsible |
|---|---|---|
| **New** | QA has logged the defect; it has not been reviewed yet | QA |
| **Assigned** | The defect has been allocated to a developer for investigation | Lead / PM |
| **Open** | Developer has accepted the defect and is working on a fix | Developer |
| **Fixed** | Developer has applied the fix and deployed to the test environment | Developer |
| **Retest** | QA is verifying that the fix resolves the defect | QA |
| **Closed** | QA verified the fix; defect is resolved | QA only |
| **Reopened** | QA retested and the defect is still present; fix did not work | QA only |
| **Deferred** | Acknowledged as a real bug but will not be fixed in this release | PM / Lead |
| **Rejected** | Determined not to be a defect (works as designed, duplicate, cannot reproduce) | Developer / Lead |
| **Duplicate** | Same defect as an existing open ticket; this copy is closed | QA / Lead |

**Critical rules:**
- **Only QA closes a defect** — never the developer who fixed it
- **Only QA reopens a defect** — after failed retest with evidence (screenshot, steps)
- **Deferred ≠ Closed** — a deferred defect still exists; it just will not be fixed now
- **Rejected requires a reason** — QA may escalate if they disagree with the rejection

**What makes a good defect report:**

```
Title:       "Checkout fails when applying two coupon codes simultaneously — total not recalculated"
             (NOT: "Coupon doesn't work")

Environment: Chrome 124, Windows 11, Staging URL: https://staging.myapp.com
Build:       v2.3.1-build-456

Steps to Reproduce:
1. Add 3 items to cart (total: £85.00)
2. Apply coupon code "SAVE10" → total becomes £76.50 ✓
3. Apply coupon code "MEMBER5" → total should become £72.68
4. Observe: total remains £76.50; second coupon is accepted but not applied

Expected: Total = £72.68 (10% + 5% stacked discounts applied)
Actual:   Total = £76.50 (only first coupon applied)

Severity: Major
Priority: High

Attachments: screenshot_01.png, console_errors.txt
```

---

**Q19: What is the difference between severity and priority? Give examples of all four combinations.**

**A:**

**Severity:** The technical impact of the defect on the system. "How badly does this break the system?" Set by QA.

**Priority:** The business urgency of fixing the defect. "How quickly must this be fixed?" Set by Product Manager / Business.

These are independent of each other. A cosmetic defect can be high priority if it affects brand reputation. A catastrophic crash can be low priority if it only affects a rarely-used feature.

**Severity levels:**

| Level | Description | Example |
|---|---|---|
| Critical (S1) | System is unusable; core function completely broken; data loss | Payment API crashes; login doesn't work for anyone; data is deleted |
| Major (S2) | Major feature is broken; workaround exists | PDF export fails; search returns wrong results |
| Minor (S3) | Feature works with minor issues | Dropdown alphabetical order wrong; tooltip text incorrect |
| Trivial (S4) | Cosmetic; no functional impact | Button misaligned by 2px; wrong shade of grey |

**Priority levels:**

| Level | Description | Example |
|---|---|---|
| P1 - Critical | Fix immediately; blocks release | Homepage shows 500 error |
| P2 - High | Fix in current sprint | Core workflow broken for many users |
| P3 - Medium | Fix in near-term release | Non-critical feature partially broken |
| P4 - Low | Fix when time permits | Minor cosmetic defect in admin panel |

**The 4 classic combinations:**

**High Severity + High Priority (S1/P1):**
The payment gateway is completely down — no customer can complete a purchase.
- Severity: Critical (core function broken, revenue loss)
- Priority: Critical (must fix right now — every minute costs money)

**High Severity + Low Priority (S1/P4):**
The legacy batch report export crashes the system — but it only runs once a month and only one internal accountant uses it.
- Severity: Critical (system crashes when the feature is used)
- Priority: Low (almost nobody uses it; fix can wait until next sprint)

**Low Severity + High Priority (S4/P1):**
The company's name is spelled "Amazone" instead of "Amazon" on the homepage hero banner.
- Severity: Trivial (no functional impact at all)
- Priority: Critical (visible to every user; brand reputation; fix immediately)

**Low Severity + Low Priority (S4/P4):**
An icon in the internal admin panel is 3 pixels off-centre.
- Severity: Trivial (cosmetic only)
- Priority: Low (almost no one sees it; fix whenever convenient)

**Interview trick question:** "The login button is missing from the homepage."
- Severity: Critical (no user can log in — core function completely broken)
- Priority: Critical (affects every user immediately — fix now)

---

**Q20: What is a bug escape? How do you prevent it?**

**A:**

A bug escape (also called defect leakage) is a defect that was not found during testing and was discovered by end users in production.

**Why bug escapes happen:**
- Insufficient test coverage (area was not tested)
- Risk was misjudged (the area was deprioritised)
- Edge case was not identified (unusual combination of inputs)
- Defect was introduced after testing was complete (late code change)
- Test data did not match real-world data
- Environment differences (production config differs from test config)
- Regression gap (fix for one bug introduced a new one in an untested area)

**Metrics to measure bug escapes:**

```
Defect Escape Rate = (Defects found in production / Total defects found) × 100

Example: 5 production bugs / 100 total bugs × 100 = 5% escape rate

Defect Removal Efficiency (DRE) = (Defects found before release / Total defects) × 100
                                 = (95 / 100) × 100 = 95% DRE
```

Higher DRE and lower escape rate = more effective testing.

**Prevention strategies:**

1. **Risk-based testing** — allocate most effort to highest-risk areas so coverage is proportional to risk
2. **Exploratory testing** — scripted tests miss unexpected scenarios; exploratory finds edge cases
3. **Code coverage gates** — enforce minimum branch coverage so untested code cannot be merged
4. **Production-like test data** — use anonymised real data in testing, not just "happy path" data
5. **Regression automation** — automated regression catches regressions from every code change
6. **Three Amigos** — QA identifies test conditions before coding begins, not after
7. **Shift-left** — test earlier; review requirements; find defects before they are coded
8. **Post-release monitoring** — track production errors; correlate with test gaps; update test suite
9. **Root cause analysis** — when an escape occurs, ask "what test should have caught this?" and add it

---

## SECTION 6: RISK AND TEST PLANNING

---

**Q21: What is risk-based testing? How do you prioritise your test effort when time is limited?**

**A:**

Risk-based testing prioritises the test effort based on the risk level of different parts of the system. It acknowledges that exhaustive testing is impossible — you cannot test every combination of inputs — so you must make smart decisions about what to test first and most thoroughly.

**Risk formula:**

```
Risk = Probability of Failure × Impact of Failure

High probability + High impact = Critical risk (test first and most thoroughly)
Low probability + Low impact  = Minimal risk (test last or skip if time runs out)
```

**Step-by-step risk assessment process:**

**Step 1: List features/areas**

```
- User login
- Payment processing
- Product search
- Admin reporting
- Footer links
- User profile update
- Email notifications
```

**Step 2: Score probability of failure (1–3 scale)**

| Score | Meaning | Indicators |
|---|---|---|
| 3 - High | Likely to fail | New code, complex logic, many dependencies, new developer |
| 2 - Medium | Might fail | Modified existing code, moderate complexity |
| 1 - Low | Unlikely to fail | Stable, mature, unchanged for 6+ months |

**Step 3: Score impact of failure (1–3 scale)**

| Score | Meaning | Indicators |
|---|---|---|
| 3 - High | Business critical | Revenue loss, legal risk, data loss, security |
| 2 - Medium | Significant impact | Core feature degraded, many users affected |
| 1 - Low | Minor | Cosmetic issue, few users, easy workaround |

**Step 4: Calculate and rank**

| Feature | Probability | Impact | Risk Score | Test Priority |
|---|---|---|---|---|
| Payment processing | 3 (new) | 3 (revenue) | 9 | CRITICAL — test first, thoroughly |
| User login | 2 (modified) | 3 (everyone needs it) | 6 | HIGH |
| Product search | 2 (modified) | 2 (UX impact) | 4 | MEDIUM |
| User profile update | 1 (stable) | 2 (moderate) | 2 | LOW |
| Admin reporting | 1 (stable) | 1 (few users) | 1 | MINIMAL — skip if time runs out |
| Footer links | 1 (unchanged) | 1 (cosmetic) | 1 | MINIMAL |

**Step 5: Allocate test effort proportionally**
- Critical (score 8–9): Full testing, multiple techniques, automation, exploratory
- High (score 5–7): Standard functional testing + key boundary values
- Medium (score 3–4): Basic positive and negative tests
- Low (score 1–2): Quick smoke check or skip

---

**Q22: What is a test plan? What sections does it contain?**

**A:**

A test plan is the master document that defines the entire testing effort for a project or release. It answers: What will be tested, how, by whom, when, and with what resources. It serves as the contract between QA and stakeholders.

**Test plan sections (based on IEEE 829 / ISTQB):**

| Section | Content |
|---|---|
| 1. Introduction / Purpose | Why this test plan exists; project name; version; references |
| 2. Scope of Testing | What IS being tested (in scope) and what is NOT being tested (out of scope) — both must be explicit |
| 3. Test Objectives | Goals: find defects, gain confidence, comply with regulation, verify requirements |
| 4. Test Strategy | Test levels (unit/integration/system), test types (functional/performance), techniques (EP, BVA), tools |
| 5. Entry and Exit Criteria | Conditions for starting testing; conditions for stopping |
| 6. Test Items | Specific features, modules, or requirements under test |
| 7. Features Not Tested | Explicitly excluded features with reasons (out of scope, not yet built, separate team) |
| 8. Test Environment | Browsers, OS, devices, test servers, database setup, test data approach |
| 9. Test Schedule | Start date, end date, milestones, dependencies |
| 10. Resources and Roles | Team members, responsibilities (who writes cases, who executes, who reviews) |
| 11. Risks and Contingencies | What could go wrong (unstable environment, resource shortage); mitigation plan |
| 12. Deliverables | What QA will produce: test cases, defect report, test summary report |
| 13. Approval | Sign-off section with names and dates |

**Entry and Exit Criteria examples:**

```
Entry Criteria (when to START testing):
- Build has been deployed to test environment
- Smoke test passes (application loads and basic navigation works)
- Test cases have been written, reviewed, and approved
- Test data is set up and accessible
- No critical-priority open defects from previous sprint

Exit Criteria (when to STOP testing and sign off):
- 95% or more of planned test cases have been executed
- 100% of Critical and High severity defects are closed
- No more than 5% open Minor or Trivial defects
- Test coverage meets 80% (as measured by requirements coverage)
- Test summary report written and reviewed
```

---

## SECTION 7: TEST METRICS

---

**Q23: What are test metrics? List and explain the most important ones.**

**A:**

Test metrics are quantitative measures of the testing process that help teams understand quality, progress, and effectiveness. Without metrics, QA status is just opinion.

**Defect-based metrics:**

**Defect Density:**
```
Defect Density = Number of Defects Found / Size of Component

Example: 15 defects / 5,000 lines of code = 3 defects per KLOC

Lower is better. Compare components to identify the most defect-prone areas.
Used to decide where to focus testing effort.
```

**Defect Removal Efficiency (DRE):**
```
DRE = (Defects found before production / Total defects) × 100

Example: 90 found in testing, 10 escaped to production
DRE = (90 / 100) × 100 = 90%

Higher is better. World-class teams target > 95% DRE.
```

**Defect Escape Rate / Defect Leakage:**
```
Defect Leakage = (Defects found in production / Total defects) × 100

Example: 10 production bugs / 100 total bugs = 10% leakage

Lower is better. This is the complement of DRE.
```

**Mean Time to Fix:**
```
MTTF = Total fix time for all defects / Number of defects fixed

Example: 300 hours / 30 defects = 10 hours per defect average

Measures how quickly the development team resolves defects.
```

**Test execution metrics:**

**Test Pass Rate:**
```
Pass Rate = (Tests Passed / Tests Executed) × 100

Example: 180 passed / 200 executed × 100 = 90% pass rate
```

**Test Execution Progress:**
```
Execution % = (Tests Executed / Total Tests Planned) × 100

Use to show sprint or release progress to stakeholders.
```

**Requirements Coverage:**
```
Requirements Coverage = (Requirements with test cases / Total requirements) × 100

Example: 80 requirements have test cases / 100 total requirements = 80% coverage

Ensures no requirement is left untested.
```

**Automation metrics:**

**Automation Coverage:**
```
Automation Coverage = (Automated test cases / Total test cases) × 100

Measures the maturity of the automation programme.
Target: 70–80% of regression suite automated.
```

**Reporting metrics to stakeholders:**

Always show trends, not just snapshots:

```
"Pass rate is 90% this week, up from 85% last week.
 We need 95% to meet our release exit criteria.
 At the current improvement rate, we will hit the threshold by Thursday."
```

A single number without context is meaningless. Trend + target + projection is what stakeholders need.

---

**Q24: What is defect density and how do you use it?**

**A:**

Defect density measures how many defects exist per unit of code (usually per 1,000 lines of code — KLOC).

```
Defect Density = Total defects found / Size of the module or system

Common size measures:
  - Lines of code (KLOC = 1,000 lines)
  - Function points
  - Number of requirements

Example:
  Module A: 35 defects / 7,000 LOC = 5 defects/KLOC   ← HIGH RISK
  Module B: 10 defects / 8,000 LOC = 1.25 defects/KLOC ← LOW RISK
```

**How QA uses defect density:**
- Identify which modules are the most defect-prone → allocate more testing effort there
- Compare current release density to historical data → is quality improving or degrading?
- Justify testing effort to management with data: "Module A has 4x the density of Module B"
- Set a threshold: builds with density > 8 defects/KLOC do not proceed to UAT

---

## SECTION 8: AGILE TESTING

---

**Q25: What is the Three Amigos meeting? What does QA contribute?**

**A:**

The Three Amigos is a technique where three perspectives collaboratively review each user story before development begins. The "three amigos" are:

**1. Product Owner (Business perspective):** "This is what I want and why it has business value."

**2. Developer (Technical perspective):** "This is how I plan to build it and what constraints exist."

**3. QA (Testing perspective):** "This is how I will verify it — and here are the edge cases none of you have thought of."

**The goal:** Shared understanding. All three parties agree on requirements, scope, acceptance criteria, and edge cases before a single line of code is written.

**What QA specifically contributes to Three Amigos:**

- Boundary values: "What happens if quantity is 0? Or negative? Or 9,999?"
- Negative scenarios: "What if the user has no payment method saved?"
- Error handling: "What is the behaviour when the payment API is down?"
- Permissions: "Can a regular user access this? What about an admin?"
- Non-functional concerns: "How many concurrent users will this support?"
- Integration questions: "Does this affect the notification service?"
- Acceptance criteria review: "How do we know this story is done?"

**Three Amigos prevents the most expensive type of defect: requirements ambiguity.** A misunderstood requirement that gets coded, tested, and deployed can cost 100x more to fix than clarifying it in a 30-minute meeting.

---

**Q26: What is shift-left testing? Why does it matter?**

**A:**

Shift-left testing means moving testing activities earlier in the development lifecycle — toward the "left" on a project timeline. Traditional development had testing as the last phase; shift-left integrates QA from the very beginning.

**Traditional vs shift-left:**

```
Traditional (shift-RIGHT):
Requirements → Design → Code → [TEST] → Deploy
                              ↑
                       QA gets involved here only

Shift-LEFT:
[QA] → Requirements → [QA] → Design → [QA] → Code → [QA] → Test → Deploy
  ↑                     ↑               ↑                ↑
Review req.        Review design    Write tests      Execute tests
for ambiguity      for testability  before code      in CI pipeline
```

**Why shift-left matters — defect cost multiplier:**

| When defect is found | Relative cost to fix |
|---|---|
| Requirements phase | 1x |
| Design phase | 5x |
| Implementation phase | 10x |
| Testing phase | 20x |
| Production (post-release) | 100x |

**How QA shifts left in practice:**
- Attend sprint planning and backlog refinement to understand stories before development
- Participate in Three Amigos meetings to define acceptance criteria
- Write test conditions while requirements are being written — before code
- Review API contract designs before implementation
- Define test data needs before the sprint starts
- Write automation framework code alongside development
- Run tests in CI so every commit gets immediate feedback

**Shift-left is not about QA doing developer work** — it is about QA's knowledge of edge cases, error scenarios, and quality concerns informing the development process from the start.

---

**Q27: What is agile testing? How does QA fit into a Scrum team?**

**A:**

Agile testing is testing within an agile development framework. Rather than testing being a separate phase after coding, testing is integrated throughout each sprint. QA is a full team member, not a downstream consumer of finished code.

**QA's role in a Scrum sprint:**

```
Sprint Planning (Day 1):
  QA attends; reviews user stories; identifies test conditions;
  estimates testing effort; raises ambiguities before work starts

Sprint Execution (Days 2–9):
  QA writes automation code in parallel with developers
  QA tests stories as soon as they are "developer done"
  QA logs defects immediately so developers can fix them in the same sprint
  QA participates in daily standup (what did I test, what am I testing, any blockers)

Sprint Review (Day 10):
  QA verifies all acceptance criteria are met before the demo
  QA helps demonstrate features to stakeholders

Sprint Retrospective:
  QA contributes to improving quality processes
  QA identifies testing bottlenecks and proposes solutions
```

**Definition of Done — QA's contribution:**

QA typically defines or strongly influences the team's Definition of Done:

```
A story is DONE when:
  - All acceptance criteria pass
  - Automated regression tests are written and passing
  - No critical or high severity defects open
  - Code reviewed and merged
  - Deployed to staging and smoke tested
  - Documentation updated if applicable
```

**The agile testing quadrants (Brian Marick):**

```
                       Business-Facing
                             ^
                             |
Q2 (Automated + Manual):     |     Q3 (Manual):
  Acceptance tests           |       Exploratory testing
  BDD/Gherkin scenarios      |       Usability testing
  Story tests                |       User acceptance testing
                             |
Supporting ←─────────────────┼─────────────────→ Critiquing
the Team                     |                   the Product
                             |
Q1 (Automated):              |     Q4 (Tools-Assisted):
  Unit tests                 |       Performance testing
  Integration tests          |       Security testing
  Component tests            |       Compatibility testing
                             |
                             v
                       Technology-Facing
```

- **Q1 and Q2:** Tests that support the team during development
- **Q3 and Q4:** Tests that critique the product from user and non-functional perspectives

---

**Q28: What is session-based exploratory testing?**

**A:**

Session-based exploratory testing (SBET) is a structured approach to exploratory testing that makes it measurable and manageable. Instead of ad hoc random testing, it uses time-boxed sessions with defined missions.

**Components of a session:**

**1. Charter (the mission):**
A clear statement of what to explore and the focus area.

```
Example charter: "Explore the user registration flow with focus on input validation —
particularly email format validation, password strength rules, and the behaviour
when a duplicate email is used."
```

**2. Time-box:**
A fixed time limit — typically 60 or 90 minutes. When the timer ends, the session is complete regardless of whether all areas were explored.

**3. Session notes:**
Written during the session using a structured format:

```
Session Charter: Explore coupon code validation in checkout
Tester: [Name]
Date: 2024-11-15
Duration: 90 minutes
Build: v2.1.4

AREAS TESTED:
  - Single coupon code application
  - Two simultaneous coupon codes
  - Expired coupon code
  - Coupon code for non-qualifying products
  - Case sensitivity of coupon codes

BUGS FOUND:
  - BUG-1045: Two coupons can be applied simultaneously; only first discount is calculated
  - BUG-1046: Coupon "SAVE10" is case-sensitive — "save10" does not work
  - BUG-1047: Expired coupon gives success message but no discount applied

ISSUES / QUESTIONS:
  - What is the intended behaviour for stacked coupons?
  - Should coupon codes be case-insensitive by design?

OBSERVATIONS:
  - Coupon validation is slow (~3 seconds API call) — possible performance issue
```

**4. Debrief:**
Report findings to the team. Convert important discoveries to formal test cases. Decide on follow-up sessions for areas not yet explored.

**Why session-based exploratory testing is valuable:**
- Makes exploratory testing accountable and reportable (number of sessions, bugs per session)
- Ensures focus — a charter prevents aimless clicking
- Builds a record of what was tested and when
- Produces defects that scripted testing would never find

---

**Q29: What are the types of performance testing? What is the difference between load, stress, spike, and soak testing?**

**A:**

Performance testing verifies how a system behaves under various load conditions. Different types answer different questions about the system's performance characteristics.

**Load Testing:**
Simulates expected real-world user load to verify the system meets performance SLAs (Service Level Agreements).

```
Goal: Does the system perform adequately under NORMAL production load?
Question: Can we handle 500 concurrent users with < 2s response time?

Profile:
  Ramp up: 0 → 500 users over 5 minutes
  Steady state: 500 users for 20 minutes
  Ramp down: 500 → 0 over 2 minutes

Pass/Fail: Response time < 2s for 95th percentile; error rate < 0.1%
```

**Stress Testing:**
Pushes the system beyond its design limits to find the breaking point.

```
Goal: Where does the system fail, and how does it fail?
Question: At what user count does response time become unacceptable or errors occur?

Profile:
  Ramp up: 0 → 2000 users progressively (beyond design limit of 500)
  Observe: When does the system start to degrade?
  When does the error rate spike?
  How does it recover when load is reduced?

Key findings: Maximum safe load, failure mode (graceful degradation or crash?)
```

**Spike Testing:**
Simulates sudden, dramatic increases in users.

```
Goal: How does the system handle a sudden traffic surge?
Question: If load jumps from 100 to 2000 users in 30 seconds (e.g., viral post, flash sale), does the system survive?

Profile:
  Normal load: 100 users
  Sudden spike: 100 → 2000 users in 30 seconds
  Return to normal: 2000 → 100 users
  Observe: How long to recover? Were requests dropped?
```

**Endurance / Soak Testing:**
Runs the system under sustained moderate load for an extended period (hours, days).

```
Goal: Do any resources leak over time?
Question: After 8 hours at 300 concurrent users, is memory still stable? Are connections pooling correctly?

Profile:
  Load: 300 users (60% of max) for 8+ hours
  Monitor: Memory usage (should be flat), CPU, DB connections, response time trend

Common findings: Memory leaks (memory grows continuously), connection pool exhaustion,
                 log file growth filling disk, session management bugs
```

**Other performance test types:**

| Type | Purpose |
|---|---|
| Volume Testing | System behaviour with very large amounts of data (millions of DB records) |
| Scalability Testing | How well performance scales when adding more servers (horizontal scaling) |
| Capacity Testing | Find the maximum users the system can handle before hardware must be upgraded |

**Key performance metrics to report:**

| Metric | Definition | Target Example |
|---|---|---|
| Response Time | Time from request sent to response received | < 2 seconds (95th percentile) |
| Throughput | Requests processed per second | 1,000 RPS |
| Error Rate | Percentage of failed requests | < 0.1% |
| Concurrent Users | Simultaneous active users | 500 |
| CPU Usage | Server CPU during load | < 80% |
| Memory | RAM during load | Stable (not growing) |

---

**Q30: What is regression testing strategy — full regression vs selective regression?**

**A:**

A regression testing strategy defines how much of the existing test suite to run after code changes. No single strategy is always best — the choice depends on the change scope, time available, and risk tolerance.

**Full Regression:**

Run every test in the entire test suite after any change.

```
When to use:
  - Before major releases
  - After large architectural refactoring
  - Before deploying to production
  - When the change scope is unknown

Pros: Maximum coverage; highest confidence
Cons: Slow (may take hours); expensive; impractical for daily CI runs

Tool support: Run the full suite nightly in CI; automated in Jenkins/GitHub Actions
```

**Selective Regression:**

Run only tests related to the changed area plus its dependencies.

```
Process:
  1. Identify which modules changed in this build
  2. Find all test cases that cover those modules (use requirements traceability matrix)
  3. Also run tests for any module that depends on the changed module
  4. Run those tests only

Example: Payment module changed → run:
  - All payment tests
  - Checkout tests (depends on payment)
  - Order confirmation tests (depends on checkout)
  - NOT: Search tests, profile tests (unrelated)

Pros: Much faster than full regression; proportional to change scope
Cons: Requires good test-to-code traceability; risk of missing cross-module impacts
```

**Risk-Based Regression:**

Always run high-risk tests; run medium-risk when time allows; skip low-risk tests until release.

```
Priority 1 (always run): Payment, login, checkout, data-saving operations
Priority 2 (run when time allows): Search, filtering, reporting, notifications
Priority 3 (run at release only): Admin tools, footer, static pages

This is applied WITHIN the CI pipeline:
  On every PR: Run Priority 1 tests only (~10 minutes)
  On every merge to main: Run Priority 1 + 2 (~30 minutes)
  Nightly: Run full regression Priority 1 + 2 + 3 (~60 minutes)
```

---

## SECTION 9: INTERVIEW Q&A

---

**Q31: What is the difference between smoke testing and sanity testing? (Interview question)**

**A:**

Smoke testing is a broad, shallow check of the entire build to verify it is stable enough to test. It covers many areas briefly. Done after every new build is received. If smoke fails, the build is rejected immediately — no further testing.

Sanity testing is a narrow, deep verification of a specific area that was changed or fixed. Done after receiving a targeted fix. It focuses only on the changed feature and its immediate neighbours.

Analogy: Smoke = checking that the car starts and the wheels are on. Sanity = checking that the repaired brake works correctly.

---

**Q32: Explain equivalence partitioning and boundary value analysis with an example. (Interview question)**

**A:**

Equivalence partitioning divides the input space into groups where every value in the group behaves identically. Instead of testing every possible input, you test one value from each group.

Boundary value analysis extends EP by focusing on the edges of each partition, because defects cluster at boundaries (off-by-one errors, incorrect comparison operators).

Example: A field accepts age 18–65 for loan eligibility.

EP partitions: under 18 (invalid), 18–65 (valid), over 65 (invalid) → 3 tests.

BVA adds: test at 17 (just below min), 18 (at min), 19 (just above min), 64 (just below max), 65 (at max), 66 (just above max) → 6 boundary tests.

Together they give 6 tests that catch the vast majority of input validation bugs, including off-by-one errors a developer might introduce by writing `> 18` instead of `>= 18`.

---

**Q33: What is the difference between severity and priority? Give an example where they differ. (Interview question)**

**A:**

Severity is the technical impact of a bug. Priority is the business urgency to fix it. They are independent and set by different people.

Classic examples:

High severity, low priority: A crash occurs when a legacy quarterly report runs for the finance team's one internal user. The system crashes (critical severity) but business urgency is low because almost nobody uses it and it only runs 4 times a year.

Low severity, high priority: The company name is spelled wrong in the homepage hero banner. There is zero functional impact (trivial severity) but it is embarrassing, visible to millions of visitors, and must be fixed within the hour (critical priority).

---

**Q34: Walk me through the defect lifecycle. When does a defect get reopened? (Interview question)**

**A:**

A defect goes: New → Assigned → Open → Fixed → Retest, then either Closed or Reopened.

Reopened means QA retested the fix and the defect is still present. QA adds new evidence — a screenshot showing the bug still exists, or an updated step-by-step reproduction — and moves the defect back to Open. The developer must investigate again.

The critical rule: only QA can Close or Reopen a defect — never the developer who fixed it. Developers have a conflict of interest. If a developer closes their own fix, there is no independent verification.

Deferred means the bug is real but will not be fixed in this release. It is NOT closed — it goes into the backlog for a future release.

---

**Q35: What is risk-based testing and how do you use it to prioritise your effort? (Interview question)**

**A:**

Risk-based testing uses Risk = Probability × Impact to prioritise which parts of the system to test most thoroughly.

I identify features with high probability of failure (new code, complex logic, recent changes) and high impact (revenue, security, core user journeys). These get deep testing with multiple techniques, automation, and exploratory sessions.

Features with low probability and low impact (stable, unchanged, rarely used) get light smoke testing. If time runs out, these are skipped — not the high-risk areas.

This means if a sprint is cut short, I have still thoroughly tested the things that matter most. Stakeholders understand the trade-off: we covered all critical areas; these lower-risk areas were not fully tested due to time constraints.

---

**Q36: What is the difference between verification and validation? (Interview question)**

**A:**

Verification asks "are we building the product right?" — checking documents, processes, and designs against standards. It does not involve running the software.

Validation asks "are we building the right product?" — checking that the working software actually meets user needs.

Example: Reviewing the requirements specification to check it is complete, unambiguous, and testable is verification. Running the built application and confirming it does what the specification says is validation.

In the SDLC, verification happens throughout (review every document as it is produced). Validation happens when there is working software to test.

---

**Q37: What is the test pyramid and why should a QA team follow it? (Interview question)**

**A:**

The test pyramid says: many unit tests, a moderate number of API/integration tests, and a few UI tests. This is the opposite of what teams naturally tend toward — they write UI tests because they are easy to understand, which creates the "ice cream cone" anti-pattern.

The pyramid matters because UI tests are slow (minutes each), brittle (break when the UI changes), and expensive to maintain. Unit tests run in milliseconds, are stable because they test pure logic, and are cheap.

A practical distribution: 70% unit tests, 20% API tests, 10% UI tests. For a QA engineer, this means resisting the urge to automate every scenario in the UI and instead asking "can this be covered at the API level?" A test that verifies a business calculation rule should be a unit test, not a Playwright test that opens a browser to trigger the calculation.

---

**Q38: What is shift-left testing and how did you apply it in practice? (Interview question)**

**A:**

Shift-left means involving QA earlier — during requirements and design, not after code is written. The reason is that defects found in requirements cost almost nothing to fix, while defects found in production cost 100x more.

In practice I shift left by attending sprint planning and backlog refinement to spot ambiguous or untestable requirements before the sprint starts. I participate in Three Amigos meetings where I bring edge cases the team has not thought of. I write test conditions alongside user stories, before a developer writes a line of code. I set up test data and environment requirements at the start of the sprint so there is no blocking when code is ready to test.

The result is that stories are clearer before development starts, developers know what will be tested so they code with that in mind, and there are far fewer late-sprint surprises.

---

**Q39: What is the difference between load testing and stress testing? (Interview question)**

**A:**

Load testing applies expected real-world load to verify the system meets performance targets under normal conditions. If the system is designed for 500 concurrent users, I run 500 concurrent users and measure response times, throughput, and error rates against the agreed SLA.

Stress testing pushes the system beyond its design limits to find the breaking point. I ramp users far beyond 500 — to 1000, 1500, 2000 — and observe at what point the system starts to degrade, when errors spike, and critically, how it fails: does it give a graceful "service unavailable" message or does it crash hard?

Stress testing also reveals how the system recovers. If I drop the load back to 500 after the system is stressed, does it recover to normal performance within 60 seconds, or does it require a restart?

---

**Q40: What is exploratory testing and when do you prefer it over scripted testing? (Interview question)**

**A:**

Exploratory testing means simultaneously designing and executing tests, guided by knowledge and intuition, usually in a time-boxed session with a defined charter. There are no pre-written test steps — the tester decides what to do next based on what they observe.

I prefer exploratory testing over scripted when: testing a new feature where requirements are still evolving; investigating an area where scripted tests pass but something feels wrong; assessing usability and user experience; or probing the area around a recently fixed bug.

Scripted testing is better when: running regression tests that must be repeatable and comparable to previous runs; testing compliance requirements where I must prove specific scenarios were executed; or onboarding a new tester who needs guided step-by-step procedures.

The key is combining both: scripted testing for repeatability and coverage measurement, exploratory testing for finding what was not anticipated.

---

*Guide complete — covers SDLC, STLC, test levels, test pyramid, test design techniques (EP, BVA, decision tables, state transition, use case), code coverage, defect lifecycle, severity vs priority, risk-based testing, test planning, test metrics, agile testing, performance testing types, and 10 interview questions with full answers.*
