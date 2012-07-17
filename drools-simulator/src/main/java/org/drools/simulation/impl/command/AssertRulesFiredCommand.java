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

import org.drools.command.Context;
import org.drools.command.impl.GenericCommand;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class AssertRulesFiredCommand implements GenericCommand<Object> {

    private FiredRuleCounter firedRuleCounter;

    private Map<String, Integer> ruleNameToExpectedFireCountMap = new HashMap<String, Integer>();

    public AssertRulesFiredCommand() {
    }

    public AssertRulesFiredCommand(FiredRuleCounter firedRuleCounter) {
        this.firedRuleCounter = firedRuleCounter;
    }

    public FiredRuleCounter getFiredRuleCounter() {
        return firedRuleCounter;
    }

    public void setFiredRuleCounter(FiredRuleCounter firedRuleCounter) {
        this.firedRuleCounter = firedRuleCounter;
    }

    public void addAssertRuleFired(String ruleName, int fireCount) {
        ruleNameToExpectedFireCountMap.put(ruleName, fireCount);
    }

    public void addAssertRuleNotFired(String ruleName) {
        addAssertRuleFired(ruleName, 0);
    }

    // Execute

    public Object execute(Context context) {
        for (Map.Entry<String, Integer> entry : ruleNameToExpectedFireCountMap.entrySet()) {
            String ruleName = entry.getKey();
            int expectedFireCount = entry.getValue();
            int actualFireCount = firedRuleCounter.getRuleNameFireCount(ruleName);
            Assert.assertEquals("The rule (" + ruleName + ")'s fireCount is incorrect.",
                    expectedFireCount, actualFireCount);
        }
        return null;
    }

}
