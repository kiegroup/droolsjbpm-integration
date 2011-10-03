package org.drools.benchmark.util;

import static java.lang.System.gc;
import static java.lang.System.runFinalization;

public class MemoryUtil {

    public static long usedMemory() {
        Runtime r = Runtime.getRuntime();
        return r.totalMemory() - r.freeMemory();
    }

    public static void aggressiveGC(int maxAttempts) {
        long prevUsedMemory = usedMemory();
        for (int i = 0; i < maxAttempts; i++) {
            long usedMemory = freeMemory();
            if (prevUsedMemory - usedMemory <= 0) break;
            prevUsedMemory = usedMemory;
        }
    }

    private static long freeMemory() {
        runFinalization();
        gc();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            // Ignore
        }
        return usedMemory();
    }
}
