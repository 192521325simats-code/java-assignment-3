package q3_resource_safety;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Shared Group Work Verification:
 * Validates that both manual and try-with-resources versions produce identical reports on valid data.
 */
public class SalesReportDemo {

    public static void main(String[] args) throws IOException {
        System.out.println("======================================================================");
        System.out.println("  SHARED GROUP WORK: VERIFYING MANUAL VS TRY-WITH-RESOURCES OUTPUT    ");
        System.out.println("======================================================================");

        List<String> files = List.of("store_north.csv", "store_south.csv", "store_east.csv");

        // Create sample valid data files
        createStoreFile("store_north.csv", new String[]{
                "ITEM-101,10,15.50",
                "ITEM-102,5,40.00"
        });
        createStoreFile("store_south.csv", new String[]{
                "ITEM-201,8,22.00",
                "ITEM-202,12,18.50"
        });
        createStoreFile("store_east.csv", new String[]{
                "ITEM-301,20,12.00",
                "ITEM-302,15,30.00"
        });

        String manualOut = "summary_manual.csv";
        String safeOut = "summary_safe.csv";

        ManualSalesReportGenerator manualGen = new ManualSalesReportGenerator();
        SafeSalesReportGenerator safeGen = new SafeSalesReportGenerator();

        manualGen.generateReport(files, manualOut);
        safeGen.generateReport(files, safeOut);

        // Verify identical report contents
        String manualContent = Files.readString(Path.of(manualOut));
        String safeContent = Files.readString(Path.of(safeOut));

        System.out.println("\nGenerated Manual Report Content:\n" + manualContent);
        System.out.println("Generated Safe Report Content:\n" + safeContent);

        boolean identical = manualContent.equals(safeContent);
        System.out.println(">>> VERIFICATION RESULT: " + (identical ? "IDENTICAL PASS [100% MATCH]" : "MISMATCH FAIL"));

        // Cleanup
        for (String f : files) new File(f).delete();
        new File(manualOut).delete();
        new File(safeOut).delete();
    }

    private static void createStoreFile(String filename, String[] lines) throws IOException {
        try (FileWriter fw = new FileWriter(filename)) {
            for (String l : lines) {
                fw.write(l + "\n");
            }
        }
    }
}
