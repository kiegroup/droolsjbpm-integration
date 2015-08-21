package org.kie.perf.suite;

import org.kie.perf.scenario.IPerfTest;

public interface ITestSuite {

    public String getTestPackage();

    public void initScenario(final IPerfTest scenario) throws Exception;

    public void startScenario(final IPerfTest scenario);

}
