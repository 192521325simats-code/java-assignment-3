package q4_thread_lifecycle;

/**
 * Scanner Station implemented using the Runnable interface.
 * Promotes composition over inheritance and decouples task from execution mechanism.
 */
public class RunnableScannerStation implements Runnable {
    private final int stationId;
    private final int scriptsToScan;
    private int scriptsScanned = 0;

    public RunnableScannerStation(int stationId, int scriptsToScan) {
        this.stationId = stationId;
        this.scriptsToScan = scriptsToScan;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Runnable Station %d started scanning %d scripts.\n",
                Thread.currentThread().getName(), stationId, scriptsToScan);

        for (int i = 1; i <= scriptsToScan; i++) {
            try {
                Thread.sleep(15);
                scriptsScanned++;
            } catch (InterruptedException e) {
                System.err.printf("[%s] Runnable Station %d interrupted!\n",
                        Thread.currentThread().getName(), stationId);
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.printf("[%s] Runnable Station %d FINISHED scanning %d scripts.\n",
                Thread.currentThread().getName(), stationId, scriptsScanned);
    }

    public int getScriptsScanned() {
        return scriptsScanned;
    }

    public int getStationId() {
        return stationId;
    }
}
