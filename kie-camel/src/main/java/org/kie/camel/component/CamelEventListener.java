/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.camel.component;

import java.util.List;

import org.drools.core.audit.event.ActivationLogEvent;
import org.drools.core.audit.event.LogEvent;
import org.drools.core.audit.event.ObjectLogEvent;
import org.drools.core.audit.event.RuleBaseLogEvent;
import org.drools.core.audit.event.RuleFlowGroupLogEvent;
import org.drools.core.audit.event.RuleFlowLogEvent;
import org.drools.core.audit.event.RuleFlowNodeLogEvent;
import org.drools.core.audit.event.RuleFlowVariableLogEvent;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.impl.StatelessKnowledgeSessionImpl;
import org.drools.core.runtime.process.InternalProcessRuntime;
import org.drools.core.spi.Activation;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.NodeContainer;
import org.kie.api.event.KieRuntimeEvent;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.event.kiebase.AfterFunctionRemovedEvent;
import org.kie.api.event.kiebase.AfterKieBaseLockedEvent;
import org.kie.api.event.kiebase.AfterKieBaseUnlockedEvent;
import org.kie.api.event.kiebase.AfterKiePackageAddedEvent;
import org.kie.api.event.kiebase.AfterKiePackageRemovedEvent;
import org.kie.api.event.kiebase.AfterProcessAddedEvent;
import org.kie.api.event.kiebase.AfterProcessRemovedEvent;
import org.kie.api.event.kiebase.AfterRuleAddedEvent;
import org.kie.api.event.kiebase.AfterRuleRemovedEvent;
import org.kie.api.event.kiebase.BeforeFunctionRemovedEvent;
import org.kie.api.event.kiebase.BeforeKieBaseLockedEvent;
import org.kie.api.event.kiebase.BeforeKieBaseUnlockedEvent;
import org.kie.api.event.kiebase.BeforeKiePackageAddedEvent;
import org.kie.api.event.kiebase.BeforeKiePackageRemovedEvent;
import org.kie.api.event.kiebase.BeforeProcessAddedEvent;
import org.kie.api.event.kiebase.BeforeProcessRemovedEvent;
import org.kie.api.event.kiebase.BeforeRuleAddedEvent;
import org.kie.api.event.kiebase.BeforeRuleRemovedEvent;
import org.kie.api.event.kiebase.DefaultKieBaseEventListener;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.event.kiebase.KieBaseEventManager;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;
import org.kie.internal.event.KnowledgeRuntimeEventManager;
import org.kie.internal.runtime.KnowledgeRuntime;

public class CamelEventListener implements
        RuleRuntimeEventListener,
        AgendaEventListener,
        ProcessEventListener,
        KieBaseEventListener {

    public static final String AGENDA_EVENT = "AgendaEvent";
    public static final String RULE_RUNTIME_EVENT = "RuleRuntimeEvent";
    public static final String PROCESS_EVENT = "ProcessEvent";
    public static final String KIE_BASE_EVENT = "KieBaseEvent";

    private KnowledgeRuntime krt;
    private KieConsumer kieConsumer;

    public CamelEventListener(KnowledgeRuntime krt, KieConsumer kieConsumer) {
        this.krt = krt;
        this.kieConsumer = kieConsumer;
    }

    public void addEventListener(String eventType) {
        if (AGENDA_EVENT.equals(eventType) && krt instanceof KieRuntimeEventManager) {
            krt.addEventListener( (AgendaEventListener) this );
        } else if (RULE_RUNTIME_EVENT.equals(eventType) && krt instanceof KieRuntimeEventManager) {
            krt.addEventListener( (RuleRuntimeEventListener) this );
        } else if (PROCESS_EVENT.equals(eventType) && krt instanceof KieRuntimeEventManager) {
            krt.addEventListener( (ProcessEventListener) this );
        } else if (KIE_BASE_EVENT.equals(eventType) && krt instanceof KieBaseEventManager) {
            KieBaseEventManager kbem = (KieBaseEventManager) krt;
            kbem.addEventListener( (KieBaseEventListener) this );
        } else {
            throw new IllegalArgumentException("Not supported eventType: " + eventType + " for class " + krt.getClass());
        }
    }

    public void removeEventListener(String eventType) {
        if (AGENDA_EVENT.equals(eventType) && krt instanceof KieRuntimeEventManager) {
            krt.removeEventListener((AgendaEventListener) this);
        } else if (RULE_RUNTIME_EVENT.equals(eventType) && krt instanceof KieRuntimeEventManager) {
            krt.removeEventListener((RuleRuntimeEventListener) this);
        } else if (PROCESS_EVENT.equals(eventType) && krt instanceof KieRuntimeEventManager) {
            krt.removeEventListener((ProcessEventListener) this);
        } else if (KIE_BASE_EVENT.equals(eventType) && krt instanceof KieBaseEventManager) {
            KieBaseEventManager kbem = (KieBaseEventManager) krt;
            kbem.removeEventListener((KieBaseEventListener) this);
        } else {
            throw new IllegalArgumentException("Not supported eventType: " + eventType + " for class " + krt.getClass());
        }
    }

    private void processEvent(Object event) {
        kieConsumer.process(event);
    }

    @Override
    public void matchCreated(MatchCreatedEvent event) {
        processEvent(event);
    }

    @Override
    public void matchCancelled(MatchCancelledEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        processEvent(event);
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        processEvent(event);
    }

    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
        processEvent(event);
    }

    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeKiePackageAdded(BeforeKiePackageAddedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterKiePackageAdded(AfterKiePackageAddedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeKiePackageRemoved(BeforeKiePackageRemovedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterKiePackageRemoved(AfterKiePackageRemovedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeKieBaseLocked(BeforeKieBaseLockedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterKieBaseLocked(AfterKieBaseLockedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeKieBaseUnlocked(BeforeKieBaseUnlockedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterKieBaseUnlocked(AfterKieBaseUnlockedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeRuleAdded(BeforeRuleAddedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterRuleAdded(AfterRuleAddedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeRuleRemoved(BeforeRuleRemovedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterRuleRemoved(AfterRuleRemovedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeFunctionRemoved(BeforeFunctionRemovedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterFunctionRemoved(AfterFunctionRemovedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeProcessAdded(BeforeProcessAddedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterProcessAdded(AfterProcessAddedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeProcessRemoved(BeforeProcessRemovedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterProcessRemoved(AfterProcessRemovedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        processEvent(event);
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        processEvent(event);
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        processEvent(event);
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        processEvent(event);
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        processEvent(event);
    }

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        processEvent(event);
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        processEvent(event);
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        processEvent(event);
    }
}
