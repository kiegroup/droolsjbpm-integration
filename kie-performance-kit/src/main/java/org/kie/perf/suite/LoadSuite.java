package org.kie.perf.suite;

import java.util.Arrays;
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

public class LoadSuite implements ITestSuite {
    
    protected static final Logger log = LoggerFactory.getLogger(LoadSuite.class);
    public static final String TEST_PACKAGE = "org.jbpm.test.performance.scenario.load";

    protected int iterations;
    protected IRunType run;
    
    public LoadSuite() {
        TestConfig tc = TestConfig.getInstance();
        iterations = tc.getIterations();
        run = tc.getRunType().newInstance();
    }
    
    @Override
    public void start() throws Exception {
        TestConfig tc = TestConfig.getInstance();
        Executor exec = Executor.getInstance();
        Set<Class<? extends IPerfTest>> scenarios = exec.getScenarios(TEST_PACKAGE);
        if (scenarios.size() == 1) {
            
            IPerfTest scenario = scenarios.iterator().next().newInstance();

            exec.initMetrics(scenario);
            scenario.init();
            if (tc.isWarmUp()) {
                exec.performWarmUp(scenario);
            }
            scenario.initMetrics();

            CPUUsageHistogramSet cpuusage = null;
            boolean cpuusageEnabled = tc.getMeasure().contains(Measure.CPUUSAGE);
            if (cpuusageEnabled) {
                cpuusage = CPUUsageHistogramSet.getInstance(scenario.getClass());
                cpuusage.start();
            }
            startScenario(scenario);
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
    
    private void startScenario(IPerfTest scenario) {
        scenario.init();
        MetricRegistry metrics = SharedMetricRegistry.getInstance();
        TestConfig tc = TestConfig.getInstance();
        IRunType run = tc.getRunType().newInstance();

        Timer.Context contextDuration = null;
        if (tc.getRunType() != RunType.DURATION) {
            Timer duration = metrics.timer(MetricRegistry.name(scenario.getClass(), "scenario.total.duration"));
            contextDuration = duration.time();
        }

        KPKLimit limit = scenario.getClass().getAnnotation(KPKLimit.class);
        int max = Integer.MAX_VALUE;
        if (limit != null) {
            max = limit.value();
        }

        Timer scenarioDuration = metrics.timer(MetricRegistry.name(scenario.getClass(), "scenario.single.duration"));
        run.start(max);
        while (!run.isEnd()) {
            Timer.Context context = scenarioDuration.time();
            try {
                scenario.execute();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            context.stop();
        }
        if (contextDuration != null) {
            contextDuration.stop();
        }
        scenario.close();
    }

}
