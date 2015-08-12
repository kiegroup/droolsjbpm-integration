package org.kie.perf.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
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
    private Method getProcessCpuLoad;
    private Timer timer = new Timer();

    private static CPUUsageHistogramSet instance = null;

    private CPUUsageHistogramSet(Class<?> scenario) {
        this.scenario = scenario;

        operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        try {
            getProcessCpuLoad = operatingSystemMXBean.getClass().getMethod("getProcessCpuLoad");
            getProcessCpuLoad.setAccessible(true);
        } catch (Exception e) {

        }
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
            value = (Double) getProcessCpuLoad.invoke(operatingSystemMXBean);
            value *= 100;
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

        metrics.put(MetricRegistry.name(scenario, "cpu.usage"), cpuUsageHistogram);

        return metrics;
    }

}
