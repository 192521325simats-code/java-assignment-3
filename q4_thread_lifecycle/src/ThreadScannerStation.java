package q4_thread_lifecycle;

/**
 * Scanner Station implemented by extending java.lang.Thread.
 */
public class ThreadScannerStation extends Thread {
    private final int stationId;
    private final int scriptsToScan;
    private int scriptsScanned = 0;

    public ThreadScannerStation(int stationId, int scriptsToScan) {
        super("ThreadStation-" + stationId);
        this.stationId = stationId;
        this.scriptsToScan = scriptsToScan;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Station %d started scanning batch of %d scripts on Thread: %s\n",
                Thread.currentThread().getName(), stationId, scriptsToScan, Thread.currentThread().getName());

        for (int i = 1; i <= scriptsToScan; i++) {
            try {
                // Simulating optical scanning hardware latency (TIMED_WAITING state)
                Thread.sleep(15);
                scriptsScanned++;
            } catch (InterruptedException e) {
                System.err.printf("[%s] Station %d interrupted!\n", Thread.currentThread().getName(), stationId);
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.printf("[%s] Station %d FINISHED scanning %d scripts.\n",
                Thread.currentThread().getName(), stationId, scriptsScanned);
    }

    public int getScriptsScanned() {
        return scriptsScanned;
    }

    public int getStationId() {
        return stationId;
    }
}
