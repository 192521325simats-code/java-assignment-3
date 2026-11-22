# Q3: Resource Safety, Debugging and Robust Code

**Bloom’s Level:** K4 – Analyse  
**Marks:** 7  

*(Note: In accordance with assignment instructions, responsibilities are reversed for Q3: Student A performs Debugging and Experimental Analysis; Student B performs Design and Correctness).*

---

## 1. Shared Group Work

### 1.1 Problem Context & System Implementation
In a retail enterprise environment, the daily sales aggregation program must parse sales logs from multiple physical stores (`store_1.csv`, `store_2.csv`, ...) and write an integrated summary report (`summary.csv`).
- Under heavy daily usage, the application intermittently crashed with `java.io.IOException: Too many open files` (POSIX `EMFILE`), and left incomplete or corrupted summary reports visible to managers.
- Both manual `try-catch-finally` with `close()` (`ManualSalesReportGenerator.java`) and `try-with-resources` (`SafeSalesReportGenerator.java`) were developed and tested.
- **Verification on Valid Inputs:** Executed via `SalesReportDemo.java`, both implementations produced 100% byte-for-byte identical output files.

---

## 2. Student A – Debugging and Experimental Analysis

### 2.1 Locating the Resource Leak via Step-Through Debugging
Using breakpoints on file opening (`new FileReader(...)`) and file closing (`reader.close()`):
1. **The Breakpoint Trace:**
   - Breakpoint 1 (`ManualSalesReportGenerator.java:31`): `currentReader = new BufferedReader(...)` opens file descriptor `fd:41` for `store_1.csv`.
   - Normal completion: `reader.close()` is reached at line 59; `fd:41` is freed.
   - Breakpoint 2 (`ManualSalesReportGenerator.java:31`): `currentReader` opens file descriptor `fd:42` for `store_2_corrupt.csv`.
   - At line 42, during record parsing, `IllegalArgumentException("Corrupt record...")` is thrown due to malformed data.
   - **The Critical Bug:** Control immediately aborts the loop and jumps to the `catch (Exception e)` block at line 68. Line 59 (`currentReader.close()`) is **skipped entirely**.
   - In the `finally` block (line 74), only `writer.close()` is called. `currentReader` is never closed. File descriptor `fd:42` remains permanently open and pinned in the OS process table.
2. **Cascading Failure:** If 1,024 stores are processed, repeatedly encountering errors or unclosed descriptors rapidly exhausts the OS per-process file descriptor limit ($1024$ on standard Linux/Unix systems), causing subsequent operations to fail with `Too many open files`.

### 2.2 Experimental Failure Injection and File Handle Audit
A test was constructed with three stores (`store_1_valid.csv`, `store_2_corrupt.csv`, `store_3_valid.csv`).

| Implementation Variant | Injected Error Point | File Handles Left Leaked | Post-Condition Report State |
|---|---|---|---|
| **Manual `try-finally`** | Malformed CSV in store 2 | **1 descriptor permanently leaked** | Corrupted / partial file left on disk |
| **`try-with-resources`** | Malformed CSV in store 2 | **0 descriptors leaked (100% clean)** | Temp file wiped; zero corrupted data exposed |

### 2.3 Diagnostic Logging of Failing Files
Structured diagnostic logging was introduced in `SafeSalesReportGenerator.java`:
```java
System.err.println("[LOG ERROR]: Failed processing store file: " + storeFile 
        + " | Cause: " + e.getMessage());
```
This logs the precise offending file name, line index, and payload anomaly before triggering resource deallocation.

---

## 3. Student B – Design and Correctness

### 3.1 Resource Lifecycle Inventory
1. **Input Resources:** `BufferedReader` (wrapping `FileReader`) instantiated for each store file $1 \le i \le f$.
   - *Requirement:* Must be closed immediately upon completing that store's records, or immediately when an error occurs during parsing.
2. **Output Resource:** `BufferedWriter` (wrapping `FileWriter`) opened once for the summary report.
   - *Requirement:* Must be closed when the entire report is successfully flushed, or closed and discarded if any preceding stage aborts.

### 3.2 Why Manual `try-catch-finally` Leaks Resources
1. **Early Loop Exit Skips In-Loop Close:** If `close()` is placed at the end of the loop body, any exception thrown in the loop body bypasses the `close()` call.
2. **Single Reference Overwrite:** Storing loop readers in a single variable (`currentReader`) means that even if `finally` attempts to close `currentReader`, previous instances that were not closed are lost to garbage collection while holding open OS handles.
3. **Exceptions in `finally` Mask Primary Errors:** If an exception is thrown in the `finally` block (e.g., `writer.close()` fails), any unhandled exception from the `try` block is silently lost/overridden, masking the real bug.

### 3.3 How Try-With-Resources Guarantees Deterministic Closing
1. **The `AutoCloseable` Contract:** In Java 7+, any class implementing `java.lang.AutoCloseable` can be managed by `try (Resource r = ...)`.
2. **Deterministic Reverse-Order Deallocation:** When multiple resources are declared in a single `try (...)`, the compiler generates bytecode guaranteeing that resources are closed in the exact **reverse order of their declaration**.
3. **Execution Invariant:** The synthetic `close()` calls are generated within implicit bytecode exception handlers (`any` handler blocks), ensuring that `close()` is invoked whether the block exits normally, returns, breaks, or throws an exception or error.

### 3.4 Suppressed Exceptions in Java
- **Definition:** When an exception is thrown inside the `try` block, and during the automatic closing of resources another exception is thrown by `close()`, Java cannot throw both simultaneously.
- **Resolution:** Java designates the `try` block's exception as the **primary exception**. The secondary exception thrown during `close()` is attached to the primary exception as a **suppressed exception** via `Throwable.addSuppressed(Throwable exception)`.
- **Retrieval:** The caller can retrieve all suppressed exceptions using `Throwable.getSuppressed()`, guaranteeing that no diagnostic evidence is lost.

---

## 4. Joint Engineering Decision

### When Try-With-Resources is Not Sufficient on Its Own
While `try-with-resources` guarantees that file descriptors and sockets are closed, it is **not sufficient on its own** to guarantee application robustness in the following scenarios:
1. **Partial File Writes & Non-Atomic Operations:** If a failure occurs halfway through writing a summary report, the destination file is left partially written and corrupted. Robustness requires writing to a `.tmp` file and executing an **atomic rename** (`Files.move(..., ATOMIC_MOVE / REPLACE_EXISTING)`).
2. **Disk Full (`ENOSPC`) & Unflushed Buffers:** `close()` may flush remaining buffers. If the underlying disk quota is exceeded, `close()` itself will fail. Business transactions must track explicit two-phase commits.
3. **Non-AutoCloseable Legacy APIs:** Legacy C-native handles or JNI pointers that do not implement `AutoCloseable` require custom wrappers.
4. **Network Socket Timeouts & Half-Closed Connections:** Sockets closed without explicit linger configurations may hang in `TIME_WAIT` or leak TCP connection table entries.

---

## 5. Alternative Concept and Complexity Analysis

### 5.1 Architectural Comparison of Resource Management
```
1. No Closing at All:
   try { Reader r = new Reader(file); ... }
   --> Result: Leaks 1 OS file descriptor per iteration. Hits EMFILE crash after 1,024 files.

2. Flawed Explicit Close in Finally:
   finally { if (writer != null) writer.close(); }
   --> Result: Reader handles leaked upon any read error; complex nested try-catch required.

3. Scoped Try-With-Resources:
   for (...) { try (Reader r = ...) { ... } }
   --> Result: Strictly 0 leaked handles. O(1) concurrent active file handles.
```

### 5.2 Time and Space Complexity Analysis for $f$ Store Files
Let $f$ be the number of store files, and $r$ be the average number of records per store file.

| Strategy | Processing Time Complexity | Max File Handles Open During Normal Execution | File Handles Leaked Under Failure at Store $k$ ($1 \le k \le f$) |
|---|---|---|---|
| **No Closing** | $O(f \cdot r)$ | $f + 1$ (all files open concurrently) | $k + 1$ handles permanently leaked |
| **Manual Finally (flawed)** | $O(f \cdot r)$ | $2$ (1 writer + 1 active reader) | $1$ reader leaked at failure point $k$ |
| **Try-With-Resources** | $O(f \cdot r)$ | **$2$ (1 writer + 1 active reader)** | **$0$ (guaranteed $O(1)$ space overhead)** |

**Mathematical Proof of Handle Bound:**  
Under scoped try-with-resources:
$$\text{MaxConcurrentHandles}(t) \le 1_{\text{writer}} + 1_{\text{reader}} = 2 = O(1)$$
Regardless of whether $f = 10$ or $f = 100,000$, the system's memory and OS file descriptor footprint remains constant ($O(1)$), completely eliminating `Too many open files` exceptions.
