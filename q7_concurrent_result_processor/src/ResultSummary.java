package q7_concurrent_result_processor;

import java.util.List;

/**
 * Summary statistics representing total successes, failures, and execution performance.
 */
public record ResultSummary(
        int totalFilesAttempted,
        int successfulCount,
        int failureCount,
        long elapsedTimeMs,
        List<ProcessingFailure> failuresSample
) {
    public void printReport() {
        System.out.println("\n=======================================================");
        System.out.println("       SEMESTER RESULT PROCESSING FINAL AUDIT REPORT   ");
        System.out.println("=======================================================");
        System.out.printf("Total Files Submitted      : %,d\n", totalFilesAttempted);
        System.out.printf("Successful Results Processed: %,d (%.2f%%)\n",
                successfulCount, (successfulCount * 100.0 / totalFilesAttempted));
        System.out.printf("Failed / Corrupt Files      : %,d (%.2f%%)\n",
                failureCount, (failureCount * 100.0 / totalFilesAttempted));
        System.out.printf("Total Elapsed Wall Time    : %d ms (%.2f seconds)\n",
                elapsedTimeMs, elapsedTimeMs / 1000.0);
        System.out.printf("Throughput                 : %,.1f files/sec\n",
                (totalFilesAttempted * 1000.0) / elapsedTimeMs);

        System.out.println("\nSample Captured Failures (First 5):");
        failuresSample.stream().limit(5).forEach(f ->
                System.out.printf(" - File: %s | Type: %s | Error: %s\n",
                        f.fileName(), f.failureType(), f.errorMessage()));
        System.out.println("=======================================================");
    }
}
