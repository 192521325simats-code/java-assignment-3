package q2_exam_portal;

/**
 * Thrown by the Service layer when an answer submission is attempted
 * outside the designated examination schedule window.
 */
public class ExamWindowClosedException extends ExamException {
    private final String examId;
    private final long submissionTimeEpochMs;

    public ExamWindowClosedException(String message, String examId, long submissionTimeEpochMs) {
        super(message);
        this.examId = examId;
        this.submissionTimeEpochMs = submissionTimeEpochMs;
    }

    public String getExamId() {
        return examId;
    }

    public long getSubmissionTimeEpochMs() {
        return submissionTimeEpochMs;
    }
}
