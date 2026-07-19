# Manual Testing — Complete Interview Q&A Guide

> Senior QA Interview Preparation | Every concept answered as a real interview question

---

## SECTION 1 — What is Manual Testing?

**Q1: What is manual testing? Is it less valuable than automation? Give a strong answer.**

**A:** Manual testing is the process of a human tester executing test cases and investigating software behaviour without using an automated tool to drive the application. The tester acts as a real user: navigating the interface, entering data, observing outcomes, and exercising judgement about whether the system works correctly.

Manual testing is absolutely not less valuable than automation. That framing is a false dichotomy. Automation and manual testing solve different problems, and neither can replace the other.

Here is what manual testing does that automation cannot:

- **Exploratory judgement**: A human notices when something "feels wrong" even if it is technically passing. A misaligned button, a confusing error message, a flow that is technically correct but utterly confusing to a real user — automation cannot evaluate any of these.
- **Usability and UX evaluation**: Can a real user complete their goal comfortably? Automation can only verify that a button exists and a form submits — not that the experience is intuitive.
- **Adaptation**: An exploratory tester changes direction mid-session when they discover something unexpected. An automation script follows its script rigidly and cannot follow a thread.
- **UAT**: Business users validating that software meets real-world requirements must be done by humans. Automation can check logic but not meaning.
- **One-off scenarios**: Writing and maintaining an automated test for a scenario that will only run twice has negative ROI.
- **Accessibility with screen readers**: Whether a screen reader experience makes sense requires a human to judge.

Automation has its place: regression packs, smoke suites, data-driven API checks, CI/CD gates. But those automation benefits depend on manual testing having already done the discovery, boundary exploration, and specification of what correct behaviour looks like.

A strong QA engineer uses both strategically — they do not treat automation as inherently superior.

---

**Q2: When do you choose manual testing over automation? Give a decision framework.**

**A:** The decision comes down to five factors: frequency of execution, stability of the feature, type of judgement required, ROI, and requirements clarity.

| Factor | Choose Automation | Choose Manual |
|--------|------------------|---------------|
| Frequency | Runs hundreds of times (regression, CI) | Runs once or twice |
| Stability | UI and behaviour are stable | Feature is new or changing every sprint |
| Judgement | Logic is precise and rule-based | Requires human interpretation or UX eval |
| ROI | Automation cost breaks even after N runs | Never breaks even |
| Requirements | Requirements are clear and stable | Requirements are vague, missing, or evolving |
| Discovery | Already know what to test | Need to discover what the system does |

**Automate:** Regression suites, smoke packs, API contract checks, data-driven test combinations, CI gates.

**Keep manual:** New feature first-pass testing, exploratory sessions, UAT, usability evaluation, one-off edge cases, visual and layout verification, accessibility with screen readers.

My rule: if I'm running it more than ten times and it isn't going to change, it is a candidate for automation. If I need to think and adapt while testing, it is manual.

---

## SECTION 2 — Types of Manual Testing

**Q3: What is smoke testing? Give a real example with pass/fail criteria.**

**A:** Smoke testing is a shallow, wide test pass immediately after receiving a new build to verify that the application is stable enough to test further. It covers only the most critical, highest-level paths — not deep functional scenarios. Think of it as answering the question: "Is this build even worth investing QA time in?"

The term comes from electronics: you power on a circuit board for the first time and check whether it smokes. If it does, you stop. If it doesn't, you move on to proper testing.

**When:** Immediately after a new build is deployed to the test environment, before any other testing.

**Goal:** Go/no-go on the build. Not to find all bugs — just to confirm the critical skeleton of the application is intact.

**Real example — E-learning platform, new build received:**

| Smoke Test Step | Pass Criterion | Fail Consequence |
|----------------|---------------|-----------------|
| Application loads at the URL | Home page renders, no 500 error | Build rejected |
| Login page is reachable | `/login` loads, email and password fields display | Build rejected |
| Login with valid credentials | User reaches dashboard | Build rejected |
| Main navigation is functional | Each nav item loads its target page | Build rejected |
| Core feature accessible | Student can open a course lesson | Build rejected |

If any of these five fail, the build is returned to development. No further testing is done. Writing a defect report and continuing testing on a broken build wastes QA time.

**Pass rate target:** 100%. Smoke tests do not fail gracefully.

---

**Q4: What is sanity testing? How is it different from smoke testing?**

**A:** Sanity testing is a narrow, focused test on a specific fix or new functionality to confirm that the fix has resolved the reported issue and has not broken anything immediately adjacent.

Where smoke testing is wide and shallow (covers the whole application, briefly), sanity testing is narrow and deep (covers only the specific fix area, thoroughly). A useful comparison:

| Aspect | Smoke Testing | Sanity Testing |
|--------|--------------|---------------|
| **Scope** | Whole application | Just the affected feature or fix |
| **Depth** | Shallow — 1-2 scenarios per area | Deep — multiple scenarios in one area |
| **When** | After any new build arrives | After a specific bug fix is delivered |
| **Goal** | Is this build stable enough to test? | Did this fix actually work? |
| **Duration** | 15–45 minutes | 5–20 minutes |
| **Written by** | QA team | QA team |

**Real example:**

Bug reported: "Login fails when the user's email contains a plus sign (e.g. user+tag@example.com)"

Bug fix delivered. Sanity test:

1. Log in with `user+tag@example.com` — must succeed (the original defect scenario)
2. Log in with `user+work+home@example.com` — must succeed (variation)
3. Log in with `user+@example.com` — must succeed (edge case)
4. Log in with standard email `jane@example.com` — must still succeed (regression check on adjacent behaviour)
5. Confirm session token is created and dashboard loads

If steps 1-3 pass, the fix is confirmed. Step 4 confirms the fix did not break normal login.

Only if the sanity check passes do we move to full regression. If it fails, the fix is returned to the developer immediately — no point regressing when the primary scenario still fails.

---

**Q5: What is regression testing? When do you run it?**

**A:** Regression testing is a comprehensive test pass to verify that existing, working functionality has not been broken by new code changes. It answers the question: "Did we break anything that was previously working?"

Every code change — no matter how small — has the potential to introduce unintended side effects. Regression testing is the safety net that catches those side effects before they reach production.

**When to run:**
- After any bug fix is delivered and sanity-checked
- After any new feature is merged into the codebase
- After a refactor or infrastructure change
- Before every production release
- After any dependency upgrade (framework, library, API version)

**What to include:**

A risk-based regression suite typically includes:
1. All previously failing tests that are now fixed (confirm they stay fixed)
2. All tests covering code that was recently modified
3. All tests covering features that share code with what changed
4. All tests for core business flows (the most critical user journeys)
5. All tests covering historically fragile areas

**Real example:**

New feature: "Remember Me" checkbox added to login form.

Regression scope:
- All login scenarios (valid, invalid, locked, expired password, forgot password)
- Session management tests (timeout, persistence, concurrent sessions)
- All features that read from the session (profile, dashboard, settings)
- SSO and OAuth flows if applicable (they share authentication logic)

Even though only the login form changed, anything that touches the session needs to be regressed because "Remember Me" modifies how sessions work.

**Automation note:** Regression is the single best candidate for automation. It is repetitive, stable, well-defined, and run frequently. A strong QA team automates the regression suite and runs it on every CI build.

---

## SECTION 3 — The Critical Question: Smoke vs Sanity vs Regression

**Q6: CRITICAL — What is the difference between smoke testing, sanity testing, and regression testing? (Table + real examples from one feature)**

**A:** This is one of the most frequently asked QA interview questions. The three types address three different questions at three different moments in the test cycle.

| Aspect | Smoke Testing | Sanity Testing | Regression Testing |
|--------|--------------|---------------|--------------------|
| **Question answered** | Is this build testable? | Did this specific fix work? | Did anything previously working break? |
| **Scope** | Wide — whole application | Narrow — one specific area | Wide — whole application or risk-assessed subset |
| **Depth** | Shallow | Deep (within its narrow scope) | Deep |
| **When executed** | After every new build arrives | After a specific bug fix | After any code change, before every release |
| **Who triggers it** | New build deployment | Bug fix delivery | Code merge or release |
| **Pass criteria** | All critical flows are functional | The reported defect is resolved | All previously passing tests still pass |
| **Fail consequence** | Build rejected, returned to dev | Fix returned to developer | Release blocked pending investigation |
| **Duration** | Minutes to 1 hour | 5–20 minutes | Hours to days |
| **Test count** | 5–20 critical scenarios | 5–10 targeted scenarios | Full suite (hundreds of tests) |
| **Dependency** | None — first test type | Depends on smoke passing | Depends on sanity passing |

**Real example — all three using the login feature:**

Scenario: A new build is received that includes a fix for the bug "Login 500 error when email contains an apostrophe" plus a new "Remember Me" feature.

**Step 1 — Smoke Test (is the build testable?):**
1. Navigate to `/login` — page loads
2. Enter valid credentials — user reaches dashboard
3. Navigate to main sections — all load without error

Result: Build is stable. Proceed to sanity testing.

**Step 2 — Sanity Test (did the apostrophe fix work?):**
1. Log in with `o'brien@example.com` — now succeeds (defect was 500 error)
2. Log in with `mc'tavish+work@example.com` — succeeds
3. Log in with `jane@example.com` — still succeeds (no regression on normal case)

Result: Fix is confirmed. Proceed to regression.

**Step 3 — Regression Test (did anything break?):**
1. Run full login test suite: valid, invalid, locked, expired, forgot password
2. Run session management tests: timeout, persistence across browser restart (new for Remember Me)
3. Run all tests that interact with the auth service
4. Run SSO tests
5. Run tests for all pages that check session state

Result: If all pass → go/no-go decision. If any fail → investigate, raise defects, assess impact.

**The sequence:** Smoke → Sanity → Regression. Each gate must pass before the next begins.

---

## SECTION 4 — UAT

**Q7: What is UAT? What is QA's role in it?**

**A:** UAT — User Acceptance Testing — is the final validation gate before software goes to production. It answers the question: "Does this software meet what the business agreed to build, from the perspective of the people who will actually use it?"

UAT is performed by business users, product owners, or real end users — not by the QA team. The QA team has already signed off on functional quality before UAT begins. UAT adds a layer of business and domain validation that technical testing cannot provide.

**Why UAT is distinct from QA testing:**

QA verifies that software works correctly according to specifications. UAT verifies that specifications correctly captured what the business actually needs. These are different questions. A checkout flow might be perfectly functional but completely unusable by the client's finance team because the terminology is wrong, the date format doesn't match their country, or the workflow doesn't match their approval process. Only the actual users catch that.

**QA's role in UAT:**

| QA Responsibility | What It Involves |
|------------------|-----------------|
| Write UAT test scripts | Plain-language acceptance test scripts business users can follow without technical knowledge |
| Prepare the environment | Ensure the UAT environment is stable, correct build version, smoke tested |
| Set up test data | Real-world representative data — school names, student records, real date formats |
| Brief participants | Explain how to use the defect tracker, how to write up an issue |
| Support during UAT | Answer questions about expected behaviour, investigate anomalies, clarify if something is a bug or by design |
| Log defects | Raise any issues found in the defect tracker with full steps to reproduce |
| Track progress | Monitor test completion, flag outstanding scenarios |
| Facilitate sign-off | Prepare the UAT summary report and sign-off documentation |

**What QA does NOT do in UAT:**
- Execute the UAT tests (business users do that)
- Make the go/no-go decision (business stakeholders do that)
- Accept known issues on the business's behalf

**UAT entry criteria — UAT cannot begin until:**
- QA has completed its test cycle and signed off
- All P1 and P2 defects from the QA cycle are closed
- UAT environment has passed a smoke test
- Test data is in place and realistic
- UAT participants are confirmed and available
- UAT test scripts are written and reviewed

**UAT exit criteria — UAT is complete when:**
- All planned UAT scenarios are executed
- All P1 defects found during UAT are resolved and re-tested
- Business stakeholders have formally signed off
- Known outstanding issues are documented, accepted by the business, and deferred or scheduled

---

## SECTION 5 — Exploratory Testing

**Q8: What is exploratory testing? How is it different from ad-hoc testing?**

**A:** Exploratory testing is a structured, simultaneous approach to learning, test design, and test execution. The tester uses their domain knowledge, curiosity, and heuristics to investigate the system, adapting what they test based on what they discover as they go.

The key word is "simultaneous". In scripted testing, test design happens before execution. In exploratory testing, design and execution happen at the same time — the tester's observations in minute 20 inform what they test in minute 30.

**What makes exploratory testing NOT the same as ad-hoc testing:**

| Aspect | Exploratory Testing | Ad-hoc Testing |
|--------|--------------------|--------------------|
| **Structure** | Time-boxed sessions with a written charter | No structure at all |
| **Goal** | Specific mission defined before the session | No defined goal |
| **Documentation** | Notes taken throughout; defects logged; debrief produced | Nothing documented |
| **Reproducibility** | Another tester could repeat the session using the notes | Cannot be reproduced |
| **Skill level** | Requires domain knowledge and test heuristics | Relies only on intuition |
| **Reporting** | Session report communicated to the team | Nothing reported |
| **Value** | High — structured discovery with audit trail | Limited — depends entirely on that tester's intuition on that day |

**When to use exploratory testing:**
- New features before scripted tests are written
- Unclear or missing requirements (explore to discover what the system does)
- After a major refactor (check for unexpected side effects)
- When scripted tests pass but something "feels off"
- End-to-end user journey verification
- Accessibility and usability investigation

**When ad-hoc is acceptable:**
- A developer hands you something very new and asks for a quick five-minute first impression
- Extremely time-pressured situations as a first pass

Ad-hoc should never be the primary testing strategy. If you are testing without a charter and without taking notes, you are not producing repeatable, auditable quality assurance — you are guessing.

---

**Q9: What is session-based exploratory testing? How do you write a charter?**

**A:** Session-Based Exploratory Testing (SBET) is the industry-standard framework for structuring exploratory testing. It was defined by Jonathan and James Bach. The framework adds accountability and repeatability to exploratory testing while preserving the freedom to follow discoveries.

**The four components of every SBET session:**

1. **Charter** — A mission statement that defines the scope and goal of the session
2. **Time box** — A fixed duration (typically 45–90 minutes)
3. **Notes** — A running log of what you did, what you found, what questions arose
4. **Debrief** — A summary of discoveries, outstanding questions, and ideas for follow-up sessions

**Charter formula:**

```
Explore [target area of the application]
Using [technique, approach, or perspective]
To discover [risks, issues, or information you are looking for]
```

**Example charters:**

- "Explore the login form using boundary values, special characters, and timing to discover input validation gaps and authentication edge cases"
- "Explore the student enrolment flow as a new school administrator to discover usability barriers and unclear language"
- "Explore the PDF export feature using oversized, empty, and malformed documents to discover error handling and system limits"
- "Explore the admin dashboard using keyboard-only navigation to discover accessibility gaps"
- "Explore the checkout flow using concurrent browser tabs to discover session and state management issues"

**What makes a good charter:**
- Specific enough to keep the session focused
- Broad enough to allow genuine exploration
- Contains an explicit "to discover" clause — this is the hypothesis driving the session
- Does NOT prescribe specific steps (that would make it a scripted test, not exploratory)

**Time box discipline:**

A time box prevents sessions from becoming open-ended. When the box expires:
- Write up your notes
- Log any defects
- Write the debrief
- Decide whether a follow-up session is needed

A typical exploratory session is 60 minutes of actual testing, plus 15 minutes for documentation.

---

**Q10: Walk through a complete 60-minute exploratory testing session on a login feature.**

**A:** Here is a complete session with charter, notes, and debrief.

---

**Charter:** "Explore the login form using boundary conditions, special characters, timing, and error handling to discover authentication vulnerabilities and input validation gaps"

**Tester:** QA Engineer
**Date:** Current sprint
**Environment:** Test environment v2.4.1
**Time box:** 60 minutes

---

**[0:00–0:10] Initial observation and mapping**

Navigated to `/login`. Observed: Email field, Password field, "Log In" button, "Forgot Password?" link, "Sign Up" link. No CAPTCHA visible. The password field shows a character counter while typing — noted: this leaks password length to a shoulder surfer or camera.

**[0:10–0:25] Input boundary testing**

- 1-character email: `a` → Submitted → "Please enter a valid email" (expected, good)
- Valid 254-character email (RFC 5321 max): Accepted → returned 401 (expected)
- 255-character email: Accepted and submitted → **ANOMALY: no max length enforced. Is this intentional?**
- Email with no TLD: `user@example` → Form submitted → **POTENTIAL DEFECT: client validation should reject this before submission**
- Email with double `@`: `user@@example.com` → "Invalid email" shown (good)
- Empty email + valid password → "Email is required" shown (good)
- Valid email + empty password → "Password is required" shown (good)

**[0:25–0:40] Special characters in inputs**

- Email `o'brien@example.com` → **DEFECT raised: application returns HTTP 500. Apostrophe is not sanitised. Possible SQL injection vector. Logged as DEFECT-4421 — Critical.**
- Email `user+tag@example.com` → Login succeeded (good, plus addressing is valid per RFC)
- Email with spaces `user @example.com` → Form strips the space, submits `user@example.com` → **QUESTION: Is this trimming intended? Could allow a user to bypass unique email constraint.**
- Password `P@$$w0rd!{}[]` → Logged in successfully (special characters accepted, good)
- Password with only spaces `     ` → Form accepted and submitted → **POTENTIAL DEFECT: whitespace-only password should be rejected**
- Password with null byte in browser console injection → Server returns 400 (handled correctly)

**[0:40–0:53] Timing and state**

- Rapid double-click on "Log In" button → **DEFECT raised: two identical POST requests sent to `/api/login`. Server creates two sessions. Button should disable after first click. Logged as DEFECT-4422 — Medium.**
- Opened two browser tabs, logged in on both simultaneously → Second login invalidated the first session → confirmed session management policy (single session)
- Left the login form open for 30 minutes, then submitted → Login succeeded → **QUESTION: Is there a CSRF token expiry? Is the token validated server-side?**
- Submitted during slow 3G throttle (DevTools) → Spinner appeared, then 504 Gateway Timeout → Error state visible — "Something went wrong". No retry option. **USABILITY NOTE: could tell the user to try again.**

**[0:53–1:00] Documentation**

**Defects raised this session:**
- DEFECT-4421: HTTP 500 on login with apostrophe in email (Critical — possible SQL injection)
- DEFECT-4422: Double-submission of login request on rapid button click (Medium — duplicate sessions)

**Anomalies requiring follow-up:**
- 255+ character email accepted (no max length validation)
- Whitespace-only password accepted by form
- Space trimming in email — is this documented behaviour?

**Open questions for the team:**
- Is the CSRF token validated server-side? (Needs security team confirmation)
- What is the account lockout threshold? (Need to test without risking locking real test accounts)
- Is showing password character count during typing intentional?

**Ideas for next session:**
- Test account lockout: how many failures, how long does the lockout last, is there a notification?
- Test "Forgot Password" flow: link expiry, reuse of used links, multiple active reset links
- Test session behaviour after password change: are existing sessions invalidated?

---

## SECTION 6 — Ad-hoc, UI/UX, Cross-Browser, Compatibility, Localisation

**Q11: What is ad-hoc testing? When is it appropriate?**

**A:** Ad-hoc testing is unstructured, undocumented testing based entirely on the tester's intuition. No charter, no notes, no defined goal, no formal defect logging during the session. The tester opens the application and clicks around, trying to break things based on experience.

**When it is appropriate:**
- A developer hands you a brand-new feature and asks for a five-minute first impression
- You want to familiarise yourself with a completely new area before writing a test charter
- You have five minutes at the end of a test cycle and want to do a quick gut-check

**When it is NOT appropriate:**
- As the primary testing strategy for a feature
- In place of structured exploratory testing
- When you need to demonstrate test coverage to a manager or auditor
- When findings need to be repeatable or traceable

**The core limitation of ad-hoc testing:** If you find a defect, you need to go back and reconstruct your steps to reproduce it. That often fails. If you do not find a defect, you have no record of what you tested. Ad-hoc produces no coverage evidence.

Use ad-hoc for orientation, then move to exploratory testing (with a charter and notes) for actual coverage.

---

**Q12: What is UI/UX testing? What specifically do you check?**

**A:** UI/UX testing evaluates whether the visual design, layout, interaction patterns, and overall user experience are correct, consistent, and usable. It is distinct from functional testing — UI/UX testing is not asking "does this submit button work?" but "is it clear to a user what this button will do, and can they reach it comfortably?"

**What to check in UI testing:**

**Visual correctness:**
- Does the UI match the approved design mockup or Figma spec?
- Are font sizes, weights, and families correct?
- Are colours consistent with the design system?
- Are spacing and padding correct (especially on cards, modals, forms)?
- Are icons the correct size and positioned correctly?

**Layout and responsiveness:**
- No overlapping elements at any supported screen size
- No horizontal scroll on mobile viewports
- Text does not truncate or overflow its container
- Images maintain correct aspect ratio and are not distorted

**Usability:**
- Do button labels clearly communicate what will happen?
- Is the tab order logical (top-to-bottom, left-to-right)?
- Are error messages displayed adjacent to the relevant field, not at the top of the form?
- Are required fields clearly marked?
- Are loading states shown when the system is working?
- Are empty states handled (what does a first-time user see with no data)?

**Interaction:**
- Hover states on interactive elements (buttons, links, cards)
- Disabled states look visually distinct from enabled
- Active/selected states are clearly indicated
- Animations are smooth and do not flash

**Mobile-specific:**
- Touch targets are minimum 44x44px (Apple guideline) or 48x48px (Material guideline)
- Keyboard appears when an input is tapped
- Dropdowns use the native mobile picker
- Swipe gestures work correctly

---

**Q13: What is cross-browser testing? What commonly breaks between browsers?**

**A:** Cross-browser testing verifies that the application looks and behaves consistently across different web browsers and browser versions. The same HTML, CSS, and JavaScript can render differently in Chrome, Firefox, Safari, and Edge because each uses a different rendering engine (Blink, Gecko, WebKit).

**Browsers to cover (minimum):**
- Chrome (Windows and macOS)
- Firefox (Windows and macOS)
- Safari (macOS and iOS — WebKit)
- Edge (Windows)
- Chrome on Android (mobile Chrome)
- Safari on iOS (all iOS browsers use WebKit underneath)

**What commonly breaks between browsers:**

| Issue | Which Browsers | Detail |
|-------|---------------|--------|
| `<input type="date">` | Safari | Safari does not render a date picker UI — shows a plain text field |
| CSS Grid gaps | Older Safari | Older iOS Safari has partial Grid support |
| `position: sticky` | Older Safari | Needs `-webkit-sticky` prefix in older versions |
| `<dialog>` element | Safari 15.3 and earlier | Not supported — needs a polyfill |
| WebP images | Safari 13 and earlier | Not supported — falls back to blank |
| `aspect-ratio` CSS | Safari 14 and earlier | Not supported |
| CORS fetch behaviour | Safari | Some stricter CORS enforcement |
| Font rendering | All | Anti-aliasing differs — fonts look slightly thicker on macOS |
| Scroll behaviour | Safari | Momentum/rubber-band scrolling behaves differently |
| Form autofill styling | All | Browsers apply different autofill background colours |
| `focus-visible` | Older browsers | Fallback to always-visible focus ring |

**Key insight:** Safari on iOS is always the highest-risk browser. Apple requires all iOS browsers (including Chrome on iPhone) to use Safari's WebKit engine under the hood. So "Chrome on iPhone" is functionally Safari. Test iOS Safari as a priority.

**Tools for cross-browser testing:**
- BrowserStack — real devices in the cloud, automated and live manual testing
- Sauce Labs — similar, strong CI integration
- LambdaTest — more affordable option for teams
- Chrome DevTools device mode — useful for layout but is not a real device; verify findings on real hardware before release

---

**Q14: What is compatibility testing?**

**A:** Compatibility testing verifies that an application works correctly across combinations of operating systems, devices, hardware configurations, screen resolutions, and software versions. It is broader than cross-browser testing — cross-browser is one dimension of compatibility.

**Dimensions of compatibility testing:**

| Dimension | What to Test |
|-----------|-------------|
| Operating system | Windows 10, Windows 11, macOS Ventura, macOS Sonoma |
| Browser versions | Current and one-previous version of each major browser |
| Device types | Desktop, laptop, tablet, phone |
| Screen resolutions | 1920x1080, 1366x768, 2560x1440, 375x812 (mobile) |
| Mobile OS | Android 12/13/14, iOS 16/17 |
| Network | WiFi, 4G, 3G, offline |
| Assistive tech | Screen readers, high contrast mode, large text system settings |

**Real example:** An EdTech platform must work on:
- School-issued Windows laptops running Chrome (corporate managed)
- Teacher MacBooks running Safari
- Students' Android phones running Chrome
- iPads running Safari (school-issued)
- Low-bandwidth rural connections

Each combination can expose different issues.

---

**Q15: What is localisation testing? What specifically do you check?**

**A:** Localisation testing (l10n testing) verifies that the application is correctly adapted for specific languages, regions, date formats, number formats, currency formats, and cultural conventions. It is different from internationalisation (i18n) testing — internationalisation is verifying that the application is built to support multiple locales; localisation tests whether a specific locale is correctly implemented.

**What to check in localisation testing:**

**Date formats:**
- `01/12/2024` — is this January 12 (US) or December 1 (UK/AU)?
- ISO 8601: `2024-12-01` (safest for international use)
- Does the calendar start on Sunday (US) or Monday (UK/EU)?
- Are date pickers region-aware?

**Number and currency formats:**
- `1,000.00` (US) vs `1.000,00` (Germany, Brazil)
- `$1,000` vs `€1.000` vs `£1,000`
- Decimal separators must match the locale

**Text and translation:**
- Are all strings translated? (Check for English strings remaining in a non-English locale)
- Are translations grammatically correct?
- Are translated strings too long for UI containers? (German words can be 30-40% longer than English equivalents)
- Are RTL languages (Arabic, Hebrew, Farsi) mirroring the layout correctly? (Navigation, text direction, icon placement all flip)

**Content and culture:**
- Are phone number formats locale-appropriate?
- Are address formats correct? (Different countries have different address field orders)
- Are units metric or imperial?
- Are names handled correctly? (Some cultures use family name first)
- Are images and icons culturally neutral or region-appropriate?

**Technical:**
- UTF-8 encoding supporting special characters: é, ü, ñ, ي, 中文
- String truncation: does the UI handle longer translated strings without breaking layout?
- Pluralisation: some languages have more than two plural forms (e.g. Polish has four)

---

## SECTION 7 — Writing Test Cases

**Q16: What are all the fields in a test case? Explain what each one is for.**

**A:** A well-formed test case contains these fields:

| Field | Purpose |
|-------|---------|
| **Test Case ID** | Unique identifier that can be referenced in defect reports, test cycle reports, and traceability matrices. Format example: `TC-LOGIN-001`. Never use a sequential number alone — include a module prefix. |
| **Title** | A one-line summary of what is being tested. Must be specific enough to understand without reading the steps. Uses the title formula. |
| **Module / Feature** | The functional area of the application (Login, Cart, Checkout, Student Profile). Used for filtering and reporting. |
| **Priority** | P1 Critical / P2 High / P3 Medium / P4 Low. Drives test execution order. |
| **Severity** | Critical / High / Medium / Low. What is the business impact if this test fails? (Priority = urgency of testing; Severity = impact of failure.) |
| **Preconditions** | The exact state the system must be in before step 1. If preconditions are not met, the test is invalid. "User account exists with email X and password Y" or "User is not logged in" are examples. |
| **Test Steps** | Numbered, single-action, imperative-voice steps. Specific enough for someone unfamiliar with the feature to follow exactly. |
| **Test Data** | Exact input values required. Do not say "valid email" — say `jane.smith@example.com`. Consistency and reproducibility depend on specific data. |
| **Expected Result** | The precise, verifiable outcome after all steps are completed. What URL, what message, what state change, what API response, what database record. Not "it works". |
| **Actual Result** | Filled in during execution. What actually happened. |
| **Status** | Pass / Fail / Blocked / Not Executed. Updated during each test cycle. |
| **Author** | Who wrote the test case. Needed for questions and maintenance. |
| **Last Updated** | When the test case was last reviewed. Stale test cases against a changed feature are worse than no test case. |
| **Test Environment** | Where the test was executed (dev, test, staging, UAT). |
| **Automation Status** | Manual / Automated / Candidate for Automation. Useful for planning automation work. |

---

**Q17: What is the test case title formula? Show good and bad examples.**

**A:** The title formula is:

```
[Feature] [Action or Condition] [Expected Outcome]
```

Alternatively stated as:

```
[What is being tested] [Under what condition] [Should produce what result]
```

**Good titles:**

- "Login form authenticates user with valid email and password"
- "Login page displays error message when password is incorrect"
- "Forgot Password sends reset email to registered address within 5 minutes"
- "Student profile photo upload rejects files larger than 5MB"
- "Course search returns results filtered by subject when subject filter is applied"

**Bad titles — and why:**

| Bad Title | Problem |
|-----------|---------|
| "Test login" | What aspect of login? What condition? What is the expected outcome? Untestable. |
| "Verify user" | Means nothing to someone reading the test plan |
| "Check form" | Which form? What check? |
| "Login should work" | "Work" is not a defined, verifiable outcome |
| "TC001" | The ID is not the title — the title must be human-readable |
| "Login — negative scenario" | Which negative scenario? There are dozens of them. |

**The title test:** Read the title and ask — can I write the expected result without reading the steps? If yes, the title is good. If no, it is too vague.

---

**Q18: How do you write Steps to Reproduce? Show a perfect example.**

**A:** Steps to Reproduce (StR) in a defect report — and Steps in a test case — follow the same principles. Each step is a single, specific action that anyone can follow without interpretation.

**Rules:**
1. One action per step — never "click and verify" in the same step (verification belongs in Expected Result)
2. Use imperative verb: "Click", "Enter", "Navigate", "Select", "Wait for", "Observe"
3. Specify exact values, not "valid email" or "a file"
4. Order matters — every dependency must appear in sequence
5. Include the URL, page name, or element name to disambiguate
6. A non-technical reader should be able to follow the steps

**Bad steps:**
```
1. Login to the app
2. Go to profile
3. Upload a photo and check it works
```

Problems: "Login to the app" — with which credentials, at which URL? "Go to profile" — how? "Upload a photo and check it works" — two actions, and "works" is not defined.

**Perfect example — profile photo upload test case:**

```
Preconditions:
- User account exists: email = student@example.com, password = Test123!
- User is logged in and on the home page at https://app.example.com/dashboard
- Test file available: valid_profile.jpg (200x200px, 45KB, JPEG)
- Test file available: oversized_file.jpg (2000x2000px, 8MB, JPEG)

Steps:
1. Click the user avatar in the top-right navigation bar
2. Select "My Profile" from the dropdown menu
3. Verify the Profile page loads at /profile
4. Click the "Change Photo" button below the current profile picture
5. When the file chooser dialog opens, navigate to and select valid_profile.jpg
6. Click "Open" in the file chooser dialog
7. Verify a preview of valid_profile.jpg appears in the upload preview area
8. Click the "Save Photo" button
9. Observe the result

Expected Result:
- The new profile photo replaces the placeholder avatar on the Profile page
- A success notification appears: "Profile photo updated successfully"
- The user avatar in the top-right navigation also updates to show the new photo
- Navigating away and returning to /profile still shows the new photo (confirms persistence)
- HTTP response to the upload request is 200 OK
```

Every step is one action. Every element is named. Data is specified. The expected result is precise and verifiable.

---

**Q19: What is the difference between Expected Result and Actual Result?**

**A:**

**Expected Result** is written before execution. It describes precisely what should happen if the software is working correctly. It is derived from the requirements, acceptance criteria, design specifications, or — in the absence of documentation — reasonable user expectations. The expected result must be verifiable: it describes something you can observe, measure, or check.

Examples of precise expected results:
- "User is redirected to `/dashboard`. The page heading reads 'Dashboard'. The username 'Jane Smith' appears in the top-right navigation. HTTP response is 200 OK."
- "An error toast notification appears at the top of the screen with the message 'File size exceeds the 5MB limit. Please upload a smaller file.' The file upload field returns to its empty state."

Examples of imprecise expected results (do not use these):
- "It works" — how do you know it worked?
- "Login is successful" — what does successful look like?
- "Error is shown" — which error? Where? What does it say?

**Actual Result** is written during execution, only when the test fails. It documents exactly what the system actually did — the observable difference from the expected result. It must be specific enough to create a defect report from.

Examples of precise actual results:
- "User remains on the login page at `/login`. No error message is displayed. The console shows a 500 Internal Server Error. The network tab shows the POST to `/api/login` returned status 500."
- "No error message appears. The file is uploaded and the profile photo is replaced despite being 8MB. The upload limit is not enforced."

**The relationship:** The gap between Expected Result and Actual Result is the defect. The more precise both are, the more useful the defect report becomes for the developer fixing it.

---

## SECTION 8 — Complete Test Cases

**Q20: Show 3 complete test cases with all fields — login success, login failure, and forgot password.**

**A:**

---

**Test Case 1: Login — Successful Authentication**

| Field | Value |
|-------|-------|
| **ID** | TC-LOGIN-001 |
| **Title** | Login form authenticates user and redirects to dashboard with valid email and password |
| **Module** | Authentication / Login |
| **Priority** | P1 — Critical |
| **Severity** | Critical |
| **Author** | QA Engineer |
| **Preconditions** | 1. User account exists: email = `jane.smith@example.com`, password = `SecurePass123!` 2. User is NOT currently logged in 3. Application is accessible at `https://app.example.com/login` |
| **Test Data** | Email: `jane.smith@example.com` / Password: `SecurePass123!` |

**Steps:**
1. Open a browser and navigate to `https://app.example.com/login`
2. Verify the Login page loads with an Email field, Password field, and "Log In" button visible
3. Click the Email field and enter `jane.smith@example.com`
4. Click the Password field and enter `SecurePass123!`
5. Click the "Log In" button
6. Observe the result

**Expected Result:**
- User is redirected to `https://app.example.com/dashboard`
- The page heading reads "Dashboard"
- The top-right navigation shows "Jane Smith" with a user avatar
- A "Log Out" link or icon is visible in the navigation
- No error messages are displayed anywhere on the page
- Network tab: POST to `/api/login` returns HTTP 200 with a session token

**Actual Result:** *(Filled during execution)*
**Status:** *(Pass / Fail / Blocked)*

---

**Test Case 2: Login — Incorrect Password**

| Field | Value |
|-------|-------|
| **ID** | TC-LOGIN-002 |
| **Title** | Login form displays error message and remains on login page when incorrect password is entered |
| **Module** | Authentication / Login |
| **Priority** | P1 — Critical |
| **Severity** | High |
| **Preconditions** | 1. User account exists: email = `jane.smith@example.com` 2. User is NOT logged in 3. User is on the Login page |
| **Test Data** | Email: `jane.smith@example.com` / Password: `WrongPassword99!` |

**Steps:**
1. Navigate to `https://app.example.com/login`
2. Enter `jane.smith@example.com` in the Email field
3. Enter `WrongPassword99!` in the Password field
4. Click the "Log In" button
5. Observe the result

**Expected Result:**
- User remains on the Login page — URL stays at `/login`
- An inline error message is displayed: "Invalid email or password. Please try again."
- The Password field is cleared (empty)
- The Email field retains the value `jane.smith@example.com`
- No account lockout is triggered (this is the first failed attempt)
- Network tab: POST to `/api/login` returns HTTP 401 Unauthorized

**Actual Result:** *(Filled during execution)*
**Status:** *(Pass / Fail / Blocked)*

---

**Test Case 3: Forgot Password — Reset Email Delivery**

| Field | Value |
|-------|-------|
| **ID** | TC-LOGIN-003 |
| **Title** | Forgot Password sends a functional reset email to the registered address within 5 minutes |
| **Module** | Authentication / Forgot Password |
| **Priority** | P2 — High |
| **Severity** | High |
| **Preconditions** | 1. User account exists with email `jane.smith@example.com` 2. QA has access to the email inbox for `jane.smith@example.com` (use Mailinator or test inbox) 3. User is on the Login page at `/login` |
| **Test Data** | Email: `jane.smith@example.com` |

**Steps:**
1. Navigate to `https://app.example.com/login`
2. Click the "Forgot Password?" link below the password field
3. Verify the Forgot Password page loads with a single email input field
4. Enter `jane.smith@example.com` in the Email field
5. Click the "Send Reset Link" button
6. Observe the on-screen confirmation
7. Open the email inbox for `jane.smith@example.com`
8. Locate the password reset email
9. Note the time of receipt
10. Click the reset link in the email body

**Expected Result:**
- On-screen: "A password reset link has been sent to jane.smith@example.com. Please check your inbox."
- Email received: Subject contains "Reset your password" or "Password reset request"
- Email received: Within 5 minutes of clicking "Send Reset Link"
- Email sender: A no-reply address from the expected domain (e.g. noreply@example.com)
- Reset link in email navigates to the password reset form at `/reset-password?token=...`
- Password reset form allows entering and confirming a new password
- Reset link is single-use (using it a second time returns an "expired or invalid link" message)

**Actual Result:** *(Filled during execution)*
**Status:** *(Pass / Fail / Blocked)*

---

## SECTION 9 — Priority and Risk

**Q21: What is test case priority? How do you decide P1 vs P4?**

**A:** Priority defines the order in which test cases should be executed, based on the business risk of that area failing.

| Priority | Label | Meaning | Examples |
|----------|-------|---------|---------|
| **P1** | Critical | Must test first. Release blocker if it fails. Core business function is unavailable or data is at risk. | Login, payment processing, data save, security features, core feature functionality |
| **P2** | High | Important and should be tested before release. Significant user impact if broken but a workaround may exist. | Profile update, search, notifications, report generation, settings |
| **P3** | Medium | Should be tested but not a release blocker. Minor impact or workaround available. | Non-critical UI formatting, optional filters, secondary navigation |
| **P4** | Low | Test only if time permits. Minimal user impact. Cosmetic or edge case. | Tooltip text, minor visual alignment, low-traffic edge case messages |

**How to assign priority — the decision criteria:**

Ask these questions:
1. **If this fails in production, what is the impact?** Financial loss → P1. User cannot complete core task → P1. Minor annoyance → P3 or P4.
2. **How likely is this area to fail?** Recent changes → higher priority. Stable and unchanged → lower.
3. **How many users are affected?** All users hit this path → P1. 1% of users → P3.
4. **Is there a workaround?** No workaround → escalate priority. Easy workaround → lower priority.
5. **Is there regulatory or security impact?** Any security or compliance risk → P1 always.

**Note:** Priority and Severity are different things. Priority = when to test it. Severity = how bad is the impact if it fails. A cosmetic bug on the login page might be P2 (because it is on a high-traffic page and must be tested early) but Severity Low (it does not break functionality). A catastrophic bug on a rarely-visited admin page might be Severity Critical but Priority P3 (low traffic, test later).

---

**Q22: How do you prioritise when you have limited time before a release?**

**A:** This is a risk-based prioritisation problem. When time is shorter than the full test suite, the goal is to maximise risk coverage — test the things most likely to fail AND most damaging if they fail first.

**My framework:**

**Step 1 — Run smoke tests immediately.** Is the build even stable? If not, no point in anything else.

**Step 2 — Test what changed.** New code is highest risk. Run all tests for any features that were modified in this release, and any areas that share code with those features.

**Step 3 — Test the highest-traffic user journeys.** The core things every user does every day. Login, core feature, data save.

**Step 4 — Test all payment and security flows.** Non-negotiable. Financial and security failures have legal and reputational consequences.

**Step 5 — Test integration points.** Where two systems connect is where failures hide — APIs, third-party services, SSO, payment gateways.

**Step 6 — Test areas with a history of bugs.** If something has broken three times before, it is likely to break again.

**Step 7 — Apply risk scoring.** For each remaining area, score: Probability of failure (1–5) × Business impact if it fails (1–5) = Risk score. Test highest scores first.

```
Risk Score = Probability × Impact

Score 20–25 → Test immediately (P1)
Score 10–19 → Test before release (P2)
Score 5–9   → Test if time allows (P3)
Score 1–4   → Defer to next cycle (P4)
```

**What to formally defer:** Document anything not tested as a known risk. Communicate it clearly in the cycle completion report and go/no-go decision. Never silently skip tests — always make the risk visible to the team and stakeholders.

---

**Q23: What is risk-based testing? Give a real example of what to test first.**

**A:** Risk-based testing is a test prioritisation strategy where the order and depth of testing is determined by the assessed risk of each area — risk being the combination of the probability that area will fail and the business impact if it does.

Rather than testing everything equally, risk-based testing concentrates effort where failures would hurt most. In a world of limited time and resources, this is the most professionally defensible approach.

**Formula:** Risk = Probability of Failure × Consequence of Failure

**Real example — a new "Batch Student Import" feature is being released alongside a minor navigation change:**

| Feature/Area | Probability of Failure | Consequence of Failure | Risk Score | Test First? |
|-------------|----------------------|----------------------|-----------|------------|
| Batch import of CSV (new feature) | 4 — brand new code | 5 — corrupts student records | 20 | Yes — immediately |
| Login (unchanged) | 1 — stable, no changes | 5 — all users locked out | 5 | Yes — smoke test |
| Navigation bar (minor change) | 3 — some CSS changed | 2 — visual only | 6 | After import |
| Password reset (unchanged) | 1 — stable | 4 — self-service blocked | 4 | Later if time |
| Admin report export (unchanged) | 1 — stable | 3 — admin inconvenienced | 3 | Defer |

**Testing order:**
1. Smoke test the whole application (5 minutes)
2. Deep test the batch CSV import — all edge cases, error handling, data validation, large file sizes
3. Sanity check the navigation change
4. Regression the login flow
5. If time remains: password reset, admin reports

This approach ensures the highest-risk area (new batch import, which could corrupt data) gets the most thorough coverage, while stable areas get lighter coverage proportionate to their actual risk.

---

## SECTION 10 — Test Execution and Reporting

**Q24: What do you do when a test is blocked?**

**A:** A blocked test is one that cannot be executed due to an external dependency — not because it fails, but because the conditions for running it cannot be met.

**My process when a test is blocked:**

1. **Identify the specific blocker.** What exactly is preventing the test from running? Be precise: "The UAT environment is returning a 503 because the database connection pool is exhausted" is better than "the environment is broken."

2. **Mark the test case as Blocked with a clear reason.** In the test management tool (Zephyr, TestRail), update the status to Blocked and add a note: "Blocked by: mail server misconfigured in test environment — emails are not being delivered. Dependency on DevOps to fix SMTP configuration. Ticket: DEV-2291."

3. **Flag the blocker to the team immediately.** Post in the team channel, tag the owner. Blockers that are not communicated stay unresolved. For P1 blockers, escalate in standup.

4. **Attempt a workaround.** Can I test in a different environment? Can I mock the dependency? Can I test the feature partially? Can I use a dev's local environment? Not always possible, but always worth trying.

5. **Move on to other test cases.** One blocked test must not stop the entire cycle. Work around it, continue other testing, and revisit when the blocker is resolved.

6. **Track it as a daily risk.** Mention blocked tests in daily standup until they are unblocked. A blocked P1 test is a release risk.

7. **Re-execute as soon as unblocked.** When the dependency is resolved, pick the test back up immediately. Do not let it slip to "not executed" in the final report.

**In the completion report:** Blocked tests are reported separately from failures. They represent a known gap in coverage — a risk the team must consciously accept or resolve before release.

---

**Q25: What is a test cycle completion report? What does it contain?**

**A:** A test cycle completion report is the formal summary produced at the end of a test cycle. It gives stakeholders an objective, data-driven view of software quality so they can make an informed release decision. It is the QA team's primary output.

**What it contains:**

```
TEST CYCLE COMPLETION REPORT
==============================
Project:           Student Platform v3.1.0
Environment:       Test Environment (build 3.1.0-rc2)
Cycle Dates:       Sprint 24 — Days 3 to 5
Testers:           QA Engineer 1, QA Engineer 2

TEST EXECUTION SUMMARY
-----------------------
Total Test Cases Planned:  142
Executed:                  137 (96.5%)
  Passed:                  121 (88.3% of executed)
  Failed:                   11 (8.0% of executed)
  Blocked:                   5 (3.6% of executed)
Not Executed:                5 (3.5%)

DEFECT SUMMARY
--------------
Total Defects Raised: 17
  Critical (P1): 2  [Open: 1 | Closed: 1]
  High (P2):     6  [Open: 2 | Closed: 4]
  Medium (P3):   5  [Open: 4 | Closed: 1]
  Low (P4):      4  [Open: 4 | Closed: 0]

RISK AREAS
----------
- Batch CSV import: 3 failures, including 1 P1 (data corruption on special chars in names)
- Email notifications: 5 tests blocked — SMTP not configured in test environment
- Payment gateway integration: 2 P2 failures on partial refund scenarios

GO / NO-GO RECOMMENDATION
--------------------------
RECOMMENDATION: NO-GO

Reasons:
1. DEF-551 (Critical): CSV import corrupts student records when name contains
   comma — data integrity risk — release blocker
2. 5 email notification tests are blocked — cannot confirm email delivery works

Conditions for GO:
- DEF-551 resolved, re-tested, and passed
- SMTP configuration fixed in test environment; 5 blocked tests executed and passing
- 2 P2 payment refund defects reviewed by Product Owner — risk accept or fix

Approved by: [QA Lead Signature / Date]
```

---

**Q26: What is a go/no-go decision? What factors influence it?**

**A:** A go/no-go decision is the formal recommendation made at the end of a test cycle about whether the software is safe to release to production. QA makes this recommendation — the final decision belongs to the product owner, release manager, or business stakeholder.

**Factors that determine NO-GO (release should not proceed):**

- Any P1/Critical defect is still open — these are release blockers by definition
- Any known data loss, data corruption, or security vulnerability is unresolved
- Any compliance or legal requirement is not met
- Pass rate is below the agreed threshold (commonly 90%)
- Blocked tests covering high-risk areas have not been resolved
- Performance or load issues will affect production under expected volume

**Factors considered for conditional GO:**

- All P1 defects are closed
- P2 defects are reviewed and either fixed or formally risk-accepted by the product owner
- Pass rate meets the agreed threshold
- Blocked tests are either resolved or the risk has been assessed and accepted
- Outstanding P3/P4 issues are documented as known issues for the next release
- UAT has been completed and signed off

**QA's role:** Provide objective data and a clear recommendation. Not to make the business decision — the business takes that responsibility. But QA must not be passive: if the evidence says No-Go and you say nothing, you share responsibility for the production incident.

---

## SECTION 11 — Advanced Manual Testing Concepts

**Q27: How do you test a responsive design manually?**

**A:** Responsive design testing verifies that the application looks and functions correctly across the spectrum of screen sizes and device types it is intended to support.

**My step-by-step approach:**

**Step 1 — Define the breakpoints.** Check the design system or CSS for the breakpoints the application uses. Common breakpoints: 320px (small mobile), 375px (iPhone SE), 768px (tablet), 1024px (tablet landscape/small desktop), 1280px (desktop), 1440px (wide desktop).

**Step 2 — Use browser DevTools for rapid iteration.** Chrome DevTools (F12 → Toggle Device Toolbar) lets you set exact pixel widths and test layouts quickly. Note: this is a simulation, not a real device.

**Step 3 — Test at each breakpoint — what to check:**

| Check | What to Look For |
|-------|----------------|
| No horizontal scroll | Scroll the page horizontally — there should be none on mobile |
| No overlapping elements | Inspect cards, modals, nav — nothing should overlap |
| Text readability | Body text should be at least 16px, readable without zooming |
| Touch targets | Buttons and links at least 44px tall on mobile |
| Navigation | Hamburger menu or collapsed nav on mobile; full nav on desktop |
| Images | Scale correctly, no stretching, no overflow |
| Forms | Input fields full-width on mobile, correctly stacked |
| Tables | Either horizontally scroll or collapse to a card format |
| Modals | Fit within the viewport; scrollable if content is long |

**Step 4 — Test on real devices.** DevTools simulation is approximate. Always test critical layouts on actual hardware — at minimum an actual Android phone and an actual iPhone — before releasing.

**Step 5 — Test orientation.** Test portrait and landscape on both phone and tablet. Layouts often break in landscape mode on phones.

**Step 6 — Test system text size.** Increase the OS system font size to maximum. Does the layout survive? Do elements overflow their containers?

---

**Q28: What is the difference between testing and checking?**

**A:** This is a subtle but important distinction introduced by Michael Bolton and James Bach.

**Checking** is the verification of something already known. It is algorithmic and deterministic. Given a known input, does the system produce the expected output? Automated regression tests are checks. A smoke test checklist is checking.

**Testing** is investigation under uncertainty. It involves learning, exploration, judgement, and adapting based on what you discover. It answers questions that are not yet known. Exploratory testing is testing. UAT is testing. Evaluating whether a feature "feels right to a user" is testing.

**Why the distinction matters in practice:**

In an interview or project context, this distinction explains why automation cannot replace manual testing. Automation can only check things that are already defined — it verifies known behaviour against a fixed assertion. It cannot discover unknown issues, evaluate subjective quality, or adapt when it finds something unexpected.

A QA engineer who only checks is valuable. A QA engineer who both checks and tests is exceptional — they find what scripts miss.

In your portfolio: frame your exploratory testing work as testing (investigation, discovery, learning) and your regression work as checking (verification, confirmation). This demonstrates that you understand the strategic difference between the two activities.

---

**Q29: How do you test a feature with no requirements document?**

**A:** Missing requirements are extremely common, especially in Agile teams where story cards are thin and detail lives in people's heads.

**My approach:**

1. **Gather implicit requirements.** Talk to the developer ("what did you build?"), the product owner ("what problem does this solve?"), and the designer ("what was the design intent?"). Review any designs, wireframes, or Jira tickets. Check the git commit messages for context.

2. **Use existing patterns as a guide.** How does the rest of the application handle similar situations? Error messages, validation rules, empty states — consistent patterns imply implicit requirements.

3. **Apply test heuristics.** Use the SFDPOT heuristic (James Bach):
   - **S**tructure — what is it made of? Test each element.
   - **F**unction — what does it do? Test each function.
   - **D**ata — what data does it consume or produce? Test boundaries and edge cases.
   - **P**latform — where does it run? Test environment combinations.
   - **O**perations — how will it be used? Test the typical user journey and abuse cases.
   - **T**ime — does timing matter? Test timeouts, sequences, concurrent usage.

4. **Run a discovery exploratory session.** Charter: "Explore [feature] to understand its behaviour, discover its boundaries, and identify potential risks." Take notes. Your session notes become implicit requirements documentation.

5. **Write the requirements you discover.** After your first exploratory pass, document what you found as acceptance criteria. Review them with the product owner. This protects the team — you now have a shared understanding of what correct behaviour looks like.

6. **Flag the risk.** Raise in standup or with the tech lead that testing without requirements increases the risk of misunderstanding. Push for acceptance criteria to be written, even retrospectively, before the story is closed.

---

**Q30: What is pairwise testing? When do you use it?**

**A:** Pairwise testing (also called all-pairs testing) is a combinatorial test design technique that dramatically reduces the number of test cases needed when testing multiple input variables, while still covering every pair of variable combinations at least once.

**The problem it solves:**

Imagine a search filter with three inputs:
- Subject: Mathematics, Science, English (3 options)
- Year Level: Year 7, Year 8, Year 9, Year 10 (4 options)
- Status: Active, Inactive (2 options)

Full combinatorial coverage: 3 × 4 × 2 = 24 test cases.

Research shows that most bugs are triggered by interactions between two variables, not three or more simultaneously. Pairwise testing ensures every pair of variables is covered in at least one test case, reducing the 24 cases to approximately 8-10, while still catching the vast majority of real bugs.

**When to use pairwise testing:**

- Many input parameters with multiple values each
- API testing with multiple query parameters
- Configuration testing (OS + browser + feature flag combinations)
- Form fields with multiple independent dropdowns
- Filter combinations on a search or report page

**Tools for generating pairwise combinations:**
- PICT (Pairwise Independent Combinatorial Testing) — free Microsoft tool
- Hexawise — web-based tool
- AllPairs — open source Python tool

**Trade-off:** Pairwise is not suitable when the interaction of three or more specific values is known to be a risk. In that case, write specific test cases for those combinations explicitly.

---

## SECTION 12 — Interview Q&A Bank

**Q31: A developer tells you "it works on my machine." How do you respond?**

**A:** This is a classic environment discrepancy situation. "Works on my machine" is a real observation — it genuinely worked in the developer's local environment. It does not mean the defect is not real. It means there is a difference between environments.

My response:

First, I confirm the defect is still reproducible in the test environment by running through the exact steps to reproduce and capturing a video or screenshot as evidence.

Second, I compare the two environments: browser version, OS, application build version, test data, environment configuration (feature flags, environment variables, API endpoints). One of these differences is causing the discrepancy.

Third, I raise it constructively: "I can reproduce it consistently in the test environment on [browser X, OS Y]. Can you check what version and config your local environment is running? I'd like to find the difference so we can ensure it doesn't affect production users."

The goal is not to win an argument — it is to find and fix the problem before it reaches production users.

---

**Q32: How do you test a new feature on the day before release with only 2 hours?**

**A:** Two hours before release requires immediate triage and ruthless prioritisation.

First 15 minutes: Run the smoke test for the entire application. If it fails, escalate immediately — the release may need to be delayed.

Next 45 minutes: Test the new feature specifically. Understand what changed (talk to the developer, read the PR). Test the primary happy path thoroughly. Test the most obvious and damaging failure scenarios. Test the integration points.

Next 30 minutes: Targeted regression on areas that share code with the change. Not the full regression suite — the highest-risk adjacent areas.

Final 30 minutes: Log anything remaining as known risk. Write a brief risk summary for the release manager: "Tested X and Y. Could not test Z due to time — risk assessment: [low/medium/high] because [reason]."

Document everything. Never silently skip testing — always make the risk visible. A release manager who knows there is a risk can make an informed decision. One who does not know cannot protect the business.

---

**Q33: How do you demonstrate the value of exploratory testing to a manager who only wants to see test cases in a spreadsheet?**

**A:** The best demonstration is outcomes — defects found through exploratory testing that scripted tests would have missed.

I keep a record of defect origin: how was each defect found? Script-based, exploratory, UAT, production? Over time, the data shows how many critical defects came from exploratory sessions that no script covered.

I also show session notes and charters as equivalent documentation to test cases. A well-written SBET session has: a goal (charter), a time box, documented actions, documented findings, and a debrief. This is auditable and reproducible — just structured differently from a step-by-step test case.

Finally, I explain the economic argument: exploratory testing finds defects earlier (and cheaper to fix) than a scripted test that was never written would have. The question is not "scripts or exploration" — it is "what combination gives us the best coverage for the risk?"

---

**Q34: What is test coverage? How do you measure it for manual testing?**

**A:** Test coverage is a measure of how much of a system's functionality, requirements, or risk has been tested. For manual testing, it is typically measured against requirements or test cases, not code.

**Common manual test coverage metrics:**

- **Requirements coverage:** What percentage of documented requirements have at least one test case? (Target: 100% for P1 requirements)
- **Test case execution rate:** Of the planned test cases, how many were executed? (Reported as: 137/142 = 96.5%)
- **Pass rate:** Of executed tests, what percentage passed? (128/137 = 93.4%)
- **Defect detection efficiency:** How many defects were found in QA vs found by users post-release?
- **Risk coverage:** Have all identified high-risk areas been tested?

**What manual coverage cannot measure (unlike automated code coverage):** Line-by-line code execution. This is why automated unit and integration tests complement manual testing — together they provide both functional coverage and code-level coverage.

In practice, I report test coverage in every cycle completion report as: total tests planned vs executed vs passed vs failed, with a breakdown by module. This gives stakeholders a precise view of what was tested and what was not.

---

**Q35: What is the difference between a defect, a bug, and a failure?**

**A:** These terms are used interchangeably in most teams but have precise technical meanings:

- **Defect (or fault):** A flaw in the code or design that has the potential to cause an incorrect result. The defect exists in the code regardless of whether it has been triggered. "There is a missing null check on line 42."

- **Bug:** Informal term, synonymous with defect. Used widely in Agile teams.

- **Failure:** The observable, incorrect behaviour that occurs when a defect is executed. "When the user submits a form with no name, the application throws a NullPointerException." The failure is the symptom; the defect is the root cause.

- **Error:** A human action that produced the defect. A developer misunderstanding a requirement and writing the wrong logic is the error. The resulting code flaw is the defect.

In a QA context: we observe failures during testing, we report defects in the defect tracker, and developers investigate to find and fix the root cause (which may be an error in logic, design, or requirements).

---

**Q36: What questions do you ask when you receive a new story to test?**

**A:** The right questions before testing are as important as the testing itself. They surface ambiguity before it becomes a defect.

My standard questions for any new story:

**Requirements:**
- What is the acceptance criteria? Are all scenarios covered (happy path, negative, edge cases)?
- What is the expected behaviour for empty states, null inputs, and maximum limits?
- Are there any regulatory or accessibility requirements for this feature?

**Scope:**
- What other features or services does this change touch? (What do I need to regression test?)
- What is explicitly out of scope for this story?

**Data:**
- What test data do I need? Where do I get it?
- Are there any constraints on test data (real PII, data volume limits)?

**Environment:**
- When will this be available in the test environment?
- Are there any environment-specific configurations I need to be aware of?
- Are there feature flags? Which environments have them enabled?

**Integration:**
- Does this interact with any external service or API? Is there a sandbox/mock available?

**History:**
- Has this area had bugs before? Are there known fragile areas?

Asking these before testing begins saves time, prevents assumptions, and surfaces gaps in requirements while they can still be fixed cheaply.

---

**Q37: How do you write a defect report?**

**A:** A good defect report is precise enough for a developer to reproduce the issue without asking any questions. It contains:

**Required fields:**

| Field | Content |
|-------|---------|
| **Title** | "[Feature] [Specific failing behaviour] [Condition]" — e.g. "Login form returns 500 error when email contains an apostrophe" |
| **Severity** | Critical / High / Medium / Low — impact on users |
| **Priority** | P1 / P2 / P3 / P4 — urgency of fix |
| **Environment** | OS, browser, build version, test environment |
| **Preconditions** | State required to reproduce |
| **Steps to Reproduce** | Numbered, one action per step, with exact data |
| **Expected Result** | What should happen according to requirements |
| **Actual Result** | What actually happened — precise and observable |
| **Evidence** | Screenshot, video, API response, log extract, network trace |
| **Test Case ID** | Link to the failing test case if applicable |

**What separates a professional defect report:**

- The developer can reproduce it in under 5 minutes with no additional questions
- The expected result references a requirement, acceptance criterion, or standard — not just "it should work"
- The evidence is attached, not just described
- The severity is justified — not all bugs are P1 (crying wolf causes defect reports to be ignored)

---

**Q38: What is pairwise testing vs equivalence partitioning vs boundary value analysis?**

**A:** These are three test design techniques for choosing what input values to test.

**Equivalence Partitioning (EP):** Divides the input space into groups (partitions) where all values in a group should behave the same way. Test one value from each partition. For an age field accepting 18–65: Valid partition (18–65), below range (<18), above range (>65). Test one from each — e.g. 30, 10, 80. This reduces test cases from infinite to three.

**Boundary Value Analysis (BVA):** Tests the values at the edges of equivalence partitions, where errors are most likely to occur. For age 18–65: test 17 (just below), 18 (minimum), 19 (just above minimum), 64 (just below maximum), 65 (maximum), 66 (just above). Boundaries are where off-by-one errors live.

**Pairwise Testing:** When there are multiple input variables, tests every pair of variable combinations at least once. Reduces N × M × O combinations to a much smaller set while still covering the interactions most likely to cause defects.

**When to use each:**

| Technique | Best For |
|-----------|----------|
| Equivalence Partitioning | Single input with multiple valid/invalid ranges |
| Boundary Value Analysis | Any numeric or date input with limits |
| Pairwise | Multiple independent inputs with multiple values each |

In practice, BVA is used within EP — partition first, then apply BVA at the boundaries of each partition.

---

**Q39: What do you do if you find a critical defect at the end of a test cycle, one day before release?**

**A:** Finding a critical defect late is high-pressure — but the right response is the same regardless of timing: surface the information clearly and immediately.

**My immediate actions:**

1. **Confirm reproducibility.** Run it again with fresh steps. Can I reproduce it consistently? Is it environment-specific? Is it data-dependent?

2. **Capture thorough evidence.** Full video of reproduction, API request and response (from Network tab), console errors, exact test data, build version. This needs to be unambiguous.

3. **Raise the defect immediately with Critical severity.** In Jira, Zephyr, or the defect tracker. Complete steps to reproduce, expected result, actual result, evidence. Tag the relevant developer and QA lead.

4. **Notify the team — do not wait for standup.** Post in the team channel, tag the developer, product owner, and release manager. "Critical defect found in [feature]: [brief description]. It is a data integrity issue / security issue / [specific type]. Defect link: [URL]. Needs immediate triage."

5. **Provide impact assessment.** What percentage of users are affected? Is there a workaround? What is the data risk?

6. **Recommend No-Go.** In the test cycle report, state clearly: "Release is NOT recommended. DEF-XXX is a critical open defect and a release blocker."

The business may choose to release anyway with a known risk — that is their decision to make. My job is to ensure they make it with complete, accurate information, and that the decision is documented.

---

**Q40: How do you keep your manual testing skills sharp as automation becomes more prevalent?**

**A:** Manual testing skills do not become obsolete — they evolve. As automation handles repetitive regression checking, manual testing shifts increasingly toward higher-value activities: exploratory testing, risk analysis, usability evaluation, and complex user journey investigation.

**How I keep skills current:**

- **Practice exploratory testing regularly.** Use SBET on new features, competitor products, and open-source applications. Structured exploration builds pattern recognition.
- **Study test heuristics.** Mnemonics like SFDPOT, HICCUPPS, and FEW HICCUPPS give systematic coverage without a script.
- **Learn the domain deeply.** The more I understand the business context, regulations, and user needs, the better my intuition for where to test and what to question.
- **Review post-release defects.** Every bug that reached production is a lesson. What category of test would have caught it? How can I add that category to future cycles?
- **Engage with the QA community.** Ministry of Testing, Testing Peers, exploratory testing workshops, and the writings of James Bach, Michael Bolton, and Elisabeth Hendrickson.

The most important skill is judgement — knowing what to test, how deeply, and why. That is not automatable and does not become less valuable.

---

*End of Manual Testing Complete Interview Q&A Guide — 40 Questions Covered*
