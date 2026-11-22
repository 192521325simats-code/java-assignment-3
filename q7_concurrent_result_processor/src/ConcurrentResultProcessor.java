package q7_concurrent_result_processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

/**
 * Concurrent, Fault-Tolerant Processor for 10,000 answer-script files.
 * Uses an ExecutorService, non-blocking ConcurrentHashMap, and ConcurrentLinkedQueue
 * to guarantee complete audit accuracy and resilience against individual file corruptions.
 */
public class ConcurrentResultProcessor {
    private final int threadPoolSize;

    public ConcurrentResultProcessor(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public ResultSummary processFiles(List<String> filePaths) throws InterruptedException {
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        CompletionService<TaskOutcome> completionService = new ExecutorCompletionService<>(executor);

        Map<String, StudentResult> resultMap = new ConcurrentHashMap<>(filePaths.size());
        ConcurrentLinkedQueue<ProcessingFailure> failureQueue = new ConcurrentLinkedQueue<>();
        LongAdder successCounter = new LongAdder();
        LongAdder failureCounter = new LongAdder();

        // Submit all tasks asynchronously
        for (String path : filePaths) {
            completionService.submit(new ScoreFileProcessorTask(path));
        }

        // Gather all outcomes as they complete
        int total = filePaths.size();
        for (int i = 0; i < total; i++) {
            try {
                Future<TaskOutcome> future = completionService.take();
                TaskOutcome outcome = future.get();
                if (outcome.isSuccess) {
                    resultMap.put(outcome.studentId, outcome.result);
                    successCounter.increment();
                } else {
                    failureQueue.add(outcome.failure);
                    failureCounter.increment();
                }
            } catch (ExecutionException e) {
                // Defensive catch for unexpected worker exceptions
                failureQueue.add(new ProcessingFailure("UNKNOWN", "EXECUTION_EXCEPTION", e.getCause().getMessage()));
                failureCounter.increment();
            }
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long elapsedMs = System.currentTimeMillis() - startTime;
        return new ResultSummary(
                total,
                successCounter.intValue(),
                failureCounter.intValue(),
                elapsedMs,
                new ArrayList<>(failureQueue)
        );
    }
}
