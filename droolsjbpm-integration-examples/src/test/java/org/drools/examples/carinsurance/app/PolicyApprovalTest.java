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

package org.drools.examples.carinsurance.app;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.command.ContextManager;
import org.drools.examples.carinsurance.domain.Car;
import org.drools.examples.carinsurance.domain.CarType;
import org.drools.examples.carinsurance.domain.Driver;
import org.drools.examples.carinsurance.domain.policy.CoverageType;
import org.drools.examples.carinsurance.domain.request.CoverageRequest;
import org.drools.examples.carinsurance.domain.request.PolicyRequest;
import org.drools.fluent.VariableContext;
import org.drools.fluent.standard.FluentStandardSimulation;
import org.drools.fluent.standard.imp.FluentStandardSimulationImpl;
import org.drools.io.ResourceFactory;
import org.drools.simulation.impl.SimulationImpl;
import org.drools.simulation.impl.Simulator;
import org.joda.time.LocalDate;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PolicyApprovalTest {

    @Test
    public void approvePolicyRequest() {
        FluentStandardSimulation simulationFluent = new FluentStandardSimulationImpl();

        Driver john = new Driver("John", "Smith", new LocalDate(1970, 1, 1));
        Car mini = new Car("MINI-01", CarType.SMALL, false, new BigDecimal("10000.00"));
        PolicyRequest johnMiniPolicyRequest = new PolicyRequest(john, mini);
        johnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COLLISION));
        johnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COMPREHENSIVE));


        // @formatter:off          
        simulationFluent.newPath("init")
            .newStep(0)
                .newKnowledgeBuilder()
                    .add(ResourceFactory.newClassPathResource("org/drools/examples/carinsurance/rule/policyApprovalRules.drl"),
                            ResourceType.DRL)
                    .end(ContextManager.ROOT, KnowledgeBuilder.class.getName())
                .newKnowledgeBase()
                    .addKnowledgePackages()
                    .end(ContextManager.ROOT, KnowledgeBase.class.getName())
                .newStatefulKnowledgeSession()
                    .insert(john).set("john")
                    .insert(mini).set("mini")
                    .insert(johnMiniPolicyRequest).set("johnMiniPolicyRequest")
                    .fireAllRules()
                    .test("johnMiniPolicyRequest.automaticallyDisapproved == false")
                    .test("johnMiniPolicyRequest.disapprovalMessageList.size() == 0")
                    .end()
                .end()
            .end();
        // @formatter:on

        runSimulation(simulationFluent);
    }

    @Test @Ignore("TODO FIXME")
    public void rejectMinors() {
        FluentStandardSimulation simulationFluent = new FluentStandardSimulationImpl();

        Driver john = new Driver("John", "Smith", new LocalDate().minusYears(10));
        Car mini = new Car("MINI-01", CarType.SMALL, false, new BigDecimal("10000.00"));
        PolicyRequest johnMiniPolicyRequest = new PolicyRequest(john, mini);
        johnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COLLISION));
        johnMiniPolicyRequest.addCoverageRequest(new CoverageRequest(CoverageType.COMPREHENSIVE));


        // @formatter:off
        simulationFluent.newPath("init")
            .newStep(0)
                .newKnowledgeBuilder()
                    .add(ResourceFactory.newClassPathResource("org/drools/examples/carinsurance/rule/policyApprovalRules.drl"),
                            ResourceType.DRL)
                    .end(ContextManager.ROOT, KnowledgeBuilder.class.getName())
                .newKnowledgeBase()
                    .addKnowledgePackages()
                    .end(ContextManager.ROOT, KnowledgeBase.class.getName())
                .newStatefulKnowledgeSession()
                    .insert(john).set("john")
                    .insert(mini).set("mini")
                    .insert(johnMiniPolicyRequest).set("johnMiniPolicyRequest")
                    .fireAllRules()
                    .test("johnMiniPolicyRequest.automaticallyDisapproved == true")
                    .test("johnMiniPolicyRequest.disapprovalMessageList.size() == 1")
                    .end()
                .end()
            .end();
        // @formatter:on

        runSimulation(simulationFluent);
    }
    
    private void runSimulation(FluentStandardSimulation f) {
        SimulationImpl sim = (SimulationImpl) ((FluentStandardSimulationImpl) f).getSimulation();
    
        Simulator simulator = new Simulator( sim,
                                             new Date().getTime() );
        simulator.run();
    }       

}
