package org.kie.perf.suite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.kie.perf.Executor;
import org.kie.perf.SharedMetricRegistry;
import org.kie.perf.TestConfig;
import org.kie.perf.TestConfig.Measure;
import org.kie.perf.metrics.CPUUsageHistogramSet;
import org.kie.perf.metrics.PerfRepoReporter;
import org.kie.perf.run.IRunType;
import org.kie.perf.scenario.IPerfTest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;

public class SoakSuite implements ITestSuite {
    
    public static final String TEST_PACKAGE = "org.jbpm.test.performance.scenario.soak";

    protected int iterations;
    protected int expectedRate;
    protected IRunType run;

    public SoakSuite() {
        TestConfig tc = TestConfig.getInstance();
        iterations = tc.getIterations();
        expectedRate = tc.getExpectedRate();
        run = tc.getRunType().newInstance();
    }

    public void start() throws Exception {
        TestConfig tc = TestConfig.getInstance();
        Executor exec = Executor.getInstance();
        Set<Class<? extends IPerfTest>> scenarios = exec.getScenarios(TEST_PACKAGE);
        List<IPerfTest> scenarioInstances = new ArrayList<IPerfTest>();
        for (Class<? extends IPerfTest> c : scenarios) {
            IPerfTest scenario = c.newInstance();
            scenario.init();
            scenarioInstances.add(scenario);
        }
        boolean cpuusageEnabled = tc.getMeasure().contains(Measure.CPUUSAGE);
        CPUUsageHistogramSet cpuusage = null;
        if (cpuusageEnabled) {
            cpuusage = CPUUsageHistogramSet.getInstance(SoakSuite.class);
            cpuusage.start();
        }
        long timeForOneRun = Math.round(60.0 / expectedRate) * 1000; // expected rate is in runs per minute
        
        ScheduledReporter reporter = exec.getReporter();
        if (reporter instanceof PerfRepoReporter) {
            ((PerfRepoReporter) reporter).setScheduled(true);
        }
        reporter.start(tc.getPeriodicity(), TimeUnit.SECONDS);
        
        run.start(Integer.MAX_VALUE);
        while (!run.isEnd()) {
            long startTime = System.currentTimeMillis();
            for (IPerfTest scenario : scenarioInstances) {
                startScenario(scenario);
            }
            long endTime = System.currentTimeMillis();
            long pause = timeForOneRun - (endTime - startTime);
            if (pause > 0) {
                Thread.sleep(pause);
            }
        }
        reporter.stop();
        if (cpuusageEnabled) {
            cpuusage.stop();
        }
        for (IPerfTest scenario : scenarioInstances) {
            scenario.close();
        }
    }
    
    private void startScenario(IPerfTest scenario) {
        MetricRegistry metrics = SharedMetricRegistry.getInstance();

        Timer scenarioDuration = metrics.timer(MetricRegistry.name(scenario.getClass(), "scenario.single.duration"));
        scenario.init();
        scenario.initMetrics();
        Timer.Context context = scenarioDuration.time();
        try {
            scenario.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        context.stop();
    }

}
