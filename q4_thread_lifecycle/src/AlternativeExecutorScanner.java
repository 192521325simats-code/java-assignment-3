package q4_thread_lifecycle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Alternative implementation using a Fixed Thread Pool via ExecutorService.
 * Reuses worker threads, controls thread stack allocation, and scales to N batches.
 */
public class AlternativeExecutorScanner {

    public static void main(String[] args) throws InterruptedException {
        int totalScripts = 10_000;
        int batchSize = 100;
        int numBatches = totalScripts / batchSize;
        int poolSize = 4;

        System.out.println("======================================================================");
        System.out.println("   ALTERNATIVE CONCEPT: EXECUTOR SERVICE FIXED THREAD POOL SCANNING   ");
        System.out.println("======================================================================");
        System.out.printf("Total Scripts: %,d | Batches: %d | Worker Pool Size: %d threads\n",
                totalScripts, numBatches, poolSize);

        AtomicInteger totalScanned = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        long startTime = System.currentTimeMillis();

        for (int b = 1; b <= numBatches; b++) {
            final int batchId = b;
            executor.submit(() -> {
                // Simulate processing of one batch
                for (int s = 0; s < batchSize; s++) {
                    totalScanned.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        boolean finished = executor.awaitTermination(30, TimeUnit.SECONDS);

        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("Execution Completed: %b | Total Scanned: %,d | Elapsed: %d ms\n",
                finished, totalScanned.get(), duration);
    }
}
