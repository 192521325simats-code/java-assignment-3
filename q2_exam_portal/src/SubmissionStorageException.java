package q2_exam_portal;

/**
 * Domain-level storage exception thrown by the storage abstraction layer.
 * Chains low-level IO/disk exceptions to preserve root cause evidence.
 */
public class SubmissionStorageException extends ExamException {
    public SubmissionStorageException(String message) {
        super(message);
    }

    public SubmissionStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
