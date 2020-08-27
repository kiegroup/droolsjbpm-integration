/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.spring.boot.autoconfiguration.audit.replication;

import java.util.List;
import java.util.Map;

import org.jbpm.services.task.audit.JPATaskLifeCycleEventListener;
import org.jbpm.services.task.events.DefaultTaskEventListener;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.task.api.TaskVariable;
import org.springframework.beans.factory.annotation.Autowired;

import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_ACTIVATED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_ADDED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_CLAIMED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_COMPLETED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_DELEGATED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_EXITED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_FAILED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_FORWARDED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_NOMINATED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_REASSIGNED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_RELEASED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_RESUMED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_SKIPPED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_STARTED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_STOPPED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_SUSPENDED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_VAR_IN_CHANGED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.TASK_VAR_OUT_CHANGED;


public class AuditDataReplicationTaskLifeCycleEventListenerProducer extends DefaultTaskEventListener {

    @Autowired
    private JMSSender jmsSender;


    private void sendMessage(TaskEvent event, int type) {
        Object msg = event.getMetadata().get(JPATaskLifeCycleEventListener.METADATA_TASK_EVENT);
        if (msg != null) {
            jmsSender.sendMessage(msg, type);
            event.getMetadata().remove(JPATaskLifeCycleEventListener.METADATA_TASK_EVENT);
        }

        msg = event.getMetadata().get(JPATaskLifeCycleEventListener.METADATA_AUDIT_TASK);
        if (msg != null) {
            jmsSender.sendMessage(msg, type);
            event.getMetadata().remove(JPATaskLifeCycleEventListener.METADATA_AUDIT_TASK);
        }

        msg = event.getMetadata().get(JPATaskLifeCycleEventListener.METADATA_VAR_EVENT);
        if (msg != null) {
            List<TaskVariable> variables = (List<TaskVariable>) msg;
            variables.forEach(e -> jmsSender.sendMessage(e, type));
            event.getMetadata().remove(JPATaskLifeCycleEventListener.METADATA_VAR_EVENT);
        }
    }

    @Override
    public void afterTaskActivatedEvent(TaskEvent event) {
        sendMessage(event, TASK_ACTIVATED);
    }

    @Override
    public void afterTaskClaimedEvent(TaskEvent event) {
        sendMessage(event, TASK_CLAIMED);
    }

    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        sendMessage(event, TASK_SKIPPED);

    }

    @Override
    public void afterTaskStartedEvent(TaskEvent event) {
        sendMessage(event, TASK_STARTED);
    }

    @Override
    public void afterTaskStoppedEvent(TaskEvent event) {
        sendMessage(event, TASK_STOPPED);
    }

    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {
        sendMessage(event, TASK_COMPLETED);
    }

    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        sendMessage(event, TASK_FAILED);
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        sendMessage(event, TASK_ADDED);
    }

    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        sendMessage(event, TASK_EXITED);
    }

    @Override
    public void beforeTaskReleasedEvent(TaskEvent event) {
        sendMessage(event, TASK_RELEASED);
    }

    @Override
    public void afterTaskReleasedEvent(TaskEvent event) {
        sendMessage(event, TASK_RELEASED);
    }

    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        sendMessage(event, TASK_RESUMED);
    }

    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        sendMessage(event, TASK_SUSPENDED);
    }

    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
        sendMessage(event, TASK_FORWARDED);
    }

    @Override
    public void afterTaskDelegatedEvent(TaskEvent event) {
        sendMessage(event, TASK_DELEGATED);
    }

    @Override
    public void afterTaskNominatedEvent(TaskEvent event) {
        sendMessage(event, TASK_NOMINATED);
    }
    

    @Override
    public void afterTaskOutputVariableChangedEvent(TaskEvent event, Map<String, Object> variables) {
        sendMessage(event, TASK_VAR_OUT_CHANGED);
    }

    @Override
    public void afterTaskInputVariableChangedEvent(TaskEvent event, Map<String, Object> variables) {
        sendMessage(event, TASK_VAR_IN_CHANGED);
    }

    @Override
    public void afterTaskAssignmentsAddedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities) {                
        sendMessage(event, TASK_REASSIGNED);
    }

    @Override
    public void afterTaskAssignmentsRemovedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities) {
        sendMessage(event, TASK_REASSIGNED);
    }

}
