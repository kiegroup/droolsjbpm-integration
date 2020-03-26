package org.kie.perf.suite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.kie.perf.Executor;
import org.kie.perf.SharedMetricRegistry;
import org.kie.perf.TestConfig;
import org.kie.perf.TestConfig.Measure;
import org.kie.perf.TestConfig.RunType;
import org.kie.perf.annotation.KPKConstraint;
import org.kie.perf.annotation.KPKLimit;
import org.kie.perf.metrics.CPUUsageHistogramSet;
import org.kie.perf.run.IRunType;
import org.kie.perf.scenario.IPerfTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class ConcurrentLoadSuite implements ITestSuite {

    protected static final Logger log = LoggerFactory.getLogger(LoadSuite.class);

    protected int iterations;
    protected IRunType run;
    
    public ConcurrentLoadSuite() {
        TestConfig tc = TestConfig.getInstance();
        iterations = tc.getIterations();
        run = tc.getRunType().newInstance();
    }
    
    @Override
    public void start() throws Exception {
        TestConfig tc = TestConfig.getInstance();
        Executor exec = Executor.getInstance();
        Set<Class<? extends IPerfTest>> scenarios = exec.getScenarios(tc.getTestPackage());
        if (scenarios.size() == 1) {
            
            IPerfTest scenario = scenarios.iterator().next().newInstance();

            exec.initMetrics(scenario);
            scenario.init();
            if (tc.isWarmUp()) {
                exec.performWarmUp(scenario);
            }

            CPUUsageHistogramSet cpuusage = null;
            boolean cpuusageEnabled = tc.getMeasure().contains(Measure.CPUUSAGE);
            if (cpuusageEnabled) {
                cpuusage = CPUUsageHistogramSet.getInstance(scenario.getClass());
                cpuusage.start();
            }
            startScenario(scenario.getClass());
            if (cpuusageEnabled) {
                cpuusage.stop();
            }

            exec.getReporter().report();
        } else {
            for (Class<? extends IPerfTest> c : scenarios) {
                KPKConstraint constraint = exec.checkScenarioConstraints(c);
                if (constraint != null) {
                    log.info("Scenario '" + tc.getScenario() + "' skipped due to constraints " + Arrays.toString(constraint.value()));
                } else {
                    exec.forkScenario(c.getSimpleName());
                }
            }
        }
    }
    
    private void startScenario(Class<? extends IPerfTest> scenario) {
        List<Thread> threadsList = new ArrayList<Thread>();

        KPKLimit limit = scenario.getAnnotation(KPKLimit.class);
        final int max = (limit != null) ? limit.value() : Integer.MAX_VALUE;

        TestConfig tc = TestConfig.getInstance();

        Timer.Context contextDuration = null;
        if (tc.getRunType() != RunType.DURATION) {
            Timer duration = SharedMetricRegistry.getInstance().timer(MetricRegistry.name(scenario, "scenario.total.duration"));
            contextDuration = duration.time();
        }
        List<IPerfTest> tests = new ArrayList<IPerfTest>();
        int threads = tc.getThreads();
        for (int i = 0; i < threads; ++i) {
            IPerfTest test;
            try {
                test = scenario.newInstance();
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
