package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.impl.EnvironmentFactory;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.CommandBasedAuditLogService;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteSessionCommandService;
import org.kie.services.client.api.command.RemoteTaskCommandExecutor;

public class RemoteRuntimeEngine implements RuntimeEngine {

    private final RemoteConfiguration config;

    public RemoteRuntimeEngine(RemoteConfiguration configuration) {
        this.config = configuration;
    }

    public KieSession getKieSession() {
        CommandService commandService = new RemoteSessionCommandService(config);
        return new CommandBasedStatefulKnowledgeSession(commandService);
    }

    public TaskService getTaskService() {
        CommandExecutor executor = new RemoteTaskCommandExecutor(config);
        return new CommandBasedTaskService((CommandService)executor, new TaskEventSupport());
    }
    
    public AuditLogService getAuditLogService() { 
        CommandService commandService = new RemoteSessionCommandService(config);
        return new CommandBasedAuditLogService(commandService);
    }

}
