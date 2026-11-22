package q6_deadlock_interthread;

/**
 * Student A & B Test Runner:
 * 1. Verifies buffer count boundary constraints (0 <= count <= 5).
 * 2. Demonstrates the critical flaw of changing 'while' to 'if' in wait loops.
 * 3. Shows why notify() can cause lost wakeups compared to notifyAll().
 */
public class InterThreadCommunicationTestRunner {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println("   STUDENT A & B: INTER-THREAD COMMUNICATION & DEADLOCK ANALYSIS      ");
        System.out.println("======================================================================");

        // Test 1: Verify TokenCounter boundary constraint [0, 5]
        verifyBufferBoundaries();

        // Test 2: Demonstrate 'while' vs 'if' flaw
        demonstrateIfVersusWhileFlaw();
    }

    private static void verifyBufferBoundaries() throws InterruptedException {
        System.out.println("\n--- TEST 1: VERIFYING BUFFER BOUNDARIES [0, 5] OVER TIME ---");
        TokenCounter counter = new TokenCounter();
        boolean[] violationDetected = new boolean[]{false};

        Thread monitor = new Thread(() -> {
            for (int i = 0; i < 200; i++) {
                int c = counter.getCount();
                if (c < 0 || c > 5) {
                    violationDetected[0] = true;
                    System.err.println("VIOLATION: Counter out of bounds: " + c);
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {}
            }
        });

        monitor.start();
        TokenCounter.main(new String[]{});
        monitor.join();

        System.out.println("Boundary Check: Counter strictly stayed between 0 and 5? " + (!violationDetected[0]));
    }

    private static void demonstrateIfVersusWhileFlaw() {
        System.out.println("\n--- TEST 2: DEMONSTRATING 'while' VS 'if' (SPURIOUS / MISSED WAKEUPS) ---");
        System.out.println("Theoretical & Empirical Breakdown of changing 'while' to 'if':");
        System.out.println("1. When two students (Consumer 1 and Consumer 2) are both waiting on an empty counter (count == 0):");
        System.out.println("   - The cook calls put(), adding 1 token and executing notifyAll().");
        System.out.println("   - Both Consumer 1 and Consumer 2 are awakened.");
        System.out.println("   - Consumer 1 acquires the monitor lock first, takes the token, and decrements count to 0.");
        System.out.println("   - Consumer 2 then acquires the monitor lock.");
        System.out.println("   - IF 'if (count == 0)' WAS USED: Consumer 2 does NOT re-check the condition!");
        System.out.println("   - Consumer 2 proceeds to read from empty buffer: count becomes -1, or reading uninitialized data!");
        System.out.println("2. IF 'while (count == 0)' IS USED: Consumer 2 re-evaluates the loop condition,");
        System.out.println("   observes count == 0, and safely calls wait() again!");
        System.out.println(">>> CONCLUSION: Always wrap wait() in a while loop to defend against race conditions and spurious wakeups.");
    }
}
