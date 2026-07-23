# Part 3 — Core Java | 60 Questions | Full Answers + Code

> CV context: Kupeshanth Kupenthiran — Trainee QA at Qoria Lanka, Intern at Cerexio.
> Java, Selenium, TestNG, Playwright, Python. Singer Page BDD project (Serenity+Cucumber).

---

## Q1. What are the 4 pillars of OOP? Explain with automation examples.

The four pillars are **Encapsulation, Inheritance, Abstraction, and Polymorphism**.

### 1. Encapsulation — LoginPage hides its fields

Encapsulation means hiding the internal state and only exposing behaviour through methods.

```java
// LoginPage.java — fields are private, behaviour is public
public class LoginPage {
    private By usernameLocator = By.id("username");
    private By passwordLocator = By.id("password");
    private By loginBtnLocator = By.id("loginBtn");

    private WebDriver driver;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    // Exposed behaviour — caller never touches the locators directly
    public void login(String user, String pass) {
        driver.findElement(usernameLocator).sendKeys(user);
        driver.findElement(passwordLocator).sendKeys(pass);
        driver.findElement(loginBtnLocator).click();
    }
}
```

### 2. Inheritance — BaseTest sets up/tears down the driver

```java
// BaseTest.java
public class BaseTest {
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}

// LoginTest.java — inherits setUp/tearDown
public class LoginTest extends BaseTest {
    @Test
    public void validLoginTest() {
        driver.get("https://example.com");
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("admin", "password");
        // assertions
    }
}
```

### 3. Polymorphism — WebDriver reference works for Chrome, Firefox, Edge

```java
WebDriver driver;  // reference type = WebDriver (interface)

driver = new ChromeDriver();   // runtime object = Chrome
driver = new FirefoxDriver();  // swap to Firefox — same code
driver = new EdgeDriver();     // swap to Edge   — same code

// Overloading = compile-time polymorphism
public void login(String user, String pass) { ... }
public void login(String user, String pass, boolean rememberMe) { ... }
```

### 4. Abstraction — BasePage defines a contract for all page objects

```java
// BasePage.java — abstract class
public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Concrete helper available to all pages
    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Abstract — each page must implement its own verification
    public abstract boolean isPageLoaded();
}

// LoginPage.java — must implement isPageLoaded
public class LoginPage extends BasePage {
    public LoginPage(WebDriver driver) { super(driver); }

    @Override
    public boolean isPageLoaded() {
        return waitForElement(By.id("username")).isDisplayed();
    }
}
```

---

## Q2. What is the difference between class and object?

| Concept | Description |
|---------|-------------|
| Class   | Blueprint / template — defines fields and methods |
| Object  | An instance of the class — actual memory allocation |

```java
// Class — definition (blueprint)
public class Car {
    String brand;
    int speed;

    void accelerate() {
        speed += 10;
        System.out.println(brand + " going at " + speed);
    }
}

// Objects — instances
Car car1 = new Car();   // new object created on heap
car1.brand = "Toyota";

Car car2 = new Car();   // separate object, separate memory
car2.brand = "Honda";

// Automation context
// ChromeDriver is the class
// driver = new ChromeDriver() is the object
WebDriver driver = new ChromeDriver();
```

**Key points:**
- A class can have many objects.
- Each object has its own copy of instance variables.
- Static members belong to the class, not to any object.

---

## Q3. What is a constructor? Types of constructors?

A constructor is a special method that has the **same name as the class**, no return type, and is called automatically when an object is created.

### Types

**1. Default constructor** — JVM provides one if you write none

```java
public class BrowserConfig {
    String browser;
    int timeout;
    // JVM generates: public BrowserConfig() {}
}

BrowserConfig config = new BrowserConfig(); // JVM default used
```

**2. No-arg constructor** — you write it explicitly

```java
public class BrowserConfig {
    String browser;
    int timeout;

    public BrowserConfig() {
        browser = "chrome";
        timeout = 10;
    }
}
```

**3. Parameterized constructor** — accepts arguments

```java
public class BrowserConfig {
    String browser;
    int timeout;

    public BrowserConfig(String browser, int timeout) {
        this.browser = browser;
        this.timeout = timeout;
    }
}

BrowserConfig config = new BrowserConfig("firefox", 15);
```

**4. Copy constructor** — copies another object (not built-in in Java, written manually)

```java
public class TestData {
    String username;
    String password;

    public TestData(TestData other) {
        this.username = other.username;
        this.password = other.password;
    }
}
```

> **Note:** Constructors are NOT inherited. A subclass must call `super(...)` if the parent has only parameterised constructors.

---

## Q4. What is method overloading vs method overriding?

### Method Overloading (Compile-time polymorphism)

Same method name, different parameter list, in the **same class**.

```java
public class WaitHelper {
    // Overload 1 — default wait
    public WebElement waitFor(By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Overload 2 — custom timeout
    public WebElement waitFor(By locator, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Overload 3 — wait for text
    public boolean waitFor(By locator, String expectedText) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.textToBe(locator, expectedText));
    }
}
```

### Method Overriding (Runtime polymorphism)

Child class provides a new implementation of a method inherited from the parent.

```java
public class BasePage {
    public void load(String url) {
        driver.get(url);
    }
    public boolean isLoaded() {
        return true;  // default
    }
}

public class CheckoutPage extends BasePage {
    @Override
    public boolean isLoaded() {
        // Custom implementation for this specific page
        return driver.findElement(By.id("checkoutForm")).isDisplayed();
    }
}
```

| Feature | Overloading | Overriding |
|---------|-------------|------------|
| Where   | Same class  | Parent + child class |
| Signature | Must differ | Must be same |
| Return type | Can differ | Must be same (or covariant) |
| Binding | Compile-time | Runtime |
| Annotation | N/A | `@Override` (recommended) |

---

## Q5. What is an interface vs abstract class? When to use each?

### Interface

- All methods are implicitly `public abstract` (before Java 8).
- Java 8+ allows `default` and `static` methods.
- A class can **implement multiple** interfaces.
- No constructor, no instance variables (only `public static final` constants).

```java
public interface Clickable {
    void click(By locator);

    default void clickWithRetry(By locator, int retries) {
        for (int i = 0; i < retries; i++) {
            try { click(locator); return; }
            catch (Exception e) { /* retry */ }
        }
    }
}

public interface Verifiable {
    boolean isDisplayed(By locator);
    String getText(By locator);
}

// Multiple interface implementation
public class LoginPage implements Clickable, Verifiable {
    @Override public void click(By locator) { driver.findElement(locator).click(); }
    @Override public boolean isDisplayed(By locator) { return driver.findElement(locator).isDisplayed(); }
    @Override public String getText(By locator) { return driver.findElement(locator).getText(); }
}
```

### Abstract Class

- Can have constructors, instance variables, concrete methods.
- A class can extend only **one** abstract class.
- Use when subclasses share common state or behaviour.

```java
public abstract class BasePage {
    protected WebDriver driver;            // shared state
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {    // constructor
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Concrete — shared helper
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    // Abstract — subclass must implement
    public abstract boolean isPageLoaded();
}
```

### When to use each

| Use Interface when... | Use Abstract Class when... |
|-----------------------|---------------------------|
| You want to define a capability/contract | You want to share common code and state |
| Multiple inheritance of type is needed | You need a constructor with common setup |
| Unrelated classes share a contract | Closely related subclasses share logic |

---

## Q6. Can we instantiate an abstract class?

**No.** An abstract class cannot be instantiated directly.

```java
abstract class BasePage {
    public abstract boolean isPageLoaded();
}

// COMPILE ERROR:
BasePage page = new BasePage();  // Cannot instantiate abstract class

// CORRECT — instantiate a concrete subclass
BasePage page = new LoginPage(driver);  // LoginPage extends BasePage

// ANONYMOUS CLASS — technically creates a subclass on the fly
BasePage anon = new BasePage() {
    @Override
    public boolean isPageLoaded() { return true; }
};
```

---

## Q7. What is the difference between `this` and `super`?

| Keyword | Purpose |
|---------|---------|
| `this`  | Refers to the current object's instance |
| `super` | Refers to the parent class |

```java
public class BasePage {
    protected WebDriver driver;
    protected int timeout;

    public BasePage(WebDriver driver, int timeout) {
        this.driver = driver;    // this.driver = field, driver = parameter
        this.timeout = timeout;
    }

    public void click(By locator) {
        driver.findElement(locator).click();
    }
}

public class LoginPage extends BasePage {
    private String pageUrl;

    public LoginPage(WebDriver driver) {
        super(driver, 10);        // calls parent constructor
        this.pageUrl = "/login";  // sets this class's field
    }

    @Override
    public void click(By locator) {
        System.out.println("LoginPage click with extra logging");
        super.click(locator);     // calls parent's click method
    }
}
```

---

## Q8. What is constructor chaining?

Constructor chaining is calling one constructor from another using `this()` or `super()`.

```java
public class DriverConfig {
    String browser;
    int timeout;
    boolean headless;

    // Constructor 1 — primary
    public DriverConfig(String browser, int timeout, boolean headless) {
        this.browser = browser;
        this.timeout = timeout;
        this.headless = headless;
    }

    // Constructor 2 — chains to Constructor 1
    public DriverConfig(String browser, int timeout) {
        this(browser, timeout, false);  // this() — calls constructor in same class
    }

    // Constructor 3 — chains to Constructor 2
    public DriverConfig(String browser) {
        this(browser, 10);              // chains again
    }

    // Constructor 4 — uses all defaults
    public DriverConfig() {
        this("chrome");                 // chains to constructor 3
    }
}

// Usage
DriverConfig c1 = new DriverConfig();                   // chrome, 10, false
DriverConfig c2 = new DriverConfig("firefox");          // firefox, 10, false
DriverConfig c3 = new DriverConfig("chrome", 15, true); // chrome, 15, true
```

> **Rule:** `this()` or `super()` must be the **first statement** in a constructor.

---

## Q9. What are access modifiers?

| Modifier    | Same class | Same package | Subclass | Everywhere |
|-------------|:----------:|:------------:|:--------:|:----------:|
| `public`    | Yes        | Yes          | Yes      | Yes        |
| `protected` | Yes        | Yes          | Yes      | No         |
| default     | Yes        | Yes          | No       | No         |
| `private`   | Yes        | No           | No       | No         |

```java
public class BasePage {
    public WebDriver driver;      // accessible anywhere (avoid — breaks encapsulation)
    protected WebDriverWait wait; // accessible in subclasses (good for helpers)
    int retryCount = 3;           // default — same package only
    private String sessionId;     // fully hidden — only this class

    public String getSessionId() {         // getter — controlled access
        return sessionId;
    }
    private void initSession() { ... }    // internal helper — private is correct
}
```

---

## Q10. What is static vs instance?

**Static** members belong to the **class**. **Instance** members belong to the **object**.

```java
public class DriverManager {
    // Static — shared across ALL instances / accessible without creating object
    private static WebDriver driver;
    private static int instanceCount = 0;

    // Instance — each object has its own copy
    private String sessionName;
    private long startTime;

    public static WebDriver getDriver() { return driver; }
    public static void setDriver(WebDriver d) { driver = d; }

    public DriverManager(String name) {
        this.sessionName = name;         // instance field
        this.startTime = System.currentTimeMillis();
        instanceCount++;                 // shared static counter
    }
}

// Static method — no object needed
DriverManager.setDriver(new ChromeDriver());
WebDriver driver = DriverManager.getDriver();

// Instance method — object required
DriverManager mgr = new DriverManager("Session1");
```

**Rules:**
- Static methods cannot access instance fields directly.
- Instance methods can access both static and instance fields.
- `main` is static because JVM calls it before any object exists.

---

## Q11. What is the `final` keyword?

`final` prevents modification or extension.

```java
// 1. final variable — value cannot change (effectively a constant)
public final int MAX_TIMEOUT = 30;
public static final String BASE_URL = "https://app.example.com";

// 2. final method — cannot be overridden in a subclass
public class BasePage {
    public final void takeScreenshot(String name) {
        // critical utility — no override allowed
        TakesScreenshot ts = (TakesScreenshot) driver;
        File src = ts.getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(src, new File("screenshots/" + name + ".png"));
    }
}

// 3. final class — cannot be subclassed
// String is a final class — you cannot extend String
public final class EncryptionUtil {
    // Utility class — no one should extend this
    public static String encrypt(String data) { ... }
}

// 4. final parameter — cannot be reassigned inside the method
public void login(final String username, final String password) {
    // username = "other";  // COMPILE ERROR
    driver.findElement(By.id("user")).sendKeys(username);
}
```

---

## Q12. What is the difference between == and equals() for Strings? (CRITICAL)

`==` compares **references** (memory address). `equals()` compares **content**.

```java
String a = "hello";              // goes to String pool
String b = "hello";              // same pool reference
String c = new String("hello");  // new object on heap

System.out.println(a == b);         // true  — same pool reference
System.out.println(a == c);         // false — different objects
System.out.println(a.equals(c));    // true  — same content

// AUTOMATION TRAP — comparing page title
String expected = "Dashboard";
String actual = driver.getTitle();

// WRONG — compares references
if (actual == expected) { ... }

// CORRECT — compares content
if (actual.equals(expected)) { ... }

// Even safer — case-insensitive
if (actual.equalsIgnoreCase(expected)) { ... }

// Avoiding NullPointerException — put the literal first
if ("Dashboard".equals(actual)) { ... }  // won't throw NPE if actual is null

// assertEquals in TestNG/JUnit uses .equals() internally
Assert.assertEquals(actual, expected);
```

**Key rule:** Always use `.equals()` for String content comparison. Never use `==`.

---

## Q13. What is String, StringBuilder, StringBuffer? When to use each?

### String — Immutable

```java
String s = "Hello";
s = s + " World";  // creates a NEW String object — original "Hello" is abandoned

// Proof of immutability
String s1 = "Test";
String s2 = s1.concat(" Value"); // returns a NEW string
System.out.println(s1); // still "Test"
```

### StringBuilder — Mutable, NOT thread-safe, fast

```java
// Building XPath or CSS selectors dynamically
StringBuilder xpath = new StringBuilder("//div[");
xpath.append("@class='container'");
xpath.append(" and ");
xpath.append("@data-testid='login'");
xpath.append("]");
String result = xpath.toString();
// Result: //div[@class='container' and @data-testid='login']

// Building test report strings
StringBuilder report = new StringBuilder();
for (String step : testSteps) {
    report.append(step).append("\n");
}
```

### StringBuffer — Mutable, thread-safe, slower

```java
// Use when multiple threads write to the same string
// Rare in test automation — prefer StringBuilder
StringBuffer buffer = new StringBuffer();
buffer.append("Thread-safe content");
```

| Feature | String | StringBuilder | StringBuffer |
|---------|--------|---------------|--------------|
| Mutable | No | Yes | Yes |
| Thread-safe | Yes (immutable) | No | Yes |
| Performance | Slow (many ops) | Fast | Slower |
| Use when | Few concatenations, constants | Single-thread, dynamic build | Multi-thread string building |

---

## Q14. What is autoboxing and unboxing?

Autoboxing is automatic conversion between primitive types and their wrapper class equivalents.

```java
// Autoboxing — primitive to wrapper (automatic)
int primitiveInt = 42;
Integer wrapperInt = primitiveInt;  // auto-boxed

List<Integer> list = new ArrayList<>();
list.add(5);    // autoboxing: int 5 → Integer(5)
list.add(10);   // autoboxing

// Unboxing — wrapper to primitive (automatic)
int sum = list.get(0) + list.get(1);  // unboxed back to int

// Automation example — test data maps
Map<String, Integer> timeouts = new HashMap<>();
timeouts.put("login", 10);       // autoboxing: 10 → Integer(10)
timeouts.put("checkout", 30);

int checkoutTimeout = timeouts.get("checkout");  // unboxing: Integer → int

// Null unboxing trap
Integer value = null;
int result = value;  // NullPointerException — cannot unbox null

// Fix
int result = (value != null) ? value : 0;
```

---

## Q15. What is ArrayList vs LinkedList vs Vector?

```java
// ArrayList — dynamic array, fast random access O(1), slow insert/delete at middle O(n)
List<String> pageNames = new ArrayList<>();
pageNames.add("Home");
pageNames.add("Login");
pageNames.add("Dashboard");
pageNames.get(1);  // fast — direct index access

// LinkedList — doubly linked list, fast insert/delete O(1), slow access O(n)
LinkedList<String> stepQueue = new LinkedList<>();
stepQueue.addFirst("Open browser");   // fast prepend
stepQueue.addLast("Close browser");   // fast append
stepQueue.removeFirst();              // fast removal

// Vector — like ArrayList but synchronized (thread-safe), slower, legacy
Vector<String> legacyList = new Vector<>();  // avoid in new code

// In automation — ArrayList is used almost everywhere
List<WebElement> checkboxes = driver.findElements(By.cssSelector("input[type='checkbox']"));
// checkboxes is already an ArrayList<WebElement>
```

| Feature | ArrayList | LinkedList | Vector |
|---------|-----------|------------|--------|
| Underlying | Dynamic array | Doubly linked list | Dynamic array |
| Random access | O(1) | O(n) | O(1) |
| Insert/delete at ends | O(n) | O(1) | O(n) |
| Thread-safe | No | No | Yes |
| Use when | Most cases | Queue/stack operations | Legacy thread-safe (avoid) |

---

## Q16. What is HashMap vs LinkedHashMap vs TreeMap?

```java
// HashMap — no guaranteed order, O(1) average get/put
Map<String, String> testData = new HashMap<>();
testData.put("username", "admin");
testData.put("password", "secret");
testData.put("email", "admin@test.com");
// Iteration order NOT guaranteed

// LinkedHashMap — maintains INSERTION order
Map<String, String> orderedConfig = new LinkedHashMap<>();
orderedConfig.put("browser", "chrome");
orderedConfig.put("env", "staging");
orderedConfig.put("timeout", "10");
// Iterates in insertion order: browser → env → timeout

// TreeMap — sorted by key (natural order), O(log n) get/put
Map<String, Integer> sortedErrors = new TreeMap<>();
sortedErrors.put("ZeroTimeout", 5);
sortedErrors.put("ElementNotFound", 12);
sortedErrors.put("AssertionFailed", 3);
// Iterates alphabetically: AssertionFailed, ElementNotFound, ZeroTimeout

// Automation use case — test config with known order
Map<String, String> envVars = new LinkedHashMap<>();
envVars.put("BASE_URL", "https://staging.example.com");
envVars.put("API_KEY", "abc123");
```

---

## Q17. How does HashMap work internally?

HashMap uses an **array of buckets** (Node[] table). Each bucket is a linked list (or red-black tree when size > 8).

```java
// Step 1: hashCode() is computed for the key
String key = "username";
int hash = key.hashCode();  // e.g., -1268878963

// Step 2: index = (n - 1) & hash  where n = capacity (default 16)
// Determines which bucket to use

// Step 3: If collision — keys with same bucket index form a linked list
// Java 8+: linked list converts to red-black tree when > 8 entries in one bucket

Map<String, String> map = new HashMap<>();
map.put("username", "admin");
// Internally:
// 1. hash("username") computed
// 2. bucket index = hash & 15 (for capacity 16)
// 3. New Node("username", "admin") stored in that bucket

// Retrieval
map.get("username");
// 1. hash("username") computed again
// 2. Same bucket found
// 3. .equals("username") used to find the exact entry

// Default capacity: 16, load factor: 0.75
// Resizes (doubles) when: size > capacity * load_factor (16 * 0.75 = 12 entries)

// CRITICAL: key objects must properly implement hashCode() AND equals()
// If hashCode() is inconsistent with equals(), HashMap behaves incorrectly
```

**Interview one-liner:** "HashMap uses hashCode() to find the bucket and equals() to find the exact entry within that bucket. Collisions are resolved by a linked list, upgraded to a red-black tree after 8 entries."

---

## Q18. What is HashSet vs LinkedHashSet vs TreeSet?

Sets do not allow **duplicate elements**.

```java
// HashSet — no order, O(1) add/contains
Set<String> browsers = new HashSet<>();
browsers.add("chrome");
browsers.add("firefox");
browsers.add("chrome");  // duplicate — ignored silently
System.out.println(browsers.size());  // 2

// LinkedHashSet — insertion order maintained
Set<String> executionOrder = new LinkedHashSet<>();
executionOrder.add("login");
executionOrder.add("addToCart");
executionOrder.add("checkout");
// Iterates: login → addToCart → checkout

// TreeSet — sorted order, O(log n)
Set<String> sortedTests = new TreeSet<>();
sortedTests.add("TC003_Logout");
sortedTests.add("TC001_Login");
sortedTests.add("TC002_Search");
// Iterates: TC001_Login, TC002_Search, TC003_Logout

// Automation use case — deduplicate test data
List<String> rawEmails = Arrays.asList("a@test.com", "b@test.com", "a@test.com");
Set<String> uniqueEmails = new HashSet<>(rawEmails);
// uniqueEmails: {a@test.com, b@test.com}
```

---

## Q19. What is Iterator vs ListIterator?

```java
List<WebElement> elements = driver.findElements(By.tagName("a"));

// Iterator — forward-only, works on any Collection
Iterator<WebElement> it = elements.iterator();
while (it.hasNext()) {
    WebElement el = it.next();
    System.out.println(el.getText());
    // can remove current element safely:
    if (el.getText().isEmpty()) it.remove();
}

// ListIterator — bidirectional, only for List
ListIterator<String> listIt = testSteps.listIterator();
while (listIt.hasNext()) {
    String step = listIt.next();
    // Go forward
}
while (listIt.hasPrevious()) {
    String step = listIt.previous();
    // Go backward
}
listIt.set("Updated step");  // replace current element
listIt.add("New step");      // insert at current position
```

| Feature | Iterator | ListIterator |
|---------|----------|--------------|
| Direction | Forward only | Forward & backward |
| Works on | Any Collection | List only |
| Remove | Yes | Yes |
| Replace/Add | No | Yes |

---

## Q20. What is ConcurrentModificationException and how to fix it?

Thrown when you modify a collection while iterating over it with an enhanced for loop or iterator.

```java
List<String> tests = new ArrayList<>(Arrays.asList("TC1", "TC2", "TC3", "SKIP_TC4"));

// WRONG — throws ConcurrentModificationException
for (String test : tests) {
    if (test.startsWith("SKIP")) {
        tests.remove(test);  // modifying while iterating — CRASH
    }
}

// FIX 1 — use Iterator.remove()
Iterator<String> it = tests.iterator();
while (it.hasNext()) {
    if (it.next().startsWith("SKIP")) {
        it.remove();  // safe removal through iterator
    }
}

// FIX 2 — removeIf (Java 8, cleanest)
tests.removeIf(test -> test.startsWith("SKIP"));

// FIX 3 — collect to a new list
List<String> toRun = tests.stream()
    .filter(test -> !test.startsWith("SKIP"))
    .collect(Collectors.toList());

// FIX 4 — CopyOnWriteArrayList (thread-safe, for concurrent contexts)
List<String> safeList = new CopyOnWriteArrayList<>(tests);
for (String test : safeList) {
    if (test.startsWith("SKIP")) {
        safeList.remove(test);  // no exception — iterates on a copy
    }
}
```

---

## Q21. What is try-catch-finally?

```java
public String readConfigFile(String path) {
    BufferedReader reader = null;
    try {
        // Code that may throw an exception
        reader = new BufferedReader(new FileReader(path));
        return reader.readLine();

    } catch (FileNotFoundException e) {
        System.err.println("Config file not found: " + path);
        return "chrome";  // default fallback

    } catch (IOException e) {
        System.err.println("Error reading config: " + e.getMessage());
        return null;

    } finally {
        // ALWAYS executes — even if return or exception happened
        if (reader != null) {
            try { reader.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
        System.out.println("readConfigFile() finished");
    }
}
```

**Key rules:**
- `finally` always executes (except `System.exit()`).
- `finally` executes even if `catch` has a `return`.
- If `finally` has a `return`, it overrides the `catch` return.

---

## Q22. What is checked vs unchecked exception?

```java
// CHECKED exceptions — must be declared or caught (compile-time check)
// Extends Exception (not RuntimeException)
public void readFile() throws IOException {          // must declare
    FileReader reader = new FileReader("data.csv");  // FileNotFoundException is checked
    // ...
}

public void readFile() {
    try {
        FileReader reader = new FileReader("data.csv");
    } catch (IOException e) {  // must catch
        e.printStackTrace();
    }
}

// Common checked: IOException, SQLException, ClassNotFoundException

// UNCHECKED exceptions — optional to catch (runtime)
// Extends RuntimeException
public void clickElement(WebElement el) {
    el.click();  // may throw NullPointerException — no forced handling
}

// Common unchecked: NullPointerException, ArrayIndexOutOfBoundsException,
// IllegalArgumentException, ClassCastException, StaleElementReferenceException
```

| Type | Extends | Checked at compile-time | Must catch/declare |
|------|---------|------------------------|-------------------|
| Checked | `Exception` | Yes | Yes |
| Unchecked | `RuntimeException` | No | No |

---

## Q23. What is try-with-resources?

Automatically closes resources that implement `AutoCloseable`. No need for `finally`.

```java
// OLD way — verbose finally block
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("data.csv"));
    String line = reader.readLine();
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (reader != null) try { reader.close(); } catch (IOException e) {}
}

// NEW way — try-with-resources (Java 7+)
try (BufferedReader reader = new BufferedReader(new FileReader("data.csv"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
} catch (IOException e) {
    e.printStackTrace();
}
// reader.close() called automatically — even on exception

// Multiple resources
try (FileReader fr = new FileReader("input.csv");
     FileWriter fw = new FileWriter("output.csv")) {
    // both closed automatically in reverse order
} catch (IOException e) {
    e.printStackTrace();
}
```

---

## Q24. What is exception chaining?

Wrapping one exception inside another to preserve the original cause.

```java
public class PageLoadException extends RuntimeException {
    public PageLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}

public void loadLoginPage() {
    try {
        driver.findElement(By.id("username"));
    } catch (NoSuchElementException e) {
        // Chain — preserve original exception as cause
        throw new PageLoadException("Login page did not load correctly", e);
    }
}

// Caller can access both
try {
    loadLoginPage();
} catch (PageLoadException e) {
    System.out.println("High-level: " + e.getMessage());
    System.out.println("Root cause: " + e.getCause().getMessage());
    e.printStackTrace();  // shows full chain
}
```

---

## Q25. How do you create a custom exception?

```java
// Custom checked exception
public class TestDataException extends Exception {
    public TestDataException(String message) {
        super(message);
    }
    public TestDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Custom unchecked exception
public class DriverInitException extends RuntimeException {
    private final String browser;

    public DriverInitException(String browser, String message) {
        super(message);
        this.browser = browser;
    }

    public String getBrowser() { return browser; }
}

// Usage
public WebDriver createDriver(String browser) {
    switch (browser.toLowerCase()) {
        case "chrome": return new ChromeDriver();
        case "firefox": return new FirefoxDriver();
        default:
            throw new DriverInitException(browser, "Unsupported browser: " + browser);
    }
}

try {
    WebDriver d = createDriver("safari");
} catch (DriverInitException e) {
    System.out.println("Failed browser: " + e.getBrowser());
    System.out.println("Reason: " + e.getMessage());
}
```

---

## Q26. What are Lambda expressions? (Java 8)

A lambda is a concise way to represent an anonymous function (implementation of a functional interface).

```java
// Syntax: (parameters) -> expression  or  (parameters) -> { statements; }

// Old way — anonymous class
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Test started");
    }
};

// Lambda equivalent
Runnable r = () -> System.out.println("Test started");

// Comparator — old way
Collections.sort(testNames, new Comparator<String>() {
    @Override
    public int compare(String a, String b) { return a.compareTo(b); }
});

// Lambda equivalent
Collections.sort(testNames, (a, b) -> a.compareTo(b));
// Even shorter with method reference
Collections.sort(testNames, String::compareTo);

// WebDriverWait — ExpectedCondition is a functional interface
wait.until(driver -> driver.findElement(By.id("result")).isDisplayed());

// Filtering test data
List<String> failedTests = allTests.stream()
    .filter(test -> test.startsWith("FAIL"))  // lambda
    .collect(Collectors.toList());

// Clicking each element
elements.forEach(el -> el.click());
elements.forEach(WebElement::click);  // method reference
```

---

## Q27. What is a functional interface?

A functional interface has exactly **one abstract method**. Lambdas can be used wherever a functional interface is expected.

```java
// @FunctionalInterface annotation — enforces single abstract method
@FunctionalInterface
public interface PageAction {
    void execute(WebDriver driver);

    // Can have default and static methods — doesn't violate the rule
    default void withLogging(WebDriver driver) {
        System.out.println("Executing action");
        execute(driver);
    }
}

// Using the custom functional interface
PageAction openLogin = driver -> driver.get("https://example.com/login");
PageAction clickSubmit = driver -> driver.findElement(By.id("submit")).click();

openLogin.execute(driver);
clickSubmit.execute(driver);

// Built-in functional interfaces (java.util.function)
Predicate<String> isNotEmpty = str -> !str.isEmpty();           // takes T, returns boolean
Function<String, Integer> strLen = str -> str.length();          // takes T, returns R
Consumer<WebElement> clickEl = WebElement::click;                // takes T, returns void
Supplier<WebDriver> driverSupplier = ChromeDriver::new;         // takes nothing, returns T
BiFunction<String, String, Boolean> matches = String::equals;   // takes T, U, returns R

// ExpectedCondition<T> is a functional interface
wait.until(driver -> {
    String title = driver.getTitle();
    return title.contains("Dashboard");
});
```

---

## Q28. What are Streams? (filter, map, collect, count, findFirst, anyMatch)

Streams allow functional-style operations on collections without modifying the source.

```java
List<String> allTests = Arrays.asList(
    "TC01_Login", "TC02_Logout", "TC03_Search", "TC04_Checkout", "SKIP_TC05"
);

// filter — keep elements matching a condition
List<String> toRun = allTests.stream()
    .filter(t -> !t.startsWith("SKIP"))
    .collect(Collectors.toList());
// [TC01_Login, TC02_Logout, TC03_Search, TC04_Checkout]

// map — transform each element
List<String> upperNames = allTests.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());

// map to extract locator text from web elements
List<String> linkTexts = driver.findElements(By.tagName("a"))
    .stream()
    .map(WebElement::getText)
    .collect(Collectors.toList());

// count — terminal operation returning long
long skipCount = allTests.stream().filter(t -> t.startsWith("SKIP")).count();

// findFirst — returns Optional
Optional<String> firstFail = allTests.stream()
    .filter(t -> t.contains("Fail"))
    .findFirst();
firstFail.ifPresent(t -> System.out.println("First failure: " + t));

// anyMatch — returns boolean
boolean hasSkipped = allTests.stream().anyMatch(t -> t.startsWith("SKIP"));

// allMatch / noneMatch
boolean allPassible = allTests.stream().noneMatch(t -> t.startsWith("SKIP"));

// Chaining
long count = driver.findElements(By.tagName("input")).stream()
    .filter(el -> el.getAttribute("type").equals("checkbox"))
    .filter(WebElement::isEnabled)
    .count();

// collect to map
Map<String, Integer> testLengths = allTests.stream()
    .collect(Collectors.toMap(t -> t, String::length));

// joining
String report = allTests.stream().collect(Collectors.joining(", "));
```

---

## Q29. What is Optional and why use it?

Optional is a container that may or may not hold a non-null value. It forces callers to handle the case where a value may be absent, preventing NullPointerExceptions.

```java
// Without Optional — NPE risk
public String getTestEnv() {
    return System.getenv("TEST_ENV");  // may return null
}
String env = getTestEnv().toUpperCase();  // NPE if null

// With Optional
public Optional<String> getTestEnv() {
    return Optional.ofNullable(System.getenv("TEST_ENV"));
}

Optional<String> env = getTestEnv();

// isPresent / get
if (env.isPresent()) {
    System.out.println(env.get().toUpperCase());
}

// orElse — default value
String value = env.orElse("staging");

// orElseGet — lazy default
String value = env.orElseGet(() -> loadDefaultEnv());

// orElseThrow
String value = env.orElseThrow(() -> new RuntimeException("TEST_ENV not set"));

// map — transform value inside Optional
Optional<String> upper = env.map(String::toUpperCase);

// ifPresent
env.ifPresent(e -> System.out.println("Running on: " + e));

// Automation example — optional element
Optional<WebElement> closeButton = driver.findElements(By.id("closeModal"))
    .stream().findFirst();
closeButton.ifPresent(WebElement::click);
```

---

## Q30. What are method references (4 types)?

Method references are a shorthand for lambdas that call a single method.

```java
// Syntax: ClassName::methodName   or   instance::methodName

// Type 1: Static method reference — ClassName::staticMethod
Function<String, Integer> parse = Integer::parseInt;
// equivalent to: str -> Integer.parseInt(str)

List<String> numbers = Arrays.asList("1", "2", "3");
List<Integer> ints = numbers.stream().map(Integer::parseInt).collect(Collectors.toList());

// Type 2: Instance method of a particular object — instance::method
String prefix = "TC_";
Predicate<String> startsWith = prefix::equals;
// equivalent to: s -> prefix.equals(s) -- note: rare; usually ClassName::method

// More common form:
PrintStream out = System.out;
Consumer<String> print = out::println;
// equivalent to: s -> System.out.println(s)

// Type 3: Instance method of an arbitrary object of a type — ClassName::instanceMethod
Function<String, String> toUpper = String::toUpperCase;
// equivalent to: str -> str.toUpperCase()

List<WebElement> elements = driver.findElements(By.tagName("a"));
elements.stream().map(WebElement::getText).forEach(System.out::println);
// WebElement::getText = el -> el.getText()

// Type 4: Constructor reference — ClassName::new
Supplier<ArrayList> listFactory = ArrayList::new;
ArrayList list = listFactory.get();

// In Driver factories
Function<ChromeOptions, ChromeDriver> driverFactory = ChromeDriver::new;
ChromeDriver d = driverFactory.apply(new ChromeOptions());
```

---

## Q31. What is the difference between for loop, enhanced for, forEach?

```java
List<String> testNames = Arrays.asList("TC01", "TC02", "TC03");

// 1. Traditional for loop — use when you need index
for (int i = 0; i < testNames.size(); i++) {
    System.out.println("Test " + i + ": " + testNames.get(i));
}

// 2. Enhanced for (for-each) — use when index not needed, simple iteration
for (String name : testNames) {
    System.out.println(name);
}
// Cannot modify the list while iterating (ConcurrentModificationException)
// Cannot get current index directly

// 3. forEach (Java 8 Iterable.forEach) — functional style
testNames.forEach(name -> System.out.println(name));
testNames.forEach(System.out::println);  // method reference

// forEach with index — use IntStream
IntStream.range(0, testNames.size())
    .forEach(i -> System.out.println(i + ": " + testNames.get(i)));

// Automation context — iterating WebElements
List<WebElement> rows = driver.findElements(By.cssSelector("table tr"));

// Traditional — need index
for (int i = 0; i < rows.size(); i++) {
    System.out.println("Row " + i + ": " + rows.get(i).getText());
}

// Enhanced for — simple text printing
for (WebElement row : rows) {
    System.out.println(row.getText());
}

// Stream forEach — filter then act
rows.stream()
    .filter(r -> r.getText().contains("Active"))
    .forEach(WebElement::click);
```

---

## Q32. What is ThreadLocal? (Critical for parallel Selenium)

ThreadLocal provides each thread with its own isolated copy of a variable. Without it, parallel tests share one WebDriver instance and crash each other.

```java
// DriverManager.java — ThreadLocal-based driver management
public class DriverManager {

    // Each thread gets its OWN WebDriver instance
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void removeDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();  // prevents memory leak
        }
    }
}

// BaseTest.java
public class BaseTest {

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        DriverManager.setDriver(driver);
    }

    @AfterMethod
    public void tearDown() {
        DriverManager.removeDriver();  // quit + cleanup thread's driver
    }
}

// Any page/test — gets the correct driver for THIS thread
public class LoginPage {
    private WebDriver driver = DriverManager.getDriver();
    // Thread-1's test gets Thread-1's driver
    // Thread-2's test gets Thread-2's driver
}
```

**Without ThreadLocal:** Thread-1 creates driver, Thread-2 creates another driver, but they both write to the same static field. Thread-1's driver gets overwritten → test crashes.

---

## Q33. What is synchronized keyword?

`synchronized` ensures only one thread can execute a block or method at a time.

```java
// Synchronized method
public class ReportLogger {
    private List<String> logs = new ArrayList<>();

    // Only one thread can call addLog at a time
    public synchronized void addLog(String message) {
        logs.add(message);
    }

    // Synchronized block — finer control
    public void addLogBlock(String message) {
        // non-synchronized work
        String formatted = "[" + Thread.currentThread().getName() + "] " + message;

        // only the critical section is locked
        synchronized (this) {
            logs.add(formatted);
        }
    }
}

// Singleton with double-checked locking
public class DriverFactory {
    private static volatile DriverFactory instance;

    private DriverFactory() {}

    public static DriverFactory getInstance() {
        if (instance == null) {
            synchronized (DriverFactory.class) {
                if (instance == null) {  // second check inside synchronized
                    instance = new DriverFactory();
                }
            }
        }
        return instance;
    }
}
```

---

## Q34. What is volatile?

`volatile` tells the JVM that a variable can be modified by multiple threads and should always be read from main memory (not from CPU cache).

```java
public class DriverFactory {
    // volatile — ensures all threads see the latest value
    private static volatile DriverFactory instance;

    // Without volatile:
    // Thread 1 sets instance = new DriverFactory()
    // Thread 2 may still see instance == null (reading from cache)
    // → Two instances created!

    // With volatile:
    // Any write to instance is immediately visible to all threads
}

// volatile does NOT provide atomicity
private volatile int count = 0;
count++;  // read-modify-write — still NOT atomic even with volatile
// Use AtomicInteger for atomic increment
private AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();  // atomic
```

**Rule:** Use `volatile` for flags and single-write-multi-read scenarios. Use `synchronized` or `AtomicXxx` for compound operations.

---

## Q35. What is a design pattern? Name the ones used in automation.

A design pattern is a reusable solution to a commonly occurring problem in software design.

| Category | Pattern | Use in Automation |
|----------|---------|-------------------|
| Creational | Singleton | Single WebDriver, single config manager |
| Creational | Factory | DriverFactory — create Chrome/Firefox/Edge |
| Creational | Builder | Test data objects with many optional fields |
| Structural | Page Object Model | Encapsulate UI interactions in page classes |
| Structural | Facade | BasePage — hide WebDriver complexity |
| Structural | Decorator | Logging wrapper around WebDriver |
| Behavioural | Strategy | Different browser strategies |
| Behavioural | Command | Test steps as command objects |
| Behavioural | Observer | Test listeners (ITestListener in TestNG) |

---

## Q36. Explain Singleton pattern with thread-safe implementation.

Ensures only ONE instance of a class exists throughout the JVM.

```java
public class ConfigManager {
    private static volatile ConfigManager instance;
    private Properties props = new Properties();

    // Private constructor — prevents external instantiation
    private ConfigManager() {
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    // Thread-safe lazy initialization — double-checked locking
    public static ConfigManager getInstance() {
        if (instance == null) {                        // 1st check (no lock)
            synchronized (ConfigManager.class) {
                if (instance == null) {                // 2nd check (with lock)
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    public String get(String key) {
        return props.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}

// Usage — always same instance
String url = ConfigManager.getInstance().get("base.url");
String browser = ConfigManager.getInstance().get("browser", "chrome");

// Enum Singleton — simplest thread-safe singleton (Joshua Bloch recommended)
public enum DriverHolder {
    INSTANCE;
    private WebDriver driver;
    public WebDriver getDriver() { return driver; }
    public void setDriver(WebDriver d) { this.driver = d; }
}
```

---

## Q37. Explain Page Object Model as a design pattern.

POM separates test logic from UI interaction logic. Each page is represented by a Java class.

```java
// BasePage.java — abstract base for all pages
public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }

    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    public abstract boolean isLoaded();
}

// LoginPage.java
public class LoginPage extends BasePage {
    @FindBy(id = "username") private WebElement usernameField;
    @FindBy(id = "password") private WebElement passwordField;
    @FindBy(id = "loginBtn")  private WebElement loginButton;
    @FindBy(className = "error-msg") private WebElement errorMsg;

    public LoginPage(WebDriver driver) { super(driver); }

    public LoginPage enterUsername(String user) {
        usernameField.clear();
        usernameField.sendKeys(user);
        return this;  // fluent
    }

    public LoginPage enterPassword(String pass) {
        passwordField.clear();
        passwordField.sendKeys(pass);
        return this;
    }

    public DashboardPage clickLogin() {
        loginButton.click();
        return new DashboardPage(driver);
    }

    public String getErrorMessage() { return errorMsg.getText(); }

    @Override
    public boolean isLoaded() { return usernameField.isDisplayed(); }
}

// Test using POM — clean, readable
public class LoginTest extends BaseTest {
    @Test
    public void validLoginShouldGoToDashboard() {
        LoginPage loginPage = new LoginPage(driver);
        DashboardPage dashboard = loginPage
            .enterUsername("admin")
            .enterPassword("secret")
            .clickLogin();
        Assert.assertTrue(dashboard.isLoaded(), "Dashboard not loaded");
    }
}
```

---

## Q38. Explain Factory pattern with DriverFactory example.

Factory pattern delegates object creation to a factory method. Callers don't know which subclass is instantiated.

```java
public class DriverFactory {

    public static WebDriver createDriver(String browser) {
        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOpts = new ChromeOptions();
                chromeOpts.addArguments("--start-maximized", "--no-sandbox");
                return new ChromeDriver(chromeOpts);

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ffOpts = new FirefoxOptions();
                return new FirefoxDriver(ffOpts);

            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();

            case "chrome-headless":
                WebDriverManager.chromedriver().setup();
                ChromeOptions headless = new ChromeOptions();
                headless.addArguments("--headless", "--window-size=1920,1080");
                return new ChromeDriver(headless);

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }
}

// BaseTest.java — reads from config or system property
@BeforeMethod
public void setUp() {
    String browser = System.getProperty("browser", "chrome");
    WebDriver driver = DriverFactory.createDriver(browser);
    DriverManager.setDriver(driver);
}

// Run with: mvn test -Dbrowser=firefox
```

---

## Q39. Explain Builder pattern with test data example.

Builder pattern constructs complex objects step by step, especially when an object has many optional parameters.

```java
// TestUser.java — complex test data object
public class TestUser {
    private final String username;    // required
    private final String password;    // required
    private final String email;       // optional
    private final String role;        // optional
    private final boolean active;     // optional

    private TestUser(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.email    = builder.email;
        this.role     = builder.role;
        this.active   = builder.active;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail()    { return email; }
    public String getRole()     { return role; }
    public boolean isActive()   { return active; }

    // Static inner Builder class
    public static class Builder {
        private final String username;
        private final String password;
        private String email  = "default@test.com";
        private String role   = "user";
        private boolean active = true;

        public Builder(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public Builder email(String email) { this.email = email; return this; }
        public Builder role(String role)   { this.role = role;   return this; }
        public Builder active(boolean v)   { this.active = v;    return this; }

        public TestUser build() {
            return new TestUser(this);
        }
    }
}

// Usage — clear, readable, flexible
TestUser admin = new TestUser.Builder("admin", "Admin@123")
    .email("admin@company.com")
    .role("admin")
    .active(true)
    .build();

TestUser guest = new TestUser.Builder("guest", "Guest@123")
    .role("guest")
    .build();
```

---

## Q40. What is Maven? What is pom.xml?

Maven is a **build tool and dependency manager** for Java projects. `pom.xml` (Project Object Model) is its configuration file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <!-- Project coordinates -->
    <groupId>com.qoria</groupId>
    <artifactId>automation-framework</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <!-- Properties — reusable values -->
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <selenium.version>4.18.1</selenium.version>
        <testng.version>7.9.0</testng.version>
    </properties>

    <!-- Dependencies -->
    <dependencies>
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>${selenium.version}</version>
        </dependency>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <!-- Build configuration -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
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

## Q41. What are Maven scopes?

| Scope | Available at compile | Available at test | Packaged in JAR | Example |
|-------|:--------------------:|:-----------------:|:---------------:|---------|
| `compile` (default) | Yes | Yes | Yes | selenium-java |
| `test` | No | Yes | No | testng, junit |
| `provided` | Yes | Yes | No | servlet-api (server provides it) |
| `runtime` | No | Yes | Yes | JDBC driver |
| `system` | Yes | Yes | No | Local jar (use with caution) |

```xml
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <version>7.9.0</version>
    <scope>test</scope>       <!-- only in test classpath, not shipped -->
</dependency>

<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>4.18.1</version>
    <!-- scope compile — default, available everywhere -->
</dependency>
```

---

## Q42. What is Maven lifecycle phases?

The three main lifecycles: **default** (build), **clean**, **site**.

**Default lifecycle — key phases in order:**

```
validate → compile → test-compile → test → package → verify → install → deploy
```

```bash
# Common commands
mvn clean                  # delete target/ directory
mvn compile                # compile src/main/java
mvn test                   # compile + run tests
mvn package                # compile + test + create JAR/WAR
mvn install                # package + install to local ~/.m2 repo
mvn clean test             # clean then test
mvn test -Dtest=LoginTest  # run specific test class
mvn test -Dbrowser=firefox # pass system property

# Skip tests
mvn package -DskipTests    # package without running tests
mvn package -Dmaven.test.skip=true  # skip compilation of tests too
```

---

## Q43. What is the difference between int and Integer?

```java
// int — primitive, stored on stack, cannot be null
int a = 5;
int b = 10;
int sum = a + b;  // direct value operation, fast

// Integer — wrapper class, stored on heap, can be null
Integer x = 5;    // autoboxed
Integer y = null; // valid — no NPE yet

// NPE trap
Integer count = null;
int total = count + 5;  // NullPointerException — unboxing null

// Integer cache — values -128 to 127 are cached
Integer i1 = 127;
Integer i2 = 127;
System.out.println(i1 == i2);  // true — same cached object

Integer i3 = 200;
Integer i4 = 200;
System.out.println(i3 == i4);  // false — different objects
System.out.println(i3.equals(i4));  // true — same value

// Use Integer when:
// 1. Storing in collections (List<Integer>, Map<String, Integer>)
// 2. Value can be null (optional config timeout)
// 3. Using Integer utility methods
int max = Integer.MAX_VALUE;      // 2147483647
int min = Integer.MIN_VALUE;      // -2147483648
int parsed = Integer.parseInt("42");
String str = Integer.toString(42);
String hex = Integer.toHexString(255);  // "ff"
```

---

## Q44. What is generics? Why use it?

Generics allow classes, interfaces, and methods to operate on types specified as parameters, providing compile-time type safety.

```java
// Without generics — unsafe, requires casting
List rawList = new ArrayList();
rawList.add("hello");
rawList.add(42);  // no error at compile time
String s = (String) rawList.get(1);  // ClassCastException at runtime

// With generics — type-safe
List<String> typedList = new ArrayList<>();
typedList.add("hello");
// typedList.add(42);  // COMPILE ERROR — caught early

// Generic class
public class TestDataStore<T> {
    private List<T> data = new ArrayList<>();

    public void add(T item)  { data.add(item); }
    public T get(int index)  { return data.get(index); }
    public int size()        { return data.size(); }
}

TestDataStore<String> names = new TestDataStore<>();
TestDataStore<Integer> counts = new TestDataStore<>();

// Generic method
public static <T> List<T> filterList(List<T> list, Predicate<T> condition) {
    return list.stream().filter(condition).collect(Collectors.toList());
}

List<String> filteredTests = filterList(allTests, t -> t.startsWith("TC"));

// Bounded wildcards
public void processElements(List<? extends WebElement> elements) {
    // accepts List<WebElement>, List<WebInput>, etc.
    elements.forEach(e -> System.out.println(e.getText()));
}
```

---

## Q45. What is varargs?

Varargs (variable arguments) allows a method to accept zero or more arguments of a specified type.

```java
// Syntax: Type... paramName  (must be last parameter)
public static void logStep(String... steps) {
    for (String step : steps) {
        System.out.println("[STEP] " + step);
    }
}

logStep("Open browser");
logStep("Navigate to login", "Enter credentials");
logStep("Login", "Verify dashboard", "Logout", "Close browser");

// Varargs with other parameters
public void waitForAny(int timeoutSeconds, By... locators) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    for (By locator : locators) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return;  // found one — stop waiting
        } catch (TimeoutException ignored) {}
    }
}

waitForAny(10, By.id("success"), By.id("dashboard"), By.className("home"));

// Internally, varargs is just an array
public void print(String... args) {
    System.out.println(args.length);  // number of arguments
    System.out.println(args[0]);      // first argument
}
```

---

## Q46. What is the ternary operator?

A compact if-else expression: `condition ? valueIfTrue : valueIfFalse`

```java
// Syntax
String result = (1 > 0) ? "yes" : "no";

// Automation use cases
// 1. Setting browser based on config
String browser = System.getProperty("browser") != null
    ? System.getProperty("browser")
    : "chrome";

// 2. Headless mode from environment
boolean headless = "true".equals(System.getenv("HEADLESS"));
String mode = headless ? "headless" : "headed";

// 3. Reporting
String status = testPassed ? "PASS" : "FAIL";

// 4. Safe getAttribute
String value = element.getAttribute("value");
String display = (value != null && !value.isEmpty()) ? value : "(empty)";

// Nested ternary — avoid, hard to read
String priority = score > 90 ? "High" : score > 50 ? "Medium" : "Low";
// Better as if-else
```

---

## Q47. What is a static block and when does it run?

A static block runs **once** when the class is first loaded by the JVM, before any object is created or static method is called.

```java
public class DriverFactory {
    private static final String DEFAULT_BROWSER;
    private static final int DEFAULT_TIMEOUT;

    // Static block — runs ONCE when class is loaded
    static {
        System.out.println("DriverFactory class loading...");

        // Load from properties file
        Properties props = new Properties();
        try (InputStream is = DriverFactory.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            System.err.println("Could not load config.properties");
        }

        DEFAULT_BROWSER = props.getProperty("browser", "chrome");
        DEFAULT_TIMEOUT = Integer.parseInt(props.getProperty("timeout", "10"));
    }

    public static WebDriver createDriver() {
        return createDriver(DEFAULT_BROWSER);
    }
}

// Execution order within a class:
// 1. Static variables declared (left to right, top to bottom)
// 2. Static blocks run (in order of appearance)
// 3. Object created (constructor called)
```

---

## Q48. What is the difference between throw and throws?

```java
// throws — declares that a method MAY throw an exception (checked exceptions)
//          appears in method signature
public void readConfig(String path) throws IOException, FileNotFoundException {
    // method may throw these — caller must handle or declare
    FileReader fr = new FileReader(path);
}

// throw — actually THROWS an exception (creates and raises it)
//         appears inside method body
public WebDriver createDriver(String browser) {
    if (browser == null || browser.isEmpty()) {
        throw new IllegalArgumentException("Browser cannot be null or empty");
    }
    switch (browser) {
        case "chrome": return new ChromeDriver();
        default:
            throw new UnsupportedOperationException("Browser not supported: " + browser);
    }
}

// Both together
public void login(String user, String pass) throws TestDataException {
    if (user == null) {
        throw new TestDataException("Username cannot be null");  // throw
    }
    // ...
}
```

| | `throw` | `throws` |
|-|---------|----------|
| Purpose | Raise an exception | Declare possible exceptions |
| Location | Method body | Method signature |
| Used with | Single exception instance | Exception class names |

---

## Q49. What is NullPointerException and how to prevent it?

NullPointerException (NPE) occurs when you call a method or access a field on a null reference.

```java
// Common causes
WebElement el = driver.findElement(By.id("nonExistent")); // returns null? No — throws NoSuchElementException
// BUT:
String text = el.getText();   // NPE if el is null

String url = driver.getCurrentUrl();
if (url.contains("login")) { ... }  // NPE if url is null (rare but possible)

// Prevention strategies

// 1. Null checks
if (element != null) {
    element.click();
}

// 2. Optional
Optional.ofNullable(element).ifPresent(WebElement::click);

// 3. Null-safe method with guard
public String safeGetText(By locator) {
    List<WebElement> elements = driver.findElements(locator);
    return elements.isEmpty() ? "" : elements.get(0).getText();
}

// 4. Use Objects.requireNonNull for parameters
public LoginPage(WebDriver driver) {
    this.driver = Objects.requireNonNull(driver, "WebDriver cannot be null");
}

// 5. Never return null — return Optional or empty collection
public Optional<WebElement> findElement(By locator) {
    List<WebElement> els = driver.findElements(locator);
    return els.isEmpty() ? Optional.empty() : Optional.of(els.get(0));
}

// 6. Assign before use
String title = driver.getTitle();
if (title != null && title.contains("Dashboard")) { ... }

// Literal on the left avoids NPE
"Dashboard".equals(driver.getTitle());  // safe even if getTitle() returns null
```

---

## Q50. What is instanceof operator?

`instanceof` tests whether an object is an instance of a given class or interface.

```java
// Basic usage
WebDriver driver = new ChromeDriver();
System.out.println(driver instanceof ChromeDriver);      // true
System.out.println(driver instanceof WebDriver);         // true (interface)
System.out.println(driver instanceof TakesScreenshot);   // true (Chrome implements it)
System.out.println(driver instanceof FirefoxDriver);     // false

// Taking screenshot safely
if (driver instanceof TakesScreenshot) {
    File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    FileUtils.copyFile(screenshot, new File("screenshots/test.png"));
}

// JavaScript execution safely
if (driver instanceof JavascriptExecutor) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
}

// Java 16+ pattern matching — eliminates the cast
if (driver instanceof ChromeDriver chromeDriver) {
    // chromeDriver is already cast — no explicit (ChromeDriver) needed
    chromeDriver.executeCdpCommand("...", Map.of());
}
```

---

## Q51. What is the difference between Array and ArrayList?

```java
// Array — fixed size, primitive-friendly, fast
String[] browsers = new String[3];
browsers[0] = "chrome";
browsers[1] = "firefox";
browsers[2] = "edge";
// browsers[3] = "safari";  // ArrayIndexOutOfBoundsException

int[] timeouts = {5, 10, 15, 30};  // primitive array — no boxing overhead

// ArrayList — dynamic size, only objects, flexible
List<String> browserList = new ArrayList<>();
browserList.add("chrome");
browserList.add("firefox");
browserList.add("edge");
browserList.add("safari");    // no size limit
browserList.remove("edge");   // easy removal
browserList.size();           // current size
browserList.get(0);           // random access

// Convert between them
String[] arr = {"a", "b", "c"};
List<String> list = new ArrayList<>(Arrays.asList(arr));
String[] backToArr = list.toArray(new String[0]);
```

| Feature | Array | ArrayList |
|---------|-------|-----------|
| Size | Fixed | Dynamic |
| Primitives | Yes | No (auto-boxing) |
| Performance | Faster | Slightly slower |
| Methods | length field only | add, remove, contains, sort, etc. |

---

## Q52. How do you read a properties file in Java?

```java
// Method 1 — Properties class (most common)
public class ConfigReader {
    private static Properties props = new Properties();

    static {
        try (InputStream is = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is == null) throw new RuntimeException("config.properties not found in resources");
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}

// config.properties (in src/test/resources/)
// base.url=https://staging.example.com
// browser=chrome
// implicit.wait=10

// Usage
String baseUrl = ConfigReader.get("base.url");
String browser = ConfigReader.get("browser", "chrome");
int timeout = Integer.parseInt(ConfigReader.get("implicit.wait", "10"));

// Method 2 — System.getProperty() (override at runtime)
// mvn test -Dbrowser=firefox
String browser = System.getProperty("browser", ConfigReader.get("browser", "chrome"));
```

---

## Q53. How do you read a CSV file for data-driven testing?

```java
// Method 1 — Basic BufferedReader (no library)
public static List<String[]> readCsv(String fileName) throws IOException {
    List<String[]> data = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(
            new FileReader("src/test/resources/" + fileName))) {
        String line;
        br.readLine();  // skip header row
        while ((line = br.readLine()) != null) {
            String[] row = line.split(",");
            data.add(row);
        }
    }
    return data;
}

// Usage
List<String[]> users = readCsv("test-users.csv");
for (String[] row : users) {
    String username = row[0];
    String password = row[1];
    // use in test...
}

// Method 2 — TestNG DataProvider from CSV
@DataProvider(name = "loginData")
public Object[][] loginDataProvider() throws IOException {
    List<String[]> rows = readCsv("login-data.csv");
    Object[][] data = new Object[rows.size()][2];
    for (int i = 0; i < rows.size(); i++) {
        data[i][0] = rows.get(i)[0];  // username
        data[i][1] = rows.get(i)[1];  // password
    }
    return data;
}

@Test(dataProvider = "loginData")
public void loginTest(String username, String password) {
    new LoginPage(driver).login(username, password);
}

// Method 3 — Apache Commons CSV (handles quoted commas, etc.)
// Dependency: commons-csv
try (Reader reader = new FileReader("src/test/resources/data.csv");
     CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
    for (CSVRecord record : parser) {
        String user = record.get("username");
        String pass = record.get("password");
    }
}
```

---

## Q54. What is reflection? How does TestNG use it?

Reflection allows inspecting and invoking classes, methods, and fields at runtime, without knowing them at compile time.

```java
// Basic reflection
Class<?> clazz = Class.forName("com.qoria.pages.LoginPage");
Object page = clazz.getConstructor(WebDriver.class).newInstance(driver);
Method loginMethod = clazz.getMethod("login", String.class, String.class);
loginMethod.invoke(page, "admin", "password");

// How TestNG uses reflection internally
// 1. Scans test class for @Test annotated methods
// 2. Creates an instance of the class
// 3. Calls @BeforeMethod methods via reflection
// 4. Calls @Test method via reflection
// 5. Calls @AfterMethod methods via reflection

// Example — your own test runner using reflection
public void runAllTests(Class<?> testClass) throws Exception {
    Object instance = testClass.getDeclaredConstructor().newInstance();

    for (Method method : testClass.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Test.class)) {
            System.out.println("Running: " + method.getName());
            method.invoke(instance);
        }
    }
}

// Reading annotation values via reflection
Test annotation = method.getAnnotation(Test.class);
System.out.println("Priority: " + annotation.priority());
System.out.println("Groups: " + Arrays.toString(annotation.groups()));
```

---

## Q55. What is Collections class? Key methods?

`java.util.Collections` is a utility class with static methods for operating on collections.

```java
List<String> tests = new ArrayList<>(Arrays.asList("TC03", "TC01", "TC02", "TC01"));

// Sort
Collections.sort(tests);                               // TC01, TC01, TC02, TC03
Collections.sort(tests, Collections.reverseOrder());   // TC03, TC02, TC01, TC01

// Search
int idx = Collections.binarySearch(tests, "TC02");    // needs sorted list

// Shuffle — randomize order
Collections.shuffle(tests);
Collections.shuffle(tests, new Random(42));  // seeded — reproducible

// Min / Max
String min = Collections.min(tests);
String max = Collections.max(tests);

// Frequency
int count = Collections.frequency(tests, "TC01");  // 2

// Reverse
Collections.reverse(tests);

// Fill — replace all elements
Collections.fill(tests, "PENDING");

// Unmodifiable view — prevent modification
List<String> immutable = Collections.unmodifiableList(tests);
// immutable.add("X");  // UnsupportedOperationException

// Singleton — single-element immutable collection
List<String> single = Collections.singletonList("onlyOne");

// Empty collections
List<String> empty = Collections.emptyList();
Map<String, String> emptyMap = Collections.emptyMap();

// nCopies
List<String> repeated = Collections.nCopies(5, "chrome");
// [chrome, chrome, chrome, chrome, chrome]
```

---

## Q56. What is Comparable vs Comparator?

### Comparable — natural ordering, implemented by the class itself

```java
public class TestResult implements Comparable<TestResult> {
    private String testName;
    private int duration;

    @Override
    public int compareTo(TestResult other) {
        // Sort by duration ascending
        return Integer.compare(this.duration, other.duration);
    }
}

List<TestResult> results = new ArrayList<>();
Collections.sort(results);  // uses compareTo — natural order
```

### Comparator — external ordering, defined outside the class

```java
// Sort TestResult by testName (external rule)
Comparator<TestResult> byName = (r1, r2) -> r1.getTestName().compareTo(r2.getTestName());

// Sort by duration descending
Comparator<TestResult> byDurationDesc = Comparator.comparingInt(TestResult::getDuration).reversed();

// Chain comparators — sort by status, then by name
Comparator<TestResult> combined = Comparator
    .comparing(TestResult::getStatus)
    .thenComparing(TestResult::getTestName);

Collections.sort(results, byName);
results.sort(byDurationDesc);  // Java 8 List.sort
```

| Feature | Comparable | Comparator |
|---------|------------|------------|
| Package | java.lang | java.util |
| Method | `compareTo(T o)` | `compare(T o1, T o2)` |
| Where | Inside the class | Separate class or lambda |
| Usage | `Collections.sort(list)` | `Collections.sort(list, comparator)` |
| Flexibility | One natural order | Multiple orderings |

---

## Q57. What are text blocks (Java 15)?

Text blocks provide a clean way to write multi-line strings without concatenation and escape characters.

```java
// Old way — messy
String json = "{\n" +
    "  \"username\": \"admin\",\n" +
    "  \"password\": \"secret\"\n" +
    "}";

// Text block (Java 15+)
String json = """
    {
      "username": "admin",
      "password": "secret"
    }
    """;

// XPath in text block — no more escaping quotes
String xpath = """
    //div[@class='container']
      /following-sibling::span[@data-id='result']
    """;

// SQL query
String query = """
    SELECT user_id, username, email
    FROM users
    WHERE active = true
    AND role = 'admin'
    ORDER BY username
    """;

// HTML for assertions
String expectedHtml = """
    <div class="success">
        Login successful
    </div>
    """;
```

---

## Q58. What is the var keyword (Java 10)?

`var` lets the compiler infer the local variable type — it is NOT dynamic typing; the type is fixed at compile time.

```java
// Old way — redundant type on the left
ArrayList<String> names = new ArrayList<String>();
HashMap<String, List<TestResult>> results = new HashMap<String, List<TestResult>>();

// With var — type inferred, less verbose
var names   = new ArrayList<String>();      // still ArrayList<String>
var results = new HashMap<String, List<TestResult>>();

// Automation usage
var driver  = new ChromeDriver();           // inferred as ChromeDriver
var wait    = new WebDriverWait(driver, Duration.ofSeconds(10));
var options = new ChromeOptions();

// Loop variables
var elements = driver.findElements(By.tagName("a"));
for (var el : elements) {
    System.out.println(el.getText());
}

// Limitations
// var x;          // ERROR — must be initialized (cannot infer from nothing)
// var x = null;   // ERROR — cannot infer from null
// var can only be used for LOCAL variables — not fields, parameters, return types
```

---

## Q59. What is record class (Java 14)?

A `record` is a concise way to declare immutable data carrier classes. The compiler auto-generates constructor, getters, equals, hashCode, and toString.

```java
// Old POJO — verbose
public final class TestData {
    private final String username;
    private final String password;
    private final String env;

    public TestData(String username, String password, String env) {
        this.username = username;
        this.password = password;
        this.env = env;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEnv()      { return env; }
    // + equals, hashCode, toString...
}

// RECORD — equivalent, one line
public record TestData(String username, String password, String env) {}

// Usage
TestData data = new TestData("admin", "Admin@123", "staging");
System.out.println(data.username());  // getter — no "get" prefix
System.out.println(data.env());
System.out.println(data);  // auto toString: TestData[username=admin, password=Admin@123, env=staging]

// Compact constructor — add validation
public record TestData(String username, String password, String env) {
    public TestData {  // compact constructor
        Objects.requireNonNull(username, "username cannot be null");
        if (password.length() < 6) throw new IllegalArgumentException("password too short");
    }
}
```

---

## Q60. What are the top 10 common Java mistakes in automation code?

### Mistake 1 — Using == instead of equals() for String comparison

```java
// WRONG
if (driver.getTitle() == "Dashboard") { ... }
// CORRECT
if ("Dashboard".equals(driver.getTitle())) { ... }
```

### Mistake 2 — Not closing WebDriver (missing quit in finally/AfterMethod)

```java
// CORRECT — always quit in @AfterMethod
@AfterMethod
public void tearDown() {
    if (DriverManager.getDriver() != null) {
        DriverManager.getDriver().quit();
        DriverManager.removeDriver();
    }
}
```

### Mistake 3 — Using Thread.sleep instead of explicit waits

```java
// WRONG — brittle, wastes time
Thread.sleep(3000);
// CORRECT
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));
```

### Mistake 4 — Not using ThreadLocal for parallel tests

```java
// WRONG — shared static driver breaks parallel tests
public static WebDriver driver;
// CORRECT — ThreadLocal per thread
private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
```

### Mistake 5 — Catching too broad exceptions

```java
// WRONG — swallows all exceptions silently
try { driver.findElement(By.id("x")).click(); }
catch (Exception e) {}  // never do this
// CORRECT
catch (NoSuchElementException e) {
    Assert.fail("Element not found: " + e.getMessage());
}
```

### Mistake 6 — Concatenating Strings in loops

```java
// WRONG — creates many String objects
String result = "";
for (String step : steps) { result += step + ", "; }
// CORRECT
StringBuilder sb = new StringBuilder();
for (String step : steps) { sb.append(step).append(", "); }
String result = sb.toString();
```

### Mistake 7 — Not removing ThreadLocal causing memory leaks

```java
// WRONG — ThreadLocal value stays in thread's map forever
driverThreadLocal.set(driver);
// CORRECT — always remove after use
try { /* test */ } finally { driverThreadLocal.remove(); }
```

### Mistake 8 — Using raw types with generics

```java
// WRONG — no type safety
List testData = new ArrayList();
// CORRECT
List<String[]> testData = new ArrayList<>();
```

### Mistake 9 — Hardcoding waits and selectors

```java
// WRONG
driver.findElement(By.xpath("//div[3]/table/tbody/tr[1]/td[2]"));
// CORRECT — data-testid or meaningful attributes
driver.findElement(By.cssSelector("[data-testid='user-name']"));
```

### Mistake 10 — NullPointerException from getAttribute()

```java
// WRONG — getAttribute returns null if attribute doesn't exist
String value = element.getAttribute("value").toLowerCase();  // NPE
// CORRECT
String value = element.getAttribute("value");
String lower = (value != null) ? value.toLowerCase() : "";
```

---

*End of Part 3 — Core Java | 60 Questions*
