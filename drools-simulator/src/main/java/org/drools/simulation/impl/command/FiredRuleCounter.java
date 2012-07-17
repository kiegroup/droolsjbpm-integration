/*
 * Copyright 2012 JBoss Inc
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

package org.drools.simulation.impl.command;

import org.drools.event.rule.ActivationCancelledEvent;
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.AgendaGroupPoppedEvent;
import org.drools.event.rule.AgendaGroupPushedEvent;
import org.drools.event.rule.BeforeActivationFiredEvent;
import org.drools.event.rule.RuleFlowGroupActivatedEvent;
import org.drools.event.rule.RuleFlowGroupDeactivatedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FiredRuleCounter implements AgendaEventListener {

    private List<String> inclusiveRuleNameList = null;
    private List<String> exclusiveRuleNameList = null;

    private Map<String, Integer> ruleNameToFireCountMap = new HashMap<String, Integer>();

    public FiredRuleCounter() {
    }

    public List<String> getInclusiveRuleNameList() {
        return inclusiveRuleNameList;
    }

    public void setInclusiveRuleNameList(List<String> inclusiveRuleNameList) {
        this.inclusiveRuleNameList = inclusiveRuleNameList;
    }

    public List<String> getExclusiveRuleNameList() {
        return exclusiveRuleNameList;
    }

    public void setExclusiveRuleNameList(List<String> exclusiveRuleNameList) {
        this.exclusiveRuleNameList = exclusiveRuleNameList;
    }

    // Events

    public void activationCreated(ActivationCreatedEvent event) {
    }

    public void activationCancelled(ActivationCancelledEvent event) {
    }

    public void beforeActivationFired(BeforeActivationFiredEvent event) {
    }

    public void afterActivationFired(AfterActivationFiredEvent event) {
        String ruleName = event.getActivation().getRule().getName();
        if (acceptRuleName(ruleName)) {
            incrementFireCount(ruleName);
        }
    }

    private boolean acceptRuleName(String ruleName) {
        if (inclusiveRuleNameList != null) {
            if (!inclusiveRuleNameList.contains(ruleName)) {
                return false;
            }
        }
        if (exclusiveRuleNameList != null) {
            if (exclusiveRuleNameList.contains(ruleName)) {
                return false;
            }
        }
        return true;
    }

    private void incrementFireCount(String ruleName) {
        Integer fireCount = ruleNameToFireCountMap.get(ruleName);
        if (fireCount == null) {
            fireCount = 1;
        } else {
            fireCount++;
        }
        ruleNameToFireCountMap.put(ruleName, fireCount);
    }

    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
    }

    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
    }

    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
    }

    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
    }

    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
    }

    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
    }

    // Results

    public int getRuleNameFireCount(String ruleName) {
        Integer fireCount = ruleNameToFireCountMap.get(ruleName);
        return fireCount == null ? 0 : fireCount;
    }

}
