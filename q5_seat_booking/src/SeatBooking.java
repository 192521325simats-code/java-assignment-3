package q5_seat_booking;

/**
 * Exact assignment class for Q5 Complete the Code.
 * Completes TODO 1 to ensure atomic check-then-book operations.
 */
public class SeatBooking {
    private final boolean[] booked;
    private int bookingsMade = 0;

    public SeatBooking(int seats) {
        booked = new boolean[seats];
    }

    // TODO 1: Make the check-then-book operation atomic so that a seat can never be sold twice
    public synchronized boolean book(int seat) {
        if (!booked[seat]) {
            // Artificial tiny delay to amplify thread interleaving during test
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}

            booked[seat] = true;
            bookingsMade++;
            return true;
        }
        return false;
    }

    public int getBookingsMade() {
        return bookingsMade;
    }

    public static void main(String[] args) throws InterruptedException {
        SeatBooking screen = new SeatBooking(10);
        Thread[] users = new Thread[100];

        for (int i = 0; i < users.length; i++) {
            final int seat = i % 10; // 10 users compete for every seat
            users[i] = new Thread(() -> screen.book(seat));
            users[i].start();
        }

        for (Thread t : users) {
            t.join();
        }

        System.out.println("Seats sold: " + screen.bookingsMade);
    }
}
