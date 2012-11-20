package org.jbpm.simulation.impl.ht;

import java.util.concurrent.TimeUnit;

import org.kie.definition.process.Node;


public interface StaffPool {

	
	public long allocate(long startTime);
	
	public long allocate(long startTime, Node element);
	
	public double getResourceUtilization();
	
	public double getResourceCost();
	
	TimeUnit getElementTimeUnit();
}
