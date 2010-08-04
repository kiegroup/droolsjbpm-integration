package org.drools.grid.services.configuration;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GridTopologyView implements Serializable{
	
	private String name;
	private Map<String, ExecutionEnvironmentView> executionEnvironments = new HashMap<String, ExecutionEnvironmentView>();
	private Map<String, DirectoryInstanceView> directoryInstances = new HashMap<String, DirectoryInstanceView>();
	private Map<String, TaskServerInstanceView> taskServerInstances = new HashMap<String, TaskServerInstanceView>();

	public GridTopologyView() { }
	
	public GridTopologyView(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<ExecutionEnvironmentView> getExecutionEnvironments() {
		return Collections.unmodifiableCollection(executionEnvironments.values());
	}
	
	public Collection<DirectoryInstanceView> getDirectoryInstances() {
		return Collections.unmodifiableCollection(directoryInstances.values());
	}
	
	public Collection<TaskServerInstanceView> getTaskServers(){
		return Collections.unmodifiableCollection(taskServerInstances.values());
	}

	public void addExecutionEnvironment(ExecutionEnvironmentView newExecutionEnvironment) {
		String newGridResourceName = newExecutionEnvironment.getName();
		checkUniqueName(newGridResourceName, directoryInstances, taskServerInstances);
		executionEnvironments.put(newGridResourceName, newExecutionEnvironment);
	}
	
	public void addDirectoryInstance(DirectoryInstanceView newDirectoryInstance){
		String newGridResourceName = newDirectoryInstance.getName();
		checkUniqueName(newGridResourceName, executionEnvironments, taskServerInstances);
		directoryInstances.put(newGridResourceName, newDirectoryInstance);
	}
	
	public void addTaskServerInstance(TaskServerInstanceView newTaskServerInstance){
		String newGridResourceName = newTaskServerInstance.getName();
		checkUniqueName(newGridResourceName, executionEnvironments, directoryInstances);
		taskServerInstances.put(newGridResourceName, newTaskServerInstance);
	}
	
	public ExecutionEnvironmentView getExecutionEnvironment(String name){
		return executionEnvironments.get(name);
	}
	
	public DirectoryInstanceView getDirectoryInstance(String name){
		return directoryInstances.get(name);
	}

	public TaskServerInstanceView getTaskServerInstance(String name) {
		return taskServerInstances.get(name);
	}
	
	public void removeResource(String name){
		boolean removed = executionEnvironments.remove(name) != null;
		removed |=  directoryInstances.remove(name) != null;
		removed |= taskServerInstances.remove(name) != null;
	}

	private void checkUniqueName(String newGridResourceName, 
			Map<String, ? extends GridResourceView> firstResources, 
			Map<String, ? extends GridResourceView> secondResources) {
		if(null != firstResources.get(newGridResourceName) || null != secondResources.get(newGridResourceName))
			throw new IllegalArgumentException("Existing resource with name: " + newGridResourceName);
	}
}
