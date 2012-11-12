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

package org.drools.container.spring;

import org.kie.event.rule.*;

public class MockAgendaEventListener implements AgendaEventListener {

    public void activationCreated(ActivationCreatedEvent activationCreatedEvent) {
        System.out.println("MockAgendaEventListener:: activationCreated");
    }

    public void activationCancelled(ActivationCancelledEvent activationCancelledEvent) {
        System.out.println("MockAgendaEventListener:: activationCancelled");
    }

    public void beforeActivationFired(BeforeActivationFiredEvent beforeActivationFiredEvent) {
        System.out.println("MockAgendaEventListener:: beforeActivationFired");
        SpringDroolsListenersTest.incrementValueFromListener();
    }

    public void afterActivationFired(AfterActivationFiredEvent afterActivationFiredEvent) {
        System.out.println("MockAgendaEventListener:: afterActivationFired");
    }

    public void agendaGroupPopped(AgendaGroupPoppedEvent agendaGroupPoppedEvent) {
        System.out.println("MockAgendaEventListener:: agendaGroupPopped");
    }

    public void agendaGroupPushed(AgendaGroupPushedEvent agendaGroupPushedEvent) {
        System.out.println("MockAgendaEventListener:: agendaGroupPushed");
    }

    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        System.out.println("MockAgendaEventListener:: beforeRuleFlowGroupActivated");
    }

    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        System.out.println("MockAgendaEventListener:: afterRuleFlowGroupActivated");
    }

    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        System.out.println("MockAgendaEventListener:: beforeRuleFlowGroupDeactivated");
    }

    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        System.out.println("MockAgendaEventListener:: afterRuleFlowGroupDeactivated");
    }

}
