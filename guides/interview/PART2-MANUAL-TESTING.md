# Part 2 — Manual Testing | 40 Questions | Full Answers + Examples

> All answers written in first person as Kupeshanth Kupenthiran.
> Role: Trainee Quality Engineer at Qoria Lanka | B.Sc. IT & Management, University of Moratuwa

---

## Q1: What is SDLC? Name all phases.

**A:**

SDLC stands for Software Development Life Cycle. It is the structured process that a software team follows to plan, design, build, test, deploy, and maintain a software product. Every phase has defined inputs, activities, and outputs, and together they ensure that software is built systematically and meets the requirements.

The phases of SDLC are:

```
1. Planning
   - Define scope, objectives, timeline, and resources
   - Feasibility study (technical, financial, operational)
   - Output: Project Plan

2. Requirements Analysis
   - Gather and document functional and non-functional requirements
   - Stakeholder interviews, workshops
   - Output: Software Requirements Specification (SRS)

3. System Design
   - Translate requirements into architecture and design
   - High-level design (HLD) and Low-level design (LLD)
   - Database schema, UI wireframes, API contracts
   - Output: Design Document

4. Implementation (Coding)
   - Developers write code based on the design
   - Unit testing by developers
   - Output: Working code in version control

5. Testing
   - QA team validates the software against requirements
   - Functional, integration, regression, performance testing
   - Output: Test Results, Defect Reports

6. Deployment
   - Release tested software to production or client environment
   - Could be staged (dev -> staging -> prod)
   - Output: Live software

7. Maintenance
   - Bug fixes after release, performance monitoring
   - Enhancement requests, patch releases
   - Output: Updated software, Release notes
```

In my work at Qoria, I am most actively involved in phases 2 through 5, but I also contribute to deployment verification through smoke testing post-release.

---

## Q2: What is STLC? Name all phases with QA activities at each.

**A:**

STLC stands for Software Testing Life Cycle. It is the sequence of activities that a QA team performs to plan, design, execute, and close the testing of a software product. STLC runs parallel to and integrated with the SDLC.

```
PHASE 1 — Requirement Analysis
  Entry: SRS, User Stories, AC
  QA Activities:
    - Review requirements for testability
    - Identify ambiguous or missing acceptance criteria
    - Define what can be tested and what cannot
    - Raise clarification questions (RTM starts here)
  Exit: Signed-off requirements, identified test conditions

PHASE 2 — Test Planning
  Entry: Approved requirements, project schedule
  QA Activities:
    - Write the Test Plan document
    - Estimate testing effort
    - Assign roles and responsibilities
    - Define test environment needs and tools
    - Define entry/exit criteria
  Exit: Approved Test Plan

PHASE 3 — Test Case Design
  Entry: Test Plan, detailed requirements
  QA Activities:
    - Write detailed test cases (positive, negative, boundary)
    - Apply test design techniques (BVA, EP, Decision Tables)
    - Create test data
    - Map test cases to requirements (RTM)
    - Peer review of test cases
  Exit: Reviewed and approved test cases

PHASE 4 — Test Environment Setup
  Entry: System design documents, test plan
  QA Activities:
    - Configure test environments (dev, staging, UAT)
    - Set up test tools (JIRA, Selenium, Playwright)
    - Verify environment readiness
    - Prepare test data
  Exit: Test environment ready and smoke-tested

PHASE 5 — Test Execution
  Entry: Test cases approved, environment ready, build available
  QA Activities:
    - Execute test cases (smoke first, then full suite)
    - Log defects in JIRA
    - Re-test fixed defects
    - Run regression after fixes
    - Update test results
  Exit: All tests executed, pass/fail documented

PHASE 6 — Test Closure
  Entry: All planned tests executed
  QA Activities:
    - Produce Test Summary Report
    - Analyse defect metrics (open, closed, severity distribution)
    - Verify exit criteria are met
    - Archive test artefacts
    - Retrospective: lessons learned
  Exit: Test Summary Report signed off
```

At Qoria, I go through all of these phases in every sprint, though some phases are compressed in Agile compared to a waterfall project.

---

## Q3: What is the difference between SDLC and STLC?

**A:**

The key distinction is scope: SDLC covers the entire software development process, while STLC covers only the testing portion within that process.

```
+------------------+-----------------------------+-----------------------------+
| Dimension        | SDLC                        | STLC                        |
+------------------+-----------------------------+-----------------------------+
| Scope            | Entire software lifecycle   | Testing activities only     |
| Owner            | Project team (Dev, QA, PM)  | QA / Test team              |
| Starts when      | Project is initiated        | Requirements are available  |
| Ends when        | Product is in maintenance   | Test closure is complete    |
| Phases           | 7 phases (Plan to Maintain) | 6 phases (Analyse to Close) |
| Key outputs      | Working software product    | Test reports, defect logs   |
| Relationship     | Parent process              | Child process within SDLC   |
+------------------+-----------------------------+-----------------------------+
```

Think of it this way: SDLC is the house, and STLC is the quality inspection process that runs throughout the construction of that house. STLC does not replace SDLC — it operates inside it.

Another way I explain it: SDLC answers "How do we build the software?" and STLC answers "How do we verify that what was built is correct?"

---

## Q4: How do you write a good test case? Show all fields.

**A:**

A well-written test case is the backbone of structured manual testing. It needs to be clear enough that any tester, even someone unfamiliar with the feature, can execute it and get consistent results.

Here are all the fields a good test case should contain:

```
+------------------+--------------------------------------------------+
| Field            | Description                                      |
+------------------+--------------------------------------------------+
| Test Case ID     | Unique identifier (e.g., TC_LOGIN_001)           |
| Test Case Title  | Short, descriptive name of what is being tested  |
| Module / Feature | Which part of the application                    |
| Priority         | High / Medium / Low                              |
| Test Type        | Functional / Regression / Smoke / Negative       |
| Preconditions    | State the system must be in before execution     |
| Test Steps       | Numbered, specific actions the tester takes      |
| Test Data        | Input values to use (usernames, values, files)   |
| Expected Result  | What SHOULD happen if the system works correctly |
| Actual Result    | What ACTUALLY happened (filled during execution) |
| Status           | Pass / Fail / Blocked / Not Executed             |
| Defect ID        | Link to JIRA defect if status is Fail            |
| Tested By        | Name of the tester                               |
| Date Executed    | Date of execution                                |
| Environment      | Browser, OS, version details                     |
+------------------+--------------------------------------------------+
```

Good test cases are also written with the mindset that the Expected Result must be completely specific — not "the system should work correctly" but "the system should display a success message: 'Login successful' and redirect to /dashboard." Vague expected results make Pass/Fail decisions subjective, which defeats the purpose.

---

## Q5: Write a test case for the login feature. (Complete example)

**A:**

Here is a complete set of test cases for a standard login feature. I will write three to show positive, negative, and boundary scenarios:

```
=================================================================
TEST CASE 1 — Valid login
=================================================================
Test Case ID    : TC_LOGIN_001
Title           : Verify successful login with valid credentials
Module          : Authentication
Priority        : High
Test Type       : Functional / Smoke
Preconditions   : User is registered. Browser is open. App is on
                  login page (https://app.example.com/login)
Test Data       : Email:    testuser@example.com
                  Password: ValidPass@123
Test Steps      :
  1. Navigate to the login page.
  2. Enter the email in the Email field.
  3. Enter the password in the Password field.
  4. Click the "Login" button.
Expected Result : User is redirected to the dashboard (/dashboard).
                  A welcome message "Welcome, Test User" is displayed.
                  The session token is set in browser cookies.
Actual Result   : [filled during execution]
Status          : [Pass / Fail]
Defect ID       : N/A

=================================================================
TEST CASE 2 — Invalid password
=================================================================
Test Case ID    : TC_LOGIN_002
Title           : Verify error message on incorrect password
Module          : Authentication
Priority        : High
Test Type       : Negative
Preconditions   : Same as TC_LOGIN_001
Test Data       : Email:    testuser@example.com
                  Password: WrongPassword!
Test Steps      :
  1. Navigate to the login page.
  2. Enter valid email.
  3. Enter an incorrect password.
  4. Click "Login".
Expected Result : User remains on the login page.
                  Error message displayed: "Invalid email or password."
                  No session token is created.
Actual Result   : [filled during execution]
Status          : [Pass / Fail]
Defect ID       : N/A

=================================================================
TEST CASE 3 — Empty fields submission
=================================================================
Test Case ID    : TC_LOGIN_003
Title           : Verify validation when both fields are empty
Module          : Authentication
Priority        : Medium
Test Type       : Negative / Boundary
Preconditions   : Browser open on login page
Test Data       : Email: (empty)  Password: (empty)
Test Steps      :
  1. Navigate to the login page.
  2. Leave both Email and Password fields empty.
  3. Click "Login".
Expected Result : Login is not attempted.
                  Inline validation messages appear:
                  - "Email is required" under Email field.
                  - "Password is required" under Password field.
Actual Result   : [filled during execution]
Status          : [Pass / Fail]
Defect ID       : N/A
```

At Cerexio and Qoria, I follow this structure consistently so that anyone on the team can execute my test cases without needing to ask clarifying questions.

---

## Q6: What is the defect lifecycle? Walk through all statuses.

**A:**

The defect lifecycle (also called bug lifecycle) describes the states a defect moves through from the moment it is discovered until it is permanently closed. Understanding this cycle is essential for any QA professional because it governs how we track and communicate quality issues.

```
                       [New / Open]
                           |
                 Assigned to developer
                           |
                       [Assigned]
                           |
              Developer investigates & works on it
                           |
               +-----[In Progress]-----+
               |                       |
          Can reproduce            Cannot reproduce
               |                       |
           [Fixed]              [Need More Info]
               |                       |
     QA re-tests the fix          QA provides more
               |                  details & re-opens
        +------+------+
        |             |
    [Verified]    [Reopened]
    (test passes) (test fails again)
        |
   Release decision
        |
    [Closed]

ADDITIONAL STATUSES:
  [Deferred]  - Valid bug but fixed in a future release
  [Rejected]  - Developer disagrees it is a bug
  [Duplicate] - Same bug already logged elsewhere
  [Not a Bug] - Working as designed
```

Status Descriptions:
- **New/Open** — Logged by QA, not yet reviewed by dev team
- **Assigned** — Assigned to a developer for investigation
- **In Progress** — Developer is actively working on a fix
- **Fixed** — Developer has applied a fix and marked it ready for re-test
- **Reopened** — QA tested the fix and it is still failing
- **Verified** — QA confirmed the fix works correctly
- **Closed** — Defect is fully resolved and confirmed in the release
- **Deferred** — Acknowledged but pushed to a future sprint
- **Rejected** — Developer disputes that it is a defect
- **Duplicate** — Same defect reported in another ticket

At Qoria, I track all my defects in JIRA and I am responsible for moving tickets through Verified and Closed states after re-testing.

---

## Q7: What is the difference between severity and priority? Give examples of all 4 combinations.

**A:**

This is one of the most commonly asked questions in QA interviews and one that many people get subtly wrong. Let me define both clearly and then give all four combinations.

**Severity** = The technical impact of the defect on the application. How badly does it break the system? This is a QA judgment.

**Priority** = The urgency of fixing the defect. How soon does it need to be fixed? This is a business/product decision.

```
+------------------+------------------------------------------+
| Severity         | Impact on system functionality           |
| Levels           | Critical > High > Medium > Low           |
+------------------+------------------------------------------+
| Priority         | How urgently the fix is needed           |
| Levels           | Urgent > High > Medium > Low             |
+------------------+------------------------------------------+
```

**All 4 Combinations with Real Examples:**

```
COMBINATION 1 — High Severity + High Priority
  Example: The "Pay Now" button on the checkout page crashes the app
           for all users. No purchase can be made.
  Severity: High — core functionality completely broken
  Priority: High — this must be fixed immediately; revenue is lost
  Action: Drop everything, fix today

COMBINATION 2 — High Severity + Low Priority
  Example: The application crashes when you upload a file with
           a .psd extension. Almost no users do this.
  Severity: High — crash is technically severe
  Priority: Low — very few users hit this path, not urgent
  Action: Log it, schedule for a future sprint

COMBINATION 3 — Low Severity + High Priority
  Example: The company logo on the homepage is broken (shows
           alt text instead of the image) on the day of a major
           product launch or investor demo.
  Severity: Low — the app still works fine functionally
  Priority: High — bad optics, must be fixed before the event
  Action: Quick fix before launch, even though it is cosmetic

COMBINATION 4 — Low Severity + Low Priority
  Example: A tooltip on a settings page has a minor spelling
           error in a word that most users never read.
  Severity: Low — cosmetic, no functional impact
  Priority: Low — no urgency, can be fixed whenever
  Action: Log it, address during a maintenance sprint
```

Understanding this distinction helps me have more intelligent conversations with product owners and developers about how to triage a defect backlog.

---

## Q8: What is smoke testing? Give a real example.

**A:**

Smoke testing is a shallow, quick pass through the most critical and basic functions of an application to determine whether the build is stable enough for deeper testing to begin. The name comes from hardware testing — you power on a circuit board and check if it smokes before running any further tests.

The goal is not to find all bugs. The goal is to answer one question: "Is this build fundamentally broken or not?" If smoke tests fail, we reject the build and send it back to development without wasting time on detailed testing.

**Characteristics of smoke testing:**
- Fast — typically 30 minutes to 2 hours for a large application
- Broad coverage, shallow depth
- Tests the most critical user journeys only
- Performed on every new build before QA proceeds

**Real example from my work at Qoria:**

When a new build of the EdTech Insight platform is deployed to the staging environment, our smoke test suite covers:

```
SMOKE TEST CHECKLIST — EdTech Insight Platform
================================================
1. Application loads without errors (HTTP 200, no JS console errors)
2. Login with a valid school admin account works
3. Dashboard loads and displays data widgets
4. Navigation menu items are accessible
5. Student list page loads and shows records
6. Report generation page loads without 500 errors
7. Logout works and session is cleared
8. API health endpoint returns 200 OK
================================================
Total: ~8 tests, target completion time: 20 minutes
```

If any of these 8 tests fail, I mark the build as rejected in JIRA and the development team investigates before we proceed with the sprint's full testing scope.

---

## Q9: What is sanity testing? How is it different from smoke testing?

**A:**

Sanity testing is a narrow, focused test pass performed after a bug fix or a small change, to verify that the specific fix works and has not broken the immediately adjacent functionality. It is more focused than smoke testing — you are not testing the whole application, just the area that was changed.

The goal of sanity testing is to answer: "Did this specific fix solve the reported problem, and did the fix introduce any obvious side effects in the related area?"

**Key differences:**

```
+-------------------+------------------------+------------------------+
| Dimension         | Smoke Testing          | Sanity Testing         |
+-------------------+------------------------+------------------------+
| When performed    | After each new build   | After a bug fix or     |
|                   |                        | small change           |
| Scope             | Entire application     | Specific feature or    |
|                   | (broad)                | module (narrow)        |
| Depth             | Shallow                | Moderate               |
| Goal              | Is the build stable?   | Did this fix work?     |
| Documentation     | Often scripted         | Often unscripted       |
| Who performs it   | QA team                | QA team                |
+-------------------+------------------------+------------------------+
```

**Real example from my work:**

At Qoria, a developer fixed a bug where student names with apostrophes (like "O'Brien") were being stored incorrectly in the database. After the fix was deployed to staging, I ran a sanity test:

1. Created a student with the name "O'Brien" — verified it saved correctly
2. Searched for the student by name — verified they appeared in search results
3. Exported a report including this student — verified the name appeared correctly
4. Checked that other student records were unaffected

I did not re-run the entire regression suite. I focused only on the area touched by the fix. That is the essence of sanity testing — targeted, efficient verification.

---

## Q10: What is regression testing? When do you do it?

**A:**

Regression testing is the process of re-running previously passing tests after a change has been made to the codebase, to ensure that the change has not inadvertently broken functionality that was working before. The word "regress" means "to go back" — we are checking that the software has not "gone back" to a broken state.

The core idea is: every time you change code, you risk breaking something else. Regression testing is the safety net that catches those unintended breakages.

**When do I run regression testing?**

```
TRIGGERS FOR REGRESSION TESTING:
1. After every bug fix — does the fix break anything else?
2. After every new feature is added — does new code affect old code?
3. After any refactoring — behaviour should not change
4. Before a release — final confidence check across the entire system
5. After configuration or environment changes
6. After third-party library upgrades
```

**Types of regression:**
- **Full regression** — run every single test case; done before major releases
- **Partial regression** — run tests related to the changed area; done in sprints
- **Automated regression** — most practical approach; I run our Selenium TestNG suite automatically in GitHub Actions on every PR at Qoria

**Real practice at Qoria:**

Our automated regression suite in Playwright and Selenium runs on every pull request via GitHub Actions. This means by the time a developer merges code, we have already run ~300 automated regression checks. For the sprint-end release, I also run a targeted manual regression pass on the highest-risk areas identified during sprint planning.

Regression testing is where automation pays off the most. Running 300 test cases manually before every release would be impossible; running them in a CI pipeline takes about 15 minutes.

---

## Q11: What is the difference between smoke, sanity, and regression? (Full table answer)

**A:**

This is one of the most frequently asked questions in QA interviews. Let me break it down with a full comparison and then a memorable summary.

```
+------------------+-----------------+-------------------+-------------------+
| Dimension        | Smoke Testing   | Sanity Testing    | Regression Testing|
+------------------+-----------------+-------------------+-------------------+
| Also called      | Build verif.    | Subset regression | Full/partial      |
|                  | testing         |                   | regression        |
+------------------+-----------------+-------------------+-------------------+
| When             | New build       | After small fix   | After any change  |
|                  | deployed        | or minor change   | before release    |
+------------------+-----------------+-------------------+-------------------+
| Scope            | Entire app,     | Specific module   | Broad — entire    |
|                  | shallow         | or fix area only  | application       |
+------------------+-----------------+-------------------+-------------------+
| Depth            | Very shallow    | Moderate          | Deep              |
+------------------+-----------------+-------------------+-------------------+
| Goal             | Is the build    | Did this fix      | Did changes break |
|                  | stable enough   | work correctly?   | anything that was |
|                  | to test?        |                   | already working?  |
+------------------+-----------------+-------------------+-------------------+
| Time taken       | Short (mins-    | Short to medium   | Long (hours or    |
|                  | 1-2 hours)      | (minutes-hours)   | days for full)    |
+------------------+-----------------+-------------------+-------------------+
| Scripted?        | Yes, fixed set  | Often exploratory | Yes, maintained   |
|                  | of cases        |                   | test case library |
+------------------+-----------------+-------------------+-------------------+
| Automation?      | Often automated | Rarely automated  | Highly automated  |
+------------------+-----------------+-------------------+-------------------+
| Fail outcome     | Build rejected, | Fix rejected,     | Release blocked,  |
|                  | not tested      | re-develop        | bugs fixed        |
+------------------+-----------------+-------------------+-------------------+
```

**Memory Aid:**

> "Smoke checks if the car starts. Sanity checks if the repaired brake still works. Regression checks if fixing the brake broke the steering."

In practice at Qoria, all three types are part of our sprint workflow. I run smoke tests when a build arrives, sanity tests after developer bug fixes, and automated regression on every PR and before every release.

---

## Q12: What is functional testing?

**A:**

Functional testing is a type of software testing that verifies the application behaves according to its specified functional requirements. It tests WHAT the system does — the features, user interactions, and business logic — without caring about HOW the system does it internally (that is structural/white-box testing).

Every test I write that checks "when I click this button, does this expected thing happen" is functional testing.

**What functional testing covers:**

```
- User interface interactions (buttons, forms, navigation)
- Business rule validation (e.g., discount logic, access control)
- Data input and output (does entering X produce result Y?)
- API responses (does calling /api/report return the right data?)
- Integration between components (does the login module pass the
  correct token to the dashboard module?)
- Error handling (does the system handle invalid input gracefully?)
```

**Types of testing that fall under functional testing:**

- Smoke testing
- Sanity testing
- Regression testing
- Integration testing
- System testing
- User Acceptance Testing (UAT)

**Non-functional testing is the counterpart** — it covers performance, security, usability, and reliability: HOW WELL the system works, not WHAT it does.

At Cerexio and Qoria, the bulk of my test case writing and execution is functional testing — verifying that each user story does what the acceptance criteria say it should do.

---

## Q13: What is exploratory testing? How do you structure it?

**A:**

Exploratory testing is a simultaneous process of learning, designing, and executing tests in real time, guided by the tester's knowledge, intuition, and observation of the application's behaviour, rather than following a pre-written script. The key characteristic is that test design and test execution happen at the same time.

It is not random or unstructured "clicking around." Good exploratory testing is disciplined and guided by a charter or mission.

**How I structure exploratory testing (Session-Based Approach):**

```
STEP 1 — Define a Charter (Mission Statement)
  A charter is a short, focused goal for the session.
  Example: "Explore the student report export feature to find
            issues related to data completeness and edge cases
            in date range selection."
  Time-box: 60-90 minutes per session

STEP 2 — Note Taking During Session
  - Log what I tested (areas explored)
  - Log issues found (potential bugs, observations)
  - Log questions raised (unclear behaviour to investigate further)
  Tools: Notepad, JIRA draft tickets, screen recorder

STEP 3 — Debriefing After Session
  - Review notes
  - Convert observations to formal JIRA defects
  - Identify follow-up charters if needed

STEP 4 — Metrics
  - Time on mission: % of session actually testing
  - Bugs found
  - Areas covered
```

**When I use exploratory testing:**

1. When requirements are incomplete or changing rapidly
2. When a new build arrives and I want to quickly understand what changed
3. After regression to find defects that scripted tests would miss
4. When testing complex user flows that are hard to fully script in advance

At Qoria, I use exploratory testing most effectively in the first day after a new feature is deployed, when I am learning the feature's behaviour and forming hypotheses about where it might break.

---

## Q14: What is boundary value analysis? Give a real example with exact values.

**A:**

Boundary Value Analysis (BVA) is a test design technique based on the principle that defects are most likely to occur at the edges (boundaries) of input ranges, not in the middle. So instead of testing random values, we specifically test the values at and just beyond the boundaries.

**Rule of thumb:** For every boundary, test:
- Just below the boundary (invalid side)
- At the boundary (valid)
- Just above the boundary (valid or invalid, depending on which end)

**Real example — Password Length Validation:**

Suppose the requirement says: "Password must be between 8 and 20 characters."

```
  BOUNDARY MAP:
  
  |--- INVALID ---|--- VALID ----------------------|--- INVALID ---|
  1  2  3  4  5  6  7 [8] 9  ...  18 19 [20] 21  22  23  ...
  
  Lower boundary = 8
  Upper boundary = 20
  
  BVA Test Values:
  +-------+--------+---------+----------------------------------+
  | Value | Chars  | Expected| Reason                           |
  +-------+--------+---------+----------------------------------+
  |   7   | 7 chars| INVALID | Just below lower boundary        |
  |   8   | 8 chars| VALID   | AT lower boundary                |
  |   9   | 9 chars| VALID   | Just above lower boundary        |
  |  19   |19 chars| VALID   | Just below upper boundary        |
  |  20   |20 chars| VALID   | AT upper boundary                |
  |  21   |21 chars| INVALID | Just above upper boundary        |
  +-------+--------+---------+----------------------------------+
```

I only need these 6 values to achieve good boundary coverage. Testing 13 or 17 characters is unlikely to find anything different from testing 9.

At Cerexio, when I was testing Angular form validations, I consistently applied BVA to all numeric and text-length fields to find validation bugs that only occurred at the exact boundary value. This technique caught at least two defects where the developer had used "greater than" instead of "greater than or equal to" in their validation logic.

---

## Q15: What is equivalence partitioning? Give a real example with partitions.

**A:**

Equivalence Partitioning (EP) is a test design technique where you divide input data into groups (partitions) within which all values are expected to behave identically. The principle is: if one value from a partition works, all values in that partition work. So you only need to test one representative from each partition.

This reduces the number of test cases without losing coverage of distinct behaviours.

**Real example — Age field for a streaming platform:**

Requirement: "Users must be aged 18 or over to register. Age must be a positive integer. Maximum allowed age is 120."

```
EQUIVALENCE PARTITIONS:
+------------------+------------------+-------------+--------------+
| Partition        | Range            | Type        | Test Value   |
+------------------+------------------+-------------+--------------+
| EP1 — Underage   | 1 to 17          | Invalid     | 15           |
| EP2 — Valid age  | 18 to 120        | Valid       | 35           |
| EP3 — Over max   | 121 and above    | Invalid     | 150          |
| EP4 — Zero       | 0                | Invalid     | 0            |
| EP5 — Negative   | -1 and below     | Invalid     | -5           |
| EP6 — Non-integer| Decimal/text     | Invalid     | "abc", 25.5  |
+------------------+------------------+-------------+--------------+
```

I need just 6 test cases to cover all distinct behaviours. Testing 25 AND 35 AND 47 would be redundant — they all fall in EP2 and should behave identically.

**EP and BVA are complementary:** EP tells me which ranges to test, BVA tells me which specific values within those ranges to prioritise. I use them together — EP to partition, BVA to pick the boundary values within and around each partition.

---

## Q16: What is decision table testing? Show a real example table.

**A:**

Decision table testing is a technique used when the output of a system depends on a combination of multiple input conditions. It helps ensure that all combinations of business rules are tested, which is especially useful for complex conditional logic.

Each column in the table represents a test case, and each row represents a condition or action. The table structure makes it impossible to miss a combination.

**Real example — Discount eligibility system:**

Business rules:
- Premium members get a discount
- Orders over $100 get a discount
- If BOTH conditions are true, the discount is 20%; if only one, 10%; if neither, 0%

```
DECISION TABLE:

+---------------------------+------+------+------+------+
| Conditions                | TC1  | TC2  | TC3  | TC4  |
+---------------------------+------+------+------+------+
| Is Premium Member?        |  Y   |  Y   |  N   |  N   |
| Order > $100?             |  Y   |  N   |  Y   |  N   |
+---------------------------+------+------+------+------+
| Actions / Expected Result |      |      |      |      |
+---------------------------+------+------+------+------+
| Apply 20% discount        |  X   |      |      |      |
| Apply 10% discount        |      |  X   |  X   |      |
| Apply 0% discount         |      |      |      |  X   |
+---------------------------+------+------+------+------+
```

Test case count = 2^n where n = number of binary conditions = 2^2 = 4 cases.

This technique is especially useful for financial systems, rule engines, and any application with branching business logic. At Qoria, when testing access control rules (e.g., "admin AND active AND verified account can access report exports"), I mentally build a decision table to ensure I have covered all meaningful combinations.

---

## Q17: What is state transition testing?

**A:**

State transition testing is a technique where we test the system's behaviour as it moves between different states, triggered by specific events or conditions. It is most useful for systems that maintain state — like order management, user account systems, booking workflows, or any workflow with defined statuses.

**Core concepts:**
- **State** — A condition or status the system is in (e.g., "Logged In", "Cart Empty", "Order Placed")
- **Event/Trigger** — An action that causes the system to change state (e.g., "Click Login", "Add to Cart")
- **Transition** — The move from one state to another
- **Action** — What the system does during the transition

**Example — User Account States:**

```
STATE TRANSITION DIAGRAM:

  [Not Registered]
        |
     [Registers]
        |
        v
  [Pending Verification]
        |
   [Clicks email link]
        |
        v
    [Active Account]
     |          |
 [Deactivates] [Admin suspends]
     |          |
     v          v
 [Inactive]  [Suspended]
     |          |
 [Re-activates] [Admin reinstates]
     |          |
     +----------+
     v
 [Active Account]
```

**State Transition Table:**

```
+--------------------+------------------+------------------------+
| Current State      | Event            | Next State             |
+--------------------+------------------+------------------------+
| Not Registered     | Register         | Pending Verification   |
| Pending            | Verify email     | Active                 |
| Pending            | Timeout (24h)    | Registration Expired   |
| Active             | Deactivate self  | Inactive               |
| Active             | Admin suspends   | Suspended              |
| Inactive           | Re-activate      | Active                 |
| Suspended          | Admin reinstates | Active                 |
+--------------------+------------------+------------------------+
```

I use this technique when testing user lifecycle management or any workflow that has a defined status field. The key insight is to test not just the valid transitions but also the invalid ones — what happens if you try to log in from a Suspended state? The system should reject it, not allow it.

---

## Q18: What is a test plan? What sections does it contain?

**A:**

A test plan is a formal document that describes the scope, approach, objectives, resources, and schedule for testing a software project. It is the "contract" between the QA team and the stakeholders about what will and will not be tested, how it will be tested, and what constitutes done.

A test plan is written during the Test Planning phase of STLC and is typically owned by the QA Lead or Senior QA Engineer.

**Standard sections of a test plan:**

```
1. INTRODUCTION
   - Purpose of the document
   - Project overview and context

2. SCOPE
   - In-scope: what will be tested
   - Out-of-scope: what will NOT be tested (and why)

3. TEST OBJECTIVES
   - What we are trying to verify or achieve

4. TESTING TYPES
   - Functional, regression, performance, security, etc.

5. ENTRY AND EXIT CRITERIA
   - Entry: conditions that must be true before testing starts
   - Exit: conditions that must be true before testing ends

6. TEST APPROACH / STRATEGY
   - Manual vs automation split
   - Tools and frameworks to be used
   - Test levels (unit, integration, system, UAT)

7. TEST ENVIRONMENT
   - Servers, browsers, devices, OS versions
   - Test data sources

8. RESOURCE PLAN
   - Who is responsible for what
   - Timeline and milestones

9. RISK AND MITIGATION
   - Potential risks (environment unavailability, late code delivery)
   - How we mitigate each

10. DEFECT MANAGEMENT
    - Tool used (JIRA)
    - Severity and priority definitions
    - Defect triage process

11. DELIVERABLES
    - Test cases, test results, defect reports, test summary report

12. APPROVALS
    - Sign-off from QA Lead, PM, and relevant stakeholders
```

In Agile projects like at Qoria, we rarely write a full formal test plan per sprint, but we maintain a lightweight test strategy document that covers points 2-6, updated at the start of each release cycle.

---

## Q19: What is a Requirements Traceability Matrix (RTM)?

**A:**

A Requirements Traceability Matrix (RTM) is a document that maps and traces requirements to the test cases that verify them. It creates a bidirectional link between requirements and tests so that we can answer two important questions at any point:

1. "Is every requirement covered by at least one test?" (forward traceability)
2. "Why does this test case exist? Which requirement does it support?" (backward traceability)

**Why RTM matters:**

Without an RTM, it is easy to accidentally miss testing a requirement, or to have redundant test cases with no clear purpose. The RTM makes test coverage visible and auditable.

**RTM Structure:**

```
+--------+--------------------------+-------------------+----------+--------+
| Req ID | Requirement Description  | Test Case ID(s)   | Status   | Result |
+--------+--------------------------+-------------------+----------+--------+
| REQ-01 | User must be able to log | TC_LOGIN_001      | Executed | Pass   |
|        | in with valid credentials| TC_LOGIN_002      | Executed | Pass   |
+--------+--------------------------+-------------------+----------+--------+
| REQ-02 | Password must be 8-20    | TC_PWD_001        | Executed | Pass   |
|        | characters               | TC_PWD_002        | Executed | Fail   |
+--------+--------------------------+-------------------+----------+--------+
| REQ-03 | System must send         | TC_EMAIL_001      | Pending  | N/A    |
|        | verification email       |                   |          |        |
+--------+--------------------------+-------------------+----------+--------+
```

**Benefits:**
- Ensures no requirement is untested (coverage gap detection)
- Allows impact analysis when requirements change
- Provides audit trail for compliance projects
- Helps prioritise test execution by requirement priority

At Cerexio, I maintained an RTM in an Excel spreadsheet for each module I tested. At Qoria, we link test cases to user stories in JIRA, which gives us electronic traceability built into our workflow tool.

---

## Q20: What is UAT? What is QA's role in it?

**A:**

UAT stands for User Acceptance Testing. It is the final phase of testing where actual business users or stakeholders validate the software against their real-world use cases and business needs, to decide whether the product is ready to be accepted and released.

UAT is fundamentally different from QA testing. QA verifies the system meets technical requirements. UAT verifies the system meets business needs and user expectations — a critical distinction.

**Who performs UAT:**
- Business stakeholders, product owners, actual end users
- NOT typically the QA team — though QA supports the process

**QA's role in UAT:**

```
BEFORE UAT:
  - Ensure all QA testing is complete and sign-off given
  - Prepare UAT test environments and stable builds
  - Write UAT test scripts or user scenarios for business users
  - Brief UAT participants on the testing process
  - Set up defect logging access in JIRA for non-technical users
  - Prepare test data that reflects real business scenarios

DURING UAT:
  - Support business users who encounter questions or issues
  - Triage issues raised: genuine defects vs training issues vs
    out-of-scope requests
  - Log defects on behalf of users who cannot use JIRA directly
  - Maintain UAT execution status tracking

AFTER UAT:
  - Compile UAT results into a summary report
  - Ensure all critical UAT defects are fixed and re-tested
  - Facilitate the Go/No-Go decision meeting
  - Support sign-off documentation
```

UAT outcomes:
- **Accepted** — Software is approved for release as-is
- **Accepted with conditions** — Minor issues noted but release allowed
- **Rejected** — Major issues found, fixes required before release

At Qoria, our product owner and occasionally school-side stakeholders participate in UAT before major platform releases. My role is to prepare the test environment and user scenarios they follow.

---

## Q21: What is risk-based testing?

**A:**

Risk-based testing is an approach where test effort and prioritisation are guided by the risk associated with different parts of the application, rather than testing everything equally. It recognises a fundamental constraint: we never have unlimited time to test, so we must invest testing effort where the risk of failure is highest.

**Risk = Probability of failure × Impact if it fails**

**How to identify risk:**

```
RISK FACTORS TO CONSIDER:
- Complexity of the code (more complex = higher risk)
- Frequency of code changes (frequently changed = higher risk)
- Business criticality (payment flow vs about page = very different risk)
- History of defects (areas that have had bugs before)
- New or unfamiliar technology
- Integration points between components
- Regulatory or compliance requirements
```

**Risk matrix for prioritising test areas:**

```
           | Low Impact | High Impact |
-----------+------------+-------------+
High Prob  |  MEDIUM    |    HIGH     |  <-- Test first
Low Prob   |   LOW      |   MEDIUM    |  <-- Test last or skip
```

**Practical application at Qoria:**

At the start of each sprint, I review the user stories being developed and I mentally assign each a risk level. A change to the core report generation engine gets high-priority test attention. A cosmetic label change on a settings page gets light-touch testing. This ensures that if we run out of testing time at the end of a sprint, we have covered the high-risk areas and made a conscious, documented choice to accept the risk on the low-risk ones.

Risk-based testing is how professional QA teams make intelligent trade-offs under time pressure, rather than either testing everything superficially or running out of time on low-importance features.

---

## Q22: How do you decide which tests to automate vs keep manual?

**A:**

This is a strategic decision and one I think about carefully at Qoria. Not everything should be automated, and not everything should stay manual. The decision framework I use considers several factors:

**Strong candidates for automation:**

```
AUTOMATE WHEN:
  1. High repetition — tests run every sprint or every build
     (regression suite, smoke suite)
  2. Stable features — requirements and UI are unlikely to change
  3. Data-driven — same test logic with many different input sets
  4. Time-consuming manually — long workflows with many steps
  5. Performance and load tests — impossible to simulate manually
  6. Cross-browser/cross-device — need to run on many environments
  7. CI/CD integration — need to run on every commit automatically
```

**Strong candidates for staying manual:**

```
KEEP MANUAL WHEN:
  1. Exploratory testing — requires human intuition and discovery
  2. Usability testing — requires subjective human judgement
  3. One-time or short-lived features — automation ROI is negative
  4. Frequently changing UI — locator maintenance would be costly
  5. Complex visual comparisons — pixel-perfect validation
  6. Accessibility testing — screen reader behavior needs human judgment
  7. New features — requirements still evolving, automation too early
```

**The ROI rule of thumb:**

> If the cost of building and maintaining the automation is MORE than the time saved by running it, keep it manual.

For example: if a test takes 5 minutes to run manually and we run it 10 times per year, that is 50 minutes of manual effort per year. If it takes 8 hours to automate and 2 hours per year to maintain, the automation breaks even after year 5. Not worth it for a low-value test.

At Qoria, our automation covers the regression suite and CI smoke tests. Complex exploratory scenarios and accessibility checks remain in our manual testing scope.

---

## Q23: What is cross-browser testing?

**A:**

Cross-browser testing is the practice of verifying that a web application functions correctly and looks as expected across multiple web browsers and browser versions. The same HTML, CSS, and JavaScript code can render and behave differently in Chrome, Firefox, Safari, and Edge due to differences in browser rendering engines.

**Why it matters:**

Different users use different browsers. If your application only works in Chrome and you have not tested Firefox or Safari, you may have broken functionality that affects a significant portion of your user base.

**What cross-browser testing checks:**

```
- Layout and rendering (CSS consistency)
- JavaScript execution (browser JS engine differences)
- Form element behaviour (dropdowns, date pickers, file uploads)
- Font rendering
- Animation and transition behavior
- Browser-specific API support (localStorage, Service Workers)
- Cookie and session handling
```

**Approach at Qoria:**

I test our EdTech Insight platform on Chrome, Firefox, and Edge as the primary matrix. We use Playwright, which makes this straightforward because Playwright natively supports running the same tests against Chromium, Firefox, and WebKit (Safari engine) with a single configuration change.

```javascript
// playwright.config.js — multi-browser config
projects: [
  { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  { name: 'firefox',  use: { ...devices['Desktop Firefox'] } },
  { name: 'webkit',   use: { ...devices['Desktop Safari'] } },
]
```

We run the core smoke suite across all three browsers on every build. The full regression suite runs against Chrome as primary, with targeted cross-browser runs before major releases.

---

## Q24: What is compatibility testing?

**A:**

Compatibility testing is a broader category of testing that verifies an application works correctly across different environments, configurations, and combinations of hardware and software. Cross-browser testing is one type of compatibility testing.

**Dimensions of compatibility testing:**

```
1. BROWSER COMPATIBILITY
   Chrome, Firefox, Safari, Edge — different versions

2. OPERATING SYSTEM COMPATIBILITY
   Windows 10/11, macOS Ventura/Sonoma, Ubuntu

3. DEVICE COMPATIBILITY
   Desktop, tablet, mobile — different screen sizes and resolutions

4. MOBILE COMPATIBILITY (specific type)
   iOS vs Android, different device models and OS versions

5. NETWORK COMPATIBILITY
   3G, 4G, Wi-Fi, slow connections, high latency

6. DATABASE COMPATIBILITY
   Same app working against MySQL, PostgreSQL, Oracle (if applicable)

7. SOFTWARE VERSION COMPATIBILITY
   - Backward compatibility: works with older versions of data formats
   - Forward compatibility: older version works with newer data

8. HARDWARE COMPATIBILITY
   Different screen resolutions, input devices, printer drivers
```

**How I approach it:**

At Qoria, our compatibility matrix is defined in our test strategy document. We have a defined set of "officially supported" browser and OS combinations that we test on every release. Anything outside that matrix is best-effort. The matrix was decided based on analytics data about what our actual users use.

The key principle is that you cannot test every possible combination — the number is astronomical. You identify the most common and highest-risk combinations and build your matrix from those.

---

## Q25: What is usability testing?

**A:**

Usability testing evaluates how easy, intuitive, and efficient an application is for real users to use. It focuses on the user experience — not whether features work correctly (that is functional testing), but whether they work in a way that actual people can understand and use without frustration.

**What usability testing examines:**

```
- Learnability: Can a new user figure out how to use the feature
                without training or documentation?
- Efficiency: Can experienced users complete tasks quickly?
- Memorability: After time away, can users remember how to do tasks?
- Error prevention: Does the design prevent users from making mistakes?
- Satisfaction: Do users find the experience pleasant or frustrating?
```

**Usability heuristics (Nielsen's 10 — commonly used framework):**

1. Visibility of system status (loading spinners, progress indicators)
2. Match between system and real world (use familiar language)
3. User control and freedom (undo, cancel, back)
4. Consistency and standards (same actions, same labels, same icons)
5. Error prevention (disable buttons until required fields are filled)
6. Recognition over recall (don't make users remember things)
7. Flexibility and efficiency (power user shortcuts)
8. Aesthetic and minimalist design (no unnecessary elements)
9. Help users recognize, diagnose, recover from errors
10. Help and documentation (accessible, searchable help)

**QA's role in usability testing:**

Formal usability testing involves actual users being observed while completing tasks. QA's role is usually to flag usability issues discovered during functional testing — if an error message is confusing, if a button placement is counterintuitive, or if a workflow requires too many steps. These are not functional defects but they are quality issues I log at Qoria as UI/UX improvement tickets.

---

## Q26: What is localization testing?

**A:**

Localization testing (also called L10n testing) verifies that an application has been correctly adapted for a specific locale, language, culture, or region. It goes beyond just translating text — it covers dates, currencies, number formats, addresses, right-to-left text, cultural sensitivities, and local regulations.

**Difference between Localisation and Internationalisation (i18n vs L10n):**

```
Internationalisation (i18n): Designing the application so it CAN
support multiple languages/locales without re-engineering.
(Engineering responsibility)

Localisation (L10n): Actually adapting the application for a
specific locale — translating text, formatting dates correctly, etc.
(QA verifies this is done correctly)
```

**What localization testing checks:**

```
1. TEXT TRANSLATION
   - All UI elements correctly translated (no English strings left in)
   - No truncation (some languages are much longer than English)
   - No broken characters or encoding issues (UTF-8)

2. DATE AND TIME FORMATS
   - UK: DD/MM/YYYY | US: MM/DD/YYYY | ISO: YYYY-MM-DD
   - 12-hour vs 24-hour clock

3. NUMBER AND CURRENCY FORMATS
   - US: 1,234.56 | Germany: 1.234,56 | India: 1,23,456.78
   - Currency symbols and placement ($100 vs 100€)

4. ADDRESS FORMATS
   - Different field orders per country (City/State/Zip varies)

5. RIGHT-TO-LEFT (RTL) SUPPORT
   - Arabic, Hebrew: text flows right to left
   - Layout must mirror (left nav becomes right nav)

6. CULTURAL APPROPRIATENESS
   - Images, icons, colours may have different meanings
   - Avoid culturally insensitive imagery

7. LEGAL AND REGULATORY
   - GDPR compliance for EU, PDPA for Thailand, etc.
   - Cookie consent dialogs per jurisdiction
```

At Qoria, we serve schools across multiple countries, so localization testing is relevant — particularly for date format display in reports and ensuring that region-specific currency or measurement formats are correct.

---

## Q27: What is accessibility testing?

**A:**

Accessibility testing verifies that a software application can be used by people with disabilities, including visual, auditory, motor, and cognitive impairments. The international standard for web accessibility is WCAG — Web Content Accessibility Guidelines — with Level AA being the most commonly required compliance level.

**Why it matters:**

In many jurisdictions (UK, US, EU, Australia), accessibility compliance is a legal requirement for public-facing digital services. Beyond compliance, it is simply the right thing — software should be usable by everyone.

**Common accessibility tests:**

```
1. KEYBOARD NAVIGATION
   - All interactive elements reachable by Tab key
   - Correct focus order (logical top-to-bottom, left-to-right)
   - No keyboard traps (can always Tab out of any component)
   - Visible focus indicator (not hidden by CSS)

2. SCREEN READER COMPATIBILITY
   - All images have descriptive alt text
   - Form fields have associated labels
   - Buttons have meaningful text (not just "Click here")
   - Dynamic content changes announced to screen reader

3. COLOUR AND CONTRAST
   - Text to background contrast ratio: 4.5:1 minimum (WCAG AA)
   - Information not conveyed by colour alone
   - Error states not indicated only by red colour

4. RESPONSIVE AND ZOOM
   - Text can be resized to 200% without loss of content
   - No horizontal scrolling at 320px width

5. FORM ACCESSIBILITY
   - Required fields indicated (not just by asterisk)
   - Error messages clearly associated with the problem field

TOOLS USED:
- axe-core (automated) — integrates with Playwright
- WAVE browser extension
- Lighthouse accessibility audit
- NVDA or VoiceOver (screen reader manual testing)
```

At Qoria, I have used axe-core integrated with Playwright to run automated accessibility scans as part of our test suite, and I supplement these with manual keyboard navigation testing for new features.

---

## Q28: What is performance testing vs load testing vs stress testing?

**A:**

These three terms are closely related but test different aspects of a system's non-functional behaviour under varying load conditions. Let me define each clearly and compare them.

**Performance testing** is the umbrella term for all testing that evaluates how fast, stable, and scalable a system is under various conditions. Load and stress testing are subtypes of performance testing.

```
+------------------+-----------------------+-------------------------+
| Type             | What it tests         | Goal                    |
+------------------+-----------------------+-------------------------+
| Performance      | Speed, response time, | Baseline performance    |
| Testing          | throughput under       | characterization        |
| (parent)         | normal load            |                        |
+------------------+-----------------------+-------------------------+
| Load Testing     | System behaviour under | Verify system handles   |
|                  | expected peak load     | expected concurrent     |
|                  |                        | users without degrading |
+------------------+-----------------------+-------------------------+
| Stress Testing   | System behaviour under | Find the breaking point |
|                  | load BEYOND capacity   | and how it fails        |
|                  |                        | (gracefully or not)     |
+------------------+-----------------------+-------------------------+
| Spike Testing    | Sudden extreme load    | Handles sudden traffic  |
|                  | in short burst         | spike (e.g. viral event)|
+------------------+-----------------------+-------------------------+
| Soak/Endurance   | Normal load over a     | Memory leaks, resource  |
| Testing          | long time period       | exhaustion over time    |
+------------------+-----------------------+-------------------------+
| Volume Testing   | Large amounts of data  | System handles large    |
|                  | in the database        | data volumes correctly  |
+------------------+-----------------------+-------------------------+
```

**Real example using JMeter:**

At Qoria, if I were to load test the report generation API:
- **Performance baseline:** 1 user, measure response time — should be under 2 seconds
- **Load test:** 500 concurrent users, verify response time stays under 5 seconds and success rate stays above 99%
- **Stress test:** Ramp users to 2000, 5000 — identify at what point the system starts failing and how it behaves when it does (does it crash or degrade gracefully?)

I have used JMeter for performance testing in my academic projects and understand how to set up thread groups, ramp-up periods, and response time assertions.

---

## Q29: What is the difference between verification and validation?

**A:**

Verification and Validation are two fundamental quality assurance concepts that are often confused. The simplest way to remember them is:

> "Verification = Are we building the product right? Validation = Are we building the right product?"

**Verification:**
- Checks that the work product (code, design, test case) conforms to its specification
- Involves inspections, reviews, walkthroughs — does NOT require running the software
- Example: Reviewing a design document to confirm it matches the requirements
- Question answered: "Does this code/design/document match what the spec says?"

**Validation:**
- Checks that the final software meets the real needs of the user or business
- Involves actual execution of the software in real or representative conditions
- Example: Running the application and confirming users can complete their tasks
- Question answered: "Does this software do what the user actually needs?"

```
+------------------+------------------------+-------------------------+
| Dimension        | Verification           | Validation              |
+------------------+------------------------+-------------------------+
| When             | During development     | At the end of a phase   |
|                  | (before execution)     | or project              |
+------------------+------------------------+-------------------------+
| What             | Documents, code,       | The running software    |
|                  | designs, test cases    |                         |
+------------------+------------------------+-------------------------+
| How              | Reviews, inspections,  | Testing, demos,         |
|                  | walkthroughs, audits   | UAT, pilots             |
+------------------+------------------------+-------------------------+
| Goal             | "Built it right"       | "Built the right thing" |
+------------------+------------------------+-------------------------+
| Example          | Code review against    | User runs UAT and       |
|                  | design doc             | confirms it meets needs |
+------------------+------------------------+-------------------------+
```

Both are essential. You can verify a perfectly coded system (built it right) that still does not meet the user's actual needs (not the right thing). Both types of QA activities are needed to deliver quality software.

---

## Q30: What is a test strategy vs test plan vs test cases?

**A:**

These three artefacts exist at different levels of abstraction. Think of them as zoom levels — from the 10,000-foot view down to the ground level.

```
HIERARCHY:

  TEST STRATEGY (Highest level)
  "HOW we approach quality in this organisation/project"
      |
  TEST PLAN (Project/release level)
  "WHAT we will test in THIS release and HOW"
      |
  TEST CASES (Execution level)
  "EXACTLY what steps we will execute to test THIS feature"
```

**Test Strategy:**
- Organisation-wide or project-wide document
- Covers testing philosophy, tools, automation approach, risk approach
- Written once (or rarely updated) by QA Lead / Head of QA
- Example content: "We use Playwright for UI automation, TestNG for unit/API, Agile-aligned testing, risk-based prioritisation, CI/CD integration"

**Test Plan:**
- Written for a specific project or release
- Covers scope, schedule, resources, entry/exit criteria for THIS release
- Updated per major release
- Example content: "Sprint 12 release: in scope — report export module; out of scope — admin portal; 3 QA engineers; release on Nov 30"

**Test Cases:**
- Written for each specific feature or user story
- Detailed, step-by-step instructions
- Multiple test cases per feature
- Example content: "TC_RPT_001: Generate report for date range spanning DST change — Steps 1-6 — Expected: correct record count"

At Qoria, our test strategy lives in Confluence and is updated quarterly. Our test plan is an Agile one-pager per release. Our test cases live in JIRA linked to user stories.

---

## Q31: What is ad-hoc testing?

**A:**

Ad-hoc testing is unstructured, unplanned testing performed without test cases, test plans, or documentation. The tester tests the application randomly based on their knowledge and intuition, with the goal of finding defects that structured testing might miss.

It is the opposite of scripted testing, and it should not be confused with exploratory testing (which, despite being creative, still has a charter and session structure).

**Characteristics:**
- No documentation before or during testing
- No predefined test cases
- Random selection of features to test
- Based on tester's experience and gut feeling
- Not repeatable in the same way

**When ad-hoc testing is useful:**
- When you have leftover time after scripted test execution
- When a new build arrives and you want a quick sanity impression
- When you have deep knowledge of the system and can leverage it
- When you want to test "what would a mischievous user do?"

**Limitation:**

Because ad-hoc testing produces no documentation, defects found must be logged immediately before they are forgotten. Also, ad-hoc testing cannot demonstrate coverage — you cannot prove to a stakeholder what was and was not tested.

**Pair testing** is a variant where two testers test ad-hoc together — one operates the application while the other observes and suggests scenarios. This is more effective than solo ad-hoc testing.

At Qoria, I use ad-hoc testing informally as a supplement after I have completed my scripted test cases, particularly when I have a hunch about an area that might be fragile.

---

## Q32: What is pairwise testing?

**A:**

Pairwise testing (also called all-pairs testing) is a combinatorial test design technique used when a system has multiple input parameters and you need to test combinations of them, but testing ALL combinations is impractical.

The pairwise principle states that most defects are caused by the interaction of TWO parameters at a time, not three or more simultaneously. So instead of testing every possible combination (which grows exponentially), we test every pair of parameter values at least once.

**Problem without pairwise:**

Suppose a form has:
- 3 browsers: Chrome, Firefox, Edge
- 4 operating systems: Windows, macOS, Linux, iOS
- 2 user roles: Admin, Student

Full combination count: 3 × 4 × 2 = **24 test cases**

With pairwise, we can cover all pairs in approximately **9 test cases**.

**Example pairwise table (simplified):**

```
+----+----------+------------+---------+
| TC | Browser  | OS         | Role    |
+----+----------+------------+---------+
| 1  | Chrome   | Windows    | Admin   |
| 2  | Chrome   | macOS      | Student |
| 3  | Chrome   | Linux      | Admin   |
| 4  | Firefox  | Windows    | Student |
| 5  | Firefox  | macOS      | Admin   |
| 6  | Firefox  | iOS        | Student |
| 7  | Edge     | Windows    | Student |
| 8  | Edge     | Linux      | Student |
| 9  | Edge     | iOS        | Admin   |
+----+----------+------------+---------+
Every pair of values appears at least once.
```

Tools like PICT (Microsoft) or AllPairs can generate pairwise tables automatically. At Qoria, I apply pairwise thinking informally when defining our cross-browser and device test matrix — choosing which combinations to run for our compatibility suite rather than testing the full combinatorial explosion.

---

## Q33: What is a test environment?

**A:**

A test environment is the combination of hardware, software, network configuration, data, and tools that are set up to execute testing activities. It is a replica (or controlled simulation) of the production environment where the software will eventually run.

**Why test environments matter:**

Testing in an inappropriate environment can give false results — tests that pass in a dev environment may fail in production because of infrastructure differences.

**Typical environment tiers:**

```
ENVIRONMENT HIERARCHY:

  [Local / Dev]          Developer's machine, personal testing
        |
  [Development (DEV)]    Latest code, unstable, dev team uses this
        |
  [Testing / QA]         Stable enough for QA, test cases run here
        |
  [Staging / Pre-prod]   Mirror of production, UAT happens here
        |
  [Production (PROD)]    Live environment, real users, real data
```

**Components of a test environment:**

```
- Server infrastructure (cloud, on-prem, Docker containers)
- Application build and version
- Database with test data
- Network configuration (firewalls, proxies, SSL)
- Browser and OS combinations
- Testing tools installed (Selenium, Playwright, JMeter)
- Configuration files (.env for each environment)
- Test data — pre-populated and refreshed between runs
```

**Challenges I have faced with test environments at Qoria:**

Environment instability is a real problem. Sometimes the QA environment goes down during a sprint because of a bad deployment, and I raise this as a blocker immediately. I have learned to always verify the environment is healthy as the first step before executing any tests — if the environment is broken, all test results are meaningless.

---

## Q34: What is entry criteria and exit criteria?

**A:**

Entry criteria are the conditions that must be met before a testing phase can begin. Exit criteria are the conditions that must be met before testing is considered complete. They are defined in the test plan and protect the quality of the testing process.

**Why they matter:**

Without entry criteria, testers waste time testing against an unstable or incomplete build. Without exit criteria, there is no objective definition of "done" — testing could go on indefinitely or be cut short without proper justification.

**Typical Entry Criteria for a test execution phase:**

```
ENTRY CRITERIA:
  1. Test environment is set up and stable (smoke test passed)
  2. Build has been deployed and verified by developer
  3. Test cases for this sprint are reviewed and approved
  4. Test data is prepared and loaded
  5. All critical blockers from previous sprint are resolved
  6. Required access credentials are available to QA team
```

**Typical Exit Criteria for a release:**

```
EXIT CRITERIA:
  1. All test cases planned for this release have been executed
  2. Zero open Critical or High severity defects
  3. Medium and Low defects deferred have been acknowledged by PM
  4. Regression suite pass rate is >= 95%
  5. Test Summary Report has been reviewed and signed off
  6. All UAT sign-off received from product owner
  7. All defects in "Verified" state are moved to "Closed"
```

**What to do when entry criteria are not met:**

At Qoria, if I receive a build and the smoke test fails, I immediately raise a blocker in JIRA, notify the team in Slack, and do not proceed with the sprint testing plan until the build is fixed. Testing against a broken build produces unreliable results and wastes everyone's time.

---

## Q35: What is a test summary report?

**A:**

A test summary report is a document produced at the end of a testing phase or sprint that summarises the testing activities performed, test results, defects found, and provides a quality recommendation to stakeholders.

It is the QA team's formal communication to project management, product owners, and stakeholders about the current quality state of the software and whether it is ready to release.

**Standard sections of a test summary report:**

```
1. EXECUTIVE SUMMARY
   Brief narrative: what was tested, outcome, recommendation

2. TEST SCOPE
   What features/modules were included and excluded

3. TEST EXECUTION SUMMARY
   +--------------------------+--------+
   | Total test cases planned |  150   |
   | Executed                 |  145   |
   | Passed                   |  138   |
   | Failed                   |    5   |
   | Blocked                  |    2   |
   | Not executed             |    5   |
   | Pass rate                | 95.2%  |
   +--------------------------+--------+

4. DEFECT SUMMARY
   +----------+--------+------+--------+------+-------+
   | Severity | Raised | Open | Closed | Def. | Rej.  |
   +----------+--------+------+--------+------+-------+
   | Critical |   2    |  0   |   2    |  0   |   0   |
   | High     |   5    |  1   |   4    |  0   |   0   |
   | Medium   |   8    |  3   |   4    |  1   |   0   |
   | Low      |   6    |  4   |   2    |  0   |   0   |
   +----------+--------+------+--------+------+-------+

5. RISK ASSESSMENT
   - Known risks with untested areas
   - Risk acceptance decisions made

6. QA RECOMMENDATION
   GO: Recommend release with noted conditions
   NO-GO: Do not release until [specific issues resolved]

7. SIGN-OFF
   QA Lead signature / approval
```

At Qoria, I produce a sprint-level test summary at the end of each sprint to feed into our sprint review and release readiness meeting.

---

## Q36: How do you test a feature when requirements are unclear?

**A:**

Unclear requirements are one of the most common challenges in real-world QA, and handling them professionally is an important skill. Here is my approach:

**Step 1 — Do not start testing on an assumption.** Testing against a wrong assumption produces tests that verify the wrong thing. First, I identify specifically what is unclear.

**Step 2 — Document my questions.** I write out exactly what I do not understand and what clarification I need. I prefer written questions because they create a documented record of the ambiguity.

**Step 3 — Seek clarification from the right person.** For business rule questions, I go to the product owner. For technical implementation questions, I go to the developer. For UI behaviour questions, I may look for existing similar patterns in the application.

**Step 4 — Reference existing similar features.** If the new feature is similar to an existing one, I use the existing feature's behaviour as a reference. Most products have internal consistency that helps fill gaps.

**Step 5 — Make assumptions explicit if needed.** If clarification is not available in time (e.g., the product owner is unavailable), I document my assumption clearly in the JIRA ticket: "Testing against assumption: X. Please confirm." This way, if the assumption is wrong, it is visible and correctable, not buried.

**Step 6 — Use exploratory testing first.** Before scripted test cases, I explore the feature freely to understand how it actually behaves. Sometimes the implementation makes the intent clear even when the requirement is vague.

**Step 7 — Schedule a three-amigos session.** In Agile, this is a meeting between the developer, QA, and product owner to review the story together before development begins. I advocate for these sessions at Qoria whenever stories have ambiguous acceptance criteria.

---

## Q37: How do you handle a blocked test?

**A:**

A blocked test is one that cannot be executed because of a dependency that is preventing progress — a broken environment, a missing prerequisite feature, unavailable test data, or a dependent defect that has not been fixed.

**My step-by-step approach:**

```
STEP 1 — Identify and document the blocker
  What exactly is blocking the test? Be specific.
  Example: "TC_RPT_005 blocked because the CSV export button
  throws a 500 error (see DEF-238) and cannot proceed."

STEP 2 — Log the blocked status in JIRA
  Mark the test case as "Blocked" (not "Failed")
  Link it to the blocking defect or dependency

STEP 3 — Raise the blocker in standup
  Bring it to the team's attention the same day
  Do not wait — blockers left unaddressed compound

STEP 4 — Continue with other tests
  Do not sit idle waiting for the blocker to be resolved
  Move to other test cases in the sprint and return to
  the blocked one when the dependency is resolved

STEP 5 — Re-assess risk
  If the blocker cannot be resolved before the release window,
  raise it in the exit criteria review. A blocked test is a
  known risk that the product owner must accept or reject.

STEP 6 — Unblock and execute
  When the dependency is resolved, execute the blocked test
  and update its status immediately
```

**Important:** A blocked test is different from a failed test. Blocked means the test could not run, not that it ran and failed. Distinguishing these in reports is important — a failed test tells you about a product defect; a blocked test tells you about a process or environment issue.

---

## Q38: What do you do when a developer says "it's not a bug, it's a feature"?

**A:**

This is a real situation I have encountered, and it requires a combination of professionalism, evidence, and the right escalation path.

**My approach:**

**Step 1 — Do not argue in the moment.** My first response is to acknowledge the developer's perspective without dismissing it. They may be right — the spec might genuinely be silent on that behaviour.

**Step 2 — Go back to the requirements.** I check the user story, the acceptance criteria, any design specs or wireframes, and previous discussions recorded in JIRA. If the requirements are silent, the developer has a valid point. If the requirements say the opposite of what the application is doing, I have evidence to support the defect.

**Step 3 — Present evidence, not opinion.** I re-open the JIRA ticket and add a comment with the specific requirement reference, a screenshot of the requirement, and a clear statement of the gap between specification and implementation. I keep it factual and professional, not personal.

**Step 4 — Involve the product owner.** If the disagreement persists, the product owner is the right tie-breaker. The PO defines what the product should do. I frame the escalation carefully: "There is a disagreement about intended behaviour on [ticket]. Can you clarify whether [specific behaviour] is expected?" I do not say "the developer is wrong" — I ask for clarification.

**Step 5 — Accept the outcome.** If the product owner confirms it is working as designed, I update the defect to "Not a Bug / By Design" and move on. If the PO confirms it is a defect, the developer proceeds with the fix. In either case, I ensure the outcome is documented.

**Step 6 — Improve the requirements upstream.** After resolution, I add the clarified behaviour to the acceptance criteria so that future testers and developers have that clarity from the start.

The key principle is that this disagreement is about specification, not about personalities. Keeping it professional and evidence-based produces the right outcome every time.

---

## Q39: How do you prioritise tests when time is limited?

**A:**

Time pressure is a constant reality in Agile testing, and prioritising effectively is one of the most important skills a QA engineer can have. My approach combines risk-based thinking with stakeholder input.

**My prioritisation framework:**

```
TIER 1 — MUST TEST (Non-negotiable)
  - Core user journeys (happy paths for primary features)
  - Any area that changed in this sprint
  - Previously failing areas that were recently fixed
  - High-business-value features (payment, login, core workflows)
  - Anything the product owner has flagged as high priority

TIER 2 — SHOULD TEST (If time permits)
  - Negative and edge cases for Tier 1 features
  - Regression of adjacent features (may be affected by changes)
  - Medium-priority acceptance criteria

TIER 3 — NICE TO TEST (Accept the risk if skipped)
  - Low-usage, low-risk features with stable history
  - Cosmetic and UI polish items
  - Extremely unlikely edge cases
```

**Specific techniques I use:**

1. **Risk × Impact matrix** — I map each test area against its risk of failure and business impact. High risk + high impact = test first.

2. **Change-based prioritisation** — What changed in this build? Test that area thoroughly first. Stable areas with no code changes need less attention.

3. **Defect history** — Areas that have had bugs before are more likely to have bugs again. Test those earlier.

4. **Stakeholder alignment** — Before starting execution, I quickly align with the product owner on which features are most critical for this release. Their business context helps refine my technical risk assessment.

5. **Explicit risk documentation** — If I skip Tier 3 items, I document it clearly: "The following test areas were not executed due to time constraints: [list]. Risk level: Low. Accepted by [PO name]." This protects both me and the team.

At Qoria, I apply this framework at the start of every sprint as part of my test planning, so that by the time testing begins, I already have my priority order established.

---

## Q40: What is the shift-left testing approach?

**A:**

Shift-left testing is the practice of moving testing activities earlier in the software development lifecycle — "to the left" on the timeline — rather than waiting until a feature is fully developed before testing begins.

The traditional (shifted-right) approach: design → develop → develop → develop → test → fix → release

The shift-left approach: test planning → test during design → test during development → test after development → release

**Why shift-left matters:**

```
COST OF DEFECTS BY PHASE (Relative cost to fix):

  Requirements phase:   $1
  Design phase:         $5
  Development phase:    $10
  Testing phase:        $50
  Production:           $200+

The later a defect is found, the more expensive it is to fix.
Shift-left finds defects earlier, dramatically reducing cost.
```

**Shift-left activities at each phase:**

```
REQUIREMENTS PHASE:
  - QA reviews user stories and AC before development starts
  - Raises ambiguities and testability concerns early
  - Three-amigos sessions with dev and PO
  - QA writes test conditions alongside requirement writing

DESIGN PHASE:
  - QA reviews design documents and API contracts
  - Flags design decisions that make testing difficult
  - Starts writing test cases before code is written

DEVELOPMENT PHASE:
  - Developers write unit tests (TDD / BDD)
  - QA provides early feedback on partial builds
  - Static code analysis and code reviews include QA perspective
  - API contract testing before UI is built

DEPLOYMENT:
  - Tests integrated into CI/CD (automated on every commit)
  - Immediate feedback when code breaks tests
```

**How I practise shift-left at Qoria:**

I attend sprint planning and actively review user stories before the sprint begins. If acceptance criteria are unclear, I raise them then — not after development is complete. I also start writing test cases during the development phase so they are ready the moment code is ready to test. This means I can begin meaningful testing the same day a feature is declared "dev complete," rather than spending days writing test cases first.

The cultural shift required for shift-left is that QA is not a gatekeeper at the end of the pipeline — QA is a collaborative partner throughout the entire process. That is the mindset I bring to every sprint at Qoria and every role I work in.

---

*End of Part 2 — Manual Testing Interview Questions*
