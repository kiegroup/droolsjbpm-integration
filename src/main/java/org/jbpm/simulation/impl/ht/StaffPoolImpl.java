package org.jbpm.simulation.impl.ht;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.drools.definition.process.Node;
import org.jbpm.simulation.SimulationContext;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.TimeGenerator;
import org.jbpm.simulation.TimeGeneratorFactory;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class StaffPoolImpl implements StaffPool {
    
    private Map<String, Object> properties;

	private int poolSize;
	private long duration;
	private List<Long> allocatedTill = new ArrayList<Long>();
	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	//default working hours set to eight hours
	private long workingHours = ( 8 * 60 * 60 * 1000);
	private long poolCapacity = 0;
	
	private long performedWork = 0;
	
	private RandomData randomizer = new RandomDataImpl();
	
	private double resourceCost;
	
	private TimeUnit elementTimeUnit;
	
	private TimeGenerator timeGenerator;


	public StaffPoolImpl(String processId, Node element, double simulationDuration) {
	    
	    SimulationDataProvider provider = SimulationContext.getContext().getDataProvider();
	    
	    properties = provider.getSimulationDataForNode(element);
	    
	    timeGenerator=TimeGeneratorFactory.newTimeGenerator(properties);
		
		this.elementTimeUnit = SimulationUtils.getTimeUnit(properties);
		this.poolSize = (int)SimulationUtils.asDouble(properties.get(SimulationConstants.STAFF_AVAILABILITY));
		
		this.duration = timeGenerator.generateTime();
		
		long workingHoursOpt = (long)SimulationUtils.asDouble(properties.get(SimulationConstants.WORKING_HOURS));
		if (workingHoursOpt > 0) {
			this.workingHours = timeUnit.convert(workingHoursOpt, TimeUnit.HOURS);
		}
		this.poolCapacity = poolSize * this.workingHours;
		
		// if simulation is estimated to more than one day multiply working hours by that factor
		if (simulationDuration > 1) {
			this.poolCapacity = (long) (this.poolCapacity * simulationDuration);
		}
		
		this.resourceCost = SimulationUtils.asDouble(properties.get(SimulationConstants.COST_PER_TIME_UNIT));
		
		
	}
	
	
	protected long allocate(long startTime, long duration) {
		long waitTime = 0;
		performedWork += duration;
		if(allocatedTill.size() < poolSize) {
		
			allocatedTill.add(startTime + duration);
		
			return waitTime;
		 } else {
			 Collections.sort(allocatedTill);
		
			 long allocated = allocatedTill.get(0);
			 if (allocated >= startTime) {
				 waitTime = allocated - startTime;
				 allocated += duration;
		
			 } else {
				 allocated = startTime + duration;
			 }
			 allocatedTill.set(0, allocated);
		
			 return waitTime;
		}
	}
	

	public long allocate(long startTime) {
		
		return allocate(startTime, this.duration);
	}
	
	public long allocate(long startTime, Node element) {

		long duration = this.duration = timeGenerator.generateTime();
		
		return allocate(startTime, duration);
	}
	
	public double getResourceUtilization() {
		return performedWork * 100 / poolCapacity;
	}

	/* (non-Javadoc)
	 * @see org.onebpm.simulation.engine.api.StaffPool#getResourceCost()
	 */
	public double getResourceCost() {
		
		return this.resourceCost;
	}
	
	
	public TimeUnit getElementTimeUnit() {
		return elementTimeUnit;
	}
}
