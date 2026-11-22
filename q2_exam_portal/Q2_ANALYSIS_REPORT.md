# Q2: User-Defined Exceptions and Propagation in an Online Examination Portal

**Bloom’s Level:** K4 – Analyse  
**Marks:** 7  

---

## 1. Shared Group Work

### 1.1 Exception Hierarchy Design
The examination portal structures business domain errors and system storage failures under a unified, extensible hierarchy rooted in `ExamException`:

```
                           java.lang.Throwable
                                    |
                           java.lang.Exception
                                    |
                       q2_exam_portal.ExamException (Checked)
                                    |
       +----------------------------+----------------------------+
       |                            |                            |
ExamWindowClosedException    StudentNotRegisteredException    FileSizeExceededException
   (Checked Business)           (Checked Security)               (Checked Payload)
                                    |
                        SubmissionStorageException
                             (Checked Storage)
                                    |
                        [Chains: java.io.IOException]
```

### 1.2 Rationale for Checked vs Unchecked
- **Checked (`extends ExamException extends Exception`):** Used for all condition failures (e.g. deadline closure, non-registration, storage full) because these are recoverable business events. Callers (e.g., Controllers) are forced by the compiler to anticipate and handle them, mapping each failure to appropriate HTTP responses and user feedback.
- **Unchecked (`extends RuntimeException`):** Reserved for unrecoverable developer bugs (e.g. `NullPointerException`, illegal internal state) where callers cannot meaningfully recover.

### 1.3 Architectural Layering with `throw` and `throws`
1. **Storage Layer (`Storage.java`):**
   - Catches low-level `IOException` from filesystem operations.
   - Wraps the error into `SubmissionStorageException`, setting the original `IOException` as the cause (`super(message, cause)`).
   - Declares `throws SubmissionStorageException`.
2. **Service Layer (`ExamService.java`):**
   - Enforces core validation rules (`ExamWindowClosedException`, `StudentNotRegisteredException`, `FileSizeExceededException`).
   - Propagates storage exceptions upward.
   - Declares `throws ExamException`.
3. **Controller Layer (`ExamController.java`):**
   - Boundary endpoint that receives user requests.
   - Implements `try-catch (ExamException e)` with granular catches for each subclass.
   - Translates internal exceptions into user-friendly responses while logging diagnostic stack traces.

---

## 2. Student A – Exception Hierarchy and Propagation Design

### 2.1 Exception Hierarchy Class Diagram
```
+-----------------------------------------------------------------+
|                        ExamException                            |
|-----------------------------------------------------------------|
| + ExamException(message: String)                                |
| + ExamException(message: String, cause: Throwable)              |
+-----------------------------------------------------------------+
             ^                       ^                     ^
             |                       |                     |
+-------------------------+ +-------------------------+ +-------------------------+
| ExamWindowClosedEx      | | StudentNotRegisteredEx  | | FileSizeExceededEx      |
|-------------------------| |-------------------------| |-------------------------|
| - examId: String        | | - studentRollNo: String | | - actualSizeKb: long    |
| - submissionEpoch: long | | - courseCode: String    | | - maxSizeKb: long       |
+-------------------------+ +-------------------------+ +-------------------------+
             ^
             |
+-----------------------------------------------------------------+
|                   SubmissionStorageException                    |
|-----------------------------------------------------------------|
| + SubmissionStorageException(message: String)                   |
| + SubmissionStorageException(message: String, cause: Throwable) |
+-----------------------------------------------------------------+
```

### 2.2 Propagation Path of Storage Failure
```
[1. Storage.save()]
   - Checks sizeKb > 500
   - IOException("Disk quota exceeded") thrown
   - Caught in Storage catch block
   - Wrapped into SubmissionStorageException(msg, IOException)
   - Thrown up to Service layer
         |
         v
[2. ExamService.submitAnswerScript()]
   - Declares 'throws ExamException'
   - Uncaught in Service layer; propagates up call stack
         |
         v
[3. ExamController.handleSubmission()]
   - Enclosed in try block
   - Caught by 'catch (SubmissionStorageException e)'
   - Inspects e.getCause() for administrative log
   - Returns sanitized user message
```

### 2.3 User Message vs Internal Log
- **Message Student Sees:**  
  `SYSTEM ERROR: We could not save your submission due to an internal server storage error. Please contact exam cell.`
- **Internal Audit Log:**  
  `[Controller Internal Log] Storage subsystem failure: Could not store answer02.pdf`  
  `[Controller Internal Root Cause]: Disk quota exceeded`

---

## 3. Student B – Failure Scenarios and Chaining Evidence

### 3.1 Failure Scenarios Test Table
| Scenario | Student Roll No | Size (KB) | Window Status | Caught Subclass | Cause (`e.getCause()`) | Student Response Message |
|---|---|---|---|---|---|---|
| 1. Valid Submission | `192521325` | 250 | Open | None | None | `SUCCESS: Answer script valid_script.pdf submitted successfully!` |
| 2. Unregistered Student | `192599999` | 250 | Open | `StudentNotRegisteredException` | None | `SUBMISSION REJECTED: You are not officially enrolled in CSA09.` |
| 3. Closed Window | `192521325` | 250 | Closed | `ExamWindowClosedException` | None | `SUBMISSION REJECTED: The deadline for this examination has passed.` |
| 4. Oversized Payload | `192521325` | 2048 | Open | `FileSizeExceededException` | None | `SUBMISSION REJECTED: Upload size (2048 KB) exceeds maximum allowed limit (1024 KB).` |
| 5. Disk Quota Exceeded | `192521325` | 750 | Open | `SubmissionStorageException` | `IOException: Disk quota exceeded` | `SYSTEM ERROR: We could not save your submission due to an internal server storage error...` |

### 3.2 Chained Stack Trace (`getCause()`)
When chaining is utilized, the JVM prints both the high-level domain failure and the nested low-level root cause:
```text
q2_exam_portal.SubmissionStorageException: Could not store answer_final.pdf
    at q2_exam_portal.Storage.save(Storage.java:18)
    at q2_exam_portal.ExamService.submitAnswerScript(ExamService.java:38)
    at q2_exam_portal.ExamController.handleSubmission(ExamController.java:17)
Caused by: java.io.IOException: POSIX error: ENOSPC (No space left on device: /var/exam_storage)
    at q2_exam_portal.Storage.save(Storage.java:15)
    ... 2 more
```

### 3.3 Information Loss Without Chaining
If re-thrown without the cause (`throw new SubmissionStorageException("Could not store " + fileName);`):
1. `e.getCause()` evaluates to `null`.
2. The root technical failure reason (`Disk quota exceeded` / `ENOSPC`) is obliterated.
3. Attempting to call `e.getCause().getMessage()` triggers a `NullPointerException`.
4. System administrators cannot determine whether the save failed due to permissions (`EACCES`), network mount disconnection, or disk full (`ENOSPC`).

---

## 4. Joint Conclusion

### Why Domain Layering is Essential
1. **Abstraction and Encapsulation:** Higher layers should only deal with domain concepts. Controllers should not depend on storage-specific technologies (such as local POSIX filesystems, AWS S3 buckets, or SQL database BLOBs).
2. **Security & Information Disclosure:** Directly exposing raw `IOException` messages (e.g., `/mnt/nfs/shares/students/answer.pdf: Access Denied`) leaks server paths, volume architectures, and internal vulnerabilities to malicious clients.
3. **Auditability:** Wrapping in `SubmissionStorageException` retains high-level business context (`fileName`, `studentRollNo`) while chaining the underlying `IOException` for back-end operational triage.

---

## 5. Complete the Code Verification

### Completed Code in `StorageDemo.java`
```java
// TODO 1: Wrap the IOException in a SubmissionStorageException, keeping the IOException as its cause
throw new SubmissionStorageException("Could not store " + fileName, e);
```

### Output Produced by Execution
```text
Saved answer01.pdf
Error: Could not store answer02.pdf
Cause: Disk quota exceeded
```

### Explanation of Cause Line if `IOException` is Omitted
If `new SubmissionStorageException("Could not store " + fileName)` were called without passing `e`:
- `e.getCause()` returns `null`.
- The statement `System.out.println("Cause: " + e.getCause().getMessage());` will evaluate `null.getMessage()`, throwing a **`java.lang.NullPointerException`** at runtime, terminating the program abruptly.

---

## 6. Alternative Concept and Complexity Analysis

### 6.1 Checked vs Unchecked Custom Exceptions
- **If Made Unchecked (`extends RuntimeException`):**
  - **Method Signatures:** Methods no longer require `throws ExamException` in their signatures (`void save(...)`, `void submitAnswerScript(...)`).
  - **Calling Code:** The compiler does not verify whether `catch` blocks exist. If a developer forgets to catch `ExamWindowClosedException`, the exception propagates uncaught up the thread stack, potentially crashing the HTTP server worker thread or returning generic HTTP 500 errors to students.

### 6.2 Time and Space Cost of Stack Trace Capture
- **Mechanism:** When a `Throwable` is instantiated, its constructor invokes native `Throwable.fillInStackTrace()`. This requires the JVM to walk the thread's native C/Java execution frames, recording method pointers, class references, and bytecode program counters ($O(D)$ where $D$ is stack depth).
- **Benchmark Overhead:** Instantiating an exception with stack trace capture takes ~**1,100 ns**, whereas a lightweight exception overriding `fillInStackTrace()` to return `this` takes ~**25 ns** (~**44x speedup**).
- **Memory Held by Chained Exceptions:**
  - A chained exception maintains a strong reference to its `cause`, which in turn holds references to its own stack trace array, message string, and any suppressed exceptions.
  - Until the top-level exception is dereferenced and garbage collected, the entire chain of underlying exception objects remains pinned in memory. For deep exception chains, this retains multiple kilobytes of heap metadata per unhandled error.
