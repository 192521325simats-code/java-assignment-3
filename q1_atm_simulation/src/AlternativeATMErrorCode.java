package q1_atm_simulation;

import java.io.File;
import java.util.Scanner;

/**
 * Alternative implementation of ATM withdrawal avoiding exceptions entirely.
 * Uses preemptive validation (Scanner.hasNextDouble(), range checks, file checks)
 * and returns status error codes.
 */
public class AlternativeATMErrorCode {
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_CARD_READER_FILE_MISSING = -1;
    public static final int STATUS_INVALID_NON_NUMERIC = -2;
    public static final int STATUS_NEGATIVE_OR_ZERO_AMOUNT = -3;
    public static final int STATUS_INSUFFICIENT_FUNDS = -4;

    private double balance;
    private final String cardReaderConfigFile;
    private boolean cardEjected = false;

    public AlternativeATMErrorCode(double initialBalance, String cardReaderConfigFile) {
        this.balance = initialBalance;
        this.cardReaderConfigFile = cardReaderConfigFile;
    }

    /**
     * Attempts withdrawal using error code returns without throwing exceptions.
     */
    public int processWithdrawalWithCodes(String rawInput) {
        System.out.println("\n[ATM ErrorCode] Processing input: '" + rawInput + "'");

        // Step 1: Pre-check file existence
        File file = new File(cardReaderConfigFile);
        if (!file.exists()) {
            performCleanup();
            return STATUS_CARD_READER_FILE_MISSING;
        }

        // Step 2: Pre-check non-numeric input using Scanner
        Scanner scanner = new Scanner(rawInput.trim());
        if (!scanner.hasNextDouble()) {
            performCleanup();
            return STATUS_INVALID_NON_NUMERIC;
        }

        double amount = scanner.nextDouble();

        // Check if there are trailing invalid characters (e.g. "50abc")
        if (scanner.hasNext()) {
            performCleanup();
            return STATUS_INVALID_NON_NUMERIC;
        }

        // Step 3: Range check for negative / zero amount
        if (amount <= 0.0) {
            performCleanup();
            return STATUS_NEGATIVE_OR_ZERO_AMOUNT;
        }

        // Step 4: Business balance check
        if (amount > balance) {
            performCleanup();
            return STATUS_INSUFFICIENT_FUNDS;
        }

        // Step 5: Success
        balance -= amount;
        System.out.println("[ATM ErrorCode] Dispensed: $" + amount + " | Remaining: $" + balance);
        performCleanup();
        return STATUS_SUCCESS;
    }

    private void performCleanup() {
        cardEjected = true;
        System.out.println("[ATM ErrorCode] Card ejected and session cleared.");
    }

    public double getBalance() {
        return balance;
    }

    public boolean isCardEjected() {
        return cardEjected;
    }

    public static String getStatusMessage(int statusCode) {
        return switch (statusCode) {
            case STATUS_SUCCESS -> "SUCCESS: Withdrawal completed.";
            case STATUS_CARD_READER_FILE_MISSING -> "ERROR (-1): Card reader configuration file missing.";
            case STATUS_INVALID_NON_NUMERIC -> "ERROR (-2): Non-numeric characters entered.";
            case STATUS_NEGATIVE_OR_ZERO_AMOUNT -> "ERROR (-3): Amount must be greater than zero.";
            case STATUS_INSUFFICIENT_FUNDS -> "ERROR (-4): Requested amount exceeds available balance.";
            default -> "ERROR: Unknown status code.";
        };
    }
}
