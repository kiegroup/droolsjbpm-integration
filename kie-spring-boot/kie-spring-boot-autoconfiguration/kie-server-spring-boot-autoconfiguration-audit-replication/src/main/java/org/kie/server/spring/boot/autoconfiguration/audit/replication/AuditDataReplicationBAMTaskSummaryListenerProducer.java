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

import org.jbpm.services.task.events.DefaultTaskEventListener;
import org.jbpm.services.task.lifecycle.listeners.BAMTaskEventListener;
import org.kie.api.task.TaskEvent;
import org.springframework.beans.factory.annotation.Autowired;

import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.BAM_TASK_EVENT;

public class AuditDataReplicationBAMTaskSummaryListenerProducer extends DefaultTaskEventListener {

    @Autowired
    private JMSSender jmsSender;

    @Override
    public void afterTaskStartedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskActivatedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskClaimedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {

        sendJMSTask(event);
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskStoppedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskReleasedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskDelegatedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskNominatedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        sendJMSTask(event);
    }

    protected void sendJMSTask(TaskEvent event) {
        Object msg = event.getMetadata().get(BAMTaskEventListener.METADATA_BAMTASK_EVENT);
        if (msg != null) {
            jmsSender.sendMessage(msg, BAM_TASK_EVENT);
            event.getMetadata().remove(BAMTaskEventListener.METADATA_BAMTASK_EVENT);
        }
    }

}
