package org.kie.server.services.prometheus;

import java.util.stream.IntStream;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.commons.lang3.ArrayUtils;

public class PrometheusMetrics {

    private static final long NANOSECONDS_PER_MICROSECOND = 1_000_000;
    private static final long HALF_SECOND_NANO = 500_000_000;
    private static final long NANOSECONDS_PER_SECOND = 1_000_000_000;

    private static long toNano(long second) {
        return second * NANOSECONDS_PER_SECOND;
    }

    private static long toMicro(long second) {
        return second * NANOSECONDS_PER_MICROSECOND;
    }

    private static double[] rangeNano(int start, int end) {
        return IntStream.range(start, end).mapToDouble(l -> toNano((long) l)).toArray();
    }

    private static double[] rangeMicro(int start, int end) {
        return IntStream.range(start, end).mapToDouble(l -> toMicro((long) l)).toArray();
    }

    private static double[] ONE_TO_FIVE = rangeNano(1, 5);

    private static final double[] DECISION_TIME_BUCKETS;

    static {
        DECISION_TIME_BUCKETS = ArrayUtils.addAll(rangeMicro(1, 10), ONE_TO_FIVE);
    }

    private static final Histogram dmnEvaluationTimeHistogram = Histogram.build()
            .name("dmn_evaluate_decision_nanosecond")
            .help("DMN Evaluation Time")
            .labelNames("container_id", "group_id", "artifact_id", "version", "decision_namespace", "decision_name")
            .buckets(DECISION_TIME_BUCKETS)
            .register();

    Histogram getEvaluationTimeHistogram() {
        return dmnEvaluationTimeHistogram;
    }

    private static final Counter dmnNumberOfEvaluationFailed = Counter.build()
            .name("dmn_evaluate_failed_count")
            .help("DMN Evaluation Failed")
            .labelNames("container_id", "group_id", "artifact_id", "version", "decision_namespace", "decision_name")
            .register();

    Counter getDMNNumberOfEvaluationFailed() {
        return dmnNumberOfEvaluationFailed;
    }

    private static final double[] RULE_TIME_BUCKETS;

    static {
        RULE_TIME_BUCKETS = ArrayUtils.addAll(rangeMicro(1, 10), ONE_TO_FIVE);
    }

    private static final Histogram droolsEvaluationTimeHistogram = Histogram.build()
            .name("drl_match_fired_nanosecond")
            .help("Drools Firing Time")
            .labelNames("container_id", "ksessionId", "group_id", "artifact_id", "version", "rule_name")
            .buckets(RULE_TIME_BUCKETS)
            .register();

    Histogram getDroolsEvaluationTimeHistogram() {
        return droolsEvaluationTimeHistogram;
    }
}
