package org.kie.perf;

import com.codahale.metrics.MetricRegistry;

public class SharedMetricRegistry {

    private static MetricRegistry instance;
    private static boolean isWarmUp = false;
    private static MetricRegistry warmUpInstance;
    
    private SharedMetricRegistry() {
        
    }
    
    public static void setWarmUp(boolean isWarmUp) {
        SharedMetricRegistry.isWarmUp = isWarmUp;
    }

    public static MetricRegistry getInstance() {
        if (isWarmUp) {
            if (warmUpInstance == null) {
                warmUpInstance = new MetricRegistry();
            }
            return warmUpInstance;
        }
        if (instance == null) {
            instance = new MetricRegistry();
        }
        return instance;
    }

}
