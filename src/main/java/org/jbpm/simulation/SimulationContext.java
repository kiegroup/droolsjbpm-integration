package org.jbpm.simulation;

import java.util.List;

public class SimulationContext {

    protected static InheritableThreadLocal<SimulationContext> simulationContextThreadLocal = new InheritableThreadLocal<SimulationContext>();

    private SimulationRegistry registry;
    private SimulationRepository repository;
    private SimulationDataProvider dataProvider;
    private List<String> currentPath;
    private long startTime;
    private long currentTime;
    
    public static SimulationContext getContext() {
        return simulationContextThreadLocal.get();
    }

    public static void setContext(SimulationContext context) {
        simulationContextThreadLocal.set(context);
    }
    
    public SimulationRepository getRepository() {
        return repository;
    }
    
    public SimulationRegistry getRegistry() {
        return registry;
    }
    
    protected void setRepository(SimulationRepository repository) {
        this.repository = repository;
    }
    
    protected void setRegistry(SimulationRegistry registry) {
        this.registry = registry;
    }

    public List<String> getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(List<String> currentPath) {
        this.currentPath = currentPath;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public SimulationDataProvider getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(SimulationDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
}
