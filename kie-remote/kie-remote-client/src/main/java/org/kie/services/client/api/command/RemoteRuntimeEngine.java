package org.kie.services.client.api.command;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.audit.AuditService;
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
        return new KieSessionClientCommandObject(config);
    }

    /**
     * @return a {@link TaskService} instance that acts as a client instance to the remote API
     */
    public TaskService getTaskService() {
        return new TaskServiceClientCommandObject(config);
    }
   
    /**
     * @return a {@link AuditLogService} instance that acts as a client instance to the remote API
     */
    public AuditService getAuditLogService() { 
        return new AuditServiceClientCommandObject(config);
    }

}
