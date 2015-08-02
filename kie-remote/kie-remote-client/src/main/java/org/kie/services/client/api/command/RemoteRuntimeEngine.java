/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.services.client.api.command;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.task.TaskService;
import org.kie.remote.client.api.RemoteTaskService;

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
     * @return a {@link AuditService} instance that acts as a client instance to the remote API
     */
    public AuditService getAuditService() { 
        return new AuditServiceClientCommandObject(config);
    }

    /**
     * @return a {@link RemoteTaskService} instance that acts as a client instance to the remote API
     */
    public RemoteTaskService getRemoteTaskService() {
        return new RemoteTaskServiceClientImpl(config);
    }
}
