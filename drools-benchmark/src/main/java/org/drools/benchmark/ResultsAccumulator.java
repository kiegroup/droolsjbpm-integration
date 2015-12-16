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

package org.drools.benchmark;

import java.text.*;

public class ResultsAccumulator {

    public static final String RESULTS_FORMAT = "Benchmark Description;" +
                "Min Duration;Max Duration;Avg Duration;" +
                "Min Used Memory;Max Used Memory;Avg Used Memory";

    private static NumberFormat nf = NumberFormat.getInstance();

    static {
        nf.setGroupingUsed(false);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(3);
        nf.setMaximumIntegerDigits(12);
    }

    private String benchmarkDescription;
    private int resultsCounter = 0;

    private long minDuration = Long.MAX_VALUE;
    private long maxDuration = 0L;
    private long totalDuration = 0L;

    private long minUsedMemory = Long.MAX_VALUE;
    private long maxUsedMemory = 0L;
    private long totalUsedMemory = 0L;

    public void accumulate(BenchmarkResult result) {
        if (benchmarkDescription == null) benchmarkDescription = result.getDescription();
        else if (!benchmarkDescription.equals(result.getDescription())) throw new RuntimeException("Accumulating different benchmarks");

        resultsCounter++;

        minDuration = Math.min(minDuration, result.getDuration());
        maxDuration = Math.max(maxDuration, result.getDuration());
        totalDuration += result.getDuration();

        minUsedMemory = Math.min(minUsedMemory, result.memoryUsedByBenchmark());
        maxUsedMemory = Math.max(maxUsedMemory, result.memoryUsedByBenchmark());
        totalUsedMemory += result.memoryUsedByBenchmark();
    }

    @Override
    public String toString() {
        double averageDuration = resultsCounter < 4 ?
                totalDuration / (double)resultsCounter :
                (totalDuration - minDuration - maxDuration) / (double)(resultsCounter - 2);

        return benchmarkDescription + ";" +
                nf.format(minDuration) + ";" + nf.format(maxDuration) + ";" + nf.format(averageDuration) + ";" +
                nf.format(minUsedMemory) + ";" + nf.format(maxUsedMemory) + ";" + nf.format(totalUsedMemory / resultsCounter);
    }
}
