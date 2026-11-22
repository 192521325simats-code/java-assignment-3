package q2_exam_portal;

/**
 * Thrown when the uploaded script exceeds the system allowable file size limit.
 */
public class FileSizeExceededException extends ExamException {
    private final long actualSizeKb;
    private final long maxSizeKb;

    public FileSizeExceededException(String message, long actualSizeKb, long maxSizeKb) {
        super(message);
        this.actualSizeKb = actualSizeKb;
        this.maxSizeKb = maxSizeKb;
    }

    public long getActualSizeKb() {
        return actualSizeKb;
    }

    public long getMaxSizeKb() {
        return maxSizeKb;
    }
}
