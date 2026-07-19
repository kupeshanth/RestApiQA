# Core Java — Full Interview Q&A (Expanded Edition)
## 55 Questions | Full Answers | Code Examples | Automation Context

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
// This is runtime polymorphism.
```

---

**4. Abstraction** — hide complexity, show only what matters.

```java
// BasePage hides HOW navigation works
public abstract class BasePage {
    protected WebDriver driver;

    public abstract boolean isLoaded();   // subclass MUST implement

    public void navigateTo(String path) { // hidden HOW — tests just call this
        driver.get(System.getProperty("base.url") + path);
    }
}

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
    }
}

// OVERRIDING — parent and child, SAME signature
// Resolved at RUNTIME (dynamic binding)
public class Animal {
    public String speak() { return "..."; }
}

public class Dog extends Animal {
    @Override
    public String speak() { return "Woof!"; }
}

Animal a = new Dog();   // Dog stored as Animal reference
a.speak();              // returns "Woof!" — runtime decides based on actual type
```

| | Overloading | Overriding |
|--|------------|-----------|
| Location | Same class | Parent + Child |
| Signature | Different params | Identical |
| When decided | Compile time | Runtime |
| `@Override` | No | Yes (recommended) |

---

**Q3: What is the difference between an interface and an abstract class?**

**A:**

```java
// INTERFACE — defines WHAT a class must do (contract)
public interface Clickable {
    void click();                    // abstract — must implement
    void doubleClick();

    default void highlight() {       // default — optional override (Java 8+)
        System.out.println("Highlighted");
    }
}

// ABSTRACT CLASS — partial implementation + contract
public abstract class BasePage {
    protected WebDriver driver;      // can have fields

    public BasePage(WebDriver driver) {   // can have constructor
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public abstract boolean isLoaded();  // must implement

    public void navigateTo(String url) { // can implement shared behaviour
        driver.get(url);
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
- Use **interface** when: defining capability/contract that unrelated classes might implement
- Use **abstract class** when: sharing code between closely related classes (BasePage, BaseTest)

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

    public String describe() { return "Animal: " + name; }
}

public class Dog extends Animal {
    private String breed;

    public Dog(String name, String breed) {
        super(name);    // 'super' calls parent constructor — MUST be first line
        this.breed = breed;
    }

    @Override
    public String describe() {
        String parentDesc = super.describe();  // call parent's method
        return parentDesc + " | Breed: " + breed;
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

TestConfig config1 = new TestConfig();                               // all defaults
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
sb.delete(1, 3);      // "0X234"
sb.reverse();         // "432X0"

// StringBuffer — MUTABLE, thread-safe (synchronized), SLOWER than StringBuilder
StringBuffer sbuf = new StringBuffer();
// Use only when multiple threads modify the same string

// Rule: String for fixed values, StringBuilder for building in single thread,
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

// .equals() compares CONTENT
a.equals(b)   // true
"Hello".equalsIgnoreCase("hello")  // true — case-insensitive

// .compareTo() — lexicographic comparison (used for sorting)
"apple".compareTo("banana")   // negative — "apple" comes before "banana"
"banana".compareTo("apple")   // positive — "banana" comes after "apple"
"apple".compareTo("apple")    // 0 — identical

// In automation: always use .equals() to compare URL, page title, element text
String actualTitle = driver.getTitle();
Assert.assertEquals(actualTitle, "Dashboard");  // uses .equals() internally
```

---

**Q9: What is the difference between `final`, `finally`, and `finalize()`?**

**A:**
```java
// FINAL — modifier with 3 uses:
final int MAX_RETRY = 3;            // variable cannot be reassigned
final class ImmutableConfig { }     // class cannot be extended

class BaseTest {
    public final void setUp() { }   // method cannot be overridden by subclass
}

// FINALLY — block that ALWAYS runs in try-catch
@AfterMethod
public void tearDown() {
    try {
        performCleanup();
    } catch (Exception e) {
        System.err.println("Cleanup failed: " + e.getMessage());
    } finally {
        driver.quit();  // runs REGARDLESS of exception — perfect for cleanup
    }
}

// FINALIZE() — deprecated. Called by GC before collecting object.
// NEVER use this. GC timing is unpredictable. Use finally or try-with-resources.
@Override
protected void finalize() throws Throwable {
    // DON'T USE — deprecated since Java 9, removed in Java 18
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
int x = value;    // NullPointerException! Can't unbox null

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
    }

    try {
        loadUserData(username);
    } catch (IOException e) {
        throw new RuntimeException("Failed to load user data", e);  // wrap in unchecked
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

// Execution order: static block → instance block → constructor
public class Demo {
    static { System.out.println("1. Static block"); }           // first, once
    { System.out.println("2. Instance block"); }                // before constructor
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
LinkedList<String> ll = new LinkedList<>();
ll.addFirst("item");   // O(1) — fast at head/tail

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
// Iteration order: unpredictable

// LinkedHashMap — maintains INSERTION order
Map<String, Integer> linkedMap = new LinkedHashMap<>();
linkedMap.put("step1", 1);
linkedMap.put("step2", 2);
linkedMap.put("step3", 3);
// Iteration: step1, step2, step3 (insertion order)

// TreeMap — sorted by KEY in natural order (or custom Comparator)
Map<String, Integer> treeMap = new TreeMap<>();
treeMap.put("banana", 2);
treeMap.put("apple", 1);
treeMap.put("cherry", 3);
// Iteration: apple, banana, cherry (alphabetical)

// Automation use:
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
// Iteration order = order elements were added

// TreeSet — no duplicates, SORTED order, O(log n)
Set<String> treeSet = new TreeSet<>();
treeSet.add("chrome"); treeSet.add("firefox"); treeSet.add("safari");
// Iteration: chrome, firefox, safari (alphabetical)
treeSet.first();   // "chrome"
treeSet.last();    // "safari"

// Automation use case:
Set<String> visitedUrls = new HashSet<>();  // track visited pages, no duplicates
driver.findElements(By.tagName("a"))
    .stream()
    .map(e -> e.getAttribute("href"))
    .forEach(visitedUrls::add);
System.out.println("Unique URLs: " + visitedUrls.size());
```

---

**Q16: How does HashMap work internally?**

**A:**
```
HashMap uses an array of buckets (linked list/tree per bucket).

1. When you call put("key", value):
   - Java calls key.hashCode() → gets a hash number
   - Maps hash to a bucket index: index = hash & (arrayLength - 1)
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

```java
// Must override both equals() AND hashCode() for custom keys
public class OrderKey {
    private String orderId;
    private String customerId;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OrderKey)) return false;
        OrderKey other = (OrderKey) obj;
        return Objects.equals(orderId, other.orderId) &&
               Objects.equals(customerId, other.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, customerId);
    }
}
```

---

**Q17: What is the difference between `Iterator` and `ListIterator`? What is ConcurrentModificationException?**

**A:**
```java
// Iterator — forward only, works on any Collection
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
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
    lit.set("X" + lit.next());   // replace current element
}

// ConcurrentModificationException — modify list during for-each iteration
for (String s : list) {
    if (s.equals("b")) list.remove(s);  // ConcurrentModificationException!
}

// Fix 1: Iterator.remove()
Iterator<String> iter = list.iterator();
while (iter.hasNext()) {
    if (iter.next().equals("b")) iter.remove();
}

// Fix 2: removeIf() — cleanest
list.removeIf(s -> s.equals("b"));
```

---

## SECTION 4 — EXCEPTION HANDLING

---

**Q18: What is the difference between checked and unchecked exceptions?**

**A:**
```java
// CHECKED — compiler forces you to handle or declare
public void loadFile(String path) throws IOException {
    FileReader fr = new FileReader(path);   // throws FileNotFoundException (checked)
}

// Common checked exceptions:
// IOException, FileNotFoundException, SQLException, ClassNotFoundException

// UNCHECKED (RuntimeException) — no requirement to declare or catch
public void click(WebElement el) {
    el.click();  // might throw NoSuchElementException — no 'throws' needed
}

// Common unchecked exceptions:
// NullPointerException, ArrayIndexOutOfBoundsException,
// NoSuchElementException (Selenium), TimeoutException, IllegalArgumentException

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
    throw new RuntimeException("DB connection failed during test setup", e);
    // Stack trace shows BOTH: RuntimeException + original SQLException + message
}

// Reading chained exceptions:
try {
    runTest();
} catch (Exception e) {
    System.out.println("Top: " + e.getMessage());
    System.out.println("Cause: " + e.getCause().getMessage());
    e.printStackTrace();  // shows full chain
}

// try-with-resources — auto-closes resources even if exception thrown
try (FileReader fr = new FileReader("data.csv");
     BufferedReader br = new BufferedReader(fr)) {
    String line;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }
}  // fr and br closed automatically
```

---

## SECTION 5 — JAVA 8 FEATURES

---

**Q20: What is a functional interface and how does it relate to lambdas?**

**A:**
```java
// Functional interface — exactly ONE abstract method
@FunctionalInterface
public interface Condition {
    boolean check(String input);

    default Condition and(Condition other) {
        return input -> this.check(input) && other.check(input);
    }
}

// Lambda — short implementation of a functional interface
Condition notEmpty = input -> !input.isEmpty();
Condition longEnough = input -> input.length() >= 8;
notEmpty.and(longEnough).check("hi");     // false

// Common built-in functional interfaces:
Predicate<String> isValid = s -> s != null && !s.isBlank();
Function<String, Integer> toLength = String::length;
Supplier<String> getDefault = () -> "default";
Consumer<String> print = System.out::println;
Runnable task = () -> performCleanup();
BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);

// In RestAssured:
given()
    .filter((req, resp, ctx) -> {      // lambda as filter
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
// Creating Optionals:
Optional<String> empty   = Optional.empty();
Optional<String> present = Optional.of("hello");       // throws if null
Optional<String> maybe   = Optional.ofNullable(value); // null becomes empty

// Using Optional — ALL methods:
email.isPresent()                    // true if has value
email.isEmpty()                      // true if empty (Java 11+)
email.get()                          // get value — throws if empty
email.orElse("default@mail.com")     // get value or default
email.orElseGet(() -> generateEmail()) // get value or call supplier (lazy)
email.orElseThrow(() -> new RuntimeException("No email"))

email.ifPresent(e -> System.out.println(e))           // only if present
email.ifPresentOrElse(e -> log(e), () -> log("none")) // Java 9+
email.map(String::toUpperCase)       // transform value if present
email.flatMap(e -> Optional.of(e))   // when mapper returns Optional
email.filter(e -> e.contains("@"))   // keep value only if matches
email.or(() -> Optional.of("alt"))   // Java 9+ — provide alternative Optional

// Chaining (common pattern):
Optional.ofNullable(getApiResponse())
    .map(resp -> resp.jsonPath().getString("orderId"))
    .filter(id -> id.startsWith("ORD-"))
    .ifPresent(id -> verifyOrder(id));

// In automation:
Optional<WebElement> loginBtn = driver.findElements(By.id("loginBtn")).stream().findFirst();
loginBtn.ifPresent(WebElement::click);  // click only if found
```

---

**Q22: Explain Streams with intermediate vs terminal operations.**

**A:**
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// INTERMEDIATE OPERATIONS (lazy — don't run until terminal is called):
// filter(), map(), flatMap(), sorted(), distinct(), limit(), skip(), peek()

List<Integer> result = numbers.stream()
    .filter(n -> n % 2 == 0)          // keep evens: [2,4,6,8,10]
    .map(n -> n * n)                   // square each: [4,16,36,64,100]
    .sorted(Comparator.reverseOrder()) // reverse: [100,64,36,16,4]
    .limit(3)                          // take 3: [100,64,36]
    .collect(Collectors.toList());

// TERMINAL OPERATIONS (eager — trigger execution, consume the stream):
// collect(), forEach(), count(), findFirst(), findAny(),
// anyMatch(), allMatch(), noneMatch(), reduce(), min(), max(), toArray()

long count    = numbers.stream().filter(n -> n > 5).count();        // 5
boolean any   = numbers.stream().anyMatch(n -> n > 9);              // true
boolean all   = numbers.stream().allMatch(n -> n > 0);              // true
Optional<Integer> first = numbers.stream().filter(n -> n > 7).findFirst();  // Optional[8]
int sum = numbers.stream().reduce(0, Integer::sum);                 // 55
String joined = numbers.stream().map(String::valueOf).collect(Collectors.joining(", ")); // "1, 2, ..."
Map<Boolean, List<Integer>> partitioned = numbers.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0)); // {true=[2,4,6,8,10], false=[1,3,...]}

// Automation example: filter WebElements
List<String> enabledBtns = driver.findElements(By.tagName("button"))
    .stream()
    .filter(WebElement::isEnabled)
    .map(WebElement::getText)
    .distinct()
    .sorted()
    .collect(Collectors.toList());
```

---

## SECTION 6 — MULTITHREADING

---

**Q23: What is the difference between a process and a thread?**

**A:**
```
Process:
- Independent program with its own memory space (heap, stack, code)
- Communication between processes is expensive (IPC, sockets)
- Crash in one process doesn't affect others
- Example: Maven = one process; Chrome browser = one process

Thread:
- Lightweight unit of execution within a process
- Shares the process heap (objects accessible to all threads)
- Has its own stack (local variables, call stack)
- Communication is cheap but needs synchronisation
- Crash in one thread (uncaught exception) can kill the process
- Example: each parallel test = one thread inside the Maven process
```

---

**Q24: What is race condition and how do you prevent it?**

**A:**
```java
// Race condition — two threads modify shared data simultaneously → unpredictable result
public class Counter {
    private int count = 0;

    public void increment() {
        count++;   // NOT atomic! Three ops: read, increment, write
                   // Thread 1 reads 5, Thread 2 reads 5 → both write 6 → one increment lost!
    }
}

// Fix 1: synchronized method
public class Counter {
    private int count = 0;
    public synchronized void increment() { count++; }
}

// Fix 2: AtomicInteger (faster for single variable)
private AtomicInteger count = new AtomicInteger(0);
public void increment() { count.incrementAndGet(); }

// Fix 3: synchronized block (finer control)
private final Object lock = new Object();
public void increment() {
    synchronized (lock) { count++; }
}

// Automation: race condition in screenshot file naming
// Thread 1 and Thread 2 both write "failure.png" → corruption
// Fix: use thread ID in filename
String name = "FAIL_" + Thread.currentThread().getId() + "_" + System.nanoTime() + ".png";
```

---

**Q25: What is `volatile` and when do you use it?**

**A:**
```java
// Problem: each thread caches variable values — changes not visible to other threads

// Without volatile — Thread 2 might never see Thread 1's update
private boolean isRunning = true;

// With volatile — all threads read from main memory, not thread-local cache
private volatile boolean isRunning = true;

// volatile does NOT make compound operations atomic!
// volatile count++ is STILL a race condition (three steps)
// Use AtomicInteger for that

// Correct use case: status flag written by one thread, read by many
private volatile boolean stopRequested = false;

public void stop() {
    stopRequested = true;   // written by shutdown thread
}

public void run() {
    while (!stopRequested) {   // read by worker thread — volatile ensures visibility
        doWork();
    }
}
```

---

## SECTION 7 — DESIGN PATTERNS Q&A

---

**Q26: Explain the Singleton pattern. Is it thread-safe?**

**A:**
```java
// BASIC — NOT thread-safe
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

// Thread-Safe 1: synchronized method (slow — every call acquires lock)
public static synchronized ConfigReader getInstance() {
    if (instance == null) { instance = new ConfigReader(); }
    return instance;
}

// Thread-Safe 2: double-checked locking (fast after first creation)
private static volatile ConfigReader instance;

public static ConfigReader getInstance() {
    if (instance == null) {
        synchronized (ConfigReader.class) {
            if (instance == null) {
                instance = new ConfigReader();
            }
        }
    }
    return instance;
}

// Thread-Safe 3: initialization-on-demand holder (BEST — simplest, lazy, no sync needed)
public class ConfigReader {
    private ConfigReader() { }

    private static class Holder {
        static final ConfigReader INSTANCE = new ConfigReader();
    }

    public static ConfigReader getInstance() {
        return Holder.INSTANCE;
    }
}

String url = ConfigReader.getInstance().get("base_url");
```

---

**Q27: Explain the Page Object Model pattern and why it's used in automation.**

**A:**
```java
// WITHOUT POM — locators scattered everywhere
@Test
public void loginTest() {
    driver.findElement(By.id("username")).sendKeys("admin");
    driver.findElement(By.id("password")).sendKeys("Admin@123");
    driver.findElement(By.id("loginBtn")).click();
    // If id="username" changes to id="user_input" → update EVERY test file!
}

// WITH POM — locators in one place
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
// id="username" changes → only update LoginPage.java — all tests fixed automatically

// Benefits: Maintainability, Readability, Reusability, Separation of concerns
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
if (s != null) s.length();
Optional.ofNullable(s).map(String::length);
Objects.requireNonNull(s, "s cannot be null");
String safe = (s != null) ? s : "";
```

**Q31: What is `instanceof` operator?**
```java
Object obj = "hello";
if (obj instanceof String) {
    String s = (String) obj;    // traditional: check then cast
}

// Java 16+ pattern matching — check and assign in one step
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

**Q32: What is `varargs`?**
```java
public void log(String level, String... messages) {  // varargs: zero or more
    for (String msg : messages) System.out.println("[" + level + "] " + msg);
}

log("INFO");                               // zero messages
log("INFO", "Test started");              // one message
log("ERROR", "Failed", "Screenshot taken"); // multiple messages
// varargs must be LAST parameter
```

**Q33: What is the difference between `break`, `continue`, and `return`?**
```java
for (int i = 0; i < 10; i++) {
    if (i == 3) continue;   // skip i=3, continue loop → prints 0,1,2,4,5,6
    if (i == 7) break;      // stop loop entirely at i=7
    System.out.println(i);
}

// return — exits the current METHOD entirely
public String findStatus(int code) {
    if (code == 200) return "OK";
    if (code == 404) return "Not Found";
    return "Unknown";
}
```

**Q34: What is a deep copy vs shallow copy?**
```java
// Shallow copy — copies references, not nested objects
int[] original = {1, 2, 3};
int[] shallow = original;        // same array!
shallow[0] = 99;
System.out.println(original[0]); // 99 — original changed!

// Deep copy — new independent object
int[] deep = original.clone();   // new array with same values
deep[0] = 99;
System.out.println(original[0]); // 1 — original unchanged

// Map deep copy (shallow — copies map structure but not nested objects)
Map<String, String> copy = new HashMap<>(originalData);
```

**Q35: What is the `Comparable` vs `Comparator` interface?**
```java
// Comparable — class defines its OWN natural ordering
public class TestResult implements Comparable<TestResult> {
    private long duration;

    @Override
    public int compareTo(TestResult other) {
        return Long.compare(this.duration, other.duration);
    }
}
Collections.sort(results);   // uses compareTo() automatically

// Comparator — EXTERNAL ordering, more flexible, multiple sort orders
Comparator<TestResult> byName = Comparator.comparing(TestResult::getName);
Comparator<TestResult> byDuration = Comparator.comparingLong(TestResult::getDuration).reversed();
results.sort(byName.thenComparing(byDuration));
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

    public String get(String key)                 { return props.getProperty(key); }
    public String get(String key, String def)     { return props.getProperty(key, def); }
    public int    getInt(String key, int def)     {
        String val = props.getProperty(key);
        return val != null ? Integer.parseInt(val) : def;
    }
}

// config.properties:
// base_url=https://jsonplaceholder.typicode.com
// browser=chrome
// timeout=30

ConfigReader config = new ConfigReader("src/test/resources/config.properties");
String url     = config.get("base_url");
String browser = config.get("browser", "chrome");  // default if key missing
int timeout    = config.getInt("timeout", 30);
```

---

**Q37: How do you read data from a CSV file for data-driven testing?**
```java
public class CsvReader {
    public static Object[][] readCsv(String filePath) throws IOException {
        List<Object[]> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine();   // skip header row
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    rows.add(line.split(","));
                }
            }
        }
        return rows.toArray(new Object[0][]);
    }
}

// testdata/loginData.csv:
// username,password,expectedResult
// admin,Admin@123,Dashboard
// user1,User@123,Home
// wrong,badpass,Error

@DataProvider(name = "loginData")
public Object[][] loginData() throws IOException {
    return CsvReader.readCsv("src/test/resources/testdata/loginData.csv");
}

@Test(dataProvider = "loginData")
public void loginTest(String username, String password, String expected) {
    loginPage.login(username, password);
    assertTrue(driver.getPageSource().contains(expected));
}
```

---

**Q38: What is reflection and how is it used in test automation frameworks?**
```java
// Reflection — inspect and manipulate classes, methods, fields at runtime
// Used by TestNG, JUnit, Spring, Mockito internally to find @Test methods

// Get class info
Class<?> clazz = LoginPage.class;
System.out.println(clazz.getName());         // "pages.LoginPage"
System.out.println(clazz.getSuperclass());   // "base.BasePage"

// Get all methods
Method[] methods = clazz.getDeclaredMethods();
for (Method m : methods) {
    System.out.println(m.getName() + " — " + m.getReturnType().getSimpleName());
}

// Read annotations at runtime (how TestNG finds @Test methods)
for (Method m : clazz.getMethods()) {
    if (m.isAnnotationPresent(Test.class)) {
        Test testAnnotation = m.getAnnotation(Test.class);
        System.out.println("Test: " + m.getName() +
                           " groups=" + Arrays.toString(testAnnotation.groups()));
    }
}

// Invoke a method dynamically
Method loginMethod = clazz.getMethod("login", String.class, String.class);
LoginPage page = new LoginPage(driver);
loginMethod.invoke(page, "admin", "Admin@123");

// Access private field (testing purposes only)
Field field = clazz.getDeclaredField("usernameField");
field.setAccessible(true);   // bypass private modifier
WebElement element = (WebElement) field.get(page);
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
String s = stringStore.get();   // no cast needed

// Bounded generics — restrict to Number subclasses
public <T extends Number> double sumList(List<T> list) {
    return list.stream().mapToDouble(Number::doubleValue).sum();
}

// Wildcards
public void printList(List<?> list) { list.forEach(System.out::println); }   // any type
public void addNumbers(List<? extends Number> list) { /* read only */ }      // upper bound
public void addToList(List<? super Integer> list) { list.add(42); }          // lower bound

// Practical: generic wait utility
public <T> T waitFor(Supplier<T> condition, int timeoutSec) throws InterruptedException {
    long end = System.currentTimeMillis() + timeoutSec * 1000L;
    while (System.currentTimeMillis() < end) {
        T result = condition.get();
        if (result != null) return result;
        Thread.sleep(500);
    }
    throw new RuntimeException("Condition not met within " + timeoutSec + "s");
}

WebElement btn = waitFor(() -> driver.findElement(By.id("submit")), 10);
```

---

**Q40: What are the most common Java mistakes in automation code?**
```java
// 1. Using == for String comparison
if (driver.getTitle() == "Login")         // WRONG — always false for Strings
if (driver.getTitle().equals("Login"))    // CORRECT

// 2. Not handling StaleElementReferenceException
WebElement el = driver.findElement(By.id("btn"));
page.refresh();
el.click();   // StaleElementReferenceException — element reference is stale
// Fix: find element again after page changes

// 3. Using Thread.sleep instead of proper waits
Thread.sleep(3000);  // fixed delay — slow and unreliable
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("result")));  // correct

// 4. Not closing streams/connections
InputStream is = new FileInputStream("data.csv");
// forgot to close → resource leak
// Fix: try-with-resources
try (InputStream is = new FileInputStream("data.csv")) { ... }

// 5. Modifying list while iterating
for (String item : list) {
    if (condition) list.remove(item);  // ConcurrentModificationException!
}
// Fix:
list.removeIf(item -> condition);

// 6. Swallowing exceptions silently
try { riskyOperation(); } catch (Exception e) { }  // bug hidden!
// Fix: always log or rethrow

// 7. Static WebDriver (not ThreadLocal) for parallel tests
private static WebDriver driver;  // all parallel threads share ONE browser!
private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();  // correct

// 8. Not removing ThreadLocal after test
@AfterMethod
public void tearDown() {
    getDriver().quit();
    driver.remove();   // MUST remove — prevents memory leak between tests
}
```

---

## SECTION 10 — GENERICS, REFLECTION, AND MODERN JAVA

---

**Q41: What are the four types of method references in Java?**

**A:**

Method references are shorthand for lambdas that call a single method. There are four types:

```java
// TYPE 1: Static method reference — ClassName::staticMethod
// Lambda: n -> Integer.parseInt(n)
Function<String, Integer> parser = Integer::parseInt;
parser.apply("42");  // 42

List<String> strs = Arrays.asList("3", "1", "4", "1", "5");
strs.stream().map(Integer::parseInt).sorted().forEach(System.out::println);

// TYPE 2: Instance method reference on a PARTICULAR object — instance::method
String prefix = "ORDER-";
// Lambda: s -> prefix.concat(s)
Function<String, String> addPrefix = prefix::concat;
addPrefix.apply("001");  // "ORDER-001"

PrintStream out = System.out;
Consumer<String> printer = out::println;  // System.out::println

// TYPE 3: Instance method reference on an ARBITRARY object of a type — Type::instanceMethod
// Lambda: s -> s.toUpperCase()
Function<String, String> upper = String::toUpperCase;
List<String> results = strs.stream().map(String::toUpperCase).collect(Collectors.toList());

// Lambda: (a, b) -> a.compareTo(b)
Comparator<String> compare = String::compareTo;

// In automation:
List<String> texts = elements.stream().map(WebElement::getText).collect(Collectors.toList());
boolean anyEnabled = elements.stream().anyMatch(WebElement::isEnabled);

// TYPE 4: Constructor reference — ClassName::new
// Lambda: () -> new ArrayList<>()
Supplier<List<String>> listMaker = ArrayList::new;
List<String> newList = listMaker.get();

// Lambda: s -> new StringBuilder(s)
Function<String, StringBuilder> sbMaker = StringBuilder::new;
StringBuilder sb = sbMaker.apply("hello");
```

---

**Q42: What are Java Streams intermediate vs terminal operators in detail?**

**A:**

```java
// ALL INTERMEDIATE OPERATORS (lazy — return Stream, do not execute):
// filter(Predicate)          — keep elements matching condition
// map(Function)              — transform each element to new type
// mapToInt/Long/Double        — transform to primitive stream (no boxing)
// flatMap(Function)          — transform each element to Stream, then flatten
// distinct()                 — remove duplicates (uses equals/hashCode)
// sorted()                   — natural order sort
// sorted(Comparator)         — custom sort
// limit(long n)              — take at most n elements
// skip(long n)               — skip first n elements
// peek(Consumer)             — side effect, pass-through (debug only)
// takeWhile(Predicate)       — Java 9+: keep while condition true, stop at first false
// dropWhile(Predicate)       — Java 9+: drop while true, keep rest

// ALL TERMINAL OPERATORS (eager — consume Stream, return result):
// collect(Collector)         — gather into List, Set, Map, String
// forEach(Consumer)          — iterate, side effect only
// count()                    — count elements
// findFirst()                — Optional of first element
// findAny()                  — Optional of any element (faster for parallel)
// anyMatch(Predicate)        — true if any element matches
// allMatch(Predicate)        — true if all elements match
// noneMatch(Predicate)       — true if no elements match
// reduce(identity, BinaryOperator) — fold into single value
// min(Comparator)            — Optional of minimum
// max(Comparator)            — Optional of maximum
// toArray()                  — convert to Object[] or typed array
// iterator()                 — get Iterator (for manual iteration)
// sum() / average() / summaryStatistics() — on IntStream/LongStream/DoubleStream

// Examples:
IntStream.rangeClosed(1, 10)
    .filter(n -> n % 2 == 0)
    .map(n -> n * n)
    .sum();  // 4+16+36+64+100 = 220

Map<String, List<String>> groupedByStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::getStatus));

Map<String, Long> countByStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

IntSummaryStatistics stats = orders.stream()
    .mapToInt(Order::getItemCount)
    .summaryStatistics();
System.out.println("Min: " + stats.getMin() + ", Max: " + stats.getMax() +
                   ", Avg: " + stats.getAverage());
```

---

**Q43: What is Optional chaining and how do you avoid NullPointerException with it?**

**A:**

```java
// Without Optional — multiple null checks, verbose
public String getCustomerCity(Order order) {
    if (order == null) return "Unknown";
    Customer customer = order.getCustomer();
    if (customer == null) return "Unknown";
    Address address = customer.getAddress();
    if (address == null) return "Unknown";
    return address.getCity();
}

// With Optional chaining — clean, no null checks
public String getCustomerCity(Order order) {
    return Optional.ofNullable(order)
        .map(Order::getCustomer)      // if order present, get customer
        .map(Customer::getAddress)    // if customer present, get address
        .map(Address::getCity)        // if address present, get city
        .orElse("Unknown");           // if any is empty, return default
}

// flatMap when the mapped value is itself Optional
public Optional<String> findUserEmail(String userId) {
    return Optional.ofNullable(userRepository.findById(userId))
        .flatMap(User::getEmail);   // User::getEmail returns Optional<String>
    // Without flatMap, you'd get Optional<Optional<String>>
}

// Combining with stream
List<String> emails = users.stream()
    .map(user -> Optional.ofNullable(user.getEmail()))
    .filter(Optional::isPresent)
    .map(Optional::get)
    .collect(Collectors.toList());

// Or more cleanly:
List<String> emails = users.stream()
    .map(User::getEmail)
    .filter(Objects::nonNull)
    .collect(Collectors.toList());
```

---

**Q44: What is CompletableFuture and how do you use it for async testing?**

**A:**

```java
// CompletableFuture — a Future that can be completed manually or by async computation
// Useful for: waiting for async operations, running tasks in parallel

// Basic async execution
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    // runs in ForkJoinPool (separate thread)
    return callExternalApi();
});

// Get the result (blocks until complete)
String result = future.get();        // throws checked exceptions
String result = future.join();       // throws unchecked (better for streams)

// Chain operations
CompletableFuture<String> pipeline = CompletableFuture
    .supplyAsync(() -> fetchOrderId())           // async: get order ID
    .thenApply(id -> "/api/orders/" + id)        // sync: transform
    .thenApplyAsync(url -> fetchFromApi(url))    // async: fetch
    .thenApply(resp -> resp.jsonPath().getString("status")); // sync: parse

// Run multiple futures in parallel and wait for all
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> callApi1());
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> callApi2());
CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> callApi3());

CompletableFuture.allOf(f1, f2, f3).join();  // wait for all to complete
String r1 = f1.join();
String r2 = f2.join();
String r3 = f3.join();

// Exception handling
CompletableFuture<String> safe = future
    .exceptionally(ex -> "default-on-error")    // recover from exception
    .handle((result, ex) -> {                   // handle both success and failure
        if (ex != null) return "error: " + ex.getMessage();
        return result.toUpperCase();
    });

// Timeout (Java 9+)
CompletableFuture<String> withTimeout = future
    .orTimeout(5, TimeUnit.SECONDS)  // complete with TimeoutException if takes > 5s
    .exceptionally(ex -> "timed out");
```

---

**Q45: What is the `var` keyword in Java 10?**

**A:**

`var` is a local variable type inference keyword. The compiler infers the actual type from the right-hand side — it is not a dynamic type (like JavaScript's `var`). The type is determined at compile time.

```java
// Without var — explicit types
ArrayList<Map<String, List<WebElement>>> structure = new ArrayList<>();
Map<String, String> testData = new HashMap<>();
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// With var — compiler infers the type
var structure = new ArrayList<Map<String, List<WebElement>>>();  // ArrayList inferred
var testData  = new HashMap<String, String>();                   // HashMap inferred
var wait      = new WebDriverWait(driver, Duration.ofSeconds(10)); // WebDriverWait inferred

// var in for-each loops
for (var entry : testData.entrySet()) {   // entry is Map.Entry<String,String>
    System.out.println(entry.getKey() + " = " + entry.getValue());
}

// var in try-with-resources
try (var br = new BufferedReader(new FileReader("data.csv"))) {
    var line = br.readLine();
}

// CANNOT use var for:
var x;                    // no initializer — cannot infer type
var list = null;          // null has no type — cannot infer
int[] arr = {1,2,3};     // array initializer without explicit type

// var is NOT a keyword in the traditional sense — 'var' can still be used as a method name
// var is a "reserved type name" in Java

// Important: var does NOT change the type — it's still statically typed
var s = "hello";
s = 42;  // compile error — s is String, cannot assign int
```

---

**Q46: What are record classes in Java 14+?**

**A:**

Records are a concise way to declare immutable data-holding classes. The compiler automatically generates the constructor, getters, `equals()`, `hashCode()`, and `toString()`.

```java
// Without record — verbose
public class OrderSummary {
    private final String orderId;
    private final double amount;
    private final String status;

    public OrderSummary(String orderId, double amount, String status) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }
    public String orderId() { return orderId; }
    public double amount()  { return amount; }
    public String status()  { return status; }

    @Override public boolean equals(Object o) { /* ... boilerplate ... */ }
    @Override public int hashCode()           { /* ... boilerplate ... */ }
    @Override public String toString()        { /* ... boilerplate ... */ }
}

// WITH record — 1 line replaces ~30 lines
public record OrderSummary(String orderId, double amount, String status) { }
// Compiler generates: constructor, orderId(), amount(), status(), equals(), hashCode(), toString()

// Usage — same as the verbose class
OrderSummary summary = new OrderSummary("ORD-001", 149.99, "PENDING");
System.out.println(summary.orderId());    // "ORD-001"
System.out.println(summary.amount());    // 149.99
System.out.println(summary);             // OrderSummary[orderId=ORD-001, amount=149.99, status=PENDING]

// Records are IMMUTABLE — no setters, fields are final
// summary.orderId = "ORD-002";  // compile error

// Add custom behaviour:
public record OrderSummary(String orderId, double amount, String status) {
    // Compact constructor — validation logic
    public OrderSummary {
        if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("orderId required");
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
    }

    // Custom method
    public boolean isPending() { return "PENDING".equals(status); }

    // Static factory method
    public static OrderSummary pending(String orderId, double amount) {
        return new OrderSummary(orderId, amount, "PENDING");
    }
}

// In automation — great for representing API response data
public record ApiResponse(int statusCode, String body, String contentType) { }

ApiResponse resp = new ApiResponse(response.getStatusCode(),
    response.getBody().asString(), response.getContentType());
assertEquals(resp.statusCode(), 201);
assertTrue(resp.body().contains("orderId"));
```

---

**Q47: What are switch expressions in Java 14+?**

**A:**

Switch expressions are an enhanced form of the traditional switch statement. They return a value and use `->` (arrow) syntax to eliminate the need for `break` and reduce the chance of fall-through bugs.

```java
// OLD switch statement — verbose, error-prone (easy to forget break)
String result;
switch (status) {
    case "PENDING":
        result = "Waiting for confirmation";
        break;
    case "ACTIVE":
    case "CONFIRMED":
        result = "In progress";
        break;  // without this break, falls through to CANCELLED!
    case "CANCELLED":
        result = "Order cancelled";
        break;
    default:
        result = "Unknown status";
}

// NEW switch expression (Java 14+) — returns a value, no fall-through
String result = switch (status) {
    case "PENDING"              -> "Waiting for confirmation";
    case "ACTIVE", "CONFIRMED"  -> "In progress";  // multiple labels
    case "CANCELLED"            -> "Order cancelled";
    default                     -> "Unknown status: " + status;
};

// For complex cases, use yield to return a value from a block
String result = switch (status) {
    case "ACTIVE" -> {
        System.out.println("Processing active order...");
        yield "In progress";   // yield instead of return in switch block
    }
    default -> "Unknown";
};

// In automation — browser/driver selection
WebDriver driver = switch (System.getProperty("browser", "chrome")) {
    case "chrome"  -> { WebDriverManager.chromedriver().setup(); yield new ChromeDriver(); }
    case "firefox" -> { WebDriverManager.firefoxdriver().setup(); yield new FirefoxDriver(); }
    case "edge"    -> { WebDriverManager.edgedriver().setup(); yield new EdgeDriver(); }
    default        -> throw new IllegalArgumentException("Unknown browser");
};

// Switch on integer — HTTP status mapping
String category = switch (statusCode / 100) {
    case 2  -> "Success";
    case 4  -> "Client Error";
    case 5  -> "Server Error";
    default -> "Other";
};
```

---

**Q48: What are text blocks in Java 15+?**

**A:**

Text blocks allow multi-line strings without the need for explicit `\n` concatenation. The content preserves indentation relative to the closing `"""`.

```java
// OLD way — hard to read, error-prone escaping
String payload = "{\n" +
    "  \"customerId\": \"" + customerId + "\",\n" +
    "  \"amount\": " + amount + ",\n" +
    "  \"type\": \"STANDARD\"\n" +
    "}";

// TEXT BLOCK (Java 15+) — reads like actual JSON
String payload = """
        {
          "customerId": "%s",
          "amount": %.2f,
          "type": "STANDARD"
        }
        """.formatted(customerId, amount);
// No escaping of " needed inside text block
// .formatted() applies String.format() to the text block

// SQL query
String query = """
        SELECT order_id, customer_id, amount, status
        FROM orders
        WHERE customer_id = @customerId
          AND status = 'PENDING'
        ORDER BY created_at DESC
        LIMIT 20
        """;

// HTML snippet for assertions
String expectedHtml = """
        <div class="order-summary">
          <h2>Order Confirmed</h2>
          <p class="order-id">ORD-001</p>
        </div>
        """;

// Incidental whitespace: the common leading whitespace is stripped automatically.
// The closing """ position controls the indent baseline.

// Text block methods:
String block = """
        Hello
        World
        """;
block.stripIndent();      // remove common leading whitespace (already done for text blocks)
block.translateEscapes(); // process \n \t etc inside the text block
block.formatted(args);    // String.format() applied to text block
```

---

## SECTION 11 — PRACTICAL AUTOMATION JAVA QUESTIONS

---

**Q49: How do you read a CSV file for data-driven testing in TestNG?**

**A:**
```java
// Using standard Java IO (no external library needed)
public class CsvDataReader {

    public static Object[][] fromCsv(String resourcePath) {
        List<String[]> rows = new ArrayList<>();
        // getResourceAsStream works from both JAR and file system
        try (InputStream is = CsvDataReader.class.getResourceAsStream(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            br.readLine(); // skip header

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                rows.add(parseCsvLine(line));  // handle quoted fields
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot read CSV: " + resourcePath, e);
        }
        return rows.toArray(new Object[0][]);
    }

    // Handle quoted fields: "Smith, John", 25 → ["Smith, John", "25"]
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"')       { inQuotes = !inQuotes; }
            else if (c == ',' && !inQuotes) { fields.add(sb.toString()); sb = new StringBuilder(); }
            else                { sb.append(c); }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }
}

// Place CSV in src/test/resources/testdata/loginData.csv
@DataProvider(name = "loginData")
public Object[][] loginData() {
    return CsvDataReader.fromCsv("/testdata/loginData.csv");
}

@Test(dataProvider = "loginData")
public void loginTest(String username, String password, String expected) {
    loginPage.login(username, password);
    assertTrue(driver.getPageSource().contains(expected));
}
```

---

**Q50: How do you read a properties file and use it across your test suite?**

**A:**
```java
// Thread-safe singleton ConfigReader using initialization-on-demand holder
public class ConfigReader {
    private final Properties props;

    private ConfigReader() {
        props = new Properties();
        // Load from classpath (works in JAR and during mvn test)
        String env = System.getProperty("env", "staging");
        String file = "/config-" + env + ".properties";  // config-staging.properties
        try (InputStream is = getClass().getResourceAsStream(file)) {
            if (is == null) throw new RuntimeException("Config file not found: " + file);
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config", e);
        }
    }

    private static class Holder {
        static final ConfigReader INSTANCE = new ConfigReader();
    }

    public static ConfigReader get() { return Holder.INSTANCE; }

    public String     getString(String key)               { return props.getProperty(key); }
    public String     getString(String key, String def)   { return props.getProperty(key, def); }
    public int        getInt(String key, int def)         {
        String v = props.getProperty(key);
        return v != null ? Integer.parseInt(v.trim()) : def;
    }
    public boolean    getBool(String key, boolean def)    {
        String v = props.getProperty(key);
        return v != null ? Boolean.parseBoolean(v.trim()) : def;
    }
    public long       getLong(String key, long def)       {
        String v = props.getProperty(key);
        return v != null ? Long.parseLong(v.trim()) : def;
    }
}

// src/test/resources/config-staging.properties
// base.url=https://staging-api.example.com
// auth.token=staging-token-xyz
// timeout.seconds=30
// headless=true

// Usage anywhere in your test suite:
String url     = ConfigReader.get().getString("base.url");
int    timeout = ConfigReader.get().getInt("timeout.seconds", 30);
boolean headless = ConfigReader.get().getBool("headless", false);

// Run different environments:
// mvn test -Denv=staging   → loads config-staging.properties
// mvn test -Denv=prod      → loads config-prod.properties
```

---

**Q51: What is reflection and how does TestNG use it internally?**

**A:**
```java
// Reflection lets you inspect and invoke code at runtime without compile-time knowledge.

// TestNG uses reflection to:
// 1. Find all @Test methods in your test class
// 2. Find @BeforeMethod / @AfterMethod methods
// 3. Read @DataProvider method to get test data
// 4. Read @Parameters annotation to inject config values
// 5. Check @Listeners annotation to wire up listener classes

// Example: how TestNG finds @Test methods (simplified)
public void discoverTests(Class<?> testClass) {
    for (Method method : testClass.getMethods()) {
        if (method.isAnnotationPresent(Test.class)) {
            Test annotation = method.getAnnotation(Test.class);
            System.out.println("Test: " + method.getName());
            System.out.println("  groups: " + Arrays.toString(annotation.groups()));
            System.out.println("  priority: " + annotation.priority());
            System.out.println("  enabled: " + annotation.enabled());
        }
    }
}

// You can use reflection in your own automation utilities:

// Dynamically invoke page object methods by string name (useful in keyword-driven frameworks)
public Object invokePageMethod(Object page, String methodName, Object... args) {
    try {
        Class<?>[] paramTypes = Arrays.stream(args)
            .map(Object::getClass)
            .toArray(Class<?>[]::new);
        Method method = page.getClass().getMethod(methodName, paramTypes);
        return method.invoke(page, args);
    } catch (ReflectiveOperationException e) {
        throw new RuntimeException("Cannot invoke " + methodName + " on " + page.getClass(), e);
    }
}

// Usage:
LoginPage loginPage = new LoginPage(driver);
invokePageMethod(loginPage, "login", "admin", "Admin@123");  // calls loginPage.login("admin", "Admin@123")
```

---

**Q52: What common Java mistakes do automation engineers make in production?**

**A:**

Ten specific mistakes with fixes:

```java
// 1. == for String/Object comparison
if (driver.getTitle() == "Dashboard")          // WRONG
if (driver.getTitle().equals("Dashboard"))     // CORRECT

// 2. Static WebDriver in parallel tests
private static WebDriver driver;               // all threads share ONE browser → chaos
private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();  // CORRECT

// 3. Not removing ThreadLocal (memory leak)
// After test: must call driver.remove() — not just driver.get().quit()
@AfterMethod
public void tearDown() {
    if (driverPool.get() != null) {
        driverPool.get().quit();
        driverPool.remove();   // critical — prevents memory leak
    }
}

// 4. Catching generic Exception and swallowing it
try { risky(); } catch (Exception e) { }   // bug silently hidden — test may pass incorrectly

// 5. Not closing file resources (memory/file handle leak)
FileReader fr = new FileReader("file.csv");   // never closed if exception thrown
// Fix: try-with-resources

// 6. Building strings in loops with concatenation
String result = "";
for (String s : list) result += s;           // creates N temporary objects
StringBuilder sb = new StringBuilder();
for (String s : list) sb.append(s);          // one object

// 7. NullPointerException in assertion
String text = element.getText();             // may return null
Assert.assertEquals(text, "Expected");      // NPE if text is null
Assert.assertEquals("Expected", text);      // still NPE but clearer
String safeText = (text != null) ? text : "";  // safe
Assert.assertEquals(safeText, "Expected");

// 8. Hardcoded Thread.sleep
Thread.sleep(5000);  // always waits 5 seconds even if element appears in 100ms
wait.until(ExpectedConditions.visibilityOfElementLocated(locator));  // correct

// 9. Missing @AfterMethod for cleanup — test data pollutes next test
// Always clean up: delete test records, reset state, clear cookies

// 10. Ignoring autoboxing NullPointerException
Integer nullable = getValueThatMightBeNull();
int x = nullable;   // NPE if null — always null-check wrapper types before unboxing
```

---

**Q53: How do you create test data using the Builder pattern?**

**A:**
```java
// Builder pattern for complex test data objects
public class TestOrder {
    // Required fields
    private final String customerId;
    private final double amount;
    // Optional with defaults
    private final String type;
    private final String currency;
    private final boolean giftWrap;
    private final String promoCode;
    private final int quantity;

    private TestOrder(Builder b) {
        this.customerId = b.customerId;
        this.amount     = b.amount;
        this.type       = b.type;
        this.currency   = b.currency;
        this.giftWrap   = b.giftWrap;
        this.promoCode  = b.promoCode;
        this.quantity   = b.quantity;
    }

    public String toJson() {
        return String.format(
            "{\"customerId\":\"%s\",\"amount\":%.2f,\"type\":\"%s\"," +
            "\"currency\":\"%s\",\"giftWrap\":%b,\"quantity\":%d%s}",
            customerId, amount, type, currency, giftWrap, quantity,
            promoCode != null ? ",\"promoCode\":\"" + promoCode + "\"" : ""
        );
    }

    // Getters
    public String getCustomerId() { return customerId; }
    public double getAmount()     { return amount; }

    public static class Builder {
        private final String customerId;   // required — in constructor
        private final double amount;       // required
        private String type    = "STANDARD";
        private String currency = "AUD";
        private boolean giftWrap = false;
        private String promoCode = null;
        private int quantity = 1;

        public Builder(String customerId, double amount) {
            if (customerId == null || customerId.isBlank())
                throw new IllegalArgumentException("customerId required");
            this.customerId = customerId;
            this.amount = amount;
        }

        public Builder type(String type)       { this.type = type; return this; }
        public Builder currency(String c)      { this.currency = c; return this; }
        public Builder giftWrap()              { this.giftWrap = true; return this; }
        public Builder promoCode(String code)  { this.promoCode = code; return this; }
        public Builder quantity(int qty)       { this.quantity = qty; return this; }

        public TestOrder build()               { return new TestOrder(this); }
    }
}

// Usage in tests:
TestOrder standard = new TestOrder.Builder("CUST-42", 99.99).build();

TestOrder premium = new TestOrder.Builder("CUST-99", 499.99)
    .type("PRIORITY")
    .currency("USD")
    .giftWrap()
    .promoCode("SAVE20")
    .quantity(3)
    .build();

// Use in RestAssured:
given()
    .contentType("application/json")
    .body(premium.toJson())
    .post("/api/orders");
```

---

**Q54: What are generics wildcards and when do you use them?**

**A:**

```java
// THREE wildcard types:

// 1. Unbounded wildcard: List<?> — any type, read-only
public void printAll(List<?> items) {
    for (Object item : items) {
        System.out.println(item);  // can only use Object methods
    }
}
printAll(List.of("a", "b"));       // works with String
printAll(List.of(1, 2, 3));        // works with Integer
printAll(List.of(new Order()));    // works with Order

// 2. Upper bounded wildcard: List<? extends Number> — any Number subclass, read-only
public double sumList(List<? extends Number> numbers) {
    return numbers.stream().mapToDouble(Number::doubleValue).sum();
}
sumList(List.of(1, 2, 3));         // Integer extends Number
sumList(List.of(1.5, 2.5));        // Double extends Number
// numbers.add(5);  — compile error: can't add (type not guaranteed)

// 3. Lower bounded wildcard: List<? super Integer> — Integer or parent types, write-only
public void addNumbers(List<? super Integer> list) {
    list.add(1);    // safe — Integer is a subtype of whatever ? super Integer is
    list.add(2);
    list.add(3);
}
List<Number> numberList = new ArrayList<>();
addNumbers(numberList);   // works — Integer is a subtype of Number
List<Object> objectList = new ArrayList<>();
addNumbers(objectList);   // works — Integer is a subtype of Object

// PECS mnemonic — Producer Extends, Consumer Super:
// If you PRODUCE (read) from the collection: use ? extends T
// If you CONSUME (write to) the collection: use ? super T

// Automation example: generic response verifier
public <T> void verifyField(List<? extends Map<String, Object>> records,
                             String fieldName, T expectedValue) {
    for (Map<String, Object> record : records) {
        assertEquals(record.get(fieldName), expectedValue,
            "Field " + fieldName + " mismatch in record: " + record);
    }
}
```

---

**Q55: What are the differences between `Comparable` and `Comparator`, and when would you use each in automation?**

**A:**

```java
// COMPARABLE — the class defines its OWN natural ordering
// Implement in the class being sorted
// One ordering only — the "natural" order

public class TestResult implements Comparable<TestResult> {
    private String testName;
    private long   durationMs;
    private String status;

    @Override
    public int compareTo(TestResult other) {
        // Natural order: shortest test first
        return Long.compare(this.durationMs, other.durationMs);
    }
}

List<TestResult> results = getTestResults();
Collections.sort(results);   // uses compareTo() automatically
results.stream().sorted().collect(Collectors.toList());   // also uses compareTo()

// COMPARATOR — external ordering, more flexible
// Defined outside the class, can have multiple orderings, can be a lambda

// Sort by name ascending
Comparator<TestResult> byName = Comparator.comparing(TestResult::getTestName);

// Sort by duration descending (longest first)
Comparator<TestResult> slowestFirst = Comparator.comparingLong(TestResult::getDurationMs).reversed();

// Sort by status, then by name within each status group
Comparator<TestResult> byStatusThenName = Comparator
    .comparing(TestResult::getStatus)
    .thenComparing(TestResult::getTestName);

results.sort(slowestFirst);  // sort externally

// Lambda comparator (for one-off sorting)
results.sort((a, b) -> a.getTestName().compareTo(b.getTestName()));

// In automation: sort API responses for deterministic assertions
List<Order> orders = response.jsonPath().getList("orders", Order.class);
orders.sort(Comparator.comparing(Order::getOrderId));  // sort before asserting
assertEquals(orders.get(0).getOrderId(), "ORD-001");   // now predictable

// Null-safe comparator (Java 8)
Comparator<TestResult> nullSafe = Comparator.nullsFirst(
    Comparator.comparing(TestResult::getDurationMs)
);
// Null results go to the beginning, non-null sorted by duration
```

---

*Guide covers 55 questions total: original 40 expanded + 15 new questions. New additions cover: generics in detail (Q39, Q54), reflection (Q38, Q51), lambda/functional interfaces (Q20), streams terminal vs intermediate operators (Q22, Q42), all 4 method reference types (Q41), Optional chaining (Q43), CompletableFuture basics (Q44), var keyword Java 10 (Q45), record classes Java 14+ (Q46), switch expressions Java 14+ (Q47), text blocks Java 15+ (Q48), reading CSV for data-driven testing (Q37, Q49), reading properties files (Q36, Q50), common automation Java mistakes (Q40, Q52), Builder pattern for test data (Q53), and Comparable vs Comparator in automation context (Q35, Q55).*
