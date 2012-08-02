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
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.simulation.util.SimulationUtils;

public class StaffPoolImpl implements StaffPool {
    
    private Map<String, Object> properties;

	private String distibutionType;
	private int poolSize;
	private long duration;
	private List<Long> allocatedTill = new ArrayList<Long>();
	private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	//default working hours set to eight hours
	private long workingHours = ( 8 * 60 * 60 * 1000);
	private long poolCapacity = 0;
	
	private long performedWork = 0;
	
	private RandomData randomizer = new RandomDataImpl();
	
	// optional properties that depends on selected distribution
	private double range;
	private double standadDeviation;
	
	private double resourceCost;
	
	private TimeUnit elementTimeUnit;


	public StaffPoolImpl(String processId, Node element, double simulationDuration) {
	    
	    SimulationDataProvider provider = SimulationContext.getContext().getDataProvider();
	    
	    properties = provider.getSimulationDataForNode(processId, element);
		
		this.elementTimeUnit = SimulationUtils.getTimeUnit(properties);
		this.distibutionType = (String) properties.get(SimulationConstants.DISTRIBUTION_TYPE);
		this.poolSize = SimulationUtils.asInt(properties.get(SimulationConstants.STAFF_AVAILABILITY));
		this.duration = timeUnit.convert(SimulationUtils.asLong(properties.get(SimulationConstants.DURATION)), this.elementTimeUnit);
		
		long workingHoursOpt = SimulationUtils.asLong(properties.get(SimulationConstants.WORKING_HOURS));
		if (workingHoursOpt > 0) {
			this.workingHours = timeUnit.convert(workingHoursOpt, TimeUnit.HOURS);
		}
		this.poolCapacity = poolSize * this.workingHours;
		
		// if simulation is estimated to more than one day multiply working hours by that factor
		if (simulationDuration > 1) {
			this.poolCapacity = (long) (this.poolCapacity * simulationDuration);
		}
		
		this.range = timeUnit.convert(SimulationUtils.asLong(properties.get(SimulationConstants.RANGE)), this.elementTimeUnit);
		this.standadDeviation = timeUnit.convert(SimulationUtils.asLong(properties.get(SimulationConstants.STANDARD_DEVIATION)), this.elementTimeUnit);
		
		this.resourceCost = SimulationUtils.asDouble(properties.get(SimulationConstants.COST_PER_TIME_UNIT));
		
		
	}
	
	
	protected long allocate(long startTime, String distribution, long duration, double range, double sdv) {
		long waitTime = 0;
		// calculate duration based on distribution type
		long instanceDuration = calculateDuration(distribution, duration, range, sdv);
		performedWork += instanceDuration;
		if(allocatedTill.size() < poolSize) {
		
			allocatedTill.add(startTime + instanceDuration);
		
			return waitTime;
		 } else {
			 Collections.sort(allocatedTill);
		
			 long allocated = allocatedTill.get(0);
			 if (allocated >= startTime) {
				 waitTime = allocated - startTime;
				 allocated += instanceDuration;
		
			 } else {
				 allocated = startTime + instanceDuration;
			 }
			 allocatedTill.set(0, allocated);
		
			 return waitTime;
		}
	}
	

	public long allocate(long startTime) {
		
		return allocate(startTime, this.distibutionType, this.duration, this.range, this.standadDeviation);
	}
	
	public long allocate(long startTime, Node element) {

		String distibutionType = (String) properties.get(SimulationConstants.DISTRIBUTION_TYPE);
		long duration = timeUnit.convert(SimulationUtils.asLong(properties.get(SimulationConstants.DURATION)), this.elementTimeUnit);
		
		double range = timeUnit.convert(SimulationUtils.asLong(properties.get(SimulationConstants.RANGE)), this.elementTimeUnit);
		double standadDeviation = timeUnit.convert(SimulationUtils.asLong(properties.get(SimulationConstants.STANDARD_DEVIATION)), this.elementTimeUnit);
		
		return allocate(startTime, distibutionType, duration, range, standadDeviation);
	}
	
	public double getResourceUtilization() {
		return performedWork * 100 / poolCapacity;
	}
	
	protected long calculateDuration(String distibutionType, long duration, double range, double standardDeviation) {
		if ("uniform".equalsIgnoreCase(distibutionType)) {
			return (long) randomizer.nextUniform((duration-range), (duration+range));
		} else if ("normal".equalsIgnoreCase(distibutionType)) {
			return (long) randomizer.nextGaussian(duration, standardDeviation);
		} else {
			return duration;
		}
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
