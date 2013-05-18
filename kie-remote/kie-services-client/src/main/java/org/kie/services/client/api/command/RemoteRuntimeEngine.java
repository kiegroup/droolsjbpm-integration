package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;

public class RemoteRuntimeEngine implements RuntimeEngine {

	private String url;
	private String deploymentId;
	private String contextId;
	
	public RemoteRuntimeEngine(String url, String deploymentId, String contextId) {
		this.url = url;
		this.deploymentId = deploymentId;
		this.contextId = contextId;
	}
	
	public KieSession getKieSession() {
		String url = this.url + "/rest/runtime/" + deploymentId + "/execute";
		if (this.contextId != null) {
			url += "?contextId=" + contextId;
		}
		CommandService commandService = new RemoteRestCommandService(url, deploymentId);
		return new CommandBasedStatefulKnowledgeSession(commandService);
	}

	public TaskService getTaskService() {
		String url = this.url + "/rest/task/execute";
		if (this.contextId != null) {
			url += "?contextId=" + contextId;
		}
		CommandExecutor executor = new RemoteTaskCommandExecutor(url, deploymentId);
		return new CommandBasedTaskService(executor);
	}

}
