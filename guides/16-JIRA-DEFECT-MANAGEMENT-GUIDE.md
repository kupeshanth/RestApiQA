# JIRA & Defect Management — Complete Interview Q&A Guide

> Every concept covered as a real interview question with full answer, notes, examples, and context.
> 40+ questions. Senior QA Engineer level.

---

## SECTION 1 — JIRA FUNDAMENTALS

---

**Q1: What is JIRA and how do QA engineers use it day-to-day?**

**A:** JIRA (by Atlassian) is the industry-standard project management and issue-tracking tool used by software development teams worldwide. For QA engineers it is the central operational hub for everything from planning to bug management.

How QA engineers specifically use JIRA:

| Activity | How JIRA supports it |
|---|---|
| Bug tracking | Log, track, and manage defects from discovery through closure |
| Sprint planning | Review and refine user stories; flag untestable acceptance criteria |
| Test coverage | Link test cases to stories; track what is and is not tested |
| Sprint execution | Update card statuses; add test evidence as comments and attachments |
| Reporting | Use dashboards and JQL filters to report on open bugs, test status, release readiness |
| Release gating | Block a release by keeping critical bugs open until verified fixed |
| Audit trail | Every action, comment, and status change is permanently recorded |

QA is the gatekeeper of the "Done" column. A story does not move to Done until QA has verified all acceptance criteria and there are no open critical or high bugs linked to it.

---

**Q2: What are the JIRA issue types? When do you use each one?**

**A:** JIRA has several issue types, each with a specific purpose:

**Epic**
- Represents a large body of work that spans multiple sprints — a major feature or theme
- Example: "User Authentication Module", "Checkout Redesign", "API v2 Migration"
- QA uses Epics to understand feature scope and plan regression testing across related stories
- Test coverage at the Epic level = has every aspect of this feature been tested?

**Story (User Story)**
- A user-facing requirement written from the end-user's perspective
- Format: "As a [role], I want [feature] so that [benefit]"
- Example: "As a registered user, I want to reset my password so I can regain access to my account"
- QA derives test cases directly from the Acceptance Criteria (AC) in the Story
- This is the primary unit of work QA tests in a sprint

**Task**
- Work that is not a user-facing feature and not a bug
- Example: "Set up test environment", "Update Postman collection", "Write load test scripts"
- QA creates Tasks for non-testing activities like infrastructure setup or documentation

**Sub-task**
- A child issue of a Story or Task, used to break down work into trackable chunks
- QA creates sub-tasks like: "Write test cases for login story", "Execute manual test run", "Automate regression tests", "Perform cross-browser check"
- Sub-tasks keep testing effort visible on the board without cluttering the parent Story

**Bug**
- A defect discovered during testing that deviates from expected behaviour
- Always linked to the Story or Epic it affects
- Carries: Severity, Priority, Steps to Reproduce, Environment, Attachments
- The primary JIRA issue type owned and managed by QA

**Improvement / Enhancement**
- A request to improve existing functionality (not broken, but could be better)
- Example: "Make the error message clearer when login fails"
- QA triages these: is this a bug (violates spec) or an enhancement (no spec violation)?

---

**Q3: What is a JIRA board? What is the difference between a Kanban board and a Scrum board?**

**A:** A JIRA board is the visual view of work items, organised into columns representing workflow stages (To Do, In Progress, Done, etc.). It is the team's shared view of what is being worked on right now.

| Aspect | Kanban Board | Scrum Board |
|---|---|---|
| Time-boxing | No fixed iterations — continuous flow | Fixed sprints (1, 2, or 4 weeks) |
| Planning cadence | Work pulled as capacity allows | Sprint planning meeting every sprint |
| WIP limits | Explicit column limits (e.g., max 3 cards in "In Progress") | Implicitly constrained by sprint scope |
| Release timing | Continuous / deploy whenever ready | End of sprint or after sprint demo |
| Backlog | Single prioritised queue | Product backlog + sprint backlog |
| Scope changes | Can add work at any time | Sprint scope is locked after planning |
| QA rhythm | Test continuously as stories arrive | Test all sprint stories before sprint ends |
| Best for | Support teams, bug-fix streams, operational work | Feature development teams |
| Key metric | Cycle time (how long each card takes) | Velocity (how many points per sprint) |

**QA in Kanban:** Focus is on throughput and WIP. When a card lands in your column, pick it up immediately. Flag anything that is blocked and causing bottlenecks.

**QA in Scrum:** Focus is on sprint commitment. Test all stories before the sprint retrospective. Log bugs early enough that developers can fix them within the same sprint. If a story cannot be tested in time, flag it in the stand-up — do not silently let it carry over.

---

## SECTION 2 — WRITING BUG REPORTS

---

**Q4: How do you write a good bug report summary? What is the formula?**

**A:** A good summary tells a developer exactly what the problem is, where it is, and under what condition it occurs — without them needing to read anything else.

**The formula:**
```
[Area/Component] [What went wrong] [Condition/Context]
```

**Good examples:**
```
Login: User redirected to 404 page when clicking 'Forgot Password' on mobile Safari
Checkout: Order total does not include applied discount code on payment summary screen
User Profile: Avatar upload fails silently when file size exceeds 2MB on Chrome 124
Search API: Returns HTTP 500 when query parameter contains special characters (%, &)
Registration: Duplicate account created when Submit button is double-clicked rapidly
Password Reset: Reset link does not expire after 24 hours — can be used multiple times
Admin Dashboard: Monthly revenue total inflated — refunded orders not subtracted
```

**Bad examples — and why:**
```
"Login is broken"                     — no specifics, not actionable
"Bug in checkout"                     — says nothing about what is wrong
"Something is wrong with the API"     — completely useless
"Fix the profile page"                — an instruction, not a description
"It doesn't work"                     — worst possible summary
```

**Why the formula works:** The Area tells the developer which module/team is responsible. The "what went wrong" describes the exact failure. The "condition/context" tells them when it happens — which is often 80% of the debugging information they need.

**Interview tip:** A hiring manager will ask you to write a bug report on the spot. Having this formula memorised and being able to apply it immediately demonstrates senior-level QA thinking.

---

**Q5: What fields must every bug report have? What happens if fields are missing?**

**A:** Every bug report must be complete enough for a developer who has never seen the feature to reproduce, understand, and fix the defect without asking a single follow-up question.

**Non-negotiable fields:**

| Field | What to put | Why it matters |
|---|---|---|
| **Summary** | [Area] [Issue] [Condition] formula | First thing read; determines urgency |
| **Steps to Reproduce** | Numbered, exact, with test data | Without this, no one can reproduce it |
| **Expected Result** | Quote from AC or spec | Defines what "correct" means |
| **Actual Result** | Exact error, value, HTTP code | Describes the failure precisely |
| **Environment** | OS, browser, version, test env | Bug may be environment-specific |
| **Severity** | Critical / High / Medium / Low | Determines triage priority |
| **Attachments** | Screenshot or screen recording | Visual evidence removes ambiguity |
| **Linked Story** | Parent Story or Epic | Establishes traceability |

**Optional but very valuable:**
- Labels (regression, api, mobile, security)
- Components (which part of the system)
- Fix Version (which release should fix it)
- Priority (P1/P2/P3/P4 — set during triage)

**What happens when fields are missing:**
- No Steps to Reproduce = developer cannot reproduce = "Cannot Reproduce" status = wasted time
- No Expected Result = developer argues "it's working as designed" = prolonged debate
- No Environment = developer cannot set up the right conditions = cannot reproduce
- No screenshot = developer sees different state = "I can't see this issue"
- No Severity = bug sits in triage without urgency = gets missed

A missing field in a bug report always costs time. A complete bug report saves the team hours.

---

**Q6: What is the Steps to Reproduce format? Show a perfect example.**

**A:** Steps to Reproduce must be a numbered list where each step is a single, specific action starting from a clean, known state. They must include exact test data, exact UI elements, and exact navigation paths.

**Rules:**
1. Start from a defined starting state (logged out, specific user, specific data)
2. One action per step — no combining actions
3. Include exact data (email address, product name, dollar amount)
4. Specify the exact UI element (button name, link text, field label)
5. Be specific about what to observe at each relevant step

**Perfect example:**
```
STEPS TO REPRODUCE:

1. Open Safari browser on iPhone 14 Pro (iOS 17.2)
2. Navigate to https://qa.myapp.com/login
3. Enter email: testuser@example.com in the Email field
4. Enter password: TestPass123! in the Password field
5. Tap the 'Sign In' button
6. Verify you are on the Dashboard page (logged in successfully)
7. Tap the Profile icon in the top-right corner
8. Tap 'Account Settings'
9. Tap 'Change Password'
10. Enter current password: TestPass123!
11. Enter new password: NewPass456! in the 'New Password' field
12. Enter NewPass456! in the 'Confirm Password' field
13. Tap the 'Save Changes' button
14. Observe the resulting screen and any error messages
```

**What makes this perfect:**
- Starts from a defined starting state (specific browser, specific device)
- Uses exact test credentials (not "any test user")
- Every step is one action
- The reproduce path is unambiguous — anyone anywhere can follow it
- The observation step is at the end, not mixed with actions

---

**Q7: What is Expected Result vs Actual Result? What makes a good one?**

**A:** Expected Result describes what should happen according to the specification, acceptance criteria, or business requirement. Actual Result describes exactly what did happen.

**What makes a GOOD Expected Result:**
- References the Acceptance Criteria or specification by ID/section
- Describes the exact outcome — HTTP status, UI state, data value, message text
- Is objective, not vague

**What makes a GOOD Actual Result:**
- States the exact failure — HTTP error code, exact error message text, the specific wrong value
- Includes supporting data (from DevTools, logs, database query)
- Does not editorialize — just states facts

**Comparison table:**

| Field | Bad (avoid) | Good (use this) |
|---|---|---|
| Expected | "It should work correctly" | "User is redirected to the Dashboard (per AC #3 of MYAPP-145)" |
| Expected | "The form should submit" | "HTTP 200 OK is returned with body `{ success: true, userId: <id> }`" |
| Actual | "The page breaks" | "HTTP 500 Internal Server Error is returned. Response body: `{ error: 'SQL syntax error near...' }`" |
| Actual | "Wrong data shows" | "Order total displayed: $120.00. Expected: $96.00 (20% discount on $120.00 = $24.00 off)" |
| Actual | "Button doesn't work" | "Clicking 'Submit' produces no visible response. No network request is made (verified in Chrome DevTools Network tab)" |

**Key principle:** A developer should be able to fix the bug from the Expected/Actual alone, without even running the repro steps. That is how precise they should be.

---

**Q8: Show a complete bug report for a UI bug with all fields filled.**

**A:**

```
SUMMARY:
Login: User redirected to 404 page when clicking 'Forgot Password' on mobile Safari

DESCRIPTION:
When a user on mobile Safari iOS 17 taps the 'Forgot Password' link on the Login
page, they are redirected to a broken 404 page instead of the password reset form.
This prevents mobile Safari users from recovering their accounts.
Confirmed on 3 different iPhones. Tested on Chrome mobile — works correctly.
This is a mobile Safari-specific regression. Worked correctly in v2.3.1.

STEPS TO REPRODUCE:
1. Open Safari browser on iPhone (iOS 17.x)
2. Navigate to https://qa.myapp.com/login
3. Enter any email address in the Email field (use: testuser@example.com)
4. Tap the 'Forgot Password?' link below the password field
5. Observe the resulting page and the URL in the address bar

EXPECTED RESULT:
User is navigated to the Password Reset page (/forgot-password) showing the
"Enter your email to receive a reset link" form.
Per Acceptance Criteria #2 of MYAPP-145 (User Password Recovery).

ACTUAL RESULT:
User is redirected to a 404 "Page Not Found" page.
URL in address bar: https://qa.myapp.com/forgot-passord
Note the typo — 'forgot-passord' is missing the letter 'w'. This is a hardcoded
link with a typo that only manifests on mobile Safari (possibly case/routing issue).

ENVIRONMENT:
- Device: iPhone 14 Pro, iPhone 13 Mini (reproduced on both)
- OS: iOS 17.2
- Browser: Safari 17.x (default)
- Not reproduced on: Chrome 124 (Android), Chrome 124 (desktop), Firefox 125 (desktop)
- App Version: 2.4.1
- Test Environment: QA (https://qa.myapp.com)

SEVERITY: High
PRIORITY: P1
LABELS: mobile, safari, regression, login, authentication
COMPONENTS: Authentication, Frontend
FIX VERSION: 2.4.2
LINKED ISSUES: MYAPP-145 (parent story: User Password Recovery)
ATTACHMENTS: screenshot_404_safari.png, screen_recording_safari_forgot_password.mp4
```

---

**Q9: Show a complete bug report for an API bug with all fields filled.**

**A:**

```
SUMMARY:
Search API: Returns HTTP 500 when query parameter contains special characters (%, &)

DESCRIPTION:
GET /api/v1/products/search returns a 500 Internal Server Error when the 'q' query
parameter includes URL-encoded special characters such as % or &.
This is a data validation failure — input is not sanitised before being passed to
the database query layer. The error response exposes a SQL fragment, suggesting a
potential SQL injection vulnerability. Affects any search including terms like
"50% off", "salt & pepper", "R&B music".

STEPS TO REPRODUCE:
1. Authenticate and obtain a valid Bearer token (use: testapi@myapp.com / TestApi123!)
2. Open Postman or cURL
3. Send: GET https://qa-api.myapp.com/api/v1/products/search?q=50%25+off
   Headers: Authorization: Bearer <token>, Accept: application/json
4. Observe the HTTP response status and body
5. Repeat with: GET .../search?q=salt+%26+pepper
6. Observe both return 500

EXPECTED RESULT:
HTTP 200 OK with a JSON array of matching products, or HTTP 200 with an empty
array [] if no matching products exist.
Per API specification in Confluence: API Contract v1.3, Section 4.2 (Search endpoint).

ACTUAL RESULT:
HTTP 500 Internal Server Error
Response body:
{
  "error": "Internal Server Error",
  "message": "Unexpected token '%' in SQL near '...WHERE name LIKE '%50%'"
}
The SQL fragment in the error message exposes internal query structure — possible
SQL injection vulnerability that should be investigated alongside the input sanitisation fix.

ENVIRONMENT:
- API Base URL: https://qa-api.myapp.com
- API Version: v1
- Authentication: Bearer token (OAuth2, scope: read:products)
- Testing tools: Postman v10.21, automated test MYAPP-AT-212
- Test Environment: QA
- Reproduced in: automated test suite (see MYAPP-AT-212)
- Not tested in: Staging (environment not available at time of filing)

SEVERITY: Critical (potential SQL injection + 500 on valid input)
PRIORITY: P1
LABELS: api, security, input-validation, regression, sql-injection
COMPONENTS: Search API, Backend, Security
FIX VERSION: 2.4.2
LINKED ISSUES: MYAPP-201 (parent story: Product Search), MYAPP-AT-212 (failing automation test)
ATTACHMENTS: postman_500_response.png, postman_collection_search_bug.json, curl_repro.sh
```

---

**Q10: Show a complete bug report for a data bug with all fields filled.**

**A:**

```
SUMMARY:
Reporting Dashboard: Monthly revenue total inflated — refunded orders excluded from calculation

DESCRIPTION:
The Monthly Revenue figure on the Admin Reporting Dashboard shows gross revenue
(all orders) when it should show net revenue (after refunds), per Finance
Reporting Spec v2.1 Section 3.2. Finance team flagged a $14,200 discrepancy
between the JIRA dashboard and the accounting system for March 2024.
This is a data integrity issue affecting financial reporting accuracy and potentially
impacting business decisions.

STEPS TO REPRODUCE:
1. Log in as Admin: admin@myapp.com / TestAdmin123!
2. Navigate to: Admin > Reports > Monthly Revenue
3. Select month: March 2024
4. Note the 'Total Revenue' figure displayed
5. Compare against Finance data:
   - Gross orders March 2024: $156,700
   - Total refunds issued March 2024: $14,200
   - Expected net revenue (per spec): $142,500
   - Expected gross revenue (per spec): $156,700
6. Cross-reference Confluence: Finance Reporting Spec v2.1, Section 3.2
   "Monthly Revenue = Gross Revenue (before refunds)"

EXPECTED RESULT:
Monthly Revenue = $156,700 (gross: sum of ALL orders placed in March, regardless
of refund status). Definition per Finance Reporting Spec v2.1 Section 3.2.

ACTUAL RESULT:
Monthly Revenue displayed: $142,500
This equals Net Revenue (all orders minus refunds), not Gross Revenue as specified.
The dashboard is excluding refunded orders from the total.
Difference from expected: $14,200 (the total refunded amount for March).

SQL verification:
SELECT SUM(total_amount) FROM orders WHERE DATE_FORMAT(created_at, '%Y-%m') = '2024-03';
-- Returns: $156,700 (gross — what the dashboard should show)
SELECT SUM(total_amount) FROM orders WHERE DATE_FORMAT(created_at, '%Y-%m') = '2024-03'
  AND status != 'refunded';
-- Returns: $142,500 (what the dashboard incorrectly shows)

ENVIRONMENT:
- URL: https://qa.myapp.com/admin/reports/monthly-revenue
- Role: Admin
- Test Data: March 2024 dataset in QA environment
- Browser: Chrome 124 (also reproduced on Firefox 125)
- Database: PostgreSQL (QA instance)

SEVERITY: High (incorrect financial data affects business reporting)
PRIORITY: P2 (no immediate customer-facing impact but affects Finance operations)
LABELS: data-integrity, reporting, finance, calculation-error, regression
COMPONENTS: Admin Dashboard, Reporting Module, Database
FIX VERSION: 2.5.0
LINKED ISSUES: MYAPP-310 (parent story: Revenue Dashboard), MYAPP-DS-44 (Finance data spec)
ATTACHMENTS: dashboard_screenshot_march.png, finance_comparison_march.xlsx, db_query_results.png
```

---

## SECTION 3 — SEVERITY AND PRIORITY

---

**Q11: What is severity? What are the severity levels? Who sets it?**

**A:** Severity describes how bad the technical impact of a bug is — how much damage it does to the system or user experience. It is a technical assessment.

**Severity is set by: the QA Engineer** — you assessed the impact when you found the bug.

| Level | Definition | Examples |
|---|---|---|
| **Critical** | System crash, complete feature unavailable, data loss or corruption, security breach | App crashes on launch; payment processing fails for all users; personal data exposed; SQL injection possible; database corrupted |
| **High** | Major feature broken with no workaround, significant portion of users affected | Login fails for 30% of users; checkout process blocked; API consistently returns wrong data; file upload completely non-functional |
| **Medium** | Feature partially broken, a workaround exists, business flow degraded but not blocked | Search filter not working (can still browse manually); date shown in wrong format; export to PDF fails (can use CSV instead) |
| **Low** | Cosmetic issue, no functional impact, minor inconvenience | Typo in label text; button slightly misaligned; wrong tooltip text; incorrect icon colour |

**Critical rule:** Severity is about the technical reality of the impact, not about how many people are affected (that is partly priority). A crash that affects 0.1% of users is still Critical severity — it is still a crash.

---

**Q12: What is priority? What are the priority levels? Who sets it?**

**A:** Priority describes how urgently the business needs the bug fixed. It is a business decision based on context — release dates, customer contracts, visibility, and risk tolerance.

**Priority is set by: the Product Manager or Business Analyst** — they understand the business context that determines urgency.

| Level | Definition | Target response |
|---|---|---|
| **P1** | Fix immediately — production incident, imminent release blocker, contractual SLA breach | Same day / within hours |
| **P2** | Fix in the current sprint — significant impact if delayed further | Within the current sprint |
| **P3** | Fix in the next sprint — important but not urgent | Planned in an upcoming sprint |
| **P4** | Fix when time allows — low business impact, can wait | Backlog — no specific timeline |

---

**Q13: What is the difference between severity and priority? Give examples of all 4 major combinations.**

**A:** Severity and priority measure different things and are set by different people:

- **Severity** = how bad is the bug technically? (QA sets this)
- **Priority** = how urgently does the business need it fixed? (PM sets this)

They frequently diverge, and understanding why is a key senior QA skill.

**All 4 major combinations with real examples:**

**HIGH Severity + HIGH Priority (most common critical scenario):**
- Bug: Payment processing fails for all users in production
- Severity is Critical — complete feature failure affecting all users
- Priority is P1 — revenue is stopping in real time, fix immediately
- Action: Immediate hotfix, everyone drops current work

**HIGH Severity + LOW Priority:**
- Bug: App crashes when importing more than 1,000 records in the admin bulk import tool
- Severity is Critical — a crash is always Critical
- Priority is P4 — this tool is used by 2 internal data analysts once per quarter; the whole module is being deprecated next quarter
- Action: Schedule for the decommission plan; no sprint impact

**LOW Severity + HIGH Priority:**
- Bug: The homepage still shows the old 2019 company logo instead of the new rebranded logo
- Severity is Low — purely cosmetic, no functionality affected
- Priority is P1 — the CEO is demoing the product live in 4 hours at the company rebrand launch
- Action: CSS fix deployed immediately

**MEDIUM Severity + MEDIUM Priority:**
- Bug: Date format on the Invoice PDF shows MM/DD/YYYY but EU customers expect DD/MM/YYYY
- Severity is Medium — affects users but a workaround exists (they can read the date, just inconvenient)
- Priority is P3 — planned for next sprint as part of localisation work
- Action: In the backlog, addressed in upcoming sprint

**Interview insight:** The classic trap is assuming high severity always means high priority. Demonstrating you understand this separation shows senior-level thinking.

---

## SECTION 4 — BUG LIFECYCLE

---

**Q14: What is the defect lifecycle? Walk through ALL statuses.**

**A:** The defect lifecycle traces a bug from the moment it is discovered through to its final resolution. Every transition has a specific trigger and responsible party.

```
QA finds bug
     |
     v
   [NEW]         ← QA creates the bug report with all fields
     |
     v
  [OPEN]         ← Triage: bug accepted as valid, assigned to developer
     |
     v
[IN PROGRESS]   ← Developer actively working on the fix
     |
     v
  [FIXED]        ← Developer completes fix, deploys to QA environment
     |
     v
[READY FOR       ← QA is notified that the fix is available to test
  RETEST]
     |
     v
  [RETEST]       ← QA executing verification of the fix
     |
    / \
   /   \
PASS  FAIL
  |     |
  v     v
[CLOSED] [REOPENED] → back to IN PROGRESS
```

**Full status dictionary:**

| Status | Meaning | Who triggers it |
|---|---|---|
| New | Bug just created, not yet reviewed | QA |
| Open | Triaged, validated as real bug, assigned | Dev Lead / PM during triage |
| In Progress | Developer actively working on fix | Developer |
| Fixed | Fix committed, deployed to QA environment | Developer |
| Ready for Retest | QA is notified to verify the fix | Developer |
| Retest | QA is actively verifying | QA |
| Closed | Fix verified and confirmed working | QA |
| Reopened | Bug reappears after being Closed | QA |
| Rejected | Determined to be not a bug (intended behaviour or invalid report) | Dev / PM during triage |
| Duplicate | Same bug already reported; linked to the original | QA / Dev |
| Deferred | Valid bug, consciously not fixing in this release | PM |
| Cannot Reproduce | Developer / QA cannot replicate with provided steps | Dev |
| Won't Fix | Valid bug, business decision not to fix it | PM |

---

**Q15: What is Rejected status? When do you reject a bug, and what does QA do when their bug gets rejected?**

**A:** A bug is Rejected when it is determined to be working as designed (not a defect), when the report contains incorrect information, or when the issue cannot be considered a bug against the current specification.

**Legitimate rejection reasons:**
- The AC does not specify the behaviour QA expected — it is a gap in the AC, not a bug
- The test was performed against incorrect test data or an outdated environment
- The behaviour is intentionally designed that way (confirmed by PM)
- The report is based on a misunderstanding of the feature's purpose

**When QA receives a rejection:**

Step 1: Read the rejection reason carefully and with an open mind. Sometimes the developer has context you do not.

Step 2: Check the Acceptance Criteria, specification, or design mockup. If the actual behaviour violates the AC, the rejection is incorrect.

Step 3: If you still believe the bug is valid:
- Add a comment citing the exact AC or requirement that is violated
- Attach additional evidence — screen recording, comparison with spec
- Discuss with the developer directly — most rejections come from misunderstandings, not malice
- Escalate to the Product Manager or Tech Lead if there is a genuine disagreement

Step 4: If the PM confirms "that is intentional behaviour," close the bug and raise a separate Story to update the AC to reflect the actual behaviour.

**Key principle:** Never close a bug you believe is valid just to avoid conflict. Your role is to advocate for quality based on specifications. The spec is the arbiter.

---

**Q16: What is Deferred status? When do you defer a bug?**

**A:** Deferred means the bug is valid and acknowledged, but the team has consciously decided not to fix it in the current release. It is a risk-accepted decision made by the Product Manager or business stakeholder.

**When bugs are deferred:**
- The fix is complex and would destabilise the release if rushed
- The affected feature is being redesigned or deprecated soon anyway
- The bug affects a low-traffic area and the release deadline is immovable
- Business has evaluated the risk and decided the bug is acceptable for this version

**How to handle a Deferred bug as QA:**
1. Ensure it is documented clearly — the Deferred status, the reason, and the target version for the fix
2. Add to the release notes if it is user-visible ("Known Issues" section)
3. Add the bug to the regression suite — it should be re-verified in the release when it is eventually fixed
4. Monitor: if the deferred bug causes more user complaints or escalates in business impact, advocate for re-prioritising it

**What Deferred is NOT:**
- It is not a way to silently ignore bugs
- A QA engineer should not accept "defer" for Critical or P1 bugs without escalating to senior leadership — deferring a critical bug is a risk acceptance decision that requires explicit sign-off

---

**Q17: What is Duplicate status? How do you handle duplicate bug reports?**

**A:** A Duplicate is when the same defect has been reported more than once — two separate JIRA tickets describing the same root cause and same failure.

**How to identify duplicates:**
- Search for the same area and symptom before filing: `project = MYAPP AND summary ~ "forgot password" AND status != Closed`
- Check the existing bug board before creating a new report
- Duplicates may have different repro steps but the same underlying cause

**How to handle a Duplicate:**
1. Identify which ticket is the canonical (original) one — usually the older one or the more detailed one
2. On the duplicate: set status to "Duplicate", use the JIRA link type "duplicates" to link to the original
3. Add a comment explaining why it is a duplicate and which ticket to track
4. All discussion, updates, and fixes happen on the original ticket
5. Both tickets close when the original is fixed and verified

**If your bug is marked as Duplicate:**
- Read the linked original bug to confirm it is truly the same issue
- If the root cause differs (same symptom, different cause), comment to explain the distinction — sometimes what looks like a duplicate is actually a different underlying bug
- If it is a true duplicate, follow the original ticket for updates

---

**Q18: What is Cannot Reproduce status? How do you handle a bug you cannot reproduce?**

**A:** Cannot Reproduce means the developer (or QA, during retest) attempted to follow the Steps to Reproduce and could not trigger the failure.

**Common reasons a bug cannot be reproduced:**
- Environment-specific (occurs only on iOS, only in QA-2, only under a specific load)
- Timing-related (race condition, intermittent)
- Test data-specific (the exact data state that triggered it no longer exists)
- Fix was deployed without awareness (another PR fixed it as a side effect)
- Steps were incomplete or ambiguous in the original report

**What QA does when their bug is marked Cannot Reproduce:**

1. Do not close it — "Cannot Reproduce" is not the same as "does not exist"
2. Provide additional context:
   - Exact test data (export the data, attach it)
   - Video recording of the bug occurring
   - Browser/OS version strings (from about page)
   - Application log excerpt from when the bug occurred
   - Network trace (HAR file) if it is an API issue
3. Try to reproduce it yourself again in the same environment at the same time of day
4. Ask: is this environment-specific? Try different environments
5. If it remains unreproducible, escalate with a comment: "I observed this on [specific conditions]. Attaching full evidence. Keeping open for monitoring — if it reappears I will capture full trace immediately."
6. Set a reminder or add it to your exploratory test session checklist

**Intermittent bugs are real bugs.** A race condition that appears 1 in 10 times is still a defect. The challenge is capturing enough evidence for the developer to fix it, not dismissing it.

---

## SECTION 5 — JIRA IN AGILE SPRINTS

---

**Q19: How does QA use JIRA in sprint planning? What do you look for?**

**A:** Sprint planning is where QA has its most important preventative role — catching problems before they reach the test phase.

**QA activities during sprint planning / backlog grooming:**

**Evaluate Acceptance Criteria quality for every story:**
A story is not ready for sprint if its AC is vague, missing, or non-testable:
- "The page should be user-friendly" — not testable
- "It should work correctly" — not testable
- "Fast enough" — not testable (define: under 2 seconds?)
- Missing error states — what happens when the API returns an error? What happens with no results?
- Missing edge cases — what is the max file size? What happens at boundaries?

**Questions QA asks during grooming:**
- "What exactly should happen when [edge case]?"
- "Is there an error state we need to test — what should the user see?"
- "Which browsers and devices must this work on?"
- "Are there performance requirements — must it load in under 2 seconds?"
- "What test data will we need — do we have it in QA environment?"
- "Does this story have external API dependencies that might not be ready?"

**Block stories that are not testable:** A QA engineer should formally block a story from being brought into the sprint if the AC is too vague to test. Getting clear AC before sprint start saves far more time than trying to test ambiguous requirements mid-sprint.

**Estimate testing effort:** Advocate for testing effort to be included in story points. A 13-point story with no testing budget is unrealistic.

---

**Q20: How does QA use JIRA during sprint execution?**

**A:** During sprint execution, JIRA is the primary communication and coordination tool for the QA engineer.

**Daily QA workflow:**

Morning:
- Check the board for stories moved to "Ready for QA" overnight
- Review any bugs that were marked "Fixed" or "Ready for Retest" by developers
- Check the sprint burndown to assess if the sprint is on track

During testing:
- Pick up "Ready for QA" stories in priority order
- Move cards through QA statuses (In QA → Done or bug filed)
- Log bugs immediately with full details — do not batch them at end of day
- Add test progress comments to the story card: "Testing AC 1, 2 — PASS. AC 3 in progress."

Blocked:
- If testing is blocked (environment down, missing test data, dependency unmet), immediately update the card with a "Blocked" label and raise it in stand-up
- Do not silently sit on a blocked story

End of day:
- Update all card statuses
- Add a comment to any card you did not finish: "Testing in progress — ACs 1-3 done. AC 4 pending due to env issue."

Sprint close:
- Verify every "Done" story has: all ACs verified, no open critical/high bugs, test evidence attached
- A story with an open P1 bug linked to it should NOT be moved to Done
- Flag any stories that will carry over — these affect velocity and sprint metrics

---

**Q21: How do you link bugs to stories in JIRA? Why does it matter?**

**A:** In JIRA, you link issues using the "Link Issue" function, selecting the relationship type.

**Most common link types used by QA:**

| Link Type | When to use |
|---|---|
| `relates to` | Bug is related to this story (general association) |
| `is caused by` / `causes` | One bug is the root cause of another |
| `blocks` / `is blocked by` | Bug MYAPP-200 blocks Story MYAPP-145 from being closed |
| `duplicates` / `is duplicated by` | Two tickets describing the same defect |
| `is tested by` | Story is tested by a particular test case (Zephyr integration) |

**Why linking matters:**

1. **Traceability:** You can trace from a business requirement (Story) to the defects found against it, to whether those defects were fixed. This is the audit trail.

2. **Release gate:** A story with a linked `blocks` bug cannot be moved to Done until the bug is resolved. JIRA can enforce this.

3. **Impact analysis:** When a bug is found, linking it to the story shows the PM exactly which feature is affected and whether that story can ship.

4. **Metrics:** Bug count per story, bug count per Epic, re-opened bug rate — all of these come from the link structure.

5. **Regression prevention:** When a bug is closed, its linked Story tells you what to include in the regression suite.

---

**Q22: What are sub-tasks in JIRA? How does QA use them?**

**A:** Sub-tasks are child issues belonging to a Story or Task. They allow complex work to be broken down into trackable, assignable chunks without creating noise at the Story level.

**QA creates sub-tasks to make testing work visible:**

```
Story: MYAPP-145 — User Password Reset
  |
  ├── Sub-task: MYAPP-146 — Write test cases for password reset (QA, 2 pts)
  ├── Sub-task: MYAPP-147 — Execute manual test cases — password reset (QA, 1 pt)
  ├── Sub-task: MYAPP-148 — Cross-browser testing — password reset iOS/Android (QA, 1 pt)
  └── Sub-task: MYAPP-149 — Automate password reset regression tests (QA, 3 pts)
```

**Why sub-tasks matter for QA:**
- Testing work is visible in sprint planning — it is not hidden inside the story estimate
- The board shows a realistic completion state — a story is not "done" if the automation sub-task is still open
- Different QA engineers can be assigned different sub-tasks (one writes, one executes)
- Sprint velocity is measured more accurately (a story should not be "done" until all sub-tasks are done)

**Sub-task naming convention:**
```
[Activity]: [Feature or Story name] — [Scope if needed]
Write test cases: Password Reset
Execute test cases: Password Reset — happy path
Automate: Password Reset regression
Cross-browser: Password Reset — iOS Safari
```

---

## SECTION 6 — JQL

---

**Q23: What is JQL? Write 10 real QA JQL queries with explanations.**

**A:** JQL (JIRA Query Language) is JIRA's search syntax. It lets you find, filter, and report on issues with precision. Every QA engineer should be able to write JQL without looking it up.

**Syntax pattern:**
```
field operator value [AND/OR field operator value]
```

**10 real QA JQL queries:**

**1. All bugs assigned to me in the current project:**
```jql
project = "MYAPP" AND issuetype = Bug AND assignee = currentUser() AND status != Closed
```
Use: Your daily bug queue.

**2. All bugs Ready for Retest in current sprint:**
```jql
project = "MYAPP" AND sprint in openSprints() AND status = "Ready for Retest"
```
Use: Morning check — what fixes are waiting for your verification?

**3. All open P1 and P2 bugs:**
```jql
project = "MYAPP" AND issuetype = Bug AND priority in (P1, P2)
  AND status not in (Closed, Done, "Won't Fix")
```
Use: Release readiness check — can we ship?

**4. Bugs created in the last 7 days:**
```jql
project = "MYAPP" AND issuetype = Bug AND created >= -7d ORDER BY created DESC
```
Use: Weekly bug trend report.

**5. All bugs for a specific fix version (release):**
```jql
project = "MYAPP" AND issuetype = Bug AND fixVersion = "2.5.0"
ORDER BY priority ASC, status ASC
```
Use: Release-specific bug dashboard.

**6. All unresolved bugs in a specific component:**
```jql
project = "MYAPP" AND issuetype = Bug AND component = "Authentication"
  AND resolution = Unresolved
```
Use: Find all known issues in one module.

**7. Stories with no acceptance criteria (testability check):**
```jql
project = "MYAPP" AND issuetype = Story AND sprint in openSprints()
  AND "Acceptance Criteria" is EMPTY
```
Use: Backlog grooming — flag untestable stories before they enter the sprint.

**8. Bugs reported by me that are still open:**
```jql
project = "MYAPP" AND issuetype = Bug AND reporter = currentUser()
  AND status not in (Closed, Done)
ORDER BY priority ASC
```
Use: Track your own filed bugs to ensure they are progressing.

**9. Overdue bugs — created more than 14 days ago and still not resolved:**
```jql
project = "MYAPP" AND issuetype = Bug AND created <= -14d
  AND status not in (Closed, Done, "Won't Fix", Deferred)
ORDER BY priority ASC, created ASC
```
Use: Weekly triage — identify stale bugs that need attention.

**10. All bugs marked as duplicates in the current sprint:**
```jql
project = "MYAPP" AND issuetype = Bug AND sprint in openSprints()
  AND status = "Duplicate"
```
Use: Identify and close duplicate ticket clutter.

**JQL operator quick reference:**

| Operator | Example |
|---|---|
| `=` | `status = "In Progress"` |
| `!=` | `status != Closed` |
| `in` | `priority in (P1, P2)` |
| `not in` | `status not in (Closed, Done)` |
| `is EMPTY` | `assignee is EMPTY` |
| `is not EMPTY` | `fixVersion is not EMPTY` |
| `>=` | `created >= -7d` |
| `~` | `summary ~ "login"` (contains) |

---

## SECTION 7 — TEST MANAGEMENT

---

**Q24: What is Zephyr Scale? How do you create test cases in JIRA using it?**

**A:** Zephyr Scale (by SmartBear, formerly Zephyr for JIRA) is the most widely used test management add-on for JIRA. It extends JIRA with test-specific issue types (Test Cases, Test Cycles, Test Executions) and provides traceability between business requirements and test coverage.

**Core concepts:**

| Concept | Description |
|---|---|
| Test Case | A reusable test with preconditions, steps, expected results, and status |
| Test Cycle | A collection of test cases executed together for a sprint or release |
| Test Execution | An instance of running a test case within a cycle — records Pass/Fail/Blocked |
| Test Plan | High-level grouping of test cycles across releases |
| Folder | Organises test cases by feature, component, or sprint |

**Creating a test case linked to a Story:**
1. Open the Story in JIRA (e.g., MYAPP-145)
2. Find the Zephyr Scale panel (usually in the right sidebar or bottom of issue view)
3. Click "Create Test Case"
4. Fill in:
   - Summary: what is being tested
   - Precondition: the starting state required
   - Test Steps: numbered actions
   - Expected Result: per step or at the end
5. Link the test case to Story MYAPP-145 (traceability link is created)
6. Assign the test case to a Test Cycle

**Test case example structure:**
```
Test Case: TC-003 — Forgot Password Link navigates to Reset page
Precondition: User is on the Login page (/login), not logged in
Links to: MYAPP-145, AC #2

Steps:
1. Tap the 'Forgot Password?' link               Expected: /forgot-password page loads
2. Verify the page title                          Expected: "Reset Your Password"
3. Verify an email input field is present         Expected: Email field visible, empty
4. Verify the 'Send Reset Link' button is present Expected: Button visible, enabled
```

---

**Q25: What is a test cycle in Zephyr Scale?**

**A:** A test cycle is a collection of test cases grouped for a specific purpose — typically a sprint, a release, or a specific testing phase (smoke, regression, exploratory). It is the execution unit: you run a cycle, record results for each test case, and the cycle gives you a pass rate.

**Typical sprint test cycle structure:**
```
Sprint 24 Test Cycle — v2.4.1
├── Authentication Feature Tests (linked to MYAPP-145, MYAPP-146)
│   ├── TC-001: Valid login — happy path                          [PASS]
│   ├── TC-002: Invalid password — correct error message shown    [PASS]
│   ├── TC-003: Forgot Password link — mobile Safari             [FAIL → Bug MYAPP-200]
│   └── TC-004: Session expires after 30 minutes of inactivity   [PASS]
│
├── Checkout Feature Tests (linked to MYAPP-155)
│   ├── TC-010: Add item to cart                                  [PASS]
│   ├── TC-011: Apply discount code                               [BLOCKED]
│   └── TC-012: Complete checkout with credit card               [NOT EXECUTED]
│
└── Regression Suite — Core Flows
    ├── TC-050: Register new user                                  [PASS]
    └── TC-051: Place and complete an order                       [PASS]
```

**Test cycles are used for:**
- Sprint-level reporting: what percentage of tests passed in Sprint 24?
- Release gate: do we have enough test coverage to ship v2.4.1?
- Historical record: what was the test state when v2.3.0 was released?

---

**Q26: What is test execution status in Zephyr Scale?**

**A:** When you execute a test case within a cycle, you record one of the following statuses:

| Status | Meaning | Next action |
|---|---|---|
| **Pass** | Test executed, actual result matches expected | No action needed |
| **Fail** | Test executed, actual result does not match expected | Log a JIRA bug, link it to the execution |
| **Blocked** | Cannot execute — dependency not met, environment down, test data missing | Log a JIRA blocker, escalate |
| **Not Executed** | Test not yet run | Default status when cycle is created |
| **In Progress** | Currently executing (useful for long tests) | Update when done |
| **N/A** | Not applicable for this execution context | Use when a test is conditionally excluded |

**Key principle:** A Fail status should always be accompanied by a JIRA bug ticket. The link between the test execution failure and the bug ticket is what creates full traceability: Requirement → Test Case → Execution → Bug.

---

**Q27: How do you track test coverage in JIRA?**

**A:** Test coverage is tracked through the traceability chain from Story to test case to execution.

**How to track coverage:**

1. **Story-level coverage:** In Zephyr Scale, open a Story and see the "Test Coverage" panel — it shows how many test cases are linked and their execution status. A Story with 5 ACs and 3 test cases has gaps.

2. **JQL for uncovered stories:**
   ```jql
   project = "MYAPP" AND issuetype = Story AND sprint in openSprints()
     AND issue not in testPlan()
   ```
   Returns stories with no linked test cases at all.

3. **Sprint cycle report:** The Test Cycle execution report in Zephyr Scale shows total tests / executed / pass / fail / blocked breakdown for the entire sprint.

4. **Traceability matrix:** Zephyr Scale generates a traceability matrix: Story → Test Cases → Test Executions → Bugs. This shows at a glance where coverage exists and where there are gaps.

5. **At release time — release readiness check:**
   ```jql
   project = "MYAPP" AND fixVersion = "2.5.0" AND issuetype = Bug
     AND status not in (Closed, Done)
   ```
   Zero results = no open bugs in this release. Combined with cycle pass rate, this is your release gate evidence.

**What "100% coverage" actually means:** Every AC in every story in scope has at least one test case, and every test case has been executed with a Pass or documented exception. Coverage is not about line coverage — it is about requirement traceability.

---

## SECTION 8 — ADVANCED TOPICS AND SCENARIOS

---

**Q28: What is a bug escape? How do you prevent it?**

**A:** A bug escape is a defect that passes through the QA process and reaches production — found by real users instead of the test team. It is a key quality metric. A high escape rate means the QA process has gaps.

**Bug escape rate formula:**
```
Escape rate = (Production bugs found by users) / (Total bugs found in period) × 100%
```

**Prevention strategies:**

1. **Risk-based testing:** Focus more effort on high-traffic, high-value, and recently changed areas. A login page change deserves more testing than a rarely used admin utility.

2. **Definition of Done enforcement:** A story is not Done without evidence that all ACs passed. Never approve "Done" verbally — require attached test evidence.

3. **Automated regression suite:** Automate the critical user journeys (login, checkout, core API flows) so they run on every deployment. Human testers cannot run the same 200 tests after every merge — automation can.

4. **Shift-left testing:** Involve QA in design (review wireframes), development (review PRs, test dev builds early), and planning (flag untestable ACs before sprint start). Earlier discovery = cheaper fix.

5. **Exploratory testing:** Scripted tests miss unexpected interactions. Structured exploration of new features beyond the AC is essential.

6. **Exit criteria for releases:** Define objective release gates — no P1/P2 open bugs, 95%+ pass rate on regression cycle, all critical paths automated.

7. **Post-escape analysis:** When a bug escapes, run a blameless post-mortem: "Why did our process not catch this? What test do we add?" Add a regression test for every escape.

**The goal is not zero bug escapes — it is continuous improvement of the escape rate over time.**

---

**Q29: A developer marks your bug as "not a bug" — what do you do? Walk me through your exact steps.**

**A:** This is a very common scenario, and handling it professionally is a mark of a senior QA engineer.

**Step 1: Read the rejection reason with an open mind.**
Sometimes the developer has context you do not. They may know the behaviour was intentionally designed this way, or that you tested against an old version of the spec. Start by genuinely trying to understand their perspective.

**Step 2: Verify against the specification.**
Pull up the Acceptance Criteria of the parent Story, the design mockup, or the API contract. If the actual behaviour contradicts the spec, the bug is valid regardless of intent.

**Step 3: If the bug is valid — respond professionally with evidence:**
Add a comment like:
> "Thanks for the feedback. I've checked AC #3 of MYAPP-145 which states [exact quote]. The current behaviour [describe actual] does not meet this requirement. Could you review? I'm attaching the spec reference and a screen recording."

**Step 4: Have a direct conversation.**
If the comment does not resolve it, have a face-to-face or call conversation. Most "not a bug" conflicts are actually miscommunications about requirements — they rarely need escalation.

**Step 5: If genuine disagreement remains — escalate to the PM.**
The Product Manager owns the requirements. Present both perspectives neutrally: "Dev believes this is intended; I believe it violates AC #3. Can you clarify?"

**Step 6: If PM sides with Dev — update the AC.**
If the PM confirms the behaviour is intentional but the AC was written incorrectly, update the AC to match reality and close the bug. Then log a separate Story to update the documentation.

**What you never do:**
- Never close a valid bug under social pressure
- Never argue repeatedly without adding new evidence
- Never go over the PM's head without first giving them the chance to arbitrate

---

**Q30: How do you handle a bug you cannot reproduce? What are your steps?**

**A:** A bug that cannot be reproduced is still a bug — it just means the evidence gathering needs to be better. The correct approach is methodical, not dismissal.

**Step 1: Document everything immediately.**
Before the environment changes, capture: exact URL, current user account, any error message text, browser console errors (DevTools), network request/response (HAR file), timestamp, and what you were doing exactly when it occurred.

**Step 2: Retry under the exact same conditions.**
Try the same steps, same data, same environment, same time of day. Intermittent bugs are often timing or load-related.

**Step 3: Vary the conditions systematically.**
- Different browser or OS version
- Different user account (permission differences?)
- Different test environment (QA-1 vs QA-2)
- Different network (VPN vs direct)
- Different data (try fresh test data vs reused data)

**Step 4: Check logs and monitoring.**
Application logs, error tracking tools (Sentry, Datadog), and network traces often capture the error even when it is not visually reproduced.

**Step 5: File the bug anyway, clearly marked.**
Create the JIRA ticket with all captured evidence. Mark it with labels like `intermittent`, `cannot-reproduce`. Add the note: "Observed once — providing all available evidence. Cannot reproduce reliably."

**Step 6: Set up monitoring.**
For intermittent bugs, add them to your exploratory testing checklist. Next time you are in that area of the app, specifically try to trigger it.

**Step 7: Collaborate with the developer.**
Share your evidence — log snippets, recordings, network traces. Intermittent bugs are often race conditions or timing bugs that developers can identify from log patterns even without a live reproduction.

**Never say "it must have been a one-off."** If you saw it happen, it happened. The question is what conditions caused it.

---

## SECTION 9 — INTERVIEW QUESTIONS

---

**Q31: Walk me through how you write a perfect bug report. What makes it "perfect"?**

**A:** A perfect bug report allows any developer — including one who has never seen the feature — to reproduce, understand, and fix the defect without asking a single follow-up question.

It has: a Summary using [Area] [Issue] [Condition] formula, numbered Steps to Reproduce starting from a clean known state with exact test data, an Expected Result quoting the AC or spec, an Actual Result with exact error codes and messages, the Environment details, Severity, visual evidence (screenshot/recording), and links to the parent Story.

The test is simple: hand the report to a developer who has never worked on this feature. If they can reproduce the bug in 5 minutes without asking you anything, the report is perfect.

---

**Q32: What is the difference between severity and priority? Give me one example where they are opposite.**

**A:** Severity = how bad the technical impact is (set by QA). Priority = how urgently the business needs it fixed (set by PM).

Classic opposite example: A crash in the bulk import tool used by 2 internal users once a quarter. Severity is Critical (it's a crash). Priority is P4 (almost no business impact, module being deprecated). Conversely, an old logo still showing on the homepage is Low Severity (cosmetic) but P1 Priority if the CEO demos the app at the rebrand launch in 4 hours.

---

**Q33: What does the defect lifecycle look like? Which statuses are unique to QA?**

**A:** New → Open → In Progress → Fixed → Ready for Retest → Retest → Closed (or Reopened).

QA owns: New (we create it), Retest (we verify it), Closed (we confirm it), Reopened (we trigger it when the fix fails verification). Additional paths: Rejected, Duplicate, Deferred, Cannot Reproduce, Won't Fix.

The most important QA-owned transitions are: moving to Closed only after verification with evidence, and Reopening with a detailed comment and new evidence explaining exactly what was retested and what failed.

---

**Q34: What is JQL and write me a query to find all high-priority bugs in the current sprint that are not yet fixed.**

**A:** JQL (JIRA Query Language) is JIRA's search syntax for filtering and reporting on issues.

Query:
```jql
project = "MYAPP"
  AND issuetype = Bug
  AND sprint in openSprints()
  AND priority in (P1, P2)
  AND status not in (Fixed, Closed, Done)
ORDER BY priority ASC, created ASC
```

This shows all P1 and P2 bugs in the active sprint that have not yet been fixed — exactly the list you would review in a stand-up to assess sprint health.

---

**Q35: How do you track test coverage for a sprint release using JIRA?**

**A:** Through Zephyr Scale's traceability features: every Story in scope must have test cases linked to it, every test case must be in the sprint Test Cycle, and every test case must be executed with a recorded status (Pass/Fail/Blocked).

For release readiness I check: (1) JQL showing any stories with no linked test cases, (2) Zephyr Scale cycle report showing execution percentage and pass rate, (3) JQL showing open P1/P2 bugs in the fix version. When all stories have test coverage, the pass rate meets the exit criteria (e.g., 95%+), and no critical bugs are open, the release is testable.

---

**Q36: What is the difference between a Kanban board and a Scrum board from a QA perspective?**

**A:** In Scrum, QA works to a sprint commitment — you must test all stories before the sprint ends. Bugs found late in the sprint may not get fixed in the same sprint, which is a planning failure. QA needs to test stories as soon as they are "Ready for QA" — not wait until the last day.

In Kanban, testing is continuous — there is no sprint deadline pressure. QA pulls work as capacity allows. The risk is work piling up if developers produce faster than QA can test — WIP limits prevent this.

Both boards share the same goal for QA: zero stories in "Done" with open critical bugs linked to them.

---

**Q37: How do you handle it when the sprint is almost over and you have not finished testing all stories?**

**A:** Escalate immediately — do not wait for the last day. As soon as you see that testing will not complete in time, raise it in the daily stand-up.

Steps:
1. Triage: which stories are highest risk if untested? Complete those first.
2. Log a blocker comment on stories that will carry over — "Testing not started due to late availability. Will carry to next sprint."
3. Agree with the PM which stories can carry over and which must be completed before release
4. Never move a story to "Done" without completing testing — the board should reflect reality, not wishful thinking
5. Run a post-sprint retrospective item: "Why did stories arrive in Ready for QA too late?" — this is usually a process problem, not a QA speed problem

---

**Q38: What is a regression bug and how do you handle it in JIRA?**

**A:** A regression bug is a defect in previously working functionality — a working feature broke because of a recent code change.

How to handle it in JIRA:
1. Add the label `regression` to the bug
2. Note in the description: "This functionality was working correctly in v2.3.1. Regression introduced in v2.4.0."
3. Link it to the Story or PR that introduced the change if identifiable
4. Raise severity/priority — regression bugs indicate quality degradation in tested code, which is more alarming than a new bug in a new feature
5. Track regression rate as a metric: if regression bugs are trending up, the automated regression suite needs to be expanded or the PR review process needs to include more QA involvement

---

**Q39: What is a bug escape and what would you do if one happened?**

**A:** A bug escape is a defect that passed through QA and was found in production by real users. It is one of the most important quality metrics to track.

Immediate response:
1. Assess severity — is this causing data loss, outages, or blocking users? If Critical, trigger the incident response process
2. Verify and reproduce — confirm it is the same bug or a new variant
3. Log it in JIRA with high priority and "production" label
4. Alert the team

Root cause analysis (after immediate response):
1. Why was this not caught in QA? Was there a test case for this area?
2. Was the test case executed in the last test cycle?
3. Was the testing environment missing something production has?
4. Was this a regression from a recent code change that was not tested?

Process improvement:
1. Write a test case covering the exact scenario
2. Add it to the automated regression suite
3. Retrospective: update the exit criteria or test coverage requirements to prevent recurrence

The correct response is blameless — focus on the process gap, not the person.

---

**Q40: How do you use sub-tasks in JIRA to make testing effort visible in a sprint?**

**A:** Testing work is invisible if it is all lumped inside a Story's estimate. Sub-tasks make testing effort explicit, trackable, and assignable.

For every Story, I create sub-tasks:
- "Write test cases — [Story name]" (2 points, QA)
- "Execute test cases — [Story name]" (1 point, QA)  
- "Cross-browser/device testing — [Story name]" (1 point, QA if required)
- "Automate regression tests — [Story name]" (3 points, QA if required)

Benefits: the sprint board shows accurate QA progress. Developers can see that a Story is in "In QA — test cases written" vs "In QA — executing". The PM can see testing effort in sprint planning and not under-estimate stories. Velocity is tracked more accurately because testing sub-tasks carry their own story points.

A story should not be marked Done while any QA sub-task is still In Progress — JIRA can enforce this with a workflow rule.

---

**Q41: What questions do you ask during backlog grooming to ensure a story is testable?**

**A:** I ask questions that expose gaps in the Acceptance Criteria before the story enters the sprint — when it is cheap to fix them.

**Questions I always ask:**
- "What should happen if [edge case]?" — e.g., "What if the user submits an empty form? What if the API returns an error? What if there are zero search results?"
- "Are there any performance requirements?" — "Should this load in under 2 seconds?"
- "Which browsers and devices must this be tested on?"
- "What are the field validation rules?" — min/max length, allowed characters, error messages
- "What is the expected behaviour when the user has no permissions for this action?"
- "Is there test data in the QA environment for this scenario?"
- "Does this story depend on another story or external API that may not be ready?"
- "What is the error state? What does the user see when something goes wrong?"
- "Is there a mockup or design for all states — loading, empty, error, populated?"

A story without answers to these questions is not ready for sprint. Blocking it in grooming prevents a week of ambiguity, mid-sprint blockers, and half-tested stories at sprint end.

---

**Q42: What would you add to your JIRA workflow that most QA engineers miss?**

**A:** Most QA engineers only use JIRA reactively — they log bugs and move cards. Senior QA engineers use JIRA proactively:

1. **Save JQL filters as dashboards.** A QA dashboard with gadgets for: Open P1/P2 bugs, Ready for Retest queue, Stories with no test cases, and Sprint progress — visible at a glance every morning.

2. **Trend analysis.** Bug count per sprint over time. If it is going up, something is wrong in the process. If it is going down, automation is working.

3. **Component-level bug analysis.** Which component generates the most bugs? That tells you where to focus exploratory testing and where automation is weakest.

4. **Reopen rate.** If a bug is reopened more than once, it signals either poor developer testing or unclear QA verification criteria.

5. **Time-in-status.** How long do bugs sit in "Ready for Retest" before QA picks them up? High wait times indicate QA capacity problems.

6. **Escape rate tracking.** Count production bugs vs total bugs per release. A rising escape rate is an early warning signal that the test suite or process is degrading.

7. **Pre-sprint AC audit.** Run the "stories with empty AC" JQL before sprint planning and ensure every story entering the sprint has testable criteria.

These proactive uses of JIRA separate a quality engineer who tracks bugs from a quality engineer who drives quality improvement.

---

*End of JIRA & Defect Management Complete Q&A Guide — 42 Questions*
