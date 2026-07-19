# Accessibility & WCAG Testing — Complete Interview Q&A Guide

> Senior QA Interview Preparation | Every concept answered as a real interview question

---

## SECTION 1 — What is Accessibility Testing?

**Q1: What is accessibility testing? Why is it important — both legally and ethically?**

**A:** Accessibility testing is the practice of verifying that a software application can be used by people with disabilities. This includes users who rely on screen readers (blind users), keyboard-only navigation (motor-impaired users), high-contrast mode or magnification (low-vision users), captions (deaf or hard-of-hearing users), and simplified interfaces (users with cognitive disabilities).

**Why it matters — the ethical case:**

Digital products are increasingly the primary channel for accessing education, employment, healthcare, government services, and social connection. If a product excludes users with disabilities, it excludes them from opportunities that are available to everyone else. In EdTech specifically: if a student with a visual impairment cannot access an online assessment or a student with a motor impairment cannot submit an assignment, they are not just inconvenienced — they are excluded from education. That is an ethical failure that QA has a direct role in preventing.

**Why it matters — the legal case:**

| Law / Standard | Region | Who Must Comply |
|---------------|--------|-----------------|
| WCAG 2.1 AA | Global (de facto standard) | Effectively all public-facing digital products |
| ADA (Americans with Disabilities Act) | United States | Public-facing websites and apps (courts have ruled digital = place of public accommodation) |
| Section 508 | United States | Federal agencies and their contractors |
| EN 301 549 | European Union | Public sector bodies; required for EU procurement |
| Equality Act 2010 | United Kingdom | All service providers |
| AODA | Ontario, Canada | Public and private sector organisations |
| DDA | Australia | All service providers |

**Legal consequences of failure:** Target, Domino's Pizza, Netflix, and numerous universities have faced lawsuits for accessibility failures. Domino's case reached the US Supreme Court (2019), which ruled ADA applies to websites. Settlements are expensive; reputational damage is lasting.

**The QA argument:** Accessibility bugs caught during development cost 1x to fix. Bugs caught after release cost 6-10x. Bugs that trigger lawsuits cost orders of magnitude more. Accessibility testing is risk management as much as it is quality assurance.

---

**Q2: What is WCAG? Who created it and why?**

**A:** WCAG stands for Web Content Accessibility Guidelines. It is the internationally recognised technical standard for making web content accessible to people with disabilities.

**Who created it:** WCAG is published by the W3C (World Wide Web Consortium), specifically by the WAI (Web Accessibility Initiative) working group. The W3C is the international standards body that governs web technologies — the same body that standardises HTML, CSS, and HTTP.

**Why it was created:** As the web became central to daily life in the 1990s, it became clear that the same technology enabling mass access for most people was creating new barriers for people with disabilities. Tim Berners-Lee, who invented the web, famously said: "The power of the web is in its universality. Access by everyone regardless of disability is an essential aspect." WCAG was created to define what "accessible" concretely means so developers, designers, and testers could build to a shared, verifiable standard.

**Version history:**
- WCAG 1.0 (1999) — first version, HTML-focused
- WCAG 2.0 (2008) — technology-agnostic, works for HTML, PDF, mobile
- WCAG 2.1 (2018) — adds 17 new success criteria, particularly for mobile and cognitive accessibility
- WCAG 2.2 (2023) — adds 9 more criteria, particularly enhanced focus visibility and target size
- WCAG 3.0 — in development (fundamental rethink of the scoring model)

**What QA tests against today:** WCAG 2.1 Level AA is the current practical standard. Most accessibility laws globally reference WCAG 2.1 AA. Some organisations are also testing against WCAG 2.2 AA, which adds important keyboard and focus requirements.

---

## SECTION 2 — Conformance Levels

**Q3: What are the WCAG conformance levels — A, AA, and AAA? Which do most companies target?**

**A:** WCAG is organised into three conformance levels. Each level adds requirements on top of the previous one.

**Level A — Minimum Accessibility**

Level A requirements prevent the most severe barriers — situations where users with disabilities simply cannot access content at all.

Examples of Level A criteria:
- Every image must have an `alt` attribute (WCAG 1.1.1)
- All functionality must be accessible via keyboard — no mouse required (WCAG 2.1.1)
- Colour must not be the only means of conveying information (WCAG 1.4.1)
- Pages must have a descriptive title (WCAG 2.4.2)
- Videos must have captions (WCAG 1.2.2)
- Users must not be trapped in a component by keyboard focus (WCAG 2.1.2)

**Status:** Non-negotiable floor. Failure to meet Level A means some users cannot use the product at all.

**Level AA — Recommended Standard**

Level AA includes everything in Level A, plus additional requirements that address a broader range of barriers. This is the target for almost all commercial products, and the level required by most accessibility laws globally.

Examples of AA criteria beyond Level A:
- Colour contrast: 4.5:1 for normal text, 3:1 for large text (WCAG 1.4.3)
- Text can be resized to 200% without loss of content or functionality (WCAG 1.4.4)
- Focus is visible on all keyboard-interactive elements (WCAG 2.4.7)
- Error messages identify the field in error and describe what is wrong (WCAG 3.3.1, 3.3.3)
- Navigation is consistent across pages (WCAG 3.2.3)
- Images of text are not used for styling (WCAG 1.4.5)
- Multiple pathways to navigate (search, sitemap) (WCAG 2.4.5)

**Level AAA — Aspirational**

Level AAA adds the most demanding requirements. Full AAA conformance across an entire site is considered impractical for most organisations — not because organisations do not care, but because some AAA requirements are impossible to apply consistently to all content types.

Examples of AAA criteria:
- Sign language interpretation provided for all audio content (WCAG 1.2.6)
- Enhanced contrast ratio: 7:1 for normal text (WCAG 1.4.6)
- No timing restrictions at all (WCAG 2.2.3)
- Pronunciation guide for unusual words (WCAG 3.1.6)

**What most companies target:** WCAG 2.1 Level AA. QA tests to this standard. Level A is the baseline non-negotiable. Level AAA is aspirational and applied where practical.

---

## SECTION 3 — POUR Principles

**Q4: What is POUR? Explain each principle and give a real example of what can fail.**

**A:** POUR is the acronym for the four foundational principles of WCAG 2.1. Every WCAG success criterion belongs to one of these four principles. If content violates any principle, it is not accessible.

**P — Perceivable:** Users must be able to perceive all information and UI components. If information exists only in a form a user cannot perceive, it is inaccessible.

**O — Operable:** Users must be able to operate all UI components and navigation. If a user cannot trigger an action, they cannot use that feature.

**U — Understandable:** Information and the operation of the interface must be understandable. Technically accessible content that is confusing or unclear is still a barrier.

**R — Robust:** Content must be robust enough to be interpreted reliably by a wide variety of current and future user agents, including assistive technologies.

Full detail on each principle follows in Q5–Q8.

---

**Q5: What is Perceivable? What accessibility issues fall under it?**

**A:** Perceivable means that all information — content, UI labels, status messages, error messages, images, videos — must be available in a form that the user can perceive, regardless of their sensory ability.

A user who is blind cannot see an image. If the image conveys meaningful information and has no text alternative, that user is denied information that a sighted user receives. That is a Perceivable failure.

**Core Perceivable requirements and what can fail:**

| Requirement | WCAG Criterion | What Fails |
|------------|---------------|-----------|
| Text alternatives for images | 1.1.1 (Level A) | `<img>` with no `alt`, or `alt="photo.jpg"` (filename), or `alt="image"` (meaningless) |
| Captions for video | 1.2.2 (Level A) | Video content with no closed captions, or auto-generated captions that are inaccurate |
| Audio descriptions | 1.2.5 (Level AA) | Video showing important visual information (a diagram being drawn) with no audio description of what is happening |
| Colour is not the only signal | 1.4.1 (Level A) | Form error shown in red only — no icon, no text, no border — colour-blind users see no error indicator |
| Sufficient contrast | 1.4.3 (Level AA) | Light grey `#999` on white `#fff` = 2.85:1 contrast ratio — fails for normal text (need 4.5:1) |
| Text resize to 200% | 1.4.4 (Level AA) | Zoom to 200% and content overlaps, is cut off, or disappears |
| No images of text | 1.4.5 (Level AA) | Page headings are JPEG images of text instead of HTML + CSS styling — zoom makes them blurry, screen readers cannot read them naturally |

**Real bug example:** An online quiz marks incorrect answers in red and correct answers in green. There are no icons, no text labels, no border changes. A student with red-green colour blindness (8% of males, 0.5% of females) sees all answers in the same shade of grey. They cannot tell what they got right or wrong. This is a WCAG 1.4.1 (Level A) failure — colour is the only means of conveying information.

---

**Q6: What is Operable? What do you test for keyboard operability?**

**A:** Operable means that every action a user can take with a mouse must also be achievable with a keyboard alone, and more broadly that the interface does not create barriers for users with motor or physical disabilities.

Users who cannot use a mouse — due to tremors, paralysis, limb differences, or temporary injuries — rely entirely on a keyboard. Some use switch access devices with even fewer input options. If a button, dropdown, form, or navigation item is only reachable by clicking, these users are blocked.

**Core Operable requirements:**

| Requirement | WCAG Criterion | What Fails |
|------------|---------------|-----------|
| Keyboard accessible | 2.1.1 (Level A) | A dropdown opens only on mouse hover — Tab key cannot reach it |
| No keyboard trap | 2.1.2 (Level A) | Focus enters a widget and cannot escape; pressing Tab does nothing |
| No seizure-triggering content | 2.3.1 (Level A) | Content flashes more than 3 times per second |
| Skip navigation | 2.4.1 (Level A) | No "Skip to main content" link — keyboard users must Tab through 20+ nav items on every page |
| Page has a title | 2.4.2 (Level A) | Browser tab reads "Untitled" or the site name with no page-specific context |
| Focus order | 2.4.3 (Level A) | Tab order jumps to the bottom of the page before reaching mid-page content |
| Focus visible | 2.4.7 (Level AA) | Interactive elements show no visual focus indicator when focused by keyboard |
| Sufficient time | 2.2.1 (Level A) | Session expires after 15 minutes with no warning and no ability to extend |

**Keyboard operability testing — what specifically to test:**

1. Can I reach every interactive element using Tab (forward) and Shift+Tab (backward)?
2. Can I activate buttons with Enter and Space?
3. Can I navigate within components (radio groups, dropdown options, tab panels) with Arrow keys?
4. Can I close modals and dropdowns with Escape?
5. Does Tab order follow the visual reading order (top to bottom, left to right)?
6. Does focus never get trapped in a component (except intentionally in modals)?
7. When I open a modal, does focus move into it?
8. When I close a modal, does focus return to the element that opened it?
9. Is there a "Skip to main content" link as the very first Tab stop?
10. Is the focused element clearly visible at all times?

---

**Q7: What is Understandable? What makes content understandable?**

**A:** Understandable means that users can understand both the content on the page and how to operate the interface. Even if content is perceivable and the interface is operable, it must also make sense to the user.

An error message that says "Error: 422" is technically visible — it is perceivable. But it is not understandable. A user with a cognitive disability, or even a non-technical user, cannot act on it. Understandable requires that information be communicated in a way users can comprehend and act upon.

**Core Understandable requirements:**

| Requirement | WCAG Criterion | What Fails |
|------------|---------------|-----------|
| Language of page | 3.1.1 (Level A) | `<html>` has no `lang` attribute — screen readers cannot select the correct pronunciation dictionary |
| Language of parts | 3.1.2 (Level AA) | A French phrase in an English page has no `lang="fr"` — pronounced with English phonemes by screen reader |
| No unexpected context changes | 3.2.1, 3.2.2 (Level A) | Selecting an option in a dropdown automatically navigates to a new page without any user confirmation |
| Consistent navigation | 3.2.3 (Level AA) | The main navigation is in a different location on different pages |
| Error identification | 3.3.1 (Level A) | Form submitted with errors — no indication of which field is wrong |
| Error suggestion | 3.3.3 (Level AA) | Error message says "Invalid" with no guidance on what format is expected |
| Labels visible and persistent | 3.3.2 (Level A) | Input uses only placeholder text as its label — placeholder disappears when typing begins |

**Good vs bad error message examples:**

Bad: "Error" / "Invalid input" / "Please try again"

Good: "Email address is required — please enter your email in the format name@example.com"

Good error messages answer: Which field? What is wrong? How do I fix it?

**Real bug example:** A registration form's "Date of Birth" field expects `DD/MM/YYYY` but the placeholder only shows "Date of Birth". When a user enters `1990-01-15`, the form rejects it with "Invalid date format". This fails WCAG 3.3.3 (Error Suggestion, Level AA) because the error does not tell the user what format is expected.

---

**Q8: What is Robust? What does it mean for assistive technology compatibility?**

**A:** Robust means that content is built in a way that can be reliably interpreted by current and future assistive technologies. As browsers and screen readers evolve, robustly built content continues to work correctly.

The core of Robustness is well-formed, semantically correct HTML with proper ARIA usage. When developers use HTML as intended — headings for headings, buttons for buttons, links for links — assistive technologies can interpret and convey that structure correctly.

**Core Robust requirements:**

| Requirement | WCAG Criterion | What Fails |
|------------|---------------|-----------|
| Valid HTML | 4.1.1 (Level A) | Unclosed tags, duplicate IDs, improperly nested elements — these cause screen reader parsing errors |
| Name, role, value | 4.1.2 (Level A) | A custom `<div>` button has no `role="button"`, no `aria-label`, and no keyboard event listener — screen reader announces "group" instead of "button", and Enter does nothing |
| Status messages | 4.1.3 (Level AA) | A loading spinner appears but is not in an ARIA live region — screen reader users have no idea the system is working |

**The most common Robust failure:**

Developers building custom UI components (dropdowns, modals, date pickers, accordions, tabs) using generic HTML elements (`<div>`, `<span>`) without adding the ARIA attributes that expose their role, name, and state to assistive technology.

**Example:**
```html
<!-- WRONG: A div pretending to be a button -->
<div class="btn-primary" onclick="submitForm()">Submit</div>
<!-- Screen reader announces: "Submit" (as generic text or group, not button)
     Keyboard users: Enter key does nothing because divs are not focusable
     Tab key: element is not in the focus order -->

<!-- RIGHT: An actual button -->
<button type="submit">Submit</button>
<!-- Screen reader announces: "Submit, button"
     Keyboard: focusable, Enter/Space activates it
     Role, name, state: all automatically provided by the browser -->
```

---

## SECTION 4 — Colour Contrast

**Q9: What is colour contrast ratio? What are the requirements — 4.5:1, 3:1 — and for what types of content?**

**A:** Colour contrast ratio is a mathematical measure of the difference in luminance (brightness) between two colours — typically foreground (text) and background. It ranges from 1:1 (identical colours — no contrast) to 21:1 (black text on white — maximum contrast).

The ratio is calculated from the relative luminance values of the two colours using a formula defined in WCAG. You do not need to calculate this manually — tools do it for you.

**WCAG AA contrast requirements:**

| Content Type | Minimum Ratio (AA) | Enhanced Ratio (AAA) |
|-------------|------------------|--------------------|
| Normal text (less than 18pt regular or 14pt bold) | **4.5:1** | 7:1 |
| Large text (18pt+ regular, or 14pt+ bold) | **3:1** | 4.5:1 |
| UI components (button borders, input borders, icons) | **3:1** | Not specified |
| Disabled elements | No requirement | No requirement |
| Decorative images | No requirement | No requirement |
| Logotypes | No requirement | No requirement |

**Common contrast failures:**

| Foreground | Background | Ratio | Verdict |
|-----------|-----------|-------|---------|
| `#767676` grey | `#ffffff` white | 4.54:1 | Barely passes AA for normal text |
| `#999999` grey | `#ffffff` white | 2.85:1 | Fails AA for normal text — extremely common mistake for placeholder text and help text |
| `#FF8C00` orange | `#ffffff` white | 2.63:1 | Fails — brand orange is commonly used as button text |
| `#000000` black | `#ffffff` white | 21:1 | Maximum contrast — always passes |
| `#0057A3` dark blue | `#ffffff` white | 7.65:1 | Passes both AA and AAA |

**Tools to check contrast:**

- WebAIM Contrast Checker: webaim.org/resources/contrastchecker — paste hex values, get instant ratio and pass/fail
- Chrome DevTools: In the Elements panel, click the colour swatch next to a CSS `color` property — the picker shows the contrast ratio with a tick (pass) or warning (fail)
- WAVE browser extension: highlights contrast failures visually on the page
- Colour Contrast Analyser (Paciello Group): a desktop app with an eyedropper for any on-screen colour

**Key insight for EdTech:** Placeholder text in form fields almost always fails WCAG contrast. Browsers render placeholder text in a light grey that is intentionally subtle. Always check placeholder text contrast — and note that placeholder text also fails for other reasons (it disappears when typing, it is not a label). Double failure.

---

## SECTION 5 — Manual Keyboard Testing

**Q10: How do you manually test keyboard accessibility step by step?**

**A:** Keyboard accessibility testing is the single most fundamental manual accessibility check. If you have time for only one accessibility test, this is it.

**Step-by-step procedure:**

1. **Close all other applications that might intercept keyboard shortcuts.**

2. **Place focus in the browser address bar** by clicking it with the mouse (this is the last time you use the mouse during this test).

3. **Press Tab to move focus into the page.** The very first Tab stop should be a "Skip to main content" link. If it is not, that is WCAG 2.4.1 failure (Level A).

4. **Continue pressing Tab.** For every element that receives focus, verify:
   - Is the focused element visually highlighted? (Focus indicator visible — WCAG 2.4.7)
   - Does the tab order make logical sense? (Top to bottom, left to right — WCAG 2.4.3)
   - Is this element interactive? (Non-interactive text, decorative images, and containers should NOT receive focus)

5. **Test activation.** For every interactive element that receives focus:
   - Press Enter to activate links and buttons
   - Press Space to activate buttons and toggle checkboxes
   - Press Arrow keys to move within components (radio groups, select dropdowns, tabs, sliders)

6. **Test complex components:**
   - Dropdown menu: can you open it with Enter, navigate options with Arrow keys, select with Enter, close with Escape?
   - Modal dialog: when it opens, does focus move into it? Can you Tab through everything inside? Does Tab NOT escape to the background? Does Escape close it and return focus to the trigger?
   - Date picker: can you enter a date without using the calendar widget (keyboard users often prefer direct typing)?
   - Custom select/combobox: announced as a combobox? Options reachable? Current selection announced?

7. **Test at the bottom of the page.** When you Tab past the last interactive element, does focus wrap back to the top or to the skip link? Does focus ever disappear entirely?

8. **Document every issue.** Note the element, what you expected, what happened, and the WCAG criterion violated.

---

**Q11: What specific keys do you use in keyboard navigation testing?**

**A:**

| Key | What It Does | What to Test |
|-----|-------------|-------------|
| `Tab` | Move focus forward to next interactive element | Every interactive element must be reachable |
| `Shift + Tab` | Move focus backward | Reverse navigation must also be logical |
| `Enter` | Activate links; submit buttons; open dropdowns | Must trigger the same action as a mouse click |
| `Space` | Activate buttons; toggle checkboxes; scroll page | Buttons must respond to Space, not just Enter |
| `Arrow keys` | Navigate within components (radio groups, dropdowns, sliders, tab panels, carousels) | Inside-component navigation — separate from page-level Tab navigation |
| `Escape` | Close modals, dropdowns, tooltips, autocomplete suggestions | Every dismissible overlay must respond to Escape |
| `Home` | Move to first option in a list or start of input | Useful in custom list components |
| `End` | Move to last option in a list or end of input | Useful in custom list components |
| `Page Up / Page Down` | Scroll the page when focus is on the page | Should not be captured by a component unless it is a scrollable region |
| `Ctrl + Home` | Jump to the top of the page | Should always work on a standard web page |

**NVDA-specific navigation keys (browse mode, Windows screen reader):**

| Key | What It Does |
|-----|-------------|
| `H` | Jump to next heading |
| `Shift + H` | Jump to previous heading |
| `B` | Jump to next button |
| `F` | Jump to next form field |
| `L` | Jump to next link |
| `T` | Jump to next table |
| `1` – `6` | Jump to heading of that level (e.g. `2` jumps to next H2) |
| `NVDA + F7` | Open element list (all headings, links, or form fields) |
| `NVDA + Down Arrow` | Read from current position |

---

## SECTION 6 — Screen Reader Testing

**Q12: How do you test with a screen reader? What is NVDA, VoiceOver, and TalkBack?**

**A:** Screen reader testing reveals whether content that is visually clear is also programmatically clear — whether a blind user would receive the same information and be able to perform the same tasks as a sighted user.

**The three main screen readers for QA:**

**NVDA (NonVisual Desktop Access) — Windows:**
- Free and open source, downloadable from nvaccess.org
- Approximately 40% of screen reader users use NVDA
- Works with Chrome, Firefox, Edge
- Best starting point for QA teams on Windows
- Uses "Browse Mode" (arrow-key navigation of page content) and "Forms Mode" (typed input)

**VoiceOver — macOS and iOS:**
- Built into every Apple device — free, no installation needed
- Enable on macOS: System Settings → Accessibility → VoiceOver → Enable (or Cmd + F5)
- Enable on iPhone/iPad: Settings → Accessibility → VoiceOver
- Essential for testing Safari on iOS — all iOS browsers use WebKit and VoiceOver is the standard screen reader
- Uses rotor for navigating by headings, links, form fields (two-finger twist gesture on iOS)

**TalkBack — Android:**
- Built into Android devices — free
- Enable: Settings → Accessibility → TalkBack
- Used for mobile accessibility testing on Android
- Approximately 6% of global screen reader usage
- Required for testing Android apps and web apps in Android Chrome

**QA screen reader testing workflow (NVDA on Windows):**

1. Launch NVDA (NVDA+N opens the menu if needed)
2. Open the browser and navigate to the page
3. Verify the page title is announced when the page loads
4. Press H to jump through headings — is the heading structure logical?
5. Press F to navigate through form fields — is each field's label announced?
6. Press B to navigate through buttons — are all button names meaningful?
7. Press L to navigate through links — do link names make sense out of context?
8. Tab through all interactive elements — does NVDA announce role, name, and state for each?
9. Submit a form with errors — are the error messages announced?
10. Open a modal — does focus move into it? Is its purpose announced?
11. Close the modal with Escape — does focus return to the trigger?
12. Navigate a data table — are column headers announced with each cell?

**What to listen for:** Does what the screen reader announces match what the screen shows? Does the reading order make logical sense? Can a user who cannot see the screen understand the page and complete their task?

---

## SECTION 7 — Alt Text

**Q13: What is alt text? What makes good alt text vs bad alt text?**

**A:** Alt text (alternative text) is a text description of an image provided via the HTML `alt` attribute. Screen readers read the alt text aloud in place of the image. When images fail to load, alt text is displayed. Search engines also use alt text.

Every `<img>` element must have an `alt` attribute. Whether it should be descriptive or empty depends on the purpose of the image.

**Rules by image type:**

| Image Type | Alt Requirement | Example |
|------------|----------------|---------|
| Informative — conveys content | Descriptive | `alt="Bar chart showing Year 10 student pass rates increasing from 62% in Term 1 to 81% in Term 4"` |
| Functional — is a button or link | Describes the action | `alt="Search"` on a magnifying glass icon that triggers a search |
| Decorative — purely visual, adds no information | Empty — `alt=""` | Background texture, decorative border illustration |
| Image of text | The text itself | `alt="Welcome to EduApp"` on a logo image |
| Complex — chart, diagram, map | Short description + longer description in page content | `alt="Diagram of the water cycle"` with a full text description following |
| Avatar / profile photo | Person's name | `alt="Jane Smith, Year 10 Mathematics Teacher"` |

**Good alt text — characteristics:**
- Describes the content or function of the image
- Is appropriately concise (typically under 150 characters for simple images)
- Does not start with "Image of..." or "Photo of..." (screen readers already say "image")
- Conveys the same information a sighted user receives from the image
- For charts: includes the key takeaway data, not just "a chart"

**Bad alt text examples:**

| Bad Alt Text | Why It Fails |
|-------------|-------------|
| `alt="image.png"` | Filename — meaningless |
| `alt="photo"` | Generic — no information about the content |
| `alt="click here"` | Describes an action, not the image content |
| Missing `alt` attribute | Screen reader reads the filename or full URL |
| `alt=" "` (space) | Browser may still treat this as the filename |
| Alt text for every image in a gallery: "image" | All images sound identical — no navigation possible |

**Real example of a meaningful alt text for EdTech:**

Image: A bar chart showing student assessment scores.

Bad: `alt="chart"`

Better: `alt="Assessment results bar chart"`

Best: `alt="Bar chart: Assessment results for Year 10 Science. Term 1: 58%, Term 2: 67%, Term 3: 72%, Term 4: 81%. Consistent improvement across the year."`

The best version gives a screen reader user the same insight a sighted user gets from glancing at the chart.

---

## SECTION 8 — Form Accessibility

**Q14: How do you test form accessibility? What must every form input have?**

**A:** Forms are where most users interact with an application most deeply — submitting data, logging in, searching. They are also one of the highest-risk areas for accessibility failures.

**What every form input must have:**

1. **A programmatically associated label.** Not just a visible label — a label connected to the input in the HTML so that screen readers announce it when the field is focused.

```html
<!-- Correct: label with for= matching input id= -->
<label for="email">Email address</label>
<input type="email" id="email" name="email" autocomplete="email" />

<!-- Correct: label wrapping the input -->
<label>
  Email address
  <input type="email" name="email" autocomplete="email" />
</label>

<!-- Correct: aria-label when visual label is not possible or desired -->
<input type="search" aria-label="Search courses" />

<!-- Wrong: placeholder only — disappears when typing, not a label -->
<input type="email" placeholder="Email address" />
<!-- Screen reader announces: "edit text" — no label context at all -->
```

2. **A persistent visible label.** Placeholder text is not a label — it disappears when the user starts typing. If the label disappears, users forget what the field is for, especially users with cognitive disabilities.

3. **An error message that is specific, identifies the field, and suggests correction.** (See Q7 for detail.)

4. **Required fields must be marked.** Use both an asterisk (visual) and `aria-required="true"` or `required` HTML attribute (programmatic).

5. **Input type appropriate to the data.** `type="email"` for email, `type="tel"` for phone numbers, `type="number"` for numbers. This triggers the appropriate mobile keyboard and helps screen readers announce the expected format.

**Keyboard operability of forms:**

- All form fields reachable and completable via keyboard
- Tab order through form fields is logical (top to bottom, left to right)
- Submitting with Enter key in any text field submits the form (standard behaviour — do not disable this)
- Error messages must be announced — either by placing them in an `aria-live` region or by moving focus to the error summary

**Testing procedure for forms:**

1. Tab through every field — is each field's label announced?
2. Submit the form empty — are all required field errors announced?
3. Fill fields with invalid data — are specific, helpful error messages shown and announced?
4. Fill the form correctly — does submission succeed?
5. Check all field labels with DevTools (inspect → look for `<label>` elements or `aria-label`)
6. Run axe-core scan on the form page — `label` violations are automatically detected

---

## SECTION 9 — Focus Indicators

**Q15: What is a focus indicator? How do you test it?**

**A:** A focus indicator is the visual highlight that appears around an interactive element when it receives keyboard focus. It tells keyboard users: "this is where you are on the page, and if you press Enter or Space, this is what you will activate."

Without a focus indicator, keyboard navigation is impossible for most users — they are navigating blind.

**WCAG requirement:** WCAG 2.4.7 (Level AA) requires that keyboard-focusable elements have a visible focus indicator. WCAG 2.4.11 (Level AA, added in WCAG 2.2) strengthens this with specific contrast and size requirements.

**The most common focus indicator failure in CSS:**

```css
/* This destroys keyboard accessibility for all users */
:focus {
  outline: none;
}

/* Slightly less bad — removes outline everywhere but
   only restores for mouse clicks — still broken for keyboard */
:focus:not(:focus-visible) {
  outline: none;
}
```

Developers remove the default browser focus ring (usually a blue or black outline) because it looks inconsistent with the design. But they often forget to replace it with something else, leaving keyboard users with no visible indication of their position.

**Correct approach:**

```css
/* Remove the browser default and replace with a custom styled ring */
:focus {
  outline: 3px solid #005FCC;
  outline-offset: 2px;
  border-radius: 4px;
}

/* Or use focus-visible for mouse-click suppression while
   keeping keyboard focus visible */
:focus-visible {
  outline: 3px solid #005FCC;
  outline-offset: 2px;
}
```

**How to test:**

1. Press Tab to begin keyboard navigation on any page
2. Tab through every interactive element
3. For each element that receives focus: can you clearly see where focus is?
4. Specifically check: links in body text, navigation items, buttons, form fields, tabs, accordions, and custom dropdowns
5. If focus ever "disappears" (you pressed Tab but cannot find the focused element) — that is a WCAG 2.4.7 failure
6. In DevTools: `:focus` pseudo-class selector shows what CSS applies to the focused state

**WCAG 2.4.11 (WCAG 2.2 addition):** The focus indicator must have a minimum area of the perimeter of the unfocused element, and must have at least 3:1 contrast ratio against adjacent colours.

---

## SECTION 10 — Heading Structure

**Q16: What is the correct heading structure? How do you test it?**

**A:** Headings (`<h1>` through `<h6>`) create the document outline of a web page. Screen reader users navigate by headings — pressing H to jump between them — to get an overview of a page and navigate to the section they want, just as a sighted user scans headings visually.

A logical heading structure is as important for accessibility as a logical table of contents is for a printed document.

**Rules for correct heading structure:**

1. **One `<h1>` per page** — the main page title. Never zero, never two.
2. **Never skip heading levels.** Do not go from `<h2>` to `<h4>` — always maintain hierarchy.
3. **Headings convey hierarchy, not visual style.** If you want text to look like a heading but it is not structurally a heading, use CSS styling on a paragraph, not an `<h>` tag.
4. **Every section of the page should have a heading** — so screen reader users know where they are.
5. **Headings should describe the content that follows** — not just be decorative titles.

**Correct heading structure:**
```
H1: Online Learning Platform
  H2: Your Courses
    H3: Mathematics — Year 10
    H3: Science — Year 11
  H2: Your Progress
    H3: This Week
    H3: This Month
  H2: Announcements
    H3: Term 3 Assessment Schedule Released
    H3: Platform Maintenance: 14 July
```

**Incorrect heading structure:**
```
H1: Online Learning Platform
  H3: Your Courses        ← Skipped H2 — screen reader users lose structural context
    H1: Mathematics       ← Second H1 — page now has two "top-level" topics
  H2: Your Progress
    H5: This Week         ← Jumped from H2 to H5 — skipped H3 and H4
```

**How to test heading structure:**

1. **NVDA (Windows):** Press H to jump through headings. Listen for the level announced: "Heading level 2 — Your Courses". Does the outline match the page structure?

2. **WAVE browser extension:** Click "Structure" tab — shows all headings in the correct visual hierarchy with their levels.

3. **Browser DevTools:** Open console and run:
   ```javascript
   Array.from(document.querySelectorAll('h1,h2,h3,h4,h5,h6'))
     .forEach(h => console.log(h.tagName, h.textContent.trim()));
   ```
   This lists all headings in order — quickly reveals skipped levels or duplicate H1s.

4. **HeadingsMap browser extension:** Visualises the complete heading hierarchy.

---

## SECTION 11 — Link Text and ARIA

**Q17: What makes good link text? What is wrong with "click here"?**

**A:** Link text must make sense out of context. Screen reader users navigate by links — pressing L to jump between them — and hear only the link text itself, not the surrounding paragraph. They use the link text to decide whether to follow the link.

"Click here" heard in isolation tells a user nothing about where the link goes or what it does. A screen reader user navigating by links hears: "click here... click here... click here... read more... click here..." — completely useless.

**Good vs bad link text:**

| Bad Link Text | Why It Fails | Good Alternative |
|--------------|-------------|-----------------|
| "Click here" | No destination or purpose in isolation | "Download the Year 10 Mathematics workbook (PDF, 2.4MB)" |
| "Read more" | More about what? | "Read more about the Year 10 Science curriculum" |
| "Here" | Not a description of anything | "View the student progress report for Term 3" |
| "Link" | Screen readers already say "link" before announcing link text | Any meaningful text |
| The full URL as text | `https://example.com/reports/2024/q3?format=pdf` is unreadable aloud | "Q3 2024 Progress Report (PDF)" |
| Multiple identical links: "Submit" | When multiple forms on a page all have "Submit", they cannot be distinguished | "Submit course enrolment" and "Submit assignment" |

**WCAG requirement:** WCAG 2.4.4 (Level A) — "The purpose of each link can be determined from the link text alone, or from the link text together with its programmatically determined link context."

**When link text cannot be changed visually:** Use `aria-label` to provide screen-reader-only context:

```html
<!-- Visual: "Read more" — but accessible name is specific -->
<a href="/curriculum/year10" aria-label="Read more about Year 10 Mathematics curriculum">
  Read more
</a>
```

---

**Q18: What is ARIA? When should developers use ARIA attributes?**

**A:** ARIA stands for Accessible Rich Internet Applications. It is a set of HTML attributes defined by the W3C that allow developers to communicate the role, name, state, and value of custom UI components to assistive technologies when standard HTML cannot convey this information on its own.

**The First Rule of ARIA:** "Do not use ARIA if you can use a native HTML element with the correct semantics instead."

Native HTML elements have built-in accessibility. A `<button>` is keyboard focusable, activatable with Enter and Space, and announces "button" to screen readers automatically. A `<div onclick="...">` does none of this unless you manually add ARIA, keyboard handlers, and tabindex.

**When ARIA is appropriate:**

1. **Custom interactive components that have no native HTML equivalent:**
   - A custom tab panel (`role="tablist"`, `role="tab"`, `role="tabpanel"`)
   - An autocomplete combobox (`role="combobox"`, `role="listbox"`, `role="option"`)
   - A custom slider (`role="slider"`, `aria-valuenow`, `aria-valuemin`, `aria-valuemax`)

2. **Status messages that appear without focus changing:**
   - "3 items added to cart" appearing after an action: `role="status"` or `aria-live="polite"` announces it to screen readers without moving focus

3. **Dynamic content updates:**
   - Error messages added to the DOM: `role="alert"` or `aria-live="assertive"` for critical messages; `aria-live="polite"` for non-critical updates

4. **Supplementary descriptions:**
   - `aria-describedby` to connect a field to help text or an error message
   - `aria-label` when no visible label exists

**Common ARIA attributes:**

| Attribute | Purpose | Example |
|-----------|---------|---------|
| `role` | Declares what kind of component this is | `role="dialog"`, `role="alertdialog"`, `role="navigation"` |
| `aria-label` | Provides an accessible name (overrides visible text) | `<input aria-label="Search courses">` |
| `aria-labelledby` | Names element by referencing another element's ID | `<section aria-labelledby="courses-heading">` |
| `aria-describedby` | Connects element to description text | `<input aria-describedby="email-help" aria-describedby="email-error">` |
| `aria-expanded` | State: is this expandable element open or closed? | `<button aria-expanded="true">` on an accordion trigger |
| `aria-hidden` | Hides element from accessibility tree (decorative content) | `<svg aria-hidden="true">` on a decorative icon |
| `aria-live` | Announces dynamic content changes | `<div aria-live="polite">Loading...</div>` |
| `aria-required` | Programmatically marks a field as required | `<input aria-required="true">` |
| `aria-invalid` | Programmatically marks a field as in error | `<input aria-invalid="true">` |

---

## SECTION 12 — Automated Accessibility Testing

**Q19: What is axe-core? How do you use it with Playwright?**

**A:** axe-core is an open-source accessibility rules engine developed by Deque Systems. It runs in the browser, analyses the rendered DOM, and returns a detailed report of WCAG violations with information about which element failed, which WCAG criterion was violated, and the severity of the impact.

axe-core is the engine behind many accessibility testing tools: the axe DevTools browser extension, axe in Playwright, axe in Cypress, and Lighthouse's accessibility audits.

**Installing axe-core for Playwright:**

```bash
npm install --save-dev @axe-core/playwright
```

**Basic usage:**

```typescript
import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('login page has no WCAG AA violations', async ({ page }) => {
  await page.goto('/login');

  const results = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa'])  // Test WCAG 2.1 A and AA
    .analyze();

  expect(results.violations).toEqual([]);
});
```

**What the violations array contains:**

Each violation object includes:
- `id` — the axe rule ID (e.g. `color-contrast`, `label`, `image-alt`)
- `impact` — `critical`, `serious`, `moderate`, or `minor`
- `description` — human-readable description of the failure
- `helpUrl` — link to axe's documentation explaining the rule and fix
- `nodes` — array of DOM elements that failed, including the HTML snippet

**Useful axe options:**

```typescript
// Scope to a specific component
const results = await new AxeBuilder({ page })
  .include('#login-form')
  .withTags(['wcag2aa'])
  .analyze();

// Exclude a third-party widget your team does not control
const results = await new AxeBuilder({ page })
  .exclude('#payment-iframe')
  .withTags(['wcag2aa'])
  .analyze();

// Test only specific rules
const results = await new AxeBuilder({ page })
  .withRules(['color-contrast', 'label', 'image-alt'])
  .analyze();

// Test after an interaction (dynamic content)
await page.click('button[type="submit"]');
await page.waitForSelector('[role="alert"]');
const results = await new AxeBuilder({ page })
  .withTags(['wcag2aa'])
  .analyze();
```

---

**Q20: Show a complete Playwright + axe test file for WCAG AA compliance.**

**A:**

```typescript
import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

// ============================================================
// HELPER: Print readable violation summary on failure
// ============================================================
function printViolations(violations: any[]) {
  if (violations.length === 0) return;
  console.log('\n=== ACCESSIBILITY VIOLATIONS ===');
  violations.forEach(v => {
    console.log(`\n[${v.impact.toUpperCase()}] ${v.id}: ${v.description}`);
    console.log(`  WCAG: ${v.tags.filter((t: string) => t.startsWith('wcag')).join(', ')}`);
    console.log(`  Help: ${v.helpUrl}`);
    v.nodes.slice(0, 3).forEach((n: any) => {
      console.log(`  Element: ${n.html}`);
      if (n.failureSummary) console.log(`  Fix: ${n.failureSummary}`);
    });
  });
}

// ============================================================
// TEST 1: Full page WCAG AA check — home page
// ============================================================
test('home page has no WCAG AA violations', async ({ page }) => {
  await page.goto('/');

  const results = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa'])
    .analyze();

  printViolations(results.violations);
  expect(results.violations).toEqual([]);
});

// ============================================================
// TEST 2: Login page — scoped to form component
// ============================================================
test('login form is accessible (scoped to #login-form)', async ({ page }) => {
  await page.goto('/login');

  const results = await new AxeBuilder({ page })
    .include('#login-form')
    .withTags(['wcag2aa'])
    .analyze();

  printViolations(results.violations);
  expect(results.violations).toHaveLength(0);
});

// ============================================================
// TEST 3: Error state accessibility — form submitted empty
// ============================================================
test('login form error messages are accessible after failed submission', async ({ page }) => {
  await page.goto('/login');

  // Trigger validation errors by submitting empty form
  await page.click('button[type="submit"]');

  // Wait for error messages to appear in the DOM
  await page.waitForSelector('[role="alert"], .field-error', { timeout: 5000 });

  const results = await new AxeBuilder({ page })
    .withTags(['wcag2aa'])
    .analyze();

  printViolations(results.violations);
  expect(results.violations).toEqual([]);
});

// ============================================================
// TEST 4: Colour contrast only
// ============================================================
test('all text meets WCAG AA colour contrast requirements', async ({ page }) => {
  await page.goto('/');

  const results = await new AxeBuilder({ page })
    .withRules(['color-contrast'])
    .analyze();

  if (results.violations.length > 0) {
    console.log('Contrast failures:');
    results.violations[0].nodes.forEach(n => {
      const data = n.any[0]?.data;
      console.log(`  Element: ${n.html}`);
      if (data) {
        console.log(`  Ratio: ${data.contrastRatio} (required: ${data.expectedContrastRatio})`);
        console.log(`  FG: ${data.fgColor}, BG: ${data.bgColor}`);
      }
    });
  }

  expect(results.violations).toHaveLength(0);
});

// ============================================================
// TEST 5: Keyboard navigation — login form
// ============================================================
test('user can complete login using keyboard only', async ({ page }) => {
  await page.goto('/login');

  // Tab into the page — first focus should be skip link
  await page.keyboard.press('Tab');
  const firstFocus = page.locator(':focus');
  await expect(firstFocus).toContainText(/skip/i);

  // Tab to email field
  await page.keyboard.press('Tab');
  await expect(page.locator(':focus')).toHaveAttribute('type', 'email');
  await page.keyboard.type('student@example.com');

  // Tab to password field
  await page.keyboard.press('Tab');
  await expect(page.locator(':focus')).toHaveAttribute('type', 'password');
  await page.keyboard.type('Password123!');

  // Tab to submit button and activate with Enter
  await page.keyboard.press('Tab');
  await expect(page.locator(':focus')).toHaveAttribute('type', 'submit');
  await page.keyboard.press('Enter');

  // Confirm successful login
  await expect(page).toHaveURL('/dashboard');
});

// ============================================================
// TEST 6: Modal focus management
// ============================================================
test('modal dialog traps focus and returns it on close', async ({ page }) => {
  await page.goto('/courses');

  // Open a modal
  await page.keyboard.press('Tab');
  // Tab to "Add Course" button and open modal
  const addButton = page.getByRole('button', { name: /add course/i });
  await addButton.focus();
  await page.keyboard.press('Enter');

  // Verify focus moved into the modal
  const modal = page.getByRole('dialog');
  await expect(modal).toBeVisible();

  // Try to Tab out — focus should stay in modal
  for (let i = 0; i < 20; i++) {
    await page.keyboard.press('Tab');
    const activeEl = await page.evaluate(() => document.activeElement?.closest('[role="dialog"]'));
    expect(activeEl).toBeTruthy();
  }

  // Close modal with Escape
  await page.keyboard.press('Escape');
  await expect(modal).not.toBeVisible();

  // Verify focus returned to the trigger button
  await expect(addButton).toBeFocused();
});
```

---

**Q21: What is Lighthouse? How do you run an accessibility audit?**

**A:** Lighthouse is an open-source, automated auditing tool built into Chrome DevTools and available as a CLI. It audits web pages for performance, SEO, best practices, and accessibility. The accessibility audit is powered by axe-core plus some additional checks.

**Running Lighthouse in Chrome DevTools:**

1. Open Chrome DevTools (F12)
2. Navigate to the "Lighthouse" tab (may need to click ">>" to find it)
3. Select "Accessibility" as the only category (uncheck others for speed)
4. Click "Analyze page load"
5. Wait 30-60 seconds for the audit to complete
6. Review the score (0-100) and the list of failed audits

**Running Lighthouse from the CLI:**

```bash
# Audit a public URL
npx lighthouse https://example.com --only-categories=accessibility --output html --output-path ./lighthouse-a11y.html

# Audit localhost with Chrome running headlessly
npx lighthouse http://localhost:3000 --only-categories=accessibility --output html --output-path ./report.html --chrome-flags="--headless"

# Audit and output JSON (useful for CI threshold checking)
npx lighthouse https://example.com --only-categories=accessibility --output json --output-path ./a11y.json
```

**Interpreting the Lighthouse score:**

The Lighthouse accessibility score is weighted — each failed audit deducts a different number of points depending on impact. A score of 100 means no automatically detectable failures. A score does not directly map to WCAG conformance — it is possible to score 95+ and still have WCAG AA failures that only manual testing would find.

**Use Lighthouse for:**
- Quick health checks and trend tracking
- Executive reporting ("our accessibility score improved from 78 to 94")
- Finding obvious failures quickly (missing alt text, missing labels, contrast failures)

**Do not use Lighthouse alone for:**
- WCAG conformance certification
- Claiming a product is "accessible" — manual testing is always required

---

**Q22: What does axe catch vs what does it miss? The 30% rule explained.**

**A:** Automated accessibility testing tools — including axe-core, Lighthouse, and WAVE — catch approximately 30–40% of WCAG issues. The remaining 60–70% require human testing.

This is not a failure of the tools. It reflects a fundamental limitation: automated tools can only evaluate what can be determined from the static code and DOM. Many accessibility requirements involve meaning, usability, and experience — things that require human judgement.

**What axe CAN automatically detect:**

| Rule | What axe checks |
|------|----------------|
| `image-alt` | Whether an `alt` attribute is present on `<img>` |
| `label` | Whether a form input has a programmatic label |
| `color-contrast` | Whether text meets the minimum contrast ratio |
| `html-has-lang` | Whether the `<html>` element has a `lang` attribute |
| `document-title` | Whether the page has a `<title>` element |
| `duplicate-id` | Whether any `id` attributes are duplicated |
| `aria-valid-attr` | Whether ARIA attributes are used correctly |
| `button-name` | Whether buttons have an accessible name |
| `link-name` | Whether links have an accessible name |

**What axe CANNOT detect (requires manual testing):**

| Issue | Why automation cannot detect it |
|-------|--------------------------------|
| Meaningful vs meaningless alt text | axe knows `alt` exists; it cannot judge whether "photo" is meaningful for that image |
| Logical reading order | axe does not know if the Tab order makes sense in the visual and content context |
| Screen reader announcement quality | Whether content sounds natural and understandable when read aloud |
| Focus management in interactions | Whether focus correctly returns to the trigger element after closing a modal |
| Cognitive accessibility | Is the content clear, consistent, and manageable for users with cognitive disabilities? |
| Keyboard trap situations | axe checks basic syntax but cannot simulate navigating a complex custom component |
| Error announcement timing | Whether errors are announced at the moment the user needs to know about them |
| Meaningful headings | axe checks heading levels exist; it cannot judge if the heading text describes the section |
| Captions quality | axe detects absence of captions; it cannot evaluate caption accuracy |

**The practical implication:** Running axe and seeing zero violations does not mean the page is accessible. It means the 30-40% of detectable issues are clean. Always follow automated scanning with keyboard testing, screen reader testing, and human review.

---

## SECTION 13 — Common Accessibility Bugs

**Q23: What are the 15 most common accessibility bugs QA finds?**

**A:**

**1. Missing or meaningless alt text on images**
WCAG 1.1.1 (Level A). `<img>` with no `alt`, `alt="image.png"`, or `alt="photo"`. Screen readers cannot describe the content. Severity: Critical. Detected by axe.

**2. Low colour contrast ratio**
WCAG 1.4.3 (Level AA). Text contrast below 4.5:1 for normal text or 3:1 for large text. Fails for low-vision users. Common failures: grey help text on white, orange text on white, light placeholder text. Severity: Serious. Detected by axe.

**3. No visible keyboard focus indicator**
WCAG 2.4.7 (Level AA). CSS `outline: none` applied with no replacement. Keyboard users cannot see their position. The single most common CSS accessibility failure. Severity: Serious. Requires keyboard testing.

**4. Form field has no label**
WCAG 1.3.1, 4.1.2 (Level A). Input with no `<label>`, no `aria-label`, no `aria-labelledby`. Screen reader announces only "edit text" — no field context. Severity: Critical. Detected by axe.

**5. Error message not announced to screen reader**
WCAG 3.3.1 (Level A). Error messages appear visually but the DOM element is not in an ARIA live region. Screen reader users are not informed of the error. Severity: Serious. Requires screen reader testing.

**6. Modal dialog does not trap keyboard focus**
WCAG 2.1.2 (Level A). Tab can escape a modal into the background content. Users navigate elements they cannot see. Severity: Serious. Requires keyboard testing.

**7. Skip navigation link missing**
WCAG 2.4.1 (Level A). No "Skip to main content" link as first Tab stop. Keyboard users must Tab through 15-30 navigation items on every page load. Severity: Moderate. Requires keyboard testing.

**8. Colour is the only indicator of status**
WCAG 1.4.1 (Level A). Error fields shown in red with no icon, no label, no border change. Red-green colour-blind users (8% of males) cannot distinguish error state from normal state. Severity: Serious. Requires visual inspection.

**9. Placeholder text used as the only field label**
WCAG 1.3.1, 3.3.2 (Level A). Placeholder text is not a label. It disappears when the user types. Users with cognitive disabilities forget what the field is asking for. Severity: Serious. Detected by axe.

**10. Heading structure skips levels or has multiple H1s**
WCAG 1.3.1 (Level A). Going from H2 directly to H4, or having two H1 elements on a page. Screen reader navigation by headings becomes meaningless. Severity: Moderate. Detected by WAVE/DevTools.

**11. Link text does not describe the destination**
WCAG 2.4.4 (Level A). "Click here", "Read more", "Learn more" — meaningless in isolation. Screen reader link navigation becomes useless. Severity: Moderate. Requires review.

**12. Auto-playing audio or video**
WCAG 1.4.2 (Level A). Content plays automatically with audio, with no mechanism to pause it within 3 seconds. Screen reader audio is drowned out. Severity: Serious. Requires page load testing.

**13. Session timeout with no warning**
WCAG 2.2.1 (Level A). Session expires and user is redirected to login with no advance warning, losing unsaved work. Severity: Serious. Requires time-based testing.

**14. Links open in new tab without warning**
WCAG 3.2.2 (Level A). `target="_blank"` with no announcement that a new tab will open. Confuses screen reader users who press Back and find the previous page unchanged. Severity: Moderate. Requires link review.

**15. Custom interactive component is not keyboard operable**
WCAG 2.1.1 (Level A). A dropdown, date picker, carousel, or autocomplete built with `<div>` elements that responds only to mouse events. Keyboard users cannot activate or navigate the component. Severity: Critical. Requires keyboard testing.

---

## SECTION 14 — Skip Links and Focus Traps

**Q24: What is a skip navigation link? Why is it important?**

**A:** A skip navigation link (also called a "skip link") is a keyboard-navigable link — typically the very first Tab stop on a page — that allows keyboard users to bypass the repeated navigation menu and jump directly to the main page content.

**Why it is critical:** On every page load, keyboard users must Tab through every interactive element before reaching the main content. On a page with a header containing a logo, a hamburger menu, five navigation links, a search bar, and a language selector, that is 10+ Tab presses before reaching anything useful — multiplied by every page the user visits. For a user who navigates an entire product by keyboard, this is exhausting and time-wasting.

**WCAG requirement:** WCAG 2.4.1 (Level A) — "A mechanism is available to bypass blocks of content that are repeated on multiple web pages."

**How it works:**

```html
<!-- At the very top of the page body — before the nav -->
<a href="#main-content" class="skip-link">Skip to main content</a>

<nav>
  <!-- All navigation links here -->
</nav>

<main id="main-content">
  <!-- Main content here -->
</main>
```

```css
/* Visually hidden by default — appears on focus */
.skip-link {
  position: absolute;
  top: -40px;
  left: 0;
  background: #000;
  color: #fff;
  padding: 8px;
  z-index: 9999;
  text-decoration: none;
}

.skip-link:focus {
  top: 0;  /* Appears when focused */
}
```

**Testing procedure:**

1. Navigate to any page of the application
2. Press Tab — the first Tab stop must show the skip link (it appears visually when focused)
3. Press Enter — focus jumps to the main content area (the `#main-content` element)
4. Verify that the next Tab after activating the skip link is the first interactive element in the main content — not a navigation item

---

**Q25: What is a modal focus trap? How do you test it?**

**A:** A modal focus trap is the intentional containment of keyboard focus within an open modal dialog. When a modal is open, the background content is visually hidden and functionally inaccessible — keyboard focus must not be able to escape into that background content.

This is the one situation where a "keyboard trap" is REQUIRED for accessibility — not a violation of it.

**Why a focus trap is necessary:**

Without a focus trap, a keyboard user pressing Tab inside a modal will eventually Tab past the last focusable element in the modal and land on the background page content — content they cannot see because the modal is overlaying it. They are now lost: interacting with invisible elements, unable to find the modal's close button, unable to return to their task.

**Correct focus trap behaviour:**

- When the modal opens: focus moves into the modal (to the close button or first focusable element)
- While the modal is open: Tab cycles through focusable elements within the modal only
- At the last focusable element: Tab wraps back to the first focusable element in the modal
- At the first focusable element: Shift+Tab wraps to the last focusable element
- Pressing Escape: closes the modal, focus returns to the element that triggered it

**How to test focus trap:**

1. Open a modal dialog using the keyboard (Tab to the trigger button, press Enter)
2. Press Tab repeatedly and count the interactive elements in the modal
3. Continue pressing Tab past what appears to be the last element — does focus stay inside the modal?
4. Press Shift+Tab from the first element — does focus stay inside the modal?
5. Press Escape — does the modal close?
6. After Escape: is focus back on the button or link that opened the modal?

**Defect template:**

"The [modal name] modal does not trap keyboard focus. Pressing Tab past the last focusable element in the modal moves focus to [background element]. Expected: focus cycles within the modal until dismissed with Escape. WCAG 2.1.2 Level A."

---

## SECTION 15 — Accessibility in Agile and EdTech

**Q26: How do you test accessibility in an Agile sprint? Where does it fit?**

**A:** Accessibility should be built into the sprint workflow at every stage, not added as a final audit. "Shift left" on accessibility means catching violations as code is written, not after the sprint review.

**Before the sprint — Definition of Ready:**
- Acceptance criteria include explicit accessibility requirements: "All form inputs must have visible, persistent labels", "Keyboard users must be able to open and close the dropdown"
- Designs are reviewed for colour contrast (using Figma plugins or WebAIM Contrast Checker) and focus states before development begins
- Any new component type has an ARIA pattern referenced (aria-practices.org)

**During development:**
- Developers install the axe DevTools browser extension and check new components as they build them
- Automated axe-core scans are configured in the CI pipeline — a new WCAG violation in a PR is flagged automatically
- Code reviews include: does this use native HTML elements where possible? Are custom components ARIA-compliant?

**QA in the sprint:**
- I run axe-core scans on every new page or component as part of my functional test cycle
- I conduct keyboard navigation testing for every new interactive element — can I reach it, operate it, and navigate away from it?
- For major new UI (a new form, a new modal, a new navigation pattern), I test with NVDA
- I check colour contrast for any new text colours or background colours
- I raise accessibility defects with the same priority system as functional defects:
  - WCAG Level A failure (blocks access) → P1 Critical
  - WCAG Level AA failure (significant barrier) → P2 High
  - Usability/cognitive issue → P3 Medium

**Definition of Done:**
- No new WCAG A or AA violations introduced (confirmed by automated scan)
- Keyboard navigation verified for all new interactive elements
- Screen reader tested for any significant new UI pattern (modal, form, navigation)
- Colour contrast checked for any new colour combinations

**Sprint retrospective:** Review: how many accessibility issues were caught in dev vs QA vs production? The trend should move left over time.

---

**Q27: Why is accessibility especially important for EdTech? How does it affect real students?**

**A:** EdTech occupies a uniquely high-stakes position in accessibility. Unlike a retail website where an inaccessible feature is an inconvenience, an inaccessible EdTech product can exclude a student from education itself.

**The student populations at risk:**

| Disability Type | Estimated Prevalence | How EdTech Must Serve Them |
|----------------|--------------------|-----------------------------|
| Visual impairment | ~1.5% of students | Screen reader compatible content, meaningful alt text on all educational images, accessible charts and diagrams |
| Motor impairment | ~1% of students | Full keyboard accessibility — cannot use a mouse to drag-and-drop activities, timed interactions are configurable |
| Cognitive/learning differences (dyslexia, ADHD) | ~10–15% of students | Clear consistent navigation, no distracting auto-playing content, plain language, adequate time on timed assessments |
| Deaf / hard of hearing | ~0.5% of students | Captions on all video content, transcripts for audio, no audio-only instructions |
| Colour blindness | ~8% of male students | No colour-only indicators (marking correct/incorrect answers, highlighting important text) |

**The legal dimension for EdTech:**

UK and international schools purchasing EdTech are increasingly required to demonstrate that software meets accessibility standards as part of procurement. The UK Public Sector Bodies Accessibility Regulations (2018) require public sector organisations — including state schools — to use accessible software. Failure to provide accessible software can trigger contract penalties or disqualify vendors from procurement.

**The ethical dimension:**

Education is a fundamental right. A student with a visual impairment who cannot access an online assessment is not disadvantaged by their disability — they are disadvantaged by a product that was not built to include them. That is a choice made by the team that built the product. QA has a direct role in ensuring that choice is reversed before it affects a real student.

**Practical QA focus areas for EdTech:**
- Assessments and quizzes: keyboard operable, time limits are adjustable, answers not indicated only by colour
- Video content: captions are accurate, transcripts available, player controls keyboard accessible
- Document uploads/downloads: PDFs must be tagged and accessible (not image-based scans)
- Interactive learning activities (drag-and-drop, matching): must have keyboard-accessible alternatives
- Progress dashboards: charts must have text alternatives, data tables have appropriate headers
- Error states on assessment submission: must be clearly identified and describable

---

## SECTION 16 — Interview Q&A Bank

**Q28: An interviewer asks: "What is WCAG?" Give the best possible answer.**

**A:** WCAG stands for Web Content Accessibility Guidelines. It is the international technical standard for making digital content accessible to people with disabilities. It is published by the W3C — the same body that standardises HTML and CSS.

WCAG 2.1 is the current standard most organisations target. It is organised around four principles — POUR: Perceivable, Operable, Understandable, and Robust. Each principle contains guidelines, and each guideline contains testable success criteria.

There are three conformance levels: A (minimum), AA (the standard most laws require), and AAA (aspirational). QA tests to WCAG 2.1 Level AA.

In practice, WCAG 2.1 AA is referenced by the ADA in the US, Section 508 for federal systems, EN 301 549 in the EU, and the Equality Act in the UK. Failing to meet it is a legal risk, an ethical failure, and increasingly a procurement barrier.

---

**Q29: How would you explain the difference between automated and manual accessibility testing to a non-technical product owner?**

**A:** I would use this analogy:

"Automated accessibility testing is like a spell-checker. It catches the things it knows to look for — missing alt text, insufficient colour contrast, missing form labels — quickly and consistently across every page. But just like a spell-checker cannot tell you if your sentence is confusing or your argument is wrong, automated tools cannot tell you whether a screen reader user can actually understand and navigate your product.

Manual testing — trying to use the product with only a keyboard, or listening to a screen reader navigate through a form — fills in the 60–70% of issues that automation misses. Together, both approaches give us genuine confidence.

Neither replaces the other. The automation runs fast in CI and catches regressions. The manual testing catches the things that require a human to notice."

---

**Q30: You run axe-core and get zero violations. A developer says the product is now accessible. How do you respond?**

**A:** I would say: "Zero axe violations is a great start, but it means we've confirmed the 30–40% of issues that axe can automatically detect are clean. We still need to do manual testing to know whether the product is accessible."

Then I would explain what axe checks vs what it misses, using specific examples:

- "axe confirmed that every image has an `alt` attribute. But I still need to check that the alt text for our progress charts actually describes the data, not just says 'chart'."
- "axe confirmed that every form field has a label element. But I need to verify with a screen reader that the label is announced clearly when the field receives focus."
- "axe cannot test whether Tab order is logical, whether focus returns to the correct place after a modal closes, or whether a screen reader user can actually complete the student registration form."

I would then run keyboard navigation testing and at minimum a brief NVDA walkthrough of the critical user flows before claiming the product is accessible to a meaningful standard.

---

**Q31: A developer argues that accessibility is the design team's responsibility, not theirs. How do you handle this?**

**A:** Accessibility is a shared responsibility across the entire team — design, development, QA, and product. Each role has a distinct contribution:

- **Design** ensures colour contrast, focus states, heading hierarchy, and clear visual communication in mockups before any code is written
- **Development** ensures semantic HTML, keyboard operability, ARIA implementation, and correct focus management in the built product
- **QA** verifies both against WCAG criteria, finds what was missed, and raises defects
- **Product** ensures accessibility requirements are included in acceptance criteria and prioritised in the backlog

If a developer does not implement accessible focus management in a modal — that is a development bug, exactly as much as any other functional bug. The fact that a designer did not explicitly annotate it does not remove development responsibility.

I would frame this to the developer as: "Accessibility is part of the definition of done, just like functional correctness. A modal that traps keyboard users is as broken as a modal that doesn't open at all — it just affects fewer users."

---

**Q32: What is ARIA live region? Give a real example of when you need one.**

**A:** An ARIA live region is a section of the page that informs assistive technologies when its content changes, causing screen readers to automatically announce the new content without the user having to navigate to it.

There are two levels:

- `aria-live="polite"` — announces the change after the screen reader finishes what it is currently saying. Use for non-critical updates.
- `aria-live="assertive"` — interrupts what the screen reader is saying to announce the change immediately. Use only for critical, urgent information. (Use sparingly — frequent interruptions are disorienting.)
- `role="status"` — equivalent to `aria-live="polite"` for status messages
- `role="alert"` — equivalent to `aria-live="assertive"` for urgent error messages

**Real example — file upload progress:**

```html
<!-- The live region is present in the DOM before any content changes -->
<div aria-live="polite" aria-atomic="true" id="upload-status">
  <!-- Initially empty -->
</div>
```

```javascript
// When upload starts — screen reader announces "Uploading: 0%"
document.getElementById('upload-status').textContent = 'Uploading: 0%';

// When progress changes — screen reader announces the new percentage
document.getElementById('upload-status').textContent = 'Uploading: 65%';

// When complete — screen reader announces "Upload complete"
document.getElementById('upload-status').textContent = 'Upload complete. File saved successfully.';
```

Without the live region: a blind user would click "Upload" and have no feedback until they navigated to find the status message — they would not know if the upload succeeded, failed, or is still running.

**Other real use cases:**
- Cart item count updating when user adds an item
- "3 results found" after a search filter is applied
- Error messages appearing after form validation
- "Message sent" confirmation after a form submission
- Loading state announcements ("Loading courses...")

---

**Q33: How do you write an accessibility bug report that developers will act on?**

**A:** An accessibility bug report needs the same precision as any other defect report, plus three additional elements: the WCAG criterion violated, the user impact stated explicitly, and the fix guidance.

**Template:**

```
Title: [Component name] [specific failing behaviour] — WCAG [criterion] [Level]

Example: Login form error message not announced by screen reader — WCAG 3.3.1 Level A

Severity: Serious (WCAG Level A failure — prevents access for screen reader users)
Priority: P2

Environment: Chrome 126, Windows 11, NVDA 2024.1

Steps to Reproduce:
1. Open NVDA (screen reader)
2. Navigate to /login
3. Tab to the Email field
4. Leave it empty
5. Tab to the Password field
6. Leave it empty
7. Tab to the "Log In" button
8. Press Enter to submit the form

Expected Result:
The error message "Email is required" is announced by NVDA automatically
(either via an aria-live region or by focus moving to the error message).
WCAG 3.3.1 requires that input errors are identified to the user.

Actual Result:
NVDA does not announce any error message. The error text "Email is required"
is visible on screen but is only in a static <div> with no ARIA live region.
Screen reader users receive no feedback that the form submission failed.

User Impact:
Blind users submitting the login form with missing fields will receive no
feedback. They cannot determine why the form did not proceed or which field
is incorrect. They are unable to complete login independently.

Suggested Fix:
Add role="alert" to the error container, or add aria-live="assertive" to
an error summary region. Alternatively, move keyboard focus to the first
error message after failed form submission.

Reference: WCAG 2.1 SC 3.3.1 — Error Identification (Level A)
axe rule: form-field-multiple-labels, or manually detected
```

The user impact section is the most important addition for accessibility bugs — it makes the human cost concrete and prevents the defect from being deprioritised as "just an accessibility thing."

---

**Q34: What is the difference between `aria-label` and `aria-labelledby`?**

**A:** Both provide an accessible name to an element — the name that assistive technologies announce when the element receives focus. The difference is where the name comes from.

**`aria-label`** provides the name directly as a string value on the element itself. Use it when there is no visible text label in the DOM to reference.

```html
<!-- Search input — no visible label in the design -->
<input type="search" aria-label="Search courses and lessons" />
<!-- Screen reader announces: "Search courses and lessons, edit text" -->

<!-- Icon button — the icon has no text -->
<button aria-label="Close dialog">
  <svg><!-- X icon --></svg>
</button>
<!-- Screen reader announces: "Close dialog, button" -->
```

**`aria-labelledby`** references the ID of another element in the DOM that serves as the label. Use it when there is already visible text in the DOM that should serve as the label, or when the label text is used by multiple elements.

```html
<!-- Modal dialog labelled by its visible heading -->
<div role="dialog" aria-labelledby="dialog-title">
  <h2 id="dialog-title">Add Student</h2>
  <!-- Dialog content -->
</div>
<!-- Screen reader announces: "Add Student, dialog" when focus enters -->

<!-- Form field labelled by a visible custom label -->
<span id="dob-label">Date of birth</span>
<input type="text" aria-labelledby="dob-label" />
<!-- Screen reader announces: "Date of birth, edit text" -->
```

**Key difference:**
- `aria-label` = the label text is defined in the attribute (invisible in the DOM, only for assistive tech)
- `aria-labelledby` = the label text comes from an existing visible DOM element, referenced by ID

**Priority of accessible name calculation:** `aria-labelledby` wins over `aria-label`, which wins over the element's content or associated `<label>` element.

---

**Q35: What is the `role="alert"` attribute and when do you use it?**

**A:** `role="alert"` is an ARIA role that designates an element as a live region that will automatically announce its content to screen readers when it changes — and does so immediately, interrupting whatever the screen reader is currently saying.

It is equivalent to `aria-live="assertive"` with `aria-atomic="true"`.

**When to use it:** For urgent, time-sensitive error messages and alerts that users must know about immediately.

```html
<!-- This element is in the DOM before any errors occur — empty -->
<div role="alert" id="error-message"></div>

<!-- When a critical error occurs — the content change triggers automatic announcement -->
<script>
  document.getElementById('error-message').textContent = 
    'Login failed. Invalid email or password. Please try again.';
</script>
```

When the text is added to the element, NVDA or VoiceOver immediately announces: "Login failed. Invalid email or password. Please try again." — even if the user's focus is elsewhere on the page.

**When NOT to use `role="alert"`:**

- Non-critical updates ("3 results found") → use `aria-live="polite"` or `role="status"` instead
- Every status message → `role="alert"` interrupts the user; use it only for errors and truly urgent messages
- In place of moving focus to an error summary → for complex forms with many errors, moving focus to an error summary at the top of the form is often better UX than an alert

**Common bug:** The `role="alert"` element exists in the DOM before the error occurs. If you inject the alert element itself after the error, some screen readers do not detect it. Always put the element in the DOM on page load (empty), then populate its content when the alert is needed.

---

*End of Accessibility and WCAG Testing Complete Interview Q&A Guide — 35 Questions Covered*
