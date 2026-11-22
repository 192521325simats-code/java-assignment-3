# CSA09 – Advanced Java Programming (Assignment 3)
## Exception Handling, Multithreading, Concurrency, and Fault-Tolerant Systems

This repository contains the complete, compilable, and experimentally benchmarked implementations for all seven questions (Q1 to Q7) of **CSA09 Programming in Java – Assignment 3** (Bloom's Taxonomy Levels K4 – Analyse and K5 – Evaluate).

---

## Repository Structure & Question Mapping

```
java-assignment-3/
├── README.md
├── q1_atm_simulation/
│   ├── src/
│   │   ├── InsufficientFundsException.java
│   │   ├── CardReaderException.java
│   │   ├── ATMWithdrawal.java
│   │   └── AlternativeATMErrorCode.java
│   ├── test/
│   │   └── ATMTestRunner.java
│   └── Q1_ANALYSIS_REPORT.md
│
├── q2_exam_portal/
│   ├── src/
│   │   ├── ExamException.java
│   │   ├── ExamWindowClosedException.java
│   │   ├── StudentNotRegisteredException.java
│   │   ├── SubmissionStorageException.java
│   │   ├── FileSizeExceededException.java
│   │   ├── Storage.java
│   │   ├── ExamService.java
│   │   ├── ExamController.java
│   │   └── StorageDemo.java
│   ├── test/
│   │   └── ExamPortalTestRunner.java
│   └── Q2_ANALYSIS_REPORT.md
│
├── q3_resource_safety/
│   ├── src/
│   │   ├── ManualSalesReportGenerator.java
│   │   ├── SafeSalesReportGenerator.java
│   │   └── SalesReportDemo.java
│   ├── test/
│   │   └── ResourceSafetyTestRunner.java
│   └── Q3_ANALYSIS_REPORT.md
│
├── q4_thread_lifecycle/
│   ├── src/
│   │   ├── ThreadScannerStation.java
│   │   ├── RunnableScannerStation.java
│   │   ├── ScannerStationDemo.java
│   │   └── AlternativeExecutorScanner.java
│   ├── test/
│   │   └── ThreadLifeCycleTestRunner.java
│   └── Q4_ANALYSIS_REPORT.md
│
├── q5_seat_booking/
│   ├── src/
│   │   ├── SeatBooking.java
│   │   ├── UnsynchronizedSeatBooking.java
│   │   ├── SynchronizedBlockSeatBooking.java
│   │   └── AlternativeAtomicSeatBooking.java
│   ├── test/
│   │   └── SeatBookingBenchmark.java
│   └── Q5_ANALYSIS_REPORT.md
│
├── q6_deadlock_interthread/
│   ├── src/
│   │   ├── DeadlockTransferDemo.java
│   │   ├── SafeLockOrderedTransferDemo.java
│   │   ├── TokenCounter.java
│   │   └── AlternativeBlockingQueueDemo.java
│   ├── test/
│   │   └── InterThreadCommunicationTestRunner.java
│   └── Q6_ANALYSIS_REPORT.md
│
└── q7_concurrent_result_processor/
    ├── src/
    │   ├── StudentScore.java
    │   ├── StudentResult.java
    │   ├── ProcessingFailure.java
    │   ├── ResultSummary.java
    │   ├── ScoreFileProcessorTask.java
    │   ├── ConcurrentResultProcessor.java
    │   ├── DatasetGenerator.java
    │   └── AlternativeParallelStreamProcessor.java
    ├── test/
    │   └── ResultProcessorBenchmark.java
    └── Q7_ANALYSIS_REPORT.md
```

---

## Assignment Overview & Key Deliverables

| Question | Topic | Core Focus | Key Concepts & Code Artifacts |
|---|---|---|---|
| **Q1** | Bank ATM Simulation | Exception Design & Catch Ordering | `try`, multi-catch (`NumberFormatException \| IllegalArgumentException`), `finally` guarantees, `Throwable` hierarchy, error codes vs exceptions. |
| **Q2** | Online Examination Portal | User-Defined Exceptions & Propagation | Architectural layering (Storage $\rightarrow$ Service $\rightarrow$ Controller), exception chaining (`super(msg, cause)`), `StorageDemo.java` (TODO 1 completed). |
| **Q3** | Daily Sales Report Generator | Resource Safety & Memory Leaks | `AutoCloseable`, `try-with-resources` vs manual `finally`, file descriptor leaks (`EMFILE`), atomic temp-file replace, suppressed exceptions (`getSuppressed()`). |
| **Q4** | Parallel Answer-Script Scanning | Thread Creation & Life Cycle | `Thread` vs `Runnable`, `start()` vs `run()`, `sleep()`, `join()`, `isAlive()`, thread priorities, daemon threads, thread scheduler non-determinism. |
| **Q5** | Cinema Seat-Booking System | Race Conditions & Critical Sections | Check-then-act race reproduction, double booking prevention, `synchronized` methods, fine-grained `synchronized` blocks, lock-free `AtomicBoolean[]` CAS, `SeatBooking.java` (TODO 1 completed). |
| **Q6** | Deadlock & Inter-Thread Communication | Deadlock Prevention & Bounded Buffers | Coffman's 4 conditions, global lock ordering, producer-consumer token counter (`wait()`, `notifyAll()`), `while` vs `if` condition loops, `TokenCounter.java` (TODO 1 & 2 completed). |
| **Q7** | Concurrent Fault-Tolerant Processor | High-Scale Concurrent Architecture | $10,000$ score files, `ExecutorService`, `Callable`, `Future`, `ConcurrentHashMap`, `ConcurrentLinkedQueue`, `LongAdder`, benchmark across 1, 2, 4, 8 threads. |

---

## Compilation and Execution Instructions

### Prerequisites
- Java Development Kit (JDK 17 or higher recommended; fully tested on JDK 21+).
- Standard Java SE libraries only (zero external Maven/Gradle dependencies required).

### Compile Everything
From the root directory:
```bash
# Compile all source and test files across all questions
javac -d bin q1_atm_simulation/src/*.java q1_atm_simulation/test/*.java
javac -d bin q2_exam_portal/src/*.java q2_exam_portal/test/*.java
javac -d bin q3_resource_safety/src/*.java q3_resource_safety/test/*.java
javac -d bin q4_thread_lifecycle/src/*.java q4_thread_lifecycle/test/*.java
javac -d bin q5_seat_booking/src/*.java q5_seat_booking/test/*.java
javac -d bin q6_deadlock_interthread/src/*.java q6_deadlock_interthread/test/*.java
javac -d bin q7_concurrent_result_processor/src/*.java q7_concurrent_result_processor/test/*.java
```

### Run Demonstrations and Test Suites
```bash
# Q1: Bank ATM Simulation Test Suite
java -cp bin q1_atm_simulation.ATMTestRunner

# Q2: Online Exam Portal StorageDemo & Chaining Suite
java -cp bin q2_exam_portal.StorageDemo
java -cp bin q2_exam_portal.ExamPortalTestRunner

# Q3: Resource Safety & Sales Report Generator
java -cp bin q3_resource_safety.SalesReportDemo
java -cp bin q3_resource_safety.ResourceSafetyTestRunner

# Q4: Thread Life Cycle & Scanning Stations
java -cp bin q4_thread_lifecycle.ScannerStationDemo
java -cp bin q4_thread_lifecycle.ThreadLifeCycleTestRunner

# Q5: Seat Booking Race Condition & Benchmark
java -cp bin q5_seat_booking.SeatBooking
java -cp bin q5_seat_booking.SeatBookingBenchmark

# Q6: Inter-Thread Communication & Deadlock Transfers
java -cp bin q6_deadlock_interthread.TokenCounter
java -cp bin q6_deadlock_interthread.SafeLockOrderedTransferDemo
java -cp bin q6_deadlock_interthread.InterThreadCommunicationTestRunner

# Q7: High-Throughput Concurrent Result Processor (10,000 Files)
java -cp bin q7_concurrent_result_processor.ResultProcessorBenchmark
```
