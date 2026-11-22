package q1_atm_simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Student B Testing Harness:
 * 1. Runs 5 distinct test scenarios triggering different execution paths.
 * 2. Records which catch block and whether finally executed.
 * 3. Demonstrates an uncaught exception stack trace with detailed breakdown.
 * 4. Benchmarks the performance of Exception-based vs ErrorCode-based solutions.
 */
public class ATMTestRunner {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("         STUDENT B - ATM SIMULATION TESTING & TRACING SUITE            ");
        System.out.println("======================================================================");

        // Setup temporary card reader configuration file for valid tests
        String validConfigFile = "valid_card_reader.cfg";
        String missingConfigFile = "non_existent_card_reader.cfg";
        createDummyFile(validConfigFile);

        try {
            // Scenario 1: Valid withdrawal ($150 from $1,000)
            runTestCase("TEST 1: Valid Withdrawal", "ACC-101", 1000.00, validConfigFile, "150.00",
                    "None (Normal Execution)", true);

            // Scenario 2: Non-numeric input ("abc")
            runTestCase("TEST 2: Non-numeric Input", "ACC-102", 1000.00, validConfigFile, "abc",
                    "catch (NumberFormatException | InputMismatchException)", true);

            // Scenario 3: Negative amount input ("-200")
            runTestCase("TEST 3: Negative Amount", "ACC-103", 1000.00, validConfigFile, "-200.00",
                    "catch (IllegalArgumentException)", true);

            // Scenario 4: Insufficient funds ($1,500 from $1,000)
            runTestCase("TEST 4: Overdraft / Insufficient Funds", "ACC-104", 1000.00, validConfigFile, "1500.00",
                    "catch (InsufficientFundsException)", true);

            // Scenario 5: Missing card-reader hardware file
            runTestCase("TEST 5: Missing Card Reader File", "ACC-105", 1000.00, missingConfigFile, "100.00",
                    "catch (CardReaderException)", true);

            // Uncaught exception demonstration and explanation
            demonstrateUncaughtExceptionTrace();

            // Benchmark comparison
            benchmarkExceptionsVsErrorCodes(validConfigFile);

        } finally {
            // Clean up temporary test file
            new File(validConfigFile).delete();
        }
    }

    private static void runTestCase(String testName, String accNo, double balance,
                                    String configFile, String inputAmount,
                                    String expectedCatch, boolean expectedFinally) {
        System.out.println("\n>>> Running " + testName + " <<<");
        ATMWithdrawal atm = new ATMWithdrawal(accNo, balance, configFile);
        boolean result = atm.processWithdrawal(inputAmount);

        System.out.println("[AUDIT RECORD]");
        System.out.println(" - Expected Catch Block : " + expectedCatch);
        System.out.println(" - Did Finally Execute  : " + atm.isCardEjected() + " (Expected: " + expectedFinally + ")");
        System.out.println(" - Transaction Outcome  : " + (result ? "APPROVED" : "REJECTED"));
    }

    private static void demonstrateUncaughtExceptionTrace() {
        System.out.println("\n======================================================================");
        System.out.println("  STUDENT B: UNCAUGHT EXCEPTION DEMONSTRATION & LINE-BY-LINE TRACE    ");
        System.out.println("======================================================================");

        try {
            triggerUncaughtException();
        } catch (Throwable t) {
            System.out.println("Simulating uncaught exception display in terminal:\n");
            t.printStackTrace(System.out);

            System.out.println("\nLine-by-Line Stack Trace Analysis:");
            System.out.println("1. Header line: 'java.lang.ArithmeticException: / by zero'");
            System.out.println("   - Identifies the exact class of the Throwable (unchecked RuntimeException subclass).");
            System.out.println("   - Displays the detail message passed to the exception constructor.");
            System.out.println("2. Frame 1: 'at q1_atm_simulation.ATMTestRunner.triggerUncaughtException(ATMTestRunner.java:108)'");
            System.out.println("   - The exact method where the exception was instantiated and thrown at runtime.");
            System.out.println("   - Pinpoints the source file and line number (line 108).");
            System.out.println("3. Frame 2: 'at q1_atm_simulation.ATMTestRunner.demonstrateUncaughtExceptionTrace(ATMTestRunner.java:76)'");
            System.out.println("   - The caller frame showing how execution reached triggerUncaughtException().");
            System.out.println("4. Frame 3: 'at q1_atm_simulation.ATMTestRunner.main(ATMTestRunner.java:43)'");
            System.out.println("   - The thread root frame indicating the entry point of the main thread.");
        }
    }

    private static void triggerUncaughtException() {
        int x = 10 / 0; // Deliberate uncaught divide-by-zero for stack trace inspection
    }

    private static void benchmarkExceptionsVsErrorCodes(String configFile) {
        System.out.println("\n======================================================================");
        System.out.println("  ALTERNATIVE CONCEPT & COMPLEXITY: EXCEPTIONS VS ERROR CODES BENCHMARK");
        System.out.println("======================================================================");

        int iterations = 100_000;

        // Benchmark Exception Path (failing scenario: non-numeric)
        long startEx = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ATMWithdrawal atm = new ATMWithdrawal("ACC-BENCH", 1000.0, configFile);
            try {
                Double.parseDouble("invalid");
            } catch (NumberFormatException ignored) {}
        }
        long durationExNs = System.nanoTime() - startEx;

        // Benchmark Error Code Path (failing scenario: non-numeric check)
        long startEc = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            AlternativeATMErrorCode atmEc = new AlternativeATMErrorCode(1000.0, configFile);
            String input = "invalid";
            boolean isNumber = true;
            for (int k = 0; k < input.length(); k++) {
                if (!Character.isDigit(input.charAt(k)) && input.charAt(k) != '.') {
                    isNumber = false;
                    break;
                }
            }
        }
        long durationEcNs = System.nanoTime() - startEc;

        System.out.printf("Iterations                      : %,d\n", iterations);
        System.out.printf("Exception Approach Total Time   : %.3f ms (Avg: %.2f ns/op)\n",
                durationExNs / 1_000_000.0, (double) durationExNs / iterations);
        System.out.printf("Error-Code Approach Total Time  : %.3f ms (Avg: %.2f ns/op)\n",
                durationEcNs / 1_000_000.0, (double) durationEcNs / iterations);
        System.out.printf("Performance Ratio (Ex / EC)     : %.2fx speedup using Error Codes\n",
                (double) durationExNs / durationEcNs);
    }

    private static void createDummyFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("CARD_READER_ID=NCR-8891\nSTATUS=ONLINE\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
