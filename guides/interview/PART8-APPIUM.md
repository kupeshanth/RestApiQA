# Part 8 — Appium Mobile Testing | 50 Questions | Full Answers + Code

> CV context: Kupeshanth — Qoria Lanka (GitHub Actions, GCP, Playwright), Cerexio (JIRA, Agile).
> Skills base: Java, Python, JavaScript, Selenium, TestNG.

---

## Q1. What is Appium and how does it differ from Selenium?

**Answer:**
Appium is an open-source, cross-platform mobile automation framework that allows you to test native, hybrid, and mobile web applications on iOS and Android using the same API as Selenium WebDriver.

Key differences:

| Feature | Selenium | Appium |
|---|---|---|
| Target | Web browsers | Mobile apps (native/hybrid/web) |
| Protocol | WebDriver (W3C) | WebDriver + Mobile JSON Wire Protocol |
| No app modification | Not needed | Not needed (no SDK to embed) |
| Language | Any | Any (Java, Python, JS, Ruby) |
| Inspector tool | Browser DevTools | Appium Inspector |

Appium wraps platform-specific frameworks:
- Android: UIAutomator2 (Android 5+), Espresso
- iOS: XCUITest (iOS 9.3+)

---

## Q2. Explain the Appium Architecture end-to-end.

**Answer:**
```
Test Script (Java/Python)
        |
        | HTTP (JSON Wire Protocol / W3C WebDriver)
        v
Appium Server (Node.js process on port 4723)
        |
        |--- Android ---> UIAutomator2 Server (APK on device) ---> ADB ---> Device/Emulator
        |
        |--- iOS -------> WebDriverAgent (XPC) ---> Device/Simulator
```

Step-by-step flow:
1. Test script sends HTTP POST to Appium server (`/session`)
2. Appium parses desired capabilities and starts the appropriate driver
3. For Android: Appium installs `UIAutomator2Server.apk` and `UIAutomator2ServerTest.apk` via ADB
4. Commands are forwarded through ADB to UIAutomator2 which interacts with the app
5. Results are returned back up the chain as HTTP responses

Key components:
- **Appium Server**: Node.js process, can run locally or on cloud
- **ADB (Android Debug Bridge)**: Bridge between PC and Android device
- **UIAutomator2**: Google's framework that actually drives Android UI
- **WebDriverAgent**: Apple's framework used by Appium to drive iOS
- **XCUITest**: Apple's native testing framework under WebDriverAgent

---

## Q3. What are Desired Capabilities? Show Android and iOS examples.

**Answer:**
Desired Capabilities (now called "Appium Options" in Appium 2.x) are a set of key-value pairs sent when creating a session. They tell Appium what platform, device, and app to use.

**Android Example (Java):**
```java
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import java.net.URL;

UiAutomator2Options options = new UiAutomator2Options();
options.setPlatformName("Android");
options.setPlatformVersion("12.0");
options.setDeviceName("Pixel_5_API_31");
options.setUdid("emulator-5554");
options.setAppPackage("com.example.myapp");
options.setAppActivity(".MainActivity");
options.setNoReset(true);
options.setAutoGrantPermissions(true);
options.setNewCommandTimeout(60);

AndroidDriver driver = new AndroidDriver(
    new URL("http://localhost:4723"), options
);
```

**iOS Example (Java):**
```java
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

XCUITestOptions options = new XCUITestOptions();
options.setPlatformName("iOS");
options.setPlatformVersion("16.0");
options.setDeviceName("iPhone 14");
options.setUdid("00008110-001A2B3C4D5E6F78");
options.setApp("/path/to/MyApp.app");
options.setBundleId("com.example.myapp");
options.setNoReset(true);
options.setWdaLocalPort(8100);
options.setAutoAcceptAlerts(true);

IOSDriver driver = new IOSDriver(
    new URL("http://localhost:4723"), options
);
```

**Key capabilities explained:**
- `appPackage`: Android app package name (e.g. `com.google.android.calculator`)
- `appActivity`: The activity to launch (e.g. `.Calculator`)
- `bundleId`: iOS equivalent of appPackage
- `udid`: Unique Device Identifier — required for real devices
- `noReset`: `true` = keep app state between sessions
- `fullReset`: `true` = uninstall and reinstall app
- `autoGrantPermissions`: Auto-accept runtime permissions on Android

---

## Q4. What are the three types of mobile apps and how does Appium handle each?

**Answer:**

| App Type | Description | Appium Handling |
|---|---|---|
| **Native** | Built with platform SDK (Swift/ObjC for iOS, Kotlin/Java for Android) | Direct UIAutomator2 / XCUITest |
| **Hybrid** | Native wrapper with WebView (Cordova, Ionic) | NATIVE_APP + WEBVIEW context switching |
| **Mobile Web** | Regular website in mobile browser (Chrome, Safari) | Chrome/Safari as the "app" |

**Native app setup:**
```java
options.setAppPackage("com.myapp.native");
options.setAppActivity(".HomeActivity");
// No browser involvement — direct element interaction
```

**Hybrid app setup:**
```java
// Start in native context
driver.context("NATIVE_APP");

// Switch to web context for WebView interaction
Set<String> contexts = driver.getContextHandles();
for (String context : contexts) {
    if (context.contains("WEBVIEW")) {
        driver.context(context);
        break;
    }
}

// Now use standard WebDriver selectors
WebElement loginBtn = driver.findElement(By.cssSelector(".login-btn"));
loginBtn.click();

// Switch back to native
driver.context("NATIVE_APP");
```

**Mobile web setup (Chrome on Android):**
```java
UiAutomator2Options options = new UiAutomator2Options();
options.setPlatformName("Android");
options.setDeviceName("emulator-5554");
options.setBrowserName("Chrome");
// No appPackage/appActivity needed
// driver.get("https://www.example.com") works like Selenium
```

---

## Q5. List all Appium locators and when to use each.

**Answer:**

### 1. resource-id (Android only)
```java
driver.findElement(AppiumBy.id("com.example.app:id/username_field"));
// or
driver.findElement(By.id("com.example.app:id/username_field"));
```
Use when: Element has unique `android:id` in layout XML. Fastest locator.

### 2. accessibility-id (both platforms)
```java
driver.findElement(AppiumBy.accessibilityId("login_button"));
```
Android: maps to `contentDescription`. iOS: maps to `accessibilityIdentifier`. Cross-platform preferred.

### 3. XPath
```java
// Android
driver.findElement(By.xpath("//android.widget.TextView[@text='Login']"));

// iOS
driver.findElement(By.xpath("//XCUIElementTypeButton[@name='Login']"));

// Any element with text
driver.findElement(By.xpath("//*[@text='Submit']"));
```
Use as last resort — slow, brittle.

### 4. AndroidUIAutomator (Android only)
```java
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().text(\"Login\").className(\"android.widget.Button\")"
));

// Scroll to element
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(" +
    "new UiSelector().text(\"Target Item\"))"
));

// By resource-id pattern
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().resourceIdMatches(\".*button.*\")"
));
```
Very powerful — supports scrolling and complex selectors.

### 5. iOS Predicate String (iOS only)
```java
driver.findElement(AppiumBy.iOSNsPredicateString(
    "type == 'XCUIElementTypeButton' AND name == 'Login'"
));

// Multiple conditions
driver.findElement(AppiumBy.iOSNsPredicateString(
    "label CONTAINS 'Sign' AND enabled == true"
));
```

### 6. iOS Class Chain (iOS only)
```java
driver.findElement(AppiumBy.iOSClassChain(
    "**/XCUIElementTypeButton[`name == 'Login'`]"
));

// Nested
driver.findElement(AppiumBy.iOSClassChain(
    "**/XCUIElementTypeTable/XCUIElementTypeCell[3]/XCUIElementTypeStaticText"
));
```
Faster than XPath on iOS.

### 7. className
```java
// Android
driver.findElements(By.className("android.widget.TextView"));

// iOS
driver.findElements(By.className("XCUIElementTypeButton"));
```

### 8. image (both platforms)
```java
// Uses image comparison — set base image first
driver.findElement(AppiumBy.image(base64EncodedImage));
```

---

## Q6. How do you perform gestures in Appium?

**Answer:**
Appium 2.x uses the W3C Actions API for gestures.

### Tap
```java
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

// Simple tap on element
WebElement element = driver.findElement(AppiumBy.accessibilityId("submit"));
element.click(); // works for simple taps

// Tap by coordinates
PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
Sequence tap = new Sequence(finger, 1);
tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), 200, 400));
tap.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
tap.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
driver.perform(Arrays.asList(tap));
```

### Swipe
```java
public void swipe(AndroidDriver driver, int startX, int startY, int endX, int endY) {
    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence swipe = new Sequence(finger, 1);
    swipe.addAction(finger.createPointerMove(Duration.ZERO, 
        PointerInput.Origin.viewport(), startX, startY));
    swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), 
        PointerInput.Origin.viewport(), endX, endY));
    swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    driver.perform(Arrays.asList(swipe));
}

// Swipe up (scroll down)
swipe(driver, 540, 1500, 540, 500);

// Swipe left (next page)
swipe(driver, 900, 800, 100, 800);
```

### Long Press
```java
public void longPress(AndroidDriver driver, WebElement element) {
    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Point location = element.getLocation();
    Dimension size = element.getSize();
    int centerX = location.getX() + size.getWidth() / 2;
    int centerY = location.getY() + size.getHeight() / 2;

    Sequence longPress = new Sequence(finger, 1);
    longPress.addAction(finger.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX, centerY));
    longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    longPress.addAction(new Pause(finger, Duration.ofSeconds(2)));
    longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    driver.perform(Arrays.asList(longPress));
}
```

### Pinch and Zoom
```java
public void pinchToZoom(AndroidDriver driver, int centerX, int centerY, boolean zoomIn) {
    PointerInput finger1 = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
    PointerInput finger2 = new PointerInput(PointerInput.Kind.TOUCH, "finger2");

    int offset = zoomIn ? 200 : 0;
    int targetOffset = zoomIn ? 0 : 200;

    Sequence seq1 = new Sequence(finger1, 0);
    seq1.addAction(finger1.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX - offset, centerY));
    seq1.addAction(finger1.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    seq1.addAction(finger1.createPointerMove(Duration.ofMillis(600),
        PointerInput.Origin.viewport(), centerX - targetOffset, centerY));
    seq1.addAction(finger1.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    Sequence seq2 = new Sequence(finger2, 0);
    seq2.addAction(finger2.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), centerX + offset, centerY));
    seq2.addAction(finger2.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    seq2.addAction(finger2.createPointerMove(Duration.ofMillis(600),
        PointerInput.Origin.viewport(), centerX + targetOffset, centerY));
    seq2.addAction(finger2.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

    driver.perform(Arrays.asList(seq1, seq2));
}
```

### Scroll to element (Android)
```java
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true))" +
    ".scrollIntoView(new UiSelector().text(\"Target Text\"))"
));
```

---

## Q7. Explain context switching between NATIVE_APP and WEBVIEW.

**Answer:**
Hybrid apps have multiple "contexts" — a native layer and one or more WebView layers.

```java
import io.appium.java_client.android.AndroidDriver;

// Check available contexts
Set<String> contexts = driver.getContextHandles();
System.out.println("Available contexts: " + contexts);
// Output: [NATIVE_APP, WEBVIEW_com.example.app, WEBVIEW_chrome]

// Switch to WebView
driver.context("WEBVIEW_com.example.app");

// Wait for WebView to load (important!)
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".app-header")));

// Use standard Selenium selectors in WebView
WebElement emailField = driver.findElement(By.cssSelector("input[type='email']"));
emailField.sendKeys("test@example.com");

// Switch back to native
driver.context("NATIVE_APP");

// Now interact with native elements
driver.findElement(AppiumBy.id("com.example.app:id/close_btn")).click();
```

**Troubleshooting context switching:**
```java
// Enable WebView debugging (Android) — add to app or via ADB
// adb shell am set-debug-app com.example.app

// For Chrome-based WebView, enable in capabilities:
options.setChromedriverExecutable("/path/to/chromedriver");
// Match chromedriver version to Chrome version in WebView

// Get current context
String currentContext = driver.getContext();
System.out.println("Current: " + currentContext);
```

---

## Q8. Real device vs Emulator vs Simulator — differences for QA.

**Answer:**

| Aspect | Real Device | Android Emulator | iOS Simulator |
|---|---|---|---|
| Performance | Actual device speed | Slower (virtualized) | Faster (native x86) |
| Camera/GPS | Real hardware | Limited simulation | Limited simulation |
| Network | Real carrier/WiFi | Host network | Host network |
| Push notifications | Full support | Limited | Limited |
| Biometrics | Real fingerprint | Not supported | Simulated |
| App install | APK/IPA via ADB/iTunes | APK directly | Only .app not .ipa |
| Cost | High (device farm) | Free | Free (Mac only) |
| Flakiness | Lower | Higher | Medium |
| CI/CD | Complex setup | Easy | Mac agents only |

**Real device setup with UDID:**
```bash
# Find UDID on Android
adb devices
# emulator-5554   device
# 1A2B3C4D5E6F   device  ← real device UDID

# Find UDID on iOS (Mac)
xcrun xctrace list devices
# iPhone 14 (16.0) [00008110-001A2B3C4D5E6F78]
```

```java
// Real Android device
options.setUdid("1A2B3C4D5E6F");

// Real iOS device
options.setUdid("00008110-001A2B3C4D5E6F78");
options.setXcodeCertificate("iPhone Developer: Name");
options.setXcodeOrgId("ABC123DEF");
```

---

## Q9. How do you run Appium tests on BrowserStack?

**Answer:**
```java
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import java.net.URL;

public class BrowserStackTest {

    private static final String USERNAME = System.getenv("BS_USERNAME");
    private static final String ACCESS_KEY = System.getenv("BS_ACCESS_KEY");
    private static final String URL = "https://" + USERNAME + ":" + ACCESS_KEY 
        + "@hub-cloud.browserstack.com/wd/hub";

    public AndroidDriver setupDriver() throws Exception {
        DesiredCapabilities caps = new DesiredCapabilities();
        
        // App capability — upload APK to BrowserStack first
        caps.setCapability("app", "bs://abc123def456");  // BrowserStack app ID
        
        // Device and OS
        caps.setCapability("deviceName", "Samsung Galaxy S22");
        caps.setCapability("platformVersion", "12.0");
        caps.setCapability("platformName", "Android");
        
        // BrowserStack specific
        caps.setCapability("project", "MyApp Android Tests");
        caps.setCapability("build", "Build #" + System.getenv("BUILD_NUMBER"));
        caps.setCapability("name", "Login Test");
        caps.setCapability("browserstack.networkLogs", true);
        caps.setCapability("browserstack.deviceLogs", true);
        caps.setCapability("browserstack.video", true);
        caps.setCapability("autoGrantPermissions", true);
        
        return new AndroidDriver(new URL(URL), caps);
    }
}
```

**Upload APK to BrowserStack:**
```bash
curl -u "username:accesskey" \
  -X POST "https://api-cloud.browserstack.com/app-automate/upload" \
  -F "file=@/path/to/app.apk"
# Returns: {"app_url":"bs://abc123def456"}
```

---

## Q10. How do you run Appium tests on Sauce Labs?

**Answer:**
```java
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.MutableCapabilities;
import java.net.URL;

public class SauceLabsTest {

    private static final String SAUCE_USERNAME = System.getenv("SAUCE_USERNAME");
    private static final String SAUCE_ACCESS_KEY = System.getenv("SAUCE_ACCESS_KEY");
    private static final String SAUCE_URL = 
        "https://ondemand.eu-central-1.saucelabs.com:443/wd/hub";

    public AndroidDriver setupDriver() throws Exception {
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("platformName", "Android");
        caps.setCapability("appium:app", "storage:filename=myapp.apk");
        caps.setCapability("appium:deviceName", "Google Pixel 6");
        caps.setCapability("appium:platformVersion", "12");
        caps.setCapability("appium:automationName", "UIAutomator2");
        
        MutableCapabilities sauceOptions = new MutableCapabilities();
        sauceOptions.setCapability("username", SAUCE_USERNAME);
        sauceOptions.setCapability("accessKey", SAUCE_ACCESS_KEY);
        sauceOptions.setCapability("build", "RDC-Android-1");
        sauceOptions.setCapability("name", "Sauce Android Test");
        sauceOptions.setCapability("deviceType", "phone");
        
        caps.setCapability("sauce:options", sauceOptions);
        
        return new AndroidDriver(new URL(SAUCE_URL), caps);
    }
}
```

---

## Q11. What is UDID and how do you find it?

**Answer:**
UDID (Unique Device Identifier) uniquely identifies a physical device. Required in Appium capabilities when running on real devices.

**Android:**
```bash
adb devices -l
# List of devices attached
# 1A2B3C4D5E6F7890 device product:sailfish model:Pixel_2

# Get specific info
adb -s 1A2B3C4D5E6F7890 shell getprop ro.serialno
```

**iOS (Mac):**
```bash
# Xcode instruments
xcrun xctrace list devices

# idevice_id tool
idevice_id -l

# Via Finder: Connect device, select it in Finder sidebar
# Click on the device summary line to toggle between serial/UDID
```

**In Java test:**
```java
options.setUdid("1A2B3C4D5E6F7890");  // Android
options.setUdid("00008110-001A2B3C4D5E6F78");  // iOS
```

---

## Q12. Explain appPackage and appActivity.

**Answer:**
- `appPackage`: The Android application package name defined in `AndroidManifest.xml`
- `appActivity`: The specific Android Activity (screen) to launch

**Find them via ADB:**
```bash
# Method 1: While app is running
adb shell dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'
# mCurrentFocus=Window{abc com.example.myapp/com.example.myapp.MainActivity}

# Method 2: Package manager
adb shell pm list packages | grep myapp
# package:com.example.myapp

# Method 3: Get launcher activity
adb shell cmd package resolve-activity --brief com.example.myapp
# com.example.myapp/.SplashActivity
```

**Common examples:**
```java
// Google Calculator
options.setAppPackage("com.google.android.calculator");
options.setAppActivity("com.google.android.calculator.Calculator");

// Gmail
options.setAppPackage("com.google.android.gm");
options.setAppActivity(".ConversationListActivityGmail");

// Your app
options.setAppPackage("com.yourcompany.yourapp");
options.setAppActivity(".ui.MainActivity");
```

---

## Q13. Difference between noReset and fullReset.

**Answer:**

| Capability | Effect |
|---|---|
| `noReset: false` (default) | Clears app data before session, but does NOT uninstall |
| `noReset: true` | Does NOT clear app data — keeps login state, preferences |
| `fullReset: true` | Clears app data AND uninstalls the app after session |
| `fullReset: false` (default) | Does NOT uninstall after session |

**Use cases:**
```java
// Fresh test — start with clean state
options.setNoReset(false);
options.setFullReset(false);
// → Clears data, keeps app installed

// Performance test — avoid reinstalling
options.setNoReset(true);
// → Reuse existing app state (e.g., already logged in)

// Compliance test — truly clean device
options.setFullReset(true);
// → Uninstall + reinstall (slowest)
```

**Warning:** `noReset: true` can cause test pollution between tests if the app retains state from a previous test. Always use fresh state for independent tests.

---

## Q14. What is Appium Inspector and how do you use it?

**Answer:**
Appium Inspector is a GUI tool to inspect mobile UI elements and build locators without writing code.

**Setup:**
1. Download Appium Inspector from GitHub releases
2. Start Appium Server: `appium server --port 4723`
3. In Inspector, enter Remote Host: `localhost`, Port: `4723`
4. Add capabilities JSON:
```json
{
  "platformName": "Android",
  "appium:deviceName": "emulator-5554",
  "appium:appPackage": "com.example.myapp",
  "appium:appActivity": ".MainActivity",
  "appium:automationName": "UIAutomator2"
}
```
5. Click "Start Session"

**What you see:**
- Screenshot of the device on left
- XML source tree on right
- Click any element to see its attributes (resourceId, text, className, contentDesc)
- "Tap", "Send Keys", "Clear" buttons for interactive testing

**Finding locators:**
- Click element in screenshot
- Inspector shows: `resource-id`, `content-desc`, `text`, `xpath`
- Copy the `id` value → use as `By.id()` in code

---

## Q15. Common Appium errors and how to fix them.

**Answer:**

### Error 1: `Unable to find UIAutomator2Server`
```
org.openqa.selenium.SessionNotCreatedException: An unknown server-side error occurred 
while processing the command. Original error: 'UIAutomator2Server.apk' is not compatible
```
**Fix:**
```bash
# Update UIAutomator2 driver
appium driver update uiautomator2

# Or install specific version
appium driver install uiautomator2@2.29.2
```

### Error 2: `An element could not be located`
```java
// Bad — element not loaded yet
WebElement btn = driver.findElement(By.id("submit_btn")); // NoSuchElementException

// Fix — add explicit wait
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
WebElement btn = wait.until(
    ExpectedConditions.elementToBeClickable(By.id("submit_btn"))
);
```

### Error 3: `Could not find a connected Android device`
```bash
# Check ADB
adb devices
# If empty:
adb kill-server
adb start-server

# Check USB debugging enabled on device
# Check USB cable (data cable, not charge-only)
```

### Error 4: `App did not start`
```bash
# Verify appPackage and appActivity
adb shell am start -n com.example.app/.MainActivity
# If error: check Activity name with:
adb shell dumpsys package com.example.app | grep "Activity"
```

### Error 5: `Chromedriver version mismatch` (Hybrid apps)
```bash
# Find Chrome version on device
adb shell dumpsys package com.android.chrome | grep versionName

# Download matching chromedriver from:
# https://chromedriver.chromium.org/downloads

# Set in capabilities
options.setChromedriverExecutable("/path/to/chromedriver");
```

### Error 6: `StaleElementReferenceException`
```java
// Element went stale (app re-rendered)
// Fix: re-find element before interacting
for (int attempt = 0; attempt < 3; attempt++) {
    try {
        driver.findElement(By.id("target")).click();
        break;
    } catch (StaleElementReferenceException e) {
        // retry
    }
}
```

---

## Q16. How do you handle alerts and popups in Appium?

**Answer:**
```java
// System alert (Android permissions)
// Method 1: autoGrantPermissions capability (easiest)
options.setAutoGrantPermissions(true);

// Method 2: Handle manually
try {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    Alert alert = wait.until(ExpectedConditions.alertIsPresent());
    alert.accept(); // or alert.dismiss()
} catch (TimeoutException e) {
    // No alert present
}

// Method 3: Click system permission button
driver.findElement(AppiumBy.id("com.android.permissioncontroller:id/permission_allow_button"))
    .click();

// iOS alerts
// autoAcceptAlerts: true in capabilities
// or
driver.switchTo().alert().accept();

// App-level dialog (not system)
driver.findElement(AppiumBy.accessibilityId("OK")).click();
```

---

## Q17. How do you handle scrolling in Appium?

**Answer:**
```java
// Method 1: AndroidUIAutomator scroll (most reliable on Android)
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true).instance(0))" +
    ".scrollIntoView(new UiSelector().text(\"Target Item\").instance(0))"
));

// Method 2: W3C Actions swipe (cross-platform)
public void scrollDown(AppiumDriver driver) {
    Dimension size = driver.manage().window().getSize();
    int startX = size.getWidth() / 2;
    int startY = (int)(size.getHeight() * 0.8);
    int endY = (int)(size.getHeight() * 0.2);
    
    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    Sequence scroll = new Sequence(finger, 0);
    scroll.addAction(finger.createPointerMove(Duration.ZERO,
        PointerInput.Origin.viewport(), startX, startY));
    scroll.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
    scroll.addAction(finger.createPointerMove(Duration.ofMillis(800),
        PointerInput.Origin.viewport(), startX, endY));
    scroll.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    driver.perform(Arrays.asList(scroll));
}

// Method 3: iOS - mobile:scroll
((JavascriptExecutor) driver).executeScript("mobile: scroll", 
    Map.of("direction", "down"));

// Scroll until element visible
public void scrollUntilVisible(AppiumDriver driver, By locator) {
    int maxScrolls = 10;
    for (int i = 0; i < maxScrolls; i++) {
        try {
            WebElement el = driver.findElement(locator);
            if (el.isDisplayed()) return;
        } catch (NoSuchElementException e) {
            scrollDown(driver);
        }
    }
}
```

---

## Q18. How do you take a screenshot in Appium?

**Answer:**
```java
import org.openqa.selenium.OutputType;
import org.apache.commons.io.FileUtils;
import java.io.File;

// Basic screenshot
File screenshot = driver.getScreenshotAs(OutputType.FILE);
FileUtils.copyFile(screenshot, new File("screenshots/test_screenshot.png"));

// Screenshot on test failure (TestNG listener)
public class ScreenshotListener extends TestListenerAdapter {
    
    @Override
    public void onTestFailure(ITestResult result) {
        Object driver = result.getTestContext()
            .getAttribute("driver");
        
        if (driver instanceof AppiumDriver) {
            AppiumDriver appiumDriver = (AppiumDriver) driver;
            File screenshot = appiumDriver.getScreenshotAs(OutputType.FILE);
            String fileName = "screenshots/" + result.getName() + "_" + 
                System.currentTimeMillis() + ".png";
            try {
                FileUtils.copyFile(screenshot, new File(fileName));
                System.out.println("Screenshot: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
```

---

## Q19. How do you perform parallel mobile testing?

**Answer:**
Use TestNG parallel execution with separate driver instances per thread.

**testng.xml:**
```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Mobile Parallel Suite" parallel="tests" thread-count="2">
    
    <test name="Android Tests">
        <parameter name="platform" value="Android"/>
        <parameter name="deviceName" value="Pixel_6"/>
        <parameter name="udid" value="emulator-5554"/>
        <classes>
            <class name="tests.LoginTest"/>
        </classes>
    </test>
    
    <test name="iOS Tests">
        <parameter name="platform" value="iOS"/>
        <parameter name="deviceName" value="iPhone 14"/>
        <parameter name="udid" value="00008110-001A2B3C4D5E6F78"/>
        <classes>
            <class name="tests.LoginTest"/>
        </classes>
    </test>
    
</suite>
```

**Thread-safe driver manager:**
```java
public class DriverManager {
    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    
    public static AppiumDriver getDriver() {
        return driver.get();
    }
    
    public static void setDriver(AppiumDriver d) {
        driver.set(d);
    }
    
    public static void quitDriver() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}

// Base test
public class BaseTest {
    
    @Parameters({"platform", "deviceName", "udid"})
    @BeforeMethod
    public void setUp(String platform, String deviceName, String udid) throws Exception {
        AppiumDriver d = createDriver(platform, deviceName, udid);
        DriverManager.setDriver(d);
    }
    
    @AfterMethod
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
```

**For BrowserStack parallel:**
```java
// Each test uses a different BrowserStack device
// BrowserStack handles the parallelism automatically
// Just ensure each test creates its own driver instance
```

---

## Q20. What is the Page Object Model for Appium?

**Answer:**
```java
// LoginPage.java
public class LoginPage {
    private AppiumDriver driver;
    private WebDriverWait wait;
    
    // Locators
    private By usernameField = By.id("com.example.app:id/username");
    private By passwordField = By.id("com.example.app:id/password");
    private By loginButton = AppiumBy.accessibilityId("login_btn");
    private By errorMessage = By.id("com.example.app:id/error_text");
    
    public LoginPage(AppiumDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }
    
    public LoginPage enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameField))
            .sendKeys(username);
        return this;
    }
    
    public LoginPage enterPassword(String password) {
        driver.findElement(passwordField).sendKeys(password);
        return this;
    }
    
    public HomePage clickLogin() {
        driver.findElement(loginButton).click();
        return new HomePage(driver);
    }
    
    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage))
            .getText();
    }
}

// Test
public class LoginTest extends BaseTest {
    
    @Test
    public void testSuccessfulLogin() {
        LoginPage loginPage = new LoginPage(DriverManager.getDriver());
        HomePage home = loginPage
            .enterUsername("testuser@example.com")
            .enterPassword("Password123!")
            .clickLogin();
        
        Assert.assertTrue(home.isLoaded(), "Home page should be loaded");
    }
}
```

---

## Q21 - Q30: Additional Core Questions

### Q21. How do you type text and clear a field?
```java
WebElement field = driver.findElement(By.id("com.example:id/input"));
field.clear();
field.sendKeys("Hello World");

// Programmatic keyboard hide
driver.hideKeyboard();

// Verify text
Assert.assertEquals(field.getText(), "Hello World");
```

### Q22. How do you handle device back button?
```java
// Android hardware back button
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.BACK));

// Home button
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.HOME));

// Overview/Recents
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.APP_SWITCH));
```

### Q23. How do you rotate the device?
```java
// Landscape
driver.rotate(ScreenOrientation.LANDSCAPE);

// Portrait
driver.rotate(ScreenOrientation.PORTRAIT);

// Get current
ScreenOrientation current = driver.getOrientation();
```

### Q24. How do you get device info?
```java
// Device time
String time = ((AndroidDriver) driver).getDeviceTime();

// Battery info (Android)
Map<String, Object> battery = ((HasBattery) driver).getBatteryInfo().toMap();
System.out.println("Battery level: " + battery.get("level"));

// Network connection (Android)
ConnectionState state = ((AndroidDriver) driver).getConnection();
```

### Q25. How do you install/uninstall apps during tests?
```java
// Check if installed
boolean installed = driver.isAppInstalled("com.example.app");

// Install
driver.installApp("/path/to/app.apk");

// Uninstall
driver.removeApp("com.example.app");

// Launch
driver.activateApp("com.example.app");

// Put app in background
driver.runAppInBackground(Duration.ofSeconds(5));

// Terminate
driver.terminateApp("com.example.app");
```

### Q26. How do you handle file upload in mobile?
```java
// Push file to device
((AndroidDriver) driver).pushFile(
    "/sdcard/Download/test.pdf", 
    new File("/local/path/test.pdf")
);

// Pull file from device
byte[] data = ((AndroidDriver) driver).pullFile("/sdcard/Download/output.pdf");
FileUtils.writeByteArrayToFile(new File("downloads/output.pdf"), data);
```

### Q27. What is the difference between Appium 1.x and 2.x?
| Feature | Appium 1.x | Appium 2.x |
|---|---|---|
| Drivers | Built-in | Separate plugins |
| Install drivers | N/A | `appium driver install uiautomator2` |
| Capabilities | Any key | Requires `appium:` prefix |
| Plugins | Not supported | Extensible |
| W3C | Partial | Full W3C compliance |

```bash
# Appium 2.x setup
npm install -g appium
appium driver install uiautomator2
appium driver install xcuitest
appium driver list --installed
appium server --port 4723
```

### Q28. How do you use Appium with TestNG?
```java
@Test(groups = {"smoke", "android"})
@Parameters({"platform"})
public void loginTest(String platform) {
    AppiumDriver driver = DriverManager.getDriver();
    new LoginPage(driver)
        .enterUsername("user@test.com")
        .enterPassword("pass123")
        .clickLogin();
    
    Assert.assertTrue(new HomePage(driver).isLoaded());
}

// DataProvider for multiple users
@DataProvider(name = "users")
public Object[][] getUsers() {
    return new Object[][] {
        {"admin@test.com", "admin123"},
        {"user@test.com", "user123"}
    };
}
```

### Q29. How do you integrate Appium with GitHub Actions?
```yaml
name: Mobile Tests
on: [push]

jobs:
  android-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '18'
      
      - name: Install Appium
        run: |
          npm install -g appium
          appium driver install uiautomator2
      
      - name: Enable KVM (Android emulator)
        run: |
          echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' \
            | sudo tee /etc/udev/rules.d/99-kvm4all.rules
          sudo udevadm control --reload-rules
          sudo udevadm trigger --name-match=kvm
      
      - name: Start Android Emulator
        uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: 31
          script: mvn test -Dplatform=Android
      
      # For cloud testing — no emulator needed
      - name: Run on BrowserStack
        env:
          BS_USERNAME: ${{ secrets.BS_USERNAME }}
          BS_ACCESS_KEY: ${{ secrets.BS_ACCESS_KEY }}
        run: mvn test -Denv=browserstack
```

### Q30. How do you debug Appium test failures?
```java
// 1. Enable verbose logging
// Start server: appium server --log-level debug

// 2. Save page source on failure
String pageSource = driver.getPageSource();
FileUtils.writeStringToFile(new File("page_source.xml"), pageSource, "UTF-8");

// 3. Screenshot on failure
File screenshot = driver.getScreenshotAs(OutputType.FILE);
FileUtils.copyFile(screenshot, new File("failure_screenshot.png"));

// 4. Check element attributes
WebElement el = driver.findElement(By.id("my_element"));
System.out.println("Tag: " + el.getTagName());
System.out.println("Text: " + el.getText());
System.out.println("Enabled: " + el.isEnabled());
System.out.println("Displayed: " + el.isDisplayed());

// 5. ADB logcat for app logs
// adb logcat | grep "com.example.app"
```

---

## Q31 - Q40: Locator Strategy and Advanced Questions

### Q31. How do you verify element exists without throwing exception?
```java
public boolean isElementPresent(By locator) {
    try {
        driver.findElement(locator);
        return true;
    } catch (NoSuchElementException e) {
        return false;
    }
}

// With explicit wait
public boolean isElementVisible(By locator, int seconds) {
    try {
        new WebDriverWait(driver, Duration.ofSeconds(seconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
        return true;
    } catch (TimeoutException e) {
        return false;
    }
}
```

### Q32. How do you get all elements in a list?
```java
// Get all items in a RecyclerView
List<WebElement> listItems = driver.findElements(
    By.id("com.example.app:id/list_item_text")
);

System.out.println("Total items: " + listItems.size());

for (WebElement item : listItems) {
    System.out.println("Item: " + item.getText());
}

// Get text of all visible items
List<String> itemTexts = listItems.stream()
    .map(WebElement::getText)
    .collect(Collectors.toList());
```

### Q33. How do you handle custom date pickers?
```java
// Scroll month picker
WebElement monthPicker = driver.findElement(By.id("month_picker"));

// Swipe up on picker to go to next month
PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
Point location = monthPicker.getLocation();
Dimension size = monthPicker.getSize();
int centerX = location.getX() + size.getWidth() / 2;

Sequence swipe = new Sequence(finger, 0);
swipe.addAction(finger.createPointerMove(Duration.ZERO,
    PointerInput.Origin.viewport(), centerX, location.getY() + 100));
swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
swipe.addAction(finger.createPointerMove(Duration.ofMillis(500),
    PointerInput.Origin.viewport(), centerX, location.getY() - 100));
swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
driver.perform(Arrays.asList(swipe));
```

### Q34. How do you handle network conditions in tests?
```java
// BrowserStack network simulation
caps.setCapability("browserstack.networkProfile", "4g-lte-good");
// Options: No network, Airplane Mode, 2G, 3G, 4G LTE

// Android emulator network throttle via ADB
// adb shell tc qdisc add dev eth0 root tbf rate 1Mbit burst 10kbit latency 70ms

// Appium Network Interception (Appium 2.x plugin)
// appium plugin install interceptor
```

### Q35. How do you handle biometric authentication in tests?
```java
// Android fingerprint simulation (emulator only)
// adb -e emu finger touch 1

// iOS Touch ID simulation
((IOSDriver) driver).performTouchID(true); // true = valid fingerprint

// Or via capabilities
options.setCapability("allowTouchIdEnroll", true);

// In test
((IOSDriver) driver).performTouchID(true);
// Verify authenticated state
```

### Q36. What is the AppiumFluentWait?
```java
import org.openqa.selenium.support.ui.FluentWait;

FluentWait<AppiumDriver> wait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(30))
    .pollingEvery(Duration.ofMillis(500))
    .ignoring(NoSuchElementException.class)
    .ignoring(StaleElementReferenceException.class);

WebElement element = wait.until(d -> 
    d.findElement(By.id("com.example:id/success_message"))
);
```

### Q37. How do you handle keyboard in Appium?
```java
// Check if keyboard is shown
boolean keyboardShown = ((AndroidDriver) driver).isKeyboardShown();

// Hide keyboard
if (keyboardShown) {
    driver.hideKeyboard();
}

// Press specific key
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.ENTER));
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.DELETE));
((AndroidDriver) driver).pressKey(new KeyEvent(AndroidKey.TAB));
```

### Q38. What is TestNG @DataProvider usage with Appium?
```java
@DataProvider(name = "loginData", parallel = true)
public Object[][] loginData() {
    return new Object[][] {
        {"user1@test.com", "pass1", true},
        {"user2@test.com", "pass2", true},
        {"invalid@test.com", "wrong", false},
    };
}

@Test(dataProvider = "loginData")
public void testLogin(String email, String password, boolean shouldSucceed) {
    LoginPage page = new LoginPage(DriverManager.getDriver());
    if (shouldSucceed) {
        HomePage home = page.login(email, password);
        Assert.assertTrue(home.isLoaded());
    } else {
        page.loginExpectingError(email, password);
        Assert.assertTrue(page.isErrorDisplayed());
    }
}
```

### Q39. How do you generate Appium test reports?
```java
// Maven dependencies for Allure + TestNG
// Add to pom.xml:
// allure-testng, allure-java-commons, maven-surefire-plugin with argLine

// In test
import io.qameta.allure.*;

@Feature("Authentication")
@Story("User Login")
public class LoginTest extends BaseTest {
    
    @Test(description = "Verify successful login with valid credentials")
    @Severity(SeverityLevel.CRITICAL)
    @Step("Login with {email}")
    public void testLogin() {
        // Appium steps execute here
        Allure.addAttachment("Screenshot", 
            new ByteArrayInputStream(
                ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES)
            )
        );
    }
}

// Generate report:
// mvn allure:report
// mvn allure:serve
```

### Q40. Interview question: Tell me about the Appium architecture in your own words.

**Model Answer:**
"Appium acts as a middleware server. When my test script sends a command — say, click this button — it goes as an HTTP request to the Appium server running on port 4723. The server figures out whether we're testing Android or iOS from the capabilities I set at session start. For Android, it talks to UIAutomator2 through ADB, which then directly controls the UI. For iOS, it uses WebDriverAgent which Apple's own testing framework XCUITest drives. The beauty is my Java code stays the same regardless of platform — only the capabilities change."

---

## Q41 - Q50: Architecture and Interview Scenarios

### Q41. How do you set up Appium from scratch on a Windows machine?
```bash
# Prerequisites
# 1. Install Node.js (v16+)
node --version

# 2. Install Appium
npm install -g appium
appium --version

# 3. Install drivers
appium driver install uiautomator2

# 4. Install appium-doctor
npm install -g @appium/doctor
appium-doctor --android

# Expected output:
# ✔ ANDROID_HOME is set to /path/to/android/sdk
# ✔ JAVA_HOME is set to /path/to/jdk
# ✔ adb exists
# ✔ android exists
# ✔ emulator exists

# 5. Set environment variables
# ANDROID_HOME = C:\Users\YourName\AppData\Local\Android\sdk
# JAVA_HOME = C:\Program Files\Java\jdk-17
# Add to PATH: %ANDROID_HOME%\platform-tools, %ANDROID_HOME%\tools
```

### Q42. How do you start an Android emulator for Appium testing?
```bash
# List available AVDs
emulator -list-avds
# Pixel_5_API_31
# Pixel_4_API_30

# Start emulator
emulator -avd Pixel_5_API_31 -no-snapshot-load

# Or via Android Studio: Tools → AVD Manager → Launch

# Wait for boot
adb wait-for-device
adb shell getprop sys.boot_completed
# Returns "1" when ready

# Start Appium server
appium server --port 4723 --log-level info
```

### Q43. How do you find the correct XPath for a mobile element?
```java
// In Appium Inspector — click element, copy XPath

// Building XPath manually:
// By class: //android.widget.Button
// By text: //android.widget.TextView[@text='Login']
// By resource-id: //*[@resource-id='com.example:id/login_btn']
// Relative: //android.widget.LinearLayout//android.widget.Button[1]

// Dynamic XPath with contains:
By.xpath("//android.widget.TextView[contains(@text,'Hello')]")

// Multiple attributes:
By.xpath("//android.widget.Button[@text='Submit' and @enabled='true']")

// Tip: Prefer resource-id > accessibility-id > xpath
```

### Q44. How do you handle app crashes in tests?
```java
@Test
public void testWithCrashHandling() {
    try {
        performCriticalAction();
    } catch (WebDriverException e) {
        // App likely crashed
        String logs = String.join("\n", 
            ((AndroidDriver) driver).manage().logs().get("logcat").getAll()
                .stream()
                .map(entry -> entry.getMessage())
                .collect(Collectors.toList())
        );
        
        // Save crash logs
        FileUtils.writeStringToFile(new File("crash.log"), logs, "UTF-8");
        
        // Relaunch app
        driver.activateApp("com.example.app");
        
        Assert.fail("App crashed: " + e.getMessage());
    }
}
```

### Q45. How does Appium handle TestNG groups and priorities?
```java
@Test(groups = {"smoke", "login"}, priority = 1)
public void testValidLogin() { ... }

@Test(groups = {"regression", "login"}, priority = 2, 
      dependsOnMethods = "testValidLogin")
public void testLogout() { ... }

@Test(groups = {"negative", "login"}, priority = 3)
public void testInvalidLogin() { ... }
```

### Q46. What is an implicit wait vs explicit wait in Appium?
```java
// Implicit wait — applies to every findElement call
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// Explicit wait — targeted, per-element
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
WebElement el = wait.until(
    ExpectedConditions.elementToBeClickable(By.id("submit"))
);

// Best practice: Don't mix both — causes doubled wait times
// Use explicit wait only
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // disable implicit
```

### Q47. How do you verify text on mobile elements?
```java
// getText() — standard
String actualText = driver.findElement(By.id("label")).getText();
Assert.assertEquals(actualText, "Welcome, User!");

// getAttribute("text") — same on Android
String text = driver.findElement(By.id("label")).getAttribute("text");

// getAttribute("label") — iOS
String label = driver.findElement(AppiumBy.accessibilityId("header")).getAttribute("label");

// getAttribute("value") — for input fields
String value = driver.findElement(By.id("input_field")).getAttribute("value");
```

### Q48. What is the difference between `findElement` and `findElements`?
```java
// findElement — returns first match, throws NoSuchElementException if not found
WebElement single = driver.findElement(By.className("android.widget.TextView"));

// findElements — returns all matches, returns empty list if not found (no exception)
List<WebElement> all = driver.findElements(By.className("android.widget.TextView"));
System.out.println("Found: " + all.size());

// Use findElements to check existence:
boolean exists = !driver.findElements(By.id("error_msg")).isEmpty();
```

### Q49. How do you run Appium tests with Maven?
```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.appium</groupId>
    <artifactId>java-client</artifactId>
    <version>9.0.0</version>
</dependency>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.1.2</version>
            <configuration>
                <suiteXmlFiles>
                    <suiteXmlFile>src/test/resources/testng-android.xml</suiteXmlFile>
                </suiteXmlFiles>
                <systemPropertyVariables>
                    <platform>${platform}</platform>
                    <deviceName>${deviceName}</deviceName>
                </systemPropertyVariables>
            </configuration>
        </plugin>
    </plugins>
</build>
```

```bash
# Run
mvn test -Dplatform=Android -DdeviceName="Pixel_5"

# Run smoke tests only
mvn test -Dgroups=smoke

# Skip tests
mvn package -DskipTests
```

### Q50. Describe a real mobile testing scenario you would automate.

**Model Answer + Code:**
"At Cerexio, we would automate the mobile order management flow. Here's how I would structure it:"

```java
@Feature("Order Management")
public class OrderFlowTest extends BaseTest {

    @Test(description = "End-to-end order placement on Android")
    @Severity(SeverityLevel.CRITICAL)
    public void testPlaceOrder() {
        AndroidDriver driver = (AndroidDriver) DriverManager.getDriver();
        
        // 1. Login
        new LoginPage(driver)
            .enterUsername(TestData.STANDARD_USER)
            .enterPassword(TestData.PASSWORD)
            .clickLogin();
        
        // 2. Search product
        HomePage home = new HomePage(driver);
        home.searchProduct("Widget Pro");
        
        // 3. Select first result
        ProductListPage list = new ProductListPage(driver);
        list.selectProduct(0);
        
        // 4. Add to cart
        ProductDetailPage detail = new ProductDetailPage(driver);
        detail.selectQuantity(2);
        detail.tapAddToCart();
        
        // 5. Checkout
        CartPage cart = new CartPage(driver);
        Assert.assertEquals(cart.getItemCount(), 2);
        
        CheckoutPage checkout = cart.proceedToCheckout();
        checkout.enterDeliveryAddress("123 Main St, Colombo, Sri Lanka");
        checkout.selectPaymentMethod("Credit Card");
        
        // 6. Confirm order
        OrderConfirmationPage confirmation = checkout.placeOrder();
        
        // 7. Assert
        Assert.assertTrue(confirmation.isOrderConfirmed(),
            "Order confirmation should be displayed");
        Assert.assertNotNull(confirmation.getOrderId(),
            "Order ID should be generated");
        
        // 8. Verify in DB (JDBC integration)
        String orderId = confirmation.getOrderId();
        Assert.assertTrue(
            DBHelper.orderExists(orderId),
            "Order should be persisted in database"
        );
    }
}
```

---

*End of Part 8 — Appium Mobile Testing*
*Next: Part 9 — SQL for QA*
