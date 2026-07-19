# Google Cloud Spanner — Testing Guide | Cloud Database QA

> Senior QA Interview Preparation — Cloud Database Testing with GCP

---

## SECTION 1 — What is Google Cloud Spanner

### Overview

Google Cloud Spanner is a **fully managed, globally distributed, horizontally scalable relational database** built and operated by Google. It is used by large-scale enterprise applications that need to serve users across multiple geographic regions while maintaining strict data consistency and supporting full SQL.

**Key characteristics:**
- **Fully managed:** Google handles infrastructure, replication, failover, and patching
- **Globally distributed:** Data is replicated across multiple regions (e.g. US, Europe, Asia) simultaneously
- **Horizontally scalable:** Add processing nodes to increase throughput without schema changes or downtime
- **Strongly consistent:** Unlike most distributed databases, Spanner provides globally consistent reads and serialisable transactions — all nodes see the same data at the same time
- **ACID guarantees:** Full ACID transactions across multiple rows, tables, and even across geographic regions
- **SQL support:** ANSI 2011 SQL with Google extensions

### Real-World Usage

Spanner is used where a traditional single-region database cannot scale but a NoSQL database's eventual consistency is not acceptable:
- Financial transaction systems (strict consistency required)
- Global inventory management (accurate stock counts across regions)
- Gaming leaderboards (consistent global rankings)
- Multi-region SaaS platforms

### Relevance to Kupeshanth

Qoria Lanka operates on Google Cloud Platform (GCP). Testing against Spanner is a realistic expectation for a Senior QA Engineer at Qoria — you may need to verify data written by APIs lands correctly in Spanner, validate migration scripts, or run integration tests using the Spanner emulator in CI.

---

## SECTION 2 — How Spanner Differs from Traditional SQL Databases

Understanding these differences is critical for writing correct tests — many patterns from MySQL/PostgreSQL do not apply.

### No Auto-Increment / No Sequences
MySQL and PostgreSQL support `AUTO_INCREMENT` or `SERIAL` for generating unique integer IDs automatically. Spanner does NOT support this.

**Why:** Sequential integer keys create "hot spots" — all inserts go to the same shard/region, creating a write bottleneck.

**Spanner approach:** Use UUIDs (universally unique identifiers) or application-generated IDs that distribute writes evenly.

```java
// In MySQL: the DB generates the ID
// INSERT INTO orders (amount) VALUES (99.99);  -- DB assigns id=1001

// In Spanner: your application generates the ID
import java.util.UUID;
String orderId = UUID.randomUUID().toString();  // e.g. "f47ac10b-58cc-4372-a567-0e02b2c3d479"
// Then insert: INSERT INTO orders (order_id, amount) VALUES (@orderId, @amount)
```

**QA implication:** Test data insertion scripts must generate IDs, not rely on the database generating them.

### No ENUM Type
MySQL supports `ENUM('ACTIVE', 'INACTIVE', 'PENDING')`. Spanner does NOT.

**Spanner approach:** Use `STRING(50)` column + application-level or CHECK constraint validation.

```sql
-- In Spanner, you add a CHECK constraint manually:
CREATE TABLE orders (
    order_id STRING(36) NOT NULL,
    status   STRING(20) NOT NULL,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'ACTIVE', 'CANCELLED', 'COMPLETED')),
) PRIMARY KEY (order_id);
```

**QA implication:** Test that the CHECK constraint correctly rejects invalid status values.

### Interleaved Tables (Parent-Child Locality)

Interleaved tables are a Spanner concept for storing child rows physically adjacent to their parent row for read performance. They are defined at schema level:

```sql
-- Parent table
CREATE TABLE orders (
    order_id   STRING(36) NOT NULL,
    customer_id STRING(36) NOT NULL,
    total_amount FLOAT64,
) PRIMARY KEY (order_id);

-- Child table interleaved IN PARENT orders
CREATE TABLE order_items (
    order_id     STRING(36) NOT NULL,    -- same PK prefix as parent
    item_id      STRING(36) NOT NULL,
    product_name STRING(200),
    quantity     INT64,
    price        FLOAT64,
) PRIMARY KEY (order_id, item_id),
  INTERLEAVE IN PARENT orders ON DELETE CASCADE;
```

`ON DELETE CASCADE` means deleting the parent `order` row also deletes all its `order_items` rows.

**QA implication:** Test that deleting a parent record cascades correctly to child records. Also test that inserting a child record for a non-existent parent is rejected.

### Commit Timestamps

Spanner can auto-populate a column with the exact server timestamp of the commit:

```sql
CREATE TABLE orders (
    order_id   STRING(36) NOT NULL,
    created_at TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp=true),
) PRIMARY KEY (order_id);
```

When inserting, use the sentinel value `PENDING_COMMIT_TIMESTAMP()` — Spanner replaces it with the real commit timestamp:
```java
Mutation.newInsertBuilder("orders")
    .set("order_id").to("f47ac10b-...")
    .set("created_at").to(Value.COMMIT_TIMESTAMP)  // Spanner fills this in
    .build()
```

**QA implication:** You cannot predict the exact value of a commit timestamp. Test that it is NOT NULL, that it is a valid recent timestamp, and that it is after the test start time.

### Read Staleness: Stale Reads vs Strong Reads

**Strong read:** Reads the most up-to-date data — guaranteed to see all committed writes. Slower because Spanner must coordinate across regions.

**Stale read:** Reads data as of a past timestamp (e.g. 15 seconds ago). Faster because it does not need region coordination. Used for analytics/reporting where slight lag is acceptable.

**QA implication:** If your application uses stale reads, your test assertions may read data before the write has fully propagated. Use strong reads in tests for deterministic results. Know the API option:
```java
// Strong read (always use this in tests)
ReadContext ctx = dbClient.singleUse();

// Stale read (application performance optimisation, avoid in tests)
ReadContext ctx = dbClient.singleUse(TimestampBound.ofMaxStaleness(15, TimeUnit.SECONDS));
```

### Mutations vs DML

Two ways to write data in Spanner:

**Mutations** — a batch of changes applied atomically at the end of a transaction. No individual round-trips per row:
```java
List<Mutation> mutations = new ArrayList<>();
mutations.add(Mutation.newInsertBuilder("orders")
    .set("order_id").to("abc123")
    .set("amount").to(99.99)
    .build());
dbClient.write(mutations);  // all mutations applied in one commit
```

**DML (Data Manipulation Language)** — standard SQL `INSERT`, `UPDATE`, `DELETE` inside a read-write transaction. Familiar syntax but slightly higher latency per statement:
```java
dbClient.readWriteTransaction().run(transaction -> {
    transaction.executeUpdate(
        Statement.of("INSERT INTO orders (order_id, amount) VALUES ('abc123', 99.99)")
    );
    return null;
});
```

**QA implication:** Your test setup and teardown may use either approach. Mutations are preferred for bulk test data setup (faster). DML is clearer for individual test data operations.

---

## SECTION 3 — Testing Spanner with the Emulator

### What is the Spanner Emulator?
The Cloud Spanner Emulator is a local, in-process implementation of Spanner. It supports the full Spanner API and SQL dialect without requiring a real GCP project, billing account, or network access. It is free and runs on your laptop or in CI.

**Limitations of the emulator:**
- No real distributed performance (single node)
- No global region replication
- Not suitable for performance/load testing
- Some advanced features may lag behind the real Spanner

For functional testing, the emulator is a complete substitute.

### Install and Start the Emulator

```bash
# Prerequisites: Google Cloud CLI (gcloud) installed

# Install the emulator component
gcloud components install cloud-spanner-emulator

# Start the emulator (runs in the foreground — open a new terminal for other commands)
gcloud emulators spanner start
# Output: Cloud Spanner emulator running at [localhost:9010]

# In your test/CI environment, point all Spanner client calls to the emulator:
export SPANNER_EMULATOR_HOST=localhost:9010
```

### Create an Instance and Database in the Emulator

```bash
# Use emulator config (not a real GCP region)
gcloud spanner instances create test-instance \
  --config=emulator-config \
  --description="Test instance for automated tests" \
  --nodes=1

# Create a database in the instance
gcloud spanner databases create test-db \
  --instance=test-instance

# Apply your schema DDL
gcloud spanner databases ddl update test-db \
  --instance=test-instance \
  --ddl="CREATE TABLE orders (
    order_id     STRING(36) NOT NULL,
    customer_id  STRING(36) NOT NULL,
    amount       FLOAT64 NOT NULL,
    status       STRING(20) NOT NULL,
    created_at   TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp=true),
  ) PRIMARY KEY (order_id)"
```

### Emulator in CI (Docker)

```yaml
# docker-compose.yml for CI
version: '3.8'
services:
  spanner-emulator:
    image: gcr.io/cloud-spanner-emulator/emulator:latest
    ports:
      - "9010:9010"
      - "9020:9020"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9020"]
      interval: 5s
      timeout: 3s
      retries: 5
```

```yaml
# GitHub Actions: start emulator before tests
- name: Start Spanner Emulator
  run: |
    docker run -d --name spanner-emulator \
      -p 9010:9010 -p 9020:9020 \
      gcr.io/cloud-spanner-emulator/emulator:latest

- name: Set emulator env var
  run: echo "SPANNER_EMULATOR_HOST=localhost:9010" >> $GITHUB_ENV

- name: Run tests
  run: mvn test
```

---

## SECTION 4 — SQL Queries for QA in Spanner

### Standard SQL (Same as MySQL/PostgreSQL)

```sql
-- Basic SELECT with WHERE
SELECT order_id, customer_id, amount, status
FROM orders
WHERE status = 'PENDING'
  AND amount > 50.00
ORDER BY amount DESC
LIMIT 20;

-- JOIN
SELECT o.order_id, o.amount, c.email
FROM orders o
JOIN customers c ON o.customer_id = c.customer_id
WHERE o.status = 'ACTIVE';

-- Aggregation
SELECT
    status,
    COUNT(*) AS order_count,
    SUM(amount) AS total_revenue,
    AVG(amount) AS avg_order_value
FROM orders
GROUP BY status
ORDER BY order_count DESC;
```

### Spanner-Specific: Timestamp Queries

```sql
-- Records created in the last 24 hours
SELECT order_id, created_at
FROM orders
WHERE created_at > TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY);

-- Records in a specific date range
SELECT order_id, amount
FROM orders
WHERE created_at BETWEEN TIMESTAMP('2024-01-01T00:00:00Z')
                     AND TIMESTAMP('2024-01-31T23:59:59Z');

-- Most recent record (useful in QA to get the record just created by the API)
SELECT order_id, amount, created_at
FROM orders
ORDER BY created_at DESC
LIMIT 1;
```

### Spanner-Specific: ARRAY and UNNEST

```sql
-- Spanner supports ARRAY columns
-- Unnest an array to query individual elements
SELECT o.order_id, tag
FROM orders o, UNNEST(o.tags) AS tag
WHERE tag = 'PRIORITY';
```

### Spanner-Specific: CTEs (WITH clause)

```sql
-- Common Table Expression for readability
WITH recent_orders AS (
    SELECT order_id, customer_id, amount
    FROM orders
    WHERE created_at > TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 7 DAY)
),
high_value_customers AS (
    SELECT customer_id, SUM(amount) AS total_spend
    FROM recent_orders
    GROUP BY customer_id
    HAVING SUM(amount) > 1000
)
SELECT c.email, h.total_spend
FROM high_value_customers h
JOIN customers c ON h.customer_id = c.customer_id
ORDER BY h.total_spend DESC;
```

### Spanner-Specific: JSON Column

```sql
-- Spanner supports JSON column type (since 2022)
-- Query a field inside a JSON column
SELECT
    order_id,
    JSON_VALUE(metadata, '$.source') AS order_source,
    JSON_VALUE(metadata, '$.campaign_id') AS campaign_id
FROM orders
WHERE JSON_VALUE(metadata, '$.source') = 'mobile_app';
```

### QA Validation Queries

```sql
-- Completeness: count records created in the last test run
SELECT COUNT(*) AS records_created_last_run
FROM orders
WHERE created_at > TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE);

-- Integrity: find order_items with no matching parent order (orphan check)
SELECT oi.order_id, oi.item_id
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL;

-- Validity: find invalid status values
SELECT order_id, status
FROM orders
WHERE status NOT IN ('PENDING', 'ACTIVE', 'CANCELLED', 'COMPLETED');

-- Uniqueness: find duplicate order IDs (should return zero rows)
SELECT order_id, COUNT(*) AS count
FROM orders
GROUP BY order_id
HAVING COUNT(*) > 1;
```

---

## SECTION 5 — Testing Spanner via Java Client

### Maven Dependency

```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-spanner</artifactId>
    <version>6.51.0</version>
</dependency>
```

### Complete Java Code — Connecting to Spanner (Emulator and Real)

```java
package com.example.spanner;

import com.google.cloud.spanner.*;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.SpannerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpannerTestHelper {

    private static final String PROJECT_ID  = "test-project";
    private static final String INSTANCE_ID = "test-instance";
    private static final String DATABASE_ID = "test-db";

    private final Spanner spanner;
    private final DatabaseClient dbClient;

    public SpannerTestHelper() {
        SpannerOptions.Builder optionsBuilder = SpannerOptions.newBuilder()
            .setProjectId(PROJECT_ID);

        // If SPANNER_EMULATOR_HOST is set, the client library automatically
        // uses the emulator — no code change needed
        // export SPANNER_EMULATOR_HOST=localhost:9010

        this.spanner = optionsBuilder.build().getService();
        this.dbClient = spanner.getDatabaseClient(
            DatabaseId.of(PROJECT_ID, INSTANCE_ID, DATABASE_ID)
        );
    }

    // ── Read a single row by primary key ─────────────────────────

    public Struct readOrderById(String orderId) {
        return dbClient.singleUse()      // strong read
            .readRow(
                "orders",
                Key.of(orderId),
                List.of("order_id", "customer_id", "amount", "status", "created_at")
            );
    }

    // ── Read multiple rows with a SQL query ──────────────────────

    public List<Struct> queryOrdersByCustomer(String customerId) {
        List<Struct> results = new ArrayList<>();
        try (ResultSet rs = dbClient.singleUse().executeQuery(
            Statement.newBuilder("SELECT order_id, amount, status, created_at "
                + "FROM orders WHERE customer_id = @customerId ORDER BY created_at DESC")
                .bind("customerId").to(customerId)
                .build()
        )) {
            while (rs.next()) {
                results.add(rs.getCurrentRowAsStruct());
            }
        }
        return results;
    }

    // ── Insert test data using Mutation API ──────────────────────

    public String insertTestOrder(String customerId, double amount) {
        String orderId = UUID.randomUUID().toString();
        List<Mutation> mutations = new ArrayList<>();
        mutations.add(
            Mutation.newInsertBuilder("orders")
                .set("order_id").to(orderId)
                .set("customer_id").to(customerId)
                .set("amount").to(amount)
                .set("status").to("PENDING")
                .set("created_at").to(Value.COMMIT_TIMESTAMP)
                .build()
        );
        dbClient.write(mutations);
        return orderId;  // return the generated ID so the test can use it in assertions
    }

    // ── Delete test data in teardown ─────────────────────────────

    public void deleteOrder(String orderId) {
        dbClient.write(List.of(
            Mutation.delete("orders", Key.of(orderId))
        ));
    }

    // Delete by a test-run marker (clean up all data from this test run)
    public void deleteOrdersByCustomer(String customerId) {
        dbClient.readWriteTransaction().run(transaction -> {
            transaction.executeUpdate(
                Statement.newBuilder("DELETE FROM orders WHERE customer_id = @customerId")
                    .bind("customerId").to(customerId)
                    .build()
            );
            return null;
        });
    }

    // ── Transaction example ───────────────────────────────────────

    public void transferAmount(String fromOrderId, String toOrderId, double transferAmount) {
        dbClient.readWriteTransaction().run(transaction -> {
            // Read both rows in the same transaction (consistent snapshot)
            Struct from = transaction.readRow("orders", Key.of(fromOrderId), List.of("amount"));
            Struct to   = transaction.readRow("orders", Key.of(toOrderId),   List.of("amount"));

            double newFromAmount = from.getDouble("amount") - transferAmount;
            double newToAmount   = to.getDouble("amount")   + transferAmount;

            if (newFromAmount < 0) {
                throw new SpannerException(ErrorCode.FAILED_PRECONDITION, "Insufficient balance");
            }

            transaction.buffer(List.of(
                Mutation.newUpdateBuilder("orders").set("order_id").to(fromOrderId).set("amount").to(newFromAmount).build(),
                Mutation.newUpdateBuilder("orders").set("order_id").to(toOrderId).set("amount").to(newToAmount).build()
            ));
            return null;
        });
    }

    // ── Cleanup ───────────────────────────────────────────────────

    public void close() {
        spanner.close();
    }
}
```

### Using SpannerTestHelper in a TestNG Test

```java
package com.example.tests;

import com.example.spanner.SpannerTestHelper;
import com.google.cloud.spanner.Struct;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.*;
import static org.testng.Assert.*;

public class OrderApiSpannerTest {

    private SpannerTestHelper spannerHelper;
    private static final String TEST_CUSTOMER_ID = "TEST-CUST-" + java.util.UUID.randomUUID();

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = "http://localhost:8080";
        spannerHelper = new SpannerTestHelper();
    }

    @Test
    public void postOrder_writesCorrectDataToSpanner() {
        // 1. Send API request
        Response response = RestAssured.given()
            .contentType("application/json")
            .body("{ \"customerId\": \"" + TEST_CUSTOMER_ID + "\", \"amount\": 149.99 }")
            .post("/api/orders");

        assertEquals(response.getStatusCode(), 201, "Expected 201 Created");

        // 2. Get the order ID from the response
        String orderId = response.jsonPath().getString("orderId");
        assertNotNull(orderId, "orderId should be in the response");

        // 3. Verify the data landed correctly in Spanner
        Struct orderInDb = spannerHelper.readOrderById(orderId);
        assertNotNull(orderInDb, "Order should exist in Spanner after POST");
        assertEquals(orderInDb.getString("customer_id"), TEST_CUSTOMER_ID);
        assertEquals(orderInDb.getDouble("amount"), 149.99, 0.001);
        assertEquals(orderInDb.getString("status"), "PENDING");
        assertFalse(orderInDb.isNull("created_at"), "created_at should be populated by Spanner");
    }

    @AfterClass
    public void tearDown() {
        // Clean up all test data created by this test class
        spannerHelper.deleteOrdersByCustomer(TEST_CUSTOMER_ID);
        spannerHelper.close();
    }
}
```

---

## SECTION 6 — Spanner-Specific Testing Considerations

### Strong Reads vs Stale Reads in Tests

| | Strong Read | Stale Read |
|---|---|---|
| **What it is** | Reads the very latest committed data | Reads data as of N seconds ago |
| **Consistency** | Guaranteed up-to-date | May miss recent writes |
| **Performance** | Slower (cross-region coordination) | Faster |
| **Use in tests** | Always — guarantees you see what you just wrote | Never — may cause intermittent test failures |

**Problem scenario:** Your test inserts a row, then immediately queries with a stale read. The stale read may not yet see the row, causing a false failure. Always use `dbClient.singleUse()` (strong read) in tests.

### Interleaved Table Testing

Verify the `ON DELETE CASCADE` behaviour:
```java
@Test
public void deletingParentOrder_cascadesDeleteToOrderItems() {
    // Setup: create parent order and child items
    String orderId = spannerHelper.insertTestOrder(TEST_CUSTOMER_ID, 200.00);
    spannerHelper.insertOrderItem(orderId, "PROD-1", 2, 100.00);  // 2 items

    // Verify items exist before delete
    List<Struct> itemsBefore = spannerHelper.queryItemsByOrder(orderId);
    assertEquals(itemsBefore.size(), 2);

    // Act: delete the parent order
    spannerHelper.deleteOrder(orderId);

    // Assert: all child items are also deleted (cascade)
    List<Struct> itemsAfter = spannerHelper.queryItemsByOrder(orderId);
    assertEquals(itemsAfter.size(), 0, "Cascade delete should remove all order items");
}
```

Also test that inserting a child for a non-existent parent fails:
```java
@Test(expectedExceptions = SpannerException.class)
public void insertOrderItem_forNonExistentOrder_throwsException() {
    // Interleaved table: inserting a child row for a non-existent parent is rejected
    spannerHelper.insertOrderItem("NONEXISTENT-ORDER-ID", "PROD-1", 1, 50.00);
}
```

### Spanner Commit Latency

Spanner's commit involves consensus across multiple replicas. In practice this takes ~5–20ms for regional instances, ~100ms for multi-region instances.

**Implication for tests:** In most cases your Java test will see the write immediately because the Spanner client waits for the commit to complete before returning. You do NOT need to `Thread.sleep()` after a write before reading. However, if your test reads data through a caching layer or a service that uses stale reads, you may need to account for propagation time.

### Global Consistency

Unlike eventually consistent databases (Cassandra, DynamoDB in certain modes), Spanner guarantees that once a write commits, **every subsequent read anywhere in the world sees it**. This simplifies testing significantly — you never have to account for "the write hasn't propagated yet" scenarios (as long as you use strong reads).

### Testing with Multiple Regions

In production, Spanner instances can span multiple geographic regions. For testing:
- The emulator simulates a single-node instance — no actual multi-region behaviour
- Functional tests do not need multi-region instances
- Performance/latency characteristics differ between emulator and real multi-region Spanner — do not benchmark on the emulator
- When testing multi-region failure scenarios, you need a real GCP Spanner instance in a testing GCP project

---

## SECTION 7 — RestAssured + Spanner Integration Test Pattern

This is the most common QA pattern: call the API, then verify the data landed correctly in Spanner.

```java
package com.example.tests;

import com.example.spanner.SpannerTestHelper;
import com.google.cloud.spanner.Struct;
import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.*;
import static org.testng.Assert.*;

@Epic("Order Management")
@Feature("Order Persistence")
public class OrderPersistenceIntegrationTest {

    private SpannerTestHelper spanner;
    // Unique prefix to identify test data from this run
    private final String TEST_RUN_ID = "TEST-" + java.util.UUID.randomUUID().toString().substring(0, 8);

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = System.getProperty("api.base.url", "http://localhost:8080");
        RestAssured.filters(new AllureRestAssured());  // auto-log requests/responses to Allure
        spanner = new SpannerTestHelper();
    }

    // ─── Pattern 1: POST → verify in Spanner ─────────────────────

    @Test
    @Story("POST /api/orders persists order with correct fields in Spanner")
    @Severity(SeverityLevel.BLOCKER)
    public void postOrder_persistsCorrectlyInSpanner() {
        String customerId = TEST_RUN_ID + "-CUST-1";
        double amount = 249.99;
        String type = "PRIORITY";

        // STEP 1: Call the API
        Response createResponse = createOrderViaApi(customerId, amount, type);
        assertEquals(createResponse.getStatusCode(), 201);
        String orderId = createResponse.jsonPath().getString("orderId");

        // STEP 2: Verify in Spanner
        Struct order = spanner.readOrderById(orderId);
        assertNotNull(order, "Order not found in Spanner — write did not persist");
        assertEquals(order.getString("customer_id"), customerId, "customer_id mismatch");
        assertEquals(order.getDouble("amount"),      amount, 0.001,   "amount mismatch");
        assertEquals(order.getString("status"),      "PENDING",       "initial status should be PENDING");
        assertEquals(order.getString("type"),        type,            "type mismatch");
        assertFalse(order.isNull("created_at"),     "created_at should be auto-populated");
    }

    // ─── Pattern 2: POST → PATCH → verify state transition in Spanner ──

    @Test
    @Story("PATCH /api/orders/{id}/confirm updates status to CONFIRMED in Spanner")
    @Severity(SeverityLevel.CRITICAL)
    public void confirmOrder_updatesStatusInSpanner() {
        // Seed: create an order via API
        String customerId = TEST_RUN_ID + "-CUST-2";
        Response createResponse = createOrderViaApi(customerId, 99.99, "STANDARD");
        String orderId = createResponse.jsonPath().getString("orderId");

        // Act: confirm the order
        Response confirmResponse = RestAssured.given()
            .contentType("application/json")
            .patch("/api/orders/" + orderId + "/confirm");
        assertEquals(confirmResponse.getStatusCode(), 200);

        // Verify: status updated in Spanner
        Struct order = spanner.readOrderById(orderId);
        assertEquals(order.getString("status"), "CONFIRMED",
            "Status should be CONFIRMED after calling /confirm endpoint");
        assertFalse(order.isNull("confirmed_at"),
            "confirmed_at timestamp should be populated after confirmation");
    }

    // ─── Pattern 3: DELETE → verify record gone from Spanner ─────

    @Test
    @Story("DELETE /api/orders/{id} removes record from Spanner")
    @Severity(SeverityLevel.CRITICAL)
    public void deleteOrder_removesRecordFromSpanner() {
        String customerId = TEST_RUN_ID + "-CUST-3";
        Response createResponse = createOrderViaApi(customerId, 49.99, "STANDARD");
        String orderId = createResponse.jsonPath().getString("orderId");

        // Act: delete via API
        Response deleteResponse = RestAssured.given().delete("/api/orders/" + orderId);
        assertEquals(deleteResponse.getStatusCode(), 204);

        // Verify: record no longer in Spanner
        Struct order = spanner.readOrderById(orderId);
        assertNull(order, "Order should not exist in Spanner after DELETE");
    }

    // ─── Helper ──────────────────────────────────────────────────

    @Step("Call POST /api/orders: customerId={customerId}, amount={amount}, type={type}")
    private Response createOrderViaApi(String customerId, double amount, String type) {
        String payload = String.format(
            "{ \"customerId\": \"%s\", \"amount\": %.2f, \"type\": \"%s\" }",
            customerId, amount, type
        );
        return RestAssured.given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + System.getenv("TEST_AUTH_TOKEN"))
            .body(payload)
            .post("/api/orders");
    }

    @AfterClass
    public void tearDown() {
        // Clean up all test data from this run by the test prefix
        spanner.deleteOrdersByCustomer(TEST_RUN_ID + "-CUST-1");
        spanner.deleteOrdersByCustomer(TEST_RUN_ID + "-CUST-2");
        spanner.deleteOrdersByCustomer(TEST_RUN_ID + "-CUST-3");
        spanner.close();
    }
}
```

---

## SECTION 8 — Common Spanner Issues in Testing

### Quota Errors on the Emulator

**Symptom:** `RESOURCE_EXHAUSTED: Quota exceeded` error when running many tests.

**Cause:** The emulator has limits on the number of concurrent sessions, transactions, or database operations.

**Fix:**
```bash
# Restart the emulator to reset its state
gcloud emulators spanner stop
gcloud emulators spanner start

# Or if running via Docker:
docker restart spanner-emulator
```

**Prevention:** Always call `spanner.close()` / `session.close()` in your test teardown. Unclosed sessions accumulate and exhaust the session pool.

### Hot Spots from Sequential Keys

**Symptom:** Excellent performance locally but severe write throughput degradation under load in production Spanner.

**Cause:** Sequential integer IDs (1, 2, 3, 4...) mean all inserts go to the same Spanner split (shard), creating a hot spot. Spanner cannot distribute the load.

**Fix in tests:** Generate UUIDs. They are random and distribute evenly across splits.

```java
// BAD — do not use for Spanner primary keys
long orderId = System.currentTimeMillis();  // sequential, creates hot spot

// GOOD — random distribution
String orderId = UUID.randomUUID().toString();

// ALTERNATIVE — bit-reversed integers (sequential externally, random internally)
// Used when you need a human-readable sequential ID but want Spanner distribution
```

**QA role:** Add a test that inserts 10,000 rows and measures throughput. If throughput degrades significantly as count increases, suspect a hot-spot key design.

### Session Pool Exhaustion

**Symptom:** `RESOURCE_EXHAUSTED: Too many active sessions` or tests hanging waiting for a session.

**Cause:** Your test code opens Spanner connections/sessions but does not close them in teardown.

**Fix:**
```java
// Always use try-with-resources or call close() in @AfterClass

// Option 1: try-with-resources
try (Spanner spanner = SpannerOptions.newBuilder().build().getService()) {
    DatabaseClient db = spanner.getDatabaseClient(...);
    // use db
}  // spanner.close() called automatically

// Option 2: @AfterClass teardown
@AfterClass
public void tearDown() {
    if (spanner != null) {
        spanner.close();  // releases all sessions back to the pool
    }
}
```

### Timestamp in Future Error

**Symptom:** `INVALID_ARGUMENT: Timestamp is in the future` when inserting with `PENDING_COMMIT_TIMESTAMP()`.

**Cause:** The `PENDING_COMMIT_TIMESTAMP()` sentinel value is set in the wrong field type, or the field is not defined with `OPTIONS (allow_commit_timestamp=true)`.

**Fix:**
```java
// Correct way to use commit timestamp:
Mutation.newInsertBuilder("orders")
    .set("order_id").to("abc123")
    .set("created_at").to(Value.COMMIT_TIMESTAMP)  // correct: Value.COMMIT_TIMESTAMP constant
    .build();

// Schema must have:
// created_at TIMESTAMP NOT NULL OPTIONS (allow_commit_timestamp=true)

// WRONG — do not set a future timestamp manually:
// .set("created_at").to(Timestamp.ofTimeMicroseconds(futureTime))  // may fail
```

---

## SECTION 9 — Interview Q&A

### Q1: What is Google Cloud Spanner and when would you use it?
**A:** Google Cloud Spanner is a fully managed, globally distributed relational database that provides horizontal scalability, ACID transactions, and strong consistency across multiple geographic regions. You would choose Spanner when your application needs to serve users globally, requires strict consistency (not eventual consistency), and has outgrown the vertical scaling limits of a traditional relational database like MySQL or PostgreSQL. Examples: a global payments platform where all regions must see the same balance, or a multi-region SaaS product that needs 99.999% availability with zero downtime for maintenance.

### Q2: How does Spanner differ from MySQL or PostgreSQL?
**A:** The key differences that matter for testing are: (1) No auto-increment — you must generate your own IDs (UUIDs) because sequential keys cause write hot spots; (2) No ENUM type — use STRING with CHECK constraints; (3) Interleaved tables — child rows are physically co-located with parent rows for performance, with optional cascade delete behaviour; (4) Two write APIs — Mutations (batch commit) and DML (SQL statements) — each with different performance characteristics; (5) Strong vs stale reads — Spanner lets you choose between guaranteed-current reads and faster slightly-stale reads; (6) Commit timestamps — Spanner can auto-populate a timestamp column with the exact server commit time. For testing, the most important differences are UUID keys, no sequences, and always using strong reads.

### Q3: How do you test with the Spanner emulator?
**A:** Set the environment variable `SPANNER_EMULATOR_HOST=localhost:9010` before running tests. The Spanner Java client library automatically detects this variable and routes all calls to the local emulator instead of real GCP — no code change needed. I create a test instance and database using `gcloud spanner` CLI commands, apply the schema DDL, then run tests against it. In CI I run the emulator as a Docker container (`gcr.io/cloud-spanner-emulator/emulator:latest`), start it before the test step, and set the env var in the GitHub Actions or Jenkins environment. The emulator supports the full Spanner API, so all functional tests that work on the emulator should work on real Spanner.

### Q4: What does "strong consistency" mean and why does it matter for testing?
**A:** Strong consistency means that once a write transaction commits to Spanner, every subsequent read — from any node, in any geographic region — is guaranteed to see that write. There is no "eventual consistency" propagation delay. This is a major advantage for testing: after calling `dbClient.write(mutations)`, the very next `dbClient.singleUse().readRow(...)` is guaranteed to see the written data. I do not need `Thread.sleep()` or retry logic waiting for data to propagate. By contrast, if I were testing against an eventually consistent database like Cassandra, I might need to poll and wait for reads to catch up to writes, which makes tests fragile. Always use strong reads (the default `singleUse()` context) in tests — stale reads can return data from before your test write.

### Q5: How does QA verify data written to Spanner via an API?
**A:** The standard pattern is: (1) Call the API endpoint (e.g. POST /api/orders) using RestAssured; (2) Extract the entity ID from the API response (e.g. `orderId`); (3) Query Spanner directly using the Java Spanner client with `dbClient.singleUse().readRow("orders", Key.of(orderId), columns)`; (4) Assert each field matches the expected value (correct customer ID, correct amount, status is PENDING, created_at is not null). This verifies the full write path — API received the request, business logic processed it correctly, and the data persisted to the database with the correct values. The test teardown deletes the test record from Spanner to keep the database clean.

### Q6: What is the difference between Spanner and Cloud SQL?
**A:** Both are Google Cloud managed databases but they serve different use cases. **Cloud SQL** is a managed version of MySQL, PostgreSQL, or SQL Server — familiar features, sequences, ENUMs, standard SQL — running on a single regional instance with vertical scaling. It is easier to migrate existing applications to. **Cloud Spanner** is a custom database engine built by Google — globally distributed, horizontally scalable, no hot-spot keys, strong global consistency. Spanner is more expensive and requires adapting application code (no sequences, UUID keys). Choose Cloud SQL when you need a familiar managed MySQL/PostgreSQL without global distribution needs. Choose Spanner when you need global reach, horizontal write scalability, and strong consistency across regions. From a QA perspective, testing Cloud SQL is essentially testing MySQL/PostgreSQL. Testing Spanner requires understanding its unique constraints.

### Q7: What are interleaved tables and what do you test about them?
**A:** Interleaved tables are a Spanner-specific physical co-location of parent and child rows — child rows are stored adjacent to their parent row on disk for faster reads. They are defined with `INTERLEAVE IN PARENT table_name` in the DDL. From a testing perspective, two behaviours require explicit testing: (1) **Cascade delete** — if the child table is created with `ON DELETE CASCADE`, deleting the parent row should automatically delete all its child rows; I write a test that creates a parent + multiple children, deletes the parent, then asserts the children are also gone; (2) **Parent existence enforcement** — you cannot insert a child row if its parent does not exist; I write a test that attempts to insert a child for a non-existent parent and asserts a `SpannerException` is thrown.

### Q8: How does your GCP experience at Qoria Lanka apply to Spanner testing?
**A:** At Qoria Lanka I work on a GCP-hosted platform, which means I am familiar with the GCP ecosystem — service accounts, IAM permissions, the gcloud CLI, GCP project structure, and working with managed GCP services in a CI/CD pipeline. For Spanner specifically, this translates to: knowing how to authenticate to GCP from CI (using service account JSON key or Workload Identity Federation), understanding GCP project and instance hierarchy (project > instance > database), and being comfortable with gcloud CLI commands for managing databases and applying DDL. I can set up the Spanner emulator in Docker for local and CI testing, write Java integration tests that switch between emulator (for CI) and real Spanner (for staging) via the `SPANNER_EMULATOR_HOST` env var, and interpret Spanner errors from GCP Cloud Logging when debugging test failures against a real GCP environment.

---

*Guide covers: Spanner concepts, differences from MySQL/PostgreSQL, emulator setup, Spanner SQL for QA, Java client code with full examples, integration test patterns with RestAssured, common testing issues, and interview Q&A.*
