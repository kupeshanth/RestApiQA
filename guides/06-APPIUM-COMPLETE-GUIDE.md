# Appium — Complete Interview Q&A Guide | Mobile Testing | Android + iOS

---

## SECTION 1: FOUNDATIONS — What Appium Is and How It Works

---

**Q1: What is Appium and what makes it different from device-specific automation tools?**

**A:** Appium is an open-source, cross-platform mobile test automation framework that lets you write tests for Android native apps, iOS native apps, hybrid apps (WebView-based), and mobile browsers — all using the W3C WebDriver protocol. Unlike Espresso (Android-only, Java/Kotlin only) or XCUITest (iOS-only, Swift/Obj-C only), Appium lets you write tests in any language (Java, Python, JavaScript, Ruby, C#) without modifying the app under test.

Key principles:
- One protocol (W3C WebDriver) works across all platforms
- Tests run on real devices or emulators without touching app source code
- Same architectural concepts as Selenium WebDriver

| Feature | Appium | Espresso | XCUITest |
|---|---|---|---|
| Cross-platform | Yes | No (Android only) | No (iOS only) |
| Language choice | Any (Java, Python, JS...) | Java/Kotlin only | Swift/Obj-C only |
| App modification needed | No | Yes (test APK) | No |
| Hybrid app support | Yes | Limited | Limited |
| Open source | Yes | Yes | Yes |

Real-world context: A team testing both an Android and iOS app can share the same test logic, Page Objects, and framework infrastructure, only swapping out the driver initialization and locators where platforms differ.

Common mistake: Assuming Appium has the same speed as Espresso. Appium is slower because it adds an HTTP communication layer. Espresso runs in-process on the device.

---

**Q2: Explain the Appium architecture end-to-end. What happens when your test calls `driver.click()`?**

**A:** Appium follows a client-server architecture over HTTP using the W3C WebDriver protocol.

```
┌──────────────────────────────────────────────────────┐
│         Test Script (Java / Python / JS)              │
│   driver.findElement(By.id("login_btn")).click();     │
└────────────────────┬─────────────────────────────────┘
                     │  HTTP POST /session/{id}/element/{el}/click
                     ▼
┌──────────────────────────────────────────────────────┐
│           Appium Server (Node.js)                     │
│       Listening on http://localhost:4723              │
│  - Parses W3C WebDriver HTTP request                  │
│  - Routes to installed driver plugin                  │
└──────────┬───────────────────────┬───────────────────┘
           │                       │
           ▼                       ▼
┌──────────────────┐   ┌───────────────────────────────┐
│  UiAutomator2    │   │      XCUITest Driver           │
│  Driver Plugin   │   │      (xcuitest plugin)         │
└────────┬─────────┘   └────────────┬──────────────────┘
         │                          │
         ▼                          ▼
┌──────────────────┐   ┌───────────────────────────────┐
│ Android Device / │   │  iOS Simulator / Real Device   │
│ Emulator         │   │  (WebDriverAgent installed)    │
│ (ADB + UIAuto2   │   │                               │
│  server APK)     │   │                               │
└──────────────────┘   └───────────────────────────────┘
```

Step-by-step flow:
1. Your Java test calls `driver.click()` — the Appium Java client translates this into an HTTP POST request
2. The request travels to the Appium Server (Node.js process) on port 4723
3. Appium Server routes the command to the correct driver plugin (UiAutomator2 for Android, XCUITest for iOS)
4. For Android: the UiAutomator2 server APK (installed on the device) receives the command via ADB and uses Android's Accessibility Service to perform the tap
5. For iOS: WebDriverAgent (WDA) — an app Appium installs on the device — receives the HTTP command and uses Apple's XCUITest framework to perform the action
6. The result travels back: device → driver plugin → Appium Server → HTTP response → Java client → your test

Component responsibilities:
- **ADB (Android Debug Bridge)**: Communication channel between the Appium server (on your machine) and the Android device
- **UIAutomator2 server APK**: Google's automation framework, installed as two APKs (`io.appium.uiautomator2.server` and its test) on the Android device
- **WebDriverAgent (WDA)**: An XCUITest-based app that Appium signs and installs on iOS, acting as an HTTP server on the device

---

**Q3: What is UIAutomator2 and how does it work internally?**

**A:** UIAutomator2 is Google's UI testing framework that ships as part of the Android SDK. It gives programmatic access to the Android Accessibility tree — the same data structure screen readers use to describe the UI. Appium uses UIAutomator2 as its Android driver by installing two APKs onto the device: a server APK and a test APK. These APKs run a local HTTP server on the device (port 6790 by default) and bridge between Appium Server commands and Android's actual UI interaction APIs.

What UIAutomator2 can do that makes it powerful:
- Interact with any app — including system dialogs (permissions, alerts) — because it operates at the accessibility layer, not inside the app process
- Use `UiScrollable` to scroll lists and find elements in one command
- Use `UiSelector` DSL to find elements by text, resource-id, class, index, etc.
- Access multi-window scenarios

```bash
# Install UiAutomator2 driver in Appium 2.x
appium driver install uiautomator2

# Verify it is installed
appium driver list --installed
# Output: uiautomator2@2.x.x [installed]
```

Common mistake: UIAutomator2 requires USB debugging to be enabled on the Android device. Without it, `adb devices` shows `unauthorized` and the session fails.

---

**Q4: What is XCUITest and what is WebDriverAgent (WDA)?**

**A:** XCUITest is Apple's native UI testing framework, available since iOS 9. It runs tests in a separate process on the device using Apple's Accessibility APIs. Appium's XCUITest driver wraps XCUITest by building and installing an app called WebDriverAgent (WDA) onto the iOS device or simulator.

WDA works like this:
- Appium compiles WDA using Xcode and signs it with your Apple Developer credentials
- WDA is installed on the device and launched — it starts an HTTP server on the device (port 8100)
- Appium communicates with WDA over this HTTP server, translating W3C WebDriver commands into XCUITest framework calls
- WDA interacts with the app using the same accessibility APIs XCUITest uses

Key constraints:
- XCUITest only runs on macOS — you must have a Mac to test iOS (Xcode is macOS-only)
- Real device testing requires a paid Apple Developer account to sign WDA
- Simulator testing is free (no signing required)

```bash
# Install XCUITest driver
appium driver install xcuitest

# Check iOS setup readiness
appium-doctor --ios

# List available iOS simulators
xcrun simctl list devices
```

---

**Q5: How do you install and start the Appium server? Walk through all steps.**

**A:** Appium 2.x uses a plugin architecture where the core server and platform drivers are installed separately.

```bash
# Step 1: Install Node.js (prerequisite)
# Download from https://nodejs.org — version 18+

# Step 2: Install Appium server globally
npm install -g appium

# Verify installation
appium --version
# Expected output: 2.x.x

# Step 3: Install Appium Doctor (diagnoses setup problems)
npm install -g appium-doctor
appium-doctor --android    # Check Android environment
appium-doctor --ios        # Check iOS environment (macOS only)

# Step 4: Install platform drivers
appium driver install uiautomator2    # Android
appium driver install xcuitest        # iOS (macOS only)

# Verify drivers are installed
appium driver list --installed

# Step 5: Start the Appium server
appium                                # Starts on default port 4723
appium --port 4724                    # Start on custom port
appium --log appium.log               # Log to file
appium --log-level debug              # Verbose debug logging
appium --relaxed-security             # Allow file system and other features
appium server --base-path /           # Explicit base path (Appium 2.x)

# Step 6: Android environment setup
export ANDROID_HOME=$HOME/Library/Android/sdk          # macOS
# export ANDROID_HOME=C:\Users\YourName\AppData\Local\Android\Sdk   # Windows
export PATH=$PATH:$ANDROID_HOME/platform-tools         # adb
export PATH=$PATH:$ANDROID_HOME/emulator               # emulator
export PATH=$PATH:$ANDROID_HOME/tools

# Verify ADB works
adb devices
# Should show: List of devices attached

# Step 7: Start an Android emulator
emulator -list-avds                           # List available AVDs
emulator -avd Pixel_6_API_33 -no-snapshot-load   # Start specific AVD
adb devices                                   # Confirm: emulator-5554  device
```

Maven dependencies for the Java client:

```xml
<dependencies>
  <!-- Appium Java Client -->
  <dependency>
    <groupId>io.appium</groupId>
    <artifactId>java-client</artifactId>
    <version>8.5.1</version>
  </dependency>

  <!-- Selenium WebDriver (bundled in java-client but explicit is safer) -->
  <dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.15.0</version>
  </dependency>

  <!-- TestNG runner -->
  <dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.8.0</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

Common mistake: Appium 1.x used a single package with all drivers bundled. In Appium 2.x, drivers must be installed separately. Running `appium` without installing `uiautomator2` first results in `No driver found for automationName 'UiAutomator2'`.

---

## SECTION 2: DESIRED CAPABILITIES

---

**Q6: What are Desired Capabilities in Appium? Why are they needed?**

**A:** Desired Capabilities (caps) are a JSON key-value map sent to the Appium server when creating a new session. They tell Appium everything it needs to set up the session: which platform (Android/iOS), which device, which automation driver to use, and which app to launch. Without capabilities, Appium does not know what kind of session to create.

In Appium 2.x with the Java client, capabilities are set using strongly-typed Options classes (`UiAutomator2Options` for Android, `XCUITestOptions` for iOS) rather than the old `DesiredCapabilities` map. The options classes provide compile-time type safety and IDE autocompletion.

```java
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.android.AndroidDriver;
import java.net.URL;

UiAutomator2Options options = new UiAutomator2Options();
options.setPlatformName("Android");
options.setAutomationName("UiAutomator2");
options.setDeviceName("emulator-5554");
options.setAppPackage("com.example.myapp");
options.setAppActivity("com.example.myapp.MainActivity");

AndroidDriver driver = new AndroidDriver(
    new URL("http://127.0.0.1:4723"),
    options
);
```

Common mistake: Using the deprecated `DesiredCapabilities` map in Appium 2.x leads to warnings and potential incompatibilities. Always use `UiAutomator2Options` or `XCUITestOptions`.

---

**Q7: List all important Desired Capabilities for Android with explanations.**

**A:**

```java
import io.appium.java_client.android.options.UiAutomator2Options;
import java.time.Duration;
import java.net.URL;

UiAutomator2Options options = new UiAutomator2Options();

// ── REQUIRED ─────────────────────────────────────────────────────────────────

options.setPlatformName("Android");
// Always "Android" — tells Appium which OS

options.setAutomationName("UiAutomator2");
// Which driver plugin to use — must match installed driver

options.setDeviceName("Pixel_6_API_33");
// AVD name for emulator, or device serial for real device
// Get from: adb devices (emulator-5554 or RF8M31ABCDE)

options.setPlatformVersion("13.0");
// Android OS version — optional but helps Appium select correct driver version

// ── APP TO TEST (choose one approach) ────────────────────────────────────────

// Option A: Launch an already-installed app by package + activity
options.setAppPackage("com.example.myapp");
options.setAppActivity("com.example.myapp.LoginActivity");
// activity can be relative: ".LoginActivity" (Appium prepends package name)

// Option B: Install APK from path then launch
options.setApp("/Users/yourname/Desktop/my-app-debug.apk");

// Option C: Test Chrome mobile browser
options.setBrowserName("Chrome");

// ── APP STATE CONTROL ─────────────────────────────────────────────────────────

options.setNoReset(true);
// true = do not clear app data between sessions
// false (default) = clear app data (reset to installed state)

options.setFullReset(false);
// true = uninstall app at session end, reinstall at next session start
// Slowest but guarantees a completely clean state

// ── PERMISSIONS ──────────────────────────────────────────────────────────────

options.setAutoGrantPermissions(true);
// Automatically grant all permissions listed in AndroidManifest
// Prevents permission dialogs from interrupting tests

// ── TIMEOUTS ─────────────────────────────────────────────────────────────────

options.setNewCommandTimeout(Duration.ofSeconds(120));
// How long Appium waits for the next command before ending the session
// Increase for slow CI machines or long waits

options.setAdbExecTimeout(Duration.ofSeconds(30));
// How long to wait for ADB commands to complete

// ── EMULATOR SPECIFIC ─────────────────────────────────────────────────────────

options.setAvd("Pixel_6_API_33");
// AVD name — Appium will launch the emulator if not already running

options.setAvdLaunchTimeout(Duration.ofSeconds(90));
// How long to wait for the emulator to boot

// ── UNICODE INPUT ─────────────────────────────────────────────────────────────

options.setUnicodeKeyboard(true);
options.setResetKeyboard(true);
// Required for typing special characters (Chinese, Arabic, emojis)
// Installs a Unicode-capable keyboard and restores original after test

// ── CHROMEDRIVER (for hybrid apps) ────────────────────────────────────────────

options.setChromedriverExecutable("/path/to/chromedriver");
// Path to chromedriver matching Chrome version on device
// Required for WebView context switching

options.setAutoWebview(true);
// Automatically switch to WebView context when session starts
// Only use if you know the app always opens in WebView first

// ── INIT DRIVER ───────────────────────────────────────────────────────────────

AndroidDriver driver = new AndroidDriver(
    new URL("http://127.0.0.1:4723"),
    options
);
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
```

---

**Q8: List all important Desired Capabilities for iOS with explanations.**

**A:**

```java
import io.appium.java_client.ios.options.XCUITestOptions;
import io.appium.java_client.ios.IOSDriver;
import java.time.Duration;
import java.net.URL;

XCUITestOptions options = new XCUITestOptions();

// ── REQUIRED ─────────────────────────────────────────────────────────────────

options.setPlatformName("iOS");
options.setAutomationName("XCUITest");

options.setDeviceName("iPhone 14 Pro");
// Simulator: exact name from Xcode/xcrun simctl list
// Real device: any descriptive name (UDID identifies the device)

options.setPlatformVersion("16.4");
// iOS version — used to select the correct simulator

// ── APP TO TEST ───────────────────────────────────────────────────────────────

// Option A: Install from .app file (simulators only)
options.setApp("/Users/yourname/Desktop/MyApp.app");

// Option B: Launch already-installed app by Bundle ID
options.setBundleId("com.example.MyApp");
// Bundle ID is the unique app identifier in iOS (equivalent of appPackage on Android)

// Option C: Test Safari mobile browser
options.setBrowserName("Safari");

// ── REAL DEVICE SPECIFIC ──────────────────────────────────────────────────────

options.setUdid("00008030-001A60C42E08802E");
// Unique Device Identifier — required for real devices
// Find it: Xcode → Window → Devices & Simulators, or: instruments -s devices

options.setXcodeOrgId("ABC123DEF456");
// Apple Developer Team ID — required to sign WebDriverAgent for real devices
// Find it: Apple Developer portal → Membership → Team ID

options.setXcodeSigningId("iPhone Developer");
// Signing identity string — usually "iPhone Developer" (development) or "iPhone Distribution"

// ── ALERTS AND PERMISSIONS ────────────────────────────────────────────────────

options.setAutoAcceptAlerts(true);
// Auto-dismiss iOS permission dialogs (Camera, Location, Notifications, etc.)
// Use carefully — if you need to test the permission flow, set to false

options.setAutoDismissAlerts(false);
// Auto-dismiss means clicking Cancel on alerts — different from auto-accept

// ── TIMEOUTS ─────────────────────────────────────────────────────────────────

options.setWdaLaunchTimeout(Duration.ofSeconds(120));
// How long to wait for WebDriverAgent to start on the device
// Increase for first run (WDA must compile and install)

options.setWdaConnectionTimeout(Duration.ofSeconds(60));
// How long to wait for WDA HTTP server to respond

options.setNewCommandTimeout(Duration.ofSeconds(90));

// ── APP STATE ─────────────────────────────────────────────────────────────────

options.setNoReset(false);
options.setFullReset(false);

// ── INIT DRIVER ───────────────────────────────────────────────────────────────

IOSDriver driver = new IOSDriver(
    new URL("http://127.0.0.1:4723"),
    options
);
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
```

---

**Q9: What is `appPackage` and `appActivity` for Android? How do you find them?**

**A:** `appPackage` is the unique identifier of an Android app (the `package` attribute in `AndroidManifest.xml`, e.g., `com.example.myapp`). `appActivity` is the fully qualified name of the Activity (screen) to launch when starting the test (e.g., `com.example.myapp.LoginActivity`).

Appium uses these to launch an already-installed app with `adb shell am start -n <package>/<activity>` instead of reinstalling an APK.

How to find them:

```bash
# Method 1: While the app is open, run:
adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'
# Output: mCurrentFocus=Window{...u0 com.example.myapp/com.example.myapp.MainActivity}
# Package = com.example.myapp
# Activity = com.example.myapp.MainActivity

# Method 2: Use adb logcat while launching the app
adb logcat | grep -i "activitymanager" | grep "START"
# Look for: cmp=com.example.myapp/.MainActivity

# Method 3: Inspect the APK directly with aapt
aapt dump badging /path/to/app.apk | grep -E "package:|launchable-activity"
# Output:
# package: name='com.example.myapp' versionCode='12' ...
# launchable-activity: name='com.example.myapp.MainActivity'

# Method 4: On device, list all installed packages
adb shell pm list packages | grep example
# Then find the launcher activity
adb shell cmd package resolve-activity --brief com.example.myapp
```

Common mistake: The activity can be specified as a relative path (`.MainActivity`) only if `appPackage` is set. It is safer to always use the fully qualified name to avoid `ActivityNotFoundException`.

---

**Q10: What is `bundleId` for iOS and how is it different from `appPackage` on Android?**

**A:** `bundleId` is the unique identifier of an iOS app, defined in the app's `Info.plist` as `CFBundleIdentifier` (e.g., `com.example.MyApp`). It serves the same purpose as `appPackage` on Android — uniquely identifying the app. Unlike Android, iOS apps do not have separate `Activity` equivalents; the app's entry point is determined by its `UIApplicationDelegate`, so only the `bundleId` is needed to launch it.

How to find the bundleId:

```bash
# Method 1: From the .ipa or .app file
# Unzip the .ipa and read Info.plist:
unzip MyApp.ipa -d MyApp_extracted
/usr/libexec/PlistBuddy -c "Print CFBundleIdentifier" MyApp_extracted/Payload/MyApp.app/Info.plist

# Method 2: From a running app on simulator
xcrun simctl listapps booted | grep BundleID

# Method 3: Xcode → Project → Signing & Capabilities → Bundle Identifier

# Method 4: On a real device (if paired)
ideviceinstaller -l
```

Usage in capabilities:

```java
XCUITestOptions options = new XCUITestOptions();
options.setBundleId("com.example.MyApp");
// App must already be installed on device/simulator
// Appium launches it without reinstalling
```

---

**Q11: What is UDID and how do you find it for Android and iOS?**

**A:** UDID (Unique Device Identifier) is a hardware serial number that uniquely identifies a specific physical device. In Appium, it is used to target a specific device when multiple devices are connected to the same machine.

Android UDID (called "serial number"):

```bash
# Find Android device serial number
adb devices
# Output:
# List of devices attached
# emulator-5554    device        ← emulator serial
# RF8M31ABCDE      device        ← real device serial

# Get serial of a single connected device
adb get-serialno

# Use in capabilities:
UiAutomator2Options options = new UiAutomator2Options();
options.setUdid("RF8M31ABCDE");
// or
options.setDeviceName("RF8M31ABCDE");  // deviceName can also hold the serial
```

iOS UDID:

```bash
# Method 1: Xcode
# Window → Devices and Simulators → select device → copy identifier

# Method 2: Command line (macOS)
xcrun xctrace list devices
# Output:
# == Devices ==
# John's iPhone (16.4) (00008030-001A60C42E08802E)

# Method 3: instruments
instruments -s devices

# Method 4: ideviceinfo (requires libimobiledevice)
brew install libimobiledevice
ideviceinfo -k UniqueDeviceID

# Use in capabilities:
XCUITestOptions options = new XCUITestOptions();
options.setUdid("00008030-001A60C42E08802E");
```

Common mistake: On iOS, every real device test requires the UDID to be registered in your Apple Developer account for development. Without registration, WDA cannot be signed and installed.

---

**Q12: What is the difference between `noReset`, `fullReset`, and the default reset behavior?**

**A:** These capabilities control what Appium does with app data between test sessions.

| Capability | On session start | On session end | When to use |
|---|---|---|---|
| Default (neither set) | Clear app data (reset to installed state) | App remains installed | Safe default — clean data, no reinstall cost |
| `noReset: true` | Do nothing — preserve all app data | App remains with data | Tests that depend on prior state (e.g., logged-in) |
| `fullReset: true` | Uninstall, then reinstall app | Uninstall app | Guarantee absolutely clean state; slowest |

```java
UiAutomator2Options options = new UiAutomator2Options();

// Default behavior: clears app data before session
// No explicit setting needed — this is the Appium default

// noReset — preserve state
options.setNoReset(true);
// Use when: test depends on the user already being logged in from a previous run,
// or when you've set up complex data manually and don't want it wiped.

// fullReset — complete clean slate
options.setFullReset(true);
// Use when: tests must be completely isolated with no leftover app data,
// preferences, or cached content. Pay the reinstall cost on every run.
// On Android: uninstalls and reinstalls the APK.
// On iOS: uninstalls and reinstalls the app.
```

Real-world example: In a CI pipeline running a full regression suite, use the default reset to get clean app data without the overhead of reinstalling for every test. Use `noReset: true` only for performance tests where you need pre-populated data and `fullReset: true` for security testing where residual state could affect results.

Common mistake: Setting `noReset: true` globally causes state leakage between tests. A test that creates a user account leaves that account behind for subsequent tests, causing `user already exists` failures.

---

## SECTION 3: APP TYPES AND CONTEXT SWITCHING

---

**Q13: What is the difference between a native app, a hybrid app, and a mobile web app? How do you test each?**

**A:**

**Native app**: Built using the platform's native SDK — Android (Kotlin/Java) or iOS (Swift/Obj-C). Uses native UI components (TextView, Button, RecyclerView on Android; UILabel, UIButton, UITableView on iOS). Tested using native locators: resource-id/accessibility-id on Android, accessibility-id/predicate on iOS.

**Hybrid app**: A native app shell that contains one or more WebView components rendering HTML/CSS/JS content. The app has a dual layer — native UI elements (navigation bars, tabs, status bar) in the `NATIVE_APP` context, and web content in `WEBVIEW_com.example.app` context. Built with Ionic, Cordova, Capacitor, or similar frameworks. Testing requires context switching between native and WebView layers.

**Mobile web app**: A website accessed through a mobile browser (Chrome on Android, Safari on iOS). No APK/IPA is installed — you point Appium at the browser with `browserName: Chrome` or `Safari`, and use standard Selenium web locators (CSS, XPath, ID).

```java
// ── Native App ────────────────────────────────────────────────────────────────
UiAutomator2Options nativeOptions = new UiAutomator2Options();
nativeOptions.setAppPackage("com.example.nativeapp");
nativeOptions.setAppActivity(".MainActivity");
// Use: AppiumBy.id(), AppiumBy.accessibilityId(), AppiumBy.androidUIAutomator()

// ── Hybrid App ────────────────────────────────────────────────────────────────
UiAutomator2Options hybridOptions = new UiAutomator2Options();
hybridOptions.setAppPackage("com.example.hybridapp");
hybridOptions.setAppActivity(".MainActivity");
hybridOptions.setChromedriverExecutable("/path/to/chromedriver");
// Use: native locators for NATIVE_APP context, CSS/XPath for WEBVIEW context

// ── Mobile Web App ────────────────────────────────────────────────────────────
UiAutomator2Options webOptions = new UiAutomator2Options();
webOptions.setBrowserName("Chrome");
// No appPackage/appActivity needed
// Use: By.cssSelector(), By.xpath(), By.id() — standard Selenium locators
AndroidDriver webDriver = new AndroidDriver(new URL("http://127.0.0.1:4723"), webOptions);
webDriver.get("https://example.com");
webDriver.findElement(By.cssSelector("input[name='email']")).sendKeys("test@test.com");
```

---

**Q14: How do you test a hybrid app? What is context switching and why is it needed?**

**A:** A hybrid app has two distinct automation layers. When Appium connects, it starts in `NATIVE_APP` context where only native locators work. When you navigate to a WebView section of the app, the web content is inside an embedded browser that has its own DOM — you cannot use native locators to find HTML elements inside it. You must switch context to interact with the web layer.

```java
import java.util.Set;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

// Step 1: Start in NATIVE_APP context (default)
// Interact with native elements normally
driver.findElement(AppiumBy.accessibilityId("Open Web Content")).click();

// Step 2: Wait for WebView to load, then get all contexts
Thread.sleep(2000); // Give WebView time to initialize
Set<String> contexts = driver.getContextHandles();
System.out.println("Available contexts: " + contexts);
// Output: [NATIVE_APP, WEBVIEW_com.example.myapp]

// Step 3: Switch to WebView context
String webviewContext = contexts.stream()
    .filter(c -> c.startsWith("WEBVIEW"))
    .findFirst()
    .orElseThrow(() -> new RuntimeException("No WebView context found"));

driver.context(webviewContext);
System.out.println("Switched to: " + driver.getContext());

// Step 4: Interact with web content using standard Selenium locators
driver.findElement(By.cssSelector("input[name='email']")).sendKeys("user@example.com");
driver.findElement(By.cssSelector("button[type='submit']")).click();

String heading = driver.findElement(By.tagName("h1")).getText();
System.out.println("Page heading: " + heading);

// Step 5: Switch back to native layer when done with web content
driver.context("NATIVE_APP");
driver.findElement(AppiumBy.accessibilityId("Back Button")).click();
```

**Enabling WebView debugging** (required — app developer must add this to the Android app code):

```java
// In the Android app's source code (ask the developer to add this):
WebView myWebView = (WebView) findViewById(R.id.webview);
myWebView.getSettings().setJavaScriptEnabled(true);
WebView.setWebContentsDebuggingEnabled(true); // This is required for Appium/Chrome DevTools
```

Common mistake: Forgetting that `WebView.setWebContentsDebuggingEnabled(true)` is required. Without it, `driver.getContextHandles()` returns only `[NATIVE_APP]` even if a WebView is displayed.

---

**Q15: What do you check when `getContextHandles()` only returns `[NATIVE_APP]` and you expected a WebView?**

**A:** This is the most common hybrid app testing issue. Work through this checklist:

```java
// Diagnostic code — run this when WebView is not showing up
Set<String> contexts = driver.getContextHandles();
System.out.println("Contexts found: " + contexts);
// If this prints only [NATIVE_APP], check the following:

// Check 1: Is the WebView fully loaded?
Thread.sleep(3000); // Add a wait and try again
contexts = driver.getContextHandles();
System.out.println("Contexts after wait: " + contexts);

// Check 2: Log the current page source to see what's rendered
System.out.println(driver.getPageSource()); // Inspect for WebView element

// Check 3: Verify chromedriver version matches Chrome on device
// adb shell dumpsys package com.android.chrome | grep versionName
// Download matching chromedriver from: https://chromedriver.chromium.org

// Check 4: Add chromedriver path to capabilities
options.setChromedriverExecutable("/path/to/chromedriver");
// Or let Appium auto-download:
options.setChromedriverChromeMappingFile("/path/to/chromedriver_map.json");
```

Root causes in order of frequency:
1. App missing `WebView.setWebContentsDebuggingEnabled(true)` — app developer must add this
2. Wrong chromedriver version — Chrome on device v108 requires chromedriver 108
3. WebView not yet loaded — add explicit wait before calling `getContextHandles()`
4. Multiple WebViews — call `getContextHandles()` multiple times after each navigation to refresh the list

---

## SECTION 4: LOCATOR STRATEGIES

---

**Q16: What are all the locator strategies available for Android in Appium? Show code for each.**

**A:**

```java
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import io.appium.java_client.android.AndroidDriver;

// ── 1. resource-id (BEST for Android — most stable) ────────────────────────
// The android:id attribute in the app's layout XML
driver.findElement(AppiumBy.id("com.example.myapp:id/login_button"));
// Package prefix is added automatically when using AppiumBy.id() if configured
driver.findElement(By.id("com.example.myapp:id/email_input"));

// ── 2. accessibility-id (CROSS-PLATFORM — works on Android and iOS) ────────
// Maps to contentDescription attribute in Android XML
driver.findElement(AppiumBy.accessibilityId("Submit Button"));
driver.findElement(AppiumBy.accessibilityId("email_text_field"));

// ── 3. AndroidUIAutomator (MOST POWERFUL for Android) ──────────────────────
// Uses UiSelector DSL — runs natively on device, very fast, supports scrolling
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().text(\"Login\")"
));
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().resourceId(\"com.example.myapp:id/username\")"
));
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().description(\"Submit Button\")"
));
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().className(\"android.widget.EditText\").instance(0)"
    // instance(0) = first, instance(1) = second, etc.
));
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().textContains(\"Login\")"  // partial text match
));
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().textMatches(\"Log.*n\")"  // regex match
));

// Scroll and find in one command (extremely powerful for lists)
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true))" +
    ".scrollIntoView(new UiSelector().text(\"Settings\"))"
));

// ── 4. XPath (LAST RESORT — fragile and slow) ──────────────────────────────
// Queries the serialized XML view hierarchy — slow on complex screens
driver.findElement(By.xpath("//android.widget.Button[@text='Login']"));
driver.findElement(By.xpath("//android.widget.EditText[@resource-id='com.example.myapp:id/email']"));
driver.findElement(By.xpath("//*[contains(@text,'Welcome')]"));
driver.findElement(By.xpath("//android.widget.ListView/android.widget.TextView[2]")); // 2nd item
driver.findElement(By.xpath("//android.widget.LinearLayout[@content-desc='Submit Button']"));

// ── 5. className ───────────────────────────────────────────────────────────
// Find all elements of a given Android widget class
driver.findElements(By.className("android.widget.Button"));
driver.findElements(By.className("android.widget.EditText"));
driver.findElement(By.className("android.widget.CheckBox"));

// ── 6. Image locator (visual matching) ────────────────────────────────────
// Requires Appium Images plugin and a base64-encoded reference image
// appium plugin install images  (install the plugin first)
// driver.findElement(AppiumBy.image(base64ReferenceImageString));
```

---

**Q17: What are all the locator strategies available for iOS in Appium? Show code for each.**

**A:**

```java
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import io.appium.java_client.ios.IOSDriver;

// ── 1. accessibility-id (BEST for iOS) ─────────────────────────────────────
// Maps to the accessibility label set in Xcode (UILabel.accessibilityLabel)
// Same locator works on Android (contentDescription) — cross-platform!
driver.findElement(AppiumBy.accessibilityId("loginButton"));
driver.findElement(AppiumBy.accessibilityId("Email Text Field"));
driver.findElement(AppiumBy.accessibilityId("Submit"));

// ── 2. iOS Predicate String (FAST — native NSPredicate queries) ─────────────
// Uses Apple's NSPredicate syntax — runs natively, much faster than XPath
driver.findElement(AppiumBy.iOSNsPredicateString(
    "type == 'XCUIElementTypeButton' AND name == 'Login'"
));
driver.findElement(AppiumBy.iOSNsPredicateString(
    "label CONTAINS 'Submit'"
));
driver.findElement(AppiumBy.iOSNsPredicateString(
    "value BEGINSWITH 'Enter your'"
));
driver.findElement(AppiumBy.iOSNsPredicateString(
    "type == 'XCUIElementTypeTextField' AND enabled == true"
));
driver.findElement(AppiumBy.iOSNsPredicateString(
    "name == 'username' OR name == 'email'"
));

// ── 3. iOS Class Chain (FAST — hierarchical path, like a native XPath) ──────
// Faster than XPath because it uses native XCUITest queries
driver.findElement(AppiumBy.iOSClassChain(
    "**/XCUIElementTypeButton[`name == 'Login'`]"
));
driver.findElement(AppiumBy.iOSClassChain(
    "**/XCUIElementTypeTable/XCUIElementTypeCell[2]"
    // Selects the 2nd cell in a table
));
driver.findElement(AppiumBy.iOSClassChain(
    "**/XCUIElementTypeNavigationBar/XCUIElementTypeButton[-1]"
    // -1 means last element
));

// ── 4. XPath (LAST RESORT — slow but flexible) ─────────────────────────────
driver.findElement(By.xpath("//XCUIElementTypeButton[@name='Login']"));
driver.findElement(By.xpath("//XCUIElementTypeTextField[@value='Email']"));
driver.findElement(By.xpath("//XCUIElementTypeStaticText[contains(@value,'Welcome')]"));
driver.findElement(By.xpath(
    "//XCUIElementTypeNavigationBar[@name='Settings']/XCUIElementTypeButton"
));

// ── 5. className ───────────────────────────────────────────────────────────
driver.findElements(By.className("XCUIElementTypeButton"));
driver.findElements(By.className("XCUIElementTypeTextField"));
driver.findElement(By.className("XCUIElementTypeSwitch"));
```

---

**Q18: Which locator works on BOTH Android and iOS? Why is it recommended?**

**A:** `accessibilityId` is the only locator that works on both platforms. On Android it maps to the `contentDescription` attribute of a View. On iOS it maps to the `accessibilityLabel` property of a UIView element.

```java
// This single line works on both Android and iOS:
driver.findElement(AppiumBy.accessibilityId("loginButton"));

// On Android: finds element where android:contentDescription="loginButton"
// On iOS:     finds element where accessibilityLabel="loginButton"
```

For cross-platform test code to work with `accessibilityId`, both the Android developer and iOS developer must set the accessibility attribute with the same string value.

```java
// Cross-platform base test using accessibilityId:
public class LoginTest {

    @Test
    public void testLoginButtonIsVisible() {
        // Works on both Android and iOS driver
        WebElement loginBtn = driver.findElement(AppiumBy.accessibilityId("loginButton"));
        Assert.assertTrue(loginBtn.isDisplayed(), "Login button should be visible");
    }

    @Test
    public void testEmailFieldAcceptsInput() {
        WebElement emailField = driver.findElement(AppiumBy.accessibilityId("emailInput"));
        emailField.sendKeys("test@example.com");
        // On Android: getAttribute("text") or getText()
        // On iOS:     getAttribute("value")
    }
}
```

Real-world context: Good accessibility practices (adding content descriptions on Android, accessibility labels on iOS) both improve screen reader support for real users AND make automated testing more stable. Advocating for accessibility in your app is a way to simultaneously improve quality and testability.

---

**Q19: How do you use Appium Inspector to find element locators?**

**A:** Appium Inspector is a standalone GUI tool that connects to a running Appium session and lets you visually inspect the app's element hierarchy to find locators.

Setup and usage:

```bash
# Step 1: Download Appium Inspector
# https://github.com/appium/appium-inspector/releases
# Available for macOS, Windows, Linux

# Step 2: Start Appium server
appium

# Step 3: Start Appium Inspector and configure connection
# Remote Host: 127.0.0.1
# Remote Port: 4723
# Remote Path: /

# Step 4: Enter Desired Capabilities (JSON format)
# {
#   "platformName": "Android",
#   "appium:automationName": "UiAutomator2",
#   "appium:deviceName": "emulator-5554",
#   "appium:appPackage": "com.example.myapp",
#   "appium:appActivity": ".MainActivity"
# }

# Step 5: Click "Start Session"
# The app launches on the device/emulator and a screenshot appears in Inspector

# Step 6: Click on any element in the screenshot
# Inspector shows all attributes: resource-id, contentDescription, text, class, bounds

# Step 7: Use the "Selected Element" panel to copy locators
# - id: com.example.myapp:id/login_button
# - accessibility id: Login Button
# - xpath: //android.widget.Button[@resource-id='...']
```

What Appium Inspector shows for each element:
- `resource-id` — use with `AppiumBy.id()`
- `content-desc` — use with `AppiumBy.accessibilityId()`
- `text` — use with `AndroidUIAutomator: new UiSelector().text("...")`
- `class` — use with `By.className()`
- `bounds` — pixel coordinates for direct coordinate taps

Common mistake: Relying only on XPath generated by Inspector. Inspector-generated XPaths are often absolute paths (`/hierarchy/android.widget.FrameLayout/...`) which break when any parent element changes. Always prefer `resource-id` or `accessibilityId` found in Inspector over the generated XPath.

---

**Q20: When would you choose `AndroidUIAutomator` over `XPath` for Android? What can UIAutomator do that XPath cannot?**

**A:** `AndroidUIAutomator` should be the first choice for most Android element lookups. XPath should be the last resort.

Key advantages of AndroidUIAutomator over XPath:

1. **Speed**: UIAutomator queries the native accessibility tree directly on the device without serializing it to XML. XPath requires Appium to dump the entire view hierarchy to an XML string, then parse it — this is 3-5x slower on complex screens.

2. **Scrolling built in**: UIAutomator can scroll a list and find an element in one atomic operation. XPath cannot scroll.

3. **Richer matching**: UIAutomator supports `textContains`, `textStartsWith`, `textMatches` (regex), `className().instance()`, `fromParent()`, `childSelector()`.

```java
// Scenario: Find element in a long scrollable list
// XPath approach — FAILS if element is not currently visible on screen:
driver.findElement(By.xpath("//android.widget.TextView[@text='Item 50']")); // NoSuchElementException

// AndroidUIAutomator approach — scrolls automatically to find it:
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true))" +
    ".scrollIntoView(new UiSelector().text(\"Item 50\"))"
)); // Works even if Item 50 is far down the list

// Scenario: Find the 3rd EditText on screen
// XPath:
driver.findElement(By.xpath("(//android.widget.EditText)[3]"));
// AndroidUIAutomator (more readable):
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().className(\"android.widget.EditText\").instance(2)" // 0-indexed
));

// Scenario: Find a child element within a parent container
// AndroidUIAutomator with fromParent:
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().resourceId(\"com.example.myapp:id/product_card\").instance(0)" +
    ".fromParent(new UiSelector().resourceId(\"com.example.myapp:id/add_to_cart_button\"))"
));
```

When XPath is acceptable: when you need to express complex hierarchical relationships that UISelector cannot express, or when targeting iOS and Android with a shared locator strategy (XPath works on both, though iOS predicate is better for iOS).

---

## SECTION 5: ACTIONS AND GESTURES

---

**Q21: How do you tap an element, get its text, and check if it is visible?**

**A:**

```java
import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

// ── Finding elements ──────────────────────────────────────────────────────────
WebElement loginBtn = driver.findElement(AppiumBy.id("com.example.myapp:id/login_button"));
WebElement emailField = driver.findElement(AppiumBy.accessibilityId("emailInput"));

// ── Tap (click) ───────────────────────────────────────────────────────────────
loginBtn.click();

// ── Get text ──────────────────────────────────────────────────────────────────
String buttonText = loginBtn.getText();
System.out.println("Button text: " + buttonText);

// On iOS, text may be in the 'label' or 'value' attribute instead of getText()
String iosLabel = loginBtn.getAttribute("label");  // iOS
String iosValue = emailField.getAttribute("value"); // iOS text field content

// ── Check visibility ──────────────────────────────────────────────────────────
boolean isDisplayed = loginBtn.isDisplayed();
boolean isEnabled = loginBtn.isEnabled();
boolean isSelected = driver.findElement(AppiumBy.id("com.example.myapp:id/checkbox"))
    .isSelected();

// ── Get attributes ────────────────────────────────────────────────────────────
String hint = emailField.getAttribute("hint");          // Android placeholder
String checked = driver.findElement(
    AppiumBy.id("com.example.myapp:id/agree_checkbox")
).getAttribute("checked");  // "true" or "false" as String

// ── Explicit wait before tap ──────────────────────────────────────────────────
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
WebElement dynamicButton = wait.until(
    ExpectedConditions.elementToBeClickable(
        AppiumBy.id("com.example.myapp:id/submit_btn")
    )
);
dynamicButton.click();
```

---

**Q22: How do you swipe on screen using the W3C Actions API in Appium 2.x? Why is the old TouchAction API deprecated?**

**A:** The old `TouchAction` API was Appium-proprietary and not part of any standard. The W3C WebDriver specification defines an Actions API for pointer inputs, and Appium 2.x (with Selenium 4 Java client) supports this standard directly.

```java
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import java.time.Duration;
import java.util.Collections;
import java.util.Arrays;

// ── Generic swipe helper method ────────────────────────────────────────────────
private void swipe(AndroidDriver driver, int startX, int startY, int endX, int endY, int durationMs) {
    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence swipe = new Sequence(finger, 0);

    swipe.addAction(finger.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), startX, startY));
    swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    swipe.addAction(finger.createPointerMove(Duration.ofMillis(durationMs),
        PointerInput.Origin.viewport(), endX, endY));
    swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    driver.perform(Collections.singletonList(swipe));
}

// ── Swipe up (reveals content below — scroll down) ────────────────────────────
// Screen dimensions (get dynamically):
Dimension size = driver.manage().window().getSize();
int centerX = size.getWidth() / 2;
int startY = (int)(size.getHeight() * 0.75);  // 75% down from top
int endY   = (int)(size.getHeight() * 0.25);  // 25% from top

swipe(driver, centerX, startY, centerX, endY, 800);

// ── Swipe down (reveals content above — scroll up / pull-to-refresh) ─────────
swipe(driver, centerX, endY, centerX, startY, 800);

// ── Swipe left (next page in carousel / horizontal list) ──────────────────────
int startX = (int)(size.getWidth() * 0.85);
int endX   = (int)(size.getWidth() * 0.15);
swipe(driver, startX, centerX, endX, centerX, 600);

// ── Swipe right (previous page / open side drawer) ────────────────────────────
swipe(driver, endX, centerX, startX, centerX, 600);
```

Common mistake: Using hard-coded pixel coordinates. Screen resolutions differ between devices. Always calculate coordinates as a percentage of `driver.manage().window().getSize()`.

---

**Q23: How do you perform a long press on an element?**

**A:**

```java
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Point;
import org.openqa.selenium.Dimension;
import java.time.Duration;
import java.util.Collections;

private void longPress(AndroidDriver driver, WebElement element) {
    // Find center of element
    Point location = element.getLocation();
    Dimension size = element.getSize();
    int centerX = location.getX() + size.getWidth() / 2;
    int centerY = location.getY() + size.getHeight() / 2;

    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence longPress = new Sequence(finger, 0);

    // Move to element center
    longPress.addAction(finger.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX, centerY));
    // Press down
    longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    // Hold for 2 seconds (without moving)
    longPress.addAction(finger.createPointerMove(Duration.ofMillis(2000),
        PointerInput.Origin.viewport(), centerX, centerY));
    // Release
    longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    driver.perform(Collections.singletonList(longPress));
}

// Usage:
WebElement productItem = driver.findElement(
    AppiumBy.androidUIAutomator("new UiSelector().text(\"Product Name\")")
);
longPress(driver, productItem);
// Context menu or selection mode should appear
```

---

**Q24: How do you scroll to an element that is not currently visible on screen?**

**A:** There are three approaches — choose based on platform and needs.

```java
// ── Method 1: AndroidUIAutomator (BEST for Android lists) ────────────────────
// Finds the scrollable container and scrolls until the target text is visible
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true))" +
    ".scrollIntoView(new UiSelector().text(\"Settings\"))"
));
// If there are multiple scrollable views, specify the one to scroll:
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
    ".scrollIntoView(new UiSelector().resourceId(\"com.example.myapp:id/target_item\"))"
));

// ── Method 2: W3C swipe in a loop (platform-agnostic) ─────────────────────────
// Keep swiping until the element appears (max N attempts)
int maxSwipes = 10;
for (int i = 0; i < maxSwipes; i++) {
    try {
        WebElement target = driver.findElement(
            AppiumBy.accessibilityId("Target Element")
        );
        if (target.isDisplayed()) {
            target.click();
            break;
        }
    } catch (Exception e) {
        // Element not found — swipe up and try again
        Dimension size = driver.manage().window().getSize();
        swipe(driver, size.getWidth()/2, (int)(size.getHeight()*0.75),
              size.getWidth()/2, (int)(size.getHeight()*0.25), 600);
    }
}

// ── Method 3: iOS mobile scroll command ──────────────────────────────────────
// iOS-specific — uses Appium's mobile: scroll command
Map<String, Object> params = new HashMap<>();
params.put("direction", "down");
params.put("predicateString", "label == 'Settings'");
params.put("toVisible", true);
driver.executeScript("mobile: scroll", params);
```

---

**Q25: How do you perform a pinch and zoom gesture?**

**A:** Pinch and zoom require multi-touch — two simultaneous finger actions performed using two `PointerInput` sequences.

```java
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import java.time.Duration;
import java.util.Arrays;

// ── Zoom in (spread — two fingers moving apart) ───────────────────────────────
private void zoomIn(AndroidDriver driver, int centerX, int centerY) {
    PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
    PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");

    Sequence sequence1 = new Sequence(finger1, 0);
    Sequence sequence2 = new Sequence(finger2, 0);

    // Finger 1: starts just above center, moves up-left
    sequence1.addAction(finger1.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX - 10, centerY - 10));
    sequence1.addAction(finger1.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    sequence1.addAction(finger1.createPointerMove(Duration.ofMillis(800),
        PointerInput.Origin.viewport(), centerX - 200, centerY - 200));
    sequence1.addAction(finger1.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    // Finger 2: starts just below center, moves down-right
    sequence2.addAction(finger2.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX + 10, centerY + 10));
    sequence2.addAction(finger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    sequence2.addAction(finger2.createPointerMove(Duration.ofMillis(800),
        PointerInput.Origin.viewport(), centerX + 200, centerY + 200));
    sequence2.addAction(finger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    driver.perform(Arrays.asList(sequence1, sequence2));
}

// ── Pinch in (two fingers moving toward center) ───────────────────────────────
private void pinchIn(AndroidDriver driver, int centerX, int centerY) {
    PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
    PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");

    Sequence sequence1 = new Sequence(finger1, 0);
    Sequence sequence2 = new Sequence(finger2, 0);

    // Finger 1: starts far out, moves to near center
    sequence1.addAction(finger1.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX - 200, centerY - 200));
    sequence1.addAction(finger1.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    sequence1.addAction(finger1.createPointerMove(Duration.ofMillis(800),
        PointerInput.Origin.viewport(), centerX - 10, centerY - 10));
    sequence1.addAction(finger1.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    // Finger 2: starts far out on other side, moves to near center
    sequence2.addAction(finger2.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX + 200, centerY + 200));
    sequence2.addAction(finger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    sequence2.addAction(finger2.createPointerMove(Duration.ofMillis(800),
        PointerInput.Origin.viewport(), centerX + 10, centerY + 10));
    sequence2.addAction(finger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    driver.perform(Arrays.asList(sequence1, sequence2));
}

// Usage — zoom into the center of a map:
Dimension size = driver.manage().window().getSize();
zoomIn(driver, size.getWidth() / 2, size.getHeight() / 2);
```

---

**Q26: How do you type text, clear a field, and handle the soft keyboard?**

**A:**

```java
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;

WebElement emailField = driver.findElement(AppiumBy.id("com.example.myapp:id/email"));

// ── Type text ─────────────────────────────────────────────────────────────────
emailField.clear();                          // Clear existing content first
emailField.sendKeys("user@example.com");     // Type text (triggers keyboard)

// ── Clear field ───────────────────────────────────────────────────────────────
emailField.clear();
// Alternative for stubborn fields:
emailField.sendKeys("");

// ── Hide soft keyboard ────────────────────────────────────────────────────────
driver.hideKeyboard();  // Works on both Android and iOS

// ── Check if keyboard is displayed (Android) ──────────────────────────────────
boolean isKeyboardShown = ((AndroidDriver) driver).isKeyboardShown();
if (isKeyboardShown) {
    driver.hideKeyboard();
}

// ── Press keyboard action key (Done, Search, Next) ────────────────────────────
emailField.sendKeys(org.openqa.selenium.Keys.ENTER);  // Press Enter/Done

// ── Android-specific key presses ─────────────────────────────────────────────
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.HOME));
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.APP_SWITCH));  // Recent apps

// ── Type Unicode / special characters ────────────────────────────────────────
// Requires unicodeKeyboard: true and resetKeyboard: true in capabilities
emailField.sendKeys("用户@例子.中国");   // Chinese characters
emailField.sendKeys("Привет мир");     // Cyrillic

// ── Tab to next field ─────────────────────────────────────────────────────────
emailField.sendKeys(org.openqa.selenium.Keys.TAB);

// ── iOS: dismiss keyboard by tapping Done ─────────────────────────────────────
// iOS keyboard Done button location varies — use the toolbar Done button
driver.findElement(AppiumBy.iOSNsPredicateString(
    "type == 'XCUIElementTypeButton' AND name == 'Done'"
)).click();
```

---

**Q27: How do you take a screenshot in an Appium test?**

**A:**

```java
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ── Take screenshot and save to file ─────────────────────────────────────────
public void takeScreenshot(AndroidDriver driver, String testName) throws IOException {
    // Cast driver to TakesScreenshot (all Appium drivers implement this)
    File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

    // Create timestamped filename
    String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String filename = "screenshots/" + testName + "_" + timestamp + ".png";

    // Copy to desired location
    Path destination = Paths.get(filename);
    Files.createDirectories(destination.getParent());
    Files.copy(screenshotFile.toPath(), destination);

    System.out.println("Screenshot saved: " + filename);
}

// ── Get screenshot as byte array (for embedding in Cucumber report) ───────────
byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
// scenario.attach(screenshotBytes, "image/png", "Screenshot");

// ── Get screenshot as Base64 string ──────────────────────────────────────────
String base64Screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

// ── Screenshot in TestNG @AfterMethod ─────────────────────────────────────────
import org.testng.ITestResult;

@AfterMethod
public void captureScreenshotOnFailure(ITestResult result) throws IOException {
    if (result.getStatus() == ITestResult.FAILURE) {
        takeScreenshot(driver, result.getName());
    }
}

// ── ADB screenshot (without Appium) ──────────────────────────────────────────
// Run in terminal:
// adb exec-out screencap -p > screenshot.png
```

---

**Q28: How do you handle mobile alerts and system popups in Appium?**

**A:**

```java
// ── Android permission dialogs ─────────────────────────────────────────────────

// Option 1: Auto-grant via capability (recommended for CI)
options.setAutoGrantPermissions(true);
// Grants all permissions declared in AndroidManifest before test starts

// Option 2: Handle dialog in test (for testing the permission flow itself)
// Permission dialog appears — find and click "Allow"
try {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    WebElement allowButton = wait.until(ExpectedConditions.elementToBeClickable(
        By.id("com.android.permissioncontroller:id/permission_allow_button")
    ));
    allowButton.click();
} catch (TimeoutException e) {
    // No permission dialog appeared — that's fine, continue
}

// Alternative: use accessibilityId for permission buttons (works across Android versions)
try {
    driver.findElement(AppiumBy.accessibilityId("Allow")).click();
} catch (NoSuchElementException e) {
    // Dialog not present
}

// Option 3: ADB grant before test
// adb shell pm grant com.example.myapp android.permission.CAMERA
// adb shell pm grant com.example.myapp android.permission.ACCESS_FINE_LOCATION

// ── iOS alerts ────────────────────────────────────────────────────────────────

// Option 1: Auto-accept via capability
options.setAutoAcceptAlerts(true);

// Option 2: Handle alert explicitly
try {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    // Accept native iOS alert
    WebElement alertAcceptBtn = wait.until(ExpectedConditions.elementToBeClickable(
        AppiumBy.iOSNsPredicateString("type == 'XCUIElementTypeButton' AND label == 'Allow'")
    ));
    alertAcceptBtn.click();
} catch (TimeoutException e) {
    // No alert appeared
}

// ── Standard WebDriver alert API (works for JavaScript alerts in WebView) ──────
import org.openqa.selenium.Alert;

try {
    Alert alert = driver.switchTo().alert();
    String alertText = alert.getText();
    System.out.println("Alert says: " + alertText);
    alert.accept();     // Click OK
    // alert.dismiss(); // Click Cancel
} catch (Exception e) {
    // No alert present
}
```

---

**Q29: How do you test deep links in a mobile app?**

**A:** Deep links are URLs that open a specific screen inside a mobile app (e.g., `myapp://products/123` opens the product detail screen for product 123).

```java
// ── Android deep link ─────────────────────────────────────────────────────────

// Method 1: Using ADB (works for any URL scheme)
driver.executeScript("mobile: deepLink", Map.of(
    "url", "myapp://products/123",
    "package", "com.example.myapp"
));

// Method 2: ADB shell command via Java
String deepLinkUrl = "myapp://products/123";
String adbCommand = String.format(
    "am start -W -a android.intent.action.VIEW -d %s %s",
    deepLinkUrl, "com.example.myapp"
);
// This runs via Appium's executeShellCommand or directly via Runtime

// Method 3: Appium mobile: command
Map<String, Object> params = new HashMap<>();
params.put("url", "myapp://products/123");
params.put("package", "com.example.myapp");
driver.executeScript("mobile: deepLink", params);

// ── iOS deep link ─────────────────────────────────────────────────────────────

// Method 1: Using mobile: launchApp with deeplink URL
((IOSDriver) driver).get("myapp://products/123");
// IOSDriver.get() interprets custom URL schemes as deep links

// Method 2: Using Safari to trigger the deep link
driver.get("https://example.com/redirect-to-app");
// The redirect page contains a link with the app's URL scheme

// Method 3: Execute script
driver.executeScript("mobile: deepLink", Map.of(
    "url", "myapp://products/123",
    "bundleId", "com.example.MyApp"
));

// ── Verify deep link result ───────────────────────────────────────────────────
// After the deep link is executed, the app should open to the target screen
WebElement productTitle = driver.findElement(
    AppiumBy.id("com.example.myapp:id/product_title")
);
Assert.assertEquals(productTitle.getText(), "Product 123 Name",
    "Deep link should open product 123 detail screen");
```

---

## SECTION 6: REAL DEVICE VS EMULATOR

---

**Q30: What are the key differences between testing on a real device versus an emulator/simulator?**

**A:**

| Aspect | Emulator / Simulator | Real Device |
|---|---|---|
| Cost | Free | Requires physical hardware |
| Setup | Easier — managed via Android Studio / Xcode | Requires USB cable, driver installation, trust settings |
| Speed | Slower to boot, faster for simple gestures | Physical hardware responds at true speed |
| Sensors | Simulated GPS, camera, accelerometer | Real sensors — test actual camera, Bluetooth, NFC |
| Network | Shares host machine network | Real cellular/WiFi — test offline mode, slow networks |
| Notifications | Limited | Real push notifications arrive |
| App stores | Cannot test Play Store / App Store updates | Full store integration testing |
| Parallel execution | Multiple emulators on one machine | Limited by how many devices you own |
| Crash consistency | May differ from real device crashes | Catches real-device-only crashes |
| CI integration | Easy (launch programmatically) | Requires device farm (BrowserStack, SauceLabs) or USB hubs |

Real-world recommendation: Run smoke tests on emulators in CI (fast, free, parallel), run full regression on real devices before release (catches device-specific issues).

---

**Q31: How do you set up Appium for an Android real device? What must be done on the device?**

**A:**

```java
// Java capabilities for Android real device
UiAutomator2Options options = new UiAutomator2Options();
options.setPlatformName("Android");
options.setAutomationName("UiAutomator2");
options.setDeviceName("My Pixel 6");          // Any descriptive name
options.setUdid("RF8M31ABCDE");               // Serial from 'adb devices'
options.setPlatformVersion("13");
options.setApp("/path/to/app-release.apk");   // Or use appPackage+appActivity
options.setAutoGrantPermissions(true);
options.setNoReset(true);

AndroidDriver driver = new AndroidDriver(
    new URL("http://127.0.0.1:4723"),
    options
);
```

Steps required on the Android device:

```
1. Enable Developer Options:
   Settings → About Phone → tap "Build Number" 7 times
   Message appears: "You are now a developer!"

2. Enable USB Debugging:
   Settings → Developer Options → USB Debugging → ON

3. (For some devices) Enable USB Debugging (Security Settings):
   Settings → Developer Options → USB Debugging (Security Settings) → ON

4. Connect device via USB → trust the computer when prompted
   (popup: "Allow USB Debugging? Trust this computer?" → tap "Allow")

5. Verify connection:
   adb devices
   # Expected: RF8M31ABCDE   device
   # (not "unauthorized" — if unauthorized, revoke USB debugging authorizations and reconnect)
```

```bash
# Useful ADB commands for real device troubleshooting
adb devices                                          # List all connected devices
adb -s RF8M31ABCDE shell getprop ro.build.version.release  # Get Android version
adb -s RF8M31ABCDE install -r /path/to/app.apk     # Install/reinstall APK
adb -s RF8M31ABCDE shell pm clear com.example.myapp  # Clear app data
adb -s RF8M31ABCDE shell pm grant com.example.myapp android.permission.CAMERA

# WiFi ADB (remove USB cable after connecting)
adb tcpip 5555
adb connect 192.168.1.105:5555
# Then unplug USB — WiFi debugging works until restart
```

---

**Q32: How do you set up Appium for iOS real device testing? What certificates are needed?**

**A:**

```java
XCUITestOptions options = new XCUITestOptions();
options.setPlatformName("iOS");
options.setAutomationName("XCUITest");
options.setDeviceName("John's iPhone");                   // Any name
options.setUdid("00008030-001A60C42E08802E");             // Device UDID (required)
options.setPlatformVersion("16.4");
options.setBundleId("com.example.MyApp");                  // App must be installed
options.setXcodeOrgId("ABC123DEF456");                     // Apple Developer Team ID
options.setXcodeSigningId("iPhone Developer");             // Signing identity
options.setWdaLaunchTimeout(Duration.ofSeconds(120));      // WDA takes time first run
options.setAutoAcceptAlerts(true);
options.setNoReset(true);

IOSDriver driver = new IOSDriver(
    new URL("http://127.0.0.1:4723"),
    options
);
```

Prerequisites for iOS real device:

```
1. Paid Apple Developer account ($99/year) — required to sign WebDriverAgent

2. Register the device UDID in Apple Developer Portal:
   developer.apple.com → Certificates, Identifiers & Profiles → Devices → Add Device
   Enter the UDID found via: xcrun xctrace list devices

3. Find your Team ID:
   developer.apple.com → Membership → Team ID (format: ABC123DEF456)

4. Trust the Mac on the device:
   Device → Settings → General → VPN & Device Management → Trust "Apple Development: yourname@email.com"

5. If WDA build fails with code 65:
   - Open ~/.appium/node_modules/appium-xcuitest-driver/WebDriverAgent/WebDriverAgent.xcodeproj in Xcode
   - Select WebDriverAgentLib and WebDriverAgentRunner targets
   - Set Team to your Apple Developer team
   - Set a unique Bundle ID suffix (to avoid conflicts with other Appium installations)
   - Build manually first: Product → Build (should succeed)
```

---

## SECTION 7: TROUBLESHOOTING

---

**Q33: What causes `SessionNotCreatedException` and how do you fix it?**

**A:**

```
Error: org.openqa.selenium.SessionNotCreatedException: Could not start a new session
```

Diagnostic checklist:

```bash
# 1. Is Appium server running?
appium --version    # Check it's installed
appium              # Start it — should print "Appium REST http interface listener started"

# 2. Is the UiAutomator2 driver installed?
appium driver list --installed
# If not: appium driver install uiautomator2

# 3. Is the device visible to ADB?
adb devices
# Must show: emulator-5554   device  (or real device serial)
# NOT: "unauthorized" — if so, check USB debugging settings on device

# 4. Is the appPackage/appActivity correct?
adb shell dumpsys window | grep mCurrentFocus
# Open the app manually first, then run this command

# 5. Is the APK installed? (if using appPackage/appActivity)
adb shell pm list packages | grep com.example.myapp
# If missing: adb install /path/to/app.apk

# 6. Check Appium server logs for the exact error
appium --log-level debug
# Run your test and look for the full error in server output
```

Java-side check:

```java
try {
    AndroidDriver driver = new AndroidDriver(
        new URL("http://127.0.0.1:4723"),
        options
    );
} catch (SessionNotCreatedException e) {
    System.out.println("Session failed. Cause: " + e.getMessage());
    // The message usually contains the specific reason from Appium server
    throw e;
}
```

---

**Q34: What causes `NoSuchElementException` in Appium and how do you debug it?**

**A:**

```java
// Error: NoSuchElementException: An element could not be located on the page

// ── Fix 1: Check the locator with Appium Inspector ─────────────────────────────
// Open Appium Inspector, connect to your running session, click the element
// and check all attributes — compare with what your code uses

// ── Fix 2: Add an implicit wait (if not set) ──────────────────────────────────
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// ── Fix 3: Use explicit wait for dynamic elements ─────────────────────────────
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
WebElement element = wait.until(
    ExpectedConditions.visibilityOfElementLocated(
        AppiumBy.id("com.example.myapp:id/loading_complete_indicator")
    )
);

// ── Fix 4: Check if in wrong context (hybrid app) ─────────────────────────────
System.out.println("Current context: " + driver.getContext());
// If "NATIVE_APP" but expecting web element — switch to WEBVIEW
// If "WEBVIEW_xxx" but expecting native element — switch to NATIVE_APP

// ── Fix 5: Print page source to see what is actually on screen ────────────────
System.out.println(driver.getPageSource());
// Search for the element's text or partial id in the XML output

// ── Fix 6: Element may be off-screen — scroll to it ─────────────────────────
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true))" +
    ".scrollIntoView(new UiSelector().text(\"Target Text\"))"
));

// ── Fix 7: Verify locator independently ───────────────────────────────────────
List<WebElement> allButtons = driver.findElements(By.className("android.widget.Button"));
System.out.println("Found " + allButtons.size() + " buttons:");
for (WebElement btn : allButtons) {
    System.out.println("  text=" + btn.getText() + 
                       " id=" + btn.getAttribute("resource-id") +
                       " desc=" + btn.getAttribute("content-desc"));
}
```

---

**Q35: How do you fix the `UiAutomator2 server is not running` error?**

**A:**

```bash
# Error: UiAutomator2 server is not running after 20000ms

# Fix 1: Uninstall the UIAutomator2 APKs and let Appium reinstall them
adb shell pm uninstall io.appium.uiautomator2.server
adb shell pm uninstall io.appium.uiautomator2.server.test
# Then re-run your test — Appium will reinstall automatically

# Fix 2: Check if port 6790 is already in use
adb forward --list
# Remove stale port forwards:
adb forward --remove-all

# Fix 3: Restart ADB server
adb kill-server
adb start-server
adb devices   # Verify device is listed

# Fix 4: Update UIAutomator2 driver
appium driver update uiautomator2

# Fix 5: Check device has enough disk space for APK installation
adb shell df /data

# Fix 6: Verify the device allows installing unknown apps
# Settings → Security → Install Unknown Apps → allow for ADB
```

---

## SECTION 8: INTERVIEW Q&A

---

**Q36: An interviewer asks: "Walk me through what happens architecturally when Appium runs a test. Be specific about each component."**

**A:** The test script (written in Java using the Appium Java client library) calls a method like `driver.findElement(AppiumBy.id("login_btn")).click()`. The Java client translates this into an HTTP POST request using the W3C WebDriver protocol (JSON Wire Protocol successor) and sends it to the Appium Server — a Node.js process listening on port 4723.

The Appium Server receives the request, parses the session ID to identify which driver plugin should handle it, and routes the command to the installed UiAutomator2 plugin (for Android sessions). The UiAutomator2 plugin communicates with the Android device via ADB (Android Debug Bridge). On the device, two APKs form the UIAutomator2 server: they receive the command and use Google's UIAutomator2 framework — which queries Android's Accessibility Service — to find the target view and perform the tap action.

The result travels back: tap success → UIAutomator2 server response → ADB → Appium server → HTTP 200 response → Java client → method returns.

For iOS, the same flow applies but the driver plugin is XCUITest. Instead of ADB, Appium communicates with WebDriverAgent (WDA) — an app it builds with Xcode, signs with your Apple Developer credentials, and installs on the device. WDA runs an HTTP server on port 8100 on the device and bridges Appium commands to Apple's XCUITest framework.

---

**Q37: "What is the difference between `appPackage`+`appActivity` and the `app` capability? When do you use each?"**

**A:** `app` capability provides a file path (or URL) to an APK/IPA file. When you set it, Appium installs that APK onto the device before starting the session. It guarantees a specific build is being tested.

`appPackage` + `appActivity` assume the app is already installed on the device. Appium uses `adb shell am start -n <package>/<activity>` to launch it without reinstalling. This is faster and is used when the app is pre-installed (common in CI where the install step is separate from the test step).

For iOS, `app` installs a `.app` file (simulator) or `.ipa` (real device with code signing). `bundleId` is the iOS equivalent of `appPackage`+`appActivity` — it launches an already-installed app by its bundle identifier.

Use `app` for: fresh build testing, ensuring exact APK version is tested, nightly CI runs.
Use `appPackage`+`appActivity` for: development cycles where you frequently re-run tests without reinstalling, testing in test environment with pre-installed app.

---

**Q38: "How would you make Appium tests cross-platform — run the same test on both Android and iOS?"**

**A:** The key is separating platform-specific configuration from platform-agnostic test logic. This is achieved with three patterns:

```java
// Pattern 1: Driver factory that returns the right driver based on platform
public class DriverFactory {

    public static AppiumDriver createDriver(String platform) throws Exception {
        URL serverUrl = new URL("http://127.0.0.1:4723");

        if (platform.equalsIgnoreCase("android")) {
            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            options.setAutomationName("UiAutomator2");
            options.setDeviceName("emulator-5554");
            options.setAppPackage("com.example.myapp");
            options.setAppActivity(".MainActivity");
            return new AndroidDriver(serverUrl, options);

        } else if (platform.equalsIgnoreCase("ios")) {
            XCUITestOptions options = new XCUITestOptions();
            options.setPlatformName("iOS");
            options.setAutomationName("XCUITest");
            options.setDeviceName("iPhone 14 Pro");
            options.setPlatformVersion("16.4");
            options.setBundleId("com.example.MyApp");
            return new IOSDriver(serverUrl, options);
        }
        throw new IllegalArgumentException("Unknown platform: " + platform);
    }
}

// Pattern 2: Use accessibilityId as primary locator (works on both platforms)
// In test:
WebElement loginButton = driver.findElement(AppiumBy.accessibilityId("loginButton"));
loginButton.click();
// Works on Android (contentDescription="loginButton") and iOS (accessibilityLabel="loginButton")

// Pattern 3: Platform-specific locator helper
public WebElement findElement(AppiumDriver driver) {
    String platform = driver.getCapabilities().getPlatformName().name();
    if (platform.equalsIgnoreCase("android")) {
        return driver.findElement(AppiumBy.id("com.example.myapp:id/login_btn"));
    } else {
        return driver.findElement(AppiumBy.iOSNsPredicateString("name == 'loginButton'"));
    }
}
```

Run on both platforms from TestNG via parameterization:

```xml
<!-- testng.xml -->
<suite name="Cross-Platform Suite">
  <test name="Android Tests">
    <parameter name="platform" value="android"/>
    <classes><class name="com.example.LoginTest"/></classes>
  </test>
  <test name="iOS Tests">
    <parameter name="platform" value="ios"/>
    <classes><class name="com.example.LoginTest"/></classes>
  </test>
</suite>
```

---

**Q39: "Explain `noReset` vs `fullReset`. Which would you use in a regression suite and why?"**

**A:** Default behavior (neither set) clears app data before each session but keeps the app installed. `noReset: true` preserves all app data and state between sessions. `fullReset: true` uninstalls the app at end of session and reinstalls at start — most expensive but cleanest.

For a regression suite: use the default (no explicit reset setting). This gives clean app data for each test without the overhead of APK reinstallation. The trade-off is intentional: tests are isolated from each other's data, and the suite does not spend extra time reinstalling.

Use `noReset: true` only for specific tests where prior state is required (e.g., a test that verifies in-app purchase history, which requires the purchase to have already been made). Use `fullReset: true` for security-related tests or when you suspect the app caches credentials in a way that clean-data reset does not remove.

In practice, a better alternative to `fullReset` is ADB app data clearing before each test: `adb shell pm clear com.example.myapp` — you get the clean state faster without the reinstall cost.

---

**Q40: "You open Appium Inspector and cannot find the element — the resource-id is not shown. How do you proceed?"**

**A:** Work through these approaches in order:

```
1. Check accessibility-id (contentDescription on Android, accessibilityLabel on iOS)
   — The developer may have set the accessibility attribute instead of resource-id
   — Use: AppiumBy.accessibilityId("the label shown in Inspector")

2. Try text-based matching with UIAutomator
   driver.findElement(AppiumBy.androidUIAutomator(
       "new UiSelector().text(\"Button Text\")"
   ));

3. Check class + index
   — Inspector shows the className and its position among siblings
   driver.findElement(AppiumBy.androidUIAutomator(
       "new UiSelector().className(\"android.widget.ImageButton\").instance(2)"
   ));

4. Use XPath with available attributes
   — Inspector shows bounds, text, class, content-desc even when resource-id is missing
   By.xpath("//android.widget.FrameLayout[@content-desc='profile_button']")

5. Use coordinate-based tap (last resort — very brittle)
   — Inspector shows bounds: [left,top][right,bottom]
   — Calculate center from bounds and use W3C Actions API to tap those coordinates

6. Raise with developers
   — Missing resource-id and content-desc indicates missing accessibility implementation
   — Ask developers to add android:id and android:contentDescription attributes
   — This is both a testability issue and an accessibility issue for screen reader users
```

---

**Q41: "How do you run Appium tests in parallel — for example, on two Android devices simultaneously?"**

**A:** Parallel execution requires one Appium server per device (or use Appium's built-in parallel capability), unique device identification via UDID, and a test framework that supports parallel execution (TestNG parallel threads or JUnit5 parallel execution).

```java
// BaseTest.java — thread-local driver storage for parallel safety
public class BaseTest {
    private static final ThreadLocal<AndroidDriver> driverThreadLocal = new ThreadLocal<>();

    public AndroidDriver getDriver() {
        return driverThreadLocal.get();
    }

    @BeforeMethod
    @Parameters({"deviceName", "udid", "port"})
    public void setUp(String deviceName, String udid, String port) throws Exception {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName(deviceName);
        options.setUdid(udid);
        options.setAppPackage("com.example.myapp");
        options.setAppActivity(".MainActivity");
        options.setNoReset(true);

        // Each device gets its own Appium server port
        URL serverUrl = new URL("http://127.0.0.1:" + port);
        AndroidDriver driver = new AndroidDriver(serverUrl, options);
        driverThreadLocal.set(driver);
    }

    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driverThreadLocal.remove();
        }
    }
}
```

```xml
<!-- testng.xml — run on two devices in parallel -->
<suite name="Parallel Mobile Tests" parallel="tests" thread-count="2">

  <test name="Device 1 - Pixel 6">
    <parameter name="deviceName" value="Pixel_6_API_33"/>
    <parameter name="udid" value="emulator-5554"/>
    <parameter name="port" value="4723"/>
    <classes><class name="com.example.LoginTest"/></classes>
  </test>

  <test name="Device 2 - Pixel 5">
    <parameter name="deviceName" value="Pixel_5_API_33"/>
    <parameter name="udid" value="emulator-5556"/>
    <parameter name="port" value="4724"/>
    <classes><class name="com.example.LoginTest"/></classes>
  </test>

</suite>
```

```bash
# Start two Appium servers on different ports
appium --port 4723 &
appium --port 4724 &

# Start two emulators
emulator -avd Pixel_6_API_33 -port 5554 &
emulator -avd Pixel_5_API_33 -port 5556 &

# Run
mvn test -DsuiteXmlFile=testng.xml
```

---

**Q42: "What is Appium Doctor and when do you use it?"**

**A:** `appium-doctor` is a diagnostic CLI tool that checks your machine's environment setup for Appium testing and reports what is correctly configured, what is missing, and what needs to be fixed. It saves hours of debugging environment issues.

```bash
# Install
npm install -g appium-doctor

# Check Android setup
appium-doctor --android
# Checks:
# ✓ Node.js version (18+)
# ✓ ANDROID_HOME environment variable
# ✓ JAVA_HOME environment variable
# ✓ adb on PATH
# ✓ Android SDK Build-Tools
# ✓ Android SDK Platform-Tools
# ✗ MISSING: emulator on PATH → Fix: add $ANDROID_HOME/emulator to PATH

# Check iOS setup (macOS only)
appium-doctor --ios
# Checks:
# ✓ Xcode version (14+)
# ✓ Xcode Command Line Tools
# ✓ Carthage
# ✓ ios-deploy
# ✗ MISSING: libimobiledevice → Fix: brew install libimobiledevice

# Check general setup
appium-doctor --general

# After fixing issues reported by appium-doctor, re-run to confirm all pass
```

Use it when: setting up Appium for the first time on a new machine, diagnosing session creation failures, onboarding new team members, or after OS/SDK updates.

---

**Q43: "How do you handle a scenario where some tests require the user to be logged in and others do not?"**

**A:** The most efficient approach is to pre-establish a session state and use `noReset: true` selectively, or to handle login in a hook based on test tags.

```java
// Approach 1: Login helper called in @BeforeMethod for tests that need it
public class BaseTest {
    protected AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws Exception {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setNoReset(true); // Preserve login state
        // ... other caps
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
    }

    protected void loginAs(String email, String password) {
        driver.findElement(AppiumBy.id("com.example.myapp:id/email")).sendKeys(email);
        driver.findElement(AppiumBy.id("com.example.myapp:id/password")).sendKeys(password);
        driver.findElement(AppiumBy.id("com.example.myapp:id/login_btn")).click();
        // Wait for dashboard
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(
            ExpectedConditions.visibilityOfElementLocated(
                AppiumBy.id("com.example.myapp:id/dashboard_title")
            )
        );
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}

// Tests that need login:
public class CartTest extends BaseTest {
    @BeforeMethod(dependsOnMethods = "setUp")
    public void login() {
        loginAs("user@example.com", "Password123!");
    }

    @Test
    public void testAddToCart() {
        // Already logged in
    }
}

// Tests that do not need login:
public class LoginPageTest extends BaseTest {
    @Test
    public void testLoginPageLayout() {
        // Just opened the app — login page is shown
        Assert.assertTrue(driver.findElement(
            AppiumBy.id("com.example.myapp:id/login_title")).isDisplayed());
    }
}

// Approach 2: Set cookies/tokens via deep link or API before test
// (For apps that support auth token injection)
// Use REST API to get auth token, then deep link into the app with the token
```

---

**Q44: "What common mistakes do beginners make with Appium locators?"**

**A:**

```java
// MISTAKE 1: Using absolute XPath
// Bad — breaks when any ancestor element changes:
By.xpath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/..../android.widget.Button")
// Good — relative XPath with attribute:
By.xpath("//android.widget.Button[@text='Login']")
// Better — use resource-id instead of XPath:
AppiumBy.id("com.example.myapp:id/login_button")

// MISTAKE 2: Missing the package prefix in resource-id
// Bad — often fails because Appium does not auto-prefix in all cases:
AppiumBy.id("login_button")
// Good — always include the full resource-id:
AppiumBy.id("com.example.myapp:id/login_button")

// MISTAKE 3: Using text() XPath on Android incorrectly
// Bad — Android uses @text attribute, not text() function:
By.xpath("//android.widget.TextView[text()='Login']")  // FAILS on Android
// Good:
By.xpath("//android.widget.TextView[@text='Login']")   // Works on Android

// MISTAKE 4: Using Android locators on iOS or vice versa
// Bad — resource-id is Android-only:
AppiumBy.id("com.example.myapp:id/login_button")  // Fails on iOS
// Good — use cross-platform locator:
AppiumBy.accessibilityId("loginButton")           // Works on both

// MISTAKE 5: Not waiting for element to appear
// Bad — element may not be rendered yet:
driver.findElement(AppiumBy.id("...")).click();   // May throw NoSuchElementException
// Good — use explicit wait:
new WebDriverWait(driver, Duration.ofSeconds(15))
    .until(ExpectedConditions.elementToBeClickable(AppiumBy.id("...")))
    .click();

// MISTAKE 6: Using Thread.sleep() instead of explicit waits
// Bad:
Thread.sleep(5000);  // Wastes time if element appears in 500ms, fails if it takes 6000ms
// Good:
new WebDriverWait(driver, Duration.ofSeconds(15))
    .until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.id("...")));

// MISTAKE 7: Not handling the case where element is off-screen
// Bad — NoSuchElementException if element requires scrolling:
driver.findElement(AppiumBy.accessibilityId("Settings")).click();
// Good — scroll to it first:
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true))" +
    ".scrollIntoView(new UiSelector().description(\"Settings\"))"
)).click();
```

---

**Q45: A complete BaseTest + LoginTest example tying all concepts together.**

**A:**

```java
// BaseTest.java
package com.example.base;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class BaseTest {

    protected AndroidDriver driver;
    protected WebDriverWait wait;

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setAutomationName("UiAutomator2");
        options.setDeviceName("emulator-5554");
        options.setAppPackage("com.example.myapp");
        options.setAppActivity("com.example.myapp.LoginActivity");
        options.setNoReset(false);                             // Clean app data each time
        options.setAutoGrantPermissions(true);
        options.setNewCommandTimeout(Duration.ofSeconds(90));

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        // Screenshot on failure
        if (result.getStatus() == ITestResult.FAILURE) {
            try {
                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                Path screenshotPath = Paths.get(
                    "target/screenshots/" + result.getName() + ".png"
                );
                Files.createDirectories(screenshotPath.getParent());
                Files.write(screenshotPath, screenshot);
                System.out.println("Screenshot: " + screenshotPath);
            } catch (Exception e) {
                System.err.println("Screenshot failed: " + e.getMessage());
            }
        }

        if (driver != null) driver.quit();
    }
}

// LoginTest.java
package com.example.tests;

import com.example.base.BaseTest;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    private static final String EMAIL_FIELD    = "com.example.myapp:id/email_input";
    private static final String PASSWORD_FIELD = "com.example.myapp:id/password_input";
    private static final String LOGIN_BTN      = "com.example.myapp:id/login_button";
    private static final String ERROR_MSG      = "com.example.myapp:id/error_message";
    private static final String WELCOME_TEXT   = "com.example.myapp:id/welcome_banner";

    @Test(description = "Valid login navigates to dashboard")
    public void testValidLoginNavigatesToDashboard() {
        driver.findElement(AppiumBy.id(EMAIL_FIELD)).sendKeys("user@example.com");
        driver.findElement(AppiumBy.id(PASSWORD_FIELD)).sendKeys("Password123!");
        driver.findElement(AppiumBy.id(LOGIN_BTN)).click();

        WebElement welcome = wait.until(
            ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(WELCOME_TEXT))
        );
        Assert.assertTrue(welcome.isDisplayed(), "Welcome banner should appear after login");
        Assert.assertEquals(welcome.getText(), "Welcome, John!");
    }

    @Test(description = "Invalid credentials show error")
    public void testInvalidCredentialsShowError() {
        driver.findElement(AppiumBy.id(EMAIL_FIELD)).sendKeys("bad@example.com");
        driver.findElement(AppiumBy.id(PASSWORD_FIELD)).sendKeys("wrongpassword");
        driver.findElement(AppiumBy.id(LOGIN_BTN)).click();

        WebElement errorMsg = wait.until(
            ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(ERROR_MSG))
        );
        Assert.assertEquals(errorMsg.getText(), "Invalid email or password. Please try again.");
    }

    @Test(description = "Scroll list to find item below fold")
    public void testScrollToFindProductInList() {
        // Login first
        driver.findElement(AppiumBy.id(EMAIL_FIELD)).sendKeys("user@example.com");
        driver.findElement(AppiumBy.id(PASSWORD_FIELD)).sendKeys("Password123!");
        driver.findElement(AppiumBy.id(LOGIN_BTN)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(AppiumBy.id(WELCOME_TEXT)));

        // Navigate to products
        driver.findElement(AppiumBy.accessibilityId("Products Tab")).click();

        // Scroll to find "Product 25" in a long list
        WebElement product = driver.findElement(AppiumBy.androidUIAutomator(
            "new UiScrollable(new UiSelector().scrollable(true))" +
            ".scrollIntoView(new UiSelector().text(\"Product 25\"))"
        ));
        Assert.assertTrue(product.isDisplayed(), "Product 25 should be visible after scrolling");
    }
}
```

---

---

**Q: How do you run Appium tests on BrowserStack or Sauce Labs (cloud device farms)?**

**A:** Cloud device farms provide real physical devices in the cloud. You connect to them using RemoteWebDriver with special capabilities — the rest of your test code is identical.

```java
// BrowserStack setup
public class BrowserStackBaseTest {

    protected AndroidDriver driver;

    @BeforeClass
    public void setUp() throws MalformedURLException {
        DesiredCapabilities caps = new DesiredCapabilities();

        // ── BrowserStack auth ────────────────────────────────────────────────
        caps.setCapability("browserstack.user", System.getenv("BS_USERNAME"));   // from env var
        caps.setCapability("browserstack.key",  System.getenv("BS_ACCESS_KEY")); // from env var

        // ── Choose a real device ─────────────────────────────────────────────
        caps.setCapability("device",  "Samsung Galaxy S23");   // real physical device
        caps.setCapability("os_version", "13.0");

        // ── Your app ─────────────────────────────────────────────────────────
        caps.setCapability("app", "bs://your-app-id-after-upload");  // upload app first
        // OR use a URL: caps.setCapability("app", "https://...apk");

        // ── BrowserStack extras ──────────────────────────────────────────────
        caps.setCapability("project", "My API Assessment");
        caps.setCapability("build",   "Build 1.0");
        caps.setCapability("name",    "Login Test");

        // Connect to BrowserStack instead of local Appium server
        URL browserStackUrl = new URL("https://hub-cloud.browserstack.com/wd/hub");
        driver = new AndroidDriver(browserStackUrl, caps);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}

// Your actual tests extend this — NO CHANGES NEEDED
public class LoginTest extends BrowserStackBaseTest {
    @Test
    public void validLogin() {
        driver.findElement(MobileBy.AccessibilityId("username")).sendKeys("admin");
        driver.findElement(MobileBy.AccessibilityId("password")).sendKeys("Admin@123");
        driver.findElement(MobileBy.AccessibilityId("loginBtn")).click();
        // exactly the same as local Appium test
    }
}
```

```java
// Sauce Labs setup — same concept, different URL and capability keys
caps.setCapability("sauce:options", new MutableCapabilities() {{
    put("username",  System.getenv("SAUCE_USERNAME"));
    put("accessKey", System.getenv("SAUCE_ACCESS_KEY"));
}});
URL sauceUrl = new URL("https://ondemand.eu-central-1.saucelabs.com/wd/hub");
driver = new AndroidDriver(sauceUrl, caps);
```

**Key points for interviews:**
- Cloud devices are real physical phones, not emulators
- You upload your `.apk` or `.ipa` once, then reference it by ID
- Store credentials as environment variables — never hardcode
- Test results (video, logs, screenshots) are viewable in the cloud dashboard
- Parallel execution: run on 10 devices simultaneously with the same test code

*End of File 06 — Appium Complete Q&A Guide | 45 Questions*
