package org.kie.perf.suite;

import java.util.ArrayList;
import java.util.List;

import org.kie.perf.SharedMetricRegistry;
import org.kie.perf.TestConfig;
import org.kie.perf.TestConfig.RunType;
import org.kie.perf.annotation.KPKLimit;
import org.kie.perf.run.IRunType;
import org.kie.perf.scenario.IPerfTest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class ConcurrentLoadSuite implements ITestSuite {

    @Override
    public String getTestPackage() {
        return "org.kie.perf.scenario.load";
    }

    @Override
    public void initScenario(final IPerfTest scenario) throws Exception {
        scenario.init();
    }

    @Override
    public void startScenario(final IPerfTest scenario) {
        List<Thread> threadsList = new ArrayList<Thread>();

        KPKLimit limit = scenario.getClass().getAnnotation(KPKLimit.class);
        final int max = (limit != null) ? limit.value() : Integer.MAX_VALUE;

        TestConfig tc = TestConfig.getInstance();

        Timer.Context contextDuration = null;
        if (tc.getRunType() != RunType.DURATION) {
            Timer duration = SharedMetricRegistry.getInstance().timer(MetricRegistry.name(scenario.getClass(), "scenario.total.duration"));
            contextDuration = duration.time();
        }
        List<IPerfTest> tests = new ArrayList<IPerfTest>();
        int threads = tc.getThreads();
        for (int i = 0; i < threads; ++i) {
            IPerfTest test;
            try {
                test = scenario.getClass().newInstance();
                test.init();
                test.initMetrics();
                tests.add(test);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (IPerfTest test : tests) {
            Thread t = new ThreadScenario(test, max);
            threadsList.add(t);
        }

        for (Thread t : threadsList) {
            t.start();
        }

        for (Thread t : threadsList) {
            try {
                if (t.isAlive()) {
                    t.join();
                }
            } catch (Exception ex) {
                
            }
        }
        if (contextDuration != null) {
            contextDuration.stop();
        }

        for (IPerfTest t : tests) {
            t.close();
        }
        scenario.close();
        threadsList.clear();
    }

    private static class ThreadScenario extends Thread {

        private IPerfTest scenario;
        private int max;
        
        public ThreadScenario(IPerfTest scenario, int max) {
            this.scenario = scenario;
            this.max = max;
        }
        
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
    }

}
