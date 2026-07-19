# Appium — Complete Guide | Mobile Testing | Android + iOS

---

## Table of Contents

1. [What is Appium?](#1-what-is-appium)
2. [Appium Architecture](#2-appium-architecture)
3. [Environment Setup](#3-environment-setup)
4. [Desired Capabilities](#4-desired-capabilities)
5. [App Types — Native vs Hybrid vs Mobile Web](#5-app-types--native-vs-hybrid-vs-mobile-web)
6. [All Locators](#6-all-locators)
7. [All Actions — Tap, Swipe, Scroll, Gesture](#7-all-actions--tap-swipe-scroll-gesture)
8. [Handling Hybrid Apps — Context Switching](#8-handling-hybrid-apps--context-switching)
9. [Real Device vs Emulator Setup](#9-real-device-vs-emulator-setup)
10. [Common Appium Server Commands](#10-common-appium-server-commands)
11. [Full Test Example (Java)](#11-full-test-example-java)
12. [Troubleshooting](#12-troubleshooting)
13. [Interview Q&A](#13-interview-qa)

---

## 1. What is Appium?

Appium is an open-source, cross-platform mobile test automation framework that allows you to write tests for:

- **Android** native apps (APK)
- **iOS** native apps (IPA)
- **Hybrid apps** (apps with embedded WebViews)
- **Mobile browser** (Chrome on Android, Safari on iOS)

**Key principles:**
- You write tests in any language (Java, Python, JavaScript, Ruby, C#) using the standard WebDriver protocol (W3C)
- Tests run on real devices or emulators/simulators without modifying the app source code
- Same test framework concepts as Selenium — sessions, capabilities, commands

**Why Appium over other tools?**

| Feature | Appium | Espresso (Android) | XCUITest (iOS) |
|---|---|---|---|
| Cross-platform | Yes | No | No |
| Language choice | Any | Java/Kotlin only | Swift/Obj-C only |
| No app modification | Yes | No (needs test APK) | No |
| Open source | Yes | Yes | Yes |
| Hybrid app support | Yes | Limited | Limited |

---

## 2. Appium Architecture

```
┌──────────────────────────────────────────────────────┐
│                  Test Script (Java/Python/JS)          │
│    driver.findElement(By.id("login_button")).click()  │
└─────────────────────────┬────────────────────────────┘
                           │ HTTP (W3C WebDriver Protocol)
                           ▼
┌──────────────────────────────────────────────────────┐
│              Appium Server (Node.js)                  │
│         Listens on http://localhost:4723              │
│  - Interprets WebDriver commands                      │
│  - Routes to the correct driver plugin                │
└───────────┬──────────────────────┬───────────────────┘
            │                      │
            ▼                      ▼
┌───────────────────┐   ┌──────────────────────────┐
│  UiAutomator2     │   │    XCUITest Driver        │
│  (Android Driver) │   │    (iOS Driver)           │
└────────┬──────────┘   └──────────┬───────────────┘
         │                         │
         ▼                         ▼
┌──────────────────┐   ┌───────────────────────────┐
│ Android Emulator │   │ iOS Simulator / Real Device│
│ or Real Device   │   │                           │
│ (ADB bridge)     │   │ (WebDriverAgent on device) │
└──────────────────┘   └───────────────────────────┘
```

**Component responsibilities:**

- **Test Script:** Sends HTTP commands (click, findElement, etc.) to Appium Server
- **Appium Server:** Routes commands to the appropriate driver (UIAutomator2 or XCUITest)
- **UIAutomator2:** Google's automation framework, installed as an APK on the Android device
- **XCUITest:** Apple's automation framework; Appium uses WebDriverAgent (WDA) as the bridge
- **ADB (Android Debug Bridge):** Communication layer between Appium server and Android device
- **WebDriverAgent:** An app Appium installs on iOS that acts as the HTTP server on the device

---

## 3. Environment Setup

### Prerequisites

```
Java 11 or 17 LTS
Node.js 18+ (for Appium server)
Android Studio (for Android testing)
Xcode 14+ (for iOS testing — macOS only)
Maven or Gradle (for Java project)
```

### Step 1: Install Appium Server

```bash
# Install Appium globally
npm install -g appium

# Verify installation
appium --version  # Should print 2.x.x

# Install Appium Doctor (diagnoses setup issues)
npm install -g appium-doctor
appium-doctor --android   # Check Android setup
appium-doctor --ios       # Check iOS setup
```

### Step 2: Install Appium Drivers

```bash
# Appium 2.x uses a plugin architecture — install drivers separately

# Android driver
appium driver install uiautomator2

# iOS driver
appium driver install xcuitest

# List installed drivers
appium driver list --installed
```

### Step 3: Android Setup

```
1. Install Android Studio (https://developer.android.com/studio)
2. Open Android Studio → SDK Manager → Install:
   - Android SDK Platform (API 33 or higher)
   - Android SDK Build-Tools
   - Android Emulator
   - Android SDK Platform-Tools (contains ADB)
3. Create an AVD (Android Virtual Device):
   Device Manager → Create Device → Select Pixel 6 → Choose system image (API 33) → Finish
4. Set environment variables:
```

```bash
# Add to ~/.bashrc or ~/.zshrc
export ANDROID_HOME=$HOME/Library/Android/sdk          # macOS
export ANDROID_HOME=C:\Users\YourName\AppData\Local\Android\Sdk  # Windows

export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/build-tools/33.0.0

# Verify ADB works
adb devices
```

### Step 4: Start Android Emulator

```bash
# List available emulators
emulator -list-avds

# Start a specific emulator
emulator -avd Pixel_6_API_33 -no-snapshot-load

# Verify device is listed
adb devices
# Should show: emulator-5554   device
```

### Step 5: iOS Setup (macOS only)

```bash
# Install Xcode from App Store
# Install Xcode Command Line Tools
xcode-select --install

# Install ios-deploy (for real device testing)
npm install -g ios-deploy

# Install Carthage (XCUITest dependency)
brew install carthage

# Verify setup
appium-doctor --ios
```

### Step 6: Java Project Setup (Maven)

#### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>appium-tests</artifactId>
  <version>1.0.0</version>

  <properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <java-client.version>8.5.1</java-client.version>
    <testng.version>7.8.0</testng.version>
    <selenium.version>4.15.0</selenium.version>
  </properties>

  <dependencies>

    <!-- Appium Java Client (includes Selenium WebDriver) -->
    <dependency>
      <groupId>io.appium</groupId>
      <artifactId>java-client</artifactId>
      <version>${java-client.version}</version>
    </dependency>

    <!-- TestNG Test Runner -->
    <dependency>
      <groupId>org.testng</groupId>
      <artifactId>testng</artifactId>
      <version>${testng.version}</version>
      <scope>test</scope>
    </dependency>

    <!-- Selenium WebDriver -->
    <dependency>
      <groupId>org.seleniumhq.selenium</groupId>
      <artifactId>selenium-java</artifactId>
      <version>${selenium.version}</version>
    </dependency>

    <!-- Logging -->
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
      <version>2.0.9</version>
    </dependency>

  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.1</version>
        <configuration>
          <suiteXmlFiles>
            <suiteXmlFile>testng.xml</suiteXmlFile>
          </suiteXmlFiles>
        </configuration>
      </plugin>
    </plugins>
  </build>

</project>
```

---

## 4. Desired Capabilities

Desired Capabilities (caps) tell Appium: which device, which OS, which app, and which driver to use.

### 4.1 Android Capabilities

```java
import io.appium.java_client.android.options.UiAutomator2Options;
import java.net.URL;

// Using the strongly-typed options class (recommended for Appium 2.x)
UiAutomator2Options options = new UiAutomator2Options();

// ── Required Capabilities ────────────────────────────────────────────────────

options.setPlatformName("Android");               // Always "Android" for Android
options.setAutomationName("UiAutomator2");        // Use UiAutomator2 driver
options.setDeviceName("Pixel_6_API_33");          // AVD name or "emulator-5554"
options.setPlatformVersion("13.0");               // Android version (optional but recommended)

// ── App to Test ───────────────────────────────────────────────────────────────

// Option A: Test an installed app using package + activity
options.setAppPackage("com.example.myapp");
options.setAppActivity("com.example.myapp.MainActivity");

// Option B: Install and launch an APK from path
options.setApp("/Users/yourname/Desktop/my-app.apk");

// Option C: Test Chrome browser (mobile web testing)
options.setBrowserName("Chrome");

// ── Optional Capabilities ─────────────────────────────────────────────────────

options.setAutoGrantPermissions(true);            // Auto-accept all permissions
options.setNoReset(true);                         // Don't reset app state between sessions
options.setFullReset(false);                      // Don't uninstall app between sessions
options.setNewCommandTimeout(Duration.ofSeconds(120)); // Max wait between commands
options.setUnicodeKeyboard(true);                 // Enable Unicode input
options.setResetKeyboard(true);                   // Reset keyboard after test
options.setAdbExecTimeout(Duration.ofSeconds(30));

// ── Initialize driver ─────────────────────────────────────────────────────────

AndroidDriver driver = new AndroidDriver(
    new URL("http://localhost:4723"),
    options
);
```

### 4.2 iOS Capabilities

```java
import io.appium.java_client.ios.options.XCUITestOptions;

XCUITestOptions options = new XCUITestOptions();

// ── Required Capabilities ────────────────────────────────────────────────────

options.setPlatformName("iOS");
options.setAutomationName("XCUITest");
options.setDeviceName("iPhone 14 Pro");            // Simulator name
options.setPlatformVersion("16.4");                // iOS version

// ── App to Test ───────────────────────────────────────────────────────────────

// Option A: Install from .app file (Simulator only)
options.setApp("/Users/yourname/Desktop/MyApp.app");

// Option B: Bundle ID for already-installed app
options.setBundleId("com.example.MyApp");

// Option C: Browser (mobile web testing)
options.setBrowserName("Safari");

// ── Real Device specific ─────────────────────────────────────────────────────

options.setUdid("00008030-001A60C42E08802E");   // Real device UDID
options.setXcodeOrgId("YOUR_TEAM_ID");           // Apple Developer Team ID
options.setXcodeSigningId("iPhone Developer");   // Signing identity

// ── Optional ─────────────────────────────────────────────────────────────────

options.setAutoAcceptAlerts(true);               // Auto-dismiss iOS permission dialogs
options.setNoReset(true);
options.setWdaLaunchTimeout(Duration.ofSeconds(120)); // WDA startup timeout

// ── Initialize driver ─────────────────────────────────────────────────────────

IOSDriver driver = new IOSDriver(
    new URL("http://localhost:4723"),
    options
);
```

### 4.3 Getting appPackage and appActivity

```bash
# Find appPackage and appActivity for any installed Android app:

# Method 1: adb command while app is open
adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'
# Output: mCurrentFocus=Window{...u0 com.example.myapp/com.example.myapp.MainActivity}

# Method 2: adb logcat filter
adb logcat | grep -i "activitymanager"
# Look for "START u0 {act=android.intent.action.MAIN cat=[...] cmp=com.example.myapp/.MainActivity}"

# Method 3: aapt tool (inspect APK)
aapt dump badging /path/to/app.apk | grep -E 'package:|launchable-activity'
```

---

## 5. App Types — Native vs Hybrid vs Mobile Web

### Native App
- Built with Android (Kotlin/Java) or iOS (Swift/Obj-C) native SDK
- Uses native UI components
- Locators: resource-id, accessibility-id, XPath
- Fastest performance; best OS integration

### Hybrid App
- A native shell wrapping a WebView that renders HTML/CSS/JS content
- Dual context: `NATIVE_APP` for native UI, `WEBVIEW_com.example.app` for web content
- Locators: native locators for native layer, CSS/XPath for webview
- Example: apps built with Ionic, Cordova, React Native (partial)

### Mobile Web App
- Test a website in a mobile browser (Chrome on Android, Safari on iOS)
- No app installation needed
- Uses `browserName: 'Chrome'` or `'Safari'` capability
- Locators: CSS selectors, XPath (same as Selenium)

---

## 6. All Locators

### Android Locators

```java
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

// ── resource-id (most stable for native Android apps) ─────────────────────
driver.findElement(AppiumBy.id("com.example.myapp:id/login_button"));
// Shorthand (Appium adds the package prefix automatically if configured):
driver.findElement(By.id("login_button"));

// ── accessibility-id (content-desc in Android; label in iOS) ──────────────
driver.findElement(AppiumBy.accessibilityId("Submit Button"));

// ── XPath (flexible but fragile) ──────────────────────────────────────────
driver.findElement(By.xpath("//android.widget.Button[@text='Login']"));
driver.findElement(By.xpath("//android.widget.EditText[@resource-id='com.example.myapp:id/email_field']"));
driver.findElement(By.xpath("//*[contains(@text,'Welcome')]"));
driver.findElement(By.xpath("//android.widget.ListView/android.widget.TextView[2]")); // 2nd item

// ── AndroidUIAutomator (most powerful for Android) ────────────────────────
// Uses UiAutomator2's DSL — can scroll and find in one command
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().text(\"Login\")"
));
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().resourceId(\"com.example.myapp:id/username\")"
));
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().className(\"android.widget.EditText\").instance(0)"
));
// Scroll and find — extremely useful for lists
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true))" +
    ".scrollIntoView(new UiSelector().text(\"Settings\"))"
));

// ── Class name ────────────────────────────────────────────────────────────
driver.findElements(By.className("android.widget.Button"));
driver.findElement(By.className("android.widget.EditText"));

// ── Image locator (visual matching) ───────────────────────────────────────
// Requires base64 encoded reference image
// driver.findElement(AppiumBy.image(base64ImageString));
```

### iOS Locators

```java
// ── accessibility-id (label attribute in iOS) ──────────────────────────────
driver.findElement(AppiumBy.accessibilityId("loginButton"));
driver.findElement(AppiumBy.accessibilityId("Email TextField"));

// ── XPath ─────────────────────────────────────────────────────────────────
driver.findElement(By.xpath("//XCUIElementTypeButton[@name='Login']"));
driver.findElement(By.xpath("//XCUIElementTypeTextField[@value='Email']"));
driver.findElement(By.xpath("//XCUIElementTypeStaticText[contains(@value,'Welcome')]"));

// ── iOS Predicate String (fast, native NSPredicate) ───────────────────────
driver.findElement(AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND name == 'Login'"));
driver.findElement(AppiumBy.iOSNsPredicateString("label CONTAINS 'Submit'"));
driver.findElement(AppiumBy.iOSNsPredicateString("value BEGINSWITH 'Enter'"));

// ── iOS Class Chain (fast, hierarchical) ──────────────────────────────────
driver.findElement(AppiumBy.iOSClassChain("**/XCUIElementTypeButton[`name == 'Login'`]"));
driver.findElement(AppiumBy.iOSClassChain("**/XCUIElementTypeTable/XCUIElementTypeCell[2]"));

// ── Class name ────────────────────────────────────────────────────────────
driver.findElements(By.className("XCUIElementTypeButton"));
driver.findElements(By.className("XCUIElementTypeTextField"));
```

### Locator Priority Guide

| Priority | Locator | Platform | Reason |
|---|---|---|---|
| 1 | `accessibilityId` | Both | Stable, cross-platform |
| 2 | `id` (resource-id) | Android | Unique, stable |
| 3 | `AndroidUIAutomator` | Android | Most powerful, scroll-capable |
| 4 | `iOSNsPredicateString` | iOS | Fast, native matching |
| 5 | `iOSClassChain` | iOS | Hierarchical, fast |
| 6 | `className` | Both | OK for lists |
| 7 | `XPath` | Both | Last resort — slow and fragile |

---

## 7. All Actions — Tap, Swipe, Scroll, Gesture

### Basic Actions

```java
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;

// ── Tap (equivalent to click) ──────────────────────────────────────────────
WebElement loginBtn = driver.findElement(AppiumBy.accessibilityId("Login"));
loginBtn.click();

// ── Type text ─────────────────────────────────────────────────────────────
WebElement emailField = driver.findElement(AppiumBy.id("com.example.myapp:id/email"));
emailField.clear();
emailField.sendKeys("user@example.com");

// ── Get text from element ─────────────────────────────────────────────────
String text = driver.findElement(AppiumBy.id("com.example.myapp:id/header")).getText();
System.out.println("Header text: " + text);

// ── Check if element is displayed ─────────────────────────────────────────
boolean isVisible = driver.findElement(AppiumBy.id("com.example.myapp:id/error_msg")).isDisplayed();

// ── Get attribute ─────────────────────────────────────────────────────────
String hint = emailField.getAttribute("hint");          // Android placeholder text
String value = emailField.getAttribute("value");        // iOS current value
String checked = driver.findElement(AppiumBy.id("com.example.myapp:id/checkbox"))
    .getAttribute("checked");
```

### Touch Actions (Appium 2.x — W3C Actions API)

```java
import org.openqa.selenium.interactions.Pointer;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import java.time.Duration;
import java.util.Collections;

// ── Swipe (generic — scroll the screen) ──────────────────────────────────

private void swipe(AndroidDriver driver, int startX, int startY, int endX, int endY) {
    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence swipeSequence = new Sequence(finger, 1);

    swipeSequence.addAction(finger.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), startX, startY));
    swipeSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    swipeSequence.addAction(finger.createPointerMove(Duration.ofMillis(800),
        PointerInput.Origin.viewport(), endX, endY));
    swipeSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    driver.perform(Collections.singletonList(swipeSequence));
}

// Swipe up (scroll down to see more content)
swipe(driver, 540, 1500, 540, 400);  // center-x, bottom-y → center-x, top-y

// Swipe down (scroll up / pull-to-refresh)
swipe(driver, 540, 400, 540, 1500);

// Swipe left (go to next page in carousel)
swipe(driver, 900, 800, 100, 800);

// Swipe right (go to previous page / open drawer)
swipe(driver, 100, 800, 900, 800);

// ── Long Press ────────────────────────────────────────────────────────────

private void longPress(AndroidDriver driver, WebElement element) {
    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence longPressSeq = new Sequence(finger, 1);

    org.openqa.selenium.Point location = element.getLocation();
    org.openqa.selenium.Dimension size = element.getSize();
    int centerX = location.getX() + size.getWidth() / 2;
    int centerY = location.getY() + size.getHeight() / 2;

    longPressSeq.addAction(finger.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX, centerY));
    longPressSeq.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    longPressSeq.addAction(finger.createPointerMove(Duration.ofMillis(2000),
        PointerInput.Origin.viewport(), centerX, centerY)); // Hold for 2 seconds
    longPressSeq.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    driver.perform(Collections.singletonList(longPressSeq));
}

// ── Pinch / Zoom (multi-touch) ────────────────────────────────────────────

private void zoom(AndroidDriver driver, int centerX, int centerY) {
    PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
    PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");

    Sequence zoom1 = new Sequence(finger1, 0);
    Sequence zoom2 = new Sequence(finger2, 0);

    // Both fingers start close together at center
    zoom1.addAction(finger1.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX - 10, centerY - 10));
    zoom1.addAction(finger1.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    zoom1.addAction(finger1.createPointerMove(Duration.ofMillis(600),
        PointerInput.Origin.viewport(), centerX - 200, centerY - 200)); // Move outward

    zoom2.addAction(finger2.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX + 10, centerY + 10));
    zoom2.addAction(finger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    zoom2.addAction(finger2.createPointerMove(Duration.ofMillis(600),
        PointerInput.Origin.viewport(), centerX + 200, centerY + 200)); // Move outward

    zoom1.addAction(finger1.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    zoom2.addAction(finger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    driver.perform(Arrays.asList(zoom1, zoom2));
}

// ── Scroll using AndroidUIAutomator (simplest for Android) ───────────────

// Scroll down until element with text "Settings" is visible
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true))" +
    ".scrollIntoView(new UiSelector().text(\"Settings\"))"
));

// ── Android-specific execute commands ─────────────────────────────────────

// Hide keyboard
driver.hideKeyboard();

// Navigate back
driver.navigate().back();

// Press HOME button
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.HOME));

// Press BACK button
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));

// ── iOS-specific scroll ────────────────────────────────────────────────────

// Scroll to element using iOS-specific mobile command
Map<String, Object> params = new HashMap<>();
params.put("direction", "down");
params.put("predicateString", "label == 'Settings'");
params.put("toVisible", true);
driver.executeScript("mobile: scroll", params);
```

---

## 8. Handling Hybrid Apps — Context Switching

Hybrid apps have two layers:
1. **NATIVE_APP** — the native shell (buttons, navigation bars, status bars)
2. **WEBVIEW_com.example.myapp** — an embedded browser rendering HTML content

```java
import java.util.Set;

// ── Get all available contexts ─────────────────────────────────────────────
Set<String> contexts = driver.getContextHandles();
// Example output:
// [NATIVE_APP, WEBVIEW_com.example.myapp, WEBVIEW_chrome]
System.out.println("Available contexts: " + contexts);

// ── Switch to WebView ─────────────────────────────────────────────────────
// Method 1: Switch to a specific context by name
driver.context("WEBVIEW_com.example.myapp");

// Method 2: Switch to the first non-native context
for (String context : contexts) {
    if (!context.equals("NATIVE_APP")) {
        driver.context(context);
        break;
    }
}

// ── Interact with web content (using CSS/XPath) ────────────────────────────
// After switching to WebView, you can use Selenium web locators
driver.findElement(By.cssSelector("input[name='email']")).sendKeys("user@example.com");
driver.findElement(By.cssSelector("button[type='submit']")).click();

WebElement heading = driver.findElement(By.tagName("h1"));
System.out.println("Page heading: " + heading.getText());

// ── Switch back to native layer ────────────────────────────────────────────
driver.context("NATIVE_APP");

// Back to native — use native locators again
driver.findElement(AppiumBy.accessibilityId("Back Button")).click();

// ── Check current context ─────────────────────────────────────────────────
String currentContext = driver.getContext();
System.out.println("Current context: " + currentContext);
```

### Enable WebView Debugging (Required for hybrid app testing)

For Chrome-based WebViews on Android, you must enable remote debugging in the app:

```java
// In the Android app's WebView setup (app code — ask developer to add):
WebView webView = (WebView) findViewById(R.id.webView);
webView.getSettings().setJavaScriptEnabled(true);
WebView.setWebContentsDebuggingEnabled(true); // Required for Appium

// And add to Desired Capabilities:
options.setChromedriverExecutable("/path/to/chromedriver");
// Or let Appium manage it:
options.setAutoWebview(true); // Automatically switch to webview
```

---

## 9. Real Device vs Emulator Setup

### Android Emulator (AVD)

```java
UiAutomator2Options options = new UiAutomator2Options();
options.setPlatformName("Android");
options.setAutomationName("UiAutomator2");
options.setDeviceName("emulator-5554");   // From 'adb devices' output
options.setAvd("Pixel_6_API_33");         // AVD name — Appium will launch it
options.setAvdLaunchTimeout(Duration.ofSeconds(60));
options.setAppPackage("com.example.myapp");
options.setAppActivity(".MainActivity");
```

### Android Real Device

```java
UiAutomator2Options options = new UiAutomator2Options();
options.setPlatformName("Android");
options.setAutomationName("UiAutomator2");
options.setDeviceName("My Pixel 6 Device"); // Any name (descriptive)
options.setUdid("RF8M31ABCDE");             // Device serial from 'adb devices'
options.setApp("/path/to/app-release.apk");

// Required device settings (do manually on the device):
// Settings → Developer Options → USB Debugging = ON
// Settings → Developer Options → USB Debugging (Security Settings) = ON (some devices)
// Trust the computer when prompted
```

**Commands to verify Android real device:**
```bash
adb devices          # Should show device serial and 'device' status
adb -s RF8M31ABCDE shell getprop ro.build.version.release  # Get Android version
```

### iOS Simulator

```java
XCUITestOptions options = new XCUITestOptions();
options.setPlatformName("iOS");
options.setAutomationName("XCUITest");
options.setDeviceName("iPhone 14 Pro");   // Simulator name in Xcode
options.setPlatformVersion("16.4");
options.setApp("/path/to/MyApp.app");     // .app file for simulator
options.setNoReset(false);
```

**List available iOS simulators:**
```bash
xcrun simctl list devices
```

### iOS Real Device

```java
XCUITestOptions options = new XCUITestOptions();
options.setPlatformName("iOS");
options.setAutomationName("XCUITest");
options.setDeviceName("John's iPhone");
options.setUdid("00008030-001A60C42E08802E");  // From Xcode → Window → Devices
options.setBundleId("com.example.MyApp");       // App must be installed
options.setXcodeOrgId("ABC123DEF456");          // Apple Developer Team ID
options.setXcodeSigningId("iPhone Developer");
options.setUpdatedWdaDeploymentTarget("16.0");
```

---

## 10. Common Appium Server Commands

```bash
# ── Starting Appium Server ────────────────────────────────────────────────

# Start with default settings (port 4723)
appium

# Start on a specific port
appium --port 4724

# Start with specific base path (Appium 2.x default is /)
appium --base-path /

# Start with logging to file
appium --log appium-server.log

# Start with debug logging
appium --log-level debug

# Start with relaxed security (allows file system access etc.)
appium --relaxed-security

# Start Appium 2 server
appium server --port 4723 --base-path /

# ── Driver Management (Appium 2.x) ───────────────────────────────────────

# List all available drivers (from npm registry)
appium driver list

# List installed drivers
appium driver list --installed

# Install a driver
appium driver install uiautomator2
appium driver install xcuitest

# Update a driver
appium driver update uiautomator2

# Uninstall a driver
appium driver uninstall uiautomator2

# ── ADB Commands (Android) ────────────────────────────────────────────────

# List connected devices and emulators
adb devices

# Connect to a device over WiFi (TCP/IP)
adb tcpip 5555
adb connect 192.168.1.105:5555

# Install an APK
adb -s emulator-5554 install -r /path/to/app.apk

# Uninstall an app
adb -s emulator-5554 uninstall com.example.myapp

# Capture screenshot
adb exec-out screencap -p > screenshot.png

# Start an activity
adb shell am start -n com.example.myapp/.MainActivity

# Get device info
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release

# Clear app data
adb shell pm clear com.example.myapp

# Grant permissions
adb shell pm grant com.example.myapp android.permission.CAMERA

# ── iOS / xcrun Commands ──────────────────────────────────────────────────

# List simulators
xcrun simctl list devices

# Boot a simulator
xcrun simctl boot "iPhone 14 Pro"

# Install app on simulator
xcrun simctl install booted /path/to/MyApp.app

# Launch app on simulator
xcrun simctl launch booted com.example.MyApp

# Shutdown all simulators
xcrun simctl shutdown all

# Get device UDIDs (real devices)
instruments -s devices
# Or: xcrun xctrace list devices

# ── Maven Commands ────────────────────────────────────────────────────────

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=LoginTest

# Run specific test method
mvn test -Dtest=LoginTest#testValidLogin

# Run with TestNG suite
mvn test -DsuiteXmlFile=testng.xml

# Run with system properties
mvn test -Dplatform=android -DdeviceName=emulator-5554
```

---

## 11. Full Test Example (Java)

### BaseTest.java

```java
// src/test/java/com/example/base/BaseTest.java
package com.example.base;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BaseTest {

    protected AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName("emulator-5554");
        options.setAppPackage("com.example.myapp");
        options.setAppActivity("com.example.myapp.LoginActivity");
        options.setNoReset(true);
        options.setAutoGrantPermissions(true);
        options.setNewCommandTimeout(Duration.ofSeconds(90));

        driver = new AndroidDriver(
            new URL("http://127.0.0.1:4723"),
            options
        );
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
```

### LoginTest.java

```java
// src/test/java/com/example/tests/LoginTest.java
package com.example.tests;

import com.example.base.BaseTest;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    // Locators
    private static final String EMAIL_FIELD_ID = "com.example.myapp:id/email_input";
    private static final String PASSWORD_FIELD_ID = "com.example.myapp:id/password_input";
    private static final String LOGIN_BUTTON_ID = "com.example.myapp:id/login_button";
    private static final String ERROR_MSG_ID = "com.example.myapp:id/error_message";
    private static final String WELCOME_TEXT_ID = "com.example.myapp:id/welcome_text";

    @Test(description = "Valid credentials navigate to dashboard")
    public void testValidLogin() {
        // Arrange
        WebElement emailField = driver.findElement(AppiumBy.id(EMAIL_FIELD_ID));
        WebElement passwordField = driver.findElement(AppiumBy.id(PASSWORD_FIELD_ID));
        WebElement loginButton = driver.findElement(AppiumBy.id(LOGIN_BUTTON_ID));

        // Act
        emailField.clear();
        emailField.sendKeys("user@example.com");
        passwordField.clear();
        passwordField.sendKeys("Password123!");
        loginButton.click();

        // Assert
        WebElement welcomeText = driver.findElement(AppiumBy.id(WELCOME_TEXT_ID));
        Assert.assertTrue(welcomeText.isDisplayed(), "Welcome message should be visible");
        Assert.assertEquals(welcomeText.getText(), "Welcome, John!");
    }

    @Test(description = "Invalid credentials show error message")
    public void testInvalidLogin() {
        // Arrange & Act
        driver.findElement(AppiumBy.id(EMAIL_FIELD_ID)).sendKeys("wrong@example.com");
        driver.findElement(AppiumBy.id(PASSWORD_FIELD_ID)).sendKeys("wrongpassword");
        driver.findElement(AppiumBy.id(LOGIN_BUTTON_ID)).click();

        // Assert
        WebElement errorMsg = driver.findElement(AppiumBy.id(ERROR_MSG_ID));
        Assert.assertTrue(errorMsg.isDisplayed(), "Error message should appear");
        Assert.assertEquals(errorMsg.getText(), "Invalid email or password. Please try again.");
    }

    @Test(description = "Empty email shows validation error")
    public void testEmptyEmailValidation() {
        driver.findElement(AppiumBy.id(EMAIL_FIELD_ID)).sendKeys("");
        driver.findElement(AppiumBy.id(PASSWORD_FIELD_ID)).sendKeys("Password123!");
        driver.findElement(AppiumBy.id(LOGIN_BUTTON_ID)).click();

        WebElement emailError = driver.findElement(
            AppiumBy.androidUIAutomator(
                "new UiSelector().resourceId(\"com.example.myapp:id/email_error\")"
            )
        );
        Assert.assertEquals(emailError.getText(), "Email is required");
    }

    @Test(description = "Swipe and verify list items")
    public void testScrollingProductList() throws InterruptedException {
        // Login first
        testValidLogin();

        // Navigate to products
        driver.findElement(AppiumBy.accessibilityId("Products Tab")).click();

        // Scroll down using UIAutomator2
        driver.findElement(AppiumBy.androidUIAutomator(
            "new UiScrollable(new UiSelector().scrollable(true))" +
            ".scrollIntoView(new UiSelector().text(\"Product 20\"))"
        ));

        WebElement product20 = driver.findElement(AppiumBy.androidUIAutomator(
            "new UiSelector().text(\"Product 20\")"
        ));
        Assert.assertTrue(product20.isDisplayed(), "Product 20 should be visible after scrolling");
    }
}
```

---

## 12. Troubleshooting

### Problem 1: App Not Launching — Session Creation Failed

**Error:**
```
org.openqa.selenium.SessionNotCreatedException: Could not start a new session.
```

**Causes and fixes:**

1. **Appium server not running** — Start it: `appium`
2. **Wrong appPackage/appActivity** — Verify with `adb shell dumpsys window | grep mCurrentFocus`
3. **APK not installed** — Install first: `adb install /path/to/app.apk`
4. **UiAutomator2 driver not installed** — `appium driver install uiautomator2`
5. **Device not found** — Verify: `adb devices` shows your device; check USB cable and debugging mode

---

### Problem 2: Element Not Found

**Error:**
```
NoSuchElementException: An element could not be located on the page
```

**Fixes:**

1. **Wrong locator** — Use Appium Inspector to find the correct element attributes
   - Download: https://github.com/appium/appium-inspector
   - Connect to your session and visually inspect the UI tree

2. **Element in different context** — Check if you need to switch to WebView: `driver.getContextHandles()`

3. **Implicit wait not set** — Add: `driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))`

4. **Element not yet visible** — Add explicit wait:
```java
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
WebElement element = wait.until(
    ExpectedConditions.visibilityOfElementLocated(AppiumBy.id("com.example.myapp:id/btn"))
);
```

---

### Problem 3: UIAutomator2 Server Issues

**Error:**
```
Error: UiAutomator2 server is not running
```

**Fixes:**
```bash
# Clear UIAutomator2 APKs from device and reinstall
adb shell pm uninstall io.appium.uiautomator2.server
adb shell pm uninstall io.appium.uiautomator2.server.test

# Then restart your test — Appium will reinstall automatically

# Or check if port 6790 is in use (UIAutomator2 server port)
adb forward tcp:6790 tcp:6790
```

---

### Problem 4: WDA (WebDriverAgent) Issues on iOS

**Error:**
```
xcodebuild failed with code 65
```

**Fixes:**
1. Open `~/.appium/node_modules/appium-xcuitest-driver/WebDriverAgent/WebDriverAgent.xcodeproj` in Xcode
2. Sign with your Apple Developer account (Team ID)
3. Set a unique Bundle ID suffix to avoid conflicts
4. Trust the developer on the device: Settings → General → VPN & Device Management

---

### Problem 5: Context Not Found for Hybrid App

**Error:**
```
WebviewNotFound: No webview contexts found
```

**Fixes:**
1. Ensure `WebView.setWebContentsDebuggingEnabled(true)` is in the app code
2. Wait for the WebView to fully load before getting contexts:
```java
Thread.sleep(3000); // Wait for WebView to initialize
Set<String> contexts = driver.getContextHandles();
```
3. Add chromedriver capability: `options.setChromedriverExecutable("/path/to/chromedriver")`
4. Ensure Chrome version on device matches the chromedriver version

---

## 13. Interview Q&A

**Q1. Explain the Appium architecture. What happens when a test script calls `driver.click()`?**

**A:** The test script (in Java/Python/etc.) calls `driver.click()`, which the Appium client library translates into an HTTP POST request using the W3C WebDriver protocol, sent to the Appium Server running on `localhost:4723`. The Appium Server receives this request and routes it to the installed driver plugin — either UiAutomator2 for Android or XCUITest for iOS. For Android, UIAutomator2 receives the command via the UIAutomator2 server APK installed on the device, which interacts with the app using Android's Accessibility Service. For iOS, Appium installs WebDriverAgent (WDA) on the device, which acts as an HTTP server that receives commands and uses Apple's XCUITest framework to interact with the app. The result travels back: device → driver → Appium Server → HTTP response → test script.

---

**Q2. What are Desired Capabilities and what is the difference between `appPackage` + `appActivity` and `app`?**

**A:** Desired Capabilities (caps) are a JSON key-value map sent to the Appium server at session creation time that tells it: which platform, which device, which automation driver to use, and which app to test. `appPackage` + `appActivity` are used when the app is already installed on the device — Appium launches the specific activity without reinstalling the APK. `app` provides a file path to the APK (or IPA for iOS); Appium installs it on the device before launching. Use `appPackage`/`appActivity` in CI where the app is pre-installed, and use `app` when you need to install a fresh build for each test run. For iOS, the equivalent of `appPackage`+`appActivity` is `bundleId`.

---

**Q3. What is the difference between `AndroidUIAutomator` and `XPath` as locator strategies, and when would you choose each?**

**A:** `AndroidUIAutomator` uses Google's UIAutomator2 DSL (`UiSelector`, `UiScrollable`) running natively on the Android device. It is significantly faster than XPath because it queries the native accessibility tree directly without XML parsing. Crucially, it has built-in scroll capability (`scrollIntoView`) that XPath lacks. XPath traverses the XML representation of the view hierarchy, which Appium serializes — this serialization is slow, especially for complex screens. Use `AndroidUIAutomator` for Android whenever you need scrolling, or when performance matters. Use XPath as a last resort for complex hierarchical queries not expressible in UISelector, or when you need cross-platform locators.

---

**Q4. How do you automate a swipe gesture in Appium 2.x and why is the old `TouchAction` API no longer recommended?**

**A:** The old `TouchAction` and `MultiTouchAction` APIs are Appium-proprietary and have been deprecated. Appium 2.x recommends the W3C Actions API (available in Selenium 4 and Appium 8.x Java client). You create a `PointerInput` of kind `TOUCH`, build a `Sequence` of `createPointerMove`, `createPointerDown`, `createPointerMove` (to end position with a duration for speed), and `createPointerUp` actions, then call `driver.perform()`. The W3C approach is standardized and portable across different WebDriver implementations. For Android, an even simpler option is `UiScrollable.scrollIntoView()` via `AndroidUIAutomator` locator, which handles scrolling as part of the element lookup.

---

**Q5. Explain context switching in hybrid app testing. What do you check when the WebView context is not found?**

**A:** Hybrid apps contain a `WebView` component that renders web content. Appium initially connects in `NATIVE_APP` context, where only native locators work. To interact with web content, you call `driver.getContextHandles()` to list all contexts (e.g., `[NATIVE_APP, WEBVIEW_com.example.myapp]`), then `driver.context("WEBVIEW_com.example.myapp")` to switch. After switching, standard Selenium locators (CSS, XPath, ID) work inside the WebView. You switch back with `driver.context("NATIVE_APP")`. If the WebView context is not found: (1) verify the app has `WebView.setWebContentsDebuggingEnabled(true)` enabled; (2) ensure chromedriver version matches the Chrome version on the device; (3) wait for the WebView to fully load before calling `getContextHandles()`; (4) check the `autoWebview` capability is not accidentally set to `true`, which might be switching context before the WebView loads.

---

**Q6. What is the difference between `noReset`, `fullReset`, and using neither?**

**A:**
- **Neither set (default):** Appium resets the app to its initial state before each session by clearing app data — essentially like a fresh install of data, but the app remains installed. Safe default.
- **`noReset: true`:** App data and state are preserved between sessions. Useful when the test setup is expensive (e.g., after login) and you want to test from a logged-in state, or when testing in-progress app state.
- **`fullReset: true`:** Uninstalls the app completely at the end of the session and reinstalls it at the start. Ensures a completely clean state including preferences and local storage. Slowest option; use when you need guaranteed clean state.

---

**Q7. How is iOS testing fundamentally different from Android testing in Appium?**

**A:** Key differences:

| Aspect | Android | iOS |
|---|---|---|
| Driver | UiAutomator2 (Google) | XCUITest (Apple) |
| Test app | UIAutomator2 APK installed on device | WebDriverAgent IPA signed and installed |
| Locator language | AndroidUIAutomator (UiSelector DSL) | iOS Predicate String / Class Chain |
| Simulator testing | Free (AVDs in Android Studio) | Requires macOS + Xcode (free simulators) |
| Real device | Any device with USB debugging ON | Requires paid Apple Developer account for WDA signing |
| Context types | NATIVE_APP, WEBVIEW_com.xxx | NATIVE_APP, WEBVIEW_x |
| OS access | ADB provides deep OS access | Sandboxed; limited OS access |

iOS testing requires a macOS machine because Xcode (required for XCUITest) only runs on macOS. Android testing can run on any OS.

---

**Q8. How would you handle an Android app that requires camera/location permissions that appear as system dialogs?**

**A:** There are three approaches:

1. **`autoGrantPermissions: true` capability** — Appium automatically grants all permissions listed in the AndroidManifest before the session. This is the simplest approach and works for most cases. Set it in caps: `options.setAutoGrantPermissions(true)`.

2. **ADB pre-grant** — Grant permissions before the test using ADB: `adb shell pm grant com.example.myapp android.permission.CAMERA`. Use in CI pipeline setup scripts.

3. **Handle the dialog in the test** — If the dialog appears during the test, use UIAutomator2 to click "Allow": `driver.findElement(AppiumBy.id("com.android.permissioncontroller:id/permission_allow_button")).click()`. This approach is more realistic (tests the actual permission dialog flow) but fragile because the dialog UI changes between Android versions.

For CI pipelines, option 1 or 2 is preferred to avoid flakiness from permission dialogs interfering with the test flow.

---

*End of File 06 — Appium Complete Guide*
