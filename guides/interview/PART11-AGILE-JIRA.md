# Part 11 — Agile & JIRA | 30 Questions | Full Answers

> CV context: Kupeshanth — Qoria Lanka (GitHub Actions, GCP, Playwright), Cerexio (JIRA, Agile).
> Skills base: Java, Python, Selenium, TestNG, Postman, REST API testing.

---

## Q1. What is Agile? How is it different from Waterfall?

**Answer:**

**Waterfall** is sequential — you complete one phase entirely before starting the next: Requirements → Design → Development → Testing → Deployment. Testing only begins after all development is done. If a requirement was wrong at the start, discovering it in testing is expensive.

**Agile** is iterative and incremental — work is delivered in short cycles (sprints, 1-4 weeks). Each sprint delivers a working, tested piece of software. Feedback is continuous.

| Dimension | Waterfall | Agile |
|---|---|---|
| Delivery | One big release at end | Small releases every sprint |
| Requirements | Fixed at start | Evolving, prioritised |
| Testing | Phase after development | Continuous, within sprint |
| Customer involvement | At start and end | Every sprint review |
| Change management | Very costly | Embraced |
| Team structure | Silos (Dev, QA, BA separate) | Cross-functional team |
| Risk | High (discovered late) | Low (discovered early) |
| Documentation | Comprehensive | Just enough |

**QA perspective:** In Waterfall, QA receives everything at the end and is pressured to compress test time. In Agile, QA is involved from day one — reviewing stories, clarifying AC, writing test cases before dev starts, and testing within the same sprint.

---

## Q2. Scrum vs Kanban — when does each apply?

**Answer:**

| Feature | Scrum | Kanban |
|---|---|---|
| Iterations | Fixed-length sprints (1-4 weeks) | Continuous flow, no sprints |
| Roles | Scrum Master, Product Owner, Dev Team | No fixed roles |
  | WIP limits | Implicit (sprint scope) | Explicit (e.g., max 3 in "In Progress") |
| Changes | Can't change sprint scope mid-sprint | Can add work anytime |
| Meetings | Fixed ceremonies (planning, standup, review, retro) | Daily standup recommended, others optional |
| Best for | Feature development teams with stable sprint cadence | Support/ops teams, continuous delivery |
| Metrics | Velocity (story points per sprint) | Cycle time, throughput |

**At Cerexio:** We used Scrum with 2-week sprints. JIRA boards were set up as Scrum boards with sprint planning and backlog. QA had defined testing slots in each sprint.

---

## Q3. Explain all Scrum ceremonies and QA's role in each.

**Answer:**

### 1. Sprint Planning (Start of sprint, 2-4 hours)
**Purpose:** Team selects user stories from backlog for the sprint and breaks them into tasks.

**QA's role:**
- Review acceptance criteria (AC) for each story — flag ambiguities before dev starts
- Ask "How will we know this is done?" for every story
- Estimate testing effort (story points include test effort)
- Identify stories that need test data setup or environment changes
- Flag dependencies between stories that affect testing order

**Good QA question in planning:** "This story says 'users can filter by date range' — does that include single-day selections? What happens with invalid date ranges?"

### 2. Daily Standup (Every day, 15 min)
**Purpose:** Synchronize the team — what was done, what's planned, any blockers.

**QA's role:**
- Report: "Yesterday I completed testing for STORY-123. Today I'll start STORY-145. Blocker: the API for payment is returning 500 — I need dev to look at it."
- Flag stories that are sitting in "Dev Done" without reaching QA
- Surface testing blockers early (environment down, data missing)
- Track if dev is on track — if dev is late, QA has less time

### 3. Sprint Review / Demo (End of sprint, 1-2 hours)
**Purpose:** Team demonstrates completed stories to stakeholders and Product Owner accepts or rejects.

**QA's role:**
- Present testing evidence: which tests passed, which scenarios were covered
- Demonstrate test automation ran successfully on the CI pipeline
- Flag stories that were NOT completed to testing standards
- Support PO with evidence for acceptance/rejection decisions

### 4. Sprint Retrospective (End of sprint, 1-2 hours)
**Purpose:** Team reflects on process — what went well, what to improve.

**QA's role:**
- Raise testing-specific process issues: "Stories came to QA too late in sprint"
- Suggest improvements: "We should add API contract tests to prevent the regression we had this sprint"
- Celebrate wins: "The smoke suite caught the login bug before it hit staging"
- Raise flaky test issues that slowed down CI

### 5. Backlog Grooming / Refinement (Mid-sprint, 1-2 hours)
**Purpose:** PO and team review upcoming stories, estimate them, add detail.

**QA's role:**
- Review AC before stories enter sprint — easier to fix AC in refinement than during sprint
- Add test cases or edge cases to AC: "Should also verify: empty list state, max 100 items"
- Identify testability issues: "This story has no way to reset state in automation — we need a test hook"
- Break large stories into testable increments

---

## Q4. What is a sprint?

**Answer:**
A sprint is a time-boxed iteration (typically 1-4 weeks, commonly 2 weeks) in which a team completes a defined set of work and delivers a potentially shippable product increment.

Key sprint rules:
- **Fixed duration** — the end date does not move
- **Sprint goal** — a single sentence describing the sprint's focus ("Enable users to register and verify email")
- **Scope is locked** — no new stories added once sprint starts (emergencies handled separately)
- **Potentially shippable** — by end of sprint, completed stories should be tested and ready to deploy

**Sprint lifecycle:**
```
Sprint Planning → Dev (1st week) → Dev + QA (2nd week) → Sprint Review → Retrospective → Repeat
```

**QA perspective:** A sprint ends when testing is complete, not when development is complete. If dev delivers a story on the last day, it cannot be tested in that sprint — it should not be included in the sprint commitment.

---

## Q5. What are story points?

**Answer:**
Story points are a relative measure of effort, complexity, and uncertainty for a user story. They are not hours.

**Fibonacci scale (most common):** 1, 2, 3, 5, 8, 13, 21

**What they measure:**
- Effort required
- Complexity of the work
- Uncertainty/risk

**Example estimation:**
- Story: "Add a tooltip to the login button" → 1 point (trivial)
- Story: "Implement forgot password flow" → 5 points (moderate complexity, email service, token expiry)
- Story: "Integrate payment gateway" → 13 points (high complexity, external service, edge cases)

**QA's role in estimation:** QA estimates together with developers. A story that looks like 3 points to dev might be 8 from QA's perspective if it has many edge cases to test. Planning Poker helps surface this.

**Velocity:** Team's average story points completed per sprint. Used for sprint planning. If velocity is 40 points, don't commit to 60.

---

## Q6. Explain Epic, Story, Task, Subtask, and Bug in JIRA.

**Answer:**

| Type | Description | Example |
|---|---|---|
| **Epic** | Large body of work spanning multiple sprints | "User Authentication System" |
| **Story** | User-facing feature deliverable in one sprint | "As a user, I can reset my password via email" |
| **Task** | Technical work (not user-facing) | "Set up password reset email template in SendGrid" |
| **Subtask** | Smaller piece under a Story or Task | "Write automation tests for password reset" |
| **Bug** | Defect in existing functionality | "Password reset link expires after 1 hour but UI shows 24 hours" |

**Hierarchy in JIRA:**
```
Epic: User Authentication System
 ├── Story: User Registration
 │    ├── Subtask: Dev — Build /register API
 │    ├── Subtask: QA — Write automation tests for registration
 │    └── Subtask: QA — Add negative test cases
 ├── Story: Login
 ├── Story: Password Reset
 └── Story: Email Verification
```

**Bug vs Story distinction:**
- Bug: existing feature is broken
- Story: new feature or change in behaviour

---

## Q7. How do you write a perfect bug report? Give the formula and 3 examples.

**Answer:**

### The Formula
```
Title: [Component] [Action] [Expected vs Actual] — concise, searchable

Environment: OS, Browser, App version, Environment (staging/prod)
Severity: Critical/High/Medium/Low
Priority: P1/P2/P3/P4

Steps to Reproduce:
1. (Exact, numbered, reproducible steps)
2.
3.

Expected Result: What SHOULD happen (per AC or requirements)
Actual Result: What DOES happen

Attachments: Screenshot, video, logs, network trace

Additional Context: Frequency (always/intermittent), workaround, related tickets
```

---

### Bug Example 1: UI Bug

**Title:** `[Login] Submit button remains enabled after successful logout — session not cleared`

**Environment:** Chrome 123, macOS 14, Staging v2.4.1

**Severity:** High | **Priority:** P2

**Steps to Reproduce:**
1. Navigate to https://staging.myapp.com/login
2. Enter valid credentials (user@test.com / Password123!)
3. Click "Login" — verify you are redirected to Dashboard
4. Click "Logout" — verify you are redirected to Login page
5. Click browser Back button
6. Observe the Dashboard is still visible

**Expected Result:** Browser Back after logout should redirect to Login page. Session should be invalidated — no authenticated page should be accessible.

**Actual Result:** Dashboard is displayed after logout. User can navigate the app as if still logged in. Refreshing the page then forces redirect to Login — suggesting the session token is still in memory.

**Attachments:** screen-recording-logout-back.mp4, network-trace.har

**Impact:** Security issue — in a shared computer scenario, another user could access the previous user's account data.

---

### Bug Example 2: API Bug

**Title:** `[POST /api/users] Returns 200 instead of 201 when user is created successfully`

**Environment:** Postman / RestAssured, Staging API v2.4.1

**Severity:** Medium | **Priority:** P3

**Steps to Reproduce:**
1. Send POST request to `/api/users`:
```json
POST /api/users
Content-Type: application/json
{
  "email": "newuser@example.com",
  "name": "New User",
  "role": "user"
}
```
2. Observe the HTTP response status code

**Expected Result:** HTTP 201 Created (per REST standards and API documentation)

**Actual Result:** HTTP 200 OK

**Additional context:** The user IS created correctly in the database (verified via SQL). The response body is correct. Only the status code is wrong. This will break clients that check for 201 to confirm creation.

---

### Bug Example 3: Data Bug

**Title:** `[Checkout] Order total calculated incorrectly when discount coupon applied — tax added before discount`

**Environment:** Chrome 123, Safari 17, Staging, all user types

**Severity:** Critical | **Priority:** P1

**Steps to Reproduce:**
1. Add item priced $100.00 to cart
2. Apply coupon code "SAVE10" (10% discount)
3. Proceed to checkout
4. Observe the order summary

**Expected Result:**
```
Item:      $100.00
Discount:  -$10.00  (10% off item price)
Tax (15%): $13.50   (15% of $90.00 discounted price)
Total:     $103.50
```

**Actual Result:**
```
Item:      $100.00
Tax (15%): $15.00   (15% of full price — applied BEFORE discount)
Discount:  -$10.00
Total:     $105.00
```

**Financial Impact:** Customer is overcharged $1.50 per order. With 500 orders/day, this is $750/day overcharge. Potential for chargebacks and legal action.

**Verified in DB:** `SELECT amount, tax_amount, discount_amount FROM orders WHERE id = 12345;` confirms incorrect tax amount stored.

---

## Q8. Severity vs Priority — explain with a 4x4 matrix.

**Answer:**

**Severity:** How badly the bug impacts the application (technical impact).
**Priority:** How urgently it needs to be fixed (business impact).

They are independent — a bug can be high severity but low priority, and vice versa.

```
                    PRIORITY
                Low          High
         ┌─────────────┬─────────────┐
HIGH     │ High Sev    │ High Sev    │
         │ Low Pri     │ High Pri    │
SEVERITY │ (Fix next   │ (Fix NOW)   │
         │ sprint)     │             │
         ├─────────────┼─────────────┤
LOW      │ Low Sev     │ Low Sev     │
         │ Low Pri     │ High Pri    │
         │ (Nice fix)  │ (Fix for    │
         │             │ visibility) │
         └─────────────┴─────────────┘
```

**Examples:**

| Scenario | Severity | Priority | Reason |
|---|---|---|---|
| Payment API returns 500 — users cannot check out | Critical | P1 | Revenue impact |
| Typo in Terms and Conditions page | Low | P1 | Legal/compliance |
| App crashes if user has 0 orders | High | P3 | Rare edge case, fix next sprint |
| Login button misaligned by 2px | Low | P4 | Cosmetic, low impact |
| Admin password visible in API response | Critical | P1 | Security breach |
| Dark mode has wrong shade of grey | Low | P4 | Cosmetic |

---

## Q9. Defect lifecycle — all statuses explained.

**Answer:**
```
New → Assigned → In Progress → Fixed → Ready for Testing → Verified/Closed
                                     ↗                   ↘
                               Reopened ←─────────────── Rejected/Won't Fix
```

| Status | Description | Who Acts |
|---|---|---|
| **New** | Bug reported, not yet triaged | QA reports it |
| **Assigned** | Dev team accepts and assigns to developer | Dev Lead/Scrum Master |
| **In Progress** | Developer actively working on fix | Developer |
| **Fixed** | Developer marks as fixed, deployed to test environment | Developer |
| **Ready for Testing** | Fix is deployed, waiting for QA to verify | Developer |
| **Verified / Closed** | QA confirms fix works — bug closed | QA |
| **Reopened** | QA retests — bug still exists or fix caused new bug | QA |
| **Rejected** | Dev says it's not a bug (by design, misunderstood) | Developer |
| **Won't Fix** | PO/team decides not to fix (low value, too costly) | PO/Team |
| **Duplicate** | Same bug already reported in another ticket | QA/Dev |
| **Deferred** | Fix postponed to a future release | PO |

**QA responsibility at each step:**
- Write thorough bug report → New
- Follow up to ensure assignment → Assigned
- Retest on Fixed → Verified or Reopened
- If Rejected — discuss with dev, provide more evidence
- If Won't Fix — acknowledge and document any workaround

---

## Q10. JQL queries — 10 real QA queries.

**Answer:**
JQL (JIRA Query Language) is the SQL of JIRA.

```
-- 1. All open bugs in current sprint
project = MYAPP AND issuetype = Bug AND status != Done AND sprint in openSprints()

-- 2. Bugs assigned to me
project = MYAPP AND issuetype = Bug AND assignee = currentUser() AND status != Done

-- 3. Bugs created this week
project = MYAPP AND issuetype = Bug AND created >= startOfWeek()

-- 4. Critical bugs that are unresolved
project = MYAPP AND issuetype = Bug AND severity = Critical AND resolution = Unresolved

-- 5. Stories in "QA Testing" status
project = MYAPP AND issuetype = Story AND status = "In Testing"

-- 6. Bugs reopened this sprint (regression!)
project = MYAPP AND issuetype = Bug AND status = Reopened AND sprint in openSprints()

-- 7. Issues updated today (recent activity)
project = MYAPP AND updated >= startOfDay() ORDER BY updated DESC

-- 8. Stories without test cases linked
project = MYAPP AND issuetype = Story AND "Test Cases" is EMPTY AND sprint in openSprints()

-- 9. Bugs from last sprint that weren't fixed (carried forward)
project = MYAPP AND issuetype = Bug AND sprint in closedSprints() 
AND status != Done ORDER BY priority ASC

-- 10. All blockers
project = MYAPP AND priority = Blocker AND status != Done ORDER BY created ASC

-- Advanced: Bugs filed by me that were rejected
project = MYAPP AND issuetype = Bug AND reporter = currentUser() 
AND resolution = "Won't Fix"

-- Advanced: High priority stories not yet started
project = MYAPP AND issuetype = Story AND priority in (High, Critical) 
AND status = "To Do" AND sprint in openSprints()
```

---

## Q11. What is Zephyr Scale and how do you use it for test management?

**Answer:**
Zephyr Scale (formerly Zephyr for JIRA) is a test management tool that integrates with JIRA. It provides:

- **Test Cases:** Detailed test steps, expected results, linked to stories/requirements
- **Test Cycles:** Group test cases for a specific sprint/release execution
- **Test Executions:** Record pass/fail/blocked results per test case
- **Reports:** Test coverage, pass rate, defect traceability

**Workflow:**
```
Story (JIRA) → Test Case (Zephyr) → Test Cycle (Sprint 12 Regression) →
Execution (Pass/Fail) → Bug (JIRA, linked to test case)
```

**Creating a test case in Zephyr Scale:**
```
Test Case: TC-001
Name: Verify user can login with valid credentials
Objective: Confirm that registered users can authenticate successfully

Preconditions:
- User account exists with email: testuser@example.com
- User is on the login page

Steps:
Step 1: Enter email "testuser@example.com" in the email field
Step 2: Enter password "Password123!" in the password field
Step 3: Click the "Login" button

Expected Result:
- User is redirected to the Dashboard page
- User's name appears in the navigation bar
- Session cookie is set with appropriate expiry

Labels: smoke, authentication, regression
Priority: High
Linked Stories: STORY-101
```

**Integration with automation:** Zephyr Scale has an API that allows automated tests to update execution status programmatically after a CI run.

---

## Q12. How does QA fit in a Scrum team?

**Answer:**
QA is a full member of the cross-functional Scrum team — not a separate department that receives handoffs.

**QA's responsibilities within a sprint:**
```
Week 1 (Sprint Start):
- Attend sprint planning — review AC, flag ambiguities
- Write test cases for stories being developed
- Set up test data and environments
- Review designs/prototypes for testability

Week 2 (Sprint Execution):
- Test stories as they come out of development
- Run automation against new code
- Report bugs immediately (same day)
- Retest fixes
- Support developers with reproduction steps

End of Sprint:
- Certify which stories are Done (meet AC + Definition of Done)
- Contribute to sprint review with test evidence
- Raise process issues in retrospective
```

**QA's influence on team quality:**
- Shift-left: catching defects in requirements, not in testing
- CI/CD: automation runs on every PR, fast feedback to developers
- Three Amigos: QA-BA-Dev align on requirements before coding starts
- Definition of Done: QA ensures DoD includes testing criteria

---

## Q13. What is shift-left testing in Agile?

**Answer:**
Shift-left means moving testing activities earlier (to the left) in the development timeline — from after coding to before and during coding.

**Traditional (Shift-Right) Timeline:**
```
Requirements → Design → Coding → [Testing] → Deploy
                                   ^
                                  QA starts here (too late)
```

**Shift-Left Timeline:**
```
[Requirements Review] → [Design Review] → [Dev + QA concurrent] → [Regression] → Deploy
^QA starts here           ^Test cases written    ^Tests run daily
```

**Shift-left practices:**
1. **AC review in backlog grooming** — QA reads stories and adds missing test conditions before dev starts
2. **Test-first thinking** — QA writes test cases before dev writes code
3. **TDD (Test-Driven Development)** — dev writes unit tests before production code
4. **Three Amigos meetings** — QA, BA, Dev align on expected behaviour together
5. **Static analysis** — linting, SonarQube catch code issues before test
6. **Contract testing** — Pact/OpenAPI validates API contracts early

**Result:** Bugs found in requirements cost $1 to fix. Bugs found in production cost $100+.

---

## Q14. What is the Three Amigos meeting?

**Answer:**
Three Amigos is an informal meeting (30-60 min) where three perspectives come together before development starts on a story:

- **Business Analyst / Product Owner:** "Here's what we want to build and why"
- **Developer:** "Here's how I'm planning to implement it"
- **QA:** "Here are the scenarios I'll test — are they covered?"

**Goal:** Everyone leaves with the same understanding. AC is complete, edge cases are captured, acceptance is pre-agreed.

**Example Three Amigos scenario:**

Story: "As a user, I can filter products by price range"

**BA presents:** Slider from $0 to $10,000. Results update on slider release.

**Dev questions:** "What if the database has products over $10,000?"

**QA questions:**
- "What if min > max?" (invalid range)
- "What if no products match?" (empty state)
- "Does it work with URL sharing?" (can you bookmark a filtered URL?)
- "What about $0 price products?"
- "Mobile behaviour — does the slider work on touch?"

**Outcome:** AC is updated to include empty state handling, min/max validation, and URL parameter behaviour. Dev knows to implement URL parameters. QA knows what to test. No surprises when stories reach QA.

---

## Q15. What is the Definition of Done from QA perspective?

**Answer:**
The Definition of Done (DoD) is a shared checklist that a story must pass before it is called "Done." It prevents partial work from being counted as complete.

**Example DoD for a QA-influenced team:**

```
A story is Done when:
□ All acceptance criteria are met and verified by QA
□ Code has been reviewed by at least 1 developer
□ Unit tests written and passing
□ Integration tests updated/added
□ QA automation tests added or updated (if story type warrants it)
□ No Critical or High bugs open against this story
□ Test execution results documented in Zephyr Scale
□ Smoke suite passes on CI/CD pipeline (green build)
□ Deployed to staging environment
□ QA has tested on staging (not just local dev environment)
□ Acceptance tested by PO (demo or self-service)
□ No outstanding comments in code review
□ Release notes updated (if user-facing change)
□ Performance benchmarks within acceptable range (for perf-sensitive changes)
```

**QA's role in DoD:** QA should define and own the testing-related criteria. If the team tries to ship without hitting DoD criteria, QA has the authority to say "this story is not Done."

---

## Q16. How do you handle testing when dev finishes late in the sprint?

**Answer:**
This is one of the most common practical QA interview questions.

**What I do:**

1. **Communicate early** — I track story progress daily in standup. If a story is due Tuesday and dev hasn't started testing deployment by Monday, I flag it: "If STORY-145 doesn't reach QA by Wednesday EOD, I won't have time to test it this sprint."

2. **Risk-based prioritisation** — If multiple stories arrive late, I test the highest-risk or highest-value ones first. I communicate clearly which stories I tested and which I didn't have time for.

3. **Scope negotiation** — Work with the PO to decide: push to next sprint, reduce scope, or accept known risks. I document the decision.

4. **Don't skip** — Never say "we'll test it in production." That's not a QA strategy.

5. **Root cause in retro** — Raise the pattern in retrospective: "Three sprints in a row, 40% of stories reached QA in the last 2 days. We need to commit to smaller stories or adjust our sprint structure."

**What I never do:** Rush testing, approve a story I haven't tested, or accept pressure to mark something Done when it isn't.

---

## Q17. How do you measure QA effectiveness in Agile?

**Answer:**

| Metric | What It Measures | Good Sign |
|---|---|---|
| Defect detection rate | % of bugs found by QA (vs production) | > 90% found in QA |
| Escaped defects | Bugs found by users in production | Trending down |
| Test coverage | % of stories covered by automation | Increasing |
| Bug closure rate | Bugs opened vs closed per sprint | Close > Open |
| Regression pass rate | % of regression tests green in CI | > 95% |
| Test execution time | How long the suite takes to run | Stable or decreasing |
| Story cycle time in QA | Days from "QA Ready" to "Done" | Decreasing |
| Defect removal efficiency | Bugs found before vs after release | Higher = better |

**In Agile, QA effectiveness is not just bug counts.** A QA who finds 20 bugs in testing is doing their job. A QA who prevents 20 bugs by doing thorough AC review and Three Amigos is doing better.

**At Qoria Lanka:** We track regression suite pass rate in GitHub Actions — aiming for 95%+ green builds before merge to main. Flaky tests are tracked and addressed in the current sprint.

---

## Q18. What is a bug escape?

**Answer:**
A bug escape is a defect that was not caught during the QA phase and reached production (was discovered by end users).

**Why bug escapes happen:**
- Insufficient test coverage (edge case not considered)
- Test environment differs from production
- Manual testing missed the scenario
- Automation suite didn't cover that path
- Story AC was incomplete
- Environment-specific issue (different browser, timezone, locale)
- Integration with third-party service not mocked correctly in testing

**QA response to a bug escape:**
1. **Fix first** — support the hotfix process
2. **Root cause analysis** — understand how it was missed
3. **Prevent recurrence:**
   - Add the missed test case to the automation suite
   - Update AC template to include this type of edge case
   - Add a regression check for this specific flow
4. **No-blame culture** — the goal is system improvement, not punishment

**Sample answer for interview:**
"In one sprint at Cerexio, a bug escaped to production where the date filter was returning results in the wrong timezone. I had tested it with local data, but production ran in UTC. After fixing it, I added a specific timezone test case to our regression suite and updated our DoD to include: 'date-related features must be tested in both local and UTC timezone.' No further escapes on that feature."

---

## Q19. How do you write acceptance criteria? What makes AC good or bad?

**Answer:**

**Bad AC:**
```
"The search feature should work correctly."
```
- Not testable — what does "correctly" mean?
- No specific conditions
- No edge cases

**Good AC — use Gherkin (Given/When/Then):**
```
Story: User can search for products

AC 1: Basic search
Given I am on the products page
When I type "laptop" in the search bar
Then the results list shows only products whose name or description contains "laptop"
And the results count shows the correct number of matches

AC 2: No results
Given I am on the products page
When I type "xyznotaproduct123" in the search bar
Then the results list is empty
And a message "No products found for 'xyznotaproduct123'" is displayed

AC 3: Empty search
Given I am on the products page
When I clear the search bar
Then all active products are displayed

AC 4: Special characters
Given I am on the products page
When I type "café" in the search bar
Then products with accent characters in their name are returned correctly

AC 5: Performance
Given I am on the products page
When I type any search term
Then results appear within 2 seconds
```

**QA's role in AC:** QA should review every AC in backlog grooming and add missing negative cases, edge cases, and non-functional requirements.

---

## Q20. What is velocity and how does it affect QA planning?

**Answer:**
Velocity is the average number of story points a team completes per sprint. Calculated over 3-5 sprints for reliability.

```
Sprint 10: 42 points
Sprint 11: 38 points
Sprint 12: 45 points
Average velocity: (42+38+45) / 3 = 41.6 ≈ 42 points
```

**Sprint planning:** If velocity is 42, don't commit to 60 points. Committing to more = incomplete work at sprint end.

**QA impact:**
- Higher velocity = more stories = more testing per sprint
- QA must account for testing effort in story estimation
- If QA is the bottleneck, velocity suffers — automation is the solution
- Stories with complex testing (many edge cases, integration points) should get higher estimates to reflect QA effort

**Velocity anti-patterns:**
- Counting stories as Done before QA has tested them → inflated, false velocity
- Including bugs in story point counts → obscures team capacity
- Pressure to increase velocity by cutting QA → leads to production bugs

---

## Q21. Explain the JIRA issue types you use day-to-day at Cerexio.

**Answer:**
"At Cerexio, our JIRA setup had:

**Stories** for user-facing features — I'd link my Zephyr test cases to each story and update test execution results there. Every story in our sprint had at least one test case linked before dev started.

**Bugs** for defects — I filed bugs with full reproduction steps, screenshots, and severity classification. I used the bug template our team agreed on in a previous retro. Priority was discussed with the tech lead — I'd propose Priority, they'd confirm.

**Tasks** for non-feature work — setting up test environments, writing automation scripts, updating test data scripts. These got story points in our sprint.

**Subtasks** I'd create under stories specifically for: 'Write automation tests for STORY-123'. This made QA effort visible in sprint planning rather than hidden inside a story.

The Scrum board gave the team visibility — I could see at a glance when stories were moving to 'In Testing' and could plan my day accordingly."

---

## Q22. What is a sprint burndown chart and what does it tell QA?

**Answer:**
A burndown chart shows remaining work (story points) vs time through the sprint.

```
Points
Remaining
40 | ●
35 |  ●
30 |   ●
25 |    ●
20 |     ●  ←Ideal line
15 |      ●
10 |
 5 |
 0 |─────────────────────→ Sprint Days
   1  2  3  4  5  6  7  8  9  10
```

**What it tells QA:**
- **Flat line** → work is not being completed. Stories are stuck in Dev or QA pipeline.
- **Sharp drop at end** → team is marking things done at the last minute — potential for incomplete testing.
- **Drops off above ideal line** → team is behind, testing time will be compressed.
- **Below ideal line** → team is ahead — good, QA has breathing room.

**QA action:** If you see the chart is behind mid-sprint, proactively flag to the Scrum Master that upcoming test time may be insufficient.

---

## Q23. How do you prioritise your testing work within a sprint?

**Answer:**
I use a risk-based prioritisation approach:

**Priority order:**
1. **Smoke tests first** — ensure the environment is stable before deep testing
2. **Critical path features** — core business flows (login, checkout, main feature of the sprint)
3. **High severity risks** — payment, authentication, data integrity
4. **New stories** — test new features in sprint
5. **Regression** — verify existing features didn't break
6. **Edge cases / negative tests** — only after happy path is confirmed

**Practical sprint testing order:**
```
Day 1: Smoke test, set up test data, review stories
Day 2-6: Test stories as they arrive from dev (don't wait for all to be ready)
Day 7-8: Run full regression suite, retest fixed bugs
Day 9: Buffer for last-minute fixes, sign-off
Day 10: Sprint review preparation, retro
```

**Key principle:** Don't wait for all development to finish before starting testing. Test incrementally as stories are ready.

---

## Q24. What's the difference between a test case and a test scenario?

**Answer:**

**Test Scenario:** High-level description of what to test — what is the feature or behaviour being validated.
```
Scenario: User can reset password
```

**Test Case:** Specific, detailed, step-by-step procedure with input data, steps, and expected result.
```
Test Case TC-001: Verify password reset with valid email
Precondition: User registered with email@test.com

Steps:
1. Click "Forgot Password" on login page
2. Enter "email@test.com"
3. Click "Send Reset Link"
4. Check inbox at email@test.com
5. Click reset link in email
6. Enter new password "NewPass123!"
7. Confirm password "NewPass123!"
8. Click "Reset Password"
9. Try to login with "NewPass123!"

Expected: Login successful with new password
Old password "OldPass123!" should no longer work
```

A single test scenario can have multiple test cases:
- TC-001: Valid email
- TC-002: Invalid email (not registered)
- TC-003: Expired reset link (>24 hours)
- TC-004: Password doesn't meet complexity rules
- TC-005: Passwords don't match

---

## Q25. How do you raise the quality bar in a Scrum team?

**Answer:**
Practical things I have done or would do:

1. **Write test cases before dev starts** — review AC, add edge cases as subtasks
2. **Pair with developers** — test at the developer's desk as soon as a feature is ready, give instant feedback
3. **Make automation fast** — a 5-minute smoke suite on every PR is more valuable than a 2-hour suite that nobody wants to wait for
4. **Show the value of automation** — "This regression suite caught 3 bugs this sprint before they reached staging." Make it visible.
5. **Update Definition of Done** — propose testing criteria in retro, get team agreement
6. **Three Amigos** — push for this in refinement, ensures AC is testable before sprint starts
7. **Share test cases** — developers know what will be tested → they build with quality in mind
8. **No blame on escapes** — treat production bugs as system failures, not individual failures → team stays open and honest
9. **Metrics visibility** — put the regression pass rate on the team dashboard
10. **Knowledge sharing** — run short sessions on SQL for QA, API testing basics for new team members

---

## Q26. What questions do you ask in sprint planning as a QA?

**Answer:**
For every story:
```
About requirements:
- "What does 'success' look like? How does the user know they succeeded?"
- "What happens if the user is not logged in when they hit this page?"
- "What happens with an empty state (no data)?"
- "What's the maximum allowed input? What happens if exceeded?"
- "Does this work on mobile? Which browsers?"

About integration:
- "Does this touch any existing features? What regression risk?"
- "Is there a third-party API involved? Do we have a test account/sandbox?"
- "Are there database changes? Do we need a data migration test?"

About testability:
- "How will automation access this feature? Is there a test hook or stable ID?"
- "Can we reset this feature's state between test runs?"
- "Is there a way to trigger error states (e.g., network failure, timeout)?"
```

---

## Q27. Describe your daily workflow as a QA in a Scrum team.

**Answer:**
"My typical day in a 2-week sprint:

**Morning (9:00-9:15):** Daily standup — I report what I tested yesterday, what I'm testing today, and any blockers. If something is stuck waiting for a dev fix, I say so.

**9:15-9:30:** Check the CI pipeline results from overnight. If the regression suite failed, I investigate before starting new work — was it a real regression or a flaky test?

**9:30-12:00:** Active testing of in-progress stories. I pick up stories that have moved to 'QA Ready' on the JIRA board. I run manual tests first (exploratory + scripted), then verify automation covers the scenarios.

**Afternoon:** File any bugs found with full reproduction steps in JIRA. Retest any bugs marked fixed by developers. Update Zephyr Scale with test execution results.

**Late afternoon:** Write automation for stories I've finished testing manually. Review upcoming stories from the backlog for the next sprint.

**End of sprint week:** Run the full regression suite. Prepare sign-off summary: X stories tested, Y passed, Z bugs found, W open bugs, overall recommendation for sprint review."

---

## Q28. What is a test plan and when do you write one?

**Answer:**
A test plan is a document that describes the scope, approach, resources, and schedule for testing activities.

**When to write:** For major releases, new product launches, or compliance-required testing. In Agile, test plans are lightweight — often a one-page living document rather than a 50-page static document.

**Contents of a lightweight Agile test plan:**
```
Sprint 15 Test Plan
Scope: User Profile Feature (STORY-141 to STORY-148)
Out of scope: Payment integration (next sprint)

Test types:
- Functional: manual + automated (TestNG + RestAssured)
- API: RestAssured against /api/profile/* endpoints
- UI: Playwright for profile page flows
- Regression: existing auth suite to verify no regressions

Environments: Staging (https://staging.myapp.com)
Test data: test_user_001 to test_user_010 in staging DB

Entry criteria: Stories deployed to staging, smoke suite green
Exit criteria: All AC verified, no open Critical/High bugs, regression suite > 95% pass

Test execution: Days 6-9 of sprint
Automation target: 80% of AC covered by end of sprint

Risks:
- Third-party avatar upload service may be slow in staging
- Profile picture resize functionality not yet implemented (STORY-147 deferred)
```

---

## Q29. How does Agile testing differ at Qoria Lanka vs Cerexio?

**Answer (CV-tailored):**
"At Cerexio, I worked in a more traditional Agile setup — 2-week sprints, JIRA for tracking, Zephyr for test management, manual testing for most stories with some Selenium automation. The team was co-located, and ceremonies were face-to-face. QA was a clear role — stories flowed from dev to QA to Done.

At Qoria Lanka, automation is more central to the process. We use Playwright for E2E tests and those run on every PR in GitHub Actions — tests are the first gate before code review. The pipeline is the QA process, not just a supplement to it. Stories are considered not mergeable until the pipeline is green. I maintain the YAML workflows and write tests alongside feature development, not after. The team is remote-first, so async communication is key — JIRA comments, PR reviews, and detailed test reports in GitHub Actions are how we communicate quality."

---

## Q30. Final question: What makes a great QA engineer in an Agile team?

**Answer:**
"A great QA in Agile is someone who prevents bugs as much as they catch them.

Three qualities I focus on:

**1. Shift-left mindset.** I'm not waiting for stories to be handed to me — I'm in grooming, in planning, in Three Amigos. If the AC is ambiguous, I catch it before a developer writes a line of code, not after two days of testing.

**2. Automation as a first-class skill.** A manual test I run once has limited value. An automated test I run 500 times in CI has compounding value. I treat test code with the same care as production code — proper structure, Page Object Model, no hardcoded data, meaningful assertions.

**3. Being the quality conscience of the team, not a gatekeeper.** I'm not here to block releases — I'm here to give the team accurate information about quality so they can make good decisions. If we ship with known risk, I ensure that risk is documented and understood, not hidden.

At Cerexio, this meant pushing for Three Amigos meetings that we hadn't been doing. At Qoria Lanka, it means keeping the GitHub Actions pipeline reliable — because if developers stop trusting the pipeline, they bypass it. A QA who maintains trustworthy automation is a force multiplier for the entire team."

---

*End of Part 11 — Agile & JIRA*
*For the complete interview series: Parts 1-7 in this guides/interview/ directory.*
