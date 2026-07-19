# Git for QA Engineers — Complete Guide | Commands + Branching + PR Workflow

> Senior QA Interview Reference — Git fundamentals, daily commands, branching strategies, PR workflow, CI/CD integration, and 10 interview Q&As.

---

## Table of Contents

1. [Why QA Needs Git](#section-1--why-qa-needs-git)
2. [All Daily Git Commands for QA](#section-2--all-daily-git-commands-for-qa)
3. [Branching Strategies](#section-3--branching-strategies)
4. [Pull Request (PR) Workflow](#section-4--pull-request-pr-workflow)
5. [.gitignore for QA Projects](#section-5--gitignore-for-qa-projects)
6. [Git in CI/CD](#section-6--git-in-cicd)
7. [Common Git Scenarios for QA](#section-7--common-git-scenarios-for-qa)
8. [Interview Q&A](#section-8--interview-qa)

---

## SECTION 1 — Why QA Needs Git

### The Shift from Manual to Automation-First QA

Modern QA engineers are expected to be engineers — not just testers. Automated tests are code, and code needs version control. Git is not optional for a senior QA engineer; it is a core competency.

### Five Reasons QA Needs Git

**1. Version-controlled test code**
Test suites in Playwright, Selenium, Rest Assured, or Cypress live in a Git repository — either alongside the application code (monorepo) or in a dedicated test repository. Git tracks every change to test code: who added a test, who deleted an assertion, when a new test was introduced.

**2. Branching strategies and how QA fits in**
Tests must mirror the code they test. If a developer is building a feature on `feature/user-login`, the QA automation engineer writes the corresponding tests on the same branch (or a paired branch). Git branching ensures test code and application code stay synchronised through the development lifecycle.

**3. Code review of test code**
Test code quality matters. Poorly written tests (hardcoded waits, flaky assertions, no POM) degrade the test suite. QA engineers review each other's test code and application developers review test code in Pull Requests — this only works with Git.

**4. Git blame / git log to understand what changed**
When a test starts failing unexpectedly, the first question is: "what changed?" Git log and git blame let QA answer this in seconds — identifying the exact commit, author, and diff that introduced the change, whether in application code or test code.

**5. CI/CD pipeline integration**
Automated tests are triggered by Git events — a push to a branch, a PR raised, a tag created. QA must understand the Git workflow to configure and debug pipelines, interpret CI results, and enforce quality gates.

---

## SECTION 2 — All Daily Git Commands for QA

### Initial Setup

```bash
# Configure identity — required before any commit
git config --global user.name "Your Name"
git config --global user.email "email@example.com"

# Verify configuration
git config --list

# Set default branch name to 'main' (modern convention)
git config --global init.defaultBranch main

# Set preferred editor (VS Code example)
git config --global core.editor "code --wait"
```

---

### Cloning and Starting Work

```bash
# Clone a repository into a new directory
git clone <url>

# Clone a specific branch (useful for QA working on a feature branch)
git clone <url> --branch develop

# Clone with a custom directory name
git clone <url> my-test-project

# Clone only the latest commit (shallow clone — faster for large repos)
git clone <url> --depth 1

# Check current status — what has changed
git status

# View the last 10 commits in a compact format
git log --oneline -10

# View branch graph visually (useful to understand merges)
git log --oneline --graph --all

# View commits by a specific author
git log --oneline --author="Jane Smith"

# View commits that touched a specific file
git log --oneline -- src/test/java/LoginTest.java
```

---

### Working with Branches

```bash
# List all local branches
git branch

# List all branches including remote-tracking branches
git branch -a

# List remote branches only
git branch -r

# Create a new branch and switch to it (traditional syntax)
git checkout -b feature/login-tests

# Create a new branch and switch to it (modern syntax — Git 2.23+)
git switch -c feature/login-tests

# Switch to an existing branch (traditional)
git checkout main

# Switch to an existing branch (modern)
git switch main

# Switch to previous branch (the dash shortcut)
git switch -

# Rename a branch
git branch -m old-branch-name new-branch-name

# Delete a local branch (safe — only if merged)
git branch -d feature/login-tests

# Force delete a local branch (even if unmerged — be careful)
git branch -D feature/login-tests

# Delete a remote branch
git push origin --delete feature/login-tests

# List branches with their last commit info
git branch -v
```

---

### Making Changes and Committing

```bash
# Stage a specific file
git add filename.java

# Stage an entire folder
git add src/test/

# Stage all changed and new files (use carefully — can accidentally add .env or large files)
git add .

# Stage specific lines interactively (opens an interactive patch mode)
git add -p filename.java

# See what is staged and what is not
git status

# Commit staged changes with a message
git commit -m "Add login smoke tests for MYAPP-145"

# Commit with a multi-line message
git commit -m "Add login smoke tests

- Happy path: valid credentials
- Error path: invalid password shows error message
- AC #1 and AC #2 covered (MYAPP-145)"

# Stage and commit all tracked modified files in one step (does NOT add new untracked files)
git commit -am "Fix assertion in checkout test"

# Push branch to remote
git push origin feature/login-tests

# Push and set upstream tracking for the first time
git push -u origin feature/login-tests

# After setting upstream, subsequent pushes are just:
git push
```

---

### Keeping Your Branch Up to Date

```bash
# Fetch all remote changes without merging — safe way to check what exists remotely
git fetch origin

# Fetch and see what changed on remote main
git fetch origin && git log origin/main --oneline -5

# Pull remote main changes into your current branch (fetch + merge)
git pull origin main

# Pull with rebase instead of merge (cleaner history — preferred by many teams)
git pull --rebase origin main

# Merge main into your current feature branch (explicit merge commit)
git merge main

# Merge without fast-forward (always creates a merge commit)
git merge --no-ff main

# Rebase your current branch on top of main (linear history)
git rebase main

# Abort a rebase if it goes wrong
git rebase --abort

# Continue a rebase after resolving conflicts
git rebase --continue
```

---

### Reviewing and Inspecting Changes

```bash
# Show unstaged changes (working directory vs staging area)
git diff

# Show staged changes (staging area vs last commit)
git diff --staged

# Compare two branches
git diff main..feature/login-tests

# Compare two commits
git diff abc1234..def5678

# Compare a file between two branches
git diff main..feature/login-tests -- src/test/java/LoginTest.java

# Show the full details of a specific commit (diff + metadata)
git show <commit-hash>

# Show just the files changed in a commit
git show <commit-hash> --stat

# See who changed each line of a file and in which commit
git blame src/test/java/LoginTest.java

# See blame with commit date
git blame --date=short src/test/java/LoginTest.java

# Search the commit history for a specific string (e.g., when was a method added)
git log -S "forgotPassword" --oneline

# Show all commits that modified a specific file
git log --oneline -- pom.xml
```

---

### Fixing Mistakes

```bash
# Discard unstaged changes to a file (restore to last committed state)
git restore filename.java

# Discard ALL unstaged changes in the working directory
git restore .

# Unstage a file (remove from staging area but keep changes in working directory)
git restore --staged filename.java

# Unstage using older syntax
git reset HEAD filename.java

# Amend the last commit message (before pushing only — rewrites history)
git commit --amend -m "Corrected commit message"

# Add more changes to the last commit (before pushing)
git add forgotten-file.java
git commit --amend --no-edit

# Temporarily save uncommitted work (saves everything — staged and unstaged)
git stash

# Save stash with a descriptive message
git stash push -m "WIP: login test incomplete — working on error scenario"

# List all stashes
git stash list

# Apply the most recent stash and keep it in the stash list
git stash apply

# Apply the most recent stash and remove it from the stash list
git stash pop

# Apply a specific stash by index
git stash pop stash@{2}

# Drop a specific stash
git stash drop stash@{0}

# Clear all stashes
git stash clear

# Safely undo a commit that has already been pushed (creates a new reverting commit)
git revert <commit-hash>

# Revert the most recent commit safely
git revert HEAD

# Soft reset: undo last commit but keep changes staged (useful for recommitting)
git reset --soft HEAD~1

# Mixed reset: undo last commit and unstage changes (changes remain in working dir)
git reset HEAD~1

# Hard reset: undo last commit and DELETE the changes — DESTRUCTIVE
git reset --hard HEAD~1

# Recover a deleted branch or lost commit using reflog
git reflog
git checkout -b recovered-branch <commit-hash>
```

---

### Tagging Releases

```bash
# Create a lightweight tag
git tag v1.0.0

# Create an annotated tag (recommended — includes metadata)
git tag -a v1.0.0 -m "Release 1.0.0 — first stable release"

# Tag a specific past commit
git tag -a v1.0.0 <commit-hash> -m "Release 1.0.0"

# List all tags
git tag

# Push a specific tag to remote
git push origin v1.0.0

# Push all tags to remote
git push origin --tags

# Delete a local tag
git tag -d v1.0.0

# Delete a remote tag
git push origin --delete v1.0.0

# Checkout a tag (detached HEAD state — read only)
git checkout v1.0.0
```

---

## SECTION 3 — Branching Strategies

Understanding branching strategies is essential for QA — it determines which branch to test, when to test, and what constitutes a complete test cycle.

### GitFlow

The most structured branching model — widely used in enterprise teams with scheduled releases.

**Branches:**

| Branch | Purpose | Lifetime |
|---|---|---|
| `main` | Production-ready code, tagged releases | Permanent |
| `develop` | Integration branch — all features merge here | Permanent |
| `feature/*` | Individual feature development | Temporary |
| `release/*` | Release preparation — QA final sign-off | Temporary |
| `hotfix/*` | Emergency production fixes | Temporary |

**GitFlow lifecycle:**
```
main ──────────────────────────────────────────── v1.0 ──── v1.1
      \                                           /      \
develop ──────────────────────────────────────────        hotfix/login-fix
          \          /     \          /
     feature/login  /  feature/search
                   /
            release/1.0 (QA regression here)
```

**Where QA tests in GitFlow:**
- **Feature branch** — functional testing of the specific feature as it is developed
- **Develop branch** — integration testing when multiple features merge together
- **Release branch** — full regression testing, release sign-off, no new features allowed
- **Hotfix branch** — rapid targeted testing of the emergency fix and its immediate regression

---

### GitHub Flow

Simpler model — suited for teams with continuous delivery and frequent small releases.

**Branches:**

| Branch | Purpose |
|---|---|
| `main` | Always deployable; represents production |
| `feature/*` | All work happens in short-lived feature branches |

**GitHub Flow lifecycle:**
```
main ──────────────────────────────────────────────────────
        \              /     \              /
         feature/login         feature/search
         (PR raised → CI → QA review → merge)
```

**Where QA tests in GitHub Flow:**
- **Feature branch** — QA reviews the PR, checks test coverage, may manually test
- **CI pipeline** — automated tests run on every push to the feature branch
- **Post-merge to main** — smoke tests run to confirm production readiness
- QA must ensure every PR has sufficient test coverage before it is approved to merge

---

### Trunk-Based Development

The most modern and CI/CD-aligned approach — used by high-velocity teams with strong automation.

**Branches:**

| Branch | Purpose |
|---|---|
| `main` / `trunk` | All developers commit here directly (or very short-lived branches) |
| Feature flags | Code is deployed but hidden behind a flag until ready |

**Trunk-based lifecycle:**
```
main ────commit──commit──commit──commit──commit──────
          (CI runs on every commit)
          (feature flags control visibility)
```

**Where QA tests in Trunk-Based:**
- Testing happens continuously in CI — every commit triggers the full test suite
- QA focuses on test coverage in CI rather than manual milestone testing
- Feature flags allow testing new features before they are visible to users
- QA validates feature flag behaviour (on/off states) as part of testing
- Production monitoring and canary deployments are part of the quality strategy

---

### Comparison Table

| Aspect | GitFlow | GitHub Flow | Trunk-Based |
|---|---|---|---|
| Complexity | High | Low | Medium |
| Release cadence | Scheduled releases | Continuous | Continuous |
| QA testing phase | Release branch | Feature PR | Every commit in CI |
| Branch lifespan | Feature: days/weeks | Feature: days | Hours to 1–2 days |
| Best for | Enterprise, versioned software | Startups, SaaS | High-velocity, mature CI |
| Merge conflicts | More frequent (long-lived branches) | Less frequent | Minimal |
| Automation requirement | Moderate | High | Very High |

---

## SECTION 4 — Pull Request (PR) Workflow

### What is a Pull Request?

A Pull Request (PR) or Merge Request (MR in GitLab) is a formal request to merge code from one branch into another. For QA, the PR is a quality gate — a structured checkpoint before code enters the main codebase.

### What QA Does When a PR is Raised

**Step 1 — Review the PR description**
- Does the PR description reference the JIRA ticket?
- Is there a description of what changed and why?
- Are there any known risks or areas that need extra attention?

**Step 2 — Check that tests were added or updated**
- Every feature change should come with new or updated automated tests
- Every bug fix should come with a test that would have caught the bug
- No test additions for a functional change is a red flag — comment on the PR

**Step 3 — Review the test code quality**
```
Quality checks in test code review:

Assertions:
  - Are assertions meaningful? (assert response.statusCode == 200, not assert true)
  - Do assertions validate the actual business requirement?
  - Are there assertions for both positive and negative cases?

Wait strategies:
  - No Thread.sleep() / page.waitForTimeout() hardcoded sleeps
  - Using Playwright's built-in auto-waiting or explicit waits
  - Dynamic waits only (waitForSelector, waitForResponse)

Test structure:
  - Is the Page Object Model (POM) followed?
  - Are test data hardcoded in test methods? (should be externalised)
  - Is setup/teardown clean (@BeforeEach / @AfterEach properly used)?

Readability:
  - Are test method names descriptive? (shouldRedirectToHomePageOnValidLogin vs test1)
  - Are comments present for complex logic?
  - Is there code duplication that should be extracted?
```

**Step 4 — Manual testing of the PR branch (if needed)**
For complex features or high-risk changes:
```bash
# Fetch and checkout the PR branch
git fetch origin
git checkout feature/login-tests
# or using PR number with GitHub CLI
gh pr checkout 42
```

**Step 5 — Add test evidence as PR comments**
Always comment with test evidence:
```
QA Review:

Manual test executed on branch `feature/login-tests`:
- Chrome 124 / MacOS 14.3 — PASS
- Firefox 125 / Windows 11 — PASS
- Safari 17 / iOS 17 (device) — PASS

Automated tests: All 47 tests passed
Zephyr Cycle: Sprint 24 QA Cycle — TC-003 PASS
Attached: test_run_20240415.html
```

### Merge Strategies

| Strategy | Description | When to Use |
|---|---|---|
| **Merge Commit** | Creates a merge commit preserving full branch history | GitFlow, when branch history is important |
| **Squash Merge** | Squashes all commits into one clean commit | GitHub Flow, keeping main history clean |
| **Rebase Merge** | Replays commits on top of target branch — linear history | Teams valuing clean linear history |

**QA perspective on merge strategies:**
- Squash merges make `git log` on main easier to read
- Merge commits are easier to revert (revert the merge commit)
- Rebase can make `git blame` more accurate (each commit stands alone)
- Ensure your team agrees on one strategy — inconsistent merging creates confusion

### Branch Protection Rules (Enforced by QA + DevOps)

Recommended protections for main/develop branches:
- Require at least one Pull Request review before merging
- Require status checks (CI tests) to pass before merging
- Require the branch to be up to date before merging
- Prevent force pushing to protected branches
- Require linear history (no merge commits directly to main)

QA should advocate for these protections — they enforce the quality gate automatically.

---

## SECTION 5 — .gitignore for QA Projects

A `.gitignore` file tells Git which files and folders to ignore — never track, never commit. For QA automation projects, this is critical to avoid committing credentials, generated reports, and IDE-specific files.

### .gitignore for a QA Automation Project

```gitignore
# ============================================================
# BUILD OUTPUT
# ============================================================
target/                     # Maven build output (Java projects)
build/                      # Gradle build output
out/                        # IntelliJ IDEA output
*.class                     # Compiled Java bytecode
*.jar                       # Built JAR files (unless intentionally versioned)

# ============================================================
# NODE / NPM (Playwright, Cypress, etc.)
# ============================================================
node_modules/               # npm packages — never commit, too large
npm-debug.log*
yarn-debug.log*
yarn-error.log*
package-lock.json           # Optional: some teams commit this, some don't

# ============================================================
# CREDENTIALS AND ENVIRONMENT — CRITICAL
# ============================================================
.env                        # Environment variables — NEVER commit
.env.local
.env.production
.env.*.local
*.properties.local          # Local property overrides
secrets.json                # Any file with credentials
credentials.json            # Google API credentials, etc.
serviceAccountKey.json      # Firebase service accounts

# ============================================================
# PLAYWRIGHT OUTPUT (Generated — not source)
# ============================================================
test-results/               # Playwright test results directory
playwright-report/          # Generated HTML report
.playwright/                # Playwright cache
blob-report/                # Playwright blob reports

# ============================================================
# ALLURE REPORTING
# ============================================================
allure-results/             # Raw Allure result files (generated)
allure-report/              # Generated Allure HTML report

# ============================================================
# SCREENSHOTS AND RECORDINGS (Generated during test runs)
# ============================================================
screenshots/                # Runtime screenshots from failed tests
videos/                     # Playwright/Cypress video recordings
downloads/                  # Files downloaded during tests

# ============================================================
# LOGS
# ============================================================
*.log                       # All log files
logs/                       # Log directory
test-output/                # TestNG output directory

# ============================================================
# IDE FILES
# ============================================================
.idea/                      # IntelliJ IDEA project files
*.iml                       # IntelliJ module files
.vscode/                    # VS Code workspace settings (debated — some teams commit)
.eclipse/                   # Eclipse project files
*.classpath
*.project
.settings/

# ============================================================
# OS-SPECIFIC FILES
# ============================================================
.DS_Store                   # macOS filesystem metadata
.DS_Store?
._*
Thumbs.db                   # Windows image cache
ehthumbs.db

# ============================================================
# TEMPORARY FILES
# ============================================================
*.tmp
*.temp
*.swp                       # Vim swap files
*.bak                       # Backup files
~$*                         # Office temporary files

# ============================================================
# DOCKER
# ============================================================
.docker/                    # Local Docker overrides (if sensitive)

# ============================================================
# WHAT TO ALWAYS COMMIT (do not ignore these)
# ============================================================
# src/                      — All source test code
# pom.xml / build.gradle    — Build configuration
# playwright.config.ts      — Playwright configuration
# .github/workflows/        — CI/CD pipeline definitions
# testdata/                 — Static test data (if not containing credentials)
# .gitignore itself         — Always commit this
```

### How to Check if Sensitive Files Were Accidentally Staged

```bash
# Before committing, always check what is being staged
git status
git diff --staged

# Check if .env is tracked
git ls-files .env

# If .env was accidentally committed, remove it from tracking (without deleting the file)
git rm --cached .env
git commit -m "Remove accidentally tracked .env file"
# Then rotate all secrets — assume they were exposed

# Check the entire staging area for anything suspicious
git diff --staged --stat
```

---

## SECTION 6 — Git in CI/CD

### How CI/CD Pipelines React to Git Events

```
Git Event                    →  CI Pipeline Trigger
─────────────────────────────────────────────────────────
git push (feature branch)    →  Run unit tests + linting
git push (main branch)       →  Full test suite + deployment to staging
Pull Request raised          →  Run tests, post results as PR status check
git tag v1.0.0               →  Build release artifact + deploy to production
PR merged to main            →  Deploy to staging, run smoke tests
Scheduled (nightly)          →  Full regression suite on latest main
```

### GitHub Actions Example — QA Test Pipeline

```yaml
# .github/workflows/qa-tests.yml
name: QA Automation Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'

      - name: Install dependencies
        run: npm ci

      - name: Install Playwright browsers
        run: npx playwright install --with-deps

      - name: Run Playwright tests
        run: npx playwright test
        env:
          BASE_URL: ${{ secrets.QA_BASE_URL }}
          TEST_USERNAME: ${{ secrets.TEST_USERNAME }}
          TEST_PASSWORD: ${{ secrets.TEST_PASSWORD }}

      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
          retention-days: 30
```

### Jenkins Pipeline Example

```groovy
// Jenkinsfile
pipeline {
    agent any
    
    triggers {
        // Run on every push (via GitHub webhook)
        githubPush()
    }
    
    stages {
        stage('Checkout') {
            steps { checkout scm }
        }
        
        stage('Install') {
            steps { sh 'npm ci' }
        }
        
        stage('Run Tests') {
            steps {
                sh 'npx playwright install --with-deps'
                sh 'npx playwright test --reporter=html'
            }
        }
    }
    
    post {
        always {
            publishHTML([
                reportDir: 'playwright-report',
                reportFiles: 'index.html',
                reportName: 'Playwright Test Report'
            ])
        }
        failure {
            // Notify Slack or email on failure
            echo 'Tests failed — notify QA team'
        }
    }
}
```

### Branch Protection Rules as QA Gates

The ideal QA gate flow for every PR:

```
1. Developer pushes to feature/my-feature
2. CI pipeline runs automatically:
   - Unit tests
   - Integration tests
   - Automated E2E tests
3. All checks must be GREEN (branch protection enforces this)
4. QA engineer reviews the PR:
   - Checks test coverage
   - Reviews test code quality
   - May manually test on the branch
5. QA approves the PR
6. Developer merges (squash merge to keep history clean)
7. CI runs again on main/develop after merge
8. Smoke tests verify the merge did not break anything
```

**Branch protection settings for main branch:**
- Require pull request reviews: minimum 1 (QA approval)
- Require status checks to pass: all CI jobs must be green
- Require branches to be up to date: prevent stale branch merges
- Restrict who can push directly: developers must use PRs (including QA leads)

### Environment-Specific Testing via Branches and Tags

| Branch/Tag | Environment | Test Suite |
|---|---|---|
| `feature/*` (PR) | Preview / PR environment | Feature-specific tests + smoke |
| `develop` | Development / QA environment | Full regression suite |
| `release/x.x.x` | Staging environment | Full regression + UAT |
| `main` (post-merge) | Staging | Smoke tests |
| `v1.0.0` (tag) | Production | Post-deployment smoke tests |

---

## SECTION 7 — Common Git Scenarios for QA

### Scenario 1 — "Tests were passing yesterday, failing today"

```bash
# Step 1: Find what changed recently
git log --oneline -20

# Step 2: Find what changed in the test file
git log --oneline -- src/test/java/LoginTest.java

# Step 3: Find what changed in the application code (separate repo or same repo)
git log --oneline --since="yesterday"

# Step 4: Compare yesterday vs today for a specific file
git diff HEAD~5 -- src/main/java/LoginController.java

# Step 5: Identify the exact commit that changed behaviour
git bisect start
git bisect bad HEAD              # Current commit is bad
git bisect good v2.3.0          # This version was good
# Git will checkout commits for you to test until the culprit is found
git bisect good / git bisect bad
git bisect reset                 # When done
```

### Scenario 2 — "Need to test a colleague's branch"

```bash
# Step 1: Fetch all remote branches (no local changes made)
git fetch origin

# Step 2: Check out their branch locally
git checkout -b colleagues-branch origin/feature/payment-refactor

# Alternative: modern syntax
git switch --track origin/feature/payment-refactor

# Using GitHub CLI (most convenient)
gh pr checkout 47

# Step 3: After testing, return to your branch
git switch main
# Or switch back to your own feature branch
git switch feature/my-tests
```

### Scenario 3 — "Accidentally committed .env file"

```bash
# Step 1: Remove the file from Git tracking (but keep it on disk)
git rm --cached .env

# Step 2: Add .env to .gitignore immediately
echo ".env" >> .gitignore

# Step 3: Commit the removal
git commit -m "Remove accidentally tracked .env file and add to .gitignore"

# Step 4: Push the fix
git push origin <branch-name>

# CRITICAL: Rotate all secrets in the .env file
# Assume the credentials were exposed the moment they were pushed — even privately
# Change all API keys, passwords, tokens immediately
# Notify your security team

# Step 5: If this was pushed to a public repo, the history contains the credentials
# You must rewrite history (requires force push — coordinate with team)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch .env" \
  --prune-empty --tag-name-filter cat -- --all
git push origin --force --all
# Note: all team members must re-clone after a force push of history rewrite
```

### Scenario 4 — "My branch is behind main"

```bash
# Step 1: Fetch the latest remote changes
git fetch origin

# Step 2: See how far behind you are
git log HEAD..origin/main --oneline

# Step 3: Option A — Merge main into your branch (creates a merge commit)
git merge origin/main

# Step 3: Option B — Rebase your branch on top of main (linear history)
git rebase origin/main

# If rebase causes conflicts:
# 1. Fix the conflicts in the affected files
# 2. Stage the resolved files
git add resolved-file.java
# 3. Continue the rebase
git rebase --continue
# 4. Or abort if it is getting complicated
git rebase --abort

# Step 4: Push your updated branch
git push origin feature/my-tests
# If rebased, you may need to force push (only safe on your own branch)
git push --force-with-lease origin feature/my-tests
# --force-with-lease is safer than --force — fails if someone else pushed to the branch
```

### Scenario 5 — "Need to undo last commit"

```bash
# SAFE — Undo commit but keep the changes in working directory (uncommitted)
git reset --soft HEAD~1
# Use when: you committed too early and want to restructure the commit

# SAFE — Undo commit and unstage (changes remain but not staged)
git reset HEAD~1
# Use when: you need to re-examine what you committed

# UNSAFE (DESTRUCTIVE) — Undo commit and DELETE all changes
git reset --hard HEAD~1
# Use only when: you want to completely discard the last commit and its changes
# WARNING: Changes cannot be recovered after this (unless in reflog within 30 days)

# SAFEST — Revert: undo by creating a new commit (keeps history intact)
git revert HEAD
git revert <commit-hash>
# Use when: the commit has already been pushed and others may have pulled it
# This is the ONLY safe option for reverts on shared branches

# Undo a pushed commit without force (safe for shared branches)
git revert <commit-hash>
git push origin <branch>
```

---

## SECTION 8 — Interview Q&A

### Q1. What is the difference between git fetch and git pull?

`git fetch` downloads changes from the remote repository into your local remote-tracking branches (like `origin/main`) but does NOT modify your working directory or current branch. It is safe — nothing in your local work is affected.

`git pull` is `git fetch` followed by a `git merge` (or `git rebase` if configured). It downloads AND integrates the remote changes into your current branch. This can cause merge conflicts.

When to use which:
- Use `git fetch` when you want to see what has changed on the remote before deciding whether to integrate it
- Use `git fetch` + inspect + then `git merge` or `git rebase` for more control
- Use `git pull` when you are confident you want the latest changes immediately

As a QA rule of thumb: `git fetch` before testing a colleague's branch or before rebasing — it keeps you informed without surprising you.

---

### Q2. How do you find what changed between two commits?

```bash
# Compare the diff between two commits (all files)
git diff abc1234..def5678

# Show only the file names that changed
git diff abc1234..def5678 --stat

# Compare the current branch with main (useful to see what your PR changes)
git diff main..HEAD

# Show what changed in a specific file between two commits
git diff abc1234..def5678 -- src/main/java/LoginController.java

# Show the commit log between two versions
git log v2.3.0..v2.4.0 --oneline

# Show commits by a specific author between versions
git log v2.3.0..v2.4.0 --oneline --author="Jane Smith"
```

As a QA engineer, I use this to understand the scope of changes before a test cycle — I compare the release tag with the previous release to see exactly what was modified. This informs which test cases need to be executed.

---

### Q3. What is a merge conflict and how do you resolve it?

A merge conflict occurs when two branches have made changes to the same part of the same file, and Git cannot automatically determine which change to keep. Git stops the merge and asks you to resolve it manually.

Conflict markers in a file:
```
<<<<<<< HEAD (your current branch)
def test_login_valid():
    assert response.status_code == 200
=======
def test_login_valid():
    assert response.status_code == 200
    assert response.json()['token'] is not None
>>>>>>> feature/login-enhancement (incoming branch)
```

Resolution process:
1. Open the conflicted file(s) — `git status` shows them marked as "both modified"
2. Edit the file to keep the correct version (may be yours, theirs, or a combination)
3. Remove the conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`)
4. Stage the resolved file: `git add filename`
5. Complete the merge: `git commit`

Prevention strategies:
- Merge main into your branch frequently (small conflicts are easier than large ones)
- Communicate with teammates when working on the same files
- Keep feature branches short-lived — the longer they live, the higher the chance of conflicts

---

### Q4. What is git rebase and when would you use it over merge?

`git rebase` moves or replays your commits on top of another branch, as if you had started your work from the latest state of that branch. The result is a linear, clean commit history with no merge commits.

```
Before rebase:
main:    A──B──C
feature:    D──E

After: git rebase main (on feature branch)
main:    A──B──C
feature:         D'──E'  (D and E replayed on top of C)
```

`git merge` creates a merge commit that ties the two histories together:
```
After: git merge main (on feature branch)
feature: A──B──C──D──E──M (M = merge commit)
                   └──/
```

Use rebase when:
- You want a clean, linear history without merge commits
- You are the only person working on the feature branch (rebase rewrites history — risky on shared branches)
- Your team follows a "clean history" convention

Use merge when:
- The branch is shared with other team members
- You want to preserve the full historical context of when branches diverged
- You are merging into a protected branch via PR (merge commits are standard here)

QA note: Never rebase a branch that has been pushed and others have pulled — it rewrites commit hashes and causes serious confusion.

---

### Q5. How do you ensure sensitive test data does not get committed?

Three layers of protection:

**Layer 1 — .gitignore**
Add all environment and credential files to `.gitignore` before writing any sensitive data:
```
.env
*.env
credentials.json
secrets.yaml
```

**Layer 2 — Git hooks (pre-commit hook)**
A pre-commit hook automatically scans for secrets before allowing a commit:
```bash
# .git/hooks/pre-commit
#!/bin/bash
if git diff --cached --name-only | xargs grep -l "password\|secret\|api_key" 2>/dev/null; then
  echo "Potential secret detected — commit blocked. Use environment variables instead."
  exit 1
fi
```

**Layer 3 — Secret scanning tools**
- `git-secrets` (AWS): Scans commits for AWS credentials
- `truffleHog`: Scans for high-entropy strings (likely secrets)
- GitHub's built-in secret scanning: Alerts if known secret patterns are pushed

**Best practice for test credentials:**
- Store credentials in environment variables (`.env` file locally, CI/CD secrets in GitHub Secrets / Jenkins credentials)
- Never hardcode usernames, passwords, or tokens in test code
- Use placeholder constants that are loaded from the environment:
  ```java
  String username = System.getenv("TEST_USERNAME");
  String password = System.getenv("TEST_PASSWORD");
  ```

---

### Q6. What is git stash and when is it useful?

`git stash` temporarily saves your uncommitted work (both staged and unstaged changes) and restores the working directory to a clean state, matching the last commit. Your changes are saved in a stash stack and can be retrieved later.

**When QA uses stash:**

1. **Urgently need to test another branch** — you are mid-way through writing a test when a P1 bug is reported and you need to switch branches immediately:
   ```bash
   git stash push -m "WIP: halfway through writing checkout test"
   git checkout hotfix/payment-crash
   # Test the hotfix
   git checkout feature/checkout-tests
   git stash pop
   ```

2. **Pulling latest changes** — your working directory has changes but you need to pull before you can:
   ```bash
   git stash
   git pull origin main
   git stash pop
   ```

3. **Experimenting** — you want to try an approach but are not ready to commit:
   ```bash
   git stash         # Save current state
   # Try a different approach
   git stash pop     # Restore if the experiment failed
   ```

Important: `stash pop` can cause conflicts if the files changed in the meantime. `git stash apply` applies but keeps the stash, so you can retry if there is a conflict.

---

### Q7. How does QA integrate with the PR / code review process?

QA plays two roles in the PR process:

**Role 1 — Reviewing PRs submitted by developers:**
- Verify that test code was added or updated alongside the feature code
- Review test code for quality: meaningful assertions, no hardcoded sleeps, POM followed
- Ensure the PR description references the JIRA ticket and Acceptance Criteria
- Comment on missing test coverage or flawed assertions
- Approve only when tests are sufficient and the feature change is testable

**Role 2 — Submitting test code PRs:**
- Open a PR for any changes to the test suite (new tests, page objects, helpers)
- Provide clear description: which stories the tests cover, how to run them
- Link to the JIRA test execution results or CI run
- Request review from a senior QA or developer familiar with the area

The PR process ensures test code has the same quality bar as application code. I advocate that test PRs go through the same review process — no merging of test code without at least one review.

---

### Q8. What is git blame and when do you use it as a QA?

`git blame <file>` shows each line of a file annotated with the commit hash, author, and date that last modified that line.

```bash
git blame src/main/java/LoginController.java
# Output:
# abc1234 (Jane Smith  2024-04-10 14:32:01) public Response login(String email, String password) {
# def5678 (John Dev    2024-04-12 09:15:44)     if (password.length() < 8) {
# abc1234 (Jane Smith  2024-04-10 14:32:01)         return Response.badRequest("Too short");
```

**QA uses git blame when:**

1. **A test starts failing and you need to know who changed the application code** — blame the relevant controller or service file to find the most recent author and discuss the regression with them directly

2. **You find a behaviour that contradicts the spec and want to know who made the change and when** — blame + the commit hash gives you the full context

3. **Investigating a production bug** — blame the production code to understand when and why a specific line was introduced (useful during root cause analysis)

4. **Understanding unfamiliar code before testing** — see who wrote a particular section; that person becomes your first point of contact for questions

Combine with git show to get the full diff and commit message for that specific change:
```bash
git show abc1234  # Shows the full commit that introduced that line
```

---

### Q9. How do you roll back a bad commit that has already been pushed?

There are two approaches — the choice depends on whether others have already pulled the commit.

**Approach 1 — git revert (ALWAYS SAFE — preferred for shared branches)**
Creates a new commit that undoes the changes. History is preserved.

```bash
# Revert the most recent commit
git revert HEAD

# Revert a specific commit by hash
git revert abc1234

# Push the revert commit
git push origin main
```

This is the only safe approach when the commit is on a shared branch (develop, main) and others may have already pulled it.

**Approach 2 — git reset + force push (DESTRUCTIVE — only for your own branches)**
Rewrites history — removes the commit entirely.

```bash
# Reset to before the bad commit (keeps changes in working dir)
git reset HEAD~1

# Or completely discard
git reset --hard HEAD~1

# Force push (will break anyone else who has pulled this branch)
git push --force-with-lease origin feature/my-branch
```

Use `--force-with-lease` instead of `--force` — it refuses to push if someone else has pushed to the branch since you last fetched, preventing accidental overwrite of others' work.

**Rule of thumb:** On `main`, `develop`, or any shared branch — always use `git revert`. Reserve `git reset` + force push for your own private feature branches only, and communicate with the team before doing so.

---

### Q10. What branching strategy would you recommend for a team running automated tests?

My recommendation depends on the team's maturity and release cadence:

**For most modern teams: GitHub Flow + Trunk-Based Development hybrid**

Structure:
- Short-lived feature branches (1–3 days maximum)
- PR required to merge to main/develop
- CI runs the full automated test suite on every PR
- Branch protection enforces: CI must pass + QA review required

Why this works well for QA automation:
1. **Short branches reduce conflict** — test code stays close to application code; merge conflicts are minimal
2. **CI gate is the automated test gate** — tests run automatically without QA manually triggering them
3. **Every merge is tested** — no features land without passing the test suite
4. **Fast feedback** — developers know within minutes if their change broke existing tests

**For enterprise with versioned releases: GitFlow**
- QA regression testing concentrated on the `release/*` branch
- Feature branch testing for individual feature validation
- Hotfix branches for emergency production fixes with targeted regression

**Automation strategy across branches:**

| Branch | Test Level | Execution |
|---|---|---|
| `feature/*` (PR) | Smoke + feature-specific | On every push (fast, 5–10 min) |
| `develop` | Full regression | On merge + nightly |
| `release/*` | Full regression + UAT | On every push to release |
| `main` | Smoke | Post-merge |
| Production (tag) | Smoke | Post-deployment |

The key principle: the faster the pipeline, the more frequently it runs. Keep the PR-level suite fast (smoke + targeted) and reserve the full regression for develop/release branches where time is less critical.

---

*End of Guide — Git for QA Engineers*
