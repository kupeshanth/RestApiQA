# ETL & Data Pipeline Testing — Complete Guide | Data Quality + Migration + Validation

> Senior QA Interview Preparation — Data-Level Testing Mastery

---

## SECTION 1 — What is ETL and Why QA Tests It

### ETL = Extract, Transform, Load

ETL is the process of moving data from one or more source systems into a target system, typically a data warehouse, analytics database, or another application database. Every enterprise system that aggregates, reports on, or migrates data relies on an ETL pipeline.

```
SOURCE                    TRANSFORM                   TARGET
────────────────          ──────────────────────      ─────────────────────
API / REST endpoint  →    Clean + reshape data    →   Data Warehouse
Relational DB        →    Aggregate + enrich      →   Analytics DB
CSV / Excel file     →    Validate + filter       →   Another DB / file
Kafka stream         →    Deduplicate + join      →   Reporting system
```

### The Three Stages in Detail

**Extract — Pull data from the source**
- Sources can be: REST APIs, relational databases (MySQL, PostgreSQL, Oracle), flat files (CSV, JSON, XML), message queues (Kafka, RabbitMQ), cloud storage (S3, GCS), legacy systems
- Extract can be full (everything) or incremental (only changed records since last run)
- QA concern: are ALL expected records extracted? Are any records silently skipped?

**Transform — Clean, reshape, aggregate, enrich**
- Normalise dates to a common format (e.g. `2024-01-15T10:30:00Z`)
- Standardise field values (e.g. `"M"`, `"Male"`, `"male"` → all become `"MALE"`)
- Split columns (e.g. `full_name` → `first_name`, `last_name`)
- Join data from multiple sources
- Aggregate (e.g. sum daily transactions into monthly totals)
- Enrich with reference data (e.g. add country name from country code)
- Filter out records that fail validation rules
- QA concern: is every transformation rule producing the correct output? Are edge cases handled?

**Load — Write to the target system**
- Load can be: full replace, incremental append, upsert (insert or update)
- QA concern: did all records land in target? Any truncation? Are data types correct? Are constraints respected?

### QA's Role in ETL

QA's job is to verify **data integrity at every stage** of the pipeline:

| Stage | QA Verifies |
|-------|-------------|
| Post-Extract | Count of records extracted matches source; no data left behind |
| Post-Transform | Each transformation rule applied correctly; edge cases handled |
| Post-Load | Target count matches expectation; field values correct; no corruption |
| End-to-End | Source record can be traced through to target; no data loss overall |
| Error Handling | Rejected records are logged; errors are visible; pipeline does not silently discard data |

---

## SECTION 2 — Types of Data Testing

### 1. Data Completeness
**Definition:** All expected records and fields are present in the target. No rows are missing, no columns are empty when they should have a value.

**What breaks it:** Incomplete extract, filtering bug, transformation that drops records on exception, network timeout mid-load.

**How to test:**
- Row count in source = row count in target (or expected difference with documented reason)
- Check for unexpected NULL values in mandatory fields
- Verify that all source files/tables were processed

### 2. Data Accuracy
**Definition:** The values in the target are correct — they reflect the real-world meaning of the source data.

**What breaks it:** Wrong transformation formula, rounding error, timezone conversion bug, type casting issue (e.g. `"1.5"` string → `1` integer, losing decimal).

**How to test:**
- Field-by-field comparison between source and target for a sample of records
- Verify calculated/derived fields using independent calculation
- Test boundary values (max, min, zero, negative)

### 3. Data Consistency
**Definition:** The same data referenced in different systems or tables agrees with each other.

**What breaks it:** Two pipelines updating the same entity with conflicting logic, race conditions, asynchronous writes that settle at different times.

**How to test:**
- Cross-system queries: does `customer.total_orders` in the CRM match `COUNT(orders)` in the warehouse?
- Compare the same entity across related tables after a join

### 4. Data Timeliness
**Definition:** Data arrives in the target within the expected SLA window.

**What breaks it:** Slow extract from source, transform bottleneck, load latency, pipeline queue backup.

**How to test:**
- Record the timestamp when an event is created in source
- Record the timestamp when that event appears in target
- Assert `(target_timestamp - source_timestamp) < SLA_threshold`

### 5. Data Integrity
**Definition:** Referential integrity is maintained — foreign keys point to existing parent records; no orphaned child records.

**What breaks it:** Load order issues (child loaded before parent), cascading deletes not applied, partial migration.

**How to test:**
- Run referential integrity queries after load
- Verify all foreign keys have matching primary keys in parent table

### 6. Data Uniqueness
**Definition:** No duplicate records exist where uniqueness is required (e.g. no two rows with the same `order_id`).

**What breaks it:** Pipeline run multiple times without deduplication, upsert logic failing, duplicate events in stream.

**How to test:**
- `GROUP BY id HAVING COUNT(*) > 1` — any result means duplicates exist

### 7. Data Validity
**Definition:** Values are within the expected domain — correct format, within allowed range, from allowed set.

**What breaks it:** Missing validation in transform, source data was dirty, format change in source system.

**How to test:**
- Date fields contain valid dates in expected format
- Enum fields contain only allowed values
- Numeric fields are within expected range (no negative ages, no prices of $0)
- Email fields match email format regex
- Required fields are not NULL

---

## SECTION 3 — ETL Testing Approach Step by Step

### Step 1: Understand the Data Flow
Before writing a single test, map the pipeline:
- **Source:** What system? What table/endpoint/file? What is the schema?
- **Transform rules:** List every rule (normalise date, calculate field, join with reference data, filter condition)
- **Target:** What is the target schema? What are the constraints?
- **Volume:** How many records are expected?
- **Frequency:** Is this a one-time migration or recurring pipeline?

Deliverable: a data flow diagram and a transformation rule document.

### Step 2: Design Test Cases for Each Transformation Rule
For every transformation rule, design at least:
- A happy-path test (normal input → expected output)
- A boundary test (min value, max value)
- An edge case test (NULL input, empty string, special characters, Unicode)
- A negative test (invalid input → what should happen? Error log? Default value? Rejection?)

### Step 3: Set Up Test Data in Source
- Create or seed records in the source system that cover all your test cases
- Ensure you have records that will exercise every branch of transform logic
- Include records that SHOULD be rejected (to test error handling)
- Document exactly what you put in source so you know what to expect in target

### Step 4: Run the Pipeline
- Run in a controlled test environment (not production)
- Record the run timestamp for later verification
- Monitor logs during run for any errors or warnings

### Step 5: Validate Target Against Expected Output
- Query the target for records you created in source
- Compare field by field against your expected values
- Use SQL queries or a data comparison tool

### Step 6: Verify Counts Match
```
Source record count = Target record count
OR
Source count - Rejected count = Target count
```
Document any expected differences with a reason (e.g. records filtered out by business rule).

### Step 7: Verify Rejected/Error Records Are Logged Correctly
- Records that fail validation should NOT be silently dropped
- They should appear in an error log or dead letter table
- Verify the error log contains the expected records with the correct reason code

### Step 8: Verify Edge Cases
- NULL values in source → correct handling in target (NULL preserved, default applied, or record rejected)
- Empty string → treated as NULL or kept as empty?
- Max length string → not truncated in target
- Special characters (apostrophes, quotes, Unicode, emoji) → not corrupted
- Leading/trailing whitespace → trimmed or preserved as designed
- Numeric overflow → error logged, not silently truncated

---

## SECTION 4 — SQL for ETL Validation

### Count Check: Source vs Target
```sql
-- Source count
SELECT COUNT(*) AS source_count
FROM source_schema.orders
WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01';

-- Target count
SELECT COUNT(*) AS target_count
FROM target_schema.orders_warehouse
WHERE source_created_at >= '2024-01-01' AND source_created_at < '2024-02-01';

-- They should match (or differ by exactly the number of rejected records)
-- What it finds: missing records or duplicate records introduced by the pipeline
```

### Row-by-Row Comparison Using FULL OUTER JOIN
```sql
-- Find records in source that are NOT in target (missing records)
SELECT s.order_id, s.customer_id, s.amount, 'MISSING IN TARGET' AS status
FROM source_schema.orders s
LEFT JOIN target_schema.orders_warehouse t ON s.order_id = t.order_id
WHERE t.order_id IS NULL;

-- Find records in target that are NOT in source (phantom records / duplicates from wrong source)
SELECT t.order_id, t.customer_id, t.amount, 'UNEXPECTED IN TARGET' AS status
FROM target_schema.orders_warehouse t
LEFT JOIN source_schema.orders s ON t.order_id = s.order_id
WHERE s.order_id IS NULL;

-- What it finds: data loss (records that vanished) and data phantom (records that appeared from nowhere)
```

### Field-by-Field Accuracy Comparison
```sql
-- Compare every field for matching rows — any mismatch indicates a transform bug
SELECT
    s.order_id,
    s.customer_id AS src_customer_id,
    t.customer_id AS tgt_customer_id,
    s.amount       AS src_amount,
    t.amount       AS tgt_amount,
    s.status       AS src_status,
    t.status       AS tgt_status,
    CASE WHEN s.customer_id != t.customer_id THEN 'MISMATCH: customer_id' END AS error_customer,
    CASE WHEN s.amount != t.amount           THEN 'MISMATCH: amount'      END AS error_amount,
    CASE WHEN s.status != t.status           THEN 'MISMATCH: status'      END AS error_status
FROM source_schema.orders s
JOIN target_schema.orders_warehouse t ON s.order_id = t.order_id
WHERE s.customer_id != t.customer_id
   OR s.amount != t.amount
   OR s.status != t.status;

-- What it finds: incorrect transformation applied to specific fields
```

### Null Check After Transformation
```sql
-- Mandatory fields should not be NULL in target
SELECT order_id, customer_id, amount, status
FROM target_schema.orders_warehouse
WHERE customer_id IS NULL
   OR amount IS NULL
   OR status IS NULL
   OR order_date IS NULL;

-- What it finds: fields that should always have a value but came through as NULL
-- Could indicate: transform bug, type cast failure, field mapping error
```

### Duplicate Detection
```sql
-- Find rows where the unique key appears more than once
SELECT order_id, COUNT(*) AS duplicate_count
FROM target_schema.orders_warehouse
GROUP BY order_id
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

-- To see the actual duplicate rows:
SELECT *
FROM target_schema.orders_warehouse
WHERE order_id IN (
    SELECT order_id
    FROM target_schema.orders_warehouse
    GROUP BY order_id
    HAVING COUNT(*) > 1
)
ORDER BY order_id;

-- What it finds: pipeline ran twice without deduplication, or upsert failed and inserted instead
```

### Referential Integrity Check
```sql
-- Find orphaned order_items with no matching parent order (foreign key violation)
SELECT oi.order_item_id, oi.order_id
FROM target_schema.order_items oi
LEFT JOIN target_schema.orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL;

-- Find customers referenced by orders that don't exist in customer table
SELECT DISTINCT o.customer_id
FROM target_schema.orders o
LEFT JOIN target_schema.customers c ON o.customer_id = c.customer_id
WHERE c.customer_id IS NULL;

-- What it finds: load order bug (child loaded before parent), or parent records excluded by filter
```

### Date Range Validation
```sql
-- All dates should be within expected valid range
SELECT order_id, order_date
FROM target_schema.orders_warehouse
WHERE order_date < '2000-01-01'          -- suspiciously old
   OR order_date > CURRENT_DATE + 1     -- future date (suspicious, unless by design)
   OR order_date IS NULL;               -- missing date

-- Timezone sanity check: all timestamps should be in UTC
SELECT order_id, order_timestamp
FROM target_schema.orders_warehouse
WHERE EXTRACT(TIMEZONE_HOUR FROM order_timestamp) != 0;  -- not UTC

-- What it finds: timezone conversion bugs, epoch (1970) dates from failed conversions, future dates
```

### Aggregation Validation (Pre vs Post)
```sql
-- Pre-load: calculate expected aggregate from source
SELECT
    DATE_TRUNC('month', order_date) AS month,
    SUM(amount) AS total_revenue,
    COUNT(*) AS order_count,
    AVG(amount) AS avg_order_value
FROM source_schema.orders
WHERE order_date >= '2024-01-01' AND order_date < '2024-02-01'
GROUP BY month;

-- Post-load: same calculation from target (should match exactly)
SELECT
    DATE_TRUNC('month', source_order_date) AS month,
    SUM(amount) AS total_revenue,
    COUNT(*) AS order_count,
    AVG(amount) AS avg_order_value
FROM target_schema.orders_warehouse
WHERE source_order_date >= '2024-01-01' AND source_order_date < '2024-02-01'
GROUP BY month;

-- What it finds: rounding errors in amount fields, missing records affecting aggregates,
-- duplicate records inflating sums, wrong date field used in partition
```

---

## SECTION 5 — Data Migration Testing

Data migration is a specific type of ETL — a one-time (or phased) move of data from a legacy system to a new system.

### Pre-Migration: Baseline Snapshot
Before touching anything, capture the state of the source:
```sql
-- Record counts per table
SELECT 'customers' AS table_name, COUNT(*) AS record_count FROM legacy.customers
UNION ALL
SELECT 'orders',   COUNT(*) FROM legacy.orders
UNION ALL
SELECT 'products', COUNT(*) FROM legacy.products;

-- Checksums for data integrity comparison
SELECT
    MD5(STRING_AGG(CAST(order_id AS TEXT) || amount::TEXT ORDER BY order_id)) AS checksum
FROM legacy.orders;

-- Sample of representative records (save these for post-migration comparison)
SELECT * FROM legacy.customers ORDER BY RANDOM() LIMIT 100;
```

Save all baseline counts and samples. These are your reference point.

### During Migration: Monitor Progress
- Track records migrated vs total records expected (progress %)
- Monitor error rates — if error rate spikes, stop and investigate
- Watch for long-running transactions that may cause locks
- Monitor target database growth (disk space, memory)

### Post-Migration: Verify All Records Migrated
```sql
-- Count verification
SELECT
    (SELECT COUNT(*) FROM legacy.orders) AS source_count,
    (SELECT COUNT(*) FROM new_system.orders) AS target_count,
    (SELECT COUNT(*) FROM legacy.orders) - (SELECT COUNT(*) FROM new_system.orders) AS difference;

-- Sample record comparison (check your 100 baseline records)
SELECT
    n.order_id,
    l.customer_id = n.customer_id AS customer_match,
    l.amount = n.amount AS amount_match,
    l.status = n.status AS status_match
FROM legacy.orders l
JOIN new_system.orders n ON l.legacy_order_id = n.source_id
ORDER BY n.order_id;

-- Relationship verification
SELECT COUNT(*) AS orphaned_order_items
FROM new_system.order_items oi
LEFT JOIN new_system.orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL;
```

### Reconciliation Report
After migration, produce a formal reconciliation report covering:

| Category | Count | Notes |
|----------|-------|-------|
| Source records | 1,250,000 | Total in legacy system |
| Successfully migrated | 1,247,835 | Landed in target |
| Excluded by business rule | 2,100 | Deleted/archived records not migrated |
| Failed with error | 65 | See error log — mostly encoding issues |
| Net difference explained | 0 | 1,247,835 + 2,100 + 65 = 1,250,000 ✓ |

Every record must be accounted for. "We lost some records" is never acceptable.

### Rollback Testing
Before performing the production migration, verify that rollback is possible:
- Can the migration be reversed? (Restore from backup? Replay events in reverse?)
- Test the rollback procedure in staging
- Measure rollback time — is it within the maintenance window?
- Verify data state after rollback is identical to pre-migration baseline (use checksums)

---

## SECTION 6 — Data Pipeline / Streaming Testing

Modern data pipelines are often event-driven, using message queues like Apache Kafka.

### Kafka/Event-Driven Pipeline Testing Concepts
In a streaming pipeline:
- **Producer** publishes events (e.g. `order_created`) to a Kafka topic
- **Consumer** reads events and writes to target (database, another service)
- QA tests the consumer's behaviour: does it correctly process each type of event?

### Testing Message Ordering
- Kafka guarantees ordering within a partition
- Test: produce 10 events in known order; verify consumer processes them in the same order
- Test failure scenario: what happens if consumer processes event 5 before event 3?

### Testing Deduplication
- At-least-once delivery means a message CAN be delivered more than once
- Consumer must be idempotent (processing the same message twice = same result as processing once)
- Test: publish the same message twice; verify only one record in target
- Common pattern: use `message_id` as upsert key

### Schema Validation
- Use Avro or JSON Schema to define the expected shape of each event
- Test: publish a malformed event (missing required field); verify it goes to dead letter queue
- Test: publish event with extra unknown field; verify consumer handles it gracefully

### Latency Testing (Data SLA)
```
Event created in source → published to Kafka → consumed → written to target

SLA example: data must appear in target within 5 seconds of creation in source
```
- Test: create an order; record timestamp T1; poll target until order appears; record T2
- Assert T2 - T1 < 5 seconds
- Run latency tests under load (not just single event, but 1,000 events/second burst)

### Testing Pipeline Failure Recovery
- Stop the consumer; publish 100 events; restart the consumer
- Verify: all 100 events processed, none lost, none duplicated
- This works because Kafka retains events until the consumer acknowledges them (offset management)

### Dead Letter Queue (DLQ) Testing
- The DLQ is where events go when the consumer cannot process them after N retries
- Test: publish a malformed event that will always fail processing
- Verify: after N retries, event lands in DLQ
- Verify: DLQ event contains the original message plus error information
- Verify: the main pipeline continues processing other events (DLQ event does not block the pipeline)

---

## SECTION 7 — Data Quality Dimensions

### Completeness
| | |
|---|---|
| **Definition** | All expected data is present — no missing rows, no missing mandatory field values |
| **What breaks it** | Partial extract, filtering bug, network interruption during load, silent exception in transform |
| **How to test** | Row count assertion; NULL check on mandatory fields; compare to source baseline |
| **SQL to detect** | `SELECT COUNT(*) FROM target` vs source count; `WHERE mandatory_field IS NULL` |

### Accuracy
| | |
|---|---|
| **Definition** | Values in target correctly represent the real-world data from source |
| **What breaks it** | Wrong transform formula, incorrect type conversion, rounding error, timezone bug |
| **How to test** | Field-by-field comparison on sample; verify calculated fields with independent formula |
| **SQL to detect** | `JOIN source ON id WHERE source.field != target.field` |

### Consistency
| | |
|---|---|
| **Definition** | The same data referenced in multiple places agrees |
| **What breaks it** | Two systems updated by separate processes with different logic; asynchronous propagation |
| **How to test** | Cross-system queries; compare aggregates across related tables |
| **SQL to detect** | `SELECT c.order_count, COUNT(o.id) FROM customers c JOIN orders o ON c.id = o.customer_id WHERE c.order_count != actual_count` |

### Timeliness
| | |
|---|---|
| **Definition** | Data arrives in target within the agreed SLA window |
| **What breaks it** | Slow pipeline, queue backup, transform bottleneck, network latency |
| **How to test** | Create event; measure time until it appears in target; assert < SLA threshold |
| **SQL to detect** | `WHERE EXTRACT(EPOCH FROM (target_received_at - source_created_at)) > SLA_seconds` |

### Validity
| | |
|---|---|
| **Definition** | Values conform to the expected domain — correct format, allowed range, valid set |
| **What breaks it** | Insufficient validation in transform, dirty source data, format change in source |
| **How to test** | Check date formats, enum values, numeric ranges, regex patterns |
| **SQL to detect** | `WHERE status NOT IN ('ACTIVE','INACTIVE','PENDING')` or `WHERE age < 0 OR age > 150` |

### Uniqueness
| | |
|---|---|
| **Definition** | No duplicate records where uniqueness is required |
| **What breaks it** | Pipeline run twice, failed upsert (inserted instead of updated), duplicate events consumed twice |
| **How to test** | GROUP BY primary key; assert no group has COUNT > 1 |
| **SQL to detect** | `GROUP BY id HAVING COUNT(*) > 1` |

---

## SECTION 8 — Test Data Management

### Generating Test Data
**Faker libraries** — generate realistic but fake data:
```python
# Python example using Faker
from faker import Faker
import random

fake = Faker()

def generate_customer():
    return {
        "customer_id": fake.uuid4(),
        "first_name":  fake.first_name(),
        "last_name":   fake.last_name(),
        "email":       fake.email(),
        "dob":         fake.date_of_birth(minimum_age=18, maximum_age=80).isoformat(),
        "country":     fake.country_code(),
        "created_at":  fake.date_time_this_year().isoformat()
    }

# Generate 1000 test customers
customers = [generate_customer() for _ in range(1000)]
```

**Data factories** — create related objects with consistent relationships:
```python
class OrderFactory:
    @staticmethod
    def create(customer_id, item_count=3):
        items = [ItemFactory.create() for _ in range(item_count)]
        return {
            "order_id":    fake.uuid4(),
            "customer_id": customer_id,
            "items":       items,
            "total":       sum(i["price"] for i in items),
            "status":      "PENDING"
        }
```

### Masking Production Data for Testing (PII Protection)
Never use real production data in test environments. Mask it:
- **Pseudonymisation:** Replace names/emails with fake equivalents, keep structure
- **Tokenisation:** Replace PII with a non-reversible token
- **Shuffling:** Shuffle values within a column (e.g. mix up real names randomly)
- **Redaction:** Replace with fixed placeholder (e.g. `"REDACTED@test.com"`)

```sql
-- Example: mask customer PII before copying to test environment
INSERT INTO test_env.customers
SELECT
    customer_id,                                        -- keep ID (needed for FK relationships)
    'User_' || customer_id AS first_name,               -- pseudonymise
    'Test'                 AS last_name,
    'user_' || customer_id || '@test.invalid' AS email, -- fake but unique email
    '1990-01-01'           AS dob,                      -- fixed placeholder date
    country                                             -- keep country (not PII)
FROM prod_env.customers;
```

### Test Data Refresh Strategies
- **On-demand refresh:** Wipe and re-seed before each test run
- **Snapshot restore:** Restore a known-good database snapshot before each test suite
- **Incremental seed:** Keep stable reference data; only refresh transactional data
- **Environment-per-test:** Spin up a fresh database per test (expensive but perfect isolation)

### Isolating Test Data from Production Data
- Never run data tests against production databases
- Use environment-specific connection strings; inject via environment variables
- Add a test-data identifier prefix/suffix to all test records: `order_id: "TEST-" + uuid`
- Use database schemas or separate databases per environment
- Add teardown: delete all records with the test prefix after the test run

---

## SECTION 9 — Tools for Data Testing

### Great Expectations (Python)
**What it is:** An open-source Python framework for defining, documenting, and validating data quality rules — called "expectations." Instead of writing ad-hoc SQL checks, you define expectations in code, run them against your data, and get a visual report.

```python
import great_expectations as gx

context = gx.get_context()
datasource = context.sources.add_pandas("my_source")
asset = datasource.add_dataframe_asset("orders")

# Define expectations
suite = context.add_expectation_suite("orders_suite")
validator = context.get_validator(batch_request=..., expectation_suite_name="orders_suite")

validator.expect_column_values_to_not_be_null("order_id")
validator.expect_column_values_to_be_unique("order_id")
validator.expect_column_values_to_be_between("amount", min_value=0, max_value=100000)
validator.expect_column_values_to_be_in_set("status", ["PENDING", "ACTIVE", "CANCELLED"])
validator.save_expectation_suite()

# Run validation
results = validator.validate()
# Generates a visual HTML report showing pass/fail per expectation
```

### dbt Tests
**What it is:** dbt (data build tool) transforms data inside your warehouse using SQL. It includes a built-in test framework:

```yaml
# schema.yml
models:
  - name: orders
    columns:
      - name: order_id
        tests:
          - not_null          # order_id must never be NULL
          - unique            # order_id must be unique
      - name: status
        tests:
          - accepted_values:
              values: ['PENDING', 'ACTIVE', 'CANCELLED', 'COMPLETED']
      - name: customer_id
        tests:
          - relationships:
              to: ref('customers')
              field: customer_id  # referential integrity
```

Run with: `dbt test` — generates a summary of which tests passed/failed.

**Custom dbt test example:**
```sql
-- tests/assert_revenue_positive.sql
SELECT order_id
FROM {{ ref('orders') }}
WHERE amount <= 0
-- If this query returns any rows, the test FAILS
```

### Apache Spark Testing
- Unit-test Spark transformation functions using `pyspark.testing` utilities
- Use a local Spark session for tests (no cluster needed)
```python
from pyspark.sql import SparkSession
from pyspark.testing.utils import assertDataFrameEqual

spark = SparkSession.builder.master("local").getOrCreate()

def test_transform_normalises_status():
    input_df = spark.createDataFrame([("ord1", "male"), ("ord2", "Male")], ["id", "gender"])
    result_df = normalise_gender(input_df)
    expected = spark.createDataFrame([("ord1", "MALE"), ("ord2", "MALE")], ["id", "gender"])
    assertDataFrameEqual(result_df, expected)
```

### Database Comparison Tools
- **Redgate SQL Compare** — schema and data comparison for SQL Server
- **pgdiff** — schema comparison for PostgreSQL
- **DataGrip** (JetBrains) — visual query + comparison across databases
- **Apache Griffin** — open-source data quality service for big data

### Python Pandas for Data Comparison in Tests
```python
import pandas as pd

def test_pipeline_output():
    source_df = pd.read_sql("SELECT * FROM source.orders", source_conn)
    target_df = pd.read_sql("SELECT * FROM target.orders_warehouse", target_conn)

    # Count assertion
    assert len(source_df) == len(target_df), f"Count mismatch: {len(source_df)} source vs {len(target_df)} target"

    # Merge and compare field by field
    merged = source_df.merge(target_df, left_on="order_id", right_on="order_id", suffixes=("_src", "_tgt"))
    mismatches = merged[merged["amount_src"] != merged["amount_tgt"]]
    assert len(mismatches) == 0, f"Amount mismatches found:\n{mismatches[['order_id','amount_src','amount_tgt']]}"
```

---

## SECTION 10 — Interview Q&A

### Q1: What are the different types of ETL testing?
**A:** The main types are:
- **Data completeness testing** — verifying all records arrived (row count checks)
- **Data accuracy testing** — verifying field values are correct (source vs target comparison)
- **Data transformation testing** — verifying each business rule is applied correctly
- **Data integrity testing** — referential integrity, no orphaned records
- **Data uniqueness testing** — no duplicate records
- **Error handling testing** — rejected records are logged, pipeline continues
- **Performance testing** — pipeline finishes within the time SLA

### Q2: How do you verify a data migration was successful?
**A:** I take a baseline snapshot before migration (row counts per table, checksums, sample records). After migration I verify: (1) total count in target matches source count, (2) excluded/rejected records are documented with reasons, (3) field-by-field comparison on a sample of records matches, (4) referential integrity holds (no orphaned records), (5) business-critical fields like amounts and dates match exactly. I produce a reconciliation report accounting for every record. Success means every record is either in the target or documented as excluded/rejected with a reason.

### Q3: What is data completeness vs data accuracy?
**A:** They are different quality dimensions. **Completeness** is about whether data EXISTS — are all the rows there, are mandatory fields populated? You can have 100% completeness but 0% accuracy (every row is present but the values are wrong). **Accuracy** is about whether the values are CORRECT — do they reflect the real-world truth? For example, if I have 1,000 order records in target (completeness: 100%) but the `amount` field on half of them is wrong due to a rounding bug (accuracy: 50%), completeness passes but accuracy fails.

### Q4: Write a SQL query to find duplicate records.
**A:**
```sql
-- Find IDs that appear more than once
SELECT order_id, COUNT(*) AS occurrences
FROM orders_warehouse
GROUP BY order_id
HAVING COUNT(*) > 1
ORDER BY occurrences DESC;

-- See the actual duplicate rows
SELECT *
FROM orders_warehouse
WHERE order_id IN (
    SELECT order_id FROM orders_warehouse GROUP BY order_id HAVING COUNT(*) > 1
)
ORDER BY order_id, created_at;
```

### Q5: What should you check after a data pipeline runs?
**A:** After a pipeline run I check: (1) source count vs target count — do they match or is the difference explained? (2) Any error records in the dead letter queue or error log — what were they and why did they fail? (3) A sample of records field-by-field for accuracy (4) Referential integrity — no orphaned child records (5) No unexpected NULLs in mandatory fields (6) No duplicate records where uniqueness is required (7) Pipeline completed within the time SLA (8) No anomalies in aggregates (sum, count match expected range).

### Q6: How do you test a transformation rule?
**A:** I take the transformation rule (e.g. "standardise gender: 'M', 'Male', 'male' all become 'MALE'"), design test cases covering the happy path and edge cases, create specific test data in the source containing each variant, run the pipeline, then query the target to verify the output. For each test record I assert the target value is exactly what the rule specifies. I also test what happens with invalid input (e.g. `gender = "X"`) — does it use a default, get rejected with an error log entry, or cause the pipeline to fail?

### Q7: What are the data quality dimensions?
**A:** The six standard data quality dimensions are: **Completeness** (all data present), **Accuracy** (values are correct), **Consistency** (same data agrees across systems), **Timeliness** (data arrives within SLA), **Validity** (values conform to expected domain/format/range), and **Uniqueness** (no unintended duplicates). In a senior QA interview, demonstrating you can test all six — not just count checks — distinguishes a data-aware QA from a surface-level tester.

### Q8: How do you handle PII when testing ETL pipelines?
**A:** Production data with PII (names, emails, DOBs, payment details) must never be copied to test environments in raw form. The options are: (1) **Generate synthetic test data** using Faker — no real PII at all, (2) **Mask production data** — pseudonymise names, hash emails, substitute fixed placeholder dates, then use the masked copy in test environments, (3) **Tokenise** sensitive fields, keeping a mapping only in the secure production environment. All data access should be governed by policy and masked data should be treated as production-equivalent from a security perspective.

### Q9: What is a reconciliation report?
**A:** A reconciliation report is the formal record produced after a data migration or ETL run that accounts for every source record. It shows: total source count, total successfully loaded count, count excluded by business rules (with reasons), count rejected with errors (with error codes), and proves these numbers add up to the source total. It answers the question "where is every record?" A well-written reconciliation report is the primary evidence that a migration was complete and correct, and is often a sign-off requirement before decommissioning the source system.

### Q10: How do you test pipeline failure recovery?
**A:** I test recovery by deliberately triggering failures: (1) Stop the consumer mid-run while events are in-flight — restart it and verify all in-flight events are processed exactly once (2) Introduce a poison pill message (malformed event) — verify it goes to DLQ after N retries and does not block other messages (3) Simulate a database timeout during load — verify the pipeline retries and eventually succeeds, or logs the failure without corrupting partial writes (4) Kill the pipeline process and restart it — verify it resumes from the last committed offset/checkpoint (not from the beginning), meaning no double-processing.

---

*Guide covers: ETL concepts, data quality dimensions, SQL validation queries, migration testing, streaming/Kafka pipeline testing, test data management, Great Expectations, dbt tests, and interview Q&A.*
