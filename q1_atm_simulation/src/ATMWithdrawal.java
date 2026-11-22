package q1_atm_simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Core ATM withdrawal engine demonstrating multi-layered exception handling,
 * multi-catch blocks, and guaranteed cleanup via the finally block.
 */
public class ATMWithdrawal {
    private double balance;
    private final String accountNumber;
    private final String cardReaderConfigFile;
    private boolean cardEjected = false;
    private boolean sessionTerminated = false;

    public ATMWithdrawal(String accountNumber, double initialBalance, String cardReaderConfigFile) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.cardReaderConfigFile = cardReaderConfigFile;
    }

    /**
     * Executes a withdrawal transaction given raw string inputs, simulating user input parsing.
     *
     * @param rawAmount string representation of withdrawal amount
     * @return true if withdrawal succeeded; false otherwise
     */
    public boolean processWithdrawal(String rawAmount) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("[ATM] Starting transaction for Account: " + accountNumber);
        System.out.println("[ATM] Initial Balance: $" + String.format("%.2f", balance));
        System.out.println("[ATM] Input Amount String: '" + rawAmount + "'");

        try {
            // Failure Point 1: Card reader file missing (Checked Exception)
            verifyCardReaderHardware();

            // Failure Point 2: User types non-numeric text (Unchecked Exception: NumberFormatException)
            double amount = Double.parseDouble(rawAmount);

            // Failure Point 3: Negative or zero amount (Unchecked Exception: IllegalArgumentException)
            if (amount <= 0) {
                throw new IllegalArgumentException("Withdrawal amount must be strictly positive. Entered: " + amount);
            }

            // Failure Point 4: Request exceeds balance (Checked Custom Exception: InsufficientFundsException)
            if (amount > balance) {
                throw new InsufficientFundsException(
                    "Insufficient account balance for withdrawal.", amount, balance);
            }

            // Successful path: State mutation and cash dispensing
            balance -= amount;
            System.out.println("[ATM] SUCCESS: Dispensing $" + String.format("%.2f", amount));
            System.out.println("[ATM] Remaining Balance: $" + String.format("%.2f", balance));
            return true;

        } catch (CardReaderException e) {
            // Checked exception: specific hardware/file failure
            System.err.println("[CATCH - CardReaderException]: " + e.getMessage() 
                    + " (Missing File: " + e.getReaderConfigFile() + ")");
            return false;

        } catch (InsufficientFundsException e) {
            // Custom checked exception: domain rule violation
            System.err.println("[CATCH - InsufficientFundsException]: " + e.getMessage()
                    + " [Requested: $" + String.format("%.2f", e.getRequestedAmount())
                    + ", Available: $" + String.format("%.2f", e.getCurrentBalance()) + "]");
            return false;

        } catch (NumberFormatException | java.util.InputMismatchException e) {
            // Multi-catch for invalid user input format
            System.err.println("[CATCH - MultiCatch (NumberFormat | InputMismatch)]: " + e.getMessage());
            return false;

        } catch (IllegalArgumentException e) {
            // Unchecked exception for negative or zero amounts
            System.err.println("[CATCH - IllegalArgumentException]: " + e.getMessage());
            return false;

        } finally {
            // Guarantees card ejection and session termination under all conditions
            cardEjected = true;
            sessionTerminated = true;
            System.out.println("[FINALLY] Card ejected successfully.");
            System.out.println("[FINALLY] ATM hardware session terminated and audit log synchronized.");
        }
    }

    private void verifyCardReaderHardware() throws CardReaderException {
        File configFile = new File(cardReaderConfigFile);
        if (!configFile.exists()) {
            throw new CardReaderException(
                "Card reader hardware profile missing. Security check failed.",
                cardReaderConfigFile,
                new FileNotFoundException("File not found: " + cardReaderConfigFile)
            );
        }
    }

    public double getBalance() {
        return balance;
    }

    public boolean isCardEjected() {
        return cardEjected;
    }

    public boolean isSessionTerminated() {
        return sessionTerminated;
    }
}
