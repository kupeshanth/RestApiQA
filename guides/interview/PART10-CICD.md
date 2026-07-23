# Part 10 — Jenkins & CI/CD | 40 Questions | Full Answers + Pipeline Code

> CV context: Kupeshanth — Qoria Lanka (GitHub Actions, GCP, Playwright), Cerexio (JIRA, Agile).
> Skills base: Java, Maven, TestNG, JMeter, Postman.

---

## Q1. What is CI/CD and why does QA care?

**Answer:**
**CI (Continuous Integration):** Every code commit triggers an automated build and test run. Developers get fast feedback (minutes, not days) when they break something.

**CD (Continuous Delivery):** Automatically deliver tested code to a staging environment, ready for deployment at the push of a button.

**CD (Continuous Deployment):** Every passing build automatically deploys to production without human intervention.

```
Developer commits → CI triggers → Build → Unit Tests → Integration Tests →
→ QA Automation Tests → Code Coverage → Security Scan → Deploy to Staging →
→ Smoke Tests on Staging → (manual approval) → Deploy to Production
```

**Why QA cares:**
1. Tests run automatically on every PR — no manual trigger needed
2. Failures are detected immediately at the source (specific commit)
3. Test results are visible to the whole team
4. Consistent environment — eliminates "works on my machine"
5. QA gates prevent broken code from reaching production

---

## Q2. What is Jenkins and what is its architecture?

**Answer:**
Jenkins is an open-source automation server written in Java. It orchestrates build, test, and deployment pipelines.

**Architecture:**
```
Jenkins Controller (Master)
├── Web UI (port 8080)
├── Job scheduler
├── Plugin manager
├── Build history
└── Agents (Workers)
    ├── Agent 1 (Linux — runs API tests)
    ├── Agent 2 (Windows — runs UI tests)
    └── Agent 3 (Mac — runs iOS tests)
```

**Key components:**
- **Controller (Master):** Manages the Jenkins UI, schedules jobs, stores configuration — does NOT run builds
- **Agent (Node/Worker):** Executes the actual build/test steps; can be local or remote (SSH, JNLP)
- **Executor:** A slot on an agent for running one job at a time; agents can have multiple executors
- **Workspace:** Directory on agent where job files are checked out and commands run
- **Plugin:** Extends Jenkins functionality (Git, Maven, Allure, Slack, etc.)

---

## Q3. Freestyle job vs Pipeline job — when to use each.

**Answer:**

| Feature | Freestyle Job | Pipeline Job |
|---|---|---|
| Configuration | GUI-based | Code-based (Jenkinsfile) |
| Version control | No | Yes (Jenkinsfile in repo) |
| Complex flows | Limited | Full conditional/parallel support |
| Reusability | Low | Shared Libraries |
| Visibility | Step-by-step GUI | Graphical stage view |
| Recommended | Simple, one-off tasks | All production pipelines |

**Freestyle:** Good for quick experiments — you click "Add build step" in the UI and pick Maven/Shell.

**Pipeline:** For everything real — your Jenkinsfile lives in the repo alongside your tests, changes are reviewed, history is traceable.

---

## Q4. Write a complete declarative Jenkinsfile for a QA pipeline.

**Answer:**
```groovy
// Jenkinsfile — place at root of repository
pipeline {
    agent {
        label 'linux-qa-agent'  // runs on agent with this label
    }
    
    // Environment variables available to all stages
    environment {
        APP_URL         = 'https://staging.myapp.com'
        ALLURE_RESULTS  = 'target/allure-results'
        REPORT_EMAIL    = 'qa-team@company.com'
        SLACK_CHANNEL   = '#qa-notifications'
    }
    
    // Build parameters (can be passed when triggering)
    parameters {
        choice(
            name: 'TEST_ENV',
            choices: ['staging', 'dev', 'prod'],
            description: 'Target environment'
        )
        string(
            name: 'BROWSER',
            defaultValue: 'chrome',
            description: 'Browser for UI tests'
        )
        booleanParam(
            name: 'RUN_REGRESSION',
            defaultValue: false,
            description: 'Run full regression suite'
        )
    }
    
    // Triggers
    triggers {
        // On every push to develop branch
        githubPush()
        // Nightly full regression at 2 AM
        cron('0 2 * * *')
    }
    
    stages {
        
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT}"
            }
        }
        
        stage('Install Dependencies') {
            steps {
                sh 'mvn dependency:resolve -q'
                sh 'node --version'
                sh 'npm ci'
            }
        }
        
        stage('Smoke Tests') {
            steps {
                sh """
                    mvn test \
                        -Dgroups=smoke \
                        -Denv=${params.TEST_ENV} \
                        -Dapp.url=${APP_URL} \
                        -Dmaven.test.failure.ignore=true
                """
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
                failure {
                    // Stop pipeline if smoke tests fail
                    error "Smoke tests failed — aborting pipeline"
                }
            }
        }
        
        stage('API Tests') {
            steps {
                sh """
                    mvn test \
                        -Dgroups=api \
                        -Denv=${params.TEST_ENV} \
                        -Dmaven.test.failure.ignore=true
                """
            }
        }
        
        stage('Regression Tests') {
            when {
                anyOf {
                    branch 'main'
                    expression { params.RUN_REGRESSION == true }
                    triggeredBy 'TimerTrigger'  // nightly cron
                }
            }
            steps {
                sh """
                    mvn test \
                        -Dgroups=regression \
                        -Denv=${params.TEST_ENV} \
                        -Dbrowser=${params.BROWSER} \
                        -Dmaven.test.failure.ignore=true
                """
            }
        }
        
        stage('Publish Allure Report') {
            steps {
                allure([
                    includeProperties: false,
                    jdk: '',
                    results: [[path: "${ALLURE_RESULTS}"]]
                ])
            }
        }
        
        stage('Publish HTML Report') {
            steps {
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/surefire-reports',
                    reportFiles: 'index.html',
                    reportName: 'Surefire Report'
                ])
            }
        }
    }
    
    post {
        always {
            // Archive test results regardless of outcome
            archiveArtifacts artifacts: 'target/surefire-reports/**', allowEmptyArchive: true
            archiveArtifacts artifacts: 'screenshots/**', allowEmptyArchive: true
        }
        
        failure {
            emailext(
                subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
                body: """
                    Build failed: ${env.BUILD_URL}
                    Branch: ${env.BRANCH_NAME}
                    Commit: ${env.GIT_COMMIT}
                    
                    See attached report for details.
                """,
                to: "${REPORT_EMAIL}",
                attachmentsPattern: 'target/surefire-reports/emailable-report.html'
            )
            
            slackSend(
                channel: "${SLACK_CHANNEL}",
                color: 'danger',
                message: "Build FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            )
        }
        
        success {
            slackSend(
                channel: "${SLACK_CHANNEL}",
                color: 'good',
                message: "Build PASSED: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            )
        }
        
        unstable {
            // Some tests failed but build completed
            slackSend(
                channel: "${SLACK_CHANNEL}",
                color: 'warning',
                message: "Build UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER} (<${env.BUILD_URL}|Open>)"
            )
        }
        
        cleanup {
            // Always clean workspace
            cleanWs()
        }
    }
}
```

---

## Q5. What are Jenkins stages and post actions?

**Answer:**
**Stages:** Logical groupings of steps that appear as columns in the Blue Ocean view.
- Each stage runs sequentially by default
- A failed stage stops subsequent stages (unless `maven.test.failure.ignore=true` or `when` conditions)

**Post actions:** Run after stages or the whole pipeline:
```groovy
post {
    always   { /* runs regardless of result */ }
    success  { /* only if all stages passed */ }
    failure  { /* only if pipeline failed */ }
    unstable { /* if test failures exist but build completed */ }
    changed  { /* if result differs from last build */ }
    cleanup  { /* very last — always runs */ }
}

// Stage-level post
stage('Tests') {
    steps { sh 'mvn test' }
    post {
        always { junit 'target/**/*.xml' }
        failure { archiveArtifacts 'screenshots/*.png' }
    }
}
```

---

## Q6. What are Jenkins triggers?

**Answer:**
```groovy
triggers {
    // GitHub/GitLab webhook (requires webhook plugin)
    githubPush()
    
    // Cron schedule (standard cron syntax)
    cron('H 2 * * 1-5')     // Mon-Fri at ~2 AM (H = hash for load spreading)
    cron('0 0 * * 7')        // Every Sunday midnight
    
    // Poll SCM (check for changes on interval)
    pollSCM('H/5 * * * *')  // Every 5 minutes — check git for changes
    
    // Run when upstream job completes
    upstream(upstreamProjects: 'build-job', threshold: hudson.model.Result.SUCCESS)
}

// Conditional trigger in Jenkinsfile
stage('Deploy') {
    when {
        branch 'main'
        triggeredBy 'TimerTrigger'
    }
    steps { sh './deploy.sh' }
}
```

---

## Q7. How do you use environment variables in Jenkins?

**Answer:**
```groovy
// Built-in variables
env.BUILD_NUMBER     // "42"
env.BUILD_URL        // "http://jenkins/job/my-job/42/"
env.JOB_NAME         // "my-pipeline"
env.BRANCH_NAME      // "feature/login"
env.GIT_COMMIT       // "abc1234def5678"
env.WORKSPACE        // "/var/jenkins/workspace/my-job"

// Define custom env vars
environment {
    APP_URL = 'https://staging.myapp.com'
    TIMEOUT = '30'
    
    // From Jenkins Credentials plugin (masked in logs)
    DB_PASSWORD = credentials('db-password-credential-id')
    // Creates: DB_PASSWORD, DB_PASSWORD_USR, DB_PASSWORD_PSW for username:password type
}

// Use in steps
steps {
    sh """
        echo "Testing against ${APP_URL}"
        mvn test -Denv.url=${APP_URL} -Dtimeout=${TIMEOUT}
    """
}

// Override at runtime
sh "APP_URL=https://prod.myapp.com mvn test -Denv=prod"
```

---

## Q8. How do you manage credentials in Jenkins?

**Answer:**
Never hardcode passwords in Jenkinsfiles. Use the Credentials Plugin.

**Setup (in Jenkins UI):**
1. Dashboard → Manage Jenkins → Credentials → System → Global
2. Add Credentials → Kind: Username/Password, Secret Text, SSH Key, etc.
3. Give it an ID: `browserstack-credentials`

**Use in Jenkinsfile:**
```groovy
// Secret text (API key, token)
environment {
    BS_ACCESS_KEY = credentials('browserstack-access-key')
}

steps {
    sh "mvn test -Dbs.key=${BS_ACCESS_KEY}"
}

// Username/Password
withCredentials([usernamePassword(
    credentialsId: 'db-credentials',
    usernameVariable: 'DB_USER',
    passwordVariable: 'DB_PASS'
)]) {
    sh "psql -U ${DB_USER} -W ${DB_PASS} -d mydb -c 'SELECT COUNT(*) FROM users'"
}

// SSH key
withCredentials([sshUserPrivateKey(
    credentialsId: 'deploy-ssh-key',
    keyFileVariable: 'SSH_KEY'
)]) {
    sh "ssh -i ${SSH_KEY} deploy@prod.server.com './restart.sh'"
}
```

---

## Q9. What is Blue Ocean in Jenkins?

**Answer:**
Blue Ocean is the modern Jenkins UI plugin that provides:
- Visual pipeline view — stages as swimlane columns
- Color-coded status: green (pass), red (fail), yellow (unstable), grey (skipped)
- Branch and PR visualization
- Test result drill-down

```
Pipeline: my-test-pipeline
┌──────────┬──────────┬────────────┬────────────┬─────────┐
│ Checkout │ Install  │   Smoke    │    API     │ Reports │
│   ✓ 5s  │  ✓ 45s  │   ✗ 2m3s  │  skipped  │ skipped │
└──────────┴──────────┴────────────┴────────────┴─────────┘
                       ↑ Click here to see which test failed
```

Install: Manage Jenkins → Manage Plugins → Available → Blue Ocean

---

## Q10. How do you run stages in parallel in Jenkins?

**Answer:**
```groovy
stage('Parallel Test Execution') {
    parallel {
        stage('Chrome Tests') {
            agent { label 'linux-agent' }
            steps {
                sh 'mvn test -Dbrowser=chrome -Dgroups=regression'
            }
        }
        stage('Firefox Tests') {
            agent { label 'linux-agent' }
            steps {
                sh 'mvn test -Dbrowser=firefox -Dgroups=regression'
            }
        }
        stage('API Tests') {
            agent { label 'linux-agent' }
            steps {
                sh 'mvn test -Dgroups=api'
            }
        }
    }
}

// Parallel with failFast — stop all if one fails
stage('Multi-browser') {
    failFast true
    parallel {
        stage('Chrome')  { steps { sh 'mvn test -Dbrowser=chrome'  } }
        stage('Firefox') { steps { sh 'mvn test -Dbrowser=firefox' } }
        stage('Edge')    { steps { sh 'mvn test -Dbrowser=edge'    } }
    }
}
```

---

## Q11. Full GitLab CI/CD .gitlab-ci.yml example for QA.

**Answer:**
```yaml
# .gitlab-ci.yml — place at root of repository

# Global image
image: maven:3.9.4-eclipse-temurin-17

# Stages define execution order
stages:
  - build
  - test:smoke
  - test:api
  - test:regression
  - report
  - deploy

# Cache Maven dependencies between jobs
cache:
  key: "${CI_COMMIT_REF_SLUG}"
  paths:
    - .m2/repository/

# Global variables
variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  APP_URL: "https://staging.${CI_PROJECT_NAME}.com"

# Build
compile:
  stage: build
  script:
    - mvn compile -q
  artifacts:
    paths:
      - target/classes/
    expire_in: 1 hour

# Smoke tests — run on every push
smoke-tests:
  stage: test:smoke
  script:
    - mvn test -Dgroups=smoke -Denv.url=$APP_URL -Dmaven.test.failure.ignore=true
  artifacts:
    when: always
    paths:
      - target/surefire-reports/
      - target/allure-results/
    reports:
      junit: target/surefire-reports/TEST-*.xml
    expire_in: 7 days
  rules:
    - if: '$CI_PIPELINE_SOURCE == "push"'
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'

# API tests
api-tests:
  stage: test:api
  image: maven:3.9.4-eclipse-temurin-17
  services:
    - postgres:15  # spin up test DB
  variables:
    POSTGRES_DB: testdb
    POSTGRES_USER: testuser
    POSTGRES_PASSWORD: testpass
    DB_URL: jdbc:postgresql://postgres:5432/testdb
  script:
    - mvn test -Dgroups=api -Denv=staging
  artifacts:
    when: always
    reports:
      junit: target/surefire-reports/TEST-*.xml
    paths:
      - target/allure-results/
    expire_in: 7 days
  needs: ["smoke-tests"]  # only run if smoke passed

# Regression — only on main branch or nightly schedule
regression-tests:
  stage: test:regression
  script:
    - mvn test -Dgroups=regression -Denv=staging -Dbrowser=chrome
  artifacts:
    when: always
    paths:
      - target/allure-results/
      - screenshots/
    reports:
      junit: target/surefire-reports/TEST-*.xml
    expire_in: 30 days
  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'
    - if: '$CI_PIPELINE_SOURCE == "schedule"'  # nightly
  allow_failure: false

# Playwright tests
playwright-tests:
  stage: test:regression
  image: mcr.microsoft.com/playwright:v1.44.0-jammy
  script:
    - npm ci
    - npx playwright test --reporter=allure-playwright
  artifacts:
    when: always
    paths:
      - playwright-report/
      - test-results/
    expire_in: 7 days
  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'

# Generate Allure report
generate-report:
  stage: report
  image: frankescobar/allure-docker-service
  script:
    - allure generate target/allure-results -o allure-report --clean
  artifacts:
    paths:
      - allure-report/
    expire_in: 30 days
  needs: ["regression-tests", "api-tests"]

# Deploy to staging
deploy-staging:
  stage: deploy
  image: google/cloud-sdk:alpine
  script:
    - gcloud auth activate-service-account --key-file=$GCP_SERVICE_KEY
    - gcloud app deploy --project=$GCP_PROJECT_ID --quiet
  environment:
    name: staging
    url: https://staging.myapp.com
  rules:
    - if: '$CI_COMMIT_BRANCH == "develop"'
      when: on_success
```

---

## Q12. Full GitHub Actions workflow for QA with matrix strategy.

**Answer:**
```yaml
# .github/workflows/qa-tests.yml

name: QA Test Pipeline

on:
  push:
    branches: [main, develop, 'release/**']
  pull_request:
    branches: [main, develop]
  schedule:
    - cron: '0 1 * * *'  # 1 AM UTC daily
  workflow_dispatch:       # Manual trigger
    inputs:
      environment:
        description: 'Target environment'
        required: true
        default: 'staging'
        type: choice
        options: [staging, dev, prod]
      run_regression:
        description: 'Run regression suite'
        type: boolean
        default: false

jobs:
  # ─── Smoke Tests ────────────────────────────────────────
  smoke-tests:
    name: Smoke Tests
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Run smoke tests
        run: mvn test -Dgroups=smoke -Denv=${{ inputs.environment || 'staging' }}
        env:
          APP_URL: ${{ secrets.STAGING_APP_URL }}
      
      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: smoke-results
          path: |
            target/surefire-reports/
            target/allure-results/
          retention-days: 7
      
      - name: Publish JUnit results
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: Smoke Test Results
          path: target/surefire-reports/TEST-*.xml
          reporter: java-junit

  # ─── Multi-browser Tests (Matrix Strategy) ──────────────
  ui-tests:
    name: UI Tests (${{ matrix.browser }} / ${{ matrix.os }})
    needs: smoke-tests
    runs-on: ${{ matrix.os }}
    
    strategy:
      fail-fast: false  # continue other matrix jobs even if one fails
      matrix:
        browser: [chrome, firefox, edge]
        os: [ubuntu-latest, windows-latest]
        exclude:
          - os: windows-latest
            browser: firefox  # Firefox flaky on Windows in CI
        include:
          - os: macos-latest
            browser: safari   # Safari only on macOS
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Run UI tests
        run: |
          mvn test \
            -Dbrowser=${{ matrix.browser }} \
            -Dgroups=regression \
            -Denv=staging \
            -Dmaven.test.failure.ignore=true
        env:
          APP_URL: ${{ secrets.STAGING_APP_URL }}
          DB_URL: ${{ secrets.DB_URL }}
      
      - name: Upload screenshots on failure
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: screenshots-${{ matrix.browser }}-${{ matrix.os }}
          path: screenshots/
          retention-days: 14
      
      - name: Upload Allure results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: allure-results-${{ matrix.browser }}
          path: target/allure-results/
          retention-days: 7

  # ─── Playwright Tests ───────────────────────────────────
  playwright-tests:
    name: Playwright Tests
    needs: smoke-tests
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Install Playwright browsers
        run: npx playwright install --with-deps chromium firefox
      
      - name: Run Playwright tests
        run: npx playwright test --reporter=html,allure-playwright
        env:
          BASE_URL: ${{ secrets.STAGING_APP_URL }}
          API_KEY: ${{ secrets.API_KEY }}
      
      - name: Upload Playwright report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30

  # ─── API Tests ──────────────────────────────────────────
  api-tests:
    name: API Tests
    needs: smoke-tests
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: testuser
          POSTGRES_PASSWORD: testpass
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'
      
      - name: Run API tests
        run: mvn test -Dgroups=api -Denv=staging
        env:
          DB_URL: jdbc:postgresql://localhost:5432/testdb
          DB_USER: testuser
          DB_PASS: testpass
          API_BASE_URL: ${{ secrets.STAGING_API_URL }}
          API_KEY: ${{ secrets.API_KEY }}
      
      - name: Upload results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: api-test-results
          path: target/surefire-reports/
          retention-days: 7

  # ─── Publish Allure Report ──────────────────────────────
  publish-report:
    name: Publish Allure Report
    needs: [ui-tests, api-tests, playwright-tests]
    runs-on: ubuntu-latest
    if: always()
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Download all Allure results
        uses: actions/download-artifact@v4
        with:
          pattern: allure-results-*
          merge-multiple: true
          path: allure-results
      
      - name: Generate Allure Report
        uses: simple-elf/allure-report-action@master
        with:
          allure_results: allure-results
          gh_pages: gh-pages
          allure_report: allure-report
          allure_history: allure-history
      
      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_branch: gh-pages
          publish_dir: allure-history
```

---

## Q13. How do you make tests CI-ready? Headless and no hardcoded paths.

**Answer:**
Tests that pass locally but fail in CI are a common pain point. Follow this checklist:

**1. Run browsers in headless mode:**
```java
// ChromeOptions
ChromeOptions options = new ChromeOptions();
options.addArguments("--headless=new");
options.addArguments("--no-sandbox");           // required in Docker/CI
options.addArguments("--disable-dev-shm-usage"); // prevents crashes in containers
options.addArguments("--window-size=1920,1080");
options.addArguments("--disable-gpu");

// Detect CI and apply headless automatically
boolean isCI = Boolean.parseBoolean(System.getenv("CI"));
if (isCI) {
    options.addArguments("--headless=new");
}
```

**2. No hardcoded file paths:**
```java
// Bad
String path = "C:\\Users\\Kupeshanth\\tests\\data\\users.json";

// Good — relative to project root
String path = System.getProperty("user.dir") + "/src/test/resources/data/users.json";

// Better — use ClassLoader
InputStream is = getClass().getClassLoader().getResourceAsStream("data/users.json");

// Best — use config file + environment variable
String baseDir = System.getProperty("test.data.dir", 
    System.getProperty("user.dir") + "/src/test/resources");
```

**3. No hardcoded URLs:**
```java
// Read from system property or environment variable
String appUrl = System.getProperty("app.url", 
    System.getenv().getOrDefault("APP_URL", "http://localhost:8080"));
```

**4. No hardcoded credentials:**
```java
// Read from environment variables (set in CI secrets)
String username = System.getenv("TEST_USERNAME");
String password = System.getenv("TEST_PASSWORD");
```

**5. Handle file separators:**
```java
// Use File.separator or Paths.get() instead of hardcoded /
Path screenshotPath = Paths.get("screenshots", "login_test.png");
```

---

## Q14. How do you integrate JMeter into a Jenkins pipeline?

**Answer:**
```groovy
// Jenkinsfile — JMeter performance stage
stage('Performance Tests') {
    steps {
        // Run JMeter in non-GUI mode
        sh """
            ${JMETER_HOME}/bin/jmeter.sh \
                -n \
                -t src/test/jmeter/api_load_test.jmx \
                -l target/jmeter/results/results.jtl \
                -e \
                -o target/jmeter/reports \
                -Jbase_url=${APP_URL} \
                -Jthreads=50 \
                -Jrampup=60 \
                -Jduration=300
        """
    }
    post {
        always {
            // Publish JMeter report (requires Performance Plugin)
            perfReport sourceDataFiles: 'target/jmeter/results/results.jtl'
            
            // Archive HTML dashboard
            publishHTML([
                reportDir: 'target/jmeter/reports',
                reportFiles: 'index.html',
                reportName: 'JMeter Performance Report'
            ])
        }
        failure {
            echo "Performance tests exceeded threshold — check JMeter results"
        }
    }
}
```

**Maven integration (pom.xml):**
```xml
<plugin>
    <groupId>com.lazerycode.jmeter</groupId>
    <artifactId>jmeter-maven-plugin</artifactId>
    <version>3.7.0</version>
    <configuration>
        <testResultsTimestamp>false</testResultsTimestamp>
        <propertiesUser>
            <threads>50</threads>
            <rampup>60</rampup>
            <duration>300</duration>
        </propertiesUser>
        <errorRateThresholdInPercent>5</errorRateThresholdInPercent>
    </configuration>
</plugin>
```

---

## Q15. How do you run Playwright tests in GitHub Actions?

**Answer:**
```yaml
# .github/workflows/playwright.yml
name: Playwright Tests

on:
  push:
    branches: [main, develop]
  pull_request:

jobs:
  playwright:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Install Playwright browsers
        run: npx playwright install --with-deps
        # --with-deps installs system dependencies (libgbm, etc.)
      
      - name: Run all Playwright tests
        run: npx playwright test
        env:
          BASE_URL: ${{ secrets.BASE_URL }}
          # CI=true is automatically set by GitHub Actions
          # Playwright detects CI and uses reasonable defaults
      
      - name: Run only API tests
        run: npx playwright test --grep @api
      
      - name: Upload Playwright HTML Report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30
      
      - name: Upload failed test screenshots
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: test-results
          path: test-results/
          retention-days: 14
```

**playwright.config.ts for CI:**
```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30 * 1000,
  retries: process.env.CI ? 2 : 0,      // retry twice in CI
  workers: process.env.CI ? 4 : undefined, // 4 workers in CI
  reporter: [
    ['html', { open: 'never' }],          // don't auto-open in CI
    ['junit', { outputFile: 'results/junit.xml' }],
    ['allure-playwright'],
  ],
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    headless: true,
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'firefox',  use: { ...devices['Desktop Firefox'] } },
    { name: 'webkit',   use: { ...devices['Desktop Safari'] } },
  ],
});
```

---

## Q16 - Q25: Additional CI/CD Questions

### Q16. What is environment-specific testing in CI/CD?
```groovy
// Jenkinsfile — promote through environments
stage('Test on Dev') {
    environment { APP_URL = 'https://dev.myapp.com' }
    steps { sh 'mvn test -Dgroups=smoke -Denv=dev' }
}

stage('Deploy to Staging') {
    when { branch 'release/*' }
    steps { sh './scripts/deploy.sh staging' }
}

stage('Test on Staging') {
    when { branch 'release/*' }
    environment { APP_URL = 'https://staging.myapp.com' }
    steps { sh 'mvn test -Dgroups=regression -Denv=staging' }
}

stage('Deploy to Prod') {
    when {
        branch 'main'
        expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
    }
    input { message "Deploy to Production?" }
    steps { sh './scripts/deploy.sh prod' }
}
```

### Q17. Troubleshooting: tests pass locally but fail in CI.
Common causes and fixes:

1. **Hardcoded paths** → Use `System.getProperty("user.dir")` or classpath resources
2. **Not headless** → Add `--headless=new` when `System.getenv("CI") != null`
3. **Timing issues** → CI machines are slower; increase timeouts for CI
4. **Missing environment variables** → Add secrets in CI settings
5. **Port conflicts** → CI runs multiple jobs; use dynamic port allocation
6. **No display** → Install `xvfb` or run headless
7. **Different timezone** → Use UTC in both local and CI
8. **Different OS** → Path separators, line endings

```groovy
// Debug: print environment in CI
steps {
    sh 'printenv | sort'
    sh 'java -version'
    sh 'mvn --version'
    sh 'google-chrome --version || true'
}
```

### Q18. How do you integrate Allure Report into Jenkins pipeline?
```groovy
// pom.xml dependency
// <dependency>groupId: io.qameta.allure, artifactId: allure-testng</dependency>
// <argLine>-javaagent:${settings.localRepository}/org/aspectj/aspectjweaver/VERSION/aspectjweaver-VERSION.jar</argLine>

// Jenkinsfile
stage('Tests') {
    steps {
        sh 'mvn test -Dallure.results.directory=target/allure-results'
    }
    post {
        always {
            allure([
                includeProperties: false,
                jdk: '',
                results: [[path: 'target/allure-results']]
            ])
        }
    }
}

// Jenkins plugin required: Allure Jenkins Plugin
// Manage Jenkins → Manage Plugins → Available → Allure Jenkins Plugin
// Manage Jenkins → Global Tool Configuration → Allure → Add Allure
```

### Q19. What is a Jenkins Shared Library?
```groovy
// vars/runTests.groovy (in shared library repo)
def call(Map config = [:]) {
    def browser   = config.browser   ?: 'chrome'
    def groups    = config.groups    ?: 'smoke'
    def env       = config.env       ?: 'staging'
    
    sh """
        mvn test \
            -Dbrowser=${browser} \
            -Dgroups=${groups} \
            -Denv=${env} \
            -Dmaven.test.failure.ignore=true
    """
}

// Jenkinsfile (in app repo) — uses shared library
@Library('qa-shared-library') _

pipeline {
    agent any
    stages {
        stage('Smoke') {
            steps {
                runTests(browser: 'chrome', groups: 'smoke', env: 'staging')
            }
        }
        stage('Regression') {
            steps {
                runTests(browser: 'firefox', groups: 'regression', env: 'staging')
            }
        }
    }
}
```

### Q20. How do you handle flaky tests in CI?
```groovy
// Retry failed tests automatically
stage('Regression') {
    steps {
        retry(2) {
            sh 'mvn test -Dgroups=regression'
        }
    }
}

// TestNG retry analyzer
// RetryAnalyzer.java
public class RetryAnalyzer implements IRetryAnalyzer {
    private int count = 0;
    private static final int MAX_RETRY = 2;
    
    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess() && count < MAX_RETRY) {
            count++;
            return true;
        }
        return false;
    }
}

// Apply to test
@Test(retryAnalyzer = RetryAnalyzer.class)
public void flakyTest() { ... }

// Playwright retry in config
// retries: process.env.CI ? 2 : 0
```

### Q21. What GitHub Actions triggers are most useful for QA?
```yaml
on:
  push:
    branches: [main, develop]
    paths:
      - 'src/**'           # only trigger if source changed
      - 'pom.xml'
      - '!docs/**'         # don't trigger for doc changes
  
  pull_request:
    types: [opened, synchronize, reopened]
    branches: [main]
  
  schedule:
    - cron: '0 0 * * *'   # nightly at midnight UTC
  
  workflow_dispatch:
    inputs:
      suite:
        type: choice
        options: [smoke, regression, full]
  
  workflow_call:            # can be called from another workflow
    inputs:
      environment:
        required: true
        type: string
```

### Q22. How do you cache dependencies in GitHub Actions?
```yaml
# Maven
- uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: 'maven'   # handles ~/.m2 caching automatically

# npm
- uses: actions/setup-node@v4
  with:
    node-version: '20'
    cache: 'npm'     # handles node_modules caching

# Manual cache (for Playwright browsers)
- name: Cache Playwright browsers
  uses: actions/cache@v4
  with:
    path: ~/.cache/ms-playwright
    key: playwright-${{ hashFiles('package-lock.json') }}
    restore-keys: playwright-
```

### Q23. What are GitHub Actions secrets and how do you use them?
```yaml
# Setting: Repository → Settings → Secrets and variables → Actions → New secret
# Name: STAGING_API_KEY, Value: abc123

# Use in workflow
env:
  API_KEY: ${{ secrets.STAGING_API_KEY }}
  DB_URL: ${{ secrets.DB_URL }}

# In test code
String apiKey = System.getenv("API_KEY");

# Organization secrets: shared across repos
# Environment secrets: different values per environment (staging, prod)
```

### Q24. How do you deploy a test report to GitHub Pages?
```yaml
- name: Generate Allure Report
  uses: simple-elf/allure-report-action@master
  with:
    allure_results: target/allure-results

- name: Deploy report to GitHub Pages
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: allure-report
    destination_dir: test-reports/${{ github.run_number }}

# Report URL: https://username.github.io/repo/test-reports/42/
```

### Q25. How do you send test notifications from CI?
```yaml
# Slack notification via webhook
- name: Notify Slack on failure
  if: failure()
  uses: slackapi/slack-github-action@v1
  with:
    payload: |
      {
        "text": "Tests FAILED on ${{ github.ref_name }}",
        "attachments": [{
          "color": "danger",
          "fields": [
            {"title": "Repository", "value": "${{ github.repository }}"},
            {"title": "Run", "value": "${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}"}
          ]
        }]
      }
  env:
    SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

---

## Q26 - Q35: Advanced Topics

### Q26. What is a QA CI/CD readiness checklist?
```
Pre-CI readiness:
□ Tests run with mvn test -headless=true (no display needed)
□ No hardcoded file paths (use classpath or env vars)
□ No hardcoded credentials (use environment variables)
□ No hardcoded URLs (use config or system properties)
□ Tests are independent (can run in any order)
□ Tests clean up after themselves
□ Database state doesn't depend on other tests
□ Wait strategies use explicit waits, not Thread.sleep()
□ Consistent timeouts (configurable via system properties)
□ Test data is isolated (uses unique IDs/emails)

CI pipeline requirements:
□ JUnit/TestNG XML reports are generated
□ Failed tests produce screenshots
□ Log level is appropriate (not too verbose)
□ Tests exit with non-zero code on failure
□ Test duration is acceptable (< 10 min for smoke)
□ No interactive prompts in test execution
□ Browser runs in headless mode
□ No GUI dependencies
```

### Q27. How do you version control your test configuration?
```
# config/
# ├── staging.properties
# ├── dev.properties
# └── prod.properties

# staging.properties
app.url=https://staging.myapp.com
db.url=jdbc:postgresql://staging-db:5432/myapp
api.timeout=30
browser=chrome

# Load in Java
Properties props = new Properties();
String env = System.getProperty("env", "staging");
props.load(getClass().getClassLoader()
    .getResourceAsStream("config/" + env + ".properties"));

String appUrl = props.getProperty("app.url");
```

### Q28. What is the difference between `push` and `pull_request` triggers in GitHub Actions?
```yaml
# push trigger — runs when code is pushed directly to branch
on:
  push:
    branches: [main]

# pull_request trigger — runs when PR is opened/updated
on:
  pull_request:
    branches: [main]
    types: [opened, synchronize]

# QA strategy:
# PR trigger: run smoke + API tests (fast feedback to developer)
# Push to main: run full regression (after merge)
# Schedule: run performance tests nightly
```

### Q29. How do you implement test tagging for selective CI execution?
```java
// TestNG
@Test(groups = {"smoke", "login", "critical"})
public void testLogin() { }

@Test(groups = {"regression", "payment"})
public void testPayment() { }

// Run only smoke in PR, full in main
// mvn test -Dgroups=smoke   (PR pipeline)
// mvn test -Dgroups=regression  (nightly)

// Playwright
test.describe('@smoke', () => {
    test('login works', async ({ page }) => { ... });
});

// npx playwright test --grep @smoke
```

### Q30. How do you handle test data in CI/CD?
```groovy
// Strategy 1: Reset test data before run
stage('Setup Test Data') {
    steps {
        sh '''
            psql $DB_URL -c "TRUNCATE TABLE test_users CASCADE;"
            psql $DB_URL -f src/test/resources/sql/seed_data.sql
        '''
    }
}

// Strategy 2: Use transactions (rollback after each test)
// Strategy 3: Unique identifiers per run
String runId = System.getenv("GITHUB_RUN_ID") ?: UUID.randomUUID().toString();
String testEmail = "qa_" + runId + "@test.com";

// Strategy 4: Dedicated test environment with known state
// Restore from snapshot before run
```

### Q31. What is artifact management in CI/CD?
```groovy
// Archive in Jenkins
archiveArtifacts artifacts: 'target/**/*.jar', fingerprint: true
archiveArtifacts artifacts: 'target/surefire-reports/**', allowEmptyArchive: true

// GitHub Actions
- uses: actions/upload-artifact@v4
  with:
    name: test-results-${{ github.run_number }}
    path: |
      target/surefire-reports/
      target/allure-results/
      screenshots/
    retention-days: 30
    if-no-files-found: warn
```

### Q32. What is a quality gate in CI/CD?
```groovy
// Example: fail the pipeline if > 5% tests fail
stage('Quality Gate') {
    steps {
        script {
            def results = junit 'target/surefire-reports/*.xml'
            def failRate = results.failCount / results.totalCount * 100
            
            if (failRate > 5) {
                error "Quality gate failed: ${failRate}% failure rate (threshold: 5%)"
            }
            
            if (results.totalCount < 100) {
                error "Quality gate failed: only ${results.totalCount} tests ran (expected 100+)"
            }
        }
    }
}
```

### Q33. How does your experience at Qoria Lanka with GitHub Actions help in interviews?
**Model Answer:**
"At Qoria Lanka, I work directly with GitHub Actions for our CI/CD pipeline. Our workflow triggers on every PR to validate that no tests break before merge. I maintain the `.github/workflows/` YAML files — configuring multi-browser matrix runs with Playwright, uploading artifacts for failed screenshots, and setting up job dependencies so regression only runs after smoke passes. I also manage secrets for staging environment credentials and API keys. When the pipeline breaks, I diagnose whether it's a flaky test, a dependency issue, or an actual regression by checking the GitHub Actions logs and comparing against the last passing run."

### Q34. How do you parallelize TestNG tests in Maven?
```xml
<!-- pom.xml surefire config -->
<configuration>
    <parallel>classes</parallel>       <!-- methods, classes, tests, all -->
    <threadCount>4</threadCount>
    <suiteXmlFiles>
        <suiteXmlFile>testng.xml</suiteXmlFile>
    </suiteXmlFiles>
</configuration>
```

```xml
<!-- testng.xml -->
<suite name="Suite" parallel="classes" thread-count="4">
    <test name="Regression">
        <classes>
            <class name="tests.LoginTest"/>
            <class name="tests.CheckoutTest"/>
            <class name="tests.SearchTest"/>
            <class name="tests.ProfileTest"/>
        </classes>
    </test>
</suite>
```

### Q35. How do you set up a multi-stage Docker-based test environment?
```yaml
# docker-compose.yml for local CI simulation
version: '3.8'
services:
  
  app:
    image: myapp:latest
    ports: ["8080:8080"]
    environment:
      DB_URL: jdbc:postgresql://db:5432/myapp
    depends_on: [db]
  
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: myapp
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: secret
    ports: ["5432:5432"]
  
  test-runner:
    build: .
    command: mvn test -Denv=local -Dapp.url=http://app:8080
    depends_on: [app, db]
    volumes:
      - ./target:/app/target  # mount for report access
```

---

## Q36 - Q40: Final Interview Questions

### Q36. Describe your full QA pipeline at Qoria Lanka.
**Model Answer:**
"We use GitHub Actions with a three-stage pipeline: On every PR, a smoke suite of about 30 tests runs in roughly 3 minutes — this gives developers fast feedback. When a PR merges to develop, our full API regression suite kicks off using RestAssured and a separate Playwright UI suite run in parallel across Chrome and Firefox. Screenshots of failures are uploaded as artifacts automatically. Nightly at 1 AM, the performance tests using JMeter run against staging. The full pipeline takes about 25 minutes. I maintain the YAML files, update selectors when the UI changes, and investigate failures during standup."

### Q37. What is the difference between Jenkins declarative and scripted pipeline?
```groovy
// Declarative (recommended — strict structure, easier to read)
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
    }
}

// Scripted (flexible — full Groovy)
node {
    stage('Test') {
        sh 'mvn test'
    }
    
    // Can use any Groovy
    def tests = ['smoke', 'regression', 'api']
    tests.each { suite ->
        sh "mvn test -Dgroups=${suite}"
    }
}
```

### Q38. How do you handle browser binary paths in CI?
```java
// Don't hardcode browser paths
// Bad:
System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe");

// Good: WebDriverManager
import io.github.bonigarcia.wdm.WebDriverManager;

WebDriverManager.chromedriver().setup();
WebDriver driver = new ChromeDriver(options);

// In pom.xml
// <dependency>io.github.bonigarcia:webdrivermanager:5.6.3</dependency>

// For Selenium 4.6+ — no WebDriverManager needed at all
// Selenium Manager handles this automatically
WebDriver driver = new ChromeDriver(); // just works in CI
```

### Q39. How do you measure test coverage in CI?
```xml
<!-- pom.xml — JaCoCo for code coverage -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>  <!-- 80% coverage gate -->
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### Q40. Final interview question: Walk me through how you would set up CI/CD for a new project.

**Model Answer:**
"I'd start by creating a `.github/workflows/` directory and a main `qa-pipeline.yml`. First, I'd define triggers: `push` to main and develop, `pull_request` to main, and a nightly schedule. The pipeline would have four jobs running in order: smoke tests first as a fast gate (3-5 min), then API tests and UI tests in parallel (15-20 min), then a report generation job.

I'd use `actions/setup-java` with Maven cache to speed up builds. All environment URLs, credentials, and API keys go into GitHub Secrets — never in the YAML. Tests are configured to run headless via system property checks. On failure, screenshots and logs are uploaded as artifacts with 14-day retention. Allure results from all jobs are merged and deployed to GitHub Pages.

For the Jenkinsfile equivalent, I'd use declarative pipeline syntax with a separate agent per parallel branch, credentials plugin for secrets, emailext for failure notifications, and HTML Publisher for reports. The key principle is: the pipeline is code, it lives in the repo, and changes to it go through the same PR review process as the application code."

---

*End of Part 10 — Jenkins & CI/CD*
*Next: Part 11 — Agile & JIRA*
