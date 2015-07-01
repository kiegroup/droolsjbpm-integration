package org.kie.perf.suite;

import java.util.ArrayList;
import java.util.List;

import org.kie.perf.SharedMetricRegistry;
import org.kie.perf.TestConfig;
import org.kie.perf.annotation.KPKLimit;
import org.kie.perf.run.IRunType;
import org.kie.perf.scenario.IPerfTest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class ConcurrentLoadSuite implements ITestSuite {

    protected int iterations = 1; // default
    protected int threads = 1; // default

    @Override
    public String getTestPackage() {
        return "org.kie.perf.scenario.load";
    }

    @Override
    public void initScenario(final IPerfTest scenario) throws Exception {
        TestConfig tc = TestConfig.getInstance();
        iterations = tc.getIterations();
        threads = tc.getThreads();

        scenario.init();
    }

    @Override
    public void startScenario(final IPerfTest scenario) {
        List<Thread> threadsList = new ArrayList<Thread>();

        KPKLimit limit = scenario.getClass().getAnnotation(KPKLimit.class);
        final int max = (limit != null) ? limit.value() : Integer.MAX_VALUE;

        Timer duration = SharedMetricRegistry.getInstance().timer(MetricRegistry.name(scenario.getClass(), "scenario.total.duration"));
        Timer.Context context = duration.time();
        for (int i = 0; i < threads; ++i) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    Timer duration = SharedMetricRegistry.getInstance().timer(MetricRegistry.name(scenario.getClass(), "scenario.single.duration"));
                    IRunType run = TestConfig.getInstance().getRunType().newInstance();

                    run.start(max);
                    while (!run.isEnd()) {
                        Timer.Context context = duration.time();
                        scenario.execute();
                        context.stop();
                    }
                }
            });
            threadsList.add(t);
        }

        for (Thread t : threadsList) {
            t.start();
        }

        for (Thread t : threadsList) {
            try {
                t.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        context.stop();

        scenario.close();
        threadsList.clear();
    }

}
