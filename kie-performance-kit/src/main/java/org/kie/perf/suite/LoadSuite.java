package org.kie.perf.suite;

import org.kie.perf.SharedMetricRegistry;
import org.kie.perf.TestConfig;
import org.kie.perf.annotation.KPKLimit;
import org.kie.perf.run.IRunType;
import org.kie.perf.scenario.IPerfTest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class LoadSuite implements ITestSuite {

    protected int iterations;

    @Override
    public String getTestPackage() {
        return "org.kie.perf.scenario.load";
    }

    @Override
    public void initScenario(final IPerfTest scenario) throws Exception {
        TestConfig tc = TestConfig.getInstance();
        iterations = tc.getIterations();

        scenario.init();
    }

    @Override
    public void startScenario(final IPerfTest scenario) {
        MetricRegistry metrics = SharedMetricRegistry.getInstance();
        IRunType run = TestConfig.getInstance().getRunType().newInstance();

        Timer duration = metrics.timer(MetricRegistry.name(scenario.getClass(), "scenario.total.duration"));
        Timer.Context contextDuration = duration.time();

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
        contextDuration.stop();
        scenario.close();
    }

}
