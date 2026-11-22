package q2_exam_portal;

/**
 * Controller Layer:
 * Top-level application endpoint handling incoming student submission requests.
 * Catches ExamException, decodes causes, logs audit diagnostics, and returns student-facing messages.
 */
public class ExamController {
    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    public String handleSubmission(String rollNo, String courseCode, String fileName, long sizeKb) {
        System.out.println("\n[Controller] Received submission request from RollNo: " + rollNo 
                + ", File: " + fileName + " (" + sizeKb + " KB)");
        try {
            examService.submitAnswerScript(rollNo, courseCode, fileName, sizeKb);
            return "SUCCESS: Answer script " + fileName + " submitted successfully!";
        } catch (ExamWindowClosedException e) {
            System.err.println("[Controller Log] Window closed for course: " + e.getExamId());
            return "SUBMISSION REJECTED: The deadline for this examination has passed.";
        } catch (StudentNotRegisteredException e) {
            System.err.println("[Controller Log] Unregistered student attempt: " + e.getStudentRollNumber());
            return "SUBMISSION REJECTED: You are not officially enrolled in " + e.getCourseCode() + ".";
        } catch (FileSizeExceededException e) {
            System.err.println("[Controller Log] File too large: " + e.getActualSizeKb() + " KB");
            return "SUBMISSION REJECTED: Upload size (" + e.getActualSizeKb() + " KB) exceeds maximum allowed limit (" 
                    + e.getMaxSizeKb() + " KB).";
        } catch (SubmissionStorageException e) {
            System.err.println("[Controller Internal Log] Storage subsystem failure: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[Controller Internal Root Cause]: " + e.getCause().getMessage());
            }
            return "SYSTEM ERROR: We could not save your submission due to an internal server storage error. Please contact exam cell.";
        } catch (ExamException e) {
            System.err.println("[Controller Internal Log] Generic Exam Exception: " + e.getMessage());
            return "SUBMISSION REJECTED: " + e.getMessage();
        }
    }
}
