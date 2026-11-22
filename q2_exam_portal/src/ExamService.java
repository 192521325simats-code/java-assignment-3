package q2_exam_portal;

import java.util.Set;

/**
 * Service Layer:
 * Enforces business domain logic: window checking, student course registration,
 * file size thresholds, and coordinates persistence via Storage.
 */
public class ExamService {
    private final Storage storage;
    private final Set<String> registeredStudents = Set.of("192521325", "192521326", "192521327");
    private final boolean windowOpen;

    public ExamService(Storage storage, boolean windowOpen) {
        this.storage = storage;
        this.windowOpen = windowOpen;
    }

    public void submitAnswerScript(String rollNo, String courseCode, String fileName, long sizeKb)
            throws ExamException {
        // Business Rule 1: Registration Check
        if (!registeredStudents.contains(rollNo)) {
            throw new StudentNotRegisteredException(
                "Student " + rollNo + " is not registered for course " + courseCode,
                rollNo, courseCode);
        }

        // Business Rule 2: Window Check
        if (!windowOpen) {
            throw new ExamWindowClosedException(
                "Exam window has closed for submission.", courseCode, System.currentTimeMillis());
        }

        // Business Rule 3: File Size Rule
        if (sizeKb > 1024) { // Service max limit: 1024 KB
            throw new FileSizeExceededException(
                "File size exceeds institutional limit.", sizeKb, 1024);
        }

        // Delegate persistence to Storage layer
        storage.save(fileName, sizeKb);
    }
}
