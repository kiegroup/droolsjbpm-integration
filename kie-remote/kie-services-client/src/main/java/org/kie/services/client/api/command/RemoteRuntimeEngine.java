package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.services.client.api.RemoteConfiguration;

public class RemoteRuntimeEngine implements RuntimeEngine {

    private final RemoteConfiguration config;

    public RemoteRuntimeEngine(RemoteConfiguration configuration) {
        this.config = configuration;
    }

    public KieSession getKieSession() {
        CommandService commandService;
        if (config.isRest()) {
            String url = config.getUrl() + "/runtime/" + config.getDeploymentId() + "/execute";
            commandService = new RemoteSessionCommandService(url, config);
        } else {
            commandService = new RemoteSessionCommandService(config);
        }
        return new CommandBasedStatefulKnowledgeSession(commandService);
    }

    public TaskService getTaskService() {
        CommandExecutor executor;
        if (config.isRest()) {
            String url = config.getUrl() + "/task/execute";
            executor = new RemoteTaskCommandExecutor(url, config);
        } else {
            executor = new RemoteTaskCommandExecutor(config);
        }
        return new CommandBasedTaskService(executor);
    }

}
