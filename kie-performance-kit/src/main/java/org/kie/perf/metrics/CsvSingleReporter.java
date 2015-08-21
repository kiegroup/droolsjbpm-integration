package org.kie.perf.metrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

public class CsvSingleReporter extends ScheduledReporter {
    /**
     * Returns a new {@link Builder} for {@link CsvReporter}.
     * 
     * @param registry
     *            the registry to report
     * @return a {@link Builder} instance for a {@link CsvReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link CsvReporter} instances. Defaults to using the
     * default locale, converting rates to events/second, converting durations
     * to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Locale locale;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private Clock clock;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.locale = Locale.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.clock = Clock.defaultClock();
            this.filter = MetricFilter.ALL;
        }

        /**
         * Format numbers for the given {@link Locale}.
         * 
         * @param locale
         *            a {@link Locale}
         * @return {@code this}
         */
        public Builder formatFor(Locale locale) {
            this.locale = locale;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         * 
         * @param rateUnit
         *            a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         * 
         * @param durationUnit
         *            a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Use the given {@link Clock} instance for the time.
         * 
         * @param clock
         *            a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         * 
         * @param filter
         *            a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link CsvReporter} with the given properties, writing
         * {@code .csv} files to the given directory.
         * 
         * @param directory
         *            the directory in which the {@code .csv} files will be
         *            created
         * @return a {@link CsvReporter}
         */
        public CsvSingleReporter build(File directory) {
            return new CsvSingleReporter(registry, directory, locale, rateUnit, durationUnit, clock, filter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReporter.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final File directory;
    private final Locale locale;
    private final Clock clock;

    private CsvSingleReporter(MetricRegistry registry, File directory, Locale locale, TimeUnit rateUnit, TimeUnit durationUnit, Clock clock,
            MetricFilter filter) {
        super(registry, "csv-reporter", filter, rateUnit, durationUnit);
        this.directory = directory;
        this.locale = locale;
        this.clock = clock;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {

        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            reportGauge(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            reportCounter(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            reportHistogram(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            reportMeter(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Timer> entry : timers.entrySet()) {
            reportTimer(entry.getKey(), entry.getValue());
        }
    }

    private void reportTimer(String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();

        report(name, getMeterName(name), timer.getCount(), convertRate(timer.getMeanRate()), convertDuration(snapshot.getMin()),
                convertDuration(snapshot.getMean()), convertDuration(snapshot.getMax()), convertDuration(snapshot.getStdDev()),
                convertDuration(snapshot.getMedian()), convertDuration(snapshot.get75thPercentile()), convertDuration(snapshot.get95thPercentile()),
                convertDuration(snapshot.get98thPercentile()), convertDuration(snapshot.get99thPercentile()),
                convertDuration(snapshot.get999thPercentile()));
    }

    private void reportMeter(String name, Meter meter) {
        report(name, getMeterName(name), meter.getCount(), 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    private void reportHistogram(String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();

        report(name, getMeterName(name), histogram.getCount(), 0.0f, (double) snapshot.getMin(), (double) snapshot.getMean(),
                (double) snapshot.getMax(), (double) snapshot.getStdDev(), (double) snapshot.getMedian(), (double) snapshot.get75thPercentile(),
                (double) snapshot.get95thPercentile(), (double) snapshot.get98thPercentile(), (double) snapshot.get99thPercentile(),
                (double) snapshot.get999thPercentile());
    }

    private void reportCounter(String name, Counter counter) {
        report(name, getMeterName(name), counter.getCount(), 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    private void reportGauge(String name, Gauge gauge) {
        report(name, getMeterName(name), gauge.getValue(), 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    private void report(String name, Object... values) {
        try {
            final File file = new File(directory, getFileName(name) + ".csv");
            final boolean fileAlreadyExists = file.exists();
            if (fileAlreadyExists || file.createNewFile()) {
                final PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file, true), UTF_8));
                try {
                    if (!fileAlreadyExists) {
                        out.println("Metric,Count/Value,Mean Rate [events/" + getRateUnit() + "],Min [" + getDurationUnit() + "],Mean ["
                                + getDurationUnit() + "],Max [" + getDurationUnit() + "],Standard Deviation,Median,p75,p95,p98,p99,p99.9");
                    }
                    out.printf(locale, String.format(locale, "%s%n", "%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f"), values);
                } finally {
                    out.close();
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Error writing to {}", name, e);
        }
    }

    protected String getFileName(String name) {
        String a = name.substring(name.indexOf(".", "org.kie.perf.scenario".length() + 1) + 1);
        return a.substring(0, a.indexOf("."));
    }

    protected String getMeterName(String name) {
        String a = name.substring(name.indexOf(".", "org.kie.perf.scenario".length() + 1) + 1);
        return a.substring(a.indexOf(".") + 1);
    }
}
