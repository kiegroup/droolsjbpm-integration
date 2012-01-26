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

package org.drools.examples.carinsurance.workflow;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.command.World;
import org.drools.examples.carinsurance.domain.Car;
import org.drools.examples.carinsurance.domain.CarType;
import org.drools.examples.carinsurance.domain.Driver;
import org.drools.examples.carinsurance.domain.policy.CoverageType;
import org.drools.examples.carinsurance.domain.request.CoverageRequest;
import org.drools.examples.carinsurance.domain.request.PolicyRequest;
import org.drools.fluent.simulation.SimulationFluent;
import org.drools.fluent.simulation.impl.DefaultSimulationFluent;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PolicyApprovalWorkflowTest {

    @Test
    public void approvePolicyRequest() {
        SimulationFluent simulationFluent = new DefaultSimulationFluent();

        Map<String, Object> processParams = new HashMap<String, Object>();

        Driver john = new Driver("John", "Smith", new LocalDate(1970, 1, 1));
        Car mini = new Car("MINI-01", CarType.SMALL, false, new BigDecimal("10000.00"));
        PolicyRequest johnMiniPolicyRequest = new PolicyRequest(john, mini);
        johnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COLLISION));
        johnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COMPREHENSIVE));
        processParams.put("policyRequest", johnMiniPolicyRequest);

        assertEquals(false, johnMiniPolicyRequest.isManuallyApproved());
        // @formatter:off          
        simulationFluent
        .newKnowledgeBuilder()
            .add(ResourceFactory.newClassPathResource("org/drools/examples/carinsurance/workflow/policyRequestWorkflow.bpmn"),
                    ResourceType.BPMN2)
            .end(World.ROOT, KnowledgeBuilder.class.getName())
        .newKnowledgeBase()
            .addKnowledgePackages()
            .end(World.ROOT, KnowledgeBase.class.getName())
        .newStatefulKnowledgeSession()
            .startProcess("policyRequestProcess", processParams)
            .end()
        .runSimulation();
        // @formatter:on
        assertEquals(true, johnMiniPolicyRequest.isManuallyApproved());
    }

}
