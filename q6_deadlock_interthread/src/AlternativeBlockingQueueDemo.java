package q6_deadlock_interthread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Alternative modern concurrency implementation using ArrayBlockingQueue
 * and ReentrantLock with tryLock(timeout) for transfers.
 */
public class AlternativeBlockingQueueDemo {

    public static class LockAccount {
        final int id;
        double balance;
        final ReentrantLock lock = new ReentrantLock();

        public LockAccount(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    public static boolean transferWithTryLock(LockAccount from, LockAccount to, double amount, long timeoutMs)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            if (from.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                try {
                    if (to.lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                        try {
                            if (from.balance >= amount) {
                                from.balance -= amount;
                                to.balance += amount;
                                return true;
                            }
                            return false;
                        } finally {
                            to.lock.unlock();
                        }
                    }
                } finally {
                    from.lock.unlock();
                }
            }
            // Backoff before retry to avoid livelock
            Thread.sleep((int) (Math.random() * 5));
        }
        return false; // Timed out, averted deadlock!
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- ALTERNATIVE: ARRAY BLOCKING QUEUE FOR MESS TOKENS ---");
        BlockingQueue<Integer> tokenQueue = new ArrayBlockingQueue<>(5);

        Thread cook = new Thread(() -> {
            try {
                for (int t = 1; t <= 20; t++) {
                    tokenQueue.put(t); // Automatically handles blocking on full!
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        int[] total = new int[1];
        Thread student = new Thread(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    total[0] += tokenQueue.take(); // Automatically handles blocking on empty!
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        cook.start();
        student.start();
        cook.join();
        student.join();

        System.out.println("ArrayBlockingQueue sum of tokens: " + total[0] + " (expected: 210)");
    }
}
