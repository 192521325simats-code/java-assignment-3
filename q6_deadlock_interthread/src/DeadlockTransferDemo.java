package q6_deadlock_interthread;

/**
 * Demonstrates deadlock in financial wallet transfers.
 * Two threads acquire locks in opposing orders:
 * Thread 1: Locks Acc1 then Acc2.
 * Thread 2: Locks Acc2 then Acc1.
 */
public class DeadlockTransferDemo {

    public static class Account {
        final int id;
        double balance;

        public Account(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    public static void transferDeadlock(Account from, Account to, double amount) {
        synchronized (from) {
            System.out.printf("[%s] Acquired lock on Account %d; attempting Account %d...\n",
                    Thread.currentThread().getName(), from.id, to.id);
            try {
                Thread.sleep(50); // Simulates network latency, ensuring opposite thread grabs its lock
            } catch (InterruptedException ignored) {}

            synchronized (to) {
                from.balance -= amount;
                to.balance += amount;
                System.out.printf("[%s] Successfully transferred $%.2f from %d to %d\n",
                        Thread.currentThread().getName(), amount, from.id, to.id);
            }
        }
    }

    public static void main(String[] args) {
        Account acc1 = new Account(101, 1000);
        Account acc2 = new Account(102, 1000);

        System.out.println("--- DEMONSTRATING RELIABLE DEADLOCK ---");
        Thread t1 = new Thread(() -> transferDeadlock(acc1, acc2, 100), "TransferThread-1");
        Thread t2 = new Thread(() -> transferDeadlock(acc2, acc1, 150), "TransferThread-2");

        t1.start();
        t2.start();

        // Let threads enter deadlock
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}

        System.out.printf("[Main Monitor] t1 state: %s | t2 state: %s\n", t1.getState(), t2.getState());
        System.out.println("[Main Monitor] DEADLOCK CONFIRMED: Both threads are stuck in BLOCKED state waiting for each other's lock!");
        // We do not join because it would hang indefinitely
    }
}
