package q4_thread_lifecycle;

/**
 * Shared Group Work Implementation:
 * Demonstrates the 4 stations using both Thread and Runnable,
 * showing start(), sleep(), join(), and isAlive().
 */
public class ScannerStationDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println("      PARALLEL ANSWER-SCRIPT SCANNING DEMO (SHARED GROUP WORK)        ");
        System.out.println("======================================================================");

        int stationsCount = 4;
        int scriptsPerStation = 20;

        Thread[] stations = new Thread[stationsCount];

        // Stations 0 & 1 extend Thread; Stations 2 & 3 implement Runnable
        stations[0] = new ThreadScannerStation(1, scriptsPerStation);
        stations[1] = new ThreadScannerStation(2, scriptsPerStation);
        stations[2] = new Thread(new RunnableScannerStation(3, scriptsPerStation), "RunnableStation-3");
        stations[3] = new Thread(new RunnableScannerStation(4, scriptsPerStation), "RunnableStation-4");

        System.out.println("[Main] Initial State Verification:");
        for (Thread t : stations) {
            System.out.printf(" - %s: State=%s, isAlive=%b\n", t.getName(), t.getState(), t.isAlive());
        }

        System.out.println("\n[Main] Launching all stations with start()...");
        for (Thread t : stations) {
            t.start();
        }

        // Mid-execution isAlive() check
        Thread.sleep(50);
        System.out.println("\n[Main] Mid-execution check:");
        for (Thread t : stations) {
            System.out.printf(" - %s: State=%s, isAlive=%b\n", t.getName(), t.getState(), t.isAlive());
        }

        System.out.println("\n[Main] Waiting for all stations to finish using join()...");
        for (Thread t : stations) {
            t.join(); // Blocks main thread until target station thread reaches TERMINATED state
        }

        System.out.println("\n[Main] Post-join State Verification:");
        for (Thread t : stations) {
            System.out.printf(" - %s: State=%s, isAlive=%b\n", t.getName(), t.getState(), t.isAlive());
        }

        int totalExpected = stationsCount * scriptsPerStation;
        System.out.println("\n[Main] ALL SCANNER STATIONS FINISHED. Total scripts scanned: " + totalExpected);
    }
}
