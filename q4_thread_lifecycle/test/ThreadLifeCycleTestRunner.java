package q4_thread_lifecycle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Student B Experimental Testing Suite:
 * 1. Executes 10 runs to record finishing order of stations (proving non-determinism).
 * 2. Experiments with thread priorities.
 * 3. Demonstrates daemon thread behavior when main() ends.
 * 4. Demonstrates run() vs start() behavior (Student A proof).
 */
public class ThreadLifeCycleTestRunner {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("======================================================================");
        System.out.println("     STUDENT B - THREAD BEHAVIOURAL EXPERIMENTS & TRACING             ");
        System.out.println("======================================================================");

        // Experiment 1: 10 Runs recording completion order
        runTenTrialsOrderRecording();

        // Experiment 2: Thread Priorities Experiment
        runPriorityExperiment();

        // Experiment 3: Daemon Thread Experiment
        runDaemonThreadExperiment();

        // Student A Proof: start() vs run()
        demonstrateStartVsRun();
    }

    private static void runTenTrialsOrderRecording() throws InterruptedException {
        System.out.println("\n--- EXPERIMENT 1: 10 REPEATED RUNS (OBSERVING FINISH ORDER) ---");
        System.out.println("Trial # | Finishing Order of Scanner Stations");
        System.out.println("--------+--------------------------------------------------");

        for (int trial = 1; trial <= 10; trial++) {
            List<String> finishOrder = Collections.synchronizedList(new ArrayList<>());
            Thread[] stations = new Thread[4];

            for (int i = 0; i < 4; i++) {
                final int stationId = i + 1;
                stations[i] = new Thread(() -> {
                    try {
                        Thread.sleep((int) (Math.random() * 20)); // tiny jitter
                    } catch (InterruptedException ignored) {}
                    finishOrder.add("S" + stationId);
                });
            }

            for (Thread t : stations) t.start();
            for (Thread t : stations) t.join();

            System.out.printf("   %2d   | %s\n", trial, String.join(" -> ", finishOrder));
        }
    }

    private static void runPriorityExperiment() throws InterruptedException {
        System.out.println("\n--- EXPERIMENT 2: THREAD PRIORITY EXPERIMENT ---");
        System.out.println("Assigning Station 1: MIN_PRIORITY (1) vs Station 4: MAX_PRIORITY (10)");

        List<String> finishOrder = Collections.synchronizedList(new ArrayList<>());
        Thread s1 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) Math.sin(i);
            finishOrder.add("Station-1 (Priority 1)");
        });
        Thread s4 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) Math.sin(i);
            finishOrder.add("Station-4 (Priority 10)");
        });

        s1.setPriority(Thread.MIN_PRIORITY);
        s4.setPriority(Thread.MAX_PRIORITY);

        s1.start();
        s4.start();
        s1.join();
        s4.join();

        System.out.println("Finish Order observed: " + String.join(" -> ", finishOrder));
        System.out.println("Conclusion: Priority is an advisory hint to the OS thread scheduler. Modern OS preemptive schedulers do not guarantee priority order.");
    }

    private static void runDaemonThreadExperiment() throws InterruptedException {
        System.out.println("\n--- EXPERIMENT 3: DAEMON THREAD BEHAVIOUR ---");

        Thread daemonStation = new Thread(() -> {
            try {
                System.out.println("[Daemon Station] Running long background audit...");
                Thread.sleep(3000);
                System.out.println("[Daemon Station] FINISHED (Should NEVER print if main terminates early!)");
            } catch (InterruptedException e) {
                System.out.println("[Daemon Station] Interrupted.");
            }
        });

        daemonStation.setDaemon(true);
        daemonStation.start();

        System.out.printf("[Main] Started daemon thread '%s'. isDaemon=%b\n",
                daemonStation.getName(), daemonStation.isDaemon());
        Thread.sleep(100);
        System.out.println("[Main] Main thread exiting now. Notice JVM halts immediately without waiting for daemon!");
    }

    private static void demonstrateStartVsRun() {
        System.out.println("\n--- STUDENT A EVIDENCE: CALLING run() VS start() ---");

        Runnable task = () -> {
            System.out.printf("Executing Thread Name: '%s' | Thread ID: %d | isAlive: %b\n",
                    Thread.currentThread().getName(),
                    Thread.currentThread().getId(),
                    Thread.currentThread().isAlive());
        };

        Thread t = new Thread(task, "WorkerStationThread");

        System.out.println("\n1. Direct call to run():");
        t.run(); // Synchronous call on caller thread!

        System.out.println("\n2. Call to start():");
        t.start(); // Spawns new native thread!
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
