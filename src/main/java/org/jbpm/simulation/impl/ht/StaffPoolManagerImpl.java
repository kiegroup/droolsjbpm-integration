package org.jbpm.simulation.impl.ht;

import java.util.HashMap;
import java.util.Map;

import org.drools.definition.process.Node;


public class StaffPoolManagerImpl implements StaffPoolManager {
	
	private Map<String, StaffPool> registeredPools = new HashMap<String, StaffPool>();
	
	public StaffPoolManagerImpl() {
		
	}
	
	public void registerPool(String processId, Node element, long simulationDuration) {

	    if (!registeredPools.containsKey(element.getName())) {
    		StaffPool pool = new StaffPoolImpl(processId, element, simulationDuration);
    		registeredPools.put(element.getName(), pool);
	    }
	}
	
	public StaffPool getActivityPool(String activityName) {
		return registeredPools.get(activityName);
	}

}
