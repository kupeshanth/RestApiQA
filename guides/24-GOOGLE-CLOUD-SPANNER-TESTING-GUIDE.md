# Google Cloud Spanner — Full Interview Q&A Guide
## Cloud Database Testing with GCP | Senior QA Interview Preparation

---

## SECTION 1 — SPANNER FUNDAMENTALS

---

**Q1: What is Google Cloud Spanner and when would you use it?**

**A:**

Google Cloud Spanner is a fully managed, globally distributed, horizontally scalable relational database built and operated by Google. It is the only database that simultaneously provides:
- **Global strong consistency**: once a write commits, every read anywhere in the world sees it
- **ACID transactions**: full serialisable transactions across multiple rows, tables, and geographic regions
- **Horizontal scalability**: add nodes to increase throughput without schema changes or downtime
- **ANSI SQL**: standard SQL with Google extensions

You choose Spanner when your application has outgrown the vertical scaling limits of a traditional relational database but cannot accept the eventual consistency trade-offs of NoSQL solutions:

| Use Case | Why Spanner Fits |
|----------|-----------------|
| Global payments platform | All regions must see the same account balance; strong consistency is non-negotiable |
| Multi-region SaaS product | Users in Asia and Europe must see the same data; 99.999% availability required |
| Global inventory management | Stock counts must be accurate everywhere; overselling caused by stale reads is unacceptable |
| Gaming leaderboards | Rankings must be globally consistent; a player in Tokyo must see the same rank as one in London |

Real-world scale examples: Google's own Ads platform (F1 database), Snap, PayPal, Shopify, and many financial institutions use Spanner for workloads that are too large or too globally distributed for a single-region PostgreSQL or MySQL instance.

---

**Q2: How is Spanner different from MySQL and PostgreSQL? Why do these differences matter for testing?**

**A:**

Spanner has five differences that directly affect how you write test code:

**1. No auto-increment / no sequences**

MySQL: `id INT AUTO_INCREMENT` — the database generates 1, 2, 3, 4...
PostgreSQL: `id SERIAL` or `GENERATED ALWAYS AS IDENTITY`
Spanner: neither exists. Sequential integer keys create "hot spots" — all inserts hit the same partition/shard, creating a write bottleneck.

```java
// MySQL — database generates the ID automatically
// INSERT INTO orders (amount) VALUES (99.99); -- db returns id=1001

// Spanner — application must generate the ID
String orderId = UUID.randomUUID().toString(); // random UUID, distributes evenly
// INSERT INTO orders (order_id, amount) VALUES (@orderId, @amount)
```

QA impact: your test data insertion scripts must generate IDs. You cannot rely on `LAST_INSERT_ID()` or `currval('orders_id_seq')`.

**2. No ENUM type**

MySQL: `status ENUM('ACTIVE', 'PENDING', 'CANCELLED')`
Spanner: use `STRING(20)` with a `CHECK` constraint.

```sql
CREATE TABLE orders (
    order_id STRING(36) NOT NULL,
    status   STRING(20) NOT NULL,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'ACTIVE', 'CANCELLED', 'COMPLETED')),
) PRIMARY KEY (order_id);
```

QA impact: test that invalid status values are rejected by the CHECK constraint.

**3. Interleaved tables (no foreign key enforcement by default)**

Spanner uses interleaved tables to co-locate child rows with their parent for read performance. They have cascade delete behaviour but not the same enforcement as a MySQL foreign key.

**4. Two write APIs** — Mutations (batch) and DML (SQL statements) — different performance profiles.

**5. Strong vs stale reads** — choosing the wrong read mode in tests causes false failures.

---

**Q3: What is strong consistency vs eventual consistency, and why does it matter for test assertions?**

**A:**

**Eventual consistency** (used by Cassandra, DynamoDB in certain modes, Redis): a write is accepted immediately on one node and propagates to other nodes over time (milliseconds to seconds). A read immediately after a write may return the old value if it hits a node that hasn't received the update yet. You have to poll/retry in tests to confirm writes landed.

**Strong consistency** (used by Spanner): Spanner uses the TrueTime API and a Paxos consensus protocol. A write transaction commits only when a quorum of replicas (across geographic regions) have acknowledged it. Once committed, every subsequent read — from any node, anywhere — is guaranteed to see the write. There is no propagation delay.

Why this is a significant QA advantage:

```java
// In Spanner — this ALWAYS works. No sleep, no retry needed.
spannerHelper.insertTestOrder("CUST-42", 99.99);  // commit waits for quorum
Struct row = dbClient.singleUse().readRow("orders", Key.of(orderId), columns);
assertNotNull(row);  // guaranteed to see the row just inserted

// In Cassandra (eventual consistency) — this is UNRELIABLE without a delay
cassandraSession.execute("INSERT INTO orders ...");
Row row = cassandraSession.execute("SELECT * FROM orders WHERE id=?", id).one();
// row might be NULL if read hit a replica that hasn't received the write yet!
// You'd need: Thread.sleep(500) or a retry loop — fragile
```

The rule for Spanner tests: always use strong reads (`dbClient.singleUse()`). Never use stale reads (`TimestampBound.ofMaxStaleness(...)`) in test assertions — stale reads intentionally ignore recent writes and will cause false test failures.

---

**Q4: What is an interleaved table? What specifically do you test about it?**

**A:**

An interleaved table is a Spanner-specific way to physically store child rows adjacent to their parent row on disk. This co-location makes reads of a parent with its children extremely efficient because they are on the same storage shard. The relationship is declared in the DDL schema:

```sql
-- Parent table
CREATE TABLE orders (
    order_id    STRING(36) NOT NULL,
    customer_id STRING(36) NOT NULL,
    amount      FLOAT64    NOT NULL,
    status      STRING(20) NOT NULL,
) PRIMARY KEY (order_id);

-- Child table — interleaved IN PARENT orders
CREATE TABLE order_items (
    order_id     STRING(36) NOT NULL,  -- must include parent's PK prefix
    item_id      STRING(36) NOT NULL,
    product_name STRING(200),
    quantity     INT64,
    unit_price   FLOAT64,
) PRIMARY KEY (order_id, item_id),
  INTERLEAVE IN PARENT orders ON DELETE CASCADE;
-- ON DELETE CASCADE: deleting parent row also deletes all child rows
```

Two behaviors require explicit tests:

**Test 1: Cascade delete**
```java
@Test
public void deleteParentOrder_cascadesDeleteToAllOrderItems() {
    // Setup: create parent order + 3 items
    String orderId = spannerHelper.insertTestOrder(TEST_CUSTOMER_ID, 300.00);
    spannerHelper.insertOrderItem(orderId, "PROD-1", 1, 100.00);
    spannerHelper.insertOrderItem(orderId, "PROD-2", 2, 100.00);

    // Verify items exist before delete
    assertEquals(spannerHelper.queryItemsByOrder(orderId).size(), 3);

    // Act: delete the parent
    spannerHelper.deleteOrder(orderId);

    // Assert: all items are also gone (cascade)
    assertEquals(spannerHelper.queryItemsByOrder(orderId).size(), 0,
        "ON DELETE CASCADE should have removed all order_items when order was deleted");
}
```

**Test 2: Child insert rejected for non-existent parent**
```java
@Test(expectedExceptions = SpannerException.class)
public void insertOrderItem_forNonExistentParent_throwsSpannerException() {
    // Interleaved tables enforce parent existence at insert time
    spannerHelper.insertOrderItem("NONEXISTENT-ORDER-ID", "PROD-1", 1, 50.00);
    // SpannerException thrown: parent key does not exist
}
```

---

**Q5: Why does Spanner use UUIDs instead of sequential integer IDs?**

**A:**

Spanner distributes data across splits (shards) by primary key range. If you use sequential integers (1, 2, 3, 4, 5...), every new insert goes to the same split — the one holding the highest key values. This is called a "hot spot." A single split becomes the bottleneck for all write traffic, destroying horizontal scalability.

UUIDs are random 128-bit values (e.g. `f47ac10b-58cc-4372-a567-0e02b2c3d479`). Because they are random, consecutive inserts hit different splits, distributing write load evenly across all nodes.

```java
// BAD for Spanner — all writes hit the same shard (hot spot)
long id = System.currentTimeMillis();   // sequential: 1718000001, 1718000002, ...
int  id = ordersTable.getMaxId() + 1;   // sequential: 1001, 1002, 1003, ...

// GOOD for Spanner — random, distributes evenly
String id = UUID.randomUUID().toString();
// e.g. "550e8400-e29b-41d4-a716-446655440000"
```

QA role: verify that the application uses UUIDs. A load test can confirm — insert 10,000 rows and measure write throughput. If throughput collapses as inserts continue (rather than remaining flat), suspect a sequential key hot spot.

An alternative when you need human-readable sequential IDs: use bit-reversal. Take an auto-increment counter and bit-reverse the binary representation. The resulting values look random to Spanner's partitioner but decode to a sequential sequence in application logic. This is an advanced technique used by teams that need user-facing sequential IDs for customer support purposes.

---

**Q6: What is the Spanner emulator and how do you set it up for testing?**

**A:**

The Cloud Spanner Emulator is a local, in-process implementation of Spanner that runs on your laptop or in CI. It supports the full Spanner API and SQL dialect without requiring a real GCP project, billing account, or internet access. It is free.

Limitations:
- Single node — no real distributed behaviour or multi-region replication
- Not suitable for performance/load testing
- Some very new features may lag behind real Spanner

For functional testing, the emulator is a complete substitute.

**Setup on local machine:**
```bash
# Option 1: via gcloud CLI
gcloud components install cloud-spanner-emulator
gcloud emulators spanner start
# Output: Cloud Spanner emulator running at localhost:9010

# In a separate terminal — set the env var for your Java client
export SPANNER_EMULATOR_HOST=localhost:9010

# Create instance (use emulator-config, not a real GCP region)
gcloud config configurations create emulator
gcloud config set auth/disable_credentials true
gcloud config set project test-project
gcloud config set api_endpoint_overrides/spanner http://localhost:9020/

gcloud spanner instances create test-instance \
  --config=emulator-config \
  --description="Local test instance" \
  --nodes=1

# Create database
gcloud spanner databases create test-db --instance=test-instance

# Apply DDL schema
gcloud spanner databases ddl update test-db --instance=test-instance \
  --ddl="CREATE TABLE orders (
    order_id    STRING(36) NOT NULL,
    customer_id STRING(36) NOT NULL,
    amount      FLOAT64    NOT NULL,
    status      STRING(20) NOT NULL,
    created_at  TIMESTAMP  NOT NULL OPTIONS (allow_commit_timestamp=true),
  ) PRIMARY KEY (order_id)"
```

**Setup in CI with Docker:**
```yaml
# docker-compose.yml
version: '3.8'
services:
  spanner-emulator:
    image: gcr.io/cloud-spanner-emulator/emulator:latest
    ports:
      - "9010:9010"   # gRPC port (Java client library)
      - "9020:9020"   # HTTP port (REST API and health check)
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9020"]
      interval: 5s
      timeout: 3s
      retries: 10
```

```yaml
# GitHub Actions workflow
steps:
  - name: Start Spanner Emulator
    run: |
      docker run -d --name spanner-emulator \
        -p 9010:9010 -p 9020:9020 \
        gcr.io/cloud-spanner-emulator/emulator:latest
      # Wait for emulator to be ready
      until curl -sf http://localhost:9020; do sleep 1; done

  - name: Set emulator environment variable
    run: echo "SPANNER_EMULATOR_HOST=localhost:9010" >> $GITHUB_ENV

  - name: Create test instance and database
    run: |
      gcloud spanner instances create test-instance \
        --config=emulator-config --nodes=1 --project=test-project
      gcloud spanner databases create test-db \
        --instance=test-instance --project=test-project
      gcloud spanner databases ddl update test-db \
        --instance=test-instance --project=test-project \
        --ddl-file=schema/orders.sql

  - name: Run tests
    run: mvn test
```

The Spanner Java client library reads `SPANNER_EMULATOR_HOST` and automatically routes all calls to the emulator. No code change is needed — the same Java code works against both the emulator and real Spanner.

---

**Q7: What SQL features are specific to Spanner that you use in test validation queries?**

**A:**

**Timestamp functions:**
```sql
-- Records created in the last 24 hours
SELECT order_id, created_at
FROM orders
WHERE created_at > TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 DAY);

-- Records between two timestamps
SELECT order_id, amount
FROM orders
WHERE created_at BETWEEN TIMESTAMP('2024-01-01T00:00:00Z')
                     AND TIMESTAMP('2024-01-31T23:59:59Z');

-- Most recently created record (useful to verify the record your API just wrote)
SELECT order_id, amount, status, created_at
FROM orders
ORDER BY created_at DESC
LIMIT 1;
```

**ARRAY columns and UNNEST:**
```sql
-- ARRAY type stores multiple values in a single column
-- UNNEST flattens the array so you can query individual elements
SELECT o.order_id, tag
FROM orders o, UNNEST(o.tags) AS tag
WHERE tag = 'PRIORITY';
```

**JSON columns (Spanner 2022+):**
```sql
-- Query inside a JSON column using JSON_VALUE
SELECT
    order_id,
    JSON_VALUE(metadata, '$.source') AS order_source,
    JSON_VALUE(metadata, '$.campaign_id') AS campaign_id
FROM orders
WHERE JSON_VALUE(metadata, '$.source') = 'mobile_app';
```

**CTEs (WITH clause) for readable QA validation queries:**
```sql
WITH recent_orders AS (
    SELECT order_id, customer_id, amount
    FROM orders
    WHERE created_at > TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 1 HOUR)
),
high_value AS (
    SELECT customer_id, SUM(amount) AS total
    FROM recent_orders
    GROUP BY customer_id
    HAVING SUM(amount) > 500
)
SELECT c.email, h.total
FROM high_value h
JOIN customers c ON h.customer_id = c.customer_id
ORDER BY h.total DESC;
```

**QA validation queries:**
```sql
-- Count records written by the last test run (within last 10 minutes)
SELECT COUNT(*) AS records_just_written
FROM orders
WHERE created_at > TIMESTAMP_SUB(CURRENT_TIMESTAMP(), INTERVAL 10 MINUTE);

-- Find orphan child records (interleaved table integrity check)
SELECT oi.order_id, oi.item_id
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL;

-- Find records with invalid status (constraint violation check)
SELECT order_id, status
FROM orders
WHERE status NOT IN ('PENDING', 'ACTIVE', 'CANCELLED', 'COMPLETED');

-- Find duplicate primary keys (should return zero rows — Spanner enforces PK uniqueness)
SELECT order_id, COUNT(*) AS cnt
FROM orders
GROUP BY order_id
HAVING COUNT(*) > 1;
```

---

## SECTION 2 — JAVA CLIENT AND TEST CODE

---

**Q8: How do you connect to Spanner from Java tests? Show the complete SpannerTestHelper class.**

**A:**

Maven dependency:
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-spanner</artifactId>
    <version>6.51.0</version>
</dependency>
```

```java
package com.example.spanner;

import com.google.cloud.spanner.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpannerTestHelper {

    private static final String PROJECT_ID  = "test-project";
    private static final String INSTANCE_ID = "test-instance";
    private static final String DATABASE_ID = "test-db";

    private final Spanner       spanner;
    private final DatabaseClient dbClient;

    public SpannerTestHelper() {
        SpannerOptions options = SpannerOptions.newBuilder()
            .setProjectId(PROJECT_ID)
            .build();
        // The library automatically reads SPANNER_EMULATOR_HOST env var.
        // When set, all calls go to the emulator. No code change needed.
        this.spanner  = options.getService();
        this.dbClient = spanner.getDatabaseClient(
            DatabaseId.of(PROJECT_ID, INSTANCE_ID, DATABASE_ID)
        );
    }

    // ── Read a single row by primary key (strong read) ──────────────

    public Struct readOrderById(String orderId) {
        return dbClient
            .singleUse()   // strong read — guaranteed to see latest committed data
            .readRow(
                "orders",
                Key.of(orderId),
                List.of("order_id", "customer_id", "amount", "status", "created_at")
            );
        // Returns null if row not found
    }

    // ── Read multiple rows via SQL query ─────────────────────────────

    public List<Struct> queryOrdersByCustomer(String customerId) {
        List<Struct> results = new ArrayList<>();
        // Always use parameterised queries to prevent injection
        try (ResultSet rs = dbClient.singleUse().executeQuery(
            Statement.newBuilder(
                "SELECT order_id, amount, status, created_at " +
                "FROM orders WHERE customer_id = @customerId " +
                "ORDER BY created_at DESC")
                .bind("customerId").to(customerId)
                .build()
        )) {
            while (rs.next()) {
                results.add(rs.getCurrentRowAsStruct());
            }
        }
        return results;
    }

    // ── Insert test data using Mutation API ──────────────────────────
    // Mutations are batched and committed atomically — preferred for test setup

    public String insertTestOrder(String customerId, double amount) {
        String orderId = UUID.randomUUID().toString();
        dbClient.write(List.of(
            Mutation.newInsertBuilder("orders")
                .set("order_id").to(orderId)
                .set("customer_id").to(customerId)
                .set("amount").to(amount)
                .set("status").to("PENDING")
                .set("created_at").to(Value.COMMIT_TIMESTAMP)  // Spanner fills this in
                .build()
        ));
        return orderId;
    }

    public void insertOrderItem(String orderId, String productSku, int qty, double unitPrice) {
        String itemId = UUID.randomUUID().toString();
        dbClient.write(List.of(
            Mutation.newInsertBuilder("order_items")
                .set("order_id").to(orderId)
                .set("item_id").to(itemId)
                .set("product_sku").to(productSku)
                .set("quantity").to((long) qty)
                .set("unit_price").to(unitPrice)
                .build()
        ));
    }

    // ── Update a row ─────────────────────────────────────────────────

    public void updateOrderStatus(String orderId, String newStatus) {
        dbClient.write(List.of(
            Mutation.newUpdateBuilder("orders")
                .set("order_id").to(orderId)
                .set("status").to(newStatus)
                .build()
        ));
    }

    // ── Delete by primary key ────────────────────────────────────────

    public void deleteOrder(String orderId) {
        dbClient.write(List.of(
            Mutation.delete("orders", Key.of(orderId))
        ));
    }

    // ── Delete by query — clean up all test data from a test run ────

    public void deleteOrdersByCustomer(String customerId) {
        dbClient.readWriteTransaction().run(transaction -> {
            // DML inside a read-write transaction
            long deleted = transaction.executeUpdate(
                Statement.newBuilder("DELETE FROM orders WHERE customer_id = @customerId")
                    .bind("customerId").to(customerId)
                    .build()
            );
            System.out.println("Deleted " + deleted + " test orders for " + customerId);
            return null;
        });
    }

    // ── Query order items for a given order ──────────────────────────

    public List<Struct> queryItemsByOrder(String orderId) {
        List<Struct> results = new ArrayList<>();
        try (ResultSet rs = dbClient.singleUse().executeQuery(
            Statement.newBuilder(
                "SELECT item_id, product_sku, quantity, unit_price " +
                "FROM order_items WHERE order_id = @orderId")
                .bind("orderId").to(orderId)
                .build()
        )) {
            while (rs.next()) results.add(rs.getCurrentRowAsStruct());
        }
        return results;
    }

    // ── Read-write transaction example ───────────────────────────────

    public void transferAmount(String fromId, String toId, double amount) {
        dbClient.readWriteTransaction().run(transaction -> {
            Struct from = transaction.readRow("orders", Key.of(fromId), List.of("amount"));
            Struct to   = transaction.readRow("orders", Key.of(toId),   List.of("amount"));

            double newFrom = from.getDouble("amount") - amount;
            if (newFrom < 0) {
                throw SpannerExceptionFactory.newSpannerException(
                    ErrorCode.FAILED_PRECONDITION, "Insufficient balance");
            }

            transaction.buffer(List.of(
                Mutation.newUpdateBuilder("orders")
                    .set("order_id").to(fromId).set("amount").to(newFrom).build(),
                Mutation.newUpdateBuilder("orders")
                    .set("order_id").to(toId)
                    .set("amount").to(to.getDouble("amount") + amount).build()
            ));
            return null;
        });
    }

    // ── Cleanup ───────────────────────────────────────────────────────

    public void close() {
        if (spanner != null && !spanner.isClosed()) {
            spanner.close();
        }
    }
}
```

---

**Q9: What is the difference between the Mutations API and DML in Spanner?**

**A:**

Both write data to Spanner but have different mechanics and performance characteristics:

**Mutations** — a list of write operations (INSERT, UPDATE, DELETE) accumulated in memory and committed atomically in one round-trip:
```java
// Build a list of mutations
List<Mutation> mutations = new ArrayList<>();
mutations.add(Mutation.newInsertBuilder("orders")
    .set("order_id").to("abc123")
    .set("amount").to(99.99)
    .set("status").to("PENDING")
    .build());
mutations.add(Mutation.newInsertBuilder("order_items")
    .set("order_id").to("abc123")
    .set("item_id").to("item-1")
    .set("quantity").to(2L)
    .build());

// One commit — both mutations applied atomically
dbClient.write(mutations);
```

**DML** — standard SQL `INSERT`/`UPDATE`/`DELETE` executed inside a read-write transaction. Each statement is sent to Spanner and can read back results:
```java
dbClient.readWriteTransaction().run(transaction -> {
    transaction.executeUpdate(
        Statement.of("INSERT INTO orders (order_id, amount, status) " +
                     "VALUES ('abc123', 99.99, 'PENDING')")
    );
    // Can also read within the same transaction
    Struct row = transaction.readRow("orders", Key.of("abc123"), List.of("status"));
    return null;
});
```

| Feature | Mutations | DML |
|---------|-----------|-----|
| Syntax | Java builder API | Standard SQL |
| Round trips | One commit for all | One per statement |
| Can read within txn | Only buffered (not visible until commit) | Yes, can read own writes |
| Performance | Faster for bulk ops | Slightly higher latency |
| Readability | Verbose for complex logic | Familiar SQL |

**QA recommendation**: use Mutations for bulk test data setup (inserting 100 test records) — faster. Use DML for individual operations where SQL readability matters or you need to read within the transaction.

---

**Q10: How do you verify that an API call wrote the correct data to Spanner?**

**A:**

The standard test pattern: call the API via HTTP, extract the entity ID from the response, then query Spanner directly to verify every field:

```java
package com.example.tests;

import com.example.spanner.SpannerTestHelper;
import com.google.cloud.spanner.Struct;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.*;
import static org.testng.Assert.*;

public class OrderPersistenceTest {

    private SpannerTestHelper spanner;
    // Unique marker for cleanup — avoids affecting other test data
    private final String RUN_ID = "TEST-" + UUID.randomUUID().toString().substring(0, 8);

    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = System.getProperty("api.base.url", "http://localhost:8080");
        spanner = new SpannerTestHelper();
    }

    // Pattern 1: POST → verify all fields in Spanner
    @Test
    public void postOrder_writesCorrectDataToSpanner() {
        String customerId = RUN_ID + "-CUST-1";
        double amount = 249.99;
        String type = "PRIORITY";

        // 1. Call the API
        Response response = RestAssured.given()
            .contentType("application/json")
            .body(String.format("{\"customerId\":\"%s\",\"amount\":%.2f,\"type\":\"%s\"}",
                customerId, amount, type))
            .post("/api/orders");

        assertEquals(response.getStatusCode(), 201, "Expected 201 Created");
        String orderId = response.jsonPath().getString("orderId");
        assertNotNull(orderId, "orderId should be present in API response");

        // 2. Verify the data in Spanner — strong read guarantees we see the write
        Struct order = spanner.readOrderById(orderId);
        assertNotNull(order, "Order should exist in Spanner — API write failed to persist");
        assertEquals(order.getString("customer_id"), customerId, "customer_id mismatch");
        assertEquals(order.getDouble("amount"), amount, 0.001, "amount mismatch");
        assertEquals(order.getString("status"), "PENDING", "initial status should be PENDING");
        assertEquals(order.getString("type"), type, "type mismatch");
        assertFalse(order.isNull("created_at"), "created_at should be auto-populated by Spanner");
    }

    // Pattern 2: POST → PATCH → verify state transition
    @Test
    public void confirmOrder_updatesStatusToConfirmedInSpanner() {
        String customerId = RUN_ID + "-CUST-2";
        Response createResp = RestAssured.given()
            .contentType("application/json")
            .body("{\"customerId\":\"" + customerId + "\",\"amount\":99.99}")
            .post("/api/orders");
        String orderId = createResp.jsonPath().getString("orderId");

        // Confirm the order
        Response confirmResp = RestAssured.given()
            .patch("/api/orders/" + orderId + "/confirm");
        assertEquals(confirmResp.getStatusCode(), 200);

        // Verify status updated in Spanner
        Struct order = spanner.readOrderById(orderId);
        assertEquals(order.getString("status"), "CONFIRMED",
            "Status should be CONFIRMED after /confirm call");
        assertFalse(order.isNull("confirmed_at"),
            "confirmed_at timestamp should be set after confirmation");
    }

    // Pattern 3: DELETE → verify record gone from Spanner
    @Test
    public void deleteOrder_removesRecordFromSpanner() {
        String customerId = RUN_ID + "-CUST-3";
        Response createResp = RestAssured.given()
            .contentType("application/json")
            .body("{\"customerId\":\"" + customerId + "\",\"amount\":49.99}")
            .post("/api/orders");
        String orderId = createResp.jsonPath().getString("orderId");

        // Delete via API
        Response deleteResp = RestAssured.given().delete("/api/orders/" + orderId);
        assertEquals(deleteResp.getStatusCode(), 204);

        // Verify record no longer exists in Spanner
        Struct order = spanner.readOrderById(orderId);
        assertNull(order, "Order should not exist in Spanner after DELETE");
    }

    @AfterClass
    public void tearDown() {
        // Delete all test data created by this test class
        for (int i = 1; i <= 3; i++) {
            spanner.deleteOrdersByCustomer(RUN_ID + "-CUST-" + i);
        }
        spanner.close();
    }
}
```

---

**Q11: What are stale reads vs strong reads in Spanner?**

**A:**

```java
// STRONG READ (default) — reads the most up-to-date committed data
// Spanner coordinates across replicas — slightly slower
// ALWAYS use strong reads in tests
ReadContext strongCtx = dbClient.singleUse();   // default = strong
Struct row = strongCtx.readRow("orders", Key.of(orderId), columns);

// STALE READ — reads data as of a past timestamp
// Does NOT need cross-region coordination — faster
// Used in production for analytics/reporting where slight lag is acceptable
// NEVER use in tests — may return data that doesn't include recent test writes

// Exact staleness — data as of exactly 10 seconds ago
ReadContext staleCtx = dbClient.singleUse(
    TimestampBound.ofExactStaleness(10, TimeUnit.SECONDS));

// Max staleness — data no older than 15 seconds (Spanner picks the freshest available)
ReadContext maxStaleCtx = dbClient.singleUse(
    TimestampBound.ofMaxStaleness(15, TimeUnit.SECONDS));

// Read at a specific past timestamp
ReadContext atTimeCtx = dbClient.singleUse(
    TimestampBound.ofReadTimestamp(Timestamp.ofTimeSecondsAndNanos(epochSeconds, 0)));
```

**Why stale reads exist:** In a globally distributed system, coordinating a read across replicas in multiple regions takes time (cross-region network latency). If your analytics dashboard is querying historical data, exact consistency to the millisecond does not matter. Stale reads skip the coordination step and return immediately, using the local replica's cached data.

**Why you must not use stale reads in tests:** If your test inserts a row then immediately reads with a 10-second stale read, the read returns data as of 10 seconds ago — before your insert. The row will appear not to exist, causing a false test failure. Always use `dbClient.singleUse()` with no argument (defaults to strong read) in test assertions.

---

**Q12: What is session pool management in Spanner tests and why does it matter?**

**A:**

The Spanner Java client maintains a pool of sessions internally. Sessions are persistent connections to Spanner used to execute queries and transactions. The pool has a limited number of sessions (default: minimum 100, maximum 400 per `DatabaseClient`).

In tests, the risk is **session leaks** — acquiring a session and never returning it, causing the pool to exhaust. Symptoms: tests hang waiting for a session, or you get `RESOURCE_EXHAUSTED: Too many active sessions`.

```java
// PROBLEM: ResultSet not closed → session not returned to pool
public List<Struct> badQueryMethod(String customerId) {
    List<Struct> results = new ArrayList<>();
    ResultSet rs = dbClient.singleUse().executeQuery(...);  // acquires session
    while (rs.next()) results.add(rs.getCurrentRowAsStruct());
    // rs never closed → session never returned to pool!
    return results;
}

// FIX: always use try-with-resources for ResultSet
public List<Struct> goodQueryMethod(String customerId) {
    List<Struct> results = new ArrayList<>();
    try (ResultSet rs = dbClient.singleUse().executeQuery(...)) {  // auto-closed
        while (rs.next()) results.add(rs.getCurrentRowAsStruct());
    }
    return results;
}

// Also: close the Spanner client in @AfterClass/@AfterSuite
@AfterClass
public void tearDown() {
    spannerHelper.close();  // closes all sessions in the pool
}

// Or use try-with-resources for the entire SpannerTestHelper
try (SpannerTestHelper helper = new SpannerTestHelper()) {
    // ... tests
}  // close() called automatically
```

Configure session pool for test usage (fewer sessions needed):
```java
SessionPoolOptions poolOptions = SessionPoolOptions.newBuilder()
    .setMinSessions(5)    // don't pre-create 100 sessions for small test suite
    .setMaxSessions(25)   // cap to avoid exhaustion
    .build();

SpannerOptions options = SpannerOptions.newBuilder()
    .setProjectId(PROJECT_ID)
    .setSessionPoolOption(poolOptions)
    .build();
```

---

## SECTION 3 — COMMON ERRORS AND INTERVIEW Q&A

---

**Q13: What are the most common Spanner errors in testing and how do you fix them?**

**A:**

**Error 1: RESOURCE_EXHAUSTED: Quota exceeded**
```
com.google.cloud.spanner.SpannerException: RESOURCE_EXHAUSTED: Quota exceeded for quota metric...
```
Cause: Session pool exhausted (too many open sessions), or emulator memory limit hit.
Fix:
```java
// Always close ResultSet and Spanner client
try (ResultSet rs = dbClient.singleUse().executeQuery(...)) { ... }
// @AfterClass: spannerHelper.close()

// Restart emulator if exhausted
// docker restart spanner-emulator
```

**Error 2: NOT_FOUND: Instance or database does not exist**
```
com.google.cloud.spanner.SpannerException: NOT_FOUND: Instance test-instance not found
```
Cause: Emulator started but instance/database not yet created.
Fix: Run the `gcloud spanner instances create` and `gcloud spanner databases create` commands before running tests. In CI, add a setup step before the test step.

**Error 3: INVALID_ARGUMENT: Value has wrong type**
```
INVALID_ARGUMENT: Expected type INT64 but got STRING at field quantity
```
Cause: Spanner types are strict. An INT64 column rejects a Java `int` — must use `long`.
Fix:
```java
// Spanner INT64 requires Java long
.set("quantity").to((long) quantity)   // cast int to long

// Spanner FLOAT64 requires Java double
.set("amount").to(amount)   // double is fine

// TIMESTAMP column using commit timestamp
.set("created_at").to(Value.COMMIT_TIMESTAMP)   // not a manual timestamp
```

**Error 4: ALREADY_EXISTS: Row already exists for key**
```
ALREADY_EXISTS: Row with key (abc123) already exists in table orders
```
Cause: Inserting a row with a primary key that already exists. Use `newInsertOrUpdateBuilder` or generate a new UUID.
Fix:
```java
// Option 1: always generate a new UUID
String orderId = UUID.randomUUID().toString();  // guaranteed unique

// Option 2: use insert-or-update (upsert)
Mutation.newInsertOrUpdateBuilder("orders")
    .set("order_id").to(orderId)
    ...
```

**Error 5: Data not found after insert (stale read)**
```
java.lang.AssertionError: Expected order to exist in Spanner but got null
```
Cause: Using a stale read context instead of strong read.
Fix: Always use `dbClient.singleUse()` with no arguments (strong read) in test assertions.

**Error 6: FAILED_PRECONDITION: Interleaved table parent key not found**
```
FAILED_PRECONDITION: Parent key does not exist for row being inserted into order_items
```
Cause: Inserting a child row in an interleaved table before its parent exists.
Fix: Always insert the parent row first, then child rows.

---

**Q14: What is Google Cloud Spanner and when would you use it? (Interview answer)**

**A:** Google Cloud Spanner is a fully managed, globally distributed relational database that provides horizontal scalability, ACID transactions, and strong consistency across multiple geographic regions. You choose Spanner when your application needs to serve users globally, requires strict consistency — not eventual consistency — and has outgrown the vertical scaling limits of a traditional relational database. Examples include a global payments platform where every region must see the same account balance, or a multi-region SaaS product that needs 99.999% availability. I have worked with Spanner in a GCP-based platform, writing Java integration tests that connect to the Spanner emulator in CI and verify that API writes persist the correct data with the correct field values.

---

**Q15: How does Spanner differ from MySQL for QA purposes?**

**A:** Five key differences matter for test code. First, no auto-increment — I must generate UUIDs in test data setup scripts, not rely on the database to generate IDs. Second, no ENUM type — status values use STRING with CHECK constraints, and I write specific tests to verify invalid status values are rejected. Third, interleaved tables — I explicitly test cascade delete (deleting a parent removes all children) and parent existence enforcement (inserting a child for a non-existent parent throws a SpannerException). Fourth, always use strong reads in test assertions — stale reads intentionally ignore recent writes and cause false test failures. Fifth, the Spanner emulator — identical to real Spanner for functional tests, set up by the `SPANNER_EMULATOR_HOST` environment variable, no code change needed.

---

**Q16: How do you test with the Spanner emulator in a CI pipeline?**

**A:** I run the emulator as a Docker container — `gcr.io/cloud-spanner-emulator/emulator:latest` — started before the test step. I set `SPANNER_EMULATOR_HOST=localhost:9010` in the CI environment variables, which the Spanner Java client library reads automatically. Then I run the `gcloud spanner` CLI to create the test instance, database, and apply the DDL schema. The tests then run pointing at the emulator. This approach works identically in GitHub Actions (using `docker run` in a step) and Jenkins (using Docker Compose). The emulator supports the full Spanner API, so any functional test that works on the emulator behaves the same on real Spanner — except for performance characteristics.

---

**Q17: What does "strong consistency" mean and why does it simplify testing?**

**A:** Strong consistency means that once a Spanner write transaction commits, every subsequent read — from any node, in any geographic region — is guaranteed to see that write. There is no propagation delay. After `dbClient.write(mutations)` returns, the next `dbClient.singleUse().readRow(...)` will always find the written data. I do not need `Thread.sleep()`, retry loops, or polling logic waiting for data to appear. By contrast, testing against an eventually consistent database like DynamoDB in async mode requires polling: "insert, wait 500ms, read, if null wait another 500ms, retry up to 5 times." That logic makes tests slower, more complex, and still not deterministic. Strong consistency makes Spanner test assertions as simple as "write then immediately read and assert" — which is the correct way to write tests.

---

**Q18: What is the QA pattern for verifying that an API call persisted data correctly to Spanner?**

**A:** The pattern is three steps: (1) Call the API endpoint using RestAssured — for example, POST `/api/orders` with a JSON payload; (2) Extract the entity ID from the API response body — for example, `response.jsonPath().getString("orderId")`; (3) Query Spanner directly using `SpannerTestHelper.readOrderById(orderId)` and assert every field matches what was sent. The assertions cover: the correct customer ID, amount, type, that status is set to the expected initial value, and that auto-populated fields like `created_at` are not null. The test teardown calls `spanner.deleteOrdersByCustomer(testCustomerId)` to clean up. I use a unique test-run ID prefix (e.g. `TEST-f47ac10b-CUST-1`) in customer IDs so cleanup can target exactly the test data without risking deleting production or other test data.

---

**Q19: What is the difference between Spanner and Cloud SQL?**

**A:** Both are Google Cloud managed databases but they solve different problems. Cloud SQL is a managed version of MySQL, PostgreSQL, or SQL Server — single regional instance, familiar features including sequences and ENUMs, standard SQL. It scales vertically (larger machine) and has a read replica for read scaling. Cloud SQL is the right choice for most traditional web applications that need a managed database without the operational overhead. Cloud Spanner is a custom database engine built by Google — globally distributed across multiple regions, horizontally scalable by adding nodes, no sequential key sequences, requires UUID keys, and provides strong consistency across regions. Spanner is more expensive (roughly 3-10x per GB of storage) and requires more adaptation of application code. Choose Cloud SQL when your traffic fits in a single region and your existing relational data model is straightforward. Choose Spanner when you need global reach, horizontal write scalability beyond what a single large VM can provide, and strong consistency guarantees across those regions.

---

**Q20: What are interleaved tables and what do you specifically test about them?**

**A:** Interleaved tables are a Spanner-specific physical co-location mechanism — child rows are stored adjacent to their parent row on disk, making parent+children reads very fast because they are on the same storage shard. They are defined with `INTERLEAVE IN PARENT tableName` in the CREATE TABLE DDL. For QA, two behaviours require explicit testing. First, cascade delete: if the schema uses `ON DELETE CASCADE`, deleting the parent order should automatically delete all its order_items. I create a test that creates a parent with multiple children, deletes the parent, then asserts `queryItemsByOrder(orderId).size() == 0`. Second, parent existence enforcement: inserting a child row for a non-existent parent should be rejected. I use `@Test(expectedExceptions = SpannerException.class)` to assert that the insert throws an exception. These tests verify that the schema enforces the relationship correctly, which matters because Spanner interleaving is not the same as a traditional MySQL foreign key constraint.

---

**Q21: Common Spanner session management pitfall — explain and fix it.**

**A:** The most common pitfall is a **session leak** — opening a `ResultSet` or starting a transaction and never closing it. The Spanner client has a session pool with a default maximum of 400 sessions per `DatabaseClient`. Each unclosed `ResultSet` holds a session. After enough test methods run without cleanup, the pool exhausts and tests hang indefinitely waiting for a free session.

```java
// WRONG — ResultSet never closed, session leaked
ResultSet rs = dbClient.singleUse().executeQuery(statement);
while (rs.next()) { results.add(rs.getCurrentRowAsStruct()); }
// rs and its session are never returned to pool

// CORRECT — try-with-resources closes ResultSet automatically
try (ResultSet rs = dbClient.singleUse().executeQuery(statement)) {
    while (rs.next()) { results.add(rs.getCurrentRowAsStruct()); }
}  // rs.close() called here, session returned to pool

// Also close the Spanner client in @AfterClass
@AfterClass
public void tearDown() {
    if (spannerHelper != null) {
        spannerHelper.close();  // closes all pooled sessions
    }
}
```

Detection: if tests start hanging after the first few hundred test methods, suspect session exhaustion. Add logging: `SpannerOptions.newBuilder().setNumChannels(4)` and monitor via Cloud Monitoring (for real Spanner) or emulator logs.

---

*Guide covers 21 questions: Spanner fundamentals, comparison with MySQL/PostgreSQL, strong vs eventual consistency, interleaved tables, UUID keys, emulator setup with gcloud and Docker, Spanner-specific SQL, complete Java SpannerTestHelper class, Mutations vs DML, full API-to-Spanner verification test pattern, stale vs strong reads, session pool management, common errors with fixes, and 8 interview Q&A questions.*
