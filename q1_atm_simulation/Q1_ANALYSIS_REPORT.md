# Q1: Handling Failure in a Bank ATM Simulation

**Bloom’s Level:** K4 – Analyse  
**Marks:** 6  

---

## 1. Shared Group Work

### 1.1 Identification and Classification of Failure Points
In an automated teller machine (ATM) cash withdrawal workflow, failures can occur at physical hardware boundaries, input sanitization boundaries, and domain rule boundaries. Every failure is mapped and classified in the table below:

| # | Failure Description | Cause / Trigger | Technical Classification | Specific Java Type |
|---|---|---|---|---|
| 1 | Non-numeric Input | User types alphabetic letters or symbols instead of digits | **Unchecked Exception** (`RuntimeException`) | `java.lang.NumberFormatException` / `InputMismatchException` |
| 2 | Negative or Zero Amount | User attempts to withdraw $\le 0$ currency units | **Unchecked Exception** (`RuntimeException`) | `java.lang.IllegalArgumentException` |
| 3 | Insufficient Balance | Requested amount exceeds available account balance | **Checked Exception** (Domain-specific) | `q1_atm_simulation.InsufficientFundsException` |
| 4 | Card Reader File Missing | Hardware descriptor/config file unreadable or absent | **Checked Exception** (`IOException`) | `q1_atm_simulation.CardReaderException` (`FileNotFoundException`) |
| 5 | Physical Dispenser Jam / Memory Fault | Out of physical memory or low-level mechanical fault | **Error** (`java.lang.Error`) | `java.lang.VirtualMachineError` / `DispenserHardwareError` |

---

## 2. Student A – Exception Design

### 2.1 Throwable Hierarchy Relevant to ATM
The diagram below illustrates the exact inheritance hierarchy under `java.lang.Throwable` utilized by the ATM simulation:

```
                          java.lang.Throwable
                                   |
         +-------------------------+-------------------------+
         |                                                   |
 java.lang.Error                                    java.lang.Exception
 (Unrecoverable JVM Failures)                                |
         |                               +-------------------+-------------------+
 java.lang.VirtualMachineError           |                                       |
 (e.g., OutOfMemoryError)        java.io.IOException                java.lang.RuntimeException
                                         |                           (Unchecked Exceptions)
                                 java.io.FileNotFoundException                   |
                                         |                       +---------------+---------------+
                             CardReaderException                 |                               |
                             (Checked Exception)      NumberFormatException           IllegalArgumentException
                                                                 |                               |
                                                                 |                 InsufficientFundsException
                                                                 |                 (Custom Checked Exception)
```

### 2.2 Failure Point Mapping Table
| Failure Point | Exception Type | Checked / Unchecked | Catch Block Location |
|---|---|---|---|
| Card reader file check | `CardReaderException` | Checked | `catch (CardReaderException e)` in `ATMWithdrawal.processWithdrawal()` |
| Numeric string parsing | `NumberFormatException` | Unchecked | `catch (NumberFormatException \| InputMismatchException e)` (Multi-catch) |
| Amount validation ($\le 0$) | `IllegalArgumentException` | Unchecked | `catch (IllegalArgumentException e)` |
| Balance deduction check | `InsufficientFundsException` | Checked | `catch (InsufficientFundsException e)` |
| Terminal / session exit | None (normal or abnormal) | N/A | Handled in `finally` block |

### 2.3 Justification for Catch Block Ordering
1. **Specific Subclasses Before Superclasses:** In Java, exception handling is evaluated sequentially from top to bottom. If a broader superclass catch block (such as `catch (Exception e)`) is placed before a narrower subclass (such as `catch (InsufficientFundsException e)`), the Java compiler reports a compilation error: `error: exception InsufficientFundsException has already been caught`.
2. **Multi-Catch Constraints:** Java strictly forbids alternatives in a multi-catch statement that are related by subclassing (e.g., `catch (NumberFormatException | IllegalArgumentException e)` fails compilation because `NumberFormatException` extends `IllegalArgumentException`). Therefore, unrelated disjoint types (`NumberFormatException | InputMismatchException`) are cleanly combined in multi-catch.
3. **Domain Separation:** Catching `CardReaderException` and `InsufficientFundsException` independently enables distinct recovery actions: hardware diagnostic alerts for card reader failures, and balance overdraft advisories for account balance failures.

### 2.4 Guarantees Provided by the `finally` Block
1. **Unconditional Execution:** The `finally` block is guaranteed to execute regardless of whether the `try` block completes normally, throws a caught exception, or throws an unhandled exception (with the sole exception of `System.exit()` or total JVM crash).
2. **ATM Safety Invariant:** In an ATM, the customer's physical card must be ejected and the terminal session cleared even if a software crash, input parsing error, or network timeout occurs. The `finally` block guarantees card ejection and audit logging, preventing card retention and session hijacking.

---

## 3. Student B – Testing and Tracing

### 3.1 Five Distinct Test Input Scenarios
| Test ID | Input Account & Amount | Expected Path | Catch Block Executed | Did `finally` Execute? | Result |
|---|---|---|---|---|---|
| **Test 1** | `ACC-101`, `$150.00` | Normal valid withdrawal | None | **Yes** (Card ejected) | **APPROVED** |
| **Test 2** | `ACC-102`, `"abc"` | Invalid non-numeric input | `catch (NumberFormatException \| ...)` | **Yes** (Card ejected) | **REJECTED** |
| **Test 3** | `ACC-103`, `"-200.00"` | Negative numeric value | `catch (... \| IllegalArgumentException)` | **Yes** (Card ejected) | **REJECTED** |
| **Test 4** | `ACC-104`, `"$1500.00"` | Amount exceeds balance ($1000) | `catch (InsufficientFundsException)` | **Yes** (Card ejected) | **REJECTED** |
| **Test 5** | `ACC-105`, `"$100.00"` (missing cfg) | Card reader file missing | `catch (CardReaderException)` | **Yes** (Card ejected) | **REJECTED** |

### 3.2 Stack Trace Analysis for an Uncaught Exception
When an uncaught exception (such as `ArithmeticException: / by zero`) occurs, the JVM prints:
```text
java.lang.ArithmeticException: / by zero
    at q1_atm_simulation.ATMTestRunner.triggerUncaughtException(ATMTestRunner.java:108)
    at q1_atm_simulation.ATMTestRunner.demonstrateUncaughtExceptionTrace(ATMTestRunner.java:76)
    at q1_atm_simulation.ATMTestRunner.main(ATMTestRunner.java:43)
```

**Line-by-Line Breakdown:**
- **Line 1 (`java.lang.ArithmeticException: / by zero`):** Specifies the runtime exception class and the internal exception message string generated by the JVM bytecode interpreter.
- **Line 2 (`at ...triggerUncaughtException(ATMTestRunner.java:108)`):** Points to the leaf stack frame where the processor division operation actually failed. It names the package, class, method, source file, and line number.
- **Line 3 (`at ...demonstrateUncaughtExceptionTrace(ATMTestRunner.java:76)`):** Represents the calling frame on the thread's call stack.
- **Line 4 (`at ...main(ATMTestRunner.java:43)`):** Identifies the root frame of the `main` thread where execution initiated.

### 3.3 Broader Catch Placed Before Narrower Catch
Placing `catch (Exception e)` before `catch (InsufficientFundsException e)` causes the Java compiler (`javac`) to reject the code with:
```text
ATMWithdrawal.java:62: error: exception InsufficientFundsException has already been caught
        } catch (InsufficientFundsException e) {
          ^
1 error
```
The compiler detects that `InsufficientFundsException` is a subtype of `Exception`, rendering the second block dead, unreachable code.

---

## 4. Joint Design Question

### Evaluation of Single Catch (`catch (Exception e)`)
- **Acceptability:** A single catch-all block is acceptable **only** at the outer boundary or presentation layer (such as the main event loop or top-level controller) as a safety net / fault barrier to prevent the application from crashing.
- **What is lost by catching everything with `catch (Exception e)`:**
  1. **Loss of Specificity:** The system cannot distinguish between client errors (invalid input), domain errors (overdraft), and system failures (missing files, null pointers).
  2. **Masking of Severe Bugs:** Programming errors like `NullPointerException` or `IndexOutOfBoundsException` are silently swallowed rather than diagnosed.
  3. **Loss of Recovery Semantics:** Hardware errors require technician notification, while overdraft requires informing the user to try a lower amount. A single catch treats all failures identically.

---

## 5. Alternative Concept and Complexity Analysis

### 5.1 Preemptive Error Code Approach vs Exceptions
In `AlternativeATMErrorCode.java`, checks are performed in advance using `Scanner.hasNextDouble()`, `file.exists()`, and range conditionals, returning negative integers (`-1`, `-2`, `-3`, `-4`) on failure:

```java
if (!file.exists()) return STATUS_CARD_READER_FILE_MISSING;
if (!scanner.hasNextDouble()) return STATUS_INVALID_NON_NUMERIC;
if (amount <= 0) return STATUS_NEGATIVE_OR_ZERO_AMOUNT;
if (amount > balance) return STATUS_INSUFFICIENT_FUNDS;
```

### 5.2 Readability and Check Count
- **Check Count:** Every method must duplicate checks and propagate return codes up the call chain through nested conditionals.
- **Readability:** Error code handling tangles normal business logic with error routing, leading to deeply indented code. Exceptions separate the "happy path" cleanly from error handling.

### 5.3 Time and Space Complexity Comparison
| Metric | Exception Approach | Error Code Approach |
|---|---|---|
| **Mechanism** | Stack unrolling & Throwable instantiation | Return value register pass (`eax`/`rax`) |
| **Normal Path Overhead** | Zero runtime cost (table-based zero-cost exceptions) | Continual conditional checks ($O(1)$) |
| **Failure Path Latency** | High (~$1,200$–$3,500$ ns per thrown exception) | Negligible (~$5$–$15$ ns per return) |
| **Heap Memory Allocation** | Allocates `Throwable` instance + stack trace frame array | Zero heap allocation ($0$ bytes) |
| **Garbage Collection Pressure** | Generates short-lived objects on the Eden space | Zero GC pressure |
| **Benchmark Ratio** | **~150x to 300x slower** on repeated failures | Baseline reference |

**Conclusion:** Exceptions should be reserved for exceptional, unpredicted, or cross-boundary domain failures. For high-frequency, expected user input validation, preemptive validation is superior in throughput and latency.
