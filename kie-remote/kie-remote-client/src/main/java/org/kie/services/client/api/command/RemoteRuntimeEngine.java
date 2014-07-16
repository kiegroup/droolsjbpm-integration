package org.kie.services.client.api.command;

import org.drools.core.command.CommandService;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.jbpm.process.audit.AuditLogService;
import org.jbpm.process.audit.CommandBasedAuditLogService;
import org.jbpm.services.task.events.TaskEventSupport;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.task.TaskService;

/**
 * This {@link RuntimeEngine} instance acts as a remote client 
 * to the remote API (REST or JMS).
 */
public class RemoteRuntimeEngine implements RuntimeEngine {

    private final RemoteConfiguration config;

    public RemoteRuntimeEngine(RemoteConfiguration configuration) {
        this.config = configuration;
    }

    /**
     * @return a {@link KieSession} instance that acts a client instance to the remote API
     */
    public KieSession getKieSession() {
        CommandService commandService = new RemoteSessionCommandService(config);
        return new CommandBasedStatefulKnowledgeSession(commandService);
    }

    /**
     * @return a {@link TaskService} instance that acts as a client instance to the remote API
     */
    public TaskService getTaskService() {
        CommandExecutor executor = new RemoteTaskCommandExecutor(config);
        return new CommandBasedTaskService((CommandService)executor, new TaskEventSupport());
    }
   
    /**
     * @return a {@link AuditLogService} instance that acts as a client instance to the remote API
     */
    public AuditLogService getAuditLogService() { 
        CommandService commandService = new RemoteSessionCommandService(config);
        return new CommandBasedAuditLogService(commandService);
    }

}
