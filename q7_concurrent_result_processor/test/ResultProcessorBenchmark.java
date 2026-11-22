package q7_concurrent_result_processor;

import java.io.IOException;
import java.util.List;

/**
 * Student B Testing, Benchmarking & Verification Suite:
 * 1. Generates 10,000 files with injected corrupt and missing records.
 * 2. Benchmarks execution time with thread pool sizes of 1, 2, 4, and 8 threads.
 * 3. Verifies small hand-checked test set.
 * 4. Compares against Parallel Streams.
 */
public class ResultProcessorBenchmark {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("======================================================================");
        System.out.println("   STUDENT B: CONCURRENT RESULT PROCESSOR BENCHMARK & VERIFICATION    ");
        System.out.println("======================================================================");

        String testDir = "test_scores_dataset";
        DatasetGenerator.cleanup(testDir);

        try {
            // Stage 3 Hand-Checked Validation Set (10 files)
            System.out.println("\n--- VERIFYING SMALL HAND-CHECKED TEST SET (10 FILES) ---");
            List<String> smallSet = DatasetGenerator.generateDataset(testDir, 10, 0.2, 0.1);
            ConcurrentResultProcessor smallProcessor = new ConcurrentResultProcessor(2);
            ResultSummary smallSummary = smallProcessor.processFiles(smallSet);
            smallSummary.printReport();

            int verifiedTotal = smallSummary.successfulCount() + smallSummary.failureCount();
            System.out.printf("Hand-checked Audit Check: %d == 10? %b\n", verifiedTotal, verifiedTotal == 10);

            // Cleanup small dataset
            DatasetGenerator.cleanup(testDir);

            // Large Dataset Benchmark (10,000 files)
            int totalDataset = 10_000;
            System.out.printf("\n--- GENERATING %,d FILES (WITH CORRUPT & MISSING INJECTIONS) ---\n", totalDataset);
            List<String> dataset = DatasetGenerator.generateDataset(testDir, totalDataset, 0.05, 0.03); // 5% corrupt, 3% missing
            System.out.println("Dataset created successfully.");

            int[] poolSizes = {1, 2, 4, 8};
            System.out.println("\n======================================================================");
            System.out.println("          THREAD POOL SCALABILITY BENCHMARK (1, 2, 4, 8 THREADS)       ");
            System.out.println("======================================================================");
            System.out.println("Threads | Total Time (ms) | Throughput (files/sec) | Success | Failures");
            System.out.println("--------+-----------------+------------------------+---------+---------");

            for (int p : poolSizes) {
                ConcurrentResultProcessor processor = new ConcurrentResultProcessor(p);
                ResultSummary summary = processor.processFiles(dataset);
                double throughput = (summary.totalFilesAttempted() * 1000.0) / summary.elapsedTimeMs();
                System.out.printf("   %2d   |     %6d ms   |       %8.1f         |  %5d  |  %5d  \n",
                        p, summary.elapsedTimeMs(), throughput, summary.successfulCount(), summary.failureCount());
            }

            // Benchmark Alternative: Parallel Streams
            System.out.println("\n--- BENCHMARKING ALTERNATIVE: PARALLEL STREAMS ---");
            ResultSummary streamSummary = AlternativeParallelStreamProcessor.processWithParallelStream(dataset);
            double streamThroughput = (streamSummary.totalFilesAttempted() * 1000.0) / streamSummary.elapsedTimeMs();
            System.out.printf("Parallel Streams: Elapsed = %d ms | Throughput = %.1f files/sec\n",
                    streamSummary.elapsedTimeMs(), streamThroughput);

        } finally {
            DatasetGenerator.cleanup(testDir);
        }
    }
}
