# JIRA & Defect Management — Complete Guide | Bug Reporting + Test Management

> Senior QA Interview Reference — JIRA workflow, bug reporting, severity vs priority, JQL, test management plugins, and 10 interview Q&As.

---

## Table of Contents

1. [JIRA Basics for QA](#section-1--jira-basics-for-qa)
2. [Writing a Perfect Bug Report](#section-2--writing-a-perfect-bug-report)
3. [Severity vs Priority](#section-3--severity-vs-priority)
4. [Bug Lifecycle in JIRA](#section-4--bug-lifecycle-in-jira)
5. [JIRA for Sprint / Agile](#section-5--jira-for-sprint--agile)
6. [JQL — JIRA Query Language](#section-6--jql--jira-query-language)
7. [Test Management in JIRA](#section-7--test-management-in-jira)
8. [Interview Q&A](#section-8--interview-qa)

---

## SECTION 1 — JIRA Basics for QA

### What is JIRA?

JIRA (by Atlassian) is the industry-standard project management and issue-tracking tool used by software teams. For QA engineers, JIRA is the central hub where:

- Bugs are logged and tracked from discovery to closure
- User stories contain acceptance criteria that define what to test
- Test evidence (screenshots, logs, results) is attached to issues
- Sprint health is monitored via board views and reports
- Release readiness is assessed through open bug counts and fix versions

### Core JIRA Concepts

| Concept | Description | QA Usage |
|---|---|---|
| Project | Container for all work (e.g., "MYAPP") | Scope your bug search and filters |
| Board | Visual representation of issues (Kanban or Scrum) | Track sprint progress and move cards |
| Backlog | Unscheduled issues waiting for a sprint | Groom stories for testability before sprint |
| Sprint | Time-boxed iteration (usually 2 weeks) | QA tests within the sprint window |
| Epic | Large body of work spanning multiple sprints | Understand feature context for testing |
| Story | User-facing requirement with acceptance criteria | Source of test cases |
| Bug | Defect found during testing or production | Primary QA artifact |
| Sub-task | Child of a Story or Task | QA creates these for test activities |

### Issue Types and When to Use Each

**Epic**
- Represents a large feature or theme (e.g., "User Authentication Module")
- Contains many stories underneath
- QA uses Epics to scope regression tests across a feature

**Story (User Story)**
- Describes a feature from the end-user's perspective
- Format: "As a [role], I want [feature] so that [benefit]"
- QA derives test cases directly from the Acceptance Criteria (AC)
- Example: "As a registered user, I want to reset my password so I can regain access"

**Task**
- Work that is not a user story or bug (e.g., "Set up test environment", "Update test data")
- QA creates Tasks for non-testing activities like environment configuration

**Sub-task**
- Child of a Story or Task
- QA creates sub-tasks like "Write test cases for login story" or "Execute regression suite"
- Useful for breaking testing effort into trackable chunks

**Bug**
- A defect found in the software that deviates from expected behaviour
- Always linked to the Story or Epic it relates to
- Carries Severity, Priority, Environment, and Steps to Reproduce
- Key JIRA issue type owned primarily by QA

### JIRA Workflow — How QA Gates Each Status

```
To Do → In Progress → In Review → Done
```

| Status | Who Acts | QA Gate |
|---|---|---|
| To Do | Backlog groomed, sprint planned | QA reviews AC before sprint starts |
| In Progress | Developer working | QA prepares test cases, test data |
| In Review | Code review / PR open | QA may review test coverage in PR |
| Ready for QA | Dev marks complete | QA picks up for testing |
| In QA | QA executing tests | Active testing phase |
| Done | All verified, no blockers | QA approves by moving to Done |

QA is the gatekeeper of "Done". A story is only Done when:
1. All acceptance criteria pass
2. No open blocker or critical bugs linked to the story
3. Regression tests have been executed
4. Test evidence has been attached

### Kanban vs Scrum Board

| Aspect | Kanban | Scrum |
|---|---|---|
| Time-boxing | No fixed sprints — continuous flow | Fixed sprints (1–4 weeks) |
| Planning | Work pulled as capacity allows | Sprint planning every sprint |
| WIP limits | Explicit WIP limits per column | Implicit through sprint scope |
| Releases | Continuous / on demand | End of sprint or after |
| QA role | Pull bugs/stories when ready | Test within sprint commitment |
| Best for | Support teams, bug-fix teams | Feature development teams |
| Backlog | Prioritised queue | Sprint backlog + product backlog |

**QA in Kanban:** Focus on throughput — ensure cards keep moving, flag bottlenecks, test as stories arrive.

**QA in Scrum:** Focus on sprint commitment — test all stories before sprint end, report bugs early enough for devs to fix within sprint.

---

## SECTION 2 — Writing a Perfect Bug Report

A great bug report allows any developer (including one who has never seen the feature) to reproduce, understand, and fix the defect without asking a single question.

### Every Field Explained

| Field | Purpose | QA Guidance |
|---|---|---|
| **Summary** | One-line description of the bug | Use the formula below — concise, specific |
| **Description** | Full context of the bug | Background, business impact, any notes |
| **Steps to Reproduce** | Exact numbered steps | Must be reproducible by anyone |
| **Expected Result** | What should happen per AC or spec | Quote the AC or requirement |
| **Actual Result** | What actually happened | Be factual, not opinionated |
| **Environment** | Where the bug was found | OS, browser, version, test env |
| **Severity** | How bad the impact is | Critical / High / Medium / Low |
| **Priority** | How urgently it must be fixed | P1 / P2 / P3 / P4 |
| **Assignee** | Who is fixing it | Initially unassigned; Dev Lead assigns |
| **Reporter** | Who found it | Auto-set to logged-in QA |
| **Labels** | Categorisation tags | regression, smoke, ui, api, performance |
| **Components** | System area affected | Login, Checkout, API, Database |
| **Fix Version** | Which release should include the fix | Set by Dev/PM during triage |
| **Attachments** | Screenshots, logs, HAR files | Always attach evidence — essential |
| **Linked Issues** | Related story, duplicate, blocker | Link to the parent Story |
| **Sprint** | Which sprint to fix in | Set during triage |

### Summary Formula

```
[Area] [What happened] [Condition/Context]
```

**Good summary examples:**

```
Login: User redirected to 404 page when clicking 'Forgot Password' on mobile Safari
Checkout: Order total does not include applied discount code on payment summary screen
User Profile: Avatar upload fails silently when file size exceeds 2MB on Chrome 124
Search API: Returns 500 error when query parameter contains special characters (%, &)
Registration: Duplicate account created when 'Submit' button double-clicked rapidly
```

**Bad summary examples (avoid these):**

```
Login is broken                        ← too vague
Bug in checkout page                   ← no detail
Something wrong with the API           ← useless
Fix the profile page issue             ← not a description of the defect
```

### Steps to Reproduce Format

Rules:
- Numbered list — never a wall of text
- Each step is a single action
- Start from a clean, known state
- Include exact test data used
- Specify the exact UI element or endpoint

**Template:**
```
1. [Starting condition — environment, user state]
2. [Navigate to / open]
3. [Perform exact action with exact data]
4. [Observe what happens]
```

### Expected vs Actual — Be Precise

| Wrong | Right |
|---|---|
| "It should work correctly" | "User is redirected to the dashboard (per AC #3)" |
| "The page breaks" | "HTTP 500 Internal Server Error is returned" |
| "The button doesn't do anything" | "Clicking 'Submit' produces no visible response; no network request is made (confirmed via DevTools)" |
| "Wrong data shows" | "Order total displayed: $120.00. Expected: $96.00 (20% discount applied to $120.00 = $24.00 off)" |

---

### Bug Report Example 1 — UI Bug

```
SUMMARY:
Login: User redirected to 404 page when clicking 'Forgot Password' on mobile Safari

DESCRIPTION:
When a user on mobile Safari iOS 17 taps the 'Forgot Password' link on the Login
page, they are redirected to a broken 404 page instead of the password reset form.
This prevents mobile Safari users from recovering their accounts. Impact: high —
confirmed on 3 different iPhones with iOS 17.

STEPS TO REPRODUCE:
1. Open Safari browser on iPhone (iOS 17.x)
2. Navigate to https://app.myapp.com/login
3. Enter any email address in the email field
4. Tap the 'Forgot Password?' link below the password field
5. Observe the resulting page

EXPECTED RESULT:
User is navigated to the Password Reset page at /forgot-password showing
"Enter your email to receive a reset link" (per Acceptance Criteria #2 of MYAPP-145)

ACTUAL RESULT:
User is redirected to a 404 "Page Not Found" page.
URL in address bar: https://app.myapp.com/forgot-passord (note typo — missing 'w')

ENVIRONMENT:
- Device: iPhone 14 Pro, iPhone 13 Mini (both reproduced)
- OS: iOS 17.2
- Browser: Safari 17.x (default)
- App Version: 2.4.1
- Test Environment: QA (https://qa.myapp.com)

SEVERITY: High
PRIORITY: P1
LABELS: mobile, safari, regression, login
COMPONENTS: Authentication, Frontend
FIX VERSION: 2.4.2
LINKED ISSUES: MYAPP-145 (parent story)
ATTACHMENTS: screenshot_404_mobile.png, screen_recording.mp4
```

---

### Bug Report Example 2 — API Bug

```
SUMMARY:
Search API: Returns HTTP 500 when query parameter contains special characters (%, &)

DESCRIPTION:
The GET /api/v1/products/search endpoint returns a 500 Internal Server Error when
the 'q' query parameter includes URL-encoded special characters such as % or &.
This is a data validation issue — input is not sanitised before being passed to
the database layer. Affects any user searching for terms like "50% off" or
"salt & pepper".

STEPS TO REPRODUCE:
1. Authenticate and obtain a valid Bearer token (use test account: testuser@myapp.com)
2. Send the following GET request:
   GET https://qa-api.myapp.com/api/v1/products/search?q=50%25+off
   Headers: Authorization: Bearer <token>
3. Observe the HTTP response code and body
4. Repeat with: GET .../search?q=salt+%26+pepper
5. Observe that both return 500

EXPECTED RESULT:
HTTP 200 OK with a JSON array of matching products, or HTTP 200 with an empty
array [] if no products match. Per API contract in Confluence (API Spec v1.3 p.12)

ACTUAL RESULT:
HTTP 500 Internal Server Error
Response body:
{
  "error": "Internal Server Error",
  "message": "Unexpected token '%' in SQL near '...'"
}
Note: SQL fragment visible in error suggests possible SQL injection vulnerability.

ENVIRONMENT:
- API Base URL: https://qa-api.myapp.com
- API Version: v1
- Auth: Bearer token (OAuth2)
- Tool used for testing: Postman v10, Rest Assured (automated test MYAPP-AT-212)
- Test Environment: QA

SEVERITY: Critical (potential SQL injection)
PRIORITY: P1
LABELS: api, security, input-validation, regression
COMPONENTS: Search API, Backend
FIX VERSION: 2.4.2
LINKED ISSUES: MYAPP-201 (parent story "Product Search"), MYAPP-AT-212 (failing automation test)
ATTACHMENTS: postman_500_response.png, postman_collection_search_bug.json
```

---

### Bug Report Example 3 — Data Bug

```
SUMMARY:
Reporting Dashboard: Monthly revenue total incorrect — excludes refunded orders from calculation

DESCRIPTION:
The Monthly Revenue figure shown on the Admin Reporting Dashboard appears to exclude
orders that were subsequently refunded, resulting in an inflated revenue figure.
Finance team flagged a discrepancy of $14,200 between the JIRA dashboard and the
accounting system for March 2024. This is a data integrity bug affecting business
reporting and financial accuracy.

STEPS TO REPRODUCE:
1. Log in as an Admin user (admin@myapp.com / TestAdmin123!)
2. Navigate to Admin > Reports > Monthly Revenue
3. Select month: March 2024
4. Note the Total Revenue figure displayed (actual: $142,500)
5. Compare against known data:
   - Total orders placed in March: $156,700
   - Total refunds issued in March: $14,200
   - Expected net revenue: $142,500 — MATCHES display
6. Now check the formula expectation: Revenue should be GROSS (before refunds)
   per Finance requirements doc (Confluence: Finance Reporting Spec v2.1)
   Expected: $156,700

EXPECTED RESULT:
Monthly Revenue = Gross Revenue (sum of all orders placed in the month, regardless of
refund status) = $156,700, per Finance Reporting Spec v2.1 Section 3.2

ACTUAL RESULT:
Monthly Revenue displayed: $142,500
This equals Net Revenue (after refunds), not Gross Revenue as specified.
Difference: $14,200 (the refunded amount is being excluded from the total)

ENVIRONMENT:
- URL: https://qa.myapp.com/admin/reports/monthly-revenue
- Role: Admin
- Test Data: March 2024 dataset in QA environment
- Database: PostgreSQL (QA instance)
- Browser: Chrome 124 (also reproduced on Firefox 125)

SEVERITY: High (incorrect financial data affects business decisions)
PRIORITY: P2 (P1 considered but no customer-facing impact)
LABELS: data-integrity, reporting, finance, regression
COMPONENTS: Admin Dashboard, Reporting Module, Database
FIX VERSION: 2.5.0
LINKED ISSUES: MYAPP-310 (parent story "Revenue Dashboard"), MYAPP-DS-44 (data spec)
ATTACHMENTS: dashboard_screenshot.png, excel_finance_comparison.xlsx, db_query_results.png
```

---

## SECTION 3 — Severity vs Priority

### The Key Distinction

| | Severity | Priority |
|---|---|---|
| **Definition** | How bad is the impact of the bug? | How urgently does it need to be fixed? |
| **Determined by** | QA Engineer (technical assessment) | Business / Product Manager |
| **Based on** | Technical impact — data loss, crashes, blocks users | Business context — launch date, customer contracts, visibility |
| **Scale** | Critical / High / Medium / Low | P1 / P2 / P3 / P4 |
| **Changes?** | Rarely changes after initial assessment | Can change based on release timing |

### Severity Levels Defined

| Level | Definition | Examples |
|---|---|---|
| **Critical** | System crash, data loss, complete feature unavailable, security breach | App crashes on launch, payment fails for all users, data corrupted, SQL injection possible |
| **High** | Major feature broken, significant user impact, no workaround | Login fails for subset of users, checkout process blocked, API returns wrong data |
| **Medium** | Feature partially broken, workaround exists, business flow affected | Search filter not working but manual browse works, report shows wrong date format |
| **Low** | Cosmetic issue, minor inconvenience, minimal user impact | Typo in label, misaligned button, wrong icon colour |

### Priority Levels Defined

| Level | Definition | Response Time |
|---|---|---|
| **P1** | Fix immediately — production on fire or imminent release blocker | Same day / within hours |
| **P2** | Fix in current sprint — significant impact if not resolved soon | Within current sprint |
| **P3** | Fix in next sprint — important but not urgent | Planned in upcoming sprint |
| **P4** | Fix when time allows — low business impact | Backlog, no specific timeline |

### The 4x4 Matrix — All Combinations with Examples

```
                    PRIORITY
                 P1          P2          P3          P4
              ┌───────────┬───────────┬───────────┬───────────┐
CRITICAL      │ App crash  │ Data loss │ Data loss │ (very     │
              │ on payment │ in rarely │ in legacy │  rare)    │
              │ page —     │ used admin│ module no │           │
              │ fix NOW    │ report    │ one uses  │           │
              ├───────────┼───────────┼───────────┼───────────┤
HIGH          │ Login      │ Checkout  │ Export to │ Admin-    │
              │ broken     │ slow —    │ PDF fails │ only      │
              │ day before │ fix this  │ — used    │ feature   │
              │ launch     │ sprint    │ sometimes │ rarely    │
              │            │           │           │ used      │
              ├───────────┼───────────┼───────────┼───────────┤
MEDIUM        │ Wrong logo │ Broken    │ Date      │ Search    │
              │ showing    │ filter on │ format    │ results   │
              │ before big │ main      │ wrong in  │ in wrong  │
              │ marketing  │ search    │ reports   │ order     │
              │ campaign   │           │           │           │
              ├───────────┼───────────┼───────────┼───────────┤
LOW           │ Typo in    │ Misalign- │ Wrong     │ Minor     │
              │ CEO keynote│ ed button │ tooltip   │ style     │
              │ screen     │ on landing│ text      │ issue     │
              │            │ page      │           │           │
              └───────────┴───────────┴───────────┴───────────┘
```

### Classic Conflict Scenarios

**High Severity + Low Priority:**
- App crashes when importing more than 1,000 records — but only 2 internal data analysts ever use this feature, and it is not customer-facing. The crash is real (High Severity) but business can defer the fix (Low Priority / P4).

**Low Severity + High Priority:**
- The company logo on the homepage shows the old 2019 brand mark instead of the new logo — a cosmetic issue (Low Severity) but the rebranding campaign launches tomorrow and the CEO will be on a live demo (High Priority / P1).

**Critical + P1 (most common):**
- Payment processing fails for all users in production — immediate hotfix, all hands on deck.

**Critical + P3 (uncommon but valid):**
- Data corruption occurs in a module being deprecated next quarter — critical in isolation but the entire module is being switched off; the fix is scheduled in the decommission plan.

---

## SECTION 4 — Bug Lifecycle in JIRA

### Full Status Flow

```
                        ┌─────────────┐
                        │     NEW     │  ← QA creates bug
                        └──────┬──────┘
                               │
                        ┌──────▼──────┐
                        │    OPEN     │  ← Triaged, accepted as valid
                        └──────┬──────┘
                               │
                        ┌──────▼──────┐
                        │ IN PROGRESS │  ← Developer working on fix
                        └──────┬──────┘
                               │
                        ┌──────▼──────┐
                        │    FIXED    │  ← Dev marks done, code deployed to QA env
                        └──────┬──────┘
                               │
                   ┌───────────▼───────────┐
                   │   READY FOR RETEST    │  ← QA notified to test fix
                   └───────────┬───────────┘
                               │
                        ┌──────▼──────┐
                        │   RETEST    │  ← QA actively retesting
                        └──────┬──────┘
                      ┌────────┴────────┐
               PASS   │                 │  FAIL
                      ▼                 ▼
               ┌────────────┐    ┌────────────┐
               │   CLOSED   │    │  REOPENED  │ ← Back to Open/In Progress
               └────────────┘    └────────────┘
```

### Additional Status Paths

| Status | Meaning | Who sets it | QA Action |
|---|---|---|---|
| **Rejected** | Developer or PM determines it is not a bug — intended behaviour or invalid | Dev/PM | QA reviews the rejection reason; escalates if disagrees |
| **Duplicate** | Same defect already reported; linked to original | QA/Dev | Close and link to canonical bug; monitor original |
| **Deferred** | Valid bug but not fixing in current release (conscious decision) | PM | Note in release notes; add to regression backlog |
| **Cannot Reproduce** | Dev cannot replicate with provided steps | Dev | QA provides additional context; re-attempts reproduction |
| **Won't Fix** | Valid but business decides not to fix (e.g., deprecated feature) | PM | Document; ensure it does not block release if severity allows |

### QA Responsibilities at Each Stage

**NEW:**
- QA creates the bug with all fields filled
- Attaches screenshots, logs, video recordings
- Links to the parent Story/Epic
- Sets Severity

**OPEN (after triage):**
- QA monitors — ensure it is assigned and in the correct sprint
- Provides additional information if developers ask

**IN PROGRESS:**
- QA prepares retest steps
- Verifies fix version is set
- Checks if related areas need regression testing

**FIXED / READY FOR RETEST:**
- QA is notified (via JIRA notification or Slack)
- QA verifies the build version includes the fix
- QA executes the exact Steps to Reproduce
- QA also checks surrounding areas for regression

**RETEST:**
- Execute original steps
- Verify the fix works in all environments mentioned (if needed)
- Check edge cases related to the fix

**CLOSED:**
- QA confirms fix is verified and adds a comment: "Verified on QA env v2.4.2. Closing."
- Attach passing test evidence

**REOPENED — How and When:**
Only reopen a bug when:
1. The same defect recurs after being marked Closed
2. The fix introduced a regression in the same area
3. The fix is partial — original scenario passes but edge cases still fail

Reopening process:
1. Add a comment explaining exactly what was retested and what failed
2. Update the Steps to Reproduce if the repro path changed
3. Attach new evidence (screenshot/log from the retest)
4. Set status back to "In Progress" or "Open"
5. Notify the developer via comment (@mention) so they are alerted

---

## SECTION 5 — JIRA for Sprint / Agile

### QA's Role in Backlog Grooming

Backlog grooming (refinement) is the session where the team reviews upcoming stories to ensure they are ready for a sprint. QA's responsibilities:

**Flag untestable stories — a story is not ready if:**
- Acceptance Criteria (AC) are missing or vague ("The page should work correctly" — not testable)
- No mock-ups or designs are referenced
- Dependencies on other stories or APIs are unclear
- Test data requirements are undefined
- No clear definition of "Done" exists

**Questions QA asks during grooming:**
- "What is the exact expected behaviour when [edge case]?"
- "Is there an error state we need to test?"
- "What should happen if the API returns no data?"
- "Which browsers/devices must this work on?"
- "Are there performance requirements (e.g., must load in under 2 seconds)?"

### Story Points for Testing Effort

Story points are relative estimates of complexity, not time. QA should advocate for testing effort to be included:

| Testing Effort | Indicator |
|---|---|
| Low (1–2 points) | Simple CRUD, 1–2 ACs, happy path only |
| Medium (3–5 points) | Multiple ACs, edge cases, cross-browser needed |
| High (8–13 points) | Complex business logic, API integration, performance, many ACs |
| Very High (13+) | System integration, security, multi-environment, automation required |

QA should push back if a 13-point story has 1 point allocated for QA — testing effort must be factored into the overall story estimate.

### Sprint Board from QA View

Daily QA activities on the sprint board:
- **Morning:** Review board for stories moved to "Ready for QA" overnight
- **During sprint:** Move cards through QA statuses, add comments with test progress
- **Blocked bugs:** Flag cards with blockers immediately — raise in stand-up
- **Linking evidence:** Add test run results, screenshots, and automation links as comments on stories
- **End of sprint:** Ensure no story is "Done" with open critical/high bugs linked

**Adding comments effectively:**
```
Testing complete — MYAPP-321 verified on QA env v2.4.1 (Chrome 124, Firefox 125, Safari 17).
All 5 ACs pass. Automation test suite updated: MYAPP-AT-188.
Attached: test_results_2024-04-15.html
```

### Creating Sub-tasks for Testing

Within a Story, QA creates sub-tasks to track testing activities:

```
Story: MYAPP-145 — User Password Reset
  ├── Sub-task: MYAPP-146 — Write test cases for password reset (QA)
  ├── Sub-task: MYAPP-147 — Execute test cases — password reset (QA)
  ├── Sub-task: MYAPP-148 — Automate password reset regression tests (QA)
  └── Sub-task: MYAPP-149 — Cross-browser testing — password reset (QA)
```

Sub-tasks keep testing effort visible and trackable without cluttering the Story with QA-only noise.

### Linking Issues

| Link Type | When to Use |
|---|---|
| **is blocked by** | Bug MYAPP-200 cannot be retested until MYAPP-199 is fixed first |
| **blocks** | Story MYAPP-145 is blocked by Bug MYAPP-200 |
| **relates to** | Similar issue in a different area — not the same bug |
| **duplicates / is duplicated by** | Same defect reported twice |
| **is cloned by / cloned from** | Copied issue for a related sprint or environment |

---

## SECTION 6 — JQL — JIRA Query Language

JQL allows QA to search, filter, and create dashboards with precision. All QA engineers should be comfortable writing basic JQL.

### Syntax

```
field operator value [AND/OR field operator value]
```

### Essential JQL Queries for QA

**Find all bugs assigned to me:**
```jql
project = "MYAPP" AND issuetype = Bug AND assignee = currentUser()
```

**Find all bugs in the current sprint ready for retest:**
```jql
project = "MYAPP" AND sprint in openSprints() AND status = "Ready for Retest"
```

**Find all open P1 bugs:**
```jql
project = "MYAPP" AND issuetype = Bug AND priority = P1 AND status != Closed
```

**Find bugs created in the last 7 days:**
```jql
project = "MYAPP" AND created >= -7d AND issuetype = Bug
```

**Find all bugs for a specific release:**
```jql
project = "MYAPP" AND fixVersion = "1.5.0" AND issuetype = Bug
```

**Find all unresolved bugs in a specific component:**
```jql
project = "MYAPP" AND issuetype = Bug AND component = "Authentication" AND resolution = Unresolved
```

**Find bugs updated today (to monitor active fixes):**
```jql
project = "MYAPP" AND issuetype = Bug AND updated >= startOfDay()
```

**Find all stories with no acceptance criteria (custom field example):**
```jql
project = "MYAPP" AND issuetype = Story AND "Acceptance Criteria" is EMPTY
```

**Find all blockers in the current sprint:**
```jql
project = "MYAPP" AND sprint in openSprints() AND priority = Blocker AND status != Done
```

**Find bugs reported by me that are still open:**
```jql
project = "MYAPP" AND issuetype = Bug AND reporter = currentUser() AND status != Closed
```

**Find duplicate bugs (for cleanup):**
```jql
project = "MYAPP" AND issuetype = Bug AND status = "Duplicate"
```

**Find bugs that are overdue (created more than 14 days ago and still open):**
```jql
project = "MYAPP" AND issuetype = Bug AND created <= -14d AND status not in (Closed, Done, "Won't Fix")
```

### JQL Operators Reference

| Operator | Meaning | Example |
|---|---|---|
| `=` | Equals | `status = "In Progress"` |
| `!=` | Not equals | `status != Closed` |
| `in` | In a list | `priority in (P1, P2)` |
| `not in` | Not in a list | `status not in (Closed, Done)` |
| `is EMPTY` | Field has no value | `assignee is EMPTY` |
| `is not EMPTY` | Field has a value | `fixVersion is not EMPTY` |
| `>=` / `<=` | Date comparison | `created >= -7d` |
| `~` | Contains (text search) | `summary ~ "login"` |
| `AND` / `OR` | Combine conditions | `status = Open AND priority = P1` |

### Saving Filters and Dashboards

- Save frequently used JQL as Filters (My Filters section)
- Add saved filters as Gadgets on your JIRA Dashboard (e.g., Bug Count by Status, Open P1s)
- Share filters with the team for consistent visibility

---

## SECTION 7 — Test Management in JIRA

### Why Test Management in JIRA?

Keeping test cases alongside JIRA issues means:
- Full traceability: Story → Test Case → Test Execution → Bug
- Coverage reports showing which stories have test cases
- Test results linked directly to sprints and releases

### Zephyr Scale (formerly Zephyr for JIRA)

Zephyr Scale is the most widely used JIRA test management add-on.

**Key concepts:**

| Concept | Description |
|---|---|
| **Test Case** | A reusable test with steps, expected results, and status |
| **Test Cycle** | A collection of test cases executed together for a sprint or release |
| **Test Execution** | An instance of executing a test case within a cycle — records Pass/Fail/Blocked |
| **Test Plan** | High-level grouping of test cycles across releases |
| **Folder** | Organises test cases by feature or component |

**Creating test cases linked to stories:**
1. Open the Story in JIRA (e.g., MYAPP-145)
2. In the Zephyr Scale panel, click "Create Test Case"
3. Link the test case to the Story (traceability established)
4. Add test steps with Expected Results
5. Assign the test case to a Test Cycle

**Test Cycle for a Sprint:**
```
Sprint 24 Test Cycle
├── Login Feature Tests (linked to MYAPP-145, MYAPP-146)
│   ├── TC-001: Valid login — happy path [PASS]
│   ├── TC-002: Invalid password — error message shown [PASS]
│   ├── TC-003: Forgot password link — mobile Safari [FAIL → Bug MYAPP-200]
│   └── TC-004: Session timeout after 30 min [PASS]
├── Checkout Feature Tests (linked to MYAPP-155)
│   ├── TC-010: Add item to cart [PASS]
│   └── TC-011: Apply discount code [BLOCKED]
```

**Test Execution results:**

| Status | Meaning |
|---|---|
| Pass | Test executed, result matches expected |
| Fail | Test executed, result does not match expected — log a bug |
| Blocked | Cannot execute — dependency, environment, or data issue |
| Not Executed | Test not yet run |
| In Progress | Currently executing |

**Coverage reports:**
- Zephyr Scale shows coverage % per Story (how many ACs have test cases)
- Traceability matrix: Story → Test Cases → Bugs
- Execution reports: Pass rate per sprint/cycle

### Xray (Alternative Plugin)

Xray is a popular alternative to Zephyr Scale with similar functionality:
- Test issues are JIRA issue types (Test, Test Plan, Test Execution, Test Set)
- Native Cucumber/BDD integration — feature files imported directly as test cases
- CI/CD integration with Jenkins, GitHub Actions
- Similar traceability and reporting capabilities

**Choosing between them:**
| Feature | Zephyr Scale | Xray |
|---|---|---|
| BDD/Gherkin support | Good | Excellent (native) |
| JIRA native feel | Good | Very native (issue types) |
| Reporting | Strong | Strong |
| Automation upload | Yes | Yes |
| Learning curve | Moderate | Moderate |

### Test Case to Story Traceability

Ideal traceability chain:
```
Epic: User Authentication (MYAPP-E1)
  └── Story: Password Reset (MYAPP-145)
        └── AC #1: User receives reset email within 2 minutes
        └── AC #2: Reset link expires after 24 hours
              └── Test Case: TC-003 — Reset link expiry validation
                    └── Test Execution: Sprint 24 Cycle — FAIL
                          └── Bug: MYAPP-200 — Reset link does not expire
```

This chain gives full visibility from business requirement to defect.

---

## SECTION 8 — Interview Q&A

### Q1. How do you write a good bug report?

A good bug report has everything the developer needs to find, understand, and fix the defect without asking a single follow-up question.

Key elements I always include:
- A precise Summary using the formula: [Area] [What happened] [Condition/Context]
- Numbered Steps to Reproduce starting from a known clean state, with exact data used
- Expected Result quoting the Acceptance Criteria or spec — not just "it should work"
- Actual Result describing exactly what happened — HTTP status codes, error messages, data values
- Environment: OS, browser, version, test environment URL
- Severity based on business impact
- Screenshots or screen recordings — visual evidence dramatically reduces back-and-forth
- Links to the parent Story and related issues

The goal is: any developer should be able to reproduce the bug independently within 5 minutes of reading the report.

---

### Q2. What is the difference between severity and priority?

Severity is how bad the bug is technically — its impact on the system or user. This is set by QA based on whether the bug causes data loss, crashes, or blocks core functionality.

Priority is how urgently the business needs it fixed — this is driven by business context like upcoming releases, customer impact, or SLA commitments. This is typically set by the Product Manager.

They can differ significantly. A crash in a module used by 0.1% of users is Critical severity but might be Low priority if that module is being deprecated next month. Conversely, a logo mismatch is Low severity but could be P1 priority if the product launches tomorrow.

---

### Q3. A developer marks your bug as "not a bug" — what do you do?

First, I read the rejection reason carefully — sometimes the developer has context I was missing (e.g., it was intentional behaviour or I tested against an outdated spec).

If I still believe it is a valid defect:
1. I reference the exact Acceptance Criteria or requirement that the behaviour contradicts
2. I share a screen recording or additional evidence to eliminate any ambiguity
3. I discuss it with the developer directly — often it is a miscommunication
4. If there is still disagreement, I escalate to the Product Manager or Tech Lead to make the call

I never close a bug I believe is valid just to avoid conflict. The QA role is to advocate for quality based on specifications.

---

### Q4. How do you decide when a bug is a blocker vs minor?

A blocker (Critical/P1) is a bug that either:
- Completely prevents a core user flow (cannot login, cannot checkout, data is corrupted)
- Poses a security risk (authentication bypass, data exposure)
- Makes the entire application unavailable
- Violates regulatory or compliance requirements

A minor bug is one where:
- The core functionality still works — the issue is cosmetic or edge-case
- A workaround exists
- The affected area has low user traffic
- The impact is display-only (wrong label, misaligned element)

I use the Severity × User Impact matrix: the more users affected and the more critical the disrupted flow, the higher the blocker classification.

---

### Q5. How does QA use JIRA during a sprint?

At sprint start: Review all stories for clear Acceptance Criteria during backlog grooming. Flag any stories that lack testable ACs and block them from being sprint-ready.

During the sprint: As stories move to "Ready for QA", I pick them up, execute tests, and log bugs against the story. I update the story card with test progress comments and move it through QA statuses.

Daily stand-up: I flag any stories blocked by critical bugs, environment issues, or missing test data — sprint board is the communication tool.

End of sprint: I verify all bugs linked to sprint stories are fixed and closed. A story only gets "Done" status when all ACs pass and no open critical/high bugs remain.

---

### Q6. What information must always be in a bug report?

Non-negotiable fields in every bug report:
1. **Summary** — concise, specific, following the [Area] [Issue] [Condition] formula
2. **Steps to Reproduce** — numbered, exact, reproducible by anyone
3. **Expected Result** — from the spec or AC, not vague
4. **Actual Result** — precise — exact error messages, values, HTTP codes
5. **Environment** — OS, browser/client version, test environment, app version
6. **Severity** — impact classification
7. **Attachments** — screenshot or screen recording — always
8. **Linked Story** — traceability back to the requirement

Without all of these, a bug report creates unnecessary back-and-forth and slows down the team.

---

### Q7. How do you track test coverage in JIRA?

I use Zephyr Scale (or Xray) integrated with JIRA. Each Story has test cases linked to it via the traceability panel. Zephyr Scale shows:
- Coverage percentage: how many ACs or story points have test cases assigned
- Execution status: how many test cases have been executed and their Pass/Fail/Blocked status
- Traceability matrix: Story → Test Cases → Bugs

At a release level, I create a Test Cycle for each sprint and generate an execution report showing overall pass rate. I also use JQL to identify stories with zero linked test cases — a coverage gap indicator.

---

### Q8. What is a regression bug and how do you flag it?

A regression bug is a defect in functionality that was previously working correctly — introduced by a new code change. It means a bug was accidentally created while fixing something else or adding a new feature.

How I flag it in JIRA:
1. Add the label `regression` to the bug
2. Note in the description: "This feature was working in v2.3.0. Regression introduced in v2.4.0."
3. Link it to the Story or commit that introduced the change if identifiable
4. Raise its priority — regression bugs are particularly dangerous because they represent quality degradation in tested code

Prevention: Automated regression suites are the best defence. I ensure the test suite covers previously working areas that are adjacent to new changes.

---

### Q9. How do you handle a bug that cannot be reproduced?

First, I document everything I know: exact conditions, test data, environment, time, and any logs I can capture. Even a bug that cannot be reproduced immediately needs a record.

Steps I take:
1. Try to reproduce it myself multiple times, varying conditions slightly
2. Check if it is environment-specific — try different browsers, OS, or test environments
3. Check application logs for errors around the time it occurred
4. Provide the developer with all available context (log files, network traces, session recordings if available)
5. Mark the bug as "Cannot Reproduce" if the developer also cannot reproduce — but I do not close it permanently
6. Monitor: if it reappears, I immediately capture full evidence and update the ticket

Intermittent bugs are often timing, concurrency, or environment issues — they are worth investigating even when they cannot be reliably reproduced.

---

### Q10. What is a bug escape and how do you prevent it?

A bug escape is a defect that passes through QA and reaches production — found by end users instead of the test team. It is one of the most important metrics in QA quality assessment.

**Prevention strategies:**

1. **Risk-based testing** — prioritise testing high-traffic, high-value, or recently changed areas more thoroughly
2. **Definition of Done enforcement** — never approve a "Done" story without evidence all ACs pass
3. **Regression automation** — automate the most critical user journeys so they run on every deployment
4. **Exploratory testing** — structured exploration beyond scripted tests to catch unexpected edge cases
5. **Shift-left testing** — involve QA in design and development (reviewing PRs, testing early builds)
6. **Root cause analysis on escapes** — when a bug escapes, analyse why the test process did not catch it and update the test suite accordingly
7. **Exit criteria for releases** — define clear release gates (e.g., zero P1/P2 open bugs, 95%+ test pass rate)

Bug escapes should trigger a blameless post-mortem: the question is "how do we improve the process?" not "who missed this?"

---

*End of Guide — JIRA & Defect Management*
