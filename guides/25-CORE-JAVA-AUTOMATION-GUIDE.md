# Core Java & OOP — Full Interview Q&A Guide
## Java Fundamentals + OOP + Collections + Design Patterns + Maven | Senior QA Interview Preparation

---

## SECTION 1 — OOP: FOUR PILLARS

---

**Q1: What are the four pillars of OOP? Explain each with an automation-specific example.**

**A:**

**1. Encapsulation — hide data, expose through controlled methods**

The idea is to make fields `private` and provide public methods that control how those fields are accessed or changed. This prevents external code from putting the object into an invalid state.

```java
// BAD: public fields — anyone can set usernameField to null, breaking other methods
public class LoginPage {
    public WebElement usernameField;   // external code can corrupt this
    public WebElement passwordField;
}

// GOOD: private fields + public actions
public class LoginPage {
    private final WebElement usernameField;
    private final WebElement passwordField;
    private final WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.usernameField = driver.findElement(By.id("username"));
        this.passwordField = driver.findElement(By.id("password"));
        this.loginButton   = driver.findElement(By.id("loginBtn"));
    }

    public DashboardPage login(String username, String password) {
        usernameField.clear();
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
        return new DashboardPage(((RemoteWebDriver) ((WrapsDriver) usernameField).getWrappedDriver()));
    }
    // Tests call login(), never touch usernameField directly
    // If the locator changes, only LoginPage changes — not 50 test methods
}
```

**2. Inheritance — child class reuses parent class behaviour**

```java
// BaseTest — common setup that every test class needs
public class BaseTest {
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = DriverFactory.createDriver(System.getProperty("browser", "chrome"));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}

// LoginTest inherits setUp() and tearDown() automatically
public class LoginTest extends BaseTest {
    @Test
    public void validLogin_navigatesToDashboard() {
        driver.get("https://app.example.com/login");   // driver provided by BaseTest
        new LoginPage(driver).login("admin", "Admin@123");
        assertTrue(new DashboardPage(driver).isLoaded());
    }
}
```

**3. Polymorphism — same method call, different behaviour based on actual object type**

```java
// WebDriver is an interface. ChromeDriver, FirefoxDriver, EdgeDriver all implement it.
WebDriver driver = new ChromeDriver();     // ChromeDriver stored as WebDriver type
driver.get("https://app.com");            // calls ChromeDriver's implementation

driver = new FirefoxDriver();             // same variable, different implementation
driver.get("https://app.com");            // calls FirefoxDriver's implementation

// Test code never changes. Only the concrete driver type changes.
// This is runtime polymorphism — the JVM decides which get() to call based on actual object.

// Also applies to page objects:
BasePage currentPage = new LoginPage(driver);    // LoginPage IS-A BasePage
currentPage.navigateTo("/login");                // calls inherited method

currentPage = new DashboardPage(driver);         // same reference, different object
currentPage.isLoaded();                          // calls DashboardPage's isLoaded()
```

**4. Abstraction — hide complexity, expose only what matters**

```java
// Abstract class hides HOW things work internally
public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Abstract: every page must define its own isLoaded() condition
    public abstract boolean isLoaded();

    // Concrete: shared navigation method — tests call this without knowing the implementation
    public void navigateTo(String path) {
        driver.get(System.getProperty("base.url", "https://app.example.com") + path);
        wait.until(d -> ((JavascriptExecutor) d)
            .executeScript("return document.readyState").equals("complete"));
    }

    // Concrete: shared element wait — tests don't worry about timing details
    protected WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}

public class LoginPage extends BasePage {
    public LoginPage(WebDriver driver) { super(driver); }

    @Override
    public boolean isLoaded() {
        return driver.getCurrentUrl().endsWith("/login");
    }
}
```

---

**Q2: What is the difference between an interface and an abstract class? When do you use each?**

**A:**

```java
// INTERFACE — defines WHAT (a contract/capability)
// Java 8+: can have default and static methods
// Java 9+: can have private methods
public interface Reportable {
    void logStep(String message);           // abstract — must implement
    void attachScreenshot(byte[] bytes);    // abstract

    default void logPass(String message) {  // default — optional override
        logStep("[PASS] " + message);
    }

    default void logFail(String message) {  // default
        logStep("[FAIL] " + message);
    }

    static boolean isCI() {                 // static utility
        return System.getenv("CI") != null;
    }
}

// ABSTRACT CLASS — defines WHAT + some HOW (partial implementation)
public abstract class BasePage {
    protected WebDriver driver;     // can have instance fields
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {   // can have constructor
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public abstract boolean isLoaded();   // must implement

    public void waitForPageLoad() {       // concrete — all pages inherit this
        wait.until(d -> ((JavascriptExecutor) d)
            .executeScript("return document.readyState").equals("complete"));
    }

    protected WebElement waitFor(By by) { // shared wait logic
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }
}

// A class can EXTEND only one abstract class but IMPLEMENT many interfaces
public class LoginPage extends BasePage implements Reportable, Serializable {
    public LoginPage(WebDriver driver) { super(driver); }

    @Override public boolean isLoaded() { return driver.getCurrentUrl().contains("/login"); }
    @Override public void logStep(String msg) { System.out.println(msg); }
    @Override public void attachScreenshot(byte[] bytes) { Allure.addAttachment("SS", "image/png", ...); }
}
```

| Feature | Interface | Abstract Class |
|---------|-----------|----------------|
| Fields | Only `public static final` constants | Any access modifier |
| Constructor | No constructor | Yes, can have constructor |
| Methods | Abstract + default + static | Abstract + concrete |
| Multiple inheritance | Implement many | Extend one only |
| Use when | Defining a capability/contract | Sharing code between related classes |
| Automation examples | `WebDriver`, `ITestListener`, `Comparable` | `BasePage`, `BaseTest`, `BaseApiTest` |

**Rule of thumb**: if two things share code (BasePage ← LoginPage ← are both pages), use abstract class. If two unrelated things share a capability (LoginPage and CheckoutPage both need to log steps), use interface.

---

**Q3: Can you instantiate an abstract class?**

**A:**

No, you cannot directly instantiate an abstract class using `new AbstractClassName()` — the compiler prevents it. An abstract class is incomplete by design; it has abstract methods with no body, so creating an instance would leave those methods undefined.

```java
// DIRECT INSTANTIATION — compile error
BasePage page = new BasePage(driver);   // compile error: BasePage is abstract

// ALLOWED: use a concrete subclass
BasePage page = new LoginPage(driver);  // OK — LoginPage extends BasePage and implements all abstract methods

// ALLOWED: anonymous class (one-time use, no named class)
BasePage anonymousPage = new BasePage(driver) {
    @Override
    public boolean isLoaded() {
        return true;  // provide the body inline
    }
};
// Anonymous classes are useful in tests when you need a quick one-off implementation

// ALLOWED: use as a reference type (polymorphism)
BasePage page;                          // variable declaration — no instantiation yet
page = new LoginPage(driver);           // assign a concrete implementation
page = new DashboardPage(driver);       // reassign to a different implementation
```

Why abstract classes exist: they give you the benefits of a base class (shared constructor, shared concrete methods, shared fields) while enforcing that subclasses provide the missing pieces (abstract methods). If you could instantiate an abstract class, calling an abstract method on it would have no body to execute.

---

**Q4: What is constructor chaining with `this()` and `super()`?**

**A:**

Constructor chaining means one constructor calls another constructor — either in the same class (`this()`) or in the parent class (`super()`). The call must be the first line of the constructor body.

```java
// Constructor chaining within the same class using this()
public class ApiConfig {
    private String baseUrl;
    private String authToken;
    private int    timeoutSeconds;
    private String environment;

    // Full constructor — all 4 parameters
    public ApiConfig(String baseUrl, String authToken, int timeoutSeconds, String environment) {
        this.baseUrl         = baseUrl;
        this.authToken       = authToken;
        this.timeoutSeconds  = timeoutSeconds;
        this.environment     = environment;
    }

    // 3-parameter — chains to full constructor with default environment
    public ApiConfig(String baseUrl, String authToken, int timeoutSeconds) {
        this(baseUrl, authToken, timeoutSeconds, "staging");  // this() — first line
    }

    // 2-parameter — chains to 3-parameter with default timeout
    public ApiConfig(String baseUrl, String authToken) {
        this(baseUrl, authToken, 30);
    }

    // No-arg — chains to 2-parameter with all defaults
    public ApiConfig() {
        this(System.getProperty("base.url", "https://api.example.com"),
             System.getenv("TEST_AUTH_TOKEN"));
    }
}

// Usage — each constructor provides sensible defaults
ApiConfig config1 = new ApiConfig();
ApiConfig config2 = new ApiConfig("https://staging.api.com", "token123");
ApiConfig config3 = new ApiConfig("https://prod.api.com", "token456", 60, "production");
```

```java
// super() — call parent class constructor from child class
public class BaseTest {
    protected WebDriver driver;
    protected String    environment;

    public BaseTest(String environment) {
        this.environment = environment;
        System.out.println("BaseTest init for environment: " + environment);
    }
}

public class LoginTest extends BaseTest {
    private LoginPage loginPage;

    public LoginTest() {
        super("staging");   // calls BaseTest("staging") — must be first line
        // now this.environment = "staging" from parent
    }
}
```

---

**Q5: What is the difference between `this` and `super` keywords?**

**A:**

```java
public class Animal {
    protected String name;
    protected String species;

    public Animal(String name, String species) {
        this.name    = name;     // 'this.name' distinguishes field from parameter
        this.species = species;
    }

    public String describe() {
        return species + " named " + name;
    }

    public void speak() {
        System.out.println("...");
    }
}

public class Dog extends Animal {
    private String breed;

    public Dog(String name, String breed) {
        super(name, "Dog");  // calls Animal(name, species) — must be FIRST line
        this.breed = breed;  // sets Dog's own field
    }

    @Override
    public String describe() {
        // super.describe() calls the parent's version, then we add to it
        return super.describe() + " (" + breed + ")";
    }

    @Override
    public void speak() {
        super.speak();           // call parent's speak() first
        System.out.println("Woof!");  // then add own behaviour
    }

    public Dog getSelf() {
        return this;   // 'this' returns the current instance
    }

    public void printInfo() {
        System.out.println(this.name);   // 'this.name' is the same as 'name' (unambiguous here)
        System.out.println(this.breed);  // 'this' clarifies it's the instance field, not a local var
    }
}

// Usage
Dog d = new Dog("Rex", "Labrador");
d.describe();  // "Dog named Rex (Labrador)"
d.speak();     // "..." followed by "Woof!"
```

Summary:
- `this` — refers to the current object instance. Used to distinguish field from parameter (`this.name = name`), call another constructor in the same class (`this(args)`), or pass the current object as an argument (`someMethod(this)`).
- `super` — refers to the parent class. Used to call the parent's constructor (`super(args)`) or the parent's overridden method (`super.methodName()`).

---

## SECTION 2 — CORE JAVA CONCEPTS

---

**Q6: What is the difference between String, StringBuilder, and StringBuffer?**

**A:**

```java
// STRING — IMMUTABLE
// Every operation that appears to modify a String actually creates a new String object.
String s = "Hello";
s.concat(" World");  // returns new String "Hello World" but s is STILL "Hello"
s = s.concat(" World");  // now s points to the new String "Hello World"

// This is a memory problem in loops:
String result = "";
for (int i = 0; i < 10_000; i++) {
    result = result + i;
    // Each iteration: creates new String, old one becomes garbage
    // 10,000 iterations → ~10,000 temporary String objects → pressure on GC
}

// STRINGBUILDER — MUTABLE, NOT thread-safe, FASTEST
// Modifies the same object in place — no new objects created per operation.
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10_000; i++) {
    sb.append(i);   // modifies sb in place — one object throughout the loop
}
String result = sb.toString();  // convert to String at the end

// StringBuilder key methods:
sb.append("text");          // add to end
sb.insert(3, "text");       // insert at position
sb.delete(1, 4);            // delete from index 1 to 4 (exclusive)
sb.deleteCharAt(2);         // delete single character
sb.replace(1, 3, "XY");     // replace range with new string
sb.reverse();               // reverse content
sb.length();                // current length
sb.charAt(0);               // character at index
sb.indexOf("text");         // find substring

// STRINGBUFFER — MUTABLE, thread-safe (synchronized), SLOWER than StringBuilder
StringBuffer sbuf = new StringBuffer();
// Use ONLY when multiple threads modify the same string builder instance.
// In practice, this is rare — usually you give each thread its own StringBuilder.

// Decision rule:
// Fixed string value → String
// Building a string in a single thread → StringBuilder (almost always)
// Building a string shared across threads → StringBuffer

// Automation example: build a JSON payload in a loop
StringBuilder payload = new StringBuilder("{\"items\":[");
for (String sku : skuList) {
    payload.append("{\"sku\":\"").append(sku).append("\"},");
}
payload.deleteCharAt(payload.length() - 1);  // remove trailing comma
payload.append("]}");
String json = payload.toString();
```

---

**Q7: What is the difference between `==`, `.equals()`, and `.compareTo()`?**

**A:**

```java
String a = new String("hello");
String b = new String("hello");
String c = a;

// == compares REFERENCES (memory addresses)
System.out.println(a == b);     // false — a and b are different objects in heap
System.out.println(a == c);     // true  — c points to same object as a

// String pool exception: literal strings are interned
String x = "hello";
String y = "hello";
System.out.println(x == y);     // true — both point to same interned literal
// BUT: NEVER rely on this. Always use .equals() for String comparison.

// .equals() compares CONTENT
System.out.println(a.equals(b));              // true
System.out.println(a.equals("hello"));        // true
System.out.println("HELLO".equalsIgnoreCase("hello"));  // true — case insensitive

// .compareTo() — lexicographic comparison, used for SORTING
// Returns: negative (this < other), 0 (equal), positive (this > other)
"apple".compareTo("banana");    // negative — 'a' < 'b'
"banana".compareTo("apple");    // positive — 'b' > 'a'
"apple".compareTo("apple");     // 0 — equal

// Custom sorting using compareTo
List<String> browsers = Arrays.asList("firefox", "chrome", "safari", "edge");
browsers.sort(String::compareTo);  // alphabetical: [chrome, edge, firefox, safari]

// In automation: always use .equals() for assertions
String actualTitle = driver.getTitle();
// WRONG: if (actualTitle == "Dashboard") — almost always false even if content matches
// CORRECT:
assertTrue(actualTitle.equals("Dashboard"));
Assert.assertEquals(actualTitle, "Dashboard");  // TestNG assertEquals uses .equals() internally
```

---

**Q8: What is the difference between `final`, `finally`, and `finalize()`?**

**A:**

These three keywords sound similar but have completely different purposes:

```java
// FINAL — a modifier with three different uses:

// 1. final variable — cannot be reassigned after initialisation
final int MAX_RETRIES = 3;
// MAX_RETRIES = 5;  → compile error

// 2. final method — cannot be overridden by a subclass
public class BaseTest {
    public final void initializeDriver() {   // child classes cannot override this
        // this setup must happen exactly this way for all subclasses
    }
}

// 3. final class — cannot be extended
public final class ApiConstants {
    public static final String BASE_URL   = "https://api.example.com";
    public static final String AUTH_TOKEN = System.getenv("TEST_AUTH_TOKEN");
    private ApiConstants() {}   // utility class, no instance needed
}
// Trying to extend ApiConstants → compile error

// FINALLY — a block in try-catch that ALWAYS executes
// (even if an exception is thrown, even if a return statement is reached)
@AfterMethod
public void tearDown() {
    try {
        performComplexCleanup();   // might throw an exception
        deleteTestData();
    } catch (Exception e) {
        System.err.println("Cleanup step failed: " + e.getMessage());
        // do NOT rethrow — we still want driver.quit() to run
    } finally {
        // This ALWAYS runs — the browser ALWAYS gets closed
        if (driver != null) {
            driver.quit();   // perfect place for resource cleanup
        }
    }
}

// FINALIZE() — a method that the GC calls before collecting an object
// DEPRECATED since Java 9, REMOVED in Java 18.
// DO NOT USE. GC timing is completely unpredictable.
// Use try-with-resources or finally blocks for cleanup instead.
@Deprecated
@Override
protected void finalize() throws Throwable {
    // Empty — do NOT implement this. Use try-with-resources instead.
}
```

---

**Q9: What is autoboxing? What are its pitfalls?**

**A:**

Autoboxing is Java's automatic conversion between primitive types and their wrapper class equivalents. Unboxing is the reverse.

```java
// AUTOBOXING — primitive → wrapper (automatic)
int primitive = 42;
Integer boxed = primitive;          // Java inserts: Integer.valueOf(42)

List<Integer> list = new ArrayList<>();
list.add(5);                        // autoboxed: list.add(Integer.valueOf(5))
list.add(10);
list.add(15);

// UNBOXING — wrapper → primitive (automatic)
int sum = 0;
for (Integer n : list) {
    sum += n;   // each n is unboxed: n.intValue()
}
```

Three common pitfalls:

```java
// PITFALL 1: NullPointerException when unboxing null
Integer value = null;            // perfectly valid wrapper — null is allowed
int x = value;                   // NullPointerException! Cannot unbox null to primitive

// Fix: null check before unboxing
if (value != null) int x = value;
int x = (value != null) ? value : 0;

// PITFALL 2: == comparison on cached Integers
// Java caches Integer objects for values -128 to 127 (the "Integer cache")
Integer a = 127;
Integer b = 127;
System.out.println(a == b);   // true — same cached object

Integer c = 128;
Integer d = 128;
System.out.println(c == d);   // false! — outside cache range, different objects
System.out.println(c.equals(d));  // true — always use .equals()

// PITFALL 3: Performance in large loops
Long total = 0L;                     // Long (wrapper, not primitive)
for (long i = 0; i < 1_000_000; i++) {
    total = total + i;               // unbox total, add i, box result back
    // 1,000,000 boxing/unboxing operations → significant overhead
}
// Fix: use primitive long instead
long total = 0L;
for (long i = 0; i < 1_000_000; i++) {
    total = total + i;   // pure primitive arithmetic — no boxing
}
```

---

**Q10: What is `throw` vs `throws`? What is exception chaining?**

**A:**

```java
// 'throws' — DECLARES that a method might throw checked exceptions
// Required by the compiler for checked exceptions
public void loadConfigFile(String path) throws IOException, FileNotFoundException {
    // If you don't declare throws IOException here, the compiler gives an error
    FileReader reader = new FileReader(path);  // FileNotFoundException is checked
}

// 'throw' — THROWS an exception right now (runtime action)
public String getBaseUrl() {
    String url = System.getProperty("base.url");
    if (url == null || url.isEmpty()) {
        throw new IllegalArgumentException("base.url system property is required");
        // IllegalArgumentException is unchecked — no 'throws' declaration needed
    }
    return url;
}

// EXCEPTION CHAINING — preserving the original cause when wrapping exceptions
// BAD — original cause is lost, debugging is much harder
try {
    spannerHelper.connect();
} catch (SpannerException e) {
    throw new RuntimeException("Spanner connection failed");
    // Stack trace: RuntimeException — but WHY? No information from SpannerException.
}

// GOOD — chain the original exception as the cause
try {
    spannerHelper.connect();
} catch (SpannerException e) {
    throw new RuntimeException("Spanner connection failed during test setup", e);
    // e is stored as the "cause". Stack trace shows BOTH exceptions and the original message.
}

// Reading the cause:
try {
    runSetup();
} catch (RuntimeException e) {
    System.out.println("Outer: " + e.getMessage());
    System.out.println("Cause: " + e.getCause().getMessage());
    e.printStackTrace();  // prints full chain
}

// Custom test exception with chaining
public class TestDataException extends RuntimeException {
    public TestDataException(String message) {
        super(message);
    }
    public TestDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Usage
try {
    insertTestRecord(data);
} catch (SpannerException e) {
    throw new TestDataException("Failed to insert test order: " + data.getOrderId(), e);
}
```

---

**Q11: What is the static block execution order?**

**A:**

```java
public class Demo {
    // Static variable — initialised when class is loaded
    private static int count;

    // Static block — runs ONCE when the class is first loaded by the JVM
    // Runs BEFORE any constructor, BEFORE main(), BEFORE any static method call
    static {
        System.out.println("1. Static block runs");
        count = 10;
    }

    // Instance block — runs BEFORE constructor, every time an object is created
    {
        System.out.println("2. Instance block runs");
    }

    // Constructor — runs after instance block
    public Demo() {
        System.out.println("3. Constructor runs");
    }

    public static void main(String[] args) {
        System.out.println("0. main() starts");  // but static block already ran before this line
        Demo d1 = new Demo();
        // Output: 1, 0, 2, 3 — static block ran at class loading, before main printed "0"
        Demo d2 = new Demo();
        // Output: 2, 3 — static block does NOT run again
    }
}
// Actual execution order: static block (once) → main → instance block → constructor (each new)

// Real use case in automation: load config once at class startup
public class ApiConstants {
    public static final String BASE_URL;
    public static final String AUTH_TOKEN;

    static {
        Properties props = new Properties();
        try {
            props.load(ApiConstants.class.getResourceAsStream("/config.properties"));
            BASE_URL   = props.getProperty("base.url", "https://api.example.com");
            AUTH_TOKEN = props.getProperty("auth.token", "");
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Cannot load config.properties: " + e.getMessage());
        }
    }
    // All constants are final and loaded exactly once
}
```

---

**Q12: What is `varargs` and when do you use it?**

**A:**

Varargs (variable-length arguments) allow a method to accept zero or more arguments of the same type. Inside the method, they are treated as an array. The parameter must be the last in the method signature.

```java
// Syntax: type... paramName
public void logApiStep(String level, String... messages) {
    for (String msg : messages) {
        System.out.printf("[%s] %s%n", level, msg);
    }
}

// Can call with zero, one, or many arguments
logApiStep("INFO");                                         // zero messages — valid
logApiStep("INFO", "Sending POST /api/orders");             // one message
logApiStep("ERROR", "Status was 500", "Body: " + body,     // multiple messages
           "Retrying in 2s");

// Also accepts an array
String[] msgs = {"step1", "step2", "step3"};
logApiStep("DEBUG", msgs);

// Practical automation example: building test description
public String formatTestName(String testId, String... tags) {
    String tagStr = String.join(", ", tags);
    return tagStr.isEmpty() ? testId : testId + " [" + tagStr + "]";
}

String name1 = formatTestName("TC001");                        // "TC001"
String name2 = formatTestName("TC002", "smoke");               // "TC002 [smoke]"
String name3 = formatTestName("TC003", "smoke", "regression"); // "TC003 [smoke, regression]"

// Common examples in Java standard library:
System.out.printf("Name: %s, Age: %d", name, age);  // printf uses varargs
String.format("URL: %s, Status: %d", url, status);  // format uses varargs
Arrays.asList("a", "b", "c");                        // asList uses varargs
```

---

## SECTION 3 — COLLECTIONS

---

**Q13: What is the difference between ArrayList, LinkedList, and Vector?**

**A:**

```java
// ARRAYLIST — backed by a resizable array
// Fast random access: O(1) — direct index calculation
// Slow insert/delete in middle: O(n) — must shift subsequent elements
// NOT thread-safe
List<String> al = new ArrayList<>();
al.add("chrome");     // O(1) amortised (may trigger resize)
al.get(5);            // O(1) — direct array access
al.remove(2);         // O(n) — shifts elements 3,4,5... left by one
// Use ArrayList for: most cases, especially when reads outnumber writes

// LINKEDLIST — doubly linked list of nodes
// Slow random access: O(n) — must traverse from head or tail
// Fast insert/delete at head/tail: O(1) — just update pointers
// NOT thread-safe, uses more memory (each node has prev + next pointer)
LinkedList<String> ll = new LinkedList<>();
ll.add("task1");
ll.addFirst("urgent");      // O(1) — inserts at head
ll.addLast("last");         // O(1) — inserts at tail
ll.pollFirst();             // O(1) — remove and return head (queue pop)
ll.peekFirst();             // O(1) — look at head without removing
ll.get(50);                 // O(n) — must traverse 50 nodes
// Use LinkedList for: queue/deque, frequent head/tail operations, not random access

// VECTOR — like ArrayList but SYNCHRONIZED (every method is synchronized)
// Thread-safe but slower due to lock acquisition overhead
// Legacy class — rarely used in modern Java
Vector<String> v = new Vector<>();
// Prefer: Collections.synchronizedList(new ArrayList<>()) or CopyOnWriteArrayList

// Performance comparison for 10,000 elements:
// Operation           | ArrayList | LinkedList | Vector
// Add at end          | O(1)*     | O(1)       | O(1)*
// Add at middle       | O(n)      | O(n)       | O(n)
// Add at head         | O(n)      | O(1)       | O(n)
// Get by index        | O(1)      | O(n)       | O(1)
// Remove from middle  | O(n)      | O(n)       | O(n)
// * amortised — occasional resize is O(n)

// Automation context
List<WebElement> rows = driver.findElements(By.tagName("tr"));  // returned as ArrayList
rows.get(5);       // O(1) — ArrayList is correct choice here
rows.size();       // O(1)
```

---

**Q14: What is the difference between HashMap, LinkedHashMap, and TreeMap?**

**A:**

```java
// HASHMAP — no guaranteed order, O(1) average get/put, allows ONE null key
Map<String, String> hashMap = new HashMap<>();
hashMap.put("browser",  "chrome");
hashMap.put("env",      "staging");
hashMap.put("timeout",  "30");
// Iteration order: unpredictable — might be: env, timeout, browser

// LINKEDHASHMAP — maintains INSERTION order, slightly more memory than HashMap
Map<String, String> linkedMap = new LinkedHashMap<>();
linkedMap.put("step1", "Navigate to login");
linkedMap.put("step2", "Enter credentials");
linkedMap.put("step3", "Click submit");
linkedMap.put("step4", "Verify dashboard");
// Iteration: step1, step2, step3, step4 — guaranteed insertion order
// Use for: maintaining test step order, preserving request parameter order

// TREEMAP — sorted by key in natural order (or custom Comparator), O(log n)
Map<String, Integer> treeMap = new TreeMap<>();
treeMap.put("banana", 2);
treeMap.put("apple",  1);
treeMap.put("cherry", 3);
// Iteration: apple, banana, cherry — alphabetical (natural String order)

// Custom sort order
Map<String, Integer> byLengthDesc = new TreeMap<>(
    Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder())
);
// Use TreeMap for: alphabetically sorted config, sorted test data display

// PRACTICAL AUTOMATION USAGE:
// HashMap — general test data storage
Map<String, String> testData = new HashMap<>();
testData.put("username", "admin");
testData.put("password", "Admin@123");

// LinkedHashMap — API request parameters in specific order
Map<String, String> params = new LinkedHashMap<>();
params.put("page", "1");
params.put("size", "20");
params.put("sortBy", "createdAt");

// TreeMap — sorted configuration for deterministic test output
Map<String, String> sortedConfig = new TreeMap<>(System.getProperties()
    .stringPropertyNames().stream()
    .collect(Collectors.toMap(k -> k, System.getProperty(k))));
```

---

**Q15: How does HashMap work internally?**

**A:**

HashMap internally uses an array of "buckets." Each bucket is a linked list (or a red-black tree for large buckets in Java 8+). Here is the step-by-step mechanism:

```
When you call map.put("orderId", "ORD-001"):

1. Compute hashCode: "orderId".hashCode() → some integer (e.g. 1735528448)
2. Map to bucket index: index = hash & (arrayLength - 1) → e.g. index = 12
3. Look at bucket 12:
   - If empty: store ("orderId", "ORD-001") directly
   - If has entries: check each entry with equals()
     - If an entry has key.equals("orderId"): replace its value ("ORD-001")
     - If no match (COLLISION): add to the linked list in bucket 12

When you call map.get("orderId"):
1. hashCode("orderId") → same hash
2. Index = same bucket 12
3. Walk the linked list, find entry where key.equals("orderId")
4. Return its value
```

Key properties:
- **Two objects that are `.equals()` MUST have the same `hashCode()`** — required by the contract. If you override `equals()` in a custom class, you must also override `hashCode()`.
- **Two objects with the same `hashCode()` are NOT necessarily equal** — they just land in the same bucket (collision).
- **Load factor 0.75**: when the map is 75% full, it resizes (doubles array size) and rehashes all entries. This keeps bucket chains short.
- **Java 8+ tree conversion**: when a bucket's linked list grows beyond 8 entries, it converts to a red-black tree for O(log n) lookup instead of O(n).

```java
// You MUST override both equals() and hashCode() together for custom keys
public class OrderKey {
    private String orderId;
    private String customerId;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OrderKey)) return false;
        OrderKey other = (OrderKey) obj;
        return Objects.equals(orderId, other.orderId) &&
               Objects.equals(customerId, other.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, customerId);
        // Objects.hash() combines multiple fields into one hash code
    }
}
// Without hashCode override: two OrderKey objects with same fields would
// have different hashCodes → stored in different buckets → get() returns null!
```

---

**Q16: What is ConcurrentModificationException and how do you fix it?**

**A:**

`ConcurrentModificationException` is thrown when you modify a `Collection` while iterating over it with a for-each loop or an `Iterator` that was not designed for concurrent modification.

```java
List<String> browsers = new ArrayList<>(Arrays.asList("chrome", "firefox", "safari", "ie"));

// CAUSES ConcurrentModificationException:
for (String b : browsers) {
    if (b.equals("ie")) {
        browsers.remove(b);   // modifying list while iterating → exception
    }
}

// FIX 1: use Iterator.remove() — safe removal during iteration
Iterator<String> it = browsers.iterator();
while (it.hasNext()) {
    String b = it.next();
    if (b.equals("ie")) {
        it.remove();   // remove via iterator, not via list — safe
    }
}

// FIX 2: removeIf() — cleanest, Java 8+
browsers.removeIf(b -> b.equals("ie"));
// or with method reference if you have a predicate method

// FIX 3: collect what to remove, then remove after iteration
List<String> toRemove = new ArrayList<>();
for (String b : browsers) {
    if (b.equals("ie")) toRemove.add(b);
}
browsers.removeAll(toRemove);

// FIX 4: CopyOnWriteArrayList — thread-safe, allows modification during iteration
// (creates a copy of the array on every write — expensive for frequent writes)
List<String> cowList = new CopyOnWriteArrayList<>(browsers);
for (String b : cowList) {
    if (b.equals("ie")) cowList.remove(b);   // safe — modifies the copy
}
```

---

## SECTION 4 — JAVA 8 FEATURES

---

**Q17: What is a functional interface and what are the most common ones?**

**A:**

A functional interface has exactly one abstract method. This makes it a valid target for lambda expressions and method references. The `@FunctionalInterface` annotation is optional but recommended — it causes a compile error if you accidentally add a second abstract method.

```java
// Custom functional interface
@FunctionalInterface
public interface ApiValidator {
    boolean validate(String responseBody);

    // default methods are allowed — don't count as abstract
    default ApiValidator and(ApiValidator other) {
        return body -> this.validate(body) && other.validate(body);
    }
}

// Lambda as implementation of ApiValidator
ApiValidator hasOrderId  = body -> body.contains("orderId");
ApiValidator hasStatus   = body -> body.contains("status");
ApiValidator combined    = hasOrderId.and(hasStatus);

combined.validate("{\"orderId\":\"ORD-001\",\"status\":\"PENDING\"}");  // true

// Common built-in functional interfaces in java.util.function:

// Predicate<T> — takes T, returns boolean
Predicate<String> isNotBlank = s -> s != null && !s.isBlank();
Predicate<Integer> isPositive = n -> n > 0;
Predicate<String> isLong = s -> s.length() > 100;
isNotBlank.and(isLong).test("hello");          // chain with .and()
isNotBlank.or(isPositive::test).test(null);    // chain with .or() (not valid here but shows usage)
isNotBlank.negate().test(null);               // negates: returns true for null/blank

// Function<T, R> — takes T, returns R
Function<String, Integer> strLen = String::length;
Function<String, String> toUpper = String::toUpperCase;
Function<String, String> combined2 = toUpper.andThen(s -> "[" + s + "]");
combined2.apply("hello");  // "[HELLO]"

// Supplier<T> — takes nothing, returns T
Supplier<String> getToken = () -> System.getenv("TEST_AUTH_TOKEN");
Supplier<WebDriver> createDriver = () -> new ChromeDriver();

// Consumer<T> — takes T, returns nothing
Consumer<String> print = System.out::println;
Consumer<Response> logResponse = r -> System.out.println(r.getStatusCode() + " " + r.getBody().asString());

// BiFunction<T,U,R> — takes two args, returns R
BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);

// UnaryOperator<T> — Function where T in = T out
UnaryOperator<String> trim = String::trim;

// BinaryOperator<T> — BiFunction where all three types are T
BinaryOperator<Integer> add = Integer::sum;
```

---

**Q18: Explain all Stream operations with practical automation examples.**

**A:**

```java
// INTERMEDIATE OPERATIONS (lazy — do not execute until a terminal is called):

List<WebElement> rows = driver.findElements(By.tagName("tr"));

// filter() — keep elements matching predicate
List<WebElement> enabledRows = rows.stream()
    .filter(row -> row.isEnabled())
    .collect(Collectors.toList());

// map() — transform each element
List<String> rowTexts = rows.stream()
    .map(WebElement::getText)
    .collect(Collectors.toList());

// flatMap() — flatten nested collections
List<List<String>> nested = List.of(List.of("a","b"), List.of("c","d"));
List<String> flat = nested.stream()
    .flatMap(Collection::stream)
    .collect(Collectors.toList());  // [a, b, c, d]

// distinct() — remove duplicates
List<String> uniqueStatuses = rows.stream()
    .map(r -> r.findElement(By.cssSelector(".status")).getText())
    .distinct()
    .collect(Collectors.toList());

// sorted() — sort elements
List<String> sortedNames = rows.stream()
    .map(r -> r.findElement(By.cssSelector(".name")).getText())
    .sorted()                               // natural order
    .sorted(Comparator.reverseOrder())      // reverse
    .collect(Collectors.toList());

// limit() and skip() — pagination
List<String> page2 = rowTexts.stream()
    .skip(20)    // skip first 20
    .limit(10)   // take next 10
    .collect(Collectors.toList());

// peek() — debug without changing the stream (side effect)
List<String> debugged = rowTexts.stream()
    .peek(t -> System.out.println("Processing: " + t))
    .filter(t -> t.contains("Active"))
    .collect(Collectors.toList());

// TERMINAL OPERATIONS (eager — trigger execution):

// collect() — gather results
List<String> list = stream.collect(Collectors.toList());
Set<String>  set  = stream.collect(Collectors.toSet());
Map<String, Long> counts = stream.collect(Collectors.groupingBy(s -> s, Collectors.counting()));
String joined = stream.collect(Collectors.joining(", ", "[", "]"));

// count()
long activeCount = rows.stream().filter(r -> r.getText().contains("Active")).count();

// anyMatch / allMatch / noneMatch
boolean hasError    = rows.stream().anyMatch(r -> r.getText().contains("Error"));
boolean allEnabled  = rows.stream().allMatch(WebElement::isEnabled);
boolean noneHidden  = rows.stream().noneMatch(r -> !r.isDisplayed());

// findFirst() / findAny()
Optional<WebElement> submitBtn = buttons.stream()
    .filter(b -> b.getText().equals("Submit"))
    .findFirst();
submitBtn.ifPresent(WebElement::click);

// min() / max()
Optional<WebElement> shortest = texts.stream()
    .min(Comparator.comparingInt(e -> e.getText().length()));

// reduce()
int totalLength = textList.stream()
    .map(String::length)
    .reduce(0, Integer::sum);  // sum all lengths

// forEach()
rows.stream()
    .map(r -> r.findElement(By.cssSelector(".status")).getText())
    .distinct()
    .forEach(status -> System.out.println("Found status: " + status));

// toArray()
String[] arr = textList.stream().toArray(String[]::new);
```

---

**Q19: What is Optional and what are all its methods?**

**A:**

Optional is a container that may or may not hold a non-null value. It forces callers to explicitly handle the "no value" case, preventing accidental NullPointerExceptions.

```java
// CREATING OPTIONALS:
Optional<String> empty    = Optional.empty();              // definitely empty
Optional<String> present  = Optional.of("hello");          // definitely has value (throws if null)
Optional<String> maybe    = Optional.ofNullable(getValue()); // null-safe: null → empty

// CHECKING:
opt.isPresent()   // true if has value (pre-Java 11)
opt.isEmpty()     // true if empty (Java 11+)

// GETTING THE VALUE:
opt.get()         // get value — throws NoSuchElementException if empty (use carefully)
opt.orElse("default")                             // get value or return default
opt.orElseGet(() -> generateDefault())            // get value or call supplier (lazy)
opt.orElseThrow(() -> new RuntimeException("No value found"))  // get or throw

// CONDITIONAL ACTIONS:
opt.ifPresent(v -> System.out.println("Found: " + v))     // run action only if present
opt.ifPresentOrElse(                                        // Java 9+
    v -> System.out.println("Found: " + v),
    () -> System.out.println("Not found")
);

// TRANSFORMING:
opt.map(String::toUpperCase)          // transform value if present, return new Optional
opt.flatMap(s -> Optional.of(s + "!")) // for when mapper returns Optional
opt.filter(s -> s.length() > 3)       // keep value only if condition true, else empty

// Chaining:
Optional<String> result = Optional.ofNullable(getApiResponse())
    .map(resp -> resp.jsonPath().getString("orderId"))
    .filter(id -> id.startsWith("ORD-"))
    .map(id -> id.substring(4));   // strip "ORD-" prefix

// Automation use:
Optional<WebElement> submitButton = driver.findElements(By.id("submitBtn"))
    .stream()
    .findFirst();

// Safe click — only clicks if element found
submitButton.ifPresent(WebElement::click);

// Get text or default if not found
String buttonText = submitButton
    .map(WebElement::getText)
    .orElse("Button not found");

// Throw meaningful exception if missing
WebElement requiredBtn = submitButton
    .orElseThrow(() -> new RuntimeException("Submit button not found on page: " + driver.getTitle()));
```

---

## SECTION 5 — DESIGN PATTERNS

---

**Q20: What is the Singleton pattern? Show three thread-safe implementations.**

**A:**

Singleton ensures only one instance of a class exists. In automation, it is used for `ConfigReader` (load config once), `DriverFactory` (manage a single driver per thread), or `ExtentManager` (one report file per run).

```java
// VERSION 1: synchronized method — simple, but slow (every call acquires lock)
public class ConfigReader {
    private static ConfigReader instance;
    private Properties props;

    private ConfigReader() {
        props = new Properties();
        try {
            props.load(new FileReader("src/test/resources/config.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config", e);
        }
    }

    public static synchronized ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }

    public String get(String key)                 { return props.getProperty(key); }
    public String get(String key, String def)     { return props.getProperty(key, def); }
}

// VERSION 2: double-checked locking — fast after first creation
public class ConfigReader {
    private static volatile ConfigReader instance;  // volatile is REQUIRED here

    private ConfigReader() { /* load config */ }

    public static ConfigReader getInstance() {
        if (instance == null) {                       // first check — no lock acquired
            synchronized (ConfigReader.class) {
                if (instance == null) {               // second check — with lock
                    instance = new ConfigReader();    // safe: only one thread creates it
                }
            }
        }
        return instance;   // no lock for subsequent calls — fast
    }
    // volatile prevents the compiler/JVM from reordering the write to instance
    // before all fields are fully initialised
}

// VERSION 3: Initialization-on-demand holder (BEST — simplest, thread-safe, lazy)
public class ConfigReader {
    private Properties props;

    private ConfigReader() {
        props = new Properties();
        try {
            props.load(ConfigReader.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Cannot load config: " + e.getMessage());
        }
    }

    // Inner static class is loaded only when getInstance() is first called
    // JVM guarantees class loading is thread-safe
    private static class Holder {
        static final ConfigReader INSTANCE = new ConfigReader();
    }

    public static ConfigReader getInstance() {
        return Holder.INSTANCE;   // no synchronization needed
    }

    public String get(String key) { return props.getProperty(key); }
    public String get(String key, String defaultValue) { return props.getProperty(key, defaultValue); }
    public int getInt(String key, int defaultValue) {
        String val = props.getProperty(key);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }
}

// Usage:
String baseUrl = ConfigReader.getInstance().get("base.url");
int timeout    = ConfigReader.getInstance().getInt("timeout.seconds", 30);
```

---

**Q21: What is the Factory pattern? Show with DriverFactory.**

**A:**

The Factory pattern hides the object creation logic behind a method. Callers ask for an object by type without knowing which class is instantiated or how it is configured.

```java
public class DriverFactory {

    // Static factory method — no instance needed
    public static WebDriver createDriver(String browser) {
        return switch (browser.toLowerCase()) {
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
                    options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                }
                yield new ChromeDriver(options);
            }
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions options = new FirefoxOptions();
                if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
                    options.addArguments("-headless");
                }
                yield new FirefoxDriver(options);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                yield new EdgeDriver();
            }
            case "safari" -> new SafariDriver();
            default -> throw new IllegalArgumentException(
                "Unknown browser: '" + browser + "'. Supported: chrome, firefox, edge, safari");
        };
    }
}

// Usage in BaseTest — the test doesn't know or care which driver class is used
@BeforeMethod
public void setUp() {
    String browser = System.getProperty("browser", "chrome");
    driver = DriverFactory.createDriver(browser);
    driver.manage().window().maximize();
}

// Run with: mvn test -Dbrowser=firefox -Dheadless=true
```

Benefits:
- Test class never imports `ChromeDriver` or `FirefoxDriver` — no coupling to specific driver classes
- To add Edge support: update only `DriverFactory`, not every test class
- To add remote WebDriver (Selenium Grid): add a `"remote"` case to the factory — no test code changes

---

**Q22: What is the Builder pattern? When do you use it over a constructor?**

**A:**

The Builder pattern constructs complex objects step by step. It solves the "telescoping constructor" problem — when a class has many optional fields, constructors with all combinations become unreadable.

```java
// WITHOUT Builder — which parameter is which?
new TestOrder("CUST-42", 149.99, "PRIORITY", "GIFT_WRAP", "EXPRESS", true, 2, "USD");
// Impossible to read without looking at the constructor signature every time.

// WITH Builder — self-documenting, optional fields handled cleanly
public class TestOrder {
    // Required fields
    private final String customerId;
    private final double amount;
    // Optional fields with defaults
    private final String type;
    private final String currency;
    private final String shippingMethod;
    private final boolean giftWrap;
    private final int    quantity;
    private final String promoCode;

    private TestOrder(Builder b) {
        this.customerId    = b.customerId;
        this.amount        = b.amount;
        this.type          = b.type;
        this.currency      = b.currency;
        this.shippingMethod= b.shippingMethod;
        this.giftWrap      = b.giftWrap;
        this.quantity      = b.quantity;
        this.promoCode     = b.promoCode;
    }

    // Getters
    public String getCustomerId()    { return customerId; }
    public double getAmount()        { return amount; }
    public String toJson() {
        return String.format(
            "{\"customerId\":\"%s\",\"amount\":%.2f,\"type\":\"%s\",\"currency\":\"%s\"," +
            "\"shippingMethod\":\"%s\",\"giftWrap\":%b,\"quantity\":%d,\"promoCode\":\"%s\"}",
            customerId, amount, type, currency, shippingMethod, giftWrap, quantity,
            promoCode != null ? promoCode : "");
    }

    public static class Builder {
        // Required
        private final String customerId;
        private final double amount;
        // Optional with defaults
        private String type          = "STANDARD";
        private String currency      = "AUD";
        private String shippingMethod= "STANDARD";
        private boolean giftWrap     = false;
        private int quantity         = 1;
        private String promoCode     = null;

        public Builder(String customerId, double amount) {
            if (customerId == null || customerId.isBlank())
                throw new IllegalArgumentException("customerId is required");
            if (amount <= 0)
                throw new IllegalArgumentException("amount must be positive");
            this.customerId = customerId;
            this.amount     = amount;
        }

        public Builder type(String type)             { this.type = type; return this; }
        public Builder currency(String currency)     { this.currency = currency; return this; }
        public Builder shipping(String method)       { this.shippingMethod = method; return this; }
        public Builder giftWrap(boolean wrap)        { this.giftWrap = wrap; return this; }
        public Builder quantity(int qty)             { this.quantity = qty; return this; }
        public Builder promoCode(String code)        { this.promoCode = code; return this; }

        public TestOrder build()                     { return new TestOrder(this); }
    }
}

// Usage — readable, obvious what each field is
TestOrder standard = new TestOrder.Builder("CUST-42", 149.99).build();

TestOrder priority = new TestOrder.Builder("CUST-99", 249.99)
    .type("PRIORITY")
    .shipping("EXPRESS")
    .giftWrap(true)
    .quantity(2)
    .promoCode("SAVE10")
    .build();

// In tests:
given()
    .contentType("application/json")
    .body(priority.toJson())
    .post("/api/orders");
```

---

**Q23: What is the Strategy pattern? Show with a wait strategy example.**

**A:**

The Strategy pattern defines a family of algorithms, encapsulates each one, and makes them interchangeable. The client selects which strategy to use at runtime without changing the class that uses it.

```java
// Strategy interface
public interface WaitStrategy {
    WebElement findElement(WebDriver driver, By locator);
}

// Strategy 1: Explicit wait — best for most Selenium tests
public class ExplicitWaitStrategy implements WaitStrategy {
    private final int timeoutSeconds;

    public ExplicitWaitStrategy(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public WebElement findElement(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}

// Strategy 2: Fluent wait — more flexible, ignore specific exceptions
public class FluentWaitStrategy implements WaitStrategy {
    @Override
    public WebElement findElement(WebDriver driver, By locator) {
        return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(30))
            .pollingEvery(Duration.ofMillis(500))
            .ignoring(NoSuchElementException.class)
            .ignoring(StaleElementReferenceException.class)
            .until(d -> d.findElement(locator));
    }
}

// Strategy 3: No wait — for elements that must be instantly available
public class NoWaitStrategy implements WaitStrategy {
    @Override
    public WebElement findElement(WebDriver driver, By locator) {
        return driver.findElement(locator);  // immediate, no polling
    }
}

// Context — BasePage uses whatever strategy is set
public class BasePage {
    protected WebDriver driver;
    private WaitStrategy waitStrategy;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.waitStrategy = new ExplicitWaitStrategy(10);  // sensible default
    }

    public void setWaitStrategy(WaitStrategy strategy) {
        this.waitStrategy = strategy;  // swap at runtime
    }

    protected WebElement find(By locator) {
        return waitStrategy.findElement(driver, locator);
    }
}

// Usage — switch strategy for specific pages
BasePage slowPage = new BasePage(driver);
slowPage.setWaitStrategy(new FluentWaitStrategy());  // animated page needs fluent

BasePage fastPage = new BasePage(driver);
fastPage.setWaitStrategy(new ExplicitWaitStrategy(5));  // fast loading page
```

---

## SECTION 6 — ANNOTATIONS AND MAVEN

---

**Q24: What are the built-in Java annotations and how do you create a custom annotation?**

**A:**

```java
// BUILT-IN JAVA ANNOTATIONS:
@Override              // tells compiler you are overriding a parent method (caught at compile time)
@Deprecated            // marks method/class as outdated — IDE shows strikethrough
@SuppressWarnings("unchecked")  // suppress specific compiler warnings
@FunctionalInterface   // marks interface as having exactly one abstract method

// TESTNG ANNOTATIONS (framework-specific):
@BeforeSuite  @AfterSuite    // run once per suite
@BeforeTest   @AfterTest     // run once per <test> tag in testng.xml
@BeforeClass  @AfterClass    // run once per test class
@BeforeMethod @AfterMethod   // run before/after EACH test method

@Test(groups = {"smoke", "regression"}, priority = 1, enabled = true,
      timeOut = 5000, expectedExceptions = RuntimeException.class,
      retryAnalyzer = RetryAnalyzer.class, description = "Verify login")
@DataProvider(name = "loginData", parallel = true)
@Parameters({"browser", "env"})
@Listeners({ScreenshotListener.class, AllureListener.class})

// CREATING A CUSTOM ANNOTATION:
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)    // annotation survives until runtime (needed for reflection)
@Target(ElementType.METHOD)            // applies to methods only
// Other targets: TYPE (class/interface), FIELD, PARAMETER, PACKAGE, ANNOTATION_TYPE
public @interface TestCase {
    String id();                        // required element
    String description() default "";    // optional with default
    String[] tags()       default {};   // array element
    String jiraTicket()   default "";
    SeverityLevel severity() default SeverityLevel.NORMAL;  // enum element
}

// Usage:
@TestCase(
    id          = "TC-001",
    description = "Verify POST /api/orders returns 201 with valid payload",
    tags        = {"smoke", "regression", "orders"},
    jiraTicket  = "OMS-100",
    severity    = SeverityLevel.BLOCKER
)
@Test
public void createOrder_validPayload_returns201() { ... }

// Reading the annotation at runtime via reflection:
Method method = getClass().getMethod("createOrder_validPayload_returns201");
if (method.isAnnotationPresent(TestCase.class)) {
    TestCase tc = method.getAnnotation(TestCase.class);
    System.out.println("Test ID: "   + tc.id());
    System.out.println("Tags: "      + Arrays.toString(tc.tags()));
    System.out.println("Severity: "  + tc.severity());
    System.out.println("Ticket: "    + tc.jiraTicket());
}
```

---

**Q25: What is the Maven pom.xml structure? Explain all dependency scopes and all lifecycle phases.**

**A:**

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <!-- Project coordinates — uniquely identify this project in Maven repositories -->
    <groupId>com.example</groupId>          <!-- your organisation's package -->
    <artifactId>api-automation</artifactId> <!-- project name -->
    <version>1.0-SNAPSHOT</version>         <!-- SNAPSHOT = in development, RELEASE = stable -->
    <packaging>jar</packaging>              <!-- jar (default), war, pom -->

    <!-- Properties — centralise versions, reusable across dependencies -->
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <testng.version>7.8.0</testng.version>
        <restassured.version>5.4.0</restassured.version>
        <allure.version>2.24.0</allure.version>
    </properties>

    <!-- Dependencies — external libraries -->
    <dependencies>
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>${restassured.version}</version>
            <scope>test</scope>      <!-- SCOPE — controls when dependency is available -->
        </dependency>
    </dependencies>

    <!-- Plugins — build tools -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.1.2</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>testng.xml</suiteXmlFile>
                    </suiteXmlFiles>
                    <testFailureIgnore>false</testFailureIgnore>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Dependency Scope values:**

| Scope | Compile | Test | Run | Packaged | Use Case |
|-------|---------|------|-----|----------|----------|
| `compile` (default) | ✅ | ✅ | ✅ | ✅ | Production libraries |
| `test` | ❌ | ✅ | ❌ | ❌ | TestNG, RestAssured, Allure |
| `provided` | ✅ | ✅ | ❌ | ❌ | Servlet API (server provides it at runtime) |
| `runtime` | ❌ | ✅ | ✅ | ✅ | JDBC drivers (not needed to compile, needed to run) |
| `system` | ✅ | ✅ | ❌ | ❌ | Local JAR on filesystem (avoid — not portable) |
| `import` | POM only | — | — | — | Import dependency management from another POM |

**Maven Build Lifecycle Phases (in order):**
```
validate      → verify pom.xml is correct and all info is available
initialize    → initialise build state (e.g. set properties)
compile       → compile src/main/java (production code)
test-compile  → compile src/test/java (test code)
test          → run unit tests (via Surefire plugin)
package       → create JAR/WAR in target/
verify        → run integration tests, check quality gates
install       → copy JAR to local ~/.m2 repository cache
deploy        → copy JAR to remote Maven repository (Nexus, Artifactory)
```

Each phase runs all previous phases. `mvn install` runs validate, compile, test, package, verify, install in sequence.

**Common Maven Commands:**
```bash
mvn clean                          # delete target/ directory
mvn compile                        # compile production code
mvn test-compile                   # compile test code
mvn test                           # run all tests
mvn clean test                     # clean then run tests
mvn test -Dtest=CreateOrderTest    # run specific test class
mvn test -Dtest=CreateOrderTest#createOrder_validPayload_returns201  # run specific method
mvn test -Dgroups=smoke            # run tests tagged with group "smoke"
mvn test -Denv=staging             # pass system property to tests
mvn package -DskipTests            # build JAR without running tests
mvn dependency:tree                # show dependency tree (find version conflicts)
mvn dependency:resolve             # download all dependencies
mvn versions:display-dependency-updates  # show outdated dependencies
mvn help:effective-pom             # show the complete resolved POM
```

---

## SECTION 7 — INTERVIEW Q&A

---

**Q26: What is the difference between checked and unchecked exceptions? When do you use each?**

**A:** Checked exceptions extend `Exception` and the compiler forces you to either catch them or declare them in the method signature with `throws`. Examples: `IOException`, `SQLException`, `ClassNotFoundException`. They represent recoverable situations the caller should explicitly handle — "file not found" is something the caller might recover from by trying a different path. Unchecked exceptions extend `RuntimeException` — no `throws` declaration needed, no forced catch. Examples: `NullPointerException`, `IllegalArgumentException`, `NoSuchElementException`, `TimeoutException`. They represent programming errors or unrecoverable failures. In automation, most test framework exceptions are unchecked — `NoSuchElementException`, `AssertionError`. When wrapping a checked exception for cleaner test code, always chain the original: `throw new RuntimeException("Cannot load config", e)` — the `e` preserves the original cause for debugging.

---

**Q27: What is the Page Object Model and why is it the most important design pattern in automation?**

**A:** POM is a design pattern where each page (or significant component) of an application has a corresponding Java class that encapsulates the page's element locators and the actions that can be performed on it. Tests call the action methods; they never directly access WebElements or locators. Benefits: (1) Maintainability — if a locator changes, update only the page class; all 50 tests using it are fixed automatically. (2) Readability — test code reads like a user story: `loginPage.login("admin", "Admin@123")` not `driver.findElement(By.id("username")).sendKeys(...)`. (3) Reusability — `loginPage.login()` is called by 50 tests. (4) Separation of concerns — page structure is separate from test logic. Without POM, a locator change requires touching every test that uses it — which can be hundreds of files. POM makes the automation suite maintainable as it grows.

---

**Q28: How would you implement parallel test execution in TestNG with thread-safe WebDriver management?**

**A:**
```java
// BaseTest — ThreadLocal keeps each thread's driver independent
public class BaseTest {
    private static final ThreadLocal<WebDriver> driverPool = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driverPool.get();
    }

    @BeforeMethod
    public void setUp() {
        WebDriver driver = DriverFactory.createDriver(
            System.getProperty("browser", "chrome"));
        driver.manage().window().maximize();
        driverPool.set(driver);   // set driver for THIS thread only
    }

    @AfterMethod
    public void tearDown() {
        WebDriver driver = getDriver();
        if (driver != null) {
            driver.quit();
            driverPool.remove();   // CRITICAL: prevent memory leak
        }
    }
}
```

```xml
<!-- testng.xml — run tests in parallel at method level -->
<suite name="Parallel Suite" parallel="methods" thread-count="4">
    <test name="Order Tests">
        <classes>
            <class name="com.example.tests.CreateOrderTest"/>
            <class name="com.example.tests.GetOrderTest"/>
        </classes>
    </test>
</suite>
```

`ThreadLocal` ensures that when Thread-1 calls `getDriver()` it gets Thread-1's ChromeDriver, and Thread-2 gets Thread-2's FirefoxDriver. Without `ThreadLocal`, all threads would share one `static WebDriver driver` — every thread would close the browser the other thread is using, causing random failures.

---

**Q29: What are the 4 pillars of OOP and which ones do you apply most in your automation work?**

**A:** Encapsulation, Inheritance, Polymorphism, and Abstraction. In my automation work: Encapsulation is most applied through Page Object Model — all locators are private, only public action methods are exposed. If I changed `By.id("btn")` to `By.css(".btn")`, only the page class changes. Inheritance is applied through `BaseTest` — every test class extends it and inherits `setUp()` and `tearDown()`, eliminating hundreds of lines of duplicate setup code. Polymorphism appears in driver management — `WebDriver driver = DriverFactory.createDriver("firefox")` stores a `FirefoxDriver` as a `WebDriver` type, so test code works regardless of which browser is running. Abstraction appears in `BasePage` — its `waitForElement()` method hides all the timing complexity; tests just call `find(By.id("x"))` without caring about waits, retries, or exception handling.

---

**Q30: What is a static method and when should you use static vs instance methods?**

**A:** A static method belongs to the class, not to any instance. It is called on the class name directly (`DriverFactory.createDriver("chrome")`) rather than on an object. It cannot access instance fields or `this`. Use static for: utility methods that don't need object state (`StringUtils.trimAndLower(s)`), factory methods (`DriverFactory.createDriver()`), constants (`ApiConstants.BASE_URL`), and helper methods in test classes that are called from before/after methods before any instance state exists. Use instance methods for: anything that reads or modifies the object's state (`loginPage.login(user, pass)` — reads `usernameField`, `passwordField` which belong to the instance). The distinction matters in parallel testing: static fields are shared across all threads; instance fields (including `ThreadLocal`) are per-thread safe.

---

*Guide covers 30 questions: all 4 OOP pillars with automation code, interface vs abstract class, constructor chaining, this/super, String/StringBuilder/StringBuffer, ==/.equals()/.compareTo(), final/finally/finalize(), autoboxing pitfalls, throw/throws/chaining, static block order, varargs, ArrayList/LinkedList/Vector, HashMap/LinkedHashMap/TreeMap, HashMap internals, ConcurrentModificationException, functional interfaces, all Stream operations (intermediate + terminal), Optional (all methods), Singleton (3 thread-safe versions), Factory pattern, Builder pattern, Strategy pattern, custom annotations, Maven pom.xml with all scopes and phases, and 5 interview Q&A questions.*
