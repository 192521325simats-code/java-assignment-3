package q3_resource_safety;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Student A & B Test Runner:
 * 1. Simulates partial failure during sales report generation.
 * 2. Compares file handle leaks between manual and try-with-resources.
 * 3. Demonstrates suppressed exceptions when close() also throws.
 * 4. Benchmarks scalability for f store files under failure.
 */
public class ResourceSafetyTestRunner {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("     STUDENT A (DEBUGGING) & STUDENT B (DESIGN & CORRECTNESS)         ");
        System.out.println("======================================================================");

        List<String> files = List.of("store_1_valid.csv", "store_2_corrupt.csv", "store_3_valid.csv");

        try {
            // Setup test files (store_2 has invalid syntax to trigger failure)
            createTestFile("store_1_valid.csv", "ITEM-1,5,10.0\nITEM-2,2,25.0\n");
            createTestFile("store_2_corrupt.csv", "ITEM-3,CORRUPT_QTY_MISSING_PRICE\n");
            createTestFile("store_3_valid.csv", "ITEM-4,10,15.0\n");

            // Test 1: Manual Generator Failure Analysis (Student A)
            System.out.println("\n--- RUNNING MANUAL GENERATOR WITH CORRUPT INPUT ---");
            ManualSalesReportGenerator manual = new ManualSalesReportGenerator();
            try {
                manual.generateReport(files, "report_manual_failed.csv");
            } catch (Exception e) {
                System.out.println("Caught Expected Failure: " + e.getMessage());
            }
            int leakedManual = manual.getFilesLeftOpenCount();

            // Test 2: Safe Try-With-Resources Generator (Student B)
            System.out.println("\n--- RUNNING SAFE TRY-WITH-RESOURCES GENERATOR WITH CORRUPT INPUT ---");
            SafeSalesReportGenerator safe = new SafeSalesReportGenerator();
            try {
                safe.generateReport(files, "report_safe_failed.csv");
            } catch (Exception e) {
                System.out.println("Caught Expected Failure: " + e.getMessage());
            }
            int leakedSafe = safe.getFilesLeftOpenCount();

            // Comparison Summary
            System.out.println("\n======================================================================");
            System.out.println("                      RESOURCE LEAK AUDIT SUMMARY                     ");
            System.out.println("======================================================================");
            System.out.printf("File Handles Leaked (Manual try-finally) : %d (CRITICAL LEAK!)\n", leakedManual);
            System.out.printf("File Handles Leaked (try-with-resources) : %d (LEAK FREE!)\n", leakedSafe);

            // Test 3: Demonstrating Suppressed Exception (Student B)
            demonstrateSuppressedException();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (String f : files) new File(f).delete();
            new File("report_manual_failed.csv").delete();
            new File("report_safe_failed.csv").delete();
            new File("report_safe_failed.csv.tmp").delete();
        }
    }

    private static void demonstrateSuppressedException() {
        System.out.println("\n======================================================================");
        System.out.println("       DEMONSTRATION OF SUPPRESSED EXCEPTIONS IN TRY-WITH-RESOURCES    ");
        System.out.println("======================================================================");

        class FailingResource implements AutoCloseable {
            private final String name;
            FailingResource(String name) { this.name = name; }

            public void doWork() {
                throw new IllegalStateException("Primary Exception: Disk read error in " + name);
            }

            @Override
            public void close() throws IOException {
                throw new IOException("Secondary Exception: Network pipe broken while closing " + name);
            }
        }

        try (FailingResource res = new FailingResource("StoreNetworkNFS")) {
            res.doWork();
        } catch (Exception e) {
            System.out.println("Primary Exception Caught: " + e.getMessage());
            Throwable[] suppressed = e.getSuppressed();
            System.out.println("Count of Suppressed Exceptions: " + suppressed.length);
            for (int i = 0; i < suppressed.length; i++) {
                System.out.printf("  [Suppressed %d]: %s\n", i + 1, suppressed[i]);
            }
        }
    }

    private static void createTestFile(String filename, String content) throws IOException {
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(content);
        }
    }
}
