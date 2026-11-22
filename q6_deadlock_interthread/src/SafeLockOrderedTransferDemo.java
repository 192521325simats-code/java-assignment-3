package q6_deadlock_interthread;

/**
 * Deadlock prevention using Global Consistent Lock Ordering.
 * Always acquires locks based on deterministic account ID ordering,
 * breaking Coffman's Circular Wait condition.
 */
public class SafeLockOrderedTransferDemo {

    public static class Account {
        final int id;
        double balance;

        public Account(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    public static void transferSafe(Account from, Account to, double amount) {
        // Enforce deterministic global lock order
        Account firstLock = from.id < to.id ? from : to;
        Account secondLock = from.id < to.id ? to : from;

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;
                    System.out.printf("[%s] SAFE TRANSFER: $%.2f from Account %d to %d (Acc%d=$%.2f, Acc%d=$%.2f)\n",
                            Thread.currentThread().getName(), amount, from.id, to.id,
                            from.id, from.balance, to.id, to.balance);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Account acc1 = new Account(101, 1000);
        Account acc2 = new Account(102, 1000);

        System.out.println("--- TESTING DEADLOCK-FREE SAFE TRANSFERS ---");
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 50; i++) transferSafe(acc1, acc2, 10);
        }, "SafeTransfer-1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 50; i++) transferSafe(acc2, acc1, 10);
        }, "SafeTransfer-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.printf("Completed all 100 concurrent transfers without deadlock! Acc1: $%.2f, Acc2: $%.2f\n",
                acc1.balance, acc2.balance);
    }
}
