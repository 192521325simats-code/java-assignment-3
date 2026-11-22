package q2_exam_portal;

/**
 * Base checked exception for all business domain errors in the Online Examination Portal.
 * Enforces compile-time handling across architectural boundaries.
 */
public class ExamException extends Exception {
    public ExamException(String message) {
        super(message);
    }

    public ExamException(String message, Throwable cause) {
        super(message, cause);
    }
}
