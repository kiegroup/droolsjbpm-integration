package org.kie.perf.metrics;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.UniformReservoir;

public class CPUUsageHistogramSet implements MetricSet {

    private Class<?> scenario;
    private Histogram cpuUsageHistogram = new Histogram(new UniformReservoir());
    private OperatingSystemMXBean operatingSystemMXBean;
    private Timer timer = new Timer();

    private static CPUUsageHistogramSet instance = null;

    private CPUUsageHistogramSet(Class<?> scenario) {
        this.scenario = scenario;
        operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        }


    public static CPUUsageHistogramSet getInstance(Class<?> scenario) {
        if (instance == null) {
            instance = new CPUUsageHistogramSet(scenario);
        }
        return instance;
    }

    private void update() {
        Double value = null;
        try {
            if (operatingSystemMXBean != null) {
                value = operatingSystemMXBean.getProcessCpuLoad();
                value *= 100;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (value != null) {
            long cpu = Math.round(value);
            cpuUsageHistogram.update(cpu);
        }
    }

    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 100, 200);
    }

    public void stop() {
        timer.cancel();
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> metrics = new HashMap<String, Metric>();

        if (operatingSystemMXBean != null) {
            metrics.put(MetricRegistry.name(scenario, "cpu.usage"), cpuUsageHistogram);
        }

        return metrics;
    }

}
