package org.jbpm.simulation;

import org.jbpm.simulation.impl.InMemorySimulationRepository;
import org.jbpm.simulation.impl.ht.StaffPoolManagerImpl;

public class SimulationContextFactory {

    public static SimulationContext newContext(SimulationDataProvider dataProvider) {
        SimulationContext context = new SimulationContext();
        SimulationContext.setContext(context);
        
        context.setDataProvider(dataProvider);
        context.setRegistry(new SimulationRegistry());
        context.setRepository(new InMemorySimulationRepository());
        context.setStaffPoolManager(new StaffPoolManagerImpl());
        
        return context;
    }
    
    public static SimulationContext newContext(SimulationDataProvider dataProvider, SimulationRepository repository) {
        SimulationContext context = new SimulationContext();
        SimulationContext.setContext(context);
        
        context.setDataProvider(dataProvider);
        context.setRegistry(new SimulationRegistry());
        context.setRepository(repository);
        context.setStaffPoolManager(new StaffPoolManagerImpl());
        
        return context;
    }
    
    public static SimulationContext newContext(SimulationDataProvider dataProvider, SimulationRepository repository, SimulationRegistry registry) {
        SimulationContext context = new SimulationContext();
        SimulationContext.setContext(context);
        
        context.setDataProvider(dataProvider);
        context.setRegistry(registry);
        context.setRepository(repository);
        context.setStaffPoolManager(new StaffPoolManagerImpl());
        
        return context;
    }
}
