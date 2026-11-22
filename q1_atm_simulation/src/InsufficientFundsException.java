package q1_atm_simulation;

/**
 * Custom checked exception thrown when the requested withdrawal amount
 * exceeds the current available account balance.
 */
public class InsufficientFundsException extends Exception {
    private final double requestedAmount;
    private final double currentBalance;

    public InsufficientFundsException(String message, double requestedAmount, double currentBalance) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.currentBalance = currentBalance;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }
}
