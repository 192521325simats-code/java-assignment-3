package q5_seat_booking;

/**
 * Implementation using synchronized blocks.
 * Demonstrates fine-grained per-seat locks instead of coarse instance-level locks.
 */
public class SynchronizedBlockSeatBooking {
    private final boolean[] booked;
    private final Object[] seatLocks; // Dedicated lock per seat
    private int bookingsMade = 0;
    private final Object countLock = new Object();

    public SynchronizedBlockSeatBooking(int seats) {
        this.booked = new boolean[seats];
        this.seatLocks = new Object[seats];
        for (int i = 0; i < seats; i++) {
            seatLocks[i] = new Object();
        }
    }

    public boolean book(int seat) {
        // Fine-grained lock: Locks ONLY the target seat monitor!
        synchronized (seatLocks[seat]) {
            if (!booked[seat]) {
                booked[seat] = true;
                synchronized (countLock) {
                    bookingsMade++;
                }
                return true;
            }
            return false;
        }
    }

    public int getBookingsMade() {
        synchronized (countLock) {
            return bookingsMade;
        }
    }
}
