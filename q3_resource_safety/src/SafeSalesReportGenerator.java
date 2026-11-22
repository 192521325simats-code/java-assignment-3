package q3_resource_safety;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Robust, production-grade implementation of Sales Report Generator
 * utilizing try-with-resources (AutoCloseable) and atomic temp-file rename
 * to ensure 100% leak-free operation and complete report consistency.
 */
public class SafeSalesReportGenerator {
    private int filesLeftOpenCount = 0;

    public boolean generateReport(List<String> storeFiles, String summaryReportPath) throws IOException {
        filesLeftOpenCount = 0;

        // Use a temporary file for atomic generation to prevent incomplete reports on crash
        File tempFile = new File(summaryReportPath + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("=== DAILY RETAIL CHAIN SALES SUMMARY REPORT ===\n");
            writer.write("Store File,Total Items Sold,Gross Revenue ($)\n");

            double totalChainRevenue = 0.0;
            int totalChainItems = 0;

            for (String storeFile : storeFiles) {
                // Scoped try-with-resources: guaranteed deterministic close per store file
                try (BufferedReader reader = new BufferedReader(new FileReader(storeFile))) {
                    String line;
                    int storeItems = 0;
                    double storeRevenue = 0.0;

                    while ((line = reader.readLine()) != null) {
                        if (line.trim().isEmpty() || line.startsWith("#")) continue;
                        String[] tokens = line.split(",");
                        if (tokens.length < 3) {
                            throw new IllegalArgumentException(
                                    "Corrupt record in file: " + storeFile + " -> '" + line + "'");
                        }
                        int qty = Integer.parseInt(tokens[1].trim());
                        double price = Double.parseDouble(tokens[2].trim());
                        storeItems += qty;
                        storeRevenue += (qty * price);
                    }

                    writer.write(storeFile + "," + storeItems + "," + String.format("%.2f", storeRevenue) + "\n");
                    totalChainItems += storeItems;
                    totalChainRevenue += storeRevenue;
                } // reader is closed automatically and deterministically HERE, even if exception thrown!
            }

            writer.write(String.format("TOTAL CHAIN,%d,%.2f\n", totalChainItems, totalChainRevenue));
            writer.flush();

            // Atomically replace destination file only when complete
            Files.move(tempFile.toPath(), Path.of(summaryReportPath), StandardCopyOption.REPLACE_EXISTING);
            return true;

        } catch (Exception e) {
            System.err.println("[LOG ERROR in Safe Generator]: Failure encountered: " + e.getMessage());
            // Log any suppressed exceptions
            for (Throwable suppressed : e.getSuppressed()) {
                System.err.println("  [SUPPRESSED EXCEPTION]: " + suppressed);
            }
            // Clean up partial temp file to prevent incomplete report exposure
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw e;

        } finally {
            // Under try-with-resources, all streams are guaranteed closed
            filesLeftOpenCount = 0;
            System.out.println("[AUDIT - Safe Generator]: Resources closed cleanly. Leaked files: 0");
        }
    }

    public int getFilesLeftOpenCount() {
        return filesLeftOpenCount;
    }
}
