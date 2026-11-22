package q2_exam_portal;

/**
 * Thrown when an unverified student or an unregistered roll number
 * attempts to submit answers for a course.
 */
public class StudentNotRegisteredException extends ExamException {
    private final String studentRollNumber;
    private final String courseCode;

    public StudentNotRegisteredException(String message, String studentRollNumber, String courseCode) {
        super(message);
        this.studentRollNumber = studentRollNumber;
        this.courseCode = courseCode;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public String getCourseCode() {
        return courseCode;
    }
}
