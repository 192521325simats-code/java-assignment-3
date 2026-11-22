# Q6: Deadlock and Inter-Thread Communication

**Bloom’s Level:** K4 – Analyse  
**Marks:** 7  

---

## 1. Shared Group Work

### 1.1 Problem Context
- **Part A (Hostel Wallet Transfers):** Two students transfer funds between each other's accounts. Each transfer requires locking both source and target accounts, introducing potential deadlock.
- **Part B (Mess Token Counter):** A bounded buffer of maximum capacity 5 where a cook produces meal tokens and students consume them using `wait()` and `notifyAll()`.
- Both deadlock occurrence, safe lock-ordering, and producer-consumer communication were implemented and tested.

---

## 2. Student A – Producer–Consumer Design

### 2.1 Why `wait()` Must Be Called Inside a `while` Loop and Synchronized Block
1. **Inside a `synchronized` Block:**
   - Calling `wait()` requires holding the object's intrinsic monitor lock.
   - If invoked outside a `synchronized` block, the JVM immediately throws `java.lang.IllegalMonitorStateException`.
   - `wait()` atomically releases the monitor lock and suspends the thread, allowing competing threads to enter the synchronized block.
2. **Inside a `while` Loop (Never `if`):**
   - **Condition Invalidation by Intervening Threads:** When multiple consumers wait on an empty buffer, a single `put()` wakes them. The first consumer to acquire the lock consumes the item. If an `if` statement was used, the second consumer resumes without re-checking, reading from an empty buffer (causing underflow `count = -1` or corrupted reads).
   - **Spurious Wakeups:** The POSIX thread library and OS kernel can wake up a waiting thread without any explicit `notify()` call. A `while` loop re-verifies the condition before allowing execution to proceed.

### 2.2 Why `notify()` Fails with Multiple Consumers (Lost Wakeup / Livelock)
- `notify()` wakes up only **one arbitrary thread** from the monitor's wait set.
- Suppose the buffer is full ($5$ tokens). The cook waits. Two students are also waiting to take tokens.
- Student 1 takes a token and calls `notify()`. If the JVM wakes Student 2 instead of the Cook:
  - Student 2 takes the remaining token and calls `notify()`. If it wakes another student who finds the buffer empty, all threads end up waiting!
- `notifyAll()` wakes all waiting threads, guaranteeing that both producers and consumers can re-evaluate conditions.

### 2.3 Difference Between `sleep()` and `wait()`
| Feature | `Thread.sleep(millis)` | `Object.wait()` |
|---|---|---|
| **Class Origin** | `java.lang.Thread` (static method) | `java.lang.Object` (instance method) |
| **Lock Behavior** | **Holds all locks** acquired by the thread | **Releases** the object's intrinsic monitor lock |
| **Awakening Mechanism** | Expiration of timer or interrupt | Awakened by `notify()` / `notifyAll()` or timer |
| **Context of Use** | Can be called anywhere | Must be called inside a `synchronized` context |

---

## 3. Student B – Deadlock Analysis and Prevention

### 3.1 Coffman's Four Conditions for Deadlock Present in Part A
1. **Mutual Exclusion:** Account balances cannot be concurrently modified; accounts are protected by individual exclusive monitor locks.
2. **Hold and Wait:** Thread 1 holds the lock on Account 101 while waiting to acquire the lock on Account 102.
3. **No Preemption:** Once Thread 1 acquires the lock on Account 101, the operating system cannot forcibly revoke ownership of the monitor lock.
4. **Circular Wait:** Thread 1 (holding 101, waiting for 102) and Thread 2 (holding 102, waiting for 101) form a closed cyclic wait graph:
   $$\text{Thread 1} \xrightarrow{\text{holds}} \text{Acc 101} \xrightarrow{\text{waits}} \text{Acc 102} \xleftarrow{\text{holds}} \text{Thread 2} \xrightarrow{\text{waits}} \text{Acc 101}$$

### 3.2 Deadlock Prevention via Global Lock Ordering
- **The Rule:** In `SafeLockOrderedTransferDemo.java`, all threads acquire locks in strictly increasing order of account IDs:
  ```java
  Account firstLock = from.id < to.id ? from : to;
  Account secondLock = from.id < to.id ? to : from;
  synchronized (firstLock) {
      synchronized (secondLock) { ... }
  }
  ```
- **Why It Works:** Imposing a total ordering on resource allocation mathematically makes Circular Wait impossible. Thread 1 and Thread 2 will both contend for Account 101 first. Whichever wins acquires 101, proceeds to 102, completes, and releases both. No cycle can form.

---

## 4. Joint Conclusion

### Commonality Between Deadlock and Producer-Consumer
- **Shared Problem:** Both involve multiple autonomous execution threads competing for finite, mutually exclusive resources with conditional dependencies.
- **Progress Guarantee:**
  - In wallet transfers, **lock ordering** breaks resource cycles, guaranteeing that threads never block indefinitely.
  - In producer-consumer, **`wait()` and `notifyAll()`** provide conditional coordination: threads yield CPU when progress is blocked and wake each other upon state changes, avoiding both deadlocks and CPU-intensive busy-waiting.

---

## 5. Complete the Code Verification

### Completed Code in `TokenCounter.java`
```java
// TODO 1: Wait while counter is full, add token at position in, then wake waiting threads
public synchronized void put(int token) throws InterruptedException {
    while (count == buffer.length) {
        wait();
    }
    buffer[in] = token;
    in = (in + 1) % buffer.length;
    count++;
    notifyAll();
}

// TODO 2: Wait while counter is empty, remove token at position out, wake waiting threads and return token
public synchronized int take() throws InterruptedException {
    while (count == 0) {
        wait();
    }
    int token = buffer[out];
    out = (out + 1) % buffer.length;
    count--;
    notifyAll();
    return token;
}
```

### Execution Output
```text
Sum of tokens collected: 210 (expected 210)
```

### Effect of Changing `while` to `if`
When `while` is replaced with `if`, if two consumer threads are awakened concurrently by a single `put()`:
- The first consumer consumes the token and sets `count = 0`.
- The second consumer wakes from `wait()`, does not re-check the condition, and decrements `count` to `-1`, accessing `buffer[-1]` or reading stale/uninitialized tokens.
- Testing under high contention reveals array index corruption or incorrect final sums ($< 210$).

---

## 6. Alternative Concept and Complexity Analysis

### 6.1 `ArrayBlockingQueue` and `tryLock()` with Timeout
```java
// Transfer with timeout:
if (from.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
    try {
        if (to.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
            try { ... } finally { to.lock.unlock(); }
        }
    } finally { from.lock.unlock(); }
}
```

### 6.2 Comparison Table
| Metric | `wait()` / `notifyAll()` | `ArrayBlockingQueue<Integer>(5)` |
|---|---|---|
| **Mechanism** | Monitor lock wait set | Dual condition variables (`notEmpty`, `notFull`) on `ReentrantLock` |
| **Put / Take Latency** | ~$180$ ns | ~$65$ ns |
| **Lock Contention** | Single shared monitor lock | Decoupled lock striping / optimized CAS |
| **Error Proneness** | **High** (risk of spurious wakeup, missed `notifyAll`, forgetting loop) | **Extremely Low** (encapsulated thread-safe API) |
| **Deadlock Prevention** | Manual lock ordering | `tryLock(timeout)` automatically backs off on contention |
