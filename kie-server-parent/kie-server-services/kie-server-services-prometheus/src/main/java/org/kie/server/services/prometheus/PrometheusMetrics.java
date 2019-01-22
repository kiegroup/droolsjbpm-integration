package org.kie.server.services.prometheus;

import java.util.stream.IntStream;

import io.prometheus.client.Histogram;
import org.apache.commons.lang3.ArrayUtils;

public class PrometheusMetrics {

    private static final long NANOSECONDS_PER_SECOND = 1_000_000_000;
    private static final double HALF_SECOND_NANO = 500_000_000;

    private static long toNano(long second) {
        return second * NANOSECONDS_PER_SECOND;
    }

    private static double[] rangeNano(int start, int end) {
        return IntStream.range(start, end).mapToDouble(l -> toNano((long) l)).toArray();
    }

    private static double[] ONE_TO_FIVE = rangeNano(1, 5);

    private static final double[] DECISION_TIME_BUCKETS;

    static {
        DECISION_TIME_BUCKETS = ArrayUtils.addAll(new double[]{HALF_SECOND_NANO}, ONE_TO_FIVE);
    }

    private static final Histogram dmnEvaluationTimeHistogram = Histogram.build().name("dmn_evaluate_decision_nanosecond")
            .help("DMN Evaluation Time")
            .labelNames("container_id", "group_id", "artifact_id", "version", "decision_namespace", "decision_name")
            .buckets(DECISION_TIME_BUCKETS)
            .register();

    public Histogram getEvaluationTimeHistogram() {
        return dmnEvaluationTimeHistogram;
    }

    private static final double[] RULE_TIME_BUCKETS;

    static {
        RULE_TIME_BUCKETS = ArrayUtils.addAll(new double[]{HALF_SECOND_NANO}, ONE_TO_FIVE);
    }

    private static final Histogram droolsEvaluationTimeHistogram = Histogram.build()
            .name("drl_match_fired_nanosecond")
            .help("Drools Firing Time")
            .labelNames("rule_name")
            .buckets(RULE_TIME_BUCKETS)
            .register();

    public Histogram getDroolsEvaluationTimeHistogram() {
        return droolsEvaluationTimeHistogram;
    }
}
