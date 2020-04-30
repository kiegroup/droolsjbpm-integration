/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.simulation.impl;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.simulation.SimulationEvent;
import org.jbpm.simulation.SimulationInfo;
import org.jbpm.simulation.SimulationRepository;

public class InMemorySimulationRepository implements SimulationRepository {

    protected List<SimulationEvent> events = new ArrayList<SimulationEvent>();
    
    protected SimulationInfo simulationInfo;
    
    public void storeEvent(SimulationEvent event) {
        this.events.add(event);
    }

    public List<SimulationEvent> getEvents() {
        return this.events;
    }
    
    public void close() {
    	this.events.clear();
    }

    public SimulationInfo getSimulationInfo() {
        return this.simulationInfo;
    }

    public void setSimulationInfo(SimulationInfo simInfo) {
        this.simulationInfo = simInfo;
        
    }
}
