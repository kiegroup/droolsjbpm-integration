package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.services.client.api.RemoteRestSessionFactory.AuthenticationType;

public class RemoteRuntimeEngine implements RuntimeEngine {

	private String url;
	private String deploymentId;
	private AuthenticationType authenticationType;
	private String username;
	private String password;
	private String contextId;
	
	public RemoteRuntimeEngine(String url, String deploymentId, AuthenticationType authenticationType, String username, String password, String contextId) {
		this.url = url;
		this.deploymentId = deploymentId;
		this.authenticationType = authenticationType;
		this.username = username;
		this.password = password;
		this.contextId = contextId;
	}
	
	public KieSession getKieSession() {
		String url = this.url + "/runtime/" + deploymentId + "/execute";
		if (this.contextId != null) {
			url += "?contextId=" + contextId;
		}
		CommandService commandService = new RemoteSessionCommandService(this.url, url, deploymentId, authenticationType, username, password);
		return new CommandBasedStatefulKnowledgeSession(commandService);
	}

	public TaskService getTaskService() {
		String url = this.url + "/task/execute";
		if (this.contextId != null) {
			url += "?contextId=" + contextId;
		}
		CommandExecutor executor = new RemoteTaskCommandExecutor(this.url, url, deploymentId, authenticationType, username, password);
		return new CommandBasedTaskService(executor);
	}

}
