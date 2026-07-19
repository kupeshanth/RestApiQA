# SQL & Database Testing — Complete Guide | QA Backend Verification

> Senior QA Engineer Interview Reference — SQL Fundamentals, QA Scenarios, DB Testing Types, and Interview Q&A

---

## Table of Contents

1. [Why QA Engineers Need SQL](#1-why-qa-engineers-need-sql)
2. [Core SQL — SELECT and Filtering](#2-core-sql--select-and-filtering)
3. [JOINs — Combining Tables](#3-joins--combining-tables)
4. [Aggregates — GROUP BY and HAVING](#4-aggregates--group-by-and-having)
5. [NULL Handling](#5-null-handling)
6. [String Functions](#6-string-functions)
7. [Date and Time Functions](#7-date-and-time-functions)
8. [Subqueries and IN / NOT IN](#8-subqueries-and-in--not-in)
9. [INSERT, UPDATE, DELETE — Test Data Management](#9-insert-update-delete--test-data-management)
10. [TRUNCATE vs DELETE](#10-truncate-vs-delete)
11. [Real QA SQL Scenarios — 10 Practical Examples](#11-real-qa-sql-scenarios--10-practical-examples)
12. [Database Testing Types](#12-database-testing-types)
13. [ACID Properties](#13-acid-properties)
14. [Test Data Management — Setup and Teardown](#14-test-data-management--setup-and-teardown)
15. [Connecting to a Database from Java Tests (JDBC)](#15-connecting-to-a-database-from-java-tests-jdbc)
16. [Senior Interview Q&A — 10 Questions with Full Answers](#16-senior-interview-qa--10-questions-with-full-answers)

---

## 1. Why QA Engineers Need SQL

### The QA Rationale for SQL

When you test a REST API, the response you receive is only part of the story. The real question is: **did the correct data end up in the database?**

An API can return HTTP 201 Created with a beautiful JSON body, but the data might be:
- Stored in the wrong column.
- Missing a required field.
- Saved with an incorrect value (e.g., rounding error on price).
- Linked to the wrong parent record (wrong foreign key).
- Partially written (only some columns updated).
- Soft-deleted incorrectly (row removed instead of flagged).

SQL gives QA engineers the ability to **verify the ground truth** — what actually exists in the database — independent of what the API claims.

### Key QA Use Cases

| Use Case | Why SQL is Needed |
|---|---|
| Verify POST /users API | Confirm the user row was inserted with correct values |
| Verify PATCH /orders/{id} | Confirm only the changed fields were updated |
| Validate data integrity | Check foreign keys, no orphaned records |
| Verify soft delete | Confirm `deleted_at` was set, row was not physically removed |
| Validate pagination | Confirm the correct page of records is returned |
| Audit trail verification | Confirm audit_log table has the expected entry |
| Test data setup | INSERT test records before a test |
| Test data teardown | DELETE or ROLLBACK test records after a test |
| Validate migrations | Confirm all existing data was correctly transformed |
| Find duplicate data | Check for constraint violations or bad data seeding |

---

## 2. Core SQL — SELECT and Filtering

### Basic SELECT

```sql
-- Select all columns from a table
SELECT * FROM users;

-- Select specific columns
SELECT id, name, email, created_at FROM users;

-- Give columns aliases
SELECT id AS user_id, name AS full_name, email FROM users;
```

### WHERE Clause

```sql
-- Single condition
SELECT * FROM users WHERE status = 'active';
SELECT * FROM orders WHERE total_amount > 100;

-- AND — both conditions must be true
SELECT * FROM users WHERE status = 'active' AND role = 'admin';

-- OR — either condition must be true
SELECT * FROM orders WHERE status = 'pending' OR status = 'processing';

-- NOT — negate a condition
SELECT * FROM users WHERE NOT status = 'deleted';
SELECT * FROM orders WHERE NOT (status = 'cancelled' OR status = 'refunded');

-- IN — match any value in a list
SELECT * FROM orders WHERE status IN ('pending', 'processing', 'shipped');

-- NOT IN — exclude values in a list
SELECT * FROM users WHERE role NOT IN ('admin', 'superuser');

-- BETWEEN — range check (inclusive)
SELECT * FROM orders WHERE total_amount BETWEEN 10.00 AND 100.00;
SELECT * FROM orders WHERE created_at BETWEEN '2024-01-01' AND '2024-01-31';
```

### ORDER BY

```sql
-- Ascending (default)
SELECT * FROM users ORDER BY name ASC;

-- Descending
SELECT * FROM orders ORDER BY created_at DESC;

-- Multiple columns
SELECT * FROM orders ORDER BY status ASC, created_at DESC;
```

### LIMIT and OFFSET

```sql
-- First 10 rows
SELECT * FROM users LIMIT 10;

-- Page 2 (10 per page): skip first 10, get next 10
SELECT * FROM users ORDER BY id LIMIT 10 OFFSET 10;

-- Page 3
SELECT * FROM users ORDER BY id LIMIT 10 OFFSET 20;
```

---

## 3. JOINs — Combining Tables

### Visual Explanation of JOIN Types

```
Table A (users)          Table B (orders)
id | name                id | user_id | amount
-----------               -----------------
1  | Alice               1  | 1       | 50.00
2  | Bob                 2  | 1       | 30.00
3  | Carol               3  | 4       | 20.00 ← user_id 4 doesn't exist in users
                         4  | 2       | 15.00
```

---

### INNER JOIN — Matching Rows Only

Returns rows where there is a match in **both** tables.

```sql
SELECT u.id, u.name, o.id AS order_id, o.amount
FROM users u
INNER JOIN orders o ON u.id = o.user_id;
```

**Result**: Alice (2 orders), Bob (1 order). Carol has no orders, order #3 has no user — both are excluded.

**QA Use Case**: "Show me all users who have placed at least one order." If a user appears in the result, they have matching orders.

---

### LEFT JOIN (LEFT OUTER JOIN) — All Left Rows + Matching Right

Returns **all rows from the left table**, with matching rows from the right table. Non-matching right rows are NULL.

```sql
SELECT u.id, u.name, o.id AS order_id, o.amount
FROM users u
LEFT JOIN orders o ON u.id = o.user_id;
```

**Result**: Alice (2 orders), Bob (1 order), Carol (order_id = NULL, amount = NULL). Carol is included even though she has no orders.

**QA Use Case**: "Find users who have never placed an order" — filter on `o.id IS NULL`:

```sql
SELECT u.id, u.name
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE o.id IS NULL;
-- Returns: Carol (3 | Carol)
```

---

### RIGHT JOIN (RIGHT OUTER JOIN) — All Right Rows + Matching Left

Returns **all rows from the right table**, with matching rows from the left table. Non-matching left rows are NULL.

```sql
SELECT u.id, u.name, o.id AS order_id, o.amount
FROM users u
RIGHT JOIN orders o ON u.id = o.user_id;
```

**Result**: Alice's orders, Bob's order, and order #3 with user name = NULL.

**QA Use Case**: "Find orders that have no corresponding user (orphaned orders)" — filter on `u.id IS NULL`:

```sql
SELECT o.id AS order_id, o.amount, o.user_id
FROM users u
RIGHT JOIN orders o ON u.id = o.user_id
WHERE u.id IS NULL;
-- Returns: order #3 (user_id=4, but no user with id=4)
```

---

### FULL OUTER JOIN — All Rows from Both Tables

Returns all rows from both tables. NULLs appear where there is no match.

```sql
SELECT u.id AS user_id, u.name, o.id AS order_id
FROM users u
FULL OUTER JOIN orders o ON u.id = o.user_id;
-- Note: MySQL doesn't support FULL OUTER JOIN natively — use UNION of LEFT and RIGHT JOIN
```

---

### Multi-Table JOIN

```sql
SELECT
  u.name AS customer_name,
  o.id AS order_id,
  o.total_amount,
  p.name AS payment_method,
  s.status AS shipment_status
FROM orders o
INNER JOIN users u ON o.user_id = u.id
INNER JOIN payments p ON o.id = p.order_id
LEFT JOIN shipments s ON o.id = s.order_id
WHERE o.created_at >= CURDATE()
ORDER BY o.created_at DESC;
```

---

### JOIN Quick Reference

| JOIN Type | Returns | QA Use Case |
|---|---|---|
| INNER JOIN | Matching rows only | Verify relationship exists |
| LEFT JOIN | All left + matched right | Find records with no related record |
| RIGHT JOIN | All right + matched left | Find orphaned records in right table |
| FULL OUTER JOIN | All rows from both | Full reconciliation |
| SELF JOIN | Same table joined to itself | Hierarchical data, find duplicates |

---

## 4. Aggregates — GROUP BY and HAVING

### Aggregate Functions

```sql
COUNT(*)           -- Count all rows
COUNT(column)      -- Count non-NULL values
COUNT(DISTINCT column) -- Count distinct non-NULL values
SUM(column)        -- Sum of values
AVG(column)        -- Average
MAX(column)        -- Maximum value
MIN(column)        -- Minimum value
```

### GROUP BY

```sql
-- Count orders per status
SELECT status, COUNT(*) AS order_count
FROM orders
GROUP BY status;

-- Total revenue per user
SELECT user_id, SUM(total_amount) AS total_spent
FROM orders
WHERE status = 'completed'
GROUP BY user_id
ORDER BY total_spent DESC;

-- Orders per day
SELECT DATE(created_at) AS order_date, COUNT(*) AS orders_count
FROM orders
GROUP BY DATE(created_at)
ORDER BY order_date;
```

### HAVING — Filter Groups (WHERE applied after GROUP BY)

```sql
-- Users who have spent more than $500 total
SELECT user_id, SUM(total_amount) AS total_spent
FROM orders
GROUP BY user_id
HAVING SUM(total_amount) > 500;

-- Product categories with more than 100 orders
SELECT category, COUNT(*) AS order_count
FROM orders o
INNER JOIN products p ON o.product_id = p.id
GROUP BY category
HAVING COUNT(*) > 100;

-- Find email addresses with duplicates (more than 1 occurrence)
SELECT email, COUNT(*) AS count
FROM users
GROUP BY email
HAVING COUNT(*) > 1;
```

**Key distinction**: `WHERE` filters rows **before** grouping. `HAVING` filters groups **after** grouping.

```sql
-- WHERE filters rows first, then GROUP BY groups, then HAVING filters groups
SELECT status, COUNT(*) AS count
FROM orders
WHERE created_at >= '2024-01-01'    -- filter rows first
GROUP BY status                      -- then group
HAVING COUNT(*) > 10;               -- then filter groups
```

---

## 5. NULL Handling

### IS NULL / IS NOT NULL

```sql
-- Find users with no phone number set
SELECT id, name, email FROM users WHERE phone IS NULL;

-- Find orders with a shipping date (not null)
SELECT id, status FROM orders WHERE shipped_at IS NOT NULL;

-- Wrong — this does NOT work for NULL comparison:
SELECT * FROM users WHERE phone = NULL;    -- ALWAYS returns 0 rows
SELECT * FROM users WHERE phone != NULL;  -- ALWAYS returns 0 rows
```

**Key rule**: NULL is not a value — it is the absence of a value. You cannot use `= NULL` or `!= NULL`. Always use `IS NULL` or `IS NOT NULL`.

---

### COALESCE — Return First Non-NULL Value

```sql
-- If discount is NULL, show 0
SELECT id, name, COALESCE(discount, 0) AS discount FROM products;

-- First non-null value from multiple columns
SELECT id, COALESCE(nickname, first_name, 'Unknown') AS display_name FROM users;

-- QA use: verify default values are applied
SELECT id, status, COALESCE(deleted_at, 'not deleted') AS deletion_status
FROM users;
```

---

### NULLIF — Returns NULL if Two Values Are Equal

```sql
-- Returns NULL if denominator is 0 (avoid division by zero)
SELECT NULLIF(0, 0)      -- returns NULL
SELECT NULLIF(5, 0)      -- returns 5

-- Safe division
SELECT total_sales / NULLIF(total_orders, 0) AS avg_order_value FROM summary;
```

---

## 6. String Functions

```sql
-- LIKE — pattern matching
SELECT * FROM users WHERE email LIKE '%@gmail.com';        -- ends with
SELECT * FROM users WHERE name LIKE 'A%';                  -- starts with A
SELECT * FROM users WHERE name LIKE '%Smith%';             -- contains
SELECT * FROM products WHERE sku LIKE 'PROD-___';          -- _ matches any single char

-- Case-insensitive search (MySQL default is case-insensitive; PostgreSQL use ILIKE)
SELECT * FROM users WHERE name ILIKE 'alice%';             -- PostgreSQL

-- Case conversion
SELECT UPPER(email) FROM users;          -- 'ALICE@EXAMPLE.COM'
SELECT LOWER(name) FROM users;           -- 'alice smith'

-- TRIM — remove leading/trailing whitespace
SELECT TRIM(' Alice ');                  -- 'Alice'
SELECT LTRIM(name) FROM users;          -- left trim
SELECT RTRIM(name) FROM users;          -- right trim

-- LENGTH / LEN
SELECT LENGTH(email) FROM users;        -- MySQL / PostgreSQL
SELECT LEN(email) FROM users;           -- SQL Server

-- CONCAT
SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users;
SELECT first_name || ' ' || last_name AS full_name FROM users;  -- PostgreSQL

-- SUBSTRING / SUBSTR
SELECT SUBSTRING(phone, 1, 3) AS area_code FROM users;   -- first 3 chars

-- REPLACE
SELECT REPLACE(phone, '-', '') AS clean_phone FROM users;

-- QA Use: Verify email format
SELECT id, email FROM users WHERE email NOT LIKE '%_@_%._%';
-- Finds emails that don't match basic pattern (missing @, missing domain, etc.)
```

---

## 7. Date and Time Functions

```sql
-- Current date and time
SELECT NOW();               -- '2024-01-15 14:30:00' (MySQL / PostgreSQL)
SELECT CURDATE();           -- '2024-01-15' (MySQL) / CURRENT_DATE (PostgreSQL)
SELECT CURTIME();           -- '14:30:00' (MySQL)
SELECT GETDATE();           -- SQL Server

-- Extract parts of a date
SELECT DATE(created_at) FROM orders;           -- extract date part only
SELECT YEAR(created_at) FROM orders;           -- 2024
SELECT MONTH(created_at) FROM orders;          -- 1
SELECT DAY(created_at) FROM orders;            -- 15

-- Date arithmetic
SELECT DATE_ADD(NOW(), INTERVAL 7 DAY);        -- 7 days from now (MySQL)
SELECT DATE_SUB(NOW(), INTERVAL 30 DAY);       -- 30 days ago (MySQL)
SELECT NOW() + INTERVAL '7 days';              -- PostgreSQL

-- DATEDIFF — difference between dates
SELECT DATEDIFF('2024-01-31', '2024-01-01');   -- 30 (MySQL — returns days)
SELECT DATEDIFF(day, '2024-01-01', '2024-01-31');  -- SQL Server

-- DATE_FORMAT (MySQL) / TO_CHAR (PostgreSQL)
SELECT DATE_FORMAT(created_at, '%Y-%m-%d') FROM orders;        -- '2024-01-15'
SELECT DATE_FORMAT(created_at, '%d/%m/%Y %H:%i') FROM orders;  -- '15/01/2024 14:30'
SELECT TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI:SS') FROM orders;  -- PostgreSQL

-- QA Use: Find records created in last 24 hours
SELECT * FROM transactions WHERE created_at >= NOW() - INTERVAL 1 DAY;   -- MySQL
SELECT * FROM transactions WHERE created_at >= NOW() - INTERVAL '1 day'; -- PostgreSQL

-- QA Use: Check records created today
SELECT * FROM orders WHERE DATE(created_at) = CURDATE();

-- QA Use: Find records older than 90 days (for data retention checks)
SELECT * FROM audit_logs WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

---

## 8. Subqueries and IN / NOT IN

### Subquery in WHERE

```sql
-- Find users who have placed at least one order
SELECT id, name, email FROM users
WHERE id IN (SELECT DISTINCT user_id FROM orders);

-- Find users who have NEVER placed an order
SELECT id, name, email FROM users
WHERE id NOT IN (SELECT DISTINCT user_id FROM orders WHERE user_id IS NOT NULL);
-- Note: NOT IN returns no rows if the subquery contains any NULL values — always add WHERE user_id IS NOT NULL
```

### Correlated Subquery

```sql
-- Find users whose most recent order was more than 6 months ago (lapsed users)
SELECT u.id, u.name, u.email
FROM users u
WHERE (
  SELECT MAX(created_at)
  FROM orders o
  WHERE o.user_id = u.id
) < DATE_SUB(NOW(), INTERVAL 6 MONTH);
```

### Subquery in FROM (Derived Table)

```sql
-- Average order count per user
SELECT AVG(order_count) AS avg_orders_per_user
FROM (
  SELECT user_id, COUNT(*) AS order_count
  FROM orders
  GROUP BY user_id
) AS user_order_counts;
```

### EXISTS vs IN

```sql
-- EXISTS — more efficient for large datasets, stops at first match
SELECT id, name FROM users u
WHERE EXISTS (
  SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.status = 'completed'
);

-- NOT EXISTS — find users with no completed orders
SELECT id, name FROM users u
WHERE NOT EXISTS (
  SELECT 1 FROM orders o WHERE o.user_id = u.id
);
```

---

## 9. INSERT, UPDATE, DELETE — Test Data Management

### INSERT

```sql
-- Insert a single row
INSERT INTO users (name, email, role, status, created_at)
VALUES ('QA Test User', 'qa_test_1@example.com', 'viewer', 'active', NOW());

-- Insert multiple rows at once
INSERT INTO users (name, email, role, status) VALUES
  ('Test User 1', 'test1@qa.com', 'viewer', 'active'),
  ('Test User 2', 'test2@qa.com', 'admin', 'active'),
  ('Test User 3', 'test3@qa.com', 'viewer', 'inactive');

-- Insert and retrieve the auto-generated ID
INSERT INTO users (name, email) VALUES ('New User', 'new@qa.com');
SELECT LAST_INSERT_ID();   -- MySQL
SELECT lastval();          -- PostgreSQL (after INSERT into sequence table)
```

### UPDATE

```sql
-- Update a single record
UPDATE users SET status = 'inactive' WHERE id = 101;

-- Update multiple columns
UPDATE orders
SET status = 'shipped', shipped_at = NOW(), tracking_number = 'TRK123'
WHERE id = 50;

-- Update with calculation
UPDATE products SET price = price * 0.9 WHERE category = 'Electronics';

-- IMPORTANT — Always include WHERE in UPDATE. Without it, all rows are updated.
```

### DELETE

```sql
-- Delete a specific record
DELETE FROM users WHERE id = 101;

-- Delete multiple records
DELETE FROM test_orders WHERE email LIKE '%@qa.com%';

-- Delete with JOIN (MySQL syntax)
DELETE u FROM users u
INNER JOIN test_sessions ts ON u.id = ts.user_id
WHERE ts.is_test = 1;

-- IMPORTANT — Always include WHERE in DELETE. Without it, all rows are deleted.
```

### ROLLBACK — Undo Test Data Changes

```sql
-- Wrap test data operations in a transaction so you can undo them
BEGIN;                        -- Start transaction (MySQL: START TRANSACTION)

INSERT INTO users (name, email) VALUES ('Temp Test User', 'temp@qa.com');
UPDATE orders SET status = 'cancelled' WHERE id = 999;

-- If test passed and you want to clean up:
ROLLBACK;   -- Undo all changes since BEGIN

-- If you want to keep the changes:
COMMIT;
```

---

## 10. TRUNCATE vs DELETE

| Aspect | TRUNCATE | DELETE |
|---|---|---|
| Speed | Much faster | Slower (row-by-row logging) |
| WHERE clause | Not supported | Supported |
| Rows affected | All rows removed | Specified rows (or all if no WHERE) |
| Transaction rollback | Not rollback-able in most DBs | Can be rolled back within a transaction |
| Resets AUTO_INCREMENT | Yes | No |
| Triggers | Does NOT fire row-level triggers | Fires DELETE triggers |
| Foreign Keys | Fails if FK constraints exist | Fails if FK constraints exist (unless cascaded) |
| Use Case | Clear entire table fast (test data reset) | Remove specific rows |

```sql
-- TRUNCATE — removes all rows, resets identity counter
TRUNCATE TABLE test_users;

-- DELETE without WHERE — removes all rows but is logged row by row
DELETE FROM test_users;

-- DELETE with WHERE — removes specific rows
DELETE FROM users WHERE email LIKE '%@qa.com%';
```

**QA Rule**: Use `TRUNCATE` to clear entire test/staging tables between test runs. Use `DELETE WHERE` to clean up specific test records created during a test.

---

## 11. Real QA SQL Scenarios — 10 Practical Examples

---

### Scenario 1: Verify a User Was Created After POST /users API Call

```sql
-- After calling POST /api/users with body { name: "Alice Test", email: "alice.test@example.com" }
-- Verify the row exists in the database with correct values

SELECT id, name, email, role, status, created_at
FROM users
WHERE email = 'alice.test@example.com'
  AND created_at >= NOW() - INTERVAL 1 MINUTE;

-- Expected result: 1 row returned with correct name, default role, active status
-- Failure: 0 rows = API returned 201 but didn't persist; or wrong email stored
```

---

### Scenario 2: Check All Orders Have a Corresponding User (No Orphaned Orders)

```sql
-- After data migration or seeding, verify referential integrity:
-- Every order.user_id must correspond to a valid user

SELECT o.id AS order_id, o.user_id, o.total_amount, o.status
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
WHERE u.id IS NULL;

-- Expected result: 0 rows (no orphaned orders)
-- Failure: Any rows returned = orders exist with no valid parent user
-- This indicates a foreign key constraint is missing or a bug in the delete flow
```

---

### Scenario 3: Verify Payment Amount in DB Matches What API Returned

```sql
-- After POST /api/orders returns { orderId: 12345, totalAmount: 99.99 }
-- Verify the database has the exact same amount

SELECT
  o.id,
  o.total_amount AS db_total,
  99.99 AS api_total,
  ABS(o.total_amount - 99.99) AS difference
FROM orders o
WHERE o.id = 12345;

-- Expected: difference = 0.00
-- Failure: difference > 0 = rounding error or wrong calculation in backend
-- Also check: tax, discount, and shipping breakdown columns
SELECT o.id, o.subtotal, o.tax_amount, o.discount_amount, o.total_amount
FROM orders o WHERE id = 12345;
```

---

### Scenario 4: Find Duplicate Email Addresses

```sql
-- After bulk user import or registration testing, check for duplicate emails
-- (email should be unique — UNIQUE constraint check)

SELECT email, COUNT(*) AS occurrence_count
FROM users
GROUP BY email
HAVING COUNT(*) > 1
ORDER BY occurrence_count DESC;

-- Expected: 0 rows (no duplicates)
-- Failure: Any rows = duplicate emails exist, UNIQUE constraint may be missing
-- or application is not validating uniqueness before INSERT
```

---

### Scenario 5: Count Orders Per Status to Verify API Filter Works

```sql
-- After testing GET /api/orders?status=pending returns 45 results
-- Verify the database actually has 45 pending orders

SELECT status, COUNT(*) AS count
FROM orders
GROUP BY status
ORDER BY count DESC;

-- Expected output (verify against API response totals):
-- pending    | 45
-- processing | 23
-- shipped    | 102
-- completed  | 890
-- cancelled  | 15

-- For a specific assertion:
SELECT COUNT(*) FROM orders WHERE status = 'pending';
-- Should return 45 to match the API response
```

---

### Scenario 6: Verify a Soft-Delete Sets deleted_at and Doesn't Remove the Row

```sql
-- After calling DELETE /api/users/101 (which is a soft delete in this system)
-- Verify the row still exists but deleted_at is now set

-- Check row still exists (NOT physically deleted)
SELECT id, name, email, status, deleted_at
FROM users
WHERE id = 101;

-- Expected:
-- id  | name  | email           | status   | deleted_at
-- 101 | Alice | alice@test.com  | inactive | 2024-01-15 14:30:22

-- Failure cases:
-- 0 rows returned = hard delete (physically removed) when it should be soft delete
-- deleted_at is NULL = soft delete flag not set
-- status not updated = partial update

-- Also verify the user no longer appears in active users list
SELECT COUNT(*) FROM users WHERE id = 101 AND deleted_at IS NULL;
-- Should return 0
```

---

### Scenario 7: Check Audit Log Was Created After Update

```sql
-- After PATCH /api/users/101 (updating name)
-- Verify the audit_log table captured the change

SELECT
  al.id,
  al.entity_type,
  al.entity_id,
  al.action,
  al.changed_by,
  al.old_value,
  al.new_value,
  al.created_at
FROM audit_logs al
WHERE al.entity_type = 'user'
  AND al.entity_id = 101
  AND al.action = 'UPDATE'
  AND al.created_at >= NOW() - INTERVAL 1 MINUTE
ORDER BY al.created_at DESC
LIMIT 1;

-- Expected: 1 row with old_value containing old name, new_value containing new name
-- Failure: 0 rows = audit logging not implemented or not triggered on update
-- Failure: old_value = new_value = values not captured correctly
```

---

### Scenario 8: Verify Pagination Is Returning Correct Page of Results

```sql
-- After calling GET /api/orders?page=2&limit=10 (returns 10 orders, page 2)
-- Verify the API is returning the correct 10 records (records 11–20)

-- First, get the 11th to 20th orders ordered the same way as the API
SELECT id, user_id, total_amount, created_at
FROM orders
ORDER BY created_at DESC
LIMIT 10 OFFSET 10;

-- Compare these IDs against the IDs returned by the API response
-- If API returns IDs [201, 200, 199, ...] and DB query returns same IDs = pagination is correct
-- Discrepancy = wrong ORDER BY in API, wrong OFFSET calculation, or missing sort stability

-- Also verify total count matches API's meta.totalCount
SELECT COUNT(*) AS total_orders FROM orders;
-- Should match the API's { "meta": { "totalCount": 892 } }
```

---

### Scenario 9: Find All Failed Transactions in the Last 24 Hours

```sql
-- QA monitoring query — find failed payment transactions in last 24 hours
-- Useful for investigating test failures or monitoring staging environment

SELECT
  t.id AS transaction_id,
  t.order_id,
  t.amount,
  t.payment_method,
  t.status,
  t.error_code,
  t.error_message,
  t.created_at,
  u.email AS user_email
FROM transactions t
INNER JOIN orders o ON t.order_id = o.id
INNER JOIN users u ON o.user_id = u.id
WHERE t.status = 'failed'
  AND t.created_at >= NOW() - INTERVAL 24 HOUR
ORDER BY t.created_at DESC;

-- Useful for:
-- Regression testing: compare failed count before and after deploy
-- Test verification: after triggering an intentional failure, confirm it appears here
-- Root cause: error_code and error_message reveal payment gateway error details
```

---

### Scenario 10: Check That a Discount Was Correctly Applied in DB

```sql
-- After applying a 20% discount coupon "SAVE20" to order 12345
-- Verify the discount was correctly stored and calculated

SELECT
  o.id AS order_id,
  o.subtotal,
  o.discount_amount,
  o.discount_code,
  o.total_amount,
  -- Verify the math: discount should be 20% of subtotal
  ROUND(o.subtotal * 0.20, 2) AS expected_discount,
  -- Verify total = subtotal - discount + tax
  ROUND(o.subtotal - o.discount_amount + o.tax_amount, 2) AS expected_total,
  -- Flag if calculation is wrong
  CASE
    WHEN ABS(o.discount_amount - ROUND(o.subtotal * 0.20, 2)) > 0.01
    THEN 'DISCOUNT_CALCULATION_ERROR'
    ELSE 'CORRECT'
  END AS discount_check,
  CASE
    WHEN ABS(o.total_amount - ROUND(o.subtotal - o.discount_amount + o.tax_amount, 2)) > 0.01
    THEN 'TOTAL_CALCULATION_ERROR'
    ELSE 'CORRECT'
  END AS total_check
FROM orders o
WHERE o.id = 12345;

-- Also verify the coupon usage was recorded
SELECT * FROM coupon_usages
WHERE coupon_code = 'SAVE20' AND order_id = 12345;
-- Expected: 1 row — confirms coupon was applied and logged
```

---

## 12. Database Testing Types

### Data Integrity Testing

Verifies that data stored in the database is accurate, consistent, and complete.

```sql
-- Check: No NULL in required columns (NOT NULL constraint)
SELECT COUNT(*) FROM users WHERE name IS NULL OR email IS NULL;
-- Expected: 0

-- Check: Email format is valid (basic)
SELECT id, email FROM users
WHERE email NOT REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$';
-- Expected: 0 rows

-- Check: Positive amounts only
SELECT COUNT(*) FROM orders WHERE total_amount <= 0;
-- Expected: 0

-- Check: Valid status values
SELECT DISTINCT status FROM orders;
-- Should only contain expected values: pending, processing, shipped, completed, cancelled
```

---

### Referential Integrity Testing

Verifies that foreign key relationships are correctly maintained.

```sql
-- Orphaned orders (no parent user)
SELECT COUNT(*) FROM orders o
LEFT JOIN users u ON o.user_id = u.id
WHERE u.id IS NULL;
-- Expected: 0

-- Orphaned order_items (no parent order)
SELECT COUNT(*) FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.id
WHERE o.id IS NULL;
-- Expected: 0

-- Check cascade delete worked correctly
-- After deleting user 101, verify their orders are also deleted (or soft-deleted)
SELECT COUNT(*) FROM orders WHERE user_id = 101;
-- Depends on cascade policy: 0 if cascade delete, or soft-deleted
```

---

### Schema / Structural Testing

Verifies the database structure matches the schema definition.

```sql
-- MySQL: List all columns and types for a table
DESCRIBE users;
SHOW COLUMNS FROM orders;

-- PostgreSQL: Column information
SELECT column_name, data_type, character_maximum_length, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'users'
ORDER BY ordinal_position;

-- Check for specific indexes
SHOW INDEX FROM orders;                           -- MySQL
SELECT indexname, indexdef FROM pg_indexes        -- PostgreSQL
WHERE tablename = 'orders';

-- Check constraints
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'users';
```

---

### Stored Procedure / Function Testing

```sql
-- Test a stored procedure directly
CALL sp_calculate_user_loyalty_score(101);

-- Verify the result
SELECT loyalty_score, tier FROM user_loyalty WHERE user_id = 101;

-- Test a function
SELECT fn_get_discounted_price(product_id => 5, quantity => 3);
```

---

### Trigger Testing

Triggers run automatically on INSERT/UPDATE/DELETE. Test by performing the triggering action and verifying the triggered behavior.

```sql
-- Test: INSERT into orders should trigger creation of an audit_log entry
-- Setup: ensure audit_log is empty for this test
DELETE FROM audit_logs WHERE entity_type = 'order' AND entity_id = 9999;

-- Trigger: Insert a new order
INSERT INTO orders (id, user_id, total_amount, status) VALUES (9999, 1, 50.00, 'pending');

-- Verify: trigger created audit log entry
SELECT * FROM audit_logs
WHERE entity_type = 'order' AND entity_id = 9999 AND action = 'INSERT';
-- Expected: 1 row
```

---

## 13. ACID Properties

ACID is a set of properties that guarantee database transactions are processed reliably.

### Atomicity

A transaction is treated as a single unit — either **all operations succeed**, or **none are committed**.

```sql
BEGIN;
  UPDATE accounts SET balance = balance - 100 WHERE id = 1;  -- debit Alice
  UPDATE accounts SET balance = balance + 100 WHERE id = 2;  -- credit Bob
COMMIT;
-- If either UPDATE fails, both are rolled back — money is not lost
```

**QA Test**: Insert a record that should trigger a cascade, then verify: if part of the operation fails (force an error), the entire transaction rolls back.

---

### Consistency

A transaction brings the database from one **valid state** to another valid state. All rules (constraints, triggers, cascades) are enforced.

**Example**: A transaction that would violate a UNIQUE constraint on `email` is rejected entirely, keeping the database consistent.

---

### Isolation

Concurrent transactions execute as if they were sequential. One transaction's intermediate state is not visible to other transactions.

**Isolation Levels** (from least to most strict):
- `READ UNCOMMITTED` — Can read uncommitted changes (dirty reads possible).
- `READ COMMITTED` — Only reads committed changes (default in PostgreSQL).
- `REPEATABLE READ` — Rows read in a transaction don't change within that transaction (default in MySQL).
- `SERIALIZABLE` — Highest isolation — transactions run fully sequentially.

---

### Durability

Once a transaction is committed, it **persists permanently** — even if the system crashes immediately after.

**QA Test**: Commit a transaction, simulate a crash (restart the DB), verify the data is still there.

---

## 14. Test Data Management — Setup and Teardown

### Strategy 1: Dedicated Test Database

Use a completely separate database for automated tests. Reset it before each test run.

```sql
-- Reset and re-seed (run before test suite)
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE users;

-- Re-seed with known test data
INSERT INTO users VALUES (1, 'Alice', 'alice@test.com', 'admin', 'active', NOW());
INSERT INTO users VALUES (2, 'Bob', 'bob@test.com', 'viewer', 'active', NOW());
```

---

### Strategy 2: Transaction Rollback (Per-Test Isolation)

Wrap each test in a transaction. Roll back after the test — no cleanup needed.

```sql
-- Test setup
BEGIN;

-- Test actions (insert test data, call procedures, etc.)
INSERT INTO users (name, email) VALUES ('Temp User', 'temp@test.com');
UPDATE orders SET status = 'processing' WHERE id = 50;

-- Verify results
SELECT * FROM users WHERE email = 'temp@test.com';

-- Test teardown — undo everything
ROLLBACK;
-- Database is back to exact state before the test
```

---

### Strategy 3: Tagging Test Data

Mark test data with a recognisable pattern and delete by tag after tests.

```sql
-- Use a test-specific email domain or prefix
INSERT INTO users (name, email) VALUES ('Test User', 'test_automated_20240115@qa.com');

-- Delete all test data after the suite
DELETE FROM users WHERE email LIKE 'test_automated_%@qa.com';
DELETE FROM orders WHERE user_id IN (
  SELECT id FROM users WHERE email LIKE 'test_automated_%@qa.com'
);
```

---

### Strategy 4: Database Snapshots / Migrations

Use tools like Flyway, Liquibase, or DB snapshots to restore a known-good state before tests.

```bash
# Restore snapshot before test suite (database-specific)
mysql -u root -p testdb < test_seed_snapshot.sql

# Or use Flyway to run test migrations
flyway -url=jdbc:mysql://localhost/testdb migrate
```

---

## 15. Connecting to a Database from Java Tests (JDBC)

When using RestAssured or JUnit, you often need to verify database state after calling an API. Here is how to do it with JDBC.

### Maven Dependency

```xml
<!-- pom.xml -->
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <version>8.0.33</version>
</dependency>
```

### JDBC Helper Class

```java
// src/test/java/utils/DatabaseHelper.java
import java.sql.*;
import java.util.*;

public class DatabaseHelper {

    private static final String URL = System.getenv().getOrDefault(
        "DB_URL", "jdbc:mysql://localhost:3306/testdb"
    );
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "password");

    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /** Execute a SELECT query and return results as list of maps */
    public List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        return results;
    }

    /** Execute INSERT / UPDATE / DELETE, returns rows affected */
    public int update(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }

    /** Count rows matching a condition */
    public int count(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> result = query(sql, params);
        if (!result.isEmpty()) {
            Object countVal = result.get(0).values().iterator().next();
            return ((Number) countVal).intValue();
        }
        return 0;
    }
}
```

### Using DatabaseHelper in a Test

```java
// src/test/java/tests/UserApiTest.java
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserApiTest {

    private static DatabaseHelper db = new DatabaseHelper();

    @BeforeAll
    static void setup() throws Exception {
        RestAssured.baseURI = "http://localhost:8080";
        db.connect();
    }

    @AfterAll
    static void teardown() throws Exception {
        // Clean up test data
        db.update("DELETE FROM users WHERE email = ?", "qa_test@example.com");
        db.disconnect();
    }

    @Test
    void postUser_ShouldPersistToDatabase() throws Exception {
        // Step 1: Call the API
        String body = """
            {
              "name": "QA Test User",
              "email": "qa_test@example.com",
              "role": "viewer"
            }
            """;

        int createdUserId = given()
            .contentType("application/json")
            .body(body)
        .when()
            .post("/api/users")
        .then()
            .statusCode(201)
            .body("email", equalTo("qa_test@example.com"))
            .extract().path("id");

        // Step 2: Verify in the database
        var rows = db.query(
            "SELECT id, name, email, role, status FROM users WHERE id = ?",
            createdUserId
        );

        assertEquals(1, rows.size(), "Expected 1 user row in DB");
        assertEquals("QA Test User", rows.get(0).get("name"));
        assertEquals("qa_test@example.com", rows.get(0).get("email"));
        assertEquals("viewer", rows.get(0).get("role"));
        assertEquals("active", rows.get(0).get("status"));
    }

    @Test
    void deleteUser_ShouldSoftDeleteInDatabase() throws Exception {
        // Assume user 101 exists
        given().when().delete("/api/users/101").then().statusCode(204);

        // Verify soft delete — deleted_at should be set
        var rows = db.query(
            "SELECT deleted_at FROM users WHERE id = ?", 101
        );
        assertEquals(1, rows.size(), "User row should still exist");
        assertNotNull(rows.get(0).get("deleted_at"), "deleted_at should be set after soft delete");
    }
}
```

---

## 16. Senior Interview Q&A — 10 Questions with Full Answers

---

**Q1: What is the difference between INNER JOIN, LEFT JOIN, and RIGHT JOIN? Give a QA use case for each.**

**A**:

- **INNER JOIN** returns only rows where there is a match in both tables. Use it when you want to verify that a relationship exists: e.g., "Show me all orders that have a corresponding user" — if the user_id is invalid, that order won't appear.

- **LEFT JOIN** returns all rows from the left table and matched rows from the right (NULLs for non-matches). QA use case: Find users who have never placed an order — `LEFT JOIN orders ON user.id = order.user_id WHERE order.id IS NULL`.

- **RIGHT JOIN** is the mirror of LEFT JOIN — all rows from the right table. QA use case: Find orphaned orders (orders with no corresponding user) — `RIGHT JOIN users ON order.user_id = user.id WHERE user.id IS NULL`.

In practice, most QA engineers use LEFT JOIN for orphan checks because it is more readable and universally supported, reversing the table order compared to RIGHT JOIN.

---

**Q2: How do NULLs behave in SQL, and what mistakes do developers and QA engineers often make with them?**

**A**: NULL represents the absence of a value — it is not zero, not an empty string, and not false. This causes three common mistakes:

1. **Comparing with = NULL**: `WHERE phone = NULL` always returns 0 rows because NULL is not equal to anything, including itself. Use `WHERE phone IS NULL`.

2. **NOT IN with NULLs**: If a subquery in `NOT IN` contains even one NULL, the entire `NOT IN` returns no rows. Example:
   ```sql
   -- If orders has a row with user_id = NULL, this returns 0 rows for ALL users:
   SELECT * FROM users WHERE id NOT IN (SELECT user_id FROM orders);
   -- Fix: add WHERE user_id IS NOT NULL in the subquery
   ```

3. **Aggregates ignoring NULL**: `AVG()`, `SUM()`, `COUNT(column)` all ignore NULL values. `COUNT(*)` counts all rows including NULLs. `COUNT(email)` counts only non-NULL email values.

---

**Q3: What are ACID properties and why do they matter for database testing?**

**A**: ACID stands for Atomicity, Consistency, Isolation, and Durability — the four properties that guarantee reliable database transactions.

- **Atomicity**: A transaction is all-or-nothing. QA tests: if a payment debit fails midway, the corresponding credit should not have been applied. Rollback should restore the original state.
- **Consistency**: Constraints are never violated. QA tests: verify that a NOT NULL column cannot be set to NULL, and that UNIQUE constraints prevent duplicate entries.
- **Isolation**: Concurrent transactions should not interfere. QA tests: simulate two users booking the last seat simultaneously and verify only one succeeds.
- **Durability**: Committed transactions survive crashes. QA tests: commit a transaction, restart the database, verify the data persists.

For QA, ACID is important because tests that run in parallel can interfere with each other if transactions are not properly isolated. Using transactions with ROLLBACK in test teardown ensures clean isolation.

---

**Q4: What is the difference between TRUNCATE and DELETE?**

**A**:

- **DELETE** removes rows one at a time, logs each deletion for rollback, fires DELETE triggers, and can include a WHERE clause to delete specific rows. It does NOT reset the AUTO_INCREMENT counter.
- **TRUNCATE** removes all rows instantly (deallocation), does not log individual row deletions, cannot be rolled back in most databases (it is DDL, not DML), does NOT fire row-level triggers, and RESETS the AUTO_INCREMENT counter to 1.

For QA:
- Use `TRUNCATE` to fully reset a test table between test suites (fast, resets IDs).
- Use `DELETE WHERE email LIKE 'test_%'` to clean up only the specific test data created by a particular test.
- Never use `DELETE` without `WHERE` in a production database — it deletes everything but is slower than TRUNCATE.

---

**Q5: How do you verify via SQL that an API with pagination is returning the correct records?**

**A**: The approach has two parts — count verification and data verification.

1. **Count verification**: Query the database with the same filter as the API and compare `COUNT(*)` to the API's `meta.totalCount` field.

2. **Page verification**: Replicate the API's query with the same ORDER BY and OFFSET/LIMIT:
   ```sql
   SELECT id FROM orders ORDER BY created_at DESC LIMIT 10 OFFSET 10;
   ```
   The IDs returned must match the IDs in the API response for page 2.

3. **Edge case checks**:
   - Query for a page beyond the last page — API should return an empty array, not an error.
   - Verify total count matches when adding a new record (total should increment by 1).
   - Verify consistent ordering — if data changes between page 1 and page 2 calls, results can shift; check the API uses a stable sort.

---

**Q6: What is a SQL index and how can it affect your performance test results?**

**A**: An index is a data structure (typically a B-tree) that allows the database to find rows matching a condition without scanning every row in the table. Think of it as a book's index — instead of reading every page to find "JMeter", you go directly to the indexed location.

**Impact on QA**:
- Without an index on `orders.user_id`, a query like `SELECT * FROM orders WHERE user_id = 101` performs a full table scan — O(n) time.
- With an index, the same query is O(log n) — much faster as the table grows.

**Performance test implications**:
- A performance test running against a small database (1000 rows) may show fast response times even without indexes.
- The same test against a production-scale database (10 million rows) will show massively degraded performance for unindexed queries.
- QA should run volume tests with production-scale data to catch missing indexes before they reach production.

To check indexes:
```sql
SHOW INDEX FROM orders;                    -- MySQL
\d orders                                  -- PostgreSQL (show table including indexes)
```

---

**Q7: How do you use SQL to verify data was correctly soft-deleted?**

**A**: A soft delete does not physically remove the row — it marks it as deleted using a flag column (typically `deleted_at TIMESTAMP` or `is_deleted BOOLEAN`).

**SQL verification after DELETE /api/users/101**:

```sql
-- 1. Row still exists (not physically deleted)
SELECT COUNT(*) FROM users WHERE id = 101;
-- Expected: 1 (row is still there)

-- 2. deleted_at is set to a recent timestamp
SELECT deleted_at FROM users WHERE id = 101;
-- Expected: a timestamp within the last minute

-- 3. Active queries exclude this user
SELECT COUNT(*) FROM users WHERE id = 101 AND deleted_at IS NULL;
-- Expected: 0 (user is no longer "active")

-- 4. Verify the user doesn't appear in the "active users" API
-- Call GET /api/users and verify user 101 is NOT in the response
-- Cross-check: if API uses WHERE deleted_at IS NULL, DB and API should agree
```

---

**Q8: What is the difference between optimistic and pessimistic locking in databases?**

**A**:

**Pessimistic Locking**: Assumes conflicts will occur. Locks the row when reading it, preventing other transactions from modifying it until the lock is released.

```sql
-- MySQL: Locks the row for update (no one else can modify it)
SELECT * FROM orders WHERE id = 50 FOR UPDATE;
-- Make changes, then:
COMMIT;
```

Use case: High-contention scenarios like inventory management (booking the last seat). Prevents conflicts but reduces concurrency and can cause deadlocks.

**Optimistic Locking**: Assumes conflicts are rare. No lock is taken during read. Instead, a version counter or timestamp is checked at update time.

```sql
-- Read with version
SELECT id, status, version FROM orders WHERE id = 50;
-- version = 3

-- Update — only succeeds if version hasn't changed
UPDATE orders SET status = 'shipped', version = 4
WHERE id = 50 AND version = 3;
-- If 0 rows affected → another process updated first → retry or show conflict error
```

**QA test scenarios**:
- Optimistic locking: Simulate two concurrent users editing the same record. Verify only one succeeds and the other gets a conflict error.
- Pessimistic locking: Verify that while transaction A holds a lock, transaction B waits (or times out) and does not get a dirty read.

---

**Q9: What SQL would you write to verify that all required audit log entries exist after a batch operation?**

**A**: After an operation that modifies multiple records (e.g., bulk price update for a category), verify the audit log has one entry per modified record:

```sql
-- Count products updated by the bulk operation
SELECT COUNT(*) AS products_updated
FROM products
WHERE category = 'Electronics'
  AND updated_at >= NOW() - INTERVAL 5 MINUTE;

-- Count audit log entries for the same operation
SELECT COUNT(*) AS audit_entries
FROM audit_logs
WHERE entity_type = 'product'
  AND action = 'UPDATE'
  AND change_reason = 'bulk_price_update'
  AND created_at >= NOW() - INTERVAL 5 MINUTE;

-- Both counts must match
-- If products_updated = 150 but audit_entries = 0: trigger not firing
-- If products_updated = 150 but audit_entries = 149: one record missed the trigger

-- Cross-reference: find which products are missing audit entries
SELECT p.id
FROM products p
WHERE p.category = 'Electronics'
  AND p.updated_at >= NOW() - INTERVAL 5 MINUTE
  AND p.id NOT IN (
    SELECT entity_id FROM audit_logs
    WHERE entity_type = 'product'
      AND action = 'UPDATE'
      AND created_at >= NOW() - INTERVAL 5 MINUTE
  );
```

---

**Q10: What is a transaction and why is it important in test data setup and teardown?**

**A**: A transaction is a sequence of SQL operations executed as a single logical unit. Either all operations commit (permanently saved) or all roll back (completely undone).

**Why it matters for test data**:

Without transactions, test data cleanup is manual and error-prone. If a test fails midway, partial data remains in the database, causing subsequent tests to behave unexpectedly.

With transactions:
```sql
BEGIN;

-- Setup: insert test data
INSERT INTO users (name, email) VALUES ('Test User', 'test@qa.com');
INSERT INTO orders (user_id, total) VALUES (LAST_INSERT_ID(), 100.00);

-- Run test assertions here...

-- Teardown: undo all test data, regardless of pass/fail
ROLLBACK;
```

After `ROLLBACK`, the database is in exactly the same state as before the test — no cleanup code needed, no partial data left behind.

In Java/JUnit with RestAssured, this is implemented using the `@Transactional` annotation (with Spring) or by wrapping each test's JDBC calls in a `BEGIN/ROLLBACK` block, ensuring true test isolation even when multiple tests run in parallel against the same database.

---

*End of SQL & Database Testing Complete Guide*
