# CI/CD — Jenkins + GitLab + GitHub Actions | Complete Interview Q&A Guide

---

## SECTION 1: CI/CD FUNDAMENTALS

---

**Q1: What is CI/CD? What is the difference between Continuous Integration, Continuous Delivery, and Continuous Deployment?**

**A:**

CI/CD is a set of practices and tools that automate the building, testing, and releasing of software so that teams can deliver changes frequently, safely, and reliably.

**Continuous Integration (CI)** is the practice of merging developer code changes into a shared repository multiple times per day. Each merge automatically triggers a build and automated test run so integration problems are caught within minutes rather than days.

**Continuous Delivery (CD)** extends CI. Every build that passes all tests is automatically packaged and prepared so it *could* be deployed to production at any time. The actual deployment to production still requires a manual approval step.

**Continuous Deployment** goes one step further: every passing build is automatically deployed to production with no human gate. This requires extremely high test confidence and a mature monitoring setup.

```
Developer pushes code
        |
        v
Source Control triggers pipeline automatically
        |
        v
Build stage — compile, package
        |
        v
Unit tests run (fast feedback)
        |
        v
Integration / API tests run
        |
        v
UI / E2E tests run (smoke or regression)
        |
        v
Reports published, artifacts stored
        |
        v  <-- Manual gate = Continuous DELIVERY
Deploy to staging or production
        |
        v  <-- No gate = Continuous DEPLOYMENT
Notifications sent (Slack, email, Teams)
```

**Real-world context:** Most teams practise Continuous Delivery rather than Continuous Deployment. A human approves the production push even when builds are fully green, because business timing, feature flags, or change-management processes require it.

---

**Q2: Why does QA care about CI/CD? What is QA's role in a CI/CD pipeline?**

**A:**

QA owns the automated test suite that runs inside the pipeline. If those tests are slow, flaky, or environment-dependent, the entire team's pipeline becomes unreliable and developers stop trusting it.

**QA responsibilities in CI/CD:**

| Responsibility | Why It Matters |
|---|---|
| Write CI-ready tests (headless, stateless) | Tests must run unattended in containers |
| Maintain fast, stable test suites | Flaky tests break pipelines and erode trust |
| Define pipeline stages and gates | QA decides which tests block a deployment |
| Interpret pipeline results | QA translates failures into actionable defect reports |
| Publish readable reports | Stakeholders consume test results from the pipeline |
| Tune timeouts and retries | CI machines are slower — tests need adjusted thresholds |

**Key insight:** CI/CD forces good test design discipline. If your test requires a headed browser, a local file path, or a manually configured environment, it will fail in CI. The pipeline is the quality gate — QA builds and maintains it.

---

**Q3: What is Jenkins? Describe its architecture.**

**A:**

Jenkins is an open-source automation server written in Java that orchestrates CI/CD pipelines. It was originally forked from Hudson in 2011 and is the most widely deployed CI tool in enterprise environments.

**Architectural components:**

```
Jenkins Controller (Master)
    |
    |-- Job definitions and configuration (XML on disk)
    |-- Plugin management (1800+ plugins available)
    |-- Security model and user management
    |-- Credentials store (encrypted at rest)
    |-- Build queue and scheduling engine
    |-- Web UI (port 8080 by default)
    |
    +-- Distributes work to Agents (Nodes)
            |
            +-- Agent 1: Linux  (Selenium, Playwright, Maven)
            +-- Agent 2: Windows (Edge, IE legacy tests)
            +-- Agent 3: macOS   (Safari / WebKit tests)
            +-- Agent 4: Docker  (ephemeral containers)
```

**Key terms:**
- **Controller/Master:** Orchestrates everything; does NOT run tests itself in production setups
- **Agent/Node:** A machine that actually executes jobs. Agents connect to the Controller via SSH or JNLP
- **Executor:** A slot on an agent. An agent with 2 executors can run 2 jobs simultaneously
- **Workspace:** A directory on the agent where the job's files live during a build
- **Item/Job:** A configured unit of work (freestyle job, pipeline, multibranch pipeline)

**Installing Jenkins on Ubuntu:**

```bash
# Step 1: Install Java 17 (Jenkins requirement)
sudo apt update
sudo apt install openjdk-17-jdk -y

# Step 2: Add Jenkins repository
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | \
  sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null

echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian-stable binary/ | \
  sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null

# Step 3: Install Jenkins
sudo apt update
sudo apt install jenkins -y

# Step 4: Start and enable service
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Step 5: Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
# Navigate to http://localhost:8080 and paste this password
```

**Essential plugins to install after setup:**

| Plugin | Purpose |
|---|---|
| Git Plugin | Clone Git repositories |
| Pipeline | Enables Jenkinsfile pipeline jobs |
| Blue Ocean | Modern visual pipeline UI |
| NodeJS Plugin | Install/use Node.js on agents |
| HTML Publisher | Publish Playwright/Allure HTML reports |
| JUnit Plugin | Parse and display JUnit XML test results |
| Email Extension | Send rich HTML failure emails |
| Credentials Binding | Inject secrets into pipelines securely |
| Workspace Cleanup | Clean workspace before/after builds |
| Allure Jenkins Plugin | Publish Allure reports |
| Docker Pipeline | Run steps inside Docker containers |

---

**Q4: What is the difference between a Freestyle job and a Pipeline job in Jenkins? When do you use each?**

**A:**

**Freestyle Job:**
- Configuration is done entirely through the Jenkins web UI (point and click)
- Simple to set up; good for one-off tasks or small teams new to Jenkins
- Configuration is stored as XML on the Jenkins controller — not in the repository
- Cannot be version-controlled or code-reviewed
- Limited ability to handle complex multi-stage logic
- No parallelism without plugins

**Pipeline Job (Recommended for all QA work):**
- Defined in a `Jenkinsfile` committed to the source repository
- Version-controlled: you can see who changed the pipeline, when, and why
- Reviewable in pull requests alongside the code changes
- Supports complex logic: stages, parallel execution, conditions, retries, loops
- Portable: any Jenkins instance with the right plugins runs the same Jenkinsfile
- Two syntax options: Declarative (preferred) and Scripted

**Decision guide:**
- Use **Freestyle** only for trivial administrative tasks or if you are prototyping something quickly
- Use **Pipeline** for all QA automation — it is the industry standard and enables "pipeline as code"

---

**Q5: What is a Jenkinsfile? Why is pipeline-as-code better than GUI configuration?**

**A:**

A Jenkinsfile is a text file that defines a Jenkins pipeline using Groovy DSL. It lives at the root of the repository alongside the application code. When Jenkins detects changes to the repository, it reads the Jenkinsfile and executes the pipeline it defines.

**Benefits of pipeline-as-code (Jenkinsfile over freestyle GUI config):**

| Benefit | Explanation |
|---|---|
| Version control | Every change is tracked in Git with author, timestamp, and diff |
| Code review | Pipeline changes go through pull request review like any other code |
| Reproducibility | Clone the repo on any Jenkins instance and get the same pipeline |
| Collaboration | The whole team can read and improve the pipeline |
| Recovery | If Jenkins controller is lost, pipelines are rebuilt from Git |
| Traceability | You know exactly which pipeline definition ran for a given build |

**Jenkinsfile location:**

```
my-project/
├── src/
├── tests/
├── pom.xml
├── playwright.config.ts
└── Jenkinsfile          <-- lives here at the root
```

---

**Q6: What is the difference between Declarative and Scripted pipeline syntax in Jenkins?**

**A:**

Both syntaxes define Jenkins pipelines in Groovy, but they differ in structure, flexibility, and readability.

**Declarative Pipeline:**
- Has a rigid, opinionated structure using the `pipeline {}` block
- Jenkins validates the structure at parse time — errors caught before execution
- Easier to read for teams unfamiliar with Groovy
- Recommended for most QA pipeline use cases

```groovy
// Declarative — structured, validated by Jenkins before running
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
    }
    post {
        failure {
            mail to: 'qa@company.com', subject: 'Build Failed'
        }
    }
}
```

**Scripted Pipeline:**
- Based on raw Groovy; no enforced structure
- Maximum flexibility — any Groovy code is valid
- More powerful but harder to read and validate
- Used when declarative's constraints are too limiting

```groovy
// Scripted — full Groovy power, more complex
node('linux') {
    try {
        stage('Checkout') {
            checkout scm
        }
        stage('Test') {
            sh 'mvn test'
        }
    } catch (err) {
        mail to: 'qa@company.com', subject: "Failed: ${err}"
        throw err
    }
}
```

**Rule of thumb:** Start with Declarative. Use Scripted only when you have a specific need that Declarative cannot meet (advanced dynamic stage generation, complex exception handling patterns).

---

**Q7: Write a complete Jenkinsfile for a QA test pipeline covering checkout, install, smoke tests, regression tests, publish reports, and email on failure.**

**A:**

```groovy
pipeline {
    // Run on any available agent; replace 'any' with a label for specific machines
    agent any
    // agent { label 'linux-selenium' }

    tools {
        maven  'Maven 3.9.6'     // Name must match Global Tool Configuration
        jdk    'JDK 17'
        nodejs 'NodeJS 18'
    }

    environment {
        APP_ENV      = 'staging'
        BASE_URL     = 'https://staging.myapp.com'
        BROWSER      = 'chromium'
        REPORTS_DIR  = 'target/surefire-reports'
        // Jenkins Credentials Plugin — values are injected at runtime and masked in logs
        APP_USER     = credentials('staging-username')
        APP_PASS     = credentials('staging-password')
    }

    parameters {
        choice(
            name: 'TEST_SUITE',
            choices: ['smoke', 'regression', 'full'],
            description: 'Select which test suite to run'
        )
        string(
            name: 'BROWSER_PARAM',
            defaultValue: 'chromium',
            description: 'Browser for Playwright tests'
        )
        booleanParam(
            name: 'SEND_EMAIL',
            defaultValue: true,
            description: 'Send email notification on failure?'
        )
    }

    triggers {
        pollSCM('H/5 * * * *')       // Poll SCM every 5 minutes (use webhooks in prod)
        cron('0 2 * * *')             // Nightly full regression at 2 AM
    }

    options {
        timeout(time: 60, unit: 'MINUTES')          // Abort if pipeline takes longer than 60 min
        buildDiscarder(logRotator(numToKeepStr: '10')) // Keep last 10 builds
        timestamps()                                  // Add timestamps to all log lines
    }

    stages {

        // ── STAGE 1: CHECKOUT ─────────────────────────────────────────────
        stage('Checkout') {
            steps {
                cleanWs()        // Clean workspace before checkout
                checkout scm
                echo "Branch : ${env.GIT_BRANCH}"
                echo "Commit : ${env.GIT_COMMIT}"
                echo "Build  : #${env.BUILD_NUMBER}"
            }
        }

        // ── STAGE 2: INSTALL DEPENDENCIES (PARALLEL) ─────────────────────
        stage('Install Dependencies') {
            parallel {
                stage('Maven Dependencies') {
                    steps {
                        sh 'mvn dependency:resolve -q'
                    }
                }
                stage('Node / Playwright Dependencies') {
                    steps {
                        sh 'npm ci'
                        sh 'npx playwright install chromium firefox --with-deps'
                    }
                }
            }
        }

        // ── STAGE 3: BUILD ────────────────────────────────────────────────
        stage('Build') {
            steps {
                sh 'mvn clean compile -q'
            }
        }

        // ── STAGE 4: SMOKE TESTS ──────────────────────────────────────────
        stage('Smoke Tests') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    expression {
                        return params.TEST_SUITE == 'smoke' || params.TEST_SUITE == 'full'
                    }
                }
            }
            steps {
                sh """
                    mvn test \
                      -Dtest.suite=smoke \
                      -Denv=${APP_ENV} \
                      -Dbase.url=${BASE_URL} \
                      -Dusername=${APP_USER} \
                      -Dpassword=${APP_PASS} \
                      -Dsurefire.failIfNoSpecifiedTests=false
                """
            }
            post {
                always {
                    // Parse JUnit XML and display results in Jenkins UI
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/**/*.xml'
                }
                failure {
                    echo 'Smoke tests FAILED — regression will not run'
                }
            }
        }

        // ── STAGE 5: REGRESSION TESTS (PARALLEL BROWSERS) ────────────────
        stage('Regression Tests') {
            when {
                anyOf {
                    branch 'main'
                    expression {
                        return params.TEST_SUITE == 'regression' || params.TEST_SUITE == 'full'
                    }
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
                            junit allowEmptyResults: true,
                                  testResults: 'test-results/chromium/*.xml'
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
                            junit allowEmptyResults: true,
                                  testResults: 'test-results/firefox/*.xml'
                        }
                    }
                }
            }
        }

        // ── STAGE 6: PUBLISH REPORTS ──────────────────────────────────────
        stage('Publish Reports') {
            steps {
                // Publish Surefire HTML report (requires HTML Publisher plugin)
                publishHTML([
                    allowMissing:          true,
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

                // Archive raw XML artifacts for downstream tools (Allure, etc.)
                archiveArtifacts(
                    artifacts: 'target/surefire-reports/**/*.xml, test-results/**/*.xml',
                    allowEmptyArchive: true,
                    fingerprint: true
                )
            }
        }
    }

    // ── POST ACTIONS ──────────────────────────────────────────────────────
    post {
        always {
            echo "Pipeline complete. Result: ${currentBuild.currentResult}"
        }
        success {
            echo 'All tests passed. Build is GREEN.'
        }
        unstable {
            // JUnit plugin marks build unstable when tests fail (not a build error)
            echo 'Some tests failed — build is UNSTABLE.'
        }
        failure {
            script {
                if (params.SEND_EMAIL) {
                    emailext(
                        subject: "FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                        body: """
                            <h2 style="color:red;">Build Failed</h2>
                            <table>
                                <tr><td><b>Job</b></td><td>${env.JOB_NAME}</td></tr>
                                <tr><td><b>Build</b></td><td>#${env.BUILD_NUMBER}</td></tr>
                                <tr><td><b>Branch</b></td><td>${env.GIT_BRANCH}</td></tr>
                                <tr><td><b>Commit</b></td><td>${env.GIT_COMMIT}</td></tr>
                                <tr><td><b>Console</b></td><td><a href="${env.BUILD_URL}console">View Console</a></td></tr>
                                <tr><td><b>Report</b></td><td><a href="${env.BUILD_URL}Playwright_Report">View Report</a></td></tr>
                            </table>
                        """,
                        mimeType: 'text/html',
                        to: 'qa-team@company.com',
                        replyTo: 'no-reply@company.com',
                        attachLog: false
                    )
                }
            }
        }
        cleanup {
            // Always clean workspace after the build finishes
            cleanWs()
        }
    }
}
```

---

**Q8: What are Jenkins stages and how do you define them? What happens if a stage fails?**

**A:**

Stages are logical groupings of steps within a pipeline that represent distinct phases of work. They appear as separate blocks in the Jenkins UI (and as visual nodes in Blue Ocean), making it easy to see at a glance exactly where a pipeline failed.

**Defining stages:**

```groovy
pipeline {
    agent any
    stages {
        stage('Checkout') {      // Stage 1
            steps {
                checkout scm
            }
        }
        stage('Build') {         // Stage 2 — only runs if Stage 1 passed
            steps {
                sh 'mvn compile'
            }
        }
        stage('Test') {          // Stage 3
            steps {
                sh 'mvn test'
            }
        }
        stage('Deploy') {        // Stage 4
            steps {
                sh './deploy.sh staging'
            }
        }
    }
}
```

**When a stage fails:**
- By default, Jenkins marks the pipeline as FAILED and skips all subsequent stages
- The `post {}` block at stage level and pipeline level still executes
- You can change this behaviour with `catchError`, `try/catch`, or by marking a stage as `failFast: false` in a parallel block

**Conditional stages using `when`:**

```groovy
stage('Deploy to Production') {
    when {
        branch 'main'          // Only run on the main branch
    }
    steps {
        sh './deploy.sh prod'
    }
}

stage('Regression Tests') {
    when {
        anyOf {
            branch 'main'
            expression { return params.RUN_FULL_SUITE == true }
        }
    }
    steps {
        sh 'mvn test -Dtest.suite=regression'
    }
}
```

---

**Q9: What are post actions in Jenkins? Explain always, success, failure, unstable, and cleanup.**

**A:**

Post actions define steps that run after the pipeline (or stage) completes, regardless of whether it passed or failed. They are defined in the `post {}` block at the pipeline or stage level.

```groovy
post {
    always {
        // Runs every time — whether the build passed, failed, or was aborted
        // Use for: publishing test results, archiving artifacts, logging
        junit allowEmptyResults: true, testResults: '**/*.xml'
        echo "Build result: ${currentBuild.currentResult}"
    }

    success {
        // Only runs if the pipeline completed with no failures
        // Use for: deployment to staging, release tagging, success notifications
        echo 'All tests passed. Deploying to staging.'
        sh './deploy.sh staging'
    }

    failure {
        // Only runs if the pipeline or a stage failed
        // Use for: failure notifications, alert on-call
        emailext(
            subject: "FAILED: ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
            body: "Check console: ${env.BUILD_URL}console",
            to: 'qa-team@company.com'
        )
        slackSend(
            color: 'danger',
            message: "Pipeline FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        )
    }

    unstable {
        // Runs when the build completed but is marked UNSTABLE
        // JUnit plugin marks builds unstable when tests fail (not a compilation error)
        // Use for: warning notifications when some tests failed
        echo 'Tests had failures. Review the test report.'
        emailext(
            subject: "UNSTABLE: ${env.JOB_NAME} [${env.BUILD_NUMBER}] — test failures",
            body: "Some tests failed. See report: ${env.BUILD_URL}Playwright_Report",
            to: 'qa-team@company.com'
        )
    }

    aborted {
        // Runs when the build was manually cancelled or timed out
        echo 'Pipeline was aborted.'
    }

    cleanup {
        // Always runs LAST, after all other post conditions
        // Use for: workspace cleanup
        cleanWs()
    }
}
```

**Post actions at stage level (runs after that specific stage only):**

```groovy
stage('Run Tests') {
    steps {
        sh 'mvn test'
    }
    post {
        always {
            junit 'target/surefire-reports/**/*.xml'
        }
        failure {
            echo 'Tests stage failed — not running further stages'
        }
    }
}
```

---

**Q10: How do you trigger a Jenkins job? List all trigger types with examples.**

**A:**

Jenkins jobs can be triggered in multiple ways:

**1. Manual trigger**
User clicks "Build Now" in the UI. No configuration needed.

**2. Poll SCM (scheduled polling)**
Jenkins periodically checks the repository for changes.

```groovy
triggers {
    // Check every 5 minutes; only trigger if there are new commits
    pollSCM('H/5 * * * *')
}
```

Cron syntax:
```
H/5 * * * *     Every 5 minutes
0 2 * * *       Every day at 2 AM
0 2 * * 1-5     Mon–Fri at 2 AM
0 0 * * 0       Every Sunday midnight
H(0-29) * * * * First 30 minutes of every hour (Jenkins hash distributes load)
```

**3. Cron schedule (time-based, no SCM check)**
Runs on a schedule regardless of code changes. Used for nightly regression runs.

```groovy
triggers {
    cron('0 2 * * *')   // Nightly at 2 AM
}
```

**4. Webhook trigger (GitHub/GitLab push)**
GitHub/GitLab sends an HTTP POST to Jenkins when code is pushed. Instant trigger — no polling delay.

Setup:
- Install "GitHub Plugin" or "GitLab Plugin" in Jenkins
- In Jenkins job: check "GitHub hook trigger for GITScm polling"
- In GitHub: Settings → Webhooks → Add webhook → `http://jenkins-url/github-webhook/`

**5. Upstream job trigger (build after another job)**

```groovy
triggers {
    // Trigger this job when 'build-and-package' job completes successfully
    upstream(upstreamProjects: 'build-and-package', threshold: hudson.model.Result.SUCCESS)
}
```

**6. Pull request trigger**
Using the GitHub Branch Source plugin or Multibranch Pipeline — Jenkins automatically creates and runs a pipeline for every open pull request.

**7. Build with Parameters (manual but configurable)**

```groovy
parameters {
    choice(name: 'ENV', choices: ['staging', 'prod'], description: 'Target environment')
    string(name: 'BRANCH', defaultValue: 'main', description: 'Branch to test')
    booleanParam(name: 'RUN_REGRESSION', defaultValue: false)
}
```

---

**Q11: What are environment variables in Jenkins? What built-in variables are always available?**

**A:**

Environment variables are key-value pairs available to all steps in a pipeline. Jenkins provides a set of built-in variables and you can define your own.

**Built-in Jenkins environment variables:**

```groovy
env.BUILD_NUMBER      // "42" — auto-incrementing build number
env.BUILD_URL         // "http://jenkins.company.com/job/my-job/42/"
env.JOB_NAME          // "my-project/my-job"
env.JOB_BASE_NAME     // "my-job" (without folder path)
env.GIT_BRANCH        // "origin/main"
env.GIT_COMMIT        // "a1b2c3d4..." (full SHA)
env.GIT_URL           // "https://github.com/org/repo.git"
env.WORKSPACE         // "/var/jenkins/workspace/my-job"
env.NODE_NAME         // "agent-linux-1" (or "master")
env.EXECUTOR_NUMBER   // "0" (executor slot index)
env.BUILD_TAG         // "jenkins-my-job-42"
```

**Defining custom environment variables in a pipeline:**

```groovy
pipeline {
    environment {
        // Plain string value
        APP_ENV   = 'staging'
        BASE_URL  = 'https://staging.myapp.com'

        // From Jenkins Credentials store (secret text)
        API_KEY   = credentials('api-key-secret-id')

        // From Jenkins Credentials store (username + password)
        // Automatically creates: DB_CREDS_USR and DB_CREDS_PSW
        DB_CREDS  = credentials('database-credentials-id')
    }
    stages {
        stage('Test') {
            steps {
                // Access in shell
                sh """
                    echo "Running against: ${BASE_URL}"
                    mvn test -Dbase.url=${BASE_URL} -Denv=${APP_ENV}
                """
                // Note: API_KEY and DB_CREDS values are masked in logs
            }
        }
    }
}
```

**Stage-scoped environment variables (override pipeline-level):**

```groovy
stage('Production Smoke') {
    environment {
        BASE_URL = 'https://myapp.com'   // Overrides pipeline-level BASE_URL for this stage only
        APP_ENV  = 'prod'
    }
    steps {
        sh 'mvn test -Denv=${APP_ENV} -Dbase.url=${BASE_URL}'
    }
}
```

---

**Q12: How do you store credentials securely in Jenkins? How does the Credentials Plugin work?**

**A:**

Never hardcode passwords, API keys, or tokens in a Jenkinsfile. Use the Jenkins Credentials Plugin to store secrets encrypted on the controller.

**Adding a credential:**
1. Navigate to: Jenkins → Manage Jenkins → Credentials → System → Global credentials
2. Click "Add Credentials"
3. Choose kind: Username with password / Secret text / SSH Username with private key / Certificate
4. Set an ID (e.g., `staging-app-credentials`) — this is what you reference in the Jenkinsfile
5. Jenkins stores the value encrypted using the master key

**Using credentials in a Jenkinsfile:**

```groovy
pipeline {
    environment {
        // Type 1: Secret text — creates a single variable
        API_TOKEN = credentials('my-api-token')

        // Type 2: Username + Password — creates TWO variables:
        //   APP_CREDS_USR  = the username
        //   APP_CREDS_PSW  = the password
        APP_CREDS = credentials('staging-login')

        // Type 3: SSH private key
        SSH_KEY = credentials('deploy-ssh-key')
    }
    stages {
        stage('Run API Tests') {
            steps {
                // API_TOKEN is masked in logs — appears as ****
                sh 'curl -H "Authorization: Bearer ${API_TOKEN}" ${BASE_URL}/api/health'
            }
        }
        stage('Login Test') {
            steps {
                sh """
                    mvn test \
                      -Dusername=${APP_CREDS_USR} \
                      -Dpassword=${APP_CREDS_PSW}
                """
            }
        }
    }
}

// Alternative: withCredentials block for fine-grained control
stage('Deploy') {
    steps {
        withCredentials([
            usernamePassword(
                credentialsId: 'staging-login',
                usernameVariable: 'DEPLOY_USER',
                passwordVariable: 'DEPLOY_PASS'
            )
        ]) {
            sh './deploy.sh ${DEPLOY_USER} ${DEPLOY_PASS}'
        }
        // DEPLOY_PASS is unset after this block — additional security
    }
}
```

**Security rules:**
- Never `echo` or `print` a credential variable — Jenkins masks them but it is bad practice
- Use "Masked" credentials where possible
- Apply the "Principle of Least Privilege" — give jobs only the credentials they need

---

**Q13: How do you publish HTML reports in Jenkins? How does the HTML Publisher plugin work?**

**A:**

The HTML Publisher plugin lets Jenkins link directly to test reports (Playwright HTML reports, Allure, ExtentReports, Surefire) from the build page.

**Installation:** Manage Plugins → Available → "HTML Publisher Plugin"

**Jenkinsfile configuration:**

```groovy
stage('Publish Reports') {
    steps {
        // Publish Playwright HTML report
        publishHTML([
            allowMissing:          true,   // Don't fail if report dir doesn't exist
            alwaysLinkToLastBuild: true,   // Show link even for older builds
            keepAll:               true,   // Preserve reports for all builds, not just latest
            reportDir:             'playwright-report',    // Directory containing the report
            reportFiles:           'index.html',           // Entry point HTML file
            reportName:            'Playwright Report',    // Link label in Jenkins UI
            reportTitles:          'Playwright Test Report' // Browser tab title
        ])

        // Publish Surefire (Maven/JUnit) report
        publishHTML([
            allowMissing:          true,
            alwaysLinkToLastBuild: true,
            keepAll:               true,
            reportDir:             'target/site/surefire-report',
            reportFiles:           'surefire-report.html',
            reportName:            'Surefire HTML Report'
        ])
    }
}

// Also always parse JUnit XML (separate from HTML Publisher)
post {
    always {
        junit allowEmptyResults: true,
              testResults: 'target/surefire-reports/**/*.xml, test-results/**/*.xml'
    }
}
```

**Note on Content Security Policy:** Jenkins 2.x applies a strict CSP that blocks inline scripts/styles in published HTML reports. To view Playwright reports correctly, add this to Jenkins startup arguments:

```
-Dhudson.model.DirectoryBrowserSupport.CSP=""
```

Or navigate to: Manage Jenkins → Script Console and run:

```groovy
System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")
```

---

**Q14: What is Blue Ocean in Jenkins? What advantages does it offer over the classic UI?**

**A:**

Blue Ocean is a modern, visual pipeline UI for Jenkins. It was introduced to address the dated look and poor usability of the classic Jenkins interface.

**Install:** Manage Plugins → Available → "Blue Ocean"
**Access:** `http://your-jenkins:8080/blue`

**Features of Blue Ocean:**

| Feature | Classic Jenkins | Blue Ocean |
|---|---|---|
| Pipeline view | Text-based stage list | Visual graph with coloured nodes |
| Parallel stages | Shown sequentially in logs | Displayed side-by-side |
| Test failures | Buried in console output | Surfaced prominently with test names |
| Log viewing | Entire build log | Per-stage log filtering |
| Branch/PR overview | Not built-in | Visual dashboard per branch and PR |
| Usability | Developer-focused | Accessible to non-technical stakeholders |

**Blue Ocean Pipeline Visualization:**

```
Checkout  ->  Install  ->  Smoke  ->  [Chromium]  ->  Publish
                                   ->  [Firefox ]  ->  Reports
                                   ->  [WebKit  ]
              (green)     (green)   (all green)      (green)
```

Blue Ocean is read-only for most users — pipeline edits still happen in the Jenkinsfile.

---

**Q15: How do you implement parallel execution in Jenkins pipelines? When would you use it?**

**A:**

Parallel execution runs multiple stages or steps simultaneously, reducing total pipeline duration. Common QA use cases: running tests across multiple browsers, multiple environments, or splitting a large test suite.

**Parallel stages within a stage:**

```groovy
stage('Cross-Browser Tests') {
    parallel {
        stage('Chromium') {
            agent { label 'linux' }
            steps {
                sh 'npx playwright test --project=chromium'
            }
            post {
                always { junit 'test-results/chromium/*.xml' }
            }
        }
        stage('Firefox') {
            agent { label 'linux' }
            steps {
                sh 'npx playwright test --project=firefox'
            }
            post {
                always { junit 'test-results/firefox/*.xml' }
            }
        }
        stage('WebKit') {
            agent { label 'linux' }
            steps {
                sh 'npx playwright test --project=webkit'
            }
            post {
                always { junit 'test-results/webkit/*.xml' }
            }
        }
    }
}
```

**Fail-fast control:**

```groovy
stage('Parallel Tests') {
    failFast true   // Cancel all parallel branches if ANY one fails
    parallel {
        // ... stage definitions
    }
}
```

**Dynamic parallel execution (programmatic):**

```groovy
def browsers = ['chromium', 'firefox', 'webkit']

stage('Tests - All Browsers') {
    parallel browsers.collectEntries { browser ->
        ["${browser}": {
            stage(browser) {
                sh "npx playwright test --project=${browser}"
            }
        }]
    }
}
```

**When to use parallel:**
- Cross-browser testing (always — reduces run time from 3x sequential to 1x)
- Running API tests and UI tests simultaneously
- Installing Maven and Node dependencies simultaneously
- Running tests against different data sets

**Limitation:** Each parallel branch needs its own workspace or must not write to the same files.

---

## SECTION 2: GITLAB CI/CD

---

**Q16: What is GitLab CI/CD? How is it different from Jenkins?**

**A:**

GitLab CI/CD is a built-in continuous integration system that comes with GitLab. It is configured by a `.gitlab-ci.yml` file at the repository root. When you push, GitLab automatically detects this file and runs the pipeline.

**Key differences from Jenkins:**

| Aspect | Jenkins | GitLab CI/CD |
|---|---|---|
| Setup | Separate server to install and maintain | Built into GitLab — zero setup |
| Configuration | Groovy DSL (Jenkinsfile) | YAML (.gitlab-ci.yml) |
| Runners | Jenkins Agents (must configure manually) | GitLab Runners (shared by GitLab.com or self-hosted) |
| Plugin system | 1800+ plugins, required for most features | Features built in; minimal plugins |
| UI integration | Separate tool from the repo | Fully integrated: pipelines visible on MR page |
| Secret management | Credentials plugin | Settings → CI/CD → Variables |
| Learning curve | High (Groovy, plugin config) | Medium (YAML, familiar syntax) |
| Cost | Free software but infrastructure cost | Free tier on GitLab.com; paid for advanced features |
| Container support | Docker Pipeline plugin | First-class; every job runs in a Docker image |

**GitLab CI advantage for QA:** Pipeline results appear directly on merge request pages. A reviewer can see that all tests passed before approving, without leaving GitLab.

---

**Q17: What is `.gitlab-ci.yml`? Walk through its structure with a complete example.**

**A:**

`.gitlab-ci.yml` is the pipeline definition file for GitLab CI/CD. It lives at the root of the repository. Every push to any branch (or merge request) causes GitLab to read this file and execute the defined pipeline using a GitLab Runner.

**Complete annotated example:**

```yaml
# ── GLOBAL SETTINGS ────────────────────────────────────────────────────────

# Default Docker image for all jobs (override per-job as needed)
image: eclipse-temurin:17-jdk

# Pipeline stage order — jobs in the same stage run in parallel
stages:
  - install
  - build
  - smoke
  - regression
  - report

# Variables available in all jobs
variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
  APP_ENV: "staging"
  BASE_URL: "https://staging.myapp.com"
  # Secrets are set in: Settings → CI/CD → Variables (never hardcoded here)

# ── YAML ANCHORS (reusable config blocks) ──────────────────────────────────
.maven-cache: &maven-cache
  cache:
    key: "$CI_COMMIT_REF_SLUG-maven"
    paths:
      - .m2/repository
    policy: pull-push

.node-cache: &node-cache
  cache:
    key: "$CI_COMMIT_REF_SLUG-node"
    paths:
      - node_modules/
      - ~/.cache/ms-playwright
    policy: pull-push

# ── INSTALL STAGE ───────────────────────────────────────────────────────────
install:maven:
  stage: install
  <<: *maven-cache        # Merge the maven-cache anchor
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

# ── BUILD STAGE ─────────────────────────────────────────────────────────────
build:compile:
  stage: build
  <<: *maven-cache
  script:
    - mvn clean compile -q
  artifacts:
    paths:
      - target/classes/
    expire_in: 1 hour

# ── SMOKE STAGE ─────────────────────────────────────────────────────────────
smoke:api:
  stage: smoke
  <<: *maven-cache
  script:
    - mvn test -Dtest.suite=smoke -Denv=${APP_ENV} -Dbase.url=${BASE_URL}
  artifacts:
    when: always           # Collect artifacts even if the job fails
    reports:
      junit: target/surefire-reports/TEST-*.xml   # Native GitLab JUnit integration
    paths:
      - target/surefire-reports/
    expire_in: 7 days
  only:
    - main
    - develop
    - merge_requests

# ── REGRESSION STAGE — PARALLEL BROWSER MATRIX ──────────────────────────────
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

# ── MERGE REQUEST ONLY — LIGHTER SMOKE RUN ──────────────────────────────────
mr:playwright:smoke:
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

# ── REPORT STAGE — GitLab Pages ─────────────────────────────────────────────
pages:
  stage: report
  script:
    - mkdir -p public
    - cp -r playwright-report/* public/ || true
  artifacts:
    paths:
      - public
    expire_in: 30 days
  only:
    - main
```

---

**Q18: Explain stages, jobs, image, script, and artifacts in GitLab CI. What does each keyword do?**

**A:**

**`stages`:** Defines the ordered list of pipeline phases. Jobs in the same stage run in parallel; stages run sequentially.

```yaml
stages:
  - install     # All install jobs run first, in parallel
  - build       # All build jobs run next
  - test        # All test jobs run simultaneously
  - deploy      # Deploy runs last
```

**`image`:** The Docker image to use as the job's environment. GitLab runners pull this image and execute the job inside it.

```yaml
image: eclipse-temurin:17-jdk     # Java 17 for Maven jobs
# Per-job override:
my-node-job:
  image: node:18-alpine            # Node 18 for this specific job
```

**`script`:** The shell commands to execute inside the job's container. These are the actual test commands.

```yaml
script:
  - echo "Starting tests"
  - mvn test -Dtest.suite=regression
  - echo "Tests complete"
```

**`before_script` and `after_script`:** Run before or after every job's `script` block.

```yaml
before_script:
  - echo "Setting up environment"
  - export TIMESTAMP=$(date +%s)

after_script:
  - echo "Cleaning up"
```

**`artifacts`:** Files produced by a job that are preserved and can be downloaded or passed to later stages.

```yaml
artifacts:
  when: always                    # Collect artifacts even on job failure
  expire_in: 7 days               # Auto-delete after 7 days
  paths:
    - target/surefire-reports/    # Store these directories
    - playwright-report/
  reports:
    junit: target/surefire-reports/TEST-*.xml   # Parse JUnit XML natively in GitLab UI
```

**`dependencies`:** Controls which previous-stage artifacts are downloaded into the current job.

```yaml
regression-tests:
  stage: regression
  dependencies:
    - build:compile    # Only download artifacts from this job
  script:
    - mvn test
```

---

**Q19: What are `rules` in GitLab CI? How do you use them to control when a job runs?**

**A:**

`rules` is the modern replacement for `only/except` in GitLab CI. It provides fine-grained control over when a job runs based on conditions evaluated at pipeline creation time.

**`rules` vs `only/except`:**
- `only/except` is simpler but less flexible and being phased out
- `rules` supports complex conditions, variables, and `when:` modes (manual, delayed, always, never)

**Complete `rules` examples:**

```yaml
# Run on main branch always; run on MRs as manual; never run otherwise
regression:all-browsers:
  stage: regression
  script:
    - npx playwright test
  rules:
    - if: '$CI_COMMIT_BRANCH == "main"'
      when: always
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
      when: manual         # Developer must explicitly trigger on MRs
      allow_failure: true  # Don't fail the MR pipeline if this is not triggered
    - when: never          # Default: don't run for anything else

# Run only on scheduled pipelines (nightly regression)
nightly:full-regression:
  stage: regression
  script:
    - npx playwright test --workers=4
  rules:
    - if: '$CI_PIPELINE_SOURCE == "schedule"'

# Run only on tags (release pipelines)
deploy:production:
  stage: deploy
  script:
    - ./deploy.sh prod
  rules:
    - if: '$CI_COMMIT_TAG'    # Only when a Git tag is pushed

# Run on MRs only if specific files changed
api:tests:
  stage: test
  script:
    - mvn test -Dtest.suite=api
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
      changes:
        - src/main/java/api/**/*   # Only run if API source files changed
        - tests/api/**/*
```

**Common `$CI_PIPELINE_SOURCE` values:**

| Value | Trigger |
|---|---|
| `push` | Direct push to a branch |
| `merge_request_event` | Merge request created or updated |
| `schedule` | Scheduled pipeline (GitLab CI schedules) |
| `web` | Manually triggered from GitLab UI |
| `api` | Triggered via GitLab API |

---

**Q20: How does caching work in GitLab CI? Show examples for Node modules and Maven repository.**

**A:**

Caching persists directories between pipeline runs so dependencies do not need to be re-downloaded from the internet every time. This dramatically reduces pipeline time (e.g., from 4 minutes to 30 seconds for `npm install`).

**Cache vs Artifacts:**
- **Cache:** Persisted between runs of the same pipeline. Not guaranteed — stored on the runner.
- **Artifacts:** Passed between jobs within the same pipeline. Guaranteed — stored in GitLab.

**Node modules cache:**

```yaml
variables:
  npm_config_cache: "$CI_PROJECT_DIR/.npm"

.node-cache: &node-cache
  cache:
    key:
      files:
        - package-lock.json    # Cache is invalidated when lock file changes
    paths:
      - .npm/
      - node_modules/
      - ~/.cache/ms-playwright
    policy: pull-push          # Pull at start, push updated cache at end

install:node:
  stage: install
  image: mcr.microsoft.com/playwright:v1.44.0-jammy
  <<: *node-cache
  script:
    - npm ci --cache .npm --prefer-offline   # Use local cache
    - npx playwright install chromium --with-deps

run:playwright:
  stage: test
  image: mcr.microsoft.com/playwright:v1.44.0-jammy
  <<: *node-cache
  script:
    - npx playwright test --project=chromium
```

**Maven repository cache:**

```yaml
variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"

.maven-cache: &maven-cache
  cache:
    key:
      files:
        - pom.xml              # Invalidate cache when pom.xml changes
    paths:
      - .m2/repository
    policy: pull-push

install:maven:
  stage: install
  <<: *maven-cache
  script:
    - mvn dependency:resolve -q --no-transfer-progress

test:java:
  stage: test
  <<: *maven-cache
  script:
    - mvn test -Denv=staging --no-transfer-progress
```

**Cache policy options:**
- `pull-push` (default): Download cache at start, upload updated cache at end
- `pull`: Download cache but never upload — use for read-only jobs to save time
- `push`: Never download, always upload — use for the install job that creates the cache

---

## SECTION 3: GITHUB ACTIONS

---

**Q21: What is GitHub Actions? How does it differ from Jenkins and GitLab CI?**

**A:**

GitHub Actions is a CI/CD platform built directly into GitHub. Workflows are defined as YAML files in `.github/workflows/` and triggered by GitHub events (push, pull request, schedule, etc.).

**Comparison:**

| Feature | Jenkins | GitLab CI | GitHub Actions |
|---|---|---|---|
| Where it lives | Separate server | Built into GitLab | Built into GitHub |
| Config file | Jenkinsfile (Groovy) | .gitlab-ci.yml | .github/workflows/*.yml |
| Runners | Jenkins Agents | GitLab Runners | GitHub-hosted (ubuntu/windows/macos) or self-hosted |
| Free tier | Free software (you pay for infra) | 400 CI/CD minutes/month | 2,000 minutes/month (public repos: unlimited) |
| Marketplace | 1800+ plugins | Limited | 20,000+ community actions |
| Learning curve | High | Medium | Low — YAML, excellent documentation |
| Parallel jobs | Parallel stages | Same-stage jobs | Matrix strategy |
| Best for | Enterprise, complex pipelines | Teams on GitLab | Teams on GitHub |

**GitHub Actions core concepts:**
- **Workflow:** A YAML file defining automation triggered by events
- **Event:** What triggers the workflow (push, PR, schedule, manual)
- **Job:** A collection of steps that runs on a single runner
- **Step:** A single task — run a command (`run`) or use a pre-built action (`uses`)
- **Action:** A reusable unit of work from the marketplace (e.g., `actions/checkout@v4`)
- **Runner:** The machine that executes jobs (ubuntu-latest, windows-latest, macos-latest)

---

**Q22: Write a complete GitHub Actions workflow for running Playwright tests including build, smoke, regression, and notifications.**

**A:**

```yaml
# .github/workflows/qa-pipeline.yml

name: QA Test Pipeline

# ── TRIGGERS ────────────────────────────────────────────────────────────────
on:
  push:
    branches:
      - main
      - develop
  pull_request:
    branches:
      - main
  schedule:
    - cron: '0 1 * * 1-5'        # Nightly at 1 AM UTC, Mon–Fri
  workflow_dispatch:              # Allow manual trigger from GitHub UI
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

# Cancel in-progress runs when a new push is made to the same PR/branch
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

# ── GLOBAL ENV VARIABLES ─────────────────────────────────────────────────────
env:
  JAVA_VERSION: '17'
  NODE_VERSION: '18'
  APP_ENV: 'staging'
  BASE_URL: 'https://staging.myapp.com'

# ── JOBS ────────────────────────────────────────────────────────────────────
jobs:

  # ── JOB 1: BUILD ───────────────────────────────────────────────────────────
  build:
    name: Build Application
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven              # Automatically caches ~/.m2

      - name: Build with Maven
        run: mvn clean compile -q --no-transfer-progress

      - name: Upload build artifacts
        uses: actions/upload-artifact@v4
        with:
          name: build-output
          path: target/classes/
          retention-days: 1

  # ── JOB 2: SMOKE TESTS ─────────────────────────────────────────────────────
  smoke-tests:
    name: Smoke Tests (Java/API)
    runs-on: ubuntu-latest
    needs: build                  # Wait for build to complete

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Run smoke test suite
        run: |
          mvn test \
            -Dtest.suite=smoke \
            -Denv=${{ env.APP_ENV }} \
            -Dbase.url=${{ env.BASE_URL }} \
            -Dusername=${{ secrets.APP_USERNAME }} \
            -Dpassword=${{ secrets.APP_PASSWORD }} \
            --no-transfer-progress

      - name: Publish JUnit Results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()              # Run even if tests fail
        with:
          files: 'target/surefire-reports/**/*.xml'
          check_name: 'Smoke Test Results'

      - name: Upload Surefire Reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: surefire-reports
          path: target/surefire-reports/
          retention-days: 7

  # ── JOB 3: PLAYWRIGHT — MATRIX STRATEGY ────────────────────────────────────
  playwright-tests:
    name: Playwright - ${{ matrix.browser }}
    runs-on: ubuntu-latest
    needs: smoke-tests
    # Only run full regression on main branch or scheduled nightly run
    if: |
      github.ref == 'refs/heads/main' ||
      github.event_name == 'schedule' ||
      github.event.inputs.test_suite == 'regression' ||
      github.event.inputs.test_suite == 'full'

    strategy:
      fail-fast: false            # Continue other browsers if one fails
      matrix:
        browser: [chromium, firefox, webkit]

    steps:
      - uses: actions/checkout@v4

      - name: Set up Node.js ${{ env.NODE_VERSION }}
        uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'npm'

      - name: Install Node dependencies
        run: npm ci

      - name: Install Playwright browser — ${{ matrix.browser }}
        run: npx playwright install ${{ matrix.browser }} --with-deps

      - name: Run Playwright tests — ${{ matrix.browser }}
        run: |
          npx playwright test \
            --project=${{ matrix.browser }} \
            --reporter=junit,html
        env:
          BASE_URL:     ${{ env.BASE_URL }}
          APP_USERNAME: ${{ secrets.APP_USERNAME }}
          APP_PASSWORD: ${{ secrets.APP_PASSWORD }}
          CI:           true

      - name: Upload Playwright HTML Report — ${{ matrix.browser }}
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report-${{ matrix.browser }}
          path: playwright-report/
          retention-days: 14

      - name: Upload JUnit XML Results — ${{ matrix.browser }}
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results-${{ matrix.browser }}
          path: test-results/
          retention-days: 7

  # ── JOB 4: PR SMOKE (lightweight check on pull requests) ───────────────────
  pr-smoke:
    name: PR Smoke Check
    runs-on: ubuntu-latest
    needs: build
    if: github.event_name == 'pull_request'

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'npm'
      - run: npm ci
      - run: npx playwright install chromium --with-deps
      - name: Run smoke tests on Chromium
        run: npx playwright test --project=chromium --grep "@smoke"
        env:
          BASE_URL: ${{ env.BASE_URL }}
          CI: true
      - if: always()
        uses: actions/upload-artifact@v4
        with:
          name: pr-smoke-report
          path: playwright-report/
          retention-days: 3

  # ── JOB 5: NOTIFY ON FAILURE ───────────────────────────────────────────────
  notify-failure:
    name: Notify on Failure
    runs-on: ubuntu-latest
    needs: [smoke-tests, playwright-tests]
    if: failure()

    steps:
      - name: Send Slack notification
        uses: 8398a7/action-slack@v3
        with:
          status: failure
          channel: '#qa-alerts'
          text: |
            Pipeline FAILED on `${{ github.ref_name }}`
            Triggered by: ${{ github.actor }}
            Run: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

---

**Q23: What are the `on:` trigger types in GitHub Actions? Explain each with examples.**

**A:**

The `on:` key defines what GitHub event triggers a workflow.

```yaml
on:
  # ── PUSH — runs on code push ──────────────────────────────────────────────
  push:
    branches:
      - main
      - 'release/**'          # All branches starting with release/
    paths:
      - 'src/**'              # Only trigger if files under src/ change
      - 'tests/**'
    paths-ignore:
      - '**.md'               # Never trigger for markdown-only changes

  # ── PULL REQUEST — runs when PR is opened, updated, or synchronized ───────
  pull_request:
    branches:
      - main
    types:
      - opened                # PR created
      - synchronize           # New commits pushed to PR branch
      - reopened              # PR reopened after close

  # ── SCHEDULE — cron-based scheduled runs ─────────────────────────────────
  schedule:
    - cron: '0 2 * * 1-5'    # Mon–Fri at 2 AM UTC
    - cron: '0 0 * * 0'      # Every Sunday midnight (weekly full regression)

  # ── WORKFLOW DISPATCH — manual trigger from GitHub UI or API ──────────────
  workflow_dispatch:
    inputs:
      browser:
        description: 'Browser to test'
        required: true
        type: choice
        options: [chromium, firefox, webkit]
      environment:
        description: 'Target environment'
        required: false
        default: 'staging'
      run_full_suite:
        description: 'Run full regression?'
        type: boolean
        default: false

  # ── WORKFLOW CALL — called by another workflow (reusable workflows) ────────
  workflow_call:
    inputs:
      browser:
        required: true
        type: string

  # ── RELEASE — triggers on GitHub releases ─────────────────────────────────
  release:
    types: [published]        # Run tests when a release is published

  # ── REPOSITORY DISPATCH — external HTTP trigger ───────────────────────────
  repository_dispatch:
    types: [run-tests]        # Triggered by POST to GitHub API
```

**Accessing trigger inputs in the workflow:**

```yaml
- name: Run tests for selected browser
  run: npx playwright test --project=${{ github.event.inputs.browser }}
```

---

**Q24: What is the matrix strategy in GitHub Actions? How do you use it for multi-browser testing?**

**A:**

The matrix strategy automatically generates multiple job instances from combinations of variables. For QA, the primary use is cross-browser testing — instead of writing one job per browser, you define the browsers as a list and GitHub creates one job per browser.

**Single-dimension matrix (browsers only):**

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false        # Don't cancel Firefox/WebKit if Chromium fails
      matrix:
        browser: [chromium, firefox, webkit]

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 18
          cache: 'npm'
      - run: npm ci
      - run: npx playwright install ${{ matrix.browser }} --with-deps
      - run: npx playwright test --project=${{ matrix.browser }}
      - if: always()
        uses: actions/upload-artifact@v4
        with:
          name: report-${{ matrix.browser }}
          path: playwright-report/
```

This generates 3 jobs: `test (chromium)`, `test (firefox)`, `test (webkit)`.

**Multi-dimension matrix (browsers × OS):**

```yaml
strategy:
  fail-fast: false
  matrix:
    browser: [chromium, firefox]
    os: [ubuntu-latest, windows-latest]
```

This generates 4 jobs: chromium+ubuntu, chromium+windows, firefox+ubuntu, firefox+windows.

**Matrix with exclude (skip specific combinations):**

```yaml
strategy:
  matrix:
    browser: [chromium, firefox, webkit]
    os: [ubuntu-latest, windows-latest]
    exclude:
      - browser: webkit
        os: ubuntu-latest     # WebKit only tested on Windows
```

**Matrix with include (add extra variables to specific combinations):**

```yaml
strategy:
  matrix:
    browser: [chromium, firefox, webkit]
    include:
      - browser: webkit
        os: macos-latest      # Use macOS for WebKit only
```

**Accessing matrix values:**

```yaml
name: Test - ${{ matrix.browser }} on ${{ matrix.os }}
runs-on: ${{ matrix.os || 'ubuntu-latest' }}
```

---

**Q25: How do you upload and download test artifacts in GitHub Actions?**

**A:**

Artifacts are files produced by a job (test reports, screenshots, videos) that are stored by GitHub and can be downloaded from the Actions UI or shared between jobs.

**Uploading artifacts:**

```yaml
- name: Upload Playwright HTML Report
  if: always()              # Critical: upload even when tests fail
  uses: actions/upload-artifact@v4
  with:
    name: playwright-report-${{ matrix.browser }}   # Must be unique per matrix job
    path: playwright-report/
    retention-days: 14       # Auto-delete after 14 days (max: 90 days)

- name: Upload Screenshots on Failure
  if: failure()
  uses: actions/upload-artifact@v4
  with:
    name: failure-screenshots
    path: test-results/**/*.png
    if-no-files-found: warn  # warn | error | ignore

- name: Upload multiple paths
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: all-test-outputs
    path: |
      playwright-report/
      test-results/
      target/surefire-reports/
```

**Downloading artifacts in a later job:**

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - run: npx playwright test
      - uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: test-results/

  publish-report:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - name: Download test results
        uses: actions/download-artifact@v4
        with:
          name: test-results
          path: ./downloaded-results/

      - name: Process and publish
        run: |
          ls ./downloaded-results/
          # Process the downloaded artifacts
```

---

**Q26: How do you use secrets in GitHub Actions? How are they stored and accessed safely?**

**A:**

GitHub Actions secrets are encrypted environment variables stored in GitHub and injected into workflow runs at execution time. They are masked in log output automatically.

**Adding secrets:**
1. Go to: Repository → Settings → Secrets and variables → Actions → New repository secret
2. Set Name (e.g., `APP_PASSWORD`) and Value
3. Click "Add secret" — the value is encrypted and cannot be viewed again

**Organization-level secrets:** Set once, available to all repositories in the org — ideal for shared credentials like Slack webhooks or API keys.

**Accessing secrets in a workflow:**

```yaml
env:
  # At workflow level — available to all jobs
  SLACK_WEBHOOK: ${{ secrets.SLACK_WEBHOOK_URL }}

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - name: Run tests with credentials
        run: |
          npx playwright test
        env:
          # At step level — passed to this step's environment only
          APP_USERNAME: ${{ secrets.APP_USERNAME }}
          APP_PASSWORD: ${{ secrets.APP_PASSWORD }}
          API_KEY:      ${{ secrets.API_KEY }}

      - name: Deploy with SSH
        uses: appleboy/ssh-action@master
        with:
          host:     ${{ secrets.DEPLOY_HOST }}
          username: ${{ secrets.DEPLOY_USER }}
          key:      ${{ secrets.DEPLOY_SSH_KEY }}
          script:   ./deploy.sh
```

**Security rules:**
- Never reference `${{ secrets.MY_SECRET }}` inside a `run:` command that echoes output — GitHub masks the value but it is bad practice
- Never set secrets as regular `env:` variables at the workflow level if only one job needs them
- Use environment-level secrets for production secrets (Settings → Environments → Production → Secrets) — these can require manual approval before the job can access them

---

## SECTION 4: PRACTICAL CI/CD FOR QA

---

**Q27: How do you make tests CI-ready? What checklist should every QA engineer follow?**

**A:**

CI environments are headless, containerised, stateless, and have limited resources. Tests that rely on local setup assumptions will fail. The CI-readiness checklist:

**1. Run in headless mode:**

```java
// Selenium — headless Chrome in CI
ChromeOptions options = new ChromeOptions();
if (System.getenv("CI") != null) {
    options.addArguments("--headless=new");
    options.addArguments("--no-sandbox");            // Required in Docker
    options.addArguments("--disable-dev-shm-usage"); // Required in Docker (small /dev/shm)
    options.addArguments("--window-size=1920,1080");
    options.addArguments("--disable-gpu");
}
WebDriver driver = new ChromeDriver(options);
```

```typescript
// Playwright — headless by default; set explicitly for clarity
// playwright.config.ts
use: {
    headless: process.env.CI === 'true',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    trace: 'on-first-retry',
}
```

**2. No hardcoded paths:**

```java
// WRONG — breaks on Linux CI
String reportPath = "C:\\Users\\Kupeshanth\\reports\\test.html";

// RIGHT — use project-relative path
String reportPath = System.getProperty("user.dir") + "/target/reports/test.html";
// Or use Paths.get
Path report = Paths.get("target", "reports", "test.html");
```

**3. No hardcoded URLs or credentials:**

```java
// WRONG
String baseUrl = "https://staging.myapp.com";
String password = "MyPassword123";

// RIGHT — read from environment or system properties
String baseUrl = System.getenv().getOrDefault("BASE_URL", "https://staging.myapp.com");
String password = System.getenv("APP_PASSWORD");
```

**4. Explicit waits, never Thread.sleep():**

```java
// WRONG — timing varies between local machine and slow CI
Thread.sleep(3000);

// RIGHT — wait for specific condition
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
WebElement element = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("result"))
);
```

**5. Each test is independent:**

```java
// WRONG — test 2 depends on test 1 having run
@Test
public void test1_createUser() { /* creates user with name "TestUser" */ }

@Test
public void test2_verifyUser() { /* assumes "TestUser" exists from test 1 */ }

// RIGHT — each test creates its own data
@Test
public void verifyUser() {
    String uniqueName = "User_" + System.currentTimeMillis();
    createUser(uniqueName);         // Setup inside this test
    verifyUserExists(uniqueName);
    deleteUser(uniqueName);         // Cleanup inside this test
}
```

**6. Configure timeouts via properties:**

```java
int timeout = Integer.parseInt(
    System.getProperty("wait.timeout", "30")  // 30s default; override in CI: -Dwait.timeout=60
);
```

**7. Use relative URLs in Playwright:**

```typescript
// WRONG — hardcoded domain
await page.goto('https://staging.myapp.com/login');

// RIGHT — uses baseURL from playwright.config.ts which reads from env var
await page.goto('/login');
```

**8. Retry flaky tests in CI only:**

```typescript
// playwright.config.ts
retries: process.env.CI ? 2 : 0,  // Retry twice in CI; never locally (to catch flakiness)
```

---

**Q28: How do you integrate JMeter load tests with Jenkins?**

**A:**

JMeter can run in non-GUI (headless) command-line mode, which makes it CI-compatible. The Jenkins Performance Plugin or HTML Publisher can then display JMeter's output.

**Step 1: Install JMeter on the Jenkins agent**

```bash
# On the Jenkins agent machine
wget https://downloads.apache.org/jmeter/binaries/apache-jmeter-5.6.3.tgz
tar -xzf apache-jmeter-5.6.3.tgz -C /opt/
export JMETER_HOME=/opt/apache-jmeter-5.6.3
export PATH=$JMETER_HOME/bin:$PATH
```

**Step 2: Jenkinsfile for JMeter**

```groovy
pipeline {
    agent any

    environment {
        JMETER_HOME = '/opt/apache-jmeter-5.6.3'
        JMX_FILE    = 'tests/performance/api-load-test.jmx'
        RESULTS_DIR = 'jmeter-results'
        BASE_URL    = 'https://staging.myapp.com'
    }

    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        stage('Run JMeter Load Tests') {
            steps {
                sh """
                    mkdir -p ${RESULTS_DIR}

                    ${JMETER_HOME}/bin/jmeter \
                      -n \
                      -t ${JMX_FILE} \
                      -l ${RESULTS_DIR}/results.jtl \
                      -e \
                      -o ${RESULTS_DIR}/html-report \
                      -Jbase.url=${BASE_URL} \
                      -Jthreads=50 \
                      -Jrampup=60 \
                      -Jduration=300
                """
            }
        }

        stage('Publish JMeter Report') {
            steps {
                publishHTML([
                    allowMissing:          false,
                    alwaysLinkToLastBuild: true,
                    keepAll:               true,
                    reportDir:             'jmeter-results/html-report',
                    reportFiles:           'index.html',
                    reportName:            'JMeter Load Test Report'
                ])

                // Archive raw .jtl results
                archiveArtifacts artifacts: 'jmeter-results/results.jtl',
                                 allowEmptyArchive: true
            }
        }
    }

    post {
        failure {
            emailext(
                subject: "PERF FAILURE: ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
                body: "JMeter tests failed. See report: ${env.BUILD_URL}JMeter_Load_Test_Report",
                to: 'qa-performance@company.com'
            )
        }
        cleanup {
            cleanWs()
        }
    }
}
```

**JMeter non-GUI command reference:**

```bash
jmeter -n \              # Non-GUI mode (headless — required in CI)
  -t test.jmx \          # Test plan file
  -l results.jtl \       # Raw results output (CSV/XML)
  -e \                   # Generate HTML report
  -o html-report/ \      # HTML report output directory
  -Jbase.url=https://... # Override property from command line
  -Jthreads=100          # Number of virtual users
```

---

**Q29: How do you integrate Playwright tests with GitHub Actions? Write a complete, production-quality workflow.**

**A:**

```yaml
# .github/workflows/playwright.yml

name: Playwright Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  schedule:
    - cron: '0 1 * * *'         # Nightly at 1 AM UTC

jobs:
  playwright:
    name: Playwright — ${{ matrix.browser }}
    runs-on: ubuntu-latest
    timeout-minutes: 30          # Abort the job if it runs longer than 30 minutes

    strategy:
      fail-fast: false
      matrix:
        browser: [chromium, firefox, webkit]

    steps:
      # ── SETUP ──────────────────────────────────────────────────────────────
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: 18
          cache: 'npm'           # Cache node_modules between runs

      - name: Install dependencies
        run: npm ci

      - name: Install Playwright browser
        run: npx playwright install ${{ matrix.browser }} --with-deps
        # --with-deps installs OS-level browser dependencies (critical on ubuntu-latest)

      # ── TEST EXECUTION ──────────────────────────────────────────────────────
      - name: Run Playwright tests
        run: npx playwright test --project=${{ matrix.browser }}
        env:
          BASE_URL:     ${{ vars.BASE_URL }}     # Non-secret config variable
          APP_USERNAME: ${{ secrets.APP_USERNAME }}
          APP_PASSWORD: ${{ secrets.APP_PASSWORD }}
          CI: true                              # Playwright reads this to enable retries

      # ── ARTIFACTS — always upload even on failure ──────────────────────────
      - name: Upload HTML report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: playwright-report-${{ matrix.browser }}-${{ github.run_id }}
          path: playwright-report/
          retention-days: 14

      - name: Upload test results (JUnit XML for status checks)
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results-${{ matrix.browser }}
          path: test-results/
          retention-days: 7

      - name: Upload traces on failure
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: traces-${{ matrix.browser }}
          path: test-results/**/*.zip
          retention-days: 3
```

**playwright.config.ts that pairs with this workflow:**

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './tests',
    timeout: 30_000,
    retries: process.env.CI ? 2 : 0,         // Retry on CI; fail fast locally
    workers: process.env.CI ? 4 : undefined,  // 4 parallel workers in CI

    reporter: [
        ['junit', { outputFile: 'test-results/results.xml' }],
        ['html', { open: 'never' }],          // 'never' so CI doesn't try to open a browser
        ['list'],
    ],

    use: {
        baseURL: process.env.BASE_URL ?? 'https://staging.myapp.com',
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

**Q30: How do you handle environment-specific testing in CI/CD? (dev / staging / production)**

**A:**

The core principle is: one codebase, multiple environments. Never hardcode environment values. Use environment variables, config files, or build profiles to switch between environments.

**Java/Maven — profiles approach:**

```xml
<!-- pom.xml -->
<profiles>
    <profile>
        <id>dev</id>
        <properties>
            <base.url>https://dev.myapp.com</base.url>
            <timeout>60</timeout>
        </properties>
    </profile>
    <profile>
        <id>staging</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <base.url>https://staging.myapp.com</base.url>
            <timeout>30</timeout>
        </properties>
    </profile>
    <profile>
        <id>prod</id>
        <properties>
            <base.url>https://myapp.com</base.url>
            <timeout>15</timeout>
        </properties>
    </profile>
</profiles>
```

```bash
mvn test -Pstaging      # Run with staging profile
mvn test -Pprod         # Run with production profile
```

**Playwright — environment config:**

```typescript
// playwright.config.ts
const ENV = process.env.APP_ENV ?? 'staging';

const baseURLs: Record<string, string> = {
    dev:     'https://dev.myapp.com',
    staging: 'https://staging.myapp.com',
    prod:    'https://myapp.com',
};

export default defineConfig({
    use: {
        baseURL: baseURLs[ENV] ?? baseURLs['staging'],
    },
});
```

**GitHub Actions — environment-specific jobs:**

```yaml
jobs:
  smoke-staging:
    name: Smoke Tests — Staging
    environment: staging        # GitHub Environment with staging secrets
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - run: npx playwright test --grep "@smoke"
        env:
          BASE_URL:     ${{ vars.BASE_URL }}           # From staging environment variables
          APP_PASSWORD: ${{ secrets.APP_PASSWORD }}    # From staging environment secrets

  smoke-production:
    name: Smoke Tests — Production
    environment:
      name: production
      url: https://myapp.com      # GitHub shows this URL in the deployment view
    runs-on: ubuntu-latest
    needs: smoke-staging
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - run: npx playwright test --grep "@smoke"
        env:
          BASE_URL:     ${{ vars.BASE_URL }}           # From production environment variables
          APP_PASSWORD: ${{ secrets.APP_PASSWORD }}    # From production environment secrets
```

**Jenkins — environment gate with manual approval:**

```groovy
stage('Production Smoke Tests') {
    when { branch 'main' }
    input {
        message 'Run smoke tests against production?'
        ok 'Yes, run'
        submitter 'qa-lead,release-manager'
        parameters {
            choice(name: 'CONFIRM', choices: ['yes', 'no'])
        }
    }
    environment {
        BASE_URL = 'https://myapp.com'
        APP_ENV  = 'prod'
    }
    steps {
        sh 'npx playwright test --grep "@smoke"'
    }
}
```

---

**Q31: Tests pass locally but fail in CI. How do you troubleshoot this?**

**A:**

This is one of the most common CI/CD problems. Work through this systematic checklist:

**Diagnosis step 1: Compare the environments**

| Factor | Local Machine | CI Container |
|---|---|---|
| Browser | Headed (visible) | Headless required |
| OS | Windows/macOS | Ubuntu/Linux |
| Browser version | Auto-updated | Fixed version |
| Screen resolution | High-DPI, windowed | No screen, virtual display |
| /dev/shm size | Large | Small (Docker default: 64MB) |
| Network speed | Fast | Slower (shared) |
| File paths | Absolute Windows paths | Linux paths |
| Environment variables | Set in .env locally | Must be explicitly set in CI |

**Fix: Headless browser flags (most common cause)**

```java
ChromeOptions options = new ChromeOptions();
options.addArguments("--headless=new");
options.addArguments("--no-sandbox");             // Required in Docker/CI
options.addArguments("--disable-dev-shm-usage");  // Prevents memory issues in Docker
options.addArguments("--window-size=1920,1080");   // Consistent viewport
options.addArguments("--disable-gpu");             // Not needed without display
```

**Fix: Browser not found in CI**

```bash
# Playwright — install browsers explicitly in the pipeline
npx playwright install chromium --with-deps
# --with-deps installs OS browser dependencies (libglib, libnss, etc.)

# Or use the official Playwright Docker image which has everything pre-installed
image: mcr.microsoft.com/playwright:v1.44.0-jammy
```

**Fix: Environment variables not set**

```bash
# In Jenkinsfile — debug what's available
sh 'env | sort'
sh 'echo "BASE_URL=${BASE_URL}"'

# In GitHub Actions
- run: env | sort
```

**Fix: Timing issues — CI is slower**

```java
// Increase timeouts for CI
int timeout = Integer.parseInt(System.getProperty("wait.timeout", "30"));
// Run CI with: mvn test -Dwait.timeout=60
```

```typescript
// playwright.config.ts
timeout: process.env.CI ? 60_000 : 30_000,  // 60s in CI; 30s locally
```

**Fix: File permission errors**

```bash
# Make build scripts executable
chmod +x gradlew mvnw

# Jenkins workspace permissions
sudo chown -R jenkins:jenkins /var/jenkins_home

# Docker — run as non-root user
USER node
```

**Fix: Out of memory**

```bash
export MAVEN_OPTS="-Xmx1g -XX:MaxMetaspaceSize=512m"
export NODE_OPTIONS="--max-old-space-size=4096"
```

**Fix: Artifacts not found**

```yaml
# GitHub Actions — ALWAYS use if: always() on upload steps
- uses: actions/upload-artifact@v4
  if: always()          # Without this, failed tests delete their reports
  with:
    name: playwright-report
    path: playwright-report/

# GitLab CI — set when: always on artifacts
artifacts:
  when: always          # Without this, artifacts are only collected on success
  paths:
    - playwright-report/
```

---

## SECTION 5: INTERVIEW Q&A

---

**Q32: What is the difference between CI and CD? (Interview question)**

**A:**

CI (Continuous Integration) automatically builds and tests code every time a developer commits. The goal is to catch integration problems early — if my change breaks your code, we find out within minutes, not days.

CD has two meanings: Continuous Delivery means every passing build is packaged and ready to deploy to production, but a human clicks the deploy button. Continuous Deployment removes even that human step — every passing build automatically goes to production.

In a QA interview context: CI is where our automated tests run. CD is the gate they protect. If tests fail in CI, the deployment is blocked. If tests pass, the build progresses to delivery or deployment.

---

**Q33: Why does QA need to understand CI/CD if developers set it up? (Interview question)**

**A:**

QA does not just run tests — QA owns the test automation that runs inside the pipeline. If tests are flaky, slow, or environment-dependent, QA is responsible for fixing them. QA must:

- Design tests that are headless and stateless so they work in CI containers
- Define which tests block the pipeline (smoke gate) versus which are informational
- Read and interpret pipeline results to distinguish genuine failures from environment issues
- Work with DevOps to configure the right pipeline stages and triggers
- Maintain the pipeline over time as test suites grow

A QA engineer who cannot read a Jenkinsfile or a GitHub Actions YAML is blocked from doing their job effectively in a modern team.

---

**Q34: How do you run tests only on pull requests in GitHub Actions, and full regression on main? (Interview question)**

**A:**

Use conditional logic with the `if:` key on jobs:

```yaml
jobs:
  pr-smoke:
    name: PR Smoke (Chromium only)
    runs-on: ubuntu-latest
    if: github.event_name == 'pull_request'
    steps:
      - uses: actions/checkout@v4
      - run: npm ci && npx playwright install chromium --with-deps
      - run: npx playwright test --project=chromium --grep "@smoke"

  full-regression:
    name: Full Regression (All Browsers)
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' || github.event_name == 'schedule'
    strategy:
      matrix:
        browser: [chromium, firefox, webkit]
    steps:
      - uses: actions/checkout@v4
      - run: npm ci && npx playwright install ${{ matrix.browser }} --with-deps
      - run: npx playwright test --project=${{ matrix.browser }}
```

This way, every PR gets fast smoke feedback (2–3 minutes). Only main branch and nightly schedules trigger the full cross-browser suite (15–20 minutes).

---

**Q35: What does `--no-sandbox` do in Chrome and why is it needed in CI? (Interview question)**

**A:**

Chrome uses a sandbox to provide security isolation between browser processes. Inside a Docker container (which is already isolated by the container runtime), Chrome tries to create kernel namespaces for its sandbox — this fails because containers typically do not have the required Linux capabilities.

Adding `--no-sandbox` tells Chrome to skip the sandbox creation step, which allows it to start inside a container. Without it, Chrome either silently crashes or throws an error about namespace creation.

You should also add `--disable-dev-shm-usage` because Docker containers have a small `/dev/shm` directory (64MB by default) which Chrome uses for inter-process memory sharing. Without this flag, Chrome runs out of shared memory and crashes on heavy pages.

These two flags together are the standard setup for Chrome in any Docker-based CI environment.

---

**Q36: What is a matrix strategy and when would you use it? (Interview question)**

**A:**

A matrix strategy runs the same job with different input values automatically. Instead of writing one job per browser, I define the browsers as a list and the CI system creates one job per value.

For QA, I use matrix when:
- Running Playwright tests across chromium, firefox, and webkit
- Running the same API tests against dev, staging, and production environments
- Running tests on multiple OS combinations (Ubuntu + Windows)

The key benefit is that all matrix jobs run in parallel, so 3 browsers take the same time as 1 browser. I always set `fail-fast: false` so that if Chromium fails, Firefox and WebKit still run to completion and I get a complete picture of the failure.

---

**Q37: How do you integrate JUnit XML test results into a Jenkins pipeline? (Interview question)**

**A:**

Maven Surefire generates JUnit XML files at `target/surefire-reports/TEST-*.xml`. I parse these in the Jenkins pipeline using the `junit` step from the JUnit Plugin. The results appear in the Jenkins UI as a test trend chart, pass/fail counts, and individual test details with stack traces.

```groovy
post {
    always {
        // Parse ALL XML files in surefire-reports directory
        junit allowEmptyResults: true,
              testResults: 'target/surefire-reports/**/*.xml'
        // allowEmptyResults: true prevents the step from failing if no XML is found
        // which happens when tests fail before Maven even runs them
    }
}
```

For Playwright, I configure the JUnit reporter in playwright.config.ts and point the `junit` step to `test-results/**/*.xml`.

The JUnit step also controls the build status: if tests fail (but the build compiles), Jenkins marks the build UNSTABLE (yellow) rather than FAILED (red). FAILED means the build itself broke; UNSTABLE means the build ran but tests failed.

---

**Q38: How would you handle a 45-minute test suite that is blocking the CI pipeline? (Interview question)**

**A:**

A 45-minute pipeline is too slow — developers will stop pushing frequently, which defeats CI. I would apply these strategies:

**1. Parallel execution** — split the suite across browsers or modules and run simultaneously. Three browsers in parallel = 3x speedup.

**2. Selective execution on PRs** — run only `@smoke` tagged tests on pull requests (5 minutes). Run full regression only on main branch or nightly.

**3. Caching** — cache Maven repository and node_modules. This alone can save 3–5 minutes per run.

**4. Fail-fast** — stop the pipeline immediately when smoke tests fail. There is no point running 45 minutes of regression if the core smoke tests are already broken.

**5. Test architecture** — review if tests are doing unnecessary setup, sleeping unnecessarily, or testing the same paths redundantly. A well-designed suite of 500 tests should run in under 15 minutes with parallelism.

**6. Test splitting** — tools like `@playwright/test` can split tests by file and run each group on a separate runner simultaneously.

**7. Infrastructure** — increase parallel workers on the agent, or switch to a faster runner type.

---

**Q39: What is the difference between Jenkins `post { failure }` and `post { unstable }`? (Interview question)**

**A:**

The `post { failure }` block runs when the pipeline itself fails — for example, a Maven compilation error, a shell command exits with a non-zero code, or a required tool is not found. This is a build failure, not just test failures.

The `post { unstable }` block runs when the build completed but the JUnit plugin found test failures in the parsed XML. Jenkins marks the build UNSTABLE (yellow) rather than FAILED (red). This distinction is important because:

- FAILED: the pipeline could not complete — something structural is broken
- UNSTABLE: the pipeline completed but tests reported failures — the test results need attention

In practice, I configure different notifications for each:
- `failure` — urgent Slack alert + email — something fundamental broke
- `unstable` — test failure report email — tests need investigation but the build process works

This helps the team triage quickly: a red build means CI is broken; a yellow build means test failures to investigate.

---

*Guide complete — covers Jenkins, GitLab CI/CD, GitHub Actions, environment-specific testing, CI-readiness, troubleshooting, and 8 interview questions with full answers.*
