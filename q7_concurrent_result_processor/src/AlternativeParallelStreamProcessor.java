package q7_concurrent_result_processor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

/**
 * Alternative implementation using Java 8 Parallel Streams (files.parallelStream()).
 * Uses the default Common ForkJoinPool.
 */
public class AlternativeParallelStreamProcessor {

    public static ResultSummary processWithParallelStream(List<String> filePaths) {
        long startTime = System.currentTimeMillis();

        Map<String, StudentResult> resultMap = new ConcurrentHashMap<>(filePaths.size());
        ConcurrentLinkedQueue<ProcessingFailure> failureQueue = new ConcurrentLinkedQueue<>();
        LongAdder successCounter = new LongAdder();
        LongAdder failureCounter = new LongAdder();

        // Process files in parallel stream
        filePaths.parallelStream().forEach(path -> {
            ScoreFileProcessorTask task = new ScoreFileProcessorTask(path);
            TaskOutcome outcome = task.call();
            if (outcome.isSuccess) {
                resultMap.put(outcome.studentId, outcome.result);
                successCounter.increment();
            } else {
                failureQueue.add(outcome.failure);
                failureCounter.increment();
            }
        });

        long elapsedMs = System.currentTimeMillis() - startTime;
        return new ResultSummary(
                filePaths.size(),
                successCounter.intValue(),
                failureCounter.intValue(),
                elapsedMs,
                List.copyOf(failureQueue)
        );
    }
}
