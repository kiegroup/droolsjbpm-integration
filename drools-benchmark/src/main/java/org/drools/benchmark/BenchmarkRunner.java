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
        new BenchmarkRunner().run();
    }

    private void run() {
        long start = currentTimeMillis();
        BenchmarkConfig config = new BenchmarkConfig(CONFIG_FILE);
        List<List<BenchmarkResult>> results = new ArrayList<List<BenchmarkResult>>();
        for (int i = 0; i < config.getRepetitions(); i++) results.add(executeAll(config, i));
        printResults(accumulateResults(results));
        System.out.println("\nDone in " + (currentTimeMillis() - start) + " msecs");
    }

    private void printResults(List<ResultsAccumulator> results) {
        System.out.println(ResultsAccumulator.RESULTS_FORMAT);
        for (ResultsAccumulator result : results) System.out.println(result);
    }

    private List<ResultsAccumulator> accumulateResults(List<List<BenchmarkResult>> results) {
        int benchmarksNr = results.get(0).size();
        List<ResultsAccumulator> accumulatedResults = new ArrayList<ResultsAccumulator>();
        for (int i = 0; i < benchmarksNr; i++) {
            ResultsAccumulator accumulator = new ResultsAccumulator();
            for (List<BenchmarkResult> runResults : results) accumulator.accumulate(runResults.get(i));
            accumulatedResults.add(accumulator);
        }
        return accumulatedResults;
    }

    private List<BenchmarkResult> executeAll(BenchmarkConfig config, int execNr) {
        List<BenchmarkResult> results = new ArrayList<BenchmarkResult>();
        for (BenchmarkDefinition benchmarkDef : config) {
            if (benchmarkDef.isEnabled()) {
                BenchmarkResult result = execute(config, benchmarkDef, execNr == 0);
                out.println(result);
                results.add(result);
            }
        }
        afterBenchmarkRun(config);
        return results;
    }

    private BenchmarkResult execute(BenchmarkConfig config, BenchmarkDefinition definition, boolean shouldWarmUp) {
        BenchmarkResult result = new BenchmarkResult(definition);

        if (shouldWarmUp) {
            out.println("Warming up: " + definition.getDescription());
            warmUpExecution(definition);
            afterBenchmarkRun(config);
        }

        Benchmark benchmark = definition.instance();
        result.setUsedMemoryBeforeStart(usedMemory());
        out.println("Executing: " + definition.getDescription());
        benchmark.init(definition);

        result.setDuration(executeBenchmark(definition, benchmark));
        benchmark.terminate();
        result.setUsedMemoryAfterEnd(usedMemory());
        benchmark = null; // destroy the benchmark in order to allow GC to free the memory allocated by it

        afterBenchmarkRun(config);
        result.setUsedMemoryAfterGC(usedMemory());
        return result;
    }

    private void warmUpExecution(BenchmarkDefinition definition) {
        if (definition.getWarmups() < 1) return;
        Benchmark benchmark = definition.instance();
        benchmark.init(definition);
        for (int i = 0; i < definition.getWarmups(); i++) benchmark.execute(0);
        benchmark.terminate();
    }

    private double executeBenchmark(BenchmarkDefinition definition, Benchmark benchmark) {
        long start = nanoTime();
        for (int i = 0; i < definition.getRepetitions(); i++) benchmark.execute(i);
        return ((nanoTime() - start) / 1000) / 1000.0;
    }

    private long usedMemory() {
        Runtime r = Runtime.getRuntime();
        return r.totalMemory() - r.freeMemory();
    }

    private void afterBenchmarkRun(BenchmarkConfig config) {
        long prevUsedMemory = usedMemory();
        for (int i = 0; i < config.getDelay(); i++) {
            long usedMemory = freeMemory();
            if (prevUsedMemory - usedMemory <= 0) break;
            prevUsedMemory = usedMemory;
        }
    }
    
    private long freeMemory() {
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
