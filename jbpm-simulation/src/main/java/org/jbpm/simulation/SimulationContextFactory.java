/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
