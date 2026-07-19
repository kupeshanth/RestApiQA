# My QA Experience — Real Examples & Stories | Kupeshanth Kupenthiran

> Read this the night before your interview. Know these stories cold. Every section is written to be spoken aloud — it's not a formal document, it's your preparation script.

---

## TABLE OF CONTENTS

- [Section 1 — Tell Me About Yourself (90-second script)](#section-1--tell-me-about-yourself)
- [Section 2 — My Projects With Technical Detail](#section-2--my-projects-with-technical-detail)
- [Section 3 — STAR Stories (6 behavioral answers)](#section-3--star-stories)
- [Section 4 — Honest Gap Handling](#section-4--honest-gap-handling)
- [Section 5 — Skills Confidence Matrix](#section-5--skills-confidence-matrix)
- [Section 6 — Questions to Ask the Interviewer](#section-6--questions-to-ask-the-interviewer)

---

## SECTION 1 — Tell Me About Yourself

> Memorise this. It should take exactly 75-90 seconds to say. Practise it until it sounds natural, not recited.

---

"I'm a Quality Engineer with around a year and a half of hands-on experience across two companies, working on both UI automation and API testing.

I studied software engineering at university, and during my studies I built an AI-powered Diabetic Retinopathy Detection system as my final year project — which got me interested in the intersection of technical software quality and real-world impact.

My first professional role was at Cerexio, a Singapore-based industrial IoT and analytics company. I worked there as an intern QA engineer for about six months, where I did API testing using Postman, tracked and reported bugs in JIRA, and worked directly alongside developers in Agile sprints. The product was an Angular PrimeNG-based enterprise application. That experience gave me a strong foundation in understanding how software is built and how defects move through a sprint cycle.

Since September 2025, I've been at Qoria Lanka as a Trainee Quality Engineer. Qoria is a child digital safety company, and I'm part of the team working on the EdTech Insight project. Here, I write Selenium and TestNG automation in Java, do Playwright-based UI testing in JavaScript, and our tests run through GitHub Actions pipelines connected to GCP infrastructure.

My strengths are in Java and Python automation — I'm confident with Selenium, Playwright, TestNG, and Postman. I've worked in Agile teams, I understand CI/CD pipelines from the QA perspective, and I genuinely care about delivering software that doesn't break for users.

I'm looking for a role where I can deepen my craft — work on complex systems, contribute to meaningful automation strategy, and grow into a senior QA role where I'm influencing quality from the beginning of a feature's life, not just at the end."

---

**Notes for delivery:**
- Pause after "Cerexio" and after "Qoria" — let those land
- Don't rush the final paragraph — it's where you show ambition
- If they cut you off, that's fine — they want a conversation, not a monologue

---

## SECTION 2 — My Projects With Technical Detail

---

### Project 1 — Singer Page Testing (Serenity + Cucumber + BDD)

**What the project was:**
Singer is a well-known electronics and appliance brand in Sri Lanka. This was a web application testing project where I implemented a BDD-based automation framework from scratch to test the Singer website's key user journeys — product browsing, search, and user account management.

**What I specifically did:**
- Set up the project structure using Serenity BDD with Cucumber and Java
- Wrote feature files in Gherkin language — defining scenarios in business-readable English
- Implemented step definitions that mapped Gherkin steps to Selenium WebDriver actions
- Built Page Objects for the Singer homepage, product listing pages, and account pages
- Used Serenity's reporting to generate HTML reports showing test results with screenshots embedded at each step
- Integrated assertions to verify product names, prices, and navigation flow

**Tools and why I chose them:**
- Serenity BDD over plain Cucumber because Serenity generates living documentation — the reports are readable by non-technical stakeholders, not just developers
- Cucumber/Gherkin because BDD made the test scenarios discussable with product owners before writing a single line of code
- Maven for build management because it's standard in Java test projects

**What I learned / what went wrong:**
The most challenging part was handling Singer's custom dropdown navigation — they used a JavaScript-rendered mega menu, not native HTML. My initial CSS selectors broke when the page loaded in a slightly different state. I fixed this by switching to XPath expressions that looked for the menu text content rather than the CSS class, and adding explicit waits before every navigation interaction.

I also learned that Serenity's page object model is slightly different from standard Selenium PageFactory — Serenity uses `@DefaultUrl` and page-level verification, which I had to learn separately from raw Selenium patterns.

**How to talk about it in an interview:**

Key talking points:
- "I've built a BDD framework from the ground up, not just used an existing one"
- "I wrote the Gherkin scenarios myself — this means I understand how to translate requirements into test cases"
- "The Serenity reports were valuable for demos — they show stakeholders what was tested and what passed without reading code"
- If asked about Cucumber specifically: explain the three-layer architecture — feature files, step definitions, page objects

---

### Project 2 — Python + Pytest + Selenium Automation

**What the project was:**
A self-driven automation project using Python instead of Java, to develop competency in Python-based test automation and broaden my tool range. I automated testing of a publicly accessible web application — testing form validation, navigation, and data display.

**What I specifically did:**
- Built a test suite using Pytest as the test runner
- Used Selenium WebDriver (Python binding) for browser interactions
- Implemented fixtures in Pytest (`@pytest.fixture`) for browser setup and teardown — the equivalent of `@BeforeMethod`/`@AfterMethod` in TestNG
- Used parametrize decorator (`@pytest.mark.parametrize`) to run the same test with multiple data sets
- Used conftest.py for shared configuration — base URL, browser choice, WebDriver initialization

**Sample structure I implemented:**
```python
# conftest.py
@pytest.fixture(scope="function")
def driver():
    driver = webdriver.Chrome()
    driver.maximize_window()
    yield driver
    driver.quit()

# test_search.py
@pytest.mark.parametrize("search_term,expected_count", [
    ("laptop", 10),
    ("phone", 5),
    ("", 0)  # Edge case: empty search
])
def test_search_results(driver, search_term, expected_count):
    page = SearchPage(driver)
    page.search(search_term)
    assert page.get_result_count() >= expected_count
```

**Tools and why I chose them:**
- Python because I wanted to be language-flexible — not all teams use Java
- Pytest over unittest because Pytest's fixture system is more powerful and its output is cleaner
- Parametrize because data-driven testing is essential and Pytest makes it elegant

**What I learned / what went wrong:**
Scope management in Pytest fixtures was initially confusing — using `scope="session"` for a browser instance that should have been `scope="function"` meant tests shared state between them. One test's navigation affected the next test's starting URL. Once I understood fixture scope properly, the tests became reliable.

**How to talk about it in an interview:**
- "I can work in Python as well as Java — I've built automation in both languages"
- "I understand the equivalent patterns across frameworks: pytest fixtures = TestNG @Before/@After, parametrize = TestNG @DataProvider"
- This shows adaptability — you're not locked into one tech stack

---

### Project 3 — Playwright + JavaScript UI Automation

**What the project was:**
At Qoria, I implemented Playwright-based UI automation in JavaScript for the EdTech Insight project. EdTech Insight is a platform that provides insights about student digital safety and wellbeing. The testing focus was on the dashboard views, filtering, and report generation features.

**What I specifically did:**
- Wrote Playwright tests in JavaScript using the Playwright Test runner
- Implemented authentication state management — logging in once and reusing the session cookie for all tests to avoid per-test logins
- Wrote tests for complex UI interactions: multi-select filters, date range pickers, table sorting
- Used Playwright's network interception to mock external API calls that weren't available in the test environment
- Integrated the tests into GitHub Actions — the workflow runs tests on pull requests targeting the main branch
- Captured screenshots and traces on failure for debugging

**Specific technical challenge:**
The EdTech Insight dashboard loaded data asynchronously — a table showed a loading spinner, then data appeared after 1-3 seconds depending on the filter parameters. Early tests would assert on the table before data loaded. I fixed this by waiting for the loading spinner to disappear before asserting on table content:
```javascript
await page.waitForSelector('.loading-spinner', { state: 'hidden' });
await expect(page.locator('table tbody tr')).toHaveCount(greaterThan(0));
```

**Tools and why:**
- Playwright over Selenium for this project because of its native async handling and reliable auto-wait — the EdTech Insight frontend is React-based and Playwright handles React's async rendering much better
- JavaScript because the frontend team uses JavaScript, making test code natural to review alongside product code

**How to talk about it in an interview:**
- "This is my current role — I can speak to it in detail and with real examples"
- Mention the GitHub Actions integration specifically — "I wired the Playwright tests into the PR pipeline, so QA runs automatically before any code review"
- If they ask about Playwright vs Selenium: "I've worked with both in production — Playwright is better for modern SPAs, Selenium is still the choice when you need Java integration or legacy browser testing"

---

### Project 4 — AI/ML Final Year Project — Diabetic Retinopathy Detection

**What the project was:**
My final year computer science project at university. I built a deep learning model to detect diabetic retinopathy (an eye disease that can cause blindness) from retinal fundus images. The system classified images into severity stages.

**What this demonstrates from a QA perspective:**
This project taught me about testing machine learning systems — which is fundamentally different from testing deterministic software:
- Model accuracy, precision, recall, F1-score as quality metrics
- Testing with different image qualities and edge cases (blurry images, over-exposed, incorrect orientation)
- Validation set vs. test set discipline — preventing data leakage that would inflate accuracy metrics
- Understanding that a "passing" ML test doesn't mean what it means in traditional software

**How to talk about it in an interview:**
Use this to show depth of thinking, not just tool proficiency:
- "Testing an ML model taught me to think about quality metrics beyond just pass/fail — and I bring that same thinking to software quality"
- "I understand statistical confidence, false positives vs. false negatives — which translates directly to risk assessment in software testing"
- If they're an AI/EdTech company: this is particularly relevant — shows you can work on sophisticated systems

---

## SECTION 3 — STAR Stories

> Situation, Task, Action, Result. Practice each one until you can tell it in 90-120 seconds. Know which question each one answers.

---

### STAR Story 1 — A Framework I Built
*Question it answers: "Tell me about a time you built something from scratch" / "Describe a testing framework you implemented"*

**Situation:**
When I started the Singer Page Testing project, there was no automation in place. The testing was entirely manual, which meant regression testing took significant time before any release and made it impossible to test frequently.

**Task:**
I needed to build an automation framework that was maintainable, could be run without deep technical knowledge (so future team members could run it), and produced reports that non-technical stakeholders could understand.

**Action:**
I chose Serenity BDD with Cucumber because the BDD layer meant test scenarios were written in Gherkin — readable by anyone on the team without reading Java code. I structured the project with a clear separation of concerns: feature files for business scenarios, step definition classes mapping Gherkin to code, and page objects handling element interactions. I added Serenity reporting which auto-generates HTML reports with screenshots at each step. I also documented the setup in a README so any developer could run the suite with a single Maven command.

**Result:**
The framework covered the core user journeys — navigation, search, product browsing, account access — with automated scenarios replacing the most time-consuming parts of manual regression. Serenity reports meant the product owner could review what was tested in plain English after each run. It also served as my first complete framework build — which built my confidence in architecture decisions.

---

### STAR Story 2 — API Testing Experience
*Question it answers: "Describe your experience with API testing" / "How do you approach testing backend services?"*

**Situation:**
At Cerexio, the product was an Angular-based enterprise application backed by REST APIs. When I joined, there was no structured API testing — developers tested their own endpoints manually and bugs were found late, often during integration or by clients.

**Task:**
As the QA intern, I was responsible for building out API test coverage for the core endpoints of the application — authentication, data CRUD operations, and reporting endpoints.

**Action:**
I used Postman as the primary tool because the team was small and everyone needed to be able to run tests, not just developers. I created organized collections for each API module, wrote test scripts that validated status codes, response body structure, field types, and business logic. I set up Postman environments for development and staging so the same tests ran against both with a single environment variable change. I also wrote negative test cases — what happens when you send invalid data, missing fields, unauthorized requests. Each test was documented with a description explaining what it was checking and why.

**Result:**
We started catching API-level bugs before they reached the UI layer, which significantly reduced the debugging time for the frontend team. The Postman collections became the team's shared API documentation — developers used them to understand how endpoints behaved and what responses to expect. When I transitioned at Cerexio, I handed over a documented, runnable set of API tests — which was more than existed when I arrived.

---

### STAR Story 3 — CI/CD Experience
*Question it answers: "Tell me about your experience with CI/CD" / "How have you integrated automation into a delivery pipeline?"*

**Situation:**
When I joined Qoria Lanka, the Playwright test suite existed but was being run manually by QA engineers before releases — it wasn't integrated into the development pipeline. This meant the feedback loop was slow: a developer would submit a PR, it would get reviewed, merged, and only then would QA discover issues.

**Task:**
I was tasked with integrating the Playwright tests into GitHub Actions so that tests ran automatically on every pull request, blocking merges if tests failed.

**Action:**
I wrote the GitHub Actions workflow YAML, configuring the pipeline to: install Node.js and dependencies, install Playwright browsers, run tests in headless mode with the correct environment variables (BASE_URL pointing to a staging deployment), and upload test results as artifacts on failure. I also configured the workflow to only run the full suite on PRs targeting main, and a smoke subset on feature branch PRs to balance speed with coverage. I worked with the DevOps team to ensure the GCP pipeline that deployed staging environments was triggered before tests ran, so tests always ran against a fresh build.

**Result:**
PRs now automatically trigger a test run. Failed tests block the PR merge — developers see test results directly on GitHub before the code review is complete. We caught two integration regressions in the first month that would previously have reached staging or production. The feedback loop for QA findings went from "end of sprint" to "same day as the code change."

---

### STAR Story 4 — Working in Agile
*Question it answers: "Describe your experience in an Agile team" / "What is QA's role in Agile ceremonies?"*

**Situation:**
Both at Cerexio and at Qoria, I've worked in Agile teams running two-week sprints. Initially, I came from a university background where testing was a phase that happened after code was written. In my first sprint at Cerexio, I realized QA was expected to be involved from the very start — not just at the end.

**Task:**
I needed to understand how to contribute meaningfully to each ceremony and shift my thinking from "testing after development" to "quality throughout the sprint."

**Action:**
In sprint planning, I started contributing by reviewing user stories for testability — asking questions like "what should happen when the user enters invalid data?" or "how do we define done for this story?" These questions either clarified ambiguous requirements or flagged gaps before development started. In refinement/backlog grooming, I wrote acceptance criteria alongside the product owner for new stories. In daily standups, I was transparent about testing progress and flagged blockers early rather than at the end of sprint. In retrospectives, I brought data — which types of bugs were found late, which test cases caught the most issues — to drive process improvement.

**Result:**
At Qoria, I became the person who spots acceptance criteria gaps in sprint planning — which the team found valuable because it reduced rework. We reduced "this needs clarification" comments during QA by catching ambiguity earlier. Working this way made me genuinely part of the delivery team, not an afterthought that checked code at the end.

---

### STAR Story 5 — A Bug I Found That Mattered
*Question it answers: "Tell me about a significant bug you found" / "Give an example of a high-impact defect you discovered"*

**Situation:**
During regression testing at Qoria for a sprint release of the EdTech Insight platform, I was testing a report export feature. The feature allowed school administrators to export student activity reports as CSV files for a selected date range.

**Task:**
I was running regression tests on the export feature before the sprint release. The feature had passed developer testing.

**Action:**
During exploratory testing, I tested an edge case: exporting a date range that spanned a month boundary — for example, from January 28 to February 4. The happy path (a date range within a single month) worked correctly. But when the date range crossed a month boundary, the exported CSV contained data only from the start date's month — it silently dropped data from the second month. There was no error message; the file downloaded successfully with a 200 status code. A school administrator would have no idea the data was incomplete.

I documented the bug with exact reproduction steps, the specific dates I used, a screenshot of the correct data in the UI vs. what appeared in the CSV, and the expected vs. actual content count. I flagged it as high severity because the data integrity issue was silent — users would trust a corrupt export.

**Result:**
The developer identified the root cause as an off-by-one error in the date range query — the query was using the wrong month boundary in the SQL `BETWEEN` clause. It was fixed before release. The product owner specifically thanked me for catching it in testing rather than letting it reach schools, where trust in the platform's data accuracy is critical. After this, I added explicit cross-month-boundary tests to the regression suite as a standard test case.

---

### STAR Story 6 — Improving a Process
*Question it answers: "Tell me about a time you improved a process" / "How have you contributed beyond just running tests?"*

**Situation:**
At Cerexio, the regression testing before each release was done entirely manually. It involved going through a long checklist of test steps in a shared spreadsheet. It took approximately two full days of a QA engineer's time and was error-prone — testers would sometimes miss steps or not check the right environment.

**Task:**
I identified this as an opportunity to reduce manual effort and improve reliability. I proposed to my manager that I spend time automating the most repetitive parts of the regression checklist.

**Action:**
I analysed the regression spreadsheet and identified the 20 test cases that were: always run (never skipped), purely mechanical (no judgment needed), and not dependent on test data that changed frequently. These were the login flow, core navigation, user creation, and basic data display tests. I automated these 20 cases using Selenium and Java, integrated them into a Maven project that could be run with one command, and set up the output to generate a simple pass/fail report. I handed this over to the team with documentation on how to run it and how to add new tests.

**Result:**
Those 20 automated tests ran in 8 minutes versus the 4-5 hours it took a tester to work through the same cases manually. The QA team could redirect those hours toward exploratory testing of new features — where human judgment is actually valuable. Regression coverage improved because we weren't skipping tests due to time pressure. This project also became the foundation for further automation work at Cerexio. I presented the result in a retrospective and the team formally adopted automation-first for all new regression cases going forward.

---

## SECTION 4 — Honest Gap Handling

> These are the tools or experiences you have limited depth in. Know exactly what to say — don't overstate, don't undersell. Honest confidence beats fake expertise every time.

---

### Appium (Mobile Testing)

**What you actually know:**
You understand what Appium is — it extends the Selenium WebDriver protocol to mobile apps (iOS and Android), using native locators for mobile elements. You know it uses the same pattern as Selenium but with device-specific considerations.

**What to say:**
"I haven't had the opportunity to use Appium on a production project yet. My automation experience has been web-based — Selenium, Playwright, and Playwright on mobile viewports. I understand Appium's architecture: it uses WebDriver protocol extended with mobile capabilities, and the locator strategies differ — you use accessibility IDs and XPaths targeting mobile UI hierarchy rather than DOM elements. It's on my learning list and I'd be comfortable picking it up in a structured way — my Selenium foundation transfers directly."

**Don't say:** "I've used Appium a bit" unless you genuinely have. Interviewers follow up.

---

### Gauge Framework

**What you actually know:**
Gauge is a BDD test framework (alternative to Cucumber) that uses Markdown-based specifications instead of Gherkin. It's created by ThoughtWorks and integrates with languages including Java and JavaScript.

**What to say:**
"I've read about Gauge but I haven't used it on a project — my BDD experience is with Cucumber and Serenity. I understand Gauge's approach: it uses Markdown for specs rather than Gherkin, which some teams find more readable and easier to maintain. The underlying concept is the same as Cucumber — specs drive test implementation. Given that I built a Serenity+Cucumber framework from scratch, I'm confident the learning curve for Gauge would be manageable — the pattern is the same, the syntax is different."

**Follow-up ready:** If they ask why teams choose Gauge over Cucumber — Gauge has better support for data tables in Markdown, simpler setup, and some argue its specs are more natural English without the rigid Given/When/Then structure.

---

### RestAssured (Beyond Basics)

**What you actually know:**
You understand RestAssured's API — `given().when().then()` BDD-style chaining, header setting, path parameters, response extraction, Hamcrest matchers for assertions. You've studied it and understand how to write tests with it.

**What to say:**
"I've worked with RestAssured in learning projects and understand its fluent API well — given/when/then chaining, extracting responses, Hamcrest assertions. My production API testing experience is primarily with Postman at Cerexio. At Qoria I've been more focused on UI automation. I understand the RestAssured patterns and the Java integration is natural given my Selenium background — I'd be productive with it quickly. Is RestAssured used heavily here? I'd welcome understanding the depth you need."

**Why this works:** You're honest, you show what you know, you ask a smart follow-up question that shows you're thinking about fit — not just trying to pass.

---

### "You Only Have 1.5 Years Experience But We Asked for 3+"

**What to say:**
"You're right that I'm at the earlier stage of my career in years. I want to be direct about that rather than oversell. What I can offer is someone who has been working across two companies on real, shipped products — not just academic projects. At Cerexio I was doing API testing on an enterprise application from week one. At Qoria I'm building automation that runs in the production CI pipeline. The quality of the experience has been dense.

I also learn quickly and take technical depth seriously — I've built frameworks rather than just used them, which means I understand the architecture, not just the scripts. I'd ask you to evaluate my capability through this conversation rather than through the year count alone — and I'm happy to go as deep as you want on any technical area."

**Why this works:** You acknowledge the gap directly (which builds trust), reframe the value proposition (quality over quantity), and redirect to demonstrable competence. Don't apologize for your experience — own it.

---

## SECTION 5 — Skills Confidence Matrix

| Skill | Evidence from My CV | Confidence |
|---|---|---|
| Selenium WebDriver | Singer BDD project, Qoria Selenium+TestNG suite | High |
| TestNG | Qoria daily use — groups, parallel execution, @Before/@After | High |
| Java | Primary language at Qoria, Singer project — 1+ year production use | High |
| Python | Python+Pytest automation project, university coursework | Medium |
| Postman / API Testing | Cerexio — 6 months of API testing in production | High |
| Playwright | Qoria EdTech Insight UI automation, GitHub Actions integration | High |
| BDD / Cucumber | Singer Serenity+Cucumber framework — built from scratch | High |
| Serenity BDD | Singer project — full framework build and report generation | Medium |
| GitHub Actions | Wired Playwright tests into PR pipeline at Qoria | Medium |
| JIRA | Used daily at Cerexio for bug tracking and sprint management | High |
| GCP Pipelines | Exposure at Qoria — tests run in GCP-connected pipeline | Honest |
| JavaScript | Playwright tests at Qoria, some scripting | Medium |
| JMeter | Studied and used in learning projects — not production | Honest |
| Cypress | Studied architecture and written sample tests — not production | Honest |
| Jenkins | CI/CD concepts understood — not hands-on in production | Honest |
| Appium | Conceptual knowledge only — no project experience | Honest |
| RestAssured | Study-level knowledge — fluent API understood, not production | Honest |
| Gauge | Conceptual knowledge — read documentation, not used in project | Honest |

**Confidence levels:**
- **High** = I've used this in production, I can go deep in an interview
- **Medium** = I've used this in a real project, some gaps in advanced usage
- **Honest** = I have theoretical knowledge and/or limited project use — I won't oversell this

---

## SECTION 6 — Questions to Ask the Interviewer

> Ask at least 2-3 of these. They signal that you're thinking about the role seriously, not just trying to get any job.

---

**Question 1: "How does QA fit into the development cycle here — are testers involved from the start of a sprint or brought in at the end?"**

*Why ask this:* It tells you immediately whether the team practices modern Agile quality or the old "QA is at the end of the waterfall" model. The answer reveals how much influence you'd have and how valuable your input on requirements would be. A team that involves QA from the start will give you a better, more impactful role.

---

**Question 2: "What does the automation coverage look like today, and what's the biggest gap the team wants to close in the next 6 months?"**

*Why ask this:* This question has two payoffs. First, it shows you're thinking about contributing immediately — not just settling in. Second, the answer tells you what you'd actually be working on. If the answer is "we have no automation," you know what you're signing up for. If the answer is "we have Selenium but no API testing," you know where you'd add value. It also opens the conversation for you to talk about your experience in that gap area.

---

**Question 3: "What's the biggest quality challenge the team is facing right now — not in the future, but right now?"**

*Why ask this:* Every team has a pain point. Asking this shows you're practical and interested in solving real problems, not just describing the ideal state. Their answer will tell you whether the challenge is technical (flaky tests, no CI integration), cultural (developers who bypass testing, management that skips sprints), or process-related (no test environments, no test data management). Each type of answer gives you information about the role.

---

**Question 4: "How do you measure QA effectiveness here — what metrics matter to the team and to leadership?"**

*Why ask this:* Teams that measure QA by defect count (bugs raised) often incentivize the wrong behavior. Teams that measure by escaped defects, automation coverage, and feedback speed are more mature. The answer tells you how the team thinks about quality as a discipline. It also opens a great conversation where you can share how you think about measurement — which differentiates you from candidates who can only describe what they did, not why it mattered.

---

**Question 5: "What does growth look like for a QA engineer here — what would a strong QA engineer be doing differently in 2 years compared to day one?"**

*Why ask this:* This tells you if there's a real career path or if the role is a box-checking function. A company where QA engineers grow into quality architects, security testing specialists, or team leads is a different environment than one where the role is flat. It also shows you're thinking long-term — which signals ambition and reduces the perception that you're just taking any offer.

---

*This document is yours. Internalize the stories, trust the preparation, and walk in knowing that you've done the work. You're not trying to seem experienced — you are experienced in the ways that matter for someone at your stage of career. Show that, and the right team will recognize it.*
