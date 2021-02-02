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

import org.jbpm.process.audit.AbstractAuditLoggerAdapter;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.process.SLAViolatedEvent;
import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.springframework.beans.factory.annotation.Autowired;

import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.NODE_ENTER_EVENT_TYPE;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.NODE_LEFT_EVENT_TYPE;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.NODE_SLA_VIOLATED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.PROCESS_COMPLETED_EVENT_TYPE;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.PROCESS_SLA_VIOLATED;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.PROCESS_START_EVENT_TYPE;
import static org.kie.server.spring.boot.autoconfiguration.audit.replication.MessageType.VAR_CHANGE_EVENT_TYPE;


/**
 * AuditListener
 */

public class AuditDataReplicationProcessEventProducer extends AbstractAuditLoggerAdapter {

    @Autowired
    private JMSSender jmsSender;


    @Override
    protected void processStarted(ProcessStartedEvent event) {
        ProcessInstanceLog log = (ProcessInstanceLog) getProcessInstanceMetadata(event.getProcessInstance(), METADATA_PROCESSINTANCE_LOG);
        if (log != null) {
            jmsSender.sendMessage(log, PROCESS_START_EVENT_TYPE);
        }
    }

    @Override
    protected void processCompleted(ProcessCompletedEvent event) {
        ProcessInstanceLog log = (ProcessInstanceLog) getProcessInstanceMetadata(event.getProcessInstance(), METADATA_PROCESSINTANCE_LOG);
        if (log != null) {
            jmsSender.sendMessage(log, PROCESS_COMPLETED_EVENT_TYPE);
        }
    }

    @Override
    protected void nodeEnter(ProcessNodeTriggeredEvent event) {
        NodeInstanceLog log = (NodeInstanceLog) getNodeInstanceMetadata(event.getNodeInstance(), METADATA_NODEINSTANCE_LOG);
        if (log != null) {
            jmsSender.sendMessage(log, NODE_ENTER_EVENT_TYPE);
        }
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        super.beforeNodeTriggered(event);
        NodeInstanceLog log = (NodeInstanceLog) getNodeInstanceMetadata(event.getNodeInstance(), METADATA_NODEINSTANCE_LOG);
        if (log != null) {
            jmsSender.sendMessage(log, NODE_LEFT_EVENT_TYPE);
        }
    }

    @Override
    protected void nodeLeft(ProcessNodeLeftEvent event) {
        NodeInstanceLog log = (NodeInstanceLog) getNodeInstanceMetadata(event.getNodeInstance(), METADATA_NODEINSTANCE_LOG);
        if (log != null) {
            jmsSender.sendMessage(log, NODE_LEFT_EVENT_TYPE);
        }
    }

    @Override
    protected void variableChanged(ProcessVariableChangedEvent event) {
        List<VariableInstanceLog> logs = (List<VariableInstanceLog>) getProcessInstanceMetadata(event.getProcessInstance(), METADATA_VARIABLEINSTANCE_LOG);
        if (logs != null) {
            logs.forEach(log -> jmsSender.sendMessage(log, VAR_CHANGE_EVENT_TYPE));
        }
    }

    @Override
    protected void slaProcessInstanceViolated(SLAViolatedEvent event) {
        ProcessInstanceLog log = (ProcessInstanceLog) getProcessInstanceMetadata(event.getProcessInstance(), METADATA_PROCESSINTANCE_LOG);
        if (log != null) {
            jmsSender.sendMessage(log, PROCESS_SLA_VIOLATED);
        }
    }

    @Override
    protected void slaNodeInstanceViolated(SLAViolatedEvent event) {
        NodeInstanceLog log = (NodeInstanceLog) getNodeInstanceMetadata(event.getNodeInstance(), METADATA_NODEINSTANCE_LOG);
        if (log != null) {
            jmsSender.sendMessage(log, NODE_SLA_VIOLATED);
        }
    }




}