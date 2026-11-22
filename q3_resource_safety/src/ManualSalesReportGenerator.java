package q3_resource_safety;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Flawed / Legacy implementation of the Sales Report Generator using manual try-catch-finally.
 * Leaks file descriptors when an exception occurs during reading or closing.
 */
public class ManualSalesReportGenerator {
    private final List<BufferedReader> activeReaders = new ArrayList<>();
    private int filesLeftOpenCount = 0;

    public boolean generateReport(List<String> storeFiles, String summaryReportPath) throws IOException {
        BufferedWriter writer = null;
        BufferedReader currentReader = null;
        filesLeftOpenCount = 0;
        activeReaders.clear();

        try {
            writer = new BufferedWriter(new FileWriter(summaryReportPath));
            writer.write("=== DAILY RETAIL CHAIN SALES SUMMARY REPORT ===\n");
            writer.write("Store File,Total Items Sold,Gross Revenue ($)\n");

            double totalChainRevenue = 0.0;
            int totalChainItems = 0;

            for (String storeFile : storeFiles) {
                File file = new File(storeFile);
                currentReader = new BufferedReader(new FileReader(file));
                activeReaders.add(currentReader);

                String line;
                int storeItems = 0;
                double storeRevenue = 0.0;

                while ((line = currentReader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith("#")) continue;
                    String[] tokens = line.split(",");
                    if (tokens.length < 3) {
                        // Injected parsing flaw: Throws exception while currentReader is open!
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

                // Manual close inside loop: if exception occurs above, this line is NEVER reached!
                currentReader.close();
                activeReaders.remove(currentReader);
            }

            writer.write(String.format("TOTAL CHAIN,%d,%.2f\n", totalChainItems, totalChainRevenue));
            writer.flush();
            return true;

        } catch (Exception e) {
            System.err.println("[LOG ERROR in Manual Generator]: Failed while processing. Reason: " + e.getMessage());
            throw e;

        } finally {
            // Flawed cleanup: Closes writer, but leaves unclosed reader instances
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Failed to close writer: " + e.getMessage());
                }
            }
            // Count remaining open readers that leaked due to unreached loop close
            filesLeftOpenCount = activeReaders.size();
            System.err.println("[AUDIT - Manual Generator]: Open file descriptors leaked on failure: " 
                    + filesLeftOpenCount);
        }
    }

    public int getFilesLeftOpenCount() {
        return filesLeftOpenCount;
    }
}
