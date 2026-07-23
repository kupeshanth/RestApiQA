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

"I'm a Trainee Quality Engineer with around a year and a half of hands-on professional experience across two companies, working across UI automation, API testing, and CI/CD pipeline integration.

I studied at the University of Moratuwa — a B.Sc. Honours in Information Technology and Management. During my studies I built several projects, including a Diabetic Retinopathy Detection System using Python, Machine Learning, and NLP with a React frontend. That project taught me early on that quality metrics aren't always binary — which is a perspective I bring to testing today.

My first professional role was at Cerexio, a technology company, where I worked as an Intern Software Engineer and QA Engineer for six months — March to September 2024. I did manual and automated testing on Angular PrimeNG enterprise applications, API testing with Postman, tracked bugs in JIRA, and worked directly with developers in Agile sprints.

Since September 2025, I've been at Qoria Lanka as a Trainee Quality Engineer on the EdTech Insight project. Qoria is a child digital safety company. Here I write Selenium and TestNG automation in Java, build Playwright UI tests in JavaScript, and I've been working on MCP and Agent integrations as part of how the team explores AI-augmented testing workflows. Our tests run through GitHub Actions pipelines with GCP-triggered executions for deployment validation.

My strengths are in automation — Selenium, TestNG, Playwright, and Postman — and I understand CI/CD from a QA perspective. I've worked in Agile teams at both companies, and I care about building automation that gives teams real confidence to ship.

I'm looking for a role where I can deepen my craft — contribute to meaningful automation strategy, and grow into a QA engineer who shapes quality from the beginning of a feature's life, not just at the end."

---

**Notes for delivery:**
- Pause after "Cerexio" and after "Qoria Lanka" — let those names land
- The MCP/Agent integrations mention is a differentiator — say it naturally, not like you're reading a buzzword
- Don't rush the final paragraph — it's where you show ambition and intent
- If they cut you off, that's fine — they want a conversation, not a monologue

---

## SECTION 2 — My Projects With Technical Detail

---

### Project 1 — Singer Page Testing Application (Serenity + Cucumber + BDD)

**What the project was:**
Singer is a well-known electronics and appliance brand in Sri Lanka. This was a web application testing project where I implemented a BDD-based automation framework from scratch to test the Singer website's core user journeys — product browsing, search, and navigation flows.

**What I specifically did:**
- Set up the project using Serenity BDD with Cucumber and Java
- Wrote feature files in Gherkin — defining test scenarios in plain English
- Implemented step definitions that mapped Gherkin steps to Selenium WebDriver actions
- Built Page Objects for the Singer homepage, product listing pages, and navigation
- Used Serenity's reporting to generate HTML reports with screenshots embedded at each step
- Integrated assertions to verify product names, navigation flow, and page state

**Tools and why I chose them:**
- Serenity BDD over plain Cucumber because Serenity generates living documentation — reports that non-technical stakeholders can read and understand without opening a single line of code
- Cucumber/Gherkin because test scenarios are discussable with anyone before writing code
- Maven for build management — standard for Java test projects

**What I learned / what went wrong:**
Singer's navigation menu was JavaScript-rendered — a dynamic mega menu, not static HTML. My early CSS selectors broke when the menu loaded in a slightly different state. I fixed this by switching to XPath that targeted text content rather than CSS class names, and added explicit waits before every navigation interaction.

I also learned that Serenity's page object model differs from raw Selenium PageFactory — Serenity uses `@DefaultUrl` and page-level verification steps that I had to learn separately from standard Selenium patterns.

**How to talk about it in an interview:**
- "I've built a BDD framework from scratch, not just used an existing one"
- "I wrote the Gherkin scenarios myself — I know how to translate requirements into testable scenarios"
- "Serenity reports were genuinely valuable for demos — any stakeholder could see what was tested and what passed in plain English"
- If asked about Cucumber: explain the three-layer structure — feature files, step definitions, page objects

---

### Project 2 — Automation Project Using Python, Pytest, and Selenium

**What the project was:**
A self-driven automation project in Python to build competency outside of Java. I automated testing of a publicly accessible web application — covering form validation, navigation, and data display.

**What I specifically did:**
- Built a test suite using Pytest as the test runner
- Used Selenium WebDriver's Python bindings for browser interactions
- Implemented `@pytest.fixture` for browser setup and teardown — the equivalent of TestNG's `@BeforeMethod`/`@AfterMethod`
- Used `@pytest.mark.parametrize` to run the same test across multiple data sets (data-driven testing)
- Used `conftest.py` for shared configuration — base URL, browser choice, driver initialization

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
    ("", 0)   # Edge case: empty search
])
def test_search_results(driver, search_term, expected_count):
    page = SearchPage(driver)
    page.search(search_term)
    assert page.get_result_count() >= expected_count
```

**What I learned:**
Fixture scope was initially confusing. Using `scope="session"` for a browser instance that should have been `scope="function"` meant tests shared state — one test's navigation affected the next test's starting URL. Once I understood fixture scope, tests became isolated and reliable.

**How to talk about it in an interview:**
- "I can work in Python as well as Java — I've built automation in both languages"
- "I understand the equivalent patterns across frameworks: Pytest fixtures = TestNG Before/After, parametrize = TestNG DataProvider"
- This shows adaptability — you're not locked into one stack

---

### Project 3 — UI Automation Project Using Playwright (JavaScript / Node.js)

**What the project was:**
A personal Playwright automation project built in JavaScript using Node.js, focused on testing UI interactions on a web application. This formed the foundation of the Playwright skills I now use professionally at Qoria.

**What I specifically did:**
- Wrote Playwright tests in JavaScript using the Playwright Test runner
- Implemented Page Object pattern in JavaScript for reusable selectors and actions
- Handled asynchronous operations correctly — Playwright's auto-wait combined with explicit `await` where needed
- Used Playwright's `expect` API for UI assertions
- Generated HTML reports and traces for debugging failures

**Technical challenge:**
Async content loading was the main complexity. Playwright's auto-wait handles most cases, but for pages with heavy API calls I explicitly awaited network idle state before asserting. Learning when to rely on auto-wait versus when to be explicit was the key lesson.

**How to talk about it in an interview:**
- "This personal project prepared me to use Playwright professionally at Qoria"
- "I understand Playwright's browser context model — each test gets an isolated context, which matters for state management between tests"
- "Writing tests in JavaScript gives me insight into how the frontend team's code works — test code and product code are in the same language"
- If asked Playwright vs Selenium: "Playwright is better for modern SPAs with async rendering. Selenium is the choice when you need Java integration or legacy browser coverage. I've worked with both professionally."

---

### Project 4 — Qoria Lanka — EdTech Insight (Current Professional Role)

**What the project is:**
My current professional role (September 2025 – Present) as a Trainee Quality Engineer. EdTech Insight is Qoria's platform providing insights about student digital safety and wellbeing to schools and educators. I work on this product's QA team.

**What I specifically do:**
- Write Selenium and TestNG automated tests in Java for core platform flows
- Build Playwright automation in JavaScript for additional UI coverage
- Work on MCP (Model Context Protocol) and Agent integrations — connecting Claude-based AI agents to testing workflows for test generation assistance and defect pattern analysis
- Tests run through GitHub Actions pipelines triggered on PRs and pushes
- GCP-triggered test executions run as part of the deployment pipeline, validating builds before they progress
- Collaborate with QA and Dev teams to design, execute, and maintain automated test suites
- Handle test planning, defect reporting, and performance validation

**MCP / Agent Integration detail:**
At Qoria, I work with MCP and Agent tools as part of how the team explores AI-augmented testing. This involves integrating Claude-based agents that can interpret test output, assist with test case generation from acceptance criteria, and surface patterns across defect reports. This is genuinely frontier work in QA — and it means I understand how modern AI tools plug into a testing workflow, not just traditional scripted automation.

**CI/CD detail:**
The GitHub Actions workflow runs on every PR — install, configure environment, run Playwright tests headless, upload artifacts on failure. GCP-triggered pipelines validate deployments to cloud staging environments. I've worked on configuring the workflow YAML files and coordinating timing between deployment triggers and test execution.

**How to talk about it in an interview:**
- "This is my current role — I can speak to it with real technical detail"
- Mention MCP/Agent work: "I'm doing something that most QA engineers at any experience level haven't had exposure to yet — and I find that genuinely exciting"
- Mention GitHub Actions and GCP: "I've worked on real CI/CD infrastructure, not just theory"
- "I collaborate with both QA and Dev — embedded in the delivery team, not siloed in a separate QA department"

---

### Project 5 — Cerexio — Intern Software Engineer & QA Engineer (Previous Professional Role)

**What the project was:**
Six-month internship (March 2024 – September 2024) at Cerexio, a technology company focused on industrial analytics. I worked as an Intern Software Engineer and QA Engineer on enterprise applications built with Angular using PrimeNG components.

**What I specifically did:**
- Manual and automated testing of Angular PrimeNG enterprise applications
- UI testing and API testing using Postman
- Designed test cases for both positive and negative scenarios
- Tracked and reported bugs in JIRA with clear reproduction steps, severity, and priority
- Supported the automation framework setup for the team
- Collaborated in Agile ceremonies — sprint planning, daily standups, backlog grooming, retrospectives

**API testing with Postman — technical detail:**
I built organized Postman collections for each API module and wrote JavaScript test scripts to validate status codes, response body fields, and data types. Created Postman environments for development and staging — the same collection ran against both with a single variable swap. Wrote negative test cases as standard practice: missing required fields, invalid data types, unauthorized requests, malformed JSON. Each test included a description explaining what it validated and why.

**JIRA defect reporting — technical detail:**
I wrote defect reports that developers could act on without asking follow-up questions: exact reproduction steps, environment and browser details, expected versus actual behaviour with screenshots, and severity/priority classification. Good defect reports reduce back-and-forth time significantly — this is a skill that looks simple but takes practice to do well.

**How to talk about it in an interview:**
- "My first professional QA experience — six months on a real enterprise product"
- "This is where I built my API testing discipline with Postman and my defect reporting skills in JIRA"
- If asked about Angular PrimeNG: "The UI had complex custom dropdowns, date pickers, and data tables — testing these required understanding component state, not just static page elements"
- "I worked in Agile from day one, which shaped how I think about QA's role in a sprint"

---

### Project 6 — Diabetic Retinopathy Detection System (Ongoing — Final Year Project)

**What the project is:**
My university final year project (B.Sc. IT & Management, University of Moratuwa, ongoing). An AI-powered system with a chatbot component for diabetic retinopathy — a condition that can cause blindness if undetected. The system includes an AI chatbot that users interact with, a Python Machine Learning model that analyses retinal images, an NLP layer for natural language interactions, and a React.js frontend.

**Technical scope:**
- Python for ML model development and backend
- Machine Learning for retinal image classification
- NLP for chatbot interaction handling
- React.js for the frontend interface
- AI-driven output interpretation

**What this demonstrates from a QA perspective:**
- Testing ML models is fundamentally different from testing deterministic software — accuracy, precision, recall, and F1-score are quality metrics, and a test can "pass" while the model is still dangerously wrong
- Testing an NLP chatbot involves input variation, edge case prompts, and assessing output relevance — not just status codes
- Understanding false positives vs false negatives in an ML context directly maps to risk thinking in software testing: the cost of a missed defect versus a false alarm
- Fullstack exposure — I understand the system end to end, which makes me a better tester at any level of the stack

**How to talk about it in an interview:**
- "My final year project shows I can work across the full technical stack — AI model, Python backend, React frontend"
- "More importantly, building this taught me to think about non-binary quality metrics — which I bring into how I approach test strategy"
- If they're an AI or EdTech company: "Testing AI-driven features is increasingly part of every QA role. I have a direct project where I had to think carefully about what 'correct' means for a model's output — not just a 200 response code"
- "It also shows breadth beyond testing — I understand the systems I'm testing, not just the test scripts"

---

## SECTION 3 — STAR Stories

> Situation, Task, Action, Result. Practice each one until you can deliver it in 90-120 seconds. Know which question each story answers.

---

### STAR Story 1 — A Framework I Built
*Answers: "Tell me about a time you built something from scratch" / "Describe a testing framework you implemented"*

**Situation:**
When I started the Singer Page Testing project, there was no automation. Testing was entirely manual, which meant regression took significant time before any release and made frequent testing impractical.

**Task:**
Build an automation framework that was maintainable, could be run by future team members without deep technical knowledge, and produced reports that non-technical stakeholders could read and trust.

**Action:**
I chose Serenity BDD with Cucumber because the BDD layer meant test scenarios were written in Gherkin — readable by anyone on the team without reading Java code. I structured the project with a clear separation: feature files for business scenarios, step definitions mapping Gherkin to code, and page objects handling element interactions. I added Serenity reporting which auto-generates HTML reports with screenshots at each test step. I documented the setup in a README so the suite could be run with a single Maven command.

A specific challenge was the JavaScript-rendered navigation menu — my initial CSS selectors broke when the menu loaded in different states. I switched to XPath targeting text content and added explicit waits before navigation actions, which made the tests stable.

**Result:**
The framework covered the core user journeys — navigation, search, product browsing, account access — replacing the most time-consuming manual regression with automated scenarios. Serenity reports meant the product owner could see in plain English what was tested after each run, without reading code. It was my first complete framework build, which built my confidence in making architecture decisions independently.

---

### STAR Story 2 — API Testing Experience
*Answers: "Describe your experience with API testing" / "How do you approach testing backend services?"*

**Situation:**
At Cerexio, the product was an Angular PrimeNG enterprise application backed by REST APIs. When I joined, there was no structured API testing — developers tested their own endpoints manually and bugs were often found late, sometimes by clients.

**Task:**
As the QA intern, build API test coverage for the core endpoints — authentication, CRUD operations, and data reporting.

**Action:**
I used Postman because the team was small and everyone needed to be able to run tests, not just me. I created organized collections per API module and wrote JavaScript test scripts validating status codes, response body structure, field types, and business logic. I set up Postman environments for development and staging — the same tests ran against both with a single environment variable swap. I wrote negative test cases as standard: missing required fields, invalid data types, unauthorized requests. Each test was documented with a description explaining what it checked and why.

**Result:**
We started catching API bugs before they surfaced in the UI layer, reducing debugging time for the frontend team significantly. The Postman collections became the team's shared API documentation — developers used them to understand how endpoints behaved. When I left Cerexio, I handed over a documented, runnable API test suite that hadn't existed before I arrived.

Since Cerexio, I've been building RestAssured API automation in Java — structured test classes with RequestSpecification, DataProviders, and JSON Schema validation — to complement my Postman experience with a code-level framework.

---

### STAR Story 3 — CI/CD Pipeline Integration
*Answers: "Tell me about your CI/CD experience" / "How have you integrated automation into a delivery pipeline?"*

**Situation:**
At Qoria Lanka, the test suite existed but was being run manually before releases rather than automatically in the delivery pipeline. The feedback loop was slow — a developer would submit a PR, it would be reviewed and merged, and only then would QA find issues.

**Task:**
Integrate the Playwright test suite into GitHub Actions so tests ran automatically on every pull request. Also coordinate with GCP-triggered pipelines that run tests during deployment validation.

**Action:**
I worked on the GitHub Actions workflow YAML — configuring the pipeline to install Node.js and dependencies, install Playwright browsers, run tests in headless mode against a staging deployment, and upload test results as artifacts on failure. For GCP-triggered executions, the test suite runs automatically when a new build is deployed to the cloud environment, validating the deployment before it progresses further. Tests were configured to block PR merges on critical failures.

**Result:**
PRs now trigger automated test runs. Developers see results directly on GitHub before completing code review. We caught regressions in the first month that would previously have reached staging or production. The feedback loop for QA findings went from end-of-sprint discovery to same-day, on the PR itself.

---

### STAR Story 4 — Working in Agile Teams
*Answers: "Describe your experience in an Agile team" / "What is QA's role in Agile ceremonies?"*

**Situation:**
Both at Cerexio and at Qoria, I've worked in Agile teams running two-week sprints. I came from a university background where testing was a phase after development. In my first sprint at Cerexio, I quickly realized QA was expected from the very beginning, not at the end.

**Task:**
Understand how to contribute meaningfully to each ceremony and shift from "testing at the end" to "quality throughout the sprint."

**Action:**
In sprint planning, I started reviewing user stories for testability — asking things like "what should happen when the user enters invalid data?" or "how do we define done for this story?" These questions either clarified ambiguous requirements or flagged gaps before development started. In backlog grooming at Cerexio, I contributed to writing acceptance criteria alongside the product owner. In daily standups, I was transparent about testing progress and flagged blockers early. In retrospectives at Qoria, I raised observations about where bugs were being found late and what could be moved earlier in the cycle.

**Result:**
At Qoria, I became the person who spots acceptance criteria gaps in sprint planning — which the team found valuable because it reduced rework and "this needs clarification" comments during the actual testing phase. Working this way made me part of the delivery team, not an afterthought at the end of the sprint.

---

### STAR Story 5 — A Bug I Found That Mattered
*Answers: "Tell me about a significant bug you found" / "Give an example of a high-impact defect you discovered"*

**Situation:**
During regression testing at Qoria for a sprint release of EdTech Insight, I was testing a report export feature. The feature allowed school administrators to export student activity reports as CSV files for a selected date range.

**Task:**
Run regression tests on the export feature before the sprint release. The feature had already passed developer testing on the happy path.

**Action:**
During exploratory testing, I tested a date range that crossed a month boundary — for example, January 28 to February 4. The happy path (a range within a single month) worked correctly. But when the date range crossed a month boundary, the exported CSV contained data only from the first month — it silently dropped the second month's data. There was no error message. The file downloaded successfully with a 200 status code. A school administrator would have had no idea the data was incomplete.

I documented the bug with exact reproduction steps, the specific dates I used, a screenshot comparing the in-app data to the CSV content, and the expected versus actual record counts. I flagged it as high severity — a silent data integrity issue that users would trust without knowing it was wrong.

**Result:**
The developer found an off-by-one error in the date range query — the SQL `BETWEEN` clause was using the wrong month boundary. Fixed before release. The product owner thanked me specifically because data accuracy is critical for schools trusting the platform. After the fix, I added explicit cross-month-boundary tests to the regression suite as a permanent standard test case.

---

### STAR Story 6 — Improving a Process Through Automation
*Answers: "Tell me about a time you improved a process" / "How have you contributed beyond just running tests?"*

**Situation:**
At Cerexio, regression testing before each release was done entirely manually — a shared spreadsheet of test steps that took approximately two full days to work through. It was also error-prone: testers would miss steps or check the wrong environment.

**Task:**
I identified this as an opportunity to reduce manual effort and improve consistency. I proposed to my manager that I invest time automating the most repetitive parts of the regression checklist.

**Action:**
I analysed the regression spreadsheet and identified test cases that were: always run, entirely mechanical, and not dependent on frequently-changing test data. These were login flows, core navigation, user creation, and basic data display. I supported setting up a Selenium-based automation framework covering these cases, integrated into Maven so the suite ran with one command, and set up simple pass/fail output the team could act on immediately.

**Result:**
The automated subset ran in minutes versus several hours of manual work for the same cases. The QA team redirected that time to exploratory testing of new features — where human judgment adds real value. Regression coverage improved because we weren't skipping tests due to time pressure. The team formally adopted automation-first for new regression cases going forward — a cultural shift I contributed to early in my career.

---

## SECTION 4 — Honest Gap Handling

> These are tools or experiences you have limited depth in. Know exactly what to say. Honest confidence beats fake expertise in every interview.

---

### Appium (Mobile Testing)

**What you actually know:**
You understand what Appium is — it extends the Selenium WebDriver protocol to mobile apps on iOS and Android. Uses UIAutomator2 for Android and XCUITest for iOS. Locators are different from web — AccessibilityId, resource-id, XPath targeting mobile UI hierarchy.

**What to say:**
"I haven't had the opportunity to use Appium on a production project yet. My automation experience has been web-based — Selenium, Playwright, and Playwright on mobile viewports. I understand Appium's architecture: it uses the WebDriver protocol extended with mobile capabilities, and locator strategies target the native UI hierarchy rather than the DOM. My Selenium foundation transfers directly, and I'd be comfortable picking it up in a structured way."

**Don't say:** "I've used Appium a bit" unless you genuinely have. Interviewers follow up with one question that exposes bluffing immediately.

---

### Gauge Framework

**What you actually know:**
Gauge is a BDD test framework created by ThoughtWorks. It uses Markdown-based spec files instead of Gherkin. Step implementations work in Java, Python, JavaScript, or Ruby. It was created as a simpler alternative to Cucumber.

**What to say:**
"I've read about Gauge but haven't used it on a project. My BDD experience is with Cucumber and Serenity, where I built a framework from scratch. I understand Gauge's approach: specs in Markdown rather than Gherkin, which some teams find more natural. The underlying concept is the same — readable scenarios driving test implementation. Given that I built a Serenity+Cucumber framework from scratch, the Gauge learning curve would be manageable — the pattern is the same, the syntax is different."

**Follow-up ready:** Teams choose Gauge over Cucumber because Gauge Markdown is more natural English without rigid Given/When/Then, has simpler native data table support, and easier setup. ThoughtWorks uses it on their own projects.

---

### RestAssured (Beyond Basics)

**What you actually know:**
You understand RestAssured's fluent API — `given().when().then()` chaining, header setting, path and query parameters, response extraction with `.extract().path()`, Hamcrest matchers for assertions, RequestSpecification for reusable config, and JSON Schema validation with a schema file.

**What to say:**
"I've been studying and building RestAssured automation in Java and understand its fluent API well — given/when/then chaining, extracting responses, Hamcrest assertions, RequestSpecification for shared config. My production API testing experience is primarily Postman at Cerexio. At Qoria I'm focused on Selenium, TestNG, and Playwright. The Java integration feels natural given my Selenium background — I'd be productive with RestAssured quickly in a team context. Is RestAssured used heavily here? I'd like to understand the depth you need."

**Why this works:** Honest, shows real knowledge, asks a smart follow-up question that signals you're thinking about fit.

---

### "You Only Have 1.5 Years Experience But We Asked for 3+"

**What to say:**
"You're right that I'm at an earlier stage in years, and I want to be direct about that rather than oversell it. What I can offer is someone working on real, shipped products across two companies — not just academic projects. At Cerexio I was doing API testing on an enterprise Angular application from week one. At Qoria I'm building automation in the production CI pipeline and working on MCP and Agent integrations that most QA engineers at any experience level haven't had exposure to yet.

The quality of the exposure has been dense for the time. I've built frameworks rather than just used them, worked in real CI/CD pipelines, and I keep pushing my technical depth independently.

I'd ask you to evaluate my capability through this conversation rather than the year count alone — I'm happy to go as deep as you want on any technical area."

**Why this works:** You acknowledge the gap directly — which builds trust. You reframe the value proposition, mention the genuine differentiator (MCP/Agent work), and redirect to demonstrable competence. Don't apologize for your experience. Own it.

---

## SECTION 5 — Skills Confidence Matrix

| Skill | Evidence from My CV | Confidence |
|---|---|---|
| Selenium WebDriver | Singer BDD project (personal), Qoria Selenium+TestNG (professional) | High |
| TestNG | Qoria daily use — groups, parallel execution, @Before/@After | High |
| Java | Primary language at Qoria and Singer project — professional use | High |
| Python | Python+Pytest automation project, Diabetic Retinopathy ML/NLP project | High |
| Postman / API Testing | Cerexio — 6 months API testing on enterprise Angular app | High |
| Playwright | Qoria professional use (JavaScript), personal Playwright project | High |
| BDD / Cucumber | Singer — Serenity+Cucumber framework built from scratch | High |
| JIRA | Daily use at Cerexio — bug tracking, sprint management | High |
| MCP / Agent Integration | Active work at Qoria — Claude-based agent integrations for testing | Medium |
| GitHub Actions | Configured PR pipelines at Qoria — YAML, artifacts, headless runs | Medium |
| GCP Test Pipelines | GCP-triggered test executions at Qoria — deployment validation | Medium |
| JavaScript | Playwright tests at Qoria, personal Playwright project | Medium |
| Pytest | Python+Pytest project — fixtures, parametrize, conftest | Medium |
| Serenity BDD | Singer project — full framework build and Serenity HTML reports | Medium |
| React.js | Diabetic Retinopathy project frontend | Medium |
| Angular | Tested Angular PrimeNG apps at Cerexio | Medium |
| SQL | Used in project and coursework contexts | Medium |
| JMeter / Performance | Listed in CV skills — learning/project context, not production depth | Honest |
| Cypress | Listed in CV skills — understand architecture, limited project use | Honest |
| Jenkins | CI/CD concepts understood — not hands-on in production | Honest |
| Appium | Conceptual knowledge only — no project experience | Honest |
| RestAssured | Study-level and active learning — fluent API understood, not production | Honest |
| Gauge | Read documentation — not used on a project | Honest |
| Spring Boot | Listed in web frameworks — studied/coursework context | Honest |
| Vue.js | Employee Management System project (Hasthiya IT) | Honest |

**Confidence levels:**
- **High** = Used in production or built a real framework — can go deep in any interview question
- **Medium** = Used in a real project or professional context — some gaps in advanced usage
- **Honest** = Theoretical knowledge or limited project use — won't oversell this; will say what I know and what I don't

---

## SECTION 6 — Questions to Ask the Interviewer

> Ask at least 2-3 of these. They signal that you're thinking seriously about the role, not just trying to get any offer.

---

**Question 1: "How does QA fit into the development cycle here — are testers involved from the start of a sprint or brought in once code is complete?"**

*Why ask this:* It tells you immediately whether the team practices modern Agile quality or the old "QA is at the end of the waterfall" model. The answer reveals how much influence you'd have on requirements, design decisions, and acceptance criteria. A team that involves QA from sprint planning gives you a more impactful role even at a junior level.

---

**Question 2: "What does the automation coverage look like today, and what's the biggest gap the team wants to close in the next 6 months?"**

*Why ask this:* It shows you're thinking about contributing immediately. The answer tells you what you'd actually be working on. If they say "we have Selenium but no API automation," you can speak directly to your Postman and RestAssured experience. If they say "we have no automation," you know what you're signing up for. It opens a real conversation rather than staying in the abstract.

---

**Question 3: "I've been working on MCP and Agent integrations at my current role — is AI-augmented testing something this team is exploring, or is the focus on traditional automation for now?"**

*Why ask this:* This is a differentiator question. Your work with MCP and AI agents at Qoria is real and current — raising it shows you're aware of where QA tooling is heading and that your current work is relevant to the next five years of the field, not just the last five. It also opens a conversation where your actual current experience is a relevant talking point rather than a liability.

---

**Question 4: "What's the biggest quality challenge the team is facing right now — not in the future, but today?"**

*Why ask this:* Every team has a real pain point. This question shows you're practical and interested in solving real problems. Their answer will tell you whether the challenge is technical (flaky tests, no CI), cultural (developers bypassing testing), or process-related (no test environments, poor test data management). Each type of answer gives you real information about what this role involves day to day.

---

**Question 5: "What does growth look like for a QA engineer here — what would a strong QA engineer be doing differently in 2 years compared to day one?"**

*Why ask this:* This tells you if there's a real career path or if the role is a static function. A company where QA engineers grow into quality architects, security testing specialists, or tech leads is a very different environment from one where the role is flat. It also signals ambition on your part — which reduces the perception that you're just taking any offer and increases the sense that you're choosing this role deliberately.

---

*This document is yours. Internalize the stories, trust the preparation, and walk in knowing you've done the work. You're not trying to seem experienced — you are experienced in the ways that matter for someone at your stage of career. Show that clearly, and the right team will recognize it.*
