package org.kie.server.services.prometheus;

import io.prometheus.client.Histogram;

public class PrometheusDMNMetrics {

    private static final double[] DECISION_TIME_BUCKETS = new double[]{1_000_000, 2_000_000, 3_000_000, 100_000_000, 200_000_000, 300_000_000, 400_000_000, 500_000_000};

    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000;
    public static final long HALF_SECOND_NANO = 500_000_000;

    public static long toNano(long second) {
        return second * NANOSECONDS_PER_SECOND;
    }

    private static final Histogram evaluationTimeHistogram = Histogram.build().name("dmn_evaluate_decision_nanosecond")
            .help("DMN Evaluation Time")
            .labelNames("container_id", "group_id", "artifact_id", "version", "decision_namespace", "decision_name")
            .buckets(DECISION_TIME_BUCKETS)
            .register();


    public Histogram getEvaluationTimeHistogram() {
        return evaluationTimeHistogram;
    }

}
