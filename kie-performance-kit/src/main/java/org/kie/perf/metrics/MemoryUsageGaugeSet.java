package org.kie.perf.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;

public class MemoryUsageGaugeSet implements MetricSet {

    private Class<?> scenario;

    private final MemoryMXBean mxBean;
    private final List<MemoryPoolMXBean> memoryPools;

    public MemoryUsageGaugeSet(Class<?> scenario) {
        this.scenario = scenario;
        this.mxBean = ManagementFactory.getMemoryMXBean();
        this.memoryPools = new ArrayList<MemoryPoolMXBean>(ManagementFactory.getMemoryPoolMXBeans());
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();

        gauges.put(MetricRegistry.name(scenario, "heap.init"), new Gauge<String>() {
            @Override
            public String getValue() {
                return readableFileSize(mxBean.getHeapMemoryUsage().getInit());
            }
        });

        gauges.put(MetricRegistry.name(scenario, "heap.used"), new Gauge<String>() {
            @Override
            public String getValue() {
                return readableFileSize(mxBean.getHeapMemoryUsage().getUsed());
            }
        });

        gauges.put(MetricRegistry.name(scenario, "heap.max"), new Gauge<String>() {
            @Override
            public String getValue() {
                return readableFileSize(mxBean.getHeapMemoryUsage().getMax());
            }
        });

        gauges.put(MetricRegistry.name(scenario, "heap.committed"), new Gauge<String>() {
            @Override
            public String getValue() {
                return readableFileSize(mxBean.getHeapMemoryUsage().getCommitted());
            }
        });

        gauges.put(MetricRegistry.name(scenario, "heap.usage"), new Gauge<String>() {
            @Override
            public String getValue() {
                final MemoryUsage usage = mxBean.getHeapMemoryUsage();
                NumberFormat percentFormat = NumberFormat.getPercentInstance();
                percentFormat.setMaximumFractionDigits(2);
                return percentFormat.format(((double) usage.getUsed()) / usage.getMax());
            }
        });

        for (final MemoryPoolMXBean pool : memoryPools) {
            gauges.put(name(scenario, pool.getName().toLowerCase().replace(" ", "."), "usage"), new Gauge<String>() {
                @Override
                public String getValue() {
                    final long max = pool.getUsage().getMax() == -1 ? pool.getUsage().getCommitted() : pool.getUsage().getMax();
                    NumberFormat percentFormat = NumberFormat.getPercentInstance();
                    percentFormat.setMaximumFractionDigits(2);
                    return percentFormat.format(((double) pool.getUsage().getUsed()) / max);
                }
            });
            gauges.put(name(scenario, pool.getName().toLowerCase().replace(" ", "."), "used"), new Gauge<String>() {
                @Override
                public String getValue() {
                    return readableFileSize(pool.getUsage().getUsed());
                }
            });
        }

        return Collections.unmodifiableMap(gauges);
    }

    private static String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

}
