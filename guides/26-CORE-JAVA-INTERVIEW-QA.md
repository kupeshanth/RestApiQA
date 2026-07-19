# Core Java — Full Interview Q&A
## All Possible Questions | Full Answers | Real Examples | Automation Context

---

## SECTION 1 — OOP FUNDAMENTALS

---

**Q1: What are the 4 pillars of OOP? Explain each with an example from automation.**

**A:**

**1. Encapsulation** — hiding internal data, exposing only through controlled methods.

```java
// BAD — anyone can access and corrupt data
public class LoginPage {
    public WebElement usernameField;   // anyone can call usernameField.clear() directly
}

// GOOD — hide the element, expose only the action
public class LoginPage {
    private WebElement usernameField;  // hidden
    
    public void enterUsername(String username) {
        usernameField.clear();
        usernameField.sendKeys(username);
    }
}
```
*Why it matters: if the locator changes, only LoginPage needs updating — not every test.*

---

**2. Inheritance** — child class reuses parent class behaviour.

```java
// BaseTest — common setup for all tests
public class BaseTest {
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() {
        driver.quit();
    }
}

// LoginTest — inherits setUp() and tearDown() automatically
public class LoginTest extends BaseTest {
    @Test
    public void validLoginTest() {
        driver.get("https://app.com/login");   // driver available from parent
        // ...
    }
}
```

---

**3. Polymorphism** — same method call, different behaviour based on actual object type.

```java
// WebDriver is an interface. ChromeDriver and FirefoxDriver both implement it.
WebDriver driver = new ChromeDriver();     // ChromeDriver stored as WebDriver type
driver.get("https://app.com");            // calls ChromeDriver's get()

driver = new FirefoxDriver();             // swap without changing test code
driver.get("https://app.com");            // now calls FirefoxDriver's get()

// Your test code never changed — only the underlying driver did.
// This is runtime polymorphism.
```

---

**4. Abstraction** — hide complexity, show only what matters.

```java
// BasePage hides HOW navigation works
public abstract class BasePage {
    protected WebDriver driver;
    
    public abstract boolean isLoaded();   // subclass MUST implement
    
    public void navigate(String path) {   // hidden HOW — tests just call this
        driver.get(driver.getCurrentUrl().split("/")[0] + "//" + 
                   driver.getCurrentUrl().split("/")[2] + path);
    }
}

// LoginPage shows WHAT, not HOW
public class LoginPage extends BasePage {
    @Override
    public boolean isLoaded() {
        return driver.getCurrentUrl().contains("/login");
    }
}
```

---

**Q2: What is the difference between method overloading and method overriding?**

**A:**

```java
// OVERLOADING — same class, same method name, DIFFERENT parameters
// Resolved at COMPILE TIME (static binding)
public class DataProvider {
    public void setup(String url) {
        RestAssured.baseURI = url;
    }
    
    public void setup(String url, String token) {   // different parameters
        RestAssured.baseURI = url;
        RestAssured.authentication = oauth2(token);
    }
    
    public void setup(String url, int timeout) {    // different parameter type
        RestAssured.baseURI = url;
        RestAssured.config = RestAssured.config().httpClient(
            httpClientConfig().setParam("http.socket.timeout", timeout));
    }
}

// Usage — compiler picks the right one based on parameters you pass
provider.setup("https://api.com");
provider.setup("https://api.com", "bearer-token");
provider.setup("https://api.com", 5000);
```

```java
// OVERRIDING — parent and child, SAME signature
// Resolved at RUNTIME (dynamic binding) — depends on actual object type
public class Animal {
    public String speak() { return "..."; }
}

public class Dog extends Animal {
    @Override
    public String speak() { return "Woof!"; }   // replaces parent's version
}

public class Cat extends Animal {
    @Override
    public String speak() { return "Meow!"; }
}

Animal a = new Dog();   // Dog stored as Animal reference
a.speak();              // returns "Woof!" — runtime decides based on actual type
```

**Key difference:**
| | Overloading | Overriding |
|--|------------|-----------|
| Location | Same class | Parent + Child |
| Signature | Different params | Identical |
| When decided | Compile time | Runtime |
| `@Override` | No | Yes (recommended) |
| `static` methods | Yes | No (hiding, not overriding) |

---

**Q3: What is the difference between an interface and an abstract class?**

**A:**

```java
// INTERFACE — defines WHAT a class must do (contract)
public interface Clickable {
    void click();                    // abstract — must implement
    void doubleClick();              // abstract
    
    default void highlight() {       // default — optional override (Java 8+)
        System.out.println("Highlighted");
    }
    
    static void log(String msg) {    // static — utility method (Java 8+)
        System.out.println("[LOG] " + msg);
    }
}

// ABSTRACT CLASS — partial implementation + contract
public abstract class BasePage {
    protected WebDriver driver;      // can have fields
    protected WebDriverWait wait;
    
    public BasePage(WebDriver driver) {   // can have constructor
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    public abstract boolean isLoaded();  // must implement
    
    public void navigateTo(String url) { // can implement shared behaviour
        driver.get(url);
        wait.until(d -> ((JavascriptExecutor) d)
            .executeScript("return document.readyState").equals("complete"));
    }
}

// Class can implement MANY interfaces but extend only ONE class
public class LoginPage extends BasePage implements Clickable, Serializable {
    public LoginPage(WebDriver driver) { super(driver); }
    
    @Override public boolean isLoaded() { return driver.getCurrentUrl().contains("/login"); }
    @Override public void click()       { /* implementation */ }
    @Override public void doubleClick() { /* implementation */ }
}
```

**When to choose:**
- Use **interface** when: defining capability/contract that unrelated classes might implement (Clickable, Serializable, WebDriver)
- Use **abstract class** when: sharing code between closely related classes (BasePage, BaseTest, BaseApiTest)

---

**Q4: Can we create an object of an abstract class or interface?**

**A:**
```java
// DIRECT INSTANTIATION — NOT ALLOWED
BasePage page = new BasePage(driver);   // compile error
Clickable c = new Clickable();          // compile error

// BUT — you CAN use them as reference types (polymorphism)
BasePage page = new LoginPage(driver);  // OK — LoginPage IS-A BasePage
Clickable c = new LoginPage(driver);    // OK — LoginPage implements Clickable

// Anonymous class — one-time implementation without naming it
Clickable c = new Clickable() {         // OK — anonymous class
    @Override public void click() { System.out.println("Clicked!"); }
    @Override public void doubleClick() { System.out.println("Double!"); }
};
```

---

**Q5: What is the difference between `this` and `super` keyword?**

**A:**
```java
public class Animal {
    protected String name;
    
    public Animal(String name) {
        this.name = name;   // 'this' distinguishes field from parameter
    }
    
    public String describe() {
        return "Animal: " + name;
    }
}

public class Dog extends Animal {
    private String breed;
    
    public Dog(String name, String breed) {
        super(name);    // 'super' calls parent constructor — MUST be first line
        this.breed = breed;  // 'this' sets own field
    }
    
    @Override
    public String describe() {
        String parentDescription = super.describe();  // call parent's method
        return parentDescription + " | Dog breed: " + breed;
    }
    
    public Dog getThis() {
        return this;   // 'this' returns current instance
    }
}
```

---

**Q6: What is constructor chaining?**

**A:**
```java
public class TestConfig {
    private String browser;
    private String baseUrl;
    private int timeout;
    
    // Full constructor
    public TestConfig(String browser, String baseUrl, int timeout) {
        this.browser = browser;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
    }
    
    // Constructor chaining — calls another constructor in SAME class using this()
    public TestConfig(String browser, String baseUrl) {
        this(browser, baseUrl, 30);   // default timeout 30s — this() must be first
    }
    
    public TestConfig() {
        this("chrome", "https://app.com");   // default browser and URL
    }
}

// Usage
TestConfig config1 = new TestConfig();                              // all defaults
TestConfig config2 = new TestConfig("firefox", "https://stage.com"); // custom
TestConfig config3 = new TestConfig("chrome", "https://prod.com", 60); // all specified
```

---

## SECTION 2 — CORE JAVA CONCEPTS

---

**Q7: What is the difference between `String`, `StringBuilder`, and `StringBuffer`?**

**A:**
```java
// String — IMMUTABLE. Every operation creates a new String object.
String s = "Hello";
s.concat(" World");  // creates new String, s is still "Hello"!
s = s.concat(" World");  // now s points to new "Hello World"

// BAD — creates 5 intermediate String objects in loop
String result = "";
for (int i = 0; i < 5; i++) {
    result = result + i;   // new String object each iteration → memory waste
}

// StringBuilder — MUTABLE, NOT thread-safe, FASTER
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 5; i++) {
    sb.append(i);   // modifies same object — no new objects created
}
String result = sb.toString();  // "01234"

sb.insert(2, "X");    // "01X234"
sb.delete(1, 3);      // "0X234"  — delete from index 1 to 3 (exclusive)
sb.reverse();         // "432X0"
sb.length();          // 5

// StringBuffer — MUTABLE, thread-safe (synchronized), SLOWER than StringBuilder
StringBuffer sbuf = new StringBuffer();
// Use only when multiple threads modify the same string

// Rule: String for fixed values, StringBuilder for building in a single thread,
// StringBuffer for building across multiple threads
```

---

**Q8: What is the difference between `==`, `.equals()`, and `.compareTo()` for Strings?**

**A:**
```java
String a = new String("hello");
String b = new String("hello");
String c = a;

// == compares REFERENCES (memory addresses)
a == b   // false — different objects in heap
a == c   // true  — same reference
a == "hello"  // may be true due to String pool — NEVER rely on this

// .equals() compares CONTENT
a.equals(b)   // true  — same content
a.equals("hello")  // true

// .equalsIgnoreCase() — case-insensitive comparison
"Hello".equalsIgnoreCase("hello")  // true

// .compareTo() — lexicographic comparison (used for sorting)
"apple".compareTo("banana")   // negative — "apple" comes before "banana"
"banana".compareTo("apple")   // positive — "banana" comes after "apple"
"apple".compareTo("apple")    // 0 — identical

// In automation: always use .equals() to compare URL, page title, element text
String actualTitle = driver.getTitle();
Assert.assertEquals(actualTitle, "Dashboard");  // TestNG uses .equals() internally
```

---

**Q9: What is the difference between `final`, `finally`, and `finalize()`?**

**A:**
```java
// FINAL — modifier with 3 uses:
final int MAX_RETRY = 3;            // variable cannot be reassigned
// MAX_RETRY = 5;  → compile error

final class ImmutableConfig { }     // class cannot be extended

class BaseTest {
    public final void setUp() { }   // method cannot be overridden by subclass
}

// FINALLY — block that ALWAYS runs in try-catch
@AfterMethod
public void tearDown() {
    try {
        performCleanup();           // might throw exception
    } catch (Exception e) {
        System.err.println("Cleanup failed: " + e.getMessage());
    } finally {
        driver.quit();              // runs REGARDLESS of exception
        // Perfect place for: driver.quit(), connection.close(), file.close()
    }
}

// FINALIZE() — deprecated. Called by GC before collecting object.
// NEVER use this. GC timing is unpredictable. Use finally or try-with-resources instead.
@Override
protected void finalize() throws Throwable {
    // DON'T USE — deprecated since Java 9
}
```

---

**Q10: What is autoboxing and unboxing? When can it cause problems?**

**A:**
```java
// AUTOBOXING — primitive → wrapper class (automatic)
int primitive = 5;
Integer boxed = primitive;          // autoboxed: Integer.valueOf(5)
List<Integer> list = new ArrayList<>();
list.add(42);                       // autoboxed: list.add(Integer.valueOf(42))

// UNBOXING — wrapper → primitive (automatic)
Integer boxed = Integer.valueOf(10);
int primitive = boxed;              // unboxed: boxed.intValue()

// PROBLEM 1: NullPointerException on unboxing null
Integer value = null;
int x = value;    // NullPointerException! Can't unbox null to primitive

// PROBLEM 2: Performance in loops
List<Integer> list = new ArrayList<>();
for (int i = 0; i < 1_000_000; i++) {
    list.add(i);  // 1 million autobox operations — slow
}

// PROBLEM 3: == comparison on cached Integers (-128 to 127)
Integer a = 127;
Integer b = 127;
a == b   // TRUE — Java caches -128 to 127, same object

Integer c = 128;
Integer d = 128;
c == d   // FALSE — outside cache range, different objects
// Always use .equals() for Integer/Long/Double comparisons
```

---

**Q11: What is the difference between `throw` and `throws`?**

**A:**
```java
// 'throws' — declares that a method MIGHT throw these exceptions
// Required for CHECKED exceptions
public void readConfig(String path) throws IOException, FileNotFoundException {
    // method body
}

// 'throw' — actually THROWS an exception at runtime
public void login(String username) {
    if (username == null || username.isEmpty()) {
        throw new IllegalArgumentException("Username cannot be null or empty");
        // IllegalArgumentException is UNCHECKED — no 'throws' declaration needed
    }
    
    try {
        File config = new File("config.properties");
        throw new IOException("File not found");  // CHECKED — needs 'throws' or catch
    } catch (IOException e) {
        throw new RuntimeException("Config load failed", e);  // wrap in unchecked
    }
}
```

---

**Q12: What is a static block and when is it executed?**

**A:**
```java
public class ApiConstants {
    public static final String BASE_URL;
    public static final String AUTH_TOKEN;
    
    // Static block — runs ONCE when class is first loaded, before any object created
    static {
        System.out.println("Loading API constants...");
        Properties props = new Properties();
        try {
            props.load(new FileReader("config.properties"));
            BASE_URL   = props.getProperty("base_url", "https://default.com");
            AUTH_TOKEN = props.getProperty("auth_token", "");
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Cannot load config: " + e.getMessage());
        }
    }
}

// Execution order: static block → constructor → instance block
public class Demo {
    static { System.out.println("1. Static block"); }           // first, once
    { System.out.println("2. Instance block"); }                 // before constructor
    public Demo() { System.out.println("3. Constructor"); }

    public static void main(String[] args) {
        new Demo();   // output: 1, 2, 3
        new Demo();   // output:    2, 3 (static block doesn't run again)
    }
}
```

---

## SECTION 3 — COLLECTIONS DEEP DIVE

---

**Q13: What is the difference between ArrayList, LinkedList, and Vector?**

**A:**
```java
// ArrayList — dynamic array, fast random access O(1), slow middle insert O(n)
List<String> al = new ArrayList<>();
al.get(50);          // O(1) — direct index access

// LinkedList — doubly linked nodes, slow random access O(n), fast insert O(1)
List<String> ll = new LinkedList<>();
((LinkedList<String>) ll).addFirst("item");   // O(1) — fast at head/tail

// Vector — like ArrayList but SYNCHRONIZED (thread-safe), slower
Vector<String> v = new Vector<>();
// Rarely used — prefer ArrayList + Collections.synchronizedList() or CopyOnWriteArrayList

// When to use:
// ArrayList:    default for most use cases
// LinkedList:   when frequently adding/removing from head/tail (queue, deque use case)
// Vector:       legacy — avoid, use modern thread-safe alternatives
```

---

**Q14: What is the difference between HashMap, LinkedHashMap, and TreeMap?**

**A:**
```java
// HashMap — no order guarantee, O(1) get/put, allows one null key
Map<String, Integer> hashMap = new HashMap<>();
hashMap.put("banana", 2);
hashMap.put("apple", 1);
hashMap.put("cherry", 3);
// Iteration order: unpredictable

// LinkedHashMap — maintains INSERTION order
Map<String, Integer> linkedMap = new LinkedHashMap<>();
linkedMap.put("banana", 2);
linkedMap.put("apple", 1);
linkedMap.put("cherry", 3);
// Iteration: banana, apple, cherry (insertion order)
// Use when: you need to remember the order items were added

// TreeMap — sorted by KEY in natural order (or custom Comparator)
Map<String, Integer> treeMap = new TreeMap<>();
treeMap.put("banana", 2);
treeMap.put("apple", 1);
treeMap.put("cherry", 3);
// Iteration: apple, banana, cherry (alphabetical)
// Use when: you need sorted keys, O(log n) operations

// In automation:
// HashMap: general test data storage
// LinkedHashMap: when test step order matters
// TreeMap: when you need alphabetically sorted config keys
```

---

**Q15: What is the difference between HashSet, LinkedHashSet, and TreeSet?**

**A:**
```java
// HashSet — no duplicates, NO order, O(1) add/remove/contains
Set<String> hashSet = new HashSet<>();
hashSet.add("chrome"); hashSet.add("firefox"); hashSet.add("chrome");
hashSet.size();  // 2 — duplicate ignored

// LinkedHashSet — no duplicates, INSERTION order
Set<String> linkedSet = new LinkedHashSet<>();
linkedSet.add("chrome"); linkedSet.add("firefox"); linkedSet.add("safari");
// Iteration: chrome, firefox, safari

// TreeSet — no duplicates, SORTED order, O(log n)
Set<String> treeSet = new TreeSet<>();
treeSet.add("chrome"); treeSet.add("firefox"); treeSet.add("safari");
// Iteration: chrome, firefox, safari (alphabetical — same here, different with numbers)
treeSet.first();   // "chrome"
treeSet.last();    // "safari"

// Automation use case:
Set<String> visitedUrls = new HashSet<>();  // track visited pages, no duplicates
List<WebElement> links = driver.findElements(By.tagName("a"));
links.stream().map(e -> e.getAttribute("href")).forEach(visitedUrls::add);
System.out.println("Unique URLs found: " + visitedUrls.size());
```

---

**Q16: How does HashMap work internally?**

**A:**
```
HashMap uses an array of buckets (linked list/tree per bucket).

1. When you call put("key", value):
   - Java calls key.hashCode() → gets a hash number
   - Maps hash to a bucket index: index = hash % array.length
   - If bucket empty → store directly
   - If bucket has entries → check equals() for duplicates
     - If equal key found → replace value
     - If not equal → add to linked list in bucket (collision)

2. When you call get("key"):
   - Compute hashCode() → find bucket
   - Walk bucket's list, use equals() to find exact match

Key points:
- Two objects that are equal MUST have the same hashCode
- Two objects with same hashCode are NOT necessarily equal (collision)
- HashMap allows ONE null key (stored in bucket 0)
- Load factor 0.75: resizes when 75% full (rehashes all entries)
- Java 8+: bucket with >8 entries converts list → red-black tree (O(log n) lookup)
```

---

**Q17: What is the difference between `Iterator` and `ListIterator`?**

**A:**
```java
List<String> list = Arrays.asList("a", "b", "c");

// Iterator — forward only, works on any Collection
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String item = it.next();
    if (item.equals("b")) {
        it.remove();   // safe removal during iteration
    }
}

// ListIterator — forward AND backward, only for List
ListIterator<String> lit = list.listIterator();
while (lit.hasNext()) {
    String item = lit.next();
    lit.set("X" + item);   // replace current element
    lit.add("new");        // add after current
}
// Go backward
while (lit.hasPrevious()) {
    System.out.println(lit.previous());
}

// ConcurrentModificationException — happens when you modify list while iterating with for-each
List<String> list2 = new ArrayList<>(Arrays.asList("a", "b", "c"));
for (String s : list2) {
    if (s.equals("b")) list2.remove(s);  // ConcurrentModificationException!
}
// Fix: use Iterator.remove() or removeIf()
list2.removeIf(s -> s.equals("b"));  // safe and clean
```

---

## SECTION 4 — EXCEPTION HANDLING

---

**Q18: What is the difference between checked and unchecked exceptions?**

**A:**
```java
// CHECKED — compiler forces you to handle or declare
// Extends Exception (not RuntimeException)
public void loadFile(String path) throws IOException {   // must declare
    FileReader fr = new FileReader(path);   // throws FileNotFoundException (checked)
}

// Common checked exceptions:
// IOException, FileNotFoundException, SQLException, ClassNotFoundException, ParseException

// UNCHECKED (RuntimeException) — no requirement to declare or catch
public void click(WebElement el) {
    el.click();  // might throw NoSuchElementException — no 'throws' needed
}

// Common unchecked exceptions:
// NullPointerException, ArrayIndexOutOfBoundsException, ClassCastException,
// IllegalArgumentException, NoSuchElementException (Selenium), TimeoutException

// In automation — wrap checked in unchecked for cleaner test code:
public void readConfig(String path) {
    try {
        properties.load(new FileReader(path));
    } catch (IOException e) {
        throw new RuntimeException("Cannot load config: " + path, e);  // unchecked wrapper
    }
}
```

---

**Q19: What is exception chaining? Why preserve the original cause?**

**A:**
```java
// BAD — original exception lost, hard to debug
try {
    connectToDatabase();
} catch (SQLException e) {
    throw new RuntimeException("DB connection failed");  // original cause gone!
}

// GOOD — chain exceptions, original cause preserved
try {
    connectToDatabase();
} catch (SQLException e) {
    throw new RuntimeException("DB connection failed", e);  // e is the cause
    // Stack trace shows BOTH: RuntimeException + original SQLException
}

// Reading chained exceptions:
try {
    runTest();
} catch (Exception e) {
    System.out.println("Top: " + e.getMessage());
    System.out.println("Cause: " + e.getCause().getMessage());
    e.printStackTrace();  // shows full chain
}
```

---

## SECTION 5 — JAVA 8 FEATURES

---

**Q20: What is a functional interface and how does it relate to lambdas?**

**A:**
```java
// Functional interface — exactly ONE abstract method (can have default/static methods)
@FunctionalInterface
public interface Condition {
    boolean check(String input);   // exactly one abstract method
    
    default Condition and(Condition other) {   // default method — OK
        return input -> this.check(input) && other.check(input);
    }
}

// Lambda — short implementation of a functional interface
Condition notEmpty = input -> !input.isEmpty();
Condition longEnough = input -> input.length() >= 8;

notEmpty.check("hello");         // true
notEmpty.check("");              // false
notEmpty.and(longEnough).check("hi");     // false — longEnough fails

// Common built-in functional interfaces
Predicate<String> isValid = s -> s != null && !s.isBlank();
Function<String, Integer> toLength = String::length;
Supplier<String> getDefault = () -> "default";
Consumer<String> print = System.out::println;
Runnable task = () -> performCleanup();
Comparator<String> compare = (a, b) -> a.compareTo(b);

// In RestAssured
given()
    .filter((req, resp, ctx) -> {          // lambda as filter (functional interface)
        System.out.println("Request: " + req.getURI());
        return ctx.next(req, resp);
    })
.when().get("/posts");
```

---

**Q21: What is the Optional class and why use it?**

**A:**
```java
// Problem it solves: NullPointerException from missing values
public String getUserEmail(int userId) {
    User user = database.findById(userId);
    return user.getEmail();   // NullPointerException if user not found!
}

// With Optional:
public Optional<String> getUserEmail(int userId) {
    return Optional.ofNullable(database.findById(userId))
                   .map(User::getEmail);
}

// Creating Optionals
Optional<String> empty   = Optional.empty();
Optional<String> present = Optional.of("hello");       // throws if null
Optional<String> maybe   = Optional.ofNullable(value); // null becomes empty

// Using Optional
Optional<String> email = getUserEmail(1);

email.isPresent()                   // true if has value
email.isEmpty()                     // true if empty (Java 11+)
email.get()                         // get value — throws NoSuchElementException if empty
email.orElse("no-email@default.com") // get value or default
email.orElseGet(() -> generateEmail()) // get value or call supplier
email.orElseThrow(() -> new RuntimeException("No email")) // get or throw

email.ifPresent(e -> System.out.println("Email: " + e))  // only if present
email.map(String::toUpperCase)      // transform value if present
email.filter(e -> e.contains("@")) // keep value only if matches

// In automation context:
Optional<WebElement> loginButton = driver.findElements(By.id("loginBtn"))
    .stream().findFirst();
loginButton.ifPresent(WebElement::click);   // click only if found
```

---

**Q22: Explain Streams with a practical automation example.**

**A:**
```java
// Scenario: From a list of WebElements (table rows), 
// find all rows where status = "Active", extract the name, sort alphabetically

List<WebElement> rows = driver.findElements(By.tagName("tr"));

List<String> activeNames = rows.stream()
    .skip(1)                                    // skip header row
    .filter(row -> {
        List<WebElement> cells = row.findElements(By.tagName("td"));
        return cells.size() > 1 && 
               cells.get(1).getText().equals("Active");  // status column
    })
    .map(row -> row.findElements(By.tagName("td")).get(0).getText())  // name column
    .sorted()                                   // alphabetical
    .collect(Collectors.toList());

System.out.println("Active users: " + activeNames);

// Stream operations:
// INTERMEDIATE (lazy — run only when terminal called):
// filter(), map(), sorted(), distinct(), limit(), skip(), peek(), flatMap()

// TERMINAL (eager — triggers execution):
// collect(), forEach(), count(), findFirst(), anyMatch(), allMatch(), noneMatch(),
// reduce(), min(), max(), toArray()

// Parallel stream — use carefully, not always faster
List<String> processed = hugeList.parallelStream()
    .filter(s -> s.length() > 5)
    .collect(Collectors.toList());
// Good for CPU-intensive operations on large datasets
// Bad for: I/O operations, DB calls, small lists
```

---

## SECTION 6 — MULTITHREADING

---

**Q23: What is the difference between a process and a thread?**

**A:**
```
Process:
- Independent program running in its own memory space
- Has its own heap, stack, code, data
- Communication between processes is expensive (IPC)
- Example: Chrome browser = one process; each Chrome tab = separate process

Thread:
- Lightweight unit of execution within a process
- Shares the process's heap memory
- Has its own stack
- Communication is cheap (shared memory) but needs synchronisation
- Example: Maven test run = one process; each parallel test = one thread
```

---

**Q24: What is race condition and how do you prevent it?**

**A:**
```java
// Race condition — two threads modify shared data simultaneously → unpredictable result
public class Counter {
    private int count = 0;

    public void increment() {
        count++;   // NOT atomic! Three steps: read, increment, write
                   // Thread 1 reads 5, Thread 2 reads 5,
                   // Thread 1 writes 6, Thread 2 writes 6 — one increment lost!
    }
}

// Fix 1: synchronized method
public class Counter {
    private int count = 0;
    
    public synchronized void increment() {   // only one thread at a time
        count++;
    }
}

// Fix 2: AtomicInteger (faster than synchronized for single variable)
public class Counter {
    private AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();   // atomic — thread-safe by design
    }
}

// Fix 3: synchronized block (finer control)
private final Object lock = new Object();
public void increment() {
    synchronized (lock) {
        count++;
    }
}

// In automation: race condition in screenshot naming
// Thread 1 and Thread 2 both try to write "failure.png" simultaneously → corruption
// Fix: use thread ID in filename
String filename = "FAIL_" + Thread.currentThread().getId() + "_" + System.currentTimeMillis() + ".png";
```

---

**Q25: What is `volatile` and when do you use it?**

**A:**
```java
// Problem: each thread caches variable values for performance
// Thread 1 changes isRunning to false, but Thread 2 reads its CACHED true value

// Without volatile — Thread 2 might never see the update
private boolean isRunning = true;

// With volatile — forces read from main memory, not thread's cache
private volatile boolean isRunning = true;

// Use volatile when:
// - Variable read by multiple threads, written by one
// - Simple flag or status that doesn't need compound operations

// volatile does NOT make compound operations atomic (use AtomicBoolean for that)
// volatile count++ is NOT safe! Use AtomicInteger.incrementAndGet() instead
```

---

## SECTION 7 — DESIGN PATTERNS Q&A

---

**Q26: Explain the Singleton pattern. Is it thread-safe?**

**A:**
```java
// BASIC Singleton — NOT thread-safe
public class ConfigReader {
    private static ConfigReader instance;
    
    private ConfigReader() { loadConfig(); }
    
    public static ConfigReader getInstance() {
        if (instance == null) {               // Thread 1 and Thread 2 both pass this check
            instance = new ConfigReader();     // BOTH create instances!
        }
        return instance;
    }
}

// THREAD-SAFE Singleton 1: synchronized method (slow — every call acquires lock)
public static synchronized ConfigReader getInstance() {
    if (instance == null) { instance = new ConfigReader(); }
    return instance;
}

// THREAD-SAFE Singleton 2: double-checked locking (fast after first creation)
private static volatile ConfigReader instance;   // volatile important!

public static ConfigReader getInstance() {
    if (instance == null) {                         // first check — no lock
        synchronized (ConfigReader.class) {
            if (instance == null) {                 // second check — with lock
                instance = new ConfigReader();
            }
        }
    }
    return instance;
}

// BEST Singleton 3: initialization-on-demand holder (simplest thread-safe)
public class ConfigReader {
    private ConfigReader() { }
    
    private static class Holder {
        static final ConfigReader INSTANCE = new ConfigReader();  // created once when class loaded
    }
    
    public static ConfigReader getInstance() {
        return Holder.INSTANCE;
    }
}

// In automation:
String url = ConfigReader.getInstance().get("base_url");
```

---

**Q27: Explain the Page Object Model pattern and why it's used in automation.**

**A:**
```java
// WITHOUT POM — locators scattered, tests tightly coupled to UI
@Test
public void loginTest() {
    driver.findElement(By.id("username")).sendKeys("admin");     // locator in test
    driver.findElement(By.id("password")).sendKeys("Admin@123"); // locator in test
    driver.findElement(By.id("loginBtn")).click();               // locator in test
    Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"));
}
// If #username changes to #user_input → update EVERY test file!

// WITH POM — locators in page class, tests read like user stories
public class LoginPage {
    @FindBy(id = "username") private WebElement usernameField;
    @FindBy(id = "password") private WebElement passwordField;
    @FindBy(id = "loginBtn")  private WebElement loginButton;
    @FindBy(css = ".error-msg") private WebElement errorMessage;
    
    private WebDriver driver;
    
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    public DashboardPage login(String username, String password) {
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
        return new DashboardPage(driver);
    }
    
    public String getError() { return errorMessage.getText(); }
}

@Test
public void loginTest() {
    LoginPage login = new LoginPage(driver);
    DashboardPage dashboard = login.login("admin", "Admin@123");
    Assert.assertTrue(dashboard.isLoaded());
}
// If #username changes → only update LoginPage.java — all tests fixed automatically

// Benefits:
// 1. Maintainability — one place to update locators
// 2. Readability — tests describe behaviour, not UI details
// 3. Reusability — login() method reused by 50 tests
// 4. Separation of concerns — page code vs test logic
```

---

## SECTION 8 — QUICK-FIRE ROUND

---

**Q28: What is the difference between `int` and `Integer`?**
```
int = primitive, stored on stack, cannot be null, default 0, faster
Integer = wrapper class (Object), stored on heap, CAN be null, has methods like parseInt()
Use int when: performance matters, null not needed
Use Integer when: working with Collections (List<Integer>), null is valid, need methods
```

**Q29: What is the difference between `Array` and `ArrayList`?**
```
Array: fixed size, can hold primitives + objects, no built-in methods, faster
ArrayList: dynamic size, objects only (primitives autoboxed), rich API (add/remove/contains)
int[] arr = {1,2,3};  vs  List<Integer> list = new ArrayList<>();
```

**Q30: What is a `NullPointerException` and how do you avoid it?**
```java
String s = null;
s.length();   // NullPointerException — calling method on null reference

// Prevention:
if (s != null) s.length();                         // null check
Optional.ofNullable(s).map(String::length);         // Optional
Objects.requireNonNull(s, "s cannot be null");      // fail fast with clear message
String safe = (s != null) ? s : "";                 // default value
```

**Q31: What is `instanceof` operator?**
```java
Object obj = "hello";
if (obj instanceof String) {
    String s = (String) obj;    // safe to cast
    System.out.println(s.length());
}

// Java 16+ pattern matching
if (obj instanceof String s) {   // cast + assign in one
    System.out.println(s.length());
}
```

**Q32: What is `varargs`?**
```java
// Variable number of arguments — treated as array inside method
public void log(String level, String... messages) {
    for (String msg : messages) {
        System.out.println("[" + level + "] " + msg);
    }
}

log("INFO", "Test started");
log("ERROR", "Step 1 failed", "Screenshot taken", "Test aborted");
// Varargs must be LAST parameter
```

**Q33: What is the difference between `break`, `continue`, and `return`?**
```java
for (int i = 0; i < 10; i++) {
    if (i == 3) continue;   // skip i=3, continue loop
    if (i == 7) break;      // stop loop entirely at i=7
    System.out.println(i);  // prints: 0,1,2,4,5,6
}

// return — exits the current METHOD
public String findStatus(int code) {
    if (code == 200) return "OK";         // exits method immediately
    if (code == 404) return "Not Found";
    return "Unknown";
}
```

**Q34: What is a deep copy vs shallow copy?**
```java
// Shallow copy — copies references, not nested objects
int[] original = {1, 2, 3};
int[] shallow = original;         // same array — modifying shallow modifies original!
shallow[0] = 99;
System.out.println(original[0]);  // 99 — original changed!

// Deep copy — new independent object
int[] deep = original.clone();    // new array with same values
deep[0] = 99;
System.out.println(original[0]);  // 1 — original unchanged

// For objects — must implement Cloneable or copy constructor
// In automation: copy test data maps to avoid shared state between tests
Map<String, String> testData = new HashMap<>(originalData);  // shallow copy of map
```

**Q35: What is the `Comparable` vs `Comparator` interface?**
```java
// Comparable — class defines its OWN natural ordering
public class TestResult implements Comparable<TestResult> {
    private String name;
    private long duration;
    
    @Override
    public int compareTo(TestResult other) {
        return Long.compare(this.duration, other.duration);  // sort by duration
    }
}
Collections.sort(results);   // uses compareTo() automatically

// Comparator — EXTERNAL ordering, more flexible
Comparator<TestResult> byName = Comparator.comparing(TestResult::getName);
Comparator<TestResult> byDuration = Comparator.comparingLong(TestResult::getDuration).reversed();
Comparator<TestResult> combined = byName.thenComparing(byDuration);

results.sort(byDuration);    // sort externally
results.stream().sorted(Comparator.comparing(TestResult::getName)).collect(Collectors.toList());
```

---

## SECTION 9 — JAVA IN AUTOMATION Q&A

---

**Q36: How do you read a property from a `.properties` file in Java?**
```java
public class ConfigReader {
    private Properties props = new Properties();
    
    public ConfigReader(String filePath) {
        try (InputStream is = new FileInputStream(filePath)) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load: " + filePath, e);
        }
    }
    
    public String get(String key) {
        return props.getProperty(key);
    }
    
    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}

// config.properties
// base_url=https://jsonplaceholder.typicode.com
// browser=chrome
// timeout=30

ConfigReader config = new ConfigReader("src/test/resources/config.properties");
String url     = config.get("base_url");
String browser = config.get("browser", "chrome");  // default if missing
int timeout    = Integer.parseInt(config.get("timeout", "30"));
```

---

**Q37: How do you read data from a CSV file for data-driven testing?**
```java
public class CsvReader {
    public static Object[][] readCsv(String filePath) throws IOException {
        List<Object[]> rows = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();   // skip header row
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                rows.add(values);
            }
        }
        return rows.toArray(new Object[0][]);
    }
}

// testdata.csv:
// username,password,expectedResult
// admin,Admin@123,Dashboard
// user1,User@123,Home
// wrong,badpass,Error

@DataProvider(name = "loginData")
public Object[][] loginData() throws IOException {
    return CsvReader.readCsv("src/test/resources/testdata/loginData.csv");
}

@Test(dataProvider = "loginData")
public void loginTest(String username, String password, String expectedResult) {
    loginPage.login(username, password);
    Assert.assertTrue(driver.getPageSource().contains(expectedResult));
}
```

---

**Q38: What is reflection and how is it used in test automation frameworks?**
```java
// Reflection — inspect and manipulate classes, methods, fields at runtime
// Used by TestNG, JUnit, Spring, Mockito internally

// Get class info
Class<?> clazz = LoginPage.class;
System.out.println(clazz.getName());         // "pages.LoginPage"
System.out.println(clazz.getSuperclass());   // "base.BasePage"

// Get all methods
Method[] methods = clazz.getDeclaredMethods();
for (Method m : methods) {
    System.out.println(m.getName() + " — " + m.getReturnType());
}

// Read annotations at runtime (how TestNG finds @Test methods)
for (Method m : clazz.getMethods()) {
    if (m.isAnnotationPresent(Test.class)) {
        Test testAnnotation = m.getAnnotation(Test.class);
        System.out.println("Test found: " + m.getName() +
                           " groups=" + Arrays.toString(testAnnotation.groups()));
    }
}

// Invoke a method dynamically
Method loginMethod = clazz.getMethod("login", String.class, String.class);
LoginPage page = new LoginPage(driver);
loginMethod.invoke(page, "admin", "Admin@123");
```

---

**Q39: What is generics and how does it help in test automation?**
```java
// Without generics — unsafe, requires casting everywhere
public class DataStore {
    private Object value;
    public void set(Object value) { this.value = value; }
    public Object get() { return value; }
}

DataStore store = new DataStore();
store.set("hello");
String s = (String) store.get();   // must cast, ClassCastException if wrong type

// With generics — type-safe, no casting
public class DataStore<T> {
    private T value;
    public void set(T value) { this.value = value; }
    public T get() { return value; }
}

DataStore<String> stringStore = new DataStore<>();
stringStore.set("hello");
String s = stringStore.get();   // no cast — compiler knows it's a String

DataStore<Integer> intStore = new DataStore<>();
intStore.set(42);
int n = intStore.get();         // auto-unboxed correctly

// Practical automation use: generic wait utility
public <T> T waitFor(Supplier<T> condition, int timeoutSeconds) {
    long end = System.currentTimeMillis() + timeoutSeconds * 1000L;
    while (System.currentTimeMillis() < end) {
        T result = condition.get();
        if (result != null) return result;
        Thread.sleep(500);
    }
    throw new RuntimeException("Condition not met within " + timeoutSeconds + "s");
}

// Usage:
WebElement button = waitFor(() -> driver.findElement(By.id("submit")), 10);
String text = waitFor(() -> {
    String t = driver.findElement(By.id("status")).getText();
    return t.equals("Loaded") ? t : null;
}, 30);
```

---

**Q40: What are the most common Java mistakes in automation code?**
```java
// 1. Using == for String comparison
if (driver.getTitle() == "Login")   // WRONG — always false for Strings
if (driver.getTitle().equals("Login"))  // CORRECT

// 2. Not handling StaleElementReferenceException
WebElement el = driver.findElement(By.id("btn"));
page.refresh();
el.click();   // StaleElementReferenceException!
// Fix: find again after DOM changes

// 3. Using Thread.sleep instead of waits
Thread.sleep(3000);  // fixed delay — slow and unreliable
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));  // correct

// 4. Not closing streams/connections
InputStream is = new FileInputStream("data.csv");
// ... forgot to close → resource leak
// Fix: try-with-resources
try (InputStream is = new FileInputStream("data.csv")) { ... }

// 5. Modifying list while iterating
for (String item : list) {
    if (condition) list.remove(item);  // ConcurrentModificationException!
}
// Fix: use Iterator or removeIf()
list.removeIf(item -> condition);

// 6. Ignoring exceptions silently
try {
    riskyOperation();
} catch (Exception e) {
    // empty catch — bug swallowed, test may pass incorrectly!
}
// Fix: always log or rethrow

// 7. Static WebDriver (not ThreadLocal) for parallel tests
private static WebDriver driver;  // all parallel threads share one browser!
private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();  // correct

// 8. Not releasing ThreadLocal
@AfterMethod
public void tearDown() {
    getDriver().quit();
    driver.remove();   // MUST remove to prevent memory leak
}
```
