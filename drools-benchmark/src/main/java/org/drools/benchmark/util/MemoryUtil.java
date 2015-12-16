/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
