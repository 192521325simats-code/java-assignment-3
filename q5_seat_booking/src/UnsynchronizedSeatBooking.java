package q5_seat_booking;

/**
 * Flawed / Unsynchronized implementation reproducing the check-then-act race condition.
 */
public class UnsynchronizedSeatBooking {
    private final boolean[] booked;
    private int bookingsMade = 0;

    public UnsynchronizedSeatBooking(int seats) {
        booked = new boolean[seats];
    }

    public boolean book(int seat) {
        // NON-ATOMIC CHECK-THEN-ACT
        if (!booked[seat]) {
            try {
                // Yield/sleep amplifies probability of concurrent thread preemption
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}

            booked[seat] = true;
            bookingsMade++; // NON-ATOMIC INCREMENT
            return true;
        }
        return false;
    }

    public int getBookingsMade() {
        return bookingsMade;
    }
}
