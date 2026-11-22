package q7_concurrent_result_processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.concurrent.Callable;

/**
 * Callable task processing a single student score file.
 * Encapsulates all possible I/O and format errors so that a single failure
 * never stops the worker thread or crashes the executor pool.
 */
public class ScoreFileProcessorTask implements Callable<TaskOutcome> {
    private final String filePath;

    public ScoreFileProcessorTask(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public TaskOutcome call() {
        File file = new File(filePath);

        // Check for missing file
        if (!file.exists()) {
            return TaskOutcome.failure(filePath, "MISSING_FILE", "File does not exist on disk: " + filePath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null || line.trim().isEmpty()) {
                return TaskOutcome.failure(filePath, "EMPTY_FILE", "File contains no data.");
            }

            String[] tokens = line.split(",");
            if (tokens.length < 4) {
                return TaskOutcome.failure(filePath, "CORRUPT_FORMAT", "Insufficient columns in record: " + line);
            }

            String studentId = tokens[0].trim();
            String courseCode = tokens[1].trim();
            double internal = Double.parseDouble(tokens[2].trim());
            double endSem = Double.parseDouble(tokens[3].trim());

            StudentScore score = new StudentScore(studentId, courseCode, internal, endSem);
            score.validate(); // Validates bounds: internal in [0,40], endSem in [0,60]

            StudentResult result = StudentResult.compute(score);
            return TaskOutcome.success(studentId, result);

        } catch (NumberFormatException e) {
            return TaskOutcome.failure(filePath, "NUMERIC_PARSE_ERROR", "Non-numeric score found: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return TaskOutcome.failure(filePath, "VALIDATION_ERROR", e.getMessage());
        } catch (FileNotFoundException e) {
            return TaskOutcome.failure(filePath, "FILE_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            return TaskOutcome.failure(filePath, "UNKNOWN_IO_ERROR", e.getMessage());
        }
    }
}

class TaskOutcome {
    final boolean isSuccess;
    final String studentId;
    final StudentResult result;
    final ProcessingFailure failure;

    private TaskOutcome(boolean isSuccess, String studentId, StudentResult result, ProcessingFailure failure) {
        this.isSuccess = isSuccess;
        this.studentId = studentId;
        this.result = result;
        this.failure = failure;
    }

    public static TaskOutcome success(String studentId, StudentResult result) {
        return new TaskOutcome(true, studentId, result, null);
    }

    public static TaskOutcome failure(String fileName, String type, String msg) {
        return new TaskOutcome(false, null, null, new ProcessingFailure(fileName, type, msg));
    }
}
