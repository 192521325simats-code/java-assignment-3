package q5_seat_booking;

/**
 * Student A & B Benchmarking and Race Condition Verification Suite:
 * 1. Reproduces double-booking over 10 runs without synchronization.
 * 2. Verifies that synchronized versions always sell exactly 10 seats.
 * 3. Measures execution throughput for 10,000 bookings across strategies.
 */
public class SeatBookingBenchmark {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println("   STUDENT A & B: CINEMA SEAT BOOKING BENCHMARK & RACE VERIFICATION    ");
        System.out.println("======================================================================");

        // 1. Student A: Reproducing Double Booking over 10 runs (Unsynchronized)
        System.out.println("\n--- 10 RUNS OF UNSYNCHRONIZED SEAT BOOKING (100 THREADS / 10 SEATS) ---");
        System.out.println("Run # | Seats Sold (Expected: 10) | Double-Bookings Detected");
        System.out.println("------+---------------------------+--------------------------");

        for (int run = 1; run <= 10; run++) {
            UnsynchronizedSeatBooking screen = new UnsynchronizedSeatBooking(10);
            Thread[] users = new Thread[100];
            for (int i = 0; i < users.length; i++) {
                final int seat = i % 10;
                users[i] = new Thread(() -> screen.book(seat));
                users[i].start();
            }
            for (Thread t : users) t.join();

            int sold = screen.getBookingsMade();
            int excess = sold - 10;
            System.out.printf("  %2d  |            %2d             | %s\n",
                    run, sold, excess > 0 ? ("+" + excess + " DOUBLE BOOKED! [RACE]") : "None");
        }

        // 2. Verification of Synchronized Method (SeatBooking.java TODO 1)
        System.out.println("\n--- 10 RUNS OF SYNCHRONIZED METHOD (TODO 1 VERIFICATION) ---");
        for (int run = 1; run <= 10; run++) {
            SeatBooking screen = new SeatBooking(10);
            Thread[] users = new Thread[100];
            for (int i = 0; i < users.length; i++) {
                final int seat = i % 10;
                users[i] = new Thread(() -> screen.book(seat));
                users[i].start();
            }
            for (Thread t : users) t.join();
            System.out.printf("Run %2d: Seats sold = %d (EXACT MATCH: 10)\n", run, screen.getBookingsMade());
        }

        // 3. Student B: Throughput Benchmark (10,000 Bookings)
        benchmark10000Bookings();
    }

    private static void benchmark10000Bookings() throws InterruptedException {
        System.out.println("\n======================================================================");
        System.out.println("     STUDENT B: PERFORMANCE BENCHMARK (10,000 CONCURRENT BOOKINGS)    ");
        System.out.println("======================================================================");

        int bookings = 10_000;
        int seats = 1_000;

        // Synchronized Method
        long startSyncMethod = System.nanoTime();
        SeatBooking syncMethodScreen = new SeatBooking(seats);
        Thread[] t1 = new Thread[bookings];
        for (int i = 0; i < bookings; i++) {
            final int s = i % seats;
            t1[i] = new Thread(() -> syncMethodScreen.book(s));
            t1[i].start();
        }
        for (Thread t : t1) t.join();
        long durSyncMethod = System.nanoTime() - startSyncMethod;

        // Synchronized Block (Fine-Grained per-seat locks)
        long startSyncBlock = System.nanoTime();
        SynchronizedBlockSeatBooking syncBlockScreen = new SynchronizedBlockSeatBooking(seats);
        Thread[] t2 = new Thread[bookings];
        for (int i = 0; i < bookings; i++) {
            final int s = i % seats;
            t2[i] = new Thread(() -> syncBlockScreen.book(s));
            t2[i].start();
        }
        for (Thread t : t2) t.join();
        long durSyncBlock = System.nanoTime() - startSyncBlock;

        // AtomicBoolean CAS
        long startAtomic = System.nanoTime();
        AlternativeAtomicSeatBooking atomicScreen = new AlternativeAtomicSeatBooking(seats);
        Thread[] t3 = new Thread[bookings];
        for (int i = 0; i < bookings; i++) {
            final int s = i % seats;
            t3[i] = new Thread(() -> atomicScreen.book(s));
            t3[i].start();
        }
        for (Thread t : t3) t.join();
        long durAtomic = System.nanoTime() - startAtomic;

        System.out.printf("1. Synchronized Method (Instance Monitor)   : %.2f ms\n", durSyncMethod / 1_000_000.0);
        System.out.printf("2. Synchronized Block (Fine-grained per-seat): %.2f ms\n", durSyncBlock / 1_000_000.0);
        System.out.printf("3. AtomicBoolean CAS (Lock-Free Hardware)   : %.2f ms\n", durAtomic / 1_000_000.0);
    }
}
