package q5_seat_booking;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Alternative lock-free implementation using AtomicBoolean[] and compareAndSet().
 * Eliminates thread blocking and monitor lock overhead.
 */
public class AlternativeAtomicSeatBooking {
    private final AtomicBoolean[] booked;
    private final AtomicInteger bookingsMade = new AtomicInteger(0);

    public AlternativeAtomicSeatBooking(int seats) {
        booked = new AtomicBoolean[seats];
        for (int i = 0; i < seats; i++) {
            booked[i] = new AtomicBoolean(false);
        }
    }

    public boolean book(int seat) {
        // Hardware-level atomic Compare-And-Swap (CAS)
        if (booked[seat].compareAndSet(false, true)) {
            bookingsMade.incrementAndGet();
            return true;
        }
        return false;
    }

    public int getBookingsMade() {
        return bookingsMade.get();
    }
}
