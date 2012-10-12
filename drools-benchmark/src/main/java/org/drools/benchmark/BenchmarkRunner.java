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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static java.lang.System.*;
import static org.drools.benchmark.util.MemoryUtil.aggressiveGC;
import static org.drools.benchmark.util.MemoryUtil.usedMemory;

public class BenchmarkRunner {

    // private static final String CONFIG_FILE = "benchmark.xml";
    private static final String CONFIG_FILE = "benchmark-concurrency.xml";

    private final Executor executor = Executors.newCachedThreadPool(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    public static void main(String[] args) {
        new BenchmarkRunner().run();
    }

    private void run() {
        long start = nanoTime();
        BenchmarkConfig config = new BenchmarkConfig(CONFIG_FILE);
        List<List<BenchmarkResult>> results = new ArrayList<List<BenchmarkResult>>();
        for (int i = 0; i < config.getRepetitions(); i++) {
            results.add(executeAll(config, i));
        }
        printResults(accumulateResults(results));
        out.println("\nDone in " + ((nanoTime() - start) / 1000000) + " msecs");
    }

    private void printResults(List<ResultsAccumulator> results) {
        out.println(ResultsAccumulator.RESULTS_FORMAT);
        for (ResultsAccumulator result : results) out.println(result);
    }

    private List<ResultsAccumulator> accumulateResults(List<List<BenchmarkResult>> results) {
        int benchmarksNr = results.get(0).size();
        List<ResultsAccumulator> accumulatedResults = new ArrayList<ResultsAccumulator>();
        for (int i = 0; i < benchmarksNr; i++) {
            ResultsAccumulator accumulator = new ResultsAccumulator();
            for (List<BenchmarkResult> runResults : results) {
                accumulator.accumulate(runResults.get(i));
            }
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
        aggressiveGC(config.getDelay());
        return results;
    }

    private BenchmarkResult execute(BenchmarkConfig config, BenchmarkDefinition definition, boolean shouldWarmUp) {
        Benchmark benchmark = definition.instance();
        warmUpExecution(config, definition, benchmark, shouldWarmUp);
        aggressiveGC(config.getDelay());

        BenchmarkResult result = new BenchmarkResult(definition);
        result.setUsedMemoryBeforeStart(usedMemory());
        out.println("Executing: " + definition);

        result.setDuration(executeBenchmark(definition, benchmark));
        aggressiveGC(config.getDelay());
        result.setUsedMemoryAfterEnd(usedMemory());
        benchmark = null; // destroy the benchmark in order to allow GC to free the memory allocated by it

        aggressiveGC(config.getDelay());
        result.setUsedMemoryAfterGC(usedMemory());
        return result;
    }

    private void warmUpExecution(BenchmarkConfig config, BenchmarkDefinition definition, Benchmark benchmark, boolean shouldWarmUp) {
        int warmups = shouldWarmUp ? definition.getWarmups() : (definition.isForceWarmup() ? 1 : 0);
        if (warmups < 1) return;
        out.println("Warming up: " + definition.getDescription());
        benchmark.init(definition);
        for (int i = 0; i < warmups; i++) {
            benchmark.execute(0);
        }
        benchmark.terminate();
        aggressiveGC(config.getDelay());
    }

    private long executeBenchmark(BenchmarkDefinition definition, Benchmark benchmark) {
        return definition.isParallel() ?
                executeMultiThreadedBenchmark(definition, benchmark) :
                executeSingleThreadedBenchmark(definition, benchmark);
    }

    private long executeSingleThreadedBenchmark(BenchmarkDefinition definition, Benchmark benchmark) {
        benchmark.init(definition);
        long elapsed = runBenchmark(definition, benchmark);
        benchmark.terminate();
        return elapsed;
    }

    private long executeMultiThreadedBenchmark(final BenchmarkDefinition definition, Benchmark benchmark) {
        final Benchmark[] benchmarks = new Benchmark[definition.getThreadNr()];
        for (int i = 0; i < definition.getThreadNr(); i++) {
            benchmarks[i] = benchmark.clone();
            benchmarks[i].init(definition, i == 0);
        }

        CompletionService<Long> ecs = new ExecutorCompletionService<Long>(executor);
        for (int i = 0; i < definition.getThreadNr(); i++) {
            final Benchmark b = benchmarks[i];
            ecs.submit(new Callable<Long>() {
                public Long call() throws Exception {
                    return runBenchmark(definition, b);
                }
            });
        }

        long result = 0L;
        for (int i = 0; i < definition.getThreadNr(); i++) {
            try {
                result += ecs.take().get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        for (int i = 0; i < definition.getThreadNr(); i++) {
            benchmarks[i].terminate(i == definition.getThreadNr() - 1);
        }

        return result / definition.getThreadNr();
    }

    private long runBenchmark(BenchmarkDefinition definition, Benchmark benchmark) {
        long start = nanoTime();
        for (int i = 0; i < definition.getRepetitions(); i++) {
            benchmark.execute(i);
        }
        return (nanoTime() - start) / 1000000;
    }
}