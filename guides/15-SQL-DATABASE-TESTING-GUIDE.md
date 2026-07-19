# SQL & Database Testing — Complete Interview Q&A Guide

> Every concept covered as a real interview question with full answer, notes, code, and context.
> 40+ questions. Senior QA Engineer level.

---

## SECTION 1 — WHY QA ENGINEERS NEED SQL

---

**Q1: Why does a QA engineer need to know SQL? Isn't that a developer skill?**

**A:** SQL is one of the most important QA skills because API responses only tell half the story. When you call `POST /users` and get back `201 Created`, you have verified the HTTP layer — but you have not verified whether the correct data was actually written to the database, in the right columns, with the right values, linked to the right parent records.

A QA engineer without SQL can only verify:
- The API returned a response
- The response body looks correct

A QA engineer with SQL can verify:
- The correct row was inserted
- The correct columns were populated with the correct values
- Foreign key relationships are intact
- Constraints were enforced (no duplicates, no NULLs in required fields)
- Soft deletes set the `deleted_at` flag instead of physically removing the row
- Audit logs captured the expected changes
- Pagination returns the exact records from the database, not a cached or computed list

Without SQL, you are testing the surface. With SQL, you are testing the ground truth.

**Key QA use cases for SQL:**

| Scenario | Why SQL is needed |
|---|---|
| Verify POST /users | Confirm the user row exists in DB with correct values |
| Verify PATCH /orders/{id} | Confirm only the changed fields were updated |
| Verify soft delete | Confirm deleted_at was set, row not physically removed |
| Validate pagination | Confirm the correct page of records matches DB order |
| Check audit trail | Confirm audit_log table has the expected entry |
| Detect orphaned records | Find orders with no parent user (referential integrity) |
| Find duplicate data | Detect missing UNIQUE constraints |
| Test data setup | INSERT records before a test |
| Test data teardown | DELETE or ROLLBACK records after a test |
| Validate data migration | Confirm all rows transformed correctly |

---

**Q2: How do you verify that an API call correctly wrote data to the database? Walk me through the process.**

**A:** This is a two-step verification process: first call the API, then query the database to confirm the persistence layer has the correct state.

Concrete example — testing `POST /api/users`:

Step 1 — Call the API:
```http
POST /api/users
Content-Type: application/json

{
  "name": "Alice Test",
  "email": "alice.test@example.com",
  "role": "viewer"
}
```
API returns `201 Created` with body containing the new user's ID (e.g., `{ "id": 42, ... }`).

Step 2 — Query the database to verify:
```sql
SELECT id, name, email, role, status, created_at
FROM users
WHERE id = 42;
```

Expected result:
- `id` = 42 (matches API response)
- `name` = 'Alice Test' (correct spelling, no trimming issue)
- `email` = 'alice.test@example.com' (no case conversion bug)
- `role` = 'viewer' (correct default or provided value)
- `status` = 'active' (correct default applied)
- `created_at` = a timestamp within the last few seconds (not NULL, not a future date)

Failure scenarios to verify against:
- Zero rows returned = API returned 201 but never wrote to DB
- Wrong `name` or `email` = data transformation bug
- `status` is NULL = default not applied by application
- `role` is 'admin' instead of 'viewer' = incorrect value saved
- `created_at` is NULL = trigger or ORM not setting timestamp

In automated Java tests, this looks like:
```java
// Call API, get back the created ID
int userId = given().body(requestBody).post("/api/users").then()
    .statusCode(201).extract().path("id");

// Verify in DB
var row = db.query("SELECT name, email, role, status FROM users WHERE id = ?", userId);
assertEquals(1, row.size());
assertEquals("viewer", row.get(0).get("role"));
assertEquals("active", row.get(0).get("status"));
```

---

## SECTION 2 — SELECT AND FILTERING

---

**Q3: What is SELECT and how do you use the WHERE clause? Give QA examples.**

**A:** `SELECT` retrieves rows from a table. `WHERE` filters which rows are returned. These are the two most fundamental SQL operations a QA engineer uses daily.

Basic forms:
```sql
-- Get all columns from a table
SELECT * FROM users;

-- Get specific columns only
SELECT id, name, email, created_at FROM users;

-- Give columns aliases for readability
SELECT id AS user_id, name AS full_name FROM users;
```

WHERE clause with QA examples:
```sql
-- Verify a specific user exists after POST /users
SELECT * FROM users WHERE email = 'alice.test@example.com';

-- Verify all active users (for testing GET /users?status=active)
SELECT * FROM users WHERE status = 'active';

-- Find orders above a threshold (testing a business rule)
SELECT * FROM orders WHERE total_amount > 100;

-- Range check (BETWEEN is inclusive on both ends)
SELECT * FROM orders WHERE created_at BETWEEN '2024-01-01' AND '2024-01-31';

-- Match a list of values (IN)
SELECT * FROM orders WHERE status IN ('pending', 'processing', 'shipped');

-- Exclude values (NOT IN)
SELECT * FROM users WHERE role NOT IN ('admin', 'superuser');
```

**Interview note:** `WHERE` is evaluated before `SELECT`, meaning the database filters rows first, then returns only the columns you asked for. This matters for performance — always filter on indexed columns.

---

**Q4: What is AND, OR, NOT in SQL? Give examples with real test scenarios.**

**A:** These are logical operators used to combine multiple conditions in a WHERE clause.

`AND` — both conditions must be true:
```sql
-- Find active admin users (verify role assignment API)
SELECT * FROM users WHERE status = 'active' AND role = 'admin';

-- Find high-value pending orders (test filtering API)
SELECT * FROM orders WHERE status = 'pending' AND total_amount > 500;
```

`OR` — either condition must be true:
```sql
-- Find orders that are in any "active" state
SELECT * FROM orders WHERE status = 'pending' OR status = 'processing';

-- Equivalent using IN (cleaner for multiple OR on same column)
SELECT * FROM orders WHERE status IN ('pending', 'processing');
```

`NOT` — negate a condition:
```sql
-- Find all users who are not deleted
SELECT * FROM users WHERE NOT status = 'deleted';

-- Equivalent
SELECT * FROM users WHERE status != 'deleted';

-- More complex negation
SELECT * FROM orders WHERE NOT (status = 'cancelled' OR status = 'refunded');
```

**QA test scenario:** After calling `DELETE /api/users/101` (soft delete), you want to verify the user is no longer in the active list but the row still exists:
```sql
-- Row still exists
SELECT COUNT(*) FROM users WHERE id = 101;  -- should be 1

-- But user is excluded from active users
SELECT COUNT(*) FROM users WHERE id = 101 AND status = 'active';  -- should be 0
SELECT COUNT(*) FROM users WHERE id = 101 AND deleted_at IS NULL;  -- should be 0
```

---

**Q5: What is ORDER BY? When do you use it in testing?**

**A:** `ORDER BY` sorts the result set by one or more columns. It is critical in testing because many APIs return results in a specific order, and the SQL query used to verify must use the same sort.

```sql
-- Sort ascending (A-Z, oldest first, smallest first)
SELECT * FROM users ORDER BY name ASC;

-- Sort descending (Z-A, newest first, largest first)
SELECT * FROM orders ORDER BY created_at DESC;

-- Multi-column sort: primary sort by status, secondary by date
SELECT * FROM orders ORDER BY status ASC, created_at DESC;
```

**When to use it in testing:**

1. **Pagination verification** — if the API paginates by `ORDER BY created_at DESC`, your SQL must use the same ordering:
   ```sql
   SELECT id FROM orders ORDER BY created_at DESC LIMIT 10 OFFSET 0;
   -- These IDs must match the API's first page response
   ```

2. **Verifying "most recent" record** — after creating an entity, confirm your new record is the latest:
   ```sql
   SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 1;
   -- Should be the log entry your API just created
   ```

3. **Detecting sort order bugs** — call `GET /api/products?sort=price_asc` and then:
   ```sql
   SELECT id, price FROM products ORDER BY price ASC;
   -- Product order must match the API response order
   ```

**Interview note:** SQL results have no guaranteed order unless you explicitly use `ORDER BY`. Never assume rows come back in insertion order.

---

**Q6: What is LIMIT and OFFSET? How do you test API pagination using SQL?**

**A:** `LIMIT` restricts how many rows are returned. `OFFSET` skips a number of rows before starting to return results. Together they implement pagination in SQL, mirroring how APIs typically paginate their responses.

```sql
-- Page 1: first 10 rows
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10 OFFSET 0;

-- Page 2: next 10 rows (skip first 10)
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10 OFFSET 10;

-- Page 3: rows 21-30
SELECT * FROM orders ORDER BY created_at DESC LIMIT 10 OFFSET 20;

-- Formula: OFFSET = (page_number - 1) * page_size
-- Page 5 with 20 per page: OFFSET = (5 - 1) * 20 = 80
SELECT * FROM orders ORDER BY created_at DESC LIMIT 20 OFFSET 80;
```

**How to test pagination with SQL:**

1. Call `GET /api/orders?page=2&limit=10` and note the returned IDs
2. Run the equivalent SQL:
   ```sql
   SELECT id FROM orders ORDER BY created_at DESC LIMIT 10 OFFSET 10;
   ```
3. The IDs returned by SQL must exactly match the IDs in the API response

**What to verify:**
```sql
-- Total record count matches API meta.totalCount
SELECT COUNT(*) FROM orders;
-- Should match { "meta": { "totalCount": 892 } }

-- Edge case: last page
-- If total is 892 and page size is 10, the last page (90) has 2 records
SELECT COUNT(*) FROM orders ORDER BY created_at DESC LIMIT 10 OFFSET 890;
-- Should return 2 rows, not an error
```

**Common bug caught:** API uses wrong ORDER BY internally (e.g., orders by `id` instead of `created_at`), causing pagination records to not match what the SQL verification query returns.

---

## SECTION 3 — JOINS

---

**Q7: What is an INNER JOIN? Give a QA use case with a real SQL example.**

**A:** An INNER JOIN returns only the rows where there is a matching value in both tables. If a row in table A has no match in table B, it is excluded from results entirely.

```sql
SELECT u.id, u.name, o.id AS order_id, o.total_amount, o.status
FROM users u
INNER JOIN orders o ON u.id = o.user_id;
```

This returns only users who have placed at least one order, joined with their order details.

**QA use case — Verify order-user relationship after bulk data migration:**

After migrating 10,000 orders from a legacy system, you need to verify every migrated order has a corresponding valid user:
```sql
-- If this returns fewer rows than the total order count,
-- some orders have invalid user_ids (no matching user)
SELECT COUNT(*) AS matched_orders
FROM orders o
INNER JOIN users u ON o.user_id = u.id;

-- Compare to total order count
SELECT COUNT(*) AS total_orders FROM orders;
-- If matched_orders < total_orders, there are orphaned orders
```

**Multi-table INNER JOIN — verify a complex relationship:**
```sql
SELECT u.name, o.id AS order_id, o.total_amount, p.payment_method, p.status AS payment_status
FROM orders o
INNER JOIN users u ON o.user_id = u.id
INNER JOIN payments p ON o.id = p.order_id
WHERE o.status = 'completed'
  AND p.status != 'approved';
-- Should return 0 rows: completed orders should always have approved payments
```

---

**Q8: What is a LEFT JOIN? When would you use it instead of INNER JOIN?**

**A:** A LEFT JOIN returns ALL rows from the left table, plus matching rows from the right table. Where there is no match in the right table, the right table's columns appear as NULL.

```sql
SELECT u.id, u.name, o.id AS order_id, o.total_amount
FROM users u
LEFT JOIN orders o ON u.id = o.user_id;
```

Result: All users are returned. Users with no orders get `order_id = NULL, total_amount = NULL`.

**Use LEFT JOIN instead of INNER JOIN when:**
- You want to find records that have NO matching record in another table
- You want to see all records from the primary table regardless of relationship status

**QA use case — Find users who have never placed an order:**
```sql
SELECT u.id, u.name, u.email, u.created_at
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
WHERE o.id IS NULL;
-- Returns users with zero orders — useful for testing "new user" state
```

**QA use case — Find orders with no payment record (data integrity check):**
```sql
SELECT o.id AS order_id, o.total_amount, o.status, o.created_at
FROM orders o
LEFT JOIN payments p ON o.id = p.order_id
WHERE p.id IS NULL
  AND o.status != 'draft';
-- Non-draft orders should always have a payment record
-- Any rows returned = data integrity violation
```

**Interview note:** LEFT JOIN is the most commonly used JOIN in QA work because "find X that has no Y" is an extremely frequent data integrity check pattern.

---

**Q9: What is a RIGHT JOIN? When would you use it?**

**A:** A RIGHT JOIN returns ALL rows from the right table, plus matching rows from the left table. Where there is no match in the left table, the left table's columns appear as NULL.

```sql
SELECT u.id, u.name, o.id AS order_id, o.amount
FROM users u
RIGHT JOIN orders o ON u.id = o.user_id;
```

Result: All orders are returned. Orders with no matching user (orphaned orders) get `u.id = NULL, u.name = NULL`.

**QA use case — Find orphaned orders (orders with no valid user):**
```sql
SELECT o.id AS order_id, o.user_id, o.total_amount, o.created_at
FROM users u
RIGHT JOIN orders o ON u.id = o.user_id
WHERE u.id IS NULL;
-- Returns orders whose user_id does not match any user in the users table
-- Expected: 0 rows (referential integrity intact)
-- Failure: any rows = foreign key constraint missing or cascade delete not working
```

**Practical note:** Most QA engineers rewrite RIGHT JOINs as LEFT JOINs by swapping table order — it is more readable and achieves the same result:
```sql
-- These are equivalent:
SELECT o.id FROM users u RIGHT JOIN orders o ON u.id = o.user_id WHERE u.id IS NULL;
SELECT o.id FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE u.id IS NULL;
```

---

**Q10: How do you detect orphaned records using SQL? What is an orphaned record?**

**A:** An orphaned record is a child record that references a parent record that no longer exists. For example, an `orders` row with `user_id = 500` when no user with `id = 500` exists in the `users` table.

Orphaned records indicate:
- Missing or disabled foreign key constraints
- A bug in the delete cascade logic
- Direct database manipulation that bypassed application code

**SQL to detect orphaned orders:**
```sql
SELECT o.id AS order_id, o.user_id, o.total_amount
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
WHERE u.id IS NULL;
-- Expected: 0 rows
```

**SQL to detect orphaned order_items:**
```sql
SELECT oi.id AS item_id, oi.order_id, oi.product_id
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.id
WHERE o.id IS NULL;
-- Expected: 0 rows
```

**SQL to detect orphaned payments:**
```sql
SELECT p.id, p.order_id, p.amount
FROM payments p
LEFT JOIN orders o ON p.order_id = o.id
WHERE o.id IS NULL;
-- Expected: 0 rows
```

**When to run these checks:**
- After a data migration
- After testing a delete/cascade operation
- After bulk data seeding
- As part of a data integrity regression suite

---

## SECTION 4 — GROUP BY, HAVING, AGGREGATES

---

**Q11: What is GROUP BY? Give a QA example.**

**A:** `GROUP BY` groups rows that share the same value in a column, and aggregate functions (COUNT, SUM, AVG, MAX, MIN) then compute a value for each group.

```sql
-- Count how many orders exist per status
SELECT status, COUNT(*) AS order_count
FROM orders
GROUP BY status
ORDER BY order_count DESC;
```

Result:
```
status      | order_count
------------|-------------
completed   | 890
pending     | 45
processing  | 23
cancelled   | 15
```

**QA use case — Verify API filter totals match the database:**
The API `GET /api/orders?status=pending` returns 45 results. Verify:
```sql
SELECT status, COUNT(*) AS count
FROM orders
GROUP BY status
HAVING status = 'pending';
-- Should return 45 — matches API response count
```

**QA use case — Verify revenue totals per user:**
```sql
SELECT user_id, COUNT(*) AS order_count, SUM(total_amount) AS total_spent
FROM orders
WHERE status = 'completed'
GROUP BY user_id
ORDER BY total_spent DESC;
-- Cross-check against the "User Spending Report" API endpoint
```

**QA use case — Count orders per day to verify time-based reporting:**
```sql
SELECT DATE(created_at) AS order_date, COUNT(*) AS daily_count
FROM orders
WHERE created_at >= '2024-01-01' AND created_at < '2024-02-01'
GROUP BY DATE(created_at)
ORDER BY order_date;
```

---

**Q12: What is HAVING? How is it different from WHERE?**

**A:** Both filter rows, but they apply at different stages:

- `WHERE` filters **individual rows** before grouping happens
- `HAVING` filters **groups** after GROUP BY has been applied

You cannot use aggregate functions (COUNT, SUM, etc.) in a WHERE clause — that is what HAVING is for.

```sql
-- WHERE filters rows BEFORE grouping
SELECT status, COUNT(*) AS count
FROM orders
WHERE created_at >= '2024-01-01'   -- filter: only January orders
GROUP BY status                     -- then group
HAVING COUNT(*) > 10;              -- then filter: only statuses with more than 10 orders
```

**QA example — Find email addresses that appear more than once (duplicate check):**
```sql
SELECT email, COUNT(*) AS occurrence_count
FROM users
GROUP BY email
HAVING COUNT(*) > 1
ORDER BY occurrence_count DESC;
-- Expected: 0 rows (email should be unique)
-- Any results = duplicate email addresses exist, UNIQUE constraint may be missing
```

**QA example — Find users who have spent more than $1000 (for loyalty tier testing):**
```sql
SELECT user_id, SUM(total_amount) AS total_spent
FROM orders
WHERE status = 'completed'
GROUP BY user_id
HAVING SUM(total_amount) > 1000;
-- Verify these users have the correct loyalty tier in the users table
```

**Key rule:** If you can use WHERE, use WHERE — it is faster because it reduces the number of rows before grouping. Use HAVING only when you need to filter on an aggregate value.

---

**Q13: How do you count records and verify totals using SQL?**

**A:** Use `COUNT(*)` for rows, `SUM()` for totals, and always compare against what the API reports.

```sql
-- Count all rows in a table
SELECT COUNT(*) FROM users;

-- Count rows matching a condition
SELECT COUNT(*) FROM users WHERE status = 'active';

-- Count non-NULL values in a column (ignores NULLs)
SELECT COUNT(email) FROM users;

-- Count distinct values
SELECT COUNT(DISTINCT user_id) FROM orders;

-- Sum of a numeric column
SELECT SUM(total_amount) FROM orders WHERE status = 'completed';

-- Average, min, max
SELECT AVG(total_amount), MIN(total_amount), MAX(total_amount) FROM orders;
```

**QA verification pattern:**
```sql
-- API claims GET /api/orders returns { meta: { total: 892 } }
-- Verify:
SELECT COUNT(*) AS total FROM orders;
-- Must equal 892

-- API claims GET /api/users?status=active returns 150 users
SELECT COUNT(*) FROM users WHERE status = 'active' AND deleted_at IS NULL;
-- Must equal 150
```

**Multi-column verification:**
```sql
SELECT
  COUNT(*) AS total_orders,
  COUNT(CASE WHEN status = 'completed' THEN 1 END) AS completed,
  COUNT(CASE WHEN status = 'pending' THEN 1 END) AS pending,
  SUM(CASE WHEN status = 'completed' THEN total_amount ELSE 0 END) AS completed_revenue
FROM orders;
```

---

**Q14: How do you find duplicate records in a table?**

**A:** Use `GROUP BY` on the column(s) that should be unique, then use `HAVING COUNT(*) > 1` to find groups with more than one occurrence.

**Find duplicate emails:**
```sql
SELECT email, COUNT(*) AS count
FROM users
GROUP BY email
HAVING COUNT(*) > 1
ORDER BY count DESC;
```

**Find the actual duplicate rows (not just the email):**
```sql
SELECT id, name, email, created_at
FROM users
WHERE email IN (
  SELECT email FROM users
  GROUP BY email
  HAVING COUNT(*) > 1
)
ORDER BY email, created_at;
```

**Find duplicate order_items (same order and product should not appear twice):**
```sql
SELECT order_id, product_id, COUNT(*) AS duplicate_count
FROM order_items
GROUP BY order_id, product_id
HAVING COUNT(*) > 1;
-- Expected: 0 rows
```

**Find completely identical rows:**
```sql
SELECT name, email, role, status, COUNT(*) AS row_count
FROM users
GROUP BY name, email, role, status
HAVING COUNT(*) > 1;
```

**QA context:** Duplicate detection is critical after bulk imports, registration testing (rapid double-submits), and migration testing. A missing UNIQUE constraint will show up here before it shows up in production.

---

## SECTION 5 — NULL HANDLING

---

**Q15: What is NULL in SQL? What is the "NULL trap"?**

**A:** NULL in SQL represents the absence of a value — it is not zero, not an empty string, and not false. It literally means "unknown" or "not applicable."

The NULL trap is a set of counterintuitive behaviours that catch both developers and QA engineers:

1. **Arithmetic with NULL always returns NULL:**
   ```sql
   SELECT 5 + NULL;     -- returns NULL (not 5)
   SELECT NULL * 100;   -- returns NULL (not 0)
   ```

2. **Comparison with NULL always returns NULL (not true or false):**
   ```sql
   SELECT NULL = NULL;   -- returns NULL (not TRUE!)
   SELECT NULL != 5;     -- returns NULL (not TRUE)
   ```

3. **NULL in NOT IN breaks the entire query** (covered in Q17)

4. **COUNT(column) ignores NULLs:**
   ```sql
   -- Table has 5 rows. 2 rows have email = NULL.
   SELECT COUNT(*) FROM users;       -- returns 5
   SELECT COUNT(email) FROM users;   -- returns 3 (ignores NULL)
   ```

5. **Aggregates ignore NULLs:**
   ```sql
   -- Scores: 10, 20, NULL, 30
   SELECT AVG(score) FROM tests;  -- returns 20 (average of 10, 20, 30 — NULL ignored)
   ```

**QA implication:** When verifying that a required field is always populated, use `COUNT(*)` vs `COUNT(column)` discrepancy as a check, or directly check for NULLs with `IS NULL`.

---

**Q16: What is the difference between IS NULL and = NULL?**

**A:** This is one of the most important rules in SQL and a common interview trap:

- `= NULL` **never works** — it always returns 0 rows, even when NULLs exist
- `IS NULL` is the correct way to check for NULL values

```sql
-- WRONG — always returns 0 rows, even if phone column has NULLs
SELECT * FROM users WHERE phone = NULL;

-- ALSO WRONG — always returns 0 rows
SELECT * FROM users WHERE phone != NULL;

-- CORRECT — finds rows where phone has no value
SELECT * FROM users WHERE phone IS NULL;

-- CORRECT — finds rows where phone has any value
SELECT * FROM users WHERE phone IS NOT NULL;
```

**Why does this happen?**
NULL represents "unknown." The expression `NULL = NULL` asks "is unknown equal to unknown?" — the answer is "unknown" (NULL), not TRUE. SQL cannot evaluate equality with an unknown value.

**QA scenarios where this matters:**
```sql
-- Verify deleted_at was set after soft delete
SELECT id FROM users WHERE id = 101 AND deleted_at IS NOT NULL;
-- If you wrote deleted_at != NULL this would return 0 rows even when it IS set

-- Find users with no phone on file
SELECT id, name FROM users WHERE phone IS NULL;

-- Verify all orders have a shipping address (required field check)
SELECT COUNT(*) FROM orders WHERE shipping_address IS NULL;
-- Expected: 0
```

---

**Q17: How does NOT IN behave with NULL values? What is the gotcha?**

**A:** This is one of the most dangerous traps in SQL. If a subquery used with `NOT IN` returns even a single NULL value, the entire `NOT IN` expression returns NO rows — even rows that logically should match.

**Example:**
```sql
-- Table: orders has rows with user_id: 1, 2, NULL, 4

-- This looks like it should find users not in the orders table:
SELECT * FROM users WHERE id NOT IN (SELECT user_id FROM orders);

-- But if any user_id in orders is NULL, this returns 0 rows for ALL users!
-- Because NOT IN is equivalent to: id != 1 AND id != 2 AND id != NULL AND id != 4
-- AND id != NULL is always NULL (unknown), which makes the whole AND chain NULL/false
```

**Fix — always add `WHERE column IS NOT NULL` in the subquery:**
```sql
SELECT * FROM users 
WHERE id NOT IN (
  SELECT user_id FROM orders WHERE user_id IS NOT NULL
);
-- Now safe — NULLs are excluded from the NOT IN list
```

**Alternative — use NOT EXISTS (safer and more efficient):**
```sql
SELECT * FROM users u
WHERE NOT EXISTS (
  SELECT 1 FROM orders o WHERE o.user_id = u.id
);
-- NOT EXISTS handles NULLs correctly and is generally more performant
```

**When to remember this:** Any time you write `NOT IN (SELECT ...)` — always add `WHERE column IS NOT NULL` to the subquery. This is a common source of subtle test failures where a query unexpectedly returns 0 rows.

---

**Q18: What is COALESCE and when do you use it in database testing?**

**A:** `COALESCE` returns the first non-NULL value from a list of arguments. It is used to handle NULLs gracefully — replacing them with a default value for display or calculation.

```sql
-- Syntax: COALESCE(value1, value2, value3, ...)
-- Returns the first non-NULL value

-- Replace NULL discount with 0
SELECT id, price, COALESCE(discount, 0) AS discount FROM products;

-- Use first available display name
SELECT id, COALESCE(nickname, first_name, 'Unknown') AS display_name FROM users;

-- Prevent NULL in calculations
SELECT id, price * (1 - COALESCE(discount_rate, 0)) AS final_price FROM products;
```

**QA use cases:**

1. **Verify default values are applied by the application:**
   ```sql
   SELECT id, COALESCE(status, 'NO_DEFAULT') AS status_check FROM users;
   -- If any row shows 'NO_DEFAULT', the application is not setting the default status
   ```

2. **Verify soft-delete status display:**
   ```sql
   SELECT id, name, COALESCE(deleted_at, 'active') AS deletion_status
   FROM users WHERE id IN (101, 102, 103);
   ```

3. **Safe calculation without division by zero:**
   ```sql
   SELECT order_id, total / NULLIF(item_count, 0) AS avg_item_price
   FROM orders;
   -- NULLIF returns NULL when item_count = 0, preventing division by zero error
   ```

---

## SECTION 6 — STRING FUNCTIONS

---

**Q19: What string functions do QA engineers use most often? Cover LIKE, UPPER, LOWER, TRIM, LENGTH, CONCAT.**

**A:** String functions allow you to search, transform, and validate text data in the database.

**LIKE — pattern matching:**
```sql
-- % matches any sequence of characters
SELECT * FROM users WHERE email LIKE '%@gmail.com';     -- ends with @gmail.com
SELECT * FROM users WHERE name LIKE 'A%';               -- starts with A
SELECT * FROM users WHERE name LIKE '%Smith%';          -- contains Smith

-- _ matches exactly one character
SELECT * FROM products WHERE sku LIKE 'PROD-___';       -- PROD- followed by exactly 3 chars

-- QA use: Find all test data rows (tagged with test domain)
SELECT * FROM users WHERE email LIKE '%@qa.com%';

-- QA use: Validate email format (basic check)
SELECT id, email FROM users WHERE email NOT LIKE '%_@_%._%';
-- Finds emails that don't match the pattern: something@something.tld
```

**UPPER / LOWER — case conversion:**
```sql
-- Normalize for case-insensitive comparison
SELECT * FROM users WHERE LOWER(email) = 'alice@example.com';
-- Finds alice@example.com, ALICE@EXAMPLE.COM, Alice@Example.COM

-- QA use: verify email is stored in lowercase (if that is the requirement)
SELECT id, email FROM users WHERE email != LOWER(email);
-- Any rows returned = email stored with uppercase characters — potential bug
```

**TRIM — remove leading/trailing whitespace:**
```sql
SELECT TRIM('  Alice  ');    -- 'Alice'
SELECT LTRIM('  Alice');     -- 'Alice' (left only)
SELECT RTRIM('Alice  ');     -- 'Alice' (right only)

-- QA use: detect data with accidental whitespace (common import bug)
SELECT id, name FROM users WHERE name != TRIM(name);
-- Any rows = names have leading or trailing spaces — validation bug
```

**LENGTH — string length:**
```sql
-- MySQL/PostgreSQL
SELECT id, name, LENGTH(name) AS name_length FROM users;

-- QA use: verify field length constraints are enforced
SELECT id, name FROM users WHERE LENGTH(name) > 100;
-- If the DB column is VARCHAR(100), these rows indicate a constraint bypass

-- Verify phone numbers are correct length (10 digits)
SELECT id, phone FROM users WHERE LENGTH(REPLACE(phone, '-', '')) != 10;
```

**CONCAT — combine strings:**
```sql
SELECT CONCAT(first_name, ' ', last_name) AS full_name FROM users;

-- QA use: build test assertions
SELECT CONCAT('User ', id, ': ', name, ' (', email, ')') AS user_info FROM users;
```

---

## SECTION 7 — DATE FUNCTIONS

---

**Q20: What date functions do QA engineers commonly use? Cover NOW(), DATE(), DATEDIFF().**

**A:** Date functions are essential for verifying time-sensitive data like creation timestamps, expiry dates, session lengths, and scheduled events.

**Getting current date and time:**
```sql
SELECT NOW();           -- '2024-01-15 14:30:22' — MySQL/PostgreSQL
SELECT CURDATE();       -- '2024-01-15' — MySQL date only
SELECT CURRENT_DATE;    -- PostgreSQL/standard SQL
SELECT GETDATE();       -- SQL Server
```

**Extracting parts of a date:**
```sql
SELECT DATE(created_at) FROM orders;        -- '2024-01-15' (date part only)
SELECT YEAR(created_at) FROM orders;        -- 2024
SELECT MONTH(created_at) FROM orders;       -- 1
SELECT DAY(created_at) FROM orders;         -- 15
SELECT HOUR(created_at) FROM orders;        -- 14
```

**Date arithmetic:**
```sql
-- MySQL
SELECT DATE_ADD(NOW(), INTERVAL 7 DAY);      -- 7 days in the future
SELECT DATE_SUB(NOW(), INTERVAL 30 DAY);     -- 30 days ago

-- PostgreSQL
SELECT NOW() + INTERVAL '7 days';
SELECT NOW() - INTERVAL '30 days';
```

**DATEDIFF:**
```sql
-- MySQL: returns number of days between two dates
SELECT DATEDIFF('2024-01-31', '2024-01-01');   -- 30

-- SQL Server: specify unit
SELECT DATEDIFF(day, '2024-01-01', '2024-01-31');  -- 30
```

**QA use cases:**
```sql
-- Find records created in the last 24 hours (verify recent API calls)
SELECT * FROM orders WHERE created_at >= NOW() - INTERVAL 1 DAY;    -- MySQL
SELECT * FROM orders WHERE created_at >= NOW() - INTERVAL '1 day';  -- PostgreSQL

-- Verify records created today (testing daily batch jobs)
SELECT * FROM orders WHERE DATE(created_at) = CURDATE();

-- Check password reset tokens are not expired (e.g., 24-hour expiry)
SELECT id, token, expires_at FROM password_resets
WHERE expires_at < NOW();
-- Any rows = expired tokens that should have been cleaned up

-- Verify session timeout is correctly enforced (30-minute sessions)
SELECT id FROM sessions
WHERE last_activity < NOW() - INTERVAL 30 MINUTE AND status = 'active';
-- Should return 0 — active sessions older than 30 min should be expired
```

---

## SECTION 8 — SUBQUERIES AND IN vs EXISTS

---

**Q21: What is a subquery? Give a QA example.**

**A:** A subquery is a query nested inside another query. The inner query runs first and its result is used by the outer query. Subqueries allow you to express complex filtering logic that cannot be done with a single-level query.

**Subquery in WHERE — find users who have placed at least one order:**
```sql
SELECT id, name, email FROM users
WHERE id IN (SELECT DISTINCT user_id FROM orders);
```

**Subquery in WHERE — find users who have NEVER placed an order:**
```sql
SELECT id, name, email FROM users
WHERE id NOT IN (
  SELECT DISTINCT user_id FROM orders WHERE user_id IS NOT NULL
);
-- Note: always add WHERE user_id IS NOT NULL to subquery used with NOT IN
```

**Correlated subquery — users whose last order was over 6 months ago:**
```sql
SELECT u.id, u.name, u.email
FROM users u
WHERE (
  SELECT MAX(created_at) FROM orders o WHERE o.user_id = u.id
) < DATE_SUB(NOW(), INTERVAL 6 MONTH);
```
A correlated subquery runs once per row of the outer query — it references the outer query's row (`u.id`). Useful but slower than a JOIN for large datasets.

**Derived table (subquery in FROM):**
```sql
-- Average order count per user
SELECT AVG(order_count) AS avg_orders_per_user
FROM (
  SELECT user_id, COUNT(*) AS order_count
  FROM orders
  GROUP BY user_id
) AS user_counts;
```

**QA context:** Use subqueries to build complex verification queries that check cross-table business rules without having to write multiple separate queries.

---

**Q22: What is the difference between IN and EXISTS? When do you use each?**

**A:** Both check for the existence of related records, but they work differently and have different performance characteristics.

**IN** — returns the full subquery result set, then checks membership:
```sql
SELECT id, name FROM users
WHERE id IN (SELECT user_id FROM orders WHERE status = 'completed');
-- Executes inner query once, gets all matching user_ids, then filters users
```

**EXISTS** — stops at the first matching row (short-circuit):
```sql
SELECT id, name FROM users u
WHERE EXISTS (
  SELECT 1 FROM orders o WHERE o.user_id = u.id AND o.status = 'completed'
);
-- For each user, checks if even one matching order exists, stops immediately when found
```

**When to use which:**

| Situation | Recommended | Why |
|---|---|---|
| Small result set from subquery | IN | Simple, readable |
| Large result set from subquery | EXISTS | More efficient — stops at first match |
| NULL values might be in subquery | EXISTS | Safer — EXISTS handles NULLs correctly |
| NOT IN scenario with NULLs | NOT EXISTS | NOT IN fails with NULLs; NOT EXISTS is safe |
| Multiple columns to compare | EXISTS | IN can only compare one column (unless tuples) |

**NOT EXISTS — the safe alternative to NOT IN:**
```sql
-- SAFER than NOT IN when NULLs may exist:
SELECT id, name FROM users u
WHERE NOT EXISTS (
  SELECT 1 FROM orders o WHERE o.user_id = u.id
);
-- Returns users with zero orders — correctly handles any NULLs in orders.user_id
```

---

## SECTION 9 — INSERT, UPDATE, DELETE, TRANSACTIONS

---

**Q23: What is INSERT? How do you create test data with SQL?**

**A:** `INSERT` adds new rows to a table. QA engineers use INSERT to set up preconditions before a test — creating the specific data state needed to exercise a particular code path.

```sql
-- Insert a single row
INSERT INTO users (name, email, role, status, created_at)
VALUES ('QA Test User', 'qa_test_001@qa.com', 'viewer', 'active', NOW());

-- Insert multiple rows in one statement (faster than multiple INSERTs)
INSERT INTO users (name, email, role, status) VALUES
  ('Test Admin',   'test_admin@qa.com',   'admin',  'active'),
  ('Test Viewer',  'test_viewer@qa.com',  'viewer', 'active'),
  ('Test Inactive','test_inactive@qa.com','viewer', 'inactive');

-- Insert and immediately get the generated ID (MySQL)
INSERT INTO users (name, email) VALUES ('New User', 'new@qa.com');
SELECT LAST_INSERT_ID();   -- returns the auto-generated ID

-- PostgreSQL: RETURNING clause
INSERT INTO users (name, email) VALUES ('New User', 'new@qa.com')
RETURNING id;
```

**Best practice for test data — use a recognisable pattern so you can clean up:**
```sql
-- Use a test-specific email domain
INSERT INTO users (name, email) VALUES ('Test User', 'test_auto_20240115@qa.com');

-- Later, clean up all test data easily
DELETE FROM users WHERE email LIKE 'test_auto_%@qa.com';
```

---

**Q24: What is UPDATE? How do you fix or adjust test data?**

**A:** `UPDATE` modifies existing rows. In testing, you use UPDATE to set up specific data states that are difficult to reach through the API, or to reset data to a known state between tests.

```sql
-- Update a single column
UPDATE users SET status = 'inactive' WHERE id = 101;

-- Update multiple columns at once
UPDATE orders
SET status = 'shipped', shipped_at = NOW(), tracking_number = 'TRK-TEST-123'
WHERE id = 50;

-- Update with a calculation
UPDATE products SET price = price * 0.9 WHERE category = 'Electronics';

-- Update based on a subquery
UPDATE users SET role = 'premium'
WHERE id IN (
  SELECT user_id FROM orders
  GROUP BY user_id
  HAVING SUM(total_amount) > 1000
);
```

**Critical rule: ALWAYS include a WHERE clause in UPDATE.** Without WHERE, every single row in the table is updated:
```sql
-- DANGER: Updates every user's status to inactive
UPDATE users SET status = 'inactive';

-- CORRECT: Updates only the specific user
UPDATE users SET status = 'inactive' WHERE id = 101;
```

**QA use case — reset a user's failed login count before testing lockout logic:**
```sql
UPDATE users SET failed_login_attempts = 0, locked_until = NULL WHERE id = 101;
-- Reset state to test the lockout flow from scratch
```

---

**Q25: What is DELETE? How do you clean up test data?**

**A:** `DELETE` removes rows from a table. QA engineers use DELETE in test teardown to remove test data created during a test, keeping the database clean for subsequent tests.

```sql
-- Delete a specific row
DELETE FROM users WHERE id = 101;

-- Delete using a pattern (test data cleanup)
DELETE FROM users WHERE email LIKE 'test_auto_%@qa.com';

-- Delete with a date condition
DELETE FROM sessions WHERE created_at < NOW() - INTERVAL 7 DAY;

-- Delete related records (must respect foreign key order)
-- Delete child records before parent records
DELETE FROM order_items WHERE order_id IN (
  SELECT id FROM orders WHERE user_id = 101
);
DELETE FROM orders WHERE user_id = 101;
DELETE FROM users WHERE id = 101;
```

**Critical rule: ALWAYS include a WHERE clause in DELETE.** Without WHERE, every row in the table is deleted:
```sql
-- DANGER: Deletes every row in the table
DELETE FROM users;

-- CORRECT: Deletes only the test user
DELETE FROM users WHERE email = 'test_auto@qa.com';
```

**MySQL syntax for DELETE with JOIN:**
```sql
DELETE u FROM users u
INNER JOIN test_sessions ts ON u.id = ts.user_id
WHERE ts.is_test = TRUE;
```

---

**Q26: What is the difference between TRUNCATE and DELETE?**

**A:** Both remove rows from a table, but they work very differently:

| Aspect | TRUNCATE | DELETE |
|---|---|---|
| WHERE clause | Not supported — removes ALL rows | Supported — can remove specific rows |
| Speed | Very fast — drops and recreates pages | Slower — logs each row deletion |
| Rollback | Cannot be rolled back in most databases (DDL) | Can be rolled back within a transaction |
| Resets AUTO_INCREMENT | Yes — ID counter resets to 1 | No — counter continues from where it was |
| Fires triggers | No (row-level triggers do not fire) | Yes — DELETE triggers fire for each row |
| Foreign key check | Fails if FK constraints reference the table | Fails if FK constraints reference the rows |
| Logged | Minimal logging (page deallocation) | Fully logged (every row) |

```sql
-- TRUNCATE — removes all rows fast, resets ID sequence
TRUNCATE TABLE test_users;

-- DELETE without WHERE — removes all rows but logs each one
DELETE FROM test_users;

-- DELETE with WHERE — removes only matching rows
DELETE FROM users WHERE email LIKE '%@qa.com%';
```

**QA decision guide:**
- Use `TRUNCATE` to fully reset a test table between test suites (fast, resets IDs to 1)
- Use `DELETE WHERE email LIKE 'test_%'` to clean up only the specific test data created by a test run
- Never TRUNCATE a table with foreign key constraints without disabling them first

---

**Q27: What is a transaction in SQL? What are COMMIT and ROLLBACK?**

**A:** A transaction is a sequence of SQL operations treated as a single logical unit. Either all operations succeed (COMMIT) or all are undone (ROLLBACK). No partial updates are possible within a transaction.

```sql
-- Start a transaction
BEGIN;  -- or START TRANSACTION in MySQL

-- Perform multiple operations
UPDATE accounts SET balance = balance - 100 WHERE id = 1;  -- debit Alice
UPDATE accounts SET balance = balance + 100 WHERE id = 2;  -- credit Bob

-- If both succeed, make it permanent
COMMIT;

-- If anything goes wrong, undo everything
ROLLBACK;
-- Both accounts return to their original balances
```

**QA use case — wrap test data in a transaction for clean isolation:**
```sql
BEGIN;

-- Set up test state
INSERT INTO users (name, email) VALUES ('Temp Test User', 'temp@test.com');
UPDATE orders SET status = 'processing' WHERE id = 50;

-- Run test verifications...
SELECT * FROM users WHERE email = 'temp@test.com';

-- After test, undo all changes regardless of pass/fail
ROLLBACK;
-- Database is exactly as it was before the test started
```

This pattern means you never need cleanup code — the ROLLBACK handles it all.

---

**Q28: What is ACID? Explain each property with a test scenario.**

**A:** ACID stands for Atomicity, Consistency, Isolation, and Durability — the four properties that guarantee database transactions are processed reliably.

**Atomicity — all or nothing:**
A transaction is indivisible. Either all operations within it succeed, or none of them are applied.

```sql
BEGIN;
  UPDATE accounts SET balance = balance - 500 WHERE id = 1;
  -- If this second UPDATE fails (e.g., account 2 doesn't exist):
  UPDATE accounts SET balance = balance + 500 WHERE id = 999;
ROLLBACK;  -- both changes undone — Alice gets her money back
```

QA test: Trigger a failure in the middle of a multi-step transaction. Verify the first operation was also rolled back.

**Consistency — rules are always enforced:**
A transaction brings the database from one valid state to another. Constraints (UNIQUE, NOT NULL, CHECK, FK) are never violated.

QA test: Attempt to insert a user with a duplicate email. Verify the transaction is rejected and the duplicate does not exist in the DB.

**Isolation — concurrent transactions do not interfere:**
Each transaction executes as if it were the only one running. Intermediate states are invisible to other transactions.

QA test: Simulate two concurrent users booking the last available seat. Verify only one succeeds — the second gets a conflict/unavailable error.

**Durability — committed data survives crashes:**
Once COMMIT executes, the data is permanently saved — even if the system crashes the next millisecond.

QA test: Commit a transaction, simulate a database restart, verify the committed data is still there.

**Why ACID matters for QA:**
- Tests running in parallel can corrupt each other's data if transactions are not isolated
- Using `BEGIN/ROLLBACK` in test teardown exploits Atomicity and isolation to keep tests independent
- ACID guarantees give you confidence that your test data setup is deterministic

---

**Q29: How do you verify referential integrity using SQL?**

**A:** Referential integrity means every foreign key value references a valid primary key in the parent table. You verify it using LEFT JOINs to find records with no matching parent.

**Check 1 — Orders with no valid user:**
```sql
SELECT o.id, o.user_id, o.total_amount
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
WHERE u.id IS NULL;
-- Expected: 0 rows
```

**Check 2 — Order items with no valid order:**
```sql
SELECT oi.id, oi.order_id
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.id
WHERE o.id IS NULL;
-- Expected: 0 rows
```

**Check 3 — Payments with no valid order:**
```sql
SELECT p.id, p.order_id, p.amount
FROM payments p
LEFT JOIN orders o ON p.order_id = o.id
WHERE o.id IS NULL;
-- Expected: 0 rows
```

**Check 4 — After cascade delete, verify children were removed:**
```sql
-- After deleting user 101:
SELECT COUNT(*) FROM orders WHERE user_id = 101;
-- With CASCADE DELETE: should be 0
-- With NO CASCADE: FK violation should have prevented the user delete
-- Without FK constraint: orders may still exist (orphaned) — this is the bug
```

**Verify schema has FK constraints defined:**
```sql
-- PostgreSQL
SELECT tc.constraint_name, tc.table_name, kcu.column_name,
       ccu.table_name AS referenced_table, ccu.column_name AS referenced_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.referential_constraints rc ON tc.constraint_name = rc.constraint_name
JOIN information_schema.constraint_column_usage ccu ON rc.unique_constraint_name = ccu.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY';
```

---

## SECTION 10 — REAL QA SQL SCENARIOS

---

**Q30: Give 10 real QA SQL scenarios with complete queries. These are the kinds of situations you face daily.**

**A:**

**Scenario 1 — Verify user was created after POST /api/users:**
```sql
SELECT id, name, email, role, status, created_at
FROM users
WHERE email = 'alice.test@example.com'
  AND created_at >= NOW() - INTERVAL 1 MINUTE;
-- Expected: 1 row with correct values
-- Failure: 0 rows = API returned 201 but never wrote to DB
```

**Scenario 2 — Verify no orphaned orders (referential integrity check):**
```sql
SELECT o.id AS order_id, o.user_id, o.total_amount
FROM orders o
LEFT JOIN users u ON o.user_id = u.id
WHERE u.id IS NULL;
-- Expected: 0 rows
-- Failure: rows returned = orders exist with invalid user_id
```

**Scenario 3 — Verify payment amount in DB matches what API returned:**
```sql
SELECT
  o.id,
  o.total_amount AS db_total,
  99.99 AS api_total,
  ABS(o.total_amount - 99.99) AS difference
FROM orders o
WHERE o.id = 12345;
-- Expected: difference = 0.00
-- Failure: any difference > 0.01 = rounding or calculation bug
```

**Scenario 4 — Find duplicate email addresses after bulk import:**
```sql
SELECT email, COUNT(*) AS count
FROM users
GROUP BY email
HAVING COUNT(*) > 1
ORDER BY count DESC;
-- Expected: 0 rows
-- Failure: any rows = UNIQUE constraint missing or not enforced
```

**Scenario 5 — Verify API filter count matches database:**
```sql
-- API GET /orders?status=pending returned 45 results
SELECT COUNT(*) AS pending_count FROM orders WHERE status = 'pending';
-- Expected: 45
```

**Scenario 6 — Verify soft delete sets deleted_at without removing the row:**
```sql
-- Row still exists:
SELECT COUNT(*) FROM users WHERE id = 101;    -- should be 1

-- deleted_at is set:
SELECT deleted_at FROM users WHERE id = 101;  -- should be non-NULL timestamp

-- User excluded from active queries:
SELECT COUNT(*) FROM users WHERE id = 101 AND deleted_at IS NULL;  -- should be 0
```

**Scenario 7 — Verify audit log was created after an update:**
```sql
SELECT entity_type, entity_id, action, old_value, new_value, created_at
FROM audit_logs
WHERE entity_type = 'user'
  AND entity_id = 101
  AND action = 'UPDATE'
  AND created_at >= NOW() - INTERVAL 1 MINUTE
ORDER BY created_at DESC LIMIT 1;
-- Expected: 1 row with correct old/new values
-- Failure: 0 rows = audit logging not implemented or not triggered
```

**Scenario 8 — Verify pagination returns correct records:**
```sql
-- API GET /orders?page=2&limit=10 — verify these IDs match:
SELECT id FROM orders ORDER BY created_at DESC LIMIT 10 OFFSET 10;
-- Compare these IDs to the IDs in the API response
```

**Scenario 9 — Find all failed transactions in the last 24 hours:**
```sql
SELECT t.id, t.order_id, t.amount, t.error_code, t.error_message, t.created_at, u.email
FROM transactions t
INNER JOIN orders o ON t.order_id = o.id
INNER JOIN users u ON o.user_id = u.id
WHERE t.status = 'failed'
  AND t.created_at >= NOW() - INTERVAL 24 HOUR
ORDER BY t.created_at DESC;
```

**Scenario 10 — Verify discount was correctly applied:**
```sql
SELECT
  o.id, o.subtotal, o.discount_amount, o.discount_code, o.total_amount,
  ROUND(o.subtotal * 0.20, 2) AS expected_discount,
  CASE
    WHEN ABS(o.discount_amount - ROUND(o.subtotal * 0.20, 2)) > 0.01
    THEN 'CALCULATION_ERROR'
    ELSE 'CORRECT'
  END AS check_result
FROM orders o WHERE o.id = 12345;
-- Also verify coupon was logged:
SELECT * FROM coupon_usages WHERE coupon_code = 'SAVE20' AND order_id = 12345;
```

---

## SECTION 11 — JDBC AND JAVA DATABASE TESTING

---

**Q31: What is JDBC? How do you connect Java RestAssured tests to a database?**

**A:** JDBC (Java Database Connectivity) is the standard Java API for connecting to relational databases. It allows Java test code to execute SQL queries and verify database state after API calls.

**Maven dependency:**
```xml
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <version>8.0.33</version>
</dependency>
```

**DatabaseHelper utility class:**
```java
public class DatabaseHelper {
    private static final String URL = System.getenv()
        .getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/testdb");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASS = System.getenv().getOrDefault("DB_PASSWORD", "password");

    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASS);
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    public List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++)
                    row.put(meta.getColumnName(i), rs.getObject(i));
                results.add(row);
            }
        }
        return results;
    }

    public int update(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) stmt.setObject(i + 1, params[i]);
            return stmt.executeUpdate();
        }
    }
}
```

**Using it in a JUnit test with RestAssured:**
```java
public class UserApiTest {

    private static DatabaseHelper db = new DatabaseHelper();

    @BeforeAll
    static void setup() throws Exception {
        RestAssured.baseURI = "http://localhost:8080";
        db.connect();
    }

    @AfterAll
    static void teardown() throws Exception {
        db.update("DELETE FROM users WHERE email = ?", "qa_test@example.com");
        db.disconnect();
    }

    @Test
    void postUser_shouldPersistToDatabase() throws Exception {
        // Step 1: Call the API
        int userId = given()
            .contentType("application/json")
            .body("{\"name\":\"QA Test\",\"email\":\"qa_test@example.com\",\"role\":\"viewer\"}")
        .when()
            .post("/api/users")
        .then()
            .statusCode(201)
            .extract().path("id");

        // Step 2: Verify in DB
        var rows = db.query("SELECT name, role, status FROM users WHERE id = ?", userId);
        assertEquals(1, rows.size(), "Expected 1 row in DB");
        assertEquals("viewer", rows.get(0).get("role"));
        assertEquals("active", rows.get(0).get("status"));
    }
}
```

**Why PreparedStatement?** It prevents SQL injection (parameter values are escaped) and is more efficient for repeated queries.

---

**Q32: What are the main types of database testing? Explain each.**

**A:**

**Data Integrity Testing** — verifies that stored data is accurate, complete, and meets constraints:
```sql
-- Required fields are never NULL
SELECT COUNT(*) FROM users WHERE name IS NULL OR email IS NULL;  -- expect 0

-- No negative amounts
SELECT COUNT(*) FROM orders WHERE total_amount < 0;  -- expect 0

-- Only valid status values exist
SELECT DISTINCT status FROM orders;
-- Should only contain: pending, processing, shipped, completed, cancelled

-- Email format validation
SELECT id, email FROM users
WHERE email NOT REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$';
```

**Referential Integrity Testing** — verifies foreign key relationships:
```sql
-- Every child row has a valid parent
SELECT COUNT(*) FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE u.id IS NULL;
-- Expected: 0
```

**Schema / Structural Testing** — verifies the DB structure matches the specification:
```sql
-- List columns and types
DESCRIBE users;                     -- MySQL
SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'users';

-- Check indexes
SHOW INDEX FROM orders;             -- MySQL
```

**Stored Procedure Testing** — verifies business logic in procedures:
```sql
CALL sp_calculate_loyalty_score(101);
SELECT loyalty_tier FROM users WHERE id = 101;  -- verify result
```

**Trigger Testing** — verifies triggers fire correctly:
```sql
-- Insert an order, verify audit_log trigger fired
INSERT INTO orders (user_id, total_amount, status) VALUES (1, 50.00, 'pending');
SELECT * FROM audit_logs WHERE entity_type = 'order' AND action = 'INSERT'
  AND created_at >= NOW() - INTERVAL 1 MINUTE;
-- Expected: 1 row
```

**Migration Testing** — verifies data survived a schema migration correctly:
```sql
-- Count records before and after migration
SELECT COUNT(*) FROM users;         -- run before migration, note count
-- Run migration
SELECT COUNT(*) FROM users;         -- same count after = no data loss
-- Check migrated fields
SELECT COUNT(*) FROM users WHERE new_column IS NULL;  -- 0 if backfill worked
```

---

## SECTION 12 — INTERVIEW QUESTIONS

---

**Q33: What is the difference between INNER JOIN, LEFT JOIN, and RIGHT JOIN from a QA perspective?**

**A:** INNER JOIN returns only rows with matches in both tables — use it to verify a relationship exists. LEFT JOIN returns all rows from the left table regardless of matches — use it to find records that have NO related record (e.g., users with no orders). RIGHT JOIN is the mirror — use it to find orphaned records in the right table.

In practice, most QA engineers prefer LEFT JOIN for both scenarios by swapping table order — it is more readable and universally supported. The pattern `LEFT JOIN ... WHERE right.id IS NULL` is the go-to pattern for any "find orphaned/unmatched records" check.

---

**Q34: How do NULLs behave in SQL, and what are the most common mistakes with them?**

**A:** NULL represents the absence of a value. Three critical behaviours:

1. `WHERE phone = NULL` always returns 0 rows — use `WHERE phone IS NULL`
2. `NOT IN` with a subquery that contains NULLs returns 0 rows for everything — always add `WHERE column IS NOT NULL` to the subquery
3. Aggregate functions ignore NULLs: `AVG(score)` over values `[10, NULL, 30]` returns 20, not 13.3

These are the NULL traps that cause silent test failures — your query returns 0 rows and you assume the data is clean when the query was just broken.

---

**Q35: What is the difference between TRUNCATE and DELETE, and when do you use each in testing?**

**A:** DELETE removes specific rows with a WHERE clause, fires triggers, can be rolled back, and does not reset the AUTO_INCREMENT counter. TRUNCATE removes all rows instantly, does not fire row-level triggers, cannot be rolled back (it is DDL), and resets the ID counter to 1.

In testing: use `TRUNCATE` to completely reset a test table between test suites (fast, clean slate). Use `DELETE WHERE email LIKE 'test_%'` to clean up only the specific data created by a test run without touching other rows.

---

**Q36: How do you use SQL to verify pagination is working correctly?**

**A:** Two parts: count verification and record verification.

Count: `SELECT COUNT(*) FROM orders` must equal the API's `meta.totalCount`.

Records: Replicate the API's ORDER BY and OFFSET/LIMIT:
```sql
SELECT id FROM orders ORDER BY created_at DESC LIMIT 10 OFFSET 10;
```
The returned IDs must exactly match the IDs in the API's page 2 response.

Edge cases: verify last page returns fewer records (not an error), and verify adding a record increments the total count by exactly 1.

---

**Q37: What is a transaction and why do QA engineers care about it?**

**A:** A transaction is a sequence of SQL operations that either all succeed (COMMIT) or all fail (ROLLBACK). For QA, transactions enable perfect test isolation: wrap your test data setup in `BEGIN`, run your test, then `ROLLBACK` — the database returns to exactly its prior state with no cleanup code needed. This also means tests can run in parallel against the same database without corrupting each other's data, as long as each test uses its own transaction scope.

---

**Q38: What is ACID and how does it affect your testing approach?**

**A:** ACID = Atomicity (all-or-nothing), Consistency (constraints always enforced), Isolation (concurrent transactions do not interfere), Durability (committed data persists through crashes).

For testing:
- Atomicity: test that partial failures roll back completely
- Consistency: test that constraint violations are rejected
- Isolation: test concurrent operations (booking last seat, double-submit prevention)
- Durability: verify committed data survives a restart (integration/infrastructure test)

Using `BEGIN/ROLLBACK` in automated tests directly leverages Atomicity for clean teardown.

---

**Q39: Explain the NOT IN + NULL gotcha. How do you avoid it?**

**A:** If a subquery used with `NOT IN` returns any NULL values, the entire result set is empty — even rows that should logically match. This is because `NOT IN` expands to a chain of `!=` comparisons, and `column != NULL` is always NULL (unknown), making the whole chain false.

```sql
-- BROKEN if orders.user_id has any NULLs:
SELECT * FROM users WHERE id NOT IN (SELECT user_id FROM orders);

-- FIXED:
SELECT * FROM users WHERE id NOT IN (
  SELECT user_id FROM orders WHERE user_id IS NOT NULL
);

-- BEST PRACTICE — use NOT EXISTS instead:
SELECT * FROM users u WHERE NOT EXISTS (
  SELECT 1 FROM orders o WHERE o.user_id = u.id
);
```

---

**Q40: How do you test stored procedures and database triggers?**

**A:**

**Stored procedures:** Call them directly with `CALL sp_name(params)`, then verify the results with SELECT queries:
```sql
CALL sp_calculate_loyalty_score(101);
SELECT loyalty_tier FROM users WHERE id = 101;
-- Verify the procedure produced the expected result
```
Use transaction wrapping to test negative paths — call with invalid inputs, verify the procedure handles them correctly, ROLLBACK to clean up.

**Triggers:** They fire automatically on INSERT/UPDATE/DELETE. Test them by performing the triggering action and verifying the triggered effect:
```sql
-- Test an audit log trigger on INSERT
INSERT INTO orders (user_id, total_amount, status) VALUES (1, 50.00, 'pending');

-- Verify the trigger fired and created an audit entry
SELECT * FROM audit_logs WHERE entity_type = 'order' AND action = 'INSERT'
  AND created_at >= NOW() - INTERVAL 1 MINUTE;
-- Expected: 1 row
```

Trigger testing is important because bugs in triggers are often invisible at the API level — the API returns 201, but the audit log, inventory update, or notification that the trigger should have created is missing.

---

**Q41: What SQL would you write to verify data integrity after a bulk data migration?**

**A:** A migration integrity check suite covers four areas:

```sql
-- 1. Row count matches source
SELECT COUNT(*) FROM users;  -- must equal source system count

-- 2. No required fields lost (became NULL)
SELECT COUNT(*) FROM users WHERE name IS NULL OR email IS NULL;  -- expect 0

-- 3. No duplicates created
SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;  -- expect 0 rows

-- 4. No orphaned records
SELECT COUNT(*) FROM orders o LEFT JOIN users u ON o.user_id = u.id WHERE u.id IS NULL;  -- expect 0

-- 5. Data transformed correctly (example: phone format standardised)
SELECT COUNT(*) FROM users WHERE phone NOT REGEXP '^\\+[0-9]{10,15}$';  -- expect 0

-- 6. Migrated fields have expected default values
SELECT COUNT(*) FROM users WHERE status IS NULL;  -- expect 0

-- 7. Timestamps are realistic (not NULL, not future dates, not epoch)
SELECT COUNT(*) FROM users WHERE created_at IS NULL OR created_at > NOW();  -- expect 0
```

Run these before and after migration. Any number that changes unexpectedly (other than 0 becoming 0) indicates a migration error.

---

**Q42: What is the difference between optimistic and pessimistic locking? How do you test each?**

**A:**

**Pessimistic locking** — locks the row when reading, blocking concurrent modifications:
```sql
SELECT * FROM orders WHERE id = 50 FOR UPDATE;
-- No other transaction can modify this row until this transaction commits/rollbacks
```
Test: Open transaction A with FOR UPDATE on a row. In transaction B, try to modify the same row. Verify B blocks (waits) until A commits. If B gets a dirty read, locking is broken.

**Optimistic locking** — no lock during read, uses a version column to detect concurrent changes:
```sql
-- Read with version
SELECT id, status, version FROM orders WHERE id = 50;  -- version = 3

-- Update only if version hasn't changed
UPDATE orders SET status = 'shipped', version = 4 WHERE id = 50 AND version = 3;
-- 0 rows affected = another process changed it first — application should handle this
```
Test: Simulate two concurrent users editing the same record (both read version=3, both try to update). Verify only one succeeds (1 row affected) and the other gets 0 rows affected and the application returns a conflict error (HTTP 409).

---

*End of SQL & Database Testing Complete Q&A Guide — 42 Questions*
