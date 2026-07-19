# Accessibility Testing — Complete Guide | WCAG 2.1 + Tools + Manual + Automated

> Senior QA Interview Preparation — Accessibility Testing Deep Dive (Especially Relevant for EdTech)

---

## Table of Contents

1. [What is Accessibility Testing and Why It Matters](#section-1)
2. [WCAG 2.1 Principles — POUR](#section-2)
3. [WCAG Conformance Levels](#section-3)
4. [Manual Accessibility Testing](#section-4)
5. [Automated Accessibility Testing](#section-5)
6. [Playwright Accessibility Test Example](#section-6)
7. [Common Accessibility Bugs QA Finds](#section-7)
8. [All Commands for Accessibility Testing](#section-8)
9. [Interview Q&A](#section-9)

---

## Section 1 — What is Accessibility Testing and Why It Matters {#section-1}

### Definition

Accessibility testing verifies that a software product is usable by people with disabilities. It ensures that users who rely on assistive technologies — such as screen readers, keyboard navigation, switch controls, or voice recognition — can access the same features and information as any other user.

Accessibility is not a "nice to have". It is a fundamental quality attribute, a legal requirement in many jurisdictions, and for EdTech products, it directly determines whether every student can participate equally in education.

### Legal and Regulatory Framework

| Standard | Region | Who Must Comply |
|----------|--------|-----------------|
| **WCAG 2.1 AA** | Global (de facto standard) | Effectively all public-facing digital products |
| **ADA (Americans with Disabilities Act)** | United States | Public-facing websites and apps |
| **Section 508** | United States | Federal government and their contractors |
| **EN 301 549** | European Union | Public sector bodies and European procurement |
| **Equality Act 2010** | United Kingdom | Service providers |
| **AODA** | Ontario, Canada | Public and private sector organisations |

Failing to meet WCAG 2.1 AA has resulted in legal action against organisations including Target, Domino's, Netflix, and many universities.

### Why EdTech Must Care

EdTech products serve students who may have:
- **Visual impairments**: blind students using screen readers, low vision students using zoom
- **Motor impairments**: students who cannot use a mouse, using keyboard only or switch access devices
- **Cognitive disabilities**: students with ADHD, dyslexia, processing differences
- **Hearing impairments**: deaf or hard-of-hearing students needing captions for videos

If a student with a visual impairment cannot access an online assessment or learning material, they are excluded from education. This is not just a legal problem — it is an ethical one. QA has a direct role in preventing that exclusion.

### Types of Disabilities to Test For

**Visual:**
- Blindness (screen reader users — NVDA, JAWS, VoiceOver)
- Low vision (screen magnification, high contrast mode, zoom up to 400%)
- Color blindness (cannot distinguish red from green, blue from yellow)

**Motor/Physical:**
- No mouse access (keyboard-only users)
- Switch access users (limited to one or two buttons)
- Voice recognition users (Dragon NaturallySpeaking)

**Cognitive:**
- ADHD (distracted by animations, need clear structure)
- Dyslexia (benefit from clear fonts, spacing, plain language)
- Processing difficulties (need consistent navigation, clear labels)

**Hearing:**
- Deaf users (need captions on video, transcripts on audio)
- Hard of hearing (need volume controls, visual alerts)

---

## Section 2 — WCAG 2.1 Principles — POUR (Always Asked) {#section-2}

WCAG 2.1 is organised around four principles, remembered with the acronym **POUR**.

### P — Perceivable

Information and user interface components must be presentable to users in ways they can perceive. If a user cannot perceive the information, they cannot use it.

**Key requirements:**
- All non-text content has a text alternative (alt text on images)
- Captions for video content
- Audio descriptions for video (describing visual information)
- Content is not conveyed by colour alone
- Text can be resized to 200% without loss of content or function
- Minimum colour contrast ratio: 4.5:1 for normal text, 3:1 for large text

**Test examples:**
- Screen reader reads out an image: does it announce meaningful alt text or "image.png"?
- Zoom the browser to 200%: is all content still visible and usable?
- View a form with error states: are errors indicated only by colour (bad) or by text + icon + colour (good)?

### O — Operable

User interface components and navigation must be operable. If a user cannot operate the interface, they cannot use it.

**Key requirements:**
- All functionality available from a keyboard (no mouse required)
- Users are not trapped in a component (keyboard trap)
- Sufficient time to read and use content (no auto-expiring sessions without warning)
- No flashing content that could trigger seizures (< 3 flashes per second)
- Users can bypass repeated blocks (skip navigation links)
- Pages have descriptive titles
- Focus order is logical and sequential

**Test examples:**
- Navigate the entire application using Tab, Shift+Tab, Enter, Space, Escape — can every feature be used?
- Is there a "Skip to main content" link at the top of every page?
- Does an auto-dismissing notification give users enough time to read it?

### U — Understandable

Information and operation of the user interface must be understandable. Even if users can perceive and operate the interface, they must be able to understand it.

**Key requirements:**
- Language of the page is declared (`<html lang="en">`)
- Unusual words, idioms, and jargon are explained
- Navigation is consistent across pages
- Error messages identify the field in error and describe what is wrong
- Labels are visible and persistent (not just placeholder text)
- No unexpected context changes on input

**Test examples:**
- Error on a form field: does the message say "Error" (bad) or "Email address is required — please enter your email in the format name@example.com" (good)?
- Does selecting a dropdown option automatically change the page without warning? (Bad — WCAG requires user-initiated navigation)

### R — Robust

Content must be robust enough to be interpreted reliably by a wide variety of user agents, including assistive technologies.

**Key requirements:**
- Valid, well-formed HTML (proper nesting, complete tags, valid attributes)
- Name, role, and value are available for all UI components
- Status messages are programmatically available (announced to screen readers without focus change)

**Test examples:**
- Custom dropdown built with `<div>` instead of `<select>`: does the screen reader announce its role, name, and state?
- When a loading spinner appears, is it announced to screen reader users?

---

## Section 3 — WCAG Conformance Levels {#section-3}

### Level A — Minimum Accessibility

The most basic requirements. Failing Level A makes content inaccessible to the most users with the most severe impact.

**Examples of Level A criteria:**
- Images have alt text
- Video has captions
- All functionality is keyboard accessible
- Colour is not the only means of conveying information
- Pages have a title

**Status:** Must have. Failure is a serious barrier.

---

### Level AA — Recommended Standard (Most Laws Require This)

The practical target for most organisations. Level AA includes all Level A requirements plus additional requirements.

**Examples of AA criteria beyond Level A:**
- Colour contrast ratio: 4.5:1 for normal text, 3:1 for large text (18pt+ or 14pt+ bold)
- Text can be resized to 200% without loss of functionality
- Images of text are not used (except logos)
- Focus is visible on interactive elements
- Multiple ways to navigate (search, sitemap, breadcrumbs)
- Error prevention: users can review and correct submissions
- Language of parts (when a page has content in more than one language)

**Status:** Legal requirement in most jurisdictions. This is the target for QA testing.

---

### Level AAA — Highest (Aspirational)

The most comprehensive requirements. Full AAA conformance is aspirational for most sites — not expected across entire sites but desirable for specific content.

**Examples of AAA criteria:**
- Sign language interpretation for video
- 7:1 contrast ratio (enhanced)
- No timing restrictions
- Extensive error prevention

**Status:** Aspirational. Not required by most laws but represents best practice.

---

### Colour Contrast Reference Table

| Text Type | AA Requirement | AAA Requirement |
|-----------|---------------|-----------------|
| Normal text (< 18pt regular, < 14pt bold) | 4.5:1 | 7:1 |
| Large text (18pt+ regular or 14pt+ bold) | 3:1 | 4.5:1 |
| UI components and graphical objects | 3:1 | Not specified |
| Disabled elements | No requirement | No requirement |
| Decorative images | No requirement | No requirement |
| Logotypes | No requirement | No requirement |

**How to calculate contrast ratio:**
- Use the WebAIM Contrast Checker: https://webaim.org/resources/contrastchecker/
- Formula is based on relative luminance of foreground vs background
- White on white = 1:1 (fail). Black on white = 21:1 (maximum contrast, pass)

**Common failures:**
- Light grey text on white background: `#999` on white = 2.85:1 (FAIL for normal text)
- Brand colours with insufficient contrast: orange text on white
- Placeholder text in form fields (always too light)

---

## Section 4 — Manual Accessibility Testing {#section-4}

### Keyboard Navigation Testing

Every interactive element must be reachable and operable using a keyboard alone. This is the most fundamental accessibility test.

**Keys to use:**

| Key | Action |
|-----|--------|
| `Tab` | Move focus forward through interactive elements |
| `Shift + Tab` | Move focus backward |
| `Enter` | Activate links, submit buttons |
| `Space` | Toggle checkboxes, activate buttons, scroll |
| `Arrow keys` | Navigate within components (radio groups, dropdowns, tabs, sliders) |
| `Escape` | Close modals, dropdowns, tooltips |
| `Home / End` | Jump to start/end of a list or text field |

**What to verify:**
1. Every link, button, form field, dropdown, and interactive widget is reachable by Tab
2. Tab order is logical (follows visual reading order — top to bottom, left to right)
3. Focus is never trapped in a component (unless it is a modal — modals SHOULD trap focus)
4. Focus returns to the trigger element when a modal or dropdown is closed
5. Every focused element has a **visible focus indicator** (a ring, outline, or highlight)
6. Skip navigation link appears as the very first Tab stop ("Skip to main content")

**Test procedure:**
1. Place cursor in browser address bar
2. Press Tab to move into the page
3. Tab through every interactive element on the page
4. Verify you can see where focus is at all times
5. Activate each element with Enter or Space
6. Close any modals with Escape and verify focus returns to the triggering element
7. At the bottom of the page, verify Tab wraps correctly

---

### Screen Reader Testing

Screen readers convert on-screen content to synthesized speech or Braille output. Testing with a screen reader reveals whether content that is visually clear is also programmatically clear.

**Key Screen Readers:**

| Screen Reader | Platform | Cost | Market Share |
|--------------|----------|------|-------------|
| **NVDA** (NonVisual Desktop Access) | Windows | Free (open source) | ~40% |
| **JAWS** (Job Access With Speech) | Windows | Paid (~$1000/year) | ~40% |
| **VoiceOver** | macOS / iOS (built-in) | Free | ~10% |
| **TalkBack** | Android (built-in) | Free | ~6% |
| **Narrator** | Windows (built-in) | Free | Minor |

**NVDA Quick Start (Windows):**
1. Download from nvaccess.org (free)
2. Install and launch
3. NVDA + N opens the NVDA menu
4. Press Tab to move through elements (NVDA announces each)
5. NVDA + Down Arrow reads the current line
6. H jumps between headings (in Browse Mode)
7. B jumps between buttons
8. F jumps between form fields

**What to verify with a screen reader:**
- Every image is announced with meaningful alt text (not "image.jpg" or the filename)
- Form fields announce their label when focused
- Buttons announce their accessible name ("Submit" not "button")
- Error messages are announced when they appear
- Modal dialogs are announced and focus moves into them
- Loading states are announced ("Loading..." or "Content updated")
- Table headers are announced when navigating table cells
- The page title is announced when the page loads

---

### Colour Contrast Testing

**Manual check with browser devtools:**
1. Open Chrome DevTools (F12)
2. Select the Inspector (Elements tab)
3. Click the colour swatch next to a `color` CSS property
4. The colour picker shows the contrast ratio automatically
5. A green tick = pass. A yellow warning = fail.

**Tools for colour contrast:**
- **WebAIM Contrast Checker**: https://webaim.org/resources/contrastchecker/
- **WAVE browser extension**: highlights contrast failures visually
- **Chrome DevTools**: built-in contrast ratio in colour picker
- **Colour Contrast Analyser** (app): eyedropper tool for any on-screen colour

**What to check:**
- Body text against its background
- Link text against its background (default and hover states)
- Button text against button background
- Placeholder text in form fields (often fails — very light grey)
- Error text against its background
- Text on images or gradients (pick the worst-case background colour)

---

### Focus Indicators

Every element that can receive keyboard focus must have a clearly visible focus indicator. This is required by WCAG 2.4.7 (AA) and the enhanced 2.4.11 (AA in WCAG 2.2).

**What a good focus indicator looks like:**
- Visible outline around the element (browser default: blue ring)
- Sufficient contrast between the focus outline and surrounding area (3:1 minimum in WCAG 2.2)
- Not removed with `outline: none` in CSS without a replacement

**Common failure:**
```css
/* This is the most common accessibility failure in CSS */
:focus {
  outline: none; /* WCAG failure — removes visibility for keyboard users */
}

/* Correct approach — replace with a custom visible style */
:focus {
  outline: 3px solid #005FCC;
  outline-offset: 2px;
}
```

**Test procedure:** Tab through the page and confirm every interactive element has a visible, high-contrast focus ring.

---

### Alt Text on Images

Every meaningful image must have descriptive alternative text. Decorative images should have empty alt text (`alt=""`) so screen readers skip them.

| Image Type | Alt Text Required | Example |
|------------|------------------|---------|
| Informative image | Yes — describe the content | `alt="Bar chart showing student progress from 60% to 85% over 8 weeks"` |
| Functional image (button/link) | Yes — describe the action | `alt="Search"` on a magnifying glass icon button |
| Decorative image | No — use empty alt | `alt=""` on a background illustration |
| Image of text | Yes — the text itself | `alt="Welcome to EduApp"` on a logo |
| Complex image (chart, diagram) | Yes + long description | Short alt + longer description in surrounding text |

**Bad examples:**
- `alt="image.png"` — filename is not a description
- `alt="photo"` — generic, meaningless
- `alt="click here"` — describes the action, not the content
- Missing alt attribute entirely — screen reader reads the filename

---

### Form Labels

Every form input must have an associated label. The label must be:
- Programmatically associated (using `<label for="...">` or `aria-label` or `aria-labelledby`)
- Visible (not just placeholder text — placeholder disappears when typing)
- Persistent (does not disappear when user starts typing)

**Correct approach:**
```html
<!-- Method 1: label with for attribute -->
<label for="email">Email address</label>
<input type="email" id="email" name="email" />

<!-- Method 2: label wrapping the input -->
<label>
  Email address
  <input type="email" name="email" />
</label>

<!-- Method 3: aria-label (when visual label is not possible) -->
<input type="search" aria-label="Search courses" />
```

**Wrong approach:**
```html
<!-- Placeholder only — disappears when typing, not announced as label -->
<input type="email" placeholder="Email address" />

<!-- div instead of label — not programmatically associated -->
<div>Email address</div>
<input type="email" />
```

---

### Error Messages

Error messages must:
1. **Identify the field in error** — not just "please fix the errors"
2. **Describe what is wrong** — not just "invalid input"
3. **Suggest how to fix it** — where possible
4. **Be announced to screen readers** — added to the DOM in a way assistive technology will read

**Good error message:** "Email address is required — please enter your email in the format name@example.com"

**Bad error message:** "Error" / "Invalid" / "Please try again"

---

### Heading Structure

Headings create the page outline. Screen reader users navigate by headings — they press H to jump between them. A logical heading structure is essential.

**Rules:**
- One and only one `<h1>` per page (the page title)
- Never skip levels: do not go from `<h2>` directly to `<h4>`
- Headings convey hierarchy — not visual style (use CSS for styling)
- Headings should describe the content of the section that follows

**Good structure:**
```
H1: Online Learning Platform
  H2: Your Courses
    H3: Mathematics — Year 10
    H3: Science — Year 11
  H2: Your Progress
    H3: This Week
    H3: This Month
```

**Bad structure:**
```
H1: Online Learning Platform
  H3: Your Courses  ← skipped H2
    H1: Mathematics  ← second H1, wrong hierarchy
```

---

### Link Text

Link text must make sense out of context. Screen reader users navigate by pressing L to jump between links — they hear only the link text, not the surrounding paragraph.

| Bad Link Text | Good Link Text |
|---------------|----------------|
| "Click here" | "Download the Year 10 Mathematics workbook (PDF)" |
| "Read more" | "Read more about the Year 10 curriculum" |
| "Here" | "View the student progress report" |
| "Link" | (never use "link" — screen readers already announce "link") |
| URL as text | (avoid unless URL is actually the content) |

---

## Section 5 — Automated Accessibility Testing {#section-5}

### The Limits of Automation

**Automated accessibility testing finds approximately 30–40% of WCAG issues.** The rest require manual testing and human judgement. This is because many accessibility requirements involve semantic meaning, reading order, screen reader experience, and usability — none of which can be reliably assessed by a static code analysis tool.

| Automated tools CAN detect | Manual testing required for |
|---------------------------|----------------------------|
| Missing alt text | Meaningful vs meaningless alt text |
| Missing form labels | Logical reading order |
| Low contrast ratios | Screen reader announcement quality |
| Missing page title | Focus management in complex interactions |
| Invalid ARIA attributes | Cognitive load and usability |
| Missing document language | Keyboard trap situations |
| Duplicate IDs | Error announcement timing |

---

### axe-core

The most widely used open-source accessibility rules engine. Developed by Deque Systems. Powers browser extensions, Playwright plugins, Cypress plugins, and CI tools.

```bash
npm install --save-dev axe-core
```

axe-core tests against WCAG 2.0, 2.1, 2.2 (A and AA), Section 508, and best practice rules. It categorises results by impact: critical, serious, moderate, minor.

---

### Playwright + axe

```bash
npm install --save-dev @axe-core/playwright
```

Integration: axe-core runs inside the Playwright browser context and returns a detailed violations object.

---

### Cypress + axe

```bash
npm install --save-dev cypress-axe axe-core
```

In `cypress/support/commands.js`:
```javascript
import 'cypress-axe';
```

In your test:
```javascript
cy.visit('/login');
cy.injectAxe();
cy.checkA11y();
```

---

### Lighthouse CLI

Google Lighthouse runs a suite of accessibility audits powered by axe-core plus additional checks.

```bash
npx lighthouse https://example.com --only-categories=accessibility --output html --output-path ./lighthouse-report.html
```

Lighthouse produces a score out of 100 and a detailed report. It is useful for executive reporting ("our accessibility score is 94/100") but the score does not directly map to WCAG conformance.

---

### WAVE Browser Extension

WAVE (Web Accessibility Evaluation Tool) by WebAIM provides a visual overlay of accessibility issues directly on the page.

**How to use:**
1. Install the WAVE extension for Chrome or Firefox (wave.webaim.org)
2. Navigate to the page you want to test
3. Click the WAVE icon in the browser toolbar
4. A sidebar opens with:
   - **Errors** (red icons): must fix — clear WCAG failures
   - **Alerts** (yellow icons): potential issues — need manual review
   - **Features** (green icons): accessibility features present
   - **Structural elements** (blue icons): headings, regions, lists
5. Click any icon on the page to see the exact HTML and which WCAG criterion it relates to

---

## Section 6 — Playwright Accessibility Test Example {#section-6}

### Full Working Code Examples

```typescript
import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

// ============================================================
// TEST 1: Full page WCAG AA compliance check
// ============================================================
test('home page has no WCAG AA violations', async ({ page }) => {
  await page.goto('/');
  
  const results = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa'])
    .analyze();
  
  expect(results.violations).toEqual([]);
});

// ============================================================
// TEST 2: Scoped check on a specific component
// ============================================================
test('login page specific elements are accessible', async ({ page }) => {
  await page.goto('/login');
  
  const results = await new AxeBuilder({ page })
    .include('#login-form')
    .withTags(['wcag2aa'])
    .analyze();
  
  // Log violations for debugging
  if (results.violations.length > 0) {
    console.log(JSON.stringify(results.violations, null, 2));
  }
  
  expect(results.violations).toHaveLength(0);
});

// ============================================================
// TEST 3: Exclude known third-party widgets with violations
// ============================================================
test('checkout page is accessible excluding third-party payment widget', async ({ page }) => {
  await page.goto('/checkout');
  
  const results = await new AxeBuilder({ page })
    .exclude('#payment-iframe') // Third-party widget outside our control
    .withTags(['wcag2aa'])
    .analyze();
  
  expect(results.violations).toEqual([]);
});

// ============================================================
// TEST 4: Dynamic content — test after state change
// ============================================================
test('error messages are accessible after form submission failure', async ({ page }) => {
  await page.goto('/login');
  
  // Submit form with empty fields to trigger errors
  await page.click('button[type="submit"]');
  
  // Wait for error messages to appear
  await page.waitForSelector('[role="alert"]');
  
  // Now test accessibility of the error state
  const results = await new AxeBuilder({ page })
    .withTags(['wcag2aa'])
    .analyze();
  
  if (results.violations.length > 0) {
    // Print a readable summary for the test output
    const violationSummary = results.violations.map(v => ({
      id: v.id,
      impact: v.impact,
      description: v.description,
      nodes: v.nodes.map(n => n.html).slice(0, 3)
    }));
    console.log('Violations found:', JSON.stringify(violationSummary, null, 2));
  }
  
  expect(results.violations).toEqual([]);
});

// ============================================================
// TEST 5: Keyboard navigation test
// ============================================================
test('user can navigate login form using keyboard only', async ({ page }) => {
  await page.goto('/login');
  
  // Start from URL bar equivalent — press Tab to enter page
  await page.keyboard.press('Tab');
  
  // First Tab stop should be skip navigation link
  const skipLink = page.locator(':focus');
  await expect(skipLink).toHaveText(/skip to main content/i);
  
  // Tab to email field
  await page.keyboard.press('Tab');
  const emailField = page.locator(':focus');
  await expect(emailField).toHaveAttribute('type', 'email');
  
  // Type in email field
  await page.keyboard.type('user@example.com');
  
  // Tab to password field
  await page.keyboard.press('Tab');
  await page.keyboard.type('Password123!');
  
  // Tab to submit button
  await page.keyboard.press('Tab');
  const submitButton = page.locator(':focus');
  await expect(submitButton).toHaveAttribute('type', 'submit');
  
  // Submit using Enter
  await page.keyboard.press('Enter');
  
  // Verify navigation (successful login redirects to dashboard)
  await expect(page).toHaveURL('/dashboard');
});

// ============================================================
// TEST 6: Color contrast check (via axe)
// ============================================================
test('all text meets WCAG AA colour contrast requirements', async ({ page }) => {
  await page.goto('/');
  
  const results = await new AxeBuilder({ page })
    .withRules(['color-contrast'])
    .analyze();
  
  if (results.violations.length > 0) {
    console.log('Contrast failures:', JSON.stringify(
      results.violations[0].nodes.map(n => ({
        element: n.html,
        data: n.any[0]?.data
      })), null, 2
    ));
  }
  
  expect(results.violations).toHaveLength(0);
});
```

### playwright.config.ts for Accessibility Tests

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests/accessibility',
  use: {
    baseURL: 'http://localhost:3000',
    // Run accessibility tests in Chrome only — axe behaviour is consistent
    ...devices['Desktop Chrome'],
  },
  reporter: [
    ['html', { outputFolder: 'accessibility-report' }],
    ['list'],
  ],
});
```

---

## Section 7 — Common Accessibility Bugs QA Finds {#section-7}

### 1. No Alt Text on Meaningful Images

**What it is:** An `<img>` element with no `alt` attribute, or with `alt="image.png"` (the filename).

**How to find it:** WAVE extension (red error), axe scan (`image-alt` rule), or manually inspect `<img>` tags.

**Severity:** Critical (WCAG 1.1.1 Level A)

**How to raise:** "Image on the student progress page has no alt text. Screen reader users cannot understand the content. Expected: descriptive alt text describing what the chart shows. Actual: alt attribute is absent."

---

### 2. Low Colour Contrast

**What it is:** Text colour and background colour do not have sufficient contrast ratio (below 4.5:1 for normal text).

**How to find it:** axe scan (`color-contrast` rule), WebAIM contrast checker, Chrome DevTools.

**Severity:** Serious (WCAG 1.4.3 Level AA)

**How to raise:** "Help text '#9e9e9e' on white '#ffffff' has a contrast ratio of 2.85:1. WCAG AA requires 4.5:1. Low-vision users cannot read this text. Expected: contrast ratio ≥ 4.5:1."

---

### 3. No Visible Keyboard Focus Indicator

**What it is:** CSS `outline: none` removes the default focus ring with no replacement. Keyboard users cannot see where they are on the page.

**How to find it:** Tab through the page — does focus visually appear on each element?

**Severity:** Serious (WCAG 2.4.7 Level AA)

**How to raise:** "Navigation links have no visible focus indicator. When tabbing through the site, focused links are not visually distinguished. Keyboard-only users cannot determine their location on the page."

---

### 4. Form Field Has No Label

**What it is:** An `<input>` element with no `<label>`, no `aria-label`, and no `aria-labelledby`. Screen readers announce only "edit text" with no context.

**How to find it:** axe scan (`label` rule), WAVE extension, or manually navigate form with screen reader.

**Severity:** Critical (WCAG 1.3.1 Level A, 4.1.2 Level A)

**How to raise:** "Email field on the registration form has no programmatic label. Screen reader announces 'edit text' with no description. Expected: label 'Email address' announced when field receives focus."

---

### 5. Error Message Not Announced to Screen Reader

**What it is:** An error message is displayed visually but the DOM element is not in an ARIA live region, so screen reader users are not notified.

**How to find it:** Submit a form with errors while using NVDA — does the screen reader announce the error?

**Severity:** Serious (WCAG 3.3.1 Level A)

**How to raise:** "After submitting the login form with invalid credentials, the error message 'Invalid email or password' is displayed visually but not announced by screen readers. Expected: error message announced via ARIA live region or focus moved to error."

---

### 6. Modal Dialog Lacks Focus Trap

**What it is:** When a modal opens, keyboard focus can escape the modal to the background content. Users Tab into content that should be hidden.

**How to find it:** Open a modal and Tab through it — does focus stay within the modal?

**Severity:** Serious (WCAG 2.1.2 Level A)

**How to raise:** "The 'Add student' modal does not trap keyboard focus. Pressing Tab past the last focusable element in the modal moves focus to the background page. Expected: focus cycles within the modal until it is dismissed with Escape."

---

### 7. Skip Navigation Link Missing

**What it is:** No "Skip to main content" link at the top of the page. Keyboard users must Tab through the entire navigation on every page.

**How to find it:** Press Tab as the first action on any page — is the first Tab stop a skip link?

**Severity:** Moderate (WCAG 2.4.1 Level A)

**How to raise:** "No skip navigation link is present on any page. Keyboard-only users must Tab through 22 navigation links before reaching main content on every page load. Expected: 'Skip to main content' link as the first Tab stop."

---

### 8. Auto-Playing Video with Sound

**What it is:** A video or audio element that starts playing automatically when the page loads, with no ability to stop it within 3 seconds.

**How to find it:** Load the page — does audio play without user action?

**Severity:** Serious (WCAG 1.4.2 Level A)

**How to raise:** "The hero video on the home page auto-plays with audio. Screen reader users cannot hear the screen reader over the video audio. Expected: video is muted by default, or a pause control is provided at the start of the content."

---

### 9. Session Timeout With No Warning

**What it is:** Session expires silently. The user is redirected to login with no advance warning, losing any unsaved work.

**How to find it:** Leave the application idle for the session timeout period.

**Severity:** Serious (WCAG 2.2.1 Level A)

**How to raise:** "After 15 minutes of inactivity, the session expires and the user is redirected to login without any warning. Expected: a warning dialog at least 20 seconds before expiry, giving the user the ability to extend the session."

---

### 10. Image of Text Used Instead of Styled Text

**What it is:** Headings or labels are implemented as images containing text rather than HTML text with CSS styling. Screen readers cannot read it without alt text.

**How to find it:** Zoom in to 200% — does text remain sharp? Does selecting text work on the heading? Does the screen reader read it naturally?

**Severity:** Moderate (WCAG 1.4.5 Level AA)

---

### 11. Table Without Headers

**What it is:** A data table using `<table>` without `<th>` elements or `scope` attributes. Screen readers cannot associate cell data with column headers.

**How to find it:** Navigate the table with a screen reader — are column headers announced when navigating cells?

**Severity:** Serious (WCAG 1.3.1 Level A)

---

### 12. Link Opens in New Tab Without Warning

**What it is:** A link opens in a new browser tab (`target="_blank"`) without telling the user in advance. This confuses screen reader users and violates expectation.

**How to find it:** Check all links — do any open new tabs without announcement?

**Severity:** Moderate (WCAG 3.2.2 Level A)

**Fix:** Add text "(opens in new tab)" to the link text, or use an icon with appropriate aria-label.

---

### 13. Placeholder Text as Only Label

**What it is:** An input field uses only `placeholder` text to indicate what should be entered. Placeholder disappears when typing — the user forgets the field's purpose.

**How to find it:** Click into any form field — does the placeholder disappear? Is there a persistent label?

**Severity:** Serious (WCAG 1.3.1, 3.3.2)

---

### 14. Carousel or Slider Without Pause Control

**What it is:** An auto-advancing carousel with no way to pause, stop, or control it. Distracting for users with cognitive disabilities.

**How to find it:** Any auto-advancing content on the page.

**Severity:** Moderate (WCAG 2.2.2 Level A)

---

### 15. Non-descriptive Button: "Submit" Without Context

**What it is:** Multiple forms on a page all have a "Submit" button. Screen reader users navigating by buttons cannot distinguish between them.

**How to find it:** Navigate by buttons (press B in NVDA) — do button names make sense without visual context?

**Severity:** Moderate (WCAG 4.1.2)

**Fix:** Use `aria-label="Submit course enrolment form"` to add context without changing the visual label.

---

## Section 8 — All Commands for Accessibility Testing {#section-8}

```bash
# ============================================================
# INSTALLATION
# ============================================================

# Install axe-core for Playwright integration
npm install --save-dev @axe-core/playwright

# Install axe-core standalone (for custom integrations)
npm install --save-dev axe-core

# Install cypress-axe for Cypress integration
npm install --save-dev cypress-axe axe-core

# ============================================================
# LIGHTHOUSE
# ============================================================

# Run Lighthouse accessibility audit and output HTML report
npx lighthouse https://example.com --only-categories=accessibility --output html --output-path ./lighthouse-a11y.html

# Run Lighthouse against localhost
npx lighthouse http://localhost:3000 --only-categories=accessibility --output html

# Run Lighthouse with specific Chrome flags (headless)
npx lighthouse https://example.com --only-categories=accessibility --chrome-flags="--headless"

# ============================================================
# PLAYWRIGHT
# ============================================================

# Run accessibility tests in Playwright
npx playwright test tests/accessibility/ --project=chromium

# Run accessibility tests with HTML reporter
npx playwright test tests/accessibility/ --reporter=html

# Run a specific accessibility test file
npx playwright test tests/accessibility/login.spec.ts

# Run accessibility tests across all browsers
npx playwright test tests/accessibility/ --project=chromium --project=firefox --project=webkit

# ============================================================
# PA11Y (alternative CLI accessibility tool)
# ============================================================

# Install pa11y
npm install -g pa11y

# Run pa11y against a URL (WCAG2AA standard)
pa11y https://example.com --standard WCAG2AA

# Run pa11y with HTML reporter
pa11y https://example.com --standard WCAG2AA --reporter html > pa11y-report.html

# ============================================================
# AXECON CLI
# ============================================================

# Install axe CLI
npm install -g @axe-core/cli

# Run axe CLI
axe https://example.com --tags wcag2a,wcag2aa
```

---

## Section 9 — Interview Q&A {#section-9}

### Q1: What are the POUR principles of WCAG 2.1?

**Answer:**

POUR stands for Perceivable, Operable, Understandable, and Robust — the four foundational principles of WCAG 2.1.

**Perceivable** means that all information and UI components must be presentable to users in ways they can perceive. If a user cannot perceive content, they cannot use it. Examples: alt text on images, captions on video, sufficient colour contrast.

**Operable** means that all UI components and navigation must be operable. If a user cannot operate an interface, they cannot use it. Examples: keyboard accessibility, no keyboard traps, no seizure-inducing flashes.

**Understandable** means that information and operation must be understandable. Examples: clear error messages, consistent navigation, declared page language.

**Robust** means that content must be robust enough to be reliably interpreted by a wide variety of assistive technologies. Examples: valid HTML, correct ARIA usage.

---

### Q2: What are WCAG conformance levels?

**Answer:**

WCAG has three conformance levels: A, AA, and AAA.

**Level A** is the minimum. Failing it makes content inaccessible to the most users. Examples: images must have alt text, all functionality must be keyboard accessible.

**Level AA** is the practical target. Most accessibility laws (ADA, Section 508, EN 301 549) require AA conformance. AA adds requirements like colour contrast (4.5:1 for normal text), visible focus indicators, and error identification.

**Level AAA** is aspirational. Full AAA conformance is rarely achievable across an entire site — it includes requirements like sign language for video and enhanced contrast (7:1).

In practice, QA tests to WCAG 2.1 AA.

---

### Q3: How would you test accessibility without any automation tools?

**Answer:**

I test manually using:

1. **Keyboard navigation**: Tab through the entire page — can every feature be accessed and operated without a mouse?
2. **Screen reader (NVDA, free on Windows)**: Navigate with the screen reader active and verify that all meaningful content is announced correctly
3. **Browser zoom**: Zoom to 200% — does all content remain accessible and functional?
4. **Contrast checker**: Use WebAIM Contrast Checker to verify text colour against background
5. **WAVE browser extension**: A free visual overlay tool that highlights accessibility errors on the page
6. **Reading heading structure**: In NVDA, press H to jump between headings and verify the page outline is logical
7. **Checking alt text**: Inspect images in DevTools (`alt` attribute) and verify they are meaningful

Manual testing without automation is slower but reveals issues that automated tools miss — particularly around reading experience and usability.

---

### Q4: What colour contrast ratio is required for WCAG AA?

**Answer:**

For WCAG 2.1 AA:
- **Normal text** (below 18pt regular or 14pt bold): minimum **4.5:1** contrast ratio
- **Large text** (18pt+ regular or 14pt+ bold): minimum **3:1** contrast ratio
- **UI components and graphical objects**: minimum **3:1** against adjacent colours

Common tool for checking: WebAIM Contrast Checker (webaim.org/resources/contrastchecker)

A common failure is light grey text on white: `#999999` on `#ffffff` = 2.85:1 — fails AA for normal text.

---

### Q5: How do you test with a screen reader?

**Answer:**

I use NVDA on Windows (free and widely used). Basic workflow:

1. Download and install NVDA from nvaccess.org
2. Launch NVDA and open the browser
3. Navigate to the page being tested
4. Use Tab to move through interactive elements — NVDA announces each element's role, name, and state
5. Press H to jump between headings — verify the heading structure is logical
6. Press F to navigate between form fields — verify each field has a meaningful label announced
7. Press B to jump between buttons — verify all buttons have meaningful names
8. Submit a form with errors — verify the error is announced without moving focus to the address bar
9. Open a modal — verify focus moves into it and is trapped
10. Close the modal with Escape — verify focus returns to the triggering element

I specifically listen for: is the content meaningful? Does the announcement make sense without seeing the screen? Can I understand what is on the page and what I can do?

---

### Q6: What is axe-core and how do you use it in Playwright?

**Answer:**

axe-core is an open-source accessibility rules engine from Deque Systems. It runs in the browser, analyses the DOM, and returns a list of WCAG violations with details about which element failed, which WCAG criterion was violated, and the impact level.

To use it in Playwright:

```bash
npm install --save-dev @axe-core/playwright
```

In a test:

```typescript
import AxeBuilder from '@axe-core/playwright';

test('page is accessible', async ({ page }) => {
  await page.goto('/');
  const results = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa'])
    .analyze();
  expect(results.violations).toEqual([]);
});
```

The `.withTags(['wcag2aa'])` filter runs only WCAG 2.1 AA rules. The `violations` array contains everything that failed. I can use `.include()` to scope to a specific component, or `.exclude()` to skip third-party widgets.

---

### Q7: What accessibility issues do automated tools miss?

**Answer:**

Automated tools like axe-core catch approximately 30–40% of WCAG issues. They miss:

- **Meaningful vs meaningless alt text**: axe checks that alt text exists, but cannot judge whether "photo" is a meaningful description
- **Reading order**: axe does not know if the Tab order makes logical sense in context
- **Screen reader experience**: whether content sounds natural and understandable when spoken
- **Cognitive accessibility**: is content clear, simple, and consistent?
- **Complex interaction patterns**: whether a custom date picker is actually usable with a screen reader
- **Focus management**: whether focus returns to the correct place after a modal closes
- **Error announcement timing**: whether error messages are announced at the right time
- **Context-dependent issues**: issues that only appear in a specific user flow

This is why automated testing must be complemented by keyboard testing, screen reader testing, and human review.

---

### Q8: How do you incorporate accessibility testing into an Agile sprint?

**Answer:**

Accessibility should be built in throughout the sprint, not added as a final check:

**Definition of Ready (stories entering sprint):**
- Acceptance criteria include accessibility requirements
- Design mockups are reviewed for colour contrast and focus states

**During development:**
- Developers use browser extensions (axe DevTools, WAVE) during implementation
- Automated axe checks are part of the CI pipeline

**QA in the sprint:**
- I run axe-core scans on all new pages/components as part of my test cycle
- I test keyboard navigation for every new feature
- For features with significant new UI, I test with NVDA
- I raise accessibility bugs with the same priority system as functional bugs — P1 for WCAG A failures (barriers to access), P2 for WCAG AA failures

**Definition of Done:**
- No new WCAG A or AA violations introduced
- Keyboard navigation verified for new interactive elements
- Screen reader tested for significant UI changes

This integrates accessibility into the quality gate rather than treating it as an afterthought or a separate project.

---

*End of Accessibility Testing Complete Guide*
