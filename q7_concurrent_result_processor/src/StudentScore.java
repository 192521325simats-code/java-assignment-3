package q7_concurrent_result_processor;

/**
 * Immutable entity representing raw scores parsed from an answer-script file.
 */
public record StudentScore(String studentId, String courseCode, double internalMarks, double endSemMarks) {
    public void validate() {
        if (internalMarks < 0 || internalMarks > 40) {
            throw new IllegalArgumentException("Internal marks out of valid range [0, 40]: " + internalMarks);
        }
        if (endSemMarks < 0 || endSemMarks > 60) {
            throw new IllegalArgumentException("End-sem marks out of valid range [0, 60]: " + endSemMarks);
        }
    }

    public double getTotalMarks() {
        return internalMarks + endSemMarks;
    }
}
