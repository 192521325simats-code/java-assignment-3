package q7_concurrent_result_processor;

/**
 * Entity representing the computed final grade and status.
 */
public record StudentResult(String studentId, String courseCode, double totalMarks, String grade, boolean passed) {
    public static StudentResult compute(StudentScore score) {
        double total = score.getTotalMarks();
        boolean passed = score.endSemMarks() >= 21 && total >= 50;
        String grade = "F";
        if (total >= 90) grade = "O";
        else if (total >= 80) grade = "A+";
        else if (total >= 70) grade = "A";
        else if (total >= 60) grade = "B+";
        else if (total >= 50) grade = "B";

        return new StudentResult(score.studentId(), score.courseCode(), total, grade, passed);
    }
}
