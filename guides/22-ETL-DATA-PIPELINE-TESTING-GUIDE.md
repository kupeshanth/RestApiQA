# ETL & Data Pipeline Testing — Full Q&A Interview Guide | SQL + Data Quality + Migration

> Senior QA Interview Preparation — Every concept answered as a real interview question

---

## SECTION 1 — ETL Fundamentals

---

**Q1: What is ETL? What does Extract, Transform, Load mean?**

**A:** ETL stands for Extract, Transform, Load. It is the process of moving data from one or more source systems into a target system — typically a data warehouse, analytics database, or another operational database. It is the backbone of every data integration, reporting, and migration project in enterprise software.

**Extract** — Pull data from the source system.
- Sources can be: relational databases (MySQL, PostgreSQL, Oracle, SQL Server), REST APIs, flat files (CSV, JSON, XML), cloud storage (S3, Google Cloud Storage), message queues (Kafka, RabbitMQ), legacy systems, SaaS platforms.
- Extract can be a full load (all records every time) or incremental (only records changed since the last run, identified by a timestamp or change data capture flag).

**Transform** — Clean, reshape, aggregate, and enrich the data.
- Standardise formats: `"2024-01-15"`, `"15/01/2024"`, `"Jan 15 2024"` all become `2024-01-15T00:00:00Z`
- Standardise values: `"M"`, `"Male"`, `"male"`, `"MALE"` all become `"MALE"`
- Split columns: `full_name` → `first_name` + `last_name`
- Join with reference data: add `country_name` from `country_code`
- Aggregate: sum daily sales into monthly totals
- Filter: remove cancelled or test records
- Enrich: calculate derived fields (e.g. `profit = revenue - cost`)

**Load** — Write the transformed data to the target system.
- Load modes: full replace (truncate and reload), incremental append (add new records only), upsert (insert new records, update existing ones by key).

```
SOURCE                  →   TRANSFORM               →   TARGET
──────────────────          ──────────────────          ──────────────────
REST API                    Normalise dates             Data Warehouse
Relational DB               Standardise values          Analytics DB
CSV / JSON files            Aggregate + filter          Reporting system
Kafka stream                Join + enrich               Another app DB
```

---

**Q2: Why does QA test data pipelines? What goes wrong if they are not tested?**

**A:** Data pipelines move the raw material of business decisions. If a pipeline has a bug, the business makes decisions based on wrong data — and often does not know it. Silent data corruption is worse than an outright failure because the application appears to work but the underlying data is incorrect.

**What goes wrong without QA:**
- Revenue figures in the monthly report are wrong because a rounding error in the transform drops decimal places
- 50,000 customer records fail to migrate but no one notices because there is no count check
- A timezone bug converts all timestamps to the wrong offset, corrupting reporting for one time zone
- Duplicate records appear because the pipeline ran twice without deduplication and the upsert logic failed
- Referential integrity breaks because child records were loaded before their parent records, causing orphaned rows
- PII (names, emails, passport numbers) leaks into a test environment because data masking was skipped

**QA's role in data testing:**
- Verify row counts: source count matches target count (or any difference is documented)
- Verify accuracy: field-by-field comparison between source and target
- Verify transformation rules: every rule produces the correct output for all input variants
- Verify error handling: rejected records are logged, not silently dropped
- Verify edge cases: NULLs, empty strings, special characters, maximum lengths, boundary values

---

**Q3: What are the types of data testing?**

**A:** There are six standard data quality dimensions, each corresponding to a type of data test:

| Dimension | Definition | Test Focus |
|-----------|-----------|------------|
| **Completeness** | All expected records and field values are present | Row count checks, NULL checks on mandatory fields |
| **Accuracy** | Values in the target correctly reflect the source | Field-by-field comparison, calculated field verification |
| **Consistency** | The same data referenced across systems or tables agrees | Cross-system queries, aggregate comparison |
| **Timeliness** | Data arrives within the agreed SLA window | Measure time from source event to target appearance |
| **Validity** | Values conform to expected domain, format, and range | Format checks, range checks, enum checks |
| **Uniqueness** | No unintended duplicate records | `GROUP BY + HAVING COUNT(*) > 1` queries |

Beyond these dimensions, there are also:
- **Transformation testing** — each business rule produces the correct output
- **Error handling testing** — rejected records are logged, pipeline does not silently discard data
- **Referential integrity testing** — foreign keys have matching parent records
- **Performance testing** — pipeline completes within the time SLA

---

## SECTION 2 — Data Quality Dimensions in Detail

---

**Q4: What is data completeness testing? How do you verify it with SQL?**

**A:** Data completeness testing verifies that all expected records and field values are present in the target — nothing is missing. It is the most fundamental check in ETL testing.

**Two levels of completeness:**

1. **Row completeness:** The number of records in the target matches the expected number from the source.
2. **Field completeness:** Mandatory fields (fields that should always have a value) are not NULL in the target.

**SQL for row completeness:**

```sql
-- Count records in source for the batch window
SELECT COUNT(*) AS source_count
FROM source_schema.orders
WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01';

-- Count records in target for the same window
SELECT COUNT(*) AS target_count
FROM target_schema.orders_warehouse
WHERE source_created_at >= '2024-01-01' AND source_created_at < '2024-02-01';

-- Combined: difference should be 0 (or equal to documented rejected record count)
SELECT
    (SELECT COUNT(*) FROM source_schema.orders
     WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01') AS source_count,
    (SELECT COUNT(*) FROM target_schema.orders_warehouse
     WHERE source_created_at >= '2024-01-01' AND source_created_at < '2024-02-01') AS target_count,
    (SELECT COUNT(*) FROM source_schema.orders
     WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01') -
    (SELECT COUNT(*) FROM target_schema.orders_warehouse
     WHERE source_created_at >= '2024-01-01' AND source_created_at < '2024-02-01') AS difference;
```

**SQL for field completeness (NULL check on mandatory fields):**

```sql
-- Any row returned by this query is a completeness failure
SELECT order_id, customer_id, amount, status, order_date
FROM target_schema.orders_warehouse
WHERE customer_id IS NULL
   OR amount IS NULL
   OR status IS NULL
   OR order_date IS NULL;
```

**Note:** If the count difference is non-zero, it must be accounted for. Acceptable reasons: records excluded by a documented business rule (e.g. cancelled records not migrated), records rejected due to data quality failure (these must appear in the error log). "We lost some records" with no explanation is never acceptable.

---

**Q5: What is data accuracy testing? How do you compare source versus target?**

**A:** Data accuracy testing verifies that the values in the target correctly represent what was in the source — the transformation produced the correct output.

You can have 100% completeness (all rows present) but 0% accuracy (all the values are wrong). They are independent dimensions.

**How to compare source versus target:**

The approach is a field-by-field JOIN between source and target on the primary key, filtering for any row where at least one field differs.

```sql
-- Field-by-field accuracy comparison — any row returned is an accuracy failure
SELECT
    s.order_id,
    s.customer_id       AS src_customer_id,
    t.customer_id       AS tgt_customer_id,
    s.amount            AS src_amount,
    t.amount            AS tgt_amount,
    s.status            AS src_status,
    t.status            AS tgt_status,
    CASE WHEN s.customer_id != t.customer_id THEN 'MISMATCH: customer_id' ELSE NULL END AS err_customer,
    CASE WHEN s.amount      != t.amount      THEN 'MISMATCH: amount'      ELSE NULL END AS err_amount,
    CASE WHEN s.status      != t.status      THEN 'MISMATCH: status'      ELSE NULL END AS err_status
FROM source_schema.orders s
JOIN target_schema.orders_warehouse t ON s.order_id = t.order_id
WHERE s.customer_id != t.customer_id
   OR s.amount      != t.amount
   OR s.status      != t.status;
```

For large datasets, run the accuracy check on a representative sample rather than every row:

```sql
-- Accuracy check on a random sample of 1,000 records
SELECT s.order_id, s.amount AS src_amount, t.amount AS tgt_amount
FROM source_schema.orders s
JOIN target_schema.orders_warehouse t ON s.order_id = t.order_id
WHERE s.amount != t.amount
  AND s.order_id IN (
      SELECT order_id FROM source_schema.orders ORDER BY RANDOM() LIMIT 1000
  );
```

---

**Q6: What is data consistency testing?**

**A:** Data consistency testing verifies that the same piece of information, when it appears in multiple places, has the same value in all of them. Inconsistency occurs when two systems are updated by separate processes with different logic, or when asynchronous propagation means one system updates before another.

**Example of inconsistency:** A customer's profile in the CRM system shows `total_orders = 150`. A COUNT of that customer's orders in the data warehouse returns 147. These should agree — they are derived from the same source. If they differ, one of the systems has incorrect data.

**SQL for consistency testing:**

```sql
-- Cross-system consistency: does the customer's stored order count match the actual count?
SELECT
    c.customer_id,
    c.total_orders          AS stored_order_count,
    COUNT(o.order_id)       AS actual_order_count,
    c.total_orders - COUNT(o.order_id) AS discrepancy
FROM target_schema.customers c
LEFT JOIN target_schema.orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.total_orders
HAVING c.total_orders != COUNT(o.order_id);

-- Any row returned is a consistency failure
```

**Also check:** The same product price in the product catalogue vs in historical order records. The same user email in the main users table vs in the audit log table.

---

**Q7: What is data timeliness testing? How do you verify SLA?**

**A:** Data timeliness testing verifies that data arrives in the target system within the agreed SLA (Service Level Agreement) time window. For batch pipelines, the SLA might be "all yesterday's orders must be in the warehouse by 6 AM." For streaming pipelines, it might be "an order must appear in the analytics system within 5 seconds of being placed."

**How to test timeliness:**

1. Record the timestamp when the source event is created (`T1`).
2. Poll the target system until the record appears; record the timestamp when it appears (`T2`).
3. Assert that `T2 - T1 < SLA threshold`.

**SQL for timeliness verification:**

```sql
-- Find records that violated the SLA (arrived more than 5 minutes after creation)
SELECT
    order_id,
    source_created_at,
    target_received_at,
    EXTRACT(EPOCH FROM (target_received_at - source_created_at)) AS latency_seconds
FROM target_schema.orders_warehouse
WHERE EXTRACT(EPOCH FROM (target_received_at - source_created_at)) > 300  -- 5-minute SLA = 300 seconds
ORDER BY latency_seconds DESC;

-- For batch pipelines: verify all records for a given day landed before 6 AM
SELECT COUNT(*) AS records_missing_sla
FROM target_schema.orders_warehouse
WHERE source_created_at::DATE = '2024-01-15'
  AND target_received_at > '2024-01-16 06:00:00';
-- Should return 0
```

---

**Q8: What is data validity testing? Give examples of invalid data.**

**A:** Data validity testing verifies that values conform to the expected domain — correct format, within the allowed range, and from the allowed set of values.

**Examples of invalid data and the SQL to detect them:**

```sql
-- Invalid: status values not in the allowed set
SELECT order_id, status
FROM target_schema.orders_warehouse
WHERE status NOT IN ('PENDING', 'ACTIVE', 'SHIPPED', 'DELIVERED', 'CANCELLED');

-- Invalid: negative prices or zero-price orders (if business rule says all orders > $0)
SELECT order_id, amount
FROM target_schema.orders_warehouse
WHERE amount <= 0;

-- Invalid: dates outside expected range (1970-01-01 is an epoch timestamp conversion failure)
SELECT order_id, order_date
FROM target_schema.orders_warehouse
WHERE order_date < '2000-01-01'
   OR order_date > CURRENT_DATE + INTERVAL '1 day';

-- Invalid: ages that are impossible
SELECT customer_id, age
FROM target_schema.customers
WHERE age < 0 OR age > 150;

-- Invalid: email format (basic check)
SELECT customer_id, email
FROM target_schema.customers
WHERE email NOT LIKE '%@%.%'
   OR email IS NULL;

-- Invalid: phone number format (digits only, 10–15 characters)
SELECT customer_id, phone
FROM target_schema.customers
WHERE phone ~ '[^0-9+\-\s\(\)]'  -- contains invalid characters
   OR LENGTH(REGEXP_REPLACE(phone, '[^0-9]', '', 'g')) < 10;
```

**Common validity failures in ETL:**
- A failed date conversion produces `1970-01-01` (Unix epoch) instead of NULL
- A failed type cast produces `0` instead of NULL for numeric fields
- A status value from the source system that was never added to the allowed set in the transform
- A truncated string because the target column is shorter than the source column

---

**Q9: What is data uniqueness testing? How do you find duplicates with SQL?**

**A:** Data uniqueness testing verifies that no duplicate records exist where uniqueness is required — typically on the primary key or a natural business key (e.g. `order_id`, `customer_email`).

**How duplicates appear in ETL:**
- The pipeline ran twice without deduplication logic
- Upsert logic failed and performed an INSERT instead of an UPDATE, creating a second copy
- Duplicate events consumed from a Kafka topic (at-least-once delivery)
- Multiple source systems both contained the same record with slightly different fields

**SQL to find duplicates:**

```sql
-- Step 1: Find IDs that appear more than once
SELECT order_id, COUNT(*) AS duplicate_count
FROM target_schema.orders_warehouse
GROUP BY order_id
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

-- Step 2: See the actual duplicate rows for investigation
SELECT *
FROM target_schema.orders_warehouse
WHERE order_id IN (
    SELECT order_id
    FROM target_schema.orders_warehouse
    GROUP BY order_id
    HAVING COUNT(*) > 1
)
ORDER BY order_id, created_at;

-- Step 3: Find duplicates on a composite key (when no single column is the unique key)
SELECT customer_id, order_date, amount, COUNT(*) AS duplicate_count
FROM target_schema.orders_warehouse
GROUP BY customer_id, order_date, amount
HAVING COUNT(*) > 1;
```

**Note in interview:** The two-step approach (find the IDs first, then retrieve the rows) is the most efficient pattern. Do not use `DISTINCT` to hide duplicates — find them so they can be investigated and fixed.

---

## SECTION 3 — The 8-Step ETL Testing Approach

---

**Q10: Walk through the complete 8-step ETL testing approach.**

**A:** A structured ETL test follows these eight steps in order:

**Step 1 — Understand the data flow.**
Before writing any test, map the pipeline completely. Identify: the source system and schema, every transformation rule (normalise, join, aggregate, filter, calculate), the target schema and constraints, the expected volume, and whether this is a one-time migration or a recurring pipeline. Output: a data flow diagram and a transformation rule document.

**Step 2 — Design test cases for each transformation rule.**
For every rule, design at minimum: a happy-path test, a boundary test (min/max values), an edge case test (NULL, empty string, special characters, Unicode), and a negative test (invalid input — what should happen?).

**Step 3 — Set up test data in the source.**
Create or seed records in the source system that cover every test case. Include records that will exercise every branch of transform logic. Include records that SHOULD be rejected, to test error handling. Document exactly what you put in — you need to know what to expect in the target.

**Step 4 — Run the pipeline.**
Execute in a controlled test environment (never production). Record the run timestamp. Monitor the pipeline logs during execution for warnings and errors.

**Step 5 — Validate target against expected output.**
Query the target for each test record. Compare field-by-field against the expected values you documented. Use the SQL queries from Step 2.

**Step 6 — Verify record counts match.**
`Source count = Target count`, or `Source count - Rejected count = Target count`. Any unexplained difference is a defect. Document every expected difference with a reason.

**Step 7 — Verify rejected records are logged.**
Records that fail validation must NOT be silently discarded. They must appear in an error log or dead letter table with a reason code. Verify the expected rejected records are there.

**Step 8 — Verify edge cases.**
Test NULL values, empty strings, maximum-length strings, special characters (apostrophes, Unicode, emoji), leading/trailing whitespace, and numeric overflow. Each should be handled as designed — not crash the pipeline.

---

**Q11: How do you verify a transformation rule with SQL?**

**A:** Every transformation rule should be verified by creating specific test data in the source that exercises that rule, running the pipeline, then querying the target to confirm the rule was applied correctly.

**Example rule:** "Standardise the `gender` field — map 'M', 'Male', 'male', 'MALE' all to 'MALE'. Map 'F', 'Female', 'female', 'FEMALE' all to 'FEMALE'. Any other value should be set to NULL and logged as a data quality warning."

**Test data in source:**

| customer_id | gender (source) |
|-------------|----------------|
| C001 | M |
| C002 | Male |
| C003 | male |
| C004 | MALE |
| C005 | F |
| C006 | Female |
| C007 | X (invalid) |
| C008 | NULL |
| C009 | (empty string) |

**Verification SQL in target:**

```sql
-- Verify the transformation produced expected results for each test case
SELECT
    customer_id,
    gender AS transformed_gender,
    CASE
        WHEN customer_id IN ('C001','C002','C003','C004') AND gender = 'MALE'     THEN 'PASS'
        WHEN customer_id IN ('C005','C006')               AND gender = 'FEMALE'   THEN 'PASS'
        WHEN customer_id IN ('C007','C008','C009')        AND gender IS NULL       THEN 'PASS'
        ELSE 'FAIL'
    END AS test_result
FROM target_schema.customers
WHERE customer_id IN ('C001','C002','C003','C004','C005','C006','C007','C008','C009')
ORDER BY customer_id;

-- All rows should show 'PASS'
```

---

## SECTION 4 — SQL Validation Queries

---

**Q12: Show 7 complete SQL validation queries for ETL testing.**

**A:**

**Query 1 — Count check: source vs target**
```sql
SELECT
    (SELECT COUNT(*) FROM source_schema.orders
     WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01') AS source_count,
    (SELECT COUNT(*) FROM target_schema.orders_warehouse
     WHERE source_created_at >= '2024-01-01' AND source_created_at < '2024-02-01') AS target_count,
    (SELECT COUNT(*) FROM source_schema.orders
     WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01') -
    (SELECT COUNT(*) FROM target_schema.orders_warehouse
     WHERE source_created_at >= '2024-01-01' AND source_created_at < '2024-02-01') AS difference;
-- Expected: difference = 0
```

**Query 2 — Records in source missing from target**
```sql
SELECT s.order_id, s.customer_id, s.amount, 'MISSING IN TARGET' AS status
FROM source_schema.orders s
LEFT JOIN target_schema.orders_warehouse t ON s.order_id = t.order_id
WHERE t.order_id IS NULL;
-- Any row returned = a record that was in source but did not land in target
```

**Query 3 — Phantom records in target not in source**
```sql
SELECT t.order_id, t.customer_id, t.amount, 'UNEXPECTED IN TARGET' AS status
FROM target_schema.orders_warehouse t
LEFT JOIN source_schema.orders s ON t.order_id = s.order_id
WHERE s.order_id IS NULL;
-- Any row returned = a record in target with no matching source (phantom/duplicate)
```

**Query 4 — Field-by-field accuracy comparison**
```sql
SELECT
    s.order_id,
    s.amount    AS src_amount,   t.amount    AS tgt_amount,
    s.status    AS src_status,   t.status    AS tgt_status,
    s.customer_id AS src_cust,   t.customer_id AS tgt_cust
FROM source_schema.orders s
JOIN target_schema.orders_warehouse t ON s.order_id = t.order_id
WHERE s.amount      != t.amount
   OR s.status      != t.status
   OR s.customer_id != t.customer_id;
-- Any row returned = a field-level accuracy failure
```

**Query 5 — NULL check on mandatory fields**
```sql
SELECT order_id, customer_id, amount, status, order_date
FROM target_schema.orders_warehouse
WHERE customer_id IS NULL
   OR amount      IS NULL
   OR status      IS NULL
   OR order_date  IS NULL;
-- Any row returned = a mandatory field is NULL (completeness failure)
```

**Query 6 — Duplicate detection**
```sql
SELECT order_id, COUNT(*) AS duplicate_count
FROM target_schema.orders_warehouse
GROUP BY order_id
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;
-- Any row returned = duplicate records exist
```

**Query 7 — Referential integrity check**
```sql
-- Orphaned order_items: order items with no matching parent order
SELECT oi.order_item_id, oi.order_id
FROM target_schema.order_items oi
LEFT JOIN target_schema.orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL;
-- Any row returned = referential integrity failure (orphaned child records)
```

---

**Q13: What is a count check? Write the SQL.**

**A:** A count check is the simplest and most fundamental ETL validation: comparing the number of records in the source system against the number of records in the target system after the pipeline runs. If the counts differ unexpectedly, records were either lost during the pipeline or created (phantom records/duplicates).

```sql
-- Simple count check between source and target
SELECT
    'source' AS location,
    COUNT(*) AS record_count
FROM source_schema.orders
WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01'

UNION ALL

SELECT
    'target' AS location,
    COUNT(*) AS record_count
FROM target_schema.orders_warehouse
WHERE source_created_at >= '2024-01-01' AND source_created_at < '2024-02-01';

-- The two counts should match (or differ by exactly the number of documented rejected records)
```

**Count check with difference and rejected record accounting:**

```sql
SELECT
    src.source_count,
    tgt.target_count,
    rej.rejected_count,
    (src.source_count - tgt.target_count - rej.rejected_count) AS unexplained_difference
FROM
    (SELECT COUNT(*) AS source_count FROM source_schema.orders
     WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01') src,
    (SELECT COUNT(*) AS target_count FROM target_schema.orders_warehouse
     WHERE source_created_at >= '2024-01-01' AND source_created_at < '2024-02-01') tgt,
    (SELECT COUNT(*) AS rejected_count FROM etl_schema.rejected_records
     WHERE batch_date = '2024-01-01' AND pipeline_name = 'orders_pipeline') rej;

-- unexplained_difference should be 0
-- source_count = target_count + rejected_count
```

---

**Q14: How do you do row-by-row comparison between source and target?**

**A:** Row-by-row comparison uses a FULL OUTER JOIN between source and target on the primary key. This pattern finds records that exist in one side but not the other, and also allows field-by-field comparison for records that match on the key.

```sql
-- Full outer join comparison: finds missing, phantom, and mismatched records in one query
SELECT
    COALESCE(s.order_id, t.order_id) AS order_id,
    CASE
        WHEN s.order_id IS NULL  THEN 'PHANTOM IN TARGET'   -- in target, not in source
        WHEN t.order_id IS NULL  THEN 'MISSING FROM TARGET' -- in source, not in target
        WHEN s.amount != t.amount OR s.status != t.status
             OR s.customer_id != t.customer_id THEN 'FIELD MISMATCH'
        ELSE 'OK'
    END AS comparison_result,
    s.amount      AS src_amount,   t.amount      AS tgt_amount,
    s.status      AS src_status,   t.status      AS tgt_status,
    s.customer_id AS src_cust_id,  t.customer_id AS tgt_cust_id
FROM source_schema.orders s
FULL OUTER JOIN target_schema.orders_warehouse t ON s.order_id = t.order_id
WHERE s.order_id IS NULL           -- phantom records
   OR t.order_id IS NULL           -- missing records
   OR s.amount != t.amount         -- field mismatches
   OR s.status != t.status
   OR s.customer_id != t.customer_id
ORDER BY comparison_result, order_id;
```

For very large tables (millions of rows), do not run a full outer join against the entire table. Instead:
1. Run the count check first — if counts match, sample for accuracy.
2. Run the row comparison on a specific date partition or batch.
3. Use a hash/checksum approach: compute a hash of each row and compare hashes, only investigating rows where the hash differs.

---

**Q15: How do you find records that failed to load?**

**A:** Records that fail to load should be captured in an error log table or a dead letter queue. After each pipeline run, you query this error table to verify: the expected rejected records are there, each has a reason code, and no unexpected records were rejected.

```sql
-- Query the error/rejected records log for the latest pipeline run
SELECT
    pipeline_name,
    batch_date,
    source_record_id,
    source_table,
    error_code,
    error_message,
    failed_at
FROM etl_schema.rejected_records
WHERE pipeline_name = 'orders_pipeline'
  AND batch_date = '2024-01-15'
ORDER BY failed_at DESC;

-- Count of rejected records by error type
SELECT
    error_code,
    COUNT(*) AS failure_count,
    MIN(failed_at) AS first_occurrence,
    MAX(failed_at) AS last_occurrence
FROM etl_schema.rejected_records
WHERE pipeline_name = 'orders_pipeline'
  AND batch_date = '2024-01-15'
GROUP BY error_code
ORDER BY failure_count DESC;
```

**What a well-designed error log contains:**
- The original source record ID
- The pipeline name and batch date
- The specific error code (e.g. `INVALID_DATE_FORMAT`, `NULL_MANDATORY_FIELD`, `FK_VIOLATION`)
- The error message with enough detail to diagnose
- The full source record (for reprocessing after the fix)

**If there is no error log:** any records that fail to load vanish silently. This is a design defect. QA should raise it — silent data loss is unacceptable.

---

**Q16: How do you check referential integrity after an ETL run?**

**A:** Referential integrity means that all foreign key references in the target point to existing parent records. If the pipeline loads child records before their parent records (or filters out parents while keeping children), orphaned records result.

```sql
-- Find orphaned order_items (child) with no matching order (parent)
SELECT oi.order_item_id, oi.order_id, 'No matching order' AS violation
FROM target_schema.order_items oi
LEFT JOIN target_schema.orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL;

-- Find orders referencing customers that do not exist in the customer table
SELECT DISTINCT o.order_id, o.customer_id, 'No matching customer' AS violation
FROM target_schema.orders o
LEFT JOIN target_schema.customers c ON o.customer_id = c.customer_id
WHERE c.customer_id IS NULL;

-- Find products referenced in order items that do not exist in the products table
SELECT oi.order_item_id, oi.product_id, 'No matching product' AS violation
FROM target_schema.order_items oi
LEFT JOIN target_schema.products p ON oi.product_id = p.product_id
WHERE p.product_id IS NULL;

-- Summary count of all referential integrity violations
SELECT
    'order_items → orders'     AS relationship,
    COUNT(*) AS violation_count
FROM target_schema.order_items oi
LEFT JOIN target_schema.orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL
UNION ALL
SELECT
    'orders → customers'       AS relationship,
    COUNT(*)
FROM target_schema.orders o
LEFT JOIN target_schema.customers c ON o.customer_id = c.customer_id
WHERE c.customer_id IS NULL;
```

**Common causes:** Parent records were filtered out by a business rule while child records were not. The pipeline loaded tables in the wrong order (children first). A partial failure stopped the parent load partway through.

---

## SECTION 5 — Data Migration Testing

---

**Q17: What is data migration testing? What phases does it have?**

**A:** Data migration testing is a specialised form of ETL testing for a one-time (or phased) move of data from a legacy system to a new system. It differs from recurring ETL in that it has a finite scope, a cutover point after which the legacy system is decommissioned, and much higher stakes — you cannot simply rerun the pipeline if data is lost, because the source system may no longer exist.

**Phases of data migration testing:**

**Phase 1 — Pre-migration planning:**
- Understand the source schema and the target schema in full
- Document every data mapping and transformation rule
- Agree on the scope: what is migrated, what is excluded (with documented reason)
- Define acceptance criteria: what counts as a successful migration?

**Phase 2 — Pre-migration baseline:**
- Take a snapshot of the source: row counts per table, field statistics, checksums, sample records
- Save these as the reference point for post-migration validation

**Phase 3 — Migration rehearsal (dry run):**
- Run the full migration against a test environment (not production)
- Validate using all the post-migration checks
- Identify and fix issues; repeat until the rehearsal passes completely

**Phase 4 — Production migration:**
- Run the migration during a planned maintenance window
- Monitor progress and error rates in real time

**Phase 5 — Post-migration validation:**
- Run all count checks, accuracy checks, referential integrity checks
- Compare against the pre-migration baseline
- Produce the reconciliation report

**Phase 6 — User acceptance validation:**
- Business stakeholders verify critical data in the new system (not just counts — they check specific known records)
- Smoke test the new application against the migrated data

**Phase 7 — Cutover and source decommission:**
- Only after Phase 6 passes, cut over to the new system
- Retain the source for a period (read-only) as a safety net before decommission

---

**Q18: What is a baseline snapshot and how do you create one?**

**A:** A baseline snapshot is a capture of the state of the source system before a migration begins. It provides the reference point used to validate whether the migration was complete and correct. Without a baseline, you cannot prove what was in the source at the time of migration.

**How to create one:**

```sql
-- Step 1: Record count per table
SELECT 'customers' AS table_name, COUNT(*) AS record_count FROM legacy.customers
UNION ALL
SELECT 'orders',   COUNT(*) FROM legacy.orders
UNION ALL
SELECT 'order_items', COUNT(*) FROM legacy.order_items
UNION ALL
SELECT 'products', COUNT(*) FROM legacy.products;
-- Save the output as a file or a snapshot table

-- Step 2: Aggregate statistics for key financial fields
SELECT
    COUNT(*)       AS total_orders,
    SUM(amount)    AS total_revenue,
    AVG(amount)    AS avg_order_value,
    MIN(amount)    AS min_order_value,
    MAX(amount)    AS max_order_value
FROM legacy.orders
WHERE status NOT IN ('TEST', 'DELETED'); -- exclude records not being migrated

-- Step 3: Row-level checksum for integrity verification
SELECT
    MD5(STRING_AGG(
        CAST(order_id AS TEXT) || '|' || amount::TEXT || '|' || status
        ORDER BY order_id
    )) AS orders_checksum
FROM legacy.orders;
-- Save this checksum — after migration, recalculate on the target and compare

-- Step 4: Sample 100 representative records for post-migration manual comparison
SELECT *
FROM legacy.customers
ORDER BY RANDOM()
LIMIT 100;
-- Save these as your spot-check set
```

**Baseline saved as:** a spreadsheet or a dedicated `migration_baseline` schema table. After migration, every number in the baseline is compared to the same number in the new system.

---

**Q19: What is a reconciliation report?**

**A:** A reconciliation report is the formal document produced after a data migration or ETL run that accounts for every source record and proves the migration was complete. It answers the question: "Where is every record?"

The reconciliation report shows:

| Category | Count | Notes |
|----------|-------|-------|
| Source records (total) | 1,250,000 | Total in legacy system at time of migration |
| Successfully migrated | 1,247,835 | Landed correctly in the new system |
| Excluded by business rule | 2,100 | Deleted/archived records — not migrated by design |
| Failed with documented error | 65 | Encoding issues in 65 records — logged with IDs |
| **Accounted for (sum)** | **1,250,000** | **1,247,835 + 2,100 + 65 = 1,250,000** |
| Unexplained difference | 0 | Must always be zero |

**The key rule:** Every record in the source must appear in exactly one of: successfully migrated, excluded by business rule, or failed with a documented error. The three categories must sum to the source total. Any unexplained difference is a defect.

The reconciliation report is typically a sign-off requirement for decommissioning the source system — the business will not allow the legacy system to be turned off until the reconciliation report shows zero unexplained difference.

---

**Q20: How do you test rollback capability for a migration?**

**A:** Rollback capability means that if the migration fails partway through, or if critical validation failures are found after cutover, the system can be returned to its pre-migration state within a defined time window (usually the maintenance window).

**Testing rollback:**

1. **Before migration day:** Document and rehearse the rollback procedure in the test environment. Rollback should be a well-rehearsed procedure, not an improvised response to an emergency.

2. **Restore from backup:** The most common rollback approach is restoring the target from a pre-migration backup. Test this in the test environment by:
   - Taking a backup of the target before migration.
   - Running the migration.
   - Restoring from the backup.
   - Verifying the state after restore is identical to the pre-migration baseline (row counts match, checksums match).

3. **Measure rollback time:** Time how long the restore takes. Confirm it fits within the maintenance window. A restore that takes 8 hours for a 6-hour window is a problem.

4. **Verify data integrity after rollback:**

```sql
-- After rollback, verify counts match the pre-migration baseline
SELECT
    (SELECT COUNT(*) FROM baseline_schema.orders) AS baseline_count,
    (SELECT COUNT(*) FROM restored_schema.orders) AS restored_count,
    (SELECT COUNT(*) FROM baseline_schema.orders) -
    (SELECT COUNT(*) FROM restored_schema.orders) AS difference;
-- Difference must be 0

-- Verify checksum matches the pre-migration checksum
SELECT
    MD5(STRING_AGG(
        CAST(order_id AS TEXT) || '|' || amount::TEXT ORDER BY order_id
    )) AS restored_checksum
FROM restored_schema.orders;
-- Must match the baseline_checksum saved before migration
```

5. **Test partial rollback:** If the migration is phased (e.g. customers migrated first, then orders), test that rolling back only the most recent phase leaves the system in a consistent state.

---

## SECTION 6 — Data Pipelines and Streaming

---

**Q21: What is a data pipeline? How is it different from batch ETL?**

**A:** The term "data pipeline" is often used broadly to mean any automated process that moves data from one place to another. In modern usage, it usually refers to event-driven or streaming architectures, as opposed to traditional batch ETL.

| | Batch ETL | Streaming Data Pipeline |
|---|---|---|
| **Trigger** | Scheduled (e.g. run every night at 2 AM) | Event-driven (processes data as it arrives, continuously) |
| **Latency** | Hours (data is stale until the next batch run) | Seconds to milliseconds (near real-time) |
| **Volume per run** | Large bulk (millions of rows) | Small units (individual events) |
| **Typical tools** | Informatica, Talend, Apache Spark, dbt | Apache Kafka, Apache Flink, AWS Kinesis |
| **Error handling** | Retry the failed batch | Dead letter queue for failed events |
| **Use case** | Nightly warehouse load, monthly reporting | Real-time fraud detection, live dashboards, event sourcing |

Both require QA testing — the testing approach differs because streaming pipelines must also be tested for ordering, idempotency, latency, and failure recovery in ways that batch ETL does not require.

---

**Q22: What is a Kafka topic? How do you test message ordering?**

**A:** Apache Kafka is a distributed event streaming platform. A Kafka topic is a named, ordered, durable log of events. Producers publish events to a topic; consumers read events from a topic. Topics are divided into partitions, and Kafka guarantees ordering within a single partition.

**How to test message ordering:**

1. **Setup:** Publish 10 events to a single Kafka topic partition in a known order (event IDs 1 through 10 with timestamps T1 through T10).
2. **Verify:** Query the consumer's output (the target database or downstream system) and confirm:
   - All 10 events are present.
   - They are processed in the correct order (event 1 before event 2, etc.) if ordering matters for the business logic.
3. **Test out-of-order handling:** If the system uses multiple partitions (which does not guarantee cross-partition ordering), publish events and verify the consumer handles out-of-order arrival gracefully — no incorrect aggregation, no data corruption.
4. **Test late-arriving events:** Publish an event with an old timestamp after newer events have already been processed. Does the consumer update historical aggregates correctly, or does it silently drop the late event?

**Note:** Kafka guarantees ordering WITHIN a partition. If your topic has multiple partitions and ordering across them matters, that is a consumer-side responsibility, not Kafka's. Test that the consumer handles this correctly.

---

**Q23: What is a dead letter queue? How do you test it?**

**A:** A dead letter queue (DLQ) is a separate destination where events (or messages) are sent when the consumer fails to process them after a configured number of retry attempts. The DLQ prevents a single bad event from blocking the entire pipeline — the pipeline moves on, and the failed event is stored for investigation and manual reprocessing.

**How to test the DLQ:**

1. **Publish a poison pill message:** Create an event that will always fail processing — for example, a message with a required field missing, an invalid JSON format, or a value that violates a database constraint.

2. **Verify retry behaviour:** Confirm that the consumer retries the event N times (as configured) before giving up. Monitor the consumer logs during the test.

3. **Verify DLQ routing:** After N retries, confirm the event appears in the dead letter queue (a separate Kafka topic, an SQS DLQ, a database error table — depending on the implementation).

4. **Verify DLQ record content:** The DLQ record should contain:
   - The original message content
   - The error type and error message explaining why it failed
   - The number of attempts made
   - The timestamp of each attempt

5. **Verify pipeline continues:** Confirm that while the poison pill event is being retried and routed to the DLQ, all other events in the topic continue to be processed normally. A well-designed pipeline is non-blocking.

6. **Verify DLQ monitoring:** Confirm that an alert fires (Slack notification, PagerDuty, CloudWatch alarm) when a message lands in the DLQ. A DLQ that fills silently is as bad as no DLQ.

---

**Q24: What is data pipeline latency testing?**

**A:** Latency testing for data pipelines verifies that events travel from source to target within the agreed SLA. For a streaming pipeline, this might be "all orders must appear in the analytics system within 5 seconds of being placed." For a batch pipeline, it might be "all transactions from yesterday must be in the warehouse by 6 AM."

**How to test streaming latency:**

```
1. Record T1 = timestamp when the event is created in the source system
2. Publish the event to Kafka
3. Poll the target system (e.g. query the database every 500ms)
4. Record T2 = timestamp when the event first appears in the target
5. Calculate latency = T2 - T1
6. Assert latency < SLA threshold (e.g. 5,000ms)
```

**SQL to measure latency from pipeline metadata:**

```sql
-- Find events that exceeded the 5-second SLA
SELECT
    event_id,
    source_event_timestamp,
    target_received_timestamp,
    EXTRACT(EPOCH FROM (target_received_timestamp - source_event_timestamp)) AS latency_seconds
FROM target_schema.events
WHERE EXTRACT(EPOCH FROM (target_received_timestamp - source_event_timestamp)) > 5
ORDER BY latency_seconds DESC
LIMIT 100;
```

**Also test latency under load:** A single event may process in 200ms, but 1,000 events per second may cause queue backup and 30-second latency. Test with realistic load volumes.

---

## SECTION 7 — Data Quality Tools

---

**Q25: What are the 6 standard data quality dimensions?**

**A:** The six standard data quality dimensions are a framework for systematically evaluating whether data is fit for use. In a senior QA interview, being able to name, define, and test all six distinguishes you from a QA who only does count checks.

| Dimension | Definition | Key Test |
|-----------|-----------|----------|
| **Completeness** | All expected data is present — no missing rows, no NULL mandatory fields | Row count check; NULL check on required fields |
| **Accuracy** | Values in target correctly represent the real-world data from source | Field-by-field JOIN comparison; verify calculated fields independently |
| **Consistency** | Same data referenced in multiple places agrees | Cross-system queries; compare aggregates across related tables |
| **Timeliness** | Data arrives within the agreed SLA window | Measure source-to-target latency; batch arrival time check |
| **Validity** | Values conform to expected domain, format, and range | Format checks; range checks; enum value checks |
| **Uniqueness** | No unintended duplicate records | `GROUP BY id HAVING COUNT(*) > 1` |

**In an interview:** If asked to name the dimensions, give all six. If asked which is most important, explain that it depends on the use case — for financial reporting, Accuracy is critical; for fraud detection, Timeliness is paramount; for regulatory compliance, Completeness is often the first thing audited.

---

**Q26: What is Great Expectations? How does it work?**

**A:** Great Expectations (GX) is an open-source Python framework for defining, documenting, and validating data quality rules — called "expectations." Instead of writing ad hoc SQL checks scattered across test scripts, you define expectations as code, run them against your data, and get a visual HTML report showing which expectations passed and which failed.

**Key concepts:**
- **Expectation:** A declarative assertion about your data (`expect_column_values_to_not_be_null`, `expect_column_values_to_be_unique`, etc.)
- **Expectation Suite:** A named collection of expectations for a dataset
- **Validator:** Runs the expectations against a batch of data
- **Data Docs:** Auto-generated HTML documentation and validation reports

```python
import great_expectations as gx

context = gx.get_context()

# Connect to your data
datasource = context.sources.add_pandas_filesystem(
    name="orders_datasource",
    base_directory="./data"
)

# Define expectations
suite = context.add_expectation_suite("orders_validation_suite")
validator = context.get_validator(
    batch_request=...,
    expectation_suite_name="orders_validation_suite"
)

# Add expectations
validator.expect_column_values_to_not_be_null("order_id")
validator.expect_column_values_to_be_unique("order_id")
validator.expect_column_values_to_be_between("amount", min_value=0.01, max_value=100000)
validator.expect_column_values_to_be_in_set(
    "status",
    ["PENDING", "ACTIVE", "SHIPPED", "DELIVERED", "CANCELLED"]
)
validator.expect_column_values_to_match_regex(
    "email",
    r"^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$"
)
validator.save_expectation_suite()

# Run validation
results = validator.validate()
# GX generates a visual HTML report at data_docs/
print(f"Passed: {results.success}")
print(f"Statistics: {results.statistics}")
```

**Why it matters for QA interviews:** GX is increasingly used in modern data teams as the standard for codified data quality. Being able to describe it — even at this level — demonstrates awareness of the professional tooling landscape beyond raw SQL.

---

**Q27: What is dbt testing?**

**A:** dbt (data build tool) is a tool that allows analysts and data engineers to transform data inside the warehouse using SQL. It includes a built-in test framework that runs assertions against the transformed data as part of the dbt run.

**Built-in dbt tests (schema tests):**

```yaml
# models/schema.yml
models:
  - name: orders
    columns:
      - name: order_id
        tests:
          - not_null        # order_id must never be NULL
          - unique          # order_id must be unique across all rows
      - name: status
        tests:
          - accepted_values:
              values: ['PENDING', 'ACTIVE', 'SHIPPED', 'DELIVERED', 'CANCELLED']
      - name: customer_id
        tests:
          - relationships:
              to: ref('customers')
              field: customer_id  # referential integrity check
```

**Custom dbt test (singular test):**

```sql
-- tests/assert_revenue_positive.sql
-- If this query returns ANY rows, the test FAILS
SELECT order_id
FROM {{ ref('orders') }}
WHERE amount <= 0
```

**Run dbt tests:**

```bash
dbt test                           # run all tests
dbt test --select orders           # run tests for the orders model only
dbt test --select test_type:generic  # run only schema tests
```

dbt generates a JSON result file and a web interface showing which tests passed and failed, with the exact SQL that was run.

---

## SECTION 8 — Test Data Management

---

**Q28: How do you handle PII data in test environments?**

**A:** PII (Personally Identifiable Information) — names, email addresses, dates of birth, national ID numbers, payment details, health data — must never be copied in raw form to test environments. The reasons are: regulatory (GDPR, HIPAA, PCI-DSS), contractual (vendor agreements), and ethical (protecting individuals).

**The correct approaches:**

**Option 1 — Generate synthetic test data (preferred):**
Use a library like Faker to generate realistic-looking but entirely fake data. No real PII exists at all.

```python
from faker import Faker
fake = Faker()

def generate_test_customer():
    return {
        "customer_id": fake.uuid4(),
        "first_name":  fake.first_name(),
        "last_name":   fake.last_name(),
        "email":       fake.email(),
        "dob":         fake.date_of_birth(minimum_age=18, maximum_age=80).isoformat(),
        "phone":       fake.phone_number()
    }
```

**Option 2 — Mask production data:**
If you need production data structure and volume, mask the PII fields before copying to the test environment:

```sql
-- Mask PII before inserting into test environment
INSERT INTO test_env.customers
SELECT
    customer_id,                                               -- keep ID (needed for FK)
    'User_' || SUBSTRING(customer_id::TEXT, 1, 8) AS first_name,  -- pseudonymise
    'Tester' AS last_name,
    'user_' || customer_id || '@test.invalid' AS email,        -- unique but fake
    '1990-01-01' AS dob,                                       -- fixed placeholder
    country_code                                               -- not PII — keep
FROM prod_env.customers;
```

**Policy rules:**
- All data access must be governed by policy — who can query the production database vs the test database
- Masked data should be treated as production-equivalent from a security classification perspective
- Never put test data back into production
- Add a `test.invalid` TLD to email addresses — this domain is reserved and cannot receive real email

---

**Q29: What is test data masking?**

**A:** Test data masking (also called data obfuscation or anonymisation) is the process of replacing sensitive production data with realistic but fictitious values so that the test environment contains data that is functionally representative but does not expose real PII.

**Masking techniques:**

| Technique | Description | Example |
|-----------|-------------|---------|
| **Pseudonymisation** | Replace with a fake but realistic value | `John Smith` → `Robert Taylor` |
| **Tokenisation** | Replace with a non-reversible token | `4111 1111 1111 1111` → `TOK-8a3f9c2d` |
| **Shuffling** | Randomly redistribute values within the column | Mix real names within the column |
| **Redaction** | Replace with a fixed placeholder | `jane@company.com` → `REDACTED@test.invalid` |
| **Format-preserving masking** | Replace with fake data of the same format | `07700 900123` → `07700 900456` |
| **Nulling** | Set the field to NULL | Health data not needed for the test → NULL |

**When masking is not enough:** Some data cannot be safely anonymised without removing its utility — for example, aggregate financial figures that would change the nature of the test. In these cases, generate fully synthetic data that mirrors the distribution of the real data.

---

**Q30: How do you set up and tear down database test data?**

**A:** Proper setup and teardown ensures tests are isolated (not affected by other tests' data), repeatable (same result every run), and do not leave data pollution in the test environment.

**Setup strategies:**

```python
import pytest
import psycopg2

@pytest.fixture(scope="function")
def db_connection():
    conn = psycopg2.connect(dsn=os.environ["TEST_DATABASE_URL"])
    yield conn
    conn.close()

@pytest.fixture(scope="function")
def test_order(db_connection):
    """Creates a test order and cleans it up after the test."""
    cursor = db_connection.cursor()

    # SETUP: insert test record
    cursor.execute("""
        INSERT INTO orders (order_id, customer_id, amount, status)
        VALUES ('TEST-ORDER-001', 'TEST-CUST-001', 99.99, 'PENDING')
    """)
    db_connection.commit()

    yield "TEST-ORDER-001"  # provide the order ID to the test

    # TEARDOWN: always runs, even if the test fails
    cursor.execute("DELETE FROM orders WHERE order_id LIKE 'TEST-%'")
    db_connection.commit()
    cursor.close()
```

**Prefix test data:** Always prefix test record IDs with `TEST-` or a test-specific prefix. This makes teardown safe — `DELETE FROM orders WHERE order_id LIKE 'TEST-%'` — without risking deletion of real data.

**Database snapshots:** For complex setups, restore a known-good database snapshot before each test suite. Faster for large setups than individual INSERT/DELETE cycles.

**Transactions:** Wrap each test in a database transaction that is rolled back after the test. The test sees the data during the test, but nothing is committed. This is the fastest teardown approach and guarantees perfect isolation.

```python
@pytest.fixture(scope="function")
def db_transaction(db_connection):
    """Each test runs inside a transaction that is always rolled back."""
    db_connection.autocommit = False
    yield db_connection
    db_connection.rollback()  # nothing is ever committed to the database
```

---

## SECTION 9 — Interview Q&A

---

**Q31: What are the different types of ETL testing?**

**A:** ETL testing covers several distinct types, each targeting a different potential failure:

- **Data completeness testing** — verifying all records arrived (row count checks, NULL checks on mandatory fields)
- **Data accuracy testing** — verifying field values are correct (source vs target field-by-field comparison)
- **Data transformation testing** — verifying each business rule produces the correct output for all input variants including edge cases
- **Data integrity testing** — referential integrity holds; no orphaned child records; foreign keys point to existing parents
- **Data uniqueness testing** — no duplicate records where uniqueness is required
- **Data validity testing** — values conform to format, range, and allowed set constraints
- **Error handling testing** — rejected records appear in the error log; the pipeline does not silently discard data
- **Performance testing** — pipeline completes within the time SLA

In an interview, name all types and briefly explain each. Giving just "completeness and accuracy" suggests surface-level knowledge.

---

**Q32: How do you verify a data migration was successful?**

**A:** I use a structured five-point verification:

1. **Count verification:** Total record count in the target matches the source count (source count = migrated + excluded + rejected, with zero unexplained difference).

2. **Field-by-field accuracy:** Run a JOIN between source and target for a sample of records. Every field in every sampled record must match the expected transformed value.

3. **Referential integrity:** All foreign key relationships hold. No orphaned records — every child has a parent.

4. **Business-critical aggregate verification:** Re-calculate key financial figures (total revenue, order count by date) from both source and target. They must agree.

5. **User acceptance verification:** A business stakeholder looks up specific known records in the new system and confirms the data looks correct from a business perspective — not just from a technical SQL perspective.

The output is a signed reconciliation report accounting for every source record, with a zero unexplained difference.

---

**Q33: What is the difference between data completeness and data accuracy?**

**A:** They are distinct and independent data quality dimensions.

**Completeness** is about whether data EXISTS — are all the rows present? Are all mandatory fields populated? You test completeness with a row count check and a NULL check on required fields.

**Accuracy** is about whether the VALUES are correct — do they reflect the real-world truth? Do the transformed values match what the transformation rules specify?

**The key distinction:** You can have 100% completeness and 0% accuracy. Every row is present in the target (completeness passes), but the `amount` field on every row is wrong because of a rounding bug in the transform (accuracy fails). Alternatively, you can have 100% accuracy on the rows that landed, but 50% completeness because only half the source records were extracted.

Both dimensions must be verified independently. Passing a count check does not tell you anything about whether the values are correct.

---

**Q34: Write a SQL query to find duplicate records.**

**A:**

```sql
-- Step 1: Find the IDs that are duplicated
SELECT order_id, COUNT(*) AS occurrences
FROM orders_warehouse
GROUP BY order_id
HAVING COUNT(*) > 1
ORDER BY occurrences DESC;

-- Step 2: Retrieve the actual duplicate rows for investigation
SELECT *
FROM orders_warehouse
WHERE order_id IN (
    SELECT order_id
    FROM orders_warehouse
    GROUP BY order_id
    HAVING COUNT(*) > 1
)
ORDER BY order_id, created_at;

-- Step 3: For a composite key (when there is no single primary key column)
SELECT customer_id, order_date, amount, COUNT(*) AS occurrences
FROM orders_warehouse
GROUP BY customer_id, order_date, amount
HAVING COUNT(*) > 1;
```

To remove duplicates, keep only the latest version of each:

```sql
-- Delete all but the most recent duplicate (PostgreSQL)
DELETE FROM orders_warehouse
WHERE ctid NOT IN (
    SELECT MIN(ctid)
    FROM orders_warehouse
    GROUP BY order_id
);
```

---

**Q35: What should you check after a data pipeline runs?**

**A:** After a pipeline run, my checklist covers eight areas:

1. **Source count vs target count** — do they match or is the difference documented?
2. **Error log / dead letter queue** — what records were rejected and why? Is the count of rejections expected?
3. **Field-by-field accuracy sample** — pick 50–100 records, compare source vs target field by field
4. **Referential integrity** — any orphaned records introduced?
5. **NULL check on mandatory fields** — any mandatory fields came through as NULL?
6. **Uniqueness** — any duplicate records introduced?
7. **Pipeline SLA** — did the pipeline finish within the time window? Is the data available to consumers by the agreed time?
8. **Aggregate sanity check** — does the sum of order amounts today fall within the expected range? Any anomalies (e.g. total revenue today is $0 or $1 billion when the normal range is $1–10 million)?

---

**Q36: How do you test a transformation rule?**

**A:** I test a transformation rule with a three-step process: design test cases, seed the data, verify the output.

**Example rule:** "Normalise country codes — map 'UK' and 'GB' to 'GBR'. Map 'US' and 'USA' to 'USA'. Any unrecognised code should be set to NULL and logged."

**Step 1 — Design test cases:**
- `UK` → expect `GBR`
- `GB` → expect `GBR`
- `US` → expect `USA`
- `USA` → expect `USA`
- `XX` (unknown) → expect `NULL` in target + error log entry
- `NULL` (null input) → expect `NULL` in target
- `uk` (lowercase) → expect `GBR` (case-insensitive normalisation) or `NULL` if case matters

**Step 2 — Seed the test data** with one record per case in the source.

**Step 3 — Run the pipeline and verify:**

```sql
SELECT
    customer_id,
    source_country_code,
    target_country_code,
    CASE
        WHEN source_country_code IN ('UK','GB') AND target_country_code = 'GBR'  THEN 'PASS'
        WHEN source_country_code IN ('US','USA') AND target_country_code = 'USA'  THEN 'PASS'
        WHEN source_country_code = 'XX' AND target_country_code IS NULL           THEN 'PASS'
        WHEN source_country_code IS NULL AND target_country_code IS NULL          THEN 'PASS'
        ELSE 'FAIL'
    END AS test_result
FROM test_verification_view
WHERE customer_id IN ('TEST-C001','TEST-C002','TEST-C003','TEST-C004','TEST-C005','TEST-C006');
```

All rows should show `PASS`. Any `FAIL` identifies the exact case that is broken.

---

**Q37: What is Great Expectations and how does it differ from writing raw SQL checks?**

**A:** Great Expectations (GX) is a Python framework for defining data quality rules as code — called expectations. The key differences from raw SQL:

| Aspect | Raw SQL checks | Great Expectations |
|--------|---------------|-------------------|
| **Documentation** | SQL queries in scripts, not self-documenting | Expectations are human-readable and auto-generate documentation |
| **Reusability** | SQL is often duplicated across scripts | Expectation suites are versioned and reusable |
| **Reporting** | You write your own pass/fail logic | GX auto-generates visual HTML reports |
| **Integration** | Manual integration with CI/CD | Built-in connectors for Airflow, dbt, Spark, Pandas |
| **Coverage tracking** | No central view of what is checked | Data Docs show all expectations and their last pass/fail status |

GX is used when the team wants to codify and maintain data quality rules as a living part of the codebase — not as one-off SQL scripts that go stale. It is common in data engineering teams that treat data quality as a first-class concern, equivalent to unit tests in software engineering.

---

**Q38: What is dbt testing and when do you use it?**

**A:** dbt (data build tool) is used to transform data inside a data warehouse using SQL. Its built-in test framework allows data quality assertions to be defined in YAML configuration files alongside the transformation SQL.

You use dbt testing when:
- Your transformations are already written in dbt models
- You want automated data quality checks to run as part of every dbt run (CI/CD pipeline)
- You want referential integrity checks across models (`relationships` test)
- You want to enforce uniqueness and not-null constraints as code rather than relying on database constraints

**Built-in tests:** `not_null`, `unique`, `accepted_values`, `relationships` (referential integrity)
**Custom tests:** SQL files in the `tests/` directory that return rows when the test fails (zero rows = pass)

```bash
dbt test                     # run all tests against all models
dbt test --select orders     # run only tests for the orders model
```

dbt testing is valuable because it makes data quality checks part of the transformation pipeline itself — the check runs automatically every time the model is rebuilt, with no separate test execution step required.

---

**Q39: How do you find records that are in the source but missing from the target? And records in the target that should not be there?**

**A:** Both are detected with LEFT JOIN queries:

```sql
-- Records in source that are MISSING from target (data loss)
SELECT
    s.order_id,
    s.customer_id,
    s.amount,
    s.status,
    'MISSING FROM TARGET' AS finding
FROM source_schema.orders s
LEFT JOIN target_schema.orders_warehouse t ON s.order_id = t.order_id
WHERE t.order_id IS NULL;

-- Records in target that have NO MATCHING source (phantom records)
SELECT
    t.order_id,
    t.customer_id,
    t.amount,
    t.status,
    'PHANTOM IN TARGET' AS finding
FROM target_schema.orders_warehouse t
LEFT JOIN source_schema.orders s ON t.order_id = s.order_id
WHERE s.order_id IS NULL;
```

**What these findings indicate:**

- **Missing from target:** A record was in the source but did not arrive in the target. Could mean: a filter dropped it unexpectedly, a transform exception caused it to be silently skipped, a load error was not retried. This record should be in the error log.

- **Phantom in target:** A record is in the target with no matching source. Could mean: the pipeline ran twice and inserted duplicates, the wrong source was used, a previous migration's data was not cleaned up. This is a uniqueness/accuracy failure.

---

**Q40: How do you test pipeline failure recovery?**

**A:** Pipeline failure recovery testing verifies that the pipeline can survive partial failures without data loss, duplication, or corruption. I test it by deliberately triggering failures:

1. **Stop the consumer mid-run:** Publish 100 events to Kafka. After 50 are consumed, kill the consumer process. Restart it. Verify: all 100 events are eventually processed, the first 50 are not processed twice (idempotent consumer), the last 50 are processed correctly.

2. **Publish a poison pill message:** Submit a malformed event that will always fail deserialisation or violate a business rule. Verify: after N retries (as configured), the event goes to the DLQ. Verify: all other events continue to process normally — the poison pill does not block the pipeline.

3. **Database timeout during load:** Simulate a database connection timeout or slow query during the load phase. Verify: the pipeline retries the load, eventually succeeds, and does not create partial or duplicate records.

4. **Full process restart with offset tracking:** Kill the pipeline process completely (as if the server crashed). Restart it. Verify: it resumes from the last committed Kafka offset or checkpoint — it does not reprocess events from the beginning (which would cause duplicates) and does not skip any unprocessed events.

The key properties being tested are: **idempotency** (processing the same event twice produces the same result as processing it once) and **exactly-once or at-least-once with deduplication** delivery semantics.

---

**Q41: What is the aggregation validation approach for ETL testing?**

**A:** Aggregation validation is a high-level sanity check that verifies that financial or statistical totals computed from the source match the same totals computed from the target. It does not check individual records row-by-row — it checks whether the pipeline preserved the correct aggregate outcome.

**When to use it:** After verifying counts and doing a sample accuracy check, aggregate validation gives confidence that there are no systematic errors affecting a category of records (e.g. all orders from one region have wrong amounts).

```sql
-- Pre-pipeline: compute aggregates from source
SELECT
    DATE_TRUNC('month', order_date)  AS month,
    SUM(amount)                       AS total_revenue,
    COUNT(*)                          AS order_count,
    AVG(amount)                       AS avg_order_value,
    MAX(amount)                       AS max_order_value
FROM source_schema.orders
WHERE order_date >= '2024-01-01' AND order_date < '2024-02-01'
GROUP BY DATE_TRUNC('month', order_date);

-- Post-pipeline: same computation from target (should match exactly)
SELECT
    DATE_TRUNC('month', source_order_date)  AS month,
    SUM(amount)                              AS total_revenue,
    COUNT(*)                                 AS order_count,
    AVG(amount)                              AS avg_order_value,
    MAX(amount)                              AS max_order_value
FROM target_schema.orders_warehouse
WHERE source_order_date >= '2024-01-01' AND source_order_date < '2024-02-01'
GROUP BY DATE_TRUNC('month', source_order_date);

-- Compare the two result sets — they should be identical
-- Any difference indicates: rounding errors, missing records affecting totals,
-- duplicate records inflating sums, or incorrect date field mapping
```

**Note:** Aggregate validation is the check that most closely resembles what a business user would do — compare the report from the new system to the report from the old system. If these match, the migration is correct from a business perspective.

---

*End of ETL & Data Pipeline Testing Q&A Guide — 41 questions covering ETL fundamentals, all 6 data quality dimensions, 7 SQL validation queries, migration testing, streaming pipelines, Kafka, dead letter queues, Great Expectations, dbt, PII handling, and test data management.*
