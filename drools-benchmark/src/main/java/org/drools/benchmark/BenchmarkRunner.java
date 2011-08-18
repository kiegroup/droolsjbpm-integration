/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.benchmark;

import java.util.*;

import static java.lang.System.*;

public class BenchmarkRunner {

    private static final String CONFIG_FILE = "benchmark.xml";

    public static void main(String[] args) {
        new BenchmarkRunner().execute();
    }

    private List<BenchmarkResult> execute() {
        BenchmarkConfig config = new BenchmarkConfig(CONFIG_FILE);
        List<BenchmarkResult> results = new ArrayList<BenchmarkResult>();
        for (BenchmarkDefinition benchmarkDef : config) {
            if (benchmarkDef.isEnabled()) {
                BenchmarkResult result = execute(config, benchmarkDef);
                out.println(result);
                results.add(result);
            }
        }
        return results;
    }

    private BenchmarkResult execute(BenchmarkConfig config, BenchmarkDefinition definition) {
        BenchmarkResult result = new BenchmarkResult(definition);
        result.setUsedMemoryBeforeStart(usedMemory(true));

        Benchmark benchmark = definition.instance();
        out.println("Executing: " + definition.getDescription());
        benchmark.init(definition);

        result.setDuration(executeBenchmark(definition, benchmark));
        benchmark.terminate();
        result.setUsedMemoryAfterEnd(usedMemory(false));
        benchmark = null; // destroy the benchmark in order to allow GC to free the memory allocated by it

        afterBenchmarkRun(config);
        result.setUsedMemoryAfterGC(usedMemory(true));
        return result;
    }

    private long executeBenchmark(BenchmarkDefinition definition, Benchmark benchmark) {
        long start = currentTimeMillis();
        for (int i = 0; i < definition.getRepetitions(); i++) benchmark.execute(i);
        return currentTimeMillis() - start;
    }

    private long usedMemory(boolean runGC) {
        Runtime r = Runtime.getRuntime();
        if (runGC) r.gc();
        return r.totalMemory() - r.freeMemory();
    }

    private void afterBenchmarkRun(BenchmarkConfig config) {
        Runtime.getRuntime().gc();
        try {
            Thread.sleep(config.getDelay() * 1000L);
        } catch (InterruptedException e) {
            // Ignore
        }
        Runtime.getRuntime().gc();
    }
}
