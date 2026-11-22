package q7_concurrent_result_processor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to generate synthetic test datasets including valid, corrupt, and missing files.
 */
public class DatasetGenerator {

    public static List<String> generateDataset(String directory, int count, double corruptRatio, double missingRatio)
            throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) dir.mkdirs();

        List<String> filePaths = new ArrayList<>(count);

        for (int i = 1; i <= count; i++) {
            String studentId = String.format("STU-%06d", i);
            String fileName = directory + File.separator + studentId + ".csv";
            filePaths.add(fileName);

            double r = Math.random();
            if (r < missingRatio) {
                // Do not create the file -> Simulates missing file!
                continue;
            } else if (r < (missingRatio + corruptRatio)) {
                // Corrupt file (bad format or negative marks)
                try (FileWriter fw = new FileWriter(fileName)) {
                    if (i % 2 == 0) {
                        fw.write(studentId + ",CSA09,-15.0,85.0\n"); // Out of bounds marks!
                    } else {
                        fw.write(studentId + ",CSA09,INVALID_MARKS,50.0\n"); // Non-numeric text!
                    }
                }
            } else {
                // Valid score record
                double internal = 20.0 + (int) (Math.random() * 20); // 20-39
                double endSem = 30.0 + (int) (Math.random() * 30);   // 30-59
                try (FileWriter fw = new FileWriter(fileName)) {
                    fw.write(studentId + ",CSA09," + internal + "," + endSem + "\n");
                }
            }
        }

        return filePaths;
    }

    public static void cleanup(String directory) {
        File dir = new File(directory);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) f.delete();
            }
            dir.delete();
        }
    }
}
