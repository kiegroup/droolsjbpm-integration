package org.kie.perf.scenario;

public interface IPerfTest {

    /**
     * Shouldn't contain MetricRegistry metrics = SharedMetricRegistry.getInstance();
     * For initialization of metrics use initMetrics.
     */
    public void init();
    
    public void initMetrics();
    
    public void execute();
    
    public void close();
    
}
