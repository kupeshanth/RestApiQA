# CI/CD — Jenkins + GitLab + GitHub Actions | Complete Guide for QA

---

## Table of Contents

1. [What is CI/CD and Why QA Cares](#1-what-is-cicd-and-why-qa-cares)
2. [Jenkins — Architecture and Setup](#2-jenkins--architecture-and-setup)
3. [Jenkins — Freestyle vs Pipeline Jobs](#3-jenkins--freestyle-vs-pipeline-jobs)
4. [Jenkins — Declarative Pipeline (Full Example)](#4-jenkins--declarative-pipeline-full-example)
5. [Jenkins — Triggers, Environment Variables, Blue Ocean](#5-jenkins--triggers-environment-variables-blue-ocean)
6. [Jenkins — Integrating Test Reports](#6-jenkins--integrating-test-reports)
7. [GitLab CI/CD — Full Configuration Guide](#7-gitlab-cicd--full-configuration-guide)
8. [GitHub Actions — Full Workflow Guide](#8-github-actions--full-workflow-guide)
9. [Comparing Jenkins vs GitLab vs GitHub Actions](#9-comparing-jenkins-vs-gitlab-vs-github-actions)
10. [Environment-Specific Testing](#10-environment-specific-testing)
11. [Making Tests CI-Ready](#11-making-tests-ci-ready)
12. [Troubleshooting CI/CD Issues](#12-troubleshooting-cicd-issues)
13. [Interview Q&A (8 Questions)](#13-interview-qa-8-questions)

---

## 1. What is CI/CD and Why QA Cares

### Definitions

**Continuous Integration (CI)** is the practice of merging developer code changes into a shared repository frequently — multiple times a day. Each merge triggers an automated build and test run, so problems are caught early.

**Continuous Delivery (CD)** extends CI by automatically preparing every passing build for release to a staging or production environment.

**Continuous Deployment** goes one step further: every passing build is automatically deployed to production without any manual gate.

### The CI/CD Pipeline Flow

```
Developer pushes code
        ↓
Source Control (Git repo) triggers pipeline
        ↓
Build stage — compile, package
        ↓
Unit tests run
        ↓
Integration / API tests run
        ↓
UI / E2E tests run (smoke or regression)
        ↓
Reports published
        ↓
Deploy to staging / prod (CD stage)
        ↓
Notifications sent (Slack, email)
```

### Why QA Engineers Must Care About CI/CD

| Reason | Impact on QA |
|---|---|
| Tests run automatically on every commit | QA gets fast feedback without manual trigger |
| Broken builds are caught before merge | Fewer defects reach QA |
| Flaky tests block pipelines | QA must maintain reliable, stable tests |
| Reports are published automatically | Stakeholders can view results without QA sending emails |
| Environment parity | CI runs on clean containers — tests must be environment-agnostic |
| Shift-left testing | QA is involved earlier, designing tests that fit the pipeline |

**Key insight for QA:** If your test suite cannot run unattended in a headless, containerised environment, it is not CI-ready. CI/CD forces good test design discipline.

---

## 2. Jenkins — Architecture and Setup

### Jenkins Architecture

```
Jenkins Master (Controller)
    ├── Job definitions
    ├── Plugin management
    ├── Security, credentials
    ├── Build scheduling
    └── Distributes work to...

Jenkins Agents (Nodes)
    ├── Agent 1 — Linux (runs Selenium, Playwright)
    ├── Agent 2 — Windows (runs IE/Edge tests)
    └── Agent 3 — macOS (runs Safari tests)
```

- **Master/Controller**: Orchestrates pipelines, stores config, presents the UI
- **Agent/Node**: Does the actual work (running tests, compiling)
- **Executor**: A slot on an agent that runs one job at a time
- **Workspace**: Directory on the agent where build files live

### Installing Jenkins (Ubuntu/Linux)

```bash
# Install Java (Jenkins requires Java 11 or 17)
sudo apt update
sudo apt install openjdk-17-jdk -y

# Add Jenkins repo
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | \
  sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null

echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian-stable binary/ | \
  sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null

sudo apt update
sudo apt install jenkins -y

# Start Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

Jenkins runs on port 8080 by default: `http://localhost:8080`

### Essential Plugins to Install

Navigate to **Manage Jenkins > Manage Plugins > Available**

| Plugin | Purpose |
|---|---|
| Git Plugin | Clone Git repositories |
| Pipeline | Enables Jenkinsfile pipelines |
| Blue Ocean | Modern pipeline UI |
| Maven Integration | Build Maven projects |
| NodeJS | Install/use Node.js on agents |
| HTML Publisher | Publish HTML test reports |
| JUnit | Publish JUnit XML reports |
| Email Extension | Send rich HTML emails on failure |
| Credentials Binding | Inject secrets into builds |
| Workspace Cleanup | Clean workspace before/after builds |
| Allure Jenkins Plugin | Publish Allure reports |
| Slack Notification | Send Slack alerts |
| Docker Pipeline | Run steps inside Docker containers |

---

## 3. Jenkins — Freestyle vs Pipeline Jobs

### Freestyle Job

- Configured entirely through the UI
- Simple, good for quick one-off tasks
- Hard to version-control (config stored in XML on Jenkins master)
- Not recommended for complex test suites

**Freestyle configuration steps:**
1. New Item → Freestyle Project → name it
2. Source Code Management: add Git URL + credentials
3. Build Triggers: Poll SCM with cron or "Build after other projects"
4. Build: Execute shell / Invoke Maven targets
5. Post-build actions: Publish JUnit results, email notification

### Pipeline Job (Recommended)

- Defined in a `Jenkinsfile` committed to your repository
- Version-controlled alongside the code
- Supports complex logic: stages, parallel, conditions, loops
- Two syntax options: **Declarative** (structured, recommended) and **Scripted** (Groovy, flexible)

**Declarative** is preferred for QA pipelines because it is readable, enforces structure, and catches errors early.

---

## 4. Jenkins — Declarative Pipeline (Full Example)

### Complete Jenkinsfile for a Java + Playwright/Selenium Project

```groovy
pipeline {
    // Run on any available agent; specify label for specific agents
    agent any
    // agent { label 'linux-selenium' }

    // Tool configurations — must match tool names in Global Tool Configuration
    tools {
        maven 'Maven 3.9.6'
        jdk   'JDK 17'
        nodejs 'NodeJS 18'
    }

    // Environment variables available to all stages
    environment {
        APP_ENV        = 'staging'
        BASE_URL       = 'https://staging.myapp.com'
        BROWSER        = 'chromium'
        REPORTS_DIR    = 'target/surefire-reports'
        PLAYWRIGHT_DIR = 'playwright-report'
        // Credentials stored in Jenkins Credentials store
        APP_USER       = credentials('staging-username')
        APP_PASS       = credentials('staging-password')
    }

    // Build parameters (shown in "Build with Parameters" UI)
    parameters {
        choice(name: 'TEST_SUITE', choices: ['smoke', 'regression', 'full'], description: 'Select test suite')
        string(name: 'BROWSER_PARAM', defaultValue: 'chromium', description: 'Browser for Playwright tests')
        booleanParam(name: 'SEND_EMAIL', defaultValue: true, description: 'Send email on failure?')
    }

    // Triggers
    triggers {
        // Poll SCM every 5 minutes (use webhooks in production)
        pollSCM('H/5 * * * *')
        // Scheduled run — every day at 2 AM
        cron('0 2 * * *')
        // Trigger when upstream job finishes
        // upstream(upstreamProjects: 'build-job', threshold: hudson.model.Result.SUCCESS)
    }

    options {
        // Timeout the whole pipeline after 60 minutes
        timeout(time: 60, unit: 'MINUTES')
        // Keep last 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Add timestamps to log
        timestamps()
        // Fail fast if any parallel branch fails
        // parallelsAlwaysFailFast()
    }

    stages {

        // ── STAGE 1: CHECKOUT ──────────────────────────────────────────
        stage('Checkout') {
            steps {
                // Clean workspace before checkout
                cleanWs()
                checkout scm
                echo "Checked out branch: ${env.GIT_BRANCH}"
                echo "Commit: ${env.GIT_COMMIT}"
            }
        }

        // ── STAGE 2: INSTALL DEPENDENCIES ─────────────────────────────
        stage('Install Dependencies') {
            parallel {
                stage('Maven Dependencies') {
                    steps {
                        sh 'mvn dependency:resolve -q'
                    }
                }
                stage('Node Dependencies') {
                    steps {
                        sh 'npm ci'
                        // Install Playwright browsers
                        sh 'npx playwright install chromium --with-deps'
                    }
                }
            }
        }

        // ── STAGE 3: BUILD ─────────────────────────────────────────────
        stage('Build') {
            steps {
                sh 'mvn clean compile -q'
            }
        }

        // ── STAGE 4: SMOKE TESTS ───────────────────────────────────────
        stage('Smoke Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    expression { return params.TEST_SUITE == 'smoke' || params.TEST_SUITE == 'full' }
                }
            }
            steps {
                sh """
                    mvn test \
                      -Dtest.suite=smoke \
                      -Denv=${APP_ENV} \
                      -Dbase.url=${BASE_URL} \
                      -Dsurefire.failIfNoSpecifiedTests=false
                """
            }
            post {
                always {
                    junit 'target/surefire-reports/**/*.xml'
                }
                failure {
                    echo 'Smoke tests failed — blocking regression'
                }
            }
        }

        // ── STAGE 5: REGRESSION TESTS (PARALLEL BROWSERS) ─────────────
        stage('Regression Tests') {
            when {
                anyOf {
                    branch 'main'
                    expression { return params.TEST_SUITE == 'regression' || params.TEST_SUITE == 'full' }
                }
            }
            parallel {
                stage('Chromium') {
                    steps {
                        sh """
                            npx playwright test \
                              --project=chromium \
                              --reporter=junit,html \
                              --output=test-results/chromium
                        """
                    }
                    post {
                        always {
                            junit 'test-results/chromium/*.xml'
                        }
                    }
                }
                stage('Firefox') {
                    steps {
                        sh """
                            npx playwright test \
                              --project=firefox \
                              --reporter=junit,html \
                              --output=test-results/firefox
                        """
                    }
                    post {
                        always {
                            junit 'test-results/firefox/*.xml'
                        }
                    }
                }
            }
        }

        // ── STAGE 6: PUBLISH REPORTS ───────────────────────────────────
        stage('Publish Reports') {
            steps {
                // Publish Surefire HTML report
                publishHTML([
                    allowMissing:          false,
                    alwaysLinkToLastBuild: true,
                    keepAll:               true,
                    reportDir:             'target/surefire-reports',
                    reportFiles:           'index.html',
                    reportName:            'Surefire Report'
                ])

                // Publish Playwright HTML report
                publishHTML([
                    allowMissing:          true,
                    alwaysLinkToLastBuild: true,
                    keepAll:               true,
                    reportDir:             'playwright-report',
                    reportFiles:           'index.html',
                    reportName:            'Playwright Report'
                ])

                // Archive artifacts (XML results for Allure etc.)
                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml, test-results/**/*.xml',
                                 allowEmptyArchive: true
            }
        }
    }

    // ── POST ACTIONS (run after all stages) ───────────────────────────
    post {
        always {
            echo "Pipeline finished. Status: ${currentBuild.result}"
        }
        failure {
            script {
                if (params.SEND_EMAIL) {
                    emailext(
                        subject: "FAILED: Jenkins Job '${env.JOB_NAME}' [${env.BUILD_NUMBER}]",
                        body: """
                            <h2>Build Failed</h2>
                            <p><b>Job:</b> ${env.JOB_NAME}</p>
                            <p><b>Build #:</b> ${env.BUILD_NUMBER}</p>
                            <p><b>Branch:</b> ${env.GIT_BRANCH}</p>
                            <p><b>Commit:</b> ${env.GIT_COMMIT}</p>
                            <p><b>Console:</b> <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>
                            <p><b>Report:</b> <a href="${env.BUILD_URL}Playwright_Report">Playwright Report</a></p>
                        """,
                        mimeType: 'text/html',
                        to: 'qa-team@company.com',
                        replyTo: 'no-reply@company.com',
                        attachLog: false
                    )
                }
            }
        }
        success {
            echo 'All tests passed!'
        }
        unstable {
            echo 'Some tests failed — build marked unstable.'
        }
        cleanup {
            cleanWs()
        }
    }
}
```

---

## 5. Jenkins — Triggers, Environment Variables, Blue Ocean

### Trigger Types

| Trigger | Syntax | When to Use |
|---|---|---|
| Poll SCM | `pollSCM('H/5 * * * *')` | If webhooks unavailable |
| Cron schedule | `cron('0 2 * * 1-5')` | Nightly regression runs |
| Upstream job | `upstream(upstreamProjects: 'build-job')` | After build completes |
| GitHub webhook | Configured on GitHub side; plugin receives event | Instant trigger on push |
| Manual | No trigger — user clicks "Build" | On-demand runs |

### Cron Syntax Quick Reference

```
┌─── minute (0-59)
│ ┌─── hour (0-23)
│ │ ┌─── day of month (1-31)
│ │ │ ┌─── month (1-12)
│ │ │ │ ┌─── day of week (0-7, 0=Sunday)
│ │ │ │ │
* * * * *

H  = Jenkins hash (distributes load)
H/5 * * * *   = every 5 minutes
0 2 * * *     = every day at 2 AM
0 2 * * 1-5   = Mon-Fri at 2 AM
0 0 * * 0     = every Sunday at midnight
```

### Environment Variables in Jenkins

**Built-in Variables (always available):**

```groovy
env.BUILD_NUMBER    // "42"
env.BUILD_URL       // "http://jenkins/job/my-job/42/"
env.JOB_NAME        // "my-job"
env.GIT_BRANCH      // "origin/main"
env.GIT_COMMIT      // "abc1234..."
env.WORKSPACE       // "/var/jenkins/workspace/my-job"
env.NODE_NAME       // "agent-1" or "master"
```

**Defining Custom Variables:**

```groovy
environment {
    // Plain value
    BASE_URL = 'https://staging.myapp.com'

    // From Jenkins Credentials (username:password binding)
    DB_CREDS = credentials('db-credentials-id')
    // Creates: DB_CREDS_USR and DB_CREDS_PSW automatically

    // From credentials (secret text)
    API_KEY = credentials('api-key-secret-id')
}
```

**Accessing in shell:**

```bash
sh "mvn test -Dbase.url=${BASE_URL} -Dapi.key=${API_KEY}"
```

### Blue Ocean

Blue Ocean is Jenkins's modern pipeline visualization UI.

Install: **Manage Plugins → Available → Blue Ocean**

Features:
- Visual pipeline graph showing each stage as a node
- Red/green status per stage, not just overall build
- Parallel branches shown side-by-side
- Direct link from UI to failing test details
- Simplified log viewer per stage

Access: `http://localhost:8080/blue`

---

## 6. Jenkins — Integrating Test Reports

### JUnit XML Reports (Surefire, TestNG)

```groovy
post {
    always {
        junit allowEmptyResults: true,
              testResults: 'target/surefire-reports/**/*.xml'
    }
}
```

Jenkins renders pass/fail counts, failure trends, and individual test details.

### Playwright HTML Reports

```groovy
publishHTML([
    allowMissing:          true,
    alwaysLinkToLastBuild: true,
    keepAll:               true,
    reportDir:             'playwright-report',
    reportFiles:           'index.html',
    reportName:            'Playwright Report'
])
```

### Archiving Artifacts

```groovy
archiveArtifacts artifacts: 'playwright-report/**/*',
                 allowEmptyArchive: true,
                 fingerprint: true
```

### Allure Reports (Advanced)

```groovy
// Requires Allure Jenkins Plugin + allure commandline tool installed
allure([
    includeProperties: false,
    jdk:               '',
    properties:        [],
    reportBuildPolicy: 'ALWAYS',
    results:           [[path: 'target/allure-results']]
])
```

---

## 7. GitLab CI/CD — Full Configuration Guide

GitLab CI/CD is configured by a `.gitlab-ci.yml` file at the root of your repository. When you push, GitLab's CI system reads this file and executes the pipeline.

### Core Concepts

| Concept | Description |
|---|---|
| Pipeline | The entire automation process triggered by a push |
| Stage | A group of jobs that run together |
| Job | A single unit of work (e.g., run tests) |
| Runner | A server/agent that executes jobs |
| Artifact | File(s) produced by a job, passed to later stages |
| Cache | Directories persisted between pipeline runs |

### Full .gitlab-ci.yml Example — Java + Playwright Project

```yaml
# ── GLOBAL CONFIGURATION ──────────────────────────────────────────────────

# Default Docker image for all jobs (can be overridden per job)
image: eclipse-temurin:17-jdk

# Stage execution order — jobs in same stage run in parallel
stages:
  - install
  - build
  - smoke
  - regression
  - report

# Variables available to all jobs
variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  APP_ENV: "staging"
  BASE_URL: "https://staging.myapp.com"
  # Sensitive vars go in GitLab UI: Settings → CI/CD → Variables

# ── CACHE ─────────────────────────────────────────────────────────────────
# Persist Maven local repo across pipeline runs — saves download time
.maven-cache: &maven-cache
  cache:
    key: "$CI_COMMIT_REF_SLUG-maven"
    paths:
      - .m2/repository
    policy: pull-push   # pull at start, push at end

.node-cache: &node-cache
  cache:
    key: "$CI_COMMIT_REF_SLUG-node"
    paths:
      - node_modules/
      - ~/.cache/ms-playwright
    policy: pull-push

# ── INSTALL STAGE ─────────────────────────────────────────────────────────
install:maven:
  stage: install
  <<: *maven-cache
  script:
    - mvn dependency:resolve -q
  only:
    - branches
    - merge_requests

install:node:
  stage: install
  image: mcr.microsoft.com/playwright:v1.44.0-jammy
  <<: *node-cache
  script:
    - npm ci
    - npx playwright install --with-deps
  only:
    - branches
    - merge_requests

# ── BUILD STAGE ───────────────────────────────────────────────────────────
build:
  stage: build
  <<: *maven-cache
  script:
    - mvn clean compile -q
  artifacts:
    paths:
      - target/classes/
    expire_in: 1 hour
  only:
    - branches
    - merge_requests

# ── SMOKE STAGE ───────────────────────────────────────────────────────────
smoke:tests:
  stage: smoke
  <<: *maven-cache
  script:
    - mvn test -Dtest.suite=smoke -Denv=${APP_ENV} -Dbase.url=${BASE_URL}
  artifacts:
    when: always     # collect even if job fails
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/surefire-reports/
    expire_in: 7 days
  only:
    - main
    - develop
    - merge_requests

# ── REGRESSION STAGE — PARALLEL BROWSERS ──────────────────────────────────
.playwright-base: &playwright-base
  stage: regression
  image: mcr.microsoft.com/playwright:v1.44.0-jammy
  <<: *node-cache
  script:
    - npx playwright test --project=${BROWSER} --reporter=junit,html
  artifacts:
    when: always
    paths:
      - playwright-report/
      - test-results/
    reports:
      junit: test-results/**/*.xml
    expire_in: 7 days
  only:
    - main

regression:chromium:
  <<: *playwright-base
  variables:
    BROWSER: chromium

regression:firefox:
  <<: *playwright-base
  variables:
    BROWSER: firefox

regression:webkit:
  <<: *playwright-base
  variables:
    BROWSER: webkit

# ── MERGE REQUEST PIPELINE — LIGHTER TEST RUN ─────────────────────────────
mr:smoke:
  stage: smoke
  image: mcr.microsoft.com/playwright:v1.44.0-jammy
  <<: *node-cache
  script:
    - npx playwright test --project=chromium --grep "@smoke"
  artifacts:
    when: always
    reports:
      junit: test-results/**/*.xml
    paths:
      - playwright-report/
    expire_in: 3 days
  only:
    - merge_requests

# ── REPORT STAGE ──────────────────────────────────────────────────────────
pages:
  stage: report
  script:
    # Copy Playwright report to public/ for GitLab Pages hosting
    - mkdir -p public
    - cp -r playwright-report/* public/ || true
  artifacts:
    paths:
      - public
    expire_in: 30 days
  only:
    - main

# ── RULES ALTERNATIVE (modern syntax, replaces only/except) ───────────────
# Example job using rules:
# regression:rules-example:
#   stage: regression
#   script:
#     - npx playwright test
#   rules:
#     - if: '$CI_COMMIT_BRANCH == "main"'
#       when: always
#     - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
#       when: manual
#     - when: never
```

### Key GitLab CI Variables

| Variable | Value |
|---|---|
| `$CI_COMMIT_REF_NAME` | Branch or tag name |
| `$CI_COMMIT_SHA` | Full commit hash |
| `$CI_PIPELINE_SOURCE` | `push`, `merge_request_event`, `schedule` |
| `$CI_MERGE_REQUEST_IID` | MR number |
| `$CI_PROJECT_DIR` | Workspace path |
| `$CI_REGISTRY_IMAGE` | Docker registry URL |
| `$GITLAB_USER_EMAIL` | User who triggered |

### Setting Secrets in GitLab

**Settings → CI/CD → Variables → Add Variable**

- Mark as **Protected** (only available on protected branches)
- Mark as **Masked** (hidden in logs)

Access in pipeline: `$MY_SECRET_VAR`

---

## 8. GitHub Actions — Full Workflow Guide

GitHub Actions workflows live in `.github/workflows/` as YAML files.

### Full Workflow — Java + Maven + Playwright

```yaml
# .github/workflows/test-suite.yml

name: QA Test Suite

# ── TRIGGERS ──────────────────────────────────────────────────────────────
on:
  push:
    branches:
      - main
      - develop
  pull_request:
    branches:
      - main
  schedule:
    # Nightly run at 1 AM UTC, Mon-Fri
    - cron: '0 1 * * 1-5'
  workflow_dispatch:        # Allow manual trigger from GitHub UI
    inputs:
      test_suite:
        description: 'Test suite to run'
        required: true
        default: 'smoke'
        type: choice
        options:
          - smoke
          - regression
          - full

# Cancel in-progress runs when a new push happens to same PR
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

# ── ENVIRONMENT VARIABLES ─────────────────────────────────────────────────
env:
  JAVA_VERSION: '17'
  NODE_VERSION: '18'
  APP_ENV: 'staging'
  BASE_URL: 'https://staging.myapp.com'

# ── JOBS ─────────────────────────────────────────────────────────────────
jobs:

  # ── JOB 1: BUILD ─────────────────────────────────────────────────────
  build:
    name: Build
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven         # Cache ~/.m2 automatically

      - name: Build with Maven
        run: mvn clean compile -q

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: build-output
          path: target/classes/
          retention-days: 1

  # ── JOB 2: SMOKE TESTS (JAVA + SELENIUM/TESTNG) ───────────────────────
  smoke-tests:
    name: Smoke Tests
    runs-on: ubuntu-latest
    needs: build

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Run smoke tests
        run: |
          mvn test \
            -Dtest.suite=smoke \
            -Denv=${{ env.APP_ENV }} \
            -Dbase.url=${{ env.BASE_URL }} \
            -Dusername=${{ secrets.APP_USERNAME }} \
            -Dpassword=${{ secrets.APP_PASSWORD }}

      - name: Publish Test Results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()
        with:
          files: 'target/surefire-reports/**/*.xml'

      - name: Upload Surefire Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: surefire-reports
          path: target/surefire-reports/
          retention-days: 7

  # ── JOB 3: PLAYWRIGHT — MATRIX STRATEGY (MULTI-BROWSER) ──────────────
  playwright-tests:
    name: Playwright — ${{ matrix.browser }}
    runs-on: ubuntu-latest
    needs: smoke-tests
    if: github.ref == 'refs/heads/main' || github.event_name == 'schedule'

    strategy:
      fail-fast: false     # Don't cancel other browsers if one fails
      matrix:
        browser: [chromium, firefox, webkit]

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'npm'

      - name: Install Node dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install ${{ matrix.browser }} --with-deps

      - name: Run Playwright tests
        run: |
          npx playwright test \
            --project=${{ matrix.browser }} \
            --reporter=junit,html
        env:
          BASE_URL:    ${{ env.BASE_URL }}
          APP_USERNAME: ${{ secrets.APP_USERNAME }}
          APP_PASSWORD: ${{ secrets.APP_PASSWORD }}

      - name: Upload Playwright HTML Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report-${{ matrix.browser }}
          path: playwright-report/
          retention-days: 14

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results-${{ matrix.browser }}
          path: test-results/
          retention-days: 7

  # ── JOB 4: NOTIFY ON FAILURE ──────────────────────────────────────────
  notify:
    name: Notify on Failure
    runs-on: ubuntu-latest
    needs: [smoke-tests, playwright-tests]
    if: failure()

    steps:
      - name: Send Slack notification
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          channel: '#qa-alerts'
          text: 'QA Pipeline failed on ${{ github.ref }}'
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

### Using Secrets in GitHub Actions

1. Go to **Settings → Secrets and variables → Actions → New repository secret**
2. Access in workflow: `${{ secrets.MY_SECRET }}`
3. Never print secrets in logs; GitHub automatically masks them

### Reusable Workflows

```yaml
# .github/workflows/reusable-playwright.yml
on:
  workflow_call:
    inputs:
      browser:
        required: true
        type: string
      base-url:
        required: true
        type: string
    secrets:
      APP_USERNAME:
        required: true

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npx playwright test --project=${{ inputs.browser }}
        env:
          BASE_URL: ${{ inputs.base-url }}
          APP_USERNAME: ${{ secrets.APP_USERNAME }}
```

---

## 9. Comparing Jenkins vs GitLab vs GitHub Actions

| Feature | Jenkins | GitLab CI/CD | GitHub Actions |
|---|---|---|---|
| **Setup** | Self-hosted, manual install | Built-in to GitLab | Built-in to GitHub |
| **Config file** | Jenkinsfile (Groovy) | .gitlab-ci.yml (YAML) | .github/workflows/*.yml (YAML) |
| **Hosted option** | Jenkins.io (limited) | GitLab.com (free tier) | GitHub.com (free tier) |
| **Agents/Runners** | Jenkins agents | GitLab runners | GitHub-hosted runners |
| **Plugin ecosystem** | 1800+ plugins | Limited but built-in features | Marketplace actions |
| **Parallel execution** | Yes, parallel stages | Yes, same-stage jobs run parallel | Yes, matrix strategy |
| **Secret management** | Credentials plugin | CI/CD Variables (masked/protected) | Repository/org secrets |
| **UI quality** | Dated (Blue Ocean better) | Modern, integrated | Modern, simple |
| **Cost** | Free (infrastructure cost) | Free tier + paid | Free tier (2000 min/month) |
| **Learning curve** | High (Groovy DSL) | Medium (YAML + GitLab concepts) | Low (YAML, great docs) |
| **Best for** | Large enterprise, complex pipelines | Teams already on GitLab | Teams on GitHub |
| **Docker support** | Via plugin | Native, first-class | Via `runs-on` + services |
| **Artifacts** | Archive + HTML Publisher | `artifacts:` block | `upload-artifact` action |
| **Test reports** | JUnit plugin + HTML Publisher | `reports: junit:` (native) | Third-party actions |

### When to Choose Each

- **Jenkins**: Your company already uses it; you need Windows agents; you have complex multi-repo pipelines; you need very granular plugin control.
- **GitLab CI**: Your code is on GitLab; you want CI deeply integrated with MR workflow, issue tracking, and container registry.
- **GitHub Actions**: Your code is on GitHub; you want simplicity and the huge marketplace of actions; you are a startup or open source project.

---

## 10. Environment-Specific Testing

### Strategy: Single Codebase, Multiple Environments

Never hardcode environment-specific values. Use environment variables to switch between dev/staging/prod.

### Java / Maven — Profiles Approach

```xml
<!-- pom.xml -->
<profiles>
    <profile>
        <id>dev</id>
        <properties>
            <base.url>https://dev.myapp.com</base.url>
            <db.url>jdbc:postgresql://dev-db/myapp</db.url>
        </properties>
    </profile>
    <profile>
        <id>staging</id>
        <properties>
            <base.url>https://staging.myapp.com</base.url>
            <db.url>jdbc:postgresql://staging-db/myapp</db.url>
        </properties>
    </profile>
    <profile>
        <id>prod</id>
        <properties>
            <base.url>https://myapp.com</base.url>
        </properties>
    </profile>
</profiles>
```

Run with: `mvn test -Pstaging`

### Playwright — Environment Config

```typescript
// playwright.config.ts
const ENV = process.env.APP_ENV || 'staging';

const config: PlaywrightTestConfig = {
    use: {
        baseURL: {
            dev:     'https://dev.myapp.com',
            staging: 'https://staging.myapp.com',
            prod:    'https://myapp.com',
        }[ENV] ?? 'https://staging.myapp.com',
    },
};
```

Run with: `APP_ENV=staging npx playwright test`

### Jenkins — Environment Gates

```groovy
stage('Deploy to Prod') {
    when {
        branch 'main'
    }
    input {
        message 'Deploy to production?'
        ok 'Deploy'
        submitter 'qa-lead,release-manager'
    }
    steps {
        sh './deploy.sh prod'
    }
}
```

---

## 11. Making Tests CI-Ready

CI environments are headless, containerised, and stateless. Tests that rely on local setup will break.

### Checklist for CI-Ready Tests

#### 1. Run in Headless Mode

```java
// Selenium — headless Chrome
ChromeOptions options = new ChromeOptions();
options.addArguments("--headless=new");
options.addArguments("--no-sandbox");
options.addArguments("--disable-dev-shm-usage");  // Crucial in Docker
options.addArguments("--window-size=1920,1080");

WebDriver driver = new ChromeDriver(options);
```

```typescript
// Playwright — headless by default in CI
// playwright.config.ts
use: {
    headless: process.env.CI === 'true',  // headless in CI, headed locally
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
}
```

#### 2. No Hardcoded Paths

```java
// WRONG
String reportPath = "C:\\Users\\Kupeshanth\\reports\\test.html";

// RIGHT — use project-relative path
String reportPath = System.getProperty("user.dir") + "/target/reports/test.html";
```

#### 3. Explicit Waits, Not Thread.sleep()

```java
// WRONG in CI (timing-sensitive)
Thread.sleep(3000);

// RIGHT — wait for condition
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));
```

#### 4. Configurable Timeouts

```java
// Read from system property, fallback to default
int timeout = Integer.parseInt(System.getProperty("wait.timeout", "30"));
```

#### 5. Independent Tests

Each test must set up its own data and clean up after itself. No test should depend on another test having run first.

#### 6. Handle --no-sandbox in Docker

Chrome requires `--no-sandbox` inside Docker containers.

#### 7. Use Relative URLs

```typescript
// WRONG
await page.goto('https://staging.myapp.com/login');

// RIGHT — uses baseURL from config
await page.goto('/login');
```

#### 8. Retry Flaky Tests (Playwright)

```typescript
// playwright.config.ts
retries: process.env.CI ? 2 : 0,  // Retry twice only in CI
```

---

## 12. Troubleshooting CI/CD Issues

### Problem: Tests Pass Locally, Fail in CI

| Likely Cause | Solution |
|---|---|
| Headed browser required | Add headless flags |
| Missing `--no-sandbox` | Add `--no-sandbox --disable-dev-shm-usage` to Chrome options |
| Hardcoded base URL | Use env var / config |
| Different JDK version | Pin Java version in CI config |
| Missing test data | Ensure setup creates its own data |
| Timing issues | Increase explicit wait timeouts for CI |
| Different screen resolution | Set window size explicitly |

### Problem: Browser Not Found

```bash
# Playwright — install browsers explicitly in pipeline
npx playwright install chromium --with-deps

# Or use the official Playwright Docker image
image: mcr.microsoft.com/playwright:v1.44.0-jammy
```

```bash
# Selenium — ensure ChromeDriver matches Chrome version
# Use WebDriverManager to auto-manage
# pom.xml dependency:
# io.github.bonigarcia:webdrivermanager:5.x.x
```

### Problem: Permission Errors

```bash
# Jenkins workspace permission
sudo chown -R jenkins:jenkins /var/jenkins_home

# Make scripts executable
chmod +x gradlew mvnw

# In Docker, run as non-root
USER jenkins
```

### Problem: Artifact Not Found

```yaml
# GitLab — check path is correct and job ran
artifacts:
  when: always        # IMPORTANT: collect even on failure
  paths:
    - target/surefire-reports/
```

```yaml
# GitHub Actions — confirm path
- uses: actions/upload-artifact@v4
  if: always()         # IMPORTANT: upload even on failure
  with:
    name: reports
    path: playwright-report/
```

### Problem: Out of Memory in CI

```bash
# Maven
export MAVEN_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m"

# Node/Playwright
export NODE_OPTIONS=--max-old-space-size=4096
```

### Problem: Tests Flaky in CI but Not Locally

Causes: slower CI machine, race conditions, shared state

Solutions:
- Add explicit waits
- Enable retries
- Run tests in parallel to expose race conditions locally
- Check for state shared between tests (static fields, shared DB)
- Use unique test data per test run (timestamp in test data names)

### Problem: Cannot Connect to Remote URL

```bash
# Check if environment variable is set
echo $BASE_URL

# In Jenkins, add this to troubleshoot
sh 'env | sort'
sh 'curl -I ${BASE_URL}/health'
```

---

## 13. Interview Q&A (8 Questions)

**Q1: What is the difference between CI and CD?**

CI (Continuous Integration) is the practice of automatically building and testing code every time a developer commits. It catches integration problems early. CD (Continuous Delivery) automatically prepares every passing build for deployment to staging, with a manual gate before production. Continuous Deployment removes even that gate and deploys automatically to production on every green build.

**Q2: Why is it important for QA to understand CI/CD?**

QA is responsible for the test automation that runs in the pipeline. If tests are slow, flaky, or environment-dependent, they break the CI process for the whole team. QA must design tests to be headless, stateless, and configurable so they work identically in CI containers. QA also interprets pipeline results, identifies failures, and publishes reports that stakeholders consume.

**Q3: What is a Jenkinsfile and why is it better than freestyle jobs?**

A Jenkinsfile defines the entire pipeline as code, committed to the repository alongside the application code. It is version-controlled (you can see who changed the pipeline), reviewable in pull requests, reproducible (same Jenkinsfile gives same pipeline), and portable (works on any Jenkins with the required plugins). Freestyle jobs store configuration in Jenkins's XML database, which is hard to review, backup, or replicate.

**Q4: How do you run tests only on specific branches in GitLab CI?**

Using the `only` / `except` keywords or the modern `rules` keyword:

```yaml
# Modern approach with rules
regression:
  script: npx playwright test
  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
      when: manual
    - when: never
```

**Q5: What is a matrix strategy in GitHub Actions and when would you use it?**

A matrix strategy runs the same job with different parameter combinations. For QA, the most common use is running tests across multiple browsers. You define the matrix and GitHub creates one job per combination:

```yaml
strategy:
  matrix:
    browser: [chromium, firefox, webkit]
    os: [ubuntu-latest, windows-latest]
```

This creates 6 jobs. Use `fail-fast: false` so other combinations continue if one fails.

**Q6: How do you pass secrets (credentials) into a CI pipeline safely?**

Never hardcode credentials in YAML files or Jenkinsfiles. Store them in the CI platform's secret management:
- Jenkins: Credentials plugin → reference with `credentials('id')` in `environment {}` block
- GitLab: Settings → CI/CD → Variables (mask the value so it never appears in logs)
- GitHub: Settings → Secrets and variables → Actions → reference with `${{ secrets.NAME }}`

The values are injected as environment variables at runtime and masked in logs.

**Q7: What does `--no-sandbox` do and why is it needed in CI?**

Chrome's sandbox provides security isolation between processes. Inside a Docker container (which already provides isolation), the sandbox causes Chrome to fail because it cannot create the required kernel namespaces. Adding `--no-sandbox` disables the sandbox so Chrome can start inside containers. Always also add `--disable-dev-shm-usage` because Docker containers have a small `/dev/shm` by default, which Chrome uses for memory sharing.

**Q8: How do you handle a test suite that takes 45 minutes and blocks the CI pipeline?**

Several strategies:
1. **Parallel execution** — split by module or browser and run in parallel stages/jobs
2. **Test splitting** — divide the suite across multiple runners
3. **Tag-based filtering** — run only `@smoke` on every push; save regression for nightly
4. **Conditional execution** — run full regression only on `main`, not on feature branches
5. **Caching** — cache Maven/npm dependencies to reduce install time
6. **Containerise** — consistent environment removes setup overhead
7. **Fail-fast** — stop the suite as soon as smoke tests fail, don't run regression

---

*Guide complete — covers Jenkins, GitLab CI/CD, GitHub Actions, troubleshooting, and interview preparation.*
