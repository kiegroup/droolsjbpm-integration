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

    private double minDuration = Double.MAX_VALUE;
    private double maxDuration = 0.0;
    private double totalDuration = 0.0;

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
