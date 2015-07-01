package org.kie.perf.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.ThreadDeadlockDetector;

public class ThreadStatesGaugeSet implements MetricSet {

    // do not compute stack traces.
    private final static int STACK_TRACE_DEPTH = 0;

    private Class<?> scenario;

    private final ThreadMXBean threads;
    private final ThreadDeadlockDetector deadlockDetector;

    public ThreadStatesGaugeSet(Class<?> scenario) {
        this.scenario = scenario;
        this.threads = ManagementFactory.getThreadMXBean();
        this.deadlockDetector = new ThreadDeadlockDetector();
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();

        for (final Thread.State state : Thread.State.values()) {
            gauges.put(MetricRegistry.name(scenario, "thread.state", state.toString().toLowerCase(), "count"), new Gauge<Object>() {
                @Override
                public Object getValue() {
                    return getThreadCount(state);
                }
            });
        }

        gauges.put(MetricRegistry.name(scenario, "threads.count"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return threads.getThreadCount();
            }
        });

        gauges.put(MetricRegistry.name(scenario, "daemon.count"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return threads.getDaemonThreadCount();
            }
        });

        gauges.put(MetricRegistry.name(scenario, "deadlock.count"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return deadlockDetector.getDeadlockedThreads().size();
            }
        });

        gauges.put(MetricRegistry.name(scenario, "deadlocks"), new Gauge<Set<String>>() {
            @Override
            public Set<String> getValue() {
                return deadlockDetector.getDeadlockedThreads();
            }
        });

        return Collections.unmodifiableMap(gauges);
    }

    private int getThreadCount(Thread.State state) {
        final ThreadInfo[] allThreads = getThreadInfo();
        int count = 0;
        for (ThreadInfo info : allThreads) {
            if (info != null && info.getThreadState() == state) {
                count++;
            }
        }
        return count;
    }

    ThreadInfo[] getThreadInfo() {
        return threads.getThreadInfo(threads.getAllThreadIds(), STACK_TRACE_DEPTH);
    }

}
