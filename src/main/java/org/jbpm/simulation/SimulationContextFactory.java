package org.jbpm.simulation;

import org.jbpm.simulation.impl.InMemorySimulationRepository;

public class SimulationContextFactory {

    public static SimulationContext newContext(SimulationDataProvider dataProvider) {
        SimulationContext context = new SimulationContext();
        SimulationContext.setContext(context);
        
        context.setDataProvider(dataProvider);
        context.setRegistry(new SimulationRegistry());
        context.setRepository(new InMemorySimulationRepository());
        
        return context;
    }
    
    public static SimulationContext newContext(SimulationDataProvider dataProvider, SimulationRepository repository) {
        SimulationContext context = new SimulationContext();
        SimulationContext.setContext(context);
        
        context.setDataProvider(dataProvider);
        context.setRegistry(new SimulationRegistry());
        context.setRepository(repository);
        
        return context;
    }
    
    public static SimulationContext newContext(SimulationDataProvider dataProvider, SimulationRepository repository, SimulationRegistry registry) {
        SimulationContext context = new SimulationContext();
        SimulationContext.setContext(context);
        
        context.setDataProvider(dataProvider);
        context.setRegistry(registry);
        context.setRepository(repository);
        
        return context;
    }
}
