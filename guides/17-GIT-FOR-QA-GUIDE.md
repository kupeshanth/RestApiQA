# Git for QA Engineers — Complete Interview Q&A Guide

> Senior QA Interview Reference — Every concept answered as a real interview question. Git fundamentals, daily commands, branching strategies, PR workflow, CI/CD integration, secrets management, and 10 dedicated interview Q&As.

---

## SECTION 1 — Why QA Engineers Need Git

---

**Q1: Why do QA engineers need to know Git? Is it not just for developers?**

**A:** This is one of the most common misconceptions about the QA role. Git is not optional for a senior QA engineer — it is a core competency. Here are five concrete reasons:

**1. Test code is code — it needs version control.**
Automated test suites in Playwright, Selenium, Rest Assured, or Cypress live in a Git repository alongside or adjacent to application code. Git tracks every change: who added a test, who removed an assertion, who introduced a flaky test, and when it happened. Without Git, there is no accountability or history for test code.

**2. Branching strategies determine where and when QA tests.**
In GitFlow, QA may be testing on a `release/*` branch. In GitHub Flow, QA reviews the feature branch PR. In trunk-based development, QA validates test coverage on every commit through CI. Understanding the branching model tells you exactly where your test environment lives and what you are testing against.

**3. Code review of test code.**
Test quality matters — hardcoded waits, flaky assertions, missing page objects, and duplicated logic degrade the suite. QA engineers review each other's test code and receive reviews from developers. Pull requests are the mechanism for this. Without Git, there is no code review.

**4. Git blame and git log to investigate test failures.**
When a test that was green for two weeks suddenly fails, the first question is: "what changed?" `git log` and `git blame` let you find the exact commit, author, and diff that caused the regression — in application code or test code — in under a minute.

**5. CI/CD pipeline integration.**
Tests are triggered by Git events: a push to a branch, a PR raised, a tag created. QA must understand the Git workflow to configure pipelines, interpret CI results, manage test environments per branch, and enforce quality gates through branch protection rules.

*Common mistake in interviews:* Saying "I only use Git to clone and push." Senior QA engineers use blame, log, diff, stash, rebase, cherry-pick, and they understand branching models deeply.

---

**Q2: How do you set up Git for the first time on a new machine?**

**A:** Before you can make any commit, Git needs to know who you are. These are the one-time setup commands:

```bash
# Set your identity — required before any commit
git config --global user.name "Your Name"
git config --global user.email "your.email@company.com"

# Set the default branch name to 'main' (modern convention, avoids 'master')
git config --global init.defaultBranch main

# Set your preferred editor (VS Code shown here)
git config --global core.editor "code --wait"

# Verify your configuration
git config --list

# See where config is stored
git config --list --show-origin
```

The `--global` flag applies to all repositories on your machine. You can override per-repository with `--local` inside the repo directory. This matters when you use different email addresses for work vs. personal projects.

---

## SECTION 2 — Core Daily Commands

---

**Q3: What is git clone? How do you clone a specific branch?**

**A:** `git clone` copies an entire remote repository to your local machine, including all branches, commits, and history. By default it clones the default branch (usually `main`).

```bash
# Clone the default branch
git clone https://github.com/company/my-app-tests.git

# Clone a specific branch (useful when QA is working on a feature branch)
git clone https://github.com/company/my-app-tests.git --branch develop

# Clone with a custom local folder name
git clone https://github.com/company/my-app-tests.git qa-automation

# Shallow clone — only the most recent commit (faster for large repos in CI)
git clone https://github.com/company/my-app-tests.git --depth 1

# Clone and immediately enter the directory
git clone https://github.com/company/my-app-tests.git && cd my-app-tests
```

When would a QA engineer use `--branch`? When the develop or staging branch is where tests run, cloning `--branch develop` saves an extra `git checkout develop` step. Shallow clones with `--depth 1` are common in CI pipelines where full history is not needed — they are significantly faster on large repos.

---

**Q4: What is git status? What does each status mean?**

**A:** `git status` is the most frequently used Git command. It shows the current state of your working directory and staging area.

```bash
git status
```

The output has three sections:

**1. Changes staged for commit** (already in the staging area — will go into the next commit):
```
Changes to be committed:
  modified:   src/test/LoginTest.java
  new file:   src/test/CheckoutTest.java
  deleted:    src/test/OldTest.java
```

**2. Changes not staged for commit** (modified but not yet added with `git add`):
```
Changes not staged for commit:
  modified:   src/pages/LoginPage.java
```

**3. Untracked files** (new files Git has never seen — not staged, not committed):
```
Untracked files:
  testdata/users.json
  playwright-report/
```

*Key insight:* A file can appear in both "not staged" and "staged" if you modified it, staged it, then modified it again. The staged version is what will be committed; the extra change is unstaged.

*Practical habit:* Always run `git status` before committing. It prevents accidentally committing generated files, reports, or `.env` files that should not be tracked.

---

**Q5: What is git log? How do you view recent commits effectively?**

**A:** `git log` shows the commit history. The default output is verbose — use flags to make it readable.

```bash
# Default — full output (hash, author, date, message)
git log

# Most useful format — compact one-liner per commit
git log --oneline -10

# Visual branch graph — shows merges and branches
git log --oneline --graph --all

# Filter by author — useful when investigating who changed something
git log --oneline --author="Jane Smith"

# Filter by date
git log --oneline --since="2024-04-01" --until="2024-04-30"

# Show commits that touched a specific file — critical for QA debugging
git log --oneline -- src/test/java/LoginTest.java

# Show commits whose message matches a pattern
git log --oneline --grep="MYAPP-145"

# Show the most recent commit in full detail
git show HEAD
```

As a QA engineer, my most used combination is:

```bash
git log --oneline --since="yesterday"
```

When a test fails, this immediately shows me what changed since the last known-good state. Combined with `git log --oneline -- <file>` for a specific file, I can narrow down the culprit commit in under a minute.

---

**Q6: What is a branch in Git? How do you create and switch to one?**

**A:** A branch is a lightweight movable pointer to a specific commit. When you create a branch, you create a new line of development that diverges from the current branch. Branches are extremely cheap in Git — they are just a pointer, not a copy of the files.

```bash
# List all local branches (* marks your current branch)
git branch

# List all branches including remote-tracking branches
git branch -a

# Create a new branch and switch to it — traditional syntax
git checkout -b feature/login-tests

# Create and switch — modern syntax (Git 2.23+)
git switch -c feature/login-tests

# Switch to an existing branch — traditional
git checkout main

# Switch to an existing branch — modern
git switch main

# Quick switch back to previous branch
git switch -

# Rename a branch
git branch -m old-name new-name

# Delete a local branch (safe — refuses if not fully merged)
git branch -d feature/login-tests

# Force delete even if not merged — use with caution
git branch -D feature/login-tests

# Show branches with last commit info
git branch -v
```

*QA naming conventions for branches:*
```
feature/MYAPP-145-login-tests     # feature + ticket reference
fix/MYAPP-201-checkout-assertion  # bug fix in tests
chore/update-playwright           # dependency update
```

Consistent naming makes it easy to identify what branch corresponds to which Jira ticket and whether it is a new test, a fix, or a maintenance task.

---

**Q7: How do you push a new branch to remote for the first time?**

**A:** When you create a branch locally and want to share it with your team (or have it picked up by CI), you need to push it to the remote repository and set up tracking:

```bash
# Push and set up upstream tracking (first time)
git push -u origin feature/login-tests

# The -u flag sets the upstream — after this, you can just type:
git push

# Push without setting upstream (also works, but you'll need the full command each time)
git push origin feature/login-tests

# Verify the remote tracking is set
git branch -vv
```

The `-u` (or `--set-upstream`) flag creates a tracking relationship between your local branch and the remote branch. Once set, `git push`, `git pull`, and `git status` all know which remote branch to compare against.

*Common mistake:* Forgetting `-u` on the first push. Then `git push` gives an error: "fatal: The current branch has no upstream branch." The fix is to run the full command with `-u` once.

---

**Q8: What is the difference between git add specific-file vs git add .?**

**A:** Both stage changes, but they differ in scope — and the wrong choice can cause serious problems.

```bash
# Stage a specific file — precise and intentional
git add src/test/java/LoginTest.java

# Stage a whole folder
git add src/test/java/

# Stage ALL changes in the working directory — use with caution
git add .

# Stage interactively — choose specific lines/hunks within a file
git add -p src/test/java/LoginTest.java
```

**Why `git add .` is risky in a QA automation project:**
- It can accidentally stage `playwright-report/` (500MB of HTML)
- It can accidentally stage `.env` containing passwords
- It can accidentally stage `allure-results/` (generated artifacts)
- It can accidentally stage IDE files like `.idea/` or `.classpath`

**Best practice:**
1. Have a complete `.gitignore` file so generated files are always excluded
2. Use `git add specific-file` for precision
3. Always run `git status` and `git diff --staged` before committing to see exactly what you are about to commit

*Interview insight:* If an interviewer asks "have you ever accidentally committed something you should not have?" — this is the honest answer. It happens to everyone. The follow-up answer is: how do you prevent it (.gitignore + specific adds) and how do you fix it (`git rm --cached` + rotate credentials).

---

**Q9: How do you write a good commit message?**

**A:** Commit messages are the documentation of your project history. A year from now, a teammate will read your commit message trying to understand why a change was made. A bad message ("fix stuff", "WIP") is useless.

**Format:**
```
<type>(<scope>): <short summary — 50 characters max>

<body — wrap at 72 characters, explain WHY not WHAT>

<footer — ticket reference, breaking change notes>
```

**Examples:**

```bash
# Bad — tells you nothing
git commit -m "fix tests"

# Acceptable — tells you what
git commit -m "Fix login test assertion"

# Good — tells you what and why
git commit -m "Fix login test: assert token exists in response body

The previous assertion only checked status 200. Changing to also assert
the response body contains a non-null token field, matching AC #3 of MYAPP-145.

MYAPP-145"

# More examples:
git commit -m "Add smoke tests for checkout flow (MYAPP-201)

Covers: add to cart, proceed to checkout, payment entry, order confirmation.
Positive path only in this commit — negative scenarios in MYAPP-202."

git commit -m "refactor(pom): extract wait helper to BasePage

Eliminates repeated waitForElementVisible calls duplicated across 7 page objects.
No functional change — purely structural."
```

**Types used in conventional commits:** `feat`, `fix`, `test`, `refactor`, `chore`, `docs`, `ci`

*Rule of thumb:* Write the subject line as if completing the sentence: "If applied, this commit will ___." — "Add smoke tests for checkout flow."

---

**Q10: How do you see what changed in the last commit?**

**A:** There are multiple ways, each showing different levels of detail:

```bash
# Show the full diff of the most recent commit
git show HEAD

# Show only the file names that changed in the last commit
git show HEAD --stat

# Show the diff of a specific past commit
git show abc1234

# Show only the diff for one file in the last commit
git show HEAD -- src/test/java/LoginTest.java

# See what changed in the last 3 commits (formatted nicely)
git log -3 --oneline

# Show a patch (diff) for the last 3 commits
git log -3 -p
```

As a QA engineer, I use `git show HEAD --stat` right after pulling a branch to quickly understand the scope of what changed — which files, how many lines — before deciding what to test manually beyond the automated suite.

---

**Q11: What is git diff? How do you use it in QA work?**

**A:** `git diff` compares two states and shows line-by-line differences. It is one of the most powerful tools for a QA engineer.

```bash
# Show unstaged changes (working directory vs staging area)
git diff

# Show staged changes (staging area vs last commit) — what will be in the next commit
git diff --staged

# Compare two branches — what would merge into main from this feature branch
git diff main..feature/login-tests

# Compare two commits
git diff abc1234..def5678

# Compare a specific file between two branches
git diff main..feature/login-tests -- src/main/java/LoginController.java

# Compare your branch against main to see your PR changes
git diff main..HEAD

# Show only file names (no line details)
git diff --name-only main..HEAD

# Show stats (files changed, insertions, deletions)
git diff --stat main..HEAD
```

**QA use cases for git diff:**
1. Before raising a PR — `git diff main..HEAD` to review exactly what you are submitting
2. Understanding application changes — `git diff main..feature/X -- src/main/` to understand what application code changed, so you know what to test
3. Checking test coverage gap — `git diff main..HEAD --stat` to see if test files were modified alongside application files
4. Code review — `git diff HEAD~1 HEAD` to review the most recent commit before approving

---

**Q12: What is git stash? When is it useful? Give a real QA example.**

**A:** `git stash` temporarily saves your uncommitted work (staged and unstaged changes) and restores your working directory to a clean state. Your saved changes go into a "stash stack" and can be retrieved later.

```bash
# Stash current work (all staged and unstaged changes)
git stash

# Stash with a descriptive label — always use this in practice
git stash push -m "WIP: halfway through checkout negative test scenarios"

# List all stashes
git stash list
# Output: stash@{0}: WIP: halfway through checkout negative test scenarios
#         stash@{1}: WIP: login page object refactor

# Apply most recent stash and remove it from the stack
git stash pop

# Apply most recent stash but KEEP it in the stack (safer)
git stash apply

# Apply a specific stash by index
git stash pop stash@{1}

# Drop (delete) a specific stash
git stash drop stash@{0}

# Clear all stashes
git stash clear
```

**Real QA example:**

You are halfway through writing negative test cases for the checkout flow. You have 3 files modified, nothing committed. Your manager Slack messages: "Can you quickly verify the P1 payment crash fix on `hotfix/payment-crash` before it goes to production? We need sign-off in 15 minutes."

```bash
# You cannot switch branches with uncommitted changes — Git will refuse or overwrite them
# Solution: stash your work

git stash push -m "WIP: checkout negative tests — invalid card scenarios incomplete"
git checkout hotfix/payment-crash
# ... test the hotfix, verify the fix ...
git checkout feature/checkout-tests
git stash pop   # your work is back exactly as you left it
```

Without stash, your options are: commit broken half-finished work (bad) or discard your changes (devastating). Stash is the correct answer.

*Important:* `git stash pop` can cause merge conflicts if the same files were modified on the branch you switched to. `git stash apply` is safer — it applies but does not remove the stash, so if a conflict occurs you still have your stash as a backup.

---

**Q13: What is git rebase vs git merge? When should you use each?**

**A:** Both integrate changes from one branch into another, but they produce different histories.

**git merge** creates a merge commit that explicitly records when two branches joined:

```
Before merge:
main:    A──B──C
feature: A──B──D──E

After: git merge main (on feature branch):
feature: A──B──D──E──M   (M = merge commit, C is now included)
                └──C──/
```

```bash
git checkout feature/my-tests
git merge main   # creates a merge commit, preserves full history
```

**git rebase** replays your commits on top of the target branch, as if you had started from there:

```
Before rebase:
main:    A──B──C
feature: A──B──D──E

After: git rebase main (on feature branch):
main:    A──B──C
feature: A──B──C──D'──E'   (D and E are replayed as D' and E' on top of C)
```

```bash
git checkout feature/my-tests
git rebase main   # linear history, no merge commit
```

**When to use each:**

| Situation | Use |
|---|---|
| Syncing a personal feature branch with main | `rebase` — cleaner linear history |
| Merging a feature PR into main/develop | `merge` — preserves branch history, easier to revert |
| Branch shared with other team members | `merge` — never rebase shared branches |
| Preparing a clean commit history before PR | `rebase -i` (interactive) to squash/reorder |
| Emergency hotfix into main | `merge` — speed over cleanliness |

**The golden rule of rebase:** Never rebase a branch that has been pushed to remote and pulled by other people. Rebase rewrites commit hashes — anyone who has pulled your old commits will have their history diverge from yours, causing painful conflicts.

---

**Q14: What is a merge conflict? How do you resolve it step by step?**

**A:** A merge conflict occurs when two branches have modified the same part of the same file differently, and Git cannot automatically decide which version to keep. Git pauses the merge and marks the conflicting sections for you to resolve manually.

**Step-by-step resolution:**

```bash
# Step 1: Attempt the merge (conflict happens here)
git merge main
# Output: CONFLICT (content): Merge conflict in src/test/java/LoginTest.java
# Automatic merge failed; fix conflicts and then commit the result.

# Step 2: See which files have conflicts
git status
# Both modified: src/test/java/LoginTest.java

# Step 3: Open the conflicted file — Git adds markers showing both versions
```

Inside the file, conflict markers look like this:

```java
<<<<<<< HEAD   (your current branch — feature/login-tests)
@Test
public void login_validCredentials_redirectsToDashboard() {
    loginPage.login("admin", "Admin@123");
    Assert.assertEquals(driver.getTitle(), "Dashboard");
}
=======
@Test
public void login_validCredentials_redirectsToDashboard() {
    loginPage.login("admin", "Admin@123");
    Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"));
    Assert.assertEquals(driver.getTitle(), "Dashboard");
}
>>>>>>> main   (incoming branch)
```

```bash
# Step 4: Edit the file — decide what the final version should be.
# In this case, 'main' has the better version (extra URL assertion) — keep it.
# Remove ALL conflict markers (<<<<<<<, =======, >>>>>>>)
# Final version:

@Test
public void login_validCredentials_redirectsToDashboard() {
    loginPage.login("admin", "Admin@123");
    Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"));
    Assert.assertEquals(driver.getTitle(), "Dashboard");
}

# Step 5: Stage the resolved file
git add src/test/java/LoginTest.java

# Step 6: Complete the merge
git commit   # Git auto-generates the merge commit message

# Step 7: Verify everything is clean
git status
```

**Preventing conflicts:**
- Merge or rebase from main into your feature branch frequently (small, manageable conflicts)
- Keep feature branches short-lived (days, not weeks)
- Communicate with teammates when working on the same files
- Use feature-flag-driven development to avoid long parallel branches

---

**Q15: How do you undo the last commit safely?**

**A:** There are three levels of "undo" and the right one depends on whether you want to keep your changes and whether the commit has been pushed:

```bash
# SOFT RESET — undo the commit, keep changes STAGED
# Use when: you committed too early and want to restructure what goes in the commit
git reset --soft HEAD~1

# MIXED RESET (default) — undo the commit, keep changes in working directory (unstaged)
# Use when: you want to re-examine changes before re-committing
git reset HEAD~1

# HARD RESET — undo the commit and DELETE all changes — DESTRUCTIVE
# Use when: you absolutely want to throw the last commit away entirely
git reset --hard HEAD~1
# WARNING: Changes are gone. Only recoverable via git reflog within ~30 days.

# SAFE REVERT — undo by creating a NEW commit that reverses the changes
# Use when: the commit has already been pushed to a shared branch
git revert HEAD
git push origin main
```

**Decision tree:**
```
Has the commit been pushed to a shared branch?
  YES → git revert HEAD  (never rewrite shared history)
  NO  → is the branch shared with others?
          YES → git revert HEAD
          NO  → git reset --soft/mixed/hard (choose based on whether you want to keep changes)
```

*Common interview mistake:* Saying "I'd use `git reset --hard`" without mentioning whether the commit is pushed. Hard reset + force push on a shared branch is a serious error.

---

**Q16: What is git revert vs git reset? When do you use each?**

**A:** These are fundamentally different operations that answer different questions.

**git revert** — "I want to undo a commit but keep history intact."

- Creates a NEW commit that is the inverse of the target commit
- History is preserved — nothing is deleted
- Safe on shared/public branches
- Can revert any commit in history, not just the most recent

```bash
git revert abc1234        # undo commit abc1234 by creating a new commit
git revert HEAD           # undo the most recent commit
git revert HEAD~3..HEAD   # undo the last 3 commits (creates 3 revert commits)
```

**git reset** — "I want to move the branch pointer back in history."

- Moves HEAD and the branch pointer back to a previous commit
- Rewrites history — commits after the reset point appear to be deleted
- DANGEROUS on shared branches (breaks others who have pulled those commits)
- Three flavours: `--soft`, `--mixed`, `--hard`

```bash
git reset --soft HEAD~1   # commits undone, changes staged
git reset HEAD~1           # commits undone, changes in working dir
git reset --hard HEAD~1   # commits and changes both gone
```

**Summary table:**

| | git revert | git reset |
|---|---|---|
| Rewrites history | No | Yes |
| Safe on shared branches | Yes | No (requires force push) |
| Use case | Undo a pushed commit | Undo local-only commits |
| Creates new commit | Yes | No |
| Recoverable | Always | Only via reflog (short window) |

---

**Q17: What is git blame? When do QA engineers use it?**

**A:** `git blame <file>` annotates every line of a file with the commit hash, author, and date that last modified that line. It answers: "who wrote this, and when?"

```bash
# Blame a file — see who last changed each line
git blame src/main/java/LoginController.java

# Example output:
# abc1234 (Jane Smith  2024-04-10 14:32) public Response login(String email, String pass) {
# def5678 (John Dev    2024-04-14 09:15)     if (password == null || password.isEmpty()) {
# abc1234 (Jane Smith  2024-04-10 14:32)         return Response.badRequest("Required");

# Blame with short date format
git blame --date=short src/main/java/LoginController.java

# Blame a specific range of lines (line 20 to 40)
git blame -L 20,40 src/main/java/LoginController.java

# See the full commit for a hash from blame output
git show def5678
```

**When QA engineers use git blame:**

1. **A test fails unexpectedly.** Blame the application file being tested to find who changed the logic most recently. That person is your first contact to understand what changed and whether it is intentional.

2. **Behaviour contradicts the spec.** Blame the relevant controller or service file. The commit hash gives you the full diff and message, which explains why the change was made — was it intentional (in which case the spec needs updating) or a mistake (bug).

3. **Root cause analysis on a production bug.** Blame the file where the bug lives, find the commit that introduced the problematic line, read the commit message for context, and trace the PR that merged it.

4. **Onboarding to an unfamiliar module.** Blame a key file to see who wrote the core logic. Those authors are your subject matter experts for the area you are testing.

*Pro tip:* `git blame` + `git show <hash>` is a two-step investigation pattern that answers both "who" and "why" for any line of code.

---

**Q18: How do you check out a colleague's branch to test their feature?**

**A:** This is a routine task for QA engineers who need to test a feature branch locally before approving a PR.

```bash
# Method 1: Fetch and checkout (traditional)
git fetch origin                                          # download all remote branches
git checkout -b colleagues-branch origin/feature/payment  # create local tracking branch

# Method 2: Modern switch syntax
git switch --track origin/feature/payment-refactor

# Method 3: GitHub CLI — most convenient (PR number)
gh pr checkout 47      # checks out the PR branch directly

# After testing — switch back to your branch
git switch main
git switch feature/my-tests
```

**Why `git fetch` first?** `git fetch` downloads the remote branch metadata without modifying any local files. If you skip it, Git may not know about a branch that was just created on remote and the `checkout` command will fail with "pathspec not found."

**What to do after checking out:**
```bash
# Confirm you are on the right branch
git branch   # look for * feature/payment-refactor

# See what commits are on this branch vs main
git log --oneline main..HEAD

# Run the tests
npm test  # or mvn test

# After testing, never commit to a colleague's branch without permission
git switch main  # go back to your work
```

---

## SECTION 3 — Pull Requests and Code Review

---

**Q19: What is a pull request? What does QA specifically check when reviewing one?**

**A:** A pull request (PR) — or merge request (MR) in GitLab — is a formal request to merge code from one branch into another. For QA, the PR is a quality gate. It is the last structured checkpoint before code enters the shared codebase.

**What QA checks in a PR review:**

**Step 1 — PR description quality:**
- Does the PR reference the Jira ticket? ("Implements MYAPP-145: Login feature")
- Is there a description of what changed and why?
- Are there screenshots or a test video for UI changes?
- Are there any noted risks, performance concerns, or areas needing extra attention?

**Step 2 — Test coverage:**
- Every feature change should have new or updated automated tests
- Every bug fix should have a test that would have caught the original bug
- A PR with application code changes but zero test changes is a red flag — comment on it

**Step 3 — Test code quality review:**
```
Assertions: are they meaningful?
  ✓  Assert.assertEquals(response.getStatusCode(), 200)
  ✗  Assert.assertTrue(true)   ← useless assertion

Wait strategies:
  ✓  await expect(locator).toBeVisible()   ← Playwright auto-wait
  ✗  Thread.sleep(5000)                    ← hardcoded sleep — always flag this

Structure:
  ✓  Page Object Model followed
  ✓  Test data externalised (not hardcoded in test methods)
  ✓  @BeforeEach / @AfterEach properly used

Readability:
  ✓  shouldRedirectToDashboardOnValidLogin()   ← descriptive name
  ✗  test1()                                   ← meaningless name
```

**Step 4 — Manual testing of the branch (for high-risk changes):**
```bash
gh pr checkout 47   # GitHub CLI — checkout the PR branch directly
# Run the app locally, manually verify the feature
# Test edge cases not covered by automation
```

**Step 5 — Leave test evidence as PR comments:**
```
QA review on branch feature/MYAPP-145-login:

Manual verification: Chrome 124 / macOS 14 — PASS
Automated suite: 52 tests, 0 failures
Zephyr: Sprint 24 cycle — TC-003 PASS, TC-004 PASS, TC-005 PASS

Approval: ✓ Ready to merge
```

---

**Q20: What is branch protection? How does it enforce quality gates?**

**A:** Branch protection is a repository setting that prevents direct pushes to critical branches (like `main` or `develop`) and enforces rules before any PR can be merged. It is the technical mechanism that makes quality gates automatic rather than voluntary.

**Recommended protection rules for `main`:**
```
Rule 1: Require pull request before merging
  → Nobody can push directly to main — all changes go through PRs
  → Minimum 1 approving review required (ideally QA + developer)

Rule 2: Require status checks to pass
  → CI pipeline (unit tests, integration tests, E2E tests) must be GREEN
  → If tests fail, the PR cannot merge — period

Rule 3: Require branch to be up to date
  → PR branch must include the latest main commits before merging
  → Prevents "worked on my branch, broke on main after merge" situations

Rule 4: Prevent force pushes
  → Nobody can rewrite history on main
  → Protects commit integrity for audit trails

Rule 5: Require linear history (optional — team preference)
  → Only squash or rebase merges allowed
  → Keeps main history clean and readable
```

**From a QA perspective,** branch protection means that my approval is required before code ships. If I do not approve, the PR cannot merge. This gives QA a real quality gate rather than a ceremonial one.

*Interview follow-up:* "Have you ever advocated for stricter branch protection?" — Yes. Specifically adding QA as a required reviewer on the main branch. Without it, developers can approve each other's PRs and QA review becomes optional.

---

## SECTION 4 — Branching Strategies

---

**Q21: What is GitFlow? Where does QA test within it?**

**A:** GitFlow is a structured branching strategy designed for teams with scheduled, versioned releases. It uses multiple long-lived branches to separate concerns across the development lifecycle.

**Branch structure:**

| Branch | Purpose | Lifetime |
|---|---|---|
| `main` | Production-ready code only, tagged releases | Permanent |
| `develop` | Integration branch — all features merge here | Permanent |
| `feature/*` | Individual feature development | Temporary (days/weeks) |
| `release/*` | Release preparation — bug fixes only | Temporary |
| `hotfix/*` | Emergency production fixes | Temporary |

```
main ──────────────────────────────────── v1.0 ──────── v1.1
       \                                  /         \
develop ──────────────────────────────────           hotfix/payment-crash
          \         /     \          /
     feature/login        feature/search
                               \
                            release/1.0 ← QA full regression here
```

**Where QA tests:**

1. **Feature branch** — functional testing of the specific feature as it develops. Does this feature work? Does it meet the Acceptance Criteria?

2. **Develop branch** — integration testing when multiple features merge together. Do the features work together? Are there integration regressions?

3. **Release branch** — full regression testing, release sign-off. No new features can enter after branch is cut. QA is the gatekeeper here.

4. **Hotfix branch** — rapid targeted testing of the emergency fix. Does the fix work? Did it introduce any regressions in adjacent functionality?

*Trade-off:* GitFlow provides clear testing phases but creates long-lived branches that accumulate merge conflicts. Feature branches that live for weeks become painful to integrate.

---

**Q22: What is GitHub Flow? How is it simpler than GitFlow?**

**A:** GitHub Flow is a lightweight alternative where there is effectively one long-lived branch (`main`), and all work happens in short-lived feature branches that are merged directly into `main` via PRs.

**Structure:**
- `main` — always deployable, represents production
- `feature/*` — any feature, bug fix, or experiment — merged via PR and then deleted

```
main ─────────────────────────────────────────────────
         \              /     \              /
      feature/login              feature/search
      (PR: CI + QA review → merge → delete branch)
```

**How QA fits in GitHub Flow:**
- **Feature branch PR** — QA reviews the PR, checks test coverage, may manually test the branch
- **CI pipeline** — automated tests run on every push to the feature branch; must be green before merge
- **Post-merge to main** — smoke tests run to confirm the merge did not break anything
- QA's job is ensuring every PR has sufficient test coverage and approving only when it does

**Why it is simpler than GitFlow:**
- No `develop`, `release/*`, or `hotfix/*` branches to manage
- No "when do I branch from where?" complexity
- The PR process is the entire workflow
- Works well for teams that deploy continuously rather than on scheduled release cycles

*Trade-off:* GitHub Flow is simple but requires strong automated test coverage and CI discipline. Without it, bugs reach `main` (and production) easily. GitFlow's longer release cycle gives time for structured QA; GitHub Flow compresses that into the PR review.

---

**Q23: What is trunk-based development? What does QA do differently?**

**A:** Trunk-based development (TBD) takes simplicity further than GitHub Flow. Developers commit directly to `main` (the "trunk"), or use branches so short-lived they live for hours, not days. Feature flags control feature visibility — code is deployed but features are turned on/off without deployment.

**Structure:**
```
main ──commit──commit──commit──commit──commit──commit──
     (CI runs on every single commit)
     (feature flags: flag.isEnabled("new-checkout-flow"))
```

**What QA does differently in trunk-based development:**

1. **CI is the primary test gate** — every commit triggers the full automated test suite. QA's role is maintaining and expanding that suite, not milestone testing.

2. **Feature flag testing** — QA must test both states: feature ON (new behaviour) and feature OFF (existing behaviour unchanged). This is unique to TBD.

3. **No "QA phase" — quality is built in** — there is no release branch to test. Automated coverage must be comprehensive enough to catch regressions on every commit.

4. **Canary deployments and production monitoring** — QA participates in defining health metrics and alert thresholds. Quality extends into production.

5. **Test-in-production patterns** — dark launching, A/B testing, shadow traffic. QA understands these patterns and validates them.

*When to use TBD:* High-velocity teams with mature CI/CD, strong automated coverage (>80%), and a culture of shared ownership. Not suitable for teams with limited automation or long QA cycles.

---

**Q24: How do you compare GitFlow, GitHub Flow, and trunk-based development for QA?**

**A:**

| Aspect | GitFlow | GitHub Flow | Trunk-Based |
|---|---|---|---|
| Complexity | High | Low | Medium |
| Release cadence | Scheduled | Continuous | Continuous |
| QA testing phase | Release branch | Feature PR | Every commit in CI |
| Branch lifespan | Feature: days–weeks | Feature: 1–3 days | Hours |
| Merge conflicts | Frequent (long branches) | Occasional | Rare |
| Automation requirement | Moderate | High | Very high |
| Feature flag usage | Rare | Optional | Essential |
| Best for | Enterprise, versioned releases | SaaS teams, frequent deploys | High-velocity, mature CI |

*Interview answer structure:* "The right strategy depends on the team's release cadence and automation maturity. For a team releasing on a monthly cycle with a structured QA phase, GitFlow provides clear testing milestones. For a SaaS team shipping continuously, GitHub Flow or trunk-based development requires strong automated coverage but delivers much faster feedback."

---

## SECTION 5 — Protecting Credentials and Sensitive Files

---

**Q25: How do you prevent committing .env files accidentally?**

**A:** Three layers of protection work together. Relying on a single layer is insufficient.

**Layer 1 — .gitignore (first and most important):**
```gitignore
# Add these BEFORE you ever create the .env file
.env
.env.local
.env.production
.env.*
*.env
credentials.json
secrets.yaml
serviceAccountKey.json
```

**Layer 2 — Git pre-commit hook:**
```bash
# Create: .git/hooks/pre-commit
#!/bin/bash
# Block commits that include .env files or credential-like content

# Check if .env is being staged
if git diff --cached --name-only | grep -q "\.env"; then
    echo "ERROR: Attempting to commit a .env file. Commit blocked."
    exit 1
fi

# Check for common credential patterns in staged content
if git diff --cached | grep -qiE "(password|secret|api_key|token)\s*=\s*.{6,}"; then
    echo "WARNING: Possible credentials detected in staged changes. Commit blocked."
    exit 1
fi

exit 0
```

```bash
chmod +x .git/hooks/pre-commit
```

**Layer 3 — Secret scanning tools:**
- **`git-secrets`** — scans commits for AWS credentials and custom patterns
- **`truffleHog`** — detects high-entropy strings (API keys, tokens)
- **GitHub secret scanning** — automatically alerts when known credential formats are pushed
- **gitleaks** — standalone tool, works locally and in CI

**Best practice for test credentials:**
```java
// Never hardcode — always read from environment
String username = System.getenv("TEST_USERNAME");
String password = System.getenv("TEST_PASSWORD");
String apiKey   = System.getenv("QA_API_KEY");
```

Store in `.env` locally, store in GitHub Secrets / Jenkins Credentials / AWS Secrets Manager in CI/CD.

---

**Q26: What should be in .gitignore for a QA automation project?**

**A:** A complete `.gitignore` for a QA project prevents generated files, credentials, and IDE artifacts from being accidentally committed:

```gitignore
# ── CREDENTIALS — CRITICAL — never commit these ────────────────────────────
.env
.env.local
.env.production
.env.*
credentials.json
secrets.yaml
serviceAccountKey.json

# ── JAVA BUILD OUTPUT ──────────────────────────────────────────────────────
target/
build/
out/
*.class
*.jar

# ── NODE / NPM (Playwright, Cypress) ───────────────────────────────────────
node_modules/
npm-debug.log*
yarn-error.log*

# ── PLAYWRIGHT GENERATED OUTPUT ────────────────────────────────────────────
test-results/          # Test result files
playwright-report/     # Generated HTML report
.playwright/           # Internal cache
blob-report/           # Blob reporter output

# ── ALLURE REPORTING ───────────────────────────────────────────────────────
allure-results/        # Raw result files (generated per run)
allure-report/         # Generated HTML — rebuild from allure-results

# ── SCREENSHOTS AND RECORDINGS ─────────────────────────────────────────────
screenshots/           # Runtime failure screenshots
videos/                # Playwright/Cypress recordings
downloads/             # Files downloaded during tests

# ── LOGS ───────────────────────────────────────────────────────────────────
*.log
logs/
test-output/           # TestNG output directory

# ── IDE FILES ──────────────────────────────────────────────────────────────
.idea/
*.iml
.vscode/
.classpath
.project
.settings/

# ── OS FILES ───────────────────────────────────────────────────────────────
.DS_Store
Thumbs.db

# ── ALWAYS COMMIT (never ignore these) ────────────────────────────────────
# src/                      — all test source code
# pom.xml / build.gradle    — build configuration
# playwright.config.ts      — Playwright config
# .github/workflows/        — CI/CD definitions
# testdata/                 — static test data files (without credentials)
# .gitignore itself
```

*Verification command:* Before committing for the first time on a new project:

```bash
git status            # check what Git sees
git diff --staged     # see exactly what will be committed
git ls-files .env     # if this returns output, .env is tracked — fix immediately
```

---

**Q27: What happens when you accidentally commit a secret? Walk through the fix step by step.**

**A:** This is a critical scenario. The key rule: once a secret is committed and pushed, assume it is compromised — even to a private repository. Act accordingly.

**Step 1 — Immediate: Rotate the secret.**
Before anything else, invalidate the credential. Change the API key, password, or token now. Do not wait until you clean the history. Assume it was seen the moment it was pushed.

**Step 2 — Remove from tracking (if not yet pushed):**
```bash
git rm --cached .env                    # remove from staging, keep file on disk
echo ".env" >> .gitignore              # prevent it from being re-added
git commit -m "Remove .env from tracking, add to .gitignore"
```

**Step 3 — If already pushed to remote (non-public repo):**
```bash
# Remove from the current commit (if it was the last commit)
git rm --cached .env
echo ".env" >> .gitignore
git commit -m "Remove accidentally tracked .env file"
git push origin <branch>

# But the .env is still in git history — visible in git log -p
# For a private repo with limited access, rotating the secret may be sufficient
# Notify your security team either way
```

**Step 4 — If pushed to a public repo or high-risk context (history rewrite):**
```bash
# Modern approach — git filter-repo (recommended over filter-branch)
pip install git-filter-repo
git filter-repo --path .env --invert-paths   # removes .env from ALL commits

# Force push the rewritten history (coordinate with entire team first)
git push origin --force --all
git push origin --force --tags

# All team members MUST re-clone the repository after this
# Their local copies have the old history — merging will bring it back
```

**Step 5 — Verify and communicate:**
```bash
git log -p --all -- .env   # should return nothing if rewrite succeeded
```

Notify your security team, manager, and all repository collaborators.

*The most important step is Step 1 — rotate the credential — before doing anything else.*

---

**Q28: How do you sync your branch when it falls behind main?**

**A:** When main has progressed while you were working on your feature branch, you need to integrate those changes. There are two approaches:

```bash
# Step 1: Fetch the latest remote state (always do this first)
git fetch origin

# Step 2: See how far behind you are
git log HEAD..origin/main --oneline
# If this shows commits, you are behind main

# APPROACH A — Merge (preserves merge commit, safer on shared branches)
git merge origin/main

# APPROACH B — Rebase (linear history, preferred for personal feature branches)
git rebase origin/main

# If rebase causes conflicts:
# 1. Fix the conflict in the affected files
# 2. Stage the resolved files
git add resolved-file.java
# 3. Continue the rebase
git rebase --continue
# 4. Or abort if it goes wrong
git rebase --abort

# Step 3: Push the updated branch
# If you used merge:
git push origin feature/my-tests

# If you used rebase (rewrites history — use force-with-lease, NOT --force):
git push --force-with-lease origin feature/my-tests
# --force-with-lease fails if someone else pushed to the branch since your last fetch
# Safer than --force which blindly overwrites
```

*When to do this:* Before raising a PR (branch must be up to date), and whenever main has received important changes (security fixes, shared utilities) that your tests depend on.

---

## SECTION 6 — CI/CD Integration and Tags

---

**Q29: How does Git integrate with CI/CD? Walk through what happens when you push.**

**A:** CI/CD pipelines react to Git events through webhooks — the Git host (GitHub, GitLab, Bitbucket) sends an HTTP notification to the CI server when a Git event occurs.

**Complete push-to-deploy flow:**

```
Developer pushes to feature/login-tests
        ↓
GitHub sends webhook to CI (GitHub Actions, Jenkins, CircleCI)
        ↓
CI pipeline runs:
  - Checkout the branch
  - Install dependencies
  - Compile / build
  - Unit tests
  - Integration tests
  - E2E tests (Playwright / Selenium)
        ↓
Results posted back to GitHub as "status checks" on the PR
        ↓
Branch protection checks:
  - All status checks GREEN? → PR can merge
  - Any RED? → PR is blocked until fixed
        ↓
QA approves PR
        ↓
Merge to main → CI runs again:
  - Full test suite on main
  - Deploy to staging
  - Run smoke tests on staging
        ↓
Tag v1.0.0 → CI:
  - Build release artifact
  - Deploy to production
  - Run post-deployment smoke tests
```

**Git events and what they trigger:**

| Git Event | CI Trigger |
|---|---|
| `push` to feature branch | Unit tests + linting |
| Pull Request opened | Full test suite + post results as PR status check |
| `push` to main | Full suite + deploy to staging |
| `git tag v1.0.0` | Build release + deploy to production |
| Schedule (cron) | Nightly full regression on latest main |

```yaml
# .github/workflows/qa-tests.yml — example
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
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - run: npm ci
      - run: npx playwright install --with-deps
      - run: npx playwright test
        env:
          BASE_URL: ${{ secrets.QA_BASE_URL }}
          TEST_PASSWORD: ${{ secrets.TEST_PASSWORD }}
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: playwright-report
          path: playwright-report/
```

---

**Q30: What is git tag? How is it used for releases? What does QA do with tags?**

**A:** A tag is a permanent, human-readable label attached to a specific commit. Unlike branches, tags do not move when new commits are added. They are used to mark release points.

```bash
# Create a lightweight tag (just a pointer — no metadata)
git tag v1.0.0

# Create an annotated tag (recommended — includes author, date, message)
git tag -a v1.0.0 -m "Release 1.0.0 — first stable release"

# Tag a past commit (useful when you forget to tag at release time)
git tag -a v1.0.0 abc1234 -m "Release 1.0.0"

# List all tags
git tag
git tag -l "v1.*"    # filter by pattern

# Push a specific tag to remote (tags are NOT pushed with regular git push)
git push origin v1.0.0

# Push all tags
git push origin --tags

# Checkout a tag (detached HEAD — read-only state)
git checkout v1.0.0

# Delete a local tag
git tag -d v1.0.0

# Delete a remote tag
git push origin --delete v1.0.0
```

**How QA uses tags:**

1. **Identify the exact code that was tested.** Before signing off on a release, QA tags the commit: `git tag -a v1.5.0-qa-approved -m "QA sign-off for Sprint 24 release"`. This creates an immutable record.

2. **Compare releases for regression scope.** `git diff v1.4.0..v1.5.0 --stat` shows exactly what changed between the last release and the current one — this is the minimum scope for regression testing.

3. **Reproduce a production bug.** `git checkout v1.3.0` puts the repository at the exact state of a past release, allowing QA to reproduce a bug in the environment that was live when the bug was reported.

4. **Trigger release-specific CI pipelines.** CI pipelines often have different jobs triggered by `v*` tags (build artifact, deploy to production, run post-deployment smoke tests).

---

## SECTION 7 — Scenario-Based and Interview Q&A

---

**Q31: Tests were passing yesterday but failing today. How do you use Git to investigate?**

**A:**

```bash
# Step 1: See what changed in the last 24 hours
git log --oneline --since="yesterday"

# Step 2: Find what changed in the specific test file
git log --oneline -- src/test/java/LoginTest.java

# Step 3: Find what changed in the application file the test covers
git log --oneline --since="yesterday" -- src/main/java/LoginController.java

# Step 4: See the exact diff of a suspicious commit
git show abc1234

# Step 5: If you know a good commit — use git bisect to find the bad one
git bisect start
git bisect bad HEAD              # current state is failing
git bisect good v2.3.0          # this version was passing
# Git checks out commits for you to test
# Run your tests, then:
git bisect good    # or
git bisect bad
# Repeat until Git identifies the exact commit that broke things
git bisect reset   # restore HEAD when done

# Step 6: Blame the application file to find the author
git blame src/main/java/LoginController.java
# Contact the author of the most recently changed lines
```

---

**Q32: Your branch has diverged from main. You want a clean linear history. What do you do?**

**A:**

```bash
# Fetch latest remote state
git fetch origin

# Rebase your branch on top of latest main
git rebase origin/main

# If conflicts arise:
# 1. Fix conflicts in each file
# 2. git add <resolved-file>
# 3. git rebase --continue
# Repeat for each conflict

# Verify the result looks correct
git log --oneline --graph

# Force push with lease (not --force) since rebase rewrites hashes
git push --force-with-lease origin feature/my-branch
```

Important: only do this on branches you own. Never rebase branches that others have checked out.

---

**Q33: How do you cherry-pick a commit from one branch to another?**

**A:** Cherry-pick applies a specific commit from one branch to your current branch, without merging the entire branch.

```bash
# Find the commit hash you want
git log --oneline feature/payment-fix
# abc1234 Fix: null pointer on zero-amount payment

# Switch to the target branch
git checkout main

# Apply only that one commit
git cherry-pick abc1234

# If there are conflicts:
# Fix them, then:
git cherry-pick --continue

# Or abort if it goes wrong
git cherry-pick --abort
```

**QA use case:** A hotfix was made directly on the `hotfix/` branch. You need the same fix applied to `develop` without merging the full hotfix branch. Cherry-pick the specific fix commit onto develop.

---

**Q34: How do you use git bisect to find which commit broke a test?**

**A:** `git bisect` performs a binary search through commit history to find the exact commit that introduced a regression. Instead of checking every commit, it cuts the search space in half each step.

```bash
# Start bisect
git bisect start

# Mark current state as bad (broken)
git bisect bad HEAD

# Mark a known-good commit (when tests were passing)
git bisect good v2.3.0   # or any known-good commit hash

# Git checks out a commit halfway between good and bad
# Run your tests
mvn test   # or npx playwright test -- tests/login.spec.ts

# If tests pass on this commit:
git bisect good

# If tests fail:
git bisect bad

# Git keeps halving the range — repeat 5-7 times for a range of ~100 commits
# Git eventually identifies the exact first bad commit:
# "abc1234 is the first bad commit"

# Exit bisect mode
git bisect reset   # returns to original branch and HEAD

# Inspect the culprit
git show abc1234
```

*Interview insight:* Most QA engineers do not know about `git bisect`. Mentioning it demonstrates advanced Git knowledge and a systematic debugging mindset.

---

**Q35: What does QA check when reviewing their own PR before submitting?**

**A:**

```bash
# Step 1: See all files changed
git diff main..HEAD --stat

# Step 2: Review every line you are submitting
git diff main..HEAD

# Step 3: Check commit messages are meaningful
git log main..HEAD --oneline

# Step 4: Verify no sensitive files are included
git diff main..HEAD --name-only | grep -i "\.env\|secret\|credential"
# Should return nothing

# Step 5: Confirm tests pass locally before raising the PR
npm test   # or mvn test

# Step 6: Self-review the PR description
# - Jira ticket referenced?
# - What changed and why?
# - How to test?
# - Any risks?
```

*Common mistake:* Raising a PR without reviewing your own diff first. Reviewers frequently find accidentally included debug logs, commented-out code, or TODO comments that were meant to be cleaned up.

---

## SECTION 8 — 10 Interview Q&A

---

**Q36: What is the difference between git fetch and git pull?**

**A:** `git fetch` downloads changes from the remote repository into local remote-tracking branches (like `origin/main`) but does NOT touch your working directory or current branch. It is completely safe — nothing local changes.

`git pull` is `git fetch` followed by `git merge` (or `git rebase` if configured). It downloads AND integrates remote changes into your current branch, which can cause merge conflicts.

```bash
git fetch origin           # safe — just downloads
git log origin/main --oneline -5   # inspect what changed remotely
git merge origin/main      # then integrate when ready
# vs.
git pull origin main       # fetch + merge in one step
```

As a QA rule: use `git fetch` before checking out a colleague's branch or before rebasing. It keeps you informed without surprises.

---

**Q37: How do you find what changed between two commits or two releases?**

**A:**

```bash
git diff v1.4.0..v1.5.0          # full diff between two release tags
git diff v1.4.0..v1.5.0 --stat   # file list only — understand scope
git log v1.4.0..v1.5.0 --oneline  # commit list between tags

# Compare your PR against main
git diff main..HEAD
git diff main..HEAD --name-only   # just file names
```

I use this before every release cycle to understand the scope of change and determine which tests to prioritise. If only the payment module changed, I focus regression effort there rather than running the full suite manually.

---

**Q38: What is a merge conflict and how do you resolve it?**

**A:** A merge conflict occurs when two branches modify the same section of the same file differently, and Git cannot automatically decide which version is correct. Git pauses and marks the conflicting sections.

Resolution: open the file, identify the conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`), decide which version to keep (or combine them), remove all markers, `git add` the file, then `git commit`.

Prevention: merge main into feature branches frequently, keep branches short-lived, communicate when working on the same files.

---

**Q39: What is the difference between git revert and git reset?**

**A:** `git revert` creates a new commit that undoes a previous commit — history is preserved, it is safe on shared branches.

`git reset` moves the branch pointer back — rewrites history. Dangerous on shared branches because it changes commit hashes that others may have pulled.

Rule: on `main` or `develop` — always `git revert`. On your own feature branch that nobody else has pulled — `git reset` is acceptable with care.

---

**Q40: A test has been failing in CI for three days. How do you find the commit that broke it?**

**A:**

1. `git log --oneline --since="3 days ago"` — find commits in the timeframe
2. `git log --oneline -- path/to/file` — narrow to the relevant file
3. `git bisect` — if many commits, binary search to find the exact culprit
4. `git show <commit>` — examine the breaking commit's diff
5. `git blame path/to/file` — find the author of the changed lines
6. Contact the author, discuss whether the change was intentional, fix accordingly

---

**Q41: What branching strategy would you recommend for a QA-mature team?**

**A:** GitHub Flow or trunk-based development for teams deploying continuously. GitFlow for teams with versioned releases.

For most modern teams I recommend: short-lived feature branches (max 3 days), PRs required with CI gate + QA approval for main, automated tests on every push, smoke suite post-merge. This keeps the feedback loop tight without the complexity of GitFlow's multiple long-lived branches.

---

**Q42: How do you ensure your test repository does not contain credentials?**

**A:** Three layers: `.gitignore` for all credential files, a pre-commit hook that scans staged content for credential patterns, and secret scanning tools (git-secrets, gitleaks, GitHub secret scanning). All credentials in test code come from environment variables — never hardcoded.

---

**Q43: What is git stash and when would you use it as a QA engineer?**

**A:** `git stash` saves uncommitted work and restores a clean working directory, allowing you to switch contexts immediately. Classic QA use case: you are writing tests mid-feature when a P1 bug needs urgent verification on a different branch. Stash your work, verify the bug, pop the stash, resume.

---

**Q44: How does git blame help you during a test failure investigation?**

**A:** `git blame <file>` shows who last changed each line and in which commit. When a test fails, I blame the application file being tested to find who changed the relevant code most recently. Then `git show <hash>` gives the full diff and commit message — explaining what changed and why. This tells me immediately whether the test failure is a genuine regression or an intentional behaviour change that needs the test updated.

---

**Q45: What is the difference between parallel="tests" and parallel="methods" in TestNG?**

**A:** `parallel="tests"` — each `<test>` block in `testng.xml` runs on its own thread. Tests within a `<test>` block run serially. This is the safe default. `parallel="methods"` — each individual `@Test` method runs on its own thread. This is the fastest mode but requires every test to be completely independent. Any shared state (static fields, `@BeforeClass` setup) will cause race conditions. Start with `parallel="tests"` and only move to `parallel="methods"` after confirming every test method is fully isolated.

---

*End of Guide — Git for QA Engineers*
