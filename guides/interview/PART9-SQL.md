# Part 9 — SQL for QA | 40 Questions | Full Answers + Queries

> CV context: Kupeshanth — Qoria Lanka (GCP, GitHub Actions), Cerexio (JIRA, Agile).
> Skills base: Java, Python, Postman, REST API testing.

---

## Q1. Why does a QA engineer need SQL?

**Answer:**
QA engineers use SQL to:

1. **Verify data** — Confirm that API/UI actions correctly created, updated, or deleted records in the database
2. **Set up test data** — Insert prerequisite records before running tests
3. **Clean up test data** — DELETE or TRUNCATE after tests to avoid pollution
4. **Find bugs** — Detect orphaned records, duplicate entries, NULL values, missing data
5. **Validate business logic** — Confirm calculations (totals, balances) match DB state
6. **Investigate failures** — When a test fails, query the DB to understand the actual state

Example workflow:
```
POST /api/users {"email": "test@example.com"}
→ Assert 201 Created
→ SELECT * FROM users WHERE email = 'test@example.com'  ← QA verification step
→ Assert row exists with correct values
```

Without SQL, you are only testing the API surface — not what actually happened to the data.

---

## Q2. Basic SELECT — retrieve all columns from a table.

```sql
-- All columns, all rows
SELECT * FROM users;

-- Specific columns only
SELECT id, email, created_at FROM users;

-- Column alias for readability
SELECT id AS user_id, email AS email_address FROM users;

-- Distinct values
SELECT DISTINCT status FROM orders;

-- Limit results
SELECT * FROM users LIMIT 10;
```

**QA use case:** After registering a new user via API, run:
```sql
SELECT id, email, status, created_at 
FROM users 
WHERE email = 'newuser@example.com';
```

---

## Q3. WHERE clause — filter rows by condition.

```sql
-- Exact match
SELECT * FROM orders WHERE status = 'PENDING';

-- Numeric comparison
SELECT * FROM products WHERE price > 100;

-- Not equal
SELECT * FROM users WHERE role != 'admin';
-- or
SELECT * FROM users WHERE role <> 'admin';

-- Range
SELECT * FROM orders WHERE amount BETWEEN 50 AND 200;

-- Specific values list
SELECT * FROM users WHERE status IN ('ACTIVE', 'VERIFIED');

-- Negation
SELECT * FROM users WHERE status NOT IN ('BANNED', 'DELETED');
```

---

## Q4. AND, OR, NOT — combine conditions.

```sql
-- AND — both conditions must be true
SELECT * FROM orders 
WHERE status = 'COMPLETED' 
AND amount > 1000;

-- OR — either condition
SELECT * FROM users 
WHERE role = 'admin' 
OR role = 'superuser';

-- Combining AND + OR (use parentheses!)
SELECT * FROM orders
WHERE (status = 'PENDING' OR status = 'PROCESSING')
AND created_at >= '2024-01-01';

-- NOT
SELECT * FROM users 
WHERE NOT status = 'DELETED';
-- equivalent to: WHERE status != 'DELETED'
```

**QA note:** Always use parentheses with mixed AND/OR to control precedence. Without them, AND evaluates before OR, which can produce unexpected results.

---

## Q5. ORDER BY and LIMIT/OFFSET — sorting and pagination.

```sql
-- Ascending (default)
SELECT * FROM products ORDER BY price ASC;

-- Descending
SELECT * FROM orders ORDER BY created_at DESC;

-- Multiple sort columns
SELECT * FROM users ORDER BY last_name ASC, first_name ASC;

-- LIMIT — top N rows
SELECT * FROM orders ORDER BY amount DESC LIMIT 5;

-- OFFSET — skip rows (for pagination verification)
-- Page 1: rows 1-10
SELECT * FROM products ORDER BY id LIMIT 10 OFFSET 0;

-- Page 2: rows 11-20
SELECT * FROM products ORDER BY id LIMIT 10 OFFSET 10;

-- Page 3: rows 21-30
SELECT * FROM products ORDER BY id LIMIT 10 OFFSET 20;
```

**QA pagination test:** When API returns page 2 with 10 items:
```sql
SELECT * FROM products 
WHERE status = 'ACTIVE'
ORDER BY created_at DESC
LIMIT 10 OFFSET 10;
-- Compare these IDs with API response
```

---

## Q6. INNER JOIN — return rows matching in both tables.

```sql
-- Syntax
SELECT a.column, b.column
FROM table_a a
INNER JOIN table_b b ON a.foreign_key = b.primary_key;

-- Example: Get orders with customer names
SELECT 
    o.id AS order_id,
    o.amount,
    o.status,
    u.email AS customer_email,
    u.name AS customer_name
FROM orders o
INNER JOIN users u ON o.user_id = u.id;

-- Filter joined result
SELECT 
    o.id,
    o.amount,
    u.email
FROM orders o
INNER JOIN users u ON o.user_id = u.id
WHERE o.status = 'PENDING'
ORDER BY o.created_at DESC;
```

**QA use case:** Verify that a created order is linked to the correct user:
```sql
SELECT o.id, o.amount, u.email
FROM orders o
INNER JOIN users u ON o.user_id = u.id
WHERE o.id = 12345;
-- If no row returned — order exists but user link is broken (bug!)
```

---

## Q7. LEFT JOIN — return all rows from left table, matched rows from right.

```sql
-- LEFT JOIN: all rows from 'left' table, NULL for missing right matches
SELECT 
    u.id,
    u.email,
    o.id AS order_id,
    o.amount
FROM users u
LEFT JOIN orders o ON u.id = o.user_id;
-- Users with no orders will show: user.id, user.email, NULL, NULL

-- Find users with NO orders (orphan records)
SELECT u.id, u.email
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE o.id IS NULL;

-- Find orders with no payment record
SELECT o.id, o.amount, o.status
FROM orders o
LEFT JOIN payments p ON o.id = p.order_id
WHERE p.id IS NULL;
-- These are bug candidates — completed orders with no payment!
```

**QA use case — missing data check:**
After a batch job processes orders, verify no orders were skipped:
```sql
SELECT o.id AS unprocessed_order
FROM orders o
LEFT JOIN processed_orders po ON o.id = po.order_id
WHERE po.order_id IS NULL
AND o.status = 'COMPLETED';
```

---

## Q8. RIGHT JOIN — return all rows from right table.

```sql
-- RIGHT JOIN: all rows from 'right' table, NULL for missing left matches
SELECT 
    u.email,
    r.role_name
FROM users u
RIGHT JOIN roles r ON u.role_id = r.id;
-- Shows all roles, including roles assigned to no users

-- Find roles with no assigned users
SELECT r.role_name
FROM users u
RIGHT JOIN roles r ON u.role_id = r.id
WHERE u.id IS NULL;
```

**Note:** RIGHT JOIN is less commonly used. Most people rewrite as LEFT JOIN by swapping table order:
```sql
-- Equivalent
SELECT r.role_name FROM roles r LEFT JOIN users u ON r.id = u.role_id WHERE u.id IS NULL;
```

---

## Q9. GROUP BY and HAVING — aggregate and filter groups.

```sql
-- Count orders per user
SELECT user_id, COUNT(*) AS order_count
FROM orders
GROUP BY user_id;

-- Total amount per user
SELECT user_id, SUM(amount) AS total_spent
FROM orders
GROUP BY user_id;

-- Average order value per status
SELECT status, AVG(amount) AS avg_amount, COUNT(*) AS total
FROM orders
GROUP BY status;

-- HAVING: filter groups (like WHERE but for aggregates)
-- Users with more than 5 orders
SELECT user_id, COUNT(*) AS order_count
FROM orders
GROUP BY user_id
HAVING COUNT(*) > 5;

-- Statuses with average amount > 100
SELECT status, AVG(amount) AS avg_amount
FROM orders
GROUP BY status
HAVING AVG(amount) > 100;

-- Combined with WHERE
SELECT user_id, COUNT(*) AS completed_orders
FROM orders
WHERE status = 'COMPLETED'
GROUP BY user_id
HAVING COUNT(*) >= 3;
```

**QA use case:** Find users who placed duplicate orders (same product, same day):
```sql
SELECT user_id, product_id, DATE(created_at), COUNT(*) AS count
FROM orders
GROUP BY user_id, product_id, DATE(created_at)
HAVING COUNT(*) > 1;
```

---

## Q10. Aggregate functions: COUNT, SUM, AVG, MAX, MIN.

```sql
-- COUNT: number of rows
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(DISTINCT status) AS distinct_statuses FROM orders;
SELECT COUNT(email) AS users_with_email FROM users; -- ignores NULLs

-- SUM: total of a numeric column
SELECT SUM(amount) AS total_revenue FROM orders WHERE status = 'COMPLETED';

-- AVG: average value
SELECT AVG(amount) AS avg_order_value FROM orders;
SELECT ROUND(AVG(amount), 2) AS avg_amount FROM orders;

-- MAX: largest value
SELECT MAX(amount) AS largest_order FROM orders;
SELECT MAX(created_at) AS last_order_date FROM orders;

-- MIN: smallest value
SELECT MIN(price) AS cheapest_product FROM products;

-- All together
SELECT
    COUNT(*) AS total_orders,
    SUM(amount) AS total_revenue,
    ROUND(AVG(amount), 2) AS avg_order,
    MAX(amount) AS largest_order,
    MIN(amount) AS smallest_order
FROM orders
WHERE status = 'COMPLETED';
```

---

## Q11. NULL handling — IS NULL, IS NOT NULL, COALESCE.

```sql
-- IS NULL: find rows with missing values
SELECT * FROM users WHERE phone IS NULL;

-- IS NOT NULL: rows with values
SELECT * FROM users WHERE email IS NOT NULL;

-- TRAP: NULL comparison with = ALWAYS fails!
-- Wrong — never returns rows:
SELECT * FROM users WHERE phone = NULL;

-- Correct:
SELECT * FROM users WHERE phone IS NULL;

-- NOT IN gotcha with NULLs:
-- This may return no rows if orders.user_id contains any NULL:
SELECT * FROM users 
WHERE id NOT IN (SELECT user_id FROM orders);

-- Safe version:
SELECT * FROM users 
WHERE id NOT IN (SELECT user_id FROM orders WHERE user_id IS NOT NULL);

-- COALESCE: return first non-NULL value
SELECT 
    name,
    COALESCE(phone, 'No phone provided') AS phone_display
FROM users;

-- COALESCE for default values
SELECT 
    id,
    COALESCE(discount, 0) AS discount_amount,
    amount - COALESCE(discount, 0) AS final_amount
FROM orders;

-- IFNULL (MySQL shorthand)
SELECT IFNULL(phone, 'N/A') FROM users;

-- NULLIF: return NULL if two values are equal (avoid division by zero)
SELECT amount / NULLIF(quantity, 0) AS price_per_unit FROM order_items;
```

---

## Q12. LIKE — pattern matching.

```sql
-- % = any number of characters
SELECT * FROM users WHERE email LIKE '%@example.com';  -- ends with
SELECT * FROM products WHERE name LIKE 'Pro%';         -- starts with
SELECT * FROM users WHERE name LIKE '%John%';           -- contains

-- _ = exactly one character
SELECT * FROM users WHERE phone LIKE '07_-______';     -- 07X-XXXXXX

-- Case-insensitive (MySQL default, PostgreSQL needs ILIKE)
SELECT * FROM users WHERE email ILIKE '%GMAIL%';  -- PostgreSQL

-- NOT LIKE
SELECT * FROM users WHERE email NOT LIKE '%test%';

-- QA use case: find test data to clean up
SELECT * FROM users WHERE email LIKE '%@test.com' OR email LIKE '%@example.com';
```

---

## Q13. String functions: UPPER, LOWER, TRIM, LENGTH, CONCAT.

```sql
-- UPPER / LOWER
SELECT UPPER(email) FROM users;
SELECT LOWER(status) FROM orders;

-- TRIM: remove leading/trailing spaces
SELECT TRIM(name) FROM users;
SELECT LTRIM(name) FROM users;  -- left only
SELECT RTRIM(name) FROM users;  -- right only

-- LENGTH
SELECT * FROM users WHERE LENGTH(password) < 8;  -- too short passwords (bug!)

-- CONCAT
SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users;
-- MySQL pipe: first_name || ' ' || last_name (PostgreSQL)

-- SUBSTRING
SELECT SUBSTRING(email, 1, INSTR(email, '@') - 1) AS username FROM users;

-- REPLACE
SELECT REPLACE(description, 'v1.0', 'v2.0') FROM products;

-- QA use case: find emails with extra spaces (data quality bug)
SELECT * FROM users WHERE email != TRIM(email);
```

---

## Q14. Date functions: NOW, CURDATE, DATEDIFF, DATE_FORMAT.

```sql
-- Current datetime and date
SELECT NOW();           -- 2024-03-15 14:30:00
SELECT CURDATE();       -- 2024-03-15
SELECT CURTIME();       -- 14:30:00

-- Date arithmetic
SELECT * FROM orders WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY);  -- last 7 days
SELECT * FROM sessions WHERE expires_at < NOW();  -- expired sessions

-- DATEDIFF: difference in days
SELECT DATEDIFF(NOW(), created_at) AS days_since_creation FROM users;

-- Find accounts inactive for 30+ days
SELECT * FROM users 
WHERE DATEDIFF(NOW(), last_login) > 30;

-- DATE_FORMAT
SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS order_date FROM orders;
SELECT DATE_FORMAT(created_at, '%d/%m/%Y %H:%i') AS formatted FROM orders;

-- Extract parts
SELECT YEAR(created_at), MONTH(created_at), DAY(created_at) FROM orders;
SELECT DATE(created_at) AS date_only FROM orders;

-- QA use case: verify order timestamps are reasonable
SELECT * FROM orders 
WHERE created_at > NOW()  -- future dates = bug!
OR created_at < '2020-01-01';  -- ancient dates = bug!

-- Verify token expiry (e.g., 24-hour tokens)
SELECT * FROM password_reset_tokens
WHERE created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)
AND used = 0;
-- These should be expired but are still active — bug!
```

---

## Q15. Subqueries — query inside a query.

```sql
-- Scalar subquery (returns single value)
SELECT * FROM orders 
WHERE amount > (SELECT AVG(amount) FROM orders);

-- Subquery in WHERE with IN
SELECT * FROM users 
WHERE id IN (
    SELECT user_id FROM orders WHERE status = 'COMPLETED'
);

-- Subquery with NOT IN (check NULL gotcha!)
SELECT * FROM users 
WHERE id NOT IN (
    SELECT user_id FROM orders WHERE user_id IS NOT NULL
);

-- Correlated subquery (references outer query)
SELECT u.id, u.email,
    (SELECT COUNT(*) FROM orders o WHERE o.user_id = u.id) AS order_count
FROM users u;

-- Subquery in FROM clause
SELECT dept, avg_salary
FROM (
    SELECT department AS dept, AVG(salary) AS avg_salary
    FROM employees
    GROUP BY department
) dept_summary
WHERE avg_salary > 50000;

-- QA use case: find users who ordered but never completed a payment
SELECT * FROM users 
WHERE id IN (
    SELECT DISTINCT user_id FROM orders WHERE status = 'PENDING'
)
AND id NOT IN (
    SELECT DISTINCT user_id FROM payments WHERE status = 'SUCCESS'
    WHERE user_id IS NOT NULL
);
```

---

## Q16. IN vs EXISTS — when to use each.

```sql
-- IN: good for small sets, checks if value in list
SELECT * FROM products 
WHERE category_id IN (1, 2, 3);

SELECT * FROM products 
WHERE category_id IN (SELECT id FROM categories WHERE active = 1);

-- EXISTS: better for large sets, stops at first match
SELECT * FROM users u
WHERE EXISTS (
    SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.status = 'PENDING'
);

-- NOT EXISTS: users with no completed orders
SELECT * FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.status = 'COMPLETED'
);

-- Performance rule:
-- EXISTS is faster when subquery table is large (short-circuits at first match)
-- IN is fine for small, static lists
-- NOT IN with NULL-containing columns = unreliable → prefer NOT EXISTS
```

---

## Q17. INSERT — add test data to database.

```sql
-- Single row
INSERT INTO users (email, name, status, created_at)
VALUES ('testuser@example.com', 'Test User', 'ACTIVE', NOW());

-- Multiple rows at once
INSERT INTO users (email, name, status) VALUES
('user1@test.com', 'User One', 'ACTIVE'),
('user2@test.com', 'User Two', 'INACTIVE'),
('admin@test.com', 'Admin User', 'ACTIVE');

-- Insert with auto-increment — get the generated ID
INSERT INTO orders (user_id, amount, status) VALUES (1, 150.00, 'PENDING');
SELECT LAST_INSERT_ID();  -- MySQL
-- RETURNING id  -- PostgreSQL

-- Insert only if not exists (prevent duplicates)
INSERT IGNORE INTO users (email, name) VALUES ('test@test.com', 'Test');  -- MySQL
-- or
INSERT INTO users (email, name) 
VALUES ('test@test.com', 'Test')
ON CONFLICT (email) DO NOTHING;  -- PostgreSQL

-- QA setup test data
-- Create a test user with known ID for downstream tests
INSERT INTO users (id, email, password_hash, role, status)
VALUES (9999, 'qa_test@example.com', 'hashed_pw', 'user', 'ACTIVE');
```

---

## Q18. UPDATE — modify test data.

```sql
-- Update single field
UPDATE users SET status = 'ACTIVE' WHERE id = 123;

-- Update multiple fields
UPDATE users 
SET status = 'VERIFIED', 
    verified_at = NOW(),
    updated_at = NOW()
WHERE email = 'testuser@example.com';

-- Update with calculation
UPDATE orders SET amount = amount * 1.10 WHERE status = 'PENDING';

-- Update based on another table (subquery)
UPDATE users 
SET status = 'HIGH_VALUE'
WHERE id IN (
    SELECT user_id FROM orders GROUP BY user_id HAVING SUM(amount) > 10000
);

-- IMPORTANT: Always use WHERE clause!
-- UPDATE users SET status = 'DELETED';  ← updates ALL rows — disaster!

-- Verify before updating
SELECT * FROM users WHERE email = 'testuser@example.com';  -- check first
UPDATE users SET status = 'BANNED' WHERE email = 'testuser@example.com';
SELECT * FROM users WHERE email = 'testuser@example.com';  -- verify after
```

---

## Q19. DELETE — remove test data.

```sql
-- Delete specific row
DELETE FROM users WHERE id = 9999;

-- Delete with condition
DELETE FROM sessions WHERE expires_at < NOW();

-- Delete with subquery
DELETE FROM orders WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@test.com'
);

-- IMPORTANT: Always use WHERE
-- DELETE FROM users;  ← deletes ALL rows!

-- Safe delete pattern: SELECT first
SELECT * FROM users WHERE email LIKE '%@qa_%';  -- see what will be deleted
DELETE FROM users WHERE email LIKE '%@qa_%';     -- then delete

-- QA cleanup after test run
DELETE FROM users WHERE email IN (
    'qa_test@example.com',
    'qa_admin@example.com'
);
DELETE FROM orders WHERE user_id = 9999;  -- cleanup orders first (FK)
DELETE FROM users WHERE id = 9999;         -- then user
```

---

## Q20. TRUNCATE vs DELETE — key differences.

| Feature | DELETE | TRUNCATE |
|---|---|---|
| WHERE clause | Yes | No |
| Rollback | Yes (DML) | No (DDL in MySQL) |
| Triggers | Fires | Does NOT fire |
| Auto-increment reset | No | Yes |
| Speed | Slower (row by row) | Much faster |
| Logs | Row-level | Minimal |

```sql
-- DELETE: removes specific rows, can rollback
BEGIN;
DELETE FROM test_orders WHERE user_id = 9999;
-- check something
ROLLBACK;  -- undo if needed

-- TRUNCATE: wipes entire table instantly, no rollback
TRUNCATE TABLE test_data;
-- Auto-increment resets to 1

-- QA use case:
-- Use DELETE for targeted cleanup (specific test data)
-- Use TRUNCATE for clearing entire test tables between test runs
TRUNCATE TABLE test_users;  -- faster, resets IDs
```

---

## Q21. Transactions — COMMIT and ROLLBACK.

```sql
-- Start transaction
BEGIN;  -- or START TRANSACTION;

-- Multiple operations as one unit
INSERT INTO orders (user_id, amount, status) VALUES (1, 500, 'PENDING');
INSERT INTO payments (order_id, amount, method) VALUES (LAST_INSERT_ID(), 500, 'CARD');
UPDATE inventory SET quantity = quantity - 1 WHERE product_id = 42;

-- All succeeded — commit
COMMIT;

-- Something went wrong — undo all
ROLLBACK;

-- QA pattern: test data setup with guaranteed cleanup
BEGIN;
INSERT INTO users (email, name) VALUES ('temp_qa@test.com', 'QA User');
-- run test assertions
ROLLBACK;  -- clean up automatically

-- Savepoints
START TRANSACTION;
INSERT INTO orders ...;  -- step 1
SAVEPOINT step1;
INSERT INTO payments ...;  -- step 2
-- If step 2 fails:
ROLLBACK TO SAVEPOINT step1;  -- undo step 2 only
COMMIT;  -- commit step 1
```

---

## Q22. ACID properties — explain for QA context.

**Answer:**

| Property | Meaning | QA Relevance |
|---|---|---|
| **Atomicity** | All operations succeed or all fail | Verify partial failures roll back correctly |
| **Consistency** | DB goes from valid state to valid state | Verify constraints (FK, NOT NULL, UNIQUE) are enforced |
| **Isolation** | Concurrent transactions don't interfere | Test concurrent API calls don't corrupt data |
| **Durability** | Committed data persists | Verify data survives server restart |

```sql
-- Test Atomicity: if payment fails, order should not be created
-- Call POST /api/orders with invalid payment card
-- Verify:
SELECT * FROM orders WHERE created_at > DATE_SUB(NOW(), INTERVAL 1 MINUTE);
-- Should return empty — order rolled back with payment failure

-- Test Consistency: FK constraint enforced
INSERT INTO orders (user_id, amount) VALUES (99999, 100);
-- Should fail: user_id 99999 doesn't exist
-- Error: Cannot add or update a child row: a foreign key constraint fails

-- Test Isolation: two users updating same inventory simultaneously
-- Run concurrent API calls: both users order last item
-- Verify: only one succeeds, inventory does not go negative
SELECT quantity FROM inventory WHERE product_id = 42;
-- Should be 0, not -1
```

---

## Q23. Referential integrity checks.

```sql
-- Find orphan records — orders with no matching user
SELECT o.*
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
WHERE u.id IS NULL;
-- Should return 0 rows. If not — FK constraint missing or data corruption.

-- Find payments with no matching order
SELECT p.*
FROM payments p
LEFT JOIN orders o ON p.order_id = o.id
WHERE o.id IS NULL;

-- Find products in order_items with no matching product
SELECT oi.product_id, COUNT(*) AS count
FROM order_items oi
LEFT JOIN products p ON oi.product_id = p.id
WHERE p.id IS NULL
GROUP BY oi.product_id;

-- Verify FK constraints exist
SELECT 
    TABLE_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'your_database'
AND REFERENCED_TABLE_NAME IS NOT NULL;
```

---

## Q24 - Q30: Real QA Scenarios

### Q24. Verify a POST /api/users created the correct record.
```sql
-- After POST {"email": "john@example.com", "name": "John", "role": "user"}
-- API returns: {"id": 456, "status": "ACTIVE"}

SELECT 
    id, email, name, role, status, 
    created_at, updated_at
FROM users 
WHERE id = 456;

-- Assertions:
-- email = 'john@example.com'
-- name = 'John'
-- role = 'user'
-- status = 'ACTIVE'
-- created_at IS NOT NULL
-- updated_at IS NOT NULL
-- password_hash IS NOT NULL  (should be hashed, not plain text)
-- password_hash != 'Password123!'  (must NOT be stored in plain text)
```

### Q25. Find duplicate records in a table.
```sql
-- Find duplicate emails
SELECT email, COUNT(*) AS count
FROM users
GROUP BY email
HAVING COUNT(*) > 1;

-- Get full details of duplicates
SELECT * FROM users
WHERE email IN (
    SELECT email FROM users
    GROUP BY email
    HAVING COUNT(*) > 1
)
ORDER BY email;

-- Find duplicate orders (same user, same product, same day)
SELECT user_id, product_id, DATE(created_at), COUNT(*) AS count
FROM orders
GROUP BY user_id, product_id, DATE(created_at)
HAVING COUNT(*) > 1;

-- Find duplicate transaction IDs (critical bug!)
SELECT transaction_id, COUNT(*) AS count
FROM payments
GROUP BY transaction_id
HAVING COUNT(*) > 1;
```

### Q26. Check for orphaned records.
```sql
-- Orders with no existing user (data integrity issue)
SELECT o.id, o.user_id, o.amount, o.created_at
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
WHERE u.id IS NULL;

-- OrderItems with no parent order
SELECT oi.id, oi.order_id
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.id
WHERE o.id IS NULL;

-- Fix: delete orphans (with caution!)
-- DELETE FROM order_items WHERE order_id NOT IN (SELECT id FROM orders);
```

### Q27. Verify API pagination returns correct data.
```sql
-- API: GET /api/products?page=2&limit=10&sort=created_at&order=desc

-- SQL equivalent
SELECT id, name, price, created_at
FROM products
WHERE status = 'ACTIVE'
ORDER BY created_at DESC
LIMIT 10 OFFSET 10;

-- Verify total count matches API's "total" field
SELECT COUNT(*) AS total FROM products WHERE status = 'ACTIVE';

-- Verify no duplicates across pages
-- Page 1:
SELECT id FROM products WHERE status='ACTIVE' ORDER BY created_at DESC LIMIT 10 OFFSET 0;
-- Page 2:
SELECT id FROM products WHERE status='ACTIVE' ORDER BY created_at DESC LIMIT 10 OFFSET 10;
-- IDs should not overlap
```

### Q28. Find failed transactions.
```sql
-- All failed payments in last 24 hours
SELECT 
    p.id,
    p.order_id,
    p.amount,
    p.status,
    p.failure_reason,
    p.created_at,
    u.email AS customer_email
FROM payments p
INNER JOIN orders o ON p.order_id = o.id
INNER JOIN users u ON o.user_id = u.id
WHERE p.status = 'FAILED'
AND p.created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
ORDER BY p.created_at DESC;

-- Count failure rate
SELECT 
    status,
    COUNT(*) AS count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) AS percentage
FROM payments
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY status;
```

### Q29. Verify an UPDATE call worked correctly.
```sql
-- After PUT /api/users/123 {"name": "Updated Name", "phone": "0771234567"}

-- Before update (use a transaction or check history)
SELECT name, phone, updated_at FROM users WHERE id = 123;

-- After API call
SELECT name, phone, updated_at FROM users WHERE id = 123;

-- Assertions:
-- name = 'Updated Name'
-- phone = '0771234567'
-- updated_at > previous_updated_at (timestamp was updated)
-- email should be UNCHANGED (verify field isolation)
SELECT email FROM users WHERE id = 123;  -- should still be original email
```

### Q30. Verify a DELETE call removed correct records.
```sql
-- After DELETE /api/users/456

-- Verify user is gone
SELECT COUNT(*) FROM users WHERE id = 456;
-- Should return 0

-- Verify cascade: related orders should also be handled
-- Option A: Hard delete — orders also deleted
SELECT COUNT(*) FROM orders WHERE user_id = 456;
-- Should return 0

-- Option B: Soft delete — user marked deleted, orders retained
SELECT deleted_at, status FROM users WHERE id = 456;
-- deleted_at should not be NULL
-- status might be 'DELETED'

-- Option C: Orphaned data — BUG!
SELECT COUNT(*) FROM orders WHERE user_id = 456;
-- Should NOT return any rows if user is deleted without cascade
```

### Q31. Check for data consistency after a bulk operation.
```sql
-- After batch job: "apply 10% discount to all orders over $200"

-- Verify count of updated records
SELECT COUNT(*) AS affected_orders
FROM orders 
WHERE original_amount > 200;

-- Verify the discount was applied correctly
SELECT 
    id,
    original_amount,
    discount_amount,
    final_amount,
    (original_amount * 0.10) AS expected_discount
FROM orders
WHERE original_amount > 200
AND ABS(discount_amount - (original_amount * 0.10)) > 0.01;
-- Should return 0 rows — any rows = calculation bug
```

### Q32. Verify status transitions are correct.
```sql
-- Order status state machine: CREATED → PROCESSING → SHIPPED → DELIVERED

-- Find orders that skipped a state (state machine bug)
SELECT * FROM orders 
WHERE status = 'DELIVERED'
AND id NOT IN (
    SELECT DISTINCT order_id FROM order_status_history 
    WHERE status = 'SHIPPED'
);
-- Delivered orders that were never SHIPPED = bug

-- Verify all status transitions in audit log
SELECT order_id, status, changed_at
FROM order_status_history
WHERE order_id = 12345
ORDER BY changed_at ASC;
```

### Q33. Find accounts with security issues.
```sql
-- Accounts with plain text passwords (critical bug!)
SELECT id, email, password
FROM users
WHERE password NOT LIKE '$2b$%'   -- not bcrypt
AND password NOT LIKE '$argon2%'  -- not argon2
AND LENGTH(password) < 60;        -- hashes are typically 60+ chars

-- Expired but still active sessions
SELECT * FROM sessions
WHERE expires_at < NOW()
AND active = 1;

-- Accounts with no failed login lockout despite many failures
SELECT user_id, COUNT(*) AS failures
FROM login_attempts
WHERE success = 0
AND attempted_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY user_id
HAVING COUNT(*) > 5;
```

---

## Q34. JDBC integration — running SQL from Java tests.

```java
import java.sql.*;

public class DatabaseHelper {
    
    private static final String URL = System.getenv("DB_URL");
    // e.g., "jdbc:mysql://localhost:3306/myapp"
    private static final String USER = System.getenv("DB_USER");
    private static final String PASS = System.getenv("DB_PASSWORD");
    
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
    
    // Verify user was created
    public boolean userExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed", e);
        }
    }
    
    // Get user by email
    public Map<String, String> getUser(String email) {
        String sql = "SELECT id, email, name, status, role FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            Map<String, String> user = new HashMap<>();
            if (rs.next()) {
                user.put("id", rs.getString("id"));
                user.put("email", rs.getString("email"));
                user.put("name", rs.getString("name"));
                user.put("status", rs.getString("status"));
                user.put("role", rs.getString("role"));
            }
            return user;
            
        } catch (SQLException e) {
            throw new RuntimeException("DB query failed", e);
        }
    }
    
    // Insert test data
    public int insertTestUser(String email, String name) {
        String sql = "INSERT INTO users (email, name, status, created_at) " +
                     "VALUES (?, ?, 'ACTIVE', NOW())";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, 
                 Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, email);
            stmt.setString(2, name);
            stmt.executeUpdate();
            
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
            
        } catch (SQLException e) {
            throw new RuntimeException("Insert failed", e);
        }
    }
    
    // Cleanup test data
    public void deleteTestUser(String email) {
        String sql = "DELETE FROM users WHERE email LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Delete failed", e);
        }
    }
}

// Usage in test
public class UserApiTest {
    
    private DatabaseHelper db = new DatabaseHelper();
    
    @Test
    public void testCreateUser() {
        // Call API
        Response response = given()
            .body("""
                {"email": "newuser@example.com", "name": "New User"}
            """)
            .post("/api/users");
        
        // Assert API response
        response.then().statusCode(201);
        int userId = response.jsonPath().getInt("id");
        
        // Assert database state
        Map<String, String> dbUser = db.getUser("newuser@example.com");
        Assert.assertEquals(dbUser.get("email"), "newuser@example.com");
        Assert.assertEquals(dbUser.get("status"), "ACTIVE");
    }
    
    @AfterMethod
    public void cleanup() {
        db.deleteTestUser("newuser@example.com");
    }
}
```

---

## Q35. JQL (JIRA Query Language) — common QA queries.

```sql
-- Find all open bugs in current sprint
project = "MYAPP" AND issuetype = Bug AND status != Done AND sprint in openSprints()

-- Bugs assigned to me
project = "MYAPP" AND issuetype = Bug AND assignee = currentUser()

-- High priority bugs created this week
project = "MYAPP" AND issuetype = Bug AND priority in (High, Critical)
AND created >= startOfWeek()

-- Unresolved issues in regression
project = "MYAPP" AND status = "In Testing" AND resolution = Unresolved
```

---

## Q36 - Q40: Additional SQL for QA

### Q36. Verify audit log completeness.
```sql
-- Every DELETE should have an audit trail
SELECT u.id, u.email
FROM users_deleted u
LEFT JOIN audit_log a ON a.entity_id = u.id AND a.action = 'DELETE'
WHERE a.id IS NULL;
-- Any rows = missing audit entries = bug
```

### Q37. Check for data truncation issues.
```sql
-- Find values near column length limits (potential truncation)
SELECT id, name, LENGTH(name) AS name_length
FROM products
WHERE LENGTH(name) >= 250  -- if VARCHAR(255)
ORDER BY LENGTH(name) DESC;

-- Find truncated values (exactly at limit = suspicious)
SELECT id, description
FROM products  
WHERE LENGTH(description) = 500;  -- if column is VARCHAR(500)
```

### Q38. Verify email uniqueness constraint works.
```sql
-- If API allows duplicate email registration — check:
SELECT email, COUNT(*) AS count
FROM users
GROUP BY email
HAVING COUNT(*) > 1;
-- Any rows = UNIQUE constraint not enforced — bug!

-- Check constraint exists
SHOW CREATE TABLE users;
-- Should see: UNIQUE KEY `uq_users_email` (`email`)
```

### Q39. Useful SQL patterns for API test validation.
```sql
-- 1. Record count before and after
SET @before_count = (SELECT COUNT(*) FROM orders);
-- Run API call
SET @after_count = (SELECT COUNT(*) FROM orders);
SELECT @after_count - @before_count AS records_added;  -- should be 1

-- 2. Verify no unexpected side effects
-- After updating order status, only updated_at should change:
SELECT id, status, amount, user_id, created_at, updated_at
FROM orders WHERE id = 789;
-- Compare with before-update snapshot

-- 3. Check all required fields populated
SELECT * FROM users
WHERE email IS NULL 
OR name IS NULL 
OR status IS NULL 
OR created_at IS NULL;
-- Should return 0 rows
```

### Q40. Interview answer: How do you use SQL in your daily QA work?

**Model Answer:**
"SQL is a core part of my API testing workflow. After every POST/PUT/DELETE call I run a corresponding SELECT to verify the data actually reached the database correctly. For example, if a POST /api/orders returns 201, I query the orders table to confirm the amount, status, user_id, and timestamps are all correct.

I use JOINs to verify relational data — like confirming that when an order is created, the inventory table decrements. I write queries to find orphaned records, duplicate entries, and NULL values that shouldn't exist. Before major test runs, I insert test data using parameterized INSERT statements from my Java code via JDBC with PreparedStatements — never string concatenation to avoid SQL injection.

For cleanup, I use DELETE with strict WHERE clauses or wrap test data setup in transactions that I ROLLBACK after the test. I also write GROUP BY / HAVING queries to detect duplicates during regression testing."

---

*End of Part 9 — SQL for QA*
*Next: Part 10 — Jenkins & CI/CD*
