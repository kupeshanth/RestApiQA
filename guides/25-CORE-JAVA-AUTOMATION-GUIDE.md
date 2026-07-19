# Core Java & OOP — Complete Guide for Automation Engineers
## Java Fundamentals + OOP + Collections + Design Patterns + Interview Q&A

---

## TABLE OF CONTENTS
1. [Java Basics](#1-java-basics)
2. [OOP — Four Pillars](#2-oop--four-pillars)
3. [Classes, Objects & Constructors](#3-classes-objects--constructors)
4. [Access Modifiers](#4-access-modifiers)
5. [Static vs Instance](#5-static-vs-instance)
6. [Interfaces vs Abstract Classes](#6-interfaces-vs-abstract-classes)
7. [Collections — List, Map, Set, Array](#7-collections--list-map-set-array)
8. [Exception Handling](#8-exception-handling)
9. [String Manipulation](#9-string-manipulation)
10. [Loops & Conditionals](#10-loops--conditionals)
11. [Java 8 — Lambda & Streams](#11-java-8--lambda--streams)
12. [Generics](#12-generics)
13. [Multithreading Basics](#13-multithreading-basics)
14. [Design Patterns for Automation](#14-design-patterns-for-automation)
15. [Annotations](#15-annotations)
16. [Maven Basics](#16-maven-basics)
17. [Interview Q&A — Core Java](#17-interview-qa--core-java)
18. [Interview Q&A — OOP](#18-interview-qa--oop)

---

## 1. Java Basics

### Data Types
```java
// Primitive types
int     age      = 25;           // 32-bit integer (-2B to 2B)
long    bigNum   = 9999999999L;  // 64-bit integer (add L suffix)
double  price    = 9.99;         // 64-bit decimal
float   rate     = 3.14f;        // 32-bit decimal (add f suffix)
boolean isActive = true;         // true or false
char    grade    = 'A';          // single character
byte    small    = 127;          // -128 to 127
short   mid      = 32000;        // -32768 to 32767

// Reference types (objects)
String  name     = "Kupeshanth";
int[]   scores   = {90, 85, 92};
```

### Type Casting
```java
// Widening (automatic — no data loss)
int i = 10;
long l = i;       // int → long, automatic
double d = i;     // int → double, automatic

// Narrowing (manual — may lose data)
double d = 9.99;
int i = (int) d;  // i = 9, decimal part lost — must cast explicitly

// String to int
int num = Integer.parseInt("42");

// int to String
String s = String.valueOf(42);
String s2 = 42 + "";   // shorthand
```

### Operators
```java
// Arithmetic
int result = 10 + 3;   // 13
int result = 10 % 3;   // 1 (remainder/modulo — used to check even/odd)
int result = 10 / 3;   // 3 (integer division — truncates decimal)

// Comparison
10 == 10    // true
10 != 5     // true
10 > 5      // true
10 >= 10    // true

// Logical
true && false   // false (AND — both must be true)
true || false   // true  (OR  — at least one must be true)
!true           // false (NOT)

// String equality — NEVER use == for Strings
String a = new String("hello");
String b = new String("hello");
a == b          // FALSE — compares references, not content
a.equals(b)     // TRUE  — always use .equals() for String comparison
```

---

## 2. OOP — Four Pillars

### 1. Encapsulation — hide data, expose through methods
```java
// BAD: public fields, anyone can change directly
public class User {
    public String password;   // ← anyone can write: user.password = "hacked"
}

// GOOD: private fields + public getters/setters
public class User {
    private String password;   // hidden

    public void setPassword(String password) {
        // can validate before setting
        if (password.length() >= 8) {
            this.password = password;
        }
    }

    public String getPassword() {
        return password;
    }
}
// Benefit: control over what's allowed, hide implementation
```

### 2. Inheritance — child class reuses parent class
```java
// Parent class
public class Animal {
    protected String name;

    public void eat() {
        System.out.println(name + " is eating");
    }
}

// Child class — inherits eat(), adds bark()
public class Dog extends Animal {
    public void bark() {
        System.out.println(name + " is barking");
    }
}

// In tests: BaseTest (parent) → LoginTest, CheckoutTest (children)
// Children inherit setUp() and tearDown() from BaseTest
```

### 3. Polymorphism — same method, different behaviour
```java
// Method Overriding (runtime polymorphism)
public class Shape {
    public double area() { return 0; }
}

public class Circle extends Shape {
    private double radius;
    @Override
    public double area() { return Math.PI * radius * radius; }
}

public class Rectangle extends Shape {
    private double w, h;
    @Override
    public double area() { return w * h; }
}

// Same method call, different result depending on actual type
Shape s = new Circle();       // Circle stored as Shape
s.area();                     // calls Circle's area() — runtime decision

// In automation: driver.findElement() works for Chrome, Firefox, Edge
// because all implement WebDriver — polymorphism at work
```

### 4. Abstraction — hide complexity, show only what's needed
```java
// Abstract class — can have abstract (no body) and concrete methods
public abstract class BasePage {
    protected WebDriver driver;

    // Abstract — subclasses MUST implement this
    public abstract boolean isLoaded();

    // Concrete — shared by all subclasses
    public void navigate(String url) {
        driver.get(url);
    }
}

public class LoginPage extends BasePage {
    @Override
    public boolean isLoaded() {
        return driver.getCurrentUrl().contains("/login");
    }
}

// Interface — pure abstraction, only method signatures (Java 8+ allows default methods)
public interface Clickable {
    void click();
    default void highlight() { /* optional default behaviour */ }
}
```

---

## 3. Classes, Objects & Constructors

```java
public class Car {
    // Fields (state)
    private String brand;
    private int year;
    private static int totalCars = 0;   // shared across all Car instances

    // Default constructor
    public Car() {
        this.brand = "Unknown";
        this.year  = 2024;
        totalCars++;
    }

    // Parameterised constructor
    public Car(String brand, int year) {
        this.brand = brand;     // 'this' refers to current instance
        this.year  = year;
        totalCars++;
    }

    // Copy constructor
    public Car(Car other) {
        this.brand = other.brand;
        this.year  = other.year;
        totalCars++;
    }

    // Getters and Setters
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    // Static method — belongs to class, not instance
    public static int getTotalCars() { return totalCars; }

    // toString — called automatically when printing
    @Override
    public String toString() {
        return "Car{brand=" + brand + ", year=" + year + "}";
    }

    // equals — override for object comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Car)) return false;
        Car other = (Car) obj;
        return this.brand.equals(other.brand) && this.year == other.year;
    }
}

// Using the class
Car c1 = new Car("Toyota", 2022);
Car c2 = new Car("Toyota", 2022);
System.out.println(c1 == c2);        // false — different objects
System.out.println(c1.equals(c2));   // true  — same content
System.out.println(Car.getTotalCars()); // 2
```

---

## 4. Access Modifiers

```java
public class Example {
    public    int a;   // accessible from ANYWHERE
    protected int b;   // accessible in same package + subclasses
    int           c;   // (package-private) accessible only in same package
    private   int d;   // accessible ONLY within this class
}

// Rule of thumb for automation:
// - Page object fields → private (hide locators)
// - Methods for tests to call → public
// - Shared setup methods in BaseTest → protected
// - Constants → public static final
```

| Modifier | Same Class | Same Package | Subclass | Everywhere |
|----------|-----------|-------------|----------|-----------|
| `public` | ✅ | ✅ | ✅ | ✅ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| (default) | ✅ | ✅ | ❌ | ❌ |
| `private` | ✅ | ❌ | ❌ | ❌ |

---

## 5. Static vs Instance

```java
public class Counter {
    private int count = 0;           // instance variable — each object has its own
    private static int total = 0;    // static variable — shared across ALL objects

    public void increment() {
        count++;    // changes THIS object's count
        total++;    // changes the shared total
    }

    public int getCount() { return count; }
    public static int getTotal() { return total; }
}

Counter c1 = new Counter();
Counter c2 = new Counter();
c1.increment();   // c1.count = 1, total = 1
c2.increment();   // c2.count = 1, total = 2
c2.increment();   // c2.count = 2, total = 3

c1.getCount();    // 1  — c1's own count
c2.getCount();    // 2  — c2's own count
Counter.getTotal(); // 3 — shared across both

// In automation:
// ThreadLocal<WebDriver> is instance — each thread has its own
// BASE_URL in ApiConstants is static — shared, never changes
```

---

## 6. Interfaces vs Abstract Classes

```java
// INTERFACE — what a class CAN DO
public interface Drivable {
    void accelerate(int speed);     // abstract — must implement
    void brake();                   // abstract
    default void honk() {           // default — optional to override
        System.out.println("Beep!");
    }
}

// ABSTRACT CLASS — what a class IS (partial implementation)
public abstract class Vehicle {
    protected String brand;

    public Vehicle(String brand) { this.brand = brand; }  // can have constructor

    public abstract void startEngine();   // must implement

    public void refuel() {                // concrete — inherited as-is
        System.out.println("Refuelling...");
    }
}

// Can implement multiple interfaces but extend only ONE class
public class Car extends Vehicle implements Drivable, Serializable {
    public Car(String brand) { super(brand); }

    @Override public void startEngine() { System.out.println("Vroom!"); }
    @Override public void accelerate(int speed) { /* ... */ }
    @Override public void brake() { /* ... */ }
}
```

| | Interface | Abstract Class |
|--|-----------|---------------|
| Methods | Abstract + default | Abstract + concrete |
| Fields | Only constants (public static final) | Any fields |
| Constructor | ❌ | ✅ |
| Multiple inheritance | ✅ (implement many) | ❌ (extend one) |
| Use when | Defining a contract/capability | Sharing common code with a base |

---

## 7. Collections — List, Map, Set, Array

### Array — fixed size
```java
int[] nums = {1, 2, 3, 4, 5};
nums[0] = 10;               // update
nums.length;                // 5 — fixed, cannot resize
Arrays.sort(nums);          // sort in place
```

### ArrayList — dynamic array, ordered, allows duplicates
```java
List<String> browsers = new ArrayList<>();
browsers.add("Chrome");
browsers.add("Firefox");
browsers.add("Chrome");        // duplicates allowed
browsers.get(0);               // "Chrome"
browsers.size();               // 3
browsers.remove("Firefox");
browsers.contains("Chrome");   // true
browsers.isEmpty();            // false

// Iterate
for (String b : browsers) { System.out.println(b); }
browsers.forEach(System.out::println);  // Java 8 lambda

// Most common in automation: List<WebElement>
List<WebElement> rows = driver.findElements(By.tagName("tr"));
System.out.println("Row count: " + rows.size());
```

### HashMap — key-value pairs, no order guarantee
```java
Map<String, String> testData = new HashMap<>();
testData.put("username", "admin");
testData.put("password", "Admin@123");
testData.put("username", "newAdmin");   // overwrites existing key

testData.get("username");       // "newAdmin"
testData.containsKey("email");  // false
testData.size();                // 2

// Iterate
for (Map.Entry<String, String> entry : testData.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}

// In automation: pass test data as Map to avoid many parameters
public void login(Map<String, String> credentials) {
    usernameField.sendKeys(credentials.get("username"));
    passwordField.sendKeys(credentials.get("password"));
}
```

### HashSet — no duplicates, no order
```java
Set<String> uniqueUrls = new HashSet<>();
uniqueUrls.add("https://a.com");
uniqueUrls.add("https://b.com");
uniqueUrls.add("https://a.com");   // duplicate ignored silently
uniqueUrls.size();                  // 2

// Use case: collect all visited URLs, check for duplicates
Set<String> visitedPages = new HashSet<>();
for (WebElement link : driver.findElements(By.tagName("a"))) {
    visitedPages.add(link.getAttribute("href"));
}
```

### LinkedList — ordered, fast insert/delete, slow random access
```java
LinkedList<String> queue = new LinkedList<>();
queue.add("task1");
queue.addFirst("urgent");   // add to front
queue.pollFirst();          // remove and return first
// Use as a queue or stack when order of insertion matters
```

### Useful Collections methods
```java
Collections.sort(list);                    // sort ascending
Collections.sort(list, Comparator.reverseOrder());  // sort descending
Collections.shuffle(list);                // randomise order
Collections.frequency(list, "chrome");    // count occurrences
Collections.max(list);                    // max element
Collections.min(list);                    // min element
```

---

## 8. Exception Handling

```java
// Basic try-catch-finally
try {
    WebElement el = driver.findElement(By.id("missing"));  // might throw
    el.click();
} catch (NoSuchElementException e) {
    System.out.println("Element not found: " + e.getMessage());
    // take screenshot, log, etc.
} catch (Exception e) {
    System.out.println("Unexpected error: " + e.getMessage());
} finally {
    // ALWAYS runs — even if exception thrown or method returns
    driver.quit();   // good place for cleanup
}

// Throw an exception
public void login(String user, String pass) {
    if (user == null || user.isEmpty()) {
        throw new IllegalArgumentException("Username cannot be empty");
    }
    // ...
}

// Custom exception
public class TestSetupException extends RuntimeException {
    public TestSetupException(String message) {
        super(message);
    }
    public TestSetupException(String message, Throwable cause) {
        super(message, cause);
    }
}

// throw custom exception
if (!configFile.exists()) {
    throw new TestSetupException("Config file not found: " + configFile.getPath());
}

// Checked vs Unchecked
// Checked: must handle or declare (IOException, SQLException) — compile error if not caught
// Unchecked (RuntimeException): no requirement to catch (NullPointerException, IllegalArgumentException)

// try-with-resources — auto-closes resources
try (FileReader fr = new FileReader("data.csv");
     BufferedReader br = new BufferedReader(fr)) {
    String line;
    while ((line = br.readLine()) != null) {
        System.out.println(line);
    }
}   // fr and br closed automatically even if exception thrown
```

---

## 9. String Manipulation

```java
String s = "  Hello World  ";

// Common methods
s.length()                    // 15
s.trim()                      // "Hello World" — removes leading/trailing whitespace
s.toLowerCase()               // "  hello world  "
s.toUpperCase()               // "  HELLO WORLD  "
s.contains("World")           // true
s.startsWith("  Hello")       // true
s.endsWith("World  ")         // true
s.indexOf("World")            // 8
s.replace("World", "Java")    // "  Hello Java  "
s.replaceAll("\\s+", " ")     // replace multiple spaces with single
s.split(" ")                  // ["", "", "Hello", "World", "", ""]
s.trim().split("\\s+")        // ["Hello", "World"] — split on any whitespace
s.substring(2, 7)             // "Hello"
s.charAt(2)                   // 'H'
s.isEmpty()                   // false
s.isBlank()                   // false (Java 11+) — checks if empty or whitespace only
"".isEmpty()                  // true
"   ".isBlank()               // true (Java 11+)

// String comparison — ALWAYS use equals()
"hello".equals("hello")       // true
"hello".equalsIgnoreCase("HELLO")  // true
"hello" == "hello"            // true only for literals (String pool), NEVER rely on this

// String to number
int num = Integer.parseInt("42");
double d = Double.parseDouble("3.14");

// Number to String
String s = String.valueOf(42);
String s = Integer.toString(42);

// StringBuilder — for building strings in loops (more efficient than + concatenation)
StringBuilder sb = new StringBuilder();
for (String item : list) {
    sb.append(item).append(", ");
}
String result = sb.toString();
// String + in loop creates many temporary objects → use StringBuilder

// String.format
String msg = String.format("User %s logged in at %s", username, timestamp);

// Join
String joined = String.join(", ", "a", "b", "c");  // "a, b, c"
String joined = String.join(", ", list);            // join from list
```

---

## 10. Loops & Conditionals

```java
// if-else
if (statusCode == 200) {
    System.out.println("Pass");
} else if (statusCode == 404) {
    System.out.println("Not Found");
} else {
    System.out.println("Fail: " + statusCode);
}

// Ternary operator
String result = (statusCode == 200) ? "Pass" : "Fail";

// switch
switch (browser) {
    case "chrome":  driver = new ChromeDriver();  break;
    case "firefox": driver = new FirefoxDriver(); break;
    default:        throw new IllegalArgumentException("Unknown browser: " + browser);
}

// Switch expression (Java 14+)
WebDriver driver = switch (browser) {
    case "chrome"  -> new ChromeDriver();
    case "firefox" -> new FirefoxDriver();
    default        -> throw new IllegalArgumentException("Unknown: " + browser);
};

// for loop
for (int i = 0; i < 10; i++) { System.out.println(i); }

// enhanced for (for-each)
for (WebElement row : rows) { System.out.println(row.getText()); }

// while
int retry = 0;
while (retry < 3) {
    try { sendRequest(); break; }
    catch (Exception e) { retry++; }
}

// do-while — executes at least once
do {
    page++;
    loadPage(page);
} while (hasMoreResults());

// break and continue
for (String url : urls) {
    if (url == null) continue;     // skip null, go to next
    if (url.equals("stop")) break; // stop loop entirely
    visit(url);
}
```

---

## 11. Java 8 — Lambda & Streams

### Lambda Expressions
```java
// Before Java 8 — anonymous class
Runnable r = new Runnable() {
    @Override
    public void run() { System.out.println("Running"); }
};

// Java 8 — lambda (inline function)
Runnable r = () -> System.out.println("Running");

// With parameters
Comparator<String> comp = (a, b) -> a.compareTo(b);

// Common in test code
list.forEach(item -> System.out.println(item));
list.forEach(System.out::println);   // method reference shorthand

// Sort with lambda
list.sort((a, b) -> a.getName().compareTo(b.getName()));
```

### Streams — pipeline operations on collections
```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// filter — keep only matching elements
List<Integer> evens = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());  // [2, 4, 6, 8, 10]

// map — transform each element
List<String> strings = numbers.stream()
    .map(n -> "Item " + n)
    .collect(Collectors.toList());  // ["Item 1", "Item 2", ...]

// sorted
List<Integer> sorted = numbers.stream()
    .sorted()
    .collect(Collectors.toList());

// count
long count = numbers.stream().filter(n -> n > 5).count();  // 5

// anyMatch / allMatch / noneMatch
boolean anyOver9  = numbers.stream().anyMatch(n -> n > 9);   // true
boolean allOver0  = numbers.stream().allMatch(n -> n > 0);   // true
boolean noneNeg   = numbers.stream().noneMatch(n -> n < 0);  // true

// findFirst
Optional<Integer> first = numbers.stream().filter(n -> n > 5).findFirst();
first.ifPresent(System.out::println);  // 6

// collect to Map
Map<Integer, String> map = numbers.stream()
    .collect(Collectors.toMap(n -> n, n -> "Item " + n));

// In automation: filter WebElements
List<String> enabledButtons = driver.findElements(By.tagName("button"))
    .stream()
    .filter(WebElement::isEnabled)
    .map(WebElement::getText)
    .collect(Collectors.toList());
```

### Optional — avoid NullPointerException
```java
Optional<String> opt = Optional.ofNullable(getUserName());  // might be null

opt.isPresent();            // true if has value
opt.get();                  // get value — throws if empty
opt.orElse("default");      // get value or default
opt.orElseThrow(() -> new RuntimeException("Not found"));
opt.ifPresent(System.out::println);  // only run if present
```

---

## 12. Generics

```java
// Without generics — unsafe, requires casting
List list = new ArrayList();
list.add("hello");
list.add(42);          // allowed — mixed types
String s = (String) list.get(1);  // ClassCastException at runtime!

// With generics — type-safe
List<String> list = new ArrayList<>();
list.add("hello");
// list.add(42);  // compile error — caught early

// Generic method
public <T> T getFirst(List<T> list) {
    return list.isEmpty() ? null : list.get(0);
}

String first = getFirst(List.of("a", "b"));   // no casting needed
Integer num   = getFirst(List.of(1, 2, 3));

// Generic class
public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key   = key;
        this.value = value;
    }

    public K getKey()   { return key; }
    public V getValue() { return value; }
}

Pair<String, Integer> pair = new Pair<>("age", 25);

// Bounded generics
public <T extends Number> double sum(List<T> list) {
    return list.stream().mapToDouble(Number::doubleValue).sum();
}
// Only accepts Number or its subclasses (Integer, Double, Float...)
```

---

## 13. Multithreading Basics

```java
// Thread — the unit of parallel execution
// Relevant for automation: parallel tests use multiple threads

// Create a thread
Thread t = new Thread(() -> {
    System.out.println("Running in thread: " + Thread.currentThread().getName());
});
t.start();

// Runnable
Runnable task = () -> { /* work */ };
new Thread(task).start();

// synchronized — prevent race conditions
public class Counter {
    private int count = 0;

    // Only one thread can execute this at a time
    public synchronized void increment() {
        count++;
    }
}

// volatile — ensures visibility across threads
private volatile boolean isRunning = true;

// Thread states
Thread.State.NEW        // created, not started
Thread.State.RUNNABLE   // running or ready to run
Thread.State.WAITING    // waiting indefinitely for another thread
Thread.State.BLOCKED    // waiting to acquire a lock
Thread.State.TERMINATED // finished

// ThreadLocal — each thread has its own copy (key for parallel Selenium)
private static ThreadLocal<WebDriver> driverPool = new ThreadLocal<>();
driverPool.set(new ChromeDriver());     // set for current thread
driverPool.get();                        // get current thread's driver
driverPool.remove();                     // clean up after test

// ExecutorService — manage thread pool
ExecutorService pool = Executors.newFixedThreadPool(4);
pool.submit(() -> runTest("loginTest"));
pool.submit(() -> runTest("checkoutTest"));
pool.shutdown();
pool.awaitTermination(60, TimeUnit.SECONDS);
```

---

## 14. Design Patterns for Automation

### Singleton — one instance shared everywhere
```java
// Use case: single WebDriver instance, single config reader
public class ConfigReader {
    private static ConfigReader instance;
    private Properties properties;

    private ConfigReader() {   // private constructor — can't use new
        properties = new Properties();
        try {
            properties.load(new FileReader("config.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Config file not found");
        }
    }

    // Thread-safe singleton
    public static synchronized ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}

// Usage — same instance everywhere
String url = ConfigReader.getInstance().get("base_url");
```

### Factory — create objects without specifying exact class
```java
// Use case: create different WebDriver types based on config
public class DriverFactory {
    public static WebDriver createDriver(String browser) {
        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                return new ChromeDriver();
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                return new FirefoxDriver();
            case "edge":
                WebDriverManager.edgedriver().setup();
                return new EdgeDriver();
            default:
                throw new IllegalArgumentException("Unknown browser: " + browser);
        }
    }
}

// Usage — test doesn't need to know HOW driver is created
WebDriver driver = DriverFactory.createDriver(System.getProperty("browser", "chrome"));
```

### Page Object — encapsulate page structure (most used in automation)
```java
public class LoginPage {
    private WebDriver driver;

    // Locators — private, only this class knows them
    @FindBy(id = "username") private WebElement usernameField;
    @FindBy(id = "password") private WebElement passwordField;
    @FindBy(id = "loginBtn") private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    // Actions — public, tests call these
    public DashboardPage login(String username, String password) {
        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
        return new DashboardPage(driver);   // return next page
    }
}
```

### Builder — construct complex objects step by step
```java
// Use case: build test data objects with many optional fields
public class TestUser {
    private final String username;   // required
    private final String email;      // required
    private final String role;       // optional
    private final boolean active;    // optional

    private TestUser(Builder builder) {
        this.username = builder.username;
        this.email    = builder.email;
        this.role     = builder.role;
        this.active   = builder.active;
    }

    public static class Builder {
        private String username;
        private String email;
        private String role   = "user";   // default
        private boolean active = true;    // default

        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email)       { this.email = email;       return this; }
        public Builder role(String role)         { this.role = role;         return this; }
        public Builder active(boolean active)    { this.active = active;     return this; }

        public TestUser build() {
            if (username == null) throw new IllegalStateException("Username required");
            return new TestUser(this);
        }
    }
}

// Usage — readable, flexible, no 10-parameter constructor
TestUser admin = new TestUser.Builder()
    .username("admin")
    .email("admin@test.com")
    .role("admin")
    .build();

TestUser basic = new TestUser.Builder()
    .username("user1")
    .email("user1@test.com")
    .build();   // role defaults to "user", active defaults to true
```

### Strategy — swap algorithm at runtime
```java
// Use case: different wait strategies
public interface WaitStrategy {
    WebElement waitFor(WebDriver driver, By locator);
}

public class ExplicitWaitStrategy implements WaitStrategy {
    @Override
    public WebElement waitFor(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}

public class FluentWaitStrategy implements WaitStrategy {
    @Override
    public WebElement waitFor(WebDriver driver, By locator) {
        return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(30))
            .pollingEvery(Duration.ofSeconds(2))
            .ignoring(NoSuchElementException.class)
            .until(d -> d.findElement(locator));
    }
}

// Switch strategy without changing test code
public class BasePage {
    private WaitStrategy waitStrategy = new ExplicitWaitStrategy();

    public void setWaitStrategy(WaitStrategy strategy) {
        this.waitStrategy = strategy;
    }

    public WebElement findElement(By locator) {
        return waitStrategy.waitFor(driver, locator);
    }
}
```

---

## 15. Annotations

```java
// Built-in Java annotations
@Override               // tells compiler you're overriding a parent method
@Deprecated             // marks method as outdated — use something else
@SuppressWarnings("unchecked")  // suppress specific compiler warnings
@FunctionalInterface    // interface with exactly one abstract method (for lambdas)

// Custom annotation example
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)    // available at runtime (needed for reflection)
@Target(ElementType.METHOD)            // only applies to methods
public @interface TestCase {
    String id();
    String description() default "";
    String[] tags() default {};
}

// Usage
@TestCase(id = "TC001", description = "Verify login", tags = {"smoke", "regression"})
@Test
public void loginTest() { ... }

// Read annotation at runtime
Method m = getClass().getMethod("loginTest");
TestCase tc = m.getAnnotation(TestCase.class);
System.out.println(tc.id());          // "TC001"
System.out.println(Arrays.toString(tc.tags())); // "[smoke, regression]"

// TestNG annotations (used in tests)
@BeforeSuite, @BeforeTest, @BeforeClass, @BeforeMethod
@Test(groups = "smoke", priority = 1, enabled = true, timeOut = 5000)
@DataProvider(name = "data", parallel = true)
@Parameters({"browser", "env"})
@AfterMethod, @AfterClass, @AfterTest, @AfterSuite
@Listeners({TestListener.class, RetryListener.class})
```

---

## 16. Maven Basics

```xml
<!-- pom.xml structure -->
<project>
    <groupId>com.company</groupId>        <!-- your organisation -->
    <artifactId>project-name</artifactId> <!-- project identifier -->
    <version>1.0-SNAPSHOT</version>       <!-- SNAPSHOT = in development -->

    <properties>
        <maven.compiler.source>11</maven.compiler.source>  <!-- Java version -->
        <maven.compiler.target>11</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>7.8.0</version>
            <scope>test</scope>     <!-- only on test classpath -->
        </dependency>
    </dependencies>
    <!-- scope values:
         compile  = available everywhere (default)
         test     = only in test code
         provided = needed to compile, but NOT packaged (server provides it)
         runtime  = not needed to compile, needed to run
    -->
</project>
```

### Maven Lifecycle (in order)
```
validate   → verify pom.xml is correct
compile    → compile source code
test       → run unit tests
package    → create JAR/WAR
verify     → run integration tests
install    → install to local ~/.m2 cache
deploy     → deploy to remote repository
```

### Maven Commands
```bash
mvn compile                    # compile src/main/java
mvn test-compile               # compile src/test/java
mvn test                       # run all tests
mvn clean test                 # clean target/ then run tests
mvn test -Dtest=LoginTest      # run specific class
mvn test -Dgroups=smoke        # run by group
mvn package                    # create JAR
mvn dependency:resolve         # download all deps
mvn dependency:tree            # show dep tree (find conflicts)
mvn versions:display-dependency-updates  # show outdated deps
```

---

## 17. Interview Q&A — Core Java

**Q: What is the difference between `==` and `.equals()` for Strings?**
```
== compares REFERENCES (memory addresses). Two String objects with same content
are different objects → == returns false.
.equals() compares CONTENT → returns true if characters match.
Always use .equals() for String comparison. Never ==.

String a = new String("hello");
String b = new String("hello");
a == b         // false — different objects in memory
a.equals(b)    // true  — same content
```

**Q: What is the difference between ArrayList and LinkedList?**
```
ArrayList: backed by array, fast random access O(1), slow insert/delete in middle O(n)
LinkedList: doubly linked nodes, slow random access O(n), fast insert/delete O(1)
Use ArrayList for most cases. Use LinkedList as queue/deque.
```

**Q: What is autoboxing?**
```
Automatic conversion between primitive types and wrapper classes.
int → Integer (boxing), Integer → int (unboxing) — done automatically.
List<Integer> list = new ArrayList<>();
list.add(5);    // autoboxing: 5 (int) → Integer.valueOf(5)
int x = list.get(0);  // unboxing: Integer → int
```

**Q: What is the difference between checked and unchecked exceptions?**
```
Checked: compiler forces you to handle or declare (throws). E.g. IOException, SQLException.
Unchecked (RuntimeException): no requirement. E.g. NullPointerException, IllegalArgumentException.
In automation: most exceptions are unchecked — NoSuchElementException, TimeoutException.
```

**Q: What is String immutability and why does it matter?**
```
Strings cannot be changed once created. Operations like replace() return a NEW String.
String s = "hello";
s.replace("h", "j");  // s is still "hello" — replace returns new String
s = s.replace("h", "j");  // now s points to "jello"

Why it matters: Strings are safe to share between threads. String pool is possible.
Use StringBuilder when building strings in a loop — it IS mutable.
```

**Q: What is the difference between `final`, `finally`, and `finalize()`?**
```
final:      modifier — variable can't be reassigned, method can't be overridden, class can't be extended
finally:    block in try-catch — always executes (good for cleanup like driver.quit())
finalize(): deprecated method called by GC before object is collected — don't use
```

**Q: What is a static method and when do you use it?**
```
Static method belongs to the CLASS, not an instance. Can be called without creating an object.
Use for utility methods that don't need object state.
Math.sqrt(), Collections.sort(), DriverFactory.createDriver() — all static.
Cannot access instance variables or this keyword.
```

**Q: What happens if you don't call `super()` in a child class constructor?**
```
Java automatically inserts a call to the parent's no-argument constructor.
If the parent has no no-arg constructor (only parameterised), you MUST explicitly call super(args)
or you get a compile error.
```

**Q: What is `this` and `super` keyword?**
```
this → refers to the current instance. Used to distinguish field from parameter, call own constructor.
super → refers to the parent class. Used to call parent's constructor or method.

public class Dog extends Animal {
    public Dog(String name) {
        super(name);   // call Animal's constructor
    }
    @Override
    public void speak() {
        super.speak();   // call Animal's speak()
        System.out.println("Woof!");
    }
}
```

**Q: What is garbage collection?**
```
JVM automatically reclaims memory from objects no longer referenced.
You don't need to free memory manually (unlike C/C++).
GC runs in background — you cannot predict when.
System.gc() hints GC to run but doesn't guarantee it.
In automation: don't cache WebDriver instances longer than needed — let GC collect them.
```

---

## 18. Interview Q&A — OOP

**Q: What are the 4 pillars of OOP?**
```
1. Encapsulation: hide data (private fields), expose via methods (getters/setters). Control what's accessible.
2. Inheritance: child class reuses parent class code. BaseTest → LoginTest.
3. Polymorphism: same method, different behaviour. @Override. WebDriver works for Chrome/Firefox/Edge.
4. Abstraction: hide complexity, show only what's needed. Abstract classes, interfaces.
```

**Q: What is the difference between overloading and overriding?**
```
Overloading: SAME class, SAME method name, DIFFERENT parameters. Compile-time decision.
  void login(String user, String pass)
  void login(String user, String pass, boolean rememberMe)

Overriding: DIFFERENT class (parent/child), SAME signature. Runtime decision.
  Parent: public void speak() { "..." }
  Child:  @Override public void speak() { "Woof!" }
```

**Q: What is an interface and why use it in automation?**
```
Interface defines a CONTRACT — what methods a class must have, without saying HOW.
In Selenium: WebDriver is an interface. ChromeDriver, FirefoxDriver implement it.
Your code uses WebDriver type → can swap Chrome for Firefox without changing test code.
Polymorphism through interfaces = your automation works with ANY browser that implements WebDriver.
```

**Q: Can a class extend multiple classes?**
```
NO — Java doesn't support multiple inheritance for classes (diamond problem).
But a class CAN implement multiple interfaces.
class MyClass extends BaseClass implements Clickable, Serializable { }
```

**Q: What is abstraction and how is it different from encapsulation?**
```
Encapsulation: HIDES data (private fields). HOW data is stored.
Abstraction:   HIDES complexity (abstract methods, interfaces). WHAT a class does, not HOW.

Encapsulation: "I have a password field but you can't access it directly"
Abstraction:   "I have a login() method — you call it without knowing it uses Selenium internally"
```

**Q: What is a constructor and can it be private?**
```
Constructor initialises a new object. Same name as class, no return type.
YES, constructor can be private — used in Singleton pattern to prevent outside instantiation.
Also used in utility classes (no instance needed):
public class StringUtils { private StringUtils() {} public static String... }
```

**Q: What is the difference between an abstract class and an interface?**
```
Abstract class: can have constructor, fields, concrete methods, abstract methods. Extend ONE only.
Interface: no constructor, only constants + abstract methods + default/static methods. Implement MANY.

Use abstract class when: classes share code (BaseTest, BasePage)
Use interface when:      defining a capability/contract (Clickable, Drivable, WebDriver)
```

**Q: What is method hiding vs method overriding?**
```
Overriding: instance method in child replaces parent's — runtime polymorphism (@Override)
Hiding:     static method in child hides parent's static method — NOT polymorphism
            Static methods are resolved at compile time by reference type, not object type.
```

**Q: What is the `final` keyword on a class, method, and variable?**
```
final class:    cannot be extended — String is final in Java
final method:   cannot be overridden by subclass
final variable: cannot be reassigned (value is fixed after assignment)
  final int MAX = 100;  // constant
  MAX = 200;  // compile error

In automation: ApiConstants fields are public static final — shared constants that never change.
```

**Q: What design patterns do you use in automation and why?**
```
Page Object Model (POM): encapsulates page locators, single update point when UI changes
Singleton: one ConfigReader or DriverFactory instance shared across the suite
Factory: DriverFactory.createDriver("chrome") hides which driver class is instantiated
Builder: readable test data construction — new User.Builder().name("x").email("y").build()
Strategy: swap wait strategy (explicit/fluent) without changing page object code
```
