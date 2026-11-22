package q2_exam_portal;

import java.io.IOException;

/**
 * Student B Testing Suite for Q2:
 * 1. Executes scenarios for each exception type.
 * 2. Demonstrates chained stack traces using getCause().
 * 3. Shows what diagnostic information is lost when re-thrown without a cause.
 * 4. Compares checked vs unchecked design implications and stack trace capturing overhead.
 */
public class ExamPortalTestRunner {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("     STUDENT B - ONLINE EXAMINATION PORTAL TEST SUITE & TRACING       ");
        System.out.println("======================================================================");

        Storage storage = new Storage();
        ExamService openService = new ExamService(storage, true);
        ExamService closedService = new ExamService(storage, false);

        ExamController controller = new ExamController(openService);
        ExamController closedController = new ExamController(closedService);

        // Scenario 1: Valid Submission
        System.out.println("\n--- SCENARIO 1: Valid Submission ---");
        String res1 = controller.handleSubmission("192521325", "CSA09", "valid_script.pdf", 250);
        System.out.println("Student Response: " + res1);

        // Scenario 2: Unregistered Student
        System.out.println("\n--- SCENARIO 2: Unregistered Student ---");
        String res2 = controller.handleSubmission("192599999", "CSA09", "script.pdf", 250);
        System.out.println("Student Response: " + res2);

        // Scenario 3: Exam Window Closed
        System.out.println("\n--- SCENARIO 3: Exam Window Closed ---");
        String res3 = closedController.handleSubmission("192521325", "CSA09", "script.pdf", 250);
        System.out.println("Student Response: " + res3);

        // Scenario 4: File Size Limit Exceeded (> 1024 KB)
        System.out.println("\n--- SCENARIO 4: Institutional File Size Exceeded ---");
        String res4 = controller.handleSubmission("192521325", "CSA09", "oversized.pdf", 2048);
        System.out.println("Student Response: " + res4);

        // Scenario 5: Storage Layer Quota Exceeded (Chained Exception)
        System.out.println("\n--- SCENARIO 5: Storage Layer Disk Quota (Chained Exception) ---");
        String res5 = controller.handleSubmission("192521325", "CSA09", "disk_quota_fail.pdf", 750);
        System.out.println("Student Response: " + res5);

        // Demonstration of Information Loss without Chaining
        demonstrateInformationLoss();

        // Benchmark stack trace capture overhead
        benchmarkStackTraceCapture();
    }

    private static void demonstrateInformationLoss() {
        System.out.println("\n======================================================================");
        System.out.println("        STUDENT B: EXCEPTION CHAINING VS INFORMATION LOSS             ");
        System.out.println("======================================================================");

        // WITH CHAINING:
        try {
            try {
                throw new IOException("POSIX error: ENOSPC (No space left on device: /var/exam_storage)");
            } catch (IOException io) {
                throw new SubmissionStorageException("Could not store answer_final.pdf", io);
            }
        } catch (SubmissionStorageException e) {
            System.out.println("\n[WITH CHAINING]");
            System.out.println("e.getMessage() : " + e.getMessage());
            System.out.println("e.getCause()    : " + e.getCause());
            System.out.println("Cause Message  : " + e.getCause().getMessage());
            System.out.println("Chained Stack Trace:");
            e.printStackTrace(System.out);
        }

        // WITHOUT CHAINING:
        try {
            try {
                throw new IOException("POSIX error: ENOSPC (No space left on device: /var/exam_storage)");
            } catch (IOException io) {
                // Re-thrown without cause
                throw new SubmissionStorageException("Could not store answer_final.pdf");
            }
        } catch (SubmissionStorageException e) {
            System.out.println("\n[WITHOUT CHAINING - INFORMATION LOST]");
            System.out.println("e.getMessage() : " + e.getMessage());
            System.out.println("e.getCause()    : " + e.getCause());
            System.out.println("Root Cause Message: LOST! (getCause() is null; calling getCause().getMessage() throws NullPointerException)");
            System.out.println("Severed Stack Trace (Root cause origin omitted):");
            e.printStackTrace(System.out);
        }
    }

    private static void benchmarkStackTraceCapture() {
        System.out.println("\n======================================================================");
        System.out.println("    ALTERNATIVE CONCEPT & COMPLEXITY: STACK TRACE CAPTURE OVERHEAD   ");
        System.out.println("======================================================================");

        int iterations = 100_000;

        // Standard Exception (captures stack trace via fillInStackTrace())
        long startStandard = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Exception ex = new ExamException("Benchmarking standard exception");
        }
        long durationStandardNs = System.nanoTime() - startStandard;

        // Light Exception without stack trace capture
        long startNoTrace = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Exception ex = new LightExamException("Benchmarking light exception");
        }
        long durationNoTraceNs = System.nanoTime() - startNoTrace;

        System.out.printf("Iterations                       : %,d\n", iterations);
        System.out.printf("Standard Exception (with trace)  : %.3f ms (Avg: %.2f ns/op)\n",
                durationStandardNs / 1_000_000.0, (double) durationStandardNs / iterations);
        System.out.printf("Lightweight Exception (no trace) : %.3f ms (Avg: %.2f ns/op)\n",
                durationNoTraceNs / 1_000_000.0, (double) durationNoTraceNs / iterations);
        System.out.printf("Stack Trace Capture Overhead     : %.2fx cost incurred solely by fillInStackTrace()\n",
                (double) durationStandardNs / durationNoTraceNs);
    }

    // Custom lightweight exception overriding fillInStackTrace() for benchmark comparison
    static class LightExamException extends ExamException {
        public LightExamException(String msg) {
            super(msg);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this; // Skips native stack walk
        }
    }
}
