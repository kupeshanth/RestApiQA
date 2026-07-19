# JMeter — Performance Testing Complete Guide | Load + Stress + API

> Senior QA Engineer Interview Reference — Performance Testing Types, JMeter Architecture, Metrics, CI Integration, and Best Practices

---

## Table of Contents

1. [What is Performance Testing and Why QA Owns It](#1-what-is-performance-testing-and-why-qa-owns-it)
2. [Types of Performance Testing](#2-types-of-performance-testing)
3. [Key Performance Metrics](#3-key-performance-metrics)
4. [JMeter Architecture](#4-jmeter-architecture)
5. [Building a Test Plan — Step by Step](#5-building-a-test-plan--step-by-step)
6. [HTTP Request Sampler Configuration](#6-http-request-sampler-configuration)
7. [CSV Data Set Config — Data-Driven Performance Tests](#7-csv-data-set-config--data-driven-performance-tests)
8. [Listeners — Viewing and Analyzing Results](#8-listeners--viewing-and-analyzing-results)
9. [Timers — Controlling Pacing](#9-timers--controlling-pacing)
10. [Running JMeter from Command Line (Non-GUI / CI Mode)](#10-running-jmeter-from-command-line-non-gui--ci-mode)
11. [Interpreting Results](#11-interpreting-results)
12. [Common Performance Test Scenarios](#12-common-performance-test-scenarios)
13. [Distributed Load Testing — Master-Slave Setup](#13-distributed-load-testing--masterslave-setup)
14. [Integrating JMeter with Jenkins CI](#14-integrating-jmeter-with-jenkins-ci)
15. [API Performance Testing with JMeter](#15-api-performance-testing-with-jmeter)
16. [Performance Testing Best Practices](#16-performance-testing-best-practices)
17. [All CLI Commands Reference](#17-all-cli-commands-reference)
18. [Troubleshooting Common Issues](#18-troubleshooting-common-issues)
19. [Senior Interview Q&A — 10 Questions with Full Answers](#19-senior-interview-qa--10-questions-with-full-answers)

---

## 1. What is Performance Testing and Why QA Owns It

### Definition

Performance testing is a type of non-functional testing that evaluates how a system behaves under various load conditions. It answers the question: **"Does the application remain stable, responsive, and reliable as the number of users and requests increases?"**

### Why QA Owns Performance Testing

Performance testing sits in the QA domain because:

1. **It prevents production failures** — A system that passes all functional tests can still fail under load. QA is responsible for verifying all aspects of system quality.
2. **It validates non-functional requirements** — SLAs define response time, uptime, and throughput targets. QA verifies these are met.
3. **It catches regressions** — A code change can degrade performance without breaking any functional test.
4. **Risk reduction** — Discovering that the checkout process fails under 1000 concurrent users in QA saves significant cost versus discovering it on launch day.
5. **Shift-left testing** — Modern QA integrates performance tests into CI pipelines to catch bottlenecks at every build.

### Non-Functional Requirements (NFR) Example

```
- API response time P95 < 500ms under 500 concurrent users
- Error rate < 1% under peak load
- System must sustain 1000 requests/second for 30 minutes without degradation
- Application must recover within 60 seconds after a spike
```

---

## 2. Types of Performance Testing

### Load Testing

**Purpose**: Test system behaviour under expected normal and peak load.

**Scenario**: Simulate the number of users that would realistically use the application at any given time — including peak hours.

**Goal**: Verify the system meets performance SLAs under expected load.

**Example**: "Our e-commerce site expects 500 concurrent users on a normal day and up to 2000 during a sale. Does it respond within 300ms under both conditions?"

---

### Stress Testing

**Purpose**: Find the breaking point of the system by progressively increasing load beyond normal capacity.

**Scenario**: Gradually ramp users beyond the expected peak until errors begin to occur or response times degrade significantly.

**Goal**: Identify the maximum capacity the system can handle, and understand how it fails (gracefully or catastrophically).

**Example**: "At what concurrent user count does our API start returning errors or slowing to >2 seconds?"

---

### Spike Testing

**Purpose**: Test system response to sudden, extreme surges in load.

**Scenario**: Within a very short time window, jump from normal load to a very high load (5–10x), then return to normal.

**Goal**: Verify the system handles flash events (product launches, marketing emails) and recovers quickly.

**Example**: "If a Twitter post goes viral and 10,000 users visit the site in 2 minutes, what happens?"

---

### Endurance / Soak Testing

**Purpose**: Test system stability over a prolonged period under sustained load.

**Scenario**: Run the system at normal load for an extended period (8 hours, 24 hours, 72 hours).

**Goal**: Detect memory leaks, connection pool exhaustion, disk space growth, and gradual performance degradation.

**Example**: "After 24 hours at 500 concurrent users, does the application start responding slower due to a memory leak?"

---

### Volume Testing

**Purpose**: Test system behaviour with large amounts of data in the database.

**Scenario**: Populate the database with millions of records, then run standard operations.

**Goal**: Verify that queries and operations perform acceptably at large data volumes.

**Example**: "Does the user search API stay below 200ms when the users table has 50 million records?"

---

### Scalability Testing

**Purpose**: Determine the system's ability to scale up (or out) in response to increased load.

**Scenario**: Add more resources (CPU, RAM, instances) incrementally and measure performance improvement.

**Goal**: Validate that adding infrastructure actually improves performance proportionally.

**Example**: "If we add 2 more application server instances, does throughput double?"

---

### Summary Table

| Test Type | Duration | Load Pattern | Primary Goal |
|---|---|---|---|
| Load Test | Short–Medium | Steady at expected level | Verify SLAs are met |
| Stress Test | Short–Medium | Increasing until failure | Find breaking point |
| Spike Test | Short | Sudden surge then drop | Verify surge recovery |
| Endurance / Soak | Long (hours) | Sustained at normal level | Detect memory leaks, drift |
| Volume Test | Variable | Normal load, large data | Verify data-scale performance |
| Scalability Test | Variable | Increasing with resource adds | Verify horizontal/vertical scaling |

---

## 3. Key Performance Metrics

### Response Time

The time between sending a request and receiving the full response.

| Metric | Definition | Acceptable Threshold (typical) |
|---|---|---|
| Average response time | Sum of all times / count | < 200–500ms (depends on API) |
| Median (P50) | 50% of requests are faster | — |
| P90 | 90% of requests complete within this time | < 500ms |
| P95 | 95% of requests complete within this time | < 1000ms |
| P99 | 99% of requests complete within this time | < 2000ms |
| Maximum | Slowest single request | Useful for detecting outliers |

**Why P95/P99 matters more than average**: An average of 200ms looks fine, but if P99 is 10 seconds, then 1 in 100 users is having a terrible experience. P95 is the industry standard SLA metric.

---

### Throughput

The number of requests the system successfully processes per unit of time.

- **TPS** (Transactions Per Second) — Complete business transactions per second.
- **RPS** (Requests Per Second) — Raw HTTP requests per second.

Higher throughput under the same load = better performance.

---

### Error Rate

Percentage of requests that resulted in an error (HTTP 4xx, 5xx, timeouts).

```
Error Rate = (Failed Requests / Total Requests) × 100%
```

Acceptable thresholds:
- Functional tests: 0% error rate expected.
- Load tests: < 0.1% error rate acceptable.
- Stress tests: Errors expected as load exceeds capacity — note the threshold at which errors begin.

---

### Concurrent Users

The number of virtual users simultaneously sending requests to the system. Also called "virtual users" (VU) in some tools.

---

### Latency

The time between when a request is sent and when the **first byte** of the response is received (also called TTFB — Time To First Byte). Useful for diagnosing network vs. server processing bottlenecks.

---

### Resource Utilisation

Monitor alongside JMeter results:
- **CPU %** on application servers.
- **Memory (RAM) usage** — growing memory = potential leak.
- **Database connection pool** — exhausted connections cause queuing and failures.
- **Network I/O** — bandwidth saturation.
- **Disk I/O** — affects logging-heavy or file-serving applications.

---

## 4. JMeter Architecture

### Component Hierarchy

```
Test Plan
└── Thread Group (virtual users)
    ├── Samplers (HTTP Request, JDBC, FTP, etc.)
    │   ├── Config Elements (HTTP Header Manager, CSV Data Set)
    │   ├── Pre-Processors (run before sampler)
    │   ├── Post-Processors (extract data from response)
    │   ├── Assertions (validate response)
    │   └── Timers (delay between requests)
    └── Listeners (collect and display results)
```

### Component Roles

| Component | Role | Examples |
|---|---|---|
| **Test Plan** | Root container for all test elements | One per .jmx file |
| **Thread Group** | Defines virtual users, ramp-up, duration | Thread Group, Concurrency Thread Group |
| **Sampler** | Makes actual requests | HTTP Request, JDBC Request, FTP Request |
| **Config Element** | Configures settings shared across samplers | HTTP Header Manager, HTTP Cookie Manager, CSV Data Set Config |
| **Assertion** | Validates response | Response Assertion, JSON Assertion, Duration Assertion |
| **Listener** | Collects and displays results | View Results Tree, Summary Report, Aggregate Report |
| **Timer** | Adds delay between requests (think-time) | Constant Timer, Gaussian Random Timer, Uniform Random Timer |
| **Pre-Processor** | Runs before each sampler | Regular Expression Extractor set as pre-processor |
| **Post-Processor** | Runs after each sampler to extract data | Regular Expression Extractor, JSON Extractor, XPath Extractor |
| **Controller** | Controls execution flow | Loop Controller, If Controller, Transaction Controller |

---

### Scope Rules (Critical for JMeter Interviews)

Placement of elements determines scope:
- Under a **Thread Group** — applies to all samplers in that Thread Group.
- Under a specific **Sampler** — applies only to that sampler.
- At the **Test Plan** level — applies to all Thread Groups.

---

## 5. Building a Test Plan — Step by Step

### Step 1: Thread Group Configuration

The Thread Group defines your virtual user load pattern.

```
Thread Group settings:
- Number of Threads (Users): 500
  (How many virtual users to simulate)

- Ramp-Up Period (seconds): 60
  (Time to gradually reach full 500 users — avoids instant spike)
  Formula: Ramp-up = Number of Threads / (Threads per second you want)

- Loop Count: 10 (or Infinite with duration)
  (How many times each thread repeats the test)

- Duration (seconds): 300
  (Run for 5 minutes — use instead of Loop Count for time-based tests)

- Startup Delay (seconds): 0
  (Delay before the Thread Group starts — useful for multi-group scenarios)
```

**Interpreting Thread Group Math**:
- 500 users, 60-second ramp-up = approximately 1 new user added every 0.12 seconds.
- 500 users × 10 loops = 5000 total request iterations per sampler.

---

### Step 2: HTTP Request Sampler

```
HTTP Request Sampler:
- Protocol: https
- Server Name or IP: api.myapp.com
- Port Number: 443
- HTTP Method: POST
- Path: /api/orders

Body Data (raw JSON):
{
  "userId": "${userId}",
  "productId": "${productId}",
  "quantity": 1
}

Parameters Tab (alternative to body):
- Name: username  | Value: ${username}
- Name: password  | Value: ${password}
```

**Variables**: JMeter uses `${variableName}` syntax for parameterization.

---

### Step 3: HTTP Header Manager

```
HTTP Header Manager:
- Content-Type: application/json
- Authorization: Bearer ${authToken}
- Accept: application/json
- X-Correlation-ID: ${__UUID()}
```

**Scope tip**: Place the Header Manager directly under the Thread Group or at the Test Plan level to apply to all HTTP requests.

---

### Step 4: Response Assertion

```
Response Assertion (on HTTP Request Sampler):
- Field to Test: Response Code
  - Patterns to Test: 200 (or 201)
  - Pattern Matching Rules: Equals

- Field to Test: Response Body
  - Patterns to Test: "orderId"
  - Pattern Matching Rules: Contains
```

**Other assertion types**:
- **JSON Assertion**: Assert a specific JSON path value.
- **Duration Assertion**: Assert response time < N milliseconds.
- **Size Assertion**: Assert response body size range.

---

### Complete Test Plan Structure

```
Test Plan: "Order API Load Test"
├── HTTP Cookie Manager (handles cookies for all requests)
├── HTTP Cache Manager (simulate realistic browser caching)
├── User Defined Variables
│   ├── BASE_URL = https://api.myapp.com
│   └── TOKEN = Bearer abc123
│
├── Thread Group: "Login Flow"
│   ├── HTTP Header Manager (Content-Type, Authorization)
│   ├── CSV Data Set Config (users.csv → username, password)
│   │
│   ├── HTTP Request: POST /auth/login
│   │   ├── JSON Extractor (extract authToken from response)
│   │   └── Response Assertion (status = 200)
│   │
│   └── Constant Timer (1000ms think time)
│
├── Thread Group: "Order Submission"
│   ├── HTTP Request: POST /api/orders
│   │   ├── Response Assertion (status = 201)
│   │   └── Duration Assertion (max 500ms)
│   │
│   └── Gaussian Random Timer (1000ms deviation, 500ms offset)
│
└── Listeners (at Test Plan level — collect from all Thread Groups)
    ├── View Results Tree (debug only — disable in load runs)
    ├── Summary Report
    └── Aggregate Report
```

---

## 6. HTTP Request Sampler Configuration

### GET Request

```
Method: GET
Path: /api/users?page=1&limit=10

Or use Parameters tab:
- page: 1
- limit: 10
```

### POST with JSON Body

```
Method: POST
Path: /api/users
Body Data:
{
  "name": "${name}",
  "email": "${email}",
  "role": "viewer"
}
```

### PUT Request

```
Method: PUT
Path: /api/users/${userId}
Body Data:
{ "name": "Updated Name" }
```

### DELETE Request

```
Method: DELETE
Path: /api/users/${userId}
```

### Extracting Response Values (JSON Extractor)

```
JSON Path Extractor:
- Names of created variables: userId
- JSON Path expressions: $.data.id
- Default Values: ERROR
- Match No. (0=Random, -1=All, N=Nth): 1
```

The extracted `${userId}` is then available in subsequent HTTP samplers in the same thread.

---

## 7. CSV Data Set Config — Data-Driven Performance Tests

Enables each virtual user to use a different set of test data.

### CSV File (users.csv)

```csv
username,password,userId
user1@test.com,Pass@123,101
user2@test.com,Pass@123,102
user3@test.com,Pass@123,103
user4@test.com,Pass@123,104
user5@test.com,Pass@123,105
```

### CSV Data Set Config Settings

```
CSV Data Set Config:
- Filename: users.csv (or absolute path)
- Variable Names: username,password,userId
- Delimiter: ,
- Allow Quoted Data: True
- Recycle on EOF: True   (start from beginning when end of file reached)
- Stop Thread on EOF: False
- Sharing Mode: All Threads (each iteration picks the next row across all threads)
```

**Sharing Modes**:
- `All Threads` — All threads share the same CSV row pointer. Each request gets the next row globally.
- `Current Thread Group` — Each Thread Group has its own pointer.
- `Current Thread` — Each thread cycles through all rows independently.

### Using CSV Variables in Requests

```
HTTP Request body:
{
  "email": "${username}",
  "password": "${password}"
}
```

---

## 8. Listeners — Viewing and Analyzing Results

### View Results Tree

- Shows each individual request/response in full detail (headers, body, timing).
- **Only enable during debugging** — it stores every response in memory and will cause `OutOfMemoryError` in load tests with thousands of requests.

### Summary Report

| Column | Meaning |
|---|---|
| Label | Sampler name |
| #Samples | Total requests sent |
| Average | Average response time (ms) |
| Min | Minimum response time |
| Max | Maximum response time |
| Std Dev | Standard deviation |
| Error % | Percentage of requests that failed |
| Throughput | Requests per second |
| KB/sec | Network throughput |
| Avg Bytes | Average response size |

### Aggregate Report

Similar to Summary Report but also shows **Median** and **90th/95th/99th percentile** columns — more useful for SLA validation.

### Backend Listener (InfluxDB + Grafana)

For real-time monitoring during load runs:

```
Backend Listener:
- Implementation: InfluxdbBackendListenerClient
- influxdbUrl: http://localhost:8086/write?db=jmeter
- application: OrderAPI_LoadTest
- measurement: jmeter
```

This streams results to InfluxDB, displayed in real-time Grafana dashboards.

### Aggregate Graph

Visual bar chart of response times per sampler. Useful for presentations.

---

## 9. Timers — Controlling Pacing

Timers add delays between requests to simulate realistic user think-time (users don't fire requests instantaneously).

### Constant Timer

```
Constant Timer:
- Thread Delay: 1000 (ms)
```
Adds exactly 1 second between requests.

### Gaussian Random Timer

```
Gaussian Random Timer:
- Deviation (ms): 1000
- Constant Delay Offset (ms): 500
```
Mean delay = 500ms, with ±1000ms standard deviation. More realistic than constant.

### Uniform Random Timer

```
Uniform Random Timer:
- Random Delay Maximum (ms): 2000
- Constant Delay Offset (ms): 500
```
Random delay between 500ms and 2500ms.

**Timer Scope**: A timer placed inside a Thread Group delays all samplers in that group. A timer placed under a specific sampler delays only after that sampler.

---

## 10. Running JMeter from Command Line (Non-GUI / CI Mode)

**Important**: Never run load tests from the JMeter GUI. The GUI itself consumes significant CPU and memory, skewing results. Always run in non-GUI (command-line) mode for actual performance tests.

### Basic Run

```bash
jmeter -n -t test-plan.jmx -l results.jtl
```

| Flag | Meaning |
|---|---|
| `-n` | Non-GUI mode |
| `-t` | Path to test plan (.jmx file) |
| `-l` | Path to results log file (.jtl) |

### Run with HTML Report Generation

```bash
jmeter -n -t test-plan.jmx -l results.jtl -e -o report-folder/
```

| Flag | Meaning |
|---|---|
| `-e` | Generate HTML report after test completes |
| `-o` | Output folder for the HTML report |

**Note**: The `-o` folder must be empty or non-existent before the run.

### Override JMX Properties from CLI

```bash
jmeter -n -t test-plan.jmx -l results.jtl \
  -Jthreads=500 \
  -Jrampup=60 \
  -JbaseUrl=https://staging.api.com \
  -JauthToken=Bearer\ abc123
```

In the JMX test plan, reference as `${__P(threads, 100)}` to read `-J` properties with a fallback default.

### Run a Specific Duration

```bash
jmeter -n -t test-plan.jmx -l results.jtl -Jduration=300
```

### Remote (Distributed) Run

```bash
# Run on specific remote agents from master
jmeter -n -t test-plan.jmx -l results.jtl -R 192.168.1.101,192.168.1.102
```

### Generate HTML Report from Existing JTL

```bash
jmeter -g results.jtl -o report-folder/
```

---

## 11. Interpreting Results

### Reading the Aggregate Report

```
Label             | Samples | Average | Median | 90%   | 95%   | 99%   | Min | Max  | Error% | Throughput
------------------|---------|---------|--------|-------|-------|-------|-----|------|--------|----------
POST /api/orders  | 50000   | 180ms   | 150ms  | 320ms | 480ms | 950ms | 45  | 3200 | 0.2%   | 278/sec
GET /api/products | 50000   | 95ms    | 80ms   | 180ms | 230ms | 410ms | 20  | 1100 | 0.0%   | 492/sec
TOTAL             | 100000  | 137ms   | 115ms  | 260ms | 380ms | 790ms | 20  | 3200 | 0.1%   | 770/sec
```

**How to interpret**:

- `POST /api/orders`: Average 180ms — looks good. But P99 is 950ms, meaning 500 out of 50,000 users waited almost 1 second. Check SLA.
- P95 = 480ms: If SLA is "P95 < 500ms", this is right at the edge — flag for investigation.
- Error rate 0.2% on order creation — find out if these are timeout errors or business logic errors.
- Throughput 278/sec for orders: Multiply by 60 = 16,680 orders per minute capacity.

---

### Common Thresholds (Typical Industry Standards)

| Metric | Green (Pass) | Yellow (Warning) | Red (Fail) |
|---|---|---|---|
| P95 response time | < 500ms | 500ms–1000ms | > 1000ms |
| P99 response time | < 1000ms | 1000ms–2000ms | > 2000ms |
| Error Rate | < 0.1% | 0.1%–1% | > 1% |
| Throughput degradation | < 5% drop | 5%–15% drop | > 15% drop |

Note: Thresholds vary per application and are defined in SLA documentation.

---

### Reading the JTL File

The `.jtl` results file is a CSV with one row per request:

```
timeStamp,elapsed,label,responseCode,responseMessage,threadName,success,bytes,sentBytes,grpThreads,allThreads,Latency,Connect
1706097600000,145,POST /api/orders,201,Created,Thread Group 1-1,true,245,512,500,500,140,12
1706097600001,3200,POST /api/orders,504,Gateway Timeout,Thread Group 1-2,false,0,512,500,500,3200,11
```

Fields:
- `timeStamp` — Epoch ms when request started
- `elapsed` — Response time in ms
- `responseCode` — HTTP status
- `success` — true/false
- `Latency` — TTFB in ms

---

## 12. Common Performance Test Scenarios

### Scenario 1: Baseline Test (Single User)

**Purpose**: Establish the natural response time of the system with no load, as a reference point.

```
Thread Group:
- Number of Users: 1
- Ramp-Up: 1 second
- Loop Count: 10

Expected: Response times should be lowest they will ever be.
This baseline is compared against all subsequent test results.
```

---

### Scenario 2: Load Test (Expected Peak)

**Purpose**: Verify system meets SLAs at expected peak concurrent users.

```
Thread Group:
- Number of Users: 500 (or your expected peak)
- Ramp-Up: 120 seconds (2-minute ramp to avoid spike at start)
- Duration: 300 seconds (5-minute sustained load)

Success Criteria:
- P95 < 500ms
- Error rate < 0.1%
- No server errors in logs
```

---

### Scenario 3: Stress Test (Find Breaking Point)

**Purpose**: Incrementally increase load until the system starts failing.

```
Use "Concurrency Thread Group" (from JMeter plugins) for stepped load:

Step 1:  100 users for 2 minutes
Step 2:  300 users for 2 minutes
Step 3:  500 users for 2 minutes
Step 4:  750 users for 2 minutes
Step 5: 1000 users for 2 minutes
Step 6: 1500 users for 2 minutes (until errors occur)

Watch for:
- Error rate climbing above 1%
- P95 exceeding 2 seconds
- Server OOM or crash
The last step where error rate < 1% = maximum sustainable capacity.
```

---

### Scenario 4: Spike Test

**Purpose**: Test recovery from sudden user surge.

```
Thread Group (or schedule):
Time 0–2min:    100 users (normal baseline)
Time 2–3min:   2000 users (sudden spike — add 1900 users in 1 minute)
Time 3–8min:    100 users (return to baseline)

Watch for:
- How quickly response time drops back to baseline after spike
- Whether errors occur during spike and recover
- Server memory/CPU during and after spike
```

---

### Scenario 5: Endurance / Soak Test

**Purpose**: Detect gradual performance degradation over time.

```
Thread Group:
- Number of Users: 200 (normal load — not peak)
- Duration: 28800 seconds (8 hours)

Monitor every 30 minutes:
- Average response time (should remain stable, not grow)
- Memory usage on app servers (should not grow continuously — indicates leak)
- Database connection count
- Error rate

Red flag: Response time at hour 7 is 3x the response time at hour 1 = memory leak or connection pool issue.
```

---

## 13. Distributed Load Testing — Master-Slave Setup

### Why Distributed Testing?

A single JMeter machine can typically generate 300–500 concurrent users before its own CPU/network becomes the bottleneck. For larger loads, distribute across multiple machines.

### Architecture

```
Master (JMeter GUI / CLI)
├── Sends test plan to all agents
├── Coordinates test start/stop
└── Collects and aggregates results

Load Agents (JMeter Servers — "slaves")
├── Agent 1 (192.168.1.101) → 500 virtual users
├── Agent 2 (192.168.1.102) → 500 virtual users
└── Agent 3 (192.168.1.103) → 500 virtual users
                                Total: 1500 concurrent users
```

### Setting Up Agents

```bash
# On each agent machine — start JMeter server
jmeter-server

# Or with specific IP binding
jmeter-server -Djava.rmi.server.hostname=192.168.1.101
```

### Configure Master (jmeter.properties)

```properties
# jmeter.properties on master
remote_hosts=192.168.1.101,192.168.1.102,192.168.1.103
```

### Run from Master

```bash
# Run test on all remote agents
jmeter -n -t test-plan.jmx -l results.jtl -r

# Run on specific agents
jmeter -n -t test-plan.jmx -l results.jtl -R 192.168.1.101,192.168.1.102
```

**Note**: Test data files (CSV) must be present on each agent machine at the same path.

---

## 14. Integrating JMeter with Jenkins CI

### Jenkins Performance Plugin Setup

1. Install **Performance Plugin** in Jenkins.
2. Add a **Execute Shell** build step to run JMeter.

### Jenkins Pipeline (Jenkinsfile)

```groovy
pipeline {
  agent any

  stages {
    stage('Performance Test') {
      steps {
        sh '''
          jmeter -n \
            -t performance-tests/order-api-load.jmx \
            -l results/results.jtl \
            -e -o results/html-report/ \
            -JbaseUrl=https://staging.api.com \
            -Jthreads=200 \
            -Jduration=180
        '''
      }
    }
  }

  post {
    always {
      // Archive HTML report
      publishHTML(target: [
        reportDir: 'results/html-report',
        reportFiles: 'index.html',
        reportName: 'JMeter Performance Report'
      ])

      // Performance plugin thresholds — fail build if SLAs exceeded
      perfReport(
        sourceDataFiles: 'results/results.jtl',
        errorFailedThreshold: 1,          // Fail if error rate > 1%
        errorUnstableThreshold: 0.5,      // Unstable if error rate > 0.5%
        errorUnstableResponseTimeThreshold: '',
        relativeFailedThresholdPositive: 100,
        relativeUnstableThresholdPositive: 50
      )
    }
  }
}
```

### Fail Build on Performance Regression

```groovy
// In post-always block
perfReport(
  sourceDataFiles: 'results/results.jtl',
  errorFailedThreshold: 2,         // > 2% error rate = FAILURE
  errorUnstableThreshold: 1,       // 1-2% error rate = UNSTABLE
  relativeFailedThresholdPositive: 20,  // > 20% slower than previous = FAILURE
)
```

---

## 15. API Performance Testing with JMeter

### Test Plan Structure for REST API Performance

```
Test Plan: "REST API Performance Suite"
│
├── User Defined Variables
│   ├── BASE_URL = https://api.myapp.com
│   ├── AUTH_TOKEN = Bearer ${__P(token)}
│   └── CONTENT_TYPE = application/json
│
├── HTTP Cookie Manager
├── HTTP Cache Manager
│
├── Thread Group: "Auth — Get Token"
│   ├── Threads: 1, Loop: 1 (run once before load)
│   ├── HTTP Request: POST /auth/login
│   │   └── JSON Extractor: Extract authToken → $.token
│   └── Bean Shell Assertion (or JSR223): Set token as property
│       props.put("authToken", vars.get("authToken"));
│
├── Thread Group: "CRUD Operations Load"
│   ├── Threads: 200, Ramp-Up: 60s, Duration: 300s
│   ├── HTTP Header Manager (Authorization: Bearer ${__P(authToken)})
│   │
│   ├── Transaction Controller: "Create User"
│   │   ├── HTTP Request: POST /api/users
│   │   │   ├── JSON Extractor: userId ← $.id
│   │   │   └── Response Assertion: status = 201
│   │   └── Duration Assertion: max 500ms
│   │
│   ├── Transaction Controller: "Get User"
│   │   ├── HTTP Request: GET /api/users/${userId}
│   │   └── Response Assertion: status = 200, body contains "email"
│   │
│   ├── Transaction Controller: "Update User"
│   │   ├── HTTP Request: PUT /api/users/${userId}
│   │   └── Response Assertion: status = 200
│   │
│   └── Transaction Controller: "Delete User"
│       ├── HTTP Request: DELETE /api/users/${userId}
│       └── Response Assertion: status = 204
│
└── Listeners
    ├── Aggregate Report
    └── Backend Listener (InfluxDB)
```

### JSON Assertion Example

```
JSON Assertion:
- Additionally assert value: checked
- JSON Path: $.status
- Expected Value: active
- Match as regular expression: false
```

### Chaining Requests — Extracting Dynamic Values

```
Step 1: POST /api/orders
  JSON Extractor:
  - Variable Name: orderId
  - JSON Path: $.data.orderId

Step 2: GET /api/orders/${orderId}
  Response Assertion:
  - Contains: "CONFIRMED"
```

---

## 16. Performance Testing Best Practices

### 1. Test in a Production-Like Environment

- Use same hardware specs (CPU, RAM, instances) as production.
- Use same database size (or representative sample).
- Disable development features (debug logging, dev middleware).
- Test against staging — never performance test directly against production unless a controlled canary scenario.

### 2. Warm Up the System First

Many systems (JVM, caches, CDN) need a warm-up period before reaching steady-state performance:

```
Add a warm-up Thread Group:
- 10 users for 2 minutes
- Run before the main load Thread Group
- Exclude warm-up data from SLA measurement
```

### 3. Isolate the System Under Test

- Disable unrelated services consuming resources.
- Ensure no other test teams are running tests simultaneously.
- Monitor network throughput to ensure the network is not the bottleneck.

### 4. Monitor Server-Side Metrics

JMeter measures from the client. Always monitor server-side simultaneously:
- Application server CPU, memory, thread pool utilisation.
- Database: slow query log, connection pool, query plans.
- Infrastructure: load balancer, CDN hit rates.

### 5. Use Realistic Test Data

- Use realistic usernames, products, order amounts — not sequential IDs.
- Ensure the database has production-scale data volumes.
- Randomise CSV data to avoid caching effects.

### 6. Set Assertions on Every Request

Without assertions, a request that returns a 500 error might look like it passed (it still got a response). Always add:
- Status code assertion.
- Response body assertion (key field exists).
- Duration assertion (optional, for SLA validation).

### 7. Never Use GUI Mode for Load Tests

```bash
# Always use:
jmeter -n -t plan.jmx -l results.jtl

# Never:
# (Starting test from JMeter GUI with many threads)
```

### 8. Run Multiple Times and Average

A single test run can produce variable results due to GC pauses, network jitter, and caching warm-up. Run 3–5 times and use the median run's results for reporting.

### 9. Think Time is Mandatory

Real users don't fire requests in a tight loop. Add realistic think-time (1–3 seconds for page interactions, less for API chains).

### 10. Clean Up After Tests

Delete test data created during performance tests. Ensure test teardown is part of your test plan or post-test cleanup scripts.

---

## 17. All CLI Commands Reference

```bash
# Non-GUI run (CI mode) — basic
jmeter -n -t test.jmx -l results.jtl

# Non-GUI run with HTML report
jmeter -n -t test.jmx -l results.jtl -e -o ./html-report/

# Pass JMeter properties
jmeter -n -t test.jmx -l results.jtl -Jthreads=500 -Jrampup=60 -Jduration=300

# Override JMeter system property
jmeter -n -t test.jmx -Djmeter.save.saveservice.output_format=csv

# Run in distributed mode (all remote hosts in jmeter.properties)
jmeter -n -t test.jmx -l results.jtl -r

# Run on specific remote hosts
jmeter -n -t test.jmx -l results.jtl -R 192.168.1.101,192.168.1.102

# Start JMeter server (agent/slave)
jmeter-server
jmeter-server -Djava.rmi.server.hostname=192.168.1.101

# Generate HTML report from existing JTL
jmeter -g results.jtl -o ./report/

# Run with logging level
jmeter -n -t test.jmx -l results.jtl -L DEBUG
jmeter -n -t test.jmx -l results.jtl -L ERROR

# Increase JVM heap (for large tests — set in jmeter script or HEAP env var)
HEAP="-Xms2g -Xmx4g" jmeter -n -t test.jmx -l results.jtl

# Print JMeter version
jmeter --version

# Print JMeter help
jmeter --help
```

---

## 18. Troubleshooting Common Issues

### OutOfMemoryError

**Symptom**: `java.lang.OutOfMemoryError: Java heap space` during test run.

**Causes and Fixes**:

```bash
# 1. Increase JMeter heap size (edit jmeter script or setenv.sh)
HEAP="-Xms1g -Xmx4g" jmeter -n -t test.jmx -l results.jtl

# 2. Disable View Results Tree listener (biggest memory consumer)
# Never enable "View Results Tree" during load runs

# 3. Use Simple Data Writer listener instead of listeners that store all data
# 4. Reduce the JTL file fields saved
# In jmeter.properties:
jmeter.save.saveservice.response_data=false
jmeter.save.saveservice.samplerData=false
```

---

### Too Many Open Files

**Symptom**: `java.net.SocketException: Too many open files`

**Cause**: OS limit on open file descriptors is too low for the number of concurrent connections.

**Fix** (Linux):

```bash
# Temporarily increase (for current session)
ulimit -n 65535

# Permanently (add to /etc/security/limits.conf)
*    soft    nofile    65535
*    hard    nofile    65535
```

---

### Inconsistent Results Between Runs

**Causes**:
1. JVM warmup — first run includes JIT compilation time.
2. Test data caching — repeated requests return cached responses.
3. Garbage Collection pauses — GC causes sudden latency spikes.
4. Database buffer pool — varies between runs.

**Fixes**:
- Include a 2-minute warm-up Thread Group before the main test.
- Run the test 3 times and use median results.
- Monitor GC logs alongside test results.
- Increase JVM heap to reduce GC frequency.

---

### Connection Refused / Cannot Connect

**Symptom**: All requests fail immediately with "Connection refused".

**Check**:
- Is the server running and accessible from the JMeter machine?
- Are firewall rules blocking the port?
- Is the correct IP/hostname and port set in HTTP Request Sampler?
- For distributed tests: are agents in the same network as the target?

---

### Assertions Failing Even for Correct Responses

**Symptom**: Response looks correct in View Results Tree but assertion fails.

**Common Cause**: Response body assertion with "Equals" instead of "Contains".

**Fix**: Use "Contains" pattern matching unless you need exact full-body comparison.

---

## 19. Senior Interview Q&A — 10 Questions with Full Answers

---

**Q1: What are the different types of performance testing and when would you choose each?**

**A**: There are six main types:

- **Load testing**: Simulate expected peak concurrent users (e.g., 500 users) to verify SLAs are met. Run before every major release.
- **Stress testing**: Incrementally increase load until the system breaks, to find the ceiling. Run when planning capacity or before scaling decisions.
- **Spike testing**: Suddenly jump to 10× normal load. Run when expecting flash events (product launches, marketing campaigns).
- **Endurance/Soak testing**: Sustain normal load for 8–24 hours. Run to detect memory leaks and connection pool exhaustion.
- **Volume testing**: Fill the database with millions of records and test operations. Run before migrating to a data-heavy production environment.
- **Scalability testing**: Test how performance improves when adding resources. Run when planning horizontal or vertical scaling strategy.

In practice, a QA team typically runs load tests in CI on every release, stress tests monthly, and endurance tests quarterly.

---

**Q2: What are the key metrics you monitor during a performance test and what are acceptable thresholds?**

**A**: The core metrics are:

- **P95 response time**: 95% of requests complete within this time. Typical SLA: < 500ms for API calls, < 1000ms for page loads.
- **P99 response time**: 99% of requests. Typical SLA: < 1000ms.
- **Error rate**: Percentage of failed requests. Should be < 0.1% under normal load.
- **Throughput (TPS/RPS)**: Requests processed per second. Should meet or exceed the required transaction rate.
- **Concurrent users**: The virtual user count the system is sustaining.
- **Server resources**: CPU % (< 80%), memory usage (stable, not growing), connection pool utilisation.

I always report P95 and P99 rather than average because averages can hide a small percentage of very slow requests that represent real user pain.

---

**Q3: How do you determine how many virtual users to set in a JMeter Thread Group?**

**A**: This comes from multiple sources:

1. **Business requirements**: Product/business team defines the expected peak concurrent users (e.g., "We expect 500 users at peak").
2. **Analytics data**: Look at web analytics during the busiest historical period. Active sessions at peak = starting point for concurrent users.
3. **Little's Law**: `Concurrent Users = Throughput × Response Time`. If you need 1000 TPS and each request takes 500ms = 500 concurrent sessions minimum.
4. **Load model**: Identify the percentage of users on each feature. If 30% of users browse and 10% checkout, create proportional Thread Groups.

I also add a safety buffer — typically 1.5–2× the expected peak — to ensure the system has headroom.

---

**Q4: What is the difference between P95 and average response time, and why does P95 matter more?**

**A**: The average response time is the sum of all response times divided by the number of requests. It is easily skewed by a small number of very fast requests masking a tail of very slow ones.

P95 (95th percentile) means "95% of requests completed within this time". It directly represents what most users actually experience, including the slowest 1 in 20 requests.

**Example**:
- 1000 requests
- 950 complete in 100ms
- 50 complete in 5000ms
- Average = (950 × 100 + 50 × 5000) / 1000 = 345ms — looks reasonable
- P95 = 5000ms — 50 users had a terrible experience

If your SLA is "P95 < 500ms", you would fail this test, but the average alone would mislead you into thinking performance was acceptable. This is why SLAs use percentiles, not averages.

---

**Q5: What would you investigate if a performance test shows error rate climbing under load?**

**A**: I approach this systematically:

1. **Check error types in JTL**: Are they connection timeouts, HTTP 500s, HTTP 429 (rate limiting), or HTTP 503 (service unavailable)? Each points to a different root cause.
2. **Check server logs**: Application server error logs during the test window. Look for `OutOfMemoryError`, `Connection pool exhausted`, `Too many open files`.
3. **Check database**: Slow query log, blocking locks, connection pool saturation.
4. **Check infrastructure**: CPU saturation on app servers, network bandwidth, load balancer connection limits.
5. **Check the application**: Thread pool size, request queue depth, garbage collection frequency.

The most common causes are: database connection pool exhaustion, JVM heap exhaustion causing GC pauses, or a third-party service dependency becoming the bottleneck.

---

**Q6: How do you integrate JMeter into a CI pipeline and fail the build on SLA breach?**

**A**: In a Jenkins pipeline using the Performance Plugin:

1. Run JMeter in non-GUI mode: `jmeter -n -t test.jmx -l results.jtl -e -o html-report/`
2. Use the Jenkins Performance Plugin to parse the JTL file and compare against thresholds.
3. Set failure thresholds: `errorFailedThreshold=1` (fail build if error rate > 1%).
4. Set performance regression thresholds: `relativeFailedThresholdPositive=20` (fail if response time > 20% worse than previous build).
5. Publish the HTML report as an artifact.

This way, every release is automatically validated against performance SLAs, and any regression blocks the deployment.

---

**Q7: How is JMeter different from k6 and Gatling?**

**A**:

| Aspect | JMeter | k6 | Gatling |
|---|---|---|---|
| Language | XML (JMX) + GUI | JavaScript | Scala |
| GUI | Yes | No (code-only) | No (code-only) |
| Scripting | Limited (BeanShell/JSR223 Groovy) | Full JavaScript | Full Scala |
| CI Friendliness | Moderate | Excellent | Excellent |
| Learning Curve | Low (GUI) | Low (JS) | High (Scala) |
| Reports | Basic (plugin for HTML) | Built-in beautiful | Built-in HTML |
| Distributed Load | Manual setup | k6 Cloud (paid) or open-source agents | Gatling Enterprise |
| Community | Very large | Growing fast | Medium |

**When to choose**:
- **JMeter**: Team unfamiliar with coding, needs GUI, large community and plugin ecosystem.
- **k6**: Developer/DevOps team, needs CI-native scripting in JavaScript, modern tooling.
- **Gatling**: Java/Scala team, needs DSL-based scripts with excellent reporting.

---

**Q8: What is a Transaction Controller in JMeter and why use it?**

**A**: A Transaction Controller groups multiple HTTP samplers into a logical transaction, measuring the total elapsed time of all samplers inside it as a single metric.

**Example**: A "Checkout" transaction might include:
1. GET /cart (fetch cart)
2. POST /orders (submit order)
3. GET /orders/{id} (confirm order)

Without a Transaction Controller, you see three separate response time metrics. With one named "Checkout", you also see the total time for the entire checkout flow as a single reportable metric, which is what the SLA for "checkout completion time" should be measured against.

It also makes reports cleaner and more business-aligned — stakeholders care about "checkout time", not "POST /orders time" in isolation.

---

**Q9: What is the difference between ramp-up period and test duration in JMeter, and how do you set them correctly?**

**A**:

- **Ramp-Up Period**: The time JMeter takes to gradually start all threads. If you set 500 threads and 60-second ramp-up, JMeter starts approximately 1 new thread every 0.12 seconds.
- **Test Duration**: How long the test runs once all threads are active (or from the start, depending on configuration).

**Why ramp-up matters**: Without ramp-up (ramp-up = 0), all 500 users fire simultaneously at the start — creating an artificial spike that doesn't represent real-world behaviour and can skew results by hitting the system before caches are warm.

**Setting them correctly**:
- Ramp-up = At least as long as the time it takes users to naturally arrive. For 500 users, 60–120 seconds is typical.
- Duration = Long enough to reach steady state. Generally 3–5× the ramp-up time, minimum 5 minutes for meaningful data. Endurance tests run hours.

---

**Q10: You run a load test and the average response time is fine but P99 is very high. What does this indicate and how do you investigate?**

**A**: A good average with poor P99 indicates **tail latency** — the majority of requests are fast, but a small subset are taking disproportionately long.

**Common causes**:

1. **Garbage Collection (GC) pauses**: JVM stops all threads for a GC cycle, causing periodic latency spikes. Check GC logs for long pause events during the test.
2. **Database lock contention**: Some requests hit a locked row and must wait. Check the DB slow query log and lock wait time.
3. **Resource contention under peak**: Thread pool exhaustion means some requests are queued rather than immediately processed.
4. **External API calls**: If some requests trigger a call to a slow third-party API (e.g., email service, payment gateway), those requests will be slow while others are fast.
5. **Cache misses**: Requests for uncached data are slow; cached data is fast.

**Investigation steps**:
- Cross-reference the JTL timestamp of P99 requests with server-side GC logs, slow query logs, and APM traces.
- Use an APM tool (Datadog, New Relic, Dynatrace) to trace the slow requests end-to-end.
- Look at whether high-latency requests cluster at specific time intervals (suggests GC) or are random (suggests contention or cache misses).

---

*End of JMeter Performance Testing Complete Guide*
