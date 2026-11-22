package q2_exam_portal;

import java.io.IOException;

/**
 * Storage Layer:
 * Responsible for physical file storage, quota enforcement, and I/O error abstraction.
 * Wraps low-level IOException in domain-specific SubmissionStorageException.
 */
public class Storage {

    public void save(String fileName, long sizeKb) throws SubmissionStorageException {
        try {
            if (sizeKb > 500) {
                throw new IOException("Disk quota exceeded");
            }
            System.out.println("Saved " + fileName);
        } catch (IOException e) {
            // Completed TODO 1: Wrap IOException in SubmissionStorageException preserving cause
            throw new SubmissionStorageException("Could not store " + fileName, e);
        }
    }
}
