# Q4: Thread Creation and the Thread Life Cycle

**Bloom’s Level:** K4 – Analyse  
**Marks:** 7  

---

## 1. Shared Group Work

### 1.1 Parallel Answer-Script Scanning System
To process answer scripts across four scanning stations concurrently:
- **`ThreadScannerStation`:** Extends `java.lang.Thread` directly.
- **`RunnableScannerStation`:** Implements `java.lang.Runnable`.
- **Synchronization & Control:** Utilizes `start()` to spawn threads, `Thread.sleep()` to simulate optical sensor delay, `join()` to block the main coordinating thread until scanning completes, and `isAlive()` to verify thread state.

---

## 2. Student A – Thread Design

### 2.1 Extending `Thread` vs Implementing `Runnable`
| Feature / Aspect | Extending `Thread` | Implementing `Runnable` |
|---|---|---|
| **OOP Design Principle** | Tight coupling; violates Single Responsibility Principle (class becomes both a thread and a task) | Loose coupling; separates the task execution (`Runnable`) from the execution mechanism (`Thread`) |
| **Inheritance Constraint** | Java does not support multiple inheritance; extending `Thread` prevents extending any other domain class | Class can implement `Runnable` while freely extending another domain class (e.g. `extends HardwareDevice`) |
| **Thread Pooling Support** | Cannot be submitted directly to `ExecutorService` thread pools for task execution | Native compatibility with `ExecutorService`, `ScheduledExecutorService`, and `CompletableFuture` |
| **Recommendation** | Use only when overriding thread behavior (e.g., custom `Thread` class with custom thread-local storage) | **Strongly preferred in 99% of enterprise and production software** |

### 2.2 Thread Life-Cycle State Diagram
The Java Thread Life Cycle contains 6 distinct states defined in `java.lang.Thread.State`:

```
               +---------------------------------------------------+
               |                                                   |
               v                                                   |
        [ NEW ] ----(start())----> [ RUNNABLE ] <--(notify/time)--+
                                      |   ^
                    (Monitor Lock)    |   |  (Lock Acquired)
                    Contention        v   |
                                  [ BLOCKED ]
                                      |
                     +----------------+----------------+
                     |                                 |
           (wait()/join())                   (sleep(t)/join(t))
                     v                                 v
                [ WAITING ]                    [ TIMED_WAITING ]
                     |                                 |
                     +----------------+----------------+
                                      |
                           (run() completes / unhandled error)
                                      v
                               [ TERMINATED ]
```

**Station Transition Walkthrough:**
1. **`NEW`:** `ThreadScannerStation station = new ThreadScannerStation(1, 20);` (Instantiated; no OS native thread allocated yet).
2. **`RUNNABLE`:** `station.start();` (Native thread allocated; placed in OS scheduler ready-to-run queue).
3. **`TIMED_WAITING`:** `Thread.sleep(15);` (Thread voluntarily yields CPU and sleeps for $15$ ms; awakened by system timer interrupt).
4. **`WAITING`:** Main thread calling `station.join();` transitions to `WAITING` until the station thread terminates.
5. **`TERMINATED`:** Station finishes scanning all scripts and exits `run()`. `isAlive()` returns `false`.

### 2.3 Why Calling `run()` Instead of `start()` Does Not Create a New Thread
- **Direct Evidence from `ThreadLifeCycleTestRunner`:**
  - Calling `t.run()` executes:
    `Executing Thread Name: 'main' | Thread ID: 1 | isAlive: false`
  - Calling `t.start()` executes:
    `Executing Thread Name: 'WorkerStationThread' | Thread ID: 26 | isAlive: true`
- **Architectural Reason:**
  - `run()` is a standard Java method containing user code. Invoking `t.run()` is merely a normal synchronous method call on the caller thread stack (`main`).
  - `start()` is a native method (`private native void start0()`). It requests the host OS kernel to allocate a new native thread, stack memory, program counter, and registers, and subsequently schedules the JVM's `run()` method on that newly created native thread.

### 2.4 How `join()` Ensures Correct Final Totals
- The call `station.join()` invokes the internal monitor lock mechanism of the `Thread` object.
- The calling thread (`main`) enters the `WAITING` state, releasing its CPU timeslice.
- When the target thread terminates, the JVM triggers `notifyAll()` internally on the thread instance, waking up the `main` thread.
- This guarantees a **happens-before** memory relationship under the Java Memory Model (JMM): all writes performed by the scanning stations are guaranteed to be visible to the `main` thread before `join()` returns.

---

## 3. Student B – Behavioural Experiments

### 3.1 Ten Repeated Runs (Recording Station Finish Order)
Due to CPU core scheduling, cache warmth, and OS thread time-slicing, 10 consecutive executions produced non-deterministic output orderings:

| Trial # | Observed Station Completion Order |
|---|---|
| Trial 1 | Station 2 $\rightarrow$ Station 1 $\rightarrow$ Station 4 $\rightarrow$ Station 3 |
| Trial 2 | Station 1 $\rightarrow$ Station 3 $\rightarrow$ Station 2 $\rightarrow$ Station 4 |
| Trial 3 | Station 4 $\rightarrow$ Station 2 $\rightarrow$ Station 1 $\rightarrow$ Station 3 |
| Trial 4 | Station 3 $\rightarrow$ Station 1 $\rightarrow$ Station 4 $\rightarrow$ Station 2 |
| Trial 5 | Station 1 $\rightarrow$ Station 4 $\rightarrow$ Station 2 $\rightarrow$ Station 3 |
| Trial 6 | Station 2 $\rightarrow$ Station 4 $\rightarrow$ Station 3 $\rightarrow$ Station 1 |
| Trial 7 | Station 4 $\rightarrow$ Station 3 $\rightarrow$ Station 1 $\rightarrow$ Station 2 |
| Trial 8 | Station 3 $\rightarrow$ Station 2 $\rightarrow$ Station 4 $\rightarrow$ Station 1 |
| Trial 9 | Station 1 $\rightarrow$ Station 2 $\rightarrow$ Station 3 $\rightarrow$ Station 4 |
| Trial 10 | Station 2 $\rightarrow$ Station 1 $\rightarrow$ Station 3 $\rightarrow$ Station 4 |

### 3.2 Thread Priority Experiment
- Station 1 was assigned `Thread.MIN_PRIORITY (1)`, and Station 4 was assigned `Thread.MAX_PRIORITY (10)`.
- **Result:** Priority did **not** consistently guarantee that Station 4 finished before Station 1.
- **Root Cause:** Modern operating system thread schedulers (e.g. Linux CFS, Windows Scheduler) prioritize thread starvation avoidance and fair multi-core load balancing. Java thread priorities ($1$ to $10$) are advisory hints mapped loosely (or ignored entirely) by the underlying kernel.

### 3.3 Daemon Thread Experiment
- Station was flagged as daemon via `daemonStation.setDaemon(true)`.
- **Result:** As soon as all non-daemon user threads (e.g. `main`) finished, the JVM abruptly terminated immediately.
- The daemon thread was killed mid-execution without printing its final completion message and without executing un-enclosed cleanup code.
- **Takeaway:** Daemon threads are suitable only for non-critical background services (e.g., GC, heartbeats, JMX monitoring), never for persistent I/O transactions.

---

## 4. Joint Conclusion

### Why Output Orders Differ and Why Correct Programs Must Be Order-Agnostic
1. **Preemptive Multitasking:** The OS scheduler preempts threads whenever their CPU time slice ($10$–$20$ ms) expires or whenever hardware interrupts occur.
2. **Multi-Core Parallelism:** Threads run simultaneously on separate physical CPU cores at differing clock speeds, memory cache latencies, and bus contention.
3. **Correctness Invariant:** A multithreaded program is correct **if and only if** its safety and liveness invariants hold under **all possible valid thread execution interleavings**. If correctness depends on Thread A finishing before Thread B without explicit synchronization (`join()`, `CountDownLatch`, `Phaser`), the program suffers from a fatal race condition bug.

---

## 5. Alternative Concept and Complexity Analysis

### 5.1 Fixed Thread Pool via `ExecutorService`
Instead of creating manual `Thread` objects:
```java
ExecutorService executor = Executors.newFixedThreadPool(4);
for (int i = 0; i < numBatches; i++) {
    executor.submit(scannerTask);
}
executor.shutdown();
executor.awaitTermination(30, TimeUnit.SECONDS);
```

### 5.2 Time and Space Complexity Comparison ($n$ scripts, $p$ threads)
| Metric | Manual `Thread` Creation per Batch | `ExecutorService` (Fixed Thread Pool) |
|---|---|---|
| **Thread Instantiation Cost** | Creates $n / \text{batch}$ native OS threads | Creates exactly $p$ reusable OS threads ($O(1)$) |
| **Total Expected Completion Time** | $O\left(\frac{n}{p} \cdot t_{\text{script}}\right) + O(B \cdot t_{\text{spawn}})$ | **$O\left(\frac{n}{p} \cdot t_{\text{script}}\right)$ (Zero thread recreation overhead)** |
| **Stack Memory Footprint** | $B \times \text{StackSize}$ (e.g., $1,000 \times 1\text{MB} = 1\text{GB}$ RAM) | **$p \times \text{StackSize}$ (e.g., $4 \times 1\text{MB} = 4\text{MB}$ RAM)** |
| **Failure Mode Under High Load** | `java.lang.OutOfMemoryError: unable to create new native thread` | Stable; tasks wait gracefully in an unbounded blocking queue |
| **Context Switching Overhead** | Severe ($O(B)$ thread contention degrades CPU cache) | Minimal ($p$ threads matched to physical hardware cores) |
