package org.kie.perf.metrics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.kie.perf.TestConfig;
import org.kie.perf.TestConfig.RunType;
import org.perfrepo.client.PerfRepoClient;
import org.perfrepo.model.Metric;
import org.perfrepo.model.Test;
import org.perfrepo.model.TestExecution;
import org.perfrepo.model.builder.TestExecutionBuilder;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

public class PerfRepoReporter extends ScheduledReporter {

    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static class Builder {
        private final MetricRegistry registry;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public PerfRepoReporter build(PerfRepoClient client) {
            return new PerfRepoReporter(registry, client, rateUnit, durationUnit, filter);
        }
    }

    private final PerfRepoClient client;
    private TestExecution testExecution;

    private PerfRepoReporter(MetricRegistry registry, PerfRepoClient client, TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter) {
        super(registry, "perf-repo-reporter", filter, rateUnit, durationUnit);
        this.client = client;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {

        TestConfig tc = TestConfig.getInstance();

        String testName = tc.getProjectName() + " - " + tc.getSuite() + " - " + tc.getScenario();

        String testExecutionName = testName;
        if (tc.getRunType() == RunType.DURATION) {
            testExecutionName += " - " + tc.getDuration() + " seconds";
        } else {
            testExecutionName += " - " + tc.getIterations() + " iterations";
        }

        String testUid = tc.getProjectName().toLowerCase().replaceAll(" ", "_") + "_" + tc.getSuite().toLowerCase() + "_" + tc.getScenario().toLowerCase();

        TestExecutionBuilder testExecution = TestExecution.builder().testUid(testUid).name(testExecutionName + " - Result").started(new Date());

        if (this.testExecution != null) {
            testExecution.id(this.testExecution.getId());
        }

        for (String tag : tc.getTags()) {
            testExecution.tag(tag);
        }

        for (Entry<Object, Object> e : tc.getProperties().entrySet()) {
            testExecution.parameter(e.getKey().toString(), e.getValue().toString());
        }

        // System properties
        testExecution.parameter("java.vm.name", System.getProperty("java.vm.name"));
        testExecution.parameter("java.version", System.getProperty("java.version"));
        testExecution.parameter("java.runtime.availableProcessors", String.valueOf(Runtime.getRuntime().availableProcessors()));
        try {
            testExecution.parameter("machine.hostname", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e1) {
            // ignore
        }
        testExecution.parameter("os.arch", System.getProperty("os.arch"));
        testExecution.parameter("os.name", System.getProperty("os.name"));

        List<String> metricLabels = new ArrayList<String>();

        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            reportGauge(testExecution, entry.getKey(), entry.getValue());
            metricLabels.add(entry.getKey() + ".value");
        }

        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            reportCounter(testExecution, entry.getKey(), entry.getValue());
            metricLabels.add(entry.getKey() + ".count");
        }

        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            reportHistogram(testExecution, entry.getKey(), entry.getValue());
            metricLabels.add(entry.getKey() + ".count");
            metricLabels.add(entry.getKey() + ".min");
            metricLabels.add(entry.getKey() + ".mean");
            metricLabels.add(entry.getKey() + ".max");
            metricLabels.add(entry.getKey() + ".stddev");
            metricLabels.add(entry.getKey() + ".median");
            metricLabels.add(entry.getKey() + ".p75");
            metricLabels.add(entry.getKey() + ".p95");
            metricLabels.add(entry.getKey() + ".p98");
            metricLabels.add(entry.getKey() + ".p99");
            metricLabels.add(entry.getKey() + ".p999");
        }

        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            reportMeter(testExecution, entry.getKey(), entry.getValue());
            metricLabels.add(entry.getKey() + ".count");
        }

        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            reportTimer(testExecution, entry.getKey(), entry.getValue());
            metricLabels.add(entry.getKey());
            metricLabels.add(entry.getKey() + ".count");
            metricLabels.add(entry.getKey() + ".throughput");
            metricLabels.add(entry.getKey() + ".min");
            metricLabels.add(entry.getKey() + ".mean");
            metricLabels.add(entry.getKey() + ".max");
            metricLabels.add(entry.getKey() + ".stddev");
            metricLabels.add(entry.getKey() + ".median");
            metricLabels.add(entry.getKey() + ".p75");
            metricLabels.add(entry.getKey() + ".p95");
            metricLabels.add(entry.getKey() + ".p98");
            metricLabels.add(entry.getKey() + ".p99");
            metricLabels.add(entry.getKey() + ".p999");
        }

        try {
            Test test = client.getTestByUid(testUid);
            if (test == null) {

                List<Metric> metrics = new ArrayList<Metric>();
                for (String ml : metricLabels) {
                    Metric m = new Metric();
                    m.setName(getMeterName(ml));
                    m.setDescription("TBD");
                    metrics.add(m);
                }

                test = new Test();
                test.setDescription("Automatically created test definition by PerfRepoReporter.");
                test.setUid(testUid);
                test.setGroupId("BxMS"); // TODO: move this to testConfig!!!!!
                test.setName(testName);
                test.setMetrics(metrics);
                client.createTest(test);
            }
            if (this.testExecution == null) {
                this.testExecution = testExecution.build();
                long testExecutionId = client.createTestExecution(this.testExecution);
                this.testExecution.setId(testExecutionId);
            } else {
                this.testExecution = testExecution.build();
                client.addValue(this.testExecution);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportTimer(TestExecutionBuilder testExecution, String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();

        testExecution.value(getMeterName(name) + ".count", (double) timer.getCount());
        testExecution.value(getMeterName(name) + ".throughput", convertRate(timer.getMeanRate()), "unit", "events/" + getRateUnit());
        testExecution.value(getMeterName(name) + ".min", convertDuration(snapshot.getMin()), "unit", getDurationUnit());
        testExecution.value(getMeterName(name) + ".mean", convertDuration(snapshot.getMean()), "unit", getDurationUnit());
        testExecution.value(getMeterName(name) + ".max", convertDuration(snapshot.getMax()), "unit", getDurationUnit());
        testExecution.value(getMeterName(name) + ".stddev", convertDuration(snapshot.getStdDev()));
        testExecution.value(getMeterName(name) + ".median", convertDuration(snapshot.getMedian()));
        testExecution.value(getMeterName(name) + ".p75", convertDuration(snapshot.get75thPercentile()));
        testExecution.value(getMeterName(name) + ".p95", convertDuration(snapshot.get95thPercentile()));
        testExecution.value(getMeterName(name) + ".p98", convertDuration(snapshot.get98thPercentile()));
        testExecution.value(getMeterName(name) + ".p99", convertDuration(snapshot.get99thPercentile()));
        testExecution.value(getMeterName(name) + ".p999", convertDuration(snapshot.get999thPercentile()));

    }

    private void reportMeter(TestExecutionBuilder testExecution, String name, Meter meter) {
        testExecution.value(getMeterName(name) + ".count", (double) meter.getCount());
    }

    private void reportHistogram(TestExecutionBuilder testExecution, String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();

        testExecution.value(getMeterName(name) + ".count", (double) histogram.getCount());
        testExecution.value(getMeterName(name) + ".min", convertDuration(snapshot.getMin()));
        testExecution.value(getMeterName(name) + ".mean", convertDuration(snapshot.getMean()));
        testExecution.value(getMeterName(name) + ".max", convertDuration(snapshot.getMax()));
        testExecution.value(getMeterName(name) + ".stddev", convertDuration(snapshot.getStdDev()));
        testExecution.value(getMeterName(name) + ".median", convertDuration(snapshot.getMedian()));
        testExecution.value(getMeterName(name) + ".p75", convertDuration(snapshot.get75thPercentile()));
        testExecution.value(getMeterName(name) + ".p95", convertDuration(snapshot.get95thPercentile()));
        testExecution.value(getMeterName(name) + ".p98", convertDuration(snapshot.get98thPercentile()));
        testExecution.value(getMeterName(name) + ".p99", convertDuration(snapshot.get99thPercentile()));
        testExecution.value(getMeterName(name) + ".p999", convertDuration(snapshot.get999thPercentile()));
    }

    private void reportCounter(TestExecutionBuilder testExecution, String name, Counter counter) {
        testExecution.value(getMeterName(name) + ".count", (double) counter.getCount());
    }

    private void reportGauge(TestExecutionBuilder testExecution, String name, Gauge gauge) {
        String value = gauge.getValue().toString();
        Double dval = 0.0;
        String filteredValue = "";
        String paramValue = "";
        boolean end = false;
        for (int i = 0; i < value.length(); ++i) {
            String c = String.valueOf(value.charAt(i));
            if (end || !(c.matches("[0-9]") || c.equals("."))) {
                paramValue += c;
                end = true;
            } else {
                filteredValue += c;
            }
        }
        if (!filteredValue.isEmpty()) {
            dval = Double.valueOf(filteredValue);
        }
        paramValue = paramValue.trim();
        if (paramValue.isEmpty()) {
            testExecution.value(getMeterName(name) + ".value", dval);
        } else {
            testExecution.value(getMeterName(name) + ".value", dval, "unit", paramValue);
        }
    }

    protected String getMeterName(String name) {
        String a = name.substring(name.indexOf(".", "org.kie.perf.scenario".length() + 1) + 1);
        return a.substring(a.indexOf(".") + 1);
    }
}
